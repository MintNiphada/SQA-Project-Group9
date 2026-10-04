package org.jfree.chart.renderer.category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.junit.Before;
import org.junit.Test;

public class StatisticalBarRendererTest {

    private StatisticalBarRenderer renderer;
    private CategoryPlot plot;
    private CategoryAxis domainAxis;
    private ValueAxis rangeAxis;
    private DefaultStatisticalCategoryDataset dataset;
    private Rectangle2D dataArea;
    private CategoryItemRendererState state;
    private Graphics2D g2;

    @Before
    public void setUp() {
        renderer = new StatisticalBarRenderer();
        domainAxis = new CategoryAxis("Category");
        rangeAxis = new NumberAxis("Value");
        dataset = new DefaultStatisticalCategoryDataset();
        dataset.add(5.0, 1.0, "Series1", "Category1");
        dataset.add(10.0, 2.0, "Series1", "Category2");
        plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        dataArea = new Rectangle2D.Double(0, 0, 200, 100);
        state = new CategoryItemRendererState(null);
        state.setBarWidth(10.0);
        BufferedImage image = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        g2 = image.createGraphics();
    }

    @Test
    public void testConstructor() {
        assertEquals(Color.gray, renderer.getErrorIndicatorPaint());
        assertNotNull(renderer.getErrorIndicatorStroke());
        assertTrue(renderer.getErrorIndicatorStroke() instanceof BasicStroke);
    }

    @Test
    public void testGetSetErrorIndicatorPaint() {
        renderer.setErrorIndicatorPaint(Color.red);
        assertEquals(Color.red, renderer.getErrorIndicatorPaint());
        renderer.setErrorIndicatorPaint(null);
        assertEquals(null, renderer.getErrorIndicatorPaint());
    }

    @Test
    public void testGetSetErrorIndicatorStroke() {
        Stroke stroke = new BasicStroke(2.0f);
        renderer.setErrorIndicatorStroke(stroke);
        assertEquals(stroke, renderer.getErrorIndicatorStroke());
        renderer.setErrorIndicatorStroke(null);
        assertEquals(null, renderer.getErrorIndicatorStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDrawItemWithNonStatisticalDataset() {
        CategoryDataset nonStat = new org.jfree.data.category.DefaultCategoryDataset();
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, nonStat, 0, 0, 0);
    }

    @Test
    public void testDrawItemVertical() {
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemHorizontal() {
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalWithMultipleSeries() {
        dataset.add(7.0, 1.5, "Series2", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 1, 0, 0);
    }

    @Test
    public void testDrawItemHorizontalWithMultipleSeries() {
        dataset.add(7.0, 1.5, "Series2", "Category1");
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 1, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase1() {
        renderer.setUpperClip(-1.0);
        renderer.setLowerClip(-10.0);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase2() {
        renderer.setUpperClip(0.0);
        renderer.setLowerClip(-10.0);
        dataset.add(0.0, 0.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase3() {
        renderer.setUpperClip(0.0);
        renderer.setLowerClip(-10.0);
        dataset.add(-5.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase4() {
        renderer.setUpperClip(0.0);
        renderer.setLowerClip(-10.0);
        dataset.add(-15.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase5() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(-5.0);
        dataset.add(12.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase6() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(-5.0);
        dataset.add(5.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase7() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(-5.0);
        dataset.add(-3.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase8() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(-5.0);
        dataset.add(-10.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase9() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(2.0);
        dataset.add(1.0, 0.5, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase10() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(2.0);
        dataset.add(5.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase11() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(2.0);
        dataset.add(12.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemVerticalClipCase12() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(2.0);
        dataset.add(15.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemHorizontalClipCase1() {
        renderer.setUpperClip(-1.0);
        renderer.setLowerClip(-10.0);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemHorizontalClipCase5() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(-5.0);
        dataset.add(12.0, 1.0, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawItemHorizontalClipCase9() {
        renderer.setUpperClip(10.0);
        renderer.setLowerClip(2.0);
        dataset.add(1.0, 0.5, "Series1", "Category1");
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawBarOutlineWhenWidthGreaterThan3() {
        renderer.setDrawBarOutline(true);
        state.setBarWidth(10.0);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testDrawBarOutlineWhenWidthNotGreaterThan3() {
        renderer.setDrawBarOutline(true);
        state.setBarWidth(2.0);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testErrorIndicatorStrokeNullUsesItemOutlineStroke() {
        renderer.setErrorIndicatorStroke(null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testErrorIndicatorPaintNullUsesItemOutlinePaint() {
        renderer.setErrorIndicatorPaint(null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testItemLabelGeneratorNotNullAndVisible() {
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
    }

    @Test
    public void testEntityCollectionNotNull() {
        StandardEntityCollection entities = new StandardEntityCollection();
        state.setEntityCollection(entities);
        plot.setOrientation(PlotOrientation.VERTICAL);
        renderer.drawItem(g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, 0, 0, 0);
        assertFalse(entities.getEntities().isEmpty());
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(renderer.equals(renderer));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(renderer.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(renderer.equals(new Object()));
    }

    @Test
    public void testEqualsDifferentErrorIndicatorPaint() {
        StatisticalBarRenderer other = new StatisticalBarRenderer();
        other.setErrorIndicatorPaint(Color.blue);
        assertFalse(renderer.equals(other));
    }

    @Test
    public void testEqualsSameProperties() {
        StatisticalBarRenderer other = new StatisticalBarRenderer();
        assertTrue(renderer.equals(other));
    }

    @Test
    public void testSerialization() throws Exception {
        StatisticalBarRenderer r1 = new StatisticalBarRenderer();
        r1.setErrorIndicatorPaint(Color.red);
        r1.setErrorIndicatorStroke(new BasicStroke(2.0f));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(r1);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        StatisticalBarRenderer r2 = (StatisticalBarRenderer) ois.readObject();
        assertTrue(r1.equals(r2));
    }
}
