package ai.dragonfly.distributed.monotomni.native

import ai.dragonfly.distributed.monotomni

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.util.Success
import scala.language.postfixOps

case class Generator(override val futureTimeServer: Future[monotomni.TimeServer]) extends monotomni.Generator {
  /* ready method synchs client and server time via JSONP, but only needed in the browser. */
  @JSExport
  def ready(f: js.Function0[Unit]): Unit = ready(() => {f()})
}
