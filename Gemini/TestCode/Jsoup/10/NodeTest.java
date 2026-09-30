package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NodeTest {

    private static class TestNode extends Node {
        private String name;

        public TestNode() {
            super();
            this.name = "test";
        }

        public TestNode(String baseUri) {
            super(baseUri);
            this.name = "test";
        }

        public TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
            this.name = "test";
        }

        public TestNode(String baseUri, Attributes attributes, String name) {
            super(baseUri, attributes);
            this.name = name;
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            indent(accum, depth, out);
            accum.append("<").append(nodeName()).append(">");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</").append(nodeName()).append(">");
        }
    }

    @Test
    public void testConstructors() {
        TestNode node1 = new TestNode("http://example.com/");
        Assert.assertEquals("http://example.com/", node1.baseUri());
        Assert.assertNotNull(node1.attributes());
        Assert.assertEquals(0, node1.childNodes().size());

        Attributes attrs = new Attributes();
        attrs.put("k", "v");
        TestNode node2 = new TestNode("  http://example.com/sub/  ", attrs);
        Assert.assertEquals("http://example.com/sub/", node2.baseUri());
        Assert.assertEquals("v", node2.attr("k"));

        TestNode defaultNode = new TestNode();
        Assert.assertNull(defaultNode.baseUri());
        Assert.assertNull(defaultNode.attributes());
        Assert.assertEquals(0, defaultNode.childNodes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullUri() {
        new TestNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAttributes() {
        new TestNode("http://example.com", null);
    }

    @Test
    public void testAttributesOperations() {
        TestNode node = new TestNode("http://example.com");
        Assert.assertFalse(node.hasAttr("key"));
        Assert.assertEquals("", node.attr("key"));

        node.attr("key", "value");
        Assert.assertTrue(node.hasAttr("key"));
        Assert.assertEquals("value", node.attr("key"));

        Node chained = node.removeAttr("key");
        Assert.assertSame(node, chained);
        Assert.assertFalse(node.hasAttr("key"));
        Assert.assertEquals("", node.attr("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.attr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.hasAttr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.removeAttr(null);
    }

    @Test
    public void testBaseUri() {
        TestNode node = new TestNode("http://example.com");
        Assert.assertEquals("http://example.com", node.baseUri());

        node.setBaseUri("http://example.org");
        Assert.assertEquals("http://example.org", node.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        TestNode node = new TestNode("http://example.com");
        node.setBaseUri(null);
    }

    @Test
    public void testAbsUrl() {
        TestNode node = new TestNode("http://example.com/path/index.html");
        node.attr("href", "sub/page.html");
        node.attr("absHref", "https://other.com/test");
        node.attr("query", "?search=1");

        Assert.assertEquals("http://example.com/path/sub/page.html", node.absUrl("href"));
        Assert.assertEquals("http://example.com/path/sub/page.html", node.attr("abs:href"));
        Assert.assertEquals("http://example.com/path/sub/page.html", node.attr("ABS:href"));
        Assert.assertEquals("https://other.com/test", node.absUrl("absHref"));
        Assert.assertEquals("http://example.com/path/index.html?search=1", node.absUrl("query"));
        Assert.assertEquals("", node.absUrl("nonexistent"));
        Assert.assertEquals("", node.attr("abs:nonexistent"));
        Assert.assertEquals("", node.attr("unknown"));
    }

    @Test
    public void testAbsUrlMalformedBase() {
        TestNode node = new TestNode("invalid-uri");
        node.attr("href", "http://example.com/absolute");
        Assert.assertEquals("http://example.com/absolute", node.absUrl("href"));

        node.attr("rel", "relative");
        Assert.assertEquals("", node.absUrl("rel"));
    }

    @Test
    public void testAbsUrlMalformedRelative() {
        TestNode node = new TestNode("http://example.com");
        node.attr("bad", "http://");
        Assert.assertEquals("", node.absUrl("bad"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        TestNode node = new TestNode("http://example.com");
        node.absUrl("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.absUrl(null);
    }

    @Test
    public void testChildNodesAndArray() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");

        parent.addChildren(child1, child2);
        Assert.assertEquals(2, parent.childNodes().size());
        Assert.assertSame(child1, parent.childNode(0));
        Assert.assertSame(child2, parent.childNode(1));

        Node[] array = parent.childNodesAsArray();
        Assert.assertEquals(2, array.length);
        Assert.assertSame(child1, array[0]);
        Assert.assertSame(child2, array[1]);
    }

    @Test
    public void testParentAndOwnerDocument() {
        Document doc = new Document("http://example.com");
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");

        Assert.assertNull(parent.parent());
        Assert.assertNull(parent.ownerDocument());
        Assert.assertSame(doc, doc.ownerDocument());

        doc.appendChild(parent);
        parent.addChildren(child);

        Assert.assertSame(doc, parent.parent());
        Assert.assertSame(parent, child.parent());
        Assert.assertSame(doc, parent.ownerDocument());
        Assert.assertSame(doc, child.ownerDocument());
    }

    @Test
    public void testRemove() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.addChildren(child1, child2);

        child1.remove();
        Assert.assertNull(child1.parent());
        Assert.assertEquals(1, parent.childNodes().size());
        Assert.assertSame(child2, parent.childNode(0));
        Assert.assertEquals(Integer.valueOf(0), child2.siblingIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveOrphan() {
        TestNode node = new TestNode("http://example.com");
        node.remove();
    }

    @Test
    public void testReplaceWith() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode replacement = new TestNode("http://example.com");

        parent.addChildren(child1, child2);
        child1.replaceWith(replacement);

        Assert.assertNull(child1.parent());
        Assert.assertSame(parent, replacement.parent());
        Assert.assertEquals(Integer.valueOf(0), replacement.siblingIndex());
        Assert.assertEquals(2, parent.childNodes().size());
        Assert.assertSame(replacement, parent.childNode(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNull() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        child.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithOrphan() {
        TestNode child = new TestNode("http://example.com");
        TestNode replacement = new TestNode("http://example.com");
        child.replaceWith(replacement);
    }

    @Test
    public void testSetParentNode() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");

        parent1.addChildren(child);
        Assert.assertSame(parent1, child.parent());
        Assert.assertEquals(1, parent1.childNodes().size());

        child.setParentNode(parent2);
        Assert.assertSame(parent2, child.parent());
        Assert.assertEquals(0, parent1.childNodes().size());
    }

    @Test
    public void testReplaceChildExistingParent() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");

        parent1.addChildren(child1);
        parent2.addChildren(child2);

        parent1.replaceChild(child1, child2);

        Assert.assertSame(parent1, child2.parent());
        Assert.assertNull(child1.parent());
        Assert.assertEquals(0, parent2.childNodes().size());
        Assert.assertSame(child2, parent1.childNode(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNotChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode stranger = new TestNode("http://example.com");
        TestNode in = new TestNode("http://example.com");

        parent.replaceChild(stranger, in);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNotChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode stranger = new TestNode("http://example.com");
        parent.removeChild(stranger);
    }

    @Test
    public void testAddChildrenAtIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode c1 = new TestNode("http://example.com");
        TestNode c2 = new TestNode("http://example.com");
        TestNode c3 = new TestNode("http://example.com");
        TestNode c4 = new TestNode("http://example.com");

        parent.addChildren(c1, c4);
        parent.addChildren(1, c2, c3);

        Assert.assertEquals(4, parent.childNodes().size());
        Assert.assertSame(c1, parent.childNode(0));
        Assert.assertSame(c2, parent.childNode(1));
        Assert.assertSame(c3, parent.childNode(2));
        Assert.assertSame(c4, parent.childNode(3));

        Assert.assertEquals(Integer.valueOf(0), c1.siblingIndex());
        Assert.assertEquals(Integer.valueOf(1), c2.siblingIndex());
        Assert.assertEquals(Integer.valueOf(2), c3.siblingIndex());
        Assert.assertEquals(Integer.valueOf(3), c4.siblingIndex());
    }

    @Test
    public void testSiblingOperations() {
        TestNode parent = new TestNode("http://example.com");
        TestNode c1 = new TestNode("http://example.com");
        TestNode c2 = new TestNode("http://example.com");
        TestNode c3 = new TestNode("http://example.com");

        Assert.assertNull(c1.nextSibling());
        Assert.assertNull(c1.previousSibling());

        parent.addChildren(c1, c2, c3);

        List<Node> siblings = c1.siblingNodes();
        Assert.assertEquals(3, siblings.size());
        Assert.assertSame(c1, siblings.get(0));

        Assert.assertNull(c1.previousSibling());
        Assert.assertSame(c2, c1.nextSibling());

        Assert.assertSame(c1, c2.previousSibling());
        Assert.assertSame(c3, c2.nextSibling());

        Assert.assertSame(c2, c3.previousSibling());
        Assert.assertNull(c3.nextSibling());
    }

    @Test
    public void testHtmlGenerationAndVisitor() {
        TestNode parent = new TestNode("http://example.com", new Attributes(), "div");
        TestNode child = new TestNode("http://example.com", new Attributes(), "span");
        TestNode textChild = new TestNode("http://example.com", new Attributes(), "#text");

        parent.addChildren(child, textChild);

        String outer = parent.outerHtml();
        Assert.assertTrue(outer.contains("<div>"));
        Assert.assertTrue(outer.contains("<span>"));
        Assert.assertTrue(outer.contains("</span>"));
        Assert.assertTrue(outer.contains("<#text>"));
        Assert.assertFalse(outer.contains("</#text>"));
        Assert.assertTrue(outer.contains("</div>"));
        Assert.assertEquals(outer, parent.toString());

        Document doc = new Document("http://example.com");
        doc.outputSettings().indentAmount(4);
        doc.appendChild(parent);

        String docOuter = parent.outerHtml();
        Assert.assertNotNull(docOuter);
    }

    @Test
    public void testEqualsAndHashCode() {
        TestNode n1 = new TestNode("http://example.com");
        TestNode n2 = new TestNode("http://example.com");

        Assert.assertTrue(n1.equals(n1));
        Assert.assertFalse(n1.equals(n2));
        Assert.assertFalse(n1.equals(null));
        Assert.assertFalse(n1.equals("string"));

        TestNode defaultNode = new TestNode();
        Assert.assertEquals(0, defaultNode.hashCode());

        n1.attr("k", "v");
        Assert.assertNotEquals(0, n1.hashCode());

        TestNode parent = new TestNode("http://example.com");
        parent.addChildren(n1);
        Assert.assertNotEquals(0, n1.hashCode());
    }

    @Test
    public void testClone() {
        TestNode parent = new TestNode("http://example.com", new Attributes(), "parent");
        parent.attr("k1", "v1");
        TestNode child = new TestNode("http://example.com", new Attributes(), "child");
        child.attr("k2", "v2");
        parent.addChildren(child);

        Node clonedParent = parent.clone();

        Assert.assertNotSame(parent, clonedParent);
        Assert.assertNull(clonedParent.parent());
        Assert.assertEquals("http://example.com", clonedParent.baseUri());
        Assert.assertEquals("v1", clonedParent.attr("k1"));
        Assert.assertEquals(1, clonedParent.childNodes().size());

        Node clonedChild = clonedParent.childNode(0);
        Assert.assertNotSame(child, clonedChild);
        Assert.assertSame(clonedParent, clonedChild.parent());
        Assert.assertEquals("v2", clonedChild.attr("k2"));
        Assert.assertEquals(Integer.valueOf(0), clonedChild.siblingIndex());

        clonedChild.attr("k2", "new_val");
        Assert.assertEquals("v2", child.attr("k2"));
        Assert.assertEquals("new_val", clonedChild.attr("k2"));

        TestNode defaultNode = new TestNode();
        Node clonedDefault = defaultNode.clone();
        Assert.assertNull(clonedDefault.attributes());
    }
}
