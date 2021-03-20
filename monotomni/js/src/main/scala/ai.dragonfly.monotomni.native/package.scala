package ai.dragonfly.monotomni

import connection.TimeServerConnectionFactory
import TimeTrial.Formats
import org.scalajs.dom.experimental.URL

import scala.language.implicitConversions
import scala.concurrent.ExecutionContextExecutor
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

package object native {

  implicit def jsBigInt2Long(bi:js.BigInt):Long = java.lang.Long.parseLong(bi.toString())
  implicit def Long2jsBigInt(l:Long):js.BigInt = native.String2jsBigInt(l.toString)

  implicit def URL2URI(url:URL):java.net.URI = new java.net.URI(url.toString)

  implicit val executor:ExecutionContextExecutor = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  implicit def String2jsBigInt(s:String):js.BigInt = js.BigInt(s)

  def exceptionJS(f:String => Throwable):String => JavaScriptException = (s:String) => JavaScriptException(f(s))

  def factoryJS(f:js.Function, members:(String, js.Any)*):js.Dynamic = {
    val jsObj:js.Dynamic = js.Dynamic.literal("t" -> f).selectDynamic("t")
    for ((name, value) <- members) jsObj.updateDynamic(name)(value)
    jsObj
  }

  def timeServerConnection(tscf:TimeServerConnectionFactory):js.Dynamic = factoryJS(
    (url:URL, format:Formats.Format, timeout:Int) => tscf(
      url,
      if (js.isUndefined(format)) tscf.defaultFormat else format,
      if(timeout <= 1) tscf.defaultTimeout else timeout
    ),
    "defaultTimeout" -> tscf.defaultTimeout,
    "defaultFormat" -> tscf.defaultFormat.asInstanceOf[js.Any],
    "supportedFormats" -> tscf.supportedFormats
  )

}