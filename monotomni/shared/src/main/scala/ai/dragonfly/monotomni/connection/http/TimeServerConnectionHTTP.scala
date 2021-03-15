package ai.dragonfly.monotomni.connection.http

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnection

trait TimeServerConnectionHTTP extends TimeServerConnection {
  val uri:URI
  override val path: String = uri.toString
}
