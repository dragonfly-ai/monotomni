package ai.dragonfly.monotomni.connection

import ai.dragonfly.monotomni.PendingTimeTrial
import ai.dragonfly.monotomni.TimeTrial.Formats.Format
import slogging.LazyLogging

import scala.collection.immutable.HashSet
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait TimeServerConnection extends LazyLogging {
  val path: String
  val format: Format
  val defaultTimeout: Int

  def timeTrial(): PendingTimeTrial = timeTrial(defaultTimeout)

  def timeTrial(timeoutMS: Int): PendingTimeTrial

  override def toString: String = s"${this.getClass.getSimpleName}($path, $format, $defaultTimeout)"
}
