package org.jfree.data.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A comprehensive test suite for {@link DefaultBoxAndWhiskerCategoryDataset}.
 */
public class DefaultBoxAndWhiskerCategoryDatasetTest implements DatasetChangeListener {

    private DefaultBoxAndWhiskerCategoryDataset dataset;
    private int changeEventCount;

    @Before
    public void setUp() {
        this.dataset = new DefaultBoxAndWhiskerCategoryDataset();
        this.dataset.addChangeListener(this);
        this.changeEventCount = 0;
    }

    public void datasetChanged(DatasetChangeEvent event) {
        this.changeEventCount++;
    }

    private BoxAndWhiskerItem createItem(double mean, double median, double q1, double q3,
                                         double minRegular, double maxRegular,
                                         double minOutlier, double maxOutlier,
                                         List outliers) {
        return new BoxAndWhiskerItem(new Double(mean), new Double(median), new Double(q1),
                new Double(q3), new Double(minRegular), new Double(maxRegular),
                new Double(minOutlier), new Double(maxOutlier), outliers);
    }

    @Test
    public void testEmptyDataset() {
        Assert.assertEquals(0, dataset.getRowCount());
        Assert.assertEquals(0, dataset.getColumnCount());
        Assert.assertTrue(dataset.getRowKeys().isEmpty());
        Assert.assertTrue(dataset.getColumnKeys().isEmpty());
        Assert.assertTrue(Double.isNaN(dataset.getRangeLowerBound(false)));
        Assert.assertTrue(Double.isNaN(dataset.getRangeUpperBound(false)));
        Assert.assertTrue(Double.isNaN(dataset.getRangeLowerBound(true)));
        Assert.assertTrue(Double.isNaN(dataset.getRangeUpperBound(true)));
        Range range = dataset.getRangeBounds(false);
        Assert.assertNotNull(range);
        Assert.assertEquals(0.0, range.getLowerBound(), 0.000001);
        Assert.assertEquals(0.0, range.getUpperBound(), 0.000001);
    }

    @Test
    public void testAddBoxAndWhiskerItem() {
        List<Double> outliers = Arrays.asList(new Double(0.5), new Double(10.5));
        BoxAndWhiskerItem item1 = createItem(5.0, 5.0, 3.0, 7.0, 1.0, 9.0, 0.5, 10.5, outliers);

        dataset.add(item1, "R1", "C1");

        Assert.assertEquals(1, changeEventCount);
        Assert.assertEquals(1, dataset.getRowCount());
        Assert.assertEquals(1, dataset.getColumnCount());
        Assert.assertEquals("R1", dataset.getRowKey(0));
        Assert.assertEquals("C1", dataset.getColumnKey(0));
        Assert.assertEquals(0, dataset.getRowIndex("R1"));
        Assert.assertEquals(0, dataset.getColumnIndex("C1"));

        Assert.assertEquals(0.5, dataset.getRangeLowerBound(true), 0.000001);
        Assert.assertEquals(10.5, dataset.getRangeUpperBound(true), 0.000001);
        Range range = dataset.getRangeBounds(true);
        Assert.assertEquals(0.5, range.getLowerBound(), 0.000001);
        Assert.assertEquals(10.5, range.getUpperBound(), 0.000001);

        Assert.assertEquals(item1, dataset.getItem(0, 0));
        Assert.assertEquals(new Double(5.0), dataset.getValue(0, 0));
        Assert.assertEquals(new Double(5.0), dataset.getValue("R1", "C1"));
        Assert.assertEquals(new Double(5.0), dataset.getMeanValue(0, 0));
        Assert.assertEquals(new Double(5.0), dataset.getMeanValue("R1", "C1"));
        Assert.assertEquals(new Double(5.0), dataset.getMedianValue(0, 0));
        Assert.assertEquals(new Double(5.0), dataset.getMedianValue("R1", "C1"));
        Assert.assertEquals(new Double(3.0), dataset.getQ1Value(0, 0));
        Assert.assertEquals(new Double(3.0), dataset.getQ1Value("R1", "C1"));
        Assert.assertEquals(new Double(7.0), dataset.getQ3Value(0, 0));
        Assert.assertEquals(new Double(7.0), dataset.getQ3Value("R1", "C1"));
        Assert.assertEquals(new Double(1.0), dataset.getMinRegularValue(0, 0));
        Assert.assertEquals(new Double(1.0), dataset.getMinRegularValue("R1", "C1"));
        Assert.assertEquals(new Double(9.0), dataset.getMaxRegularValue(0, 0));
        Assert.assertEquals(new Double(9.0), dataset.getMaxRegularValue("R1", "C1"));
        Assert.assertEquals(new Double(0.5), dataset.getMinOutlier(0, 0));
        Assert.assertEquals(new Double(0.5), dataset.getMinOutlier("R1", "C1"));
        Assert.assertEquals(new Double(10.5), dataset.getMaxOutlier(0, 0));
        Assert.assertEquals(new Double(10.5), dataset.getMaxOutlier("R1", "C1"));
        Assert.assertEquals(outliers, dataset.getOutliers(0, 0));
        Assert.assertEquals(outliers, dataset.getOutliers("R1", "C1"));
    }

