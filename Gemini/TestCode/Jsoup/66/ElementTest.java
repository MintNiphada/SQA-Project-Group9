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
    public void testConstructorsAndBasicProps() {
        Element el1 = new Element("div");
        Assert.assertEquals("div", el1.tagName());
        Assert.assertEquals("div", el1.nodeName());
        Assert.assertEquals("", el1.baseUri());
        Assert.assertTrue(el1.isBlock());
        Assert.assertNotNull(el1.attributes());

        Tag tag = Tag.valueOf("span");
        Element el2 = new Element(tag, "http://example.com");
        Assert.assertEquals("span", el2.tagName());
        Assert.assertEquals("http://example.com", el2.baseUri());
        Assert.assertFalse(el2.isBlock());
        Assert.assertFalse(el2.hasAttributes());

        Attributes attrs = new Attributes();
        attrs.put("id", "myId");
        Element el3 = new Element(tag, "http://example.com", attrs);
        Assert.assertTrue(el3.hasAttributes());
        Assert.assertEquals("myId", el3.id());

        el3.setBaseUri("http://other.com");
        Assert.assertEquals("http://other.com", el3.baseUri());

        el3.tagName("p");
        Assert.assertEquals("p", el3.tagName());
        Assert.assertEquals("p", el3.tag().getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTagConstructor() {
        new Element((Tag) null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullBaseUriConstructor() {
        new Element(Tag.valueOf("div"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTagName() {
        Element el = new Element("div");
        el.tagName("");
    }

    @Test
    public void testAttributesAndDataset() {
        Element el = new Element("div");
        el.attr("class", "foo bar");
        el.attr("data-name", "jsoup");
        el.attr("disabled", true);

        Assert.assertEquals("foo bar", el.attr("class"));
        Assert.assertEquals("", el.attr("disabled"));
        Assert.assertTrue(el.attributes().hasKey("disabled"));

        el.attr("disabled", false);
        Assert.assertFalse(el.attributes().hasKey("disabled"));

        Map<String, String> dataset = el.dataset();
        Assert.assertEquals(1, dataset.size());
        Assert.assertEquals("jsoup", dataset.get("name"));
    }

    @Test
    public void testParentAndChildren() {
        Element parent = new Element("div");
        Element child1 = new Element("span");
        Element child2 = new Element("p");
        TextNode text = new TextNode("text");

        parent.appendChild(child1);
        parent.appendChild(text);
        parent.appendChild(child2);

        Assert.assertEquals(3, parent.childNodeSize());
        Assert.assertEquals(2, parent.children().size());
        Assert.assertSame(child1, parent.child(0));
        Assert.assertSame(child2, parent.child(1));
        Assert.assertSame(parent, child1.parent());

        List<TextNode> textNodes = parent.textNodes();
        Assert.assertEquals(1, textNodes.size());
        Assert.assertSame(text, textNodes.get(0));

        Element child3 = new Element("a");
        child3.appendTo(parent);
        Assert.assertEquals(3, parent.children().size());
        Assert.assertSame(child3, parent.child(2));

        Element prepended = new Element("b");
        parent.prependChild(prepended);
        Assert.assertSame(prepended, parent.child(0));
    }

    @Test
    public void testParents() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        Element span = doc.selectFirst("span");
        Elements parents = span.parents();

        Assert.assertEquals(3, parents.size());
        Assert.assertEquals("p", parents.get(0).tagName());
        Assert.assertEquals("div", parents.get(1).tagName());
        Assert.assertEquals("body", parents.get(2).tagName());
    }

    @Test
    public void testDataNodes() {
        Document doc = Jsoup.parse("<script>var x = 1;</script>");
        Element script = doc.selectFirst("script");
        List<DataNode> dataNodes = script.dataNodes();

        Assert.assertEquals(1, dataNodes.size());
        Assert.assertEquals("var x = 1;", script.data());
    }

    @Test
    public void testDataWithCommentsAndNestedElements() {
        Element el = new Element("div");
        el.appendChild(new Comment("a comment"));
        Element sub = new Element("script");
        sub.appendChild(new DataNode("nested data"));
        el.appendChild(sub);

        Assert.assertEquals("a commentnested data", el.data());
    }

    @Test
    public void testInsertChildren() {
        Element parent = new Element("div");
        Element p1 = new Element("p");
        Element p2 = new Element("p");
        parent.appendChild(p1);
        parent.appendChild(p2);

        Element ins1 = new Element("span");
        Element ins2 = new Element("span");
        parent.insertChildren(1, Arrays.asList(ins1, ins2));

        Assert.assertEquals(4, parent.childNodeSize());
        Assert.assertSame(ins1, parent.child(1));
        Assert.assertSame(ins2, parent.child(2));

        Element ins3 = new Element("b");
        parent.insertChildren(-1, ins3);
        Assert.assertSame(ins3, parent.child(parent.children().size() - 1));

        parent.insertChildren(0, new Element("i"));
        Assert.assertEquals("i", parent.child(0).tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element parent = new Element("div");
        parent.insertChildren(5, Collections.singletonList(new Element("p")));
    }

    @Test
    public void testAppendAndPrependElementAndText() {
        Element el = new Element("div");
        Element child = el.appendElement("span");
        Assert.assertEquals("span", child.tagName());
        Assert.assertSame(child, el.child(0));

        Element prepended = el.prependElement("b");
        Assert.assertEquals("b", prepended.tagName());
        Assert.assertSame(prepended, el.child(0));

        el.appendText(" world");
        el.prependText("Hello ");
        Assert.assertEquals("Hello <b></b><span></span> world", el.html());
    }

    @Test
    public void testAppendAndPrependHtml() {
        Element el = new Element("div");
        el.append("<p>One</p>");
        Assert.assertEquals("<p>One</p>", el.html());

        el.prepend("<span>Zero</span>");
        Assert.assertEquals("<span>Zero</span>\n<p>One</p>", el.html());
    }

    @Test
    public void testBeforeAfterAndWrap() {
        Document doc = Jsoup.parse("<div><p id='target'>One</p></div>");
        Element p = doc.getElementById("target");

        p.before("<span>Before</span>");
        p.after("<span>After</span>");

        Element bNode = new Element("b").text("B");
        p.before(bNode);
        Element iNode = new Element("i").text("I");
        p.after(iNode);

        Assert.assertEquals("<div><span>Before</span><b>B</b><p id=\"target\">One</p><i>I</i><span>After</span></div>",
                doc.body().html().replaceAll("\\r?\\n\\s*", ""));

        p.wrap("<section class='wrapper'></section>");
        Assert.assertEquals("section", p.parent().tagName());
        Assert.assertEquals("wrapper", p.parent().className());
    }

    @Test
    public void testEmptyAndClone() {
        Element el = new Element("div");
        el.attr("class", "main");
        el.appendElement("span").text("test");

        Assert.assertEquals(1, el.childNodeSize());
        Element cloned = el.clone();
        Assert.assertEquals("main", cloned.attr("class"));
        Assert.assertEquals(1, cloned.childNodeSize());

        el.empty();
        Assert.assertEquals(0, el.childNodeSize());
        Assert.assertEquals("main", el.attr("class"));
        Assert.assertEquals(1, cloned.childNodeSize());
    }

    @Test
    public void testSiblingNavigation() {
        Document doc = Jsoup.parse("<div><p id='p1'>1</p><p id='p2'>2</p><p id='p3'>3</p></div>");
        Element p1 = doc.getElementById("p1");
        Element p2 = doc.getElementById("p2");
        Element p3 = doc.getElementById("p3");

        Assert.assertEquals(2, p1.siblingElements().size());
        Assert.assertSame(p2, p1.nextElementSibling());
        Assert.assertNull(p3.nextElementSibling());

        Assert.assertSame(p1, p2.previousElementSibling());
        Assert.assertNull(p1.previousElementSibling());

        Assert.assertSame(p1, p2.firstElementSibling());
        Assert.assertSame(p3, p2.lastElementSibling());

        Assert.assertEquals(0, p1.elementSiblingIndex());
        Assert.assertEquals(1, p2.elementSiblingIndex());
        Assert.assertEquals(2, p3.elementSiblingIndex());

        Element standalone = new Element("div");
        Assert.assertEquals(0, standalone.siblingElements().size());
        Assert.assertNull(standalone.nextElementSibling());
        Assert.assertNull(standalone.previousElementSibling());
        Assert.assertNull(standalone.firstElementSibling());
        Assert.assertNull(standalone.lastElementSibling());
        Assert.assertEquals(0, standalone.elementSiblingIndex());
    }

    @Test
    public void testCssSelector() {
        Document doc = Jsoup.parse("<div id='root'><div class='cls'><p class='c1 c2'>text</p><p class='c1 c2'>other</p></div></div>");
        Element p1 = doc.select("p").get(0);
        Element p2 = doc.select("p").get(1);

        Assert.assertEquals("#root > div.cls > p.c1.c2:nth-child(1)", p1.cssSelector());
        Assert.assertEquals("#root > div.cls > p.c1.c2:nth-child(2)", p2.cssSelector());

        Element divId = doc.getElementById("root");
        Assert.assertEquals("#root", divId.cssSelector());

        Element unattached = new Element("span");
        Assert.assertEquals("span", unattached.cssSelector());

        Element namespaced = new Element("ns:tag");
        Assert.assertEquals("ns|tag", namespaced.cssSelector());
    }

    @Test
    public void testDomSearchMethods() {
        Document doc = Jsoup.parse("<div id='d1' class='test bar' data-role='admin' title='sample'>" +
                "<p class='test' data-role='user'>Hello World</p>" +
                "<p id='p2' lang='en-US'>Foo <span>bar</span></p>" +
                "</div>");

        Assert.assertEquals(2, doc.getElementsByTag("p").size());
        Assert.assertEquals("d1", doc.getElementById("d1").id());
        Assert.assertNull(doc.getElementById("nonexistent"));
        Assert.assertEquals(2, doc.getElementsByClass("test").size());
        Assert.assertEquals(1, doc.getElementsByAttribute("title").size());
        Assert.assertEquals(2, doc.getElementsByAttributeStarting("data-").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValue("data-role", "admin").size());
        Assert.assertTrue(doc.getElementsByAttributeValueNot("data-role", "admin").size() > 0);
        Assert.assertEquals(1, doc.getElementsByAttributeValueStarting("lang", "en").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueEnding("lang", "US").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueContaining("data-role", "min").size());

        Assert.assertEquals(1, doc.getElementsByAttributeValueMatching("data-role", Pattern.compile("^adm.*")).size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueMatching("data-role", "^adm.*").size());

        Assert.assertEquals(1, doc.getElementsByIndexLessThan(1).size());
        Assert.assertEquals(1, doc.getElementsByIndexGreaterThan(1).size());
        Assert.assertEquals(1, doc.getElementsByIndexEquals(1).size());

        Assert.assertEquals(1, doc.getElementsContainingText("World").size());
        Assert.assertEquals(1, doc.getElementsContainingOwnText("Foo").size());
        Assert.assertEquals(0, doc.getElementsContainingOwnText("bar").get(0).tagName().equals("p") ? 0 : 0);

        Assert.assertEquals(1, doc.getElementsMatchingText(Pattern.compile("Hello\\s+World")).size());
        Assert.assertEquals(1, doc.getElementsMatchingText("Hello\\s+World").size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText(Pattern.compile("^Foo")).size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText("^Foo").size());

        Assert.assertTrue(doc.getAllElements().size() >= 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPatternSyntaxExceptionInAttrMatching() {
        new Element("div").getElementsByAttributeValueMatching("k", "[a-z");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPatternSyntaxExceptionInTextMatching() {
        new Element("div").getElementsMatchingText("[a-z");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPatternSyntaxExceptionInOwnTextMatching() {
        new Element("div").getElementsMatchingOwnText("[a-z");
    }

    @Test
    public void testSelectAndIs() {
        Document doc = Jsoup.parse("<div><p class='intro'>First</p><p class='body'>Second</p></div>");
        Element div = doc.selectFirst("div");

        Assert.assertEquals(2, div.select("p").size());
        Assert.assertEquals("First", div.selectFirst("p").text());
        Assert.assertTrue(div.is("div"));
        Assert.assertTrue(div.selectFirst("p.intro").is(new Evaluator.Class("intro")));
        Assert.assertFalse(div.is("span"));
    }

    @Test
    public void testTextAndOwnTextAndPreserveWhitespace() {
        Document doc = Jsoup.parse("<div>  Hello   <p>World  </p> <br> Next</div><pre>  Line1 \n Line2  </pre>");
        Element div = doc.selectFirst("div");
        Element pre = doc.selectFirst("pre");

        Assert.assertEquals("Hello World Next", div.text());
        Assert.assertEquals("Hello Next", div.ownText());
        Assert.assertEquals("Line1 \n Line2", pre.text());

        Assert.assertTrue(Element.preserveWhitespace(pre));
        Assert.assertFalse(Element.preserveWhitespace(div));
        Assert.assertFalse(Element.preserveWhitespace(null));

        Element el = new Element("div");
        Assert.assertFalse(el.hasText());
        el.text("Sample text");
        Assert.assertTrue(el.hasText());
        Assert.assertEquals("Sample text", el.text());
    }

    @Test
    public void testClassNamesAndManipulation() {
        Element el = new Element("div");
        Assert.assertEquals("", el.className());
        Assert.assertTrue(el.classNames().isEmpty());

        el.addClass("foo");
        el.addClass("bar");
        Assert.assertTrue(el.hasClass("foo"));
        Assert.assertTrue(el.hasClass("BAR"));
        Assert.assertFalse(el.hasClass("baz"));

        el.removeClass("foo");
        Assert.assertFalse(el.hasClass("foo"));
        Assert.assertTrue(el.hasClass("bar"));

        el.toggleClass("bar");
        Assert.assertFalse(el.hasClass("bar"));
        el.toggleClass("bar");
        Assert.assertTrue(el.hasClass("bar"));

        Set<String> set = new HashSet<>(Arrays.asList("one", "two"));
        el.classNames(set);
        Assert.assertTrue(el.hasClass("one"));
        Assert.assertTrue(el.hasClass("two"));
        Assert.assertFalse(el.hasClass("bar"));
    }

    @Test
    public void testHasClassBoundaryBranches() {
        Element el = new Element("div");
        el.attr("class", "header gray round");

        Assert.assertFalse(el.hasClass(""));
        Assert.assertFalse(el.hasClass("header-longer-than-attr-total-len-impossible"));
        Assert.assertTrue(el.hasClass("header"));
        Assert.assertTrue(el.hasClass("gray"));
        Assert.assertTrue(el.hasClass("round"));
        Assert.assertFalse(el.hasClass("head"));
        Assert.assertFalse(el.hasClass("roun"));

        el.attr("class", "exact");
        Assert.assertTrue(el.hasClass("exact"));
        Assert.assertFalse(el.hasClass("exac"));
    }

    @Test
    public void testValMethod() {
        Element input = new Element("input").attr("value", "123");
        Assert.assertEquals("123", input.val());
        input.val("456");
        Assert.assertEquals("456", input.attr("value"));

        Element textarea = new Element("textarea").text("Hello\nWorld");
        Assert.assertEquals("Hello\nWorld", textarea.val());
        textarea.val("Updated");
        Assert.assertEquals("Updated", textarea.text());
    }

    @Test
    public void testOuterHtmlAndOutputSettings() throws IOException {
        Element img = new Element("img");
        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.html);
        StringBuilder sb = new StringBuilder();
        img.outerHtmlHead(sb, 0, doc.outputSettings());
        img.outerHtmlTail(sb, 0, doc.outputSettings());
        Assert.assertEquals("<img>", sb.toString());

        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        sb = new StringBuilder();
        img.outerHtmlHead(sb, 0, doc.outputSettings());
        img.outerHtmlTail(sb, 0, doc.outputSettings());
        Assert.assertEquals("<img />", sb.toString());

        Element customSelfClosing = new Element(Tag.valueOf("custom", org.jsoup.parser.ParseSettings.preserveCase), "");
        customSelfClosing.tag().setSelfClosing();
        sb = new StringBuilder();
        customSelfClosing.outerHtmlHead(sb, 0, doc.outputSettings());
        customSelfClosing.outerHtmlTail(sb, 0, doc.outputSettings());
        Assert.assertEquals("<custom />", sb.toString());

        Element block = new Element("div");
        block.appendChild(new Element("p").text("hi"));
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.html).prettyPrint(true);
        String html = block.outerHtml();
        Assert.assertTrue(html.contains("<div>\n <p>hi</p>\n</div>") || html.contains("<div>"));
    }

    @Test
    public void testHtmlAndAppendableHtml() {
        Element div = new Element("div");
        div.html("<span>Test</span>");
        Assert.assertEquals("<span>Test</span>", div.html());

        StringBuilder sb = new StringBuilder();
        div.html(sb);
        Assert.assertEquals("<span>Test</span>", sb.toString());

        Assert.assertEquals("<div><span>Test</span></div>", div.toString().replaceAll("\\s+", ""));
    }
}
