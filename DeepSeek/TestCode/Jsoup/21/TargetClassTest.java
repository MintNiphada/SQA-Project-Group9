package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TargetClassTest {

    private Element mockRoot;
    private Element mockNode;

    // A simple Evaluator implementation for testing purposes
    private static class MockEvaluator extends Evaluator {
        private final boolean result;
        private final String name;

        public MockEvaluator(boolean result, String name) {
            this.result = result;
            this.name = name;
        }

        @Override
        public boolean matches(Element root, Element node) {
            return result;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Before
    public void setUp() {
        mockRoot = new Element("root");
        mockNode = new Element("node");
    }

    // --- Tests for CombiningEvaluator base class (via subclasses) ---

    @Test
    public void testAndConstructorWithEmptyCollection() {
        Collection<Evaluator> empty = new ArrayList<>();
        CombiningEvaluator.And and = new CombiningEvaluator.And(empty);
        assertNotNull(and.evaluators);
        assertTrue(and.evaluators.isEmpty());
    }

    @Test
    public void testAndConstructorWithVarargs() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        CombiningEvaluator.And and = new CombiningEvaluator.And(e1, e2);
        assertEquals(2, and.evaluators.size());
        assertTrue(and.evaluators.contains(e1));
        assertTrue(and.evaluators.contains(e2));
    }

    @Test
    public void testAndMatchesAllTrue() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        CombiningEvaluator.And and = new CombiningEvaluator.And(e1, e2);
        assertTrue(and.matches(mockRoot, mockNode));
    }

    @Test
    public void testAndMatchesOneFalse() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(false, "e2");
        CombiningEvaluator.And and = new CombiningEvaluator.And(e1, e2);
        assertFalse(and.matches(mockRoot, mockNode));
    }

    @Test
    public void testAndMatchesAllFalse() {
        Evaluator e1 = new MockEvaluator(false, "e1");
        Evaluator e2 = new MockEvaluator(false, "e2");
        CombiningEvaluator.And and = new CombiningEvaluator.And(e1, e2);
        assertFalse(and.matches(mockRoot, mockNode));
    }

    @Test
    public void testAndMatchesEmpty() {
        // An empty AND should logically return true (vacuous truth)
        CombiningEvaluator.And and = new CombiningEvaluator.And(new ArrayList<>());
        assertTrue(and.matches(mockRoot, mockNode));
    }

    @Test
    public void testAndToString() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        CombiningEvaluator.And and = new CombiningEvaluator.And(e1, e2);
        // StringUtil.join(evaluators, " ") -> "e1 e2"
        assertEquals("e1 e2", and.toString());
    }

    @Test
    public void testOrConstructorWithSingleEvaluator() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        Collection<Evaluator> coll = Collections.singletonList(e1);
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(coll);
        assertEquals(1, or.evaluators.size());
        assertTrue(or.evaluators.contains(e1));
    }

    @Test
    public void testOrConstructorWithMultipleEvaluators() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        Collection<Evaluator> coll = Arrays.asList(e1, e2);
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(coll);
        
        // Logic: if size > 1, wrap in And. So evaluators list should have 1 element (the And wrapper)
        assertEquals(1, or.evaluators.size());
        assertTrue(or.evaluators.get(0) instanceof CombiningEvaluator.And);
        
        CombiningEvaluator.And innerAnd = (CombiningEvaluator.And) or.evaluators.get(0);
        assertEquals(2, innerAnd.evaluators.size());
        assertTrue(innerAnd.evaluators.contains(e1));
        assertTrue(innerAnd.evaluators.contains(e2));
    }

    @Test
    public void testOrConstructorWithEmptyCollection() {
        Collection<Evaluator> empty = new ArrayList<>();
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(empty);
        assertTrue(or.evaluators.isEmpty());
    }

    @Test
    public void testOrAdd() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<>());
        Evaluator e1 = new MockEvaluator(true, "e1");
        or.add(e1);
        assertEquals(1, or.evaluators.size());
        assertTrue(or.evaluators.contains(e1));
    }

    @Test
    public void testOrMatchesEmpty() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<>());
        // An empty OR should logically return false
        assertFalse(or.matches(mockRoot, mockNode));
    }

    @Test
    public void testOrMatchesSingleTrue() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(e1));
        assertTrue(or.matches(mockRoot, mockNode));
    }

    @Test
    public void testOrMatchesSingleFalse() {
        Evaluator e1 = new MockEvaluator(false, "e1");
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(e1));
        assertFalse(or.matches(mockRoot, mockNode));
    }

    @Test
    public void testOrMatchesMultipleViaAndWrapper() {
        // When constructed with multiple, they are ANDed.
        // So for OR to match, the inner AND must match.
        // Inner AND matches if all are true.
        
        // Case 1: All true -> Inner AND true -> OR true
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(e1, e2));
        assertTrue(or.matches(mockRoot, mockNode));

        // Case 2: One false -> Inner AND false -> OR false
        Evaluator e3 = new MockEvaluator(false, "e3");
        CombiningEvaluator.Or or2 = new CombiningEvaluator.Or(Arrays.asList(e1, e3));
        assertFalse(or2.matches(mockRoot, mockNode));
    }

    @Test
    public void testOrMatchesAfterAdd() {
        // Construct with empty, then add evaluators directly.
        // These are NOT wrapped in And because they are added via add() method, 
        // bypassing the constructor logic that wraps >1 initial evaluators.
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<>());
        Evaluator e1 = new MockEvaluator(false, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        
        or.add(e1);
        or.add(e2);
        
        // OR logic: returns true if ANY evaluator matches.
        // e1 is false, e2 is true.
        assertTrue(or.matches(mockRoot, mockNode));
    }

    @Test
    public void testOrToString() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Collections.singletonList(e1));
        // String.format(":or%s", evaluators)
        // evaluators is a List containing e1. List.toString() is "[e1]"
        assertEquals(":or[e1]", or.toString());
    }

    @Test
    public void testOrToStringWithMultipleAdded() {
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(new ArrayList<>());
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        or.add(e1);
        or.add(e2);
        // List contains [e1, e2]
        assertEquals(":or[e1, e2]", or.toString());
    }
    
    @Test
    public void testOrToStringWithConstructorMultiple() {
        Evaluator e1 = new MockEvaluator(true, "e1");
        Evaluator e2 = new MockEvaluator(true, "e2");
        CombiningEvaluator.Or or = new CombiningEvaluator.Or(Arrays.asList(e1, e2));
        // The list contains one element: the And wrapper.
        // And.toString() returns "e1 e2"
        // So the List.toString() will be "[e1 e2]"
        // Result: ":or[e1 e2]"
        assertEquals(":or[e1 e2]", or.toString());
    }
}
