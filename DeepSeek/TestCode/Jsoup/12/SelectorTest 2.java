package org.jsoup.select;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectorTest {

    private Document doc;
    private Element body;
    private Element div1;
    private Element span;
    private Element h1;
    private Element p1;
    private Element p2;
    private Element div2;

    @Before
    public void setUp() {
        String html = "<html><head></head><body>"
                + "<div id=\"div1\" class=\"cls1 cls2\" data-info=\"123\" customAttr=\"val\">"
                + "<p class=\"cls1\">Hello</p>"
                + "<span id=\"sp\">World</span>"
                + "<div id=\"div2\"><p>inner</p></div>"
                + "</div>"
                + "<h1>Title</h1>"
                + "<p>Paragraph</p>"
                + "<p>Second</p>"
                + "</body></html>";
        doc = Jsoup.parse(html);
        body = doc.body();
        div1 = doc.getElementById("div1");
        span = doc.getElementById("sp");
        h1 = doc.getElementsByTag("h1").first();
        p1 = body.getElementsByTag("p").first(); // "Hello" inside div1
        p2 = body.getElementsByTag("p").get(1); // inner inside div2? Actually there are three <p>: first in div1, second in div2, third direct "Paragraph", fourth "Second". Let's recalc. The HTML: div1 contains <p class="cls1">Hello</p>; div2 contains <p>inner</p>; then after div1, direct <p>Paragraph</p>, <p>Second</p>. So p1 is Hello, p2 is inner, p3 is Paragraph, p4 is Second. We'll define accordingly.
        // reassign
        List<Element> allP = body.getElementsByTag("p");
        p1 = allP.get(0); // Hello
        p2 = allP.get(1); // inner
        // p3 = allP.get(2); // Paragraph
        // p4 = allP.get(3); // Second
        div2 = doc.getElementById("div2");
    }

    // Constructor validation

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullQuery() {
        new Selector(null, body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyQuery() {
        new Selector("   ", body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullRoot() {
        new Selector("div", null);
    }

    // Basic selectors

    @Test
    public void testSelectTag() {
        Elements elems = Selector.select("h1", body);
        assertEquals(1, elems.size());
        assertEquals("h1", elems.first().tagName());
    }

    @Test
    public void testSelectById() {
        Elements elems = Selector.select("#div1", body);
        assertEquals(1, elems.size());
        assertEquals("div1", elems.first().id());
    }

    @Test
    public void testSelectByIdNonExistent() {
        Elements elems = Selector.select("#nonexistent", body);
        assertTrue(elems.isEmpty());
    }

    @Test
    public void testSelectByClass() {
        Elements elems = Selector.select(".cls1", body);
        assertEquals(2, elems.size()); // div.cls1 and p.cls1
    }

    @Test
    public void testSelectByAttributeExists() {
        Elements elems = Selector.select("[customAttr]", body);
        assertEquals(1, elems.size());
        assertEquals("div1", elems.first().id());
    }

    @Test
    public void testSelectByAttributeEquals() {
        Elements elems = Selector.select("[customAttr=val]", body);
        assertEquals(1, elems.size());
        assertEquals("div1", elems.first().id());
    }

    @Test
    public void testSelectByAttributeNotEqual() {
        Elements elems = Selector.select("[customAttr!=val]", body);
        // all elements? Actually only div1 has customAttr=val, so none should have not equal with value val? Not equal checks value not equal, but includes elements that don't have attribute? Jsoup's getElementsByAttributeValueNot returns elements that have attribute but value not equal, or if attribute missing? Let's check jsoup behavior: Actually Elements getElementsByAttributeValueNot(String key, String value) returns elements that have the attribute but with a different value. So this should return empty because only element with customAttr has value val. If the attribute is missing, it won't return. So size 0.
        assertEquals(0, elems.size());
    }

    @Test
    public void testSelectByAttributeStartsWith() {
        Elements elems = Selector.select("[customAttr^=va]", body);
        assertEquals(1, elems.size());
        assertEquals("div1", elems.first().id());
    }

    @Test
    public void testSelectByAttributeEndsWith() {
        Elements elems = Selector.select("[customAttr$=al]", body);
        assertEquals(1, elems.size());
    }

    @Test
    public void testSelectByAttributeContaining() {
        Elements elems = Selector.select("[customAttr*=al]", body);
        assertEquals(1, elems.size());
    }

    @Test
    public void testSelectByAttributeMatching() {
        Elements elems = Selector.select("[customAttr~=(?i)VAL]", body); // case insensitive regex
        assertEquals(1, elems.size());
    }

    @Test
    public void testSelectByAttributeStartsWithCaret() {
        // attribute name starting with "data-"
        Elements elems = Selector.select("[^data-]", body);
        assertEquals(1, elems.size());
        assertEquals("div1", elems.first().id());
    }

    // Pseudo selectors

    @Test
    public void testPseudoLt() {
        // :lt(n) elements with sibling index < n
        Elements elems = Selector.select("p:lt(2)", body); // first two p elements (index 0 and 1)
        assertEquals(2, elems.size());
        assertEquals("Hello", elems.get(0).ownText());
        assertEquals("inner", elems.get(1).ownText());
        // but note, the root is body, which has multiple p children at various levels. :lt operates on all elements returned by root (body) children? Actually getElementsByTag("p") returns all descendants. :lt is on the resulting set. So we get first two p from all p in body.
    }

    @Test
    public void testPseudoGt() {
        Elements elems = Selector.select("p:gt(1)", body); // index >1
        assertEquals(2, elems.size()); // third and fourth p
        assertEquals("Paragraph", elems.get(0).ownText());
        assertEquals("Second", elems.get(1).ownText());
    }

    @Test
    public void testPseudoEq() {
        Elements elems = Selector.select("p:eq(0)", body);
        assertEquals(1, elems.size());
        assertEquals("Hello", elems.first().ownText());
    }

    @Test
    public void testPseudoHas() {
        Elements elems = Selector.select("div:has(p)", body);
        assertEquals(2, elems.size()); // div1 (contains p) and div2 (contains p)
    }

    @Test
    public void testPseudoContainsText() {
        Elements elems = Selector.select("p:contains(Paragraph)", body);
        assertEquals(1, elems.size());
        assertEquals("Paragraph", elems.first().ownText());
    }

    @Test
    public void testPseudoContainsOwnText() {
        Elements elems = Selector.select("p:containsOwn(Hello)", body);
        assertEquals(1, elems.size());
        assertEquals("Hello", elems.first().ownText());
    }

    @Test
    public void testPseudoMatches() {
        Elements elems = Selector.select("p:matches(^P)", body); // starts with P
        assertEquals(1, elems.size()); // "Paragraph" starts with P
        assertEquals("Paragraph", elems.first().ownText());
    }

    @Test
    public void testPseudoMatchesOwn() {
        Elements elems = Selector.select("p:matchesOwn(^H)", body); // own text starts with H
        assertEquals(1, elems.size());
        assertEquals("Hello", elems.first().ownText());
    }

    @Test
    public void testPseudoNot() {
        Elements elems = Selector.select("p:not(.cls1)", body); // p without class cls1
        // p with cls1 is first p (Hello). Others should be selected: inner, Paragraph, Second -> 3
        assertEquals(3, elems.size());
    }

    // Combinators

    @Test
    public void testCombinatorDescendant() {
        Elements elems = Selector.select("div p", body);
        // all p inside any div (descendant). That's first p, second p inside div2 -> 2
        assertEquals(2, elems.size());
    }

    @Test
    public void testCombinatorChild() {
        Elements elems = Selector.select("div > p", body);
        // direct children p of div: div1 has direct child p, div2 has direct child p -> 2
        assertEquals(2, elems.size());
    }

    @Test
    public void testCombinatorAdjacentSibling() {
        Elements elems = Selector.select("h1 + p", body); // p immediately after h1
        assertEquals(1, elems.size());
        assertEquals("Paragraph", elems.first().ownText());
    }

    @Test
    public void testCombinatorGeneralSibling() {
        Elements elems = Selector.select("h1 ~ p", body);
        assertEquals(2, elems.size()); // both Paragraph and Second
    }

    @Test
    public void testGroupingComma() {
        Elements elems = Selector.select("h1, p", body);
        assertEquals(5, elems.size()); // h1 + 4 p
    }

    @Test
    public void testMultipleAndSelectors() {
        Elements elems = Selector.select("div#div1.cls1[data-info]", body);
        assertEquals(1, elems.size());
    }

    @Test
    public void testStartWithCombinator() {
        // query starts with combinator, uses root as initial elements
        Elements elems = Selector.select("> p", body); // direct children p of body: p Paragraph, p Second
        assertEquals(2, elems.size());
    }

    @Test
    public void testStartWithHas() {
        // Starting with :has(p) should treat root (body) as all elements, then find those that contain p
        Elements elems = Selector.select(":has(p)", body);
        // body contains p, div1 contains p, div2 contains p, html? root is body, so we check body and its descendants? Actually initial set includes all elements of root: body, head?, body's children. root.getAllElements() returns all elements in that sub-tree. So it will select elements that have a p descendant. That includes body, div1, div2, but not p elements themselves. So count should be 3 (body, div1, div2). However body is the root, but getAllElements includes body itself. So check.
        assertTrue(elems.size() >= 2); // at least div1, div2, body
        assertTrue(elems.contains(body));
        assertTrue(elems.contains(div1));
        assertTrue(elems.contains(div2));
    }

    // Namespace test
    @Test
    public void testByTagNamespace() {
        // Simulate namespace: e.g., "|" in query replaces with ":"
        // We'll add an element <fb:name> to the document and select "fb|name". Need to parse XML-like tag.
        String html = "<html><body><fb:name>test</fb:name></body></html>";
        Document doc = Jsoup.parse(html, "", org.jsoup.parser.Parser.xmlParser());
        Elements elems = Selector.select("fb|name", doc.body());
        assertEquals(1, elems.size());
        assertEquals("fb:name", elems.first().tagName());
    }

    // filterOut test
    @Test
    public void testFilterOut() {
        Elements all = body.getAllElements();
        List<Element> outs = new ArrayList<Element>();
        outs.add(div1);
        outs.add(h1);
        Elements filtered = Selector.filterOut(all, outs);
        assertFalse(filtered.contains(div1));
        assertFalse(filtered.contains(h1));
        assertTrue(filtered.contains(body)); // body not in outs
    }

    // SelectorParseException
    @Test(expected = Selector.SelectorParseException.class)
    public void testParseExceptionInvalidQuery() {
        Selector.select("div::unknown", body);
    }

    // Additional edge cases: empty index for pseudo
    @Test(expected = IllegalArgumentException.class)
    public void testPseudoLtWithInvalidIndex() {
        Selector.select("p:lt(abc)", body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPseudoContainsEmptySubquery() {
        Selector.select(":contains()", body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPseudoHasEmptySubselect() {
        Selector.select(":has()", body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPseudoNotEmptySubselect() {
        Selector.select(":not()", body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testByIdEmptyId() {
        Selector.select("#", body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testByClassEmptyClassName() {
        Selector.select(".", body);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testByTagEmptyTag() {
        // select a tag name that is empty
        Selector.select("", body);
    }

    // Static multi-root select
    @Test
    public void testMultiRootSelect() {
        List<Element> roots = Arrays.asList(div1, div2);
        Elements elems = Selector.select("p", roots);
        assertEquals(2, elems.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiRootNullIterable() {
        Selector.select("div", (Iterable<Element>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiRootEmptyQuery() {
        Selector.select("   ", Collections.singletonList(body));
    }

    // Test filterForSelf via chaining
    @Test
    public void testFilterForSelfChaining() {
        // e.g., div.cls1: matches div with class cls1, which uses filterForSelf
        Elements elems = Selector.select("div.cls1", body);
        assertEquals(1, elems.size());
        assertEquals("div1", elems.first().id());
    }

    // Test whitespace handling in selectors
    @Test
    public void testSelectorWithSpaces() {
        Elements elems = Selector.select("   div   >   p  ", body);
        assertEquals(2, elems.size());
    }
}
