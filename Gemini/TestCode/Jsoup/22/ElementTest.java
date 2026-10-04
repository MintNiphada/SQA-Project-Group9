package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testConstructorsAndTag() {
        Tag divTag = Tag.valueOf("div");
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        Element el = new Element(divTag, "http://example.com/", attrs);

        assertEquals("div", el.nodeName());
        assertEquals("div", el.tagName());
        assertEquals(divTag, el.tag());
        assertEquals("http://example.com/", el.baseUri());
        assertEquals("main", el.id());
        assertTrue(el.isBlock());

        Element simpleEl = new Element(Tag.valueOf("span"), "http://example.com/");
        assertEquals("span", simpleEl.tagName());
        assertFalse(simpleEl.isBlock());
        assertEquals("", simpleEl.id());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com/");
    }

    @Test
    public void testChangeTagName() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.tagName());
        el.tagName("div");
        assertEquals("div", el.tagName());
        assertTrue(el.isBlock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeTagNameEmpty() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.tagName("");
    }

    @Test
    public void testAttrAndDataset() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("data-name", "jsoup");
        el.attr("data-type", "html-parser");
        el.attr("class", "container");

        assertEquals("jsoup", el.attr("data-name"));
        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("jsoup", dataset.get("name"));
        assertEquals("html-parser", dataset.get("type"));
    }

    @Test
    public void testParentAndParents() {
        Document doc = Jsoup.parse("<html><body><div><p><span>Hello</span></p></div></body></html>");
        Element span = doc.select("span").first();
        assertNotNull(span);

        Element p = span.parent();
        assertEquals("p", p.tagName());

        Elements parents = span.parents();
        assertEquals(4, parents.size());
        assertEquals("p", parents.get(0).tagName());
        assertEquals("div", parents.get(1).tagName());
        assertEquals("body", parents.get(2).tagName());
        assertEquals("html", parents.get(3).tagName());

        Element orphan = new Element(Tag.valueOf("div"), "");
        assertNull(orphan.parent());
        assertEquals(0, orphan.parents().size());
    }

    @Test
    public void testChildAndChildren() {
        Document doc = Jsoup.parse("<div>Text 1<p>Para 1</p>Text 2<span>Span 1</span></div>");
        Element div = doc.select("div").first();

        Elements children = div.children();
        assertEquals(2, children.size());
        assertEquals("p", children.get(0).tagName());
        assertEquals("span", children.get(1).tagName());

        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());

        List<TextNode> textNodes = div.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Text 1", textNodes.get(0).text());
        assertEquals("Text 2", textNodes.get(1).text());
    }

    @Test
    public void testDataNodes() {
        Document doc = Jsoup.parse("<script>var x = 1;</script>");
        Element script = doc.select("script").first();
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var x = 1;", dataNodes.get(0).getWholeData());
        assertEquals("var x = 1;", script.data());

        Element div = new Element(Tag.valueOf("div"), "");
        assertEquals(0, div.dataNodes().size());
        assertEquals("", div.data());
    }

    @Test
    public void testDataNested() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("script"), "");
        child.appendChild(new DataNode("alert(1);", ""));
        parent.appendChild(child);
        assertEquals("alert(1);", parent.data());
    }

    @Test
    public void testAppendAndPrependChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");

        parent.appendChild(child1);
        parent.prependChild(child2);

        assertEquals("span", parent.child(0).tagName());
        assertEquals("p", parent.child(1).tagName());
    }

    @Test
    public void testAppendAndPrependElement() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element p = parent.appendElement("p").attr("id", "first");
        Element h1 = parent.prependElement("h1").attr("id", "header");

        assertEquals("h1", parent.child(0).tagName());
        assertEquals("header", h1.id());
        assertEquals("p", parent.child(1).tagName());
        assertEquals("first", p.id());
    }

    @Test
    public void testAppendAndPrependText() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("World");
        parent.prependText("Hello ");

        assertEquals("Hello World", parent.text());
    }

    @Test
    public void testAppendAndPrependHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>Paragraph</p>");
        assertEquals(1, div.children().size());
        assertEquals("p", div.child(0).tagName());

        div.prepend("<h1>Title</h1>");
        assertEquals(2, div.children().size());
        assertEquals("h1", div.child(0).tagName());
        assertEquals("p", div.child(1).tagName());
    }

    @Test
    public void testBeforeAndAfterStringAndNode() {
        Document doc = Jsoup.parse("<div><p id=\"target\">Middle</p></div>");
        Element target = doc.getElementById("target");

        target.before("<span>BeforeHtml</span>");
        Element beforeEl = new Element(Tag.valueOf("b"), "");
        beforeEl.text("BeforeNode");
        target.before(beforeEl);

        target.after("<span>AfterHtml</span>");
        Element afterEl = new Element(Tag.valueOf("i"), "");
        afterEl.text("AfterNode");
        target.after(afterEl);

        Element div = doc.select("div").first();
        assertEquals("<span>BeforeHtml</span><b>BeforeNode</b><p id=\"target\">Middle</p><i>AfterNode</i><span>AfterHtml</span>",
                div.html().replaceAll("\\s*\\n\\s*", ""));
    }

    @Test
    public void testEmptyAndWrap() {
        Document doc = Jsoup.parse("<div id=\"out\"><p>Text 1</p><span>Text 2</span></div>");
        Element out = doc.getElementById("out");
        assertFalse(out.childNodes.isEmpty());
        out.empty();
        assertTrue(out.childNodes.isEmpty());
        assertEquals("", out.html());

        Element p = new Element(Tag.valueOf("p"), "");
        p.text("Wrapped");
        doc.body().appendChild(p);
        p.wrap("<div class=\"wrapper\"></div>");

        Element wrapper = doc.select(".wrapper").first();
        assertNotNull(wrapper);
        assertEquals("p", wrapper.child(0).tagName());
    }

    @Test
    public void testSiblingNavigation() {
        Document doc = Jsoup.parse("<div><p id=\"p1\">1</p><p id=\"p2\">2</p><p id=\"p3\">3</p></div>");
        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");
        Element p3 = doc.getElementById("p3");

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

        Elements siblings = p2.siblingElements();
        assertEquals(3, siblings.size());

        Element orphan = new Element(Tag.valueOf("div"), "");
        assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());

        Document singleDoc = Jsoup.parse("<div><p id=\"single\">Single</p></div>");
        Element single = singleDoc.getElementById("single");
        assertNull(single.firstElementSibling());
        assertNull(single.lastElementSibling());
    }

    @Test
    public void testSelectQuery() {
        Document doc = Jsoup.parse("<div id=\"main\"><a href=\"http://example.com/a\">Link A</a><a href=\"http://other.com/b\">Link B</a></div>");
        Elements links = doc.select("a[href*=example.com]");
        assertEquals(1, links.size());
        assertEquals("Link A", links.first().text());
    }

    @Test
    public void testGetElementsByTagAndId() {
        Document doc = Jsoup.parse("<div id=\"root\"><p id=\"p1\">One</p><p id=\"p2\">Two</p></div>");
        Elements ps = doc.getElementsByTag("P");
        assertEquals(2, ps.size());

        Element found = doc.getElementById("p2");
        assertNotNull(found);
        assertEquals("Two", found.text());

        Element notFound = doc.getElementById("non-existent");
        assertNull(notFound);
    }

    @Test
    public void testGetElementsByClass() {
        Document doc = Jsoup.parse("<div class=\"box red\"></div><div class=\"box blue\"></div><span class=\"blue\"></span>");
        Elements boxes = doc.getElementsByClass("box");
        assertEquals(2, boxes.size());

        Elements blues = doc.getElementsByClass("BLUE");
        assertEquals(2, blues.size());
    }

    @Test
    public void testGetElementsByAttributeMethods() {
        Document doc = Jsoup.parse("<div id=\"d1\" data-cat=\"alpha\" title=\"first tooltip\"></div>" +
                "<div id=\"d2\" data-cat=\"beta\" title=\"second tooltip\"></div>" +
                "<div id=\"d3\" data-dog=\"gamma\" title=\"tooltip test\"></div>");

        assertEquals(3, doc.getElementsByAttribute("title").size());
        assertEquals(2, doc.getElementsByAttributeStarting("data-cat").size());
        assertEquals(3, doc.getElementsByAttributeStarting("data-").size());
        assertEquals(1, doc.getElementsByAttributeValue("data-cat", "alpha").size());
        assertEquals(2, doc.getElementsByAttributeValueNot("data-cat", "alpha").size()); // includes doc/body/etc that lack it or diff value
        assertEquals(2, doc.getElementsByAttributeValueStarting("title", "second").size() + doc.getElementsByAttributeValueStarting("title", "first").size());
        assertEquals(2, doc.getElementsByAttributeValueEnding("title", "tooltip").size());
        assertEquals(3, doc.getElementsByAttributeValueContaining("title", "tool").size());

        assertEquals(2, doc.getElementsByAttributeValueMatching("data-cat", Pattern.compile("alpha|beta")).size());
        assertEquals(2, doc.getElementsByAttributeValueMatching("data-cat", "alpha|beta").size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.getElementsByAttributeValueMatching("id", "[invalid");
    }

    @Test
    public void testGetElementsByIndex() {
        Document doc = Jsoup.parse("<ul><li>1</li><li>2</li><li>3</li><li>4</li><li>5</li></ul>");
        Element ul = doc.select("ul").first();

        assertEquals(2, ul.getElementsByIndexLessThan(2).size());
        assertEquals(2, ul.getElementsByIndexGreaterThan(2).size());
        assertEquals(1, ul.getElementsByIndexEquals(2).size());
        assertEquals("3", ul.getElementsByIndexEquals(2).first().text());
    }

    @Test
    public void testGetElementsContainingTextAndMatching() {
        Document doc = Jsoup.parse("<div id=\"d1\">Hello <span>World</span></div><div id=\"d2\">Goodbye world</div>");

        assertEquals(2, doc.getElementsContainingText("world").size());
        assertEquals(1, doc.getElementsContainingOwnText("Goodbye").size());
        assertEquals(0, doc.getElementById("d1").getElementsContainingOwnText("World").size());

        assertEquals(2, doc.getElementsMatchingText(Pattern.compile("(?i)world")).size());
        assertEquals(2, doc.getElementsMatchingText("(?i)world").size());

        assertEquals(1, doc.getElementsMatchingOwnText(Pattern.compile("Goodbye.*")).size());
        assertEquals(1, doc.getElementsMatchingOwnText("Goodbye.*").size());

        assertEquals(5, doc.getAllElements().size()); // html, head, body, div#d1, span, div#d2 (or all elements in tree)
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextInvalidRegex() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.getElementsMatchingText("[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingOwnTextInvalidRegex() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.getElementsMatchingOwnText("[invalid");
    }

    @Test
    public void testTextAndOwnText() {
        Document doc = Jsoup.parse("<p>Hello <b>there</b> now!<br>Next line</p>");
        Element p = doc.select("p").first();

        assertEquals("Hello there now! Next line", p.text());
        assertEquals("Hello now! Next line", p.ownText());

        assertTrue(p.hasText());
        p.text("Brand new text");
        assertEquals("Brand new text", p.text());
        assertEquals(1, p.textNodes().size());
        assertEquals(0, p.children().size());

        Element emptyEl = new Element(Tag.valueOf("div"), "");
        assertFalse(emptyEl.hasText());
        assertEquals("", emptyEl.text());
        assertEquals("", emptyEl.ownText());
    }

    @Test
    public void testTextWithBlockElements() {
        Document doc = Jsoup.parse("<div><div>First</div><div>Second</div></div>");
        Element root = doc.select("div").first();
        assertEquals("First Second", root.text());
    }

    @Test
    public void testPreserveWhitespace() {
        Document doc = Jsoup.parse("<pre>  line 1\n  line 2  </pre>");
        Element pre = doc.select("pre").first();
        assertTrue(pre.preserveWhitespace());
        assertEquals("  line 1\n  line 2  ", pre.text());

        Element span = pre.appendElement("span");
        span.text("  span  ");
        assertTrue(span.preserveWhitespace());
    }

    @Test
    public void testClassNamesAndManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "one two three");

        assertEquals("one two three", el.className());
        Set<String> classes = el.classNames();
        assertEquals(3, classes.size());
        assertTrue(classes.contains("one"));
        assertTrue(classes.contains("two"));
        assertTrue(classes.contains("three"));

        assertTrue(el.hasClass("ONE"));
        assertTrue(el.hasClass("two"));
        assertFalse(el.hasClass("four"));

        el.addClass("four");
        assertTrue(el.hasClass("four"));
        assertEquals("one two three four", el.className());

        el.removeClass("two");
        assertFalse(el.hasClass("two"));
        assertEquals("one three four", el.className());

        el.toggleClass("five");
        assertTrue(el.hasClass("five"));
        el.toggleClass("five");
        assertFalse(el.hasClass("five"));

        Set<String> newClasses = new LinkedHashSet<String>();
        newClasses.add("alpha");
        newClasses.add("beta");
        el.classNames(newClasses);
        assertEquals("alpha beta", el.className());
    }

    @Test
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("test-value");
        assertEquals("test-value", input.val());
        assertEquals("test-value", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("multiline\ntext");
        assertEquals("multiline\ntext", textarea.val());
        assertEquals("multiline\ntext", textarea.text());
    }

    @Test
    public void testHtmlAndOuterHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>Hello</p><span>World</span>");
        assertEquals("<p>Hello</p>\n<span>World</span>", div.html());

        Document doc = Jsoup.parse("<img src=\"test.jpg\">");
        Element img = doc.select("img").first();
        assertEquals("<img src=\"test.jpg\" />", img.outerHtml());

        assertEquals("<div>\n <p>Hello</p>\n <span>World</span>\n</div>", div.toString());
    }

    @Test
    public void testEqualsHashCodeAndClone() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com/");
        el1.attr("id", "main");
        el1.addClass("container");

        Element el2 = new Element(Tag.valueOf("div"), "http://example.com/");
        el2.attr("id", "main");

        assertTrue(el1.equals(el1));
        assertFalse(el1.equals(el2));
        assertFalse(el1.equals(null));
        assertFalse(el1.equals("string"));

        assertTrue(el1.hashCode() != 0);

        Element cloned = el1.clone();
        assertNotSame(el1, cloned);
        assertEquals(el1.tagName(), cloned.tagName());
        assertEquals(el1.attr("id"), cloned.attr("id"));
        assertTrue(cloned.hasClass("container"));
    }
}
