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
    public void testTableauCreationAndBasicGettersMaximize() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 5);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 4));
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.GEQ, 1));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        Assert.assertEquals(2, tableau.getNumVariables());
        Assert.assertEquals(2, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(2, tableau.getNumSlackVariables());
        Assert.assertEquals(2, tableau.getNumArtificialVariables());
        Assert.assertEquals(2, tableau.getNumObjectiveFunctions());

        Assert.assertEquals(4, tableau.getSlackVariableOffset());
        Assert.assertEquals(6, tableau.getArtificialVariableOffset());
        Assert.assertEquals(8, tableau.getRhsOffset());
        Assert.assertEquals(9, tableau.getWidth());
        Assert.assertEquals(5, tableau.getHeight());

        double[][] data = tableau.getData();
        Assert.assertNotNull(data);
        Assert.assertEquals(5, data.length);
        Assert.assertEquals(9, data[0].length);
    }

    @Test
    public void testTableauMinimizeUnrestrictedVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { -2, 1 }, -3);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MINIMIZE, false, 1.0e-6);

        Assert.assertEquals(2, tableau.getNumVariables());
        Assert.assertEquals(3, tableau.getNumDecisionVariables());
        Assert.assertEquals(2, tableau.getOriginalNumDecisionVariables());
        Assert.assertEquals(1, tableau.getNumSlackVariables());
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(1, tableau.getNumObjectiveFunctions());

        Assert.assertEquals(4, tableau.getSlackVariableOffset());
        Assert.assertEquals(4, tableau.getArtificialVariableOffset());
        Assert.assertEquals(5, tableau.getRhsOffset());
        Assert.assertEquals(6, tableau.getWidth());
        Assert.assertEquals(2, tableau.getHeight());
    }

    @Test
    public void testConstraintNormalizationWithNegativeRhs() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, -2 }, Relationship.LEQ, -5));
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, -3));
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, -2));
        constraints.add(new LinearConstraint(new double[] { 3, 4 }, Relationship.LEQ, 10));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        List<LinearConstraint> normalized = tableau.getNormalizedConstraints();

        Assert.assertEquals(4, normalized.size());
        Assert.assertEquals(5.0, normalized.get(0).getValue(), 1.0e-6);
        Assert.assertEquals(Relationship.GEQ, normalized.get(0).getRelationship());
        Assert.assertEquals(3.0, normalized.get(1).getValue(), 1.0e-6);
        Assert.assertEquals(Relationship.LEQ, normalized.get(1).getRelationship());
        Assert.assertEquals(2.0, normalized.get(2).getValue(), 1.0e-6);
        Assert.assertEquals(Relationship.EQ, normalized.get(2).getRelationship());
        Assert.assertEquals(10.0, normalized.get(3).getValue(), 1.0e-6);
        Assert.assertEquals(Relationship.LEQ, normalized.get(3).getRelationship());
    }

    @Test
    public void testInvertedCoefficientSum() {
        RealVector v = new ArrayRealVector(new double[] { 1.5, -2.5, 4.0 });
        double sum = SimplexTableau.getInvertedCoeffiecientSum(v);
        Assert.assertEquals(-3.0, sum, 1.0e-6);
    }

    @Test
    public void testRowOperationsAndSetEntry() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 2, 4 }, Relationship.LEQ, 8));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);

        tableau.setEntry(1, 0, 4.0);
        Assert.assertEquals(4.0, tableau.getEntry(1, 0), 1.0e-6);

        tableau.divideRow(1, 2.0);
        Assert.assertEquals(2.0, tableau.getEntry(1, 0), 1.0e-6);

        tableau.setEntry(0, 0, 10.0);
        tableau.subtractRow(0, 1, 3.0);
        Assert.assertEquals(4.0, tableau.getEntry(0, 0), 1.0e-6);
    }

    @Test
    public void testDiscardArtificialVariables() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.EQ, 2));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        Assert.assertEquals(1, tableau.getNumArtificialVariables());
        int originalWidth = tableau.getWidth();
        int originalHeight = tableau.getHeight();

        tableau.discardArtificialVariables();
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
        Assert.assertEquals(originalWidth - 2, tableau.getWidth());
        Assert.assertEquals(originalHeight - 1, tableau.getHeight());

        tableau.discardArtificialVariables();
        Assert.assertEquals(0, tableau.getNumArtificialVariables());
    }

    @Test
    public void testGetSolutionBasicAndNonNegative() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 3 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.EQ, 3));
        constraints.add(new LinearConstraint(new double[] { 0, 1 }, Relationship.EQ, 4));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, true, 1.0e-6);
        tableau.discardArtificialVariables();

        RealPointValuePair solution = tableau.getSolution();
        Assert.assertNotNull(solution);
        double[] point = solution.getPoint();
        Assert.assertEquals(2, point.length);
        Assert.assertEquals(3.0, point[0], 1.0e-6);
        Assert.assertEquals(4.0, point[1], 1.0e-6);
        Assert.assertEquals(18.0, solution.getValue(), 1.0e-6);
    }

    @Test
    public void testGetSolutionWithRestrictedVariablesAndDuplicateBasis() {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 1, 1 }, 0);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 1 }, Relationship.LEQ, 5));

        SimplexTableau tableau = new SimplexTableau(f, constraints, GoalType.MAXIMIZE, false, 1.0e-6);
        RealPointValuePair solution = tableau.getSolution();
        Assert.assertNotNull(solution);
        Assert.assertEquals(2, solution.getPoint().length);
    }

    @Test
    public void testEqualsAndHashCode() {
        LinearObjectiveFunction f1 = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        LinearObjectiveFunction f2 = new LinearObjectiveFunction(new double[] { 1, 2 }, 0);
        LinearObjectiveFunction f3 = new LinearObjectiveFunction(new double[] { 2, 1 }, 0);

        List<LinearConstraint> c1 = new ArrayList<LinearConstraint>();
        c1.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 1));

        List<LinearConstraint> c2 = new ArrayList<LinearConstraint>();
        c2.add(new LinearConstraint(new double[] { 1, 0 }, Relationship.LEQ, 1));

        SimplexTableau t1 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau t2 = new SimplexTableau(f2, c2, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau t3 = new SimplexTableau(f3, c1, GoalType.MAXIMIZE, true, 1.0e-6);
        SimplexTableau t4 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, false, 1.0e-6);
        SimplexTableau t5 = new SimplexTableau(f1, c1, GoalType.MAXIMIZE, true, 1.0e-4);

        Assert.assertTrue(t1.equals(t1));
        Assert.assertTrue(t1.equals(t2));
        Assert.assertEquals(t1.hashCode(), t2.hashCode());

        Assert.assertFalse(t1.equals(null));
        Assert.assertFalse(t1.equals("different_type"));
        Assert.assertFalse(t1.equals(t3));
        Assert.assertFalse(t1.equals(t4));
        Assert.assertFalse(t1.equals(t5));
    }

    @Test
    public void testSerialization() throws Exception {
        LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 3, 5 }, 10);
        List<LinearConstraint> constraints = new ArrayList<LinearConstraint>();
        constraints.add(new LinearConstraint(new double[] { 1, 2 }, Relationship.LEQ, 8));
        constraints.add(new LinearConstraint(new double[] { 2, 1 }, Relationship.GEQ, 3));

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
        Assert.assertEquals(tableau.getWidth(), deserialized.getWidth());
        Assert.assertEquals(tableau.getHeight(), deserialized.getHeight());
        Assert.assertEquals(tableau.getEntry(0, 0), deserialized.getEntry(0, 0), 1.0e-6);
    }
}
