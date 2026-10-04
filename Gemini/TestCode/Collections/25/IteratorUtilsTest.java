package org.apache.commons.collections4;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.functors.NotNullPredicate;
import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.collections4.iterators.BoundedIterator;
import org.apache.commons.collections4.iterators.LoopingIterator;
import org.apache.commons.collections4.iterators.LoopingListIterator;
import org.apache.commons.collections4.iterators.NodeListIterator;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.collections4.iterators.PushbackIterator;
import org.apache.commons.collections4.iterators.SkippingIterator;
import org.apache.commons.collections4.iterators.ZippingIterator;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.*;

/**
 * High-coverage unit tests for {@link IteratorUtils}.
 */
public class IteratorUtilsTest {

    @Test
    public void testConstructorIsPrivate() throws Exception {
        final Constructor<IteratorUtils> constructor = IteratorUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        final IteratorUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    public void testEmptyConstantsAndMethods() {
        assertNotNull(IteratorUtils.EMPTY_ITERATOR);
        assertFalse(IteratorUtils.EMPTY_ITERATOR.hasNext());

        assertNotNull(IteratorUtils.EMPTY_LIST_ITERATOR);
        assertFalse(IteratorUtils.EMPTY_LIST_ITERATOR.hasNext());
        assertFalse(IteratorUtils.EMPTY_LIST_ITERATOR.hasPrevious());

        assertNotNull(IteratorUtils.EMPTY_ORDERED_ITERATOR);
        assertFalse(IteratorUtils.EMPTY_ORDERED_ITERATOR.hasNext());
        assertFalse(IteratorUtils.EMPTY_ORDERED_ITERATOR.hasPrevious());

        assertNotNull(IteratorUtils.EMPTY_MAP_ITERATOR);
        assertFalse(IteratorUtils.EMPTY_MAP_ITERATOR.hasNext());

        assertNotNull(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR);
        assertFalse(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.hasNext());
        assertFalse(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.hasPrevious());

        final ResettableIterator<Object> it = IteratorUtils.emptyIterator();
        assertFalse(it.hasNext());
        it.reset();
        assertFalse(it.hasNext());

        final ResettableListIterator<Object> lit = IteratorUtils.emptyListIterator();
        assertFalse(lit.hasNext());
        assertFalse(lit.hasPrevious());
        lit.reset();

        final OrderedIterator<Object> oit = IteratorUtils.emptyOrderedIterator();
        assertFalse(oit.hasNext());
        assertFalse(oit.hasPrevious());

        final MapIterator<Object, Object> mit = IteratorUtils.emptyMapIterator();
        assertFalse(mit.hasNext());

        final OrderedMapIterator<Object, Object> omit = IteratorUtils.emptyOrderedMapIterator();
        assertFalse(omit.hasNext());
        assertFalse(omit.hasPrevious());
    }

    @Test
    public void testSingletonIterators() {
        final ResettableIterator<String> sit = IteratorUtils.singletonIterator("test");
        assertTrue(sit.hasNext());
        assertEquals("test", sit.next());
        assertFalse(sit.hasNext());
        sit.reset();
        assertTrue(sit.hasNext());
        assertEquals("test", sit.next());

        final ListIterator<String> slit = IteratorUtils.singletonListIterator("testList");
        assertTrue(slit.hasNext());
        assertFalse(slit.hasPrevious());
        assertEquals("testList", slit.next());
        assertTrue(slit.hasPrevious());
        assertEquals("testList", slit.previous());
    }

    @Test
    public void testArrayIteratorObjectArray() {
        final String[] array = new String[]{"a", "b", "c"};
        ResettableIterator<String> it = IteratorUtils.arrayIterator(array);
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayIterator(array, 1);
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayIterator(array, 1, 2);
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testArrayIteratorNullVarargs() {
        IteratorUtils.arrayIterator((Object[]) null);
    }

    @Test
    public void testArrayIteratorPrimitiveAndGenericObject() {
        final int[] primitiveArray = new int[]{1, 2, 3};
        ResettableIterator<Integer> it = IteratorUtils.arrayIterator((Object) primitiveArray);
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayIterator((Object) primitiveArray, 1);
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(3), it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayIterator((Object) primitiveArray, 0, 2);
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayIteratorNotAnArray() {
        IteratorUtils.arrayIterator("NotAnArray");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayIteratorNotAnArrayWithStart() {
        IteratorUtils.arrayIterator("NotAnArray", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayIteratorNotAnArrayWithStartEnd() {
        IteratorUtils.arrayIterator("NotAnArray", 0, 1);
    }

    @Test
    public void testArrayListIteratorObjectArray() {
        final String[] array = new String[]{"x", "y", "z"};
        ResettableListIterator<String> lit = IteratorUtils.arrayListIterator(array);
        assertEquals("x", lit.next());
        assertEquals("y", lit.next());
        assertEquals("z", lit.next());
        assertFalse(lit.hasNext());

        lit = IteratorUtils.arrayListIterator(array, 1);
        assertEquals("y", lit.next());
        assertEquals("z", lit.next());
        assertFalse(lit.hasNext());

        lit = IteratorUtils.arrayListIterator(array, 1, 2);
        assertEquals("y", lit.next());
        assertFalse(lit.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testArrayListIteratorNullVarargs() {
        IteratorUtils.arrayListIterator((Object[]) null);
    }

    @Test
    public void testArrayListIteratorPrimitiveAndGenericObject() {
        final double[] doubleArray = new double[]{1.1, 2.2, 3.3};
        ResettableListIterator<Double> lit = IteratorUtils.arrayListIterator((Object) doubleArray);
        assertEquals(Double.valueOf(1.1), lit.next());
        assertEquals(Double.valueOf(2.2), lit.next());
        assertEquals(Double.valueOf(3.3), lit.next());
        assertFalse(lit.hasNext());

        lit = IteratorUtils.arrayListIterator((Object) doubleArray, 1);
        assertEquals(Double.valueOf(2.2), lit.next());
        assertEquals(Double.valueOf(3.3), lit.next());
        assertFalse(lit.hasNext());

        lit = IteratorUtils.arrayListIterator((Object) doubleArray, 0, 1);
        assertEquals(Double.valueOf(1.1), lit.next());
        assertFalse(lit.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayListIteratorNotAnArray() {
        IteratorUtils.arrayListIterator(123);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayListIteratorNotAnArrayWithStart() {
        IteratorUtils.arrayListIterator(123, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayListIteratorNotAnArrayWithStartEnd() {
        IteratorUtils.arrayListIterator(123, 0, 1);
    }

    @Test
    public void testBoundedIterator() {
        final List<String> list = Arrays.asList("a", "b", "c", "d");
        BoundedIterator<String> bIt = IteratorUtils.boundedIterator(list.iterator(), 2);
        assertEquals(Arrays.asList("a", "b"), IteratorUtils.toList(bIt));

        bIt = IteratorUtils.boundedIterator(list.iterator(), 1, 2);
        assertEquals(Arrays.asList("b", "c"), IteratorUtils.toList(bIt));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBoundedIteratorNullIterator() {
        IteratorUtils.boundedIterator(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBoundedIteratorNegativeMax() {
        IteratorUtils.boundedIterator(Collections.emptyIterator(), -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBoundedIteratorNegativeOffset() {
        IteratorUtils.boundedIterator(Collections.emptyIterator(), -1, 2);
    }

    @Test
    public void testUnmodifiableIterator() {
        final List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        final Iterator<String> unmodifiable = IteratorUtils.unmodifiableIterator(list.iterator());
        assertTrue(unmodifiable.hasNext());
        assertEquals("a", unmodifiable.next());
        try {
            unmodifiable.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableIteratorNull() {
        IteratorUtils.unmodifiableIterator(null);
    }

    @Test
    public void testUnmodifiableListIterator() {
        final List<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        final ListIterator<String> unmodifiable = IteratorUtils.unmodifiableListIterator(list.listIterator());
        assertTrue(unmodifiable.hasNext());
        assertEquals("a", unmodifiable.next());
        try {
            unmodifiable.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException ignored) {
        }
        try {
            unmodifiable.set("c");
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException ignored) {
        }
        try {
            unmodifiable.add("d");
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableListIteratorNull() {
        IteratorUtils.unmodifiableListIterator(null);
    }

    @Test
    public void testUnmodifiableMapIterator() {
        final IterableMap<String, String> map = new HashedMap<>();
        map.put("k1", "v1");
        final MapIterator<String, String> mapIt = IteratorUtils.unmodifiableMapIterator(map.mapIterator());
        assertTrue(mapIt.hasNext());
        assertEquals("k1", mapIt.next());
        try {
            mapIt.remove();
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException ignored) {
        }
        try {
            mapIt.setValue("v2");
            fail("Expected UnsupportedOperationException");
        } catch (final UnsupportedOperationException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmodifiableMapIteratorNull() {
        IteratorUtils.unmodifiableMapIterator(null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testChainedIterator() {
        final Iterator<String> it1 = Arrays.asList("1", "2").iterator();
        final Iterator<String> it2 = Arrays.asList("3", "4").iterator();
        final Iterator<String> it3 = Arrays.asList("5", "6").iterator();

        Iterator<String> chained = IteratorUtils.chainedIterator(it1, it2);
        assertEquals(Arrays.asList("1", "2", "3", "4"), IteratorUtils.toList(chained));

        chained = IteratorUtils.chainedIterator(Arrays.asList("a").iterator(), Arrays.asList("b").iterator(), Arrays.asList("c").iterator());
        assertEquals(Arrays.asList("a", "b", "c"), IteratorUtils.toList(chained));

        final List<Iterator<? extends String>> list = Arrays.asList(
                Arrays.asList("x").iterator(),
                Arrays.asList("y").iterator()
        );
        chained = IteratorUtils.chainedIterator(list);
        assertEquals(Arrays.asList("x", "y"), IteratorUtils.toList(chained));
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorNullFirst() {
        IteratorUtils.chainedIterator(null, Collections.emptyIterator());
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorNullSecond() {
        IteratorUtils.chainedIterator(Collections.emptyIterator(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorNullVarargs() {
        IteratorUtils.chainedIterator((Iterator<?>[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testChainedIteratorNullCollection() {
        IteratorUtils.chainedIterator((Collection<Iterator<?>>) null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCollatedIterator() {
        final Comparator<Integer> comp = Comparator.naturalOrder();
        final Iterator<Integer> it1 = Arrays.asList(1, 3, 5).iterator();
        final Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();

        Iterator<Integer> collated = IteratorUtils.collatedIterator(comp, it1, it2);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), IteratorUtils.toList(collated));

        final Iterator<Integer> it3 = Arrays.asList(1, 4).iterator();
        final Iterator<Integer> it4 = Arrays.asList(2, 5).iterator();
        final Iterator<Integer> it5 = Arrays.asList(3, 6).iterator();
        collated = IteratorUtils.collatedIterator(null, it3, it4, it5);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), IteratorUtils.toList(collated));

        final List<Iterator<? extends Integer>> list = Arrays.asList(
                Arrays.asList(10, 30).iterator(),
                Arrays.asList(20, 40).iterator()
        );
        collated = IteratorUtils.collatedIterator(comp, list);
        assertEquals(Arrays.asList(10, 20, 30, 40), IteratorUtils.toList(collated));
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorNullFirst() {
        IteratorUtils.collatedIterator(null, null, Collections.emptyIterator());
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorNullSecond() {
        IteratorUtils.collatedIterator(null, Collections.emptyIterator(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorNullArray() {
        IteratorUtils.collatedIterator(null, (Iterator<?>[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testCollatedIteratorNullCollection() {
        IteratorUtils.collatedIterator(null, (Collection<Iterator<?>>) null);
    }

    @Test
    public void testObjectGraphIterator() {
        final List<List<String>> graph = Arrays.asList(
                Arrays.asList("a", "b"),
                Arrays.asList("c", "d")
        );
        final Transformer<Object, Object> transformer = input -> {
            if (input instanceof List) {
                return ((List<?>) input).iterator();
            }
            return input;
        };

        final Iterator<Object> it = IteratorUtils.objectGraphIterator(graph, transformer);
        assertEquals(Arrays.asList("a", "b", "c", "d"), IteratorUtils.toList(it));

        final Iterator<Object> nullRootIt = IteratorUtils.objectGraphIterator(null, transformer);
        assertFalse(nullRootIt.hasNext());
    }

    @Test
    public void testTransformedIterator() {
        final List<Integer> list = Arrays.asList(1, 2, 3);
        final Transformer<Integer, String> transformer = String::valueOf;
        final Iterator<String> transformed = IteratorUtils.transformedIterator(list.iterator(), transformer);
        assertEquals(Arrays.asList("1", "2", "3"), IteratorUtils.toList(transformed));
    }

    @Test(expected = NullPointerException.class)
    public void testTransformedIteratorNullIterator() {
        IteratorUtils.transformedIterator(null, TransformerUtils.nopTransformer());
    }

    @Test(expected = NullPointerException.class)
    public void testTransformedIteratorNullTransformer() {
        IteratorUtils.transformedIterator(Collections.emptyIterator(), null);
    }

    @Test
    public void testFilteredIterator() {
        final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        final Predicate<Integer> predicate = value -> value % 2 == 0;
        final Iterator<Integer> filtered = IteratorUtils.filteredIterator(list.iterator(), predicate);
        assertEquals(Arrays.asList(2, 4), IteratorUtils.toList(filtered));
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredIteratorNullIterator() {
        IteratorUtils.filteredIterator(null, TruePredicate.truePredicate());
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredIteratorNullPredicate() {
        IteratorUtils.filteredIterator(Collections.emptyIterator(), null);
    }

    @Test
    public void testFilteredListIterator() {
        final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        final Predicate<Integer> predicate = value -> value % 2 != 0;
        final ListIterator<Integer> filtered = IteratorUtils.filteredListIterator(list.listIterator(), predicate);
        assertEquals(Arrays.asList(1, 3, 5), IteratorUtils.toList(filtered));
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredListIteratorNullIterator() {
        IteratorUtils.filteredListIterator(null, TruePredicate.truePredicate());
    }

    @Test(expected = NullPointerException.class)
    public void testFilteredListIteratorNullPredicate() {
        IteratorUtils.filteredListIterator(Collections.emptyList().listIterator(), null);
    }

    @Test
    public void testLoopingIterator() {
        final List<String> list = Arrays.asList("a", "b");
        final ResettableIterator<String> looping = IteratorUtils.loopingIterator(list);
        assertTrue(looping instanceof LoopingIterator);
        assertEquals("a", looping.next());
        assertEquals("b", looping.next());
        assertEquals("a", looping.next());
        assertEquals("b", looping.next());
    }

    @Test(expected = NullPointerException.class)
    public void testLoopingIteratorNull() {
        IteratorUtils.loopingIterator(null);
    }

    @Test
    public void testLoopingListIterator() {
        final List<String> list = Arrays.asList("a", "b");
        final ResettableListIterator<String> looping = IteratorUtils.loopingListIterator(list);
        assertTrue(looping instanceof LoopingListIterator);
        assertEquals("a", looping.next());
        assertEquals("b", looping.next());
        assertEquals("a", looping.next());
    }

    @Test(expected = NullPointerException.class)
    public void testLoopingListIteratorNull() {
        IteratorUtils.loopingListIterator(null);
    }

    @Test
    public void testNodeListIterator() {
        final NodeList nodeList = new NodeList() {
            private final List<Node> nodes = new ArrayList<>();

            @Override
            public Node item(final int index) {
                if (index < 0 || index >= nodes.size()) {
                    return null;
                }
                return nodes.get(index);
            }

            @Override
            public int getLength() {
                return nodes.size();
            }
        };

        final NodeListIterator it = IteratorUtils.nodeListIterator(nodeList);
        assertNotNull(it);
        assertFalse(it.hasNext());

        final Node node = new org.w3c.dom.Element() {
            @Override public String getTagName() { return "tag"; }
            @Override public String getAttribute(String name) { return ""; }
            @Override public void setAttribute(String name, String value) {}
            @Override public void removeAttribute(String name) {}
            @Override public org.w3c.dom.Attr getAttributeNode(String name) { return null; }
            @Override public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr newAttr) { return null; }
            @Override public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr oldAttr) { return null; }
            @Override public NodeList getElementsByTagName(String name) { return null; }
            @Override public String getAttributeNS(String namespaceURI, String localName) { return ""; }
            @Override public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {}
            @Override public void removeAttributeNS(String namespaceURI, String localName) {}
            @Override public org.w3c.dom.Attr getAttributeNodeNS(String namespaceURI, String localName) { return null; }
            @Override public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr newAttr) { return null; }
            @Override public NodeList getElementsByTagNameNS(String namespaceURI, String localName) { return null; }
            @Override public boolean hasAttribute(String name) { return false; }
            @Override public boolean hasAttributeNS(String namespaceURI, String localName) { return false; }
            @Override public org.w3c.dom.TypeInfo getSchemaTypeInfo() { return null; }
            @Override public void setIdAttribute(String name, boolean isId) {}
            @Override public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) {}
            @Override public void setIdAttributeNode(org.w3c.dom.Attr idAttr, boolean isId) {}
            @Override public String getNodeName() { return "node"; }
            @Override public String getNodeValue() { return null; }
            @Override public void setNodeValue(String nodeValue) {}
            @Override public short getNodeType() { return Node.ELEMENT_NODE; }
            @Override public Node getParentNode() { return null; }
            @Override public NodeList getChildNodes() { return nodeList; }
            @Override public Node getFirstChild() { return null; }
            @Override public Node getLastChild() { return null; }
            @Override public Node getPreviousSibling() { return null; }
            @Override public Node getNextSibling() { return null; }
            @Override public org.w3c.dom.NamedNodeMap getAttributes() { return null; }
            @Override public org.w3c.dom.Document getOwnerDocument() { return null; }
            @Override public Node insertBefore(Node newChild, Node refChild) { return null; }
            @Override public Node replaceChild(Node newChild, Node oldChild) { return null; }
            @Override public Node removeChild(Node oldChild) { return null; }
            @Override public Node appendChild(Node newChild) { return null; }
            @Override public boolean hasChildNodes() { return false; }
            @Override public Node cloneNode(boolean deep) { return null; }
            @Override public void normalize() {}
            @Override public boolean isSupported(String feature, String version) { return false; }
            @Override public String getNamespaceURI() { return null; }
            @Override public String getPrefix() { return null; }
            @Override public void setPrefix(String prefix) {}
            @Override public String getLocalName() { return null; }
            @Override public boolean hasAttributes() { return false; }
            @Override public String getBaseURI() { return null; }
            @Override public short compareDocumentPosition(Node other) { return 0; }
            @Override public String getTextContent() { return null; }
            @Override public void setTextContent(String textContent) {}
            @Override public boolean isSameNode(Node other) { return false; }
            @Override public String lookupPrefix(String namespaceURI) { return null; }
            @Override public boolean isDefaultNamespace(String namespaceURI) { return false; }
            @Override public String lookupNamespaceURI(String prefix) { return null; }
            @Override public boolean isEqualNode(Node arg) { return false; }
            @Override public Object getFeature(String feature, String version) { return null; }
            @Override public Object setUserData(String key, Object data, org.w3c.dom.UserDataHandler handler) { return null; }
            @Override public Object getUserData(String key) { return null; }
        };

        final NodeListIterator nodeIt = IteratorUtils.nodeListIterator(node);
        assertNotNull(nodeIt);
        assertFalse(nodeIt.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testNodeListIteratorNullNodeList() {
        IteratorUtils.nodeListIterator((NodeList) null);
    }

    @Test(expected = NullPointerException.class)
    public void testNodeListIteratorNullNode() {
        IteratorUtils.nodeListIterator((Node) null);
    }

    @Test
    public void testPeekingIterator() {
        final List<String> list = Arrays.asList("a", "b");
        final Iterator<String> peeking = IteratorUtils.peekingIterator(list.iterator());
        assertTrue(peeking instanceof PeekingIterator);
        final PeekingIterator<String> pIt = (PeekingIterator<String>) peeking;
        assertEquals("a", pIt.peek());
        assertEquals("a", pIt.next());
        assertEquals("b", pIt.peek());
        assertEquals("b", pIt.next());
        assertFalse(pIt.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testPeekingIteratorNull() {
        IteratorUtils.peekingIterator(null);
    }

    @Test
    public void testPushbackIterator() {
        final List<String> list = Arrays.asList("a", "b");
        final Iterator<String> pushback = IteratorUtils.pushbackIterator(list.iterator());
        assertTrue(pushback instanceof PushbackIterator);
        final PushbackIterator<String> pbIt = (PushbackIterator<String>) pushback;
        assertEquals("a", pbIt.next());
        pbIt.pushback("x");
        assertEquals("x", pbIt.next());
        assertEquals("b", pbIt.next());
        assertFalse(pbIt.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testPushbackIteratorNull() {
        IteratorUtils.pushbackIterator(null);
    }

    @Test
    public void testSkippingIterator() {
        final List<String> list = Arrays.asList("a", "b", "c", "d");
        final SkippingIterator<String> skipping = IteratorUtils.skippingIterator(list.iterator(), 2);
        assertEquals(Arrays.asList("c", "d"), IteratorUtils.toList(skipping));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSkippingIteratorNegativeOffset() {
        IteratorUtils.skippingIterator(Collections.emptyIterator(), -1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testZippingIterator() {
        final Iterator<String> it1 = Arrays.asList("a", "c").iterator();
        final Iterator<String> it2 = Arrays.asList("b", "d").iterator();
        final ZippingIterator<String> zip2 = IteratorUtils.zippingIterator(it1, it2);
        assertEquals(Arrays.asList("a", "b", "c", "d"), IteratorUtils.toList(zip2));

        final Iterator<String> it3 = Arrays.asList("1").iterator();
        final Iterator<String> it4 = Arrays.asList("2").iterator();
        final Iterator<String> it5 = Arrays.asList("3").iterator();
        final ZippingIterator<String> zip3 = IteratorUtils.zippingIterator(it3, it4, it5);
        assertEquals(Arrays.asList("1", "2", "3"), IteratorUtils.toList(zip3));

        final ZippingIterator<String> zipVarargs = IteratorUtils.zippingIterator(
                Arrays.asList("x").iterator(),
                Arrays.asList("y").iterator()
        );
        assertEquals(Arrays.asList("x", "y"), IteratorUtils.toList(zipVarargs));
    }

    @Test
    public void testAsIteratorAndAsEnumeration() {
        final Vector<String> vector = new Vector<>(Arrays.asList("a", "b"));
        final Iterator<String> it = IteratorUtils.asIterator(vector.elements());
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());

        final List<String> removeList = new ArrayList<>(Arrays.asList("x", "y"));
        final Vector<String> vector2 = new Vector<>(Arrays.asList("x", "y"));
        final Iterator<String> it2 = IteratorUtils.asIterator(vector2.elements(), removeList);
        assertEquals("x", it2.next());
        it2.remove();
        assertEquals(Collections.singletonList("y"), removeList);

        final Enumeration<String> en = IteratorUtils.asEnumeration(Arrays.asList("1", "2").iterator());
        assertTrue(en.hasMoreElements());
        assertEquals("1", en.nextElement());
        assertEquals("2", en.nextElement());
        assertFalse(en.hasMoreElements());
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorNullEnumeration() {
        IteratorUtils.asIterator(null);
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorNullEnumerationWithCollection() {
        IteratorUtils.asIterator(null, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorNullCollection() {
        IteratorUtils.asIterator(new Vector<>().elements(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testAsEnumerationNull() {
        IteratorUtils.asEnumeration(null);
    }

    @Test
    public void testAsIterable() {
        final List<String> list = Arrays.asList("a", "b");
        final Iterable<String> singleUse = IteratorUtils.asIterable(list.iterator());
        final List<String> result = new ArrayList<>();
        for (final String s : singleUse) {
            result.add(s);
        }
        assertEquals(list, result);

        final Iterable<String> multiUse = IteratorUtils.asMultipleUseIterable(list.iterator());
        final List<String> result1 = new ArrayList<>();
        for (final String s : multiUse) {
            result1.add(s);
        }
        assertEquals(list, result1);
        final List<String> result2 = new ArrayList<>();
        for (final String s : multiUse) {
            result2.add(s);
        }
        assertEquals(list, result2);
    }

    @Test(expected = NullPointerException.class)
    public void testAsIterableNull() {
        IteratorUtils.asIterable(null);
    }

    @Test(expected = NullPointerException.class)
    public void testAsMultipleUseIterableNull() {
        IteratorUtils.asMultipleUseIterable(null);
    }

    @Test
    public void testToListIterator() {
        final Iterator<String> it = Arrays.asList("a", "b").iterator();
        final ListIterator<String> lit = IteratorUtils.toListIterator(it);
        assertTrue(lit.hasNext());
        assertEquals("a", lit.next());
        assertTrue(lit.hasPrevious());
        assertEquals("a", lit.previous());
        assertEquals("a", lit.next());
        assertEquals("b", lit.next());
        assertFalse(lit.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void testToListIteratorNull() {
        IteratorUtils.toListIterator(null);
    }

    @Test
    public void testToArray() {
        final List<String> list = Arrays.asList("a", "b", "c");
        final Object[] objArray = IteratorUtils.toArray(list.iterator());
        assertArrayEquals(new Object[]{"a", "b", "c"}, objArray);

        final String[] strArray = IteratorUtils.toArray(list.iterator(), String.class);
        assertArrayEquals(new String[]{"a", "b", "c"}, strArray);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayNullIterator() {
        IteratorUtils.toArray(null);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayTypedNullIterator() {
        IteratorUtils.toArray(null, String.class);
    }

    @Test(expected = NullPointerException.class)
    public void testToArrayTypedNullClass() {
        IteratorUtils.toArray(Collections.emptyIterator(), null);
    }

    @Test
    public void testToList() {
        final List<String> list = Arrays.asList("a", "b", "c");
        assertEquals(list, IteratorUtils.toList(list.iterator()));
        assertEquals(list, IteratorUtils.toList(list.iterator(), 20));
    }

    @Test(expected = NullPointerException.class)
    public void testToListNullIterator() {
        IteratorUtils.toList(null);
    }

    @Test(expected = NullPointerException.class)
    public void testToListWithCapacityNullIterator() {
        IteratorUtils.toList(null, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToListInvalidCapacity() {
        IteratorUtils.toList(Collections.emptyIterator(), 0);
    }

    public static class CustomIterableWithIteratorMethod {
        public Iterator<String> iterator() {
            return Arrays.asList("custom1", "custom2").iterator();
        }
    }

    public static class CustomThrowsIteratorMethod {
        public Iterator<String> iterator() {
            throw new RuntimeException("Iterator error");
        }
    }

    public static class CustomReturnsNullIteratorMethod {
        public Iterator<String> iterator() {
            return null;
        }
    }

    @Test
    public void testGetIterator() {
        // null
        final Iterator<?> nullIt = IteratorUtils.getIterator(null);
        assertFalse(nullIt.hasNext());

        // Iterator
        final Iterator<String> rawIt = Arrays.asList("1").iterator();
        assertSame(rawIt, IteratorUtils.getIterator(rawIt));

        // Iterable
        final List<String> list = Arrays.asList("a", "b");
        final Iterator<?> listIt = IteratorUtils.getIterator(list);
        assertEquals(Arrays.asList("a", "b"), IteratorUtils.toList(listIt));

        // Object[]
        final String[] strArr = new String[]{"x", "y"};
        final Iterator<?> arrIt = IteratorUtils.getIterator(strArr);
        assertEquals(Arrays.asList("x", "y"), IteratorUtils.toList(arrIt));

        // Enumeration
        final Vector<String> vec = new Vector<>(Arrays.asList("e1", "e2"));
        final Iterator<?> enumIt = IteratorUtils.getIterator(vec.elements());
        assertEquals(Arrays.asList("e1", "e2"), IteratorUtils.toList(enumIt));

        // Map
        final Map<String, String> map = new java.util.LinkedHashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        final Iterator<?> mapIt = IteratorUtils.getIterator(map);
        assertEquals(Arrays.asList("v1", "v2"), IteratorUtils.toList(mapIt));

        // Dictionary
        final Hashtable<String, String> dict = new Hashtable<>();
        dict.put("d1", "val1");
        final Iterator<?> dictIt = IteratorUtils.getIterator(dict);
        assertEquals(Collections.singletonList("val1"), IteratorUtils.toList(dictIt));

        // Primitive array
        final int[] intArr = new int[]{10, 20};
        final Iterator<?> primArrIt = IteratorUtils.getIterator(intArr);
        assertEquals(Arrays.asList(10, 20), IteratorUtils.toList(primArrIt));

        // NodeList
        final NodeList mockNodeList = new NodeList() {
            @Override public Node item(int index) { return null; }
            @Override public int getLength() { return 0; }
        };
        final Iterator<?> nlIt = IteratorUtils.getIterator(mockNodeList);
        assertTrue(nlIt instanceof NodeListIterator);

        // Node
        final Node mockNode = new NodeListIteratorTestNode(mockNodeList);
        final Iterator<?> nodeIt = IteratorUtils.getIterator(mockNode);
        assertTrue(nodeIt instanceof NodeListIterator);

        // Reflection iterator() method
        final Iterator<?> customIt = IteratorUtils.getIterator(new CustomIterableWithIteratorMethod());
        assertEquals(Arrays.asList("custom1", "custom2"), IteratorUtils.toList(customIt));

        // Reflection iterator() method throwing RuntimeException -> falls back to singletonIterator
        final Object customThrows = new CustomThrowsIteratorMethod();
        final Iterator<?> customThrowsIt = IteratorUtils.getIterator(customThrows);
        assertEquals(Collections.singletonList(customThrows), IteratorUtils.toList(customThrowsIt));

        // Reflection iterator() method returning null -> falls back to singletonIterator
        final Object customNull = new CustomReturnsNullIteratorMethod();
        final Iterator<?> customNullIt = IteratorUtils.getIterator(customNull);
        assertEquals(Collections.singletonList(customNull), IteratorUtils.toList(customNullIt));

        // Simple Object fallback -> singletonIterator
        final Object singleObj = new Object();
        final Iterator<?> singleIt = IteratorUtils.getIterator(singleObj);
        assertEquals(Collections.singletonList(singleObj), IteratorUtils.toList(singleIt));
    }

    private static class NodeListIteratorTestNode implements Node {
        private final NodeList childNodes;
        NodeListIteratorTestNode(NodeList childNodes) { this.childNodes = childNodes; }
        @Override public String getNodeName() { return null; }
        @Override public String getNodeValue() { return null; }
        @Override public void setNodeValue(String nodeValue) {}
        @Override public short getNodeType() { return 0; }
        @Override public Node getParentNode() { return null; }
        @Override public NodeList getChildNodes() { return childNodes; }
        @Override public Node getFirstChild() { return null; }
        @Override public Node getLastChild() { return null; }
        @Override public Node getPreviousSibling() { return null; }
        @Override public Node getNextSibling() { return null; }
        @Override public org.w3c.dom.NamedNodeMap getAttributes() { return null; }
        @Override public org.w3c.dom.Document getOwnerDocument() { return null; }
        @Override public Node insertBefore(Node newChild, Node refChild) { return null; }
        @Override public Node replaceChild(Node newChild, Node oldChild) { return null; }
        @Override public Node removeChild(Node oldChild) { return null; }
        @Override public Node appendChild(Node newChild) { return null; }
        @Override public boolean hasChildNodes() { return false; }
        @Override public Node cloneNode(boolean deep) { return null; }
        @Override public void normalize() {}
        @Override public boolean isSupported(String feature, String version) { return false; }
        @Override public String getNamespaceURI() { return null; }
        @Override public String getPrefix() { return null; }
        @Override public void setPrefix(String prefix) {}
        @Override public String getLocalName() { return null; }
        @Override public boolean hasAttributes() { return false; }
        @Override public String getBaseURI() { return null; }
        @Override public short compareDocumentPosition(Node other) { return 0; }
        @Override public String getTextContent() { return null; }
        @Override public void setTextContent(String textContent) {}
        @Override public boolean isSameNode(Node other) { return false; }
        @Override public String lookupPrefix(String namespaceURI) { return null; }
        @Override public boolean isDefaultNamespace(String namespaceURI) { return false; }
        @Override public String lookupNamespaceURI(String prefix) { return null; }
        @Override public boolean isEqualNode(Node arg) { return false; }
        @Override public Object getFeature(String feature, String version) { return null; }
        @Override public Object setUserData(String key, Object data, org.w3c.dom.UserDataHandler handler) { return null; }
        @Override public Object getUserData(String key) { return null; }
    }

    @Test
    public void testApply() {
        final List<String> list = Arrays.asList("a", "b", "c");
        final List<String> output = new ArrayList<>();
        final Closure<String> closure = output::add;

        IteratorUtils.apply(list.iterator(), closure);
        assertEquals(list, output);

        IteratorUtils.apply(null, closure); // Should be a no-op
    }

    @Test(expected = NullPointerException.class)
    public void testApplyNullClosure() {
        IteratorUtils.apply(Collections.emptyIterator(), null);
    }

    @Test
    public void testFind() {
        final List<String> list = Arrays.asList("apple", "banana", "cherry");
        final Predicate<String> startsWithB = s -> s.startsWith("b");
        final Predicate<String> startsWithZ = s -> s.startsWith("z");

        assertEquals("banana", IteratorUtils.find(list.iterator(), startsWithB));
        assertNull(IteratorUtils.find(list.iterator(), startsWithZ));
        assertNull(IteratorUtils.find(null, startsWithB));
        assertNull(IteratorUtils.find(Collections.emptyIterator(), startsWithB));
    }

    @Test(expected = NullPointerException.class)
    public void testFindNullPredicate() {
        IteratorUtils.find(Collections.emptyIterator(), null);
    }

    @Test
    public void testMatchesAny() {
        final List<Integer> list = Arrays.asList(1, 2, 3);
        assertTrue(IteratorUtils.matchesAny(list.iterator(), EqualPredicate.equalPredicate(2)));
        assertFalse(IteratorUtils.matchesAny(list.iterator(), EqualPredicate.equalPredicate(5)));
        assertFalse(IteratorUtils.matchesAny(null, TruePredicate.truePredicate()));
        assertFalse(IteratorUtils.matchesAny(Collections.emptyIterator(), TruePredicate.truePredicate()));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesAnyNullPredicate() {
        IteratorUtils.matchesAny(Collections.emptyIterator(), null);
    }

    @Test
    public void testMatchesAll() {
        final List<Integer> list = Arrays.asList(2, 4, 6);
        final Predicate<Integer> isEven = val -> val % 2 == 0;
        assertTrue(IteratorUtils.matchesAll(list.iterator(), isEven));
        assertFalse(IteratorUtils.matchesAll(Arrays.asList(2, 3, 4).iterator(), isEven));
        assertTrue(IteratorUtils.matchesAll(null, NotNullPredicate.notNullPredicate()));
        assertTrue(IteratorUtils.matchesAll(Collections.emptyIterator(), NotNullPredicate.notNullPredicate()));
    }

    @Test(expected = NullPointerException.class)
    public void testMatchesAllNullPredicate() {
        IteratorUtils.matchesAll(Collections.emptyIterator(), null);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(IteratorUtils.isEmpty(null));
        assertTrue(IteratorUtils.isEmpty(Collections.emptyIterator()));
        assertFalse(IteratorUtils.isEmpty(Arrays.asList("a").iterator()));
    }

    @Test
    public void testContains() {
        final List<String> list = Arrays.asList("x", "y", "z");
        assertTrue(IteratorUtils.contains(list.iterator(), "y"));
        assertFalse(IteratorUtils.contains(list.iterator(), "w"));
        assertFalse(IteratorUtils.contains(null, "x"));
        assertFalse(IteratorUtils.contains(Collections.emptyIterator(), "x"));
    }

    @Test
    public void testGet() {
        final List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a", IteratorUtils.get(list.iterator(), 0));
        assertEquals("b", IteratorUtils.get(list.iterator(), 1));
        assertEquals("c", IteratorUtils.get(list.iterator(), 2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndex() {
        IteratorUtils.get(Arrays.asList("a").iterator(), -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexOutOfBounds() {
        IteratorUtils.get(Arrays.asList("a").iterator(), 5);
    }

    @Test
    public void testSize() {
        assertEquals(0, IteratorUtils.size(null));
        assertEquals(0, IteratorUtils.size(Collections.emptyIterator()));
        assertEquals(3, IteratorUtils.size(Arrays.asList("a", "b", "c").iterator()));
    }

    @Test
    public void testToStringDefault() {
        assertEquals("[]", IteratorUtils.toString(null));
        assertEquals("[]", IteratorUtils.toString(Collections.emptyIterator()));
        assertEquals("[a, b, c]", IteratorUtils.toString(Arrays.asList("a", "b", "c").iterator()));
    }

    @Test
    public void testToStringWithTransformer() {
        final Transformer<Integer, String> hexTransformer = i -> "0x" + Integer.toHexString(i);
        assertEquals("[]", IteratorUtils.toString(null, hexTransformer));
        assertEquals("[]", IteratorUtils.toString(Collections.<Integer>emptyIterator(), hexTransformer));
        assertEquals("[0xa, 0xb]", IteratorUtils.toString(Arrays.asList(10, 11).iterator(), hexTransformer));
    }

    @Test
    public void testToStringCustomFormat() {
        final Transformer<String, String> upperTransformer = String::toUpperCase;
        assertEquals("<<A|B|C>>", IteratorUtils.toString(Arrays.asList("a", "b", "c").iterator(), upperTransformer, "|", "<<", ">>"));
        assertEquals("<<>>", IteratorUtils.toString(Collections.<String>emptyIterator(), upperTransformer, "|", "<<", ">>"));
        assertEquals("<<>>", IteratorUtils.toString(null, upperTransformer, "|", "<<", ">>"));
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullTransformer() {
        IteratorUtils.toString(Collections.emptyIterator(), null, ",", "[", "]");
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullDelimiter() {
        IteratorUtils.toString(Collections.emptyIterator(), TransformerUtils.stringValueTransformer(), null, "[", "]");
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullPrefix() {
        IteratorUtils.toString(Collections.emptyIterator(), TransformerUtils.stringValueTransformer(), ",", null, "]");
    }

    @Test(expected = NullPointerException.class)
    public void testToStringNullSuffix() {
        IteratorUtils.toString(Collections.emptyIterator(), TransformerUtils.stringValueTransformer(), ",", "[", null);
    }
}
