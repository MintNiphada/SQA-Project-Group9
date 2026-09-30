package org.apache.commons.jxpath.ri.model.jdom;

import java.util.Locale;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Assert;
import org.junit.Test;

public class JDOMAttributeIteratorTest {

    @Test
    public void testNonElementParent() {
        Document doc = new Document();
        NodePointer parent = NodePointer.newNodePointer(new QName("doc"), doc, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("test"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testSpecificAttributeNoNamespace() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("attr1"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals(1, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertTrue(ptr instanceof JDOMAttributePointer);
        Assert.assertEquals("val1", ptr.getValue());

        Assert.assertFalse(iterator.setPosition(2));
        Assert.assertFalse(iterator.setPosition(0));
    }

    @Test
    public void testNonExistentAttributeNoNamespace() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("nonExistent"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testWildcardNoNamespace() {
        Element element = new Element("root");
        Namespace ns = Namespace.getNamespace("custom", "http://custom");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");
        element.setAttribute("attr3", "val3", ns);

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals("val1", iterator.getNodePointer().getValue());

        Assert.assertTrue(iterator.setPosition(2));
        Assert.assertEquals("val2", iterator.getNodePointer().getValue());

        Assert.assertFalse(iterator.setPosition(3));
    }

    @Test
    public void testXmlNamespacePrefix() {
        Element element = new Element("root");
        element.setAttribute("lang", "en", Namespace.XML_NAMESPACE);

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("xml", "lang"));

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertEquals("en", ptr.getValue());

        JDOMAttributeIterator wildIterator = new JDOMAttributeIterator(parent, new QName("xml", "*"));
        Assert.assertTrue(wildIterator.setPosition(1));
        Assert.assertEquals("en", wildIterator.getNodePointer().getValue());
        Assert.assertFalse(wildIterator.setPosition(2));
    }

    @Test
    public void testCustomNamespaceDefined() {
        Namespace ns = Namespace.getNamespace("custom", "http://example.com/ns");
        Element element = new Element("root");
        element.addNamespaceDeclaration(ns);
        element.setAttribute("myAttr", "customVal", ns);

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("custom", "myAttr"));

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertEquals("customVal", ptr.getValue());

        JDOMAttributeIterator wildIterator = new JDOMAttributeIterator(parent, new QName("custom", "*"));
        Assert.assertTrue(wildIterator.setPosition(1));
        Assert.assertEquals("customVal", wildIterator.getNodePointer().getValue());
        Assert.assertFalse(wildIterator.setPosition(2));
    }

    @Test
    public void testCustomNamespaceNotDefined() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("unknownPrefix", "myAttr"));

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testGetNodePointerWhenPositionZeroWithAttributes() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("attr1"));

        Assert.assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertEquals("val1", ptr.getValue());
        Assert.assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testGetNodePointerWhenPositionZeroWithoutAttributes() {
        Element element = new Element("root");

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("attr1"));

        Assert.assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNull(ptr);
        Assert.assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testSetPositionBoundaries() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");
        element.setAttribute("attr2", "val2");

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new提示(parent);

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

    private JDOMAttributeIterator new提示(NodePointer parent) {
        return new JDOMAttributeIterator(parent, new QName("*"));
    }

    @Test
    public void testGetNodePointerWithExplicitPosition() {
        Element element = new Element("root");
        element.setAttribute("a", "1");
        element.setAttribute("b", "2");

        NodePointer parent = NodePointer.newNodePointer(new QName("root"), element, Locale.getDefault());
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, new QName("*"));

        iterator.setPosition(2);
        NodePointer ptr2 = iterator.getNodePointer();
        Assert.assertNotNull(ptr2);
        Assert.assertEquals("2", ptr2.getValue());

        iterator.setPosition(1);
        NodePointer ptr1 = iterator.getNodePointer();
        Assert.assertNotNull(ptr1);
        Assert.assertEquals("1", ptr1.getValue());
    }
}
