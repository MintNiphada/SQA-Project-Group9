package org.apache.commons.math.estimation;

import org.junit.Assert;
import org.junit.Test;

public class AbstractEstimatorTest {

    private static class SimpleMeasurement extends WeightedMeasurement {
        private static final long serialVersionUID = 1L;
        private final double[] partials;

        public SimpleMeasurement(double weight, double measuredValue, double theoreticalValue, double[] partials) {
            super(weight, measuredValue);
            setIgnored(false);
            this.theoreticalValue = theoreticalValue;
            this.partials = partials;
        }

        private final double theoreticalValue;

        public double getTheoreticalValue() {
            return theoreticalValue;
        }

        public double getPartial(EstimatedParameter parameter) {
            if ("p0".equals(parameter.getName())) {
                return partials.length > 0 ? partials[0] : 0.0;
            }
            if ("p1".equals(parameter.getName())) {
                return partials.length > 1 ? partials[1] : 0.0;
            }
            return 0.0;
        }
    }

    private static class SimpleProblem implements EstimationProblem {
        private final EstimatedParameter[] allParams;
        private final EstimatedParameter[] unboundParams;
        private final WeightedMeasurement[] measurements;

        public SimpleProblem(EstimatedParameter[] allParams, EstimatedParameter[] unboundParams, WeightedMeasurement[] measurements) {
            this.allParams = allParams;
            this.unboundParams = unboundParams;
            this.measurements = measurements;
        }

        public EstimatedParameter[] getAllParameters() {
            return allParams;
        }

        public EstimatedParameter[] getUnboundParameters() {
            return unboundParams;
        }

        public WeightedMeasurement[] getMeasurements() {
            return measurements;
        }
    }

    private static class TestEstimator extends AbstractEstimator {
        public void estimate(EstimationProblem problem) throws EstimationException {
            initializeEstimate(problem);
        }

        public void callUpdateJacobian() {
            updateJacobian();
        }

        public void callUpdateResidualsAndCost() throws EstimationException {
            updateResidualsAndCost();
        }

        public void callInitializeEstimate(EstimationProblem problem) {
            initializeEstimate(problem);
        }

        public double[] getJacobian() {
            return jacobian;
        }

        public double[] getResiduals() {
            return residuals;
        }

        public double getCost() {
            return cost;
        }
    }

    @Test
    public void testCostAndJacobianEvaluationCounters() {
        TestEstimator estimator = new TestEstimator();
        Assert.assertEquals(0, estimator.getCostEvaluations());
        Assert.assertEquals(0, estimator.getJacobianEvaluations());

        estimator.incrementJacobianEvaluationsCounter();
        Assert.assertEquals(1, estimator.getJacobianEvaluations());

        estimator.setMaxCostEval(10);
    }

