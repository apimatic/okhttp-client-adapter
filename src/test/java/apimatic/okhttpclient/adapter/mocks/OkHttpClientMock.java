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
    private okhttp3.OkHttpClient client;

    /**
     * Mock of {@link Call}.
     */
    @Mock
    private Call call;

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

    /**
     * @return {@link okhttp3.OkHttpClient.Builder}.
     */
    protected okhttp3.OkHttpClient.Builder getOkHttpClientBuilder() {
        return clientBuilder;
    }

    /**
     * @return {@link okhttp3.Response}.
     */
    protected okhttp3.Response getOkhttp3Response() {
        return okHttpResponse;
    }

    /**
     * @return {@link okhttp3.ResponseBody}.
     */
    protected okhttp3.ResponseBody getOkhttp3ResponseBody() {
        return okHttpResponseBody;
    }

    /**
     * @return {@link okhttp3.Headers}.
     */
    protected okhttp3.Headers getOkhttpHeaders() {
        return okHttpHeaders;
    }

    /**
     * @return {@link okhttp3.OkHttpClient}.
     */
    protected okhttp3.OkHttpClient getClient() {
        return client;
    }

    /**
     * @return {@link Call}.
     */
    protected Call getCall() {
        return call;
    }
}
