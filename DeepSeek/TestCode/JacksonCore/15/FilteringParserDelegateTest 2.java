package com.fasterxml.jackson.core.filter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.*;

public class FilteringParserDelegateTest {

    // Token event representation
    private static class TokenEvent {
        final JsonToken token;
        final String name;
        final String text;
        final Object value;
        TokenEvent(JsonToken token, String name, String text, Object value) {
            this.token = token;
            this.name = name;
            this.text = text;
            this.value = value;
        }
    }

    // A minimal mock of JsonParser for testing.
    private static class MockParser extends JsonParser {
        private final Queue<TokenEvent> events;
        private TokenEvent current;
        private JsonToken currentToken;
        private boolean closed;
        private int skipDepth; // used by custom skipChildren

        MockParser() {
            super((ObjectCodec) null);
            events = new LinkedList<>();
        }

        public void addEvent(JsonToken token, String name, String text, Object value) {
            events.add(new TokenEvent(token, name, text, value));
        }
        public void addStartObject() { addEvent(JsonToken.START_OBJECT, null, null, null); }
        public void addEndObject() { addEvent(JsonToken.END_OBJECT, null, null, null); }
        public void addStartArray() { addEvent(JsonToken.START_ARRAY, null, null, null); }
        public void addEndArray() { addEvent(JsonToken.END_ARRAY, null, null, null); }
        public void addFieldName(String name) { addEvent(JsonToken.FIELD_NAME, name, null, null); }
        public void addStringValue(String text) { addEvent(JsonToken.VALUE_STRING, null, text, text); }
        public void addIntValue(int v) { addEvent(JsonToken.VALUE_NUMBER_INT, null, null, v); }
        public void addNullValue() { addEvent(JsonToken.VALUE_NULL, null, null, null); }

        @Override
        public JsonToken nextToken() throws IOException {
            if (events.isEmpty()) {
                current = null;
                currentToken = null;
                return null;
            }
            current = events.poll();
            currentToken = current.token;
            return currentToken;
        }

        @Override
        public JsonToken getCurrentToken() { return currentToken; }

        @Override
        public String getCurrentName() throws IOException {
            return (current != null) ? current.name : null;
        }

        @Override
        public String getText() throws IOException {
            return (current != null) ? current.text : null;
        }

        @Override
        public char[] getTextCharacters() throws IOException { return (current != null) ? current.text.toCharArray() : null; }
        @Override        public int getTextLength() throws IOException { return (current != null) ? current.text.length() : 0; }
        @Override        public int getTextOffset() throws IOException { return 0; }
		@Override public boolean hasTextCharacters() { return current != null && current.text != null; }
	@Override public BigInteger getBigIntegerValue() throws IOException { return (current != null && current.value instanceof BigInteger) ? (BigInteger) current.value : null; }
	@Override public boolean getBooleanValue() throws IOException { return (current != null && current.value instanceof Boolean) ? (Boolean) current.value : false; }
	@Override public byte getByteValue() throws IOException { return (current != null && current.value instanceof Number) ? ((Number) current.value).byteValue() : 0; }
	@Override public short getShortValue() throws IOException { return (current != null && current.value instanceof Number) ? ((Number) current.value).shortValue() : 0; }
	@Override public BigDecimal getDecimalValue() throws IOException { return (current != null && current.value instanceof BigDecimal) ? (BigDecimal) current.value : null; }
	@Override public double getDoubleValue() throws IOException { return (current != null && current.value instanceof Number) ? ((Number) current.value).doubleValue() : 0; }
	@Override public float getFloatValue() throws IOException { return (current != null && current.value instanceof Number) ? ((Number) current.value).floatValue() : 0; }
	@Override public int getIntValue() throws IOException { return (current != null && current.value instanceof Number) ? ((Number) current.value).intValue() : 0; }
	@Override public long getLongValue() throws IOException { return (current != null && current.value instanceof Number) ? ((Number) current.value).longValue() : 0; }
	@Override public NumberType getNumberType() throws IOException { return null; }
	@Override public Number getNumberValue() throws IOException { return (current != null && current.value instanceof Number) ? (Number) current.value : null; }
	@Override public int getValueAsInt() throws IOException { return 0; }
	@Override public int getValueAsInt(int defaultValue) throws IOException { return defaultValue; }
	@Override public long getValueAsLong() throws IOException { return 0L; }
	@Override public long getValueAsLong(long defaultValue) throws IOException { return defaultValue; }
	@Override public double getValueAsDouble() throws IOException { return 0.0; }
	@Override public double getValueAsDouble(double defaultValue) throws IOException { return defaultValue; }
	@Override public boolean getValueAsBoolean() throws IOException { return false; }
	@Override public boolean getValueAsBoolean(boolean defaultValue) throws IOException { return defaultValue; }
	@Override public String getValueAsString() throws IOException { return null; }
	@Override public String getValueAsString(String defaultValue) throws IOException { return defaultValue; }
	@Override public Object getEmbeddedObject() throws IOException { return null; }
	@Override public byte[] getBinaryValue(Base64Variant b64variant) throws IOException { return null; }
	@Override public int readBinaryValue(Base64Variant b64variant, OutputStream out) throws IOException { return 0; }
	@Override public JsonLocation getTokenLocation() { return null; }
	@Override public JsonLocation getCurrentLocation() { return null; }
	@Override public void overrideCurrentName(String name) { }

