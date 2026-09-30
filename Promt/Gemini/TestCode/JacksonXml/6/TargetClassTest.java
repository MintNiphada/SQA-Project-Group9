package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.Stax2WriterAdapter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;

public class ToXmlGeneratorTest {

    private XMLOutputFactory _xmlOutputFactory;
    private BufferRecycler _bufferRecycler;

    @Before
    public void setUp() {
        _xmlOutputFactory = XMLOutputFactory.newInstance();
        _bufferRecycler = new BufferRecycler();
    }

    private ToXmlGenerator createGenerator(StringWriter sw, int stdFeatures, int xmlFeatures) throws Exception {
        IOContext ctxt = new IOContext(_bufferRecycler, sw, false);
        XMLStreamWriter xsw = _xmlOutputFactory.createXMLStreamWriter(sw);
        return new ToXmlGenerator(ctxt, stdFeatures, xmlFeatures, null, xsw);
    }

    private ToXmlGenerator createGenerator(StringWriter sw) throws Exception {
        return createGenerator(sw, 0, 0);
    }

    @Test
    public void testFeatureEnumAndDefaults() {
        int defaults = ToXmlGenerator.Feature.collectDefaults();
        Assert.assertEquals(0, defaults);

        for (ToXmlGenerator.Feature f : ToXmlGenerator.Feature.values()) {
            Assert.assertFalse(f.enabledByDefault());
            Assert.assertTrue(f.getMask() > 0);
            Assert.assertTrue(f.enabledIn(f.getMask()));
            Assert.assertFalse(f.enabledIn(0));
        }
    }