    @Test
    public void testAddList() {
        List<Double> values = Arrays.asList(new Double(1.0), new Double(2.0), new Double(3.0),
                new Double(4.0), new Double(5.0));
        dataset.add(values, "R1", "C1");

        Assert.assertEquals(1, dataset.getRowCount());
        Assert.assertEquals(1, dataset.getColumnCount());
        Assert.assertEquals(new Double(3.0), dataset.getMedianValue(0, 0));
        Assert.assertEquals(new Double(3.0), dataset.getMeanValue(0, 0));
    }

    @Test
    public void testMinMaxBoundsUpdating() {
        BoxAndWhiskerItem item1 = createItem(5.0, 5.0, 3.0, 7.0, 2.0, 8.0, 2.0, 8.0, new ArrayList());
        dataset.add(item1, "R1", "C1");
        Assert.assertEquals(2.0, dataset.getRangeLowerBound(false), 0.000001);
        Assert.assertEquals(8.0, dataset.getRangeUpperBound(false), 0.000001);

        // Add item that expands both min and max
        BoxAndWhiskerItem item2 = createItem(5.0, 5.0, 3.0, 7.0, 1.0, 9.0, 1.0, 9.0, new ArrayList());
        dataset.add(item2, "R2", "C1");
        Assert.assertEquals(1.0, dataset.getRangeLowerBound(false), 0.000001);
        Assert.assertEquals(9.0, dataset.getRangeUpperBound(false), 0.000001);

        // Add item within current bounds
        BoxAndWhiskerItem item3 = createItem(5.0, 5.0, 4.0, 6.0, 3.0, 7.0, 3.0, 7.0, new ArrayList());
        dataset.add(item3, "R3", "C1");
        Assert.assertEquals(1.0, dataset.getRangeLowerBound(false), 0.000001);
        Assert.assertEquals(9.0, dataset.getRangeUpperBound(false), 0.000001);

        // Replace the item at (R2, C1) which currently holds both min and max
        BoxAndWhiskerItem item2Updated = createItem(5.0, 5.0, 4.0, 6.0, 4.0, 6.0, 4.0, 6.0, new ArrayList());
        dataset.add(item2Updated, "R2", "C1");

        Assert.assertEquals(4.0, dataset.getRangeLowerBound(false), 0.000001);
        Assert.assertEquals(6.0, dataset.getRangeUpperBound(false), 0.000001);
    }

    @Test
    public void testAddItemWithNullOutliers() {
        BoxAndWhiskerItem item = new BoxAndWhiskerItem(new Double(1.0), new Double(2.0),
                new Double(3.0), new Double(4.0), new Double(5.0), new Double(6.0),
                null, null, Collections.EMPTY_LIST);
        dataset.add(item, "R1", "C1");

        Assert.assertTrue(Double.isNaN(dataset.getRangeLowerBound(false)));
        Assert.assertTrue(Double.isNaN(dataset.getRangeUpperBound(false)));
        Assert.assertNull(dataset.getMinOutlier(0, 0));
        Assert.assertNull(dataset.getMaxOutlier(0, 0));
        Assert.assertNull(dataset.getMinOutlier("R1", "C1"));
        Assert.assertNull(dataset.getMaxOutlier("R1", "C1"));
    }

