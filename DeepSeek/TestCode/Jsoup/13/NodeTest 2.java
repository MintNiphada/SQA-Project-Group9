package org.jsoup.nodes;

import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    private TestNode node;
    private TestNode parent;
    private TestNode sibling1;
    private TestNode sibling2;

    // Concrete implementation of Node for testing purposes
    private static class TestNode extends Node {
        private String name;

        public TestNode(String baseUri, Attributes attributes, String name) {
            super(baseUri, attributes);
            this.name = name;
        }

        public TestNode(String baseUri, String name) {
            super(baseUri);
            this.name = name;
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<").append(name).append(">");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</").append(name).append(">");
        }
    }

    @Before
    public void setUp() {
        parent = new TestNode("http://example.com/", "parent");
        node = new TestNode("http://example.com/", "child");
        sibling1 = new TestNode("http://example.com/", "sib1");
        sibling2 = new TestNode("http://example.com/", "sib2");

        // Setup hierarchy: parent -> [sibling1, node, sibling2]
        parent.addChildren(sibling1, node, sibling2);
    }

    @Test
    public void testConstructorWithNullBaseUri() {
        try {
            new TestNode(null, new Attributes(), "test");
            fail("Expected IllegalArgumentException for null baseUri");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testConstructorWithNullAttributes() {
        try {
            new TestNode("http://example.com/", null, "test");
            fail("Expected IllegalArgumentException for null attributes");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testConstructorTrimsBaseUri() {
        TestNode n = new TestNode("  http://example.com/  ", "test");
        assertEquals("http://example.com/", n.baseUri());
    }

    @Test
    public void testAttrWithNullKey() {
        try {
            node.attr(null);
            fail("Expected IllegalArgumentException for null attribute key");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAttrMissing() {
        assertEquals("", node.attr("nonexistent"));
    }

    @Test
    public void testAttrPresent() {
        node.attr("key", "value");
        assertEquals("value", node.attr("key"));
    }

    @Test
    public void testAttrAbsPrefix() {
        node.attr("href", "/path");
        // abs:href should resolve relative to baseUri
        String absUrl = node.attr("abs:href");
        assertEquals("http://example.com/path", absUrl);
    }

    @Test
    public void testAttrAbsPrefixWithAbsoluteUrl() {
        node.attr("href", "http://other.com/path");
        String absUrl = node.attr("abs:href");
        assertEquals("http://other.com/path", absUrl);
    }

    @Test
    public void testHasAttr() {
        assertFalse(node.hasAttr("key"));
        node.attr("key", "value");
        assertTrue(node.hasAttr("key"));
    }

    @Test
    public void testHasAttrWithNullKey() {
        try {
            node.hasAttr(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testRemoveAttr() {
        node.attr("key", "value");
        assertTrue(node.hasAttr("key"));
        node.removeAttr("key");
        assertFalse(node.hasAttr("key"));
    }

    @Test
    public void testRemoveAttrWithNullKey() {
        try {
            node.removeAttr(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testSetBaseUri() {
        node.setBaseUri("http://new.com/");
        assertEquals("http://new.com/", node.baseUri());
    }

    @Test
    public void testSetBaseUriWithNull() {
        try {
            node.setBaseUri(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAbsUrlWithEmptyKey() {
        try {
            node.absUrl("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAbsUrlMissingAttribute() {
        assertEquals("", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlRelative() {
        node.attr("href", "page.html");
        assertEquals("http://example.com/page.html", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlAbsolute() {
        node.attr("href", "http://absolute.com/page.html");
        assertEquals("http://absolute.com/page.html", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlWithQueryStart() {
        node.attr("href", "?query=1");
        // Should append to base path
        assertEquals("http://example.com/?query=1", node.absUrl("href"));
    }

    @Test
    public void testChildNode() {
        assertEquals(sibling1, parent.childNode(0));
        assertEquals(node, parent.childNode(1));
        assertEquals(sibling2, parent.childNode(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNodeOutOfBounds() {
        parent.childNode(5);
    }

    @Test
    public void testChildNodesUnmodifiable() {
        List<Node> children = parent.childNodes();
        try {
            children.add(new TestNode("http://example.com/", "new"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testParent() {
        assertEquals(parent, node.parent());
        assertNull(sibling1.parent().parent()); // parent has no parent
    }

    @Test
    public void testOwnerDocument() {
        // In this simple test setup, there is no Document instance, so it should return null
        // unless we create a Document. Let's test with a Document.
        Document doc = new Document("http://example.com/");
        Element body = new Element("body", doc);
        doc.appendChild(body);
        
        TestNode n = new TestNode("http://example.com/", "test");
        body.appendChild(n);
        
        assertEquals(doc, n.ownerDocument());
    }

    @Test
    public void testRemove() {
        assertEquals(3, parent.childNodes().size());
        node.remove();
        assertEquals(2, parent.childNodes().size());
        assertNull(node.parent());
    }

    @Test
    public void testRemoveWithoutParent() {
        TestNode orphan = new TestNode("http://example.com/", "orphan");
        try {
            orphan.remove();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testBeforeNode() {
        node.before(sibling2); // Move sibling2 before node
        // Original: sib1, node, sib2
        // After: sib1, sib2, node
        assertEquals(sibling1, parent.childNode(0));
        assertEquals(sibling2, parent.childNode(1));
        assertEquals(node, parent.childNode(2));
    }

    @Test
    public void testBeforeNodeWithNull() {
        try {
            node.before((Node) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAfterNode() {
        node.after(sibling1); // Move sibling1 after node
        // Original: sib1, node, sib2
        // After: node, sib1, sib2
        assertEquals(node, parent.childNode(0));
        assertEquals(sibling1, parent.childNode(1));
        assertEquals(sibling2, parent.childNode(2));
    }

    @Test
    public void testAfterNodeWithNull() {
        try {
            node.after((Node) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testBeforeHtml() {
        node.before("<div>test</div>");
        // Should insert a new node before 'node'
        assertEquals(4, parent.childNodes().size());
        assertEquals("div", parent.childNode(1).nodeName());
    }

    @Test
    public void testAfterHtml() {
        node.after("<div>test</div>");
        assertEquals(4, parent.childNodes().size());
        assertEquals("div", parent.childNode(2).nodeName());
    }

    @Test
    public void testWrap() {
        node.wrap("<div class='wrapper'></div>");
        // node should now be child of div, which is child of parent
        assertEquals(1, parent.childNodes().size());
        Node wrapper = parent.childNode(0);
        assertEquals("div", wrapper.nodeName());
        assertEquals(1, wrapper.childNodes().size());
        assertEquals(node, wrapper.childNode(0));
    }

    @Test
    public void testWrapWithEmptyHtml() {
        try {
            node.wrap("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testReplaceWith() {
        TestNode replacement = new TestNode("http://example.com/", "replacement");
        node.replaceWith(replacement);
        
        assertEquals(3, parent.childNodes().size());
        assertEquals(replacement, parent.childNode(1));
        assertNull(node.parent());
    }

    @Test
    public void testReplaceWithNull() {
        try {
            node.replaceWith(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testNextSibling() {
        assertEquals(sibling2, node.nextSibling());
        assertNull(sibling2.nextSibling());
    }

    @Test
    public void testPreviousSibling() {
        assertEquals(sibling1, node.previousSibling());
        assertNull(sibling1.previousSibling());
    }

    @Test
    public void testSiblingIndex() {
        assertEquals(1, node.siblingIndex());
        assertEquals(0, sibling1.siblingIndex());
        assertEquals(2, sibling2.siblingIndex());
    }

    @Test
    public void testEquals() {
        TestNode n1 = new TestNode("http://example.com/", "test");
        TestNode n2 = new TestNode("http://example.com/", "test");
        
        assertNotEquals(n1, n2); // Node equals is always false unless same instance
        assertEquals(n1, n1);
    }

    @Test
    public void testHashCode() {
        TestNode n1 = new TestNode("http://example.com/", "test");
        TestNode n2 = new TestNode("http://example.com/", "test");
        
        // Hash code depends on parent and attributes. 
        // Both have null parent and empty attributes.
        assertEquals(n1.hashCode(), n2.hashCode());
        
        n1.attr("key", "value");
        assertNotEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    public void testClone() {
        node.attr("key", "value");
        Node clone = node.clone();
        
        assertNotSame(node, clone);
        assertEquals(node.nodeName(), clone.nodeName());
        assertEquals(node.baseUri(), clone.baseUri());
        assertEquals("value", clone.attr("key"));
        assertNull(clone.parent()); // Clone is orphan
        assertEquals(0, clone.siblingIndex());
    }

    @Test
    public void testCloneWithChildren() {
        TestNode child = new TestNode("http://example.com/", "child");
        node.addChildren(child);
        
        Node clone = node.clone();
        assertEquals(1, clone.childNodes().size());
        assertNotSame(child, clone.childNode(0));
        assertEquals(child.nodeName(), clone.childNode(0).nodeName());
    }

    @Test
    public void testToString() {
        // Relies on outerHtml which relies on OutputSettings
        // Just ensure it doesn't crash and returns something
        String s = node.toString();
        assertNotNull(s);
        assertTrue(s.contains("child"));
    }
}
