package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NodeTest {

    private static class ConcreteNode extends Node {
        private String name;

        public ConcreteNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
            this.name = "concrete";
        }

        public ConcreteNode(String baseUri) {
            super(baseUri);
            this.name = "concrete";
        }

        public ConcreteNode(String baseUri, String name) {
            super(baseUri);
            this.name = name;
        }

        public ConcreteNode() {
            super();
            this.name = "concrete";
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
            indent(accum, depth, out);
            accum.append("</").append(nodeName()).append(">");
        }
    }

    @Test
    public void testDefaultConstructor() {
        Node node = new ConcreteNode();
        Assert.assertEquals(0, node.childNodes().size());
        Assert.assertNull(node.attributes());
        Assert.assertNull(node.baseUri());
        Assert.assertNull(node.parent());
        Assert.assertEquals(Integer.valueOf(0), node.siblingIndex());
    }

    @Test
    public void testBaseUriConstructor() {
        Node node = new ConcreteNode("  http://example.com/test  ");
        Assert.assertEquals("http://example.com/test", node.baseUri());
        Assert.assertNotNull(node.attributes());
        Assert.assertEquals(0, node.childNodes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullBaseUri() {
        new ConcreteNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAttributes() {
        new ConcreteNode("http://example.com", null);
    }

    @Test
    public void testAttributesHandling() {
        Node node = new ConcreteNode("http://example.com");
        Assert.assertFalse(node.hasAttr("key"));
        Assert.assertEquals("", node.attr("key"));

        node.attr("key", "value");
        Assert.assertTrue(node.hasAttr("key"));
        Assert.assertEquals("value", node.attr("key"));
        Assert.assertNotNull(node.attributes());
        Assert.assertEquals(1, node.attributes().size());

        node.removeAttr("key");
        Assert.assertFalse(node.hasAttr("key"));
        Assert.assertEquals("", node.attr("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        Node node = new ConcreteNode("http://example.com");
        node.attr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasAttrNullKey() {
        Node node = new ConcreteNode("http://example.com");
        node.hasAttr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttrNullKey() {
        Node node = new ConcreteNode("http://example.com");
        node.removeAttr(null);
    }

    @Test
    public void testSetBaseUri() {
        Node node = new ConcreteNode("http://example.com");
        node.setBaseUri("http://example.org");
        Assert.assertEquals("http://example.org", node.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        Node node = new ConcreteNode("http://example.com");
        node.setBaseUri(null);
    }

    @Test
    public void testAbsUrl() {
        Node node = new ConcreteNode("http://example.com/dir/page.html");
        node.attr("href", "sub/target.html");
        node.attr("absHref", "http://other.com/index.html");

        Assert.assertEquals("http://example.com/dir/sub/target.html", node.absUrl("href"));
        Assert.assertEquals("http://other.com/index.html", node.absUrl("absHref"));
        Assert.assertEquals("http://example.com/dir/sub/target.html", node.attr("abs:href"));
        Assert.assertEquals("http://example.com/dir/sub/target.html", node.attr("ABS:href"));
        Assert.assertEquals("", node.absUrl("nonexistent"));
        Assert.assertEquals("", node.attr("abs:nonexistent"));
    }

    @Test
    public void testAbsUrlMalformedBase() {
        Node node = new ConcreteNode("malformed-uri");
        node.attr("href", "http://example.com/abs.html");
        node.attr("rel", "sub/rel.html");

        Assert.assertEquals("http://example.com/abs.html", node.absUrl("href"));
        Assert.assertEquals("", node.absUrl("rel"));
    }

    @Test
    public void testAbsUrlMalformedAttribute() {
        Node node = new ConcreteNode("http://example.com");
        node.attr("href", "http://[invalid-url");
        Assert.assertEquals("", node.absUrl("href"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        Node node = new ConcreteNode("http://example.com");
        node.absUrl("");
    }

    @Test
    public void testChildNodesAndArray() {
        Node parent = new ConcreteNode("http://example.com");
        Node child1 = new ConcreteNode("http://example.com", "child1");
        Node child2 = new ConcreteNode("http://example.com", "child2");

        parent.addChildren(child1, child2);

        Assert.assertEquals(2, parent.childNodes().size());
        Assert.assertEquals(child1, parent.childNode(0));
        Assert.assertEquals(child2, parent.childNode(1));

        Node[] array = parent.childNodesAsArray();
        Assert.assertEquals(2, array.length);
        Assert.assertEquals(child1, array[0]);
        Assert.assertEquals(child2, array[1]);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChildNodesUnmodifiable() {
        Node parent = new ConcreteNode("http://example.com");
        parent.childNodes().add(new ConcreteNode("http://example.com"));
    }

    @Test
    public void testOwnerDocument() {
        Document doc = new Document("http://example.com");
        Node child = new ConcreteNode("http://example.com");
        Node grandchild = new ConcreteNode("http://example.com");

        Assert.assertEquals(doc, doc.ownerDocument());
        Assert.assertNull(child.ownerDocument());

        doc.addChildren(child);
        child.addChildren(grandchild);

        Assert.assertEquals(doc, child.ownerDocument());
        Assert.assertEquals(doc, grandchild.ownerDocument());
    }

    @Test
    public void testRemove() {
        Node parent = new ConcreteNode("http://example.com");
        Node child1 = new ConcreteNode("http://example.com");
        Node child2 = new ConcreteNode("http://example.com");
        parent.addChildren(child1, child2);

        Assert.assertEquals(0, (int) child1.siblingIndex());
        Assert.assertEquals(1, (int) child2.siblingIndex());

        child1.remove();
        Assert.assertNull(child1.parent());
        Assert.assertEquals(1, parent.childNodes().size());
        Assert.assertEquals(child2, parent.childNode(0));
        Assert.assertEquals(0, (int) child2.siblingIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveOrphanThrows() {
        Node node = new ConcreteNode("http://example.com");
        node.remove();
    }

    @Test
    public void testReplaceWith() {
        Node parent = new ConcreteNode("http://example.com");
        Node child1 = new ConcreteNode("http://example.com");
        Node child2 = new ConcreteNode("http://example.com");
        Node replacement = new ConcreteNode("http://example.com");

        parent.addChildren(child1, child2);
        child1.replaceWith(replacement);

        Assert.assertNull(child1.parent());
        Assert.assertEquals(parent, replacement.parent());
        Assert.assertEquals(0, (int) replacement.siblingIndex());
        Assert.assertEquals(2, parent.childNodes().size());
        Assert.assertEquals(replacement, parent.childNode(0));
        Assert.assertEquals(child2, parent.childNode(1));
    }

    @Test
    public void testReplaceChildWithReparenting() {
        Node parent1 = new ConcreteNode("http://example.com");
        Node parent2 = new ConcreteNode("http://example.com");
        Node child1 = new ConcreteNode("http://example.com");
        Node child2 = new ConcreteNode("http://example.com");

        parent1.addChildren(child1);
        parent2.addChildren(child2);

        parent1.replaceChild(child1, child2);

        Assert.assertEquals(0, parent2.childNodes().size());
        Assert.assertEquals(1, parent1.childNodes().size());
        Assert.assertEquals(child2, parent1.childNode(0));
        Assert.assertEquals(parent1, child2.parent());
        Assert.assertNull(child1.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNotDirectParent() {
        Node parent1 = new ConcreteNode("http://example.com");
        Node parent2 = new ConcreteNode("http://example.com");
        Node child = new ConcreteNode("http://example.com");
        parent1.addChildren(child);

        parent2.replaceChild(child, new ConcreteNode("http://example.com"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNull() {
        Node parent = new ConcreteNode("http://example.com");
        Node child = new ConcreteNode("http://example.com");
        parent.addChildren(child);
        child.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithOrphan() {
        Node child = new ConcreteNode("http://example.com");
        child.replaceWith(new ConcreteNode("http://example.com"));
    }

    @Test
    public void testSetParentNode() {
        Node parent1 = new ConcreteNode("http://example.com");
        Node parent2 = new ConcreteNode("http://example.com");
        Node child = new ConcreteNode("http://example.com");

        parent1.addChildren(child);
        Assert.assertEquals(1, parent1.childNodes().size());

        child.setParentNode(parent2);
        Assert.assertEquals(0, parent1.childNodes().size());
        Assert.assertEquals(parent2, child.parent());
    }

    @Test
    public void testAddChildrenAtIndex() {
        Node parent = new ConcreteNode("http://example.com");
        Node c1 = new ConcreteNode("http://example.com", "c1");
        Node c2 = new ConcreteNode("http://example.com", "c2");
        Node c3 = new ConcreteNode("http://example.com", "c3");
        Node c4 = new ConcreteNode("http://example.com", "c4");

        parent.addChildren(c1, c4);
        parent.addChildren(1, c2, c3);

        Assert.assertEquals(4, parent.childNodes().size());
        Assert.assertEquals(c1, parent.childNode(0));
        Assert.assertEquals(c2, parent.childNode(1));
        Assert.assertEquals(c3, parent.childNode(2));
        Assert.assertEquals(c4, parent.childNode(3));

        Assert.assertEquals(0, (int) c1.siblingIndex());
        Assert.assertEquals(1, (int) c2.siblingIndex());
        Assert.assertEquals(2, (int) c3.siblingIndex());
        Assert.assertEquals(3, (int) c4.siblingIndex());
    }

    @Test
    public void testAddExistingChildReparents() {
        Node parent1 = new ConcreteNode("http://example.com");
        Node parent2 = new ConcreteNode("http://example.com");
        Node child = new ConcreteNode("http://example.com");

        parent1.addChildren(child);
        Assert.assertEquals(1, parent1.childNodes().size());

        parent2.addChildren(child);
        Assert.assertEquals(0, parent1.childNodes().size());
        Assert.assertEquals(1, parent2.childNodes().size());
        Assert.assertEquals(parent2, child.parent());
    }

    @Test
    public void testSiblings() {
        Node parent = new ConcreteNode("http://example.com");
        Node c1 = new ConcreteNode("http://example.com");
        Node c2 = new ConcreteNode("http://example.com");
        Node c3 = new ConcreteNode("http://example.com");

        parent.addChildren(c1, c2, c3);

        List<Node> siblings = c1.siblingNodes();
        Assert.assertEquals(3, siblings.size());

        Assert.assertNull(c1.previousSibling());
        Assert.assertEquals(c2, c1.nextSibling());

        Assert.assertEquals(c1, c2.previousSibling());
        Assert.assertEquals(c3, c2.nextSibling());

        Assert.assertEquals(c2, c3.previousSibling());
        Assert.assertNull(c3.nextSibling());
    }

    @Test
    public void testNextSiblingOnRoot() {
        Node root = new ConcreteNode("http://example.com");
        Assert.assertNull(root.nextSibling());
    }

    @Test(expected = NullPointerException.class)
    public void testPreviousSiblingOnRootThrows() {
        Node root = new ConcreteNode("http://example.com");
        root.previousSibling();
    }

    @Test
    public void testOuterHtmlAndToString() {
        Document doc = new Document("http://example.com");
        Node node = new ConcreteNode("http://example.com", "div");
        Node child = new ConcreteNode("http://example.com", "span");
        Node text = new TextNode("text content", "http://example.com");

        doc.appendChild(node);
        node.addChildren(child);
        child.addChildren(text);

        String html = node.outerHtml();
        Assert.assertNotNull(html);
        Assert.assertTrue(html.contains("<div>"));
        Assert.assertTrue(html.contains("<span>"));
        Assert.assertTrue(html.contains("text content"));
        Assert.assertTrue(html.contains("</span>"));
        Assert.assertTrue(html.contains("</div>"));
        Assert.assertEquals(html, node.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        Node node1 = new ConcreteNode("http://example.com");
        Node node2 = new ConcreteNode("http://example.com");

        Assert.assertTrue(node1.equals(node1));
        Assert.assertFalse(node1.equals(node2));
        Assert.assertFalse(node1.equals(null));
        Assert.assertFalse(node1.equals("String"));

        int hash1 = node1.hashCode();
        node1.attr("key", "value");
        int hash2 = node1.hashCode();
        Assert.assertNotEquals(hash1, hash2);

        Node parent = new ConcreteNode("http://example.com");
        parent.addChildren(node1);
        int hash3 = node1.hashCode();
        Assert.assertNotEquals(hash2, hash3);

        Node defaultNode = new ConcreteNode();
        Assert.assertEquals(0, defaultNode.hashCode());
    }
}
