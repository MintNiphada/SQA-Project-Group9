package org.jfree.data.statistics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.data.Range;
import org.junit.Test;

public class DefaultBoxAndWhiskerCategoryDatasetTest {

    @Test
    public void testConstructor() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        assertEquals(0, dataset.getRowCount());
        assertEquals(0, dataset.getColumnCount());
        assertTrue(Double.isNaN(dataset.getRangeLowerBound(false)));
        assertTrue(Double.isNaN(dataset.getRangeUpperBound(false)));
        assertEquals(new Range(0.0, 0.0), dataset.getRangeBounds(false));
    }

    @Test(expected = NullPointerException.class)
    public void testAddListNull() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add((List) null, "R1", "C1");
    }

    @Test
    public void testAddListItem() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> values = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        dataset.add(values, "Row1", "Col1");
        assertEquals(1, dataset.getRowCount());
        assertEquals(1, dataset.getColumnCount());
        assertNotNull(dataset.getItem(0, 0));
        
        // Check that range bounds are updated (example values from calculation)
        assertFalse(Double.isNaN(dataset.getRangeLowerBound(false)));
        assertFalse(Double.isNaN(dataset.getRangeUpperBound(false)));
    }

    @Test(expected = NullPointerException.class)
    public void testAddNullItem() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add((BoxAndWhiskerItem) null, "R1", "C1");
    }

    @Test
    public void testAddSingleItem() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(3.0, 3.0, 2.0, 4.0, 1.0, 5.0, 0.0, 6.0, new ArrayList<Double>());
        dataset.add(item, "Row1", "Col1");
        
        assertEquals(1, dataset.getRowCount());
        assertEquals(1, dataset.getColumnCount());
        assertEquals(item, dataset.getItem(0, 0));
        assertEquals(0.0, dataset.getRangeLowerBound(false), 1e-15);
        assertEquals(6.0, dataset.getRangeUpperBound(false), 1e-15);
        assertEquals(new Range(0.0, 6.0), dataset.getRangeBounds(false));
    }

    @Test
    public void testAddMultipleItemsMinMax() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(2.0, 2.0, 1.0, 3.0, 0.5, 4.0, 0.0, 5.0, null);
        BoxAndWhiskerItem item2 = new BoxAndWhiskerItem(7.0, 7.0, 5.0, 9.0, 4.0, 10.0, 3.0, 12.0, null);
        
        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C2");
        
        // min should be 0.0 (from item1), max should be 12.0 (from item2)
        assertEquals(0.0, dataset.getRangeLowerBound(false), 1e-15);
        assertEquals(12.0, dataset.getRangeUpperBound(false), 1e-15);
        assertEquals(new Range(0.0, 12.0), dataset.getRangeBounds(false));
    }

    @Test
    public void testUpdateBoundsOverwriteMaxCell() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem itemA = new BoxAndWhiskerItem(10.0, 10.0, 9.0, 11.0, 8.0, 12.0, 7.0, 13.0, null);
        BoxAndWhiskerItem itemB = new BoxAndWhiskerItem(5.0, 5.0, 4.0, 6.0, 3.0, 7.0, 2.0, 8.0, null);
        
        dataset.add(itemA, "R1", "C1"); // max cell
        dataset.add(itemB, "R2", "C2");
        
        // now overwrite the max cell (R1,C1) with a lower max value
        BoxAndWhiskerItem itemC = new BoxAndWhiskerItem(6.0, 6.0, 5.0, 7.0, 4.0, 8.0, 3.0, 9.0, null);
        dataset.add(itemC, "R1", "C1"); // triggers updateBounds
        
        // after updateBounds, min/max are only from itemC (bug? known behavior)
        // min = 3.0, max = 9.0
        assertEquals(3.0, dataset.getRangeLowerBound(false), 1e-15);
        assertEquals(9.0, dataset.getRangeUpperBound(false), 1e-15);
        // even though itemB has min=2.0, max=8.0, they are ignored
        // (demonstrates cached bounds bug)
    }

    @Test
    public void testUpdateBoundsOverwriteMinCell() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem itemA = new BoxAndWhiskerItem(1.0, 1.0, 0.5, 1.5, 0.0, 2.0, -1.0, 3.0, null);
        BoxAndWhiskerItem itemB = new BoxAndWhiskerItem(5.0, 5.0, 4.0, 6.0, 3.0, 7.0, 2.0, 8.0, null);
        
        dataset.add(itemA, "R1", "C1"); // min cell
        dataset.add(itemB, "R2", "C2");
        
        // overwrite min cell (R1,C1) with an item having higher min
        BoxAndWhiskerItem itemC = new BoxAndWhiskerItem(3.0, 3.0, 2.0, 4.0, 1.0, 5.0, 0.0, 6.0, null);
        dataset.add(itemC, "R1", "C1");
        
        // after updateBounds, min = 0.0 (from itemC), max = 6.0 (from itemC)
        assertEquals(0.0, dataset.getRangeLowerBound(false), 1e-15);
        assertEquals(6.0, dataset.getRangeUpperBound(false), 1e-15);
    }

    @Test
    public void testAddWithNullRowKey() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, null);
        try {
            dataset.add(item, null, "C1");
            fail("Expected IllegalArgumentException for null row key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAddWithNullColumnKey() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, null);
        try {
            dataset.add(item, "R1", null);
            fail("Expected IllegalArgumentException for null column key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetItem() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(5.0, 5.0, 4.0, 6.0, 3.0, 7.0, 2.0, 8.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(item, dataset.getItem(0, 0));
        // out-of-range indices
        try {
            dataset.getItem(0, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetValue() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(5.0, 5.0, 4.0, 6.0, 3.0, 7.0, 2.0, 8.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(5.0, dataset.getValue(0, 0).doubleValue(), 1e-15);
        assertEquals(5.0, dataset.getValue("R1", "C1").doubleValue(), 1e-15);
    }

    @Test
    public void testGetMeanValue() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(10.0, dataset.getMeanValue(0, 0).doubleValue(), 1e-15);
        assertEquals(10.0, dataset.getMeanValue("R1", "C1").doubleValue(), 1e-15);
        
        // non-existing cell
        assertNull(dataset.getMeanValue(0, 1));
        assertNull(dataset.getMeanValue("R2", "C1"));
    }

    @Test
    public void testGetMedianValue() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(8.0, dataset.getMedianValue(0, 0).doubleValue(), 1e-15);
        assertEquals(8.0, dataset.getMedianValue("R1", "C1").doubleValue(), 1e-15);
        
        assertNull(dataset.getMedianValue(0, 1));
    }

    @Test
    public void testGetQ1Value() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(7.0, dataset.getQ1Value(0, 0).doubleValue(), 1e-15);
        assertEquals(7.0, dataset.getQ1Value("R1", "C1").doubleValue(), 1e-15);
        
        assertNull(dataset.getQ1Value(1, 0));
    }

    @Test
    public void testGetQ3Value() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(9.0, dataset.getQ3Value(0, 0).doubleValue(), 1e-15);
        assertEquals(9.0, dataset.getQ3Value("R1", "C1").doubleValue(), 1e-15);
        
        assertNull(dataset.getQ3Value(1, 0));
    }

    @Test
    public void testGetMinRegularValue() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(6.0, dataset.getMinRegularValue(0, 0).doubleValue(), 1e-15);
        assertEquals(6.0, dataset.getMinRegularValue("R1", "C1").doubleValue(), 1e-15);
    }

    @Test
    public void testGetMaxRegularValue() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(11.0, dataset.getMaxRegularValue(0, 0).doubleValue(), 1e-15);
        assertEquals(11.0, dataset.getMaxRegularValue("R1", "C1").doubleValue(), 1e-15);
    }

    @Test
    public void testGetMinOutlier() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(5.0, dataset.getMinOutlier(0, 0).doubleValue(), 1e-15);
        assertEquals(5.0, dataset.getMinOutlier("R1", "C1").doubleValue(), 1e-15);
        
        // item with null outliers
        BoxAndWhiskerItem itemNoOutliers = new BoxAndWhiskerItem(10.0, 10.0, 9.0, 11.0, 8.0, 12.0, null, null, null);
        dataset.add(itemNoOutliers, "R2", "C2");
        assertNull(dataset.getMinOutlier(1, 0)); // row 1 col 0 corresponds to R2, C2? careful with indices
        // Row indices: 0="R1",1="R2" cols:0="C1",1="C2"
        assertNull(dataset.getMinOutlier(1, 1));
    }

    @Test
    public void testGetMaxOutlier() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, null);
        dataset.add(item, "R1", "C1");
        assertEquals(12.0, dataset.getMaxOutlier(0, 0).doubleValue(), 1e-15);
        assertEquals(12.0, dataset.getMaxOutlier("R1", "C1").doubleValue(), 1e-15);
        
        // no outlier
        BoxAndWhiskerItem itemNoOutliers = new BoxAndWhiskerItem(10.0, 10.0, 9.0, 11.0, 8.0, 12.0, null, null, null);
        dataset.add(itemNoOutliers, "R2", "C2");
        assertNull(dataset.getMaxOutlier(1, 1));
    }

    @Test
    public void testGetOutliers() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        List<Double> outliers = Arrays.asList(0.0, 15.0);
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(10.0, 8.0, 7.0, 9.0, 6.0, 11.0, 5.0, 12.0, outliers);
        dataset.add(item, "R1", "C1");
        assertEquals(outliers, dataset.getOutliers(0, 0));
        assertEquals(outliers, dataset.getOutliers("R1", "C1"));
        
        // null outliers case
        BoxAndWhiskerItem itemNoOutliers = new BoxAndWhiskerItem(10.0, 10.0, 9.0, 11.0, 8.0, 12.0, null, null, null);
        dataset.add(itemNoOutliers, "R2", "C2");
        assertNull(dataset.getOutliers(1, 1));
    }

    @Test
    public void testGetRowIndex() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "A", "X");
        assertEquals(0, dataset.getRowIndex("A"));
        assertEquals(-1, dataset.getRowIndex("B"));
    }

    @Test
    public void testGetColumnIndex() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "A", "X");
        assertEquals(0, dataset.getColumnIndex("X"));
        assertEquals(-1, dataset.getColumnIndex("Y"));
    }

    @Test
    public void testGetRowKey() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "R1", "C1");
        assertEquals("R1", dataset.getRowKey(0));
        try {
            dataset.getRowKey(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetColumnKey() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "R1", "C1");
        assertEquals("C1", dataset.getColumnKey(0));
        try {
            dataset.getColumnKey(1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetRowKeys() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "R1", "C1");
        dataset.add(new BoxAndWhiskerItem(2.0,2.0,2.0,2.0,2.0,2.0,null,null,null), "R2", "C1");
        List keys = dataset.getRowKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("R1"));
        assertTrue(keys.contains("R2"));
    }

    @Test
    public void testGetColumnKeys() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "R1", "C1");
        dataset.add(new BoxAndWhiskerItem(2.0,2.0,2.0,2.0,2.0,2.0,null,null,null), "R1", "C2");
        List keys = dataset.getColumnKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("C1"));
        assertTrue(keys.contains("C2"));
    }

    @Test
    public void testGetRowCount() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        assertEquals(0, dataset.getRowCount());
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "R1", "C1");
        assertEquals(1, dataset.getRowCount());
        dataset.add(new BoxAndWhiskerItem(2.0,2.0,2.0,2.0,2.0,2.0,null,null,null), "R2", "C2");
        assertEquals(2, dataset.getRowCount());
    }

    @Test
    public void testGetColumnCount() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        assertEquals(0, dataset.getColumnCount());
        dataset.add(new BoxAndWhiskerItem(1.0,1.0,1.0,1.0,1.0,1.0,null,null,null), "R1", "C1");
        assertEquals(1, dataset.getColumnCount());
        dataset.add(new BoxAndWhiskerItem(2.0,2.0,2.0,2.0,2.0,2.0,null,null,null), "R1", "C2");
        assertEquals(2, dataset.getColumnCount());
    }

    @Test
    public void testGetRangeLowerBoundAndUpperBound() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        // initially NaN
        assertTrue(Double.isNaN(dataset.getRangeLowerBound(false)));
        assertTrue(Double.isNaN(dataset.getRangeUpperBound(false)));
        
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(5.0,5.0,4.0,6.0,3.0,7.0,2.0,8.0,null);
        dataset.add(item, "R", "C");
        assertEquals(2.0, dataset.getRangeLowerBound(false), 1e-15);
        assertEquals(8.0, dataset.getRangeUpperBound(false), 1e-15);
    }

    @Test
    public void testGetRangeBounds() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        assertEquals(new Range(0.0, 0.0), dataset.getRangeBounds(false));
        
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(5.0,5.0,4.0,6.0,3.0,7.0,2.0,8.0,null);
        dataset.add(item, "R", "C");
        assertEquals(new Range(2.0, 8.0), dataset.getRangeBounds(false));
    }

    @Test
    public void testEquals() {
        DefaultBoxAndWhiskerCategoryDataset dataset1 = new DefaultBoxAndWhiskerCategoryDataset();
        DefaultBoxAndWhiskerCategoryDataset dataset2 = new DefaultBoxAndWhiskerCategoryDataset();
        assertTrue(dataset1.equals(dataset2));
        
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(5.0,5.0,4.0,6.0,3.0,7.0,2.0,8.0,null);
        dataset1.add(item, "R1", "C1");
        assertFalse(dataset1.equals(dataset2));
        
        dataset2.add(item, "R1", "C1");
        assertTrue(dataset1.equals(dataset2));
        
        // same object
        assertTrue(dataset1.equals(dataset1));
        // different type
        assertFalse(dataset1.equals("string"));
        // null
        assertFalse(dataset1.equals(null));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(5.0,5.0,4.0,6.0,3.0,7.0,2.0,8.0,null);
        dataset.add(item, "R1", "C1");
        
        DefaultBoxAndWhiskerCategoryDataset clone = (DefaultBoxAndWhiskerCategoryDataset) dataset.clone();
        assertNotSame(dataset, clone);
        assertEquals(dataset, clone);
        
        // Verify deep copy of data: modify original, clone unaffected
        BoxAndWhiskerItem newItem = new BoxAndWhiskerItem(10.0,10.0,9.0,11.0,8.0,12.0,7.0,13.0,null);
        dataset.add(newItem, "R2", "C2");
        assertFalse(dataset.equals(clone));
        assertEquals(1, clone.getRowCount());
    }
}
