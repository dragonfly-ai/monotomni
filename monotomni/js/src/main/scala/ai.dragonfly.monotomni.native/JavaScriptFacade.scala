package ai.dragonfly.monotomni.native

import ai.dragonfly.monotomni.TimeTrial.Formats
import ai.dragonfly.monotomni.{FailedTimeServerConnectionSynchronization, InvalidTimeTrialJSON, InvalidTimeTrialJSONP, InvalidTimeTrialParameter, InvalidTimeTrialParametersJSONP, InvalidTimeTrialString, InvalidTimeTrialXML, Mono, Omni, PendingTimeTrial, RemoteClock, RemoteClockNotReady, TimeTrial, TimeTrialJSONP}
import ai.dragonfly.monotomni.connection.TimeServerConnection
import ai.dragonfly.monotomni.native.connection.Default
import ai.dragonfly.monotomni.native.connection.http.{AJAX, JSONP, NodeJS}

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
    "toString" -> Omni.toString _
  )

  @JSExportTopLevel("monotomni")
  val scope:js.Dynamic = js.Dynamic.literal(
    // TimeTrial:
    "TimeTrial" -> factoryJS(
      (serverTimeStamp:js.BigInt) => TimeTrial(serverTimeStamp),
      "Formats" -> js.Dynamic.literal(
        "BINARY" -> Formats.BINARY.asInstanceOf[js.Any],
        "STRING" -> Formats.STRING.asInstanceOf[js.Any],
        "JSON" -> Formats.JSONP.asInstanceOf[js.Any],
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
    // "InvalidTimeTrialBinary" -> InvalidTimeTrialBinary.apply _,  // not meaningful in JavaScript library context
    "InvalidTimeTrialString" -> exceptionJS(InvalidTimeTrialString.apply),
    "InvalidTimeTrialJSON" -> exceptionJS(InvalidTimeTrialJSON.apply),
    "InvalidTimeTrialParameter" -> exceptionJS(InvalidTimeTrialParameter.apply),
    "InvalidTimeTrialXML" -> exceptionJS(InvalidTimeTrialXML.apply),
    "InvalidTimeTrialParametersJSONP" -> ((pendingTimeTrialId:String, t:String) => JavaScriptException(InvalidTimeTrialParametersJSONP(pendingTimeTrialId, t))),
    "InvalidTimeTrialJSONP" -> exceptionJS(InvalidTimeTrialJSONP.apply),
    // Remote:
    "RemoteClock" -> factoryJS(
      (timeServerConnection: TimeServerConnection, maxWait:js.BigInt) => new ai.dragonfly.monotomni.RemoteClock(
        timeServerConnection,
        if(js.isUndefined(maxWait)) RemoteClock.defaultMaxWait else maxWait
      ),
      "defaultMaxWait" -> Long2jsBigInt(RemoteClock.defaultMaxWait)
    ),
    // Remote Exceptions:
    "FailedTimeServerConnectionSynchronization" -> FailedTimeServerConnectionSynchronization.apply _,
    "RemoteConnectionNotReady" -> RemoteClockNotReady.apply _,
    "connection" -> js.Dynamic.literal(
      "http" -> js.Dynamic.literal(
        "AJAX" -> timeServerConnection(AJAX),
        "JSONP" -> timeServerConnection(JSONP),
        "NodeJS" -> timeServerConnection(NodeJS)
      ),
      "Default" -> Default.apply _
    )
  )
}
