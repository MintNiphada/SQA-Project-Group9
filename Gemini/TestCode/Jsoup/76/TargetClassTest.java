package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

public class TargetClassTest {

    @Test
    public void testConstantsSorted() throws IllegalAccessException {
        for (Field field : HtmlTreeBuilderState.Constants.class.getDeclaredFields()) {
            if (field.getType().isArray() && field.getType().getComponentType().equals(String.class)) {
                String[] array = (String[]) field.get(null);
                String[] copy = Arrays.copyOf(array, array.length);
                Arrays.sort(copy);
                Assert.assertArrayEquals(copy, array);
            }
        }
    }

    @Test
    public void testEnumValues() {
        for (HtmlTreeBuilderState state : HtmlTreeBuilderState.values()) {
            Assert.assertNotNull(state);
            Assert.assertEquals(state, HtmlTreeBuilderState.valueOf(state.name()));
        }
    }

    @Test
    public void testForeignContent() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        boolean processed = HtmlTreeBuilderState.ForeignContent.process(new Token.EOF(), tb);
        Assert.assertTrue(processed);
    }

    @Test
    public void testInitialAndBeforeHtmlTransitions() {
        String html = "   <!-- comment --> <!DOCTYPE html> <html><head></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("html", doc.child(0).nodeName());
    }

    @Test
    public void testInitialQuirksMode() {
        String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc);
    }

    @Test
    public void testBeforeHtmlTransitionsAndErrors() {
        String html = "<html><!-- comment --><div></div></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("div").size());

        String badHtml = "</div><html></html>";
        Document doc2 = Jsoup.parse(badHtml);
        Assert.assertNotNull(doc2);
    }

    @Test
    public void testBeforeHeadTransitions() {
        String html = "<html><head><title>Test</title></head><body></body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Test", doc.title());

        String autoHead = "<html><body>Hello</body></html>";
        Document doc2 = Jsoup.parse(autoHead);
        Assert.assertEquals("Hello", doc2.body().text());

        String weirdEndTag = "<html></br><head></head></html>";
        Document doc3 = Jsoup.parse(weirdEndTag);
        Assert.assertNotNull(doc3);
    }

    @Test
    public void testInHeadElements() {
        String html = "<head>" +
                "<base href='http://example.com/'>" +
                "<basefont>" +
                "<bgsound>" +
                "<link rel='stylesheet' href='a.css'>" +
                "<meta charset='utf-8'>" +
                "<title>Head Elements</title>" +
                "<style>body { color: red; }</style>" +
                "<noscript><link rel='stylesheet' href='b.css'></noscript>" +
                "<script>var x = 1;</script>" +
                "</head>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Head Elements", doc.title());
        Assert.assertEquals("http://example.com/", doc.baseUri());
        Assert.assertEquals("body { color: red; }", doc.head().select("style").first().data());
    }

    @Test
    public void testInHeadNoscript() {
        String html = "<head><noscript><!-- comment --><link rel='stylesheet' href='b.css'><meta name='test'>Text</noscript></head>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc);

        String html2 = "<head><noscript><head></noscript></head>";
        Document doc2 = Jsoup.parse(html2);
        Assert.assertNotNull(doc2);

        String html3 = "<head><noscript></br>Hello</noscript></head>";
        Document doc3 = Jsoup.parse(html3);
        Assert.assertNotNull(doc3);
    }

    @Test
    public void testAfterHead() {
        String html = "<head></head> <!-- comment --> <body>Hello</body>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Hello", doc.body().text());

        String htmlFrameset = "<head></head><frameset><frame src='foo.html'></frameset>";
        Document doc2 = Jsoup.parse(htmlFrameset);
        Assert.assertEquals(1, doc2.select("frameset").size());

        String htmlAfterHeadElements = "<head></head><meta name='foo'><body>Bar</body>";
        Document doc3 = Jsoup.parse(htmlAfterHeadElements);
        Assert.assertEquals(1, doc3.head().select("meta").size());

        String htmlBadEnd = "<head></head></html>";
        Document doc4 = Jsoup.parse(htmlBadEnd);
        Assert.assertNotNull(doc4);
    }

    @Test
    public void testInBodyFormattingAndAdoptionAgency() {
        String html = "<a><b>1<p>2</a>3</p>4</b>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc.body());

        String htmlNestedA = "<a>1<a>2</a>3</a>";
        Document doc2 = Jsoup.parse(htmlNestedA);
        Assert.assertEquals(2, doc2.body().select("a").size());

        String htmlSpan = "<span>Hello</span>";
        Document doc3 = Jsoup.parse(htmlSpan);
        Assert.assertEquals("Hello", doc3.body().select("span").text());
    }

    @Test
    public void testInBodyHeadingsAndLists() {
        String html = "<p><h1>Heading 1</h1><h2>Heading 2</h2></p><ul><li>One<li>Two</ul>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("li").size());
        Assert.assertEquals(1, doc.select("h1").size());
        Assert.assertEquals(1, doc.select("h2").size());

        String htmlDl = "<dl><dt>Term<dd>Definition<dt>Term 2<dd>Definition 2</dl>";
        Document doc2 = Jsoup.parse(htmlDl);
        Assert.assertEquals(2, doc2.select("dt").size());
        Assert.assertEquals(2, doc2.select("dd").size());
    }

    @Test
    public void testInBodyMiscellaneousTags() {
        String html = "<div>" +
                "<button>Button 1<button>Button 2</button></button>" +
                "<nobr>Nobr 1<nobr>Nobr 2</nobr></nobr>" +
                "<applet>Applet</applet>" +
                "<hr><image src='test.jpg'>" +
                "<textarea>Text\nInside</textarea>" +
                "<xmp>Xmp content</xmp>" +
                "<iframe>Iframe content</iframe>" +
                "<noembed>Noembed content</noembed>" +
                "<ruby>Ruby <rp>(</rp><rt>rt</rt><rp>)</rp></ruby>" +
                "<math><mi>x</mi></math>" +
                "<svg><image></image></svg>" +
                "</div>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("button").size());
        Assert.assertEquals(1, doc.select("textarea").size());
        Assert.assertEquals(1, doc.select("img").size());
        Assert.assertEquals(1, doc.select("svg image").size());
    }

    @Test
    public void testInBodyIsindex() {
        String html = "<isindex action='/search' prompt='Search this site: '></isindex>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("form").size());
        Assert.assertEquals(1, doc.select("input[name=isindex]").size());
    }

    @Test
    public void testInBodyFormsAndAttributes() {
        String html = "<html class='a'><html class='b'><body id='b1'><body id='b2'><form id='f1'><form id='f2'></form></body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertTrue(doc.children().first().hasClass("a"));
        Assert.assertEquals("b1", doc.body().id());
        Assert.assertEquals(1, doc.select("form").size());
    }

    @Test
    public void testInBodyPlaintext() {
        String html = "<p>Para</p><plaintext><b>Not bold</b></plaintext>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(0, doc.select("b").size());
        Assert.assertTrue(doc.text().contains("<b>Not bold</b>"));
    }

    @Test
    public void testInBodyEndTagsHandling() {
        String html = "<div><p>Paragraph</p><h1>Heading</h1><sarcasm>Text</sarcasm><span>Span</span><br></div>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("p").size());
        Assert.assertEquals(1, doc.select("h1").size());
        Assert.assertEquals(1, doc.select("br").size());

        String invalidClose = "</p></div>";
        Document doc2 = Jsoup.parse(invalidClose);
        Assert.assertNotNull(doc2);
    }

    @Test
    public void testInTableStates() {
        String html = "<table>" +
                "<caption>Caption text</caption>" +
                "<colgroup><col width='10'></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Data</td></tr></tbody>" +
                "<tfoot><tr><td>Footer</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("caption").size());
        Assert.assertEquals(1, doc.select("colgroup").size());
        Assert.assertEquals(1, doc.select("th").size());
        Assert.assertEquals(2, doc.select("td").size());
    }

    @Test
    public void testInTableFosterParentingAndText() {
        String html = "<table>Characters<b>Bold</b><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Assert.assertTrue(doc.body().text().contains("Characters"));
        Assert.assertEquals(1, doc.select("table").size());
    }

    @Test
    public void testInTableFormAndInput() {
        String html = "<table><form action='/test'><input type='hidden' name='x' value='y'><input type='text' name='q'><tr><td>A</td></tr></form></table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("input[type=hidden]").size());
        Assert.assertEquals(1, doc.select("input[type=text]").size());
    }

    @Test
    public void testInTableSpecialBranches() {
        String html = "<table><style>.a{}</style><script>var t=1;</script><tr><td>1</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.head().select("style").size());
        Assert.assertEquals(1, doc.head().select("script").size());

        String html2 = "<table>col<tr><td>Cell</td></tr></table>";
        Document doc2 = Jsoup.parse(html2);
        Assert.assertEquals(1, doc2.select("td").size());
    }

    @Test
    public void testInCaptionAndInColumnGroup() {
        String html = "<table><caption><div>Div inside caption</div></caption><tr><td>A</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("caption div").size());

        String htmlCol = "<table><colgroup><!-- comment --><col class='test'></colgroup><tr><td>B</td></tr></table>";
        Document doc2 = Jsoup.parse(htmlCol);
        Assert.assertEquals(1, doc2.select("col.test").size());
    }

    @Test
    public void testInTableBodyAndRowAndCell() {
        String html = "<table><tr><td>1</td><th>2</th></tr><tr><td>3</td></tr></table>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("tr").size());
        Assert.assertEquals(2, doc.select("td").size());
        Assert.assertEquals(1, doc.select("th").size());

        String autoRow = "<table><td>Direct Cell</td></table>";
        Document doc2 = Jsoup.parse(autoRow);
        Assert.assertEquals(1, doc2.select("td").size());
    }

    @Test
    public void testInSelectStates() {
        String html = "<select>" +
                "<!-- comment -->" +
                "<optgroup label='Group 1'>" +
                "<option value='1'>One</option>" +
                "<option value='2'>Two</option>" +
                "</optgroup>" +
                "<option value='3'>Three</option>" +
                "</select>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("optgroup").size());
        Assert.assertEquals(3, doc.select("option").size());

        String htmlSelectInTable = "<table><tr><td><select><option>A</option><tr><td>Next</td></tr></select></td></tr></table>";
        Document doc2 = Jsoup.parse(htmlSelectInTable);
        Assert.assertEquals(2, doc2.select("tr").size());
    }

    @Test
    public void testInFramesetStates() {
        String html = "<html><frameset rows='50%,50%'><!-- comment -->" +
                "<frame src='frame1.html'>" +
                "<frame src='frame2.html'>" +
                "<noframes><p>No frames</p></noframes>" +
                "</frameset></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("frame").size());
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        String html = "<html><head></head><body>Content</body><!-- comment --></html><!-- outer comment -->";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Content", doc.body().text());

        String htmlAfterBodyText = "<html><body>Hello</body></html> trailing text";
        Document doc2 = Jsoup.parse(htmlAfterBodyText);
        Assert.assertTrue(doc2.body().text().contains("trailing text"));
    }

    @Test
    public void testAfterFramesetAndAfterAfterFrameset() {
        String html = "<html><frameset><frame></frameset><!-- comment --></html><!-- outer -->";
        Document doc = Jsoup.parse(html);
        Assert.assertEquals(1, doc.select("frameset").size());

        String htmlWithNoframes = "<html><frameset><frame></frameset><noframes>No frames</noframes></html>";
        Document doc2 = Jsoup.parse(htmlWithNoframes);
        Assert.assertEquals(1, doc2.select("frameset").size());
    }

    @Test
    public void testNullCharacterHandlingInBodyAndTable() {
        String html = "<body>\u0000Hello</body>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc);

        String htmlTable = "<table>\u0000<tr><td>Cell</td></tr></table>";
        Document doc2 = Jsoup.parse(htmlTable);
        Assert.assertNotNull(doc2);
    }

    @Test
    public void testDirectProcessInvocationCoverage() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new java.io.StringReader("<div>"), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);

        Token.Character nullChar = new Token.Character().data("\u0000");
        Assert.assertFalse(HtmlTreeBuilderState.InBody.process(nullChar, tb));
        Assert.assertFalse(HtmlTreeBuilderState.InTableText.process(nullChar, tb));
        Assert.assertFalse(HtmlTreeBuilderState.InSelect.process(nullChar, tb));

        Token.Doctype doctype = new Token.Doctype();
        Assert.assertFalse(HtmlTreeBuilderState.InBody.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.InTable.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.InSelect.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.InHead.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.BeforeHtml.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.BeforeHead.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.AfterBody.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.InFrameset.process(doctype, tb));
        Assert.assertFalse(HtmlTreeBuilderState.AfterFrameset.process(doctype, tb));

        Token.StartTag dropTag = new Token.StartTag();
        dropTag.name("caption");
        Assert.assertFalse(HtmlTreeBuilderState.InBody.process(dropTag, tb));

        Token.EndTag badEndTag = new Token.EndTag();
        badEndTag.name("invalidTag1234");
        Assert.assertFalse(HtmlTreeBuilderState.BeforeHtml.process(badEndTag, tb));
        Assert.assertFalse(HtmlTreeBuilderState.BeforeHead.process(badEndTag, tb));
        Assert.assertFalse(HtmlTreeBuilderState.InHead.process(badEndTag, tb));
    }
}
