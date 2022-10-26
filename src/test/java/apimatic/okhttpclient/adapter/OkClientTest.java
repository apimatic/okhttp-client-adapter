package apimatic.okhttpclient.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import apimatic.okhttpclient.adapter.mocks.OkHttpClientMock;
import io.apimatic.coreinterfaces.http.ClientConfiguration;
import io.apimatic.coreinterfaces.http.HttpHeaders;
import io.apimatic.coreinterfaces.http.Method;
import io.apimatic.coreinterfaces.http.request.ArraySerializationFormat;
import io.apimatic.coreinterfaces.http.request.Multipart;
import io.apimatic.coreinterfaces.http.request.MultipartFile;
import io.apimatic.coreinterfaces.http.request.configuration.CoreEndpointConfiguration;
import io.apimatic.coreinterfaces.http.request.configuration.RetryOption;
import io.apimatic.coreinterfaces.http.response.Response;
import io.apimatic.coreinterfaces.logger.ApiLogger;
import io.apimatic.coreinterfaces.type.CoreFileWrapper;
import io.apimatic.okhttpclient.adapter.OkClient;

public class OkClientTest extends OkHttpClientMock {

    /**
     * timeout.
     */
    private static final long DEFAULT_TIME_OUT = 30L;

    /**
     * Success status code.
     */
    private static final int SUCCESS_STATUS_CODE = 200;

    /**
     * Retry interval.
     */
    private static final int RETRY_INTERVAL = 3;

    /**
     * Maximum wait retry time.
     */
    private static final long MAX_WAIT_RETRY_TIME = 1L;

    /**
     * Call timeout.
     */
    private static final long CALL_TIMEOUT = 1L;

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
     * Mock of {@link Response}.
     */
    @Mock
    private ApiLogger apiLogger;

    /**
     * Mock of {@link Response}.
     */
    @Mock
    private Response httpResponse;

    /**
     * Mock of List.
     */
    @Mock
    private List<SimpleEntry<String, Object>> parametersList;

    /**
     * Mock of {@link MultipartFile}.
     */
    @Mock
    private MultipartFile coreMultipartFileWrapper;

    /**
     * Mock of {@link MultipartFile}.
     */
    @Mock
    private Multipart coreMultipartWrapper;

    /**
     * Mock of {@link CoreEndpointConfiguration}.
     */
    @Mock
    private CoreEndpointConfiguration configuration;

    /**
     * Mock of {@link File}.
     */
    @Mock
    private File file;

    /**
     * Mock of {@link CoreFileWrapper}.
     */
    @Mock
    private CoreFileWrapper fileWrapper;

    /**
     * Setup the test setup.
     * @throws IOException in case of I/O Exception occurred.
     */
    @Before
    public void setup() {
        prepareStub();
    }

    /**
     * Test the default OkHttpClient.
     */
    @Test
    public void testDefaultOkHttpClient() {
        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        assertNotNull(getClient());
    }

    /**
     * Test the Skip SSL client configuration.
     */
    @Test
    public void testInsecureOkhttpClient() {
        when(clientConfiguration.skipSslCertVerification()).thenReturn(true);
        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        assertNotNull(getClient());
    }

    /**
     * test the client shutdown behaviour.
     */
    @SuppressWarnings("static-access")
    @Test
    public void testshutDown() {
        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        client.shutdown();
    }

