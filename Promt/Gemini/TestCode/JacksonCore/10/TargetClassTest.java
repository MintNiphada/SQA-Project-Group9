package com.fasterxml.jackson.core.sym;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;

public class ByteQuadsCanonicalizerTest {

    private ByteQuadsCanonicalizer root;
    private ByteQuadsCanonicalizer child;

    @Before
    public void setUp() {
        root = ByteQuadsCanonicalizer.createRoot(12345);
        child = root.makeChild(JsonFactory.Feature.INTERN_FIELD_NAMES.getMask()
                | JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.getMask());
    }

    @Test
    public void testFactoryAndInitialState() {
        ByteQuadsCanonicalizer randomizedRoot = ByteQuadsCanonicalizer.createRoot();
        Assert.assertNotNull(randomizedRoot);
        Assert.assertNotEquals(0, randomizedRoot.hashSeed());
        Assert.assertEquals(0, randomizedRoot.size());
        Assert.assertEquals(64, randomizedRoot.bucketCount());
        Assert.assertFalse(randomizedRoot.maybeDirty());

        Assert.assertEquals(12345, child.hashSeed());
        Assert.assertEquals(0, child.size());
        Assert.assertEquals(64, child.bucketCount());
        Assert.assertFalse(child.maybeDirty());
    }

    @Test
    public void testConstructorWithSizesViaReflection() throws Exception {
        Constructor<ByteQuadsCanonicalizer> ctor = ByteQuadsCanonicalizer.class.getDeclaredConstructor(
                int.class, boolean.class, int.class, boolean.class);
        ctor.setAccessible(true);

        // sz < MIN_HASH_SIZE (16) -> should be adjusted to 16
        ByteQuadsCanonicalizer sym1 = ctor.newInstance(8, true, 123, true);
        Assert.assertEquals(16, sym1.bucketCount());

        // sz not a power of 2 (e.g. 20 -> should pad to 32)
        ByteQuadsCanonicalizer sym2 = ctor.newInstance(20, true, 123, true);
        Assert.assertEquals(32, sym2.bucketCount());

        // sz is power of 2 (e.g. 64)
        ByteQuadsCanonicalizer sym3 = ctor.newInstance(64, true, 123, true);
        Assert.assertEquals(64, sym3.bucketCount());
    }

    @Test
    public void testAddAndFindSingleQuad() {
        Assert.assertNull(child.findName(100));

        String name = child.addName("a", 100);
        Assert.assertEquals("a", name);
        Assert.assertEquals(1, child.size());
        Assert.assertTrue(child.maybeDirty());

        Assert.assertEquals("a", child.findName(100));
        Assert.assertNull(child.findName(101));

        // Array overload with len 1
        String nameViaArray = child.findName(new int[]{100}, 1);
        Assert.assertEquals("a", nameViaArray);
    }

    @Test
    public void testAddAndFindTwoQuads() {
        Assert.assertNull(child.findName(100, 200));

        String name1 = child.addName("two", 100, 200);
        Assert.assertEquals("two", name1);
        Assert.assertEquals("two", child.findName(100, 200));
        Assert.assertEquals("two", child.findName(new int[]{100, 200}, 2));

        Assert.assertNull(child.findName(100, 201));
        Assert.assertNull(child.findName(101, 200));

        // q2 == 0 branch in addName(String, int, int)
        String nameZero = child.addName("zeroQ2", 300, 0);
        Assert.assertEquals("zeroQ2", nameZero);
    }

    @Test
    public void testAddAndFindThreeQuads() {
        Assert.assertNull(child.findName(100, 200, 300));

        String name = child.addName("three", 100, 200, 300);
        Assert.assertEquals("three", name);
        Assert.assertEquals("three", child.findName(100, 200, 300));
        Assert.assertEquals("three", child.findName(new int[]{100, 200, 300}, 3));

        Assert.assertNull(child.findName(100, 200, 301));
        Assert.assertNull(child.findName(100, 201, 300));
        Assert.assertNull(child.findName(101, 200, 300));
    }

