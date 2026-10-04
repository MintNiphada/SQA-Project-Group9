package org.apache.commons.lang3.builder;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;

public class HashCodeBuilderTest {

    static class SimpleObject {
        private int intField;
        protected String stringField;
        public boolean boolField;
        public SimpleObject(int intField, String stringField, boolean boolField) {
            this.intField = intField;
            this.stringField = stringField;
            this.boolField = boolField;
        }
    }

    static class ObjectWithTransient {
        private transient int transientField;
        private int normalField;
        public ObjectWithTransient(int transientField, int normalField) {
            this.transientField = transientField;
            this.normalField = normalField;
        }
    }

    static class ObjectWithStatic {
        private static int staticField = 100;
        private int normalField;
        public ObjectWithStatic(int normalField) {
            this.normalField = normalField;
        }
    }

    static class ObjectWithDollar {
        private int $dollarField;
        private int normalField;
        public ObjectWithDollar(int $dollarField, int normalField) {
            this.$dollarField = $dollarField;
            this.normalField = normalField;
        }
    }

    static class SelfReferencingObject {
        private SelfReferencingObject self;
        private int value;
        public SelfReferencingObject(int value) {
            this.value = value;
            this.self = this;
        }
    }

    static class Parent {
        private int parentField;
        public Parent(int parentField) {
            this.parentField = parentField;
        }
    }

    static class Child extends Parent {
        private int childField;
        public Child(int parentField, int childField) {
            super(parentField);
            this.childField = childField;
        }
    }

    static class EmptyClass {
    }

    @Test
    public void testDefaultConstructor() {
        HashCodeBuilder builder = new HashCodeBuilder();
        assertEquals(17, builder.toHashCode());
    }

