package org.jfree.data.time;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test suite for {@link TimePeriodValues}.
 */
public class TimePeriodValuesTest implements SeriesChangeListener {

    private TimePeriodValues series;
    private int changeEventCount;

    @Before
    public void setUp() {
        this.series = new TimePeriodValues("Test Series");
        this.series.addChangeListener(this);
        this.changeEventCount = 0;
    }

    public void seriesChanged(SeriesChangeEvent event) {
        this.changeEventCount++;
    }

    @Test
    public void testConstructors() {
        TimePeriodValues s1 = new TimePeriodValues("Series 1");
        assertEquals("Series 1", s1.getKey());
        assertEquals("Time", s1.getDomainDescription());
        assertEquals("Value", s1.getRangeDescription());
        assertEquals(0, s1.getItemCount());

        TimePeriodValues s2 = new TimePeriodValues("Series 2", "Custom Domain", "Custom Range");
        assertEquals("Series 2", s2.getKey());
        assertEquals("Custom Domain", s2.getDomainDescription());
        assertEquals("Custom Range", s2.getRangeDescription());
        assertEquals(0, s2.getItemCount());
    }

    @Test
    public void testDomainAndRangeDescription() {
        this.series.setDomainDescription("New Domain");
        assertEquals("New Domain", this.series.getDomainDescription());

        this.series.setRangeDescription("New Range");
        assertEquals("New Range", this.series.getRangeDescription());

        this.series.setDomainDescription(null);
        assertNull(this.series.getDomainDescription());

        this.series.setRangeDescription(null);
        assertNull(this.series.getRangeDescription());
    }

