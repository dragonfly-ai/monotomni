package ai.dragonfly.monotomni.native.connection.http

import java.io.ByteArrayInputStream
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.TimeoutException

import ai.dragonfly.monotomni._
import TimeTrial.{TimeTrialFormat => TTF}
import TTF.TimeTrialFormat
import ai.dragonfly.monotomni.connection.http.TimeServerConnectionHTTP
import org.scalajs.dom
import org.scalajs.dom.{Event, XMLHttpRequest, ext}

import scala.concurrent.Promise
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}


/**
 * The AJAX TimeServerConnection runs TimeTrials with the browser's native implementation of XMLHttpRequest.
 * Unless @uri points to a time server running on the same domain name, or one allowed by CORS, as the current web page,
 * AJAX will violate browser cross-origin security policies.
 * More about CORS: https://developer.mozilla.org/en-US/docs/Glossary/CORS
 * For in browser access to a cross-origin time server without CORS, see [[ai.dragonfly.monotomni.native.connection.http.JSONP]].
 * @return
 */

case class AJAX(override val uri:URI, override val defaultFormat:TimeTrialFormat = TTF.BINARY, override val defaultTimeout:Int = 3000) extends TimeServerConnectionHTTP {

  new XMLHttpRequest()

  override def supportedFormats:Seq[TimeTrialFormat] = Seq(TTF.BINARY, TTF.STRING, TTF.JSON, TTF.XML)

  override def timeTrial(format: TimeTrialFormat = defaultFormat, timeoutMilliseconds:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    val xhr = new XMLHttpRequest()
    xhr.responseType = "arraybuffer"
    xhr.timeout = timeoutMilliseconds

    xhr.onload = (e:Event) => {
      // When all goes to plan:
      if (xhr.status == 200) {
        val arrayBuffer:ArrayBuffer = xhr.response.asInstanceOf[ArrayBuffer]
        val wrapped:ByteBuffer = TypedArrayBuffer.wrap(arrayBuffer)
        val bytes:Array[Byte] = new Array[Byte](TimeTrial.BYTES)
        wrapped.get(bytes, 0, TimeTrial.BYTES)
        val inputStream = new ByteArrayInputStream(bytes)
        val tt:TimeTrial = TimeTrial.fromInputStream( format, inputStream )
        promisedTimeTrial.success( tt ) //TimeTrial.fromInputStream( format, inputStream ) )
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
