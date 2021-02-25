package ai.dragonfly.distributed.monotomni

import java.net.URI

import ai.dragonfly.distributed
import scala.language.implicitConversions

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.{Failure, Success}

trait TimeServer {
  val completedTrials: Int
  val timeDelta: Long
  val minimumEstimatedLatency: Long
  def timestamp():Long = System.currentTimeMillis() + timeDelta
}

case class TimeServerHTTP(uri:URI, override val timeDelta: Long, override val minimumEstimatedLatency: Long, override val completedTrials:Int) extends TimeServer {
  @JSExport
  override def toString: String = {
    s"Server Time Estimates : { timeDelta : $timeDelta, minimumEstimatedLatency : $minimumEstimatedLatency, completedTrials : $completedTrials }"
  }
}


object TimeServer {

  def HTTP(uri:URI):Future[TimeServer] = {
    println(uri)
    apply(native.SynchronizeTimeServerHTTP(uri))
  }

  def apply(tss:SynchronizeTimeServer):Future[TimeServer] = {
    synchronizeServer(tss)
    tss.promisedTimeServer.future
  }

  @JSExportTopLevel("LOG_TIME_TRIAL")
  def logTimeTrial(sid:Int,trial:Int,serverTimeStamp:String):Unit = {
    logTimeTrial( TimeTrial(sid, trial, java.lang.Long.parseLong(serverTimeStamp)) )
  }

  def logTimeTrial(tt: TimeTrial):Unit = {
    servers.getOrElse(tt.serverId,throw UnknownServerId(tt.serverId)).logTimeTrial(tt)
  }

  private lazy val servers: mutable.Map[Int,SynchronizeTimeServer] = mutable.Map[Int,SynchronizeTimeServer]()
  private def synchronizeServer(tss:SynchronizeTimeServer): SynchronizeTimeServer = synchronized {
    servers.getOrElseUpdate(getTimeServerID(tss.uri), tss)
  }

  private lazy val serverIDs: mutable.Map[String,Int] = mutable.Map[String,Int]()
  def getTimeServerID(uri:URI): Int = synchronized {
    serverIDs.getOrElseUpdate(uri.toString, serverIDs.size)
  }
}


trait SynchronizeTimeServer {
  val uri:URI
  val promisedTimeServer:Promise[TimeServer] = Promise[TimeServer]()
  val timeServerID:Int = TimeServer.getTimeServerID(uri)

  val futureTimeServer: Future[TimeServer] = promisedTimeServer.future

  futureTimeServer onComplete {
    case Success(l) => // process callback queue
    case Failure(t) => println("An error has occurred: " + t.getMessage)
  }

  val trialCount = 4
  val trials = new Array[Long](trialCount)

  private var minLatency:Long = Long.MaxValue
  private var bestTimeDelta:Long = Long.MaxValue
  private var completedTrials = 0

  def complete(bestTimeDelta: Long, minLatency: Long, trialCount: Int):Unit

  def logTimeTrial(t:TimeTrial): Unit = {
    println(s"Completed $t")
    val b2: Long = System.currentTimeMillis()
    val b1 = trials(t.trial)
    val latency = b2 - b1
    println(s"latency: $latency")
    if (latency < minLatency) {
      minLatency = latency
      bestTimeDelta = t.serverTimeStamp - ((b2 + b1) / 2)
    }
    completedTrials = completedTrials + 1
    if (completedTrials == trialCount) {
      println(s"Completed Trials!  Estimated offset as: $bestTimeDelta")
      complete(bestTimeDelta, minLatency, trialCount)
    }
  }
  def runTrials(): Unit
  runTrials()
}

case class UnknownServerId(serverId: Int) extends Exception(s"Unknown serverId: $serverId")

