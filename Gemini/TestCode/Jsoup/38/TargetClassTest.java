package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    public void testForeignContentState() {
        Token.Character token = new Token.Character("test");
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Assert.assertTrue(HtmlTreeBuilderState.ForeignContent.process(token, tb));
    }

    @Test
    public void testInitialStateVariants() {
        Document doc1 = Jsoup.parse("   <!-- comment --> <!DOCTYPE html> <html><head></head><body></body></html>");
        Assert.assertEquals(Document.QuirksMode.noQuirks, doc1.quirksMode());

        Document doc2 = Jsoup.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\">");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("   hello world");
        Assert.assertEquals("hello world", doc3.body().text());

        Document doc4 = Jsoup.parse("<!-- comment before doctype --><!DOCTYPE html SYSTEM 'about:legacy-compat'>");
        Assert.assertEquals(Document.QuirksMode.noQuirks, doc4.quirksMode());
    }

    @Test
    public void testBeforeHtmlVariants() {
        Document doc1 = Jsoup.parse("<!DOCTYPE html><!DOCTYPE html><html><body></body></html>");
        Assert.assertNotNull(doc1);

        Document doc2 = Jsoup.parse("<!-- c1 --> <html id='1'> <!-- c2 --> </html>");
        Assert.assertEquals("1", doc2.select("html").attr("id"));

        Document doc3 = Jsoup.parse("</head><p>text</p>");
        Assert.assertEquals("text", doc3.select("p").text());

        Document doc4 = Jsoup.parse("</div><p>text</p>");
        Assert.assertEquals("text", doc4.select("p").text());

        Document doc5 = Jsoup.parse("</body><p>text</p>");
        Assert.assertEquals("text", doc5.select("p").text());

        Document doc6 = Jsoup.parse("</br><p>text</p>");
        Assert.assertEquals("text", doc6.select("p").text());
    }

    @Test
    public void testBeforeHeadVariants() {
        Document doc1 = Jsoup.parse("<html> <!-- comment --> <!DOCTYPE html> <head></head></html>");
        Assert.assertNotNull(doc1.head());

        Document doc2 = Jsoup.parse("<html><html lang='en'><head></head></html>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<html></head><title>Test</title></html>");
        Assert.assertEquals("Test", doc3.title());

        Document doc4 = Jsoup.parse("<html></div><title>Test</title></html>");
        Assert.assertEquals("Test", doc4.title());

        Document doc5 = Jsoup.parse("<html><body>Hello</body></html>");
        Assert.assertEquals("Hello", doc5.body().text());
    }

    @Test
    public void testInHeadVariants() {
        String html = "<html><head>"
                + "<!-- head comment -->"
                + "<!DOCTYPE html>"
                + "<base href='http://example.com/test/' target='_blank'>"
                + "<basefont color='red'>"
                + "<bgsound src='sound.mp3'>"
                + "<command label='cmd'>"
                + "<link rel='stylesheet' href='style.css'>"
                + "<meta charset='UTF-8'>"
                + "<title>Sample Title</title>"
                + "<noframes>No frames supported</noframes>"
                + "<style>body { color: black; }</style>"
                + "<noscript>No script here</noscript>"
                + "<script>var a = 1;</script>"
                + "<head>"
                + "</head><body></body></html>";

        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Sample Title", doc.title());
        Assert.assertEquals("http://example.com/test/", doc.baseUri());
        Assert.assertTrue(doc.head().children().size() > 0);

        Document doc2 = Jsoup.parse("<html><head><html lang='fr'></head><body></body></html>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<html><head></foo><title>Bar</title></head></html>");
        Assert.assertEquals("Bar", doc3.title());

        Document doc4 = Jsoup.parse("<html><head></body><title>Bar</title></head></html>");
        Assert.assertEquals("Bar", doc4.title());
    }

    @Test
    public void testInHeadNoscriptVariants() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        Document doc = parser.parseInput("<html><head><noscript><!-- c --><!DOCTYPE html><html lang='en'><link rel='stylesheet'>foo</noscript></head></html>", "");
        Assert.assertNotNull(doc);

        Document doc2 = parser.parseInput("<html><head><noscript><head></head><noscript></noscript></noscript></head></html>", "");
        Assert.assertNotNull(doc2);

        Document doc3 = parser.parseInput("<html><head><noscript></br><p>text</p></noscript></head></html>", "");
        Assert.assertEquals("text", doc3.body().text());

        Document doc4 = parser.parseInput("<html><head><noscript><meta name='x'>bar</noscript></head></html>", "");
        Assert.assertNotNull(doc4);
    }

    @Test
    public void testAfterHeadVariants() {
        Document doc1 = Jsoup.parse("<html><head></head> <!-- comment --> <body>Hello</body></html>");
        Assert.assertEquals("Hello", doc1.body().text());

        Document doc2 = Jsoup.parse("<html><head></head><frameset cols='50%,50%'><frame src='frame1.html'><frame src='frame2.html'></frameset></html>");
        Assert.assertEquals(2, doc2.select("frame").size());

        Document doc3 = Jsoup.parse("<html><head></head><meta name='foo' content='bar'><body>Content</body></html>");
        Assert.assertEquals("Content", doc3.body().text());
        Assert.assertEquals("bar", doc3.select("meta").attr("content"));

        Document doc4 = Jsoup.parse("<html><head></head><head><body>Content</body></html>");
        Assert.assertEquals("Content", doc4.body().text());

        Document doc5 = Jsoup.parse("<html><head></head></custom><body>Content</body></html>");
        Assert.assertEquals("Content", doc5.body().text());

        Document doc6 = Jsoup.parse("<html><head></head></body>Content</html>");
        Assert.assertEquals("Content", doc6.body().text());

        Document doc7 = Jsoup.parse("<html><head></head><html lang='en'><body>Content</body></html>");
        Assert.assertEquals("Content", doc7.body().text());
    }

    @Test
    public void testInBodyTagsAndClosers() {
        String html = "<!DOCTYPE html><html><body>"
                + "\u0000"
                + "<p>Paragraph 1"
                + "<address>Address</address>"
                + "<h1>Header 1<h2>Header 2</h2></h1>"
                + "<pre>Preformatted</pre>"
                + "<listing>Listing text</listing>"
                + "<form id='f1'><input type='text' name='q'><form id='f2'></form></form>"
                + "<ul><li>Item 1<li>Item 2</ul>"
                + "<dl><dt>Term<dd>Definition</dl>"
                + "<button><button>Nested Button</button></button>"
                + "<a href='http://a.com'>Link 1 <a href='http://b.com'>Link 2</a></a>"
                + "<b>Bold <i>Italic <b>Nested Bold</b></i></b>"
                + "<nobr>Nobr 1 <nobr>Nobr 2</nobr></nobr>"
                + "<applet>Applet content</applet>"
                + "<area><br><embed><img src='img.png'><keygen><wbr>"
                + "<input type='hidden' name='h' value='v'>"
                + "<param name='p' value='v'><source src='s.mp3'><track src='t.vtt'>"
                + "<hr>"
                + "<image src='img.jpg'>"
                + "<textarea>Area content</textarea>"
                + "<xmp><tag>not parsed</tag></xmp>"
                + "<iframe>Frame content</iframe>"
                + "<noembed>No embed</noembed>"
                + "<select><option>Opt 1<option>Opt 2<optgroup label='g'><option>Opt 3</optgroup></select>"
                + "<ruby>Base<rp>(</rp><rt>Ruby text</rt><rp>)</rp></ruby>"
                + "<math><mi>x</mi></math>"
                + "<svg><circle cx='5' cy='5' r='5'/></svg>"
                + "<caption>bad caption in body</caption>"
                + "<sarcasm>sarcastic text</sarcasm>"
                + "</br>"
                + "<customtag>custom content</customtag>"
                + "</body></html>";

        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc);
        Assert.assertEquals(2, doc.select("ul > li").size());
        Assert.assertEquals(1, doc.select("dl > dt").size());
        Assert.assertEquals(1, doc.select("dl > dd").size());
        Assert.assertEquals("Header 2", doc.select("h2").text());
        Assert.assertEquals("Listing text", doc.select("listing").text());
        Assert.assertEquals("Area content", doc.select("textarea").text());
        Assert.assertTrue(doc.select("img").size() >= 2);
    }

    @Test
    public void testInBodyHtmlAndBodyAttributesMerge() {
        Document doc = Jsoup.parse("<html class='c1'><body class='b1'><html class='c2' id='h2'><body class='b2' id='b2'>Content</body></html>");
        Assert.assertTrue(doc.select("html").first().hasClass("c1"));
        Assert.assertEquals("h2", doc.select("html").first().id());
        Assert.assertTrue(doc.body().hasClass("b1"));
        Assert.assertEquals("b2", doc.body().id());
    }

    @Test
    public void testInBodyIsindex() {
        Document doc = Jsoup.parse("<body><isindex action='/search' prompt='Search Here: ' class='is-index'></body>");
        Assert.assertEquals(1, doc.select("form").size());
        Assert.assertEquals("/search", doc.select("form").attr("action"));
        Assert.assertEquals(1, doc.select("input[name=isindex]").size());
    }

    @Test
    public void testInBodyAdoptionAgencyAlgorithm() {
        Document doc1 = Jsoup.parse("<a><b><p>1</a>2</p>3</b>");
        Assert.assertNotNull(doc1);

        Document doc2 = Jsoup.parse("<b>1<p>2</b>3</p>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<a>1<table><tr><td>2</a>3</td></tr></table>");
        Assert.assertNotNull(doc3);

        Document doc4 = Jsoup.parse("<b>1<a href='#'>2<p>3</b>4</p>5</a>");
        Assert.assertNotNull(doc4);

        Document doc5 = Jsoup.parse("<a>1<a>2</a>3</a>");
        Assert.assertEquals(2, doc5.select("a").size());
    }

    @Test
    public void testInBodyEndTagEdgeCases() {
        Document doc1 = Jsoup.parse("<body><div><span>Text</div></span></body>");
        Assert.assertEquals("Text", doc1.select("div").text());

        Document doc2 = Jsoup.parse("<body><form id='f'><div></form>still in div</div></body>");
        Assert.assertEquals("still in div", doc2.select("div").text());

        Document doc3 = Jsoup.parse("<body></p><div>Paragraphs</div></p></body>");
        Assert.assertNotNull(doc3.select("p"));

        Document doc4 = Jsoup.parse("<body></li></dt></dd></h1></h2></h3></h4></h5></h6></body>");
        Assert.assertNotNull(doc4);

        Document doc5 = Jsoup.parse("<body><applet>code</applet></applet></body>");
        Assert.assertNotNull(doc5);
    }

    @Test
    public void testInTableStates() {
        String html = "<table>"
                + "<!-- table comment -->"
                + "<!DOCTYPE html>"
                + "<caption>Table Caption</caption>"
                + "<colgroup><col class='c1'></colgroup>"
                + "<col class='c2'>"
                + "<thead><tr><th>Header</th></tr></thead>"
                + "<tbody><tr><td>Cell 1</td></tr></tbody>"
                + "<tfoot><tr><td>Foot</td></tr></tfoot>"
                + "<tr><td>Direct Cell</td></tr>"
                + "<form id='tblForm'><input type='hidden' name='h' value='1'><input type='text' name='t'></form>"
                + "<style>td { border: 1px solid black; }</style>"
                + "<script>var t = 1;</script>"
                + "<table><tr><td>Nested</td></tr></table>"
                + "text outside cell"
                + "\u0000"
                + "</table>";

        Document doc = Jsoup.parse(html);
        Assert.assertEquals("Table Caption", doc.select("caption").text());
        Assert.assertEquals(1, doc.select("thead").size());
        Assert.assertEquals(2, doc.select("tbody").size());
        Assert.assertEquals(1, doc.select("tfoot").size());
        Assert.assertTrue(doc.text().contains("text outside cell"));
    }

    @Test
    public void testInTableEndTagsAndMisplacedTags() {
        Document doc1 = Jsoup.parse("<table><tr><td>Cell</td></tr></body></html>");
        Assert.assertEquals(1, doc1.select("table").size());

        Document doc2 = Jsoup.parse("<table></col></colgroup></caption><tbody></tbody></table>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<table><tr><td>1</td></tr><tr><th>2</th></tr></table>");
        Assert.assertEquals(2, doc3.select("tr").size());

        Document doc4 = Jsoup.parse("<table><caption>Caption<col></caption><tr><td>Cell</td></tr></table>");
        Assert.assertNotNull(doc4);

        Document doc5 = Jsoup.parse("<table><caption></col></colgroup></caption><tr><td>Cell</td></tr></table>");
        Assert.assertNotNull(doc5);
    }

    @Test
    public void testInColumnGroupVariants() {
        Document doc1 = Jsoup.parse("<table><colgroup> <!-- col comment --> <col width='10'><col width='20'></colgroup></table>");
        Assert.assertEquals(2, doc1.select("col").size());

        Document doc2 = Jsoup.parse("<table><colgroup><!DOCTYPE html><html lang='en'><col></colgroup></table>");
        Assert.assertEquals(1, doc2.select("col").size());

        Document doc3 = Jsoup.parse("<table><colgroup></other><col></table>");
        Assert.assertEquals(1, doc3.select("col").size());
    }

    @Test
    public void testInTableBodyAndRowAndCellVariants() {
        Document doc1 = Jsoup.parse("<table><tbody><th>Header without tr</th><td>Cell without tr</td></tbody></table>");
        Assert.assertEquals(1, doc1.select("tr").size());

        Document doc2 = Jsoup.parse("<table><tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody></table>");
        Assert.assertEquals(2, doc2.select("td").size());

        Document doc3 = Jsoup.parse("<table><tr><td>Cell 1<th>Header 2</td></tr></table>");
        Assert.assertEquals(1, doc3.select("td").size());
        Assert.assertEquals(1, doc3.select("th").size());

        Document doc4 = Jsoup.parse("<table><tbody><tr><td>Cell</td></tr><caption>Caption after tbody</caption></table>");
        Assert.assertEquals(1, doc4.select("caption").size());

        Document doc5 = Jsoup.parse("<table><tr><td>Cell</td></tr><col></table>");
        Assert.assertNotNull(doc5);

        Document doc6 = Jsoup.parse("<table></td></th></tr></table>");
        Assert.assertNotNull(doc6);
    }

    @Test
    public void testInSelectAndInSelectInTableVariants() {
        String html = "<select>"
                + "<!-- select comment -->"
                + "<!DOCTYPE html>"
                + "\u0000"
                + "<html lang='en'>"
                + "<option value='1'>One</option>"
                + "<optgroup label='group'>"
                + "<option value='2'>Two</option>"
                + "</optgroup>"
                + "<script>var s = 1;</script>"
                + "<input type='text'>"
                + "</select>";

        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("select > option, select > optgroup > option").size());

        Document doc2 = Jsoup.parse("<select><optgroup><option>1</option></optgroup></optgroup></option></select>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<table><tr><td><select><option>Table Select</option><tr><td>Next</td></tr></select></td></tr></table>");
        Assert.assertNotNull(doc3);

        Document doc4 = Jsoup.parse("<table><tr><td><select><option>1</option></table></td></tr></table>");
        Assert.assertNotNull(doc4);

        Document doc5 = Jsoup.parse("<select><select><option>Nested</option></select></select>");
        Assert.assertNotNull(doc5);
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        Document doc1 = Jsoup.parse("<html><head></head><body>Hello</body><!-- comment after body --></html>");
        Assert.assertEquals("Hello", doc1.body().text());

        Document doc2 = Jsoup.parse("<html><body>Hello</body></html><!-- comment after html -->");
        Assert.assertEquals("Hello", doc2.body().text());

        Document doc3 = Jsoup.parse("<html><body>Hello</body></html>   ");
        Assert.assertEquals("Hello", doc3.body().text());

        Document doc4 = Jsoup.parse("<html><body>Hello</body></html><!DOCTYPE html>");
        Assert.assertEquals("Hello", doc4.body().text());

        Document doc5 = Jsoup.parse("<html><body>Hello</body></html><p>Extra after html</p>");
        Assert.assertTrue(doc5.body().text().contains("Extra after html"));
    }

    @Test
    public void testInFramesetAndAfterFramesetVariants() {
        String html = "<!DOCTYPE html><html>"
                + "<head><title>Frameset Test</title></head>"
                + "<!-- comment before frameset -->"
                + "<frameset rows='50%,50%'>"
                + "<!-- comment in frameset -->"
                + "<frame src='top.html'>"
                + "<frame src='bottom.html'>"
                + "<noframes><p>No frames support</p></noframes>"
                + "</frameset>"
                + "<!-- comment after frameset -->"
                + "</html>"
                + "<!-- comment after after frameset -->";

        Document doc = Jsoup.parse(html);
        Assert.assertEquals(2, doc.select("frame").size());
        Assert.assertEquals("Frameset Test", doc.title());

        Document doc2 = Jsoup.parse("<html><frameset><frame src='1.html'></frameset><noframes>No</noframes></html>");
        Assert.assertNotNull(doc2);

        Document doc3 = Jsoup.parse("<html><frameset><frame src='1.html'></frameset>   </html>");
        Assert.assertNotNull(doc3);

        Document doc4 = Jsoup.parse("<html><frameset><frame src='1.html'></frameset></html><noframes>No</noframes>");
        Assert.assertNotNull(doc4);
    }

    @Test
    public void testFragmentParsingAcrossStates() {
        Element contextBody = new Element(Tag.valueOf("body"), "");
        List<org.jsoup.nodes.Node> nodes1 = Parser.parseFragment("<div>Hello</div><p>World</p>", contextBody, "");
        Assert.assertEquals(2, nodes1.size());

        Element contextTable = new Element(Tag.valueOf("table"), "");
        List<org.jsoup.nodes.Node> nodes2 = Parser.parseFragment("<tr><td>Cell</td></tr>", contextTable, "");
        Assert.assertEquals(1, nodes2.size());

        Element contextSelect = new Element(Tag.valueOf("select"), "");
        List<org.jsoup.nodes.Node> nodes3 = Parser.parseFragment("<option>1</option><option>2</option>", contextSelect, "");
        Assert.assertEquals(2, nodes3.size());

        Element contextFrameset = new Element(Tag.valueOf("frameset"), "");
        List<org.jsoup.nodes.Node> nodes4 = Parser.parseFragment("<frame src='a.html'>", contextFrameset, "");
        Assert.assertEquals(1, nodes4.size());

        Element contextColgroup = new Element(Tag.valueOf("colgroup"), "");
        List<org.jsoup.nodes.Node> nodes5 = Parser.parseFragment("<col width='50'>", contextColgroup, "");
        Assert.assertEquals(1, nodes5.size());
    }

    @Test
    public void testTextStateHandling() {
        Document doc1 = Jsoup.parse("<script>if (a < b) { console.log('hello'); }</script>");
        Assert.assertTrue(doc1.select("script").data().contains("if (a < b)"));

        Document doc2 = Jsoup.parse("<style>body > div { color: red; }</style>");
        Assert.assertTrue(doc2.select("style").data().contains("body > div"));

        Document doc3 = Jsoup.parse("<textarea>Line 1\nLine 2</textarea>");
        Assert.assertEquals("Line 1\nLine 2", doc3.select("textarea").text());

        Document doc4 = Jsoup.parse("<title>Simple Title</title>");
        Assert.assertEquals("Simple Title", doc4.title());

        Document doc5 = Jsoup.parse("<script>Unclosed script");
        Assert.assertTrue(doc5.select("script").data().contains("Unclosed script"));
    }

    @Test
    public void testFosterParentingCharactersAndElements() {
        Document doc1 = Jsoup.parse("<table>Hello<tr><td>World</td></tr></table>");
        Assert.assertTrue(doc1.text().contains("Hello"));
        Assert.assertTrue(doc1.text().contains("World"));

        Document doc2 = Jsoup.parse("<table><b>Bold</b><tr><td>Cell</td></tr></table>");
        Elements b = doc2.select("b");
        Assert.assertEquals("Bold", b.text());
        Assert.assertTrue(doc2.body().children().contains(b.first()));

        Document doc3 = Jsoup.parse("<table><input type='text' name='foo'><tr><td>Cell</td></tr></table>");
        Assert.assertEquals(1, doc3.select("input[name=foo]").size());
    }

    @Test
    public void testPlaintextHandling() {
        Document doc = Jsoup.parse("<p>Before</p><plaintext><p>Not a real tag</p><b>Still plaintext</b>");
        Assert.assertTrue(doc.text().contains("<p>Not a real tag</p>"));
    }
}
