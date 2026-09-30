package org.jfree.chart.block;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.Size2D;
import org.jfree.data.Range;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link BorderArrangement} class.
 */
public class BorderArrangementTest {

    private Graphics2D g2;

    @Before
    public void setUp() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        this.g2 = image.createGraphics();
    }

    /**
     * Test adding blocks at different edges and clearing the arrangement.
     */
    @Test
    public void testAddAndClear() {
        BorderArrangement ba = new BorderArrangement();
        Block bCenter = new EmptyBlock(10.0, 10.0);
        Block bTop = new EmptyBlock(10.0, 20.0);
        Block bBottom = new EmptyBlock(10.0, 30.0);
        Block bLeft = new EmptyBlock(40.0, 10.0);
        Block bRight = new EmptyBlock(50.0, 10.0);

        ba.add(bCenter, null);
        ba.add(bTop, RectangleEdge.TOP);
        ba.add(bBottom, RectangleEdge.BOTTOM);
        ba.add(bLeft, RectangleEdge.LEFT);
        ba.add(bRight, RectangleEdge.RIGHT);

        BorderArrangement ba2 = new BorderArrangement();
        ba2.add(bCenter, null);
        ba2.add(bTop, RectangleEdge.TOP);
        ba2.add(bBottom, RectangleEdge.BOTTOM);
        ba2.add(bLeft, RectangleEdge.LEFT);
        ba2.add(bRight, RectangleEdge.RIGHT);

        Assert.assertEquals(ba, ba2);

        ba.clear();
        BorderArrangement empty = new BorderArrangement();
        Assert.assertEquals(empty, ba);
    }

    /**
     * Test the equals method thoroughly.
     */
    @Test
    public void testEquals() {
        BorderArrangement b1 = new BorderArrangement();
        BorderArrangement b2 = new BorderArrangement();
        Assert.assertTrue(b1.equals(b1));
        Assert.assertFalse(b1.equals(null));
        Assert.assertFalse(b1.equals("Not a BorderArrangement"));
        Assert.assertTrue(b1.equals(b2));

        Block top1 = new EmptyBlock(1.0, 1.0);
        Block top2 = new EmptyBlock(1.0, 2.0);
        b1.add(top1, RectangleEdge.TOP);
        Assert.assertFalse(b1.equals(b2));
        b2.add(top2, RectangleEdge.TOP);
        Assert.assertFalse(b1.equals(b2));
        b2.add(top1, RectangleEdge.TOP);
        Assert.assertTrue(b1.equals(b2));

        Block bottom1 = new EmptyBlock(2.0, 1.0);
        b1.add(bottom1, RectangleEdge.BOTTOM);
        Assert.assertFalse(b1.equals(b2));
        b2.add(bottom1, RectangleEdge.BOTTOM);
        Assert.assertTrue(b1.equals(b2));

        Block left1 = new EmptyBlock(3.0, 1.0);
        b1.add(left1, RectangleEdge.LEFT);
        Assert.assertFalse(b1.equals(b2));
        b2.add(left1, RectangleEdge.LEFT);
        Assert.assertTrue(b1.equals(b2));

        Block right1 = new EmptyBlock(4.0, 1.0);
        b1.add(right1, RectangleEdge.RIGHT);
        Assert.assertFalse(b1.equals(b2));
        b2.add(right1, RectangleEdge.RIGHT);
        Assert.assertTrue(b1.equals(b2));

        Block center1 = new EmptyBlock(5.0, 1.0);
        b1.add(center1, null);
        Assert.assertFalse(b1.equals(b2));
        b2.add(center1, null);
        Assert.assertTrue(b1.equals(b2));
    }

    /**
     * Test serialization.
     */
    @Test
    public void testSerialization() throws Exception {
        BorderArrangement ba1 = new BorderArrangement();
        ba1.add(new EmptyBlock(10.0, 10.0), null);
        ba1.add(new EmptyBlock(10.0, 20.0), RectangleEdge.TOP);
        ba1.add(new EmptyBlock(10.0, 30.0), RectangleEdge.BOTTOM);
        ba1.add(new EmptyBlock(40.0, 10.0), RectangleEdge.LEFT);
        ba1.add(new EmptyBlock(50.0, 10.0), RectangleEdge.RIGHT);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(ba1);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        BorderArrangement ba2 = (BorderArrangement) in.readObject();
        in.close();

        Assert.assertEquals(ba1, ba2);
    }

    /**
     * Test arrange with NN (NONE, NONE) constraint.
     */
    @Test
    public void testArrangeNN() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        EmptyBlock center = new EmptyBlock(50.0, 60.0);
        EmptyBlock top = new EmptyBlock(100.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(100.0, 30.0);
        EmptyBlock left = new EmptyBlock(15.0, 50.0);
        EmptyBlock right = new EmptyBlock(25.0, 70.0);

        container.add(center, null);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);

        Size2D size = container.arrange(this.g2, RectangleConstraint.NONE);
        // center height max(50, 70, 60) = 70
        // total height = 20 + 30 + 70 = 120
        // center width = 15 + 50 + 25 = 90
        // total width = max(100, 100, 90) = 100
        Assert.assertEquals(100.0, size.getWidth(), 0.0001);
        Assert.assertEquals(120.0, size.getHeight(), 0.0001);

        Assert.assertEquals(0.0, top.getBounds().getX(), 0.0001);
        Assert.assertEquals(0.0, top.getBounds().getY(), 0.0001);
        Assert.assertEquals(100.0, top.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(20.0, top.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(0.0, bottom.getBounds().getX(), 0.0001);
        Assert.assertEquals(90.0, bottom.getBounds().getY(), 0.0001);
        Assert.assertEquals(100.0, bottom.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(30.0, bottom.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(0.0, left.getBounds().getX(), 0.0001);
        Assert.assertEquals(20.0, left.getBounds().getY(), 0.0001);
        Assert.assertEquals(15.0, left.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(70.0, left.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(75.0, right.getBounds().getX(), 0.0001);
        Assert.assertEquals(20.0, right.getBounds().getY(), 0.0001);
        Assert.assertEquals(25.0, right.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(70.0, right.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(15.0, center.getBounds().getX(), 0.0001);
        Assert.assertEquals(20.0, center.getBounds().getY(), 0.0001);
        Assert.assertEquals(60.0, center.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(70.0, center.getBounds().getHeight(), 0.0001);
    }

    /**
     * Test arrange NN on empty container.
     */
    @Test
    public void testArrangeNNEmpty() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        Size2D size = container.arrange(this.g2, RectangleConstraint.NONE);
        Assert.assertEquals(0.0, size.getWidth(), 0.0001);
        Assert.assertEquals(0.0, size.getHeight(), 0.0001);
    }

    /**
     * Test arrange with FF (FIXED, FIXED) constraint.
     */
    @Test
    public void testArrangeFF() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        EmptyBlock center = new EmptyBlock(10.0, 10.0);
        EmptyBlock top = new EmptyBlock(10.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(10.0, 30.0);
        EmptyBlock left = new EmptyBlock(15.0, 10.0);
        EmptyBlock right = new EmptyBlock(25.0, 10.0);

        container.add(center, null);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);

        RectangleConstraint constraint = new RectangleConstraint(100.0, 200.0);
        Size2D size = container.arrange(this.g2, constraint);
        Assert.assertEquals(100.0, size.getWidth(), 0.0001);
        Assert.assertEquals(200.0, size.getHeight(), 0.0001);

        Assert.assertEquals(100.0, top.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(20.0, top.getBounds().getHeight(), 0.0001);
        Assert.assertEquals(100.0, bottom.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(30.0, bottom.getBounds().getHeight(), 0.0001);

        // Center height = 200 - 20 - 30 = 150
        Assert.assertEquals(150.0, left.getBounds().getHeight(), 0.0001);
        Assert.assertEquals(15.0, left.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(150.0, right.getBounds().getHeight(), 0.0001);
        Assert.assertEquals(25.0, right.getBounds().getWidth(), 0.0001);

        // Center width = 100 - 15 - 25 = 60
        Assert.assertEquals(60.0, center.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(150.0, center.getBounds().getHeight(), 0.0001);
        Assert.assertEquals(15.0, center.getBounds().getX(), 0.0001);
        Assert.assertEquals(20.0, center.getBounds().getY(), 0.0001);
    }

    /**
     * Test arrange FF on empty container.
     */
    @Test
    public void testArrangeFFEmpty() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        RectangleConstraint constraint = new RectangleConstraint(100.0, 200.0);
        Size2D size = container.arrange(this.g2, constraint);
        Assert.assertEquals(100.0, size.getWidth(), 0.0001);
        Assert.assertEquals(200.0, size.getHeight(), 0.0001);
    }

    /**
     * Test arrange with FN (FIXED, NONE) constraint.
     */
    @Test
    public void testArrangeFN() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        EmptyBlock center = new EmptyBlock(10.0, 50.0);
        EmptyBlock top = new EmptyBlock(10.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(10.0, 30.0);
        EmptyBlock left = new EmptyBlock(15.0, 40.0);
        EmptyBlock right = new EmptyBlock(25.0, 45.0);

        container.add(center, null);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);

        RectangleConstraint constraint = new RectangleConstraint(100.0, null,
                LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        Size2D size = container.arrange(this.g2, constraint);
        Assert.assertEquals(100.0, size.getWidth(), 0.0001);
        // height: top(20) + bottom(30) + max(40, 45, 50) = 100
        Assert.assertEquals(100.0, size.getHeight(), 0.0001);
    }

    /**
     * Test arrange with FR (FIXED, RANGE) constraint within range.
     */
    @Test
    public void testArrangeFRWithinRange() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        container.add(new EmptyBlock(10.0, 20.0), RectangleEdge.TOP);
        container.add(new EmptyBlock(10.0, 30.0), RectangleEdge.BOTTOM);
        container.add(new EmptyBlock(50.0, 50.0), null);

        // Height will be 20 + 30 + 50 = 100.
        RectangleConstraint constraint = new RectangleConstraint(100.0, new Range(50.0, 150.0));
        Size2D size = container.arrange(this.g2, constraint);
        Assert.assertEquals(100.0, size.getWidth(), 0.0001);
        Assert.assertEquals(100.0, size.getHeight(), 0.0001);
    }

    /**
     * Test arrange with FR (FIXED, RANGE) constraint outside range (requires clamping).
     */
    @Test
    public void testArrangeFROutsideRange() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        container.add(new EmptyBlock(10.0, 20.0), RectangleEdge.TOP);
        container.add(new EmptyBlock(10.0, 30.0), RectangleEdge.BOTTOM);
        container.add(new EmptyBlock(50.0, 50.0), null);

        // Height will be 100, but range is [120, 150].
        RectangleConstraint constraint = new RectangleConstraint(100.0, new Range(120.0, 150.0));
        Size2D size = container.arrange(this.g2, constraint);
        Assert.assertEquals(100.0, size.getWidth(), 0.0001);
        Assert.assertEquals(120.0, size.getHeight(), 0.0001);
    }

    /**
     * Test arrange with RR (RANGE, RANGE) constraint.
     */
    @Test
    public void testArrangeRR() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        EmptyBlock center = new EmptyBlock(50.0, 60.0);
        EmptyBlock top = new EmptyBlock(100.0, 20.0);
        EmptyBlock bottom = new EmptyBlock(100.0, 30.0);
        EmptyBlock left = new EmptyBlock(15.0, 50.0);
        EmptyBlock right = new EmptyBlock(25.0, 70.0);

        container.add(center, null);
        container.add(top, RectangleEdge.TOP);
        container.add(bottom, RectangleEdge.BOTTOM);
        container.add(left, RectangleEdge.LEFT);
        container.add(right, RectangleEdge.RIGHT);

        RectangleConstraint constraint = new RectangleConstraint(
                new Range(50.0, 200.0), new Range(50.0, 200.0));
        Size2D size = container.arrange(this.g2, constraint);
        Assert.assertEquals(100.0, size.getWidth(), 0.0001);
        Assert.assertEquals(120.0, size.getHeight(), 0.0001);

        Assert.assertEquals(0.0, top.getBounds().getX(), 0.0001);
        Assert.assertEquals(0.0, top.getBounds().getY(), 0.0001);
        Assert.assertEquals(100.0, top.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(20.0, top.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(0.0, bottom.getBounds().getX(), 0.0001);
        Assert.assertEquals(90.0, bottom.getBounds().getY(), 0.0001);
        Assert.assertEquals(100.0, bottom.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(30.0, bottom.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(0.0, left.getBounds().getX(), 0.0001);
        Assert.assertEquals(20.0, left.getBounds().getY(), 0.0001);
        Assert.assertEquals(15.0, left.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(70.0, left.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(75.0, right.getBounds().getX(), 0.0001);
        Assert.assertEquals(20.0, right.getBounds().getY(), 0.0001);
        Assert.assertEquals(25.0, right.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(70.0, right.getBounds().getHeight(), 0.0001);

        Assert.assertEquals(15.0, center.getBounds().getX(), 0.0001);
        Assert.assertEquals(20.0, center.getBounds().getY(), 0.0001);
        Assert.assertEquals(60.0, center.getBounds().getWidth(), 0.0001);
        Assert.assertEquals(70.0, center.getBounds().getHeight(), 0.0001);
    }

    /**
     * Test arrange RR on empty container.
     */
    @Test
    public void testArrangeRREmpty() {
        BlockContainer container = new BlockContainer(new BorderArrangement());
        RectangleConstraint constraint = new RectangleConstraint(
                new Range(50.0, 200.0), new Range(50.0, 200.0));
        Size2D size = container.arrange(this.g2, constraint);
        Assert.assertEquals(0.0, size.getWidth(), 0.0001);
        Assert.assertEquals(0.0, size.getHeight(), 0.0001);
    }

    /**
     * Test unsupported constraints throw RuntimeException.
     */
    @Test(expected = RuntimeException.class)
    public void testArrangeNoneFixedThrowsException() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        RectangleConstraint constraint = new RectangleConstraint(0.0, null,
                LengthConstraintType.NONE, 100.0, null, LengthConstraintType.FIXED);
        ba.arrange(container, this.g2, constraint);
    }

    @Test(expected = RuntimeException.class)
    public void testArrangeNoneRangeThrowsException() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        RectangleConstraint constraint = new RectangleConstraint(0.0, null,
                LengthConstraintType.NONE, 0.0, new Range(0.0, 100.0), LengthConstraintType.RANGE);
        ba.arrange(container, this.g2, constraint);
    }

    @Test(expected = RuntimeException.class)
    public void testArrangeRangeNoneThrowsException() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        RectangleConstraint constraint = new RectangleConstraint(0.0, new Range(0.0, 100.0),
                LengthConstraintType.RANGE, 0.0, null, LengthConstraintType.NONE);
        ba.arrange(container, this.g2, constraint);
    }

    @Test(expected = RuntimeException.class)
    public void testArrangeRangeFixedThrowsException() {
        BorderArrangement ba = new BorderArrangement();
        BlockContainer container = new BlockContainer(ba);
        RectangleConstraint constraint = new RectangleConstraint(0.0, new Range(0.0, 100.0),
                LengthConstraintType.RANGE, 100.0, null, LengthConstraintType.FIXED);
        ba.arrange(container, this.g2, constraint);
    }
}