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
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "http://example.com");
        Assert.assertEquals("div", el.nodeName());
        Assert.assertEquals("div", el.tagName());
        Assert.assertEquals(tag, el.tag());
        Assert.assertTrue(el.isBlock());

        el.tagName("span");
        Assert.assertEquals("span", el.tagName());
        Assert.assertFalse(el.isBlock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTagNameEmpty() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test
    public void testAttributesAndDataset() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "myId");
        el.attr("data-test", "val1");
        el.attr("data-num", "123");

        Assert.assertEquals("myId", el.id());
        Map<String, String> dataset = el.dataset();
        Assert.assertEquals(2, dataset.size());
        Assert.assertEquals("val1", dataset.get("test"));
        Assert.assertEquals("123", dataset.get("num"));
    }

    @Test
    public void testParentAndParents() {
        Document doc = Jsoup.parse("<html><body><div><p><span>Hello</span></p></div></body></html>");
        Element span = doc.select("span").first();
        Assert.assertNotNull(span);
        Assert.assertEquals("p", span.parent().tagName());

        Elements parents = span.parents();
        Assert.assertEquals(4, parents.size());
        Assert.assertEquals("p", parents.get(0).tagName());
        Assert.assertEquals("div", parents.get(1).tagName());
        Assert.assertEquals("body", parents.get(2).tagName());
        Assert.assertEquals("html", parents.get(3).tagName());
    }

    @Test
    public void testChildrenAndChildNodesFiltering() {
        Document doc = Jsoup.parse("<div>Text 1<span>Span Text</span><!-- comment -->Text 2<script>var x=1;</script></div>");
        Element div = doc.select("div").first();

        Assert.assertEquals(2, div.children().size());
        Assert.assertEquals("span", div.child(0).tagName());
        Assert.assertEquals("script", div.child(1).tagName());

        List<TextNode> textNodes = div.textNodes();
        Assert.assertEquals(2, textNodes.size());
        Assert.assertEquals("Text 1", textNodes.get(0).getWholeText());
        Assert.assertEquals("Text 2", textNodes.get(1).getWholeText());

        List<DataNode> dataNodes = div.child(1).dataNodes();
        Assert.assertEquals(1, dataNodes.size());
        Assert.assertEquals("var x=1;", dataNodes.get(0).getWholeData());
        Assert.assertEquals("var x=1;", div.child(1).data());
    }

    @Test
    public void testElementMutationAndInsertion() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = div.appendElement("p");
        p.text("Para");

        Element h1 = div.prependElement("h1");
        h1.text("Header");

        Assert.assertEquals("<h1>Header</h1>\n<p>Para</p>", div.html());

        div.appendText(" End");
        div.prependText("Start ");
        Assert.assertEquals("Start \n<h1>Header</h1>\n<p>Para</p> End", div.html());

        Element span = new Element(Tag.valueOf("span"), "");
        span.text("Span1");
        Element em = new Element(Tag.valueOf("em"), "");
        em.text("Em1");

        div.insertChildren(1, Arrays.asList(span, em));
        Assert.assertEquals(span, div.child(0));
        Assert.assertEquals("Start ", ((TextNode) div.childNode(0)).getWholeText());

        div.insertChildren(-1, Collections.singletonList(new Element(Tag.valueOf("b"), "").text("Bold")));
        Assert.assertEquals("b", div.child(div.children().size() - 1).tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.insertChildren(5, Collections.singletonList(new Element(Tag.valueOf("p"), "")));
    }

    @Test
    public void testAppendPrependHtml() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.append("<p>One</p>");
        div.append("<p>Two</p>");
        div.prepend("<span>Zero</span>");

        Assert.assertEquals("<span>Zero</span>\n<p>One</p>\n<p>Two</p>", div.html());
    }

    @Test
    public void testSiblingsAndIndices() {
        Document doc = Jsoup.parse("<div><p class='first'>1</p><p class='second'>2</p><p class='third'>3</p></div>");
        Element p1 = doc.select(".first").first();
        Element p2 = doc.select(".second").first();
        Element p3 = doc.select(".third").first();

        Assert.assertEquals(Integer.valueOf(0), p1.elementSiblingIndex());
        Assert.assertEquals(Integer.valueOf(1), p2.elementSiblingIndex());
        Assert.assertEquals(Integer.valueOf(2), p3.elementSiblingIndex());

        Assert.assertEquals(p2, p1.nextElementSibling());
        Assert.assertNull(p3.nextElementSibling());

        Assert.assertEquals(p2, p3.previousElementSibling());
        Assert.assertNull(p1.previousElementSibling());

        Assert.assertEquals(p1, p2.firstElementSibling());
        Assert.assertEquals(p3, p2.lastElementSibling());

        Elements siblings = p2.siblingElements();
        Assert.assertEquals(2, siblings.size());
        Assert.assertTrue(siblings.contains(p1));
        Assert.assertTrue(siblings.contains(p3));
        Assert.assertFalse(siblings.contains(p2));

        Element standalone = new Element(Tag.valueOf("p"), "");
        Assert.assertEquals(Integer.valueOf(0), standalone.elementSiblingIndex());
        Assert.assertNull(standalone.nextElementSibling());
        Assert.assertNull(standalone.previousElementSibling());
        Assert.assertEquals(0, standalone.siblingElements().size());
    }

    @Test
    public void testCssSelector() {
        Document doc = Jsoup.parse("<div id='root'><div class='parent'><p class='c1 c2'>One</p><p class='c1 c2'>Two</p></div></div>");
        Element p1 = doc.select("p").get(0);
        Element p2 = doc.select("p").get(1);

        Assert.assertEquals("#root > div.parent > p.c1.c2:nth-child(1)", p1.cssSelector());
        Assert.assertEquals("#root > div.parent > p.c1.c2:nth-child(2)", p2.cssSelector());

        Element root = doc.getElementById("root");
        Assert.assertEquals("#root", root.cssSelector());

        Element standalone = new Element(Tag.valueOf("div"), "").attr("class", "foo bar");
        Assert.assertEquals("div.foo.bar", standalone.cssSelector());
    }

    @Test
    public void testDomSearchMethods() {
        Document doc = Jsoup.parse("<div id='d1' class='main highlight' data-type='test' val='123'>"
                + "<span class='highlight' val='456'>Span text</span>"
                + "<p val='abc'>Paragraph <b>bold text</b></p>"
                + "</div>");

        Assert.assertEquals("d1", doc.getElementById("d1").id());
        Assert.assertNull(doc.getElementById("nonexistent"));

        Assert.assertEquals(1, doc.getElementsByTag("span").size());
        Assert.assertEquals(2, doc.getElementsByClass("highlight").size());
        Assert.assertEquals(3, doc.getElementsByAttribute("val").size());
        Assert.assertEquals(1, doc.getElementsByAttributeStarting("data-").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValue("val", "123").size());
        Assert.assertEquals(3, doc.getElementsByAttributeValueNot("val", "123").size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueStarting("val", "12").size() + doc.getElementsByAttributeValueStarting("val", "45").size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueEnding("val", "6").size() + doc.getElementsByAttributeValueEnding("val", "c").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueContaining("val", "5").size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueMatching("val", Pattern.compile("\\d+")).size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueMatching("val", "\\d+").size());

        Assert.assertEquals(1, doc.getElementsByIndexLessThan(1).size());
        Assert.assertEquals(1, doc.getElementsByIndexGreaterThan(1).size());
        Assert.assertEquals(1, doc.getElementsByIndexEquals(1).size());

        Assert.assertEquals(1, doc.getElementsContainingText("Span").size());
        Assert.assertEquals(1, doc.getElementsContainingOwnText("Paragraph").size());
        Assert.assertEquals(0, doc.getElementsContainingOwnText("bold text").select("p").size());

        Assert.assertEquals(1, doc.getElementsMatchingText(Pattern.compile("Span.*")).size());
        Assert.assertEquals(1, doc.getElementsMatchingText("Span.*").size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText(Pattern.compile("Paragraph.*")).size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText("Paragraph.*").size());

        Assert.assertTrue(doc.getAllElements().size() >= 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidPattern() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("val", "[unclosed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextInvalidPattern() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[unclosed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingOwnTextInvalidPattern() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("[unclosed");
    }

    @Test
    public void testTextAndWhitespacePreservation() {
        Document doc = Jsoup.parse("<div><p>Hello   \n  <b>World</b></p><pre>  Line 1 \n  Line 2 </pre></div>");
        Element p = doc.select("p").first();
        Assert.assertEquals("Hello World", p.text());
        Assert.assertEquals("Hello", p.ownText());
        Assert.assertTrue(p.hasText());

        Element pre = doc.select("pre").first();
        Assert.assertEquals("  Line 1 \n  Line 2 ", pre.text());

        Element emptyDiv = new Element(Tag.valueOf("div"), "");
        Assert.assertFalse(emptyDiv.hasText());
        emptyDiv.text("New text");
        Assert.assertEquals("New text", emptyDiv.text());
        Assert.assertTrue(emptyDiv.hasText());
    }

    @Test
    public void testBrInTextAndOwnText() {
        Document doc = Jsoup.parse("<p>Line1<br>Line2</p>");
        Element p = doc.select("p").first();
        Assert.assertEquals("Line1 Line2", p.text());
        Assert.assertEquals("Line1 Line2", p.ownText());
    }

    @Test
    public void testClassManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        Assert.assertFalse(el.hasClass("active"));

        el.addClass("active");
        Assert.assertTrue(el.hasClass("active"));
        Assert.assertTrue(el.hasClass("ACTIVE"));
        Assert.assertEquals("active", el.className());

        el.addClass("second");
        Assert.assertEquals(2, el.classNames().size());

        el.removeClass("active");
        Assert.assertFalse(el.hasClass("active"));
        Assert.assertTrue(el.hasClass("second"));

        el.toggleClass("visible");
        Assert.assertTrue(el.hasClass("visible"));
        el.toggleClass("visible");
        Assert.assertFalse(el.hasClass("visible"));

        Set<String> custom = new HashSet<String>(Arrays.asList("c1", "c2"));
        el.classNames(custom);
        Assert.assertTrue(el.hasClass("c1"));
        Assert.assertTrue(el.hasClass("c2"));
    }

    @Test
    public void testValMethod() {
        Element input = new Element(Tag.valueOf("input"), "").attr("value", "testVal");
        Assert.assertEquals("testVal", input.val());
        input.val("newVal");
        Assert.assertEquals("newVal", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("textVal");
        Assert.assertEquals("textVal", textarea.val());
        textarea.val("newTextVal");
        Assert.assertEquals("newTextVal", textarea.text());
    }

    @Test
    public void testOuterHtmlSyntaxAndSelfClosing() {
        Document htmlDoc = Jsoup.parse("<img src='foo.jpg'>");
        htmlDoc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
        Assert.assertEquals("<img src=\"foo.jpg\">", htmlDoc.select("img").first().outerHtml());

        Document xmlDoc = Jsoup.parse("<img src='foo.jpg'/>", "", org.jsoup.parser.Parser.xmlParser());
        xmlDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        Assert.assertEquals("<img src=\"foo.jpg\" />", xmlDoc.select("img").first().outerHtml());

        Element customTag = new Element(Tag.valueOf("custom"), "");
        Assert.assertEquals("<custom></custom>", customTag.outerHtml());
    }

    @Test
    public void testWrappingAndDOMRelatives() {
        Document doc = Jsoup.parse("<div id='parent'><span id='target'>Text</span></div>");
        Element target = doc.getElementById("target");

        target.before("<p>Before HTML</p>");
        target.after("<p>After HTML</p>");
        Assert.assertEquals(3, doc.getElementById("parent").children().size());

        Element newBefore = new Element(Tag.valueOf("i"), "");
        Element newAfter = new Element(Tag.valueOf("u"), "");
        target.before(newBefore);
        target.after(newAfter);
        Assert.assertEquals(5, doc.getElementById("parent").children().size());

        target.wrap("<div class='wrapper'></div>");
        Assert.assertEquals("wrapper", target.parent().className());
    }

    @Test
    public void testCloneEqualsAndHashCode() {
        Element el1 = new Element(Tag.valueOf("div"), "").attr("id", "d1");
        el1.appendElement("p").text("Paragraph");

        Element el2 = el1.clone();
        Assert.assertEquals(el1, el2);
        Assert.assertEquals(el1.hashCode(), el2.hashCode());
        Assert.assertNotSame(el1, el2);

        el2.attr("id", "d2");
        Assert.assertFalse(el1.equals(el2));
        Assert.assertFalse(el1.equals(null));
        Assert.assertFalse(el1.equals("div"));
        Assert.assertTrue(el1.equals(el1));
    }

    @Test
    public void testEmptyAndHtmlSet() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.html("<p>First</p><span>Second</span>");
        Assert.assertEquals(2, div.children().size());

        div.empty();
        Assert.assertEquals(0, div.children().size());
        Assert.assertEquals("", div.html());
    }
}
