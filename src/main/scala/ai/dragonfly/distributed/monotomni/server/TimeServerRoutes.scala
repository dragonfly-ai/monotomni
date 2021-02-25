package ai.dragonfly.distributed.monotomni.server

import ai.dragonfly.distributed.monotomni.TimeTrial
import akka.http.scaladsl.server.PathMatcher._
import akka.http.scaladsl.server.PathMatchers._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpCharsets, HttpEntity, MediaType}
import akka.http.scaladsl.server.Directives.{get, pathPrefix, respondWithHeaders}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete


object TimeServerRoutes {
  lazy val routes: Route = {
    val now:Long = System.currentTimeMillis()
    get {
      respondWithHeaders(RawHeader("Cache-Control", "no-store")) {
        pathPrefix("time" / Segment / IntNumber / IntNumber) {
          (format:String, sid: Int, trial: Int) =>
            val timeTrial = TimeTrial(sid, trial, now)
            format match {
              case "BINARY" => complete(
                HttpEntity(
                  ContentTypes.`application/octet-stream`,
                  timeTrial.BINARY
                )
              )
              case "STRING" => complete(timeTrial.STRING)
              case "JSON" => complete(timeTrial.JSON)
              case "JSONP" => complete(
                HttpEntity(
                  ContentType(MediaType.applicationWithFixedCharset("javascript", HttpCharsets.`UTF-8`, "js", "javascript")),
                  timeTrial.JSONP
                )
              )
              // Assume XML for any other string?
              case _ => complete(timeTrial.XML)

            }
        }
      }
    }
  }
}
