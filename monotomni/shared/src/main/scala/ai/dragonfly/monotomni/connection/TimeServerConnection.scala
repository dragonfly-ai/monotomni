package ai.dragonfly.monotomni.connection

import ai.dragonfly.monotomni.PendingTimeTrial
import ai.dragonfly.monotomni.TimeTrial.TimeTrialFormat.TimeTrialFormat

trait TimeServerConnection {
  val path: String
  val defaultFormat: TimeTrialFormat
  val defaultTimeout: Int

  def supportedFormats: Seq[TimeTrialFormat]

  def timeTrial(): PendingTimeTrial = timeTrial(defaultFormat, defaultTimeout)

  def timeTrial(format: TimeTrialFormat, timeoutMilliseconds: Int): PendingTimeTrial

  override def toString: String = s"${this.getClass.getName}($path, $defaultFormat, $defaultTimeout)"
}
