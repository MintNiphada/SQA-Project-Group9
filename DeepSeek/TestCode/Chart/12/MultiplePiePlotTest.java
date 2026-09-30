package org.jfree.chart.plot;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.junit.Before;
import org.junit.Test;

public class MultiplePiePlotTest {

    private MultiplePiePlot plot;
    private DefaultCategoryDataset dataset;
    private JFreeChart pieChart;

    @Before
    public void setUp() {
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R1", "C1");
        dataset.addValue(2.0, "R1", "C2");
        dataset.addValue(3.0, "R2", "C1");
        dataset.addValue(4.0, "R2", "C2");
        plot = new MultiplePiePlot(dataset);
        pieChart = plot.getPieChart();
    }

    // Constructor tests
    @Test
    public void testDefaultConstructor() {
        MultiplePiePlot p = new MultiplePiePlot();
        assertNull(p.getDataset());
        assertNotNull(p.getPieChart());
        assertEquals(TableOrder.BY_COLUMN, p.getDataExtractOrder());
        assertEquals(0.0, p.getLimit(), 0.00001);
        assertEquals("Other", p.getAggregatedItemsKey().toString());
        assertNotNull(p.getAggregatedItemsPaint());
    }

    @Test
    public void testConstructorWithNullDataset() {
        MultiplePiePlot p = new MultiplePiePlot(null);
        assertNull(p.getDataset());
        assertNotNull(p.getPieChart());
    }

    @Test
    public void testConstructorWithDataset() {
        MultiplePiePlot p = new MultiplePiePlot(dataset);
        assertEquals(dataset, p.getDataset());
        assertNotNull(p.getPieChart());
    }

    // Dataset getter/setter
    @Test
    public void testGetDataset() {
        assertEquals(dataset, plot.getDataset());
    }

    @Test
    public void testSetDataset() {
        DefaultCategoryDataset newDataset = new DefaultCategoryDataset();
        newDataset.addValue(5.0, "R3", "C3");
        plot.setDataset(newDataset);
        assertEquals(newDataset, plot.getDataset());
    }

