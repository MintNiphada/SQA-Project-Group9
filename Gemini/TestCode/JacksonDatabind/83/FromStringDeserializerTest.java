package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class FromStringDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        Assert.assertNotNull(types);
        Assert.assertEquals(13, types.length);
    }

    @Test
    public void testFindDeserializerSupportedTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        for (Class<?> cls : types) {
            FromStringDeserializer.Std deser = FromStringDeserializer.findDeserializer(cls);
            Assert.assertNotNull(deser);
            Assert.assertEquals(cls, deser.handledType());
        }
        Assert.assertNull(FromStringDeserializer.findDeserializer(Object.class));
        Assert.assertNull(FromStringDeserializer.findDeserializer(String.class));
    }

    @Test
    public void testDeserializeFile() throws Exception {
        File file = mapper.readValue("\"/tmp/test.txt\"", File.class);
        Assert.assertEquals(new File("/tmp/test.txt"), file);
    }

    @Test
    public void testDeserializeURL() throws Exception {
        URL url = mapper.readValue("\"http://localhost:8080/test\"", URL.class);
        Assert.assertEquals(new URL("http://localhost:8080/test"), url);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeURLInvalid() throws Exception {
        mapper.readValue("\"not_a_valid_url\"", URL.class);
    }

    @Test
    public void testDeserializeURI() throws Exception {
        URI uri = mapper.readValue("\"urn:isbn:0451450523\"", URI.class);
        Assert.assertEquals(URI.create("urn:isbn:0451450523"), uri);

        URI emptyUri = mapper.readValue("\"\"", URI.class);
        Assert.assertEquals(URI.create(""), emptyUri);
    }

    @Test
    public void testDeserializeClass() throws Exception {
        Class<?> cls = mapper.readValue("\"java.lang.String\"", Class.class);
        Assert.assertEquals(String.class, cls);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeClassInvalid() throws Exception {
        mapper.readValue("\"non.existent.Class12345\"", Class.class);
    }

    @Test
    public void testDeserializeJavaType() throws Exception {
        JavaType type = mapper.readValue("\"java.util.List<java.lang.String>\"", JavaType.class);
        Assert.assertNotNull(type);
        Assert.assertTrue(type.isCollectionLikeType());
    }

    @Test
    public void testDeserializeCurrency() throws Exception {
        Currency curr = mapper.readValue("\"USD\"", Currency.class);
        Assert.assertEquals(Currency.getInstance("USD"), curr);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeCurrencyInvalid() throws Exception {
        mapper.readValue("\"INVALID_CURR\"", Currency.class);
    }

    @Test
    public void testDeserializePattern() throws Exception {
        Pattern pattern = mapper.readValue("\"[a-z]+\"", Pattern.class);
        Assert.assertEquals("[a-z]+", pattern.pattern());
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializePatternInvalid() throws Exception {
        mapper.readValue("\"[a-z\"", Pattern.class);
    }

    @Test
    public void testDeserializeLocale() throws Exception {
        Locale l1 = mapper.readValue("\"en\"", Locale.class);
        Assert.assertEquals(new Locale("en"), l1);

        Locale l2 = mapper.readValue("\"en_US\"", Locale.class);
        Assert.assertEquals(new Locale("en", "US"), l2);

        Locale l3 = mapper.readValue("\"en-US-variant\"", Locale.class);
        Assert.assertEquals(new Locale("en", "US", "variant"), l3);

        Locale l4 = mapper.readValue("\"en_US_variant_extra\"", Locale.class);
        Assert.assertEquals(new Locale("en", "US", "variant_extra"), l4);

        Locale lEmpty = mapper.readValue("\"\"", Locale.class);
        Assert.assertEquals(Locale.ROOT, lEmpty);
    }

    @Test
    public void testDeserializeCharset() throws Exception {
        Charset charset = mapper.readValue("\"UTF-8\"", Charset.class);
        Assert.assertEquals(Charset.forName("UTF-8"), charset);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeCharsetInvalid() throws Exception {
        mapper.readValue("\"INVALID_CHARSET_NAME\"", Charset.class);
    }

    @Test
    public void testDeserializeTimeZone() throws Exception {
        TimeZone tz = mapper.readValue("\"PST\"", TimeZone.class);
        Assert.assertEquals(TimeZone.getTimeZone("PST"), tz);
    }

    @Test
    public void testDeserializeInetAddress() throws Exception {
        InetAddress addr = mapper.readValue("\"127.0.0.1\"", InetAddress.class);
        Assert.assertEquals(InetAddress.getByName("127.0.0.1"), addr);
    }

    @Test
    public void testDeserializeInetSocketAddress() throws Exception {
        InetSocketAddress sa1 = mapper.readValue("\"localhost:8080\"", InetSocketAddress.class);
        Assert.assertEquals("localhost", sa1.getHostString());
        Assert.assertEquals(8080, sa1.getPort());

        InetSocketAddress sa2 = mapper.readValue("\"127.0.0.1\"", InetSocketAddress.class);
        Assert.assertEquals("127.0.0.1", sa2.getHostString());
        Assert.assertEquals(0, sa2.getPort());

        InetSocketAddress sa3 = mapper.readValue("\"[2001:db8::1]:9090\"", InetSocketAddress.class);
        Assert.assertEquals("[2001:db8::1]", sa3.getHostString());
        Assert.assertEquals(9090, sa3.getPort());

        InetSocketAddress sa4 = mapper.readValue("\"[2001:db8::1]\"", InetSocketAddress.class);
        Assert.assertEquals("[2001:db8::1]", sa4.getHostString());
        Assert.assertEquals(0, sa4.getPort());

        InetSocketAddress sa5 = mapper.readValue("\"2001:db8::1\"", InetSocketAddress.class);
        Assert.assertEquals("2001:db8::1", sa5.getHostString());
        Assert.assertEquals(0, sa5.getPort());
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeInetSocketAddressInvalidBracket() throws Exception {
        mapper.readValue("\"[2001:db8::1:8080\"", InetSocketAddress.class);
    }

    @Test
    public void testDeserializeStringBuilder() throws Exception {
        StringBuilder sb = mapper.readValue("\"hello world\"", StringBuilder.class);
        Assert.assertEquals("hello world", sb.toString());

        StringBuilder sbEmpty = mapper.readValue("\"\"", StringBuilder.class);
        Assert.assertEquals("", sbEmpty.toString());

        StringBuilder sbWhitespace = mapper.readValue("\"   \"", StringBuilder.class);
        Assert.assertEquals("", sbWhitespace.toString());
    }

    @Test
    public void testDeserializeEmptyStringDefaultNull() throws Exception {
        File file = mapper.readValue("\"\"", File.class);
        Assert.assertNull(file);

        Currency currency = mapper.readValue("\"   \"", Currency.class);
        Assert.assertNull(currency);
    }

    @Test
    public void testDeserializeArrayFeature() throws Exception {
        ObjectMapper unwrapMapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        File file = unwrapMapper.readValue("[\"/tmp/file.txt\"]", File.class);
        Assert.assertEquals(new File("/tmp/file.txt"), file);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeUnexpectedToken() throws Exception {
        mapper.readValue("12345", File.class);
    }

    @Test
    public void testStdInvalidKindThrowsException() {
        FromStringDeserializer.Std deser = new FromStringDeserializer.Std(String.class, 9999);
        try {
            DeserializationContext ctxt = mapper.getDeserializationContext();
            deser._deserialize("test", ctxt);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Internal error"));
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test
    public void testCustomFromStringDeserializer() throws Exception {
        FromStringDeserializer<Integer> custom = new FromStringDeserializer<Integer>(Integer.class) {
            @Override
            protected Integer _deserialize(String value, DeserializationContext ctxt) throws IOException {
                return Integer.parseInt(value);
            }
        };

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser parser = mapper.getFactory().createParser("\"42\"");
        parser.nextToken();
        Integer result = custom.deserialize(parser, ctxt);
        Assert.assertEquals(Integer.valueOf(42), result);
    }

    @Test
    public void testCustomFromStringDeserializerNullResult() throws Exception {
        FromStringDeserializer<Integer> custom = new FromStringDeserializer<Integer>(Integer.class) {
            @Override
            protected Integer _deserialize(String value, DeserializationContext ctxt) throws IOException {
                return null;
            }
        };

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser parser = mapper.getFactory().createParser("\"42\"");
        parser.nextToken();
        try {
            custom.deserialize(parser, ctxt);
            Assert.fail("Expected JsonMappingException on null result");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("not a valid textual representation"));
        }
    }

    @Test
    public void testCustomFromStringDeserializerEmbeddedObject() throws Exception {
        final Integer embedded = 100;
        FromStringDeserializer<Integer> custom = new FromStringDeserializer<Integer>(Integer.class) {
            @Override
            protected Integer _deserialize(String value, DeserializationContext ctxt) {
                return null;
            }
        };

        JsonParser parser = new com.fasterxml.jackson.core.util.JsonParserDelegate(mapper.getFactory().createParser("")) {
            @Override
            public String getValueAsString() {
                return null;
            }

            @Override
            public JsonToken getCurrentToken() {
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            }

            @Override
            public Object getEmbeddedObject() {
                return embedded;
            }
        };

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Integer res = custom.deserialize(parser, ctxt);
        Assert.assertEquals(embedded, res);

        JsonParser nullParser = new com.fasterxml.jackson.core.util.JsonParserDelegate(mapper.getFactory().createParser("")) {
            @Override
            public String getValueAsString() {
                return null;
            }

            @Override
            public JsonToken getCurrentToken() {
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            }

            @Override
            public Object getEmbeddedObject() {
                return null;
            }
        };
        Assert.assertNull(custom.deserialize(nullParser, ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testCustomFromStringDeserializerIncompatibleEmbeddedObject() throws Exception {
        FromStringDeserializer<Integer> custom = new FromStringDeserializer<Integer>(Integer.class) {
            @Override
            protected Integer _deserialize(String value, DeserializationContext ctxt) {
                return null;
            }
        };

        JsonParser parser = new com.fasterxml.jackson.core.util.JsonParserDelegate(mapper.getFactory().createParser("")) {
            @Override
            public String getValueAsString() {
                return null;
            }

            @Override
            public JsonToken getCurrentToken() {
                return JsonToken.VALUE_EMBEDDED_OBJECT;
            }

            @Override
            public Object getEmbeddedObject() {
                return new File("/tmp");
            }
        };

        mapper.readValue(parser, Integer.class);
        custom.deserialize(parser, mapper.getDeserializationContext());
    }

    @Test
    public void testMalformedURLExceptionBranch() throws Exception {
        FromStringDeserializer<URL> deser = new FromStringDeserializer<URL>(URL.class) {
            @Override
            protected URL _deserialize(String value, DeserializationContext ctxt) throws IOException {
                throw new MalformedURLException("Malformed test URL");
            }
        };

        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser parser = mapper.getFactory().createParser("\"http://bad url\"");
        parser.nextToken();
        try {
            deser.deserialize(parser, ctxt);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("problem: Malformed test URL"));
        }
    }
}
