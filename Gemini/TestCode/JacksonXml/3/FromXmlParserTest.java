package com.fasterxml.jackson.dataformat.xml.deser;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FromXmlParserTest {

    private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newFactory();

    private FromXmlParser createParser(String xml) throws Exception {
        return createParser(xml, false, 0);
    }

    private FromXmlParser createParser(String xml, boolean managedResource, int xmlFeatures) throws Exception {
        IOContext ioContext = new IOContext(new BufferRecycler(), new StringReader(xml), managedResource);
        XMLStreamReader xmlReader = XML_FACTORY.createXMLStreamReader(new StringReader(xml));
        XmlMapper mapper = new XmlMapper();
        return new FromXmlParser(ioContext, 0, xmlFeatures, mapper, xmlReader);
    }

    @Test
    public void testFeatureEnum() {
        int defaults = FromXmlParser.Feature.collectDefaults();
        Assert.assertEquals(0, defaults);
        for (FromXmlParser.Feature f : FromXmlParser.Feature.values()) {
            Assert.assertFalse(f.enabledByDefault());
            Assert.assertTrue(f.getMask() > 0);
            Assert.assertTrue(f.enabledIn(f.getMask()));
            Assert.assertFalse(f.enabledIn(0));
        }
    }

    @Test
    public void testBasicConfigurationAndCodec() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        Assert.assertNotNull(parser.version());
        Assert.assertEquals(FromXmlParser.class.getPackage().getName(), parser.version().getGroupId());
        Assert.assertTrue(parser.requiresCustomCodec());

        ObjectCodec codec = parser.getCodec();
        Assert.assertNotNull(codec);
        parser.setCodec(null);
        Assert.assertNull(parser.getCodec());
        parser.setCodec(codec);
        Assert.assertSame(codec, parser.getCodec());

        Assert.assertNotNull(parser.getStaxReader());
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
        // Duplicate close call should be safe
        parser.close();
    }

    @Test
    public void testFormatFeaturesOverride() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        Assert.assertEquals(0, parser.getFormatFeatures());

        parser.overrideFormatFeatures(0xFF, 0x0F);
        Assert.assertEquals(0x0F, parser.getFormatFeatures());

        parser.overrideFormatFeatures(0x00, 0x01);
        Assert.assertEquals(0x0E, parser.getFormatFeatures());
    }

    @Test
    public void testSimpleElementParsing() throws Exception {
        FromXmlParser parser = createParser("<root>Hello</root>");
        Assert.assertFalse(parser.hasTextCharacters());
        Assert.assertEquals(0, parser.getTextOffset());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertNotNull(parser.getParsingContext());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("root", parser.getCurrentName());
        Assert.assertEquals("root", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("Hello", parser.getText());
        Assert.assertArrayEquals("Hello".toCharArray(), parser.getTextCharacters());
        Assert.assertEquals(5, parser.getTextLength());
        Assert.assertEquals("Hello", parser.getValueAsString());
        Assert.assertEquals("Hello", parser.getValueAsString("default"));

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        Assert.assertNull(parser.getText());
        Assert.assertNull(parser.getTextCharacters());
        Assert.assertEquals(0, parser.getTextLength());
        Assert.assertNull(parser.getValueAsString());
        parser.close();
    }

    @Test
    public void testAttributesAndMixedContent() throws Exception {
        String xml = "<root attr=\"val\">Inner</root>";
        FromXmlParser parser = createParser(xml);
        parser.setXMLTextElementName("textValue");

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("attr", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("val", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("textValue", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("Inner", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testAttributesOnLeafElement() throws Exception {
        String xml = "<root><child id=\"1\"/></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("child", parser.getCurrentName());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("id", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("1", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testNestedElements() throws Exception {
        String xml = "<root><a>1</a><b>2</b></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("a", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("1", parser.getText());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("b", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("2", parser.getText());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testEmptyElement() throws Exception {
        String xml = "<root><empty/></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("empty", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testExpectedStartArray() throws Exception {
        String xml = "<root><item>A</item><item>B</item></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertTrue(parser.isExpectedStartArrayToken());
        Assert.assertEquals(JsonToken.START_ARRAY, parser.currentToken());
        Assert.assertTrue(parser.isExpectedStartArrayToken());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("A", parser.getText());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("B", parser.getText());

        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testExpectedStartArrayEmptyObject() throws Exception {
        String xml = "<root/>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertTrue(parser.isExpectedStartArrayToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testArrayWithEmptyLeafElement() throws Exception {
        String xml = "<root><item/></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertTrue(parser.isExpectedStartArrayToken());

        // In array, an empty leaf <item/> is converted to empty Object: START_OBJECT then END_OBJECT
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testArrayWithWhitespaceText() throws Exception {
        String xml = "<root><item>   </item></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertTrue(parser.isExpectedStartArrayToken());

        // White space in array leaf also exposes as empty object
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testVirtualWrapping() throws Exception {
        String xml = "<root><items>1</items><items>2</items></root>";
        FromXmlParser parser = createParser(xml);

        Set<String> wrapNames = new HashSet<String>();
        wrapNames.add("items");
        parser.addVirtualWrapping(wrapNames);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("items", parser.getCurrentName());

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("items", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("1", parser.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("items", parser.getCurrentName());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("items", parser.getCurrentName());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("2", parser.getText());
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testVirtualWrappingWithCurrentLocalName() throws Exception {
        String xml = "<root><items>value</items></root>";
        FromXmlParser parser = createParser(xml);
        // Advance into the stream to have a local name
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "items"
        parser.addVirtualWrapping(Collections.singleton("items"));
        parser.close();
    }

    @Test
    public void testNextTextValueVariants() throws Exception {
        String xml = "<root><a>test</a><b attr=\"1\">wrapped</b><empty/></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertNull(parser.nextTextValue()); // START_OBJECT
        Assert.assertNull(parser.nextTextValue()); // FIELD_NAME a
        Assert.assertEquals("test", parser.nextTextValue()); // VALUE_STRING "test"

        Assert.assertNull(parser.nextTextValue()); // FIELD_NAME b
        Assert.assertNull(parser.nextTextValue()); // START_OBJECT for attr
        Assert.assertNull(parser.nextTextValue()); // FIELD_NAME attr
        Assert.assertEquals("1", parser.nextTextValue()); // VALUE_STRING "1"
        Assert.assertNull(parser.nextTextValue()); // FIELD_NAME "" for wrapped text
        Assert.assertEquals("wrapped", parser.nextTextValue()); // VALUE_STRING "wrapped"
        Assert.assertNull(parser.nextTextValue()); // END_OBJECT for b

        Assert.assertNull(parser.nextTextValue()); // FIELD_NAME empty
        Assert.assertEquals("", parser.nextTextValue()); // Empty element produces "" in nextTextValue
        Assert.assertNull(parser.nextTextValue()); // END_OBJECT
        Assert.assertNull(parser.nextTextValue()); // END (null)
        parser.close();
    }

    @Test
    public void testNextTextValueWithBufferedToken() throws Exception {
        String xml = "<root><a>text</a></root>";
        FromXmlParser parser = createParser(xml);

        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // nextToken() will read VALUE_STRING and return it via nextTextValue
        Assert.assertEquals("text", parser.nextTextValue());
        parser.close();
    }

    @Test
    public void testNextTextValueInArray() throws Exception {
        String xml = "<root><item>1</item><item>2</item></root>";
        FromXmlParser parser = createParser(xml);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertTrue(parser.isExpectedStartArrayToken());

        Assert.assertEquals("1", parser.nextTextValue());
        Assert.assertEquals("2", parser.nextTextValue());
        Assert.assertNull(parser.nextTextValue());
        parser.close();
    }

    @Test
    public void testOverrideCurrentName() throws Exception {
        FromXmlParser parser = createParser("<root><elem>val</elem></root>");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("elem", parser.getCurrentName());

        parser.overrideCurrentName("renamedElem");
        Assert.assertEquals("renamedElem", parser.getCurrentName());

        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("renamedElem", parser.getCurrentName());

        parser.close();
    }

    @Test
    public void testOverrideCurrentNameOnStartObject() throws Exception {
        FromXmlParser parser = createParser("<root><child><inner>val</inner></child></root>");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        parser.overrideCurrentName("renamedChild");
        Assert.assertEquals("renamedChild", parser.getCurrentName());
        parser.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetCurrentNameMissingThrows() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        parser.getCurrentName();
    }

    @Test
    public void testLocations() throws Exception {
        FromXmlParser parser = createParser("<root>data</root>");
        Assert.assertNotNull(parser.getTokenLocation());
        Assert.assertNotNull(parser.getCurrentLocation());
        parser.nextToken();
        Assert.assertNotNull(parser.getTokenLocation());
        Assert.assertNotNull(parser.getCurrentLocation());
        parser.close();
    }

    @Test
    public void testBinaryValueValid() throws Exception {
        // "SGVsbG8gV29ybGQ=" is base64 for "Hello World"
        FromXmlParser parser = createParser("<root>SGVsbG8gV29ybGQ=</root>");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertNotNull(binary);
        Assert.assertEquals("Hello World", new String(binary, "UTF-8"));

        // Calling a second time should reuse cached binary value
        byte[] binary2 = parser.getBinaryValue(Base64Variants.MIME);
        Assert.assertSame(binary, binary2);
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testBinaryValueInvalidBase64() throws Exception {
        FromXmlParser parser = createParser("<root>Not Valid Base64 !!!</root>");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
    }

    @Test(expected = JsonParseException.class)
    public void testBinaryValueWrongToken() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME);
    }

    @Test
    public void testNumericAccessorsReturnDefaults() throws Exception {
        FromXmlParser parser = createParser("<root>123</root>");
        Assert.assertEquals(0, parser.getIntValue());
        Assert.assertEquals(0L, parser.getLongValue());
        Assert.assertEquals(0.0, parser.getDoubleValue(), 0.00001);
        Assert.assertEquals(0.0f, parser.getFloatValue(), 0.00001f);
        Assert.assertNull(parser.getBigIntegerValue());
        Assert.assertNull(parser.getDecimalValue());
        Assert.assertNull(parser.getNumberValue());
        Assert.assertNull(parser.getNumberType());
        Assert.assertNull(parser.getEmbeddedObject());
        parser.close();
    }

    @Test
    public void testEOFHandling() throws Exception {
        FromXmlParser parser = createParser("<root><a>test</a></root>");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        // Now context is inside object, _handleEOF should fail
        try {
            parser._handleEOF();
            Assert.fail("Expected JsonParseException on unclosed EOF");
        } catch (JsonParseException jpe) {
            Assert.assertTrue(jpe.getMessage().contains("expected close marker"));
        }
        parser.close();
    }

    @Test
    public void testEOFHandlingInRoot() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        while (parser.nextToken() != null) {
            // consume all
        }
        // At root context, calling _handleEOF() should not throw
        parser._handleEOF();
        parser.close();
    }

    @Test
    public void testAutoCloseManagedResource() throws Exception {
        FromXmlParser parser = createParser("<root/>", true, 0);
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testAutoCloseFeatureEnabled() throws Exception {
        FromXmlParser parser = createParser("<root/>", false, 0);
        parser.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testIsEmptyHelper() throws Exception {
        FromXmlParser parser = createParser("<root/>");
        Assert.assertTrue(parser._isEmpty(null));
        Assert.assertTrue(parser._isEmpty(""));
        Assert.assertTrue(parser._isEmpty("   \t\r\n"));
        Assert.assertFalse(parser._isEmpty("  a  "));
        Assert.assertFalse(parser._isEmpty("abc"));
        parser.close();
    }

    @Test
    public void testConvertToStringOnStartObject() throws Exception {
        String xml = "<root><![CDATA[Some text]]></root>";
        FromXmlParser parser = createParser(xml);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // For element with text, getValueAsString returns value
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("Some text", parser.getValueAsString());
        parser.close();
    }
}
