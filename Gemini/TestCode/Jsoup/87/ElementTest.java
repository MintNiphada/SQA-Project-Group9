package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ElementTest {

    @Test
    public void testConstructorsAndTagProperties() {
        Element el1 = new Element("div");
        Assert.assertEquals("div", el1.tagName());
        Assert.assertEquals("div", el1.nodeName());
        Assert.assertEquals("", el1.baseUri());
        Assert.assertTrue(el1.isBlock());
        Assert.assertFalse(el1.hasAttributes());

        Tag pTag = Tag.valueOf("p");
        Attributes attrs = new Attributes();
        attrs.put("id", "main");
        Element el2 = new Element(pTag, "http://example.com", attrs);
        Assert.assertEquals("p", el2.tagName());
        Assert.assertEquals(pTag, el2.tag());
        Assert.assertEquals("http://example.com", el2.baseUri());
        Assert.assertTrue(el2.hasAttributes());
        Assert.assertEquals("main", el2.id());

        Element el3 = new Element(pTag, "http://example.com");
        Assert.assertEquals("p", el3.tagName());
        Assert.assertEquals("http://example.com", el3.baseUri());

        el1.doSetBaseUri("http://foo.bar");
        Assert.assertEquals("http://foo.bar", el1.baseUri());

        el1.tagName("span");
        Assert.assertEquals("span", el1.tagName());
        Assert.assertFalse(el1.isBlock());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTagNameThrows() {
        Element el = new Element("div");
        el.tagName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTagInConstructorThrows() {
        new Element((Tag) null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullBaseUriInConstructorThrows() {
        new Element(Tag.valueOf("div"), null);
    }

    @Test
    public void testAttributesAndDataset() {
        Element el = new Element("div");
        el.attr("data-name", "jsoup");
        el.attr("data-lang", "java");
        el.attr("class", "container");
        el.attr("disabled", true);

        Assert.assertEquals("jsoup", el.attr("data-name"));
        Assert.assertEquals("", el.attr("disabled"));

        Map<String, String> dataset = el.dataset();
        Assert.assertEquals(2, dataset.size());
        Assert.assertEquals("jsoup", dataset.get("name"));
        Assert.assertEquals("java", dataset.get("lang"));

        el.attr("disabled", false);
        Assert.assertFalse(el.hasAttr("disabled"));

        Assert.assertEquals("", el.id());
        el.attr("id", "customId");
        Assert.assertEquals("customId", el.id());
    }

    @Test
    public void testChildNodesAndElements() {
        Element parent = new Element("div");
        Assert.assertEquals(0, parent.childNodeSize());
        Assert.assertEquals(0, parent.children().size());

        Element child1 = new Element("p");
        TextNode text1 = new TextNode("Hello");
        DataNode data1 = new DataNode("var x = 1;");

        parent.appendChild(child1);
        parent.appendChild(text1);
        parent.appendChild(data1);

        Assert.assertEquals(3, parent.childNodeSize());
        Assert.assertEquals(1, parent.children().size());
        Assert.assertEquals(child1, parent.child(0));
        Assert.assertEquals(parent, child1.parent());

        List<TextNode> textNodes = parent.textNodes();
        Assert.assertEquals(1, textNodes.size());
        Assert.assertEquals("Hello", textNodes.get(0).text());

        List<DataNode> dataNodes = parent.dataNodes();
        Assert.assertEquals(1, dataNodes.size());
        Assert.assertEquals("var x = 1;", dataNodes.get(0).getWholeData());

        parent.empty();
        Assert.assertEquals(0, parent.childNodeSize());
        Assert.assertEquals(0, parent.children().size());
    }

    @Test
    public void testTreeManipulation() {
        Element root = new Element("div");
        Element p = root.appendElement("p");
        p.attr("id", "p1");
        p.appendText("Text");

        Element span = root.prependElement("span");
        span.prependText("Start ");

        Assert.assertEquals(2, root.children().size());
        Assert.assertEquals("span", root.child(0).tagName());
        Assert.assertEquals("p", root.child(1).tagName());

        Element strong = new Element("strong");
        strong.appendTo(root);
        Assert.assertEquals(3, root.children().size());
        Assert.assertEquals("strong", root.child(2).tagName());

        Element em = new Element("em");
        root.prependChild(em);
        Assert.assertEquals("em", root.child(0).tagName());

        Element b = new Element("b");
        root.insertChildren(1, Collections.singletonList(b));
        Assert.assertEquals("b", root.child(1).tagName());

        Element u = new Element("u");
        root.insertChildren(-1, u);
        Assert.assertEquals("u", root.child(root.childNodeSize() - 1).tagName());
    }

    @Test
    public void testParentsAndSiblings() {
        Document doc = Jsoup.parse("<div id='root'><div id='p'><span id='s1'>1</span><span id='s2'>2</span><span id='s3'>3</span></div></div>");
        Element p = doc.getElementById("p");
        Element s1 = doc.getElementById("s1");
        Element s2 = doc.getElementById("s2");
        Element s3 = doc.getElementById("s3");

        Elements parents = s1.parents();
        Assert.assertEquals(2, parents.size());
        Assert.assertEquals("p", parents.get(0).id());
        Assert.assertEquals("root", parents.get(1).id());

        Elements s2Siblings = s2.siblingElements();
        Assert.assertEquals(2, s2Siblings.size());
        Assert.assertEquals(s1, s2Siblings.get(0));
        Assert.assertEquals(s3, s2Siblings.get(1));

        Assert.assertEquals(s3, s2.nextElementSibling());
        Assert.assertNull(s3.nextElementSibling());
        Assert.assertEquals(s1, s2.previousElementSibling());
        Assert.assertNull(s1.previousElementSibling());

        Assert.assertEquals(1, s2.nextElementSiblings().size());
        Assert.assertEquals(s3, s2.nextElementSiblings().get(0));
        Assert.assertEquals(1, s2.previousElementSiblings().size());
        Assert.assertEquals(s1, s2.previousElementSiblings().get(0));

        Assert.assertEquals(s1, s2.firstElementSibling());
        Assert.assertEquals(s3, s2.lastElementSibling());
        Assert.assertEquals(1, s2.elementSiblingIndex());

        Element orphan = new Element("div");
        Assert.assertEquals(0, orphan.siblingElements().size());
        Assert.assertNull(orphan.nextElementSibling());
        Assert.assertNull(orphan.previousElementSibling());
        Assert.assertEquals(0, orphan.nextElementSiblings().size());
        Assert.assertEquals(0, orphan.previousElementSiblings().size());
        Assert.assertEquals(0, orphan.elementSiblingIndex());
    }

    @Test
    public void testCssSelector() {
        Document doc = Jsoup.parse("<html><body><div id='header'><span class='icon active'></span></div><div><p class='text'></p><p class='text'></p></div></body></html>");
        Element header = doc.getElementById("header");
        Assert.assertEquals("#header", header.cssSelector());

        Element icon = header.selectFirst("span");
        Assert.assertEquals("#header > span.icon.active", icon.cssSelector());

        Element p2 = doc.select("p.text").get(1);
        Assert.assertEquals("html > body > div:nth-child(2) > p.text:nth-child(2)", p2.cssSelector());

        Element standalone = new Element("ns:custom");
        Assert.assertEquals("ns|custom", standalone.cssSelector());
    }

    @Test
    public void testSelectAndIs() {
        Document doc = Jsoup.parse("<div class='box'><p id='target'>Hello</p></div>");
        Element box = doc.selectFirst(".box");
        Assert.assertNotNull(box);

        Elements found = box.select("#target");
        Assert.assertEquals(1, found.size());

        Element target = box.selectFirst("#target");
        Assert.assertNotNull(target);
        Assert.assertTrue(target.is("p#target"));
        Assert.assertFalse(target.is("span"));
        Assert.assertTrue(target.is(new Evaluator.Tag("p")));
    }

    @Test
    public void testDomSearchMethods() {
        Document doc = Jsoup.parse("<div id='main' class='wrapper dark' data-group='one' val='abc-123'>" +
                "<p class='first test' title='heading'>Hello World</p>" +
                "<p class='second test' title='sub'>Foo Bar</p>" +
                "<span data-group='two' val='xyz-123'>Inner</span>" +
                "</div>");
        Element main = doc.getElementById("main");

        Assert.assertEquals("main", doc.getElementById("main").id());
        Assert.assertNull(doc.getElementById("non-existent"));

        Assert.assertEquals(2, main.getElementsByTag("p").size());
        Assert.assertEquals(2, main.getElementsByClass("test").size());
        Assert.assertEquals(2, main.getElementsByAttribute("title").size());
        Assert.assertEquals(2, main.getElementsByAttributeStarting("data-").size());
        Assert.assertEquals(1, main.getElementsByAttributeValue("title", "heading").size());
        Assert.assertEquals(3, main.getElementsByAttributeValueNot("title", "heading").size());
        Assert.assertEquals(1, main.getElementsByAttributeValueStarting("val", "abc").size());
        Assert.assertEquals(2, main.getElementsByAttributeValueEnding("val", "123").size());
        Assert.assertEquals(2, main.getElementsByAttributeValueContaining("val", "-").size());

        Assert.assertEquals(2, main.getElementsByAttributeValueMatching("val", Pattern.compile("^\\w{3}-\\d{3}$")).size());
        Assert.assertEquals(2, main.getElementsByAttributeValueMatching("val", "^\\w{3}-\\d{3}$").size());

        Assert.assertEquals(1, main.getElementsByIndexLessThan(1).size());
        Assert.assertEquals(2, main.getElementsByIndexGreaterThan(0).size());
        Assert.assertEquals(1, main.getElementsByIndexEquals(1).size());

        Assert.assertEquals(1, main.getElementsContainingText("World").size());
        Assert.assertEquals(1, main.getElementsContainingOwnText("Foo").size());
        Assert.assertEquals(1, main.getElementsMatchingText(Pattern.compile("Hello \\w+")).size());
        Assert.assertEquals(1, main.getElementsMatchingText("Hello \\w+").size());
        Assert.assertEquals(1, main.getElementsMatchingOwnText(Pattern.compile("Foo \\w+")).size());
        Assert.assertEquals(1, main.getElementsMatchingOwnText("Foo \\w+").size());

        Assert.assertEquals(4, main.getAllElements().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidRegex() {
        new Element("div").getElementsByAttributeValueMatching("k", "[a-z");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextInvalidRegex() {
        new Element("div").getElementsMatchingText("[a-z");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingOwnTextInvalidRegex() {
        new Element("div").getElementsMatchingOwnText("[a-z");
    }

    @Test
    public void testTextAndWhitespaceHandling() {
        Element div = new Element("div");
        div.html("Hello <span>there</span><br>world. <p>Paragraph</p><div>Block</div>");
        Assert.assertEquals("Hello there world. Paragraph Block", div.text());
        Assert.assertEquals("Hello world.", div.ownText());
        Assert.assertTrue(div.hasText());

        Element empty = new Element("div");
        Assert.assertFalse(empty.hasText());
        Assert.assertEquals("", empty.text());
        Assert.assertEquals("", empty.ownText());
        Assert.assertEquals("", empty.wholeText());

        Element pre = new Element("pre");
        pre.text("Line 1\n  Line 2");
        Assert.assertTrue(Element.preserveWhitespace(pre));
        Assert.assertEquals("Line 1\n  Line 2", pre.wholeText());
    }

    @Test
    public void testDataHandling() {
        Element script = new Element("script");
        script.appendChild(new DataNode("function test() { return 1; }"));
        script.appendChild(new Comment("<!-- comment -->"));
        script.appendChild(new CDataNode("cdata content"));
        Element inner = new Element("inner");
        inner.appendChild(new DataNode("data2"));
        script.appendChild(inner);

        Assert.assertEquals("function test() { return 1; }<!-- comment -->cdata contentdata2", script.data());
    }

    @Test
    public void testClassManipulation() {
        Element el = new Element("div");
        Assert.assertEquals("", el.className());
        Assert.assertTrue(el.classNames().isEmpty());

        el.addClass("foo");
        Assert.assertTrue(el.hasClass("foo"));
        Assert.assertTrue(el.hasClass("FOO"));
        Assert.assertFalse(el.hasClass("bar"));
        Assert.assertEquals("foo", el.className());

        el.addClass("bar");
        Assert.assertTrue(el.hasClass("foo"));
        Assert.assertTrue(el.hasClass("bar"));

        el.toggleClass("foo");
        Assert.assertFalse(el.hasClass("foo"));
        Assert.assertTrue(el.hasClass("bar"));

        el.toggleClass("foo");
        Assert.assertTrue(el.hasClass("foo"));

        el.removeClass("bar");
        Assert.assertFalse(el.hasClass("bar"));
        Assert.assertTrue(el.hasClass("foo"));

        Set<String> newClasses = new HashSet<>(Arrays.asList("c1", "c2"));
        el.classNames(newClasses);
        Assert.assertTrue(el.hasClass("c1"));
        Assert.assertTrue(el.hasClass("c2"));
        Assert.assertFalse(el.hasClass("foo"));

        el.classNames(Collections.emptySet());
        Assert.assertEquals("", el.className());
        Assert.assertFalse(el.hasClass("c1"));

        el.attr("class", "  first   middle   last  ");
        Assert.assertTrue(el.hasClass("first"));
        Assert.assertTrue(el.hasClass("middle"));
        Assert.assertTrue(el.hasClass("last"));
        Assert.assertFalse(el.hasClass("mid"));
        Assert.assertFalse(el.hasClass("longerthanwholeclassattributevalue"));
    }

    @Test
    public void testFormValueHandling() {
        Element input = new Element("input").attr("value", "username");
        Assert.assertEquals("username", input.val());
        input.val("newuser");
        Assert.assertEquals("newuser", input.attr("value"));

        Element textarea = new Element("textarea");
        textarea.text("comment text");
        Assert.assertEquals("comment text", textarea.val());
        textarea.val("new text");
        Assert.assertEquals("new text", textarea.text());
    }

    @Test
    public void testOuterAndInnerHtml() {
        Element div = new Element("div");
        div.attr("id", "main");
        Element p = div.appendElement("p");
        p.text("Hello");

        Assert.assertEquals("<p>Hello</p>", div.html());
        Assert.assertEquals("<div id=\"main\">\n <p>Hello</p>\n</div>", div.outerHtml());

        div.html("<span>Replaced</span>");
        Assert.assertEquals("<span>Replaced</span>", div.html());
        Assert.assertEquals("span", div.child(0).tagName());

        Element img = new Element("img").attr("src", "foo.jpg");
        Assert.assertEquals("<img src=\"foo.jpg\">", img.outerHtml());

        Document xmlDoc = Jsoup.parse("<root><img src='foo.jpg' /></root>", "", org.jsoup.parser.Parser.xmlParser());
        Element xmlImg = xmlDoc.selectFirst("img");
        Assert.assertEquals("<img src=\"foo.jpg\" />", xmlImg.outerHtml());
    }

    @Test
    public void testDomMutationsWrapBeforeAfter() {
        Document doc = Jsoup.parse("<div><p id='target'>Middle</p></div>");
        Element target = doc.getElementById("target");

        target.before("<span id='before-str'>BeforeStr</span>");
        target.before(new Element("span").attr("id", "before-node").text("BeforeNode"));
        target.after("<span id='after-str'>AfterStr</span>");
        target.after(new Element("span").attr("id", "after-node").text("AfterNode"));

        Assert.assertNotNull(doc.getElementById("before-str"));
        Assert.assertNotNull(doc.getElementById("before-node"));
        Assert.assertNotNull(doc.getElementById("after-str"));
        Assert.assertNotNull(doc.getElementById("after-node"));

        target.wrap("<div class='wrapper'></div>");
        Assert.assertEquals("wrapper", target.parent().className());
    }

    @Test
    public void testCloning() {
        Element div = new Element("div");
        div.attr("id", "origin");
        Element p = div.appendElement("p").text("content");

        Element deepClone = div.clone();
        Assert.assertNotSame(div, deepClone);
        Assert.assertEquals("origin", deepClone.id());
        Assert.assertEquals(1, deepClone.children().size());
        Assert.assertEquals("content", deepClone.child(0).text());
        Assert.assertNotSame(p, deepClone.child(0));

        Element shallow = div.shallowClone();
        Assert.assertNotSame(div, shallow);
        Assert.assertEquals("origin", shallow.id());
        Assert.assertEquals(0, shallow.children().size());
    }
}
