package org.jsoup.helper;

import org.jsoup.Connection;
import org.jsoup.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpConnectionTest {

    @Test
    public void testConnectString() {
        Connection con = HttpConnection.connect("http://example.com");
        Assert.assertNotNull(con);
        Assert.assertEquals("http://example.com", con.request().url().toExternalForm());
    }

    @Test
    public void testConnectUrl() throws MalformedURLException {
        URL url = new URL("http://example.com/test");
        Connection con = HttpConnection.connect(url);
        Assert.assertNotNull(con);
        Assert.assertEquals(url, con.request().url());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectNullUrlString() {
        HttpConnection.connect((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectEmptyUrlString() {
        HttpConnection.connect("   ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectMalformedUrlString() {
        HttpConnection.connect("htp://bad-scheme");
    }

    @Test
    public void testUrlWithSpacesEncoding() {
        Connection con = HttpConnection.connect("http://example.com/path with spaces/");
        Assert.assertEquals("http://example.com/path%20with%20spaces/", con.request().url().toExternalForm());
    }

    @Test
    public void testFluentConfiguration() throws MalformedURLException {
        URL url = new URL("http://example.com/path");
        Connection con = HttpConnection.connect(url);

        con.userAgent("Mozilla/5.0")
           .timeout(5000)
           .maxBodySize(2048)
           .followRedirects(false)
           .referrer("http://google.com")
           .method(Connection.Method.POST)
           .ignoreHttpErrors(true)
           .ignoreContentType(true)
           .validateTLSCertificates(false)
           .postDataCharset("UTF-8");

        Connection.Request req = con.request();
        Assert.assertEquals("Mozilla/5.0", req.header("User-Agent"));
        Assert.assertEquals(5000, req.timeout());
        Assert.assertEquals(2048, req.maxBodySize());
        Assert.assertFalse(req.followRedirects());
        Assert.assertEquals("http://google.com", req.header("Referer"));
        Assert.assertEquals(Connection.Method.POST, req.method());
        Assert.assertTrue(req.ignoreHttpErrors());
        Assert.assertTrue(req.ignoreContentType());
        Assert.assertFalse(req.validateTLSCertificates());
        Assert.assertEquals("UTF-8", req.postDataCharset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullUserAgent() {
        HttpConnection.connect("http://example.com").userAgent(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullReferrer() {
        HttpConnection.connect("http://example.com").referrer(null);
    }

    @Test
    public void testDataStringString() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data("k1", "v1").data("k2", "v2");
        List<Connection.KeyVal> data = (List<Connection.KeyVal>) con.request().data();
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("k1", data.get(0).key());
        Assert.assertEquals("v1", data.get(0).value());
        Assert.assertEquals("k2", data.get(1).key());
        Assert.assertEquals("v2", data.get(1).value());
    }

    @Test
    public void testDataInputStream() {
        InputStream in = new ByteArrayInputStream("test".getBytes());
        Connection con = HttpConnection.connect("http://example.com");
        con.data("file", "upload.txt", in);
        List<Connection.KeyVal> data = (List<Connection.KeyVal>) con.request().data();
        Assert.assertEquals(1, data.size());
        Connection.KeyVal kv = data.get(0);
        Assert.assertEquals("file", kv.key());
        Assert.assertEquals("upload.txt", kv.value());
        Assert.assertTrue(kv.hasInputStream());
        Assert.assertSame(in, kv.inputStream());
    }

    @Test
    public void testDataMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("keyA", "valA");
        map.put("keyB", "valB");
        Connection con = HttpConnection.connect("http://example.com");
        con.data(map);
        Assert.assertEquals(2, con.request().data().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataNullMap() {
        HttpConnection.connect("http://example.com").data((Map<String, String>) null);
    }

    @Test
    public void testDataVarargs() {
        Connection con = HttpConnection.connect("http://example.com");
        con.data("k1", "v1", "k2", "v2");
        List<Connection.KeyVal> data = (List<Connection.KeyVal>) con.request().data();
        Assert.assertEquals(2, data.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataVarargsOddCount() {
        HttpConnection.connect("http://example.com").data("k1", "v1", "k2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataVarargsNullArray() {
        HttpConnection.connect("http://example.com").data((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataVarargsEmptyKey() {
        HttpConnection.connect("http://example.com").data("", "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataVarargsNullValue() {
        HttpConnection.connect("http://example.com").data("key", null);
    }

    @Test
    public void testDataCollection() {
        List<Connection.KeyVal> list = new ArrayList<Connection.KeyVal>();
        list.add(HttpConnection.KeyVal.create("k1", "v1"));
        list.add(HttpConnection.KeyVal.create("k2", "v2"));
        Connection con = HttpConnection.connect("http://example.com");
        con.data(list);
        Assert.assertEquals(2, con.request().data().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDataCollectionNull() {
        HttpConnection.connect("http://example.com").data((List<Connection.KeyVal>) null);
    }

    @Test
    public void testHeadersAndCookies() {
        Connection con = HttpConnection.connect("http://example.com");
        con.header("Accept", "text/html");
        con.cookie("session", "abc");
        Map<String, String> cookies = new HashMap<String, String>();
        cookies.put("c1", "v1");
        cookies.put("c2", "v2");
        con.cookies(cookies);

        Assert.assertEquals("text/html", con.request().header("Accept"));
        Assert.assertEquals("abc", con.request().cookie("session"));
        Assert.assertEquals("v1", con.request().cookie("c1"));
        Assert.assertEquals("v2", con.request().cookie("c2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCookiesMap() {
        HttpConnection.connect("http://example.com").cookies(null);
    }

    @Test
    public void testParser() {
        Parser parser = Parser.xmlParser();
        Connection con = HttpConnection.connect("http://example.com");
        con.parser(parser);
        Assert.assertSame(parser, con.request().parser());
    }

    @Test
    public void testRequestAndResponseHolders() {
        Connection con = HttpConnection.connect("http://example.com");
        Connection.Request req = con.request();
        Connection.Response res = con.response();
        Assert.assertNotNull(req);
        Assert.assertNotNull(res);

        con.request(req);
        con.response(res);
        Assert.assertSame(req, con.request());
        Assert.assertSame(res, con.response());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestNegativeTimeout() {
        Connection con = HttpConnection.connect("http://example.com");
        con.timeout(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestNegativeMaxBodySize() {
        Connection con = HttpConnection.connect("http://example.com");
        con.maxBodySize(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestNullKeyValData() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.data(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestNullPostDataCharset() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.postDataCharset(null);
    }

    @Test(expected = IllegalCharsetNameException.class)
    public void testRequestUnsupportedPostDataCharset() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.postDataCharset("unsupported-charset-name-xyz");
    }

    @Test
    public void testBaseHeaderOperations() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.header("Content-Type", "text/plain");
        Assert.assertTrue(req.hasHeader("Content-Type"));
        Assert.assertTrue(req.hasHeader("content-type"));
        Assert.assertTrue(req.hasHeader("CONTENT-TYPE"));
        Assert.assertTrue(req.hasHeaderWithValue("content-type", "text/plain"));
        Assert.assertFalse(req.hasHeaderWithValue("content-type", "text/html"));
        Assert.assertFalse(req.hasHeaderWithValue("Non-Existent", "val"));

        req.header("content-type", "text/html");
        Assert.assertEquals("text/html", req.header("Content-Type"));
        Assert.assertEquals("text/html", req.header("content-type"));

        req.removeHeader("Content-Type");
        Assert.assertFalse(req.hasHeader("Content-Type"));
        Assert.assertNull(req.header("Content-Type"));

        req.removeHeader("Non-Existent");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseHeaderNullName() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.header(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseHeaderEmptyName() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.header("", "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseHeaderNullValue() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.header("Name", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseHasHeaderEmpty() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.hasHeader("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseRemoveHeaderEmpty() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.removeHeader("");
    }

    @Test
    public void testBaseCookieOperations() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.cookie("session_id", "xyz123");
        Assert.assertTrue(req.hasCookie("session_id"));
        Assert.assertEquals("xyz123", req.cookie("session_id"));
        Assert.assertFalse(req.hasCookie("other"));
        Assert.assertNull(req.cookie("other"));

        Assert.assertEquals(1, req.cookies().size());

        req.removeCookie("session_id");
        Assert.assertFalse(req.hasCookie("session_id"));
        Assert.assertNull(req.cookie("session_id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseCookieNullName() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.cookie(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseCookieEmptyName() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.cookie("", "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseCookieNullValue() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.cookie("name", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseHasCookieEmpty() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.hasCookie("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseRemoveCookieEmpty() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.removeCookie("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseNullUrl() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.url(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBaseNullMethod() {
        Connection.Request req = HttpConnection.connect("http://example.com").request();
        req.method(null);
    }

    @Test
    public void testResponseUnexecutedState() {
        HttpConnection.Response res = new HttpConnection.Response();
        Assert.assertEquals(0, res.statusCode());
        Assert.assertNull(res.statusMessage());
        Assert.assertNull(res.charset());
        Assert.assertNull(res.contentType());

        try {
            res.parse();
            Assert.fail();
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
            Assert.fail();
        }

        try {
            res.body();
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            res.bodyAsBytes();
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testProcessResponseHeaders() {
        HttpConnection.Response res = new HttpConnection.Response();
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Set-Cookie", Collections.singletonList("token=12345; Path=/; HttpOnly"));
        headers.put("Content-Type", Collections.singletonList("text/html; charset=UTF-8"));
        headers.put(null, Collections.singletonList("HTTP/1.1 200 OK"));
        headers.put("X-Empty-List", Collections.<String>emptyList());

        res.processResponseHeaders(headers);

        Assert.assertTrue(res.hasCookie("token"));
        Assert.assertEquals("12345", res.cookie("token"));
        Assert.assertEquals("text/html; charset=UTF-8", res.header("Content-Type"));
        Assert.assertFalse(res.hasHeader("X-Empty-List"));
    }

    @Test
    public void testKeyValCreationAndModification() {
        HttpConnection.KeyVal kv = HttpConnection.KeyVal.create("key1", "val1");
        Assert.assertEquals("key1", kv.key());
        Assert.assertEquals("val1", kv.value());
        Assert.assertFalse(kv.hasInputStream());
        Assert.assertNull(kv.inputStream());
        Assert.assertEquals("key1=val1", kv.toString());

        kv.key("key2");
        kv.value("val2");
        Assert.assertEquals("key2", kv.key());
        Assert.assertEquals("val2", kv.value());

        InputStream in = new ByteArrayInputStream("data".getBytes());
        HttpConnection.KeyVal kvFile = HttpConnection.KeyVal.create("fileKey", "file.bin", in);
        Assert.assertEquals("fileKey", kvFile.key());
        Assert.assertEquals("file.bin", kvFile.value());
        Assert.assertTrue(kvFile.hasInputStream());
        Assert.assertSame(in, kvFile.inputStream());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyValEmptyKey() {
        HttpConnection.KeyVal.create("", "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyValNullValue() {
        HttpConnection.KeyVal.create("key", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExecuteUnsupportedProtocol() throws IOException {
        Connection con = HttpConnection.connect("ftp://example.com/file.txt");
        con.execute();
    }
}
