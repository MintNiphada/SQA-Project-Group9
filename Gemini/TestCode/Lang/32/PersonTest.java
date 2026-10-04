package org.apache.commons.lang3.builder;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class HashCodeBuilderTest {

    static class TestObjectParent {
        private int parentInt = 10;
        private transient int parentTransient = 20;
        private static int parentStatic = 30;
    }

    static class TestObjectChild extends TestObjectParent {
        private String childStr = "test";
        private transient double childTransient = 3.14;
        private static String childStatic = "static";
        private int excludedField = 99;
    }

    static class TestObjectCycle {
        TestObjectCycle other;
        int value = 42;
    }

    static class TestObjectWithArrays {
        boolean[] bools = new boolean[]{true, false};
        byte[] bytes = new byte[]{1, 2};
        char[] chars = new char[]{'a', 'b'};
        double[] doubles = new double[]{1.1, 2.2};
        float[] floats = new float[]{3.3f, 4.4f};
        int[] ints = new int[]{5, 6};
        long[] longs = new long[]{7L, 8L};
        short[] shorts = new short[]{9, 10};
        Object[] objs = new Object[]{"sub", null};
    }

    @Test
    public void testDefaultConstructor() {
        HashCodeBuilder builder = new HashCodeBuilder();
        Assert.assertEquals(17, builder.toHashCode());
        Assert.assertEquals(17, builder.hashCode());
    }

    @Test
    public void testCustomConstructorValid() {
        HashCodeBuilder builder = new HashCodeBuilder(3, 5);
        Assert.assertEquals(3, builder.toHashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroInitial() {
        new HashCodeBuilder(0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEvenInitial() {
        new HashCodeBuilder(2, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorZeroMultiplier() {
        new HashCodeBuilder(3, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEvenMultiplier() {
        new HashCodeBuilder(3, 4);
    }

    @Test
    public void testAppendBoolean() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append(true);
        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(false);
        Assert.assertEquals(17 * 37 + 0, hcb1.toHashCode());
        Assert.assertEquals(17 * 37 + 1, hcb2.toHashCode());
    }

    @Test
    public void testAppendBooleanArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((boolean[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(new boolean[]{true, false});
        int expected = (17 * 37 + 0) * 37 + 1;
        Assert.assertEquals(expected, hcb2.toHashCode());
    }

    @Test
    public void testAppendByte() {
        byte b = 12;
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(b);
        Assert.assertEquals(17 * 37 + 12, hcb.toHashCode());
    }

    @Test
    public void testAppendByteArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((byte[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(new byte[]{1, 2});
        int expected = (17 * 37 + 1) * 37 + 2;
        Assert.assertEquals(expected, hcb2.toHashCode());
    }

    @Test
    public void testAppendChar() {
        char c = 'a';
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(c);
        Assert.assertEquals(17 * 37 + (int) 'a', hcb.toHashCode());
    }

    @Test
    public void testAppendCharArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((char[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(new char[]{'a', 'b'});
        int expected = (17 * 37 + (int) 'a') * 37 + (int) 'b';
        Assert.assertEquals(expected, hcb2.toHashCode());
    }

    @Test
    public void testAppendDouble() {
        double d = 12.34;
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(d);
        long bits = Double.doubleToLongBits(d);
        int expected = 17 * 37 + ((int) (bits ^ (bits >> 32)));
        Assert.assertEquals(expected, hcb.toHashCode());
    }

    @Test
    public void testAppendDoubleArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((double[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        double[] array = new double[]{1.0, 2.0};
        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(array);
        HashCodeBuilder hcbExpected = new HashCodeBuilder(17, 37).append(1.0).append(2.0);
        Assert.assertEquals(hcbExpected.toHashCode(), hcb2.toHashCode());
    }

    @Test
    public void testAppendFloat() {
        float f = 12.34f;
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(f);
        Assert.assertEquals(17 * 37 + Float.floatToIntBits(f), hcb.toHashCode());
    }

    @Test
    public void testAppendFloatArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((float[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(new float[]{1.0f, 2.0f});
        int expected = (17 * 37 + Float.floatToIntBits(1.0f)) * 37 + Float.floatToIntBits(2.0f);
        Assert.assertEquals(expected, hcb2.toHashCode());
    }

    @Test
    public void testAppendInt() {
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(42);
        Assert.assertEquals(17 * 37 + 42, hcb.toHashCode());
    }

    @Test
    public void testAppendIntArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((int[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(new int[]{10, 20});
        int expected = (17 * 37 + 10) * 37 + 20;
        Assert.assertEquals(expected, hcb2.toHashCode());
    }

    @Test
    public void testAppendLong() {
        long l = 123456789012345L;
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(l);
        int expected = 17 * 37 + ((int) (l ^ (l >> 32)));
        Assert.assertEquals(expected, hcb.toHashCode());
    }

    @Test
    public void testAppendLongArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((long[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(new long[]{100L, 200L});
        HashCodeBuilder expected = new HashCodeBuilder(17, 37).append(100L).append(200L);
        Assert.assertEquals(expected.toHashCode(), hcb2.toHashCode());
    }

    @Test
    public void testAppendShort() {
        short s = 25;
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(s);
        Assert.assertEquals(17 * 37 + 25, hcb.toHashCode());
    }

    @Test
    public void testAppendShortArray() {
        HashCodeBuilder hcb1 = new HashCodeBuilder(17, 37).append((short[]) null);
        Assert.assertEquals(17 * 37, hcb1.toHashCode());

        HashCodeBuilder hcb2 = new HashCodeBuilder(17, 37).append(new short[]{3, 4});
        int expected = (17 * 37 + 3) * 37 + 4;
        Assert.assertEquals(expected, hcb2.toHashCode());
    }

    @Test
    public void testAppendSuper() {
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).appendSuper(100);
        Assert.assertEquals(17 * 37 + 100, hcb.toHashCode());
    }

    @Test
    public void testAppendObjectNull() {
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append((Object) null);
        Assert.assertEquals(17 * 37, hcb.toHashCode());
    }

    @Test
    public void testAppendObjectNormal() {
        String s = "hello";
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append((Object) s);
        Assert.assertEquals(17 * 37 + s.hashCode(), hcb.toHashCode());
    }

    @Test
    public void testAppendObjectArrayNull() {
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append((Object[]) null);
        Assert.assertEquals(17 * 37, hcb.toHashCode());
    }

    @Test
    public void testAppendObjectArray() {
        Object[] array = new Object[]{"a", null, "b"};
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37).append(array);
        int expected = ((17 * 37 + "a".hashCode()) * 37 + 0) * 37 + "b".hashCode();
        Assert.assertEquals(expected, hcb.toHashCode());
    }

    @Test
    public void testAppendMultiDimensionalPrimitiveArraysViaObject() {
        Object longArr = new long[][]{{1L}, {2L}};
        Object intArr = new int[][]{{1}, {2}};
        Object shortArr = new short[][]{{1}, {2}};
        Object charArr = new char[][]{{'a'}, {'b'}};
        Object byteArr = new byte[][]{{1}, {2}};
        Object doubleArr = new double[][]{{1.0}, {2.0}};
        Object floatArr = new float[][]{{1.0f}, {2.0f}};
        Object boolArr = new boolean[][]{{true}, {false}};

        HashCodeBuilder bLong = new HashCodeBuilder(17, 37).append(longArr);
        HashCodeBuilder bInt = new HashCodeBuilder(17, 37).append(intArr);
        HashCodeBuilder bShort = new HashCodeBuilder(17, 37).append(shortArr);
        HashCodeBuilder bChar = new HashCodeBuilder(17, 37).append(charArr);
        HashCodeBuilder bByte = new HashCodeBuilder(17, 37).append(byteArr);
        HashCodeBuilder bDouble = new HashCodeBuilder(17, 37).append(doubleArr);
        HashCodeBuilder bFloat = new HashCodeBuilder(17, 37).append(floatArr);
        HashCodeBuilder bBool = new HashCodeBuilder(17, 37).append(boolArr);

        Assert.assertNotEquals(17, bLong.toHashCode());
        Assert.assertNotEquals(17, bInt.toHashCode());
        Assert.assertNotEquals(17, bShort.toHashCode());
        Assert.assertNotEquals(17, bChar.toHashCode());
        Assert.assertNotEquals(17, bByte.toHashCode());
        Assert.assertNotEquals(17, bDouble.toHashCode());
        Assert.assertNotEquals(17, bFloat.toHashCode());
        Assert.assertNotEquals(17, bBool.toHashCode());
    }

    @Test
    public void testAppendObjectPrimitiveArraysDirectlyViaAppendObject() {
        HashCodeBuilder hcb = new HashCodeBuilder(17, 37);
        hcb.append((Object) new long[]{1L});
        hcb.append((Object) new int[]{2});
        hcb.append((Object) new short[]{3});
        hcb.append((Object) new char[]{'a'});
        hcb.append((Object) new byte[]{4});
        hcb.append((Object) new double[]{5.0});
        hcb.append((Object) new float[]{6.0f});
        hcb.append((Object) new boolean[]{true});
        Assert.assertNotEquals(17, hcb.toHashCode());
    }

    @Test
    public void testReflectionHashCodeBasic() {
        TestObjectChild obj = new TestObjectChild();
        int hash1 = HashCodeBuilder.reflectionHashCode(obj);
        int hash2 = HashCodeBuilder.reflectionHashCode(17, 37, obj);
        Assert.assertEquals(hash1, hash2);
    }

    @Test
    public void testReflectionHashCodeWithTransients() {
        TestObjectChild obj = new TestObjectChild();
        int hashWithoutTransients = HashCodeBuilder.reflectionHashCode(obj, false);
        int hashWithTransients = HashCodeBuilder.reflectionHashCode(obj, true);
        int hashWithTransientsExplicit = HashCodeBuilder.reflectionHashCode(17, 37, obj, true);
        Assert.assertEquals(hashWithTransients, hashWithTransientsExplicit);
        Assert.assertNotEquals(hashWithoutTransients, hashWithTransients);
    }

    @Test
    public void testReflectionHashCodeWithReflectUpToClass() {
        TestObjectChild obj = new TestObjectChild();
        int hashChildOnly = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, TestObjectChild.class);
        int hashAll = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, Object.class);
        Assert.assertNotEquals(hashChildOnly, hashAll);

        int hashNullSuper = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null);
        Assert.assertEquals(hashAll, hashNullSuper);
    }

    @Test
    public void testReflectionHashCodeWithExcludeFieldsArray() {
        TestObjectChild obj = new TestObjectChild();
        int hashNormal = HashCodeBuilder.reflectionHashCode(obj);
        int hashExcluded = HashCodeBuilder.reflectionHashCode(obj, new String[]{"excludedField"});
        Assert.assertNotEquals(hashNormal, hashExcluded);
    }

    @Test
    public void testReflectionHashCodeWithExcludeFieldsCollection() {
        TestObjectChild obj = new TestObjectChild();
        List<String> list = new ArrayList<String>();
        list.add("excludedField");
        int hashArray = HashCodeBuilder.reflectionHashCode(obj, new String[]{"excludedField"});
        int hashCollection = HashCodeBuilder.reflectionHashCode(obj, list);
        Assert.assertEquals(hashArray, hashCollection);

        int hashNullCollection = HashCodeBuilder.reflectionHashCode(obj, (List<String>) null);
        int hashNullArray = HashCodeBuilder.reflectionHashCode(obj, (String[]) null);
        Assert.assertEquals(hashNullArray, hashNullCollection);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject1() {
        HashCodeBuilder.reflectionHashCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject2() {
        HashCodeBuilder.reflectionHashCode(null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject3() {
        HashCodeBuilder.reflectionHashCode(null, new String[]{"field"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject4() {
        HashCodeBuilder.reflectionHashCode(null, Collections.singletonList("field"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject5() {
        HashCodeBuilder.reflectionHashCode(17, 37, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject6() {
        HashCodeBuilder.reflectionHashCode(17, 37, null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject7() {
        HashCodeBuilder.reflectionHashCode(17, 37, null, true, Object.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject8() {
        HashCodeBuilder.reflectionHashCode(17, 37, null, true, Object.class, new String[0]);
    }

    @Test
    public void testReflectionRegistryCycleDetection() {
        TestObjectCycle objA = new TestObjectCycle();
        TestObjectCycle objB = new TestObjectCycle();
        objA.other = objB;
        objB.other = objA;

        int hashA = HashCodeBuilder.reflectionHashCode(objA);
        int hashB = HashCodeBuilder.reflectionHashCode(objB);
        Assert.assertEquals(hashA, hashB);
        Assert.assertFalse(HashCodeBuilder.isRegistered(objA));
        Assert.assertFalse(HashCodeBuilder.isRegistered(objB));
    }

    @Test
    public void testRegistryOperations() {
        Object dummy = new Object();
        Assert.assertFalse(HashCodeBuilder.isRegistered(dummy));
        HashCodeBuilder.register(dummy);
        Assert.assertTrue(HashCodeBuilder.isRegistered(dummy));
        Set<IDKey> registry = HashCodeBuilder.getRegistry();
        Assert.assertNotNull(registry);
        Assert.assertTrue(registry.contains(new IDKey(dummy)));
        HashCodeBuilder.unregister(dummy);
        Assert.assertFalse(HashCodeBuilder.isRegistered(dummy));
    }

    @Test
    public void testReflectionWithComplexFields() {
        TestObjectWithArrays obj = new TestObjectWithArrays();
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        Assert.assertNotEquals(0, hash);
    }

    @Test
    public void testReflectionWithAnonymousOrInnerClass() {
        Object inner = new Object() {
            private int x = 5;
        };
        int hash = HashCodeBuilder.reflectionHashCode(inner);
        Assert.assertNotEquals(0, hash);
    }
}
