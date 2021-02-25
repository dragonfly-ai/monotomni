package ai.dragonfly.distributed.monotomni.server

import akka.http.scaladsl.Http

/**
 * Ways to test this server:
 *
 * From Browser Running on Same Computer simply navigate to: http://localhost:8080/time/JSON/0/0
 *
 * From Linux/Unix Console:
 * {{{$> wget --no-proxy http://localhost:8080/time/JSON/0/0 -qO- }}}
 *
 * System Call From Scala App:
 * {{{
 * import sys.process._
 * import scala.language.postfixOps
 * println("wget --no-proxy http://localhost:8080/time/JSON/0/0 -qO-" !!)
 * }}}
 *
 * Scala App using Java IO:
 * {{{
 * import ai.dragonfly.distributed.monotomni.TimeTrial
 * val bytes = new Array[Byte](TimeTrial.BYTES)
 * new java.net.URL("http://localhost:8080/time/JSON/0/0").openConnection().getInputStream.read(bytes)
 * println(TimeTrial.BINARY(bytes))
 * }}}
 */

object ExampleTimeServer extends App {
  println("Binding to ports.")
  Http().newServerAt("0.0.0.0", 8080).bindFlow(TimeServerRoutes.routes)
}

