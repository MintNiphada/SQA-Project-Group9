package org.apache.commons.math.stat;

import org.apache.commons.math.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;
import java.util.Iterator;

public class FrequencyTest {

    private Frequency f;

    @Before
    public void setUp() {
        f = new Frequency();
    }

    @Test
    public void testEmpty() {
        Assert.assertEquals(0L, f.getSumFreq());
        Assert.assertEquals(0L, f.getCount(1));
        Assert.assertEquals(0L, f.getCount(1L));
        Assert.assertEquals(0L, f.getCount('a'));
        Assert.assertEquals(0L, f.getCount(Integer.valueOf(1)));
        Assert.assertEquals(0L, f.getCount((Object) "a"));
        Assert.assertEquals(0L, f.getCumFreq(1));
        Assert.assertEquals(0L, f.getCumFreq(1L));
        Assert.assertEquals(0L, f.getCumFreq('a'));
        Assert.assertEquals(0L, f.getCumFreq(Integer.valueOf(1)));
        Assert.assertEquals(0L, f.getCumFreq((Object) "a"));
        Assert.assertTrue(Double.isNaN(f.getPct(1)));
        Assert.assertTrue(Double.isNaN(f.getPct(1L)));
        Assert.assertTrue(Double.isNaN(f.getPct('a')));
        Assert.assertTrue(Double.isNaN(f.getPct(Integer.valueOf(1))));
        Assert.assertTrue(Double.isNaN(f.getPct((Object) "a")));
        Assert.assertTrue(Double.isNaN(f.getCumPct(1)));
        Assert.assertTrue(Double.isNaN(f.getCumPct(1L)));
        Assert.assertTrue(Double.isNaN(f.getCumPct('a')));
        Assert.assertTrue(Double.isNaN(f.getCumPct(Integer.valueOf(1))));
        Assert.assertTrue(Double.isNaN(f.getCumPct((Object) "a")));
        Assert.assertFalse(f.valuesIterator().hasNext());
    }

    @Test
    public void testAddAndCountsInteger() {
        f.addValue(1);
        f.addValue(Integer.valueOf(1));
        f.addValue(2L);
        f.addValue((Object) 3);
        f.addValue((Comparable<?>) 3);

        Assert.assertEquals(5L, f.getSumFreq());
        Assert.assertEquals(2L, f.getCount(1));
        Assert.assertEquals(2L, f.getCount(Integer.valueOf(1)));
        Assert.assertEquals(2L, f.getCount(1L));
        Assert.assertEquals(1L, f.getCount(2));
        Assert.assertEquals(2L, f.getCount(3));
        Assert.assertEquals(0L, f.getCount(4));

        Assert.assertEquals(0.4, f.getPct(1), 1e-6);
        Assert.assertEquals(0.2, f.getPct(2L), 1e-6);
        Assert.assertEquals(0.4, f.getPct(Integer.valueOf(3)), 1e-6);
        Assert.assertEquals(0.0, f.getPct(4), 1e-6);

        Assert.assertEquals(2L, f.getCumFreq(1));
        Assert.assertEquals(3L, f.getCumFreq(Integer.valueOf(2)));
        Assert.assertEquals(5L, f.getCumFreq(3L));
        Assert.assertEquals(0L, f.getCumFreq(0));
        Assert.assertEquals(5L, f.getCumFreq(4));

        Assert.assertEquals(0.4, f.getCumPct(1), 1e-6);
        Assert.assertEquals(0.6, f.getCumPct(2L), 1e-6);
        Assert.assertEquals(1.0, f.getCumPct(3), 1e-6);
        Assert.assertEquals(0.0, f.getCumPct(0), 1e-6);
        Assert.assertEquals(1.0, f.getCumPct(4), 1e-6);
    }

    @Test
    public void testAddAndCountsChar() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('b');
        f.addValue('c');

        Assert.assertEquals(4L, f.getSumFreq());
        Assert.assertEquals(1L, f.getCount('a'));
        Assert.assertEquals(2L, f.getCount('b'));
        Assert.assertEquals(1L, f.getCount('c'));
        Assert.assertEquals(0L, f.getCount('d'));

        Assert.assertEquals(0.25, f.getPct('a'), 1e-6);
        Assert.assertEquals(0.5, f.getPct('b'), 1e-6);
        Assert.assertEquals(1L, f.getCumFreq('a'));
        Assert.assertEquals(3L, f.getCumFreq('b'));
        Assert.assertEquals(4L, f.getCumFreq('c'));
        Assert.assertEquals(0L, f.getCumFreq('`'));
        Assert.assertEquals(4L, f.getCumFreq('z'));

