package org.apache.commons.math3.optimization.linear;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;

public class SimplexSolverTest {

    @Test
    public void testConstructorDefault() throws Exception {
        SimplexSolver solver = new SimplexSolver();
        Field epsilonField = SimplexSolver.class.getDeclaredField("epsilon");
        epsilonField.setAccessible(true);
        double epsilon = (Double) epsilonField.get(solver);
        Field maxUlpsField = SimplexSolver.class.getDeclaredField("maxUlps");
        maxUlpsField.setAccessible(true);
        int maxUlps = (Integer) maxUlpsField.get(solver);
        Assert.assertEquals(1.0e-6, epsilon, 0.0);
        Assert.assertEquals(10, maxUlps);
    }

    @Test
    public void testConstructorWithParams() throws Exception {
        SimplexSolver solver = new SimplexSolver(1e-8, 5);
        Field epsilonField = SimplexSolver.class.getDeclaredField("epsilon");
        epsilonField.setAccessible(true);
        double epsilon = (Double) epsilonField.get(solver);
        Field maxUlpsField = SimplexSolver.class.getDeclaredField("maxUlps");
        maxUlpsField.setAccessible(true);
        int maxUlps = (Integer) maxUlpsField.get(solver);
        Assert.assertEquals(1e-8, epsilon, 0.0);
        Assert.assertEquals(5, maxUlps);
    }

    @Test
    public void testGetPivotColumn() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 5));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        Method method = SimplexSolver.class.getDeclaredMethod("getPivotColumn", SimplexTableau.class);
        method.setAccessible(true);
        Integer pivotCol = (Integer) method.invoke(new SimplexSolver(), tableau);
        Assert.assertNotNull(pivotCol);
        Assert.assertTrue(pivotCol >= 0);
    }

    @Test
    public void testGetPivotRowNormal() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 5));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        Method getPivotCol = SimplexSolver.class.getDeclaredMethod("getPivotColumn", SimplexTableau.class);
        getPivotCol.setAccessible(true);
        Integer pivotCol = (Integer) getPivotCol.invoke(new SimplexSolver(), tableau);
        Method getPivotRow = SimplexSolver.class.getDeclaredMethod("getPivotRow", SimplexTableau.class, int.class);
        getPivotRow.setAccessible(true);
        Integer pivotRow = (Integer) getPivotRow.invoke(new SimplexSolver(), tableau, pivotCol);
        Assert.assertNotNull(pivotRow);
    }

    @Test
    public void testGetPivotRowDegeneracy() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0, 0, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 }, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.LEQ, 2));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        Method getPivotCol = SimplexSolver.class.getDeclaredMethod("getPivotColumn", SimplexTableau.class);
        getPivotCol.setAccessible(true);
        Integer pivotCol = (Integer) getPivotCol.invoke(new SimplexSolver(), tableau);
        Method getPivotRow = SimplexSolver.class.getDeclaredMethod("getPivotRow", SimplexTableau.class, int.class);
        getPivotRow.setAccessible(true);
        Integer pivotRow = (Integer) getPivotRow.invoke(new SimplexSolver(), tableau, pivotCol);
        Assert.assertNotNull(pivotRow);
    }

    @Test
    public void testDoIteration() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 5));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        SimplexSolver solver = new SimplexSolver();
        Method doIteration = SimplexSolver.class.getDeclaredMethod("doIteration", SimplexTableau.class);
        doIteration.setAccessible(true);
        doIteration.invoke(solver, tableau);
    }

    @Test
    public void testSolvePhase1Feasible() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 5));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        SimplexSolver solver = new SimplexSolver();
        Method solvePhase1 = SimplexSolver.class.getDeclaredMethod("solvePhase1", SimplexTableau.class);
        solvePhase1.setAccessible(true);
        solvePhase1.invoke(solver, tableau);
    }

    @Test(expected = NoFeasibleSolutionException.class)
    public void testSolvePhase1Infeasible() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, -1));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        SimplexSolver solver = new SimplexSolver();
        Method solvePhase1 = SimplexSolver.class.getDeclaredMethod("solvePhase1", SimplexTableau.class);
        solvePhase1.setAccessible(true);
        solvePhase1.invoke(solver, tableau);
    }

    @Test
    public void testDoOptimizeSimple() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 2));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 3));
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(2.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(2.0, solution.getPoint()[1], 1e-6);
        Assert.assertEquals(10.0, solution.getValue(), 1e-6);
    }

    @Test(expected = UnboundedSolutionException.class)
    public void testDoOptimizeUnbounded() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 0));
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = NoFeasibleSolutionException.class)
    public void testDoOptimizeInfeasible() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, -1));
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test(expected = MaxCountExceededException.class)
    public void testDoOptimizeMaxIterations() {
        SimplexSolver solver = new SimplexSolver();
        solver.setMaxIterations(1);
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 5));
        solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
    }

    @Test
    public void testRestrictToNonNegative() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 5));
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertTrue(solution.getPoint()[0] >= -1e-10);
        Assert.assertTrue(solution.getPoint()[1] >= -1e-10);
    }

    @Test
    public void testDegeneracySolving() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 0, 0, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0, 0 }, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1, 0 }, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] { 1, 1, 0 }, Relationship.LEQ, 2));
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
    }

    @Test
    public void testArtificialVariableForcedOut() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.LEQ, 1));
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MAXIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(1.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(0.0, solution.getPoint()[1], 1e-6);
    }

    @Test
    public void testPhase1Only() {
        SimplexSolver solver = new SimplexSolver();
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 1));
        PointValuePair solution = solver.optimize(f, constraints, GoalType.MINIMIZE, true);
        Assert.assertNotNull(solution);
        Assert.assertEquals(1.0, solution.getPoint()[0], 1e-6);
        Assert.assertEquals(1.0, solution.getPoint()[1], 1e-6);
    }

    @Test
    public void testGetPivotRowNull() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 0 }, 0);
        Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 0));
        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6, 10);
        Method getPivotCol = SimplexSolver.class.getDeclaredMethod("getPivotColumn", SimplexTableau.class);
        getPivotCol.setAccessible(true);
        Integer pivotCol = (Integer) getPivotCol.invoke(new SimplexSolver(), tableau);
        Method getPivotRow = SimplexSolver.class.getDeclaredMethod("getPivotRow", SimplexTableau.class, int.class);
        getPivotRow.setAccessible(true);
        Integer pivotRow = (Integer) getPivotRow.invoke(new SimplexSolver(), tableau, pivotCol);
        Assert.assertNull(pivotRow);
    }
}
