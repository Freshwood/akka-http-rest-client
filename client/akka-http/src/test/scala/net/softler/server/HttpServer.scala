package net.softler.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.`Access-Control-Allow-Methods`
import akka.http.scaladsl.model.{ContentTypes, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import net.softler.marshalling.Models.User
import net.softler.marshalling.{JsonSupport, Models}

import scala.concurrent.{ExecutionContextExecutor, Future}

trait HttpServer extends JsonSupport with Models {

  def port: Int

  import akka.http.scaladsl.server.Directives._

  implicit val system: ActorSystem = ActorSystem("test-actor-system")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

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
  }

  val server: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", port)
}
