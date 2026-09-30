package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testConstructorsAndGetters() {
        Tag tag = Tag.valueOf("div");
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        Element el = new Element(tag, "http://example.com", attrs);

        assertEquals("div", el.nodeName());
        assertEquals("div", el.tagName());
        assertSame(tag, el.tag());
        assertTrue(el.isBlock());
        assertEquals("main", el.id());
        assertEquals("http://example.com", el.baseUri());

        Element simpleEl = new Element(Tag.valueOf("span"), "");
        assertEquals("span", simpleEl.tagName());
        assertFalse(simpleEl.isBlock());
        assertEquals("", simpleEl.id());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test
    public void testAttrChaining() {
        Element el = new Element(Tag.valueOf("div"), "");
        Element returned = el.attr("title", "tooltip");
        assertSame(el, returned);
        assertEquals("tooltip", el.attr("title"));
    }

    @Test
    public void testParentsAndAccumulateParents() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        Element span = doc.select("span").first();
        Element p = doc.select("p").first();
        Element div = doc.select("div").first();
        Element body = doc.body();

        Elements parents = span.parents();
        assertEquals(4, parents.size());
        assertEquals(p, parents.get(0));
        assertEquals(div, parents.get(1));
        assertEquals(body, parents.get(2));
        assertEquals(doc.child(0), parents.get(3)); // html

        Element standalone = new Element(Tag.valueOf("div"), "");
        assertEquals(0, standalone.parents().size());
    }

    @Test
    public void testChildrenAndChildAccess() {
        Element div = new Element(Tag.valueOf("div"), "");
        assertEquals(0, div.children().size());

        div.appendText("Text node");
        Element p = div.appendElement("p");
        div.appendText("Another text node");
        Element span = div.appendElement("span");

        assertEquals(4, div.childNodes().size());
        Elements children = div.children();
        assertEquals(2, children.size());
        assertEquals(p, children.get(0));
        assertEquals(span, children.get(1));

        assertEquals(p, div.child(0));
        assertEquals(span, div.child(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildOutOfBounds() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.child(0);
    }

    @Test
    public void testAppendAndPrependChild() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        assertSame(div, div.appendChild(p));
        assertSame(div, div.prependChild(span));

        assertEquals(2, div.children().size());
        assertEquals(span, div.child(0));
        assertEquals(p, div.child(1));
        assertSame(div, p.parent());
        assertSame(div, span.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullChild() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrependNullChild() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.prependChild(null);
    }

    @Test
    public void testAppendAndPrependElement() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        Element span = div.appendElement("span");
        Element b = div.prependElement("b");

        assertEquals("span", span.tagName());
        assertEquals("b", b.tagName());
        assertEquals("http://example.com", span.baseUri());
        assertEquals("http://example.com", b.baseUri());
        assertEquals(2, div.children().size());
        assertEquals(b, div.child(0));
        assertEquals(span, div.child(1));
    }

    @Test
    public void testAppendAndPrependText() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendText("World");
        div.prependText("Hello ");

        assertEquals("Hello World", div.text());
        assertEquals(2, div.childNodes().size());
        assertTrue(div.childNode(0) instanceof TextNode);
        assertEquals("Hello ", ((TextNode) div.childNode(0)).getWholeText());
    }

    @Test
    public void testAppendAndPrependHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>One</p><p>Two</p>");
        assertEquals(2, div.children().size());
        assertEquals("One", div.child(0).text());
        assertEquals("Two", div.child(1).text());

        div.prepend("<span>First</span><span>Second</span>");
        assertEquals(4, div.children().size());
        assertEquals("First", div.child(0).text());
        assertEquals("Second", div.child(1).text());
        assertEquals("One", div.child(2).text());
        assertEquals("Two", div.child(3).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendNullHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrependNullHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.prepend(null);
    }

    @Test
    public void testEmpty() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.attr("id", "container");
        div.appendElement("p").text("Hello");
        assertEquals(1, div.childNodes().size());

        assertSame(div, div.empty());
        assertEquals(0, div.childNodes().size());
        assertEquals("container", div.id());
    }

    @Test
    public void testWrapSingleAndMultiple() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element p = doc.select("p").first();
        p.wrap("<div class='outer'><section></section></div>");

        assertEquals("<div class=\"outer\"><section><p>Hello</p></section></div>", doc.body().child(0).child(0).outerHtml());

        Document doc2 = Jsoup.parse("<div><span>Test</span></div>");
        Element span = doc2.select("span").first();
        span.wrap("<div id='one'></div><div id='two'></div>");
        assertEquals("<div id=\"one\"><span>Test</span><div id=\"two\"></div></div>", doc2.body().child(0).child(0).outerHtml());
    }

    @Test
    public void testWrapEmptyOrNoop() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div>");
        Element p = doc.select("p").first();
        Element wrapped = p.wrap("   ");
        assertNull(wrapped);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapNull() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.wrap(null);
    }

    @Test
    public void testSiblingNavigation() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p><p id='3'></p></div>");
        Element div = doc.body().child(0);
        Element p1 = div.child(0);
        Element p2 = div.child(1);
        Element p3 = div.child(2);

        assertEquals(3, p1.siblingElements().size());
        assertEquals(p2, p1.nextElementSibling());
        assertNull(p1.previousElementSibling());
        assertEquals(p1, p1.firstElementSibling());
        assertEquals(p3, p1.lastElementSibling());
        assertEquals(Integer.valueOf(0), p1.elementSiblingIndex());

        assertEquals(p3, p2.nextElementSibling());
        assertEquals(p1, p2.previousElementSibling());
        assertEquals(Integer.valueOf(1), p2.elementSiblingIndex());

        assertNull(p3.nextElementSibling());
        assertEquals(p2, p3.previousElementSibling());
        assertEquals(Integer.valueOf(2), p3.elementSiblingIndex());

        Element single = new Element(Tag.valueOf("span"), "");
        assertEquals(Integer.valueOf(0), single.elementSiblingIndex());
    }

    @Test
    public void testFirstAndLastElementSiblingSingleChild() {
        Document doc = Jsoup.parse("<div><p>Only</p></div>");
        Element p = doc.select("p").first();
        assertNull(p.firstElementSibling());
        assertNull(p.lastElementSibling());
    }

    @Test
    public void testDomSelectionMethods() {
        Document doc = Jsoup.parse("<div id='main' class='wrapper dark' data-type='article' count='5'>" +
                "<p class='content' data-attr='test-start'>Para 1</p>" +
                "<p class='content' data-attr='end-test'>Para 2</p>" +
                "<p class='extra' data-attr='mid-test-val'>Para 3</p>" +
                "<span class='dark'>Span</span>" +
                "</div>");

        Element main = doc.getElementById("main");
        assertNotNull(main);
        assertNull(doc.getElementById("non-existent"));

        assertEquals(3, doc.getElementsByTag("P").size());
        assertEquals(0, doc.getElementsByTag("h1").size());

        assertEquals(2, doc.getElementsByClass("dark").size());
        assertEquals(2, doc.getElementsByClass("content").size());

        assertEquals(1, doc.getElementsByAttribute("count").size());
        assertEquals(3, doc.getElementsByAttribute("data-attr").size());

        assertEquals(1, doc.getElementsByAttributeValue("data-type", "article").size());
        assertEquals(0, doc.getElementsByAttributeValue("data-type", "news").size());

        assertTrue(doc.getElementsByAttributeValueNot("data-type", "article").size() > 0);

        assertEquals(1, doc.getElementsByAttributeValueStarting("data-attr", "test").size());
        assertEquals(1, doc.getElementsByAttributeValueEnding("data-attr", "test").size());
        assertEquals(3, doc.getElementsByAttributeValueContaining("data-attr", "test").size());

        assertEquals(1, doc.getElementsByIndexLessThan(1).size());
        assertTrue(doc.getElementsByIndexGreaterThan(0).size() >= 1);
        assertEquals(1, doc.getElementsByIndexEquals(0).size());

        Elements all = main.getAllElements();
        assertEquals(5, all.size()); // main + 3 p's + 1 span
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementByEmptyTag() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByTag("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementByEmptyId() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementById("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementByEmptyClass() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByClass("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementByEmptyAttribute() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttribute("");
    }

    @Test
    public void testTextAndWhitespacePreservation() {
        Document doc = Jsoup.parse("<div><p>Hello   \n  <b>World</b></p><pre>  Line 1\n  Line 2</pre></div>");
        assertEquals("Hello World Line 1\n  Line 2", doc.body().text());

        Element p = doc.select("p").first();
        assertEquals("Hello World", p.text());
        assertTrue(p.hasText());

        Element emptyP = new Element(Tag.valueOf("p"), "");
        assertFalse(emptyP.hasText());
        emptyP.appendText("   ");
        assertFalse(emptyP.hasText());

        Element parentEmpty = new Element(Tag.valueOf("div"), "");
        parentEmpty.appendChild(emptyP);
        assertFalse(parentEmpty.hasText());

        emptyP.text("New text");
        assertEquals("New text", emptyP.text());
        assertTrue(parentEmpty.hasText());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTextNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.text(null);
    }

    @Test
    public void testData() {
        Document doc = Jsoup.parse("<script>var x = 1;\nvar y = 2;</script>");
        Element script = doc.select("script").first();
        assertEquals("var x = 1;\nvar y = 2;", script.data());

        Element div = new Element(Tag.valueOf("div"), "");
        assertEquals("", div.data());
    }

    @Test
    public void testClassManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.className());
        assertEquals(0, el.classNames().size());
        assertFalse(el.hasClass("active"));

        el.addClass("active");
        assertEquals("active", el.className());
        assertTrue(el.hasClass("active"));

        el.addClass("active"); // Duplicate
        assertEquals("active", el.className());

        el.addClass("selected");
        assertEquals("active selected", el.className());
        assertTrue(el.hasClass("selected"));

        el.removeClass("active");
        assertEquals("selected", el.className());
        assertFalse(el.hasClass("active"));

        el.toggleClass("visible");
        assertTrue(el.hasClass("visible"));
        el.toggleClass("visible");
        assertFalse(el.hasClass("visible"));

        Set<String> customClasses = new LinkedHashSet<String>();
        customClasses.add("one");
        customClasses.add("two");
        el.classNames(customClasses);
        assertEquals("one two", el.className());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddClassNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveClassNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.removeClass(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToggleClassNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.toggleClass(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassNamesNull() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.classNames(null);
    }

    @Test
    public void testValMethod() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "foo");
        assertEquals("foo", input.val());
        input.val("bar");
        assertEquals("bar", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("Text content");
        assertEquals("Text content", textarea.val());
        textarea.val("New content");
        assertEquals("New content", textarea.text());
    }

    @Test
    public void testHtmlAndOuterHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>Hello</p>");
        assertEquals("<p>Hello</p>", div.html());
        assertEquals("<div>\n <p>Hello</p>\n</div>", div.outerHtml());
        assertEquals(div.outerHtml(), div.toString());

        Element img = new Element(Tag.valueOf("img"), "");
        assertEquals("<img />", img.outerHtml());

        Element span = new Element(Tag.valueOf("span"), "");
        span.text("inline");
        assertEquals("<span>inline</span>", span.outerHtml());
    }

    @Test
    public void testSelectQuery() {
        Document doc = Jsoup.parse("<div><p class='intro'>Hello</p><p>World</p></div>");
        Element div = doc.select("div").first();
        Elements pTags = div.select("p");
        assertEquals(2, pTags.size());
        Elements intro = div.select(".intro");
        assertEquals(1, intro.size());
        assertEquals("Hello", intro.first().text());
    }

    @Test
    public void testEqualsAndHashCode() {
        Tag tagDiv = Tag.valueOf("div");
        Tag tagSpan = Tag.valueOf("span");

        Element el1 = new Element(tagDiv, "http://a.com");
        Element el2 = new Element(tagDiv, "http://a.com");
        Element el3 = new Element(tagSpan, "http://a.com");

        assertTrue(el1.equals(el1));
        assertTrue(el1.equals(el2));
        assertEquals(el1.hashCode(), el2.hashCode());

        assertFalse(el1.equals(null));
        assertFalse(el1.equals("string"));
        assertFalse(el1.equals(el3));

        Element el4 = new Element(tagDiv, "http://b.com");
        assertFalse(el1.equals(el4));
    }
}
