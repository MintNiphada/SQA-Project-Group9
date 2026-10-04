package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testConstructorsAndNodeName() {
        Element el1 = new Element("div");
        assertEquals("div", el1.nodeName());
        assertEquals("div", el1.tagName());
        assertTrue(el1.isBlock());

        Element el2 = new Element(Tag.valueOf("span"), "http://example.com");
        assertEquals("span", el2.nodeName());
        assertEquals("http://example.com", el2.baseUri());
        assertFalse(el2.isBlock());

        Attributes attrs = new Attributes();
        attrs.put("id", "myId");
        Element el3 = new Element(Tag.valueOf("p"), "http://example.com", attrs);
        assertEquals("myId", el3.id());
    }

    @Test
    public void testTagName() {
        Element el = new Element("div");
        el.tagName("SPAN");
        assertEquals("SPAN", el.tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagNameEmpty() {
        Element el = new Element("div");
        el.tagName("");
    }

    @Test
    public void testTag() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "");
        assertSame(tag, el.tag());
    }

    @Test
    public void testId() {
        Element el = new Element("div");
        assertEquals("", el.id());
        el.attr("ID", "test");
        assertEquals("test", el.id());
    }

    @Test
    public void testAttrStringAndBoolean() {
        Element el = new Element("input");
        el.attr("type", "text");
        assertEquals("text", el.attr("type"));

        el.attr("disabled", true);
        assertTrue(el.hasAttr("disabled"));
        assertEquals("", el.attr("disabled"));

        el.attr("disabled", false);
        assertFalse(el.hasAttr("disabled"));
    }

    @Test
    public void testDataset() {
        Element el = new Element("div");
        el.attr("data-name", "jsoup");
        el.attr("data-lang", "java");
        el.attr("class", "code");

        Map<String, String> dataset = el.dataset();
        assertEquals(2, dataset.size());
        assertEquals("jsoup", dataset.get("name"));
        assertEquals("java", dataset.get("lang"));
    }

    @Test
    public void testParentAndParents() {
        Document doc = Jsoup.parse("<div><p><span>Text</span></p></div>");
        Element span = doc.select("span").first();
        Element p = span.parent();
        assertNotNull(p);
        assertEquals("p", p.tagName());

        Elements parents = span.parents();
        assertEquals(3, parents.size());
        assertEquals("p", parents.get(0).tagName());
        assertEquals("div", parents.get(1).tagName());
        assertEquals("body", parents.get(2).tagName());

        Element orphan = new Element("div");
        assertNull(orphan.parent());
        assertEquals(0, orphan.parents().size());
    }

    @Test
    public void testChildrenAndChild() {
        Element div = new Element("div");
        div.append("Text1<p>Para</p>Text2<span>Span</span>");

        Elements children = div.children();
        assertEquals(2, children.size());
        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildOutOfBounds() {
        Element div = new Element("div");
        div.child(0);
    }

    @Test
    public void testTextNodesAndDataNodes() {
        Element div = new Element("div");
        div.append("Text1<p>Para</p>Text2");
        List<TextNode> textNodes = div.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("Text1", textNodes.get(0).text());
        assertEquals("Text2", textNodes.get(1).text());

        Element script = new Element("script");
        script.appendChild(new DataNode("var a = 1;", ""));
        script.appendChild(new Comment("a comment"));
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var a = 1;", dataNodes.get(0).getWholeData());
    }

    @Test
    public void testSelectAndIs() {
        Document doc = Jsoup.parse("<div id='d1' class='test'><span class='inner'>Hi</span></div>");
        Element div = doc.getElementById("d1");

        Elements spans = div.select("span.inner");
        assertEquals(1, spans.size());
        assertEquals("Hi", spans.first().text());

        assertTrue(div.is("#d1"));
        assertTrue(div.is(".test"));
        assertFalse(div.is("p"));

        Evaluator eval = new Evaluator.Class("test");
        assertTrue(div.is(eval));
    }

    @Test
    public void testAppendPrependChild() {
        Element div = new Element("div");
        Element p1 = new Element("p").text("1");
        Element p2 = new Element("p").text("2");

        div.appendChild(p1);
        div.prependChild(p2);

        assertEquals("p", div.child(0).tagName());
        assertEquals("2", div.child(0).text());
        assertEquals("1", div.child(1).text());
    }

    @Test
    public void testInsertChildren() {
        Element div = new Element("div");
        Element p1 = new Element("p").text("1");
        Element p2 = new Element("p").text("2");
        div.appendChild(p1);
        div.appendChild(p2);

        Element ins = new Element("span").text("ins");
        div.insertChildren(1, Collections.singletonList(ins));
        assertEquals(3, div.children().size());
        assertEquals("ins", div.child(1).text());

        Element insLast = new Element("span").text("last");
        div.insertChildren(-1, Collections.singletonList(insLast));
        assertEquals("last", div.child(div.children().size() - 1).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenNull() {
        Element div = new Element("div");
        div.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element div = new Element("div");
        div.insertChildren(5, Collections.emptyList());
    }

    @Test
    public void testAppendPrependElementAndText() {
        Element div = new Element("div");
        Element span = div.appendElement("span").text("End");
        Element h1 = div.prependElement("h1").text("Start");
        div.appendText(" TextEnd ");
        div.prependText(" TextStart ");

        assertEquals("h1", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
        assertTrue(div.text().startsWith("TextStart"));
        assertTrue(div.text().endsWith("TextEnd"));
    }

    @Test
    public void testAppendPrependHtml() {
        Element div = new Element("div");
        div.append("<span>1</span>");
        div.prepend("<p>0</p>");

        assertEquals("p", div.child(0).tagName());
        assertEquals("span", div.child(1).tagName());
    }

    @Test
    public void testBeforeAfterMethods() {
        Element div = new Element("div");
        Element p = div.appendElement("p").text("middle");

        p.before("<h1>beforeHtml</h1>");
        p.before(new Element("h2").text("beforeNode"));
        p.after("<h3>afterHtml</h3>");
        p.after(new Element("h4").text("afterNode"));

        assertEquals("h1", div.child(0).tagName());
        assertEquals("h2", div.child(1).tagName());
        assertEquals("p", div.child(2).tagName());
        assertEquals("h4", div.child(3).tagName());
        assertEquals("h3", div.child(4).tagName());
    }

    @Test
    public void testEmptyAndWrap() {
        Element div = new Element("div");
        div.append("<p>1</p><p>2</p>");
        assertEquals(2, div.children().size());
        div.empty();
        assertEquals(0, div.children().size());

        Document doc = Jsoup.parse("<div id='wrapMe'>Hello</div>");
        Element wrapMe = doc.getElementById("wrapMe");
        wrapMe.wrap("<div class='wrapper'></div>");
        assertEquals("wrapper", wrapMe.parent().className());
    }

    @Test
    public void testCssSelector() {
        Document doc = Jsoup.parse("<html><body><div id='d1'></div><div class='c1 c2'><p>One</p><p>Two</p></div></body></html>");
        Element d1 = doc.getElementById("d1");
        assertEquals("#d1", d1.cssSelector());

        Element body = doc.body();
        assertEquals("html > body", body.cssSelector());

        Element p2 = doc.select("p").get(1);
        assertEquals("html > body > div.c1.c2 > p:nth-child(2)", p2.cssSelector());

        Element customTag = new Element("ns:custom");
        assertEquals("ns|custom", customTag.cssSelector());
    }

    @Test
    public void testSiblings() {
        Document doc = Jsoup.parse("<div><p id='p1'>1</p><p id='p2'>2</p><p id='p3'>3</p></div>");
        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");
        Element p3 = doc.getElementById("p3");

        assertEquals(2, p2.siblingElements().size());
        assertEquals(p3, p2.nextElementSibling());
        assertNull(p3.nextElementSibling());
        assertEquals(p1, p2.previousElementSibling());
        assertNull(p1.previousElementSibling());

        assertEquals(p1, p2.firstElementSibling());
        assertEquals(p3, p2.lastElementSibling());
        assertEquals(Integer.valueOf(1), p2.elementSiblingIndex());

        Element orphan = new Element("p");
        assertEquals(0, orphan.siblingElements().size());
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.previousElementSibling());
        assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());

        Element singleChild = new Element("div").appendElement("span");
        assertNull(singleChild.firstElementSibling());
        assertNull(singleChild.lastElementSibling());
    }

    @Test
    public void testGetElementsByMethods() {
        Document doc = Jsoup.parse("<div id='main' class='c1 c2' data-type='test' data-name='jsoup' val='123'>" +
                "<p class='c1' title='heading'>Hello World</p>" +
                "<p class='sub' title='subhead'>Own text here<span>child</span></p>" +
                "</div>");

        Element main = doc.getElementById("main");
        assertEquals(2, main.getElementsByTag("p").size());
        assertEquals("main", main.getElementById("main").id());
        assertNull(main.getElementById("nonexistent"));

        assertEquals(2, main.getElementsByClass("c1").size());
        assertEquals(1, main.getElementsByClass("c2").size());

        assertEquals(1, main.getElementsByAttribute("val").size());
        assertEquals(2, main.getElementsByAttributeStarting("data-").size());
        assertEquals(1, main.getElementsByAttributeValue("data-type", "test").size());
        assertEquals(1, main.getElementsByAttributeValueNot("data-type", "other").size());
        assertEquals(1, main.getElementsByAttributeValueStarting("title", "head").size());
        assertEquals(1, main.getElementsByAttributeValueEnding("title", "head").size());
        assertEquals(2, main.getElementsByAttributeValueContaining("title", "head").size());

        assertEquals(1, main.getElementsByAttributeValueMatching("title", Pattern.compile("^head.*")).size());
        assertEquals(1, main.getElementsByAttributeValueMatching("title", "^head.*").size());

        assertEquals(1, main.getElementsByIndexLessThan(1).size());
        assertEquals(1, main.getElementsByIndexGreaterThan(0).size());
        assertEquals(1, main.getElementsByIndexEquals(1).size());

        assertEquals(1, main.getElementsContainingText("World").size());
        assertEquals(1, main.getElementsContainingOwnText("Own text").size());
        assertEquals(0, main.getElementsContainingOwnText("child").size());

        assertEquals(1, main.getElementsMatchingText(Pattern.compile(".*World.*")).size());
        assertEquals(1, main.getElementsMatchingText(".*World.*").size());
        assertEquals(1, main.getElementsMatchingOwnText(Pattern.compile(".*Own text.*")).size());
        assertEquals(1, main.getElementsMatchingOwnText(".*Own text.*").size());

        assertTrue(main.getAllElements().size() >= 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidPattern() {
        Element el = new Element("div");
        el.getElementsByAttributeValueMatching("title", "[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextInvalidPattern() {
        Element el = new Element("div");
        el.getElementsMatchingText("[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingOwnTextInvalidPattern() {
        Element el = new Element("div");
        el.getElementsMatchingOwnText("[invalid");
    }

    @Test
    public void testTextAndOwnText() {
        Document doc = Jsoup.parse("<div>Hello <b>there</b><br>world! <pre> formatted   space </pre></div>");
        Element div = doc.select("div").first();

        assertEquals("Hello there world! formatted   space", div.text());
        assertEquals("Hello world!", div.ownText());

        Element p = new Element("p");
        assertFalse(p.hasText());
        p.text("  ");
        assertFalse(p.hasText());
        p.text("sample");
        assertTrue(p.hasText());
        assertEquals("sample", p.text());

        Element pre = new Element("pre");
        pre.appendChild(new TextNode(" a   b ", ""));
        assertEquals(" a   b ", pre.text());
    }

    @Test
    public void testData() {
        Element script = new Element("script");
        script.appendChild(new DataNode("var x = 5;", ""));
        script.appendChild(new Comment("comment"));
        Element inner = new Element("inner");
        inner.appendChild(new DataNode("var y = 10;", ""));
        script.appendChild(inner);

        assertEquals("var x = 5;commentvar y = 10;", script.data());
    }

    @Test
    public void testClassMethods() {
        Element div = new Element("div");
        assertEquals("", div.className());
        assertEquals(0, div.classNames().size());

        div.attr("class", "  header   active  ");
        assertEquals("header   active", div.className());
        Set<String> classNames = div.classNames();
        assertEquals(2, classNames.size());
        assertTrue(classNames.contains("header"));
        assertTrue(classNames.contains("active"));

        assertTrue(div.hasClass("header"));
        assertTrue(div.hasClass("ACTIVE"));
        assertFalse(div.hasClass("act"));
        assertFalse(div.hasClass("nonexistent"));

        div.classNames(new HashSet<String>(Arrays.asList("c1", "c2")));
        assertEquals("c1 c2", div.className());

        div.addClass("c3");
        assertTrue(div.hasClass("c3"));

        div.removeClass("c1");
        assertFalse(div.hasClass("c1"));

        div.toggleClass("c2");
        assertFalse(div.hasClass("c2"));
        div.toggleClass("c2");
        assertTrue(div.hasClass("c2"));
    }

    @Test
    public void testHasClassBoundaries() {
        Element div = new Element("div");
        assertFalse(div.hasClass("test"));

        div.attr("class", "test");
        assertTrue(div.hasClass("test"));
        assertFalse(div.hasClass("testing"));
        assertFalse(div.hasClass("tes"));

        div.attr("class", "first second third");
        assertTrue(div.hasClass("first"));
        assertTrue(div.hasClass("second"));
        assertTrue(div.hasClass("third"));
        assertFalse(div.hasClass("sec"));
    }

    @Test
    public void testVal() {
        Element input = new Element("input").attr("value", "foo");
        assertEquals("foo", input.val());
        input.val("bar");
        assertEquals("bar", input.val());

        Element textarea = new Element("textarea").text("content");
        assertEquals("content", textarea.val());
        textarea.val("newContent");
        assertEquals("newContent", textarea.val());
    }

    @Test
    public void testHtmlAndOuterHtml() throws IOException {
        Element div = new Element("div");
        div.html("<p>Paragraph</p>");
        assertEquals("<p>Paragraph</p>", div.html());
        assertEquals("<div>\n <p>Paragraph</p>\n</div>", div.outerHtml());

        StringBuilder sb = new StringBuilder();
        div.html(sb);
        assertEquals("<p>Paragraph</p>", sb.toString());
        assertEquals(div.outerHtml(), div.toString());

        Element img = new Element("img");
        assertEquals("<img>", img.outerHtml());

        Document doc = Jsoup.parse("<xml><img /></xml>", "", org.jsoup.parser.Parser.xmlParser());
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        assertEquals("<img />", doc.select("img").first().outerHtml());

        Element customSelfClosing = new Element(Tag.valueOf("custom", org.jsoup.parser.ParseSettings.htmlDefault).setSelfClosing(), "");
        assertEquals("<custom />", customSelfClosing.outerHtml());
    }

    @Test
    public void testOuterHtmlWithOutline() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p></div>");
        doc.outputSettings().outline(true);
        String html = doc.body().outerHtml();
        assertTrue(html.contains("<p>"));
    }

    @Test
    public void testClone() {
        Element el = new Element("div");
        el.attr("id", "orig");
        el.append("<p>Child</p>");

        Element clone = el.clone();
        assertEquals("orig", clone.id());
        assertEquals(1, clone.children().size());

        clone.attr("id", "newId");
        clone.child(0).text("Modified");

        assertEquals("orig", el.id());
        assertEquals("Child", el.child(0).text());
        assertEquals("newId", clone.id());
        assertEquals("Modified", clone.child(0).text());
    }
}
