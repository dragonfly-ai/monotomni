package ai.dragonfly.monotomni

import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by clifton on 11/24/14.
  */

object Demo extends App {

    println("Testing local Mono+Omni MOI generation:")
    for (i <- 0 until 10) println(s"$i => ${Mono + Omni()}")

    println("Testing Mono+Omni TimeTrial parsers:")
    TimeTrial.test()
    TimeTrialJSONP.test()

    println("Testing TimeServer and TimeServerClient TimeTrials:")
    val uri: java.net.URI = new java.net.URI("http://localhost:8080/time")
    println(s"testing on URL: $uri")

    implicit val remote: Remote = new Remote(native.connection.DefaultConnection(uri))
    println(remote)


    for (i <- 0 until 10) {
        (Remo+Ami()).onComplete {
            case Success(ami: AMI) => println(s"$i => $ami")
            case Failure(_) => println(s"Could not estimate Δt for TimeServer: $uri")
        }
    }
}

object Test {

    import scala.language.implicitConversions

    implicit def int2Byte(i: Int): Byte = i.toByte

    val aI: AtomicInteger = new AtomicInteger(0)

    trait Assertion {
        val testId: Int
        val passes: Boolean
        val f: () => Any

        def apply(): Unit = try {
            val tt: Any = f()
            println(s"Test $testId ${if (passes) "passed as expected" else "UNEXPECTEDLY passed"} yielding: $tt")
        } catch {
            case t: Throwable =>
                println(s"Test $testId ${if (passes) "UNEXPECTEDLY failed" else "failed as expected"} with Exception: $t")
        }
    }

    case class Passes(override val f: () => Any) extends Assertion {
        override val testId: Int = aI.getAndIncrement()
        override val passes = true
        apply()
    }

    case class Fails(override val f: () => Any) extends Assertion {
        override val testId: Int = aI.getAndIncrement()
        override val passes = false
        apply()
    }

}