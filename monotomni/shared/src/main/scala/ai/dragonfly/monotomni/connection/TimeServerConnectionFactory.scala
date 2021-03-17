package ai.dragonfly.monotomni.connection

import java.net.URI

import ai.dragonfly.monotomni.TimeTrial.TimeTrialFormat.TimeTrialFormat

trait TimeServerConnectionFactory {
  val defaultTimeout:Int
  val defaultFormat:TimeTrialFormat

  def apply(uri:URI):TimeServerConnection = apply(uri, defaultFormat, defaultTimeout)
  def apply(uri:URI, format:TimeTrialFormat):TimeServerConnection = apply(uri, format, defaultTimeout)
  def apply(uri:URI, timeout:Int):TimeServerConnection = apply(uri, defaultFormat, timeout)
  def apply(uri:URI, format:TimeTrialFormat, timeout:Int):TimeServerConnection
}
