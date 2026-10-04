package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ElementTest {

    @Test
    public void testConstructorsAndBasicGetters() {
        Element el1 = new Element("div");
        Assert.assertEquals("div", el1.tagName());
        Assert.assertEquals("div", el1.nodeName());
        Assert.assertEquals("", el1.baseUri());
        Assert.assertTrue(el1.isBlock());
        Assert.assertEquals(0, el1.childNodeSize());
        Assert.assertFalse(el1.hasAttributes());

        Tag pTag = Tag.valueOf("p");
        Element el2 = new Element(pTag, "http://example.com");
        Assert.assertEquals("p", el2.tagName());
        Assert.assertEquals("http://example.com", el2.baseUri());
        Assert.assertFalse(el2.isBlock());

        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        Element el3 = new Element(pTag, "http://example.com", attrs);
        Assert.assertTrue(el3.hasAttributes());
        Assert.assertEquals("main", el3.id());
        Assert.assertSame(pTag, el3.tag());

        el1.doSetBaseUri("http://foo.com");
        Assert.assertEquals("http://foo.com", el1.baseUri());
    }

    @Test
    public void testTagNameModification() {
        Element el = new Element("span");
        Assert.assertEquals("span", el.tagName());
        el.tagName("DIV");
        Assert.assertEquals("DIV", el.tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTagName() {
        Element el = new Element("span");
        el.tagName("");
    }

    @Test
    public void testAttributesAndDataset() {
        Element el = new Element("div");
        el.attr("class", "one two");
        el.attr("data-test", "val1");
        el.attr("data-num", "123");
        el.attr("disabled", true);

        Assert.assertTrue(el.hasAttributes());
        Assert.assertEquals("one two", el.attr("class"));
        Assert.assertEquals("", el.attr("disabled"));

        el.attr("disabled", false);
        Assert.assertFalse(el.hasAttr("disabled"));

        Map<String, String> dataset = el.dataset();
        Assert.assertEquals(2, dataset.size());
        Assert.assertEquals("val1", dataset.get("test"));
        Assert.assertEquals("123", dataset.get("num"));

        dataset.put("new", "val2");
        Assert.assertEquals("val2", el.attr("data-new"));
    }

    @Test
    public void testParentAndParents() {
        Element root = new Element("div");
        Element p = root.appendElement("p");
        Element span = p.appendElement("span");

        Assert.assertSame(p, span.parent());
        Elements parents = span.parents();
        Assert.assertEquals(2, parents.size());
        Assert.assertSame(p, parents.get(0));
        Assert.assertSame(root, parents.get(1));

        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        Element docSpan = doc.selectFirst("span");
        Elements docParents = docSpan.parents();
        Assert.assertEquals(3, docParents.size());
        Assert.assertEquals("p", docParents.get(0).tagName());
        Assert.assertEquals("div", docParents.get(1).tagName());
        Assert.assertEquals("body", docParents.get(2).tagName());
    }

    @Test
    public void testChildrenAndChildAccess() {
        Element el = new Element("div");
        el.appendText("Text 1");
        Element c1 = el.appendElement("span");
        el.appendText("Text 2");
        Element c2 = el.appendElement("b");

        Assert.assertEquals(4, el.childNodeSize());
        Assert.assertEquals(2, el.children().size());
        Assert.assertSame(c1, el.child(0));
        Assert.assertSame(c2, el.child(1));

        List<TextNode> textNodes = el.textNodes();
        Assert.assertEquals(2, textNodes.size());
        Assert.assertEquals("Text 1", textNodes.get(0).getWholeText());

        el.empty();
        Assert.assertEquals(0, el.childNodeSize());
        Assert.assertEquals(0, el.children().size());
    }

    @Test
    public void testDataNodes() {
        Element el = new Element("script");
        DataNode dNode = new DataNode("var x = 1;");
        el.appendChild(dNode);

        List<DataNode> dataNodes = el.dataNodes();
        Assert.assertEquals(1, dataNodes.size());
        Assert.assertEquals("var x = 1;", el.data());

        Element parent = new Element("div");
        parent.appendChild(new Comment("a comment"));
        parent.appendChild(el);
        Assert.assertEquals("a commentvar x = 1;", parent.data());
    }

    @Test
    public void testAppendPrependAndInsertChildren() {
        Element parent = new Element("div");
        Element b = new Element("b");
        Element i = new Element("i");

        parent.appendChild(b);
        parent.prependChild(i);
        Assert.assertEquals(2, parent.children().size());
        Assert.assertSame(i, parent.child(0));
        Assert.assertSame(b, parent.child(1));

        Element p = new Element("p");
        p.appendTo(parent);
        Assert.assertSame(p, parent.child(2));

        Element span = new Element("span");
        span.prependElement("code");
        Assert.assertEquals("code", span.child(0).tagName());

        parent.insertChildren(1, Arrays.asList(new Element("u"), new Element("s")));
        Assert.assertEquals("i", parent.child(0).tagName());
        Assert.assertEquals("u", parent.child(1).tagName());
        Assert.assertEquals("s", parent.child(2).tagName());
        Assert.assertEquals("b", parent.child(3).tagName());

        parent.insertChildren(-1, new Element("em"));
        Assert.assertEquals("em", parent.child(parent.children().size() - 1).tagName());
    }

    @Test
    public void testAppendPrependTextAndHtml() {
        Element el = new Element("div");
        el.appendText("Hello ");
        el.prependText("Start: ");
        Assert.assertEquals("Start: Hello", el.text());

        el.append("<span id='s1'>Mid</span>");
        el.prepend("<b id='b1'>Begin</b>");
        Assert.assertNotNull(el.selectFirst("#s1"));
        Assert.assertNotNull(el.selectFirst("#b1"));
        Assert.assertEquals("b", el.child(0).tagName());
    }

    @Test
    public void testBeforeAfterWrap() {
        Element parent = new Element("div");
        Element child = parent.appendElement("span");

        child.before("<b>BeforeHtml</b>");
        child.before(new Element("i"));
        child.after("<u>AfterHtml</u>");
        child.after(new Element("small"));

        Assert.assertEquals(5, parent.children().size());
        Assert.assertEquals("b", parent.child(0).tagName());
        Assert.assertEquals("i", parent.child(1).tagName());
        Assert.assertEquals("span", parent.child(2).tagName());
        Assert.assertEquals("small", parent.child(3).tagName());
        Assert.assertEquals("u", parent.child(4).tagName());

        child.wrap("<div class='wrapper'></div>");
        Assert.assertEquals("wrapper", child.parent().className());
    }

    @Test
    public void testSiblingsNavigation() {
        Element root = new Element("div");
        Element c0 = root.appendElement("p");
        Element c1 = root.appendElement("span");
        Element c2 = root.appendElement("b");

        Assert.assertEquals(0, c0.elementSiblingIndex());
        Assert.assertEquals(1, c1.elementSiblingIndex());
        Assert.assertEquals(2, c2.elementSiblingIndex());

        Assert.assertNull(c0.previousElementSibling());
        Assert.assertSame(c1, c0.nextElementSibling());
        Assert.assertSame(c0, c1.previousElementSibling());
        Assert.assertSame(c2, c1.nextElementSibling());
        Assert.assertSame(c1, c2.previousElementSibling());
        Assert.assertNull(c2.nextElementSibling());

        Assert.assertSame(c0, c1.firstElementSibling());
        Assert.assertSame(c2, c1.lastElementSibling());

        Elements sibs = c1.siblingElements();
        Assert.assertEquals(2, sibs.size());
        Assert.assertSame(c0, sibs.get(0));
        Assert.assertSame(c2, sibs.get(1));

        Element standalone = new Element("div");
        Assert.assertEquals(0, standalone.elementSiblingIndex());
        Assert.assertNull(standalone.previousElementSibling());
        Assert.assertNull(standalone.nextElementSibling());
        Assert.assertEquals(0, standalone.siblingElements().size());
    }

    @Test
    public void testCssSelector() {
        Document doc = Jsoup.parse("<div id='id1'><p class='c1 c2'><span>A</span><span>B</span></p></div>");
        Element div = doc.selectFirst("#id1");
        Element spanA = doc.select("span").get(0);
        Element spanB = doc.select("span").get(1);

        Assert.assertEquals("#id1", div.cssSelector());
        Assert.assertEquals("#id1 > p.c1.c2 > span:nth-child(1)", spanA.cssSelector());
        Assert.assertEquals("#id1 > p.c1.c2 > span:nth-child(2)", spanB.cssSelector());

        Element standalone = new Element("div");
        standalone.addClass("foo");
        Assert.assertEquals("div.foo", standalone.cssSelector());
    }

    @Test
    public void testSelectorAndIs() {
        Element div = new Element("div").attr("id", "myid").addClass("myclass");
        Element p = div.appendElement("p").text("Hello World");

        Assert.assertTrue(div.is("#myid"));
        Assert.assertTrue(div.is(".myclass"));
        Assert.assertTrue(div.is("div"));
        Assert.assertFalse(div.is("span"));
        Assert.assertTrue(div.is(new Evaluator.Tag("div")));

        Assert.assertSame(p, div.selectFirst("p"));
        Assert.assertEquals(1, div.select("p").size());
        Assert.assertNull(div.selectFirst("span"));
    }

    @Test
    public void testGetElementsByQueries() {
        Document doc = Jsoup.parse("<div id='root' data-role='admin' data-level='5' class='main primary'>" +
                "<p class='content' title='intro'>Intro text</p>" +
                "<p class='content' title='outro'>Outro text</p>" +
                "<span class='footer' title='info'>Foot</span>" +
                "</div>");

        Assert.assertNotNull(doc.getElementById("root"));
        Assert.assertNull(doc.getElementById("nonexistent"));

        Assert.assertEquals(2, doc.getElementsByTag("p").size());
        Assert.assertEquals(2, doc.getElementsByClass("content").size());
        Assert.assertEquals(1, doc.getElementsByAttribute("data-role").size());
        Assert.assertEquals(2, doc.getElementsByAttributeStarting("data-").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValue("title", "intro").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueStarting("title", "in").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueEnding("title", "tro").size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueContaining("title", "tr").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueMatching("title", Pattern.compile("in.*")).size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueMatching("title", "^out.*").size());
        Assert.assertTrue(doc.getElementsByAttributeValueNot("title", "intro").size() > 0);

        Assert.assertEquals(1, doc.getElementsByIndexLessThan(1).size());
        Assert.assertEquals(1, doc.getElementsByIndexGreaterThan(1).size());
        Assert.assertEquals(1, doc.getElementsByIndexEquals(1).size());

        Assert.assertEquals(1, doc.getElementsContainingText("Intro").size());
        Assert.assertEquals(1, doc.getElementsContainingOwnText("Intro text").size());
        Assert.assertEquals(1, doc.getElementsMatchingText(Pattern.compile("Intro.*")).size());
        Assert.assertEquals(1, doc.getElementsMatchingText("(?i)INTRO.*").size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText(Pattern.compile("Intro.*")).size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText("^Intro.*").size());

        Assert.assertTrue(doc.getAllElements().size() >= 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRegexPattern() {
        Element el = new Element("div");
        el.getElementsByAttributeValueMatching("attr", "[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTextRegex() {
        Element el = new Element("div");
        el.getElementsMatchingText("[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidOwnTextRegex() {
        Element el = new Element("div");
        el.getElementsMatchingOwnText("[invalid");
    }

    @Test
    public void testTextAndOwnTextAndPreserveWhitespace() {
        Element p = new Element("p");
        p.appendText("Hello ");
        p.appendElement("b").text("world");
        p.appendElement("br");
        p.appendText(" Again");

        Assert.assertEquals("Hello world Again", p.text());
        Assert.assertEquals("Hello  Again", p.ownText());
        Assert.assertTrue(p.hasText());

        Element empty = new Element("div");
        Assert.assertFalse(empty.hasText());
        Assert.assertEquals("", empty.text());

        Element pre = new Element("pre");
        pre.appendText(" line 1 \n  line 2 ");
        Assert.assertEquals(" line 1 \n  line 2 ", pre.text());

        Element codeParent = new Element("pre");
        Element code = codeParent.appendElement("code");
        code.appendText("  var a = 1;  ");
        Assert.assertEquals("  var a = 1;  ", code.text());

        p.text("New text only");
        Assert.assertEquals("New text only", p.text());
        Assert.assertEquals(1, p.childNodeSize());
    }

    @Test
    public void testClasses() {
        Element el = new Element("div");
        Assert.assertEquals("", el.className());
        Assert.assertTrue(el.classNames().isEmpty());
        Assert.assertFalse(el.hasClass("foo"));

        el.addClass("foo");
        Assert.assertTrue(el.hasClass("foo"));
        Assert.assertTrue(el.hasClass("FOO"));
        Assert.assertEquals("foo", el.className());

        el.addClass("bar");
        Assert.assertTrue(el.hasClass("bar"));
        Assert.assertEquals("foo bar", el.className());

        el.removeClass("foo");
        Assert.assertFalse(el.hasClass("foo"));
        Assert.assertTrue(el.hasClass("bar"));

        el.toggleClass("bar");
        Assert.assertFalse(el.hasClass("bar"));
        el.toggleClass("bar");
        Assert.assertTrue(el.hasClass("bar"));

        Set<String> set = new HashSet<>(Arrays.asList("c1", "c2"));
        el.classNames(set);
        Assert.assertTrue(el.hasClass("c1"));
        Assert.assertTrue(el.hasClass("c2"));
        Assert.assertFalse(el.hasClass("bar"));

        el.classNames(new HashSet<String>());
        Assert.assertFalse(el.hasAttr("class"));
    }

    @Test
    public void testVal() {
        Element input = new Element("input");
        input.val("user");
        Assert.assertEquals("user", input.val());
        Assert.assertEquals("user", input.attr("value"));

        Element textarea = new Element("textarea");
        textarea.val("content here");
        Assert.assertEquals("content here", textarea.val());
        Assert.assertEquals("content here", textarea.text());
    }

    @Test
    public void testHtmlAndOuterHtml() throws IOException {
        Element div = new Element("div");
        div.html("<span>Hello</span>");
        Assert.assertEquals("<span>Hello</span>", div.html());

        StringWriter sw = new StringWriter();
        div.html(sw);
        Assert.assertEquals("<span>Hello</span>", sw.toString());

        Element img = new Element("img");
        Assert.assertEquals("<img>", img.outerHtml());

        Document doc = Document.createShell("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        Element xmlImg = new Element("img");
        doc.body().appendChild(xmlImg);
        Assert.assertEquals("<img />", xmlImg.outerHtml());

        Element divBlock = new Element("div");
        divBlock.appendElement("p").text("Text");
        Assert.assertEquals("<div>\n <p>Text</p>\n</div>", divBlock.toString());
    }

    @Test
    public void testClone() {
        Element orig = new Element("div");
        orig.attr("id", "root");
        orig.appendElement("p").text("Para");

        Element clone = orig.clone();
        Assert.assertNotSame(orig, clone);
        Assert.assertEquals(orig.outerHtml(), clone.outerHtml());

        Element shallow = orig.shallowClone();
        Assert.assertEquals(0, shallow.childNodeSize());
        Assert.assertEquals("root", shallow.id());
    }
}
