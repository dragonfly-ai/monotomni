package ai.dragonfly.monotomni.native

import java.io.ByteArrayInputStream
import java.nio.ByteBuffer

import ai.dragonfly.monotomni

import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer, Uint8Array}

/**
 * Native trait for JavaScript implementation of [[monotomni.RemoteClock]].
 */
trait TimeTrial {

  /**
   * Native Method to support BINARY [[monotomni.TimeTrial]] parsing from the [[connection.http.AJAX]] [[monotomni.connection.TimeServerConnection]]
   * @param arrayBuffer a [[monotomni.TimeTrial]] [[monotomni.TimeTrial.Formats.BINARY]] wrapped in an ArrayBuffer
   * @return a [[monotomni.TimeTrial]] instance.
   */
  def fromArrayBuffer(arrayBuffer:ArrayBuffer):monotomni.TimeTrial = {
    val wrapped:ByteBuffer = TypedArrayBuffer.wrap(arrayBuffer)
    val bytes:Array[Byte] = new Array[Byte](monotomni.TimeTrial.BYTES)
    wrapped.get(bytes, 0, monotomni.TimeTrial.BYTES)
    monotomni.TimeTrial.fromInputStream(monotomni.TimeTrial.Formats.BINARY, new ByteArrayInputStream(bytes))
  }

  /**
   * Native Method to support [[monotomni.TimeTrial]] parsing from the NodeJS [[monotomni.connection.TimeServerConnection]]
   * This method gets invoked for all formats, not just [[monotomni.TimeTrial.BINARY]].
   *
   * @param format the format of the [[monotomni.TimeTrial]] encoded in th ui8arr:Uint8Array parameter.
   * @param ui8arr an encoded [[monotomni.TimeTrial]]
   * @return a [[monotomni.TimeTrial]] instance.
   */
  def fromUint8Array(format:monotomni.TimeTrial.Formats.Format, ui8arr:Uint8Array):monotomni.TimeTrial = {
    val arr:Array[Byte] = new Array[Byte](ui8arr.byteLength)
    for (i <- 0 until ui8arr.byteLength) { arr(i) = ui8arr(i).toByte }
    monotomni.TimeTrial.fromInputStream(format, new ByteArrayInputStream( arr ) )
  }

}