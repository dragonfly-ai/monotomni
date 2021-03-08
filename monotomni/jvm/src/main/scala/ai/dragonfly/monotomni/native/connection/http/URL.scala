package ai.dragonfly.monotomni.native.connection.http

import java.net.URI
import java.util.concurrent.TimeoutException
import java.util.{Timer, TimerTask}

import ai.dragonfly.monotomni.TimeTrial.{TimeTrialFormat => TTF}
import TTF.TimeTrialFormat
import ai.dragonfly.monotomni.TimeTrial.inputStream2String
import ai.dragonfly.monotomni._
import ai.dragonfly.monotomni.connection.TimeServerConnection.TimeServerConnection

import scala.concurrent.{Future, Promise}

case class URL(uri:URI, override val defaultFormat:TimeTrialFormat = TTF.BINARY, override val defaultTimeout:Int = 3000) extends TimeServerConnection {

  override def supportedFormats:Seq[TimeTrialFormat] = Seq(TTF.BINARY, TTF.STRING, TTF.JSON, TTF.XML)

  /**
   * Execute a TimeTrial
   * @param format The format of the server response message.  Configurable for custom time servers.
   * @return PendingTimeTrial
   */
  override def timeTrial(format:TimeTrialFormat = defaultFormat, timeoutMilliseconds:Int = defaultTimeout): PendingTimeTrial = {
    val urlTxt = s"$uri/$format/"

    val promisedTimeTrial:Promise[TimeTrial] = Promise[TimeTrial]()

    val futureTimeTrial: Future[TimeTrial] = Future[TimeTrial]{
      val inputStream = new java.net.URL( urlTxt ).openConnection().getInputStream
      TimeTrial.fromInputStream(format, inputStream)
    }

    promisedTimeTrial.completeWith(futureTimeTrial)

    val pendingTimeTrial:PendingTimeTrial = PendingTimeTrial(promisedTimeTrial)
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