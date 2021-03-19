package ai.dragonfly.monotomni.connection

import ai.dragonfly.monotomni.PendingTimeTrial
import ai.dragonfly.monotomni.TimeTrial.Formats.Format

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait TimeServerConnection {
  val path: String
  val defaultFormat: Format
  val defaultTimeout: Int

  def supportedFormats: Seq[Format]

  def timeTrial(): PendingTimeTrial = timeTrial(defaultFormat, defaultTimeout)

  def timeTrial(format: Format, timeoutMilliseconds: Int): PendingTimeTrial

  override def toString: String = s"${this.getClass.getName}($path, $defaultFormat, $defaultTimeout)"
}
