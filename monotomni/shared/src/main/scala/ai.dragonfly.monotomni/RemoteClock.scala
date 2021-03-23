package ai.dragonfly.monotomni

import java.util.concurrent.atomic.AtomicLong

import ai.dragonfly.math.stats.stream.{Gaussian, Poisson}
import ai.dragonfly.monotomni.connection.TimeServerConnection
import slogging.LazyLogging

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import scala.scalajs.js
import js.annotation.{JSExport, JSExportAll}

object RemoteClock {
  val defaultMaxWait:Long = 60000L
  def apply(timeServerConnection: TimeServerConnection, maxWait:Long = defaultMaxWait):Future[RemoteClock] = {
    val r = new RemoteClock(timeServerConnection, maxWait)
    for {
      f <- r.promisedDT.future
    } yield r
  }
}

/**
 * RemoteClock manages TimeServer clock approximation.
 *
 * @param timeServerConnection connection to a remote time server.
 * @param maxWait maximum wait time, in milliseconds, between time Trials.
 */
class RemoteClock(private val timeServerConnection: TimeServerConnection, maxWait:Long=RemoteClock.defaultMaxWait) extends native.RemoteClock with LazyLogging {

  val state:RemoteClockState = RemoteClockState(this)
  private var live:Boolean = true

  /**
   * Generate an AMI based on best current estimates of ServerTime.
   * @param moi an optional MOI to convert into an AMI.
   * @return an AMI approximating the TimeServer's MOI.
   */
  override def ami(moi:MOI = Mono+Omni()):AMI = moi + (state.deltaT << 32)

  /**
   * Get the best current estimate of ServerTime right now.
   * @return the best current estimate of a ServerTime timestamp right now.
   */
  override def now():Long = System.currentTimeMillis() + state.deltaT

  private lazy val promisedDT: Promise[() => Long] = Promise[() => Long]()

  @JSExport def isReady:Boolean = promisedDT.isCompleted

  /**
   * Invoke callback parameter function as soon as a TimeServer estimate becomes available.
   * @param callback callback function.
   */
  override def ready(callback: () => Unit):Unit = {
    promisedDT.future onComplete {
      case Success(_) => callback()
      case Failure(t:Throwable) => throw t
    }
  }

  /**
   * Stops the TimeTrial scheduler if it is currently running.
   */
  @JSExport def stop():Unit = {
    if (this.live) {
      logger.warn(s"Shutting down TimeTrial scheduler on $this")
      this.live = false
    } else {
      logger.warn(s"Can't stop TimeTrial scheduler on $this because it is not running.")
    }

  }

  /**
   * Starts the TimeTrial scheduler if it is not already running.
   */
  @JSExport def start():Unit = {
    if (this.live) {
      logger.warn(s"Can't start TimeTrial scheduler $this because it is already running.")
    } else {
      logger.warn(s"Resuming TimeTrial Scheduler on $this")
      this.live = true
      scheduleNextTrial(0L)
    }
  }

  /**
   * return the suggested wait time before next trial
   */
  private lazy val logTimeTrial:(PendingTimeTrial, Long) => Long = {
    promisedDT.success( () => { state.deltaT } )
    (pendingTimeTrial:PendingTimeTrial, E:Long) => synchronized {
      val dT:Long = state.deltaT
      val timeTrial:TimeTrial = pendingTimeTrial.promisedTimeTrial.future.value.get.get // future completed upstream!
      val ts:Long = timeTrial.serverTimeStamp
      val S:Long = pendingTimeTrial.start

      val l:Int = (E - S).toInt  // total lag

      if (l < 0) {
        logger.warn(
          s"// Negative Lag? Did a local and/or server clock change?\n\t$this\n\t.logTimeTrial($pendingTimeTrial) =>\n\t\t[ lag = $l ]"
        )
        state.reportFailure()
        0L
      } else if (l > pendingTimeTrial.timeoutMilliseconds) {
        logger.warn(s"// This shouldn't happen!  PendingTimeTrial should catch Timeout\n\t$this\n\t.logTimeTrial($pendingTimeTrial) =>\n\t\t[ lag = $l, timeoutMilliseconds = ${pendingTimeTrial.timeoutMilliseconds} ] // lag exceeded timeout.")
        state.reportFailure()
        0L
      } else {
        val waitTime:Long = if (l == 0) {
          state.deltaT(ts - S)
          logger.debug(s"$this\n\t.logTimeTrial($pendingTimeTrial) =>\n\t\t[ lag = $l, error = 0, |error| = 0 ] // 0 Lag!  Perfect Trial!")
          maxWait
        } else if (state.successCount < 1) {
          state.deltaT(ts - S + (l * 0.5).toLong)
          logger.debug(s"$this\n\t.logTimeTrial($pendingTimeTrial) =>\n\t\t[ lag = $l ] // First Trial.")
          0L
        } else {
          /* E(ts) = expected value of ts given current model of `|Lq|`
           * val ets = E(ts) = S + `ΔT`.get + (`|Lq|`.mean * l) */
          val min_E_ts: Long = S + dT // 0 Lq
          val E_ts: Long = (dT + S + (state.E_NormalizedLq.mean * l)).toLong // estimated Lq
          val max_E_ts: Long = S + dT + l // Lq = 1

          val predictionError: Long = ts - E_ts

          if (ts <= min_E_ts) { // ts is earlier than Lq = 0 predicts
            state.deltaT(ts - S)
            logger.debug(s"$this\n\t.logTimeTrial($pendingTimeTrial) =>\n\t\t[ lag = $l, Emin(ts) = $min_E_ts ] // ts < E(ts) | Lq = 0")
          } else if (ts > max_E_ts) {  // ts is later than Lq = 1 predicts
            state.deltaT(ts - E)
            logger.debug(s"$this\n\t.logTimeTrial($pendingTimeTrial) =>\n\t\t[ lag = $l, Emax(ts) = $max_E_ts ] // ts > E(ts) | Lq = 1")
          } else if (l < state.L.mean) {  // |Lq| considers trials with low lag and ignores trials with high lag.

            val normalizedPredictionError: Double = predictionError / l.toDouble
            val lq: Double = state.E_NormalizedLq.mean + normalizedPredictionError

            if (l <= state.L.min) {
              // ΔT = ts - ( S + Lq )
              state.deltaT(ts - S + (lq * l).toLong)
            }

            state.E_NormalizedLq(lq)
            logger.debug(s"$this\n\t.logTimeTrial($pendingTimeTrial) =>\n\t\t[ lag = $l, |lq| = $lq, E(ts) = $E_ts, E(ts) - ts = $predictionError, |E(ts) - ts| = $normalizedPredictionError ]")
          } else {
            logger.debug(s"$this\n\t.logTimeTrial($pendingTimeTrial) // Ignoring High Lag trial: $l")
          }
          Math.min((1.0/Math.abs(predictionError / state.L.mean) * state.E_NormalizedLq.sampleSize*1000.0).toLong, maxWait)  // schedule next TimeTrial sooner if |Lq| lacks statistical support
        }
        state.L(l)
        state.reportSuccess()
        waitTime
      }
    }
  }

