package net.softler

import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import net.softler.client.{ClientRequest, ClientResponse, RequestState}
import net.softler.exception.{ClientErrorRestException, ServerErrorRestException}
import net.softler.marshalling.Models.User
import net.softler.server.HttpServer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ClientApiTest
    extends FlatSpec
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with HttpServer {

  override def port = 9000

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(10 seconds)

  trait RequestBuilder {
    private val rawRequest = ClientRequest().withJson
    val testRequest: ClientRequest[RequestState.Idempotent] =
      rawRequest.uri("http://localhost:9000/test")

    val postRequest: ClientRequest[RequestState.EntityAcceptance] =
      rawRequest.uri("http://localhost:9000/post").entity(user.toJson.toString)

    val errorRequest: ClientRequest[RequestState.Idempotent] =
      rawRequest.uri("http://localhost:9000/error")

    val allRequest: ClientRequest[RequestState.Idempotent] =
      rawRequest.uri("http://localhost:9000/all")

    val delRequest: ClientRequest[RequestState.Idempotent] =
      rawRequest.uri("http://localhost:9000/delete")

    val headRequest: ClientRequest[RequestState.Idempotent] =
      rawRequest.uri("http://localhost:9000/head")

    val optionsRequest: ClientRequest[RequestState.Idempotent] =
      rawRequest.uri("http://localhost:9000/options")
  }

  "The client api" should "handle get requests" in new RequestBuilder {

    val result: Future[ClientResponse] = testRequest.get()

    val resultWithEntity: Future[User] = testRequest.get[User]

    val errorResult: Future[User] = errorRequest.get[User]

    whenReady(result) { r =>
      r.status shouldBe StatusCodes.OK
    }

    whenReady(resultWithEntity) { r =>
      r.id shouldBe user.id
    }

    whenReady(errorResult.failed) { e =>
      e shouldBe a[ServerErrorRestException]
    }
  }

  it should "handle post requests" in new RequestBuilder {
    val request: ClientRequest[RequestState.Idempotent] =
      ClientRequest().uri("http://localhost:9000/post").withJson

    val errorResult: Future[ClientResponse] = request.get()

    val postResult: Future[ClientResponse] = postRequest.asJson.post()

    val userResult: Future[User] = postRequest.asJson.post[User]

    whenReady(errorResult) { r =>
      r.status shouldBe StatusCodes.MethodNotAllowed
    }

    // Check processing
    whenReady(errorResult) { r =>
      intercept[ClientErrorRestException](r.process)
    }

    // Check success processing
    whenReady(postResult) { r =>
      whenReady(Unmarshal(r.raw).to[User]) { userResult =>
        userResult.id shouldBe user.id
      }
    }

    // Check marshalling itself
    whenReady(userResult) { u =>
      u.id shouldBe user.id
    }
  }

  it should "handle put requests" in new RequestBuilder {
    val result: Future[ClientResponse] = allRequest.entity(user.toJson.toString).asJson.put()
    val userResult: Future[User] = allRequest.entity(user.toJson.toString).asJson.put[User]

    whenReady(result) { r =>
      r.process shouldBe a[ResponseEntity]
    }

    whenReady(userResult) { u =>
      u.id shouldBe user.id
    }
  }

  it should "handle patch requests" in new RequestBuilder {
    val result: Future[ClientResponse] = allRequest.entity(user.toJson.toString).asJson.patch()
    val userResult: Future[User] = allRequest.entity(user.toJson.toString).asJson.patch[User]

    whenReady(result) { r =>
      r.process shouldBe a[ResponseEntity]
    }

    whenReady(userResult) { u =>
      u.id shouldBe user.id
    }
  }

  it should "handle delete requests" in new RequestBuilder {
    val result: Future[ClientResponse] = delRequest.delete()
    val userResult: Future[User] = delRequest.delete[User]

    whenReady(result) { r =>
      r.process shouldBe a[ResponseEntity]
    }

    whenReady(userResult) { u =>
      u.id shouldBe user.id
    }
  }

  it should "handle head requests" in new RequestBuilder {
    val result: Future[ClientResponse] = headRequest.head()

    whenReady(result) { r =>
      r.process shouldBe a[ResponseEntity]
    }
  }

  it should "handle options requests" in new RequestBuilder {
    val result: Future[ClientResponse] = optionsRequest.options()

    whenReady(result) { r =>
      r.process shouldBe a[ResponseEntity]
    }
  }

  override def afterAll(): Unit = {
    system.terminate()
    println("Triggered test actor system shutdown")
  }
}
