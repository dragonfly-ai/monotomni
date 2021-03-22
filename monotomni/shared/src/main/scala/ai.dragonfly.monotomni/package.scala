package ai.dragonfly

import java.util.concurrent.atomic.AtomicLong

import scala.language.postfixOps
import scala.language.implicitConversions
import ai.dragonfly.monotomni.native

import scala.concurrent.ExecutionContext
import scala.scalajs.js.annotation.JSExport
import scala.util.matching.Regex

package object monotomni {

  implicit val executionContext:ExecutionContext = native.executor

  trait Clock {
    def now():Long
  }

  object Omni extends Clock {

    /** dawnOfTime = 1615141312110L
     * Helpful mnemonic: the digits of dawnOfTime count down from 16 to 11 then ends with a 0: 16 15 14 13 12 11 0 L
     * println(new java.util.Date(1615141312110L)) -> Sun Mar 07 11:21:52 MST 2021 */

    final val dawnOfTime:Long = 1615141312110L // timestamp of the birth of this library
    final val host:Long = 0x0000000F & native.HostName.hostName.hashCode() // can't use Byte.  in JavaScript environments, it's an Int/Double
    private final val counter:AtomicLong = new AtomicLong(0L)  // overflows safely with bitmask.  No need to ever reset it.
    private def count():Long = synchronized { 0x0000fff0L & (counter.getAndIncrement() << 8) } // mask off all but least significant 24 bits.
    @inline private def shiftTime(ts:Long):Long = (ts - dawnOfTime >>> 8) << 32
    def apply():MOI = shiftTime(System.currentTimeMillis()) | count() | host
    override def now():Long = System.currentTimeMillis()

    @JSExport override def toString: String = s"Mono+Omni( dawnOfTime = $dawnOfTime, host = $host, counter = ${counter.get()} )"

  }

  type MonotOmni = Omni.type

  object Mono {
    override def toString:String = ""
    def +(l:Long):Long = l
    def +(omni:Omni.type):Omni.type = omni
  }

  type MOI = Long // Type Alias communicates the difference between longs and Mono+Omni literals.

  implicit def byteArray2String(bytes: Array[Byte]): String = s"Array[Byte](${
    if (bytes.length < 1) "()" else {
      val sb = new StringBuilder(s"${bytes(0)}")
      for (i <- 1 until Math.min(bytes.length, 25)) sb.append(s", ${bytes(i).toHexString}")
      if (bytes.length > 24) sb.append(s", & ${bytes.length - 25} more")
      sb.append(")").toString
    }
  })"

  implicit class M0I(moi: MOI) {
    def timestamp:Long = Mono+Omni.dawnOfTime + (moi >>> 32)
    def hashMask:Long = Mono+Omni.host
    def count:Long = (0x0000fff0L & moi) >>> 8
    override def toString: String = s"MOI($timestamp, $count, $hashMask)"
  }

  type AMI = Long

  implicit class AM1(ami:AMI)(implicit remoteClock:RemoteClock) {
    def dT:Long = remoteClock.now() - (Mono+Omni.now())
    def timestamp:Long = Mono+Omni.dawnOfTime + (ami >>> 32) + dT
    def hashMask:Long = 0x0000000F & ami
    def count:Long = (0x0000fff0L & ami) >>> 8
    override def toString: String = s"AMI($timestamp, $count, $hashMask)"
  }

  val N2Int:Regex = "([0-9]{1,10})".r
  val N2Long:Regex = "([0-9]{1,19})".r
}