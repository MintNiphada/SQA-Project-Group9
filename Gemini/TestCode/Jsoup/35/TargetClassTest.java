package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import static org.junit.Assert.*;

public class HtmlTreeBuilderStateTest {

    @Test
    public void testInitialAndBeforeHtmlTransitions() {
        String html = "<!DOCTYPE html>   <!-- comment --><html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("html", doc.childNode(0).nodeName());
    }

    @Test
    public void testDoctypeInQuirks() {
        String html = "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test
    public void testBeforeHtmlWhitespaceAndComments() {
        String html = "   \n\r\t <!-- c1 --> <html> <!-- c2 --> </html>";
        Document doc = Jsoup.parse(html);
        assertEquals("html", doc.select("html").first().tagName());
    }

    @Test
    public void testBeforeHtmlUnexpectedTokens() {
        String html = "   <body class='test'>Hello</body>";
        Document doc = Jsoup.parse(html);
        assertEquals("test", doc.body().className());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testInHeadElements() {
        String html = "<html><head><base href='http://example.com/'><title>Test Title</title><meta name='desc' content='test'><link rel='stylesheet' href='test.css'><style>body{}</style><script>var x = 1;</script><noscript>No JS</noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test Title", doc.title());
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals(1, doc.select("meta").size());
        assertEquals(1, doc.select("link").size());
        assertEquals(1, doc.select("style").size());
        assertEquals(1, doc.select("script").size());
    }

    @Test
    public void testInHeadNoscript() {
        String html = "<html><head><noscript><!-- noscript comment --><style>p{}</style><link rel='stylesheet' href='a.css'></noscript></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test
    public void testAfterHeadTransitions() {
        String html = "<html><head></head> <!-- after head comment --> <body>Content</body></html>";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.body().text());
    }

    @Test
    public void testInBodyTags() {
        String html = "<div><p>Para 1</p><p>Para 2</p><ul><li>Item 1</li><li>Item 2</li></ul><dl><dt>Dt</dt><dd>Dd</dd></dl><pre>Preformatted</pre><form action='/submit'><input type='text' name='q'><button>Send</button></form><hr></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("p").size());
        assertEquals(2, doc.select("li").size());
        assertEquals(1, doc.select("form").size());
        assertEquals(1, doc.select("button").size());
    }

    @Test
    public void testAdoptionAgencyAlgorithm() {
        String html = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(html);
        assertEquals("<b>1</b><p><b>2</b>3</p>", doc.body().html().replaceAll("\\r?\\n", ""));
    }

    @Test
    public void testAdoptionAgencyNested() {
        String html = "<a>1<b>2<table><tr><td>3</b>4</td></tr></table>5</a>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test
    public void testFormattingElementsScope() {
        String html = "<nobr>1<nobr>2</nobr>3</nobr>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test
    public void testInTableStates() {
        String html = "<table>" +
                "<caption>Table Caption</caption>" +
                "<colgroup><col width='10'></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody>" +
                "<tfoot><tr><td>Footer</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Table Caption", doc.select("caption").text());
        assertEquals(1, doc.select("col").size());
        assertEquals("Header", doc.select("th").text());
        assertEquals(2, doc.select("tbody td").size());
        assertEquals("Footer", doc.select("tfoot td").text());
    }

    @Test
    public void testTableFosterParenting() {
        String html = "<table><tr><td>1</td></tr>TextOutside<td>2</td></table>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.body().text().contains("TextOutside"));
    }

    @Test
    public void testInTableInputAndForm() {
        String html = "<table><form id='f1'><input type='hidden' name='h' value='1'><tr><td>Cell</td></tr></form></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("input[type=hidden]").size());
    }

    @Test
    public void testInSelect() {
        String html = "<select><optgroup label='g1'><option value='1'>1</option></optgroup><option value='2'>2</option></select>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("optgroup").size());
        assertEquals(2, doc.select("option").size());
    }

    @Test
    public void testInSelectInTable() {
        String html = "<table><tr><td><select><option>A</option><tr><td>Next</td></tr></select></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test
    public void testInFrameset() {
        String html = "<html><frameset rows='50%,50%'><frame src='frame1.html'><frame src='frame2.html'><noframes><p>No frames</p></noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("frame").size());
    }

    @Test
    public void testTextareaAndXmpAndIframe() {
        String html = "<textarea><tag>inside</tag></textarea><xmp><raw>xmp</raw></xmp><iframe><frame>inside</frame></iframe>";
        Document doc = Jsoup.parse(html);
        assertEquals("<tag>inside</tag>", doc.select("textarea").val());
        assertEquals("<raw>xmp</raw>", doc.select("xmp").text());
        assertEquals(1, doc.select("iframe").size());
    }

    @Test
    public void testIsindexTagHandling() {
        String html = "<div><isindex prompt='Search here: ' action='/search'></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("form").size());
        assertEquals(1, doc.select("input[name=isindex]").size());
    }

    @Test
    public void testForeignContentAndSvgMath() {
        String html = "<div><svg><path d='M0 0'/></svg><math><mi>x</mi></math></div>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("svg").size());
        assertEquals(1, doc.select("math").size());
    }

    @Test
    public void testRubyElements() {
        String html = "<ruby>漢 <rp>(</rp><rt>かん</rt><rp>)</rp></ruby>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("ruby").size());
        assertEquals(1, doc.select("rt").size());
        assertEquals(2, doc.select("rp").size());
    }

    @Test
    public void testHeadingsAutoClose() {
        String html = "<h1>Heading 1<h2>Heading 2</h3>";
        Document doc = Jsoup.parse(html);
        assertEquals(1, doc.select("h1").size());
        assertEquals(1, doc.select("h2").size());
    }

    @Test
    public void testNullCharacterHandling() {
        String html = "<div>Hello \u0000 World</div><table>\u0000<tr><td>Cell</td></tr></table><select>\u0000<option>Opt</option></select>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test
    public void testBodyEndTagsAndAfterAfterBody() {
        String html = "<html><head></head><body>Content</body></html><!-- after html comment -->";
        Document doc = Jsoup.parse(html);
        assertEquals("Content", doc.body().text());
    }

    @Test
    public void testValuesCoverage() {
        for (HtmlTreeBuilderState state : HtmlTreeBuilderState.values()) {
            assertNotNull(state);
            assertNotNull(HtmlTreeBuilderState.valueOf(state.name()));
        }
    }
}
