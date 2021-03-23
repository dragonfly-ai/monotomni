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
import scala.util.Failure

/**
 * The JSONP TimeServerConnectionFactory runs only in Web Browsers and supports only one TimeTrial format:
 * [[ai.dragonfly.monotomni.TimeTrial.Formats.JSONP]]
 */

object JSONP extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 5000
  override val defaultFormat: Formats.Format = Formats.JSONP
  override val supportedFormats: HashSet[Format] = HashSet[Format](Formats.JSONP)

  private lazy val pendingTimeTrials: mutable.Map[MOI,PendingTimeTrial] = mutable.Map[MOI,PendingTimeTrial]()
  private def apply(pendingTimeTrial: PendingTimeTrial): PendingTimeTrial = {
    pendingTimeTrials.getOrElseUpdate(pendingTimeTrial.moi, pendingTimeTrial)
    pendingTimeTrial.promisedTimeTrial.future onComplete {
      case Failure(toe:TimeoutException) => timeout(pendingTimeTrial)
    }
    pendingTimeTrial
  }

  /**
   * Processes TimeServer JSONP TimeTrial responses initiated from native JavaScript
   * @param pendingTimeTrialId the MOI of the PendingTimeTrial associated with this TimeTrial
   * @param serverTimeStamp a String Representation of this TimeTrial.
   * @return a TimeTrial
   */
  def logTimeTrial(pendingTimeTrialId:String, serverTimeStamp: String): TimeTrial = {
    val pttId:MOI = java.lang.Long.parseLong( pendingTimeTrialId )
    val tt:TimeTrial = TimeTrial( java.lang.Long.parseLong( serverTimeStamp ) )

    pendingTimeTrials.getOrElse(
      pttId,
      throw UnknownPendingTimeTrialId(pttId)
    ).promisedTimeTrial.success(tt)

    tt
  }

  private def timeout(timedOutTimeTrial:PendingTimeTrial):Unit = pendingTimeTrials -= timedOutTimeTrial.moi

  /**
   * Factory Method for constructing the JSONP TimeServerConncetion
   * @param uri an HTTP or HTTPS TimeServer URL
   * @param format the TimeTrial format to request from the TimeServer.
   * @param timeout how long to wait, in milliseconds, for a TimeTrial response before giving up.
   * @return an instance of JSONP TimeServerConnection
   */
  override def apply(uri: URI, format: Format, timeout: Int): TimeServerConnection = JSONP(uri, timeout)
}

/**
 * The JSONP TimeServerConnection runs TimeTrials by appending a JavaScript file from the TimeServer
 * into the Document Object Model.
 *
 * This method is less efficient than AJAX, but can function across domain names.
 *
 * JSONP uses its own format for TimeTrials so its defaultFormat can not be configured.
 *
 * @param uri an HTTP or HTTPS TimeServer URL
 * @param defaultTimeout how long to wait, in milliseconds, for a TimeTrial response before giving up.
 */
case class JSONP (override val uri:URI, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  /**
   * The only format JSONP TimeServerConnections support takes the following form:
   * monotomni.connection.http.JSONP.logTimeTrial('6936979113930978754817','1615141312110');
   */
  override val format: Format = Formats.JSONP

  /**
   * Executes a TimeTrial sequence to estimate ServerTime.
   * @param timeoutMS optional timeout parameter to use instead of [[ai.dragonfly.monotomni.native.connection.http.AJAX.defaultTimeout]]
   * @return an instance of [[ai.dragonfly.monotomni.PendingTimeTrial]]
   */
  override def timeTrial(timeoutMS:Int = defaultTimeout): PendingTimeTrial = {
    try {

      val document = org.scalajs.dom.document
      val scriptTag = document.createElement("script")
      scriptTag.setAttribute("type", "text/javascript")
      val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()
      val pttId:MOI = Mono+Omni()

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
