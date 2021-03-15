package ai.dragonfly.monotomni.server

import java.io.File

import ai.dragonfly.monotomni.{TimeTrial, TimeTrialJSONP}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpCharsets, HttpEntity, MediaType}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete


object TimeServerRoutes {
  lazy val routes: Route = {
    get {
      val now:Long = System.currentTimeMillis()
      respondWithHeaders(
        RawHeader("Cache-Control", "no-store"),
        RawHeader("Keep-Alive", "timeout=90")
      ) {
        pathPrefix("time" / "JSONP" / LongNumber ) {
          (pendingTrialId: Long) =>
            val timeTrialJSONP = TimeTrialJSONP(pendingTrialId,TimeTrial(now))
            complete(
              HttpEntity(
                ContentType(
                  MediaType.applicationWithFixedCharset(
                    "javascript", HttpCharsets.`UTF-8`, "js", "javascript"
                  )
                ),
                timeTrialJSONP.JSONP
              )
            )
        } ~ pathPrefix("time" / Segment ) {
          (format:String) =>
            val timeTrial = TimeTrial(now)
            format match {
              case "BINARY" => complete( HttpEntity( ContentTypes.`application/octet-stream`, timeTrial.BINARY ) )
              case "STRING" => complete(timeTrial.STRING)
              case "JSON" => complete(timeTrial.JSON)
              case _ => complete(timeTrial.XML) // Assume XML for any other string?
            }
        } ~ pathPrefix("demo" ) {
          getFromDirectory("./public_html/")
        }
      }
    }
  }
}
