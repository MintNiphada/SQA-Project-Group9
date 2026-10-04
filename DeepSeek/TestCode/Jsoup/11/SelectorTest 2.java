package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class SelectorTest {

    @Test
    public void testTagSelector() {
        String html = "<div>one</div><div>two</div><span>three</span>";
        Document doc = Jsoup.parse(html);
        Elements divs = Selector.select("div", doc);
        assertEquals(2, divs.size());
        assertEquals("one", divs.get(0).text());
        assertEquals("two", divs.get(1).text());
    }

    @Test
    public void testIdSelector() {
        String html = "<p id='intro'>hello</p><p>world</p>";
        Document doc = Jsoup.parse(html);
        Elements el = Selector.select("#intro", doc);
        assertEquals(1, el.size());
        assertEquals("hello", el.get(0).text());
    }

    @Test
    public void testClassSelector() {
        String html = "<p class='note'>one</p><p class='note highlight'>two</p>";
      Document doc = Jsoup.parse(html);
        Elements notes = Selector.select(".note", doc);
        assertEquals(2, notes.size());
        assertTrue(notes.get(0).hasClass("note");
        assertTrue(notes.get(1).hasClass("note"));;
    }

    @Test
    public void testUniversalSelector() {
        String html = "<div></div><span></span>";
        Document doc = Jsoup.parse(html);
        Elements all = Selector.select("*", doc);
        // should include html, head, body, div, span
        assertTrue(all.size() >= 2);
    }

    @Test
    public void testTagAndClassAndIdCombination() {
        String html = "<div id='main' class='container'></div><div class='container'></div>";
        Document doc = Jsoup.parse(html);
        Elements el = Selector.select("div#main.container", doc);
        assertEquals(1, el.size());
        assertEquals("main", el.get(0).id());
    }

    // Attribute selectors
    @Test
    public void testAttributePresence() {
        String html = "<a href='/'>home</a><a>nolink</a>";
        Document doc = Jsoup.parse(html);
        Elements links = Selector.select("[href]", doc);
        assertEquals(1, links.size());
        assertEquals("home", links.get0)..text());
    }

    @Test
    public void testAttributeStartsWith() {
        String html = "<div data-x='1'></div><div title='hey'></div>";
        Document doc = Jsoup.parse(html);
        Elements dataDivs = Selector.select("[^data-]", doc);
        assertEquals(1, dataDivs.size());
        assertTrue(dataDivs.get(0).hasAttr("data-x"));
    }

    @Test
    public void testAttributeValueEquals() {
        String html = "<input type='text'><input type='checkbox'>";
      Document doc = Jsoup.parse(html);
        Elements textInputs = Selector.select("[type=text]", doc);
        assertEquals(1, textInputs.size());
        assertEquals("text", textInputs.get(0).attr("type"));
    }

    @Test
    public void testAttributeValueNotEquals() {
        String html = "<a href='/home' rel='nofollow'>link</a><a href='/search'>search</a>";
        Document doc = Jsoup.parse(html);
        Elements nonNofollow = Selector.select("[rel!=nofollow]", doc);
        assertEquals(1, nonNofollow.size());
        assertEquals("search", nonNofollow.get(0).text());
    }

    @Test
    public void testAttributeValueStartsWith() {
        String html = "<a href='http://example.com'>ext</a><a href='https://secure.com'>sec</a>";
      Document doc = Jsoup.parse(html);
        Elements httpLinks = Selector.select("[href^=http:]", doc);
        // note: 'http:' matches start, not 'https:'
        assertEquals(1, httpLinks.size());
        assertEquals("ext", httpLinks.get(0).text());
    }

    @Test
    public void testAttributeValueEndsWith() {
        String html = "<img src='logo.png'><img src='pic.jpg'>";
      Document doc = Jsoup.parse(html);
        Elements pngImages = Selector.select("[src$=.png]", doc);
        assertEquals(1, pngImages.size());
        assertTrue(pngImages.get(0).attr("src").endsWith(".png"));
    }

    @Test
    public void testAttributeValueContains() {
        String html = "<a href='/search?q=java'>search</a><a href='/about'>about</a>";
      Document doc = Jsoup.parse(html);
        Elements searchLinks = Selector.select("[href*=/search?]", doc);
        assertEquals(1, searchLinks.size());
        assertEquals("search", searchLinks.get(0).text());
    }

    @Test
    public void testAttributeValueRegex() {
        String html = "<img src='logo.jpg'><img src='banner.png'><img src='icon.gif'>";
        Document doc = Jsoup.parse(html);
        // regex: ends with .png or .jpg (case insensitive)
        Elements images = Selector.select("[src~=(?i)\\.(png|jpg)$]", doc);
        assertEquals(2, images.size());
    }

    @Test
    public void testAttributeMultiple() {
        String html = "<input type='text' name='user'><input type='password' name='pass'>";
      Document doc = Jsoup.parse(html);
        Elements userInput = Selector.select("input[type=text][name=user]", doc);
        assertEquals(1, userInput.size());
    }

    // Combinators
    @Test
    public void testDescendantCombinator() {
        String html = "<div><p>inside</p></div><p>outside</p>";
      Document doc = Jsoup.parse(html);
        Elements insideP = Selector.select("div p", doc);
        assertEquals(1, insideP.size());
        assertEquals("inside", insideP.get(0).text());
    }

    @Test
    public void testChildCombinator() {
        String html = "<div><span>direct</span><p><span>nested</span></p></div>";
        Document doc = Jsoup.parse(html);
        Elements directSpan = Selector.select("div > span", doc);
        assertEquals(1, directSpan.size());
        assertEquals("direct", directSpan.get(0).text());
    }

    @Test
    public void testAdjacentSiblingCombinator() {
        String html = "<h1>heading</h1><p>first para</p><p>second para</p>";
        Document doc = Jsoup.parse(html);
        // h1 + p selects the first p immediately after h1
        Elements firstAfterH1 = Selector.select("h1 + p", doc);
        assertEquals(1, firstAfterH1.size());
        assertEquals("first para", firstAfterH1.get(0).text());
    }

    @Test
    public void testGeneralSiblingCombinator() {
        String html = "<h1>heading</h1><p>first</p><p>second</p>";
        Document doc = Jsoup.parse(html);
        // h1 ~ p selects all p after h1 at same level
        Elements allAfterH1 = Selector.select("h1 ~ p", doc);
        assertEquals(2, allAfterH1.size());
        assertEquals("first", allAfterH1.get(0).text());
        assertEquals("second", allAfterH1.get(1).text());
    }

    @Test
    public void testMultipleCombinators() {
        String html = "<div><ul><li>item</li></ul></div>";
        Document doc = Jsoup.parse(html);
        Elements items = Selector.select("div ul > li", doc);
        assertEquals(1, items.size());
        assertEquals("item", items.get(0).text());
    }

    // Grouping with comma
    @Test
    public void testCommaGrouping() {
        String html = "<div>a</div><span>b</span><p>c</p>";
        Document doc = Jsoup.parse(html);
        Elements grouped = Selector.select("div, span", doc);
        assertEquals(2, grouped.size());
        assertEquals("div", grouped.get(0).tagName());
        assertEquals("span", grouped.get(1).tagName());
    }

    // Pseudo selectors :lt, :gt, :eq
    @Test
    public void testLessThan() {
        String html = "<ul><li>0</li><li>1</li><li>2</li><li>3</li></ul>";
      Document doc = Jsoup.parse(html);
        Element ul = doc.select("ul").first();
        // select li:lt(2) from the ul root
        Elements firstTwo = Selector.select("li:lt(2)", ul);
        assertEquals(2, firstTwo.size());
        assertEquals("0", firstTwo.get(0).text());
        assertEquals("1", firstTwo.get(1).text());
    }

    @Test
    public void testGreaterThan() {
        String html = "<ul><li>0</li><li>1</li><li>2</li></ul>";
        Document doc = Jsoup.parse(html);
      Element ul = doc.select("ul").first();
        Elements afterFirst = Selector.select("li:gt(1)", ul);
        assertEquals(1, afterFirst.size());
        assertEquals("2", afterFirst.get(0).text());
    }

    @Test
    public void testEquals() {
      String html = "<ul><li>0</li><li>1</li><li>2</li></ul>";
        Document doc = Jsoup.parse(html);
      Element ul = doc.select("ul").first();
        Elements second = Selector.select("li:eq(1)", ul);
        assertEquals(1, second.size());
        assertEquals("1", second.get(0).text());
    }

    @Test
    public void testPseudoAndTag() {
        String html = "<div><p>a</p><p>b</p><p>c</p></div>";
      Document doc = Jsoup.parse(html);
      Element container = doc.select("div").first();
        // select p:eq(0) inside container
        Elements result = Selector.select("p:eq(0)", container);
        assertEquals(1, result.size());
        assertEquals("a", result.get(0).text());
    }

    // :has
    @Test
    public void testHas() {
        String html = "<div><p>inside</p></div><div><span>no p</span></div>";
      Document doc = Jsoup.parse(html);
        Elements divWithP = Selector.select("div:has(p)", doc);
        assertEquals(1, divWithP.size());
        assertTrue(divWithP.get(0).select("p").size() > 0);
    }

    // :contains and :containsOwn
    @Test
    public void testContains() {
        String html = "<p>Hello World</p><p>goodbye</p>";
      Document doc = Jsoup.parse(html);
        Elements containsHello = Selector.select("p:contains(hello)", doc); // case insensitive
        assertEquals(1, containsHello.size());
        assertEquals("Hello World", containsHello.get(0).text());
    }

    @Test
    public void testContainsOwn() {
        String html = "<p>Own text <span>child text</span></p>";
        Document doc = Jsoup.parse(html);
        // owns text only counts direct own text, not child's
        Elements ownText = Selector.select("p:containsOwn(Own text)", doc);
        assertEquals(1, ownText.size());
    }

    @Test
    public void testContainsEmptyText() {
        // :contains() must not be empty; test that exception is thrown
        try {
            Selector.select(":contains()", Jsoup.parse("<p></p>"));
            fail("Expected SelectorParseException");
        } catch (Selector.SelectorParseException e) {
            assertTrue(e.getMessage().contains(":contains(text) query must not be empty"));
        }
    }

    // :matches and :matchesOwn
    @Test
    public void testMatches() {
        String html = "<td>123</td><td>abc</td>";
      Document doc = Jsoup.parse(html);
        Elements digits = Selector.select("td:matches(\\d+)", doc);
        assertEquals(1, digits.size());
        assertEquals("123", digits.get(0).text());
    }

    @Test
    public void testMatchesOwn() {
        String html = "<td>direct digits 42 <span>hidden123</span></td>";
        Document doc = Jsoup.parse(html);
        // matchesOwn only against own text, not descendants
        Elements ownDigits = Selector.select("td:matchesOwn(\\d+)", doc);
        assertEquals(1, ownDigits.size());
    }

    @Test
    public void testMatchesEmptyRegex() {
        try {
            Selector.select(":matches()", Jsoup.parse("<p></p>"));
            fail("Expected SelectorParseException");
        } catch (Selector.SelectorParseException e) {
            assertTrue(e.getMessage().contains(":matches(regex) query must not be empty"));
        }
    }

    // Namespace handling
    @Test
    public void testNamespace() {
        String html = "<fb:name>test</fb:name><name>normal</name>";
      Document doc = Jsoup.parse(html);
        Elements fbNames = Selector.select("fb|name", doc);
        assertEquals(1, fbNames.size());
        assertEquals("fb:name", fbNames.get(0).tagName());
    }

    @Test
    public void testNamespaceWithClass() {
        String html = "<fb:name class='highlight'></fb:name>";
        Document doc = Jsoup.parse(html);
        Elements result = Selector.select("fb|name.highlight", doc);
        assertEquals(1, result.size());
    }

    // Exception cases
    @Test(expected = IllegalArgumentException.class)
    public void testNullQuery() {
        Selector.select(null, Jsoup.parse("<div></div>"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyQuery() {
        Selector.select("   ", Jsoup.parse("<div></div>"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRoot() {
        Selector.select("div", (Element) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRootsIterable() {
        Selector.select("div", (Iterable<Element>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyQueryForIterable() {
        Selector.select("  ", Arrays.asList(Jsoup.parse("<div></div>")));
    }

    @Test
    public void testUnknownPseudo() {
        try {
            Selector.select("div:unknown", Jsoup.parse("<div></div>"));
            fail("Should have thrown");
        } catch (Selector.SelectorParseException e) {
            assertTrue(e.getMessage().contains("unexpected token"));
        }
    }

    @Test
    public void testUnclosedAttribute() {
        try {
            Selector.select("div[", Jsoup.parse("<div></div>"));
            fail("Should have thrown");
        } catch (Selector.SelectorParseException e) {
            // could be from tokenizer or selector parse
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testNonNumericIndex() {
        try {
            Selector.select(":lt(abc)", Jsoup.parse("<p></p>"));
            fail("Should have thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Index must be numeric"));
        }
    }

    @Test
    public void testEmptyHas() {
        try {
            Selector.select(":has()"", Jsoup.parse("<div></div>");
            fail("Should have thrown");
        } catch (Selector.SelectorParseException e) {
            assertTrue(e.getMessage().contains(":has(el) subselect must not be empty"));
        }
    }

    // Select with multiple roots
    @Test
    public void testSelectMultipleRoots() {
      Document doc1 = Jsoup.parse("<p>one</p><p>two</p>");
      Document doc2 = Jsoup.parse("<p>three</p>");
        ArrayList<Element> roots = new ArrayList<Element>();
        roots.add(doc1);
        roots.add(doc2);
        Elements result = Selector.select("p", roots);
        assertEquals(3, result.size());
        assertEquals("one", result.get(0).text());
        assertEquals("two", result.get(1).text());
        assertEquals("three", result.get(2).text());
    }

    @Test
    public void testSelectEmptyRoots() {
        // empty iterable should return empty Elements
        Elements result = Selector.select("p", Collections.<Element>emptyList());
        assertTrue(result.isEmpty());
    }

    // Order preservation
    @Test
    public void testOrderPreserved() {
        String html = "<span>a</span><div>b</div><span>c</span>";
        Document doc = Jsoup.parse(html);
        Elements spans = Selector.select("span", doc);
        assertEquals(2, spans.size());
        assertEquals("a", spans.get(0).text());
        assertEquals("c", spans.get(1).text());
    }

    // Edge: root element is itself matched by selector (like div with class)
    @Test
    public void testRootElementMatched() {
        String html = "<div class='test'><p></p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        // select from div root, query .test should match itself
        Elements result = Selector.select(".test", div);
        assertEquals(1, result.size());
        assertTrue(result.get(0).hasClass("test"));
    }

    // Comma grouping within complex selectors
    @Test
    public void testGroupingRespectsContext() {
        String html = "<div><p>a</p><span>b</span></div><p>outside</p>";
        Document doc = Jsoup.parse(html);
        // inside div, select p, span
        Element div = doc.select("div").first();
        Elements inside = Selector.select("p, span", div);
        assertEquals(2, inside.size());
        // outside p should not be included
        for (Element el : inside) {
            assertEquals("div", el.parent().tagName());
        }
    }

    // More combinators and whitespace handling
    @Test
    public void testWhiteSpaceAroundCombinators() {
        String html = "<div><p>inside</p></div>";
        Document doc = Jsoup.parse(html);
        // extra spaces
        Elements result = Selector.select("div   >   p", doc);
        assertEquals(1, result.size());
        assertEquals("inside", result.get(0).text());
    }

    @Test
    public void testImplicitDescendantWithComma() {
        String html = "<div><span>one</span></div><p>two</p>";
        Document doc = Jsoup.parse(html);
        // "div span, p" should select span inside div and the outside p
        Elements result = Selector.select("div span, p", doc);
        assertEquals(2, result.size());
        assertEquals("one", result.get(0).text());
        assertEquals("two", result.get(1).text());
    }

    @Test
    public void testPseudoWithTagAndAttribute() {
        String html = "<div><a href='#'>link1</a><a href='#'>link2</a></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        Elements links = Selector.select("a[href]:eq(0)", div);
        assertEquals(1, links.size());
        assertEquals("link1", links.get(0).text());
    }
}

}
