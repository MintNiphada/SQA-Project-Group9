package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Tag;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class EvaluatorTest {

    @Test
    public void testTag() {
        Evaluator.Tag eval = new Evaluator.Tag("DIV");
        Element el = new Element("div");
        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, new Element("p")));
        Assert.assertEquals("DIV", eval.toString());
    }

    @Test
    public void testTagEndsWith() {
        Evaluator.TagEndsWith eval = new Evaluator.TagEndsWith("iv");
        Assert.assertTrue(eval.matches(null, new Element("div")));
        Assert.assertFalse(eval.matches(null, new Element("span")));
        Assert.assertEquals("iv", eval.toString());
    }

    @Test
    public void testId() {
        Evaluator.Id eval = new Evaluator.Id("testId");
        Element el = new Element("div").attr("id", "testId");
        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, new Element("div").attr("id", "other")));
        Assert.assertEquals("#testId", eval.toString());
    }

    @Test
    public void testClass() {
        Evaluator.Class eval = new Evaluator.Class("btn");
        Element el = new Element("div").attr("class", "btn active");
        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, new Element("div").attr("class", "button active")));
        Assert.assertEquals(".btn", eval.toString());
    }

    @Test
    public void testAttribute() {
        Evaluator.Attribute eval = new Evaluator.Attribute("disabled");
        Element el = new Element("button").attr("disabled", "");
        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, new Element("button")));
        Assert.assertEquals("[disabled]", eval.toString());
    }

    @Test
    public void testAttributeStarting() {
        Evaluator.AttributeStarting eval = new Evaluator.AttributeStarting("data-");
        Element el = new Element("div").attr("data-name", "foo");
        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, new Element("div").attr("aria-name", "foo")));
        Assert.assertEquals("[^data-]", eval.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttributeStartingEmpty() {
        new Evaluator.AttributeStarting("");
    }

    @Test
    public void testAttributeWithValue() {
        Evaluator.AttributeWithValue eval = new Evaluator.AttributeWithValue("key", "val");
        Element el = new Element("div").attr("key", " val ");
        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, new Element("div").attr("key", "other")));
        Assert.assertFalse(eval.matches(null, new Element("div")));
        Assert.assertEquals("[key=val]", eval.toString());
    }

    @Test
    public void testAttributeWithValueQuoted() {
        Evaluator.AttributeWithValue singleQuote = new Evaluator.AttributeWithValue("key", "'val'");
        Evaluator.AttributeWithValue doubleQuote = new Evaluator.AttributeWithValue("key", "\"val\"");
        Element el = new Element("div").attr("key", "val");
        Assert.assertTrue(singleQuote.matches(null, el));
        Assert.assertTrue(doubleQuote.matches(null, el));
    }

    @Test
    public void testAttributeWithValueNot() {
        Evaluator.AttributeWithValueNot eval = new Evaluator.AttributeWithValueNot("key", "val");
        Assert.assertTrue(eval.matches(null, new Element("div").attr("key", "other")));
        Assert.assertTrue(eval.matches(null, new Element("div")));
        Assert.assertFalse(eval.matches(null, new Element("div").attr("key", "val")));
        Assert.assertEquals("[key!=val]", eval.toString());
    }

    @Test
    public void testAttributeWithValueStarting() {
        Evaluator.AttributeWithValueStarting eval = new Evaluator.AttributeWithValueStarting("href", "http");
        Assert.assertTrue(eval.matches(null, new Element("a").attr("href", "HTTP://example.com")));
        Assert.assertFalse(eval.matches(null, new Element("a").attr("href", "ftp://example.com")));
        Assert.assertFalse(eval.matches(null, new Element("a")));
        Assert.assertEquals("[href^=http]", eval.toString());
    }

    @Test
    public void testAttributeWithValueEnding() {
        Evaluator.AttributeWithValueEnding eval = new Evaluator.AttributeWithValueEnding("href", ".png");
        Assert.assertTrue(eval.matches(null, new Element("a").attr("href", "image.PNG")));
        Assert.assertFalse(eval.matches(null, new Element("a").attr("href", "image.jpg")));
        Assert.assertFalse(eval.matches(null, new Element("a")));
        Assert.assertEquals("[href$=.png]", eval.toString());
    }

    @Test
    public void testAttributeWithValueContaining() {
        Evaluator.AttributeWithValueContaining eval = new Evaluator.AttributeWithValueContaining("class", "foo");
        Assert.assertTrue(eval.matches(null, new Element("div").attr("class", "my-FOO-bar")));
        Assert.assertFalse(eval.matches(null, new Element("div").attr("class", "my-bar")));
        Assert.assertFalse(eval.matches(null, new Element("div")));
        Assert.assertEquals("[class*=foo]", eval.toString());
    }

    @Test
    public void testAttributeWithValueMatching() {
        Pattern pattern = Pattern.compile("^[0-9]+$");
        Evaluator.AttributeWithValueMatching eval = new Evaluator.AttributeWithValueMatching("data-id", pattern);
        Assert.assertTrue(eval.matches(null, new Element("div").attr("data-id", "12345")));
        Assert.assertFalse(eval.matches(null, new Element("div").attr("data-id", "abc")));
        Assert.assertFalse(eval.matches(null, new Element("div")));
        Assert.assertEquals("[data-id~=^[0-9]+$]", eval.toString());
    }

    @Test
    public void testAllElements() {
        Evaluator.AllElements eval = new Evaluator.AllElements();
        Assert.assertTrue(eval.matches(null, new Element("div")));
        Assert.assertEquals("*", eval.toString());
    }

    @Test
    public void testIndexLessThan() {
        Document doc = Jsoup.parse("<div><p>0</p><p>1</p><p>2</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);
        Element p2 = doc.select("p").get(2);

        Evaluator.IndexLessThan eval = new Evaluator.IndexLessThan(1);
        Assert.assertTrue(eval.matches(doc, p0));
        Assert.assertFalse(eval.matches(doc, p1));
        Assert.assertFalse(eval.matches(doc, p2));
        Assert.assertFalse(eval.matches(p0, p0));
        Assert.assertEquals(":lt(1)", eval.toString());
    }

    @Test
    public void testIndexGreaterThan() {
        Document doc = Jsoup.parse("<div><p>0</p><p>1</p><p>2</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);
        Element p2 = doc.select("p").get(2);

        Evaluator.IndexGreaterThan eval = new Evaluator.IndexGreaterThan(1);
        Assert.assertFalse(eval.matches(doc, p0));
        Assert.assertFalse(eval.matches(doc, p1));
        Assert.assertTrue(eval.matches(doc, p2));
        Assert.assertEquals(":gt(1)", eval.toString());
    }

    @Test
    public void testIndexEquals() {
        Document doc = Jsoup.parse("<div><p>0</p><p>1</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);

        Evaluator.IndexEquals eval = new Evaluator.IndexEquals(1);
        Assert.assertFalse(eval.matches(doc, p0));
        Assert.assertTrue(eval.matches(doc, p1));
        Assert.assertEquals(":eq(1)", eval.toString());
    }

    @Test
    public void testIsLastChild() {
        Document doc = Jsoup.parse("<div><p>0</p><p>1</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);

        Evaluator.IsLastChild eval = new Evaluator.IsLastChild();
        Assert.assertFalse(eval.matches(null, p0));
        Assert.assertTrue(eval.matches(null, p1));
        Assert.assertFalse(eval.matches(null, doc.body()));
        Assert.assertFalse(eval.matches(null, new Element("p")));
        Assert.assertEquals(":last-child", eval.toString());
    }

    @Test
    public void testIsFirstChild() {
        Document doc = Jsoup.parse("<div><p>0</p><p>1</p></div>");
        Element p0 = doc.select("p").get(0);
        Element p1 = doc.select("p").get(1);

        Evaluator.IsFirstChild eval = new Evaluator.IsFirstChild();
        Assert.assertTrue(eval.matches(null, p0));
        Assert.assertFalse(eval.matches(null, p1));
        Assert.assertFalse(eval.matches(null, doc.body()));
        Assert.assertFalse(eval.matches(null, new Element("p")));
        Assert.assertEquals(":first-child", eval.toString());
    }

    @Test
    public void testIsRoot() {
        Document doc = Jsoup.parse("<html><body><div></div></body></html>");
        Element html = doc.child(0);
        Element body = doc.body();

        Evaluator.IsRoot eval = new Evaluator.IsRoot();
        Assert.assertTrue(eval.matches(doc, html));
        Assert.assertFalse(eval.matches(doc, body));
        Assert.assertTrue(eval.matches(body, body));
        Assert.assertEquals(":root", eval.toString());
    }

    @Test
    public void testIsOnlyChild() {
        Document doc = Jsoup.parse("<div><p>alone</p></div><ul><li>1</li><li>2</li></ul>");
        Element p = doc.select("p").first();
        Element li = doc.select("li").first();

        Evaluator.IsOnlyChild eval = new Evaluator.IsOnlyChild();
        Assert.assertTrue(eval.matches(null, p));
        Assert.assertFalse(eval.matches(null, li));
        Assert.assertFalse(eval.matches(null, doc.body()));
        Assert.assertFalse(eval.matches(null, new Element("span")));
        Assert.assertEquals(":only-child", eval.toString());
    }

    @Test
    public void testIsOnlyOfType() {
        Document doc = Jsoup.parse("<div><p>1</p><span>2</span><p>3</p></div>");
        Element p = doc.select("p").first();
        Element span = doc.select("span").first();

        Evaluator.IsOnlyOfType eval = new Evaluator.IsOnlyOfType();
        Assert.assertFalse(eval.matches(null, p));
        Assert.assertTrue(eval.matches(null, span));
        Assert.assertFalse(eval.matches(null, doc.body()));
        Assert.assertFalse(eval.matches(null, new Element("div")));
        Assert.assertEquals(":only-of-type", eval.toString());
    }

    @Test
    public void testIsEmpty() {
        Evaluator.IsEmpty eval = new Evaluator.IsEmpty();
        Element empty = new Element("div");
        Element withComment = new Element("div").appendChild(new Comment("hi"));
        Element withXmlDecl = new Element("div").appendChild(new XmlDeclaration("xml", false));
        Element withDocType = new Element("div").appendChild(new DocumentType("html", "", ""));
        Element withText = new Element("div").text("text");

        Assert.assertTrue(eval.matches(null, empty));
        Assert.assertTrue(eval.matches(null, withComment));
        Assert.assertTrue(eval.matches(null, withXmlDecl));
        Assert.assertTrue(eval.matches(null, withDocType));
        Assert.assertFalse(eval.matches(null, withText));
        Assert.assertEquals(":empty", eval.toString());
    }

    @Test
    public void testContainsText() {
        Evaluator.ContainsText eval = new Evaluator.ContainsText("TARGET");
        Element el = new Element("div").text("here is the target text");
        Element noMatch = new Element("div").text("other");

        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, noMatch));
        Assert.assertEquals(":contains(target)", eval.toString());
    }

    @Test
    public void testContainsData() {
        Evaluator.ContainsData eval = new Evaluator.ContainsData("SCRIPT_DATA");
        Element el = new Element("script").appendChild(new DataNode("var script_data = 1;"));
        Element noMatch = new Element("script").appendChild(new DataNode("var other = 1;"));

        Assert.assertTrue(eval.matches(null, el));
        Assert.assertFalse(eval.matches(null, noMatch));
        Assert.assertEquals(":containsData(script_data)", eval.toString());
    }

    @Test
    public void testContainsOwnText() {
        Evaluator.ContainsOwnText eval = new Evaluator.ContainsOwnText("OWN");
        Element parent = new Element("div").text("parent own text");
        Element child = new Element("span").text("child own text");
        parent.appendChild(child);

        Assert.assertTrue(eval.matches(null, parent));
        Assert.assertTrue(eval.matches(null, child));
        Assert.assertEquals(":containsOwn(own)", eval.toString());
    }

    @Test
    public void testMatches() {
        Pattern p = Pattern.compile("^\\d{3}$");
        Evaluator.Matches eval = new Evaluator.Matches(p);
        Element el = new Element("div").text("123");
        Element child = new Element("span").text("123");
        Element parent = new Element("div").appendChild(child);
        Element fail = new Element("div").text("1234");

        Assert.assertTrue(eval.matches(null, el));
        Assert.assertTrue(eval.matches(null, parent));
        Assert.assertFalse(eval.matches(null, fail));
        Assert.assertEquals(":matches(^\\d{3}$)", eval.toString());
    }

    @Test
    public void testMatchesOwn() {
        Pattern p = Pattern.compile("^\\d{3}$");
        Evaluator.MatchesOwn eval = new Evaluator.MatchesOwn(p);
        Element child = new Element("span").text("123");
        Element parent = new Element("div").appendChild(child);

        Assert.assertTrue(eval.matches(null, child));
        Assert.assertFalse(eval.matches(null, parent));
        Assert.assertEquals(":matchesOwn(^\\d{3}$)", eval.toString());
    }

    @Test
    public void testIsNthChild() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p><p>3</p><p>4</p></div>");
        Elements ps = doc.select("p");

        Evaluator.IsNthChild evalFixed = new Evaluator.IsNthChild(0, 2);
        Assert.assertFalse(evalFixed.matches(null, ps.get(0)));
        Assert.assertTrue(evalFixed.matches(null, ps.get(1)));
        Assert.assertFalse(evalFixed.matches(null, ps.get(2)));
        Assert.assertEquals(":nth-child(2)", evalFixed.toString());

        Evaluator.IsNthChild evalStep = new Evaluator.IsNthChild(2, 1);
        Assert.assertTrue(evalStep.matches(null, ps.get(0)));
        Assert.assertFalse(evalStep.matches(null, ps.get(1)));
        Assert.assertTrue(evalStep.matches(null, ps.get(2)));
        Assert.assertFalse(evalStep.matches(null, ps.get(3)));
        Assert.assertEquals(":nth-child(2n+1)", evalStep.toString());

        Evaluator.IsNthChild evalStepZeroOffset = new Evaluator.IsNthChild(2, 0);
        Assert.assertFalse(evalStepZeroOffset.matches(null, ps.get(0)));
        Assert.assertTrue(evalStepZeroOffset.matches(null, ps.get(1)));
        Assert.assertEquals(":nth-child(2n)", evalStepZeroOffset.toString());

        Assert.assertFalse(evalFixed.matches(null, doc.body()));
        Assert.assertFalse(evalFixed.matches(null, new Element("p")));
    }

    @Test
    public void testIsNthLastChild() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p><p>3</p></div>");
        Elements ps = doc.select("p");

        Evaluator.IsNthLastChild eval = new Evaluator.IsNthLastChild(0, 1);
        Assert.assertFalse(eval.matches(null, ps.get(0)));
        Assert.assertFalse(eval.matches(null, ps.get(1)));
        Assert.assertTrue(eval.matches(null, ps.get(2)));
        Assert.assertEquals(":nth-last-child(1)", eval.toString());
    }

    @Test
    public void testIsNthOfType() {
        Document doc = Jsoup.parse("<div><p>1</p><span>1</span><p>2</p></div>");
        Elements ps = doc.select("p");

        Evaluator.IsNthOfType eval = new Evaluator.IsNthOfType(0, 2);
        Assert.assertFalse(eval.matches(null, ps.get(0)));
        Assert.assertTrue(eval.matches(null, ps.get(1)));
        Assert.assertEquals(":nth-of-type(2)", eval.toString());
    }

    @Test
    public void testIsNthLastOfType() {
        Document doc = Jsoup.parse("<div><p>1</p><span>1</span><p>2</p></div>");
        Elements ps = doc.select("p");

        Evaluator.IsNthLastOfType eval = new Evaluator.IsNthLastOfType(0, 1);
        Assert.assertFalse(eval.matches(null, ps.get(0)));
        Assert.assertTrue(eval.matches(null, ps.get(1)));
        Assert.assertEquals(":nth-last-of-type(1)", eval.toString());
    }

    @Test
    public void testIsFirstOfType() {
        Document doc = Jsoup.parse("<div><span>0</span><p>1</p><p>2</p></div>");
        Element p1 = doc.select("p").get(0);
        Element p2 = doc.select("p").get(1);

        Evaluator.IsFirstOfType eval = new Evaluator.IsFirstOfType();
        Assert.assertTrue(eval.matches(null, p1));
        Assert.assertFalse(eval.matches(null, p2));
        Assert.assertEquals(":first-of-type", eval.toString());
    }

    @Test
    public void testIsLastOfType() {
        Document doc = Jsoup.parse("<div><p>1</p><p>2</p><span>0</span></div>");
        Element p1 = doc.select("p").get(0);
        Element p2 = doc.select("p").get(1);

        Evaluator.IsLastOfType eval = new Evaluator.IsLastOfType();
        Assert.assertFalse(eval.matches(null, p1));
        Assert.assertTrue(eval.matches(null, p2));
        Assert.assertEquals(":last-of-type", eval.toString());
    }

    @Test
    public void testCssNthEvaluatorNegativeStepAndFormatting() {
        Evaluator.CssNthEvaluator evalNeg = new Evaluator.CssNthEvaluator(-2, 5) {
            @Override
            protected String getPseudoClass() {
                return "custom";
            }
            @Override
            protected int calculatePosition(Element root, Element element) {
                return element.elementSiblingIndex() + 1;
            }
        };

        Document doc = Jsoup.parse("<div><p>1</p><p>2</p><p>3</p><p>4</p><p>5</p><p>6</p></div>");
        Elements ps = doc.select("p");

        Assert.assertTrue(evalNeg.matches(null, ps.get(4)));
        Assert.assertTrue(evalNeg.matches(null, ps.get(2)));
        Assert.assertTrue(evalNeg.matches(null, ps.get(0)));
        Assert.assertFalse(evalNeg.matches(null, ps.get(1)));
        Assert.assertFalse(evalNeg.matches(null, ps.get(5)));
        Assert.assertEquals(":custom(-2n+5)", evalNeg.toString());

        Evaluator.CssNthEvaluator evalSingleArg = new Evaluator.CssNthEvaluator(3) {
            @Override
            protected String getPseudoClass() {
                return "custom";
            }
            @Override
            protected int calculatePosition(Element root, Element element) {
                return element.elementSiblingIndex() + 1;
            }
        };
        Assert.assertEquals(":custom(3)", evalSingleArg.toString());
    }
}
