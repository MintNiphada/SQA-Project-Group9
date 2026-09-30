package org.jfree.data.general;

import org.jfree.data.DomainInfo;
import org.jfree.data.DomainOrder;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.CategoryRangeInfo;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.DefaultIntervalCategoryDataset;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.pie.DefaultPieDataset;
import org.jfree.data.pie.PieDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.MultiValueCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYDomainInfo;
import org.jfree.data.xy.XYRangeInfo;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatasetUtilitiesTest {

    private static final double EPSILON = 0.000000001;

    @Test
    public void testCalculatePieDatasetTotal() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.setValue("A", 10.0);
        d.setValue("B", 25.5);
        d.setValue("C", -5.0);
        d.setValue("D", 0.0);
        d.setValue("E", null);
        Assert.assertEquals(35.5, DatasetUtilities.calculatePieDatasetTotal(d), EPSILON);

        DefaultPieDataset empty = new DefaultPieDataset();
        Assert.assertEquals(0.0, DatasetUtilities.calculatePieDatasetTotal(empty), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculatePieDatasetTotalNull() {
        DatasetUtilities.calculatePieDatasetTotal(null);
    }

    @Test
    public void testCreatePieDatasetForRow() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.addValue(3.0, "R2", "C1");
        d.addValue(4.0, "R2", "C2");

        PieDataset p1 = DatasetUtilities.createPieDatasetForRow(d, "R1");
        Assert.assertEquals(2, p1.getItemCount());
        Assert.assertEquals(1.0, p1.getValue("C1").doubleValue(), EPSILON);
        Assert.assertEquals(2.0, p1.getValue("C2").doubleValue(), EPSILON);

        PieDataset p2 = DatasetUtilities.createPieDatasetForRow(d, 1);
        Assert.assertEquals(3.0, p2.getValue(0).doubleValue(), EPSILON);
        Assert.assertEquals(4.0, p2.getValue(1).doubleValue(), EPSILON);
    }

    @Test
    public void testCreatePieDatasetForColumn() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(1.0, "R1", "C1");
        d.addValue(2.0, "R1", "C2");
        d.addValue(3.0, "R2", "C1");
        d.addValue(4.0, "R2", "C2");

        PieDataset p1 = DatasetUtilities.createPieDatasetForColumn(d, "C2");
        Assert.assertEquals(2, p1.getItemCount());
        Assert.assertEquals(2.0, p1.getValue("R1").doubleValue(), EPSILON);
        Assert.assertEquals(4.0, p1.getValue("R2").doubleValue(), EPSILON);

        PieDataset p2 = DatasetUtilities.createPieDatasetForColumn(d, 0);
        Assert.assertEquals(1.0, p2.getValue(0).doubleValue(), EPSILON);
        Assert.assertEquals(3.0, p2.getValue(1).doubleValue(), EPSILON);
    }

    @Test
    public void testCreateConsolidatedPieDataset() {
        DefaultPieDataset d = new DefaultPieDataset();
        d.setValue("A", 1.0);
        d.setValue("B", 1.0);
        d.setValue("C", 10.0);
        d.setValue("D", 88.0);
        // total = 100.0. A is 1%, B is 1%, C is 10%, D is 88%

        PieDataset p = DatasetUtilities.createConsolidatedPieDataset(d, "Other", 0.05);
        Assert.assertEquals(3, p.getItemCount());
        Assert.assertEquals(2.0, p.getValue("Other").doubleValue(), EPSILON);
        Assert.assertEquals(10.0, p.getValue("C").doubleValue(), EPSILON);
        Assert.assertEquals(88.0, p.getValue("D").doubleValue(), EPSILON);

        // When minItems threshold is not reached for aggregation
        PieDataset p2 = DatasetUtilities.createConsolidatedPieDataset(d, "Other", 0.05, 5);
        Assert.assertEquals(4, p2.getItemCount());
        Assert.assertEquals(1.0, p2.getValue("A").doubleValue(), EPSILON);
    }

    @Test
    public void testCreateCategoryDatasetFromDoubleArray() {
        double[][] data = new double[][] {{1.0, 2.0}, {3.0, 4.0}};
        CategoryDataset ds = DatasetUtilities.createCategoryDataset("R", "C", data);
        Assert.assertEquals(2, ds.getRowCount());
        Assert.assertEquals(2, ds.getColumnCount());
        Assert.assertEquals(1.0, ds.getValue("R1", "C1").doubleValue(), EPSILON);
        Assert.assertEquals(4.0, ds.getValue("R2", "C2").doubleValue(), EPSILON);
    }

    @Test
    public void testCreateCategoryDatasetFromNumberArray() {
        Number[][] data = new Number[][] {{1.0, null}, {3.0, 4.0}};
        CategoryDataset ds = DatasetUtilities.createCategoryDataset("R", "C", data);
        Assert.assertEquals(2, ds.getRowCount());
        Assert.assertEquals(2, ds.getColumnCount());
        Assert.assertEquals(1.0, ds.getValue("R1", "C1").doubleValue(), EPSILON);
        Assert.assertNull(ds.getValue("R1", "C2"));
        Assert.assertEquals(4.0, ds.getValue("R2", "C2").doubleValue(), EPSILON);
    }

    @Test
    public void testCreateCategoryDatasetFromCustomKeys() {
        Comparable[] rows = new Comparable[] {"RowA", "RowB"};
        Comparable[] cols = new Comparable[] {"Col1", "Col2", "Col3"};
        double[][] data = new double[][] {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};

        CategoryDataset ds = DatasetUtilities.createCategoryDataset(rows, cols, data);
        Assert.assertEquals(2, ds.getRowCount());
        Assert.assertEquals(3, ds.getColumnCount());
        Assert.assertEquals(6.0, ds.getValue("RowB", "Col3").doubleValue(), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetNullRowKeys() {
        DatasetUtilities.createCategoryDataset(null, new Comparable[] {"C1"}, new double[][] {{1.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetNullColKeys() {
        DatasetUtilities.createCategoryDataset(new Comparable[] {"R1"}, null, new double[][] {{1.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetDuplicateRowKeys() {
        DatasetUtilities.createCategoryDataset(new Comparable[] {"R1", "R1"}, new Comparable[] {"C1"}, new double[][] {{1.0}, {2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetDuplicateColKeys() {
        DatasetUtilities.createCategoryDataset(new Comparable[] {"R1"}, new Comparable[] {"C1", "C1"}, new double[][] {{1.0, 2.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetMismatchedRows() {
        DatasetUtilities.createCategoryDataset(new Comparable[] {"R1", "R2"}, new Comparable[] {"C1"}, new double[][] {{1.0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetMismatchedCols() {
        DatasetUtilities.createCategoryDataset(new Comparable[] {"R1"}, new Comparable[] {"C1", "C2"}, new double[][] {{1.0}});
    }

    @Test
    public void testCreateCategoryDatasetFromKeyedValues() {
        DefaultPieDataset kv = new DefaultPieDataset();
        kv.setValue("K1", 10.0);
        kv.setValue("K2", 20.0);
        CategoryDataset ds = DatasetUtilities.createCategoryDataset("R1", kv);
        Assert.assertEquals(1, ds.getRowCount());
        Assert.assertEquals(2, ds.getColumnCount());
        Assert.assertEquals(10.0, ds.getValue("R1", "K1").doubleValue(), EPSILON);
        Assert.assertEquals(20.0, ds.getValue("R1", "K2").doubleValue(), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetFromKeyedValuesNullKey() {
        DatasetUtilities.createCategoryDataset(null, new DefaultPieDataset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCategoryDatasetFromKeyedValuesNullData() {
        DatasetUtilities.createCategoryDataset("R1", (DefaultPieDataset) null);
    }

    @Test
    public void testSampleFunction2D() {
        Function2D f = new LineFunction2D(2.0, 3.0); // y = 2 + 3x
        XYDataset ds = DatasetUtilities.sampleFunction2D(f, 0.0, 10.0, 11, "Series1");
        Assert.assertEquals(1, ds.getSeriesCount());
        Assert.assertEquals(11, ds.getItemCount(0));
        Assert.assertEquals(0.0, ds.getXValue(0, 0), EPSILON);
        Assert.assertEquals(2.0, ds.getYValue(0, 0), EPSILON);
        Assert.assertEquals(10.0, ds.getXValue(0, 10), EPSILON);
        Assert.assertEquals(32.0, ds.getYValue(0, 10), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DNullFunction() {
        DatasetUtilities.sampleFunction2D(null, 0.0, 10.0, 5, "S");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DNullKey() {
        DatasetUtilities.sampleFunction2D(new LineFunction2D(1, 1), 0.0, 10.0, 5, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DInvalidRange() {
        DatasetUtilities.sampleFunction2D(new LineFunction2D(1, 1), 10.0, 0.0, 5, "S");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleFunction2DInvalidSamples() {
        DatasetUtilities.sampleFunction2D(new LineFunction2D(1, 1), 0.0, 10.0, 1, "S");
    }

    @Test
    public void testIsEmptyOrNullPieDataset() {
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull((PieDataset) null));
        DefaultPieDataset p = new DefaultPieDataset();
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(p));
        p.setValue("A", null);
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(p));
        p.setValue("B", 0.0);
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(p));
        p.setValue("C", -10.0);
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(p));
        p.setValue("D", 5.0);
        Assert.assertFalse(DatasetUtilities.isEmptyOrNull(p));
    }

    @Test
    public void testIsEmptyOrNullCategoryDataset() {
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull((CategoryDataset) null));
        DefaultCategoryDataset c = new DefaultCategoryDataset();
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(c));
        c.addValue(null, "R1", "C1");
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(c));
        c.addValue(1.0, "R1", "C1");
        Assert.assertFalse(DatasetUtilities.isEmptyOrNull(c));
    }

    @Test
    public void testIsEmptyOrNullXYDataset() {
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull((XYDataset) null));
        XYSeriesCollection coll = new XYSeriesCollection();
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(coll));
        XYSeries s = new XYSeries("S");
        coll.addSeries(s);
        Assert.assertTrue(DatasetUtilities.isEmptyOrNull(coll));
        s.add(1.0, 2.0);
        Assert.assertFalse(DatasetUtilities.isEmptyOrNull(coll));
    }

    @Test
    public void testFindDomainBoundsXYDataset() {
        XYSeriesCollection coll = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 10.0);
        s1.add(5.0, 20.0);
        coll.addSeries(s1);

        Range r = DatasetUtilities.findDomainBounds(coll);
        Assert.assertEquals(new Range(1.0, 5.0), r);

        // DomainInfo check
        class DomainInfoXYDataset extends AbstractXYDataset implements DomainInfo {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int getItemCount(int series) { return 1; }
            public Number getX(int series, int item) { return 0.0; }
            public Number getY(int series, int item) { return 0.0; }
            public double getDomainLowerBound(boolean includeInterval) { return 2.0; }
            public double getDomainUpperBound(boolean includeInterval) { return 8.0; }
            public Range getDomainBounds(boolean includeInterval) { return new Range(2.0, 8.0); }
        }
        DomainInfoXYDataset di = new DomainInfoXYDataset();
        Assert.assertEquals(new Range(2.0, 8.0), DatasetUtilities.findDomainBounds(di));
        Assert.assertEquals(2.0, DatasetUtilities.findMinimumDomainValue(di).doubleValue(), EPSILON);
        Assert.assertEquals(8.0, DatasetUtilities.findMaximumDomainValue(di).doubleValue(), EPSILON);
    }

    @Test
    public void testFindDomainBoundsWithVisibleSeriesKeys() {
        XYSeriesCollection coll = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 10.0);
        s1.add(5.0, 20.0);
        XYSeries s2 = new XYSeries("S2");
        s2.add(10.0, 30.0);
        s2.add(15.0, 40.0);
        coll.addSeries(s1);
        coll.addSeries(s2);

        Range r = DatasetUtilities.findDomainBounds(coll, Collections.singletonList("S1"), true);
        Assert.assertEquals(new Range(1.0, 5.0), r);

        class XYDomainInfoImpl extends AbstractXYDataset implements XYDomainInfo {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int getItemCount(int series) { return 1; }
            public Number getX(int series, int item) { return 0.0; }
            public Number getY(int series, int item) { return 0.0; }
            public Range getDomainBounds(List visibleSeriesKeys, boolean includeInterval) {
                return new Range(100.0, 200.0);
            }
        }
        Assert.assertEquals(new Range(100.0, 200.0),
                DatasetUtilities.findDomainBounds(new XYDomainInfoImpl(), Collections.singletonList("S"), true));
    }

    @Test
    public void testIterateDomainBoundsIntervalXYDataset() {
        class CustomIntervalXYDataset extends AbstractIntervalXYDataset {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int getItemCount(int series) { return 2; }
            public Number getX(int series, int item) { return item == 0 ? 2.0 : 4.0; }
            public Number getY(int series, int item) { return 1.0; }
            public Number getStartX(int series, int item) { return item == 0 ? 1.0 : 3.0; }
            public Number getEndX(int series, int item) { return item == 0 ? 3.0 : 5.0; }
            public Number getStartY(int series, int item) { return 1.0; }
            public Number getEndY(int series, int item) { return 1.0; }
        }
        CustomIntervalXYDataset ds = new CustomIntervalXYDataset();
        Range rWithInterval = DatasetUtilities.iterateDomainBounds(ds, true);
        Assert.assertEquals(new Range(1.0, 5.0), rWithInterval);

        Range rWithoutInterval = DatasetUtilities.iterateDomainBounds(ds, false);
        Assert.assertEquals(new Range(2.0, 4.0), rWithoutInterval);

        Range rVisible = DatasetUtilities.iterateToFindDomainBounds(ds, Collections.singletonList("S"), true);
        Assert.assertEquals(new Range(1.0, 5.0), rVisible);
    }

    @Test
    public void testFindRangeBoundsCategoryDataset() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(15.0, "R1", "C2");
        d.addValue(-2.0, "R2", "C1");

        Range r = DatasetUtilities.findRangeBounds(d);
        Assert.assertEquals(new Range(-2.0, 15.0), r);
        Assert.assertEquals(-2.0, DatasetUtilities.findMinimumRangeValue(d).doubleValue(), EPSILON);
        Assert.assertEquals(15.0, DatasetUtilities.findMaximumRangeValue(d).doubleValue(), EPSILON);

        class RangeInfoCategoryDataset extends DefaultCategoryDataset implements RangeInfo {
            public double getRangeLowerBound(boolean includeInterval) { return 50.0; }
            public double getRangeUpperBound(boolean includeInterval) { return 60.0; }
            public Range getRangeBounds(boolean includeInterval) { return new Range(50.0, 60.0); }
        }
        RangeInfoCategoryDataset ricd = new RangeInfoCategoryDataset();
        Assert.assertEquals(new Range(50.0, 60.0), DatasetUtilities.findRangeBounds(ricd));
        Assert.assertEquals(50.0, DatasetUtilities.findMinimumRangeValue(ricd).doubleValue(), EPSILON);
        Assert.assertEquals(60.0, DatasetUtilities.findMaximumRangeValue(ricd).doubleValue(), EPSILON);
    }

    @Test
    public void testFindRangeBoundsCategoryDatasetVisibleKeys() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(15.0, "R1", "C2");
        d.addValue(-2.0, "R2", "C1");

        Range r = DatasetUtilities.findRangeBounds(d, Collections.singletonList("R1"), true);
        Assert.assertEquals(new Range(5.0, 15.0), r);

        class CategoryRangeInfoDataset extends DefaultCategoryDataset implements CategoryRangeInfo {
            public Range getRangeBounds(List visibleSeriesKeys, boolean includeInterval) {
                return new Range(1.0, 99.0);
            }
        }
        CategoryRangeInfoDataset cri = new CategoryRangeInfoDataset();
        Assert.assertEquals(new Range(1.0, 99.0),
                DatasetUtilities.findRangeBounds(cri, Collections.singletonList("R1"), true));
    }

    @Test
    public void testIterateRangeBoundsIntervalCategoryDataset() {
        double[][] starts = new double[][] {{1.0, 2.0}};
        double[][] ends = new double[][] {{3.0, 5.0}};
        DefaultIntervalCategoryDataset ds = new DefaultIntervalCategoryDataset(
                new Comparable[] {"S1"}, new Comparable[] {"C1", "C2"}, starts, ends);

        Range r = DatasetUtilities.iterateRangeBounds(ds, true);
        Assert.assertEquals(new Range(1.0, 5.0), r);

        Range rVisible = DatasetUtilities.iterateToFindRangeBounds(ds, Collections.singletonList("S1"), true);
        Assert.assertEquals(new Range(1.0, 5.0), rVisible);

        Assert.assertEquals(1.0, DatasetUtilities.findMinimumRangeValue(ds).doubleValue(), EPSILON);
        Assert.assertEquals(5.0, DatasetUtilities.findMaximumRangeValue(ds).doubleValue(), EPSILON);
    }

    @Test
    public void testIterateToFindRangeBoundsStatisticalCategoryDataset() {
        DefaultStatisticalCategoryDataset ds = new DefaultStatisticalCategoryDataset();
        ds.add(10.0, 2.0, "R1", "C1"); // 8 to 12
        ds.add(20.0, 3.0, "R1", "C2"); // 17 to 23

        Range r = DatasetUtilities.iterateToFindRangeBounds(ds, Collections.singletonList("R1"), true);
        Assert.assertEquals(new Range(8.0, 23.0), r);
    }

    @Test
    public void testIterateToFindRangeBoundsMultiValueCategoryDataset() {
        DefaultMultiValueCategoryDataset ds = new DefaultMultiValueCategoryDataset();
        List<Double> v1 = Arrays.asList(2.0, 5.0, 8.0);
        List<Double> v2 = Arrays.asList(-1.0, 10.0);
        ds.add(v1, "R1", "C1");
        ds.add(v2, "R1", "C2");

        Range r = DatasetUtilities.iterateToFindRangeBounds(ds, Collections.singletonList("R1"), true);
        Assert.assertEquals(new Range(-1.0, 10.0), r);
    }

    @Test
    public void testIterateToFindRangeBoundsBoxAndWhiskerCategoryDataset() {
        DefaultBoxAndWhiskerCategoryDataset ds = new DefaultBoxAndWhiskerCategoryDataset();
        BoxAndWhiskerItem item1 = new BoxAndWhiskerItem(10.0, 10.0, 8.0, 12.0, 5.0, 15.0, 2.0, 18.0, Collections.emptyList());
        BoxAndWhiskerItem item2 = new BoxAndWhiskerItem(20.0, 20.0, 18.0, 22.0, 16.0, 25.0, 14.0, 28.0, Collections.emptyList());
        ds.add(item1, "R1", "C1");
        ds.add(item2, "R1", "C2");

        Range r = DatasetUtilities.iterateToFindRangeBounds(ds, Collections.singletonList("R1"), true);
        Assert.assertEquals(new Range(5.0, 25.0), r);
    }

    @Test
    public void testFindRangeBoundsXYDataset() {
        XYSeriesCollection coll = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 10.0);
        s1.add(2.0, -5.0);
        coll.addSeries(s1);

        Range r = DatasetUtilities.findRangeBounds(coll);
        Assert.assertEquals(new Range(-5.0, 10.0), r);
        Assert.assertEquals(-5.0, DatasetUtilities.findMinimumRangeValue(coll).doubleValue(), EPSILON);
        Assert.assertEquals(10.0, DatasetUtilities.findMaximumRangeValue(coll).doubleValue(), EPSILON);

        class RangeInfoXYDataset extends AbstractXYDataset implements RangeInfo {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int getItemCount(int series) { return 1; }
            public Number getX(int series, int item) { return 0.0; }
            public Number getY(int series, int item) { return 0.0; }
            public double getRangeLowerBound(boolean includeInterval) { return 100.0; }
            public double getRangeUpperBound(boolean includeInterval) { return 200.0; }
            public Range getRangeBounds(boolean includeInterval) { return new Range(100.0, 200.0); }
        }
        RangeInfoXYDataset rds = new RangeInfoXYDataset();
        Assert.assertEquals(new Range(100.0, 200.0), DatasetUtilities.findRangeBounds(rds));
        Assert.assertEquals(100.0, DatasetUtilities.findMinimumRangeValue(rds).doubleValue(), EPSILON);
        Assert.assertEquals(200.0, DatasetUtilities.findMaximumRangeValue(rds).doubleValue(), EPSILON);
    }

    @Test
    public void testFindRangeBoundsXYDatasetWithXRange() {
        XYSeriesCollection coll = new XYSeriesCollection();
        XYSeries s1 = new XYSeries("S1");
        s1.add(1.0, 10.0);
        s1.add(5.0, 50.0);
        s1.add(10.0, 100.0);
        coll.addSeries(s1);

        Range r = DatasetUtilities.findRangeBounds(coll, Collections.singletonList("S1"), new Range(0.0, 6.0), true);
        Assert.assertEquals(new Range(10.0, 50.0), r);

        class XYRangeInfoImpl extends AbstractXYDataset implements XYRangeInfo {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int getItemCount(int series) { return 1; }
            public Number getX(int series, int item) { return 0.0; }
            public Number getY(int series, int item) { return 0.0; }
            public Range getRangeBounds(List visibleSeriesKeys, Range xRange, boolean includeInterval) {
                return new Range(7.0, 14.0);
            }
        }
        Assert.assertEquals(new Range(7.0, 14.0),
                DatasetUtilities.findRangeBounds(new XYRangeInfoImpl(), Collections.singletonList("S"), new Range(0.0, 1.0), true));
    }

    @Test
    public void testIterateRangeBoundsOHLCDataset() {
        class CustomOHLCDataset extends AbstractXYDataset implements OHLCDataset {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int getItemCount(int series) { return 2; }
            public Number getX(int series, int item) { return item == 0 ? 1.0 : 2.0; }
            public Number getY(int series, int item) { return 10.0; }
            public Number getHigh(int series, int item) { return item == 0 ? 15.0 : 25.0; }
            public double getHighValue(int series, int item) { return getHigh(series, item).doubleValue(); }
            public Number getLow(int series, int item) { return item == 0 ? 5.0 : 8.0; }
            public double getLowValue(int series, int item) { return getLow(series, item).doubleValue(); }
            public Number getOpen(int series, int item) { return 10.0; }
            public double getOpenValue(int series, int item) { return 10.0; }
            public Number getClose(int series, int item) { return 12.0; }
            public double getCloseValue(int series, int item) { return 12.0; }
            public Number getVolume(int series, int item) { return 100.0; }
            public double getVolumeValue(int series, int item) { return 100.0; }
        }
        CustomOHLCDataset ds = new CustomOHLCDataset();
        Range r = DatasetUtilities.iterateRangeBounds(ds, true);
        Assert.assertEquals(new Range(5.0, 25.0), r);

        Range rFiltered = DatasetUtilities.iterateToFindRangeBounds(ds, Collections.singletonList("S"), new Range(0.5, 1.5), true);
        Assert.assertEquals(new Range(5.0, 15.0), rFiltered);

        Assert.assertEquals(5.0, DatasetUtilities.findMinimumRangeValue(ds).doubleValue(), EPSILON);
        Assert.assertEquals(25.0, DatasetUtilities.findMaximumRangeValue(ds).doubleValue(), EPSILON);
    }

    @Test
    public void testIterateRangeBoundsBoxAndWhiskerXYDataset() {
        class CustomBoxAndWhiskerXYDataset extends AbstractXYDataset implements BoxAndWhiskerXYDataset {
            public int getSeriesCount() { return 1; }
            public Comparable getSeriesKey(int series) { return "S"; }
            public int getItemCount(int series) { return 2; }
            public Number getX(int series, int item) { return item == 0 ? 1.0 : 2.0; }
            public Number getY(int series, int item) { return 10.0; }
            public Number getMeanValue(int series, int item) { return 10.0; }
            public Number getMedianValue(int series, int item) { return 10.0; }
            public Number getQ1Value(int series, int item) { return 8.0; }
            public Number getQ3Value(int series, int item) { return 12.0; }
            public Number getMinRegularValue(int series, int item) { return item == 0 ? 4.0 : 6.0; }
            public Number getMaxRegularValue(int series, int item) { return item == 0 ? 14.0 : 24.0; }
            public Number getMinOutlier(int series, int item) { return 1.0; }
            public Number getMaxOutlier(int series, int item) { return 30.0; }
            public List getOutliers(int series, int item) { return Collections.emptyList(); }
            public double getOutlierCoefficient() { return 1.5; }
            public double getFaroutCoefficient() { return 2.0; }
        }
        CustomBoxAndWhiskerXYDataset ds = new CustomBoxAndWhiskerXYDataset();
        Range r = DatasetUtilities.iterateToFindRangeBounds(ds, Collections.singletonList("S"), new Range(0.0, 3.0), true);
        Assert.assertEquals(new Range(4.0, 24.0), r);
    }

    @Test
    public void testFindStackedRangeBoundsCategoryDataset() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(10.0, "R2", "C1");
        d.addValue(-3.0, "R1", "C2");
        d.addValue(-4.0, "R2", "C2");

        Range r = DatasetUtilities.findStackedRangeBounds(d);
        Assert.assertEquals(new Range(-7.0, 15.0), r);

        Range rWithBase = DatasetUtilities.findStackedRangeBounds(d, 5.0);
        Assert.assertEquals(new Range(-2.0, 20.0), rWithBase);

        Assert.assertEquals(-7.0, DatasetUtilities.findMinimumStackedRangeValue(d).doubleValue(), EPSILON);
        Assert.assertEquals(15.0, DatasetUtilities.findMaximumStackedRangeValue(d).doubleValue(), EPSILON);
    }

    @Test
    public void testFindStackedRangeBoundsCategoryDatasetWithKeyToGroupMap() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(5.0, "R1", "C1");
        d.addValue(10.0, "R2", "C1");
        d.addValue(20.0, "R3", "C1");

        KeyToGroupMap map = new KeyToGroupMap("G1");
        map.mapKeyToGroup("R1", "G1");
        map.mapKeyToGroup("R2", "G1");
        map.mapKeyToGroup("R3", "G2");

        Range r = DatasetUtilities.findStackedRangeBounds(d, map);
        Assert.assertEquals(new Range(0.0, 20.0), r);
    }

    @Test
    public void testFindStackedRangeBoundsTableXYDataset() {
        DefaultTableXYDataset d = new DefaultTableXYDataset();
        XYSeries s1 = new XYSeries("S1", true, false);
        s1.add(1.0, 5.0);
        s1.add(2.0, -3.0);
        XYSeries s2 = new XYSeries("S2", true, false);
        s2.add(1.0, 10.0);
        s2.add(2.0, -4.0);

        d.addSeries(s1);
        d.addSeries(s2);

        Range r = DatasetUtilities.findStackedRangeBounds(d);
        Assert.assertEquals(new Range(-7.0, 15.0), r);

        Range rBase = DatasetUtilities.findStackedRangeBounds(d, 2.0);
        Assert.assertEquals(new Range(-5.0, 17.0), rBase);

        Assert.assertEquals(15.0, DatasetUtilities.calculateStackTotal(d, 0), EPSILON);
        Assert.assertEquals(-7.0, DatasetUtilities.calculateStackTotal(d, 1), EPSILON);
    }

    @Test
    public void testFindCumulativeRangeBounds() {
        DefaultCategoryDataset d = new DefaultCategoryDataset();
        d.addValue(10.0, "R1", "C1");
        d.addValue(15.0, "R1", "C2");
        d.addValue(-5.0, "R1", "C3");
        // Row 1 running totals: C1=10, C2=25, C3=20

        d.addValue(2.0, "R2", "C1");
        d.addValue(-8.0, "R2", "C2");
        // Row 2 running totals: C1=2, C2=-6, C3=-6

        Range r = DatasetUtilities.findCumulativeRangeBounds(d);
        Assert.assertEquals(new Range(-6.0, 25.0), r);

        DefaultCategoryDataset empty = new DefaultCategoryDataset();
        Assert.assertNull(DatasetUtilities.findCumulativeRangeBounds(empty));

        DefaultCategoryDataset allNull = new DefaultCategoryDataset();
        allNull.addValue(null, "R1", "C1");
        Assert.assertNull(DatasetUtilities.findCumulativeRangeBounds(allNull));
    }

    @Test
    public void testDeprecatedAndHelperMethods() {
        DefaultCategoryDataset cd = new DefaultCategoryDataset();
        cd.addValue(1.0, "R1", "C1");
        cd.addValue(2.0, "R1", "C2");

        Assert.assertEquals(new Range(1.0, 2.0), DatasetUtilities.iterateCategoryRangeBounds(cd, false));
        Assert.assertEquals(new Range(1.0, 2.0), DatasetUtilities.iterateRangeBounds(cd));

        XYSeriesCollection xyd = new XYSeriesCollection();
        XYSeries s = new XYSeries("S");
        s.add(1.0, 10.0);
        s.add(2.0, 20.0);
        xyd.addSeries(s);

        Assert.assertEquals(new Range(10.0, 20.0), DatasetUtilities.iterateXYRangeBounds(xyd));
        Assert.assertEquals(new Range(10.0, 20.0), DatasetUtilities.iterateRangeBounds((XYDataset) xyd));
    }

    @Test
    public void testIterateEmptyDatasetsReturnsNull() {
        XYSeriesCollection xyd = new XYSeriesCollection();
        Assert.assertNull(DatasetUtilities.iterateDomainBounds(xyd));
        Assert.assertNull(DatasetUtilities.iterateRangeBounds((XYDataset) xyd));
        Assert.assertNull(DatasetUtilities.findMinimumDomainValue(xyd));
        Assert.assertNull(DatasetUtilities.findMaximumDomainValue(xyd));
        Assert.assertNull(DatasetUtilities.findMinimumRangeValue((XYDataset) xyd));
        Assert.assertNull(DatasetUtilities.findMaximumRangeValue((XYDataset) xyd));

        DefaultCategoryDataset cd = new DefaultCategoryDataset();
        Assert.assertNull(DatasetUtilities.iterateRangeBounds(cd));
        Assert.assertNull(DatasetUtilities.findMinimumRangeValue((CategoryDataset) cd));
        Assert.assertNull(DatasetUtilities.findMaximumRangeValue((CategoryDataset) cd));
        Assert.assertNull(DatasetUtilities.findStackedRangeBounds(cd));
        Assert.assertNull(DatasetUtilities.findMinimumStackedRangeValue(cd));
        Assert.assertNull(DatasetUtilities.findMaximumStackedRangeValue(cd));
    }
}