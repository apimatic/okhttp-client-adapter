package io.apimatic.okhttpclient.adapter.interceptors;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HttpRedirectInterceptor intercepts and complete 307 and 308 redirects as described in RFC.
 */
public class HttpRedirectInterceptor implements Interceptor {
    /**
     * Maximum follow ups.
     */
    private static final int MAX_FOLLOW_UPS = 20;

    /**
     * HTTP 307 Temporary Redirect redirect status response code indicates that the resource
     * requested has been temporarily moved to the URL given by the Location headers.
     */
    private static final int TEMPORARY_REDIRECT = 307;

    /**
     * Permanent Redirect redirect status response code indicates that the resource requested has
     * been definitively moved to the URL given by the Location headers.
     */
    private static final int PERMENANT_REDIRECT = 308;

    /**
     * boolean which directs to follow SSL Redirect.
     */
    private boolean followSslRedirects;

    /**
     * Initialization constructor.
     * @param isfollowSslRedirects boolean true if following ssl redirects
     */
    public HttpRedirectInterceptor(boolean isfollowSslRedirects) {
        this.followSslRedirects = isfollowSslRedirects;
    }

    /**
     * Intercepts and complete 307 and 308 redirects as described in RFC.
     * @see okhttp3.Interceptor#intercept(okhttp3.Interceptor.Chain)
     */
    @Override
    public Response intercept(Chain it) throws IOException {
        Request request = it.request();
        Response response = null;
        try {
            response = it.proceed(request);
        } catch (SocketTimeoutException ste) {
            response = it.proceed(request);
        }

        int followUpCount = 0;
        while (response != null && (response.code() == TEMPORARY_REDIRECT
                || response.code() == PERMENANT_REDIRECT)) {
            if (++followUpCount > MAX_FOLLOW_UPS) {
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }

            String location = response.header("Location");

            if (location == null) {
                return response;
            }

            HttpUrl followUpUrl = request.url().resolve(location);

            // If configured, don't follow redirects between SSL and non-SSL.
            boolean sameScheme = followUpUrl.scheme().equals(response.request().url().scheme());
            if (!sameScheme && !followSslRedirects) {
                return null;
            }

            Request.Builder followUpRequestBuilder = request.newBuilder().url(followUpUrl);

            // Clear out authorization header if different connection.
            if (!sameConnection(request.url(), followUpUrl)) {
                followUpRequestBuilder.removeHeader("Authorization");
            }

            if (response != null) {
                try {
                    response.close();
                } catch (RuntimeException rethrown) {
                    throw rethrown;
                } catch (Exception ignored) {
                    // Ignoring if its not runtime exception
                }
            }

            response = it.proceed(followUpRequestBuilder.build());
        }

        return response;
    }

    private static boolean sameConnection(HttpUrl a, HttpUrl b) {
        return a.host().equals(b.host()) && a.port() == b.port() && a.scheme().equals(b.scheme());
    }
}
