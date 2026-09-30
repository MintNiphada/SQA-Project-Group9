package org.jfree.data.time;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

/**
 * Comprehensive test suite for the TimePeriodValues class.
 * Covers constructors, property accessors, data manipulation,
 * equality, cloning, bounds management, and edge cases.
 */
public class TimePeriodValuesTest {

    private TimePeriodValues series;

    @Before
    public void setUp() {
        series = new TimePeriodValues("TestSeries");
    }

    // ---- Constructor Tests ----

    @Test
    public void testConstructorWithNameOnly() {
        TimePeriodValues s = new TimePeriodValues("NameOnly");
        assertEquals("NameOnly", s.getKey());
        assertEquals("Time", s.getDomainDescription());
        assertEquals("Value", s.getRangeDescription());
        assertEquals(0, s.getItemCount());
    }

    @Test
    public void testConstructorWithDescriptions() {
        TimePeriodValues s = new TimePeriodValues("Full", "DomainDesc", "RangeDesc");
        assertEquals("Full", s.getKey());
        assertEquals("DomainDesc", s.getDomainDescription());
        assertEquals("RangeDesc", s.getRangeDescription());
        assertEquals(0, s.getItemCount());
    }

    // ---- Property Accessors ----

    @Test
    public void testGetAndSetDomainDescription() {
        assertEquals("Time", series.getDomainDescription());
        series.setDomainDescription("NewDomain");
        assertEquals("NewDomain", series.getDomainDescription());
        series.setDomainDescription(null);
        assertNull(series.getDomainDescription());
    }

    @Test
    public void testGetAndSetRangeDescription() {
        assertEquals("Value", series.getRangeDescription());
        series.setRangeDescription("NewRange");
        assertEquals("NewRange", series.getRangeDescription());
        series.setRangeDescription(null);
        assertNull(series.getRangeDescription());
    }

    // ---- Item Count ----