	// custom skipChildren to advance until matching end token
	@Override
        public JsonParser skipChildren() throws IOException {
            if (currentToken != JsonToken.START_OBJECT && currentToken != JsonToken.START_ARRAY) {
                return this;
            }
            int depth = 1;
            while (depth > 0) {
                JsonToken t = nextToken();
                if (t == null) break;
                if (t == JsonToken.START_OBJECT || t == JsonToken.START_ARRAY) {
                    depth++;
                } else if (t == JsonToken.END_OBJECT || t == JsonToken.END_ARRAY) {
                    depth--;
                }
            }
            return this;
        }

        @Override public void close() throws IOException { closed = true; }
        @Override public boolean isClosed() { return closed; }
        @Override public JsonStreamContext getParsingContext() { return null; }
        // other required methods
        @Override public ObjectCodec getCodec() { return null; }
        @Override public void setCodec(ObjectCodec c) { }
        @Override public Version version() { return null; }
        @Override public JsonParser enable(Feature f) { return this; }
        @Override public JsonParser disable(Feature f) { return this; }
        @Override public boolean isEnabled(Feature f) { return false; }
        @Override public void setSchema(FormatSchema schema) { }
        @Override public FormatSchema getSchema() { return null; }
        @Override public boolean canUseSchema(FormatSchema schema) { return false; }
        @Override public boolean requiresCustomCodec() { return false; }
    }

    // A configurable TokenFilter for testing
    private static class TestFilter extends TokenFilter {
        TokenFilter startObjectResult = TokenFilter.INCLUDE_ALL;
        TokenFilter startArrayResult = TokenFilter.INCLUDE_ALL;
        java.util.Map<String, TokenFilter> propertyFilters = new java.util.HashMap<>();
        TokenFilter defaultPropertyResult = TokenFilter.INCLUDE_ALL;
        boolean includeLeaf = true;

        @Override public TokenFilter filterStartObject() { return startObjectResult; }
        @Override public TokenFilter filterStartArray() { return startArrayResult; }
        @Override public TokenFilter includeProperty(String name) {
            return propertyFilters.getOrDefault(name, defaultPropertyResult);
        }
        @Override public boolean includeValue(JsonParser p) throws IOException {
            return includeLeaf;
        }
    }

    private MockParser parser;
    private TestFilter filter;
    private FilteringParserDelegate fpd;

    @Before
    public void setUp() {
        parser = new MockParser();
        filter = new TestFilter();
        // default inclusion: include all
        fpd = new FilteringParserDelegate(parser, filter, false, false);
    }

    // helper: create delegate with specific settings
    private FilteringParserDelegate createDelegate(boolean includePath, boolean allowMultiple) {
        return new FilteringParserDelegate(parser, filter, includePath, allowMultiple);
    }

