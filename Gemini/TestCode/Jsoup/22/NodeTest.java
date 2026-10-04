package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NodeTest {

    private static class ConcreteNode extends Node {
        private String name;

        public ConcreteNode(String name) {
            super("http://example.com/", new Attributes());
            this.name = name;
        }

        public ConcreteNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
            this.name = "concrete";
        }

        public ConcreteNode(String baseUri, String name) {
            super(baseUri);
            this.name = name;
        }

        public ConcreteNode() {
            super();
            this.name = "default";
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            indent(accum, depth, out);
            accum.append("<").append(name).append(">");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</").append(name).append(">");
        }
    }

    @Test
    public void testConstructors() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        ConcreteNode node1 = new ConcreteNode("  http://example.com/sub  ", attrs);
        assertEquals("http://example.com/sub", node1.baseUri());
        assertEquals("val", node1.attr("key"));
        assertEquals(0, node1.childNodes().size());

        ConcreteNode node2 = new ConcreteNode("http://example.com/", "test");
        assertEquals("http://example.com/", node2.baseUri());
        assertNotNull(node2.attributes());

        ConcreteNode node3 = new ConcreteNode();
        assertNull(node3.baseUri());
        assertNull(node3.attributes());
        assertEquals(0, node3.childNodes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullBaseUri() {
        new ConcreteNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAttributes() {
        new ConcreteNode("http://example.com/", null);
    }

    @Test
    public void testAttributesAndAbsUrl() {
        Document doc = Jsoup.parse("<div id='d1' data-src='path/file.html' href='foo.html?bar=1'></div>", "http://example.com/dir/");
        Element div = doc.getElementById("d1");

        assertEquals("path/file.html", div.attr("data-src"));
        assertEquals("", div.attr("nonexistent"));

        // Abs url via attr("abs:key")
        assertEquals("http://example.com/dir/path/file.html", div.attr("abs:data-src"));
        assertEquals("http://example.com/dir/foo.html?bar=1", div.attr("abs:href"));
        assertEquals("", div.attr("abs:nonexistent"));

        assertTrue(div.hasAttr("data-src"));
        assertTrue(div.hasAttr("abs:data-src"));
        assertFalse(div.hasAttr("abs:nonexistent"));
        assertFalse(div.hasAttr("nonexistent"));

        // Test with relative URL starting with '?'
        div.attr("query", "?test=123");
        assertEquals("http://example.com/dir/?test=123", div.absUrl("query"));

        // Test with already absolute URL
        div.attr("absLink", "https://other.org/index.html");
        assertEquals("https://other.org/index.html", div.absUrl("absLink"));

        // Test malformed base URI falling back to absolute relUrl
        ConcreteNode malformedBaseNode = new ConcreteNode("not a valid url", "node");
        malformedBaseNode.attr("absLink", "http://jsoup.org");
        malformedBaseNode.attr("relLink", "relative.html");
        assertEquals("http://jsoup.org", malformedBaseNode.absUrl("absLink"));
        assertEquals("", malformedBaseNode.absUrl("relLink"));
        assertEquals("", malformedBaseNode.absUrl("nonexistent"));

        // Test attr modification and removal
        div.attr("title", "myTitle");
        assertEquals("myTitle", div.attr("title"));
        div.removeAttr("title");
        assertFalse(div.hasAttr("title"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        Node node = new ConcreteNode("http://example.com/", "test");
        node.attr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasAttrNullKey() {
        Node node = new ConcreteNode("http://example.com/", "test");
        node.hasAttr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAttrNullKey() {
        Node node = new ConcreteNode("http://example.com/", "test");
        node.removeAttr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        Node node = new ConcreteNode("http://example.com/", "test");
        node.absUrl("");
    }

    @Test
    public void testSetBaseUri() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>", "http://example.com/");
        Element div = doc.select("div").first();
        Element span = doc.select("span").first();

        assertEquals("http://example.com/", span.baseUri());
        div.setBaseUri("http://newuri.com/");
        assertEquals("http://newuri.com/", div.baseUri());
        assertEquals("http://newuri.com/", span.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        Node node = new ConcreteNode("http://example.com/", "test");
        node.setBaseUri(null);
    }

    @Test
    public void testOwnerDocument() {
        Document doc = Jsoup.parse("<p>One</p>");
        Element p = doc.select("p").first();
        TextNode text = (TextNode) p.childNode(0);

        assertSame(doc, doc.ownerDocument());
        assertSame(doc, p.ownerDocument());
        assertSame(doc, text.ownerDocument());

        ConcreteNode orphan = new ConcreteNode("http://example.com/", "orphan");
        assertNull(orphan.ownerDocument());
    }

    @Test
    public void testChildNodesAndArray() {
        Element div = new Element(Tag.valueOf("div"), "");
        TextNode child1 = new TextNode("1", "");
        TextNode child2 = new TextNode("2", "");

        div.appendChild(child1);
        div.appendChild(child2);

        assertEquals(2, div.childNodes().size());
        assertSame(child1, div.childNode(0));
        assertSame(child2, div.childNode(1));

        Node[] array = div.childNodesAsArray();
        assertEquals(2, array.length);
        assertSame(child1, array[0]);
        assertSame(child2, array[1]);

        try {
            div.childNodes().add(new TextNode("3", ""));
            fail("childNodes list should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    public void testRemoveAndReplaceWith() {
        Document doc = Jsoup.parse("<div><p>1</p><p id='target'>2</p><p>3</p></div>");
        Element p2 = doc.getElementById("target");
        Element div = doc.select("div").first();

        Element span = new Element(Tag.valueOf("span"), "");
        span.text("replacement");
        p2.replaceWith(span);

        assertEquals("<div><p>1</p><span>replacement</span><p>3</p></div>", cleanHtml(div));
        assertNull(p2.parent());

        span.remove();
        assertEquals("<div><p>1</p><p>3</p></div>", cleanHtml(div));
        assertNull(span.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithoutParent() {
        Node node = new ConcreteNode("http://example.com/", "orphan");
        node.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNull() {
        Document doc = Jsoup.parse("<p>Hello</p>");
        Element p = doc.select("p").first();
        p.replaceWith(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithOrphanTarget() {
        Node orphan = new ConcreteNode("http://example.com/", "orphan");
        Node replacement = new ConcreteNode("http://example.com/", "replacement");
        orphan.replaceWith(replacement);
    }

    @Test
    public void testBeforeAndAfterWithNode() {
        Document doc = Jsoup.parse("<div><p id='m'>Middle</p></div>");
        Element m = doc.getElementById("m");
        Element div = doc.select("div").first();

        Element beforeNode = new Element(Tag.valueOf("b"), "");
        beforeNode.text("First");
        m.before(beforeNode);

        Element afterNode = new Element(Tag.valueOf("i"), "");
        afterNode.text("Last");
        m.after(afterNode);

        assertEquals("<div><b>First</b><p id=\"m\">Middle</p><i>Last</i></div>", cleanHtml(div));
    }

    @Test
    public void testBeforeAndAfterWithStringHtml() {
        Document doc = Jsoup.parse("<div><p id='m'>Middle</p></div>");
        Element m = doc.getElementById("m");
        Element div = doc.select("div").first();

        m.before("<b>Before</b>");
        m.after("<i>After</i>");

        assertEquals("<div><b>Before</b><p id=\"m\">Middle</p><i>After</i></div>", cleanHtml(div));
    }

    @Test
    public void testSiblingsNavigation() {
        Document doc = Jsoup.parse("<div><p id='1'>1</p><p id='2'>2</p><p id='3'>3</p></div>");
        Element p1 = doc.getElementById("1");
        Element p2 = doc.getElementById("2");
        Element p3 = doc.getElementById("3");

        assertEquals(0, p1.siblingIndex());
        assertEquals(1, p2.siblingIndex());
        assertEquals(2, p3.siblingIndex());

        assertNull(p1.previousSibling());
        assertSame(p2, p1.nextSibling());

        assertSame(p1, p2.previousSibling());
        assertSame(p3, p2.nextSibling());

        assertSame(p2, p3.previousSibling());
        assertNull(p3.nextSibling());

        List<Node> siblings = p1.siblingNodes();
        assertEquals(3, siblings.size());

        ConcreteNode orphan = new ConcreteNode("http://example.com/", "orphan");
        assertNull(orphan.nextSibling());
    }

    @Test
    public void testWrap() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element p = doc.select("p").first();
        Element div = doc.select("div").first();

        Node wrapped = p.wrap("<div class='outer'><div class='inner'></div></div><span class='rem'>Extra</span>");
        assertSame(p, wrapped);
        assertEquals("<div><div class=\"outer\"><div class=\"inner\"><p>Hello</p></div><span class=\"rem\">Extra</span></div></div>", cleanHtml(div));

        // Wrap with non-element (e.g. comment/empty) returns null
        Node noop = p.wrap("<!-- just comment -->");
        assertNull(noop);
    }

    @Test
    public void testUnwrap() {
        Document doc = Jsoup.parse("<div>One <span>Two <b>Three</b></span> Four</div>");
        Element span = doc.select("span").first();
        Element div = doc.select("div").first();

        Node firstChild = span.unwrap();
        assertNotNull(firstChild);
        assertEquals("Two ", ((TextNode) firstChild).getWholeText());
        assertEquals("<div>One Two <b>Three</b> Four</div>", cleanHtml(div));

        // Unwrap empty node
        Element emptySpan = new Element(Tag.valueOf("span"), "");
        div.appendChild(emptySpan);
        Node unwrapResult = emptySpan.unwrap();
        assertNull(unwrapResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnwrapWithoutParent() {
        Node orphan = new ConcreteNode("http://example.com/", "orphan");
        orphan.unwrap();
    }

    @Test
    public void testAddChildrenAtIndex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("span"), "");
        Element child2 = new Element(Tag.valueOf("b"), "");
        parent.addChildren(child1, child2);

        assertEquals(2, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertEquals(0, child1.siblingIndex());
        assertEquals(1, child2.siblingIndex());

        Element child3 = new Element(Tag.valueOf("i"), "");
        parent.addChildren(1, child3);

        assertEquals(3, parent.childNodes().size());
        assertSame(child1, parent.childNode(0));
        assertSame(child3, parent.childNode(1));
        assertSame(child2, parent.childNode(2));
        assertEquals(0, child1.siblingIndex());
        assertEquals(1, child3.siblingIndex());
        assertEquals(2, child2.siblingIndex());
    }

    @Test
    public void testReparentExistingChild() {
        Element parent1 = new Element(Tag.valueOf("div"), "");
        Element parent2 = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("span"), "");

        parent1.appendChild(child);
        assertSame(parent1, child.parent());
        assertEquals(1, parent1.childNodes().size());

        parent2.appendChild(child);
        assertSame(parent2, child.parent());
        assertEquals(0, parent1.childNodes().size());
        assertEquals(1, parent2.childNodes().size());
    }

    @Test
    public void testTraverse() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        final List<String> heads = new ArrayList<String>();
        final List<String> tails = new ArrayList<String>();

        doc.body().traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                heads.add(node.nodeName() + ":" + depth);
            }

            public void tail(Node node, int depth) {
                tails.add(node.nodeName() + ":" + depth);
            }
        });

        assertTrue(heads.contains("body:0"));
        assertTrue(heads.contains("div:1"));
        assertTrue(heads.contains("p:2"));
        assertTrue(heads.contains("span:3"));
        assertTrue(heads.contains("#text:4"));

        assertTrue(tails.contains("body:0"));
        assertTrue(tails.contains("div:1"));
        assertTrue(tails.contains("p:2"));
        assertTrue(tails.contains("span:3"));
    }

    @Test
    public void testOuterHtmlAndToString() {
        ConcreteNode node = new ConcreteNode("custom");
        ConcreteNode child = new ConcreteNode("child");
        node.addChildren(child);

        String html = node.outerHtml();
        assertEquals("<custom>\n <child></child></custom>", html);
        assertEquals(html, node.toString());

        // Test with TextNode to cover tail skipping for #text in OuterHtmlVisitor
        Element elem = new Element(Tag.valueOf("p"), "");
        elem.text("Text");
        assertEquals("<p>Text</p>", elem.outerHtml());
    }

    @Test
    public void testEqualsAndHashCode() {
        ConcreteNode node1 = new ConcreteNode("node1");
        ConcreteNode node2 = new ConcreteNode("node2");

        assertTrue(node1.equals(node1));
        assertFalse(node1.equals(node2));
        assertFalse(node1.equals(null));
        assertFalse(node1.equals("Some String"));

        ConcreteNode emptyNode = new ConcreteNode();
        assertNotEquals(0, emptyNode.hashCode());
        assertNotEquals(emptyNode.hashCode(), node1.hashCode());

        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(node1);
        assertNotEquals(0, node1.hashCode());
    }

    @Test
    public void testClone() {
        Document doc = Jsoup.parse("<div id='d'><p class='c'>Hello <b>World</b></p></div>", "http://example.com/");
        Element div = doc.getElementById("d");
        Element clone = div.clone();

        assertNotSame(div, clone);
        assertNull(clone.parent());
        assertEquals(0, clone.siblingIndex());
        assertEquals("http://example.com/", clone.baseUri());
        assertEquals(div.attributes().size(), clone.attributes().size());
        assertEquals(div.childNodes().size(), clone.childNodes().size());
        assertEquals(cleanHtml(div), cleanHtml(clone));

        // Deep modifications check
        Element cloneP = (Element) clone.childNode(0);
        Element origP = (Element) div.childNode(0);
        assertNotSame(origP, cloneP);
        assertSame(clone, cloneP.parent());

        cloneP.attr("class", "modified");
        assertEquals("c", origP.attr("class"));
        assertEquals("modified", cloneP.attr("class"));

        // Clone node without attributes or children
        ConcreteNode emptyNode = new ConcreteNode();
        ConcreteNode emptyClone = (ConcreteNode) emptyNode.clone();
        assertNull(emptyClone.attributes());
        assertEquals(0, emptyClone.childNodes().size());
    }

    @Test
    public void testSetParentNodeReplacesExisting() {
        Element p1 = new Element(Tag.valueOf("div"), "");
        Element p2 = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("span"), "");

        p1.appendChild(child);
        assertEquals(1, p1.childNodes().size());
        assertSame(p1, child.parent());

        child.setParentNode(p2);
        assertEquals(0, p1.childNodes().size());
        assertSame(p2, child.parent());
    }

    private static String cleanHtml(Node node) {
        return node.outerHtml().replaceAll("[\\r\\n]+", "").replaceAll(">\\s+<", "><").trim();
    }
}
