package ai.dragonfly.monotomni.native

import ai.dragonfly.monotomni._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.language.implicitConversions

trait RemoteClock extends Clock {
  def ready(callback: () => Unit):Unit
  def ami(moi:MOI = Mono+Omni()):AMI
  @JSExport("ami") def amiJS(moiJS:js.BigInt):js.BigInt = orDefault[js.BigInt](moiJS, ami())
  @JSExport("ready") def readyJS(callback: js.Function0[Unit]):Unit = ready(callback)
  @JSExport("now") def nowJS():js.BigInt = now()
}
