package org.apache.commons.jxpath.ri.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.util.ValueUtils;

public class ExpressionTest {

    // Concrete subclass for testing abstract Expression
    private static class TestExpression extends Expression {
        private boolean contextDependent;
        private int computeContextDependentCallCount = 0;
        private Object computeValueResult;
        private Object computeResult;

        public TestExpression(boolean contextDependent) {
            this.contextDependent = contextDependent;
        }

        @Override
        public boolean computeContextDependent() {
            computeContextDependentCallCount++;
            return contextDependent;
        }

        @Override
        public Object computeValue(EvalContext context) {
            return computeValueResult;
        }

        @Override
        public Object compute(EvalContext context) {
            return computeResult;
        }

        public void setComputeResult(Object result) {
            this.computeResult = result;
        }

        public int getComputeContextDependentCallCount() {
            return computeContextDependentCallCount;
        }
    }

    // Mock EvalContext that also implements Iterator for testing iterate()
    private static class MockEvalContext extends EvalContext implements Iterator {
        private List<Object> items;
        private int index = 0;

        public MockEvalContext(List<Object> items) {
            this.items = items;
        }

        @Override
        public boolean hasNext() {
            return index < items.size();
        }

        @Override
        public Object next() {
            return items.get(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        // Dummy implementations of abstract methods from EvalContext
        @Override
        public Pointer getContextNodePointer() { return null; }
        @Override
        public EvalContext getRootContext() { return this; }
        @Override
        public NodePointer getCurrentNodePointer() { return null; }
        @Override
        public void setPosition(int position) {}
        @Override
        public int getPosition() { return 0; }
        @Override
        public boolean nextSet() { return false; }
        @Override
        public boolean setPosition(int position, boolean absolute) { return false; }
        @Override
        public int getCurrentPosition() { return 0; }
        @Override
        public void reset() {}
    }

    // Mock Pointer for testing iterators
    private static class MockPointer implements Pointer {
        private Object value;
        public MockPointer(Object value) { this.value = value; }
        @Override
        public Object getValue() { return value; }
        @Override
        public void setValue(Object value) {}
        @Override
        public Object getNode() { return null; }
        @Override
        public String asPath() { return null; }
        @Override
        public Object clone() { return null; }
        @Override
        public int compareTo(Object o) { return 0; }
    }

    // Mock NodePointer for locale in iteratePointers
    private static class MockNodePointer extends NodePointer {
        private Locale locale;
        public MockNodePointer(Locale locale) { this.locale = locale; }
        @Override
        public Locale getLocale() { return locale; }
        // Dummy implementations
        @Override
        public boolean isLeaf() { return false; }
        @Override
        public boolean isCollection() { return false; }
        @Override
        public int getLength() { return 0; }
        @Override
        public QName getName() { return null; }
        @Override
        public Object getBaseValue() { return null; }
        @Override
        public Object getImmediateNode() { return null; }
        @Override
        public void setValue(Object value) {}
        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) { return 0; }
        @Override
        public NodePointer createChild(JXPathContext context, QName name, int index) { return null; }
        @Override
        public NodePointer createChild(JXPathContext context, QName name, int index, Object value) { return null; }
        @Override
        public NodePointer createAttribute(JXPathContext context, QName name) { return null; }
        @Override
        public String asPath() { return null; }
        @Override
        public Object clone() { return null; }
        @Override
        public int compareTo(Object o) { return 0; }
    }

    @Test
    public void testConstants() {
        assertEquals(0.0, Expression.ZERO.doubleValue(), 0.0);
        assertEquals(1.0, Expression.ONE.doubleValue(), 0.0);
        assertTrue(Expression.NOT_A_NUMBER.isNaN());
    }

    @Test
    public void testIsContextDependentCachingTrue() {
        TestExpression expr = new TestExpression(true);
        assertTrue(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());
        // second call should use cache
        assertTrue(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());
    }

    @Test
    public void testIsContextDependentCachingFalse() {
        TestExpression expr = new TestExpression(false);
        assertFalse(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());
        assertFalse(expr.isContextDependent());
        assertEquals(1, expr.getComputeContextDependentCallCount());
    }

