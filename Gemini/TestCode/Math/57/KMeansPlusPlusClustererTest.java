package org.apache.commons.math.stat.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.math.exception.ConvergenceException;
import org.junit.Assert;
import org.junit.Test;

public class KMeansPlusPlusClustererTest {

    @Test
    public void testPerformClusterAnalysisDegenerate() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        EuclideanIntegerPoint[] points = new EuclideanIntegerPoint[] {
            new EuclideanIntegerPoint(new int[] { 1959, 325100 }),
            new EuclideanIntegerPoint(new int[] { 1960, 373200 }),
        };
        List<Cluster<EuclideanIntegerPoint>> clusters =
                transformer.cluster(Arrays.asList(points), 1, 1);
        Assert.assertEquals(1, clusters.size());
        Assert.assertEquals(2, clusters.get(0).getPoints().size());
        EuclideanIntegerPoint pt1 = new EuclideanIntegerPoint(new int[] { 1959, 325100 });
        EuclideanIntegerPoint pt2 = new EuclideanIntegerPoint(new int[] { 1960, 373200 });
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt1));
        Assert.assertTrue(clusters.get(0).getPoints().contains(pt2));
    }

    @Test
    public void testCertainConvergence() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        EuclideanIntegerPoint numberOfPoints = new EuclideanIntegerPoint(new int[] { 0 });
        List<EuclideanIntegerPoint> points = new ArrayList<EuclideanIntegerPoint>();
        points.add(numberOfPoints);
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(points, 1, 600);
        Assert.assertEquals(1, clusters.size());
    }

    @Test
    public void testClusteringTwoClusters() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        List<EuclideanIntegerPoint> points = Arrays.asList(
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 1, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 1 }),
            new EuclideanIntegerPoint(new int[] { 100, 100 }),
            new EuclideanIntegerPoint(new int[] { 101, 100 }),
            new EuclideanIntegerPoint(new int[] { 100, 101 })
        );
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(points, 2, 10);
        Assert.assertEquals(2, clusters.size());
        int size0 = clusters.get(0).getPoints().size();
        int size1 = clusters.get(1).getPoints().size();
        Assert.assertTrue((size0 == 3 && size1 == 3));
    }

    @Test
    public void testNegativeMaxIterations() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(1746432956321l));
        List<EuclideanIntegerPoint> points = Arrays.asList(
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 10, 10 })
        );
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(points, 2, -1);
        Assert.assertEquals(2, clusters.size());
    }

    @Test(expected = ConvergenceException.class)
    public void testEmptyClusterStrategyError() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                        new Random(0),
                        KMeansPlusPlusClusterer.EmptyClusterStrategy.ERROR);
        List<EuclideanIntegerPoint> points = Arrays.asList(
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 })
        );
        transformer.cluster(points, 2, 10);
    }

    @Test
    public void testEmptyClusterStrategyLargestVariance() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                        new Random(0),
                        KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        List<EuclideanIntegerPoint> points = Arrays.asList(
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 10, 10 }),
            new EuclideanIntegerPoint(new int[] { 100, 100 })
        );
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(points, 2, 10);
        Assert.assertEquals(2, clusters.size());
    }

    @Test
    public void testEmptyClusterStrategyLargestPointsNumber() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                        new Random(0),
                        KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
        List<EuclideanIntegerPoint> points = Arrays.asList(
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 10, 10 }),
            new EuclideanIntegerPoint(new int[] { 100, 100 })
        );
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(points, 2, 10);
        Assert.assertEquals(2, clusters.size());
    }

    @Test
    public void testEmptyClusterStrategyFarthestPoint() {
        KMeansPlusPlusClusterer<EuclideanIntegerPoint> transformer =
                new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
                        new Random(0),
                        KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT);
        List<EuclideanIntegerPoint> points = Arrays.asList(
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 10, 10 }),
            new EuclideanIntegerPoint(new int[] { 100, 100 })
        );
        List<Cluster<EuclideanIntegerPoint>> clusters = transformer.cluster(points, 2, 10);
        Assert.assertEquals(2, clusters.size());
    }

    @Test
    public void testEmptyClusterStrategyEnum() {
        for (KMeansPlusPlusClusterer.EmptyClusterStrategy strategy :
                KMeansPlusPlusClusterer.EmptyClusterStrategy.values()) {
            Assert.assertNotNull(strategy);
            Assert.assertEquals(strategy, KMeansPlusPlusClusterer.EmptyClusterStrategy.valueOf(strategy.name()));
        }
    }
}
