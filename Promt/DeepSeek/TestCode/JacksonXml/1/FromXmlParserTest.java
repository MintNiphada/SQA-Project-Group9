package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class FromXmlParserTest {

    @Mock
    private IOContext ioContext;
    @Mock
    private ObjectCodec objectCodec;
    @Mock
    private XMLStreamReader xmlStreamReader;

    private FromXmlParser parser;

    private static final int DEFAULT_GENERIC_FEATURES = 0;
    private static final int DEFAULT_XML_FEATURES = 0;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(ioContext.getSourceReference()).thenReturn(com.fasterxml.jackson.core.JsonLocation.NA);
        parser = new FromXmlParser(ioContext, DEFAULT_GENERIC_FEATURES, DEFAULT_XML_FEATURES,
                objectCodec, xmlStreamReader);
    }

    @Test
    public void testVersion() {
        assertNotNull(parser.version());
    }

    @Test
    public void testGetSetCodec() {
        assertSame(objectCodec, parser.getCodec());
        ObjectCodec newCodec = mock(ObjectCodec.class);
        parser.setCodec(newCodec);
        assertSame(newCodec, parser.getCodec());
        parser.setCodec(objectCodec); // restore
    }

    @Test
    public void testSetXMLTextElementName() {
        assertEquals("", parser._cfgNameForTextElement);
        parser.setXMLTextElementName("value");
        assertEquals("value", parser._cfgNameForTextElement);
        parser.setXMLTextElementName("");
        assertEquals("", parser._cfgNameForTextElement);
    }

    @Test
    public void testRequiresCustomCodec() {
        assertTrue(parser.requiresCustomCodec());
    }

    @Test
    public void testFeatureEnableDisableIsEnabledConfigure() {
        // No features defined in enum, but we can still test the infrastructure
        // The Feature enum is empty, so we can't instantiate it.
        // We'll just test that methods don't throw.
        // We'll use mock or fake feature by accessing _formatFeatures directly.
        // Since Feature enum has no instances, we can't really test these methods,
        // but we can verify they don't crash and the mask logic works if we had features.
        // We'll just call them with null to see if they handle it (they won't, NPE).
        // So we'll skip direct feature method tests for empty enum.
    }

    @Test
    public void testFeatureMethodsWithMockFeature() {
        // Since Feature is an empty enum, we can't test these methods directly.
        // But we can test that the bit manipulation methods work correctly on _formatFeatures.
        int initial = parser.getFormatFeatures();
        assertEquals(0, initial);

        // overrideFormatFeatures
        parser.overrideFormatFeatures(0x01, 0x03);
        assertEquals(1, parser.getFormatFeatures());
        parser.overrideFormatFeatures(0, 0x01);
        assertEquals(0, parser.getFormatFeatures());
    }

    @Test
    public void testGetStaxReader() {
        assertSame(xmlStreamReader, parser.getStaxReader());
    }

    @Test
    public void testIsClosed() {
        assertFalse(parser.isClosed());
    }

    @Test
    public void testGetParsingContext() {
        assertNotNull(parser.getParsingContext());
    }

    @Test
    public void testGetTokenLocation() {
        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
    }

    @Test
    public void testGetCurrentLocation() {
        JsonLocation loc = parser.getCurrentLocation();
        assertNotNull(loc);
    }

    @Test
    public void testHasTextCharacters() {
        assertFalse(parser.hasTextCharacters());
    }

    @Test
    public void testGetEmbeddedObject() throws IOException {
        assertNull(parser.getEmbeddedObject());
    }

    @Test(expected = JsonParseException.class)
    public void testGetBinaryValueNotStringOrEmbedded() throws IOException {
        // Set current token to something else
        parser._currToken = JsonToken.START_OBJECT;
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    @Test
    public void testGetBinaryValueWithEmbeddedObjectNullBinary() throws IOException {
        parser._currToken = JsonToken.VALUE_EMBEDDED_OBJECT;
        try {
            parser.getBinaryValue(Base64Variants.getDefaultVariant());
            fail("Should have thrown");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test
    public void testGetTextCharacters() throws IOException {
        // Before any token
        assertNull(parser.getTextCharacters());

        // Simulate VALUE_STRING with text
        parser._currToken = JsonToken.VALUE_STRING;
        parser._currText = "test";
        assertArrayEquals("test".toCharArray(), parser.getTextCharacters());

        // Simulate FIELD_NAME
        // Need to set context name
        parser._parsingContext.setCurrentName("field");
        parser._currToken = JsonToken.FIELD_NAME;
        assertArrayEquals("field".toCharArray(), parser.getTextCharacters());

        // null token case
        parser._currToken = null;
        assertNull(parser.getTextCharacters());
    }

    @Test
    public void testGetTextLength() throws IOException {
        parser._currToken = JsonToken.VALUE_STRING;
        parser._currText = "hello";
        assertEquals(5, parser.getTextLength());

        parser._currToken = null;
        assertEquals(0, parser.getTextLength());
    }

    @Test
    public void testGetTextOffset() throws IOException {
        assertEquals(0, parser.getTextOffset());
    }

    @Test
    public void testGetValueAsStringDefault() throws IOException {
        // null token
        assertNull(parser.getValueAsString(null));
        assertEquals("default", parser.getValueAsString("default"));

        // VALUE_STRING
        parser._currToken = JsonToken.VALUE_STRING;
        parser._currText = "myText";
        assertEquals("myText", parser.getValueAsString(null));

        // FIELD_NAME
        parser._currToken = JsonToken.FIELD_NAME;
        parser._parsingContext.setCurrentName("fieldName");
        assertEquals("fieldName", parser.getValueAsString(null));

        // START_OBJECT that can be converted
        when(xmlStreamReader.convertToString()).thenReturn("converted");
        parser._currToken = JsonToken.START_OBJECT;
        assertEquals("converted", parser.getValueAsString(null));
        // check state changes
        assertEquals(JsonToken.VALUE_STRING, parser._currToken);
        assertEquals("converted", parser._currText);

        // START_OBJECT that cannot be converted
        when(xmlStreamReader.convertToString()).thenReturn(null);
        parser._currToken = JsonToken.START_OBJECT;
        assertNull(parser.getValueAsString("default"));
        assertEquals("default", parser.getValueAsString("default"));

        // Other scalar value (simulated)
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        // asString returns token representation, but we don't have a real value
        // For coverage, we just test it doesn't crash
        String res = parser.getValueAsString(null);
        assertNotNull(res);
    }

    @Test
    public void testGetBigIntegerValue() throws IOException {
        assertNull(parser.getBigIntegerValue());
    }

    @Test
    public void testGetDecimalValue() throws IOException {
        assertNull(parser.getDecimalValue());
    }

    @Test
    public void testGetDoubleValue() throws IOException {
        assertEquals(0.0, parser.getDoubleValue(), 0);
    }

    @Test
    public void testGetFloatValue() throws IOException {
        assertEquals(0.0f, parser.getFloatValue(), 0);
    }

    @Test
    public void testGetIntValue() throws IOException {
        assertEquals(0, parser.getIntValue());
    }

    @Test
    public void testGetLongValue() throws IOException {
        assertEquals(0L, parser.getLongValue());
    }

    @Test
    public void testGetNumberType() throws IOException {
        assertNull(parser.getNumberType());
    }

    @Test
    public void testGetNumberValue() throws IOException {
        assertNull(parser.getNumberValue());
    }

    @Test
    public void testClose() throws IOException {
        when(ioContext.isResourceManaged()).thenReturn(false);
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testCloseWithResourceManaged() throws IOException {
        when(ioContext.isResourceManaged()).thenReturn(true);
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testOverrideCurrentName() {
        parser._currToken = JsonToken.FIELD_NAME;
        parser._parsingContext.setCurrentName("original");
        parser.overrideCurrentName("newName");
        assertEquals("newName", parser._parsingContext.getCurrentName());

        // For START_OBJECT, parent context should be updated
        XmlReadContext parent = parser._parsingContext;
        parser._parsingContext = parser._parsingContext.createChildObjectContext(-1, -1);
        parser._currToken = JsonToken.START_OBJECT;
        parser.overrideCurrentName("parentName");
        assertEquals("parentName", parent.getCurrentName());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCurrentNameNull() throws IOException {
        parser._currToken = JsonToken.START_OBJECT;
        // parent context has no name set
        when(parser._parsingContext.getParent()).thenReturn(null);
        parser.getCurrentName();
    }

    @Test(expected = JsonParseException.class)
    public void testHandleEOFNotInRoot() throws Exception {
        // Make parser think it's not in root context
        parser._parsingContext = parser._parsingContext.createChildObjectContext(-1, -1);
        // We need to call _handleEOF through some method that triggers it.
        // Since it's protected, we can test via nextToken? But nextToken would need XML.
        // Instead, we can use reflection or make a subclass in test.
        // Simpler: call the protected method via a helper or using Whitebox.
        // We'll just declare a test subclass.
        TestableFromXmlParser testParser = new TestableFromXmlParser(ioContext, 0, 0, objectCodec, xmlStreamReader);
        testParser._parsingContext = testParser._parsingContext.createChildObjectContext(-1, -1);
        testParser.callHandleEOF();
    }

    private class TestableFromXmlParser extends FromXmlParser {
        public TestableFromXmlParser(IOContext ctxt, int genericParserFeatures, int xmlFeatures,
                ObjectCodec codec, XMLStreamReader xmlReader) {
            super(ctxt, genericParserFeatures, xmlFeatures, codec, xmlReader);
        }

        public void callHandleEOF() throws JsonParseException {
            _handleEOF();
        }
    }

    @Test
    public void testIsEmpty() {
        assertTrue(parser._isEmpty(null));
        assertTrue(parser._isEmpty(""));
        assertTrue(parser._isEmpty("   "));
        assertTrue(parser._isEmpty("\t\n "));
        assertFalse(parser._isEmpty("a"));
        assertFalse(parser._isEmpty(" a"));
    }

    @Test
    public void testGetByteArrayBuilder() {
        ByteArrayBuilder builder1 = parser._getByteArrayBuilder();
        assertNotNull(builder1);
        ByteArrayBuilder builder2 = parser._getByteArrayBuilder();
        assertSame(builder1, builder2);
    }

    @Test
    public void testReleaseBuffers() throws IOException {
        parser._releaseBuffers(); // should not throw
    }

    @Test
    public void testNextTokenWithNextTokenAlreadySet() throws IOException {
        parser._nextToken = JsonToken.START_OBJECT;
        parser._currToken = null;
        parser._parsingContext = parser._parsingContext.createChildObjectContext(-1, -1);
        XmlReadContext grandParent = parser._parsingContext.createChildObjectContext(-1, -1);
        parser._parsingContext = grandParent;
        JsonToken result = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, result);
        assertEquals(JsonToken.START_OBJECT, parser._currToken);
        assertNull(parser._nextToken);
        // Check that a new child context was created
        assertNotSame(grandParent, parser._parsingContext);
    }

    @Test
    public void testNextTokenWithNextTokenFieldName() throws IOException {
        parser._nextToken = JsonToken.FIELD_NAME;
        when(xmlStreamReader.getLocalName()).thenReturn("field");
        parser._currToken = null;
        JsonToken result = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, result);
        assertEquals("field", parser._parsingContext.getCurrentName());
    }

    @Test
    public void testNextTokenEndObject() throws IOException {
        parser._nextToken = JsonToken.END_OBJECT;
        parser._parsingContext = parser._parsingContext.createChildObjectContext(-1, -1);
        parser._currToken = null;
        Set<String> wraps = new HashSet<>();
        wraps.add("dummy");
        parser._namesToWrap = wraps;
        JsonToken result = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, result);
        // Should have gone up to parent, which has no namesToWrap
        assertNull(parser._namesToWrap);
    }

    @Test
    public void testNextTokenEndArray() throws IOException {
        parser._nextToken = JsonToken.END_ARRAY;
        parser._parsingContext = parser._parsingContext.createChildArrayContext(-1, -1);
        parser._currToken = null;
        JsonToken result = parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, result);
    }

    @Test
    public void testNextTokenStartArray() throws IOException {
        parser._nextToken = JsonToken.START_ARRAY;
        parser._parsingContext = parser._parsingContext.createChildObjectContext(-1, -1);
        parser._currToken = null;
        JsonToken result = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, result);
    }

    @Test
    public void testIsExpectedStartArrayToken() {
        parser._currToken = JsonToken.START_OBJECT;
        parser._parsingContext = parser._parsingContext.createChildObjectContext(-1, -1);
        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser._currToken);
        assertNull(parser._nextToken);

        // Now test with already array
        parser._currToken = JsonToken.START_ARRAY;
        assertTrue(parser.isExpectedStartArrayToken());

        // Test with other token
        parser._currToken = JsonToken.FIELD_NAME;
        assertFalse(parser.isExpectedStartArrayToken());
    }
}