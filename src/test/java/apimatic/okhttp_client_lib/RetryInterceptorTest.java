package apimatic.okhttp_client_lib;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import apimatic.okhttp_client_lib.mocks.CompatibilityFactoryMock;
import io.apimatic.core_interfaces.http.ClientConfiguration;
import io.apimatic.core_interfaces.http.Method;
import io.apimatic.core_interfaces.http.request.configuration.RetryOption;
import io.apimatic.core_interfaces.logger.ApiLogger;
import io.apimatic.okhttp_client_lib.interceptors.RetryInterceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.Request;
import okhttp3.Response;

public class RetryInterceptorTest extends CompatibilityFactoryMock {

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    @Mock
    private ClientConfiguration clientConfiguration;

    @Mock
    private Request request;

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private Response response;

    @Mock
    private Chain chain;

    @Before
    public void setup() throws IOException {
        prepareStub();
    }

    @Test
    public void testRetryUsingCode() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.code()).thenReturn(400);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.DEFAULT);
        Response response = interceptor.intercept(chain);
        assertFalse(response.isSuccessful());
    }

    @Test(expected = IOException.class)
    public void testTimeOutException() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(chain.proceed(request)).thenThrow(IOException.class);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.code()).thenReturn(400);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.DEFAULT);
        interceptor.intercept(chain);
    }

    @Test(expected = IOException.class)
    public void testShouldRetyOnTimeOutException() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(clientConfiguration.shouldRetryOnTimeout()).thenReturn(true);
        when(chain.proceed(request)).thenThrow(IOException.class);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.code()).thenReturn(400);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.DEFAULT);
        interceptor.intercept(chain);
    }

    @Test
    public void testRetryHttpMethodsUsingCode() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.code()).thenReturn(400);
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.ENABLE_FOR_HTTP_METHOD);
        Response response = interceptor.intercept(chain);
        assertFalse(response.isSuccessful());
    }


    @Test
    public void testRetryWithHeader() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.header("Retry-After")).thenReturn("3");
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.DEFAULT);
        Response response = interceptor.intercept(chain);
        assertFalse(response.isSuccessful());
    }


    @Test(expected = DateTimeParseException.class)
    public void testRetryWithWrongHeaderValue() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.header("Retry-After")).thenReturn("3N");
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.DEFAULT);
        interceptor.intercept(chain);
    }

    @Test
    public void testRetryWithDateHeaderValue() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(request.method()).thenReturn(Method.GET.toString());
        when(response.header("Retry-After")).thenReturn("Wed, 13 Jul 2022 06:10:00 GMT");
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.DEFAULT);
        interceptor.intercept(chain);
    }

    @Test
    public void testZeroRetry() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(0);
        when(request.method()).thenReturn(Method.GET.toString());
        RetryInterceptor interceptor = new RetryInterceptor(clientConfiguration, apiLogger);
        interceptor.addRequestEntry(request, RetryOption.DEFAULT);
        Response response = interceptor.intercept(chain);
        assertFalse(response.isSuccessful());
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
    }
}
