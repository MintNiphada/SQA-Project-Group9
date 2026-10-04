package org.apache.commons.math.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class SimplexTableauTest {

    @Test
    public void testMaximizeNonNegativeLeq() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 7);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 0, 2 }, Relationship.LEQ, 12));
        constraints.add(new LinearConstraint(new double[] { 3, 2 }, Relationship.LEQ, 18));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        Assert.assertEquals(2, tableau.getNumVariables());
        Assert.assertEquals(2, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(3, tableau.getNumSlackVariables());
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(3, tableau.getSlackVariableOffset());
        Assert.assertEquals(6, tableau.getArtificialVariableOffset());
        Assert.assertEquals(6, tableau.getRhsOffset());
        Assert.assertEquals(4, tableau.getHeight());
        Assert.assertEquals(7, tableau.getWidth());

        Assert.assertEquals(1.0, tableau.getEntry(0, 0), 1.0e-6);
        Assert.assertEquals(-3.0, tableau.getEntry(0, 1), 1.0e-6);
        Assert.assertEquals(-5.0, tableau.getEntry(0, 2), 1.0e-6);
        Assert.assertEquals(7.0, tableau.getEntry(0, 6), 1.0e-6);

        tableau.discardArtificialVariables();
        Assert.assertEquals(7, tableau.getWidth());
    }

    @Test
    public void testMinimizeUnrestrictedGeqAndNegativeRhs() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, -1 }, -4);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.GEQ, -5));
        constraints.add(new LinearConstraint(new double[] { -2, 1 }, Relationship.LEQ, -3));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1.0e-6);

        Assert.assertEquals(2, tableau.getNumVariables());
        Assert.assertEquals(3, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(4, tableau.getNegativeDecisionVariableOffset());

        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();
        Assert.assertEquals(2, normalized.size());
        Assert.assertEquals(5.0, normalized.get(0).getValue(), 1.0e-6);
        Assert.assertEquals(Relationship.LEQ, normalized.get(0).getRelationship());
        Assert.assertEquals(3.0, normalized.get(1).getValue(), 1.0e-6);
        Assert.assertEquals(Relationship.GEQ, normalized.get(1).getRelationship());
    }

    @Test
    public void testArtificialVariablesAndInitialization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.EQ, 3));
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(2, tableau.getNumArtificialVariables());
        Assert.assertEquals(1, tableau.getNumSlackVariables());

        tableau.discardArtificialVariables();
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(3, tableau.getHeight());
    }

    @Test
    public void testInvertedCoefficientSum() {
        RealVector vector = new ArrayRealVector(new double[] { 1.5, -2.5, 3.0 });
        double sum = SimplexTableau.getInvertedCoeffiecientSum(vector);
        Assert.assertEquals(-2.0, sum, 1.0e-6);
    }

    @Test
    public void testRowOperationsAndSetEntry() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        tableau.setEntry(1, 1, 8.0);
        Assert.assertEquals(8.0, tableau.getEntry(1, 1), 1.0e-6);

        tableau.divideRow(1, 2.0);
        Assert.assertEquals(4.0, tableau.getEntry(1, 1), 1.0e-6);

        tableau.subtractRow(1, 1, 0.5);
        Assert.assertEquals(2.0, tableau.getEntry(1, 1), 1.0e-6);

        double[][] data = tableau.getData();
        Assert.assertEquals(tableau.getHeight(), data.length);
        Assert.assertEquals(tableau.getWidth(), data[0].length);
    }

    @Test
    public void testGetSolutionBasicAndNegativeOffset() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 5));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 10));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1.0e-6);
        tableau.discardArtificialVariables();

        RealPointValuePair solution = tableau.getSolution();
        double[] point = solution.getPoint();
        Assert.assertEquals(2, point.length);
        Assert.assertEquals(5.0, point[0], 1.0e-6);
        Assert.assertEquals(10.0, point[1], 1.0e-6);
        Assert.assertEquals(25.0, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testGetSolutionWithRestrictedNegativeVariable() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 10));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        RealPointValuePair solution = tableau.getSolution();
        Assert.assertNotNull(solution);
        Assert.assertEquals(2, solution.getPoint().length);
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 2, 4 }, 0);
        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));
        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 5));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau t2 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau tDiffF = new SimplexTableau(f2, c1, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau tDiffC = new SimplexTableau(f1, c2, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau tDiffGoal = new SimplexTableau(f1, c1, GoalType.MINIMIZE, true, 1.0e-6);
        SimplexTableau tDiffRestrict = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, 1.0e-6);
        SimplexTableau tDiffEps = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-4);

        Assert.assertTrue(t1.equals(t1));
        Assert.assertTrue(t1.equals(t2));
        Assert.assertEquals(t1.hashCode(), t2.hashCode());

        Assert.assertFalse(t1.equals(null));
        Assert.assertFalse(t1.equals("String"));
        Assert.assertFalse(t1.equals(tDiffF));
        Assert.assertFalse(t1.equals(tDiffC));
        Assert.assertFalse(t1.equals(tDiffGoal));
        Assert.assertFalse(t1.equals(tDiffRestrict));
        Assert.assertFalse(t1.equals(tDiffEps));
    }

    @Test
    public void testSerialization() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 3);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(tableau);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        Assert.assertEquals(tableau, deserialized);
        Assert.assertEquals(tableau.hashCode(), deserialized.hashCode());
    }
}
