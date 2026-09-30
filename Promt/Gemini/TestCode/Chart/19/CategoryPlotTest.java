package org.jfree.chart.plot;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
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
import org.junit.Test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
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

public class CategoryPlotTest {

    private static class TestPlotListener implements PlotChangeListener {
        private boolean notified = false;
        public void plotChanged(PlotChangeEvent event) {
            this.notified = true;
        }
        public boolean isNotified() {
            return this.notified;
        }
        public void reset() {
            this.notified = false;
        }
    }

    @Test
    public void testDefaultConstructor() {
        CategoryPlot plot = new CategoryPlot();
        Assert.assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        Assert.assertNull(plot.getDataset());
        Assert.assertNull(plot.getDomainAxis());
        Assert.assertNull(plot.getRangeAxis());
        Assert.assertNull(plot.getRenderer());
        Assert.assertEquals(DatasetRenderingOrder.REVERSE, plot.getDatasetRenderingOrder());
        Assert.assertEquals(SortOrder.ASCENDING, plot.getColumnRenderingOrder());
        Assert.assertEquals(SortOrder.ASCENDING, plot.getRowRenderingOrder());
        Assert.assertFalse(plot.isDomainGridlinesVisible());
        Assert.assertTrue(plot.isRangeGridlinesVisible());
        Assert.assertFalse(plot.isRangeCrosshairVisible());
        Assert.assertTrue(plot.isRangeCrosshairLockedOnData());
        Assert.assertFalse(plot.isDomainZoomable());
        Assert.assertTrue(plot.isRangeZoomable());
        Assert.assertEquals("Category Plot", plot.getPlotType());
    }

