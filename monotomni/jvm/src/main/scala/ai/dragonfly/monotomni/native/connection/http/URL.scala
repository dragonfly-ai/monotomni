package ai.dragonfly.monotomni.native.connection.http

import java.net.URI

import ai.dragonfly.monotomni
import monotomni._
import monotomni.TimeTrial.Formats
import monotomni.connection.TimeServerConnectionFactory
import monotomni.connection.http.TimeServerConnectionHTTP

import scala.concurrent.{Future, Promise}

object URL extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 3000
  override val defaultFormat: Formats.Format = Formats.BINARY
}

/**
 * Native JVM implementation of a [[monotomni.connection.http.TimeServerConnectionHTTP]]
 * @param uri the http or https address of a TimeServer, e.g. https://timeserver.domain.com/time
 * @param format The format of the server response message.  Configurable for custom time servers.
 * @param defaultTimeout number of milliseconds until the request times out.
 */
case class URL(override val uri:URI, override val format:Formats.Format, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  override val path: String = uri.toString

  /**
   * Execute a TimeTrial
   * @param timeoutMS number of milliseconds until the request times out.
   * @return PendingTimeTrial
   */
  override def timeTrial(timeoutMS:Int = defaultTimeout): PendingTimeTrial = {
    PendingTimeTrial(
      Promise[TimeTrial]().completeWith(
        Future[TimeTrial]{
          TimeTrial.fromInputStream(
            format,
            new java.net.URL( s"$uri/$format/" ).openConnection().getInputStream
          )
        }
      ),
      timeoutMS
    )
  }

}