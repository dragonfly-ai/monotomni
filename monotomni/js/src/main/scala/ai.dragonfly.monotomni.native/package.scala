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

  implicit def jsBigInt2Long(l:js.BigInt):Long = java.lang.Long.parseLong(l.toString())
  implicit def Long2jsBigInt(L:Long):js.BigInt = native.String2jsBigInt(L.toString())

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
    `Mono+Omni`.apply _,
    "dawnOfTime" -> Long2jsBigInt(Mono+Omni.dawnOfTime),
    "host" -> Long2jsBigInt(Mono+Omni.host)
  )

  //js.Dynamic.global.updateDynamic("Mono+Omni")(`Mono+Omni.Scope`) // window["Mono+Omni"]
  //js.Dynamic.global.updateDynamic("Mono〸Omni")(`Mono+Omni.Scope`) // global: Mono〸Omni

  val `Remo+Ami.Scope`:js.Dynamic = ClassAndCompanion2JS(
    (remote:Remote, moi:MOI) => if(js.isUndefined(moi)) Remo+Ami()(remote) else Remo+Ami(moi)(remote)
  )

  val remote:js.Dynamic = ClassAndCompanion2JS(
    (timeServerConnection: TimeServerConnection, maxWait:js.BigInt) => Remote(
      timeServerConnection,
      if(js.isUndefined(maxWait)) Remote.defaultMaxWait else maxWait
    ),
    "defaultMaxWait" -> Long2jsBigInt(Remote.defaultMaxWait)
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

  def wrapExceptionInJS(f:String => Throwable):String => JavaScriptException = (s:String) => JavaScriptException(f(s))

  @JSExportTopLevel("monotomni")
  val JavaScriptScope:js.Dynamic = js.Dynamic.literal(
    "Mono+Omni" -> `Mono+Omni.Scope`,  //u3038 = 〸
    "Remo+Ami" -> `Remo+Ami.Scope`,  //u3038 = 〸
    "now" -> (() => Long2jsBigInt(System.currentTimeMillis())),
    // TimeTrial:
    "TimeTrial" -> timeTrial,
    "PendingTimeTrial" -> pendingTimeTrial,
    "TimeTrialJSONP" -> timeTrialJSONP,
    // TimeTrial Exceptions:
    // "InvalidTimeTrialBinary" -> InvalidTimeTrialBinary.apply _,  // not meaningful in JavaScript library context
    "InvalidTimeTrialString" -> wrapExceptionInJS(InvalidTimeTrialString.apply),
    "InvalidTimeTrialJSON" -> wrapExceptionInJS(InvalidTimeTrialJSON.apply),
    "InvalidTimeTrialParameter" -> wrapExceptionInJS(InvalidTimeTrialParameter.apply),
    "InvalidTimeTrialXML" -> wrapExceptionInJS(InvalidTimeTrialXML.apply),
    "InvalidTimeTrialParametersJSONP" -> ((pendingTimeTrialId:String, t:String) => JavaScriptException(InvalidTimeTrialParametersJSONP(pendingTimeTrialId, t))),
    "InvalidTimeTrialJSONP" -> wrapExceptionInJS(InvalidTimeTrialJSONP.apply),
    // Remote:
    "Remote" -> remote,
    // Remote Exceptions:
    "FailedTimeServerConnectionSynchronization" -> FailedTimeServerConnectionSynchronization.apply _,
    "RemoteConnectionNotReady" -> RemoteConnectionNotReady.apply _,
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