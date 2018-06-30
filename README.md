# akka-http-rest-client
A simple and ready to use akka http client for building HTTP requests and processing responses with custom response handlers

[![Build Status](https://travis-ci.org/Freshwood/akka-http-rest-client.svg?branch=master)](https://travis-ci.org/Freshwood/akka-http-rest-client)

akka-http-rest-client is based on the [Akka HTTP](https://github.com/akka/akka-http) architecture.
The goal is to have an easy to use REST client, which can be used in almost any Scala project
#### Features

- Easy using like Spring [Rest Templates](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
- Builder style for a simple request building
- Almost all http methods available
- Default response processor support
- Functionality to un marshall responses with a given un marshaller (less code for you)
- Development was focused on JSON support

## Installation

``` scala
// All releases are published to bintray
resolvers += Resolver.bintrayRepo("freshwood", "maven")

libraryDependencies ++= List(
  "net.softler" %% "akka-http-rest-client" % "0.1.1"
)
```

(ammonite)

```scala
interp.repositories() ++= Seq(coursier.MavenRepository("https://dl.bintray.com/freshwood/maven/"))

import $ivy.`net.softler::akka-http-rest-client:0.1.1`
```

## Usage

Simple as hell

``` scala

// When you have an unmarshaller available: (e.g: RootJsonFormat[User])
val result: Future[User] = ClientRequest("http://test.de/1").withJson.get[User]

// Or you just maka a call...
val result: Future[ClientResponse] = testRequest.get()

// Now you can process the response with the inline processor
// The future will fail when an error occurs
val processed: Future[ResponseEntity] = result map(_.process)

// Or you just handle the response by your own
val original: Future[HttpResponse] = result.map(_.response)

```

### Handle (Gzip, Deflate) responses

```scala
// Just a request on a API with GZIP responses
val result: Future[ClientResponse] = encodedRequest.withText.get()

// Just map over the entire response an encoder to get your desired object 
val string = result.map(_.decode).flatMap(_.as[String])

```

## Contribution ##

It would be a pleasure to see a nice contribution.
Just add a PR (Pull Request)
Or you add an issue with an corresponding PR.

## License ##

This code is open source software licensed under the MIT License
