package ai.dragonfly.monotomni

import scala.concurrent.ExecutionContext

package object native {
  implicit val executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def exit(i: Int):Unit = sys.exit(i)
}
