package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectorTest {

    @Test
    public void testSelectByTag() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><span>Three</span></div>");
        Elements pTags = Selector.select("p", doc);
        Assert.assertEquals(2, pTags.size());
        Assert.assertEquals("One", pTags.get(0).text());
        Assert.assertEquals("Two", pTags.get(1).text());
    }

    @Test
    public void testSelectByNamespaceTag() {
        Document doc = Jsoup.parse("<xml><fb:name>Facebook</fb:name><other>Test</other></xml>");
        Elements fbTags = Selector.select("fb|name", doc);
        Assert.assertEquals(1, fbTags.size());
        Assert.assertEquals("fb:name", fbTags.get(0).tagName());
        Assert.assertEquals("Facebook", fbTags.get(0).text());
    }

    @Test
    public void testSelectById() {
        Document doc = Jsoup.parse("<div><p id=\"p1\">First</p><p id=\"p2\">Second</p></div>");
        Elements p1 = Selector.select("#p1", doc);
        Assert.assertEquals(1, p1.size());
        Assert.assertEquals("First", p1.get(0).text());

        Elements notFound = Selector.select("#nonexistent", doc);
        Assert.assertEquals(0, notFound.size());

        Elements tagAndId = Selector.select("p#p2", doc);
        Assert.assertEquals(1, tagAndId.size());
        Assert.assertEquals("Second", tagAndId.get(0).text());
    }

    @Test
    public void testSelectByClass() {
        Document doc = Jsoup.parse("<div><p class=\"intro\">Intro</p><p class=\"body text\">Body</p></div>");
        Elements intros = Selector.select(".intro", doc);
        Assert.assertEquals(1, intros.size());
        Assert.assertEquals("Intro", intros.get(0).text());

        Elements bodies = Selector.select("p.body", doc);
        Assert.assertEquals(1, bodies.size());
        Assert.assertEquals("Body", bodies.get(0).text());

        Elements combined = Selector.select("p.body.text", doc);
        Assert.assertEquals(1, combined.size());
        Assert.assertEquals("Body", combined.get(0).text());
    }

    @Test
    public void testSelectAll() {
        Document doc = Jsoup.parse("<div><p><span>Hello</span></p></div>");
        Elements allInDiv = Selector.select("div *", doc);
        Assert.assertTrue(allInDiv.size() >= 2);
    }

    @Test
    public void testSelectByAttribute() {
        Document doc = Jsoup.parse("<div id=\"main\" data-src=\"img.png\" data-alt=\"desc\" title=\"greeting\">"
                + "<a href=\"http://example.com/sub/test.html\" rel=\"nofollow\">Link</a>"
                + "<a href=\"https://secure.example.com\" rel=\"alternate\">Secure</a>"
                + "<img src=\"picture.png\" width=\"500\" />"
                + "<input type=\"text\" name=\"user123\" />"
                + "</div>");

        // [attr]
        Elements hasTitle = Selector.select("[title]", doc);
        Assert.assertEquals(1, hasTitle.size());
        Assert.assertEquals("greeting", hasTitle.attr("title"));

        // [^attrPrefix]
        Elements dataAttrs = Selector.select("[^data-]", doc);
        Assert.assertEquals(1, dataAttrs.size());
        Assert.assertEquals("main", dataAttrs.attr("id"));

        // [attr=val]
        Elements relNofollow = Selector.select("[rel=nofollow]", doc);
        Assert.assertEquals(1, relNofollow.size());
        Assert.assertEquals("Link", relNofollow.text());

        // [attr!=val]
        Elements notNofollow = Selector.select("a[rel!=nofollow]", doc);
        Assert.assertEquals(1, notNofollow.size());
        Assert.assertEquals("Secure", notNofollow.text());

        // [attr^=valPrefix]
        Elements hrefHttp = Selector.select("a[href^=http:]", doc);
        Assert.assertEquals(1, hrefHttp.size());
        Assert.assertEquals("Link", hrefHttp.text());

        // [attr$=valSuffix]
        Elements imgPng = Selector.select("img[src$=.png]", doc);
        Assert.assertEquals(1, imgPng.size());
        Assert.assertEquals("picture.png", imgPng.attr("src"));

        // [attr*=valContaining]
        Elements hrefSub = Selector.select("a[href*=/sub/]", doc);
        Assert.assertEquals(1, hrefSub.size());
        Assert.assertEquals("Link", hrefSub.text());

        // [attr~=regex]
        Elements matchName = Selector.select("input[name~=user\\d+]", doc);
        Assert.assertEquals(1, matchName.size());
        Assert.assertEquals("user123", matchName.attr("name"));
    }

    @Test
    public void testPseudoIndexSelectors() {
        Document doc = Jsoup.parse("<ul><li>0</li><li>1</li><li>2</li><li>3</li><li>4</li></ul>");

        Elements lt = Selector.select("li:lt(2)", doc);
        Assert.assertEquals(2, lt.size());
        Assert.assertEquals("0", lt.get(0).text());
        Assert.assertEquals("1", lt.get(1).text());

        Elements gt = Selector.select("li:gt(2)", doc);
        Assert.assertEquals(2, gt.size());
        Assert.assertEquals("3", gt.get(0).text());
        Assert.assertEquals("4", gt.get(1).text());

        Elements eq = Selector.select("li:eq(2)", doc);
        Assert.assertEquals(1, eq.size());
        Assert.assertEquals("2", eq.get(0).text());
    }

    @Test
    public void testPseudoTextSelectors() {
        Document doc = Jsoup.parse("<div id=\"d1\"><p>Hello <b>World</b></p><p>Foo Bar</p></div>");

        // :contains(text)
        Elements contains = Selector.select("p:contains(World)", doc);
        Assert.assertEquals(1, contains.size());
        Assert.assertEquals("Hello World", contains.get(0).text());

        // :containsOwn(text)
        Elements containsOwnP = Selector.select("p:containsOwn(Hello)", doc);
        Assert.assertEquals(1, containsOwnP.size());

        Elements containsOwnB = Selector.select("b:containsOwn(World)", doc);
        Assert.assertEquals(1, containsOwnB.size());

        Elements containsOwnPFalse = Selector.select("p:containsOwn(World)", doc);
        Assert.assertEquals(0, containsOwnPFalse.size());

        // :matches(regex)
        Elements matches = Selector.select("p:matches((?i)foo)", doc);
        Assert.assertEquals(1, matches.size());
        Assert.assertEquals("Foo Bar", matches.get(0).text());

        // :matchesOwn(regex)
        Elements matchesOwn = Selector.select("p:matchesOwn(^Foo\\s+Bar$)", doc);
        Assert.assertEquals(1, matchesOwn.size());
    }

    @Test
    public void testPseudoHasSelector() {
        Document doc = Jsoup.parse("<div class=\"box\"><p><span>Inside</span></p></div><div class=\"empty\"></div>");
        Elements hasSpan = Selector.select("div:has(span)", doc);
        Assert.assertEquals(1, hasSpan.size());
        Assert.assertEquals("box", hasSpan.get(0).className());
    }

    @Test
    public void testCombinators() {
        Document doc = Jsoup.parse("<div id=\"root\">"
                + "<div id=\"c1\" class=\"first\">"
                + "  <span id=\"s1\">Child1</span>"
                + "  <p id=\"p1\">Paragraph</p>"
                + "</div>"
                + "<div id=\"c2\" class=\"second\">"
                + "  <span id=\"s2\">Child2</span>"
                + "</div>"
                + "<div id=\"c3\" class=\"third\">"
                + "  <span id=\"s3\">Child3</span>"
                + "</div>"
                + "</div>");

        // Child combinator (>)
        Elements childSpan = Selector.select("div#c1 > span", doc);
        Assert.assertEquals(1, childSpan.size());
        Assert.assertEquals("s1", childSpan.get(0).id());

        // Descendant combinator (space)
        Elements descendantSpan = Selector.select("div#root span", doc);
        Assert.assertEquals(3, descendantSpan.size());

        // Adjacent sibling combinator (+)
        Elements adjacent = Selector.select("div#c1 + div", doc);
        Assert.assertEquals(1, adjacent.size());
        Assert.assertEquals("c2", adjacent.get(0).id());

        // General sibling combinator (~)
        Elements siblings = Selector.select("div#c1 ~ div", doc);
        Assert.assertEquals(2, siblings.size());
        Assert.assertEquals("c2", siblings.get(0).id());
        Assert.assertEquals("c3", siblings.get(1).id());

        // Group / OR combinator (,)
        Elements group = Selector.select("span#s1, span#s3", doc);
        Assert.assertEquals(2, group.size());
        Assert.assertEquals("s1", group.get(0).id());
        Assert.assertEquals("s3", group.get(1).id());
    }

    @Test
    public void testRootCombinatorPrefix() {
        Element div = Jsoup.parse("<div><p>Direct</p><span><p>Nested</p></span></div>").body().child(0);

        // Starts with '>' combinator
        Elements directChildren = Selector.select("> p", div);
        Assert.assertEquals(1, directChildren.size());
        Assert.assertEquals("Direct", directChildren.get(0).text());
    }

    @Test
    public void testSelectMultipleRoots() {
        Document doc = Jsoup.parse("<div><p>P1</p></div><div><p>P2</p></div>");
        Elements divs = doc.select("div");
        Elements pTags = Selector.select("p", divs);
        Assert.assertEquals(2, pTags.size());
        Assert.assertEquals("P1", pTags.get(0).text());
        Assert.assertEquals("P2", pTags.get(1).text());
    }

    @Test
    public void testComplexSelectorIntersection() {
        Document doc = Jsoup.parse("<div id=\"d1\" class=\"active highlight\" title=\"test\">One</div>"
                + "<div id=\"d2\" class=\"active\">Two</div>");
        Elements found = Selector.select("div.active.highlight#d1[title=test]", doc);
        Assert.assertEquals(1, found.size());
        Assert.assertEquals("One", found.get(0).text());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectNullQuery() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(null, doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectEmptyQuery() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("   ", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectNullRoot() {
        Selector.select("div", (Element) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectNullRootsIterable() {
        Selector.select("div", (Iterable<Element>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSelectEmptyQueryRootsIterable() {
        List<Element> list = new ArrayList<Element>();
        list.add(Jsoup.parse("<div></div>"));
        Selector.select("", list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConsumeIndexInvalid() {
        Document doc = Jsoup.parse("<div><p>A</p><p>B</p></div>");
        Selector.select("p:eq(invalid)", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyHasClause() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(":has()", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContainsClause() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(":contains()", doc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMatchesClause() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select(":matches()", doc);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testUnexpectedTokenThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("div % invalid", doc);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidAttributeSelectorThrowsException() {
        Document doc = Jsoup.parse("<div></div>");
        Selector.select("[attr?value]", doc);
    }

    @Test
    public void testSelectorParseExceptionConstructor() {
        Selector.SelectorParseException ex = new Selector.SelectorParseException("Error at %s: %d", "token", 5);
        Assert.assertEquals("Error at token: 5", ex.getMessage());
    }
}
