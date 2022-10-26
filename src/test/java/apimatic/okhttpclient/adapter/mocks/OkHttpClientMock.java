package apimatic.okhttpclient.adapter.mocks;

import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import okhttp3.Call;

public class OkHttpClientMock extends CompatibilityFactoryMock {

    /**
     * Initializes mocks annotated with Mock.
     */
    @Rule
    public MockitoRule initRule = MockitoJUnit.rule().silent();

    /**
     * Mock of {okhttp3.OkHttpClient}.
     */
    @Mock
    protected okhttp3.OkHttpClient client;

    /**
     * Mock of {@link Call}
     */
    @Mock
    protected Call call;

    /**
     * Mock of {@link okhttp3.OkHttpClient.Builder}.
     */
    @Mock
    private okhttp3.OkHttpClient.Builder clientBuilder;

    /**
     * Mock of {@link okhttp3.Response}.
     */
    @Mock
    private okhttp3.Response okHttpResponse;

    /**
     * Mock of {@link okhttp3.ResponseBody}.
     */
    @Mock
    private okhttp3.ResponseBody okHttpResponseBody;

    /**
     * Mock of {@link okhttp3.Headers}.
     */
    @Mock
    private okhttp3.Headers okHttpHeaders;

    protected okhttp3.OkHttpClient.Builder getOkHttpClientBuilder() {
        return clientBuilder;
    }

    protected okhttp3.Response getOkhttp3Response() {
        return okHttpResponse;
    }

    protected okhttp3.ResponseBody getOkhttp3ResponseBody() {
        return okHttpResponseBody;
    }

    protected okhttp3.Headers getOkhttpHeaders() {
        return okHttpHeaders;
    }
}
