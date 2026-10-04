package org.jsoup.nodes;

import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ElementTest {

    @Test
    public void testConstructorsAndTag() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "http://example.com");
        assertEquals("div", el.nodeName());
        assertEquals("div", el.tagName());
        assertSame(tag, el.tag());
        assertTrue(el.isBlock());

        el.tagName("span");
        assertEquals("span", el.tagName());
        assertEquals("span", el.nodeName());
        assertFalse(el.isBlock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTagConstructorThrows() {
        new Element(null, "http://example.com", new Attributes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTagNameThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test
    public void testIdAndAttr() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());
        el.attr("id", "main");
        assertEquals("main", el.id());
        el.attr("data-test", "val");
        assertEquals("val", el.attr("data-test"));

        Map<String, String> dataset = el.dataset();
        assertEquals(1, dataset.size());
        assertEquals("val", dataset.get("test"));
    }

    @Test
    public void testParentAndParents() {
        Element root = new Element(Tag.valueOf("#root"), "");
        Element body = root.appendElement("body");
        Element div = body.appendElement("div");
        Element p = div.appendElement("p");

        assertSame(div, p.parent());
        Elements parents = p.parents();
        assertEquals(2, parents.size());
        assertSame(div, parents.get(0));
        assertSame(body, parents.get(1));
    }

    @Test
    public void testChildrenAndChildNodes() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendText("Hello ");
        Element span = div.appendElement("span");
        span.text("World");
        div.appendChild(new DataNode("data content", ""));

        assertEquals(1, div.children().size());
        assertSame(span, div.child(0));
        List<TextNode> textNodes = div.textNodes();
        assertEquals(1, textNodes.size());
        assertEquals("Hello ", textNodes.get(0).getWholeText());

        List<DataNode> dataNodes = div.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("data content", dataNodes.get(0).getWholeData());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildIndexOutOfBounds() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.child(0);
    }

    @Test
    public void testChildManipulation() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendElement("p").text("2");
        div.prependElement("h1").text("1");
        div.appendText("3");
        div.prependText("0");

        assertEquals("0", ((TextNode) div.childNode(0)).getWholeText());
        assertEquals("h1", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
        assertEquals("3", ((TextNode) div.childNode(3)).getWholeText());
    }

    @Test
    public void testInsertChildren() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p1 = new Element(Tag.valueOf("p"), "");
        Element p2 = new Element(Tag.valueOf("p"), "");
        div.appendChild(p1);

        div.insertChildren(0, Collections.singletonList(p2));
        assertEquals(2, div.children().size());
        assertSame(p2, div.child(0));
        assertSame(p1, div.child(1));

        Element p3 = new Element(Tag.valueOf("p"), "");
        div.insertChildren(-1, Collections.singletonList(p3));
        assertSame(p3, div.child(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenNullThrows() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBoundsThrows() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.insertChildren(5, Collections.emptyList());
    }

    @Test
    public void testHtmlAppendPrepend() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>1</p>");
        assertEquals(1, div.children().size());
        div.prepend("<span>0</span>");
        assertEquals(2, div.children().size());
        assertEquals("span", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
    }

    @Test
    public void testSiblings() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element el0 = parent.appendElement("span");
        Element el1 = parent.appendElement("p");
        Element el2 = parent.appendElement("div");

        assertEquals(0, el0.siblingElements().indexOf(el1));
        assertEquals(2, el0.siblingElements().size());
        assertSame(el1, el0.nextElementSibling());
        assertNull(el0.previousElementSibling());
        assertSame(el0, el1.previousElementSibling());
        assertSame(el2, el1.nextElementSibling());
        assertSame(el0, el1.firstElementSibling());
        assertSame(el2, el1.lastElementSibling());
        assertEquals(Integer.valueOf(1), el1.elementSiblingIndex());

        Element standalone = new Element(Tag.valueOf("p"), "");
        assertEquals(0, standalone.siblingElements().size());
        assertNull(standalone.nextElementSibling());
        assertNull(standalone.previousElementSibling());
        assertEquals(Integer.valueOf(0), standalone.elementSiblingIndex());
    }

    @Test
    public void testDomSelectionMethods() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "root-id");
        div.attr("class", "c1 c2");
        div.attr("data-type", "special");

        Element child1 = div.appendElement("p");
        child1.attr("class", "c1");
        child1.attr("title", "foo-bar");
        child1.text("Alpha Charlie");

        Element child2 = div.appendElement("span");
        child2.attr("title", "bar-baz");
        child2.text("Bravo");

        assertSame(div, div.getElementById("root-id"));
        assertNull(div.getElementById("non-existent"));

        assertEquals(1, div.getElementsByTag("span").size());
        assertEquals(2, div.getElementsByClass("c1").size());
        assertEquals(1, div.getElementsByClass("c2").size());
        assertEquals(2, div.getElementsByAttribute("title").size());
        assertEquals(1, div.getElementsByAttributeStarting("data-").size());
        assertEquals(1, div.getElementsByAttributeValue("title", "foo-bar").size());
        assertEquals(2, div.getElementsByAttributeValueNot("title", "foo-bar").size());
        assertEquals(1, div.getElementsByAttributeValueStarting("title", "foo").size());
        assertEquals(1, div.getElementsByAttributeValueEnding("title", "baz").size());
        assertEquals(2, div.getElementsByAttributeValueContaining("title", "bar").size());
        assertEquals(2, div.getElementsByAttributeValueMatching("title", Pattern.compile("bar")).size());
        assertEquals(2, div.getElementsByAttributeValueMatching("title", "bar").size());

        assertEquals(1, div.getElementsByIndexLessThan(1).size());
        assertEquals(1, div.getElementsByIndexGreaterThan(0).size());
        assertEquals(1, div.getElementsByIndexEquals(0).size());

        assertEquals(2, div.getElementsContainingText("Alpha").size());
        assertEquals(1, div.getElementsContainingOwnText("Alpha").size());
        assertEquals(2, div.getElementsMatchingText(Pattern.compile("Alpha")).size());
        assertEquals(2, div.getElementsMatchingText("Alpha").size());
        assertEquals(1, div.getElementsMatchingOwnText(Pattern.compile("Alpha")).size());
        assertEquals(1, div.getElementsMatchingOwnText("Alpha").size());

        assertEquals(3, div.getAllElements().size());
        assertEquals(1, div.select("p.c1").size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingPatternSyntaxException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("attr", "[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextPatternSyntaxException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingOwnTextPatternSyntaxException() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("[invalid");
    }

    @Test
    public void testTextAndWhitespace() {
        Element p = new Element(Tag.valueOf("p"), "");
        p.text(" Hello   <b>world</b>! ");
        assertEquals("Hello <b>world</b>!", p.text());
        assertTrue(p.hasText());

        Element parent = new Element(Tag.valueOf("div"), "");
        parent.append("Hello <br> world <p>Paragraph</p>");
        assertEquals("Hello world Paragraph", parent.text());
        assertEquals("Hello world", parent.ownText());

        Element empty = new Element(Tag.valueOf("div"), "");
        assertFalse(empty.hasText());
        assertEquals("", empty.text());
        assertEquals("", empty.ownText());
    }

    @Test
    public void testData() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x = 1;", ""));
        assertEquals("var x = 1;", script.data());

        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(script);
        assertEquals("var x = 1;", div.data());
    }

    @Test
    public void testClassNamesAndManipulation() {
        Element div = new Element(Tag.valueOf("div"), "");
        assertEquals("", div.className());
        assertFalse(div.hasClass("foo"));

        div.addClass("foo");
        assertTrue(div.hasClass("foo"));
        assertEquals("foo", div.className());

        div.addClass("bar");
        assertTrue(div.hasClass("bar"));
        assertEquals("foo bar", div.className());

        div.removeClass("foo");
        assertFalse(div.hasClass("foo"));
        assertTrue(div.hasClass("bar"));

        div.toggleClass("bar");
        assertFalse(div.hasClass("bar"));
        div.toggleClass("baz");
        assertTrue(div.hasClass("baz"));

        Set<String> customClasses = new HashSet<String>(Arrays.asList("a", "b"));
        div.classNames(customClasses);
        assertEquals(2, div.classNames().size());
        assertTrue(div.hasClass("a"));
        assertTrue(div.hasClass("b"));
    }

    @Test
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test-value");
        assertEquals("test-value", input.val());
        assertEquals("test-value", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("text-content");
        assertEquals("text-content", textarea.val());
        assertEquals("text-content", textarea.text());
    }

    @Test
    public void testHtmlAndOuterHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>Hello</p>");
        assertEquals("<p>Hello</p>", div.html());
        assertEquals("<div>\n <p>Hello</p>\n</div>", div.outerHtml());

        Element img = new Element(Tag.valueOf("img"), "");
        assertEquals("<img />", img.outerHtml());

        assertEquals(div.outerHtml(), div.toString());
    }

    @Test
    public void testEmpty() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>1</p><span>2</span>");
        assertEquals(2, div.children().size());
        div.empty();
        assertEquals(0, div.children().size());
        assertEquals("", div.text());
    }

    @Test
    public void testWrapBeforeAfter() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span");

        child.before("<p>before</p>");
        assertEquals("p", parent.child(0).tagName());
        assertSame(child, parent.child(1));

        child.after("<b>after</b>");
        assertEquals("b", parent.child(2).tagName());

        Element nodeBefore = new Element(Tag.valueOf("i"), "");
        child.before(nodeBefore);
        assertSame(nodeBefore, parent.child(1));

        Element nodeAfter = new Element(Tag.valueOf("u"), "");
        child.after(nodeAfter);
        assertSame(nodeAfter, parent.child(3));

        Element isolated = new Element(Tag.valueOf("em"), "");
        isolated.wrap("<div class='wrapper'></div>");
    }

    @Test
    public void testEqualsAndHashCodeAndClone() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("div"), "");

        assertEquals(el1, el1);
        assertNotEquals(el1, el2);
        assertNotEquals(el1, null);
        assertNotEquals(el1, "string");
        assertNotEquals(0, el1.hashCode());

        el1.addClass("highlight");
        Element clone = el1.clone();
        assertNotEquals(el1, clone);
        assertEquals(el1.html(), clone.html());
        assertEquals(el1.className(), clone.className());
        assertTrue(clone.hasClass("highlight"));
    }

    @Test
    public void testPreserveWhitespace() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.appendText("  spaces  \n  lines  ");
        assertTrue(pre.preserveWhitespace());
        assertEquals("  spaces  \n  lines  ", pre.text());

        Element p = new Element(Tag.valueOf("p"), "");
        p.appendText("  spaces  \n  lines  ");
        assertFalse(p.preserveWhitespace());
        assertEquals("spaces lines", p.text());
    }
}
