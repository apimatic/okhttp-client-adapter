# okhttp-client-adapter

[![Maven Central][maven-badge]][maven-url]
[![Tests][test-badge]][test-url]
[![Lint Code][lint-badge]][lint-url]
[![Test Coverage][test-coverage-url]][code-climate-url]
[![Licence][license-badge]][license-url]
## Introduction
This project contains OkHttp client adpater which is wrapper of Okhttp client implementation. This implementation is being provided to java core library from an APIMatic SDK.

## Prerequisites
* The JRE flavor requires `JDK 1.8`.
## Install the maven package
OKHttp Client adapter's Maven group ID is `io.apimatic`, and its artifact ID is `core-interfaces`.


## Classes
| Name                                                                    | Description                                                        |
|-------------------------------------------------------------------------|--------------------------------------------------------------------|
| [`OkClient`](./src/main/java/io/apimatic/okhttpclient/adapter/OkClient.java)                     | HTTP Client class to send HTTP Requests and read the responses |
| [`HttpRedirectInterceptor`](./src/main/java/io/apimatic/okhttpclient/adapter/interceptors/HttpRedirectInterceptor.java)            | HttpRedirectInterceptor intercepts and complete 307 and 308 redirects as described in RFC                        |
| [`RetryInterceptor`](./src/main/java/io/apimatic/okhttpclient/adapter/interceptors/RetryInterceptor.java)             | RetryInterceptor intercepts and retry requests if failed based on configuration                |


[license-badge]: https://img.shields.io/badge/licence-MIT-blue
[license-url]: LICENSE
[maven-badge]: https://img.shields.io/maven-central/v/io.apimatic/okhttp-client-adapter?color=green
[maven-url]: https://central.sonatype.com/artifact/io.apimatic/okhttp-client-adapter
[test-badge]: https://github.com/apimatic/okhttp-client-adapter/actions/workflows/build-and-test.yml/badge.svg
[test-url]: https://github.com/apimatic/okhttp-client-adapter/actions/workflows/build-and-test.yml
[code-climate-url]: https://codeclimate.com/github/apimatic/okhttp-client-adapter
[maintainability-url]: https://api.codeclimate.com/v1/badges/0ab44ce56382cc0ee640/maintainability
[test-coverage-url]: https://api.codeclimate.com/v1/badges/0ab44ce56382cc0ee640/test_coverage
[lint-badge]: https://github.com/apimatic/okhttp-client-adapter/actions/workflows/linter.yml/badge.svg
[lint-url]: https://github.com/apimatic/okhttp-client-adapter/actions/workflows/linter.yml
