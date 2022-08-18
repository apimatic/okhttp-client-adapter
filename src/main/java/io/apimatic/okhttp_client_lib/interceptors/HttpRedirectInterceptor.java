/*
 * TesterLib
 *
 * This file was automatically generated for Stamplay by APIMATIC v3.0 ( https://www.apimatic.io ).
 */

package io.apimatic.okhttp_client_lib.interceptors;

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
    private static final int MAX_FOLLOW_UPS = 20;
    private boolean followSslRedirects;

    /**
     * Initialization constructor.
     * 
     * @param followSslRedirects boolean true if following ssl redirects
     */
    public HttpRedirectInterceptor(boolean followSslRedirects) {
        this.followSslRedirects = followSslRedirects;
    }

    /**
     * Intercepts and complete 307 and 308 redirects as described in RFC.
     * 
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
        while (response != null && (response.code() == 307 || response.code() == 308)) {
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