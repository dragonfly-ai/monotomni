package ai.dragonfly.monotomni.native

import ai.dragonfly.monotomni
import monotomni.TimeTrial.Formats
import monotomni.{FailedTimeServerConnectionSynchronization, InvalidTimeTrialJSON, InvalidTimeTrialJSONP, InvalidTimeTrialParameter, InvalidTimeTrialParametersJSONP, InvalidTimeTrialString, InvalidTimeTrialXML, Mono, Omni, PendingTimeTrial, RemoteClock, RemoteClockNotReady, TimeTrial, TimeTrialJSONP}
import monotomni.connection.TimeServerConnection
import monotomni.native.connection.Default
import monotomni.native.connection.http.{AJAX, JSONP, NodeJS}

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.annotation.JSExportTopLevel

/**
 * Exposes Mono+Omni library bindings to native JavaScript.
 */
object JavaScriptFacade {

  @JSExportTopLevel("Mono") val mono:js.Dynamic = js.Dynamic.literal(
    "valueOf" -> (() => js.BigInt(0)),
    "toString" -> (() => "")
  )

  @JSExportTopLevel("Omni") val omni:js.Dynamic = factoryJS(
    () => Long2jsBigInt(Mono+Omni()),
    "dawnOfTime" -> Long2jsBigInt(Mono+Omni.dawnOfTime),
    "host" -> Long2jsBigInt(Mono+Omni.host),
    "now" -> (() => Long2jsBigInt(Mono+Omni.now())),
    "valueOf" -> Omni.toString _,
    "toString" -> (() => s"bject: ${Omni.toString()}")
  )

  @JSExportTopLevel("monotomni")
  val scope:js.Dynamic = js.Dynamic.literal(
    "MOI" -> factoryJS(
      (moiJS:js.BigInt) => {
        val moi:monotomni.M0I = monotomni.M0I(moiJS)
        js.Dynamic.literal(
          "timestamp" ->  Long2jsBigInt(moi.timestamp),
          "hashMask" ->  Long2jsBigInt(Mono+Omni.host),
          "count" ->  Long2jsBigInt(moi.count),
          "toString" -> moi.toString _
        )
      }
    ),
    "AMI" -> factoryJS(
      (amiJS:js.BigInt, remoteClock:monotomni.RemoteClock) => {
        val ami:monotomni.AM1 = monotomni.AM1(amiJS)(remoteClock)
        js.Dynamic.literal(
          "timestamp" ->  Long2jsBigInt(ami.timestamp),
          "hashMask" ->  Long2jsBigInt(ami.hashMask),
          "count" ->  Long2jsBigInt(ami.count),
          "toString" -> ami.toString _
        )
      }
    ),
    // TimeTrial:
    "TimeTrial" -> factoryJS(
      (serverTimeStamp:js.BigInt) => TimeTrial(serverTimeStamp),
      "Formats" -> js.Dynamic.literal(
        "BINARY" -> Formats.BINARY.asInstanceOf[js.Any],
        "STRING" -> Formats.STRING.asInstanceOf[js.Any],
        "JSON" -> Formats.JSON.asInstanceOf[js.Any],
        "XML" -> Formats.XML.asInstanceOf[js.Any],
        "JSONP" -> Formats.JSONP.asInstanceOf[js.Any]
      ),
      "Bytes" -> TimeTrial.BYTES,
      "fromArrayBuffer" -> TimeTrial.fromArrayBuffer,
      "fromUint8Array" -> TimeTrial.fromUint8Array,
      "BINARY" -> TimeTrial.BINARY _,
      "JSON" -> TimeTrial.JSON _,
      "STRING" -> TimeTrial.STRING _,
      "XML" -> TimeTrial.XML _,
      "test" -> TimeTrial.test _
    ),
    "PendingTimeTrial" -> factoryJS(PendingTimeTrial.apply _),
    "TimeTrialJSONP" -> factoryJS(
      TimeTrialJSONP.apply _,
      "test" -> TimeTrialJSONP.test _,
      "JSONP" -> TimeTrialJSONP.JSONP _
    ),
    // TimeTrial Exceptions:
    // "InvalidTimeTrialBinary" -> exceptionJS(InvalidTimeTrialBinary.apply),  // todo: adapt to ArrayBuffer and Uint8Array versions?
    "InvalidTimeTrialString" -> exceptionJS(InvalidTimeTrialString.apply),
    "InvalidTimeTrialJSON" -> exceptionJS(InvalidTimeTrialJSON.apply),
    "InvalidTimeTrialParameter" -> exceptionJS(InvalidTimeTrialParameter.apply),
    "InvalidTimeTrialXML" -> exceptionJS(InvalidTimeTrialXML.apply),
    "InvalidTimeTrialParametersJSONP" -> ((pendingTimeTrialId:String, t:String) => JavaScriptException(InvalidTimeTrialParametersJSONP(pendingTimeTrialId, t))),
    "InvalidTimeTrialJSONP" -> exceptionJS(InvalidTimeTrialJSONP.apply),
    // Remote:
    "RemoteClock" -> factoryJS(
      (timeServerConnection: TimeServerConnection, maxWait:js.BigInt) => new monotomni.RemoteClock(
        timeServerConnection,
        orDefault[js.BigInt](maxWait, RemoteClock.defaultMaxWait)
      ),
      "defaultMaxWait" -> Long2jsBigInt(RemoteClock.defaultMaxWait)
    ),
    // Remote Exceptions:
    "FailedTimeServerConnectionSynchronization" -> FailedTimeServerConnectionSynchronization.apply _,
    "RemoteConnectionNotReady" -> RemoteClockNotReady.apply _,
    "connection" -> js.Dynamic.literal(
      "http" -> js.Dynamic.literal(
        "AJAX" -> timeServerConnection(AJAX),
        "JSONP" -> {
          val jsonp:js.Dynamic = timeServerConnection(JSONP)
          jsonp.updateDynamic("logTimeTrial")(JSONP.logTimeTrial _)
          jsonp
        },
        "NodeJS" -> timeServerConnection(NodeJS)
      ),
      "Default" -> Default.apply _
    )
  )
}
