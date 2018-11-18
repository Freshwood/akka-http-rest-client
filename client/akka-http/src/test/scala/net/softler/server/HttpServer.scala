package net.softler.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.{Deflate, Gzip}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Methods`
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import net.softler.marshalling.Models.User
import net.softler.marshalling.{JsonSupport, Models}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait HttpServer extends JsonSupport with Models {

  def port: Int

  import akka.http.scaladsl.server.Directives._

  implicit val system: ActorSystem = ActorSystem("test-actor-system")

  implicit val materializer: Materializer = ActorMaterializer()

  implicit val executionContext: ExecutionContext = system.dispatcher

  lazy val routes: Route = path("test") {
    pathEndOrSingleSlash {
      get {
        complete(user)
      }
    }
  } ~ path("error") {
    get {
      complete(HttpResponse(StatusCodes.InternalServerError, entity = "Test Error"))
    }
  } ~ path("post") {
    post {
      entity(as[User]) { user =>
        complete(StatusCodes.Created -> user)
      }
    }
  } ~ path("all") {
    entity(as[User]) { u =>
      complete(u)
    }
  } ~ path("delete") {
    delete {
      complete(user)
    }
  } ~ path("head") {
    head {
      complete(StatusCodes.OK)
    }
  } ~ path("options") {
    options {
      complete(
        HttpResponse(200).withHeaders(
          `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE, HEAD, PATCH)
        ))
    }
  } ~ path("encoded") {
    encodeResponseWith(Gzip, Deflate) {
      complete("Hello World")
    }
  }

  Http().bindAndHandle(routes, "localhost", port) onComplete {
    case Success(_) => println(s"Test http service successfully bound to port $port")
    case Failure(_) => println(s"Test http service could not be bound to port $port")
  }
}
