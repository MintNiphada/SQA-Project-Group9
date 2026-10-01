package org.apache.commons.collections.functors;

import org.apache.commons.collections.Predicate;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class EqualPredicateTest {

    @Test
    public void testFactoryWithNullReturnsNullPredicate() {
        Predicate<String> predicate = EqualPredicate.equalPredicate(null);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate instanceof NullPredicate);
        Assert.assertTrue(predicate.evaluate(null));
        Assert.assertFalse(predicate.evaluate("test"));
    }

    @Test
    public void testFactoryWithNonNullReturnsEqualPredicate() {
        String value = "hello";
        Predicate<String> predicate = EqualPredicate.equalPredicate(value);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate instanceof EqualPredicate);
        Assert.assertTrue(predicate.evaluate("hello"));
        Assert.assertFalse(predicate.evaluate("world"));
        Assert.assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testFactoryWithEquatorAndNullObjectReturnsNullPredicate() {
        Equator<String> equator = new Equator<String>() {
            @Override
            public boolean equate(String o1, String o2) {
                return o1 != null && o1.equalsIgnoreCase(o2);
            }

            @Override
            public int hash(String o) {
                return o == null ? 0 : o.toLowerCase().hashCode();
            }
        };

        Predicate<String> predicate = EqualPredicate.equalPredicate(null, equator);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate instanceof NullPredicate);
        Assert.assertTrue(predicate.evaluate(null));
        Assert.assertFalse(predicate.evaluate("hello"));
    }

    @Test
    public void testFactoryWithEquatorAndNonNullObject() {
        Equator<String> caseInsensitiveEquator = new Equator<String>() {
            @Override
            public boolean equate(String o1, String o2) {
                if (o1 == null) {
                    return o2 == null;
                }
                return o1.equalsIgnoreCase(o2);
            }

            @Override
            public int hash(String o) {
                return o == null ? 0 : o.toLowerCase().hashCode();
            }
        };

        Predicate<String> predicate = EqualPredicate.equalPredicate("HELLO", caseInsensitiveEquator);
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate instanceof EqualPredicate);
        Assert.assertTrue(predicate.evaluate("hello"));
        Assert.assertTrue(predicate.evaluate("HELLO"));
        Assert.assertTrue(predicate.evaluate("HeLLo"));
        Assert.assertFalse(predicate.evaluate("world"));
        Assert.assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testConstructorWithSingleArgument() {
        Integer target = 42;
        EqualPredicate<Integer> predicate = new EqualPredicate<Integer>(target);

        Assert.assertEquals(target, predicate.getValue());
        Assert.assertTrue(predicate.evaluate(42));
        Assert.assertTrue(predicate.evaluate(Integer.valueOf(42)));
        Assert.assertFalse(predicate.evaluate(43));
        Assert.assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testConstructorWithNullObject() {
        EqualPredicate<String> predicate = new EqualPredicate<String>(null);
        Assert.assertNull(predicate.getValue());
        Assert.assertTrue(predicate.evaluate(null));
        Assert.assertFalse(predicate.evaluate("not-null"));
    }

    @Test
    public void testConstructorWithTwoArguments() {
        Equator<Integer> mod10Equator = new Equator<Integer>() {
            @Override
            public boolean equate(Integer o1, Integer o2) {
                if (o1 == null || o2 == null) {
                    return o1 == o2;
                }
                return (o1 % 10) == (o2 % 10);
            }

            @Override
            public int hash(Integer o) {
                return o == null ? 0 : o % 10;
            }
        };

        EqualPredicate<Integer> predicate = new EqualPredicate<Integer>(15, mod10Equator);
        Assert.assertEquals(Integer.valueOf(15), predicate.getValue());
        Assert.assertTrue(predicate.evaluate(15));
        Assert.assertTrue(predicate.evaluate(25));
        Assert.assertTrue(predicate.evaluate(5));
        Assert.assertFalse(predicate.evaluate(14));
        Assert.assertFalse(predicate.evaluate(null));
    }

    @Test
    public void testGetValueReturnsExactStoredInstance() {
        Object obj = new Object();
        EqualPredicate<Object> predicate = new EqualPredicate<Object>(obj);
        Assert.assertSame(obj, predicate.getValue());
    }

    @Test
    public void testEvaluateWithSameInstance() {
        String testString = new String("sample");
        EqualPredicate<String> predicate = new EqualPredicate<String>(testString);
        Assert.assertTrue(predicate.evaluate(testString));
    }

    @Test
    public void testEvaluateWithDifferentObjectsWithEqualValues() {
        String str1 = new String("testString");
        String str2 = new String("testString");
        Assert.assertNotSame(str1, str2);

        EqualPredicate<String> predicate = new EqualPredicate<String>(str1);
        Assert.assertTrue(predicate.evaluate(str2));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        EqualPredicate<String> predicate = new EqualPredicate<String>("serializeTest");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(predicate);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EqualPredicate<String> deserialized = (EqualPredicate<String>) ois.readObject();

        Assert.assertNotNull(deserialized);
        Assert.assertEquals("serializeTest", deserialized.getValue());
        Assert.assertTrue(deserialized.evaluate("serializeTest"));
        Assert.assertFalse(deserialized.evaluate("other"));
    }
}
