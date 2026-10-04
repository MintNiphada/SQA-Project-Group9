package com.fasterxml.jackson.databind.introspect;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;

public class AnnotationMapTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface Ann1 {
        String value() default "1";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Ann2 {
        int value() default 2;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Ann3 {
        boolean value() default true;
    }

    @Ann1("first")
    @Ann2(10)
    @Ann3(true)
    private static class Sample1 {}

    @Ann1("second")
    @Ann2(20)
    private static class Sample2 {}

    private Ann1 getAnn1Sample1() {
        return Sample1.class.getAnnotation(Ann1.class);
    }

    private Ann2 getAnn2Sample1() {
        return Sample1.class.getAnnotation(Ann2.class);
    }

    private Ann3 getAnn3Sample1() {
        return Sample1.class.getAnnotation(Ann3.class);
    }

    private Ann1 getAnn1Sample2() {
        return Sample2.class.getAnnotation(Ann1.class);
    }

    private Ann2 getAnn2Sample2() {
        return Sample2.class.getAnnotation(Ann2.class);
    }

    @Test
    public void testEmptyAnnotationMap() {
        AnnotationMap map = new AnnotationMap();

        Assert.assertEquals(0, map.size());
        Assert.assertNull(map.get(Ann1.class));
        Assert.assertEquals("[null]", map.toString());

        Iterable<Annotation> iterable = map.annotations();
        Assert.assertNotNull(iterable);
        Assert.assertFalse(iterable.iterator().hasNext());
    }

    @Test
    public void testAddAndGet() {
        AnnotationMap map = new AnnotationMap();
        Ann1 ann1 = getAnn1Sample1();

        // Adding for the first time: previous is null, returns false
        boolean result1 = map.add(ann1);
        Assert.assertFalse(result1);
        Assert.assertEquals(1, map.size());
        Assert.assertSame(ann1, map.get(Ann1.class));
        Assert.assertNull(map.get(Ann2.class));

        // Re-adding the same annotation: previous is equal, returns true
        boolean result2 = map.add(ann1);
        Assert.assertTrue(result2);
        Assert.assertEquals(1, map.size());

        // Adding another annotation of a different type
        Ann2 ann2 = getAnn2Sample1();
        boolean result3 = map.add(ann2);
        Assert.assertFalse(result3);
        Assert.assertEquals(2, map.size());
        Assert.assertSame(ann2, map.get(Ann2.class));
    }

    @Test
    public void testAddReplacingDifferentValue() {
        AnnotationMap map = new AnnotationMap();
        Ann1 ann1_1 = getAnn1Sample1();
        Ann1 ann1_2 = getAnn1Sample2();

        map.add(ann1_1);
        Assert.assertEquals("first", map.get(Ann1.class).value());

        // Replacing with a different annotation instance with different value
        boolean result = map.add(ann1_2);
        Assert.assertFalse(result); // previous.equals(ann1_2) is false
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("second", map.get(Ann1.class).value());
    }

    @Test
    public void testAddIfNotPresent() {
        AnnotationMap map = new AnnotationMap();
        Ann1 ann1_1 = getAnn1Sample1();
        Ann1 ann1_2 = getAnn1Sample2();

        // First addition when _annotations is null
        boolean added1 = map.addIfNotPresent(ann1_1);
        Assert.assertTrue(added1);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("first", map.get(Ann1.class).value());

        // Second addition of the same type when already present
        boolean added2 = map.addIfNotPresent(ann1_2);
        Assert.assertFalse(added2);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("first", map.get(Ann1.class).value());

        // Addition of a new type
        Ann2 ann2 = getAnn2Sample1();
        boolean added3 = map.addIfNotPresent(ann2);
        Assert.assertTrue(added3);
        Assert.assertEquals(2, map.size());
        Assert.assertSame(ann2, map.get(Ann2.class));
    }

    @Test
    public void testAnnotationsIterable() {
        AnnotationMap map = new AnnotationMap();
        map.add(getAnn1Sample1());
        map.add(getAnn2Sample1());

        Iterable<Annotation> iterable = map.annotations();
        Assert.assertNotNull(iterable);

        int count = 0;
        for (Annotation ann : iterable) {
            Assert.assertNotNull(ann);
            count++;
        }
        Assert.assertEquals(2, count);
    }

    @Test
    public void testToString() {
        AnnotationMap map = new AnnotationMap();
        Assert.assertEquals("[null]", map.toString());

        Ann1 ann1 = getAnn1Sample1();
        map.add(ann1);
        String str = map.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains(Ann1.class.getName()) || str.contains("Ann1"));
    }

    @Test
    public void testMergeWithNullOrEmpty() {
        AnnotationMap map1 = new AnnotationMap();
        AnnotationMap map2 = new AnnotationMap();

        // null and null
        Assert.assertNull(AnnotationMap.merge(null, null));

        // null and empty
        Assert.assertSame(map2, AnnotationMap.merge(null, map2));
        Assert.assertNull(AnnotationMap.merge(map1, null));

        // empty and empty
        Assert.assertSame(map2, AnnotationMap.merge(map1, map2));

        // non-empty and null
        map1.add(getAnn1Sample1());
        Assert.assertSame(map1, AnnotationMap.merge(map1, null));

        // null and non-empty
        map2.add(getAnn2Sample1());
        Assert.assertSame(map2, AnnotationMap.merge(null, map2));

        // empty and non-empty
        AnnotationMap emptyMap = new AnnotationMap();
        Assert.assertSame(map2, AnnotationMap.merge(emptyMap, map2));

        // non-empty and empty
        Assert.assertSame(map1, AnnotationMap.merge(map1, emptyMap));
    }

    @Test
    public void testMergeBothNonEmpty() {
        AnnotationMap primary = new AnnotationMap();
        primary.add(getAnn1Sample1()); // Ann1 = "first"
        primary.add(getAnn3Sample1()); // Ann3 = true

        AnnotationMap secondary = new AnnotationMap();
        secondary.add(getAnn1Sample2()); // Ann1 = "second"
        secondary.add(getAnn2Sample2()); // Ann2 = 20

        AnnotationMap merged = AnnotationMap.merge(primary, secondary);

        Assert.assertNotNull(merged);
        Assert.assertEquals(3, merged.size());

        // Primary should override secondary for Ann1
        Assert.assertEquals("first", merged.get(Ann1.class).value());
        // Secondary Ann2 should be included
        Assert.assertEquals(20, merged.get(Ann2.class).value());
        // Primary Ann3 should be included
        Assert.assertTrue(merged.get(Ann3.class).value());
    }

    @Test
    public void testProtectedAddMethod() {
        AnnotationMap map = new AnnotationMap();
        Ann1 ann1 = getAnn1Sample1();

        boolean firstAdd = map._add(ann1);
        Assert.assertFalse(firstAdd);

        boolean secondAdd = map._add(ann1);
        Assert.assertTrue(secondAdd);
    }
}
