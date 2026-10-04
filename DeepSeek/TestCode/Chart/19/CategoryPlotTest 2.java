package org.jfree.chart.plot;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.chart.LegendItemCollection;

public class CategoryPlotTest {

    private CategoryPlot plot;
    private CategoryAxis domainAxis;
    private ValueAxis rangeAxis;
    private DefaultCategoryDataset dataset;
    private CategoryItemRenderer renderer;

    @Before
    public void setUp() {
        plot = new CategoryPlot();
        domainAxis = new CategoryAxis("Domain");
        rangeAxis = new NumberAxis("Range");
        dataset = new DefaultCategoryDataset();
        renderer = new BarRenderer();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(plot);
        assertEquals(PlotOrientation.VERTICAL, plot.getOrientation());
        assertTrue(plot.getDataset() == null);
        assertTrue(plot.getDomainAxis() == null);
        assertTrue(plot.getRangeAxis() == null);
        assertFalse(plot.isDomainGridlinesVisible());
        assertTrue(plot.isRangeGridlinesVisible());
        assertEquals(-3537691700434728188L, 0); // nothing to check
    }

    @Test
    public void testParameterizedConstructor() {
        CategoryPlot p = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        assertEquals(dataset, p.getDataset());
        assertEquals(domainAxis, p.getDomainAxis());
        assertEquals(rangeAxis, p.getRangeAxis());
        assertEquals(renderer, p.getRenderer());
    }

