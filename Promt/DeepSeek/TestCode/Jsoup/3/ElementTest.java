package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import java.util.*;

import org.jsoup.parser.Tag;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

public class ElementTest {
    private Element div;
    private Element span;
    private Document doc;

    @Before
    public void setUp() {
        div = new Element(Tag.valueOf("div"), "");
        span = new Element(Tag.valueOf("span"), "");
        doc = new Document("");
    }

    @Test
    public void testConstructor() {
        Element el = new Element(Tag.valueOf("p"), "http://example.com");
        assertEquals("p", el.tagName());
        assertEquals("http://example.com", el.baseUri());
        assertNotNull(el.attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "");
    }

    @Test
    public void testConstructorWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("id", "test");
        Element el = new Element(Tag.valueOf("div"), "", attrs);
        assertEquals("test", el.id());
    }

    @Test
    public void testNodeName() {
        assertEquals("div", div.nodeName());
    }

    @Test
    public void testTagName() {
        assertEquals("div", div.tagName());
    }

    @Test
    public void testTag() {
        assertSame(div.tag(), div.tag());
    }

    @Test
    public void testIsBlock() {
        assertTrue(div.isBlock());
        assertFalse(span.isBlock());
    }

    @Test
    public void testId() {
        assertEquals("", div.id());
        div.attr("id", "myId");
        assertEquals("myId", div.id());
    }

    @Test
    public void testAttrSet() {
        Element el = div.attr("key", "value");
        assertSame(div, el);
        assertEquals("value", div.attr("key"));
    }

    @Test
    public void testParent() {
        assertNull(div.parent());
        Element child = new Element(Tag.valueOf("p"), "");
        div.appendChild(child);
        assertSame(div, child.parent());
    }

    @Test
    public void testParents() {
        Element child = new Element(Tag.valueOf("p"), "");
        div.appendChild(child);
        Element grandchild = new Element(Tag.valueOf("span"), "");
        child.appendChild(grandchild);
        Elements parents = grandchild.parents();
        assertEquals(2, parents.size());
        assertSame(child, parents.get(0));
        assertSame(div, parents.get(1));
    }

    @Test
    public void testParentsExcludeRoot() {
        Element root = new Element(Tag.valueOf("#root"), "");
        Element child = new Element(Tag.valueOf("div"), "");
        root.appendChild(child);
        Elements parents = child.parents();
        assertEquals(0, parents.size());
    }

    @Test
    public void testChild() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        assertSame(p1, div.child(0));
        assertSame(p2, div.child(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildOutOfBounds() {
        div.child(0);
    }

    @Test
    public void testChildren() {
        Element p = new Element(Tag.valueOf("p"), "");
        div.appendChild(new TextNode("text", ""));
        div.appendChild(p);
        Elements children = div.children();
        assertEquals(1, children.size());
        assertSame(p, children.get(0));
    }

    @Test
    public void testSelect() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("class", "foo");
        div.appendChild(p);
        Elements result = div.select("p.foo");
        assertEquals(1, result.size());
        assertSame(p, result.get(0));
    }

    @Test
    public void testAppendChild() {
        Element child = new Element(Tag.valueOf("p"), "");
        Element result = div.appendChild(child);
        assertSame(div, result);
        assertSame(div, child.parent());
        assertEquals(1, div.childNodes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendChildNull() {
        div.appendChild(null);
    }

    @Test
    public void testPrependChild() {
        Element first = new Element(Tag.valueOf("p"), "");
        Element second = new Element(Tag.valueOf("span"), "");
        div.appendChild(first);
        div.prependChild(second);
        assertEquals(2, div.childNodes().size());
        assertSame(second, div.child(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrependChildNull() {
        div.prependChild(null);
    }

    @Test
    public void testAppendElement() {
        Element child = div.appendElement("p");
        assertEquals("p", child.tagName());
        assertSame(div, child.parent());
        assertEquals(1, div.children().size());
    }

    @Test
    public void testPrependElement() {
        Element child = div.prependElement("span");
        assertEquals("span", child.tagName());
        assertSame(div, child.parent());
        assertEquals(1, div.children().size());
        assertSame(child, div.child(0));
    }

    @Test
    public void testAppendText() {
        Element result = div.appendText("hello");
        assertSame(div, result);
        assertEquals(1, div.childNodes().size());
        assertTrue(div.childNodes().get(0) instanceof TextNode);
        assertEquals("hello", ((TextNode) div.childNodes().get(0)).getWholeText());
    }

    @Test
    public void testPrependText() {
        div.appendText("world");
        div.prependText("hello ");
        assertEquals(2, div.childNodes().size());
        assertEquals("hello world", div.text());
    }

    @Test
    public void testAppend() {
        Element result = div.append("<p>one</p><p>two</p>");
        assertSame(div, result);
        assertEquals(2, div.children().size());
        assertEquals("one", div.child(0).text());
        assertEquals("two", div.child(1).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNull() {
        div.append(null);
    }

    @Test
    public void testPrepend() {
        div.append("<p>two</p>");
        div.prepend("<p>one</p>");
        assertEquals(2, div.children().size());
        assertEquals("one", div.child(0).text());
        assertEquals("two", div.child(1).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrependNull() {
        div.prepend(null);
    }

    @Test
    public void testEmpty() {
        div.appendChild(new Element(Tag.valueOf("p"), ""));
        div.attr("id", "keep");
        Element result = div.empty();
        assertSame(div, result);
        assertEquals(0, div.childNodes().size());
        assertEquals("keep", div.id());
    }

    @Test
    public void testWrap() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.text("content");
        div.appendChild(p);
        Element result = p.wrap("<div class='wrapper'></div>");
        assertSame(p, result);
        assertEquals("div", p.parent().tagName());
        assertTrue(p.parent().hasClass("wrapper"));
        assertEquals("content", p.text());
    }

    @Test
    public void testWrapUnbalanced() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.text("content");
        div.appendChild(p);
        p.wrap("<div class='outer'></div><span class='remainder'></span>");
        Element parent = p.parent();
        assertEquals("div", parent.tagName());
        assertTrue(parent.hasClass("outer"));
        // remainder should be appended to wrap
        Elements children = parent.children();
        assertEquals(2, children.size());
        assertEquals("span", children.get(1).tagName());
        assertTrue(children.get(1).hasClass("remainder"));
    }

    @Test
    public void testWrapNoop() {
        Element p = new Element(Tag.valueOf("p"), "");
        div.appendChild(p);
        Element result = p.wrap("text"); // no element to wrap
        assertNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapEmpty() {
        div.wrap("");
    }

    @Test
    public void testSiblingElements() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        Elements siblings = p1.siblingElements();
        assertEquals(2, siblings.size());
        assertSame(p1, siblings.get(0));
        assertSame(p2, siblings.get(1));
    }

    @Test
    public void testNextElementSibling() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        assertSame(p2, p1.nextElementSibling());
        assertNull(p2.nextElementSibling());
    }

    @Test
    public void testPreviousElementSibling() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        assertSame(p1, p2.previousElementSibling());
        assertNull(p1.previousElementSibling());
    }

    @Test
    public void testFirstElementSibling() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        assertSame(p1, p1.firstElementSibling());
        assertSame(p1, p2.firstElementSibling());
        // only one child
        Element single = new Element(Tag.valueOf("div"), "");
        single.appendChild(new Element(Tag.valueOf("p"), ""));
        assertNull(single.child(0).firstElementSibling());
    }

    @Test
    public void testElementSiblingIndex() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        assertEquals(Integer.valueOf(0), p1.elementSiblingIndex());
        assertEquals(Integer.valueOf(1), p2.elementSiblingIndex());
        // no parent
        assertEquals(Integer.valueOf(0), new Element(Tag.valueOf("div"), "").elementSiblingIndex());
    }

    @Test
    public void testLastElementSibling() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        assertSame(p2, p1.lastElementSibling());
        assertSame(p2, p2.lastElementSibling());
        // only one child
        Element single = new Element(Tag.valueOf("div"), "");
        single.appendChild(new Element(Tag.valueOf("p"), ""));
        assertNull(single.child(0).lastElementSibling());
    }

    @Test
    public void testGetElementsByTag() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        Elements result = div.getElementsByTag("p");
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByTagEmpty() {
        div.getElementsByTag("");
    }

    @Test
    public void testGetElementById() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("id", "target");
        div.appendChild(p);
        Element found = div.getElementById("target");
        assertSame(p, found);
        assertNull(div.getElementById("missing"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementByIdEmpty() {
        div.getElementById("");
    }

    @Test
    public void testGetElementsByClass() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        p1.attr("class", "foo bar");
        Element p2 = new Element(Tag.valueOf("p"), "");
        p2.attr("class", "foo");
        div.appendChild(p1);
        div.appendChild(p2);
        Elements result = div.getElementsByClass("foo");
        assertEquals(2, result.size());
        result = div.getElementsByClass("bar");
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByClassEmpty() {
        div.getElementsByClass("");
    }

    @Test
    public void testGetElementsByAttribute() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("data-x", "1");
        div.appendChild(p);
        Elements result = div.getElementsByAttribute("data-x");
        assertEquals(1, result.size());
        assertSame(p, result.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeEmpty() {
        div.getElementsByAttribute("");
    }

    @Test
    public void testGetElementsByAttributeValue() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("data-x", "1");
        div.appendChild(p);
        Elements result = div.getElementsByAttributeValue("data-x", "1");
        assertEquals(1, result.size());
        result = div.getElementsByAttributeValue("data-x", "2");
        assertEquals(0, result.size());
    }

    @Test
    public void testGetElementsByAttributeValueNot() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        p1.attr("data-x", "1");
        Element p2 = new Element(Tag.valueOf("p"), "");
        p2.attr("data-x", "2");
        div.appendChild(p1);
        div.appendChild(p2);
        Elements result = div.getElementsByAttributeValueNot("data-x", "1");
        assertEquals(1, result.size());
        assertSame(p2, result.get(0));
    }

    @Test
    public void testGetElementsByAttributeValueStarting() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("href", "http://example.com");
        div.appendChild(p);
        Elements result = div.getElementsByAttributeValueStarting("href", "http://");
        assertEquals(1, result.size());
        result = div.getElementsByAttributeValueStarting("href", "https://");
        assertEquals(0, result.size());
    }

    @Test
    public void testGetElementsByAttributeValueEnding() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("src", "image.png");
        div.appendChild(p);
        Elements result = div.getElementsByAttributeValueEnding("src", ".png");
        assertEquals(1, result.size());
        result = div.getElementsByAttributeValueEnding("src", ".jpg");
        assertEquals(0, result.size());
    }

    @Test
    public void testGetElementsByAttributeValueContaining() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.attr("title", "hello world");
        div.appendChild(p);
        Elements result = div.getElementsByAttributeValueContaining("title", "world");
        assertEquals(1, result.size());
        result = div.getElementsByAttributeValueContaining("title", "foo");
        assertEquals(0, result.size());
    }

    @Test
    public void testGetElementsByIndexLessThan() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        Element p3 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        div.appendChild(p3);
        Elements result = div.getElementsByIndexLessThan(1);
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }

    @Test
    public void testGetElementsByIndexGreaterThan() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        Element p3 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        div.appendChild(p3);
        Elements result = div.getElementsByIndexGreaterThan(1);
        assertEquals(1, result.size());
        assertSame(p3, result.get(0));
    }

    @Test
    public void testGetElementsByIndexEquals() {
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);
        div.appendChild(p2);
        Elements result = div.getElementsByIndexEquals(1);
        assertEquals(1, result.size());
        assertSame(p2, result.get(0));
    }

    @Test
    public void testGetAllElements() {
        Element p = new Element(Tag.valueOf("p"), "");
        div.appendChild(p);
        Elements all = div.getAllElements();
        assertTrue(all.contains(div));
        assertTrue(all.contains(p));
    }

    @Test
    public void testText() {
        div.appendText(" hello ");
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText(" world ");
        div.appendChild(p);
        assertEquals("hello world", div.text());
    }

    @Test
    public void testTextBlockSpacing() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("block");
        div.appendText("inline");
        div.appendChild(p);
        assertEquals("inline block", div.text());
    }

    @Test
    public void testTextPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendText("  hello  ");
        assertEquals("  hello  ", pre.text());
    }

    @Test
    public void testTextSet() {
        div.appendChild(new Element(Tag.valueOf("p"), ""));
        Element result = div.text("new text");
        assertSame(div, result);
        assertEquals(1, div.childNodes().size());
        assertTrue(div.childNodes().get(0) instanceof TextNode);
        assertEquals("new text", div.text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTextSetNull() {
        div.text(null);
    }

    @Test
    public void testHasText() {
        assertFalse(div.hasText());
        div.appendText(" ");
        assertFalse(div.hasText()); // blank
        div.empty();
        div.appendText("a");
        assertTrue(div.hasText());
        div.empty();
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("b");
        div.appendChild(p);
        assertTrue(div.hasText());
    }

    @Test
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x = 1;", ""));
        assertEquals("var x = 1;", script.data());
        // nested element data
        Element div2 = new Element(Tag.valueOf("div"), "");
        div2.appendChild(new DataNode("data1", ""));
        Element inner = new Element(Tag.valueOf("span"), "");
        inner.appendChild(new DataNode("data2", ""));
        div2.appendChild(inner);
        assertEquals("data1data2", div2.data());
    }

    @Test
    public void testClassName() {
        assertEquals("", div.className());
        div.attr("class", "foo bar");
        assertEquals("foo bar", div.className());
    }

    @Test
    public void testClassNamesSet() {
        div.attr("class", "foo bar");
        Set<String> classes = div.classNames();
        assertEquals(2, classes.size());
        assertTrue(classes.contains("foo"));
        assertTrue(classes.contains("bar"));
    }

    @Test
    public void testClassNamesSetEmpty() {
        Set<String> classes = div.classNames();
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testClassNamesSetSet() {
        Set<String> classes = new LinkedHashSet<String>();
        classes.add("a");
        classes.add("b");
        Element result = div.classNames(classes);
        assertSame(div, result);
        assertEquals("a b", div.attr("class"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassNamesSetSetNull() {
        div.classNames(null);
    }

    @Test
    public void testHasClass() {
        div.attr("class", "foo bar");
        assertTrue(div.hasClass("foo"));
        assertFalse(div.hasClass("baz"));
    }

    @Test
    public void testAddClass() {
        div.addClass("foo");
        assertTrue(div.hasClass("foo"));
        div.addClass("bar");
        assertTrue(div.hasClass("bar"));
        assertEquals("foo bar", div.attr("class"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddClassNull() {
        div.addClass(null);
    }

    @Test
    public void testRemoveClass() {
        div.attr("class", "foo bar");
        div.removeClass("foo");
        assertFalse(div.hasClass("foo"));
        assertTrue(div.hasClass("bar"));
        div.removeClass("bar");
        assertEquals("", div.attr("class"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveClassNull() {
        div.removeClass(null);
    }

    @Test
    public void testToggleClass() {
        div.toggleClass("foo");
        assertTrue(div.hasClass("foo"));
        div.toggleClass("foo");
        assertFalse(div.hasClass("foo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToggleClassNull() {
        div.toggleClass(null);
    }

    @Test
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "test");
        assertEquals("test", input.val());
        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("content");
        assertEquals("content", textarea.val());
    }

    @Test
    public void testValSet() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("newval");
        assertEquals("newval", input.attr("value"));
        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("newtext");
        assertEquals("newtext", textarea.text());
    }

    @Test
    public void testHtml() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("hello");
        div.appendChild(p);
        assertEquals("<p>hello</p>", div.html());
    }

    @Test
    public void testHtmlSet() {
        div.html("<p>test</p>");
        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());
        assertEquals("test", div.child(0).text());
    }

    @Test
    public void testOuterHtml() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("text");
        assertEquals("<p>text</p>", p.toString());
    }

    @Test
    public void testOuterHtmlSelfClosing() {
        Element br = new Element(Tag.valueOf("br"), "");
        assertEquals("<br />", br.toString());
    }

    @Test
    public void testEquals() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("div"), "");
        assertEquals(el1, el2);
        el1.attr("id", "x");
        assertFalse(el1.equals(el2));
        el2.attr("id", "x");
        assertEquals(el1, el2);
        assertFalse(el1.equals(null));
        assertFalse(el1.equals("string"));
    }

    @Test
    public void testHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("div"), "");
        assertEquals(el1.hashCode(), el2.hashCode());
        el1.attr("id", "x");
        assertFalse(el1.hashCode() == el2.hashCode());
    }

    @Test
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        assertTrue(pre.preserveWhitespace());
        Element div = new Element(Tag.valueOf("div"), "");
        assertFalse(div.preserveWhitespace());
        // parent preserves whitespace
        Element child = new Element(Tag.valueOf("span"), "");
        pre.appendChild(child);
        assertTrue(child.preserveWhitespace());
    }
}
