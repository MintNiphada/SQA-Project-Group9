package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Assert;
import org.junit.Test;

public class XYPlotTest {

    private static class TestPlotListener implements PlotChangeListener {
        int eventCount = 0;
        PlotChangeEvent lastEvent;

        public void plotChanged(PlotChangeEvent event) {
            this.eventCount++;
            this.lastEvent = event;
        }
    }

    private XYDataset createSampleDataset() {
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(1.0, 1.0);
        series1.add(2.0, 2.0);
        series1.add(3.0, 3.0);
        XYSeries series2 = new XYSeries("Series 2");
        series2.add(1.0, 2.0);
        series2.add(2.0, 3.0);
        series2.add(3.0, 4.0);
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series1);
        collection.addSeries(series2);
        return collection;
    }

    @Test
    public void testDefaultConstructor() {
        XYPlot plot = new XYPlot();
        Assert.assertNull(plot.getDataset());
        Assert.assertNull(plot.getDomainAxis());
        Assert.assertNull(plot.getRangeAxis());
        Assert.assertNull(plot.getRenderer());
        Assert.assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        Assert.assertEquals(1, plot.getWeight());
        Assert.assertEquals("XY Plot", plot.getPlotType());
        Assert.assertTrue(plot.isDomainGridlinesVisible());
        Assert.assertTrue(plot.isRangeGridlinesVisible());
        Assert.assertFalse(plot.isDomainMinorGridlinesVisible());
        Assert.assertFalse(plot.isRangeMinorGridlinesVisible());
        Assert.assertFalse(plot.isDomainZeroBaselineVisible());
        Assert.assertFalse(plot.isRangeZeroBaselineVisible());
        Assert.assertFalse(plot.isDomainCrosshairVisible());
        Assert.assertFalse(plot.isRangeCrosshairVisible());
        Assert.assertTrue(plot.isDomainCrosshairLockedOnData());
        Assert.assertTrue(plot.isRangeCrosshairLockedOnData());
        Assert.assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());
        Assert.assertEquals(SeriesRenderingOrder.REVERSE, plot.getSeriesRenderingOrder());
    }

    @Test
    public void testCustomConstructor() {
        XYDataset dataset = createSampleDataset();
        NumberAxis domainAxis = new NumberAxis("X");
        NumberAxis rangeAxis = new NumberAxis("Y");
        XYItemRenderer renderer = new XYLineAndShapeRenderer();

        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);

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
        XYPlot plot = new XYPlot();
        TestPlotListener listener = new TestPlotListener();
        plot.addChangeListener(listener);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
        Assert.assertEquals(1, listener.eventCount);

        // Setting same orientation shouldn't fire change event
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(1, listener.eventCount);

        try {
            plot.setOrientation(null);
            Assert.fail("Null orientation should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testAxisOffset() {
        XYPlot plot = new XYPlot();
        TestPlotListener listener = new TestPlotListener();
        plot.addChangeListener(listener);

        RectangleInsets insets = new RectangleInsets(5, 5, 5, 5);
        plot.setAxisOffset(insets);
        Assert.assertEquals(insets, plot.getAxisOffset());
        Assert.assertEquals(1, listener.eventCount);

        try {
            plot.setAxisOffset(null);
            Assert.fail("Null offset should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testDomainAxesOperations() {
        XYPlot plot = new XYPlot();
        NumberAxis axis1 = new NumberAxis("X1");
        NumberAxis axis2 = new NumberAxis("X2");

        plot.setDomainAxis(0, axis1);
        plot.setDomainAxis(1, axis2);
        Assert.assertEquals(2, plot.getDomainAxisCount());
        Assert.assertSame(axis1, plot.getDomainAxis(0));
        Assert.assertSame(axis2, plot.getDomainAxis(1));
        Assert.assertEquals(0, plot.getDomainAxisIndex(axis1));
        Assert.assertEquals(1, plot.getDomainAxisIndex(axis2));
        Assert.assertEquals(-1, plot.getDomainAxisIndex(new NumberAxis("Unknown")));

        plot.setDomainAxisLocation(0, AxisLocation.TOP_OR_LEFT);
        Assert.assertEquals(AxisLocation.TOP_OR_LEFT, plot.getDomainAxisLocation(0));
        Assert.assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getDomainAxisLocation(1)); // Opposite of index 0

        plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getDomainAxisLocation(1));

        plot.setDomainAxes(new ValueAxis[] {axis2, axis1});
        Assert.assertSame(axis2, plot.getDomainAxis(0));
        Assert.assertSame(axis1, plot.getDomainAxis(1));

        plot.clearDomainAxes();
        Assert.assertEquals(0, plot.getDomainAxisCount());
        Assert.assertNull(plot.getDomainAxis(0));
    }

    @Test
    public void testRangeAxesOperations() {
        XYPlot plot = new XYPlot();
        NumberAxis axis1 = new NumberAxis("Y1");
        NumberAxis axis2 = new NumberAxis("Y2");

        plot.setRangeAxis(0, axis1);
        plot.setRangeAxis(1, axis2);
        Assert.assertEquals(2, plot.getRangeAxisCount());
        Assert.assertSame(axis1, plot.getRangeAxis(0));
        Assert.assertSame(axis2, plot.getRangeAxis(1));
        Assert.assertEquals(0, plot.getRangeAxisIndex(axis1));
        Assert.assertEquals(1, plot.getRangeAxisIndex(axis2));
        Assert.assertEquals(-1, plot.getRangeAxisIndex(new NumberAxis("Unknown")));

        plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getRangeAxisLocation(0));
        Assert.assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getRangeAxisLocation(1)); // Opposite

        plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
        Assert.assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getRangeAxisLocation(1));

        plot.setRangeAxes(new ValueAxis[] {axis2, axis1});
        Assert.assertSame(axis2, plot.getRangeAxis(0));
        Assert.assertSame(axis1, plot.getRangeAxis(1));

        plot.clearRangeAxes();
        Assert.assertEquals(0, plot.getRangeAxisCount());
        Assert.assertNull(plot.getRangeAxis(0));
    }

    @Test
    public void testParentPlotAxisDelegation() {
        XYPlot parent = new XYPlot();
        NumberAxis domain = new NumberAxis("Parent Domain");
        NumberAxis range = new NumberAxis("Parent Range");
        parent.setDomainAxis(0, domain);
        parent.setRangeAxis(0, range);

        XYPlot child = new XYPlot();
        child.setParent(parent);

        Assert.assertSame(domain, child.getDomainAxis());
        Assert.assertSame(range, child.getRangeAxis());
        Assert.assertEquals(0, child.getDomainAxisIndex(domain));
        Assert.assertEquals(0, child.getRangeAxisIndex(range));
    }

    @Test
    public void testAxisEdgeResolutions() {
        XYPlot plot = new XYPlot();
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setDomainAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
        plot.setRangeAxisLocation(0, AxisLocation.BOTTOM_OR_LEFT);
        Assert.assertEquals(RectangleEdge.BOTTOM, plot.getDomainAxisEdge());
        Assert.assertEquals(RectangleEdge.LEFT, plot.getRangeAxisEdge());

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(RectangleEdge.LEFT, plot.getDomainAxisEdge());
        Assert.assertEquals(RectangleEdge.BOTTOM, plot.getRangeAxisEdge());
    }

    @Test
    public void testDatasetsAndRenderers() {
        XYPlot plot = new XYPlot();
        XYDataset ds1 = createSampleDataset();
        XYDataset ds2 = new DefaultTableXYDataset();
        XYItemRenderer r1 = new XYLineAndShapeRenderer();
        XYItemRenderer r2 = new StandardXYItemRenderer();

        plot.setDataset(0, ds1);
        plot.setDataset(1, ds2);
        plot.setRenderer(0, r1);
        plot.setRenderer(1, r2);

        Assert.assertEquals(2, plot.getDatasetCount());
        Assert.assertEquals(2, plot.getRendererCount());
        Assert.assertSame(ds1, plot.getDataset(0));
        Assert.assertSame(ds2, plot.getDataset(1));
        Assert.assertSame(r1, plot.getRenderer(0));
        Assert.assertSame(r2, plot.getRenderer(1));

        Assert.assertEquals(0, plot.indexOf(ds1));
        Assert.assertEquals(1, plot.indexOf(ds2));
        Assert.assertEquals(-1, plot.indexOf(createSampleDataset()));

        Assert.assertEquals(0, plot.getIndexOf(r1));
        Assert.assertEquals(1, plot.getIndexOf(r2));
        Assert.assertEquals(-1, plot.getIndexOf(new XYLineAndShapeRenderer()));

        Assert.assertSame(r1, plot.getRendererForDataset(ds1));
        Assert.assertSame(r2, plot.getRendererForDataset(ds2));

        plot.setRenderers(new XYItemRenderer[] {r2, r1});
        Assert.assertSame(r2, plot.getRenderer(0));
        Assert.assertSame(r1, plot.getRenderer(1));
    }

    @Test
    public void testDatasetAxisMappings() {
        XYPlot plot = new XYPlot();
        plot.setDomainAxis(0, new NumberAxis("X0"));
        plot.setDomainAxis(1, new NumberAxis("X1"));
        plot.setRangeAxis(0, new NumberAxis("Y0"));
        plot.setRangeAxis(1, new NumberAxis("Y1"));

        plot.mapDatasetToDomainAxis(0, 1);
        plot.mapDatasetToRangeAxis(0, 1);

        Assert.assertSame(plot.getDomainAxis(1), plot.getDomainAxisForDataset(0));
        Assert.assertSame(plot.getRangeAxis(1), plot.getRangeAxisForDataset(0));

        List domainAxesList = Arrays.asList(new Integer(1), new Integer(0));
        List rangeAxesList = Arrays.asList(new Integer(1), new Integer(0));
        plot.mapDatasetToDomainAxes(0, domainAxesList);
        plot.mapDatasetToRangeAxes(0, rangeAxesList);

        Assert.assertSame(plot.getDomainAxis(1), plot.getDomainAxisForDataset(0));
        Assert.assertSame(plot.getRangeAxis(1), plot.getRangeAxisForDataset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapDatasetToDomainAxesInvalidNegative() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToDomainAxes(-1, new ArrayList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapDatasetToDomainAxesEmptyList() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToDomainAxes(0, new ArrayList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapDatasetToDomainAxesDuplicateList() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToDomainAxes(0, Arrays.asList(new Integer(0), new Integer(0)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapDatasetToDomainAxesNonIntegerList() {
        XYPlot plot = new XYPlot();
        plot.mapDatasetToDomainAxes(0, Arrays.asList("invalid"));
    }

    @Test
    public void testGridlineAndBaselineProperties() {
        XYPlot plot = new XYPlot();
        Stroke stroke = new BasicStroke(2.0f);
        Paint paint = Color.RED;

        plot.setDomainGridlinesVisible(false);
        Assert.assertFalse(plot.isDomainGridlinesVisible());
        plot.setDomainGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getDomainGridlineStroke());
        plot.setDomainGridlinePaint(paint);
        Assert.assertEquals(paint, plot.getDomainGridlinePaint());

        plot.setDomainMinorGridlinesVisible(true);
        Assert.assertTrue(plot.isDomainMinorGridlinesVisible());
        plot.setDomainMinorGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getDomainMinorGridlineStroke());
        plot.setDomainMinorGridlinePaint(paint);
        Assert.assertEquals(paint, plot.getDomainMinorGridlinePaint());

        plot.setRangeGridlinesVisible(false);
        Assert.assertFalse(plot.isRangeGridlinesVisible());
        plot.setRangeGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeGridlineStroke());
        plot.setRangeGridlinePaint(paint);
        Assert.assertEquals(paint, plot.getRangeGridlinePaint());

        plot.setRangeMinorGridlinesVisible(true);
        Assert.assertTrue(plot.isRangeMinorGridlinesVisible());
        plot.setRangeMinorGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeMinorGridlineStroke());
        plot.setRangeMinorGridlinePaint(paint);
        Assert.assertEquals(paint, plot.getRangeMinorGridlinePaint());

        plot.setDomainZeroBaselineVisible(true);
        Assert.assertTrue(plot.isDomainZeroBaselineVisible());
        plot.setDomainZeroBaselineStroke(stroke);
        Assert.assertEquals(stroke, plot.getDomainZeroBaselineStroke());
        plot.setDomainZeroBaselinePaint(paint);
        Assert.assertEquals(paint, plot.getDomainZeroBaselinePaint());

        plot.setRangeZeroBaselineVisible(true);
        Assert.assertTrue(plot.isRangeZeroBaselineVisible());
        plot.setRangeZeroBaselineStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeZeroBaselineStroke());
        plot.setRangeZeroBaselinePaint(paint);
        Assert.assertEquals(paint, plot.getRangeZeroBaselinePaint());
    }

    @Test
    public void testCrosshairs() {
        XYPlot plot = new XYPlot();
        Stroke stroke = new BasicStroke(1.5f);
        Paint paint = Color.GREEN;

        plot.setDomainCrosshairVisible(true);
        Assert.assertTrue(plot.isDomainCrosshairVisible());
        plot.setDomainCrosshairValue(10.5);
        Assert.assertEquals(10.5, plot.getDomainCrosshairValue(), 1e-6);
        plot.setDomainCrosshairLockedOnData(false);
        Assert.assertFalse(plot.isDomainCrosshairLockedOnData());
        plot.setDomainCrosshairStroke(stroke);
        Assert.assertEquals(stroke, plot.getDomainCrosshairStroke());
        plot.setDomainCrosshairPaint(paint);
        Assert.assertEquals(paint, plot.getDomainCrosshairPaint());

        plot.setRangeCrosshairVisible(true);
        Assert.assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairValue(20.5);
        Assert.assertEquals(20.5, plot.getRangeCrosshairValue(), 1e-6);
        plot.setRangeCrosshairLockedOnData(false);
        Assert.assertFalse(plot.isRangeCrosshairLockedOnData());
        plot.setRangeCrosshairStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeCrosshairStroke());
        plot.setRangeCrosshairPaint(paint);
        Assert.assertEquals(paint, plot.getRangeCrosshairPaint());
    }

    @Test
    public void testQuadrants() {
        XYPlot plot = new XYPlot();
        Point2D origin = new Point2D.Double(5.0, 5.0);
        plot.setQuadrantOrigin(origin);
        Assert.assertEquals(origin, plot.getQuadrantOrigin());

        plot.setQuadrantPaint(0, Color.RED);
        plot.setQuadrantPaint(1, Color.BLUE);
        plot.setQuadrantPaint(2, Color.GREEN);
        plot.setQuadrantPaint(3, Color.YELLOW);

        Assert.assertEquals(Color.RED, plot.getQuadrantPaint(0));
        Assert.assertEquals(Color.BLUE, plot.getQuadrantPaint(1));
        Assert.assertEquals(Color.GREEN, plot.getQuadrantPaint(2));
        Assert.assertEquals(Color.YELLOW, plot.getQuadrantPaint(3));

        try {
            plot.setQuadrantPaint(-1, Color.BLACK);
            Assert.fail("Index < 0 should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            plot.setQuadrantPaint(4, Color.BLACK);
            Assert.fail("Index > 3 should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testMarkers() {
        XYPlot plot = new XYPlot();
        Marker m1 = new ValueMarker(1.0);
        Marker m2 = new IntervalMarker(2.0, 3.0);

        plot.addDomainMarker(m1);
        plot.addDomainMarker(m2, Layer.BACKGROUND);
        Collection domainFg = plot.getDomainMarkers(Layer.FOREGROUND);
        Collection domainBg = plot.getDomainMarkers(Layer.BACKGROUND);
        Assert.assertTrue(domainFg.contains(m1));
        Assert.assertTrue(domainBg.contains(m2));

        Assert.assertTrue(plot.removeDomainMarker(m1));
        Assert.assertFalse(plot.removeDomainMarker(m1)); // Already removed
        Assert.assertTrue(plot.removeDomainMarker(m2, Layer.BACKGROUND));

        Marker rm1 = new ValueMarker(5.0);
        Marker rm2 = new IntervalMarker(6.0, 7.0);
        plot.addRangeMarker(rm1);
        plot.addRangeMarker(rm2, Layer.BACKGROUND);
        Collection rangeFg = plot.getRangeMarkers(Layer.FOREGROUND);
        Collection rangeBg = plot.getRangeMarkers(Layer.BACKGROUND);
        Assert.assertTrue(rangeFg.contains(rm1));
        Assert.assertTrue(rangeBg.contains(rm2));

        Assert.assertTrue(plot.removeRangeMarker(rm1));
        Assert.assertTrue(plot.removeRangeMarker(rm2, Layer.BACKGROUND));

        plot.addDomainMarker(m1);
        plot.addDomainMarker(m2, Layer.BACKGROUND);
        plot.clearDomainMarkers();
        Assert.assertNull(plot.getDomainMarkers(Layer.FOREGROUND));
        Assert.assertNull(plot.getDomainMarkers(Layer.BACKGROUND));

        plot.addRangeMarker(rm1);
        plot.addRangeMarker(rm2, Layer.BACKGROUND);
        plot.clearRangeMarkers();
        Assert.assertNull(plot.getRangeMarkers(Layer.FOREGROUND));
        Assert.assertNull(plot.getRangeMarkers(Layer.BACKGROUND));
    }

    @Test
    public void testAnnotations() {
        XYPlot plot = new XYPlot();
        XYTextAnnotation a1 = new XYTextAnnotation("A1", 1.0, 2.0);
        XYTextAnnotation a2 = new XYTextAnnotation("A2", 3.0, 4.0);

        plot.addAnnotation(a1);
        plot.addAnnotation(a2);

        List annotations = plot.getAnnotations();
        Assert.assertEquals(2, annotations.size());
        Assert.assertTrue(annotations.contains(a1));
        Assert.assertTrue(annotations.contains(a2));

        Assert.assertTrue(plot.removeAnnotation(a1));
        Assert.assertFalse(plot.removeAnnotation(a1));
        Assert.assertEquals(1, plot.getAnnotations().size());

        plot.clearAnnotations();
        Assert.assertEquals(0, plot.getAnnotations().size());
    }

    @Test
    public void testGetDataRange() {
        NumberAxis domainAxis = new NumberAxis("X");
        NumberAxis rangeAxis = new NumberAxis("Y");
        XYPlot plot = new XYPlot(createSampleDataset(), domainAxis, rangeAxis, new XYLineAndShapeRenderer());

        Range dRange = plot.getDataRange(domainAxis);
        Assert.assertNotNull(dRange);
        Assert.assertEquals(1.0, dRange.getLowerBound(), 1e-6);
        Assert.assertEquals(3.0, dRange.getUpperBound(), 1e-6);

        Range rRange = plot.getDataRange(rangeAxis);
        Assert.assertNotNull(rRange);
        Assert.assertEquals(1.0, rRange.getLowerBound(), 1e-6);
        Assert.assertEquals(4.0, rRange.getUpperBound(), 1e-6);

        XYTextAnnotation ann = new XYTextAnnotation("Test", 10.0, 20.0);
        plot.addAnnotation(ann);
        // xy text annotation does not implement XYAnnotationBoundsInfo, data range should be unaffected
        Assert.assertEquals(3.0, plot.getDataRange(domainAxis).getUpperBound(), 1e-6);

        XYLineAnnotation lineAnn = new XYLineAnnotation(0.0, 0.0, 5.0, 10.0);
        plot.addAnnotation(lineAnn);
        // XYLineAnnotation implements XYAnnotationBoundsInfo
        Assert.assertEquals(5.0, plot.getDataRange(domainAxis).getUpperBound(), 1e-6);
        Assert.assertEquals(10.0, plot.getDataRange(rangeAxis).getUpperBound(), 1e-6);
    }

    @Test
    public void testPanning() {
        NumberAxis domainAxis = new NumberAxis("X");
        domainAxis.setRange(0.0, 10.0);
        NumberAxis rangeAxis = new NumberAxis("Y");
        rangeAxis.setRange(0.0, 10.0);
        XYPlot plot = new XYPlot(null, domainAxis, rangeAxis, null);

        Assert.assertFalse(plot.isDomainPannable());
        Assert.assertFalse(plot.isRangePannable());

        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        Assert.assertTrue(plot.isDomainPannable());
        Assert.assertTrue(plot.isRangePannable());

        plot.panDomainAxes(0.1, null, new Point2D.Double(0, 0));
        Assert.assertEquals(1.0, domainAxis.getLowerBound(), 1e-6);
        Assert.assertEquals(11.0, domainAxis.getUpperBound(), 1e-6);

        plot.panRangeAxes(-0.1, null, new Point2D.Double(0, 0));
        Assert.assertEquals(-1.0, rangeAxis.getLowerBound(), 1e-6);
        Assert.assertEquals(9.0, rangeAxis.getUpperBound(), 1e-6);
    }

    @Test
    public void testZooming() {
        NumberAxis domainAxis = new NumberAxis("X");
        domainAxis.setRange(0.0, 10.0);
        NumberAxis rangeAxis = new NumberAxis("Y");
        rangeAxis.setRange(0.0, 10.0);
        XYPlot plot = new XYPlot(null, domainAxis, rangeAxis, null);

        Assert.assertTrue(plot.isDomainZoomable());
        Assert.assertTrue(plot.isRangeZoomable());

        plot.zoomDomainAxes(0.5, null, null);
        Assert.assertEquals(2.5, domainAxis.getLowerBound(), 1e-6);
        Assert.assertEquals(7.5, domainAxis.getUpperBound(), 1e-6);

        plot.zoomRangeAxes(2.0, null, null);
        Assert.assertEquals(-5.0, rangeAxis.getLowerBound(), 1e-6);
        Assert.assertEquals(15.0, rangeAxis.getUpperBound(), 1e-6);

        plot.zoomDomainAxes(0.2, 0.8, null, null);
        Assert.assertEquals(3.5, domainAxis.getLowerBound(), 1e-6);
        Assert.assertEquals(6.5, domainAxis.getUpperBound(), 1e-6);

        plot.zoomRangeAxes(0.25, 0.75, null, null);
        Assert.assertEquals(0.0, rangeAxis.getLowerBound(), 1e-6);
        Assert.assertEquals(10.0, rangeAxis.getUpperBound(), 1e-6);
    }

    @Test
    public void testLegendItems() {
        XYPlot plot = new XYPlot(createSampleDataset(), new NumberAxis("X"), new NumberAxis("Y"), new XYLineAndShapeRenderer());
        LegendItemCollection items = plot.getLegendItems();
        Assert.assertEquals(2, items.getItemCount());

        LegendItemCollection fixed = new LegendItemCollection();
        fixed.add(new LegendItem("Fixed"));
        plot.setFixedLegendItems(fixed);
        Assert.assertSame(fixed, plot.getLegendItems());
        Assert.assertEquals(1, plot.getLegendItems().getItemCount());
    }

    @Test
    public void testDrawRenderingPipeline() {
        XYDataset dataset = createSampleDataset();
        NumberAxis domainAxis = new NumberAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);

        // Configure various visual elements to exercise full draw branches
        plot.setDomainTickBandPaint(new Color(240, 240, 240));
        plot.setRangeTickBandPaint(new Color(245, 245, 245));
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainMinorGridlinesVisible(true);
        plot.setRangeMinorGridlinesVisible(true);
        plot.setQuadrantPaint(0, new Color(255, 200, 200));
        plot.setQuadrantPaint(1, new Color(200, 255, 200));
        plot.setQuadrantPaint(2, new Color(200, 200, 255));
        plot.setQuadrantPaint(3, new Color(255, 255, 200));
        plot.addDomainMarker(new ValueMarker(2.0), Layer.BACKGROUND);
        plot.addDomainMarker(new ValueMarker(2.0), Layer.FOREGROUND);
        plot.addRangeMarker(new ValueMarker(2.5), Layer.BACKGROUND);
        plot.addRangeMarker(new ValueMarker(2.5), Layer.FOREGROUND);
        plot.addAnnotation(new XYTextAnnotation("Test Annotation", 2.0, 2.0));

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = chart.createBufferedImage(500, 300);
        Assert.assertNotNull(image);

        // Switch to Horizontal orientation and FORWARD rendering order
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        BufferedImage imageH = chart.createBufferedImage(500, 300);
        Assert.assertNotNull(imageH);
    }

    @Test
    public void testDrawEmptyPlotAndNoDataMessage() {
        XYPlot plot = new XYPlot(new DefaultXYDataset(), new NumberAxis("X"), new NumberAxis("Y"), new XYLineAndShapeRenderer());
        plot.setNoDataMessage("NO DATA FOUND");
        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);
    }

    @Test
    public void testHandleClick() {
        NumberAxis domainAxis = new NumberAxis("X");
        NumberAxis rangeAxis = new NumberAxis("Y");
        XYPlot plot = new XYPlot(createSampleDataset(), domainAxis, rangeAxis, new XYLineAndShapeRenderer());

        ChartRenderingInfo info = new ChartRenderingInfo();
        JFreeChart chart = new JFreeChart(plot);
        chart.createBufferedImage(400, 300, info);

        plot.handleClick(200, 150, info.getPlotInfo());
        Assert.assertTrue(plot.getDomainCrosshairValue() > 0.0);
        Assert.assertTrue(plot.getRangeCrosshairValue() > 0.0);
    }

    @Test
    public void testFixedAxisSpaces() {
        XYPlot plot = new XYPlot();
        AxisSpace ds = new AxisSpace();
        ds.setTop(10.0);
        ds.setBottom(10.0);
        plot.setFixedDomainAxisSpace(ds);
        Assert.assertSame(ds, plot.getFixedDomainAxisSpace());

        AxisSpace rs = new AxisSpace();
        rs.setLeft(15.0);
        rs.setRight(15.0);
        plot.setFixedRangeAxisSpace(rs);
        Assert.assertSame(rs, plot.getFixedRangeAxisSpace());
    }

    @Test
    public void testEqualsAndHashCode() {
        XYPlot plot1 = new XYPlot(createSampleDataset(), new NumberAxis("X"), new NumberAxis("Y"), new XYLineAndShapeRenderer());
        XYPlot plot2 = new XYPlot(createSampleDataset(), new NumberAxis("X"), new NumberAxis("Y"), new XYLineAndShapeRenderer());

        Assert.assertEquals(plot1, plot2);
        Assert.assertEquals(plot1, plot1);
        Assert.assertFalse(plot1.equals(null));
        Assert.assertFalse(plot1.equals("Not an XYPlot"));

        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertFalse(plot1.equals(plot2));
        plot2.setOrientation(PlotOrientation.VERTICAL);
        Assert.assertEquals(plot1, plot2);

        plot2.setDomainCrosshairVisible(true);
        Assert.assertFalse(plot1.equals(plot2));
        plot2.setDomainCrosshairVisible(false);
        Assert.assertEquals(plot1, plot2);

        plot2.setWeight(5);
        Assert.assertFalse(plot1.equals(plot2));
        plot2.setWeight(1);
        Assert.assertEquals(plot1, plot2);

        plot2.setQuadrantOrigin(new Point2D.Double(1.0, 1.0));
        Assert.assertFalse(plot1.equals(plot2));
        plot2.setQuadrantOrigin(new Point2D.Double(0.0, 0.0));
        Assert.assertEquals(plot1, plot2);
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        XYPlot plot1 = new XYPlot(createSampleDataset(), new NumberAxis("X"), new NumberAxis("Y"), new XYLineAndShapeRenderer());
        plot1.addAnnotation(new XYTextAnnotation("Anno", 1.0, 2.0));
        plot1.addDomainMarker(new ValueMarker(1.5));
        plot1.addRangeMarker(new ValueMarker(2.5));
        plot1.setQuadrantPaint(0, Color.PINK);

        XYPlot plot2 = (XYPlot) plot1.clone();

        Assert.assertNotSame(plot1, plot2);
        Assert.assertSame(plot2, plot2.getDomainAxis().getPlot());
        Assert.assertSame(plot2, plot2.getRangeAxis().getPlot());
        Assert.assertEquals(plot1, plot2);

        plot2.setDomainCrosshairValue(99.0);
        Assert.assertFalse(plot1.getDomainCrosshairValue() == plot2.getDomainCrosshairValue());
    }

    @Test
    public void testSerialization() throws Exception {
        XYPlot plot1 = new XYPlot(createSampleDataset(), new NumberAxis("X"), new NumberAxis("Y"), new XYLineAndShapeRenderer());
        plot1.setQuadrantPaint(0, Color.RED);
        plot1.setDomainTickBandPaint(Color.GRAY);
        plot1.setRangeTickBandPaint(Color.LIGHT_GRAY);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(plot1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        XYPlot plot2 = (XYPlot) in.readObject();
        in.close();

        Assert.assertEquals(plot1, plot2);
        Assert.assertSame(plot2, plot2.getDomainAxis().getPlot());
        Assert.assertSame(plot2, plot2.getRangeAxis().getPlot());
    }

    @Test
    public void testSelectionSupport() {
        XYPlot plot = new XYPlot();
        Assert.assertFalse(plot.canSelectByPoint());
        Assert.assertTrue(plot.canSelectByRegion());

        plot.select(1.0, 2.0, new Rectangle2D.Double(0, 0, 100, 100), null);
        plot.clearSelection();
    }

    @Test
    public void testEventNotifications() {
        XYPlot plot = new XYPlot();
        TestPlotListener listener = new TestPlotListener();
        plot.addChangeListener(listener);

        plot.datasetChanged(new DatasetChangeEvent(this, null));
        Assert.assertTrue(listener.eventCount > 0);

        int count = listener.eventCount;
        RendererChangeEvent rce = new RendererChangeEvent(new XYLineAndShapeRenderer(), true);
        plot.rendererChanged(rce);
        Assert.assertTrue(listener.eventCount > count);
    }
}