# okhttp-client-adapter

[![Maven Central][maven-badge]][maven-url]
[![Tests][test-badge]][test-url]
[![Licence][license-badge]][license-url]
## Introduction
This project contains OkHttp client adpater which is wrapper of Okhttp client implementation. This implementation is being provided to java core library from an APIMatic SDK.

## Prerequisites
* The JRE flavor requires `JDK 1.8`.
## Install the maven package
OKHttp Client adapter's Maven group ID is `io.apimatic`, and its artifact ID is `core-interfaces`.
To add a dependency on OkHttp client adapter using Maven, use the following:
```java
<dependency>
    <groupId>io.apimatic</groupId>
    <artifactId>okhttp-client-adapter</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Classes
| Name                                                                    | Description                                                        |
|-------------------------------------------------------------------------|--------------------------------------------------------------------|
| [`OkClient`](./src/main/java/io/apimatic/okhttpclient/adapter/OkClient.java)                     | HTTP Client class to send HTTP Requests and read the responses |
| [`HttpRedirectInterceptor`](./src/main/java/io/apimatic/okhttpclient/adapter/interceptors/HttpRedirectInterceptor.java)            | HttpRedirectInterceptor intercepts and complete 307 and 308 redirects as described in RFC                        |
| [`RetryInterceptor`](./src/main/java/io/apimatic/okhttpclient/adapter/interceptors/RetryInterceptor.java)             | RetryInterceptor intercepts and retry requests if failed based on configuration                |


[license-badge]: https://img.shields.io/badge/licence-APIMATIC-blue
[license-url]: LICENSE
[maven-badge]: https://img.shields.io/maven-central/v/io.apimatic/okhttp-client-adapter?color=green
[maven-url]: https://central.sonatype.dev/artifact/io.apimatic/okhttp-client-adapter/0.1.0
[test-badge]: https://github.com/apimatic/okhttp-client-adapter/actions/workflows/build-and-test.yml/badge.svg
[test-url]: https://github.com/apimatic/okhttp-client-adapter/actions/workflows/build-and-test.yml
