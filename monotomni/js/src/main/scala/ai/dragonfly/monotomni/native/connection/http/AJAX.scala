package ai.dragonfly.monotomni.native.connection.http

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.net.URI

import ai.dragonfly.monotomni._
import TimeTrial.Formats
import ai.dragonfly.monotomni.connection.TimeServerConnectionFactory
import ai.dragonfly.monotomni.connection.http.TimeServerConnectionHTTP
import ai.dragonfly.monotomni.native.connection.http.AJAX.defaultFormat
import org.scalajs.dom
import org.scalajs.dom.{Event, XMLHttpRequest, ext}

import scala.collection.immutable.HashSet
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
}

case class AJAX(override val uri:URI, override val format:Formats.Format, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  new XMLHttpRequest()  // throw exception if run outside of browser environment.

  override def timeTrial(timeoutMS:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    val xhr = new XMLHttpRequest()
    if (format == Formats.BINARY) xhr.responseType = "arraybuffer"
    xhr.timeout = timeoutMS

    xhr.onload = (e:Event) => {
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

    xhr.open("GET", urlTxt )

    xhr.send()

    PendingTimeTrial(promisedTimeTrial, timeoutMS)
  }
}
