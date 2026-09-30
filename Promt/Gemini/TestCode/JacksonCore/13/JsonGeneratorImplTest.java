package com.fasterxml.jackson.core.json;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class JsonGeneratorImplTest {

    private static class ConcreteJsonGeneratorImpl extends JsonGeneratorImpl {
        final List<String> calls = new ArrayList<String>();
        private boolean closed = false;

        public ConcreteJsonGeneratorImpl(IOContext ctxt, int features, ObjectCodec codec) {
            super(ctxt, features, codec);
        }

        public int[] getOutputEscapes() {
            return _outputEscapes;
        }

        public boolean isUnquotedNamesConfigured() {
            return _cfgUnqNames;
        }

        public SerializableString getRootValueSeparator() {
            return _rootValueSeparator;
        }

        public IOContext getIOContext() {
            return _ioContext;
        }

        public void callCheckStdFeatureChanges(int newFeatureFlags, int changedFeatures) {
            _checkStdFeatureChanges(newFeatureFlags, changedFeatures);
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            calls.add("writeFieldName:" + name);
        }

        @Override
        public void writeString(String text) throws IOException {
            calls.add("writeString:" + text);
        }

        @Override
        public void writeStartArray() throws IOException { }

        @Override
        public void writeEndArray() throws IOException { }

        @Override
        public void writeStartObject() throws IOException { }

        @Override
        public void writeEndObject() throws IOException { }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException { }

        @Override
        public void writeRaw(String text) throws IOException { }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException { }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException { }

        @Override
        public void writeRaw(char c) throws IOException { }

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException { }

        @Override
        public void writeNumber(int v) throws IOException { }

        @Override
        public void writeNumber(long v) throws IOException { }

        @Override
        public void writeNumber(BigInteger v) throws IOException { }

        @Override
        public void writeNumber(double v) throws IOException { }

        @Override
        public void writeNumber(float v) throws IOException { }

        @Override
        public void writeNumber(BigDecimal v) throws IOException { }

        @Override
        public void writeNumber(String encodedValue) throws IOException { }

        @Override
        public void writeBoolean(boolean state) throws IOException { }

        @Override
        public void writeNull() throws IOException { }

        @Override
        protected void _releaseBuffers() { }

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException { }

        @Override
        public void flush() throws IOException { }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    private static class CustomEscapes extends CharacterEscapes {
        private static final long serialVersionUID = 1L;
        private final int[] ascii = CharacterEscapes.sStandardAsciiEscapesForJSON();

        @Override
        public int[] getEscapeCodesForAscii() {
            int[] custom = new int[ascii.length];
            System.arraycopy(ascii, 0, custom, 0, ascii.length);
            custom['a'] = CharacterEscapes.ESCAPE_STANDARD;
            return custom;
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            return null;
        }
    }

    private ConcreteJsonGeneratorImpl createGenerator(int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "testSource", false);
        return new ConcreteJsonGeneratorImpl(ctxt, features, null);
    }

    @Test
    public void testConstructorDefaults() {
        int defaultFeatures = JsonGenerator.Feature.collectDefaults();
        ConcreteJsonGeneratorImpl gen = createGenerator(defaultFeatures);

        Assert.assertNotNull(gen.getIOContext());
        Assert.assertEquals(0, gen.getHighestEscapedChar());
        Assert.assertNull(gen.getCharacterEscapes());
        Assert.assertArrayEquals(JsonGeneratorImpl.sOutputEscapes, gen.getOutputEscapes());
        Assert.assertEquals(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, gen.getRootValueSeparator());
        // By default QUOTE_FIELD_NAMES is enabled, so _cfgUnqNames should be false
        Assert.assertFalse(gen.isUnquotedNamesConfigured());
    }

    @Test
    public void testConstructorWithEscapeNonAsciiAndUnquotedNames() {
        int features = 0;
        features |= JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        // QUOTE_FIELD_NAMES is disabled (not added to mask)
        ConcreteJsonGeneratorImpl gen = createGenerator(features);

        Assert.assertEquals(127, gen.getHighestEscapedChar());
        Assert.assertTrue(gen.isUnquotedNamesConfigured());
    }

    @Test
    public void testConstructorWithQuoteFieldNamesEnabled() {
        int features = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        ConcreteJsonGeneratorImpl gen = createGenerator(features);

        Assert.assertEquals(0, gen.getHighestEscapedChar());
        Assert.assertFalse(gen.isUnquotedNamesConfigured());
    }

    @Test
    public void testEnableQuoteFieldNames() {
        // Start without QUOTE_FIELD_NAMES
        ConcreteJsonGeneratorImpl gen = createGenerator(0);
        Assert.assertTrue(gen.isUnquotedNamesConfigured());

        // Enabling QUOTE_FIELD_NAMES should set _cfgUnqNames to false
        JsonGenerator returnedGen = gen.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        Assert.assertSame(gen, returnedGen);
        Assert.assertFalse(gen.isUnquotedNamesConfigured());

        // Enabling another feature should not alter _cfgUnqNames
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        Assert.assertFalse(gen.isUnquotedNamesConfigured());
    }

    @Test
    public void testCheckStdFeatureChanges() {
        int initialFeatures = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        ConcreteJsonGeneratorImpl gen = createGenerator(initialFeatures);
        Assert.assertFalse(gen.isUnquotedNamesConfigured());

        // Disable QUOTE_FIELD_NAMES via setFeatureMask / _checkStdFeatureChanges
        int newFeatures = 0;
        int changed = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        gen.callCheckStdFeatureChanges(newFeatures, changed);
        Assert.assertTrue(gen.isUnquotedNamesConfigured());

        // Enable QUOTE_FIELD_NAMES again
        newFeatures = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        gen.callCheckStdFeatureChanges(newFeatures, changed);
        Assert.assertFalse(gen.isUnquotedNamesConfigured());
    }

    @Test
    public void testSetHighestNonEscapedChar() {
        ConcreteJsonGeneratorImpl gen = createGenerator(0);

        gen.setHighestNonEscapedChar(127);
        Assert.assertEquals(127, gen.getHighestEscapedChar());

        gen.setHighestNonEscapedChar(65535);
        Assert.assertEquals(65535, gen.getHighestEscapedChar());

        gen.setHighestNonEscapedChar(0);
        Assert.assertEquals(0, gen.getHighestEscapedChar());

        // Negative numbers should be normalized to 0
        gen.setHighestNonEscapedChar(-1);
        Assert.assertEquals(0, gen.getHighestEscapedChar());

        gen.setHighestNonEscapedChar(-100);
        Assert.assertEquals(0, gen.getHighestEscapedChar());

        JsonGenerator returnedGen = gen.setHighestNonEscapedChar(255);
        Assert.assertSame(gen, returnedGen);
        Assert.assertEquals(255, gen.getHighestEscapedChar());
    }

    @Test
    public void testSetCharacterEscapes() {
        ConcreteJsonGeneratorImpl gen = createGenerator(0);

        CustomEscapes customEscapes = new CustomEscapes();
        JsonGenerator returnedGen = gen.setCharacterEscapes(customEscapes);
        Assert.assertSame(gen, returnedGen);
        Assert.assertSame(customEscapes, gen.getCharacterEscapes());
        Assert.assertArrayEquals(customEscapes.getEscapeCodesForAscii(), gen.getOutputEscapes());

        // Reverting to null should restore default output escapes
        gen.setCharacterEscapes(null);
        Assert.assertNull(gen.getCharacterEscapes());
        Assert.assertArrayEquals(JsonGeneratorImpl.sOutputEscapes, gen.getOutputEscapes());
    }

    @Test
    public void testSetRootValueSeparator() {
        ConcreteJsonGeneratorImpl gen = createGenerator(0);

        SerializedString customSep = new SerializedString("/");
        JsonGenerator returnedGen = gen.setRootValueSeparator(customSep);
        Assert.assertSame(gen, returnedGen);
        Assert.assertSame(customSep, gen.getRootValueSeparator());

        gen.setRootValueSeparator(null);
        Assert.assertNull(gen.getRootValueSeparator());
    }

    @Test
    public void testVersion() {
        ConcreteJsonGeneratorImpl gen = createGenerator(0);
        Version v = gen.version();
        Assert.assertNotNull(v);
    }

    @Test
    public void testWriteStringField() throws IOException {
        ConcreteJsonGeneratorImpl gen = createGenerator(0);
        gen.writeStringField("myKey", "myValue");

        Assert.assertEquals(2, gen.calls.size());
        Assert.assertEquals("writeFieldName:myKey", gen.calls.get(0));
        Assert.assertEquals("writeString:myValue", gen.calls.get(1));
    }

    @Test
    public void testWriteStringFieldWithNullValue() throws IOException {
        ConcreteJsonGeneratorImpl gen = createGenerator(0);
        gen.writeStringField("nullKey", null);

        Assert.assertEquals(2, gen.calls.size());
        Assert.assertEquals("writeFieldName:nullKey", gen.calls.get(0));
        Assert.assertEquals("writeString:null", gen.calls.get(1));
    }
}
