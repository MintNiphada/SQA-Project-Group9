package org.apache.commons.math3.optimization.linear;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.junit.Assert;
import org.junit.Test;

public class SimplexTableauTest {

    @Test
    public void testConstructorsAndGetters() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 5.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.GEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 6.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);

        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(2, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(2, tableau.getNumSlackVariables());
        Assert.assertEquals(2, tableau.getNumArtificialVariables());
        Assert.assertEquals(2 + 2, tableau.getSlackVariableOffset());
        Assert.assertEquals(2 + 2 + 2, tableau.getArtificialVariableOffset());
        Assert.assertEquals(tableau.getWidth() - 1, tableau.getRhsOffset());
        Assert.assertEquals(5, tableau.getHeight());
        Assert.assertEquals(9, tableau.getWidth());
        Assert.assertNotNull(tableau.getData());
    }

    @Test
    public void testNegativeConstraintNormalization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, -2.0 }, Relationship.LEQ, -3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6, 10);
        List<LinearConstraint> normalized = tableau.normalizeConstraints(constraints);

        Assert.assertEquals(1, normalized.size());
        Assert.assertEquals(3.0, normalized.get(0).getValue(), 1e-9);
        Assert.assertEquals(Relationship.GEQ, normalized.get(0).getRelationship());
        Assert.assertEquals(-1.0, normalized.get(0).getCoefficients().getEntry(0), 1e-9);
        Assert.assertEquals(2.0, normalized.get(0).getCoefficients().getEntry(1), 1e-9);
    }

    @Test
    public void testNonRestrictedVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3.0, -2.0 }, 10.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1e-6);

        Assert.assertEquals(3, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(1, tableau.getNumSlackVariables());

        double invertedSum = SimplexTableau.getInvertedCoefficientSum(new ArrayRealVector(new double[] { 3.0, -2.0 }));
        Assert.assertEquals(-1.0, invertedSum, 1e-9);
    }

    @Test
    public void testGetBasicRowAndSolution() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 5.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 7.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);

        Integer basicRowZ = tableau.getBasicRow(0);
        Assert.assertEquals(Integer.valueOf(0), basicRowZ);

        Integer nonBasicCol = tableau.getBasicRow(1);
        Assert.assertNull(nonBasicCol);

        PointValuePair solution = tableau.getSolution();
        Assert.assertEquals(0.0, solution.getPoint()[0], 1e-9);
        Assert.assertEquals(0.0, solution.getPoint()[1], 1e-9);
        Assert.assertEquals(0.0, solution.getValue(), 1e-9);
    }

    @Test
    public void testSolutionWithNegativeVariablesAndDuplicates() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 4.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1e-6);

        tableau.setEntry(0, 1, 0.0);
        tableau.setEntry(1, 1, 1.0);
        tableau.setEntry(0, 2, 0.0);
        tableau.setEntry(1, 2, 1.0);

        PointValuePair solution = tableau.getSolution();
        Assert.assertEquals(4.0, solution.getPoint()[0], 1e-9);
        Assert.assertEquals(0.0, solution.getPoint()[1], 1e-9);
    }

    @Test
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.GEQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, true, 1e-6);
        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());

        tableau.dropPhase1Objective();
        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(0, tableau.getNumArtificialVariables());

        tableau.dropPhase1Objective();
        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
    }

    @Test
    public void testOptimalityOperations() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);
        Assert.assertTrue(tableau.isOptimal());

        tableau.setEntry(0, 1, -2.0);
        Assert.assertFalse(tableau.isOptimal());
    }

    @Test
    public void testRowOperations() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 4.0 }, Relationship.LEQ, 8.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1e-6);

        tableau.divideRow(1, 2.0);
        Assert.assertEquals(1.0, tableau.getEntry(1, 1), 1e-9);
        Assert.assertEquals(2.0, tableau.getEntry(1, 2), 1e-9);
        Assert.assertEquals(4.0, tableau.getEntry(1, tableau.getRhsOffset()), 1e-9);

        tableau.subtractRow(0, 1, -2.0);
        Assert.assertEquals(0.0, tableau.getEntry(0, 1), 1e-9);
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1.0, 3.0 }, 0.0);
        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tab1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1e-6);
        SimplexTableau tab2 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1e-6);
        SimplexTableau tab3 = new SimplexTableau(f2, c1, GoalType.MAXIMIZE, true, 1e-6);
        SimplexTableau tab4 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, 1e-6);

        Assert.assertTrue(tab1.equals(tab1));
        Assert.assertTrue(tab1.equals(tab2));
        Assert.assertEquals(tab1.hashCode(), tab2.hashCode());

        Assert.assertFalse(tab1.equals(null));
        Assert.assertFalse(tab1.equals("String"));
        Assert.assertFalse(tab1.equals(tab3));
        Assert.assertFalse(tab1.equals(tab4));
    }

    @Test
    public void testSerialization() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 5.0 }, 1.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 10.0));
        constraints.add(new LinearConstraint(new double[] { 3.0, 1.0 }, Relationship.GEQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1e-6);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(tableau);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        Assert.assertEquals(tableau, deserialized);
        Assert.assertEquals(tableau.getHeight(), deserialized.getHeight());
        Assert.assertEquals(tableau.getWidth(), deserialized.getWidth());
        Assert.assertEquals(tableau.getEntry(0, 0), deserialized.getEntry(0, 0), 1e-9);
    }
}
