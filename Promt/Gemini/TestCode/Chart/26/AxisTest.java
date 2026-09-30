package org.jfree.chart.axis;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

public class AxisTest {

    private static class TestAxis extends Axis {
        private static final long serialVersionUID = 1L;
        private boolean configureCalled = false;

        public TestAxis(String label) {
            super(label);
        }

        @Override
        public void configure() {
            this.configureCalled = true;
        }

        @Override
        public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {
            return space != null ? space : new AxisSpace();
        }

        @Override
        public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge, PlotRenderingInfo plotState) {
            AxisState state = new AxisState(cursor);
            if (isAxisLineVisible()) {
                drawAxisLine(g2, cursor, dataArea, edge);
            }
            drawLabel(getLabel(), g2, plotArea, dataArea, edge, state, plotState);
            return state;
        }

        @Override
        public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
            return Collections.emptyList();
        }
    }

    private static class RecordingListener implements AxisChangeListener {
        private int changeCount = 0;
        private AxisChangeEvent lastEvent = null;

        @Override
        public void axisChanged(AxisChangeEvent event) {
            this.changeCount++;
            this.lastEvent = event;
        }
    }

    private TestAxis axis;
    private RecordingListener listener;
    private Graphics2D g2;

    @Before
    public void setUp() {
        this.axis = new TestAxis("Test Label");
        this.listener = new RecordingListener();
        this.axis.addChangeListener(this.listener);
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        this.g2 = image.createGraphics();
    }

    @Test
    public void testConstructorAndDefaults() {
        TestAxis a = new TestAxis("Default");
        Assert.assertEquals("Default", a.getLabel());
        Assert.assertTrue(a.isVisible());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LABEL_FONT, a.getLabelFont());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LABEL_PAINT, a.getLabelPaint());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LABEL_INSETS, a.getLabelInsets());
        Assert.assertEquals(0.0, a.getLabelAngle(), 1e-9);
        Assert.assertNull(a.getLabelToolTip());
        Assert.assertNull(a.getLabelURL());

        Assert.assertTrue(a.isAxisLineVisible());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LINE_PAINT, a.getAxisLinePaint());
        Assert.assertEquals(Axis.DEFAULT_AXIS_LINE_STROKE, a.getAxisLineStroke());

        Assert.assertTrue(a.isTickLabelsVisible());
        Assert.assertEquals(Axis.DEFAULT_TICK_LABEL_FONT, a.getTickLabelFont());
        Assert.assertEquals(Axis.DEFAULT_TICK_LABEL_PAINT, a.getTickLabelPaint());
        Assert.assertEquals(Axis.DEFAULT_TICK_LABEL_INSETS, a.getTickLabelInsets());

        Assert.assertTrue(a.isTickMarksVisible());
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_STROKE, a.getTickMarkStroke());
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_PAINT, a.getTickMarkPaint());
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH, a.getTickMarkInsideLength(), 1e-9f);
        Assert.assertEquals(Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH, a.getTickMarkOutsideLength(), 1e-9f);

        Assert.assertNull(a.getPlot());
        Assert.assertEquals(0.0, a.getFixedDimension(), 1e-9);
    }

    @Test
    public void testSetVisible() {
        axis.setVisible(false);
        Assert.assertFalse(axis.isVisible());
        Assert.assertEquals(1, listener.changeCount);

        axis.setVisible(false);
        Assert.assertEquals(1, listener.changeCount);

        axis.setVisible(true);
        Assert.assertTrue(axis.isVisible());
        Assert.assertEquals(2, listener.changeCount);
    }

    @Test
    public void testSetLabel() {
        axis.setLabel("New Label");
        Assert.assertEquals("New Label", axis.getLabel());
        Assert.assertEquals(1, listener.changeCount);

        axis.setLabel("New Label");
        Assert.assertEquals(1, listener.changeCount);

        axis.setLabel(null);
        Assert.assertNull(axis.getLabel());
        Assert.assertEquals(2, listener.changeCount);

        axis.setLabel(null);
        Assert.assertEquals(2, listener.changeCount);

        axis.setLabel("Restored");
        Assert.assertEquals("Restored", axis.getLabel());
        Assert.assertEquals(3, listener.changeCount);
    }

    @Test
    public void testSetLabelFont() {
        Font font = new Font("Dialog", Font.BOLD, 14);
        axis.setLabelFont(font);
        Assert.assertEquals(font, axis.getLabelFont());
        Assert.assertEquals(1, listener.changeCount);

        axis.setLabelFont(font);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelFontNull() {
        axis.setLabelFont(null);
    }

    @Test
    public void testSetLabelPaint() {
        axis.setLabelPaint(Color.RED);
        Assert.assertEquals(Color.RED, axis.getLabelPaint());
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaintNull() {
        axis.setLabelPaint(null);
    }

    @Test
    public void testSetLabelInsets() {
        RectangleInsets insets = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
        axis.setLabelInsets(insets);
        Assert.assertEquals(insets, axis.getLabelInsets());
        Assert.assertEquals(1, listener.changeCount);

        axis.setLabelInsets(insets);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelInsetsNull() {
        axis.setLabelInsets(null);
    }

    @Test
    public void testSetLabelAngle() {
        axis.setLabelAngle(Math.PI / 4.0);
        Assert.assertEquals(Math.PI / 4.0, axis.getLabelAngle(), 1e-9);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test
    public void testSetLabelToolTip() {
        axis.setLabelToolTip("Tooltip");
        Assert.assertEquals("Tooltip", axis.getLabelToolTip());
        Assert.assertEquals(1, listener.changeCount);

        axis.setLabelToolTip(null);
        Assert.assertNull(axis.getLabelToolTip());
        Assert.assertEquals(2, listener.changeCount);
    }

    @Test
    public void testSetLabelURL() {
        axis.setLabelURL("http://example.com");
        Assert.assertEquals("http://example.com", axis.getLabelURL());
        Assert.assertEquals(1, listener.changeCount);

        axis.setLabelURL(null);
        Assert.assertNull(axis.getLabelURL());
        Assert.assertEquals(2, listener.changeCount);
    }

    @Test
    public void testSetAxisLineVisible() {
        axis.setAxisLineVisible(false);
        Assert.assertFalse(axis.isAxisLineVisible());
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test
    public void testSetAxisLinePaint() {
        axis.setAxisLinePaint(Color.BLUE);
        Assert.assertEquals(Color.BLUE, axis.getAxisLinePaint());
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisLinePaintNull() {
        axis.setAxisLinePaint(null);
    }

    @Test
    public void testSetAxisLineStroke() {
        Stroke stroke = new BasicStroke(2.0f);
        axis.setAxisLineStroke(stroke);
        Assert.assertEquals(stroke, axis.getAxisLineStroke());
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisLineStrokeNull() {
        axis.setAxisLineStroke(null);
    }

    @Test
    public void testSetTickLabelsVisible() {
        axis.setTickLabelsVisible(false);
        Assert.assertFalse(axis.isTickLabelsVisible());
        Assert.assertEquals(1, listener.changeCount);

        axis.setTickLabelsVisible(false);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test
    public void testSetTickLabelFont() {
        Font font = new Font("Courier", Font.ITALIC, 8);
        axis.setTickLabelFont(font);
        Assert.assertEquals(font, axis.getTickLabelFont());
        Assert.assertEquals(1, listener.changeCount);

        axis.setTickLabelFont(font);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickLabelFontNull() {
        axis.setTickLabelFont(null);
    }

    @Test
    public void testSetTickLabelPaint() {
        axis.setTickLabelPaint(Color.GREEN);
        Assert.assertEquals(Color.GREEN, axis.getTickLabelPaint());
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickLabelPaintNull() {
        axis.setTickLabelPaint(null);
    }

    @Test
    public void testSetTickLabelInsets() {
        RectangleInsets insets = new RectangleInsets(1.0, 1.0, 1.0, 1.0);
        axis.setTickLabelInsets(insets);
        Assert.assertEquals(insets, axis.getTickLabelInsets());
        Assert.assertEquals(1, listener.changeCount);

        axis.setTickLabelInsets(insets);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickLabelInsetsNull() {
        axis.setTickLabelInsets(null);
    }

    @Test
    public void testSetTickMarksVisible() {
        axis.setTickMarksVisible(false);
        Assert.assertFalse(axis.isTickMarksVisible());
        Assert.assertEquals(1, listener.changeCount);

        axis.setTickMarksVisible(false);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test
    public void testSetTickMarkInsideLength() {
        axis.setTickMarkInsideLength(3.5f);
        Assert.assertEquals(3.5f, axis.getTickMarkInsideLength(), 1e-9f);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test
    public void testSetTickMarkOutsideLength() {
        axis.setTickMarkOutsideLength(4.5f);
        Assert.assertEquals(4.5f, axis.getTickMarkOutsideLength(), 1e-9f);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test
    public void testSetTickMarkStroke() {
        Stroke stroke = new BasicStroke(3.0f);
        axis.setTickMarkStroke(stroke);
        Assert.assertEquals(stroke, axis.getTickMarkStroke());
        Assert.assertEquals(1, listener.changeCount);

        axis.setTickMarkStroke(stroke);
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickMarkStrokeNull() {
        axis.setTickMarkStroke(null);
    }

    @Test
    public void testSetTickMarkPaint() {
        axis.setTickMarkPaint(Color.MAGENTA);
        Assert.assertEquals(Color.MAGENTA, axis.getTickMarkPaint());
        Assert.assertEquals(1, listener.changeCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTickMarkPaintNull() {
        axis.setTickMarkPaint(null);
    }

    @Test
    public void testSetPlotAndFixedDimension() {
        Assert.assertFalse(axis.configureCalled);
        axis.setPlot(null);
        Assert.assertTrue(axis.configureCalled);
        Assert.assertNull(axis.getPlot());

        axis.setFixedDimension(100.0);
        Assert.assertEquals(100.0, axis.getFixedDimension(), 1e-9);
    }

    @Test
    public void testListeners() {
        Assert.assertTrue(axis.hasListener(listener));
        RecordingListener otherListener = new RecordingListener();
        Assert.assertFalse(axis.hasListener(otherListener));

        axis.removeChangeListener(listener);
        Assert.assertFalse(axis.hasListener(listener));

        axis.setVisible(false);
        Assert.assertEquals(0, listener.changeCount);
    }

    @Test
    public void testGetLabelEnclosure() {
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        Assert.assertTrue(enclosure.getWidth() > 0);
        Assert.assertTrue(enclosure.getHeight() > 0);

        Rectangle2D enclosureLeft = axis.getLabelEnclosure(g2, RectangleEdge.LEFT);
        Assert.assertTrue(enclosureLeft.getWidth() > 0);
        Assert.assertTrue(enclosureLeft.getHeight() > 0);

        axis.setLabel(null);
        Rectangle2D enclosureNull = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        Assert.assertEquals(0.0, enclosureNull.getWidth(), 1e-9);
        Assert.assertEquals(0.0, enclosureNull.getHeight(), 1e-9);

        axis.setLabel("");
        Rectangle2D enclosureEmpty = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        Assert.assertEquals(0.0, enclosureEmpty.getWidth(), 1e-9);
        Assert.assertEquals(0.0, enclosureEmpty.getHeight(), 1e-9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDrawLabelNullState() {
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        axis.drawLabel("Test", g2, area, area, RectangleEdge.TOP, null, null);
    }

    @Test
    public void testDrawLabelWithNullAndEmptyLabel() {
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        AxisState state = new AxisState(50.0);
        AxisState result = axis.drawLabel(null, g2, area, area, RectangleEdge.TOP, state, null);
        Assert.assertSame(state, result);
        Assert.assertEquals(50.0, result.getCursor(), 1e-9);

        result = axis.drawLabel("", g2, area, area, RectangleEdge.TOP, state, null);
        Assert.assertSame(state, result);
        Assert.assertEquals(50.0, result.getCursor(), 1e-9);
    }

    @Test
    public void testDrawLabelEdgesAndEntities() {
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 200, 200);
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 160, 160);

        ChartRenderingInfo chartInfo = new ChartRenderingInfo();
        PlotRenderingInfo plotState = new PlotRenderingInfo(chartInfo);
        axis.setLabelToolTip("Tip");
        axis.setLabelURL("http://url");

        // TOP
        AxisState stateTop = new AxisState(20.0);
        axis.drawLabel("Label Top", g2, plotArea, dataArea, RectangleEdge.TOP, stateTop, plotState);
        Assert.assertTrue(stateTop.getCursor() < 20.0);

        // BOTTOM
        AxisState stateBottom = new AxisState(180.0);
        axis.drawLabel("Label Bottom", g2, plotArea, dataArea, RectangleEdge.BOTTOM, stateBottom, plotState);
        Assert.assertTrue(stateBottom.getCursor() > 180.0);

        // LEFT
        AxisState stateLeft = new AxisState(20.0);
        axis.drawLabel("Label Left", g2, plotArea, dataArea, RectangleEdge.LEFT, stateLeft, plotState);
        Assert.assertTrue(stateLeft.getCursor() < 20.0);

        // RIGHT
        AxisState stateRight = new AxisState(180.0);
        axis.drawLabel("Label Right", g2, plotArea, dataArea, RectangleEdge.RIGHT, stateRight, plotState);
        Assert.assertTrue(stateRight.getCursor() > 180.0);

        EntityCollection entities = chartInfo.getEntityCollection();
        Assert.assertNotNull(entities);
        Assert.assertEquals(4, entities.getEntityCount());

        // Cover null entity collection inside plotState
        ChartRenderingInfo infoNoEntities = new ChartRenderingInfo(null);
        PlotRenderingInfo plotStateNoEntities = new PlotRenderingInfo(infoNoEntities);
        AxisState stateTest = new AxisState(20.0);
        axis.drawLabel("Label Test", g2, plotArea, dataArea, RectangleEdge.TOP, stateTest, plotStateNoEntities);
    }

    @Test
    public void testDrawAxisLine() {
        Rectangle2D dataArea = new Rectangle2D.Double(10, 20, 100, 200);
        axis.drawAxisLine(g2, 20, dataArea, RectangleEdge.TOP);
        axis.drawAxisLine(g2, 220, dataArea, RectangleEdge.BOTTOM);
        axis.drawAxisLine(g2, 10, dataArea, RectangleEdge.LEFT);
        axis.drawAxisLine(g2, 110, dataArea, RectangleEdge.RIGHT);
    }

    @Test
    public void testEqualsAndClone() throws CloneNotSupportedException {
        TestAxis a1 = new TestAxis("Label");
        TestAxis a2 = new TestAxis("Label");

        Assert.assertTrue(a1.equals(a1));
        Assert.assertFalse(a1.equals(null));
        Assert.assertFalse(a1.equals("Not an Axis"));
        Assert.assertTrue(a1.equals(a2));

        a2.setVisible(false);
        Assert.assertFalse(a1.equals(a2));
        a2.setVisible(true);

        a2.setLabel("Other");
        Assert.assertFalse(a1.equals(a2));
        a2.setLabel("Label");

        a2.setLabelFont(new Font("Dialog", Font.BOLD, 20));
        Assert.assertFalse(a1.equals(a2));
        a2.setLabelFont(a1.getLabelFont());

        a2.setLabelPaint(Color.PINK);
        Assert.assertFalse(a1.equals(a2));
        a2.setLabelPaint(a1.getLabelPaint());

        a2.setLabelInsets(new RectangleInsets(10, 10, 10, 10));
        Assert.assertFalse(a1.equals(a2));
        a2.setLabelInsets(a1.getLabelInsets());

        a2.setLabelAngle(1.23);
        Assert.assertFalse(a1.equals(a2));
        a2.setLabelAngle(a1.getLabelAngle());

        a2.setLabelToolTip("TT");
        Assert.assertFalse(a1.equals(a2));
        a2.setLabelToolTip(null);

        a2.setLabelURL("URL");
        Assert.assertFalse(a1.equals(a2));
        a2.setLabelURL(null);

        a2.setAxisLineVisible(false);
        Assert.assertFalse(a1.equals(a2));
        a2.setAxisLineVisible(true);

        a2.setAxisLineStroke(new BasicStroke(5.0f));
        Assert.assertFalse(a1.equals(a2));
        a2.setAxisLineStroke(a1.getAxisLineStroke());

        a2.setAxisLinePaint(Color.CYAN);
        Assert.assertFalse(a1.equals(a2));
        a2.setAxisLinePaint(a1.getAxisLinePaint());

        a2.setTickLabelsVisible(false);
        Assert.assertFalse(a1.equals(a2));
        a2.setTickLabelsVisible(true);

        a2.setTickLabelFont(new Font("Serif", Font.PLAIN, 15));
        Assert.assertFalse(a1.equals(a2));
        a2.setTickLabelFont(a1.getTickLabelFont());

        a2.setTickLabelPaint(Color.YELLOW);
        Assert.assertFalse(a1.equals(a2));
        a2.setTickLabelPaint(a1.getTickLabelPaint());

        a2.setTickLabelInsets(new RectangleInsets(2, 2, 2, 2));
        Assert.assertFalse(a1.equals(a2));
        a2.setTickLabelInsets(a1.getTickLabelInsets());

        a2.setTickMarksVisible(false);
        Assert.assertFalse(a1.equals(a2));
        a2.setTickMarksVisible(true);

        a2.setTickMarkInsideLength(9.0f);
        Assert.assertFalse(a1.equals(a2));
        a2.setTickMarkInsideLength(a1.getTickMarkInsideLength());

        a2.setTickMarkOutsideLength(9.0f);
        Assert.assertFalse(a1.equals(a2));
        a2.setTickMarkOutsideLength(a1.getTickMarkOutsideLength());

        a2.setTickMarkPaint(Color.ORANGE);
        Assert.assertFalse(a1.equals(a2));
        a2.setTickMarkPaint(a1.getTickMarkPaint());

        a2.setTickMarkStroke(new BasicStroke(4.0f));
        Assert.assertFalse(a1.equals(a2));
        a2.setTickMarkStroke(a1.getTickMarkStroke());

        a2.setFixedDimension(55.0);
        Assert.assertFalse(a1.equals(a2));
        a2.setFixedDimension(a1.getFixedDimension());

        Assert.assertTrue(a1.equals(a2));

        TestAxis clone = (TestAxis) a1.clone();
        Assert.assertEquals(a1, clone);
        Assert.assertNull(clone.getPlot());
        Assert.assertFalse(clone.hasListener(listener));
    }

    @Test
    public void testSerialization() throws Exception {
        TestAxis a1 = new TestAxis("Serialize Me");
        a1.setLabelToolTip("ToolTip");
        a1.setLabelURL("URL");
        a1.setFixedDimension(42.0);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(a1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        TestAxis a2 = (TestAxis) in.readObject();
        in.close();

        Assert.assertEquals(a1, a2);
        Assert.assertFalse(a2.hasListener(listener));
    }
}