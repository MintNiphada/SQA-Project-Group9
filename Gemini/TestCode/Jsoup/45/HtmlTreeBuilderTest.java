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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HtmlTreeBuilderTest {

    private HtmlTreeBuilder tb;

    @Before
    public void setUp() {
        tb = new HtmlTreeBuilder();
    }

    @Test
    public void testParseSimple() {
        Document doc = tb.parse("<p>Hello</p>", "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(doc);
        Assert.assertEquals("http://example.com", doc.baseUri());
        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.originalState() == null ? HtmlTreeBuilderState.Initial : tb.originalState());
    }

    @Test
    public void testParseFragmentNullContext() {
        List<Node> nodes = tb.parseFragment("<div>test</div>", null, "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(nodes);
        Assert.assertTrue(nodes.size() > 0);
        Assert.assertTrue(tb.isFragmentParsing());
    }

    @Test
    public void testParseFragmentWithVariousContexts() {
        String[] tags = new String[]{"title", "textarea", "iframe", "noembed", "noframes", "style", "xmp", "script", "noscript", "plaintext", "div", "custom"};
        for (String tag : tags) {
            HtmlTreeBuilder b = new HtmlTreeBuilder();
            Element ctx = new Element(Tag.valueOf(tag), "http://example.com");
            Document owner = new Document("http://example.com");
            owner.quirksMode(Document.QuirksMode.quirks);
            ctx.setParentNode(owner);
            List<Node> nodes = b.parseFragment("content", ctx, "http://example.com", ParseErrorList.noTracking());
            Assert.assertNotNull(nodes);
            Assert.assertEquals(Document.QuirksMode.quirks, b.getDocument().quirksMode());
        }
    }

    @Test
    public void testParseFragmentWithFormContext() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        form.appendChild(div);
        List<Node> nodes = tb.parseFragment("<input name='foo'>", div, "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(nodes);
        Assert.assertEquals(form, tb.getFormElement());
    }

    @Test
    public void testStateAndTransition() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.transition(HtmlTreeBuilderState.InBody);
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.state());
        tb.markInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.originalState());
    }

    @Test
    public void testFramesetOk() {
        tb.framesetOk(true);
        Assert.assertTrue(tb.framesetOk());
        tb.framesetOk(false);
        Assert.assertFalse(tb.framesetOk());
    }

    @Test
    public void testGetDocumentAndBaseUri() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(tb.getDocument());
        Assert.assertEquals("http://example.com", tb.getBaseUri());
    }

    @Test
    public void testMaybeSetBaseUri() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element base = new Element(Tag.valueOf("base"), "http://example.com");
        base.attr("href", "http://jsoup.org/path");
        tb.maybeSetBaseUri(base);
        Assert.assertEquals("http://jsoup.org/path", tb.getBaseUri());
        Assert.assertEquals("http://jsoup.org/path", tb.getDocument().baseUri());

        Element base2 = new Element(Tag.valueOf("base"), "http://example.com");
        base2.attr("href", "http://other.org");
        tb.maybeSetBaseUri(base2);
        Assert.assertEquals("http://jsoup.org/path", tb.getBaseUri());

        HtmlTreeBuilder b2 = new HtmlTreeBuilder();
        b2.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element baseEmpty = new Element(Tag.valueOf("base"), "http://example.com");
        b2.maybeSetBaseUri(baseEmpty);
        Assert.assertEquals("http://example.com", b2.getBaseUri());
    }

    @Test
    public void testError() {
        ParseErrorList errors = ParseErrorList.tracking(10);
        tb.initialiseParse("<p></p>", "http://example.com", errors);
        tb.process(new Token.Character().data("a"));
        tb.error(HtmlTreeBuilderState.Initial);
        Assert.assertFalse(errors.isEmpty());
    }

    @Test
    public void testProcessToken() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.transition(HtmlTreeBuilderState.InBody);
        Token.Character c = new Token.Character();
        c.data("foo");
        boolean res1 = tb.process(c);
        Assert.assertTrue(res1);
        boolean res2 = tb.process(c, HtmlTreeBuilderState.InBody);
        Assert.assertTrue(res2);
    }

    @Test
    public void testInsertStartTag() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("div", new Attributes());
        Element el = tb.insert(startTag);
        Assert.assertEquals("div", el.tagName());
        Assert.assertTrue(tb.onStack(el));

        Element p = tb.insertStartTag("p");
        Assert.assertEquals("p", p.tagName());
        Assert.assertTrue(tb.onStack(p));
    }

    @Test
    public void testInsertSelfClosingStartTag() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("img", new Attributes());
        startTag.selfClosing = true;
        Element el = tb.insert(startTag);
        Assert.assertEquals("img", el.tagName());
        Assert.assertTrue(tb.onStack(el));

        Token.StartTag customTag = new Token.StartTag();
        customTag.nameAttr("custom-element", new Attributes());
        customTag.selfClosing = true;
        Element el2 = tb.insert(customTag);
        Assert.assertEquals("custom-element", el2.tagName());
    }

    @Test
    public void testInsertEmpty() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("br", new Attributes());
        startTag.selfClosing = true;
        Element el = tb.insertEmpty(startTag);
        Assert.assertEquals("br", el.tagName());

        Token.StartTag unknown = new Token.StartTag();
        unknown.nameAttr("foo", new Attributes());
        unknown.selfClosing = true;
        Element unkEl = tb.insertEmpty(unknown);
        Assert.assertEquals("foo", unkEl.tagName());
    }

    @Test
    public void testInsertForm() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Token.StartTag startTag = new Token.StartTag();
        startTag.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(startTag, true);
        Assert.assertEquals("form", form.tagName());
        Assert.assertEquals(form, tb.getFormElement());
        Assert.assertTrue(tb.onStack(form));

        FormElement form2 = tb.insertForm(startTag, false);
        Assert.assertEquals(form2, tb.getFormElement());
    }

    @Test
    public void testInsertCommentAndCharacters() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Token.Comment comment = new Token.Comment();
        comment.getData().append("hello");
        tb.insert(comment);

        Element div = tb.insertStartTag("div");
        Token.Character c = new Token.Character();
        c.data("text");
        tb.insert(c);
        Assert.assertEquals(1, div.textNodes().size());

        Element script = tb.insertStartTag("script");
        Token.Character sc = new Token.Character();
        sc.data("var a = 1;");
        tb.insert(sc);
        Assert.assertEquals(1, script.dataNodes().size());

        Element style = tb.insertStartTag("style");
        Token.Character stc = new Token.Character();
        stc.data("body { color: red; }");
        tb.insert(stc);
        Assert.assertEquals(1, style.dataNodes().size());
    }

    @Test
    public void testInsertFormListedElementAssociatesWithForm() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Token.StartTag formTag = new Token.StartTag();
        formTag.nameAttr("form", new Attributes());
        FormElement form = tb.insertForm(formTag, true);

        Token.StartTag inputTag = new Token.StartTag();
        inputTag.nameAttr("input", new Attributes());
        tb.insert(inputTag);

        Assert.assertEquals(1, form.elements().size());
    }

    @Test
    public void testStackOperations() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
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
        Assert.assertFalse(tb.onStack(new Element(Tag.valueOf("p"), "")));
        Assert.assertEquals(span, tb.pop());
        Assert.assertEquals(div, tb.getFromStack("div"));
        Assert.assertNull(tb.getFromStack("nonexistent"));

        Assert.assertEquals(body, tb.aboveOnStack(div));
        Assert.assertTrue(tb.removeFromStack(div));
        Assert.assertFalse(tb.removeFromStack(div));

        Element p = new Element(Tag.valueOf("p"), "");
        tb.insertOnStackAfter(body, p);
        Assert.assertEquals(p, tb.getStack().get(2));

        Element em = new Element(Tag.valueOf("em"), "");
        tb.replaceOnStack(p, em);
        Assert.assertEquals(em, tb.getStack().get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertOnStackAfterNotFound() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        tb.insertOnStackAfter(el1, el2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceOnStackNotFound() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element el1 = new Element(Tag.valueOf("div"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        tb.replaceOnStack(el1, el2);
    }

    @Test
    public void testPopStackToClose() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(span);

        tb.popStackToClose("p");
        Assert.assertEquals(2, tb.getStack().size());
        Assert.assertEquals(body, tb.currentElement());

        tb.push(p);
        tb.push(span);
        tb.popStackToClose("a", "p");
        Assert.assertEquals(2, tb.getStack().size());
    }

    @Test
    public void testPopStackToBefore() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(span);

        tb.popStackToBefore("p");
        Assert.assertEquals(3, tb.getStack().size());
        Assert.assertEquals(p, tb.currentElement());
    }

    @Test
    public void testClearStackToContexts() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tbody = new Element(Tag.valueOf("tbody"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");

        tb.push(html);
        tb.push(table);
        tb.push(tbody);
        tb.push(tr);
        tb.push(td);

        tb.clearStackToTableRowContext();
        Assert.assertEquals(tr, tb.currentElement());

        tb.push(td);
        tb.clearStackToTableBodyContext();
        Assert.assertEquals(tbody, tb.currentElement());

        tb.push(tr);
        tb.push(td);
        tb.clearStackToTableContext();
        Assert.assertEquals(table, tb.currentElement());
    }

    @Test
    public void testResetInsertionModeAllBranches() {
        String[] tags = new String[]{"select", "td", "tr", "tbody", "tfoot", "thead", "caption", "colgroup", "table", "head", "body", "frameset", "html", "custom"};
        HtmlTreeBuilderState[] expected = new HtmlTreeBuilderState[]{
                HtmlTreeBuilderState.InSelect, HtmlTreeBuilderState.InCell, HtmlTreeBuilderState.InRow,
                HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InTableBody, HtmlTreeBuilderState.InTableBody,
                HtmlTreeBuilderState.InCaption, HtmlTreeBuilderState.InColumnGroup, HtmlTreeBuilderState.InTable,
                HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.InBody, HtmlTreeBuilderState.InFrameset,
                HtmlTreeBuilderState.BeforeHead, HtmlTreeBuilderState.InBody
        };

        for (int i = 0; i < tags.length; i++) {
            HtmlTreeBuilder b = new HtmlTreeBuilder();
            b.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
            b.getStack().clear();
            Element el = new Element(Tag.valueOf(tags[i]), "");
            b.push(el);
            b.resetInsertionMode();
            Assert.assertEquals(expected[i], b.state());
        }
    }

    @Test
    public void testResetInsertionModeTdNotLast() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.getStack().clear();
        tb.push(new Element(Tag.valueOf("html"), ""));
        tb.push(new Element(Tag.valueOf("td"), ""));
        tb.push(new Element(Tag.valueOf("p"), ""));
        tb.resetInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.InBody, tb.state());
    }

    @Test
    public void testInScopeVariations() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.getStack().clear();
        Element html = new Element(Tag.valueOf("html"), "");
        Element table = new Element(Tag.valueOf("table"), "");
        Element tr = new Element(Tag.valueOf("tr"), "");
        Element td = new Element(Tag.valueOf("td"), "");
        Element p = new Element(Tag.valueOf("p"), "");

        tb.push(html);
        tb.push(table);
        tb.push(tr);
        tb.push(td);
        tb.push(p);

        Assert.assertTrue(tb.inScope("p"));
        Assert.assertTrue(tb.inScope(new String[]{"p", "span"}));
        Assert.assertTrue(tb.inScope("td"));
        Assert.assertFalse(tb.inTableScope("p"));
        Assert.assertTrue(tb.inTableScope("table"));

        Element ul = new Element(Tag.valueOf("ul"), "");
        Element li = new Element(Tag.valueOf("li"), "");
        tb.push(ul);
        tb.push(li);
        Assert.assertTrue(tb.inListItemScope("li"));
        Assert.assertFalse(tb.inListItemScope("p"));

        Element button = new Element(Tag.valueOf("button"), "");
        Element bText = new Element(Tag.valueOf("b"), "");
        tb.push(button);
        tb.push(bText);
        Assert.assertTrue(tb.inButtonScope("b"));
        Assert.assertFalse(tb.inButtonScope("li"));
    }

    @Test
    public void testInSelectScope() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.getStack().clear();
        Element optgroup = new Element(Tag.valueOf("optgroup"), "");
        Element option = new Element(Tag.valueOf("option"), "");
        tb.push(optgroup);
        tb.push(option);

        Assert.assertTrue(tb.inSelectScope("option"));
        Assert.assertTrue(tb.inSelectScope("optgroup"));

        tb.push(new Element(Tag.valueOf("div"), ""));
        Assert.assertFalse(tb.inSelectScope("option"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInScopeFailNotReachable() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.getStack().clear();
        tb.inScope("div");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInSelectScopeFailNotReachable() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.getStack().clear();
        tb.inSelectScope("div");
    }

    @Test
    public void testHeadElementAndFosterInserts() {
        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        Assert.assertEquals(head, tb.getHeadElement());

        tb.setFosterInserts(true);
        Assert.assertTrue(tb.isFosterInserts());
        tb.setFosterInserts(false);
        Assert.assertFalse(tb.isFosterInserts());
    }

    @Test
    public void testPendingTableCharacters() {
        tb.newPendingTableCharacters();
        Assert.assertNotNull(tb.getPendingTableCharacters());
        Assert.assertTrue(tb.getPendingTableCharacters().isEmpty());

        List<String> list = new ArrayList<String>();
        list.add("test");
        tb.setPendingTableCharacters(list);
        Assert.assertEquals(1, tb.getPendingTableCharacters().size());
        Assert.assertEquals("test", tb.getPendingTableCharacters().get(0));
    }

    @Test
    public void testGenerateImpliedEndTags() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.getStack().clear();
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element dd = new Element(Tag.valueOf("dd"), "");
        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(dd);

        tb.generateImpliedEndTags();
        Assert.assertEquals(body, tb.currentElement());

        tb.push(p);
        tb.push(dd);
        tb.generateImpliedEndTags("dd");
        Assert.assertEquals(dd, tb.currentElement());
    }

    @Test
    public void testIsSpecial() {
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("address"), "")));
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("div"), "")));
        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("span"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("custom-tag"), "")));
    }

    @Test
    public void testActiveFormattingElementsBasic() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Assert.assertNull(tb.lastFormattingElement());
        Assert.assertNull(tb.removeLastFormattingElement());

        Element b1 = new Element(Tag.valueOf("b"), "");
        tb.pushActiveFormattingElements(b1);
        Assert.assertEquals(b1, tb.lastFormattingElement());
        Assert.assertTrue(tb.isInActiveFormattingElements(b1));
        Assert.assertEquals(b1, tb.getActiveFormattingElement("b"));
        Assert.assertNull(tb.getActiveFormattingElement("i"));

        Element b2 = new Element(Tag.valueOf("b"), "");
        tb.replaceActiveFormattingElement(b1, b2);
        Assert.assertTrue(tb.isInActiveFormattingElements(b2));
        Assert.assertFalse(tb.isInActiveFormattingElements(b1));

        tb.insertMarkerToFormattingElements();
        Assert.assertNull(tb.getActiveFormattingElement("b"));

        Element i1 = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i1);
        tb.clearFormattingElementsToLastMarker();
        Assert.assertTrue(tb.isInActiveFormattingElements(b2));
        Assert.assertFalse(tb.isInActiveFormattingElements(i1));

        tb.removeFromActiveFormattingElements(b2);
        Assert.assertFalse(tb.isInActiveFormattingElements(b2));
    }

    @Test
    public void testPushActiveFormattingElementsMaxThree() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        tb.pushActiveFormattingElements(b1);
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        tb.pushActiveFormattingElements(b4);

        Assert.assertFalse(tb.isInActiveFormattingElements(b1));
        Assert.assertTrue(tb.isInActiveFormattingElements(b2));
        Assert.assertTrue(tb.isInActiveFormattingElements(b3));
        Assert.assertTrue(tb.isInActiveFormattingElements(b4));
    }

    @Test
    public void testReconstructFormattingElements() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        tb.reconstructFormattingElements();

        Element b = new Element(Tag.valueOf("b"), "");
        tb.pushActiveFormattingElements(b);
        tb.push(b);
        tb.reconstructFormattingElements();

        tb.pop();
        tb.reconstructFormattingElements();
        Assert.assertTrue(tb.onStack(tb.lastFormattingElement()));
    }

    @Test
    public void testReconstructFormattingElementsMultiple() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element body = tb.insertStartTag("body");
        Element b = new Element(Tag.valueOf("b"), "");
        b.attr("class", "foo");
        Element i = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(b);
        tb.pushActiveFormattingElements(i);

        tb.reconstructFormattingElements();
        Assert.assertEquals("i", tb.currentElement().tagName());
        Assert.assertEquals("b", tb.aboveOnStack(tb.currentElement()).tagName());
    }

    @Test
    public void testReconstructFormattingElementsWithMarkerAndOnStack() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element body = tb.insertStartTag("body");
        Element b = new Element(Tag.valueOf("b"), "");
        tb.pushActiveFormattingElements(b);
        tb.push(b);
        tb.insertMarkerToFormattingElements();
        Element i = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i);

        tb.reconstructFormattingElements();
        Assert.assertEquals("i", tb.currentElement().tagName());
    }

    @Test
    public void testInsertInFosterParent() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        Element html = tb.insertStartTag("html");
        Element body = tb.insertStartTag("body");
        Element table = tb.insertStartTag("table");
        body.appendChild(table);

        TextNode text = new TextNode("fostered", "");
        tb.insertInFosterParent(text);
        Assert.assertEquals(body, text.parent());
        Assert.assertEquals(0, body.childNodes().indexOf(text));

        Element table2 = new Element(Tag.valueOf("table"), "");
        tb.push(table2);
        TextNode text2 = new TextNode("fostered2", "");
        tb.insertInFosterParent(text2);
        Assert.assertEquals(table, text2.parent());

        tb.getStack().clear();
        Element root = new Element(Tag.valueOf("html"), "");
        tb.push(root);
        TextNode text3 = new TextNode("fostered3", "");
        tb.insertInFosterParent(text3);
        Assert.assertEquals(root, text3.parent());
    }

    @Test
    public void testToString() {
        tb.initialiseParse("<p></p>", "http://example.com", ParseErrorList.noTracking());
        String str = tb.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("TreeBuilder{"));
    }
}
