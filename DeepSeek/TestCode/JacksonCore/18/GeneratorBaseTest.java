package com.fasterxml.jackson.core.base;

import java.io.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.VersionUtil;

import org.junit.Assert;
import org.junit.Test;

public class GeneratorBaseTest {

    // Concrete minimal implementation for testing abstract class
    private static class TestGenerator extends GeneratorBase {
        public String lastWrittenFieldName;
        public String lastWrittenString;
        public String lastWrittenRaw;
        public int lastWrittenRawOffset;
        public int lastWrittenRawLen;
        public char[] lastWrittenRawChars;
        public int lastWrittenRawCharsOffset;
        public int lastWrittenRawCharsLen;
        public SerializableString lastWrittenSerializableString;
        public boolean flushCalled;
        public boolean closeCalled;
        public boolean releaseBuffersCalled;
        public String verifyValueWriteType;
        public boolean verifyValueWriteCalled;
        public int highestNonEscapedChar = 0;
        public boolean reportErrorCalled;
        public String reportErrorMessage;
        public boolean reportUnsupportedOperationCalled;
        public boolean writeSimpleObjectCalled;
        public Object writeSimpleObjectArg;
        public PrettyPrinter prettyPrinter;
        public boolean defaultPrettyPrinterConstructed;

        public TestGenerator() {
            this(0, null);
        }

        public TestGenerator(int features, ObjectCodec codec) {
            super(features, codec);
        }

        public TestGenerator(int features, ObjectCodec codec, JsonWriteContext ctxt) {
            super(features, codec, ctxt);
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            lastWrittenFieldName = name;
        }

        @Override
        public void writeString(String text) throws IOException {
            lastWrittenString = text;
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            lastWrittenString = new String(text, offset, len);
        }

        @Override
        public void writeRaw(String text) throws IOException {
            lastWrittenRaw = text;
        }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            lastWrittenRaw = text;
            lastWrittenRawOffset = offset;
            lastWrittenRawLen = len;
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            lastWrittenRawChars = text;
            lastWrittenRawCharsOffset = offset;
            lastWrittenRawCharsLen = len;
        }

        @Override
        public void writeRaw(SerializableString text) throws IOException {
            lastWrittenSerializableString = text;
        }

        @Override
        public void writeNumber(int i) throws IOException { }
        @Override
        public void writeNumber(long l) throws IOException { }
        @Override
        public void writeNumber(double d) throws IOException { }
        @Override
        public void writeNumber(float f) throws IOException { }
        @Override
        public void writeNumber(BigDecimal dec) throws IOException { }
        @Override
        public void writeBoolean(boolean state) throws IOException { }
        @Override
        public void writeNull() throws IOException { }

        @Override
        public void flush() throws IOException {
            flushCalled = true;
        }

        @Override
        public void close() throws IOException {
            super.close();
            closeCalled = true;
        }

        @Override
        protected void _releaseBuffers() {
            releaseBuffersCalled = true;
        }

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {
            verifyValueWriteType = typeMsg;
            verifyValueWriteCalled = true;
        }

        @Override
        public void setHighestNonEscapedChar(int c) {
            highestNonEscapedChar = c;
        }

        @Override
        protected void _reportError(String msg) throws IOException {
            reportErrorCalled = true;
            reportErrorMessage = msg;
            throw new IOException(msg);
        }

        @Override
        protected void _reportUnsupportedOperation() {
            reportUnsupportedOperationCalled = true;
        }

        @Override
        public PrettyPrinter getPrettyPrinter() {
            return prettyPrinter;
        }

        @Override
        public JsonGenerator setPrettyPrinter(PrettyPrinter pp) {
            prettyPrinter = pp;
            return this;
        }

        @Override
        protected PrettyPrinter _constructDefaultPrettyPrinter() {
            defaultPrettyPrinterConstructed = true;
            return new DefaultPrettyPrinter();
        }

        @Override
        protected void _writeSimpleObject(Object value) throws IOException {
            writeSimpleObjectCalled = true;
            writeSimpleObjectArg = value;
        }

