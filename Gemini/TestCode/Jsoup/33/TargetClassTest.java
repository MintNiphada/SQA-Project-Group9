package org.jsoup.parser;

import org.jsoup.nodes.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HtmlTreeBuilderTest {

    @Test
    public void testParseDocument() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document doc = tb.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>", "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(doc);
        Assert.assertEquals("Test", doc.title());
        Assert.assertEquals("Hello", doc.select("p").text());
        Assert.assertEquals("http://example.com", tb.getBaseUri());
        Assert.assertFalse(tb.isFragmentParsing());
    }

    @Test
    public void testParseFragmentWithVariousContexts() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        List<Node> nodes = tb.parseFragment("<p>One</p><p>Two</p>", div, "http://example.com", ParseErrorList.noTracking());
        Assert.assertEquals(2, nodes.size());
        Assert.assertTrue(tb.isFragmentParsing());

        String[] tags = new String[]{"title", "textarea", "iframe", "noembed", "noframes", "style", "xmp", "script", "noscript", "plaintext", "other"};
        for (String tag : tags) {
            HtmlTreeBuilder tbTag = new HtmlTreeBuilder();
            Element ctx = new Element(Tag.valueOf(tag), "http://example.com");
            List<Node> subNodes = tbTag.parseFragment("content", ctx, "http://example.com", ParseErrorList.noTracking());
            Assert.assertNotNull(subNodes);
        }

        Document ownerDoc = new Document("http://example.com");
        ownerDoc.quirksMode(Document.QuirksMode.quirks);
        Element form = new FormElement(Tag.valueOf("form"), "http://example.com", new org.jsoup.nodes.Attributes());
        ownerDoc.appendChild(form);
        Element inputContext = new Element(Tag.valueOf("input"), "http://example.com");
        form.appendChild(inputContext);

        HtmlTreeBuilder tbForm = new HtmlTreeBuilder();
        List<Node> formFragNodes = tbForm.parseFragment("<input type='text' />", inputContext, "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(formFragNodes);
        Assert.assertNotNull(tbForm.getFormElement());
    }

    @Test
    public void testParseFragmentNullContext() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        List<Node> nodes = tb.parseFragment("<div>Hello</div>", null, "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(nodes);
        Assert.assertTrue(nodes.size() > 0);
    }

    @Test
    public void testStateAndTransitions() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<div></div>", "http://example.com", ParseErrorList.tracking(10));
        tb.transition(HtmlTreeBuilderState.Initial);
        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.state());
        tb.markInsertionMode();
        Assert.assertEquals(HtmlTreeBuilderState.Initial, tb.originalState());

        tb.framesetOk(false);
        Assert.assertFalse(tb.framesetOk());
        tb.framesetOk(true);
        Assert.assertTrue(tb.framesetOk());

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("test comment");
        boolean processed = tb.process(commentToken);
        Assert.assertTrue(processed);
        boolean processedSpecific = tb.process(commentToken, HtmlTreeBuilderState.Initial);
        Assert.assertTrue(processedSpecific);
    }

    @Test
    public void testBaseUriHandling() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", ParseErrorList.noTracking());
        
        Element baseWithHref = new Element(Tag.valueOf("base"), "http://example.com");
        baseWithHref.attr("href", "http://example.com/sub/");
        tb.maybeSetBaseUri(baseWithHref);
        Assert.assertEquals("http://example.com/sub/", tb.getBaseUri());

        Element baseTargetOnly = new Element(Tag.valueOf("base"), "http://example.com");
        baseTargetOnly.attr("target", "_blank");
        tb.maybeSetBaseUri(baseTargetOnly);
        Assert.assertEquals("http://example.com/sub/", tb.getBaseUri());

        Element secondBase = new Element(Tag.valueOf("base"), "http://example.com");
        secondBase.attr("href", "http://example.com/other/");
        tb.maybeSetBaseUri(secondBase);
        Assert.assertEquals("http://example.com/sub/", tb.getBaseUri());
    }

    @Test
    public void testErrorTracking() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        ParseErrorList errors = ParseErrorList.tracking(5);
        tb.initialiseParse("<div>", "http://example.com", errors);
        tb.process(new Token.EndTag("span"));
        tb.error(HtmlTreeBuilderState.InBody);
        Assert.assertTrue(errors.size() > 0);
    }

    @Test
    public void testInsertMethods() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", ParseErrorList.noTracking());

        Element elDiv = tb.insert("div");
        Assert.assertEquals("div", elDiv.nodeName());
        Assert.assertTrue(tb.onStack(elDiv));

        Token.StartTag imgTag = new Token.StartTag("img");
        imgTag.selfClosing = true;
        Element imgEl = tb.insert(imgTag);
        Assert.assertEquals("img", imgEl.nodeName());

        Token.StartTag customTag = new Token.StartTag("custom-tag");
        customTag.selfClosing = true;
        Element customEl = tb.insert(customTag);
        Assert.assertEquals("custom-tag", customEl.nodeName());

        Token.StartTag nonSelfClosing = new Token.StartTag("span");
        Element spanEl = tb.insert(nonSelfClosing);
        Assert.assertEquals("span", spanEl.nodeName());

        Token.StartTag formTag = new Token.StartTag("form");
        FormElement formEl = tb.insertForm(formTag, true);
        Assert.assertEquals("form", formEl.nodeName());
        Assert.assertEquals(formEl, tb.getFormElement());
        Assert.assertTrue(tb.onStack(formEl));

        FormElement formEl2 = tb.insertForm(formTag, false);
        Assert.assertEquals("form", formEl2.nodeName());
        Assert.assertEquals(formEl2, tb.getFormElement());

        Token.Comment commentToken = new Token.Comment();
        commentToken.getData().append("comment");
        tb.insert(commentToken);

        Token.Character charToken = new Token.Character();
        charToken.data("Sample text");
        tb.insert(charToken);

        Element scriptEl = tb.insert("script");
        Token.Character scriptData = new Token.Character();
        scriptData.data("var x = 1;");
        tb.insert(scriptData);
        Assert.assertEquals(1, scriptEl.dataNodes().size());
    }

    @Test
    public void testStackOperations() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", ParseErrorList.noTracking());

        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        Element p = new Element(Tag.valueOf("p"), "");
        Element span = new Element(Tag.valueOf("span"), "");

        tb.push(html);
        tb.push(body);
        tb.push(p);
        tb.push(span);

        Assert.assertEquals(span, tb.currentElement());
        Assert.assertEquals(p, tb.aboveOnStack(span));
        Assert.assertNull(tb.aboveOnStack(html));
        Assert.assertEquals(span, tb.getFromStack("span"));
        Assert.assertNull(tb.getFromStack("nonexistent"));

        tb.replaceOnStack(p, new Element(Tag.valueOf("div"), ""));
        Assert.assertNotNull(tb.getFromStack("div"));
        Assert.assertNull(tb.getFromStack("p"));

        Element inserted = new Element(Tag.valueOf("b"), "");
        tb.insertOnStackAfter(tb.getFromStack("div"), inserted);
        Assert.assertTrue(tb.onStack(inserted));

        tb.popStackToBefore("b");
        Assert.assertEquals(inserted, tb.currentElement());

        tb.push(new Element(Tag.valueOf("i"), ""));
        tb.popStackToClose("b");
        Assert.assertNull(tb.getFromStack("b"));
        Assert.assertNull(tb.getFromStack("i"));

        tb.push(new Element(Tag.valueOf("span"), ""));
        tb.push(new Element(Tag.valueOf("em"), ""));
        tb.popStackToClose("span", "em");
        Assert.assertNull(tb.getFromStack("em"));
        Assert.assertNull(tb.getFromStack("span"));

        Element toRemove = new Element(Tag.valueOf("section"), "");
        tb.push(toRemove);
        Assert.assertTrue(tb.removeFromStack(toRemove));
        Assert.assertFalse(tb.removeFromStack(toRemove));

        tb.push(new Element(Tag.valueOf("table"), ""));
        tb.push(new Element(Tag.valueOf("tbody"), ""));
        tb.push(new Element(Tag.valueOf("tr"), ""));
        tb.push(new Element(Tag.valueOf("div"), ""));

        tb.clearStackToTableRowContext();
        Assert.assertEquals("tr", tb.currentElement().nodeName());

        tb.push(new Element(Tag.valueOf("div"), ""));
        tb.clearStackToTableBodyContext();
        Assert.assertEquals("tbody", tb.currentElement().nodeName());

        tb.push(new Element(Tag.valueOf("div"), ""));
        tb.clearStackToTableContext();
        Assert.assertEquals("table", tb.currentElement().nodeName());

        tb.pop();
        Assert.assertNotNull(tb.getStack());
    }

    @Test
    public void testScopes() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", ParseErrorList.noTracking());

        tb.push(new Element(Tag.valueOf("html"), ""));
        tb.push(new Element(Tag.valueOf("body"), ""));
        tb.push(new Element(Tag.valueOf("div"), ""));
        tb.push(new Element(Tag.valueOf("p"), ""));

        Assert.assertTrue(tb.inScope("p"));
        Assert.assertTrue(tb.inScope("div"));
        Assert.assertFalse(tb.inScope("span"));
        Assert.assertTrue(tb.inScope(new String[]{"p", "span"}));

        tb.push(new Element(Tag.valueOf("ol"), ""));
        tb.push(new Element(Tag.valueOf("li"), ""));
        Assert.assertTrue(tb.inListItemScope("li"));
        Assert.assertFalse(tb.inListItemScope("div"));

        tb.push(new Element(Tag.valueOf("button"), ""));
        Assert.assertTrue(tb.inButtonScope("button"));
        Assert.assertFalse(tb.inButtonScope("p"));

        tb.push(new Element(Tag.valueOf("table"), ""));
        Assert.assertTrue(tb.inTableScope("table"));
        Assert.assertFalse(tb.inTableScope("p"));

        tb.push(new Element(Tag.valueOf("select"), ""));
        tb.push(new Element(Tag.valueOf("option"), ""));
        Assert.assertTrue(tb.inSelectScope("option"));
        Assert.assertFalse(tb.inSelectScope("div"));
    }

    @Test
    public void testResetInsertionMode() {
        String[] tags = new String[]{"select", "td", "tr", "tbody", "caption", "colgroup", "table", "head", "body", "frameset", "html", "custom"};
        for (String tag : tags) {
            HtmlTreeBuilder tb = new HtmlTreeBuilder();
            tb.initialiseParse("<div></div>", "http://example.com", ParseErrorList.noTracking());
            tb.getStack().clear();
            tb.push(new Element(Tag.valueOf("html"), ""));
            tb.push(new Element(Tag.valueOf(tag), ""));
            tb.resetInsertionMode();
            Assert.assertNotNull(tb.state());
        }
    }

    @Test
    public void testFormattingElements() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", ParseErrorList.noTracking());

        Element html = tb.insert("html");
        Element body = tb.insert("body");

        Element b1 = new Element(Tag.valueOf("b"), "");
        Element b2 = new Element(Tag.valueOf("b"), "");
        Element b3 = new Element(Tag.valueOf("b"), "");
        Element b4 = new Element(Tag.valueOf("b"), "");

        tb.pushActiveFormattingElements(b1);
        tb.pushActiveFormattingElements(b2);
        tb.pushActiveFormattingElements(b3);
        tb.pushActiveFormattingElements(b4);

        Assert.assertTrue(tb.isInActiveFormattingElements(b4));
        Assert.assertNotNull(tb.getActiveFormattingElement("b"));
        Assert.assertNull(tb.getActiveFormattingElement("i"));

        tb.insertMarkerToFormattingElements();
        Assert.assertNull(tb.getActiveFormattingElement("b"));

        Element i1 = new Element(Tag.valueOf("i"), "");
        Element i2 = new Element(Tag.valueOf("i"), "");
        tb.pushActiveFormattingElements(i1);
        tb.replaceActiveFormattingElement(i1, i2);
        Assert.assertTrue(tb.isInActiveFormattingElements(i2));
        Assert.assertFalse(tb.isInActiveFormattingElements(i1));

        tb.removeFromActiveFormattingElements(i2);
        Assert.assertFalse(tb.isInActiveFormattingElements(i2));

        tb.pushActiveFormattingElements(i1);
        tb.clearFormattingElementsToLastMarker();
        Assert.assertFalse(tb.isInActiveFormattingElements(i1));

        Element a = new Element(Tag.valueOf("a"), "");
        a.attr("href", "http://example.com");
        tb.pushActiveFormattingElements(a);
        tb.reconstructFormattingElements();
        Assert.assertTrue(tb.onStack(tb.getFromStack("a")));
    }

    @Test
    public void testGenerateImpliedEndTags() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", ParseErrorList.noTracking());

        tb.push(new Element(Tag.valueOf("html"), ""));
        tb.push(new Element(Tag.valueOf("body"), ""));
        tb.push(new Element(Tag.valueOf("p"), ""));
        tb.push(new Element(Tag.valueOf("li"), ""));

        tb.generateImpliedEndTags("p");
        Assert.assertEquals("p", tb.currentElement().nodeName());

        tb.push(new Element(Tag.valueOf("li"), ""));
        tb.generateImpliedEndTags();
        Assert.assertEquals("body", tb.currentElement().nodeName());
    }

    @Test
    public void testFosterParenting() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body><table></table></body></html>", "http://example.com", ParseErrorList.noTracking());

        Element html = tb.insert("html");
        Element body = tb.insert("body");
        Element table = tb.insert("table");

        tb.setFosterInserts(true);
        Assert.assertTrue(tb.isFosterInserts());

        Element span = new Element(Tag.valueOf("span"), "");
        tb.insertInFosterParent(span);
        Assert.assertEquals(body, span.parent());

        Element divNoTable = new Element(Tag.valueOf("div"), "");
        tb.getStack().clear();
        tb.push(html);
        tb.insertInFosterParent(divNoTable);
        Assert.assertEquals(html, divNoTable.parent());
    }

    @Test
    public void testFormAndHeadAndPendingTableChars() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html></html>", "http://example.com", ParseErrorList.noTracking());

        Element head = new Element(Tag.valueOf("head"), "");
        tb.setHeadElement(head);
        Assert.assertEquals(head, tb.getHeadElement());

        FormElement form = new FormElement(Tag.valueOf("form"), "", new org.jsoup.nodes.Attributes());
        tb.setFormElement(form);
        Assert.assertEquals(form, tb.getFormElement());

        List<Token.Character> chars = new ArrayList<Token.Character>();
        Token.Character c = new Token.Character();
        c.data("foo");
        chars.add(c);
        tb.setPendingTableCharacters(chars);
        Assert.assertEquals(1, tb.getPendingTableCharacters().size());

        tb.newPendingTableCharacters();
        Assert.assertEquals(0, tb.getPendingTableCharacters().size());

        Assert.assertTrue(tb.isSpecial(new Element(Tag.valueOf("p"), "")));
        Assert.assertFalse(tb.isSpecial(new Element(Tag.valueOf("custom"), "")));
    }

    @Test
    public void testToStringAndGetters() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse("<html><head></head><body></body></html>", "http://example.com", ParseErrorList.noTracking());
        Assert.assertNotNull(tb.getDocument());
        Assert.assertNotNull(tb.toString());
    }
}
