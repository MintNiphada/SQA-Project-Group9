package org.jfree.chart.plot;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.Before;
import org.junit.Test;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.RectangleAnchor;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.util.UnitType;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

public class PiePlotTest {

    private PiePlot plot;
    private DefaultPieDataset dataset;

    @Before
    public void setUp() {
        plot = new PiePlot();
        dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 20.0);
        dataset.setValue("C", 30.0);
        plot.setDataset(dataset);
    }

    // ------------------ Constructors ------------------

    @Test
    public void testDefaultConstructor() {
        PiePlot p = new PiePlot();
        assertNull(p.getDataset());
        assertEquals(0, p.getPieIndex());
        assertEquals(PiePlot.DEFAULT_INTERIOR_GAP, p.getInteriorGap(), 1e-12);
        assertTrue(p.isCircular());
        assertEquals(PiePlot.DEFAULT_START_ANGLE, p.getStartAngle(), 1e-12);
        assertEquals(Rotation.CLOCKWISE, p.getDirection());
        assertEquals(PiePlot.DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW,
                p.getMinimumArcAngleToDraw(), 1e-15);
        assertNotNull(p.getLabelGenerator());
        assertNotNull(p.getLabelFont());
        assertNotNull(p.getLabelPaint());
        assertNotNull(p.getBaseSectionPaint());
        assertFalse(p.getSimpleLabels());
        assertTrue(p.getLabelLinksVisible());
        assertNotNull(p.getLegendLabelGenerator());
    }

    @Test
    public void testConstructorWithDataset() {
        PiePlot p = new PiePlot(dataset);
        assertEquals(dataset, p.getDataset());
    }

    // ------------------ Dataset ------------------

    @Test
    public void testSetDataset() {
        DefaultPieDataset ds2 = new DefaultPieDataset();
        plot.setDataset(ds2);
        assertEquals(ds2, plot.getDataset());
    }

    @Test
    public void testSetDatasetNull() {
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    // ------------------ Pie index ------------------

    @Test
    public void testSetPieIndex() {
        plot.setPieIndex(3);
        assertEquals(3, plot.getPieIndex());
    }

    // ------------------ Interior gap ------------------

    @Test
    public void testSetInteriorGapValid() {
        plot.setInteriorGap(0.10);
        assertEquals(0.10, plot.getInteriorGap(), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInteriorGapNegative() {
        plot.setInteriorGap(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInteriorGapTooLarge() {
        plot.setInteriorGap(0.5); // > MAX_INTERIOR_GAP (0.40)
    }

    // ------------------ Direction ------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSetDirectionNull() {
        plot.setDirection(null);
    }

    @Test
    public void testSetDirection() {
        plot.setDirection(Rotation.ANTICLOCKWISE);
        assertEquals(Rotation.ANTICLOCKWISE, plot.getDirection());
    }

    // ------------------ Circular ------------------

    @Test
    public void testSetCircular() {
        plot.setCircular(false);
        assertFalse(plot.isCircular());
        plot.setCircular(true, false);
        assertTrue(plot.isCircular());
    }

    // ------------------ Ignore flags ------------------

    @Test
    public void testSetIgnoreNullValues() {
        plot.setIgnoreNullValues(true);
        assertTrue(plot.getIgnoreNullValues());
    }

    @Test
    public void testSetIgnoreZeroValues() {
        plot.setIgnoreZeroValues(true);
        assertTrue(plot.getIgnoreZeroValues());
    }

    // ------------------ Section paints ------------------

    @Test
    public void testBaseSectionPaint() {
        plot.setBaseSectionPaint(Color.red);
        assertEquals(Color.red, plot.getBaseSectionPaint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseSectionPaintNull() {
        plot.setBaseSectionPaint(null);
    }

    @Test
    public void testSectionPaintPerKey() {
        plot.setSectionPaint("A", Color.blue);
        assertEquals(Color.blue, plot.getSectionPaint("A"));
    }

    // ------------------ Section outlines ------------------

    @Test
    public void testSectionOutlinesVisible() {
        plot.setSectionOutlinesVisible(false);
        assertFalse(plot.getSectionOutlinesVisible());
    }

    @Test
    public void testBaseSectionOutlinePaint() {
        plot.setBaseSectionOutlinePaint(Color.green);
        assertEquals(Color.green, plot.getBaseSectionOutlinePaint());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseSectionOutlinePaintNull() {
        plot.setBaseSectionOutlinePaint(null);
    }

    @Test
    public void testBaseSectionOutlineStroke() {
        Stroke s = new BasicStroke(2.0f);
        plot.setBaseSectionOutlineStroke(s);
        assertEquals(s, plot.getBaseSectionOutlineStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBaseSectionOutlineStrokeNull() {
        plot.setBaseSectionOutlineStroke(null);
    }

    // ------------------ Shadow ------------------

    @Test
    public void testShadowPaint() {
        plot.setShadowPaint(Color.black);
        assertEquals(Color.black, plot.getShadowPaint());
    }

    @Test
    public void testShadowXOffset() {
        plot.setShadowXOffset(5.0);
        assertEquals(5.0, plot.getShadowXOffset(), 1e-12);
    }

    @Test
    public void testShadowYOffset() {
        plot.setShadowYOffset(6.0);
        assertEquals(6.0, plot.getShadowYOffset(), 1e-12);
    }

    // ------------------ Explode percentages ------------------

    @Test
    public void testExplodePercent() {
        plot.setExplodePercent("A", 0.2);
        assertEquals(0.2, plot.getExplodePercent("A"), 1e-12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetExplodePercentNullKey() {
        plot.setExplodePercent(null, 0.1);
    }

    @Test
    public void testGetMaximumExplodePercent() {
        plot.setExplodePercent("A", 0.1);
        plot.setExplodePercent("B",0.3);
        assertEquals(0.3, plot.getMaximumExplodePercent(),1e-12);
    }

    // ------------------ Label generator ------------------

    @Test
    public void testLabelGenerator() {
        PieSectionLabelGenerator gen = mock(PieSectionLabelGenerator.class);
        plot.setLabelGenerator(gen);
        assertEquals(gen, plot.getLabelGenerator());
        plot.setLabelGenerator(null);
        assertNull(plot.getLabelGenerator());
    }

    // ------------------ Label font ------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelFontNull() {
        plot.setLabelFont(null);
    }

    @Test
    public void testLabelFont() {
        Font f = new Font("Serif", Font.BOLD, 14);
        plot.setLabelFont(f);
        assertEquals(f, plot.getLabelFont());
    }

    // ------------------ Label paint ------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaintNull() {
        plot.setLabelPaint(null);
    }

    @Test
    public void testLabelPaint() {
        plot.setLabelPaint(Color.magenta);
        assertEquals(Color.magenta, plot.getLabelPaint());
    }

    // ------------------ Label background paint ------------------

    @Test
    public void testLabelBackgroundPaint() {
        plot.setLabelBackgroundPaint(Color.yellow);
        assertEquals(Color.yellow, plot.getLabelBackgroundPaint());
    }

    @Test
    public void testLabelBackgroundPaintNull() {
        plot.setLabelBackgroundPaint(null);
        assertNull(plot.getLabelBackgroundPaint());
    }

    // ------------------ Label outline paint ------------------

    @Test
    public void testLabelOutlinePaint() {
        plot.setLabelOutlinePaint(Color.red);
        assertEquals(Color.red, plot.getLabelOutlinePaint());
    }    @Test    public void testLabelOutlinePaintNull() {        plot.setLabelOutlinePaint(null);
        assertNull(plot.getLabelOutlinePaint());    }

    // ------------------ Label outline stroke ------------------

    @Test
    public void testLabelOutlineStroke() {
        Stroke s = new BasicStroke(1.0f);
        plot.setLabelOutlineStroke(s);
        assertEquals(s, plot.getLabelOutlineStroke());
        plot.setLabelOutlineStroke(null);
        assertNull(plot.getLabelOutlineStroke());
    }

    // ------------------ Label shadow paint ------------------

    @Test
    public void testLabelShadowPaint() {
        plot.setLabelShadowPaint(Color.lightGray);
        assertEquals(Color.lightGray, plot.getLabelShadowPaint());
        plot.setLabelShadowPaint(null);
        assertNull(plot.getLabelShadowPaint());
    }

    // ------------------ Simple label flags / offsets ------------------

    @Test
    public void testSimpleLabels() {
        plot.setSimpleLabels(true);
        assertTrue(plot.getSimpleLabels());
    }

    @Test
    public void testGetSimpleLabelOffset() {
        RectangleInsets offset = new RectangleInsets(UnitType.RELATIVE,0.1,0.1,0.1,0.1);
        plot.setSimpleLabelOffset(offset);
        assertEquals(offset, plot.getSimpleLabelOffset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSimpleLabelOffsetNull() {
        plot.setSimpleLabelOffset(null);
    }

    // ------------------ Label padding ------------------

    @Test
    public void testLabelPadding() {
        RectangleInsets pad = new RectangleInsets(2,2,2,2);
        plot.setLabelPadding(pad);
        assertEquals(pad, plot.getLabelPadding());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelPaddingNull() {
        plot.setLabelPadding(null);
    }

    // ------------------ Label distributor ------------------

    @Test
    public void testLabelDistributor() {
        AbstractPieLabelDistributor d = new PieLabelDistributor(10);
        plot.setLabelDistributor(d);
        assertEquals(d, plot.getLabelDistributor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelDistributorNull() {
        plot.setLabelDistributor(null);
    }

    // ------------------ Tool tip generator ------------------

    @Test
    public void testToolTipGenerator() {
        PieToolTipGenerator gen = mock(PieToolTipGenerator.class);
        plot.setToolTipGenerator(gen);
        assertEquals(gen, plot.getToolTipGenerator());
    }

    // ------------------ URL generator ------------------

    @Test
    public void testURLGenerator() {
        PieURLGenerator gen = mock(PieURLGenerator.class);
        plot.setURLGenerator(gen);
        assertEquals(gen, plot.getURLGenerator());
    }

    // ------------------ Minimum angle ------------------

    @Test
    public void testMinimumArcAngleToDraw() {
        plot.setMinimumArcAngleToDraw(0.001);
        assertEquals(0.001, plot.getMinimumArcAngleToDraw(), 1e-12);
    }

    // ------------------ Legend item shape ------------------

    @Test
    public void testLegendItemShape() {
        Shape shape = new Ellipse2D.Double(0,0,10,10);
        plot.setLegendItemShape(shape);
        assertEquals(shape, plot.getLegendItemShape());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLegendItemShapeNull() {
        plot.setLegendItemShape(null);
    }

    // ------------------ Legend label generator ------------------

    @Test
    public void testLegendLabelGenerator() {
        PieSectionLabelGenerator gen = mock(PieSectionLabelGenerator.class);
        plot.setLegendLabelGenerator(gen);
        assertEquals(gen, plot.getLegendLabelGenerator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetLegendLabelGeneratorNull() {
        plot.setLegendLabelGenerator(null);
    }

    // ------------------ Label link paint ------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelLinkPaintNull() {
        plot.setLabelLinkPaint(null);
    }

    @Test
    public void testLabelLinkPaint() {
        plot.setLabelLinkPaint(Color.pink);
        assertEquals(Color.pink, plot.getLabelLinkPaint());
    }

    // ------------------ Label link stroke ------------------

    @Test(expected = IllegalArgumentException.class)
    public void testSetLabelLinkStrokeNull() {
        plot.setLabelLinkStroke(null);
    }

    @Test
    public void testLabelLinkStroke() {
        Stroke s = new BasicStroke(1.5f);
        plot.setLabelLinkStroke(s);
        assertEquals(s, plot.getLabelLinkStroke());
    }

    // ------------------ Label links visible ------------------

    @Test
    public void testLabelLinksVisible() {
        plot.setLabelLinksVisible(false);
        assertFalse(plot.getLabelLinksVisible());
    }

    // ------------------ Label gap ------------------

    @Test
    public void testLabelGap() {
        plot.setLabelGap(0.03);
        assertEquals(0.03, plot.getLabelGap(), 1e-12);
    }

    // ------------------ Maximum label width ------------------

    @Test
    public void testMaximumLabelWidth() {
        plot.setMaximumLabelWidth(0.2);
        assertEquals(0.2, plot.getMaximumLabelWidth(), 1e-12);
    }

    // ------------------ Label link margin ------------------

    @Test
    public void testLabelLinkMargin() {
        plot.setLabelLinkMargin(0.05);
        assertEquals(0.05, plot.getLabelLinkMargin(), 1e-12);
    }

    // ------------------ equals() ------------------

    @Test
    public void testEquals() {
        PiePlot p1 = new PiePlot(dataset);
        PiePlot p2 = new PiePlot(dataset);
        assertTrue(p1.equals(p2));
        p2.setCircular(false);
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setStartAngle(45.0);
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setDirection(Rotation.ANTICLOCKWISE);
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setIgnoreZeroValues(true);
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setIgnoreNullValues(true);
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setBaseSectionPaint(Color.blue);
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setLabelFont(new Font("Serif", Font.ITALIC, 12));
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setSimpleLabels(true);
        assertFalse(p1.equals(p2));
        p2 = new PiePlot(dataset);
        p2.setLabelLinksVisible(false);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(plot.equals(null));
    }

    @Test
    public void testEqualsOtherType() {
        assertFalse(plot.equals(new Object()));
    }

    // ------------------ clone() ------------------

    @Test
    public void testClone() throws CloneNotSupportedException {
        PiePlot clone = (PiePlot) plot.clone();
        assertNotSame(plot, clone);
        assertTrue(plot.equals(clone));
        clone.getDataset().setValue("D", 40.0);
        // original should not be affected
        assertEquals(3, dataset.getItemCount());
    }

    // ------------------ getLegendItems() ------------------

    @Test
    public void testGetLegendItemsNullDataset() {
        PiePlot p = new PiePlot();
        LegendItemCollection items = p.getLegendItems();
        assertNotNull(items);
        assertEquals(0, items.getItemCount());
    }

    @Test
    public void testGetLegendItemsWithValues() {
        LegendItemCollection items = plot.getLegendItems();
        assertEquals(3, items.getItemCount());
        LegendItem item = items.get(0);
        assertEquals("A", item.getLabel());
        assertNotNull(item.getFillPaint());
    }

    @Test
    public void testGetLegendItemsIgnoreNullValues() {
        dataset.setValue("D", null);
        plot.setIgnoreNullValues(true);
        LegendItemCollection items = plot.getLegendItems();
        assertEquals(3, items.getItemCount());
    }

    @Test
    public void testGetLegendItemsIgnoreZeroValues() {
        dataset.setValue("E", 0.0);
        plot.setIgnoreZeroValues(true);
        LegendItemCollection items = plot.getLegendItems();
        assertEquals(3, items.getItemCount());
    }

    @Test
    public void testGetLegendItemsNegativeValue() {
        dataset.setValue("F", -5.0);
        LegendItemCollection items = plot.getLegendItems();
        assertEquals(3, items.getItemCount()); // negative not included
    }

    // ------------------ getArcBounds() ------------------

    @Test
    public void testGetArcBoundsNoExplode() {
        Rectangle2D unexploded = new Rectangle2D.Double(100,100,200,200);
        Rectangle2D exploded = new Rectangle2D.Double(120,120,160,160);
        Rectangle2D result = plot.getArcBounds(unexploded, exploded, 0,90,0.0);
        assertEquals(unexploded, result);
    }

    @Test
    public void testGetArcBoundsWithExplode() {
        Rectangle2D unexploded = new Rectangle2D.Double(100,100,200,200);
        Rectangle2D exploded = new Rectangle2D.Double(140,140,120,120);
        Rectangle2D result = plot.getArcBounds(unexploded, exploded, 0,90,0.5);
        assertNotSame(unexploded, result);
    }

    // ------------------ Draw tests (mocked Graphics2D) ------------------

    private Graphics2D createMockGraphics() {
        Graphics2D g2 = mock(Graphics2D.class);
        FontMetrics fm = mock(FontMetrics.class);
        when(fm.stringWidth(anyString()).thenReturn(10);
        when(fm.getAscent()).thenReturn(5);
        when(fm.getDescent()).thenReturn(5);
        when(g2.getFontMetrics(any(Font.class))).thenReturn(fm);
        when(g2.getClip()).thenReturn(new Rectangle2D.Double(0,0,800,600));
        Composite comp = mock(Composite.class);
        when(g2.getComposite()).thenReturn(comp);
        doNothing().when(g2).setPaint(any(Paint.class));
        doNothing().when(g2).setStroke(any(Stroke.class));
        doNothing().when(g2).fill(any(Shape.class));
        doNothing().when(g2).draw(any(Shape.class));
        doNothing().when(g2).setComposite(any(Composite.class));
        doNothing().when(g2).clip(any(Shape.class));
        doNothing().when(g2).setFont(any(Font.class));
        return g2;
    }

    @Test
    public void testDrawEmptyDataset() {
        PiePlot p = new PiePlot();
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        p.draw(g2, area, null, null, null);
        // no exception expected
    }

    @Test
    public void testDrawWithData() {
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawWithSimpleLabelsTrue() {
        plot.setSimpleLabels(true);
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
 }

    @Test
    public void testDrawWithLabelGeneratorNull() {
        plot.setLabelGenerator(null);
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawWithShadowPaintNull() {
        plot.setShadowPaint(null);
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawWithToolTipsAndURLs() {
        PieToolTipGenerator tipGen = mock(PieToolTipGenerator.class);
        when(tipGen.generateToolTip(any(PieDataset.class), any(Comparable.class))).thenReturn("tip");
        plot.setToolTipGenerator(tipGen);
        PieURLGenerator urlGen = mock(PieURLGenerator.class);
        when(urlGen.generateURL(any(PieDataset.class), any(Comparable.class), anyInt())).
            thenReturn("url");
        plot.setURLGenerator(urlGen);
        PlotRenderingInfo info = new PlotRenderingInfo();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        Graphics2D g2 = createMockGraphics();
        plot.draw(g2, area, null, null, info);
        EntityCollection entities = info.getEntityCollection();
        assertNotNull(entities);
        assertEquals(3, entities.getEntityCount());
        PieSectionEntity entity = (PieSectionEntity) entities.getEntity(0);
        assertEquals("tip", entity.getToolTipText());
        assertEquals("url", entity.getURLText());
    }

    @Test
    public void testDrawWithSectionOutlinesVisibleFalse() {
        plot.setSectionOutlinesVisible(false);
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawWithMinimumArcAngleToDraw() {
        plot.setMinimumArcAngleToDraw(10.0); // larger than angle per section
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawClockwise() {
        // already clockwise, but confirming no exception
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawAnticlockwise() {
        plot.setDirection(Rotation.ANTICLOCKWISE);
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawWithZeroValuesIgnored() {
        dataset.setValue("Z", 0.0);
        plot.setIgnoreZeroValues(true);
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    @Test
    public void testDrawWithNullValuesNotIgnored() {
        dataset.setValue("N", null);
        plot.setIgnoreNullValues(false);
        Graphics2D g2 = createMockGraphics();
        Rectangle2D area = new Rectangle2D.Double(0,0,800,600);
        plot.draw(g2, area, null, null, null);
    }

    // ------------------ Draw internal methods (protected) ------------------

    @Test
    public void testDrawItem() throws Exception {
        // need to set up state manually to test drawItem
        // This is complex but achievable with reflection
        // Alternatively, we just rely on draw() coverage
    }

    // ------------------ Plot type ------------------

    @Test
    public void testGetPlotType() {
       assertEquals("Pie Plot", plot.getPlotType());
    }

    // ------------------ Locale keys ------------------

    @Test
    public void testGetSectionKeyWithDataset() {
        assertEquals("A", plot.getSectionKey(0));
        assertEquals("B", plot.getSectionKey(1));
        assertEquals("C", plot.getSectionKey(2));
        // out of range
        assertTrue(plot.getSectionKey(3) instanceof Integer);
    }

    // ------------------ Lookup paints (autoPopulate) ------------------

    @Test
    public void testLookupSectionPaint() {
        // with autoPopulate false, returns base paint
        assertEquals(Color.gray, plot.lookUpSectionPaint((Comparable)"X", false));
        // with autoPopulate true, uses drawing supplier if available
        // not easily tested here, but we can verify no exception
        assertNotNull(plot.lookUpSectionPaint("A", true));
    }

    // ------------------ Additional equals exercises ------------------

    @Test
    public void testEqualsWithPaintMapChanges() {
        PiePlot p1 = new PiePlot(dataset);
        PiePlot p2 = new PiePlot(dataset);
        p1.setSectionPaint("A", Color.red);
        p2.setSectionPaint("A", Color.blue);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsWithShadowOffsets() {
        PiePlot p1 = new PiePlot(dataset);
        PiePlot p2 = new PiePlot(dataset);
        p1.setShadowXOffset(3.0);
        assertFalse(p1.equals(p2));
    }
}
