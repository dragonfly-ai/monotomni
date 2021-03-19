package ai.dragonfly.monotomni

import java.util.concurrent.atomic.AtomicInteger

import ai.dragonfly.monotomni.TimeTrial.Formats
import ai.dragonfly.monotomni.connection.TimeServerConnection

import scala.util.{Failure, Success}

/**
  * Created by clifton on 11/24/14.
  */

object Demo extends App {

    println("Testing local Mono+Omni MOI generation:")
    for (i <- 0 until 10) {
        val moi:MOI = Mono+Omni()
        println(s"$i => $moi {${M0I(moi)}}")
    }

    println("Testing Mono+Omni TimeTrial parsers:")
    TimeTrial.test()
    TimeTrialJSONP.test()

    println("Testing TimeServer and TimeServerClient TimeTrials:")
    val uri1: java.net.URI = new java.net.URI("http://localhost:8080/time")
    println(s"testing on URL: $uri1")

    /* Default Connection */
    Remote(native.connection.Default(uri1)) onComplete {
        case Success(r:Remote) =>
            implicit val remote:Remote = r
            println(s"Approximate Monotonically Increasing Omni-Present Identifiers from:\n\t$remote")
            print(s"\t[${Remo+Ami()}")
            for (i <- 1 until 50) print(s", ${Remo+Ami()}")
            print("]")
        case Failure(t:Throwable) => println(s"Couldn't connect to $uri1")
    }

    val uri2:java.net.URI = new java.net.URI("http://127.0.0.1:8080/time")

    /* alternatively */
    implicit val r:Remote = new Remote(native.connection.Default(uri2, Formats.JSON))
    r.ready(() => {
        println(s"Approximate Monotonically Increasing Omni-Present Identifiers from:\n\t$r")
        print(s"\t[${Remo+Ami()}")
        for (i <- 1 until 50) print(s", ${Remo+Ami()}")
        print("]")
    })
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