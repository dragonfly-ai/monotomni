package ai.dragonfly.distributed.monotomni

import scala.concurrent.Future

/**
  * Created by clifton on 11/24/14.
  */

object Test extends App {

    println("Testing Mono+Omni TimeTrial parsers:")
    TimeTrial.test()

    println("Testing TimeServer and TimeServerClient TimeTrials:")
    val uri:java.net.URI = new java.net.URI("http://localhost:8080/time")
    println(uri)
    val futureTimeServer:Future[TimeServer] = TimeServer.HTTP(uri)
    println(futureTimeServer)

    val src:Generator = Generator(futureTimeServer)

    src.ready(() => {
        for {
            i0:Moi <- src.next()
            i1:Moi <- src.next()
            i2:Moi <- src.next()
            i3:Moi <- src.next()
        } yield {
            def printSnowflake(snflk: Moi):Unit = println(s"$snflk -> ${M0I(snflk)}")
            printSnowflake(i0)
            printSnowflake(i1)
            printSnowflake(i2)
            printSnowflake(i3)
            println(s"Average Snowflake:${(i0 + i1 + i2 + i3) / 4}")
        }
      }
    )
}