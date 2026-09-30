package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.Rotation;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.junit.Assert;
import org.junit.Test;

public class PiePlotTest {

    @Test
    public void testConstructorAndDefaults() {
        PiePlot plot = new PiePlot();
        Assert.assertNull(plot.getDataset());
        Assert.assertEquals(0, plot.getPieIndex());
        Assert.assertEquals(PiePlot.DEFAULT_INTERIOR_GAP, plot.getInteriorGap(), 0.0001);
        Assert.assertTrue(plot.isCircular());
        Assert.assertEquals(PiePlot.DEFAULT_START_ANGLE, plot.getStartAngle(), 0.0001);
        Assert.assertEquals(Rotation.CLOCKWISE, plot.getDirection());
        Assert.assertEquals(PiePlot.DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW, plot.getMinimumArcAngleToDraw(), 0.000001);
        Assert.assertTrue(plot.getSectionOutlinesVisible());
        Assert.assertEquals(Color.gray, plot.getBaseSectionPaint());
        Assert.assertEquals(Plot.DEFAULT_OUTLINE_PAINT, plot.getBaseSectionOutlinePaint());
        Assert.assertEquals(Plot.DEFAULT_OUTLINE_STROKE, plot.getBaseSectionOutlineStroke());
        Assert.assertEquals(Color.gray, plot.getShadowPaint());
        Assert.assertEquals(4.0, plot.getShadowXOffset(), 0.001);
        Assert.assertEquals(4.0, plot.getShadowYOffset(), 0.001);
        Assert.assertNotNull(plot.getLabelGenerator());
        Assert.assertEquals(PiePlot.DEFAULT_LABEL_FONT, plot.getLabelFont());
        Assert.assertEquals(PiePlot.DEFAULT_LABEL_PAINT, plot.getLabelPaint());
        Assert.assertEquals(PiePlot.DEFAULT_LABEL_BACKGROUND_PAINT, plot.getLabelBackgroundPaint());
        Assert.assertEquals(PiePlot.DEFAULT_LABEL_OUTLINE_PAINT, plot.getLabelOutlinePaint());
        Assert.assertEquals(PiePlot.DEFAULT_LABEL_OUTLINE_STROKE, plot.getLabelOutlineStroke());
        Assert.assertEquals(PiePlot.DEFAULT_LABEL_SHADOW_PAINT, plot.getLabelShadowPaint());
        Assert.assertTrue(plot.getLabelLinksVisible());
        Assert.assertFalse(plot.getSimpleLabels());
        Assert.assertFalse(plot.getIgnoreNullValues());
        Assert.assertFalse(plot.getIgnoreZeroValues());
        Assert.assertEquals("Pie Plot", plot.getPlotType());
    }

    @Test
    public void testSetDataset() {
        PiePlot plot = new PiePlot();
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Section 1", 10.0);
        plot.setDataset(dataset);
        Assert.assertSame(dataset, plot.getDataset());

        plot.setDataset(null);
        Assert.assertNull(plot.getDataset());
    }

    @Test
    public void testSetPieIndex() {
        PiePlot plot = new PiePlot();
        plot.setPieIndex(2);
        Assert.assertEquals(2, plot.getPieIndex());
    }

    @Test
    public void testStartAngle() {
        PiePlot plot = new PiePlot();
        plot.setStartAngle(45.0);
        Assert.assertEquals(45.0, plot.getStartAngle(), 0.0001);
    }

