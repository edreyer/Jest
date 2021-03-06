package io.searchbox.client.http;

import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.client.http.apache.HttpDeleteWithEntity;
import io.searchbox.client.http.apache.HttpGetWithEntity;
import io.searchbox.core.Search;
import org.apache.http.Header;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @author Dogukan Sonmez
 */
public class JestHttpClientTest {

    JestHttpClient client = new JestHttpClient();

    @Test
    public void constructGetHttpMethod() throws UnsupportedEncodingException {
        HttpUriRequest request = client.constructHttpMethod("GET", "jest/get",
                null);
        assertNotNull(request);
        assertEquals(request.getURI().getPath(), "jest/get");
        assertTrue(request instanceof HttpGetWithEntity);
    }

    @Test
    public void constructPutHttpMethod() throws UnsupportedEncodingException {
        HttpUriRequest request = client.constructHttpMethod("PUT", "jest/put",
                "data");
        assertNotNull(request);
        assertEquals(request.getURI().getPath(), "jest/put");
        assertTrue(request instanceof HttpPut);
    }

    @Test
    public void constructPostHttpMethod() throws UnsupportedEncodingException {
        HttpUriRequest request = client.constructHttpMethod("POST",
                "jest/post", "data");
        assertNotNull(request);
        assertEquals(request.getURI().getPath(), "jest/post");
        assertTrue(request instanceof HttpPost);
    }

    @Test
    public void constructDeleteHttpMethod() throws UnsupportedEncodingException {
        HttpUriRequest request = client.constructHttpMethod("DELETE",
                "jest/delete", null);
        assertNotNull(request);
        assertEquals(request.getURI().getPath(), "jest/delete");
        assertTrue(request instanceof HttpDeleteWithEntity);
    }

    @Test
    public void constructHeadHttpMethod() throws UnsupportedEncodingException {
        HttpUriRequest request = client.constructHttpMethod("HEAD",
                "jest/head", null);
        assertNotNull(request);
        assertEquals(request.getURI().getPath(), "jest/head");
        assertTrue(request instanceof HttpHead);
    }

    @Test
    public void addHeadersToRequest() throws IOException {
        final String headerKey = "foo";
        final String headerValue = "bar";

        CloseableHttpResponse httpResponseMock = mock(CloseableHttpResponse.class);
        doReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "OK")).when(httpResponseMock).getStatusLine();
        doReturn(null).when(httpResponseMock).getEntity();

        CloseableHttpClient closeableHttpClientMock = mock(CloseableHttpClient.class);
        doReturn(httpResponseMock).when(closeableHttpClientMock).execute(any(HttpUriRequest.class));

        // Construct a new Jest client according to configuration via factory
        JestHttpClient clientWithMockedHttpClient;
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:9200").build());
        clientWithMockedHttpClient = (JestHttpClient) factory.getObject();
        clientWithMockedHttpClient.setHttpClient(closeableHttpClientMock);

        // could reuse the above setup for testing core types against expected
        // HttpUriRequest (more of an end to end test)

        String query = "{\n" +
                "    \"query\": {\n" +
                "        \"filtered\" : {\n" +
                "            \"query\" : {\n" +
                "                \"query_string\" : {\n" +
                "                    \"query\" : \"test\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"filter\" : {\n" +
                "                \"term\" : { \"user\" : \"kimchy\" }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        Search search = new Search.Builder(query)
                // multiple index or types can be added.
                .addIndex("twitter")
                .addType("tweet")
                .setHeader(headerKey, headerValue)
                .build();
        // send request (not really)
        clientWithMockedHttpClient.execute(search);

        verify(closeableHttpClientMock).execute(argThat(new ArgumentMatcher<HttpUriRequest>() {
            @Override
            public boolean matches(Object o) {
                boolean retval = false;

                if (o instanceof HttpUriRequest) {
                    HttpUriRequest req = (HttpUriRequest) o;
                    Header header = req.getFirstHeader(headerKey);
                    if (header != null) {
                        retval = headerValue.equals(header.getValue());
                    }
                }

                return retval;
            }
        }));
    }

}
