package apimatic.okhttpclient.adapter;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import apimatic.okhttpclient.adapter.mocks.CompatibilityFactoryMock;
import io.apimatic.coreinterfaces.compatibility.CompatibilityFactory;
import io.apimatic.coreinterfaces.http.ClientConfiguration;
import io.apimatic.coreinterfaces.http.Method;
import io.apimatic.coreinterfaces.http.request.configuration.CoreEndpointConfiguration;
import io.apimatic.coreinterfaces.http.request.configuration.RetryOption;
import io.apimatic.coreinterfaces.logger.ApiLogger;
import io.apimatic.okhttpclient.adapter.interceptors.RetryInterceptor;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptorTest extends CompatibilityFactoryMock {

    /**
     * Retry interval.
     */
    private static final Long RETRY_INTERVAL = 1L;

    /**
     * Status code of bad request.
     */
    private static final int BAD_REQUET_STATUS_CODE = 400;

    /**
     * Status code of resource not found.
     */
    private static final int NOT_FOUND_STATUS_CODE = 404;

    /**
     * back off interval.
     */
    private static final int BACK_OFF_FACTOR = 2;

    /**
     * number of retries.
     */
    private static final int NO_OF_RETRIES = 3;

    /**
     * Maximum retry wait time.
     */
    private static final long MAX_RETRY_WAIT_TIME = 6L;

    /**
     * Initializes mocks annotated with Mock.
     */
    @Rule
    public MockitoRule initRule = MockitoJUnit.rule().silent();

    /**
     * Mock of {@link ClientConfiguration}.
     */
    @Mock
    private ClientConfiguration clientConfiguration;

    /**
     * Mock of {@link Request}.
     */
    @Mock
    private Request request;

    /**
     * Mock of {@link CompatibilityFactory}.
     */
    @Mock
    private CompatibilityFactory compatibilityFactory;

    /**
     * Mock of {@link CoreEndpointConfiguration}.
     */
    @Mock
    private CoreEndpointConfiguration endpointConfiguration;

    /**
     * Mock of {@link ApiLogger}.
     */
    @Mock
    private ApiLogger apiLogger;

    /**
     * Mock of {@link Headers}.
     */
    @Mock
    private Headers headers;

    /**
     * Mock of {@link Response}.
     */
    @Mock
    private Response response;

    /**
     * Mock of {@link Chain}.
     */
    @Mock
    private Chain chain;

    /**
     * Mock of {@link HttpUrl}.
     */
    @Mock
    private HttpUrl url;

    /**
     * Setup the test setup.
     * @throws IOException in case of I/O Exception occurred
     */
    @Before
    public void setup() throws IOException {
        prepareStub();
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testRetryUsingCode() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(NO_OF_RETRIES);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.code()).thenReturn(BAD_REQUET_STATUS_CODE);
        when(request.url()).thenReturn(url);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        Response httpResponse = interceptor.intercept(chain);
        assertFalse(httpResponse.isSuccessful());
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test(expected = IOException.class)
    public void testTimeOutException() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(NO_OF_RETRIES);
        when(chain.proceed(request)).thenThrow(IOException.class);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.code()).thenReturn(BAD_REQUET_STATUS_CODE);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        interceptor.intercept(chain);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test(expected = IOException.class)
    public void testShouldRetyOnTimeOutException() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(NO_OF_RETRIES);
        when(clientConfiguration.shouldRetryOnTimeout()).thenReturn(true);
        when(chain.proceed(request)).thenThrow(IOException.class);
        when(request.method()).thenReturn(Method.GET.toString());
        when(request.url()).thenReturn(url);
        when(response.code()).thenReturn(BAD_REQUET_STATUS_CODE);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        interceptor.intercept(chain);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testRetryHttpMethodsUsingCode() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(NO_OF_RETRIES);
        when(request.method()).thenReturn(Method.GET.toString());
        when(request.url()).thenReturn(url);
        when(response.code()).thenReturn(BAD_REQUET_STATUS_CODE);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        Response httpResponse = interceptor.intercept(chain);
        assertFalse(httpResponse.isSuccessful());
    }


    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testRetryWithHeader() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(NO_OF_RETRIES);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.header("Retry-After")).thenReturn("3");
        when(response.headers()).thenReturn(headers);
        when(request.url()).thenReturn(url);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        Response httpResponse = interceptor.intercept(chain);
        assertFalse(httpResponse.isSuccessful());
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test(expected = DateTimeParseException.class)
    public void testRetryWithWrongHeaderValue() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(NO_OF_RETRIES);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.header("Retry-After")).thenReturn("3N");
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        interceptor.intercept(chain);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testRetryWithDateHeaderValue() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(NO_OF_RETRIES);
        when(request.url()).thenReturn(url);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.header("Retry-After")).thenReturn("Wed, 13 Jul 2022 06:10:00 GMT");
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        interceptor.intercept(chain);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testZeroRetry() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(0);
        when(request.method()).thenReturn(Method.GET.toString());
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, endpointConfiguration, null);
        Response httpResponse = interceptor.intercept(chain);
        assertFalse(httpResponse.isSuccessful());
    }

    private void prepareStub() throws IOException {
        Set<Method> methodToRetry = new HashSet<Method>();
        methodToRetry.add(Method.GET);
        methodToRetry.add(Method.PUT);
        Set<Integer> statusCodeToRetry = new HashSet<>();
        statusCodeToRetry.add(BAD_REQUET_STATUS_CODE);
        statusCodeToRetry.add(NOT_FOUND_STATUS_CODE);

        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);
        when(clientConfiguration.getHttpMethodsToRetry()).thenReturn(methodToRetry);
        when(clientConfiguration.getHttpStatusCodesToRetry()).thenReturn(statusCodeToRetry);
        when(clientConfiguration.getRetryInterval()).thenReturn(RETRY_INTERVAL);
        when(clientConfiguration.getBackOffFactor()).thenReturn(BACK_OFF_FACTOR);
        when(clientConfiguration.getMaximumRetryWaitTime()).thenReturn(MAX_RETRY_WAIT_TIME);
        when(endpointConfiguration.getRetryOption()).thenReturn(RetryOption.DEFAULT);
        when(headers.toMultimap()).thenReturn(Collections.EMPTY_MAP);
        when(compatibilityFactory.createHttpHeaders(anyMap())).thenReturn(getHttpHeaders());
    }
}
