package ai.dragonfly.distributed.monotomni

import scala.concurrent.ExecutionContextExecutor

package object native {
  implicit val executor:ExecutionContextExecutor = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
}
