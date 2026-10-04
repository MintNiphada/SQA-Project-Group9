package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Arc2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.Icon;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.CategoryItemRendererState;

public class MinMaxCategoryRendererTest {

    @Test
    public void testDefaultConstructor() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        assertNotNull(r);
        assertFalse(r.isDrawLines());
        assertNotNull(r.getGroupPaint());
        assertNotNull(r.getGroupStroke());
        assertNotNull(r.getObjectIcon());
        assertNotNull(r.getMaxIcon());
        assertNotNull(r.getMinIcon());
    }

    @Test
    public void testSetDrawLines() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setDrawLines(true);
        assertTrue(r.isDrawLines());
        r.setDrawLines(false);
        assertFalse(r.isDrawLines());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGroupPaintNull() {
        new MinMaxCategoryRenderer().setGroupPaint(null);
    }

    @Test
    public void testSetGroupPaint() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setGroupPaint(Color.red);
        assertEquals(Color.red, r.getGroupPaint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetGroupStrokeNull() {
        new MinMaxCategoryRenderer().setGroupStroke(null);
    }

    @Test
    public void testSetGroupStroke() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        BasicStroke stroke = new BasicStroke(2.0f);
        r.setGroupStroke(stroke);
        assertEquals(stroke, r.getGroupStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetObjectIconNull() {
        new MinMaxCategoryRenderer().setObjectIcon(null);
    }

    @Test
    public void testSetObjectIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Icon icon = new MinMaxCategoryRenderer().getObjectIcon();
        r.setObjectIcon(icon);
        assertSame(icon, r.getObjectIcon());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaxIconNull() {
        new MinMaxCategoryRenderer().setMaxIcon(null);
    }

    @Test
    public void testSetMaxIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Icon icon = new MinMaxCategoryRenderer().getMaxIcon();
        r.setMaxIcon(icon);
        assertSame(icon, r.getMaxIcon());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinIconNull() {
        new MinMaxCategoryRenderer().setMinIcon(null);
    }

    @Test
    public void testSetMinIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Icon icon = new MinMaxCategoryRenderer().getMinIcon();
        r.setMinIcon(icon);
        assertSame(icon, r.getMinIcon());
    }

    @Test
    public void testDrawItemNullValue() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "S1", "C1");
        CategoryPlot plot = createPlot(dataset);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        CategoryItemRendererState state = new CategoryItemRendererState(null);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemSingleSeriesVertical() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "S1", "C1");
        CategoryPlot plot = createPlot(dataset);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        CategoryItemRendererState state = new CategoryItemRendererState(null);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemMultipleSeriesVertical() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(3.0, "S2", "C1");
        dataset.addValue(2.0, "S3", "C1");
        CategoryPlot plot = createPlot(dataset);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        CategoryItemRendererState state = new CategoryItemRendererState(null);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 1, 0, 0);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 2, 0, 0);
    }

    @Test
    public void testDrawItemHorizontalOrientation() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "S1", "C1");
        CategoryPlot plot = createPlot(dataset);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        CategoryItemRendererState state = new CategoryItemRendererState(null);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemWithPlotLines() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setDrawLines(true);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "S1", "C1");
        dataset.addValue(2.0, "S1", "C2");
        CategoryPlot plot = createPlot(dataset);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        CategoryItemRendererState state = new CategoryItemRendererState(null);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 1, 0);
    }

    @Test
    public void testDrawItemPlotLinesWithNullPrevious() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        r.setDrawLines(true);
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(null, "S1", "C1");
        dataset.addValue(2.0, "S1", "C2");
        CategoryPlot plot = createPlot(dataset);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        CategoryItemRendererState state = new CategoryItemRendererState(null);
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 1, 0);
    }

    @Test
    public void testDrawItemWithEntityCollection() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "S1", "C1");
        CategoryPlot plot = createPlot(dataset);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        CategoryItemRendererState state = new CategoryItemRendererState(null);
        state.setEntityCollection(new org.jfree.chart.entity.StandardEntityCollection());
        Rectangle2D dataArea = new Rectangle2D.Double(0, 0, 200, 200);
        r.drawItem(g2, state, dataArea, plot, plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);
    }

    @Test
    public void testSerialization() throws Exception {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        r1.setGroupPaint(Color.blue);
        r1.setGroupStroke(new BasicStroke(2.0f));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(r1);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        MinMaxCategoryRenderer r2 = (MinMaxCategoryRenderer) ois.readObject();
        assertNotNull(r2);
        assertEquals(r1.isDrawLines(), r2.isDrawLines());
        assertEquals(r1.getGroupPaint(), r2.getGroupPaint());
        assertEquals(r1.getGroupStroke(), r2.getGroupStroke());
        assertNotNull(r2.getObjectIcon());
        assertNotNull(r2.getMaxIcon());
        assertNotNull(r2.getMinIcon());
    }

    private CategoryPlot createPlot(DefaultCategoryDataset dataset) {
        CategoryAxis domainAxis = new CategoryAxis("Category");
        NumberAxis rangeAxis = new NumberAxis("Value");
        CategoryPlot plot = new CategoryPlot(dataset, domainAxis, rangeAxis, null);
        return plot;
    }
}
