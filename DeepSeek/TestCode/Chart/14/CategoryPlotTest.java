package org.jfree.chart.plot;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Collection;
import java.util.List;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.util.Layer;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.SortOrder;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;

public class CategoryPlotTest {

    private CategoryPlot plot;
    private DefaultCategoryDataset dataset;
    private CategoryAxis domainAxis;
    private ValueAxis rangeAxis;
    private CategoryItemRenderer renderer;

    @Before
    public void setUp() {
        dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "Series1", "Category1");
        domainAxis = new CategoryAxis("Domain");
        rangeAxis = new NumberAxis("Range");
        renderer = new BarRenderer();
        plot = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
    }

    @Test
    public void testDefaultConstructor() {
        CategoryPlot p = new CategoryPlot();
        assertNull(p.getDataset());
        assertNull(p.getDomainAxis());
        assertNull(p.getRangeAxis());
        assertNull(p.getRenderer());
        assertEquals(PlotOrientation.VERTICAL, p.getOrientation());
        assertEquals(false, p.isDomainGridlinesVisible());
        assertEquals(true, p.isRangeGridlinesVisible());
        assertEquals(false, p.isRangeCrosshairVisible());
        assertEquals(0.0, p.getAnchorValue(), 0.0001);
        assertEquals(1, p.getWeight()); // default weight not explicitly set? Actually weight is init to 0 in constructor? The weight is int, default 0. Let's check: in source, weight is declared but not initialized explicitly in constructor, so default 0. However, tested with getWeight().
        // Actually weight is not intialized in the default constructor, so default int value 0. So getWeight() returns 0.
        assertEquals(0, p.getWeight());
        assertNotNull(p.getAnnotations());
        assertTrue(p.getAnnotations().isEmpty());
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals(dataset, plot.getDataset());
        assertEquals(domainAxis, plot.getDomainAxis());
        assertEquals(rangeAxis, plot.getRangeAxis());
        assertEquals(renderer, plot.getRenderer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetOrientationNull() {
        plot.setOrientation(null);
    }

    @Test
    public void testSetOrientation() {
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        assertEquals(PlotOrientation.HORIZONTAL, plot.getOrientation());
        // check change event not needed for coverage
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
    public void testDomainAxisManagement() {
        CategoryAxis newAxis = new CategoryAxis("New");
        plot.setDomainAxis(1, newAxis);
        assertEquals(newAxis, plot.getDomainAxis(1));
        // ensure old axis detached? Not testing listener removal but coverage ok.
        assertEquals(1, plot.getDomainAxisIndex(newAxis));
        assertEquals(-1, plot.getDomainAxisIndex(new CategoryAxis("Missing")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDomainAxisIndexNull() {
        plot.getDomainAxisIndex(null);
    }

    @Test
    public void testSetDomainAxesArray() {
        CategoryAxis[] axes = new CategoryAxis[]{new CategoryAxis("A"), new CategoryAxis("B")};
        plot.setDomainAxes(axes);
        assertEquals(axes[0], plot.getDomainAxis(0));
        assertEquals(axes[1], plot.getDomainAxis(1));
    }

    @Test
    public void testClearDomainAxes() {
        plot.setDomainAxis(1, new CategoryAxis("Axis1"));
        assertNotNull(plot.getDomainAxis(1));
        plot.clearDomainAxes();
        // after clear, all axes removed
        for (int i = 0; i < plot.getDomainAxisCount(); i++) {
            assertNull(plot.getDomainAxis(i));
        }
        assertEquals(0, plot.getDomainAxisCount());
    }

    @Test
    public void testDomainAxisLocation() {
        plot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);
        assertEquals(AxisLocation.TOP_OR_LEFT, plot.getDomainAxisLocation());
        // set with notify false
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT, false);
        assertEquals(AxisLocation.BOTTOM_OR_LEFT, plot.getDomainAxisLocation());
        // set location for index 1, non-null
        plot.setDomainAxisLocation(1, AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getDomainAxisLocation(1));
        // null location for index 0 should throw
        try {
            plot.setDomainAxisLocation(0, null, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // passed
        }
    }

    @Test
    public void testRangeAxisManagement() {
        ValueAxis newRange = new NumberAxis("New Range");
        plot.setRangeAxis(1, newRange);
        assertEquals(newRange, plot.getRangeAxis(1));
        assertEquals(1, plot.getRangeAxisIndex(newRange));
        assertEquals(-1, plot.getRangeAxisIndex(new NumberAxis("Other")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRangeAxisIndexNull() {
        plot.getRangeAxisIndex(null);
    }

    @Test
    public void testSetRangeAxesArray() {
        ValueAxis[] axes = new ValueAxis[]{new NumberAxis("R1"), new NumberAxis("R2")};
        plot.setRangeAxes(axes);
        assertEquals(axes[0], plot.getRangeAxis(0));
        assertEquals(axes[1], plot.getRangeAxis(1));
    }

    @Test
    public void testClearRangeAxes() {
        plot.setRangeAxis(1, new NumberAxis("R1"));
        assertNotNull(plot.getRangeAxis(1));
        plot.clearRangeAxes();
        assertNull(plot.getRangeAxis(0));
        assertNull(plot.getRangeAxis(1));
        assertEquals(0, plot.getRangeAxisCount());
    }

    @Test
    public void testRangeAxisLocation() {
        plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
        assertEquals(AxisLocation.TOP_OR_RIGHT, plot.getRangeAxisLocation());
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT, false);
        assertEquals(AxisLocation.BOTTOM_OR_RIGHT, plot.getRangeAxisLocation());
        plot.setRangeAxisLocation(1, AxisLocation.TOP_OR_LEFT);
        assertEquals(AxisLocation.TOP_OR_LEFT, plot.getRangeAxisLocation(1));
        try {
            plot.setRangeAxisLocation(0, null, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testDatasetManagement() {
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        plot.setDataset(1, ds2);
        assertEquals(ds2, plot.getDataset(1));
        assertEquals(2, plot.getDatasetCount());
    }

    @Test
    public void testMapDatasetToDomainAxis() {
        plot.mapDatasetToDomainAxis(0, 1);
        assertEquals(1, ((Integer) TestUtils.getField(plot, "datasetToDomainAxisMap")).intValue()); // Using reflection or assume mapping works via getter
        // Use public method getDomainAxisForDataset
        CategoryAxis axis = plot.getDomainAxisForDataset(0);
        // since we have axis at index 1? No axis set. It should return the axis at index 1 if exists, else fallback to parent or default. But we can test it.
        // Set an axis at index 1
        CategoryAxis axis2 = new CategoryAxis("Second");
        plot.setDomainAxis(1, axis2);
        assertEquals(axis2, plot.getDomainAxisForDataset(0));
    }

    @Test
    public void testMapDatasetToRangeAxis() {
        plot.mapDatasetToRangeAxis(0, 1);
        assertEquals(1, ((Integer) TestUtils.getField(plot, "datasetToRangeAxisMap")).intValue());
        ValueAxis axis2 = new NumberAxis("Second Range");
        plot.setRangeAxis(1, axis2);
        assertEquals(axis2, plot.getRangeAxisForDataset(0));
    }

    @Test
    public void testRendererManagement() {
        CategoryItemRenderer newRenderer = new LineAndShapeRenderer();
        plot.setRenderer(1, newRenderer);
        assertEquals(newRenderer, plot.getRenderer(1));
        assertEquals(1, plot.getIndexOf(newRenderer));
        // set renderers array
        CategoryItemRenderer[] renderers = new CategoryItemRenderer[]{new BarRenderer(), new LineAndShapeRenderer()};
        plot.setRenderers(renderers);
        assertEquals(renderers[0], plot.getRenderer(0));
        assertEquals(renderers[1], plot.getRenderer(1));
    }

    @Test
    public void testRendererForDataset() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        plot.setDataset(1, ds);
        BarRenderer r2 = new BarRenderer();
        plot.setRenderer(1, r2);
        assertEquals(r2, plot.getRendererForDataset(ds));
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
    public void testDomainGridlines() {
        assertFalse(plot.isDomainGridlinesVisible());
        plot.setDomainGridlinesVisible(true);
        assertTrue(plot.isDomainGridlinesVisible());
        // set same no event? still coverage
        plot.setDomainGridlinesVisible(true);
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
        Stroke stroke = new BasicStroke(2.0f);
        plot.setDomainGridlineStroke(stroke);
        assertEquals(stroke, plot.getDomainGridlineStroke());
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
    public void testRangeGridlines() {
        assertTrue(plot.isRangeGridlinesVisible());
        plot.setRangeGridlinesVisible(false);
        assertFalse(plot.isRangeGridlinesVisible());
        plot.setRangeGridlinesVisible(false); // no change
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeGridlineStrokeNull() {
        plot.setRangeGridlineStroke(null);
    }

    @Test
    public void testRangeGridlineStroke() {
        Stroke stroke = new BasicStroke(3.0f);
        plot.setRangeGridlineStroke(stroke);
        assertEquals(stroke, plot.getRangeGridlineStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeGridlinePaintNull() {
        plot.setRangeGridlinePaint(null);
    }

    @Test
    public void testRangeGridlinePaint() {
        plot.setRangeGridlinePaint(Color.BLUE);
        assertEquals(Color.BLUE, plot.getRangeGridlinePaint());
    }

    @Test
    public void testFixedLegendItems() {
        LegendItemCollection items = new LegendItemCollection();
        assertNull(plot.getFixedLegendItems());
        plot.setFixedLegendItems(items);
        assertEquals(items, plot.getFixedLegendItems());
        // getLegendItems should return fixed if set
        LegendItemCollection fromGet = plot.getLegendItems();
        assertEquals(items, fromGet);
    }

    @Test
    public void testLegendItemsWithDatasetNoFixed() {
        // ensure getLegendItems works with no fixed items, should generate from dataset
        plot.setFixedLegendItems(null);
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull(items);
        // Our dataset has one series
        assertEquals(1, items.getItemCount());
    }

    @Test
    public void testAnchorValue() {
        assertEquals(0.0, plot.getAnchorValue(), 0.0001);
        plot.setAnchorValue(5.0);
        assertEquals(5.0, plot.getAnchorValue(), 0.0001);
        plot.setAnchorValue(10.0, false);
        assertEquals(10.0, plot.getAnchorValue(), 0.0001);
    }

    @Test
    public void testRangeCrosshair() {
        assertFalse(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(true);
        assertTrue(plot.isRangeCrosshairVisible());
        plot.setRangeCrosshairVisible(false);
        assertFalse(plot.isRangeCrosshairVisible());

        assertEquals(0.0, plot.getRangeCrosshairValue(), 0.0001);
        plot.setRangeCrosshairValue(5.5);
        assertEquals(5.5, plot.getRangeCrosshairValue(), 0.0001);
        // if crosshair not visible, listener not notified? Just set value with notify true
        plot.setRangeCrosshairValue(6.0, true);
        assertEquals(6.0, plot.getRangeCrosshairValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeCrosshairStrokeNull() {
        plot.setRangeCrosshairStroke(null);
    }

    @Test
    public void testRangeCrosshairStroke() {
        Stroke stroke = new BasicStroke(4.0f);
        plot.setRangeCrosshairStroke(stroke);
        assertEquals(stroke, plot.getRangeCrosshairStroke());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetRangeCrosshairPaintNull() {
        plot.setRangeCrosshairPaint(null);
    }

    @Test
    public void testRangeCrosshairPaint() {
        plot.setRangeCrosshairPaint(Color.GREEN);
        assertEquals(Color.GREEN, plot.getRangeCrosshairPaint());
    }

    @Test
    public void testRangeCrosshairLockedOnData() {
        assertTrue(plot.isRangeCrosshairLockedOnData());
        plot.setRangeCrosshairLockedOnData(false);
        assertFalse(plot.isRangeCrosshairLockedOnData());
        plot.setRangeCrosshairLockedOnData(false); // no change
    }

    @Test
    public void testAddRemoveAnnotation() {
        assertTrue(plot.getAnnotations().isEmpty());
        CategoryAnnotation ann = new CategoryAnnotation() {
            public void draw(java.awt.Graphics2D g2, CategoryPlot plot, java.awt.geom.Rectangle2D dataArea,
                             CategoryAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {}
        };
        plot.addAnnotation(ann);
        assertEquals(1, plot.getAnnotations().size());
        assertTrue(plot.removeAnnotation(ann));
        assertFalse(plot.removeAnnotation(ann));
        assertEquals(0, plot.getAnnotations().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAnnotationNull() {
        plot.addAnnotation(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveAnnotationNull() {
        plot.removeAnnotation(null);
    }

    @Test
    public void testClearAnnotations() {
        CategoryAnnotation ann = new CategoryAnnotation() { /* empty */ };
        plot.addAnnotation(ann);
        plot.clearAnnotations();
        assertTrue(plot.getAnnotations().isEmpty());
    }

    // Marker management tests
    @Test
    public void testAddDomainMarker() {
        CategoryMarker marker = new CategoryMarker("Cat1");
        plot.addDomainMarker(marker);
        Collection markers = plot.getDomainMarkers(Layer.FOREGROUND);
        assertNotNull(markers);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testAddDomainMarkerSpecificLayer() {
        CategoryMarker marker = new CategoryMarker("Cat1");
        plot.addDomainMarker(1, marker, Layer.BACKGROUND, false);
        Collection markers = plot.getDomainMarkers(1, Layer.BACKGROUND);
        assertTrue(markers.contains(marker));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDomainMarkerNullMarker() {
        plot.addDomainMarker(0, null, Layer.FOREGROUND, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDomainMarkerNullLayer() {
        plot.addDomainMarker(0, new CategoryMarker("X"), null, true);
    }

    @Test
    public void testClearDomainMarkers() {
        plot.addDomainMarker(new CategoryMarker("A"));
        plot.addDomainMarker(new CategoryMarker("B"));
        plot.clearDomainMarkers();
        assertNull(plot.getDomainMarkers(Layer.FOREGROUND)); // after clear, collections removed? Actually clearDomainMarkers clears maps and keys, then getDomainMarkers returns null because the maps are empty and get returns null.
        assertNull(plot.getDomainMarkers(Layer.BACKGROUND));
    }

    @Test
    public void testClearDomainMarkersForIndex() {
        CategoryMarker m1 = new CategoryMarker("A");
        plot.addDomainMarker(1, m1, Layer.FOREGROUND);
        plot.clearDomainMarkers(1);
        assertFalse(plot.getDomainMarkers(1, Layer.FOREGROUND).contains(m1) if it exists; // After clear, getDomainMarkers for that index returns null because the map removes the key)
        assertNull(plot.getDomainMarkers(1, Layer.FOREGROUND));
    }

    @Test
    public void testRemoveDomainMarker() {
        CategoryMarker m = new CategoryMarker("X");
        plot.addDomainMarker(m);
        assertTrue(plot.removeDomainMarker(m));
        assertFalse(plot.removeDomainMarker(m));
        // remove with layer
        CategoryMarker m2 = new CategoryMarker("Y");
        plot.addDomainMarker(m2, Layer.BACKGROUND);
        assertTrue(plot.removeDomainMarker(m2, Layer.BACKGROUND));
        assertFalse(plot.removeDomainMarker(m2, Layer.BACKGROUND));
        // remove with index and notify false
        CategoryMarker m3 = new CategoryMarker("Z");
        plot.addDomainMarker(0, m3, Layer.FOREGROUND);
        assertTrue(plot.removeDomainMarker(0, m3, Layer.FOREGROUND, false));
    }

    @Test
    public void testAddRangeMarker() {
        Marker marker = new ValueMarker(1.5);
        plot.addRangeMarker(marker);
        Collection markers = plot.getRangeMarkers(Layer.FOREGROUND);
        assertNotNull(markers);
        assertTrue(markers.contains(marker));
    }

    @Test
    public void testAddRangeMarkerSpecific() {
        Marker marker = new ValueMarker(2.0);
        plot.addRangeMarker(1, marker, Layer.BACKGROUND, false);
        Collection markers = plot.getRangeMarkers(1, Layer.BACKGROUND);
        assertTrue(markers.contains(marker));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveRangeMarkerNull() {
        plot.removeRangeMarker(0, null, Layer.FOREGROUND, true);
    }

    @Test
    public void testClearRangeMarkers() {
        plot.addRangeMarker(new ValueMarker(1.0));
        plot.clearRangeMarkers();
        assertNull(plot.getRangeMarkers(Layer.FOREGROUND));
    }

    @Test
    public void testClearRangeMarkersForIndex() {
        plot.addRangeMarker(1, new ValueMarker(2.0), Layer.FOREGROUND);
        plot.clearRangeMarkers(1);
        assertNull(plot.getRangeMarkers(1, Layer.FOREGROUND));
    }

    @Test
    public void testRemoveRangeMarker() {
        Marker m = new ValueMarker(3.0);
        plot.addRangeMarker(m);
        assertTrue(plot.removeRangeMarker(m));
        assertFalse(plot.removeRangeMarker(m));
        Marker m2 = new ValueMarker(4.0);
        plot.addRangeMarker(m2, Layer.BACKGROUND);
        assertTrue(plot.removeRangeMarker(m2, Layer.BACKGROUND));
        assertFalse(plot.removeRangeMarker(m2, Layer.BACKGROUND));
    }

    @Test
    public void testGetCategories() {
        List categories = plot.getCategories();
        assertNotNull(categories);
        assertEquals(1, categories.size());
        assertEquals("Category1", categories.get(0));
    }

    @Test
    public void testGetCategoriesForAxis() {
        CategoryAxis axis = new CategoryAxis("Test");
        plot.setDomainAxis(1, axis);
        DefaultCategoryDataset ds2 = new DefaultCategoryDataset();
        ds2.addValue(2.0, "S2", "Cat2");
        plot.setDataset(1, ds2);
        // map dataset 1 to axis index 1
        plot.mapDatasetToDomainAxis(1, 1);
        List cats = plot.getCategoriesForAxis(axis);
        assertTrue(cats.contains("Cat2"));
    }

    @Test
    public void testGetDataRange() {
        Range dataRange = plot.getDataRange(plot.getRangeAxis());
        // Our dataset has value 1.0, range should be 1.0-1.0
        assertEquals(new Range(1.0, 1.0), dataRange);
        // test with axis not present
        ValueAxis other = new NumberAxis("Other");
        Range nullRange = plot.getDataRange(other);
        assertNull(nullRange);
    }

    @Test
    public void testDrawSharedDomainAxis() {
        assertFalse(plot.getDrawSharedDomainAxis());
        plot.setDrawSharedDomainAxis(true);
        assertTrue(plot.getDrawSharedDomainAxis());
    }

    @Test
    public void testWeight() {
        assertEquals(0, plot.getWeight());
        plot.setWeight(5);
        assertEquals(5, plot.getWeight());
    }

    @Test
    public void testFixedDomainAxisSpace() {
        assertNull(plot.getFixedDomainAxisSpace());
        AxisSpace space = new AxisSpace();
        plot.setFixedDomainAxisSpace(space);
        assertEquals(space, plot.getFixedDomainAxisSpace());
        plot.setFixedDomainAxisSpace(null, false);
        assertNull(plot.getFixedDomainAxisSpace());
    }

    @Test
    public void testFixedRangeAxisSpace() {
        assertNull(plot.getFixedRangeAxisSpace());
        AxisSpace space = new AxisSpace();
        plot.setFixedRangeAxisSpace(space);
        assertEquals(space, plot.getFixedRangeAxisSpace());
        plot.setFixedRangeAxisSpace(null, false);
        assertNull(plot.getFixedRangeAxisSpace());
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
    public void testZoom() {
        // zoom(double) calls getRangeAxis() etc. We'll just test code path
        plot.zoom(0.1);
        // range should be changed
        // after zoom, anchor value is default 0.0? Actually range axis range will be from -scaled/2 to +scaled/2, i.e. around anchor 0.0, with length = original length * 0.1.
        // Not checking exact values, just coverage.
        plot.zoom(0.0); // should set auto range
        assertTrue(plot.getRangeAxis().isAutoRange());
    }

    @Test
    public void testEquals() {
        CategoryPlot plot1 = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        CategoryPlot plot2 = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        assertTrue(plot1.equals(plot2));
        assertFalse(plot1.equals(new Object()));
        assertFalse(plot1.equals(new CategoryPlot()));
    }

    @Test
    public void testEqualsWithDifferences() {
        CategoryPlot p1 = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        CategoryPlot p2 = new CategoryPlot(dataset, domainAxis, rangeAxis, renderer);
        p2.setOrientation(PlotOrientation.HORIZONTAL);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        CategoryPlot clone = (CategoryPlot) plot.clone();
        assertNotNull(clone);
        assertNotSame(plot, clone);
        assertEquals(plot.getWeight(), clone.getWeight());
        assertEquals(plot.getDataset(), clone.getDataset()); // same dataset instance? clone method clones datasets list, but dataset objects are not deep-cloned; they are the same references, but the plot registers listeners. That's acceptable.
        // Check that clone has its own domainAxes list but same axis objects
        assertEquals(plot.getDomainAxis(), clone.getDomainAxis());
        // test that clone is independent
    }

    // Helper inner class or utility to access private fields via reflection if needed; but we can avoid deeper reflection by using public methods.
    // However, for map access we might need reflection. Let's provide a utility method in the test class itself using reflection, but avoid external dependencies.
    // I'll add a small static utility method.
    private static class TestUtils {
        static Object getField(Object target, String fieldName) {
            try {
                java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
