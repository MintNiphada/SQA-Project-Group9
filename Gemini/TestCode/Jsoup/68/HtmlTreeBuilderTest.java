package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder tb;

    @Before
    public void setUp() {
        tb = new HtmlTreeBuilder();
    }

    @Test
    public void testInitialiseParseAndDefaults() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        tb.initialiseParse(new StringReader(""), "http://example.com", errors, ParseSettings.htmlDefault);

        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        Assert.assertNull(tb.originalState());
        Assert.assertNull(tb.getHeadElement());
        Assert.assertNull(tb.getFormElement());
        Assert.assertTrue(tb.framesetOk());
        Assert.assertFalse(tb.isFosterInserts());
        Assert.assertFalse(tb.isFragmentParsing());
        Assert.assertEquals("http://example.com", tb.getBaseUri());
        Assert.assertNotNull(tb.getDocument());
        Assert.assertNotNull(tb.defaultSettings());
    }

    @Test
    public void testStateAndFramesetTransitions() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.transition(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        tb.markInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.framesetOk(false);
        Assert.assertFalse(tb.framesetOk());
        tb.framesetOk(true);
        Assert.assertTrue(tb.framesetOk());
    }

    @Test
    public void testBaseUriHandling() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element baseWithoutHref = new Element(Tag.valueOf("base"), "http://example.com");
        tb.maybeSetBaseUri(baseWithoutHref);
        Assert.assertEquals("http://example.com", tb.getBaseUri());

        Element baseWithHref = new Element(Tag.valueOf("base"), "http://example.com");
        baseWithHref.attr("href", "http://example.com/sub/");
        tb.maybeSetBaseUri(baseWithHref);
        Assert.assertEquals("http://example.com/sub/", tb.getBaseUri());
        Assert.assertEquals("http://example.com/sub/", tb.getDocument().baseUri());

        Element baseIgnored = new Element(Tag.valueOf("base"), "http://example.com");
        baseIgnored.attr("href", "http://another.com/");
        tb.maybeSetBaseUri(baseIgnored);
        Assert.assertEquals("http://example.com/sub/", tb.getBaseUri());
    }

    @Test
    public void testInsertElementsAndPop() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element root = tb.insertStartTag("html");
        Assert.assertEquals(1, tb.getStack().size());
        Assert.assertEquals("html", tb.currentElement().nodeName());

        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.nameAttr("body", new Attributes());
        Element body = tb.insert(bodyTag);
        Assert.assertEquals(2, tb.getStack().size());
        Assert.assertTrue(tb.onStack(body));
        Assert.assertEquals(root, tb.aboveOnStack(body));

        Element popped = tb.pop();
        Assert.assertEquals(body, popped);
        Assert.assertEquals(1, tb.getStack().size());
        Assert.assertFalse(tb.onStack(body));
    }

    @Test
    public void testInsertSelfClosingTags() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(10), ParseSettings.htmlDefault);
        tb.insertStartTag("html");

        Token.StartTag knownTag = new Token.StartTag();
        knownTag.nameAttr("div", new Attributes());
        knownTag.selfClosing = true;
        Element div = tb.insertEmpty(knownTag);
        Assert.assertFalse(div.tag().isSelfClosing());

        Token.StartTag unknownTag = new Token.StartTag();
        unknownTag.nameAttr("custom-tag", new Attributes());
        unknownTag.selfClosing = true;
        Element custom = tb.insertEmpty(unknownTag);
        Assert.assertTrue(custom.tag().isSelfClosing());

        Token.StartTag scriptTag = new Token.StartTag();
        scriptTag.nameAttr("script", new Attributes());
        scriptTag.selfClosing = true;
        Element script = tb.insert(scriptTag);
        Assert.assertEquals("script", script.nodeName());
    }

    @Test
    public void testInsertFormAndControls() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        tb.insertStartTag("body");

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formTag, true);
        Assert.assertEquals(form, tb.getFormElement());
        Assert.assertTrue(tb.onStack(form));

        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        Element input = tb.insertEmpty(inputTag);
        Assert.assertTrue(form.elements().contains(input));

        Token.StartTag form2Tag = new Token.StartTag();
        form2Tag.nameAttr("form", new Attributes());
        FormElement form2 = tb.insertForm(form2Tag, false);
        Assert.assertEquals(form2, tb.getFormElement());
        Assert.assertFalse(tb.onStack(form2));
    }

    @Test
    public void testInsertCommentAndCharacters() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Token.Comment comment = new Token.Comment();
        comment.getData().append("doc comment");
        tb.insert(comment);
        Assert.assertEquals("doc comment", ((Comment) tb.getDocument().childNode(0)).getData());

        tb.insertStartTag("html");
        tb.insertStartTag("body");

        Token.Character charToken = new Token.Character();
        charToken.data("hello text");
        tb.insert(charToken);
        Assert.assertTrue(tb.currentElement().childNode(0) instanceof TextNode);

        tb.insertStartTag("script");
        Token.Character scriptChars = new Token.Character();
        scriptChars.data("var x = 1;");
        tb.insert(scriptChars);
        Assert.assertTrue(tb.currentElement().childNode(0) instanceof DataNode);
    }

    @Test
    public void testStackManipulations() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");
        Element div = tb.insertStartTag("div");
        Element p = tb.insertStartTag("p");

        Assert.assertEquals(p, tb.getFromStack("p"));
        Assert.assertNull(tb.getFromStack("span"));

        Assert.assertTrue(tb.removeFromStack(div));
        Assert.assertFalse(tb.removeFromStack(div));
        Assert.assertFalse(tb.onStack(div));

        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertOnStackAfter(body, span);
        Assert.assertEquals(1, tb.getStack().indexOf(body));
        Assert.assertEquals(2, tb.getStack().indexOf(span));

        Element section = new Element(Tag.valueOf("section"), "");
        tb.replaceOnStack(span, section);
        Assert.assertFalse(tb.onStack(span));
        Assert.assertEquals(2, tb.getStack().indexOf(section));

        tb.push(div);
        tb.popStackToBefore("div");
        Assert.assertEquals("div", tb.currentElement().nodeName());

        tb.popStackToClose("div");
        Assert.assertFalse(tb.onStack(div));

        tb.push(new Element(Tag.valueOf("ul"), ""));
        tb.push(new Element(Tag.valueOf("li"), ""));
        tb.popStackToClose("li", "ul");
        Assert.assertFalse(tb.onStack(tb.getFromStack("li")));
    }

    @Test
    public void testClearStackToContexts() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("table");
        tb.insertStartTag("tbody");
        tb.insertStartTag("tr");
        tb.insertStartTag("td");

        tb.clearStackToTableRowContext();
        Assert.assertEquals("tr", tb.currentElement().nodeName());

        tb.insertStartTag("td");
        tb.clearStackToTableBodyContext();
        Assert.assertEquals("tbody", tb.currentElement().nodeName());

        tb.insertStartTag("tr");
        tb.clearStackToTableContext();
        Assert.assertEquals("table", tb.currentElement().nodeName());
    }

    @Test
    public void testScopes() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("table");
        tb.insertStartTag("tr");
        tb.insertStartTag("td");

        Assert.assertTrue(tb.inScope("td"));
        Assert.assertTrue(tb.inScope("table"));
        Assert.assertFalse(tb.inScope("body"));
        Assert.assertFalse(tb.inTableScope("body"));
        Assert.assertTrue(tb.inTableScope("table"));

        tb.insertStartTag("ol");
        tb.insertStartTag("li");
        Assert.assertTrue(tb.inListItemScope("li"));
        Assert.assertFalse(tb.inListItemScope("body"));

        tb.insertStartTag("button");
        Assert.assertTrue(tb.inButtonScope("button"));
        Assert.assertFalse(tb.inButtonScope("li"));

        tb.insertStartTag("select");
        tb.insertStartTag("option");
        Assert.assertTrue(tb.inSelectScope("option"));
        Assert.assertFalse(tb.inSelectScope("div"));
    }

    @Test
    public void testActiveFormattingElements() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        tb.insertStartTag("body");

        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        tb.pushActiveFormattingElements(b1);
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        Assert.assertTrue(tb.isInActiveFormattingElements(b1));

        tb.pushActiveFormattingElements(b4);
        Assert.assertFalse(tb.isInActiveFormattingElements(b1));
        Assert.assertTrue(tb.isInActiveFormattingElements(b4));

        Assert.assertEquals(b4, tb.lastFormattingElement());
        Assert.assertEquals(b4, tb.getActiveFormattingElement("b"));
        Assert.assertNull(tb.getActiveFormattingElement("i"));

        Element bReplacement = new Element(Tag.valueOf("b"), "");
        tb.replaceActiveFormattingElement(b4, bReplacement);
        Assert.assertEquals(bReplacement, tb.lastFormattingElement());

        tb.insertMarkerToFormattingElements();
        Assert.assertNull(tb.lastFormattingElement());
        Assert.assertNull(tb.getActiveFormattingElement("b"));

        tb.clearFormattingElementsToLastMarker();
        Assert.assertEquals(bReplacement, tb.lastFormattingElement());

        tb.removeFromActiveFormattingElements(bReplacement);
        Assert.assertFalse(tb.isInActiveFormattingElements(bReplacement));

        while (tb.removeLastFormattingElement() != null) {}
        Assert.assertNull(tb.removeLastFormattingElement());
    }

    @Test
    public void testReconstructFormattingElements() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        tb.insertStartTag("body");

        Element i = new Element(Tag.valueOf("i"), "");
        i.attr("class", "italics");
        tb.pushActiveFormattingElements(i);

        tb.reconstructFormattingElements();
        Assert.assertEquals("i", tb.currentElement().nodeName());
        Assert.assertEquals("italics", tb.currentElement().attr("class"));
        Assert.assertTrue(tb.onStack(tb.lastFormattingElement()));

        tb.reconstructFormattingElements();
    }

    @Test
    public void testGenerateImpliedEndTags() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("p");

        tb.generateImpliedEndTags("p");
        Assert.assertEquals("p", tb.currentElement().nodeName());

        tb.generateImpliedEndTags();
        Assert.assertEquals("body", tb.currentElement().nodeName());
    }

    @Test
    public void testFosterParenting() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");
        tb.insertStartTag("table");

        tb.setFosterInserts(true);
        Assert.assertTrue(tb.isFosterInserts());

        Token.Character fosterText = new Token.Character();
        fosterText.data("fostered");
        Element textHolder = new Element(Tag.valueOf("span"), "");
        tb.insertInFosterParent(textHolder);

        Assert.assertEquals(body, textHolder.parent());
        Assert.assertEquals(0, body.children().indexOf(textHolder));
    }

    @Test
    public void testResetInsertionMode() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("select");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InSelect, tb.state());

        tb.pop();
        tb.insertStartTag("table");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InTable, tb.state());

        tb.insertStartTag("tr");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InRow, tb.state());

        tb.insertStartTag("th");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InCell, tb.state());

        tb.pop();
        tb.pop();
        tb.insertStartTag("tbody");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InTableBody, tb.state());

        tb.pop();
        tb.insertStartTag("caption");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InCaption, tb.state());

        tb.pop();
        tb.insertStartTag("colgroup");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InColumnGroup, tb.state());

        tb.pop();
        tb.pop();
        tb.insertStartTag("frameset");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InFrameset, tb.state());
    }

    @Test
    public void testSpecialTagsAndPendingCharacters() {
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("div"), "")));
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("span"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));

        tb.setHeadElement(new Element(Tag.valueOf("head"), ""));
        Assert.assertNotNull(tb.getHeadElement());

        tb.newPendingTableCharacters();
        Assert.assertNotNull(tb.getPendingTableCharacters());
        List<String> chars = new ArrayList<>();
        chars.add("abc");
        tb.setPendingTableCharacters(chars);
        Assert.assertEquals(1, tb.getPendingTableCharacters().size());
    }

    @Test
    public void testParseFragmentScenarios() {
        Element contextDiv = new Element(Tag.valueOf("div"), "http://example.com");
        List<Node> nodes = tb.parseFragment("<span>text</span>", contextDiv, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("span", nodes.get(0).nodeName());

        Element contextTitle = new Element(Tag.valueOf("title"), "http://example.com");
        List<Node> titleNodes = tb.parseFragment("some title &amp; text", contextTitle, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertFalse(titleNodes.isEmpty());

        Element contextScript = new Element(Tag.valueOf("script"), "http://example.com");
        List<Node> scriptNodes = tb.parseFragment("alert(1);", contextScript, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertFalse(scriptNodes.isEmpty());

        List<Node> noContextNodes = tb.parseFragment("<p>no context</p>", null, "http://example.com", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertFalse(noContextNodes.isEmpty());
    }

    @Test
    public void testProcessTokenAndToString() {
        tb.initialiseParse(new StringReader(""), "http://example.com", ParseErrorList.tracking(10), ParseSettings.htmlDefault);
        Token.Comment comment = new Token.Comment();
        comment.getData().append("test");
        boolean processed = tb.process(comment);
        Assert.assertTrue(processed);

        boolean processedDirect = tb.process(comment, HtmlTreeBuilderState.Initial);
        Assert.assertTrue(processedDirect);

        tb.error(HtmlTreeBuilderState.Initial);

        String str = tb.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("TreeBuilder{"));
    }
}
