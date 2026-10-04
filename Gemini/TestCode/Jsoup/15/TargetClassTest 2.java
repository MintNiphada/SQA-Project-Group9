package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

public class TreeBuilderStateTest {

    private TreeBuilder createTreeBuilder() {
        TreeBuilder tb = new TreeBuilder();
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(100));
        return tb;
    }

    @Test
    public void testEnumValuesAndValueOf() {
        TreeBuilderState[] states = TreeBuilderState.values();
        Assert.assertTrue(states.length >= 23);
        for (TreeBuilderState state : states) {
            Assert.assertEquals(state, TreeBuilderState.valueOf(state.name()));
        }
    }

    @Test
    public void testForeignContentState() {
        TreeBuilder tb = createTreeBuilder();
        Token.Character c = new Token.Character("content");
        Assert.assertTrue(TreeBuilderState.ForeignContent.process(c, tb));
    }

    @Test
    public void testInitialState() {
        TreeBuilder tb = createTreeBuilder();

        // 1. Whitespace token (ignored)
        Token.Character ws = new Token.Character("   \t\n");
        Assert.assertTrue(TreeBuilderState.Initial.process(ws, tb));

        // 2. Comment token
        Token.Comment comment = new Token.Comment();
        comment.getData().append("test comment");
        Assert.assertTrue(TreeBuilderState.Initial.process(comment, tb));
        Assert.assertEquals(1, tb.getDocument().childNodeSize());

        // 3. Doctype with force quirks
        Token.Doctype doctype = new Token.Doctype();
        doctype.getName().append("html");
        doctype.getPublicIdentifier().append("public");
        doctype.getSystemIdentifier().append("system");
        doctype.forceQuirks = true;
        Assert.assertTrue(TreeBuilderState.Initial.process(doctype, tb));
        Assert.assertEquals(Document.QuirksMode.quirks, tb.getDocument().quirksMode());
        Assert.assertEquals(TreeBuilderState.BeforeHtml, tb.state());

        // 4. Fallback/anything else in Initial
        TreeBuilder tb2 = createTreeBuilder();
        Token.StartTag startTag = new Token.StartTag("div");
        Assert.assertTrue(TreeBuilderState.Initial.process(startTag, tb2));
    }

    @Test
    public void testBeforeHtmlState() {
        TreeBuilder tb = createTreeBuilder();

        // Doctype in BeforeHtml -> error and returns false
        Token.Doctype doctype = new Token.Doctype();
        Assert.assertFalse(TreeBuilderState.BeforeHtml.process(doctype, tb));

        // Comment
        Token.Comment comment = new Token.Comment();
        comment.getData().append("comment in before html");
        Assert.assertTrue(TreeBuilderState.BeforeHtml.process(comment, tb));

        // Whitespace
        Token.Character ws = new Token.Character("  ");
        Assert.assertTrue(TreeBuilderState.BeforeHtml.process(ws, tb));

        // Start tag html
        Token.StartTag startHtml = new Token.StartTag("html");
        Assert.assertTrue(TreeBuilderState.BeforeHtml.process(startHtml, tb));
        Assert.assertEquals(TreeBuilderState.BeforeHead, tb.state());

        // End tag ("head", "body", "html", "br")
        TreeBuilder tb2 = createTreeBuilder();
        Token.EndTag endHead = new Token.EndTag("head");
        Assert.assertTrue(TreeBuilderState.BeforeHtml.process(endHead, tb2));

        // Other end tag -> error and returns false
        TreeBuilder tb3 = createTreeBuilder();
        Token.EndTag endDiv = new Token.EndTag("div");
        Assert.assertFalse(TreeBuilderState.BeforeHtml.process(endDiv, tb3));

        // Anything else (e.g. Start tag body)
        TreeBuilder tb4 = createTreeBuilder();
        Token.StartTag startBody = new Token.StartTag("body");
        Assert.assertTrue(TreeBuilderState.BeforeHtml.process(startBody, tb4));
    }

    @Test
    public void testBeforeHeadState() {
        TreeBuilder tb = createTreeBuilder();
        tb.transition(TreeBuilderState.BeforeHead);

        // Whitespace
        Assert.assertTrue(TreeBuilderState.BeforeHead.process(new Token.Character(" \t"), tb));

        // Comment
        Token.Comment comment = new Token.Comment();
        comment.getData().append("head comment");
        Assert.assertTrue(TreeBuilderState.BeforeHead.process(comment, tb));

        // Doctype -> error and false
        Assert.assertFalse(TreeBuilderState.BeforeHead.process(new Token.Doctype(), tb));

        // Start tag html
        Assert.assertTrue(TreeBuilderState.BeforeHead.process(new Token.StartTag("html"), tb));

        // Start tag head
        Token.StartTag headTag = new Token.StartTag("head");
        Assert.assertTrue(TreeBuilderState.BeforeHead.process(headTag, tb));
        Assert.assertEquals(TreeBuilderState.InHead, tb.state());
        Assert.assertNotNull(tb.getHeadElement());

        // End tag ("head", "body", "html", "br")
        TreeBuilder tb2 = createTreeBuilder();
        tb2.process(new Token.StartTag("html"));
        tb2.transition(TreeBuilderState.BeforeHead);
        Assert.assertTrue(TreeBuilderState.BeforeHead.process(new Token.EndTag("body"), tb2));

        // Other end tag -> error and false
        TreeBuilder tb3 = createTreeBuilder();
        tb3.process(new Token.StartTag("html"));
        tb3.transition(TreeBuilderState.BeforeHead);
        Assert.assertFalse(TreeBuilderState.BeforeHead.process(new Token.EndTag("div"), tb3));

        // Anything else (e.g. Start tag p)
        TreeBuilder tb4 = createTreeBuilder();
        tb4.process(new Token.StartTag("html"));
        tb4.transition(TreeBuilderState.BeforeHead);
        Assert.assertTrue(TreeBuilderState.BeforeHead.process(new Token.StartTag("p"), tb4));
    }

    @Test
    public void testInHeadState() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));

        // Whitespace
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.Character("  "), tb));

        // Comment
        Token.Comment comment = new Token.Comment();
        comment.getData().append("in head comment");
        Assert.assertTrue(TreeBuilderState.InHead.process(comment, tb));

        // Doctype -> error and false
        Assert.assertFalse(TreeBuilderState.InHead.process(new Token.Doctype(), tb));

        // Start tag html
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("html"), tb));

        // Start tags: base (with href), basefont, bgsound, command, link
        Token.StartTag baseTag = new Token.StartTag("base");
        baseTag.attributes.put("href", "http://example.com/base/");
        Assert.assertTrue(TreeBuilderState.InHead.process(baseTag, tb));
        Assert.assertEquals("http://example.com/base/", tb.getBaseUri());

        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("link"), tb));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("meta"), tb));

        // Title (handleRcData)
        TreeBuilder tbTitle = createTreeBuilder();
        tbTitle.process(new Token.StartTag("html"));
        tbTitle.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("title"), tbTitle));
        Assert.assertEquals(TreeBuilderState.Text, tbTitle.state());

        // Style & noframes (handleRawtext)
        TreeBuilder tbStyle = createTreeBuilder();
        tbStyle.process(new Token.StartTag("html"));
        tbStyle.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("style"), tbStyle));
        Assert.assertEquals(TreeBuilderState.Text, tbStyle.state());

        // Noscript
        TreeBuilder tbNoscript = createTreeBuilder();
        tbNoscript.process(new Token.StartTag("html"));
        tbNoscript.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("noscript"), tbNoscript));
        Assert.assertEquals(TreeBuilderState.InHeadNoscript, tbNoscript.state());

        // Script
        TreeBuilder tbScript = createTreeBuilder();
        tbScript.process(new Token.StartTag("html"));
        tbScript.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("script"), tbScript));
        Assert.assertEquals(TreeBuilderState.Text, tbScript.state());

        // Head inside head -> error and false
        Assert.assertFalse(TreeBuilderState.InHead.process(new Token.StartTag("head"), tb));

        // Anything else start tag -> pops head and continues
        TreeBuilder tbDiv = createTreeBuilder();
        tbDiv.process(new Token.StartTag("html"));
        tbDiv.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.StartTag("div"), tbDiv));

        // End tag head
        TreeBuilder tbEndHead = createTreeBuilder();
        tbEndHead.process(new Token.StartTag("html"));
        tbEndHead.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.EndTag("head"), tbEndHead));
        Assert.assertEquals(TreeBuilderState.AfterHead, tbEndHead.state());

        // End tag body/html/br
        TreeBuilder tbEndBody = createTreeBuilder();
        tbEndBody.process(new Token.StartTag("html"));
        tbEndBody.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.EndTag("body"), tbEndBody));

        // Invalid end tag
        TreeBuilder tbEndSpan = createTreeBuilder();
        tbEndSpan.process(new Token.StartTag("html"));
        tbEndSpan.process(new Token.StartTag("head"));
        Assert.assertFalse(TreeBuilderState.InHead.process(new Token.EndTag("span"), tbEndSpan));

        // Default (EOF)
        TreeBuilder tbEof = createTreeBuilder();
        tbEof.process(new Token.StartTag("html"));
        tbEof.process(new Token.StartTag("head"));
        Assert.assertTrue(TreeBuilderState.InHead.process(new Token.EOF(), tbEof));
    }

    @Test
    public void testInHeadNoscriptState() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.StartTag("noscript"));

        // Doctype
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.Doctype(), tb));

        // Start tag html
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.StartTag("html"), tb));

        // Whitespace, comment, link/meta/style
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.Character(" "), tb));
        Token.Comment c = new Token.Comment();
        c.getData().append("comment");
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(c, tb));
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.StartTag("link"), tb));

        // End tag br
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.EndTag("br"), tb));

        // Start tag head/noscript or invalid end tag -> error and false
        TreeBuilder tb2 = createTreeBuilder();
        tb2.process(new Token.StartTag("html"));
        tb2.process(new Token.StartTag("head"));
        tb2.process(new Token.StartTag("noscript"));
        Assert.assertFalse(TreeBuilderState.InHeadNoscript.process(new Token.StartTag("head"), tb2));
        Assert.assertFalse(TreeBuilderState.InHeadNoscript.process(new Token.EndTag("span"), tb2));

        // End tag noscript
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.EndTag("noscript"), tb2));
        Assert.assertEquals(TreeBuilderState.InHead, tb2.state());

        // Anything else
        TreeBuilder tb3 = createTreeBuilder();
        tb3.process(new Token.StartTag("html"));
        tb3.process(new Token.StartTag("head"));
        tb3.process(new Token.StartTag("noscript"));
        Assert.assertTrue(TreeBuilderState.InHeadNoscript.process(new Token.Character("some text"), tb3));
    }

    @Test
    public void testAfterHeadState() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.EndTag("head"));

        // Whitespace, Comment, Doctype
        Assert.assertTrue(TreeBuilderState.AfterHead.process(new Token.Character(" \n"), tb));
        Token.Comment c = new Token.Comment();
        c.getData().append("comment");
        Assert.assertTrue(TreeBuilderState.AfterHead.process(c, tb));
        Assert.assertTrue(TreeBuilderState.AfterHead.process(new Token.Doctype(), tb));

        // Start tag html
        Assert.assertTrue(TreeBuilderState.AfterHead.process(new Token.StartTag("html"), tb));

        // Start tag body
        TreeBuilder tbBody = createTreeBuilder();
        tbBody.process(new Token.StartTag("html"));
        tbBody.process(new Token.StartTag("head"));
        tbBody.process(new Token.EndTag("head"));
        Assert.assertTrue(TreeBuilderState.AfterHead.process(new Token.StartTag("body"), tbBody));
        Assert.assertEquals(TreeBuilderState.InBody, tbBody.state());
        Assert.assertFalse(tbBody.framesetOk());

        // Start tag frameset
        TreeBuilder tbFrameset = createTreeBuilder();
        tbFrameset.process(new Token.StartTag("html"));
        tbFrameset.process(new Token.StartTag("head"));
        tbFrameset.process(new Token.EndTag("head"));
        Assert.assertTrue(TreeBuilderState.AfterHead.process(new Token.StartTag("frameset"), tbFrameset));
        Assert.assertEquals(TreeBuilderState.InFrameset, tbFrameset.state());

        // Start tag head tags: base, link, meta, style, etc.
        TreeBuilder tbHeadTags = createTreeBuilder();
        tbHeadTags.process(new Token.StartTag("html"));
        tbHeadTags.process(new Token.StartTag("head"));
        tbHeadTags.process(new Token.EndTag("head"));
        Assert.assertTrue(TreeBuilderState.AfterHead.process(new Token.StartTag("meta"), tbHeadTags));

        // Start tag head -> false
        Assert.assertFalse(TreeBuilderState.AfterHead.process(new Token.StartTag("head"), tbHeadTags));

        // End tag body/html
        TreeBuilder tbEnd = createTreeBuilder();
        tbEnd.process(new Token.StartTag("html"));
        tbEnd.process(new Token.StartTag("head"));
        tbEnd.process(new Token.EndTag("head"));
        Assert.assertTrue(TreeBuilderState.AfterHead.process(new Token.EndTag("body"), tbEnd));

        // Invalid end tag
        TreeBuilder tbInvalidEnd = createTreeBuilder();
        tbInvalidEnd.process(new Token.StartTag("html"));
        tbInvalidEnd.process(new Token.StartTag("head"));
        tbInvalidEnd.process(new Token.EndTag("head"));
        Assert.assertFalse(TreeBuilderState.AfterHead.process(new Token.EndTag("p"), tbInvalidEnd));
    }

    @Test
    public void testTextState() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.StartTag("title"));
        Assert.assertEquals(TreeBuilderState.Text, tb.state());

        // Character
        Assert.assertTrue(TreeBuilderState.Text.process(new Token.Character("My Title"), tb));

        // EndTag
        Assert.assertTrue(TreeBuilderState.Text.process(new Token.EndTag("title"), tb));

        // EOF in Text state
        TreeBuilder tbEof = createTreeBuilder();
        tbEof.process(new Token.StartTag("html"));
        tbEof.process(new Token.StartTag("head"));
        tbEof.process(new Token.StartTag("style"));
        Assert.assertTrue(TreeBuilderState.Text.process(new Token.EOF(), tbEof));
    }

    @Test
    public void testInBodyStateVariousTags() {
        // Document parsing tests covering numerous branches in InBody
        String html = "<!DOCTYPE html><html id=1><body id=2>"
                + "<p>Text with <b>bold <i>italic <nobr>nobr</b></nobr></i></b></p>"
                + "<h1>Header 1<h2>Header 2</h3>"
                + "<pre>Preformatted</pre><listing>Listing</listing>"
                + "<form id=f1><p><input type=text><input type=hidden><hr></form>"
                + "<ul><li>Item 1<li>Item 2</ul><ol><li>Ordered</ol>"
                + "<dl><dt>Term<dd>Desc</dl>"
                + "<plaintext>Plain text content"
                + "</body></html>";
        Document doc = Jsoup.parse(html);
        Assert.assertNotNull(doc);
        Assert.assertEquals(2, doc.select("li").size());

        // Adoption Agency Algorithm & formatting tests
        String aaa = "<p><b>1<p>2</b>3</p><a><b><p>4</b></a>5</p>"
                + "<a>1<a href='#'>2</a>3</a>"
                + "<nobr>1<nobr>2</nobr>3</nobr>"
                + "<applet>app<p>let</applet><marquee>mar</marquee><object>obj</object>"
                + "<button><button>nested button</button></button>"
                + "<textarea>text</textarea><xmp>xmp</xmp><iframe>if</iframe><noembed>ne</noembed>"
                + "<ruby>base<rt>rt</rt><rp>rp</rp></ruby>"
                + "<select><option>1<optgroup label='g'><option>2</optgroup></select>"
                + "<math><svg><image><isindex action='search.php' prompt='Search: '>";
        Document docAaa = Jsoup.parse(aaa);
        Assert.assertNotNull(docAaa);

        // Additional InBody End Tags
        String endTags = "<p>paragraph</p></p><div>div</div></form></li></dd></dt>"
                + "<h1>h1</h1></h2></h3></h4></h5></h6></sarcasm><br></html>";
        Document docEnd = Jsoup.parse(endTags);
        Assert.assertNotNull(docEnd);
    }

    @Test
    public void testInBodyDirectProcessing() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));

        // Null character token check
        Token.Character nullChar = new Token.Character(String.valueOf(0x0000));
        Assert.assertFalse(TreeBuilderState.InBody.process(nullChar, tb));

        // Doctype token check
        Assert.assertFalse(TreeBuilderState.InBody.process(new Token.Doctype(), tb));

        // Tag: body start tag merging attributes
        Token.StartTag body2 = new Token.StartTag("body");
        body2.attributes.put("class", "main");
        Assert.assertTrue(TreeBuilderState.InBody.process(body2, tb));
        Assert.assertEquals("main", tb.getStack().get(1).attr("class"));

        // Tag: html start tag merging attributes
        Token.StartTag html2 = new Token.StartTag("html");
        html2.attributes.put("lang", "en");
        Assert.assertTrue(TreeBuilderState.InBody.process(html2, tb));
        Assert.assertEquals("en", tb.getStack().getFirst().attr("lang"));

        // Tag: frameset start tag when framesetOk is false
        Token.StartTag fs = new Token.StartTag("frameset");
        tb.framesetOk(false);
        Assert.assertFalse(TreeBuilderState.InBody.process(fs, tb));

        // Tag: ignored start tags in InBody (caption, col, etc.)
        Assert.assertFalse(TreeBuilderState.InBody.process(new Token.StartTag("caption"), tb));
        Assert.assertFalse(TreeBuilderState.InBody.process(new Token.StartTag("tbody"), tb));
        Assert.assertFalse(TreeBuilderState.InBody.process(new Token.StartTag("tr"), tb));

        // End tag: body when body not in scope
        TreeBuilder tbNoBody = createTreeBuilder();
        tbNoBody.process(new Token.StartTag("html"));
        Assert.assertFalse(TreeBuilderState.InBody.process(new Token.EndTag("body"), tbNoBody));

        // End tag: unknown / unhandled tag
        Assert.assertTrue(TreeBuilderState.InBody.process(new Token.EndTag("customtag"), tb));
    }

    @Test
    public void testInTableAndInTableText() {
        String tableHtml = "<table>"
                + "<!-- comment -->"
                + "<colgroup><col width='10'></colgroup>"
                + "<caption>Table Caption</caption>"
                + "<thead><tr><th>Head</th></tr></thead>"
                + "<tbody><tr><td>Cell 1</td><td>Cell 2</td></tr></tbody>"
                + "<tfoot><tr><td>Foot</td></tr></tfoot>"
                + "</table>";
        Document doc = Jsoup.parse(tableHtml);
        Assert.assertEquals(1, doc.select("table").size());
        Assert.assertEquals(1, doc.select("caption").size());
        Assert.assertEquals(1, doc.select("thead").size());
        Assert.assertEquals(1, doc.select("tbody").size());
        Assert.assertEquals(1, doc.select("tfoot").size());
        Assert.assertEquals(3, doc.select("tr").size());

        // Foster parenting and misplaced text inside table
        String fosterHtml = "<table>Misplaced Text<b>Bold</b><tr><td>Cell</td></tr></table>";
        Document docFoster = Jsoup.parse(fosterHtml);
        Assert.assertNotNull(docFoster);

        // Table tags direct error processing
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        Assert.assertEquals(TreeBuilderState.InTable, tb.state());

        // Doctype in table -> error and false
        Assert.assertFalse(TreeBuilderState.InTable.process(new Token.Doctype(), tb));

        // Form inside table
        Assert.assertTrue(TreeBuilderState.InTable.process(new Token.StartTag("form"), tb));
        Assert.assertNotNull(tb.getFormElement());
        // Second form in table -> error and false
        Assert.assertFalse(TreeBuilderState.InTable.process(new Token.StartTag("form"), tb));

        // Hidden input vs non-hidden input in table
        Token.StartTag hiddenInput = new Token.StartTag("input");
        hiddenInput.attributes.put("type", "hidden");
        Assert.assertTrue(TreeBuilderState.InTable.process(hiddenInput, tb));

        // End tag body/caption/col/html in InTable -> error and false
        Assert.assertFalse(TreeBuilderState.InTable.process(new Token.EndTag("body"), tb));
        Assert.assertFalse(TreeBuilderState.InTable.process(new Token.EndTag("caption"), tb));
        Assert.assertFalse(TreeBuilderState.InTable.process(new Token.EndTag("tr"), tb));

        // InTableText null character handling
        tb.transition(TreeBuilderState.InTableText);
        Token.Character nullChar = new Token.Character(String.valueOf(0x0000));
        Assert.assertFalse(TreeBuilderState.InTableText.process(nullChar, tb));
    }

    @Test
    public void testInCaptionState() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("caption"));
        Assert.assertEquals(TreeBuilderState.InCaption, tb.state());

        // End tag caption
        Assert.assertTrue(TreeBuilderState.InCaption.process(new Token.EndTag("caption"), tb));
        Assert.assertEquals(TreeBuilderState.InTable, tb.state());

        // Re-enter caption
        tb.process(new Token.StartTag("caption"));
        // Invalid end tag in caption
        Assert.assertFalse(TreeBuilderState.InCaption.process(new Token.EndTag("body"), tb));
        Assert.assertFalse(TreeBuilderState.InCaption.process(new Token.EndTag("colgroup"), tb));

        // Start tag table/tr inside caption (triggers close caption)
        Assert.assertTrue(TreeBuilderState.InCaption.process(new Token.StartTag("tr"), tb));
    }

    @Test
    public void testInColumnGroupState() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("colgroup"));
        Assert.assertEquals(TreeBuilderState.InColumnGroup, tb.state());

        // Whitespace, comment, doctype
        Assert.assertTrue(TreeBuilderState.InColumnGroup.process(new Token.Character(" "), tb));
        Token.Comment c = new Token.Comment();
        c.getData().append("col comment");
        Assert.assertTrue(TreeBuilderState.InColumnGroup.process(c, tb));
        Assert.assertTrue(TreeBuilderState.InColumnGroup.process(new Token.Doctype(), tb));

        // Start tag col
        Assert.assertTrue(TreeBuilderState.InColumnGroup.process(new Token.StartTag("col"), tb));

        // End tag colgroup
        Assert.assertTrue(TreeBuilderState.InColumnGroup.process(new Token.EndTag("colgroup"), tb));
        Assert.assertEquals(TreeBuilderState.InTable, tb.state());

        // Re-enter colgroup and send anything else
        tb.process(new Token.StartTag("colgroup"));
        Assert.assertTrue(TreeBuilderState.InColumnGroup.process(new Token.StartTag("tr"), tb));
    }

    @Test
    public void testInTableBodyAndInRowAndInCell() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("table"));
        tb.process(new Token.StartTag("tbody"));
        Assert.assertEquals(TreeBuilderState.InTableBody, tb.state());

        // Th/Td directly in tbody (automatically opens tr)
        Assert.assertTrue(TreeBuilderState.InTableBody.process(new Token.StartTag("td"), tb));
        Assert.assertEquals(TreeBuilderState.InCell, tb.state());

        // Close cell
        Assert.assertTrue(TreeBuilderState.InCell.process(new Token.EndTag("td"), tb));
        Assert.assertEquals(TreeBuilderState.InRow, tb.state());

        // Th in row
        Assert.assertTrue(TreeBuilderState.InRow.process(new Token.StartTag("th"), tb));
        Assert.assertEquals(TreeBuilderState.InCell, tb.state());

        // Table start tag inside cell (closes cell and bubbles up)
        Assert.assertTrue(TreeBuilderState.InCell.process(new Token.StartTag("table"), tb));

        // Invalid end tags in cell
        TreeBuilder tbCell = createTreeBuilder();
        tbCell.process(new Token.StartTag("html"));
        tbCell.process(new Token.StartTag("body"));
        tbCell.process(new Token.StartTag("table"));
        tbCell.process(new Token.StartTag("tbody"));
        tbCell.process(new Token.StartTag("tr"));
        tbCell.process(new Token.StartTag("td"));
        Assert.assertFalse(TreeBuilderState.InCell.process(new Token.EndTag("body"), tbCell));

        // End tag tr in row
        TreeBuilder tbRow = createTreeBuilder();
        tbRow.process(new Token.StartTag("html"));
        tbRow.process(new Token.StartTag("body"));
        tbRow.process(new Token.StartTag("table"));
        tbRow.process(new Token.StartTag("tbody"));
        tbRow.process(new Token.StartTag("tr"));
        Assert.assertTrue(TreeBuilderState.InRow.process(new Token.EndTag("tr"), tbRow));
        Assert.assertEquals(TreeBuilderState.InTableBody, tbRow.state());

        // End tag tbody in tbody
        Assert.assertTrue(TreeBuilderState.InTableBody.process(new Token.EndTag("tbody"), tbRow));
        Assert.assertEquals(TreeBuilderState.InTable, tbRow.state());
    }

    @Test
    public void testInSelectAndInSelectInTable() {
        String selectHtml = "<table><tr><td><select><option value=1>One<optgroup label='g'><option value=2>Two</optgroup></select></td></tr></table>";
        Document doc = Jsoup.parse(selectHtml);
        Assert.assertEquals(1, doc.select("select").size());
        Assert.assertEquals(2, doc.select("option").size());

        // Direct InSelect tests
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.StartTag("select"));
        Assert.assertEquals(TreeBuilderState.InSelect, tb.state());

        // Null character -> false
        Assert.assertFalse(TreeBuilderState.InSelect.process(new Token.Character(String.valueOf(0x0000)), tb));

        // Comment, Doctype
        Token.Comment c = new Token.Comment();
        c.getData().append("comment in select");
        Assert.assertTrue(TreeBuilderState.InSelect.process(c, tb));
        Assert.assertFalse(TreeBuilderState.InSelect.process(new Token.Doctype(), tb));

        // Option and optgroup start tags
        Assert.assertTrue(TreeBuilderState.InSelect.process(new Token.StartTag("option"), tb));
        Assert.assertTrue(TreeBuilderState.InSelect.process(new Token.StartTag("optgroup"), tb));

        // Optgroup and option end tags
        Assert.assertTrue(TreeBuilderState.InSelect.process(new Token.EndTag("optgroup"), tb));
        Assert.assertTrue(TreeBuilderState.InSelect.process(new Token.EndTag("option"), tb));

        // Nested select start tag -> closes current select
        Assert.assertTrue(TreeBuilderState.InSelect.process(new Token.StartTag("select"), tb));

        // InSelectInTable transition and processing
        TreeBuilder tbTable = createTreeBuilder();
        tbTable.process(new Token.StartTag("html"));
        tbTable.process(new Token.StartTag("body"));
        tbTable.process(new Token.StartTag("table"));
        tbTable.process(new Token.StartTag("tbody"));
        tbTable.process(new Token.StartTag("tr"));
        tbTable.process(new Token.StartTag("td"));
        tbTable.process(new Token.StartTag("select"));
        Assert.assertEquals(TreeBuilderState.InSelectInTable, tbTable.state());

        // Start tag table inside select in table
        Assert.assertTrue(TreeBuilderState.InSelectInTable.process(new Token.StartTag("table"), tbTable));
    }

    @Test
    public void testFramesetStates() {
        String framesetHtml = "<!DOCTYPE html><html><frameset rows='50%,50%'><frame src='1.html'><frame src='2.html'><noframes>No frames supported</noframes></frameset></html>";
        Document doc = Jsoup.parse(framesetHtml);
        Assert.assertEquals(1, doc.select("frameset").size());
        Assert.assertEquals(2, doc.select("frame").size());

        // Direct InFrameset, AfterFrameset, AfterAfterFrameset testing
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("head"));
        tb.process(new Token.EndTag("head"));
        tb.process(new Token.StartTag("frameset"));
        Assert.assertEquals(TreeBuilderState.InFrameset, tb.state());

        // Whitespace, comment, doctype
        Assert.assertTrue(TreeBuilderState.InFrameset.process(new Token.Character(" \t"), tb));
        Token.Comment c = new Token.Comment();
        c.getData().append("frameset comment");
        Assert.assertTrue(TreeBuilderState.InFrameset.process(c, tb));
        Assert.assertFalse(TreeBuilderState.InFrameset.process(new Token.Doctype(), tb));

        // Frame and noframes
        Assert.assertTrue(TreeBuilderState.InFrameset.process(new Token.StartTag("frame"), tb));
        Assert.assertTrue(TreeBuilderState.InFrameset.process(new Token.StartTag("noframes"), tb));

        // End tag frameset -> transitions to AfterFrameset
        Assert.assertTrue(TreeBuilderState.InFrameset.process(new Token.EndTag("frameset"), tb));
        Assert.assertEquals(TreeBuilderState.AfterFrameset, tb.state());

        // AfterFrameset whitespace, comment, doctype
        Assert.assertTrue(TreeBuilderState.AfterFrameset.process(new Token.Character(" "), tb));
        Assert.assertTrue(TreeBuilderState.AfterFrameset.process(c, tb));
        Assert.assertFalse(TreeBuilderState.AfterFrameset.process(new Token.Doctype(), tb));

        // End tag html -> transitions to AfterAfterFrameset
        Assert.assertTrue(TreeBuilderState.AfterFrameset.process(new Token.EndTag("html"), tb));
        Assert.assertEquals(TreeBuilderState.AfterAfterFrameset, tb.state());

        // AfterAfterFrameset tokens
        Assert.assertTrue(TreeBuilderState.AfterAfterFrameset.process(c, tb));
        Assert.assertTrue(TreeBuilderState.AfterAfterFrameset.process(new Token.Character(" "), tb));
        Assert.assertTrue(TreeBuilderState.AfterAfterFrameset.process(new Token.EOF(), tb));
    }

    @Test
    public void testAfterBodyAndAfterAfterBody() {
        TreeBuilder tb = createTreeBuilder();
        tb.process(new Token.StartTag("html"));
        tb.process(new Token.StartTag("body"));
        tb.process(new Token.EndTag("body"));
        Assert.assertEquals(TreeBuilderState.AfterBody, tb.state());

        // Whitespace, comment, doctype
        Assert.assertTrue(TreeBuilderState.AfterBody.process(new Token.Character(" \n"), tb));
        Token.Comment c = new Token.Comment();
        c.getData().append("after body comment");
        Assert.assertTrue(TreeBuilderState.AfterBody.process(c, tb));
        Assert.assertFalse(TreeBuilderState.AfterBody.process(new Token.Doctype(), tb));

        // Start tag html
        Assert.assertTrue(TreeBuilderState.AfterBody.process(new Token.StartTag("html"), tb));

        // End tag html -> transitions to AfterAfterBody
        Assert.assertTrue(TreeBuilderState.AfterBody.process(new Token.EndTag("html"), tb));
        Assert.assertEquals(TreeBuilderState.AfterAfterBody, tb.state());

        // AfterAfterBody tokens
        Assert.assertTrue(TreeBuilderState.AfterAfterBody.process(c, tb));
        Assert.assertTrue(TreeBuilderState.AfterAfterBody.process(new Token.Character(" \t"), tb));
        Assert.assertTrue(TreeBuilderState.AfterAfterBody.process(new Token.EOF(), tb));

        // Anything else in AfterAfterBody -> transitions back to InBody
        Assert.assertTrue(TreeBuilderState.AfterAfterBody.process(new Token.StartTag("p"), tb));
        Assert.assertEquals(TreeBuilderState.InBody, tb.state());
    }
}
