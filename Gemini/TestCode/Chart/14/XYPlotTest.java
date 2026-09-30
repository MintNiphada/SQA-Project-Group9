package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
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

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Assert;
import org.junit.Test;

public class XYPlotTest {

    private static class MyPlotChangeListener implements PlotChangeListener {
        int eventCount = 0;

        public void plotChanged(PlotChangeEvent event) {
            this.eventCount++;
        }
    }

    @Test
    public void testConstructorsAndDefaults() {
        XYPlot plot1 = new XYPlot();
        Assert.assertNull(plot1.getDataset());
        Assert.assertNull(plot1.getDomainAxis());
        Assert.assertNull(plot1.getRangeAxis());
        Assert.assertNull(plot1.getRenderer());
        Assert.assertEquals(PlotOrientation.VERTICAL, plot1.getOrientation());
        Assert.assertEquals(1, plot1.getWeight());
        Assert.assertTrue(plot1.isDomainGridlinesVisible());
        Assert.assertTrue(plot1.isRangeGridlinesVisible());
        Assert.assertFalse(plot1.isDomainZeroBaselineVisible());
        Assert.assertFalse(plot1.isRangeZeroBaselineVisible());
        Assert.assertFalse(plot1.isDomainCrosshairVisible());
        Assert.assertFalse(plot1.isRangeCrosshairVisible());
        Assert.assertTrue(plot1.isDomainCrosshairLockedOnData());
        Assert.assertTrue(plot1.isRangeCrosshairLockedOnData());
        Assert.assertNotNull(plot1.getPlotType());

        NumberAxis xAxis = new NumberAxis("X");
        NumberAxis yAxis = new NumberAxis("Y");
        XYSeriesCollection dataset = new XYSeriesCollection();
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();
        XYPlot plot2 = new XYPlot(dataset, xAxis, yAxis, renderer);

        Assert.assertEquals(dataset, plot2.getDataset());
        Assert.assertEquals(xAxis, plot2.getDomainAxis());
        Assert.assertEquals(yAxis, plot2.getRangeAxis());
        Assert.assertEquals(renderer, plot2.getRenderer());
        Assert.assertEquals(plot2, xAxis.getPlot());
        Assert.assertEquals(plot2, yAxis.getPlot());
        Assert.assertEquals(plot2, renderer.getPlot());
    }

    @Test
    public void testOrientation() {
        XYPlot plot = new XYPlot();
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());

