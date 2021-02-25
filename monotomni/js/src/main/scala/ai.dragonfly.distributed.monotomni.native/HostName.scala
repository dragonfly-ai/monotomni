package ai.dragonfly.distributed.monotomni.native

import org.scalajs.dom.raw.Navigator

import scala.scalajs.js

object HostName {
  lazy val hostName: String = js.Dynamic.global.selectDynamic("navigator") match { case n: Navigator => n.userAgent case _ => "client" }
}