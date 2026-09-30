package org.apache.commons.math3.optim;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.junit.Test;
import org.junit.Assert;

public class BaseOptimizerTest {

    private static class TestOptimizer extends BaseOptimizer<Object> {
        private final Object result;

        public TestOptimizer(ConvergenceChecker<Object> checker) {
            this(checker, null);
        }

        public TestOptimizer(ConvergenceChecker<Object> checker, Object result) {
            super(checker);
            this.result = result;
        }

        @Override
        protected Object doOptimize() {
            return result;
        }
    }

    private static final ConvergenceChecker<Object> DUMMY_CHECKER = new ConvergenceChecker<Object>() {
        @Override
        public boolean converged(int iteration, Object previous, Object current) {
            return false;
        }
    };

    @Test
    public void testConstructor() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        Assert.assertEquals(0, opt.getMaxEvaluations());
        Assert.assertEquals(0, opt.getMaxIterations());
        Assert.assertEquals(0, opt.getEvaluations());
        Assert.assertEquals(0, opt.getIterations());
        Assert.assertSame(DUMMY_CHECKER, opt.getConvergenceChecker());
    }

    @Test
    public void testConstructorWithNullChecker() {
        TestOptimizer opt = new TestOptimizer(null);
        Assert.assertNull(opt.getConvergenceChecker());
    }

    @Test
    public void testParseOptimizationDataMaxEval() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxEval(100));
        Assert.assertEquals(100, opt.getMaxEvaluations());
        Assert.assertEquals(0, opt.getMaxIterations()); // unchanged
    }

    @Test
    public void testParseOptimizationDataMaxIter() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxIter(50));
        Assert.assertEquals(0, opt.getMaxEvaluations()); // unchanged
        Assert.assertEquals(50, opt.getMaxIterations());
    }

    @Test
    public void testParseOptimizationDataBoth() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxEval(42), new MaxIter(7));
        Assert.assertEquals(42, opt.getMaxEvaluations());
        Assert.assertEquals(7, opt.getMaxIterations());
    }

    @Test
    public void testParseOptimizationDataRetainsPreviousValues() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        // set max eval to 10
        opt.optimize(new MaxEval(10));
        Assert.assertEquals(10, opt.getMaxEvaluations());
        // subsequent call with no arguments retains previous max
        opt.optimize();
        Assert.assertEquals(10, opt.getMaxEvaluations());
        Assert.assertEquals(0, opt.getMaxIterations());
    }

    @Test
    public void testParseOptimizationDataOverwritesOnlyGiven() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxEval(10));
        opt.optimize(new MaxIter(5));
        // eval should still be 10, iter should be 5
        Assert.assertEquals(10, opt.getMaxEvaluations());
        Assert.assertEquals(5, opt.getMaxIterations());
    }

    @Test(expected = NullPointerException.class)
    public void testParseOptimizationDataNullArray() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize((OptimizationData[]) null);
    }

    @Test
    public void testParseOptimizationDataNullElements() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        // array with nulls should not throw, max stays at default 0
        opt.optimize(null, new MaxEval(1), null);
        Assert.assertEquals(1, opt.getMaxEvaluations()); // MaxEval processed
        // second call with a single null and nothing else -> no change
        opt.optimize((OptimizationData) null);
        Assert.assertEquals(1, opt.getMaxEvaluations()); // unchanged
    }

    @Test
    public void testInitialCounts() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        Assert.assertEquals(0, opt.getEvaluations());
        Assert.assertEquals(0, opt.getIterations());
    }

    @Test
    public void testOptimizeResetsEvaluationCount() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        // first set a positive max, then increment
        opt.optimize(new MaxEval(10));
        opt.incrementEvaluationCount();
        Assert.assertEquals(1, opt.getEvaluations());
        // calling optimize resets count
        opt.optimize();
        Assert.assertEquals(0, opt.getEvaluations());
    }

    @Test
    public void testOptimizeResetsIterationCount() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxIter(10));
        opt.incrementIterationCount();
        Assert.assertEquals(1, opt.getIterations());
        opt.optimize();
        Assert.assertEquals(0, opt.getIterations());
    }

    @Test
    public void testIncrementEvaluationCountNormal() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxEval(5));
        // increment three times
        opt.incrementEvaluationCount();
        opt.incrementEvaluationCount();
        opt.incrementEvaluationCount();
        Assert.assertEquals(3, opt.getEvaluations());
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testIncrementEvaluationCountThrows() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxEval(1));
        opt.incrementEvaluationCount(); // first call ok
        opt.incrementEvaluationCount(); // second throws
    }

    @Test(expected = TooManyEvaluationsException.class)
    public void testIncrementEvaluationCountThrowsImmediatelyWhenMaxZero() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        // max evaluations is 0, first increment should throw
        opt.incrementEvaluationCount();
    }

    @Test
    public void testIncrementIterationCountNormal() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxIter(5));
        opt.incrementIterationCount();
        opt.incrementIterationCount();
        Assert.assertEquals(2, opt.getIterations());
    }

    @Test(expected = TooManyIterationsException.class)
    public void testIncrementIterationCountThrows() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.optimize(new MaxIter(1));
        opt.incrementIterationCount(); // ok
        opt.incrementIterationCount(); // throws
    }

    @Test(expected = TooManyIterationsException.class)
    public void testIncrementIterationCountThrowsImmediatelyWhenMaxZero() {
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER);
        opt.incrementIterationCount();
    }

    @Test
    public void testOptimizeReturnsDoOptimizeResult() {
        Object expectedResult = new Object();
        TestOptimizer opt = new TestOptimizer(DUMMY_CHECKER, expectedResult);
        Object actual = opt.optimize();
        Assert.assertSame(expectedResult, actual);
    }
}
