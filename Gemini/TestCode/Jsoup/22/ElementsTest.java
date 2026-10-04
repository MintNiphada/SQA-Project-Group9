package org.jsoup.select;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ElementsTest {

    @Test
    public void testConstructorsAndCloning() {
        Elements empty = new Elements();
        Assert.assertEquals(0, empty.size());

        Element el1 = new Element(Tag.valueOf("p"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");

        List<Element> list = new ArrayList<Element>();
        list.add(el1);
        list.add(el2);

        Elements fromCollection = new Elements((java.util.Collection<Element>) list);
        Assert.assertEquals(2, fromCollection.size());

        Elements fromList = new Elements(list);
        Assert.assertEquals(2, fromList.size());

        Elements fromVarargs = new Elements(el1, el2);
        Assert.assertEquals(2, fromVarargs.size());

        Elements cloned = fromVarargs.clone();
        Assert.assertEquals(2, cloned.size());
        Assert.assertNotSame(fromVarargs.get(0), cloned.get(0));
        Assert.assertEquals(fromVarargs.get(0).tagName(), cloned.get(0).tagName());
    }

    @Test
    public void testAttributeMethods() {
        String html = "<div id='d1' class='test' title='main'>One</div><div id='d2' title='sub'>Two</div>";
        Document doc = Jsoup.parse(html);
        Elements divs = doc.select("div");

        Assert.assertTrue(divs.hasAttr("id"));
        Assert.assertTrue(divs.hasAttr("class"));
        Assert.assertFalse(divs.hasAttr("nonexistent"));
        Assert.assertEquals("d1", divs.attr("id"));
        Assert.assertEquals("test", divs.attr("class"));

        Elements empty = new Elements();
        Assert.assertFalse(empty.hasAttr("id"));
        Assert.assertEquals("", empty.attr("id"));

        divs.attr("data-custom", "value1");
        Assert.assertEquals("value1", divs.get(0).attr("data-custom"));
        Assert.assertEquals("value1", divs.get(1).attr("data-custom"));

        divs.removeAttr("title");
        Assert.assertFalse(divs.hasAttr("title"));
        Assert.assertFalse(divs.get(0).hasAttr("title"));
        Assert.assertFalse(divs.get(1).hasAttr("title"));
    }

    @Test
    public void testClassMethods() {
        String html = "<p class='one two'>Text 1</p><p class='one'>Text 2</p><p>Text 3</p>";
        Document doc = Jsoup.parse(html);
        Elements ps = doc.select("p");

        Assert.assertTrue(ps.hasClass("one"));
        Assert.assertTrue(ps.hasClass("two"));
        Assert.assertFalse(ps.hasClass("three"));

        Elements empty = new Elements();
        Assert.assertFalse(empty.hasClass("one"));

        ps.addClass("three");
        Assert.assertTrue(ps.get(0).hasClass("three"));
        Assert.assertTrue(ps.get(1).hasClass("three"));
        Assert.assertTrue(ps.get(2).hasClass("three"));

        ps.removeClass("one");
        Assert.assertFalse(ps.hasClass("one"));
        Assert.assertFalse(ps.get(0).hasClass("one"));

        ps.toggleClass("toggle-check");
        Assert.assertTrue(ps.hasClass("toggle-check"));
        Assert.assertTrue(ps.get(0).hasClass("toggle-check"));

        ps.toggleClass("toggle-check");
        Assert.assertFalse(ps.hasClass("toggle-check"));
    }

    @Test
    public void testValMethod() {
        String html = "<form><input name='foo' value='bar'/><input name='baz' value='qux'/></form>";
        Document doc = Jsoup.parse(html);
        Elements inputs = doc.select("input");

        Assert.assertEquals("bar", inputs.val());

        inputs.val("updated");
        Assert.assertEquals("updated", inputs.get(0).val());
        Assert.assertEquals("updated", inputs.get(1).val());

        Elements empty = new Elements();
        Assert.assertEquals("", empty.val());
        empty.val("something");
    }

    @Test
    public void testTextAndHtml() {
        String html = "<div><p>Hello</p><p><b>World</b></p><p></p></div>";
        Document doc = Jsoup.parse(html);
        Elements ps = doc.select("p");

        Assert.assertEquals("Hello World", ps.text());
        Assert.assertTrue(ps.hasText());

        Elements empty = new Elements();
        Assert.assertEquals("", empty.text());
        Assert.assertFalse(empty.hasText());
        Assert.assertEquals("", empty.html());
        Assert.assertEquals("", empty.outerHtml());
        Assert.assertEquals("", empty.toString());

        Assert.assertEquals("Hello\n<b>World</b>\n", ps.html());
        Assert.assertEquals("<p>Hello</p>\n<p><b>World</b></p>\n<p></p>", ps.outerHtml());
        Assert.assertEquals(ps.outerHtml(), ps.toString());

        ps.html("<em>New</em>");
        Assert.assertEquals("New New New", ps.text());
        Assert.assertEquals("<em>New</em>\n<em>New</em>\n<em>New</em>", ps.html());
    }

    @Test
    public void testDomManipulationMethods() {
        String html = "<div><i>One</i><i>Two</i></div>";
        Document doc = Jsoup.parse(html);
        Elements is = doc.select("i");

        is.tagName("em");
        Assert.assertEquals("<em>One</em>\n<em>Two</em>", is.outerHtml());

        is.prepend("<span>Pre-</span>");
        Assert.assertEquals("<em><span>Pre-</span>One</em>\n<em><span>Pre-</span>Two</em>", is.outerHtml());

        is.append("<span>-Post</span>");
        Assert.assertEquals("<em><span>Pre-</span>One<span>-Post</span></em>\n<em><span>Pre-</span>Two<span>-Post</span></em>", is.outerHtml());

        is.before("<hr>");
        Assert.assertEquals(2, doc.select("hr").size());

        is.after("<br>");
        Assert.assertEquals(2, doc.select("br").size());

        is.wrap("<div class='wrapper'></div>");
        Assert.assertEquals(2, doc.select(".wrapper").size());

        is.empty();
        Assert.assertEquals("<em></em>\n<em></em>", is.outerHtml());

        is.unwrap();
        Assert.assertEquals(0, doc.select("em").size());

        Elements wrappers = doc.select(".wrapper");
        wrappers.remove();
        Assert.assertEquals(0, doc.select(".wrapper").size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapEmptyHtmlException() {
        Elements els = new Elements(new Element(Tag.valueOf("p"), ""));
        els.wrap("");
    }

    @Test
    public void testSelectionAndFiltering() {
        String html = "<div id='d1' class='test'><span>1</span></div><div id='d2'><span>2</span></div>";
        Document doc = Jsoup.parse(html);
        Elements divs = doc.select("div");

        Elements selectedSpan = divs.select("span");
        Assert.assertEquals(2, selectedSpan.size());

        Elements notD1 = divs.not("#d1");
        Assert.assertEquals(1, notD1.size());
        Assert.assertEquals("d2", notD1.get(0).id());

        Elements eq0 = divs.eq(0);
        Assert.assertEquals(1, eq0.size());
        Assert.assertEquals("d1", eq0.get(0).id());

        Elements eqOutOfRange = divs.eq(10);
        Assert.assertEquals(0, eqOutOfRange.size());

        Assert.assertTrue(divs.is("#d1"));
        Assert.assertFalse(divs.is("#nonexistent"));

        Elements parents = divs.parents();
        Assert.assertTrue(parents.contains(doc.body()));
    }

    @Test
    public void testFirstLastAndTraverse() {
        Elements empty = new Elements();
        Assert.assertNull(empty.first());
        Assert.assertNull(empty.last());

        Element el1 = new Element(Tag.valueOf("p"), "");
        Element el2 = new Element(Tag.valueOf("span"), "");
        Elements els = new Elements(el1, el2);

        Assert.assertSame(el1, els.first());
        Assert.assertSame(el2, els.last());

        final List<Node> visited = new ArrayList<Node>();
        els.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                visited.add(node);
            }

            @Override
            public void tail(Node node, int depth) {
            }
        });

        Assert.assertEquals(2, visited.size());
        Assert.assertSame(el1, visited.get(0));
        Assert.assertSame(el2, visited.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTraverseNullVisitor() {
        Elements els = new Elements(new Element(Tag.valueOf("p"), ""));
        els.traverse(null);
    }

    @Test
    public void testListInterfaceDelegates() {
        Element el1 = new Element(Tag.valueOf("h1"), "");
        Element el2 = new Element(Tag.valueOf("h2"), "");
        Element el3 = new Element(Tag.valueOf("h3"), "");

        Elements els = new Elements();
        Assert.assertTrue(els.isEmpty());
        Assert.assertEquals(0, els.size());

        Assert.assertTrue(els.add(el1));
        Assert.assertEquals(1, els.size());
        Assert.assertFalse(els.isEmpty());
        Assert.assertTrue(els.contains(el1));
        Assert.assertFalse(els.contains(el2));

        els.add(0, el2);
        Assert.assertSame(el2, els.get(0));
        Assert.assertSame(el1, els.get(1));

        Assert.assertEquals(0, els.indexOf(el2));
        Assert.assertEquals(1, els.indexOf(el1));
        Assert.assertEquals(-1, els.indexOf(el3));

        els.add(el2);
        Assert.assertEquals(2, els.lastIndexOf(el2));

        Assert.assertTrue(els.containsAll(Arrays.asList(el1, el2)));
        Assert.assertFalse(els.containsAll(Arrays.asList(el1, el3)));

        Element setOld = els.set(1, el3);
        Assert.assertSame(el1, setOld);
        Assert.assertSame(el3, els.get(1));

        Element removedIndex = els.remove(1);
        Assert.assertSame(el3, removedIndex);
        Assert.assertEquals(2, els.size());

        boolean removedObj = els.remove(el2);
        Assert.assertTrue(removedObj);
        Assert.assertEquals(1, els.size());
        Assert.assertFalse(els.remove(new Object()));

        els.clear();
        Assert.assertEquals(0, els.size());

        Assert.assertTrue(els.addAll(Arrays.asList(el1, el2)));
        Assert.assertEquals(2, els.size());

        Assert.assertTrue(els.addAll(1, Collections.singletonList(el3)));
        Assert.assertEquals(3, els.size());
        Assert.assertSame(el3, els.get(1));

        Assert.assertTrue(els.removeAll(Collections.singletonList(el3)));
        Assert.assertEquals(2, els.size());

        Assert.assertTrue(els.retainAll(Collections.singletonList(el1)));
        Assert.assertEquals(1, els.size());
        Assert.assertSame(el1, els.get(0));

        Object[] arrayObj = els.toArray();
        Assert.assertEquals(1, arrayObj.length);
        Assert.assertSame(el1, arrayObj[0]);

        Element[] arrayTyped = els.toArray(new Element[0]);
        Assert.assertEquals(1, arrayTyped.length);
        Assert.assertSame(el1, arrayTyped[0]);

        Iterator<Element> it = els.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(el1, it.next());
        Assert.assertFalse(it.hasNext());

        ListIterator<Element> lit = els.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertSame(el1, lit.next());

        ListIterator<Element> litIndex = els.listIterator(1);
        Assert.assertFalse(litIndex.hasNext());
        Assert.assertTrue(litIndex.hasPrevious());
        Assert.assertSame(el1, litIndex.previous());

        List<Element> sub = els.subList(0, 1);
        Assert.assertEquals(1, sub.size());
        Assert.assertSame(el1, sub.get(0));

        Elements sameEls = new Elements(el1);
        Assert.assertEquals(els, sameEls);
        Assert.assertEquals(els.hashCode(), sameEls.hashCode());
        Assert.assertNotEquals(els, new Elements(el2));
    }
}
