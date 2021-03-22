package ai.dragonfly.monotomni

import connection.TimeServerConnectionFactory
import TimeTrial.Formats
import org.scalajs.dom.experimental.URL

import scala.language.implicitConversions
import scala.concurrent.ExecutionContextExecutor
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

package object native {
  /**
   * native ExecutionContextExecutor for asynchronous operations conducted by RemoteClock and TimeTrial
   */
  implicit val executor:ExecutionContextExecutor = scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  /**
   * The 64 Bit Integer serves as the fundamental data type for Mono+Omni timestamps and MOI/AMI identifiers.
   * Internally, both JVM and Scala.JS implementations rely on scala.Long which has no analogous type in JavaScript.
   * For JavaScript developers who want to use Mono+Omni as a library, [[ai.dragonfly.monotomni.native.JavaScriptFacade]]
   * provides variants of all public methods with parameters of type: scala.Long which instead take parameters of type:
   * js.BigInt.
   * JavaScript developers can write: monotomni.TimeTrial(1615141312110n) or monotomni.TimeTrial(BigInt("1615141312110"))
   * Unfortunately, this disqualifies the Mono+Omni JavaScript library from running on Internet Explorer which Microsoft
   * no longer maintains.
   */

  /**
   * Implicitly converts between js.BigInt and Long/MOI types to support Mono+Omni as a native JavaScript library.
   *
   * @param bi a native JavaScript BigInt
   * @return Long
   */
  implicit def jsBigInt2Long(bi:js.BigInt):Long = java.lang.Long.parseLong(bi.toString())

  /**
   * Implicitly converts between Long/MOI and js.BigInt types to support Mono+Omni as a native JavaScript library.
   *
   * @param l a scala.Long.
   * @return js.BigInt
   */
  implicit def Long2jsBigInt(l:Long):js.BigInt = js.BigInt(l.toString)

  /**
   * Implicitly converts between native JavaScript URL and java.net.URI types to support Mono+Omni as a native JavaScript library.
   * @param url a native JavaScript URL.
   * @return java.net.URI
   */
  implicit def URL2URI(url:URL):java.net.URI = new java.net.URI(url.toString)

  /**
   * Wraps internal Exception types in instances of scala.scalajs.js.JavaScriptException.
   * @param f a factory method that returns an Exception from a single parameter of type:String.
   * @return
   */
  def exceptionJS(f:String => Throwable):String => JavaScriptException = (s:String) => JavaScriptException(f(s))

  /**
   * Generates a facade for scala or scala.js factory methods in the form of a native JavaScript Function Object with
   * member fields.
   *
   * factoryJS generates native JavaScript objects with static methods and fields scoped analogously to scala syntax for
   * case classes and classes with companion objects.
   *
   * For example, instead of having to call: some.path.ClassName.apply() to access a case class's factory method,
   * native JavaScript developers can call: some.path.ClassName() just as Scala and Scala.js developers can.
   *
   * When using this on factories with default parameters or overloaded factory methods, take care to handle undefined
   * parameters in a single apply method.
   *
   * @param f a function that wraps a scala object's factory apply method.
   * @param members other data members that appear in the scala object that should get exposed in JavaScript.
   * @return
   */
  def factoryJS(f:js.Function, members:(String, js.Any)*):js.Dynamic = {
    val jsObj:js.Dynamic = js.Dynamic.literal("t" -> f).selectDynamic("t")
    for ((name, value) <- members) jsObj.updateDynamic(name)(value)
    jsObj
  }

  /**
   * Convenience method for exporting TimeServerConnectionFactory objects to JavaScript.
   * @param tscf a TimeServerConnectionFactory for export to JavaScript
   * @return a native JavaScript instance of a TimeServerConnectionFactory object
   */
  def timeServerConnection(tscf:TimeServerConnectionFactory):js.Dynamic = factoryJS(
    (url:URL, format:Formats.Format, timeout:Int) => tscf(
      url,
      orDefault[Formats.Format](format, tscf.defaultFormat),
      orDefaultInt(timeout, tscf.defaultTimeout)
    ),
    "defaultTimeout" -> tscf.defaultTimeout,
    "defaultFormat" -> tscf.defaultFormat.asInstanceOf[js.Any],
    "supportedFormats" -> js.Array(tscf.supportedFormats),
    "supportsFormat" -> tscf.supportsFormat _
  )

  /**
   * Convenience Method for handling default values in native JavaScript factories and overloaded methods.
   * @param parameter a parameter that may have an undefined value.
   * @param default a default value to use if parameter is undefined.
   * @tparam T the type of the default value
   * @return the specified parameter or the default value.
   */
  def orDefault[T <: js.Any](parameter:T, default:T):T = if(js.isUndefined(parameter)) default else parameter

  def orDefaultInt(parameter:Int, default:Int):Int = if(parameter == 0) default else parameter

  /**
   * exit the program if running in Node.JS
   * @param i exit code.
   */
  def exit(i: Int):Unit = try {
    js.Dynamic.global.process.exit(i)
  } catch {
    case _:Throwable => println("Can't exit from the browser.")
  }

}