    @Test
    public void testGetItemCountEmpty() {
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testGetItemCountAfterAdd() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1.0));
        assertEquals(1, series.getItemCount());
    }

    @Test
    public void testGetItemCountAfterDelete() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1.0));
        series.delete(0, 0);
        assertEquals(0, series.getItemCount());
    }

    // ---- Data Access ----

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemInvalidIndexEmpty() {
        series.getDataItem(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemInvalidIndexNegative() {
        series.getDataItem(-1);
    }

    @Test
    public void testGetDataItemValid() {
        TimePeriodValue item = new TimePeriodValue(new SimpleTimePeriod(new Date(100), new Date(200)), 5.0);
        series.add(item);
        assertEquals(item, series.getDataItem(0));
    }

    @Test
    public void testGetTimePeriod() {
        TimePeriodValue item = new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 5.0);
        series.add(item);
        assertEquals(item.getPeriod(), series.getTimePeriod(0));
    }

    @Test
    public void testGetValue() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 7.5));
        assertEquals(7.5, series.getValue(0));
    }

    // ---- Adding Items (TimePeriodValue) ----

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullTimePeriodValue() {
        series.add((TimePeriodValue) null);
    }

    @Test
    public void testAddSingleItemUpdatesBounds() {
        SimpleTimePeriod period = new SimpleTimePeriod(new Date(1000), new Date(2000));
        TimePeriodValue item = new TimePeriodValue(period, 10.0);
        series.add(item);
        assertEquals(0, series.getMinStartIndex());
        assertEquals(0, series.getMaxStartIndex());
        assertEquals(0, series.getMinMiddleIndex());
        assertEquals(0, series.getMaxMiddleIndex());
        assertEquals(0, series.getMinEndIndex());
        assertEquals(0, series.getMaxEndIndex());
    }

    @Test
    public void testAddMultipleItemsCheckStartIndices() {
        // earliest start: item2, latest start: item0
        SimpleTimePeriod p0 = new SimpleTimePeriod(new Date(3000), new Date(4000));
        SimpleTimePeriod p1 = new SimpleTimePeriod(new Date(2000), new Date(3500));
        SimpleTimePeriod p2 = new SimpleTimePeriod(new Date(1000), new Date(2500));
        series.add(new TimePeriodValue(p0, 1));
        series.add(new TimePeriodValue(p1, 2));
        series.add(new TimePeriodValue(p2, 3));
        assertEquals(2, series.getMinStartIndex()); // 1000 < 2000, 3000
        assertEquals(0, series.getMaxStartIndex()); // 3000 > 2000, 1000
    }

    @Test
    public void testAddMultipleItemsCheckEndIndices() {
        // earliest end: item1, latest end: item0
        SimpleTimePeriod p0 = new SimpleTimePeriod(new Date(1000), new Date(5000));
        SimpleTimePeriod p1 = new SimpleTimePeriod(new Date(2000), new Date(2500));
        SimpleTimePeriod p2 = new SimpleTimePeriod(new Date(3000), new Date(4000));
        series.add(new TimePeriodValue(p0, 1));
        series.add(new TimePeriodValue(p1, 2));
        series.add(new TimePeriodValue(p2, 3));
        assertEquals(1, series.getMinEndIndex()); // end 2500
        assertEquals(0, series.getMaxEndIndex()); // end 5000
    }

    @Test
    public void testAddMultipleItemsCheckMiddleIndices() {
        // middle: p2=3500, p0=3000, p1=2500? Let's compute precisely.
        // Use clear dates to observe bug in maxMiddleIndex calculation.
        SimpleTimePeriod p0 = new SimpleTimePeriod(new Date(1000), new Date(3000)); // middle 2000
        SimpleTimePeriod p1 = new SimpleTimePeriod(new Date(2000), new Date(4000)); // middle 3000
        SimpleTimePeriod p2 = new SimpleTimePeriod(new Date(3000), new Date(5000)); // middle 4000
        series.add(new TimePeriodValue(p0, 1));
        series.add(new TimePeriodValue(p1, 2));
        series.add(new TimePeriodValue(p2, 3));
        // min middle should be index of smallest middle (2000) -> index 0
        assertEquals(0, series.getMinMiddleIndex());
        // max middle: buggy behavior uses minMiddleIndex for comparison, which is 0, middle 2000
        // so maxMiddleIndex will not be updated correctly. The actual value may be unpredictable.
        // We record the observed behavior rather than correct logical behavior.
        // After p0: maxMiddleIndex=0, minMiddleIndex=0. Add p1 (middle 3000 > 2000): maxMiddleIndex=1.
        // Add p2 (middle 4000): compare with minMiddleIndex's middle (still 0's 2000?) minMiddleIndex is 0? But minMiddleIndex might have been updated? 
        // When adding p1, minMiddleIndex would compare 3000 with 2000, not update because 3000 >= 2000, so minMiddleIndex remains 0.
        // For maxMiddle, after p0: maxMiddleIndex=0. Add p1: use getDataItem(this.minMiddleIndex) which is getDataItem(0), middle 2000, 3000 > 2000 -> maxMiddleIndex=1. Add p2: again use getDataItem(minMiddleIndex=0) middle 2000, 4000 > 2000 -> maxMiddleIndex=2.
        // So due to bug, maxMiddleIndex ends up as 2 even though it's the largest, but it only works because minMiddleIndex stayed 0. So verify that.
        assertEquals(2, series.getMaxMiddleIndex());
    }

    // ---- Adding via convenience methods ----

    @Test
    public void testAddPeriodAndDouble() {
        SimpleTimePeriod p = new SimpleTimePeriod(new Date(0), new Date(1000));
        series.add(p, 42.5);
        assertEquals(1, series.getItemCount());
        assertEquals(42.5, series.getValue(0).doubleValue(), 0.00001);
        assertEquals(p, series.getTimePeriod(0));
    }

    @Test
    public void testAddPeriodAndNumber() {
        SimpleTimePeriod p = new SimpleTimePeriod(new Date(0), new Date(1000));
        series.add(p, Integer.valueOf(10));
        assertEquals(1, series.getItemCount());
        assertEquals(10, series.getValue(0).intValue());
        assertEquals(p, series.getTimePeriod(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPeriodAndDoubleNullPeriod() {
        series.add((TimePeriod) null, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPeriodAndNumberNullPeriod() {
        series.add((TimePeriod) null, Integer.valueOf(1));
    }

    // ---- Update Value ----

    @Test
    public void testUpdateValue() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(100)), 10.0));
        series.update(0, 20.0);
        assertEquals(20.0, series.getValue(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateInvalidIndex() {
        series.update(0, 10.0);
    }

    // ---- Delete ----

    @Test
    public void testDeleteSingleItem() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        series.delete(0, 0);
        assertEquals(0, series.getItemCount());
        // All bounds reset to -1
        assertEquals(-1, series.getMinStartIndex());
        assertEquals(-1, series.getMaxStartIndex());
        assertEquals(-1, series.getMinMiddleIndex());
        assertEquals(-1, series.getMaxMiddleIndex());
        assertEquals(-1, series.getMinEndIndex());
        assertEquals(-1, series.getMaxEndIndex());
    }

    @Test
    public void testDeleteMultipleItems() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 1));
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(3000), new Date(4000)), 2));
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(5000), new Date(6000)), 3));
        series.delete(0, 1); // remove first two
        assertEquals(1, series.getItemCount());
        assertEquals(new Date(5000), series.getTimePeriod(0).getStart());
        assertEquals(new Date(6000), series.getTimePeriod(0).getEnd());
        // Bounds recalculated for the remaining single item
        assertEquals(0, series.getMinStartIndex());
        assertEquals(0, series.getMaxStartIndex());
    }

    @Test
    public void testDeleteAllItems() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(2000), new Date(3000)), 2));
        series.delete(0, 1);
        assertEquals(0, series.getItemCount());
        assertEquals(-1, series.getMinStartIndex());
    }

    @Test
    public void testDeleteWithStartGreaterThanEnd() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        series.delete(1, 0); // start > end, no deletion
        assertEquals(1, series.getItemCount()); // unchanged
        // recalculateBounds called but with same data
        assertEquals(0, series.getMinStartIndex());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteNegativeStart() {
        series.delete(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteStartOutOfBounds() {
        series.delete(1, 1); // on empty series
    }

    // ---- Equality ----

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
    public void testEqualsSameProperties() {
        TimePeriodValues s1 = new TimePeriodValues("Series1", "Dom", "Ran");
        TimePeriodValues s2 = new TimePeriodValues("Series1", "Dom", "Ran");
        assertTrue(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentDomain() {
        TimePeriodValues s1 = new TimePeriodValues("S", "Dom1", "Ran");
        TimePeriodValues s2 = new TimePeriodValues("S", "Dom2", "Ran");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentRange() {
        TimePeriodValues s1 = new TimePeriodValues("S", "Dom", "Ran1");
        TimePeriodValues s2 = new TimePeriodValues("S", "Dom", "Ran2");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentItemCount() {
        TimePeriodValues s1 = new TimePeriodValues("S");
        s1.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        TimePeriodValues s2 = new TimePeriodValues("S");
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsDifferentData() {
        TimePeriodValues s1 = new TimePeriodValues("S");
        s1.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 1));
        TimePeriodValues s2 = new TimePeriodValues("S");
        s2.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 2));
        assertFalse(s1.equals(s2));
    }

    @Test
    public void testEqualsSameData() {
        TimePeriodValues s1 = new TimePeriodValues("S");
        s1.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 1));
        TimePeriodValues s2 = new TimePeriodValues("S");
        s2.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 1));
        assertTrue(s1.equals(s2));
    }

    // ---- Hashcode (basic consistency) ----

    @Test
    public void testHashCodeConsistency() {
        TimePeriodValues s1 = new TimePeriodValues("H");
        s1.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        int h1 = s1.hashCode();
        int h2 = s1.hashCode();
        assertEquals(h1, h2);
    }

    @Test
    public void testHashCodeEquality() {
        TimePeriodValues s1 = new TimePeriodValues("S", "D", "R");
        s1.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        TimePeriodValues s2 = new TimePeriodValues("S", "D", "R");
        s2.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    // ---- Clone ----

    @Test
    public void testCloneBasic() throws CloneNotSupportedException {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 5.0));
        TimePeriodValues clone = (TimePeriodValues) series.clone();
        assertNotNull(clone);
        assertNotSame(series, clone);
        assertEquals(series.getItemCount(), clone.getItemCount());
        assertEquals(series.getDomainDescription(), clone.getDomainDescription());
        assertEquals(series.getRangeDescription(), clone.getRangeDescription());
        // data items should be deeply copied (via createCopy -> clone of TimePeriodValue)
        assertNotSame(series.getDataItem(0), clone.getDataItem(0));
        assertTrue(series.equals(clone));
    }

    @Test
    public void testCloneEmptySeries() throws CloneNotSupportedException {
        TimePeriodValues clone = (TimePeriodValues) series.clone();
        assertEquals(0, clone.getItemCount());
        assertTrue(series.equals(clone));
    }

    @Test
    public void testCloneModifyOriginalDoesNotAffectClone() throws CloneNotSupportedException {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 5.0));
        TimePeriodValues clone = (TimePeriodValues) series.clone();
        series.update(0, 10.0);
        assertEquals(5.0, clone.getValue(0));
    }

    // ---- createCopy ----

    @Test
    public void testCreateCopyFullRange() throws CloneNotSupportedException {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(2000), new Date(3000)), 2));
        TimePeriodValues copy = series.createCopy(0, 1);
        assertEquals(2, copy.getItemCount());
        assertNotSame(series.getDataItem(0), copy.getDataItem(0));
        assertNotSame(series.getDataItem(1), copy.getDataItem(1));
        assertEquals(1.0, copy.getValue(0));
        assertEquals(2.0, copy.getValue(1));
    }

    @Test
    public void testCreateCopySubset() throws CloneNotSupportedException {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(0), new Date(1000)), 1));
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(2000), new Date(3000)), 2));
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(4000), new Date(5000)), 3));
        TimePeriodValues copy = series.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(2.0, copy.getValue(0));
        assertEquals(3.0, copy.getValue(1));
    }

    @Test
    public void testCreateCopyEmptySource() throws CloneNotSupportedException {
        TimePeriodValues copy = series.createCopy(0, -1); // end < start, loop will not add anything
        assertEquals(0, copy.getItemCount());
    }

    // ---- Bound Index Getters ----

    @Test
    public void testInitialBoundIndices() {
        assertEquals(-1, series.getMinStartIndex());
        assertEquals(-1, series.getMaxStartIndex());
        assertEquals(-1, series.getMinMiddleIndex());
        assertEquals(-1, series.getMaxMiddleIndex());
        assertEquals(-1, series.getMinEndIndex());
        assertEquals(-1, series.getMaxEndIndex());
    }

    @Test
    public void testBoundsResetAfterClear() {
        series.add(new TimePeriodValue(new SimpleTimePeriod(new Date(1000), new Date(2000)), 1));
        series.delete(0, 0);
        assertEquals(-1, series.getMinStartIndex());
        assertEquals(-1, series.getMaxStartIndex());
        assertEquals(-1, series.getMinMiddleIndex());
        assertEquals(-1, series.getMaxMiddleIndex());
        assertEquals(-1, series.getMinEndIndex());
        assertEquals(-1, series.getMaxEndIndex());
    }

    @Test
    public void testBoundsWithMultipleAddsDeleteSingle() {
        SimpleTimePeriod p0 = new SimpleTimePeriod(new Date(1000), new Date(2000));
        SimpleTimePeriod p1 = new SimpleTimePeriod(new Date(3000), new Date(4000));
        series.add(new TimePeriodValue(p0, 1));
        series.add(new TimePeriodValue(p1, 2));
        series.delete(0, 0); // remove p0
        assertEquals(0, series.getMinStartIndex()); // p1 is now at index 0
        assertEquals(0, series.getMaxStartIndex());
        assertEquals(0, series.getMinMiddleIndex());
        assertEquals(0, series.getMaxMiddleIndex());
        assertEquals(0, series.getMinEndIndex());
        assertEquals(0, series.getMaxEndIndex());
    }
}
