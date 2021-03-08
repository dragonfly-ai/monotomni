package ai.dragonfly

import scala.language.postfixOps
import scala.language.implicitConversions
import ai.dragonfly.monotomni.native

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

package object monotomni {

  implicit val ec:ExecutionContext = native.executor

  val Omni:`Mono+Omni`.type = `Mono+Omni`
  type MOI = Long  // Type Alias to communicate the difference between longs and Mono+Omni literals.

  implicit def byteArray2String(bytes: Array[Byte]): String = s"Array[Byte](${
    if (bytes.length < 1) "()" else {
      val sb = new StringBuilder(s"${bytes(0)}")
      for (i <- 1 until Math.min(bytes.length, 25)) sb.append(s", ${bytes(i).toHexString}")
      if (bytes.length > 24) sb.append(s", & ${bytes.length - 25} more")
      sb.append(")").toString
    }
  })"

  implicit class M0I(moi: MOI) {
    def timestamp:Long = Mono+Omni.dawnOfTime + moi >>> 32
    def hashMask:Int = Mono+Omni.host
    def count:Int = ((moi << 56) >>> 56).toInt
    override def toString: String = s"Moi(${moi >>> 32}, ${(moi << 32) >> 40}, ${(moi << 56) >>> 56})"
  }

  val Ami:`Remo+Ami`.type = `Remo+Ami`
  type AMI = Long

  val N2Int:Regex = "([0-9]{1,10})".r
  val N2Long:Regex = "([0-9]{1,19})".r
}