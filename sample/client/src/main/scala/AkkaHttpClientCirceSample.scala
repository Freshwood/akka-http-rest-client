import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import net.softler.client.ClientRequest

import scala.concurrent.{ExecutionContext, Future}

/**
  * Sample shows how to fetch json information in an very simple fashion
  */
object AkkaHttpClientCirceSample extends App with FailFastCirceSupport {

  implicit lazy val system: ActorSystem = ActorSystem()
  implicit lazy val materializer: Materializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContext = system.dispatcher

  case class GithubUser(login: String)

  val clientRequest: Future[GithubUser] = ClientRequest("https://api.github.com/users/Freshwood")
    .get[GithubUser]

  clientRequest.foreach(println)
}
