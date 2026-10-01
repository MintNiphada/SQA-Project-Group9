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
    public void testNonElementNode() {
        Document doc = new Document();
        NodePointer parent = new JDOMNodePointer(doc, Locale.getDefault());
        QName name = new QName("test");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testSpecificAttributeNoNamespaceFound() {
        Element element = new Element("root");
        element.setAttribute("attr1", "value1");
        element.setAttribute("attr2", "value2");

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals(1, iterator.getPosition());

        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertTrue(ptr instanceof JDOMAttributePointer);
        Assert.assertEquals("value1", ptr.getValue());

        Assert.assertFalse(iterator.setPosition(2));
        Assert.assertEquals(2, iterator.getPosition());
    }

    @Test
    public void testSpecificAttributeNotFound() {
        Element element = new Element("root");
        element.setAttribute("attr1", "value1");

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("nonexistent");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testXmlNamespacePrefix() {
        Element element = new Element("root");
        Attribute xmlAttr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(xmlAttr);

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("xml", "lang");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertEquals("en", ptr.getValue());
    }

    @Test
    public void testXmlNamespaceWildcard() {
        Element element = new Element("root");
        Attribute xmlAttr = new Attribute("lang", "en", Namespace.XML_NAMESPACE);
        element.setAttribute(xmlAttr);
        element.setAttribute(new Attribute("other", "val"));

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("xml", "*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertEquals("en", ptr.getValue());
        Assert.assertFalse(iterator.setPosition(2));
    }

    @Test
    public void testCustomNamespaceResolved() {
        Namespace customNs = Namespace.getNamespace("custom", "http://commons.apache.org/custom");
        Element element = new Element("root");
        element.addNamespaceDeclaration(customNs);
        Attribute attr = new Attribute("customAttr", "customVal", customNs);
        element.setAttribute(attr);

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        parent.getNamespaceResolver().registerNamespace("custom", "http://commons.apache.org/custom");

        QName name = new QName("custom", "customAttr");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertTrue(iterator.setPosition(1));
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertEquals("customVal", ptr.getValue());
    }

    @Test
    public void testCustomNamespaceUnknown() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("unknownPrefix", "attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertEquals(0, iterator.getPosition());
        Assert.assertFalse(iterator.setPosition(1));
        Assert.assertNull(iterator.getNodePointer());
    }

    @Test
    public void testWildcardWithoutNamespace() {
        Element element = new Element("root");
        element.setAttribute("a1", "v1");
        element.setAttribute("a2", "v2");
        Namespace customNs = Namespace.getNamespace("c", "http://example.com");
        element.setAttribute(new Attribute("a3", "v3", customNs));

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("*");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals("v1", iterator.getNodePointer().getValue());

        Assert.assertTrue(iterator.setPosition(2));
        Assert.assertEquals("v2", iterator.getNodePointer().getValue());

        Assert.assertFalse(iterator.setPosition(3));
    }

    @Test
    public void testGetNodePointerWhenPositionZero() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertEquals(0, iterator.getPosition());
        NodePointer ptr = iterator.getNodePointer();
        Assert.assertNotNull(ptr);
        Assert.assertEquals("val1", ptr.getValue());
        Assert.assertEquals(0, iterator.getPosition());
    }

    @Test
    public void testSetPositionBoundaries() {
        Element element = new Element("root");
        element.setAttribute("attr1", "val1");

        NodePointer parent = new JDOMNodePointer(element, Locale.getDefault());
        QName name = new QName("attr1");
        JDOMAttributeIterator iterator = new JDOMAttributeIterator(parent, name);

        Assert.assertFalse(iterator.setPosition(0));
        Assert.assertEquals(0, iterator.getPosition());

        Assert.assertFalse(iterator.setPosition(-1));
        Assert.assertEquals(-1, iterator.getPosition());

        Assert.assertTrue(iterator.setPosition(1));
        Assert.assertEquals(1, iterator.getPosition());

        Assert.assertFalse(iterator.setPosition(2));
        Assert.assertEquals(2, iterator.getPosition());
    }
}
