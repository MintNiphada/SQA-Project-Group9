package com.fasterxml.jackson.dataformat.xml.deser;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonLocation;

public class XmlTokenStreamTest {

    private XMLInputFactory _xmlInputFactory;

    @Before
    public void setUp() {
        _xmlInputFactory = XMLInputFactory.newInstance();
        // Configure standard StAX properties if supported
        try {
            _xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        } catch (Exception e) {
            // ignore if not supported
        }
    }

    private XmlTokenStream createStream(String xml) throws Exception {
        XMLStreamReader sr = _xmlInputFactory.createXMLStreamReader(new StringReader(xml));
        // Advance to START_ELEMENT
        while (sr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            sr.next();
        }
        return new XmlTokenStream(sr, xml);
    }

    @Test
    public void testConstructorWithInvalidState() throws Exception {
        XMLStreamReader sr = _xmlInputFactory.createXMLStreamReader(new StringReader("<root/>"));
        // sr is at START_DOCUMENT initially
        Assert.assertEquals(XMLStreamConstants.START_DOCUMENT, sr.getEventType());
        try {
            new XmlTokenStream(sr, "test");
            Assert.fail("Expected IllegalArgumentException when reader is not at START_ELEMENT");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("should be pointing to START_ELEMENT"));
        }
    }

    @Test
    public void testSimpleElementNoAttributes() throws Exception {
        XmlTokenStream stream = createStream("<root>Hello World</root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        Assert.assertEquals("root", stream.getLocalName());
        Assert.assertFalse(stream.hasAttributes());
        Assert.assertNotNull(stream.getXmlReader());

        // Next token: Text
        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);
        Assert.assertEquals("Hello World", stream.getText());
        Assert.assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());

        // Next token: End element
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
        Assert.assertEquals("root", stream.getLocalName());

        // Next token: End document
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END, token);

        // Calling next() again after end
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END, token);
    }

    @Test
    public void testAttributesIteration() throws Exception {
        XmlTokenStream stream = createStream("<root id=\"123\" type=\"test\">content</root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        Assert.assertTrue(stream.hasAttributes());

        // First attribute name
        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);
        Assert.assertEquals("id", stream.getLocalName());

        // First attribute value
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, token);
        Assert.assertEquals("123", stream.getText());

        // Second attribute name
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);
        Assert.assertEquals("type", stream.getLocalName());

        // Second attribute value
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, token);
        Assert.assertEquals("test", stream.getText());

        // Content
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);
        Assert.assertEquals("content", stream.getText());

        // End element
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        // End
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END, token);
    }

    @Test
    public void testEmptyElement() throws Exception {
        XmlTokenStream stream = createStream("<root/>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        Assert.assertFalse(stream.hasAttributes());

        // Next is END_ELEMENT directly (no text, empty tag)
        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END, token);
    }

    @Test
    public void testNestedElements() throws Exception {
        XmlTokenStream stream = createStream("<root><child1>abc</child1><child2 attr=\"val\"/></root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        Assert.assertEquals("root", stream.getLocalName());

        // child1 start
        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        Assert.assertEquals("child1", stream.getLocalName());

        // child1 text
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);
        Assert.assertEquals("abc", stream.getText());

        // child1 end
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);
        Assert.assertEquals("child1", stream.getLocalName());

        // child2 start
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        Assert.assertEquals("child2", stream.getLocalName());
        Assert.assertTrue(stream.hasAttributes());

        // child2 attr name
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);
        Assert.assertEquals("attr", stream.getLocalName());

        // child2 attr val
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, token);
        Assert.assertEquals("val", stream.getText());

        // child2 end
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        // root end
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        // End of stream
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END, token);
    }

    @Test
    public void testCommentsAndCData() throws Exception {
        XmlTokenStream stream = createStream("<root><!-- a comment --><![CDATA[cdata text]]></root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);
        Assert.assertEquals("cdata text", stream.getText());

        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_END, token);
    }

    @Test
    public void testSkipEndElement() throws Exception {
        XmlTokenStream stream = createStream("<root><child/></root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        int token = stream.next(); // child START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);

        stream.skipEndElement(); // skips child END_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.getCurrentToken());
        Assert.assertEquals("child", stream.getLocalName());

        // Calling skipEndElement when next is not END_ELEMENT
        try {
            // Next is root END_ELEMENT, but let's read it first
            stream.skipEndElement(); // should skip root END_ELEMENT
            Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.getCurrentToken());
            Assert.assertEquals("root", stream.getLocalName());

            // Now next is XML_END, so skipEndElement should throw IOException
            stream.skipEndElement();
            Assert.fail("Expected IOException when next event is not END_ELEMENT");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Expected END_ELEMENT"));
        }
    }

    @Test
    public void testSkipAttributesWhenAtAttributeName() throws Exception {
        XmlTokenStream stream = createStream("<root id=\"123\" name=\"test\">body</root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        int token = stream.next(); // At attribute name "id"
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);

        stream.skipAttributes();
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        Assert.assertFalse(stream.hasAttributes());

        // Next should directly read the text body
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);
        Assert.assertEquals("body", stream.getText());
    }

    @Test
    public void testSkipAttributesInOtherStates() throws Exception {
        XmlTokenStream stream = createStream("<root>body</root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        // Call skipAttributes when at START_ELEMENT
        stream.skipAttributes();
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        int token = stream.next(); // TEXT
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);

        // Call skipAttributes when at TEXT
        stream.skipAttributes();
        Assert.assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());

        token = stream.next(); // END_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        // Call skipAttributes when at END_ELEMENT (invalid state)
        try {
            stream.skipAttributes();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Current state not XML_START_ELEMENT or XML_ATTRIBUTE_NAME"));
        }
    }

    @Test
    public void testConvertToStringSuccessWithEmptyTag() throws Exception {
        XmlTokenStream stream = createStream("<root attr=\"val\"/>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);

        String converted = stream.convertToString();
        Assert.assertEquals("", converted);
        Assert.assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());
        Assert.assertEquals("", stream.getText());
        Assert.assertEquals("root", stream.getLocalName());
    }

    @Test
    public void testConvertToStringSuccessWithText() throws Exception {
        XmlTokenStream stream = createStream("<root attr=\"val\">hello text</root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);

        String converted = stream.convertToString();
        Assert.assertEquals("hello text", converted);
        Assert.assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());
        Assert.assertEquals("hello text", stream.getText());
    }

    @Test
    public void testConvertToStringInvalidStates() throws Exception {
        XmlTokenStream stream = createStream("<root><child/></root>");
        // At START_ELEMENT
        Assert.assertNull(stream.convertToString());

        stream.next(); // child START_ELEMENT
        Assert.assertNull(stream.convertToString());
    }

    @Test
    public void testConvertToStringWithNestedTagsReturnsNull() throws Exception {
        XmlTokenStream stream = createStream("<root attr=\"val\"><child/></root>");
        int token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, token);

        String converted = stream.convertToString();
        Assert.assertNull(converted);
    }

    @Test
    public void testRepeatStartElementBasic() throws Exception {
        XmlTokenStream stream = createStream("<items><item>1</item><item>2</item></items>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());
        Assert.assertEquals("items", stream.getLocalName());

        int token = stream.next(); // item START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        Assert.assertEquals("item", stream.getLocalName());

        // Repeat start element
        stream.repeatStartElement();

        token = stream.next(); // Replayed START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);

        token = stream.next(); // text "1"
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);
        Assert.assertEquals("1", stream.getText());

        token = stream.next(); // END_ELEMENT item
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        token = stream.next(); // Second START_ELEMENT item
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        Assert.assertEquals("item", stream.getLocalName());
    }

    @Test
    public void testRepeatStartElementInvalidState() throws Exception {
        XmlTokenStream stream = createStream("<root>text</root>");
        stream.next(); // TEXT
        Assert.assertEquals(XmlTokenStream.XML_TEXT, stream.getCurrentToken());
        try {
            stream.repeatStartElement();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Current state not XML_START_ELEMENT"));
        }
    }

    @Test
    public void testVirtualWrappingWithDelayedStart() throws Exception {
        XmlTokenStream stream = createStream("<root><unwrapped>val</unwrapped><other>val2</other></root>");
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        int token = stream.next(); // unwrapped START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        Assert.assertEquals("unwrapped", stream.getLocalName());

        // Virtual wrapper wrapping
        stream.repeatStartElement();

        token = stream.next(); // Replayed unwrapped START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);

        token = stream.next(); // text val
        Assert.assertEquals(XmlTokenStream.XML_TEXT, token);

        token = stream.next(); // end unwrapped
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        token = stream.next(); // other START_ELEMENT (triggers implicit end + delayed start)
        Assert.assertEquals(XmlTokenStream.XML_END_ELEMENT, token);

        token = stream.next(); // delayed other START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
        Assert.assertEquals("other", stream.getLocalName());
    }

    @Test
    public void testMultipleRepeatStartElementCalls() throws Exception {
        XmlTokenStream stream = createStream("<root><item>text</item></root>");
        stream.next(); // item START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, stream.getCurrentToken());

        stream.repeatStartElement();
        int token = stream.next(); // replayed START_ELEMENT
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);

        // Repeat again on intermediate wrapper
        stream.repeatStartElement();
        token = stream.next();
        Assert.assertEquals(XmlTokenStream.XML_START_ELEMENT, token);
    }

    @Test
    public void testLocations() throws Exception {
        String xml = "<root>\n  <child attr=\"val\">test</child>\n</root>";
        XmlTokenStream stream = createStream(xml);

        JsonLocation curLoc = stream.getCurrentLocation();
        JsonLocation tokLoc = stream.getTokenLocation();

        Assert.assertNotNull(curLoc);
        Assert.assertNotNull(tokLoc);
        Assert.assertEquals(xml, curLoc.getSourceRef());
        Assert.assertEquals(xml, tokLoc.getSourceRef());
    }

    @Test
    public void testCloseAndCloseCompletely() throws Exception {
        XmlTokenStream stream = createStream("<root>text</root>");
        stream.close();

        XmlTokenStream stream2 = createStream("<root>text</root>");
        stream2.closeCompletely();
    }

    @Test
    public void testToStringAndNamespace() throws Exception {
        XmlTokenStream stream = createStream("<ns:root xmlns:ns=\"http://example.com\" id=\"1\">test</ns:root>");
        Assert.assertEquals("root", stream.getLocalName());
        Assert.assertEquals("http://example.com", stream.getNamespaceURI());

        String str = stream.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("Token stream:"));
        Assert.assertTrue(str.contains("name=root"));
    }
}
