package org.jfree.chart.plot;

import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;
import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.event.MarkerChangeListener;

public class ValueMarkerTest {

    @Test
    public void testConstructorWithValue() {
        ValueMarker marker = new ValueMarker(1.0);
        assertEquals(1.0, marker.getValue(), 0.0000001);
    }

    @Test
    public void testConstructorWithValuePaintStroke() {
        Paint paint = Color.RED;
        Stroke stroke = new BasicStroke(1.0f);
        ValueMarker marker = new ValueMarker(2.0, paint, stroke);
        assertEquals(2.0, marker.getValue(), 0.0000001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullPaint() {
        new ValueMarker(1.0, null, new BasicStroke(1.0f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStroke() {
        new ValueMarker(1.0, Color.BLUE, null);
    }

    @Test
    public void testConstructorFull() {
        Paint paint = Color.BLUE;
        Stroke stroke = new BasicStroke(2.0f);
        Paint outlinePaint = Color.GREEN;
        Stroke outlineStroke = new BasicStroke(1.0f);
        float alpha = 0.5f;
        ValueMarker marker = new ValueMarker(3.0, paint, stroke, outlinePaint, outlineStroke, alpha);
        assertEquals(3.0, marker.getValue(), 0.0000001);
    }

    @Test
    public void testConstructorFullWithNullOutlinePaintAndStroke() {
        ValueMarker marker = new ValueMarker(4.0, Color.ORANGE, new BasicStroke(1.0f), null, null, 0.8f);
        assertNotNull(marker);
        assertEquals(4.0, marker.getValue(), 0.0000001);
    }

    @Test
    public void testGetValue() {
        ValueMarker marker = new ValueMarker(5.0);
        assertEquals(5.0, marker.getValue(), 0.0000001);
    }

    @Test
    public void testSetValue() {
        ValueMarker marker = new ValueMarker(1.0);
        marker.setValue(10.0);
        assertEquals(10.0, marker.getValue(), 0.0000001);
    }

    @Test
    public void testSetValueNotifiesListeners() {
        ValueMarker marker = new ValueMarker(1.0);
        final boolean[] notified = {false};
        marker.addChangeListener(new MarkerChangeListener() {
            @Override
            public void markerChanged(MarkerChangeEvent event) {
                notified[0] = true;
            }
        });
        marker.setValue(2.0);
        assertTrue(notified[0]);
    }

    @Test
    public void testEqualsSameObject() {
        ValueMarker marker = new ValueMarker(1.0);
        assertTrue(marker.equals(marker));
    }

    @Test
    public void testEqualsNull() {
        ValueMarker marker = new ValueMarker(1.0);
        assertFalse(marker.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        ValueMarker marker = new ValueMarker(1.0);
        assertFalse(marker.equals("not a marker"));
    }

    @Test
    public void testEqualsSameValues() {
        ValueMarker marker1 = new ValueMarker(2.0, Color.BLUE, new BasicStroke(1.0f));
        ValueMarker marker2 = new ValueMarker(2.0, Color.BLUE, new BasicStroke(1.0f));
        assertTrue(marker1.equals(marker2));
        assertTrue(marker2.equals(marker1));
    }

    @Test
    public void testEqualsDifferentValue() {
        ValueMarker marker1 = new ValueMarker(1.0, Color.BLUE, new BasicStroke(1.0f));
        ValueMarker marker2 = new ValueMarker(2.0, Color.BLUE, new BasicStroke(1.0f));
        assertFalse(marker1.equals(marker2));
    }

    @Test
    public void testEqualsDifferentPaint() {
        ValueMarker marker1 = new ValueMarker(1.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker marker2 = new ValueMarker(1.0, Color.BLUE, new BasicStroke(1.0f));
        assertFalse(marker1.equals(marker2));
    }

    @Test
    public void testEqualsDifferentStroke() {
        BasicStroke stroke1 = new BasicStroke(1.0f);
        BasicStroke stroke2 = new BasicStroke(2.0f);
        ValueMarker marker1 = new ValueMarker(1.0, Color.BLUE, stroke1);
        ValueMarker marker2 = new ValueMarker(1.0, Color.BLUE, stroke2);
        assertFalse(marker1.equals(marker2));
    }

    @Test
    public void testEqualsIgnoreOutlinePaintStrokeWhenNull() {
        // Full constructor with null outlinePaint/outlineStroke
        ValueMarker marker1 = new ValueMarker(1.0, Color.BLUE, new BasicStroke(1.0f), null, null, 0.5f);
        ValueMarker marker2 = new ValueMarker(1.0, Color.BLUE, new BasicStroke(1.0f), null, null, 0.5f);
        assertTrue(marker1.equals(marker2));
    }

    @Test
    public void testEqualsDifferentAlpha() {
        ValueMarker marker1 = new ValueMarker(1.0, Color.BLUE, new BasicStroke(1.0f), null, null, 0.5f);
        ValueMarker marker2 = new ValueMarker(1.0, Color.BLUE, new BasicStroke(1.0f), null, null, 0.8f);
        // super.equals should compare alpha, so they should be different
        assertFalse(marker1.equals(marker2));
    }
}