    @Test
    public void testDirection() {
        PiePlot plot = new PiePlot();
        plot.setDirection(Rotation.ANTICLOCKWISE);
        Assert.assertEquals(Rotation.ANTICLOCKWISE, plot.getDirection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDirectionNull() {
        PiePlot plot = new PiePlot();
        plot.setDirection(null);
    }

    @Test
    public void testInteriorGap() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(0.20);
        Assert.assertEquals(0.20, plot.getInteriorGap(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInteriorGapTooSmall() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(-0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInteriorGapTooLarge() {
        PiePlot plot = new PiePlot();
        plot.setInteriorGap(0.41);
    }

    @Test
    public void testCircular() {
        PiePlot plot = new PiePlot();
        plot.setCircular(false);
        Assert.assertFalse(plot.isCircular());
        plot.setCircular(true, false);
        Assert.assertTrue(plot.isCircular());
    }

    @Test
    public void testIgnoreNullAndZeroValues() {
        PiePlot plot = new PiePlot();
        plot.setIgnoreNullValues(true);
        Assert.assertTrue(plot.getIgnoreNullValues());
        plot.setIgnoreZeroValues(true);
        Assert.assertTrue(plot.getIgnoreZeroValues());
    }

    @Test
    public void testSectionPaint() {
        PiePlot plot = new PiePlot();
        Assert.assertNull(plot.getSectionPaint("Key1"));
        plot.setSectionPaint("Key1", Color.red);
        Assert.assertEquals(Color.red, plot.getSectionPaint("Key1"));
        Assert.assertEquals(Color.red, plot.lookupSectionPaint("Key1"));
        Assert.assertEquals(plot.getBaseSectionPaint(), plot.lookupSectionPaint("NonExisting"));

        plot.setBaseSectionPaint(Color.blue);
        Assert.assertEquals(Color.blue, plot.getBaseSectionPaint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseSectionPaintNull() {
        PiePlot plot = new PiePlot();
        plot.setBaseSectionPaint(null);
    }

    @Test
    public void testSectionOutlines() {
        PiePlot plot = new PiePlot();
        plot.setSectionOutlinesVisible(false);
        Assert.assertFalse(plot.getSectionOutlinesVisible());

        plot.setSectionOutlinePaint("Key1", Color.green);
        Assert.assertEquals(Color.green, plot.getSectionOutlinePaint("Key1"));
        Assert.assertEquals(Color.green, plot.lookupSectionOutlinePaint("Key1"));

        plot.setBaseSectionOutlinePaint(Color.yellow);
        Assert.assertEquals(Color.yellow, plot.getBaseSectionOutlinePaint());

        Stroke stroke = new BasicStroke(2.0f);
        plot.setSectionOutlineStroke("Key1", stroke);
        Assert.assertEquals(stroke, plot.getSectionOutlineStroke("Key1"));
        Assert.assertEquals(stroke, plot.lookupSectionOutlineStroke("Key1"));

        Stroke baseStroke = new BasicStroke(3.0f);
        plot.setBaseSectionOutlineStroke(baseStroke);
        Assert.assertEquals(baseStroke, plot.getBaseSectionOutlineStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseSectionOutlinePaintNull() {
        PiePlot plot = new PiePlot();
        plot.setBaseSectionOutlinePaint(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseSectionOutlineStrokeNull() {
        PiePlot plot = new PiePlot();
        plot.setBaseSectionOutlineStroke(null);
    }

    @Test
    public void testShadowAttributes() {
        PiePlot plot = new PiePlot();
        plot.setShadowPaint(Color.black);
        Assert.assertEquals(Color.black, plot.getShadowPaint());
        plot.setShadowXOffset(5.5);
        Assert.assertEquals(5.5, plot.getShadowXOffset(), 0.001);
        plot.setShadowYOffset(6.5);
        Assert.assertEquals(6.5, plot.getShadowYOffset(), 0.001);
    }

    @Test
    public void testExplodePercentages() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Section 1", 10.0);
        dataset.setValue("Section 2", 20.0);

        PiePlot plot = new PiePlot(dataset);
        Assert.assertEquals(0.0, plot.getExplodePercent("Section 1"), 0.0001);
        Assert.assertEquals(0.0, plot.getMaximumExplodePercent(), 0.0001);

        plot.setExplodePercent("Section 1", 0.3);
        plot.setExplodePercent("Section 2", 0.5);
        Assert.assertEquals(0.3, plot.getExplodePercent("Section 1"), 0.0001);
        Assert.assertEquals(0.5, plot.getExplodePercent("Section 2"), 0.0001);
        Assert.assertEquals(0.5, plot.getMaximumExplodePercent(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExplodePercentNullKey() {
        PiePlot plot = new PiePlot();
        plot.setExplodePercent(null, 0.2);
    }

    @Test
    public void testLabelAttributes() {
        PiePlot plot = new PiePlot();
        PieSectionLabelGenerator labelGen = new StandardPieSectionLabelGenerator("{0}: {1}");
        plot.setLabelGenerator(labelGen);
        Assert.assertSame(labelGen, plot.getLabelGenerator());

        Font font = new Font("Dialog", Font.BOLD, 12);
        plot.setLabelFont(font);
        Assert.assertEquals(font, plot.getLabelFont());

        plot.setLabelPaint(Color.red);
        Assert.assertEquals(Color.red, plot.getLabelPaint());

        plot.setLabelBackgroundPaint(Color.white);
        Assert.assertEquals(Color.white, plot.getLabelBackgroundPaint());

        plot.setLabelOutlinePaint(Color.cyan);
        Assert.assertEquals(Color.cyan, plot.getLabelOutlinePaint());

        Stroke st = new BasicStroke(1.2f);
        plot.setLabelOutlineStroke(st);
        Assert.assertEquals(st, plot.getLabelOutlineStroke());

        plot.setLabelShadowPaint(Color.magenta);
        Assert.assertEquals(Color.magenta, plot.getLabelShadowPaint());

        RectangleInsets insets = new RectangleInsets(3, 3, 3, 3);
        plot.setLabelPadding(insets);
        Assert.assertEquals(insets, plot.getLabelPadding());

        plot.setSimpleLabels(true);
        Assert.assertTrue(plot.getSimpleLabels());

        RectangleInsets simpleOffset = new RectangleInsets(0.1, 0.1, 0.1, 0.1);
        plot.setSimpleLabelOffset(simpleOffset);
        Assert.assertEquals(simpleOffset, plot.getSimpleLabelOffset());

        plot.setLabelGap(0.05);
        Assert.assertEquals(0.05, plot.getLabelGap(), 0.0001);

        plot.setMaximumLabelWidth(0.25);
        Assert.assertEquals(0.25, plot.getMaximumLabelWidth(), 0.0001);

        plot.setLabelLinksVisible(false);
        Assert.assertFalse(plot.getLabelLinksVisible());

        plot.setLabelLinkMargin(0.08);
        Assert.assertEquals(0.08, plot.getLabelLinkMargin(), 0.0001);

        plot.setLabelLinkPaint(Color.pink);
        Assert.assertEquals(Color.pink, plot.getLabelLinkPaint());

        Stroke linkStroke = new BasicStroke(0.8f);
        plot.setLabelLinkStroke(linkStroke);
        Assert.assertEquals(linkStroke, plot.getLabelLinkStroke());

        PieLabelDistributor distributor = new PieLabelDistributor(5);
        plot.setLabelDistributor(distributor);
        Assert.assertSame(distributor, plot.getLabelDistributor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelFontNull() {
        new PiePlot().setLabelFont(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaintNull() {
        new PiePlot().setLabelPaint(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaddingNull() {
        new PiePlot().setLabelPadding(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSimpleLabelOffsetNull() {
        new PiePlot().setSimpleLabelOffset(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelDistributorNull() {
        new PiePlot().setLabelDistributor(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelLinkPaintNull() {
        new PiePlot().setLabelLinkPaint(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelLinkStrokeNull() {
        new PiePlot().setLabelLinkStroke(null);
    }

    @Test
    public void testToolTipAndUrlGenerators() {
        PiePlot plot = new PiePlot();
        PieToolTipGenerator toolTipGen = new StandardPieToolTipGenerator();
        plot.setToolTipGenerator(toolTipGen);
        Assert.assertSame(toolTipGen, plot.getToolTipGenerator());

        PieURLGenerator urlGen = new StandardPieURLGenerator("item.jsp");
        plot.setURLGenerator(urlGen);
        Assert.assertSame(urlGen, plot.getURLGenerator());
    }

    @Test
    public void testLegendAttributes() {
        PiePlot plot = new PiePlot();
        plot.setLegendItemShape(new Rectangle(0, 0, 10, 10));
        Assert.assertNotNull(plot.getLegendItemShape());

        PieSectionLabelGenerator legendGen = new StandardPieSectionLabelGenerator("{0}");
        plot.setLegendLabelGenerator(legendGen);
        Assert.assertSame(legendGen, plot.getLegendLabelGenerator());

        PieSectionLabelGenerator ttGen = new StandardPieSectionLabelGenerator("{1}");
        plot.setLegendLabelToolTipGenerator(ttGen);
        Assert.assertSame(ttGen, plot.getLegendLabelToolTipGenerator());

        PieURLGenerator urlGen = new StandardPieURLGenerator("legend.jsp");
        plot.setLegendLabelURLGenerator(urlGen);
        Assert.assertSame(urlGen, plot.getLegendLabelURLGenerator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLegendItemShapeNull() {
        new PiePlot().setLegendItemShape(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLegendLabelGeneratorNull() {
        new PiePlot().setLegendLabelGenerator(null);
    }

    @Test
    public void testGetLegendItems() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Section 1", 10.0);
        dataset.setValue("Section 2", 0.0);
        dataset.setValue("Section 3", null);
        dataset.setValue("Section 4", 20.0);

        PiePlot plot = new PiePlot(dataset);
        plot.setLegendLabelToolTipGenerator(new StandardPieSectionLabelGenerator());
        plot.setLegendLabelURLGenerator(new StandardPieURLGenerator("test.jsp"));

        // Defaults: null and zero included
        LegendItemCollection items = plot.getLegendItems();
        Assert.assertEquals(4, items.getItemCount());

        // Ignore null values
        plot.setIgnoreNullValues(true);
        items = plot.getLegendItems();
        Assert.assertEquals(3, items.getItemCount());

        // Ignore zero values
        plot.setIgnoreZeroValues(true);
        items = plot.getLegendItems();
        Assert.assertEquals(2, items.getItemCount());

        // Null dataset
        plot.setDataset(null);
        items = plot.getLegendItems();
        Assert.assertEquals(0, items.getItemCount());
    }

    @Test
    public void testGetSectionKey() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 20.0);

        PiePlot plot = new PiePlot(dataset);
        Assert.assertEquals("A", plot.getSectionKey(0));
        Assert.assertEquals("B", plot.getSectionKey(1));
        Assert.assertEquals(new Integer(5), plot.getSectionKey(5));

        plot.setDataset(null);
        Assert.assertEquals(new Integer(0), plot.getSectionKey(0));
    }

    @Test
    public void testGetArcBounds() {
        PiePlot plot = new PiePlot();
        Rectangle2D unexploded = new Rectangle2D.Double(10, 10, 100, 100);
        Rectangle2D exploded = new Rectangle2D.Double(0, 0, 120, 120);

        Rectangle2D bounds0 = plot.getArcBounds(unexploded, exploded, 0.0, 90.0, 0.0);
        Assert.assertEquals(unexploded, bounds0);

        Rectangle2D boundsExploded = plot.getArcBounds(unexploded, exploded, 0.0, 90.0, 0.5);
        Assert.assertNotNull(boundsExploded);
        Assert.assertEquals(100.0, boundsExploded.getWidth(), 0.0001);
        Assert.assertEquals(100.0, boundsExploded.getHeight(), 0.0001);
    }

    @Test
    public void testDrawRendering() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 25.0);
        dataset.setValue("B", 25.0);
        dataset.setValue("C", 25.0);
        dataset.setValue("D", 25.0);
        dataset.setValue("Zero", 0.0);
        dataset.setValue("Null", null);

        PiePlot plot = new PiePlot(dataset);
        plot.setExplodePercent("A", 0.2);
        plot.setToolTipGenerator(new StandardPieToolTipGenerator());
        plot.setURLGenerator(new StandardPieURLGenerator());

        JFreeChart chart = new JFreeChart(plot);
        BufferedImage image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);

        // Anticlockwise drawing
        plot.setDirection(Rotation.ANTICLOCKWISE);
        image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);

        // Simple labels drawing
        plot.setSimpleLabels(true);
        image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);

        // Non-circular
        plot.setCircular(false);
        image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);

        // Null dataset drawing
        plot.setDataset(null);
        image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);

        // Empty dataset drawing
        plot.setDataset(new DefaultPieDataset());
        image = chart.createBufferedImage(400, 300);
        Assert.assertNotNull(image);
    }

    @Test
    public void testEqualsAndCloning() throws Exception {
        PiePlot p1 = new PiePlot();
        PiePlot p2 = new PiePlot();
        Assert.assertEquals(p1, p2);

        p1.setStartAngle(123.0);
        Assert.assertNotEquals(p1, p2);
        p2.setStartAngle(123.0);
        Assert.assertEquals(p1, p2);

        p1.setCircular(false);
        Assert.assertNotEquals(p1, p2);
        p2.setCircular(false);
        Assert.assertEquals(p1, p2);

        p1.setDirection(Rotation.ANTICLOCKWISE);
        Assert.assertNotEquals(p1, p2);
        p2.setDirection(Rotation.ANTICLOCKWISE);
        Assert.assertEquals(p1, p2);

        p1.setInteriorGap(0.15);
        Assert.assertNotEquals(p1, p2);
        p2.setInteriorGap(0.15);
        Assert.assertEquals(p1, p2);

        p1.setExplodePercent("Key1", 0.1);
        Assert.assertNotEquals(p1, p2);
        p2.setExplodePercent("Key1", 0.1);
        Assert.assertEquals(p1, p2);

        p1.setSimpleLabels(true);
        Assert.assertNotEquals(p1, p2);
        p2.setSimpleLabels(true);
        Assert.assertEquals(p1, p2);

        p1.setLabelLinksVisible(false);
        Assert.assertNotEquals(p1, p2);
        p2.setLabelLinksVisible(false);
        Assert.assertEquals(p1, p2);

        p1.setIgnoreNullValues(true);
        Assert.assertNotEquals(p1, p2);
        p2.setIgnoreNullValues(true);
        Assert.assertEquals(p1, p2);

        p1.setIgnoreZeroValues(true);
        Assert.assertNotEquals(p1, p2);
        p2.setIgnoreZeroValues(true);
        Assert.assertEquals(p1, p2);

        p1.setPieIndex(3);
        Assert.assertNotEquals(p1, p2);
        p2.setPieIndex(3);
        Assert.assertEquals(p1, p2);

        // Clone test
        PiePlot cloned = (PiePlot) p1.clone();
        Assert.assertEquals(p1, cloned);
        Assert.assertNotSame(p1, cloned);
    }

    @Test
    public void testSerialization() throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 20.0);

        PiePlot p1 = new PiePlot(dataset);
        p1.setSectionPaint("A", Color.red);
        p1.setSectionOutlinePaint("A", Color.blue);
        p1.setSectionOutlineStroke("A", new BasicStroke(1.5f));
        p1.setExplodePercent("A", 0.25);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(p1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        PiePlot p2 = (PiePlot) in.readObject();
        in.close();

        Assert.assertEquals(p1, p2);
    }
}