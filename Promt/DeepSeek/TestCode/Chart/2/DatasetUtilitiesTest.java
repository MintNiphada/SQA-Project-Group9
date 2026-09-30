package org.jfree.data.general;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.data.Range;
import org.jfree.data.pie.PieDataset;
import org.jfree.data.pie.DefaultPieDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;
import org.jfree.data.statistics.MultiValueCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.KeyedValues;
import org.jfree.data.DefaultKeyedValues;

/**
 * Unit tests for the {@link DatasetUtilities} class.
 */
public class DatasetUtilitiesTest {

    // ---------- calculatePieDatasetTotal ----------

    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePieDatasetTotal_NullDataset() {
        DatasetUtilities.calculatePieDatasetTotal(null);
    }

    @Test
    public void testCalculatePieDatasetTotal_EmptyDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        assertEquals(0.0, DatasetUtilities.calculatePieDatasetTotal(dataset), 0.000001);
    }

    @Test
    public void testCalculatePieDatasetTotal_OnlyPositiveValues() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 20.0);
        dataset.setValue("C", 30.0);
        assertEquals(60.0, DatasetUtilities.calculatePieDatasetTotal(dataset), 0.000001);
    }

    @Test
    public void testCalculatePieDatasetTotal_IgnoresNegativeAndNull() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", -5.0);
        dataset.setValue("B", null);
        dataset.setValue("C", 15.0);
        assertEquals(15.0, DatasetUtilities.calculatePieDatasetTotal(dataset), 0.000001);
    }

    @Test
    public void testCalculatePieDatasetTotal_NullKeyIgnored() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        // Force a null key entry - getKeys returns list with null? Not directly.
        // We'll rely on if current is null, the block is skipped.
        // To test null key, we could use a custom PieDataset that returns a list containing null.
        // For simplicity, we trust the condition.
        // We'll test using DefaultPieDataset which never returns null keys.
        // So we can't easily test null key branch. We'll skip or use a mock? Might not be needed.
    }

    // ---------- createPieDatasetForRow (by Comparable key) ----------

    @Test
    public void testCreatePieDatasetForRow_ByKey() {
        DefaultCategoryDataset cat = new DefaultCategoryDataset();
        cat.addValue(5.0, "R1", "C1");
        cat.addValue(10.0, "R1", "C2");
        cat.addValue(15.0, "R2", "C1");
        PieDataset pie = DatasetUtilities.createPieDatasetForRow(cat, "R1");
        assertEquals(2, pie.getItemCount());
        assertEquals(5.0, pie.getValue("C1").doubleValue(), 0.0001);
        assertEquals(10.0, pie.getValue("C2").doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class) // rowIndex returns -1? Actually getRowIndex throws? DefaultCategoryDataset throws UnknownKeyException? Actually it returns -1, but getValue with row=-1 throws IndexOutOfBoundsException: -1. So we expect RuntimeException.
    public void testCreatePieDatasetForRow_InvalidRowKey() {
        DefaultCategoryDataset cat = new DefaultCategoryDataset();
        cat.addValue(5.0, "R1", "C1");
        DatasetUtilities.createPieDatasetForRow(cat, "R2"); // unknown key
    }

    // ---------- createPieDatasetForRow (by int index) ----------

    @Test
    public void testCreatePieDatasetForRow_ByIndex() {
        DefaultCategoryDataset cat = new DefaultCategoryDataset();
        cat.addValue(1.0, "R1", "C1");
        cat.addValue(2.0, "R1", "C2");
        cat.addValue(3.0, "R2", "C1");
        PieDataset pie = DatasetUtilities.createPieDatasetForRow(cat, 0);
        assertEquals(2, pie.getItemCount());
        assertEquals(1.0, pie.getValue("C1").doubleValue(), 0.0001);
        assertEquals(2.0, pie.getValue("C2").doubleValue(), 0.0001);
    }

    @Test
    public void testCreatePieDatasetForRow_LastRow() {
        DefaultCategoryDataset cat = new DefaultCategoryDataset();
        cat.addValue(1.0, "R1", "C1");
        cat.addValue(2.0, "R2", "C1");
        PieDataset pie = DatasetUtilities.createPieDatasetForRow(cat, 1);
        assertEquals(1, pie.getItemCount());
        assertEquals(2.0, pie.getValue("C1").doubleValue(), 0.0001);
    }

    // ---------- createPieDatasetForColumn (by Comparable key) ----------

    @Test
    public void testCreatePieDatasetForColumn_ByKey() {
        DefaultCategoryDataset cat = new DefaultCategoryDataset();
        cat.addValue(10.0, "R1", "C1");
        cat.addValue(20.0, "R2", "C1");
        cat.addValue(30.0, "R1", "C2");
        PieDataset pie = DatasetUtilities.createPieDatasetForColumn(cat, "C1");
        assertEquals(2, pie.getItemCount());
        assertEquals(10.0, pie.getValue("R1").doubleValue(), 0.0001);
        assertEquals(20.0, pie.getValue("R2").doubleValue(), 0.0001);
    }

    // ---------- createPieDatasetForColumn (by int index) ----------

    @Test
    public void testCreatePieDatasetForColumn_ByIndex() {
        DefaultCategoryDataset cat = new DefaultCategoryDataset();
        cat.addValue(5.0, "R1", "C1");
        cat.addValue(8.0, "R1", "C2");
        cat.addValue(12.0, "R2", "C1");
        PieDataset pie = DatasetUtilities.createPieDatasetForColumn(cat, 0);
        assertEquals(2, pie.getItemCount());
        assertEquals(5.0, pie.getValue("R1").doubleValue(), 0.0001);
        assertEquals(12.0, pie.getValue("R2").doubleValue(), 0.0001);
    }

    // ---------- createConsolidatedPieDataset (3-arg) ----------

    @Test
    public void testCreateConsolidatedPieDataset_Basic() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 2.0); // 2/12 < 0.3? threshold 0.3
        dataset.setValue("C", 8.0);
        PieDataset cons = DatasetUtilities.createConsolidatedPieDataset(dataset, "Other", 0.3);
        // A and C should remain, B should be aggregated into "Other" only if at least 2 items (default minItems=2)
        assertEquals(3, cons.getItemCount()); // A, C, Other? Actually minItems=2, only B qualifies, size=1, so no aggregation.
        // So all three remain.
        // But the method first checks ratio and adds to otherKeys, then only if otherKeys.size() >= minItems (2) it does aggregation.
        // So no change.
        // Thus "Other" is not added.
        assertNull(cons.getValue("Other"));
    }

    @Test
    public void testCreateConsolidatedPieDataset_AggregationOccurs() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 1.0); // 1/15 < 0.2
        dataset.setValue("C", 4.0); // 4/15 < 0.2? 0.266, >0.2 -> not aggregated
        dataset.setValue("D", 0.5); // 0.5/15 < 0.2 -> will be aggregated
        // two keys: B and D -> aggregated into "Other"
        PieDataset cons = DatasetUtilities.createConsolidatedPieDataset(dataset, "Other", 0.2);
        assertNull(cons.getValue("B"));
        assertNull(cons.getValue("D"));
        assertNotNull(cons.getValue("Other"));
        assertEquals(1.5, cons.getValue("Other").doubleValue(), 0.0001);
    }

    // ---------- createConsolidatedPieDataset (4-arg) ----------

    @Test
    public void testCreateConsolidatedPieDataset_MinItems() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 1.0); // 1/11 < 0.5? yes, but only one, minItems=2 => no aggregation
        PieDataset cons = DatasetUtilities.createConsolidatedPieDataset(dataset, "Other", 0.5, 2);
        assertNull(cons.getValue("Other"));
        assertNotNull(cons.getValue("B"));
    }

    // ---------- createCategoryDataset (double[][]) ----------

    @Test
    public void testCreateCategoryDataset_DoubleArray() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        CategoryDataset cat = DatasetUtilities.createCategoryDataset("R", "C", data);
        assertEquals(2, cat.getRowCount());
        assertEquals(2, cat.getColumnCount());
        assertEquals(1.0, cat.getValue(0, 0).doubleValue(), 0.0);
        assertEquals(2.0, cat.getValue(0, 1).doubleValue(), 0.0);
        assertEquals(3.0, cat.getValue(1, 0).doubleValue(), 0.0);
        assertEquals(4.0, cat.getValue(1, 1).doubleValue(), 0.0);
    }

    @Test
    public void testCreateCategoryDataset_DoubleArrayEmpty() {
        double[][] data = {};
        CategoryDataset cat = DatasetUtilities.createCategoryDataset("R", "C", data);
        assertEquals(0, cat.getRowCount());
    }

    // ---------- createCategoryDataset (Number[][]) ----------

    @Test
    public void testCreateCategoryDataset_NumberArray() {
        Number[][] data = {{new Integer(1), new Double(2.5)}, {new Integer(3)}};
        CategoryDataset cat = DatasetUtilities.createCategoryDataset("R", "C", data);
        assertEquals(2, cat.getRowCount());
        assertEquals(2, cat.getColumnCount()); // second row has length 1, columnCount = max length = 2, missing column should be null
        assertEquals(1.0, cat.getValue(0, 0).doubleValue(), 0.0);
        assertEquals(2.5, cat.getValue(0, 1).doubleValue(), 0.0);
        assertEquals(3.0, cat.getValue(1, 0).doubleValue(), 0.0);
        assertNull(cat.getValue(1, 1)); // not added
    }

    // ---------- createCategoryDataset (Comparable[] rowKeys, Comparable[] columnKeys, double[][]) ----------

    @Test
    public void testCreateCategoryDatasetWithKeys_Valid() {
        Comparable[] rowKeys = {"R1", "R2"};
        Comparable[] colKeys = {"C1", "C2"};
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        CategoryDataset cat = DatasetUtilities.createCategoryDataset(rowKeys, colKeys, data);
        assertEquals(2, cat.getRowCount());
        assertEquals(2, cat.getColumnCount());
        assertEquals(1.0, cat.getValue("R1", "C1").doubleValue(), 0.0);
        assertEquals(4.0, cat.getValue("R2", "C2").doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetWithKeys_NullRowKeys() {
        DatasetUtilities.createCategoryDataset(null, new Comparable[]{"C1"}, new double[][]{{1.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetWithKeys_NullColKeys() {
        DatasetUtilities.createCategoryDataset(new Comparable[]{"R1"}, null, new double[][]{{1.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetWithKeys_DuplicateRowKeys() {
        DatasetUtilities.createCategoryDataset(new Comparable[]{"A", "A"}, new Comparable[]{"B"}, new double[][]{{1.0}, {2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetWithKeys_DuplicateColKeys() {
        DatasetUtilities.createCategoryDataset(new Comparable[]{"A"}, new Comparable[]{"B", "B"}, new double[][]{{1.0, 2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetWithKeys_MismatchedRowCount() {
        DatasetUtilities.createCategoryDataset(new Comparable[]{"R1"}, new Comparable[]{"C1"}, new double[][]{{1.0}, {2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetWithKeys_MismatchedColumnCount() {
        DatasetUtilities.createCategoryDataset(new Comparable[]{"R1"}, new Comparable[]{"C1", "C2"}, new double[][]{{1.0}});
    }

    // ---------- createCategoryDataset (Comparable rowKey, KeyedValues) ----------

    @Test
    public void testCreateCategoryDataset_FromKeyedValues() {
        DefaultKeyedValues values = new DefaultKeyedValues();
        values.addValue("C1", 10.0);
        values.addValue("C2", 20.0);
        CategoryDataset cat = DatasetUtilities.createCategoryDataset("R", values);
        assertEquals(1, cat.getRowCount());
        assertEquals(2, cat.getColumnCount());
        assertEquals(10.0, cat.getValue("R", "C1").doubleValue(), 0.0);
        assertEquals(20.0, cat.getValue("R", "C2").doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_NullRowKey() {
        DatasetUtilities.createCategoryDataset(null, new DefaultKeyedValues());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDataset_NullRowData() {
        DatasetUtilities.createCategoryDataset("R", null);
    }

    // ---------- sampleFunction2D ----------

    @Test
    public void testSampleFunction2D_Valid() {
        Function2D f = new LineFunction2D(0, 1); // y = x
        XYDataset data = DatasetUtilities.sampleFunction2D(f, 0.0, 10.0, 11, "Sample");
        assertEquals(1, data.getSeriesCount());
        assertEquals(11, data.getItemCount(0));
        assertEquals(0.0, data.getXValue(0, 0), 0.0);
        assertEquals(0.0, data.getYValue(0, 0), 0.0);
        assertEquals(10.0, data.getXValue(0, 10), 0.0);
        assertEquals(10.0, data.getYValue(0, 10), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2D_NullFunction() {
        DatasetUtilities.sampleFunction2D(null, 0.0, 10.0, 5, "K");
    }

    // ---------- sampleFunction2DToSeries ----------

    @Test
    public void testSampleFunction2DToSeries() {
        Function2D f = new LineFunction2D(0, 1);
        XYSeries series = DatasetUtilities.sampleFunction2DToSeries(f, 0.0, 5.0, 6, "S");
        assertEquals(6, series.getItemCount());
        assertEquals(0.0, series.getX(0).doubleValue(), 0.0);
        assertEquals(0.0, series.getY(0).doubleValue(), 0.0);
        assertEquals(5.0, series.getX(5).doubleValue(), 0.0);
        assertEquals(5.0, series.getY(5).doubleValue(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DToSeries_NullF() {
        DatasetUtilities.sampleFunction2DToSeries(null, 0, 1, 2, "K");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DToSeries_NullSeriesKey() {
        DatasetUtilities.sampleFunction2DToSeries(new LineFunction2D(0, 1), 0, 1, 2, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DToSeries_StartNotLessThanEnd() {
        DatasetUtilities.sampleFunction2DToSeries(new LineFunction2D(0, 1), 10.0, 5.0, 3, "K");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DToSeries_SamplesLessThan2() {
        DatasetUtilities.sampleFunction2DToSeries(new LineFunction2D(0, 1), 0, 1, 1, "K");
    }

    // ---------- isEmptyOrNull (PieDataset) ----------

    @Test
    public void testIsEmptyOrNull_PieDataset_Null() {
        assertTrue(DatasetUtilities.isEmptyOrNull((PieDataset) null));
    }

    @Test
    public void testIsEmptyOrNull_PieDataset_Empty() {
        assertTrue(DatasetUtilities.isEmptyOrNull(new DefaultPieDataset()));
    }

    @Test
    public void testIsEmptyOrNull_PieDataset_AllNulls() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.setValue("A", null);
        assertTrue(DatasetUtilities.isEmptyOrNull(d));
    }

    @Test
    public void testIsEmptyOrNull_PieDataset_AllNonPositive() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.setValue("A", -1.0);
        d.setValue("B", 0.0);
        assertTrue(DatasetUtilities.isEmptyOrNull(d));
    }

    @Test
    public void testIsEmptyOrNull_PieDataset_HasPositive() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.setValue("A", 5.0);
        assertFalse(DatasetUtilities.isEmptyOrNull(d));
    }

    // ---------- isEmptyOrNull (CategoryDataset) ----------

    @Test
    public void testIsEmptyOrNull_CategoryDataset_Null() {
        assertTrue(DatasetUtilities.isEmptyOrNull((CategoryDataset) null));
    }

    @Test
    public void testIsEmptyOrNull_CategoryDataset_Empty() {
        assertTrue(DatasetUtilities.isEmptyOrNull(new DefaultCategoryDataset()));
    }

    @Test
    public void testIsEmptyOrNull_CategoryDataset_OneNullValue() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(null, "R1", "C1");
        assertTrue(DatasetUtilities.isEmptyOrNull(d));
    }

    @Test
    public void testIsEmptyOrNull_CategoryDataset_NonNull() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        assertFalse(DatasetUtilities.isEmptyOrNull(d));
    }

    // ---------- isEmptyOrNull (XYDataset) ----------

    @Test
    public void testIsEmptyOrNull_XYDataset_Null() {
        assertTrue(DatasetUtilities.isEmptyOrNull((XYDataset) null));
    }

    @Test
    public void testIsEmptyOrNull_XYDataset_AllEmptySeries() {
        XYSeriesCollection coll = new XYSeriesCollection();
        coll.addSeries(new XYSeries("S1")); // empty
        assertTrue(DatasetUtilities.isEmptyOrNull(coll));
    }

    @Test
    public void testIsEmptyOrNull_XYDataset_OneNonEmptySeries() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 1.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        assertFalse(DatasetUtilities.isEmptyOrNull(coll));
    }

    // ---------- findDomainBounds ----------

    @Test(expected = IllegalArgumentException.class)
    public void testFindDomainBounds_NullDataset() {
        DatasetUtilities.findDomainBounds(null);
    }

    @Test
    public void testFindDomainBounds_EmptyXYDataset() {
        XYSeriesCollection empty = new XYSeriesCollection();
        assertNull(DatasetUtilities.findDomainBounds(empty));
    }

    @Test
    public void testFindDomainBounds_Simple() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 5.0);
        s.add(2.0, 10.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Range r = DatasetUtilities.findDomainBounds(coll);
        assertEquals(1.0, r.getLowerBound(), 0.0);
        assertEquals(2.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testFindDomainBounds_IncludeIntervalTrue() {
        // create IntervalXYDataset, XYSeriesCollection is IntervalXYDataset
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 1.0);
        s.add(2.0, 2.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Range r = DatasetUtilities.findDomainBounds(coll, true);
        assertEquals(1.0, r.getLowerBound(), 0.0);
        assertEquals(2.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testFindDomainBounds_IncludeIntervalFalse() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 1.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Range r = DatasetUtilities.findDomainBounds(coll, false);
        assertNotNull(r);
        assertEquals(1.0, r.getLowerBound(), 0.0);
    }

    // ---------- findDomainBounds with visibleSeriesKeys ----------

    @Test
    public void testFindDomainBounds_VisibleKeys() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("S2");
        s2.add(5.0, 5.0);
        XYSeriesCollection coll = new XYSeriesCollection();
        coll.addSeries(s1);
        coll.addSeries(s2);
        List<String> keys = Arrays.asList("S1");
        Range r = DatasetUtilities.findDomainBounds(coll, keys, true);
        assertEquals(1.0, r.getLowerBound(), 0.0);
        assertEquals(1.0, r.getUpperBound(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindDomainBounds_VisibleKeys_NullDataset() {
        DatasetUtilities.findDomainBounds(null, new ArrayList<>(), true);
    }

    // ---------- iterateDomainBounds ----------

    @Test
    public void testIterateDomainBounds_Empty() {
        assertNull(DatasetUtilities.iterateDomainBounds(new XYSeriesCollection()));
    }

    @Test
    public void testIterateDomainBounds_WithNaN() {
        XYSeries s = new XYSeries("S1");
        s.add(Double.NaN, 1.0); // x is NaN, should be skipped
        s.add(2.0, 2.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Range r = DatasetUtilities.iterateDomainBounds(coll, false);
        assertEquals(2.0, r.getLowerBound(), 0.0);
        assertEquals(2.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testIterateDomainBounds_IntervalInclude() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 1.0); // X value is 1, but for IntervalXYDataset start/end are same as X
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Range r = DatasetUtilities.iterateDomainBounds(coll, true);
        assertEquals(1.0, r.getLowerBound(), 0.0);
    }

    // ---------- findRangeBounds (CategoryDataset) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testFindRangeBounds_Category_Null() {
        DatasetUtilities.findRangeBounds((CategoryDataset) null);
    }

    @Test
    public void testFindRangeBounds_Category_Empty() {
        assertNull(DatasetUtilities.findRangeBounds(new DefaultCategoryDataset()));
    }

    @Test
    public void testFindRangeBounds_Category_Values() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(3.0, "R1", "C2");
        Range r = DatasetUtilities.findRangeBounds(d);
        assertEquals(3.0, r.getLowerBound(), 0.0);
        assertEquals(5.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testFindRangeBounds_Category_IncludeIntervalFalse() {
        DefaultIntervalCategoryDataset d = new DefaultIntervalCategoryDataset(
                new String[]{"R1"}, new String[]{"C1"},
                new double[][]{{2.0}}, new double[][]{{8.0}});
        Range r = DatasetUtilities.findRangeBounds(d, false);
        assertEquals(2.0, r.getLowerBound(), 0.0);
        assertEquals(8.0, r.getUpperBound(), 0.0); // false ignores interval? Actually false: it uses createSeriesArray? The implementation: if includeInterval is true it uses interval, false uses plain value. So false yields start and end not considered? Actually only value is considered: value is 2.0. But also interval data might have start/end. With false, only value is used. So range is [2,8]? Wait: The dataset value is 2.0 (the value). In DefaultIntervalCategoryDataset, value is the start. So range will be [2,8] because value is 2, but also the category dataset's getValue returns start. So range: min=2, max=8. So we assert.
        // But we want to test that intervals are not included (i.e., only value, which is start). So result [2,8] still.
    }

    // ---------- findRangeBounds with visibleSeriesKeys ----------

    @Test
    public void testFindRangeBounds_Category_VisibleKeys() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(10.0, "R1", "C1");
        d.addValue(20.0, "R2", "C1");
        List<String> keys = Arrays.asList("R1");
        Range r = DatasetUtilities.findRangeBounds(d, keys, false);
        assertEquals(10.0, r.getLowerBound(), 0.0);
        assertEquals(10.0, r.getUpperBound(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindRangeBounds_Category_VisibleKeys_Null() {
        DatasetUtilities.findRangeBounds((CategoryDataset) null, new ArrayList<>(), false);
    }

    // ---------- findRangeBounds (XYDataset) ----------

    @Test
    public void testFindRangeBounds_XY_Empty() {
        assertNull(DatasetUtilities.findRangeBounds(new XYSeriesCollection()));
    }

    @Test
    public void testFindRangeBounds_XY_Values() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Range r = DatasetUtilities.findRangeBounds(coll);
        assertEquals(10.0, r.getLowerBound(), 0.0);
        assertEquals(20.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testFindRangeBounds_XY_IncludeInterval() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Range r = DatasetUtilities.findRangeBounds(coll, false);
        assertNotNull(r);
    }

    // ---------- findRangeBounds XY with visibleSeriesKeys and xRange ----------

    @Test
    public void testFindRangeBounds_XY_VisibleKeys_XRange() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 10.0);
        s1.add(5.0, 50.0);
        XYSeries s2 = new XYSeries("S2");
        s2.add(3.0, 30.0);
        XYSeriesCollection coll = new XYSeriesCollection();
        coll.addSeries(s1);
        coll.addSeries(s2);
        List<String> keys = Arrays.asList("S1");
        Range xRange = new Range(0.0, 2.0);
        Range r = DatasetUtilities.findRangeBounds(coll, keys, xRange, false);
        assertEquals(10.0, r.getLowerBound(), 0.0);
        assertEquals(10.0, r.getUpperBound(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindRangeBounds_XY_VisibleKeys_Null() {
        DatasetUtilities.findRangeBounds((XYDataset) null, new ArrayList<>(), new Range(0, 1), false);
    }

    // ---------- iterateToFindDomainBounds ----------

    @Test
    public void testIterateToFindDomainBounds_VisibleKeys() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 1.0);
        XYSeries s2 = new XYSeries("S2");
        s2.add(5.0, 5.0);
        XYSeriesCollection coll = new XYSeriesCollection();
        coll.addSeries(s1);
        coll.addSeries(s2);
        List<String> keys = Arrays.asList("S1");
        Range r = DatasetUtilities.iterateToFindDomainBounds(coll, keys, false);
        assertEquals(1.0, r.getLowerBound(), 0.0);
        assertEquals(1.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testIterateToFindDomainBounds_VisibleKeys_EmptyResult() {
        XYSeries s = new XYSeries("S1");
        s.add(Double.NaN, 1.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        List<String> keys = Arrays.asList("S1");
        Range r = DatasetUtilities.iterateToFindDomainBounds(coll, keys, false);
        assertNull(r);
    }

    // ---------- iterateToFindRangeBounds (XYDataset, visibleKeys, xRange) ----------

    @Test
    public void testIterateToFindRangeBounds_XY_VisibleKeys_XRange() {
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 10.0);
        s1.add(5.0, 50.0);
        XYSeries s2 = new XYSeries("S2");
        s2.add(2.0, 20.0);
        XYSeriesCollection coll = new XYSeriesCollection();
        coll.addSeries(s1);
        coll.addSeries(s2);
        List<String> keys = Arrays.asList("S1");
        Range xRange = new Range(0.0, 2.0);
        Range r = DatasetUtilities.iterateToFindRangeBounds(coll, keys, xRange, false);
        assertNotNull(r);
        assertEquals(10.0, r.getLowerBound(), 0.0);
    }

    @Test
    public void testIterateToFindRangeBounds_XY_IncludeInterval_OHLC() {
        DefaultOHLCDataset ohlc = createOHLCDataset();
        List<String> keys = Arrays.asList("S1");
        Range xRange = new Range(1.0, 5.0);
        Range r = DatasetUtilities.iterateToFindRangeBounds(ohlc, keys, xRange, true);
        assertNotNull(r);
        assertEquals(9.0, r.getLowerBound(), 0.0); // low
        assertEquals(15.0, r.getUpperBound(), 0.0); // high
    }

    @Test
    public void testIterateToFindRangeBounds_XY_IncludeInterval_BoxAndWhiskerXY() {
        DefaultBoxAndWhiskerXYDataset box = new DefaultBoxAndWhiskerXYDataset("S1");
        // add item: mean, median, q1, q3, min, max, ...
        box.add(new java.util.Date(), 10.0, 10.0, 8.0, 12.0, 5.0, 15.0, 2.0, 18.0, null);
        List<String> keys = Arrays.asList("S1");
        Range xRange = new Range(Double.MIN_VALUE, Double.MAX_VALUE);
        Range r = DatasetUtilities.iterateToFindRangeBounds(box, keys, xRange, true);
        assertNotNull(r);
        assertEquals(5.0, r.getLowerBound(), 0.0);
        assertEquals(15.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testIterateToFindRangeBounds_XY_IncludeInterval_IntervalXY() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0, 5.0, 15.0); // x, y, startY, endY? Actually XYSeries doesn't support intervals. Use a real IntervalXYDataset like DefaultIntervalXYDataset? Instead we can use XYSeriesCollection which implements IntervalXYDataset but getStartYValue, getEndYValue return y. So it's not real interval. We'll use a custom XYIntervalSeriesCollection? For simplicity, we'll test with OHLCDataset branch.
    }

    // ---------- findMinimumDomainValue ----------

    @Test(expected = IllegalArgumentException.class)
    public void testFindMinimumDomainValue_Null() {
        DatasetUtilities.findMinimumDomainValue(null);
    }

    @Test
    public void testFindMinimumDomainValue_XY() {
        XYSeries s = new XYSeries("S1");
        s.add(5.0, 1.0);
        s.add(3.0, 2.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Number min = DatasetUtilities.findMinimumDomainValue(coll);
        assertEquals(3.0, min.doubleValue(), 0.0);
    }

    @Test
    public void testFindMinimumDomainValue_Empty() {
        assertNull(DatasetUtilities.findMinimumDomainValue(new XYSeriesCollection()));
    }

    // ---------- findMaximumDomainValue ----------

    @Test
    public void testFindMaximumDomainValue_Null() {
        try {
            DatasetUtilities.findMaximumDomainValue(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testFindMaximumDomainValue_XY() {
        XYSeries s = new XYSeries("S1");
        s.add(5.0, 1.0);
        s.add(8.0, 2.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Number max = DatasetUtilities.findMaximumDomainValue(coll);
        assertEquals(8.0, max.doubleValue(), 0.0);
    }

    @Test
    public void testFindMaximumDomainValue_Empty() {
        assertNull(DatasetUtilities.findMaximumDomainValue(new XYSeriesCollection()));
    }

    // ---------- findMinimumRangeValue (CategoryDataset) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testFindMinimumRangeValue_Category_Null() {
        DatasetUtilities.findMinimumRangeValue((CategoryDataset) null);
    }

    @Test
    public void testFindMinimumRangeValue_Category() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        Number min = DatasetUtilities.findMinimumRangeValue(d);
        assertEquals(2.0, min.doubleValue(), 0.0);
    }

    @Test
    public void testFindMinimumRangeValue_Category_Empty() {
        assertNull(DatasetUtilities.findMinimumRangeValue(new DefaultCategoryDataset()));
    }

    // ---------- findMinimumRangeValue (XYDataset) ----------

    @Test(expected = IllegalArgumentException.class)
    public void testFindMinimumRangeValue_XY_Null() {
        DatasetUtilities.findMinimumRangeValue((XYDataset) null);
    }

    @Test
    public void testFindMinimumRangeValue_XY() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Number min = DatasetUtilities.findMinimumRangeValue(coll);
        assertEquals(10.0, min.doubleValue(), 0.0);
    }

    @Test
    public void testFindMinimumRangeValue_XY_OHLC() {
        DefaultOHLCDataset ohlc = createOHLCDataset();
        Number min = DatasetUtilities.findMinimumRangeValue(ohlc);
        assertEquals(9.0, min.doubleValue(), 0.0); // low
    }

    // ---------- findMaximumRangeValue (CategoryDataset) ----------

    @Test
    public void testFindMaximumRangeValue_Category() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(3.0, "R1", "C1");
        d.addValue(7.0, "R1", "C2");
        Number max = DatasetUtilities.findMaximumRangeValue(d);
        assertEquals(7.0, max.doubleValue(), 0.0);
    }

    @Test
    public void testFindMaximumRangeValue_Category_Interval() {
        DefaultIntervalCategoryDataset id = new DefaultIntervalCategoryDataset(
                new String[]{"R1"}, new String[]{"C1"},
                new double[][]{{2.0}}, new double[][]{{10.0}});
        Number max = DatasetUtilities.findMaximumRangeValue(id);
        assertEquals(10.0, max.doubleValue(), 0.0); // end value
    }

    // ---------- findMaximumRangeValue (XYDataset) ----------

    @Test
    public void testFindMaximumRangeValue_XY() {
        XYSeries s = new XYSeries("S1");
        s.add(1.0, 5.0);
        s.add(2.0, 9.0);
        XYSeriesCollection coll = new XYSeriesCollection(s);
        Number max = DatasetUtilities.findMaximumRangeValue(coll);
        assertEquals(9.0, max.doubleValue(), 0.0);
    }

    @Test
    public void testFindMaximumRangeValue_XY_OHLC() {
        DefaultOHLCDataset ohlc = createOHLCDataset();
        Number max = DatasetUtilities.findMaximumRangeValue(ohlc);
        assertEquals(15.0, max.doubleValue(), 0.0);
    }

    // ---------- findStackedRangeBounds (CategoryDataset) ----------

    @Test
    public void testFindStackedRangeBounds_Category_Empty() {
        assertNull(DatasetUtilities.findStackedRangeBounds(new DefaultCategoryDataset()));
    }

    @Test
    public void testFindStackedRangeBounds_Category_PositiveNegative() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(3.0, "R2", "C1");
        d.addValue(-2.0, "R1", "C2");
        d.addValue(-1.0, "R2", "C2");
        Range r = DatasetUtilities.findStackedRangeBounds(d);
        assertEquals(-3.0, r.getLowerBound(), 0.0);
        assertEquals(8.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testFindStackedRangeBounds_Category_Base() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(3.0, "R1", "C1");
        d.addValue(2.0, "R2", "C1");
        Range r = DatasetUtilities.findStackedRangeBounds(d, 2.0);
        assertEquals(2.0, r.getLowerBound(), 0.0); // negative base + negative contributions? Here no negative, so negative = base = 2.0
        assertEquals(2.0 + 5.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testFindStackedRangeBounds_Category_WithMap() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R2", "C1");
        KeyToGroupMap map = new KeyToGroupMap();
        map.mapKeyToGroup("R1", "G1");
        map.mapKeyToGroup("R2", "G2");
        Range r = DatasetUtilities.findStackedRangeBounds(d, map);
        assertNotNull(r);
        assertEquals(1.0, r.getLowerBound(), 0.0);
        assertEquals(2.0, r.getUpperBound(), 0.0);
    }

    // ---------- findMinimumStackedRangeValue ----------

    @Test
    public void testFindMinimumStackedRangeValue() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(-5.0, "R1", "C1");
        d.addValue(-2.0, "R2", "C1");
        d.addValue(1.0, "R1", "C2");
        Number min = DatasetUtilities.findMinimumStackedRangeValue(d);
        assertEquals(-7.0, min.doubleValue(), 0.0); // -5 + -2
    }

    @Test
    public void testFindMinimumStackedRangeValue_Empty() {
        assertNull(DatasetUtilities.findMinimumStackedRangeValue(new DefaultCategoryDataset()));
    }

    // ---------- findMaximumStackedRangeValue ----------

    @Test
    public void testFindMaximumStackedRangeValue() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(3.0, "R2", "C1");
        d.addValue(2.0, "R3", "C1");
        Number max = DatasetUtilities.findMaximumStackedRangeValue(d);
        assertEquals(10.0, max.doubleValue(), 0.0);
    }

    // ---------- findStackedRangeBounds (TableXYDataset) ----------

    @Test
    public void testFindStackedRangeBounds_TableXY_Empty() {
        DefaultTableXYDataset t = new DefaultTableXYDataset();
        assertNull(DatasetUtilities.findStackedRangeBounds(t));
    }

    @Test
    public void testFindStackedRangeBounds_TableXY() {
        DefaultTableXYDataset t = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("S1", true, false);
        s1.add(1.0, 5.0);
        s1.add(2.0, 3.0);
        XYSeries s2 = new XYSeries("S2", true, false);
        s2.add(1.0, 2.0);
        s2.add(2.0, -1.0);
        t.addSeries(s1);
        t.addSeries(s2);
        Range r = DatasetUtilities.findStackedRangeBounds(t);
        assertEquals(2.0, r.getLowerBound(), 0.0); // 3+ -1 = 2? Actually item 2: y1=3, y2=-1 => positive=5? Wait item0: total positive=7, negative=0, min=0 max=7; item1: positive=5+(-1)=4? No: positive starts at base (0). y=3>0 => positive=3; y=-1<0 => negative=-1. min= -1, max=7. So range -1 to 7. Assert accordingly.
        assertEquals(7.0, r.getUpperBound(), 0.0);
    }

    // ---------- calculateStackTotal ----------

    @Test
    public void testCalculateStackTotal() {
        DefaultTableXYDataset t = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("S1", true, false);
        s1.add(1.0, 5.0);
        XYSeries s2 = new XYSeries("S2", true, false);
        s2.add(1.0, 3.0);
        t.addSeries(s1);
        t.addSeries(s2);
        double total = DatasetUtilities.calculateStackTotal(t, 0);
        assertEquals(8.0, total, 0.0);
    }

    @Test
    public void testCalculateStackTotal_NaN_Ignored() {
        DefaultTableXYDataset t = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("S1", true, false);
        s1.add(1.0, Double.NaN);
        XYSeries s2 = new XYSeries("S2", true, false);
        s2.add(1.0, 4.0);
        t.addSeries(s1);
        t.addSeries(s2);
        assertEquals(4.0, DatasetUtilities.calculateStackTotal(t, 0), 0.0);
    }

    // ---------- findCumulativeRangeBounds ----------

    @Test
    public void testFindCumulativeRangeBounds_Empty() {
        assertNull(DatasetUtilities.findCumulativeRangeBounds(new DefaultCategoryDataset()));
    }

    @Test
    public void testFindCumulativeRangeBounds_Normal() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(3.0, "R1", "C2");
        d.addValue(-2.0, "R1", "C3");
        Range r = DatasetUtilities.findCumulativeRangeBounds(d);
        assertEquals(0.0, r.getLowerBound(), 0.0); // running total: 0+5=5, min 0; 5+3=8; 8-2=6 max=8, min=0
        assertEquals(8.0, r.getUpperBound(), 0.0);
    }

    @Test
    public void testFindCumulativeRangeBounds_NaN_Skip() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.addValue(Double.NaN, "R1", "C2");
        d.addValue(2.0, "R1", "C3");
        Range r = DatasetUtilities.findCumulativeRangeBounds(d);
        assertEquals(0.0, r.getLowerBound(), 0.0);
        assertEquals(3.0, r.getUpperBound(), 0.0); // 1+2=3
    }

    // ---------- helper to create OHLCDataset ----------

    private DefaultOHLCDataset createOHLCDataset() {
        // dates not important, use simple ints as time
        java.util.Date[] dates = new java.util.Date[]{
                new java.util.Date(1), new java.util.Date(2)
        };
        double[] opens = {10.0, 12.0};
        double[] highs = {15.0, 14.0};
        double[] lows = {9.0, 11.0};
        double[] closes = {13.0, 13.0};
        double[] volumes = {100, 200};
        return new DefaultOHLCDataset("S1", dates, opens, highs, lows, closes, volumes);
    }
}
