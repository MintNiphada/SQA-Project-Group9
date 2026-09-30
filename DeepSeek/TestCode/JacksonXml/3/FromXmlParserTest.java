package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.ContentReference;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class FromXmlParserTest {

    private FromXmlParser createParser(String xml) throws Exception {
        XMLInputFactory f = XMLInputFactory.newInstance();
        XMLStreamReader sr = f.createXMLStreamReader(new StringReader(xml));
        IOContext ctxt = new IOContext(ContentReference.rawReference("dummy"), false);
        return new FromXmlParser(ctxt, 0, 0, null, sr);
    }

    @Test
    public void testInitialState() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertNotNull(parser);
        Assert.assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        Assert.assertFalse(parser.isClosed());
        Assert.assertNotNull(parser.getParsingContext());
        Assert.assertTrue(parser.requiresCustomCodec());
        Assert.assertNull(parser.getCodec());
        Assert.assertEquals(0, parser.getFormatFeatures());
    }

    @Test
    public void testSimpleText() throws Exception {
        FromXmlParser parser = createParser("<root>text</root>");
        // START_OBJECT
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME ""
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("", parser.getCurrentName());
        // VALUE_STRING "text"
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("text", parser.getText());
        // END_OBJECT
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // null (EOF)
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testEmptyElement() throws Exception {
        FromXmlParser parser = createParser("<root />");
        // START_OBJECT
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME "root"
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("root", parser.getCurrentName());
        // VALUE_NULL (empty leaf)
        Assert.assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        // END_OBJECT? Actually after VALUE_NULL, next token is null because stream ends.
        // But context is not popped; however, parser returns null.
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testAttributes() throws Exception {
        FromXmlParser parser = createParser("<root attr='val' />");
        // START_OBJECT
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME "root"
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("root", parser.getCurrentName());
        // START_OBJECT (due to attribute)
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME "attr"
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("attr", parser.getCurrentName());
        // VALUE_STRING "val"
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("val", parser.getText());
        // END_OBJECT (closing attribute wrapper)
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // END_OBJECT (closing root)
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testArrayHandling() throws Exception {
        FromXmlParser parser = createParser("<root><item>1</item><item>2</item></root>");
        // START_OBJECT
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME "root"
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // Now we expect an array: call isExpectedStartArrayToken()
        Assert.assertTrue(parser.isExpectedStartArrayToken());
        // Now current token should be START_ARRAY
        Assert.assertEquals(JsonToken.START_ARRAY, parser.getCurrentToken());
        // Next token: START_OBJECT for first item (since array element)
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME "item"
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("item", parser.getCurrentName());
        // VALUE_STRING "1"
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("1", parser.getText());
        // END_OBJECT for first item
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // START_OBJECT for second item
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME "item"
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // VALUE_STRING "2"
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("2", parser.getText());
        // END_OBJECT for second item
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // END_ARRAY
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        // END_OBJECT for root
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testVirtualWrapping() throws Exception {
        FromXmlParser parser = createParser("<root><item>1</item></root>");
        // advance to first FIELD_NAME
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // set virtual wrapping for "item"
        Set<String> wrapSet = new HashSet<String>();
        wrapSet.add("item");
        parser.addVirtualWrapping(wrapSet);
        // now we are at START_OBJECT? Actually after addVirtualWrapping, the token is still FIELD_NAME.
        // The method may repeat start element. We need to continue parsing.
        // The next token should be START_OBJECT (due to wrapping)
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // then FIELD_NAME "item" again
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        Assert.assertEquals("item", parser.getCurrentName());
        // then VALUE_STRING "1"
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        Assert.assertEquals("1", parser.getText());
        // END_OBJECT for inner wrapper
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // END_OBJECT for outer wrapper
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // END_OBJECT for root
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testEmptyTextInArray() throws Exception {
        FromXmlParser parser = createParser("<root><item></item></root>");
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // switch to array
        Assert.assertTrue(parser.isExpectedStartArrayToken());
        // next token should be START_OBJECT (empty object placeholder)
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // then END_OBJECT
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        // END_ARRAY
        Assert.assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        // END_OBJECT root
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testNextTextValue() throws Exception {
        FromXmlParser parser = createParser("<root>text</root>");
        // START_OBJECT
        Assert.assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        // FIELD_NAME
        Assert.assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // nextTextValue should return "text"
        String text = parser.nextTextValue();
        Assert.assertEquals("text", text);
        Assert.assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
        // next token should be END_OBJECT
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }

    @Test
    public void testGetValueAsString() throws Exception {
        FromXmlParser parser = createParser("<root>text</root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        Assert.assertEquals("text", parser.getValueAsString());
        Assert.assertEquals("text", parser.getValueAsString("default"));
        // test with null token
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // null
        Assert.assertNull(parser.getValueAsString());
        Assert.assertEquals("default", parser.getValueAsString("default"));
        parser.close();
    }

    @Test(expected = JsonParseException.class)
    public void testGetBinaryValueInvalid() throws Exception {
        FromXmlParser parser = createParser("<root>notbase64</root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        // should throw because not valid base64
        parser.getBinaryValue(Base64Variants.MIME);
    }

    @Test
    public void testClose() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
        // closing again should not throw
        parser.close();
    }

    @Test
    public void testOverrideCurrentName() throws Exception {
        FromXmlParser parser = createParser("<root>text</root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        Assert.assertEquals("", parser.getCurrentName());
        parser.overrideCurrentName("newName");
        Assert.assertEquals("newName", parser.getCurrentName());
        parser.close();
    }

    @Test
    public void testGetTextCharacters() throws Exception {
        FromXmlParser parser = createParser("<root>text</root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        char[] chars = parser.getTextCharacters();
        Assert.assertArrayEquals("text".toCharArray(), chars);
        Assert.assertEquals(4, parser.getTextLength());
        Assert.assertEquals(0, parser.getTextOffset());
        Assert.assertFalse(parser.hasTextCharacters());
        parser.close();
    }

    @Test
    public void testNumericAccessors() throws Exception {
        FromXmlParser parser = createParser("<root />");
        // all numeric methods return defaults
        Assert.assertNull(parser.getBigIntegerValue());
        Assert.assertNull(parser.getDecimalValue());
        Assert.assertEquals(0.0, parser.getDoubleValue(), 0.0);
        Assert.assertEquals(0.0f, parser.getFloatValue(), 0.0f);
        Assert.assertEquals(0, parser.getIntValue());
        Assert.assertEquals(0L, parser.getLongValue());
        Assert.assertNull(parser.getNumberType());
        Assert.assertNull(parser.getNumberValue());
        parser.close();
    }

    @Test
    public void testGetEmbeddedObject() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertNull(parser.getEmbeddedObject());
        parser.close();
    }

    @Test
    public void testRequiresCustomCodec() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertTrue(parser.requiresCustomCodec());
        parser.close();
    }

    @Test
    public void testSetCodec() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertNull(parser.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            // dummy implementation
        };
        parser.setCodec(codec);
        Assert.assertSame(codec, parser.getCodec());
        parser.close();
    }

    @Test
    public void testFormatFeatures() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertEquals(0, parser.getFormatFeatures());
        parser.overrideFormatFeatures(0x01, 0x01);
        Assert.assertEquals(0x01, parser.getFormatFeatures());
        parser.overrideFormatFeatures(0x00, 0x01);
        Assert.assertEquals(0x00, parser.getFormatFeatures());
        parser.close();
    }

    @Test
    public void testIsClosed() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertFalse(parser.isClosed());
        parser.close();
        Assert.assertTrue(parser.isClosed());
    }

    @Test
    public void testTokenLocation() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertNotNull(parser.getTokenLocation());
        Assert.assertNotNull(parser.getCurrentLocation());
        parser.close();
    }

    @Test
    public void testSetXMLTextElementName() throws Exception {
        FromXmlParser parser = createParser("<root>text</root>");
        parser.setXMLTextElementName("value");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        // the field name should now be "value" instead of ""
        Assert.assertEquals("value", parser.getCurrentName());
        parser.nextToken(); // VALUE_STRING
        Assert.assertEquals("text", parser.getText());
        parser.close();
    }

    @Test
    public void testVersion() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertNotNull(parser.version());
        parser.close();
    }

    @Test
    public void testGetStaxReader() throws Exception {
        FromXmlParser parser = createParser("<root />");
        Assert.assertNotNull(parser.getStaxReader());
        parser.close();
    }

    @Test
    public void testIsEmpty() throws Exception {
        // indirectly test _isEmpty via empty text handling
        FromXmlParser parser = createParser("<root>   </root>");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        // next token should be VALUE_STRING? Actually whitespace-only text is considered empty,
        // but in object context, it will be treated as text? Let's see: in nextToken, for XML_TEXT,
        // if not leaf and inObject, and _currToken != FIELD_NAME and _isEmpty true, it loops.
        // So it will skip the text and go to END_ELEMENT. So we get END_OBJECT directly.
        Assert.assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        Assert.assertNull(parser.nextToken());
        parser.close();
    }
}
