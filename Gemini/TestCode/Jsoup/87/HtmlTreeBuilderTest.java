package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder createBuilder() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Parser parser = new Parser(tb);
        tb.initialiseParse(new StringReader(""), "http://example.com/", parser);
        return tb;
    }

    @Test
    public void testDefaultSettings() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseSettings settings = tb.defaultSettings();
        Assert.assertNotNull(settings);
        Assert.assertFalse(settings.preserveTagCase());
        Assert.assertFalse(settings.preserveAttributeCase());
    }

    @Test
    public void testInitialiseParseAndGetters() {
        HtmlTreeBuilder tb = createBuilder();
        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        Assert.assertNull(tb.originalState());
        Assert.assertNull(tb.getHeadElement());
        Assert.assertNull(tb.getFormElement());
        Assert.assertNotNull(tb.getDocument());
        Assert.assertEquals("http://example.com/", tb.getBaseUri());
        Assert.assertTrue(tb.framesetOk());
        Assert.assertFalse(tb.isFosterInserts());
        Assert.assertFalse(tb.isFragmentParsing());
        Assert.assertNotNull(tb.getPendingTableCharacters());
        Assert.assertTrue(tb.getPendingTableCharacters().isEmpty());
    }

    @Test
    public void testStateTransitionsAndMark() {
        HtmlTreeBuilder tb = createBuilder();
        tb.transition(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.state());

        tb.markInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.transition(HtmlTreeBuilderState.InTable);
        Assert.assertEquals(HtmlTreeBuilderState.InTable, tb.state());
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());

        tb.framesetOk(false);
        Assert.assertFalse(tb.framesetOk());

        tb.framesetOk(true);
        Assert.assertTrue(tb.framesetOk());
    }

    @Test
    public void testProcessTokens() {
        HtmlTreeBuilder tb = createBuilder();
        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("test comment");
        boolean handled = tb.process(commentToken);
        Assert.assertTrue(handled);
        Assert.assertTrue(tb.getDocument().childNodeSize() > 0);

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        handled = tb.process(doctype, HtmlTreeBuilderState.Initial);
        Assert.assertTrue(handled);
    }

    @Test
    public void testMaybeSetBaseUri() {
        HtmlTreeBuilder tb = createBuilder();
        Element baseWithoutHref = new Element(Tag.valueOf("base", tb.settings), "http://example.com/");
        tb.maybeSetBaseUri(baseWithoutHref);
        Assert.assertEquals("http://example.com/", tb.getBaseUri());

        Element baseWithHref = new Element(Tag.valueOf("base", tb.settings), "http://example.com/");
        baseWithHref.attr("href", "http://jsoup.org/");
        tb.maybeSetBaseUri(baseWithHref);
        Assert.assertEquals("http://jsoup.org/", tb.getBaseUri());
        Assert.assertEquals("http://jsoup.org/", tb.getDocument().baseUri());

        Element baseSecond = new Element(Tag.valueOf("base", tb.settings), "http://example.com/");
        baseSecond.attr("href", "http://other.org/");
        tb.maybeSetBaseUri(baseSecond);
        Assert.assertEquals("http://jsoup.org/", tb.getBaseUri());
    }

    @Test
    public void testStackOperations() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        tb.push(html);
        tb.push(body);
        tb.push(div);
        tb.push(span);

        Assert.assertEquals(4, tb.getStack().size());
        Assert.assertTrue(tb.onStack(div));
        Assert.assertEquals(div, tb.aboveOnStack(span));
        Assert.assertEquals(body, tb.aboveOnStack(div));
        Assert.assertEquals(html, tb.aboveOnStack(body));
        Assert.assertNull(tb.aboveOnStack(html));

        Assert.assertEquals(div, tb.getFromStack("div"));
        Assert.assertNull(tb.getFromStack("p"));

        Element popped = tb.pop();
        Assert.assertEquals(span, popped);
        Assert.assertFalse(tb.onStack(span));

        Element p = new Element(Tag.valueOf("p"), "");
        tb.insertOnStackAfter(body, p);
        Assert.assertEquals(p, tb.getStack().get(2));

        Element section = new Element(Tag.valueOf("section"), "");
        tb.replaceOnStack(p, section);
        Assert.assertEquals(section, tb.getStack().get(2));
        Assert.assertFalse(tb.onStack(p));

        boolean removed = tb.removeFromStack(section);
        Assert.assertTrue(removed);
        Assert.assertFalse(tb.onStack(section));
        Assert.assertFalse(tb.removeFromStack(section));
    }

    @Test
    public void testPopStackToCloseAndBefore() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        tb.push(html);
        tb.push(body);
        tb.push(div);
        tb.push(p);
        tb.push(span);

        tb.popStackToBefore("div");
        Assert.assertEquals(3, tb.getStack().size());
        Assert.assertEquals("div", tb.currentElement().nodeName());

        tb.push(p);
        tb.push(span);
        tb.popStackToClose("p");
        Assert.assertEquals(3, tb.getStack().size());
        Assert.assertEquals("div", tb.currentElement().nodeName());

        tb.push(p);
        tb.push(span);
        tb.popStackToClose("p", "div");
        Assert.assertEquals(4, tb.getStack().size());
        Assert.assertEquals("p", tb.currentElement().nodeName());
    }

    @Test
    public void testClearStackToContexts() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element div = new Element(Tag.valueOf("div"), "");

        tb.push(html);
        tb.push(table);
        tb.push(tbody);
        tb.push(tr);
        tb.push(td);
        tb.push(div);

        tb.clearStackToTableRowContext();
        Assert.assertEquals(tr, tb.currentElement());

        tb.push(td);
        tb.push(div);
        tb.clearStackToTableBodyContext();
        Assert.assertEquals(tbody, tb.currentElement());

        tb.push(tr);
        tb.push(td);
        tb.clearStackToTableContext();
        Assert.assertEquals(table, tb.currentElement());

        tb.push(div);
        tb.clearStackToTableContext();
        Assert.assertEquals(table, tb.currentElement());
    }

    @Test
    public void testInsertElementsAndCharacters() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = tb.insertStartTag("html");
        Assert.assertNotNull(html);
        Assert.assertEquals(1, tb.getStack().size());

        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("div", new Attributes());
        Element div = tb.insert(startTag);
        Assert.assertEquals("div", div.tagName());
        Assert.assertEquals(2, tb.getStack().size());

        Token.Character charToken = new Token.Character();
        charToken.data("Hello");
        tb.insert(charToken);
        Assert.assertEquals(1, div.childNodeSize());
        Assert.assertTrue(div.childNode(0) instanceof TextNode);

        Token.Character cdataToken = new Token.Character();
        cdataToken.data("cdata content");
        cdataToken.asCharacter();
        Token.StartTag scriptTag = new Token.StartTag();
        scriptTag.nameAttr("script", new Attributes());
        tb.insert(scriptTag);

        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 1;");
        tb.insert(scriptChar);
        Assert.assertEquals(1, tb.currentElement().childNodeSize());

        tb.pop();
        Token.Comment comment = new Token.Comment();
        comment.getData().append("inline comment");
        tb.insert(comment);
        Assert.assertEquals(2, div.childNodeSize());
    }

    @Test
    public void testInsertSelfClosingAndEmpty() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = tb.insertStartTag("html");
        tb.push(html);

        Token.StartTag imgTag = new Token.StartTag();
        imgTag.nameAttr("img", new Attributes());
        imgTag.selfClosing = true;
        Element img = tb.insertEmpty(imgTag);
        Assert.assertEquals("img", img.tagName());

        Token.StartTag customTag = new Token.StartTag();
        customTag.nameAttr("custom-element", new Attributes());
        customTag.selfClosing = true;
        Element custom = tb.insertEmpty(customTag);
        Assert.assertTrue(custom.tag().isSelfClosing());

        Token.StartTag selfDiv = new Token.StartTag();
        selfDiv.nameAttr("div", new Attributes());
        selfDiv.selfClosing = true;
        Element resDiv = tb.insert(selfDiv);
        Assert.assertNotNull(resDiv);
    }

    @Test
    public void testInsertForm() {
        HtmlTreeBuilder tb = createBuilder();
        tb.insertStartTag("html");

        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formTag, true);
        Assert.assertNotNull(form);
        Assert.assertEquals(form, tb.getFormElement());
        Assert.assertTrue(tb.onStack(form));

        Token.StartTag formTag2 = new Token.StartTag();
        formTag2.nameAttr("form", new Attributes());
        FormElement form2 = tb.insertForm(formTag2, false);
        Assert.assertNotNull(form2);
        Assert.assertEquals(form2, tb.getFormElement());
        Assert.assertFalse(tb.onStack(form2));

        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        Element input = tb.insert(inputTag);
        Assert.assertTrue(form2.elements().contains(input));
    }

    @Test
    public void testHeadElementAndFosterInserts() {
        HtmlTreeBuilder tb = createBuilder();
        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        Assert.assertEquals(head, tb.getHeadElement());

        tb.setFosterInserts(true);
        Assert.assertTrue(tb.isFosterInserts());
        tb.setFosterInserts(false);
        Assert.assertFalse(tb.isFosterInserts());

        tb.newPendingTableCharacters();
        tb.getPendingTableCharacters().add("chars");
        Assert.assertEquals(1, tb.getPendingTableCharacters().size());
    }

    @Test
    public void testFosterParenting() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        tb.push(html);
        tb.push(body);
        tb.push(table);
        body.appendChild(table);

        tb.setFosterInserts(true);
        TextNode textNode = new TextNode("fostered");
        tb.insertInFosterParent(textNode);
        Assert.assertEquals(textNode, table.previousSibling());

        tb.pop();
        Element table2 = new Element(Tag.valueOf("table"), "");
        tb.push(table2);
        TextNode textNode2 = new TextNode("fostered2");
        tb.insertInFosterParent(textNode2);
        Assert.assertEquals(body, textNode2.parent());

        HtmlTreeBuilder tbFrag = createBuilder();
        Element fragRoot = new Element(Tag.valueOf("html"), "");
        tbFrag.push(fragRoot);
        TextNode textNode3 = new TextNode("fostered3");
        tbFrag.insertInFosterParent(textNode3);
        Assert.assertEquals(fragRoot, textNode3.parent());
    }

    @Test
    public void testResetInsertionMode() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);

        String[] tags = new String[]{"select", "td", "th", "tr", "tbody", "thead", "tfoot", "caption", "colgroup", "table", "head", "body", "frameset", "html"};
        HtmlTreeBuilderState[] expected = new HtmlTreeBuilderState[]{
                HtmlTreeBuilderState.InSelect, HtmlTreeBuilderState.InCell, HtmlTreeBuilderState.InCell,
                HtmlTreeBuilderState.InRow, HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InTableBody,
                HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InCaption, HtmlTreeBuilderState.InColumnGroup,
                HtmlTreeBuilderState.InTable, HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.InBody,
                HtmlTreeBuilderState.InFrameset, HtmlTreeBuilderState.BeforeHead
        };

        for (int i = 0; i < tags.length; i++) {
            Element el = new Element(Tag.valueOf(tags[i]), "");
            tb.push(el);
            tb.resetInsertionMode();
            Assert.assertEquals("Testing tag: " + tags[i], expected[i], tb.state());
            tb.pop();
        }
    }

    @Test
    public void testInScope() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element div = new Element(Tag.valueOf("div"), "");

        tb.push(html);
        tb.push(body);
        tb.push(table);
        tb.push(tr);
        tb.push(td);
        tb.push(div);

        Assert.assertTrue(tb.inScope("div"));
        Assert.assertTrue(tb.inScope("td"));
        Assert.assertFalse(tb.inScope("body"));
        Assert.assertTrue(tb.inScope(new String[]{"div", "span"}));
        Assert.assertTrue(tb.inListItemScope("div"));
        Assert.assertTrue(tb.inButtonScope("div"));
        Assert.assertTrue(tb.inTableScope("table"));

        Element select = new Element(Tag.valueOf("select"), "");
        Element option = new Element(Tag.valueOf("option"), "");
        tb.push(select);
        tb.push(option);
        Assert.assertTrue(tb.inSelectScope("option"));
        Assert.assertFalse(tb.inSelectScope("div"));
    }

    @Test
    public void testScopesMaxDepth() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        tb.push(html);
        for (int i = 0; i < 110; i++) {
            Element div = new Element(Tag.valueOf("div"), "");
            tb.push(div);
        }
        Assert.assertFalse(tb.inScope("html"));
    }

    @Test
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element li = new Element(Tag.valueOf("li"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(li);

        tb.generateImpliedEndTags("p");
        Assert.assertEquals(p, tb.currentElement());

        tb.push(li);
        tb.generateImpliedEndTags();
        Assert.assertEquals(body, tb.currentElement());
    }

    @Test
    public void testIsSpecial() {
        HtmlTreeBuilder tb = createBuilder();
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("table"), "")));
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("div"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("span"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("b"), "")));
    }

    @Test
    public void testActiveFormattingElements() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Element a1 = new Element(Tag.valueOf("a"), "");
        a1.attr("href", "http://jsoup.org");
        Element a2 = new Element(Tag.valueOf("a"), "");
        a2.attr("href", "http://jsoup.org");
        Element a3 = new Element(Tag.valueOf("a"), "");
        a3.attr("href", "http://jsoup.org");
        Element a4 = new Element(Tag.valueOf("a"), "");
        a4.attr("href", "http://jsoup.org");

        tb.pushActiveFormattingElements(a1);
        tb.pushActiveFormattingElements(a2);
        tb.pushActiveFormattingElements(a3);
        Assert.assertEquals(3, tb.lastFormattingElement() != null ? 3 : 0);
        tb.pushActiveFormattingElements(a4);
        Assert.assertFalse(tb.isInActiveFormattingElements(a1));
        Assert.assertTrue(tb.isInActiveFormattingElements(a4));

        Assert.assertEquals(a4, tb.getActiveFormattingElement("a"));
        Assert.assertNull(tb.getActiveFormattingElement("b"));

        Element b = new Element(Tag.valueOf("b"), "");
        tb.replaceActiveFormattingElement(a4, b);
        Assert.assertTrue(tb.isInActiveFormattingElements(b));
        Assert.assertFalse(tb.isInActiveFormattingElements(a4));

        tb.insertMarkerToFormattingElements();
        Assert.assertNull(tb.getActiveFormattingElement("b"));

        Element i = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i);
        Assert.assertEquals(i, tb.getActiveFormattingElement("i"));

        tb.clearFormattingElementsToLastMarker();
        Assert.assertFalse(tb.isInActiveFormattingElements(i));
        Assert.assertTrue(tb.isInActiveFormattingElements(b));

        tb.removeFromActiveFormattingElements(b);
        Assert.assertFalse(tb.isInActiveFormattingElements(b));

        Assert.assertNull(tb.removeLastFormattingElement());
    }

    @Test
    public void testReconstructFormattingElements() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Element b = new Element(Tag.valueOf("b"), "");
        b.attr("class", "bold");
        tb.pushActiveFormattingElements(b);

        tb.reconstructFormattingElements();
        Assert.assertEquals("b", tb.currentElement().nodeName());
        Assert.assertEquals("bold", tb.currentElement().attr("class"));

        tb.reconstructFormattingElements();
        Assert.assertEquals("b", tb.currentElement().nodeName());
    }

    @Test
    public void testReconstructFormattingElementsWithMarker() {
        HtmlTreeBuilder tb = createBuilder();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        tb.push(html);
        tb.push(body);

        Element b = new Element(Tag.valueOf("b"), "");
        tb.push(b);
        tb.pushActiveFormattingElements(b);
        tb.insertMarkerToFormattingElements();

        Element i = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i);

        tb.reconstructFormattingElements();
        Assert.assertEquals("i", tb.currentElement().nodeName());
    }

    @Test
    public void testParseFragment() {
        Parser parser = Parser.htmlParser();
        HtmlTreeBuilder tb = (HtmlTreeBuilder) parser.getTreeBuilder();

        List<Node> nodes = tb.parseFragment("<div><p>Hello</p></div>", null, "http://example.com/", parser);
        Assert.assertTrue(nodes.size() > 0);

        Element bodyContext = new Element(Tag.valueOf("body"), "");
        Document ownerDoc = new Document("http://example.com/");
        ownerDoc.quirksMode(Document.QuirksMode.quirks);
        ownerDoc.appendChild(bodyContext);

        List<Node> nodes2 = tb.parseFragment("<span>text</span>", bodyContext, "http://example.com/", parser);
        Assert.assertEquals(1, nodes2.size());
        Assert.assertEquals("span", nodes2.get(0).nodeName());

        String[] specialContexts = new String[]{"title", "iframe", "script", "noscript", "plaintext"};
        for (String ctxTag : specialContexts) {
            Element ctx = new Element(Tag.valueOf(ctxTag), "");
            List<Node> ctxNodes = tb.parseFragment("content", ctx, "http://example.com/", parser);
            Assert.assertNotNull(ctxNodes);
        }

        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        Element divInForm = new Element(Tag.valueOf("div"), "");
        form.appendChild(divInForm);
        List<Node> formFragNodes = tb.parseFragment("<input name='foo'>", divInForm, "http://example.com/", parser);
        Assert.assertEquals(1, formFragNodes.size());
    }

    @Test
    public void testError() {
        Parser parser = Parser.htmlParser().setTrackErrors(10);
        HtmlTreeBuilder tb = (HtmlTreeBuilder) parser.getTreeBuilder();
        tb.initialiseParse(new StringReader("<div>"), "http://example.com/", parser);
        tb.process(new Token.EndTag().name("span"));
        tb.error(HtmlTreeBuilderState.InBody);
        Assert.assertTrue(parser.getErrors().size() > 0);
    }

    @Test
    public void testToString() {
        HtmlTreeBuilder tb = createBuilder();
        String str = tb.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("TreeBuilder{"));
    }
}
