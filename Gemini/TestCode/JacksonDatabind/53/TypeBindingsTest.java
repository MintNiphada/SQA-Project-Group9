package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TypeBindingsTest {

    static class CustomGeneric1<T> {}
    static class CustomGeneric2<K, V> {}
    static class CustomGeneric3<A, B, C> {}
    static class NonGeneric {}

    @Test
    public void testEmptyBindings() {
        TypeBindings b = TypeBindings.emptyBindings();
        Assert.assertTrue(b.isEmpty());
        Assert.assertEquals(0, b.size());
        Assert.assertNull(b.getBoundName(0));
        Assert.assertNull(b.getBoundName(-1));
        Assert.assertNull(b.getBoundType(0));
        Assert.assertNull(b.getBoundType(-1));
        Assert.assertNull(b.findBoundType("T"));
        Assert.assertFalse(b.hasUnbound("T"));
        Assert.assertEquals("<>", b.toString());
        Assert.assertEquals(Collections.emptyList(), b.getTypeParameters());
        Assert.assertEquals(0, b.typeParameterArray().length);
    }

    @Test
    public void testCreateFromList() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        JavaType intType = tf.constructType(Integer.class);

        TypeBindings b0 = TypeBindings.create(NonGeneric.class, (List<JavaType>) null);
        Assert.assertTrue(b0.isEmpty());

        TypeBindings bEmpty = TypeBindings.create(NonGeneric.class, Collections.<JavaType>emptyList());
        Assert.assertTrue(bEmpty.isEmpty());

        TypeBindings b1 = TypeBindings.create(List.class, Collections.singletonList(stringType));
        Assert.assertEquals(1, b1.size());
        Assert.assertEquals("E", b1.getBoundName(0));
        Assert.assertEquals(stringType, b1.getBoundType(0));

        TypeBindings b2 = TypeBindings.create(Map.class, Arrays.asList(stringType, intType));
        Assert.assertEquals(2, b2.size());
        Assert.assertEquals("K", b2.getBoundName(0));
        Assert.assertEquals("V", b2.getBoundName(1));
    }

    @Test
    public void testCreateFromNullOrEmptyArray() {
        TypeBindings b1 = TypeBindings.create(NonGeneric.class, (JavaType[]) null);
        Assert.assertTrue(b1.isEmpty());

        TypeBindings b2 = TypeBindings.create(NonGeneric.class, new JavaType[0]);
        Assert.assertTrue(b2.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArrayLengthMismatch0() {
        TypeBindings.create(List.class, new JavaType[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArrayLengthMismatchSingle() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        TypeBindings.create(NonGeneric.class, new JavaType[] { stringType });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateArrayLengthMismatchMultiple() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        TypeBindings.create(NonGeneric.class, new JavaType[] { stringType, stringType, stringType });
    }

    @Test
    public void testCreateWith3Parameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t1 = tf.constructType(String.class);
        JavaType t2 = tf.constructType(Integer.class);
        JavaType t3 = tf.constructType(Boolean.class);

        TypeBindings b = TypeBindings.create(CustomGeneric3.class, new JavaType[] { t1, t2, t3 });
        Assert.assertEquals(3, b.size());
        Assert.assertEquals("A", b.getBoundName(0));
        Assert.assertEquals("B", b.getBoundName(1));
        Assert.assertEquals("C", b.getBoundName(2));
        Assert.assertEquals(t1, b.getBoundType(0));
        Assert.assertEquals(t2, b.getBoundType(1));
        Assert.assertEquals(t3, b.getBoundType(2));
    }

    @Test
    public void testCreateWith1StashOptimization() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);

        TypeBindings b1 = TypeBindings.create(Collection.class, st);
        Assert.assertEquals(1, b1.size());
        Assert.assertEquals(st, b1.getBoundType(0));

        TypeBindings b2 = TypeBindings.create(List.class, st);
        Assert.assertEquals(1, b2.size());

        TypeBindings b3 = TypeBindings.create(ArrayList.class, st);
        Assert.assertEquals(1, b3.size());

        TypeBindings b4 = TypeBindings.create(AbstractList.class, st);
        Assert.assertEquals(1, b4.size());

        TypeBindings b5 = TypeBindings.create(Iterable.class, st);
        Assert.assertEquals(1, b5.size());

        TypeBindings b6 = TypeBindings.create(CustomGeneric1.class, st);
        Assert.assertEquals(1, b6.size());
        Assert.assertEquals("T", b6.getBoundName(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWith1Mismatch() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeBindings.create(Map.class, tf.constructType(String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWith1OnNonGeneric() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeBindings.create(NonGeneric.class, tf.constructType(String.class));
    }

    @Test
    public void testCreateWith2StashOptimization() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType k = tf.constructType(String.class);
        JavaType v = tf.constructType(Integer.class);

        TypeBindings b1 = TypeBindings.create(Map.class, k, v);
        Assert.assertEquals(2, b1.size());
        Assert.assertEquals("K", b1.getBoundName(0));
        Assert.assertEquals("V", b1.getBoundName(1));

        TypeBindings b2 = TypeBindings.create(HashMap.class, k, v);
        Assert.assertEquals(2, b2.size());

        TypeBindings b3 = TypeBindings.create(LinkedHashMap.class, k, v);
        Assert.assertEquals(2, b3.size());

        TypeBindings b4 = TypeBindings.create(CustomGeneric2.class, k, v);
        Assert.assertEquals(2, b4.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWith2Mismatch() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType k = tf.constructType(String.class);
        JavaType v = tf.constructType(Integer.class);
        TypeBindings.create(List.class, k, v);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWith2OnNonGeneric() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType k = tf.constructType(String.class);
        JavaType v = tf.constructType(Integer.class);
        TypeBindings.create(NonGeneric.class, k, v);
    }

    @Test
    public void testCreateIfNeeded1Arg() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);

        TypeBindings b1 = TypeBindings.createIfNeeded(NonGeneric.class, st);
        Assert.assertTrue(b1.isEmpty());

        TypeBindings b2 = TypeBindings.createIfNeeded(CustomGeneric1.class, st);
        Assert.assertEquals(1, b2.size());
        Assert.assertEquals(st, b2.getBoundType(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeeded1ArgMismatch() {
        TypeFactory tf = TypeFactory.defaultInstance();
        TypeBindings.createIfNeeded(Map.class, tf.constructType(String.class));
    }

    @Test
    public void testCreateIfNeededArray() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);
        JavaType it = tf.constructType(Integer.class);

        TypeBindings b1 = TypeBindings.createIfNeeded(NonGeneric.class, new JavaType[] { st });
        Assert.assertTrue(b1.isEmpty());

        TypeBindings b2 = TypeBindings.createIfNeeded(CustomGeneric2.class, new JavaType[] { st, it });
        Assert.assertEquals(2, b2.size());
        Assert.assertEquals(st, b2.getBoundType(0));
        Assert.assertEquals(it, b2.getBoundType(1));

        TypeBindings b3 = TypeBindings.createIfNeeded(NonGeneric.class, (JavaType[]) null);
        Assert.assertTrue(b3.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeededArrayMismatch() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);
        TypeBindings.createIfNeeded(CustomGeneric2.class, new JavaType[] { st });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIfNeededArrayMismatchNull() {
        TypeBindings.createIfNeeded(CustomGeneric2.class, (JavaType[]) null);
    }

    @Test
    public void testWithUnboundVariableAndHasUnbound() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);
        TypeBindings b = TypeBindings.create(List.class, st);

        Assert.assertFalse(b.hasUnbound("X"));

        TypeBindings b2 = b.withUnboundVariable("X");
        Assert.assertTrue(b2.hasUnbound("X"));
        Assert.assertFalse(b2.hasUnbound("Y"));

        TypeBindings b3 = b2.withUnboundVariable("Y");
        Assert.assertTrue(b3.hasUnbound("X"));
        Assert.assertTrue(b3.hasUnbound("Y"));
        Assert.assertFalse(b3.hasUnbound("Z"));
    }

    @Test
    public void testFindBoundType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);
        JavaType it = tf.constructType(Integer.class);
        TypeBindings b = TypeBindings.create(Map.class, st, it);

        Assert.assertEquals(st, b.findBoundType("K"));
        Assert.assertEquals(it, b.findBoundType("V"));
        Assert.assertNull(b.findBoundType("UNKNOWN"));
    }

    @Test
    public void testFindBoundTypeWithResolvedRecursiveType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ResolvedRecursiveType rrt = new ResolvedRecursiveType(CustomGeneric1.class, TypeBindings.emptyBindings());
        JavaType st = tf.constructType(String.class);

        TypeBindings b = TypeBindings.create(CustomGeneric1.class, rrt);
        Assert.assertEquals(rrt, b.findBoundType("T"));

        rrt.setReference(st);
        Assert.assertEquals(st, b.findBoundType("T"));
    }

    @Test
    public void testGetTypeParameters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);
        JavaType it = tf.constructType(Integer.class);
        TypeBindings b = TypeBindings.create(Map.class, st, it);

        List<JavaType> params = b.getTypeParameters();
        Assert.assertEquals(2, params.size());
        Assert.assertEquals(st, params.get(0));
        Assert.assertEquals(it, params.get(1));
    }

    @Test
    public void testToString() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);
        JavaType it = tf.constructType(Integer.class);
        TypeBindings b = TypeBindings.create(Map.class, st, it);

        String str = b.toString();
        Assert.assertTrue(str.startsWith("<"));
        Assert.assertTrue(str.endsWith(">"));
        Assert.assertTrue(str.contains(","));
    }

    @Test
    public void testEqualsAndHashCode() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType st = tf.constructType(String.class);
        JavaType it = tf.constructType(Integer.class);
        JavaType bt = tf.constructType(Boolean.class);

        TypeBindings b1 = TypeBindings.create(Map.class, st, it);
        TypeBindings b2 = TypeBindings.create(Map.class, st, it);
        TypeBindings b3 = TypeBindings.create(Map.class, st, bt);
        TypeBindings b4 = TypeBindings.create(List.class, st);

        Assert.assertEquals(b1, b1);
        Assert.assertEquals(b1, b2);
        Assert.assertEquals(b1.hashCode(), b2.hashCode());

        Assert.assertFalse(b1.equals(null));
        Assert.assertFalse(b1.equals("string"));
        Assert.assertFalse(b1.equals(b3));
        Assert.assertFalse(b1.equals(b4));
    }

    @Test
    public void testSerializationReadResolve() throws Exception {
        TypeBindings empty = TypeBindings.emptyBindings();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(empty);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object resolvedEmpty = ois.readObject();
        ois.close();

        Assert.assertSame(TypeBindings.emptyBindings(), resolvedEmpty);

        TypeFactory tf = TypeFactory.defaultInstance();
        TypeBindings nonEmpty = TypeBindings.create(List.class, tf.constructType(String.class));
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(nonEmpty);
        oos.close();

        bais = new ByteArrayInputStream(baos.toByteArray());
        ois = new ObjectInputStream(bais);
        Object resolvedNonEmpty = ois.readObject();
        ois.close();

        Assert.assertEquals(nonEmpty, resolvedNonEmpty);
    }
}
