package ai.dragonfly.distributed.monotomni.native

import ai.dragonfly.distributed.monotomni
import java.net.URI

import ai.dragonfly.distributed.monotomni.TimeTrial

import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.Success
import scala.concurrent._
import scala.scalajs.js.{JSON, ReferenceError}

trait TimeServerHTTP {

}

object SynchronizeTimeServerHTTP {
  @JSExportTopLevel("SynchronizeTimeServerHTTP")
  def apply(url: org.scalajs.dom.experimental.URL): SynchronizeTimeServerHTTP = SynchronizeTimeServerHTTP(new URI(url.toString))
}


case class SynchronizeTimeServerHTTP(override val uri:URI) extends monotomni.SynchronizeTimeServer {

  override def runTrials(): Unit = {
    try {
      val document = org.scalajs.dom.document
      for (trial: Int <- 0 until trialCount) {
        val scriptTag = document.createElement("script")
        scriptTag.setAttribute("type", "text/javascript")
        val urlTxt = s"$uri/JSONP/$timeServerID/$trial"
        scriptTag.setAttribute("src", urlTxt)
        document.getElementsByTagName("head")(0).appendChild(scriptTag)
        trials(trial) = System.currentTimeMillis()
      }
    } catch {
      case e:scala.scalajs.js.JavaScriptException =>
        println("Node.js?")
        import scala.scalajs.js
        import js.Dynamic.{global => g}
        import js.DynamicImplicits._

        val http = g.require("http")
        trait Response extends js.Object {
          def on(s:String, callback: js.Function):Unit
        }

        for (trial: Int <- 0 until trialCount) {
          val urlTxt = s"$uri/JSON/$timeServerID/$trial"
          http.get(urlTxt,(res:Response) => {
            val sb:StringBuilder = new StringBuilder()

            res.on("data", (chunk: js.Any) => sb.append( chunk ))

            res.on("end", () => {
              try {
                logTimeTrial(TimeTrial.JSON(sb.toString()))
              } catch {
                case e: scala.scalajs.js.JavaScriptException =>
                  println(e.getMessage())
              }
            })
          })
        }
    }
  }

  override def complete(timeDelta: Long, minimumEstimatedLatency: Long, completedTrials: Int): Unit = promisedTimeServer.complete(Success(monotomni.TimeServerHTTP(uri,timeDelta,minimumEstimatedLatency,completedTrials)))
}

case class RequiresBrowserEnvironment() extends Exception("Could not find Browser Environment!")