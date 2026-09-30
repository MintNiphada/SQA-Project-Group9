package com.fasterxml.jackson.dataformat.xml.deser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamLocation2;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonLocation;

@RunWith(MockitoJUnitRunner.class)
public class XmlTokenStreamTest {

    @Mock
    private XMLStreamReader2 mockReader;

    @Mock
    private XMLStreamLocation2 mockLocation;

    private XmlTokenStream tokenStream;

    @Before
    public void setUp() throws Exception {
        // Common setup for constructor: point to START_ELEMENT
        when(mockReader.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
        when(mockReader.getLocalName()).thenReturn("root");
        when(mockReader.getNamespaceURI()).thenReturn("http://ns");
        when(mockReader.getAttributeCount()).thenReturn(0);
        when(mockReader.getLocationInfo()).thenReturn(mock(XMLStreamLocation2.class));
        tokenStream = new XmlTokenStream(mockReader, new Object());
    }

    // Helper to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = XmlTokenStream.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field field = XmlTokenStream.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = XmlTokenStream.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    // ---------- Constructor tests ----------

    @Test
    public void testConstructorValidStartElement() throws Exception {
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
        assertEquals("root", tokenStream.getLocalName());
        assertEquals("http://ns", tokenStream.getNamespaceURI());
        assertFalse(tokenStream.hasAttributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalidEventType() throws Exception {
        XMLStreamReader2 badReader = mock(XMLStreamReader2.class);
        when(badReader.getEventType()).thenReturn(XMLStreamConstants.END_ELEMENT);
        new XmlTokenStream(badReader, new Object());
    }

    // ---------- next() basic flow ----------

    @Test
    public void testNextStartElementWithAttributes() throws Exception {
        // Reconfigure mock for this test
        XMLStreamReader2 reader = mock(XMLStreamReader2.class);
        when(reader.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
        when(reader.getLocalName()).thenReturn("elem");
        when(reader.getNamespaceURI()).thenReturn("ns");
        when(reader.getAttributeCount()).thenReturn(2);
        when(reader.getAttributeLocalName(0)).thenReturn("attr1");
        when(reader.getAttributeNamespace(0)).thenReturn("ns1");
        when(reader.getAttributeValue(0)).thenReturn("val1");
        when(reader.getAttributeLocalName(1)).thenReturn("attr2");
        when(reader.getAttributeNamespace(1)).thenReturn("ns2");
        when(reader.getAttributeValue(1)).thenReturn("val2");
        when(reader.getLocationInfo()).thenReturn(mock(XMLStreamLocation2.class));

        XmlTokenStream stream = new XmlTokenStream(reader, new Object());

        // First next: should return XML_ATTRIBUTE_NAME for first attribute
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        assertEquals("attr1", stream.getLocalName());
        assertEquals("ns1", stream.getNamespaceURI());
        assertEquals("val1", stream.getText());

        // Second next: XML_ATTRIBUTE_VALUE
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, stream.next());
        assertEquals("val1", stream.getText());

        // Third next: second attribute name
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, stream.next());
        assertEquals("attr2", stream.getLocalName());
        assertEquals("ns2", stream.getNamespaceURI());
        assertEquals("val2", stream.getText());

        // Fourth next: second attribute value
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_VALUE, stream.next());
        assertEquals("val2", stream.getText());

        // After attributes, _collectUntilTag will be called; we need to simulate next events
        // We'll mock reader.next() to return END_ELEMENT after attributes
        when(reader.next()).thenReturn(XMLStreamConstants.END_ELEMENT);
        // _collectUntilTag will return null because no text
        // Then _handleEndElement will be called
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    @Test
    public void testNextTextAndEndElement() throws Exception {
        XMLStreamReader2 reader = mock(XMLStreamReader2.class);
        when(reader.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
        when(reader.getLocalName()).thenReturn("elem");
        when(reader.getNamespaceURI()).thenReturn("ns");
        when(reader.getAttributeCount()).thenReturn(0);
        when(reader.getLocationInfo()).thenReturn(mock(XMLStreamLocation2.class));

        XmlTokenStream stream = new XmlTokenStream(reader, new Object());

        // Simulate _collectUntilTag: first next returns CHARACTERS, then END_ELEMENT
        when(reader.next()).thenReturn(XMLStreamConstants.CHARACTERS, XMLStreamConstants.END_ELEMENT);
        when(reader.getText()).thenReturn("some text");

        // First next after start element: should return XML_TEXT
        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("some text", stream.getText());

        // Next call: should return XML_END_ELEMENT
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());
    }

    @Test
    public void testNextEndDocument() throws Exception {
        XMLStreamReader2 reader = mock(XMLStreamReader2.class);
        when(reader.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
        when(reader.getLocalName()).thenReturn("elem");
        when(reader.getNamespaceURI()).thenReturn("ns");
        when(reader.getAttributeCount()).thenReturn(0);
        when(reader.getLocationInfo()).thenReturn(mock(XMLStreamLocation2.class));

        XmlTokenStream stream = new XmlTokenStream(reader, new Object());

        // After start element, _collectUntilTag returns null and next event is END_ELEMENT
        when(reader.next()).thenReturn(XMLStreamConstants.END_ELEMENT);
        assertEquals(XmlTokenStream.XML_END_ELEMENT, stream.next());

        // Now state is XML_END_ELEMENT, next call will go to _skipUntilTag
        // Simulate _skipUntilTag returning END_DOCUMENT
        when(reader.hasNext()).thenReturn(true);
        when(reader.next()).thenReturn(XMLStreamConstants.END_DOCUMENT);
        assertEquals(XmlTokenStream.XML_END, stream.next());
    }

    // ---------- repeatElement handling ----------

    @Test
    public void testRepeatStartDup() throws Exception {
        // Set up repeat element
        setField(tokenStream, "_repeatElement", 1); // REPLAY_START_DUP
        setField(tokenStream, "_currentWrapper", mock(ElementWrapper.class));
        ElementWrapper wrapper = (ElementWrapper) getField(tokenStream, "_currentWrapper");
        when(wrapper.intermediateWrapper()).thenReturn(mock(ElementWrapper.class));

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals(0, getField(tokenStream, "_repeatElement"));
    }

    @Test
    public void testRepeatEnd() throws Exception {
        setField(tokenStream, "_repeatElement", 2); // REPLAY_END
        when(mockReader.getLocalName()).thenReturn("endElem");
        when(mockReader.getNamespaceURI()).thenReturn("endNs");

        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
        assertEquals("endElem", tokenStream.getLocalName());
        assertEquals("endNs", tokenStream.getNamespaceURI());
    }

    @Test
    public void testRepeatStartDelayed() throws Exception {
        setField(tokenStream, "_repeatElement", 3); // REPLAY_START_DELAYED
        setField(tokenStream, "_nextLocalName", "delayed");
        setField(tokenStream, "_nextNamespaceURI", "delayedNs");

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
        assertEquals("delayed", tokenStream.getLocalName());
        assertEquals("delayedNs", tokenStream.getNamespaceURI());
        assertNull(getField(tokenStream, "_nextLocalName"));
        assertNull(getField(tokenStream, "_nextNamespaceURI"));
    }

    @Test(expected = IllegalStateException.class)
    public void testRepeatUnrecognizedType() throws Exception {
        setField(tokenStream, "_repeatElement", 99);
        tokenStream.next();
    }

    // ---------- skipEndElement ----------

    @Test
    public void testSkipEndElementSuccess() throws Exception {
        // We need to mock next() to return XML_END_ELEMENT
        // We'll use a spy or partial mock? Instead, we can set up the internal state so that next() returns END_ELEMENT.
        // Simpler: create a new stream with a reader that will produce END_ELEMENT on next.
        XMLStreamReader2 reader = mock(XMLStreamReader2.class);
        when(reader.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
        when(reader.getLocalName()).thenReturn("e");
        when(reader.getNamespaceURI()).thenReturn("ns");
        when(reader.getAttributeCount()).thenReturn(0);
        when(reader.getLocationInfo()).thenReturn(mock(XMLStreamLocation2.class));
        XmlTokenStream stream = new XmlTokenStream(reader, new Object());

        // After start, next will call _collectUntilTag; we mock to return END_ELEMENT immediately
        when(reader.next()).thenReturn(XMLStreamConstants.END_ELEMENT);
        // So first next returns END_ELEMENT
        stream.skipEndElement(); // should not throw
    }

    @Test(expected = IOException.class)
    public void testSkipEndElementFailure() throws Exception {
        XMLStreamReader2 reader = mock(XMLStreamReader2.class);
        when(reader.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
        when(reader.getLocalName()).thenReturn("e");
        when(reader.getNamespaceURI()).thenReturn("ns");
        when(reader.getAttributeCount()).thenReturn(0);
        when(reader.getLocationInfo()).thenReturn(mock(XMLStreamLocation2.class));
        XmlTokenStream stream = new XmlTokenStream(reader, new Object());

        // Make next return something else (e.g., START_ELEMENT)
        when(reader.next()).thenReturn(XMLStreamConstants.START_ELEMENT);
        stream.skipEndElement();
    }

    // ---------- getters ----------

    @Test
    public void testGetCurrentToken() {
        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.getCurrentToken());
    }

    @Test
    public void testGetText() throws Exception {
        setField(tokenStream, "_textValue", "hello");
        assertEquals("hello", tokenStream.getText());
    }

    @Test
    public void testGetLocalName() {
        assertEquals("root", tokenStream.getLocalName());
    }

    @Test
    public void testGetNamespaceURI() {
        assertEquals("http://ns", tokenStream.getNamespaceURI());
    }

    @Test
    public void testHasAttributesTrue() throws Exception {
        // Set state to START_ELEMENT and attributeCount > 0
        setField(tokenStream, "_currentState", XmlTokenStream.XML_START_ELEMENT);
        setField(tokenStream, "_attributeCount", 2);
        assertTrue(tokenStream.hasAttributes());
    }

    @Test
    public void testHasAttributesFalse() {
        // default state START_ELEMENT with 0 attributes
        assertFalse(tokenStream.hasAttributes());
    }

    // ---------- close methods ----------

    @Test
    public void testCloseCompletely() throws Exception {
        tokenStream.closeCompletely();
        verify(mockReader).closeCompletely();
    }

    @Test(expected = IOException.class)
    public void testCloseCompletelyThrowsXMLStreamException() throws Exception {
        doThrow(new XMLStreamException("test")).when(mockReader).closeCompletely();
        tokenStream.closeCompletely();
    }

    @Test
    public void testClose() throws Exception {
        tokenStream.close();
        verify(mockReader).close();
    }

    @Test(expected = IOException.class)
    public void testCloseThrowsXMLStreamException() throws Exception {
        doThrow(new XMLStreamException("test")).when(mockReader).close();
        tokenStream.close();
    }

    // ---------- location methods ----------

    @Test
    public void testGetCurrentLocation() throws Exception {
        XMLStreamLocation2 loc = mock(XMLStreamLocation2.class);
        when(loc.getCharacterOffset()).thenReturn(100L);
        when(loc.getLineNumber()).thenReturn(5);
        when(loc.getColumnNumber()).thenReturn(10);
        when(mockReader.getLocationInfo().getCurrentLocation()).thenReturn(loc);

        JsonLocation jsonLoc = tokenStream.getCurrentLocation();
        assertEquals(100L, jsonLoc.getCharOffset());
        assertEquals(5, jsonLoc.getLineNr());
        assertEquals(10, jsonLoc.getColumnNr());
    }

    @Test
    public void testGetCurrentLocationNull() throws Exception {
        when(mockReader.getLocationInfo().getCurrentLocation()).thenReturn(null);
        JsonLocation jsonLoc = tokenStream.getCurrentLocation();
        assertEquals(-1L, jsonLoc.getCharOffset());
        assertEquals(-1, jsonLoc.getLineNr());
        assertEquals(-1, jsonLoc.getColumnNr());
    }

    @Test
    public void testGetTokenLocation() throws Exception {
        XMLStreamLocation2 loc = mock(XMLStreamLocation2.class);
        when(loc.getCharacterOffset()).thenReturn(200L);
        when(loc.getLineNumber()).thenReturn(7);
        when(loc.getColumnNumber()).thenReturn(15);
        when(mockReader.getLocationInfo().getStartLocation()).thenReturn(loc);

        JsonLocation jsonLoc = tokenStream.getTokenLocation();
        assertEquals(200L, jsonLoc.getCharOffset());
        assertEquals(7, jsonLoc.getLineNr());
        assertEquals(15, jsonLoc.getColumnNr());
    }

    // ---------- repeatStartElement ----------

    @Test
    public void testRepeatStartElementValid() throws Exception {
        // Ensure current state is XML_START_ELEMENT
        setField(tokenStream, "_currentState", XmlTokenStream.XML_START_ELEMENT);
        tokenStream.repeatStartElement();
        assertEquals(1, getField(tokenStream, "_repeatElement")); // REPLAY_START_DUP
        assertNotNull(getField(tokenStream, "_currentWrapper"));
    }

    @Test(expected = IllegalStateException.class)
    public void testRepeatStartElementInvalidState() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_END_ELEMENT);
        tokenStream.repeatStartElement();
    }

    // ---------- skipAttributes ----------

    @Test
    public void testSkipAttributesInAttributeNameState() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_ATTRIBUTE_NAME);
        setField(tokenStream, "_attributeCount", 5);
        tokenStream.skipAttributes();
        assertEquals(0, getField(tokenStream, "_attributeCount"));
        assertEquals(XmlTokenStream.XML_START_ELEMENT, getField(tokenStream, "_currentState"));
    }

