package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SelectorTest {

    @Test
    public void testSelectByTag() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><span>Three</span></div>");
        Elements ps = Selector.select("p", doc);
        assertEquals(2, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Two", ps.get(1).text());

        Elements divs = Selector.select("div", doc);
        assertEquals(1, divs.size());
    }

    @Test
    public void testSelectById() {
        Document doc = Jsoup.parse("<div id='d1'><p id='p1'>One</p><p id='p2'>Two</p></div>");
        Elements p1 = Selector.select("#p1", doc);
        assertEquals(1, p1.size());
        assertEquals("p1", p1.first().id());

        Elements notFound = Selector.select("#nonexistent", doc);
        assertEquals(0, notFound.size());
    }

    @Test
    public void testSelectByClass() {
        Document doc = Jsoup.parse("<div class='main content'><p class='lead'>One</p><p class='lead bold'>Two</p></div>");
        Elements leads = Selector.select(".lead", doc);
        assertEquals(2, leads.size());

        Elements boldLeads = Selector.select(".lead.bold", doc);
        assertEquals(1, boldLeads.size());
        assertEquals("Two", boldLeads.first().text());

        Elements mainContent = Selector.select("div.main", doc);
        assertEquals(1, mainContent.size());
    }

    @Test
    public void testSelectAll() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        Elements all = Selector.select("*", doc.body());
        assertTrue(all.size() >= 4); // body, div, p, span
    }

    @Test
    public void testSelectNamespacedTag() {
        Document doc = Jsoup.parse("<xml><fb:name>Facebook</fb:name><other:name>Other</other:name></xml>", "", org.jsoup.parser.Parser.xmlParser());
        Elements fbName = Selector.select("fb|name", doc);
        assertEquals(1, fbName.size());
        assertEquals("Facebook", fbName.first().text());
    }

    @Test
    public void testAttributeSelectors() {
        String html = "<div id='div1' class='test-class' title='foo' data-role='admin' data-src='foo.png' " +
                "href='http://example.com/test' rel='nofollow external' custom='abc-123-def'>" +
                "<a href='https://secure.example.com'>Secure</a>" +
                "<a href='http://example.com/path/index.html'>Index</a>" +
                "<img src='image.png' />" +
                "<img src='image.jpg' />" +
                "</div>";
        Document doc = Jsoup.parse(html);

        // [attr]
        Elements hasTitle = Selector.select("[title]", doc);
        assertEquals(1, hasTitle.size());

        // [^attrPrefix]
        Elements dataAttrs = Selector.select("[^data-]", doc);
        assertEquals(1, dataAttrs.size());

        // [attr=val]
        Elements exactTitle = Selector.select("[title=foo]", doc);
        assertEquals(1, exactTitle.size());

        // [attr!=val]
        Elements notTitle = Selector.select("img[src!=image.png]", doc);
        assertEquals(1, notTitle.size());
        assertEquals("image.jpg", notTitle.first().attr("src"));

        // [attr^=valPrefix]
        Elements startsWith = Selector.select("a[href^=https]", doc);
        assertEquals(1, startsWith.size());
        assertEquals("Secure", startsWith.first().text());

        // [attr$=valSuffix]
        Elements endsWith = Selector.select("img[src$=.png]", doc);
        assertEquals(1, endsWith.size());
        assertEquals("image.png", endsWith.first().attr("src"));

        // [attr*=valContaining]
        Elements contains = Selector.select("div[custom*=123]", doc);
        assertEquals(1, contains.size());

        // [attr~=regex]
        Elements regexMatch = Selector.select("img[src~=(?i)\\.(png|jpe?g)]", doc);
        assertEquals(2, regexMatch.size());
    }

    @Test
    public void testCombinators() {
        String html = "<div id='root'>" +
                "<div class='parent1'>" +
                "<p class='first'>First</p>" +
                "<p class='second'>Second</p>" +
                "<span>Span 1</span>" +
                "<span>Span 2</span>" +
                "</div>" +
                "<div class='parent2'>" +
                "<div><p class='nested'>Nested</p></div>" +
                "</div>" +
                "</div>";
        Document doc = Jsoup.parse(html);
        Element root = doc.getElementById("root");

        // Descendant (E F)
        Elements descendants = Selector.select("div p", root);
        assertEquals(3, descendants.size());

        // Child (E > F)
        Elements children = Selector.select("div.parent1 > p", root);
        assertEquals(2, children.size());

        Elements notDirectChildren = Selector.select("div.parent2 > p", root);
        assertEquals(0, notDirectChildren.size());

        // Adjacent sibling (E + F)
        Elements adjacent = Selector.select("p.first + p", root);
        assertEquals(1, adjacent.size());
        assertEquals("Second", adjacent.first().text());

        // General sibling (E ~ F)
        Elements general = Selector.select("p.first ~ span", root);
        assertEquals(2, general.size());

        // Group (E, F)
        Elements group = Selector.select("p.first, p.second", root);
        assertEquals(2, group.size());

        Elements multiGroup = Selector.select("p.first, span, .nested", root);
        assertEquals(4, multiGroup.size());
    }

    @Test
    public void testLeadingCombinators() {
        Document doc = Jsoup.parse("<div id='d'><p>One</p><p>Two</p><span>Three</span></div>");
        Element div = doc.getElementById("d");

        Elements childP = Selector.select("> p", div);
        assertEquals(2, childP.size());

        Element firstP = div.select("p").first();
        Elements nextP = Selector.select("+ p", firstP);
        assertEquals(1, nextP.size());
        assertEquals("Two", nextP.first().text());

        Elements spanSib = Selector.select("~ span", firstP);
        assertEquals(1, spanSib.size());
        assertEquals("Three", spanSib.first().text());

        Elements descendantSpan = Selector.select(" span", div);
        assertEquals(1, descendantSpan.size());
    }

    @Test
    public void testPseudoIndexSelectors() {
        Document doc = Jsoup.parse("<ul><li>0</li><li>1</li><li>2</li><li>3</li><li>4</li></ul>");

        Elements lt = Selector.select("li:lt(2)", doc);
        assertEquals(2, lt.size());
        assertEquals("0", lt.get(0).text());
        assertEquals("1", lt.get(1).text());

        Elements gt = Selector.select("li:gt(2)", doc);
        assertEquals(2, gt.size());
        assertEquals("3", gt.get(0).text());
        assertEquals("4", gt.get(1).text());

        Elements eq = Selector.select("li:eq(2)", doc);
        assertEquals(1, eq.size());
        assertEquals("2", eq.first().text());
    }

    @Test
    public void testPseudoContentSelectors() {
        Document doc = Jsoup.parse("<div><p class='lead'>Hello <b>World</b></p><p>Goodbye World</p><p>Hello</p></div>");

        // :contains(text)
        Elements contains = Selector.select("p:contains(Hello)", doc);
        assertEquals(2, contains.size());

        Elements containsInsensitive = Selector.select("p:contains(hello)", doc);
        assertEquals(2, containsInsensitive.size());

        // :containsOwn(text)
        Elements containsOwn = Selector.select("p:containsOwn(Hello)", doc);
        assertEquals(2, containsOwn.size());

        Elements containsOwnWorld = Selector.select("p:containsOwn(World)", doc);
        assertEquals(1, containsOwnWorld.size());
        assertEquals("Goodbye World", containsOwnWorld.first().text());

        // :matches(regex)
        Elements matches = Selector.select("p:matches((?i)goodbye)", doc);
        assertEquals(1, matches.size());

        // :matchesOwn(regex)
        Elements matchesOwn = Selector.select("p:matchesOwn(^Hello$)", doc);
        assertEquals(1, matchesOwn.size());
        assertEquals("Hello", matchesOwn.first().text());
    }

    @Test
    public void testPseudoHasAndNot() {
        Document doc = Jsoup.parse("<div><div class='a'><p>Paragraph 1</p></div><div class='b'><span>Span 1</span></div></div>");

        // :has(selector) starting query
        Elements hasPFromRoot = Selector.select(":has(p)", doc.body());
        assertTrue(hasPFromRoot.size() >= 2); // div and div.a

        Elements divHasP = Selector.select("div:has(p)", doc.body());
        assertEquals(2, divHasP.size());

        // :not(selector)
        Elements notP = Selector.select("div > div:not(.a)", doc.body());
        assertEquals(1, notP.size());
        assertEquals("b", notP.first().className());
    }

    @Test
    public void testChainedAndIntersections() {
        Document doc = Jsoup.parse("<div id='d1' class='main highlight' title='test'>Found</div>" +
                "<div id='d2' class='main'>Skip</div>");

        Elements result = Selector.select("div#d1.main.highlight[title=test]", doc);
        assertEquals(1, result.size());
        assertEquals("Found", result.first().text());

        Elements empty = Selector.select("div#d1.other", doc);
        assertEquals(0, empty.size());
    }

    @Test
    public void testSelectMultipleRoots() {
        Document doc1 = Jsoup.parse("<p class='target'>Doc1</p>");
        Document doc2 = Jsoup.parse("<p class='target'>Doc2</p>");
        List<Element> roots = Arrays.asList(doc1.body(), doc2.body());

        Elements result = Selector.select("p.target", roots);
        assertEquals(2, result.size());
        assertEquals("Doc1", result.get(0).text());
        assertEquals("Doc2", result.get(1).text());
    }

    @Test
    public void testFilterOutStaticMethod() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("p"), "");
        Element el3 = new Element(Tag.valueOf("span"), "");

        List<Element> from = Arrays.asList(el1, el2, el3);
        List<Element> out = Collections.singletonList(el2);

        Elements filtered = Selector.filterOut(from, out);
        assertEquals(2, filtered.size());
        assertTrue(filtered.contains(el1));
        assertFalse(filtered.contains(el2));
        assertTrue(filtered.contains(el3));
    }

    @Test
    public void testSelectorParseException() {
        Selector.SelectorParseException ex = new Selector.SelectorParseException("Test error %s", "details");
        assertEquals("Test error details", ex.getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullQueryThrows() {
        Selector.select(null, new Element(Tag.valueOf("div"), ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyQueryThrows() {
        Selector.select("   ", new Element(Tag.valueOf("div"), ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRootThrows() {
        Selector.select("div", (Element) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRootsCollectionThrows() {
        Selector.select("div", (Iterable<Element>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyQueryMultipleRootsThrows() {
        Selector.select("", Collections.singletonList(new Element(Tag.valueOf("div"), "")));
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testUnhandledTokenThrows() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("???", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidIndexPseudoSelectorThrows() {
        Document doc = Jsoup.parse("<div><p>1</p></div>");
        Selector.select("p:lt(notanumber)", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyHasThrows() {
        Document doc = Jsoup.parse("<div><p>1</p></div>");
        Selector.select(":has()", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContainsThrows() {
        Document doc = Jsoup.parse("<div><p>1</p></div>");
        Selector.select(":contains()", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMatchesThrows() {
        Document doc = Jsoup.parse("<div><p>1</p></div>");
        Selector.select(":matches()", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyNotThrows() {
        Document doc = Jsoup.parse("<div><p>1</p></div>");
        Selector.select(":not()", doc);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidAttributeSelectorSyntaxThrows() {
        Document doc = Jsoup.parse("<div><p>1</p></div>");
        Selector.select("[attr?invalid]", doc);
    }

    @Test
    public void testAdjacentAndGeneralSiblingEdgeCases() {
        Document doc = Jsoup.parse("<div><p id='p1'></p><span id='s1'></span><p id='p2'></p></div><div><p id='p3'></p></div>");
        Element p1 = doc.getElementById("p1");
        Element p3 = doc.getElementById("p3");

        // Candidates in different parents
        Elements adj = Selector.select("p#p1 + p#p3", doc);
        assertEquals(0, adj.size());

        Elements gen = Selector.select("p#p1 ~ p#p3", doc);
        assertEquals(0, gen.size());

        // Preceding sibling where index is not greater
        Elements genReverse = Selector.select("p#p2 ~ p#p1", doc);
        assertEquals(0, genReverse.size());

        // First sibling has no previous sibling
        Elements adjFirst = Selector.select("+ p#p1", doc.body());
        assertEquals(0, adjFirst.size());
    }

    @Test
    public void testDescendantFilterEdgeCase() {
        Document doc = Jsoup.parse("<div><p><span>text</span></p></div>");
        Element div = doc.select("div").first();
        Elements p = div.select("p");
        // When parent matches candidate in filterForDescendants, it should be excluded
        Elements result = Selector.select("p p", div);
        assertEquals(0, result.size());
    }

    @Test
    public void testChildFilterParentMismatch() {
        Document doc = Jsoup.parse("<div><p><span>text</span></p></div>");
        Element span = doc.select("span").first();
        // Orphan elements without parents
        Element detached1 = new Element(Tag.valueOf("div"), "");
        Element detached2 = new Element(Tag.valueOf("span"), "");
        detached1.appendChild(detached2);

        Elements result = Selector.select("div > span", doc);
        assertEquals(0, result.size());
    }
}
