package apimatic.okhttpclient.adapter.mocks;

import org.junit.Rule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import io.apimatic.coreinterfaces.http.HttpHeaders;

public class HttpHeadersMock {

    /**
     * Initializes mocks annotated with Mock.
     */
    @Rule
    public MockitoRule initRule = MockitoJUnit.rule().silent();

    /**
     * Mock of {@link HttpHeaders}.
     */
    @Mock
    private HttpHeaders httpHeaders;

    /**
     * 
     * @return {@link HttpHeaders}
     */
    protected HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }
}
