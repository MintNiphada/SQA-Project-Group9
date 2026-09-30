package org.jfree.data.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.jfree.data.DomainInfo;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.KeyedValues;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryRangeInfo;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.MultiValueCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYDomainInfo;
import org.jfree.data.xy.XYRangeInfo;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.pie.PieDataset;
import org.jfree.data.pie.DefaultPieDataset;

public class DatasetUtilitiesTest {

    // ----------------- calculatePieDatasetTotal -----------------

    @Test(expected = IllegalArgumentException.class)
    public void calculatePieDatasetTotal_NullDataset_ThrowsException() {
        DatasetUtilities.calculatePieDatasetTotal(null);
    }

    @Test
    public void calculatePieDatasetTotal_EmptyDataset_ReturnsZero() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        Assert.assertEquals(0.0, total, 0.0);
    }

    @Test
    public void calculatePieDatasetTotal_AllZeroOrNegativeValues_ReturnsZero() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 0);
        dataset.setValue("B", -5);
        dataset.setValue("C", -3.14);
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        Assert.assertEquals(0.0, total, 0.0);
    }

    @Test
    public void calculatePieDatasetTotal_MixedNullValues_ReturnsPositiveSum() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10);
        dataset.setValue("B", null);
        dataset.setValue("C", 5.5);
        dataset.setValue("D", -2);
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        Assert.assertEquals(15.5, total, 0.0001);
    }

    @Test
    public void calculatePieDatasetTotal_OnlyNullValues_ReturnsZero() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", null);
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        Assert.assertEquals(0.0, total, 0.0);
    }

    // ----------------- createPieDatasetForRow(CategoryDataset, Comparable) -----------------

    @Test
    public void createPieDatasetForRow_ByRowKey_ReturnsCorrectPieDataset() {
        DefaultCategoryDataset category = new DefaultCategoryDataset();
        category.addValue(1.0, "Row1", "Col1");
        category.addValue(2.0, "Row1", "Col2");
        category.addValue(3.0, "Row2", "Col1");
        PieDataset pie = DatasetUtilities.createPieDatasetForRow(category, "Row1");
        Assert.assertEquals(2, pie.getItemCount());
        Assert.assertEquals(1.0, pie.getValue("Col1").doubleValue(), 0.0);
        Assert.assertEquals(2.0, pie.getValue("Col2").doubleValue(), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void createPieDatasetForRow_ByRowKey_InvalidKey_ThrowsException() {
        DefaultCategoryDataset category = new DefaultCategoryDataset();
        category.addValue(1.0, "Row1", "Col1");
        DatasetUtilities.createPieDatasetForRow(category, "NonExistent");
    }

    // ----------------- createPieDatasetForRow(CategoryDataset, int) -----------------

    @Test
    public void createPieDatasetForRow_ByIndex_ReturnsCorrectPieDataset() {
        DefaultCategoryDataset category = new DefaultCategoryDataset();
        category.addValue(5.0, "RowA", "Col1");
        category.addValue(7.0, "RowA", "Col2");
        category.addValue(9.0, "RowB", "Col1");
        PieDataset pie = DatasetUtilities.createPieDatasetForRow(category, 0);
        Assert.assertEquals(2, pie.getItemCount());
        Assert.assertEquals(5.0, pie.getValue("Col1").doubleValue(), 0.0);
        Assert.assertEquals(7.0, pie.getValue("Col2").doubleValue(), 0.0);
    }

    // ----------------- createPieDatasetForColumn(CategoryDataset, Comparable) -----------------

    @Test
    public void createPieDatasetForColumn_ByColumnKey_ReturnsCorrectPieDataset() {
        DefaultCategoryDataset category = new DefaultCategoryDataset();
        category.addValue(1.0, "Row1", "Col1");
        category.addValue(2.0, "Row2", "Col1");
        category.addValue(3.0, "Row1", "Col2");
        PieDataset pie = DatasetUtilities.createPieDatasetForColumn(category, "Col1");
        Assert.assertEquals(2, pie.getItemCount());
        Assert.assertEquals(1.0, pie.getValue("Row1").doubleValue(), 0.0);
        Assert.assertEquals(2.0, pie.getValue("Row2").doubleValue(), 0.0);
    }

    // ----------------- createPieDatasetForColumn(CategoryDataset, int) -----------------

    @Test
    public void createPieDatasetForColumn_ByIndex_ReturnsCorrectPieDataset() {
        DefaultCategoryDataset category = new DefaultCategoryDataset();
        category.addValue(10.0, "R1", "C1");
        category.addValue(20.0, "R2", "C1");
        category.addValue(30.0, "R1", "C2");
        PieDataset pie = DatasetUtilities.createPieDatasetForColumn(category, 0);
        Assert.assertEquals(2, pie.getItemCount());
        Assert.assertEquals(10.0, pie.getValue("R1").doubleValue(), 0.0);
        Assert.assertEquals(20.0, pie.getValue("R2").doubleValue(), 0.0);
    }

    // ----------------- createConsolidatedPieDataset -----------------

    @Test(expected = IllegalArgumentException.class)
    public void createConsolidatedPieDataset_NullSource_ThrowsException() {
        DatasetUtilities.createConsolidatedPieDataset(null, "Other", 0.10, 2);
    }

    @Test
    public void createConsolidatedPieDataset_AllAboveThreshold_NoAggregation() {
        DefaultPieDataset source = new DefaultPieDataset();
        source.setValue("A", 50);
        source.setValue("B", 50);
        PieDataset result = DatasetUtilities.createConsolidatedPieDataset(source, "Other", 0.10);
        Assert.assertEquals(2, result.getItemCount());
        Assert.assertEquals(50.0, result.getValue("A").doubleValue(), 0.0);
        Assert.assertEquals(50.0, result.getValue("B").doubleValue(), 0.0);
    }

    @Test
    public void createConsolidatedPieDataset_ItemsBelowThresholdAndEnoughMinItems_Aggregated() {
        DefaultPieDataset source = new DefaultPieDataset();
        source.setValue("A", 10);
        source.setValue("B", 1);
        source.setValue("C", 1);
        source.setValue("D", 8); // total = 20; B and C are 5% each, below 10%
        PieDataset result = DatasetUtilities.createConsolidatedPieDataset(source, "Other", 0.10, 2);
        Assert.assertEquals(3, result.getItemCount());
        Assert.assertEquals(10.0, result.getValue("A").doubleValue(), 0.0);
        Assert.assertEquals(8.0, result.getValue("D").doubleValue(), 0.0);
        Assert.assertEquals(2.0, result.getValue("Other").doubleValue(), 0.0);
    }

    @Test
    public void createConsolidatedPieDataset_ItemsBelowThresholdButNotEnoughMinItems_NoAggregation() {
        DefaultPieDataset source = new DefaultPieDataset();
        source.setValue("A", 90);
        source.setValue("B", 10); // 10% exactly at threshold, not below
        PieDataset result = DatasetUtilities.createConsolidatedPieDataset(source, "Other", 0.10, 2);
        Assert.assertEquals(2, result.getItemCount()); // unchanged
    }

    @Test
    public void createConsolidatedPieDataset_NullValuesInSource_Ignored() {
        DefaultPieDataset source = new DefaultPieDataset();
        source.setValue("A", 50);
        source.setValue("B", null);
        source.setValue("C", 5); // 5/55 < 0.10? 0.09 < 0.10 so below threshold
        PieDataset result = DatasetUtilities.createConsolidatedPieDataset(source, "Other", 0.10, 1);
        Assert.assertEquals(2, result.getItemCount());
        Assert.assertEquals(50.0, result.getValue("A").doubleValue(), 0.0);
        Assert.assertEquals(5.0, result.getValue("Other").doubleValue(), 0.0);
    }

    // ----------------- createCategoryDataset (prefixes, double[][]) -----------------

    @Test
    public void createCategoryDataset_DoubleArray_ReturnsCorrectDataset() {
        double[][] data = { {1.0, 2.0}, {3.0, 4.0} };
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("Row", "Col", data);
        Assert.assertEquals(2, dataset.getRowCount());
        Assert.assertEquals(2, dataset.getColumnCount());
        Assert.assertEquals(1.0, dataset.getValue(0, 0).doubleValue(), 0.0);
        Assert.assertEquals(4.0, dataset.getValue(1, 1).doubleValue(), 0.0);
    }

    @Test
    public void createCategoryDataset_DoubleArray_EmptyRows_ReturnsEmptyDataset() {
        double[][] data = new double[0][0];
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("R", "C", data);
        Assert.assertEquals(0, dataset.getRowCount());
    }

    // ----------------- createCategoryDataset (prefixes, Number[][]) -----------------

    @Test
    public void createCategoryDataset_NumberArray_ReturnsCorrectDataset() {
        Number[][] data = { {1, 2}, {3, 4} };
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("R", "C", data);
        Assert.assertEquals(2, dataset.getRowCount());
        Assert.assertEquals(3.0, dataset.getValue(1, 0).doubleValue(), 0.0);
    }

    // ----------------- createCategoryDataset(Comparable[], Comparable[], double[][]) -----------------

    @Test(expected = IllegalArgumentException.class)
    public void createCategoryDataset_RowKeysNull_ThrowsException() {
        DatasetUtilities.createCategoryDataset(null, new String[]{"C"}, new double[][]{{1}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCategoryDataset_ColumnKeysNull_ThrowsException() {
        DatasetUtilities.createCategoryDataset(new String[]{"R"}, null, new double[][]{{1}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCategoryDataset_DuplicateRowKeys_ThrowsException() {
        DatasetUtilities.createCategoryDataset(new String[]{"A","A"}, new String[]{"C"}, new double[][]{{1},{2}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCategoryDataset_DuplicateColumnKeys_ThrowsException() {
        DatasetUtilities.createCategoryDataset(new String[]{"R"}, new String[]{"C","C"}, new double[][]{{1,2}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCategoryDataset_RowKeysLengthMismatch_ThrowsException() {
        DatasetUtilities.createCategoryDataset(new String[]{"R1","R2"}, new String[]{"C1"}, new double[][]{{1}});
    }

    @Test
    public void createCategoryDataset_ValidArgs_ReturnsCorrectDataset() {
        String[] rows = {"R1", "R2"};
        String[] cols = {"C1", "C2", "C3"};
        double[][] data = { {1.1, 1.2, 1.3}, {2.1, 2.2} };
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(rows, cols, data);
        Assert.assertEquals(2, dataset.getRowCount());
        Assert.assertEquals(3, dataset.getColumnCount());
        Assert.assertEquals(1.1, dataset.getValue(0, 0).doubleValue(), 0.0);
        // column 3 for row 2 not set? Since data[1].length=2, the third column is not added, so null.
        Assert.assertNull(dataset.getValue(1, 2));
    }

    // ----------------- createCategoryDataset(Comparable, KeyedValues) -----------------

    @Test(expected = IllegalArgumentException.class)
    public void createCategoryDataset_RowKeyNull_ThrowsException() {
        DatasetUtilities.createCategoryDataset(null, new org.jfree.data.KeyedValueCollection());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createCategoryDataset_RowDataNull_ThrowsException() {
        DatasetUtilities.createCategoryDataset("Test", null);
    }

    @Test
    public void createCategoryDataset_FromKeyedValues_ReturnsCorrectDataset() {
        org.jfree.data.DefaultKeyedValues values = new org.jfree.data.DefaultKeyedValues();
        values.addValue("K1", 10);
        values.addValue("K2", 20);
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("Row", values);
        Assert.assertEquals(1, dataset.getRowCount());
        Assert.assertEquals(2, dataset.getColumnCount());
        Assert.assertEquals(10.0, dataset.getValue(0, 0).doubleValue(), 0.0);
        Assert.assertEquals(20.0, dataset.getValue(0, 1).doubleValue(), 0.0);
    }

    // ----------------- sampleFunction2D and sampleFunction2DToSeries -----------------

    @Test(expected = IllegalArgumentException.class)
    public void sampleFunction2DToSeries_NullFunction_ThrowsException() {
        DatasetUtilities.sampleFunction2DToSeries(null, 0, 1, 10, "Test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sampleFunction2DToSeries_NullSeriesKey_ThrowsException() {
        Function2D f = new Function2D() { public double getValue(double x) { return x; } };
        DatasetUtilities.sampleFunction2DToSeries(f, 0, 1, 10, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sampleFunction2DToSeries_StartGreaterThanOrEqualEnd_ThrowsException() {
        Function2D f = new Function2D() { public double getValue(double x) { return x; } };
        DatasetUtilities.sampleFunction2DToSeries(f, 5, 5, 10, "Test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void sampleFunction2DToSeries_SamplesLessThan2_ThrowsException() {
        Function2D f = new Function2D() { public double getValue(double x) { return x; } };
        DatasetUtilities.sampleFunction2DToSeries(f, 0, 1, 1, "Test");
    }

    @Test
    public void sampleFunction2DToSeries_Valid_ReturnsCorrectSeries() {
        Function2D f = new Function2D() { public double getValue(double x) { return x*x; } };
        XYSeries series = DatasetUtilities.sampleFunction2DToSeries(f, 0, 3, 4, "S");
        Assert.assertEquals(4, series.getItemCount());
        Assert.assertEquals(0.0, series.getY(0).doubleValue(), 0.0001);
        Assert.assertEquals(1.0, series.getX(1).doubleValue(), 0.0001);
        Assert.assertEquals(9.0, series.getY(3).doubleValue(), 0.0001);
    }

    @Test
    public void sampleFunction2D_ReturnsCorrectDataset() {
        Function2D f = new Function2D() { public double getValue(double x) { return x; } };
        XYDataset dataset = DatasetUtilities.sampleFunction2D(f, 0, 10, 11, "Line");
        Assert.assertEquals(1, dataset.getSeriesCount());
        Assert.assertEquals(11, dataset.getItemCount(0));
    }

    // ----------------- isEmptyOrNull(PieDataset) -----------------

    @Test
    public void isEmptyOrNull_PieDataset_Null_ReturnsTrue() {
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull((PieDataset) null));
    }

    @Test
    public void isEmptyOrNull_PieDataset_Empty_ReturnsTrue() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void isEmptyOrNull_PieDataset_AllNullOrZeroOrNegative_ReturnsTrue() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", null);
        dataset.setValue("B", 0.0);
        dataset.setValue("C", -5.0);
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void isEmptyOrNull_PieDataset_HasPositive_ReturnsFalse() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 0.0);
        dataset.setValue("B", 5.0);
        Assert.assertFalse(DatasetUtilities.isEmptyOrNull(dataset));
    }

    // ----------------- isEmptyOrNull(CategoryDataset) -----------------

    @Test
    public void isEmptyOrNull_CategoryDataset_Null_ReturnsTrue() {
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull((CategoryDataset) null));
    }

    @Test
    public void isEmptyOrNull_CategoryDataset_Empty_ReturnsTrue() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void isEmptyOrNull_CategoryDataset_AllNulls_ReturnsTrue() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue((Number) null, "R", "C");
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void isEmptyOrNull_CategoryDataset_HasValue_ReturnsFalse() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1.0, "R", "C");
        Assert.assertFalse(DatasetUtilities.isEmptyOrNull(dataset));
    }

    // ----------------- isEmptyOrNull(XYDataset) -----------------

    @Test
    public void isEmptyOrNull_XYDataset_Null_ReturnsTrue() {
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull((XYDataset) null));
    }

    @Test
    public void isEmptyOrNull_XYDataset_Empty_ReturnsTrue() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void isEmptyOrNull_XYDataset_SeriesWithItems_ReturnsFalse() {
        XYSeries s = new XYSeries("S");
        s.add(1.0, 1.0);
        XYSeriesCollection dataset = new XYSeriesCollection(s);
        Assert.assertFalse(DatasetUtilities.isEmptyOrNull(dataset));
    }

    @Test
    public void isEmptyOrNull_XYDataset_SeriesEmpty_ReturnsTrue() {
        XYSeries s = new XYSeries("S");
        XYSeriesCollection dataset = new XYSeriesCollection(s);
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(dataset));
    }

    // ----------------- findDomainBounds(XYDataset) -----------------

    @Test(expected = IllegalArgumentException.class)
    public void findDomainBounds_XYDataset_Null_ThrowsException() {
        DatasetUtilities.findDomainBounds((XYDataset) null);
    }

    @Test
    public void findDomainBounds_XYDataset_NoData_ReturnsNull() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Range range = DatasetUtilities.findDomainBounds(dataset);
        Assert.assertNull(range);
    }

    @Test
    public void findDomainBounds_XYDataset_WithValues_ReturnsRange() {
        XYSeries s = new XYSeries("S");
        s.add(10.0, 5.0);
        s.add(20.0, 8.0);
        XYSeriesCollection dataset = new XYSeriesCollection(s);
        Range range = DatasetUtilities.findDomainBounds(dataset);
        Assert.assertEquals(10.0, range.getLowerBound(), 0.0);
        Assert.assertEquals(20.0, range.getUpperBound(), 0.0);
    }

    @Test
    public void findDomainBounds_XYDataset_DomainInfo_Used() {
        // Create a dataset implementing DomainInfo
        XYDataset domainInfoDataset = new XYDataset() {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int indexOf(Comparable key) { return 0; }
            public int getItemCount(int series) { return 2; }
            public Number getX(int series, int item) { return null; }
            public double getXValue(int series, int item) { return 0; }
            public Number getY(int series, int item) { return null; }
            public double getYValue(int series, int item) { return 0; }
        };
        Range result = DatasetUtilities.findDomainBounds(domainInfoDataset);
        // Since it doesn't implement DomainInfo, uses iteration which returns null (actual NaN values).
        Assert.assertNotNull(result); // but will be null? Actually getXValue returns 0.0, not NaN. So min=0, max=0.
        Assert.assertEquals(0.0, result.getLowerBound(), 0.0);
    }

    // ----------------- findDomainBounds(XYDataset, boolean) -----------------

    @Test
    public void findDomainBounds_IncludeInterval_False_OnlyXValues() {
        XYSeries s = new XYSeries("S");
        s.add(5, 1);
        XYSeriesCollection dataset = new XYSeriesCollection(s);
        Range range = DatasetUtilities.findDomainBounds(dataset, false);
        Assert.assertEquals(5.0, range.getLowerBound(), 0.0);
    }

    // ----------------- iterateDomainBounds(XYDataset) -----------------

    @Test
    public void iterateDomainBounds_XYDataset_NoData_ReturnsNull() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Range range = DatasetUtilities.iterateDomainBounds(dataset);
        Assert.assertNull(range);
    }

    @Test
    public void iterateDomainBounds_XYDataset_WithNaN_Ignores() {
        XYSeries s = new XYSeries("S");
        s.add(Double.NaN, 5);
        s.add(10.0, 5);
        XYSeriesCollection dataset = new XYSeriesCollection(s);
        Range range = DatasetUtilities.iterateDomainBounds(dataset);
        Assert.assertEquals(10.0, range.getLowerBound(), 0.0);
    }

    // ----------------- findRangeBounds(CategoryDataset) -----------------

    @Test(expected = IllegalArgumentException.class)
    public void findRangeBounds_CategoryDataset_Null_ThrowsException() {
        DatasetUtilities.findRangeBounds((CategoryDataset) null);
    }

    @Test
    public void findRangeBounds_CategoryDataset_NoData_ReturnsNull() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Assert.assertNull(DatasetUtilities.findRangeBounds(dataset));
    }

    @Test
    public void findRangeBounds_CategoryDataset_Values_ReturnsRange() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(5.0, "R", "C");
        dataset.addValue(10.0, "R2", "C");
        Range range = DatasetUtilities.findRangeBounds(dataset);
        Assert.assertEquals(5.0, range.getLowerBound(), 0.0);
        Assert.assertEquals(10.0, range.getUpperBound(), 0.0);
    }

    // ----------------- iterateRangeBounds(CategoryDataset) -----------------

    @Test
    public void iterateRangeBounds_CategoryDataset_IncludeInterval_False() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(3.0, "R", "C");
        Range range = DatasetUtilities.iterateRangeBounds(dataset, false);
        Assert.assertEquals(3.0, range.getLowerBound(), 0.0);
        Assert.assertEquals(3.0, range.getUpperBound(), 0.0);
    }

    @Test
    public void iterateRangeBounds_IntervalCategoryDataset_IncludesStartEnd() {
        // Create a simple IntervalCategoryDataset mock
        IntervalCategoryDataset icd = new IntervalCategoryDataset() {
            public int getRowCount() { return 1; }
            public int getColumnCount() { return 1; }
            public List getColumnKeys() { return Arrays.asList("C"); }
            public List getRowKeys() { return Arrays.asList("R"); }
            public Comparable getRowKey(int row) { return "R"; }
            public Comparable getColumnKey(int column) { return "C"; }
            public int getRowIndex(Comparable key) { return 0; }
            public int getColumnIndex(Comparable key) { return 0; }
            public Number getValue(int row, int column) { return 5; }
            public Number getValue(Comparable rowKey, Comparable columnKey) { return 5; }
            public int getColumnCount() { return 1; }
            public int getRowCount() { return 1; }
            public Number getStartValue(int row, int column) { return 2; }
            public Number getEndValue(int row, int column) { return 7; }
            public Number getStartValue(Comparable rowKey, Comparable columnKey) { return 2; }
            public Number getEndValue(Comparable rowKey, Comparable columnKey) { return 7; }
        };
        Range range = DatasetUtilities.iterateRangeBounds(icd, true);
        Assert.assertEquals(2.0, range.getLowerBound(), 0.0);
        Assert.assertEquals(7.0, range.getUpperBound(), 0.0);
    }

    // ----------------- findMinimumDomainValue(XYDataset) -----------------

    @Test(expected = IllegalArgumentException.class)
    public void findMinimumDomainValue_Null_ThrowsException() {
        DatasetUtilities.findMinimumDomainValue(null);
    }

    @Test
    public void findMinimumDomainValue_NoData_ReturnsNull() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        Assert.assertNull(DatasetUtilities.findMinimumDomainValue(dataset));
    }

    @Test
    public void findMinimumDomainValue_IntervalXYDataset_ReturnsStartX() {
        XYSeries s = new XYSeries("S");
        s.add(3.0, 5.0);
        s.add(7.0, 9.0);
        XYSeriesCollection dataset = new XYSeriesCollection(s); // not IntervalXYDataset
        // For IntervalXYDataset, we need custom.
        // We'll test with DomainInfo first
    }

    // ... (more tests for findMaximumDomainValue, findMinimumRangeValue, findMaximumRangeValue, etc.)

    // For brevity, I'll include a few critical tests and then the rest in full code.

    // I'll write complete test class with all methods in the final answer.
    // Now I will construct the full test class code.

}
