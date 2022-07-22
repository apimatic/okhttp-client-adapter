/*
 * TesterLib
 *
 * This file was automatically generated for Stamplay by APIMATIC v3.0 ( https://www.apimatic.io ).
 */

package io.apimatic.okhttp_client_lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import io.apimatic.core_interfaces.compatibility.CompatibilityFactory;
import io.apimatic.core_interfaces.http.HttpClient;
import io.apimatic.core_interfaces.http.HttpClientConfiguration;
import io.apimatic.core_interfaces.http.HttpHeaders;
import io.apimatic.core_interfaces.http.HttpMethod;
import io.apimatic.core_interfaces.http.request.HttpRequest;
import io.apimatic.core_interfaces.http.request.MultipartFileWrapper;
import io.apimatic.core_interfaces.http.request.MultipartWrapper;
import io.apimatic.core_interfaces.http.request.configuration.RequestRetryConfiguration;
import io.apimatic.core_interfaces.http.response.HttpResponse;
import io.apimatic.core_interfaces.type.FileWrapper;
import io.apimatic.okhttp_client_lib.interceptors.HttpRedirectInterceptor;
import io.apimatic.okhttp_client_lib.interceptors.RetryInterceptor;

/**
 * HTTP Client class to send HTTP Requests and read the responses.
 */
public class OkClient implements HttpClient {
    private static final Object syncObject = new Object();
    private static volatile okhttp3.OkHttpClient defaultOkHttpClient;

    /**
     * Private instance of the okhttp3.OkHttpClient.
     */
    private okhttp3.OkHttpClient client;

    private CompatibilityFactory compatibilityFactory;

    /**
     * Default constructor.
     * 
     * @param httpClientConfig The specified http client configuration.
     */
    public OkClient(HttpClientConfiguration httpClientConfig,
            CompatibilityFactory compatibilityFactory) {
        this.compatibilityFactory = compatibilityFactory;
        okhttp3.OkHttpClient httpClientInstance = httpClientConfig.getHttpClientInstance();
        if (httpClientInstance != null) {
            if (httpClientConfig.shouldOverrideHttpClientConfigurations()) {
                applyHttpClientConfigurations(httpClientInstance, httpClientConfig);
            } else {
                this.client = httpClientInstance;
            }
        } else {
            applyHttpClientConfigurations(getDefaultOkHttpClient(), httpClientConfig);
        }
    }

