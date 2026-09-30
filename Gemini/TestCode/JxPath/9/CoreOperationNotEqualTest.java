package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationNotEqualTest {

    @Test
    public void testGetSymbol() {
        Expression arg1 = new Constant("a");
        Expression arg2 = new Constant("b");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        Assert.assertEquals("!=", op.getSymbol());
    }

    @Test
    public void testComputeValueEqualStrings() {
        Constant arg1 = new Constant("test");
        Constant arg2 = new Constant("test");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);

        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueDifferentStrings() {
        Constant arg1 = new Constant("test1");
        Constant arg2 = new Constant("test2");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);

        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueEqualNumbers() {
        Constant arg1 = new Constant(Double.valueOf(42.0));
        Constant arg2 = new Constant(Double.valueOf(42.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);

        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueDifferentNumbers() {
        Constant arg1 = new Constant(Double.valueOf(42.0));
        Constant arg2 = new Constant(Double.valueOf(43.0));
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);

        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueNumberAndString() {
        Constant arg1 = new Constant(Double.valueOf(100.0));
        Constant arg2 = new Constant("100");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);

        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueWithEvalContext() {
        JXPathContextReferenceImpl jxpathContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        RootContext rootContext = new RootContext(jxpathContext, NodePointer.newNodePointer(null, new Object(), null));

        Constant arg1 = new Constant("hello");
        Constant arg2 = new Constant("world");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);

        Object result = op.computeValue(rootContext);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testToStringRepresentation() {
        Constant arg1 = new Constant("a");
        Constant arg2 = new Constant("b");
        CoreOperationNotEqual op = new CoreOperationNotEqual(arg1, arg2);
        String str = op.toString();
        Assert.assertTrue(str.contains("!="));
    }
}
