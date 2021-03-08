package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnection.TimeServerConnection

object DefaultConnection {
  def apply(uri: URI): TimeServerConnection = http.URL(uri)
}
