package ai.dragonfly.distributed.monotomni.native

import ai.dragonfly.distributed.monotomni
import scala.concurrent.Future

case class Generator(futureTimeServer: Future[monotomni.TimeServer]) extends monotomni.Generator {

}
