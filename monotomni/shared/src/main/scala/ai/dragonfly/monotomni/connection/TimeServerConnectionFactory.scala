package ai.dragonfly.monotomni.connection

import java.net.URI

import ai.dragonfly.monotomni.TimeTrial.Formats

import scala.collection.immutable.HashSet
import scala.scalajs.js.annotation.JSExport

trait TimeServerConnectionFactory {
  @JSExport val defaultTimeout:Int
  @JSExport val defaultFormat:Formats.Format
  @JSExport val supportedFormats:HashSet[Formats.Format] = HashSet.from[Formats.Format](Formats.all)

  @JSExport def supportsFormat(preferredFormat:Formats.Format):Boolean = supportedFormats.contains(preferredFormat)

  def apply(uri:URI, format:Formats.Format = defaultFormat, timeout:Int = defaultTimeout):TimeServerConnection
}