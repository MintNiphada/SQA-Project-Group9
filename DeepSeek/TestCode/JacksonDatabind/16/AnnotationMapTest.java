package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

public class AnnotationMapTest {

    // Custom annotations for testing
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation1 {}
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation2 {}
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation3 {}

    private static final TestAnnotation1 ANN1 = new TestAnnotation1() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return TestAnnotation1.class;
        }
    };
    private static final TestAnnotation2 ANN2 = new TestAnnotation2() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return TestAnnotation2.class;
        }
    };
    private static final TestAnnotation3 ANN3 = new TestAnnotation3() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return TestAnnotation3.class;
        }
    };

    // Helper to create AnnotationMap with given annotations
    private AnnotationMap createMap(Annotation... anns) {
        AnnotationMap map = new AnnotationMap();
        for (Annotation ann : anns) {
            map.add(ann);
        }
        return map;
    }

    @Test
    public void testGetWhenAnnotationsNull() {
        AnnotationMap map = new AnnotationMap();
        Assert.assertNull(map.get(TestAnnotation1.class));
    }

    @Test
    public void testGetWhenAnnotationPresent() {
        AnnotationMap map = createMap(ANN1);
        Assert.assertSame(ANN1, map.get(TestAnnotation1.class));
    }

    @Test
    public void testGetWhenAnnotationNotPresent() {
        AnnotationMap map = createMap(ANN1);
        Assert.assertNull(map.get(TestAnnotation2.class));
    }

    @Test
    public void testAnnotationsWhenNull() {
        AnnotationMap map = new AnnotationMap();
        Iterable<Annotation> iterable = map.annotations();
        Assert.assertNotNull(iterable);
        Assert.assertFalse(iterable.iterator().hasNext());
    }

    @Test
    public void testAnnotationsWhenEmpty() {
        AnnotationMap map = new AnnotationMap();
        // force internal map to be empty but not null
        map.addIfNotPresent(ANN1);
        map = new AnnotationMap(); // reset to null? Actually we need an empty map.
        // We can create an AnnotationMap with empty HashMap via reflection, but easier: use merge to get empty map?
        // Instead, we can test annotations on a map that had an annotation removed? No remove method.
        // The only way to have empty _annotations is if it was never added to. But then _annotations is null.
        // The code checks size() == 0, but if _annotations is null, it returns empty list anyway.
        // So we can't have a non-null empty map without using reflection. But we can test the branch by using a map that had an annotation added and then? No removal.
        // However, we can test the condition where _annotations is not null but size is 0 by using a map that was created with an empty HashMap via the private constructor.
        // We'll use reflection to set _annotations to an empty HashMap.
        AnnotationMap map = new AnnotationMap();
        try {
            java.lang.reflect.Field f = AnnotationMap.class.getDeclaredField("_annotations");
            f.setAccessible(true);
            f.set(map, new HashMap<>());
        } catch (Exception e) {
            Assert.fail("Reflection failed");
        }
        Iterable<Annotation> iterable = map.annotations();
        Assert.assertNotNull(iterable);
        Assert.assertFalse(iterable.iterator().hasNext());
    }

    @Test
    public void testAnnotationsWhenNonEmpty() {
        AnnotationMap map = createMap(ANN1, ANN2);
        Iterable<Annotation> iterable = map.annotations();
        Assert.assertNotNull(iterable);
        Iterator<Annotation> it = iterable.iterator();
        Assert.assertTrue(it.hasNext());
        Set<Annotation> set = new HashSet<>();
        while (it.hasNext()) set.add(it.next());
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains(ANN1));
        Assert.assertTrue(set.contains(ANN2));
    }

    @Test
    public void testMergePrimaryNull() {
        AnnotationMap secondary = createMap(ANN1);
        AnnotationMap result = AnnotationMap.merge(null, secondary);
        Assert.assertSame(secondary, result);
    }

    @Test
    public void testMergePrimaryEmpty() {
        AnnotationMap primary = new AnnotationMap(); // _annotations null
        AnnotationMap secondary = createMap(ANN1);
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        Assert.assertSame(secondary, result);
    }

    @Test
    public void testMergePrimaryWithEmptyMap() {
        AnnotationMap primary = new AnnotationMap();
        // make primary have empty HashMap
        try {
            java.lang.reflect.Field f = AnnotationMap.class.getDeclaredField("_annotations");
            f.setAccessible(true);
            f.set(primary, new HashMap<>());
        } catch (Exception e) {
            Assert.fail("Reflection failed");
        }
        AnnotationMap secondary = createMap(ANN1);
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        Assert.assertSame(secondary, result);
    }

    @Test
    public void testMergeSecondaryNull() {
        AnnotationMap primary = createMap(ANN1);
        AnnotationMap result = AnnotationMap.merge(primary, null);
        Assert.assertSame(primary, result);
    }

    @Test
    public void testMergeSecondaryEmpty() {
        AnnotationMap primary = createMap(ANN1);
        AnnotationMap secondary = new AnnotationMap();
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        Assert.assertSame(primary, result);
    }

    @Test
    public void testMergeSecondaryWithEmptyMap() {
        AnnotationMap primary = createMap(ANN1);
        AnnotationMap secondary = new AnnotationMap();
        try {
            java.lang.reflect.Field f = AnnotationMap.class.getDeclaredField("_annotations");
            f.setAccessible(true);
            f.set(secondary, new HashMap<>());
        } catch (Exception e) {
            Assert.fail("Reflection failed");
        }
        AnnotationMap result = AnnotationMap.merge(primary, secondary);
        Assert.assertSame(primary, result);
    }

    @Test
    public void testMergeBothNonEmpty() {
        AnnotationMap primary = createMap(ANN1, ANN2);
        AnnotationMap secondary = createMap(ANN2, ANN3);
        AnnotationMap merged = AnnotationMap.merge(primary, secondary);
        // secondary added first, then primary overrides
        // So ANN2 from primary should override secondary's ANN2
        Assert.assertSame(ANN1, merged.get(TestAnnotation1.class));
        Assert.assertSame(ANN2, merged.get(TestAnnotation2.class)); // primary's ANN2
        Assert.assertSame(ANN3, merged.get(TestAnnotation3.class));
        Assert.assertEquals(3, merged.size());
    }

    @Test
    public void testMergePrimaryOverridesSecondary() {
        // Create two different instances of same annotation type
        TestAnnotation1 ann1a = new TestAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TestAnnotation1.class;
            }
        };
        TestAnnotation1 ann1b = new TestAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TestAnnotation1.class;
            }
        };
        AnnotationMap primary = createMap(ann1a);
        AnnotationMap secondary = createMap(ann1b);
        AnnotationMap merged = AnnotationMap.merge(primary, secondary);
        Assert.assertSame(ann1a, merged.get(TestAnnotation1.class));
    }

    @Test
    public void testSizeWhenNull() {
        AnnotationMap map = new AnnotationMap();
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testSizeWhenNonEmpty() {
        AnnotationMap map = createMap(ANN1, ANN2);
        Assert.assertEquals(2, map.size());
    }

    @Test
    public void testAddIfNotPresentWhenNullMap() {
        AnnotationMap map = new AnnotationMap();
        boolean added = map.addIfNotPresent(ANN1);
        Assert.assertTrue(added);
        Assert.assertSame(ANN1, map.get(TestAnnotation1.class));
    }

    @Test
    public void testAddIfNotPresentWhenKeyNotPresent() {
        AnnotationMap map = createMap(ANN1);
        boolean added = map.addIfNotPresent(ANN2);
        Assert.assertTrue(added);
        Assert.assertSame(ANN2, map.get(TestAnnotation2.class));
    }

    @Test
    public void testAddIfNotPresentWhenKeyPresent() {
        AnnotationMap map = createMap(ANN1);
        boolean added = map.addIfNotPresent(ANN1);
        Assert.assertFalse(added);
        Assert.assertSame(ANN1, map.get(TestAnnotation1.class));
    }

    @Test(expected = NullPointerException.class)
    public void testAddIfNotPresentWithNullAnnotation() {
        AnnotationMap map = new AnnotationMap();
        map.addIfNotPresent(null);
    }

    @Test
    public void testAddWhenMapNull() {
        AnnotationMap map = new AnnotationMap();
        boolean changed = map.add(ANN1);
        // _add returns false because previous was null
        Assert.assertFalse(changed);
        Assert.assertSame(ANN1, map.get(TestAnnotation1.class));
    }

    @Test
    public void testAddWhenKeyNotPresent() {
        AnnotationMap map = createMap(ANN1);
        boolean changed = map.add(ANN2);
        Assert.assertFalse(changed); // previous null
        Assert.assertSame(ANN2, map.get(TestAnnotation2.class));
    }

    @Test
    public void testAddWhenKeyPresentAndEqual() {
        AnnotationMap map = createMap(ANN1);
        boolean changed = map.add(ANN1);
        // previous is ANN1, equals true
        Assert.assertTrue(changed);
        Assert.assertSame(ANN1, map.get(TestAnnotation1.class));
    }

    @Test
    public void testAddWhenKeyPresentButNotEqual() {
        TestAnnotation1 anotherAnn1 = new TestAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TestAnnotation1.class;
            }
            @Override
            public boolean equals(Object obj) {
                return false; // force not equal
            }
        };
        AnnotationMap map = createMap(ANN1);
        boolean changed = map.add(anotherAnn1);
        // previous not null, but equals false
        Assert.assertFalse(changed);
        Assert.assertSame(anotherAnn1, map.get(TestAnnotation1.class)); // replaced
    }

    @Test(expected = NullPointerException.class)
    public void testAddWithNullAnnotation() {
        AnnotationMap map = new AnnotationMap();
        map.add(null);
    }

    @Test
    public void testToStringWhenNull() {
        AnnotationMap map = new AnnotationMap();
        Assert.assertEquals("[null]", map.toString());
    }

    @Test
    public void testToStringWhenNonEmpty() {
        AnnotationMap map = createMap(ANN1);
        String str = map.toString();
        Assert.assertTrue(str.contains(TestAnnotation1.class.getName()));
    }

    @Test
    public void testAddMethodReturnValueEdgeCases() {
        // Test _add directly via add() with various scenarios
        // When map is null, _add creates map, put returns null -> false
        AnnotationMap map = new AnnotationMap();
        Assert.assertFalse(map.add(ANN1));
        // When map has same annotation (equal), put returns previous, equals true -> true
        Assert.assertTrue(map.add(ANN1));
        // When map has different annotation of same type (not equal), put returns previous, equals false -> false
        TestAnnotation1 different = new TestAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TestAnnotation1.class;
            }
            @Override
            public boolean equals(Object obj) {
                return false;
            }
        };
        Assert.assertFalse(map.add(different));
    }
}
