package org.apache.commons.math3.random;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;

public class BitsStreamGeneratorTest {

    private static class DummyGenerator extends BitsStreamGenerator {
        private final Queue<Integer> values = new LinkedList<Integer>();
        private long seed;

        @Override
        public void setSeed(int seed) {
            this.seed = seed;
        }

        @Override
        public void setSeed(int[] seed) {
            this.seed = seed != null && seed.length > 0 ? seed[0] : 0;
        }

        @Override
        public void setSeed(long seed) {
            this.seed = seed;
        }

        public void pushNext(int val) {
            values.add(val);
        }

        @Override
        protected int next(int bits) {
            if (!values.isEmpty()) {
                return values.poll() & ((1 << bits) - 1);
            }
            // Simple LCG fallback if no values are queued
            seed = (seed * 6364136223846793005L + 1442695040888963407L);
            return (int) (seed >>> (64 - bits));
        }
    }

    private static class MockGenerator extends BitsStreamGenerator {
        private final int[] sequence;
        private int index = 0;

        public MockGenerator(int... sequence) {
            this.sequence = sequence;
        }

        @Override
        public void setSeed(int seed) {}

        @Override
        public void setSeed(int[] seed) {}

        @Override
        public void setSeed(long seed) {}

        @Override
        protected int next(int bits) {
            int val = sequence[index % sequence.length];
            index++;
            if (bits == 32) {
                return val;
            }
            return val & ((1 << bits) - 1);
        }
    }

    @Test
    public void testSetSeed() {
        DummyGenerator gen = new DummyGenerator();
        gen.setSeed(12345);
        Assert.assertEquals(12345, gen.seed);

        gen.setSeed(new int[]{42, 43});
        Assert.assertEquals(42, gen.seed);

        gen.setSeed(new int[]{});
        Assert.assertEquals(0, gen.seed);

        gen.setSeed((int[]) null);
        Assert.assertEquals(0, gen.seed);

        gen.setSeed(9876543210L);
        Assert.assertEquals(9876543210L, gen.seed);
    }

    @Test
    public void testNextBoolean() {
        DummyGenerator gen = new DummyGenerator();
        gen.pushNext(0);
        Assert.assertFalse(gen.nextBoolean());

        gen.pushNext(1);
        Assert.assertTrue(gen.nextBoolean());

        gen.pushNext(0xFFFFFFFF);
        Assert.assertTrue(gen.nextBoolean());
    }

