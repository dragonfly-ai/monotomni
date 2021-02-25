package ai.dragonfly.distributed.monotomni.native

import java.net.URI

import ai.dragonfly.distributed.monotomni
import monotomni.TimeTrial
import monotomni.byteArray2String

import scala.language.implicitConversions
import scala.util.Success

trait TimeServerHTTP {

}

case class SynchronizeTimeServerHTTP(uri:URI) extends monotomni.SynchronizeTimeServer {

  override def runTrials(): Unit = for (trial: Int <- 0 until trialCount) {
    println(s"runTrials($uri)")
    try {
      trials(trial) = System.currentTimeMillis()
      val bytes = new Array[Byte](TimeTrial.BYTES)
      val urlTxt = s"$uri/BINARY/$timeServerID/$trial"
      println(s"runTrials($uri) ... $urlTxt")
      new java.net.URL(urlTxt).openConnection().getInputStream.read(bytes)
      val timeTrial = TimeTrial.BINARY(bytes)
      println(s"bytes: ${byteArray2String(bytes)}")
      println(s"timeTrial($trial):$timeTrial")
      logTimeTrial(timeTrial)
    } catch {
      case t: Throwable => println(s"Encountered error on trial $trial"); t.printStackTrace()
    }
  }

  override def complete(timeDelta: Long, minimumEstimatedLatency: Long, completedTrials: Int): Unit = {
    val timeServer:monotomni.TimeServer = monotomni.TimeServerHTTP(uri,timeDelta,minimumEstimatedLatency,completedTrials)
    val result = Success(timeServer)
    promisedTimeServer.complete(result)
  }
}
