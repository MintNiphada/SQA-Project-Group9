package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class FilteringParserDelegateTest {

    private final JsonFactory _jsonFactory = new JsonFactory();

    @Test
    public void testIncludeAllFilterBasicTraversal() throws IOException {
        String json = "{\"a\":123,\"b\":[true,false,\"hello\"],\"c\":null}";
        JsonParser p = _jsonFactory.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertSame(TokenFilter.INCLUDE_ALL, fp.getFilter());
        Assert.assertEquals(0, fp.getMatchCount());

        Assert.assertNull(fp.getCurrentToken());
        Assert.assertNull(fp.currentToken());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, fp.getCurrentTokenId());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, fp.currentTokenId());
        Assert.assertFalse(fp.hasCurrentToken());
        Assert.assertTrue(fp.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        Assert.assertFalse(fp.hasToken(JsonToken.START_OBJECT));

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertTrue(fp.isExpectedStartObjectToken());
        Assert.assertFalse(fp.isExpectedStartArrayToken());
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, fp.getCurrentTokenId());
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, fp.currentTokenId());
        Assert.assertTrue(fp.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertTrue(fp.hasToken(JsonToken.START_OBJECT));
        Assert.assertTrue(fp.hasCurrentToken());
        Assert.assertNull(fp.getCurrentName());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("a", fp.getCurrentName());
        Assert.assertEquals("a", fp.getText());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(123, fp.getIntValue());
        Assert.assertEquals(123L, fp.getLongValue());
        Assert.assertEquals((short) 123, fp.getShortValue());
        Assert.assertEquals((byte) 123, fp.getByteValue());
        Assert.assertEquals(123.0f, fp.getFloatValue(), 0.0f);
        Assert.assertEquals(123.0, fp.getDoubleValue(), 0.0);
        Assert.assertEquals(BigInteger.valueOf(123), fp.getBigIntegerValue());
        Assert.assertEquals(BigDecimal.valueOf(123), fp.getDecimalValue());
        Assert.assertEquals(JsonParser.NumberType.INT, fp.getNumberType());
        Assert.assertEquals(123, fp.getNumberValue());
        Assert.assertEquals(123, fp.getValueAsInt());
        Assert.assertEquals(123, fp.getValueAsInt(42));
        Assert.assertEquals(123L, fp.getValueAsLong());
        Assert.assertEquals(123L, fp.getValueAsLong(42L));
        Assert.assertEquals(123.0, fp.getValueAsDouble(), 0.0);
        Assert.assertEquals(123.0, fp.getValueAsDouble(42.0), 0.0);
        Assert.assertEquals("123", fp.getValueAsString());
        Assert.assertEquals("123", fp.getValueAsString("default"));
        Assert.assertFalse(fp.getValueAsBoolean());
        Assert.assertFalse(fp.getValueAsBoolean(false));

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("b", fp.getCurrentName());

        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertTrue(fp.isExpectedStartArrayToken());
        Assert.assertFalse(fp.isExpectedStartObjectToken());
        Assert.assertEquals("b", fp.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_TRUE, fp.nextToken());
        Assert.assertTrue(fp.getBooleanValue());
        Assert.assertTrue(fp.getValueAsBoolean());
        Assert.assertTrue(fp.getValueAsBoolean(false));

        Assert.assertEquals(JsonToken.VALUE_FALSE, fp.nextToken());
        Assert.assertFalse(fp.getBooleanValue());

        Assert.assertEquals(JsonToken.VALUE_STRING, fp.nextToken());
        Assert.assertEquals("hello", fp.getText());
        Assert.assertTrue(fp.hasTextCharacters());
        Assert.assertNotNull(fp.getTextCharacters());
        Assert.assertTrue(fp.getTextLength() > 0);
        Assert.assertTrue(fp.getTextOffset() >= 0);

        Assert.assertEquals(JsonToken.END_ARRAY, fp.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("c", fp.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NULL, fp.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());
        Assert.assertNull(fp.getCurrentToken());

        fp.close();
    }

    @Test
    public void testTokenClearingAndState() throws IOException {
        String json = "{\"x\":1}";
        JsonParser p = _jsonFactory.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertNull(fp.getLastClearedToken());
        fp.clearCurrentToken();
        Assert.assertNull(fp.getLastClearedToken());

        fp.nextToken();
        Assert.assertEquals(JsonToken.START_OBJECT, fp.getCurrentToken());
        fp.clearCurrentToken();
        Assert.assertNull(fp.getCurrentToken());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.getLastClearedToken());

        fp.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOverrideCurrentNameThrowsException() throws IOException {
        String json = "{\"x\":1}";
        JsonParser p = _jsonFactory.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        fp.nextToken();
        fp.overrideCurrentName("y");
    }

    @Test
    public void testLocationsAndDelegatedMethods() throws IOException {
        String json = "{\"data\":\"AQIDBA==\"}";
        JsonParser p = _jsonFactory.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertNotNull(fp.getCurrentLocation());
        Assert.assertNotNull(fp.getTokenLocation());
        Assert.assertNotNull(fp.getParsingContext());

        fp.nextToken(); // START_OBJECT
        fp.nextToken(); // FIELD_NAME
        fp.nextToken(); // VALUE_STRING

        byte[] binary = fp.getBinaryValue(Base64Variants.MIME);
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4}, binary);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = fp.readBinaryValue(Base64Variants.MIME, baos);
        Assert.assertEquals(4, read);
        Assert.assertArrayEquals(new byte[]{1, 2, 3, 4}, baos.toByteArray());

        Assert.assertNull(fp.getEmbeddedObject());

        fp.close();
    }

    @Test
    public void testNextValue() throws IOException {
        String json = "{\"a\":1,\"b\":2}";
        JsonParser p = _jsonFactory.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextValue());
        Assert.assertEquals(1, fp.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextValue());
        Assert.assertEquals(2, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextValue());
        Assert.assertNull(fp.nextValue());

        fp.close();
    }

    @Test
    public void testSkipChildrenOnObjectAndArray() throws IOException {
        String json = "{\"obj\":{\"x\":1,\"y\":2},\"arr\":[1,2,3],\"final\":4}";
        JsonParser p = _jsonFactory.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("obj", fp.getCurrentName());

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertSame(fp, fp.skipChildren());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.getCurrentToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("arr", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertSame(fp, fp.skipChildren());
        Assert.assertEquals(JsonToken.END_ARRAY, fp.getCurrentToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertSame(fp, fp.skipChildren());

        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilterSinglePropertyWithPath() throws IOException {
        String json = "{\"a\":1,\"target\":{\"sub\":42},\"b\":2}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, false);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("target", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("sub", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(42, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilterSinglePropertyWithoutPath() throws IOException {
        String json = "{\"a\":1,\"target\":42,\"b\":2}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, true);

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("target", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(42, fp.getIntValue());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilterNestedPropertyIncludePathBuffering() throws IOException {
        String json = "{\"wrapper\":{\"nested\":{\"value\":99}},\"other\":[1,2]}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("wrapper".equals(name) || "nested".equals(name) || "value".equals(name)) {
                    return this;
                }
                return null;
            }

            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return true;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("wrapper", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("nested", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("value", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(99, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilterArrayElementsWithBuffering() throws IOException {
        String json = "{\"list\":[{\"id\":1,\"keep\":false},{\"id\":2,\"keep\":true}],\"extra\":10}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("list".equals(name) || "id".equals(name) || "keep".equals(name)) {
                    return this;
                }
                return null;
            }

            @Override
            public TokenFilter filterStartObject() {
                return this;
            }

            @Override
            public TokenFilter filterStartArray() {
                return this;
            }

            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return "2".equals(p.getText()) || JsonToken.VALUE_TRUE == p.currentToken();
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("list", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("id", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(2, fp.getIntValue());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("keep", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_TRUE, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testScalarFilterWithAllowMultipleMatchesFalse() throws IOException {
        String json = "123";
        JsonParser p = _jsonFactory.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, false, false);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(123, fp.getIntValue());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilterStartArrayReturnsIncludeAll() throws IOException {
        String json = "{\"data\":[1,2]}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return this;
            }

            @Override
            public TokenFilter filterStartArray() {
                return TokenFilter.INCLUDE_ALL;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("data", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(1, fp.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(2, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilterStartObjectReturnsIncludeAll() throws IOException {
        String json = "{\"data\":{\"a\":1}}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return this;
            }

            @Override
            public TokenFilter filterStartObject() {
                return TokenFilter.INCLUDE_ALL;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("data", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("a", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(1, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilterExcludesAll() throws IOException {
        String json = "{\"a\":[1,2],\"b\":{\"c\":3}}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        Assert.assertNull(fp.nextToken());
        fp.close();
    }

    @Test
    public void testIncludeImmediateParentDeprecatedFlag() throws IOException {
        String json = "{\"parent\":{\"target\":100}}";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return this;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, true);
        fp._includeImmediateParent = true;

        JsonToken t = fp.nextToken();
        Assert.assertNotNull(t);

        fp.close();
    }

    @Test
    public void testMultipleFilteredArraysAndObjects() throws IOException {
        String json = "[[1, 2], [3, 4]]";
        JsonParser p = _jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return this;
            }

            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() == 4;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(4, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }
}
