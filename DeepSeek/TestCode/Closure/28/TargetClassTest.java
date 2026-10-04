package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

public class InlineCostEstimatorTest {

    @Test
    public void testGetCostWithNoThreshold() {
        Node root = new Node(1);
        int cost = InlineCostEstimator.getCost(root);
        assertTrue(cost >= 0);
    }

    @Test
    public void testGetCostWithThresholdNotExceeded() {
        Node root = new Node(1);
        int cost = InlineCostEstimator.getCost(root, 100);
        assertTrue(cost >= 0);
        assertTrue(cost <= 100);
    }

    @Test
    public void testGetCostWithThresholdExceeded() {
        Node root = new Node(1);
        int cost = InlineCostEstimator.getCost(root, 0);
        assertEquals(0, cost);
    }

    @Test
    public void testGetCostWithNegativeThreshold() {
        Node root = new Node(1);
        int cost = InlineCostEstimator.getCost(root, -1);
        assertEquals(0, cost);
    }

    @Test
    public void testGetCostWithNullRoot() {
        try {
            InlineCostEstimator.getCost(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetCostWithNullRootAndThreshold() {
        try {
            InlineCostEstimator.getCost(null, 10);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testEstimatedIdentifierCostConstant() {
        assertEquals(2, InlineCostEstimator.ESTIMATED_IDENTIFIER_COST);
    }

    @Test
    public void testGetCostWithThresholdExactlyMet() {
        Node root = new Node(1);
        int cost = InlineCostEstimator.getCost(root, 2);
        assertTrue(cost >= 0);
    }

    @Test
    public void testGetCostWithLargeThreshold() {
        Node root = new Node(1);
        int cost = InlineCostEstimator.getCost(root, Integer.MAX_VALUE);
        assertTrue(cost >= 0);
    }
}