    /**
     * test the OK client constructor variant.
     */
    @Test
    public void testOkClientConstructor1() {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newBuilder()).thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS))
                .thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS))
                .thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS))
                .thenReturn(getOkHttpClientBuilder());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        assertNotNull(getClient());
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testFileWrapperMockClient() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());

        when(getRequest().getBody()).thenReturn(fileWrapper);
        when(fileWrapper.getContentType()).thenReturn("application/json");

        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String fileWrapperString = fileWrapper.toString();
        when(httpResponse.getBody()).thenReturn(fileWrapperString);
        when(getOkhttp3ResponseBody().string()).thenReturn(fileWrapperString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = fileWrapper.toString();
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testFileWrapperMockClient1() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());

        when(getRequest().getBody()).thenReturn(fileWrapper);


        when(getRequest().getHttpMethod()).thenReturn(Method.POST);
        when(getHttpHeaders().has("content-type")).thenReturn(true);


        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String fileWrapperString = fileWrapper.toString();
        when(httpResponse.getBody()).thenReturn(fileWrapperString);
        when(getOkhttp3ResponseBody().string()).thenReturn(fileWrapperString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = fileWrapper.toString();
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testFileWrapperMockClient2() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());

        when(getRequest().getBody()).thenReturn(fileWrapper);

        when(getRequest().getHttpMethod()).thenReturn(Method.POST);
        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String fileWrapperString = fileWrapper.toString();
        when(httpResponse.getBody()).thenReturn(fileWrapperString);
        when(getOkhttp3ResponseBody().string()).thenReturn(fileWrapperString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);
        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = fileWrapper.toString();
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testGetRequestMockClient() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.GET);


        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "Get Response";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);
        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testSimplePostRequestMockClient() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);
        when(getRequest().getBody()).thenReturn("bodyValue");


        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "Get Response";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testRequestWithNoRetries() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(-1);
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);
        when(getRequest().getBody()).thenReturn("bodyValue");


        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "Get Response";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testExecuteRequestWithLogging() throws IOException {
        when(clientConfiguration.getNumberOfRetries()).thenReturn(-1);
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory(), apiLogger);
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);
        when(getRequest().getBody()).thenReturn("bodyValue");


        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "Get Response";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test(expected = IOException.class)
    public void testExecuteRequestWithLogging1() throws IOException {
        IOException ioException = new IOException("Connection Error");

        when(clientConfiguration.getNumberOfRetries()).thenReturn(-1);
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory(), apiLogger);
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);
        when(getRequest().getBody()).thenReturn("bodyValue");
        when(getCall().execute()).thenThrow(ioException);

        client.execute(getRequest(), configuration);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testPostMultipartFileWrapperRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", coreMultipartFileWrapper));

        when(getRequest().getParameters()).thenReturn(listP);
        when(coreMultipartFileWrapper.getFileWrapper()).thenReturn(fileWrapper);
        when(file.getName()).thenReturn("Test\nFile\r\"Part\"");

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "File has been posted";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testPostMultipartFileWrapperRequest1() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);
        when(getHttpHeaders().asMultimap()).thenReturn(
                Collections.singletonMap("custom-header", Arrays.asList("application/json")));

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", coreMultipartFileWrapper));

        when(getRequest().getParameters()).thenReturn(listP);
        when(coreMultipartFileWrapper.getFileWrapper()).thenReturn(fileWrapper);
        when(fileWrapper.getContentType()).thenReturn("application/octet-stream");
        when(file.getName()).thenReturn("Test\nFile\r\"Part\"");

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "File has been posted";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testPostMultipartRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", coreMultipartWrapper));

        String serverResponseString = "File has been posted";
        byte[] byteArray = serverResponseString.getBytes();
        when(getRequest().getParameters()).thenReturn(listP);
        when(coreMultipartWrapper.getByteArray()).thenReturn(byteArray);
        when(file.getName()).thenReturn("Test\nFile\r\"Part\"");

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testSimpleObjectWithMultiPart() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", coreMultipartWrapper));
        listP.add(new SimpleEntry<String, Object>("simple object", "object"));

        String serverResponseString = "object";

        when(getRequest().getParameters()).thenReturn(listP);

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testPostFormParametersRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(false);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory(), null);
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", "form value"));

        when(getRequest().getParameters()).thenReturn(listP);


        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "form paramaters";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testPostEmptyBodyRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "empty body";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testPostMultipartWrapperRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());
        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", "formValue"));

        when(getRequest().getParameters()).thenReturn(listP);

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponseString = "form parameters sent";
        when(httpResponse.getBody()).thenReturn(serverResponseString);
        when(getOkhttp3ResponseBody().string()).thenReturn(serverResponseString);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    /**
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @Test
    public void testFileWrapperMockClientBinaryResponse() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(getClient());
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(configuration.hasBinaryResponse()).thenReturn(true);
        when(getClient().newCall(any(okhttp3.Request.class))).thenReturn(getCall());

        OkClient client = new OkClient(clientConfiguration, getCompatibilityFactory());

        when(getRequest().getBody()).thenReturn(fileWrapper);
        when(fileWrapper.getContentType()).thenReturn("application/json");
        when(fileWrapper.getFile()).thenReturn(file);


        when(getRequest().getHttpMethod()).thenReturn(Method.POST);

        when(getCall().execute()).thenReturn(getOkhttp3Response());
        when(getOkhttp3Response().body()).thenReturn(getOkhttp3ResponseBody());
        String serverResponse =
                "{\"ServerMessage\" : \"This is a message from server\" , \"ServerCode\" : 5000 }";
        InputStream serverResponseStream = new ByteArrayInputStream(serverResponse.getBytes());

        when(httpResponse.getRawBody()).thenReturn(serverResponseStream);
        when(getOkhttp3ResponseBody().byteStream()).thenReturn(serverResponseStream);
        when(getOkhttp3Response().code()).thenReturn(SUCCESS_STATUS_CODE);

        when(getCompatibilityFactory().createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class))).thenReturn(httpResponse);

        Response coreHttpResponse = client.execute(getRequest(), configuration);
        InputStream expected = serverResponseStream;
        InputStream actual = coreHttpResponse.getRawBody();
        assertEquals(actual, expected);
    }

    private void prepareStub() {
        when(configuration.getArraySerializationFormat())
                .thenReturn(ArraySerializationFormat.INDEXED);
        when(configuration.getRetryOption()).thenReturn(RetryOption.DEFAULT);

        when(clientConfiguration.getNumberOfRetries()).thenReturn(RETRY_INTERVAL);
        when(clientConfiguration.getTimeout()).thenReturn(DEFAULT_TIME_OUT);
        when(clientConfiguration.getMaximumRetryWaitTime()).thenReturn(MAX_WAIT_RETRY_TIME);
        when(getCompatibilityFactory().createHttpHeaders(anyMap())).thenReturn(getHttpHeaders());
        when(getCompatibilityFactory().createHttpHeaders(getHttpHeaders()))
                .thenReturn(getHttpHeaders());
        when(getHttpHeaders().value("content-type")).thenReturn("application/octet-stream");
        when(getClient().newBuilder()).thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS))
                .thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS))
                .thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS))
                .thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().callTimeout(CALL_TIMEOUT, TimeUnit.SECONDS))
                .thenReturn(getOkHttpClientBuilder());
        when(getOkHttpClientBuilder().build()).thenReturn(getClient());
        when(configuration.getArraySerializationFormat())
                .thenReturn(ArraySerializationFormat.INDEXED);
        when(configuration.getRetryOption()).thenReturn(RetryOption.DEFAULT);
        when(getRequest().getHeaders()).thenReturn(getHttpHeaders());
        when(getOkhttp3Response().headers()).thenReturn(getOkhttpHeaders());
        when(getRequest().getUrl(ArraySerializationFormat.INDEXED))
                .thenReturn("https://localhost:3000");
        when(fileWrapper.getFile()).thenReturn(file);
        when(coreMultipartFileWrapper.getHeaders()).thenReturn(getHttpHeaders());
        when(coreMultipartWrapper.getHeaders()).thenReturn(getHttpHeaders());
        String fileContent = "I'm the file content";
        byte[] fileBytes = fileContent.getBytes();
        when(coreMultipartWrapper.getByteArray()).thenReturn(fileBytes);
    }
}
