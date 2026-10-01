package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonGeneratorImplTest {

    // Concrete implementation for testing
    static class TestJsonGenerator extends JsonGeneratorImpl {
        public String lastFieldName;
        public String lastStringValue;

        public TestJsonGenerator(IOContext ctxt, int features, ObjectCodec codec) {
            super(ctxt, features, codec);
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            lastFieldName = name;
        }

        @Override
        public void writeString(String text) throws IOException {
            lastStringValue = text;
        }

        // Implement all abstract methods from GeneratorBase with empty bodies
        @Override protected void _writeString(String text) throws IOException { }
        @Override protected void _writeString2(int c) throws IOException { }
        @Override protected void _flushBuffer() throws IOException { }
        @Override protected void _releaseBuffers() { }
        @Override protected void _writeStartArray() throws IOException { }
        @Override protected void _writeEndArray() throws IOException { }
        @Override protected void _writeStartObject() throws IOException { }
        @Override protected void _writeEndObject() throws IOException { }
        @Override protected void _writeFieldName(String name) throws IOException { }
        @Override protected void _writeFieldName(SerializableString name) throws IOException { }
        @Override protected void _writeNull() throws IOException { }
        @Override protected void _writeNumber(int v) throws IOException { }
        @Override protected void _writeNumber(long v) throws IOException { }
        @Override protected void _writeNumber(BigInteger v) throws IOException { }
        @Override protected void _writeNumber(double v) throws IOException { }
        @Override protected void _writeNumber(float v) throws IOException { }
        @Override protected void _writeNumber(BigDecimal v) throws IOException { }
        @Override protected void _writeNumber(String encodedValue) throws IOException { }
        @Override protected void _writeBoolean(boolean state) throws IOException { }
        @Override protected void _writeRaw(String text) throws IOException { }
        @Override protected void _writeRaw(String text, int offset, int len) throws IOException { }
        @Override protected void _writeRaw(char[] text, int offset, int len) throws IOException { }
        @Override protected void _writeRaw(char c) throws IOException { }
        @Override protected void _writeRawValue(String text) throws IOException { }
        @Override protected void _writeRawValue(String text, int offset, int len) throws IOException { }
        @Override protected void _writeRawValue(char[] text, int offset, int len) throws IOException { }
        @Override protected void _writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException { }
    }

    // Minimal CharacterEscapes implementation for testing
    static class TestCharacterEscapes extends CharacterEscapes {
        private final int[] escapes;
        public TestCharacterEscapes(int[] escapes) {
            this.escapes = escapes;
        }
        @Override
        public int[] getEscapeCodesForAscii() {
            return escapes;
        }
        @Override
        public SerializableString getEscapeSequence(int ch) {
            return null;
        }
    }

    // Minimal SerializableString implementation
    static class TestSerializableString implements SerializableString {
        private final String value;
        public TestSerializableString(String value) {
            this.value = value;
        }
        @Override
        public String getValue() { return value; }
        @Override
        public int charLength() { return value.length(); }
        @Override
        public char[] asQuotedChars() { return value.toCharArray(); }
        @Override
        public byte[] asUnquotedUTF8() { return value.getBytes(java.nio.charset.StandardCharsets.UTF_8); }
        @Override
        public byte[] asQuotedUTF8() { return value.getBytes(java.nio.charset.StandardCharsets.UTF_8); }
    }

    private IOContext createIOContext() {
        return new IOContext(new BufferRecycler(), null, false);
    }

    @Test
    public void testConstructorDefaultFeatures() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        // ESCAPE_NON_ASCII not enabled -> _maximumNonEscapedChar should be 0
        assertEquals(0, gen._maximumNonEscapedChar);
        // QUOTE_FIELD_NAMES enabled by default -> _cfgUnqNames should be false
        assertFalse(gen._cfgUnqNames);
    }

    @Test
    public void testConstructorEscapeNonAsciiEnabled() {
        IOContext ctxt = createIOContext();
        int features = Feature.ESCAPE_NON_ASCII.getMask();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, features, null);
        assertEquals(127, gen._maximumNonEscapedChar);
    }

    @Test
    public void testConstructorQuoteFieldNamesDisabled() {
        IOContext ctxt = createIOContext();
        int features = Feature.QUOTE_FIELD_NAMES.getMask(); // enabled by default, so disable by not including? Actually getMask() returns bit, we need to disable. We'll pass 0 and then disable? The constructor checks enabledIn(features). So if we pass 0, QUOTE_FIELD_NAMES is enabled (default). To disable, we need to pass features with that bit cleared. We'll use ~Feature.QUOTE_FIELD_NAMES.getMask() & some base. Simpler: use JsonFactory.Feature? Actually, we can use Feature.QUOTE_FIELD_NAMES.enabledIn(features) returns true if bit is set. By default, the feature is enabled, meaning the bit is set in the default feature flags. But we can pass a features value that does not include that bit. We'll use 0 and then call disable? But the constructor only uses the passed features. So we need to pass a features value where QUOTE_FIELD_NAMES is not enabled. We can compute: int features = 0; // default features include QUOTE_FIELD_NAMES? Actually, the default feature flags are defined in JsonFactory or JsonGenerator.Feature. The default for QUOTE_FIELD_NAMES is true. So if we pass 0, it will still be enabled because the default is true? Wait, the constructor uses Feature.QUOTE_FIELD_NAMES.enabledIn(features). The enabledIn method checks if the bit is set in the given flags. If we pass 0, the bit is not set, so enabledIn returns false. So _cfgUnqNames = !false = true. So passing 0 will disable QUOTE_FIELD_NAMES? That seems contradictory. Let's check: In Jackson, features are typically enabled by default, meaning the bit is set in the default feature flags. But if we pass 0, we are explicitly providing a feature set with no bits set, so all features are disabled. So QUOTE_FIELD_NAMES will be disabled. So _cfgUnqNames will be true. So we can test with features=0 to get _cfgUnqNames=true. So we'll use that.
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        assertTrue(gen._cfgUnqNames);
    }

    @Test
    public void testEnableQuoteFieldNames() {
        IOContext ctxt = createIOContext();
        // start with QUOTE_FIELD_NAMES disabled (features=0)
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        assertTrue(gen._cfgUnqNames);
        gen.enable(Feature.QUOTE_FIELD_NAMES);
        assertFalse(gen._cfgUnqNames);
    }

    @Test
    public void testEnableOtherFeatureDoesNotAffectUnqNames() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        boolean before = gen._cfgUnqNames;
        gen.enable(Feature.ESCAPE_NON_ASCII);
        assertEquals(before, gen._cfgUnqNames);
    }

    @Test
    public void testCheckStdFeatureChanges() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        // newFeatureFlags with QUOTE_FIELD_NAMES enabled
        int newFeatures = Feature.QUOTE_FIELD_NAMES.getMask();
        gen._checkStdFeatureChanges(newFeatures, 0);
        assertFalse(gen._cfgUnqNames);
        // newFeatureFlags with QUOTE_FIELD_NAMES disabled
        gen._checkStdFeatureChanges(0, 0);
        assertTrue(gen._cfgUnqNames);
    }

    @Test
    public void testSetHighestNonEscapedCharPositive() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        JsonGenerator ret = gen.setHighestNonEscapedChar(200);
        assertSame(gen, ret);
        assertEquals(200, gen._maximumNonEscapedChar);
    }

    @Test
    public void testSetHighestNonEscapedCharZero() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        gen.setHighestNonEscapedChar(0);
        assertEquals(0, gen._maximumNonEscapedChar);
    }

    @Test
    public void testSetHighestNonEscapedCharNegative() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        gen.setHighestNonEscapedChar(-5);
        assertEquals(0, gen._maximumNonEscapedChar);
    }

    @Test
    public void testGetHighestEscapedChar() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        gen._maximumNonEscapedChar = 300;
        assertEquals(300, gen.getHighestEscapedChar());
    }

    @Test
    public void testSetCharacterEscapesNull() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        // set some custom first to ensure revert
        gen._outputEscapes = new int[]{1,2,3};
        gen._characterEscapes = new TestCharacterEscapes(new int[]{4,5,6});
        JsonGenerator ret = gen.setCharacterEscapes(null);
        assertSame(gen, ret);
        assertNull(gen._characterEscapes);
        assertArrayEquals(CharTypes.get7BitOutputEscapes(), gen._outputEscapes);
    }

    @Test
    public void testSetCharacterEscapesNonNull() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        int[] customEscapes = new int[]{10,20,30};
        CharacterEscapes esc = new TestCharacterEscapes(customEscapes);
        JsonGenerator ret = gen.setCharacterEscapes(esc);
        assertSame(gen, ret);
        assertSame(esc, gen._characterEscapes);
        assertArrayEquals(customEscapes, gen._outputEscapes);
    }

    @Test
    public void testGetCharacterEscapes() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        assertNull(gen.getCharacterEscapes());
        CharacterEscapes esc = new TestCharacterEscapes(new int[]{1});
        gen.setCharacterEscapes(esc);
        assertSame(esc, gen.getCharacterEscapes());
    }

    @Test
    public void testSetRootValueSeparator() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        SerializableString sep = new TestSerializableString("|");
        JsonGenerator ret = gen.setRootValueSeparator(sep);
        assertSame(gen, ret);
        assertSame(sep, gen._rootValueSeparator);
    }

    @Test
    public void testSetRootValueSeparatorNull() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        gen.setRootValueSeparator(null);
        assertNull(gen._rootValueSeparator);
    }

    @Test
    public void testVersion() {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        Version v = gen.version();
        assertNotNull(v);
        // It should be the version of TestJsonGenerator class, but we can't assert exact value.
        assertTrue(v.getMajorVersion() >= 0);
    }

    @Test
    public void testWriteStringField() throws IOException {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        gen.writeStringField("name", "value");
        assertEquals("name", gen.lastFieldName);
        assertEquals("value", gen.lastStringValue);
    }

    @Test
    public void testWriteStringFieldWithNulls() throws IOException {
        IOContext ctxt = createIOContext();
        TestJsonGenerator gen = new TestJsonGenerator(ctxt, 0, null);
        // The method does not check for null, so it will pass null to writeFieldName/writeString
        gen.writeStringField(null, null);
        assertNull(gen.lastFieldName);
        assertNull(gen.lastStringValue);
    }
}
