package org.apache.commons.math3.optim;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.exception.TooManyIterationsException;
import org.junit.Assert;
import org.junit.Test;

public class BaseOptimizerTest {

    private static class DummyOptimizer extends BaseOptimizer<String> {
        private int evalCountToPerform = 0;
        private int iterCountToPerform = 0;
        private String returnValue = "OPTIMAL";

        public DummyOptimizer(ConvergenceChecker<String> checker) {
            super(checker);
        }

        public void setEvalCountToPerform(int count) {
            this.evalCountToPerform = count;
        }

        public void setIterCountToPerform(int count) {
            this.iterCountToPerform = count;
        }

        public void setReturnValue(String value) {
            this.returnValue = value;
        }

        @Override
        protected String doOptimize() {
            for (int i = 0; i < evalCountToPerform; i++) {
                incrementEvaluationCount();
            }
            for (int i = 0; i < iterCountToPerform; i++) {
                incrementIterationCount();
            }
            return returnValue;
        }

        public void publicIncrementEvaluationCount() {
            incrementEvaluationCount();
        }

        public void publicIncrementIterationCount() {
            incrementIterationCount();
        }
    }

    private static class DummyChecker implements ConvergenceChecker<String> {
        public boolean converged(int iteration, String previous, String current) {
            return true;
        }
    }

    private static class DummyOptimizationData implements OptimizationData {}

    @Test
    public void testConstructorAndInitialState() {
        ConvergenceChecker<String> checker = new DummyChecker();
        DummyOptimizer optimizer = new DummyOptimizer(checker);

        Assert.assertSame(checker, optimizer.getConvergenceChecker());
        Assert.assertEquals(0, optimizer.getMaxEvaluations());
        Assert.assertEquals(0, optimizer.getMaxIterations());
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(0, optimizer.getIterations());
    }

    @Test
    public void testNullConvergenceChecker() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        Assert.assertNull(optimizer.getConvergenceChecker());
    }

    @Test
    public void testOptimizeSuccessfulExecution() {
        DummyOptimizer optimizer = new DummyOptimizer(new DummyChecker());
        optimizer.setEvalCountToPerform(5);
        optimizer.setIterCountToPerform(3);

        String result = optimizer.optimize(new MaxEval(10), new MaxIter(10));

        Assert.assertEquals("OPTIMAL", result);
        Assert.assertEquals(10, optimizer.getMaxEvaluations());
        Assert.assertEquals(10, optimizer.getMaxIterations());
        Assert.assertEquals(5, optimizer.getEvaluations());
        Assert.assertEquals(3, optimizer.getIterations());
    }

    @Test
    public void testOptimizeResetsCounters() {
        DummyOptimizer optimizer = new DummyOptimizer(new DummyChecker());
        optimizer.setEvalCountToPerform(4);
        optimizer.setIterCountToPerform(2);

        optimizer.optimize(new MaxEval(10), new MaxIter(10));
        Assert.assertEquals(4, optimizer.getEvaluations());
        Assert.assertEquals(2, optimizer.getIterations());

        optimizer.setEvalCountToPerform(1);
        optimizer.setIterCountToPerform(1);
        optimizer.optimize();

        Assert.assertEquals(1, optimizer.getEvaluations());
        Assert.assertEquals(1, optimizer.getIterations());
        Assert.assertEquals(10, optimizer.getMaxEvaluations());
        Assert.assertEquals(10, optimizer.getMaxIterations());
    }

    @Test
    public void testParseOptimizationDataWithMultipleOptions() {
        DummyOptimizer optimizer = new DummyOptimizer(null);

        optimizer.optimize(
            new DummyOptimizationData(),
            new MaxEval(100),
            new DummyOptimizationData(),
            new MaxIter(50),
            new DummyOptimizationData()
        );

        Assert.assertEquals(100, optimizer.getMaxEvaluations());
        Assert.assertEquals(50, optimizer.getMaxIterations());
    }

    @Test
    public void testParseOptimizationDataOverridesPreviousSettings() {
        DummyOptimizer optimizer = new DummyOptimizer(null);

        optimizer.optimize(new MaxEval(100), new MaxIter(50));
        Assert.assertEquals(100, optimizer.getMaxEvaluations());
        Assert.assertEquals(50, optimizer.getMaxIterations());

        optimizer.optimize(new MaxEval(200));
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        Assert.assertEquals(50, optimizer.getMaxIterations());

        optimizer.optimize(new MaxIter(75));
        Assert.assertEquals(200, optimizer.getMaxEvaluations());
        Assert.assertEquals(75, optimizer.getMaxIterations());
    }

    @Test
    public void testTooManyEvaluationsException() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setEvalCountToPerform(6);
        optimizer.setIterCountToPerform(0);

        try {
            optimizer.optimize(new MaxEval(5), new MaxIter(10));
            Assert.fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            Assert.assertEquals(5, e.getMax().intValue());
        }
    }

    @Test
    public void testTooManyIterationsException() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.setEvalCountToPerform(0);
        optimizer.setIterCountToPerform(4);

        try {
            optimizer.optimize(new MaxEval(10), new MaxIter(3));
            Assert.fail("Expected TooManyIterationsException");
        } catch (TooManyIterationsException e) {
            Assert.assertEquals(3, e.getMax().intValue());
        }
    }

    @Test
    public void testDirectIncrementEvaluationCountBoundary() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.optimize(new MaxEval(1), new MaxIter(1));

        optimizer.publicIncrementEvaluationCount();
        Assert.assertEquals(1, optimizer.getEvaluations());

        try {
            optimizer.publicIncrementEvaluationCount();
            Assert.fail("Expected TooManyEvaluationsException");
        } catch (TooManyEvaluationsException e) {
            Assert.assertEquals(1, e.getMax().intValue());
        }
    }

    @Test
    public void testDirectIncrementIterationCountBoundary() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.optimize(new MaxEval(1), new MaxIter(1));

        optimizer.publicIncrementIterationCount();
        Assert.assertEquals(1, optimizer.getIterations());

        try {
            optimizer.publicIncrementIterationCount();
            Assert.fail("Expected TooManyIterationsException");
        } catch (TooManyIterationsException e) {
            Assert.assertEquals(1, e.getMax().intValue());
        }
    }

    @Test
    public void testEmptyOptimizationData() {
        DummyOptimizer optimizer = new DummyOptimizer(null);
        optimizer.optimize();

        Assert.assertEquals(0, optimizer.getMaxEvaluations());
        Assert.assertEquals(0, optimizer.getMaxIterations());
        Assert.assertEquals(0, optimizer.getEvaluations());
        Assert.assertEquals(0, optimizer.getIterations());
    }
}
