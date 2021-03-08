package ai.dragonfly.monotomni.native


import scala.scalajs.js

object HostName {

  /**
   * JavaScript environments can configure hostName by declaring var HOST_NAME = "Some String" in global scope before loading Mono+Omni
   */

  lazy val hostName: String = try {
    js.Dynamic.global.HOST_NAME.toString
  } catch {
    case _: scala.scalajs.js.JavaScriptException => s"${(Math.random()*Int.MaxValue).toInt}"
  }

}