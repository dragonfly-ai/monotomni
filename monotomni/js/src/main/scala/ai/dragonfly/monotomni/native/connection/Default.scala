package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni
import monotomni.TimeTrial.Formats.Format
import monotomni.connection.{TimeServerConnection, TimeServerConnectionFactory}
import org.scalajs.dom.window
import slogging.LazyLogging

import scala.language.implicitConversions

object Default extends LazyLogging {

  def apply(uri:URI, format:Format = null, timeout:Int = -1):TimeServerConnection = {
    val factory:TimeServerConnectionFactory = apply(uri)
    factory(
      uri,
      if (format == null) factory.defaultFormat else format,
      if (timeout < 1) factory.defaultTimeout else timeout
    )
  }

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