    @Test
    public void testGetPlotType() {
        assertEquals("Category_Plot", plot.getPlotType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientationNull() {
        plot.setOrientation(null);
    }

    @Test
    public void testSetOrientation() {
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetAxisOffsetNull() {
        plot.setAxisOffset(null);
    }

    @Test
    public void testSetAxisOffset() {
        RectangleInsets offset = new RectangleInsets(1.0, 2.0, 3.0, 4.0);
        plot.setAxisOffset(offset);
        assertEquals(offset, plot.getAxisOffset());
    }

    @Test
    public void testGetDomainAxisNull() {
        assertNull(plot.getDomainAxis());
    }

    @Test
    public void testSetDomainAxis() {
        plot.setDomainAxis(domainAxis);
        assertEquals(domainAxis, plot.getDomainAxis());
    }

    @Test
    public void testSetDomainAxisWithNotification() {
        PlotChangeListener listener = new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent e) {}
        };
        plot.addChangeListener(listener);
        plot.setDomainAxis(domainAxis);
        // we can't easily test that notification was sent, but method should not throw
    }

    @Test
    public void testSetDomainAxisNull() {
        plot.setDomainAxis(domainAxis);
        plot.setDomainAxis(null);
        assertNull(plot.getDomainAxis());
    }

    @Test
    public void testGetDomainAxisIndex() {
        plot.setDomainAxis(domainAxis);
        assertEquals(0, plot.getDomainAxisIndex(domainAxis));
    }

    @Test
    public void testGetDomainAxisLocationDefault() {
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainAxisLocationIndex0Null() {
        plot.setDomainAxisLocation(0, null);
    }

    @Test
    public void testSetDomainAxisLocation() {
        plot.setDomainAxisLocation(0, AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation(0));
    }

    @Test
    public void testGetDomainAxisEdge() {
        assertEquals(plot.getDomainAxisEdge(), Plot.resolveDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT, PlotOrientation.VERTICAL));
    }

    @Test
    public void testGetDomainAxisCount() {
        assertEquals(1, plot.getDomainAxisCount());
        plot.setDomainAxis(1, new CategoryAxis("Second"));
        assertEquals(2, plot.getDomainAxisCount());
    }

    @Test
    public void testClearDomainAxes() {
        plot.setDomainAxis(domainAxis);
        plot.clearDomainAxes();
        assertEquals(0, plot.getDomainAxesCount());
        assertNull(plot.getDomainAxis());
    }

    @Test
    public void testSetDomainAxes() {
        CategoryAxis a1 = new CategoryAxis("A1");
        CategoryAxis a2 = new CategoryAxis("A2");
        plot.setDomainAxes(new CategoryAxis[]{a1, a2});
        assertEquals(a1, plot.getDomainAxis(0));
        assertEquals(a2, plot.getDomainAxis(1));
    }

    @Test
    public void testRangeAxis() {
        plot.setRangeAxis(rangeAxis);
        assertEquals(rangeAxis, plot.getRangeAxis());
        plot.setRangeAxis(null);
        assertNull(plot.getRangeAxis());
    }

    @Test
    public void testGetRangeAxisIndex() {
        plot.setRangeAxis(rangeAxis);
        assertEquals(0, plot.getRangeAxisIndex(rangeAxis));
        NumberAxis other = new NumberAxis("Other");
        assertEquals(-1, plot.getRangeAxisIndex(other));
    }

    @Test
    public void testSetRangeAxes() {
        NumberAxis a1 = new NumberAxis("A1");
        NumberAxis a2 = new NumberAxis("A2");
        plot.setRangeAxes(new ValueAxis[]{a1, a2});
        assertEquals(a1, plot.getRangeAxis(0));
        assertEquals(a2, plot.getRangeAxis(1));
    }

    @Test
    public void testClearRangeAxes() {
        plot.setRangeAxis(rangeAxis);
        plot.clearRangeAxes();
        assertEquals(0, plot.getRangeAxesCount());
        assertNull(plot.getRangeAxis());
    }

    @Test
    public void testDataset() {
        plot.setDataset(dataset);
        assertEquals(dataset, plot.getDataset());
        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    @Test
    public void testGetDatasetCount() {
        assertEquals(1, plot.getDatasetCount());
        plot.setDataset(1, dataset);
        assertEquals(2, plot.getDatasetCount());
    }

    @Test
    public void testMapDatasetToDomainAxis() {
        plot.setDataset(dataset);
        plot.mapDatasetToDomainAxis(0, 0);
        assertEquals(plot.getDomainAxis(), plot.getDomainAxisForDataset(0));
    }

    @Test
    public void testMapDatasetToRangeAxis() {
        plot.setDataset(dataset);
        plot.setRangeAxis(rangeAxis);
        plot.mapDatasetToRangeAxis(0, 0);
        assertEquals(rangeAxis, plot.getRangeAxisForDataset(0));
    }

    @Test
    public void testRenderer() {
        plot.setRenderer(renderer);
        assertEquals(renderer, plot.getRenderer());
        plot.setRenderer(null);
        assertNull(plot.getRenderer());
    }

    @Test
    public void testSetRendererWithIndex() {
        plot.setDataset(dataset);
        BarRenderer r2 = new BarRenderer();
        plot.setRenderer(0, r2);
        assertEquals(r2, plot.getRenderer());
    }

    @Test
    public void testSetRenderersArray() {
        BarRenderer r1 = new BarRenderer();
        BarRenderer r2 = new BarRenderer();
        plot.setRenderers(new CategoryItemRenderer[]{r1, r2});
        assertEquals(r1, plot.getRenderer(0));
        assertEquals(r2, plot.getRenderer(1));
    }

    @Test
    public void testGetRendererForDataset() {
        plot.setDataset(dataset);
        plot.setRenderer(renderer);
        assertEquals(renderer, plot.getRendererForDataset(dataset));
    }

    @Test
    public void testGetIndexOf() {
        plot.setRenderer(renderer);
        assertEquals(0, plot.getIndexOf(renderer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDatasetRenderingOrderNull() {
        plot.setDatasetRenderingOrder(null);
    }

    @Test
    public void testDatasetRenderingOrder() {
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        assertEquals(DatasetRenderingOrder.FORWARD, plot.getDatasetRenderingOrder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetColumnRenderingOrderNull() {
        plot.setColumnRenderingOrder(null);
    }

    @Test
    public void testColumnRenderingOrder() {
        plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        assertEquals(SortOrder.DESCENDING, plot.getColumnRenderingOrder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRowRenderingOrderNull() {
        plot.setRowRenderingOrder(null);
    }

    @Test
    public void testRowRenderingOrder() {
        plot.setRowRenderingOrder(SortOrder.DESCENDING);
        assertEquals(SortOrder.DESCENDING, plot.getRowRenderingOrder());
    }

    @Test
    public void testDomainGridlinesVisible() {
        plot.setDomainGridlinesVisible(true);
        assertTrue(plot.isDomainGridlinesVisible());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainGridlinePositionNull() {
        plot.setDomainGridlinePosition(null);
    }

    @Test
    public void testDomainGridlinePosition() {
        plot.setDomainGridlinePosition(CategoryAnchor.START);
        assertEquals(CategoryAnchor.START, plot.getDomainGridlinePosition());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainGridlineStrokeNull() {
        plot.setDomainGridlineStroke(null);
    }

    @Test
    public void testDomainGridlineStroke() {
        Stroke s = new BasicStroke(1.0f);
        plot.setDomainGridlineStroke(s);
        assertEquals(s, plot.getDomainGridlineStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDomainGridlinePaintNull() {
        plot.setDomainGridlinePaint(null);
    }

    @Test
    public void testDomainGridlinePaint() {
        plot.setDomainGridlinePaint(Color.RED);
        assertEquals(Color.RED, plot.getDomainGridlinePaint());
    }

    @Test
    public void testRangeGridlinesVisible() {
        plot.setRangeGridlinesVisible(false);
        assertFalse(plot.isRangeGridlinesVisible());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeGridlineStrokeNull() {
        plot.setRangeGridlineStroke(null);
    }

    @Test
    public void testRangeGridlineStroke() {
        Stroke s = new BasicStroke(2.0f);
        plot.setRangeGridlineStroke(s);
        assertEquals(s, plot.getRangeGridlineStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeGridlinePaintNull() {
        plot.setRangeGridlinePaint(null);
    }

    @Test
    public void testRangeGridlinePaint() {
        plot.setRangeGridlinePaint(Color.BLUE);
        assertEquals(Color.BLUE, plot.getRangeGridlinePaint();
    }

    @Test
    public void testFixedLegendItems() {
        LegendItemCollection items = new LegendItemCollection();
        plot.setFixedLegendItems(items);
        assertEquals(items, plot.getFixedLegendItems());
    }

    @Test
    public void testGetLegendItemsNonNull() {
        assertNotNull(plot.getLegendItems());
    }

    @Test
    public void testAnchorValue() {
        plot.setAnchorValue(5.0);
        assertEquals(5.0, plot.getAnchorValue(), 0.0);
    }

    @Test
    public void testRangeCrosshair() {
        assertFalse(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairValue(10.0);
        assertEquals(10.0, plot.getRangeCrosshairValue(), 0.0);
        Stroke s = new BasicStroke(1.5f);
        plot.setRangeCrosshairStroke(s);
        assertEquals(s, plot.getRangeCrosshairStroke());
        plot.setRangeCrosshairPaint(Color.GREEN);
        assertEquals(Color.GREEN, plot.getRangeCrosshairPaint());
        assertTrue(plot.isRangeCrosshairLockedOnData());
        plot.setRangeCrosshairLockedOnData(false);
        assertFalse(plot.isRangeCrosshairLockedOnData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeCrosshairStrokeNull() {
        plot.setRangeCrosshairStroke(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeCrosshairPaintNull() {
        plot.setRangeCrosshairPaint(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDomainMarkerNullMarker() {
        plot.addDomainMarker(null, Layer.FOREGROUND);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDomainMarkerNullLayer() {
        plot.addDomainMarker(new CategoryMarker("A"), null);
    }

    @Test
    public void testAddDomainMarker() {
        CategoryMarker marker = new CategoryMarker("Cat");
        plot.addDomainMarker(marker, Layer.FOREGROUND);
        Collection markers = plot.getDomainMarkers(Layer.FOREGROUND);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testClearDomainMarkers() {
        plot.addDomainMarker(new CategoryMarker("A"), Layer.BACKGROUND);
        plot.clearDomainMarkers();
        assertNull(plot.getDomainMarkers(Layer.BACKGROUND));
        assertNull(plot.getDomainMarkers(Layer.FOREGROUND));
    }

    @Test
    public void testClearDomainMarkersByIndex() {
        plot.addDomainMarker(new CategoryMarker("A"), Layer.FOREGROUND);
        plot.clearDomainMarkers(0);
        assertNull(plot.getDomainMarkers(0, Layer.FOREGROUND));
    }

    @Test
    public void testAddRangeMarker() {
        Marker marker = new ValueMarker(2.0);
        plot.addRangeMarker(marker, Layer.BACKGROUND);
        Collection markers = plot.getRangeMarkers(Layer.BACKGROUND);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testClearRangeMarkers() {
        plot.addRangeMarker(new ValueMarker(3.0), Layer.FOREGROUND);
        plot.clearRangeMarkers();
        assertNull(plot.getRangeMarkers(Layer.FOREGROUND));
    }

    @Test
    public void testClearRangeMarkersByIndex() {
        plot.addRangeMarker(0, new ValueMarker(4.0), Layer.FOREGROUND);
        plot.clearRangeMarkers(0);
        assertNull(plot.getRangeMarkers(0, Layer.FOREGROUND));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAnnotationNull() {
        plot.addAnnotation(null);
    }

    @Test
    public void testAddRemoveAnnotation() {
        CategoryAnnotation ann = new CategoryAnnotation() {
            // dummy implementation
            public void draw(java.awt.Graphics2D g2, CategoryPlot plot, java.awt.geom.Rectangle2D dataArea, CategoryAxis domainAxis, ValueAxis rangeAxis) {}
            public void draw(java.awt.Graphics2D g2, CategoryPlot plot, java.awt.geom.Rectangle2D dataArea, CategoryAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {}
        };
        plot.addAnnotation(ann);
        assertTrue(plot.getAnnotations().contains(ann));
        assertTrue(plot.removeAnnotation(ann));
        assertFalse(plot.getAnnotations().contains(ann));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAnnotationNull() {
        plot.removeAnnotation(null);
    }

    @Test
    public void testClearAnnotations() {
        plot.addAnnotation(new TextAnnotation("test"));
        plot.clearAnnotations();
        assertTrue(plot.getAnnotations().isEmpty());
    }

    @Test
    public void testWeight() {
        plot.setWeight(5);
        assertEquals(5, plot.getWeight());
    }

    @Test
    public void testFixedDomainAxisSpace() {
        AxisSpace space = new AxisSpace();
        plot.setFixedDomainAxisSpace(space);
        assertEquals(space, plot.getFixedDomainAxisSpace());
    }

    @Test
    public void testFixedRangeAxisSpace() {
        AxisSpace space = new AxisSpace();
        plot.setFixedRangeAxisSpace(space);
        assertEquals(space, plot.getFixedRangeAxisSpace());
    }

    @Test
    public void testGetCategoriesNoDataset() {
        assertNull(plot.getCategories());
    }

    @Test
    public void testGetCategoriesWithDataset() {
        dataset.addValue(1.0, "Row", "Col");
        plot.setDataset(dataset);
        List categories = plot.getCategories();
        assertNotNull(categories);
        assertTrue(categories.contains("Col"));
    }

    @Test
    public void testGetCategoriesForAxis() {
        dataset.addValue(1.0, "R", "C");
        plot.setDataset(dataset);
        plot.setDomainAxis(domainAxis);
        List cats = plot.getCategoriesForAxis(domainAxis);
        assertTrue(cats.contains("C"));
    }

    @Test
    public void testDrawSharedDomainAxis() {
        assertFalse(plot.getDrawSharedDomainAxis());
        plot.setDrawSharedDomainAxis(true);
        assertTrue(plot.getDrawSharedDomainAxis());
    }

    @Test
    public void testIsDomainZoomable() {
        assertFalse(plot.isDomainZoomable());
    }

    @Test
    public void testIsRangeZoomable() {
        assertTrue(plot.isRangeZoomable());
    }

    @Test
    public void testZoomDomainAxesNoOp() {
        plot.zoomDomainAxes(2.0, null, null);
        // no exception
    }

    @Test
    public void testZoomRangeAxes() {
        plot.setRangeAxis(new NumberAxis("Y"));
        plot.zoomRangeAxes(2.0, null, null);
        // no exception
    }

    @Test
    public void testEquals() {
        CategoryPlot p1 = new CategoryPlot();
        CategoryPlot p2 = new CategoryPlot();
        assertTrue(p1.equals(p2));
        p2.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        plot.setDataset(dataset);
        plot.setRenderer(renderer);
        CategoryPlot clone = (CategoryPlot) plot.clone();
        assertNotSame(plot, clone);
        assertNotSame(plot.getDomainAxis(), clone.getDomainAxis());
        assertNotSame(plot.getRangeAxis(), clone.getRangeAxis());
        assertNotSame(plot.getDataset(), clone.getDataset());
        assertNotSame(plot.getRenderer(), clone.getRenderer());
    }

    @Test
    public void testDatasetChanged() {
        PlotChangeListener listener = new PlotChangeListener() {
            public void plotChanged(PlotChangeEvent e) {
                // dummy
            }
        };
        plot.addChangeListener(listener);
        plot.datasetChanged(new DatasetChangeEvent(this, dataset));
        // no exception
    }

    @Test
    public void testRendererChanged() {
        plot.rendererChanged(new RendererChangeEvent(renderer));
        // no exception
    }

    // edge case: setDomainAxisLocation with null for index>0 allowed
    @Test
    public void testSetDomainAxisLocationNullForNonZeroIndex() {
        plot.setDomainAxisLocation(1, null, false);
        assertNull(plot.getDomainAxisLocation(1));
    }

    @Test
    public void testSetRangeAxisLocationNullForNonZeroIndex() {
        plot.setRangeAxisLocation(1, null, false);
        assertNull(plot.getRangeAxisLocation(1));
    }
}
```
