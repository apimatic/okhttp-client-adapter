# okhttp-client-adapter
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
    <version>{version}</version>
</dependency>
```

## Classes
| Name                                                                    | Description                                                        |
|-------------------------------------------------------------------------|--------------------------------------------------------------------|
| [`OkClient`](./src/main/java/io/apimatic/okhttpclient/adapter/OkClient.java)                     | HTTP Client class to send HTTP Requests and read the responses |
| [`HttpRedirectInterceptor`](./src/main/java/io/apimatic/okhttpclient/adapter/interceptors/HttpRedirectInterceptor.java)            | HttpRedirectInterceptor intercepts and complete 307 and 308 redirects as described in RFC                        |
| [`RetryInterceptor`](./src/main/java/io/apimatic/okhttpclient/adapter/interceptors/RetryInterceptor.java)             | RetryInterceptor intercepts and retry requests if failed based on configuration                |
