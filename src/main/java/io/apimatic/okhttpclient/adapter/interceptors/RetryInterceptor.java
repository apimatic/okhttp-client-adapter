package io.apimatic.okhttpclient.adapter.interceptors;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import io.apimatic.coreinterfaces.http.ClientConfiguration;
import io.apimatic.coreinterfaces.http.HttpMethodType;
import io.apimatic.coreinterfaces.http.request.Request;
import io.apimatic.coreinterfaces.http.request.configuration.CoreEndpointConfiguration;
import io.apimatic.coreinterfaces.http.response.Response;
import io.apimatic.coreinterfaces.logger.ApiLogger;
import io.apimatic.okhttpclient.adapter.OkClient;
import okhttp3.Interceptor;

/**
 * RetryInterceptor intercepts and retry requests if failed based on configuration.
 */
public class RetryInterceptor implements Interceptor {

    /**
     * Maximum Back off interval.
     */
    private static final int RANDOM_NUMBER_MULTIPLIER = 100;

    /**
     * Maximum Retry interval.
     */
    private static final int TO_MILLISECOND_MULTIPLIER = 1000;


    /**
     * RFC Date Time Formatter.
     */
    private static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z").withZone(ZoneId.of("GMT"));

    /**
     * To keep track of requests being sent and its current state.
     */
    private final ConcurrentMap<okhttp3.Request, RequestState> requestEntries;

    /**
     * User specified retry configurations.
     */
    private final ClientConfiguration httpClientConfiguration;

    /**
     * Private instance of HttpLogger.
     */
    private final ApiLogger httpLogger;

    /**
     * Default Constructor, Initializes the httpClientConfiguration attribute.
     * @param httpClientConfig the user specified configurations.
     * @param httpApiLogger for logging request and response.
     */
    public RetryInterceptor(final ClientConfiguration httpClientConfig,
            final ApiLogger httpApiLogger) {
        this.httpLogger = httpApiLogger;
        this.httpClientConfiguration = httpClientConfig;
        requestEntries = new ConcurrentHashMap<>();
    }

    /**
     * Intercepts and retry requests if failed based on configuration.
     * @see okhttp3.Interceptor#intercept(okhttp3.Interceptor.Chain)
     */
    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {

        okhttp3.Request request = chain.request();
        RequestState requestState = getRequestState(request);
        boolean isWhitelistedRequestMethod = this.httpClientConfiguration.getHttpMethodsToRetry()
                .contains(HttpMethodType.valueOf(request.method()));
        boolean isRetryAllowedForRequest = requestState.endpointConfiguration.getRetryOption()
                .isRetryAllowed(isWhitelistedRequestMethod);
        okhttp3.Response response = null;
        IOException timeoutException = null;
        boolean shouldRetry = false;

        do {
            try {
                response = getResponse(chain, request, response, true);
                timeoutException = null;
            } catch (IOException ioException) {
                timeoutException = ioException;
                response = null;
                if (!httpClientConfiguration.shouldRetryOnTimeout()) {
                    break;
                }
            }

            shouldRetry = isRetryAllowedForRequest
                    && needToRetry(requestState, response, timeoutException != null);

            if (shouldRetry) {

                // Performing wait time calculation.
                calculateWaitTime(requestState, response);

                // Checking total wait time against allowed max back-off time
                if (hasWaitTimeLimitExceeded(requestState)) {
                    break;
                }

                if (response == null) {

                    logError(requestState, timeoutException);
                }
                // Waiting before making next request
                holdExecution(requestState.currentWaitInMilliSeconds);

                // Incrementing retry attempt count
                requestState.retryCount++;

                if (httpLogger != null) {
                    httpLogger.logRequest(requestState.httpRequest, request.url().toString(),
                            "Retry Attempt # " + requestState.retryCount);
                }
            }

        } while (shouldRetry);

        this.requestEntries.remove(request);

        if (timeoutException != null) {
            throw timeoutException;
        }

        return response;
    }

    /**
     * Get the response Recursively since we have to handle the SocketException gracefully.
     * @param chain the interceptor chain.
     * @param request the HTTP request.
     * @param response the HTTP response.
     * @param shouldCloseResponse whether to close the response or not.
     * @return the HTTP response.
     * @throws IOException exception to be thrown in case of timeout.
     */
    private okhttp3.Response getResponse(Chain chain, okhttp3.Request request,
            okhttp3.Response response, boolean shouldCloseResponse) throws IOException {

        try {
            if (shouldCloseResponse && response != null) {
                response.close();
            }
            return chain.proceed(request);
        } catch (SocketException socketException) {
            return getResponse(chain, request, response, false);
        } catch (IOException exception) {
            throw exception;
        }

    }

    /**
     * Checks if the retry request is to be made against provided response.
     * @param requestState The current state of request entry.
     * @param response The HTTP response.
     * @param isTimeoutException We are retrying because of timeout or not
     * @return true If request is needed to be retried.
     */
    private boolean needToRetry(RequestState requestState, okhttp3.Response response,
            boolean isTimeoutException) {
        boolean isValidAttempt =
                requestState.retryCount < this.httpClientConfiguration.getNumberOfRetries();
        boolean isValidResponseToRetry =
                response != null && (this.httpClientConfiguration.getHttpStatusCodesToRetry()
                        .contains(response.code()) || hasRetryAfterHeader(response));
        return isValidAttempt && (isTimeoutException || isValidResponseToRetry);
    }

    /**
     * Checks if the overall wait time has reached to its limit.
     * @param requestState the current state of request entry.
     * @return true if total wait time exceeds maximum back-off time.
     */
    private boolean hasWaitTimeLimitExceeded(RequestState requestState) {
        return this.httpClientConfiguration.getMaximumRetryWaitTime() > 0
                && toMilliseconds(this.httpClientConfiguration
                        .getMaximumRetryWaitTime()) < requestState.totalWaitTimeInMilliSeconds;
    }

