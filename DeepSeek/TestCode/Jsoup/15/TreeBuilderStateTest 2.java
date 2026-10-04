package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.jsoup.helper.DescendableLinkedList;
import org.jsoup.nodes.*;

import java.util.LinkedList;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TreeBuilderStateTest {

    @Mock
    private TreeBuilder tb;
    @Mock
    private Document document;
    @Mock
    private Tokeniser tokeniser;
    @Mock
    private Element element;
    @Mock
    private DescendableLinkedList<Element> stack;

    @Before
    public void setUp() {
        when(tb.getDocument()).thenReturn(document);
        when(tb.getBaseUri()).thenReturn("");
        when(tb.tokeniser).thenReturn(tokeniser);
        when(tb.getStack()).thenReturn(stack);
        when(tb.insert(any(Token.StartTag.class))).thenReturn(element);
        when(tb.insert(any(Element.class))).thenReturn(element);
        when(tb.insert(any(Token.Comment.class))).thenReturn(element);
        when(tb.insert(any(Token.Character.class))).thenReturn(element);
        when(tb.insertEmpty(any(Token.StartTag.class))).thenReturn(element);
        when(tb.getHeadElement()).thenReturn(element);
        when(tb.currentElement()).thenReturn(element);
        when(element.nodeName()).thenReturn("html");
        when(stack.getFirst()).thenReturn(element);
        when(stack.size()).thenReturn(2);
        when(stack.get(0)).thenReturn(element);
        when(stack.get(1)).thenReturn(element);
        when(stack.descendingIterator()).thenReturn(new LinkedList<Element>().descendingIterator());
    }

    // Tests for Initial state
    @Test
    public void testInitialWhitespace() {
        TreeBuilderState state = TreeBuilderState.Initial;
        Token t = new Token.Character("   ");
        assertTrue(state.process(t, tb));
        verifyNoMoreInteractions(tb);
    }

    @Test
    public void testInitialComment() {
        TreeBuilderState state = TreeBuilderState.Initial;
        Token.Comment comment = new Token.Comment("comment");
        when(tb.insert(comment)).thenReturn(element);
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment));
    }

    @Test
    public void testInitialDoctype() {
        TreeBuilderState state = TreeBuilderState.Initial;
        Token.Doctype doctype = new Token.Doctype("html", "", "");
        assertTrue(state.process(doctype, tb));
        verify(document).appendChild(any(DocumentType.class));
        verify(tb).transition(TreeBuilderState.BeforeHtml);
    }

    @Test
    public void testInitialDoctypeForceQuirks() {
        TreeBuilderState state = TreeBuilderState.Initial;
        Token.Doctype doctype = new Token.Doctype("html", "", "");
        doctype.forceQuirks = true;
        when(document.quirksMode(Document.QuirksMode.quirks)).thenReturn(document);
        assertTrue(state.process(doctype, tb));
        verify(document).quirksMode(Document.QuirksMode.quirks));
        verify(tb).transition(TreeBuilderState.BeforeHtml);
    }

    @Test
    public void testInitialOtherTag() {
        TreeBuilderState state = TreeBuilderState.Initial;
        Token.StartTag start = new Token.StartTag("html");
        when(tb.process(start)).thenReturn(true);
        assertTrue(state.process(start, tb));
        verify(tb).transition(TreeBuilderState.BeforeHtml);
        verify(tb).process(start);
    }

    // Tests for BeforeHtml state
    @Test
    public void testBeforeHtmlDoctype() {
        TreeBuilderState state = TreeBuilderState.BeforeHtml;
        Token.Doctype doctype = new Token.Doctype("html", "", "");
        assertFalse(state.process(doctype, tb));
        verify(tb).error(state);
    }

    @Test
    public void testBeforeHtmlComment() {
        TreeBuilderState state = TreeBuilderState.BeforeHtml;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    @Test
    public void testBeforeHtmlWhitespace() {
        TreeBuilderState state = TreeBuilderState.BeforeHtml;
        Token.Character ws = new Token.Character(" ");
        assertTrue(state.process(ws, tb));
        verifyNoMoreInteractions(tb);
    }

    @Test
    public void testBeforeHtmlStartTagHtml() {
        TreeBuilderState state = TreeBuilderState.BeforeHtml;
        Token.StartTag html = new Token.StartTag("html");
        assertTrue(state.process(html, tb));
        verify(tb).insert(html);
        verify(tb).transition(TreeBuilderState.BeforeHead);
    }

    @Test
    public void testBeforeHtmlEndTagRecognizedAndOther() {
        TreeBuilderState state = TreeBuilderState.BeforeHtml;
        Token.EndTag end = new Token.EndTag("head");
        when(tb.process(end)).thenReturn(true);
        assertTrue(state.process(end, tb));
        // anythingElse: insert("html"), transition, process
        verify(tb).insert("html");
        verify(tb).transition(TreeBuilderState.BeforeHead);
        verify(tb).process(end);
    }

    @Test
    public void testBeforeHtmlEndTagUnrecognized() {
        TreeBuilderState state = TreeBuilderState.BeforeHtml;
        Token.EndTag end = new Token.EndTag("div");
        assertFalse(state.process(end, tb));
        verify(tb).error(state);
    }

    @Test
    public void testBeforeHtmlOtherStartTag() {
        TreeBuilderState state = TreeBuilderState.BeforeHtml;
        Token.StartTag start = new Token.StartTag("div");
        when(tb.process(start)).thenReturn(true);
        assertTrue(state.process(start, tb));
        verify(tb).insert("html");
        verify(tb).transition(TreeBuilderState.BeforeHead);
        verify(tb).process(start);
    }

    // Tests for BeforeHead state
    @Test
    public void testBeforeHeadWhitespace() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        assertTrue(state.process(new Token.Character(" "), tb));
    }

    @Test
    public void testBeforeHeadComment() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        Token.Comment c = new Token.Comment("c");
        assertTrue(state.process(c, tb));
        verify(tb).insert(c);
    }

    @Test
    public void testBeforeHeadDoctype() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        assertFalse(state.process(new Token.Doctype("html", "", ""), tb));
        verify(tb).error(state);
    }

    @Test
    public void testBeforeHeadStartTagHtml() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        Token.StartTag html = new Token.StartTag("html");
        when(tb.process(html, TreeBuilderState.InBody)).thenReturn(true);
        assertTrue(state.process(html, tb));
        verify(tb).process(html, TreeBuilderState.InBody);
    }

    @Test
    public void testBeforeHeadStartTagHead() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        Token.StartTag head = new Token.StartTag("head");
        assertTrue(state.process(head, tb));
        verify(tb).insert(head);
        verify(tb).setHeadElement(element);
        verify(tb).transition(TreeBuilderState.InHead);
    }

    @Test
    public void testBeforeHeadEndTagRecognized() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        Token.EndTag end = new Token.EndTag("head");
        when(tb.process(any(Token.StartTag.class))).thenReturn(element);
        when(tb.process(end)).thenReturn(true);
        assertTrue(state.process(end, tb));
        verify(tb).process(new Token.StartTag("head"));
        verify(tb).process(end);
    }

    @Test
    public void testBeforeHeadEndTagUnrecognized() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        Token.EndTag end = new Token.EndTag("div");
        assertFalse(state.process(end, tb));
        verify(tb).error(state);
    }

    @Test
    public void testBeforeHeadOtherStart() {
        TreeBuilderState state = TreeBuilderState.BeforeHead;
        Token.StartTag other = new Token.StartTag("div");
        when(tb.process(new Token.StartTag("head"))).thenReturn(element);
        when(tb.process(other)).thenReturn(true);
        assertTrue(state.process(other, tb));
        verify(tb).process(new Token.StartTag("head"));
        verify(tb).process(other);
    }

    // InHead tests (simplified coverage; many branches require more complex setup)
    @Test
    public void testInHeadWhitespace() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.Character c = new Token.Character(" ");
        when(tb.insert(c)).thenReturn(element);
        assertTrue(state.process(c, tb));
        verify(tb).insert(c);
    }

    @Test
    public void testInHeadComment() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    @Test
    public void testInHeadDoctype() {
        TreeBuilderState state = TreeBuilderState.InHead;
        assertFalse(state.process(new Token.Doctype("html", "", ""), tb));
        verify(tb).error(state);
    }

    @Test
    public void testInHeadStartTagHtml() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag html = new Token.StartTag("html");
        when(tb.process(html, TreeBuilderState.InBody)).thenReturn(true);
        assertTrue(state.process(html, tb));
        verify(tb).process(html, TreeBuilderState.InBody);
    }

    @Test
    public void testInHeadStartTagBase() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag base = new Token.StartTag("base");
        when(tb.insertEmpty(base)).thenReturn(element);
        assertTrue(state.process(base, tb));
        verify(tb).insertEmpty(base);
    }

    @Test
    public void testInHeadStartTagMeta() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag meta = new Token.StartTag("meta");
        assertTrue(state.process(meta, tb));
        verify(tb).insertEmpty(meta);
    }

    @Test
    public void testInHeadStartTagTitle() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag title = new Token.StartTag("title");
        // handleRcData should be called; we verify tokeniser transitions
        assertTrue(state.process(title, tb));
        verify(tb).insert(title);
        verify(tokeniser).transition(TokeniserState.Rcdata);
        verify(tb).markInsertionMode();
        verify(tb).transition(TreeBuilderState.Text);
    }

    @Test
    public void testInHeadStartTagNofframes() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag noframes = new Token.StartTag("noframes");
        assertTrue(state.process(noframes, tb));
        verify(tb).insert(noframes);
        verify(tokeniser).transition(TokeniserState.Rawtext);
        verify(tb).markInsertionMode();
        verify(tb).transition(TreeBuilderState.Text);
    }

    @Test
    public void testInHeadStartTagNoscript() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag noscript = new Token.StartTag("noscript");
        assertTrue(state.process(noscript, tb));
        verify(tb).insert(noscript);
        verify(tb).transition(TreeBuilderState.InHeadNoscript);
    }

    @Test
    public void testInHeadStartTagScript() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag script = new Token.StartTag("script");
        assertTrue(state.process(script, tb));
        verify(tb).insert(script);
        verify(tokeniser).transition(TokeniserState.ScriptData);
        verify(tb).markInsertionMode();
        verify(tb).transition(TreeBuilderState.Text);
    }

    @Test
    public void testInHeadStartTagHead_duplicate_error() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.StartTag head = new Token.StartTag("head");
        assertFalse(state.process(head, tb));
        verify(tb).error(state);
    }

    @Test
    public void testInHeadEndTagHead() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.EndTag endHead = new Token.EndTag("head");
        assertTrue(state.process(endHead, tb));
        verify(tb).pop();
        verify(tb).transition(TreeBuilderState.AfterHead);
    }

    @Test
    public void testInHeadEndTagBody() {
        TreeBuilderState state = TreeBuilderState.InHead;
        Token.EndTag endBody = new Token.EndTag("body");
        when(tb.process(any(Token.EndTag.class))).thenReturn(true);
        assertTrue(state.process(endBody, tb));
        // anythingElse: process end head
        verify(tb).process(new Token.EndTag("head"));
        verify(tb).process(endBody);
    }

    // InHeadNoscript state
    @Test
    public void testInHeadNoscriptDoctype() {
        TreeBuilderState state = TreeBuilderState.InHeadNoscript;
        assertTrue(state.process(new Token.Doctype("html", "", ""), tb));
        verify(tb).error(state);
    }

    @Test
    public void testInHeadNoscriptStartTagHtml() {
        TreeBuilderState state = TreeBuilderState.InHeadNoscript;
        Token.StartTag html = new Token.StartTag("html");
        when(tb.process(html, TreeBuilderState.InBody)).thenReturn(true);
        assertTrue(state.process(html, tb));
        verify(tb).process(html, TreeBuilderState.InBody);
    }

    @Test
    public void testInHeadNoscriptEndTagNoscript() {
        TreeBuilderState state = TreeBuilderState.InHeadNoscript;
        Token.EndTag end = new Token.EndTag("noscript");
        assertTrue(state.process(end, tb));
        verify(tb).pop();
        verify(tb).transition(TreeBuilderState.InHead);
    }

    // AfterHead state (simplified)
    @Test
    public void testAfterHeadWhitespace() {
        TreeBuilderState state = TreeBuilderState.AfterHead;
        Token.Character c = new Token.Character(" ");
        when(tb.insert(c)).thenReturn(element);
        assertTrue(state.process(c, tb));
        verify(tb).insert(c);
    }

    @Test
    public void testAfterHeadComment() {
        TreeBuilderState state = TreeBuilderState.AfterHead;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    @Test
    public void testAfterHeadDoctype() {
        TreeBuilderState state = TreeBuilderState.AfterHead;
        assertTrue(state.process(new Token.Doctype("html", "", ""), tb));
        verify(tb).error(state);
    }

    @Test
    public void testAfterHeadStartTagBody() {
        TreeBuilderState state = TreeBuilderState.AfterHead;
        Token.StartTag body = new Token.StartTag("body");
        assertTrue(state.process(body, tb));
        verify(tb).insert(body);
        verify(tb).framesetOk(false);
        verify(tb).transition(TreeBuilderState.InBody);
    }

    @Test
    public void testAfterHeadStartTagFrameset() {
        TreeBuilderState state = TreeBuilderState.AfterHead;
        Token.StartTag frameset = new Token.StartTag("frameset");
        assertTrue(state.process(frameset, tb));
        verify(tb).insert(frameset);
        verify(tb).transition(TreeBuilderState.InFrameset);
    }

    // InBody: extensive, so we test key branches
    @Test
    public void testInBodyCharacterWhitespace() {
        TreeBuilderState state = TreeBuilderState.InBody;
        Token.Character c = new Token.Character(" ");
        assertTrue(state.process(c, tb));
        verify(tb).reconstructFormattingElements();
        verify(tb).insert(c);
    }

    @Test
    public void testInBodyCharacterNullString() {
        TreeBuilderState state = TreeBuilderState.InBody;
        Token.Character c = new Token.Character("\u0000");
        assertFalse(state.process(c, tb));
        verify(tb).error(state);
    }

    @Test
    public void testInBodyComment() {
        TreeBuilderState state = TreeBuilderState.InBody;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    @Test
    public void testInBodyDoctype() {
        TreeBuilderState state = TreeBuilderState.InBody;
        assertFalse(state.process(new Token.Doctype("html", "", ""), tb));
        verify(tb).error(state);
    }

    // InTable basic
    @Test
    public void testInTableCharacter() {
        TreeBuilderState state = TreeBuilderState.InTable;
        Token.Character c = new Token.Character("a");
        when(tb.process(c)).thenReturn(true);
        assertTrue(state.process(c, tb));
        verify(tb).newPendingTableCharacters();
        verify(tb).markInsertionMode();
        verify(tb).transition(TreeBuilderState.InTableText);
        verify(tb).process(c);
    }

    // Text state
    @Test
    public void testTextCharacter() {
        TreeBuilderState state = TreeBuilderState.Text;
        Token.Character c = new Token.Character("a");
        assertTrue(state.process(c, tb));
        verify(tb).insert(c);
    }

    @Test
    public void testTextEof() {
        TreeBuilderState state = TreeBuilderState.Text;
        Token eof = Token.EOF;
        when(tb.originalState()).thenReturn(TreeBuilderState.InBody);
        when(tb.process(eof)).thenReturn(true);
        assertTrue(state.process(eof, tb));
        verify(tb).pop();
        verify(tb).transition(TreeBuilderState.InBody);
        verify(tb).process(eof);
    }

    @Test
    public void testTextEndTag() {
        TreeBuilderState state = TreeBuilderState.Text;
        Token.EndTag end = new Token.EndTag("script");
        when(tb.originalState()).thenReturn(TreeBuilderState.InBody);
        assertTrue(state.process(end, tb));
        verify(tb).pop();
        verify(tb).transition(TreeBuilderState.InBody);
    }

    // InTableText basics
    @Test
    public void testInTableTextCharacter() {
        TreeBuilderState state = TreeBuilderState.InTableText;
        Token.Character c = new Token.Character("a");
        assertTrue(state.process(c, tb));
        verify(tb).getPendingTableCharacters().add(c);
    }

    // InCaption
    @Test
    public void testInCaptionEndTagCaption() {
        TreeBuilderState state = TreeBuilderState.InCaption;
        Token.EndTag end = new Token.EndTag("caption");
        when(tb.inTableScope("caption")).thenReturn(true);
        when(tb.currentElement().nodeName()).thenReturn("caption");
        assertTrue(state.process(end, tb));
        verify(tb).popStackToClose("caption");
        verify(tb).clearFormattingElementsToLastMarker();
        verify(tb).transition(TreeBuilderState.InTable);
    }

    // InColumnGroup
    @Test
    public void testInColumnGroupWhitespace() {
        TreeBuilderState state = TreeBuilderState.InColumnGroup;
        Token.Character ws = new Token.Character(" ");
        assertTrue(state.process(ws, tb));
        verify(tb).insert(ws);
    }

    @Test
    public void testInColumnGroupComment() {
        TreeBuilderState state = TreeBuilderState.InColumnGroup;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    @Test
    public void testInColumnGroupDoctype() {
        TreeBuilderState state = TreeBuilderState.InColumnGroup;
        assertTrue(state.process(new Token.Doctype("html", "", ""), tb));
        verify(tb).error(state);
    }

    @Test
    public void testInColumnGroupStartTagCol() {
        TreeBuilderState state = TreeBuilderState.InColumnGroup;
        Token.StartTag col = new Token.StartTag("col");
        assertTrue(state.process(col, tb));
        verify(tb).insertEmpty(col);
    }

    // InTableBody
    @Test
    public void testInTableBodyStartTagTr() {
        TreeBuilderState state = TreeBuilderState.InTableBody;
        Token.StartTag tr = new Token.StartTag("tr");
        assertTrue(state.process(tr, tb));
        verify(tb).clearStackToTableBodyContext();
        verify(tb).insert(tr);
        verify(tb).transition(TreeBuilderState.InRow);
    }

    // InRow
    @Test
    public void testInRowStartTagTd() {
        TreeBuilderState state = TreeBuilderState.InRow;
        Token.StartTag td = new Token.StartTag("td");
        assertTrue(state.process(td, tb));
        verify(tb).clearStackToTableRowContext();
        verify(tb).insert(td);
        verify(tb).transition(TreeBuilderState.InCell);
        verify(tb).insertMarkerToFormattingElements();
    }

    // InCell
    @Test
    public void testInCellEndTagTdInScope() {
        TreeBuilderState state = TreeBuilderState.InCell;
        Token.EndTag td = new Token.EndTag("td");
        when(tb.inTableScope("td")).thenReturn(true);
        when(tb.currentElement().nodeName()).thenReturn("td");
        assertTrue(state.process(td, tb));
        verify(tb).popStackToClose("td");
        verify(tb).clearFormattingElementsToLastMarker();
        verify(tb).transition(TreeBuilderState.InRow);
    }

    // InSelect
    @Test
    public void testInSelectCharacter() {
        TreeBuilderState state = TreeBuilderState.InSelect;
        Token.Character c = new Token.Character("a");
        assertTrue(state.process(c, tb));
        verify(tb).insert(c);
    }

    @Test
    public void testInSelectComment() {
        TreeBuilderState state = TreeBuilderState.InSelect;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    @Test
    public void testInSelectDoctype() {
        TreeBuilderState state = TreeBuilderState.InSelect;
        assertFalse(state.process(new Token.Doctype("html", "", ""), tb));
        verify(tb).error(state);
    }

    @Test
    public void testInSelectEndTagSelect() {
        TreeBuilderState state = TreeBuilderState.InSelect;
        Token.EndTag select = new Token.EndTag("select");
        when(tb.inSelectScope("select")).thenReturn(true);
        when(tb.currentElement().nodeName()).thenReturn("select");
        assertTrue(state.process(select, tb));
        verify(tb).popStackToClose("select");
        verify(tb).resetInsertionMode();
    }

    // InSelectInTable
    @Test
    public void testInSelectInTableStartTriggersError() {
        TreeBuilderState state = TreeBuilderState.InSelectInTable;
        Token.StartTag caption = new Token.StartTag("caption");
        when(tb.process(any(Token.EndTag.class))).thenReturn(true);
        when(tb.process(caption)).thenReturn(true);
        assertTrue(state.process(caption, tb));
        verify(tb).error(state);
        verify(tb).process(new Token.EndTag("select"));
        verify(tb).process(caption);
    }

    // AfterBody
    @Test
    public void testAfterBodyWhitespace() {
        TreeBuilderState state = TreeBuilderState.AfterBody;
        Token.Character ws = new Token.Character(" ");
        when(tb.process(ws, TreeBuilderState.InBody)).thenReturn(true);
        assertTrue(state.process(ws, tb));
        verify(tb).process(ws, TreeBuilderState.InBody);
    }

    // InFrameset
    @Test
    public void testInFramesetWhitespace() {
        TreeBuilderState state = TreeBuilderState.InFrameset;
        Token.Character ws = new Token.Character(" ");
        assertTrue(state.process(ws, tb));
        verify(tb).insert(ws);
    }

    // AfterFrameset
    @Test
    public void testAfterFramesetWhitespace() {
        TreeBuilderState state = TreeBuilderState.AfterFrameset;
        Token.Character ws = new Token.Character(" ");
        assertTrue(state.process(ws, tb));
        verify(tb).insert(ws);
    }

    // AfterAfterBody
    @Test
    public void testAfterAfterBodyComment() {
        TreeBuilderState state = TreeBuilderState.AfterAfterBody;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    // AfterAfterFrameset
    @Test
    public void testAfterAfterFramesetComment() {
        TreeBuilderState state = TreeBuilderState.AfterAfterFrameset;
        Token.Comment comment = new Token.Comment("c");
        assertTrue(state.process(comment, tb));
        verify(tb).insert(comment);
    }

    // ForeignContent (stub)
    @Test
    public void testForeignContentAny() {
        TreeBuilderState state = TreeBuilderState.ForeignContent;
        assertTrue(state.process(new Token.StartTag("math"), tb));
    }

    // Utility: test nullString constant indirectly (already tested via InBody null char)
    // test isWhitespace indirectly through states.
}
```
