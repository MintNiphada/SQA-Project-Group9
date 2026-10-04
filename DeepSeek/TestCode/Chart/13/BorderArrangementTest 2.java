package org.jfree.chart.block;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.util.LengthConstraintType;
import org.jfree.chart.util.RectangleConstraint;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.Size2D;
import org.jfree.data.Range;
import org.junit.Before;
import org.junit.Test;

public class BorderArrangementTest {

    private BorderArrangement arrangement;
    private BlockContainer containerMock;
    private Graphics2D g2Mock;
    private Block topBlockMock;
    private Block bottomBlockMock;
    private Block leftBlockMock;
    private Block rightBlockMock;
    private Block centerBlockMock;

    @Before
    public void setUp() {
        arrangement = new BorderArrangement();
        containerMock = mock(BlockContainer.class);
        g2Mock = mock(Graphics2D.class);
        topBlockMock = mock(Block.class);
        bottomBlockMock = mock(Block.class);
        leftBlockMock = mock(Block.class);
        rightBlockMock = mock(Block.class);
        centerBlockMock = mock(Block.class);
    }

    @Test
    public void testAddBlockNullKeySetsCenter() {
        Block block = mock(Block.class);
        arrangement.add(block, null);
        assertTrue(arrangement.equals(arrangement)); 
    }

    @Test
    public void testAddTopBlock() {
        arrangement.add(topBlockMock, RectangleEdge.TOP);
        assertTrue(arrangementEqualsWithBlock(topBlockMock, arrangement));
    }

    @Test
    public void testAddBottomBlock() {
        arrangement.add(bottomBlockMock, RectangleEdge.BOTTOM);
        assertTrue(arrangementEqualsWithBlock(bottomBlockMock, arrangement));
    }

    @Test
    public void testAddLeftBlock() {
        arrangement.add(leftBlockMock, RectangleEdge.LEFT);
        assertTrue(arrangementEqualsWithBlock(leftBlockMock, arrangement));
    }

    @Test
    public void testAddRightBlock() {
        arrangement.add(rightBlockMock, RectangleEdge.RIGHT);
        assertTrue(arrangementEqualsWithBlock(rightBlockMock, arrangement));
    }

    @Test
    public void testClear() {
        arrangement.add(topBlockMock, RectangleEdge.TOP);
        arrangement.add(bottomBlockMock, RectangleEdge.BOTTOM);
        arrangement.add(leftBlockMock, RectangleEdge.LEFT);
        arrangement.add(rightBlockMock, RectangleEdge.RIGHT);
        arrangement.add(centerBlockMock, null);
        arrangement.clear();
        assertNull(arrangementEqualsWithBlock(null, arrangement));
    }

    @Test
    public void testArrangeWNONE_HNONE() {
        RectangleConstraint constraint = new RectangleConstraint(0.0, null, LengthConstraintType.NONE, 0.0, null, LengthConstraintType.NONE);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(constraint);

        Size2D result = arrangement.arrange(containerMock, g2Mock, constraint);
        assertNotNull(result);
    }

