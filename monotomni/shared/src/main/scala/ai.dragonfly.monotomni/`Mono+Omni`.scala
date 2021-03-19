package ai.dragonfly.monotomni

import scala.scalajs.js.annotation.JSExport
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.Future


object `Mono+Omni` {

  /* dawnOfTime = 1615141312110L
   * Helpful mnemonic: the digits of dawnOfTime count down from 16 to 11 then ends with a 0: 16 15 14 13 12 11 0 L
   * println(new java.util.Date(1615141312110L)) -> Sun Mar 07 11:21:52 MST 2021 */

  final val dawnOfTime:Long = 1615141312110L // timestamp of the birth of this library
  final val host:Long = 0x0000000F & native.HostName.hostName.hashCode() // can't use Byte.  in JavaScript environments, it's an Int/Double
  private final val counter:AtomicLong = new AtomicLong(0L)  // overflows safely with bitmask.  No need to ever reset it.
  private def count():Long = synchronized { 0x0000fff0L & (counter.getAndIncrement() << 8) } // mask off all but least significant 24 bits.
  @inline private def shiftTime(ts:Long):Long = (ts - dawnOfTime >>> 8) << 32
  def apply():MOI = shiftTime(System.currentTimeMillis()) | count() | host

  @JSExport override def toString: String = s"Mono+Omni( dawnOfTime = $dawnOfTime, host = $host, counter = ${counter.get()} )"

}

object Mono {
  @inline def +(o: `Mono+Omni`.type):`Mono+Omni`.type = o
  @inline def +(moi: MOI):MOI = moi // also handles Long
  @inline def +(i: Int):Int = i
  @inline def +(b: Byte):Byte = b
}

object `Remo+Ami` {
  def apply(moi:MOI = Mono+Omni())(implicit r:Remote):AMI = r(moi)
}

object Remo {
  @inline def +(o: `Remo+Ami`.type):`Remo+Ami`.type = o
  @inline def +(ami: AMI):AMI = ami // also handles Long
  @inline def +(fami:Future[AMI]):Future[AMI] = fami // also handles Future[Long]
  @inline def +(i: Int):Int = i
  @inline def +(b: Byte):Byte = b
  @inline def +():Unit = ()
}
