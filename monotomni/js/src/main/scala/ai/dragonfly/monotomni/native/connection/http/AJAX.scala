package ai.dragonfly.monotomni.native.connection.http

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeoutException
import java.net.URI

import ai.dragonfly.monotomni._
import TimeTrial.Formats
import ai.dragonfly.monotomni.connection.TimeServerConnectionFactory
import ai.dragonfly.monotomni.connection.http.TimeServerConnectionHTTP

import org.scalajs.dom
import org.scalajs.dom.{Event, XMLHttpRequest, ext}

import scala.concurrent.Promise
import scala.scalajs.js.typedarray.ArrayBuffer


/**
 * The AJAX TimeServerConnection runs TimeTrials with the browser's native implementation of XMLHttpRequest.
 * Unless @uri points to a time server running on the same domain name, or one allowed by CORS, as the current web page,
 * AJAX will violate browser cross-origin security policies.
 * More about CORS: https://developer.mozilla.org/en-US/docs/Glossary/CORS
 * For in browser access to a cross-origin time server without CORS, see [[ai.dragonfly.monotomni.native.connection.http.JSONP]].
 * @return
 */

object AJAX extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 3000
  override val defaultFormat: Formats.Format = Formats.BINARY
  override val supportedFormats:Seq[Formats.Format] = Seq(Formats.BINARY, Formats.STRING, Formats.JSON, Formats.XML)
}

case class AJAX(override val uri:URI, override val defaultFormat:Formats.Format, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  new XMLHttpRequest()  // throw exception if run outside of browser environment.

  override def supportedFormats:Seq[Formats.Format] = AJAX.supportedFormats

  override def timeTrial(format: Formats.Format = defaultFormat, timeoutMilliseconds:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    val xhr = new XMLHttpRequest()
    if (format == Formats.BINARY) xhr.responseType = "arraybuffer"
    xhr.timeout = timeoutMilliseconds

    xhr.onload = (e:Event) => {
      // When all goes to plan:
      if (xhr.status == 200) {
        promisedTimeTrial.success(
          format match {
            case Formats.BINARY =>
              TimeTrial.fromArrayBuffer(xhr.response.asInstanceOf[ArrayBuffer])
            case _ =>
              TimeTrial.fromInputStream(
                format,
                new ByteArrayInputStream(
                  xhr.response.asInstanceOf[String].getBytes(StandardCharsets.UTF_8)
                )
              )
          }
        )
      } else {
        promisedTimeTrial.failure(ext.AjaxException(xhr))
      }
    }

    // What more could possibly go wrong?
    xhr.ontimeout = (e: dom.Event) => {
      println(e)
      promisedTimeTrial.failure( new TimeoutException() )
    }

    xhr.open("GET", urlTxt )

    xhr.send()

    PendingTimeTrial(promisedTimeTrial, timeoutMilliseconds)
  }
}
