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
    public void testConstructorAndBasicProperties() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 5.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.GEQ, 1.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.EQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(2, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getNumSlackVariables());
        Assert.assertEquals(2, tableau.getNumArtificialVariables());
        Assert.assertEquals(5, tableau.getHeight());
        Assert.assertEquals(9, tableau.getWidth());

        Assert.assertEquals(4, tableau.getSlackVariableOffset());
        Assert.assertEquals(6, tableau.getArtificialVariableOffset());
        Assert.assertEquals(8, tableau.getRhsOffset());
    }

    @Test
    public void testTableauWithNegativeRestrictionFalseAndMinimization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1.0, 2.0 }, -3.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 2.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1.0e-6, 15);

        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(3, tableau.getNumDecisionVariables());
        Assert.assertEquals(1, tableau.getNumSlackVariables());
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(2, tableau.getHeight());
        Assert.assertEquals(6, tableau.getWidth());
    }

    @Test
    public void testNegativeConstraintNormalization() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, -1.0 }, Relationship.LEQ, -5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        List<LinearConstraint> normalized = tableau.normalizeConstraints(constraints);
        Assert.assertEquals(1, normalized.size());
        Assert.assertEquals(5.0, normalized.get(0).getValue(), 1.0e-6);
        Assert.assertEquals(Relationship.GEQ, normalized.get(0).getRelationship());
        Assert.assertEquals(-1.0, normalized.get(0).getCoefficients().getEntry(0), 1.0e-6);
        Assert.assertEquals(1.0, normalized.get(0).getCoefficients().getEntry(1), 1.0e-6);
    }

    @Test
    public void testGetInvertedCoefficientSum() {
        RealVector v = new ArrayRealVector(new double[] { 1.5, -2.5, 3.0 });
        double invertedSum = SimplexTableau.getInvertedCoefficientSum(v);
        Assert.assertEquals(-2.0, invertedSum, 1.0e-6);
    }

    @Test
    public void testGetBasicRow() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 2.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 3.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        Integer basicRowS0 = tableau.getBasicRow(3);
        Assert.assertEquals(Integer.valueOf(1), basicRowS0);

        Integer basicRowS1 = tableau.getBasicRow(4);
        Assert.assertEquals(Integer.valueOf(2), basicRowS1);

        Integer nonBasic = tableau.getBasicRow(1);
        Assert.assertNull(nonBasic);
    }

    @Test
    public void testRowOperations() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2.0, 4.0 }, Relationship.LEQ, 8.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        tableau.divideRow(1, 2.0);
        Assert.assertEquals(1.0, tableau.getEntry(1, 1), 1.0e-6);
        Assert.assertEquals(2.0, tableau.getEntry(1, 2), 1.0e-6);
        Assert.assertEquals(4.0, tableau.getEntry(1, tableau.getRhsOffset()), 1.0e-6);

        tableau.setEntry(0, 1, 3.0);
        Assert.assertEquals(3.0, tableau.getEntry(0, 1), 1.0e-6);

        tableau.subtractRow(0, 1, 3.0);
        Assert.assertEquals(0.0, tableau.getEntry(0, 1), 1.0e-6);
    }

    @Test
    public void testIsOptimal() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -1.0, -2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 10.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        Assert.assertFalse(tableau.isOptimal());

        tableau.setEntry(0, 1, 1.0);
        tableau.setEntry(0, 2, 2.0);
        Assert.assertTrue(tableau.isOptimal());
    }

    @Test
    public void testDropPhase1Objective() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.EQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());

        tableau.dropPhase1Objective();
        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
        Assert.assertEquals(0, tableau.getNumArtificialVariables());

        tableau.dropPhase1Objective();
        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());
    }

    @Test
    public void testGetSolutionNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, 3.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        tableau.setEntry(0, 1, 0.0);
        tableau.setEntry(0, 2, 0.0);

        RealPointValuePair solution = tableau.getSolution();
        Assert.assertEquals(2, solution.getPoint().length);
        Assert.assertEquals(4.0, solution.getPoint()[0], 1.0e-6);
        Assert.assertEquals(5.0, solution.getPoint()[1], 1.0e-6);
        Assert.assertEquals(23.0, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testGetSolutionUnrestrictedVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));
        constraints.add(new LinearConstraint(new double[] { 0.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        RealPointValuePair solution = tableau.getSolution();
        Assert.assertEquals(2, solution.getPoint().length);
        Assert.assertEquals(4.0, solution.getPoint()[0], 1.0e-6);
        Assert.assertEquals(5.0, solution.getPoint()[1], 1.0e-6);
    }

    @Test
    public void testGetData() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1.0 }, 0.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0 }, Relationship.LEQ, 2.0));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        double[][] data = tableau.getData();
        Assert.assertEquals(tableau.getHeight(), data.length);
        Assert.assertEquals(tableau.getWidth(), data[0].length);
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1.0, 2.0 }, 0.0);
        LinearObjectiveFunction f3 = new LinearObjectiveFunction(new double[] { 2.0, 1.0 }, 0.0);

        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));

        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1.0, 0.0 }, Relationship.LEQ, 4.0));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-6, 10);
        SimplexTableau t2 = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, 1.0e-6, 10);
        SimplexTableau t3 = new SimplexTableau(f3, c1, GoalType.MAXIMIZE, true, 1.0e-6, 10);
        SimplexTableau t4 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, 1.0e-6, 10);

        Assert.assertTrue(t1.equals(t1));
        Assert.assertTrue(t1.equals(t2));
        Assert.assertEquals(t1.hashCode(), t2.hashCode());

        Assert.assertFalse(t1.equals(null));
        Assert.assertFalse(t1.equals("other type"));
        Assert.assertFalse(t1.equals(t3));
        Assert.assertFalse(t1.equals(t4));
    }

    @Test
    public void testSerialization() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2.0, -1.0 }, 3.0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1.0, 1.0 }, Relationship.LEQ, 5.0));

        SimplexTableau original = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SimplexTableau deserialized = (SimplexTableau) ois.readObject();
        ois.close();

        Assert.assertEquals(original, deserialized);
        Assert.assertEquals(original.getHeight(), deserialized.getHeight());
        Assert.assertEquals(original.getWidth(), deserialized.getWidth());
    }
}
