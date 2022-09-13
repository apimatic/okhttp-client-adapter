package apimatic.okhttp_client_lib;

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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import apimatic.okhttp_client_lib.mocks.OkHttpClientMock;
import io.apimatic.core_interfaces.http.CoreHttpClientConfiguration;
import io.apimatic.core_interfaces.http.CoreHttpMethod;
import io.apimatic.core_interfaces.http.HttpHeaders;
import io.apimatic.core_interfaces.http.request.ArraySerializationFormat;
import io.apimatic.core_interfaces.http.request.CoreMultipartFileWrapper;
import io.apimatic.core_interfaces.http.request.CoreMultipartWrapper;
import io.apimatic.core_interfaces.http.request.configuration.CoreEndpointConfiguration;
import io.apimatic.core_interfaces.http.request.configuration.RetryOption;
import io.apimatic.core_interfaces.http.response.CoreHttpResponse;
import io.apimatic.core_interfaces.type.FileWrapper;
import io.apimatic.okhttp_client_lib.OkClient;

public class OkClientTest extends OkHttpClientMock {


    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    @Mock
    private CoreHttpClientConfiguration clientConfiguration;

    @Mock
    private CoreHttpResponse coreHttpResponse;

    @Mock
    private List<SimpleEntry<String, Object>> parametersList;

    @Mock
    private CoreMultipartFileWrapper coreMultipartFileWrapper;

    @Mock
    private CoreMultipartWrapper coreMultipartWrapper;

    @Mock
    private CoreEndpointConfiguration configuration;

    @Mock
    private File file;

    @Mock
    private FileWrapper fileWrapper;

    @Before
    public void setup() {
        prepareStub();
    }

    @Test
    public void testOkClientConstructor() {
        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        assertNotNull(client);
    }

    @Test
    public void testshutDown() {
        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        client.shutdown();
    }

