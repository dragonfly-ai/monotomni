package ai.dragonfly.monotomni

import java.util.{Timer, TimerTask}
import java.util.concurrent.atomic.AtomicInteger

import ai.dragonfly.monotomni.TimeTrial.Formats
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
  * Created by clifton on 11/24/14.
  */

object Demo extends App {

    LoggerConfig.factory = PrintLoggerFactory()
    LoggerConfig.level = LogLevel.DEBUG

    // completion promises
    val localPromise:Promise[Boolean] = Promise[Boolean]()
    val r1Promise:Promise[Boolean] = Promise[Boolean]()
    val r2Promise:Promise[Boolean] = Promise[Boolean]()

    Future.traverse(
        Seq[Promise[Boolean]](localPromise, r1Promise, r2Promise)
    ) {
        pb => (for { fb <- pb.future } yield {
            fb
        }).recover { case t:Throwable => true }
    } onComplete {
        println("SHUTTING DOWN ALL PROCESSES!")
        native.exit(0)
    }

    def repeat(f:Boolean => Boolean, countTo:Int):Unit = {
        var count:Int = 0
        scheduleRandomDelay(() => {
            count = count + 1
            f(count >= countTo)
        })
    }

    println(s"Running Demo Application\n${Mono+Omni}\n\n")

    TimeTrial.test()
    TimeTrialJSONP.test()

    println(s"Generating 10 randomly delayed MOI values ...")
    repeat(
        f = (stop:Boolean) => {
            val moi = Mono+Omni()
            println(s"Mono+Omni() => $moi ~ ${M0I(moi)}")
            if (stop) {
                localPromise.success(true)
                println(s"... no longer Generating randomly delayed MOI values.")
            }
            stop
        },
        countTo = 10
    )

    println("Testing TimeServer and TimeServerClient TimeTrials:")
    val uri1: java.net.URI = new java.net.URI("http://localhost:8080/time")
    println(s"testing on URL: $uri1")

    /* Default Connection */
    RemoteClock(native.connection.Default(uri1)) onComplete {
        case Success(rc:RemoteClock) =>
            implicit val remoteClock:RemoteClock = rc
            println(s"Generating 10 randomly delayed AMI values from $remoteClock ...")
            repeat(
                f = (stop:Boolean) => {
                    val ami:AMI = remoteClock.ami()
                    println(s"$remoteClock.ami() => $ami ~ ${AM1(ami)}")
                    if (stop) {
                        remoteClock.stop()
                        println(s"... no longer Generating randomly delayed AMI values from $remoteClock.")
                        r1Promise.success(true)
                    }
                    stop
                },
                countTo = 10
            )
        case Failure(t:Throwable) => println(s"Couldn't connect to $uri1")
    }

    val uri2:java.net.URI = new java.net.URI("http://127.0.0.1:8080/time")

    /* alternatively */
    implicit val remoteClock:RemoteClock = new RemoteClock(native.connection.Default(uri2, Formats.JSON))
    remoteClock.ready(() => {
        println(s"Generating 10 randomly delayed AMI values from $remoteClock ...")
        repeat(
            f = (stop:Boolean) => {
                val ami:AMI = remoteClock.ami()
                println(s"$remoteClock.ami() => $ami ~ ${AM1(ami)}")
                if (stop) {
                    remoteClock.stop()
                    println(s"... no longer Generating randomly delayed AMI values from $remoteClock.")
                    r2Promise.success(true)
                }
                stop
            },
            countTo = 10
        )
    })

    def scheduleRandomDelay(f:() => Boolean):Unit = {
        new Timer(s"Timer(moi = ${Mono+Omni()})").schedule(
            new TimerTask() {
                override def run():Unit = {
                    if (!f()) scheduleRandomDelay(f)
                }
            },
            (Math.random() * 3000).toLong
        )
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