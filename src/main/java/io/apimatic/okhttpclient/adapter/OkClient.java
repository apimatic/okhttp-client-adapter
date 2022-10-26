package io.apimatic.okhttpclient.adapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import io.apimatic.coreinterfaces.compatibility.CompatibilityFactory;
import io.apimatic.coreinterfaces.http.ClientConfiguration;
import io.apimatic.coreinterfaces.http.HttpClient;
import io.apimatic.coreinterfaces.http.HttpHeaders;
import io.apimatic.coreinterfaces.http.Method;
import io.apimatic.coreinterfaces.http.request.ArraySerializationFormat;
import io.apimatic.coreinterfaces.http.request.Multipart;
import io.apimatic.coreinterfaces.http.request.MultipartFile;
import io.apimatic.coreinterfaces.http.request.Request;
import io.apimatic.coreinterfaces.http.request.configuration.CoreEndpointConfiguration;
import io.apimatic.coreinterfaces.http.response.Response;
import io.apimatic.coreinterfaces.logger.ApiLogger;
import io.apimatic.coreinterfaces.type.CoreFileWrapper;
import io.apimatic.okhttpclient.adapter.interceptors.HttpRedirectInterceptor;
import io.apimatic.okhttpclient.adapter.interceptors.RetryInterceptor;

/**
 * HTTP Client class to send HTTP Requests and read the responses.
 */
public class OkClient implements HttpClient {
    /**
     * An object to avoid deadlock.
     */
    private static final Object SYNC_OBJECT = new Object();

    /**
     * A default okhttp3.OkHttpClient instance.
     */
    private static volatile okhttp3.OkHttpClient defaultOkHttpClient;

    /**
     * An instance for insecure okhttp3.OkHttpClient.
     */
    private static okhttp3.OkHttpClient insecureOkHttpClient;

    /**
     * Private instance of the okhttp3.OkHttpClient.
     */
    private okhttp3.OkHttpClient client;

    /**
     * Private instance of ApiLogger.
     */
    private ApiLogger apiLogger;

    /**
     * Private instance of CompatibilityFactory.
     */
    private static CompatibilityFactory compatibilityFactory;

    /**
     * Constructor to initialize the OKClient.
     * @param httpClientConfig client configuration
     * @param compatibilityFactoryImpl the compatibilityFactory for backward compatibility
     * @param httpLogger the logger for logging information
     */
    public OkClient(final ClientConfiguration httpClientConfig,
            final CompatibilityFactory compatibilityFactoryImpl, final ApiLogger httpLogger) {
        this(httpClientConfig, compatibilityFactoryImpl);
        this.apiLogger = httpLogger;
    }

    /**
     * Constructor to initialize the OKClient.
     * @param httpClientConfig the httpClientConfiguration
     * @param compatibilityFactoryImpl the compatibilityFactory for backward compatibility
     */
    public OkClient(final ClientConfiguration httpClientConfig,
            final CompatibilityFactory compatibilityFactoryImpl) {
        OkClient.compatibilityFactory = compatibilityFactoryImpl;
        okhttp3.OkHttpClient httpClientInstance = httpClientConfig.getHttpClientInstance();
        if (httpClientInstance != null) {
            if (httpClientConfig.shouldOverrideHttpClientConfigurations()) {
                applyHttpClientConfigurations(httpClientInstance, httpClientConfig);
            } else {
                this.client = httpClientInstance;
            }
        } else {
            if (httpClientConfig.skipSslCertVerification()) {
                applyHttpClientConfigurations(getInsecureOkHttpClient(httpClientConfig),
                        httpClientConfig);
            } else {
                applyHttpClientConfigurations(getDefaultOkHttpClient(), httpClientConfig);
            }
        }
    }

