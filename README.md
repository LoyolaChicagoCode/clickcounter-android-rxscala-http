<!---
[![Build Status](https://travis-ci.org/LoyolaChicagoCode/clickcounter-android-rxscala.svg?branch=master)](https://travis-ci.org/LoyolaChicagoCode/clickcounter-android-rxscala) 
[![Coverage Status](https://img.shields.io/coveralls/LoyolaChicagoCode/clickcounter-android-rxscala.svg)](https://coveralls.io/r/LoyolaChicagoCode/clickcounter-android-rxscala) 
[![Download](https://api.bintray.com/packages/loyolachicagocode/generic/clickcounter-android-rxscala/images/download.svg) ](https://bintray.com/loyolachicagocode/generic/clickcounter-android-rxscala/_latestVersion)
-->

# Android Client for the RESTful Click Counter Service

This is an Android client app for the
[RESTful click counter service](https://github.com/LoyolaChicagoCode/clickcounter-spray-scala).
Together, the client and the service form a very simple
collaborative distributed/mobile application.

# Learning Objectives

This example is intended as a starting point for anyone planning
develop reactive, asynchronous HTTP-based Android client applications
using RxScala. Its learning objectives are:

- Android application development using Scala
    - Using the Simple Build Tool (sbt) for Scala in conjunction with
      [pfn's well-maintained plugin](https://github.com/pfn/android-sdk-plugin)
    - Using IntelliJ IDEA (optional)
	- HTTP client functionality using
	  [HttpURLConnection](http://developer.android.com/reference/java/net/HttpURLConnection.html)
	- [Scala futures](http://doc.akka.io/docs/akka/snapshot/scala/futures.html)
	  for background activities
	- Parsing JSON responses using
	  [org.json](http://developer.android.com/reference/org/json/package-summary.html)
	  included with Android
- Android application architecture for testability and maintainability
    - [Dependency Inversion Principle (DIP)](http://en.wikipedia.org/wiki/Dependency_inversion_principle)
    - [Model-View-Adapter](http://en.wikipedia.org/wiki/Model-view-adapter) architectural pattern
    - Separation of Android activity into event-handling and lifecycle management
    - Separation of stateful and reactive components using [RxScala](http://rxscala.github.io)
- Effective testing
    - Unit testing and [Behavior-Driven Development (BDD)](http://en.wikipedia.org/wiki/Behavior-driven_development) 
      with ScalaTest
    - [Mock objects](http://en.wikipedia.org/wiki/Mock_object) with [ScalaMock](http://scalamock.org/)
    - Functional testing (out-of-container) using [Robolectric](http://robolectric.org/)
- End-to-end example of continuous integration (CI) for Scala/Android (see status badges at the top of this file)

# Discussion

## HTTP client

The choice of
[HttpURLConnection](http://developer.android.com/reference/java/net/HttpURLConnection.html)
as the HTTP client needs some justification.

- The natural match for [RxScala](http://rxscala.github.io)
  would have been [RxApacheHttp](https://github.com/ReactiveX/RxApacheHttp).
  This, however, depends on a different version of the Apache HttpClient than
  [the one included in Android](https://developer.android.com/reference/org/apache/http/client/HttpClient.html),
  leading to all sorts of problems.
- The popular third-party clients
  (Loopj's [AsyncHttpClient](http://loopj.com/android-async-http),
  [OkHttp](http://square.github.io/okhttp), and others)
  do not appear to support
  [server-sent events (SSE)](http://en.wikipedia.org/wiki/Server-sent_events)
  anyway.
- [Akka SSE](https://github.com/hseeberger/akka-sse) works nicely in principle,
  but getting it to run on Android still requires quite a bit of work on the
  Proguard configuration.

Therefore, we decided to develop a home-grown SSE client based on
HttpURLConnection for subscribing to a stream of current counter values
coming from the server. Using HttpURLConnection for a fire-and-forget
`POST` for changing the counter value is really simple already.

## Availability of public service instance

The public service instance on [Heroku](http://laufer-clickcounter.herokuapp.com/)
tends to go above the Redis Labs connection limit and will stop working
temporarily. You are encouraged to host your own service instance.

# Building and Running

Please refer to [these notes](https://github.com/LoyolaChicagoBooks/lucoodevcourse/blob/master/source/scalaandroiddev.rst) for details.

# References

- [RxJava for Android](http://code.hootsuite.com/observing-observables-in-mobile-rxjava-for-android)
- [How to parse JSON in Android](http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android)
- [James Earl Douglas's SBTB 2014 presentation](https://www.youtube.com/watch?v=sZYAFWTyOlE)
- [James Earl Douglas's Scala CI example](https://github.com/earldouglas/scala-ci): Travis and Coveralls
- [Matthew Fellows's Scala CI/CD example](http://www.onegeek.com.au/scala/setting-up-travis-ci-for-scala): Travis and Bintray
