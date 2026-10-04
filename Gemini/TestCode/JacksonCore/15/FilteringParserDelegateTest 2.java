package com.fasterxml.jackson.core.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;

public class FilteringParserDelegateTest {

    private final JsonFactory JSON_F = new JsonFactory();

    @Test
    public void testIncludeAllPassThrough() throws IOException {
        String json = "{\"a\":123,\"b\":[true,false,\"hello\"],\"c\":null}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertSame(TokenFilter.INCLUDE_ALL, parser.getFilter());
        Assert.assertEquals(0, parser.getMatchCount());
        Assert.assertNull(parser.getCurrentToken());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        Assert.assertFalse(parser.hasCurrentToken());
        Assert.assertFalse(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        Assert.assertFalse(parser.hasToken(JsonToken.START_OBJECT));

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertTrue(parser.isExpectedStartObjectToken());
        Assert.assertFalse(parser.isExpectedStartArrayToken());
        Assert.assertTrue(parser.hasCurrentToken());
        Assert.assertTrue(parser.hasToken(JsonToken.START_OBJECT));
        Assert.assertTrue(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, parser.getCurrentTokenId());
        Assert.assertNull(parser.getCurrentName());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("a", parser.getCurrentName());
        Assert.assertEquals("a", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(123, parser.getIntValue());
        Assert.assertEquals(123L, parser.getLongValue());
        Assert.assertEquals(123.0, parser.getDoubleValue(), 0.001);
        Assert.assertEquals(123.0f, parser.getFloatValue(), 0.001f);
        Assert.assertEquals((byte) 123, parser.getByteValue());
        Assert.assertEquals((short) 123, parser.getShortValue());
        Assert.assertEquals(BigInteger.valueOf(123), parser.getBigIntegerValue());
        Assert.assertEquals(BigDecimal.valueOf(123), parser.getDecimalValue());
        Assert.assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        Assert.assertEquals(Integer.valueOf(123), parser.getNumberValue());
        Assert.assertEquals(123, parser.getValueAsInt());
        Assert.assertEquals(123, parser.getValueAsInt(456));
        Assert.assertEquals(123L, parser.getValueAsLong());
        Assert.assertEquals(123L, parser.getValueAsLong(456L));
        Assert.assertEquals(123.0, parser.getValueAsDouble(), 0.001);
        Assert.assertEquals(123.0, parser.getValueAsDouble(456.0), 0.001);
        Assert.assertFalse(parser.getValueAsBoolean());
        Assert.assertFalse(parser.getValueAsBoolean(true));
        Assert.assertEquals("123", parser.getValueAsString());
        Assert.assertEquals("123", parser.getValueAsString("def"));

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("b", parser.getCurrentName());

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertTrue(parser.isExpectedStartArrayToken());
        Assert.assertEquals("b", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        Assert.assertTrue(parser.getBooleanValue());
        Assert.assertTrue(parser.getValueAsBoolean());

        Assert.assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        Assert.assertFalse(parser.getBooleanValue());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("hello", parser.getText());
        Assert.assertTrue(parser.hasTextCharacters());
        Assert.assertNotNull(parser.getTextCharacters());
        Assert.assertEquals(5, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertNull(parser.getEmbeddedObject());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("c", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        p.close();
        parser.close();
    }

    @Test
    public void testClearCurrentTokenAndContext() throws IOException {
        String json = "{\"x\": 1}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertNull(parser.getLastClearedToken());
        parser.clearCurrentToken();
        Assert.assertNull(parser.getCurrentToken());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());

        // Calling clear again when current token is null does not overwrite last cleared
        parser.clearCurrentToken();
        Assert.assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("x", parser.getCurrentName());

        JsonStreamContext ctxt = parser.getParsingContext();
        Assert.assertNotNull(ctxt);
        Assert.assertEquals("x", ctxt.getCurrentName());

        JsonLocation loc = parser.getCurrentLocation();
        Assert.assertNotNull(loc);
        JsonLocation tokLoc = parser.getTokenLocation();
        Assert.assertNotNull(tokLoc);

        p.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOverrideCurrentNameThrows() throws IOException {
        String json = "{\"x\": 1}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        parser.nextToken();
        try {
            parser.overrideCurrentName("y");
        } finally {
            p.close();
        }
    }

    @Test
    public void testFilterIncludePropertyWithPath() throws IOException {
        String json = "{\"a\":1,\"b\":{\"c\":2,\"d\":3},\"e\":4}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("b".equals(name)) {
                    return new TokenFilter() {
                        @Override
                        public TokenFilter includeProperty(String nestedName) {
                            if ("d".equals(nestedName)) {
                                return TokenFilter.INCLUDE_ALL;
                            }
                            return null;
                        }
                    };
                }
                return null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("b", parser.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("d", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(3, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testFilterIncludePropertyNoPath() throws IOException {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "b".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, false, true);

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("b", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(2, parser.getIntValue());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testFilterArrayElements() throws IOException {
        String json = "[10, 20, 30, 40]";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return this;
            }

            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                int val = p.getIntValue();
                return val == 20 || val == 40;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(20, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(40, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testFilterStartArrayIncludeAllDirectly() throws IOException {
        String json = "{\"arr\": [1, 2, 3]}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("arr".equals(name)) {
                    return new TokenFilter() {
                        @Override
                        public TokenFilter filterStartArray() {
                            return TokenFilter.INCLUDE_ALL;
                        }
                    };
                }
                return null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("arr", parser.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(1, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(2, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(3, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testFilterStartObjectIncludeAllDirectly() throws IOException {
        String json = "{\"obj\": {\"x\": 1}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("obj".equals(name)) {
                    return new TokenFilter() {
                        @Override
                        public TokenFilter filterStartObject() {
                            return TokenFilter.INCLUDE_ALL;
                        }
                    };
                }
                return null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("obj", parser.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("x", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(1, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testNextValue() throws IOException {
        String json = "{\"a\": 1, \"b\": 2}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextValue());
        Assert.assertEquals(1, parser.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextValue());
        Assert.assertEquals(2, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextValue());
        Assert.assertNull(parser.nextValue());

        p.close();
    }

    @Test
    public void testSkipChildrenOnObjectAndArray() throws IOException {
        String json = "{\"skipMe\": {\"a\": 1, \"b\": [2, 3]}, \"keepMe\": 42}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("skipMe", parser.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        parser.skipChildren();
        Assert.assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("keepMe", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(42, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testSkipChildrenOnScalarDoesNothing() throws IOException {
        String json = "{\"a\": 1, \"b\": 2}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertSame(parser, parser.skipChildren());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());

        p.close();
    }

    @Test
    public void testFilterCompletelyExcludesSubtree() throws IOException {
        String json = "{\"exclude\": {\"nested\": [1, 2, 3]}, \"include\": 99}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("include".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, false, true);

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("include", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(99, parser.getIntValue());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testFilterRootScalar() throws IOException {
        String json = "12345";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(12345, parser.getIntValue());
        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testFilterRootScalarExcluded() throws IOException {
        String json = "12345";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) {
                return false;
            }
        };
        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertNull(parser.nextToken());

        p.close();
    }

    @Test
    public void testBinaryAndBase64Methods() throws IOException {
        String base64Data = "SGVsbG8gV29ybGQ="; // "Hello World"
        String json = "\"" + base64Data + "\"";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate parser = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals("Hello World".getBytes("UTF-8"), binary);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);
        Assert.assertEquals(11, bytesRead);
        Assert.assertArrayEquals("Hello World".getBytes("UTF-8"), out.toByteArray());

        p.close();
    }

    @Test
    public void testIncludeImmediateParentBehavior() throws IOException {
        String json = "{\"parent\": {\"child\": 100}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("child".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return this;
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, false, true);
        parser._includeImmediateParent = true;

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("child", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(100, parser.getIntValue());

        p.close();
    }

    @Test
    public void testEmptyContainersWithBuffering() throws IOException {
        String json = "{\"emptyObj\": {}, \"emptyArr\": [], \"keep\": 1}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("keep".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return new TokenFilter() {
                    @Override
                    public TokenFilter filterStartObject() {
                        return this;
                    }

                    @Override
                    public TokenFilter filterStartArray() {
                        return this;
                    }
                };
            }
        };

        FilteringParserDelegate parser = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("keep", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        Assert.assertEquals(1, parser.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());

        p.close();
    }
}