    @Test
    public void testAddAndFindMultiQuads() {
        int[] q4 = new int[]{10, 20, 30, 40};
        int[] q5 = new int[]{10, 20, 30, 40, 50};
        int[] q6 = new int[]{10, 20, 30, 40, 50, 60};
        int[] q7 = new int[]{10, 20, 30, 40, 50, 60, 70};
        int[] q8 = new int[]{10, 20, 30, 40, 50, 60, 70, 80};
        int[] q9 = new int[]{10, 20, 30, 40, 50, 60, 70, 80, 90};

        child.addName("q4", q4, 4);
        child.addName("q5", q5, 5);
        child.addName("q6", q6, 6);
        child.addName("q7", q7, 7);
        child.addName("q8", q8, 8);
        child.addName("q9", q9, 9);

        Assert.assertEquals("q4", child.findName(q4, 4));
        Assert.assertEquals("q5", child.findName(q5, 5));
        Assert.assertEquals("q6", child.findName(q6, 6));
        Assert.assertEquals("q7", child.findName(q7, 7));
        Assert.assertEquals("q8", child.findName(q8, 8));
        Assert.assertEquals("q9", child.findName(q9, 9));

        // Mismatches in each position for fixed lengths (exercising _verifyLongName cases)
        int[] q4Bad = new int[]{10, 20, 30, 41};
        Assert.assertNull(child.findName(q4Bad, 4));

        int[] q5Bad = new int[]{10, 20, 30, 40, 51};
        Assert.assertNull(child.findName(q5Bad, 5));

        int[] q6Bad = new int[]{10, 20, 30, 40, 50, 61};
        Assert.assertNull(child.findName(q6Bad, 6));

        int[] q7Bad = new int[]{10, 20, 30, 40, 50, 60, 71};
        Assert.assertNull(child.findName(q7Bad, 7));

        int[] q8Bad = new int[]{10, 20, 30, 40, 50, 60, 70, 81};
        Assert.assertNull(child.findName(q8Bad, 8));

        int[] q9Bad = new int[]{10, 20, 30, 40, 50, 60, 70, 80, 91};
        Assert.assertNull(child.findName(q9Bad, 9));

        // Test addName with array of lengths 1, 2, 3
        child.addName("arr1", new int[]{111}, 1);
        child.addName("arr2", new int[]{222, 333}, 2);
        child.addName("arr3", new int[]{444, 555, 666}, 3);

        Assert.assertEquals("arr1", child.findName(111));
        Assert.assertEquals("arr2", child.findName(222, 333));
        Assert.assertEquals("arr3", child.findName(444, 555, 666));
    }

