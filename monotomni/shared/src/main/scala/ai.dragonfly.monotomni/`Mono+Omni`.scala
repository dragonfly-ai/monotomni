package ai.dragonfly.monotomni

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import java.util.concurrent.atomic.AtomicLong

import ai.dragonfly.math.stats.stream.{Gaussian, Poisson}
import ai.dragonfly.monotomni.connection.TimeServerConnection

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import ai.dragonfly.monotomni.native

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}


object `Mono+Omni` {
  /*
   * dawnOfTime = 1615141312110L
   * Helpful mnemonic: the digits of dawnOfTime count down from 16 to 11 then ends with a 0: 16 15 14 13 12 11 0 L
   * println(new java.util.Date(1615141312110L)) -> Sun Mar 07 11:21:52 MST 2021
   */
  final val dawnOfTime:Long = 1615141312110L // timestamp of the birth of this library
  final val host: Byte = native.HostName.hostName.hashCode().toByte
  private final val counter:AtomicLong = new AtomicLong(0L)  // overflows safely with bitmask.  No need to ever reset it.
  private def count():Long = 0x0000000000000fffL & counter.getAndIncrement() // mask off all but least significant 24 bits.
  @inline def apply(ts:Long = System.currentTimeMillis()):MOI = (((ts - dawnOfTime) >>> 8) << 32) | (count() << 8) | host
}

object Mono {
  @inline def +(o: `Mono+Omni`.type):`Mono+Omni`.type = o
  @inline def +(moi: MOI):MOI = moi // also handles Long
  @inline def +(i: Int):Int = i
  @inline def +(b: Byte):Byte = b
}

object `Remo+Ami` {
  def apply(moi:MOI = Mono+Omni())(implicit r:Remote):Future[AMI] = r(moi)
}

object Remo {
  @inline def +(o: `Remo+Ami`.type):`Remo+Ami`.type = o
  @inline def +(ami: AMI):AMI = ami // also handles Long
  @inline def +(fami:Future[AMI]):Future[AMI] = fami // also handles Long
  @inline def +(i: Int):Int = i
  @inline def +(b: Byte):Byte = b
  @inline def +():Unit = ()
}

/**
 * Remote manages TimeServer clock approximation.
 * @param timeServerConnection connection to a remote time server.
 * @param maxWait maximum wait time, in milliseconds, between time Trials.
 */
class Remote(private val timeServerConnection: TimeServerConnection, maxWait:Long = 60000) {

  def apply(moi:MOI = Mono+Omni()):Future[AMI] = for { dT <- `E(Δt)`} yield moi + (dT << 32)

  /*
   * Lq = latency of client requests to the server.
   * Ls = latency of server responses to the client.
   * L = Lag = Lq + Ls
   *
   * |Lq| = normalized Lq = Lq / L
   */

  private var L:Poisson = new Poisson
  private var `|Lq|`:Gaussian = new Gaussian  // Estimates Lag(RequestTime) / Lag

  /* ts = timeTrial.serverTimeStamp
   * ΔT = ts - ( S + Lq ) */
  private val `ΔT`:AtomicLong = new AtomicLong(0L)
  private val successTrials:AtomicLong = new AtomicLong(0L)
  private val failedTrials:AtomicLong = new AtomicLong(0L)

  private lazy val `promisedΔT1`:Promise[() => Long] = Promise[() => Long]()

  /**
   * @return E(Δt)
   */
  private def `E(Δt)`:Future[Long] = for { ready <- `promisedΔT1`.future } yield ready()

  private def setDeltaT(deltaT:Long):Unit = synchronized {
    // only set ΔT if it changes.
    if (deltaT != `ΔT`.get()) {
      `ΔT`.set(deltaT)
      `|Lq|` = new Gaussian
      `|Lq|`(0.5) // reset request lag model: Lq
    }
  }

