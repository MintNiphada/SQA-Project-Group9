package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testEnumValuesAndValueOf() {
        HtmlTreeBuilderState[] states = HtmlTreeBuilderState.values();
        Assert.assertTrue(states.length > 0);
        Assert.assertEquals(HtmlTreeBuilderState.Initial, HtmlTreeBuilderState.valueOf("Initial"));
        Assert.assertEquals(HtmlTreeBuilderState.ForeignContent, HtmlTreeBuilderState.valueOf("ForeignContent"));
    }

    @Test
    public void testInitialStateVariants() {
        Document doc1 = Jsoup.parse("   <!DOCTYPE html><html></html>");
        Assert.assertNotNull(doc1.childNode(0));

        Document doc2 = Jsoup.parse("<!-- comment --><!DOCTYPE html><html></html>");
        Assert.assertEquals("#comment", doc2.childNode(0).nodeName());

        Document doc3 = Jsoup.parse("<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html></html>");
        Assert.assertNotNull(doc3.doctype());

        Document doc4 = Jsoup.parse("<html>text before doctype<!DOCTYPE html></html>");
        Assert.assertNotNull(doc4.body());
    }

    @Test
    public void testBeforeHtmlVariants() {
        Document doc1 = Jsoup.parse("<!DOCTYPE html>   <html><head></head><body></body></html>");
        Assert.assertEquals("html", doc1.child(0).tagName());

        Document doc2 = Jsoup.parse("<!DOCTYPE html><!-- comment --><html></html>");
        Assert.assertEquals("html", doc2.child(0).tagName());

        Document doc3 = Jsoup.parse("<!DOCTYPE html></head><html></html>");
        Assert.assertEquals("html", doc3.child(0).tagName());

        Document doc4 = Jsoup.parse("<!DOCTYPE html></div><html></html>");
        Assert.assertEquals("html", doc4.child(0).tagName());

        Document doc5 = Jsoup.parse("<!DOCTYPE html><!DOCTYPE html><html></html>");
        Assert.assertEquals("html", doc5.child(0).tagName());
    }

    @Test
    public void testBeforeHeadVariants() {
        Document doc1 = Jsoup.parse("<html>   <head></head></html>");
        Assert.assertNotNull(doc1.head());

        Document doc2 = Jsoup.parse("<html><!-- c --> <head></head></html>");
        Assert.assertNotNull(doc2.head());

        Document doc3 = Jsoup.parse("<html><!DOCTYPE html><head></head></html>");
        Assert.assertNotNull(doc3.head());

        Document doc4 = Jsoup.parse("<html><html><head></head></html>");
        Assert.assertNotNull(doc4.head());

        Document doc5 = Jsoup.parse("<html></head><head></head></html>");
        Assert.assertNotNull(doc5.head());

        Document doc6 = Jsoup.parse("<html></div><head></head></html>");
        Assert.assertNotNull(doc6.head());

        Document doc7 = Jsoup.parse("<html><body></body></html>");
        Assert.assertNotNull(doc7.body());
    }

    @Test
    public void testInHeadVariants() {
        String html = "<html><head>" +
                "   <!-- c -->" +
                "<!DOCTYPE html>" +
                "<html>" +
                "<base href='http://example.com/'>" +
                "<basefont>" +
                "<bgsound>" +
                "<command>" +
                "<link rel='stylesheet' href='foo.css'>" +
                "<meta charset='utf-8'>" +
                "<title>Test Title</title>" +
                "<noframes>noframes text</noframes>" +
                "<style>body { color: red; }</style>" +
                "<noscript><meta http-equiv='refresh'></noscript>" +
                "<script>var a = 1;</script>" +
                "<head>" +
                "</head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Test Title", doc.title());
        Assert.assertEquals("http://example.com/", doc.baseUri());

        Document docEnd = Jsoup.parse("<html><head></head><body></body></html>");
        Assert.assertNotNull(docEnd.body());

        Document docBadEnd = Jsoup.parse("<html><head></div></head><body></body></html>");
        Assert.assertNotNull(docBadEnd.body());
    }

    @Test
    public void testInHeadNoscriptVariants() {
        String html = "<html><head><noscript>" +
                "<!DOCTYPE html>" +
                "<html>" +
                "<!-- c -->" +
                "<link rel='stylesheet'>" +
                "<meta charset='utf-8'>" +
                "<style>p { color: blue; }</style>" +
                "<br>" +
                "<head>" +
                "<noscript>" +
                "</noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc.body());
    }

    @Test
    public void testAfterHeadVariants() {
        String html1 = "<html><head></head>   <!-- c --><body></body></html>";
        Document doc1 = Jsoup.parse(html1);
        Assert.assertNotNull(doc1.body());

        String html2 = "<html><head></head><!DOCTYPE html><body></body></html>";
        Document doc2 = Jsoup.parse(html2);
        Assert.assertNotNull(doc2.body());

        String html3 = "<html><head></head><html><body></body></html>";
        Document doc3 = Jsoup.parse(html3);
        Assert.assertNotNull(doc3.body());

        String html4 = "<html><head></head><frameset><frame></frameset></html>";
        Document doc4 = Jsoup.parse(html4);
        Assert.assertNotNull(doc4.select("frameset").first());

        String html5 = "<html><head></head><meta charset='utf-8'><title>Late</title><body></body></html>";
        Document doc5 = Jsoup.parse(html5);
        Assert.assertEquals("Late", doc5.title());

        String html6 = "<html><head></head><head><body></body></html>";
        Document doc6 = Jsoup.parse(html6);
        Assert.assertNotNull(doc6.body());

        String html7 = "<html><head></head></body><body></body></html>";
        Document doc7 = Jsoup.parse(html7);
        Assert.assertNotNull(doc7.body());

        String html8 = "<html><head></head></div><body></body></html>";
        Document doc8 = Jsoup.parse(html8);
        Assert.assertNotNull(doc8.body());
    }

    @Test
    public void testInBodyTagsAndFormatting() {
        String html = "<html><head></head><body>" +
                "\u0000" +
                "   " +
                "<!-- comment -->" +
                "<!DOCTYPE html>" +
                "<a href='#'>Link1<a href='#'>Link2</a></a>" +
                "<area><br><embed><img><keygen><wbr>" +
                "<p>Para 1</p>" +
                "<div>Div 1</div>" +
                "<span>Span 1</span>" +
                "<ul><li>Item 1<li>Item 2</li></ul>" +
                "<html id='secondHtml'>" +
                "<body class='secondBody'>" +
                "<h1>Heading 1<h2>Heading 2</h2></h1>" +
                "<pre>Pre 1</pre><listing>List 1</listing>" +
                "<form id='f1'><input type='text'></form><form id='f2'></form>" +
                "<dl><dt>Term<dd>Desc</dl>" +
                "<plaintext>Plaintext content" +
                "</body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertTrue(doc.text().contains("Plaintext content"));
    }

    @Test
    public void testInBodyButtonsAndFormatting() {
        String html = "<button>Button1<button>Button2</button></button>" +
                "<b>Bold <i>Italic <b>Nested</b></i></b>" +
                "<nobr>Nobr1<nobr>Nobr2</nobr></nobr>" +
                "<applet>Applet</applet><marquee>Marquee</marquee><object>Obj</object>" +
                "<hr>" +
                "<image src='foo.png'>" +
                "<svg><image href='bar.png'></svg>" +
                "<textarea>\nText</textarea>" +
                "<xmp>Xmp text</xmp>" +
                "<iframe>Frame text</iframe>" +
                "<noembed>Noembed text</noembed>" +
                "<select><option>1</option><optgroup label='g'><option>2</option></optgroup></select>" +
                "<ruby>Ruby <rp>(</rp><rt>Rt</rt><rp>)</rp></ruby>" +
                "<math><mrow></mrow></math>" +
                "<caption>Ignored</caption>" +
                "<input type='hidden'><input type='text'>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc.select("textarea").first());
        Assert.assertEquals("Text", doc.select("textarea").first().text());
    }

    @Test
    public void testInBodyAdoptionAgency() {
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("1", doc.body().child(0).text());

        String tableFoster = "<b>1<table><tr><td>2</b>3</td></tr></table>";
        Document docTable = Jsoup.parse(tableFoster);
        Assert.assertNotNull(docTable.select("table").first());

        String htmlDeep = "<a><b><c><d><e><f><g><h><i><j><p>Deep</p></i></j></h></g></f></e></d></c></b></a>";
        Document docDeep = Jsoup.parse(htmlDeep);
        Assert.assertNotNull(docDeep.body());
    }

    @Test
    public void testInBodyEndTags() {
        String html = "<div>" +
                "<p>Test" +
                "<h1>H1" +
                "<dl><dt>Dt<dd>Dd" +
                "<ul><li>Li1<li>Li2" +
                "<applet>App</applet>" +
                "</span>" +
                "</sarcasm>" +
                "</br>" +
                "</p>" +
                "</div>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc.body());

        Document docHtmlEnd = Jsoup.parse("<html><body>Hello</body></html>");
        Assert.assertEquals("Hello", docHtmlEnd.body().text());

        Document docSpanEnd = Jsoup.parse("<span>Hello</span>");
        Assert.assertEquals("Hello", docSpanEnd.body().text());
    }

    @Test
    public void testInTableStates() {
        String html = "<table>" +
                "   " +
                "<!-- c -->" +
                "<!DOCTYPE html>" +
                "<caption>Cap</caption>" +
                "<colgroup><col width='10'></colgroup>" +
                "<col width='20'>" +
                "<thead><tr><th>H1</th></tr></thead>" +
                "<tbody><tr><td>D1</td></tr></tbody>" +
                "<tfoot><tr><td>F1</td></tr></tfoot>" +
                "<tr><td>Row</td></tr>" +
                "<table><tr><td>Nested</td></tr></table>" +
                "<style>td {color: red;}</style>" +
                "<input type='hidden' name='h' value='1'>" +
                "<input type='text' name='t'>" +
                "<form id='tf'></form>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Cap", doc.select("caption").text());
        Assert.assertEquals("H1", doc.select("th").text());
    }

    @Test
    public void testInTableTextAndFoster() {
        String html = "<table>Foo<tr><td>Bar</td></tr>Baz</table>";
        Document doc = Jsoup.parse(html);
        Assert.assertTrue(doc.text().contains("Foo"));
        Assert.assertTrue(doc.text().contains("Baz"));

        String nullCharTable = "<table>\u0000<tr><td>Cell</td></tr></table>";
        Document docNull = Jsoup.parse(nullCharTable);
        Assert.assertNotNull(docNull.select("table").first());
    }

    @Test
    public void testInCaptionAndColumnGroup() {
        String capHtml = "<table><caption>Cap<b>Bold</b><tr><td>Next</td></tr></caption></table>";
        Document capDoc = Jsoup.parse(capHtml);
        Assert.assertEquals("CapBold", capDoc.select("caption").text());

        String colHtml = "<table><colgroup><!-- c --><!DOCTYPE html><col></colgroup></table>";
        Document colDoc = Jsoup.parse(colHtml);
        Assert.assertNotNull(colDoc.select("col").first());
    }

    @Test
    public void testInTableBodyAndRowAndCell() {
        String html = "<table><tbody>" +
                "<tr>" +
                "<td>Cell 1</td>" +
                "<th>Cell 2</th>" +
                "</tr>" +
                "<tr>" +
                "<td>Cell 3" +
                "<tr>" +
                "<td>Cell 4</td>" +
                "</tr>" +
                "</tbody></table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(4, doc.select("td, th").size());

        String errHtml = "<table><tr></th></td></tr></table>";
        Document errDoc = Jsoup.parse(errHtml);
        Assert.assertNotNull(errDoc.body());
    }

    @Test
    public void testInSelectStates() {
        String html = "<select>" +
                "\u0000" +
                "<!-- c -->" +
                "<!DOCTYPE html>" +
                "<html>" +
                "<option value='1'>One</option>" +
                "<optgroup label='g'><option value='2'>Two</option></optgroup>" +
                "<select>" +
                "<input>" +
                "<script>var x = 1;</script>" +
                "</select>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("option").size());

        String inTableSelect = "<table><tr><td><select><option>A</option><tr><td>Next</td></tr></select></td></tr></table>";
        Document docTableSelect = Jsoup.parse(inTableSelect);
        Assert.assertNotNull(docTableSelect.select("select").first());
    }

    @Test
    public void testFramesetStates() {
        String html = "<html><head></head>" +
                "<frameset rows='50%,50%'>" +
                "   <!-- c -->" +
                "<!DOCTYPE html>" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames</p></noframes>" +
                "<frameset cols='*'><frame src='nested.html'></frameset>" +
                "</frameset></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(3, doc.select("frame").size());

        String afterFrameset = "<html><frameset><frame></frameset>   <!-- c --><noframes></noframes></html>";
        Document docAf = Jsoup.parse(afterFrameset);
        Assert.assertNotNull(docAf.select("frameset").first());
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        String html = "<html><head></head><body>Hello</body>   <!-- c --><!DOCTYPE html></html><!-- c2 -->";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Hello", doc.body().text());

        Document docExtra = Jsoup.parse("<html><body></body></html><p>Extra after html</p>");
        Assert.assertEquals("Extra after html", docExtra.body().text());
    }

    @Test
    public void testFragmentParsingBranches() {
        Element context = new Element(Tag.valueOf("select"), "");
        List<org.jsoup.nodes.Node> nodes = Parser.parseFragment("<option>1</option><option>2</option>", context, "http://example.com");
        Assert.assertEquals(2, nodes.size());

        Element tableContext = new Element(Tag.valueOf("table"), "");
        List<org.jsoup.nodes.Node> tableNodes = Parser.parseFragment("<tr><td>Cell</td></tr>", tableContext, "http://example.com");
        Assert.assertFalse(tableNodes.isEmpty());

        Element bodyContext = new Element(Tag.valueOf("body"), "");
        List<org.jsoup.nodes.Node> bodyNodes = Parser.parseFragment("<div>Body fragment</div>", bodyContext, "http://example.com");
        Assert.assertFalse(bodyNodes.isEmpty());
    }

    @Test
    public void testIsIndexHandling() {
        String html = "<isindex prompt='Search here: ' action='/search'>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc.select("form").first());
        Assert.assertNotNull(doc.select("input[name=isindex]").first());
    }

    @Test
    public void testForeignContentDirectProcess() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div></div>", "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Token.Character t = new Token.Character().data("text");
        boolean res = HtmlTreeBuilderState.ForeignContent.process(t, tb);
        Assert.assertTrue(res);
    }
}