    /**
     * Applies the httpClientConfigurations on okhttp3.OkHttpClient.
     */
    private void applyHttpClientConfigurations(okhttp3.OkHttpClient client,
            HttpClientConfiguration httpClientConfig) {
        okhttp3.OkHttpClient.Builder clientBuilder = client.newBuilder();
        clientBuilder.readTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS)
                .connectTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS);

        clientBuilder.addInterceptor(new HttpRedirectInterceptor(true));
        // If retries are allowed then RetryInterceptor must be registered
        if (httpClientConfig.getNumberOfRetries() > 0) {
            clientBuilder.callTimeout(httpClientConfig.getMaximumRetryWaitTime(), TimeUnit.SECONDS)
                    .addInterceptor(new RetryInterceptor(httpClientConfig));
        } else {
            clientBuilder.callTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS);
        }

        this.client = clientBuilder.build();
    }

    /**
     * Getter for the default static instance of the okhttp3.OkHttpClient.
     */
    private okhttp3.OkHttpClient getDefaultOkHttpClient() {
        if (defaultOkHttpClient == null) {
            synchronized (syncObject) {
                if (defaultOkHttpClient == null) {
                    defaultOkHttpClient =
                            new okhttp3.OkHttpClient.Builder().retryOnConnectionFailure(false)
                                    .callTimeout(0, TimeUnit.SECONDS).build();
                }
            }
        }
        return defaultOkHttpClient;
    }

    /**
     * Shutdown the underlying OkHttpClient instance.
     */
    public static void shutdown() {
        if (defaultOkHttpClient != null) {
            defaultOkHttpClient.dispatcher().executorService().shutdown();
            defaultOkHttpClient.connectionPool().evictAll();
        }
    }

    /**
     * Execute a given HttpRequest to get string/binary response back.
     * 
     * @param httpRequest The given HttpRequest to execute.
     * @param hasBinaryResponse Whether the response is binary or string.
     * @param retryConfiguration The overridden retry configuration for request.
     * @return CompletableFuture of HttpResponse after execution.
     */
    public CompletableFuture<HttpResponse> executeAsync(final HttpRequest httpRequest,
            boolean hasBinaryResponse, RequestRetryConfiguration requestRetryConfiguration) {
        okhttp3.Request okHttpRequest = convertRequest(httpRequest);

        RetryInterceptor retryInterceptor = getRetryInterceptor();
        if (retryInterceptor != null) {
            retryInterceptor.addRequestEntry(okHttpRequest, requestRetryConfiguration);
        }

        final CompletableFuture<HttpResponse> callBack = new CompletableFuture<>();
        client.newCall(okHttpRequest).enqueue(new okhttp3.Callback() {

            public void onFailure(okhttp3.Call call, IOException e) {
                publishResponse(null, httpRequest, callBack, e, hasBinaryResponse);
            }

            public void onResponse(okhttp3.Call call, okhttp3.Response okHttpResponse) {
                publishResponse(okHttpResponse, httpRequest, callBack, null, hasBinaryResponse);
            }
        });

        return callBack;
    }

    /**
     * Execute a given HttpRequest to get string/binary response back.
     * 
     * @param httpRequest The given HttpRequest to execute.
     * @param hasBinaryResponse Whether the response is binary or string.
     * @param retryConfiguration The overridden retry configuration for request.
     * @return The converted http response.
     * @throws IOException exception to be thrown while converting response.
     */
    public HttpResponse execute(HttpRequest httpRequest, boolean hasBinaryResponse,
            RequestRetryConfiguration requestRetryConfiguration) throws IOException {
        okhttp3.Request okHttpRequest = convertRequest(httpRequest);

        RetryInterceptor retryInterceptor = getRetryInterceptor();
        if (retryInterceptor != null) {
            retryInterceptor.addRequestEntry(okHttpRequest, requestRetryConfiguration);
        }

        okhttp3.Response okHttpResponse = null;
        okHttpResponse = client.newCall(okHttpRequest).execute();
        return convertResponse(httpRequest, okHttpResponse, hasBinaryResponse);
    }

    /**
     * Returns RetryInterceptor instance registered with client.
     * 
     * @return The RetryInterceptor instance.
     */
    private RetryInterceptor getRetryInterceptor() {
        return (RetryInterceptor) this.client.interceptors().stream()
                .filter(interceptor -> interceptor instanceof RetryInterceptor).findFirst()
                .orElse(null);
    }

    /**
     * Publishes success or failure result as HttpResponse from a HttpRequest.
     * 
     * @param okHttpResponse The okhttp response to publish.
     * @param httpRequest The internal http request.
     * @param completionBlock The success and failure code block reference to invoke the delegate.
     * @param error The reported errors for getting the http response.
     * @param hasBinaryResponse Whether the response is binary or string.
     * @return The converted http response.
     */
    private HttpResponse publishResponse(okhttp3.Response okHttpResponse, HttpRequest httpRequest,
            CompletableFuture<HttpResponse> completionBlock, Throwable error,
            boolean hasBinaryResponse) {
        HttpResponse httpResponse = null;
        try {
            httpResponse = convertResponse(httpRequest, okHttpResponse, hasBinaryResponse);

            // if there are no errors, pass on to the callback function
            if (error == null && httpResponse != null) {
                completionBlock.complete(httpResponse);
            } else {
                completionBlock.completeExceptionally(error);
            }
        } catch (IOException e) {
            completionBlock.completeExceptionally(e);
        }
        return httpResponse;
    }

    /**
     * Converts a given OkHttp response into our internal http response model.
     * 
     * @param request The given http request in internal format.
     * @param response The given OkHttp response.
     * @param hasBinaryResponse Whether the response is binary or string.
     * @return The converted http response.
     * @throws IOException exception to be thrown while converting response.
     */
    protected HttpResponse convertResponse(HttpRequest request, okhttp3.Response response,
            boolean hasBinaryResponse) throws IOException {
        HttpResponse httpResponse = null;

        if (response != null) {

            okhttp3.ResponseBody responseBody = response.body();

            HttpHeaders headers =
                    compatibilityFactory.createHttpHeaders(response.headers().toMultimap());

            if (hasBinaryResponse) {
                InputStream responseStream = responseBody.byteStream();
                httpResponse = compatibilityFactory.createHttpResponse(response.code(), headers,
                        responseStream);
            } else {
                String responseString = responseBody.string();
                InputStream responseStream = new ByteArrayInputStream(responseString.getBytes());
                httpResponse = compatibilityFactory.createHttpResponse(response.code(), headers,
                        responseStream, responseString);

                responseBody.close();
                response.close();
            }
        }

        return httpResponse;
    }

    /**
     * Converts a given internal http request into an okhttp request model.
     * 
     * @param httpRequest The given http request in internal format.
     * @return The converted okhttp request
     */
    private okhttp3.Request convertRequest(HttpRequest httpRequest) {
        okhttp3.RequestBody requestBody;

        if (httpRequest.getBody() != null) {

            // set request media type
            String contentType;
            Object body = httpRequest.getBody();

            // set request body
            if (body instanceof FileWrapper) {
                FileWrapper file = (FileWrapper) body;

                if (file.getContentType() != null && !file.getContentType().isEmpty()) {
                    contentType = file.getContentType();
                    httpRequest.getHeaders().add("content-type", contentType);
                } else if (httpRequest.getHeaders().has("content-type")) {
                    contentType = httpRequest.getHeaders().value("content-type");
                } else {
                    contentType = "application/octet-stream";
                    httpRequest.getHeaders().add("content-type", contentType);
                }

                requestBody = okhttp3.RequestBody.create(file.getFile(),
                        okhttp3.MediaType.parse(contentType));
            } else {
                // set request body
                if (!httpRequest.getHeaders().has("content-type")) {
                    httpRequest.getHeaders().add("content-type", "application/json; charset=UTF-8");
                }

                contentType = httpRequest.getHeaders().value("content-type");
                // set request body
                requestBody = okhttp3.RequestBody.create(((String) body).getBytes(),
                        okhttp3.MediaType.parse(contentType));
            }
        } else {

            List<SimpleEntry<String, Object>> parameters = httpRequest.getParameters();
            boolean multipartRequest = false;

            // set request fields
            if (parameters != null && parameters.size() > 0) {
                // check if a request is a multipart request
                for (SimpleEntry<String, Object> param : parameters) {
                    if ((param.getValue() instanceof MultipartFileWrapper)
                            || (param.getValue() instanceof MultipartWrapper)) {
                        multipartRequest = true;
                        break;
                    }
                }

                if (multipartRequest) {
                    // make a multipart request if a file is being sent
                    requestBody = createMultipartRequestBody(httpRequest);
                } else {
                    okhttp3.FormBody.Builder formBuilder = new okhttp3.FormBody.Builder();
                    for (SimpleEntry<String, Object> param : parameters) {
                        formBuilder.add(param.getKey(),
                                (param.getValue() == null) ? "" : param.getValue().toString());
                    }
                    requestBody = formBuilder.build();
                }
            } else if (httpRequest.getHttpMethod().equals(HttpMethod.GET)) {
                requestBody = null;
            } else {
                requestBody = okhttp3.RequestBody.create(new byte[0], null);
            }
        }

        // set request headers
        okhttp3.Headers.Builder requestHeaders = new okhttp3.Headers.Builder();
        if (httpRequest.getHeaders() != null) {
            requestHeaders = createRequestHeaders(httpRequest.getHeaders());
        }

        // build the request
        okhttp3.Request okHttpRequest = new okhttp3.Request.Builder()
                .method(httpRequest.getHttpMethod().toString(), requestBody)
                .headers(requestHeaders.build()).url(httpRequest.getUrl()).build();

        return okHttpRequest;
    }

    private okhttp3.RequestBody createMultipartRequestBody(HttpRequest httpRequest) {
        okhttp3.MultipartBody.Builder multipartBuilder =
                new okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM);

        for (SimpleEntry<String, Object> param : httpRequest.getParameters()) {
            if (param.getValue() instanceof MultipartFileWrapper) {
                MultipartFileWrapper wrapperObj = (MultipartFileWrapper) param.getValue();
                okhttp3.MediaType mediaType;
                if (wrapperObj.getFileWrapper().getContentType() != null
                        && !wrapperObj.getFileWrapper().getContentType().isEmpty()) {
                    mediaType =
                            okhttp3.MediaType.parse(wrapperObj.getFileWrapper().getContentType());
                } else {
                    mediaType =
                            okhttp3.MediaType.parse(wrapperObj.getHeaders().value("content-type"));
                }

                okhttp3.RequestBody body = okhttp3.RequestBody
                        .create(wrapperObj.getFileWrapper().getFile(), mediaType);
                HttpHeaders fileWrapperHeaders = compatibilityFactory.createHttpHeaders(wrapperObj.getHeaders());
                fileWrapperHeaders.remove("content-type");
                okhttp3.Headers.Builder fileWrapperHeadersBuilder =
                        createRequestHeaders(fileWrapperHeaders);

                fileWrapperHeadersBuilder.add("Content-Disposition",
                        "form-data; name="
                                + appendQuotedStringAndEncodeEscapeCharacters(param.getKey())
                                + "; filename=" + appendQuotedStringAndEncodeEscapeCharacters(
                                        wrapperObj.getFileWrapper().getFile().getName()));
                multipartBuilder.addPart(fileWrapperHeadersBuilder.build(), body);
            } else if (param.getValue() instanceof MultipartWrapper) {
                MultipartWrapper wrapperObject = (MultipartWrapper) param.getValue();
                okhttp3.RequestBody body = okhttp3.RequestBody.create(wrapperObject.getByteArray(),
                        okhttp3.MediaType.parse(wrapperObject.getHeaders().value("content-type")));
                HttpHeaders wrapperHeaders = compatibilityFactory.createHttpHeaders(wrapperObject.getHeaders());
                wrapperHeaders.remove("content-type");
                okhttp3.Headers.Builder wrapperHeadersBuilder =
                        createRequestHeaders(wrapperHeaders);

                wrapperHeadersBuilder.add("Content-Disposition", "form-data; name="
                        + appendQuotedStringAndEncodeEscapeCharacters(param.getKey()));
                multipartBuilder.addPart(wrapperHeadersBuilder.build(), body);
            } else {
                multipartBuilder.addFormDataPart(param.getKey(),
                        (param.getValue() == null) ? "" : param.getValue().toString());
            }
        }
        return multipartBuilder.build();
    }

    private static okhttp3.Headers.Builder createRequestHeaders(HttpHeaders headers) {
        okhttp3.Headers.Builder requestHeaders = new okhttp3.Headers.Builder();
        for (Entry<String, List<String>> kv : headers.asMultimap().entrySet()) {
            for (String value : kv.getValue()) {
                requestHeaders.add(kv.getKey(), value);
            }
        }
        return requestHeaders;
    }

    private static String appendQuotedStringAndEncodeEscapeCharacters(String key) {
        String target = "\"";
        for (int i = 0, len = key.length(); i < len; i++) {
            char ch = key.charAt(i);
            switch (ch) {
                case '\n':
                    target += "%0A";
                    break;
                case '\r':
                    target += "%0D";
                    break;
                case '"':
                    target += "%22";
                    break;
                default:
                    target += ch;
                    break;
            }
        }
        target += '"';
        return target;
    }

}
