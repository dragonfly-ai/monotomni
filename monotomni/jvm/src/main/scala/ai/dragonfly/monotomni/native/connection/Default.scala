package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnection
import ai.dragonfly.monotomni.TimeTrial.Formats.Format

/**
 * Always returns [[ai.dragonfly.monotomni.native.connection.http.URL]] for JVM environments.
 */
object Default {

  /**
   * Always returns [[ai.dragonfly.monotomni.native.connection.http.URL]] for JVM environments.
   *
   * @param uri an HTTP or HTTPS TimeServer URL
   * @param format the TimeTrial format to request from the TimeServer.
   * @param timeout how long to wait, in milliseconds, for a TimeTrial response before giving up.
   * @return an instance of [[ai.dragonfly.monotomni.native.connection.http.URL]]
   */
  def apply(uri:URI, format:Format = null, timeout:Int = -1):TimeServerConnection = {
    http.URL(
      uri,
      if (format == null) http.URL.defaultFormat else format,
      if (timeout < 100) http.URL.defaultTimeout else timeout
    )
  }
}
