package org.apache.commons.jxpath.ri.model.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@RunWith(MockitoJUnitRunner.class)
public class DOMAttributeIteratorTest {

    @Mock
    private NodePointer parent;

    private Document document;
    private Element element;

    @Before
    public void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.newDocument();
        element = document.createElement("root");
        when(parent.getNode()).thenReturn(element);
    }

    @Test
    public void testConstructorNonElementNode() {
        Node textNode = mock(Node.class);
        when(textNode.getNodeType()).thenReturn(Node.TEXT_NODE);
        when(parent.getNode()).thenReturn(textNode);

        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("attr"));
        assertEquals(0, iterator.getPosition());
        assertNull(iterator.getNodePointer());
    }

    @Test
    public void testConstructorAttributeFoundNoNamespace() {
        element.setAttribute("myattr", "value");
        QName name = new QName("myattr");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        List<Attr> attrs = getAttributes(iterator);
        assertEquals(1, attrs.size());
        Attr attr = attrs.get(0);
        assertEquals("myattr", attr.getNodeName());
    }

    @Test
    public void testConstructorAttributeNotFoundNoNamespace() {
        QName name = new QName("missing");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        assertEquals(0, getAttributes(iterator).size());
    }

    @Test
    public void testConstructorAttributeWithNamespaceFallback() throws Exception {
        element.setAttribute("pre:local", "value");

        NamespaceResolver nsResolver = mock(NamespaceResolver.class);
        when(parent.getNamespaceResolver()).thenReturn(nsResolver);
        when(nsResolver.getNamespaceURI("pre")).thenReturn("http://example.com/someNS");
        when(parent.getNamespaceURI("pre")).thenReturn("http://example.com/someNS");

        QName name = new QName("pre", "local");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        List<Attr> attrs = getAttributes(iterator);
        assertEquals(1, attrs.size());
        Attr attr = attrs.get(0);
        assertEquals("pre:local", attr.getNodeName());
    }

    @Test
    public void testConstructorStarAttribute() {
        element.setAttribute("attr1", "val1");
        element.setAttributeNS("http://example.com/ns", "ns:attr2", "val2");
        element.setAttribute("xmlns", "http://default");
        element.setAttribute("xmlns:pre", "http://example.com/pre");

        QName name = new QName(null, "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        List<Attr> attrs = getAttributes(iterator);

        assertEquals(1, attrs.size());
        assertEquals("attr1", attrs.get(0).getNodeName());
    }

    @Test
    public void testConstructorStarWithPrefix() {
        element.setAttribute("attr1", "val1");
        element.setAttribute("pre:attr2", "val2");
        when(parent.getNamespaceURI("pre")).thenReturn("http://example.com/pre");

        QName name = new QName("pre", "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        List<Attr> attrs = getAttributes(iterator);
        assertEquals(1, attrs.size());
        assertEquals("pre:attr2", attrs.get(0).getNodeName());
    }

    @Test
    public void testTestAttrXmlnsExclusion() {
        element.setAttribute("xmlns", "http://default");
        element.setAttribute("xmlns:pre", "http://example.com/pre");
        QName name = new QName(null, "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        List<Attr> attrs = getAttributes(iterator);
        assertEquals(0, attrs.size());
    }

    @Test
    public void testGetNodePointerWithZeroPositionAndEmpty() {
        Node textNode = mock(Node.class);
        when(textNode.getNodeType()).thenReturn(Node.TEXT_NODE);
        when(parent.getNode()).thenReturn(textNode);
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("attr"));
        assertNull(iterator.getNodePointer());
        assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testGetNodePointerWithZeroPositionAndNonEmpty() {
        element.setAttribute("attr", "val");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, new QName("attr"));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        assertEquals(0, iterator.getPosition());
        NodePointer ptr2 = iterator.getNodePointer();
        assertNotNull(ptr2);
    }

    @Test
    public void testGetNodePointerWithNonZeroPosition() {
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");
        QName name = new QName(null, "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        assertTrue(iterator.setPosition(2));
        NodePointer ptr = iterator.getNodePointer();
        assertNotNull(ptr);
        DOMAttributePointer attrPtr = (DOMAttributePointer) ptr;
        assertEquals("attr2", ((Attr) attrPtr.getImmediateNode()).getNodeName());
    }

    @Test
    public void testSetPosition() {
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");
        QName name = new QName(null, "*");
        DOMAttributeIterator iterator = new DOMAttributeIterator(parent, name);
        assertEquals(0, iterator.getPosition());
        assertTrue(iterator.setPosition(1));
        assertEquals(1, iterator.getPosition());
        assertTrue(iterator.setPosition(2));
        assertEquals(2, iterator.getPosition());
        assertFalse(iterator.setPosition(0));
        assertEquals(0, iterator.getPosition());
        assertFalse(iterator.setPosition(3));
        assertEquals(3, iterator.getPosition());
    }

    private List<Attr> getAttributes(DOMAttributeIterator iterator) {
        try {
        java.lang.reflect.Field field = DOMAttributeIterator.class.getDeclaredField("attributes");
            field.setAccessible(true);
            return (List<Attr>) field.get(iterator);
        }        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
