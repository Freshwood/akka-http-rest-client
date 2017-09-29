package net.softler.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
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
        complete(user)
      }
    }
  }

  val server: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", port)
}
