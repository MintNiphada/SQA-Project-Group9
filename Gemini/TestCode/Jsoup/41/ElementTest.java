package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

public class ElementTest {

    @Test
    public void testConstructorsAndTagProperties() {
        Element el = new Element(Tag.valueOf("DIV"), "http://example.com");
        Assert.assertEquals("div", el.nodeName());
        Assert.assertEquals("div", el.tagName());
        Assert.assertTrue(el.isBlock());
        Assert.assertEquals("http://example.com", el.baseUri());
        Assert.assertEquals(0, el.attributes().size());

        el.tagName("span");
        Assert.assertEquals("span", el.tagName());
        Assert.assertFalse(el.isBlock());
        Assert.assertEquals(Tag.valueOf("span"), el.tag());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTagNameThrows() {
        Element el = new Element(Tag.valueOf("p"), "");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTagThrows() {
        new Element(null, "");
    }

    @Test
    public void testIdAndAttributes() {
        Element el = new Element(Tag.valueOf("div"), "");
        Assert.assertEquals("", el.id());
        el.attr("id", "main");
        Assert.assertEquals("main", el.id());
        el.attr("data-custom", "value");
        Assert.assertEquals("value", el.dataset().get("custom"));
    }

    @Test
    public void testParentAndParentsHierarchy() {
        Document doc = Jsoup.parse("<html><body><div id='d1'><p id='p1'><span>Text</span></p></div></body></html>");
        Element span = doc.select("span").first();
        Assert.assertNotNull(span);
        Assert.assertEquals("p", span.parent().tagName());

        Elements parents = span.parents();
        Assert.assertEquals(4, parents.size());
        Assert.assertEquals("p", parents.get(0).tagName());
        Assert.assertEquals("div", parents.get(1).tagName());
        Assert.assertEquals("body", parents.get(2).tagName());
        Assert.assertEquals("html", parents.get(3).tagName());

        Element orphan = new Element(Tag.valueOf("div"), "");
        Assert.assertNull(orphan.parent());
        Assert.assertEquals(0, orphan.parents().size());
    }

    @Test
    public void testChildrenAndChildNodes() {
        Document doc = Jsoup.parse("<div>Text 1<p>Para</p>Text 2<script>var x = 1;</script><span>Span</span></div>");
        Element div = doc.select("div").first();

        Assert.assertEquals(2, div.children().size());
        Assert.assertEquals("p", div.child(0).tagName());
        Assert.assertEquals("span", div.child(1).tagName());

        List<TextNode> textNodes = div.textNodes();
        Assert.assertEquals(2, textNodes.size());
        Assert.assertEquals("Text 1", textNodes.get(0).text());
        Assert.assertEquals("Text 2", textNodes.get(1).text());

        Element script = div.select("script").first();
        List<DataNode> dataNodes = script.dataNodes();
        Assert.assertEquals(1, dataNodes.size());
        Assert.assertEquals("var x = 1;", script.data());
    }

    @Test
    public void testAppendAndPrependNodes() {
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.appendElement("p").text("First");
        div.prependElement("span").text("Zero");
        div.appendText(" trailing");
        div.prependText("leading ");

        Assert.assertEquals(4, div.childNodeSize());
        Assert.assertEquals("leading ", ((TextNode) div.childNode(0)).getWholeText());
        Assert.assertEquals("span", div.child(0).tagName());
        Assert.assertEquals("p", div.child(1).tagName());
        Assert.assertEquals(" trailing", ((TextNode) div.childNode(3)).getWholeText());

        div.append("<b>bold</b>");
        div.prepend("<i>italic</i>");
        Assert.assertEquals("italic", div.child(0).text());
        Assert.assertEquals("bold", div.child(div.children().size() - 1).text());
    }

    @Test
    public void testInsertChildren() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p1 = new Element(Tag.valueOf("p"), "").text("1");
        Element p2 = new Element(Tag.valueOf("p"), "").text("2");
        div.appendChild(p1);

        List<Node> toInsert = Collections.singletonList(p2);
        div.insertChildren(0, toInsert);
        Assert.assertEquals(2, div.children().size());
        Assert.assertEquals("2", div.child(0).text());
        Assert.assertEquals("1", div.child(1).text());

        Element p3 = new Element(Tag.valueOf("p"), "").text("3");
        div.insertChildren(-1, Collections.singletonList(p3));
        Assert.assertEquals(3, div.children().size());
        Assert.assertEquals("3", div.child(2).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.insertChildren(5, Collections.emptyList());
    }

    @Test
    public void testSiblingsNavigation() {
        Document doc = Jsoup.parse("<div><p id='1'></p><p id='2'></p><p id='3'></p></div>");
        Element p1 = doc.getElementById("1");
        Element p2 = doc.getElementById("2");
        Element p3 = doc.getElementById("3");

        Assert.assertEquals(p2, p1.nextElementSibling());
        Assert.assertNull(p1.previousElementSibling());
        Assert.assertEquals(p1, p2.previousElementSibling());
        Assert.assertEquals(p3, p2.nextElementSibling());
        Assert.assertNull(p3.nextElementSibling());

        Assert.assertEquals(p1, p2.firstElementSibling());
        Assert.assertEquals(p3, p2.lastElementSibling());
        Assert.assertEquals(Integer.valueOf(1), p2.elementSiblingIndex());
        Assert.assertEquals(2, p2.siblingElements().size());

        Element orphan = new Element(Tag.valueOf("p"), "");
        Assert.assertNull(orphan.nextElementSibling());
        Assert.assertNull(orphan.previousElementSibling());
        Assert.assertEquals(0, orphan.siblingElements().size());
        Assert.assertEquals(Integer.valueOf(0), orphan.elementSiblingIndex());
    }

    @Test
    public void testCssSelector() {
        Document doc = Jsoup.parse("<div id='d'><div class='c1 c2'><p>1</p><p>2</p></div></div>");
        Element d = doc.getElementById("d");
        Assert.assertEquals("#d", d.cssSelector());

        Element innerDiv = doc.select(".c1").first();
        Assert.assertEquals("#d > div.c1.c2", innerDiv.cssSelector());

        Element p2 = doc.select("p").get(1);
        Assert.assertEquals("#d > div.c1.c2 > p:nth-child(2)", p2.cssSelector());

        Element orphan = new Element(Tag.valueOf("span"), "");
        orphan.addClass("test");
        Assert.assertEquals("span.test", orphan.cssSelector());
    }

    @Test
    public void testDomQueryMethods() {
        Document doc = Jsoup.parse("<div id='d' class='main hero' title='greeting heading' data-num='123'><p class='sub'>One</p><p class='sub' val='abc-456'>Two</p><a href='http://jsoup.org'>Jsoup</a></div>");
        Element root = doc.body();

        Assert.assertEquals(1, root.getElementsByTag("div").size());
        Assert.assertEquals(2, root.getElementsByTag("p").size());
        Assert.assertEquals(doc.getElementById("d"), root.getElementById("d"));
        Assert.assertNull(root.getElementById("nonexistent"));

        Assert.assertEquals(1, root.getElementsByClass("main").size());
        Assert.assertEquals(2, root.getElementsByClass("sub").size());

        Assert.assertEquals(1, root.getElementsByAttribute("title").size());
        Assert.assertEquals(1, root.getElementsByAttributeStarting("data-").size());
        Assert.assertEquals(1, root.getElementsByAttributeValue("title", "greeting heading").size());
        Assert.assertTrue(root.getElementsByAttributeValueNot("title", "other").size() > 0);
        Assert.assertEquals(1, root.getElementsByAttributeValueStarting("title", "greet").size());
        Assert.assertEquals(1, root.getElementsByAttributeValueEnding("title", "heading").size());
        Assert.assertEquals(1, root.getElementsByAttributeValueContaining("title", "eeti").size());

        Assert.assertEquals(1, root.getElementsByAttributeValueMatching("val", Pattern.compile("\\w+-\\d+")).size());
        Assert.assertEquals(1, root.getElementsByAttributeValueMatching("val", "\\w+-\\d+").size());

        Assert.assertEquals(1, root.getElementsByIndexLessThan(1).size());
        Assert.assertEquals(1, root.getElementsByIndexGreaterThan(0).size());
        Assert.assertEquals(1, root.getElementsByIndexEquals(0).size());

        Assert.assertEquals(1, root.getElementsContainingText("One").size());
        Assert.assertEquals(1, root.getElementsContainingOwnText("One").size());
        Assert.assertEquals(1, root.getElementsMatchingText(Pattern.compile("Two")).size());
        Assert.assertEquals(1, root.getElementsMatchingText("Two").size());
        Assert.assertEquals(1, root.getElementsMatchingOwnText(Pattern.compile("Two")).size());
        Assert.assertEquals(1, root.getElementsMatchingOwnText("Two").size());
        Assert.assertTrue(root.getAllElements().size() >= 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPatternAttributeValueMatching() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("key", "[unclosed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPatternMatchingText() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPatternMatchingOwnText() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("[unclosed");
    }

    @Test
    public void testTextAndOwnText() {
        Document doc = Jsoup.parse("<div>Hello <b>world</b>!<br>New line <pre>  preserved   whitespace </pre></div>");
        Element div = doc.select("div").first();

        Assert.assertEquals("Hello world! New line preserved whitespace", div.text());
        Assert.assertEquals("Hello !", div.ownText());
        Assert.assertTrue(div.hasText());

        Element empty = new Element(Tag.valueOf("div"), "");
        Assert.assertEquals("", empty.text());
        Assert.assertEquals("", empty.ownText());
        Assert.assertFalse(empty.hasText());

        Element pre = doc.select("pre").first();
        Assert.assertEquals("preserved whitespace", pre.text());

        empty.text("New text & content");
        Assert.assertEquals("New text & content", empty.text());
        Assert.assertEquals(1, empty.childNodes().size());
    }

    @Test
    public void testPreserveWhitespace() {
        Document doc = Jsoup.parse("<pre><span>  abc  </span></pre>");
        Element span = doc.select("span").first();
        Assert.assertTrue(Element.preserveWhitespace(span));
        Assert.assertTrue(Element.preserveWhitespace(span.parent()));
        Assert.assertFalse(Element.preserveWhitespace(new Element(Tag.valueOf("div"), "")));
        Assert.assertFalse(Element.preserveWhitespace(null));
    }

    @Test
    public void testClassManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        Assert.assertEquals("", el.className());
        Assert.assertEquals(0, el.classNames().size());
        Assert.assertFalse(el.hasClass("active"));

        el.addClass("active");
        Assert.assertEquals("active", el.className());
        Assert.assertTrue(el.hasClass("active"));
        Assert.assertTrue(el.hasClass("ACTIVE"));

        el.addClass("highlight");
        Assert.assertEquals(2, el.classNames().size());

        el.removeClass("active");
        Assert.assertFalse(el.hasClass("active"));
        Assert.assertTrue(el.hasClass("highlight"));

        el.toggleClass("toggled");
        Assert.assertTrue(el.hasClass("toggled"));
        el.toggleClass("toggled");
        Assert.assertFalse(el.hasClass("toggled"));

        Set<String> custom = new HashSet<String>(Arrays.asList("a", "b"));
        el.classNames(custom);
        Assert.assertTrue(el.hasClass("a"));
        Assert.assertTrue(el.hasClass("b"));
    }

    @Test
    public void testValMethod() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("foo");
        Assert.assertEquals("foo", input.val());
        Assert.assertEquals("foo", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("bar");
        Assert.assertEquals("bar", textarea.val());
        Assert.assertEquals("bar", textarea.text());
    }

    @Test
    public void testHtmlAndOuterHtml() {
        Document doc = Jsoup.parse("<div><p>Paragraph</p></div>");
        Element div = doc.select("div").first();

        Assert.assertEquals("<p>Paragraph</p>", div.html());
        Assert.assertEquals("<div>\n <p>Paragraph</p>\n</div>", div.outerHtml());
        Assert.assertEquals(div.outerHtml(), div.toString());

        div.html("<span>Replaced</span>");
        Assert.assertEquals("<span>Replaced</span>", div.html());
        Assert.assertEquals("Replaced", div.text());

        Element emptyDiv = new Element(Tag.valueOf("div"), "");
        Assert.assertEquals("", emptyDiv.html());

        Element imgHtml = new Element(Tag.valueOf("img"), "");
        Assert.assertEquals("<img>", imgHtml.outerHtml());

        Document xmlDoc = Jsoup.parse("<img />", "", org.jsoup.parser.Parser.xmlParser());
        Assert.assertEquals("<img />", xmlDoc.select("img").first().outerHtml());

        Element meta = new Element(Tag.valueOf("meta"), "");
        Assert.assertEquals("<meta>", meta.outerHtml());
    }

    @Test
    public void testDomMutations() {
        Document doc = Jsoup.parse("<div id='wrap'><p id='target'>Middle</p></div>");
        Element target = doc.getElementById("target");

        target.before("<span id='before'>Start</span>");
        Assert.assertEquals("before", target.previousElementSibling().id());

        target.after("<span id='after'>End</span>");
        Assert.assertEquals("after", target.nextElementSibling().id());

        Element nodeBefore = new Element(Tag.valueOf("b"), "").text("BoldBefore");
        Element nodeAfter = new Element(Tag.valueOf("i"), "").text("ItalicAfter");
        target.before(nodeBefore);
        target.after(nodeAfter);

        Assert.assertEquals("b", target.previousElementSibling().tagName());
        Assert.assertEquals("i", target.nextElementSibling().tagName());

        target.wrap("<div class='container'></div>");
        Assert.assertEquals("container", target.parent().className());

        target.empty();
        Assert.assertEquals(0, target.childNodeSize());
        Assert.assertEquals("", target.text());
    }

    @Test
    public void testEqualityAndHashCodeAndClone() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        el1.attr("id", "test");
        el1.text("Content");

        Element el2 = new Element(Tag.valueOf("div"), "http://example.com");
        el2.attr("id", "test");
        el2.text("Content");

        Assert.assertEquals(el1, el1);
        Assert.assertNotEquals(el1, el2);
        Assert.assertNotEquals(el1, null);
        Assert.assertNotEquals(el1, "string");

        Assert.assertEquals(el1.hashCode(), el1.hashCode());

        Element cloned = el1.clone();
        Assert.assertEquals(el1.outerHtml(), cloned.outerHtml());
        Assert.assertNotSame(el1, cloned);
        Assert.assertNotEquals(el1, cloned);
    }
}
