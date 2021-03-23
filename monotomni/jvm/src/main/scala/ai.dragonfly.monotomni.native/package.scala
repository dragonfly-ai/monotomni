package ai.dragonfly.monotomni

import ai.dragonfly.monotomni

import scala.concurrent.ExecutionContext

package object native {
  /**
   * native ExecutionContextExecutor for asynchronous operations conducted by [[monotomni.RemoteClock]] and [[monotomni.TimeTrial]]
   */
  implicit val executor: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  /**
   * exposes native exit functionality for [[monotomni.Demo]] and [[monotomni.RemoteClock]] [[monotomni.TimeTrial]]s
   */
  def exit(i: Int):Unit = sys.exit(i)
}
