package org.apache.commons.math.stat;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class FrequencyTest {

    @Test
    public void testEmpty() {
        Frequency f = new Frequency();
        Assert.assertEquals(0L, f.getSumFreq());
        Assert.assertEquals(0L, f.getCount("a"));
        Assert.assertEquals(0L, f.getCount(1));
        Assert.assertEquals(0L, f.getCount(1L));
        Assert.assertEquals(0L, f.getCount('a'));
        Assert.assertTrue(Double.isNaN(f.getPct("a")));
        Assert.assertTrue(Double.isNaN(f.getPct(1)));
        Assert.assertTrue(Double.isNaN(f.getPct(1L)));
        Assert.assertTrue(Double.isNaN(f.getPct('a')));
        Assert.assertEquals(0L, f.getCumFreq("a"));
        Assert.assertEquals(0L, f.getCumFreq(1));
        Assert.assertEquals(0L, f.getCumFreq(1L));
        Assert.assertEquals(0L, f.getCumFreq('a'));
        Assert.assertTrue(Double.isNaN(f.getCumPct("a")));
        Assert.assertTrue(Double.isNaN(f.getCumPct(1)));
        Assert.assertTrue(Double.isNaN(f.getCumPct(1L)));
        Assert.assertTrue(Double.isNaN(f.getCumPct('a')));
    }

    @Test
    public void testAddValueAndCounts() {
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(Integer.valueOf(1));
        f.addValue(1L);
        f.addValue(2L);
        f.addValue(3);

        Assert.assertEquals(5L, f.getSumFreq());
        Assert.assertEquals(3L, f.getCount(1));
        Assert.assertEquals(3L, f.getCount(Integer.valueOf(1)));
        Assert.assertEquals(3L, f.getCount(1L));
        Assert.assertEquals(3L, f.getCount((Object) 1L));
        Assert.assertEquals(1L, f.getCount(2));
        Assert.assertEquals(1L, f.getCount(3L));
        Assert.assertEquals(0L, f.getCount(4));
        Assert.assertEquals(0L, f.getCount("non-existent"));

        Assert.assertEquals(0.6, f.getPct(1), 1e-6);
        Assert.assertEquals(0.6, f.getPct(1L), 1e-6);
        Assert.assertEquals(0.6, f.getPct(Integer.valueOf(1)), 1e-6);
        Assert.assertEquals(0.2, f.getPct(2), 1e-6);
        Assert.assertEquals(0.0, f.getPct(4), 1e-6);
    }

    @Test
    public void testCumulative() {
        Frequency f = new Frequency();
        f.addValue(10);
        f.addValue(20);
        f.addValue(30);
        f.addValue(30);

        Assert.assertEquals(0L, f.getCumFreq(5));
        Assert.assertEquals(0.0, f.getCumPct(5), 1e-6);

        Assert.assertEquals(1L, f.getCumFreq(10));
        Assert.assertEquals(0.25, f.getCumPct(10), 1e-6);

        Assert.assertEquals(1L, f.getCumFreq(15));
        Assert.assertEquals(0.25, f.getCumPct(15), 1e-6);

        Assert.assertEquals(2L, f.getCumFreq(20));
        Assert.assertEquals(0.50, f.getCumPct(20), 1e-6);

        Assert.assertEquals(4L, f.getCumFreq(30));
        Assert.assertEquals(1.0, f.getCumPct(30), 1e-6);

        Assert.assertEquals(4L, f.getCumFreq(40));
        Assert.assertEquals(1.0, f.getCumPct(40), 1e-6);

        Assert.assertEquals(1L, f.getCumFreq(10L));
        Assert.assertEquals(0.25, f.getCumPct(10L), 1e-6);
        Assert.assertEquals(1L, f.getCumFreq(Integer.valueOf(10)));
        Assert.assertEquals(0.25, f.getCumPct(Integer.valueOf(10)), 1e-6);
    }

    @Test
    public void testCharOperations() {
        Frequency f = new Frequency();
        f.addValue('a');
        f.addValue('b');
        f.addValue('c');
        f.addValue('c');

        Assert.assertEquals(4L, f.getSumFreq());
        Assert.assertEquals(1L, f.getCount('a'));
        Assert.assertEquals(2L, f.getCount('c'));
        Assert.assertEquals(0L, f.getCount('d'));

        Assert.assertEquals(0.25, f.getPct('a'), 1e-6);
        Assert.assertEquals(0.50, f.getPct('c'), 1e-6);

        Assert.assertEquals(0L, f.getCumFreq('`'));
        Assert.assertEquals(1L, f.getCumFreq('a'));
        Assert.assertEquals(2L, f.getCumFreq('b'));
        Assert.assertEquals(4L, f.getCumFreq('c'));
        Assert.assertEquals(4L, f.getCumFreq('z'));

        Assert.assertEquals(0.25, f.getCumPct('a'), 1e-6);
        Assert.assertEquals(0.50, f.getCumPct('b'), 1e-6);
        Assert.assertEquals(1.0, f.getCumPct('c'), 1e-6);
    }

    @Test
    public void testCustomComparator() {
        Comparator<String> comp = Collections.reverseOrder();
        Frequency f = new Frequency(comp);
        f.addValue("A");
        f.addValue("B");
        f.addValue("C");

        Assert.assertEquals(3L, f.getSumFreq());
        Assert.assertEquals(0L, f.getCumFreq("D"));
        Assert.assertEquals(1L, f.getCumFreq("C"));
        Assert.assertEquals(2L, f.getCumFreq("B"));
        Assert.assertEquals(3L, f.getCumFreq("A"));
        Assert.assertEquals(3L, f.getCumFreq("@"));
    }

    @Test
    public void testClearAndIterator() {
        Frequency f = new Frequency();
        f.addValue("one");
        f.addValue("two");
        Iterator iter = f.valuesIterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertNotNull(iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertNotNull(iter.next());
        Assert.assertFalse(iter.hasNext());

        f.clear();
        Assert.assertEquals(0L, f.getSumFreq());
        Assert.assertFalse(f.valuesIterator().hasNext());
    }

    @Test
    public void testToString() {
        Frequency f = new Frequency();
        f.addValue("a");
        f.addValue("b");
        String str = f.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("Value \t Freq. \t Pct. \t Cum Pct. \n"));
        Assert.assertTrue(str.contains("a\t1\t"));
        Assert.assertTrue(str.contains("b\t1\t"));
    }

    @Test
    public void testIncompatibleTypesInAddValue() {
        Frequency f = new Frequency();
        f.addValue("test");
        try {
            f.addValue(1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Value not comparable to existing values.", ex.getMessage());
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedAddValueObject() {
        Frequency f = new Frequency();
        f.addValue((Object) "test");
        Assert.assertEquals(1L, f.getCount("test"));
    }

    @Test
    public void testIncompatibleTypeCountAndCumFreq() {
        Frequency f = new Frequency();
        f.addValue("test");
        Assert.assertEquals(0L, f.getCount(10));
        Assert.assertEquals(0L, f.getCount(new Object()));
        Assert.assertEquals(0L, f.getCumFreq(new Object()));
    }

    @Test
    public void testSerialization() throws Exception {
        Frequency f = new Frequency();
        f.addValue("a");
        f.addValue("b");
        f.addValue("b");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(f);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Frequency deserialized = (Frequency) ois.readObject();
        ois.close();

        Assert.assertEquals(f.getSumFreq(), deserialized.getSumFreq());
        Assert.assertEquals(f.getCount("b"), deserialized.getCount("b"));
        Assert.assertEquals(f.getCumFreq("b"), deserialized.getCumFreq("b"));
    }
}
