package org.apache.commons.math.stat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
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
        Assert.assertEquals(0L, f.getCount(0));
        Assert.assertEquals(0L, f.getCount(0L));
        Assert.assertEquals(0L, f.getCount('a'));
        Assert.assertEquals(0L, f.getCount((Object) 0));
        Assert.assertEquals(Double.NaN, f.getPct(0), 0.0);
        Assert.assertEquals(Double.NaN, f.getPct(0L), 0.0);
        Assert.assertEquals(Double.NaN, f.getPct('a'), 0.0);
        Assert.assertEquals(Double.NaN, f.getPct((Object) 0), 0.0);
        Assert.assertEquals(0L, f.getCumFreq(0));
        Assert.assertEquals(0L, f.getCumFreq(0L));
        Assert.assertEquals(0L, f.getCumFreq('a'));
        Assert.assertEquals(0L, f.getCumFreq((Object) 0));
        Assert.assertEquals(Double.NaN, f.getCumPct(0), 0.0);
        Assert.assertEquals(Double.NaN, f.getCumPct(0L), 0.0);
        Assert.assertEquals(Double.NaN, f.getCumPct('a'), 0.0);
        Assert.assertEquals(Double.NaN, f.getCumPct((Object) 0), 0.0);
        Assert.assertFalse(f.valuesIterator().hasNext());
    }

    @Test
    public void testCounts() {
        f.addValue(1);
        f.addValue(Integer.valueOf(1));
        f.addValue(1L);
        f.addValue((Object) 1L);
        f.addValue(2);
        f.addValue(3L);

        Assert.assertEquals(6L, f.getSumFreq());
        Assert.assertEquals(4L, f.getCount(1));
        Assert.assertEquals(4L, f.getCount(Integer.valueOf(1)));
        Assert.assertEquals(4L, f.getCount(1L));
        Assert.assertEquals(4L, f.getCount((Object) 1L));
        Assert.assertEquals(1L, f.getCount(2));
        Assert.assertEquals(1L, f.getCount(3L));
        Assert.assertEquals(0L, f.getCount(4));
    }

    @Test
    public void testPct() {
        f.addValue(1);
        f.addValue(2);
        f.addValue(2);
        f.addValue(3);

        Assert.assertEquals(0.25, f.getPct(1), 1e-10);
        Assert.assertEquals(0.50, f.getPct(2), 1e-10);
        Assert.assertEquals(0.25, f.getPct(3), 1e-10);
        Assert.assertEquals(0.0, f.getPct(4), 1e-10);
        Assert.assertEquals(0.50, f.getPct(2L), 1e-10);
        Assert.assertEquals(0.50, f.getPct((Object) 2L), 1e-10);
    }

    @Test
    public void testCumFreqAndPct() {
        f.addValue(10);
        f.addValue(20);
        f.addValue(20);
        f.addValue(30);

        Assert.assertEquals(0L, f.getCumFreq(5));
        Assert.assertEquals(0L, f.getCumFreq(9L));
        Assert.assertEquals(1L, f.getCumFreq(10));
        Assert.assertEquals(1L, f.getCumFreq(15));
        Assert.assertEquals(3L, f.getCumFreq(20));
        Assert.assertEquals(3L, f.getCumFreq(25));
        Assert.assertEquals(4L, f.getCumFreq(30));
        Assert.assertEquals(4L, f.getCumFreq(35));

        Assert.assertEquals(0.0, f.getCumPct(5), 1e-10);
        Assert.assertEquals(0.25, f.getCumPct(10), 1e-10);
        Assert.assertEquals(0.75, f.getCumPct(20), 1e-10);
        Assert.assertEquals(1.0, f.getCumPct(30), 1e-10);
        Assert.assertEquals(1.0, f.getCumPct(35), 1e-10);
        Assert.assertEquals(0.75, f.getCumPct(20L), 1e-10);
        Assert.assertEquals(0.75, f.getCumPct((Object) 20L), 1e-10);
        Assert.assertEquals(0.75, f.getCumPct((Object) Integer.valueOf(20)), 1e-10);
    }

    @Test
    public void testCharValues() {
        f.addValue('a');
        f.addValue('b');
        f.addValue('b');
        f.addValue('c');

        Assert.assertEquals(4L, f.getSumFreq());
        Assert.assertEquals(1L, f.getCount('a'));
        Assert.assertEquals(2L, f.getCount('b'));
        Assert.assertEquals(0L, f.getCount('z'));

        Assert.assertEquals(0.25, f.getPct('a'), 1e-10);
        Assert.assertEquals(0.50, f.getPct('b'), 1e-10);

        Assert.assertEquals(0L, f.getCumFreq(' '));
        Assert.assertEquals(1L, f.getCumFreq('a'));
        Assert.assertEquals(3L, f.getCumFreq('b'));
        Assert.assertEquals(4L, f.getCumFreq('c'));
        Assert.assertEquals(4L, f.getCumFreq('d'));

        Assert.assertEquals(0.75, f.getCumPct('b'), 1e-10);
    }

    @Test
    public void testClear() {
        f.addValue(1);
        f.addValue(2);
        Assert.assertEquals(2L, f.getSumFreq());
        f.clear();
        Assert.assertEquals(0L, f.getSumFreq());
        Assert.assertEquals(0L, f.getCount(1));
        Assert.assertFalse(f.valuesIterator().hasNext());
    }

    @Test
    public void testValuesIterator() {
        f.addValue(30);
        f.addValue(10);
        f.addValue(20);

        Iterator it = f.valuesIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(10L, it.next());
        Assert.assertEquals(20L, it.next());
        Assert.assertEquals(30L, it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testToString() {
        f.addValue(1);
        f.addValue(2);
        String s = f.toString();
        Assert.assertNotNull(s);
        Assert.assertTrue(s.startsWith("Value \t Freq. \t Pct. \t Cum Pct. \n"));
        Assert.assertTrue(s.contains("1\t1\t50%"));
        Assert.assertTrue(s.contains("2\t1\t50%"));
    }

    @Test
    public void testCustomComparator() {
        Frequency cf = new Frequency(Collections.reverseOrder());
        cf.addValue(10L);
        cf.addValue(20L);
        cf.addValue(30L);

        Assert.assertEquals(3L, cf.getSumFreq());
        Iterator it = cf.valuesIterator();
        Assert.assertEquals(30L, it.next());
        Assert.assertEquals(20L, it.next());
        Assert.assertEquals(10L, it.next());

        Assert.assertEquals(0L, cf.getCumFreq(35L));
        Assert.assertEquals(1L, cf.getCumFreq(30L));
        Assert.assertEquals(2L, cf.getCumFreq(20L));
        Assert.assertEquals(3L, cf.getCumFreq(10L));
        Assert.assertEquals(3L, cf.getCumFreq(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIncompatibleType() {
        f.addValue(1);
        f.addValue("a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNonComparable() {
        f.addValue(new Object());
    }

    @Test
    public void testIncompatibleLookup() {
        f.addValue(1);
        Assert.assertEquals(0L, f.getCount("a"));
        Assert.assertEquals(0L, f.getCumFreq("a"));
        Assert.assertEquals(0.0, f.getCumPct("a"), 1e-10);
    }

    @Test
    public void testNaturalComparatorCompare() {
        Frequency freq = new Frequency();
        freq.addValue("b");
        freq.addValue("a");
        freq.addValue("c");

        Assert.assertEquals(1L, freq.getCumFreq("a"));
        Assert.assertEquals(2L, freq.getCumFreq("b"));
        Assert.assertEquals(3L, freq.getCumFreq("c"));
        Assert.assertEquals(0L, freq.getCumFreq("0"));
        Assert.assertEquals(3L, freq.getCumFreq("z"));
    }

    @Test
    public void testIntegerVsLongLookup() {
        f.addValue(10);
        Assert.assertEquals(1L, f.getCount(Integer.valueOf(10)));
        Assert.assertEquals(1L, f.getCount((Object) Integer.valueOf(10)));
        Assert.assertEquals(1L, f.getCumFreq(Integer.valueOf(10)));
        Assert.assertEquals(1L, f.getCumFreq((Object) Integer.valueOf(10)));
    }
}
