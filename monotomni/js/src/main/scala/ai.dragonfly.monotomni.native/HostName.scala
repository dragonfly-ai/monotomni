package ai.dragonfly.monotomni.native

import ai.dragonfly.monotomni

import slogging.LazyLogging

import scala.scalajs.js

/**
 * JavaScript environments can configure hostName by declaring var HOST_NAME = "Some String" in global scope before loading Mono+Omni
 * This variable determines the least significant 8 bits of every [[monotomni.MOI]] and [[monotomni.AMI]] id generated from this host.
 */
object HostName extends LazyLogging {

  lazy val hostName: String = try {
    val hn = js.Dynamic.global.HOST_NAME.toString
    logger.debug(s"Found HOST_NAME: $hn")
    hn
  } catch {
    case _: scala.scalajs.js.JavaScriptException =>
      logger.warn("Cannot find HOST_NAME global variable.")
      (Math.random()*Int.MaxValue).toInt.toString
  }

}