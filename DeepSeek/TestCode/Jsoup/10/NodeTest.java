package org.jsoup.nodes;

import static org.junit.Assert.*;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class NodeTest {

    private static class TestNode extends Node {
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
            accum.append("testHead");
        }
        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("testTail");
        }
    }

    @Test
    public void testConstructorWithBaseUriAndAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        Node node = new TestNode(" http://example.com/ ", attrs);
        assertEquals("http://example.com/", node.baseUri());
        assertSame(attrs, node.attributes());
        assertEquals(0, node.childNodes().size());
        assertNotNull(node.childNodes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullBaseUri() {
        new TestNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAttributes() {
        new TestNode("http://example.com/", null);
    }

    @Test
    public void testDefaultConstructor() {
        TestNode node = new TestNode();
        assertNull(node.attributes());
        assertTrue(node.childNodes().getClass().getSimpleName().contains("EmptyList"));
    }

    @Test
    public void testAttrGetValidKey() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://example.com");
        Node node = new TestNode("http://base.com", attrs);
        assertEquals("http://example.com", node.attr("href"));
    }

    @Test
    public void testAttrGetNonExistingKey() {
        Node node = new TestNode("http://base.com");
        assertEquals("", node.attr("nonexistent"));
    }

    @Test
    public void testAttrGetAbsPrefix() {
        Node node = new TestNode("http://base.com");
        node.attr("abs:href", "relative.html");
        // will try to make absolute, but baseUri is valid
        assertTrue(node.attr("abs:href").startsWith("http://base.com"));
    }

    @Test
    public void testAttrGetAbsPrefixWhenAttributeMissing() {
        Node node = new TestNode("http://base.com");
        assertEquals("", node.attr("abs:nonexistent"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrGetNullKey() {
        Node node = new TestNode("http://base.com");
        node.attr(null);
    }

    @Test
    public void testAttrSet() {
        Node node = new TestNode("http://base.com");
        node.attr("href", "test.html");
        assertEquals("test.html", node.attr("href"));
        assertSame(node, node.attr("key", "val"));
    }

    @Test
    public void testHasAttr() {
        Node node = new TestNode("http://base.com");
        assertFalse(node.hasAttr("key"));
        node.attr("key", "val");
        assertTrue(node.hasAttr("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasAttrNullKey() {
        Node node = new TestNode("http://base.com");
        node.hasAttr(null);
    }

    @Test
    public void testRemoveAttr() {
        Node node = new TestNode("http://base.com");
        node.attr("key", "val");
        assertTrue(node.hasAttr("key"));
        assertSame(node, node.removeAttr("key"));
        assertFalse(node.hasAttr("key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttrNullKey() {
        Node node = new TestNode("http://base.com");
        node.removeAttr(null);
    }

    @Test
    public void testSetBaseUri() {
        Node node = new TestNode("http://base.com");
        node.setBaseUri("http://new.com");
        assertEquals("http://new.com", node.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        Node node = new TestNode("http://base.com");
        node.setBaseUri(null);
    }

    @Test
    public void testAbsUrlWhenAttributeMissing() {
        Node node = new TestNode("http://base.com");
        assertEquals("", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlWithValidBaseAndRelativeUrl() throws MalformedURLException {
        Node node = new TestNode("http://base.com");
        node.attr("href", "relative.html");
        String abs = node.absUrl("href");
        assertEquals(new URL("http://base.com/relative.html").toExternalForm(), abs);
    }

    @Test
    public void testAbsUrlWithAbsoluteUrl() throws MalformedURLException {
        Node node = new TestNode("http://base.com");
        node.attr("href", "http://absolute.com/path");
        assertEquals("http://absolute.com/path", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlWithMalformedBaseButAbsoluteRelUrl() throws MalformedURLException {
        Node node = new TestNode("invalid");
        node.attr("href", "http://absolute.com/path");
        assertEquals("http://absolute.com/path", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlWithMalformedBaseAndMalformedRelUrl() {
        Node node = new TestNode("invalid");
        node.attr("href", "invalid");
        assertEquals("", node.absUrl("href"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlNullKey() {
        Node node = new TestNode("http://base.com");
        node.absUrl(null);
    }

    @Test
    public void testChildNode() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        assertSame(child, parent.childNode(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNodeOutOfBounds() {
        Node parent = new TestNode("http://parent");
        parent.childNode(0);
    }

    @Test
    public void testChildNodesUnmodifiable() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        List<Node> children = parent.childNodes();
        assertEquals(1, children.size());
        try {
            children.add(new TestNode("http://another"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    public void testParent() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        assertSame(parent, child.parent());
    }

    @Test
    public void testOwnerDocumentWhenNodeIsDocument() {
        Document doc = new Document("http://doc.com");
        assertSame(doc, doc.ownerDocument());
    }

    @Test
    public void testOwnerDocumentWhenParentIsDocument() {
        Document doc = new Document("http://doc.com");
        Node child = new TestNode("http://child");
        doc.addChildren(child);
        assertSame(doc, child.ownerDocument());
    }

    @Test
    public void testOwnerDocumentWhenNoParent() {
        Node node = new TestNode("http://node");
        assertNull(node.ownerDocument());
    }

    @Test
    public void testOwnerDocumentPropagatesUp() {
        Document doc = new Document("http://doc.com");
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        doc.addChildren(parent);
        parent.addChildren(child);
        assertSame(doc, child.ownerDocument());
    }

    @Test
    public void testRemove() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        assertEquals(1, parent.childNodes().size());
        child.remove();
        assertEquals(0, parent.childNodes().size());
        assertNull(child.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithNoParent() {
        Node node = new TestNode("http://node");
        node.remove();
    }

    @Test
    public void testReplaceWith() {
        Node parent = new TestNode("http://parent");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        parent.addChildren(child1);
        child1.replaceWith(child2);
        assertSame(child2, parent.childNode(0));
        assertNull(child1.parent());
        assertSame(parent, child2.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNullNode() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        child.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNoParent() {
        Node node = new TestNode("http://node");
        node.replaceWith(new TestNode("http://other"));
    }

    @Test
    public void testSetParentNodeChangesParent() {
        Node oldParent = new TestNode("http://old");
        Node newParent = new TestNode("http://new");
        Node child = new TestNode("http://child");
        oldParent.addChildren(child);
        assertSame(oldParent, child.parent());
        child.setParentNode(newParent);
        assertSame(newParent, child.parent());
        assertFalse(oldParent.childNodes().contains(child));
        assertTrue(newParent.childNodes().contains(child));
    }

    @Test
    public void testReplaceChild() {
        Node parent = new TestNode("http://parent");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        parent.addChildren(child1);
        parent.replaceChild(child1, child2);
        assertSame(child2, parent.childNode(0));
        assertSame(parent, child2.parent());
        assertNull(child1.parent());
        assertEquals(0, child2.siblingIndex().intValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNotChildOfParent() {
        Node parent = new TestNode("http://parent");
        Node other = new TestNode("http://other");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        parent.replaceChild(other, child);
    }

    @Test
    public void testReplaceChildWithNodeHavingExistingParent() {
        Node parent1 = new TestNode("http://parent1");
        Node parent2 = new TestNode("http://parent2");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        parent1.addChildren(child1);
        parent2.addChildren(child2);
        parent2.replaceChild(child2, child1);
        assertFalse(parent1.childNodes().contains(child1));
        assertSame(parent2, child1.parent());
    }

    @Test
    public void testRemoveChild() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        parent.removeChild(child);
        assertEquals(0, parent.childNodes().size());
        assertNull(child.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNotChild() {
        Node parent = new TestNode("http://parent");
        Node other = new TestNode("http://other");
        parent.removeChild(other);
    }

    @Test
    public void testAddChildrenVarargs() {
        Node parent = new TestNode("http://parent");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        parent.addChildren(child1, child2);
        assertEquals(2, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child2.siblingIndex().intValue());
    }

    @Test
    public void testAddChildrenAtIndex() {
        Node parent = new TestNode("http://parent");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        Node child3 = new TestNode("http://child3");
        parent.addChildren(child1, child3); // order: child1, child3
        parent.addChildren(1, child2);      // insert at index 1
        assertEquals(3, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertSame(child3, parent.childNode(2));
        assertEquals(0, child1.siblingIndex().intValue());
        assertEquals(1, child2.siblingIndex().intValue());
        assertEquals(2, child3.siblingIndex().intValue());
    }

    @Test
    public void testAddChildrenWithExistingParentReassigns() {
        Node oldParent = new TestNode("http://old");
        Node newParent = new TestNode("http://new");
        Node child = new TestNode("http://child");
        oldParent.addChildren(child);
        newParent.addChildren(child);
        assertFalse(oldParent.childNodes().contains(child));
        assertTrue(newParent.childNodes().contains(child));
        assertSame(newParent, child.parent());
    }

    @Test
    public void testSiblingNodes() {
        Node parent = new TestNode("http://parent");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        parent.addChildren(child1, child2);
        List<Node> siblings = child1.siblingNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(child1));
        assertTrue(siblings.contains(child2));
    }

    @Test
    public void testNextSibling() {
        Node parent = new TestNode("http://parent");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        parent.addChildren(child1, child2);
        assertSame(child2, child1.nextSibling());
        assertNull(child2.nextSibling());
    }

    @Test
    public void testNextSiblingNoParent() {
        Node node = new TestNode("http://node");
        assertNull(node.nextSibling());
    }

    @Test
    public void testPreviousSibling() {
        Node parent = new TestNode("http://parent");
        Node child1 = new TestNode("http://child1");
        Node child2 = new TestNode("http://child2");
        parent.addChildren(child1, child2);
        assertNull(child1.previousSibling());
        assertSame(child1, child2.previousSibling());
    }

    @Test
    public void testSiblingIndex() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        parent.addChildren(child);
        assertEquals(0, child.siblingIndex().intValue());
    }

    @Test
    public void testSetSiblingIndex() {
        Node node = new TestNode("http://node");
        node.setSiblingIndex(5);
        assertEquals(5, node.siblingIndex().intValue());
    }

    @Test
    public void testOuterHtml() {
        Node node = new TestNode("http://node");
        String html = node.outerHtml();
        assertTrue(html.contains("testHead"));
        assertTrue(html.contains("testTail"));
        // also test toString
        assertEquals(html, node.toString());
    }

    @Test
    public void testCloneOrphan() {
        Node original = new TestNode("http://original");
        original.attr("key", "val");
        Node child = new TestNode("http://child");
        original.addChildren(child);
        Node cloned = original.clone();
        assertNotSame(original, cloned);
        assertNull(cloned.parent());
        assertEquals(0, cloned.siblingIndex().intValue());
        assertEquals("http://original", cloned.baseUri());
        assertEquals("val", cloned.attr("key"));
        assertNotSame(original.attributes(), cloned.attributes());
        assertEquals(1, cloned.childNodes().size());
        Node clonedChild = cloned.childNode(0);
        assertNotSame(child, clonedChild);
        assertSame(cloned, clonedChild.parent());
        assertEquals("http://child", clonedChild.baseUri());
    }

    @Test
    public void testCloneWithParent() {
        Node parent = new TestNode("http://parent");
        Node original = new TestNode("http://original");
        original.setSiblingIndex(3);
        Node cloned = original.doClone(parent);
        assertSame(parent, cloned.parent());
        assertEquals(3, cloned.siblingIndex().intValue());
        assertEquals("http://original", cloned.baseUri());
    }

    @Test
    public void testEqualsSameObject() {
        Node node = new TestNode("http://test");
        assertTrue(node.equals(node));
    }

    @Test
    public void testEqualsDifferentObject() {
        Node node1 = new TestNode("http://test");
        Node node2 = new TestNode("http://test");
        assertFalse(node1.equals(node2));
    }

    @Test
    public void testHashCodeIncludesParent() {
        Node parent = new TestNode("http://parent");
        Node child = new TestNode("http://child");
        int hashWithoutParent = child.hashCode();
        parent.addChildren(child);
        assertNotEquals(hashWithoutParent, child.hashCode());
    }

    @Test
    public void testHashCodeIncludesAttributes() {
        Node node1 = new TestNode("http://test");
        node1.attr("key", "val");
        Node node2 = new TestNode("http://test");
        assertNotEquals(node1.hashCode(), node2.hashCode());
    }

    @Test
    public void testOuterHtmlVisitorTailNotCalledForTextNode() {
        // This test ensures that the outerHtmlTail is not called for a node with nodeName "#text"
        Node textNode = new TestNode() {
            @Override
            public String nodeName() {
                return "#text";
            }
        };
        String html = textNode.outerHtml();
        assertTrue(html.contains("testHead"));
        assertFalse(html.contains("testTail"));
    }
}
