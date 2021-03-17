package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnectionFactory

object Default {
  def apply(uri: URI): TimeServerConnectionFactory = http.URL
}
