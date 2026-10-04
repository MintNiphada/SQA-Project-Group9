package org.apache.commons.math.util;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Test;

public class MultidimensionalCounterTest {

    @Test
    public void testConstructorAndGetters() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3, 4);
        Assert.assertEquals(3, c.getDimension());
        Assert.assertEquals(24, c.getSize());
        Assert.assertArrayEquals(new int[]{2, 3, 4}, c.getSizes());
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroSize() {
        new MultidimensionalCounter(2, 0, 4);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeSize() {
        new MultidimensionalCounter(2, -3, 4);
    }

    @Test
    public void testGetCountMultiToUni() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 4, 3);
        Assert.assertEquals(0, c.getCount(0, 0, 0));
        Assert.assertEquals(1, c.getCount(0, 0, 1));
        Assert.assertEquals(2, c.getCount(0, 0, 2));
        Assert.assertEquals(3, c.getCount(0, 1, 0));
        Assert.assertEquals(12, c.getCount(1, 0, 0));
        Assert.assertEquals(23, c.getCount(1, 3, 2));
    }

    @Test(expected = DimensionMismatchException.class)
    public void testGetCountDimensionMismatchTooFew() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3, 4);
        c.getCount(1, 2);
    }

    @Test(expected = DimensionMismatchException.class)
    public void testGetCountDimensionMismatchTooMany() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        c.getCount(1, 2, 3);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountNegativeIndex() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        c.getCount(-1, 1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountIndexTooHigh() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        c.getCount(2, 1);
    }

    @Test
    public void testGetCountsUniToMulti() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 4, 3);
        Assert.assertArrayEquals(new int[]{0, 0, 0}, c.getCounts(0));
        Assert.assertArrayEquals(new int[]{0, 0, 1}, c.getCounts(1));
        Assert.assertArrayEquals(new int[]{0, 0, 2}, c.getCounts(2));
        Assert.assertArrayEquals(new int[]{0, 1, 0}, c.getCounts(3));
        Assert.assertArrayEquals(new int[]{1, 0, 0}, c.getCounts(12));
        Assert.assertArrayEquals(new int[]{1, 3, 2}, c.getCounts(23));
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountsNegative() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        c.getCounts(-1);
    }

    @Test(expected = OutOfRangeException.class)
    public void testGetCountsTooHigh() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        c.getCounts(6);
    }

    @Test
    public void testIterator() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        MultidimensionalCounter.Iterator iter = c.iterator();

        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(-1, iter.getCount());

        int expectedIndex = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                Assert.assertTrue(iter.hasNext());
                int nextIndex = iter.next();
                Assert.assertEquals(expectedIndex, nextIndex);
                Assert.assertEquals(expectedIndex, iter.getCount());
                Assert.assertArrayEquals(new int[]{i, j}, iter.getCounts());
                Assert.assertEquals(i, iter.getCount(0));
                Assert.assertEquals(j, iter.getCount(1));
                expectedIndex++;
            }
        }

        Assert.assertFalse(iter.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        c.iterator().remove();
    }

    @Test
    public void testToString() {
        MultidimensionalCounter c = new MultidimensionalCounter(2, 3);
        String s = c.toString();
        Assert.assertEquals("[0][0]", s);
    }

    @Test
    public void testOneDimensional() {
        MultidimensionalCounter c = new MultidimensionalCounter(5);
        Assert.assertEquals(1, c.getDimension());
        Assert.assertEquals(5, c.getSize());
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(i, c.getCount(i));
            Assert.assertArrayEquals(new int[]{i}, c.getCounts(i));
        }
    }
}