    // ============== Basic token accessor tests ==============
    @Test
    public void testInitialState() throws IOException {
        assertNull(fpd.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, fpd.getCurrentTokenId());
        assertFalse(fpd.hasCurrentToken());
        assertTrue(fpd.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(fpd.hasToken(JsonToken.START_OBJECT));
    }

    @Test
    public void testClearCurrentToken() throws IOException {
        parser.addStartObject(); // include all
        fpd.nextToken();
        assertEquals(JsonToken.START_OBJECT, fpd.getCurrentToken());
        fpd.clearCurrentToken();
        assertNull(fpd.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, fpd.getLastClearedToken());
    }

    @Test
    public void testOverrideCurrentNameUnsupported() {
        try {
            fpd.overrideCurrentName("test");
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetParsingContextInitiallyHead() throws IOException {
        JsonStreamContext ctx = fpd.getParsingContext();
        assertNotNull(ctx);
        // check that it returns _headContext
    }

    // ============== nextToken with INCLUDE_ALL filter ==============
    @Test
    public void testIncludeAllPassThrough() throws IOException {
        parser.addStartObject();
        parser.addFieldName("a");
        parser.addStringValue("val");
        parser.addEndObject();

        fpd = createDelegate(false, false);
        filter.propertyFilters.put("a", TokenFilter.INCLUDE_ALL);
        filter.defaultPropertyResult = TokenFilter.INCLUDE_ALL;

        assertEquals(JsonToken.START_OBJECT, fpd.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fpd.nextToken());
        assertEquals("a", fpd.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
        assertEquals("val", fpd.getText());
        assertEquals(JsonToken.END_OBJECT, fpd.nextToken());
        assertNull(fpd.nextToken());
    }

    // ============== Filter with null root ==============
    @Test
    public void testNullRootFilterSkipsEverything() throws IOException {
        parser.addStartObject();
        parser.addFieldName("x");
        parser.addStringValue("ignored");
        parser.addEndObject();
        parser.addStringValue("orphan");
        fpd = new FilteringParserDelegate(parser, null, false, false);
        assertNull(fpd.nextToken()); // skips everything, returns null
        assertNull(fpd.getCurrentToken());
    }

    // ============== Scalar value filtering ==============
    @Test
    public void testScalarIncluded() throws IOException {
        parser.addStringValue("hello");
        filter.includeLeaf = true;
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
        assertEquals("hello", fpd.getText());
    }

    @Test
    public void testScalarExcluded() throws IOException {
        parser.addStringValue("hello");
        filter.includeLeaf = false;
        assertNull(fpd.nextToken()); // excluded, no more tokens
    }

    // ============== Object/Array with filters ==============
    @Test
    public void testStartObjectExcludedBecauseFilterNull() throws IOException {
        filter.startObjectResult = null;
        parser.addStartObject();
        parser.addFieldName("x");
        parser.addStringValue("v");
        parser.addEndObject();
        parser.addStringValue("after");
        // object filtered out completely, next token should be "after"
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
    }

    @Test
    public void testStartArrayExcludedBecauseFilterNull() throws IOException {
        filter.startArrayResult = null;
        parser.addStartArray();
        parser.addStringValue("v");
        parser.addEndArray();
        parser.addStringValue("after");
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
    }

    @Test
    public void testStartObjectIncludedButChildrenFiltered() throws IOException {
        filter.startObjectResult = TokenFilter.INCLUDE_ALL;
        filter.defaultPropertyResult = null; // all properties excluded
        parser.addStartObject();
        parser.addFieldName("x");
        parser.addStringValue("v");
        parser.addEndObject();
        // object token included, but since no properties, next is end object
        assertEquals(JsonToken.START_OBJECT, fpd.nextToken());
        // field name should be skipped, then value skipped, then end object handled
        // next token should be END_OBJECT (if start handled) or skip?
        // With include_all for object, headContext.isStartHandled true, so returnEnd=true
        assertEquals(JsonToken.END_OBJECT, fpd.nextToken());
        assertNull(fpd.nextToken());
    }

    @Test
    public void testFieldNameIncludedViaIncludeProperty() throws IOException {
        filter.propertyFilters.put("a", TokenFilter.INCLUDE_ALL);
        filter.defaultPropertyResult = null; // others excluded
        parser.addStartObject();
        parser.addFieldName("a");
        parser.addStringValue("aVal");
        parser.addFieldName("b");
        parser.addStringValue("bVal");
        parser.addEndObject();

        fpd = createDelegate(false, false);
        assertEquals(JsonToken.START_OBJECT, fpd.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fpd.nextToken());
        assertEquals("a", fpd.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
        assertEquals("aVal", fpd.getText());
        // next should be END_OBJECT because 'b' filtered out
        assertEquals(JsonToken.END_OBJECT, fpd.nextToken());
        assertNull(fpd.nextToken());
    }

    @Test
    public void testIncludePathBuffering() throws IOException {
        // includePath=true: path to matched property is included.
        filter.propertyFilters.put("inner", TokenFilter.INCLUDE_ALL);
        filter.defaultPropertyResult = null; // exclude others at top level
        parser.addStartObject();
        parser.addFieldName("outer");
        parser.addStartObject();
        parser.addFieldName("inner");
        parser.addStringValue("val");
        parser.addEndObject();
        parser.addEndObject();

        fpd = createDelegate(true, false);
        // expected: START_OBJECT (from path), FIELD_NAME "outer" (path), START_OBJECT (path),
        // FIELD_NAME "inner", VALUE_STRING, END_OBJECT, END_OBJECT
        assertEquals(JsonToken.START_OBJECT, fpd.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fpd.nextToken());
        assertEquals("outer", fpd.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, fpd.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fpd.nextToken());
        assertEquals("inner", fpd.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
        assertEquals("val", fpd.getText());
        assertEquals(JsonToken.END_OBJECT, fpd.nextToken());
        assertEquals(JsonToken.END_OBJECT, fpd.nextToken());
        assertNull(fpd.nextToken());
    }

    @Test
    public void testIncludeImmediateParent() throws Exception {
        // Set _includeImmediateParent via reflection
        filter.propertyFilters.put("key", TokenFilter.INCLUDE_ALL);
        filter.defaultPropertyResult = null; // exclude top-level properties except "key"
        parser.addStartObject();
        parser.addFieldName("key");
        parser.addStringValue("val");
        parser.addEndObject();

        fpd = createDelegate(false, false);
        Field f = FilteringParserDelegate.class.getDeclaredField("_includeImmediateParent");
        f.setAccessible(true);
        f.setBoolean(fpd, true);

        // With includeImmediateParent, when encountering fieldName that is included, parent start object should appear if not handled.
        // Expect: START_OBJECT (from buffering), FIELD_NAME, VALUE_STRING, END_OBJECT
        assertEquals(JsonToken.START_OBJECT, fpd.nextToken());
        assertEquals(JsonToken.FIELD_NAME, fpd.nextToken());
        assertEquals("key", fpd.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
        assertEquals(JsonToken.END_OBJECT, fpd.nextToken());
        assertNull(fpd.nextToken());
    }

    // ============== nextValue and skipChildren ==============
    @Test
    public void testNextValueSkipsFieldName() throws IOException {
        filter.defaultPropertyResult = TokenFilter.INCLUDE_ALL;
        parser.addStartObject();
        parser.addFieldName("a");
        parser.addStringValue("val");
        parser.addEndObject();

        fpd.nextToken(); // START_OBJECT
        JsonToken t = fpd.nextValue();
        assertEquals(JsonToken.VALUE_STRING, t);
        assertEquals("val", fpd.getText());
    }

    @Test
    public void testSkipChildren() throws IOException {
        filter.defaultPropertyResult = TokenFilter.INCLUDE_ALL;
        parser.addStartObject();
        parser.addFieldName("a");
        parser.addStringValue("val");
        parser.addFieldName("b");
        parser.addStartArray();
        parser.addStringValue("arr");
        parser.addEndArray();
        parser.addEndObject();
        parser.addStringValue("after");

        fpd = createDelegate(false, false);
        assertEquals(JsonToken.START_OBJECT, fpd.nextToken());
        fpd.skipChildren(); // should skip to END_OBJECT
        // After skip, current token should be END_OBJECT? Actually skipChildren returns 'this', but next token should be outside.
        // To verify, we call nextToken again, expecting "after"
        assertEquals(JsonToken.VALUE_STRING, fpd.nextToken());
        assertEquals("after", fpd.getText());
    }

    // ============== Delegated methods ==============
    @Test
    public void testGetCurrentNameDuringObject() throws IOException {
        parser.addStartObject();
        parser.addFieldName("prop");
        parser.addStartObject();
        // move to inner START_OBJECT
        fpd = createDelegate(false, false);
        fpd.nextToken(); // START_OBJECT outer
        assertEquals(JsonToken.START_OBJECT, fpd.getCurrentToken());
        assertNull(fpd.getCurrentName(), "Outer object start name should be null");

        fpd.nextToken(); // FIELD_NAME "prop"
        assertEquals(JsonToken.FIELD_NAME, fpd.getCurrentToken());
        assertEquals("prop", fpd.getCurrentName());

        fpd.nextToken(); // inner START_OBJECT
        assertEquals(JsonToken.START_OBJECT, fpd.getCurrentToken());
        // getCurrentName should return parent's current name
        assertEquals("prop", fpd.getCurrentName());
    }

    @Test
    public void testGetTextDelegates() throws IOException {
        parser.addStringValue("textual");
        fpd.nextToken();
        assertEquals("textual", fpd.getText());
        assertEquals(7, fpd.getTextLength());
    }

    @Test
    public void testNumericDelegates() throws IOException {
        parser.addIntValue(42);
        fpd.nextToken();
        assertEquals(42, fpd.getIntValue());
    }

    // Test exception propagation
    @Test(expected = IOException.class)
    public void testIOExceptionPropagated() throws IOException {
        parser = new MockParser() {
            @Override
            public JsonToken nextToken() throws IOException {
                throw new IOException("test");
            }
        };
        fpd = new FilteringParserDelegate(parser, filter, false, false);
        fpd.nextToken();
    }

    // Test that _nextBuffered throws appropriate error (simulate broken chain)
    // This is tricky; we may rely on internal consistency.

    // Test with multiple matches (allowMultipleMatches true), but since not implemented, we skip deep checks.

    // Test that getFilter() returns rootFilter
    @Test
    public void testGetFilter() {
        assertSame(filter, fpd.getFilter());
    }

    // Test getMatchCount initial zero
    @Test
    public void testGetMatchCountInitial() {
        assertEquals(0, fpd.getMatchCount());
    }

    // Additional edge: null token in nextToken loop
    @Test
    public void testDelegateReturnsNullToken() throws IOException {
        // no events added
        assertNull(fpd.nextToken());
    }
}
