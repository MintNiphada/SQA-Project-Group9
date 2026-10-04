package com.fasterxml.jackson.core.filter;

import com.fasterxml.jackson.core.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FilteringParserDelegateTest {

    private final JsonFactory JSON_F = new JsonFactory();

    @Test
    public void testBasicTokenAndIdAccessors() throws Exception {
        String json = "{\"a\": 123}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertNull(fp.getCurrentToken());
        Assert.assertNull(fp.currentToken());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, fp.getCurrentTokenId());
        Assert.assertEquals(JsonTokenId.ID_NO_TOKEN, fp.currentTokenId());
        Assert.assertFalse(fp.hasCurrentToken());
        Assert.assertTrue(fp.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        Assert.assertFalse(fp.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertFalse(fp.hasToken(JsonToken.START_OBJECT));
        Assert.assertFalse(fp.isExpectedStartArrayToken());
        Assert.assertFalse(fp.isExpectedStartObjectToken());
        Assert.assertEquals(0, fp.getMatchCount());
        Assert.assertSame(TokenFilter.INCLUDE_ALL, fp.getFilter());

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.getCurrentToken());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.currentToken());
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, fp.getCurrentTokenId());
        Assert.assertEquals(JsonTokenId.ID_START_OBJECT, fp.currentTokenId());
        Assert.assertTrue(fp.hasCurrentToken());
        Assert.assertTrue(fp.hasTokenId(JsonTokenId.ID_START_OBJECT));
        Assert.assertFalse(fp.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        Assert.assertTrue(fp.hasToken(JsonToken.START_OBJECT));
        Assert.assertFalse(fp.isExpectedStartArrayToken());
        Assert.assertTrue(fp.isExpectedStartObjectToken());

        fp.clearCurrentToken();
        Assert.assertNull(fp.getCurrentToken());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.getLastClearedToken());

        fp.close();
    }

    @Test
    public void testArrayTokenAccessors() throws Exception {
        String json = "[10, 20]";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertTrue(fp.isExpectedStartArrayToken());
        Assert.assertFalse(fp.isExpectedStartObjectToken());

        fp.close();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOverrideCurrentName() throws Exception {
        String json = "{\"a\": 1}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        fp.nextToken();
        fp.overrideCurrentName("b");
    }

    @Test
    public void testDelegateValueAccessors() throws Exception {
        String json = "{\"intVal\": 42, \"str\": \"hello\", \"bool\": true, \"dec\": 12.34}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertNotNull(fp.getCurrentLocation());
        Assert.assertNotNull(fp.getTokenLocation());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("intVal", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals("42", fp.getText());
        Assert.assertTrue(fp.hasTextCharacters());
        Assert.assertEquals(2, fp.getTextCharacters().length >= 2 ? 2 : 0);
        Assert.assertEquals(2, fp.getTextLength());
        Assert.assertEquals(0, fp.getTextOffset());
        Assert.assertEquals(42, fp.getIntValue());
        Assert.assertEquals(42L, fp.getLongValue());
        Assert.assertEquals((byte) 42, fp.getByteValue());
        Assert.assertEquals((short) 42, fp.getShortValue());
        Assert.assertEquals(42.0, fp.getDoubleValue(), 0.0001);
        Assert.assertEquals(42.0f, fp.getFloatValue(), 0.0001f);
        Assert.assertEquals(BigInteger.valueOf(42), fp.getBigIntegerValue());
        Assert.assertEquals(JsonParser.NumberType.INT, fp.getNumberType());
        Assert.assertEquals(42, fp.getNumberValue().intValue());
        Assert.assertEquals(42, fp.getValueAsInt());
        Assert.assertEquals(42, fp.getValueAsInt(100));
        Assert.assertEquals(42L, fp.getValueAsLong());
        Assert.assertEquals(42L, fp.getValueAsLong(100L));
        Assert.assertEquals(42.0, fp.getValueAsDouble(), 0.0001);
        Assert.assertEquals(42.0, fp.getValueAsDouble(99.0), 0.0001);
        Assert.assertFalse(fp.getValueAsBoolean());
        Assert.assertFalse(fp.getValueAsBoolean(false));
        Assert.assertEquals("42", fp.getValueAsString());
        Assert.assertEquals("42", fp.getValueAsString("def"));

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("str", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, fp.nextToken());
        Assert.assertEquals("hello", fp.getText());
        Assert.assertEquals("hello", fp.getValueAsString());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("bool", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_TRUE, fp.nextToken());
        Assert.assertTrue(fp.getBooleanValue());
        Assert.assertTrue(fp.getValueAsBoolean());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("dec", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_FLOAT, fp.nextToken());
        Assert.assertEquals(new BigDecimal("12.34"), fp.getDecimalValue());

        Assert.assertNull(fp.getEmbeddedObject());
        Assert.assertNotNull(fp.getBinaryValue(Base64Variants.MIME));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Assert.assertEquals(0, fp.readBinaryValue(Base64Variants.MIME, out));

        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testNextValue() throws Exception {
        String json = "{\"a\": 1, \"b\": [2, 3]}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextValue());
        Assert.assertEquals(1, fp.getIntValue());
        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextValue());
        Assert.assertEquals(2, fp.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextValue());
        Assert.assertEquals(3, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, fp.nextValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextValue());
        Assert.assertNull(fp.nextValue());

        fp.close();
    }

    @Test
    public void testSkipChildrenObjectAndArray() throws Exception {
        String json = "{\"obj\": {\"x\": 1, \"y\": 2}, \"arr\": [10, 20], \"after\": 3}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("obj", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        fp.skipChildren();
        Assert.assertEquals(JsonToken.END_OBJECT, fp.getCurrentToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("arr", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        fp.skipChildren();
        Assert.assertEquals(JsonToken.END_ARRAY, fp.getCurrentToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(3, fp.getIntValue());
        fp.skipChildren(); // calling skipChildren on scalar should do nothing
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.getCurrentToken());

        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilteringIncludePathTrue() throws Exception {
        String json = "{\"a\": 1, \"target\": {\"x\": 10, \"y\": 20}, \"b\": 2}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("target", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("x", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(10, fp.getIntValue());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("y", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(20, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testFilteringIncludePathFalse() throws Exception {
        String json = "{\"a\": 1, \"target\": 99, \"b\": 2}";
        JsonParser p = JSON_F.createParser(json);
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
        Assert.assertEquals(99, fp.getIntValue());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testIncludePathBufferingNestedObject() throws Exception {
        String json = "{\"root\": {\"nested\": {\"leaf\": 123}, \"other\": 456}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("root".equals(name) || "nested".equals(name)) {
                    return this;
                }
                if ("leaf".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("root", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("nested", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("leaf", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(123, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testIncludePathBufferingArray() throws Exception {
        String json = "{\"arr\": [1, 2, 3]}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("arr".equals(name)) {
                    return new TokenFilter() {
                        @Override
                        public TokenFilter filterStartArray() {
                            return this;
                        }

                        @Override
                        public boolean includeValue(JsonParser p) throws IOException {
                            return p.getIntValue() == 2;
                        }
                    };
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("arr", fp.getCurrentName());
        Assert.assertEquals(JsonToken.START_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(2, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_ARRAY, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testArrayFilteringNoMatchingElements() throws Exception {
        String json = "{\"arr\": [1, 2, 3], \"other\": 10}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("arr".equals(name)) {
                    return new TokenFilter() {
                        @Override
                        public TokenFilter filterStartArray() {
                            return this;
                        }

                        @Override
                        public boolean includeValue(JsonParser p) throws IOException {
                            return false;
                        }
                    };
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testAllowMultipleMatchesFalseScalar() throws Exception {
        String json = "[10, 20, 30]";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return this;
            }

            @Override
            public boolean includeValue(JsonParser p) {
                return true;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, false);
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(10, fp.getIntValue());

        fp.close();
    }

    @Test
    public void testAllowMultipleMatchesFalseStruct() throws Exception {
        String json = "[{\"a\": 1}, {\"b\": 2}]";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return this;
            }

            @Override
            public TokenFilter filterStartObject() {
                return TokenFilter.INCLUDE_ALL;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, false);
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("a", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(1, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testIncludeImmediateParentDeprecated() throws Exception {
        String json = "{\"a\": {\"b\": 123}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("b".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return this;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, false, true);
        fp._includeImmediateParent = true;

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("b", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(123, fp.getIntValue());

        fp.close();
    }

    @Test
    public void testNestedArrayInObjectFiltering() throws Exception {
        String json = "{\"outer\": {\"innerArr\": [{\"item\": 1}, {\"item\": 2}]}}";
        JsonParser p = JSON_F.createParser(json);
        final Set<String> matchedFields = new HashSet<String>();
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("outer".equals(name) || "innerArr".equals(name) || "item".equals(name)) {
                    return this;
                }
                return null;
            }

            @Override
            public TokenFilter filterStartArray() {
                return this;
            }

            @Override
            public TokenFilter filterStartObject() {
                return this;
            }

            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() == 2;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        while (fp.nextToken() != null) {
            if (fp.getCurrentToken() == JsonToken.FIELD_NAME) {
                matchedFields.add(fp.getCurrentName());
            }
        }
        Assert.assertTrue(matchedFields.contains("outer"));
        Assert.assertTrue(matchedFields.contains("innerArr"));
        Assert.assertTrue(matchedFields.contains("item"));

        fp.close();
    }

    @Test
    public void testGetCurrentNameInStructures() throws Exception {
        String json = "{\"nested\": {\"val\": 1}}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertNull(fp.getCurrentName());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("nested", fp.getCurrentName());

        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals("nested", fp.getCurrentName());

        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("val", fp.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals("val", fp.getCurrentName());

        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testEmptyAndNullFilterBranches() throws Exception {
        String json = "{\"skipObj\": {\"a\": 1}, \"skipArr\": [1, 2], \"keep\": 3}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("keep".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        Assert.assertEquals(JsonToken.START_OBJECT, fp.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, fp.nextToken());
        Assert.assertEquals("keep", fp.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, fp.nextToken());
        Assert.assertEquals(3, fp.getIntValue());
        Assert.assertEquals(JsonToken.END_OBJECT, fp.nextToken());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testRootScalarFilter() throws Exception {
        String json = "\"singleScalar\"";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertEquals(JsonToken.VALUE_STRING, fp.nextToken());
        Assert.assertEquals("singleScalar", fp.getText());
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testRootScalarExcluded() throws Exception {
        String json = "\"singleScalar\"";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser p) {
                return false;
            }
        };
        FilteringParserDelegate fp = new FilteringParserDelegate(p, filter, true, true);
        Assert.assertNull(fp.nextToken());

        fp.close();
    }

    @Test
    public void testGetParsingContext() throws Exception {
        String json = "{\"a\": 1}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate fp = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        Assert.assertNotNull(fp.getParsingContext());
        Assert.assertTrue(fp.getParsingContext().inRoot());

        fp.nextToken();
        Assert.assertNotNull(fp.getParsingContext());
        Assert.assertTrue(fp.getParsingContext().inObject());

        fp.close();
    }
}
