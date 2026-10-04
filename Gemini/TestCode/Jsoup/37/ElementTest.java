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
    public void testConstructorsAndTag() {
        Tag tag = Tag.valueOf("div");
        Attributes attrs = new Attributes();
        attrs.put("key", "val");
        Element el = new Element(tag, "http://example.com", attrs);

        Assert.assertEquals("div", el.nodeName());
        Assert.assertEquals("div", el.tagName());
        Assert.assertSame(tag, el.tag());
        Assert.assertTrue(el.isBlock());
        Assert.assertEquals("http://example.com", el.baseUri());
        Assert.assertEquals("val", el.attr("key"));

        Element el2 = new Element(Tag.valueOf("span"), "http://example.com");
        Assert.assertEquals("span", el2.tagName());
        Assert.assertFalse(el2.isBlock());

        el2.tagName("p");
        Assert.assertEquals("p", el2.tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullTag() {
        new Element(null, "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyTagName() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("");
    }

    @Test
    public void testIdAndAttributes() {
        Element el = new Element(Tag.valueOf("div"), "");
        Assert.assertEquals("", el.id());

        el.attr("id", "main");
        Assert.assertEquals("main", el.id());

        el.attr("data-test", "true");
        Map<String, String> dataset = el.dataset();
        Assert.assertEquals("true", dataset.get("test"));
    }

    @Test
    public void testParentAndParents() {
        Element root = new Element(Tag.valueOf("#root"), "");
        Element parent = root.appendElement("div");
        Element child = parent.appendElement("span");
        Element grandChild = child.appendElement("b");

        Assert.assertSame(child, grandChild.parent());
        Elements parents = grandChild.parents();
        Assert.assertEquals(2, parents.size());
        Assert.assertSame(child, parents.get(0));
        Assert.assertSame(parent, parents.get(1));

        Element standalone = new Element(Tag.valueOf("div"), "");
        Assert.assertNull(standalone.parent());
        Assert.assertEquals(0, standalone.parents().size());
    }

    @Test
    public void testChildrenAndFiltering() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.append("Text1 <span>Span</span> Text2 <!-- comment --> <script>data</script>");

        Assert.assertEquals(2, el.children().size());
        Assert.assertEquals("span", el.child(0).tagName());
        Assert.assertEquals("script", el.child(1).tagName());

        List<TextNode> textNodes = el.textNodes();
        Assert.assertEquals(2, textNodes.size());
        Assert.assertEquals("Text1 ", textNodes.get(0).getWholeText());

        List<DataNode> dataNodes = el.child(1).dataNodes();
        Assert.assertEquals(1, dataNodes.size());
        Assert.assertEquals("data", dataNodes.get(0).getWholeData());
    }

    @Test
    public void testSelect() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.append("<p class='intro'>First</p><p id='second'>Second</p>");
        Elements pTags = el.select("p");
        Assert.assertEquals(2, pTags.size());
        Assert.assertEquals("First", el.select(".intro").text());
        Assert.assertEquals("Second", el.select("#second").text());
    }

    @Test
    public void testAppendPrependAndText() {
        Element el = new Element(Tag.valueOf("div"), "");
        Element p = el.appendElement("p");
        p.appendText("World");
        p.prependText("Hello ");
        Assert.assertEquals("Hello World", p.text());

        Element span = el.prependElement("span");
        span.text("Prefix: ");
        Assert.assertEquals("span", el.child(0).tagName());
        Assert.assertEquals("p", el.child(1).tagName());

        el.prepend("<b>Prepended</b>");
        el.append("<i>Appended</i>");
        Assert.assertEquals("b", el.child(0).tagName());
        Assert.assertEquals("i", el.child(el.children().size() - 1).tagName());
    }

    @Test
    public void testInsertChildren() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendElement("p").text("One");
        div.appendElement("p").text("Three");

        List<Node> toInsert = new ArrayList<Node>();
        Element mid = new Element(Tag.valueOf("p"), "").text("Two");
        toInsert.add(mid);

        div.insertChildren(1, toInsert);
        Assert.assertEquals(3, div.children().size());
        Assert.assertEquals("Two", div.child(1).text());

        List<Node> toInsertEnd = new ArrayList<Node>();
        toInsertEnd.add(new Element(Tag.valueOf("p"), "").text("Four"));
        div.insertChildren(-1, toInsertEnd);
        Assert.assertEquals(4, div.children().size());
        Assert.assertEquals("Four", div.child(3).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenNull() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenOutOfBounds() {
        Element div = new Element(Tag.valueOf("div"), "");
        div.insertChildren(2, Collections.emptyList());
    }

    @Test
    public void testSiblingNavigation() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element c1 = root.appendElement("p").attr("id", "1");
        Element c2 = root.appendElement("span").attr("id", "2");
        Element c3 = root.appendElement("a").attr("id", "3");

        Assert.assertEquals(0, (int) c1.elementSiblingIndex());
        Assert.assertEquals(1, (int) c2.elementSiblingIndex());
        Assert.assertEquals(2, (int) c3.elementSiblingIndex());

        Assert.assertNull(c1.previousElementSibling());
        Assert.assertSame(c2, c1.nextElementSibling());
        Assert.assertSame(c1, c2.previousElementSibling());
        Assert.assertSame(c3, c2.nextElementSibling());
        Assert.assertNull(c3.nextElementSibling());

        Assert.assertSame(c1, c2.firstElementSibling());
        Assert.assertSame(c3, c2.lastElementSibling());

        Elements siblings = c2.siblingElements();
        Assert.assertEquals(2, siblings.size());
        Assert.assertSame(c1, siblings.get(0));
        Assert.assertSame(c3, siblings.get(1));

        Element isolated = new Element(Tag.valueOf("div"), "");
        Assert.assertEquals(0, isolated.siblingElements().size());
        Assert.assertNull(isolated.nextElementSibling());
        Assert.assertNull(isolated.previousElementSibling());
        Assert.assertEquals(0, (int) isolated.elementSiblingIndex());
        Assert.assertNull(isolated.firstElementSibling());
        Assert.assertNull(isolated.lastElementSibling());
    }

    @Test
    public void testDomSearchMethods() {
        Element doc = Jsoup.parse("<div id='d1' class='main hero' data-key='v1'>"
                + "<p id='p1' class='sub' custom='abc 123'>Paragraph 1</p>"
                + "<p id='p2' custom='xyz 456'>Paragraph 2 <b>bold</b></p>"
                + "<span class='sub'>Span</span>"
                + "</div>").body();

        Assert.assertEquals(2, doc.getElementsByTag("p").size());
        Assert.assertEquals("d1", doc.getElementById("d1").id());
        Assert.assertNull(doc.getElementById("not-found"));
        Assert.assertEquals(2, doc.getElementsByClass("sub").size());
        Assert.assertEquals(1, doc.getElementsByAttribute("data-key").size());
        Assert.assertEquals(1, doc.getElementsByAttributeStarting("data-").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValue("id", "p1").size());
        Assert.assertTrue(doc.getElementsByAttributeValueNot("id", "p1").size() > 0);
        Assert.assertEquals(1, doc.getElementsByAttributeValueStarting("custom", "abc").size());
        Assert.assertEquals(1, doc.getElementsByAttributeValueEnding("custom", "456").size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueContaining("custom", " ").size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueMatching("custom", Pattern.compile("\\d+")).size());
        Assert.assertEquals(2, doc.getElementsByAttributeValueMatching("custom", "\\d+").size());

        Element d1 = doc.getElementById("d1");
        Assert.assertEquals(1, d1.getElementsByIndexLessThan(1).size());
        Assert.assertEquals(1, d1.getElementsByIndexGreaterThan(1).size());
        Assert.assertEquals(1, d1.getElementsByIndexEquals(1).size());

        Assert.assertEquals(2, doc.getElementsContainingText("Paragraph").size());
        Assert.assertEquals(1, doc.getElementsContainingOwnText("Paragraph 1").size());
        Assert.assertEquals(2, doc.getElementsMatchingText(Pattern.compile("Paragraph \\d")).size());
        Assert.assertEquals(2, doc.getElementsMatchingText("Paragraph \\d").size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText(Pattern.compile("Paragraph 1")).size());
        Assert.assertEquals(1, doc.getElementsMatchingOwnText("Paragraph 1").size());

        Assert.assertTrue(doc.getAllElements().size() >= 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsByAttributeValueMatchingInvalidPattern() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("attr", "[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingTextInvalidPattern() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetElementsMatchingOwnTextInvalidPattern() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingOwnText("[invalid");
    }

    @Test
    public void testTextAndOwnText() {
        Element el = new Element(Tag.valueOf("p"), "");
        el.append("Hello <b>there</b> <br> now!");
        Assert.assertEquals("Hello there now!", el.text());
        Assert.assertEquals("Hello now!", el.ownText());
        Assert.assertTrue(el.hasText());

        Element emptyEl = new Element(Tag.valueOf("p"), "");
        Assert.assertFalse(emptyEl.hasText());
        Assert.assertEquals("", emptyEl.text());
        Assert.assertEquals("", emptyEl.ownText());

        el.text("Replaced text");
        Assert.assertEquals("Replaced text", el.text());

        Element pre = new Element(Tag.valueOf("pre"), "");
        pre.append("  line1\n  line2  ");
        Assert.assertTrue(Element.preserveWhitespace(pre));
        Assert.assertEquals("line1\n  line2", pre.text());
    }

    @Test
    public void testDataMethod() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x = 1;", ""));
        Assert.assertEquals("var x = 1;", script.data());

        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(script);
        Assert.assertEquals("var x = 1;", div.data());
    }

    @Test
    public void testClassManipulation() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "one two three");

        Assert.assertEquals("one two three", el.className());
        Assert.assertTrue(el.hasClass("two"));
        Assert.assertTrue(el.hasClass("TWO"));
        Assert.assertFalse(el.hasClass("four"));

        el.addClass("four");
        Assert.assertTrue(el.hasClass("four"));

        el.removeClass("two");
        Assert.assertFalse(el.hasClass("two"));

        el.toggleClass("five");
        Assert.assertTrue(el.hasClass("five"));
        el.toggleClass("five");
        Assert.assertFalse(el.hasClass("five"));

        Set<String> customClasses = new LinkedHashSet<String>(Arrays.asList("alpha", "beta"));
        el.classNames(customClasses);
        Assert.assertTrue(el.hasClass("alpha"));
        Assert.assertTrue(el.hasClass("beta"));
        Assert.assertFalse(el.hasClass("one"));
    }

    @Test
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("user");
        Assert.assertEquals("user", input.val());
        Assert.assertEquals("user", input.attr("value"));

        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("long text");
        Assert.assertEquals("long text", textarea.val());
        Assert.assertEquals("long text", textarea.text());
    }

    @Test
    public void testHtmlAndOuterHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.html("<p>Paragraph</p>");
        Assert.assertEquals("<p>Paragraph</p>", el.html());
        Assert.assertEquals("<div>\n <p>Paragraph</p>\n</div>", el.outerHtml());

        Element img = new Element(Tag.valueOf("img"), "");
        Assert.assertEquals("<img />", img.outerHtml());

        Document doc = Jsoup.parse("<div><span>Test</span></div>");
        doc.outputSettings().prettyPrint(false);
        Assert.assertEquals("<div><span>Test</span></div>", doc.body().child(0).outerHtml());
    }

    @Test
    public void testBeforeAfterAndWrap() {
        Element doc = Jsoup.parse("<div><p id='target'>Target</p></div>").body();
        Element target = doc.getElementById("target");

        target.before("<span>Before</span>");
        target.after("<span>After</span>");
        Assert.assertEquals("<span>Before</span><p id=\"target\">Target</p><span>After</span>", doc.child(0).html());

        target.wrap("<div class='wrapper'></div>");
        Assert.assertEquals("wrapper", target.parent().className());
    }

    @Test
    public void testCloneEqualsHashCodeAndToString() {
        Element el1 = new Element(Tag.valueOf("div"), "http://example.com");
        el1.attr("class", "test");
        el1.appendElement("span").text("Content");

        Element clone = el1.clone();
        Assert.assertNotSame(el1, clone);
        Assert.assertEquals(el1.outerHtml(), clone.outerHtml());
        Assert.assertTrue(clone.hasClass("test"));

        clone.addClass("extra");
        Assert.assertTrue(clone.hasClass("extra"));
        Assert.assertFalse(el1.hasClass("extra"));

        Assert.assertEquals(el1, el1);
        Assert.assertNotEquals(el1, clone);
        Assert.assertNotEquals(el1, "not-an-element");
        Assert.assertEquals(el1.hashCode(), el1.hashCode());
        Assert.assertEquals(el1.outerHtml(), el1.toString());
    }
}
