package org.apache.commons.math3.optimization.linear;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class SimplexSolverTest {

    @Test
    public void testDefaultConstructor() {
        SimplexSolver solver = new SimplexSolver();
        Assert.assertNotNull(solver);
    }

    @Test
    public void testCustomConstructor() {
        SimplexSolver solver = new SimplexSolver(1e-4, 5);
        Assert.assertNotNull(solver);
    }

    @Test
    public void testSimpleMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(2.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(6.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(36.0, solution.getValue(), 1e-6);
    }

    @Test
    public void testSimpleMinimize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 4));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(4.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(0.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(-8.0, solution.getValue(), 1e-6);
    }

    @Test(expected = UnboundedSolutionException.class)
    public void testUnboundedSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -1 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = NoFeasibleSolutionException.class)
    public void testNoFeasibleSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, 2));

        SimplexSolver solver = new SimplexSolver();
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test
    public void testEqualityConstraintsPhase1() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 2));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertEquals(2.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(0.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(2.0, solution.getValue(), 1e-6);
    }

    @Test
    public void testNegativeVariablesAllowed() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, -10));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, -2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 5));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, false);
        Assert.assertEquals(-10.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(5.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(-15.0, solution.getValue(), 1e-6);
    }

    @Test
    public void testDegeneracyAndBlandRule() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 10, -57, -9, -24 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 0.5, -5.5, -2.5, 9 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 0.5, -1.5, -0.5, 1 }, Relationship.LEQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0, 0 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertNotNull(solution);
    }

    @Test
    public void testSolvePhase1NoArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 1));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertEquals(1.0, solution.getPoint()[0], 1e-6);
    }

    @Test(expected = MaxCountExceededException.class)
    public void testMaxIterationsExceeded() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));

        SimplexSolver solver = new SimplexSolver();
        solver.setMaxIterations(0);
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test
    public void testArtificialVariableRemovalTieBreak() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0, 0, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.EQ, 0));
        constraints.add(new LinearConstraint(new double[] { 1, 0, 1 }, Relationship.GEQ, 0));

        SimplexSolver solver = new SimplexSolver();
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertNotNull(solution);
    }
}
