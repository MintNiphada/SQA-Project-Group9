package com.fasterxml.jackson.core.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.json.DupDetector;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import org.junit.Assert;
import org.junit.Test;

public class GeneratorBaseTest {

    private static class DummyGenerator extends GeneratorBase {
        public int highestNonEscapedChar = -1;
        public String lastFieldName;
        public String lastStringValue;
        public String lastRawValue;
        public boolean writeNullCalled = false;
        public boolean writeBooleanCalled = false;
        public boolean booleanValue = false;
        public boolean flushed = false;
        public boolean buffersReleased = false;
        public String lastVerifyMsg = null;

        public DummyGenerator(int features, ObjectCodec codec) {
            super(features, codec);
        }

        public DummyGenerator(int features, ObjectCodec codec, JsonWriteContext ctxt) {
            super(features, codec, ctxt);
        }

        @Override
        public JsonGenerator setHighestNonEscapedChar(int maxChar) {
            highestNonEscapedChar = maxChar;
            return this;
        }

        @Override
        public int getHighestNonEscapedChar() {
            return highestNonEscapedChar;
        }

        @Override
        public void writeFieldName(String name) throws IOException {
            this.lastFieldName = name;
        }

        @Override
        public void writeString(String text) throws IOException {
            this.lastStringValue = text;
        }

        @Override
        public void writeString(char[] text, int offset, int len) throws IOException {
            this.lastStringValue = new String(text, offset, len);
        }

        @Override
        public void writeRaw(String text) throws IOException {
            this.lastRawValue = text;
        }

        @Override
        public void writeRaw(String text, int offset, int len) throws IOException {
            this.lastRawValue = text.substring(offset, offset + len);
        }

        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException {
            this.lastRawValue = new String(text, offset, len);
        }

        @Override
        public void writeRaw(char c) throws IOException {
            this.lastRawValue = String.valueOf(c);
        }

        @Override
        public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
        }

        @Override
        public void writeNumber(int v) throws IOException {}

        @Override
        public void writeNumber(long v) throws IOException {}

        @Override
        public void writeNumber(BigInteger v) throws IOException {}

        @Override
        public void writeNumber(double v) throws IOException {}

        @Override
        public void writeNumber(float v) throws IOException {}

        @Override
        public void writeNumber(BigDecimal v) throws IOException {}

        @Override
        public void writeNumber(String v) throws IOException {}

        @Override
        public void writeBoolean(boolean state) throws IOException {
            this.writeBooleanCalled = true;
            this.booleanValue = state;
        }

        @Override
        public void writeNull() throws IOException {
            this.writeNullCalled = true;
        }

        @Override
        public void writeStartArray() throws IOException {
            _writeContext = _writeContext.createChildArrayContext();
        }

        @Override
        public void writeEndArray() throws IOException {
            _writeContext = _writeContext.getParent();
        }

        @Override
        public void writeStartObject() throws IOException {
            _writeContext = _writeContext.createChildObjectContext();
        }

        @Override
        public void writeEndObject() throws IOException {
            _writeContext = _writeContext.getParent();
        }

        @Override
        public void flush() throws IOException {
            this.flushed = true;
        }

        @Override
        protected void _releaseBuffers() {
            this.buffersReleased = true;
        }

        @Override
        protected void _verifyValueWrite(String typeMsg) throws IOException {
            this.lastVerifyMsg = typeMsg;
        }

        // Expose protected methods for testing
        public int decodeSurrogate(int surr1, int surr2) throws IOException {
            return _decodeSurrogate(surr1, surr2);
        }

        public String asString(BigDecimal value) throws IOException {
            return _asString(value);
        }

        public PrettyPrinter constructDefaultPrettyPrinter() {
            return _constructDefaultPrettyPrinter();
        }