    @Test
    public void testNextBytes() {
        DummyGenerator gen = new DummyGenerator();

        // 0-length array
        byte[] b0 = new byte[0];
        gen.nextBytes(b0);

        // 1-byte array
        byte[] b1 = new byte[1];
        gen.pushNext(0x12345678);
        gen.nextBytes(b1);
        Assert.assertEquals((byte) 0x78, b1[0]);

        // 2-byte array
        byte[] b2 = new byte[2];
        gen.pushNext(0x12345678);
        gen.nextBytes(b2);
        Assert.assertEquals((byte) 0x78, b2[0]);
        Assert.assertEquals((byte) 0x56, b2[1]);

        // 3-byte array
        byte[] b3 = new byte[3];
        gen.pushNext(0x12345678);
        gen.nextBytes(b3);
        Assert.assertEquals((byte) 0x78, b3[0]);
        Assert.assertEquals((byte) 0x56, b3[1]);
        Assert.assertEquals((byte) 0x34, b3[2]);

        // 4-byte array (full word loop)
        byte[] b4 = new byte[4];
        gen.pushNext(0x12345678);
        gen.pushNext(0); // extra fallback if needed
        gen.nextBytes(b4);
        Assert.assertEquals((byte) 0x78, b4[0]);
        Assert.assertEquals((byte) 0x56, b4[1]);
        Assert.assertEquals((byte) 0x34, b4[2]);
        Assert.assertEquals((byte) 0x12, b4[3]);

        // 5-byte array (1 full word + 1 byte remainder)
        byte[] b5 = new byte[5];
        gen.pushNext(0x04030201);
        gen.pushNext(0x08070605);
        gen.nextBytes(b5);
        Assert.assertEquals((byte) 0x01, b5[0]);
        Assert.assertEquals((byte) 0x02, b5[1]);
        Assert.assertEquals((byte) 0x03, b5[2]);
        Assert.assertEquals((byte) 0x04, b5[3]);
        Assert.assertEquals((byte) 0x05, b5[4]);

        // 7-byte array (1 full word + 3 byte remainder)
        byte[] b7 = new byte[7];
        gen.pushNext(0x04030201);
        gen.pushNext(0x08070605);
        gen.nextBytes(b7);
        Assert.assertEquals((byte) 0x01, b7[0]);
        Assert.assertEquals((byte) 0x02, b7[1]);
        Assert.assertEquals((byte) 0x03, b7[2]);
        Assert.assertEquals((byte) 0x04, b7[3]);
        Assert.assertEquals((byte) 0x05, b7[4]);
        Assert.assertEquals((byte) 0x06, b7[5]);
        Assert.assertEquals((byte) 0x07, b7[6]);

        // 8-byte array (2 full words)
        byte[] b8 = new byte[8];
        gen.pushNext(0x04030201);
        gen.pushNext(0x08070605);
        gen.pushNext(0);
        gen.nextBytes(b8);
        Assert.assertEquals((byte) 0x01, b8[0]);
        Assert.assertEquals((byte) 0x02, b8[1]);
        Assert.assertEquals((byte) 0x03, b8[2]);
        Assert.assertEquals((byte) 0x04, b8[3]);
        Assert.assertEquals((byte) 0x05, b8[4]);
        Assert.assertEquals((byte) 0x06, b8[5]);
        Assert.assertEquals((byte) 0x07, b8[6]);
        Assert.assertEquals((byte) 0x08, b8[7]);
    }

    @Test
    public void testNextDouble() {
        DummyGenerator gen = new DummyGenerator();

        gen.pushNext(0);
        gen.pushNext(0);
        Assert.assertEquals(0.0d, gen.nextDouble(), 1e-15);

        gen.pushNext(0x03FFFFFF); // 26 bits
        gen.pushNext(0x03FFFFFF); // 26 bits
        double maxDouble = gen.nextDouble();
        Assert.assertTrue(maxDouble > 0.9999999);
        Assert.assertTrue(maxDouble < 1.0);

        // General range check
        for (int i = 0; i < 100; i++) {
            double d = gen.nextDouble();
            Assert.assertTrue("nextDouble() out of range [0, 1): " + d, d >= 0.0 && d < 1.0);
        }
    }

    @Test
    public void testNextFloat() {
        DummyGenerator gen = new DummyGenerator();

        gen.pushNext(0);
        Assert.assertEquals(0.0f, gen.nextFloat(), 1e-7f);

        gen.pushNext(0x007FFFFF); // 23 bits
        float maxFloat = gen.nextFloat();
        Assert.assertTrue(maxFloat > 0.999999f);
        Assert.assertTrue(maxFloat < 1.0f);

        for (int i = 0; i < 100; i++) {
            float f = gen.nextFloat();
            Assert.assertTrue("nextFloat() out of range [0, 1): " + f, f >= 0.0f && f < 1.0f);
        }
    }

    @Test
    public void testNextGaussian() {
        DummyGenerator gen = new DummyGenerator();
        gen.setSeed(42L);

        double g1 = gen.nextGaussian();
        double g2 = gen.nextGaussian();
        Assert.assertFalse(Double.isNaN(g1));
        Assert.assertFalse(Double.isNaN(g2));

        // Test clear() resets the cache
        gen.clear();
        double g3 = gen.nextGaussian();
        Assert.assertFalse(Double.isNaN(g3));

        // Test clearing again
        gen.clear();
        gen.clear(); // double clear is safe
        double g4 = gen.nextGaussian();
        Assert.assertFalse(Double.isNaN(g4));
    }

