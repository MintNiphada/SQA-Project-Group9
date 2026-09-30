package com.fasterxml.jackson.core.sym;

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ByteQuadsCanonicalizerTest {

    @Test
    public void testFactoryMethodsAndRootProperties() {
        ByteQuadsCanonicalizer root1 = ByteQuadsCanonicalizer.createRoot();
        Assert.assertNotNull(root1);
        Assert.assertNotEquals(0, root1.hashSeed());
        Assert.assertEquals(0, root1.size());
        Assert.assertEquals(64, root1.bucketCount());
        Assert.assertFalse(root1.maybeDirty());

        ByteQuadsCanonicalizer root2 = ByteQuadsCanonicalizer.createRoot(12345);
        Assert.assertEquals(12345, root2.hashSeed());
        Assert.assertEquals(0, root2.size());
        Assert.assertEquals(64, root2.bucketCount());
        Assert.assertEquals(0, root2.primaryCount());
        Assert.assertEquals(0, root2.secondaryCount());
        Assert.assertEquals(0, root2.tertiaryCount());
        Assert.assertEquals(0, root2.spilloverCount());
        Assert.assertEquals(0, root2.totalCount());

        String str = root2.toString();
        Assert.assertTrue(str.contains("ByteQuadsCanonicalizer"));
        Assert.assertTrue(str.contains("size=0"));
    }

    @Test
    public void testPrivateConstructorSizingViaReflection() throws Exception {
        Constructor<ByteQuadsCanonicalizer> ctor = ByteQuadsCanonicalizer.class.getDeclaredConstructor(
                int.class, boolean.class, int.class, boolean.class);
        ctor.setAccessible(true);

        // Size below MIN_HASH_SIZE (16) should be adjusted to 16
        ByteQuadsCanonicalizer sym1 = ctor.newInstance(8, true, 123, true);
        Assert.assertEquals(16, sym1.bucketCount());

        // Non-power-of-2 size (e.g., 20) should round up to next power of 2 (32)
        ByteQuadsCanonicalizer sym2 = ctor.newInstance(20, true, 123, true);
        Assert.assertEquals(32, sym2.bucketCount());

        // Exact power of 2 (e.g., 32)
        ByteQuadsCanonicalizer sym3 = ctor.newInstance(32, true, 123, true);
        Assert.assertEquals(32, sym3.bucketCount());
    }

    @Test
    public void testTertiaryShiftCalculation() {
        Assert.assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(16));
        Assert.assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(64));
        Assert.assertEquals(4, ByteQuadsCanonicalizer._calcTertiaryShift(255));
        Assert.assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(256));
        Assert.assertEquals(5, ByteQuadsCanonicalizer._calcTertiaryShift(1024));
        Assert.assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(1025));
        Assert.assertEquals(6, ByteQuadsCanonicalizer._calcTertiaryShift(4096));
        Assert.assertEquals(7, ByteQuadsCanonicalizer._calcTertiaryShift(4097));
        Assert.assertEquals(7, ByteQuadsCanonicalizer._calcTertiaryShift(65536));
    }

    @Test
    public void testAddAndFindSingleQuad() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(100);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.INTERN_FIELD_NAMES.getMask());

        Assert.assertNull(child.findName(0x1111));

        String added = child.addName("a", 0x1111);
        Assert.assertEquals("a", added);
        Assert.assertEquals(1, child.size());
        Assert.assertTrue(child.maybeDirty());

        String found = child.findName(0x1111);
        Assert.assertEquals("a", found);

        // Miss check
        Assert.assertNull(child.findName(0x2222));
    }

    @Test
    public void testAddAndFindTwoQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(200);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        Assert.assertNull(child.findName(0x11, 0x22));

        String added = child.addName("ab", 0x11, 0x22);
        Assert.assertEquals("ab", added);
        Assert.assertEquals(1, child.size());

        String found = child.findName(0x11, 0x22);
        Assert.assertEquals("ab", found);

        // Matching first, different second
        Assert.assertNull(child.findName(0x11, 0x33));
        // Different first
        Assert.assertNull(child.findName(0x33, 0x22));

        // Adding with q2 = 0 should work and use single-quad hash internally
        child.addName("a0", 0x44, 0);
        Assert.assertEquals("a0", child.findName(0x44, 0));
    }

    @Test
    public void testAddAndFindThreeQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(300);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        Assert.assertNull(child.findName(0x11, 0x22, 0x33));

        String added = child.addName("abc", 0x11, 0x22, 0x33);
        Assert.assertEquals("abc", added);
        Assert.assertEquals(1, child.size());

        String found = child.findName(0x11, 0x22, 0x33);
        Assert.assertEquals("abc", found);

        Assert.assertNull(child.findName(0x11, 0x22, 0x44));
        Assert.assertNull(child.findName(0x11, 0x44, 0x33));
        Assert.assertNull(child.findName(0x44, 0x22, 0x33));
    }

    @Test
    public void testAddAndFindArrayQuads() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(400);
        ByteQuadsCanonicalizer child = root.makeChild(JsonFactory.Feature.INTERN_FIELD_NAMES.getMask());

        // qlen 1
        child.addName("q1", new int[]{0x10}, 1);
        Assert.assertEquals("q1", child.findName(0x10));
        Assert.assertEquals("q1", child.findName(new int[]{0x10}, 1));

        // qlen 2
        child.addName("q2", new int[]{0x10, 0x20}, 2);
        Assert.assertEquals("q2", child.findName(0x10, 0x20));
        Assert.assertEquals("q2", child.findName(new int[]{0x10, 0x20}, 2));

        // qlen 3
        child.addName("q3", new int[]{0x10, 0x20, 0x30}, 3);
        Assert.assertEquals("q3", child.findName(0x10, 0x20, 0x30));
        Assert.assertEquals("q3", child.findName(new int[]{0x10, 0x20, 0x30}, 3));

        // qlen 4
        int[] q4 = new int[]{1, 2, 3, 4};
        child.addName("q4", q4, 4);
        Assert.assertEquals("q4", child.findName(q4, 4));

        // qlen 5
        int[] q5 = new int[]{1, 2, 3, 4, 5};
        child.addName("q5", q5, 5);
        Assert.assertEquals("q5", child.findName(q5, 5));

        // qlen 6
        int[] q6 = new int[]{1, 2, 3, 4, 5, 6};
        child.addName("q6", q6, 6);
        Assert.assertEquals("q6", child.findName(q6, 6));

        // qlen 7
        int[] q7 = new int[]{1, 2, 3, 4, 5, 6, 7};
        child.addName("q7", q7, 7);
        Assert.assertEquals("q7", child.findName(q7, 7));

        // qlen 8
        int[] q8 = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        child.addName("q8", q8, 8);
        Assert.assertEquals("q8", child.findName(q8, 8));

        // qlen 9 (> 8, exercises _verifyLongName2)
        int[] q9 = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        child.addName("q9", q9, 9);
        Assert.assertEquals("q9", child.findName(q9, 9));

        // Mismatches in long quads
        int[] q9Mismatch = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 99};
        Assert.assertNull(child.findName(q9Mismatch, 9));

        int[] q8Mismatch = new int[]{1, 2, 3, 4, 5, 6, 7, 99};
        Assert.assertNull(child.findName(q8Mismatch, 8));

        int[] q4Mismatch = new int[]{1, 2, 3, 99};
        Assert.assertNull(child.findName(q4Mismatch, 4));
    }

    @Test
    public void testCalcHashVariants() {
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(12345);

        int h1 = sym.calcHash(0x12345678);
        Assert.assertNotEquals(0, h1);

        int h2 = sym.calcHash(0x12345678, 0x87654321);
        Assert.assertNotEquals(0, h2);

        int h3 = sym.calcHash(0x12345678, 0x87654321, 0x11223344);
        Assert.assertNotEquals(0, h3);

        int h4 = sym.calcHash(new int[]{1, 2, 3, 4}, 4);
        Assert.assertNotEquals(0, h4);

        int h5 = sym.calcHash(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 10);
        Assert.assertNotEquals(0, h5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcHashArrayTooShort() {
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(12345);
        sym.calcHash(new int[]{1, 2, 3}, 3);
    }

    @Test
    public void testChildReleaseAndMerge() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(500);
        ByteQuadsCanonicalizer child1 = root.makeChild(JsonFactory.Feature.INTERN_FIELD_NAMES.getMask());

        child1.addName("symbol1", 100);
        child1.addName("symbol2", 200);
        Assert.assertEquals(2, child1.size());
        Assert.assertEquals(0, root.size());

        child1.release();
        // Child's release merges into root
        Assert.assertEquals(2, root.size());

        // Second release without modification should be no-op
        child1.release();
        Assert.assertEquals(2, root.size());

        // Second child inherits root's symbols
        ByteQuadsCanonicalizer child2 = root.makeChild(JsonFactory.Feature.INTERN_FIELD_NAMES.getMask());
        Assert.assertEquals(2, child2.size());
        Assert.assertEquals("symbol1", child2.findName(100));
        Assert.assertEquals("symbol2", child2.findName(200));

        // Releasing untouched child does nothing
        child2.release();
        Assert.assertEquals(2, root.size());
    }

    @Test
    public void testChildMergeExceedingMaxEntriesForReuse() throws Exception {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(600);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        // Force child count to exceed MAX_ENTRIES_FOR_REUSE (6000)
        Field countField = ByteQuadsCanonicalizer.class.getDeclaredField("_count");
        countField.setAccessible(true);
        countField.setInt(child, 6001);

        Field hashSharedField = ByteQuadsCanonicalizer.class.getDeclaredField("_hashShared");
        hashSharedField.setAccessible(true);
        hashSharedField.setBoolean(child, false);

        child.release();

        // Root should have been purged/reset to empty initial table
        Assert.assertEquals(0, root.size());
    }

    @Test
    public void testCollisionsCascadingLookups() {
        // Create a root with small hash size to easily induce primary, secondary, tertiary, and spillover entries
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(777);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        // Insert many entries to fill primary and force cascading into secondary, tertiary, spillover
        for (int i = 1; i <= 30; i++) {
            child.addName("name_1q_" + i, i * 1000);
            child.addName("name_2q_" + i, i * 1000, i * 2000);
            child.addName("name_3q_" + i, i * 1000, i * 2000, i * 3000);
            child.addName("name_4q_" + i, new int[]{i * 10, i * 20, i * 30, i * 40}, 4);
            child.addName("name_long_" + i, new int[]{i, i + 1, i + 2, i + 3, i + 4, i + 5, i + 6, i + 7, i + 8}, 9);
        }

        Assert.assertEquals(150, child.size());
        Assert.assertTrue(child.primaryCount() > 0);
        Assert.assertTrue(child.secondaryCount() >= 0);
        Assert.assertTrue(child.tertiaryCount() >= 0);
        Assert.assertTrue(child.totalCount() >= child.size());

        // Verify retrieval of all added symbols across various paths
        for (int i = 1; i <= 30; i++) {
            Assert.assertEquals("name_1q_" + i, child.findName(i * 1000));
            Assert.assertEquals("name_2q_" + i, child.findName(i * 1000, i * 2000));
            Assert.assertEquals("name_3q_" + i, child.findName(i * 1000, i * 2000, i * 3000));
            Assert.assertEquals("name_4q_" + i, child.findName(new int[]{i * 10, i * 20, i * 30, i * 40}, 4));
            Assert.assertEquals("name_long_" + i, child.findName(new int[]{i, i + 1, i + 2, i + 3, i + 4, i + 5, i + 6, i + 7, i + 8}, 9));
        }

        // Verify find miss on collisions
        Assert.assertNull(child.findName(999999));
        Assert.assertNull(child.findName(999999, 999999));
        Assert.assertNull(child.findName(999999, 999999, 999999));
        Assert.assertNull(child.findName(new int[]{999999, 999998, 999997, 999996}, 4));
    }

    @Test
    public void testRehashWithAllQuadLengths() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(111);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        // Add 1, 2, 3, 4, 5, 6, 7, 8, 9 quads
        child.addName("len1", 1);
        child.addName("len2", 1, 2);
        child.addName("len3", 1, 2, 3);
        child.addName("len4", new int[]{1, 2, 3, 4}, 4);
        child.addName("len5", new int[]{1, 2, 3, 4, 5}, 5);
        child.addName("len6", new int[]{1, 2, 3, 4, 5, 6}, 6);
        child.addName("len7", new int[]{1, 2, 3, 4, 5, 6, 7}, 7);
        child.addName("len8", new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 8);
        child.addName("len9", new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, 9);

        // Add enough entries to trigger rehash
        for (int i = 100; i < 200; i++) {
            child.addName("fill_" + i, i);
        }

        Assert.assertTrue(child.bucketCount() > 64);
        Assert.assertEquals("len1", child.findName(1));
        Assert.assertEquals("len2", child.findName(1, 2));
        Assert.assertEquals("len3", child.findName(1, 2, 3));
        Assert.assertEquals("len4", child.findName(new int[]{1, 2, 3, 4}, 4));
        Assert.assertEquals("len5", child.findName(new int[]{1, 2, 3, 4, 5}, 5));
        Assert.assertEquals("len6", child.findName(new int[]{1, 2, 3, 4, 5, 6}, 6));
        Assert.assertEquals("len7", child.findName(new int[]{1, 2, 3, 4, 5, 6, 7}, 7));
        Assert.assertEquals("len8", child.findName(new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 8));
        Assert.assertEquals("len9", child.findName(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, 9));
    }

    @Test
    public void testRehashNukesWhenExceedingMaxSize() throws Exception {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(999);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        // Set hashSize to MAX_T_SIZE (65536)
        Field hashSizeField = ByteQuadsCanonicalizer.class.getDeclaredField("_hashSize");
        hashSizeField.setAccessible(true);
        hashSizeField.setInt(child, 0x10000);

        // Invoke rehash via reflection
        Method rehashMethod = ByteQuadsCanonicalizer.class.getDeclaredMethod("rehash");
        rehashMethod.setAccessible(true);
        rehashMethod.invoke(child);

        // After nuking, size should be 0
        Assert.assertEquals(0, child.size());
    }

    @Test
    public void testSpilloverOverflowWithFailOnDoS() throws Exception {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(888);
        ByteQuadsCanonicalizer child = root.makeChild(
                JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW.getMask()
        );

        // Make hashSize > 1024 so _reportTooManyCollisions will throw
        Field hashSizeField = ByteQuadsCanonicalizer.class.getDeclaredField("_hashSize");
        hashSizeField.setAccessible(true);
        hashSizeField.setInt(child, 2048);

        // Place spilloverEnd right at (hashSize << 3)
        Field spilloverEndField = ByteQuadsCanonicalizer.class.getDeclaredField("_spilloverEnd");
        spilloverEndField.setAccessible(true);
        spilloverEndField.setInt(child, 2048 << 3);

        Field hashAreaField = ByteQuadsCanonicalizer.class.getDeclaredField("_hashArea");
        hashAreaField.setAccessible(true);
        hashAreaField.set(child, new int[(2048 << 3) + 100]);

        Field namesField = ByteQuadsCanonicalizer.class.getDeclaredField("_names");
        namesField.setAccessible(true);
        namesField.set(child, new String[2048 << 1]);

        try {
            Method reportMethod = ByteQuadsCanonicalizer.class.getDeclaredMethod("_reportTooManyCollisions");
            reportMethod.setAccessible(true);
            reportMethod.invoke(child);
            Assert.fail("Expected IllegalStateException due to collision DoS");
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            Assert.assertTrue(cause instanceof IllegalStateException);
            Assert.assertTrue(cause.getMessage().contains("suspect a DoS attack"));
        }
    }

    @Test
    public void testAppendLongNameBufferGrowth() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(333);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        // Add multiple distinct long names to force repeated extension of _hashArea
        for (int i = 0; i < 200; i++) {
            int[] quads = new int[20];
            for (int k = 0; k < quads.length; k++) {
                quads[k] = i * 100 + k;
            }
            child.addName("long_name_" + i, quads, quads.length);
        }

        Assert.assertEquals(200, child.size());
        for (int i = 0; i < 200; i++) {
            int[] quads = new int[20];
            for (int k = 0; k < quads.length; k++) {
                quads[k] = i * 100 + k;
            }
            Assert.assertEquals("long_name_" + i, child.findName(quads, quads.length));
        }
    }

    @Test
    public void testVerifyLongNameEdgeCases() {
        ByteQuadsCanonicalizer root = ByteQuadsCanonicalizer.createRoot(444);
        ByteQuadsCanonicalizer child = root.makeChild(0);

        int[] q4_a = new int[]{10, 20, 30, 40};
        int[] q4_b = new int[]{10, 20, 30, 41};
        child.addName("q4_a", q4_a, 4);
        Assert.assertNull(child.findName(q4_b, 4));

        int[] q5_a = new int[]{10, 20, 30, 40, 50};
        int[] q5_b = new int[]{10, 20, 30, 40, 51};
        child.addName("q5_a", q5_a, 5);
        Assert.assertNull(child.findName(q5_b, 5));

        int[] q6_a = new int[]{10, 20, 30, 40, 50, 60};
        int[] q6_b = new int[]{10, 20, 30, 40, 50, 61};
        child.addName("q6_a", q6_a, 6);
        Assert.assertNull(child.findName(q6_b, 6));

        int[] q7_a = new int[]{10, 20, 30, 40, 50, 60, 70};
        int[] q7_b = new int[]{10, 20, 30, 40, 50, 60, 71};
        child.addName("q7_a", q7_a, 7);
        Assert.assertNull(child.findName(q7_b, 7));

        int[] q8_a = new int[]{10, 20, 30, 40, 50, 60, 70, 80};
        int[] q8_b = new int[]{10, 20, 30, 40, 50, 60, 70, 81};
        child.addName("q8_a", q8_a, 8);
        Assert.assertNull(child.findName(q8_b, 8));
    }
}