        Assert.assertEquals(0.25, f.getCumPct('a'), 1e-6);
        Assert.assertEquals(0.75, f.getCumPct('b'), 1e-6);
        Assert.assertEquals(1.0, f.getCumPct('c'), 1e-6);
    }

    @Test
    public void testClear() {
        f.addValue(10);
        f.addValue(20);
        Assert.assertEquals(2L, f.getSumFreq());
        f.clear();
        Assert.assertEquals(0L, f.getSumFreq());
        Assert.assertEquals(0L, f.getCount(10));
    }

    @Test
    public void testValuesIterator() {
        f.addValue(30);
        f.addValue(10);
        f.addValue(20);

        Iterator<Comparable<?>> it = f.valuesIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(10L, it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(20L, it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(30L, it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testCustomComparator() {
        Frequency custom = new Frequency(String.CASE_INSENSITIVE_ORDER);
        custom.addValue("A");
        custom.addValue("a");
        custom.addValue("b");

        Assert.assertEquals(3L, custom.getSumFreq());
        Assert.assertEquals(2L, custom.getCount("a"));
        Assert.assertEquals(2L, custom.getCount("A"));
        Assert.assertEquals(1L, custom.getCount("B"));
        Assert.assertEquals(2L, custom.getCumFreq("a"));
        Assert.assertEquals(3L, custom.getCumFreq("b"));
        Assert.assertEquals(0L, custom.getCumFreq("0"));
        Assert.assertEquals(3L, custom.getCumFreq("z"));
    }

    @Test
    public void testToString() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(2);
        String str = f.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("Value \t Freq. \t Pct. \t Cum Pct. \n"));
        Assert.assertTrue(str.contains("1\t1\t"));
        Assert.assertTrue(str.contains("2\t2\t"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNonComparableObject() {
        f.addValue(new Object());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncompatibleTypesAdd() {
        f.addValue("test");
        f.addValue(1);
    }

    @Test
    public void testIncompatibleTypeLookups() {
        f.addValue("foo");
        f.addValue("bar");
        Assert.assertEquals(0L, f.getCount(10));
        Assert.assertEquals(0L, f.getCumFreq(10));
        Assert.assertEquals(0.0, f.getPct(10), 1e-6);
        Assert.assertEquals(0.0, f.getCumPct(10), 1e-6);
    }

    @Test
    public void testCumFreqMiddleBranches() {
        f.addValue(10L);
        f.addValue(20L);
        f.addValue(30L);
        f.addValue(40L);

        Assert.assertEquals(1L, f.getCumFreq(10L));
        Assert.assertEquals(1L, f.getCumFreq(15L));
        Assert.assertEquals(2L, f.getCumFreq(20L));
        Assert.assertEquals(2L, f.getCumFreq(25L));
        Assert.assertEquals(3L, f.getCumFreq(30L));
        Assert.assertEquals(3L, f.getCumFreq(35L));
        Assert.assertEquals(4L, f.getCumFreq(40L));
        Assert.assertEquals(4L, f.getCumFreq(50L));
        Assert.assertEquals(0L, f.getCumFreq(5L));
    }

    @Test
    public void testEqualsAndHashCode() {
        Frequency f1 = new Frequency();
        Frequency f2 = new Frequency();

        Assert.assertTrue(f1.equals(f1));
        Assert.assertFalse(f1.equals(null));
        Assert.assertFalse(f1.equals("other"));
        Assert.assertTrue(f1.equals(f2));
        Assert.assertEquals(f1.hashCode(), f2.hashCode());

        f1.addValue(1);
        Assert.assertFalse(f1.equals(f2));
        Assert.assertFalse(f2.equals(f1));

        f2.addValue(1);
        Assert.assertTrue(f1.equals(f2));
        Assert.assertEquals(f1.hashCode(), f2.hashCode());

        f1.addValue(2);
        f2.addValue(3);
        Assert.assertFalse(f1.equals(f2));
    }

    @Test
    public void testDeprecatedObjectMethods() {
        f.addValue(Integer.valueOf(10));
        f.addValue(Integer.valueOf(20));

        Assert.assertEquals(1L, f.getCount((Object) 10));
        Assert.assertEquals(0.5, f.getPct((Object) 10), 1e-6);
        Assert.assertEquals(1L, f.getCumFreq((Object) 10));
        Assert.assertEquals(0.5, f.getCumPct((Object) 10), 1e-6);
    }
}
