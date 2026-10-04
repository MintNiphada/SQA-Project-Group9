package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.NodeVisitor;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class NodeTest {

    private static class ConcreteNode extends Node {
        public ConcreteNode() {
            super();
        }

        public ConcreteNode(String baseUri) {
            super(baseUri);
        }

        public ConcreteNode(String baseUri, Attributes attributes) {
            super(baseUri, attributes);
        }

        @Override
        public String nodeName() {
            return "concrete";
        }

        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("<concrete>");
        }

        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("</concrete>");
        }
    }

    @Test
    public void testConstructorsAndBaseUri() {
        ConcreteNode node1 = new ConcreteNode();
        Assert.assertNull(node1.baseUri());
        Assert.assertNull(node1.attributes());
        Assert.assertEquals(0, node1.childNodeSize());

        ConcreteNode node2 = new ConcreteNode("http://example.com/ ");
        Assert.assertEquals("http://example.com/", node2.baseUri());
        Assert.assertNotNull(node2.attributes());

        Attributes attrs = new Attributes();
        attrs.put("k", "v");
        ConcreteNode node3 = new ConcreteNode("http://example.com", attrs);
        Assert.assertEquals("http://example.com", node3.baseUri());
        Assert.assertEquals("v", node3.attr("k"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullUri() {
        new ConcreteNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullAttributes() {
        new ConcreteNode("http://example.com", null);
    }

    @Test
    public void testAttributesAndAbsUrl() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com/sub/");
        el.attr("href", "test.html");
        el.attr("other", "value");

        Assert.assertEquals("test.html", el.attr("href"));
        Assert.assertEquals("http://example.com/sub/test.html", el.attr("abs:href"));
        Assert.assertEquals("http://example.com/sub/test.html", el.attr("ABS:href"));
        Assert.assertEquals("", el.attr("nonexistent"));
        Assert.assertEquals("", el.attr("abs:nonexistent"));

        Assert.assertTrue(el.hasAttr("href"));
        Assert.assertTrue(el.hasAttr("abs:href"));
        Assert.assertTrue(el.hasAttr("other"));
        Assert.assertFalse(el.hasAttr("abs:other"));
        Assert.assertFalse(el.hasAttr("abs:missing"));

        el.removeAttr("other");
        Assert.assertFalse(el.hasAttr("other"));

        el.attr("absLink", "http://absolute.com/page");
        Assert.assertEquals("http://absolute.com/page", el.absUrl("absLink"));
        Assert.assertEquals("", el.absUrl("missing"));

        el.attr("badLink", "http:// invalid url ");
        Assert.assertEquals("", el.absUrl("badLink"));
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

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        Node node = new ConcreteNode("http://example.com");
        node.absUrl("");
    }

    @Test
    public void testSetBaseUri() {
        Element parent = new Element(Tag.valueOf("div"), "http://old.com");
        Element child = new Element(Tag.valueOf("p"), "http://old.com");
        parent.appendChild(child);

        parent.setBaseUri("http://new.com");
        Assert.assertEquals("http://new.com", parent.baseUri());
        Assert.assertEquals("http://new.com", child.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseUriNull() {
        Node node = new ConcreteNode("http://example.com");
        node.setBaseUri(null);
    }

    @Test
    public void testChildNodesOperations() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("span"), "");
        Element child2 = new Element(Tag.valueOf("b"), "");

        parent.appendChild(child1);
        parent.appendChild(child2);

        Assert.assertEquals(2, parent.childNodeSize());
        Assert.assertSame(child1, parent.childNode(0));
        Assert.assertSame(child2, parent.childNode(1));

        List<Node> children = parent.childNodes();
        Assert.assertEquals(2, children.size());

        Node[] childArray = parent.childNodesAsArray();
        Assert.assertEquals(2, childArray.length);
        Assert.assertSame(child1, childArray[0]);

        List<Node> copy = parent.childNodesCopy();
        Assert.assertEquals(2, copy.size());
        Assert.assertNotSame(child1, copy.get(0));
        Assert.assertEquals("span", copy.get(0).nodeName());

        Assert.assertSame(parent, child1.parent());
        Assert.assertSame(parent, child1.parentNode());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChildNodesUnmodifiable() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(new Element(Tag.valueOf("span"), ""));
        parent.childNodes().add(new Element(Tag.valueOf("p"), ""));
    }

    @Test
    public void testOwnerDocument() {
        Document doc = Jsoup.parse("<div><p>text</p></div>");
        Element div = doc.select("div").first();
        Element p = doc.select("p").first();
        TextNode text = (TextNode) p.childNode(0);

        Assert.assertSame(doc, doc.ownerDocument());
        Assert.assertSame(doc, div.ownerDocument());
        Assert.assertSame(doc, p.ownerDocument());
        Assert.assertSame(doc, text.ownerDocument());

        Element orphan = new Element(Tag.valueOf("span"), "");
        Assert.assertNull(orphan.ownerDocument());
    }

    @Test
    public void testRemoveAndReplace() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child1);
        parent.appendChild(child2);

        child1.remove();
        Assert.assertNull(child1.parent());
        Assert.assertEquals(1, parent.childNodeSize());
        Assert.assertSame(child2, parent.childNode(0));
        Assert.assertEquals(0, child2.siblingIndex());

        Element replacement = new Element(Tag.valueOf("b"), "");
        child2.replaceWith(replacement);
        Assert.assertNull(child2.parent());
        Assert.assertSame(parent, replacement.parent());
        Assert.assertEquals(0, replacement.siblingIndex());
        Assert.assertSame(replacement, parent.childNode(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveOrphanThrows() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        orphan.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithOrphanThrows() {
        Element orphan = new Element(Tag.valueOf("div"), "");
        orphan.replaceWith(new Element(Tag.valueOf("span"), ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNullThrows() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);
        child.replaceWith(null);
    }

    @Test
    public void testBeforeAndAfterNodes() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element mid = new Element(Tag.valueOf("span"), "");
        parent.appendChild(mid);

        Element before = new Element(Tag.valueOf("b"), "");
        Element after = new Element(Tag.valueOf("i"), "");

        mid.before(before);
        mid.after(after);

        Assert.assertEquals(3, parent.childNodeSize());
        Assert.assertSame(before, parent.childNode(0));
        Assert.assertSame(mid, parent.childNode(1));
        Assert.assertSame(after, parent.childNode(2));
        Assert.assertEquals(0, before.siblingIndex());
        Assert.assertEquals(1, mid.siblingIndex());
        Assert.assertEquals(2, after.siblingIndex());
    }

    @Test
    public void testBeforeAndAfterHtml() {
        Document doc = Jsoup.parse("<div><span>target</span></div>");
        Element span = doc.select("span").first();

        span.before("<p>before</p>");
        span.after("<b>after</b>");

        Assert.assertEquals("<div><p>before</p><span>target</span><b>after</b></div>", doc.body().html());
    }

    @Test
    public void testWrapAndUnwrap() {
        Document doc = Jsoup.parse("<div><span>target</span></div>");
        Element span = doc.select("span").first();

        span.wrap("<div class='outer'><div class='inner'></div></div>");
        Assert.assertEquals("<div><div class=\"outer\"><div class=\"inner\"><span>target</span></div></div></div>", doc.body().html());

        Node unwrappedChild = span.unwrap();
        Assert.assertNotNull(unwrappedChild);
        Assert.assertEquals("<div><div class=\"outer\"><div class=\"inner\">target</div></div></div>", doc.body().html());
    }

    @Test
    public void testWrapUnbalanced() {
        Document doc = Jsoup.parse("<div><span>target</span></div>");
        Element span = doc.select("span").first();
        span.wrap("<div class='w1'></div><div class='w2'></div>");
        Assert.assertEquals("<div><div class=\"w1\"><span>target</span><div class=\"w2\"></div></div></div>", doc.body().html());
    }

    @Test
    public void testWrapNonElementReturnsNull() {
        Document doc = Jsoup.parse("<div><span>target</span></div>");
        Element span = doc.select("span").first();
        Node wrapped = span.wrap("just plain text");
        Assert.assertNull(wrapped);
    }

    @Test
    public void testUnwrapEmptyElement() {
        Document doc = Jsoup.parse("<div><p></p></div>");
        Element p = doc.select("p").first();
        Node child = p.unwrap();
        Assert.assertNull(child);
        Assert.assertEquals("<div></div>", doc.body().html());
    }

    @Test
    public void testSiblingMethods() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("span"), "");
        Element child2 = new Element(Tag.valueOf("b"), "");
        Element child3 = new Element(Tag.valueOf("i"), "");

        parent.appendChild(child1);
        parent.appendChild(child2);
        parent.appendChild(child3);

        List<Node> siblings = child2.siblingNodes();
        Assert.assertEquals(2, siblings.size());
        Assert.assertSame(child1, siblings.get(0));
        Assert.assertSame(child3, siblings.get(1));

        Assert.assertNull(child1.previousSibling());
        Assert.assertSame(child1, child2.previousSibling());
        Assert.assertSame(child2, child3.previousSibling());

        Assert.assertSame(child2, child1.nextSibling());
        Assert.assertSame(child3, child2.nextSibling());
        Assert.assertNull(child3.nextSibling());

        Element orphan = new Element(Tag.valueOf("div"), "");
        Assert.assertTrue(orphan.siblingNodes().isEmpty());
        Assert.assertNull(orphan.previousSibling());
        Assert.assertNull(orphan.nextSibling());
    }

    @Test
    public void testAddChildrenEdgeCases() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("a"), "");
        Element child2 = new Element(Tag.valueOf("b"), "");
        Element child3 = new Element(Tag.valueOf("c"), "");

        parent.addChildren(child1, child3);
        parent.addChildren(1, child2);

        Assert.assertEquals(3, parent.childNodeSize());
        Assert.assertSame(child1, parent.childNode(0));
        Assert.assertSame(child2, parent.childNode(1));
        Assert.assertSame(child3, parent.childNode(2));

        Element anotherParent = new Element(Tag.valueOf("section"), "");
        anotherParent.appendChild(child2);

        Assert.assertEquals(2, parent.childNodeSize());
        Assert.assertEquals(1, anotherParent.childNodeSize());
        Assert.assertSame(child2, anotherParent.childNode(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildWrongParent() {
        Element parent1 = new Element(Tag.valueOf("div"), "");
        Element parent2 = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("a"), "");
        Element child2 = new Element(Tag.valueOf("b"), "");
        parent1.appendChild(child1);
        parent2.appendChild(child2);

        parent1.replaceChild(child2, new Element(Tag.valueOf("span"), ""));
    }

    @Test
    public void testTraverse() {
        Document doc = Jsoup.parse("<div><p>One</p></div>");
        final int[] counts = new int[2];
        doc.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                counts[0]++;
            }

            public void tail(Node node, int depth) {
                counts[1]++;
            }
        });
        Assert.assertTrue(counts[0] > 0);
        Assert.assertTrue(counts[1] > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraverseNullVisitor() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.traverse(null);
    }

    @Test
    public void testOuterHtmlAndToString() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "test");
        el.appendChild(new Element(Tag.valueOf("p"), "").text("hello"));

        Assert.assertEquals("<div id=\"test\">\n <p>hello</p>\n</div>", el.outerHtml());
        Assert.assertEquals(el.outerHtml(), el.toString());

        StringBuilder sb = new StringBuilder();
        el.indent(sb, 2, new Document("").outputSettings());
        Assert.assertEquals("\n    ", sb.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        el1.attr("class", "main");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        child1.text("content");
        el1.appendChild(child1);

        Element el2 = new Element(Tag.valueOf("div"), "http://different.com");
        el2.attr("class", "main");
        Element child2 = new Element(Tag.valueOf("p"), "http://different.com");
        child2.text("content");
        el2.appendChild(child2);

        Assert.assertEquals(el1, el1);
        Assert.assertEquals(el1, el2);
        Assert.assertEquals(el1.hashCode(), el2.hashCode());

        Assert.assertFalse(el1.equals(null));
        Assert.assertFalse(el1.equals("a string"));

        Element el3 = new Element(Tag.valueOf("div"), "");
        el3.attr("class", "other");
        Assert.assertFalse(el1.equals(el3));

        Element el4 = new Element(Tag.valueOf("div"), "");
        el4.attr("class", "main");
        Assert.assertFalse(el1.equals(el4));

        ConcreteNode emptyNode1 = new ConcreteNode();
        ConcreteNode emptyNode2 = new ConcreteNode();
        Assert.assertEquals(emptyNode1, emptyNode2);
        Assert.assertEquals(emptyNode1.hashCode(), emptyNode2.hashCode());
    }

    @Test
    public void testClone() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        root.attr("key", "val");
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        child.text("text");
        root.appendChild(child);

        Node clone = root.clone();
        Assert.assertNotSame(root, clone);
        Assert.assertNull(clone.parent());
        Assert.assertEquals(1, clone.childNodeSize());

        Node childClone = clone.childNode(0);
        Assert.assertNotSame(child, childClone);
        Assert.assertSame(clone, childClone.parent());
        Assert.assertEquals(root.outerHtml(), clone.outerHtml());

        childClone.attr("new", "attr");
        Assert.assertFalse(child.hasAttr("new"));
    }

    @Test
    public void testSetParentNode() {
        ConcreteNode parent1 = new ConcreteNode("http://example.com");
        ConcreteNode parent2 = new ConcreteNode("http://example.com");
        ConcreteNode child = new ConcreteNode("http://example.com");

        parent1.appendChild(child);
        Assert.assertSame(parent1, child.parent());

        child.setParentNode(parent2);
        Assert.assertSame(parent2, child.parent());
        Assert.assertEquals(0, parent1.childNodeSize());
    }
}
