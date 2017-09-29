package net.softler.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import net.softler.processor.ResponseProcessor

import scala.annotation.implicitNotFound
import scala.collection._
import scala.concurrent.{ExecutionContext, Future}

/**
  * The evidences to build a [[ClientRequest]]
  */
sealed trait RequestState

object RequestState {

  sealed trait Idempotent extends RequestState

  sealed trait NotReady extends RequestState

  sealed trait EntityAcceptance extends RequestState

  @implicitNotFound("The request is not idempotent. you need for example: GET, DELETE ...")
  type RequestIsIdempotent[S] = S =:= RequestState.Idempotent

  @implicitNotFound("Request is already built")
  type RequestNotReady[S] = S =:= RequestState.NotReady

  @implicitNotFound("A HTTP entity is required for this request type")
  type RequestWithEntity[S] = S =:= RequestState.EntityAcceptance
}

/**
  * The akka http request data holder
  */
sealed trait AkkaHttpRequest {
  def request: HttpRequest
}

/**
  * The http methods implementation which can be mixed in
  */
sealed trait Methods[R <: RequestState] extends AkkaHttpRequest {

  import RequestState._

  private def getRequest: HttpRequest = request.copy(method = HttpMethods.GET)

  private def postRequest: HttpRequest = request.copy(method = HttpMethods.POST)

  def get()(implicit evidence: RequestIsIdempotent[R],
            system: ActorSystem,
            materializer: Materializer,
            executionContext: ExecutionContext): Future[ClientResponse] = ClientResponse(
    Http().singleRequest(getRequest)
  )

  def get[A](implicit evidence: RequestIsIdempotent[R],
             system: ActorSystem,
             materializer: Materializer,
             executionContext: ExecutionContext,
             um: Unmarshaller[ResponseEntity, A],
             processor: ResponseProcessor): Future[A] = ClientResponse.as[A](
    Http().singleRequest(getRequest)
  )

  def post()(implicit evidence: RequestWithEntity[R],
             system: ActorSystem,
             materializer: Materializer,
             executionContext: ExecutionContext): Future[ClientResponse] =
    ClientResponse(Http().singleRequest(postRequest))

  def post[A](implicit evidence: RequestWithEntity[R],
              system: ActorSystem,
              materializer: Materializer,
              executionContext: ExecutionContext,
              um: Unmarshaller[ResponseEntity, A],
              processor: ResponseProcessor): Future[A] = ClientResponse.as[A](
    Http().singleRequest(postRequest)
  )
}

/**
  * The request accept headers for easy adding accept headers
  */
sealed trait AcceptHeaders[R <: RequestState] extends AkkaHttpRequest {
  def withJson: ClientRequest[RequestState.Idempotent] =
    ClientRequest(request.addHeader(Accept(MediaTypes.`application/json`)))
}

/**
  * The entity support trait specifies requests with data changes
  */
sealed trait EntitySupport[R <: RequestState] extends AkkaHttpRequest {

  import RequestState._

  def entity(entity: RequestEntity): ClientRequest[RequestState.EntityAcceptance] =
    ClientRequest(request.copy(entity = entity))

  def entity(data: String): ClientRequest[RequestState.EntityAcceptance] =
    ClientRequest(request.copy(entity = data))

  /**
    * Set the content type as [[ContentTypes.`application/json`]]
    * Make sure we have an entity first otherwise it gets overwritten
    */
  def asJson(implicit ev: RequestWithEntity[R]): ClientRequest[RequestState.EntityAcceptance] =
    ClientRequest {
      request
        .copy(entity = request.entity.withContentType(ContentTypes.`application/json`))
        .addHeader(Accept(MediaRange(MediaTypes.`application/json`)))
    }
}

/**
  * The client request which can be easily built with the mixed in traits
  *
  * @param request The actual request state which can be fired via Methods trait
  * @tparam R Determines the builder state
  */
case class ClientRequest[R <: RequestState](request: HttpRequest)
    extends Methods[R]
    with AcceptHeaders[R]
    with EntitySupport[R] {

  import RequestState._

  def body[A](implicit um: Unmarshaller[RequestEntity, A], materializer: Materializer): Future[A] =
    Unmarshal(request.entity).to[A]

  def uri(uri: Uri): ClientRequest[RequestState.Idempotent] = ClientRequest(request.copy(uri = uri))

  def http2: ClientRequest[Idempotent] =
    ClientRequest(request.copy(protocol = HttpProtocols.`HTTP/2.0`))

  def headers(headers: immutable.Seq[HttpHeader]): ClientRequest[Idempotent] =
    ClientRequest(request.copy(headers = headers))
}

/**
  * The companion to easily create a [[ClientRequest]] object
  */
object ClientRequest {

  import RequestState._

  def apply(): ClientRequest[NotReady] = ClientRequest(HttpRequest())

  def apply(uri: Uri): ClientRequest[Idempotent] = ClientRequest(HttpRequest(uri = uri))
}
