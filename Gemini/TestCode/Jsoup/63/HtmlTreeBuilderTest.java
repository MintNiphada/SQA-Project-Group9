package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder tb;

    @Before
    public void setUp() {
        tb = new HtmlTreeBuilder();
    }

    @Test
    public void testDefaultSettings() {
        ParseSettings settings = tb.defaultSettings();
        assertNotNull(settings);
        assertEquals(ParseSettings.htmlDefault, settings);
    }

    @Test
    public void testInitialiseParseAndState() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        tb.initialiseParse(new StringReader("<div></div>"), "http://example.com", errors, ParseSettings.htmlDefault);
        assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        assertNull(tb.originalState());
        assertEquals("http://example.com", tb.getBaseUri());
        assertTrue(tb.framesetOk());
        assertFalse(tb.isFosterInserts());
        assertFalse(tb.isFragmentParsing());
        assertNotNull(tb.getDocument());
        assertNull(tb.getHeadElement());
        assertNull(tb.getFormElement());
        assertEquals(0, tb.getPendingTableCharacters().size());
    }

    @Test
    public void testTransitionAndMarkInsertionMode() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.transition(HtmlTreeBuilderState.InBody);
        assertEquals(HtmlTreeBuilderState.InBody, tb.state());
        tb.markInsertionMode();
        assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());
        tb.transition(HtmlTreeBuilderState.InTable);
        assertEquals(HtmlTreeBuilderState.InTable, tb.state());
        assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());
    }

    @Test
    public void testFramesetOkAndFosterInserts() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.framesetOk(false);
        assertFalse(tb.framesetOk());
        tb.setFosterInserts(true);
        assertTrue(tb.isFosterInserts());
    }

    @Test
    public void testHeadAndFormElementAccessors() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        assertSame(head, tb.getHeadElement());

        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        tb.setFormElement(form);
        assertSame(form, tb.getFormElement());
    }

    @Test
    public void testPendingTableCharacters() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        List<String> list = new ArrayList<>();
        list.add("test");
        tb.setPendingTableCharacters(list);
        assertEquals(1, tb.getPendingTableCharacters().size());
        assertEquals("test", tb.getPendingTableCharacters().get(0));
        tb.newPendingTableCharacters();
        assertEquals(0, tb.getPendingTableCharacters().size());
    }

    @Test
    public void testMaybeSetBaseUri() {
        tb.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element base = new Element(Tag.valueOf("base"), "http://example.com/");
        base.attr("href", "http://example.com/sub/");
        tb.maybeSetBaseUri(base);
        assertEquals("http://example.com/sub/", tb.getBaseUri());
        assertEquals("http://example.com/sub/", tb.getDocument().baseUri());

        Element base2 = new Element(Tag.valueOf("base"), "http://example.com/sub/");
        base2.attr("href", "http://example.com/another/");
        tb.maybeSetBaseUri(base2);
        assertEquals("http://example.com/sub/", tb.getBaseUri());

        HtmlTreeBuilder tb2 = new HtmlTreeBuilder();
        tb2.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element baseNoHref = new Element(Tag.valueOf("base"), "http://example.com/");
        baseNoHref.attr("target", "_blank");
        tb2.maybeSetBaseUri(baseNoHref);
        assertEquals("http://example.com/", tb2.getBaseUri());
    }

    @Test
    public void testErrorTracking() {
        ParseErrorList errors = ParseErrorList.tracking(5);
        tb.initialiseParse(new StringReader("<p>"), "", errors, ParseSettings.htmlDefault);
        Token.StartTag tag = new Token.StartTag();
        tag.nameAttr("p", new Attributes());
        tb.process(tag, HtmlTreeBuilderState.Initial);
        assertEquals(1, errors.size());
    }

    @Test
    public void testStackOperations() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(div);
        tb.push(p);

        assertEquals(4, tb.getStack().size());
        assertTrue(tb.onStack(body));
        assertSame(p, tb.currentElement());

        assertSame(body, tb.aboveOnStack(div));
        assertNull(tb.aboveOnStack(html));

        assertSame(div, tb.getFromStack("div"));
        assertNull(tb.getFromStack("span"));

        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertOnStackAfter(div, span);
        assertEquals(5, tb.getStack().size());
        assertSame(span, tb.getStack().get(3));

        Element replaced = new Element(Tag.valueOf("section"), "");
        tb.replaceOnStack(span, replaced);
        assertSame(replaced, tb.getStack().get(3));

        assertTrue(tb.removeFromStack(replaced));
        assertFalse(tb.removeFromStack(span));

        assertSame(p, tb.pop());
        assertEquals(3, tb.getStack().size());
    }

    @Test
    public void testPopStackVariants() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div1 = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        Element b = new Element(Tag.valueOf("b"), "");

        tb.push(html);
        tb.push(body);
        tb.push(div1);
        tb.push(span);
        tb.push(b);

        tb.popStackToBefore("div");
        assertEquals(3, tb.getStack().size());
        assertSame(div1, tb.currentElement());

        tb.push(span);
        tb.push(b);
        tb.popStackToClose("span");
        assertEquals(3, tb.getStack().size());
        assertSame(div1, tb.currentElement());

        tb.push(span);
        tb.push(b);
        tb.popStackToClose("b", "div");
        assertEquals(4, tb.getStack().size());
        assertSame(span, tb.currentElement());
    }

    @Test
    public void testClearStackToContexts() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");

        tb.push(html);
        tb.push(body);
        tb.push(table);
        tb.push(tbody);
        tb.push(tr);
        tb.push(td);

        tb.clearStackToTableRowContext();
        assertSame(tr, tb.currentElement());

        tb.push(td);
        tb.clearStackToTableBodyContext();
        assertSame(tbody, tb.currentElement());

        tb.push(tr);
        tb.clearStackToTableContext();
        assertSame(table, tb.currentElement());

        tb.pop();
        tb.clearStackToTableContext();
        assertSame(html, tb.currentElement());
    }

    @Test
    public void testInsertStartTagAndElement() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Token.StartTag st = new Token.StartTag();
        st.nameAttr("div", new Attributes());
        Element el = tb.insert(st);
        assertEquals("div", el.tagName());
        assertSame(el, tb.currentElement());

        Element p = tb.insertStartTag("p");
        assertEquals("p", p.tagName());
        assertSame(p, tb.currentElement());
    }

    @Test
    public void testInsertSelfClosingTag() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Token.StartTag img = new Token.StartTag();
        img.nameAttr("img", new Attributes());
        img.selfClosing = true;
        Element imgEl = tb.insert(img);
        assertEquals("img", imgEl.tagName());

        Token.StartTag custom = new Token.StartTag();
        custom.nameAttr("custom-tag", new Attributes());
        custom.selfClosing = true;
        Element customEl = tb.insert(custom);
        assertEquals("custom-tag", customEl.tagName());
    }

    @Test
    public void testInsertFormAndControls() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formTag, true);
        assertSame(form, tb.getFormElement());
        assertSame(form, tb.currentElement());

        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        Element input = tb.insertEmpty(inputTag);
        assertEquals(1, form.elements().size());
        assertTrue(form.elements().contains(input));

        Token.StartTag formTag2 = new Token.StartTag();
        formTag2.nameAttr("form", new Attributes());
        FormElement form2 = tb.insertForm(formTag2, false);
        assertSame(form2, tb.getFormElement());
        assertNotSame(form2, tb.currentElement());
    }

    @Test
    public void testInsertCommentAndCharacter() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("hello world");
        tb.insert(commentToken);
        assertEquals(1, html.childNodeSize());
        assertTrue(html.childNode(0) instanceof Comment);

        Token.Character charToken = new Token.Character();
        charToken.data("text");
        tb.insert(charToken);
        assertEquals(2, html.childNodeSize());
        assertTrue(html.childNode(1) instanceof TextNode);

        Element script = tb.insertStartTag("script");
        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 1;");
        tb.insert(scriptChar);
        assertEquals(1, script.childNodeSize());
        assertTrue(script.childNode(0) instanceof DataNode);
    }

    @Test
    public void testInsertCommentBeforeStackInit() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("initial comment");
        tb.insert(commentToken);
        assertEquals(1, tb.getDocument().childNodeSize());
        assertTrue(tb.getDocument().childNode(0) instanceof Comment);
    }

    @Test
    public void testFosterParenting() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");
        Element table = tb.insertStartTag("table");
        tb.setFosterInserts(true);

        Element div = new Element(Tag.valueOf("div"), "");
        tb.insert(div);
        assertSame(body, div.parent());

        Element tableWithoutParent = new Element(Tag.valueOf("table"), "");
        tb.push(tableWithoutParent);
        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertInFosterParent(span);
        assertSame(table, span.parent());

        tb.getStack().clear();
        Element fragRoot = new Element(Tag.valueOf("html"), "");
        tb.push(fragRoot);
        Element em = new Element(Tag.valueOf("em"), "");
        tb.insertInFosterParent(em);
        assertSame(fragRoot, em.parent());
    }

    @Test
    public void testScopes() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);

        assertTrue(tb.inScope("p"));
        assertFalse(tb.inScope("table"));

        tb.push(table);
        tb.push(tr);
        tb.push(td);

        assertTrue(tb.inScope("td"));
        assertFalse(tb.inScope("p"));
        assertTrue(tb.inTableScope("table"));
        assertFalse(tb.inTableScope("body"));

        Element ul = new Element(Tag.valueOf("ul"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        tb.push(ul);
        tb.push(li);
        assertTrue(tb.inListItemScope("li"));
        assertFalse(tb.inListItemScope("td"));

        Element button = new Element(Tag.valueOf("button"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        tb.push(button);
        tb.push(span);
        assertTrue(tb.inButtonScope("span"));
        assertFalse(tb.inButtonScope("li"));

        Element optgroup = new Element(Tag.valueOf("optgroup"), "");
        Element option = new Element(Tag.valueOf("option"), "");
        tb.push(optgroup);
        tb.push(option);
        assertTrue(tb.inSelectScope("option"));
        assertFalse(tb.inSelectScope("button"));
    }

    @Test
    public void testGenerateImpliedEndTags() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        Element dt = new Element(Tag.valueOf("dt"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(li);
        tb.push(dt);

        tb.generateImpliedEndTags("p");
        assertEquals("p", tb.currentElement().nodeName());

        tb.generateImpliedEndTags();
        assertEquals("body", tb.currentElement().nodeName());
    }

    @Test
    public void testIsSpecial() {
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("body"), "")));
        assertTrue(tb.isSpecial(new Element(Tag.valueOf("table"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("b"), "")));
        assertFalse(tb.isSpecial(new Element(Tag.valueOf("span"), "")));
    }

    @Test
    public void testFormattingElements() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertNull(tb.lastFormattingElement());
        assertNull(tb.removeLastFormattingElement());

        Element b1 = new Element(Tag.valueOf("b"), "");
        b1.attr("class", "bold");
        Element b2 = new Element(Tag.valueOf("b"), "");
        b2.attr("class", "bold");
        Element b3 = new Element(Tag.valueOf("b"), "");
        b3.attr("class", "bold");
        Element b4 = new Element(Tag.valueOf("b"), "");
        b4.attr("class", "bold");

        tb.pushActiveFormattingElements(b1);
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        assertTrue(tb.isInActiveFormattingElements(b1));
        tb.pushActiveFormattingElements(b4);
        assertFalse(tb.isInActiveFormattingElements(b1));

        tb.insertMarkerToFormattingElements();
        assertNull(tb.lastFormattingElement());
        assertNull(tb.getActiveFormattingElement("b"));

        Element i = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i);
        assertSame(i, tb.getActiveFormattingElement("i"));

        Element iNew = new Element(Tag.valueOf("i"), "");
        tb.replaceActiveFormattingElement(i, iNew);
        assertSame(iNew, tb.getActiveFormattingElement("i"));

        tb.removeFromActiveFormattingElements(iNew);
        assertNull(tb.getActiveFormattingElement("i"));

        tb.clearFormattingElementsToLastMarker();
        assertEquals(3, tb.lastFormattingElement() != null ? 1 : 0);
    }

    @Test
    public void testReconstructFormattingElements() {
        tb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");

        Element b = new Element(Tag.valueOf("b"), "");
        b.attr("id", "b1");
        Element i = new Element(Tag.valueOf("i"), "");
        i.attr("id", "i1");

        tb.pushActiveFormattingElements(b);
        tb.pushActiveFormattingElements(i);

        tb.reconstructFormattingElements();

        assertEquals("i", tb.currentElement().tagName());
        assertEquals("b", tb.aboveOnStack(tb.currentElement()).tagName());
    }

    @Test
    public void testResetInsertionModeAllBranches() {
        assertModeTransition("select", HtmlTreeBuilderState.InSelect);
        assertModeTransition("td", HtmlTreeBuilderState.InCell);
        assertModeTransition("th", HtmlTreeBuilderState.InCell);
        assertModeTransition("tr", HtmlTreeBuilderState.InRow);
        assertModeTransition("tbody", HtmlTreeBuilderState.InTableBody);
        assertModeTransition("thead", HtmlTreeBuilderState.InTableBody);
        assertModeTransition("tfoot", HtmlTreeBuilderState.InTableBody);
        assertModeTransition("caption", HtmlTreeBuilderState.InCaption);
        assertModeTransition("colgroup", HtmlTreeBuilderState.InColumnGroup);
        assertModeTransition("table", HtmlTreeBuilderState.InTable);
        assertModeTransition("head", HtmlTreeBuilderState.InBody);
        assertModeTransition("body", HtmlTreeBuilderState.InBody);
        assertModeTransition("frameset", HtmlTreeBuilderState.InFrameset);
        assertModeTransition("html", HtmlTreeBuilderState.BeforeHead);
    }

    private void assertModeTransition(String tag, HtmlTreeBuilderState expectedState) {
        HtmlTreeBuilder htb = new HtmlTreeBuilder();
        htb.initialiseParse(new StringReader(""), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element el = new Element(Tag.valueOf(tag), "");
        htb.push(el);
        htb.resetInsertionMode();
        assertEquals("Failed for tag: " + tag, expectedState, htb.state());
    }

    @Test
    public void testParseFragmentContexts() {
        Document doc = new Document("http://example.com/");
        Element contextTitle = doc.appendElement("title");
        List<Node> nodesTitle = tb.parseFragment("Hello <b>world</b>", contextTitle, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertEquals(1, nodesTitle.size());

        Element contextIframe = doc.appendElement("iframe");
        List<Node> nodesIframe = tb.parseFragment("<div>content</div>", contextIframe, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertEquals(1, nodesIframe.size());

        Element contextScript = doc.appendElement("script");
        List<Node> nodesScript = tb.parseFragment("var x = 1;", contextScript, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertEquals(1, nodesScript.size());

        Element contextPlain = doc.appendElement("plaintext");
        List<Node> nodesPlain = tb.parseFragment("plain text", contextPlain, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertEquals(1, nodesPlain.size());

        FormElement form = doc.appendElement("form");
        Element contextDivInForm = form.appendElement("div");
        List<Node> nodesFormDiv = tb.parseFragment("<input name='foo'>", contextDivInForm, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertFalse(nodesFormDiv.isEmpty());

        List<Node> nodesNullContext = tb.parseFragment("<p>Para</p>", null, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        assertFalse(nodesNullContext.isEmpty());
    }

    @Test
    public void testToStringOutput() {
        tb.initialiseParse(new StringReader("<div></div>"), "", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.push(new Element(Tag.valueOf("html"), ""));
        String str = tb.toString();
        assertNotNull(str);
        assertTrue(str.contains("TreeBuilder{"));
        assertTrue(str.contains("state="));
    }
}