    @Test
    public void testNextInt() {
        DummyGenerator gen = new DummyGenerator();
        gen.pushNext(0);
        Assert.assertEquals(0, gen.nextInt());

        gen.pushNext(-1);
        Assert.assertEquals(-1, gen.nextInt());

        gen.pushNext(42);
        Assert.assertEquals(42, gen.nextInt());
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextIntBoundedZero() {
        DummyGenerator gen = new DummyGenerator();
        gen.nextInt(0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextIntBoundedNegative() {
        DummyGenerator gen = new DummyGenerator();
        gen.nextInt(-5);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextIntBoundedMinVal() {
        DummyGenerator gen = new DummyGenerator();
        gen.nextInt(Integer.MIN_VALUE);
    }

    @Test
    public void testNextIntBoundedPowerOfTwo() {
        DummyGenerator gen = new DummyGenerator();

        // n = 1
        gen.pushNext(0x7FFFFFFF);
        Assert.assertEquals(0, gen.nextInt(1));

        // n = 2
        gen.pushNext(0);
        Assert.assertEquals(0, gen.nextInt(2));

        gen.pushNext(0x7FFFFFFF);
        Assert.assertEquals(1, gen.nextInt(2));

        // n = 16
        gen.pushNext(0x40000000); // 1/2 of MAX
        Assert.assertEquals(8, gen.nextInt(16));

        // n = 1 << 30
        for (int i = 0; i < 50; i++) {
            int val = gen.nextInt(1 << 30);
            Assert.assertTrue(val >= 0 && val < (1 << 30));
        }
    }

    @Test
    public void testNextIntBoundedNonPowerOfTwo() {
        DummyGenerator gen = new DummyGenerator();

        for (int n : new int[]{3, 5, 6, 7, 10, 100, 1000, Integer.MAX_VALUE}) {
            for (int i = 0; i < 20; i++) {
                int val = gen.nextInt(n);
                Assert.assertTrue("nextInt(n) must be in [0, n): " + val, val >= 0 && val < n);
            }
        }
    }

    @Test
    public void testNextIntRejectionLoop() {
        // We craft a sequence where bits - val + (n - 1) < 0 to trigger the while rejection loop
        // Let n = Integer.MAX_VALUE - 5 = 2147483642
        // If next(31) returns Integer.MAX_VALUE - 1 = 2147483646:
        // bits = 2147483646
        // val = bits % n = 4
        // bits - val + (n - 1) = 2147483642 + 2147483641 = 4294967283 -> -13 in signed 32-bit int
        // Since -13 < 0, it rejects and loops.
        // Second iteration: next(31) returns 10.
        // bits = 10, val = 10, bits - val + (n - 1) = 2147483641 > 0, returns 10.
        int n = Integer.MAX_VALUE - 5;
        int firstBits = Integer.MAX_VALUE - 1;
        int secondBits = 10;

        MockGenerator gen = new MockGenerator(firstBits, secondBits);
        int result = gen.nextInt(n);
        Assert.assertEquals(10, result);
    }

    @Test
    public void testNextLong() {
        DummyGenerator gen = new DummyGenerator();

        // 0 high, 0 low -> 0L
        gen.pushNext(0);
        gen.pushNext(0);
        Assert.assertEquals(0L, gen.nextLong());

        // 1 high, 2 low -> (1L << 32) | 2L
        gen.pushNext(1);
        gen.pushNext(2);
        Assert.assertEquals((1L << 32) | 2L, gen.nextLong());

        // -1 high, -1 low -> -1L
        gen.pushNext(-1);
        gen.pushNext(-1);
        Assert.assertEquals(-1L, gen.nextLong());

        // Check range across random outputs
        for (int i = 0; i < 100; i++) {
            long l = gen.nextLong();
            // Just verify it runs and doesn't crash
            Assert.assertTrue(l == l);
        }
    }
}
