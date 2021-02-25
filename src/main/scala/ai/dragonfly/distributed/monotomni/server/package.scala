package ai.dragonfly.distributed.monotomni

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor

package object server {
  println("Starting TimeServer.")
  implicit val system:ActorSystem = ActorSystem("ActorSystem")
  implicit val executionContext:ExecutionContextExecutor = system.dispatcher
}
