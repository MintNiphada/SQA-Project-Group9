package org.jfree.chart.renderer.category;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.annotations.CategoryTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.IntervalCategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.CustomCategoryURLGenerator;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.util.GradientPaintTransformType;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.LengthAdjustmentType;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.RectangleAnchor;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.StandardGradientPaintTransformer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultKeyedValues2DDataset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractCategoryItemRendererTest {

    private static class TestCategoryItemRenderer extends AbstractCategoryItemRenderer {
        private static final long serialVersionUID = 1L;

        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
                ValueAxis rangeAxis, CategoryDataset dataset, int row,
                int column, int pass) {
        }

        public void drawItem(Graphics2D g2, CategoryItemRendererState state,
                Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
                ValueAxis rangeAxis, CategoryDataset dataset, int row,
                int column, boolean selected, int pass) {
        }
    }

    private TestCategoryItemRenderer renderer;
    private BufferedImage image;
    private Graphics2D g2;

    @Before
    public void setUp() {
        this.renderer = new TestCategoryItemRenderer();
        this.image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        this.g2 = this.image.createGraphics();
    }

    @Test
    public void testGetPassCount() {
        Assert.assertEquals(1, this.renderer.getPassCount());
    }

    @Test
    public void testGetSetPlot() {
        CategoryPlot plot = new CategoryPlot();
        this.renderer.setPlot(plot);
        Assert.assertSame(plot, this.renderer.getPlot());

        try {
            this.renderer.setPlot(null);
            Assert.fail("Expected IllegalArgumentException on null plot");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testItemLabelGenerator() {
        CategoryItemLabelGenerator gen1 = new StandardCategoryItemLabelGenerator();
        CategoryItemLabelGenerator gen2 = new StandardCategoryItemLabelGenerator();

        this.renderer.setBaseItemLabelGenerator(gen1);
        Assert.assertEquals(gen1, this.renderer.getBaseItemLabelGenerator());
        Assert.assertEquals(gen1, this.renderer.getItemLabelGenerator(0, 0, false));

        this.renderer.setSeriesItemLabelGenerator(0, gen2);
        Assert.assertEquals(gen2, this.renderer.getSeriesItemLabelGenerator(0));
        Assert.assertEquals(gen2, this.renderer.getItemLabelGenerator(0, 0, false));
        Assert.assertEquals(gen1, this.renderer.getItemLabelGenerator(1, 0, false));

        this.renderer.setSeriesItemLabelGenerator(0, null, false);
        Assert.assertNull(this.renderer.getSeriesItemLabelGenerator(0));
        Assert.assertEquals(gen1, this.renderer.getItemLabelGenerator(0, 0, true));

        this.renderer.setBaseItemLabelGenerator(null, false);
        Assert.assertNull(this.renderer.getBaseItemLabelGenerator());
        Assert.assertNull(this.renderer.getItemLabelGenerator(0, 0, false));
    }

    @Test
    public void testToolTipGenerator() {
        CategoryToolTipGenerator gen1 = new StandardCategoryToolTipGenerator();
        CategoryToolTipGenerator gen2 = new StandardCategoryToolTipGenerator();

        this.renderer.setBaseToolTipGenerator(gen1);
        Assert.assertEquals(gen1, this.renderer.getBaseToolTipGenerator());
        Assert.assertEquals(gen1, this.renderer.getToolTipGenerator(0, 0, false));

        this.renderer.setSeriesToolTipGenerator(0, gen2);
        Assert.assertEquals(gen2, this.renderer.getSeriesToolTipGenerator(0));
        Assert.assertEquals(gen2, this.renderer.getToolTipGenerator(0, 0, false));
        Assert.assertEquals(gen1, this.renderer.getToolTipGenerator(1, 0, false));

        this.renderer.setSeriesToolTipGenerator(0, null, false);
        Assert.assertNull(this.renderer.getSeriesToolTipGenerator(0));
        Assert.assertEquals(gen1, this.renderer.getToolTipGenerator(0, 0, false));

        this.renderer.setBaseToolTipGenerator(null, false);
        Assert.assertNull(this.renderer.getBaseToolTipGenerator());
        Assert.assertNull(this.renderer.getToolTipGenerator(0, 0, false));
    }

    @Test
    public void testURLGenerator() {
        CategoryURLGenerator gen1 = new StandardCategoryURLGenerator();
        CategoryURLGenerator gen2 = new StandardCategoryURLGenerator();

        this.renderer.setBaseURLGenerator(gen1);
        Assert.assertEquals(gen1, this.renderer.getBaseURLGenerator());
        Assert.assertEquals(gen1, this.renderer.getURLGenerator(0, 0, false));

        this.renderer.setSeriesURLGenerator(0, gen2);
        Assert.assertEquals(gen2, this.renderer.getSeriesURLGenerator(0));
        Assert.assertEquals(gen2, this.renderer.getURLGenerator(0, 0, false));
        Assert.assertEquals(gen1, this.renderer.getURLGenerator(1, 0, false));

        this.renderer.setSeriesURLGenerator(0, null, false);
        Assert.assertNull(this.renderer.getSeriesURLGenerator(0));
        Assert.assertEquals(gen1, this.renderer.getURLGenerator(0, 0, false));

        this.renderer.setBaseURLGenerator(null, false);
        Assert.assertNull(this.renderer.getBaseURLGenerator());
        Assert.assertNull(this.renderer.getURLGenerator(0, 0, false));
    }

    @Test
    public void testLegendGenerators() {
        CategorySeriesLabelGenerator labelGen = new StandardCategorySeriesLabelGenerator("{0}");
        this.renderer.setLegendItemLabelGenerator(labelGen);
        Assert.assertSame(labelGen, this.renderer.getLegendItemLabelGenerator());

        try {
            this.renderer.setLegendItemLabelGenerator(null);
            Assert.fail("Expected IllegalArgumentException on null generator");
        } catch (IllegalArgumentException e) {
            // expected
        }

        CategorySeriesLabelGenerator tipGen = new StandardCategorySeriesLabelGenerator("Tip {0}");
        this.renderer.setLegendItemToolTipGenerator(tipGen);
        Assert.assertSame(tipGen, this.renderer.getLegendItemToolTipGenerator());

        CategorySeriesLabelGenerator urlGen = new StandardCategorySeriesLabelGenerator("URL {0}");
        this.renderer.setLegendItemURLGenerator(urlGen);
        Assert.assertSame(urlGen, this.renderer.getLegendItemURLGenerator());
    }

    @Test
    public void testAnnotations() {
        CategoryAnnotation a1 = new CategoryTextAnnotation("A1", "C1", 10.0);
        CategoryAnnotation a2 = new CategoryTextAnnotation("A2", "C2", 20.0);

        this.renderer.addAnnotation(a1); // Foreground default
        this.renderer.addAnnotation(a2, Layer.BACKGROUND);

        try {
            this.renderer.addAnnotation(null);
            Assert.fail("Expected IllegalArgumentException on null annotation");
        } catch (IllegalArgumentException e) {
            // expected
        }

        boolean removedA1 = this.renderer.removeAnnotation(a1);
        Assert.assertTrue(removedA1);

        this.renderer.removeAnnotations();
        Assert.assertFalse(this.renderer.removeAnnotation(a2));
    }

    @Test
    public void testInitialiseAndState() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis(), new NumberAxis(), this.renderer);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        PlotRenderingInfo info = new PlotRenderingInfo(new ChartRenderingInfo());

        CategoryItemRendererState state = this.renderer.initialise(this.g2, dataArea, plot, dataset, info);
        Assert.assertNotNull(state);
        Assert.assertEquals(2, this.renderer.getRowCount());
        Assert.assertEquals(1, this.renderer.getColumnCount());
        Assert.assertNotNull(state.getVisibleSeriesArray());
        Assert.assertEquals(2, state.getVisibleSeriesArray().length);

        CategoryItemRendererState nullDatasetState = this.renderer.initialise(this.g2, dataArea, plot, null, info);
        Assert.assertNotNull(nullDatasetState);
        Assert.assertEquals(0, this.renderer.getRowCount());
        Assert.assertEquals(0, this.renderer.getColumnCount());
    }

    @Test
    public void testFindRangeBounds() {
        Assert.assertNull(this.renderer.findRangeBounds(null));
        Assert.assertNull(this.renderer.findRangeBounds(new DefaultCategoryDataset()));

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(20.0, "R1", "C2");
        dataset.addValue(-5.0, "R2", "C1");

        Range r = this.renderer.findRangeBounds(dataset);
        Assert.assertNotNull(r);
        Assert.assertEquals(-5.0, r.getLowerBound(), 0.001);
        Assert.assertEquals(20.0, r.getUpperBound(), 0.001);

        this.renderer.setDataBoundsIncludesVisibleSeriesOnly(true);
        this.renderer.setSeriesVisible(1, Boolean.FALSE);
        Range rVisibleOnly = this.renderer.findRangeBounds(dataset);
        Assert.assertNotNull(rVisibleOnly);
        Assert.assertEquals(10.0, rVisibleOnly.getLowerBound(), 0.001);
        Assert.assertEquals(20.0, rVisibleOnly.getUpperBound(), 0.001);
    }

    @Test
    public void testGetItemMiddle() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        CategoryAxis domainAxis = new CategoryAxis("Domain");
        Rectangle2D dataArea = new Rectangle2D.Double(0.0, 0.0, 100.0, 100.0);

        double middle = this.renderer.getItemMiddle("R1", "C1", dataset, domainAxis, dataArea, RectangleEdge.BOTTOM);
        Assert.assertTrue(middle > 0.0);
    }

    @Test
    public void testDrawBackgroundAndOutline() {
        CategoryPlot plot = new CategoryPlot();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);
        this.renderer.drawBackground(this.g2, plot, dataArea);
        this.renderer.drawOutline(this.g2, plot, dataArea);
    }

    @Test
    public void testDrawDomainLine() {
        CategoryPlot plot = new CategoryPlot();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 100, 100);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        this.renderer.drawDomainLine(this.g2, plot, dataArea, 50.0, Color.RED, new BasicStroke(1.0f));

        plot.setOrientation(PlotOrientation.VERTICAL);
        this.renderer.drawDomainLine(this.g2, plot, dataArea, 50.0, Color.BLUE, new BasicStroke(1.0f));

        try {
            this.renderer.drawDomainLine(this.g2, plot, dataArea, 50.0, null, new BasicStroke(1.0f));
            Assert.fail("Expected IllegalArgumentException on null paint");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            this.renderer.drawDomainLine(this.g2, plot, dataArea, 50.0, Color.RED, null);
            Assert.fail("Expected IllegalArgumentException on null stroke");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDrawRangeLine() {
        CategoryPlot plot = new CategoryPlot();
        ValueAxis axis = new NumberAxis();
        axis.setRange(0.0, 100.0);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 100, 100);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        this.renderer.drawRangeLine(this.g2, plot, axis, dataArea, 50.0, Color.RED, new BasicStroke(1.0f));

        plot.setOrientation(PlotOrientation.VERTICAL);
        this.renderer.drawRangeLine(this.g2, plot, axis, dataArea, 50.0, Color.BLUE, new BasicStroke(1.0f));

        // Value out of range -> should return early
        this.renderer.drawRangeLine(this.g2, plot, axis, dataArea, 200.0, Color.BLUE, new BasicStroke(1.0f));
    }

    @Test
    public void testDrawDomainMarker() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis(), new NumberAxis(), this.renderer);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        CategoryMarker marker = new CategoryMarker("C1", Color.GREEN, new BasicStroke(1.0f));
        marker.setLabel("Marker Label");

        // As Area
        marker.setDrawAsLine(false);
        plot.setOrientation(PlotOrientation.VERTICAL);
        this.renderer.drawDomainMarker(this.g2, plot, plot.getDomainAxis(), marker, dataArea);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        this.renderer.drawDomainMarker(this.g2, plot, plot.getDomainAxis(), marker, dataArea);

        // As Line
        marker.setDrawAsLine(true);
        plot.setOrientation(PlotOrientation.VERTICAL);
        this.renderer.drawDomainMarker(this.g2, plot, plot.getDomainAxis(), marker, dataArea);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        this.renderer.drawDomainMarker(this.g2, plot, plot.getDomainAxis(), marker, dataArea);

        // Unknown category
        CategoryMarker unknown = new CategoryMarker("Unknown");
        this.renderer.drawDomainMarker(this.g2, plot, plot.getDomainAxis(), unknown, dataArea);
    }

    @Test
    public void testDrawRangeMarkerValueMarker() {
        CategoryPlot plot = new CategoryPlot();
        ValueAxis axis = new NumberAxis();
        axis.setRange(0.0, 100.0);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        ValueMarker vm = new ValueMarker(50.0, Color.RED, new BasicStroke(1.0f));
        vm.setLabel("VMarker");

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        this.renderer.drawRangeMarker(this.g2, plot, axis, vm, dataArea);

        plot.setOrientation(PlotOrientation.VERTICAL);
        this.renderer.drawRangeMarker(this.g2, plot, axis, vm, dataArea);

        // Outside range
        ValueMarker outMarker = new ValueMarker(150.0);
        this.renderer.drawRangeMarker(this.g2, plot, axis, outMarker, dataArea);
    }

    @Test
    public void testDrawRangeMarkerIntervalMarker() {
        CategoryPlot plot = new CategoryPlot();
        ValueAxis axis = new NumberAxis();
        axis.setRange(0.0, 100.0);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);

        IntervalMarker im = new IntervalMarker(20.0, 80.0, Color.YELLOW, new BasicStroke(1.0f),
                Color.BLACK, new BasicStroke(1.0f), 0.5f);
        im.setLabel("Interval");

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        this.renderer.drawRangeMarker(this.g2, plot, axis, im, dataArea);

        plot.setOrientation(PlotOrientation.VERTICAL);
        this.renderer.drawRangeMarker(this.g2, plot, axis, im, dataArea);

        // Gradient paint
        IntervalMarker imGradient = new IntervalMarker(20.0, 80.0,
                new GradientPaint(0f, 0f, Color.RED, 10f, 10f, Color.BLUE));
        imGradient.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.HORIZONTAL));
        this.renderer.drawRangeMarker(this.g2, plot, axis, imGradient, dataArea);

        // Non-intersecting interval
        IntervalMarker nonIntersect = new IntervalMarker(120.0, 150.0);
        this.renderer.drawRangeMarker(this.g2, plot, axis, nonIntersect, dataArea);
    }

    @Test
    public void testCalculateMarkerTextAnchorPoints() {
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D markerArea = new Rectangle2D.Double(10, 10, 40, 40);
        RectangleInsets insets = new RectangleInsets(2, 2, 2, 2);

        Point2D p1 = this.renderer.calculateDomainMarkerTextAnchorPoint(this.g2, PlotOrientation.HORIZONTAL,
                dataArea, markerArea, insets, LengthAdjustmentType.EXPAND, RectangleAnchor.CENTER);
        Assert.assertNotNull(p1);

        Point2D p2 = this.renderer.calculateDomainMarkerTextAnchorPoint(this.g2, PlotOrientation.VERTICAL,
                dataArea, markerArea, insets, LengthAdjustmentType.EXPAND, RectangleAnchor.CENTER);
        Assert.assertNotNull(p2);

        Point2D p3 = this.renderer.calculateRangeMarkerTextAnchorPoint(this.g2, PlotOrientation.HORIZONTAL,
                dataArea, markerArea, insets, LengthAdjustmentType.EXPAND, RectangleAnchor.CENTER);
        Assert.assertNotNull(p3);

        Point2D p4 = this.renderer.calculateRangeMarkerTextAnchorPoint(this.g2, PlotOrientation.VERTICAL,
                dataArea, markerArea, insets, LengthAdjustmentType.EXPAND, RectangleAnchor.CENTER);
        Assert.assertNotNull(p4);
    }

    @Test
    public void testGetLegendItem() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis(), new NumberAxis(), this.renderer);

        Assert.assertNull(this.renderer.getLegendItem(0, 0)); // Not assigned yet because renderer.getPlot() is used
        this.renderer.setPlot(plot);

        this.renderer.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("Tip {0}"));
        this.renderer.setLegendItemURLGenerator(new StandardCategorySeriesLabelGenerator("URL {0}"));

        LegendItem li = this.renderer.getLegendItem(0, 0);
        Assert.assertNotNull(li);
        Assert.assertEquals("R1", li.getLabel());
        Assert.assertEquals("Tip R1", li.getToolTipText());
        Assert.assertEquals("URL R1", li.getURLText());

        this.renderer.setSeriesVisible(0, Boolean.FALSE);
        Assert.assertNull(this.renderer.getLegendItem(0, 0));

        this.renderer.setSeriesVisible(0, Boolean.TRUE);
        this.renderer.setSeriesVisibleInLegend(0, Boolean.FALSE);
        Assert.assertNull(this.renderer.getLegendItem(0, 0));
    }

    @Test
    public void testGetLegendItems() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R2", "C1");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis(), new NumberAxis(), this.renderer);

        this.renderer.setPlot(plot);
        LegendItemCollection lic = this.renderer.getLegendItems();
        Assert.assertEquals(0, lic.getItemCount());

        this.renderer.setPlot(null);
        Assert.assertEquals(0, this.renderer.getLegendItems().getItemCount());
    }

    @Test
    public void testEqualsAndHashCode() {
        TestCategoryItemRenderer r1 = new TestCategoryItemRenderer();
        TestCategoryItemRenderer r2 = new TestCategoryItemRenderer();

        Assert.assertTrue(r1.equals(r1));
        Assert.assertFalse(r1.equals(null));
        Assert.assertFalse(r1.equals("Not a renderer"));
        Assert.assertTrue(r1.equals(r2));
        Assert.assertEquals(r1.hashCode(), r2.hashCode());

        r1.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        Assert.assertFalse(r1.equals(r2));
        r2.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        Assert.assertTrue(r1.equals(r2));

        r1.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        Assert.assertFalse(r1.equals(r2));
        r2.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        Assert.assertTrue(r1.equals(r2));

        r1.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        Assert.assertFalse(r1.equals(r2));
        r2.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        Assert.assertTrue(r1.equals(r2));

        r1.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
        Assert.assertFalse(r1.equals(r2));
        r2.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
        Assert.assertTrue(r1.equals(r2));

        r1.setBaseURLGenerator(new StandardCategoryURLGenerator());
        Assert.assertFalse(r1.equals(r2));
        r2.setBaseURLGenerator(new StandardCategoryURLGenerator());
        Assert.assertTrue(r1.equals(r2));

        r1.setSeriesURLGenerator(0, new StandardCategoryURLGenerator());
        Assert.assertFalse(r1.equals(r2));
        r2.setSeriesURLGenerator(0, new StandardCategoryURLGenerator());
        Assert.assertTrue(r1.equals(r2));

        r1.setLegendItemLabelGenerator(new StandardCategorySeriesLabelGenerator("Custom {0}"));
        Assert.assertFalse(r1.equals(r2));
        r2.setLegendItemLabelGenerator(new StandardCategorySeriesLabelGenerator("Custom {0}"));
        Assert.assertTrue(r1.equals(r2));

        r1.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("Tip {0}"));
        Assert.assertFalse(r1.equals(r2));
        r2.setLegendItemToolTipGenerator(new StandardCategorySeriesLabelGenerator("Tip {0}"));
        Assert.assertTrue(r1.equals(r2));

        r1.setLegendItemURLGenerator(new StandardCategorySeriesLabelGenerator("URL {0}"));
        Assert.assertFalse(r1.equals(r2));
        r2.setLegendItemURLGenerator(new StandardCategorySeriesLabelGenerator("URL {0}"));
        Assert.assertTrue(r1.equals(r2));

        CategoryTextAnnotation a1 = new CategoryTextAnnotation("A", "C", 1.0);
        r1.addAnnotation(a1);
        Assert.assertFalse(r1.equals(r2));
        r2.addAnnotation(a1);
        Assert.assertTrue(r1.equals(r2));

        CategoryTextAnnotation a2 = new CategoryTextAnnotation("A2", "C", 2.0);
        r1.addAnnotation(a2, Layer.BACKGROUND);
        Assert.assertFalse(r1.equals(r2));
        r2.addAnnotation(a2, Layer.BACKGROUND);
        Assert.assertTrue(r1.equals(r2));
    }

    @Test
    public void testGetDrawingSupplier() {
        Assert.assertNull(this.renderer.getDrawingSupplier());

        CategoryPlot plot = new CategoryPlot();
        DrawingSupplier supplier = new DefaultDrawingSupplier();
        plot.setDrawingSupplier(supplier);
        this.renderer.setPlot(plot);

        Assert.assertSame(supplier, this.renderer.getDrawingSupplier());
    }

    @Test
    public void testUpdateCrosshairValues() {
        CategoryCrosshairState state = new CategoryCrosshairState();
        CategoryPlot plot = new CategoryPlot();
        this.renderer.setPlot(plot);

        plot.setRangeCrosshairLockedOnData(true);
        this.renderer.updateCrosshairValues(state, "R1", "C1", 10.0, 0, 50.0, 60.0, PlotOrientation.VERTICAL);

        plot.setRangeCrosshairLockedOnData(false);
        this.renderer.updateCrosshairValues(state, "R1", "C1", 10.0, 0, 50.0, 60.0, PlotOrientation.HORIZONTAL);

        // Null orientation throws exception
        try {
            this.renderer.updateCrosshairValues(state, "R1", "C1", 10.0, 0, 50.0, 60.0, null);
            Assert.fail("Expected IllegalArgumentException on null orientation");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Null state is a no-op
        this.renderer.updateCrosshairValues(null, "R1", "C1", 10.0, 0, 50.0, 60.0, PlotOrientation.VERTICAL);
    }

    @Test
    public void testDrawItemLabel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");
        dataset.addValue(-5.0, "R1", "C2");

        this.renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        this.renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));
        this.renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER));

        this.renderer.drawItemLabel(this.g2, PlotOrientation.VERTICAL, dataset, 0, 0, false, 50.0, 50.0, false);
        this.renderer.drawItemLabel(this.g2, PlotOrientation.HORIZONTAL, dataset, 0, 1, false, 50.0, 50.0, true);
    }

    @Test
    public void testDrawAnnotations() {
        CategoryPlot plot = new CategoryPlot();
        this.renderer.setPlot(plot);
        CategoryTextAnnotation a1 = new CategoryTextAnnotation("Fg", "C1", 5.0);
        CategoryTextAnnotation a2 = new CategoryTextAnnotation("Bg", "C1", 10.0);
        this.renderer.addAnnotation(a1, Layer.FOREGROUND);
        this.renderer.addAnnotation(a2, Layer.BACKGROUND);

        CategoryAxis domainAxis = new CategoryAxis();
        ValueAxis rangeAxis = new NumberAxis();
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);
        PlotRenderingInfo info = new PlotRenderingInfo(new ChartRenderingInfo());

        this.renderer.drawAnnotations(this.g2, dataArea, domainAxis, rangeAxis, Layer.FOREGROUND, info);
        this.renderer.drawAnnotations(this.g2, dataArea, domainAxis, rangeAxis, Layer.BACKGROUND, info);
    }

    @Test
    public void testCloning() throws Exception {
        this.renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        this.renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
        this.renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        this.renderer.setSeriesToolTipGenerator(0, new StandardCategoryToolTipGenerator());
        this.renderer.setBaseURLGenerator(new StandardCategoryURLGenerator());
        this.renderer.setSeriesURLGenerator(0, new StandardCategoryURLGenerator());

        TestCategoryItemRenderer clone = (TestCategoryItemRenderer) this.renderer.clone();
        Assert.assertNotSame(this.renderer, clone);
        Assert.assertEquals(this.renderer, clone);
    }

    @Test
    public void testGetDomainAndRangeAxis() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        CategoryAxis domainAxis = new CategoryAxis("D");
        ValueAxis rangeAxis = new NumberAxis("R");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, this.renderer);

        Assert.assertSame(domainAxis, this.renderer.getDomainAxis(plot, dataset));
        Assert.assertSame(rangeAxis, this.renderer.getRangeAxis(plot, 0));
        Assert.assertSame(rangeAxis, this.renderer.getRangeAxis(plot, 99));
    }

    @Test
    public void testAddEntity() {
        EntityCollection entities = new StandardEntityCollection();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R1", "C1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis(), new NumberAxis(), this.renderer);
        this.renderer.setPlot(plot);

        this.renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        this.renderer.setBaseURLGenerator(new StandardCategoryURLGenerator());

        Shape hotspot = new Rectangle2D.Double(10, 10, 20, 20);
        this.renderer.addEntity(entities, hotspot, dataset, 0, 0, false);
        Assert.assertEquals(1, entities.getEntityCount());

        // Null hotspot triggers default shape creation
        this.renderer.addEntity(entities, null, dataset, 0, 0, false, 50.0, 50.0);
        Assert.assertEquals(2, entities.getEntityCount());

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        this.renderer.addEntity(entities, null, dataset, 0, 0, false, 50.0, 50.0);
        Assert.assertEquals(3, entities.getEntityCount());

        this.renderer.setBaseCreateEntities(false);
        this.renderer.addEntity(entities, hotspot, dataset, 0, 0, false);
        Assert.assertEquals(3, entities.getEntityCount());
    }

    @Test
    public void testCreateHotSpotBoundsAndHitTest() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(50.0, "R1", "C1");
        CategoryAxis domainAxis = new CategoryAxis();
        ValueAxis rangeAxis = new NumberAxis();
        rangeAxis.setRange(0.0, 100.0);
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, this.renderer);
        this.renderer.setPlot(plot);

        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 100, 100);
        CategoryItemRendererState state = this.renderer.initialise(this.g2, dataArea, plot, dataset, null);

        Rectangle2D bounds = this.renderer.createHotSpotBounds(this.g2, dataArea, plot, domainAxis,
                rangeAxis, dataset, 0, 0, false, state, null);
        Assert.assertNotNull(bounds);

        boolean hit = this.renderer.hitTest(bounds.getCenterX(), bounds.getCenterY(), this.g2, dataArea,
                plot, domainAxis, rangeAxis, dataset, 0, 0, false, state);
        Assert.assertTrue(hit);

        boolean miss = this.renderer.hitTest(-100.0, -100.0, this.g2, dataArea,
                plot, domainAxis, rangeAxis, dataset, 0, 0, false, state);
        Assert.assertFalse(miss);

        try {
            this.renderer.createHotSpotShape(this.g2, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, false, state);
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testSerialization() throws Exception {
        this.renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        this.renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        this.renderer.setBaseURLGenerator(new StandardCategoryURLGenerator());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(this.renderer);

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        TestCategoryItemRenderer r2 = (TestCategoryItemRenderer) in.readObject();

        Assert.assertEquals(this.renderer, r2);
    }
}