    /**
     * Calculates the wait time for next request.
     * @param requestState The current state of request entry.
     * @param response The HTTP response.
     */
    private void calculateWaitTime(RequestState requestState, okhttp3.Response response) {
        long retryAfterHeaderValue = 0;
        if (response != null && hasRetryAfterHeader(response)) {
            retryAfterHeaderValue = getCalculatedHeaderValue(response.header("Retry-After"));
        }
        long calculatedBackOffInMilliSeconds = getCalculatedBackOffValue(requestState);
        requestState.currentWaitInMilliSeconds =
                Math.max(retryAfterHeaderValue, calculatedBackOffInMilliSeconds);
        requestState.totalWaitTimeInMilliSeconds += requestState.currentWaitInMilliSeconds;
    }

    /**
     * Checks if the response contains Retry-After header.
     * @param response The HTTP response.
     * @return true If response contains Retry-After header.
     */
    private boolean hasRetryAfterHeader(okhttp3.Response response) {
        String retryAfter = response.header("Retry-After");
        return retryAfter != null && !retryAfter.isEmpty();
    }

    /**
     * Analyzes the header value and checks the header if it contains date in proper format or
     * seconds. If header value is date then it calculates the delta time in milliseconds.
     * @param headerValue The retry-after header value.
     * @return long value of calculated wait time in milliseconds.
     */
    private long getCalculatedHeaderValue(String headerValue) {
        try {
            return toMilliseconds(Long.parseLong(headerValue));
        } catch (NumberFormatException nfe) {
            long requestAtValueInSeconds = LocalDateTime
                    .parse(headerValue, RFC1123_DATE_TIME_FORMATTER).toEpochSecond(ZoneOffset.UTC);
            long currentDateTimeInSeconds =
                    LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC);
            return toMilliseconds(requestAtValueInSeconds - currentDateTimeInSeconds);
        }
    }

    /**
     * Calculates the back-off value based on a formula which uses back-off factor and retry Count.
     * @param requestState The current state of request entry.
     * @return long value of back-off time based on formula in milliseconds.
     */
    private long getCalculatedBackOffValue(RequestState requestState) {
        return (long) (TO_MILLISECOND_MULTIPLIER * this.httpClientConfiguration.getRetryInterval()
                * Math.pow(this.httpClientConfiguration.getBackOffFactor(), requestState.retryCount)
                + Math.random() * RANDOM_NUMBER_MULTIPLIER);
    }

    /**
     * Holds the execution for stored wait time in milliseconds of this thread.
     * @param milliSeconds The wait time in milli seconds.
     */
    private void holdExecution(long milliSeconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliSeconds);
        } catch (InterruptedException e) {
            // No handler needed
        }
    }

    /**
     * Converts the seconds to milliseconds.
     * @param seconds The seconds to convert.
     * @return long value of milliseconds.
     */
    private long toMilliseconds(long seconds) {
        return seconds * TO_MILLISECOND_MULTIPLIER;
    }

    /**
     * Adds entry into Request entry map.
     * @param okHttpRequest The OK HTTP Request.
     * @param endpointConfiguration The overridden endpointConfiguration for request.
     * @param request The core interface Request
     */
    public void addRequestEntry(okhttp3.Request okHttpRequest,
            CoreEndpointConfiguration endpointConfiguration, Request request) {
        this.requestEntries.put(okHttpRequest, new RequestState(endpointConfiguration, request));
    }

    /**
     * getter for current request state entry from map.
     * @param okHttpRequest The OK HTTP Request.
     * @return RequestEntry The current request entry.
     */
    private RequestState getRequestState(okhttp3.Request okHttpRequest) {
        return this.requestEntries.get(okHttpRequest);
    }

    /**
     * Logs the response.
     * @param requestState The current state of request.
     * @param response The OKhttp Response.
     */
    @SuppressWarnings("unused")
    private void logResponse(RequestState requestState, okhttp3.Response response) {
        Response httpResponse = null;
        try {
            httpResponse = OkClient.convertResponse(requestState.httpRequest, response,
                    requestState.endpointConfiguration.hasBinaryResponse());
            if (httpLogger != null) {
                httpLogger.logResponse(requestState.httpRequest, httpResponse);
            }
        } catch (IOException ioException) {
            logError(requestState, ioException);
        }
    }

    /**
     * Logs the exception.
     * @param requestState The current state of request.
     * @param ioException The exception.
     */
    private void logError(RequestState requestState, IOException ioException) {
        if (httpLogger != null) {
            httpLogger.setError(requestState.httpRequest, ioException);
            httpLogger.logResponse(requestState.httpRequest, null);
        }
    }


    /**
     * Class to hold the request info until request completes.
     */
    private final class RequestState {

        /**
         * The internal HTTP request.
         */
        private Request httpRequest;

        /**
         * To keep track of requests count.
         */
        private int retryCount = 0;

        /**
         * To store the wait time for next request.
         */
        private long currentWaitInMilliSeconds = 0;

        /**
         * To keep track of overall wait time.
         */
        private long totalWaitTimeInMilliSeconds = 0;

        /**
         * To keep track of request endpoint configurations.
         */
        private CoreEndpointConfiguration endpointConfiguration;

        /**
         * Default Constructor.
         * @param coreEndpointConfiguration The end point configuration
         * @param request The client request
         */
        private RequestState(final CoreEndpointConfiguration coreEndpointConfiguration,
                final Request request) {
            this.endpointConfiguration = coreEndpointConfiguration;
            this.httpRequest = request;
        }
    }
}