    @Test
    public void testCalcHashMultiQuadInvalid() {
        try {
            child.calcHash(new int[]{1, 2, 3}, 3);
            Assert.fail("Expected IllegalArgumentException for qlen < 4");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testCalcHashDistribution() {
        int h1 = child.calcHash(42);
        int h2 = child.calcHash(42, 84);
        int h3 = child.calcHash(42, 84, 126);
        int h4 = child.calcHash(new int[]{42, 84, 126, 168}, 4);
        int h5 = child.calcHash(new int[]{42, 84, 126, 168, 210}, 5);

        Assert.assertNotEquals(0, h1);
        Assert.assertNotEquals(0, h2);
        Assert.assertNotEquals(0, h3);
        Assert.assertNotEquals(0, h4);
        Assert.assertNotEquals(0, h5);
    }

    @Test
    public void testRehashAndGrowth() {
        int count = 100;
        for (int i = 0; i < count; i++) {
            child.addName("sym" + i, i * 7, i * 11);
        }
        Assert.assertEquals(count, child.size());
        Assert.assertTrue(child.bucketCount() > 64);

        for (int i = 0; i < count; i++) {
            Assert.assertEquals("sym" + i, child.findName(i * 7, i * 11));
        }

        // Add 1, 3, and 5 quad names and ensure rehash preserves them
        child.addName("singleQuad", 99999);
        child.addName("triQuad", 111, 222, 333);
        child.addName("fiveQuad", new int[]{1, 2, 3, 4, 5}, 5);

        for (int i = count; i < count + 200; i++) {
            child.addName("extra" + i, i * 13, i * 17);
        }

        Assert.assertEquals("singleQuad", child.findName(99999));
        Assert.assertEquals("triQuad", child.findName(111, 222, 333));
        Assert.assertEquals("fiveQuad", child.findName(new int[]{1, 2, 3, 4, 5}, 5));
    }

    @Test
    public void testParentChildMergeAndRelease() {
        child.addName("p1", 101);
        child.addName("p2", 102, 202);
        child.addName("p3", 103, 203, 303);

        Assert.assertTrue(child.maybeDirty());
        Assert.assertEquals(3, child.size());
        Assert.assertEquals(0, root.size());

        child.release();

        Assert.assertEquals(3, root.size());
        Assert.assertFalse(child.maybeDirty());

        // Creating second child after merge should retain symbols
        ByteQuadsCanonicalizer child2 = root.makeChild(JsonFactory.Feature.INTERN_FIELD_NAMES.getMask());
        Assert.assertEquals(3, child2.size());
        Assert.assertEquals("p1", child2.findName(101));
        Assert.assertEquals("p2", child2.findName(102, 202));
        Assert.assertEquals("p3", child2.findName(103, 203, 303));

        // Re-releasing clean child does nothing
        child2.release();
        Assert.assertEquals(3, root.size());
    }

    @Test
    public void testMergeChildExceedingMaxEntriesForReuse() throws Exception {
        // Child with > 6000 entries should reset parent to initial
        ByteQuadsCanonicalizer bigChild = root.makeChild(0);

        Field countField = ByteQuadsCanonicalizer.class.getDeclaredField("_count");
        countField.setAccessible(true);
        countField.setInt(bigChild, 6001);

        Field sharedField = ByteQuadsCanonicalizer.class.getDeclaredField("_hashShared");
        sharedField.setAccessible(true);
        sharedField.setBoolean(bigChild, false);

        bigChild.release();

        Assert.assertEquals(0, root.size());
        Assert.assertEquals(64, root.bucketCount());
    }

    @Test
    public void testMergeChildWithEqualCountDoesNotUpdate() throws Exception {
        Method mergeMethod = ByteQuadsCanonicalizer.class.getDeclaredMethod("mergeChild",
                Class.forName("com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer$TableInfo"));
        mergeMethod.setAccessible(true);

        Constructor<?> tiCtor = Class.forName("com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer$TableInfo")
                .getDeclaredConstructor(ByteQuadsCanonicalizer.class);
        tiCtor.setAccessible(true);
        Object tableInfo = tiCtor.newInstance(child); // count = 0

        mergeMethod.invoke(root, tableInfo);
        Assert.assertEquals(0, root.size());
    }

    @Test
    public void testCountsAndToString() {
        child.addName("n1", 10);
        child.addName("n2", 20);

        Assert.assertTrue(child.primaryCount() >= 0);
        Assert.assertTrue(child.secondaryCount() >= 0);
        Assert.assertTrue(child.tertiaryCount() >= 0);
        Assert.assertTrue(child.spilloverCount() >= 0);
        Assert.assertTrue(child.totalCount() >= 2);

        String str = child.toString();
        Assert.assertNotNull(str);
        Assert.assertTrue(str.contains("ByteQuadsCanonicalizer"));
        Assert.assertTrue(str.contains("size=2"));
    }

    @Test
    public void testCalcTertiaryShift() {
        Assert.assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(16));
        Assert.assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(64));
        Assert.assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(252));
        Assert.assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(256));
        Assert.assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(1024));
        Assert.assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(1028));
        Assert.assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(4096));
        Assert.assertEquals(7, ByteQuadsCanonicalizer._calcTertiaryShift(8192));
    }

    @Test
    public void testReportTooManyCollisions() throws Exception {
        Method method = ByteQuadsCanonicalizer.class.getDeclaredMethod("_reportTooManyCollisions");
        method.setAccessible(true);

        // Under 1024 slots: does not throw
        method.invoke(child);

        // Set hashSize to > 1024
        Field hashSizeField = ByteQuadsCanonicalizer.class.getDeclaredField("_hashSize");
        hashSizeField.setAccessible(true);
        hashSizeField.setInt(child, 2048);

        try {
            method.invoke(child);
            Assert.fail("Expected IllegalStateException when _hashSize > 1024");
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof IllegalStateException);
            Assert.assertTrue(e.getCause().getMessage().contains("suspect a DoS attack"));
        }
    }

    @Test
    public void testNonInterningMode() {
        ByteQuadsCanonicalizer noInternChild = root.makeChild(0);
        String name = new String("customName");
        String added = noInternChild.addName(name, 555);
        Assert.assertSame(name, added);

        String added2 = noInternChild.addName(new String("customName2"), 555, 666);
        Assert.assertEquals("customName2", added2);

        String added3 = noInternChild.addName(new String("customName3"), 555, 666, 777);
        Assert.assertEquals("customName3", added3);

        String addedMulti = noInternChild.addName(new String("customName4"), new int[]{1, 2, 3, 4}, 4);
        Assert.assertEquals("customName4", addedMulti);
    }

    @Test
    public void testLongNameExtensionBufferExpansion() {
        // Add a long name that triggers extension array resizing
        int[] quads = new int[100];
        for (int i = 0; i < quads.length; i++) {
            quads[i] = i + 1000;
        }

        child.addName("veryLong1", quads, 100);
        Assert.assertEquals("veryLong1", child.findName(quads, 100));

        // Modify quads slightly to ensure mismatch detection
        quads[99] = -1;
        Assert.assertNull(child.findName(quads, 100));
    }

    @Test
    public void testNukeSymbolsWhenMaxTableSizeExceeded() throws Exception {
        Method rehashMethod = ByteQuadsCanonicalizer.class.getDeclaredMethod("rehash");
        rehashMethod.setAccessible(true);

        Field hashSizeField = ByteQuadsCanonicalizer.class.getDeclaredField("_hashSize");
        hashSizeField.setAccessible(true);
        // MAX_T_SIZE is 0x10000 (65536). If hashSize is 0x10000, newSize will be 2 * 0x10000 > MAX_T_SIZE
        hashSizeField.setInt(child, 0x10000);

        child.addName("dummy", 1234);
        rehashMethod.invoke(child);

        Assert.assertEquals(0, child.size());
    }

    @Test
    public void testCollisionsInTertiaryAndSpillover() throws Exception {
        // Construct colliding entries manually to hit tertiary and spillover areas
        int seed = child.hashSeed();
        int baseHash = child.calcHash(10);

        // Find numbers that map to the exact same offset or tertiary bucket
        for (int i = 0; i < 50; i++) {
            child.addName("coll" + i, i * 64 + 1);
        }

        for (int i = 0; i < 50; i++) {
            Assert.assertEquals("coll" + i, child.findName(i * 64 + 1));
        }

        // 2-quad collisions
        for (int i = 0; i < 50; i++) {
            child.addName("coll2_" + i, i * 64 + 1, 999);
        }
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals("coll2_" + i, child.findName(i * 64 + 1, 999));
        }

        // 3-quad collisions
        for (int i = 0; i < 50; i++) {
            child.addName("coll3_" + i, i * 64 + 1, 999, 888);
        }
        for (int i = 0; i < 50; i++) {
            Assert.assertEquals("coll3_" + i, child.findName(i * 64 + 1, 999, 888));
        }

        // multi-quad collisions
        int[] q = new int[]{1, 2, 3, 4};
        for (int i = 0; i < 50; i++) {
            q[0] = i * 64 + 1;
            child.addName("coll4_" + i, Arrays.copyOf(q, 4), 4);
        }
        for (int i = 0; i < 50; i++) {
            q[0] = i * 64 + 1;
            Assert.assertEquals("coll4_" + i, child.findName(q, 4));
        }
    }
}
