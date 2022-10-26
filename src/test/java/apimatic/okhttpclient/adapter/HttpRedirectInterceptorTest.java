package apimatic.okhttpclient.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import io.apimatic.coreinterfaces.http.ClientConfiguration;
import io.apimatic.coreinterfaces.http.Method;
import io.apimatic.okhttpclient.adapter.interceptors.HttpRedirectInterceptor;
import okhttp3.HttpUrl;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;

public class HttpRedirectInterceptorTest {

    /**
     * Http Url instance.
     */
    private static final HttpUrl HTTP_URL =
            new HttpUrl("https", "username", "password", "localhost", 3000, Arrays.asList("Search"),
                    null, null, "https:\\localhost:3000\\location");

    /**
     * Status code of bad request.
     */
    private static final int BAD_REQUET_STATUS_CODE = 400;

    /**
     * Status code of resource not found.
     */
    private static final int NOT_FOUND_STATUS_CODE = 404;

    /**
     * Status code of temporary redirect.
     */
    private static final int TEMPORARY_REDIRECT_STATUS_CODE = 307;

    /**
     * redirect port.
     */
    private static final int PORT = 3000;

    /**
     * Retry interval
     */
    private static final long RETRY_INTERVAL = 1L;

    /**
     * Back off factor
     */
    private static final int BACK_OFF_FACTOR = 2;

    /**
     * Maximum retry wait time
     */
    private static final long MAX_RETRY_WAIT_TIME = 6L;

    /**
     * Initializes mocks annotated with Mock.
     */
    @Rule
    public MockitoRule initRule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

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
     * Mock of {@link Request.Builder}.
     */
    @Mock
    private Request.Builder requestBuilder;

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
     * Setup the test setup
     * @throws IOException in case of I/O Exception occurred
     */
    @Before
    public void setup() throws IOException {
        prepareStub();
    }

    @Test
    public void testResponseWithSuccessCode() throws IOException {
        HttpRedirectInterceptor httpRedirectInterceptor = new HttpRedirectInterceptor(false);
        httpRedirectInterceptor.intercept(chain);
    }

    @Test
    public void testResponseWithRedirectCodeNullHeader() throws IOException {
        when(response.code()).thenReturn(TEMPORARY_REDIRECT_STATUS_CODE);
        HttpRedirectInterceptor httpRedirectInterceptor = new HttpRedirectInterceptor(false);
        Response response = httpRedirectInterceptor.intercept(chain);
        assertFalse(response.isRedirect());
    }

    @Test(expected = ProtocolException.class)
    public void testResponseWithRedirectCodeHeader() throws IOException {
        when(response.header("Location")).thenReturn("location");
        when(response.code()).thenReturn(TEMPORARY_REDIRECT_STATUS_CODE);
        when(response.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.scheme()).thenReturn("https");
        when(url.resolve("location")).thenReturn(HTTP_URL);
        when(url.host()).thenReturn("localhost");
        when(url.port()).thenReturn(PORT);

        HttpRedirectInterceptor httpRedirectInterceptor = new HttpRedirectInterceptor(false);
        httpRedirectInterceptor.intercept(chain);
    }

    @Test
    public void testInterceptWithTooManyFollowUp() throws IOException {
        when(response.code()).thenReturn(TEMPORARY_REDIRECT_STATUS_CODE);
        HttpRedirectInterceptor httpRedirectInterceptor = new HttpRedirectInterceptor(true);
        Response response = httpRedirectInterceptor.intercept(chain);
        assertNull(response.header("Location"));
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
        when(request.newBuilder()).thenReturn(requestBuilder);
        when(requestBuilder.url(HTTP_URL)).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
    }
}
