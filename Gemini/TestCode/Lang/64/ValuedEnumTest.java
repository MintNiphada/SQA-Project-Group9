package org.apache.commons.lang.enums;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ValuedEnumTest {

    private static final class TestValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final TestValuedEnum ONE = new TestValuedEnum("One", 1);
        public static final TestValuedEnum TWO = new TestValuedEnum("Two", 2);
        public static final TestValuedEnum THREE = new TestValuedEnum("Three", 3);

        private TestValuedEnum(String name, int value) {
            super(name, value);
        }

        public static TestValuedEnum getEnum(int value) {
            return (TestValuedEnum) getEnum(TestValuedEnum.class, value);
        }

        public static TestValuedEnum getEnum(String name) {
            return (TestValuedEnum) getEnum(TestValuedEnum.class, name);
        }

        public static Map getEnumMap() {
            return getEnumMap(TestValuedEnum.class);
        }

        public static List getEnumList() {
            return getEnumList(TestValuedEnum.class);
        }

        public static Iterator iterator() {
            return iterator(TestValuedEnum.class);
        }
    }

    private static final class OtherValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final OtherValuedEnum OTHER_ONE = new OtherValuedEnum("OtherOne", 1);

        private OtherValuedEnum(String name, int value) {
            super(name, value);
        }
    }

    @Test
    public void testGetValue() {
        Assert.assertEquals(1, TestValuedEnum.ONE.getValue());
        Assert.assertEquals(2, TestValuedEnum.TWO.getValue());
        Assert.assertEquals(3, TestValuedEnum.THREE.getValue());
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("One", TestValuedEnum.ONE.getName());
        Assert.assertEquals("Two", TestValuedEnum.TWO.getName());
        Assert.assertEquals("Three", TestValuedEnum.THREE.getName());
    }

    @Test
    public void testGetEnum() {
        Assert.assertSame(TestValuedEnum.ONE, TestValuedEnum.getEnum(1));
        Assert.assertSame(TestValuedEnum.TWO, TestValuedEnum.getEnum(2));
        Assert.assertSame(TestValuedEnum.THREE, TestValuedEnum.getEnum(3));
        Assert.assertNull(TestValuedEnum.getEnum(4));
        Assert.assertNull(TestValuedEnum.getEnum(0));
        Assert.assertNull(TestValuedEnum.getEnum(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEnumNullClass() {
        ValuedEnum.getEnum(null, 1);
    }

    @Test
    public void testCompareTo() {
        Assert.assertTrue(TestValuedEnum.ONE.compareTo(TestValuedEnum.TWO) < 0);
        Assert.assertTrue(TestValuedEnum.TWO.compareTo(TestValuedEnum.ONE) > 0);
        Assert.assertEquals(0, TestValuedEnum.ONE.compareTo(TestValuedEnum.ONE));
        Assert.assertEquals(0, TestValuedEnum.TWO.compareTo(TestValuedEnum.TWO));
        Assert.assertEquals(0, TestValuedEnum.ONE.compareTo(OtherValuedEnum.OTHER_ONE));
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToNull() {
        TestValuedEnum.ONE.compareTo(null);
    }

    @Test(expected = ClassCastException.class)
    public void testCompareToNonEnum() {
        TestValuedEnum.ONE.compareTo("NotAnEnum");
    }

    @Test
    public void testToString() {
        String str1 = TestValuedEnum.ONE.toString();
        Assert.assertEquals("ValuedEnumTest.TestValuedEnum[One=1]", str1);
        Assert.assertSame(str1, TestValuedEnum.ONE.toString());

        String str2 = TestValuedEnum.TWO.toString();
        Assert.assertEquals("ValuedEnumTest.TestValuedEnum[Two=2]", str2);

        String strOther = OtherValuedEnum.OTHER_ONE.toString();
        Assert.assertEquals("ValuedEnumTest.OtherValuedEnum[OtherOne=1]", strOther);
    }

    @Test
    public void testEqualsAndHashCode() {
        Assert.assertEquals(TestValuedEnum.ONE, TestValuedEnum.ONE);
        Assert.assertNotEquals(TestValuedEnum.ONE, TestValuedEnum.TWO);
        Assert.assertNotEquals(TestValuedEnum.ONE, OtherValuedEnum.OTHER_ONE);
        Assert.assertNotEquals(TestValuedEnum.ONE, null);
        Assert.assertNotEquals(TestValuedEnum.ONE, "One");

        Assert.assertEquals(TestValuedEnum.ONE.hashCode(), TestValuedEnum.ONE.hashCode());
    }

    @Test
    public void testEnumListAndMap() {
        List list = TestValuedEnum.getEnumList();
        Assert.assertEquals(3, list.size());
        Assert.assertTrue(list.contains(TestValuedEnum.ONE));
        Assert.assertTrue(list.contains(TestValuedEnum.TWO));
        Assert.assertTrue(list.contains(TestValuedEnum.THREE));

        Map map = TestValuedEnum.getEnumMap();
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(TestValuedEnum.ONE, map.get("One"));
        Assert.assertEquals(TestValuedEnum.TWO, map.get("Two"));
        Assert.assertEquals(TestValuedEnum.THREE, map.get("Three"));

        Iterator it = TestValuedEnum.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(TestValuedEnum.ONE, it.next());
        Assert.assertSame(TestValuedEnum.TWO, it.next());
        Assert.assertSame(TestValuedEnum.THREE, it.next());
        Assert.assertFalse(it.hasNext());
    }
}