    @Test
    public void testAddAndGet() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);

        this.series.add(new TimePeriodValue(d1, 10.0));
        assertEquals(1, this.series.getItemCount());
        assertEquals(1, this.changeEventCount);
        assertEquals(d1, this.series.getTimePeriod(0));
        assertEquals(new Double(10.0), this.series.getValue(0));

        this.series.add(d2, 20.0);
        assertEquals(2, this.series.getItemCount());
        assertEquals(2, this.changeEventCount);
        assertEquals(d2, this.series.getTimePeriod(1));
        assertEquals(new Double(20.0), this.series.getValue(1));

        Day d3 = new Day(3, 1, 2020);
        this.series.add(d3, (Number) null);
        assertEquals(3, this.series.getItemCount());
        assertEquals(3, this.changeEventCount);
        assertEquals(d3, this.series.getTimePeriod(2));
        assertNull(this.series.getValue(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        this.series.add((TimePeriodValue) null);
    }

    @Test
    public void testUpdate() {
        Day d1 = new Day(1, 1, 2020);
        this.series.add(d1, 50.0);
        assertEquals(1, this.changeEventCount);

        this.series.update(0, 100.0);
        assertEquals(new Double(100.0), this.series.getValue(0));
        assertEquals(2, this.changeEventCount);
    }

    @Test
    public void testDelete() {
        Day d1 = new Day(1, 1, 2020);
        Day d2 = new Day(2, 1, 2020);
        Day d3 = new Day(3, 1, 2020);
        Day d4 = new Day(4, 1, 2020);

        this.series.add(d1, 1.0);
        this.series.add(d2, 2.0);
        this.series.add(d3, 3.0);
        this.series.add(d4, 4.0);

        int initialEvents = this.changeEventCount;
        this.series.delete(1, 2);

        assertEquals(2, this.series.getItemCount());
        assertEquals(d1, this.series.getTimePeriod(0));
        assertEquals(d4, this.series.getTimePeriod(1));
        assertEquals(initialEvents + 1, this.changeEventCount);

        this.series.delete(0, 1);
        assertEquals(0, this.series.getItemCount());
        assertEquals(-1, this.series.getMinStartIndex());
        assertEquals(-1, this.series.getMaxStartIndex());
        assertEquals(-1, this.series.getMinMiddleIndex());
        assertEquals(-1, this.series.getMaxMiddleIndex());
        assertEquals(-1, this.series.getMinEndIndex());
        assertEquals(-1, this.series.getMaxEndIndex());
    }

    @Test
    public void testBoundsTracking() {
        assertEquals(-1, this.series.getMinStartIndex());
        assertEquals(-1, this.series.getMaxStartIndex());
        assertEquals(-1, this.series.getMinMiddleIndex());
        assertEquals(-1, this.series.getMaxMiddleIndex());
        assertEquals(-1, this.series.getMinEndIndex());
        assertEquals(-1, this.series.getMaxEndIndex());

        SimpleTimePeriod p1 = new SimpleTimePeriod(new Date(100L), new Date(200L)); // middle 150
        this.series.add(p1, 1.0);
        assertEquals(0, this.series.getMinStartIndex());
        assertEquals(0, this.series.getMaxStartIndex());
        assertEquals(0, this.series.getMinMiddleIndex());
        assertEquals(0, this.series.getMaxMiddleIndex());
        assertEquals(0, this.series.getMinEndIndex());
        assertEquals(0, this.series.getMaxEndIndex());

        SimpleTimePeriod p2 = new SimpleTimePeriod(new Date(50L), new Date(250L)); // middle 150
        this.series.add(p2, 2.0);
        assertEquals(1, this.series.getMinStartIndex());
        assertEquals(0, this.series.getMaxStartIndex());
        assertEquals(0, this.series.getMinMiddleIndex());
        assertEquals(0, this.series.getMaxMiddleIndex());
        assertEquals(0, this.series.getMinEndIndex());
        assertEquals(1, this.series.getMaxEndIndex());

        SimpleTimePeriod p3 = new SimpleTimePeriod(new Date(120L), new Date(180L)); // middle 150
        this.series.add(p3, 3.0);
        assertEquals(1, this.series.getMinStartIndex());
        assertEquals(2, this.series.getMaxStartIndex());
        assertEquals(0, this.series.getMinMiddleIndex());
        assertEquals(0, this.series.getMaxMiddleIndex());
        assertEquals(2, this.series.getMinEndIndex());
        assertEquals(1, this.series.getMaxEndIndex());

        SimpleTimePeriod p4 = new SimpleTimePeriod(new Date(60L), new Date(100L)); // middle 80
        this.series.add(p4, 4.0);
        assertEquals(1, this.series.getMinStartIndex());
        assertEquals(2, this.series.getMaxStartIndex());
        assertEquals(3, this.series.getMinMiddleIndex());
        assertEquals(2, this.series.getMinEndIndex());
        assertEquals(1, this.series.getMaxEndIndex());

        SimpleTimePeriod p5 = new SimpleTimePeriod(new Date(150L), new Date(250L)); // middle 200
        this.series.add(p5, 5.0);
        assertEquals(1, this.series.getMinStartIndex());
        assertEquals(4, this.series.getMaxStartIndex());
        assertEquals(3, this.series.getMinMiddleIndex());
        assertEquals(4, this.series.getMaxMiddleIndex());
        assertEquals(2, this.series.getMinEndIndex());
        assertEquals(1, this.series.getMaxEndIndex());
    }

    @Test
    public void testEqualsAndHashCode() {
        TimePeriodValues s1 = new TimePeriodValues("Series", "D", "R");
        TimePeriodValues s2 = new TimePeriodValues("Series", "D", "R");

        assertTrue(s1.equals(s1));
        assertFalse(s1.equals(null));
        assertFalse(s1.equals("Not a TimePeriodValues"));

        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.setDomainDescription("D2");
        assertFalse(s1.equals(s2));
        s2.setDomainDescription("D2");
        assertTrue(s1.equals(s2));

        s1.setRangeDescription("R2");
        assertFalse(s1.equals(s2));
        s2.setRangeDescription("R2");
        assertTrue(s1.equals(s2));

        TimePeriodValues sDiffKey = new TimePeriodValues("Different Key", "D2", "R2");
        assertFalse(s1.equals(sDiffKey));

        s1.add(new Day(1, 1, 2020), 10.0);
        assertFalse(s1.equals(s2));
        s2.add(new Day(1, 1, 2020), 10.0);
        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Day(2, 1, 2020), 20.0);
        s2.add(new Day(2, 1, 2020), 30.0);
        assertFalse(s1.equals(s2));

        TimePeriodValues sNulls1 = new TimePeriodValues("S", null, null);
        TimePeriodValues sNulls2 = new TimePeriodValues("S", null, null);
        assertTrue(sNulls1.equals(sNulls2));
        assertEquals(sNulls1.hashCode(), sNulls2.hashCode());
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        TimePeriodValues s1 = new TimePeriodValues("Series");
        s1.add(new Day(1, 1, 2020), 10.0);
        s1.add(new Day(2, 1, 2020), 20.0);

        TimePeriodValues s2 = (TimePeriodValues) s1.clone();
        assertTrue(s1 != s2);
        assertEquals(s1.getClass(), s2.getClass());
        assertTrue(s1.equals(s2));

        s2.add(new Day(3, 1, 2020), 30.0);
        assertFalse(s1.equals(s2));
        assertEquals(2, s1.getItemCount());
        assertEquals(3, s2.getItemCount());
    }

    @Test
    public void testCreateCopy() throws CloneNotSupportedException {
        TimePeriodValues s1 = new TimePeriodValues("Series");
        TimePeriodValues emptyCopy = s1.createCopy(0, -1);
        assertEquals(0, emptyCopy.getItemCount());

        s1.add(new Day(1, 1, 2020), 10.0);
        s1.add(new Day(2, 1, 2020), 20.0);
        s1.add(new Day(3, 1, 2020), 30.0);
        s1.add(new Day(4, 1, 2020), 40.0);

        TimePeriodValues copy = s1.createCopy(1, 2);
        assertEquals(2, copy.getItemCount());
        assertEquals(new Day(2, 1, 2020), copy.getTimePeriod(0));
        assertEquals(new Double(20.0), copy.getValue(0));
        assertEquals(new Day(3, 1, 2020), copy.getTimePeriod(1));
        assertEquals(new Double(30.0), copy.getValue(1));
    }

    @Test
    public void testSerialization() {
        TimePeriodValues s1 = new TimePeriodValues("Series", "Time", "Value");
        s1.add(new Day(1, 1, 2020), 100.0);
        s1.add(new Day(2, 1, 2020), 200.0);

        TimePeriodValues s2 = null;
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(buffer);
            out.writeObject(s1);
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
            s2 = (TimePeriodValues) in.readObject();
            in.close();
        } catch (Exception e) {
            fail("Serialization failed: " + e.getMessage());
        }

        assertNotNull(s2);
        assertEquals(s1, s2);
        assertEquals(s1.getMinStartIndex(), s2.getMinStartIndex());
        assertEquals(s1.getMaxStartIndex(), s2.getMaxStartIndex());
        assertEquals(s1.getMinMiddleIndex(), s2.getMinMiddleIndex());
        assertEquals(s1.getMaxMiddleIndex(), s2.getMaxMiddleIndex());
        assertEquals(s1.getMinEndIndex(), s2.getMinEndIndex());
        assertEquals(s1.getMaxEndIndex(), s2.getMaxEndIndex());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemOutOfBounds() {
        this.series.getDataItem(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTimePeriodOutOfBounds() {
        this.series.getTimePeriod(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfBounds() {
        this.series.getValue(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateOutOfBounds() {
        this.series.update(0, 10.0);
    }
}