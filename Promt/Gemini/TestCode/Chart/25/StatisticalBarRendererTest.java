package org.jfree.chart.renderer.category;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.junit.Assert;
import org.junit.Test;

public class StatisticalBarRendererTest {

    @Test
    public void testConstructorAndDefaults() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        Assert.assertEquals(Color.gray, renderer.getErrorIndicatorPaint());
        Assert.assertEquals(new BasicStroke(0.5f), renderer.getErrorIndicatorStroke());
    }

    @Test
    public void testSetErrorIndicatorPaint() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        final boolean[] notified = new boolean[]{false};
        renderer.addChangeListener(new RendererChangeListener() {
            public void rendererChanged(RendererChangeEvent event) {
                notified[0] = true;
            }
        });

        renderer.setErrorIndicatorPaint(Color.red);
        Assert.assertEquals(Color.red, renderer.getErrorIndicatorPaint());
        Assert.assertTrue(notified[0]);

        notified[0] = false;
        renderer.setErrorIndicatorPaint(null);
        Assert.assertNull(renderer.getErrorIndicatorPaint());
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testSetErrorIndicatorStroke() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        final boolean[] notified = new boolean[]{false};
        renderer.addChangeListener(new RendererChangeListener() {
            public void rendererChanged(RendererChangeEvent event) {
                notified[0] = true;
            }
        });

        BasicStroke stroke = new BasicStroke(2.0f);
        renderer.setErrorIndicatorStroke(stroke);
        Assert.assertEquals(stroke, renderer.getErrorIndicatorStroke());
        Assert.assertTrue(notified[0]);

        notified[0] = false;
        renderer.setErrorIndicatorStroke(null);
        Assert.assertNull(renderer.getErrorIndicatorStroke());
        Assert.assertTrue(notified[0]);
    }

    @Test
    public void testEquals() {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        StatisticalBarRenderer r2 = new StatisticalBarRenderer();
        Assert.assertTrue(r1.equals(r2));
        Assert.assertTrue(r2.equals(r1));
        Assert.assertTrue(r1.equals(r1));

        Assert.assertFalse(r1.equals(null));
        Assert.assertFalse(r1.equals("Not a renderer"));

        r1.setErrorIndicatorPaint(Color.blue);
        Assert.assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(Color.blue);
        Assert.assertTrue(r1.equals(r2));

        r1.setErrorIndicatorPaint(null);
        Assert.assertFalse(r1.equals(r2));
        r2.setErrorIndicatorPaint(null);
        Assert.assertTrue(r1.equals(r2));

        r1.setSeriesPaint(0, Color.black);
        Assert.assertFalse(r1.equals(r2));
        r2.setSeriesPaint(0, Color.black);
        Assert.assertTrue(r1.equals(r2));
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        r1.setErrorIndicatorPaint(Color.cyan);
        r1.setErrorIndicatorStroke(new BasicStroke(1.5f));
        StatisticalBarRenderer r2 = (StatisticalBarRenderer) r1.clone();

        Assert.assertNotSame(r1, r2);
        Assert.assertSame(r1.getClass(), r2.getClass());
        Assert.assertEquals(r1, r2);
    }

    @Test
    public void testSerialization() throws Exception {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        r1.setErrorIndicatorPaint(new GradientPaint(1.0f, 2.0f, Color.red, 3.0f, 4.0f, Color.yellow));
        r1.setErrorIndicatorStroke(new BasicStroke(1.2f));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(r1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        StatisticalBarRenderer r2 = (StatisticalBarRenderer) in.readObject();
        in.close();

        Assert.assertEquals(r1, r2);
        Assert.assertEquals(r1.getErrorIndicatorPaint(), r2.getErrorIndicatorPaint());
        Assert.assertEquals(r1.getErrorIndicatorStroke(), r2.getErrorIndicatorStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDrawItemInvalidDataset() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        CategoryPlot plot = new CategoryPlot(
                new DefaultCategoryDataset(),
                new CategoryAxis("Category"),
                new NumberAxis("Value"),
                renderer
        );
        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 200));
        g2.dispose();
    }

    @Test
    public void testDrawItemVertical() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Type A");
        dataset.add(-5.0, 1.5, "Series 1", "Type B");
        dataset.add(15.0, 3.0, "Series 2", "Type A");
        dataset.add(2.0, 0.5, "Series 2", "Type B");

        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart(plot);

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        BufferedImage image = chart.createBufferedImage(400, 300, info);
        Assert.assertNotNull(image);
        Assert.assertTrue(info.getEntityCollection().getEntityCount() > 0);

        // Test with null paint & stroke to cover fallback branches
        renderer.setErrorIndicatorPaint(null);
        renderer.setErrorIndicatorStroke(null);
        image = chart.createBufferedImage(400, 300, info);
        Assert.assertNotNull(image);
    }

    @Test
    public void testDrawItemHorizontal() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Type A");
        dataset.add(-5.0, 1.5, "Series 1", "Type B");
        dataset.add(15.0, 3.0, "Series 2", "Type A");
        dataset.add(2.0, 0.5, "Series 2", "Type B");

        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());
        BufferedImage image = chart.createBufferedImage(400, 300, info);
        Assert.assertNotNull(image);
        Assert.assertTrue(info.getEntityCollection().getEntityCount() > 0);

        // Test with null paint & stroke to cover fallback branches
        renderer.setErrorIndicatorPaint(null);
        renderer.setErrorIndicatorStroke(null);
        image = chart.createBufferedImage(400, 300, info);
        Assert.assertNotNull(image);
    }

    @Test
    public void testDrawItemSingleSeries() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Type A");

        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = chart.createBufferedImage(400, 300, (ChartRenderingInfo) null);
        Assert.assertNotNull(image);

        plot.setOrientation(PlotOrientation.HORIZONTAL);
        image = chart.createBufferedImage(400, 300, (ChartRenderingInfo) null);
        Assert.assertNotNull(image);
    }

    @Test
    public void testDrawItemWithVariousAxisRangesVertical() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Type A");
        dataset.add(-10.0, 2.0, "Series 1", "Type B");

        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), rangeAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart(plot);

        // Range all positive: lower clip > 0
        rangeAxis.setRange(5.0, 20.0);
        chart.createBufferedImage(300, 200, (ChartRenderingInfo) null);

        // Range all negative: upper clip < 0
        rangeAxis.setRange(-20.0, -5.0);
        chart.createBufferedImage(300, 200, (ChartRenderingInfo) null);

        // Range spanning 0: lower <= 0 <= upper
        rangeAxis.setRange(-15.0, 15.0);
        chart.createBufferedImage(300, 200, (ChartRenderingInfo) null);
    }

    @Test
    public void testDrawItemWithVariousAxisRangesHorizontal() {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(10.0, 2.0, "Series 1", "Type A");
        dataset.add(-10.0, 2.0, "Series 1", "Type B");

        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), rangeAxis, renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);

        // Range all positive: lower clip > 0
        rangeAxis.setRange(5.0, 20.0);
        chart.createBufferedImage(300, 200, (ChartRenderingInfo) null);

        // Range all negative: upper clip < 0
        rangeAxis.setRange(-20.0, -5.0);
        chart.createBufferedImage(300, 200, (ChartRenderingInfo) null);

        // Range spanning 0: lower <= 0 <= upper
        rangeAxis.setRange(-15.0, 15.0);
        chart.createBufferedImage(300, 200, (ChartRenderingInfo) null);
    }
}