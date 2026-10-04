package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AxisTest {

    private TestAxis axis;
    private Graphics2D g2;

    @Before
    public void setUp() {
        axis = new TestAxis("Test Label");
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        g2 = img.createGraphics();
    }

    private static class TestAxis extends Axis {
        private boolean configureCalled = false;

        public TestAxis(String label) {
            super(label);
        }

        @Override
        public void configure() {
            configureCalled = true;
        }

        public boolean isConfigureCalled() {
            return configureCalled;
        }

        public void resetConfigureCalled() {
            configureCalled = false;
        }

        @Override
        public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {
            return null;
        }

        @Override
        public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
            return null;
        }

        @Override
        public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
            return null;
        }

        public Rectangle2D getLabelEnclosurePublic(Graphics2D g2, RectangleEdge edge) {
            return getLabelEnclosure(g2, edge);
        }

        public AxisState drawLabelPublic(String label, Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, AxisState state, PlotRenderingInfo plotState) {
            return drawLabel(label, g2, plotArea, dataArea, edge, state, plotState);
        }

        public void drawAxisLinePublic(Graphics2D g2, double cursor, Rectangle2D dataArea, RectangleEdge edge) {
            drawAxisLine(g2, cursor, dataArea, edge);
        }
    }

    private static class TestPlot extends Plot {
        @Override
        public String getPlotType() {
            return null;
        }

        @Override
        public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
        }
    }

    @Test
    public void testDefaultValues() {
        Assert.assertTrue(axis.isVisible());
        Assert.assertEquals("Test Label", axis.getLabel());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LABEL_FONT, axis.getLabelFont());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LABEL_PAINT, axis.getLabelPaint());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LABEL_INSETS, axis.getLabelInsets());
        Assert.assertEquals(0.0, axis.getLabelAngle(), 0.0);
        Assert.assertNull(axis.getLabelToolTip());
        Assert.assertNull(axis.getLabelURL());
        Assert.assertTrue(axis.isAxisLineVisible());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LINE_PAINT, axis.getAxisLinePaint());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LINE_STROKE, axis.getAxisLineStroke());
        Assert.assertTrue(axis.isTickLabelsVisible());
        Assert.assertEquals(Axis.DEFAULT_TICK_LABEL_FONT, axis.getTickLabelFont());
        Assert.assertEquals(Axis.DEFAULT_TICK_LABEL_PAINT, axis.getTickLabelPaint());
        Assert.assertEquals(Axis.DEFAULT_TICK_LABEL_INSETS, axis.getTickLabelInsets());
        Assert.assertTrue(axis.isTickMarksVisible());
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_STROKE, axis.getTickMarkStroke());
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_PAINT, axis.getTickMarkPaint());
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH, axis.getTickMarkInsideLength(), 0.0);
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH, axis.getTickMarkOutsideLength(), 0.0);
        Assert.assertNull(axis.getPlot());
        Assert.assertEquals(0.0, axis.getFixedDimension(), 0.0);
    }

    @Test
    public void testSetVisible() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setVisible(false);
        Assert.assertFalse(axis.isVisible());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setVisible(false);
        Assert.assertFalse(notified[0]);
    }

    @Test
    public void testSetLabel() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setLabel("New Label");
        Assert.assertEquals("New Label", axis.getLabel());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setLabel("New Label");
        Assert.assertFalse(notified[0]);
        axis.setLabel(null);
        Assert.assertNull(axis.getLabel());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setLabel(null);
        Assert.assertFalse(notified[0]);
        axis.setLabel("Another");
        Assert.assertEquals("Another", axis.getLabel());
        Assert.assertTrue(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelFontNull() {
        axis.setLabelFont(null);
    }

    @Test
    public void testSetLabelFont() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Font newFont = new Font("Serif", Font.BOLD, 14);
        axis.setLabelFont(newFont);
        Assert.assertEquals(newFont, axis.getLabelFont());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setLabelFont(newFont);
        Assert.assertFalse(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaintNull() {
        axis.setLabelPaint(null);
    }

    @Test
    public void testSetLabelPaint() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Paint newPaint = Color.red;
        axis.setLabelPaint(newPaint);
        Assert.assertEquals(newPaint, axis.getLabelPaint());
        Assert.assertTrue(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelInsetsNull() {
        axis.setLabelInsets(null);
    }

    @Test
    public void testSetLabelInsets() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        RectangleInsets insets = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
        axis.setLabelInsets(insets);
        Assert.assertEquals(insets, axis.getLabelInsets());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setLabelInsets(insets);
        Assert.assertFalse(notified[0]);
    }

    @Test
    public void testSetLabelAngle() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setLabelAngle(1.5);
        Assert.assertEquals(1.5, axis.getLabelAngle(), 0.0);
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testSetLabelToolTip() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setLabelToolTip("Tip");
        Assert.assertEquals("Tip", axis.getLabelToolTip());
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testSetLabelURL() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setLabelURL("http://example.com");
        Assert.assertEquals("http://example.com", axis.getLabelURL());
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testSetAxisLineVisible() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setAxisLineVisible(false);
        Assert.assertFalse(axis.isAxisLineVisible());
        Assert.assertTrue(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisLinePaintNull() {
        axis.setAxisLinePaint(null);
    }

    @Test
    public void testSetAxisLinePaint() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Paint paint = Color.blue;
        axis.setAxisLinePaint(paint);
        Assert.assertEquals(paint, axis.getAxisLinePaint());
        Assert.assertTrue(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisLineStrokeNull() {
        axis.setAxisLineStroke(null);
    }

    @Test
    public void testSetAxisLineStroke() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Stroke stroke = new BasicStroke(2.0f);
        axis.setAxisLineStroke(stroke);
        Assert.assertEquals(stroke, axis.getAxisLineStroke());
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testSetTickLabelsVisible() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setTickLabelsVisible(false);
        Assert.assertFalse(axis.isTickLabelsVisible());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setTickLabelsVisible(false);
        Assert.assertFalse(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickLabelFontNull() {
        axis.setTickLabelFont(null);
    }

    @Test
    public void testSetTickLabelFont() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Font font = new Font("Serif", Font.ITALIC, 12);
        axis.setTickLabelFont(font);
        Assert.assertEquals(font, axis.getTickLabelFont());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setTickLabelFont(font);
        Assert.assertFalse(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickLabelPaintNull() {
        axis.setTickLabelPaint(null);
    }

    @Test
    public void testSetTickLabelPaint() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Paint paint = Color.green;
        axis.setTickLabelPaint(paint);
        Assert.assertEquals(paint, axis.getTickLabelPaint());
        Assert.assertTrue(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickLabelInsetsNull() {
        axis.setTickLabelInsets(null);
    }

    @Test
    public void testSetTickLabelInsets() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        RectangleInsets insets = new RectangleInsets(1.0, 2.0, 3.0, 4.0);
        axis.setTickLabelInsets(insets);
        Assert.assertEquals(insets, axis.getTickLabelInsets());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setTickLabelInsets(insets);
        Assert.assertFalse(notified[0]);
    }

    @Test
    public void testSetTickMarksVisible() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setTickMarksVisible(false);
        Assert.assertFalse(axis.isTickMarksVisible());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setTickMarksVisible(false);
        Assert.assertFalse(notified[0]);
    }

    @Test
    public void testSetTickMarkInsideLength() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setTickMarkInsideLength(5.0f);
        Assert.assertEquals(5.0f, axis.getTickMarkInsideLength(), 0.0);
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testSetTickMarkOutsideLength() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        axis.setTickMarkOutsideLength(3.0f);
        Assert.assertEquals(3.0f, axis.getTickMarkOutsideLength(), 0.0);
        Assert.assertTrue(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickMarkStrokeNull() {
        axis.setTickMarkStroke(null);
    }

    @Test
    public void testSetTickMarkStroke() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Stroke stroke = new BasicStroke(2.0f);
        axis.setTickMarkStroke(stroke);
        Assert.assertEquals(stroke, axis.getTickMarkStroke());
        Assert.assertTrue(notified[0]);
        notified[0] = false;
        axis.setTickMarkStroke(stroke);
        Assert.assertFalse(notified[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickMarkPaintNull() {
        axis.setTickMarkPaint(null);
    }

    @Test
    public void testSetTickMarkPaint() {
        final boolean[] notified = {false};
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {
                notified[0] = true;
            }
        };
        axis.addChangeListener(listener);
        Paint paint = Color.yellow;
        axis.setTickMarkPaint(paint);
        Assert.assertEquals(paint, axis.getTickMarkPaint());
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testSetPlot() {
        Plot plot = new TestPlot();
        axis.resetConfigureCalled();
        axis.setPlot(plot);
        Assert.assertSame(plot, axis.getPlot());
        Assert.assertTrue(axis.isConfigureCalled());
    }

    @Test
    public void testSetFixedDimension() {
        axis.setFixedDimension(10.0);
        Assert.assertEquals(10.0, axis.getFixedDimension(), 0.0);
    }

    @Test
    public void testAddRemoveHasListener() {
        AxisChangeListener listener = new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {}
        };
        Assert.assertFalse(axis.hasListener(listener));
        axis.addChangeListener(listener);
        Assert.assertTrue(axis.hasListener(listener));
        axis.removeChangeListener(listener);
        Assert.assertFalse(axis.hasListener(listener));
    }

    @Test
    public void testGetLabelEnclosureNullLabel() {
        axis.setLabel(null);
        Rectangle2D enclosure = axis.getLabelEnclosurePublic(g2, RectangleEdge.TOP);
        Assert.assertTrue(enclosure.isEmpty());
    }

    @Test
    public void testGetLabelEnclosureEmptyLabel() {
        axis.setLabel("");
        Rectangle2D enclosure = axis.getLabelEnclosurePublic(g2, RectangleEdge.TOP);
        Assert.assertTrue(enclosure.isEmpty());
    }

    @Test
    public void testGetLabelEnclosureNonEmpty() {
        axis.setLabel("Test");
        Rectangle2D enclosure = axis.getLabelEnclosurePublic(g2, RectangleEdge.TOP);
        Assert.assertFalse(enclosure.isEmpty());
        enclosure = axis.getLabelEnclosurePublic(g2, RectangleEdge.LEFT);
        Assert.assertFalse(enclosure.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDrawLabelNullState() {
        axis.drawLabelPublic("Label", g2, new Rectangle2D.Double(), new Rectangle2D.Double(), RectangleEdge.TOP, null, null);
    }

    @Test
    public void testDrawLabelNullLabel() {
        AxisState state = new AxisState();
        AxisState result = axis.drawLabelPublic(null, g2, new Rectangle2D.Double(), new Rectangle2D.Double(), RectangleEdge.TOP, state, null);
        Assert.assertSame(state, result);
    }

    @Test
    public void testDrawLabelEmptyLabel() {
        AxisState state = new AxisState();
        AxisState result = axis.drawLabelPublic("", g2, new Rectangle2D.Double(), new Rectangle2D.Double(), RectangleEdge.TOP, state, null);
        Assert.assertSame(state, result);
    }

    @Test
    public void testDrawLabelTop() {
        AxisState state = new AxisState();
        state.setCursor(50.0);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        AxisState result = axis.drawLabelPublic("Test", g2, new Rectangle2D.Double(), dataArea, RectangleEdge.TOP, state, null);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDrawLabelBottom() {
        AxisState state = new AxisState();
        state.setCursor(50.0);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        AxisState result = axis.drawLabelPublic("Test", g2, new Rectangle2D.Double(), dataArea, RectangleEdge.BOTTOM, state, null);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDrawLabelLeft() {
        AxisState state = new AxisState();
        state.setCursor(50.0);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        AxisState result = axis.drawLabelPublic("Test", g2, new Rectangle2D.Double(), dataArea, RectangleEdge.LEFT, state, null);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDrawLabelRight() {
        AxisState state = new AxisState();
        state.setCursor(50.0);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        AxisState result = axis.drawLabelPublic("Test", g2, new Rectangle2D.Double(), dataArea, RectangleEdge.RIGHT, state, null);
        Assert.assertNotNull(result);
    }

    @Test
    public void testDrawAxisLineTop() {
        axis.drawAxisLinePublic(g2, 50.0, new Rectangle2D.Double(10, 10, 80, 80), RectangleEdge.TOP);
    }

    @Test
    public void testDrawAxisLineBottom() {
        axis.drawAxisLinePublic(g2, 50.0, new Rectangle2D.Double(10, 10, 80, 80), RectangleEdge.BOTTOM);
    }

    @Test
    public void testDrawAxisLineLeft() {
        axis.drawAxisLinePublic(g2, 50.0, new Rectangle2D.Double(10, 10, 80, 80), RectangleEdge.LEFT);
    }

    @Test
    public void testDrawAxisLineRight() {
        axis.drawAxisLinePublic(g2, 50.0, new Rectangle2D.Double(10, 10, 80, 80), RectangleEdge.RIGHT);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        Axis clone = (Axis) axis.clone();
        Assert.assertNotSame(axis, clone);
        Assert.assertEquals(axis.isVisible(), clone.isVisible());
        Assert.assertEquals(axis.getLabel(), clone.getLabel());
        Assert.assertEquals(axis.getLabelFont(), clone.getLabelFont());
        Assert.assertEquals(axis.getLabelPaint(), clone.getLabelPaint());
        Assert.assertEquals(axis.getLabelInsets(), clone.getLabelInsets());
        Assert.assertEquals(axis.getLabelAngle(), clone.getLabelAngle(), 0.0);
        Assert.assertEquals(axis.getLabelToolTip(), clone.getLabelToolTip());
        Assert.assertEquals(axis.getLabelURL(), clone.getLabelURL());
        Assert.assertEquals(axis.isAxisLineVisible(), clone.isAxisLineVisible());
        Assert.assertEquals(axis.getAxisLinePaint(), clone.getAxisLinePaint());
        Assert.assertEquals(axis.getAxisLineStroke(), clone.getAxisLineStroke());
        Assert.assertEquals(axis.isTickLabelsVisible(), clone.isTickLabelsVisible());
        Assert.assertEquals(axis.getTickLabelFont(), clone.getTickLabelFont());
        Assert.assertEquals(axis.getTickLabelPaint(), clone.getTickLabelPaint());
        Assert.assertEquals(axis.getTickLabelInsets(), clone.getTickLabelInsets());
        Assert.assertEquals(axis.isTickMarksVisible(), clone.isTickMarksVisible());
        Assert.assertEquals(axis.getTickMarkInsideLength(), clone.getTickMarkInsideLength(), 0.0);
        Assert.assertEquals(axis.getTickMarkOutsideLength(), clone.getTickMarkOutsideLength(), 0.0);
        Assert.assertEquals(axis.getTickMarkPaint(), clone.getTickMarkPaint());
        Assert.assertEquals(axis.getTickMarkStroke(), clone.getTickMarkStroke());
        Assert.assertEquals(axis.getFixedDimension(), clone.getFixedDimension(), 0.0);
        Assert.assertNull(clone.getPlot());
        Assert.assertFalse(clone.hasListener(new AxisChangeListener() {
            public void axisChanged(AxisChangeEvent event) {}
        }));
    }

    @Test
    public void testEquals() {
        Axis axis1 = new TestAxis("Label");
        Axis axis2 = new TestAxis("Label");
        Assert.assertTrue(axis1.equals(axis2));
        Assert.assertTrue(axis1.equals(axis1));
        Assert.assertFalse(axis1.equals(null));
        Assert.assertFalse(axis1.equals(new Object()));

        axis2.setVisible(false);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setVisible(true);

        axis2.setLabel("Different");
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setLabel("Label");

        axis2.setLabelFont(new Font("Serif", Font.PLAIN, 12));
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setLabelFont(Axis.DEFAULT_AXIS_LABEL_FONT);

        axis2.setLabelPaint(Color.red);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setLabelPaint(Axis.DEFAULT_AXIS_LABEL_PAINT);

        axis2.setLabelInsets(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setLabelInsets(Axis.DEFAULT_AXIS_LABEL_INSETS);

        axis2.setLabelAngle(1.0);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setLabelAngle(0.0);

        axis2.setLabelToolTip("tip");
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setLabelToolTip(null);

        axis2.setLabelURL("url");
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setLabelURL(null);

        axis2.setAxisLineVisible(false);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setAxisLineVisible(true);

        axis2.setAxisLineStroke(new BasicStroke(2.0f));
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setAxisLineStroke(Axis.DEFAULT_AXIS_LINE_STROKE);

        axis2.setAxisLinePaint(Color.blue);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setAxisLinePaint(Axis.DEFAULT_AXIS_LINE_PAINT);

        axis2.setTickLabelsVisible(false);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickLabelsVisible(true);

        axis2.setTickLabelFont(new Font("Serif", Font.PLAIN, 12));
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickLabelFont(Axis.DEFAULT_TICK_LABEL_FONT);

        axis2.setTickLabelPaint(Color.green);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickLabelPaint(Axis.DEFAULT_TICK_LABEL_PAINT);

        axis2.setTickLabelInsets(new RectangleInsets(1.0, 2.0, 3.0, 4.0));
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickLabelInsets(Axis.DEFAULT_TICK_LABEL_INSETS);

        axis2.setTickMarksVisible(false);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickMarksVisible(true);

        axis2.setTickMarkInsideLength(5.0f);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickMarkInsideLength(Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH);

        axis2.setTickMarkOutsideLength(5.0f);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickMarkOutsideLength(Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH);

        axis2.setTickMarkPaint(Color.yellow);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickMarkPaint(Axis.DEFAULT_TICK_MARK_PAINT);

        axis2.setTickMarkStroke(new BasicStroke(2.0f));
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setTickMarkStroke(Axis.DEFAULT_TICK_MARK_STROKE);

        axis2.setFixedDimension(10.0);
        Assert.assertFalse(axis1.equals(axis2));
        axis2.setFixedDimension(0.0);

        Assert.assertTrue(axis1.equals(axis2));
    }
}
