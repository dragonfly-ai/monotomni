package ai.dragonfly.distributed.monotomni

import scala.concurrent.ExecutionContext

package object native {
  implicit val executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
