package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.BasicVariables;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ExpressionTest {

    private static class TestExpression extends Expression {
        private boolean contextDependentResult = false;
        private int computeContextDependentCallCount = 0;
        private Object computeResult = null;
        private Object computeValueResult = null;

        public TestExpression(boolean contextDependent) {
            this.contextDependentResult = contextDependent;
        }

        public TestExpression(Object computeResult, Object computeValueResult) {
            this.computeResult = computeResult;
            this.computeValueResult = computeValueResult;
        }

        public boolean computeContextDependent() {
            computeContextDependentCallCount++;
            return contextDependentResult;
        }

        public Object computeValue(EvalContext context) {
            return computeValueResult;
        }

        public Object compute(EvalContext context) {
            return computeResult;
        }
    }

    private static class DummyEvalContext extends EvalContext {
        private List list;
        private int index = -1;
        private RootContext rootContext;

        public DummyEvalContext(List list, RootContext rootContext) {
            super(null);
            this.list = list == null ? Collections.EMPTY_LIST : list;
            this.rootContext = rootContext;
        }

        public NodePointer getCurrentNodePointer() {
            if (index >= 0 && index < list.size()) {
                Object item = list.get(index);
                if (item instanceof NodePointer) {
                    return (NodePointer) item;
                }
                return NodePointer.newNodePointer(new QName("item"), item, Locale.ENGLISH);
            }
            return null;
        }

        public boolean nextNode() {
            index++;
            return index < list.size();
        }

        public boolean nextSet() {
            return false;
        }

        public int getPosition() {
            return index + 1;
        }

        public RootContext getRootContext() {
            return rootContext;
        }
    }

    @Test
    public void testConstants() {
        Assert.assertEquals(0.0, Expression.ZERO.doubleValue(), 0.0);
        Assert.assertEquals(1.0, Expression.ONE.doubleValue(), 0.0);
        Assert.assertTrue(Double.isNaN(Expression.NOT_A_NUMBER.doubleValue()));
    }

    @Test
    public void testIsContextDependentCaching() {
        TestExpression trueExpr = new TestExpression(true);
        Assert.assertTrue(trueExpr.isContextDependent());
        Assert.assertTrue(trueExpr.isContextDependent());
        Assert.assertEquals(1, trueExpr.computeContextDependentCallCount);

        TestExpression falseExpr = new TestExpression(false);
        Assert.assertFalse(falseExpr.isContextDependent());
        Assert.assertFalse(falseExpr.isContextDependent());
        Assert.assertEquals(1, falseExpr.computeContextDependentCallCount);
    }

    @Test
    public void testIterateWithEvalContextResult() {
        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(parentContext, NodePointer.newNodePointer(new QName("root"), "root", Locale.ENGLISH));

        NodePointer np1 = NodePointer.newNodePointer(new QName("n1"), "val1", Locale.ENGLISH);
        NodePointer np2 = NodePointer.newNodePointer(new QName("n2"), "val2", Locale.ENGLISH);
        List nodes = Arrays.asList(new Object[]{np1, np2});

        DummyEvalContext evalContext = new DummyEvalContext(nodes, rootContext);
        TestExpression expr = new TestExpression(evalContext, null);

        Iterator it = expr.iterate(null);
        Assert.assertTrue(it instanceof Expression.ValueIterator);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("val1", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("val2", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testIterateWithSimpleListResult() {
        List data = Arrays.asList(new Object[]{"apple", "banana"});
        TestExpression expr = new TestExpression(data, null);

        Iterator it = expr.iterate(null);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("apple", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("banana", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testIterateWithNullResult() {
        TestExpression expr = new TestExpression(null, null);
        Iterator it = expr.iterate(null);
        Assert.assertNotNull(it);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testIteratePointersWithNull() {
        TestExpression expr = new TestExpression(null, null);
        Iterator it = expr.iteratePointers(null);
        Assert.assertNotNull(it);
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testIteratePointersWithEvalContext() {
        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = new RootContext(parentContext, NodePointer.newNodePointer(new QName("root"), "testRoot", Locale.ENGLISH));
        DummyEvalContext evalContext = new DummyEvalContext(new ArrayList(), rootContext);

        TestExpression expr = new TestExpression(evalContext, null);
        Iterator it = expr.iteratePointers(null);
        Assert.assertSame(evalContext, it);
    }

    @Test
    public void testIteratePointersWithObjectList() {
        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext("testRoot");
        RootContext rootContext = new RootContext(parentContext, NodePointer.newNodePointer(new QName("root"), "testRoot", Locale.GERMAN));
        DummyEvalContext context = new DummyEvalContext(new ArrayList(), rootContext);

        List data = Arrays.asList(new Object[]{"elem1", "elem2"});
        TestExpression expr = new TestExpression(data, null);

        Iterator it = expr.iteratePointers(context);
        Assert.assertTrue(it instanceof Expression.PointerIterator);
        Assert.assertTrue(it.hasNext());
        Object p1 = it.next();
        Assert.assertTrue(p1 instanceof NodePointer);
        Assert.assertEquals("elem1", ((NodePointer) p1).getValue());
        Assert.assertEquals(Locale.GERMAN, ((NodePointer) p1).getLocale());

        Assert.assertTrue(it.hasNext());
        Object p2 = it.next();
        Assert.assertTrue(p2 instanceof NodePointer);
        Assert.assertEquals("elem2", ((NodePointer) p2).getValue());
        Assert.assertEquals(Locale.GERMAN, ((NodePointer) p2).getLocale());

        Assert.assertFalse(it.hasNext());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testPointerIteratorWithExistingPointers() {
        BasicVariables vars = new BasicVariables();
        vars.declareVariable("var1", "value1");
        VariablePointer vp = new VariablePointer(vars, new QName("var1"));

        List list = Arrays.asList(new Object[]{vp, "plainString"});
        Expression.PointerIterator it = new Expression.PointerIterator(list.iterator(), new QName("defaultQName"), Locale.FRENCH);

        Assert.assertTrue(it.hasNext());
        Object next1 = it.next();
        Assert.assertSame(vp, next1);

        Assert.assertTrue(it.hasNext());
        Object next2 = it.next();
        Assert.assertTrue(next2 instanceof NodePointer);
        Assert.assertEquals("plainString", ((NodePointer) next2).getValue());
        Assert.assertEquals(Locale.FRENCH, ((NodePointer) next2).getLocale());

        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    @SuppressWarnings("deprecation")
    public void testPointerIteratorRemoveThrowsException() {
        List list = Arrays.asList(new Object[]{"item"});
        Expression.PointerIterator it = new Expression.PointerIterator(list.iterator(), new QName("q"), Locale.ENGLISH);
        it.remove();
    }

    @Test
    public void testValueIteratorWithPointersAndRawValues() {
        NodePointer np = NodePointer.newNodePointer(new QName("node"), "nodeValue", Locale.US);
        List list = Arrays.asList(new Object[]{np, "rawValue", null});

        Expression.ValueIterator it = new Expression.ValueIterator(list.iterator());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("nodeValue", it.next());

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("rawValue", it.next());

        Assert.assertTrue(it.hasNext());
        Assert.assertNull(it.next());

        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValueIteratorRemoveThrowsException() {
        List list = Arrays.asList(new Object[]{"val"});
        Expression.ValueIterator it = new Expression.ValueIterator(list.iterator());
        it.remove();
    }

    @Test
    public void testComputeValueMethod() {
        TestExpression expr = new TestExpression("compResult", "compValResult");
        Assert.assertEquals("compValResult", expr.computeValue(null));
        Assert.assertEquals("compResult", expr.compute(null));
    }
}
