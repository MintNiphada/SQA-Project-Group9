package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;

public class NodeTest {

    // Concrete subclass for testing
    static class TestNode extends Node {
        public TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
        }
        public TestNode(String baseUri) {
            super(baseUri);
        }
        public TestNode() {
            super();
        }
        @Override
        public String nodeName() {
            return "#test";
        }
        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<test>");
        }
        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</test>");
        }
        // Expose protected methods for testing
        public void exposeSetParentNode(Node parent) {
            setParentNode(parent);
        }
        public void exposeReplaceChild(Node out, Node in) {
            replaceChild(out, in);
        }
        public void exposeRemoveChild(Node out) {
            removeChild(out);
        }
        public void exposeAddChildren(Node... children) {
            addChildren(children);
        }
        public void exposeAddChildren(int index, Node... children) {
            addChildren(index, children);
        }
        public Node[] exposeChildNodesAsArray() {
            return childNodesAsArray();
        }
        public void exposeSetSiblingIndex(int index) {
            setSiblingIndex(index);
        }
    }

    @Test
    public void testConstructorWithBaseUriAndAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("http://example.com", node.baseUri());
        assertEquals(attrs, node.attributes());
        assertTrue(node.childNodes().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullBaseUri() {
        new TestNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAttributes() {
        new TestNode("http://example.com", null);
    }

    @Test
    public void testConstructorWithBaseUriOnly() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("http://example.com", node.baseUri());
        assertNotNull(node.attributes());
        assertTrue(node.attributes().size() == 0);
    }

    @Test
    public void testDefaultConstructor() {
        TestNode node = new TestNode();
        assertNull(node.baseUri());
        assertNull(node.attributes());
        assertEquals(0, node.childNodes().size());
    }

    @Test
    public void testAttrGetExisting() {
        Attributes attrs = new Attributes();
        attrs.put("class", "foo");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("foo", node.attr("class"));
    }

    @Test
    public void testAttrGetNonExisting() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.attr("nonexistent"));
    }

    @Test
    public void testAttrGetAbsPrefix() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/path");
        TestNode node = new TestNode("http://example.com", attrs);
        String absUrl = node.attr("abs:href");
        assertEquals("http://example.com/path", absUrl);
    }

    @Test
    public void testAttrGetAbsPrefixMissingAttribute() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.attr("abs:href"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrGetNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.attr(null);
    }

    @Test
    public void testAttrSet() {
        TestNode node = new TestNode("http://example.com");
        node.attr("key", "value");
        assertEquals("value", node.attr("key"));
        assertSame(node, node.attr("key2", "value2"));
    }

    @Test
    public void testHasAttr() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        TestNode node = new TestNode("http://example.com", attrs);
        assertTrue(node.hasAttr("key"));
        assertFalse(node.hasAttr("nonexistent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.hasAttr(null);
    }

    @Test
    public void testRemoveAttr() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        TestNode node = new TestNode("http://example.com", attrs);
        node.removeAttr("key");
        assertFalse(node.hasAttr("key"));
        assertSame(node, node.removeAttr("nonexistent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttrNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.removeAttr(null);
    }

    @Test
    public void testBaseUri() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("http://example.com", node.baseUri());
    }

    @Test
    public void testSetBaseUri() {
        TestNode node = new TestNode("http://example.com");
        node.setBaseUri("http://newexample.com");
        assertEquals("http://newexample.com", node.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        TestNode node = new TestNode("http://example.com");
        node.setBaseUri(null);
    }

    @Test
    public void testAbsUrlMissingAttribute() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlAbsoluteAttribute() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://absolute.com/path");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("http://absolute.com/path", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlRelativeAttributeValidBase() {
        Attributes attrs = new Attributes();
        attrs.put("href", "/path");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("http://example.com/path", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlInvalidBaseButAbsoluteRelUrl() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://absolute.com/path");
        TestNode node = new TestNode("invalid_base", attrs);
        assertEquals("http://absolute.com/path", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlInvalidBaseAndInvalidRelUrl() {
        Attributes attrs = new Attributes();
        attrs.put("href", "invalid_url");
        TestNode node = new TestNode("invalid_base", attrs);
        assertEquals("", node.absUrl("href"));
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
    public void testChildNode() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1, child2);
        assertEquals(child1, parent.childNode(0));
        assertEquals(child2, parent.childNode(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNodeOutOfBounds() {
        TestNode parent = new TestNode("http://example.com");
        parent.childNode(0);
    }

    @Test
    public void testChildNodes() {
        TestNode parent = new TestNode("http://example.com");
        List<Node> children = parent.childNodes();
        assertTrue(children.isEmpty());
        TestNode child = new TestNode("http://example.com");
        parent.exposeAddChildren(child);
        children = parent.childNodes();
        assertEquals(1, children.size());
        try {
            children.add(new TestNode("http://example.com"));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testChildNodesAsArray() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.exposeAddChildren(child);
        Node[] arr = parent.exposeChildNodesAsArray();
        assertEquals(1, arr.length);
        assertEquals(child, arr[0]);
    }

    @Test
    public void testParent() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        child.exposeSetParentNode(parent);
        assertEquals(parent, child.parent());
    }

    @Test
    public void testOwnerDocumentForDocument() {
        Document doc = new Document("http://example.com");
        assertEquals(doc, doc.ownerDocument());
    }

    @Test
    public void testOwnerDocumentForNodeWithParent() {
        Document doc = new Document("http://example.com");
        TestNode child = new TestNode("http://example.com");
        doc.appendChild(child);
        assertEquals(doc, child.ownerDocument());
    }

    @Test
    public void testOwnerDocumentForNodeWithoutParent() {
        TestNode node = new TestNode("http://example.com");
        assertNull(node.ownerDocument());
    }

    @Test
    public void testRemove() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.exposeAddChildren(child);
        assertNotNull(child.parent());
        child.remove();
        assertNull(child.parent());
        assertFalse(parent.childNodes().contains(child));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithoutParent() {
        TestNode node = new TestNode("http://example.com");
        node.remove();
    }

    @Test
    public void testReplaceWith() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1);
        child1.replaceWith(child2);
        assertEquals(1, parent.childNodes().size());
        assertEquals(child2, parent.childNode(0));
        assertNull(child1.parent());
        assertEquals(parent, child2.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNullNode() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.exposeAddChildren(child);
        child.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNoParent() {
        TestNode node = new TestNode("http://example.com");
        node.replaceWith(new TestNode("http://example.com"));
    }

    @Test
    public void testSetParentNode() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        child.exposeSetParentNode(parent1);
        assertEquals(parent1, child.parent());
        assertTrue(parent1.childNodes().contains(child));
        child.exposeSetParentNode(parent2);
        assertEquals(parent2, child.parent());
        assertFalse(parent1.childNodes().contains(child));
        assertTrue(parent2.childNodes().contains(child));
    }

    @Test
    public void testReplaceChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1);
        parent.exposeReplaceChild(child1, child2);
        assertEquals(1, parent.childNodes().size());
        assertEquals(child2, parent.childNode(0));
        assertNull(child1.parent());
        assertEquals(parent, child2.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildOutNotChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeReplaceChild(child1, child2);
    }

    @Test
    public void testRemoveChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.exposeAddChildren(child);
        parent.exposeRemoveChild(child);
        assertFalse(parent.childNodes().contains(child));
        assertNull(child.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNotChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.exposeRemoveChild(child);
    }

    @Test
    public void testAddChildrenVarargs() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1, child2);
        assertEquals(2, parent.childNodes().size());
        assertEquals(child1, parent.childNode(0));
        assertEquals(child2, parent.childNode(1));
        assertEquals(parent, child1.parent());
        assertEquals(parent, child2.parent());
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child2.siblingIndex().intValue());
    }

    @Test
    public void testAddChildrenAtIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1);
        parent.exposeAddChildren(0, child2);
        assertEquals(2, parent.childNodes().size());
        assertEquals(child2, parent.childNode(0));
        assertEquals(child1, parent.childNode(1));
        assertEquals(0, child2.siblingIndex().intValue());
        assertEquals(1, child1.siblingIndex().intValue());
    }

    @Test
    public void testAddChildrenReparenting() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent1.exposeAddChildren(child);
        parent2.exposeAddChildren(child);
        assertFalse(parent1.childNodes().contains(child));
        assertTrue(parent2.childNodes().contains(child));
        assertEquals(parent2, child.parent());
    }

    @Test
    public void testSiblingNodes() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1, child2);
        List<Node> siblings = child1.siblingNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(child1));
        assertTrue(siblings.contains(child2));
    }

    @Test
    public void testNextSibling() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1, child2);
        assertEquals(child2, child1.nextSibling());
        assertNull(child2.nextSibling());
    }

    @Test
    public void testNextSiblingNoParent() {
        TestNode node = new TestNode("http://example.com");
        assertNull(node.nextSibling());
    }

    @Test
    public void testPreviousSibling() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1, child2);
        assertEquals(child1, child2.previousSibling());
        assertNull(child1.previousSibling());
    }

    @Test
    public void testSiblingIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1, child2);
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child2.siblingIndex().intValue());
    }

    @Test
    public void testSetSiblingIndex() {
        TestNode node = new TestNode("http://example.com");
        node.exposeSetSiblingIndex(5);
        assertEquals(5, node.siblingIndex().intValue());
    }

    @Test
    public void testOuterHtml() {
        Document doc = new Document("http://example.com");
        TestNode node = new TestNode("http://example.com");
        doc.appendChild(node);
        String html = node.outerHtml();
        assertTrue(html.contains("<test>"));
        assertTrue(html.contains("</test>"));
    }

    @Test
    public void testToString() {
        Document doc = new Document("http://example.com");
        TestNode node = new TestNode("http://example.com");
        doc.appendChild(node);
        assertEquals(node.outerHtml(), node.toString());
    }

    @Test
    public void testEqualsSameObject() {
        TestNode node = new TestNode("http://example.com");
        assertTrue(node.equals(node));
    }

    @Test
    public void testEqualsDifferentObject() {
        TestNode node1 = new TestNode("http://example.com");
        TestNode node2 = new TestNode("http://example.com");
        assertFalse(node1.equals(node2));
    }

    @Test
    public void testHashCode() {
        TestNode node = new TestNode("http://example.com");
        node.hashCode();
        TestNode parent = new TestNode("http://example.com");
        node.exposeSetParentNode(parent);
        int hash1 = node.hashCode();
        node.attr("key", "value");
        int hash2 = node.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testDefaultConstructorChildNodesEmptyList() {
        TestNode node = new TestNode();
        List<Node> children = node.childNodes();
        assertTrue(children.isEmpty());
        try {
            children.add(new TestNode("http://example.com"));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAddChildrenWithNullElements() {
        TestNode parent = new TestNode("http://example.com");
        try {
            parent.exposeAddChildren(0, (Node) null);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAbsUrlWithBaseUriHavingTrailingSlash() {
        Attributes attrs = new Attributes();
        attrs.put("href", "path");
        TestNode node = new TestNode("http://example.com/", attrs);
        assertEquals("http://example.com/path", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlWithBaseUriNoTrailingSlash() {
        Attributes attrs = new Attributes();
        attrs.put("href", "path");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("http://example.com/path", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlWithAbsoluteAttributeHavingProtocol() {
        Attributes attrs = new Attributes();
        attrs.put("href", "https://secure.com/path");
        TestNode node = new TestNode("http://example.com", attrs);
        assertEquals("https://secure.com/path", node.absUrl("href"));
    }

    @Test
    public void testOuterHtmlWithoutOwnerDocument() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.outerHtml();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testIndent() {
        Document doc = new Document("http://example.com");
        doc.outputSettings().indentAmount(2);
        TestNode parent = new TestNode("http://example.com");
        doc.appendChild(parent);
        TestNode child = new TestNode("http://example.com");
        parent.exposeAddChildren(child);
        String html = parent.outerHtml();
        assertTrue(html.contains("\n"));
    }

    @Test
    public void testAttrAbsPrefixOnly() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.attr("abs:");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetParentNodeNull() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        child.exposeSetParentNode(parent);
        child.exposeSetParentNode(null);
        assertNull(child.parent());
        assertFalse(parent.childNodes().contains(child));
    }

    @Test
    public void testReplaceChildReparenting() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode otherParent = new TestNode("http://example.com");
        otherParent.exposeAddChildren(child2);
        parent.exposeAddChildren(child1);
        parent.exposeReplaceChild(child1, child2);
        assertEquals(parent, child2.parent());
        assertFalse(otherParent.childNodes().contains(child2));
    }

    @Test
    public void testAddChildrenEmptyVarargs() {
        TestNode parent = new TestNode("http://example.com");
        parent.exposeAddChildren();
        assertEquals(0, parent.childNodes().size());
    }

    @Test
    public void testSiblingNodesNoParent() {
        TestNode node = new TestNode("http://example.com");
        try {
            node.siblingNodes();
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testReindexAfterRemoveChild() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode child3 = new TestNode("http://example.com");
        parent.exposeAddChildren(child1, child2, child3);
        parent.exposeRemoveChild(child2);
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child3.siblingIndex().intValue());
    }
}