    @Test
    public void testSetDatasetToNull() {
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    @Test
    public void testSetDatasetFiresEvent() {
        final boolean[] eventFired = {false};
        plot.addChangeListener(new PlotChangeListener() {
            @Override
            public void plotChanged(PlotChangeEvent event) {
                eventFired[0] = true;
            }
        });
        plot.setDataset(new DefaultCategoryDataset());
        assertTrue(eventFired[0]);
    }

    // PieChart getter/setter
    @Test
    public void testGetPieChart() {
        assertNotNull(plot.getPieChart());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPieChartNull() {
        plot.setPieChart(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetPieChartWithNonPiePlot() {
        JFreeChart barChart = ChartFactory.createBarChart("Test", "X", "Y", dataset);
        plot.setPieChart(barChart);
    }

    @Test
    public void testSetPieChartValid() {
        PiePlot piePlot = new PiePlot();
        JFreeChart newChart = new JFreeChart(piePlot);
        plot.setPieChart(newChart);
        assertEquals(newChart, plot.getPieChart());
    }

    @Test
    public void testSetPieChartFiresEvent() {
        final boolean[] eventFired = {false};
        plot.addChangeListener(new PlotChangeListener() {
            @Override
            public void plotChanged(PlotChangeEvent event) {
                eventFired[0] = true;
            }
        });
        PiePlot piePlot = new PiePlot();
        JFreeChart newChart = new JFreeChart(piePlot);
        plot.setPieChart(newChart);
        assertTrue(eventFired[0]);
    }

    // DataExtractOrder
    @Test
    public void testGetDataExtractOrder() {
        assertEquals(TableOrder.BY_COLUMN, plot.getDataExtractOrder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDataExtractOrderNull() {
        plot.setDataExtractOrder(null);
    }

    @Test
    public void testSetDataExtractOrderValid() {
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertEquals(TableOrder.BY_ROW, plot.getDataExtractOrder());
    }

    @Test
    public void testSetDataExtractOrderFiresEvent() {
        final boolean[] eventFired = {false};
        plot.addChangeListener(new PlotChangeListener() {
            @Override
            public void plotChanged(PlotChangeEvent event) {
                eventFired[0] = true;
            }
        });
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        assertTrue(eventFired[0]);
    }

    // Limit
    @Test
    public void testGetLimit() {
        assertEquals(0.0, plot.getLimit(), 0.00001);
    }

    @Test
    public void testSetLimit() {
        plot.setLimit(0.25);
        assertEquals(0.25, plot.getLimit(), 0.00001);
    }

    @Test
    public void testSetLimitFiresEvent() {
        final boolean[] eventFired = {false};
        plot.addChangeListener(new PlotChangeListener() {
            @Override
            public void plotChanged(PlotChangeEvent event) {
                eventFired[0] = true;
            }
        });
        plot.setLimit(0.5);
        assertTrue(eventFired[0]);
    }

    // AggregatedItemsKey
    @Test
    public void testGetAggregatedItemsKey() {
        assertEquals("Other", plot.getAggregatedItemsKey().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsKeyNull() {
        plot.setAggregatedItemsKey(null);
    }

    @Test
    public void testSetAggregatedItemsKey() {
        plot.setAggregatedItemsKey("NewKey");
        assertEquals("NewKey", plot.getAggregatedItemsKey().toString());
    }

    @Test
    public void testSetAggregatedItemsKeyFiresEvent() {
        final boolean[] eventFired = {false};
        plot.addChangeListener(new PlotChangeListener() {
            @Override
            public void plotChanged(PlotChangeEvent event) {
                eventFired[0] = true;
            }
        });
        plot.setAggregatedItemsKey("Key");
        assertTrue(eventFired[0]);
    }

    // AggregatedItemsPaint
    @Test
    public void testGetAggregatedItemsPaint() {
        assertEquals(Color.lightGray, plot.getAggregatedItemsPaint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAggregatedItemsPaintNull() {
        plot.setAggregatedItemsPaint(null);
    }

    @Test
    public void testSetAggregatedItemsPaint() {
        plot.setAggregatedItemsPaint(Color.red);
        assertEquals(Color.red, plot.getAggregatedItemsPaint());
    }

    @Test
    public void testSetAggregatedItemsPaintFiresEvent() {
        final boolean[] eventFired = {false};
        plot.addChangeListener(new PlotChangeListener() {
            @Override
            public void plotChanged(PlotChangeEvent event) {
                eventFired[0] = true;
            }
        });
        plot.setAggregatedItemsPaint(Color.green);
        assertTrue(eventFired[0]);
    }

    // getPlotType
    @Test
    public void testGetPlotType() {
        assertEquals("Multiple Pie Plot", plot.getPlotType());
    }

    // draw method - basic test for coverage (no assertion on rendering)
    @Test
    public void testDrawWithNullDataset() {
        MultiplePiePlot p = new MultiplePiePlot(null);
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        try {
            p.draw(g2, area, null, null, null);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawWithEmptyDataset() {
        MultiplePiePlot p = new MultiplePiePlot(new DefaultCategoryDataset());
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 100, 100);
        try {
            p.draw(g2, area, null, null, null);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawWithDataByColumn() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 200);
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        try {
            plot.draw(g2, area, null, null, null);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawWithDataByRow() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 200);
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        try {
            plot.draw(g2, area, null, null, null);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawWithLimitGreaterThanZero() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 200);
        plot.setLimit(0.1);
        try {
            plot.draw(g2, area, null, null, null);
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawWithRenderInfo() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 200);
        PlotRenderingInfo info = new PlotRenderingInfo(null);
        try {
            plot.draw(g2, area, null, null, info);
            // just verify no exception
        } finally {
            g2.dispose();
        }
    }

    @Test
    public void testDrawWithManyRowsAndColumnsToTriggerOffset() {
        // Create dataset with 10 series/columns to test xoffset calculation
        DefaultCategoryDataset big = new DefaultCategoryDataset();
        for (int i = 0; i < 10; i++) {
            big.addValue(i, "Row" + i, "Col" + i);
        }
        MultiplePiePlot p = new MultiplePiePlot(big);
        p.setDataExtractOrder(TableOrder.BY_ROW); // row count = 10
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 300, 300);
        try {
            p.draw(g2, area, null, null, null);
        } finally {
            g2.dispose();
        }
    }

    // getLegendItems tests
    @Test
    public void testGetLegendItemsWithNullDataset() {
        MultiplePiePlot p = new MultiplePiePlot();
        LegendItemCollection items = p.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.getItemCount());
    }

    @Test
    public void testGetLegendItemsWithDataByColumn() {
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
        // Should have one per column key (C1, C2) because limit=0 -> no aggregated
        assertEquals(2, items.getItemCount());
    }

    @Test
    public void testGetLegendItemsWithDataByRow() {
        plot.setDataExtractOrder(TableOrder.BY_ROW);
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
        assertEquals(2, items.getItemCount()); // two rows
    }

    @Test
    public void testGetLegendItemsWithLimit() {
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        plot.setLimit(0.2); // any limit >0 should add aggregated item
        plot.setAggregatedItemsKey("Other");
        LegendItemCollection items = plot.getLegendItems();
        // 2 columns + 1 aggregated = 3
        assertEquals(3, items.getItemCount());
        LegendItem last = items.get(items.getItemCount() - 1);
        assertEquals("Other", last.getLabel());
    }

    @Test
    public void testGetLegendItemsWithLimitAndCustomKey() {
        plot.setDataExtractOrder(TableOrder.BY_COLUMN);
        plot.setLimit(0.1);
        plot.setAggregatedItemsKey("CustomKey");
        LegendItemCollection items = plot.getLegendItems();
        LegendItem last = items.get(items.getItemCount() - 1);
        assertEquals("CustomKey", last.getLabel());
    }

    // equals tests
    @Test
    public void testEqualsSameObject() {
        assertTrue(plot.equals(plot));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(plot.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(plot.equals("string"));
    }

    @Test
    public void testEqualsIdentical() {
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        assertEquals(plot, p2);
    }

    @Test
    public void testEqualsDifferentDataExtractOrder() {
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        p2.setDataExtractOrder(TableOrder.BY_ROW);
        assertFalse(plot.equals(p2));
    }

    @Test
    public void testEqualsDifferentLimit() {
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        p2.setLimit(0.5);
        assertFalse(plot.equals(p2));
    }

    @Test
    public void testEqualsDifferentAggregatedItemsKey() {
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        p2.setAggregatedItemsKey("OtherKey");
        assertFalse(plot.equals(p2));
    }

    @Test
    public void testEqualsDifferentAggregatedItemsPaint() {
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        p2.setAggregatedItemsPaint(Color.black);
        assertFalse(plot.equals(p2));
    }

    @Test
    public void testEqualsDifferentPieChart() {
        MultiplePiePlot p2 = new MultiplePiePlot(dataset);
        PiePlot piePlot = new PiePlot();
        JFreeChart newChart = new JFreeChart(piePlot);
        p2.setPieChart(newChart);
        assertFalse(plot.equals(p2));
    }

    // Serialization test
    @Test
    public void testSerialization() throws Exception {
        MultiplePiePlot p1 = new MultiplePiePlot(dataset);
        p1.setLimit(0.3);
        p1.setAggregatedItemsKey("Aggkey");
        p1.setAggregatedItemsPaint(Color.blue);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(p1);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        MultiplePiePlot p2 = (MultiplePiePlot) ois.readObject();
        ois.close();

        assertNotNull(p2);
        assertEquals(p1.getLimit(), p2.getLimit(), 0.0);
        assertEquals(p1.getAggregatedItemsKey(), p2.getAggregatedItemsKey());
        assertEquals(p1.getAggregatedItemsPaint(), p2.getAggregatedItemsPaint());
        assertEquals(p1.getDataExtractOrder(), p2.getDataExtractOrder());
        // Note: serialization doesn't preserve dataset equality because dataset is transient? Actually dataset is standard field, will be serialized.
        // We can check dataset not null.
        assertNotNull(p2.getDataset());
    }

    // Additional tests for prefetchSectionPaints (indirectly via legend/draw)
    @Test
    public void testPrefetchSectionPaintsFillsCache() {
        // This test ensures that getLegendItems triggers prefetchSectionPaints
        // which populates sectionPaints map to avoid NPE in draw.
        assertNotNull(plot.getLegendItems()); // ensures prefetchSectionPaints run
        // Adding a new key to dataset and calling getLegendItems again should work
        dataset.addValue(6.0, "NewR", "C1");
        LegendItemCollection items = plot.getLegendItems();
        // Number of keys depends on order; BY_COLUMN will use column keys (C1, C2) + aggregated if limit>0
        // Since limit=0, it should be 2 columns
        assertEquals(2, items.getItemCount());
    }
}
