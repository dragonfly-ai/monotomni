package ai.dragonfly.monotomni

import scala.concurrent.ExecutionContext

package object native {
  implicit val executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
