package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Test;

public class CoreOperationEqualTest {

    @Test
    public void testGetSymbol() {
        Constant arg1 = new Constant("a");
        Constant arg2 = new Constant("b");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Assert.assertEquals("=", op.getSymbol());
    }

    @Test
    public void testComputeValueEqualStrings() {
        Constant arg1 = new Constant("test");
        Constant arg2 = new Constant("test");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueUnequalStrings() {
        Constant arg1 = new Constant("test1");
        Constant arg2 = new Constant("test2");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueEqualNumbers() {
        Constant arg1 = new Constant(Double.valueOf(10.5));
        Constant arg2 = new Constant(Double.valueOf(10.5));
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueUnequalNumbers() {
        Constant arg1 = new Constant(Double.valueOf(10.5));
        Constant arg2 = new Constant(Double.valueOf(20.5));
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueNumberAndStringEqual() {
        Constant arg1 = new Constant(Double.valueOf(100.0));
        Constant arg2 = new Constant("100");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueNumberAndStringUnequal() {
        Constant arg1 = new Constant(Double.valueOf(100.0));
        Constant arg2 = new Constant("101");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue(null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueBooleanEqual() {
        Constant arg1 = new Constant(Double.valueOf(1));
        Constant arg2 = new Constant(Double.valueOf(1));
        CoreOperationEqual innerOp1 = new CoreOperationEqual(arg1, arg2);
        CoreOperationEqual innerOp2 = new CoreOperationEqual(arg1, arg2);
        CoreOperationEqual outerOp = new CoreOperationEqual(innerOp1, innerOp2);
        Object result = outerOp.computeValue(null);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testComputeValueBooleanUnequal() {
        Constant arg1 = new Constant(Double.valueOf(1));
        Constant arg2 = new Constant(Double.valueOf(1));
        Constant arg3 = new Constant(Double.valueOf(2));
        CoreOperationEqual trueOp = new CoreOperationEqual(arg1, arg2);
        CoreOperationEqual falseOp = new CoreOperationEqual(arg1, arg3);
        CoreOperationEqual outerOp = new CoreOperationEqual(trueOp, falseOp);
        Object result = outerOp.computeValue(null);
        Assert.assertEquals(Boolean.FALSE, result);
    }

    @Test
    public void testComputeValueWithContext() {
        JXPathContextReferenceImpl parentContext = (JXPathContextReferenceImpl) JXPathContext.newContext(new Object());
        NodePointer rootPointer = NodePointer.newNodePointer(null, "root", null);
        RootContext rootContext = new RootContext(parentContext, rootPointer);

        Constant arg1 = new Constant("abc");
        Constant arg2 = new Constant("abc");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        Object result = op.computeValue(rootContext);
        Assert.assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void testToStringFormat() {
        Constant arg1 = new Constant("a");
        Constant arg2 = new Constant("b");
        CoreOperationEqual op = new CoreOperationEqual(arg1, arg2);
        String str = op.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("="));
    }
}
