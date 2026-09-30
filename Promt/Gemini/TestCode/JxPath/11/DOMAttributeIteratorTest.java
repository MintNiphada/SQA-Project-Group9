package org.apache.commons.jxpath.ri.model.dom;

import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class DOMAttributeIteratorTest {

    private Document document;
    private DocumentBuilder builder;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
        document = builder.newDocument();
    }

    @Test
    public void testNonElementNode() {
        Comment comment = document.createComment("test comment");
        DOMNodePointer pointer = new DOMNodePointer(comment, Locale.US, "id1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testTextNode() {
        Text text = document.createTextNode("sample text");
        DOMNodePointer pointer = new DOMNodePointer(text, Locale.US, "id2");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testSimpleAttributeByName() {
        Element element = document.createElement("testElement");
        element.setAttribute("name", "John");
        element.setAttribute("age", "30");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("name"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals(1, iterator.getPosition());
        NodePointer attrPointer = iterator.getNodePointer();
        Assert.assertNotNull(attrPointer);
        Assert.assertEquals("name", ((DOMAttributePointer) attrPointer).getName().getName());
        Assert.assertEquals("John", attrPointer.getValue());

        Assert.assertFalse(iterator.setPosition(2));
        Assert.assertFalse(iterator.setPosition(0));
        Assert.assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testNonExistentAttributeByName() {
        Element element = document.createElement("testElement");
        element.setAttribute("name", "John");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("unknown"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testWildcardAttributes() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");
        element.setAttribute("xmlns", "http://www.w3.org/2000/xmlns/");
        element.setAttribute("xmlns:test", "http://example.com/test");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertNotNull(iterator.getNodePointer());
        Assert.assertTrue(iterator.setPosition(2));
        Assert.assertNotNull(iterator.getNodePointer());
        Assert.assertFalse(iterator.setPosition(3));
    }

    @Test
    public void testGetNodePointerWhenPositionZero() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr1", "val1");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        Assert.assertEquals(0, iterator.getPosition());
        NodePointer np = iterator.getNodePointer();
        Assert.assertNotNull(np);
        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertEquals("val1", np.getValue());
    }

    @Test
    public void testGetNodePointerWhenPositionZeroAndEmpty() {
        Element element = document.createElement("testElement");
        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("attr1"));

        Assert.assertEquals(0, iterator.getPosition());
        NodePointer np = iterator.getNodePointer();
        Assert.assertNull(np);
    }

    @Test
    public void testNamespaceAttributeMatch() {
        String nsURI = "http://example.com/ns";
        Element element = document.createElementNS(nsURI, "foo:testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", nsURI);
        element.setAttributeNS(nsURI, "foo:targetAttr", "attrValue");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("foo", "targetAttr"));

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        Assert.assertNotNull(np);
        Assert.assertEquals("targetAttr", ((DOMAttributePointer) np).getName().getName());
        Assert.assertEquals("attrValue", np.getValue());
    }

    @Test
    public void testNamespaceAttributeWithWildcardPrefixMatch() {
        String nsURI = "http://example.com/ns";
        Element element = document.createElementNS(nsURI, "foo:testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", nsURI);
        element.setAttributeNS(nsURI, "foo:target1", "v1");
        element.setAttributeNS(nsURI, "foo:target2", "v2");
        element.setAttribute("plain", "v3");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("foo", "*"));

        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals("target1", ((DOMAttributePointer) iterator.getNodePointer()).getName().getName());
        Assert.assertTrue(iterator.setPosition(2));
        Assert.assertEquals("target2", ((DOMAttributePointer) iterator.getNodePointer()).getName().getName());
        Assert.assertFalse(iterator.setPosition(3));
    }

    @Test
    public void testNamespaceAttributeMismatchNamespace() {
        String nsURI1 = "http://example.com/ns1";
        String nsURI2 = "http://example.com/ns2";
        Element element = document.createElementNS(nsURI1, "foo:testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo", nsURI1);
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:bar", nsURI2);
        element.setAttributeNS(nsURI2, "bar:attr", "val");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("foo", "attr"));

        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testUnresolvablePrefix() {
        Element element = document.createElement("testElement");
        element.setAttribute("attr", "val");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("unknownPrefix", "attr"));

        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testNonNamespaceAwareDocumentAttributeMatching() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        Document nonNsDoc = factory.newDocumentBuilder().newDocument();

        Element element = nonNsDoc.createElement("testElement");
        element.setAttribute("test:attr", "val");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("test", "attr"));

        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testWildcardDifferentNamespacePrefixesResolvingToSameURI() {
        String nsURI = "http://example.com/common";
        Element element = document.createElementNS(nsURI, "ns1:testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns1", nsURI);
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns2", nsURI);
        element.setAttributeNS(nsURI, "ns2:myAttr", "value123");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("ns1", "*"));

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        Assert.assertNotNull(np);
        Assert.assertEquals("value123", np.getValue());
    }

    @Test
    public void testSpecificNameDifferentPrefixResolvingToSameURI() {
        String nsURI = "http://example.com/common";
        Element element = document.createElementNS(nsURI, "ns1:testElement");
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns1", nsURI);
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ns2", nsURI);
        element.setAttributeNS(nsURI, "ns2:myAttr", "value456");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("ns1", "myAttr"));

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer np = iterator.getNodePointer();
        Assert.assertNotNull(np);
        Assert.assertEquals("value456", np.getValue());
    }

    @Test
    public void testSetPositionBoundaries() {
        Element element = document.createElement("testElement");
        element.setAttribute("a1", "v1");
        element.setAttribute("a2", "v2");

        DOMNodePointer pointer = new DOMNodePointer(element, Locale.US, "elem1");
        DOMAttributeIterator iterator = new DOMAttributeIterator(pointer, new QName("*"));

        Assert.assertFalse(iterator.setPosition(-1));
        Assert.assertEquals(-1, iterator.getPosition());

        Assert.assertFalse(iterator.setPosition(0));
        Assert.assertEquals(0, iterator.getPosition());

        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals(1, iterator.getPosition());

        Assert.assertTrue(iterator.setPosition(2));
        Assert.assertEquals(2, iterator.getPosition());

        Assert.assertFalse(iterator.setPosition(3));
        Assert.assertEquals(3, iterator.getPosition());
    }
}
