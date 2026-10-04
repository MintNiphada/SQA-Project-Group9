package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testEnumValuesAndValueOf() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        Assert.assertTrue(states.length > 0);
        Assert.assertEquals(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
    }

    @Test
    public void testInitialState() {
        String html = "<!DOCTYPE html><html><head></head><body><!-- comment --></body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc.documentType());
        Assert.assertEquals("html", doc.documentType().name());

        Document quirksDoc = Jsoup.parse("<!DOCTYPE html foo \"bar\"><html></html>");
        Assert.assertNotNull(quirksDoc);

        Document textBeforeDoc = Jsoup.parse("   <!-- c --><html></html>");
        Assert.assertEquals(1, textBeforeDoc.children().size());

        Document rawTextDoc = Jsoup.parse("Hello world");
        Assert.assertEquals("Hello world", rawTextDoc.body().text());
    }

    @Test
    public void testBeforeHtmlAndBeforeHead() {
        Document doc1 = Jsoup.parse("<html><head><title>Test</title></head><body></body></html>");
        Assert.assertEquals("Test", doc1.title());

        Document doc2 = Jsoup.parse("</head><title>Test2</title>");
        Assert.assertEquals("Test2", doc2.title());

        Document doc3 = Jsoup.parse("<!DOCTYPE html><html><!-- comment -->   <head></head></html>");
        Assert.assertEquals(0, doc3.head().children().size());

        Document doc4 = Jsoup.parse("<div>Content</div>");
        Assert.assertEquals("div", doc4.body().child(0).tagName());

        Document doc5 = Jsoup.parse("<!DOCTYPE html><div>Foo</div>");
        Assert.assertEquals("Foo", doc5.body().text());
    }

    @Test
    public void testInHeadElements() {
        String html = "<!DOCTYPE html><head><base href='http://example.com/'><basefont><bgsound><link rel='stylesheet'><meta charset='utf-8'><title>Title</title><style>body{}</style><noscript><link rel='stylesheet'></noscript><script>var a = 1;</script></head><body></body>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Title", doc.title());
        Assert.assertEquals("http://example.com/", doc.baseUri());
        Assert.assertTrue(doc.head().select("style").size() > 0);
        Assert.assertTrue(doc.head().select("script").size() > 0);

        Document docNoFrames = Jsoup.parse("<head><noframes><p>No frames</p></noframes></head>");
        Assert.assertNotNull(docNoFrames.head());

        Document docInvalidHead = Jsoup.parse("<head><head></head><body></body>");
        Assert.assertNotNull(docInvalidHead.body());

        Document docBadEnd = Jsoup.parse("<head></badtag><title>Hi</title></head>");
        Assert.assertEquals("Hi", docBadEnd.title());
    }

    @Test
    public void testInHeadNoscript() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new java.io.StringReader("<noscript><!-- c --><link><meta><style></noscript>"), "", new ParseErrorList(10, 10), ParseSettings.htmlDefault);
        tb.runParser();
        Document doc = tb.getDocument();
        Assert.assertNotNull(doc);

        Document doc2 = Jsoup.parse("<head><noscript><head><noscript>Foo</noscript></noscript></head>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<head><noscript><br></noscript></head>");
        Assert.assertNotNull(doc3);
    }

    @Test
    public void testAfterHead() {
        Document doc1 = Jsoup.parse("<html><head></head>   <!-- c --><body>Hello</body></html>");
        Assert.assertEquals("Hello", doc1.body().text());

        Document doc2 = Jsoup.parse("<html><head></head><frameset cols='20%'><frame src='a.html'></frameset></html>");
        Assert.assertNotNull(doc2.select("frameset"));

        Document doc3 = Jsoup.parse("<html><head></head><meta name='foo' content='bar'><body></body></html>");
        Assert.assertEquals(1, doc3.head().select("meta").size());

        Document doc4 = Jsoup.parse("<html><head></head><head><body></body></html>");
        Assert.assertNotNull(doc4.body());

        Document doc5 = Jsoup.parse("<html><head></head></bad><body>Body</body></html>");
        Assert.assertEquals("Body", doc5.body().text());
    }

    @Test
    public void testInBodyFormattingAndAdoptionAgency() {
        Document doc1 = Jsoup.parse("<a>1<p>2<a>3</a>4</p>5</a>");
        Assert.assertEquals(5, doc1.body().textNodes().size() + doc1.body().select("p, a").text().length() > 0 ? 1 : 0);

        Document doc2 = Jsoup.parse("<b>1<p>2</b>3</p>");
        Assert.assertEquals("1", doc2.select("b").first().text());

        Document doc3 = Jsoup.parse("<b><i><p>text</p></i></b>");
        Assert.assertEquals("text", doc3.select("p").first().text());

        Document doc4 = Jsoup.parse("<nobr>1<nobr>2</nobr>3</nobr>");
        Assert.assertNotNull(doc4);

        Document doc5 = Jsoup.parse("<span>1<span>2</span>3</span>");
        Assert.assertEquals("123", doc5.body().text());
    }

    @Test
    public void testInBodyTagsAndLists() {
        Document doc1 = Jsoup.parse("<ul><li>1<li>2</ul><ol><li>3</ol><dl><dt>dt<dd>dd</dl>");
        Assert.assertEquals(3, doc1.select("li").size());
        Assert.assertEquals(1, doc1.select("dt").size());
        Assert.assertEquals(1, doc1.select("dd").size());

        Document doc2 = Jsoup.parse("<h1>h1<h2>h2<h3>h3<h4>h4<h5>h5<h6>h6</h1>");
        Assert.assertEquals(6, doc2.select("h1, h2, h3, h4, h5, h6").size());

        Document doc3 = Jsoup.parse("<pre>pre</pre><listing>listing</listing><plaintext>plain<b>notbold</b>");
        Assert.assertEquals(1, doc3.select("pre").size());
        Assert.assertEquals(1, doc3.select("listing").size());
        Assert.assertTrue(doc3.body().html().contains("plain"));

        Document doc4 = Jsoup.parse("<form><button>btn</button><button>btn2</button></form><form>");
        Assert.assertEquals(2, doc4.select("button").size());
    }

    @Test
    public void testInBodyMediaAndSpecials() {
        Document doc = Jsoup.parse("<div><area><br><embed><img><keygen><wbr><param><source><track><hr><image src='a.jpg'><input type='hidden'><input type='text'></div>");
        Assert.assertTrue(doc.select("img").size() > 0);
        Assert.assertEquals(2, doc.select("input").size());
        Assert.assertEquals(1, doc.select("hr").size());

        Document docXmp = Jsoup.parse("<p><xmp>alert('1')</xmp></p><iframe src='foo'></iframe><noembed>noembed</noembed>");
        Assert.assertEquals(1, docXmp.select("xmp").size());
        Assert.assertEquals(1, docXmp.select("iframe").size());

        Document docTextarea = Jsoup.parse("<textarea>hello world</textarea>");
        Assert.assertEquals("hello world", docTextarea.select("textarea").first().text());
    }

    @Test
    public void testInBodyRubyAndSvgMath() {
        Document doc = Jsoup.parse("<ruby>漢 <rp>(</rp><rt>かん</rt><rp>)</rp></ruby><math><mi>x</mi></math><svg><circle></svg><svg><image></svg>");
        Assert.assertEquals(1, doc.select("ruby").size());
        Assert.assertEquals(2, doc.select("rp").size());
        Assert.assertEquals(1, doc.select("math").size());
        Assert.assertEquals(2, doc.select("svg").size());
    }

    @Test
    public void testInBodyAppletsAndIsIndex() {
        Document doc = Jsoup.parse("<applet>applet content</applet><marquee>marquee</marquee><object>obj</object>");
        Assert.assertEquals(1, doc.select("applet").size());
        Assert.assertEquals(1, doc.select("marquee").size());
        Assert.assertEquals(1, doc.select("object").size());

        Document docIsIndex = Jsoup.parse("<isindex action='/search' prompt='Search this site'>");
        Assert.assertEquals(1, docIsIndex.select("form").size());
        Assert.assertEquals(1, docIsIndex.select("input[name=isindex]").size());
    }

    @Test
    public void testInBodyEndTagsHandling() {
        Document doc = Jsoup.parse("</p><p>test</p></address></br></sarcasm></body></html>");
        Assert.assertEquals("test", doc.select("p").first().text());

        Document docLiEnd = Jsoup.parse("<li>item 1</li></li>");
        Assert.assertEquals(1, docLiEnd.select("li").size());

        Document docFormEnd = Jsoup.parse("<form>test</form></form>");
        Assert.assertEquals(1, docFormEnd.select("form").size());

        Document docHeadingsEnd = Jsoup.parse("<h1>heading</h3>");
        Assert.assertEquals(1, docHeadingsEnd.select("h1").size());

        Document docAppletEnd = Jsoup.parse("<object>test</object></object>");
        Assert.assertEquals(1, docAppletEnd.select("object").size());

        Document docSpanEnd = Jsoup.parse("<span>test</span></span>");
        Assert.assertEquals(1, docSpanEnd.select("span").size());
    }

    @Test
    public void testInTableStates() {
        String html = "<table><caption>Cap</caption><colgroup><col></colgroup><thead><tr><th>H1</th></tr></thead><tbody><tr><td>D1</td></tr></tbody><tfoot><tr><td>F1</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Cap", doc.select("caption").text());
        Assert.assertEquals(1, doc.select("colgroup").size());
        Assert.assertEquals(1, doc.select("thead").size());
        Assert.assertEquals(1, doc.select("tbody").size());
        Assert.assertEquals(1, doc.select("tfoot").size());
        Assert.assertEquals(3, doc.select("tr").size());
        Assert.assertEquals("H1", doc.select("th").text());
        Assert.assertEquals(2, doc.select("td").size());

        Document docFoster = Jsoup.parse("<table>Hello<tr><td>World</td></tr></table>");
        Assert.assertTrue(docFoster.body().html().startsWith("Hello"));

        Document docTableExtra = Jsoup.parse("<table><input type='hidden' name='csrf' value='1'><input type='text'><tr><td>Cell</td></tr></table>");
        Assert.assertEquals(2, docTableExtra.select("input").size());

        Document docNestedTable = Jsoup.parse("<table><tr><td><table><tr><td>Nested</td></tr></table></td></tr></table>");
        Assert.assertEquals(2, docNestedTable.select("table").size());

        Document docBadTableEnds = Jsoup.parse("<table></col></colgroup></caption></body></html>");
        Assert.assertEquals(1, docBadTableEnds.select("table").size());
    }

    @Test
    public void testInSelectStates() {
        String html = "<select><optgroup label='group'><option value='1'>One</option><option value='2'>Two</option></optgroup></select>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("select").size());
        Assert.assertEquals(1, doc.select("optgroup").size());
        Assert.assertEquals(2, doc.select("option").size());

        Document docInTable = Jsoup.parse("<table><tr><td><select><option>1</option></select></td></tr></table>");
        Assert.assertEquals(1, docInTable.select("select").size());

        Document docSelectSwitch = Jsoup.parse("<select><input><option>1</option></select>");
        Assert.assertEquals(1, docSelectSwitch.select("select").size());

        Document docBadSelect = Jsoup.parse("<select><script>var x = 1;</script></select>");
        Assert.assertEquals(1, docBadSelect.select("select").size());

        Document docSelectInTableTags = Jsoup.parse("<table><select><td>Cell</td></select></table>");
        Assert.assertNotNull(docSelectInTableTags);
    }

    @Test
    public void testInFramesetStates() {
        String html = "<html><frameset rows='50%,50%'><frame src='frame1.html'><frame src='frame2.html'><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("frameset").size());
        Assert.assertEquals(2, doc.select("frame").size());

        Document doc2 = Jsoup.parse("<html><!-- c1 --><frameset><frame></frameset><!-- c2 --></html><!-- c3 -->");
        Assert.assertEquals(1, doc2.select("frameset").size());

        Document doc3 = Jsoup.parse("<html><frameset><frameset><frame></frameset></frameset></html>");
        Assert.assertEquals(2, doc3.select("frameset").size());
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        Document doc = Jsoup.parse("<html><head></head><body>Hello</body><!-- c --></html><!-- c2 -->   ");
        Assert.assertEquals("Hello", doc.body().text());

        Document docTextAfter = Jsoup.parse("<html><body>Hello</body></html><div>Extra</div>");
        Assert.assertEquals("Hello Extra", docTextAfter.body().text());

        Document docDocTypeAfter = Jsoup.parse("<html><body>Hello</body></html><!DOCTYPE html>");
        Assert.assertEquals("Hello", docDocTypeAfter.body().text());
    }

    @Test
    public void testForeignContentAndNullCharacter() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new java.io.StringReader("\u0000<body>\u0000<table>\u0000<tr>\u0000<td>\u0000<select>\u0000<option>\u0000</option></select></td></tr></table></body>"), "", new ParseErrorList(10, 10), ParseSettings.htmlDefault);
        tb.runParser();
        Document doc = tb.getDocument();
        Assert.assertNotNull(doc);

        Token dummyToken = new Token.Character().data("text");
        boolean result = HtmlTreeBuilderState.ForeignContent.process(dummyToken, tb);
        Assert.assertTrue(result);
    }

    @Test
    public void testDirectStateProcessEdgeCases() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new java.io.StringReader("<div>"), "", new ParseErrorList(10, 10), ParseSettings.htmlDefault);

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.forceQuirks = true;
        boolean dRes = HtmlTreeBuilderState.Initial.process(doctype, tb);
        Assert.assertTrue(dRes);

        Token.Comment comment = new Token.Comment();
        comment.getData().append("comment");
        Assert.assertTrue(HtmlTreeBuilderState.Initial.process(comment, tb));

        Token.Character ws = new Token.Character().data("   ");
        Assert.assertTrue(HtmlTreeBuilderState.Initial.process(ws, tb));

        Token.EOF eof = new Token.EOF();
        Assert.assertTrue(HtmlTreeBuilderState.Text.process(eof, tb));

        Token.EndTag endScript = new Token.EndTag();
        endScript.name("script");
        Assert.assertTrue(HtmlTreeBuilderState.Text.process(endScript, tb));
    }
}
