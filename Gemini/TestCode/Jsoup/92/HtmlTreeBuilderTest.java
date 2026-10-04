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
import java.util.List;

public class HtmlTreeBuilderTest {
    private HtmlTreeBuilder builder;
    private Parser parser;

    @Before
    public void setUp() {
        builder = new HtmlTreeBuilder();
        parser = new Parser(builder);
        builder.initialiseParse(new StringReader(""), "http://example.com/", parser);
    }

    @Test
    public void testInitialiseAndDefaults() {
        Assert.assertNotNull(builder.defaultSettings());
        Assert.assertEquals(HtmlTreeBuilderState.Initial, builder.state());
        Assert.assertNull(builder.originalState());
        Assert.assertTrue(builder.framesetOk());
        Assert.assertFalse(builder.isFosterInserts());
        Assert.assertFalse(builder.isFragmentParsing());
        Assert.assertNotNull(builder.getDocument());
        Assert.assertEquals("http://example.com/", builder.getBaseUri());
    }

    @Test
    public void testStateTransitionsAndMark() {
        builder.transition(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(HtmlTreeBuilderState.InBody, builder.state());
        builder.markInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InBody, builder.originalState());
        builder.transition(HtmlTreeBuilderState.InTable);
        Assert.assertEquals(HtmlTreeBuilderState.InTable, builder.state());
        Assert.assertEquals(HtmlTreeBuilderState.InBody, builder.originalState());

        builder.framesetOk(false);
        Assert.assertFalse(builder.framesetOk());
    }

    @Test
    public void testStackOperations() {
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("p"), "");
        Element el3 = new Element(Tag.valueOf("span"), "");

        builder.push(el1);
        builder.push(el2);
        Assert.assertEquals(2, builder.getStack().size());
        Assert.assertTrue(builder.onStack(el1));
        Assert.assertTrue(builder.onStack(el2));
        Assert.assertFalse(builder.onStack(el3));

        Assert.assertEquals(el1, builder.aboveOnStack(el2));
        Assert.assertEquals(el2, builder.currentElement());

        builder.insertOnStackAfter(el1, el3);
        Assert.assertEquals(3, builder.getStack().size());
        Assert.assertEquals(el3, builder.getStack().get(1));

        Element el4 = new Element(Tag.valueOf("b"), "");
        builder.replaceOnStack(el3, el4);
        Assert.assertEquals(el4, builder.getStack().get(1));
        Assert.assertFalse(builder.onStack(el3));

        Assert.assertEquals(el4, builder.getFromStack("b"));
        Assert.assertNull(builder.getFromStack("nonexistent"));

        Assert.assertTrue(builder.removeFromStack(el4));
        Assert.assertFalse(builder.removeFromStack(el3));

