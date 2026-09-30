package org.jfree.chart.renderer.category;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link MinMaxCategoryRenderer} class.
 */
public class MinMaxCategoryRendererTest {

    private static class EventListener implements RendererChangeListener {
        private boolean notified = false;

        public void rendererChanged(RendererChangeEvent event) {
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
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Assert.assertFalse(r.isDrawLines());
        Assert.assertEquals(Color.black, r.getGroupPaint());
        Assert.assertEquals(new BasicStroke(1.0f), r.getGroupStroke());
        Assert.assertNotNull(r.getMinIcon());
        Assert.assertNotNull(r.getMaxIcon());
        Assert.assertNotNull(r.getObjectIcon());
    }

    @Test
    public void testSetDrawLines() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        EventListener listener = new EventListener();
        r.addChangeListener(listener);

        Assert.assertFalse(r.isDrawLines());
        r.setDrawLines(true);
        Assert.assertTrue(r.isDrawLines());
        Assert.assertTrue(listener.isNotified());

        listener.reset();
        r.setDrawLines(true);
        Assert.assertFalse(listener.isNotified());

        r.setDrawLines(false);
        Assert.assertFalse(r.isDrawLines());
        Assert.assertTrue(listener.isNotified());
    }

    @Test
    public void testSetGroupPaint() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        EventListener listener = new EventListener();
        r.addChangeListener(listener);

        r.setGroupPaint(Color.red);
        Assert.assertEquals(Color.red, r.getGroupPaint());
        Assert.assertTrue(listener.isNotified());

        try {
            r.setGroupPaint(null);
            Assert.fail("Expected IllegalArgumentException on null argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetGroupStroke() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        EventListener listener = new EventListener();
        r.addChangeListener(listener);

        Stroke stroke = new BasicStroke(2.5f);
        r.setGroupStroke(stroke);
        Assert.assertEquals(stroke, r.getGroupStroke());
        Assert.assertTrue(listener.isNotified());

        try {
            r.setGroupStroke(null);
            Assert.fail("Expected IllegalArgumentException on null argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetObjectIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        EventListener listener = new EventListener();
        r.addChangeListener(listener);

        Icon icon = new ImageIcon();
        r.setObjectIcon(icon);
        Assert.assertEquals(icon, r.getObjectIcon());
        Assert.assertTrue(listener.isNotified());

        try {
            r.setObjectIcon(null);
            Assert.fail("Expected IllegalArgumentException on null argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetMaxIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        EventListener listener = new EventListener();
        r.addChangeListener(listener);

        Icon icon = new ImageIcon();
        r.setMaxIcon(icon);
        Assert.assertEquals(icon, r.getMaxIcon());
        Assert.assertTrue(listener.isNotified());

        try {
            r.setMaxIcon(null);
            Assert.fail("Expected IllegalArgumentException on null argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetMinIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        EventListener listener = new EventListener();
        r.addChangeListener(listener);

        Icon icon = new ImageIcon();
        r.setMinIcon(icon);
        Assert.assertEquals(icon, r.getMinIcon());
        Assert.assertTrue(listener.isNotified());

        try {
            r.setMinIcon(null);
            Assert.fail("Expected IllegalArgumentException on null argument");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testIconDimensionsAndPainting() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Icon minIcon = r.getMinIcon();
        Icon maxIcon = r.getMaxIcon();
        Icon objIcon = r.getObjectIcon();

        Assert.assertTrue(minIcon.getIconWidth() >= 0);
        Assert.assertTrue(minIcon.getIconHeight() >= 0);
        Assert.assertTrue(maxIcon.getIconWidth() >= 0);
        Assert.assertTrue(maxIcon.getIconHeight() >= 0);
        Assert.assertTrue(objIcon.getIconWidth() >= 0);
        Assert.assertTrue(objIcon.getIconHeight() >= 0);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        minIcon.paintIcon(null, g2, 10, 10);
        maxIcon.paintIcon(null, g2, 20, 20);
        objIcon.paintIcon(null, g2, 30, 30);
        g2.dispose();
    }

    @Test
    public void testDrawItemVertical() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series 1", "Category 1");
        dataset.addValue(4.0, "Series 2", "Category 1");
        dataset.addValue(2.0, "Series 3", "Category 1");

        dataset.addValue(3.0, "Series 1", "Category 2");
        dataset.addValue(5.0, "Series 2", "Category 2");
        dataset.addValue(1.0, "Series 3", "Category 2");

        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), r);
        plot.setOrientation(PlotOrientation.VERTICAL);

        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 260, 160);
        ChartRenderingInfo info = new ChartRenderingInfo();
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(info);
        CategoryItemRendererState state = r.initialise(g2, dataArea, plot, 0, plotInfo);

        CategoryAxis domainAxis = plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        for (int row = 0; row < dataset.getRowCount(); row++) {
            for (int col = 0; col < dataset.getColumnCount(); col++) {
                r.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, col, 0);
            }
        }
        g2.dispose();

        EntityCollection entities = info.getEntityCollection();
        Assert.assertNotNull(entities);
        Assert.assertTrue(entities.getEntityCount() > 0);
    }

    @Test
    public void testDrawItemHorizontal() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series 1", "Category 1");
        dataset.addValue(5.0, "Series 2", "Category 1");
        dataset.addValue(3.0, "Series 1", "Category 2");
        dataset.addValue(2.0, "Series 2", "Category 2");

        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), r);
        plot.setOrientation(PlotOrientation.HORIZONTAL);

        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 260, 160);
        ChartRenderingInfo info = new ChartRenderingInfo();
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(info);
        CategoryItemRendererState state = r.initialise(g2, dataArea, plot, 0, plotInfo);