    /**
     * Applies the httpClientConfigurations on okhttp3.OkHttpClient.
     * @param okHttpClient a okhttp client instance
     * @param httpClientConfig a client configuration
     */
    private void applyHttpClientConfigurations(final okhttp3.OkHttpClient okHttpClient,
            final ClientConfiguration httpClientConfig) {
        okhttp3.OkHttpClient.Builder clientBuilder = okHttpClient.newBuilder();
        clientBuilder.readTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS)
                .writeTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS)
                .connectTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS);

        clientBuilder.addInterceptor(new HttpRedirectInterceptor(true));
        // If retries are allowed then RetryInterceptor must be registered
        if (httpClientConfig.getNumberOfRetries() > 0) {
            clientBuilder.callTimeout(httpClientConfig.getMaximumRetryWaitTime(), TimeUnit.SECONDS)
                    .addInterceptor(new RetryInterceptor(httpClientConfig, apiLogger));
        } else {
            clientBuilder.callTimeout(httpClientConfig.getTimeout(), TimeUnit.SECONDS);
        }

        this.client = clientBuilder.build();
    }

    /**
     * Getter for the default static instance of the okhttp3.OkHttpClient.
     * @param httpClientConfiguration the client configuration
     * @return {@link OkHttpClient}
     */
    private okhttp3.OkHttpClient getInsecureOkHttpClient(
            ClientConfiguration httpClientConfiguration) {
        if (insecureOkHttpClient == null) {
            synchronized (SYNC_OBJECT) {
                if (insecureOkHttpClient == null) {
                    insecureOkHttpClient = createInsecureOkHttpClient(httpClientConfiguration);
                }
            }
        }
        return insecureOkHttpClient;
    }

    private static okhttp3.OkHttpClient createInsecureOkHttpClient(
            final ClientConfiguration httpClientConfiguration) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                        throws CertificateException {}

                public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                        throws CertificateException {}

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return new okhttp3.OkHttpClient().newBuilder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        public boolean verify(final String hostname, final SSLSession session) {
                            return true;
                        }
                    }).retryOnConnectionFailure(true)
                    .callTimeout(httpClientConfiguration.getTimeout(), TimeUnit.SECONDS).build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Getter for the default static instance of the okhttp3.OkHttpClient.
     * @return {@link OkHttpClient}
     */
    private okhttp3.OkHttpClient getDefaultOkHttpClient() {
        if (defaultOkHttpClient == null) {
            synchronized (SYNC_OBJECT) {
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

        if (insecureOkHttpClient != null) {
            insecureOkHttpClient.dispatcher().executorService().shutdown();
            insecureOkHttpClient.connectionPool().evictAll();
        }
    }

    /**
     * Execute a given Request to get string/binary response back.
     * @param httpRequest The given Request to execute.
     * @param endpointConfiguration The endpointconfiguration for request.
     * @return CompletableFuture of HttpResponse after execution.
     */
    public CompletableFuture<Response> executeAsync(final Request httpRequest,
            final CoreEndpointConfiguration endpointConfiguration) {
        okhttp3.Request okHttpRequest =
                convertRequest(httpRequest, endpointConfiguration.getArraySerializationFormat());

        RetryInterceptor retryInterceptor = getRetryInterceptor();
        if (retryInterceptor != null) {
            retryInterceptor.addRequestEntry(okHttpRequest, endpointConfiguration, httpRequest);
        }

        final CompletableFuture<Response> callBack = new CompletableFuture<>();
        client.newCall(okHttpRequest).enqueue(new okhttp3.Callback() {

            public void onFailure(final okhttp3.Call call, final IOException e) {
                publishResponse(null, httpRequest, callBack, e,
                        endpointConfiguration.hasBinaryResponse());
            }

            public void onResponse(final okhttp3.Call call, final okhttp3.Response okHttpResponse) {
                publishResponse(okHttpResponse, httpRequest, callBack, null,
                        endpointConfiguration.hasBinaryResponse());
            }
        });

        return callBack;
    }

    /**
     * Execute a given Request to get string/binary response back.
     * @param httpRequest The given Request to execute.
     * @param endpointConfiguration The endpointConfiguration for request.
     * @return The converted http response.
     * @throws IOException exception to be thrown while converting response.
     */
    public Response execute(final Request httpRequest,
            final CoreEndpointConfiguration endpointConfiguration) throws IOException {
        okhttp3.Request okHttpRequest =
                convertRequest(httpRequest, endpointConfiguration.getArraySerializationFormat());

        RetryInterceptor retryInterceptor = getRetryInterceptor();
        if (retryInterceptor != null) {
            retryInterceptor.addRequestEntry(okHttpRequest, endpointConfiguration, httpRequest);
        }

        okhttp3.Response okHttpResponse = null;
        try {
            okHttpResponse = client.newCall(okHttpRequest).execute();
        } catch (IOException e) {
            // log response with error
            if (apiLogger != null) {
                apiLogger.setError(httpRequest, e);
                apiLogger.logResponse(httpRequest, null);
            }
            throw e;
        }

        Response httpResponse = null;
        try {
            httpResponse = convertResponse(httpRequest, okHttpResponse,
                    endpointConfiguration.hasBinaryResponse());
        } catch (IOException e) {
            if (apiLogger != null) {
                apiLogger.setError(httpRequest, e);
            }
        }

        if (apiLogger != null) {
            // log response
            apiLogger.logResponse(httpRequest, httpResponse);
        }
        return httpResponse;
    }

    /**
     * Returns RetryInterceptor instance registered with client.
     * @return The RetryInterceptor instance.
     */
    private RetryInterceptor getRetryInterceptor() {
        return (RetryInterceptor) this.client.interceptors().stream()
                .filter(interceptor -> interceptor instanceof RetryInterceptor).findFirst()
                .orElse(null);
    }

    /**
     * Publishes success or failure result as HttpResponse from a HttpRequest.
     * @param okHttpResponse The okhttp response to publish.
     * @param httpRequest The internal http request.
     * @param completionBlock The success and failure code block reference to invoke the delegate.
     * @param error The reported errors for getting the http response.
     * @param hasBinaryResponse Whether the response is binary or string.
     * @return The converted http response.
     */
    private Response publishResponse(final okhttp3.Response okHttpResponse,
            final Request httpRequest, final CompletableFuture<Response> completionBlock,
            final Throwable error, final boolean hasBinaryResponse) {
        Response httpResponse = null;
        try {
            if (error != null) {
                if (apiLogger != null) {
                    apiLogger.setError(httpRequest, error);
                }
            }
            httpResponse = convertResponse(httpRequest, okHttpResponse, hasBinaryResponse);

            // if there are no errors, pass on to the callback function
            if (error == null && httpResponse != null) {
                completionBlock.complete(httpResponse);
            } else {
                completionBlock.completeExceptionally(error);
            }
        } catch (IOException e) {
            if (apiLogger != null) {
                apiLogger.setError(httpRequest, e);
            }
            completionBlock.completeExceptionally(e);
        }
        if (apiLogger != null) {
            apiLogger.logResponse(httpRequest, httpResponse);
        }
        return httpResponse;
    }

    /**
     * Converts a given OkHttp response into our internal http response model.
     * @param request The given http request in internal format.
     * @param response The given OkHttp response.
     * @param hasBinaryResponse Whether the response is binary or string.
     * @return The converted http response.
     * @throws IOException exception to be thrown while converting response.
     */
    public static Response convertResponse(final Request request, final okhttp3.Response response,
            final boolean hasBinaryResponse) throws IOException {
        Response httpResponse = null;

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
     * @param httpRequest The given http request in internal format.
     * @param arraySerializationFormat
     * @return The converted okhttp request
     */
    private okhttp3.Request convertRequest(final Request httpRequest,
            final ArraySerializationFormat arraySerializationFormat) {
        okhttp3.RequestBody requestBody;

        if (httpRequest.getBody() != null) {

            // set request media type
            String contentType;
            Object body = httpRequest.getBody();

            // set request body
            if (body instanceof CoreFileWrapper) {
                CoreFileWrapper file = (CoreFileWrapper) body;

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
                    if ((param.getValue() instanceof MultipartFile)
                            || (param.getValue() instanceof Multipart)) {
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
            } else if (httpRequest.getHttpMethod().toString().equals(Method.GET.toString())) {
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

        // log request
        if (apiLogger != null) {
            apiLogger.logRequest(httpRequest, httpRequest.getUrl(arraySerializationFormat));
        }

        // build the request
        okhttp3.Request okHttpRequest = new okhttp3.Request.Builder()
                .method(httpRequest.getHttpMethod().toString(), requestBody)
                .headers(requestHeaders.build()).url(httpRequest.getUrl(arraySerializationFormat))
                .build();

        return okHttpRequest;
    }

    private okhttp3.RequestBody createMultipartRequestBody(Request httpRequest) {
        okhttp3.MultipartBody.Builder multipartBuilder =
                new okhttp3.MultipartBody.Builder().setType(okhttp3.MultipartBody.FORM);

        for (SimpleEntry<String, Object> param : httpRequest.getParameters()) {
            if (param.getValue() instanceof MultipartFile) {
                MultipartFile wrapperObj = (MultipartFile) param.getValue();
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
                HttpHeaders fileWrapperHeaders =
                        compatibilityFactory.createHttpHeaders(wrapperObj.getHeaders());
                fileWrapperHeaders.remove("content-type");
                okhttp3.Headers.Builder fileWrapperHeadersBuilder =
                        createRequestHeaders(fileWrapperHeaders);

                fileWrapperHeadersBuilder.add("Content-Disposition",
                        "form-data; name="
                                + appendQuotedStringAndEncodeEscapeCharacters(param.getKey())
                                + "; filename=" + appendQuotedStringAndEncodeEscapeCharacters(
                                        wrapperObj.getFileWrapper().getFile().getName()));
                multipartBuilder.addPart(fileWrapperHeadersBuilder.build(), body);
            } else if (param.getValue() instanceof Multipart) {
                Multipart wrapperObject = (Multipart) param.getValue();
                okhttp3.RequestBody body = okhttp3.RequestBody.create(wrapperObject.getByteArray(),
                        okhttp3.MediaType.parse(wrapperObject.getHeaders().value("content-type")));
                HttpHeaders wrapperHeaders =
                        compatibilityFactory.createHttpHeaders(wrapperObject.getHeaders());
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

    private static okhttp3.Headers.Builder createRequestHeaders(final HttpHeaders headers) {
        okhttp3.Headers.Builder requestHeaders = new okhttp3.Headers.Builder();
        for (Entry<String, List<String>> kv : headers.asMultimap().entrySet()) {
            for (String value : kv.getValue()) {
                requestHeaders.add(kv.getKey(), value);
            }
        }
        return requestHeaders;
    }

    private static String appendQuotedStringAndEncodeEscapeCharacters(final String key) {
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