    @Test
    public void testSkipAttributesInStartElementState() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_START_ELEMENT);
        setField(tokenStream, "_attributeCount", 3);
        tokenStream.skipAttributes();
        // According to comment, should do nothing
        assertEquals(3, getField(tokenStream, "_attributeCount"));
        assertEquals(XmlTokenStream.XML_START_ELEMENT, getField(tokenStream, "_currentState"));
    }

    @Test
    public void testSkipAttributesInTextState() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_TEXT);
        tokenStream.skipAttributes(); // should not throw
    }

    @Test(expected = IllegalStateException.class)
    public void testSkipAttributesInvalidState() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_END);
        tokenStream.skipAttributes();
    }

    // ---------- convertToString ----------

    @Test
    public void testConvertToStringApplicable() throws Exception {
        // Set state to XML_ATTRIBUTE_NAME and _nextAttributeIndex=0
        setField(tokenStream, "_currentState", XmlTokenStream.XML_ATTRIBUTE_NAME);
        setField(tokenStream, "_nextAttributeIndex", 0);
        // Mock reader to simulate _collectUntilTag: first CHARACTERS, then END_ELEMENT
        when(mockReader.next()).thenReturn(XMLStreamConstants.CHARACTERS, XMLStreamConstants.END_ELEMENT);
        when(mockReader.getText()).thenReturn("converted");
        when(mockReader.getEventType()).thenReturn(XMLStreamConstants.END_ELEMENT); // after collect
        when(mockReader.getLocalName()).thenReturn("elem");
        when(mockReader.getNamespaceURI()).thenReturn("ns");

        String result = tokenStream.convertToString();
        assertEquals("converted", result);
        assertEquals(XmlTokenStream.XML_TEXT, getField(tokenStream, "_currentState"));
        assertEquals("converted", tokenStream.getText());
        assertEquals("elem", tokenStream.getLocalName());
        assertEquals("ns", tokenStream.getNamespaceURI());
    }

    @Test
    public void testConvertToStringEmptyTag() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_ATTRIBUTE_NAME);
        setField(tokenStream, "_nextAttributeIndex", 0);
        // _collectUntilTag returns null (no text), then END_ELEMENT
        when(mockReader.next()).thenReturn(XMLStreamConstants.END_ELEMENT);
        when(mockReader.getEventType()).thenReturn(XMLStreamConstants.END_ELEMENT);
        when(mockReader.getLocalName()).thenReturn("elem");
        when(mockReader.getNamespaceURI()).thenReturn("ns");

        String result = tokenStream.convertToString();
        assertEquals("", result);
        assertEquals(XmlTokenStream.XML_TEXT, getField(tokenStream, "_currentState"));
    }

    @Test
    public void testConvertToStringNotApplicable() throws Exception {
        // Not in XML_ATTRIBUTE_NAME state
        setField(tokenStream, "_currentState", XmlTokenStream.XML_START_ELEMENT);
        assertNull(tokenStream.convertToString());

        // In XML_ATTRIBUTE_NAME but _nextAttributeIndex != 0
        setField(tokenStream, "_currentState", XmlTokenStream.XML_ATTRIBUTE_NAME);
        setField(tokenStream, "_nextAttributeIndex", 1);
        assertNull(tokenStream.convertToString());
    }

    // ---------- _next() internal branches ----------

    @Test
    public void testNextFromAttributeValue() throws Exception {
        // Set state to XML_ATTRIBUTE_VALUE, _nextAttributeIndex < _attributeCount
        setField(tokenStream, "_currentState", XmlTokenStream.XML_ATTRIBUTE_VALUE);
        setField(tokenStream, "_nextAttributeIndex", 0);
        setField(tokenStream, "_attributeCount", 1);
        when(mockReader.getAttributeLocalName(0)).thenReturn("attr");
        when(mockReader.getAttributeNamespace(0)).thenReturn("attrNs");
        when(mockReader.getAttributeValue(0)).thenReturn("attrVal");

        // next() should increment index and return XML_ATTRIBUTE_NAME
        assertEquals(XmlTokenStream.XML_ATTRIBUTE_NAME, tokenStream.next());
        assertEquals(1, getField(tokenStream, "_nextAttributeIndex"));
    }

    @Test
    public void testNextFromStartElementWithTextAndEndElement() throws Exception {
        // Simulate _collectUntilTag returning text and then END_ELEMENT
        setField(tokenStream, "_currentState", XmlTokenStream.XML_START_ELEMENT);
        setField(tokenStream, "_nextAttributeIndex", 0);
        setField(tokenStream, "_attributeCount", 0);
        when(mockReader.next()).thenReturn(XMLStreamConstants.CHARACTERS, XMLStreamConstants.END_ELEMENT);
        when(mockReader.getText()).thenReturn("text");

        assertEquals(XmlTokenStream.XML_TEXT, tokenStream.next());
        assertEquals("text", tokenStream.getText());
    }

    @Test
    public void testNextFromTextToEndElement() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_TEXT);
        // _handleEndElement will be called
        assertEquals(XmlTokenStream.XML_END_ELEMENT, tokenStream.next());
    }

    @Test
    public void testNextFromEndState() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_END);
        assertEquals(XmlTokenStream.XML_END, tokenStream.next());
    }

    // ---------- _collectUntilTag and _skipUntilTag via next ----------

    @Test
    public void testCollectUntilTagWithMultipleTextSegments() throws Exception {
        XMLStreamReader2 reader = mock(XMLStreamReader2.class);
        when(reader.getEventType()).thenReturn(XMLStreamConstants.START_ELEMENT);
        when(reader.getLocalName()).thenReturn("e");
        when(reader.getNamespaceURI()).thenReturn("ns");
        when(reader.getAttributeCount()).thenReturn(0);
        when(reader.getLocationInfo()).thenReturn(mock(XMLStreamLocation2.class));
        XmlTokenStream stream = new XmlTokenStream(reader, new Object());

        // Simulate _collectUntilTag: CHARACTERS, CDATA, then END_ELEMENT
        when(reader.next()).thenReturn(
                XMLStreamConstants.CHARACTERS,
                XMLStreamConstants.CDATA,
                XMLStreamConstants.END_ELEMENT);
        when(reader.getText()).thenReturn("part1", "part2");

        assertEquals(XmlTokenStream.XML_TEXT, stream.next());
        assertEquals("part1part2", stream.getText());
    }

    @Test
    public void testSkipUntilTagReturnsStartElement() throws Exception {
        // After END_ELEMENT, next() calls _skipUntilTag
        setField(tokenStream, "_currentState", XmlTokenStream.XML_END_ELEMENT);
        when(mockReader.hasNext()).thenReturn(true);
        when(mockReader.next()).thenReturn(XMLStreamConstants.START_ELEMENT);
        // Then _initStartElement will be called; we need to mock its requirements
        when(mockReader.getLocalName()).thenReturn("child");
        when(mockReader.getNamespaceURI()).thenReturn("childNs");
        when(mockReader.getAttributeCount()).thenReturn(0);

        assertEquals(XmlTokenStream.XML_START_ELEMENT, tokenStream.next());
    }

    @Test(expected = IllegalStateException.class)
    public void testSkipUntilTagNoMoreInput() throws Exception {
        setField(tokenStream, "_currentState", XmlTokenStream.XML_END_ELEMENT);
        when(mockReader.hasNext()).thenReturn(false);
        tokenStream.next();
    }

    // ---------- _initStartElement with wrapper ----------

    @Test
    public void testInitStartElementWrapperMatching() throws Exception {
        ElementWrapper wrapper = mock(ElementWrapper.class);
        when(wrapper.matchesWrapper("child", "childNs")).thenReturn(true);
        when(wrapper.intermediateWrapper()).thenReturn(mock(ElementWrapper.class));
        setField(tokenStream, "_currentWrapper", wrapper);

        // Simulate reader returning matching element
        when(mockReader.getLocalName()).thenReturn("child");
        when(mockReader.getNamespaceURI()).thenReturn("childNs");
        when(mockReader.getAttributeCount()).thenReturn(0);

        // Call _initStartElement via next() after setting state to something that triggers it
        // We'll directly invoke private method for clarity
        int result = (Integer) invokePrivate(tokenStream, "_initStartElement", new Class[0]);
        assertEquals(XmlTokenStream.XML_START_ELEMENT, result);
        assertEquals("child", tokenStream.getLocalName());
    }

    @Test
    public void testInitStartElementWrapperNotMatching() throws Exception {
        ElementWrapper wrapper = mock(ElementWrapper.class);
        when(wrapper.matchesWrapper("other", "otherNs")).thenReturn(false);
        when(wrapper.getWrapperLocalName()).thenReturn("wrapperLocal");
        when(wrapper.getWrapperNamespace()).thenReturn("wrapperNs");
        when(wrapper.getParent()).thenReturn(null);
        setField(tokenStream, "_currentWrapper", wrapper);

        when(mockReader.getLocalName()).thenReturn("other");
        when(mockReader.getNamespaceURI()).thenReturn("otherNs");
        when(mockReader.getAttributeCount()).thenReturn(0);

        int result = (Integer) invokePrivate(tokenStream, "_initStartElement", new Class[0]);
        assertEquals(XmlTokenStream.XML_END_ELEMENT, result);
        assertEquals("wrapperLocal", tokenStream.getLocalName());
        assertEquals("wrapperNs", tokenStream.getNamespaceURI());
        assertEquals(3, getField(tokenStream, "_repeatElement")); // REPLAY_START_DELAYED
        assertEquals("other", getField(tokenStream, "_nextLocalName"));
        assertEquals("otherNs", getField(tokenStream, "_nextNamespaceURI"));
    }

    // ---------- _handleEndElement with wrapper ----------

    @Test
    public void testHandleEndElementWrapperMatching() throws Exception {
        ElementWrapper wrapper = mock(ElementWrapper.class);
        when(wrapper.isMatching()).thenReturn(true);
        when(wrapper.getWrapperLocalName()).thenReturn("wrapLocal");
        when(wrapper.getWrapperNamespace()).thenReturn("wrapNs");
        when(wrapper.getParent()).thenReturn(null);
        setField(tokenStream, "_currentWrapper", wrapper);

        int result = (Integer) invokePrivate(tokenStream, "_handleEndElement", new Class[0]);
        assertEquals(XmlTokenStream.XML_END_ELEMENT, result);
        assertEquals(2, getField(tokenStream, "_repeatElement")); // REPLAY_END
        assertEquals("wrapLocal", tokenStream.getLocalName());
        assertEquals("wrapNs", tokenStream.getNamespaceURI());
        assertNull(getField(tokenStream, "_currentWrapper"));
    }

    @Test
    public void testHandleEndElementWrapperNotMatching() throws Exception {
        ElementWrapper wrapper = mock(ElementWrapper.class);
        when(wrapper.isMatching()).thenReturn(false);
        when(wrapper.getParent()).thenReturn(null);
        setField(tokenStream, "_currentWrapper", wrapper);

        int result = (Integer) invokePrivate(tokenStream, "_handleEndElement", new Class[0]);
        assertEquals(XmlTokenStream.XML_END_ELEMENT, result);
        assertNull(getField(tokenStream, "_currentWrapper"));
    }

    // ---------- toString ----------

    @Test
    public void testToString() {
        String str = tokenStream.toString();
        assertTrue(str.contains("state="));
        assertTrue(str.contains("name=root"));
    }
}
