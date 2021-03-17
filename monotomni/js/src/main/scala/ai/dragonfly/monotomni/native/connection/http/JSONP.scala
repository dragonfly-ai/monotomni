package ai.dragonfly.monotomni.native.connection.http

import java.net.URI
import java.util.{Timer, TimerTask}
import java.util.concurrent.TimeoutException

import ai.dragonfly.monotomni._
import TimeTrial.{TimeTrialFormat => TTF}
import TTF.TimeTrialFormat
import ai.dragonfly.monotomni.connection.TimeServerConnectionFactory
import ai.dragonfly.monotomni.connection.http.TimeServerConnectionHTTP

import scala.collection.mutable
import scala.concurrent.Promise
import scala.scalajs.js.annotation.JSExportTopLevel

/**
 * For Web Browsers only!
 * JSONP coordinates browser based time clients with TimeTrials executed over JSONP.
 */

object JSONP extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 5000
  override val defaultFormat: TimeTrialFormat = TTF.JSONP

  @JSExportTopLevel("LOG_JSONP_TIME_TRIAL")
  def logTimeTrial(pendingTimeTrialId:String, serverTimeStamp: String): TimeTrial = logTimeTrial(
    java.lang.Long.parseLong( pendingTimeTrialId ),
    TimeTrial( java.lang.Long.parseLong( serverTimeStamp ) )
  )

  def logTimeTrial(pendingTimeTrialId:MOI, tt:TimeTrial): TimeTrial = {
    pendingTimeTrials.getOrElse(
      pendingTimeTrialId, throw UnknownPendingTimeTrialId(pendingTimeTrialId)
    ).timeTrialPromise.success(tt)
    tt
  }

  private lazy val pendingTimeTrials: mutable.Map[MOI,PendingTimeTrial] = mutable.Map[MOI,PendingTimeTrial]()
  def apply(pendingTimeTrial: PendingTimeTrial): PendingTimeTrial = {
    pendingTimeTrials.getOrElseUpdate(pendingTimeTrial.moi, pendingTimeTrial)
  }

  def timeout(timedOutTimeTrial:PendingTimeTrial):Unit = pendingTimeTrials -= timedOutTimeTrial.moi

}

case class JSONP (override val uri:URI, override val defaultFormat:TimeTrialFormat, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  /** TimeTrialFormat.BINARY not supported!
   *
   * @return a Seq of supported TimeTrialFormat flags.
   */
  override def supportedFormats:Seq[TimeTrialFormat] = Seq(TTF.JSONP)

  override def timeTrial(format: TimeTrialFormat = defaultFormat, timeoutMilliseconds:Int = defaultTimeout): PendingTimeTrial = {
    try {

      val document = org.scalajs.dom.document
      val scriptTag = document.createElement("script")
      scriptTag.setAttribute("type", "text/javascript")
      val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()
      val pendingTimeTrial:PendingTimeTrial = PendingTimeTrial(promisedTimeTrial, timeoutMilliseconds)
      JSONP(pendingTimeTrial)
      val urlTxt = s"$uri/JSONP/${pendingTimeTrial.moi}"
      scriptTag.setAttribute("src", urlTxt)
      document.getElementsByTagName("head")(0).appendChild(scriptTag)


      // handle timeout:
      new Timer(s"TimeTrial# ${pendingTimeTrial.moi} timeout monitor.").schedule(
        new TimerTask() {
          override def run():Unit = if (!promisedTimeTrial.future.isCompleted) {
            promisedTimeTrial.failure(new TimeoutException())
          }
        },
        timeoutMilliseconds
      )
      pendingTimeTrial
    } catch {
      case jse:scala.scalajs.js.JavaScriptException =>
        println(jse)
        throw RequiresBrowserEnvironment()
      case e: Throwable => throw RequiresBrowserEnvironment()
    }
  }

}

// JSONP Exceptions:
case class RequiresBrowserEnvironment() extends Exception("Could not find Browser Environment!")

case class UnknownPendingTimeTrialId(pendingTimeTrialId: MOI) extends Exception(s"Unknown Pending TimeTrial ID: $pendingTimeTrialId.  Could it have timed out?")
