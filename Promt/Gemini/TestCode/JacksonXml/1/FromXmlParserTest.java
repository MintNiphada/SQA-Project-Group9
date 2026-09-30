package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.dataformat.xml.PackageVersion;

import static org.junit.Assert.*;

public class FromXmlParserTest {

    private final XMLInputFactory _xmlInputFactory = XMLInputFactory.newInstance();

    private FromXmlParser _createParser(String xml) throws Exception {
        return _createParser(xml, 0, 0, false);
    }

    private FromXmlParser _createParser(String xml, int genericFeatures, int xmlFeatures, boolean managedResource) throws Exception {
        IOContext ioContext = new IOContext(new BufferRecycler(), xml, managedResource);
        XMLStreamReader sr = _xmlInputFactory.createXMLStreamReader(new StringReader(xml));
        return new FromXmlParser(ioContext, genericFeatures, xmlFeatures, null, sr);
    }

    @Test
    public void testVersionAndCodec() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        Version v = parser.version();
        assertNotNull(v);
        assertEquals(PackageVersion.VERSION, v);

        assertTrue(parser.requiresCustomCodec());
        assertNull(parser.getCodec());

        parser.setCodec(null);
        assertNull(parser.getCodec());
        parser.close();
    }

    @Test
    public void testFeaturesAndConfig() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        assertEquals(0, parser.getFormatFeatures());

        int defaults = FromXmlParser.Feature.collectDefaults();
        assertEquals(0, defaults);

        parser.overrideFormatFeatures(0xFF, 0x0F);
        assertEquals(0x0F, parser.getFormatFeatures());

        parser.setXMLTextElementName("myText");
        parser.close();
    }

    @Test
    public void testGetStaxReader() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        assertNotNull(parser.getStaxReader());
        parser.close();
    }

    @Test
    public void testSimpleParsingAndTextAccessors() throws Exception {
        String xml = "<root><child>value</child></root>";
        FromXmlParser parser = _createParser(xml);

        assertFalse(parser.isClosed());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getEmbeddedObject());
        assertFalse(parser.hasTextCharacters());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());
        assertEquals("child", parser.getText());

        parser.overrideCurrentName("renamedChild");
        assertEquals("renamedChild", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals("value", parser.getValueAsString());
        assertEquals("value", parser.getValueAsString("default"));
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertNotNull(parser.getTextCharacters());
        assertEquals("value", new String(parser.getTextCharacters()));

        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
        loc = parser.getCurrentLocation();
        assertNotNull(loc);

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("renamedChild", parser.getCurrentName());

        assertNull(parser.nextToken());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testNumericAccessors() throws Exception {
        FromXmlParser parser = _createParser("<root>123</root>");
        assertNull(parser.getBigIntegerValue());
        assertNull(parser.getDecimalValue());
        assertEquals(0.0, parser.getDoubleValue(), 0.0001);
        assertEquals(0.0f, parser.getFloatValue(), 0.0001f);
        assertEquals(0, parser.getIntValue());
        assertEquals(0L, parser.getLongValue());
        assertNull(parser.getNumberType());
        assertNull(parser.getNumberValue());
        parser.close();
    }

    @Test
    public void testExpectedStartArrayToken() throws Exception {
        FromXmlParser parser = _createParser("<root><item>1</item><item>2</item></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertTrue(parser.isExpectedStartArrayToken());
        assertEquals(JsonToken.START_ARRAY, parser.currentToken());
        assertTrue(parser.isExpectedStartArrayToken());

        parser.close();
    }

    @Test
    public void testExpectedStartArrayTokenWhenNotStartObject() throws Exception {
        FromXmlParser parser = _createParser("<root><item>1</item></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());

        assertFalse(parser.isExpectedStartArrayToken());
        parser.close();
    }

    @Test
    public void testEmptyElementAndNullValue() throws Exception {
        String xml = "<root><empty/></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("empty", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testAttributesHandling() throws Exception {
        String xml = "<root id=\"123\" name=\"test\">Content</root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("id", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("123", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(FromXmlParser.DEFAULT_UNNAMED_TEXT_PROPERTY, parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Content", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTextValue() throws Exception {
        String xml = "<root><child>textValue</child><empty/></root>";
        FromXmlParser parser = _createParser(xml);

        assertNull(parser.nextTextValue());
        assertNull(parser.nextTextValue());
        String text = parser.nextTextValue();
        assertEquals("textValue", text);

        assertNull(parser.nextTextValue());
        String emptyText = parser.nextTextValue();
        assertEquals("", emptyText);

        parser.close();
    }

    @Test
    public void testNextTextValueWithAttributes() throws Exception {
        String xml = "<root attr=\"val\">hello</root>";
        FromXmlParser parser = _createParser(xml);

        assertNull(parser.nextTextValue());
        assertNull(parser.nextTextValue());
        assertNull(parser.nextTextValue());
        assertEquals("val", parser.nextTextValue());

        parser.close();
    }

    @Test
    public void testVirtualWrapping() throws Exception {
        String xml = "<root><items><item>A</item><item>B</item></items></root>";
        FromXmlParser parser = _createParser(xml);

        Set<String> wrapNames = new HashSet<String>();
        wrapNames.add("items");
        parser.addVirtualWrapping(wrapNames);

        assertNotNull(parser.nextToken());
        assertNotNull(parser.getParsingContext());
        parser.close();
    }

    @Test
    public void testBinaryValue() throws Exception {
        String base64 = "SGVsbG8gV29ybGQ=";
        String xml = "<root><data>" + base64 + "</data></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(decoded);
        assertEquals("Hello World", new String(decoded));

        byte[] decodedAgain = parser.getBinaryValue(Base64Variants.MIME);
        assertSame(decoded, decodedAgain);

        parser.close();
    }

    @Test
    public void testBinaryValueInvalidToken() throws Exception {
        String xml = "<root><child>value</child></root>";
        FromXmlParser parser = _createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        try {
            parser.getBinaryValue(Base64Variants.MIME);
            fail("Expected exception for non-value token");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("not VALUE_STRING"));
        }
        parser.close();
    }

    @Test
    public void testBinaryValueInvalidBase64() throws Exception {
        String xml = "<root><data>???notBase64???</data></root>";
        FromXmlParser parser = _createParser(xml);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        try {
            parser.getBinaryValue(Base64Variants.MIME);
            fail("Expected exception on corrupted base64 data");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Failed to decode VALUE_STRING as base64"));
        }
        parser.close();
    }

    @Test
    public void testCloseResourceManaged() throws Exception {
        FromXmlParser parser = _createParser("<root/>", 0, 0, true);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        parser.close();
    }

    @Test
    public void testCloseAutoCloseSource() throws Exception {
        FromXmlParser parser = _createParser("<root/>", JsonParser.Feature.AUTO_CLOSE_SOURCE.getMask(), 0, false);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testCloseUnmanaged() throws Exception {
        FromXmlParser parser = _createParser("<root/>", 0, 0, false);
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test
    public void testGetCurrentNameMissingNameException() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        try {
            parser.getCurrentName();
            fail("Expected IllegalStateException when token has no name yet");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Missing name"));
        }
        parser.close();
    }

    @Test
    public void testOverrideCurrentNameOnObject() throws Exception {
        FromXmlParser parser = _createParser("<root><child>val</child></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        parser.overrideCurrentName("overridden");
        parser.close();
    }

    @Test
    public void testGetTextWhenNullToken() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        assertNull(parser.getText());
        assertNull(parser.getTextCharacters());
        assertEquals(0, parser.getTextLength());
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));
        parser.close();
    }

    @Test
    public void testGetValueAsStringScalar() throws Exception {
        FromXmlParser parser = _createParser("<root><child/></root>");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("null", parser.getValueAsString());
        parser.close();
    }

    @Test
    public void testByteArrayBuilderHelper() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        ByteArrayBuilder b1 = parser._getByteArrayBuilder();
        assertNotNull(b1);
        ByteArrayBuilder b2 = parser._getByteArrayBuilder();
        assertSame(b1, b2);
        parser.close();
    }

    @Test
    public void testIsEmptyHelper() throws Exception {
        FromXmlParser parser = _createParser("<root/>");
        assertTrue(parser._isEmpty(null));
        assertTrue(parser._isEmpty(""));
        assertTrue(parser._isEmpty("   \t\r\n"));
        assertFalse(parser._isEmpty("abc"));
        assertFalse(parser._isEmpty("  a  "));
        parser.close();
    }

    @Test
    public void testHandleEOF() throws Exception {
        FromXmlParser parser = _createParser("<root><child>abc</child></root>");
        parser.nextToken();
        parser.nextToken();
        try {
            parser._handleEOF();
            fail("Expected JsonParseException for invalid EOF inside object");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("expected close marker"));
        }
        parser.close();
    }

    @Test
    public void testNestedElements() throws Exception {
        String xml = "<root><parent><leaf1>a</leaf1><leaf2>b</leaf2></parent></root>";
        FromXmlParser parser = _createParser(xml);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("parent", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("leaf1", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("leaf2", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("b", parser.getText());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }
}