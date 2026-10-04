package org.jsoup.select;

import org.junit.Assert;
import org.junit.Test;

public class QueryParserTest {

    @Test
    public void testSimpleTag() {
        Evaluator eval = QueryParser.parse("div");
        Assert.assertTrue(eval instanceof Evaluator.Tag);
        Assert.assertEquals("div", eval.toString());
    }

    @Test
    public void testId() {
        Evaluator eval = QueryParser.parse("#main");
        Assert.assertTrue(eval instanceof Evaluator.Id);
    }

    @Test
    public void testClass() {
        Evaluator eval = QueryParser.parse(".highlight");
        Assert.assertTrue(eval instanceof Evaluator.Class);
    }

    @Test
    public void testAllElements() {
        Evaluator eval = QueryParser.parse("*");
        Assert.assertTrue(eval instanceof Evaluator.AllElements);
    }

    @Test
    public void testNamespaceTag() {
        Evaluator eval = QueryParser.parse("fb|like");
        Assert.assertTrue(eval instanceof Evaluator.Tag);
        Assert.assertEquals("fb:like", eval.toString());

        Evaluator wildNs = QueryParser.parse("*|custom");
        Assert.assertTrue(wildNs instanceof CombiningEvaluator.Or);
    }

    @Test
    public void testAttributes() {
        Evaluator eval1 = QueryParser.parse("[href]");
        Assert.assertTrue(eval1 instanceof Evaluator.Attribute);

        Evaluator eval2 = QueryParser.parse("[^data-]");
        Assert.assertTrue(eval2 instanceof Evaluator.AttributeStarting);

        Evaluator eval3 = QueryParser.parse("[type=text]");
        Assert.assertTrue(eval3 instanceof Evaluator.AttributeWithValue);

        Evaluator eval4 = QueryParser.parse("[type!=text]");
        Assert.assertTrue(eval4 instanceof Evaluator.AttributeWithValueNot);

        Evaluator eval5 = QueryParser.parse("[href^=https]");
        Assert.assertTrue(eval5 instanceof Evaluator.AttributeWithValueStarting);

        Evaluator eval6 = QueryParser.parse("[href$=.png]");
        Assert.assertTrue(eval6 instanceof Evaluator.AttributeWithValueEnding);

        Evaluator eval7 = QueryParser.parse("[class*=active]");
        Assert.assertTrue(eval7 instanceof Evaluator.AttributeWithValueContaining);

        Evaluator eval8 = QueryParser.parse("[href~=https?://.+]");
        Assert.assertTrue(eval8 instanceof Evaluator.AttributeWithValueMatching);
    }

    @Test
    public void testHierarchyCombinators() {
        Evaluator child = QueryParser.parse("div > p");
        Assert.assertTrue(child instanceof CombiningEvaluator.And);

        Evaluator descendant = QueryParser.parse("div p");
        Assert.assertTrue(descendant instanceof CombiningEvaluator.And);

        Evaluator nextSibling = QueryParser.parse("h1 + p");
        Assert.assertTrue(nextSibling instanceof CombiningEvaluator.And);

        Evaluator anySibling = QueryParser.parse("h1 ~ p");
        Assert.assertTrue(anySibling instanceof CombiningEvaluator.And);

        Evaluator orEval = QueryParser.parse("h1, p, span");
        Assert.assertTrue(orEval instanceof CombiningEvaluator.Or);
    }

    @Test
    public void testLeadingCombinators() {
        Evaluator rootChild = QueryParser.parse("> p");
        Assert.assertTrue(rootChild instanceof CombiningEvaluator.And);

        Evaluator rootSibling = QueryParser.parse("+ p");
        Assert.assertTrue(rootSibling instanceof CombiningEvaluator.And);

        Evaluator rootGeneralSibling = QueryParser.parse("~ p");
        Assert.assertTrue(rootGeneralSibling instanceof CombiningEvaluator.And);
    }

    @Test
    public void testOrCombinatorPrecedence() {
        Evaluator mixed = QueryParser.parse("a, b > c");
        Assert.assertTrue(mixed instanceof CombiningEvaluator.Or);

        Evaluator mixedAnd = QueryParser.parse("div.cls > p");
        Assert.assertTrue(mixedAnd instanceof CombiningEvaluator.And);
    }

    @Test
    public void testIndexEvaluators() {
        Evaluator lt = QueryParser.parse(":lt(3)");
        Assert.assertTrue(lt instanceof Evaluator.IndexLessThan);

        Evaluator gt = QueryParser.parse(":gt(1)");
        Assert.assertTrue(gt instanceof Evaluator.IndexGreaterThan);

        Evaluator eq = QueryParser.parse(":eq(0)");
        Assert.assertTrue(eq instanceof Evaluator.IndexEquals);
    }

    @Test
    public void testStructuralAndPseudos() {
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
    public void testNthEvaluators() {
        Assert.assertTrue(QueryParser.parse(":nth-child(odd)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(even)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(2n+1)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(+2n-1)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(2n)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(3)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(+3)") instanceof Evaluator.IsNthChild);
        Assert.assertTrue(QueryParser.parse(":nth-child(-3)") instanceof Evaluator.IsNthChild);

        Assert.assertTrue(QueryParser.parse(":nth-last-child(2)") instanceof Evaluator.IsNthLastChild);
        Assert.assertTrue(QueryParser.parse(":nth-of-type(2n+1)") instanceof Evaluator.IsNthOfType);
        Assert.assertTrue(QueryParser.parse(":nth-last-of-type(2n+1)") instanceof Evaluator.IsNthLastOfType);
    }

    @Test
    public void testFunctionalPseudos() {
        Assert.assertTrue(QueryParser.parse(":has(p > a)") instanceof StructuralEvaluator.Has);
        Assert.assertTrue(QueryParser.parse(":contains(hello)") instanceof Evaluator.ContainsText);
        Assert.assertTrue(QueryParser.parse(":containsOwn(hello)") instanceof Evaluator.ContainsOwnText);
        Assert.assertTrue(QueryParser.parse(":containsData(var x)") instanceof Evaluator.ContainsData);
        Assert.assertTrue(QueryParser.parse(":matches(\\d+)") instanceof Evaluator.Matches);
        Assert.assertTrue(QueryParser.parse(":matchesOwn(\\d+)") instanceof Evaluator.MatchesOwn);
        Assert.assertTrue(QueryParser.parse(":not(.hidden)") instanceof StructuralEvaluator.Not);
    }

    @Test
    public void testComplexSelector() {
        Evaluator eval = QueryParser.parse("div#main.content[data-active=true] > p:first-child:not(.old)");
        Assert.assertNotNull(eval);
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyQueryThrows() {
        QueryParser.parse("");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidPseudoThrows() {
        QueryParser.parse(":unknown-pseudo");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyIdThrows() {
        QueryParser.parse("#");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyClassThrows() {
        QueryParser.parse(".");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyHasThrows() {
        QueryParser.parse(":has()");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyContainsThrows() {
        QueryParser.parse(":contains()");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyContainsDataThrows() {
        QueryParser.parse(":containsData()");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyMatchesThrows() {
        QueryParser.parse(":matches()");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testEmptyNotThrows() {
        QueryParser.parse(":not()");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidIndexThrows() {
        QueryParser.parse(":eq(abc)");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testInvalidNthThrows() {
        QueryParser.parse(":nth-child(foo)");
    }

    @Test(expected = Selector.SelectorParseException.class)
    public void testMalformedAttributeThrows() {
        QueryParser.parse("[href?foo]");
    }
}
