package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnection.TimeServerConnection
import org.scalajs.dom.window

object DefaultConnection {
  def apply(uri: URI): TimeServerConnection = {
    val timeServerConnection = try {
      println("Try AJAX")
      if (window.location.hostname == uri.getHost) http.AJAX(uri)
      else {
        println("Domains don't match!\nTry JSONP")
        http.JSONP(uri)
      }
    } catch {
//      case jse: scala.scalajs.js.JavaScriptException =>
//        println("No Browser Context Available!  No AJAX, No JSONP.\nTry Node.JS")
//        http.NodeJS(uri)
      case t: Throwable =>
        println("No Browser Context Available!  No AJAX, No JSONP.\nTry Node.JS")
        http.NodeJS(uri)
    }
    println(s"Connecting to:\n\tTimeServer@$uri\n\twith $timeServerConnection")
    timeServerConnection
  }
}
