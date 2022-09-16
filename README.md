# okhttp-client-lib
This project contains OkHttp client implementation. This implementation is being provided to java core library from an APIMatic SDK.

## Classes
| Name                                                                    | Description                                                        |
|-------------------------------------------------------------------------|--------------------------------------------------------------------|
| [`OkClient`](./src/main/java/io/apimatic/okhttp_client_lib/OkClient.java)                     | HTTP Client class to send HTTP Requests and read the responses |
| [`HttpRedirectInterceptor`](./src/main/java/io/apimatic/okhttp_client_lib/interceptors/HttpRedirectInterceptor.java)            | HttpRedirectInterceptor intercepts and complete 307 and 308 redirects as described in RFC                        |
| [`RetryInterceptor`](./src/main/java/io/apimatic/okhttp_client_lib/interceptors/RetryInterceptor.java)             | RetryInterceptor intercepts and retry requests if failed based on configuration                |
