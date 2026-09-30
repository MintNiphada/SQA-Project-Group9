package org.jfree.chart.plot;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link MultiplePiePlot} class.
 */
public class MultiplePiePlotTest {

    private DefaultCategoryDataset dataset;

    @Before
    public void setUp() {
        this.dataset = new DefaultCategoryDataset();
        this.dataset.addValue(10.0, "Row 1", "Col 1");
        this.dataset.addValue(20.0, "Row 1", "Col 2");
        this.dataset.addValue(30.0, "Row 1", "Col 3");
        this.dataset.addValue(40.0, "Row 2", "Col 1");
        this.dataset.addValue(50.0, "Row 2", "Col 2");
        this.dataset.addValue(60.0, "Row 2", "Col 3");
    }

    @Test
    public void testDefaultConstructor() {
        MultiplePiePlot plot = new MultiplePiePlot();
        Assert.assertNull(plot.getDataset());
        Assert.assertNotNull(plot.getPieChart());
        Assert.assertTrue(plot.getPieChart().getPlot() instanceof PiePlot);
        Assert.assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
        Assert.assertEquals(0.0, plot.getLimit(), 0.000001);
        Assert.assertEquals("Other", plot.getAggregatedItemsKey());
        Assert.assertEquals(Color.lightGray, plot.getAggregatedItemsPaint());
        Assert.assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    @Test
    public void testDatasetConstructor() {
        MultiplePiePlot plot = new MultiplePiePlot(this.dataset);
        Assert.assertSame(this.dataset, plot.getDataset());
        Assert.assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
    }

    @Test
    public void testSetDataset() {
        MultiplePiePlot plot = new MultiplePiePlot();
        final AtomicInteger changeCount = new AtomicInteger(0);
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                changeCount.incrementAndGet();
            }
        });

        plot.setDataset(this.dataset);
        Assert.assertSame(this.dataset, plot.getDataset());
        Assert.assertTrue(changeCount.get() > 0);

        // Modify dataset to verify listener registration
        int countBefore = changeCount.get();
        this.dataset.addValue(70.0, "Row 3", "Col 1");
        Assert.assertTrue(changeCount.get() > countBefore);

        // Setting dataset to null should remove listener
        plot.setDataset(null);
        Assert.assertNull(plot.getDataset());
        countBefore = changeCount.get();
        this.dataset.addValue(80.0, "Row 3", "Col 2");
        Assert.assertEquals(countBefore, changeCount.get());
    }

    @Test
    public void testSetPieChart() {
        MultiplePiePlot plot = new MultiplePiePlot();
        final AtomicInteger changeCount = new AtomicInteger(0);
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                changeCount.incrementAndGet();
            }
        });

        PiePlot newPiePlot = new PiePlot();
        JFreeChart newChart = new JFreeChart("Custom", newPiePlot);
        plot.setPieChart(newChart);
        Assert.assertSame(newChart, plot.getPieChart());
        Assert.assertEquals(1, changeCount.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPieChartNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setPieChart(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPieChartInvalidPlot() {
        MultiplePiePlot plot = new MultiplePiePlot();
        FastScatterPlot scatterPlot = new FastScatterPlot();
        JFreeChart invalidChart = new JFreeChart("Invalid", scatterPlot);
        plot.setPieChart(invalidChart);
    }

    @Test
    public void testSetDataExtractOrder() {
        MultiplePiePlot plot = new MultiplePiePlot();
        final AtomicInteger changeCount = new AtomicInteger(0);
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                changeCount.incrementAndGet();
            }
        });

        plot.setDataExtractOrder(TableOrder.BY_ROW);
        Assert.assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
        Assert.assertEquals(1, changeCount.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDataExtractOrderNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setDataExtractOrder(null);
    }

    @Test
    public void testSetLimit() {
        MultiplePiePlot plot = new MultiplePiePlot();
        final AtomicInteger changeCount = new AtomicInteger(0);
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                changeCount.incrementAndGet();
            }
        });

        plot.setLimit(0.15);
        Assert.assertEquals(0.15, plot.getLimit(), 0.000001);
        Assert.assertEquals(1, changeCount.get());
    }

    @Test
    public void testSetAggregatedItemsKey() {
        MultiplePiePlot plot = new MultiplePiePlot();
        final AtomicInteger changeCount = new AtomicInteger(0);
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                changeCount.incrementAndGet();
            }
        });

        plot.setAggregatedItemsKey("Miscellaneous");
        Assert.assertEquals("Miscellaneous", plot.getAggregatedItemsKey());
        Assert.assertEquals(1, changeCount.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsKeyNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsKey(null);
    }

    @Test
    public void testSetAggregatedItemsPaint() {
        MultiplePiePlot plot = new MultiplePiePlot();
        final AtomicInteger changeCount = new AtomicInteger(0);
        plot.addChangeListener(new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent event) {
                changeCount.incrementAndGet();
            }
        });

        Color newColor = Color.magenta;
        plot.setAggregatedItemsPaint(newColor);
        Assert.assertEquals(newColor, plot.getAggregatedItemsPaint());
        Assert.assertEquals(1, changeCount.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsPaintNull() {
        MultiplePiePlot plot = new MultiplePiePlot();
        plot.setAggregatedItemsPaint(null);
    }

    @Test
    public void testDrawNullOrEmptyDataset() {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 400, 300);

        // Null dataset
        MultiplePiePlot plot1 = new MultiplePiePlot(null);
        plot1.draw(g2, area, new Point2D.Double(10, 10), null, null);

        // Empty dataset
        MultiplePiePlot plot2 = new MultiplePiePlot(new DefaultCategoryDataset());
        plot2.draw(g2, area, null, null, null);

        g2.dispose();
    }

    @Test
    public void testDrawByColumnAndByRow() {
        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 600, 400);

        // Draw BY_COLUMN with limit = 0.0
        MultiplePiePlot plotByCol = new MultiplePiePlot(this.dataset);
        plotByCol.setDataExtractOrder(TableOrder.BY_COLUMN);
        plotByCol.setLimit(0.0);

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        plotByCol.draw(g2, area, new Point2D.Double(0, 0), null, info.getPlotInfo());
        Assert.assertFalse(info.getEntityCollection().getEntities().isEmpty());

        // Draw BY_ROW with limit > 0.0
        MultiplePiePlot plotByRow = new MultiplePiePlot(this.dataset);
        plotByRow.setDataExtractOrder(TableOrder.BY_ROW);
        plotByRow.setLimit(0.25);
        plotByRow.draw(g2, area, null, null, null);

        g2.dispose();
    }

    @Test
    public void testDrawWithDimensionSwapping() {
        // Create an area where width < height to trigger row/column swapping
        BufferedImage image = new BufferedImage(200, 600, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 600);

        MultiplePiePlot plot = new MultiplePiePlot(this.dataset);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN); // 3 columns -> pieCount = 3
        plot.draw(g2, area, null, null, null);

        g2.dispose();
    }

    @Test
    public void testDrawWithUnevenGridOffset() {
        // 5 columns -> displayCols = 3, displayRows = 2, diff = (6 - 5) = 1 (triggers xoffset calculation)
        DefaultCategoryDataset ds5 = new DefaultCategoryDataset();
        for (int i = 1; i <= 5; i++) {
            ds5.addValue(10, "R1", "C" + i);
            ds5.addValue(20, "R2", "C" + i);
        }

        BufferedImage image = new BufferedImage(600, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 600, 400);

        MultiplePiePlot plot = new MultiplePiePlot(ds5);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        plot.draw(g2, area, null, null, null);

        g2.dispose();
    }

    @Test
    public void testPrefetchSectionPaintsCustomColor() {
        MultiplePiePlot plot = new MultiplePiePlot(this.dataset);
        PiePlot subPiePlot = (PiePlot) plot.getPieChart().getPlot();
        subPiePlot.setSectionPaint("Row 1", Color.red);

        // Extract BY_COLUMN uses row keys
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        LegendItemCollection items1 = plot.getLegendItems();
        Assert.assertEquals(2, items1.getItemCount());
        Assert.assertEquals(Color.red, items1.get(0).getFillPaint());

        // Extract BY_ROW uses column keys
        subPiePlot.setSectionPaint("Col 1", Color.blue);
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        LegendItemCollection items2 = plot.getLegendItems();
        Assert.assertEquals(3, items2.getItemCount());
        Assert.assertEquals(Color.blue, items2.get(0).getFillPaint());
    }

    @Test
    public void testGetLegendItems() {
        MultiplePiePlot plot = new MultiplePiePlot(null);
        LegendItemCollection emptyItems = plot.getLegendItems();
        Assert.assertEquals(0, emptyItems.getItemCount());

        plot.setDataset(this.dataset);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        plot.setLimit(0.0);

        LegendItemCollection items = plot.getLegendItems();
        Assert.assertEquals(2, items.getItemCount());
        Assert.assertEquals("Row 1", items.get(0).getLabel());
        Assert.assertEquals("Row 2", items.get(1).getLabel());
        Assert.assertSame(this.dataset, items.get(0).getDataset());

        // Test with limit > 0.0 (includes aggregated items legend)
        plot.setLimit(0.5);
        LegendItemCollection itemsWithLimit = plot.getLegendItems();
        Assert.assertEquals(3, itemsWithLimit.getItemCount());
        Assert.assertEquals("Other", itemsWithLimit.get(2).getLabel());
        Assert.assertEquals(plot.getAggregatedItemsPaint(), itemsWithLimit.get(2).getFillPaint());

        // Test with BY_ROW
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        plot.setLimit(0.0);
        LegendItemCollection rowItems = plot.getLegendItems();
        Assert.assertEquals(3, rowItems.getItemCount());
        Assert.assertEquals("Col 1", rowItems.get(0).getLabel());
    }

    @Test
    public void testEquals() {
        MultiplePiePlot p1 = new MultiplePiePlot(this.dataset);
        MultiplePiePlot p2 = new MultiplePiePlot(this.dataset);

        Assert.assertTrue(p1.equals(p1));
        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("Not a plot"));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertTrue(p2.equals(p1));

        // dataExtractOrder
        p1.setDataExtractOrder(TableOrder.BY_ROW);
        Assert.assertFalse(p1.equals(p2));
        p2.setDataExtractOrder(TableOrder.BY_ROW);
        Assert.assertTrue(p1.equals(p2));

        // limit
        p1.setLimit(0.2);
        Assert.assertFalse(p1.equals(p2));
        p2.setLimit(0.2);
        Assert.assertTrue(p1.equals(p2));

        // aggregatedItemsKey
        p1.setAggregatedItemsKey("Extra");
        Assert.assertFalse(p1.equals(p2));
        p2.setAggregatedItemsKey("Extra");
        Assert.assertTrue(p1.equals(p2));

        // aggregatedItemsPaint
        p1.setAggregatedItemsPaint(Color.yellow);
        Assert.assertFalse(p1.equals(p2));
        p2.setAggregatedItemsPaint(Color.yellow);
        Assert.assertTrue(p1.equals(p2));

        // pieChart
        PiePlot subPlot = new PiePlot();
        JFreeChart customChart = new JFreeChart("Title", subPlot);
        p1.setPieChart(customChart);
        Assert.assertFalse(p1.equals(p2));
        p2.setPieChart(new JFreeChart("Title", new PiePlot()));
        // Different chart instances with default contents may not be equal
        p2.setPieChart(customChart);
        Assert.assertTrue(p1.equals(p2));

        // super.equals check (e.g. outline paint)
        p1.setOutlinePaint(Color.blue);
        Assert.assertFalse(p1.equals(p2));
        p2.setOutlinePaint(Color.blue);
        Assert.assertTrue(p1.equals(p2));
    }

    @Test
    public void testSerialization() throws Exception {
        MultiplePiePlot p1 = new MultiplePiePlot(this.dataset);
        p1.setAggregatedItemsPaint(new GradientPaint(0f, 0f, Color.red, 10f, 10f, Color.blue));
        p1.setLimit(0.12);
        p1.setAggregatedItemsKey("Aggregated");
        p1.setDataExtractOrder(TableOrder.BY_ROW);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(p1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        MultiplePiePlot p2 = (MultiplePiePlot) in.readObject();
        in.close();

        Assert.assertEquals(p1.getDataExtractOrder(), p2.getDataExtractOrder());
        Assert.assertEquals(p1.getLimit(), p2.getLimit(), 0.000001);
        Assert.assertEquals(p1.getAggregatedItemsKey(), p2.getAggregatedItemsKey());
        Assert.assertEquals(p1.getAggregatedItemsPaint(), p2.getAggregatedItemsPaint());
        Assert.assertEquals(p1.getPlotType(), p2.getPlotType());

        // Test drawing on deserialized object
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        p2.draw(g2, new Rectangle(0, 0, 300, 300), null, null, null);
        g2.dispose();
    }

    @Test
    public void testCloning() throws Exception {
        MultiplePiePlot p1 = new MultiplePiePlot(this.dataset);
        MultiplePiePlot p2 = (MultiplePiePlot) p1.clone();

        Assert.assertNotSame(p1, p2);
        Assert.assertSame(p1.getClass(), p2.getClass());
        Assert.assertTrue(p1.equals(p2));
    }
}