    @Test
    public void testInitializeEstimate() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter p1 = new EstimatedParameter("p1", 2.0);
        EstimatedParameter[] all = new EstimatedParameter[]{p0, p1};
        EstimatedParameter[] unbound = new EstimatedParameter[]{p0};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(1.0, 2.0, 1.0, new double[]{2.0})
        };
        SimpleProblem problem = new SimpleProblem(all, unbound, measurements);

        estimator.estimate(problem);

        Assert.assertEquals(0, estimator.getCostEvaluations());
        Assert.assertEquals(0, estimator.getJacobianEvaluations());
        Assert.assertEquals(Double.POSITIVE_INFINITY, estimator.getCost(), 1e-15);
        Assert.assertEquals(1, estimator.getResiduals().length);
        Assert.assertEquals(1, estimator.getJacobian().length);
    }

    @Test
    public void testUpdateJacobian() {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter p1 = new EstimatedParameter("p1", 2.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0, p1};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(4.0, 0.0, 0.0, new double[]{3.0, 5.0}),
            new SimpleMeasurement(9.0, 0.0, 0.0, new double[]{2.0, 4.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);
        estimator.callInitializeEstimate(problem);

        estimator.callUpdateJacobian();

        Assert.assertEquals(1, estimator.getJacobianEvaluations());
        double[] jac = estimator.getJacobian();
        Assert.assertEquals(4, jac.length);
        Assert.assertEquals(-2.0 * 3.0, jac[0], 1e-12);
        Assert.assertEquals(-2.0 * 5.0, jac[1], 1e-12);
        Assert.assertEquals(-3.0 * 2.0, jac[2], 1e-12);
        Assert.assertEquals(-3.0 * 4.0, jac[3], 1e-12);
    }

    @Test
    public void testUpdateResidualsAndCostSuccess() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(5);
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(4.0, 10.0, 8.0, new double[]{1.0}),
            new SimpleMeasurement(9.0, 5.0, 2.0, new double[]{1.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);
        estimator.callInitializeEstimate(problem);

        estimator.callUpdateResidualsAndCost();

        Assert.assertEquals(1, estimator.getCostEvaluations());
        double[] residuals = estimator.getResiduals();
        Assert.assertEquals(2, residuals.length);
        Assert.assertEquals(2.0 * 2.0, residuals[0], 1e-12);
        Assert.assertEquals(3.0 * 3.0, residuals[1], 1e-12);

        double expectedCost = Math.sqrt(4.0 * 4.0 + 9.0 * 9.0);
        Assert.assertEquals(expectedCost, estimator.getCost(), 1e-12);
    }

    @Test(expected = EstimationException.class)
    public void testUpdateResidualsAndCostExceedMaxEval() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        estimator.setMaxCostEval(1);
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(1.0, 2.0, 1.0, new double[]{1.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);
        estimator.callInitializeEstimate(problem);

        estimator.callUpdateResidualsAndCost();
        estimator.callUpdateResidualsAndCost();
    }

    @Test
    public void testGetRMS() {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(2.0, 5.0, 2.0, new double[]{1.0}),
            new SimpleMeasurement(3.0, 6.0, 4.0, new double[]{1.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);

        double criterion = 2.0 * (3.0 * 3.0) + 3.0 * (2.0 * 2.0);
        double expectedRMS = Math.sqrt(criterion / 2.0);
        Assert.assertEquals(expectedRMS, estimator.getRMS(problem), 1e-12);
    }

    @Test
    public void testGetChiSquare() {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(2.0, 5.0, 1.0, new double[]{1.0}),
            new SimpleMeasurement(4.0, 7.0, 3.0, new double[]{1.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);

        double expectedChiSquare = (4.0 * 4.0 / 2.0) + (4.0 * 4.0 / 4.0);
        Assert.assertEquals(expectedChiSquare, estimator.getChiSquare(problem), 1e-12);
    }

    @Test
    public void testGetCovariancesSuccess() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter p1 = new EstimatedParameter("p1", 2.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0, p1};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(1.0, 1.0, 1.0, new double[]{1.0, 0.0}),
            new SimpleMeasurement(1.0, 2.0, 2.0, new double[]{0.0, 2.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);
        estimator.callInitializeEstimate(problem);

        double[][] covar = estimator.getCovariances(problem);
        Assert.assertEquals(2, covar.length);
        Assert.assertEquals(2, covar[0].length);
        Assert.assertEquals(1.0, covar[0][0], 1e-10);
        Assert.assertEquals(0.0, covar[0][1], 1e-10);
        Assert.assertEquals(0.0, covar[1][0], 1e-10);
        Assert.assertEquals(0.25, covar[1][1], 1e-10);
    }

    @Test(expected = EstimationException.class)
    public void testGetCovariancesSingular() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter p1 = new EstimatedParameter("p1", 2.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0, p1};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(1.0, 1.0, 1.0, new double[]{1.0, 1.0}),
            new SimpleMeasurement(1.0, 2.0, 2.0, new double[]{2.0, 2.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);
        estimator.callInitializeEstimate(problem);

        estimator.getCovariances(problem);
    }

    @Test(expected = EstimationException.class)
    public void testGuessParametersErrorsNoDegreesOfFreedom() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter p1 = new EstimatedParameter("p1", 2.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0, p1};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(1.0, 1.0, 1.0, new double[]{1.0, 0.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);
        estimator.callInitializeEstimate(problem);

        estimator.guessParametersErrors(problem);
    }

    @Test
    public void testGuessParametersErrorsSuccess() throws EstimationException {
        TestEstimator estimator = new TestEstimator();
        EstimatedParameter p0 = new EstimatedParameter("p0", 1.0);
        EstimatedParameter[] params = new EstimatedParameter[]{p0};
        WeightedMeasurement[] measurements = new WeightedMeasurement[]{
            new SimpleMeasurement(1.0, 4.0, 2.0, new double[]{2.0}),
            new SimpleMeasurement(1.0, 5.0, 1.0, new double[]{1.0})
        };
        SimpleProblem problem = new SimpleProblem(params, params, measurements);
        estimator.callInitializeEstimate(problem);

        double[] errors = estimator.guessParametersErrors(problem);
        Assert.assertEquals(1, errors.length);

        double chiSq = (2.0 * 2.0 / 1.0) + (4.0 * 4.0 / 1.0);
        double c = Math.sqrt(chiSq / (2 - 1));
        double jTj = 1.0 * (2.0 * 2.0) + 1.0 * (1.0 * 1.0);
        double covar00 = 1.0 / jTj;
        double expectedError = Math.sqrt(covar00) * c;

        Assert.assertEquals(expectedError, errors[0], 1e-10);
    }
}
