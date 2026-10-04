package com.fasterxml.jackson.databind.deser.std;

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
import org.mockito.Mockito;

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

public class FromStringDeserializerTest {

    @Test
    public void testTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        Assert.assertNotNull(types);
        Assert.assertEquals(12, types.length);
        for (Class<?> cls : types) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(cls);
            Assert.assertNotNull(deser);
            Assert.assertEquals(cls, deser.handledType());
        }
    }

    @Test
    public void testFindDeserializerUnknown() {
        Assert.assertNull(FromStringDeserializer.findDeserializer(Object.class));
        Assert.assertNull(FromStringDeserializer.findDeserializer(String.class));
    }

    @Test
    public void testStdDeserializeFile() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(File.class);
        Object result = deser._deserialize("/tmp/test.txt", null);
        Assert.assertTrue(result instanceof File);
        Assert.assertEquals(new File("/tmp/test.txt"), result);
    }

    @Test
    public void testStdDeserializeURL() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(URL.class);
        Object result = deser._deserialize("http://localhost:8080/test", null);
        Assert.assertTrue(result instanceof URL);
        Assert.assertEquals(new URL("http://localhost:8080/test"), result);
    }

    @Test
    public void testStdDeserializeURI() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(URI.class);
        Object result = deser._deserialize("urn:isbn:0451450523", null);
        Assert.assertTrue(result instanceof URI);
        Assert.assertEquals(URI.create("urn:isbn:0451450523"), result);

        Object emptyResult = deser._deserializeFromEmptyString();
        Assert.assertEquals(URI.create(""), emptyResult);
    }

    @Test
    public void testStdDeserializeClass() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Class.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.doReturn(String.class).when(ctxt).findClass("java.lang.String");
        Object result = deser._deserialize("java.lang.String", ctxt);
        Assert.assertEquals(String.class, result);
    }

    @Test
    public void testStdDeserializeClassException() throws Exception {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Class.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.doThrow(new ClassNotFoundException("Boom")).when(ctxt).findClass("non.existent.Class");
        Mockito.when(ctxt.instantiationException(Mockito.any(), Mockito.any(Throwable.class)))
                .thenReturn(new JsonMappingException(null, "Instantiation error"));
        try {
            deser._deserialize("non.existent.Class", ctxt);
            Assert.fail("Should throw exception");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testStdDeserializeJavaType() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(JavaType.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        TypeFactory tf = TypeFactory.defaultInstance();
        Mockito.when(ctxt.getTypeFactory()).thenReturn(tf);
        Object result = deser._deserialize("java.lang.String", ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof JavaType);
        Assert.assertEquals(tf.constructType(String.class), result);
    }

    @Test
    public void testStdDeserializeCurrency() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Currency.class);
        Object result = deser._deserialize("USD", null);
        Assert.assertEquals(Currency.getInstance("USD"), result);
    }

    @Test
    public void testStdDeserializePattern() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Pattern.class);
        Object result = deser._deserialize("a*b", null);
        Assert.assertTrue(result instanceof Pattern);
        Assert.assertEquals(Pattern.compile("a*b").pattern(), ((Pattern) result).pattern());
    }

    @Test
    public void testStdDeserializeLocale() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Locale.class);
        Assert.assertEquals(new Locale("en"), deser._deserialize("en", null));
        Assert.assertEquals(new Locale("en", "US"), deser._deserialize("en_US", null));
        Assert.assertEquals(new Locale("en", "US", "WIN"), deser._deserialize("en_US_WIN", null));
        Assert.assertEquals(Locale.ROOT, deser._deserializeFromEmptyString());
    }

    @Test
    public void testStdDeserializeCharset() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(Charset.class);
        Assert.assertEquals(Charset.forName("UTF-8"), deser._deserialize("UTF-8", null));
    }

    @Test
    public void testStdDeserializeTimeZone() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(TimeZone.class);
        Assert.assertEquals(TimeZone.getTimeZone("UTC"), deser._deserialize("UTC", null));
    }

    @Test
    public void testStdDeserializeInetAddress() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(InetAddress.class);
        Assert.assertEquals(InetAddress.getByName("127.0.0.1"), deser._deserialize("127.0.0.1", null));
    }

    @Test
    public void testStdDeserializeInetSocketAddress() throws IOException {
        FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(InetSocketAddress.class);
        
        InetSocketAddress addr1 = (InetSocketAddress) deser._deserialize("localhost:8080", null);
        Assert.assertEquals("localhost", addr1.getHostName());
        Assert.assertEquals(8080, addr1.getPort());

        InetSocketAddress addr2 = (InetSocketAddress) deser._deserialize("localhost", null);
        Assert.assertEquals("localhost", addr2.getHostName());
        Assert.assertEquals(0, addr2.getPort());

        InetSocketAddress addr3 = (InetSocketAddress) deser._deserialize("[::1]:9090", null);
        Assert.assertEquals("[::1]", addr3.getHostString());
        Assert.assertEquals(9090, addr3.getPort());

        InetSocketAddress addr4 = (InetSocketAddress) deser._deserialize("[::1]", null);
        Assert.assertEquals("[::1]", addr4.getHostString());
        Assert.assertEquals(0, addr4.getPort());

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        try {
            deser._deserialize("[::1", ctxt);
            Assert.fail("Should have failed invalid IPv6 bracket");
        } catch (InvalidFormatException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStdDeserializeInvalidKind() throws IOException {
        FromStringDeserializer.Std deser = new FromStringDeserializer.Std(Object.class, 999);
        deser._deserialize("test", null);
    }

    @Test
    public void testDeserializeUnwrapSingleValueArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        URI uri = mapper.readValue("[\"http://localhost\"]", URI.class);
        Assert.assertEquals(URI.create("http://localhost"), uri);
    }

    @Test
    public void testDeserializeUnwrapSingleValueArrayExtraTokens() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        try {
            mapper.readValue("[\"http://localhost\", \"http://other\"]", URI.class);
            Assert.fail("Should throw JsonMappingException for extra token");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testDeserializeEmptyStringDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = mapper.readValue("\"\"", File.class);
        Assert.assertNull(file);
    }

    @Test
    public void testDeserializeWeirdStringException() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("\"not_a_valid_currency\"", Currency.class);
            Assert.fail("Should throw exception for invalid currency");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testDeserializeEmbeddedObject() throws IOException {
        FromStringDeserializer<URI> deser = new FromStringDeserializer<URI>(URI.class) {
            @Override
            protected URI _deserialize(String value, DeserializationContext ctxt) {
                return URI.create(value);
            }
        };

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.getValueAsString()).thenReturn(null);
        Mockito.when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_EMBEDDED_OBJECT);

        Mockito.when(parser.getEmbeddedObject()).thenReturn(null);
        Assert.assertNull(deser.deserialize(parser, ctxt));

        URI testUri = URI.create("http://localhost");
        Mockito.when(parser.getEmbeddedObject()).thenReturn(testUri);
        Assert.assertSame(testUri, deser.deserialize(parser, ctxt));

        Mockito.when(parser.getEmbeddedObject()).thenReturn(Integer.valueOf(123));
        Mockito.when(ctxt.mappingException(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(new JsonMappingException(null, "Don't know how to convert"));

        try {
            deser.deserialize(parser, ctxt);
            Assert.fail("Should have thrown mapping exception");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testDeserializeNonEmbeddedNonString() throws IOException {
        FromStringDeserializer<URI> deser = new FromStringDeserializer<URI>(URI.class) {
            @Override
            protected URI _deserialize(String value, DeserializationContext ctxt) {
                return URI.create(value);
            }
        };

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.getValueAsString()).thenReturn(null);
        Mockito.when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NUMBER_INT);
        Mockito.when(ctxt.mappingException(URI.class)).thenReturn(new JsonMappingException(null, "Cannot map"));

        try {
            deser.deserialize(parser, ctxt);
            Assert.fail("Should have thrown mapping exception");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testDeserializeCustomReturnsNullWeirdString() throws IOException {
        FromStringDeserializer<String> deser = new FromStringDeserializer<String>(String.class) {
            @Override
            protected String _deserialize(String value, DeserializationContext ctxt) {
                return null;
            }
        };

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.getValueAsString()).thenReturn("someText");
        Mockito.when(ctxt.weirdStringException(Mockito.anyString(), Mockito.any(), Mockito.anyString()))
                .thenReturn(new JsonMappingException(null, "Weird string"));

        try {
            deser.deserialize(parser, ctxt);
            Assert.fail("Should have thrown weird string exception");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e);
        }
    }
}
