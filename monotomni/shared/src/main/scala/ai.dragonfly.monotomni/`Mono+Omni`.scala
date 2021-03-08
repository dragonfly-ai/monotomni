package ai.dragonfly.monotomni

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import java.util.concurrent.atomic.AtomicLong

import ai.dragonfly.monotomni.connection.TimeServerConnection.TimeServerConnection

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
  private final def count:Long = 0x0000000000000fffL & counter.getAndIncrement()  // mask off all but least significant 24 bits.
  @inline def apply(ts:Long = System.currentTimeMillis()):MOI = (((ts - dawnOfTime) >>> 8) << 32) | (count << 8) | host
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

class Remote(private val timeServerConnection: TimeServerConnection) {

  def apply(moi:MOI = Mono+Omni()):Future[AMI] = for { dT <- `E(Δt)` } yield moi + (dT << 32)

  private val minLag:AtomicLong = new AtomicLong(Long.MaxValue)
  private val estimatedTimeDelta:AtomicLong = new AtomicLong(Long.MaxValue)
  private val completedTrials:AtomicLong = new AtomicLong(0L)
  private val failedTrials:AtomicLong = new AtomicLong(0L)

  private lazy val completedTrialPromise:Promise[() => Long] = Promise[() => Long]()

  def `E(Δt)`:Future[Long] = for {
    ready <- completedTrialPromise.future
  } yield ready()

  private lazy val completeTimeTrial:(PendingTimeTrial, TimeTrial) => Long = {
    completedTrialPromise.success(() => estimatedTimeDelta.get())
    (pendingTimeTrial:PendingTimeTrial, timeTrial:TimeTrial) => synchronized {
      val end: Long = System.currentTimeMillis()
      val lag = end - pendingTimeTrial.start
      println(s"CompletedTimeTrial with: $lag MS of lag.")
      if (lag < minLag.get) {
        minLag.set(lag)
        estimatedTimeDelta.set((timeTrial.serverTimeStamp + (lag / 2)) - pendingTimeTrial.start)
      }
      println(this)
      completedTrials.incrementAndGet()
    }
  }

  import java.util.{Timer, TimerTask}
  val timeTrialScheduler:Timer = new Timer(s"Remote $this timeout monitor.")
  private val lastDelay:AtomicLong = new AtomicLong(0L)
  private def scheduleNextTrial():Unit = timeTrialScheduler.schedule(
    new TimerTask() {
      override def run():Unit = {
        val pendingTimeTrial:PendingTimeTrial = timeServerConnection.timeTrial()
        pendingTimeTrial.timeTrialPromise.future onComplete {
          case Success(timeTrial:TimeTrial) => completeTimeTrial(pendingTimeTrial, timeTrial)
          case Failure(_) => failedTrials.incrementAndGet()
        }
        scheduleNextTrial()
      }
    },
    lastDelay.addAndGet( lastDelay.get + 5L)
  )

  override def toString:String = if (completedTrials.get() > 0) {
    s"Server Time Estimates : { estimatedTimeDelta : $estimatedTimeDelta, minLag : $minLag, completedTrials : $completedTrials }"
  } else s"Initializing TimeServerConnection: $timeServerConnection"

  scheduleNextTrial()
}

case class FailedToSynchronizeWithTimeServerConnection(timeServerConnection: TimeServerConnection) extends Exception(s"Failed to Synchronize over TimeServerConnection: $timeServerConnection")