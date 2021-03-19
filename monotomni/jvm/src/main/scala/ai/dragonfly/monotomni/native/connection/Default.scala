package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnection
import ai.dragonfly.monotomni.TimeTrial.Formats.Format

object Default {
  def apply(uri:URI, format:Format = null, timeout:Int = -1):TimeServerConnection = {
    http.URL(
      uri,
      if (format == null) http.URL.defaultFormat else format,
      if (timeout < 1) http.URL.defaultTimeout else timeout
    )
  }
}