    @Test
    public void testFeatureConfiguration() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);

        Assert.assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        gen.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        Assert.assertTrue(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.disable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        Assert.assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        Assert.assertTrue(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        gen.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        Assert.assertFalse(gen.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));

        Assert.assertEquals(0, gen.getFormatFeatures());
        gen.overrideFormatFeatures(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(),
                ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask());
        Assert.assertEquals(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(), gen.getFormatFeatures());

        Assert.assertTrue(gen.canWriteFormattedNumbers());
        Assert.assertEquals(-1, gen.getOutputBuffered());
        Assert.assertNotNull(gen.getOutputTarget());
        Assert.assertNotNull(gen.getStaxWriter());
    }

    @Test
    public void testInitGeneratorXml10And11() throws Exception {
        // XML 1.0 declaration
        StringWriter sw1 = new StringWriter();
        ToXmlGenerator gen1 = createGenerator(sw1, 0, ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask());
        gen1.initGenerator();
        // Calling again should be no-op
        gen1.initGenerator();
        gen1.setNextName(new QName("root"));
        gen1.writeStartObject();
        gen1.writeEndObject();
        gen1.close();
        Assert.assertTrue(sw1.toString().contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));

        // XML 1.1 declaration
        StringWriter sw2 = new StringWriter();
        ToXmlGenerator gen2 = createGenerator(sw2, 0, ToXmlGenerator.Feature.WRITE_XML_1_1.getMask());
        gen2.initGenerator();
        gen2.setNextName(new QName("root"));
        gen2.writeStartObject();
        gen2.writeEndObject();
        gen2.close();
        Assert.assertTrue(sw2.toString().contains("<?xml version=\"1.1\" encoding=\"UTF-8\"?>"));
    }

    @Test
    public void testBasicObjectAndFields() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        Assert.assertTrue(gen.inRoot());

        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        Assert.assertFalse(gen.inRoot());

        gen.writeStringField("str", "value");
        gen.writeFieldName(new SerializedString("str2"));
        gen.writeString(new SerializedString("value2"));

        gen.writeFieldName("num");
        gen.writeNumber(42);

        gen.writeFieldName("long");
        gen.writeNumber(100L);

        gen.writeFieldName("double");
        gen.writeNumber(12.34d);

        gen.writeFieldName("float");
        gen.writeNumber(5.6f);

        gen.writeFieldName("bool");
        gen.writeBoolean(true);

        gen.writeFieldName("nullVal");
        gen.writeNull();

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<root>"));
        Assert.assertTrue(xml.contains("<str>value</str>"));
        Assert.assertTrue(xml.contains("<str2>value2</str2>"));
        Assert.assertTrue(xml.contains("<num>42</num>"));
        Assert.assertTrue(xml.contains("<long>100</long>"));
        Assert.assertTrue(xml.contains("<double>12.34</double>"));
        Assert.assertTrue(xml.contains("<float>5.6</float>"));
        Assert.assertTrue(xml.contains("<bool>true</bool>"));
        Assert.assertTrue(xml.contains("<nullVal/>") || xml.contains("<nullVal></nullVal>"));
        Assert.assertTrue(xml.contains("</root>"));
    }

    @Test
    public void testAttributes() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("attrStr"));
        gen.setNextIsAttribute(true);
        gen.writeString("hello");

        gen.setNextName(new QName("attrChars"));
        gen.setNextIsAttribute(true);
        char[] chars = "world".toCharArray();
        gen.writeString(chars, 0, chars.length);

        gen.setNextName(new QName("attrInt"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(123);

        gen.setNextName(new QName("attrLong"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(456L);

        gen.setNextName(new QName("attrDouble"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(7.89);

        gen.setNextName(new QName("attrFloat"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(1.23f);

        gen.setNextName(new QName("attrBool"));
        gen.setNextIsAttribute(true);
        gen.writeBoolean(false);

        gen.setNextName(new QName("attrBigDec"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigDecimal("99.99"));

        gen.setNextName(new QName("attrBigInt"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigInteger("999999999"));

        gen.setNextName(new QName("attrNull"));
        gen.setNextIsAttribute(true);
        gen.writeNull();

        gen.setNextName(new QName("attrBinary"));
        gen.setNextIsAttribute(true);
        gen.writeBinary(Base64Variants.MIME, new byte[]{1, 2, 3}, 0, 3);

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("attrStr=\"hello\""));
        Assert.assertTrue(xml.contains("attrChars=\"world\""));
        Assert.assertTrue(xml.contains("attrInt=\"123\""));
        Assert.assertTrue(xml.contains("attrLong=\"456\""));
        Assert.assertTrue(xml.contains("attrDouble=\"7.89\""));
        Assert.assertTrue(xml.contains("attrFloat=\"1.23\""));
        Assert.assertTrue(xml.contains("attrBool=\"false\""));
        Assert.assertTrue(xml.contains("attrBigDec=\"99.99\""));
        Assert.assertTrue(xml.contains("attrBigInt=\"999999999\""));
        Assert.assertFalse(xml.contains("attrNull"));
    }

    @Test
    public void testBigNumbersAndPlainDecimal() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.writeFieldName("decPlain");
        gen.writeNumber(new BigDecimal("1e-5"));

        gen.setNextName(new QName("decPlainAttr"));
        gen.setNextIsAttribute(true);
        gen.writeNumber(new BigDecimal("1e-4"));

        gen.setNextName(new QName("decPlainUnwrapped"));
        gen.setNextIsUnwrapped(true);
        gen.writeNumber(new BigDecimal("1.234"));

        gen.writeFieldName("bigInt");
        gen.writeNumber(new BigInteger("12345678901234567890"));

        gen.writeFieldName("encodedNum");
        gen.writeNumber("98765");

        gen.writeFieldName("nullDec");
        gen.writeNumber((BigDecimal) null);

        gen.writeFieldName("nullBigInt");
        gen.writeNumber((BigInteger) null);

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<decPlain>0.00001</decPlain>"));
        Assert.assertTrue(xml.contains("decPlainAttr=\"0.0001\""));
        Assert.assertTrue(xml.contains("1.234"));
        Assert.assertTrue(xml.contains("<bigInt>12345678901234567890</bigInt>"));
        Assert.assertTrue(xml.contains("<encodedNum>98765</encodedNum>"));
        Assert.assertTrue(xml.contains("<nullDec/>") || xml.contains("<nullDec></nullDec>"));
        Assert.assertTrue(xml.contains("<nullBigInt/>") || xml.contains("<nullBigInt></nullBigInt>"));
    }

    @Test
    public void testCDataAndUnwrapped() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.setNextName(new QName("cdataText"));
        gen.setNextIsCData(true);
        gen.writeString("<special>&text</special>");

        gen.setNextName(new QName("cdataChars"));
        gen.setNextIsCData(true);
        char[] chars = "<cdataChars>".toCharArray();
        gen.writeString(chars, 0, chars.length);

        gen.setNextName(new QName("unwrappedStr"));
        gen.setNextIsUnwrapped(true);
        gen.writeString("unwrappedValue");

        gen.setNextName(new QName("unwrappedChars"));
        gen.setNextIsUnwrapped(true);
        char[] unwrappedChars = "rawCharValue".toCharArray();
        gen.writeString(unwrappedChars, 0, unwrappedChars.length);

        gen.setNextName(new QName("unwrappedCData"));
        gen.setNextIsUnwrapped(true);
        gen.setNextIsCData(true);
        gen.writeString("unwrappedCDataVal");

        gen.setNextName(new QName("unwrappedCDataChars"));
        gen.setNextIsUnwrapped(true);
        gen.setNextIsCData(true);
        char[] unwrappedCDChars = "unwrappedCDCharsVal".toCharArray();
        gen.writeString(unwrappedCDChars, 0, unwrappedCDChars.length);

        gen.setNextName(new QName("unwrappedInt"));
        gen.setNextIsUnwrapped(true);
        gen.writeNumber(111);

        gen.setNextName(new QName("unwrappedLong"));
        gen.setNextIsUnwrapped(true);
        gen.writeNumber(222L);

        gen.setNextName(new QName("unwrappedDouble"));
        gen.setNextIsUnwrapped(true);
        gen.writeNumber(333.33);

        gen.setNextName(new QName("unwrappedFloat"));
        gen.setNextIsUnwrapped(true);
        gen.writeNumber(444.44f);

        gen.setNextName(new QName("unwrappedBool"));
        gen.setNextIsUnwrapped(true);
        gen.writeBoolean(true);

        gen.setNextName(new QName("unwrappedBigInt"));
        gen.setNextIsUnwrapped(true);
        gen.writeNumber(new BigInteger("555"));

        gen.setNextName(new QName("unwrappedNull"));
        gen.setNextIsUnwrapped(true);
        gen.writeNull();

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<![CDATA[<special>&text</special>]]>"));
        Assert.assertTrue(xml.contains("<![CDATA[<cdataChars>]]>"));
        Assert.assertTrue(xml.contains("unwrappedValue"));
        Assert.assertTrue(xml.contains("rawCharValue"));
        Assert.assertTrue(xml.contains("<![CDATA[unwrappedCDataVal]]>"));
        Assert.assertTrue(xml.contains("<![CDATA[unwrappedCDCharsVal]]>"));
        Assert.assertTrue(xml.contains("111"));
        Assert.assertTrue(xml.contains("222"));
        Assert.assertTrue(xml.contains("333.33"));
        Assert.assertTrue(xml.contains("444.44"));
        Assert.assertTrue(xml.contains("true"));
        Assert.assertTrue(xml.contains("555"));
    }

    @Test
    public void testBinaryData() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.writeFieldName("binaryLeaf");
        byte[] bytes = new byte[]{10, 20, 30, 40};
        gen.writeBinary(Base64Variants.MIME, bytes, 0, bytes.length);

        gen.setNextName(new QName("unwrappedBinary"));
        gen.setNextIsUnwrapped(true);
        gen.writeBinary(Base64Variants.MIME, bytes, 1, 2);

        gen.writeFieldName("nullBinary");
        gen.writeBinary(Base64Variants.MIME, null, 0, 0);

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<binaryLeaf>"));
        Assert.assertTrue(xml.contains("<nullBinary/>") || xml.contains("<nullBinary></nullBinary>"));
    }

    @Test
    public void testArraysAndWrappedValues() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.startWrappedValue(new QName("itemsWrapper"), new QName("item"));
        gen.writeStartArray();
        gen.writeString("item1");
        gen.setNextName(new QName("item"));
        gen.writeString("item2");
        gen.writeEndArray();
        gen.finishWrappedValue(new QName("itemsWrapper"), new QName("item"));

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<itemsWrapper>"));
        Assert.assertTrue(xml.contains("<item>item1</item>"));
        Assert.assertTrue(xml.contains("<item>item2</item>"));
        Assert.assertTrue(xml.contains("</itemsWrapper>"));
    }

    @Test
    public void testPrettyPrinterSupport() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        DefaultXmlPrettyPrinter pp = new DefaultXmlPrettyPrinter();
        gen.setPrettyPrinter(pp);
        Assert.assertNotNull(gen._constructDefaultPrettyPrinter());

        gen.setNextName(new QName("root"));
        gen.writeStartObject();

        gen.writeFieldName("leaf");
        gen.writeString("val");

        gen.writeFieldName("leafChars");
        gen.writeString("val2".toCharArray(), 0, 4);

        gen.writeFieldName("leafInt");
        gen.writeNumber(10);

        gen.writeFieldName("leafLong");
        gen.writeNumber(20L);

        gen.writeFieldName("leafDouble");
        gen.writeNumber(30.5);

        gen.writeFieldName("leafFloat");
        gen.writeNumber(40.5f);

        gen.writeFieldName("leafBool");
        gen.writeBoolean(false);

        gen.writeFieldName("leafBigDec");
        gen.writeNumber(new BigDecimal("50.5"));

        gen.writeFieldName("leafBigInt");
        gen.writeNumber(new BigInteger("60"));

        gen.writeFieldName("leafNull");
        gen.writeNull();

        gen.writeFieldName("leafBin");
        gen.writeBinary(Base64Variants.MIME, new byte[]{1, 2}, 0, 2);

        gen.startWrappedValue(new QName("wrap"), new QName("elem"));
        gen.writeStartArray();
        gen.writeString("elemVal");
        gen.writeEndArray();
        gen.finishWrappedValue(new QName("wrap"), new QName("elem"));

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<root>"));
        Assert.assertTrue(xml.contains("<leaf>val</leaf>"));
        Assert.assertTrue(xml.contains("<leafChars>val2</leafChars>"));
    }

    @Test
    public void testSetNextNameIfMissingAndRepeatedFieldName() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);

        QName name1 = new QName("elem1");
        QName name2 = new QName("elem2");
        Assert.assertTrue(gen.setNextNameIfMissing(name1));
        Assert.assertFalse(gen.setNextNameIfMissing(name2));

        gen.writeStartObject();

        gen.setNextName(new QName("repeated"));
        gen.writeRepeatedFieldName();
        gen.writeString("v1");

        gen.setNextName(new QName("repeated"));
        gen.writeRepeatedFieldName();
        gen.writeString("v2");

        gen.writeEndObject();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<elem1>"));
        Assert.assertTrue(xml.contains("<repeated>v1</repeated>"));
        Assert.assertTrue(xml.contains("<repeated>v2</repeated>"));
    }

    @Test
    public void testRawMethodsTriggerStax2EmulationReport() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);

        try {
            gen.writeRaw("raw");
            Assert.fail("Expected JsonGenerationException due to stax2 emulation");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("does not implement Stax2 API"));
        }

        try {
            gen.writeRaw("raw", 0, 3);
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("does not implement Stax2 API"));
        }

        try {
            gen.writeRaw("raw".toCharArray(), 0, 3);
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("does not implement Stax2 API"));
        }

        try {
            gen.writeRaw('c');
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("does not implement Stax2 API"));
        }

        try {
            gen.writeRawValue("rawValue");
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("does not implement Stax2 API"));
        }

        try {
            gen.writeRawValue("rawValue", 0, 5);
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("does not implement Stax2 API"));
        }

        try {
            gen.writeRawValue("rawValue".toCharArray(), 0, 5);
            Assert.fail("Expected JsonGenerationException");
        } catch (JsonGenerationException e) {
            Assert.assertTrue(e.getMessage().contains("does not implement Stax2 API"));
        }

        try {
            gen.writeRawValue(new SerializedString("raw"));
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8StringUnsupported() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen.writeRawUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8StringUnsupported() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen.writeUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingNameThrowsException() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen.writeStartObject();
    }

    @Test(expected = JsonGenerationException.class)
    public void testEndElementWithoutStartThrowsException() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen._handleEndObject();
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEndArrayWithoutArrayContextThrows() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen.writeEndArray();
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObjectWithoutObjectContextThrows() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen.writeEndObject();
    }

    @Test
    public void testAutoCloseAndFlush() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        gen.enable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        gen.setNextName(new QName("root"));
        gen.writeStartObject();
        gen.writeFieldName("items");
        gen.writeStartArray();
        gen.writeString("item1");

        gen.flush();
        gen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<root>"));
        Assert.assertTrue(xml.contains("item1"));
        Assert.assertTrue(xml.contains("</root>"));
    }

    @Test
    public void testReleaseBuffersNoOp() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator gen = createGenerator(sw);
        gen._releaseBuffers();
    }
}
