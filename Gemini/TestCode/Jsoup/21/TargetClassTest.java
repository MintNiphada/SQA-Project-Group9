package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CombiningEvaluatorTest {

    private Element root;
    private Element element;

    private static class TrueEvaluator extends Evaluator {
        private final String name;

        TrueEvaluator(String name) {
            this.name = name;
        }

        TrueEvaluator() {
            this("true");
        }

        @Override
        public boolean matches(Element root, Element element) {
            return true;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class FalseEvaluator extends Evaluator {
        private final String name;

        FalseEvaluator(String name) {
            this.name = name;
        }

        FalseEvaluator() {
            this("false");
        }

        @Override
        public boolean matches(Element root, Element element) {
            return false;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Before
    public void setUp() {
        root = new Element(Tag.valueOf("div"), "");
        element = new Element(Tag.valueOf("span"), "");
        root.appendChild(element);
    }

    @Test
    public void testAndMatchesAllTrue() {
        CombiningEvaluator.And andEvaluator = new CombiningEvaluator.And(
                new TrueEvaluator("t1"),
                new TrueEvaluator("t2"),
                new TrueEvaluator("t3")
        );

        assertTrue(andEvaluator.matches(root, element));
    }

    @Test
    public void testAndMatchesWithOneFalse() {
        CombiningEvaluator.And andEvaluator = new CombiningEvaluator.And(
                new TrueEvaluator("t1"),
                new FalseEvaluator("f1"),
                new TrueEvaluator("t2")
        );

        assertFalse(andEvaluator.matches(root, element));
    }

    @Test
    public void testAndMatchesEmpty() {
        CombiningEvaluator.And andEvaluator = new CombiningEvaluator.And(Collections.<Evaluator>emptyList());
        assertTrue(andEvaluator.matches(root, element));
    }

    @Test
    public void testAndToString() {
        CombiningEvaluator.And andEvaluator = new CombiningEvaluator.And(
                new TrueEvaluator("div"),
                new TrueEvaluator(".class")
        );

        assertEquals("div .class", andEvaluator.toString());
    }

    @Test
    public void testAndConstructors() {
        List<Evaluator> list = Arrays.<Evaluator>asList(new TrueEvaluator("e1"), new FalseEvaluator("e2"));
        CombiningEvaluator.And andFromCollection = new CombiningEvaluator.And(list);
        assertEquals(2, andFromCollection.evaluators.size());

        CombiningEvaluator.And andFromVarargs = new CombiningEvaluator.And(new TrueEvaluator("e1"), new FalseEvaluator("e2"));
        assertEquals(2, andFromVarargs.evaluators.size());
    }

    @Test
    public void testOrConstructorEmpty() {
        List<Evaluator> emptyList = Collections.emptyList();
        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(emptyList);

        assertEquals(0, orEvaluator.evaluators.size());
        assertFalse(orEvaluator.matches(root, element));
    }

    @Test
    public void testOrConstructorSingleEvaluator() {
        List<Evaluator> singleList = Collections.<Evaluator>singletonList(new TrueEvaluator("t1"));
        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(singleList);

        assertEquals(1, orEvaluator.evaluators.size());
        assertTrue(orEvaluator.evaluators.get(0) instanceof TrueEvaluator);
        assertTrue(orEvaluator.matches(root, element));
    }

    @Test
    public void testOrConstructorMultipleEvaluatorsWrappedInAnd() {
        List<Evaluator> list = Arrays.<Evaluator>asList(new TrueEvaluator("t1"), new FalseEvaluator("f1"));
        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(list);

        assertEquals(1, orEvaluator.evaluators.size());
        assertTrue(orEvaluator.evaluators.get(0) instanceof CombiningEvaluator.And);
        // AND(true, false) is false, so OR should be false
        assertFalse(orEvaluator.matches(root, element));
    }

    @Test
    public void testOrAddAndMatches() {
        List<Evaluator> initialList = Arrays.<Evaluator>asList(new TrueEvaluator("t1"), new FalseEvaluator("f1"));
        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(initialList);

        // Initially matches is false
        assertFalse(orEvaluator.matches(root, element));

        // Add true evaluator as second OR branch
        orEvaluator.add(new TrueEvaluator("t2"));
        assertEquals(2, orEvaluator.evaluators.size());
        assertTrue(orEvaluator.matches(root, element));
    }

    @Test
    public void testOrMatchesAllFalse() {
        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(Collections.<Evaluator>emptyList());
        orEvaluator.add(new FalseEvaluator("f1"));
        orEvaluator.add(new FalseEvaluator("f2"));

        assertFalse(orEvaluator.matches(root, element));
    }

    @Test
    public void testOrToString() {
        List<Evaluator> list = new ArrayList<Evaluator>();
        list.add(new TrueEvaluator("div"));
        CombiningEvaluator.Or orEvaluator = new CombiningEvaluator.Or(list);

        assertEquals(":or[div]", orEvaluator.toString());

        orEvaluator.add(new TrueEvaluator("span"));
        assertEquals(":or[div, span]", orEvaluator.toString());
    }

    @Test
    public void testCombiningEvaluatorBaseConstructors() {
        CombiningEvaluator custom = new CombiningEvaluator() {
            @Override
            public boolean matches(Element root, Element node) {
                return false;
            }
        };
        assertTrue(custom.evaluators.isEmpty());

        List<Evaluator> evList = Arrays.<Evaluator>asList(new TrueEvaluator(), new FalseEvaluator());
        CombiningEvaluator customWithList = new CombiningEvaluator(evList) {
            @Override
            public boolean matches(Element root, Element node) {
                return false;
            }
        };
        assertEquals(2, customWithList.evaluators.size());
    }
}
