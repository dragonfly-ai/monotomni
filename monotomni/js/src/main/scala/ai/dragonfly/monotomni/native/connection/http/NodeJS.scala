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
import scala.scalajs.js.typedarray.Uint8Array


object NodeJS extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 3000
  override val defaultFormat: Formats.Format = Formats.BINARY
  override val supportedFormats: Seq[Format] = Seq(Formats.BINARY, Formats.STRING, Formats.JSON, Formats.XML)
}

case class NodeJS(override val uri:URI, override val defaultFormat:Formats.Format, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  override def supportedFormats:Seq[Formats.Format] = NodeJS.supportedFormats

  val http: js.Dynamic = g.require("http")
  val https: js.Dynamic = g.require("https")
  val Buffer: js.Dynamic = g.require("buffer").Buffer

  trait Response extends js.Object {
    def on(s:String, callback: js.Function):Unit
    def setEncoding(s:String):Unit
  }

  class HttpRequestOptions(timeout:Int) extends js.Object

  /**
   * Execute a TimeTrial
   * @param format The format of the server response message.  Configurable for custom time servers.
   * @return PendingTimeTrial
   */
  override def timeTrial(format:Formats.Format = defaultFormat, timeoutMilliseconds:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format/"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    // http or https?
    (uri.getScheme match {
      case "http" => http
      case "https" => https
    }).get(urlTxt, new HttpRequestOptions(timeout = timeoutMilliseconds), (res:Response) => {
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
            println(s"Error on $urlTxt")
            println(jse.getMessage())
            promisedTimeTrial.failure(jse)
          case t: Throwable =>
            println(t.printStackTrace())
            promisedTimeTrial.failure(t)
        }
      })

      //res.on("end", () => {})
    })

    PendingTimeTrial(promisedTimeTrial, timeoutMilliseconds)
  }
}