        CategoryAxis domainAxis = plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        for (int row = 0; row < dataset.getRowCount(); row++) {
            for (int col = 0; col < dataset.getColumnCount(); col++) {
                r.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, col, 0);
            }
        }
        g2.dispose();

        EntityCollection entities = info.getEntityCollection();
        Assert.assertNotNull(entities);
        Assert.assertTrue(entities.getEntityCount() > 0);
    }

    @Test
    public void testDrawItemWithNullValues() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "Series 1", "Category 1");
        dataset.addValue(4.0, "Series 2", "Category 1");
        dataset.addValue(2.0, "Series 1", "Category 2");
        dataset.addValue(null, "Series 2", "Category 2");

        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), r);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 280, 180);
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(new ChartRenderingInfo());
        CategoryItemRendererState state = r.initialise(g2, dataArea, plot, 0, plotInfo);

        CategoryAxis domainAxis = plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        for (int row = 0; row < dataset.getRowCount(); row++) {
            for (int col = 0; col < dataset.getColumnCount(); col++) {
                r.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, col, 0);
            }
        }
        g2.dispose();
    }

    @Test
    public void testDrawItemSingleRow() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "Series 1", "Category 1");
        dataset.addValue(15.0, "Series 1", "Category 2");

        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Category"), new NumberAxis("Value"), r);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 280, 180);
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(null);
        CategoryItemRendererState state = r.initialise(g2, dataArea, plot, 0, plotInfo);

        CategoryAxis domainAxis = plot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

        for (int col = 0; col < dataset.getColumnCount(); col++) {
            r.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, col, 0);
        }
        g2.dispose();
    }

    @Test
    public void testSerialization() throws Exception {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        r1.setDrawLines(true);
        r1.setGroupPaint(new GradientPaint(0f, 0f, Color.red, 10f, 10f, Color.blue));
        r1.setGroupStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(r1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        MinMaxCategoryRenderer r2 = (MinMaxCategoryRenderer) in.readObject();
        in.close();

        Assert.assertEquals(r1.isDrawLines(), r2.isDrawLines());
        Assert.assertEquals(r1.getGroupPaint(), r2.getGroupPaint());
        Assert.assertEquals(r1.getGroupStroke(), r2.getGroupStroke());
        Assert.assertNotNull(r2.getMinIcon());
        Assert.assertNotNull(r2.getMaxIcon());
        Assert.assertNotNull(r2.getObjectIcon());

        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        r2.getMinIcon().paintIcon(null, g2, 0, 0);
        r2.getMaxIcon().paintIcon(null, g2, 0, 0);
        r2.getObjectIcon().paintIcon(null, g2, 0, 0);
        g2.dispose();
    }

    @Test
    public void testCloning() throws Exception {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = (MinMaxCategoryRenderer) r1.clone();

        Assert.assertNotSame(r1, r2);
        Assert.assertSame(r1.getClass(), r2.getClass());
        Assert.assertEquals(r1.isDrawLines(), r2.isDrawLines());
        Assert.assertEquals(r1.getGroupPaint(), r2.getGroupPaint());
        Assert.assertEquals(r1.getGroupStroke(), r2.getGroupStroke());
    }
}