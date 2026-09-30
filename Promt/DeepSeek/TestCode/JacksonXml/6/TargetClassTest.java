package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.ri.Stax2WriterAdapter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.dataformat.xml.XmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ToXmlGeneratorTest {

    private XMLStreamWriter2 mockWriter;
    private IOContext mockIOContext;
    private ObjectCodec mockCodec;
    private ToXmlGenerator generator;

    @Before
    public void setUp() throws Exception {
        mockWriter = mock(XMLStreamWriter2.class);
        mockIOContext = mock(IOContext.class);
        mockCodec = mock(ObjectCodec.class);
        when(mockIOContext.isResourceManaged()).thenReturn(false);
        generator = new ToXmlGenerator(mockIOContext, 0, 0, mockCodec, mockWriter);
    }

    @After
    public void tearDown() throws Exception {
        if (generator != null && !generator.isClosed()) {
            generator.close();
        }
    }

    // Test Feature enum
    @Test
    public void testFeatureDefaults() {
        assertFalse(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.enabledByDefault());
        assertFalse(ToXmlGenerator.Feature.WRITE_XML_1_1.enabledByDefault());
        assertEquals(0, ToXmlGenerator.Feature.collectDefaults());
    }

    @Test
    public void testFeatureMasks() {
        assertTrue(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask() > 0);
        assertTrue(ToXmlGenerator.Feature.WRITE_XML_1_1.getMask() > 0);
        assertNotEquals(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(),
                ToXmlGenerator.Feature.WRITE_XML_1_1.getMask());
    }

    @Test
    public void testFeatureEnabledIn() {
        int flags = ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask();
        assertTrue(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.enabledIn(flags));
        assertFalse(ToXmlGenerator.Feature.WRITE_XML_1_1.enabledIn(flags));
    }

    // Test initGenerator
    @Test
    public void testInitGeneratorNoDeclaration() throws Exception {
        generator.initGenerator();
        generator.initGenerator(); // second call should be no-op
        verify(mockWriter, never()).writeStartDocument(anyString(), anyString());
    }

    @Test
    public void testInitGeneratorWithXmlDeclaration() throws Exception {
        generator = new ToXmlGenerator(mockIOContext, 0,
                ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(), mockCodec, mockWriter);
        generator.initGenerator();
        verify(mockWriter).writeStartDocument("UTF-8", "1.0");
    }

    @Test
    public void testInitGeneratorWithXml11() throws Exception {
        generator = new ToXmlGenerator(mockIOContext, 0,
                ToXmlGenerator.Feature.WRITE_XML_1_1.getMask(), mockCodec, mockWriter);
        generator.initGenerator();
        verify(mockWriter).writeStartDocument("UTF-8", "1.1");
    }

    @Test
    public void testInitGeneratorWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter prettyPrinter = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(prettyPrinter);
        generator.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        generator.initGenerator();
        verify(prettyPrinter).writePrologLinefeed(any(XMLStreamWriter2.class));
    }

    @Test(expected = JsonGenerationException.class)
    public void testInitGeneratorXmlStreamException() throws Exception {
        doThrow(new XMLStreamException("test")).when(mockWriter).writeStartDocument(anyString(), anyString());
        generator = new ToXmlGenerator(mockIOContext, 0,
                ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(), mockCodec, mockWriter);
        generator.initGenerator();
    }

    // Test configuration methods
    @Test
    public void testEnableDisableFeature() {
        assertFalse(generator.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        generator.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertTrue(generator.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        generator.disable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertFalse(generator.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
    }

    @Test
    public void testConfigureFeature() {
        generator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        assertTrue(generator.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
        generator.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);
        assertFalse(generator.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
    }

    @Test
    public void testOverrideFormatFeatures() {
        generator.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        generator.overrideFormatFeatures(0, ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask());
        assertFalse(generator.isEnabled(ToXmlGenerator.Feature.WRITE_XML_DECLARATION));
    }

    @Test
    public void testGetFormatFeatures() {
        assertEquals(0, generator.getFormatFeatures());
        generator.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        assertEquals(ToXmlGenerator.Feature.WRITE_XML_DECLARATION.getMask(), generator.getFormatFeatures());
    }

    @Test
    public void testGetOutputTarget() {
        assertSame(mockWriter, generator.getOutputTarget());
    }

    @Test
    public void testGetOutputBuffered() {
        assertEquals(-1, generator.getOutputBuffered());
    }

    @Test
    public void testCanWriteFormattedNumbers() {
        assertTrue(generator.canWriteFormattedNumbers());
    }

    @Test
    public void testInRoot() {
        assertTrue(generator.inRoot());
    }

    @Test
    public void testGetStaxWriter() {
        assertSame(mockWriter, generator.getStaxWriter());
    }

    // Test setNextName and related
    @Test
    public void testSetNextName() {
        QName name = new QName("http://example.com", "test");
        generator.setNextName(name);
        // No direct getter, but we can test via setNextNameIfMissing
        assertFalse(generator.setNextNameIfMissing(new QName("other")));
    }

    @Test
    public void testSetNextNameIfMissing() {
        assertTrue(generator.setNextNameIfMissing(new QName("http://example.com", "test")));
        assertFalse(generator.setNextNameIfMissing(new QName("http://example.com", "other")));
    }

    @Test
    public void testSetNextIsAttribute() {
        generator.setNextIsAttribute(true);
        // No direct getter, but we can test behavior
    }

    @Test
    public void testSetNextIsUnwrapped() {
        generator.setNextIsUnwrapped(true);
        // No direct getter, but we can test behavior
    }

    @Test
    public void testSetNextIsCData() {
        generator.setNextIsCData(true);
        // No direct getter, but we can test behavior
    }

    // Test writeFieldName
    @Test
    public void testWriteFieldName() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("testField");
        // Should not throw
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldNameExpectingValue() throws Exception {
        generator.writeStartObject();
        generator.writeFieldName("testField");
        generator.writeFieldName("anotherField"); // Should fail
    }

    @Test
    public void testWriteFieldNameSerializableString() throws Exception {
        generator.writeStartObject();
        SerializableString ss = mock(SerializableString.class);
        when(ss.getValue()).thenReturn("testField");
        generator.writeFieldName(ss);
        // Should not throw
    }

    // Test writeStringField
    @Test
    public void testWriteStringField() throws Exception {
        generator.writeStartObject();
        generator.setNextName(new QName("http://example.com", "field"));
        generator.writeStringField("field", "value");
        verify(mockWriter).writeStartElement("http://example.com", "field");
        verify(mockWriter).writeCharacters("value");
        verify(mockWriter).writeEndElement();
    }

    // Test writeStartArray/writeEndArray
    @Test
    public void testWriteStartEndArray() throws Exception {
        generator.writeStartArray();
        assertFalse(generator.inRoot());
        generator.writeEndArray();
        assertTrue(generator.inRoot());
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEndArrayNotInArray() throws Exception {
        generator.writeEndArray();
    }

    // Test writeStartObject/writeEndObject
    @Test
    public void testWriteStartEndObject() throws Exception {
        generator.setNextName(new QName("http://example.com", "root"));
        generator.writeStartObject();
        assertFalse(generator.inRoot());
        generator.writeEndObject();
        assertTrue(generator.inRoot());
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObjectNotInObject() throws Exception {
        generator.writeEndObject();
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteStartObjectMissingName() throws Exception {
        generator.writeStartObject();
    }

    @Test(expected = JsonGenerationException.class)
    public void testHandleEndObjectEmptyStack() throws Exception {
        generator._handleEndObject();
    }

    // Test writeString
    @Test
    public void testWriteStringAsAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeString("value");
        verify(mockWriter).writeAttribute("http://example.com", "attr", "value");
    }

    @Test
    public void testWriteStringAsElement() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeString("value");
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeCharacters("value");
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteStringAsCData() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsCData(true);
        generator.writeString("value");
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeCData("value");
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteStringUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeString("value");
        verify(mockWriter).writeCharacters("value");
    }

    @Test
    public void testWriteStringUnwrappedCData() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.setNextIsCData(true);
        generator.writeString("value");
        verify(mockWriter).writeCData("value");
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteStringMissingName() throws Exception {
        generator.writeString("value");
    }

    // Test writeString with char array
    @Test
    public void testWriteStringCharArray() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeString("value".toCharArray(), 0, 5);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeCharacters("value".toCharArray(), 0, 5);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteStringCharArrayAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeString("value".toCharArray(), 0, 5);
        verify(mockWriter).writeAttribute(eq("http://example.com"), eq("attr"), anyString());
    }

    @Test
    public void testWriteStringCharArrayUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeString("value".toCharArray(), 0, 5);
        verify(mockWriter).writeCharacters("value".toCharArray(), 0, 5);
    }

    @Test
    public void testWriteStringCharArrayUnwrappedCData() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.setNextIsCData(true);
        generator.writeString("value".toCharArray(), 0, 5);
        verify(mockWriter).writeCData("value".toCharArray(), 0, 5);
    }

    // Test writeBoolean
    @Test
    public void testWriteBoolean() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeBoolean(true);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeBoolean(true);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteBooleanAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeBoolean(false);
        verify(mockWriter).writeBooleanAttribute(null, "http://example.com", "attr", false);
    }

    @Test
    public void testWriteBooleanUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeBoolean(true);
        verify(mockWriter).writeBoolean(true);
    }

    // Test writeNull
    @Test
    public void testWriteNull() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNull();
        verify(mockWriter).writeEmptyElement("http://example.com", "elem");
    }

    @Test
    public void testWriteNullAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeNull();
        // Should not write anything for attribute null
        verify(mockWriter, never()).writeEmptyElement(anyString(), anyString());
    }

    @Test
    public void testWriteNullUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeNull();
        // Should not write anything for unwrapped null
        verify(mockWriter, never()).writeEmptyElement(anyString(), anyString());
    }

    // Test writeNumber int
    @Test
    public void testWriteNumberInt() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(42);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeInt(42);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteNumberIntAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeNumber(42);
        verify(mockWriter).writeIntAttribute(null, "http://example.com", "attr", 42);
    }

    @Test
    public void testWriteNumberIntUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeNumber(42);
        verify(mockWriter).writeInt(42);
    }

    // Test writeNumber long
    @Test
    public void testWriteNumberLong() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(42L);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeLong(42L);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteNumberLongAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeNumber(42L);
        verify(mockWriter).writeLongAttribute(null, "http://example.com", "attr", 42L);
    }

    @Test
    public void testWriteNumberLongUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeNumber(42L);
        verify(mockWriter).writeLong(42L);
    }

    // Test writeNumber double
    @Test
    public void testWriteNumberDouble() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(3.14);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeDouble(3.14);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteNumberDoubleAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeNumber(3.14);
        verify(mockWriter).writeDoubleAttribute(null, "http://example.com", "attr", 3.14);
    }

    @Test
    public void testWriteNumberDoubleUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeNumber(3.14);
        verify(mockWriter).writeDouble(3.14);
    }

    // Test writeNumber float
    @Test
    public void testWriteNumberFloat() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(3.14f);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeFloat(3.14f);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteNumberFloatAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeNumber(3.14f);
        verify(mockWriter).writeFloatAttribute(null, "http://example.com", "attr", 3.14f);
    }

    @Test
    public void testWriteNumberFloatUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeNumber(3.14f);
        verify(mockWriter).writeFloat(3.14f);
    }

    // Test writeNumber BigDecimal
    @Test
    public void testWriteNumberBigDecimal() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(new BigDecimal("3.14"));
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeDecimal(any(BigDecimal.class));
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteNumberBigDecimalNull() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber((BigDecimal) null);
        verify(mockWriter).writeEmptyElement("http://example.com", "elem");
    }

    @Test
    public void testWriteNumberBigDecimalAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeNumber(new BigDecimal("3.14"));
        verify(mockWriter).writeDecimalAttribute("", "http://example.com", "attr", new BigDecimal("3.14"));
    }

    @Test
    public void testWriteNumberBigDecimalUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeNumber(new BigDecimal("3.14"));
        verify(mockWriter).writeDecimal(any(BigDecimal.class));
    }

    @Test
    public void testWriteNumberBigDecimalAsPlain() throws Exception {
        generator.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        generator.setNextName(new QName("http://example.com", "elem"));
        BigDecimal dec = new BigDecimal("3.14");
        generator.writeNumber(dec);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeCharacters(dec.toPlainString());
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteNumberBigDecimalAsPlainAttribute() throws Exception {
        generator.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        BigDecimal dec = new BigDecimal("3.14");
        generator.writeNumber(dec);
        verify(mockWriter).writeAttribute("", "http://example.com", "attr", dec.toPlainString());
    }

    @Test
    public void testWriteNumberBigDecimalAsPlainUnwrapped() throws Exception {
        generator.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        BigDecimal dec = new BigDecimal("3.14");
        generator.writeNumber(dec);
        verify(mockWriter).writeCharacters(dec.toPlainString());
    }

    // Test writeNumber BigInteger
    @Test
    public void testWriteNumberBigInteger() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(new BigInteger("42"));
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeInteger(any(BigInteger.class));
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteNumberBigIntegerNull() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber((BigInteger) null);
        verify(mockWriter).writeEmptyElement("http://example.com", "elem");
    }

    @Test
    public void testWriteNumberBigIntegerAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeNumber(new BigInteger("42"));
        verify(mockWriter).writeIntegerAttribute("", "http://example.com", "attr", new BigInteger("42"));
    }

    @Test
    public void testWriteNumberBigIntegerUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        generator.writeNumber(new BigInteger("42"));
        verify(mockWriter).writeInteger(any(BigInteger.class));
    }

    // Test writeNumber String
    @Test
    public void testWriteNumberString() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber("42");
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeCharacters("42");
        verify(mockWriter).writeEndElement();
    }

    // Test writeBinary
    @Test
    public void testWriteBinary() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        byte[] data = new byte[]{1, 2, 3};
        generator.writeBinary(Base64Variants.getDefaultVariant(), data, 0, 3);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeBinary(data, 0, 3);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteBinaryNull() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeBinary(Base64Variants.getDefaultVariant(), null, 0, 0);
        verify(mockWriter).writeEmptyElement("http://example.com", "elem");
    }

    @Test
    public void testWriteBinaryAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        byte[] data = new byte[]{1, 2, 3};
        generator.writeBinary(Base64Variants.getDefaultVariant(), data, 0, 3);
        verify(mockWriter).writeBinaryAttribute(eq(""), eq("http://example.com"), eq("attr"), any(byte[].class));
    }

    @Test
    public void testWriteBinaryUnwrapped() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsUnwrapped(true);
        byte[] data = new byte[]{1, 2, 3};
        generator.writeBinary(Base64Variants.getDefaultVariant(), data, 0, 3);
        verify(mockWriter).writeBinary(data, 0, 3);
    }

    // Test writeRaw
    @Test
    public void testWriteRaw() throws Exception {
        generator.writeRaw("raw text");
        verify(mockWriter).writeRaw("raw text");
    }

    @Test
    public void testWriteRawWithOffset() throws Exception {
        generator.writeRaw("raw text", 0, 3);
        verify(mockWriter).writeRaw("raw text", 0, 3);
    }

    @Test
    public void testWriteRawCharArray() throws Exception {
        char[] text = "raw text".toCharArray();
        generator.writeRaw(text, 0, 3);
        verify(mockWriter).writeRaw(text, 0, 3);
    }

    @Test
    public void testWriteRawChar() throws Exception {
        generator.writeRaw('a');
        verify(mockWriter).writeRaw("a");
    }

    // Test writeRawValue
    @Test
    public void testWriteRawValue() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeRawValue("raw value");
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeRaw("raw value");
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteRawValueAttribute() throws Exception {
        generator.setNextName(new QName("http://example.com", "attr"));
        generator.setNextIsAttribute(true);
        generator.writeRawValue("raw value");
        verify(mockWriter).writeAttribute("http://example.com", "attr", "raw value");
    }

    @Test
    public void testWriteRawValueWithOffset() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeRawValue("raw value", 0, 3);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeRaw("raw value", 0, 3);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testWriteRawValueCharArray() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        char[] text = "raw value".toCharArray();
        generator.writeRawValue(text, 0, 3);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeRaw(text, 0, 3);
        verify(mockWriter).writeEndElement();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawValueSerializableString() throws Exception {
        generator.writeRawValue(mock(SerializableString.class));
    }

    // Test writeRawUTF8String and writeUTF8String
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8String() throws Exception {
        generator.writeRawUTF8String(new byte[]{1, 2, 3}, 0, 3);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8String() throws Exception {
        generator.writeUTF8String(new byte[]{1, 2, 3}, 0, 3);
    }

    // Test flush
    @Test
    public void testFlush() throws Exception {
        generator.enable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
        generator.flush();
        verify(mockWriter).flush();
    }

    @Test
    public void testFlushNotEnabled() throws Exception {
        generator.flush();
        verify(mockWriter, never()).flush();
    }

    // Test close
    @Test
    public void testClose() throws Exception {
        generator.close();
        verify(mockWriter).close();
    }

    @Test
    public void testCloseResourceManaged() throws Exception {
        when(mockIOContext.isResourceManaged()).thenReturn(true);
        generator.close();
        verify(mockWriter).closeCompletely();
    }

    @Test
    public void testCloseAutoCloseTarget() throws Exception {
        generator.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        generator.close();
        verify(mockWriter).closeCompletely();
    }

    @Test
    public void testCloseAutoCloseJsonContent() throws Exception {
        generator.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        generator.setNextName(new QName("http://example.com", "root"));
        generator.writeStartObject();
        generator.close();
        verify(mockWriter).close();
    }

    // Test startWrappedValue and finishWrappedValue
    @Test
    public void testStartWrappedValue() throws Exception {
        QName wrapper = new QName("http://example.com", "wrapper");
        QName wrapped = new QName("http://example.com", "wrapped");
        generator.startWrappedValue(wrapper, wrapped);
        verify(mockWriter).writeStartElement("http://example.com", "wrapper");
    }

    @Test
    public void testStartWrappedValueNullWrapper() throws Exception {
        QName wrapped = new QName("http://example.com", "wrapped");
        generator.startWrappedValue(null, wrapped);
        verify(mockWriter, never()).writeStartElement(anyString(), anyString());
    }

    @Test
    public void testFinishWrappedValue() throws Exception {
        QName wrapper = new QName("http://example.com", "wrapper");
        QName wrapped = new QName("http://example.com", "wrapped");
        generator.finishWrappedValue(wrapper, wrapped);
        verify(mockWriter).writeEndElement();
    }

    @Test
    public void testFinishWrappedValueNullWrapper() throws Exception {
        QName wrapped = new QName("http://example.com", "wrapped");
        generator.finishWrappedValue(null, wrapped);
        verify(mockWriter, never()).writeEndElement();
    }

    // Test writeRepeatedFieldName
    @Test
    public void testWriteRepeatedFieldName() throws Exception {
        generator.writeStartObject();
        generator.setNextName(new QName("http://example.com", "field"));
        generator.writeRepeatedFieldName();
        // Should not throw
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteRepeatedFieldNameExpectingValue() throws Exception {
        generator.writeStartObject();
        generator.setNextName(new QName("http://example.com", "field"));
        generator.writeRepeatedFieldName();
        generator.writeRepeatedFieldName(); // Should fail
    }

    // Test setPrettyPrinter
    @Test
    public void testSetPrettyPrinter() {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        // No direct getter, but we can test behavior
    }

    @Test
    public void testSetPrettyPrinterNonXml() {
        PrettyPrinter pp = mock(PrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        // Should not throw
    }

    // Test _constructDefaultPrettyPrinter
    @Test
    public void testConstructDefaultPrettyPrinter() {
        PrettyPrinter pp = generator._constructDefaultPrettyPrinter();
        assertTrue(pp instanceof DefaultXmlPrettyPrinter);
    }

    // Test _verifyValueWrite
    @Test(expected = JsonGenerationException.class)
    public void testVerifyValueWriteExpectingName() throws Exception {
        generator._verifyValueWrite("test");
    }

    // Test _releaseBuffers
    @Test
    public void testReleaseBuffers() {
        generator._releaseBuffers();
        // Should not throw
    }

    // Test handleMissingName
    @Test(expected = IllegalStateException.class)
    public void testHandleMissingName() {
        generator.handleMissingName();
    }

    // Test checkNextIsUnwrapped
    @Test
    public void testCheckNextIsUnwrapped() {
        assertFalse(generator.checkNextIsUnwrapped());
        generator.setNextIsUnwrapped(true);
        assertTrue(generator.checkNextIsUnwrapped());
        assertFalse(generator.checkNextIsUnwrapped());
    }

    // Test _reportUnimplementedStax2
    @Test(expected = JsonGenerationException.class)
    public void testReportUnimplementedStax2() throws Exception {
        generator._reportUnimplementedStax2("testMethod");
    }

    // Test toFullBuffer
    @Test
    public void testToFullBuffer() throws Exception {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        byte[] result = generator.toFullBuffer(data, 1, 3);
        assertArrayEquals(new byte[]{2, 3, 4}, result);
    }

    @Test
    public void testToFullBufferFullArray() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        byte[] result = generator.toFullBuffer(data, 0, 3);
        assertSame(data, result);
    }

    @Test
    public void testToFullBufferEmpty() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        byte[] result = generator.toFullBuffer(data, 0, 0);
        assertEquals(0, result.length);
    }

    // Test with pretty printer
    @Test
    public void testWriteStringWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeString("value");
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq("value"), eq(false));
    }

    @Test
    public void testWriteStringWithPrettyPrinterCData() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.setNextIsCData(true);
        generator.writeString("value");
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq("value"), eq(true));
    }

    @Test
    public void testWriteBooleanWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeBoolean(true);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(true));
    }

    @Test
    public void testWriteNullWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNull();
        verify(pp).writeLeafNullElement(mockWriter, "http://example.com", "elem");
    }

    @Test
    public void testWriteNumberIntWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(42);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(42));
    }

    @Test
    public void testWriteNumberLongWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(42L);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(42L));
    }

    @Test
    public void testWriteNumberDoubleWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(3.14);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(3.14));
    }

    @Test
    public void testWriteNumberFloatWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        generator.writeNumber(3.14f);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(3.14f));
    }

    @Test
    public void testWriteNumberBigDecimalWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        BigDecimal dec = new BigDecimal("3.14");
        generator.writeNumber(dec);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(dec));
    }

    @Test
    public void testWriteNumberBigIntegerWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        BigInteger bi = new BigInteger("42");
        generator.writeNumber(bi);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(bi));
    }

    @Test
    public void testWriteBinaryWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "elem"));
        byte[] data = new byte[]{1, 2, 3};
        generator.writeBinary(Base64Variants.getDefaultVariant(), data, 0, 3);
        verify(pp).writeLeafElement(eq(mockWriter), eq("http://example.com"), eq("elem"), eq(data), eq(0), eq(3));
    }

    @Test
    public void testStartWrappedValueWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        QName wrapper = new QName("http://example.com", "wrapper");
        QName wrapped = new QName("http://example.com", "wrapped");
        generator.startWrappedValue(wrapper, wrapped);
        verify(pp).writeStartElement(mockWriter, "http://example.com", "wrapper");
    }

    @Test
    public void testFinishWrappedValueWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        QName wrapper = new QName("http://example.com", "wrapper");
        QName wrapped = new QName("http://example.com", "wrapped");
        generator.finishWrappedValue(wrapper, wrapped);
        verify(pp).writeEndElement(eq(mockWriter), anyInt());
    }

    @Test
    public void testWriteStartObjectWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "root"));
        generator.writeStartObject();
        verify(pp).writeStartObject(generator);
    }

    @Test
    public void testWriteEndObjectWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.setNextName(new QName("http://example.com", "root"));
        generator.writeStartObject();
        generator.writeEndObject();
        verify(pp).writeEndObject(eq(generator), anyInt());
    }

    @Test
    public void testWriteStartArrayWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.writeStartArray();
        verify(pp).writeStartArray(generator);
    }

    @Test
    public void testWriteEndArrayWithPrettyPrinter() throws Exception {
        XmlPrettyPrinter pp = mock(XmlPrettyPrinter.class);
        generator.setPrettyPrinter(pp);
        generator.writeStartArray();
        generator.writeEndArray();
        verify(pp).writeEndArray(eq(generator), anyInt());
    }

    // Test stax2 emulation
    @Test(expected = JsonGenerationException.class)
    public void testWriteRawWithStax2Emulation() throws Exception {
        XMLStreamWriter nonStax2Writer = mock(XMLStreamWriter.class);
        ToXmlGenerator gen = new ToXmlGenerator(mockIOContext, 0, 0, mockCodec, nonStax2Writer);
        gen.writeRaw("test");
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteRawValueWithStax2Emulation() throws Exception {
        XMLStreamWriter nonStax2Writer = mock(XMLStreamWriter.class);
        ToXmlGenerator gen = new ToXmlGenerator(mockIOContext, 0, 0, mockCodec, nonStax2Writer);
        gen.setNextName(new QName("http://example.com", "elem"));
        gen.writeRawValue("test");
    }

    // Test writeString with SerializableString
    @Test
    public void testWriteStringSerializableString() throws Exception {
        generator.setNextName(new QName("http://example.com", "elem"));
        SerializableString ss = mock(SerializableString.class);
        when(ss.getValue()).thenReturn("value");
        generator.writeString(ss);
        verify(mockWriter).writeStartElement("http://example.com", "elem");
        verify(mockWriter).writeCharacters("value");
        verify(mockWriter).writeEndElement();
    }
}
