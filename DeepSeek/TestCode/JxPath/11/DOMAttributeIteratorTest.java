package org.apache.commons.jxpath.ri.model.dom;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodeIterator;

public class DOMAttributeIteratorTest {
    private DocumentBuilder builder;

    private DocumentBuilder getBuilder() throws ParserConfigurationException {
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
        }
        return builder;
    }

    private Document newDocument() throws ParserConfigurationException {
        return getBuilder().newDocument();
    }

    private DOMNodePointer createPointer(Node node) {
        return new DOMNodePointer(node, Locale.getDefault(), null);
    }

    @Test
    public void testConstructorNonElementNode() throws Exception {
        Document doc = newDocument();
        Node docNode = doc; // document node is not element
        DOMNodePointer pointer = createPointer(docNode);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        assertNull(iterator.getNodePointer());
        assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testConstructorNoAttributes() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        assertNull(iterator.getNodePointer());
        assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testConstructorSpecificNameNoMatch() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("attr1", "value1");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr2"));
        assertNull(iterator.getNodePointer());
        assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testConstructorSpecificNameMatch() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("attr1", "value1");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));
        assertNotNull(iterator.getNodePointer());
        assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testConstructorWildcardWithAttributes() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        element.setAttributeNS("http://example.com/ns", "c", "3");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        assertNotNull(iterator.getNodePointer());
        // position is 0 after constructor, but getNodePointer sets to 1 then back to 0
        assertEquals(0, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        Attr attr1 = (Attr) iterator.getNodePointer().getImmediateNode();
        assertEquals("a", attr1.getName());
        assertTrue(iterator.setPosition(2));
        Attr attr2 = (Attr) iterator.getNodePointer().getImmediateNode();
        assertEquals("b", attr2.getName());
        assertTrue(iterator.setPosition(3));
        Attr attr3 = (Attr) iterator.getNodePointer().getImmediateNode();
        assertEquals("c", attr3.getLocalName());
        assertFalse(iterator.setPosition(4));
    }

    @Test
    public void testConstructorWildcardExcludesXmlns() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:pre", "http://example.com/ns");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://default.ns");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        assertTrue(iterator.setPosition(1));
        Attr attr = (Attr) iterator.getNodePointer().getImmediateNode();
        assertEquals("a", attr.getName());
        assertFalse(iterator.setPosition(2));
    }

    @Test
    public void testConstructorWithPrefixNamespaceMatch() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttributeNS("http://example.com/ns", "pre:attr", "value");
        DOMNodePointer pointer = createPointer(element);
        // Query with same prefix and local name
        DOMAttributeIterator iterator1 = new DOMAttributeIterator(pointer, new QName("pre:attr"));
        assertNotNull(iterator1.getNodePointer());
        Attr attr1 = (Attr) iterator1.getNodePointer().getImmediateNode();
        assertEquals("attr", attr1.getLocalName());
        assertEquals("pre", attr1.getPrefix());

        // Query with prefix but different prefix pointing to same namespace
        // The element must have namespace declaration for another prefix
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:pre2", "http://example.com/ns");
        DOMAttributeIterator iterator2 = new DOMAttributeIterator(pointer, new QName("pre2:attr"));
        assertNotNull(iterator2.getNodePointer());
        Attr attr2 = (Attr) iterator2.getNodePointer().getImmediateNode();
        assertEquals("attr", attr2.getLocalName());
        assertEquals("pre", attr2.getPrefix());
    }

    @Test
    public void testConstructorWithPrefixNamespaceNoMatch() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttributeNS("http://example.com/ns1", "pre:attr", "value");
        DOMNodePointer pointer = createPointer(element);
        // different namespace with same prefix
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("other:attr"));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testGetNodePointerEmpty() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testGetNodePointerNonEmptyInitialPosition() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        NodePointer attrPtr = iterator.getNodePointer();
        assertNotNull(attrPtr);
        Attr attr = (Attr) attrPtr.getImmediateNode();
        assertEquals("a", attr.getName());
        assertEquals(0, iterator.getPosition()); // position remains 0 after getNodePointer
    }

    @Test
    public void testSetPositionValidInvalid() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        Attr attr1 = (Attr) iterator.getNodePointer().getImmediateNode();
        assertEquals("a", attr1.getName());

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        Attr attr2 = (Attr) iterator.getNodePointer().getImmediateNode();
        assertEquals("b", attr2.getName());

        assertFalse(iterator.setPosition(0));
        assertFalse(iterator.setPosition(3));
        // position unchanged after invalid set
        assertEquals(2, iterator.getPosition());
    }

    @Test
    public void testGetNodePointerAfterSetPosition() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        element.setAttribute("c", "3");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        iterator.setPosition(2);
        Attr attr = (Attr) iterator.getNodePointer().getImmediateNode();
        assertEquals("b", attr.getName());
        assertEquals(2, iterator.getPosition());
    }

    @Test
    public void testGetNodePointerPositionClamping() throws Exception {
        Document doc = newDocument();
        Element element = doc.createElement("root");
        element.setAttribute("a", "1");
        DOMNodePointer pointer = createPointer(element);
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        // position = 0, setPosition(1) internally, then position reset to 0
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals(0, iterator.getPosition());
        // Now set position to negative, then getNodePointer should clamp to 0 and return first
        iterator.setPosition(-1);
        ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        Attr attr = (Attr) ptr.getImmediateNode();
        assertEquals("a", attr.getName());
        assertEquals(-1, iterator.getPosition()); // position remains -1 after get? Actually get sets only if position==0, so no change
    }
}
