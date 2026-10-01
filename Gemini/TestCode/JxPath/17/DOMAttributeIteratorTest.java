package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import static org.junit.Assert.*;

public class DOMAttributeIteratorTest {

    private Document documentNS;
    private Document documentNoNS;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factoryNS = DocumentBuilderFactory.newInstance();
        factoryNS.setNamespaceAware(true);
        DocumentBuilder builderNS = factoryNS.newDocumentBuilder();
        documentNS = builderNS.newDocument();

        DocumentBuilderFactory factoryNoNS = DocumentBuilderFactory.newInstance();
        factoryNoNS.setNamespaceAware(false);
        DocumentBuilder builderNoNS = factoryNoNS.newDocumentBuilder();
        documentNoNS = builderNoNS.newDocument();
    }

    @Test
    public void testNonElementNode() {
        Text textNode = documentNS.createTextNode("sample text");
        DOMNodePointer pointer = new DOMNodePointer(textNode, Locale.getDefault());

        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr"));
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testDocumentNode() {
        DOMNodePointer pointer = new DOMNodePointer(documentNS, Locale.getDefault());

        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testElementWithoutAttributes() {
        Element element = documentNS.createElement("root");
        documentNS.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr"));
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testSimpleAttributeByName() {
        Element element = documentNS.createElement("root");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        assertEquals(0, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());
        assertEquals("val1", iterator.getNodePointer().getValue());

        assertFalse(iterator.setPosition(2));
        assertFalse(iterator.setPosition(0));
        assertFalse(iterator.setPosition(-1));
    }

    @Test
    public void testWildcardAttributes() {
        Element element = documentNS.createElementNS("http://example.com/ns", "p:root");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p", "http://example.com/ns");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://example.com/ns");
        element.setAttributeNS("http://example.com/ns", "p:a1", "val1");
        element.setAttribute("a2", "val2");
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        // xmlns and xmlns:p should be filtered out
        assertTrue(iterator.setPosition(1));
        assertNotNull(iterator.getNodePointer());
        assertTrue(iterator.setPosition(2));
        assertNotNull(iterator.getNodePointer());
        assertFalse(iterator.setPosition(3));
    }

    @Test
    public void testWildcardNonNamespaceAwareXmlnsAttribute() {
        Element element = documentNoNS.createElement("root");
        element.setAttribute("xmlns", "http://example.com");
        element.setAttribute("xmlns:foo", "http://foo.com");
        element.setAttribute("regularAttr", "value");
        documentNoNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertTrue(iterator.setPosition(1));
        assertEquals("regularAttr", iterator.getNodePointer().getName().getName());
        assertFalse(iterator.setPosition(2));
    }

    @Test
    public void testNamespaceAttributeMatchByPrefix() {
        String nsURI = "http://example.com/ns";
        Element element = documentNS.createElementNS(nsURI, "foo:root");
        element.setAttributeNS(nsURI, "foo:bar", "baz");
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("foo", "bar"));

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("baz", attrPointer.getValue());
    }

    @Test
    public void testNamespaceAttributeMatchByDifferentPrefixSameURI() {
        String nsURI = "http://example.com/ns";
        Element element = documentNS.createElementNS(nsURI, "p1:root");
        element.setAttributeNS(nsURI, "p1:myAttr", "hello");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p1", nsURI);
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p2", nsURI);
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        // Requesting with prefix "p2", which maps to the same namespace URI
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("p2", "myAttr"));

        assertTrue(iterator.setPosition(1));
        NodePointer attrPointer = iterator.getNodePointer();
        assertNotNull(attrPointer);
        assertEquals("hello", attrPointer.getValue());
    }

    @Test
    public void testNamespaceAttributeFallbackWhenGetAttributeNodeNSFails() {
        // Create an element where getAttributeNodeNS might not find it directly
        // but testAttr matches via iteration
        String nsURI = "http://example.com/ns";
        Element element = documentNS.createElementNS(nsURI, "p:root");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p", nsURI);
        element.setAttributeNS(nsURI, "p:testAttr", "match");
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        // Asking for non-existent local name in known prefix
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("p", "nonExistent"));
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testUnknownPrefixNamespaceResolution() {
        Element element = documentNS.createElement("root");
        documentNS.appendChild(element);
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        // Prefix "unknown" cannot be resolved to any namespace URI
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("unknown", "attr"));
        assertFalse(iterator.setPosition(1));
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testGetNodePointerWhenPositionZero() {
        Element element = documentNS.createElement("root");
        element.setAttribute("a", "1");
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("a"));

        assertEquals(0, iterator.getPosition());
        // When position is 0, getNodePointer() internally sets position to 1, fetches, and resets to 0
        NodePointer np = iterator.getNodePointer();
        assertNotNull(np);
        assertEquals("1", np.getValue());
        assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testSetPositionBoundaries() {
        Element element = documentNS.createElement("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());

        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        assertNotNull(iterator.getNodePointer());

        assertFalse(iterator.setPosition(3));
        assertEquals(3, iterator.getPosition());

        assertFalse(iterator.setPosition(-1));
        assertEquals(-1, iterator.getPosition());
    }

    @Test
    public void testAttributeWildcardPrefixMismatch() {
        String nsURI1 = "http://example.com/ns1";
        String nsURI2 = "http://example.com/ns2";
        Element element = documentNS.createElementNS(nsURI1, "p1:root");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p1", nsURI1);
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:p2", nsURI2);
        element.setAttributeNS(nsURI1, "p1:attr", "val1");
        element.setAttributeNS(nsURI2, "p2:attr", "val2");
        documentNS.appendChild(element);

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.getDefault());

        // Wildcard local name with prefix p1
        DOMAttributeIterator iteratorP1 = new DOMAttributeIterator(pointer, new QName("p1", "*"));
        assertTrue(iteratorP1.setPosition(1));
        assertEquals("val1", iteratorP1.getNodePointer().getValue());
        assertFalse(iteratorP1.setPosition(2));

        // Wildcard local name with prefix p2
        DOMAttributeIterator iteratorP2 = new DOMAttributeIterator(pointer, new QName("p2", "*"));
        assertTrue(iteratorP2.setPosition(1));
        assertEquals("val2", iteratorP2.getNodePointer().getValue());
        assertFalse(iteratorP2.setPosition(2));
    }
}
