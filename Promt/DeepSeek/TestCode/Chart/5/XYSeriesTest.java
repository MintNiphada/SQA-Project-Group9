package org.jfree.data.xy;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.SeriesException;

public class XYSeriesTest {

    private XYSeries series;
    private boolean listenerNotified;

    @Before
    public void setUp() {
        series = new XYSeries("Test");
        listenerNotified = false;
    }

    private SeriesChangeListener createListener() {
        return new SeriesChangeListener() {
            @Override
            public void seriesChanged(SeriesChangeEvent event) {
                listenerNotified = true;
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new XYSeries(null);
    }

    @Test
    public void testConstructorDefaultFlags() {
        XYSeries s = new XYSeries("Key");
        assertTrue(s.getAutoSort());
        assertTrue(s.getAllowDuplicateXValues());
    }

    @Test
    public void testConstructorAutoSortFalse() {
        XYSeries s = new XYSeries("Key", false);
        assertFalse(s.getAutoSort());
        assertTrue(s.getAllowDuplicateXValues());
    }

    @Test
    public void testConstructorAllFlags() {
        XYSeries s = new XYSeries("Key", false, false);
        assertFalse(s.getAutoSort());
        assertFalse(s.getAllowDuplicateXValues());
    }

    @Test
    public void testGetItemCountInitiallyZero() {
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testGetItemsReturnsUnmodifiableList() {
        assertTrue(series.getItems().isEmpty());
        series.add(1.0, 2.0);
        assertEquals(1, series.getItems().size());
        try {
            series.getItems().add(new XYDataItem(3.0, 4.0));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testGetMaximumItemCountDefault() {
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
    }

    @Test
    public void testSetMaximumItemCount() {
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        assertEquals(2, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(3.0, series.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testSetMaximumItemCountFiresEvent() {
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.addChangeListener(createListener());
        series.setMaximumItemCount(1);
        assertTrue(listenerNotified);
    }

    @Test
    public void testAddXYDataItem() {
        XYDataItem item = new XYDataItem(1.0, 2.0);
        series.add(item);
        assertEquals(1, series.getItemCount());
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(2.0, series.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddDoubleDouble() {
        series.add(1.0, 2.0);
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testAddDoubleDoubleNotifyFalse() {
        series.addChangeListener(createListener());
        series.add(1.0, 2.0, false);
        assertFalse(listenerNotified);
    }

    @Test
    public void testAddDoubleNumber() {
        series.add(1.0, (Number) null);
        assertEquals(1, series.getItemCount());
        assertNull(series.getY(0));
    }

    @Test
    public void testAddDoubleNumberNotifyFalse() {
        series.addChangeListener(createListener());
        series.add(1.0, 2.0, false);
        assertFalse(listenerNotified);
    }

    @Test
    public void testAddNumberNumber() {
        series.add(new Double(1.0), new Double(2.0));
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testAddNumberNumberNotifyFalse() {
        series.addChangeListener(createListener());
        series.add(new Double(1.0), new Double(2.0), false);
        assertFalse(listenerNotified);
    }

    @Test
    public void testAddWithAutoSortTrue() {
        series.add(3.0, 3.0);
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(2.0, series.getX(1).doubleValue(), 0.0);
        assertEquals(3.0, series.getX(2).doubleValue(), 0.0);
    }

    @Test
    public void testAddWithAutoSortFalse() {
        XYSeries s = new XYSeries("Key", false);
        s.add(3.0, 3.0);
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        assertEquals(3.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0);
        assertEquals(2.0, s.getX(2).doubleValue(), 0.0);
    }

    @Test
    public void testAddDuplicateXWhenAllowed() {
        series.add(1.0, 1.0);
        series.add(1.0, 2.0);
        assertEquals(2, series.getItemCount());
        assertEquals(1.0, series.getY(0).doubleValue(), 0.0);
        assertEquals(2.0, series.getY(1).doubleValue(), 0.0);
    }

    @Test(expected = SeriesException.class)
    public void testAddDuplicateXWhenNotAllowed() {
        XYSeries s = new XYSeries("Key", true, false);
        s.add(1.0, 1.0);
        s.add(1.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        series.add((XYDataItem) null);
    }

    @Test
    public void testAddWithMaximumItemCount() {
        series.setMaximumItemCount(2);
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        assertEquals(2, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(3.0, series.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testDelete() {
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(3.0, series.getX(0).doubleValue(), 0.0);
    }

    @Test
    public void testDeleteFiresEvent() {
        series.add(1.0, 1.0);
        series.addChangeListener(createListener());
        series.delete(0, 0);
        assertTrue(listenerNotified);
    }

    @Test
    public void testDeleteStartGreaterThanEnd() {
        series.add(1.0, 1.0);
        series.addChangeListener(createListener());
        series.delete(1, 0); // no removal, but event still fired
        assertEquals(1, series.getItemCount());
        assertTrue(listenerNotified);
    }

    @Test
    public void testRemoveByIndex() {
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        XYDataItem removed = series.remove(0);
        assertEquals(1.0, removed.getX().doubleValue(), 0.0);
        assertEquals(1, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByIndexInvalid() {
        series.remove(0);
    }

    @Test
    public void testRemoveByX() {
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        XYDataItem removed = series.remove(new Double(1.0));
        assertEquals(1.0, removed.getX().doubleValue(), 0.0);
        assertEquals(1, series.getItemCount());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByXNonExisting() {
        series.remove(new Double(1.0));
    }

    @Test
    public void testClear() {
        series.add(1.0, 1.0);
        series.clear();
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testClearFiresEvent() {
        series.add(1.0, 1.0);
        series.addChangeListener(createListener());
        series.clear();
        assertTrue(listenerNotified);
    }

    @Test
    public void testClearEmptySeriesNoEvent() {
        series.addChangeListener(createListener());
        series.clear();
        assertFalse(listenerNotified);
    }

    @Test
    public void testGetDataItem() {
        series.add(1.0, 2.0);
        XYDataItem item = series.getDataItem(0);
        assertEquals(1.0, item.getX().doubleValue(), 0.0);
        assertEquals(2.0, item.getY().doubleValue(), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemInvalidIndex() {
        series.getDataItem(0);
    }

    @Test
    public void testGetX() {
        series.add(1.0, 2.0);
        assertEquals(1.0, series.getX(0).doubleValue(), 0.0);
    }

    @Test
    public void testGetY() {
        series.add(1.0, 2.0);
        assertEquals(2.0, series.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testGetYNull() {
        series.add(1.0, (Number) null);
        assertNull(series.getY(0));
    }

    @Test
    public void testUpdateByIndex() {
        series.add(1.0, 2.0);
        series.updateByIndex(0, 3.0);
        assertEquals(3.0, series.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testUpdateByIndexFiresEvent() {
        series.add(1.0, 2.0);
        series.addChangeListener(createListener());
        series.updateByIndex(0, 3.0);
        assertTrue(listenerNotified);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateByIndexInvalid() {
        series.updateByIndex(0, 1.0);
    }

    @Test
    public void testUpdate() {
        series.add(1.0, 2.0);
        series.update(new Double(1.0), new Double(3.0));
        assertEquals(3.0, series.getY(0).doubleValue(), 0.0);
    }

    @Test(expected = SeriesException.class)
    public void testUpdateNonExistingX() {
        series.update(new Double(1.0), new Double(2.0));
    }

    @Test
    public void testAddOrUpdateNullX() {
        try {
            series.addOrUpdate(null, 1.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddOrUpdateNewX() {
        XYDataItem overwritten = series.addOrUpdate(1.0, 2.0);
        assertNull(overwritten);
        assertEquals(1, series.getItemCount());
        assertEquals(2.0, series.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdateExistingXDuplicateNotAllowed() {
        XYSeries s = new XYSeries("Key", true, false);
        s.add(1.0, 2.0);
        XYDataItem overwritten = s.addOrUpdate(1.0, 3.0);
        assertNotNull(overwritten);
        assertEquals(2.0, overwritten.getY().doubleValue(), 0.0);
        assertEquals(3.0, s.getY(0).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdateExistingXDuplicateAllowed() {
        series.add(1.0, 2.0);
        XYDataItem overwritten = series.addOrUpdate(1.0, 3.0);
        assertNull(overwritten);
        assertEquals(2, series.getItemCount());
        assertEquals(2.0, series.getY(0).doubleValue(), 0.0);
        assertEquals(3.0, series.getY(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdateAutoSortFalse() {
        XYSeries s = new XYSeries("Key", false, false);
        s.add(2.0, 2.0);
        s.addOrUpdate(1.0, 1.0);
        assertEquals(2, s.getItemCount());
        assertEquals(2.0, s.getX(0).doubleValue(), 0.0);
        assertEquals(1.0, s.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testAddOrUpdateWithMaximumItemCount() {
        series.setMaximumItemCount(2);
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.addOrUpdate(3.0, 3.0);
        assertEquals(2, series.getItemCount());
        assertEquals(2.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(3.0, series.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testIndexOfAutoSortTrue() {
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        assertEquals(0, series.indexOf(new Double(1.0)));
        assertEquals(1, series.indexOf(new Double(2.0)));
        assertTrue(series.indexOf(new Double(3.0)) < 0);
    }

    @Test
    public void testIndexOfAutoSortFalse() {
        XYSeries s = new XYSeries("Key", false);
        s.add(2.0, 2.0);
        s.add(1.0, 1.0);
        assertEquals(0, s.indexOf(new Double(2.0)));
        assertEquals(1, s.indexOf(new Double(1.0)));
        assertEquals(-1, s.indexOf(new Double(3.0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfNullX() {
        series.indexOf(null);
    }

    @Test
    public void testToArray() {
        series.add(1.0, 2.0);
        series.add(3.0, null);
        double[][] arr = series.toArray();
        assertEquals(2, arr[0].length);
        assertEquals(1.0, arr[0][0], 0.0);
        assertEquals(3.0, arr[0][1], 0.0);
        assertEquals(2.0, arr[1][0], 0.0);
        assertTrue(Double.isNaN(arr[1][1]));
    }

    @Test
    public void testToArrayEmpty() {
        double[][] arr = series.toArray();
        assertEquals(0, arr[0].length);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        series.add(1.0, 2.0);
        XYSeries clone = (XYSeries) series.clone();
        assertNotSame(series, clone);
        assertEquals(series, clone);
        clone.add(3.0, 4.0);
        assertNotEquals(series.getItemCount(), clone.getItemCount());
    }

    @Test
    public void testCreateCopy() throws CloneNotSupportedException {
        series.add(1.0, 1.0);
        series.add(2.0, 2.0);
        series.add(3.0, 3.0);
        XYSeries copy = series.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertEquals(1.0, copy.getX(0).doubleValue(), 0.0);
        assertEquals(2.0, copy.getX(1).doubleValue(), 0.0);
    }

    @Test
    public void testCreateCopyStartGreaterThanEnd() throws CloneNotSupportedException {
        series.add(1.0, 1.0);
        XYSeries copy = series.createCopy(1, 0);
        assertEquals(0, copy.getItemCount());
    }

    @Test
    public void testCreateCopySingleItem() throws CloneNotSupportedException {
        series.add(1.0, 1.0);
        XYSeries copy = series.createCopy(0, 0);
        assertEquals(1, copy.getItemCount());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(series.equals(series));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(series.equals("String"));
    }

    @Test
    public void testEqualsDifferentMaximumItemCount() {
        XYSeries s1 = new XYSeries("Key");
        XYSeries s2 = new XYSeries("Key");
        s2.setMaximumItemCount(10);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentAutoSort() {
        XYSeries s1 = new XYSeries("Key", true);
        XYSeries s2 = new XYSeries("Key", false);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentAllowDuplicateXValues() {
        XYSeries s1 = new XYSeries("Key", true, true);
        XYSeries s2 = new XYSeries("Key", true, false);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentData() {
        XYSeries s1 = new XYSeries("Key");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("Key");
        s2.add(2.0, 2.0);
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsSameData() {
        XYSeries s1 = new XYSeries("Key");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("Key");
        s2.add(1.0, 1.0);
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testHashCodeConsistency() {
        XYSeries s1 = new XYSeries("Key");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("Key");
        s2.add(1.0, 1.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testHashCodeEmptySeries() {
        XYSeries s1 = new XYSeries("Key");
        XYSeries s2 = new XYSeries("Key");
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
