package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class QueryParserTest {

    @Test
    public void testBasicSelectors() {
        Evaluator eval = QueryParser.parse("div");
        Assert.assertTrue(eval instanceof Evaluator.Tag);

        eval = QueryParser.parse("#main");
        Assert.assertTrue(eval instanceof Evaluator.Id);

        eval = QueryParser.parse(".content");
        Assert.assertTrue(eval instanceof Evaluator.Class);

        eval = QueryParser.parse("*");
        Assert.assertTrue(eval instanceof Evaluator.AllElements);
    }

    @Test
    public void testNamespaceTags() {
        Evaluator eval = QueryParser.parse("*|div");
        Assert.assertTrue(eval instanceof CombiningEvaluator.Or);

        eval = QueryParser.parse("ns|div");
        Assert.assertTrue(eval instanceof Evaluator.Tag);
    }

    @Test
    public void testAttributes() {
        Evaluator eval = QueryParser.parse("[href]");
        Assert.assertTrue(eval instanceof Evaluator.Attribute);

        eval = QueryParser.parse("[^data-]");
        Assert.assertTrue(eval instanceof Evaluator.AttributeStarting);

        eval = QueryParser.parse("[href=http://example.com]");
        Assert.assertTrue(eval instanceof Evaluator.AttributeWithValue);

        eval = QueryParser.parse("[href!=http://example.com]");
        Assert.assertTrue(eval instanceof Evaluator.AttributeWithValueNot);

        eval = QueryParser.parse("[href^=https]");
        Assert.assertTrue(eval instanceof Evaluator.AttributeWithValueStarting);

        eval = QueryParser.parse("[href$=.png]");
        Assert.assertTrue(eval instanceof Evaluator.AttributeWithValueEnding);

        eval = QueryParser.parse("[href*=test]");
        Assert.assertTrue(eval instanceof Evaluator.AttributeWithValueContaining);

        eval = QueryParser.parse("[href~=.*test.*]");
        Assert.assertTrue(eval instanceof Evaluator.AttributeWithValueMatching);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidAttributeOperator() {
        QueryParser.parse("[href?invalid]");
    }

    @Test
    public void testCombinators() {
        Evaluator eval = QueryParser.parse("div > p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse("div p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse("div + p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse("div ~ p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse("div, p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.Or);

        eval = QueryParser.parse("div, p > span");
        Assert.assertTrue(eval instanceof CombiningEvaluator.Or);

        eval = QueryParser.parse("a, b, c");
        Assert.assertTrue(eval instanceof CombiningEvaluator.Or);
    }

    @Test
    public void testLeadingCombinators() {
        Evaluator eval = QueryParser.parse("> p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse("+ p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse("~ p");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse(" p");
        Assert.assertTrue(eval instanceof Evaluator.Tag);
    }

    @Test
    public void testStructuralAndIndexPseudoSelectors() {
        Assert.assertTrue(QueryParser.parse(":lt(3)") instanceof Evaluator.IndexLessThan);
        Assert.assertTrue(QueryParser.parse(":gt(1)") instanceof Evaluator.IndexGreaterThan);
        Assert.assertTrue(QueryParser.parse(":eq(2)") instanceof Evaluator.IndexEquals);
        Assert.assertTrue(QueryParser.parse(":first-child") instanceof Evaluator.IsFirstChild);
        Assert.assertTrue(QueryParser.parse(":last-child") instanceof Evaluator.IsLastChild);
        Assert.assertTrue(QueryParser.parse(":first-of-type") instanceof Evaluator.IsFirstOfType);
        Assert.assertTrue(QueryParser.parse(":last-of-type") instanceof Evaluator.IsLastOfType);
        Assert.assertTrue(QueryParser.parse(":only-child") instanceof Evaluator.IsOnlyChild);
        Assert.assertTrue(QueryParser.parse(":only-of-type") instanceof Evaluator.IsOnlyOfType);
        Assert.assertTrue(QueryParser.parse(":empty") instanceof Evaluator.IsEmpty);
        Assert.assertTrue(QueryParser.parse(":root") instanceof Evaluator.IsRoot);
    }

    @Test
    public void testNthSelectors() {
        Assert.assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(+2n+1)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(2n-1)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(2n)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(n)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(5)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(+5)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(-5)") instanceof Evaluator.IsNthChild);

        Assert.assertTrue(QueryParser.parse(":nth-last-child(2)") instanceof Evaluator.IsNthLastChild);
        Assert.assertTrue(QueryParser.parse(":nth-of-type(2)") instanceof Evaluator.IsNthOfType);
        Assert.assertTrue(QueryParser.parse(":nth-last-of-type(2)") instanceof Evaluator.IsNthLastOfType);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidNthChild() {
        QueryParser.parse(":nth-child(invalid)");
    }

    @Test
    public void testTextAndRegexPseudoSelectors() {
        Assert.assertTrue(QueryParser.parse(":contains(text)") instanceof Evaluator.ContainsText);
        Assert.assertTrue(QueryParser.parse(":containsOwn(text)") instanceof Evaluator.ContainsOwnText);
        Assert.assertTrue(QueryParser.parse(":containsData(data)") instanceof Evaluator.ContainsData);
        Assert.assertTrue(QueryParser.parse(":matches(.*)") instanceof Evaluator.Matches);
        Assert.assertTrue(QueryParser.parse(":matchesOwn(.*)") instanceof Evaluator.MatchesOwn);
        Assert.assertTrue(QueryParser.parse(":has(div)") instanceof StructuralEvaluator.Has);
        Assert.assertTrue(QueryParser.parse(":not(div)") instanceof StructuralEvaluator.Not);
    }

    @Test
    public void testSubQueryInBracketsAndParentheses() {
        Evaluator eval = QueryParser.parse("div[data-val=(test)]");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);

        eval = QueryParser.parse("div:has([data-val=test])");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test
    public void testChainedElementSelector() {
        Evaluator eval = QueryParser.parse("div.content#main[name=test]");
        Assert.assertTrue(eval instanceof CombiningEvaluator.And);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testUnhandledSelector() {
        QueryParser.parse("div:unknown-pseudo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyHasSubselect() {
        QueryParser.parse(":has()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyNotSubselect() {
        QueryParser.parse(":not()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContains() {
        QueryParser.parse(":contains()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyContainsData() {
        QueryParser.parse(":containsData()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyMatches() {
        QueryParser.parse(":matches()");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonNumericIndex() {
        QueryParser.parse(":eq(abc)");
    }

    @Test
    public void testExecutionWithDocument() {
        Document doc = Jsoup.parse("<div id='id1' class='cl1'><p class='cl2'><span>Text</span></p><p>Data</p></div>");
        Element el = doc.select(QueryParser.parse("div#id1.cl1 > p.cl2 span")).first();
        Assert.assertNotNull(el);
        Assert.assertEquals("Text", el.text());
    }
}
