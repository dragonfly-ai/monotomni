package ai.dragonfly.monotomni.native

import scala.scalajs.js

object HostName {

  /**
   * JavaScript environments can configure hostName by declaring var HOST_NAME = "Some String" in global scope before loading Mono+Omni
   */

  lazy val hostName: String = try {
    val hn = js.Dynamic.global.HOST_NAME.toString
    println(s"Found HOST_NAME: $hn")
    hn
  } catch {
    case _: scala.scalajs.js.JavaScriptException =>
      println("Cannot find HOST_NAME global variable.")
      (Math.random()*Int.MaxValue).toInt.toString
  }

}