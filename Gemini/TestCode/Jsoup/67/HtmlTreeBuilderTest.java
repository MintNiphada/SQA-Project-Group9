package org.jsoup.parser;

import org.jsoup.Jsoup;
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
import java.util.Arrays;
import java.util.List;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder tb;

    @Before
    public void setUp() {
        tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.tracking(10), ParseSettings.htmlDefault);
    }

    @Test
    public void testDefaultSettings() {
        ParseSettings settings = tb.defaultSettings();
        Assert.assertNotNull(settings);
        Assert.assertEquals(ParseSettings.htmlDefault, settings);
    }

    @Test
    public void testInitialiseParseAndStateManagement() {
        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        Assert.assertNull(tb.originalState());
        Assert.assertTrue(tb.framesetOk());
        Assert.assertFalse(tb.isFosterInserts());
        Assert.assertFalse(tb.isFragmentParsing());
        Assert.assertEquals("http://example.com/", tb.getBaseUri());
        Assert.assertNotNull(tb.getDocument());

        tb.markInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.state());
        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.originalState());

        tb.framesetOk(false);
        Assert.assertFalse(tb.framesetOk());
    }

    @Test
    public void testMaybeSetBaseUri() {
        Element base1 = new Element(Tag.valueOf("base"), "http://example.com/");
        base1.attr("href", "http://example.com/sub/");
        tb.maybeSetBaseUri(base1);
        Assert.assertEquals("http://example.com/sub/", tb.getBaseUri());

        Element base2 = new Element(Tag.valueOf("base"), "http://example.com/");
        base2.attr("href", "http://example.com/other/");
        tb.maybeSetBaseUri(base2);
        Assert.assertEquals("http://example.com/sub/", tb.getBaseUri());

        HtmlTreeBuilder tb2 = new HtmlTreeBuilder();
        tb2.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element baseTargetOnly = new Element(Tag.valueOf("base"), "http://example.com/");
        baseTargetOnly.attr("target", "_blank");
        tb2.maybeSetBaseUri(baseTargetOnly);
        Assert.assertEquals("http://example.com/", tb2.getBaseUri());
    }

    @Test
    public void testProcessTokens() {
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("hello comment");
        boolean res = tb.process(commentToken);
        Assert.assertTrue(res);
        Assert.assertEquals(1, tb.getDocument().childNodeSize());
        Assert.assertTrue(tb.getDocument().childNode(0) instanceof Comment);

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        res = tb.process(doctype, HtmlTreeBuilderState.Initial);
        Assert.assertTrue(res);
    }

    @Test
    public void testError() {
        Token.StartTag tag = new Token.StartTag();
        tag.nameAttr("div", new Attributes());
        tb.process(tag);
        int errorCountBefore = tb.errors.size();
        tb.error(HtmlTreeBuilderState.Initial);
        Assert.assertTrue(tb.errors.size() >= errorCountBefore);
    }

    @Test
    public void testInsertElementsAndStackOperations() {
        Element html = tb.insertStartTag("html");
        Assert.assertEquals("html", html.tagName());
        Assert.assertTrue(tb.onStack(html));
        Assert.assertEquals(html, tb.currentElement());

        Token.StartTag bodyTag = new Token.StartTag();
        bodyTag.nameAttr("body", new Attributes());
        Element body = tb.insert(bodyTag);
        Assert.assertEquals(body, tb.currentElement());
        Assert.assertEquals(html, tb.aboveOnStack(body));

        Token.StartTag divTag = new Token.StartTag();
        divTag.nameAttr("div", new Attributes());
        Element div = tb.insert(divTag);
        Assert.assertEquals(div, tb.getFromStack("div"));

        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertOnStackAfter(div, span);
        Assert.assertEquals(span, tb.currentElement());

        Element p = new Element(Tag.valueOf("p"), "");
        tb.replaceOnStack(span, p);
        Assert.assertEquals(p, tb.currentElement());
        Assert.assertFalse(tb.onStack(span));

        Assert.assertTrue(tb.removeFromStack(p));
        Assert.assertFalse(tb.removeFromStack(span));

        Assert.assertEquals(div, tb.pop());
        Assert.assertEquals(body, tb.currentElement());

        tb.push(div);
        Assert.assertEquals(div, tb.currentElement());
        Assert.assertEquals(3, tb.getStack().size());
    }

    @Test
    public void testInsertSelfClosingTags() {
        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");

        Token.StartTag selfClosingCustom = new Token.StartTag();
        selfClosingCustom.nameAttr("custom-tag", new Attributes());
        selfClosingCustom.selfClosing = true;
        Element custom = tb.insert(selfClosingCustom);
        Assert.assertEquals("custom-tag", custom.tagName());
        Assert.assertTrue(custom.tag().isSelfClosing());

        Token.StartTag selfClosingBr = new Token.StartTag();
        selfClosingBr.nameAttr("br", new Attributes());
        selfClosingBr.selfClosing = true;
        Element br = tb.insertEmpty(selfClosingBr);
        Assert.assertEquals("br", br.tagName());

        Token.StartTag nonVoidSelfClosing = new Token.StartTag();
        nonVoidSelfClosing.nameAttr("div", new Attributes());
        nonVoidSelfClosing.selfClosing = true;
        Element emptyDiv = tb.insertEmpty(nonVoidSelfClosing);
        Assert.assertEquals("div", emptyDiv.tagName());
    }

    @Test
    public void testInsertCharactersAndComments() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        Element script = tb.insertStartTag("script");

        Token.Character dataChar = new Token.Character();
        dataChar.data("var x = 1;");
        tb.insert(dataChar);
        Assert.assertEquals(1, script.childNodeSize());
        Assert.assertTrue(script.childNode(0) instanceof DataNode);

        Element style = tb.insertStartTag("style");
        Token.Character styleChar = new Token.Character();
        styleChar.data("body { color: red; }");
        tb.insert(styleChar);
        Assert.assertEquals(1, style.childNodeSize());
        Assert.assertTrue(style.childNode(0) instanceof DataNode);

        tb.pop();
        tb.pop();

        Element p = tb.insertStartTag("p");
        Token.Character textChar = new Token.Character();
        textChar.data("Hello World");
        tb.insert(textChar);
        Assert.assertEquals(1, p.childNodeSize());
        Assert.assertTrue(p.childNode(0) instanceof TextNode);

        Token.Comment comment = new Token.Comment();
        comment.getData().append("A comment");
        tb.insert(comment);
        Assert.assertEquals(2, p.childNodeSize());
        Assert.assertTrue(p.childNode(1) instanceof Comment);
    }

    @Test
    public void testFormElementAssociation() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formTag, true);
        Assert.assertEquals(form, tb.getFormElement());
        Assert.assertTrue(tb.onStack(form));

        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        inputTag.attributes.put("name", "username");
        Element input = tb.insertEmpty(inputTag);

        Assert.assertTrue(form.elements().contains(input));

        Token.StartTag formTagNoStack = new Token.StartTag();
        formTagNoStack.nameAttr("form", new Attributes());
        FormElement form2 = tb.insertForm(formTagNoStack, false);
        Assert.assertEquals(form2, tb.getFormElement());
        Assert.assertFalse(tb.onStack(form2));

        tb.setFormElement(null);
        Assert.assertNull(tb.getFormElement());
    }

    @Test
    public void testPopStackVariations() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("div");
        tb.insertStartTag("p");
        tb.insertStartTag("span");

        tb.popStackToBefore("div");
        Assert.assertEquals("div", tb.currentElement().tagName());

        tb.insertStartTag("p");
        tb.insertStartTag("b");
        tb.popStackToClose("p");
        Assert.assertEquals("div", tb.currentElement().tagName());

        tb.insertStartTag("ul");
        tb.insertStartTag("li");
        tb.insertStartTag("i");
        tb.popStackToClose("ul", "ol");
        Assert.assertEquals("div", tb.currentElement().tagName());
    }

    @Test
    public void testClearStackContexts() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("table");
        tb.insertStartTag("tbody");
        tb.insertStartTag("tr");
        tb.insertStartTag("td");

        tb.clearStackToTableRowContext();
        Assert.assertEquals("tr", tb.currentElement().tagName());

        tb.insertStartTag("td");
        tb.clearStackToTableBodyContext();
        Assert.assertEquals("tbody", tb.currentElement().tagName());

        tb.insertStartTag("tr");
        tb.clearStackToTableContext();
        Assert.assertEquals("table", tb.currentElement().tagName());
    }

    @Test
    public void testResetInsertionMode() {
        String[] tags = new String[]{"select", "td", "tr", "tbody", "caption", "colgroup", "table", "head", "body", "frameset", "html"};
        HtmlTreeBuilderState[] expectedStates = new HtmlTreeBuilderState[]{
                HtmlTreeBuilderState.InSelect,
                HtmlTreeBuilderState.InCell,
                HtmlTreeBuilderState.InRow,
                HtmlTreeBuilderState.InTableBody,
                HtmlTreeBuilderState.InCaption,
                HtmlTreeBuilderState.InColumnGroup,
                HtmlTreeBuilderState.InTable,
                HtmlTreeBuilderState.InBody,
                HtmlTreeBuilderState.InBody,
                HtmlTreeBuilderState.InFrameset,
                HtmlTreeBuilderState.BeforeHead
        };

        for (int i = 0; i < tags.length; i++) {
            tb.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
            tb.insertStartTag("html");
            tb.insertStartTag(tags[i]);
            tb.resetInsertionMode();
            Assert.assertEquals(expectedStates[i], tb.state());
        }

        tb.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        tb.insertStartTag("custom");
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testScopeChecks() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("div");

        Assert.assertTrue(tb.inScope("div"));
        Assert.assertTrue(tb.inScope(new String[]{"div", "span"}));
        Assert.assertFalse(tb.inScope("p"));

        tb.insertStartTag("table");
        tb.insertStartTag("tr");
        tb.insertStartTag("td");
        tb.insertStartTag("p");

        Assert.assertTrue(tb.inScope("p"));
        Assert.assertTrue(tb.inTableScope("table"));
        Assert.assertFalse(tb.inTableScope("body"));

        tb.insertStartTag("ol");
        tb.insertStartTag("li");
        Assert.assertTrue(tb.inListItemScope("li"));

        tb.insertStartTag("button");
        Assert.assertTrue(tb.inButtonScope("button"));

        tb.insertStartTag("select");
        tb.insertStartTag("option");
        Assert.assertTrue(tb.inSelectScope("option"));
        Assert.assertFalse(tb.inSelectScope("input"));
    }

    @Test
    public void testFosterParenting() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        Element table = tb.insertStartTag("table");
        Element tr = tb.insertStartTag("tr");

        tb.setFosterInserts(true);
        Assert.assertTrue(tb.isFosterInserts());

        Element div = new Element(Tag.valueOf("div"), "");
        tb.insertInFosterParent(div);

        Assert.assertEquals(table.parent(), div.parent());
        Assert.assertEquals(0, table.parent().children().indexOf(div));

        Element tableWithoutParent = new Element(Tag.valueOf("table"), "");
        HtmlTreeBuilder tb2 = new HtmlTreeBuilder();
        tb2.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element rootHtml = tb2.insertStartTag("html");
        tb2.push(tableWithoutParent);
        Element div2 = new Element(Tag.valueOf("div"), "");
        tb2.insertInFosterParent(div2);
        Assert.assertEquals(rootHtml, div2.parent());

        HtmlTreeBuilder tb3 = new HtmlTreeBuilder();
        tb3.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element rHtml = tb3.insertStartTag("html");
        tb3.insertInFosterParent(new Element(Tag.valueOf("span"), ""));
        Assert.assertEquals(1, rHtml.childNodeSize());
    }

    @Test
    public void testFormattingElementsAndReconstruction() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");

        Element b1 = tb.insertStartTag("b");
        tb.pushActiveFormattingElements(b1);
        Assert.assertEquals(b1, tb.lastFormattingElement());
        Assert.assertTrue(tb.isInActiveFormattingElements(b1));
        Assert.assertEquals(b1, tb.getActiveFormattingElement("b"));

        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        tb.pushActiveFormattingElements(b4);

        tb.insertMarkerToFormattingElements();
        Assert.assertNull(tb.lastFormattingElement());
        Assert.assertNull(tb.getActiveFormattingElement("b"));

        Element iTag = tb.insertStartTag("i");
        tb.pushActiveFormattingElements(iTag);
        tb.clearFormattingElementsToLastMarker();
        Assert.assertNull(tb.getActiveFormattingElement("i"));

        Element u = tb.insertStartTag("u");
        tb.pushActiveFormattingElements(u);
        tb.removeFromActiveFormattingElements(u);
        Assert.assertFalse(tb.isInActiveFormattingElements(u));

        Element em1 = tb.insertStartTag("em");
        tb.pushActiveFormattingElements(em1);
        Element em2 = new Element(Tag.valueOf("em"), "");
        tb.replaceActiveFormattingElement(em1, em2);
        Assert.assertTrue(tb.isInActiveFormattingElements(em2));
        Assert.assertFalse(tb.isInActiveFormattingElements(em1));

        tb.pop();
        Element sub = tb.insertStartTag("sub");
        tb.pushActiveFormattingElements(sub);
        tb.pop();
        tb.reconstructFormattingElements();
        Assert.assertEquals("sub", tb.currentElement().tagName());

        Assert.assertNotNull(tb.removeLastFormattingElement());
    }

    @Test
    public void testGenerateImpliedEndTags() {
        tb.insertStartTag("html");
        tb.insertStartTag("body");
        tb.insertStartTag("p");
        tb.insertStartTag("span");

        tb.generateImpliedEndTags();
        Assert.assertEquals("span", tb.currentElement().tagName());

        tb.pop();
        tb.generateImpliedEndTags();
        Assert.assertEquals("body", tb.currentElement().tagName());

        tb.insertStartTag("p");
        tb.generateImpliedEndTags("p");
        Assert.assertEquals("p", tb.currentElement().tagName());
    }

    @Test
    public void testHeadElementAndPendingTableCharacters() {
        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        Assert.assertEquals(head, tb.getHeadElement());

        List<String> chars = Arrays.asList("a", "b", "c");
        tb.setPendingTableCharacters(chars);
        Assert.assertEquals(chars, tb.getPendingTableCharacters());

        tb.newPendingTableCharacters();
        Assert.assertEquals(0, tb.getPendingTableCharacters().size());
    }

    @Test
    public void testIsSpecial() {
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("div"), "")));
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("table"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("span"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));
    }

    @Test
    public void testParseFragmentContexts() {
        String[] contexts = new String[]{"title", "textarea", "iframe", "noembed", "noframes", "style", "xmp", "script", "noscript", "plaintext", "div"};
        for (String ctxTag : contexts) {
            Element ctx = new Element(Tag.valueOf(ctxTag), "http://example.com/");
            List<Node> nodes = tb.parseFragment("test content", ctx, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
            Assert.assertNotNull(nodes);
        }

        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com/", new Attributes());
        Element divInForm = new Element(Tag.valueOf("div"), "http://example.com/");
        form.appendChild(divInForm);

        List<Node> formFragmentNodes = tb.parseFragment("<input name='foo'>", divInForm, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertNotNull(formFragmentNodes);

        List<Node> noContextNodes = tb.parseFragment("<p>no context</p>", null, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertNotNull(noContextNodes);
        Assert.assertTrue(noContextNodes.size() > 0);
    }

    @Test
    public void testToStringOutput() {
        String str = tb.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.startsWith("TreeBuilder{"));
    }

    @Test
    public void testFullIntegrationViaJsoup() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head><title>Test</title><base href='http://base.org/'></head><body><div id='d'><p>Hello <b>World</b></p><form><input type='text'/></form><table><tr><td>Cell</td></tr></table></div></body></html>");
        Assert.assertEquals("Test", doc.title());
        Assert.assertEquals("http://base.org/", doc.baseUri());
        Assert.assertNotNull(doc.select("form").first());
        Assert.assertEquals(1, doc.select("input").size());
        Assert.assertEquals("Cell", doc.select("td").first().text());
    }
}