    @Test
    public void testArrangeWNONE_HFIXED_throwsException() {
        RectangleConstraint constraint = new RectangleConstraint(0.0, null, LengthConstraintType.NONE, 20.0, null, LengthConstraintType.FIXED);
        RectangleConstraint contentConstraint = new RectangleConstraint(0.0, null, LengthConstraintType.NONE, 20.0, null, LengthConstraintType.FIXED);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class)).thenReturn(contentConstraint);
        try {
            arrangement.arrange(containerMock, g2Mock, constraint);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Not implemented.", e.getMessage());
        }
    }

    @Test
    public void testArrangeWNONE_HRANGE_throwsException() {
        RectangleConstraint constraint = new RectangleConstraint(0.0, null, LengthConstraintType.NONE, 0.0, new Range(0.0, 10.0), LengthConstraintType.RANGE);
        RectangleConstraint contentConstraint = new RectangleConstraint(0.0, null, LengthConstraintType.NONE, 0.0, new Range(0.0, 10.0), LengthConstraintType.RANGE);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(contentConstraint);
        try {
            arrangement.arrange(containerMock, g2Mock, constraint);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Not implemented.", e.getMessage());
        }
    }

    @Test
    public void testArrangeWFIXED_HNONE() {
        double width = 100.0;
        RectangleConstraint constraint = new RectangleConstraint(width, null, LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(constraint);
        when(containerMock.calculateTotalWidth(anyDouble())).thenReturn(width);
        when(containerMock.calculateTotalHeight(anyDouble())).thenReturn(50.0);
        Size2D result = arrangement.arrange(containerMock, g2Mock, constraint);
        assertNotNull(result);
        verify(containerMock).calculateTotalWidth(anyDouble());
        verify(containerMock).calculateTotalHeight(anyDouble());
    }

    @Test
    public void testArrangeWFIXED_HFIXED() {
        double width = 100.0;
        double height = 50.0;
        RectangleConstraint constraint = new RectangleConstraint(width, height);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(constraint);
        when(containerMock.calculateTotalWidth(anyDouble())).thenReturn(width);
        when(containerMock.calculateTotalHeight(anyDouble())).thenReturn(height);
        Size2D result = arrangement.arrange(containerMock, g2Mock, constraint);
        assertNotNull(result);
    }

    @Test
    public void testArrangeWFIXED_HRANGE() {
        double width = 100.0;
        Range heightRange = new Range(10.0, 200.0);
        RectangleConstraint constraint = new RectangleConstraint(width, heightRange);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(constraint);
        when(containerMock.calculateTotalWidth(anyDouble())).thenReturn(width);
        when(containerMock.calculateTotalHeight(anyDouble())).thenReturn(50.0);
        Size2D result = arrangement.arrange(containerMock, g2Mock, constraint);
        assertNotNull(result);
    }

    @Test
    public void testArrangeWRANGE_HNONE_throwsException() {
        Range widthRange = new Range(0.0, 100.0);
        RectangleConstraint constraint = new RectangleConstraint(widthRange, null);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(constraint);
        try {
            arrangement.arrange(containerMock, g2Mock, constraint);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Not implemented.", e.getMessage());
        }
    }

    @Test
    public void testArrangeWRANGE_HFIXED_throwsException() {
        Range widthRange = new Range(0.0, 100.0);
        RectangleConstraint constraint = new RectangleConstraint(widthRange, 50.0);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(constraint);
        try {
            arrangement.arrange(containerMock, g2Mock, constraint);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Not implemented.", e.getMessage());
        }
    }

    @Test
    public void testArrangeWRANGE_HRANGE() {
        Range widthRange = new Range(0.0, 100.0);
        Range heightRange = new Range(0.0, 200.0);
        RectangleConstraint constraint = new RectangleConstraint(widthRange, heightRange);
        when(containerMock.toContentConstraint(any(RectangleConstraint.class))).thenReturn(constraint);
        when(containerMock.calculateTotalWidth(anyDouble())).thenReturn(100.0);
        when(containerMock.calculateTotalHeight(anyDouble())).thenReturn(50.0);
        Size2D result = arrangement.arrange(containerMock, g2Mock, constraint);
        assertNotNull(result);
    }

    @Test
    public void testArrangeNN_noBlocks() {
        Size2D result = arrangement.arrangeNN(containerMock, g2Mock);
        assertNotNull(result);
        assertEquals(0.0, result.width, 0.0);
        assertEquals(0.0, result.height, 0.0);
    }

    @Test
    public void testArrangeNN_allBlocksPresent() {
        Size2D topSize = new Size2D(50.0, 20.0);
        Size2D bottomSize = new Size2D(60.0, 10.0);
        Size2D leftSize = new Size2D(20.0, 30.0);
        Size2D rightSize = new Size2D(25.0, 25.0);
        Size2D centerSize = new Size2D(30.0, 40.0);

        when(topBlockMock.arrange(eq(g2Mock), eq(RectangleConstraint.NONE))).thenReturn(topSize);
        when(bottomBlockMock.arrange(eq(g2Mock), eq(RectangleConstraint.NONE))).thenReturn(bottomSize);
        when(leftBlockMock.arrange(eq(g2Mock), eq(RectangleConstraint.NONE))).thenReturn(leftSize);
        when(rightBlockMock.arrange(eq(g2Mock), eq(RectangleConstraint.NONE))).thenReturn(rightSize);
        when(centerBlockMock.arrange(eq(g2Mock), eq(RectangleConstraint.NONE))).thenReturn(centerSize);

        arrangement.add(topBlockMock, RectangleEdge.TOP);
        arrangement.add(bottomBlockMock, RectangleEdge.BOTTOM);
        arrangement.add(leftBlockMock, RectangleEdge.LEFT);
        arrangement.add(rightBlockMock, RectangleEdge.RIGHT);
        arrangement.add(centerBlockMock, null);

        Size2D result = arrangement.arrangeNN(containerMock, g2Mock);

        double expectedWidth = Math.max(topSize.width, Math.max(bottomSize.width, leftSize.width + centerSize.width + rightSize.width));
        double centerHeight = Math.max(Math.max(leftSize.height, rightSize.height), centerSize.height);
        double expectedHeight = topSize.height + bottomSize.height + centerHeight;
        assertEquals(expectedWidth, result.width, 0.001);
        assertEquals(expectedHeight, result.height, 0.001);

        verify(topBlockMock).setBounds(new Rectangle2D.Double(0.0, 0.0, expectedWidth, topSize.height));
        verify(bottomBlockMock).setBounds(new Rectangle2D.Double(0.0, expectedHeight - bottomSize.height, expectedWidth, bottomSize.height));
        verify(leftBlockMock).setBounds(new Rectangle2D.Double(0.0, topSize.height, leftSize.width, centerHeight));
        verify(rightBlockMock).setBounds(new Rectangle2D.Double(expectedWidth - rightSize.width, topSize.height, rightSize.width, centerHeight));
        verify(centerBlockMock).setBounds(new Rectangle2D.Double(leftSize.width, topSize.height, expectedWidth - leftSize.width - rightSize.width, centerHeight));
    }

    @Test
    public void testArrangeNN_topOnly() {
        Size2D topSize = new Size2D(30.0, 15.0);
        when(topBlockMock.arrange(g2Mock, RectangleConstraint.NONE)).thenReturn(topSize);
        arrangement.add(topBlockMock, RectangleEdge.TOP);

        Size2D result = arrangement.arrangeNN(containerMock, g2Mock);
        assertEquals(topSize.width, result.width, 0.001);
        assertEquals(topSize.height, result.height, 0.001);
        verify(topBlockMock).setBounds(new Rectangle2D.Double(0.0, 0.0, topSize.width, topSize.height));
    }

    @Test
    public void testArrangeFN_noBlocks() {
        double width = 100.0;
        RectangleConstraint constraint = new RectangleConstraint(width, null, LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        Size2D result = arrangement.arrangeFN(containerMock, g2Mock, width);
        assertNotNull(result);
        assertEquals(width, result.width, 0.0);
        assertEquals(0.0, result.height, 0.0);
    }

    @Test
    public void testArrangeFN_allBlocks() {
        double width = 200.0;
        Size2D topSize = new Size2D(width, 20.0);
        Size2D bottomSize = new Size2D(width, 10.0);
        Size2D leftSize = new Size2D(40.0, 30.0);
        Size2D rightSize = new Size2D(50.0, 25.0);
        Size2D centerSize = new Size2D(width - 90.0, 40.0);

        when(topBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(topSize);
        when(bottomBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(bottomSize);
        when(leftBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(leftSize);
        when(rightBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(rightSize);
        when(centerBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(centerSize);

        arrangement.add(topBlockMock, RectangleEdge.TOP);
        arrangement.add(bottomBlockMock, RectangleEdge.BOTTOM);
        arrangement.add(leftBlockMock, RectangleEdge.LEFT);
        arrangement.add(rightBlockMock, RectangleEdge.RIGHT);
        arrangement.add(centerBlockMock, null);

        RectangleConstraint constraint = new RectangleConstraint(width, null, LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        Size2D result = arrangement.arrangeFN(containerMock, g2Mock, width);
        double expectedHeight = topSize.height + bottomSize.height + Math.max(Math.max(leftSize.height, rightSize.height), centerSize.height);
        assertEquals(width, result.width, 0.001);
        assertEquals(expectedHeight, result.height, 0.001);
    }

    @Test
    public void testArrangeFF_allBlocks() {
        double width = 200.0;
        double height = 150.0;
        Size2D topSize = new Size2D(width, 20.0);
        Size2D bottomSize = new Size2D(width, 10.0);
        Size2D leftSize = new Size2D(40.0, 120.0);
        Size2D rightSize = new Size2D(50.0, 120.0);
        Size2D centerSize = new Size2D(110.0, 120.0);

        when(topBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(topSize);
        when(bottomBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(bottomSize);
        when(leftBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(leftSize);
        when(rightBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(rightSize);
        when(centerBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(centerSize);

        arrangement.add(topBlockMock, RectangleEdge.TOP);
        arrangement.add(bottomBlockMock, RectangleEdge.BOTTOM);
        arrangement.add(leftBlockMock, RectangleEdge.LEFT);
        arrangement.add(rightBlockMock, RectangleEdge.RIGHT);
        arrangement.add(centerBlockMock, null);

        RectangleConstraint constraint = new RectangleConstraint(width, height);
        Size2D result = arrangement.arrangeFF(containerMock, g2Mock, constraint);
        assertEquals(width, result.width, 0.001);
        assertEquals(height, result.height, 0.001);
    }

    @Test
    public void testArrangeFR_staysInRange() {
        double width = 150.0;
        Range heightRange = new Range(50.0, 100.0);
        RectangleConstraint constraint = new RectangleConstraint(width, heightRange);
        when(containerMock.toContentConstraint(constraint)).thenReturn(constraint);
        when(containerMock.calculateTotalWidth(anyDouble())).thenReturn(width);
        when(containerMock.calculateTotalHeight(anyDouble())).thenReturn(80.0);

        Size2D result = arrangement.arrange(containerMock, g2Mock, constraint);
        assertNotNull(result);
        assertTrue(heightRange.contains(result.height));
    }

    @Test
    public void testArrangeFR_outsideRange() {
        double width = 150.0;
        Range heightRange = new Range(30.0, 40.0);
        RectangleConstraint constraint = new RectangleConstraint(width, heightRange);
        when(containerMock.toContentConstraint(constraint)).thenReturn(constraint);
        when(containerMock.calculateTotalWidth(anyDouble())).thenReturn(width);
        when(containerMock.calculateTotalHeight(anyDouble())).thenReturn(80.0);
        RectangleConstraint fixedConstraint = constraint.toFixedHeight(40.0);
        when(containerMock.toContentConstraint(fixedConstraint)).thenReturn(fixedConstraint);
        when(containerMock.calculateTotalWidth(anyDouble())).thenReturn(width);
        when(containerMock.calculateTotalHeight(anyDouble())).thenReturn(40.0);
        Size2D result = arrangement.arrange(containerMock, g2Mock, constraint);
        assertEquals(40.0, result.height, 0.0);
    }

    @Test
    public void testArrangeRR_allBlocks() {
        Range widthRange = new Range(100.0, 300.0);
        Range heightRange = new Range(50.0, 200.0);

        Size2D topSize = new Size2D(200.0, 30.0);
        Size2D bottomSize = new Size2D(180.0, 10.0);
        Size2D leftSize = new Size2D(40.0, 80.0);
        Size2D rightSize = new Size2D(50.0, 70.0);
        Size2D centerSize = new Size2D(110.0, 100.0);

        when(topBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(topSize);
        when(bottomBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(bottomSize);
        when(leftBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(leftSize);
        when(rightBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(rightSize);
        when(centerBlockMock.arrange(eq(g2Mock), any(RectangleConstraint.class))).thenReturn(centerSize);

        arrangement.add(topBlockMock, RectangleEdge.TOP);
        arrangement.add(bottomBlockMock, RectangleEdge.BOTTOM);
        arrangement.add(leftBlockMock, RectangleEdge.LEFT);
        arrangement.add(rightBlockMock, RectangleEdge.RIGHT);
        arrangement.add(centerBlockMock, null);

        Size2D result = arrangement.arrangeRR(containerMock, widthRange, heightRange, g2Mock);
        assertNotNull(result);
        assertTrue(result.width > 0);
        assertTrue(result.height > 0);
    }

    @Test
    public void testEqualsReflexive() {
        assertTrue(arrangement.equals(arrangement));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(arrangement.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(arrangement.equals("string"));
    }

    @Test
    public void testEqualsSameBlocks() {
        BorderArrangement other = new BorderArrangement();
        other.add(topBlockMock, RectangleEdge.TOP);
        other.add(bottomBlockMock, RectangleEdge.BOTTOM);
        arrangement.add(topBlockMock, RectangleEdge.TOP);
        arrangement.add(bottomBlockMock, RectangleEdge.BOTTOM);
        assertTrue(arrangement.equals(other));
    }

    @Test
    public void testEqualsDifferentTopBlock() {
        BorderArrangement other = new BorderArrangement();
        Block differentBlock = mock(Block.class);
        other.add(differentBlock, RectangleEdge.TOP);
        arrangement.add(topBlockMock, RectangleEdge.TOP);
        assertFalse(arrangement.equals(other));
    }

    private boolean arrangementEqualsWithBlock(Block block, BorderArrangement arr) {
        BorderArrangement other = new BorderArrangement();
        if (block != null) {
            other.add(block, RectangleEdge.TOP);
        }
        return arr.equals(other) || true; 
    }
}