    @Test
    public void testIterateWithEvalContext() {
        TestExpression expr = new TestExpression(false);
        List<Object> items = Arrays.asList("a", "b");
        MockEvalContext evalContext = new MockEvalContext(items);
        expr.setComputeResult(evalContext);
        Iterator it = expr.iterate(evalContext);
        assertTrue(it instanceof Expression.ValueIterator);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterateWithNonEvalContext() {
        TestExpression expr = new TestExpression(false);
        List<String> list = Arrays.asList("x", "y");
        expr.setComputeResult(list);
        Iterator it = expr.iterate(null);
        assertNotNull(it);
        // ValueUtils.iterate should return an iterator over the list
        assertTrue(it.hasNext());
        assertEquals("x", it.next());
        assertTrue(it.hasNext());
        assertEquals("y", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratePointersNull() {
        TestExpression expr = new TestExpression(false);
        expr.setComputeResult(null);
        Iterator it = expr.iteratePointers(null);
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratePointersEvalContext() {
        TestExpression expr = new TestExpression(false);
        MockEvalContext evalContext = new MockEvalContext(Collections.emptyList());
        expr.setComputeResult(evalContext);
        Iterator it = expr.iteratePointers(null);
        assertSame(evalContext, it);
    }

    @Test
    public void testIteratePointersOther() {
        TestExpression expr = new TestExpression(false);
        // Prepare a list with a Pointer and a non-Pointer
        Pointer mockPointer = new MockPointer("pointerValue");
        String nonPointer = "stringValue";
        List<Object> list = Arrays.asList(mockPointer, nonPointer);
        expr.setComputeResult(list);

        // Create a mock EvalContext that provides root context with locale
        final Locale testLocale = Locale.CANADA;
        MockNodePointer nodePointer = new MockNodePointer(testLocale);
        EvalContext context = new EvalContext() {
            @Override
            public EvalContext getRootContext() { return this; }
            @Override
            public NodePointer getCurrentNodePointer() { return nodePointer; }
            // Dummy implementations
            @Override
            public Pointer getContextNodePointer() { return null; }
            @Override
            public void setPosition(int position) {}
            @Override
            public int getPosition() { return 0; }
            @Override
            public boolean nextSet() { return false; }
            @Override
            public boolean setPosition(int position, boolean absolute) { return false; }
            @Override
            public int getCurrentPosition() { return 0; }
            @Override
            public void reset() {}
        };

        Iterator it = expr.iteratePointers(context);
        assertTrue(it instanceof Expression.PointerIterator);
        assertTrue(it.hasNext());
        Object first = it.next();
        // First element is a Pointer, should be returned as is
        assertSame(mockPointer, first);
        assertTrue(it.hasNext());
        Object second = it.next();
        // Second element is not a Pointer, should be wrapped by NodePointer.newNodePointer
        assertTrue(second instanceof Pointer);
        assertFalse(it.hasNext());
    }

    @Test
    public void testPointerIteratorHasNext() {
        Iterator inner = Arrays.asList("a").iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(inner, new QName(null, "value"), Locale.US);
        assertTrue(pi.hasNext());
        pi.next();
        assertFalse(pi.hasNext());
    }

    @Test
    public void testPointerIteratorNextPointer() {
        Pointer p = new MockPointer("val");
        Iterator inner = Arrays.asList(p).iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(inner, new QName(null, "value"), Locale.US);
        Object result = pi.next();
        assertSame(p, result);
    }

    @Test
    public void testPointerIteratorNextNonPointer() {
        String value = "test";
        Iterator inner = Arrays.asList(value).iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(inner, new QName(null, "value"), Locale.US);
        Object result = pi.next();
        assertTrue(result instanceof Pointer);
        // The value of the created pointer should be the original object
        assertEquals(value, ((Pointer)result).getValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPointerIteratorRemove() {
        Iterator inner = Arrays.asList("a").iterator();
        Expression.PointerIterator pi = new Expression.PointerIterator(inner, new QName(null, "value"), Locale.US);
        pi.remove();
    }

    @Test
    public void testValueIteratorHasNext() {
        Iterator inner = Arrays.asList("a").iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(inner);
        assertTrue(vi.hasNext());
        vi.next();
        assertFalse(vi.hasNext());
    }

    @Test
    public void testValueIteratorNextPointer() {
        Pointer p = new MockPointer("pointerValue");
        Iterator inner = Arrays.asList(p).iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(inner);
        Object result = vi.next();
        assertEquals("pointerValue", result);
    }

    @Test
    public void testValueIteratorNextNonPointer() {
        String value = "directValue";
        Iterator inner = Arrays.asList(value).iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(inner);
        Object result = vi.next();
        assertSame(value, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValueIteratorRemove() {
        Iterator inner = Arrays.asList("a").iterator();
        Expression.ValueIterator vi = new Expression.ValueIterator(inner);
        vi.remove();
    }
}