        public boolean getCfgNumbersAsStrings() {
            return _cfgNumbersAsStrings;
        }
    }

    private static class DummyCodec extends ObjectCodec {
        public Object lastWrittenValue;

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException {
            return null;
        }

        @Override
        public <T> T readValue(JsonParser p, TypeReference<?> valueTypeRef) throws IOException {
            return null;
        }

        @Override
        public <T> T readValue(JsonParser p, ResolvedType valueType) throws IOException {
            return null;
        }

        @Override
        public <T extends TreeNode> T readTree(JsonParser p) throws IOException {
            return null;
        }

        @Override
        public void writeValue(JsonGenerator gen, Object value) throws IOException {
            this.lastWrittenValue = value;
        }

        @Override
        public void writeTree(JsonGenerator gen, TreeNode tree) throws IOException {
            this.lastWrittenValue = tree;
        }

        @Override
        public <T> java.util.Iterator<T> readValues(JsonParser p, Class<T> valueType) throws IOException {
            return null;
        }

        @Override
        public <T> java.util.Iterator<T> readValues(JsonParser p, TypeReference<?> valueTypeRef) throws IOException {
            return null;
        }

        @Override
        public <T> java.util.Iterator<T> readValues(JsonParser p, ResolvedType valueType) throws IOException {
            return null;
        }

        @Override
        public TreeNode createObjectNode() {
            return null;
        }

        @Override
        public TreeNode createArrayNode() {
            return null;
        }

        @Override
        public JsonParser treeAsTokens(TreeNode n) {
            return null;
        }

        @Override
        public <T> T treeToValue(TreeNode n, Class<T> valueType) throws JsonProcessingException {
            return null;
        }
    }

    private static class DummyTreeNode implements TreeNode {
        @Override
        public JsonToken asToken() {
            return JsonToken.START_OBJECT;
        }

        @Override
        public JsonParser.NumberType numberType() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isValueNode() {
            return false;
        }

        @Override
        public boolean isContainerNode() {
            return true;
        }

        @Override
        public boolean isMissingNode() {
            return false;
        }

        @Override
        public boolean isArray() {
            return false;
        }

        @Override
        public boolean isObject() {
            return true;
        }

        @Override
        public TreeNode get(String fieldName) {
            return null;
        }

        @Override
        public TreeNode get(int index) {
            return null;
        }

        @Override
        public TreeNode path(String fieldName) {
            return null;
        }

        @Override
        public TreeNode path(int index) {
            return null;
        }

        @Override
        public java.util.Iterator<String> fieldNames() {
            return Collections.emptyIterator();
        }

        @Override
        public TreeNode at(JsonPointer ptr) {
            return null;
        }

        @Override
        public TreeNode at(String jsonPtrExpr) throws IllegalArgumentException {
            return null;
        }

        @Override
        public JsonParser traverse() {
            return null;
        }

        @Override
        public JsonParser traverse(ObjectCodec codec) {
            return null;
        }
    }

    @Test
    public void testConstructorsAndInitialState() {
        DummyCodec codec = new DummyCodec();
        DummyGenerator gen1 = new DummyGenerator(0, codec);
        Assert.assertSame(codec, gen1.getCodec());
        Assert.assertFalse(gen1.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertFalse(gen1.getCfgNumbersAsStrings());
        Assert.assertNotNull(gen1.getOutputContext());
        Assert.assertNull(gen1.getOutputContext().getDupDetector());

        int flags = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        DummyGenerator gen2 = new DummyGenerator(flags, null);
        Assert.assertNull(gen2.getCodec());
        Assert.assertTrue(gen2.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertTrue(gen2.getCfgNumbersAsStrings());
        Assert.assertNotNull(gen2.getOutputContext().getDupDetector());

        JsonWriteContext customCtxt = JsonWriteContext.createRootContext(null);
        DummyGenerator gen3 = new DummyGenerator(0, codec, customCtxt);
        Assert.assertSame(customCtxt, gen3.getOutputContext());
    }

    @Test
    public void testVersion() {
        DummyGenerator gen = new DummyGenerator(0, null);
        Version v = gen.version();
        Assert.assertNotNull(v);
        Assert.assertFalse(v.isUnknown());
    }

    @Test
    public void testCurrentValueDelegation() {
        DummyGenerator gen = new DummyGenerator(0, null);
        Assert.assertNull(gen.getCurrentValue());
        Object obj = new Object();
        gen.setCurrentValue(obj);
        Assert.assertSame(obj, gen.getCurrentValue());
    }

    @Test
    public void testFeatureEnableDisable() {
        DummyGenerator gen = new DummyGenerator(0, null);
        Assert.assertEquals(0, gen.getFeatureMask());

        // Enable derived: WRITE_NUMBERS_AS_STRINGS
        gen.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        Assert.assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertTrue(gen.getCfgNumbersAsStrings());

        // Disable derived: WRITE_NUMBERS_AS_STRINGS
        gen.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        Assert.assertFalse(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertFalse(gen.getCfgNumbersAsStrings());

        // Enable derived: ESCAPE_NON_ASCII
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        Assert.assertTrue(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        Assert.assertEquals(127, gen.highestNonEscapedChar);

        // Disable derived: ESCAPE_NON_ASCII
        gen.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        Assert.assertFalse(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        Assert.assertEquals(0, gen.highestNonEscapedChar);

        // Enable derived: STRICT_DUPLICATE_DETECTION
        gen.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertTrue(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());

        // Calling enable again when already enabled should not fail
        gen.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());

        // Disable derived: STRICT_DUPLICATE_DETECTION
        gen.disable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        Assert.assertFalse(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNull(gen.getOutputContext().getDupDetector());

        // Non-derived feature
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        Assert.assertTrue(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        gen.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        Assert.assertFalse(gen.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSetFeatureMask() {
        DummyGenerator gen = new DummyGenerator(0, null);

        // Change with derived features enabled
        int mask1 = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask()
                | JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        gen.setFeatureMask(mask1);
        Assert.assertEquals(mask1, gen.getFeatureMask());
        Assert.assertTrue(gen.getCfgNumbersAsStrings());
        Assert.assertEquals(127, gen.highestNonEscapedChar);
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());

        // Change with derived features disabled
        gen.setFeatureMask(0);
        Assert.assertEquals(0, gen.getFeatureMask());
        Assert.assertFalse(gen.getCfgNumbersAsStrings());
        Assert.assertEquals(0, gen.highestNonEscapedChar);
        Assert.assertNull(gen.getOutputContext().getDupDetector());

        // Change with only non-derived features
        int mask2 = JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask();
        gen.setFeatureMask(mask2);
        Assert.assertEquals(mask2, gen.getFeatureMask());

        // No change
        gen.setFeatureMask(mask2);
        Assert.assertEquals(mask2, gen.getFeatureMask());
    }

    @Test
    public void testOverrideStdFeatures() {
        DummyGenerator gen = new DummyGenerator(0, null);

        int mask = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask()
                | JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        int values = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();

        gen.overrideStdFeatures(values, mask);
        Assert.assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertFalse(gen.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        Assert.assertTrue(gen.getCfgNumbersAsStrings());

        // Overriding with identical values should be a no-op
        gen.overrideStdFeatures(values, mask);
        Assert.assertTrue(gen.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));

        // Test duplicate detection enabling and disabling via override
        int dupMask = JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        gen.overrideStdFeatures(dupMask, dupMask);
        Assert.assertTrue(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNotNull(gen.getOutputContext().getDupDetector());

        gen.overrideStdFeatures(dupMask, dupMask); // re-enable when already non-null

        gen.overrideStdFeatures(0, dupMask);
        Assert.assertFalse(gen.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));
        Assert.assertNull(gen.getOutputContext().getDupDetector());
    }

    @Test
    public void testPrettyPrinterHandling() {
        DummyGenerator gen = new DummyGenerator(0, null);
        Assert.assertNull(gen.getPrettyPrinter());

        gen.useDefaultPrettyPrinter();
        PrettyPrinter pp = gen.getPrettyPrinter();
        Assert.assertNotNull(pp);
        Assert.assertTrue(pp instanceof DefaultPrettyPrinter);

        // Calling again should not replace existing pretty printer
        gen.useDefaultPrettyPrinter();
        Assert.assertSame(pp, gen.getPrettyPrinter());

        PrettyPrinter defaultPp = gen.constructDefaultPrettyPrinter();
        Assert.assertNotNull(defaultPp);
        Assert.assertTrue(defaultPp instanceof DefaultPrettyPrinter);
    }

    @Test
    public void testCodecHandling() {
        DummyGenerator gen = new DummyGenerator(0, null);
        Assert.assertNull(gen.getCodec());

        DummyCodec codec = new DummyCodec();
        gen.setCodec(codec);
        Assert.assertSame(codec, gen.getCodec());
    }

    @Test
    public void testWriteFieldNameAndStringWithSerializableString() throws IOException {
        DummyGenerator gen = new DummyGenerator(0, null);
        SerializedString ss = new SerializedString("myField");

        gen.writeFieldName(ss);
        Assert.assertEquals("myField", gen.lastFieldName);

        SerializedString ssValue = new SerializedString("myValue");
        gen.writeString(ssValue);
        Assert.assertEquals("myValue", gen.lastStringValue);
    }

    @Test
    public void testWriteRawValueVariants() throws IOException {
        DummyGenerator gen = new DummyGenerator(0, null);

        gen.writeRawValue("rawValue1");
        Assert.assertEquals("write raw value", gen.lastVerifyMsg);
        Assert.assertEquals("rawValue1", gen.lastRawValue);

        gen.writeRawValue("abcrawValue2def", 3, 9);
        Assert.assertEquals("rawValue2", gen.lastRawValue);

        char[] chars = "xyzrawValue3uvw".toCharArray();
        gen.writeRawValue(chars, 3, 9);
        Assert.assertEquals("rawValue3", gen.lastRawValue);

        SerializedString ssRaw = new SerializedString("rawValue4");
        gen.writeRawValue(ssRaw);
        Assert.assertEquals("rawValue4", gen.lastRawValue);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteBinaryWithInputStreamUnsupported() throws IOException {
        DummyGenerator gen = new DummyGenerator(0, null);
        byte[] data = new byte[]{1, 2, 3};
        gen.writeBinary(Base64Variants.MIME, new ByteArrayInputStream(data), data.length);
    }

    @Test
    public void testWriteObject() throws IOException {
        DummyGenerator genWithoutCodec = new DummyGenerator(0, null);

        // Null value writes null
        genWithoutCodec.writeObject(null);
        Assert.assertTrue(genWithoutCodec.writeNullCalled);

        // Non-null without codec falls back to simple object
        genWithoutCodec.writeObject(Boolean.TRUE);
        Assert.assertTrue(genWithoutCodec.writeBooleanCalled);
        Assert.assertTrue(genWithoutCodec.booleanValue);

        // Non-null with codec
        DummyCodec codec = new DummyCodec();
        DummyGenerator genWithCodec = new DummyGenerator(0, codec);
        Object target = "TargetPojo";
        genWithCodec.writeObject(target);
        Assert.assertSame(target, codec.lastWrittenValue);
    }

    @Test
    public void testWriteTree() throws IOException {
        DummyGenerator gen = new DummyGenerator(0, null);

        // Null tree node writes null
        gen.writeTree(null);
        Assert.assertTrue(gen.writeNullCalled);

        // Non-null tree node without codec throws IllegalStateException
        DummyTreeNode treeNode = new DummyTreeNode();
        try {
            gen.writeTree(treeNode);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("No ObjectCodec defined", e.getMessage());
        }

        // Non-null tree node with codec delegates to codec
        DummyCodec codec = new DummyCodec();
        DummyGenerator genWithCodec = new DummyGenerator(0, codec);
        genWithCodec.writeTree(treeNode);
        Assert.assertSame(treeNode, codec.lastWrittenValue);
    }

    @Test
    public void testCloseAndIsClosed() throws IOException {
        DummyGenerator gen = new DummyGenerator(0, null);
        Assert.assertFalse(gen.isClosed());

        gen.close();
        Assert.assertTrue(gen.isClosed());
    }

    @Test
    public void testBigDecimalAsString() throws IOException {
        DummyGenerator gen = new DummyGenerator(0, null);
        BigDecimal bd = new BigDecimal("12345.67890");
        Assert.assertEquals("12345.67890", gen.asString(bd));
    }

    @Test
    public void testDecodeSurrogateValid() throws IOException {
        DummyGenerator gen = new DummyGenerator(0, null);

        // Low surrogate boundary
        int codePoint1 = gen.decodeSurrogate(GeneratorBase.SURR1_FIRST, GeneratorBase.SURR2_FIRST);
        Assert.assertEquals(0x10000, codePoint1);

        // High surrogate boundary
        int codePoint2 = gen.decodeSurrogate(GeneratorBase.SURR1_LAST, GeneratorBase.SURR2_LAST);
        Assert.assertEquals(0x10FFFF, codePoint2);

        // Emoji surrogate pair (U+1F600: 0xD83D 0xDE00)
        int codePoint3 = gen.decodeSurrogate(0xD83D, 0xDE00);
        Assert.assertEquals(0x1F600, codePoint3);
    }

    @Test
    public void testDecodeSurrogateInvalidSecondChar() {
        DummyGenerator gen = new DummyGenerator(0, null);

        try {
            gen.decodeSurrogate(0xD800, 0xDBFF); // second is less than SURR2_FIRST (0xDC00)
            Assert.fail("Expected JsonGenerationException");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof JsonGenerationException);
            Assert.assertTrue(e.getMessage().contains("Incomplete surrogate pair"));
        }

        try {
            gen.decodeSurrogate(0xD800, 0xE000); // second is greater than SURR2_LAST (0xDFFF)
            Assert.fail("Expected JsonGenerationException");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof JsonGenerationException);
            Assert.assertTrue(e.getMessage().contains("Incomplete surrogate pair"));
        }
    }
}