        try {
            plot.setOrientation(null);
            Assert.fail("Expected IllegalArgumentException for null orientation");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAxisOffset() {
        XYPlot plot = new XYPlot();
        RectangleInsets insets = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
        plot.setAxisOffset(insets);
        Assert.assertEquals(insets, plot.getAxisOffset());

        try {
            plot.setAxisOffset(null);
            Assert.fail("Expected IllegalArgumentException for null offset");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDomainAxesManagement() {
        XYPlot plot = new XYPlot();
        NumberAxis axis0 = new NumberAxis("X0");
        NumberAxis axis1 = new NumberAxis("X1");

        plot.setDomainAxis(0, axis0);
        plot.setDomainAxis(1, axis1);
        Assert.assertEquals(axis0, plot.getDomainAxis());
        Assert.assertEquals(axis0, plot.getDomainAxis(0));
        Assert.assertEquals(axis1, plot.getDomainAxis(1));
        Assert.assertEquals(2, plot.getDomainAxisCount());
        Assert.assertEquals(0, plot.getDomainAxisIndex(axis0));
        Assert.assertEquals(1, plot.getDomainAxisIndex(axis1));
        Assert.assertEquals(-1, plot.getDomainAxisIndex(new NumberAxis("Nonexistent")));

        plot.setDomainAxes(new ValueAxis[] { axis1, axis0 });
        Assert.assertEquals(axis1, plot.getDomainAxis(0));
        Assert.assertEquals(axis0, plot.getDomainAxis(1));

        plot.clearDomainAxes();
        Assert.assertEquals(0, plot.getDomainAxisCount());
        Assert.assertNull(plot.getDomainAxis(0));

        // Parent lookup
        CombinedDomainXYPlot parent = new CombinedDomainXYPlot(axis0);
        XYPlot subplot = new XYPlot();
        parent.add(subplot);
        Assert.assertEquals(axis0, subplot.getDomainAxis(0));
        Assert.assertEquals(0, subplot.getDomainAxisIndex(axis0));
    }

    @Test
    public void testRangeAxesManagement() {
        XYPlot plot = new XYPlot();
        NumberAxis axis0 = new NumberAxis("Y0");
        NumberAxis axis1 = new NumberAxis("Y1");

        plot.setRangeAxis(0, axis0);
        plot.setRangeAxis(1, axis1);
        Assert.assertEquals(axis0, plot.getRangeAxis());
        Assert.assertEquals(axis0, plot.getRangeAxis(0));
        Assert.assertEquals(axis1, plot.getRangeAxis(1));
        Assert.assertEquals(2, plot.getRangeAxisCount());
        Assert.assertEquals(0, plot.getRangeAxisIndex(axis0));
        Assert.assertEquals(1, plot.getRangeAxisIndex(axis1));
        Assert.assertEquals(-1, plot.getRangeAxisIndex(new NumberAxis("Nonexistent")));

        plot.setRangeAxes(new ValueAxis[] { axis1, axis0 });
        Assert.assertEquals(axis1, plot.getRangeAxis(0));
        Assert.assertEquals(axis0, plot.getRangeAxis(1));

        plot.clearRangeAxes();
        Assert.assertEquals(0, plot.getRangeAxisCount());
        Assert.assertNull(plot.getRangeAxis(0));

        // Parent lookup
        CombinedRangeXYPlot parent = new CombinedRangeXYPlot(axis0);
        XYPlot subplot = new XYPlot();
        parent.add(subplot);
        Assert.assertEquals(axis0, subplot.getRangeAxis(0));
        Assert.assertEquals(0, subplot.getRangeAxisIndex(axis0));
    }

    @Test
    public void testAxisLocationsAndEdges() {
        XYPlot plot = new XYPlot();
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        Assert.assertEquals(AxisLocation.TOP_OR_LEFT, plot.getDomainAxisLocation());
        Assert.assertEquals(RectangleEdge.TOP, plot.getDomainAxisEdge());

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(RectangleEdge.LEFT, plot.getDomainAxisEdge());

        plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getDomainAxisLocation(1));
        Assert.assertEquals(RectangleEdge.RIGHT, plot.getDomainAxisEdge(1));

        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        Assert.assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getRangeAxisLocation());
        Assert.assertEquals(RectangleEdge.BOTTOM, plot.getRangeAxisEdge());

        plot.setOrientation(PlotOrientation.VERTICAL);
        Assert.assertEquals(RectangleEdge.RIGHT, plot.getRangeAxisEdge());

        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getRangeAxisLocation(1));

        try {
            plot.setDomainAxisLocation(0, null);
            Assert.fail("Expected IllegalArgumentException for null location at index 0");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setRangeAxisLocation(0, null);
            Assert.fail("Expected IllegalArgumentException for null location at index 0");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDatasetsAndRenderers() {
        XYPlot plot = new XYPlot();
        XYSeries series1 = new XYSeries("S1");
        series1.add(1.0, 2.0);
        XYSeriesCollection d0 = new XYSeriesCollection(series1);
        XYSeriesCollection d1 = new XYSeriesCollection();

        plot.setDataset(0, d0);
        plot.setDataset(1, d1);
        Assert.assertEquals(d0, plot.getDataset());
        Assert.assertEquals(d0, plot.getDataset(0));
        Assert.assertEquals(d1, plot.getDataset(1));
        Assert.assertEquals(2, plot.getDatasetCount());
        Assert.assertEquals(0, plot.indexOf(d0));
        Assert.assertEquals(1, plot.indexOf(d1));
        Assert.assertEquals(-1, plot.indexOf(new XYSeriesCollection()));

        XYItemRenderer r0 = new StandardXYItemRenderer();
        XYItemRenderer r1 = new DefaultXYItemRenderer();
        plot.setRenderer(0, r0);
        plot.setRenderer(1, r1);
        Assert.assertEquals(r0, plot.getRenderer());
        Assert.assertEquals(r0, plot.getRenderer(0));
        Assert.assertEquals(r1, plot.getRenderer(1));
        Assert.assertEquals(0, plot.getIndexOf(r0));
        Assert.assertEquals(1, plot.getIndexOf(r1));
        Assert.assertEquals(-1, plot.getIndexOf(new StandardXYItemRenderer()));

        Assert.assertEquals(r0, plot.getRendererForDataset(d0));
        Assert.assertEquals(r1, plot.getRendererForDataset(d1));

        plot.setRenderers(new XYItemRenderer[] { r1, r0 });
        Assert.assertEquals(r1, plot.getRenderer(0));
        Assert.assertEquals(r0, plot.getRenderer(1));

        Assert.assertEquals(1, plot.getSeriesCount());
    }

    @Test
    public void testDatasetAxisMapping() {
        XYPlot plot = new XYPlot();
        XYSeries series = new XYSeries("S");
        series.add(10.0, 20.0);
        XYSeriesCollection dataset0 = new XYSeriesCollection(series);
        XYSeriesCollection dataset1 = new XYSeriesCollection(series);
        NumberAxis domain0 = new NumberAxis("D0");
        NumberAxis domain1 = new NumberAxis("D1");
        NumberAxis range0 = new NumberAxis("R0");
        NumberAxis range1 = new NumberAxis("R1");

        plot.setDataset(0, dataset0);
        plot.setDataset(1, dataset1);
        plot.setDomainAxis(0, domain0);
        plot.setDomainAxis(1, domain1);
        plot.setRangeAxis(0, range0);
        plot.setRangeAxis(1, range1);

        plot.mapDatasetToDomainAxis(1, 1);
        plot.mapDatasetToRangeAxis(1, 1);

        Assert.assertEquals(domain0, plot.getDomainAxisForDataset(0));
        Assert.assertEquals(domain1, plot.getDomainAxisForDataset(1));
        Assert.assertEquals(range0, plot.getRangeAxisForDataset(0));
        Assert.assertEquals(range1, plot.getRangeAxisForDataset(1));

        try {
            plot.getDomainAxisForDataset(-1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.getRangeAxisForDataset(2);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetDataRange() {
        XYPlot plot = new XYPlot();
        XYSeries s = new XYSeries("S");
        s.add(1.0, 10.0);
        s.add(5.0, 50.0);
        XYSeriesCollection dataset = new XYSeriesCollection(s);
        NumberAxis domainAxis = new NumberAxis("X");
        NumberAxis rangeAxis = new NumberAxis("Y");
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();

        plot.setDataset(dataset);
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setRenderer(renderer);

        Range xRange = plot.getDataRange(domainAxis);
        Assert.assertNotNull(xRange);
        Assert.assertEquals(1.0, xRange.getLowerBound(), 1e-9);
        Assert.assertEquals(5.0, xRange.getUpperBound(), 1e-9);

        Range yRange = plot.getDataRange(rangeAxis);
        Assert.assertNotNull(yRange);
        Assert.assertEquals(10.0, yRange.getLowerBound(), 1e-9);
        Assert.assertEquals(50.0, yRange.getUpperBound(), 1e-9);

        NumberAxis unmappedAxis = new NumberAxis("Unmapped");
        Assert.assertNull(plot.getDataRange(unmappedAxis));
    }

    @Test
    public void testRenderingOrders() {
        XYPlot plot = new XYPlot();
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        Assert.assertEquals(DatasetRenderingOrder.FORWARD, plot.getDatasetRenderingOrder());

        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        Assert.assertEquals(SeriesRenderingOrder.FORWARD, plot.getSeriesRenderingOrder());

        try {
            plot.setDatasetRenderingOrder(null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setSeriesRenderingOrder(null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testMarkersDomainAndRange() {
        XYPlot plot = new XYPlot();
        ValueMarker m1 = new ValueMarker(1.0);
        ValueMarker m2 = new ValueMarker(2.0);
        ValueMarker m3 = new ValueMarker(3.0);
        ValueMarker m4 = new ValueMarker(4.0);

        plot.addDomainMarker(m1);
        plot.addDomainMarker(m2, Layer.BACKGROUND);
        plot.addDomainMarker(1, m3, Layer.FOREGROUND);
        plot.addDomainMarker(1, m4, Layer.BACKGROUND, true);

        Collection fg0 = plot.getDomainMarkers(Layer.FOREGROUND);
        Assert.assertTrue(fg0.contains(m1));
        Collection bg0 = plot.getDomainMarkers(Layer.BACKGROUND);
        Assert.assertTrue(bg0.contains(m2));

        Assert.assertTrue(plot.removeDomainMarker(m1));
        Assert.assertFalse(plot.removeDomainMarker(new ValueMarker(999.0)));
        Assert.assertTrue(plot.removeDomainMarker(m2, Layer.BACKGROUND));
        Assert.assertTrue(plot.removeDomainMarker(1, m3, Layer.FOREGROUND));
        Assert.assertTrue(plot.removeDomainMarker(1, m4, Layer.BACKGROUND, true));

        // Test remove when empty / doesn't exist (D4J Chart-14 bug coverage)
        XYPlot emptyPlot = new XYPlot();
        Assert.assertFalse(emptyPlot.removeDomainMarker(new ValueMarker(1.0)));
        Assert.assertFalse(emptyPlot.removeDomainMarker(new ValueMarker(1.0), Layer.BACKGROUND));
        Assert.assertFalse(emptyPlot.removeDomainMarker(1, new ValueMarker(1.0), Layer.FOREGROUND));
        Assert.assertFalse(emptyPlot.removeRangeMarker(new ValueMarker(1.0)));
        Assert.assertFalse(emptyPlot.removeRangeMarker(new ValueMarker(1.0), Layer.BACKGROUND));
        Assert.assertFalse(emptyPlot.removeRangeMarker(1, new ValueMarker(1.0), Layer.FOREGROUND));

        // Range markers
        plot.addRangeMarker(m1);
        plot.addRangeMarker(m2, Layer.BACKGROUND);
        plot.addRangeMarker(1, m3, Layer.FOREGROUND);
        plot.addRangeMarker(1, m4, Layer.BACKGROUND, true);

        Collection rfg0 = plot.getRangeMarkers(Layer.FOREGROUND);
        Assert.assertTrue(rfg0.contains(m1));
        Collection rbg0 = plot.getRangeMarkers(Layer.BACKGROUND);
        Assert.assertTrue(rbg0.contains(m2));

        Assert.assertTrue(plot.removeRangeMarker(m1));
        Assert.assertFalse(plot.removeRangeMarker(new ValueMarker(999.0)));
        Assert.assertTrue(plot.removeRangeMarker(m2, Layer.BACKGROUND));
        Assert.assertTrue(plot.removeRangeMarker(1, m3, Layer.FOREGROUND));
        Assert.assertTrue(plot.removeRangeMarker(1, m4, Layer.BACKGROUND, true));

        // Clear markers
        plot.addDomainMarker(m1);
        plot.addDomainMarker(1, m2, Layer.FOREGROUND);
        plot.clearDomainMarkers();
        Assert.assertNull(plot.getDomainMarkers(0, Layer.FOREGROUND));
        Assert.assertNull(plot.getDomainMarkers(1, Layer.FOREGROUND));

        plot.addRangeMarker(m1);
        plot.addRangeMarker(1, m2, Layer.FOREGROUND);
        plot.clearRangeMarkers();
        Assert.assertNull(plot.getRangeMarkers(0, Layer.FOREGROUND));
        Assert.assertNull(plot.getRangeMarkers(1, Layer.FOREGROUND));
    }

    @Test
    public void testAnnotations() {
        XYPlot plot = new XYPlot();
        XYTextAnnotation a1 = new XYTextAnnotation("A1", 10.0, 20.0);
        XYTextAnnotation a2 = new XYTextAnnotation("A2", 30.0, 40.0);

        plot.addAnnotation(a1);
        plot.addAnnotation(a2, false);

        List annotations = plot.getAnnotations();
        Assert.assertEquals(2, annotations.size());
        Assert.assertTrue(annotations.contains(a1));
        Assert.assertTrue(annotations.contains(a2));

        Assert.assertTrue(plot.removeAnnotation(a1));
        Assert.assertFalse(plot.removeAnnotation(new XYTextAnnotation("Nonexistent", 0, 0)));
        Assert.assertTrue(plot.removeAnnotation(a2, true));
        Assert.assertEquals(0, plot.getAnnotations().size());

        plot.addAnnotation(a1);
        plot.clearAnnotations();
        Assert.assertEquals(0, plot.getAnnotations().size());
    }

    @Test
    public void testGridlinesAndBaselines() {
        XYPlot plot = new XYPlot();

        plot.setDomainGridlinesVisible(false);
        Assert.assertFalse(plot.isDomainGridlinesVisible());
        Stroke s1 = new BasicStroke(1.5f);
        plot.setDomainGridlineStroke(s1);
        Assert.assertEquals(s1, plot.getDomainGridlineStroke());
        plot.setDomainGridlinePaint(Color.red);
        Assert.assertEquals(Color.red, plot.getDomainGridlinePaint());

        plot.setRangeGridlinesVisible(false);
        Assert.assertFalse(plot.isRangeGridlinesVisible());
        Stroke s2 = new BasicStroke(2.5f);
        plot.setRangeGridlineStroke(s2);
        Assert.assertEquals(s2, plot.getRangeGridlineStroke());
        plot.setRangeGridlinePaint(Color.green);
        Assert.assertEquals(Color.green, plot.getRangeGridlinePaint());

        plot.setDomainZeroBaselineVisible(true);
        Assert.assertTrue(plot.isDomainZeroBaselineVisible());
        Stroke s3 = new BasicStroke(3.5f);
        plot.setDomainZeroBaselineStroke(s3);
        Assert.assertEquals(s3, plot.getDomainZeroBaselineStroke());
        plot.setDomainZeroBaselinePaint(Color.yellow);
        Assert.assertEquals(Color.yellow, plot.getDomainZeroBaselinePaint());

        plot.setRangeZeroBaselineVisible(true);
        Assert.assertTrue(plot.isRangeZeroBaselineVisible());
        Stroke s4 = new BasicStroke(4.5f);
        plot.setRangeZeroBaselineStroke(s4);
        Assert.assertEquals(s4, plot.getRangeZeroBaselineStroke());
        plot.setRangeZeroBaselinePaint(Color.cyan);
        Assert.assertEquals(Color.cyan, plot.getRangeZeroBaselinePaint());
    }

    @Test
    public void testCrosshairs() {
        XYPlot plot = new XYPlot();

        plot.setDomainCrosshairVisible(true);
        Assert.assertTrue(plot.isDomainCrosshairVisible());
        plot.setDomainCrosshairLockedOnData(false);
        Assert.assertFalse(plot.isDomainCrosshairLockedOnData());
        plot.setDomainCrosshairValue(12.34, true);
        Assert.assertEquals(12.34, plot.getDomainCrosshairValue(), 1e-9);
        Stroke s1 = new BasicStroke(1.1f);
        plot.setDomainCrosshairStroke(s1);
        Assert.assertEquals(s1, plot.getDomainCrosshairStroke());
        plot.setDomainCrosshairPaint(Color.magenta);
        Assert.assertEquals(Color.magenta, plot.getDomainCrosshairPaint());

        plot.setRangeCrosshairVisible(true);
        Assert.assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairLockedOnData(false);
        Assert.assertFalse(plot.isRangeCrosshairLockedOnData());
        plot.setRangeCrosshairValue(56.78, true);
        Assert.assertEquals(56.78, plot.getRangeCrosshairValue(), 1e-9);
        Stroke s2 = new BasicStroke(2.2f);
        plot.setRangeCrosshairStroke(s2);
        Assert.assertEquals(s2, plot.getRangeCrosshairStroke());
        plot.setRangeCrosshairPaint(Color.orange);
        Assert.assertEquals(Color.orange, plot.getRangeCrosshairPaint());
    }

    @Test
    public void testQuadrantsAndTickBands() {
        XYPlot plot = new XYPlot();
        Point2D origin = new Point2D.Double(5.0, 10.0);
        plot.setQuadrantOrigin(origin);
        Assert.assertEquals(origin, plot.getQuadrantOrigin());

        plot.setQuadrantPaint(0, Color.red);
        plot.setQuadrantPaint(1, Color.green);
        plot.setQuadrantPaint(2, Color.blue);
        plot.setQuadrantPaint(3, Color.yellow);
        Assert.assertEquals(Color.red, plot.getQuadrantPaint(0));
        Assert.assertEquals(Color.green, plot.getQuadrantPaint(1));
        Assert.assertEquals(Color.blue, plot.getQuadrantPaint(2));
        Assert.assertEquals(Color.yellow, plot.getQuadrantPaint(3));

        try {
            plot.setQuadrantPaint(-1, Color.black);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            plot.setQuadrantPaint(4, Color.black);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setDomainTickBandPaint(Color.lightGray);
        Assert.assertEquals(Color.lightGray, plot.getDomainTickBandPaint());
        plot.setRangeTickBandPaint(Color.darkGray);
        Assert.assertEquals(Color.darkGray, plot.getRangeTickBandPaint());
    }

    @Test
    public void testFixedAxisSpace() {
        XYPlot plot = new XYPlot();
        AxisSpace space1 = new AxisSpace();
        space1.setTop(10.0);
        space1.setBottom(10.0);
        plot.setFixedDomainAxisSpace(space1, true);
        Assert.assertEquals(space1, plot.getFixedDomainAxisSpace());

        AxisSpace space2 = new AxisSpace();
        space2.setLeft(15.0);
        space2.setRight(15.0);
        plot.setFixedRangeAxisSpace(space2, true);
        Assert.assertEquals(space2, plot.getFixedRangeAxisSpace());
    }

    @Test
    public void testZooming() {
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setRange(0.0, 100.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setRange(0.0, 100.0);
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);

        Assert.assertTrue(plot.isDomainZoomable());
        Assert.assertTrue(plot.isRangeZoomable());

        PlotRenderingInfo info = new PlotRenderingInfo(null);
        info.setDataArea(new Rectangle2D.Double(0.0, 0.0, 100.0, 100.0));

        plot.zoomDomainAxes(0.5, info, new Point2D.Double(50.0, 50.0));
        Assert.assertEquals(50.0, xAxis.getRange().getLength(), 1e-6);

        plot.zoomDomainAxes(0.5, info, new Point2D.Double(50.0, 50.0), true);
        plot.zoomDomainAxes(0.2, 0.8, info, new Point2D.Double(50.0, 50.0));

        plot.zoomRangeAxes(0.5, info, new Point2D.Double(50.0, 50.0));
        Assert.assertEquals(50.0, yAxis.getRange().getLength(), 1e-6);

        plot.zoomRangeAxes(0.5, info, new Point2D.Double(50.0, 50.0), true);
        plot.zoomRangeAxes(0.2, 0.8, info, new Point2D.Double(50.0, 50.0));

        // Check horizontal zooming
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.zoomDomainAxes(0.5, info, new Point2D.Double(50.0, 50.0), true);
        plot.zoomRangeAxes(0.5, info, new Point2D.Double(50.0, 50.0), true);
    }

    @Test
    public void testWeightAndFixedLegendItems() {
        XYPlot plot = new XYPlot();
        plot.setWeight(3);
        Assert.assertEquals(3, plot.getWeight());

        LegendItemCollection custom = new LegendItemCollection();
        custom.add(new LegendItem("Custom", "Desc", "ToolTip", "URL", new Rectangle2D.Double(0, 0, 1, 1), Color.red));
        plot.setFixedLegendItems(custom);
        Assert.assertEquals(custom, plot.getFixedLegendItems());
        Assert.assertEquals(custom, plot.getLegendItems());
    }

    @Test
    public void testDrawCoverage() {
        XYSeries series = new XYSeries("Series1");
        series.add(10.0, 20.0);
        series.add(20.0, 30.0);
        series.add(30.0, 15.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        NumberAxis domainAxis = new NumberAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setDomainTickBandPaint(new Color(240, 240, 240));
        plot.setRangeTickBandPaint(new Color(240, 240, 240));
        plot.setQuadrantPaint(0, new Color(255, 0, 0, 50));
        plot.setQuadrantPaint(1, new Color(0, 255, 0, 50));
        plot.setQuadrantPaint(2, new Color(0, 0, 255, 50));
        plot.setQuadrantPaint(3, new Color(255, 255, 0, 50));

        plot.addDomainMarker(new ValueMarker(15.0), Layer.FOREGROUND);
        plot.addDomainMarker(new ValueMarker(25.0), Layer.BACKGROUND);
        plot.addRangeMarker(new ValueMarker(25.0), Layer.FOREGROUND);
        plot.addRangeMarker(new ValueMarker(15.0), Layer.BACKGROUND);
        plot.addAnnotation(new XYTextAnnotation("Note", 20.0, 20.0));

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);

        // Test draw in HORIZONTAL orientation and forward dataset order
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
        BufferedImage imageH = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(imageH);

        // Click handler
        PlotRenderingInfo info = new PlotRenderingInfo(null);
        info.setDataArea(new Rectangle2D.Double(40, 40, 300, 200));
        plot.handleClick(50, 50, info);

        // Draw with small area (should abort early)
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        plot.draw(g2, new Rectangle2D.Double(0, 0, 5, 5), null, null, null);
        g2.dispose();
    }

    @Test
    public void testEventNotifications() {
        XYPlot plot = new XYPlot();
        MyPlotChangeListener listener = new MyPlotChangeListener();
        plot.addChangeListener(listener);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(1, listener.eventCount);

        plot.datasetChanged(new DatasetChangeEvent(this, null));
        Assert.assertEquals(2, listener.eventCount);

        plot.rendererChanged(new RendererChangeEvent(new StandardXYItemRenderer()));
        Assert.assertEquals(3, listener.eventCount);
    }

    @Test
    public void testEqualsAndClone() throws Exception {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 2.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        NumberAxis x = new NumberAxis("X");
        NumberAxis y = new NumberAxis("Y");
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();

        XYPlot plot1 = new XYPlot(dataset, x, y, renderer);
        XYPlot plot2 = new XYPlot(dataset, new NumberAxis("X"), new NumberAxis("Y"), new StandardXYItemRenderer());

        Assert.assertEquals(plot1, plot2);
        Assert.assertEquals(plot1, plot1);
        Assert.assertFalse(plot1.equals("Not a plot"));
        Assert.assertFalse(plot1.equals(null));

        XYPlot clone = (XYPlot) plot1.clone();
        Assert.assertEquals(plot1, clone);
        Assert.assertNotSame(plot1, clone);
        Assert.assertNotSame(plot1.getDomainAxis(), clone.getDomainAxis());
        Assert.assertNotSame(plot1.getRangeAxis(), clone.getRangeAxis());
    }

    @Test
    public void testSerialization() throws Exception {
        XYSeries series = new XYSeries("S");
        series.add(1.0, 2.0);
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        NumberAxis x = new NumberAxis("X");
        NumberAxis y = new NumberAxis("Y");
        StandardXYItemRenderer renderer = new StandardXYItemRenderer();

        XYPlot plot1 = new XYPlot(dataset, x, y, renderer);
        plot1.setDomainZeroBaselineVisible(true);
        plot1.setRangeZeroBaselineVisible(true);
        plot1.setQuadrantPaint(0, Color.red);
        plot1.setDomainTickBandPaint(Color.blue);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(plot1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        XYPlot plot2 = (XYPlot) in.readObject();
        in.close();

        Assert.assertEquals(plot1, plot2);
    }
}