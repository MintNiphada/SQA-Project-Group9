package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testInitialStateAndDoctypes() {
        String html = "   <!-- comment -->\n<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.documentType());
        assertEquals("html", doc.documentType().name());

        Document quirksDoc = Jsoup.parse("<!DOCTYPE html SYSTEM \"something\" >");
        assertNotNull(quirksDoc);
    }

    @Test
    public void testBeforeHtml() {
        Document doc1 = Jsoup.parse("<html><head></head><body></body></html>");
        assertNotNull(doc1.body());

        Document doc2 = Jsoup.parse("<!-- comment -->  </head><div>Text</div>");
        assertEquals("Text", doc2.body().text());

        Document doc3 = Jsoup.parse("</random><p>Hi</p>");
        assertEquals("Hi", doc3.select("p").text());
    }

    @Test
    public void testBeforeHeadAndInHead() {
        String html = "<html><!-- comm --> <head><title>Test Title</title>"
                + "<base href='http://example.com/'>"
                + "<meta name='keywords' content='test'>"
                + "<link rel='stylesheet' href='test.css'>"
                + "<style>body { color: red; }</style>"
                + "<noframes>No frames</noframes>"
                + "<noscript>No script</noscript>"
                + "<script>var a = 1;</script>"
                + "</head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("test.css", doc.select("link").attr("href"));
    }

    @Test
    public void testInHeadNoscript() {
        String html = "<head><noscript><!-- comm --> <link rel='stylesheet' href='a.css'><meta charset='utf-8'></noscript></head>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.head());

        Document doc2 = Jsoup.parse("<head><noscript></noscript></head>");
        assertNotNull(doc2.head());

        Document doc3 = Jsoup.parse("<head><noscript><p>Text</noscript></head>");
        assertNotNull(doc3.body());
    }

    @Test
    public void testAfterHead() {
        Document doc = Jsoup.parse("<html><head></head><!-- c --> <base href='http://example.com/'><title>After</title><body></body></html>");
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("After", doc.title());

        Document doc2 = Jsoup.parse("<html><head></head> <frameset rows='100%'><frame src='a.html'></frameset></html>");
        assertEquals(1, doc2.select("frameset").size());
        assertEquals(1, doc2.select("frame").size());

        Document doc3 = Jsoup.parse("<html><head></head></head><body>Hello</body></html>");
        assertEquals("Hello", doc3.body().text());
    }

    @Test
    public void testInBodyFormattingAndTags() {
        String html = "<p>Para 1<p>Para 2<hr><br><img src='test.png'>"
                + "<a>Link 1 <a>Link 2</a></a>"
                + "<b>Bold <i>Bold-Italic</b> Italic</i>"
                + "<ul><li>Item 1<li>Item 2</ul>"
                + "<dl><dt>Dt 1<dd>Dd 1<dt>Dt 2<dd>Dd 2</dl>"
                + "<h1>Heading 1<h2>Heading 2</h2></h1>"
                + "<pre>Preformatted</pre><listing>Listing</listing>"
                + "<button><button>Nested button</button></button>"
                + "<nobr>Nobr 1 <nobr>Nobr 2</nobr></nobr>"
                + "<applet><p>Applet content</p></applet>"
                + "<form id='f1'><input type='hidden' name='h' value='v'></form>"
                + "<form id='f2'></form>"
                + "<xmp><p>Raw</p></xmp><iframe>Frame</iframe><noembed>Noembed</noembed>"
                + "<span>Span</span>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
        assertEquals(2, doc.select("p").size());
        assertEquals(2, doc.select("li").size());
        assertEquals(2, doc.select("dt").size());
        assertEquals(1, doc.select("form").size());
    }

    @Test
    public void testInBodyIsindex() {
        Document doc = Jsoup.parse("<isindex action='/search' prompt='Search this site:'>");
        assertNotNull(doc.select("form").first());
        assertNotNull(doc.select("input[name=isindex]").first());
    }

    @Test
    public void testInBodyImageAndSvgMath() {
        Document doc = Jsoup.parse("<image src='img.jpg'><svg><image xlink:href='test.svg'></svg><math><mi>x</mi></math>");
        assertEquals(1, doc.select("img").size());
        assertEquals(1, doc.select("svg image").size());
        assertEquals(1, doc.select("math").size());
    }

    @Test
    public void testInBodyAdoptionAgency() {
        Document doc = Jsoup.parse("<b>1<p>2</b>3</p>");
        assertEquals("1", doc.select("b").first().text());
        assertEquals("2", doc.select("p b").first().text());
        assertEquals("23", doc.select("p").first().text());

        Document doc2 = Jsoup.parse("<a>1<p>2</a>3</p>");
        assertEquals("1", doc2.select("a").first().text());
        assertEquals("2", doc2.select("p a").first().text());
    }

    @Test
    public void testInBodyTableInP() {
        Document doc = Jsoup.parse("<p>Hello<table><tr><td>Cell</td></tr></table>World</p>");
        assertEquals(2, doc.select("p").size());
        assertEquals(1, doc.select("table").size());
    }

    @Test
    public void testInTableStates() {
        String html = "<table>"
                + "<caption>Caption Text</caption>"
                + "<colgroup><col width='10'><col width='20'></colgroup>"
                + "<thead><tr><th>Header 1</th><th>Header 2</th></tr></thead>"
                + "<tbody><tr><td>Data 1</td><td>Data 2</td></tr></tbody>"
                + "<tfoot><tr><td>Foot 1</td><td>Foot 2</td></tr></tfoot>"
                + "</table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("caption").size());
        assertEquals(2, doc.select("col").size());
        assertEquals(1, doc.select("thead").size());
        assertEquals(1, doc.select("tbody").size());
        assertEquals(1, doc.select("tfoot").size());
        assertEquals(3, doc.select("tr").size());
    }

    @Test
    public void testInTableFosterParenting() {
        Document doc = Jsoup.parse("<table><tr><td>1</td></tr>Text Outside<tr><td>2</td></tr></table>");
        assertTrue(doc.body().text().contains("Text Outside"));
    }

    @Test
    public void testInTableHiddenInputAndForm() {
        Document doc = Jsoup.parse("<table><input type='hidden' name='k' value='v'><form id='tblForm'><tr><td>Cell</td></tr></form></table>");
        assertEquals(1, doc.select("input[type=hidden]").size());
        assertEquals(1, doc.select("form").size());
    }

    @Test
    public void testInSelectStates() {
        String html = "<select name='test'>"
                + "<optgroup label='group1'><option value='1'>One<option value='2'>Two</optgroup>"
                + "<option value='3'>Three"
                + "</select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("select").size());
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(3, doc.select("option").size());

        Document doc2 = Jsoup.parse("<table><tr><td><select><option>1</select></td></tr></table>");
        assertEquals(1, doc2.select("table select").size());

        Document doc3 = Jsoup.parse("<select><input><keygen><textarea></select>");
        assertEquals(1, doc3.select("input").size());
    }

    @Test
    public void testInFramesetAndAfterFrameset() {
        String html = "<html><frameset cols='50%,50%'><frame src='frame1.html'><frame src='frame2.html'><noframes>No frames allowed</noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("frameset").size());
        assertEquals(2, doc.select("frame").size());
        assertEquals(1, doc.select("noframes").size());
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        Document doc = Jsoup.parse("<html><head></head><body>Hello</body></html><!-- final comment -->");
        assertEquals("Hello", doc.body().text());
        assertEquals(1, doc.select("body").size());

        Document doc2 = Jsoup.parse("<html><body>Hello</body></html><div>Extra</div>");
        assertTrue(doc2.body().text().contains("Extra"));
    }

    @Test
    public void testRubyParsing() {
        Document doc = Jsoup.parse("<ruby>漢 <rp>(</rp><rt>かん</rt><rp>)</rp></ruby>");
        assertEquals(1, doc.select("ruby").size());
        assertEquals(2, doc.select("rp").size());
        assertEquals(1, doc.select("rt").size());
    }

    @Test
    public void testFragmentParsing() {
        List<Element> nodes = Parser.parseFragment("<div><p>Fragment text</p></div>", new Element(Tag.valueOf("body"), ""), "");
        assertFalse(nodes.isEmpty());
        assertEquals("Fragment text", nodes.get(0).select("p").text());

        List<Element> trNodes = Parser.parseFragment("<td>Cell 1</td><td>Cell 2</td>", new Element(Tag.valueOf("tr"), ""), "");
        assertEquals(2, trNodes.size());
    }

    @Test
    public void testNullCharacterHandling() {
        Document doc = Jsoup.parse("<body>\u0000Text\u0000</body>");
        assertNotNull(doc);
    }

    @Test
    public void testForeignContentEnumCoverage() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertTrue(HtmlTreeBuilderState.ForeignContent.process(new Token.Character().data("test"), tb));
    }
}
