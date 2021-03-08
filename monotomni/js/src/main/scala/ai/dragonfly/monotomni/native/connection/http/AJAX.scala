package ai.dragonfly.monotomni.native.connection.http

import java.io.ByteArrayInputStream
import java.net.URI
import java.util.concurrent.TimeoutException

import ai.dragonfly.monotomni._
import TimeTrial.{TimeTrialFormat => TTF}
import TTF.TimeTrialFormat
import ai.dragonfly.monotomni.connection.TimeServerConnection.TimeServerConnection
import org.scalajs.dom

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

case class AJAX(uri:URI, override val defaultFormat:TimeTrialFormat = TTF.BINARY, override val defaultTimeout:Int = 3000) extends TimeServerConnection {

  new dom.XMLHttpRequest()

  override def supportedFormats:Seq[TimeTrialFormat] = Seq(TTF.BINARY, TTF.STRING, TTF.JSON, TTF.XML)

  override def timeTrial(format: TimeTrialFormat = defaultFormat, timeoutMilliseconds:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format/"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    val xhr = new dom.XMLHttpRequest()
    xhr.responseType = "arraybuffer"
    xhr.timeout = timeoutMilliseconds
    xhr.open("GET", urlTxt )

    xhr.onload = (e: dom.Event) => {
      // When all goes to plan:
      if (xhr.status == 200) {
        val arrayBuffer:ArrayBuffer = xhr.response.asInstanceOf[ArrayBuffer]
        val inputStream = new ByteArrayInputStream( TypedArrayBuffer.wrap(arrayBuffer).array)
        promisedTimeTrial.success( TimeTrial.fromInputStream( format, inputStream ) )
      } else {
        promisedTimeTrial.failure(dom.ext.AjaxException(xhr))
      }
    }

    // What more could possibly go wrong?
    xhr.ontimeout = (e: dom.Event) => promisedTimeTrial.failure( new TimeoutException() )

    xhr.send()

    PendingTimeTrial(promisedTimeTrial)
  }
}