    @Test
    public void testParameterizedConstructorValid() {
        HashCodeBuilder builder = new HashCodeBuilder(3, 5);
        assertEquals(3, builder.toHashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInitialZero() {
        new HashCodeBuilder(0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInitialEven() {
        new HashCodeBuilder(2, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMultiplierZero() {
        new HashCodeBuilder(3, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorMultiplierEven() {
        new HashCodeBuilder(3, 4);
    }

    @Test
    public void testAppendBooleanTrue() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(true);
        assertEquals(17 * 37 + 0, builder.toHashCode());
    }

    @Test
    public void testAppendBooleanFalse() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(false);
        assertEquals(17 * 37 + 1, builder.toHashCode());
    }

    @Test
    public void testAppendBooleanArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((boolean[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendBooleanArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new boolean[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendBooleanArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new boolean[]{true, false});
        int expected = 17 * 37 + 0;
        expected = expected * 37 + 1;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendByte() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((byte) 5);
        assertEquals(17 * 37 + 5, builder.toHashCode());
    }

    @Test
    public void testAppendByteArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((byte[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendByteArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new byte[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendByteArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new byte[]{1, 2});
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendChar() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append('A');
        assertEquals(17 * 37 + 'A', builder.toHashCode());
    }

    @Test
    public void testAppendCharArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((char[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendCharArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new char[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendCharArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new char[]{'a', 'b'});
        int expected = 17 * 37 + 'a';
        expected = expected * 37 + 'b';
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendDouble() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(1.5);
        long bits = Double.doubleToLongBits(1.5);
        int expected = 17 * 37 + (int) (bits ^ (bits >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendDoubleArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((double[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendDoubleArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new double[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendDoubleArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new double[]{1.0, 2.0});
        long bits1 = Double.doubleToLongBits(1.0);
        int expected = 17 * 37 + (int) (bits1 ^ (bits1 >> 32));
        long bits2 = Double.doubleToLongBits(2.0);
        expected = expected * 37 + (int) (bits2 ^ (bits2 >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendFloat() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(1.5f);
        int expected = 17 * 37 + Float.floatToIntBits(1.5f);
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendFloatArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((float[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendFloatArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new float[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendFloatArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new float[]{1.0f, 2.0f});
        int expected = 17 * 37 + Float.floatToIntBits(1.0f);
        expected = expected * 37 + Float.floatToIntBits(2.0f);
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendInt() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(10);
        assertEquals(17 * 37 + 10, builder.toHashCode());
    }

    @Test
    public void testAppendIntArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((int[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendIntArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new int[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendIntArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new int[]{1, 2});
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendLong() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(10L);
        int expected = 17 * 37 + (int) (10L ^ (10L >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendLongArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((long[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendLongArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new long[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendLongArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new long[]{1L, 2L});
        int expected = 17 * 37 + (int) (1L ^ (1L >> 32));
        expected = expected * 37 + (int) (2L ^ (2L >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendShort() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((short) 5);
        assertEquals(17 * 37 + 5, builder.toHashCode());
    }

    @Test
    public void testAppendShortArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((short[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendShortArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new short[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendShortArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new short[]{1, 2});
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((Object) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendObjectNonArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        String s = "test";
        builder.append(s);
        assertEquals(17 * 37 + s.hashCode(), builder.toHashCode());
    }

    @Test
    public void testAppendObjectIntArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        int[] array = {1, 2};
        builder.append((Object) array);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectLongArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        long[] array = {1L, 2L};
        builder.append((Object) array);
        int expected = 17 * 37 + (int) (1L ^ (1L >> 32));
        expected = expected * 37 + (int) (2L ^ (2L >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectShortArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        short[] array = {1, 2};
        builder.append((Object) array);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectCharArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        char[] array = {'a', 'b'};
        builder.append((Object) array);
        int expected = 17 * 37 + 'a';
        expected = expected * 37 + 'b';
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectByteArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        byte[] array = {1, 2};
        builder.append((Object) array);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectDoubleArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        double[] array = {1.0, 2.0};
        builder.append((Object) array);
        long bits1 = Double.doubleToLongBits(1.0);
        int expected = 17 * 37 + (int) (bits1 ^ (bits1 >> 32));
        long bits2 = Double.doubleToLongBits(2.0);
        expected = expected * 37 + (int) (bits2 ^ (bits2 >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectFloatArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        float[] array = {1.0f, 2.0f};
        builder.append((Object) array);
        int expected = 17 * 37 + Float.floatToIntBits(1.0f);
        expected = expected * 37 + Float.floatToIntBits(2.0f);
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectBooleanArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        boolean[] array = {true, false};
        builder.append((Object) array);
        int expected = 17 * 37 + 0;
        expected = expected * 37 + 1;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectObjectArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        Object[] array = {"a", "b"};
        builder.append((Object) array);
        int expected = 17 * 37 + "a".hashCode();
        expected = expected * 37 + "b".hashCode();
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectMultiDimensionalArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        int[][] multi = {{1, 2}, {3}};
        builder.append((Object) multi);
        int expected = 17 * 37;
        for (int[] sub : multi) {
            for (int val : sub) {
                expected = expected * 37 + val;
            }
        }
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectArrayNull() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append((Object[]) null);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendObjectArrayEmpty() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new Object[0]);
        assertEquals(17 * 37, builder.toHashCode());
    }

    @Test
    public void testAppendSuper() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.appendSuper(100);
        assertEquals(17 * 37 + 100, builder.toHashCode());
    }

    @Test
    public void testToHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(1);
        assertEquals(builder.toHashCode(), builder.hashCode());
    }

    @Test
    public void testHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(1);
        assertEquals(builder.toHashCode(), builder.hashCode());
    }

    @Test
    public void testChaining() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        int result = builder.append(1).append(2).toHashCode();
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, result);
    }

    @Test
    public void testReflectionHashCodeSimple() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + (obj.stringField == null ? 0 : obj.stringField.hashCode());
        expected = expected * 37 + (obj.boolField ? 0 : 1);
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeWithTransientsFalse() {
        ObjectWithTransient obj = new ObjectWithTransient(5, 10);
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        int expected = 17 * 37 + 10;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeWithTransientsTrue() {
        ObjectWithTransient obj = new ObjectWithTransient(5, 10);
        int hash = HashCodeBuilder.reflectionHashCode(obj, true);
        int expected = 17 * 37 + 5;
        expected = expected * 37 + 10;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeExcludeFieldsArray() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        String[] excludes = {"intField", "boolField"};
        int hash = HashCodeBuilder.reflectionHashCode(obj, excludes);
        int expected = 17 * 37 + obj.stringField.hashCode();
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeExcludeFieldsCollection() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        Collection<String> excludes = Arrays.asList("intField", "boolField");
        int hash = HashCodeBuilder.reflectionHashCode(obj, excludes);
        int expected = 17 * 37 + obj.stringField.hashCode();
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeExcludeFieldsNullCollection() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        int hash = HashCodeBuilder.reflectionHashCode(obj, (Collection<String>) null);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + obj.stringField.hashCode();
        expected = expected * 37 + (obj.boolField ? 0 : 1);
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeStaticFieldExcluded() {
        ObjectWithStatic obj = new ObjectWithStatic(5);
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        int expected = 17 * 37 + 5;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeDollarFieldExcluded() {
        ObjectWithDollar obj = new ObjectWithDollar(100, 200);
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        int expected = 17 * 37 + 200;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeSelfReference() {
        SelfReferencingObject obj = new SelfReferencingObject(10);
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        int expected = 17 * 37 + 10;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeInheritance() {
        Child child = new Child(1, 2);
        int hash = HashCodeBuilder.reflectionHashCode(child);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeReflectUpToClass() {
        Child child = new Child(1, 2);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, child, false, Parent.class, null);
        int expected = 17 * 37 + 1;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeReflectUpToClassNull() {
        Child child = new Child(1, 2);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, child, false, null, null);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeReflectUpToClassSameAsObject() {
        Child child = new Child(1, 2);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, child, false, Child.class, null);
        int expected = 17 * 37 + 2;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeEmptyClass() {
        EmptyClass obj = new EmptyClass();
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        assertEquals(17 * 37, hash);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObject() {
        HashCodeBuilder.reflectionHashCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeNullObjectWithParams() {
        HashCodeBuilder.reflectionHashCode(17, 37, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeInitialZero() {
        HashCodeBuilder.reflectionHashCode(0, 37, new SimpleObject(1, "a", true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeInitialEven() {
        HashCodeBuilder.reflectionHashCode(2, 37, new SimpleObject(1, "a", true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeMultiplierZero() {
        HashCodeBuilder.reflectionHashCode(17, 0, new SimpleObject(1, "a", true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReflectionHashCodeMultiplierEven() {
        HashCodeBuilder.reflectionHashCode(17, 2, new SimpleObject(1, "a", true));
    }

    @Test
    public void testRegistry() {
        Object obj = new Object();
        assertFalse(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.register(obj);
        assertTrue(HashCodeBuilder.isRegistered(obj));
        HashCodeBuilder.unregister(obj);
        assertFalse(HashCodeBuilder.isRegistered(obj));
    }

    @Test
    public void testGetRegistry() {
        Set<IDKey> registry = HashCodeBuilder.getRegistry();
        assertNotNull(registry);
        assertTrue(registry.isEmpty());
    }

    @Test
    public void testReflectionHashCodeClearsRegistry() {
        SimpleObject obj = new SimpleObject(1, "a", true);
        HashCodeBuilder.reflectionHashCode(obj);
        assertFalse(HashCodeBuilder.isRegistered(obj));
    }

    @Test
    public void testReflectionHashCodeWithExcludeFieldsArrayNull() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, null);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + obj.stringField.hashCode();
        expected = expected * 37 + (obj.boolField ? 0 : 1);
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeWithExcludeFieldsEmpty() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, new String[0]);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + obj.stringField.hashCode();
        expected = expected * 37 + (obj.boolField ? 0 : 1);
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeWithExcludeFieldsNonExistent() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, new String[]{"nonexistent"});
        int expected = 17 * 37 + 1;
        expected = expected * 37 + obj.stringField.hashCode();
        expected = expected * 37 + (obj.boolField ? 0 : 1);
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeWithTestTransientsAndExcludes() {
        ObjectWithTransient obj = new ObjectWithTransient(5, 10);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, obj, true, null, new String[]{"normalField"});
        int expected = 17 * 37 + 5;
        assertEquals(expected, hash);
    }

    @Test
    public void testAppendObjectArrayWithNullElements() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        Object[] array = {null, "a"};
        builder.append(array);
        int expected = 17 * 37;
        expected = expected * 37 + "a".hashCode();
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendBooleanArrayWithAllValues() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(new boolean[]{true, false, true});
        int expected = 17 * 37 + 0;
        expected = expected * 37 + 1;
        expected = expected * 37 + 0;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendIntMinMax() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(Integer.MAX_VALUE);
        builder.append(Integer.MIN_VALUE);
        int expected = 17 * 37 + Integer.MAX_VALUE;
        expected = expected * 37 + Integer.MIN_VALUE;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendLongMinMax() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(Long.MAX_VALUE);
        builder.append(Long.MIN_VALUE);
        int expected = 17 * 37 + (int) (Long.MAX_VALUE ^ (Long.MAX_VALUE >> 32));
        expected = expected * 37 + (int) (Long.MIN_VALUE ^ (Long.MIN_VALUE >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendFloatNaN() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(Float.NaN);
        int expected = 17 * 37 + Float.floatToIntBits(Float.NaN);
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendDoubleNaN() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.append(Double.NaN);
        long bits = Double.doubleToLongBits(Double.NaN);
        int expected = 17 * 37 + (int) (bits ^ (bits >> 32));
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testAppendObjectWithArrayOfPrimitiveWrappers() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        Integer[] array = {1, 2};
        builder.append((Object) array);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + 2;
        assertEquals(expected, builder.toHashCode());
    }

    @Test
    public void testReflectionHashCodeWithNullExcludeFieldsArray() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, null);
        int expected = 17 * 37 + 1;
        expected = expected * 37 + obj.stringField.hashCode();
        expected = expected * 37 + (obj.boolField ? 0 : 1);
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeWithReflectUpToClassAndExcludes() {
        Child child = new Child(1, 2);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, child, false, Parent.class, new String[]{"parentField"});
        assertEquals(17 * 37, hash);
    }

    @Test
    public void testReflectionHashCodeWithAllParams() {
        Child child = new Child(1, 2);
        int hash = HashCodeBuilder.reflectionHashCode(17, 37, child, true, null, new String[]{"childField"});
        int expected = 17 * 37 + 1;
        assertEquals(expected, hash);
    }

    @Test
    public void testReflectionHashCodeObjectWithNoFields() {
        Object obj = new Object();
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        assertEquals(17 * 37, hash);
    }

    @Test
    public void testReflectionHashCodeConsistency() {
        SimpleObject obj = new SimpleObject(1, "test", true);
        int hash1 = HashCodeBuilder.reflectionHashCode(obj);
        int hash2 = HashCodeBuilder.reflectionHashCode(obj);
        assertEquals(hash1, hash2);
    }

    @Test
    public void testReflectionHashCodeDifferentObjects() {
        SimpleObject obj1 = new SimpleObject(1, "a", true);
        SimpleObject obj2 = new SimpleObject(2, "b", false);
        assertNotEquals(HashCodeBuilder.reflectionHashCode(obj1), HashCodeBuilder.reflectionHashCode(obj2));
    }

    @Test
    public void testAppendObjectWithArrayOfArrays() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        Object[] array = {new int[]{1, 2}, new String[]{"a"}};
        builder.append(array);
        int expected = 17 * 37;
        for (int val : new int[]{1, 2}) {
            expected = expected * 37 + val;
        }
        expected = expected * 37 + "a".hashCode();
        assertEquals(expected, builder.toHashCode());
    }
}
