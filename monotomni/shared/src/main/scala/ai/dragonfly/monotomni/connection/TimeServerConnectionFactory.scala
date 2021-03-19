package ai.dragonfly.monotomni.connection

import java.net.URI

import ai.dragonfly.monotomni.TimeTrial.Formats.Format

import scala.scalajs.js.annotation.JSExport

trait TimeServerConnectionFactory {
  @JSExport val defaultTimeout:Int
  @JSExport val defaultFormat:Format
  @JSExport val supportedFormats: Seq[Format]

  def apply(uri:URI, format:Format = defaultFormat, timeout:Int = defaultTimeout):TimeServerConnection
}

// curried rice?
