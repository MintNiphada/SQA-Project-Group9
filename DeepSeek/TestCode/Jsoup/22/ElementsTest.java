package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.helper.Validate;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ElementsTest {

    private Element e1;
    private Element e2;
    private Element e3;
    private Elements emptyElements;
    private Elements nonEmptyElements;

    @Before
    public void setUp() {
        e1 = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        e1.attr("id", "first");
        e1.attr("class", "alpha beta");
        e1.text("Hello");
        e2 = new Element(org.jsoup.parser.Tag.valueOf("span"), "");
        e2.attr("class", "beta gamma");
        e2.text("World");
        e3 = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        e3.text("Test");
        emptyElements = new Elements();
        nonEmptyElements = new Elements(e1, e2, e3);
    }

    @Test
    public void testDefaultConstructor() {
        Elements elements = new Elements();
        assertTrue(elements.isEmpty());
        assertEquals(0, elements.size());
    }

    @Test
    public void testCollectionConstructor() {
        Collection<Element> col = Arrays.asList(e1, e2);
        Elements elements = new Elements(col);
        assertEquals(2, elements.size());
        assertTrue(elements.contains(e1));
        assertTrue(elements.contains(e2));
    }

    @Test
    public void testListConstructor() {
        List<Element> list = new ArrayList<Element>();
        list.add(e1);
        list.add(e2);
        Elements elements = new Elements(list);
        assertEquals(2, elements.size());
        assertSame(list, elements.contents);
    }

    @Test
    public void testVarargsConstructor() {
        Elements elements = new Elements(e1, e2);
        assertEquals(2, elements.size());
        assertTrue(elements.contains(e1));
        assertTrue(elements.contains(e2));
    }

    @Test
    public void testClone() {
        Elements clone = nonEmptyElements.clone();
        assertEquals(nonEmptyElements.size(), clone.size());
        assertNotSame(nonEmptyElements, clone);
        for (int i = 0; i < nonEmptyElements.size(); i++) {
            assertNotSame(nonEmptyElements.get(i), clone.get(i));
            assertEquals(nonEmptyElements.get(i).outerHtml(), clone.get(i).outerHtml());
        }
        clone.get(0).attr("id", "modified");
        assertFalse("modified".equals(nonEmptyElements.get(0).attr("id")));
    }

    @Test
    public void testAttrGetFirstMatch() {
        assertEquals("first", nonEmptyElements.attr("id"));
    }

    @Test
    public void testAttrGetNoMatch() {
        assertEquals("", nonEmptyElements.attr("nonexistent"));
    }

    @Test
    public void testAttrGetEmptyElements() {
        assertEquals("", emptyElements.attr("id"));
    }

    @Test
    public void testHasAttrTrue() {
        assertTrue(nonEmptyElements.hasAttr("id"));
    }

    @Test
    public void testHasAttrFalse() {
        assertFalse(nonEmptyElements.hasAttr("nonexistent"));
    }

    @Test
    public void testHasAttrEmpty() {
        assertFalse(emptyElements.hasAttr("id"));
    }

    @Test
    public void testAttrSet() {
        Elements result = nonEmptyElements.attr("data-test", "value");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertEquals("value", e.attr("data-test"));
        }
    }

    @Test
    public void testRemoveAttr() {
        nonEmptyElements.attr("data-test", "value");
        Elements result = nonEmptyElements.removeAttr("data-test");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertFalse(e.hasAttr("data-test"));
        }
    }

    @Test
    public void testAddClass() {
        Elements result = nonEmptyElements.addClass("newclass");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertTrue(e.hasClass("newclass"));
        }
    }

    @Test
    public void testRemoveClass() {
        nonEmptyElements.addClass("toremove");
        Elements result = nonEmptyElements.removeClass("toremove");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertFalse(e.hasClass("toremove"));
        }
    }

    @Test
    public void testToggleClass() {
        nonEmptyElements.addClass("toggle");
        Elements result = nonEmptyElements.toggleClass("toggle");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertFalse(e.hasClass("toggle"));
        }
        nonEmptyElements.toggleClass("toggle");
        for (Element e : nonEmptyElements) {
            assertTrue(e.hasClass("toggle"));
        }
    }

    @Test
    public void testHasClassTrue() {
        assertTrue(nonEmptyElements.hasClass("beta"));
    }

    @Test
    public void testHasClassFalse() {
        assertFalse(nonEmptyElements.hasClass("nonexistent"));
    }

    @Test
    public void testHasClassEmpty() {
        assertFalse(emptyElements.hasClass("beta"));
    }

    @Test
    public void testValGet() {
        e1.val("inputValue");
        assertEquals("inputValue", nonEmptyElements.val());
    }

    @Test
    public void testValGetEmpty() {
        assertEquals("", emptyElements.val());
    }

    @Test
    public void testValSet() {
        Elements result = nonEmptyElements.val("newValue");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertEquals("newValue", e.val());
        }
    }

    @Test
    public void testText() {
        assertEquals("Hello World Test", nonEmptyElements.text());
    }

    @Test
    public void testTextEmpty() {
        assertEquals("", emptyElements.text());
    }

    @Test
    public void testHasTextTrue() {
        assertTrue(nonEmptyElements.hasText());
    }

    @Test
    public void testHasTextFalse() {
        Elements noText = new Elements(new Element(org.jsoup.parser.Tag.valueOf("div"), ""));
        assertFalse(noText.hasText());
    }

    @Test
    public void testHtml() {
        e1.html("<b>Bold</b>");
        e2.html("<i>Italic</i>");
        e3.html("Plain");
        String expected = "<b>Bold</b>\n<i>Italic</i>\nPlain";
        assertEquals(expected, nonEmptyElements.html());
    }

    @Test
    public void testHtmlEmpty() {
        assertEquals("", emptyElements.html());
    }

    @Test
    public void testOuterHtml() {
        String outer = nonEmptyElements.outerHtml();
        assertTrue(outer.contains("<div"));
        assertTrue(outer.contains("<span"));
        assertTrue(outer.contains("<p"));
    }

    @Test
    public void testToString() {
        assertEquals(nonEmptyElements.outerHtml(), nonEmptyElements.toString());
    }

    @Test
    public void testTagName() {
        Elements result = nonEmptyElements.tagName("section");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertEquals("section", e.tagName());
        }
    }

    @Test
    public void testHtmlSet() {
        Elements result = nonEmptyElements.html("<span>new</span>");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertEquals("<span>new</span>", e.html());
        }
    }

    @Test
    public void testPrepend() {
        Elements result = nonEmptyElements.prepend("<b>pre</b>");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertTrue(e.html().startsWith("<b>pre</b>"));
        }
    }

    @Test
    public void testAppend() {
        Elements result = nonEmptyElements.append("<b>post</b>");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertTrue(e.html().endsWith("<b>post</b>"));
        }
    }

    @Test
    public void testBefore() {
        Element parent = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        parent.appendChild(e1);
        Elements single = new Elements(e1);
        single.before("<span>before</span>");
        assertTrue(parent.html().contains("<span>before</span>"));
    }

    @Test
    public void testAfter() {
        Element parent = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        parent.appendChild(e1);
        Elements single = new Elements(e1);
        single.after("<span>after</span>");
        assertTrue(parent.html().contains("<span>after</span>"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapEmptyStringThrows() {
        nonEmptyElements.wrap("");
    }

    @Test
    public void testWrap() {
        Elements result = nonEmptyElements.wrap("<div class='wrap'></div>");
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertEquals("div", e.parent().tagName());
            assertTrue(e.parent().hasClass("wrap"));
        }
    }

    @Test
    public void testUnwrap() {
        Element parent = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        Element child = new Element(org.jsoup.parser.Tag.valueOf("span"), "");
        parent.appendChild(child);
        child.text("text");
        Elements elements = new Elements(child);
        elements.unwrap();
        assertEquals("text", parent.text());
        assertFalse(parent.children().isEmpty());
    }

    @Test
    public void testEmpty() {
        Elements result = nonEmptyElements.empty();
        assertSame(nonEmptyElements, result);
        for (Element e : nonEmptyElements) {
            assertEquals("", e.html());
        }
    }

    @Test
    public void testRemove() {
        Element parent = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        parent.appendChild(e1);
        Elements elements = new Elements(e1);
        elements.remove();
        assertFalse(parent.children().contains(e1));
    }

    @Test
    public void testSelect() {
        Elements selected = nonEmptyElements.select("div");
        assertEquals(1, selected.size());
        assertEquals(e1, selected.first());
    }

    @Test
    public void testNot() {
        Elements result = nonEmptyElements.not("div");
        assertEquals(2, result.size());
        assertFalse(result.contains(e1));
        assertTrue(result.contains(e2));
        assertTrue(result.contains(e3));
    }

    @Test
    public void testEqValidIndex() {
        Elements eq = nonEmptyElements.eq(1);
        assertEquals(1, eq.size());
        assertEquals(e2, eq.first());
    }

    @Test
    public void testEqInvalidIndex() {
        Elements eq = nonEmptyElements.eq(10);
        assertTrue(eq.isEmpty());
    }

    @Test
    public void testIsTrue() {
        assertTrue(nonEmptyElements.is("div"));
    }

    @Test
    public void testIsFalse() {
        assertFalse(nonEmptyElements.is("table"));
    }

    @Test
    public void testParents() {
        Element parent = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        parent.appendChild(e1);
        Elements elements = new Elements(e1);
        Elements parents = elements.parents();
        assertTrue(parents.contains(parent));
    }

    @Test
    public void testFirst() {
        assertEquals(e1, nonEmptyElements.first());
    }

    @Test
    public void testFirstEmpty() {
        assertNull(emptyElements.first());
    }

    @Test
    public void testLast() {
        assertEquals(e3, nonEmptyElements.last());
    }

    @Test
    public void testLastEmpty() {
        assertNull(emptyElements.last());
    }

    @Test
    public void testTraverse() {
        final StringBuilder sb = new StringBuilder();
        NodeVisitor visitor = new NodeVisitor() {
            public void head(Node node, int depth) {
                sb.append("head");
            }
            public void tail(Node node, int depth) {
                sb.append("tail");
            }
        };
        Elements result = nonEmptyElements.traverse(visitor);
        assertSame(nonEmptyElements, result);
        assertTrue(sb.length() > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraverseNullVisitor() {
        nonEmptyElements.traverse(null);
    }

    @Test
    public void testSize() {
        assertEquals(3, nonEmptyElements.size());
        assertEquals(0, emptyElements.size());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(emptyElements.isEmpty());
        assertFalse(nonEmptyElements.isEmpty());
    }

    @Test
    public void testContains() {
        assertTrue(nonEmptyElements.contains(e1));
        assertFalse(nonEmptyElements.contains(new Element(org.jsoup.parser.Tag.valueOf("div"), "")));
    }

    @Test
    public void testIterator() {
        Iterator<Element> it = nonEmptyElements.iterator();
        assertTrue(it.hasNext());
        assertEquals(e1, it.next());
    }

    @Test
    public void testToArray() {
        Object[] arr = nonEmptyElements.toArray();
        assertEquals(3, arr.length);
        assertEquals(e1, arr[0]);
    }

    @Test
    public void testToArrayTyped() {
        Element[] arr = nonEmptyElements.toArray(new Element[0]);
        assertEquals(3, arr.length);
        assertEquals(e1, arr[0]);
    }

    @Test
    public void testAdd() {
        Element newEl = new Element(org.jsoup.parser.Tag.valueOf("a"), "");
        assertTrue(nonEmptyElements.add(newEl));
        assertEquals(4, nonEmptyElements.size());
        assertTrue(nonEmptyElements.contains(newEl));
    }

    @Test
    public void testRemoveObject() {
        assertTrue(nonEmptyElements.remove(e1));
        assertEquals(2, nonEmptyElements.size());
        assertFalse(nonEmptyElements.contains(e1));
    }

    @Test
    public void testContainsAll() {
        assertTrue(nonEmptyElements.containsAll(Arrays.asList(e1, e2)));
        assertFalse(nonEmptyElements.containsAll(Arrays.asList(e1, new Element(org.jsoup.parser.Tag.valueOf("a"), ""))));
    }

    @Test
    public void testAddAll() {
        Collection<Element> col = Arrays.asList(new Element(org.jsoup.parser.Tag.valueOf("a"), ""), new Element(org.jsoup.parser.Tag.valueOf("b"), ""));
        assertTrue(nonEmptyElements.addAll(col));
        assertEquals(5, nonEmptyElements.size());
    }

    @Test
    public void testAddAllAtIndex() {
        Element newEl = new Element(org.jsoup.parser.Tag.valueOf("a"), "");
        assertTrue(nonEmptyElements.addAll(1, Arrays.asList(newEl)));
        assertEquals(4, nonEmptyElements.size());
        assertEquals(newEl, nonEmptyElements.get(1));
    }

    @Test
    public void testRemoveAll() {
        assertTrue(nonEmptyElements.removeAll(Arrays.asList(e1, e2)));
        assertEquals(1, nonEmptyElements.size());
        assertFalse(nonEmptyElements.contains(e1));
    }

    @Test
    public void testRetainAll() {
        assertTrue(nonEmptyElements.retainAll(Arrays.asList(e1)));
        assertEquals(1, nonEmptyElements.size());
        assertTrue(nonEmptyElements.contains(e1));
    }

    @Test
    public void testClear() {
        nonEmptyElements.clear();
        assertTrue(nonEmptyElements.isEmpty());
    }

    @Test
    public void testEquals() {
        Elements other = new Elements(e1, e2, e3);
        assertTrue(nonEmptyElements.equals(other));
        assertFalse(nonEmptyElements.equals(emptyElements));
    }

    @Test
    public void testHashCode() {
        Elements other = new Elements(e1, e2, e3);
        assertEquals(nonEmptyElements.hashCode(), other.hashCode());
    }

    @Test
    public void testGet() {
        assertEquals(e1, nonEmptyElements.get(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {
        nonEmptyElements.get(10);
    }

    @Test
    public void testSet() {
        Element newEl = new Element(org.jsoup.parser.Tag.valueOf("a"), "");
        Element old = nonEmptyElements.set(1, newEl);
        assertEquals(e2, old);
        assertEquals(newEl, nonEmptyElements.get(1));
    }

    @Test
    public void testAddAtIndex() {
        Element newEl = new Element(org.jsoup.parser.Tag.valueOf("a"), "");
        nonEmptyElements.add(1, newEl);
        assertEquals(4, nonEmptyElements.size());
        assertEquals(newEl, nonEmptyElements.get(1));
    }

    @Test
    public void testRemoveIndex() {
        Element removed = nonEmptyElements.remove(1);
        assertEquals(e2, removed);
        assertEquals(2, nonEmptyElements.size());
    }

    @Test
    public void testIndexOf() {
        assertEquals(0, nonEmptyElements.indexOf(e1));
        assertEquals(-1, nonEmptyElements.indexOf(new Element(org.jsoup.parser.Tag.valueOf("a"), "")));
    }

    @Test
    public void testLastIndexOf() {
        nonEmptyElements.add(e1);
        assertEquals(3, nonEmptyElements.lastIndexOf(e1));
    }

    @Test
    public void testListIterator() {
        ListIterator<Element> it = nonEmptyElements.listIterator();
        assertTrue(it.hasNext());
        assertEquals(e1, it.next());
    }

    @Test
    public void testListIteratorWithIndex() {
        ListIterator<Element> it = nonEmptyElements.listIterator(1);
        assertEquals(e2, it.next());
    }

    @Test
    public void testSubList() {
        List<Element> sub = nonEmptyElements.subList(0, 2);
        assertEquals(2, sub.size());
        assertEquals(e1, sub.get(0));
        assertEquals(e2, sub.get(1));
    }
}
