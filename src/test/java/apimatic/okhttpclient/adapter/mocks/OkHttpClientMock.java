package apimatic.okhttpclient.adapter.mocks;

import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import okhttp3.Call;

public class OkHttpClientMock extends CompatibilityFactoryMock {

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule().silent();

    @Mock
    protected okhttp3.OkHttpClient client;

    @Mock
    protected Call call;

    @Mock
    protected okhttp3.OkHttpClient.Builder clientBuilder;

    @Mock
    protected okhttp3.Response okHttpResponse;

    @Mock
    protected okhttp3.ResponseBody okHttpResponseBody;

    @Mock
    protected okhttp3.Headers okHttpHeaders;

}
