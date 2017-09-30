# akka-http-rest-client
A simple and ready to use akka http client for building HTTP requests and processing responses with custom response handlers

[![Build Status](https://travis-ci.org/Freshwood/akka-http-rest-client.svg?branch=master)](https://travis-ci.org/Freshwood/akka-http-rest-client)

akka-http-rest-client is based on the [Akka HTTP](https://github.com/akka/akka-http) architecture.
The goal is to have an easy to use REST client, which can be used in any Scala project
#### Features

- Easy using like Spring [Rest Templates](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)
- Builder style for a simple request building
- Almost all http methods available
- Default response processor support
- Functionality to un marshall responses with a given un marshaller (less code for you)

## Installation

(Coming soon)

``` scala
:)
```

## Usage

Simple as hell

``` scala
val result: Future[User] = ClientRequest("http://test.de/1").withJson.get[User]
```
## Contribution ##

Would be nice

## License ##

This code is open source software licensed under the MIC License