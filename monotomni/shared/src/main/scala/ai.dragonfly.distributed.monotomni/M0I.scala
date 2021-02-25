package ai.dragonfly.distributed.monotomni

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import java.util.concurrent.atomic.AtomicLong

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import ai.dragonfly.distributed.monotomni.native

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future, Promise, duration}
import scala.util.Success

sealed trait monotomni {
  final val dawnOfTime = 1615141312110L // timestamp of the birth of this library
  final val hashMask: Int = 0x000000ff & ((native.HostName.hostName.hashCode() << 24) >> 24)
}

object Mono {
  def +(o: Any):monotomni = Omni
}

object Omni extends monotomni {


}

object Generator {
  @JSExportTopLevel("Generator")
  def apply(futureTimeServer:Future[TimeServer]): Generator = native.Generator(futureTimeServer)

}

trait Generator {
  val futureTimeServer:Future[TimeServer] // Defined Natively

  def ready(f: () => Unit): Unit = futureTimeServer onComplete {
    case Success(timeServer:TimeServer) => f()
    case _ => println("Whatever?")
  }

  private final val counter: AtomicLong = new AtomicLong

  def next(): Future[Moi] = {
    val now:Long = System.currentTimeMillis()
    val count = counter.incrementAndGet()
    if (count >= 0x10000) counter.set(0)
    for {
      timeServer <- futureTimeServer
    } yield ((((now + timeServer.timeDelta) - (Mono+Omni).dawnOfTime) >>> 8) << 32) | (count << 8) | (Mono+Omni).hashMask
  }

  @JSExport
  def prettyPrint(moi: Moi): String = M0I(moi).toString

  @JSExport
  override def toString: String = s"Generator Status: { dawnOfTime: ${Mono+Omni.dawnOfTime}, hashMask: ${Mono+Omni.hashMask}, counter: $counter, TimeServer: $futureTimeServer }"

}