        @Override
        public int writeBinary(Base64Variant b64variant, InputStream data, int dataLength) throws IOException {
            return super.writeBinary(b64variant, data, dataLength);
        }
    }

    @Test
    public void testConstructorWithFeaturesAndCodec() {
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {}
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        TestGenerator gen = new TestGenerator(Feature.STRICT_DUPLICATE_DETECTION.getMask(), codec);
        Assert.assertTrue(gen.isEnabled(Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertSame(codec, gen.getCodec());
        Assert.assertNotNull(gen.getOutputContext());
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testConstructorWithFeaturesCodecAndContext() {
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {}
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        JsonWriteContext ctxt = JsonWriteContext.createRootContext(null);
        TestGenerator gen = new TestGenerator(Feature.WRITE_NUMBERS_AS_STRINGS.getMask(), codec, ctxt);
        Assert.assertTrue(gen.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertSame(codec, gen.getCodec());
        Assert.assertSame(ctxt, gen.getOutputContext());
    }

    @Test
    public void testVersion() {
        TestGenerator gen = new TestGenerator();
        Version v = gen.version();
        Assert.assertNotNull(v);
    }

    @Test
    public void testGetCurrentValue() {
        TestGenerator gen = new TestGenerator();
        Assert.assertNull(gen.getCurrentValue());
        gen.getOutputContext().setCurrentValue("test");
        Assert.assertEquals("test", gen.getCurrentValue());
    }

    @Test
    public void testSetCurrentValue() {
        TestGenerator gen = new TestGenerator();
        gen.setCurrentValue(42);
        Assert.assertEquals(42, gen.getCurrentValue());
    }

    @Test
    public void testIsEnabled() {
        TestGenerator gen = new TestGenerator(Feature.QUOTE_FIELD_NAMES.getMask(), null);
        Assert.assertTrue(gen.isEnabled(Feature.QUOTE_FIELD_NAMES));
        Assert.assertFalse(gen.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    @Test
    public void testGetFeatureMask() {
        int mask = Feature.QUOTE_FIELD_NAMES.getMask() | Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        TestGenerator gen = new TestGenerator(mask, null);
        Assert.assertEquals(mask, gen.getFeatureMask());
    }

    @Test
    public void testEnableWriteNumbersAsStrings() {
        TestGenerator gen = new TestGenerator();
        Assert.assertFalse(gen._cfgNumbersAsStrings);
        gen.enable(Feature.WRITE_NUMBERS_AS_STRINGS);
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        Assert.assertTrue(gen.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    @Test
    public void testEnableEscapeNonAscii() {
        TestGenerator gen = new TestGenerator();
        gen.enable(Feature.ESCAPE_NON_ASCII);
        Assert.assertEquals(127, gen.highestNonEscapedChar);
    }

    @Test
    public void testEnableStrictDuplicateDetection() {
        TestGenerator gen = new TestGenerator();
        Assert.assertNull(gen.getOutputContext().getDupDetector());
        gen.enable(Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testEnableStrictDuplicateDetectionAlreadyEnabled() {
        TestGenerator gen = new TestGenerator(Feature.STRICT_DUPLICATE_DETECTION.getMask(), null);
        DupDetector existing = gen.getOutputContext().getDupDetector();
        Assert.assertNotNull(existing);
        gen.enable(Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertSame(existing, gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testDisableWriteNumbersAsStrings() {
        TestGenerator gen = new TestGenerator(Feature.WRITE_NUMBERS_AS_STRINGS.getMask(), null);
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        gen.disable(Feature.WRITE_NUMBERS_AS_STRINGS);
        Assert.assertFalse(gen._cfgNumbersAsStrings);
        Assert.assertFalse(gen.isEnabled(Feature.WRITE_NUMBERS_AS_STRINGS));
    }

    @Test
    public void testDisableEscapeNonAscii() {
        TestGenerator gen = new TestGenerator(Feature.ESCAPE_NON_ASCII.getMask(), null);
        gen.disable(Feature.ESCAPE_NON_ASCII);
        Assert.assertEquals(0, gen.highestNonEscapedChar);
    }

    @Test
    public void testDisableStrictDuplicateDetection() {
        TestGenerator gen = new TestGenerator(Feature.STRICT_DUPLICATE_DETECTION.getMask(), null);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
        gen.disable(Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testSetFeatureMask() {
        TestGenerator gen = new TestGenerator();
        int newMask = Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask();
        gen.setFeatureMask(newMask);
        Assert.assertEquals(newMask, gen.getFeatureMask());
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        Assert.assertEquals(127, gen.highestNonEscapedChar);
    }

    @Test
    public void testSetFeatureMaskNoChange() {
        TestGenerator gen = new TestGenerator(Feature.QUOTE_FIELD_NAMES.getMask(), null);
        gen.setFeatureMask(Feature.QUOTE_FIELD_NAMES.getMask());
        Assert.assertTrue(gen.isEnabled(Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testOverrideStdFeatures() {
        TestGenerator gen = new TestGenerator();
        int values = Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask();
        int mask = Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask();
        gen.overrideStdFeatures(values, mask);
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        Assert.assertEquals(127, gen.highestNonEscapedChar);
    }

    @Test
    public void testOverrideStdFeaturesDisable() {
        TestGenerator gen = new TestGenerator(Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask(), null);
        int values = 0;
        int mask = Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask();
        gen.overrideStdFeatures(values, mask);
        Assert.assertFalse(gen._cfgNumbersAsStrings);
        Assert.assertEquals(0, gen.highestNonEscapedChar);
    }

    @Test
    public void testOverrideStdFeaturesNoChange() {
        TestGenerator gen = new TestGenerator(Feature.QUOTE_FIELD_NAMES.getMask(), null);
        gen.overrideStdFeatures(Feature.QUOTE_FIELD_NAMES.getMask(), Feature.QUOTE_FIELD_NAMES.getMask());
        Assert.assertTrue(gen.isEnabled(Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testCheckStdFeatureChangesEnableEscapeNonAscii() {
        TestGenerator gen = new TestGenerator();
        gen._checkStdFeatureChanges(Feature.ESCAPE_NON_ASCII.getMask(), Feature.ESCAPE_NON_ASCII.getMask());
        Assert.assertEquals(127, gen.highestNonEscapedChar);
    }

    @Test
    public void testCheckStdFeatureChangesDisableEscapeNonAscii() {
        TestGenerator gen = new TestGenerator(Feature.ESCAPE_NON_ASCII.getMask(), null);
        gen._checkStdFeatureChanges(0, Feature.ESCAPE_NON_ASCII.getMask());
        Assert.assertEquals(0, gen.highestNonEscapedChar);
    }

    @Test
    public void testCheckStdFeatureChangesEnableStrictDuplicateDetection() {
        TestGenerator gen = new TestGenerator();
        Assert.assertNull(gen.getOutputContext().getDupDetector());
        gen._checkStdFeatureChanges(Feature.STRICT_DUPLICATE_DETECTION.getMask(), Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testCheckStdFeatureChangesDisableStrictDuplicateDetection() {
        TestGenerator gen = new TestGenerator(Feature.STRICT_DUPLICATE_DETECTION.getMask(), null);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
        gen._checkStdFeatureChanges(0, Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testCheckStdFeatureChangesNoDerivedFeatures() {
        TestGenerator gen = new TestGenerator();
        gen._checkStdFeatureChanges(Feature.QUOTE_FIELD_NAMES.getMask(), Feature.QUOTE_FIELD_NAMES.getMask());
        Assert.assertFalse(gen._cfgNumbersAsStrings);
    }

    @Test
    public void testUseDefaultPrettyPrinterWhenNull() {
        TestGenerator gen = new TestGenerator();
        Assert.assertNull(gen.prettyPrinter);
        gen.useDefaultPrettyPrinter();
        Assert.assertTrue(gen.defaultPrettyPrinterConstructed);
        Assert.assertNotNull(gen.prettyPrinter);
    }

    @Test
    public void testUseDefaultPrettyPrinterWhenAlreadySet() {
        TestGenerator gen = new TestGenerator();
        PrettyPrinter existing = new DefaultPrettyPrinter();
        gen.setPrettyPrinter(existing);
        gen.useDefaultPrettyPrinter();
        Assert.assertSame(existing, gen.prettyPrinter);
        Assert.assertFalse(gen.defaultPrettyPrinterConstructed);
    }

    @Test
    public void testSetCodec() {
        TestGenerator gen = new TestGenerator();
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {}
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        gen.setCodec(codec);
        Assert.assertSame(codec, gen.getCodec());
    }

    @Test
    public void testGetOutputContext() {
        TestGenerator gen = new TestGenerator();
        Assert.assertNotNull(gen.getOutputContext());
    }

    @Test
    public void testWriteFieldNameSerializableString() throws IOException {
        TestGenerator gen = new TestGenerator();
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "field"; }
            @Override
            public int charLength() { return 5; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeFieldName(ss);
        Assert.assertEquals("field", gen.lastWrittenFieldName);
    }

    @Test
    public void testWriteStringSerializableString() throws IOException {
        TestGenerator gen = new TestGenerator();
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "value"; }
            @Override
            public int charLength() { return 5; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeString(ss);
        Assert.assertEquals("value", gen.lastWrittenString);
    }

    @Test
    public void testWriteRawValueString() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeRawValue("raw");
        Assert.assertTrue(gen.verifyValueWriteCalled);
        Assert.assertEquals("write raw value", gen.verifyValueWriteType);
        Assert.assertEquals("raw", gen.lastWrittenRaw);
    }

    @Test
    public void testWriteRawValueStringWithOffset() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeRawValue("rawtext", 3, 3);
        Assert.assertTrue(gen.verifyValueWriteCalled);
        Assert.assertEquals("write raw value", gen.verifyValueWriteType);
        Assert.assertEquals("rawtext", gen.lastWrittenRaw);
        Assert.assertEquals(3, gen.lastWrittenRawOffset);
        Assert.assertEquals(3, gen.lastWrittenRawLen);
    }

    @Test
    public void testWriteRawValueCharArray() throws IOException {
        TestGenerator gen = new TestGenerator();
        char[] text = {'r', 'a', 'w'};
        gen.writeRawValue(text, 0, 3);
        Assert.assertTrue(gen.verifyValueWriteCalled);
        Assert.assertEquals("write raw value", gen.verifyValueWriteType);
        Assert.assertArrayEquals(text, gen.lastWrittenRawChars);
        Assert.assertEquals(0, gen.lastWrittenRawCharsOffset);
        Assert.assertEquals(3, gen.lastWrittenRawCharsLen);
    }

    @Test
    public void testWriteRawValueSerializableString() throws IOException {
        TestGenerator gen = new TestGenerator();
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return "raw"; }
            @Override
            public int charLength() { return 3; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeRawValue(ss);
        Assert.assertTrue(gen.verifyValueWriteCalled);
        Assert.assertEquals("write raw value", gen.verifyValueWriteType);
        Assert.assertSame(ss, gen.lastWrittenSerializableString);
    }

    @Test
    public void testWriteBinary() throws IOException {
        TestGenerator gen = new TestGenerator();
        try {
            gen.writeBinary(Base64Variants.getDefaultVariant(), new ByteArrayInputStream(new byte[0]), 0);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        Assert.assertTrue(gen.reportUnsupportedOperationCalled);
    }

    @Test
    public void testWriteObjectNull() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeObject(null);
        // writeNull is called, but our stub does nothing, so we just verify no exception
    }

    @Test
    public void testWriteObjectWithCodec() throws IOException {
        final boolean[] codecCalled = {false};
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {
                codecCalled[0] = true;
            }
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        TestGenerator gen = new TestGenerator(0, codec);
        gen.writeObject("test");
        Assert.assertTrue(codecCalled[0]);
    }

    @Test
    public void testWriteObjectWithoutCodec() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeObject("test");
        Assert.assertTrue(gen.writeSimpleObjectCalled);
        Assert.assertEquals("test", gen.writeSimpleObjectArg);
    }

    @Test
    public void testWriteTreeNull() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeTree(null);
        // writeNull is called, no exception
    }

    @Test
    public void testWriteTreeWithCodec() throws IOException {
        final boolean[] codecCalled = {false};
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {
                codecCalled[0] = true;
            }
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        TestGenerator gen = new TestGenerator(0, codec);
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
        gen.writeTree(node);
        Assert.assertTrue(codecCalled[0]);
    }

    @Test
    public void testWriteTreeWithoutCodec() throws IOException {
        TestGenerator gen = new TestGenerator();
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
        try {
            gen.writeTree(node);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("No ObjectCodec defined", e.getMessage());
        }
    }

    @Test
    public void testFlush() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.flush();
        Assert.assertTrue(gen.flushCalled);
    }

    @Test
    public void testClose() throws IOException {
        TestGenerator gen = new TestGenerator();
        Assert.assertFalse(gen.isClosed());
        gen.close();
        Assert.assertTrue(gen.isClosed());
        Assert.assertTrue(gen.closeCalled);
    }

    @Test
    public void testIsClosed() {
        TestGenerator gen = new TestGenerator();
        Assert.assertFalse(gen.isClosed());
    }

    @Test
    public void testAsString() throws IOException {
        TestGenerator gen = new TestGenerator();
        BigDecimal bd = new BigDecimal("123.45");
        String result = gen._asString(bd);
        Assert.assertEquals("123.45", result);
    }

    @Test
    public void testDecodeSurrogateValid() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result = gen._decodeSurrogate(0xD800, 0xDC00);
        Assert.assertEquals(0x10000, result);
    }

    @Test
    public void testDecodeSurrogateInvalidSecondLow() throws IOException {
        TestGenerator gen = new TestGenerator();
        try {
            gen._decodeSurrogate(0xD800, 0xDBFF);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Incomplete surrogate pair"));
        }
    }

    @Test
    public void testDecodeSurrogateInvalidSecondHigh() throws IOException {
        TestGenerator gen = new TestGenerator();
        try {
            gen._decodeSurrogate(0xD800, 0xE000);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Incomplete surrogate pair"));
        }
    }

    @Test
    public void testDecodeSurrogateBoundaryValues() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result1 = gen._decodeSurrogate(0xD800, 0xDFFF);
        Assert.assertEquals(0x103FF, result1);
        int result2 = gen._decodeSurrogate(0xDBFF, 0xDC00);
        Assert.assertEquals(0x10FC00, result2);
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(0xD800, GeneratorBase.SURR1_FIRST);
        Assert.assertEquals(0xDBFF, GeneratorBase.SURR1_LAST);
        Assert.assertEquals(0xDC00, GeneratorBase.SURR2_FIRST);
        Assert.assertEquals(0xDFFF, GeneratorBase.SURR2_LAST);
    }

    @Test
    public void testDerivedFeaturesMask() {
        int expectedMask = Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | Feature.ESCAPE_NON_ASCII.getMask()
                | Feature.STRICT_DUPLICATE_DETECTION.getMask();
        Assert.assertEquals(expectedMask, GeneratorBase.DERIVED_FEATURES_MASK);
    }

    @Test
    public void testEnableNonDerivedFeature() {
        TestGenerator gen = new TestGenerator();
        gen.enable(Feature.QUOTE_FIELD_NAMES);
        Assert.assertTrue(gen.isEnabled(Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testDisableNonDerivedFeature() {
        TestGenerator gen = new TestGenerator(Feature.QUOTE_FIELD_NAMES.getMask(), null);
        gen.disable(Feature.QUOTE_FIELD_NAMES);
        Assert.assertFalse(gen.isEnabled(Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testSetFeatureMaskWithStrictDuplicateDetectionEnable() {
        TestGenerator gen = new TestGenerator();
        gen.setFeatureMask(Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testSetFeatureMaskWithStrictDuplicateDetectionDisable() {
        TestGenerator gen = new TestGenerator(Feature.STRICT_DUPLICATE_DETECTION.getMask(), null);
        gen.setFeatureMask(0);
        Assert.assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testOverrideStdFeaturesWithStrictDuplicateDetectionEnable() {
        TestGenerator gen = new TestGenerator();
        gen.overrideStdFeatures(Feature.STRICT_DUPLICATE_DETECTION.getMask(), Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testOverrideStdFeaturesWithStrictDuplicateDetectionDisable() {
        TestGenerator gen = new TestGenerator(Feature.STRICT_DUPLICATE_DETECTION.getMask(), null);
        gen.overrideStdFeatures(0, Feature.STRICT_DUPLICATE_DETECTION.getMask());
        Assert.assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testCheckStdFeatureChangesEnableWriteNumbersAsStrings() {
        TestGenerator gen = new TestGenerator();
        gen._checkStdFeatureChanges(Feature.WRITE_NUMBERS_AS_STRINGS.getMask(), Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        Assert.assertTrue(gen._cfgNumbersAsStrings);
    }

    @Test
    public void testCheckStdFeatureChangesDisableWriteNumbersAsStrings() {
        TestGenerator gen = new TestGenerator(Feature.WRITE_NUMBERS_AS_STRINGS.getMask(), null);
        gen._checkStdFeatureChanges(0, Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        Assert.assertFalse(gen._cfgNumbersAsStrings);
    }

    @Test
    public void testCheckStdFeatureChangesEscapeNonAsciiNotChanged() {
        TestGenerator gen = new TestGenerator();
        gen._checkStdFeatureChanges(Feature.WRITE_NUMBERS_AS_STRINGS.getMask(), Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        Assert.assertEquals(0, gen.highestNonEscapedChar);
    }

    @Test
    public void testCheckStdFeatureChangesStrictDuplicateDetectionNotChanged() {
        TestGenerator gen = new TestGenerator();
        gen._checkStdFeatureChanges(Feature.WRITE_NUMBERS_AS_STRINGS.getMask(), Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        Assert.assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testWriteObjectWithCodecNullValue() throws IOException {
        final boolean[] codecCalled = {false};
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {
                codecCalled[0] = true;
            }
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        TestGenerator gen = new TestGenerator(0, codec);
        gen.writeObject(null);
        Assert.assertFalse(codecCalled[0]);
    }

    @Test
    public void testWriteTreeNullWithCodec() throws IOException {
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            @Override
            public <T> T treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T createArrayNode() { return null; }
            @Override
            public <T extends TreeNode> T createObjectNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override
            public void writeValue(JsonGenerator gen, Object value) {}
            @Override
            public void writeTree(JsonGenerator gen, TreeNode tree) {}
            @Override
            public TreeNode createObjectNode() { return null; }
            @Override
            public TreeNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
        };
        TestGenerator gen = new TestGenerator(0, codec);
        gen.writeTree(null);
        // No exception expected
    }

    @Test
    public void testAsStringLargeScale() throws IOException {
        TestGenerator gen = new TestGenerator();
        BigDecimal bd = new BigDecimal("1E+100");
        String result = gen._asString(bd);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDecodeSurrogateFirstBoundaryLow() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result = gen._decodeSurrogate(0xD800, 0xDC00);
        Assert.assertEquals(0x10000, result);
    }

    @Test
    public void testDecodeSurrogateFirstBoundaryHigh() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result = gen._decodeSurrogate(0xDBFF, 0xDFFF);
        Assert.assertEquals(0x10FFFF, result);
    }

    @Test
    public void testDecodeSurrogateSecondBoundaryLow() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result = gen._decodeSurrogate(0xD800, 0xDC00);
        Assert.assertEquals(0x10000, result);
    }

    @Test
    public void testDecodeSurrogateSecondBoundaryHigh() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result = gen._decodeSurrogate(0xD800, 0xDFFF);
        Assert.assertEquals(0x103FF, result);
    }

    @Test
    public void testDecodeSurrogateInvalidSecondJustBelow() throws IOException {
        TestGenerator gen = new TestGenerator();
        try {
            gen._decodeSurrogate(0xD800, 0xDBFF);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Incomplete surrogate pair"));
        }
    }

    @Test
    public void testDecodeSurrogateInvalidSecondJustAbove() throws IOException {
        TestGenerator gen = new TestGenerator();
        try {
            gen._decodeSurrogate(0xD800, 0xE000);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Incomplete surrogate pair"));
        }
    }

    @Test
    public void testEnableMultipleDerivedFeatures() {
        TestGenerator gen = new TestGenerator();
        gen.enable(Feature.WRITE_NUMBERS_AS_STRINGS);
        gen.enable(Feature.ESCAPE_NON_ASCII);
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        Assert.assertEquals(127, gen.highestNonEscapedChar);
    }

    @Test
    public void testDisableMultipleDerivedFeatures() {
        TestGenerator gen = new TestGenerator(
                Feature.WRITE_NUMBERS_AS_STRINGS.getMask() | Feature.ESCAPE_NON_ASCII.getMask(),
                null);
        gen.disable(Feature.WRITE_NUMBERS_AS_STRINGS);
        gen.disable(Feature.ESCAPE_NON_ASCII);
        Assert.assertFalse(gen._cfgNumbersAsStrings);
        Assert.assertEquals(0, gen.highestNonEscapedChar);
    }

    @Test
    public void testSetFeatureMaskComplex() {
        TestGenerator gen = new TestGenerator();
        int mask = Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | Feature.ESCAPE_NON_ASCII.getMask()
                | Feature.STRICT_DUPLICATE_DETECTION.getMask();
        gen.setFeatureMask(mask);
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        Assert.assertEquals(127, gen.highestNonEscapedChar);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testOverrideStdFeaturesComplex() {
        TestGenerator gen = new TestGenerator();
        int values = Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | Feature.ESCAPE_NON_ASCII.getMask()
                | Feature.STRICT_DUPLICATE_DETECTION.getMask();
        int mask = values;
        gen.overrideStdFeatures(values, mask);
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        Assert.assertEquals(127, gen.highestNonEscapedChar);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testWriteRawValueStringEmpty() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeRawValue("");
        Assert.assertTrue(gen.verifyValueWriteCalled);
        Assert.assertEquals("", gen.lastWrittenRaw);
    }

    @Test
    public void testWriteRawValueStringWithOffsetZeroLen() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeRawValue("raw", 0, 0);
        Assert.assertTrue(gen.verifyValueWriteCalled);
        Assert.assertEquals("raw", gen.lastWrittenRaw);
        Assert.assertEquals(0, gen.lastWrittenRawOffset);
        Assert.assertEquals(0, gen.lastWrittenRawLen);
    }

    @Test
    public void testWriteRawValueCharArrayEmpty() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.writeRawValue(new char[0], 0, 0);
        Assert.assertTrue(gen.verifyValueWriteCalled);
        Assert.assertEquals(0, gen.lastWrittenRawChars.length);
    }

    @Test
    public void testWriteObjectWithCodecAndNullCodec() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
        gen.writeObject("test");
        Assert.assertTrue(gen.writeSimpleObjectCalled);
    }

    @Test
    public void testWriteTreeWithCodecAndNullCodec() throws IOException {
        TestGenerator gen = new TestGenerator(0, null);
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
        try {
            gen.writeTree(node);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("No ObjectCodec defined", e.getMessage());
        }
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        TestGenerator gen = new TestGenerator();
        gen.close();
        Assert.assertTrue(gen.isClosed());
        gen.close();
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testAsStringZero() throws IOException {
        TestGenerator gen = new TestGenerator();
        String result = gen._asString(BigDecimal.ZERO);
        Assert.assertEquals("0", result);
    }

    @Test
    public void testAsStringNegative() throws IOException {
        TestGenerator gen = new TestGenerator();
        String result = gen._asString(new BigDecimal("-123.45"));
        Assert.assertEquals("-123.45", result);
    }

    @Test
    public void testDecodeSurrogateMaxValue() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result = gen._decodeSurrogate(0xDBFF, 0xDFFF);
        Assert.assertEquals(0x10FFFF, result);
    }

    @Test
    public void testDecodeSurrogateMinValue() throws IOException {
        TestGenerator gen = new TestGenerator();
        int result = gen._decodeSurrogate(0xD800, 0xDC00);
        Assert.assertEquals(0x10000, result);
    }

    @Test
    public void testEnableFeatureReturnsThis() {
        TestGenerator gen = new TestGenerator();
        Assert.assertSame(gen, gen.enable(Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testDisableFeatureReturnsThis() {
        TestGenerator gen = new TestGenerator();
        Assert.assertSame(gen, gen.disable(Feature.QUOTE_FIELD_NAMES));
    }

    @Test
    public void testSetFeatureMaskReturnsThis() {
        TestGenerator gen = new TestGenerator();
        Assert.assertSame(gen, gen.setFeatureMask(0));
    }

    @Test
    public void testOverrideStdFeaturesReturnsThis() {
        TestGenerator gen = new TestGenerator();
        Assert.assertSame(gen, gen.overrideStdFeatures(0, 0));
    }

    @Test
    public void testUseDefaultPrettyPrinterReturnsThis() {
        TestGenerator gen = new TestGenerator();
        Assert.assertSame(gen, gen.useDefaultPrettyPrinter());
    }

    @Test
    public void testSetCodecReturnsThis() {
        TestGenerator gen = new TestGenerator();
        Assert.assertSame(gen, gen.setCodec(null));
    }

    @Test
    public void testWriteFieldNameSerializableStringNullValue() throws IOException {
        TestGenerator gen = new TestGenerator();
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return null; }
            @Override
            public int charLength() { return 0; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeFieldName(ss);
        Assert.assertNull(gen.lastWrittenFieldName);
    }

    @Test
    public void testWriteStringSerializableStringNullValue() throws IOException {
        TestGenerator gen = new TestGenerator();
        SerializableString ss = new SerializableString() {
            @Override
            public String getValue() { return null; }
            @Override
            public int charLength() { return 0; }
            @Override
            public char[] asQuotedChars() { return new char[0]; }
            @Override
            public byte[] asUnquotedUTF8() { return new byte[0]; }
            @Override
            public byte[] asQuotedUTF8() { return new byte[0]; }
        };
        gen.writeString(ss);
        Assert.assertNull(gen.lastWrittenString);
    }

    @Test
    public void testCheckStdFeatureChangesAllDerivedFeatures() {
        TestGenerator gen = new TestGenerator();
        int newFeatures = Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | Feature.ESCAPE_NON_ASCII.getMask()
                | Feature.STRICT_DUPLICATE_DETECTION.getMask();
        int changed = newFeatures;
        gen._checkStdFeatureChanges(newFeatures, changed);
        Assert.assertTrue(gen._cfgNumbersAsStrings);
        Assert.assertEquals(127, gen.highestNonEscapedChar);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testCheckStdFeatureChangesDisableAllDerivedFeatures() {
        TestGenerator gen = new TestGenerator(
                Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | Feature.ESCAPE_NON_ASCII.getMask()
                | Feature.STRICT_DUPLICATE_DETECTION.getMask(),
                null);
        int changed = Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | Feature.ESCAPE_NON_ASCII.getMask()
                | Feature.STRICT_DUPLICATE_DETECTION.getMask();
        gen._checkStdFeatureChanges(0, changed);
        Assert.assertFalse(gen._cfgNumbersAsStrings);
        Assert.assertEquals(0, gen.highestNonEscapedChar);
        Assert.assertNull(gen.getOutputContext().getDupDetector());
    }
}
