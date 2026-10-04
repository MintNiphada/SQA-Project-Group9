package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

public class TreeBuilderStateTest {

    private TreeBuilder tb;

    @Before
    public void setUp() {
        tb = new TreeBuilder();
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(100));
    }

    @Test
    public void testEnumValues() {
        for (TreeBuilderState state : TreeBuilderState.values()) {
            assertNotNull(state);
            assertEquals(state, TreeBuilderState.valueOf(state.name()));
        }
    }

    @Test
    public void testForeignContentState() {
        Token.Comment comment = new Token.Comment();
        comment.getData().append("test");
        assertTrue(TreeBuilderState.ForeignContent.process(comment, tb));
    }

    @Test
    public void testInitialState() {
        // Whitespace token
        Token.Character ws = new Token.Character("   \t\n\r");
        assertTrue(TreeBuilderState.Initial.process(ws, tb));

        // Comment token
        Token.Comment comment = new Token.Comment();
        comment.getData().append("comment");
        assertTrue(TreeBuilderState.Initial.process(comment, tb));
        assertEquals(1, tb.getDocument().childNodeSize());

        // Doctype token without force quirks
        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        doctype.publicIdentifier.append("pub");
        doctype.systemIdentifier.append("sys");
        doctype.forceQuirks = false;
        assertTrue(TreeBuilderState.Initial.process(doctype, tb));
        assertEquals(Document.QuirksMode.noQuirks, tb.getDocument().quirksMode());
        assertEquals(TreeBuilderState.BeforeHtml, tb.state());

        // Doctype token with force quirks
        TreeBuilder tbQuirks = new TreeBuilder();
        tbQuirks.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(100));
        doctype.forceQuirks = true;
        assertTrue(TreeBuilderState.Initial.process(doctype, tbQuirks));
        assertEquals(Document.QuirksMode.quirks, tbQuirks.getDocument().quirksMode());

        // Other token transition to BeforeHtml
        TreeBuilder tbOther = new TreeBuilder();
        tbOther.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(100));
        Token.StartTag startTag = new Token.StartTag("div");
        assertTrue(TreeBuilderState.Initial.process(startTag, tbOther));
    }

    @Test
    public void testBeforeHtmlState() {
        // Doctype error
        Token.Doctype dt = new Token.Doctype();
        assertFalse(TreeBuilderState.BeforeHtml.process(dt, tb));

        // Comment
        Token.Comment c = new Token.Comment();
        c.getData().append("comment");
        assertTrue(TreeBuilderState.BeforeHtml.process(c, tb));

        // Whitespace
        Token.Character ws = new Token.Character("  ");
        assertTrue(TreeBuilderState.BeforeHtml.process(ws, tb));

        // Start tag html
        Token.StartTag htmlTag = new Token.StartTag("html");
        assertTrue(TreeBuilderState.BeforeHtml.process(htmlTag, tb));
        assertEquals(TreeBuilderState.BeforeHead, tb.state());

        // End tag head/body/html/br
        TreeBuilder tb2 = new TreeBuilder();
        tb2.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(100));
        Token.EndTag endHead = new Token.EndTag("head");
        assertTrue(TreeBuilderState.BeforeHtml.process(endHead, tb2));

        // Invalid end tag
        TreeBuilder tb3 = new TreeBuilder();
        tb3.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(100));
        Token.EndTag endDiv = new Token.EndTag("div");
        assertFalse(TreeBuilderState.BeforeHtml.process(endDiv, tb3));
    }

    @Test
    public void testBeforeHeadState() {
        Token.Character ws = new Token.Character(" ");
        assertTrue(TreeBuilderState.BeforeHead.process(ws, tb));

        Token.Comment c = new Token.Comment();
        c.getData().append("comment");
        assertTrue(TreeBuilderState.BeforeHead.process(c, tb));

        Token.Doctype dt = new Token.Doctype();
        assertFalse(TreeBuilderState.BeforeHead.process(dt, tb));

        Token.StartTag headTag = new Token.StartTag("head");
        assertTrue(TreeBuilderState.BeforeHead.process(headTag, tb));
        assertEquals(TreeBuilderState.InHead, tb.state());
        assertNotNull(tb.getHeadElement());

        // Invalid end tag
        Token.EndTag endDiv = new Token.EndTag("div");
        assertFalse(TreeBuilderState.BeforeHead.process(endDiv, tb));

        // End tag head/body/html/br
        Token.EndTag endBr = new Token.EndTag("br");
        assertTrue(TreeBuilderState.BeforeHead.process(endBr, tb));
    }

    @Test
    public void testInHeadState() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));

        // Whitespace
        Token.Character ws = new Token.Character(" ");
        assertTrue(TreeBuilderState.InHead.process(ws, tb));

        // Comment
        Token.Comment c = new Token.Comment();
        c.getData().append("com");
        assertTrue(TreeBuilderState.InHead.process(c, tb));

        // Doctype error
        Token.Doctype dt = new Token.Doctype();
        assertFalse(TreeBuilderState.InHead.process(dt, tb));

        // Start tag html
        Token.StartTag htmlStart = new Token.StartTag("html");
        assertTrue(TreeBuilderState.InHead.process(htmlStart, tb));

        // Start tag base with href
        Attributes baseAttrs = new Attributes();
        baseAttrs.put("href", "http://jsoup.org");
        Token.StartTag baseTag = new Token.StartTag("base", baseAttrs);
        assertTrue(TreeBuilderState.InHead.process(baseTag, tb));
        assertEquals("http://jsoup.org", tb.getBaseUri());

        // Start tags: meta, title, style, noscript, script
        assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("meta"), tb));
        assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("title"), tb));
        tb.transition(TreeBuilderState.InHead);

        assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("style"), tb));
        tb.transition(TreeBuilderState.InHead);

        assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("noscript"), tb));
        assertEquals(TreeBuilderState.InHeadNoscript, tb.state());
        tb.transition(TreeBuilderState.InHead);

        assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("script"), tb));
        assertEquals(TreeBuilderState.Text, tb.state());
        tb.transition(TreeBuilderState.InHead);

        // Start tag head (error)
        assertFalse(TreeBuilderState.InHead.process(new Token.StartTag("head"), tb));

        // End tags: head, body, invalid
        assertFalse(TreeBuilderState.InHead.process(new Token.EndTag("span"), tb));
        assertTrue(TreeBuilderState.InHead.process(new Token.EndTag("head"), tb));
        assertEquals(TreeBuilderState.AfterHead, tb.state());
    }

    @Test
    public void testInHeadNoscriptState() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.StartTag("noscript"));

        // Doctype
        TreeBuilderState.InHeadNoscript.process(new Token.Doctype(), tb);

        // StartTag html
        assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.StartTag("html"), tb));

        // Whitespace, Comment, supported start tags
        assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.Character(" "), tb));
        Token.Comment comment = new Token.Comment();
        comment.getData().append("test");
        assertTrue(TreeBuilderState.InHeadNoscript.process(comment, tb));
        assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.StartTag("link"), tb));

        // EndTag br
        assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.EndTag("br"), tb));

        // StartTag head/noscript error
        assertFalse(TreeBuilderState.InHeadNoscript.process(new Token.StartTag("head"), tb));
        assertFalse(TreeBuilderState.InHeadNoscript.process(new Token.EndTag("div"), tb));

        // EndTag noscript
        assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.EndTag("noscript"), tb));
        assertEquals(TreeBuilderState.InHead, tb.state());
    }

    @Test
    public void testAfterHeadState() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.EndTag("head"));
        assertEquals(TreeBuilderState.AfterHead, tb.state());

        // Whitespace & comment & doctype
        assertTrue(TreeBuilderState.AfterHead.process(new Token.Character(" "), tb));
        Token.Comment c = new Token.Comment();
        c.getData().append("after head");
        assertTrue(TreeBuilderState.AfterHead.process(c, tb));
        TreeBuilderState.AfterHead.process(new Token.Doctype(), tb);

        // Head start tag error
        assertFalse(TreeBuilderState.AfterHead.process(new Token.StartTag("head"), tb));

        // Base/style tag in AfterHead
        assertTrue(TreeBuilderState.AfterHead.process(new Token.StartTag("meta"), tb));

        // End tag invalid
        assertFalse(TreeBuilderState.AfterHead.process(new Token.EndTag("div"), tb));

        // StartTag frameset
        tb.transition(TreeBuilderState.AfterHead);
        assertTrue(TreeBuilderState.AfterHead.process(new Token.StartTag("frameset"), tb));
        assertEquals(TreeBuilderState.InFrameset, tb.state());
    }

    @Test
    public void testInBodyTagsAndAdoptionAgency() {
        String html = "<p>para 1<b>bold <i>italic</p> still italic</i></b>" +
                "<form action='foo'><p><form>nested form</form></p></form>" +
                "<h1>h1<h2>h2</h2></h1>" +
                "<ul><li>item 1<li>item 2</ul>" +
                "<dl><dt>dt1<dd>dd1</dl>" +
                "<button>btn<button>btn2</button></button>" +
                "<a>link 1 <a>link 2</a>" +
                "<nobr>nobr 1 <nobr>nobr 2</nobr></nobr>" +
                "<applet>applet content</applet>" +
                "<input type='hidden'><input type='text'>" +
                "<hr><image src='img.png'>" +
                "<isindex action='search' prompt='find: '>" +
                "<textarea>text</textarea><xmp>raw</xmp><iframe>frame</iframe><noembed>embed</noembed>" +
                "<select><option>1<optgroup label='g'><option>2</optgroup></select>" +
                "<ruby>rb<rp>(</rp><rt>rt</rt><rp>)</rp></ruby>" +
                "<math><mi>x</mi></math><svg><circle/></svg>" +
                "<table><caption>cap</caption><colgroup><col></colgroup><tbody><tr><th>th<td>td</td></tr></tbody></table>";

        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertNotNull(doc.body());
    }

    @Test
    public void testInBodyNullCharacter() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        Token.Character nullChar = new Token.Character(String.valueOf(0x0000));
        assertFalse(TreeBuilderState.InBody.process(nullChar, tb));
    }

    @Test
    public void testInBodyInvalidTableTags() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        assertFalse(TreeBuilderState.InBody.process(new Token.StartTag("td"), tb));
        assertFalse(TreeBuilderState.InBody.process(new Token.StartTag("tr"), tb));
        assertFalse(TreeBuilderState.InBody.process(new Token.StartTag("tbody"), tb));
    }

    @Test
    public void testInBodyEndTags() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));

        // End tag p when not in button scope
        assertTrue(TreeBuilderState.InBody.process(new Token.EndTag("p"), tb));

        // End tag li / dd / dt when not in scope
        assertFalse(TreeBuilderState.InBody.process(new Token.EndTag("li"), tb));
        assertFalse(TreeBuilderState.InBody.process(new Token.EndTag("dd"), tb));
        assertFalse(TreeBuilderState.InBody.process(new Token.EndTag("dt"), tb));
        assertFalse(TreeBuilderState.InBody.process(new Token.EndTag("h1"), tb));

        // End tag br
        assertFalse(TreeBuilderState.InBody.process(new Token.EndTag("br"), tb));

        // End tag body
        assertTrue(TreeBuilderState.InBody.process(new Token.EndTag("body"), tb));
        assertEquals(TreeBuilderState.AfterBody, tb.state());
    }

    @Test
    public void testInTableAndInTableText() {
        String html = "<table>" +
                "pending text" +
                "<!-- table comment -->" +
                "<colgroup><col></colgroup>" +
                "<thead><tr><th>head</th></tr></thead>" +
                "<tbody><tr><td>data</td></tr></tbody>" +
                "<tfoot><tr><td>foot</td></tr></tfoot>" +
                "<form>form in table</form>" +
                "<script>var a = 1;</script>" +
                "<style>td { color: red; }</style>" +
                "<input type='hidden' name='h' value='v'>" +
                "<tr><td>cell</td></tr>" +
                "</table>";

        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("table").first());
        assertNotNull(doc.select("td").first());
    }

    @Test
    public void testInTableNullCharAndDoctype() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        assertEquals(TreeBuilderState.InTable, tb.state());

        // Doctype in table
        assertFalse(TreeBuilderState.InTable.process(new Token.Doctype(), tb));

        // Transition to InTableText
        Token.Character c = new Token.Character("abc");
        assertTrue(TreeBuilderState.InTable.process(c, tb));
        assertEquals(TreeBuilderState.InTableText, tb.state());

        // Null character in InTableText
        Token.Character nullChar = new Token.Character(String.valueOf(0x0000));
        assertFalse(TreeBuilderState.InTableText.process(nullChar, tb));
    }

    @Test
    public void testInCaptionAndInColumnGroup() {
        String html = "<table><caption>Caption Text<p>inside caption</p></caption><colgroup><col class='c1'></colgroup></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("caption").first());
        assertEquals("Caption Text inside caption", doc.select("caption").first().text());

        // InCaption with invalid end tags
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("caption"));
        assertEquals(TreeBuilderState.InCaption, tb.state());
        assertFalse(TreeBuilderState.InCaption.process(new Token.EndTag("body"), tb));

        // InColumnGroup with invalid doctype & comment
        tb.transition(TreeBuilderState.InColumnGroup);
        TreeBuilderState.InColumnGroup.process(new Token.Doctype(), tb);
        Token.Comment comment = new Token.Comment();
        comment.getData().append("col comment");
        assertTrue(TreeBuilderState.InColumnGroup.process(comment, tb));
    }

    @Test
    public void testInTableBodyInRowInCell() {
        String html = "<table><tr><td>Cell 1<th>Cell 2</td><td>Cell 3</tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(3, doc.select("td, th").size());

        // InRow invalid end tags
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("tbody"));
        tb.process(new Token.StartTag("tr"));
        assertEquals(TreeBuilderState.InRow, tb.state());
        assertFalse(TreeBuilderState.InRow.process(new Token.EndTag("body"), tb));

        // InCell invalid end tags
        tb.process(new Token.StartTag("td"));
        assertEquals(TreeBuilderState.InCell, tb.state());
        assertFalse(TreeBuilderState.InCell.process(new Token.EndTag("body"), tb));
    }

    @Test
    public void testInSelectAndInSelectInTable() {
        String html = "<select><option>1<optgroup label='a'><option>2</optgroup><option>3</select>" +
                "<table><tr><td><select><option>Table Select</option><tr><td>next</tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals(2, doc.select("select").size());

        // Direct token testing on InSelect
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("select"));
        assertEquals(TreeBuilderState.InSelect, tb.state());

        assertFalse(TreeBuilderState.InSelect.process(new Token.Doctype(), tb));
        Token.Comment comment = new Token.Comment();
        comment.getData().append("select comment");
        assertTrue(TreeBuilderState.InSelect.process(comment, tb));

        Token.Character nullChar = new Token.Character(String.valueOf(0x0000));
        assertFalse(TreeBuilderState.InSelect.process(nullChar, tb));
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.EndTag("body"));
        assertEquals(TreeBuilderState.AfterBody, tb.state());

        // Comment after body
        Token.Comment comment = new Token.Comment();
        comment.getData().append("after body comment");
        assertTrue(TreeBuilderState.AfterBody.process(comment, tb));

        // Doctype after body (error)
        assertFalse(TreeBuilderState.AfterBody.process(new Token.Doctype(), tb));

        // End tag html transitions to AfterAfterBody
        assertTrue(TreeBuilderState.AfterBody.process(new Token.EndTag("html"), tb));
        assertEquals(TreeBuilderState.AfterAfterBody, tb.state());

        // Comment in AfterAfterBody
        assertTrue(TreeBuilderState.AfterAfterBody.process(comment, tb));

        // Whitespace in AfterAfterBody
        assertTrue(TreeBuilderState.AfterAfterBody.process(new Token.Character(" \n"), tb));

        // EOF in AfterAfterBody
        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.AfterAfterBody.process(eof, tb));
    }

    @Test
    public void testFramesetStates() {
        String html = "<html><head><title>Frameset</title></head><frameset cols='50%,50%'><frame src='frame1.html'><frame src='frame2.html'><noframes>No frames</noframes></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
        assertEquals(2, doc.select("frame").size());

        // InFrameset direct checks
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("frameset"));
        assertEquals(TreeBuilderState.InFrameset, tb.state());

        assertFalse(TreeBuilderState.InFrameset.process(new Token.Doctype(), tb));
        Token.Comment comment = new Token.Comment();
        comment.getData().append("frameset comment");
        assertTrue(TreeBuilderState.InFrameset.process(comment, tb));
        assertTrue(TreeBuilderState.InFrameset.process(new Token.Character(" "), tb));

        // Transition from InFrameset to AfterFrameset
        assertTrue(TreeBuilderState.InFrameset.process(new Token.EndTag("frameset"), tb));
        assertEquals(TreeBuilderState.AfterFrameset, tb.state());

        // In AfterFrameset
        assertTrue(TreeBuilderState.AfterFrameset.process(new Token.Character(" "), tb));
        assertTrue(TreeBuilderState.AfterFrameset.process(comment, tb));
        assertFalse(TreeBuilderState.AfterFrameset.process(new Token.Doctype(), tb));

        // Transition to AfterAfterFrameset
        assertTrue(TreeBuilderState.AfterFrameset.process(new Token.EndTag("html"), tb));
        assertEquals(TreeBuilderState.AfterAfterFrameset, tb.state());

        // In AfterAfterFrameset
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(comment, tb));
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(new Token.Character(" \t"), tb));
        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.AfterAfterFrameset.process(eof, tb));
    }

    @Test
    public void testTextState() {
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("script"));
        assertEquals(TreeBuilderState.Text, tb.state());

        // Character insertion in Text state
        Token.Character c = new Token.Character("alert('test');");
        assertTrue(TreeBuilderState.Text.process(c, tb));

        // End tag script pops and transitions back
        assertTrue(TreeBuilderState.Text.process(new Token.EndTag("script"), tb));
        assertEquals(TreeBuilderState.InBody, tb.state());

        // Text state EOF
        tb.process(new Token.StartTag("script"));
        assertEquals(TreeBuilderState.Text, tb.state());
        Token.EOF eof = new Token.EOF();
        assertTrue(TreeBuilderState.Text.process(eof, tb));
    }
}
