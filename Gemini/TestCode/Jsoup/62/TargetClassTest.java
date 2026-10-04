package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testEnumValuesAndValueOf() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        assertTrue(states.length > 0);
        assertEquals(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
    }

    @Test
    public void testForeignContent() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div></div>", "", ParseErrorList.tracking(10), ParseSettings.preserveCase);
        Token.Comment comment = new Token.Comment();
        comment.getData().append("test");
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(comment, tb));
    }

    @Test
    public void testInitialStateAndDoctype() {
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().name());

        String quirksHtml = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><body></body></html>";
        Document quirksDoc = Jsoup.parse(quirksHtml);
        assertNotNull(quirksDoc.documentType());

        String wsBeforeDoc = "   <!-- comment --> <!DOCTYPE html><html></html>";
        Document wsDoc = Jsoup.parse(wsBeforeDoc);
        assertEquals("html", wsDoc.documentType().name());
    }

    @Test
    public void testBeforeHtml() {
        String html = "<!-- c -->\n<html><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("html", doc.child(0).nodeName());

        Parser parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<!DOCTYPE html><html><!DOCTYPE html></html>", "");
        assertTrue(parser.getErrors().size() > 0);

        Document docEndTag = Jsoup.parse("</head><p>text</p>");
        assertEquals("text", docEndTag.select("p").text());

        Document docInvalidEnd = Jsoup.parse("</div><p>text</p>");
        assertEquals("text", docInvalidEnd.select("p").text());
    }

    @Test
    public void testBeforeHeadAndInHead() {
        String html = "<html><!-- c -->\n<head><title>Test</title><base href=\"http://example.com/\"><meta charset=\"utf-8\"><link rel=\"stylesheet\" href=\"style.css\"><style>body{color:red;}</style><script>var x=1;</script><noscript><link rel=\"stylesheet\" href=\"ns.css\"></noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html, "http://base.com/");
        assertEquals("Test", doc.title());
        assertEquals("http://example.com/", doc.baseUri());

        Parser parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<html><head><!DOCTYPE html><head></head></head><body></body></html>", "");
        assertTrue(parser.getErrors().size() > 0);

        Document docHtmlStartInHead = Jsoup.parse("<html><head><html class=\"foo\"></head><body></body></html>");
        assertEquals("foo", docHtmlStartInHead.select("html").attr("class"));

        Document docHeadEndTags = Jsoup.parse("<html><head></br>Text");
        assertEquals("Text", docHeadEndTags.body().text());

        Document docHeadBadEnd = Jsoup.parse("<html><head></span>Text");
        assertEquals("Text", docHeadBadEnd.body().text());

        Document docBasefont = Jsoup.parse("<html><head><basefont><bgsound><command></head><body></body></html>");
        assertEquals(0, docBasefont.select("basefont").size());
    }

    @Test
    public void testInHeadNoscript() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<html><head><noscript><!DOCTYPE html><style>body{}</style><!-- comment --> <link rel=\"stylesheet\"><meta name=\"foo\"><noscript></noscript></noscript></head><body></body></html>", "");
        assertNotNull(doc);

        Document docNoscriptBr = Jsoup.parse("<html><head><noscript></br><p>test</p></noscript></head><body></body></html>");
        assertEquals("test", docNoscriptBr.select("p").text());

        Document docNoscriptBad = Jsoup.parse("<html><head><noscript><head><div>foo</div></noscript></head><body></body></html>");
        assertTrue(docNoscriptBad.text().contains("foo"));
    }

    @Test
    public void testAfterHead() {
        String html = "<html><head></head><!-- c -->\n<body><p>Hello</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Hello", doc.select("p").text());

        Document docFrameset = Jsoup.parse("<html><head></head><frameset cols=\"25%,75%\"><frame src=\"frame.html\"></frameset></html>");
        assertNotNull(docFrameset.select("frameset").first());

        Document docHeadInAfterHead = Jsoup.parse("<html><head></head><meta name=\"test\" content=\"val\"><body></body></html>");
        assertEquals("val", docHeadInAfterHead.select("meta").attr("content"));

        Parser parser = Parser.htmlParser().setTrackErrors(10);
        parser.parseInput("<html><head></head><!DOCTYPE html><head></body></html>", "");
        assertTrue(parser.getErrors().size() > 0);

        Document docEnd = Jsoup.parse("<html><head></head></body><p>Text</p></html>");
        assertEquals("Text", docEnd.text());
    }

    @Test
    public void testInBodyTagsAndFormatting() {
        String html = "<body><a href=\"1\">1<a href=\"2\">2</a></a><b>bold<i>italic</b>under</i><p>P1<p>P2<span>Span</span><hr><br><img src=\"img.png\"><input type=\"text\"><input type=\"hidden\"><wbr></body>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("a").size());
        assertEquals("bolditalic", doc.select("b").text());
        assertEquals(2, doc.select("p").size());
        assertEquals("Span", doc.select("span").text());
        assertNotNull(doc.select("hr").first());
        assertNotNull(doc.select("img").first());
        assertEquals(2, doc.select("input").size());

        Document docNullChar = Jsoup.parse("<p>Hello\u0000World</p>");
        assertTrue(docNullChar.select("p").text().contains("Hello"));

        Document docBodyMerge = Jsoup.parse("<body class=\"b1\"><body class=\"b2\" id=\"bodyId\">Text</body>");
        assertEquals("b1", docBodyMerge.body().className());
        assertEquals("bodyId", docBodyMerge.body().id());

        Document docHtmlMerge = Jsoup.parse("<html class=\"h1\"><html class=\"h2\" id=\"htmlId\">Text</html>");
        assertEquals("h1", docHtmlMerge.select("html").attr("class"));
        assertEquals("htmlId", docHtmlMerge.select("html").attr("id"));
    }

    @Test
    public void testInBodyHeadingsAndLists() {
        String html = "<div><h1>H1<h2>H2</h2></h1><pre>Pre\nText</pre><listing>List</listing><ul><li>Item 1<li>Item 2</ul><ol><li>O1</ol><dl><dt>Dt1<dd>Dd1<dt>Dt2<dd>Dd2</dl></div>";
        Document doc = Jsoup.parse(html);
        assertEquals("H1", doc.select("h1").text());
        assertEquals("H2", doc.select("h2").text());
        assertEquals(2, doc.select("ul > li").size());
        assertEquals(1, doc.select("ol > li").size());
        assertEquals(2, doc.select("dt").size());
        assertEquals(2, doc.select("dd").size());

        Document docNestedHeadings = Jsoup.parse("<h3>Heading 3<h4>Heading 4</h3>");
        assertEquals("Heading 3", docNestedHeadings.select("h3").text());
        assertEquals("Heading 4", docNestedHeadings.select("h4").text());
    }

    @Test
    public void testInBodyFormsAndButtons() {
        String html = "<form id=\"f1\"><p><input name=\"i1\"></p><button>Btn1<button>Btn2</button></button><textarea>text</textarea><keygen><select><option>1<optgroup label=\"g\"><option>2</optgroup></select></form><form id=\"f2\"></form>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("#f1").size());
        assertEquals(1, doc.select("textarea").size());
        assertEquals(2, doc.select("option").size());
        assertEquals(1, doc.select("optgroup").size());

        Document docIsindex = Jsoup.parse("<isindex action=\"/search\" prompt=\"Search:\">");
        assertNotNull(docIsindex.select("form").first());
        assertNotNull(docIsindex.select("input[name=isindex]").first());
    }

    @Test
    public void testInBodySpecialElements() {
        String html = "<nobr>Nobr 1<nobr>Nobr 2</nobr></nobr><applet><p>Applet</p></applet><marquee>Marquee</marquee><object>Object</object><ruby>Ruby <rp>(</rp><rt>rt</rt><rp>)</rp></ruby><math><mi>x</mi></math><svg><image href=\"foo.png\"></svg><image src=\"bar.png\"><iframe>Frame</iframe><noembed>NoEmbed</noembed><xmp>Xmp<b>not bold</b></xmp><plaintext>Plain<b>not bold</b>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("nobr").size());
        assertEquals(1, doc.select("applet").size());
        assertEquals(1, doc.select("ruby").size());
        assertEquals(1, doc.select("math").size());
        assertEquals(1, doc.select("svg").size());
        assertEquals(1, doc.select("img").size());
        assertEquals(1, doc.select("xmp").size());
    }

    @Test
    public void testInBodyAdoptionAgencyDeep() {
        String html = "<b>1<p>2<b>3<i>4</b>5</i>6</p>7</b>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("p").first());
        assertTrue(doc.select("i").size() > 0);

        String tableAdoption = "<b>1<table><tr><td>2</b>3</td></tr></table>";
        Document tableDoc = Jsoup.parse(tableAdoption);
        assertNotNull(tableDoc.select("table").first());

        String deepAdoption = "<a>1<b>2<i>3<em>4<strong>5<small>6<strike>7<tt>8<p>9</p>10</tt></strike></small></strong></em></i></b></a>";
        Document deepDoc = Jsoup.parse(deepAdoption);
        assertEquals(1, deepDoc.select("p").size());
    }

    @Test
    public void testInBodyEndTagsHandling() {
        String html = "</p><p>Line 1</p></span></li></dd></dt></h1></h2></h3></h4></h5></h6></form></body></html></br><sarcasm>test</sarcasm>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.text().contains("Line 1"));
        assertTrue(doc.text().contains("test"));

        Document docAppletEnd = Jsoup.parse("<applet>Test</applet>");
        assertEquals("Test", docAppletEnd.select("applet").text());

        Document docButtonScope = Jsoup.parse("<p><button>Btn</button></p>");
        assertEquals("Btn", docButtonScope.select("button").text());

        Document docSpanEnd = Jsoup.parse("<span>Span 1</span>");
        assertEquals("Span 1", docSpanEnd.select("span").text());
    }

    @Test
    public void testInTableStates() {
        String html = "<table><!-- c --><caption>Cap</caption><colgroup><col width=\"10\"></colgroup><thead><tr><th>H1</th></tr></thead><tbody><tr><td>D1</td></tr></tbody><tfoot><tr><td>F1</td></tr></tfoot></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Cap", doc.select("caption").text());
        assertEquals("H1", doc.select("th").text());
        assertEquals(2, doc.select("td").size());

        Document docTableTextFoster = Jsoup.parse("<table>   FOO   <tr><td>Cell</td></tr></table>");
        assertTrue(docTableTextFoster.body().text().contains("FOO"));
        assertEquals("Cell", docTableTextFoster.select("td").text());

        Document docTableTags = Jsoup.parse("<table><input type=\"hidden\" name=\"sec\"><input type=\"text\" name=\"txt\"><form id=\"f\"><tr><td>1</td></tr></form></table>");
        assertEquals(1, docTableTags.select("input[type=hidden]").size());
        assertEquals(1, docTableTags.select("input[type=text]").size());

        Document docNestedTable = Jsoup.parse("<table><tr><td><table><tr><td>Nested</td></tr></table></td></tr></table>");
        assertEquals("Nested", docNestedTable.select("table table td").text());

        Document docTableCol = Jsoup.parse("<table><col><tbody><tr><td>Col</td></tr></tbody></table>");
        assertEquals("Col", docTableCol.select("td").text());

        Document docTableDirectTd = Jsoup.parse("<table><td>Direct</td></table>");
        assertEquals("Direct", docTableDirectTd.select("td").text());
    }

    @Test
    public void testInCaptionAndColumnGroup() {
        String html = "<table><caption>Caption Text<p>In Cap</p></caption><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Caption Text In Cap", doc.select("caption").text());

        Document docCapTableEnd = Jsoup.parse("<table><caption>Cap<table><tr><td>Nested</td></tr></table></caption></table>");
        assertNotNull(docCapTableEnd.select("caption").first());

        Document docColgroup = Jsoup.parse("<table><colgroup><!-- c --> <col><col></colgroup><tr><td>A</td></tr></table>");
        assertEquals(2, docColgroup.select("col").size());

        Document docColgroupEnd = Jsoup.parse("<table><colgroup><col></colgroup><col><tr><td>B</td></tr></table>");
        assertNotNull(docColgroupEnd.select("colgroup").first());
    }

    @Test
    public void testInTableBodyAndInRowAndInCell() {
        String html = "<table><tr><td>Cell 1<th>Cell 2</td><tr><td>Cell 3</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("tr").size());
        assertEquals(2, doc.select("td").size());
        assertEquals(1, doc.select("th").size());

        Document docTableBodyEnd = Jsoup.parse("<table><tbody><tr><td>A</td></tbody><tfoot><tr><td>B</td></tfoot></table>");
        assertEquals(1, docTableBodyEnd.select("tbody").size());
        assertEquals(1, docTableBodyEnd.select("tfoot").size());

        Document docCellClose = Jsoup.parse("<table><tr><td>1<td>2<tr><th>3<th>4</table>");
        assertEquals(2, docCellClose.select("td").size());
        assertEquals(2, docCellClose.select("th").size());

        Document docCellTableInside = Jsoup.parse("<table><tr><td>1<table><tr><td>2</td></tr></table></td></tr></table>");
        assertEquals(2, docCellTableInside.select("table").size());

        Document docCellInvalidEnd = Jsoup.parse("<table><tr><td>1</body></head></td></tr></table>");
        assertEquals("1", docCellInvalidEnd.select("td").text());
    }

    @Test
    public void testInSelectStates() {
        String html = "<select><option value=\"1\">One<option value=\"2\">Two<optgroup label=\"g\"><option value=\"3\">Three</optgroup></select>";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.select("option").size());

        Document docSelectInTable = Jsoup.parse("<table><tr><td><select><option>1</option><tr><td>Next</td></tr></select></td></tr></table>");
        assertNotNull(docSelectInTable.select("select").first());
        assertEquals("Next", docSelectInTable.select("td").last().text());

        Document docSelectKeygen = Jsoup.parse("<select><input><keygen><textarea></select>");
        assertNotNull(docSelectKeygen);

        Document docSelectScript = Jsoup.parse("<select><script>var x = 1;</script><option>A</option></select>");
        assertEquals("A", docSelectScript.select("option").text());

        Document docSelectNullChar = Jsoup.parse("<select><option>A\u0000B</option></select>");
        assertTrue(docSelectNullChar.select("option").text().contains("A"));
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        String html = "<html><head></head><body><p>Text</p></body><!-- comment -->\n</html><!-- after comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Text", doc.select("p").text());

        Document docExtraHtml = Jsoup.parse("<html><body></body></html><html class=\"extra\">");
        assertNotNull(docExtraHtml);

        Document docTextAfterBody = Jsoup.parse("<html><body></body></html>TextAfter");
        assertTrue(docTextAfterBody.body().text().contains("TextAfter"));

        Document docDoctypeAfterBody = Jsoup.parse("<html><body></body></html><!DOCTYPE html>");
        assertNotNull(docDoctypeAfterBody);
    }

    @Test
    public void testFramesetStates() {
        String html = "<html><frameset rows=\"50%,50%\"><!-- comment -->\n<frame src=\"frame1.html\"><frame src=\"frame2.html\"><noframes><p>No frames</p></noframes></frameset><!-- after --></html><!-- after-after -->";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("frame").size());
        assertEquals("No frames", doc.select("noframes p").text());

        Document docNestedFrameset = Jsoup.parse("<frameset><frameset><frame></frameset></frameset>");
        assertEquals(2, docNestedFrameset.select("frameset").size());

        Document docFramesetAfterAfter = Jsoup.parse("<html><frameset><frame></frameset></html><!-- comment --> <noframes></noframes>");
        assertNotNull(docFramesetAfterAfter);
    }

    @Test
    public void testFragmentParsing() {
        List<Element> nodes = Parser.parseFragment("<div><p>Paragraph 1</p><p>Paragraph 2</p></div>", new Element(Tag.valueOf("body"), ""), "");
        assertEquals(1, nodes.size());
        assertEquals(2, nodes.get(0).select("p").size());

        List<Element> trNodes = Parser.parseFragment("<tr><td>Cell</td></tr>", new Element(Tag.valueOf("tbody"), ""), "");
        assertEquals(1, trNodes.size());
        assertEquals("Cell", trNodes.get(0).select("td").text());

        List<Element> tdNodes = Parser.parseFragment("<td>Cell 1</td><td>Cell 2</td>", new Element(Tag.valueOf("tr"), ""), "");
        assertEquals(2, tdNodes.size());

        List<Element> optNodes = Parser.parseFragment("<option>1</option><option>2</option>", new Element(Tag.valueOf("select"), ""), "");
        assertEquals(2, optNodes.size());
    }

    @Test
    public void testTextStateEofAndEndTags() {
        Document docTitle = Jsoup.parse("<title>Title text");
        assertEquals("Title text", docTitle.title());

        Document docStyle = Jsoup.parse("<style>body { color: blue;");
        assertTrue(docStyle.select("style").data().contains("color: blue"));

        Document docTextarea = Jsoup.parse("<textarea>Unclosed textarea");
        assertEquals("Unclosed textarea", docTextarea.select("textarea").text());
    }
}
