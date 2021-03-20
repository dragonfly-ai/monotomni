package ai.dragonfly.monotomni

import connection.{TimeServerConnection, TimeServerConnectionFactory}
import TimeTrial.Formats
import native.connection.http.{AJAX, JSONP, NodeJS}
import native.connection.Default
import org.scalajs.dom.experimental.URL

import scala.language.implicitConversions
import scala.concurrent.ExecutionContextExecutor
import scala.scalajs.js
import js.annotation.JSExportTopLevel
import scala.scalajs.js.JavaScriptException

package object native {

  implicit def jsBigInt2Long(bi:js.BigInt):Long = java.lang.Long.parseLong(bi.toString())
  implicit def Long2jsBigInt(l:Long):js.BigInt = native.String2jsBigInt(l.toString)

  implicit def URL2URI(url:URL):java.net.URI = new java.net.URI(url.toString)

  implicit val executor:ExecutionContextExecutor = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  implicit def String2jsBigInt(s:String):js.BigInt = js.BigInt(s)

  def ClassAndCompanion2JS(f:js.Function, members:(String, js.Any)*):js.Dynamic = {
    val jsObj:js.Dynamic = js.Dynamic.literal("t" -> f).selectDynamic("t")
    for ((name, value) <- members) jsObj.updateDynamic(name)(value)
    jsObj
  }

  val timeTrial:js.Dynamic = ClassAndCompanion2JS(
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
  )  // KEEP GOING!

  val pendingTimeTrial:js.Dynamic = ClassAndCompanion2JS(PendingTimeTrial.apply _)

  val timeTrialJSONP:js.Dynamic = ClassAndCompanion2JS(
    TimeTrialJSONP.apply _,
    "test" -> TimeTrialJSONP.test _,
    "JSONP" -> TimeTrialJSONP.JSONP _
  )

  val `Mono+Omni.Scope`:js.Dynamic = ClassAndCompanion2JS(
    () => Long2jsBigInt(Mono+Omni()),
    "dawnOfTime" -> Long2jsBigInt(Mono+Omni.dawnOfTime),
    "host" -> Long2jsBigInt(Mono+Omni.host),
    "now" -> (() => Long2jsBigInt(Mono+Omni.now())),
    "toString" -> Omni.toString _
  )

  //js.Dynamic.global.updateDynamic("Mono+Omni")(`Mono+Omni.Scope`) // window["Mono+Omni"]
  //js.Dynamic.global.updateDynamic("Mono〸Omni")(`Mono+Omni.Scope`) // global: Mono〸Omni

  val remoteClock:js.Dynamic = ClassAndCompanion2JS(
    (timeServerConnection: TimeServerConnection, maxWait:js.BigInt) => new ai.dragonfly.monotomni.RemoteClock(
      timeServerConnection,
      if(js.isUndefined(maxWait)) RemoteClock.defaultMaxWait else maxWait
    ),
    "defaultMaxWait" -> Long2jsBigInt(RemoteClock.defaultMaxWait)
  )

  def timeServerConnection(tscf:TimeServerConnectionFactory):js.Dynamic = ClassAndCompanion2JS(
    (url:URL, format:Formats.Format, timeout:Int) => tscf(
      url,
      if (js.isUndefined(format)) tscf.defaultFormat else format,
      if(timeout <= 1) tscf.defaultTimeout else timeout
    ),
    "defaultTimeout" -> tscf.defaultTimeout,
    "defaultFormat" -> tscf.defaultFormat.asInstanceOf[js.Any],
    "supportedFormats" -> tscf.supportedFormats
  )

  def exceptionJS(f:String => Throwable):String => JavaScriptException = (s:String) => JavaScriptException(f(s))

  @JSExportTopLevel("Mono") val mono:js.Dynamic = js.Dynamic.literal(
    "valueOf" -> (() => js.BigInt(0)),
    "toString" -> (() => "")
  )
  @JSExportTopLevel("Omni") val omni:js.Dynamic = `Mono+Omni.Scope`

  @JSExportTopLevel("monotomni")
  val JavaScriptScope:js.Dynamic = js.Dynamic.literal(
    // TimeTrial:
    "TimeTrial" -> timeTrial,
    "PendingTimeTrial" -> pendingTimeTrial,
    "TimeTrialJSONP" -> timeTrialJSONP,
    // TimeTrial Exceptions:
    // "InvalidTimeTrialBinary" -> InvalidTimeTrialBinary.apply _,  // not meaningful in JavaScript library context
    "InvalidTimeTrialString" -> exceptionJS(InvalidTimeTrialString.apply),
    "InvalidTimeTrialJSON" -> exceptionJS(InvalidTimeTrialJSON.apply),
    "InvalidTimeTrialParameter" -> exceptionJS(InvalidTimeTrialParameter.apply),
    "InvalidTimeTrialXML" -> exceptionJS(InvalidTimeTrialXML.apply),
    "InvalidTimeTrialParametersJSONP" -> ((pendingTimeTrialId:String, t:String) => JavaScriptException(InvalidTimeTrialParametersJSONP(pendingTimeTrialId, t))),
    "InvalidTimeTrialJSONP" -> exceptionJS(InvalidTimeTrialJSONP.apply),
    // Remote:
    "RemoteClock" -> remoteClock,
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