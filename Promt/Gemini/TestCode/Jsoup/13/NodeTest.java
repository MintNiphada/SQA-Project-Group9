package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    private static class TestNode extends Node {
        private String name;

        TestNode() {
            super();
            this.name = "test";
        }

        TestNode(String baseUri) {
            super(baseUri);
            this.name = "test";
        }

        TestNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
            this.name = "test";
        }

        TestNode(String baseUri, Attributes attributes, String name) {
            super(baseUri, attributes);
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

    @Test
    public void testConstructors() {
        TestNode defaultNode = new TestNode();
        assertNull(defaultNode.baseUri());
        assertNull(defaultNode.attributes());
        assertTrue(defaultNode.childNodes().isEmpty());

        TestNode nodeWithUri = new TestNode("http://example.com");
        assertEquals("http://example.com", nodeWithUri.baseUri());
        assertNotNull(nodeWithUri.attributes());

        Attributes attrs = new Attributes();
        attrs.put("key", "value");
        TestNode fullNode = new TestNode("  http://example.com  ", attrs);
        assertEquals("http://example.com", fullNode.baseUri());
        assertEquals("value", fullNode.attr("key"));
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
    public void testAttributesHandling() {
        TestNode node = new TestNode("http://example.com");
        node.attr("key1", "val1");
        assertTrue(node.hasAttr("key1"));
        assertFalse(node.hasAttr("key2"));
        assertEquals("val1", node.attr("key1"));
        assertEquals("", node.attr("nonexistent"));

        node.removeAttr("key1");
        assertFalse(node.hasAttr("key1"));
        assertEquals("", node.attr("key1"));

        node.setBaseUri("http://example.com/sub/");
        assertEquals("http://example.com/sub/", node.baseUri());
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

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        TestNode node = new TestNode("http://example.com");
        node.setBaseUri(null);
    }

    @Test
    public void testAbsUrlResolution() {
        TestNode node = new TestNode("http://example.com/path/index.html");
        node.attr("href", "rel.html");
        node.attr("absHref", "http://other.com/page.html");
        node.attr("query", "?param=value");

        assertEquals("http://example.com/path/rel.html", node.absUrl("href"));
        assertEquals("http://example.com/path/rel.html", node.attr("abs:href"));
        assertEquals("http://example.com/path/rel.html", node.attr("abs:HREF"));
        assertEquals("http://other.com/page.html", node.absUrl("absHref"));
        assertEquals("http://example.com/path/index.html?param=value", node.absUrl("query"));
        assertEquals("", node.absUrl("missing"));

        // Invalid base URI, but valid absolute attribute value
        TestNode invalidBaseNode = new TestNode("invalid-uri");
        invalidBaseNode.attr("href", "http://example.com/absolute.html");
        assertEquals("http://example.com/absolute.html", invalidBaseNode.absUrl("href"));

        // Invalid base URI and invalid relative URL
        invalidBaseNode.attr("rel", "not-a-valid-absolute-url");
        assertEquals("", invalidBaseNode.absUrl("rel"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlNullKey() {
        TestNode node = new TestNode("http://example.com");
        node.absUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        TestNode node = new TestNode("http://example.com");
        node.absUrl("");
    }

    @Test
    public void testChildrenAndHierarchy() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");

        parent.addChildren(child1, child2);

        assertEquals(2, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));

        Node[] childArray = parent.childNodesAsArray();
        assertEquals(2, childArray.length);
        assertSame(child1, childArray[0]);
        assertSame(child2, childArray[1]);

        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());

        assertEquals(0, child1.siblingIndex());
        assertEquals(1, child2.siblingIndex());

        assertSame(child2, child1.nextSibling());
        assertNull(child2.nextSibling());

        assertNull(child1.previousSibling());
        assertSame(child1, child2.previousSibling());

        List<Node> siblings = child1.siblingNodes();
        assertEquals(2, siblings.size());
        assertSame(child1, siblings.get(0));
        assertSame(child2, siblings.get(1));
    }

    @Test
    public void testAddChildrenAtIndex() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode child3 = new TestNode("http://example.com");

        parent.addChildren(child1, child3);
        parent.addChildren(1, child2);

        assertEquals(3, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertSame(child3, parent.childNode(2));

        assertEquals(0, child1.siblingIndex());
        assertEquals(1, child2.siblingIndex());
        assertEquals(2, child3.siblingIndex());
    }

    @Test
    public void testReparentChild() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");

        parent1.addChildren(child);
        assertEquals(1, parent1.childNodes().size());
        assertSame(parent1, child.parent());

        parent2.addChildren(child);
        assertEquals(0, parent1.childNodes().size());
        assertEquals(1, parent2.childNodes().size());
        assertSame(parent2, child.parent());
    }

    @Test
    public void testSetParentNode() {
        TestNode parent1 = new TestNode("http://example.com");
        TestNode parent2 = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");

        parent1.addChildren(child);
        child.setParentNode(parent2);

        assertEquals(0, parent1.childNodes().size());
        assertSame(parent2, child.parent());
    }

    @Test
    public void testReplaceChildAndReplaceWith() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");
        TestNode replacement = new TestNode("http://example.com");

        parent.addChildren(child1, child2);
        parent.replaceChild(child1, replacement);

        assertEquals(2, parent.childNodes().size());
        assertSame(replacement, parent.childNode(0));
        assertNull(child1.parent());
        assertSame(parent, replacement.parent());
        assertEquals(0, replacement.siblingIndex());

        TestNode child3 = new TestNode("http://example.com");
        child2.replaceWith(child3);
        assertEquals(2, parent.childNodes().size());
        assertSame(child3, parent.childNode(1));
        assertNull(child2.parent());
        assertSame(parent, child3.parent());
        assertEquals(1, child3.siblingIndex());
    }

    @Test
    public void testRemove() {
        TestNode parent = new TestNode("http://example.com");
        TestNode child1 = new TestNode("http://example.com");
        TestNode child2 = new TestNode("http://example.com");

        parent.addChildren(child1, child2);
        child1.remove();

        assertEquals(1, parent.childNodes().size());
        assertSame(child2, parent.childNode(0));
        assertEquals(0, child2.siblingIndex());
        assertNull(child1.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveOrphanThrows() {
        TestNode orphan = new TestNode("http://example.com");
        orphan.remove();
    }

    @Test
    public void testOwnerDocument() {
        Document doc = Jsoup.parse("<div><p>text</p></div>", "http://example.com");
        Element div = doc.select("div").first();
        Element p = doc.select("p").first();
        Node text = p.childNode(0);

        assertSame(doc, doc.ownerDocument());
        assertSame(doc, div.ownerDocument());
        assertSame(doc, p.ownerDocument());
        assertSame(doc, text.ownerDocument());

        TestNode orphan = new TestNode("http://example.com");
        assertNull(orphan.ownerDocument());
    }

    @Test
    public void testBeforeAndAfterWithNode() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element mid = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(mid);

        Element beforeNode = new Element(Tag.valueOf("b"), "http://example.com");
        Element afterNode = new Element(Tag.valueOf("i"), "http://example.com");

        mid.before(beforeNode);
        mid.after(afterNode);

        assertEquals(3, parent.childNodes().size());
        assertSame(beforeNode, parent.childNode(0));
        assertSame(mid, parent.childNode(1));
        assertSame(afterNode, parent.childNode(2));
    }

    @Test
    public void testBeforeAndAfterWithHtml() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element mid = new Element(Tag.valueOf("span"), "http://example.com");
        parent.appendChild(mid);

        mid.before("<b>bold</b>");
        mid.after("<i>italic</i>");

        assertEquals(3, parent.childNodes().size());
        assertEquals("b", ((Element) parent.childNode(0)).tagName());
        assertSame(mid, parent.childNode(1));
        assertEquals("i", ((Element) parent.childNode(2)).tagName());
    }

    @Test
    public void testWrapSingleElement() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element p = doc.select("p").first();
        Node wrapped = p.wrap("<div class='wrapper'></div>");
        assertSame(p, wrapped);
        assertEquals("<div class=\"wrapper\">\n <p>Hello</p>\n</div>", doc.body().child(0).outerHtml());
    }

    @Test
    public void testWrapDeepElement() {
        Document doc = Jsoup.parse("<div><span>Text</span></div>");
        Element span = doc.select("span").first();
        span.wrap("<div class='one'><div class='two'></div></div>");
        assertEquals("<div>\n <div class=\"one\">\n  <div class=\"two\">\n   <span>Text</span>\n  </div>\n </div>\n</div>", doc.body().html());
    }

    @Test
    public void testWrapMultipleElementsRemainder() {
        Document doc = Jsoup.parse("<div><span>Text</span></div>");
        Element span = doc.select("span").first();
        span.wrap("<div></div><p>rem</p>");
        assertEquals("<div>\n <div>\n  <span>Text</span>\n  <p>rem</p>\n </div>\n</div>", doc.body().html());
    }

    @Test
    public void testWrapWithInvalidHtmlReturnsNull() {
        Document doc = Jsoup.parse("<div><span>Text</span></div>");
        Element span = doc.select("span").first();
        Node res = span.wrap("");
        assertNull(res);
    }

    @Test
    public void testSiblingNavigationEdgeCases() {
        TestNode orphan = new TestNode("http://example.com");
        assertNull(orphan.nextSibling());

        TestNode parent = new TestNode("http://example.com");
        TestNode singleChild = new TestNode("http://example.com");
        parent.addChildren(singleChild);

        assertNull(singleChild.nextSibling());
        assertNull(singleChild.previousSibling());
    }

    @Test
    public void testOuterHtmlAndToString() {
        TestNode node = new TestNode("http://example.com");
        assertEquals("<test></test>", node.outerHtml());
        assertEquals("<test></test>", node.toString());

        TestNode parent = new TestNode("http://example.com");
        TestNode child = new TestNode("http://example.com");
        parent.addChildren(child);
        assertEquals("<test><test></test></test>", parent.outerHtml());
    }

    @Test
    public void testOuterHtmlWithTextNodeVisitor() {
        Document doc = new Document("http://example.com");
        Element p = doc.createElement("p");
        p.appendText("Hello World");
        doc.body().appendChild(p);
        assertTrue(doc.outerHtml().contains("Hello World"));
    }

    @Test
    public void testIndent() {
        TestNode node = new TestNode("http://example.com");
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings settings = new Document.OutputSettings();
        settings.indentAmount(2);
        node.indent(sb, 2, settings);
        assertEquals("\n    ", sb.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        TestNode node1 = new TestNode("http://example.com");
        TestNode node2 = new TestNode("http://example.com");

        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(node2));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("string"));

        assertEquals(node1.hashCode(), node1.hashCode());

        TestNode defaultNode = new TestNode();
        assertEquals(0, defaultNode.hashCode());

        TestNode parent = new TestNode("http://example.com");
        parent.addChildren(node1);
        assertTrue(node1.hashCode() != 0);
    }

    @Test
    public void testClone() {
        TestNode parent = new TestNode("http://example.com");
        parent.attr("key", "parentVal");
        TestNode child1 = new TestNode("http://example.com");
        child1.attr("childKey", "childVal");
        TestNode child2 = new TestNode("http://example.com");

        parent.addChildren(child1, child2);

        TestNode clone = (TestNode) parent.clone();

        assertNotSame(parent, clone);
        assertNull(clone.parent());
        assertEquals(0, clone.siblingIndex());
        assertEquals("parentVal", clone.attr("key"));
        assertEquals(2, clone.childNodes().size());

        TestNode clonedChild1 = (TestNode) clone.childNode(0);
        assertNotSame(child1, clonedChild1);
        assertSame(clone, clonedChild1.parent());
        assertEquals(0, clonedChild1.siblingIndex());
        assertEquals("childVal", clonedChild1.attr("childKey"));

        // Mutating clone does not mutate original
        clonedChild1.attr("childKey", "newVal");
        assertEquals("childVal", child1.attr("childKey"));
        assertEquals("newVal", clonedChild1.attr("childKey"));
    }
}
