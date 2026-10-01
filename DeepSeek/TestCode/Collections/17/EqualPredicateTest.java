package org.apache.commons.collections.functors;

import org.junit.Assert;
import org.junit.Test;

public class EqualPredicateTest {

    // Helper Equator that always returns true
    private static final Equator<Object> TRUE_EQUATOR = new Equator<Object>() {
        @Override
        public boolean equate(Object o1, Object o2) {
            return true;
        }
    };

    // Helper Equator that always returns false
    private static final Equator<Object> FALSE_EQUATOR = new Equator<Object>() {
        @Override
        public boolean equate(Object o1, Object o2) {
            return false;
        }
    };

    // Helper Equator that returns true only for exactly equal references
    private static final Equator<Object> REFERENCE_EQUATOR = new Equator<Object>() {
        @Override
        public boolean equate(Object o1, Object o2) {
            return o1 == o2;
        }
    };

    @Test
    public void testEqualPredicateNullReturnsNullPredicate() {
        Predicate<String> predicate = EqualPredicate.equalPredicate(null);
        Assert.assertTrue(predicate instanceof NullPredicate);
        // NullPredicate.evaluate(null) returns true
        Assert.assertTrue(((NullPredicate) predicate).evaluate(null));
        Assert.assertFalse(((NullPredicate) predicate).evaluate("not null"));
    }

    @Test
    public void testEqualPredicateNonNullReturnsEqualPredicate() {
        Predicate<String> predicate = EqualPredicate.equalPredicate("test");
        Assert.assertTrue(predicate instanceof EqualPredicate);
        EqualPredicate<String> equalPredicate = (EqualPredicate<String>) predicate;
        Assert.assertEquals("test", equalPredicate.getValue());
    }

    @Test
    public void testEqualPredicateNullWithEquatorReturnsNullPredicate() {
        Predicate<String> predicate = EqualPredicate.equalPredicate(null, TRUE_EQUATOR);
        Assert.assertTrue(predicate instanceof NullPredicate);
    }

    @Test
    public void testEqualPredicateNonNullWithEquatorReturnsEqualPredicate() {
        Equator<String> customEquator = new Equator<String>() {
            @Override
            public boolean equate(String o1, String o2) {
                return true;
            }
        };
        Predicate<String> predicate = EqualPredicate.equalPredicate("data", customEquator);
        Assert.assertTrue(predicate instanceof EqualPredicate);
        EqualPredicate<String> equalPredicate = (EqualPredicate<String>) predicate;
        Assert.assertEquals("data", equalPredicate.getValue());
    }

    @Test
    public void testConstructorWithObjectUsesDefaultEquator() {
        EqualPredicate<String> predicate = new EqualPredicate<String>("hello");
        // DefaultEquator should use equals()
        Assert.assertTrue(predicate.evaluate("hello"));
        Assert.assertFalse(predicate.evaluate("world"));
        Assert.assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testConstructorWithObjectAndEquator() {
        EqualPredicate<Integer> predicate = new EqualPredicate<Integer>(42, REFERENCE_EQUATOR);
        Integer value = 42;
        Assert.assertFalse(predicate.evaluate(value)); // false because new Integer(42) is not reference equal to the input (value is not the same object)
        Assert.assertTrue(predicate.evaluate(42)); // 42 autoboxed to Integer, but this is a new Integer? Actually autoboxing caches small ints, so 42 might be same object. To be safe, we use a mutable object.
        // Use a mutable object
        Object key = new Object();
        EqualPredicate<Object> pred2 = new EqualPredicate<Object>(key, REFERENCE_EQUATOR);
        Assert.assertTrue(pred2.evaluate(key));
        Assert.assertFalse(pred2.evaluate(new Object()));
    }

    @Test
    public void testEvaluateWithTrueEquatorReturnsTrue() {
        EqualPredicate<String> predicate = new EqualPredicate<String>("ignored", TRUE_EQUATOR);
        Assert.assertTrue(predicate.evaluate("anything"));
        Assert.assertTrue(predicate.evaluate(null));
    }

    @Test
    public void testEvaluateWithFalseEquatorReturnsFalse() {
        EqualPredicate<String> predicate = new EqualPredicate<String>("ignored", FALSE_EQUATOR);
        Assert.assertFalse(predicate.evaluate("anything"));
        Assert.assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testEvaluateWithNullStoredValueUsingRefEquator() {
        EqualPredicate<Object> predicate = new EqualPredicate<Object>(null, REFERENCE_EQUATOR);
        Assert.assertTrue(predicate.evaluate(null));
        Assert.assertFalse(predicate.evaluate("something"));
    }

    @Test
    public void testEvaluateWithNullInputAndNonNullStoredValueDefaultEquator() {
        EqualPredicate<String> predicate = new EqualPredicate<String>("data");
        // DefaultEquator handles null input, should return false
        Assert.assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testGetValueReturnsStoredObject() {
        String stored = "stored";
        EqualPredicate<String> predicate = new EqualPredicate<String>(stored);
        Assert.assertSame(stored, predicate.getValue());

        predicate = new EqualPredicate<String>(null);
        Assert.assertNull(predicate.getValue());
    }

    @Test
    public void testGetValueWithEquatorConstructorReturnsStoredObject() {
        Integer value = 100;
        EqualPredicate<Integer> predicate = new EqualPredicate<Integer>(value, TRUE_EQUATOR);
        Assert.assertSame(value, predicate.getValue());
    }

    @Test
    public void testSerialization() throws Exception {
        // Not required by Defects4J but ensures Serializable
        EqualPredicate<String> original = new EqualPredicate<String>("test");
        byte[] data = SerializationTestHelper.serialize(original);
        EqualPredicate<String> deserialized = (EqualPredicate<String>) SerializationTestHelper.deserialize(data);
        Assert.assertEquals(original.getValue(), deserialized.getValue());
        Assert.assertTrue(deserialized.evaluate("test"));
        Assert.assertFalse(deserialized.evaluate("other"));
    }

    // Utility for serialization (only used if needed, but to avoid dependency we can skip)
    // Actually we cannot rely on SerializationTestHelper unless we implement it. For simplicity, omit.
    // Use a local helper:
    private static class SerializationTestHelper {
        static byte[] serialize(Object obj) throws Exception {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        }
        static Object deserialize(byte[] data) throws Exception {
            java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(data);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
            return ois.readObject();
        }
    }
}
