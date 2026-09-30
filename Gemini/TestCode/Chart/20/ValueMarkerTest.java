package org.jfree.chart.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.event.MarkerChangeListener;
import org.junit.Test;

/**
 * Tests for the {@link ValueMarker} class.
 */
public class ValueMarkerTest implements MarkerChangeListener {

    private MarkerChangeEvent lastEvent;

    /**
     * Responds to a marker change event.
     *
     * @param event the event.
     */
    public void markerChanged(MarkerChangeEvent event) {
        this.lastEvent = event;
    }

    /**
     * Test the single argument constructor and getValue().
     */
    @Test
    public void testConstructor1() {
        ValueMarker m = new ValueMarker(45.0);
        assertEquals(45.0, m.getValue(), 0.000001);
        assertNotNull(m.getPaint());
        assertNotNull(m.getStroke());
        assertEquals(0.8f, m.getAlpha(), 0.00001f);
    }

    /**
     * Test the three argument constructor.
     */
    @Test
    public void testConstructor2() {
        Paint paint = Color.red;
        Stroke stroke = new BasicStroke(1.5f);
        ValueMarker m = new ValueMarker(12.34, paint, stroke);

        assertEquals(12.34, m.getValue(), 0.000001);
        assertEquals(paint, m.getPaint());
        assertEquals(stroke, m.getStroke());
        assertEquals(1.0f, m.getAlpha(), 0.00001f);
    }

    /**
     * Test the six argument constructor.
     */
    @Test
    public void testConstructor3() {
        Paint paint = Color.blue;
        Stroke stroke = new BasicStroke(2.0f);
        Paint outlinePaint = Color.green;
        Stroke outlineStroke = new BasicStroke(0.5f);
        float alpha = 0.5f;

        ValueMarker m = new ValueMarker(100.0, paint, stroke, outlinePaint, outlineStroke, alpha);
        assertEquals(100.0, m.getValue(), 0.000001);
        assertEquals(paint, m.getPaint());
        assertEquals(stroke, m.getStroke());
        assertEquals(alpha, m.getAlpha(), 0.00001f);
    }

    /**
     * Test setting the value and the event notification mechanism.
     */
    @Test
    public void testSetValueAndNotification() {
        ValueMarker m = new ValueMarker(10.0);
        m.addChangeListener(this);
        this.lastEvent = null;

        m.setValue(20.0);
        assertEquals(20.0, m.getValue(), 0.000001);
        assertNotNull(this.lastEvent);
        assertEquals(m, this.lastEvent.getMarker());

        // Update with same value should still notify
        this.lastEvent = null;
        m.setValue(20.0);
        assertNotNull(this.lastEvent);
    }

    /**
     * Test the equals() method covering all branches.
     */
    @Test
    public void testEquals() {
        ValueMarker m1 = new ValueMarker(50.0, Color.blue, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(50.0, Color.blue, new BasicStroke(1.0f));

        // Reflexive
        assertTrue(m1.equals(m1));

        // Symmetric and equal
        assertTrue(m1.equals(m2));
        assertTrue(m2.equals(m1));

        // Value difference
        m2.setValue(50.1);
        assertFalse(m1.equals(m2));
        assertFalse(m2.equals(m1));
        m2.setValue(50.0);

        // Super class differences (paint, stroke, alpha, label, etc.)
        m2.setPaint(Color.red);
        assertFalse(m1.equals(m2));
        m2.setPaint(Color.blue);

        m2.setStroke(new BasicStroke(2.0f));
        assertFalse(m1.equals(m2));
        m2.setStroke(new BasicStroke(1.0f));

        m2.setAlpha(0.25f);
        assertFalse(m1.equals(m2));
        m2.setAlpha(1.0f);

        m2.setLabel("Test");
        assertFalse(m1.equals(m2));
        m2.setLabel(null);

        // Non-ValueMarker Marker subclass or different class
        IntervalMarker im = new IntervalMarker(50.0, 50.0, Color.blue, new BasicStroke(1.0f), Color.blue, new BasicStroke(1.0f), 1.0f);
        assertFalse(m1.equals(im));

        // Null and Object of different type
        assertFalse(m1.equals(null));
        assertFalse(m1.equals("Not a marker"));
    }

    /**
     * Test special double values (NaN, Infinities).
     */
    @Test
    public void testSpecialDoubleValues() {
        ValueMarker m1 = new ValueMarker(Double.NaN);
        ValueMarker m2 = new ValueMarker(Double.NaN);
        // Note: double == double with NaN returns false in standard Java equality
        assertFalse(m1.equals(m2));

        ValueMarker posInf1 = new ValueMarker(Double.POSITIVE_INFINITY);
        ValueMarker posInf2 = new ValueMarker(Double.POSITIVE_INFINITY);
        assertTrue(posInf1.equals(posInf2));

        ValueMarker negInf = new ValueMarker(Double.NEGATIVE_INFINITY);
        assertFalse(posInf1.equals(negInf));
    }

    /**
     * Test cloning for ValueMarker.
     */
    @Test
    public void testCloning() throws CloneNotSupportedException {
        ValueMarker m1 = new ValueMarker(25.0, Color.green, new BasicStroke(1.2f));
        ValueMarker m2 = (ValueMarker) m1.clone();

        assertTrue(m1 != m2);
        assertTrue(m1.getClass() == m2.getClass());
        assertTrue(m1.equals(m2));

        // Verify independent modifications
        m2.setValue(30.0);
        assertFalse(m1.equals(m2));
    }

    /**
     * Test serialization of ValueMarker.
     */
    @Test
    public void testSerialization() {
        ValueMarker m1 = new ValueMarker(123.456, new GradientPaint(0.0f, 0.0f, Color.red, 1.0f, 1.0f, Color.white), new BasicStroke(2.5f));
        ValueMarker m2 = null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(buffer);
            out.writeObject(m1);
            out.close();

            ByteArrayInputStream inBuffer = new ByteArrayInputStream(buffer.toByteArray());
            ObjectInputStream in = new ObjectInputStream(inBuffer);
            m2 = (ValueMarker) in.readObject();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        assertEquals(m1, m2);
    }
}