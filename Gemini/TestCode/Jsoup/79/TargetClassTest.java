package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

public class LeafNodeTest {

    private static class ConcreteLeafNode extends LeafNode {
        ConcreteLeafNode() {
            super();
        }

        ConcreteLeafNode(String val) {
            this.value = val;
        }

        @Override
        public String nodeName() {
            return "#test";
        }

        @Override
        void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) {
        }

        @Override
        void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {
        }
    }

    @Test
    public void testCoreValueGetterAndSetter() {
        ConcreteLeafNode node = new ConcreteLeafNode("initial");
        Assert.assertEquals("initial", node.coreValue());
        node.coreValue("updated");
        Assert.assertEquals("updated", node.coreValue());
    }

    @Test
    public void testAttrWithSingleCoreValue() {
        ConcreteLeafNode node = new ConcreteLeafNode("data");
        Assert.assertFalse(node.hasAttributes());
        Assert.assertEquals("data", node.attr("#test"));
        Assert.assertEquals("", node.attr("other"));
        Assert.assertFalse(node.hasAttributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        ConcreteLeafNode node = new ConcreteLeafNode("data");
        node.attr(null);
    }

    @Test
    public void testAttrSettingCoreValueDirectly() {
        ConcreteLeafNode node = new ConcreteLeafNode();
        node.attr("#test", "newValue");
        Assert.assertFalse(node.hasAttributes());
        Assert.assertEquals("newValue", node.attr("#test"));
    }

    @Test
    public void testAttrSettingDifferentKeyPromotesToAttributes() {
        ConcreteLeafNode node = new ConcreteLeafNode("data");
        Assert.assertFalse(node.hasAttributes());
        node.attr("otherKey", "val");
        Assert.assertTrue(node.hasAttributes());
        Assert.assertEquals("data", node.attr("#test"));
        Assert.assertEquals("val", node.attr("otherKey"));
        
        node.attr("#test", "dataModified");
        Assert.assertEquals("dataModified", node.attr("#test"));
    }

    @Test
    public void testAttributesAccessPromotesWithNullCoreValue() {
        ConcreteLeafNode node = new ConcreteLeafNode();
        Assert.assertFalse(node.hasAttributes());
        Attributes attrs = node.attributes();
        Assert.assertNotNull(attrs);
        Assert.assertTrue(node.hasAttributes());
        Assert.assertFalse(attrs.hasKey("#test"));
        
        Attributes attrs2 = node.attributes();
        Assert.assertSame(attrs, attrs2);
    }

    @Test
    public void testAttributesAccessPromotesWithNonNullCoreValue() {
        ConcreteLeafNode node = new ConcreteLeafNode("initial");
        Attributes attrs = node.attributes();
        Assert.assertNotNull(attrs);
        Assert.assertTrue(node.hasAttributes());
        Assert.assertEquals("initial", attrs.get("#test"));
    }

    @Test
    public void testHasAttrPromotesAttributes() {
        ConcreteLeafNode node = new ConcreteLeafNode("data");
        Assert.assertFalse(node.hasAttributes());
        Assert.assertTrue(node.hasAttr("#test"));
        Assert.assertTrue(node.hasAttributes());
        Assert.assertFalse(node.hasAttr("missing"));
    }

    @Test
    public void testRemoveAttrPromotesAttributes() {
        ConcreteLeafNode node = new ConcreteLeafNode("data");
        Assert.assertFalse(node.hasAttributes());
        node.removeAttr("#test");
        Assert.assertTrue(node.hasAttributes());
        Assert.assertFalse(node.hasAttr("#test"));
        Assert.assertEquals("", node.attr("#test"));
    }

    @Test
    public void testAbsUrlPromotesAttributes() {
        ConcreteLeafNode node = new ConcreteLeafNode("https://example.com");
        Assert.assertFalse(node.hasAttributes());
        String abs = node.absUrl("#test");
        Assert.assertTrue(node.hasAttributes());
        Assert.assertNotNull(abs);
    }

    @Test
    public void testBaseUriWithoutParent() {
        ConcreteLeafNode node = new ConcreteLeafNode();
        Assert.assertEquals("", node.baseUri());
        node.doSetBaseUri("https://example.com");
        Assert.assertEquals("", node.baseUri());
    }

    @Test
    public void testBaseUriWithParent() {
        Element parent = new Element("div");
        parent.setBaseUri("https://example.com/base/");
        ConcreteLeafNode node = new ConcreteLeafNode();
        parent.appendChild(node);
        Assert.assertEquals("https://example.com/base/", node.baseUri());
    }

    @Test
    public void testChildNodeSize() {
        ConcreteLeafNode node = new ConcreteLeafNode();
        Assert.assertEquals(0, node.childNodeSize());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEnsureChildNodesThrowsException() {
        ConcreteLeafNode node = new ConcreteLeafNode();
        node.ensureChildNodes();
    }
}