    @Test
    public void testGettersWithNullItem() {
        // Test key-based getters when key exists with null item or keys don't exist
        Assert.assertNull(dataset.getMeanValue("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getMedianValue("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getQ1Value("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getQ3Value("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getMinRegularValue("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getMaxRegularValue("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getMinOutlier("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getMaxOutlier("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getOutliers("NonExistentRow", "NonExistentCol"));
        Assert.assertNull(dataset.getValue("NonExistentRow", "NonExistentCol"));
    }

    @Test
    public void testRowAndColumnKeyLists() {
        BoxAndWhiskerItem item1 = createItem(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, new ArrayList());
        BoxAndWhiskerItem item2 = createItem(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, new ArrayList());

        dataset.add(item1, "R1", "C1");
        dataset.add(item2, "R2", "C2");

        List rowKeys = dataset.getRowKeys();
        Assert.assertEquals(2, rowKeys.size());
        Assert.assertTrue(rowKeys.contains("R1"));
        Assert.assertTrue(rowKeys.contains("R2"));

        List colKeys = dataset.getColumnKeys();
        Assert.assertEquals(2, colKeys.size());
        Assert.assertTrue(colKeys.contains("C1"));
        Assert.assertTrue(colKeys.contains("C2"));

        Assert.assertEquals(0, dataset.getRowIndex("R1"));
        Assert.assertEquals(1, dataset.getRowIndex("R2"));
        Assert.assertEquals(-1, dataset.getRowIndex("UnknownRow"));

        Assert.assertEquals(0, dataset.getColumnIndex("C1"));
        Assert.assertEquals(1, dataset.getColumnIndex("C2"));
        Assert.assertEquals(-1, dataset.getColumnIndex("UnknownCol"));
    }

    @Test
    public void testEqualsAndHashCode() {
        DefaultBoxAndWhiskerCategoryDataset d1 = new DefaultBoxAndWhiskerCategoryDataset();
        DefaultBoxAndWhiskerCategoryDataset d2 = new DefaultBoxAndWhiskerCategoryDataset();

        Assert.assertTrue(d1.equals(d1));
        Assert.assertFalse(d1.equals(null));
        Assert.assertFalse(d1.equals("A String"));
        Assert.assertTrue(d1.equals(d2));

        BoxAndWhiskerItem item = createItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, new ArrayList());
        d1.add(item, "R1", "C1");
        Assert.assertFalse(d1.equals(d2));

        d2.add(item, "R1", "C1");
        Assert.assertTrue(d1.equals(d2));

        BoxAndWhiskerItem itemDifferent = createItem(2.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, new ArrayList());
        DefaultBoxAndWhiskerCategoryDataset d3 = new DefaultBoxAndWhiskerCategoryDataset();
        d3.add(itemDifferent, "R1", "C1");
        Assert.assertFalse(d1.equals(d3));
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        BoxAndWhiskerItem item = createItem(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, new ArrayList());
        dataset.add(item, "R1", "C1");

        DefaultBoxAndWhiskerCategoryDataset clone = (DefaultBoxAndWhiskerCategoryDataset) dataset.clone();

        Assert.assertNotSame(dataset, clone);
        Assert.assertSame(dataset.getClass(), clone.getClass());
        Assert.assertEquals(dataset, clone);

        // Modifying original dataset should not affect clone
        BoxAndWhiskerItem item2 = createItem(2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, new ArrayList());
        dataset.add(item2, "R2", "C2");

        Assert.assertFalse(dataset.equals(clone));
        Assert.assertEquals(1, clone.getRowCount());
        Assert.assertEquals(2, dataset.getRowCount());
    }

    @Test
    public void testNullKeysInAdd() {
        BoxAndWhiskerItem item = createItem(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, new ArrayList());
        try {
            dataset.add(item, null, "C1");
            Assert.fail("Expected IllegalArgumentException for null rowKey");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            dataset.add(item, "R1", null);
            Assert.fail("Expected IllegalArgumentException for null columnKey");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
}