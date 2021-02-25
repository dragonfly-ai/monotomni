package ai.dragonfly.distributed

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import ai.dragonfly.distributed.monotomni.native

import scala.util.matching.Regex

package object monotomni {
  type Moi = Long  // Type Alias to communicate the difference between longs and snowflake literals.

  //implicit def longToM0I(moi: Moi): M0I = M0I(moi)
  //implicit def M0IToLong(m0i: M0I): Moi = m0i.moi
  implicit val executor: ExecutionContext = native.executor

  implicit def byteArray2String(bytes: Array[Byte]): String = s"Array[Byte](${
    if (bytes.length < 1) "()" else {
      val sb = new StringBuilder(s"${bytes(0)}")
      for (i <- 1 until Math.min(bytes.length, 25)) sb.append(s", ${bytes(i).toHexString}")
      if (bytes.length > 24) sb.append(s", & ${bytes.length - 25} more")
      sb.append(")").toString
    }
  })"
  implicit class M0I(moi: Moi) {
    def timestamp:Long = moi >>> 32 + Omni.dawnOfTime
    def hashMask:Int = Omni.hashMask
    def count:Int = ((moi << 56) >>> 56).toInt
    override def toString: String = s"Moi(${moi >>> 32}, ${(moi << 32) >> 40}, ${(moi << 56) >>> 56})"
  }

  val N2Int:Regex = "([0-9]{1,10})".r
  val N2Long:Regex = "([0-9]{1,19})".r
}