---
layout: page
title: Akka http rest client
tagline: A simple to use akka http based rest client
description: A simple to use akka http based rest client
---

# Akka http rest client

## Motivation

The motivation for this project is to provide a easy to use rest client for scala.

## Installation

``` scala
// All releases are published to bintray
resolvers += Resolver.bintrayRepo("freshwood", "maven")

libraryDependencies ++= List(
  "net.softler" %% "akka-http-rest-client" % "0.2.1"
)
```

## Usage

To interact with http related services you need the following things:

* akka http specific context
* a request
* a processor
* fetching the response

First of all you need a ready to use akka http context.

For example: 

``` scala
trait AkkaHttpContext {
  implicit lazy val system: ActorSystem = ActorSystem()
  implicit lazy val materializer: Materializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContext = system.dispatcher
}
```

Now, you can create a request:

``` scala
val request: ClientRequest[RequestState.Idempotent] = ClientRequest(
    "https://github.com/Freshwood/akka-http-rest-client/blob/master/README.md")
```

You can use this request as many time you want. It will not be fired until you are processing this request.

Fire the request:

``` scala
val response: Future[ClientResponse] = request.get()
```

Processing the client response

``` scala
response flatMap (_.as[String]) foreach println
```

There will be used a default processor under the hood.
The default processor will fail on every non success result. (status != 2xx)

Anyway you can overwrite this processor to use your custom processor for response handling.

You can give an unmarshaller to an client response. In this example we are using **String**, because it works on every response. (See below how to use a custom unmarshaller)

The above code will be handled when the underlying response was successful.

## Full example with a json model (circe)

Prerequisite:

* Reference this project
* Reference akka http circe support

``` scala
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
```

## Examples

You can find the full examples here:
- [Samples](https://github.com/Freshwood/akka-http-rest-client/tree/master/sample/client/src/main/scala)