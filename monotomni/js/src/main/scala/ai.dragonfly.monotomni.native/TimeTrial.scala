package ai.dragonfly.monotomni.native

import java.io.ByteArrayInputStream
import java.nio.ByteBuffer

import ai.dragonfly.monotomni

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer, Uint8Array}

trait TimeTrial {

  val fromArrayBuffer:js.Function1[ArrayBuffer, monotomni.TimeTrial] = (arrayBuffer:ArrayBuffer) => {
    val wrapped:ByteBuffer = TypedArrayBuffer.wrap(arrayBuffer)
    val bytes:Array[Byte] = new Array[Byte](monotomni.TimeTrial.BYTES)
    wrapped.get(bytes, 0, monotomni.TimeTrial.BYTES)
    monotomni.TimeTrial.fromInputStream(monotomni.TimeTrial.Formats.BINARY, new ByteArrayInputStream(bytes))
  }

  val fromUint8Array:js.Function2[monotomni.TimeTrial.Formats.Format, Uint8Array, monotomni.TimeTrial] = (format:monotomni.TimeTrial.Formats.Format, ui8arr:Uint8Array) => {
    val arr:Array[Byte] = new Array[Byte](ui8arr.byteLength)
    for (i <- 0 until ui8arr.byteLength) { arr(i) = ui8arr(i).toByte }
    monotomni.TimeTrial.fromInputStream(format, new ByteArrayInputStream( arr ) )
  }

}