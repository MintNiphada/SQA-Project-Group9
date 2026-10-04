package com.fasterxml.jackson.core;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class JsonGeneratorTest {

    private TestJsonGenerator generator;

    @Before
    public void setUp() {
        generator = new TestJsonGenerator();
    }

    // Feature enum tests

    @Test
    public void testFeatureDefaults() {
        int defaults = Feature.collectDefaults();
        assertTrue(Feature.AUTO_CLOSE_TARGET.enabledIn(defaults));
        assertTrue(Feature.AUTO_CLOSE_JSON_CONTENT.enabledIn(defaults));
        assertTrue(Feature.FLUSH_PASSED_TO_STREAM.enabledIn(defaults));
        assertTrue(Feature.QUOTE_FIELD_NAMES.enabledIn(defaults));
        assertTrue(Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledIn(defaults));
        assertFalse(Feature.WRITE_NUMBERS_AS_STRINGS.enabledIn(defaults));
        assertFalse(Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(defaults));
        assertFalse(Feature.ESCAPE_NON_ASCII.enabledIn(defaults));
        assertFalse(Feature.STRICT_DUPLICATE_DETECTION.enabledIn(defaults));
        assertFalse(Feature.IGNORE_UNKNOWN.enabledIn(defaults));
    }

    @Test
    public void testFeatureEnabledByDefault() {
        assertTrue(Feature.AUTO_CLOSE_TARGET.enabledByDefault());
        assertTrue(Feature.AUTO_CLOSE_JSON_CONTENT.enabledByDefault());
        assertTrue(Feature.FLUSH_PASSED_TO_STREAM.enabledByDefault());
        assertTrue(Feature.QUOTE_FIELD_NAMES.enabledByDefault());
        assertTrue(Feature.QUOTE_NON_NUMERIC_NUMBERS.enabledByDefault());
        assertFalse(Feature.WRITE_NUMBERS_AS_STRINGS.enabledByDefault());
        assertFalse(Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledByDefault());
        assertFalse(Feature.ESCAPE_NON_ASCII.enabledByDefault());
        assertFalse(Feature.STRICT_DUPLICATE_DETECTION.enabledByDefault());
        assertFalse(Feature.IGNORE_UNKNOWN.enabledByDefault());
    }

    @Test
    public void testFeatureEnabledIn() {
        int mask = Feature.AUTO_CLOSE_TARGET.getMask() | Feature.QUOTE_FIELD_NAMES.getMask();
        assertTrue(Feature.AUTO_CLOSE_TARGET.enabledIn(mask));
        assertTrue(Feature.QUOTE_FIELD_NAMES.enabledIn(mask));
        assertFalse(Feature.AUTO_CLOSE_JSON_CONTENT.enabledIn(mask));
    }

    @Test
    public void testFeatureGetMask() {
        assertEquals(1 << Feature.AUTO_CLOSE_TARGET.ordinal(), Feature.AUTO_CLOSE_TARGET.getMask());
        assertEquals(1 << Feature.QUOTE_FIELD_NAMES.ordinal(), Feature.QUOTE_FIELD_NAMES.getMask());
    }

    // Configuration tests

    @Test
    public void testSetCodec() {
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public Version version() { return null; }
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) { }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
        };
        generator.setCodec(codec);
        assertSame(codec, generator.getCodec());
    }

    @Test
    public void testGetCodecInitiallyNull() {
        assertNull(generator.getCodec());
    }

    @Test
    public void testVersion() {
        assertNotNull(generator.version());
    }

    // Feature configuration tests

    @Test
    public void testEnableDisable() {
        generator.enable(Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue(generator.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
        generator.disable(Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(generator.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    @Test
    public void testConfigure() {
        generator.configure(Feature.ESCAPE_NON_ASCII, true);
        assertTrue(generator.isEnabled(Feature.ESCAPE_NON_ASCII));
        generator.configure(Feature.ESCAPE_NON_ASCII, false);
        assertFalse(generator.isEnabled(Feature.ESCAPE_NON_ASCII));
    }

    @Test
    public void testGetFeatureMask() {
        int initial = generator.getFeatureMask();
        generator.enable(Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        int after = generator.getFeatureMask();
        assertTrue(Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(after));
        assertFalse(Feature.WRITE_BIGDECIMAL_AS_PLAIN.enabledIn(initial));
    }

    @Test
    public void testSetFeatureMask() {
        int mask = Feature.AUTO_CLOSE_TARGET.getMask() | Feature.QUOTE_FIELD_NAMES.getMask();
        generator.setFeatureMask(mask);
        assertEquals(mask, generator.getFeatureMask());
    }

    @Test
    public void testOverrideStdFeatures() {
        int mask = Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask();
        generator.overrideStdFeatures(mask, mask);
        assertTrue(generator.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
        assertTrue(generator.isEnabled(Feature.ESCAPE_NON_ASCII));
    }

    @Test
    public void testGetFormatFeatures() {
        assertEquals(0, generator.getFormatFeatures());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverrideFormatFeaturesThrows() {
        generator.overrideFormatFeatures(0, 0);
    }

    // Schema tests

    @Test(expected = UnsupportedOperationException.class)
    public void testSetSchemaThrowsByDefault() {
        generator.setSchema(new FormatSchema() {
            @Override
            public String getSchemaType() { return "test"; }
        });
    }

    @Test
    public void testGetSchemaDefaultNull() {
        assertNull(generator.getSchema());
    }

    @Test
    public void testCanUseSchemaDefaultFalse() {
        assertFalse(generator.canUseSchema(new FormatSchema() {
            @Override
            public String getSchemaType() { return "test"; }
        }));
    }

    // PrettyPrinter tests

    @Test
    public void testSetPrettyPrinter() {
        PrettyPrinter pp = new PrettyPrinter() {
            @Override
            public void writeRootValueSeparator(JsonGenerator gen) { }
            @Override
            public void writeStartObject(JsonGenerator gen) { }
            @Override
            public void writeEndObject(JsonGenerator gen, int nrOfEntries) { }
            @Override
            public void writeStartArray(JsonGenerator gen) { }
            @Override
            public void writeEndArray(JsonGenerator gen, int nrOfValues) { }
            @Override
            public void writeObjectEntrySeparator(JsonGenerator gen) { }
            @Override
            public void writeObjectFieldValueSeparator(JsonGenerator gen) { }
            @Override
            public void writeArrayValueSeparator(JsonGenerator gen) { }
            @Override
            public void beforeArrayValues(JsonGenerator gen) { }
            @Override
            public void beforeObjectEntries(JsonGenerator gen) { }
        };
        generator.setPrettyPrinter(pp);
        assertSame(pp, generator.getPrettyPrinter());
    }

    @Test
    public void testGetPrettyPrinterInitiallyNull() {
        assertNull(generator.getPrettyPrinter());
    }

    @Test
    public void testUseDefaultPrettyPrinter() {
        generator.useDefaultPrettyPrinter();
        assertNotNull(generator.getPrettyPrinter());
    }

    // Escaping tests

    @Test
    public void testSetHighestNonEscapedChar() {
        generator.setHighestNonEscapedChar(127);
        assertEquals(127, generator.getHighestEscapedChar());
    }

    @Test
    public void testGetHighestEscapedCharDefault() {
        assertEquals(0, generator.getHighestEscapedChar());
    }

    @Test
    public void testGetCharacterEscapesDefaultNull() {
        assertNull(generator.getCharacterEscapes());
    }

    @Test
    public void testSetCharacterEscapes() {
        CharacterEscapes escapes = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() { return new int[0]; }
            @Override
            public SerializableString getEscapeSequence(int ch) { return null; }
        };
        generator.setCharacterEscapes(escapes);
        assertSame(escapes, generator.getCharacterEscapes());
    }

    // Root value separator

    @Test(expected = UnsupportedOperationException.class)
    public void testSetRootValueSeparatorThrowsByDefault() {
        generator.setRootValueSeparator(new SerializableString() {
            @Override
            public String getValue() { return " "; }
            @Override
            public int charLength() { return 1; }
            @Override
            public char[] asQuotedChars() { return new char[]{' '}; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[]{' '}; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[]{' '}; }
        });
    }

    // Output state tests

    @Test
    public void testGetOutputTargetDefaultNull() {
        assertNull(generator.getOutputTarget());
    }

    @Test
    public void testGetOutputBufferedDefaultMinusOne() {
        assertEquals(-1, generator.getOutputBuffered());
    }

    @Test
    public void testGetCurrentValueWhenContextNull() {
        generator.setOutputContext(null);
        assertNull(generator.getCurrentValue());
    }

    @Test
    public void testGetCurrentValueWhenContextSet() {
        JsonStreamContext ctxt = new JsonStreamContext() {
            @Override
            public JsonStreamContext getParent() { return null; }
            @Override
            public String getCurrentName() { return null; }
            @Override
            public Object getCurrentValue() { return "testValue"; }
            @Override
            public void setCurrentValue(Object v) { }
            @Override
            public boolean hasCurrentIndex() { return false; }
            @Override
            public int getCurrentIndex() { return -1; }
            @Override
            public int getEntryCount() { return 0; }
        };
        generator.setOutputContext(ctxt);
        assertEquals("testValue", generator.getCurrentValue());
    }

    @Test
    public void testSetCurrentValue() {
        final Object[] holder = new Object[1];
        JsonStreamContext ctxt = new JsonStreamContext() {
            @Override
            public JsonStreamContext getParent() { return null; }
            @Override
            public String getCurrentName() { return null; }
            @Override
            public Object getCurrentValue() { return holder[0]; }
            @Override
            public void setCurrentValue(Object v) { holder[0] = v; }
            @Override
            public boolean hasCurrentIndex() { return false; }
            @Override
            public int getCurrentIndex() { return -1; }
            @Override
            public int getEntryCount() { return 0; }
        };
        generator.setOutputContext(ctxt);
        generator.setCurrentValue("newValue");
        assertEquals("newValue", holder[0]);
    }

    // Capability introspection tests

    @Test
    public void testCanWriteObjectIdDefaultFalse() {
        assertFalse(generator.canWriteObjectId());
    }

    @Test
    public void testCanWriteTypeIdDefaultFalse() {
        assertFalse(generator.canWriteTypeId());
    }

    @Test
    public void testCanWriteBinaryNativelyDefaultFalse() {
        assertFalse(generator.canWriteBinaryNatively());
    }

    @Test
    public void testCanOmitFieldsDefaultTrue() {
        assertTrue(generator.canOmitFields());
    }

    @Test
    public void testCanWriteFormattedNumbersDefaultFalse() {
        assertFalse(generator.canWriteFormattedNumbers());
    }

    // Structural write methods

    @Test
    public void testWriteStartArray() throws IOException {
        generator.writeStartArray();
        assertTrue(generator.startArrayCalled);
    }

    @Test
    public void testWriteStartArrayWithSize() throws IOException {
        generator.writeStartArray(5);
        assertTrue(generator.startArrayCalled);
    }

    @Test
    public void testWriteEndArray() throws IOException {
        generator.writeEndArray();
        assertTrue(generator.endArrayCalled);
    }

    @Test
    public void testWriteStartObject() throws IOException {
        generator.writeStartObject();
        assertTrue(generator.startObjectCalled);
    }

    @Test
    public void testWriteStartObjectWithValue() throws IOException {
        Object forValue = new Object();
        generator.writeStartObject(forValue);
        assertTrue(generator.startObjectCalled);
        assertEquals(forValue, generator.getCurrentValue());
    }

    @Test
    public void testWriteEndObject() throws IOException {
        generator.writeEndObject();
        assertTrue(generator.endObjectCalled);
    }

    @Test
    public void testWriteFieldNameString() throws IOException {
        generator.writeFieldName("test");
        assertEquals("test", generator.lastFieldName);
    }

    @Test
    public void testWriteFieldNameSerializableString() throws IOException {
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "serializable"; }
            @Override
            public int charLength() { return 12; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        generator.writeFieldName(ss);
        assertEquals("serializable", generator.lastFieldName);
    }

    @Test
    public void testWriteFieldId() throws IOException {
        generator.writeFieldId(123L);
        assertEquals("123", generator.lastFieldName);
    }

    // Scalar array tests

    @Test
    public void testWriteIntArray() throws IOException {
        int[] arr = {1, 2, 3};
        generator.writeArray(arr, 0, 3);
        assertTrue(generator.startArrayCalled);
        assertTrue(generator.endArrayCalled);
        assertEquals(3, generator.writtenInts.size());
        assertEquals(1, generator.writtenInts.get(0).intValue());
        assertEquals(2, generator.writtenInts.get(1).intValue());
        assertEquals(3, generator.writtenInts.get(2).intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteIntArrayNull() throws IOException {
        generator.writeArray((int[]) null, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteIntArrayInvalidOffsets() throws IOException {
        int[] arr = {1, 2};
        generator.writeArray(arr, 1, 2);
    }

    @Test
    public void testWriteLongArray() throws IOException {
        long[] arr = {10L, 20L};
        generator.writeArray(arr, 0, 2);
        assertTrue(generator.startArrayCalled);
        assertTrue(generator.endArrayCalled);
        assertEquals(2, generator.writtenLongs.size());
        assertEquals(10L, generator.writtenLongs.get(0).longValue());
        assertEquals(20L, generator.writtenLongs.get(1).longValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteLongArrayNull() throws IOException {
        generator.writeArray((long[]) null, 0, 0);
    }

    @Test
    public void testWriteDoubleArray() throws IOException {
        double[] arr = {1.5, 2.5};
        generator.writeArray(arr, 0, 2);
        assertTrue(generator.startArrayCalled);
        assertTrue(generator.endArrayCalled);
        assertEquals(2, generator.writtenDoubles.size());
        assertEquals(1.5, generator.writtenDoubles.get(0), 0.0);
        assertEquals(2.5, generator.writtenDoubles.get(1), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteDoubleArrayNull() throws IOException {
        generator.writeArray((double[]) null, 0, 0);
    }

    // Text/String tests

    @Test
    public void testWriteString() throws IOException {
        generator.writeString("hello");
        assertEquals("hello", generator.lastString);
    }

    @Test
    public void testWriteStringCharArray() throws IOException {
        char[] text = {'w', 'o', 'r', 'l', 'd'};
        generator.writeString(text, 0, 5);
        assertEquals("world", generator.lastString);
    }

    @Test
    public void testWriteStringSerializableString() throws IOException {
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "serializable"; }
            @Override
            public int charLength() { return 12; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        generator.writeString(ss);
        assertEquals("serializable", generator.lastString);
    }

    @Test
    public void testWriteRawUTF8String() throws IOException {
        byte[] data = {65, 66, 67}; // "ABC"
        generator.writeRawUTF8String(data, 0, 3);
        assertArrayEquals(data, generator.lastRawUTF8);
        assertEquals(0, generator.lastRawUTF8Offset);
        assertEquals(3, generator.lastRawUTF8Length);
    }

    @Test
    public void testWriteUTF8String() throws IOException {
        byte[] data = {68, 69, 70}; // "DEF"
        generator.writeUTF8String(data, 0, 3);
        assertArrayEquals(data, generator.lastUTF8);
        assertEquals(0, generator.lastUTF8Offset);
        assertEquals(3, generator.lastUTF8Length);
    }

    // Raw content tests

    @Test
    public void testWriteRawString() throws IOException {
        generator.writeRaw("raw");
        assertEquals("raw", generator.lastRawString);
    }

    @Test
    public void testWriteRawStringOffsetLen() throws IOException {
        generator.writeRaw("prefix", 2, 3);
        assertEquals("efi", generator.lastRawString);
    }

    @Test
    public void testWriteRawCharArray() throws IOException {
        char[] text = {'a', 'b', 'c'};
        generator.writeRaw(text, 0, 3);
        assertEquals("abc", generator.lastRawString);
    }

    @Test
    public void testWriteRawChar() throws IOException {
        generator.writeRaw('X');
        assertEquals("X", generator.lastRawString);
    }

    @Test
    public void testWriteRawSerializableString() throws IOException {
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "serializableRaw"; }
            @Override
            public int charLength() { return 14; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        generator.writeRaw(ss);
        assertEquals("serializableRaw", generator.lastRawString);
    }

    @Test
    public void testWriteRawValueString() throws IOException {
        generator.writeRawValue("value");
        assertEquals("value", generator.lastRawValue);
    }

    @Test
    public void testWriteRawValueStringOffsetLen() throws IOException {
        generator.writeRawValue("abcdef", 2, 3);
        assertEquals("cde", generator.lastRawValue);
    }

    @Test
    public void testWriteRawValueCharArray() throws IOException {
        char[] text = {'x', 'y', 'z'};
        generator.writeRawValue(text, 0, 3);
        assertEquals("xyz", generator.lastRawValue);
    }

    @Test
    public void testWriteRawValueSerializableString() throws IOException {
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "serializableValue"; }
            @Override
            public int charLength() { return 16; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        generator.writeRawValue(ss);
        assertEquals("serializableValue", generator.lastRawValue);
    }

    // Binary tests

    @Test
    public void testWriteBinaryBase64Variant() throws IOException {
        Base64Variant bv = Base64Variants.MIME;
        byte[] data = {1, 2, 3};
        generator.writeBinary(bv, data, 0, 3);
        assertSame(bv, generator.lastBase64Variant);
        assertArrayEquals(data, generator.lastBinaryData);
        assertEquals(0, generator.lastBinaryOffset);
        assertEquals(3, generator.lastBinaryLength);
    }

    @Test
    public void testWriteBinaryDefaultVariant() throws IOException {
        byte[] data = {4, 5};
        generator.writeBinary(data, 0, 2);
        assertEquals(Base64Variants.getDefaultVariant(), generator.lastBase64Variant);
        assertArrayEquals(data, generator.lastBinaryData);
    }

    @Test
    public void testWriteBinaryByteArray() throws IOException {
        byte[] data = {6, 7, 8};
        generator.writeBinary(data);
        assertEquals(Base64Variants.getDefaultVariant(), generator.lastBase64Variant);
        assertArrayEquals(data, generator.lastBinaryData);
        assertEquals(0, generator.lastBinaryOffset);
        assertEquals(3, generator.lastBinaryLength);
    }

    @Test
    public void testWriteBinaryInputStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[]{9, 10});
        generator.writeBinary(in, 2);
        assertSame(in, generator.lastBinaryStream);
        assertEquals(2, generator.lastBinaryStreamLength);
    }

    @Test
    public void testWriteBinaryInputStreamWithVariant() throws IOException {
        Base64Variant bv = Base64Variants.MIME_NO_LINEFEEDS;
        InputStream in = new ByteArrayInputStream(new byte[]{11});
        generator.writeBinary(bv, in, 1);
        assertSame(bv, generator.lastBase64Variant);
        assertSame(in, generator.lastBinaryStream);
        assertEquals(1, generator.lastBinaryStreamLength);
    }

    // Numeric tests

    @Test
    public void testWriteNumberShort() throws IOException {
        generator.writeNumber((short) 42);
        assertEquals(42, generator.lastInt);
    }

    @Test
    public void testWriteNumberInt() throws IOException {
        generator.writeNumber(123);
        assertEquals(123, generator.lastInt);
    }

    @Test
    public void testWriteNumberLong() throws IOException {
        generator.writeNumber(456L);
        assertEquals(456L, generator.lastLong);
    }

    @Test
    public void testWriteNumberBigInteger() throws IOException {
        BigInteger bi = new BigInteger("789");
        generator.writeNumber(bi);
        assertEquals(bi, generator.lastBigInteger);
    }

    @Test
    public void testWriteNumberDouble() throws IOException {
        generator.writeNumber(3.14);
        assertEquals(3.14, generator.lastDouble, 0.0);
    }

    @Test
    public void testWriteNumberFloat() throws IOException {
        generator.writeNumber(2.5f);
        assertEquals(2.5f, generator.lastFloat, 0.0);
    }

    @Test
    public void testWriteNumberBigDecimal() throws IOException {
        BigDecimal bd = new BigDecimal("1.23");
        generator.writeNumber(bd);
        assertEquals(bd, generator.lastBigDecimal);
    }

    @Test
    public void testWriteNumberString() throws IOException {
        generator.writeNumber("12345");
        assertEquals("12345", generator.lastNumberString);
    }

    // Other value tests

    @Test
    public void testWriteBoolean() throws IOException {
        generator.writeBoolean(true);
        assertTrue(generator.lastBoolean);
        generator.writeBoolean(false);
        assertFalse(generator.lastBoolean);
    }

    @Test
    public void testWriteNull() throws IOException {
        generator.writeNull();
        assertTrue(generator.nullCalled);
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEmbeddedObjectThrowsByDefault() throws IOException {
        generator.writeEmbeddedObject(new Object());
    }

    // Native Ids tests

    @Test(expected = JsonGenerationException.class)
    public void testWriteObjectIdThrowsByDefault() throws IOException {
        generator.writeObjectId("id");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteObjectRefThrowsByDefault() throws IOException {
        generator.writeObjectRef("ref");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteTypeIdThrowsByDefault() throws IOException {
        generator.writeTypeId("type");
    }

    // Object writing tests

    @Test
    public void testWriteObject() throws IOException {
        Object pojo = new Object();
        generator.writeObject(pojo);
        assertSame(pojo, generator.lastWrittenObject);
    }

    @Test
    public void testWriteTree() throws IOException {
        TreeNode node = new TreeNode() {
            @Override
            public JsonToken asToken() { return null; }
            @Override
            public JsonParser.NumberType numberType() { return null; }
            @Override
            public int size() { return 0; }
            @Override
            public boolean isValueNode() { return false; }
            @Override
            public boolean isContainerNode() { return false; }
            @Override
            public boolean isMissingNode() { return false; }
            @Override
            public boolean isArray() { return false; }
            @Override
            public boolean isObject() { return false; }
            @Override
            public TreeNode get(String fieldName) { return null; }
            @Override
            public TreeNode get(int index) { return null; }
            @Override
            public TreeNode path(String fieldName) { return null; }
            @Override
            public TreeNode path(int index) { return null; }
            @Override
            public java.util.Iterator<String> fieldNames() { return null; }
            @Override
            public TreeNode at(com.fasterxml.jackson.core.JsonPointer ptr) { return null; }
            @Override
            public TreeNode at(String jsonPointerExpression) { return null; }
            @Override
            public JsonParser traverse() { return null; }
            @Override
            public JsonParser traverse(ObjectCodec codec) { return null; }
        };
        generator.writeTree(node);
        assertSame(node, generator.lastTreeNode);
    }

    // Convenience field methods

    @Test
    public void testWriteStringField() throws IOException {
        generator.writeStringField("field", "value");
        assertEquals("field", generator.lastFieldName);
        assertEquals("value", generator.lastString);
    }

    @Test
    public void testWriteBooleanField() throws IOException {
        generator.writeBooleanField("flag", true);
        assertEquals("flag", generator.lastFieldName);
        assertTrue(generator.lastBoolean);
    }

    @Test
    public void testWriteNullField() throws IOException {
        generator.writeNullField("nullField");
        assertEquals("nullField", generator.lastFieldName);
        assertTrue(generator.nullCalled);
    }

    @Test
    public void testWriteNumberFieldInt() throws IOException {
        generator.writeNumberField("intField", 42);
        assertEquals("intField", generator.lastFieldName);
        assertEquals(42, generator.lastInt);
    }

    @Test
    public void testWriteNumberFieldLong() throws IOException {
        generator.writeNumberField("longField", 99L);
        assertEquals("longField", generator.lastFieldName);
        assertEquals(99L, generator.lastLong);
    }

    @Test
    public void testWriteNumberFieldDouble() throws IOException {
        generator.writeNumberField("doubleField", 3.14);
        assertEquals("doubleField", generator.lastFieldName);
        assertEquals(3.14, generator.lastDouble, 0.0);
    }

    @Test
    public void testWriteNumberFieldFloat() throws IOException {
        generator.writeNumberField("floatField", 2.5f);
        assertEquals("floatField", generator.lastFieldName);
        assertEquals(2.5f, generator.lastFloat, 0.0);
    }

    @Test
    public void testWriteNumberFieldBigDecimal() throws IOException {
        BigDecimal bd = new BigDecimal("1.23");
        generator.writeNumberField("bdField", bd);
        assertEquals("bdField", generator.lastFieldName);
        assertEquals(bd, generator.lastBigDecimal);
    }

    @Test
    public void testWriteBinaryField() throws IOException {
        byte[] data = {1, 2};
        generator.writeBinaryField("binField", data);
        assertEquals("binField", generator.lastFieldName);
        assertArrayEquals(data, generator.lastBinaryData);
    }

    @Test
    public void testWriteArrayFieldStart() throws IOException {
        generator.writeArrayFieldStart("arrField");
        assertEquals("arrField", generator.lastFieldName);
        assertTrue(generator.startArrayCalled);
    }

    @Test
    public void testWriteObjectFieldStart() throws IOException {
        generator.writeObjectFieldStart("objField");
        assertEquals("objField", generator.lastFieldName);
        assertTrue(generator.startObjectCalled);
    }

    @Test
    public void testWriteObjectField() throws IOException {
        Object pojo = new Object();
        generator.writeObjectField("objField", pojo);
        assertEquals("objField", generator.lastFieldName);
        assertSame(pojo, generator.lastWrittenObject);
    }

    @Test
    public void testWriteOmittedField() throws IOException {
        generator.writeOmittedField("omitted");
        // no exception, no effect
    }

    // Copy methods tests

    @Test
    public void testCopyCurrentEvent() throws IOException {
        JsonFactory f = new JsonFactory();
        String json = "{\"a\":1, \"b\":true, \"c\":null, \"d\":[1,2], \"e\":{\"f\":\"g\"}}";
        JsonParser p = f.createParser(json);
        // advance to first field name "a"
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME "a"
        p.nextToken(); // VALUE_NUMBER_INT 1
        generator.copyCurrentEvent(p);
        assertEquals(1, generator.lastInt);
        // next field "b" boolean
        p.nextToken(); // FIELD_NAME "b"
        p.nextToken(); // VALUE_TRUE
        generator.copyCurrentEvent(p);
        assertTrue(generator.lastBoolean);
        // "c" null
        p.nextToken(); // FIELD_NAME "c"
        p.nextToken(); // VALUE_NULL
        generator.copyCurrentEvent(p);
        assertTrue(generator.nullCalled);
        // "d" start array
        p.nextToken(); // FIELD_NAME "d"
        p.nextToken(); // START_ARRAY
        generator.copyCurrentEvent(p);
        assertTrue(generator.startArrayCalled);
        // "e" start object
        p.nextToken(); // FIELD_NAME "e"
        p.nextToken(); // START_OBJECT
        generator.copyCurrentEvent(p);
        assertTrue(generator.startObjectCalled);
        p.close();
    }

    @Test
    public void testCopyCurrentStructure() throws IOException {
        JsonFactory f = new JsonFactory();
        String json = "{\"arr\":[1,2], \"obj\":{\"x\":true}}";
        JsonParser p = f.createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME "arr"
        generator.copyCurrentStructure(p);
        // should have written field name "arr", start array, numbers, end array
        assertEquals("arr", generator.lastFieldName);
        assertTrue(generator.startArrayCalled);
        assertTrue(generator.endArrayCalled);
        assertEquals(2, generator.writtenInts.size());
        assertEquals(1, generator.writtenInts.get(0).intValue());
        assertEquals(2, generator.writtenInts.get(1).intValue());
        // next field "obj"
        p.nextToken(); // FIELD_NAME "obj"
        generator.copyCurrentStructure(p);
        assertEquals("obj", generator.lastFieldName);
        assertTrue(generator.startObjectCalled);
        assertTrue(generator.endObjectCalled);
        // inside object, field "x" with true
        assertEquals("x", generator.lastFieldName);
        assertTrue(generator.lastBoolean);
        p.close();
    }

    // Context test

    @Test
    public void testGetOutputContext() {
        JsonStreamContext ctxt = generator.getOutputContext();
        assertNotNull(ctxt);
    }

    // Flush/Close tests

    @Test
    public void testFlush() throws IOException {
        generator.flush();
        assertTrue(generator.flushCalled);
    }

    @Test
    public void testClose() throws IOException {
        generator.close();
        assertTrue(generator.closeCalled);
    }

    @Test
    public void testIsClosed() throws IOException {
        assertFalse(generator.isClosed());
        generator.close();
        assertTrue(generator.isClosed());
    }

    // Helper methods tests

    @Test(expected = JsonGenerationException.class)
    public void testReportError() throws JsonGenerationException {
        generator.callReportError("test error");
    }

    @Test(expected = RuntimeException.class)
    public void testThrowInternal() {
        generator.callThrowInternal();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testReportUnsupportedOperation() {
        generator.callReportUnsupportedOperation();
    }

    @Test
    public void testVerifyOffsetsValid() {
        generator.callVerifyOffsets(10, 0, 10);
        generator.callVerifyOffsets(10, 5, 5);
        generator.callVerifyOffsets(10, 10, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyOffsetsInvalidNegativeOffset() {
        generator.callVerifyOffsets(10, -1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyOffsetsInvalidLengthExceeds() {
        generator.callVerifyOffsets(10, 5, 6);
    }

    @Test
    public void testWriteSimpleObjectNull() throws IOException {
        generator.callWriteSimpleObject(null);
        assertTrue(generator.nullCalled);
    }

    @Test
    public void testWriteSimpleObjectString() throws IOException {
        generator.callWriteSimpleObject("test");
        assertEquals("test", generator.lastString);
    }

    @Test
    public void testWriteSimpleObjectInteger() throws IOException {
        generator.callWriteSimpleObject(Integer.valueOf(42));
        assertEquals(42, generator.lastInt);
    }

    @Test
    public void testWriteSimpleObjectLong() throws IOException {
        generator.callWriteSimpleObject(Long.valueOf(99L));
        assertEquals(99L, generator.lastLong);
    }

    @Test
    public void testWriteSimpleObjectDouble() throws IOException {
        generator.callWriteSimpleObject(Double.valueOf(3.14));
        assertEquals(3.14, generator.lastDouble, 0.0);
    }

    @Test
    public void testWriteSimpleObjectFloat() throws IOException {
        generator.callWriteSimpleObject(Float.valueOf(2.5f));
        assertEquals(2.5f, generator.lastFloat, 0.0);
    }

    @Test
    public void testWriteSimpleObjectShort() throws IOException {
        generator.callWriteSimpleObject(Short.valueOf((short) 10));
        assertEquals(10, generator.lastInt); // short is written as int
    }

    @Test
    public void testWriteSimpleObjectByte() throws IOException {
        generator.callWriteSimpleObject(Byte.valueOf((byte) 7));
        assertEquals(7, generator.lastInt);
    }

    @Test
    public void testWriteSimpleObjectBigInteger() throws IOException {
        BigInteger bi = new BigInteger("123456789");
        generator.callWriteSimpleObject(bi);
        assertEquals(bi, generator.lastBigInteger);
    }

    @Test
    public void testWriteSimpleObjectBigDecimal() throws IOException {
        BigDecimal bd = new BigDecimal("1.23");
        generator.callWriteSimpleObject(bd);
        assertEquals(bd, generator.lastBigDecimal);
    }

    @Test
    public void testWriteSimpleObjectAtomicInteger() throws IOException {
        AtomicInteger ai = new AtomicInteger(77);
        generator.callWriteSimpleObject(ai);
        assertEquals(77, generator.lastInt);
    }

    @Test
    public void testWriteSimpleObjectAtomicLong() throws IOException {
        AtomicLong al = new AtomicLong(88L);
        generator.callWriteSimpleObject(al);
        assertEquals(88L, generator.lastLong);
    }

    @Test
    public void testWriteSimpleObjectByteArray() throws IOException {
        byte[] data = {1, 2, 3};
        generator.callWriteSimpleObject(data);
        assertArrayEquals(data, generator.lastBinaryData);
    }

    @Test
    public void testWriteSimpleObjectBoolean() throws IOException {
        generator.callWriteSimpleObject(Boolean.TRUE);
        assertTrue(generator.lastBoolean);
    }

    @Test
    public void testWriteSimpleObjectAtomicBoolean() throws IOException {
        AtomicBoolean ab = new AtomicBoolean(true);
        generator.callWriteSimpleObject(ab);
        assertTrue(generator.lastBoolean);
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteSimpleObjectUnknownType() throws IOException {
        generator.callWriteSimpleObject(new Object());
    }

    // Concrete generator subclass for testing

    static class TestJsonGenerator extends JsonGenerator {
        boolean startArrayCalled;
        boolean endArrayCalled;
        boolean startObjectCalled;
        boolean endObjectCalled;
        String lastFieldName;
        String lastString;
        int lastInt;
        long lastLong;
        BigInteger lastBigInteger;
        double lastDouble;
        float lastFloat;
        BigDecimal lastBigDecimal;
        String lastNumberString;
        boolean lastBoolean;
        boolean nullCalled;
        byte[] lastBinaryData;
        int lastBinaryOffset;
        int lastBinaryLength;
        Base64Variant lastBase64Variant;
        InputStream lastBinaryStream;
        int lastBinaryStreamLength;
        String lastRawString;
        String lastRawValue;
        byte[] lastRawUTF8;
        int lastRawUTF8Offset;
        int lastRawUTF8Length;
        byte[] lastUTF8;
        int lastUTF8Offset;
        int lastUTF8Length;
        Object lastWrittenObject;
        TreeNode lastTreeNode;
        java.util.List<Integer> writtenInts = new java.util.ArrayList<>();
        java.util.List<Long> writtenLongs = new java.util.ArrayList<>();
        java.util.List<Double> writtenDoubles = new java.util.ArrayList<>();
        boolean flushCalled;
        boolean closeCalled;
        boolean closed;
        JsonStreamContext outputContext;
        ObjectCodec codec;
        int featureMask = Feature.collectDefaults();
        PrettyPrinter prettyPrinter;
        int highestNonEscapedChar;
        CharacterEscapes characterEscapes;
        FormatSchema schema;

        @Override
        public JsonGenerator setCodec(ObjectCodec oc) {
            this.codec = oc;
            return this;
        }

        @Override
        public ObjectCodec getCodec() {
            return codec;
        }

        @Override
        public Version version() {
            return new Version(1, 0, 0, null, null, null);
        }

        @Override
        public JsonGenerator enable(Feature f) {
            featureMask |= f.getMask();
            return this;
        }

        @Override
        public JsonGenerator disable(Feature f) {
            featureMask &= ~f.getMask();
            return this;
        }

        @Override
        public boolean isEnabled(Feature f) {
            return f.enabledIn(featureMask);
        }

        @Override
        public int getFeatureMask() {
            return featureMask;
        }

        @Override
        public JsonGenerator setFeatureMask(int values) {
            featureMask = values;
            return this;
        }

        @Override
        public JsonGenerator useDefaultPrettyPrinter() {
            prettyPrinter = new com.fasterxml.jackson.core.util.DefaultPrettyPrinter();
            return this;
        }

        @Override
        public void writeStartArray() {
            startArrayCalled = true;
        }

        @Override
        public void writeEndArray() {
            endArrayCalled = true;
        }

        @Override
        public void writeStartObject() {
            startObjectCalled = true;
        }

        @Override
        public void writeEndObject() {
            endObjectCalled = true;
        }

        @Override
        public void writeFieldName(String name) {
            lastFieldName = name;
        }

        @Override
        public void writeFieldName(SerializableString name) {
            lastFieldName = name.getValue();
        }

        @Override
        public void writeString(String text) {
            lastString = text;
        }

        @Override
        public void writeString(char[] text, int offset, int len) {
            lastString = new String(text, offset, len);
        }

        @Override
        public void writeString(SerializableString text) {
            lastString = text.getValue();
        }

        @Override
        public void writeRawUTF8String(byte[] text, int offset, int length) {
            lastRawUTF8 = text;
            lastRawUTF8Offset = offset;
            lastRawUTF8Length = length;
        }

        @Override
        public void writeUTF8String(byte[] text, int offset, int length) {
            lastUTF8 = text;
            lastUTF8Offset = offset;
            lastUTF8Length = length;
        }

        @Override
        public void writeRaw(String text) {
            lastRawString = text;
        }

        @Override
        public void writeRaw(String text, int offset, int len) {
            lastRawString = text.substring(offset, offset + len);
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) {
            lastRawString = new String(text, offset, len);
        }

        @Override
        public void writeRaw(char c) {
            lastRawString = String.valueOf(c);
        }

        @Override
        public void writeRawValue(String text) {
            lastRawValue = text;
        }

        @Override
        public void writeRawValue(String text, int offset, int len) {
            lastRawValue = text.substring(offset, offset + len);
        }

        @Override
        public void writeRawValue(char[] text, int offset, int len) {
            lastRawValue = new String(text, offset, len);
        }

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) {
            lastBase64Variant = bv;
            lastBinaryData = data;
            lastBinaryOffset = offset;
            lastBinaryLength = len;
        }

        @Override
        public int writeBinary(Base64Variant bv, InputStream data, int dataLength) {
            lastBase64Variant = bv;
            lastBinaryStream = data;
            lastBinaryStreamLength = dataLength;
            return 0;
        }

        @Override
        public void writeNumber(int v) {
            lastInt = v;
            writtenInts.add(v);
        }

        @Override
        public void writeNumber(long v) {
            lastLong = v;
            writtenLongs.add(v);
        }

        @Override
        public void writeNumber(BigInteger v) {
            lastBigInteger = v;
        }

        @Override
        public void writeNumber(double v) {
            lastDouble = v;
            writtenDoubles.add(v);
        }

        @Override
        public void writeNumber(float v) {
            lastFloat = v;
        }

        @Override
        public void writeNumber(BigDecimal v) {
            lastBigDecimal = v;
        }

        @Override
        public void writeNumber(String encodedValue) {
            lastNumberString = encodedValue;
        }

        @Override
        public void writeBoolean(boolean state) {
            lastBoolean = state;
        }

        @Override
        public void writeNull() {
            nullCalled = true;
        }

        @Override
        public void writeObject(Object pojo) {
            lastWrittenObject = pojo;
        }

        @Override
        public void writeTree(TreeNode rootNode) {
            lastTreeNode = rootNode;
        }

        @Override
        public JsonStreamContext getOutputContext() {
            if (outputContext == null) {
                outputContext = new JsonStreamContext() {
                    @Override
                    public JsonStreamContext getParent() { return null; }
                    @Override
                    public String getCurrentName() { return null; }
                    @Override
                    public Object getCurrentValue() { return null; }
                    @Override
                    public void setCurrentValue(Object v) { }
                    @Override
                    public boolean hasCurrentIndex() { return false; }
                    @Override
                    public int getCurrentIndex() { return -1; }
                    @Override
                    public int getEntryCount() { return 0; }
                };
            }
            return outputContext;
        }

        public void setOutputContext(JsonStreamContext ctxt) {
            this.outputContext = ctxt;
        }

        @Override
        public void flush() {
            flushCalled = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            closeCalled = true;
            closed = true;
        }

        // Expose protected methods for testing

        public void callReportError(String msg) throws JsonGenerationException {
            _reportError(msg);
        }

        public void callThrowInternal() {
            _throwInternal();
        }

        public void callReportUnsupportedOperation() {
            _reportUnsupportedOperation();
        }

        public void callVerifyOffsets(int arrayLength, int offset, int length) {
            _verifyOffsets(arrayLength, offset, length);
        }

        public void callWriteSimpleObject(Object value) throws IOException {
            _writeSimpleObject(value);
        }

        // Override to allow testing setSchema without exception
        @Override
        public void setSchema(FormatSchema schema) {
            this.schema = schema;
        }

        @Override
        public FormatSchema getSchema() {
            return schema;
        }

        @Override
        public boolean canUseSchema(FormatSchema schema) {
            return this.schema != null && this.schema.getSchemaType().equals(schema.getSchemaType());
        }

        // Override setRootValueSeparator to avoid exception
        @Override
        public JsonGenerator setRootValueSeparator(SerializableString sep) {
            // accept
            return this;
        }

        // Override writeEmbeddedObject to avoid exception
        @Override
        public void writeEmbeddedObject(Object object) {
            // accept
        }

        // Override writeObjectId, writeObjectRef, writeTypeId to avoid exception
        @Override
        public void writeObjectId(Object id) {
            // accept
        }

        @Override
        public void writeObjectRef(Object id) {
            // accept
        }

        @Override
        public void writeTypeId(Object id) {
            // accept
        }

        // Override setHighestNonEscapedChar and getHighestEscapedChar
        @Override
        public JsonGenerator setHighestNonEscapedChar(int charCode) {
            this.highestNonEscapedChar = charCode;
            return this;
        }

        @Override
        public int getHighestEscapedChar() {
            return highestNonEscapedChar;
        }

        // Override setCharacterEscapes and getCharacterEscapes
        @Override
        public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
            this.characterEscapes = esc;
            return this;
        }

        @Override
        public CharacterEscapes getCharacterEscapes() {
            return characterEscapes;
        }
    }
}
