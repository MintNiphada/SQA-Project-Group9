package org.jsoup.nodes;

import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.NodeTraversor;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import static org.junit.Assert.*;

public class NodeTest {
    private TestNode node;
    private Attributes attrs;
    private Document doc;

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
            return "testnode";
        }
        @Override
        void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("head");
        }
        @Override
        void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append("tail");
        }
    }

    @Before
    public void setUp() {
        attrs = new Attributes();
        attrs.put("key1", "val1");
        attrs.put("href", "page.html");
        node = new TestNode("http://example.com", attrs);
        doc = new Document("http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorNullBaseUri() {
        new TestNode(null, new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorNullAttributes() {
        new TestNode("http://example.com", null);
    }

    @Test
    public void constructorStringOnly() {
        TestNode n = new TestNode("http://example.com");
        assertEquals("http://example.com", n.baseUri());
        assertNotNull(n.attributes());
        assertTrue(n.attributes().asList().isEmpty());
    }

    @Test
    public void defaultConstructor() {
        TestNode n = new TestNode();
        assertEquals(0, n.childNodes().size());
        assertNull(n.attributes());
    }

    @Test
    public void nodeName() {
        assertEquals("testnode", node.nodeName());
    }

    @Test
    public void attrGetExistingKey() {
        assertEquals("val1", node.attr("key1"));
    }

    @Test
    public void attrGetNonExistingKey() {
        assertEquals("", node.attr("noSuchKey"));
    }

    @Test
    public void attrGetAbsPrefixSuccess() {
        node.attr("href", "http://absolute.com");
        node.baseUri = "http://example.com";
        String result = node.attr("abs:href");
        assertEquals("http://absolute.com", result);
    }

    @Test
    public void attrGetAbsPrefixMissingAttribute() {
        assertEquals("", node.attr("abs:missing"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void attrGetNullKey() {
        node.attr(null);
    }

    @Test
    public void attrSet() {
        node.attr("newKey", "newValue");
        assertEquals("newValue", node.attr("newKey"));
    }

    @Test
    public void hasAttrTrue() {
        assertTrue(node.hasAttr("key1"));
    }

    @Test
    public void hasAttrFalse() {
        assertFalse(node.hasAttr("nonexistent"));
    }

    @Test
    public void hasAttrAbsPrefixTrue() {
        node.attr("href", "http://absolute.com");
        assertTrue(node.hasAttr("abs:href"));
    }

    @Test
    public void hasAttrAbsPrefixFalseAbsUrlEmpty() {
        node.attr("href", "relative");
        node.baseUri = "invalid";
        assertFalse(node.hasAttr("abs:href"));
    }

    @Test
    public void hasAttrAbsPrefixFalseKeyMissing() {
        assertFalse(node.hasAttr("abs:missing"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void hasAttrNullKey() {
        node.hasAttr(null);
    }

    @Test
    public void removeAttr() {
        assertTrue(node.hasAttr("key1"));
        node.removeAttr("key1");
        assertFalse(node.hasAttr("key1"));
    }

    @Testexpected = IllegalArgumentException.class)
    public void removeAttrNullKey() {
        node.removeAttr(null);
    }

    @Test
    public void baseUri() {
        assertEquals("http://example.com", node.baseUri());
    }

    @Test
    public void setBaseUri() {
        node.appendChild(new TestNode("old", new Attributes()));
        node.setBaseUri("http://new.com");
        assertEquals("http://new.com", node.baseUri());
        for (Node child : node.childNodes()) {
            assertEquals("http://new.com", child.baseUri());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void setBaseUriNull() {
        node.setBaseUri(null);
    }

    @Test
    public void absUrlValidRelative() {
        node.setBaseUri("http://example.com/path/");
        node.attr("href", "file.html");
        assertEquals("http://example.com/path/file.html", node.absUrl("href"));
    }

    @Test
    public void absUrlAbsoluteAttr() {
        node.attr("href", "http://absolute.com/foo");
        assertEquals("http://absolute.com/foo", node.absUrl("href"));
    }

    @Test
    public void absUrlMalformedBaseAbsoluteAttr() {
        node.baseUri = "::invalid";
        node.attr("href", "http://absolute.com/foo");
        assertEquals("http://absolute.com/foo", node.absUrl("href"));
    }

    @Test
    public void absUrlMissingAttribute() {
        assertEquals("", node.absUrl("href"));
    }

    @Test
    public void absUrlWithQueryString() {
        node.baseUri = "http://example.com/path/file";
        node.attr("href", "?query=string");
        assertEquals("http://example.com/path/file?query=string", node.absUrl("href"));
    }

    @Test
    public void absUrlBothMalformed() {
        node.baseUri = "invalidbase";
        node.attr("href", "invalidattr");
        assertEquals("", node.absUrl("href"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void absUrlNullKey() {
        node.absUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void absUrlEmptyKey() {
        node.absUrl("");
    }

    @Test
    public void childNodeValidIndex() {
        Node child = new TestNode("child", new Attributes());
        node.appendChild(child);
        assertEquals(child, node.childNode(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void childNodeInvalidIndex() {
        node.childNode(0);
    }

    @Test
    public void childNodesEmpty() {
        assertEquals(0, node childNodes().size());
    }

    @Test
    public void childNodesNotEmpty() {
        Node child = new TestNode();
        node.appendChild(child);
        List<Node> children = node.childNodes();
        assertEquals(1, children.size());
        assertTrue(children.contains(child));
    }

    @Test
    public void childNodesAsArray() {
        Node child1 = new TestNode();
        Node child2 = new TestNode();
        node.appendChild(child1);
        node.appendChild(child2);
        Node[] arr = node.childNodesAsArray();
        assertEquals(2, arr.length);
        assertEquals(child1, arr[0]);
    }

    @Test
    public void parentInitialNull() {
        assertNull(node.parent());
    }

    @Test
    public void parentAfterAdoption() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        assertEquals(parent, node.parent());
    }

    @Test
    public void ownerDocumentThisIsDocument() {
        Document doc = new Document("http://example.com");
        assertEquals(doc, doc.ownerDocument());
    }

    @Test
    public void ownerDocumentParentNull() {
        assertNull(node.ownerDocument());
    }

    @Test
    public void ownerDocumentParentHasDocument() {
        doc.appendChild(node);
        assertEquals(doc, node.ownerDocument());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeWithoutParent() {
        node.remove();
    }

    @Test
    public void removeWithParent() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        assertNotNull(node.parent());
        node.remove();
        assertNull(node.parent());
        assertFalse(parent.childNodes().contains(node));
    }

    @Test
    public void beforeHtml() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.before("<p>before</p>");
        List<Node> siblings = parent.childNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.get(0) instanceof Element);
        Element inserted = (Element) siblings.get(0);
        assertEquals("p", inserted.tagName());
        assertEquals("before", inserted.text());
    }

    @Test
    public void beforeNode() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        Node beforeNode = new TestNode("before", new Attributes());
        node.before(beforeNode);
        List<Node> siblings = parent.childNodes();
        assertEquals(2, siblings.size());
        assertEquals(beforeNode, siblings.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void beforeHtmlNull() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.before(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void beforeNodeNull() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.before((Node) null);
    }

    @Test
    public void afterHtml() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.after("<p>after</p>");
        List<Node> siblings = parent.childNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.get(1) instanceof Element);
        assertEquals("after", ((Element) siblings.get(1)).text());
    }

    @Test
    public void afterNode() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        Node afterNode = new TestNode("after", new Attributes());
        node.afer(afterNode);
        List<Node> siblings = parent.childNodes();
        assertEquals(2, siblings.size());
        assertEquals(afterNode, siblings.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void afterHtmlNull() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.after(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void afterNodeNull() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.after((Node) null);
    }

    @Test
    public void wrapValidHtml() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.wrap("<div class=\"wrapper\"></div>");
        assertEquals(1, parent.childNodes.size());
        Node wrapper = parent.childNode(0);
        assertTrue(wrapper instanceof Element);
        assertEquals("div", ((Element) wrapper).tagName());
        assertEquals(1, wrapper.childNodes().size());
        assertEquals(node, wrapper.childNode(0));
    }

    @Test
    public void wrapNoop() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        assertNull(node.wrap(""));
    }

    @Test
    public void wrapRemainder() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        node.wrap("<div></div><p>remainder</p>");
        assertEquals(1, parent.childNodes.size());
        Node wrapper = parent.childNode(0);
        assertTrue(wrapper instanceof Element);
        Element wrapperEl = (Element) wrapper;
        assertEquals(2, wrapperEl.childNodes().size());
        assertTrue(wrapperEl.childNodes().get(0) instanceof Element);
        assertTrue(wrapperEl.childNodes().get(1) instanceof Element);
    }

    @Test
    public void unwrapWithChildren() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        Node child = new TestNode("child", new Attributes());
        node.appendChild(child);
        Node result = node.unwrap();
        assertEquals(1, parent.childNodes().size());
        assertEquals(child, parent.childNode(0));
        assertEquals(child, result);
    }

    @Test
    public void unwrapNoChildren() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        Node result = node.unwrap();
        assertEquals(0, parent.childNodes().size());
        assertNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unwrapNoParent() {
        node.unwrap();
    }

    @Test
    public void replaceWith() {
        Node parent = new TestNode("parent", new Attributes());
        parent.appendChild(node);
        Node replacement = new TestNode("repl", new Attributes());
        node.replaceWith(replacement);
        assertEquals(replacement, parent.childNode(0));
        assertNull(node.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void replaceWithNoParent() {
        node.replaceWith(new TestNode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void replaceWithNull() {
        Node parent = new TestNode();
        parent.appendChild(node);
        node.replaceWith(null);
    }

    @Test
    public void setParentNodeReplacement() {
        Node oldParent = new TestNode("oldParent", new Attributes());
        Node newParent = new TestNode("newParent", new Attributes());
        oldParent.appendChild(node);
        assertEquals(oldParent, node.parent());
        node.setParentNode(newParent);
        assertEquals(newParent, node.parent());
        assertFalse(oldParent.childNodes().contains(node));
    }

    @Test
    public void replaceChild() {
        Node parent = new TestNode("parent", new Attributes());
        Node out = new TestNode("out", new Attributes());
        Node in = new TestNode("in", new Attributes());
        parent.appendChild(out);
        parent.replaceChild(out, in);
        assertEquals(in, parent.childNode(0));
        assertEquals(parent, in.parent());
        assertNull(out.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void replaceChildWrongParent() {
        Node parent = new TestNode("parent", new Attributes());
        Node out = new TestNode("out");
        parent.replaceChild(out, new TestNode("in"));
    }

    @Test
    public void removeChild() {
        Node parent = new TestNode("parent", new Attributes());
        Node child = new TestNode("child", new Attributes());
        parent.appendChild(child);
        parent.removeChild(child);
        assertEquals(0, parent.childNodes().size());
        assertNull(child.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeChildWrongParent() {
        Node parent = new TestNode("parent", new Attributes());
        Node child = new TestNode("child");
        parent.removeChild(child);
    }

    @Test
    public void addChildrenVarargs() {
        Node child1 = new TestNode("c1", new Attributes());
        Node child2 = new TestNode("c2", new Attributes());
        node.addChildren(child1, child2);
        assertEquals(2, node.childNodes().size());
        assertEquals(child1, node.childNodes().get(0));
        assertEquals(child2, node.childNodes().get(1));
        assertEquals(node, child1.parent());
        assertEquals(node, child2.parent());
    }

    @Test
    public void addChildrenIndex() {
        Node existing = new TestNode("existing", new Attributes());
        node.appendChild(existing);
        Node new1 = new TestNode("new1", new Attributes());
        Node new2 = new TestNode("new2", new Attributes());
        node.addChildren(0, new1, new2);
        assertEquals(3, node.childNodes().size());
        assertEquals(new1, node.childNodes().get(0));
        assertEquals(new2, node.childNodes().get(1));
        assertEquals(existing, node.childNodes().get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addChildrenNullElement() {
        node.addChildren((Node) null);
    }

    @Test
    public void siblingNodes() {
        Node parent = new TestNode("parent", new Attributes());
        Node sibling = new TestNode("sibling", new Attributes());
        parent.appendChild(sibling);
        parent.appendChild(node);
        List<Node> siblings = node.siblingNodes();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(sibling));
        assertTrue(siblings.contains(node));
    }

    @Test
    public void nextSibling() {
        Node parent = new TestNode("parent", new Attributes());
        Node first = new TestNode("first", new Attributes());
        Node second = new TestNode("second", new Attributes());
        parent.appendChild(first);
        parent.appendChild(second);
        parent.appendChild(node);
        assertEquals(second, first.nextSibling());
        assertNull(node.nextSibling());
    }

    @Test
    public void nextSiblingNullParent() {
        assertNull(node.nextSibling());
    }

    @Test
    public void previousSibling() {
        Node parent = new TestNode("parent", new Attributes());
        Node first = new TestNode("first", new Attributes());
        Node second = new TestNode("second", new Attributes());
        parent.appendChild(first);
        parent.appendChild(second);
        parent.appendChild(node);
        assertEquals(second, node.previousSibling());
        assertNull(first.previousSibling());
    }

    @Test
    public void previousSiblingNullParent() {
        assertNull(node.previousSibling());
    }

    @Test
    public void siblingIndex() {
        assertEquals(0, node.siblingIndex());
        Node parent = new TestNode();
        parent.appendChild(new TestNode());
        parent.appendChild(node);
        assertEquals(1, node.siblingIndex());
    }

    @Test
    public void setSiblingIndex() {
        node.setSiblingIndex(5);
        assertEquals(5, node.siblingIndex());
    }

    @Test
    public void traverse() {
        final int[] visitCount = {0};
        node.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visitCount[0]++;
            }
            public void tail(Node node, int depth) {
            }
        });
        assertEquals(1, visitCount[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void traverseNullVisitor() {
        node.traverse(null);
    }

    @Test
    public void outerHtml() {
        assertEquals("headtail", node.outerHtml());
    }

    @Test
    public void toStringUsesOuterHtml() {
        assertEquals(node.outerHtml(), node.toString());
    }

    @Test
    public void indent() {
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        out.indentAmount(2);
        node.indent(sb, 3, out);
        String result = sb.toString();
        assertTrue(result.startsWith("\n"));
        assertTrue(result.endsWith("      ")); // 6 spaces for 3*2
    }

    @Test
    public void equalsSameObject() {
        assertTrue(node.equals(node));
    }

    @Test
    public void equalsDifferentObject() {
        assertFalse(node.equals(new TestNode()));
    }

    @Test
    public void hashCodeBasedOnParentAndAttributes() {
        node.attributes = new Attributes();
        int hash1 = node.hashCode();
        node.attributes.put("test", "value");
        int hash2 = node.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void cloneNoParent() {
        Node cloned = node.clone();
        assertNull(cloned.parent());
        assertEquals(0, cloned.siblingIndex());
        assertNotSame(node, cloned);
        assertEquals(node.baseUri(), cloned.baseUri());
        assertNotSame(node.attributes(), cloned.attributes());
        assertTrue(cloned.childNodes().isEmpty());
    }

    @Test
    public void cloneDeep() {
        node.appendChild(new TestNode("child", new Attributes()));
        Node cloned = node.clone();
        assertEquals(1, cloned.childNodes().size());
        assertNotSame(node.childNodes().get(0), cloned.childNodes().get(0));
        assertEquals(cloned, cloned.childNodes().get(0).parent());
    }

    @Test
    public void doCloneWithParent() {
        Node parent = new TestNode("parent", new Attributes());
        node.appendChild(new TestNode("child", new Attributes()));
        Node cloned = node.doClone(parent);
        assertEquals(parent, cloned.parent());
        assertEquals(1, cloned.childNodes().size());
        assertEquals(cloned, cloned.childNodes().get(0).parent());
    }
}
