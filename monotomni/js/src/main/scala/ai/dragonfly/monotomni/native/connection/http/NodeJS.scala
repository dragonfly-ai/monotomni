package ai.dragonfly.monotomni.native.connection.http

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnectionFactory
import ai.dragonfly.monotomni.connection.http.TimeServerConnectionHTTP
import ai.dragonfly.monotomni.{PendingTimeTrial, TimeTrial}
import TimeTrial.Formats
import ai.dragonfly.monotomni.TimeTrial.Formats.Format

import scala.concurrent.Promise
import scala.scalajs.js
import js.Dynamic.{global => g}
import js.DynamicImplicits._
import scala.collection.immutable.HashSet
import scala.scalajs.js.typedarray.Uint8Array


object NodeJS extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 3000
  override val defaultFormat: Formats.Format = Formats.BINARY
}

case class NodeJS(override val uri:URI, override val format:Formats.Format, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {
  // http or https?
  val scheme: js.Dynamic = uri.getScheme match {
    case "http" => g.require("http")
    case "https" => g.require("https")
  }

  val Buffer: js.Dynamic = g.require("buffer").Buffer

  trait Response extends js.Object {
    def on(s:String, callback: js.Function):Unit
    def setEncoding(s:String):Unit
  }

  class HttpRequestOptions(timeout:Int) extends js.Object

  /**
   * Executes a TimeTrial sequence to estimate ServerTime.
   * @param timeoutMS optional timeout parameter to use instead of [[ai.dragonfly.monotomni.native.connection.http.AJAX.defaultTimeout]]
   * @return an instance of [[ai.dragonfly.monotomni.PendingTimeTrial]]
   */
  override def timeTrial(timeoutMS:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format/"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    scheme.get(urlTxt, new HttpRequestOptions(timeout = timeoutMS), (res:Response) => {
      res.setEncoding("binary")

      res.on("data", (chunk: js.Any) => { // all valid responses fit in only one chunk.
        try {
          promisedTimeTrial.success(
            TimeTrial.fromUint8Array(
              format,
              Buffer.from(chunk, "binary").asInstanceOf[Uint8Array]
            )
          )
        } catch {
          case jse: scala.scalajs.js.JavaScriptException =>
            logger.error(s"Error on $urlTxt")
            logger.error(jse.getMessage())
            promisedTimeTrial.tryFailure(jse)
          case t: Throwable =>
            logger.error(t.getMessage)
            logger.error(t.getStackTrace.mkString("Array(", ", ", ")"))
            promisedTimeTrial.tryFailure(t)
        }
      })

    })

    PendingTimeTrial(promisedTimeTrial, timeoutMS)
  }
}