  /**
   * return the suggested wait time before next trial
   */
  private lazy val logTimeTrial:(PendingTimeTrial, TimeTrial, Long) => Long = {
    `promisedΔT1`.success( () => { `ΔT`.get() } )
    (pendingTimeTrial:PendingTimeTrial, timeTrial:TimeTrial, E:Long) => synchronized {
      val dT:Long = `ΔT`.get()
      val ts:Long = timeTrial.serverTimeStamp
      val S:Long = pendingTimeTrial.start

      val l:Int = (E - S).toInt  // total lag

      if (l < 0) {
        // This should never happen unless the local clock changes during a time trial.
        println(s"logTimeTrial($pendingTimeTrial, $timeTrial) =>\n\t[ lag = $l ] // negative lag?")
        failedTrials.incrementAndGet()
        0L
      } else if (l > pendingTimeTrial.timeoutMilliseconds) {
        // This also shouldn't happen.  Timeout exceptions should get caught by the timeTrial method and the scheduler.
        println(s"logTimeTrial($pendingTimeTrial, $timeTrial) =>\n\t[ lag = $l, timeoutMilliseconds = ${pendingTimeTrial.timeoutMilliseconds} ] // lag exceeded timeout.")
        failedTrials.incrementAndGet()
        0L
      } else {

        val waitTime:Long = if (l == 0) {
          setDeltaT(ts - S)
          println(s"logTimeTrial($pendingTimeTrial, $timeTrial) =>\n\t[ lag = $l, error = 0, |error| = 0 ] // 0 Lag!  Perfect Trial!\n\tState: $this")
          maxWait
        } else if (successTrials.get() < 1) {
          setDeltaT(ts - S + (l * 0.5).toLong)
          println(s"logTimeTrial($pendingTimeTrial, $timeTrial) =>\n\t[ lag = $l ] // First Trial.\n\tState: $this")
          0L
        } else {
          /* E(ts) = expected value of ts given current model of `|Lq|`
           * val ets = E(ts) = S + `ΔT`.get + (`|Lq|`.mean * l) */
          val min_E_ts: Long = S + dT // 0 Lq
          val E_ts: Long = (dT + S + (`|Lq|`.mean * l)).toLong // estimated Lq
          val max_E_ts: Long = S + dT + l // Lq = 1

          val predictionError: Long = ts - E_ts

          if (ts <= min_E_ts) { // ts is earlier than Lq = 0 predicts
            setDeltaT(ts - S)
            println(s"logTimeTrial($pendingTimeTrial, $timeTrial) =>\n\t[ lag = $l, Emin(ts) = $min_E_ts ] // ts < E(ts) | Lq = 0\n\tState: $this")
          } else if (ts > max_E_ts) {  // ts is later than Lq = 1 predicts
            setDeltaT(ts - E)
            println(s"logTimeTrial($pendingTimeTrial, $timeTrial) =>\n\t[ lag = $l, Emax(ts) = $max_E_ts ] // ts > E(ts) | Lq = 1\n\tState: $this")
          } else if (l < L.mean) {  // |Lq| considers trials with low lag and ignores trials with high lag.

            val normalizedPredictionError: Double = predictionError / l.toDouble
            val lq: Double = `|Lq|`.mean + normalizedPredictionError

            if (l <= L.min) {
              // ΔT = ts - ( S + Lq )
              setDeltaT(ts - S + (lq * l).toLong)
            }

            `|Lq|`(lq)
            println(s"logTimeTrial($pendingTimeTrial, $timeTrial) =>\n\t[ lag = $l, |lq| = $lq, E(ts) = $E_ts, E(ts) - ts = $predictionError, |E(ts) - ts| = $normalizedPredictionError ]\n\tState: $this")
          } else {
            println(s"Ignoring High Lag trial: $l")
          }
          Math.min((1.0/Math.abs(predictionError / L.mean) * `|Lq|`.sampleSize*1000.0).toLong, maxWait)  // schedule next TimeTrial sooner if |Lq| lacks statistical support
        }
        L(l)
        successTrials.incrementAndGet()
        waitTime
      }
    }
  }

  import java.util.{Timer, TimerTask}
  val timeTrialScheduler:Timer = new Timer(s"Remote $this timeout monitor.")
  private def scheduleNextTrial(delay:Long, inBurstsOf:Int = 3):Unit = {
    println(s"scheduleNextTrial($delay MS /* waits ${delay / 1000} Seconds */)")
    timeTrialScheduler.schedule(
      new TimerTask() {
        override def run():Unit = {
          Future.traverse(
            // run inBurstsOf time trials
            Seq.tabulate[PendingTimeTrial](inBurstsOf)( _ => timeServerConnection.timeTrial() )
          ) {
            ptt => (for { timeTrial <- ptt.timeTrialPromise.future } yield {
              val end:Long = System.currentTimeMillis()
              logTimeTrial(ptt, timeTrial, end)
            }).recover { case t:Throwable => maxWait }  // if a trial fails, wait the maximum amount of time.
          } onComplete {
            case Success(waitTimes:Seq[Long]) => scheduleNextTrial( waitTimes.max )
            case Failure(t) => println(t) // try a few more times, then fail this TimeServerConnection?
          }
        }
      },
      delay
    )
  }

  override def toString:String = if (L.sampleSize > 0) {
    s"TimeServer Time Estimates : {\n\tsuccessTrials = $successTrials\n\tfailedTrials = $failedTrials\n\tE(Δt) = ${`ΔT`}\n\t|Lq| = ${`|Lq|`}\n\tlagModel = $L\n}"
  } else s"Initializing TimeServerConnection: $timeServerConnection"

  scheduleNextTrial(0L)
}

case class FailedToSynchronizeWithTimeServerConnection(timeServerConnection: TimeServerConnection) extends Exception(s"Failed to Synchronize over TimeServerConnection: $timeServerConnection")