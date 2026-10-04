package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JsonGeneratorTest {

    private SimpleGenerator g;

    @Before
    public void setUp() {
        g = new SimpleGenerator();
    }

    // =========================================================================
    // Feature Enum and Masking Tests
    // =========================================================================

    @Test
    public void testFeatureDefaultsAndMask() {
        int defaults = JsonGenerator.Feature.collectDefaults();
        Assert.assertTrue(JsonGenerator.Feature.AUTO_CLOSE_TARGET.enabledByDefault());
        Assert.assertTrue(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.enabledByDefault());
        Assert.assertTrue(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM.enabledByDefault());
        Assert.assertTrue(JsonGenerator.Feature.QUOTE_FIELD_NAMES.enabledByDefault());
        Assert.assertTrue(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledByDefault());
        Assert.assertFalse(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.enabledByDefault());
        Assert.assertFalse(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledByDefault());
        Assert.assertFalse(JsonGenerator.Feature.ESCAPE_NON_ASCII.enabledByDefault());
        Assert.assertFalse(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.enabledByDefault());
        Assert.assertFalse(JsonGenerator.Feature.IGNORE_UNKNOWN.enabledByDefault());

        for (JsonGenerator.Feature f : JsonGenerator.Feature.values()) {
            if (f.enabledByDefault()) {
                Assert.assertTrue((defaults & f.getMask()) != 0);
                Assert.assertTrue(f.enabledIn(defaults));
            } else {
                Assert.assertFalse((defaults & f.getMask()) != 0);
                Assert.assertFalse(f.enabledIn(defaults));
            }
        }
    }

    @Test
    public void testConfigure() {
        g.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        Assert.assertFalse(g.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        g.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        Assert.assertTrue(g.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testOverrideStdFeatures() {
        int initial = g.getFeatureMask();
        int mask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        int values = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask(); // enable WRITE_NUMBERS, disable QUOTE_FIELD_NAMES

        g.overrideStdFeatures(values, mask);
        Assert.assertTrue(g.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertFalse(g.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testFormatFeatures() {
        Assert.assertEquals(0, g.getFormatFeatures());
        try {
            g.overrideFormatFeatures(1, 1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("No FormatFeatures defined"));
        }
    }

    // =========================================================================
    // Schema & Capability Introspection Tests
    // =========================================================================

    @Test
    public void testSchemaHandling() {
        Assert.assertNull(g.getSchema());
        FormatSchema schema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "TEST_SCHEMA";
            }
        };
        Assert.assertFalse(g.canUseSchema(schema));
        try {
            g.setSchema(schema);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("TEST_SCHEMA"));
        }
    }

    @Test
    public void testCapabilityIntrospections() {
        Assert.assertFalse(g.canWriteObjectId());
        Assert.assertFalse(g.canWriteTypeId());
        Assert.assertFalse(g.canWriteBinaryNatively());
        Assert.assertTrue(g.canOmitFields());
        Assert.assertFalse(g.canWriteFormattedNumbers());
    }

    // =========================================================================
    // Native Ids & Embedded Objects (Exceptions)
    // =========================================================================

    @Test(expected = JsonGenerationException.class)
    public void testWriteEmbeddedObject() throws IOException {
        g.writeEmbeddedObject("custom-opaque");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteObjectId() throws IOException {
        g.writeObjectId("id-123");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteObjectRef() throws IOException {
        g.writeObjectRef("id-123");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteTypeId() throws IOException {
        g.writeTypeId("type-123");
    }

    // =========================================================================
    // Configuration & State Access
    // =========================================================================

    @Test
    public void testPrettyPrinter() {
        Assert.assertNull(g.getPrettyPrinter());
        PrettyPrinter pp = new PrettyPrinter() {
            @Override
            public void writeRootValueSeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void writeStartObject(JsonGenerator gen) throws IOException {}
            @Override
            public void writeEndObject(JsonGenerator gen, int nrOfValues) throws IOException {}
            @Override
            public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void writeStartArray(JsonGenerator gen) throws IOException {}
            @Override
            public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {}
            @Override
            public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {}
            @Override
            public void beforeArrayValues(JsonGenerator gen) throws IOException {}
            @Override
            public void beforeObjectEntries(JsonGenerator gen) throws IOException {}
        };
        Assert.assertSame(g, g.setPrettyPrinter(pp));
        Assert.assertSame(pp, g.getPrettyPrinter());
    }

    @Test
    public void testEscapingAndSeparators() {
        Assert.assertSame(g, g.setHighestNonEscapedChar(127));
        Assert.assertEquals(0, g.getHighestEscapedChar());
        Assert.assertNull(g.getCharacterEscapes());
        Assert.assertSame(g, g.setCharacterEscapes(null));
        try {
            g.setRootValueSeparator(new SerializedString(" "));
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
    }

    @Test
    public void testOutputTargetAndBuffer() {
        Assert.assertNull(g.getOutputTarget());
        Assert.assertEquals(-1, g.getOutputBuffered());
    }

    @Test
    public void testCurrentValue() {
        Assert.assertNull(g.getCurrentValue());
        g.setCurrentValue("value1"); // No context set, should not fail

        DummyContext context = new DummyContext();
        g.setContext(context);
        Assert.assertNull(g.getCurrentValue());
        g.setCurrentValue("value2");
        Assert.assertEquals("value2", g.getCurrentValue());
        Assert.assertEquals("value2", context.getCurrentValue());
    }

    // =========================================================================
    // Structural Writes
    // =========================================================================

    @Test
    public void testStructuralWrites() throws IOException {
        g.writeStartArray(5);
        Assert.assertEquals("[", g.tokens.get(g.tokens.size() - 1));

        DummyContext context = new DummyContext();
        g.setContext(context);
        g.writeStartObject("parentObj");
        Assert.assertEquals("{", g.tokens.get(g.tokens.size() - 1));
        Assert.assertEquals("parentObj", context.getCurrentValue());

        g.writeFieldId(12345L);
        Assert.assertEquals("fieldName:12345", g.tokens.get(g.tokens.size() - 1));
    }

    // =========================================================================
    // Write Array Overloads (int[], long[], double[])
    // =========================================================================

    @Test
    public void testWriteArrayInt() throws IOException {
        int[] arr = new int[]{10, 20, 30, 40};
        g.writeArray(arr, 1, 2);
        Assert.assertEquals("[", g.tokens.get(0));
        Assert.assertEquals("int:20", g.tokens.get(1));
        Assert.assertEquals("int:30", g.tokens.get(2));
        Assert.assertEquals("]", g.tokens.get(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArrayIntNull() throws IOException {
        g.writeArray((int[]) null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArrayIntInvalidOffset() throws IOException {
        g.writeArray(new int[]{1, 2}, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArrayIntInvalidLength() throws IOException {
        g.writeArray(new int[]{1, 2}, 1, 2);
    }

    @Test
    public void testWriteArrayLong() throws IOException {
        long[] arr = new long[]{100L, 200L, 300L};
        g.writeArray(arr, 0, 3);
        Assert.assertEquals("[", g.tokens.get(0));
        Assert.assertEquals("long:100", g.tokens.get(1));
        Assert.assertEquals("long:200", g.tokens.get(2));
        Assert.assertEquals("long:300", g.tokens.get(3));
        Assert.assertEquals("]", g.tokens.get(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArrayLongNull() throws IOException {
        g.writeArray((long[]) null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArrayLongInvalidBounds() throws IOException {
        g.writeArray(new long[]{1L}, 0, 2);
    }

    @Test
    public void testWriteArrayDouble() throws IOException {
        double[] arr = new double[]{1.5, 2.5};
        g.writeArray(arr, 0, 2);
        Assert.assertEquals("[", g.tokens.get(0));
        Assert.assertEquals("double:1.5", g.tokens.get(1));
        Assert.assertEquals("double:2.5", g.tokens.get(2));
        Assert.assertEquals("]", g.tokens.get(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArrayDoubleNull() throws IOException {
        g.writeArray((double[]) null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteArrayDoubleInvalidBounds() throws IOException {
        g.writeArray(new double[]{1.0}, 2, 0);
    }

    // =========================================================================
    // Raw and Binary Value Writes
    // =========================================================================

    @Test
    public void testWriteRawSerializableString() throws IOException {
        SerializedString ss = new SerializedString("raw-value");
        g.writeRaw(ss);
        Assert.assertEquals("raw:raw-value", g.tokens.get(g.tokens.size() - 1));

        g.writeRawValue(ss);
        Assert.assertEquals("rawValue:raw-value", g.tokens.get(g.tokens.size() - 1));
    }

    @Test
    public void testWriteBinaryOverloads() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4};
        g.writeBinary(data, 1, 2);
        Assert.assertTrue(g.tokens.get(g.tokens.size() - 1).startsWith("binary:"));

        g.writeBinary(data);
        Assert.assertTrue(g.tokens.get(g.tokens.size() - 1).startsWith("binary:"));

        InputStream in = new ByteArrayInputStream(data);
        int written = g.writeBinary(in, 4);
        Assert.assertEquals(4, written);
        Assert.assertTrue(g.tokens.get(g.tokens.size() - 1).startsWith("binaryStream:4"));
    }

    @Test
    public void testWriteNumberShort() throws IOException {
        g.writeNumber((short) 42);
        Assert.assertEquals("int:42", g.tokens.get(g.tokens.size() - 1));
    }

    // =========================================================================
    // Convenience Field Write Methods
    // =========================================================================

    @Test
    public void testFieldWriteMethods() throws IOException {
        g.writeStringField("f_str", "val");
        Assert.assertEquals("fieldName:f_str", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("str:val", g.tokens.get(g.tokens.size() - 1));

        g.writeBooleanField("f_bool", true);
        Assert.assertEquals("fieldName:f_bool", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("bool:true", g.tokens.get(g.tokens.size() - 1));

        g.writeNullField("f_null");
        Assert.assertEquals("fieldName:f_null", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("null", g.tokens.get(g.tokens.size() - 1));

        g.writeNumberField("f_int", 1);
        Assert.assertEquals("fieldName:f_int", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("int:1", g.tokens.get(g.tokens.size() - 1));

        g.writeNumberField("f_long", 2L);
        Assert.assertEquals("fieldName:f_long", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("long:2", g.tokens.get(g.tokens.size() - 1));

        g.writeNumberField("f_double", 3.0);
        Assert.assertEquals("fieldName:f_double", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("double:3.0", g.tokens.get(g.tokens.size() - 1));

        g.writeNumberField("f_float", 4.0f);
        Assert.assertEquals("fieldName:f_float", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("float:4.0", g.tokens.get(g.tokens.size() - 1));

        g.writeNumberField("f_dec", BigDecimal.TEN);
        Assert.assertEquals("fieldName:f_dec", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("dec:10", g.tokens.get(g.tokens.size() - 1));

        g.writeBinaryField("f_bin", new byte[]{5});
        Assert.assertEquals("fieldName:f_bin", g.tokens.get(g.tokens.size() - 2));
        Assert.assertTrue(g.tokens.get(g.tokens.size() - 1).startsWith("binary:"));

        g.writeArrayFieldStart("f_arr");
        Assert.assertEquals("fieldName:f_arr", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("[", g.tokens.get(g.tokens.size() - 1));

        g.writeObjectFieldStart("f_obj");
        Assert.assertEquals("fieldName:f_obj", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("{", g.tokens.get(g.tokens.size() - 1));

        g.writeObjectField("f_pojo", "pojoVal");
        Assert.assertEquals("fieldName:f_pojo", g.tokens.get(g.tokens.size() - 2));
        Assert.assertEquals("obj:pojoVal", g.tokens.get(g.tokens.size() - 1));

        g.writeOmittedField("f_omit"); // Should not fail or do anything
    }

    // =========================================================================
    // Helper Methods & _writeSimpleObject
    // =========================================================================

    @Test
    public void testWriteSimpleObject() throws IOException {
        g._writeSimpleObject(null);
        Assert.assertEquals("null", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject("strVal");
        Assert.assertEquals("str:strVal", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(10);
        Assert.assertEquals("int:10", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(20L);
        Assert.assertEquals("long:20", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(30.5d);
        Assert.assertEquals("double:30.5", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(40.5f);
        Assert.assertEquals("float:40.5", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject((short) 50);
        Assert.assertEquals("int:50", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject((byte) 60);
        Assert.assertEquals("int:60", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(BigInteger.valueOf(70));
        Assert.assertEquals("bigInt:70", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(new BigDecimal("80.5"));
        Assert.assertEquals("dec:80.5", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(new AtomicInteger(90));
        Assert.assertEquals("int:90", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(new AtomicLong(100L));
        Assert.assertEquals("long:100", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(new byte[]{1, 2});
        Assert.assertTrue(g.tokens.get(g.tokens.size() - 1).startsWith("binary:"));

        g._writeSimpleObject(Boolean.TRUE);
        Assert.assertEquals("bool:true", g.tokens.get(g.tokens.size() - 1));

        g._writeSimpleObject(new AtomicBoolean(false));
        Assert.assertEquals("bool:false", g.tokens.get(g.tokens.size() - 1));

        try {
            g._writeSimpleObject(new Object());
            Assert.fail("Expected IllegalStateException for unhandled object");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("No ObjectCodec defined"));
        }
    }

    @Test
    public void testProtectedHelpers() {
        try {
            g._reportError("custom error");
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertEquals("custom error", e.getMessage());
            Assert.assertSame(g, e.getProcessor());
        }

        try {
            g._reportUnsupportedOperation();
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("Operation not supported"));
        }
    }

    // =========================================================================
    // copyCurrentEvent & copyCurrentStructure
    // =========================================================================

    @Test(expected = JsonGenerationException.class)
    public void testCopyCurrentEventNull() throws IOException {
        MockJsonParser parser = new MockJsonParser(new ArrayList<MockToken>());
        g.copyCurrentEvent(parser);
    }

    @Test(expected = JsonGenerationException.class)
    public void testCopyCurrentEventNotAvailable() throws IOException {
        List<MockToken> tokens = new ArrayList<MockToken>();
        tokens.add(new MockToken(JsonToken.NOT_AVAILABLE));
        MockJsonParser parser = new MockJsonParser(tokens);
        parser.nextToken();
        g.copyCurrentEvent(parser);
    }

    @Test
    public void testCopyCurrentEventAllTypes() throws IOException {
        List<MockToken> list = new ArrayList<MockToken>();
        list.add(new MockToken(JsonToken.START_OBJECT));
        list.add(new MockToken(JsonToken.FIELD_NAME).name("prop"));
        list.add(new MockToken(JsonToken.VALUE_STRING).text("hello").textChars("hello".toCharArray()));
        list.add(new MockToken(JsonToken.VALUE_STRING).text("world")); // hasTextCharacters = false
        list.add(new MockToken(JsonToken.VALUE_NUMBER_INT).numType(JsonParser.NumberType.INT).intVal(100));
        list.add(new MockToken(JsonToken.VALUE_NUMBER_INT).numType(JsonParser.NumberType.BIG_INTEGER).bigIntVal(BigInteger.valueOf(200)));
        list.add(new MockToken(JsonToken.VALUE_NUMBER_INT).numType(JsonParser.NumberType.LONG).longVal(300L));
        list.add(new MockToken(JsonToken.VALUE_NUMBER_FLOAT).numType(JsonParser.NumberType.BIG_DECIMAL).decVal(new BigDecimal("1.23")));
        list.add(new MockToken(JsonToken.VALUE_NUMBER_FLOAT).numType(JsonParser.NumberType.FLOAT).floatVal(4.56f));
        list.add(new MockToken(JsonToken.VALUE_NUMBER_FLOAT).numType(JsonParser.NumberType.DOUBLE).doubleVal(7.89));
        list.add(new MockToken(JsonToken.VALUE_TRUE));
        list.add(new MockToken(JsonToken.VALUE_FALSE));
        list.add(new MockToken(JsonToken.VALUE_NULL));
        list.add(new MockToken(JsonToken.VALUE_EMBEDDED_OBJECT).embedded("embeddedVal"));
        list.add(new MockToken(JsonToken.START_ARRAY));
        list.add(new MockToken(JsonToken.END_ARRAY));
        list.add(new MockToken(JsonToken.END_OBJECT));

        MockJsonParser parser = new MockJsonParser(list);

        while (parser.nextToken() != null) {
            g.copyCurrentEvent(parser);
        }

        Assert.assertTrue(g.tokens.contains("{"));
        Assert.assertTrue(g.tokens.contains("fieldName:prop"));
        Assert.assertTrue(g.tokens.contains("strChars:hello"));
        Assert.assertTrue(g.tokens.contains("str:world"));
        Assert.assertTrue(g.tokens.contains("int:100"));
        Assert.assertTrue(g.tokens.contains("bigInt:200"));
        Assert.assertTrue(g.tokens.contains("long:300"));
        Assert.assertTrue(g.tokens.contains("dec:1.23"));
        Assert.assertTrue(g.tokens.contains("float:4.56"));
        Assert.assertTrue(g.tokens.contains("double:7.89"));
        Assert.assertTrue(g.tokens.contains("bool:true"));
        Assert.assertTrue(g.tokens.contains("bool:false"));
        Assert.assertTrue(g.tokens.contains("null"));
        Assert.assertTrue(g.tokens.contains("obj:embeddedVal"));
        Assert.assertTrue(g.tokens.contains("["));
        Assert.assertTrue(g.tokens.contains("]"));
        Assert.assertTrue(g.tokens.contains("}"));
    }

    @Test
    public void testCopyCurrentStructureNested() throws IOException {
        List<MockToken> list = new ArrayList<MockToken>();
        list.add(new MockToken(JsonToken.START_OBJECT));
        list.add(new MockToken(JsonToken.FIELD_NAME).name("arrayProp"));
        list.add(new MockToken(JsonToken.START_ARRAY));
        list.add(new MockToken(JsonToken.VALUE_NUMBER_INT).numType(JsonParser.NumberType.INT).intVal(1));
        list.add(new MockToken(JsonToken.VALUE_NUMBER_INT).numType(JsonParser.NumberType.INT).intVal(2));
        list.add(new MockToken(JsonToken.END_ARRAY));
        list.add(new MockToken(JsonToken.FIELD_NAME).name("scalarProp"));
        list.add(new MockToken(JsonToken.VALUE_TRUE));
        list.add(new MockToken(JsonToken.END_OBJECT));

        MockJsonParser parser = new MockJsonParser(list);
        parser.nextToken(); // At START_OBJECT

        g.copyCurrentStructure(parser);

        Assert.assertEquals("{", g.tokens.get(0));
        Assert.assertEquals("fieldName:arrayProp", g.tokens.get(1));
        Assert.assertEquals("[", g.tokens.get(2));
        Assert.assertEquals("int:1", g.tokens.get(3));
        Assert.assertEquals("int:2", g.tokens.get(4));
        Assert.assertEquals("]", g.tokens.get(5));
        Assert.assertEquals("fieldName:scalarProp", g.tokens.get(6));
        Assert.assertEquals("bool:true", g.tokens.get(7));
        Assert.assertEquals("}", g.tokens.get(8));
    }

    @Test
    public void testCopyCurrentStructureStartingWithFieldName() throws IOException {
        List<MockToken> list = new ArrayList<MockToken>();
        list.add(new MockToken(JsonToken.FIELD_NAME).name("onlyField"));
        list.add(new MockToken(JsonToken.VALUE_STRING).text("textVal"));

        MockJsonParser parser = new MockJsonParser(list);
        parser.nextToken(); // At FIELD_NAME

        g.copyCurrentStructure(parser);

        Assert.assertEquals("fieldName:onlyField", g.tokens.get(0));
        Assert.assertEquals("str:textVal", g.tokens.get(1));
    }

    @Test(expected = JsonGenerationException.class)
    public void testCopyCurrentStructureNullToken() throws IOException {
        MockJsonParser parser = new MockJsonParser(new ArrayList<MockToken>());
        g.copyCurrentStructure(parser);
    }

    // =========================================================================
    // Mock / Subclass Implementations
    // =========================================================================

    public static class SimpleGenerator extends JsonGenerator {
        public final List<String> tokens = new ArrayList<String>();
        private int _features = Feature.collectDefaults();
        private JsonStreamContext _context;

        public void setContext(JsonStreamContext ctxt) {
            this._context = ctxt;
        }

        @Override
        public JsonGenerator setCodec(ObjectCodec oc) { return this; }
        @Override
        public ObjectCodec getCodec() { return null; }
        @Override
        public Version version() { return Version.unknownVersion(); }

        @Override
        public JsonGenerator enable(Feature f) {
            _features |= f.getMask();
            return this;
        }

        @Override
        public JsonGenerator disable(Feature f) {
            _features &= ~f.getMask();
            return this;
        }

        @Override
        public boolean isEnabled(Feature f) {
            return (_features & f.getMask()) != 0;
        }

        @Override
        public int getFeatureMask() {
            return _features;
        }

        @Override
        @SuppressWarnings("deprecation")
        public JsonGenerator setFeatureMask(int values) {
            _features = values;
            return this;
        }

        @Override
        public JsonGenerator useDefaultPrettyPrinter() { return this; }

        @Override
        public void writeStartArray() throws IOException { tokens.add("["); }
        @Override
        public void writeEndArray() throws IOException { tokens.add("]"); }
        @Override
        public void writeStartObject() throws IOException { tokens.add("{"); }
        @Override
        public void writeEndObject() throws IOException { tokens.add("}"); }

        @Override
        public void writeFieldName(String name) throws IOException { tokens.add("fieldName:" + name); }
        @Override
        public void writeFieldName(SerializableString name) throws IOException { tokens.add("fieldName:" + name.getValue()); }

        @Override
        public void writeString(String text) throws IOException { tokens.add("str:" + text); }
        @Override
        public void writeString(char[] text, int offset, int len) throws IOException { tokens.add("strChars:" + new String(text, offset, len)); }
        @Override
        public void writeString(SerializableString text) throws IOException { tokens.add("str:" + text.getValue()); }
        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException { tokens.add("rawUtf8:" + new String(text, offset, length, "UTF-8")); }
        @Override
        public void writeUTF8String(byte[] text, int offset, int length) throws IOException { tokens.add("utf8:" + new String(text, offset, length, "UTF-8")); }

        @Override
        public void writeRaw(String text) throws IOException { tokens.add("raw:" + text); }
        @Override
        public void writeRaw(String text, int offset, int len) throws IOException { tokens.add("raw:" + text.substring(offset, offset + len)); }
        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException { tokens.add("raw:" + new String(text, offset, len)); }
        @Override
        public void writeRaw(char c) throws IOException { tokens.add("raw:" + c); }

        @Override
        public void writeRawValue(String text) throws IOException { tokens.add("rawValue:" + text); }
        @Override
        public void writeRawValue(String text, int offset, int len) throws IOException { tokens.add("rawValue:" + text.substring(offset, offset + len)); }
        @Override
        public void writeRawValue(char[] text, int offset, int len) throws IOException { tokens.add("rawValue:" + new String(text, offset, len)); }

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
            tokens.add("binary:" + len);
        }

        @Override
        public int writeBinary(Base64Variant bv, InputStream data, int dataLength) throws IOException {
            byte[] b = new byte[dataLength > 0 ? dataLength : 100];
            int read = data.read(b);
            tokens.add("binaryStream:" + read);
            return read;
        }

        @Override
        public void writeNumber(int v) throws IOException { tokens.add("int:" + v); }
        @Override
        public void writeNumber(long v) throws IOException { tokens.add("long:" + v); }
        @Override
        public void writeNumber(BigInteger v) throws IOException { tokens.add("bigInt:" + v); }
        @Override
        public void writeNumber(double v) throws IOException { tokens.add("double:" + v); }
        @Override
        public void writeNumber(float v) throws IOException { tokens.add("float:" + v); }
        @Override
        public void writeNumber(BigDecimal v) throws IOException { tokens.add("dec:" + v); }
        @Override
        public void writeNumber(String encodedValue) throws IOException { tokens.add("numStr:" + encodedValue); }

        @Override
        public void writeBoolean(boolean state) throws IOException { tokens.add("bool:" + state); }
        @Override
        public void writeNull() throws IOException { tokens.add("null"); }

        @Override
        public void writeObject(Object pojo) throws IOException { tokens.add("obj:" + pojo); }
        @Override
        public void writeTree(TreeNode rootNode) throws IOException { tokens.add("tree:" + rootNode); }

        @Override
        public JsonStreamContext getOutputContext() { return _context; }

        @Override
        public void flush() throws IOException {}
        @Override
        public boolean isClosed() { return false; }
        @Override
        public void close() throws IOException {}
    }

    public static class DummyContext extends JsonStreamContext {
        private Object _currValue;

        public DummyContext() {
            super();
        }

        @Override
        public String getCurrentName() { return null; }
        @Override
        public JsonStreamContext getParent() { return null; }

        @Override
        public Object getCurrentValue() {
            return _currValue;
        }

        @Override
        public void setCurrentValue(Object v) {
            _currValue = v;
        }
    }

    public static class MockToken {
        final JsonToken token;
        String name;
        String text;
        char[] textChars;
        JsonParser.NumberType numType;
        int intVal;
        long longVal;
        BigInteger bigIntVal;
        float floatVal;
        double doubleVal;
        BigDecimal decVal;
        Object embedded;

        public MockToken(JsonToken token) {
            this.token = token;
        }
        public MockToken name(String n) { this.name = n; return this; }
        public MockToken text(String t) { this.text = t; return this; }
        public MockToken textChars(char[] c) { this.textChars = c; return this; }
        public MockToken numType(JsonParser.NumberType nt) { this.numType = nt; return this; }
        public MockToken intVal(int v) { this.intVal = v; return this; }
        public MockToken longVal(long v) { this.longVal = v; return this; }
        public MockToken bigIntVal(BigInteger v) { this.bigIntVal = v; return this; }
        public MockToken floatVal(float v) { this.floatVal = v; return this; }
        public MockToken doubleVal(double v) { this.doubleVal = v; return this; }
        public MockToken decVal(BigDecimal v) { this.decVal = v; return this; }
        public MockToken embedded(Object o) { this.embedded = o; return this; }
    }

    public static class MockJsonParser extends JsonParser {
        private final List<MockToken> _tokens;
        private int _index = -1;

        public MockJsonParser(List<MockToken> tokens) {
            this._tokens = tokens;
        }

        private MockToken curr() {
            if (_index >= 0 && _index < _tokens.size()) {
                return _tokens.get(_index);
            }
            return null;
        }

        @Override
        public ObjectCodec getCodec() { return null; }
        @Override
        public void setCodec(ObjectCodec c) {}
        @Override
        public Version version() { return Version.unknownVersion(); }
        @Override
        public void close() throws IOException {}
        @Override
        public boolean isClosed() { return false; }
        @Override
        public JsonStreamContext getParsingContext() { return null; }
        @Override
        public JsonLocation getTokenLocation() { return null; }
        @Override
        public JsonLocation getCurrentLocation() { return null; }

        @Override
        public JsonToken nextToken() throws IOException {
            _index++;
            if (_index < _tokens.size()) {
                _currToken = _tokens.get(_index).token;
                return _currToken;
            }
            _currToken = null;
            return null;
        }

        @Override
        public JsonToken currentToken() {
            MockToken mt = curr();
            return mt == null ? null : mt.token;
        }

        @Override
        public String getCurrentName() throws IOException {
            MockToken mt = curr();
            return mt == null ? null : mt.name;
        }

        @Override
        public String getText() throws IOException {
            MockToken mt = curr();
            return mt == null ? null : mt.text;
        }

        @Override
        public boolean hasTextCharacters() {
            MockToken mt = curr();
            return mt != null && mt.textChars != null;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            MockToken mt = curr();
            return mt == null ? null : mt.textChars;
        }

        @Override
        public int getTextLength() throws IOException {
            MockToken mt = curr();
            return mt != null && mt.textChars != null ? mt.textChars.length : 0;
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public Number getNumberValue() throws IOException { return null; }

        @Override
        public NumberType getNumberType() throws IOException {
            MockToken mt = curr();
            return mt == null ? null : mt.numType;
        }

        @Override
        public int getIntValue() throws IOException {
            MockToken mt = curr();
            return mt == null ? 0 : mt.intVal;
        }

        @Override
        public long getLongValue() throws IOException {
            MockToken mt = curr();
            return mt == null ? 0L : mt.longVal;
        }

        @Override
        public BigInteger getBigIntegerValue() throws IOException {
            MockToken mt = curr();
            return mt == null ? null : mt.bigIntVal;
        }

        @Override
        public float getFloatValue() throws IOException {
            MockToken mt = curr();
            return mt == null ? 0.0f : mt.floatVal;
        }

        @Override
        public double getDoubleValue() throws IOException {
            MockToken mt = curr();
            return mt == null ? 0.0 : mt.doubleVal;
        }

        @Override
        public BigDecimal getDecimalValue() throws IOException {
            MockToken mt = curr();
            return mt == null ? null : mt.decVal;
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            MockToken mt = curr();
            return mt == null ? null : mt.embedded;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant bv) throws IOException { return new byte[0]; }
        @Override
        public void overrideCurrentName(String name) {}
        @Override
        public JsonParser skipChildren() throws IOException { return this; }
    }
}
