package com.fasterxml.jackson.databind.deser.std;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Assert;
import org.junit.Test;

public class FromStringDeserializerTest {

    @Test
    public void testTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        Assert.assertNotNull(types);
        Assert.assertEquals(12, types.length);
        for (Class<?> type : types) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(type);
            Assert.assertNotNull(deser);
        }
    }

    @Test
    public void testFindDeserializerUnknown() {
        Assert.assertNull(FromStringDeserializer.findDeserializer(Object.class));
        Assert.assertNull(FromStringDeserializer.findDeserializer(String.class));
    }

    @Test
    public void testDeserializeFile() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(File.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("/tmp/test.txt", ctxt);
        Assert.assertEquals(new File("/tmp/test.txt"), result);
    }

    @Test
    public void testDeserializeURL() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(URL.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("http://localhost:8080/test", ctxt);
        Assert.assertEquals(new URL("http://localhost:8080/test"), result);
    }

    @Test
    public void testDeserializeURI() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(URI.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("http://localhost:8080/test", ctxt);
        Assert.assertEquals(URI.create("http://localhost:8080/test"), result);
        Assert.assertEquals(URI.create(""), deser._deserializeFromEmptyString());
    }

    @Test
    public void testDeserializeClass() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Class.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("java.lang.String", ctxt);
        Assert.assertEquals(String.class, result);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeClassInvalid() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Class.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser._deserialize("non.existing.ClassName", ctxt);
    }

    @Test
    public void testDeserializeJavaType() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(JavaType.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("java.util.List<java.lang.String>", ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof JavaType);
        Assert.assertEquals(TypeFactory.defaultInstance().constructFromCanonical("java.util.List<java.lang.String>"), result);
    }

    @Test
    public void testDeserializeCurrency() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Currency.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("USD", ctxt);
        Assert.assertEquals(Currency.getInstance("USD"), result);
    }

    @Test
    public void testDeserializePattern() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Pattern.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("a*b", ctxt);
        Assert.assertTrue(result instanceof Pattern);
        Assert.assertEquals("a*b", ((Pattern) result).pattern());
    }

    @Test
    public void testDeserializeLocale() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Locale.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Assert.assertEquals(new Locale("en"), deser._deserialize("en", ctxt));
        Assert.assertEquals(new Locale("en", "US"), deser._deserialize("en_US", ctxt));
        Assert.assertEquals(new Locale("en", "US", "VAR"), deser._deserialize("en_US_VAR", ctxt));
        Assert.assertNull(deser._deserializeFromEmptyString());
    }

    @Test
    public void testDeserializeCharset() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Charset.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("UTF-8", ctxt);
        Assert.assertEquals(Charset.forName("UTF-8"), result);
    }

    @Test
    public void testDeserializeTimeZone() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(TimeZone.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("UTC", ctxt);
        Assert.assertEquals(TimeZone.getTimeZone("UTC"), result);
    }

    @Test
    public void testDeserializeInetAddress() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(InetAddress.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deser._deserialize("127.0.0.1", ctxt);
        Assert.assertEquals(InetAddress.getByName("127.0.0.1"), result);
    }

    @Test
    public void testDeserializeInetSocketAddress() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(InetSocketAddress.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        InetSocketAddress addr1 = (InetSocketAddress) deser._deserialize("127.0.0.1:8080", ctxt);
        Assert.assertEquals(8080, addr1.getPort());
        Assert.assertEquals("127.0.0.1", addr1.getHostName());

        InetSocketAddress addr2 = (InetSocketAddress) deser._deserialize("127.0.0.1", ctxt);
        Assert.assertEquals(0, addr2.getPort());

        InetSocketAddress addr3 = (InetSocketAddress) deser._deserialize("[::1]:9090", ctxt);
        Assert.assertEquals(9090, addr3.getPort());
        Assert.assertEquals("[::1]", addr3.getHostString());

        InetSocketAddress addr4 = (InetSocketAddress) deser._deserialize("[::1]", ctxt);
        Assert.assertEquals(0, addr4.getPort());
        Assert.assertEquals("[::1]", addr4.getHostString());

        InetSocketAddress addr5 = (InetSocketAddress) deser._deserialize("2001:db8::1", ctxt);
        Assert.assertEquals(0, addr5.getPort());
    }

    @Test(expected = InvalidFormatException.class)
    public void testDeserializeMalformedBracketedIPv6() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(InetSocketAddress.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser._deserialize("[::1", ctxt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidKind() throws Exception {
        FromStringDeserializer.Std deser = new FromStringDeserializer.Std(Object.class, -1);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser._deserialize("test", ctxt);
    }

    @Test
    public void testDeserializeThroughObjectMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        URI uri = mapper.readValue("\"http://localhost:8080\"", URI.class);
        Assert.assertEquals(URI.create("http://localhost:8080"), uri);

        URI emptyUri = mapper.readValue("\"\"", URI.class);
        Assert.assertEquals(URI.create(""), emptyUri);

        Currency nullCurrency = mapper.readValue("\"   \"", Currency.class);
        Assert.assertNull(nullCurrency);
    }

    @Test
    public void testDeserializeArrayUnwrapping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        URI uri = mapper.readValue("[\"http://localhost:8080\"]", URI.class);
        Assert.assertEquals(URI.create("http://localhost:8080"), uri);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeArrayUnwrappingTooManyElements() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        mapper.readValue("[\"http://localhost:8080\", \"http://localhost:8081\"]", URI.class);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeArrayWithoutUnwrappingEnabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        mapper.readValue("[\"http://localhost:8080\"]", URI.class);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeInvalidStringValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("\"NOT_A_VALID_CURRENCY_CODE_12345\"", Currency.class);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeNullTokenFails() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("12345", Pattern.class);
    }

    @Test
    public void testCustomDeserializerEmbeddedObject() throws Exception {
        FromStringDeserializer<URI> deser = new FromStringDeserializer<URI>(URI.class) {
            @Override
            protected URI _deserialize(String value, DeserializationContext ctxt) {
                return URI.create(value);
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser jpNull = mapper.getFactory().createParser("");
        jpNull.nextToken();
        Assert.assertNull(deser._deserializeEmbedded(null, ctxt) == null ? null : new Object());

        try {
            deser._deserializeEmbedded(Integer.valueOf(123), ctxt);
            Assert.fail("Should have failed");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Don't know how to convert embedded Object"));
        }
    }
}
