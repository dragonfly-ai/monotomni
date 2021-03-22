package ai.dragonfly.monotomni.native.connection.http

import java.net.URI
import java.util.{Timer, TimerTask}
import java.util.concurrent.TimeoutException

import ai.dragonfly.monotomni._
import TimeTrial.Formats
import ai.dragonfly.monotomni.TimeTrial.Formats.Format
import ai.dragonfly.monotomni.connection.{TimeServerConnection, TimeServerConnectionFactory}
import ai.dragonfly.monotomni.connection.http.TimeServerConnectionHTTP

import scala.collection.immutable.HashSet
import scala.collection.mutable
import scala.concurrent.Promise

/**
 * For Web Browsers only!
 * JSONP coordinates browser based time clients with TimeTrials executed over JSONP.
 */

object JSONP extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 5000
  override val defaultFormat: Formats.Format = Formats.JSONP
  override val supportedFormats: HashSet[Format] = HashSet[Format](Formats.JSONP)

  private lazy val pendingTimeTrials: mutable.Map[MOI,PendingTimeTrial] = mutable.Map[MOI,PendingTimeTrial]()
  def apply(pendingTimeTrial: PendingTimeTrial): PendingTimeTrial = {
    pendingTimeTrials.getOrElseUpdate(pendingTimeTrial.moi, pendingTimeTrial)
  }

  def logTimeTrial(pendingTimeTrialId:String, serverTimeStamp: String): TimeTrial = {
    val pttId:MOI = java.lang.Long.parseLong( pendingTimeTrialId )
    val tt:TimeTrial = TimeTrial( java.lang.Long.parseLong( serverTimeStamp ) )

    pendingTimeTrials.getOrElse(
      pttId,
      throw UnknownPendingTimeTrialId(pttId)
    ).promisedTimeTrial.success(tt)

    tt
  }

  def timeout(timedOutTimeTrial:PendingTimeTrial):Unit = pendingTimeTrials -= timedOutTimeTrial.moi

  /**
   * JSONP clients use their own format.
   */
  override def apply(uri: URI, format: Format, timeout: Int): TimeServerConnection = JSONP(uri, timeout)
}

case class JSONP (override val uri:URI, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  override val format: Format = Formats.JSONP

  override def timeTrial(timeoutMS:Int = defaultTimeout): PendingTimeTrial = {
    try {

      val document = org.scalajs.dom.document
      val scriptTag = document.createElement("script")
      scriptTag.setAttribute("type", "text/javascript")
      val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()
      val pttId:MOI = Mono+Omni()

      // handle timeout:
      new Timer(s"JSONP PendingTimeTrial# $pttId timeout monitor.").schedule(
        new TimerTask() {
          override def run():Unit = if (!promisedTimeTrial.future.isCompleted) {
            promisedTimeTrial.failure(new TimeoutException())
          }
        },
        timeoutMS
      )

      val urlTxt = s"$uri/JSONP/$pttId"
      scriptTag.setAttribute("src", urlTxt)
      document.getElementsByTagName("head")(0).appendChild(scriptTag)

      JSONP(
        PendingTimeTrial(promisedTimeTrial, timeoutMS, Mono+Omni.now(), pttId)
      )
    } catch {
      case jse:scala.scalajs.js.JavaScriptException =>
        logger.error(jse.getMessage())
        throw RequiresBrowserEnvironment()
      case e: Throwable =>
        logger.error(e.getMessage)
        throw RequiresBrowserEnvironment()
    }
  }

}

// JSONP Exceptions:
case class RequiresBrowserEnvironment() extends Exception("Could not find Browser Environment!")

case class UnknownPendingTimeTrialId(pendingTimeTrialId: MOI) extends Exception(s"Unknown Pending TimeTrial ID: $pendingTimeTrialId.  Could it have timed out?")
