package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

public class ElementTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructorWithNullTagThrowsIllegalArgument() {
        new Element(null, "", new Attributes());
    }

    @Test
    public void constructorWithTagAndBaseUriSetsTag() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "");
        assertEquals("div", el.tagName());
    }

    @Test
    public void nodeNameReturnsTagName() {
        Element el = new Element(Tag.valueOf("span"), "");
        assertEquals("span", el.nodeName());
    }

    @Test
    public void tagNameGetterReturnsName() {
        Element el = new Element(Tag.valueOf("p"), "");
        assertEquals("p", el.tagName());
 }

    @Test
    public void tagNameSetterChangesTag() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("span");
        assertEquals("span", el.tagName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void tagNameSetterWithEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName("));    
    }

    @Test(expected = IllegalArgumentException.class)
    public void tagNameSetterWithNullThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.tagName(null);    
    }

    @Test
    public void tagMethodReturnsTagObject() {
        Tag tag = Tag.valueOf("div");
        Element el = new Element(tag, "");
        assertSame(tag, el.tag());
 }

    @Test
    public void isBlockDelegatesToTag() {
        Element div = new Element(Tag.valueOf("div"), "");
        Element span = new Element(Tag.valueOf("span"), "");
        assertTrue(div.isBlock());
        assertFalse(span.isBlock());
    }

    @Test
    public void idReturnsIdAttribute() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "testId");
        assertEquals("testId", el.id());
    }

    @Test
    public void idReturnsEmptyWhenNoIdAttribute() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertEquals("", el.id());
 }

    @Test
    public void attrKeyValueReturnsElement() {
        Element el = new Element(Tag.valueOf("div"), "");
        Element returned = el.attr("class", "someClass");
        assertSame(el, returned);
        assertEquals("someClass", el.attr("class"));
    }

    @Test
    public void datasetReturnsAttributesDataset() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("data-name", "value");
        Map<String, String> ds = el.dataset();
        assertEquals("value", ds.get("name"));
    }

    @Test
    public void parentReturnsParentElement() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);
        assertSame(parent, child.parent());
    }

    @Test
    public void parentsReturnsAncestorsExcludingRoot() {
        Document doc = Document.createShell("");
        Element body = doc.body();
        Element div = body.appendElement("div");
        Element p = div.appendElement("p");
        Elements parents = p.parents();
        assertEquals(2, parents.size());
        assertSame(div, parents.get(0));
        assertSame(body, parents.get(1));
    }

    @Test
    public void childByIndexReturnsElementChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child1);
        parent.appendChild(child2);
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void childByIndexOutOfBoundsThrows() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.child(0);
    }

    @Test
    public void childrenReturnsOnlyElementChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(new TextNode("text", ""));
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);
        Elements children = parent.children();
        assertEquals(1, children.size());
        assertSame(child, children.get(0));
    }

    @Test
    public void textNodesReturnsUnmodifiableListOfTextNodes() {
        Element parent = new Element(Tag.valueOf("div"), "");
        TextNode tn = new TextNode("hello", "");
        parent.appendChild(tn);
        List<TextNode> textNodes = parent.textNodes();
        assertEquals(1, textNodes.size());
        assertSame(tn, textNodes.get(0));
        try {
            textNodes.add(new TextNode("", ""));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void dataNodesReturnsDataNodes() {
        Element parent = new Element(Tag.valueOf("script"), "");
        DataNode dn = new DataNode("var x=1;", "");
        parent.appendChild(dn);
        List<DataNode> dataNodes = parent.dataNodes();
        assertEquals(1, dataNodes.size());
        assertSame(dn, dataNodes.get(0));
    }

    @Test
    public void selectCssQueryReturnsMatchedElements() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span");
        child.addClass("test");
        Elements selected = parent.select("span.test");
        assertEquals(1, selected.size());
        assertSame(child, selected.first());
    }

    @Test
    public void appendChildAddsNodeAndReturnsElement() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        Element returned = parent.appendChild(child);
        assertSame(parent, returned);
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void appendChildNullThrows() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(null);
    }

    @Test
    public void prependChildAddsNodeAtStart() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");
        parent.appendChild(child1);
        parent.prependChild(child2);
        assertEquals(2, parent.children().size());
        assertSame(child2, parent.child(0));
        assertSame(child1, parent.child(1));
    }

    @Test
    public void appendElementCreatesAndAddsChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span");
        assertNotNull(child);
        assertEquals("span", child.tagName());
        assertSame(parent, child.parent());
    }

    @Test
    public void prependElementCreatesAndAddsFirstChild() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element existing = parent.appendElement("p");
        Element newFirst = parent.prependElement("span");
        assertSame(newFirst, parent.child(0));
        assertSame(existing, parent.child(1));
    }

    @Test
    public void appendTextCreatesTextNodeAndAppends() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("hello");
        assertEquals("hello", parent.text());
    }

    @Test
    public void prependTextCreatesTextNodeAndPrepends() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("world");
        parent.prependText("hello");
        assertEquals("hello world", parent.text());
    }

    @Test
    public void appendHtmlParseAndAppendsNodes() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.append("<p>one</p><span>two</span>");
        assertEquals(2, parent.children().size());
        assertEquals("p", parent.child(0).tagName());
        assertEquals("span", parent.child(1).tagName());
    }

    @Test
    public void prependHtmlParseAndPrependsNodes() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.append("<p>one</p>");
        parent.prepend("<span>zero</span>");
        assertEquals(2, parent.children().size());
        assertEquals("span", parent.child(0).tagName());
    }

    @Test
    public void beforeHtmlInsertsSiblingBefore() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        child2.before("<em>middle</em>");
        List<Element> children = parent.children();
        assertEquals(3, children.size());
        assertEquals("p", children.get(0).tagName());
        assertEquals("em", children.get(1).tagName());
        assertEquals("span", children.get(2).tagName());
    }

    @Test
    public void afterHtmlInsertsSiblingAfter() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        child1.after("<em>middle</em>");
        List<Element> children = parent.children();
        assertEquals(3, children.size());
        assertEquals("p", children.get(0).tagName());
        assertEquals("em", children.get(1).tagName());
        assertEquals("span", children.get(2).tagName());
    }

    @Test
    public void beforeNodeInsertsSibling() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        Element newNode = new Element(Tag.valueOf("em"), "");
        child2.before(newNode);
        assertEquals("p", parent.child(0).tagName());
        assertEquals("em", parent.child(1).tagName());
        assertEquals("span", parent.child(2).tagName());
    }

    @Test
    public void afterNodeInsertsSibling() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        Element newNode = new Element(Tag.valueOf("em"), "");
        child1.after(newNode);
        assertEquals("p", parent.child(0).tagName());
        assertEquals("em", parent.child(1).tagName());
        assertEquals("span", parent.child(2).tagName());
    }

    @Test
    public void emptyRemovesAllChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("hello");
        parent.appendElement("p");
        parent.empty();
        assertEquals(0, parent.children().size());
        assertEquals("", parent.text());
    }

    @Test
    public void wrapWrapsElementWithHtml() {
        Document doc = Document.createShell("");
        Element body = doc.body();
        Element p = body.appendElement("p");
        p.text("content");
        p.wrap("<div class=\"wrap\"></div>");
        Element wrapper = body.child(0);
        assertEquals("div", wrapper.tagName());
        assertTrue(wrapper.hasClass("wrap"));
        assertEquals(p, wrapper.child(0));
    }

    @Test
    public void siblingElementsReturnsParentChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        Elements siblings = child1.siblingElements();
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(child1));
        assertTrue(siblings.contains(child2));
    }

    @Test
    public void nextElementSiblingReturnsNextSiblingOrNull() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        assertSame(child2, child1.nextElementSibling());
        assertNull(child2.nextElementSibling());
    }

    @Test
    public void previousElementSiblingReturnsPreviousSiblingOrNull() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        assertSame(child1, child2.previousElementSibling());
        assertNull(child1.previousElementSibling());
    }

    @Test
    public void firstElementSiblingReturnsFirstSiblingOrNull() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        assertSame(child1, child1.firstElementSibling());
        assertSame(child1, child2.firstElementSibling());
        Element solo = new Element(Tag.valueOf("div"), "");
        assertNull(solo.firstElementSibling());
    }

    @Test
    public void lastElementSiblingReturnsLastSiblingOrNull() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        assertSame(child2, child1.lastElementSibling());
        assertSame(child2, child2.lastElementSibling());
        Element solo = new Element(Tag.valueOf("div"), "");
        assertNull(solo.lastElementSibling());
    }

    @Test
    public void elementSiblingIndexReturnsIndexInParentChildren() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = parent.appendElement("p");
        Element child2 = parent.appendElement("span");
        assertEquals(0, (int) child1.elementSiblingIndex());
        assertEquals(1, (int) child2.elementSiblingIndex());
        Element solo = new Element(Tag.valueOf("div"), "");
        assertEquals(0, (int) solo.elementSiblingIndex());
    }

    @Test
    public void getElementsByTagReturnsMatchingElements() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("span");
        parent.appendElement("p");
        Elements ps = parent.getElementsByTag("p");
        assertEquals(2, ps.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getElementsByTagEmptyStringThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByTag("");    
    }

    @Test
    public void getElementByIdFindsFirstMatch() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = parent.appendElement("span");
        child.attr("id", "target");
        Element found = parent.getElementById("target");
        assertSame(child, found);
    }

    @Test
    public void getElementByIdNoMatchReturnsNull() {
        Element parent = new Element(Tag.valueOf("div"), "");
        assertNull(parent.getElementById("none"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getElementByIdEmptyThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementById("");    
    }

    @Test
    public void getElementsByClassFindsByClassName() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").addClass("target");
        parent.appendElement("span").addClass("other");
        Elements result = parent.getElementsByClass("target");
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsByAttributeFindsByAttributePresence() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("data-x", "1");
        parent.appendElement("span");
        Elements result = parent.getElementsByAttribute("data-x");
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsByAttributeStartingFindsByAttributePrefix() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("data-x", "1");
        parent.appendElement("span").attr("data-y", "1");
        Elements result = parent.getElementsByAttributeStarting("data-");
        assertEquals(2, result.size());
    }

    @Test
    public void getElementsByAttributeValueFindsByExactMatch() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("class", "target");
        parent.appendElement("span").attr("class", "other");
        Elements result = parent.getElementsByAttributeValue("class", "target");
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsByAttributeValueNotFindsNonMatch() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("class", "target");
        parent.appendElement("span").attr("class", "other");
        Elements result = parent.getElementsByAttributeValueNot("class", "target");
        assertEquals(1, result.size());
        assertEquals("span", result.get(0).tagName());
    }

    @Test
    public void getElementsByAttributeValueStartingFindsByValuePrefix() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("class", "target-one");
        parent.appendElement("span").attr("class", "target-two");
        parent.appendElement("em").attr("class", "other");
        Elements result = parent.getElementsByAttributeValueStarting("class", "target-");
        assertEquals(2, result.size());
    }

    @Test
    public void getElementsByAttributeValueEndingFindsByValueSuffix() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("class", "suffix-1");
        parent.appendElement("span").attr("class", "suffix-2");
        parent.appendElement("em").attr("class", "other");
        Elements result = parent.getElementsByAttributeValueEnding("class", "fix-1");
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsByAttributeValueContainingFindsBySubstring() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("class", "target-one");
        parent.appendElement("span").attr("class", "target-two");
        parent.appendElement("em").attr("class", "other");
        Elements result = parent.getElementsByAttributeValueContaining("class", "get-");
        assertEquals(2, result.size());
    }

    @Test
    public void getElementsByAttributeValueMatchingWithPatternFindsByRegex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("class", "one");
        parent.appendElement("span").attr("class", "two");
        parent.appendElement("em").attr("class", "three");
        Pattern pattern = Pattern.compile("t.o");
        Elements result = parent.getElementsByAttributeValueMatching("class", pattern);
        assertEquals(1, result.size());
        assertEquals("span", result.get(0).tagName());
    }

    @Test
    public void getElementsByAttributeValueMatchingWithStringRegexWorks() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").attr("class", "one");
        parent.appendElement("span").attr("class", "two");
        Elements result = parent.getElementsByAttributeValueMatching("class", "t.o");
        assertEquals(1, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getElementsByAttributeValueMatchingInvalidRegexThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsByAttributeValueMatching("class", "[invalid");
    }

    @Test
    public void getElementsByIndexLessThanFindsByIndex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("span");
        parent.appendElement("em");
        Elements result = parent.getElementsByIndexLessThan(1);
        assertEquals(1, result.size());
        assertEquals("p", result.get(0).tagName());
    }

    @Test
    public void getElementsByIndexGreaterThanFindsByIndex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("span");
        parent.appendElement("em");
        Elements result = parent.getElementsByIndexGreaterThan(1);
        assertEquals(1, result.size());
        assertEquals("em", result.get(0).tagName());
    }

    @Test
    public void getElementsByIndexEqualsFindsExactIndex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.appendElement("span");
        parent.appendElement("em");
        Elements result = parent.getElementsByIndexEquals(1);
        assertEquals(1, result.size());
        assertEquals("span", result.get(0).tagName());
    }

    @Test
    public void getElementsContainingTextFindsByText() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").text("Hello");
        parent.appendElement("span").text("World");
        Elements result = parent.getElementsContainingText("hello");
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsContainingOwnTextFindsDirectText() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element p = parent.appendElement("p");
        p.appendText("Hello");
        p.appendElement("b").text("child");
        Elements result = parent.getElementsContainingOwnText("ello");
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsMatchingTextWithPatternFindsByRegex() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").text("Hello");
        parent.appendElement("span").text("World");
        Pattern pattern = Pattern.compile("W.rld");
        Elements result = parent.getElementsMatchingText(pattern);
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsMatchingTextWithStringRegexWorks() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").text("Hello");
        parent.appendElement("span").text("World");
        Elements result = parent.getElementsMatchingText("W.rld");
        assertEquals(1, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getElementsMatchingTextInvalidRegexThrows() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.getElementsMatchingText("[invalid");
    }

    @Test
    public void getElementsMatchingOwnTextWithPatternFindsDirectText() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element p = parent.appendElement("p");
        p.appendText("Hello");
        p.appendElement("b").text("child");
        Pattern pattern = Pattern.compile("ell.");
        Elements result = parent.getElementsMatchingOwnText(pattern);
        assertEquals(1, result.size());
    }

    @Test
    public void getElementsMatchingOwnTextWithStringRegexWorks() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element p = parent.appendElement("p");
        p.appendText("Hello");
        Elements result = parent.getElementsMatchingOwnText("H.llo");
        assertEquals(1, result.size());
    }

    @Test
    public void getAllElementsReturnsAllDescendantsAndSelf() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").appendElement("span");
        parent.appendElement("em");
        Elements all = parent.getAllElements();
        assertEquals(4, all.size()); // div, p, span, em
    }

    @Test
    public void textReturnsCombinedTextOfAllDescendants() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("Hello ");
        parent.appendElement("b").text("there");
        parent.appendText(" now!");
        assertEquals("Hello there now!", parent.text());
    }

    @Test
    public void textSetterClearsAndAddsText() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.text("new text");
        assertEquals(1, parent.children().size());
        assertTrue(parent.child(0) instanceof TextNode);
        assertEquals("new text", parent.text());
    }

    @Test
    public void ownTextReturnsDirectTextOnly() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("direct ");
        parent.appendElement("b").text("child");
        parent.appendText("more");
        assertEquals("direct more", parent.ownText());
    }

    @Test
    public void hasTextReturnsTrueIfNonBlankTextExists() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendText("  ");
        assertFalse(parent.hasText());
        parent.appendText("hello");
        assertTrue(parent.hasText());
    }

    @Test
    public void dataReturnsCombinedDataOfDataNodesAndChildElements() {
        Element script = new Element(Tag.valueOf("script"), "");
        script.appendChild(new DataNode("var x=1;", ""));
        assertEquals("var x=1;", script.data());
        Element div = new Element(Tag.valueOf("div"), "");
        div.appendChild(script.clone());
        assertEquals("var x=1;", div.data());
    }

    @Test
    public void classNameReturnsClassAttribute() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "foo bar");
        assertEquals("foo bar", el.className());
    }

    @Test
    public void classNamesReturnsSetOfClasses() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("class", "foo bar baz");
        Set<String> classes = el.classNames();
        assertEquals(3, classes.size());
        assertTrue(classes.contains("foo"));
        assertTrue(classes.contains("bar"));
        assertTrue(classes.contains("baz"));
    }

    @Test
    public void classNamesSetModifiesClassAttribute() {
        Element el = new Element(Tag.valueOf("div"), "");
        Set<String> newClasses = new LinkedHashSet<String>();
        newClasses.add("one");
        newClasses.add("two");
        el.classNames(newClasses);
        assertEquals("one two", el.className());
    }

    @Test
    public void hasClassChecksCaseInsensitively() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("Foo");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("FOO"));
        assertFalse(el.hasClass("Bar"));
    }

    @Test
    public void addClassAddsClass() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("newClass");
        assertTrue(el.hasClass("newClass"));
    }

    @Test
    public void removeClassRemovesClass() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("toRemove");
        el.removeClass("toRemove");
        assertFalse(el.hasClass("toRemove"));
    }

    @Test
    public void toggleClassAddsIfAbsent() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.toggleClass("toggledClass");
        assertTrue(el.hasClass("toggledClass"));
    }

    @Test
    public void toggleClassRemovesIfPresent() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.addClass("toggledClass");
        el.toggleClass("toggledClass");
        assertFalse(el.hasClass("toggledClass"));
    }

    @Test
    public void valForTextareaReturnsText() {
        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.text("some text");
        assertEquals("some text", textarea.val());
    }

    @Test
    public void valForInputReturnsValueAttribute() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.attr("value", "val1");
        assertEquals("val1", input.val());
    }

    @Test
    public void valSetterForTextareaSetsText() {
        Element textarea = new Element(Tag.valueOf("textarea"), "");
        textarea.val("new val");
        assertEquals("new val", textarea.text());
    }

    @Test
    public void valSetterForInputSetsValueAttribute() {
        Element input = new Element(Tag.valueOf("input"), "");
        input.val("newVal");
        assertEquals("newVal", input.attr("value"));
    }

    @Test
    public void htmlGetterReturnsInnerHtml() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p").text("Hello");
        parent.appendElement("span");
        String inner = parent.html();
        assertTrue(inner.contains("<p>Hello</p>"));
        assertTrue(inner.contains("<span></span>"));
    }

    @Test
    public void htmlSetterReplacesInnerHtml() {
        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendElement("p");
        parent.html("<span>new</span>");
        assertEquals(1, parent.children().size());
        assertEquals("span", parent.child(0).tagName());
        assertEquals("new", parent.child(0).text());
    }

    @Test
    public void preserveWhitespaceReturnsTrueIfTagOrParentPreserves() {
        Element pre = new Element(Tag.valueOf("pre"), "");
        assertTrue(pre.preserveWhitespace());
        Element div = new Element(Tag.valueOf("div"), "");
        assertFalse(div.preserveWhitespace());
        pre.appendChild(div);
        assertTrue(div.preserveWhitespace());
    }

    @Test
    public void equalsChecksIdentity() {
        Element el = new Element(Tag.valueOf("div"), "");
        assertTrue(el.equals(el));
        assertFalse(el.equals(new Element(Tag.valueOf("div"), "")));
    }

    @Test
    public void cloneCreatesDeepCopy() {
        Element original = new Element(Tag.valueOf("div"), "");
        original.attr("class", "test");
        original.appendText("hello");
        Element clone = original.clone();
        assertNotSame(riginal, clone);
        assertEquals(riginal.html(), clone.html());
        assertTrue(clone.classNames().contains("test"));
    }

    @Test
    public void toStringReturnsOuterHtml() {
        Element el = new Element(Tag.valueOf("div"), "");
        el.attr("id", "test");
        String str = el.toString();
        assertTrue(str.startsWith("<div"));
        assertTrue(str.endsWith("</div>"));
    }
}