    @Test
    public void testConstructorWithArguments() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();

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
    public void testSetOrientation() {
        CategoryPlot plot = new CategoryPlot();
        TestPlotListener listener = new TestPlotListener();
        plot.addChangeListener(listener);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
        Assert.assertTrue(listener.isNotified());

        try {
            plot.setOrientation(null);
            Assert.fail("Null orientation should throw exception.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetAxisOffset() {
        CategoryPlot plot = new CategoryPlot();
        TestPlotListener listener = new TestPlotListener();
        plot.addChangeListener(listener);

        RectangleInsets insets = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
        plot.setAxisOffset(insets);
        Assert.assertEquals(insets, plot.getAxisOffset());
        Assert.assertTrue(listener.isNotified());

        try {
            plot.setAxisOffset(null);
            Assert.fail("Null axisOffset should throw exception.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDomainAxesManagement() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis axis1 = new CategoryAxis("Axis 1");
        CategoryAxis axis2 = new CategoryAxis("Axis 2");

        plot.setDomainAxis(0, axis1);
        plot.setDomainAxis(1, axis2);

        Assert.assertEquals(2, plot.getDomainAxisCount());
        Assert.assertSame(axis1, plot.getDomainAxis(0));
        Assert.assertSame(axis2, plot.getDomainAxis(1));
        Assert.assertEquals(0, plot.getDomainAxisIndex(axis1));
        Assert.assertEquals(1, plot.getDomainAxisIndex(axis2));
        Assert.assertEquals(-1, plot.getDomainAxisIndex(new CategoryAxis("Other")));

        CategoryAxis[] axes = new CategoryAxis[] { new CategoryAxis("A"), new CategoryAxis("B") };
        plot.setDomainAxes(axes);
        Assert.assertEquals(2, plot.getDomainAxisCount());
        Assert.assertEquals("A", plot.getDomainAxis(0).getLabel());
        Assert.assertEquals("B", plot.getDomainAxis(1).getLabel());

        plot.clearDomainAxes();
        Assert.assertEquals(0, plot.getDomainAxisCount());
        Assert.assertNull(plot.getDomainAxis());
    }

    @Test
    public void testDomainAxisLocations() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_RIGHT);
        Assert.assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation());
        Assert.assertEquals(RectangleEdge.TOP, plot.getDomainAxisEdge());

        plot.setDomainAxisLocation(1, AxisLocation.BOTTOM_OR_LEFT, true);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation(1));

        try {
            plot.setDomainAxisLocation(0, null, true);
            Assert.fail("Null location for index 0 should fail.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRangeAxesManagement() {
        CategoryPlot plot = new CategoryPlot();
        NumberAxis axis1 = new NumberAxis("Range 1");
        NumberAxis axis2 = new NumberAxis("Range 2");

        plot.setRangeAxis(0, axis1);
        plot.setRangeAxis(1, axis2);

        Assert.assertEquals(2, plot.getRangeAxisCount());
        Assert.assertSame(axis1, plot.getRangeAxis(0));
        Assert.assertSame(axis2, plot.getRangeAxis(1));
        Assert.assertEquals(0, plot.getRangeAxisIndex(axis1));
        Assert.assertEquals(1, plot.getRangeAxisIndex(axis2));
        Assert.assertEquals(-1, plot.getRangeAxisIndex(new NumberAxis("Other")));

        ValueAxis[] axes = new ValueAxis[] { new NumberAxis("A"), new NumberAxis("B") };
        plot.setRangeAxes(axes);
        Assert.assertEquals(2, plot.getRangeAxisCount());
        Assert.assertEquals("A", plot.getRangeAxis(0).getLabel());
        Assert.assertEquals("B", plot.getRangeAxis(1).getLabel());

        plot.clearRangeAxes();
        Assert.assertEquals(0, plot.getRangeAxisCount());
        Assert.assertNull(plot.getRangeAxis());
    }

    @Test
    public void testRangeAxisLocations() {
        CategoryPlot plot = new CategoryPlot();
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
        Assert.assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation());
        Assert.assertEquals(RectangleEdge.RIGHT, plot.getRangeAxisEdge());

        plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT, true);
        Assert.assertEquals(AxisLocation.TOP_OR_LEFT, plot.getRangeAxisLocation(1));

        try {
            plot.setRangeAxisLocation(0, null, true);
            Assert.fail("Null location for index 0 should fail.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDatasetsAndRenderers() {
        CategoryPlot plot = new CategoryPlot();
        DefaultCategoryDataset ds0 = new DefaultCategoryDataset();
        DefaultCategoryDataset ds1 = new DefaultCategoryDataset();
        BarRenderer r0 = new BarRenderer();
        LineAndShapeRenderer r1 = new LineAndShapeRenderer();

        plot.setDataset(0, ds0);
        plot.setDataset(1, ds1);
        plot.setRenderer(0, r0);
        plot.setRenderer(1, r1);

        Assert.assertEquals(2, plot.getDatasetCount());
        Assert.assertSame(ds0, plot.getDataset(0));
        Assert.assertSame(ds1, plot.getDataset(1));
        Assert.assertSame(r0, plot.getRenderer(0));
        Assert.assertSame(r1, plot.getRenderer(1));

        Assert.assertSame(r0, plot.getRendererForDataset(ds0));
        Assert.assertSame(r1, plot.getRendererForDataset(ds1));
        Assert.assertNull(plot.getRendererForDataset(new DefaultCategoryDataset()));

        Assert.assertEquals(0, plot.getIndexOf(r0));
        Assert.assertEquals(1, plot.getIndexOf(r1));
        Assert.assertEquals(-1, plot.getIndexOf(new BarRenderer()));

        CategoryItemRenderer[] renderers = new CategoryItemRenderer[] { new BarRenderer(), new LineAndShapeRenderer() };
        plot.setRenderers(renderers);
        Assert.assertSame(renderers[0], plot.getRenderer(0));
        Assert.assertSame(renderers[1], plot.getRenderer(1));
    }

    @Test
    public void testDatasetAxisMapping() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAxis c0 = new CategoryAxis("C0");
        CategoryAxis c1 = new CategoryAxis("C1");
        NumberAxis r0 = new NumberAxis("R0");
        NumberAxis r1 = new NumberAxis("R1");

        plot.setDomainAxis(0, c0);
        plot.setDomainAxis(1, c1);
        plot.setRangeAxis(0, r0);
        plot.setRangeAxis(1, r1);

        DefaultCategoryDataset ds0 = new DefaultCategoryDataset();
        DefaultCategoryDataset ds1 = new DefaultCategoryDataset();
        plot.setDataset(0, ds0);
        plot.setDataset(1, ds1);

        plot.mapDatasetToDomainAxis(0, 0);
        plot.mapDatasetToDomainAxis(1, 1);
        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);

        Assert.assertSame(c0, plot.getDomainAxisForDataset(0));
        Assert.assertSame(c1, plot.getDomainAxisForDataset(1));
        Assert.assertSame(r0, plot.getRangeAxisForDataset(0));
        Assert.assertSame(r1, plot.getRangeAxisForDataset(1));
    }