        Element popped = builder.pop();
        Assert.assertEquals(el2, popped);
        Assert.assertEquals(1, builder.getStack().size());
    }

    @Test
    public void testPopStackToClose() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div);
        builder.push(span);

        builder.popStackToClose("div");
        Assert.assertEquals(2, builder.getStack().size());
        Assert.assertEquals(body, builder.currentElement());

        builder.push(div);
        builder.push(span);
        builder.popStackToClose(new String[]{"div", "span"});
        Assert.assertEquals(3, builder.getStack().size());
        Assert.assertEquals(div, builder.currentElement());
    }

    @Test
    public void testPopStackToBefore() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element div = new Element(Tag.valueOf("div"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(body);
        builder.push(div);
        builder.push(p);

        builder.popStackToBefore("div");
        Assert.assertEquals(3, builder.getStack().size());
        Assert.assertEquals(div, builder.currentElement());
    }

    @Test
    public void testClearStackToContexts() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");

        builder.push(html);
        builder.push(table);
        builder.push(tbody);
        builder.push(tr);
        builder.push(td);

        builder.clearStackToTableRowContext();
        Assert.assertEquals(4, builder.getStack().size());
        Assert.assertEquals(tr, builder.currentElement());

        builder.clearStackToTableBodyContext();
        Assert.assertEquals(3, builder.getStack().size());
        Assert.assertEquals(tbody, builder.currentElement());

        builder.clearStackToTableContext();
        Assert.assertEquals(2, builder.getStack().size());
        Assert.assertEquals(table, builder.currentElement());
    }

    @Test
    public void testInsertStartTagAndSelfClosing() {
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);

        Element div = builder.insertStartTag("div");
        Assert.assertEquals("div", div.normalName());
        Assert.assertTrue(builder.onStack(div));

        Token.StartTag selfClosing = new Token.StartTag();
        selfClosing.name("meta");
        selfClosing.selfClosing = true;
        Element meta = builder.insert(selfClosing);
        Assert.assertEquals("meta", meta.normalName());

        Token.StartTag customSelfClosing = new Token.StartTag();
        customSelfClosing.name("custom-tag");
        customSelfClosing.selfClosing = true;
        Element custom = builder.insert(customSelfClosing);
        Assert.assertEquals("custom-tag", custom.normalName());
    }

    @Test
    public void testInsertEmptyAndForm() {
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);

        Token.StartTag imgTag = new Token.StartTag();
        imgTag.name("img");
        imgTag.selfClosing = true;
        Element img = builder.insertEmpty(imgTag);
        Assert.assertEquals("img", img.normalName());

        Token.StartTag formTag = new Token.StartTag();
        formTag.name("form");
        FormElement form = builder.insertForm(formTag, true);
        Assert.assertEquals("form", form.normalName());
        Assert.assertEquals(form, builder.getFormElement());
        Assert.assertTrue(builder.onStack(form));

        Token.StartTag formTagNoStack = new Token.StartTag();
        formTagNoStack.name("form");
        FormElement form2 = builder.insertForm(formTagNoStack, false);
        Assert.assertEquals(form2, builder.getFormElement());
        Assert.assertFalse(builder.onStack(form2));
    }

    @Test
    public void testInsertCommentAndCharacter() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element script = new Element(Tag.valueOf("script"), "");
        builder.push(html);

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("test comment");
        builder.insert(commentToken);
        Assert.assertEquals(1, html.childNodeSize());

        Token.Character charToken = new Token.Character();
        charToken.data("hello");
        builder.insert(charToken);
        Assert.assertEquals(2, html.childNodeSize());

        builder.push(script);
        Token.Character scriptChar = new Token.Character();
        scriptChar.data("var x = 1;");
        builder.insert(scriptChar);
        Assert.assertEquals(1, script.childNodeSize());

        Token.Character cdata = new Token.CData("data");
        builder.insert(cdata);
        Assert.assertEquals(2, script.childNodeSize());
    }

    @Test
    public void testActiveFormattingElements() {
        Element a1 = new Element(Tag.valueOf("a"), "");
        Element a2 = new Element(Tag.valueOf("a"), "");
        Element a3 = new Element(Tag.valueOf("a"), "");
        Element a4 = new Element(Tag.valueOf("a"), "");

        builder.pushActiveFormattingElements(a1);
        builder.pushActiveFormattingElements(a2);
        builder.pushActiveFormattingElements(a3);
        builder.pushActiveFormattingElements(a4);

        Assert.assertEquals(3, builder.lastFormattingElement() != null ? 3 : 0);
        Assert.assertTrue(builder.isInActiveFormattingElements(a4));
        Assert.assertNotNull(builder.getActiveFormattingElement("a"));

        Element b = new Element(Tag.valueOf("b"), "");
        builder.replaceActiveFormattingElement(a4, b);
        Assert.assertTrue(builder.isInActiveFormattingElements(b));
        Assert.assertFalse(builder.isInActiveFormattingElements(a4));

        builder.insertMarkerToFormattingElements();
        Assert.assertNull(builder.getActiveFormattingElement("b"));

        builder.clearFormattingElementsToLastMarker();
        Assert.assertEquals(b, builder.lastFormattingElement());

        builder.removeFromActiveFormattingElements(b);
        Assert.assertNull(builder.getActiveFormattingElement("b"));
        Assert.assertNotNull(builder.removeLastFormattingElement());
    }

    @Test
    public void testReconstructFormattingElements() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        builder.push(html);
        builder.push(body);

        Element b = new Element(Tag.valueOf("b"), "");
        b.attr("class", "bold");
        builder.pushActiveFormattingElements(b);

        builder.reconstructFormattingElements();
        Assert.assertEquals("b", builder.currentElement().normalName());
        Assert.assertEquals("bold", builder.currentElement().attr("class"));
    }

    @Test
    public void testScopeChecks() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element button = new Element(Tag.valueOf("button"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(table);
        builder.push(tr);
        builder.push(td);

        Assert.assertTrue(builder.inTableScope("table"));
        Assert.assertFalse(builder.inTableScope("tr"));

        builder.push(button);
        Assert.assertTrue(builder.inButtonScope("button"));

        builder.push(p);
        Assert.assertTrue(builder.inScope("p"));
        Assert.assertFalse(builder.inScope("h1"));
        Assert.assertTrue(builder.inScope(new String[]{"p", "div"}));

        Element ol = new Element(Tag.valueOf("ol"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        builder.push(ol);
        builder.push(li);
        Assert.assertTrue(builder.inListItemScope("li"));
    }

    @Test
    public void testInSelectScope() {
        Element select = new Element(Tag.valueOf("select"), "");
        Element optgroup = new Element(Tag.valueOf("optgroup"), "");
        Element option = new Element(Tag.valueOf("option"), "");

        builder.push(select);
        builder.push(optgroup);
        builder.push(option);

        Assert.assertTrue(builder.inSelectScope("option"));
        Assert.assertTrue(builder.inSelectScope("optgroup"));
        Assert.assertFalse(builder.inSelectScope("div"));
    }

    @Test
    public void testFosterParenting() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");

        html.appendChild(body);
        body.appendChild(table);

        builder.push(html);
        builder.push(body);
        builder.push(table);
        builder.push(tr);

        builder.setFosterInserts(true);
        Assert.assertTrue(builder.isFosterInserts());

        TextNode text = new TextNode("fostered");
        builder.insertInFosterParent(text);
        Assert.assertEquals(table, text.nextSibling());
    }

    @Test
    public void testFosterParentingNoTableParent() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");

        builder.push(html);
        builder.push(table);

        TextNode text = new TextNode("fostered");
        builder.insertInFosterParent(text);
        Assert.assertEquals(1, html.childNodeSize());
    }

    @Test
    public void testFosterParentingNoTableOnStack() {
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);

        TextNode text = new TextNode("fostered");
        builder.insertInFosterParent(text);
        Assert.assertEquals(1, html.childNodeSize());
    }

    @Test
    public void testMaybeSetBaseUri() {
        Element base1 = new Element(Tag.valueOf("base"), "");
        base1.attr("href", "http://example.com/path/");
        builder.maybeSetBaseUri(base1);
        Assert.assertEquals("http://example.com/path/", builder.getBaseUri());

        Element base2 = new Element(Tag.valueOf("base"), "");
        base2.attr("href", "http://other.com/");
        builder.maybeSetBaseUri(base2);
        Assert.assertEquals("http://example.com/path/", builder.getBaseUri());
    }

    @Test
    public void testPendingTableCharacters() {
        builder.newPendingTableCharacters();
        Assert.assertNotNull(builder.getPendingTableCharacters());
        builder.getPendingTableCharacters().add("chars");
        Assert.assertEquals(1, builder.getPendingTableCharacters().size());
    }

    @Test
    public void testHeadAndFormElement() {
        Element head = new Element(Tag.valueOf("head"), "");
        builder.setHeadElement(head);
        Assert.assertEquals(head, builder.getHeadElement());

        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        builder.setFormElement(form);
        Assert.assertEquals(form, builder.getFormElement());
    }

    @Test
    public void testIsSpecial() {
        Assert.assertTrue(builder.isSpecial(new Element(Tag.valueOf("p"), "")));
        Assert.assertTrue(builder.isSpecial(new Element(Tag.valueOf("table"), "")));
        Assert.assertTrue(builder.isSpecial(new Element(Tag.valueOf("body"), "")));
        Assert.assertFalse(builder.isSpecial(new Element(Tag.valueOf("span"), "")));
        Assert.assertFalse(builder.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));
    }

    @Test
    public void testGenerateImpliedEndTags() {
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        builder.push(html);
        builder.push(body);
        builder.push(p);

        builder.generateImpliedEndTags();
        Assert.assertEquals(body, builder.currentElement());

        builder.push(p);
        builder.generateImpliedEndTags("p");
        Assert.assertEquals(p, builder.currentElement());
    }

    @Test
    public void testResetInsertionModeAllBranches() {
        String[] tags = new String[]{
            "select", "td", "th", "tr", "tbody", "thead", "tfoot",
            "caption", "colgroup", "table", "head", "body", "frameset", "html"
        };
        HtmlTreeBuilderState[] states = new HtmlTreeBuilderState[]{
            HtmlTreeBuilderState.InSelect, HtmlTreeBuilderState.InCell, HtmlTreeBuilderState.InCell,
            HtmlTreeBuilderState.InRow, HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InTableBody,
            HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InCaption, HtmlTreeBuilderState.InColumnGroup,
            HtmlTreeBuilderState.InTable, HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.InBody,
            HtmlTreeBuilderState.InFrameset, HtmlTreeBuilderState.BeforeHead
        };

        for (int i = 0; i < tags.length; i++) {
            builder.getStack().clear();
            builder.push(new Element(Tag.valueOf("html"), ""));
            builder.push(new Element(Tag.valueOf(tags[i]), ""));
            builder.resetInsertionMode();
            Assert.assertEquals("Testing tag: " + tags[i], states[i], builder.state());
        }
    }

    @Test
    public void testParseFragmentContexts() {
        String[] contexts = new String[]{"title", "textarea", "iframe", "script", "noscript", "plaintext", "div"};
        for (String ctxTag : contexts) {
            Element context = new Element(Tag.valueOf(ctxTag), "");
            List<Node> nodes = builder.parseFragment("content <b>text</b>", context, "http://example.com/", parser);
            Assert.assertNotNull(nodes);
        }

        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        Element divInForm = new Element(Tag.valueOf("div"), "");
        form.appendChild(divInForm);
        List<Node> formNodes = builder.parseFragment("<input type='text'>", divInForm, "http://example.com/", parser);
        Assert.assertNotNull(formNodes);

        List<Node> nullContextNodes = builder.parseFragment("<div>test</div>", null, "http://example.com/", parser);
        Assert.assertNotNull(nullContextNodes);
    }

    @Test
    public void testErrorTracking() {
        parser.setTrackErrors(10);
        builder.error(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(1, parser.getErrors().size());
    }

    @Test
    public void testToStringOutput() {
        Element html = new Element(Tag.valueOf("html"), "");
        builder.push(html);
        String str = builder.toString();
        Assert.assertTrue(str.contains("TreeBuilder{"));
        Assert.assertTrue(str.contains("state="));
    }

    @Test
    public void testFullIntegrationParse() {
        Document doc = Jsoup.parse("<!DOCTYPE html><html><head><base href='http://foo.com/'><title>Test</title></head><body><table><tr><td>Cell</td></tr></table><form><input></form></body></html>");
        Assert.assertNotNull(doc);
        Assert.assertEquals("Test", doc.title());
        Assert.assertEquals("http://foo.com/", doc.baseUri());
        Assert.assertEquals(1, doc.select("table").size());
        Assert.assertEquals(1, doc.select("form").size());
    }
}