  import java.util.{Timer, TimerTask}
  val timeTrialScheduler:Timer = new Timer(s"Remote $this timeout monitor.")
  private def scheduleNextTrial(delay:Long, inBurstsOf:Int = 3):Unit = {
    if (this.live) {
      val thisRemoteClock:RemoteClock = this
      logger.debug(s"$this\n\t.scheduleNextTrial($delay MS /* waits ${delay / 1000} Seconds */)")
      timeTrialScheduler.schedule(
        new TimerTask() {
          override def run(): Unit = if (thisRemoteClock.live) {
            Future.traverse(
              // run inBurstsOf time trials
              Seq.tabulate[PendingTimeTrial](inBurstsOf)(_ => timeServerConnection.timeTrial())
            ) {
              ptt =>
                (for {timeTrial <- ptt.promisedTimeTrial.future} yield {
                  val end: Long = System.currentTimeMillis()
                  logTimeTrial(ptt, end)
                }).recover { case t: Throwable =>
                  val failureCount = state.reportFailure()
                  logger.error(s"$ptt failed!  $failureCount ${
                    if (failureCount > 1) "failures" else "failure"
                  } so far.  Error:\n\t$t")
                  maxWait // if a trial fails, wait the maximum amount of time.
                }
            } onComplete {
              case Success(waitTimes: Seq[Long]) => scheduleNextTrial(waitTimes.max)
              case Failure(t) => logger.error(t.toString)
            }
          } else logger.error(s"Can't schedule TimeTrial because $thisRemoteClock already shut down.")
        },
        delay
      )
    }
  }

  @JSExport override def toString:String = s"${
    if (!isReady) "Initializing: " else ""
  }RemoteClock($timeServerConnection, $maxWait)"

  scheduleNextTrial(0L)
}


case class RemoteClockState(remoteClock: RemoteClock) extends LazyLogging {
  def successCount:Long = this.successTrials.get()

  def reportSuccess():Long = this.successTrials.incrementAndGet()
  def reportFailure():Long = this.failedTrials.incrementAndGet()

  /*
 * Lq = latency of client requests to the server.
 * Ls = latency of server responses to the client.
 * L = Lag = Lq + Ls
 *
 * E_NormalizedLq = |Lq| = normalized Lq = Lq / L
 */

  val L: Poisson = new Poisson
  var E_NormalizedLq: Gaussian = new Gaussian // Estimates Lag(RequestTime) / Lag

  /* ts = timeTrial.serverTimeStamp
 * DT = ΔT = ts - ( S + Lq ) */
  private val DT: AtomicLong = new AtomicLong(0L)
  private val successTrials: AtomicLong = new AtomicLong(0L)
  private val failedTrials: AtomicLong = new AtomicLong(0L)

  private def trialCount: Long = successTrials.get + failedTrials.get

  def deltaT:Long = if (remoteClock.isReady) DT.get() else throw RemoteClockNotReady(remoteClock)

  def deltaT(dT:Long):Unit = synchronized {
    // only set ΔT if it changes.
    if (dT != deltaT) {
      DT.set(dT)
      E_NormalizedLq = new Gaussian
      E_NormalizedLq(0.5) // reset request lag model: Lq
    }
    logger.debug(s"Updated ΔT $this")
  }

  override def toString: String = s"$remoteClock.state = {\n\tsuccessRate = $successTrials/$trialCount,\n\tE(Δt) = $DT${
    if (trialCount > 2) s",\n\tlagStats = {\n\t\t|Lq| = $E_NormalizedLq,\n\t\tL = $L\n\t}" else ""
  }\n}"
}

@JSExportAll case class FailedTimeServerConnectionSynchronization(timeServerConnection: TimeServerConnection) extends Exception(s"Failed to Synchronize over TimeServerConnection: $timeServerConnection")

@JSExportAll case class RemoteClockNotReady(remote: RemoteClock) extends Exception(s"Remote Connection Not Ready: $remote")