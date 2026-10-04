package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
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
    private HtmlTreeBuilder builder;

    @Before
    public void setUp() {
        builder = new HtmlTreeBuilder();
    }

    @Test
    public void testDefaultSettings() {
        ParseSettings settings = builder.defaultSettings();
        Assert.assertNotNull(settings);
        Assert.assertFalse(settings.preserveTagCase());
        Assert.assertFalse(settings.preserveAttributeCase());
    }

    @Test
    public void testInitialiseParseAndGetters() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        builder.initialiseParse(new StringReader("<div></div>"), "http://example.com/", errors, ParseSettings.htmlDefault);
        Assert.assertEquals(HtmlTreeBuilderState.Initial, builder.state());
        Assert.assertNull(builder.originalState());
        Assert.assertEquals("http://example.com/", builder.getBaseUri());
        Assert.assertTrue(builder.framesetOk());
        Assert.assertFalse(builder.isFosterInserts());
        Assert.assertFalse(builder.isFragmentParsing());
        Assert.assertNotNull(builder.getDocument());
    }

    @Test
    public void testStateTransitionsAndMark() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        builder.transition(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(HtmlTreeBuilderState.InBody, builder.state());
        builder.markInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InBody, builder.originalState());
        builder.framesetOk(false);
        Assert.assertFalse(builder.framesetOk());
    }

    @Test
    public void testBaseUriHandling() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element base1 = new Element(Tag.valueOf("base"), "http://example.com/");
        base1.attr("href", "http://example.com/sub/");
        builder.maybeSetBaseUri(base1);
        Assert.assertEquals("http://example.com/sub/", builder.getBaseUri());
        Assert.assertEquals("http://example.com/sub/", builder.getDocument().baseUri());

        Element base2 = new Element(Tag.valueOf("base"), "http://example.com/sub/");
        base2.attr("href", "http://other.com/");
        builder.maybeSetBaseUri(base2);
        Assert.assertEquals("http://example.com/sub/", builder.getBaseUri());

        HtmlTreeBuilder b2 = new HtmlTreeBuilder();
        b2.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element baseWithoutHref = new Element(Tag.valueOf("base"), "http://example.com/");
        baseWithoutHref.attr("target", "_blank");
        b2.maybeSetBaseUri(baseWithoutHref);
        Assert.assertEquals("http://example.com/", b2.getBaseUri());
    }

    @Test
    public void testStackOperations() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div);
        builder.push(span);

        Assert.assertEquals(4, builder.getStack().size());
        Assert.assertTrue(builder.onStack(div));
        Assert.assertEquals(span, builder.currentElement());
        Assert.assertEquals(div, builder.aboveOnStack(span));
        Assert.assertEquals(body, builder.aboveOnStack(div));

        Assert.assertEquals(div, builder.getFromStack("div"));
        Assert.assertNull(builder.getFromStack("table"));

        Element popped = builder.pop();
        Assert.assertEquals(span, popped);
        Assert.assertFalse(builder.onStack(span));

        Element p = new Element(Tag.valueOf("p"), "");
        builder.insertOnStackAfter(body, p);
        Assert.assertEquals(2, builder.getStack().indexOf(p));

        Element article = new Element(Tag.valueOf("article"), "");
        builder.replaceOnStack(p, article);
        Assert.assertEquals(2, builder.getStack().indexOf(article));
        Assert.assertFalse(builder.onStack(p));

        Assert.assertTrue(builder.removeFromStack(article));
        Assert.assertFalse(builder.removeFromStack(p));
    }

    @Test
    public void testPopStackVariants() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div1 = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div1);
        builder.push(p);
        builder.push(span);

        builder.popStackToBefore("p");
        Assert.assertEquals(p, builder.currentElement());

        builder.push(span);
        builder.popStackToClose("p");
        Assert.assertEquals(div1, builder.currentElement());

        builder.push(p);
        builder.push(span);
        builder.popStackToClose("span", "div");
        Assert.assertEquals(p, builder.currentElement());
    }

    @Test
    public void testClearStackToContexts() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(table);
        builder.push(tbody);
        builder.push(tr);
        builder.push(td);
        builder.push(p);

        builder.clearStackToTableRowContext();
        Assert.assertEquals(tr, builder.currentElement());

        builder.push(td);
        builder.push(p);
        builder.clearStackToTableBodyContext();
        Assert.assertEquals(tbody, builder.currentElement());

        builder.push(tr);
        builder.push(td);
        builder.clearStackToTableContext();
        Assert.assertEquals(table, builder.currentElement());
    }

    @Test
    public void testScopes() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element button = new Element(Tag.valueOf("button"), "");
        Element ol = new Element(Tag.valueOf("ol"), "");
        Element li = new Element(Tag.valueOf("li"), "");

        builder.push(html);
        builder.push(body);
        builder.push(table);
        builder.push(tr);
        builder.push(button);
        builder.push(ol);
        builder.push(li);

        Assert.assertTrue(builder.inListItemScope("li"));
        Assert.assertFalse(builder.inListItemScope("body"));
        Assert.assertTrue(builder.inButtonScope("button"));
        Assert.assertTrue(builder.inScope(new String[]{"li", "ol"}));
        Assert.assertTrue(builder.inTableScope("table"));
        Assert.assertFalse(builder.inTableScope("body"));
    }

    @Test
    public void testInSelectScope() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element optgroup = new Element(Tag.valueOf("optgroup"), "");
        Element option = new Element(Tag.valueOf("option"), "");

        builder.push(optgroup);
        builder.push(option);
        Assert.assertTrue(builder.inSelectScope("option"));
        Assert.assertTrue(builder.inSelectScope("optgroup"));
        Assert.assertFalse(builder.inSelectScope("div"));
    }

    @Test
    public void testActiveFormattingElementsOperations() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        builder.pushActiveFormattingElements(b1);
        builder.pushActiveFormattingElements(b2);
        builder.pushActiveFormattingElements(b3);
        Assert.assertEquals(3, builder.formattingElements.size());

        builder.pushActiveFormattingElements(b4);
        Assert.assertEquals(3, builder.formattingElements.size());
        Assert.assertFalse(builder.isInActiveFormattingElements(b1));
        Assert.assertTrue(builder.isInActiveFormattingElements(b4));

        Assert.assertEquals(b4, builder.lastFormattingElement());
        Assert.assertEquals(b4, builder.getActiveFormattingElement("b"));
        Assert.assertNull(builder.getActiveFormattingElement("i"));

        Element bReplacement = new Element(Tag.valueOf("b"), "");
        builder.replaceActiveFormattingElement(b4, bReplacement);
        Assert.assertEquals(bReplacement, builder.lastFormattingElement());

        builder.removeFromActiveFormattingElements(bReplacement);
        Assert.assertEquals(b3, builder.lastFormattingElement());

        builder.insertMarkerToFormattingElements();
        Assert.assertNull(builder.lastFormattingElement());
        Assert.assertNull(builder.getActiveFormattingElement("b"));

        Element i = new Element(Tag.valueOf("i"), "");
        builder.pushActiveFormattingElements(i);
        Assert.assertEquals(i, builder.lastFormattingElement());

        builder.clearFormattingElementsToLastMarker();
        Assert.assertEquals(b3, builder.lastFormattingElement());

        Assert.assertEquals(b3, builder.removeLastFormattingElement());
        Assert.assertEquals(b2, builder.removeLastFormattingElement());
        Assert.assertNull(builder.removeLastFormattingElement());
        Assert.assertNull(builder.lastFormattingElement());
    }

    @Test
    public void testReconstructFormattingElements() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        builder.push(html);
        builder.push(body);

        Element b = new Element(Tag.valueOf("b"), "");
        b.attr("class", "bold");
        builder.pushActiveFormattingElements(b);

        builder.reconstructFormattingElements();
        Assert.assertEquals("b", builder.currentElement().nodeName());
        Assert.assertEquals("bold", builder.currentElement().attr("class"));
        Assert.assertTrue(builder.onStack(builder.lastFormattingElement()));
    }

    @Test
    public void testGenerateImpliedEndTags() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");

        builder.push(html);
        builder.push(body);
        builder.push(p);
        builder.push(li);

        builder.generateImpliedEndTags("p");
        Assert.assertEquals(p, builder.currentElement());

        builder.push(li);
        builder.generateImpliedEndTags();
        Assert.assertEquals(body, builder.currentElement());
    }

    @Test
    public void testInsertStartTagAndTokens() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("div", new Attributes());
        Element el = builder.insert(startTag);
        Assert.assertEquals("div", el.nodeName());
        Assert.assertEquals(el, builder.currentElement());

        Element span = builder.insertStartTag("span");
        Assert.assertEquals("span", span.nodeName());
        Assert.assertEquals(span, builder.currentElement());

        Token.Comment comment = new Token.Comment();
        comment.getData().append("test comment");
        builder.insert(comment);
        Assert.assertEquals(1, span.childNodeSize());

        Token.Character character = new Token.Character();
        character.data("text");
        builder.insert(character);
        Assert.assertEquals(2, span.childNodeSize());
        Assert.assertTrue(span.childNode(1) instanceof TextNode);

        Token.StartTag scriptTag = new Token.StartTag();
        scriptTag.nameAttr("script", new Attributes());
        Element script = builder.insert(scriptTag);
        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 1;");
        builder.insert(scriptChar);
        Assert.assertEquals("var x = 1;", script.data());
    }

    @Test
    public void testInsertSelfClosingAndEmptyTags() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        builder.initialiseParse(new StringReader(""), "http://example.com/", errors, ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);

        Token.StartTag imgTag = new Token.StartTag();
        imgTag.nameAttr("img", new Attributes());
        imgTag.selfClosing = true;
        Element img = builder.insertEmpty(imgTag);
        Assert.assertEquals("img", img.nodeName());

        Token.StartTag divTag = new Token.StartTag();
        divTag.nameAttr("div", new Attributes());
        divTag.selfClosing = true;
        Element div = builder.insertEmpty(divTag);
        Assert.assertEquals("div", div.nodeName());
        Assert.assertTrue(errors.size() > 0);

        Token.StartTag customTag = new Token.StartTag();
        customTag.nameAttr("custom-el", new Attributes());
        customTag.selfClosing = true;
        Element custom = builder.insert(customTag);
        Assert.assertEquals("custom-el", custom.nodeName());
    }

    @Test
    public void testInsertForm() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = builder.insertForm(formTag, true);
        Assert.assertEquals(form, builder.getFormElement());
        Assert.assertEquals(form, builder.currentElement());

        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        Element input = builder.insertEmpty(inputTag);
        Assert.assertTrue(form.elements().contains(input));
    }

    @Test
    public void testHeadElementAndFosterInsertsAndPendingTableCharacters() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element head = new Element(Tag.valueOf("head"), "");
        builder.setHeadElement(head);
        Assert.assertEquals(head, builder.getHeadElement());

        builder.setFosterInserts(true);
        Assert.assertTrue(builder.isFosterInserts());

        List<String> chars = new ArrayList<>(Arrays.asList("a", "b"));
        builder.setPendingTableCharacters(chars);
        Assert.assertEquals(chars, builder.getPendingTableCharacters());
        builder.newPendingTableCharacters();
        Assert.assertTrue(builder.getPendingTableCharacters().isEmpty());
    }

    @Test
    public void testFosterParenting() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        html.appendChild(body);
        body.appendChild(table);

        builder.push(html);
        builder.push(body);
        builder.push(table);

        builder.setFosterInserts(true);
        TextNode text = new TextNode("fostered");
        builder.insertInFosterParent(text);
        Assert.assertEquals(text, table.previousSibling());
    }

    @Test
    public void testResetInsertionMode() {
        builder.initialiseParse(new StringReader(""), "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element select = new Element(Tag.valueOf("select"), "");

        builder.push(html);
        builder.push(body);
        builder.push(select);
        builder.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InSelect, builder.state());

        builder.pop();
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");

        builder.push(table);
        builder.push(tbody);
        builder.push(tr);
        builder.push(td);

        builder.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InCell, builder.state());

        builder.pop();
        builder.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InRow, builder.state());

        builder.pop();
        builder.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InTableBody, builder.state());

        builder.pop();
        builder.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InTable, builder.state());
    }

    @Test
    public void testIsSpecial() {
        Assert.assertTrue(builder.isSpecial(new Element(Tag.valueOf("div"), "")));
        Assert.assertTrue(builder.isSpecial(new Element(Tag.valueOf("p"), "")));
        Assert.assertTrue(builder.isSpecial(new Element(Tag.valueOf("table"), "")));
        Assert.assertFalse(builder.isSpecial(new Element(Tag.valueOf("span"), "")));
        Assert.assertFalse(builder.isSpecial(new Element(Tag.valueOf("b"), "")));
    }

    @Test
    public void testParseFragments() {
        Element contextDiv = new Element(Tag.valueOf("div"), "");
        List<Node> nodes = builder.parseFragment("<span>Hello</span>", contextDiv, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertEquals(1, nodes.size());
        Assert.assertEquals("span", nodes.get(0).nodeName());

        Element titleCtx = new Element(Tag.valueOf("title"), "");
        List<Node> titleNodes = builder.parseFragment("Sample & Title", titleCtx, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertFalse(titleNodes.isEmpty());

        Element scriptCtx = new Element(Tag.valueOf("script"), "");
        List<Node> scriptNodes = builder.parseFragment("console.log('hi');", scriptCtx, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertFalse(scriptNodes.isEmpty());

        Element styleCtx = new Element(Tag.valueOf("style"), "");
        List<Node> styleNodes = builder.parseFragment("body { color: red; }", styleCtx, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertFalse(styleNodes.isEmpty());

        List<Node> nullCtxNodes = builder.parseFragment("<p>Plain</p>", null, "http://example.com/", ParseErrorList.noTracking(), ParseSettings.htmlDefault);
        Assert.assertFalse(nullCtxNodes.isEmpty());
    }

    @Test
    public void testToStringAndErrorTracking() {
        ParseErrorList errors = ParseErrorList.tracking(5);
        builder.initialiseParse(new StringReader(""), "http://example.com/", errors, ParseSettings.htmlDefault);
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);
        builder.currentToken = new Token.StartTag().nameAttr("p", new Attributes());
        builder.error(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(1, errors.size());

        String str = builder.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("TreeBuilder"));
    }

    @Test
    public void testFullDocumentParse() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head><title>Test</title></head><body><form action='/submit'><input name='q'/></form><table><tr><td>Cell</td></tr></table></body></html>");
        Assert.assertEquals("Test", doc.title());
        Assert.assertNotNull(doc.selectFirst("form"));
        Assert.assertNotNull(doc.selectFirst("input"));
        Assert.assertEquals("Cell", doc.selectFirst("td").text());
    }
}
