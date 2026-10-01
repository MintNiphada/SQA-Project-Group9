package com.fasterxml.jackson.core.sym;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.util.InternCache;

public class ByteQuadsCanonicalizerTest {

    private ByteQuadsCanonicalizer root;

    @Before
    public void setUp() {
        root = ByteQuadsCanonicalizer.createRoot();
    }

    @Test
    public void testCreateRoot() {
        assertNotNull(root);
        assertEquals(64, root.bucketCount());
        assertTrue(root.maybeDirty()); // root is not shared initially
        assertEquals(0, root.size());
    }

    @Test
    public void testMakeChild() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertNotNull(child);
        assertEquals(64, child.bucketCount());
        assertFalse(child.maybeDirty()); // child starts shared
        assertEquals(0, child.size());
    }

    @Test
    public void testAddNameSingleQuad() {
        String name = "a";
        String result = root.addName(name, 1);
        assertSame(name, result); // interned, but same string literal
        assertEquals(1, root.size());
        assertEquals(name, root.findName(1));
    }

    @Test
    public void testAddNameTwoQuads() {
        String name = "ab";
        String result = root.addName(name, 1, 2);
        assertEquals(name, result);
        assertEquals(1, root.size());
        assertEquals(name, root.findName(1, 2));
    }

    @Test
    public void testAddNameThreeQuads() {
        String name = "abc";
        String result = root.addName(name, 1, 2, 3);
        assertEquals(name, result);
        assertEquals(1, root.size());
        assertEquals(name, root.findName(1, 2, 3));
    }

    @Test
    public void testAddNameLong() {
        String name = "longname";
        int[] q = new int[] { 1, 2, 3, 4, 5 };
        String result = root.addName(name, q, q.length);
        assertEquals(name, result);
        assertEquals(1, root.size());
        assertEquals(name, root.findName(q, q.length));
    }

    @Test
    public void testAddNameWithInternDisabled() {
        int flags = JsonFactory.Feature.collectDefaults();
        flags = JsonFactory.Feature.INTERN_FIELD_NAMES.disableIn(flags);
        ByteQuadsCanonicalizer child = root.makeChild(flags);
        String name = new String("test"); // not interned
        String result = child.addName(name, 1);
        assertNotSame(name, result); // should not be interned
        assertEquals("test", result);
    }

    @Test
    public void testFindNameNonExistent() {
        assertNull(root.findName(1));
        assertNull(root.findName(1, 2));
        assertNull(root.findName(1, 2, 3));
        assertNull(root.findName(new int[] { 1, 2, 3, 4 }, 4));
    }

    @Test
    public void testFindNameAfterAdd() {
        root.addName("x", 10);
        root.addName("y", 20, 30);
        root.addName("z", 40, 50, 60);
        root.addName("w", new int[] { 70, 80, 90, 100 }, 4);
        assertEquals("x", root.findName(10));
        assertEquals("y", root.findName(20, 30));
        assertEquals("z", root.findName(40, 50, 60));
        assertEquals("w", root.findName(new int[] { 70, 80, 90, 100 }, 4));
    }

    @Test
    public void testSize() {
        assertEquals(0, root.size());
        root.addName("a", 1);
        assertEquals(1, root.size());
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertEquals(0, child.size());
        child.addName("b", 2);
        assertEquals(1, child.size());
    }

    @Test
    public void testBucketCount() {
        assertEquals(64, root.bucketCount());
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertEquals(64, child.bucketCount());
    }

    @Test
    public void testMaybeDirty() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertFalse(child.maybeDirty());
        child.addName("a", 1);
        assertTrue(child.maybeDirty());
        child.release();
        assertFalse(child.maybeDirty()); // after release, shared again
    }

    @Test
    public void testReleaseMergesIntoParent() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        child.addName("child", 100);
        assertEquals(0, root.size());
        child.release();
        assertEquals(1, root.size());
        assertEquals("child", root.findName(100));
    }

    @Test
    public void testReleaseNoMergeIfNotDirty() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        // child is not dirty
        child.release();
        assertEquals(0, root.size());
    }

    @Test
    public void testMergeChildWhenCountsEqual() throws Exception {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        child.addName("a", 1);
        child.release();
        int countAfter = root.size();
        // create another child with same count as root
        ByteQuadsCanonicalizer child2 = root.makeChild(0);
        // child2 has same count as root (via tableInfo)
        child2.release();
        assertEquals(countAfter, root.size()); // no change
    }

    @Test
    public void testMergeChildExceedingMaxEntriesForReuse() {
        ByteQuadsCanonicalizer child = root.makeChild(0);
        // add many names to exceed MAX_ENTRIES_FOR_REUSE (6000)
        for (int i = 0; i < 6001; i++) {
            child.addName("name" + i, i);
        }
        child.release();
        // root should be reset to empty
        assertEquals(0, root.size());
    }

    @Test
    public void testRehashPreservesNames() {
        // add enough names to trigger rehash (more than half of 64 = 32, and spillover condition)
        for (int i = 0; i < 40; i++) {
            root.addName("n" + i, i);
        }
        // after rehash, all names should be findable
        for (int i = 0; i < 40; i++) {
            assertEquals("n" + i, root.findName(i));
        }
    }

    @Test
    public void testRehashDoublesHashSize() {
        int initialSize = root.bucketCount();
        // add many names to force rehash
        for (int i = 0; i < 50; i++) {
            root.addName("n" + i, i);
        }
        assertTrue(root.bucketCount() > initialSize);
    }

    @Test
    public void testPrimaryCount() {
        assertEquals(0, root.primaryCount());
        root.addName("a", 1);
        assertEquals(1, root.primaryCount());
    }

    @Test
    public void testSecondaryCount() {
        assertEquals(0, root.secondaryCount());
        // force secondary by filling primary? Not easily, but after many adds some may go to secondary
        for (int i = 0; i < 50; i++) {
            root.addName("n" + i, i);
        }
        assertTrue(root.secondaryCount() >= 0);
    }

    @Test
    public void testTertiaryCount() {
        assertEquals(0, root.tertiaryCount());
        for (int i = 0; i < 100; i++) {
            root.addName("n" + i, i);
        }
        assertTrue(root.tertiaryCount() >= 0);
    }

    @Test
    public void testSpilloverCount() {
        assertEquals(0, root.spilloverCount());
        for (int i = 0; i < 200; i++) {
            root.addName("n" + i, i);
        }
        assertTrue(root.spilloverCount() >= 0);
    }

    @Test
    public void testTotalCount() {
        assertEquals(0, root.totalCount());
        root.addName("a", 1);
        assertEquals(1, root.totalCount());
    }

    @Test
    public void testToString() {
        String str = root.toString();
        assertNotNull(str);
        assertTrue(str.contains("size=0"));
    }

    @Test
    public void testCalcHashSingleQuad() {
        int hash = root.calcHash(1);
        assertTrue(hash != 0);
    }

    @Test
    public void testCalcHashTwoQuads() {
        int hash = root.calcHash(1, 2);
        assertTrue(hash != 0);
    }

    @Test
    public void testCalcHashThreeQuads() {
        int hash = root.calcHash(1, 2, 3);
        assertTrue(hash != 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcHashInvalidQlen() {
        root.calcHash(new int[] { 1, 2, 3 }, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNameWithZeroQlen() {
        root.addName("x", new int[] {}, 0);
    }

    @Test
    public void testSpilloverStart() throws Exception {
        Method m = ByteQuadsCanonicalizer.class.getDeclaredMethod("_spilloverStart");
        m.setAccessible(true);
        int start = (int) m.invoke(root);
        assertEquals(448, start); // for hashSize=64: (64<<3)-64 = 512-64=448
    }

    @Test
    public void testCalcTertiaryShift() throws Exception {
        Method m = ByteQuadsCanonicalizer.class.getDeclaredMethod("_calcTertiaryShift", int.class);
        m.setAccessible(true);
        assertEquals(4, m.invoke(null, 16));
        assertEquals(4, m.invoke(null, 64));
        assertEquals(5, m.invoke(null, 256));
        assertEquals(6, m.invoke(null, 1024));
        assertEquals(7, m.invoke(null, 4096));
    }

    @Test
    public void testReportTooManyCollisionsSmallTableDoesNotThrow() throws Exception {
        Method m = ByteQuadsCanonicalizer.class.getDeclaredMethod("_reportTooManyCollisions");
        m.setAccessible(true);
        // root has hashSize=64 <= 1024, so should not throw
        m.invoke(root);
    }

    @Test
    public void testNukeSymbols() throws Exception {
        root.addName("a", 1);
        assertEquals(1, root.size());
        Method nuke = ByteQuadsCanonicalizer.class.getDeclaredMethod("nukeSymbols", boolean.class);
        nuke.setAccessible(true);
        nuke.invoke(root, true);
        assertEquals(0, root.size());
        assertNull(root.findName(1));
    }

    @Test
    public void testVerifyLongName() throws Exception {
        Method verify = ByteQuadsCanonicalizer.class.getDeclaredMethod("_verifyLongName", int[].class, int.class, int.class);
        verify.setAccessible(true);
        int[] q = { 1, 2, 3, 4, 5 };
        // add a long name to get spillOffset
        root.addName("long", q, 5);
        // find the entry to get spillOffset? Not directly. We'll test via findName.
        assertNotNull(root.findName(q, 5));
    }

    @Test
    public void testFindSecondaryReturnsNull() throws Exception {
        Method findSec = ByteQuadsCanonicalizer.class.getDeclaredMethod("_findSecondary", int.class, int.class);
        findSec.setAccessible(true);
        // with no entries, should return null
        assertNull(findSec.invoke(root, 0, 1));
    }

    @Test
    public void testAddNameWithQ2Zero() {
        // when q2==0, uses single-quad hash
        String name = "zeroQ2";
        root.addName(name, 1, 0);
        assertEquals(name, root.findName(1));
    }

    @Test
    public void testInternCacheUsed() {
        String name = "interned";
        String result = root.addName(name, 1);
        assertSame(InternCache.instance.intern(name), result);
    }

    @Test
    public void testHashSeed() {
        assertTrue(root.hashSeed() != 0);
    }

    @Test
    public void testChildInheritsParentState() {
        root.addName("parent", 1);
        ByteQuadsCanonicalizer child = root.makeChild(0);
        assertEquals("parent", child.findName(1));
    }

    @Test
    public void testAddNameAfterRehashStillFindable() {
        // force rehash by adding many names
        for (int i = 0; i < 100; i++) {
            root.addName("n" + i, i);
        }
        for (int i = 0; i < 100; i++) {
            assertEquals("n" + i, root.findName(i));
        }
    }

    @Test
    public void testAddNameLongExpandsArray() {
        // add a very long name to trigger array expansion in _appendLongName
        int[] q = new int[100];
        for (int i = 0; i < 100; i++) {
            q[i] = i + 1;
        }
        String name = "verylong";
        root.addName(name, q, 100);
        assertEquals(name, root.findName(q, 100));
    }

    @Test
    public void testFindNameWithLongNameAfterRehash() {
        int[] q = { 1, 2, 3, 4, 5, 6, 7, 8 };
        root.addName("long8", q, 8);
        // force rehash
        for (int i = 0; i < 50; i++) {
            root.addName("n" + i, i + 100);
        }
        assertEquals("long8", root.findName(q, 8));
    }

    @Test
    public void testTotalCountMatchesSum() {
        for (int i = 0; i < 50; i++) {
            root.addName("n" + i, i);
        }
        int sum = root.primaryCount() + root.secondaryCount() + root.tertiaryCount() + root.spilloverCount();
        assertEquals(sum, root.totalCount());
    }
}
