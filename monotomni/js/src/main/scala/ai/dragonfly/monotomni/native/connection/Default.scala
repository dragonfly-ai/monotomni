package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni
import ai.dragonfly.monotomni.native._
import monotomni.TimeTrial.Formats.Format
import monotomni.connection.{TimeServerConnection, TimeServerConnectionFactory}
import org.scalajs.dom.experimental.URL
import org.scalajs.dom.window
import slogging.LazyLogging

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExport

/**
 * Tries to discover the 'best' [[monotomni.connection.TimeServerConnectionFactory]] for a given JavaScript environment and TimeServer URI.
 */

object Default extends LazyLogging {

  /**
   * Helper Method to expose the Default.apply(uri:URI, format:Format = null, timeout:Int = -1):[[monotomni.connection.TimeServerConnection]]
   * method to native JavaScript.
   *
   * @param url an HTTP or HTTPS TimeServer URL in native JavaScript URL format.
   * @param format the TimeTrial format to request from the TimeServer.
   * @param timeout how long to wait, in milliseconds, for a TimeTrial response before giving up.
   * @return the best [[TimeServerConnection]]
   */
  @JSExport("apply") def applyJS(url:URL, format:Format, timeout:Double):TimeServerConnection = apply(
    url,
    orDefault[Format](format, null),
    orDefaultNumber(timeout, -1).toInt
  )

  /**
   * Finds the best [[monotomni.connection.TimeServerConnectionFactory]] and invokes it with optional parameters.
   * @param uri an HTTP or HTTPS TimeServer URL
   * @param format the [[TimeTrial]].[[Format]] to request from the TimeServer.
   * @param timeout how long to wait, in milliseconds, for a TimeTrial response before giving up.
   * @return the best [[monotomni.connection.TimeServerConnection]]
   */
  def apply(uri:URI, format:Format = null, timeout:Int = -1):TimeServerConnection = {
    val factory:TimeServerConnectionFactory = apply(uri)
    factory(
      uri,
      if (format == null) factory.defaultFormat else format,
      if (timeout < 100) factory.defaultTimeout else timeout
    )
  }

  /**
   * Finds the best [[monotomni.connection.TimeServerConnectionFactory]] for the given JavaScript environment and TimeServer URI.
   * @param uri an HTTP or HTTPS TimeServer URL
   * @return the best suitable [[monotomni.connection.TimeServerConnectionFactory]]
   */
  def apply(uri:URI):TimeServerConnectionFactory = {
    val timeServerConnection:TimeServerConnectionFactory = try {
      logger.debug(s"AJAX?  ${window.location.hostname} == ${uri.getHost} ?")
      if (window.location.hostname == uri.getHost) http.AJAX
      else {
        logger.debug("Domains don't match!\nJSONP?")
        http.JSONP
      }
    } catch {
      case _: Throwable =>
        logger.debug("No Browser Context Available!\nNode.JS?")
        http.NodeJS
    }
    logger.debug(s"Connecting to:\n\tTimeServer@$uri\n\twith $timeServerConnection")
    timeServerConnection
  }

}
