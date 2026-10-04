package org.jsoup.helper;

import org.jsoup.Connection;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpConnectionTest {

    @Test
    public void testConnectUrlStringAndUrl() throws Exception {
        Connection con1 = HttpConnection.connect("http://example.com/test");
        Assert.assertNotNull(con1);
        Assert.assertEquals("http://example.com/test", con1.request().url().toExternalForm());

        Connection con2 = HttpConnection.connect(new URL("https://example.com/api"));
        Assert.assertNotNull(con2);
        Assert.assertEquals("https://example.com/api", con2.request().url().toExternalForm());
    }

    @Test
    public void testEncodeUrl() throws Exception {
        URL encoded = HttpConnection.encodeUrl(new URL("http://example.com/path with spaces/"));
        Assert.assertEquals("http://example.com/path%20with%20spaces/", encoded.toExternalForm());

        Connection con = new HttpConnection();
        con.url("http://example.com/search?q=hello world");
        Assert.assertEquals("http://example.com/search?q=hello%20world", con.request().url().toExternalForm());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUrlStringThrowsException() {
        HttpConnection.connect("invalid_url_protocol");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUrlStringThrowsException() {
        HttpConnection.connect("");
    }

    @Test
    public void testFluentConfigurationMethods() throws Exception {
        HttpConnection con = new HttpConnection();
        con.url(new URL("http://example.com"))
           .userAgent("CustomUA")
           .timeout(5000)
           .maxBodySize(2048)
           .followRedirects(false)
           .referrer("http://google.com")
           .method(Connection.Method.POST)
           .ignoreHttpErrors(true)
           .ignoreContentType(true)
           .postDataCharset("UTF-8");

        Connection.Request req = con.request();
        Assert.assertEquals("CustomUA", req.header("User-Agent"));
        Assert.assertEquals(5000, req.timeout());
        Assert.assertEquals(2048, req.maxBodySize());
        Assert.assertFalse(req.followRedirects());
        Assert.assertEquals("http://google.com", req.header("Referer"));
        Assert.assertEquals(Connection.Method.POST, req.method());
        Assert.assertTrue(req.ignoreHttpErrors());
        Assert.assertTrue(req.ignoreContentType());
        Assert.assertEquals("UTF-8", req.postDataCharset());

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new java.net.InetSocketAddress("127.0.0.1", 8080));
        con.proxy(proxy);
        Assert.assertEquals(proxy, req.proxy());

        con.proxy("localhost", 8888);
        Assert.assertNotNull(req.proxy());

        SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        con.sslSocketFactory(sslFactory);
        Assert.assertEquals(sslFactory, req.sslSocketFactory());

        Parser parser = Parser.xmlParser();
        con.parser(parser);
        Assert.assertEquals(parser, req.parser());

        HttpConnection.Response res = new HttpConnection.Response();
        con.response(res);
        Assert.assertEquals(res, con.response());
        con.request(req);
        Assert.assertEquals(req, con.request());
    }

    @Test
    public void testDataMethods() {
        HttpConnection con = new HttpConnection();
        con.data("k1", "v1");
        con.data("k2", "v2", new ByteArrayInputStream("test".getBytes()));
        con.data("k3", "v3", new ByteArrayInputStream("test2".getBytes()), "text/plain");

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("k4", "v4");
        con.data(dataMap);

        con.data("k5", "v5", "k6", "v6");

        List<Connection.KeyVal> list = new ArrayList<>();
        list.add(HttpConnection.KeyVal.create("k7", "v7"));
        con.data(list);

        Assert.assertEquals(7, con.request().data().size());
        Assert.assertEquals("v1", con.data("k1").value());
        Assert.assertEquals("text/plain", con.data("k3").contentType());
        Assert.assertNull(con.data("nonexistent"));

        con.requestBody("raw body text");
        Assert.assertEquals("raw body text", con.request().requestBody());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataOddArrayThrowsException() {
        HttpConnection con = new HttpConnection();
        con.data("k1", "v1", "odd");
    }

    @Test
    public void testHeadersAndCookies() {
        HttpConnection con = new HttpConnection();
        con.header("Accept-Language", "en-US");
        con.header("X-Test", "1");
        Assert.assertTrue(con.request().hasHeader("accept-language"));
        Assert.assertTrue(con.request().hasHeaderWithValue("x-test", "1"));
        Assert.assertFalse(con.request().hasHeaderWithValue("x-test", "2"));
        Assert.assertEquals("en-US", con.request().header("ACCEPT-LANGUAGE"));

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-1", "val1");
        headers.put("X-Custom-2", "val2");
        con.headers(headers);
        Assert.assertEquals("val1", con.request().header("X-Custom-1"));

        con.request().removeHeader("X-Test");
        Assert.assertFalse(con.request().hasHeader("X-Test"));

        con.cookie("session", "abc");
        Assert.assertTrue(con.request().hasCookie("session"));
        Assert.assertEquals("abc", con.request().cookie("session"));

        Map<String, String> cookies = new HashMap<>();
        cookies.put("c1", "v1");
        cookies.put("c2", "v2");
        con.cookies(cookies);
        Assert.assertEquals("v1", con.request().cookie("c1"));
        con.request().removeCookie("session");
        Assert.assertFalse(con.request().hasCookie("session"));
        Assert.assertEquals(2, con.request().cookies().size());
    }

    @Test
    public void testMultiHeaders() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.addHeader("Accept", "text/html");
        req.addHeader("Accept", "application/xhtml+xml");

        List<String> values = req.headers("Accept");
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(req.header("Accept").contains("text/html, application/xhtml+xml"));

        Map<String, String> headerMap = req.headers();
        Assert.assertTrue(headerMap.containsKey("Accept"));
        Assert.assertEquals("text/html", headerMap.get("Accept"));

        Map<String, List<String>> multi = req.multiHeaders();
        Assert.assertEquals(2, multi.get("Accept").size());
    }

    @Test
    public void testKeyValImplementation() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("file", "test.txt", new ByteArrayInputStream("data".getBytes()));
        Assert.assertEquals("file", kv.key());
        Assert.assertEquals("test.txt", kv.value());
        Assert.assertTrue(kv.hasInputStream());
        Assert.assertNotNull(kv.inputStream());
        Assert.assertEquals("file=test.txt", kv.toString());

        kv.contentType("image/png");
        Assert.assertEquals("image/png", kv.contentType());

        HttpConnection.KeyVal kv2 = HttpConnection.KeyVal.create("key2", "val2");
        Assert.assertFalse(kv2.hasInputStream());
        Assert.assertNull(kv2.inputStream());
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testInvalidPostDataCharset() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.postDataCharset("INVALID-CHARSET-NAME");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeout() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.timeout(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMaxBodySize() {
        HttpConnection.Request req = new HttpConnection.Request();
        req.maxBodySize(-5);
    }

    @Test
    public void testResponseProperties() {
        HttpConnection.Response res = new HttpConnection.Response();
        Assert.assertNull(res.charset());
        res.charset("UTF-8");
        Assert.assertEquals("UTF-8", res.charset());
        Assert.assertEquals(0, res.statusCode());
        Assert.assertNull(res.statusMessage());
        Assert.assertNull(res.contentType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUnexecutedResponseThrowsException() throws IOException {
        HttpConnection.Response res = new HttpConnection.Response();
        res.parse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBodyUnexecutedResponseThrowsException() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.body();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBodyAsBytesUnexecutedResponseThrowsException() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.bodyAsBytes();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBodyStreamUnexecutedResponseThrowsException() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.bodyStream();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBufferUpUnexecutedResponseThrowsException() {
        HttpConnection.Response res = new HttpConnection.Response();
        res.bufferUp();
    }

    @Test(expected = MalformedURLException.class)
    public void testExecuteUnsupportedProtocolThrowsException() throws IOException {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("ftp://ftp.example.com"));
        HttpConnection.Response.execute(req);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteRequestBodyInGetThrowsException() throws IOException {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("http://example.com"));
        req.method(Connection.Method.GET);
        req.requestBody("Some Body");
        HttpConnection.Response.execute(req);
    }

    @Test
    public void testProcessResponseHeaders() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new LinkedHashMap<>();
        List<String> setCookie = new ArrayList<>();
        setCookie.add("SID=12345; Path=/; Secure");
        setCookie.add("UID=67890; HttpOnly");
        setCookie.add(null);
        headers.put("Set-Cookie", setCookie);
        headers.put("Content-Type", Collections.singletonList("text/html; charset=UTF-8"));
        headers.put(null, Collections.singletonList("HTTP/1.1 200 OK"));

        res.processResponseHeaders(headers);

        Assert.assertEquals("12345", res.cookie("SID"));
        Assert.assertEquals("67890", res.cookie("UID"));
        Assert.assertEquals("text/html; charset=UTF-8", res.header("Content-Type"));
    }

    @Test
    public void testSerialiseRequestUrl() throws Exception {
        HttpConnection.Request req = new HttpConnection.Request();
        req.url(new URL("http://example.com/test?a=1"));
        req.data(HttpConnection.KeyVal.create("b", "2 2"));

        Method method = HttpConnection.Response.class.getDeclaredMethod("serialiseRequestUrl", Connection.Request.class);
        method.setAccessible(true);
        method.invoke(null, req);

        Assert.assertEquals("http://example.com/test?a=1&b=2+2", req.url().toExternalForm());
        Assert.assertEquals(0, req.data().size());
    }

    @Test
    public void testSetOutputContentType() throws Exception {
        Method method = HttpConnection.Response.class.getDeclaredMethod("setOutputContentType", Connection.Request.class);
        method.setAccessible(true);

        HttpConnection.Request req1 = new HttpConnection.Request();
        String bound1 = (String) method.invoke(null, req1);
        Assert.assertNull(bound1);
        Assert.assertTrue(req1.header("Content-Type").startsWith("application/x-www-form-urlencoded"));

        HttpConnection.Request req2 = new HttpConnection.Request();
        req2.data(HttpConnection.KeyVal.create("file", "a.txt", new ByteArrayInputStream("text".getBytes())));
        String bound2 = (String) method.invoke(null, req2);
        Assert.assertNotNull(bound2);
        Assert.assertTrue(req2.header("Content-Type").contains("multipart/form-data; boundary=" + bound2));

        HttpConnection.Request req3 = new HttpConnection.Request();
        req3.header("Content-Type", "multipart/form-data");
        String bound3 = (String) method.invoke(null, req3);
        Assert.assertNotNull(bound3);
        Assert.assertTrue(req3.header("Content-Type").contains("boundary=" + bound3));
    }

    @Test
    public void testFixHeaderEncoding() throws Exception {
        Method method = HttpConnection.Base.class.getDeclaredMethod("fixHeaderEncoding", String.class);
        method.setAccessible(true);

        String ascii = "Normal Header";
        String resAscii = (String) method.invoke(null, ascii);
        Assert.assertEquals(ascii, resAscii);

        String utf8Str = "Test â‚¬";
        String resUtf8 = (String) method.invoke(null, utf8Str);
        Assert.assertNotNull(resUtf8);
    }

    @Test
    public void testEncodeMimeName() throws Exception {
        Method method = HttpConnection.class.getDeclaredMethod("encodeMimeName", String.class);
        method.setAccessible(true);

        Assert.assertNull(method.invoke(null, (String) null));
        Assert.assertEquals("foo%22bar", method.invoke(null, "foo\"bar"));
        Assert.assertEquals("simple", method.invoke(null, "simple"));
    }
}
