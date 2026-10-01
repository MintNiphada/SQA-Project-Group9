package org.apache.commons.math3.random;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

public class BitsStreamGeneratorTest {

    // Concrete implementation for testing abstract class
    private static class TestBitsStreamGenerator extends BitsStreamGenerator {
        private int[] nextValues;
        private int callCount = 0;
        private int seedInt;
        private int[] seedIntArray;
        private long seedLong;

        public TestBitsStreamGenerator(int[] nextValues) {
            super();
            this.nextValues = nextValues;
        }

        @Override
        public void setSeed(int seed) {
            this.seedInt = seed;
        }

        @Override
        public void setSeed(int[] seed) {
            this.seedIntArray = seed;
        }

        @Override
        public void setSeed(long seed) {
            this.seedLong = seed;
        }

        @Override
        protected int next(int bits) {
            if (callCount < nextValues.length) {
                return nextValues[callCount++];
            }
            return 0;
        }
    }

    @Test
    public void testConstructor() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{});
        // nextGaussian should be NaN after construction
        // We can test indirectly via nextGaussian behavior
    }

    @Test
    public void testNextBooleanTrue() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{1});
        assertTrue(gen.nextBoolean());
    }

    @Test
    public void testNextBooleanFalse() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{0});
        assertFalse(gen.nextBoolean());
    }

    @Test
    public void testNextBytesFullBlocks() {
        // 4 bytes exactly
        int randomValue = 0x01020304;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{randomValue});
        byte[] bytes = new byte[4];
        gen.nextBytes(bytes);
        assertEquals((byte) 0x04, bytes[0]);
        assertEquals((byte) 0x03, bytes[1]);
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x01, bytes[3]);
    }

    @Test
    public void testNextBytesMoreThanOneBlock() {
        // 5 bytes, needs two next(32) calls
        int random1 = 0x01020304;
        int random2 = 0x05060708;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{random1, random2});
        byte[] bytes = new byte[5];
        gen.nextBytes(bytes);
        assertEquals((byte) 0x04, bytes[0]);
        assertEquals((byte) 0x03, bytes[1]);
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x01, bytes[3]);
        assertEquals((byte) 0x08, bytes[4]);
    }

    @Test
    public void testNextBytesEmptyArray() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{});
        byte[] bytes = new byte[0];
        gen.nextBytes(bytes);
        assertEquals(0, bytes.length);
    }

    @Test
    public void testNextBytesLessThanBlock() {
        int randomValue = 0x01020304;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{randomValue});
        byte[] bytes = new byte[2];
        gen.nextBytes(bytes);
        assertEquals((byte) 0x04, bytes[0]);
        assertEquals((byte) 0x03, bytes[1]);
    }

    @Test
    public void testNextBytesExactMultipleOfBlock() {
        int random1 = 0x01020304;
        int random2 = 0x05060708;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{random1, random2});
        byte[] bytes = new byte[8];
        gen.nextBytes(bytes);
        assertEquals((byte) 0x04, bytes[0]);
        assertEquals((byte) 0x03, bytes[1]);
        assertEquals((byte) 0x02, bytes[2]);
        assertEquals((byte) 0x01, bytes[3]);
        assertEquals((byte) 0x08, bytes[4]);
        assertEquals((byte) 0x07, bytes[5]);
        assertEquals((byte) 0x06, bytes[6]);
        assertEquals((byte) 0x05, bytes[7]);
    }

    @Test
    public void testNextDouble() {
        // high = next(26) << 26, low = next(26)
        // high = 1 << 26 = 67108864
        // low = 0
        // result = 67108864 * 0x1.0p-52d = 67108864 / 2^52
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{1, 0});
        double result = gen.nextDouble();
        double expected = ((long) 1 << 26) * 0x1.0p-52d;
        assertEquals(expected, result, 0.0);
    }

    @Test
    public void testNextDoubleMax() {
        // high = (2^26 - 1) << 26, low = 2^26 - 1
        int max26 = (1 << 26) - 1;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{max26, max26});
        double result = gen.nextDouble();
        long high = ((long) max26) << 26;
        long low = max26;
        double expected = (high | low) * 0x1.0p-52d;
        assertEquals(expected, result, 0.0);
    }

    @Test
    public void testNextDoubleZero() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{0, 0});
        double result = gen.nextDouble();
        assertEquals(0.0, result, 0.0);
    }

    @Test
    public void testNextFloat() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{1});
        float result = gen.nextFloat();
        float expected = 1 * 0x1.0p-23f;
        assertEquals(expected, result, 0.0f);
    }

    @Test
    public void testNextFloatZero() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{0});
        float result = gen.nextFloat();
        assertEquals(0.0f, result, 0.0f);
    }

    @Test
    public void testNextFloatMax() {
        int max23 = (1 << 23) - 1;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{max23});
        float result = gen.nextFloat();
        float expected = max23 * 0x1.0p-23f;
        assertEquals(expected, result, 0.0f);
    }

    @Test
    public void testNextGaussianPair() {
        // nextDouble calls: x and y
        // x: high=1<<26, low=0 => x = 67108864 / 2^52
        // y: high=0, low=0 => y = 0
        // But log(0) is -Infinity, sqrt(-2 * -Infinity) = Infinity
        // So we need y > 0
        // Let's use x = 0.5, y = 0.5
        // x = 0.5 => high = 0x80000 (2^19), low = 0? Actually 0.5 * 2^52 = 2^51 = high<<26 | low
        // high = 2^25, low = 0
        int highX = 1 << 25;
        int lowX = 0;
        int highY = 1 << 25;
        int lowY = 0;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{highX, lowX, highY, lowY});
        double g1 = gen.nextGaussian();
        double g2 = gen.nextGaussian();
        // Verify they are not NaN and not equal (generally)
        assertFalse(Double.isNaN(g1));
        assertFalse(Double.isNaN(g2));
        // The second call should return the cached value
        // But we need to check the caching mechanism
    }

    @Test
    public void testNextGaussianCaching() {
        // First call generates pair, second returns cached
        int highX = 1 << 25;
        int lowX = 0;
        int highY = 1 << 25;
        int lowY = 0;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{highX, lowX, highY, lowY, 0, 0});
        double first = gen.nextGaussian();
        double second = gen.nextGaussian();
        // The two values should be different (cos vs sin)
        // And the second should not require more next() calls if cached
        // But our mock will just return 0 for extra calls
        assertFalse(Double.isNaN(first));
        assertFalse(Double.isNaN(second));
    }

    @Test
    public void testNextGaussianAfterClear() {
        int highX = 1 << 25;
        int lowX = 0;
        int highY = 1 << 25;
        int lowY = 0;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{highX, lowX, highY, lowY, highX, lowX, highY, lowY});
        double first = gen.nextGaussian();
        gen.clear();
        double second = gen.nextGaussian();
        // After clear, should generate new pair
        assertFalse(Double.isNaN(first));
        assertFalse(Double.isNaN(second));
    }

    @Test
    public void testNextInt() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{12345});
        assertEquals(12345, gen.nextInt());
    }

    @Test
    public void testNextIntNPowerOfTwo() {
        // n = 8 (power of 2)
        // next(31) returns 0x40000000 (2^30)
        // (8 * 2^30) >> 31 = (2^33) >> 31 = 4
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{0x40000000});
        assertEquals(4, gen.nextInt(8));
    }

    @Test
    public void testNextIntNPowerOfTwoZero() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{0});
        assertEquals(0, gen.nextInt(8));
    }

    @Test
    public void testNextIntNNotPowerOfTwo() {
        // n = 7
        // bits = 14, val = 14 % 7 = 0, bits - val + (n-1) = 14 - 0 + 6 = 20 >= 0
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{14});
        assertEquals(0, gen.nextInt(7));
    }

    @Test
    public void testNextIntNNotPowerOfTwoRejection() {
        // n = 7
        // Integer.MAX_VALUE = 2147483647
        // remainder = Integer.MAX_VALUE % 7 = 2147483647 % 7 = 3
        // We need bits such that bits - val + (n-1) < 0 to trigger rejection
        // bits = 0, val = 0, 0 - 0 + 6 = 6 >= 0 (no rejection)
        // bits = 1, val = 1, 1 - 1 + 6 = 6 >= 0
        // bits = 2, val = 2, 2 - 2 + 6 = 6 >= 0
        // bits = 3, val = 3, 3 - 3 + 6 = 6 >= 0
        // bits = 4, val = 4, 4 - 4 + 6 = 6 >= 0
        // Actually rejection happens when bits - val + (n-1) < 0
        // Since val = bits % n, bits - val is always a multiple of n and >= 0
        // So bits - val + (n-1) is always >= n-1 >= 0 for n>0
        // Wait, the condition is: while (bits - val + (n - 1) < 0)
        // This can happen if bits is large and val is small? No, val = bits % n, so bits - val is multiple of n.
        // Actually bits - val + (n-1) < 0 means bits - val < -(n-1), impossible since bits - val >= 0.
        // Let me re-check: The condition is from Harmony: while (bits - val + (n - 1) < 0)
        // But bits is 31-bit, so bits <= Integer.MAX_VALUE.
        // Actually the rejection is for when bits is in the last partial block.
        // Let's simulate: n=7, max=2147483647, remainder=3.
        // We want bits such that bits > max - remainder = 2147483644
        // bits = 2147483645, val = 2147483645 % 7 = 3, bits - val + 6 = 2147483645 - 3 + 6 = 2147483648 which overflows to negative!
        // So rejection happens due to overflow.
        // We'll provide bits that cause overflow, then a valid one.
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{2147483645, 14});
        assertEquals(0, gen.nextInt(7));
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextIntNZero() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{});
        gen.nextInt(0);
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void testNextIntNNegative() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{});
        gen.nextInt(-1);
    }

    @Test
    public void testNextIntNOne() {
        // n=1 is power of 2
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{12345});
        assertEquals(0, gen.nextInt(1));
    }

    @Test
    public void testNextLong() {
        // high = next(32) << 32, low = next(32) & 0xffffffffL
        int highVal = 0x12345678;
        int lowVal = 0x9ABCDEF0;
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{highVal, lowVal});
        long expected = ((long) highVal << 32) | ((long) lowVal & 0xffffffffL);
        assertEquals(expected, gen.nextLong());
    }

    @Test
    public void testNextLongZero() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{0, 0});
        assertEquals(0L, gen.nextLong());
    }

    @Test
    public void testNextLongMax() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{0xFFFFFFFF, 0xFFFFFFFF});
        long expected = ((long) 0xFFFFFFFF << 32) | 0xFFFFFFFFL;
        assertEquals(expected, gen.nextLong());
    }

    @Test
    public void testClear() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{1 << 25, 0, 1 << 25, 0, 1 << 25, 0, 1 << 25, 0});
        double first = gen.nextGaussian();
        gen.clear();
        double second = gen.nextGaussian();
        // After clear, should behave like first call again
        assertFalse(Double.isNaN(first));
        assertFalse(Double.isNaN(second));
    }

    @Test
    public void testSetSeedInt() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{});
        gen.setSeed(123);
        // Just verify no exception
    }

    @Test
    public void testSetSeedIntArray() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{});
        gen.setSeed(new int[]{1, 2, 3});
    }

    @Test
    public void testSetSeedLong() {
        TestBitsStreamGenerator gen = new TestBitsStreamGenerator(new int[]{});
        gen.setSeed(123L);
    }
}