    @Test
    public void testRenderingOrders() {
        CategoryPlot plot = new CategoryPlot();
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        Assert.assertEquals(DatasetRenderingOrder.FORWARD, plot.getDatasetRenderingOrder());
        try {
            plot.setDatasetRenderingOrder(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        Assert.assertEquals(SortOrder.DESCENDING, plot.getColumnRenderingOrder());
        try {
            plot.setColumnRenderingOrder(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        Assert.assertEquals(SortOrder.DESCENDING, plot.getRowRenderingOrder());
        try {
            plot.setRowRenderingOrder(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGridlineProperties() {
        CategoryPlot plot = new CategoryPlot();

        plot.setDomainGridlinesVisible(true);
        Assert.assertTrue(plot.isDomainGridlinesVisible());

        plot.setDomainGridlinePosition(CategoryAnchor.START);
        Assert.assertEquals(CategoryAnchor.START, plot.getDomainGridlinePosition());
        try {
            plot.setDomainGridlinePosition(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        Stroke stroke = new BasicStroke(2.0f);
        plot.setDomainGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getDomainGridlineStroke());
        try {
            plot.setDomainGridlineStroke(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setDomainGridlinePaint(Color.RED);
        Assert.assertEquals(Color.RED, plot.getDomainGridlinePaint());
        try {
            plot.setDomainGridlinePaint(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setRangeGridlinesVisible(false);
        Assert.assertFalse(plot.isRangeGridlinesVisible());

        plot.setRangeGridlineStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeGridlineStroke());
        try {
            plot.setRangeGridlineStroke(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setRangeGridlinePaint(Color.BLUE);
        Assert.assertEquals(Color.BLUE, plot.getRangeGridlinePaint());
        try {
            plot.setRangeGridlinePaint(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCrosshairProperties() {
        CategoryPlot plot = new CategoryPlot();

        plot.setRangeCrosshairVisible(true);
        Assert.assertTrue(plot.isRangeCrosshairVisible());

        plot.setRangeCrosshairValue(10.5);
        Assert.assertEquals(10.5, plot.getRangeCrosshairValue(), 1e-9);

        plot.setRangeCrosshairLockedOnData(false);
        Assert.assertFalse(plot.isRangeCrosshairLockedOnData());

        Stroke stroke = new BasicStroke(1.5f);
        plot.setRangeCrosshairStroke(stroke);
        Assert.assertEquals(stroke, plot.getRangeCrosshairStroke());
        try {
            plot.setRangeCrosshairStroke(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

        plot.setRangeCrosshairPaint(Color.GREEN);
        Assert.assertEquals(Color.GREEN, plot.getRangeCrosshairPaint());
        try {
            plot.setRangeCrosshairPaint(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDomainAndRangeMarkers() {
        CategoryPlot plot = new CategoryPlot();
        CategoryMarker cm1 = new CategoryMarker("C1");
        CategoryMarker cm2 = new CategoryMarker("C2");

        plot.addDomainMarker(cm1);
        plot.addDomainMarker(cm2, Layer.BACKGROUND);

        Collection fgDm = plot.getDomainMarkers(Layer.FOREGROUND);
        Collection bgDm = plot.getDomainMarkers(Layer.BACKGROUND);
        Assert.assertTrue(fgDm.contains(cm1));
        Assert.assertTrue(bgDm.contains(cm2));

        plot.clearDomainMarkers(0);
        Assert.assertNull(plot.getDomainMarkers(0, Layer.FOREGROUND));
        Assert.assertNull(plot.getDomainMarkers(0, Layer.BACKGROUND));

        plot.addDomainMarker(cm1);
        plot.clearDomainMarkers();
        Assert.assertNull(plot.getDomainMarkers(Layer.FOREGROUND));

        ValueMarker vm1 = new ValueMarker(1.0);
        ValueMarker vm2 = new ValueMarker(2.0);
        plot.addRangeMarker(vm1);
        plot.addRangeMarker(1, vm2, Layer.BACKGROUND);

        Collection fgRm = plot.getRangeMarkers(Layer.FOREGROUND);
        Collection bgRm = plot.getRangeMarkers(1, Layer.BACKGROUND);
        Assert.assertTrue(fgRm.contains(vm1));
        Assert.assertTrue(bgRm.contains(vm2));

        plot.clearRangeMarkers(1);
        Assert.assertNull(plot.getRangeMarkers(1, Layer.BACKGROUND));
        plot.clearRangeMarkers();
        Assert.assertNull(plot.getRangeMarkers(Layer.FOREGROUND));
    }

    @Test
    public void testAnnotations() {
        CategoryPlot plot = new CategoryPlot();
        CategoryAnnotation a1 = new CategoryTextAnnotation("A1", "C1", 10.0);
        CategoryAnnotation a2 = new CategoryTextAnnotation("A2", "C2", 20.0);

        plot.addAnnotation(a1);
        plot.addAnnotation(a2);

        List annotations = plot.getAnnotations();
        Assert.assertEquals(2, annotations.size());
        Assert.assertTrue(annotations.contains(a1));
        Assert.assertTrue(annotations.contains(a2));

        plot.removeAnnotation(a1);
        Assert.assertEquals(1, plot.getAnnotations().size());
        Assert.assertFalse(plot.getAnnotations().contains(a1));

        plot.clearAnnotations();
        Assert.assertEquals(0, plot.getAnnotations().size());
    }

    @Test
    public void testGetCategoriesAndCategoriesForAxis() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(1.0, "R1", "Cat1");
        ds.addValue(2.0, "R1", "Cat2");
        CategoryAxis axis = new CategoryAxis("Categories");

        CategoryPlot plot = new CategoryPlot(ds, axis, new NumberAxis("Range"), new BarRenderer());
        List categories = plot.getCategories();
        Assert.assertNotNull(categories);
        Assert.assertEquals(2, categories.size());
        Assert.assertEquals("Cat1", categories.get(0));
        Assert.assertEquals("Cat2", categories.get(1));

        List catsForAxis = plot.getCategoriesForAxis(axis);
        Assert.assertEquals(2, catsForAxis.size());
        Assert.assertTrue(catsForAxis.contains("Cat1"));
        Assert.assertTrue(catsForAxis.contains("Cat2"));
    }

    @Test
    public void testGetDataRange() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(10.0, "R1", "C1");
        ds.addValue(50.0, "R1", "C2");
        NumberAxis rangeAxis = new NumberAxis("Range");
        CategoryPlot plot = new CategoryPlot(ds, new CategoryAxis("Domain"), rangeAxis, new BarRenderer());

        Range range = plot.getDataRange(rangeAxis);
        Assert.assertNotNull(range);
        Assert.assertEquals(0.0, range.getLowerBound(), 1e-9); // BarRenderer includes zero
        Assert.assertEquals(50.0, range.getUpperBound(), 1e-9);

        Assert.assertNull(plot.getDataRange(new NumberAxis("Unrelated")));
    }

    @Test
    public void testZoomAndAnchor() {
        NumberAxis rangeAxis = new NumberAxis("Range");
        rangeAxis.setRange(0.0, 100.0);
        CategoryPlot plot = new CategoryPlot(null, null, rangeAxis, null);

        plot.setAnchorValue(50.0);
        Assert.assertEquals(50.0, plot.getAnchorValue(), 1e-9);

        plot.zoom(0.5);
        Assert.assertEquals(25.0, rangeAxis.getLowerBound(), 1e-9);
        Assert.assertEquals(75.0, rangeAxis.getUpperBound(), 1e-9);

        plot.zoom(0.0);
        Assert.assertTrue(rangeAxis.isAutoRange());

        plot.zoomRangeAxes(2.0, new PlotRenderingInfo(null), new Point2D.Double(0, 0));
        plot.zoomRangeAxes(0.2, 0.8, new PlotRenderingInfo(null), new Point2D.Double(0, 0));
        plot.zoomDomainAxes(0.5, new PlotRenderingInfo(null), new Point2D.Double(0, 0));
        plot.zoomDomainAxes(0.2, 0.8, new PlotRenderingInfo(null), new Point2D.Double(0, 0));
    }

    @Test
    public void testFixedAxisSpaceAndWeight() {
        CategoryPlot plot = new CategoryPlot();
        plot.setWeight(5);
        Assert.assertEquals(5, plot.getWeight());

        AxisSpace domainSpace = new AxisSpace();
        domainSpace.setTop(10.0);
        domainSpace.setBottom(10.0);
        plot.setFixedDomainAxisSpace(domainSpace);
        Assert.assertSame(domainSpace, plot.getFixedDomainAxisSpace());

        AxisSpace rangeSpace = new AxisSpace();
        rangeSpace.setLeft(15.0);
        rangeSpace.setRight(15.0);
        plot.setFixedRangeAxisSpace(rangeSpace);
        Assert.assertSame(rangeSpace, plot.getFixedRangeAxisSpace());
    }

    @Test
    public void testLegendItems() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(1.0, "Series 1", "Type A");
        CategoryPlot plot = new CategoryPlot(ds, new CategoryAxis("X"), new NumberAxis("Y"), new BarRenderer());

        LegendItemCollection lic = plot.getLegendItems();
        Assert.assertEquals(1, lic.get(0).getSeriesIndex());

        LegendItemCollection customLic = new LegendItemCollection();
        customLic.add(new LegendItem("Custom", "Description", "Tooltip", "URL", new Rectangle(10, 10), Color.RED));
        plot.setFixedLegendItems(customLic);
        Assert.assertSame(customLic, plot.getFixedLegendItems());
        Assert.assertSame(customLic, plot.getLegendItems());
    }

    @Test
    public void testCloningAndSerialization() throws Exception {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        ds.addValue(1.0, "R1", "C1");
        CategoryPlot plot1 = new CategoryPlot(ds, new CategoryAxis("Domain"), new NumberAxis("Range"), new BarRenderer());
        plot1.addAnnotation(new CategoryTextAnnotation("Annot", "C1", 1.0));
        plot1.addDomainMarker(new CategoryMarker("C1"));
        plot1.addRangeMarker(new ValueMarker(1.0));

        CategoryPlot plot2 = (CategoryPlot) plot1.clone();
        Assert.assertNotSame(plot1, plot2);
        Assert.assertEquals(plot1, plot2);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(plot1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        CategoryPlot plot3 = (CategoryPlot) in.readObject();
        in.close();

        Assert.assertEquals(plot1, plot3);
    }

    @Test
    public void testEqualsAndHashCode() {
        CategoryPlot plot1 = new CategoryPlot();
        CategoryPlot plot2 = new CategoryPlot();
        Assert.assertEquals(plot1, plot2);

        plot1.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertFalse(plot1.equals(plot2));
        plot2.setOrientation(PlotOrientation.HORIZONTAL);
        Assert.assertEquals(plot1, plot2);

        plot1.setDomainGridlinesVisible(true);
        Assert.assertFalse(plot1.equals(plot2));
        plot2.setDomainGridlinesVisible(true);
        Assert.assertEquals(plot1, plot2);

        plot1.setRangeCrosshairValue(55.0);
        Assert.assertFalse(plot1.equals(plot2));
        plot2.setRangeCrosshairValue(55.0);
        Assert.assertEquals(plot1, plot2);
    }

    @Test
    public void testDrawRendering() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "S1", "C1");
        dataset.addValue(20.0, "S1", "C2");
        dataset.addValue(15.0, "S2", "C1");
        dataset.addValue(25.0, "S2", "C2");

        CategoryAxis domainAxis = new CategoryAxis("Domain");
        NumberAxis rangeAxis = new NumberAxis("Range");
        BarRenderer renderer = new BarRenderer();

        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairValue(15.0);
        plot.addDomainMarker(new CategoryMarker("C1"));
        plot.addRangeMarker(new ValueMarker(12.0));
        plot.addAnnotation(new CategoryTextAnnotation("Note", "C1", 10.0));

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = chart.createBufferedImage(400, 300, new ChartRenderingInfo());
        Assert.assertNotNull(image);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        plot.setRowRenderingOrder(SortOrder.DESCENDING);

        BufferedImage imageH = chart.createBufferedImage(400, 300, new ChartRenderingInfo());
        Assert.assertNotNull(imageH);
    }

    @Test
    public void testHandleClick() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "S1", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Domain"), new NumberAxis("Range"), new BarRenderer());
        
        JFreeChart chart = new JFreeChart(plot);
        ChartRenderingInfo info = new ChartRenderingInfo();
        chart.createBufferedImage(400, 300, info);

        Rectangle2D dataArea = info.getPlotInfo().getDataArea();
        int clickX = (int) (dataArea.getX() + dataArea.getWidth() / 2);
        int clickY = (int) (dataArea.getY() + dataArea.getHeight() / 2);

        plot.handleClick(clickX, clickY, info.getPlotInfo());
        Assert.assertTrue(plot.getAnchorValue() > 0.0);
    }

    @Test
    public void testEvents() {
        CategoryPlot plot = new CategoryPlot();
        TestPlotListener listener = new TestPlotListener();
        plot.addChangeListener(listener);

        plot.datasetChanged(new DatasetChangeEvent(this, new DefaultCategoryDataset()));
        Assert.assertTrue(listener.isNotified());

        listener.reset();
        plot.rendererChanged(new RendererChangeEvent(new BarRenderer()));
        Assert.assertTrue(listener.isNotified());
    }
}