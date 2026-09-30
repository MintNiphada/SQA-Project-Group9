package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryLineAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CategoryPlotTest implements PlotChangeListener {

    private boolean plotChanged;

    public void plotChanged(PlotChangeEvent event) {
        this.plotChanged = true;
    }

    @Before
    public void setUp() {
        this.plotChanged = false;
    }

    private DefaultCategoryDataset createSampleDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(3.0, "R2", "C1");
        dataset.addValue(4.0, "R2", "C2");
        return dataset;
    }

    @Test
    public void testConstructorAndDefaults() {
        CategoryPlot plot = new CategoryPlot();
        Assert.assertNotNull(plot.getPlotType());
        Assert.assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        Assert.assertEquals(new RectangleInsets(4.0, 4.0, 4.0, 4.0), plot.getAxisOffset());
        Assert.assertNull(plot.getDomainAxis());
        Assert.assertNull(plot.getRangeAxis());
        Assert.assertNull(plot.getRenderer());
        Assert.assertNull(plot.getDataset());
        Assert.assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation());
        Assert.assertEquals(AxisLocation.TOP_OR_LEFT, plot.getRangeAxisLocation());
        Assert.assertFalse(plot.getDrawSharedDomainAxis());
        Assert.assertFalse(plot.isDomainGridlinesVisible());
        Assert.assertTrue(plot.isRangeGridlinesVisible());
        Assert.assertEquals(CategoryAnchor.MIDDLE, plot.getDomainGridlinePosition());
        Assert.assertEquals(CategoryPlot.DEFAULT_GRIDLINE_STROKE, plot.getDomainGridlineStroke());
        Assert.assertEquals(CategoryPlot.DEFAULT_GRIDLINE_PAINT, plot.getDomainGridlinePaint());
        Assert.assertEquals(CategoryPlot.DEFAULT_GRIDLINE_STROKE, plot.getRangeGridlineStroke());
        Assert.assertEquals(CategoryPlot.DEFAULT_GRIDLINE_PAINT, plot.getRangeGridlinePaint());
        Assert.assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());
        Assert.assertEquals(SortOrder.ASCENDING, plot.getColumnRenderingOrder());
        Assert.assertEquals(SortOrder.ASCENDING, plot.getRowRenderingOrder());
        Assert.assertEquals(0.0, plot.getAnchorValue(), 0.0001);
        Assert.assertEquals(CategoryPlot.DEFAULT_CROSSHAIR_VISIBLE, plot.isRangeCrosshairVisible());
        Assert.assertEquals(0.0, plot.getRangeCrosshairValue(), 0.0001);
        Assert.assertEquals(CategoryPlot.DEFAULT_CROSSHAIR_STROKE, plot.getRangeCrosshairStroke());
        Assert.assertEquals(CategoryPlot.DEFAULT_CROSSHAIR_PAINT, plot.getRangeCrosshairPaint());
        Assert.assertTrue(plot.isRangeCrosshairLockedOnData());
        Assert.assertNotNull(plot.getAnnotations());
        Assert.assertEquals(0, plot.getAnnotations().size());
        Assert.assertFalse(plot.isDomainZoomable());
        Assert.assertTrue(plot.isRangeZoomable());
    }

    @Test
    public void testParameterizedConstructor() {
        DefaultCategoryDataset dataset = createSampleDataset();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        ValueAxis rangeAxis = new NumberAxis("Value");
        CategoryItemRenderer renderer = new BarRenderer();

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);

        Assert.assertSame(dataset, plot.getDataset());
        Assert.assertSame(domainAxis, plot.getDomainAxis());
        Assert.assertSame(rangeAxis, plot.getRangeAxis());
        Assert.assertSame(renderer, plot.getRenderer());
        Assert.assertSame(plot, domainAxis.getPlot());
        Assert.assertSame(plot, rangeAxis.getPlot());
        Assert.assertSame(plot, renderer.getPlot());
    }

    @Test
    public void testOrientation() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setOrientation(null);
            Assert.fail("Null orientation should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAxisOffset() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);
        RectangleInsets insets = new RectangleInsets(10.0, 10.0, 10.0, 10.0);
        plot.setAxisOffset(insets);
        Assert.assertEquals(insets, plot.getAxisOffset());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setAxisOffset(null);
            Assert.fail("Null offset should throw IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testDomainAxesOperations() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);
        CategoryAxis axis1 = new CategoryAxis("Axis1");
        CategoryAxis axis2 = new CategoryAxis("Axis2");

        plot.setDomainAxis(0, axis1);
        Assert.assertSame(axis1, plot.getDomainAxis(0));
        Assert.assertSame(axis1, plot.getDomainAxis());
        Assert.assertEquals(0, plot.getDomainAxisIndex(axis1));
        Assert.assertEquals(1, plot.getDomainAxisCount());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setDomainAxis(1, axis2, false);
        Assert.assertSame(axis2, plot.getDomainAxis(1));
        Assert.assertEquals(1, plot.getDomainAxisIndex(axis2));
        Assert.assertEquals(2, plot.getDomainAxisCount());
        Assert.assertFalse(this.plotChanged);

        plot.setDomainAxis(axis2);
        Assert.assertSame(axis2, plot.getDomainAxis(0));

        CategoryAxis axis3 = new CategoryAxis("Axis3");
        Assert.assertEquals(-1, plot.getDomainAxisIndex(axis3));
        try {
            plot.getDomainAxisIndex(null);
            Assert.fail("Expected IllegalArgumentException on null axis.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        CategoryAxis[] axes = new CategoryAxis[] {new CategoryAxis("A"), new CategoryAxis("B")};
        plot.setDomainAxes(axes);
        Assert.assertEquals(2, plot.getDomainAxisCount());
        Assert.assertEquals("A", plot.getDomainAxis(0).getLabel());
        Assert.assertEquals("B", plot.getDomainAxis(1).getLabel());

        plot.clearDomainAxes();
        Assert.assertEquals(0, plot.getDomainAxisCount());
        Assert.assertNull(plot.getDomainAxis(0));
    }

    @Test
    public void testRangeAxesOperations() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);
        NumberAxis axis1 = new NumberAxis("Range1");
        NumberAxis axis2 = new NumberAxis("Range2");

        plot.setRangeAxis(0, axis1);
        Assert.assertSame(axis1, plot.getRangeAxis(0));
        Assert.assertSame(axis1, plot.getRangeAxis());
        Assert.assertEquals(0, plot.getRangeAxisIndex(axis1));
        Assert.assertEquals(1, plot.getRangeAxisCount());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setRangeAxis(1, axis2, false);
        Assert.assertSame(axis2, plot.getRangeAxis(1));
        Assert.assertEquals(1, plot.getRangeAxisIndex(axis2));
        Assert.assertEquals(2, plot.getRangeAxisCount());
        Assert.assertFalse(this.plotChanged);

        plot.setRangeAxis(axis2);
        Assert.assertSame(axis2, plot.getRangeAxis(0));

        NumberAxis axis3 = new NumberAxis("Range3");
        Assert.assertEquals(-1, plot.getRangeAxisIndex(axis3));
        try {
            plot.getRangeAxisIndex(null);
            Assert.fail("Expected IllegalArgumentException on null axis.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        ValueAxis[] axes = new ValueAxis[] {new NumberAxis("A"), new NumberAxis("B")};
        plot.setRangeAxes(axes);
        Assert.assertEquals(2, plot.getRangeAxisCount());
        Assert.assertEquals("A", plot.getRangeAxis(0).getLabel());
        Assert.assertEquals("B", plot.getRangeAxis(1).getLabel());

        plot.clearRangeAxes();
        Assert.assertEquals(0, plot.getRangeAxisCount());
        Assert.assertNull(plot.getRangeAxis(0));
    }

    @Test
    public void testDomainAxisLocations() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);

        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        Assert.assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation());
        Assert.assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation(0));
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT, false);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation(1));
        Assert.assertFalse(this.plotChanged);

        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT, true);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getDomainAxisLocation(0));

        try {
            plot.setDomainAxisLocation(0, null);
            Assert.fail("Null location for index 0 should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        Assert.assertEquals(RectangleEdge.TOP, plot.getDomainAxisEdge(0));
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(RectangleEdge.BOTTOM, plot.getDomainAxisEdge(0));

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        Assert.assertEquals(RectangleEdge.LEFT, plot.getDomainAxisEdge(0));
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(RectangleEdge.RIGHT, plot.getDomainAxisEdge(0));
    }

    @Test
    public void testRangeAxisLocations() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);

        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation());
        Assert.assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation(0));
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT, false);
        Assert.assertEquals(AxisLocation.TOP_OR_LEFT, plot.getRangeAxisLocation(1));
        Assert.assertFalse(this.plotChanged);

        try {
            plot.setRangeAxisLocation(0, null);
            Assert.fail("Null location for index 0 should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
        Assert.assertEquals(RectangleEdge.LEFT, plot.getRangeAxisEdge(0));
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(RectangleEdge.RIGHT, plot.getRangeAxisEdge(0));

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
        Assert.assertEquals(RectangleEdge.TOP, plot.getRangeAxisEdge(0));
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(RectangleEdge.BOTTOM, plot.getRangeAxisEdge(0));
    }

    @Test
    public void testDatasetsAndRenderers() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset ds1 = createSampleDataset();
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        ds2.addValue(10.0, "R3", "C3");

        CategoryAxis domainAxis1 = new CategoryAxis("Domain1");
        CategoryAxis domainAxis2 = new CategoryAxis("Domain2");
        ValueAxis rangeAxis1 = new NumberAxis("Range1");
        ValueAxis rangeAxis2 = new NumberAxis("Range2");

        CategoryItemRenderer r1 = new BarRenderer();
        CategoryItemRenderer r2 = new LineAndShapeRenderer();

        plot.setDomainAxis(0, domainAxis1);
        plot.setDomainAxis(1, domainAxis2);
        plot.setRangeAxis(0, rangeAxis1);
        plot.setRangeAxis(1, rangeAxis2);

        plot.setDataset(0, ds1);
        plot.setDataset(1, ds2);
        plot.setRenderer(0, r1);
        plot.setRenderer(1, r2);

        Assert.assertSame(ds1, plot.getDataset());
        Assert.assertSame(ds1, plot.getDataset(0));
        Assert.assertSame(ds2, plot.getDataset(1));
        Assert.assertNull(plot.getDataset(2));
        Assert.assertEquals(2, plot.getDatasetCount());

        Assert.assertSame(r1, plot.getRenderer());
        Assert.assertSame(r1, plot.getRenderer(0));
        Assert.assertSame(r2, plot.getRenderer(1));
        Assert.assertNull(plot.getRenderer(2));
        Assert.assertEquals(0, plot.getIndexOf(r1));
        Assert.assertEquals(1, plot.getIndexOf(r2));
        Assert.assertSame(r1, plot.getRendererForDataset(ds1));
        Assert.assertSame(r2, plot.getRendererForDataset(ds2));
        Assert.assertNull(plot.getRendererForDataset(new DefaultCategoryDataset()));

        plot.mapDatasetToDomainAxis(0, 1);
        Assert.assertSame(domainAxis2, plot.getDomainAxisForDataset(0));
        plot.mapDatasetToRangeAxis(0, 1);
        Assert.assertSame(rangeAxis2, plot.getRangeAxisForDataset(0));

        CategoryItemRenderer[] renderers = new CategoryItemRenderer[] {new BarRenderer(), new LineAndShapeRenderer()};
        plot.setRenderers(renderers);
        Assert.assertSame(renderers[0], plot.getRenderer(0));
        Assert.assertSame(renderers[1], plot.getRenderer(1));
    }

    @Test
    public void testRenderingOrders() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        Assert.assertEquals(DatasetRenderingOrder.FORWARD, plot.getDatasetRenderingOrder());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setDatasetRenderingOrder(null);
            Assert.fail("Null order should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        this.plotChanged = false;
        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        Assert.assertEquals(SortOrder.DESCENDING, plot.getColumnRenderingOrder());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setColumnRenderingOrder(null);
            Assert.fail("Null column order should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        this.plotChanged = false;
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        Assert.assertEquals(SortOrder.DESCENDING, plot.getRowRenderingOrder());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setRowRenderingOrder(null);
            Assert.fail("Null row order should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGridlineProperties() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);

        plot.setDomainGridlinesVisible(true);
        Assert.assertTrue(plot.isDomainGridlinesVisible());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setDomainGridlinePosition(CategoryAnchor.START);
        Assert.assertEquals(CategoryAnchor.START, plot.getDomainGridlinePosition());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setDomainGridlinePosition(null);
            Assert.fail("Null position should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        this.plotChanged = false;
        Stroke stroke = new BasicStroke(2.0f);
        plot.setDomainGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getDomainGridlineStroke());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setDomainGridlineStroke(null);
            Assert.fail("Null stroke should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        this.plotChanged = false;
        Paint paint = Color.RED;
        plot.setDomainGridlinePaint(paint);
        Assert.assertEquals(paint, plot.getDomainGridlinePaint());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setDomainGridlinePaint(null);
            Assert.fail("Null paint should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        this.plotChanged = false;
        plot.setRangeGridlinesVisible(false);
        Assert.assertFalse(plot.isRangeGridlinesVisible());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setRangeGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeGridlineStroke());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setRangeGridlineStroke(null);
            Assert.fail("Null stroke should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        this.plotChanged = false;
        plot.setRangeGridlinePaint(paint);
        Assert.assertEquals(paint, plot.getRangeGridlinePaint());
        Assert.assertTrue(this.plotChanged);

        try {
            plot.setRangeGridlinePaint(null);
            Assert.fail("Null paint should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testCrosshairProperties() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);

        plot.setRangeCrosshairVisible(true);
        Assert.assertTrue(plot.isRangeCrosshairVisible());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setRangeCrosshairLockedOnData(false);
        Assert.assertFalse(plot.isRangeCrosshairLockedOnData());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setRangeCrosshairValue(12.5);
        Assert.assertEquals(12.5, plot.getRangeCrosshairValue(), 0.001);
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.setRangeCrosshairValue(15.0, false);
        Assert.assertEquals(15.0, plot.getRangeCrosshairValue(), 0.001);
        Assert.assertFalse(this.plotChanged);

        Stroke stroke = new BasicStroke(3.0f);
        plot.setRangeCrosshairStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeCrosshairStroke());

        try {
            plot.setRangeCrosshairStroke(null);
            Assert.fail("Null crosshair stroke should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        plot.setRangeCrosshairPaint(Color.GREEN);
        Assert.assertEquals(Color.GREEN, plot.getRangeCrosshairPaint());

        try {
            plot.setRangeCrosshairPaint(null);
            Assert.fail("Null crosshair paint should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testMarkers() {
        CategoryPlot plot = new CategoryPlot();
        CategoryMarker cm1 = new CategoryMarker("C1");
        CategoryMarker cm2 = new CategoryMarker("C2");

        plot.addDomainMarker(cm1);
        plot.addDomainMarker(cm2, Layer.BACKGROUND);
        plot.addDomainMarker(1, cm1, Layer.FOREGROUND);

        Collection fgMarkers = plot.getDomainMarkers(Layer.FOREGROUND);
        Assert.assertNotNull(fgMarkers);
        Assert.assertTrue(fgMarkers.contains(cm1));

        Collection bgMarkers = plot.getDomainMarkers(Layer.BACKGROUND);
        Assert.assertNotNull(bgMarkers);
        Assert.assertTrue(bgMarkers.contains(cm2));

        Assert.assertTrue(plot.removeDomainMarker(cm1));
        Assert.assertTrue(plot.removeDomainMarker(cm2, Layer.BACKGROUND));
        Assert.assertFalse(plot.removeDomainMarker(cm2, Layer.BACKGROUND));

        plot.clearDomainMarkers(1);
        plot.clearDomainMarkers();

        ValueMarker vm1 = new ValueMarker(1.5);
        ValueMarker vm2 = new ValueMarker(2.5);

        plot.addRangeMarker(vm1);
        plot.addRangeMarker(vm2, Layer.BACKGROUND);
        plot.addRangeMarker(1, vm1, Layer.FOREGROUND);

        Collection fgRMarkers = plot.getRangeMarkers(Layer.FOREGROUND);
        Assert.assertNotNull(fgRMarkers);
        Assert.assertTrue(fgRMarkers.contains(vm1));

        Collection bgRMarkers = plot.getRangeMarkers(Layer.BACKGROUND);
        Assert.assertNotNull(bgRMarkers);
        Assert.assertTrue(bgRMarkers.contains(vm2));

        Assert.assertTrue(plot.removeRangeMarker(vm1));
        Assert.assertTrue(plot.removeRangeMarker(vm2, Layer.BACKGROUND));
        Assert.assertFalse(plot.removeRangeMarker(vm2, Layer.BACKGROUND));

        plot.clearRangeMarkers(1);
        plot.clearRangeMarkers();
    }

    @Test
    public void testAnnotations() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);

        CategoryAnnotation a1 = new CategoryTextAnnotation("A1", "C1", 2.0);
        CategoryAnnotation a2 = new CategoryLineAnnotation("C1", 1.0, "C2", 2.0, Color.BLACK, new BasicStroke(1.0f));

        plot.addAnnotation(a1);
        Assert.assertEquals(1, plot.getAnnotations().size());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.addAnnotation(a2, false);
        Assert.assertEquals(2, plot.getAnnotations().size());
        Assert.assertFalse(this.plotChanged);

        this.plotChanged = false;
        boolean removed = plot.removeAnnotation(a1);
        Assert.assertTrue(removed);
        Assert.assertEquals(1, plot.getAnnotations().size());
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        removed = plot.removeAnnotation(a2, false);
        Assert.assertTrue(removed);
        Assert.assertEquals(0, plot.getAnnotations().size());
        Assert.assertFalse(this.plotChanged);

        plot.addAnnotation(a1);
        plot.clearAnnotations();
        Assert.assertEquals(0, plot.getAnnotations().size());

        try {
            plot.addAnnotation(null);
            Assert.fail("Null annotation should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            plot.removeAnnotation(null);
            Assert.fail("Null annotation removal should fail.");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testLegendItems() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset dataset = createSampleDataset();
        BarRenderer renderer = new BarRenderer();
        plot.setDataset(dataset);
        plot.setRenderer(renderer);

        LegendItemCollection lic = plot.getLegendItems();
        Assert.assertNotNull(lic);
        Assert.assertEquals(2, lic.getItemCount());

        LegendItemCollection fixed = new LegendItemCollection();
        fixed.add(new LegendItem("Fixed", "FixedDesc", "tip", "url", new Rectangle2D.Double(0, 0, 1, 1), Color.RED));
        plot.setFixedLegendItems(fixed);
        Assert.assertSame(fixed, plot.getFixedLegendItems());
        Assert.assertSame(fixed, plot.getLegendItems());

        plot.setFixedLegendItems(null);
        Assert.assertNull(plot.getFixedLegendItems());
    }

    @Test
    public void testZoomAndAnchor() {
        DefaultCategoryDataset dataset = createSampleDataset();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        rangeAxis.setRange(0.0, 10.0);
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, new BarRenderer());

        plot.setAnchorValue(5.0);
        Assert.assertEquals(5.0, plot.getAnchorValue(), 0.001);

        plot.zoom(0.5);
        Range r = rangeAxis.getRange();
        Assert.assertEquals(2.5, r.getLowerBound(), 0.01);
        Assert.assertEquals(7.5, r.getUpperBound(), 0.01);

        plot.zoom(0.0);
        Assert.assertTrue(rangeAxis.isAutoRange());

        plot.zoomRangeAxes(2.0, new PlotRenderingInfo(null), new Point2D.Double(10, 20), false);
        plot.zoomRangeAxes(0.1, 0.9, new PlotRenderingInfo(null), new Point2D.Double(10, 20));

        // Domain zooming does nothing
        plot.zoomDomainAxes(0.5, new PlotRenderingInfo(null), new Point2D.Double(0, 0));
        plot.zoomDomainAxes(0.1, 0.9, new PlotRenderingInfo(null), new Point2D.Double(0, 0));
        plot.zoomDomainAxes(0.5, new PlotRenderingInfo(null), new Point2D.Double(0, 0), true);
    }

    @Test
    public void testDataRangeAndCategories() {
        DefaultCategoryDataset dataset = createSampleDataset();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        BarRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);

        Range range = plot.getDataRange(rangeAxis);
        Assert.assertNotNull(range);
        Assert.assertEquals(1.0, range.getLowerBound(), 0.001);
        Assert.assertEquals(4.0, range.getUpperBound(), 0.001);

        List categories = plot.getCategories();
        Assert.assertNotNull(categories);
        Assert.assertEquals(2, categories.size());
        Assert.assertEquals("C1", categories.get(0));
        Assert.assertEquals("C2", categories.get(1));

        List axisCats = plot.getCategoriesForAxis(domainAxis);
        Assert.assertEquals(2, axisCats.size());
    }

    @Test
    public void testWeightAndFixedSpace() {
        CategoryPlot plot = new CategoryPlot();
        plot.setWeight(3);
        Assert.assertEquals(3, plot.getWeight());

        AxisSpace space = new AxisSpace();
        space.setTop(15.0);
        plot.setFixedDomainAxisSpace(space);
        Assert.assertSame(space, plot.getFixedDomainAxisSpace());

        plot.setFixedRangeAxisSpace(space);
        Assert.assertSame(space, plot.getFixedRangeAxisSpace());

        plot.setDrawSharedDomainAxis(true);
        Assert.assertTrue(plot.getDrawSharedDomainAxis());
    }

    @Test
    public void testDrawPlotVerticalAndHorizontal() {
        DefaultCategoryDataset dataset = createSampleDataset();
        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        BarRenderer renderer = new BarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);

        plot.addDomainMarker(new CategoryMarker("C1"));
        plot.addRangeMarker(new ValueMarker(2.5));
        plot.addAnnotation(new CategoryTextAnnotation("Note", "C1", 3.0));
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairValue(2.0);

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();

        ChartRenderingInfo info = new ChartRenderingInfo();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 400, 300), info);

        // Test Horizontal orientation drawing
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        chart.draw(g2, new Rectangle2D.Double(0, 0, 400, 300), info);

        // Test click handling
        plot.handleClick(100, 100, info.getPlotInfo());

        // Test draw with zero size area
        plot.draw(g2, new Rectangle2D.Double(0, 0, 5, 5), new Point2D.Double(0, 0), null, null);

        g2.dispose();
    }

    @Test
    public void testEqualsAndClone() throws Exception {
        CategoryPlot p1 = new CategoryPlot(createSampleDataset(), new CategoryAxis("D"), new NumberAxis("R"), new BarRenderer());
        CategoryPlot p2 = new CategoryPlot(createSampleDataset(), new CategoryAxis("D"), new NumberAxis("R"), new BarRenderer());

        Assert.assertTrue(p1.equals(p1));
        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("Other"));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertTrue(p2.equals(p1));

        p2.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertFalse(p1.equals(p2));
        p2.setOrientation(PlotOrientation.VERTICAL);
        Assert.assertTrue(p1.equals(p2));

        CategoryPlot cloned = (CategoryPlot) p1.clone();
        Assert.assertNotSame(p1, cloned);
        Assert.assertEquals(p1.getClass(), cloned.getClass());
        Assert.assertTrue(p1.equals(cloned));
    }

    @Test
    public void testSerialization() throws Exception {
        CategoryPlot p1 = new CategoryPlot(createSampleDataset(), new CategoryAxis("D"), new NumberAxis("R"), new BarRenderer());
        p1.setDomainGridlinesVisible(true);
        p1.setRangeGridlinesVisible(true);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(p1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        CategoryPlot p2 = (CategoryPlot) in.readObject();
        in.close();

        Assert.assertTrue(p1.equals(p2));
    }

    @Test
    public void testEvents() {
        CategoryPlot plot = new CategoryPlot();
        plot.addChangeListener(this);

        this.plotChanged = false;
        plot.datasetChanged(new DatasetChangeEvent(this, null));
        Assert.assertTrue(this.plotChanged);

        this.plotChanged = false;
        plot.rendererChanged(new RendererChangeEvent(new BarRenderer()));
        Assert.assertTrue(this.plotChanged);
    }
}