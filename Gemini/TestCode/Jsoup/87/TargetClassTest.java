package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testConstantsSorted() {
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartToHead);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartPClosers);
        assertSorted(HtmlTreeBuilderState.Constants.Headings);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartPreListing);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartLiBreakers);
        assertSorted(HtmlTreeBuilderState.Constants.DdDt);
        assertSorted(HtmlTreeBuilderState.Constants.Formatters);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartApplets);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartEmptyFormatters);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartMedia);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartInputAttribs);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartOptions);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartRuby);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyStartDrop);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyEndClosers);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyEndAdoptionFormatters);
        assertSorted(HtmlTreeBuilderState.Constants.InBodyEndTableFosters);
        assertSorted(HtmlTreeBuilderState.Constants.InCellNames);
        assertSorted(HtmlTreeBuilderState.Constants.InCellBody);
        assertSorted(HtmlTreeBuilderState.Constants.InCellTable);
        assertSorted(HtmlTreeBuilderState.Constants.InCellCol);
    }

    private void assertSorted(String[] arr) {
        String[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);
        Assert.assertArrayEquals(copy, arr);
    }

    @Test
    public void testInitialStateVariants() {
        Document doc1 = Jsoup.parse("   <!-- comment --> <!DOCTYPE html> <html><head></head><body></body></html>");
        Assert.assertNotNull(doc1);
        Assert.assertEquals(Document.QuirksMode.noQuirks, doc1.quirksMode());

        Document doc2 = Jsoup.parse("<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><body></body></html>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<html><head></head><body>Content without doctype</body></html>");
        Assert.assertEquals(Document.QuirksMode.noQuirks, doc3.quirksMode());

        Document doc4 = Jsoup.parse("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\"><html><body></body></html>");
        Assert.assertNotNull(doc4);
    }

    @Test
    public void testBeforeHtmlTransitions() {
        Document doc = Jsoup.parse(" \t\n <!-- comment --> <html id='test'><head></head><body></body></html>");
        Assert.assertEquals("test", doc.select("html").attr("id"));

        Document doc2 = Jsoup.parse("</head><p>text</p>");
        Assert.assertEquals("text", doc2.select("p").text());

        Document doc3 = Jsoup.parse("</div><p>test</p>");
        Assert.assertEquals("test", doc3.select("p").text());
    }

    @Test
    public void testBeforeHeadTransitions() {
        Document doc = Jsoup.parse("<html> <!-- comm --> \n <head id='h'><title>T</title></head><body></body></html>");
        Assert.assertEquals("h", doc.head().id());

        Document doc2 = Jsoup.parse("<html><html attr='val'><head></head><body></body></html>");
        Assert.assertEquals("val", doc2.select("html").attr("attr"));

        Document doc3 = Jsoup.parse("<html></head><title>Test</title><body></body></html>");
        Assert.assertEquals("Test", doc3.title());

        Document doc4 = Jsoup.parse("<html></wrong><title>Test</title><body></body></html>");
        Assert.assertEquals("Test", doc4.title());
    }

    @Test
    public void testInHeadState() {
        String html = "<head>" +
                "<base href='http://example.com/'>" +
                "<basefont><bgsound><command><link rel='stylesheet' href='a.css'>" +
                "<meta charset='utf-8'>" +
                "<title>Test Title</title>" +
                "<style>body { color: red; }</style>" +
                "<noframes>No frames</noframes>" +
                "<noscript><meta http-equiv='refresh' content='1'></noscript>" +
                "<script>var x = 1;</script>" +
                "</head>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("http://example.com/", doc.baseUri());
        Assert.assertEquals("Test Title", doc.title());
        Assert.assertEquals("body { color: red; }", doc.head().select("style").data());
        Assert.assertEquals("var x = 1;", doc.head().select("script").data());

        Document doc2 = Jsoup.parse("<head><head></head><body></body></html>");
        Assert.assertNotNull(doc2.head());

        Document doc3 = Jsoup.parse("<head></custom><p>Body</p>");
        Assert.assertEquals("Body", doc3.body().text());

        Document doc4 = Jsoup.parse("<head></body><p>Body</p>");
        Assert.assertEquals("Body", doc4.body().text());
    }

    @Test
    public void testInHeadNoscriptState() {
        Document doc = Jsoup.parse("<head><noscript><!-- comm --> <link rel='stylesheet' href='test.css'><meta charset='utf-8'></noscript></head>");
        Assert.assertNotNull(doc.head().select("noscript"));

        Document doc2 = Jsoup.parse("<head><noscript></br><p>text</noscript></head>");
        Assert.assertNotNull(doc2.body().select("p"));

        Document doc3 = Jsoup.parse("<head><noscript><head></noscript></head>");
        Assert.assertNotNull(doc3);
    }

    @Test
    public void testAfterHeadState() {
        Document doc1 = Jsoup.parse("<html><head></head> <!-- comment --> \n <body><p>Text</p></body></html>");
        Assert.assertEquals("Text", doc1.body().select("p").text());

        Document doc2 = Jsoup.parse("<html><head></head><title>Late Title</title><body></body></html>");
        Assert.assertEquals("Late Title", doc2.title());

        Document doc3 = Jsoup.parse("<html><head></head><frameset><frame src='frame.html'></frameset></html>");
        Assert.assertNotNull(doc3.select("frameset").first());

        Document doc4 = Jsoup.parse("<html><head></head></custom><body><p>Text</p></body></html>");
        Assert.assertEquals("Text", doc4.body().select("p").text());

        Document doc5 = Jsoup.parse("<html><head></head><head><body><p>Text</p></body></html>");
        Assert.assertEquals("Text", doc5.body().select("p").text());
    }

    @Test
    public void testInBodyFormattingAndAdoptionAgency() {
        Document doc1 = Jsoup.parse("<a href='1'>1<a href='2'>2</a></a>");
        Assert.assertEquals(2, doc1.body().select("a").size());

        Document doc2 = Jsoup.parse("<b>1<p>2</b>3</p>");
        Assert.assertEquals("1", doc2.body().select("b").first().text());
        Assert.assertEquals("2", doc2.body().select("p > b").first().text());

        Document doc3 = Jsoup.parse("<b><i>test</b></i>");
        Assert.assertNotNull(doc3.body().select("b"));
        Assert.assertNotNull(doc3.body().select("i"));

        Document doc4 = Jsoup.parse("<span><p>Text</p></span>");
        Assert.assertEquals("Text", doc4.body().select("p").text());

        Document doc5 = Jsoup.parse("<a><div><span></a>content</div>");
        Assert.assertNotNull(doc5.body());

        Document doc6 = Jsoup.parse("<nobr>1<nobr>2</nobr>3</nobr>");
        Assert.assertNotNull(doc6.body());
    }

    @Test
    public void testInBodyTags() {
        String html = "<p>P1<h1>H1</h1><h2>H2</h2><h3>H3</h3><h4>H4</h4><h5>H5</h5><h6>H6</h6>" +
                "<ul><li>LI 1<li>LI 2</ul>" +
                "<dl><dt>DT<dd>DD</dl>" +
                "<pre>\nPreformatted</pre>" +
                "<listing>Listing</listing>" +
                "<form id='f1'><input type='text' value='val'><input type='hidden' name='h'>" +
                "<button>Btn</button></form>" +
                "<applet><param name='p' value='v'></applet>" +
                "<hr><img src='img.jpg'><wbr><area><keygen>" +
                "<audio><source src='s.mp3'><track></audio>" +
                "<svg><image></svg><image>" +
                "<isindex action='/search' prompt='Search here:'>" +
                "<textarea>text</textarea><xmp>raw &amp;</xmp><iframe>if</iframe><noembed>ne</noembed>" +
                "<select><optgroup><option>1</option></optgroup></select>" +
                "<ruby>base<rt>rt<rp>)</rp></ruby>" +
                "<math><mrow></mrow></math>" +
                "<table><caption>Cap</caption><colgroup><col></colgroup><tbody><tr><td>Cell</td><th>Header</th></tr></tbody></table>" +
                "<plaintext>Plain text content";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Preformatted", doc.select("pre").text());
        Assert.assertEquals("Cell", doc.select("td").text());
        Assert.assertEquals("Search here: ", doc.select("label").text());
    }

    @Test
    public void testInBodySpecialEndTags() {
        Document doc1 = Jsoup.parse("<div><p>Paragraph</p></div>");
        Assert.assertEquals("Paragraph", doc1.select("p").text());

        Document doc2 = Jsoup.parse("<ul><li>1<li>2</li></li></ul>");
        Assert.assertEquals(2, doc2.select("li").size());

        Document doc3 = Jsoup.parse("<h1>Head</h1></h2>");
        Assert.assertEquals("Head", doc3.select("h1").text());

        Document doc4 = Jsoup.parse("<form>test</form></form>");
        Assert.assertEquals("test", doc4.select("form").text());

        Document doc5 = Jsoup.parse("</p>");
        Assert.assertEquals(1, doc5.select("p").size());

        Document doc6 = Jsoup.parse("</br>");
        Assert.assertEquals(1, doc6.select("br").size());

        Document doc7 = Jsoup.parse("</sarcasm><applet></applet>");
        Assert.assertNotNull(doc7.body());

        Document doc8 = Jsoup.parse("<body class='test'></body>");
        Assert.assertEquals("test", doc8.body().className());

        Document doc9 = Jsoup.parse("<html><head></head><body></body></html><p>After</p>");
        Assert.assertEquals("After", doc9.select("p").text());
    }

    @Test
    public void testInTableStates() {
        String html = "<table>" +
                "<!-- comm -->" +
                "<caption>Cap</caption>" +
                "<colgroup><col></colgroup>" +
                "<thead><tr><th>H</th></tr></thead>" +
                "<tbody><tr><td>D</td></tr></tbody>" +
                "<tfoot><tr><td>F</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Cap", doc.select("caption").text());
        Assert.assertEquals("H", doc.select("th").text());
        Assert.assertEquals("D", doc.select("td").first().text());

        Document doc2 = Jsoup.parse("<table><tr><form><input type='hidden'></form><td>Cell</td></tr></table>");
        Assert.assertEquals("Cell", doc2.select("td").text());

        Document doc3 = Jsoup.parse("<table>   text <tr><td>Cell</td></tr></table>");
        Assert.assertEquals("text Cell", doc3.text());

        Document doc4 = Jsoup.parse("<table><script>var x = 1;</script><style>td { color: red; }</style></table>");
        Assert.assertEquals("var x = 1;", doc4.select("script").data());

        Document doc5 = Jsoup.parse("<table><input type='text'></table>");
        Assert.assertNotNull(doc5.select("input").first());

        Document doc6 = Jsoup.parse("<table><td>Cell</td></table>");
        Assert.assertEquals("Cell", doc6.select("td").text());

        Document doc7 = Jsoup.parse("<table><tr><td>1</td></tr><tr><td>2</td></tr></table>");
        Assert.assertEquals(2, doc7.select("tr").size());

        Document doc8 = Jsoup.parse("<table><caption><tr><td>1</td></tr></caption></table>");
        Assert.assertNotNull(doc8.select("caption").first());
    }

    @Test
    public void testInColumnGroup() {
        Document doc = Jsoup.parse("<table><colgroup><!-- comm --> \n <col id='c1'><col id='c2'></colgroup><tr><td>1</td></tr></table>");
        Assert.assertEquals("c1", doc.select("col").first().id());

        Document doc2 = Jsoup.parse("<table><colgroup><meta></colgroup></table>");
        Assert.assertNotNull(doc2);
    }

    @Test
    public void testInSelectStates() {
        String html = "<select>" +
                "<!-- comm -->" +
                "<optgroup label='g1'>" +
                "<option value='1'>One" +
                "<option value='2'>Two" +
                "</optgroup>" +
                "<option value='3'>Three" +
                "</select>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(3, doc.select("option").size());

        Document doc2 = Jsoup.parse("<table><tr><td><select><option>1</option><td>Next</td></select></td></tr></table>");
        Assert.assertEquals(2, doc2.select("td").size());

        Document doc3 = Jsoup.parse("<select><input type='text'><p>Outside</p></select>");
        Assert.assertNotNull(doc3.select("input").first());
        Assert.assertEquals("Outside", doc3.select("p").text());

        Document doc4 = Jsoup.parse("<select><script>var a = 0;</script></select>");
        Assert.assertEquals("var a = 0;", doc4.select("script").data());
    }

    @Test
    public void testInFramesetAndAfter() {
        String html = "<html><frameset rows='50%,50%'>" +
                "<!-- comm -->" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames</p></noframes>" +
                "</frameset></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("frame").size());

        Document doc2 = Jsoup.parse("<html><frameset><frame></frameset></html><!-- comment -->");
        Assert.assertEquals(1, doc2.select("frame").size());

        Document doc3 = Jsoup.parse("<html><frameset><frame></frameset></html> <noframes>text</noframes>");
        Assert.assertNotNull(doc3.select("frameset").first());
    }

    @Test
    public void testFragmentParsing() {
        Element div = new Element(Tag.valueOf("div"), "");
        XmlTreeBuilder xmlTreeBuilder = new XmlTreeBuilder();
        Assert.assertNotNull(xmlTreeBuilder);

        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        Parser parser = new Parser(treeBuilder);
        treeBuilder.initialiseParseFragment(div);
        Assert.assertTrue(treeBuilder.isFragmentParsing());

        Document fragmentDoc = parser.parseInput("<div><p>Fragment</p></div>", "");
        Assert.assertEquals("Fragment", fragmentDoc.select("p").text());
    }

    @Test
    public void testForeignContentDirectProcess() {
        HtmlTreeBuilderState state = HtmlTreeBuilderState.ForeignContent;
        Token.Character token = new Token.Character().data("text");
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Assert.assertTrue(state.process(token, tb));
    }

    @Test
    public void testNullCharacterHandling() {
        Document doc1 = Jsoup.parse("\u0000");
        Assert.assertNotNull(doc1);

        Document doc2 = Jsoup.parse("<table>\u0000</table>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<select>\u0000</select>");
        Assert.assertNotNull(doc3);
    }
}
