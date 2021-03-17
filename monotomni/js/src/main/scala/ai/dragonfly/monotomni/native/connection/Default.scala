package ai.dragonfly.monotomni.native.connection

import java.net.URI

import ai.dragonfly.monotomni.connection.TimeServerConnectionFactory
import org.scalajs.dom.window

object Default {

  def apply(uri:URI):TimeServerConnectionFactory = {
    val timeServerConnection:TimeServerConnectionFactory = try {
      println("Try AJAX")
      if (window.location.hostname == uri.getHost) http.AJAX
      else {
        println("Domains don't match!\nTry JSONP")
        http.JSONP
      }
    } catch {
      case t: Throwable =>
        println("No Browser Context Available!  No AJAX, No JSONP.\nTry Node.JS")
        http.NodeJS
    }
    println(s"Connecting to:\n\tTimeServer@$uri\n\twith $timeServerConnection")
    timeServerConnection
  }

}