    @Test
    public void testOkClientConstructor1() {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newBuilder()).thenReturn(clientBuilder);
        when(clientBuilder.readTimeout(30l, TimeUnit.SECONDS)).thenReturn(clientBuilder);
        when(clientBuilder.writeTimeout(30l, TimeUnit.SECONDS)).thenReturn(clientBuilder);
        when(clientBuilder.connectTimeout(30l, TimeUnit.SECONDS)).thenReturn(clientBuilder);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        assertNotNull(client);
    }

    @Test
    public void testFileWrapperMockClient() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);

        when(coreHttpRequest.getBody()).thenReturn(fileWrapper);
        when(fileWrapper.getContentType()).thenReturn("application/json");

        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);


        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String fileWrapperString = fileWrapper.toString();
        when(coreHttpResponse.getBody()).thenReturn(fileWrapperString);
        when(okHttpResponseBody.string()).thenReturn(fileWrapperString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = fileWrapper.toString();
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }


    @Test
    public void testFileWrapperMockClient1() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);

        when(coreHttpRequest.getBody()).thenReturn(fileWrapper);


        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);
        when(httpHeaders.has("content-type")).thenReturn(true);


        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String fileWrapperString = fileWrapper.toString();
        when(coreHttpResponse.getBody()).thenReturn(fileWrapperString);
        when(okHttpResponseBody.string()).thenReturn(fileWrapperString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = fileWrapper.toString();
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }


    @Test
    public void testFileWrapperMockClient2() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);

        when(coreHttpRequest.getBody()).thenReturn(fileWrapper);


        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);


        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String fileWrapperString = fileWrapper.toString();
        when(coreHttpResponse.getBody()).thenReturn(fileWrapperString);
        when(okHttpResponseBody.string()).thenReturn(fileWrapperString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = fileWrapper.toString();
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }


    @Test
    public void testGetRequestMockClient() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.GET);


        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String serverResponseString = "Get Response";
        when(coreHttpResponse.getBody()).thenReturn(serverResponseString);
        when(okHttpResponseBody.string()).thenReturn(serverResponseString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    @Test
    public void testSimplePostRequestMockClient() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);
        when(coreHttpRequest.getBody()).thenReturn("bodyValue");


        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String serverResponseString = "Get Response";
        when(coreHttpResponse.getBody()).thenReturn(serverResponseString);
        when(okHttpResponseBody.string()).thenReturn(serverResponseString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }


    @Test
    public void testPostMultipartFileWrapperRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", coreMultipartFileWrapper));

        when(coreHttpRequest.getParameters()).thenReturn(listP);
        when(coreMultipartFileWrapper.getFileWrapper()).thenReturn(fileWrapper);
        when(file.getName()).thenReturn("Test\nFile\r\"Part\"");

        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String serverResponseString = "File has been posted";
        when(coreHttpResponse.getBody()).thenReturn(serverResponseString);
        when(okHttpResponseBody.string()).thenReturn(serverResponseString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }

    @Test
    public void testPostFormParametersRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", "form value"));

        when(coreHttpRequest.getParameters()).thenReturn(listP);


        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String serverResponseString = "form paramaters";
        when(coreHttpResponse.getBody()).thenReturn(serverResponseString);
        when(okHttpResponseBody.string()).thenReturn(serverResponseString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }


    @Test
    public void testPostEmptyBodyRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);

        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String serverResponseString = "empty body";
        when(coreHttpResponse.getBody()).thenReturn(serverResponseString);
        when(okHttpResponseBody.string()).thenReturn(serverResponseString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }


    @Test
    public void testPostEmptyBodyAsyncRequest()
            throws IOException, InterruptedException, ExecutionException {
//        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
//        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
//        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);
//
//        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
//        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);
//
//
//        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
//        String serverResponseString = "empty body";
//        when(coreHttpResponse.getBody()).thenReturn(serverResponseString);
//        when(okHttpResponseBody.string()).thenReturn(serverResponseString);
//        when(okHttpResponse.code()).thenReturn(200);
//
//        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
//                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);
//
//        CompletableFuture<CoreHttpResponse> completableFutureResponse =
//                client.executeAsync(coreHttpRequest, configuration);
//        CoreHttpResponse coreHttpResponse = completableFutureResponse.get();
//        String expected = serverResponseString;
//        String actual = coreHttpResponse.getBody();
//        assertEquals(actual, expected);
    }

    @Test
    public void testPostMultipartWrapperRequest() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);
        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);

        List<SimpleEntry<String, Object>> listP = new ArrayList<>();
        listP.add(new SimpleEntry<String, Object>("fileWrapper", "formValue"));

        when(coreHttpRequest.getParameters()).thenReturn(listP);

        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String serverResponseString = "form parameters sent";
        when(coreHttpResponse.getBody()).thenReturn(serverResponseString);
        when(okHttpResponseBody.string()).thenReturn(serverResponseString);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class), anyString())).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        String expected = serverResponseString;
        String actual = coreHttpResponse.getBody();
        assertEquals(actual, expected);
    }


    @Test
    public void testFileWrapperMockClientBinaryResponse() throws IOException {
        when(clientConfiguration.getHttpClientInstance()).thenReturn(client);
        when(clientConfiguration.shouldOverrideHttpClientConfigurations()).thenReturn(true);
        when(configuration.hasBinaryResponse()).thenReturn(true);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        OkClient client = new OkClient(clientConfiguration, compatibilityFactory);

        when(coreHttpRequest.getBody()).thenReturn(fileWrapper);
        when(fileWrapper.getContentType()).thenReturn("application/json");
        when(fileWrapper.getFile()).thenReturn(file);


        when(coreHttpRequest.getHttpMethod()).thenReturn(CoreHttpMethod.POST);

        when(call.execute()).thenReturn(okHttpResponse);
        when(okHttpResponse.body()).thenReturn(okHttpResponseBody);
        String serverResponse =
                "{\"ServerMessage\" : \"This is a message from server\" , \"ServerCode\" : 5000 }";
        InputStream serverResponseStream = new ByteArrayInputStream(serverResponse.getBytes());

        when(coreHttpResponse.getRawBody()).thenReturn(serverResponseStream);
        when(okHttpResponseBody.byteStream()).thenReturn(serverResponseStream);
        when(okHttpResponse.code()).thenReturn(200);

        when(compatibilityFactory.createHttpResponse(anyInt(), any(HttpHeaders.class),
                any(InputStream.class))).thenReturn(coreHttpResponse);

        CoreHttpResponse coreHttpResponse = client.execute(coreHttpRequest, configuration);
        InputStream expected = serverResponseStream;
        InputStream actual = coreHttpResponse.getRawBody();
        assertEquals(actual, expected);
    }


    private void prepareStub() {
        when(configuration.getArraySerializationFormat())
                .thenReturn(ArraySerializationFormat.INDEXED);
        when(configuration.getRetryOption()).thenReturn(RetryOption.DEFAULT);

        when(clientConfiguration.getNumberOfRetries()).thenReturn(3);
        when(clientConfiguration.getTimeout()).thenReturn(30l);
        when(clientConfiguration.getMaximumRetryWaitTime()).thenReturn(1l);
        when(compatibilityFactory.createHttpHeaders(anyMap())).thenReturn(httpHeaders);
        when(compatibilityFactory.createHttpHeaders(httpHeaders)).thenReturn(httpHeaders);
        when(httpHeaders.value("content-type")).thenReturn("application/octet-stream");
        when(client.newBuilder()).thenReturn(clientBuilder);
        when(clientBuilder.readTimeout(30l, TimeUnit.SECONDS)).thenReturn(clientBuilder);
        when(clientBuilder.writeTimeout(30l, TimeUnit.SECONDS)).thenReturn(clientBuilder);
        when(clientBuilder.connectTimeout(30l, TimeUnit.SECONDS)).thenReturn(clientBuilder);
        when(clientBuilder.callTimeout(1l, TimeUnit.SECONDS)).thenReturn(clientBuilder);
        when(clientBuilder.build()).thenReturn(client);
        when(configuration.getArraySerializationFormat())
                .thenReturn(ArraySerializationFormat.INDEXED);
        when(configuration.getRetryOption()).thenReturn(RetryOption.DEFAULT);
        when(coreHttpRequest.getHeaders()).thenReturn(httpHeaders);
        when(okHttpResponse.headers()).thenReturn(okHttpHeaders);
        when(coreHttpRequest.getUrl(ArraySerializationFormat.INDEXED))
                .thenReturn("https://localhost:3000");
        when(fileWrapper.getFile()).thenReturn(file);
        when(coreMultipartFileWrapper.getHeaders()).thenReturn(httpHeaders);
        when(coreMultipartWrapper.getHeaders()).thenReturn(httpHeaders);
        String fileContent = "I'm the file content";
        byte[] fileBytes = fileContent.getBytes();
        when(coreMultipartWrapper.getByteArray()).thenReturn(fileBytes);
    }
}
