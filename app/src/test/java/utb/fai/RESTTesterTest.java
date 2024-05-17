package utb.fai;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.RESTTester;

import org.apache.http.client.methods.HttpDelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;

public class RESTTesterTest {

    private RESTTester restTester;

    @Before
    public void setUp() throws NonUniqueModuleNamesException, InternalErrorException {
        restTester = new RESTTester("http://example.com", "TestModule", "GET", null);
        restTester.runModule();
    }

    @After
    public void cleanUp() throws InternalErrorException {
        restTester.terminateModule();
    }

    @Test
    public void testBuildGetRequest() {
        String[] params = { "param1=value1", "param2=value2" };
        HttpGet getRequest = restTester.buildGetRequest("http://example.com", params);

        assertNotNull(getRequest);
        assertEquals("GET", getRequest.getMethod());
        assertEquals("http://example.com?param1=value1&param2=value2", getRequest.getURI().toString());
    }

    @Test
    public void testBuildPostRequest() throws UnsupportedEncodingException {
        String[] params = { "param1=value1", "param2=value2", "#body={\"data\": \"value\"}" };
        HttpPost postRequest = restTester.buildPostRequest("http://example.com", params);

        assertNotNull(postRequest);
        assertEquals("POST", postRequest.getMethod());
        assertEquals("http://example.com?param1=value1&param2=value2", postRequest.getURI().toString());
    }

    @Test
    public void testBuildPutRequest() throws UnsupportedEncodingException {
        String[] params = { "param1=value1", "param2=value2" };
        HttpPut putRequest = restTester.buildPutRequest("http://example.com", params);

        assertNotNull(putRequest);
        assertEquals("PUT", putRequest.getMethod());
        assertEquals("http://example.com?param1=value1&param2=value2", putRequest.getURI().toString());
    }

    @Test
    public void testBuildDeleteRequest() {
        String[] params = { "param1=value1", "param2=value2" };
        HttpDelete deleteRequest = restTester.buildDeleteRequest("http://example.com", params);

        assertNotNull(deleteRequest);
        assertEquals("DELETE", deleteRequest.getMethod());
        assertEquals("http://example.com?param1=value1&param2=value2", deleteRequest.getURI().toString());
    }

}
