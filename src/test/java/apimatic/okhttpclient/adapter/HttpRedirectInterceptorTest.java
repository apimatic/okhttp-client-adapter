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
import io.apimatic.coreinterfaces.http.ClientConfiguration;
import io.apimatic.coreinterfaces.http.Method;
import io.apimatic.okhttpclient.adapter.interceptors.HttpRedirectInterceptor;
import okhttp3.HttpUrl;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;

public class HttpRedirectInterceptorTest {

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    @Mock
    private ClientConfiguration clientConfiguration;

    @Mock
    private Request request;

    @Mock
    private Request.Builder requestBuilder;
    
    @Mock
    private Response response;

    @Mock
    private Chain chain;

    @Mock
    private HttpUrl url;

    private final HttpUrl httpUrl = new HttpUrl("https", "username", "password", "localhost", 3000,
            Arrays.asList("Search"), null, null, "https:\\localhost:3000\\location");

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
        when(response.code()).thenReturn(307);
        HttpRedirectInterceptor httpRedirectInterceptor = new HttpRedirectInterceptor(false);
        Response response = httpRedirectInterceptor.intercept(chain);
        assertFalse(response.isRedirect());
    }

    @Test(expected = ProtocolException.class)
    public void testResponseWithRedirectCodeHeader() throws IOException {
        when(response.header("Location")).thenReturn("location");
        when(response.code()).thenReturn(307);
        when(response.request()).thenReturn(request);
        when(request.url()).thenReturn(url);
        when(url.scheme()).thenReturn("https");
        when(url.resolve("location")).thenReturn(httpUrl);
        when(url.host()).thenReturn("localhost");
        when(url.port()).thenReturn(3000);
     
        HttpRedirectInterceptor httpRedirectInterceptor = new HttpRedirectInterceptor(false);
        httpRedirectInterceptor.intercept(chain);
    }

    @Test
    public void testInterceptWithTooManyFollowUp() throws IOException {
        when(response.code()).thenReturn(307);
        HttpRedirectInterceptor httpRedirectInterceptor = new HttpRedirectInterceptor(true);
        Response response =  httpRedirectInterceptor.intercept(chain);
        assertNull(response.header("Location"));
    }

    private void prepareStub() throws IOException {
        Set<Method> methodToRetry = new HashSet<Method>();
        methodToRetry.add(Method.GET);
        methodToRetry.add(Method.PUT);
        Set<Integer> statusCodeToRetry = new HashSet<>();
        statusCodeToRetry.add(400);
        statusCodeToRetry.add(404);

        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);
        when(clientConfiguration.getHttpMethodsToRetry()).thenReturn(methodToRetry);
        when(clientConfiguration.getHttpStatusCodesToRetry()).thenReturn(statusCodeToRetry);
        when(clientConfiguration.getRetryInterval()).thenReturn(1l);
        when(clientConfiguration.getBackOffFactor()).thenReturn(2);
        when(clientConfiguration.getMaximumRetryWaitTime()).thenReturn(6l);
        when(request.newBuilder()).thenReturn(requestBuilder);
        when(requestBuilder.url(httpUrl)).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
    }

}
