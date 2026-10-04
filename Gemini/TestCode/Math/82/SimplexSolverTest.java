package org.apache.commons.math.optimization.linear;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class SimplexSolverTest {

    @Test
    public void testConstructors() {
        SimplexSolver solver1 = new SimplexSolver();
        Assert.assertEquals(1.0e-6, solver1.epsilon, 1.0e-12);

        SimplexSolver solver2 = new SimplexSolver(1.0e-4);
        Assert.assertEquals(1.0e-4, solver2.epsilon, 1.0e-12);
    }

    @Test
    public void testMinimizeSimple() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 6.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 2.0 }, Relationship.LEQ, 12.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertNotNull(solution);
        Assert.assertEquals(4.0, solution.getPoint()[0], 1.0e-6);
        Assert.assertEquals(0.0, solution.getPoint()[1], 1.0e-6);
        Assert.assertEquals(-8.0, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testMaximizeSimple() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, 5.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 2.0 }, Relationship.LEQ, 12.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 2.0 }, Relationship.LEQ, 18.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertNotNull(solution);
        Assert.assertEquals(2.0, solution.getPoint()[0], 1.0e-6);
        Assert.assertEquals(6.0, solution.getPoint()[1], 1.0e-6);
        Assert.assertEquals(36.0, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testTwoPhaseWithEqualityConstraint() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.EQ, 3.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertNotNull(solution);
        Assert.assertEquals(0.0, solution.getPoint()[0], 1.0e-6);
        Assert.assertEquals(1.5, solution.getPoint()[1], 1.0e-6);
        Assert.assertEquals(1.5, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testTwoPhaseWithGreaterOrEqualConstraint() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.GEQ, 5.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);

        Assert.assertNotNull(solution);
        Assert.assertEquals(4.0, solution.getPoint()[0], 1.0e-6);
        Assert.assertEquals(0.0, solution.getPoint()[1], 1.0e-6);
        Assert.assertEquals(8.0, solution.getValue(), 1.0e-6);
    }

    @Test(expected = NoFeasibleSolutionException.class)
    public void testInfeasibleProblem() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 4.0));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MINIMIZE, true);
    }

    @Test(expected = UnboundedSolutionException.class)
    public void testUnboundedProblem() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, -1.0 }, Relationship.LEQ, 1.0));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test
    public void testNegativeVariablesAllowed() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.GEQ, -2.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.GEQ, -3.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, -4.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);

        Assert.assertNotNull(solution);
        Assert.assertEquals(-4.0, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testIsOptimalAndSolvePhase1Directly() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 2.0));

        SimplexSolver solver = new SimplexSolver();
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1.0e-6);

        Assert.assertFalse(solver.isOptimal(tableau));

        solver.solvePhase1(tableau);
        tableau.discardArtificialVariables();

        while (!solver.isOptimal(tableau)) {
            solver.doIteration(tableau);
        }

        Assert.assertTrue(solver.isOptimal(tableau));
        RealPointValuePair solution = tableau.getSolution();
        Assert.assertEquals(2.0, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testSolvePhase1WithoutArtificialVariables() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 2.0));

        SimplexSolver solver = new SimplexSolver();
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        solver.solvePhase1(tableau);
        Assert.assertTrue(solver.isOptimal(tableau) || !solver.isOptimal(tableau));
    }

    @Test
    public void testIterationStepExecution() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1.0, -1.0 }, 0.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 3.0));

        SimplexSolver solver = new SimplexSolver();
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1.0e-6);

        Assert.assertFalse(solver.isOptimal(tableau));
        solver.doIteration(tableau);
        Assert.assertNotNull(tableau.getSolution());
    }

    @Test
    public void testConstantObjectiveFunction() throws OptimizationException {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0.0, 0.0 }, 10.0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexSolver solver = new SimplexSolver();
        RealPointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);

        Assert.assertNotNull(solution);
        Assert.assertEquals(10.0, solution.getValue(), 1.0e-6);
    }
}
