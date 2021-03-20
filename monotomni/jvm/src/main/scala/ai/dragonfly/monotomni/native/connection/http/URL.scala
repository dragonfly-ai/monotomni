package ai.dragonfly.monotomni.native.connection.http

import java.net.URI
import java.util.concurrent.TimeoutException
import java.util.{Timer, TimerTask}

import ai.dragonfly.monotomni.TimeTrial.Formats
import ai.dragonfly.monotomni.TimeTrial.Formats.Format
import ai.dragonfly.monotomni._
import ai.dragonfly.monotomni.connection.TimeServerConnectionFactory
import ai.dragonfly.monotomni.connection.http.TimeServerConnectionHTTP

import scala.concurrent.{Future, Promise}

object URL extends TimeServerConnectionFactory {
  override val defaultTimeout: Int = 3000
  override val defaultFormat: Formats.Format = Formats.BINARY
  override val supportedFormats: Seq[Format] = Seq(Formats.BINARY, Formats.STRING, Formats.JSON, Formats.XML)
}

case class URL(override val uri:URI, override val defaultFormat:Formats.Format, override val defaultTimeout:Int) extends TimeServerConnectionHTTP {

  override val path: String = uri.toString

  override def supportedFormats:Seq[Formats.Format] = URL.supportedFormats

  /**
   * Execute a TimeTrial
   * @param format The format of the server response message.  Configurable for custom time servers.
   * @return PendingTimeTrial
   */
  override def timeTrial(format:Formats.Format = defaultFormat, timeoutMilliseconds:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format/"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    val futureTimeTrial: Future[TimeTrial] = Future[TimeTrial]{
      val inputStream = new java.net.URL( urlTxt ).openConnection().getInputStream
      TimeTrial.fromInputStream(format, inputStream)
    }

    promisedTimeTrial.completeWith(futureTimeTrial)

    val pendingTimeTrial:PendingTimeTrial = PendingTimeTrial(promisedTimeTrial, timeoutMilliseconds)
    // handle timeout:
    new Timer(s"TimeTrial# ${pendingTimeTrial.moi} timeout monitor.").schedule(
      new TimerTask() {
        override def run():Unit = if (!futureTimeTrial.isCompleted) {
          promisedTimeTrial.failure(new TimeoutException())
        }
      },
      timeoutMilliseconds
    )
    pendingTimeTrial
  }

}
