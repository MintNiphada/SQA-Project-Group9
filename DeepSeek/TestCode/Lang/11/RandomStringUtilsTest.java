package org.apache.commons.lang3;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Random;

public class RandomStringUtilsTest {

    @Test
    public void testConstructor() {
        new RandomStringUtils();
    }

    @Test
    public void testRandomCountZero() {
        assertEquals("", RandomStringUtils.random(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCount() {
        RandomStringUtils.random(-1);
    }

    @Test
    public void testRandomPositiveCount() {
        String result = RandomStringUtils.random(10);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    public void testRandomAsciiCountZero() {
        assertEquals("", RandomStringUtils.randomAscii(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAsciiNegativeCount() {
        RandomStringUtils.randomAscii(-1);
    }

    @Test
    public void testRandomAsciiPositiveCount() {
        String result = RandomStringUtils.randomAscii(10);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 32 && c <= 126);
        }
    }

    @Test
    public void testRandomAlphabeticCountZero() {
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphabeticNegativeCount() {
        RandomStringUtils.randomAlphabetic(-1);
    }

    @Test
    public void testRandomAlphabeticPositiveCount() {
        String result = RandomStringUtils.randomAlphabetic(10);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    @Test
    public void testRandomAlphanumericCountZero() {
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphanumericNegativeCount() {
        RandomStringUtils.randomAlphanumeric(-1);
    }

    @Test
    public void testRandomAlphanumericPositiveCount() {
        String result = RandomStringUtils.randomAlphanumeric(10);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    @Test
    public void testRandomNumericCountZero() {
        assertEquals("", RandomStringUtils.randomNumeric(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNumericNegativeCount() {
        RandomStringUtils.randomNumeric(-1);
    }

    @Test
    public void testRandomNumericPositiveCount() {
        String result = RandomStringUtils.randomNumeric(10);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test
    public void testRandomLettersNumbersCountZero() {
        assertEquals("", RandomStringUtils.random(0, true, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomLettersNumbersNegativeCount() {
        RandomStringUtils.random(-1, true, true);
    }

    @Test
    public void testRandomLettersNumbersPositiveCount() {
        String result = RandomStringUtils.random(10, true, true);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    @Test
    public void testRandomLettersOnly() {
        String result = RandomStringUtils.random(10, true, false);
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    @Test
    public void testRandomNumbersOnly() {
        String result = RandomStringUtils.random(10, false, true);
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test
    public void testRandomNoLettersNoNumbers() {
        String result = RandomStringUtils.random(10, false, false);
        assertEquals(10, result.length());
    }

    @Test
    public void testRandomWithStartEnd() {
        String result = RandomStringUtils.random(10, 65, 91, false, false);
        for (char c : result.toCharArray()) {
            assertTrue(c >= 65 && c < 91);
        }
    }

    @Test
    public void testRandomWithStartEndLetters() {
        String result = RandomStringUtils.random(10, 65, 91, true, false);
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c) && c >= 65 && c < 91);
        }
    }

    @Test
    public void testRandomWithCharsArray() {
        char[] chars = {'a', 'b', 'c'};
        String result = RandomStringUtils.random(10, chars);
        for (char c : result.toCharArray()) {
            assertTrue(c == 'a' || c == 'b' || c == 'c');
        }
    }

    @Test
    public void testRandomWithCharsArrayNull() {
        String result = RandomStringUtils.random(10, (char[]) null);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithEmptyCharsArray() {
        RandomStringUtils.random(10, new char[0]);
    }

    @Test
    public void testRandomWithStringChars() {
        String result = RandomStringUtils.random(10, "xyz");
        for (char c : result.toCharArray()) {
            assertTrue(c == 'x' || c == 'y' || c == 'z');
        }
    }

    @Test
    public void testRandomWithStringCharsNull() {
        String result = RandomStringUtils.random(10, (String) null);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithEmptyStringChars() {
        RandomStringUtils.random(10, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCountWithChars() {
        RandomStringUtils.random(-1, new char[]{'a'});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCountWithString() {
        RandomStringUtils.random(-1, "abc");
    }

    @Test
    public void testRandomWithStartEndAndCharsArray() {
        char[] chars = {'a', 'b', 'c', 'd', 'e'};
        String result = RandomStringUtils.random(10, 1, 4, false, false, chars);
        for (char c : result.toCharArray()) {
            assertTrue(c == 'b' || c == 'c' || c == 'd');
        }
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRandomStartEndOutOfBounds() {
        char[] chars = {'a', 'b'};
        RandomStringUtils.random(5, 0, 3, false, false, chars);
    }

    @Test
    public void testRandomWithSeededRandom() {
        Random seeded = new Random(12345);
        String result1 = RandomStringUtils.random(10, 0, 0, false, false, null, seeded);
        seeded = new Random(12345);
        String result2 = RandomStringUtils.random(10, 0, 0, false, false, null, seeded);
        assertEquals(result1, result2);
    }

    @Test
    public void testRandomWithControlledRandomLetterFilter() {
        // chars: '1' (non-letter), 'a' (letter)
        char[] chars = {'1', 'a'};
        // nextInt will return 0 (index of '1') first, then 1 (index of 'a')
        ControlledRandom random = new ControlledRandom(0, 1);
        String result = RandomStringUtils.random(1, 0, chars.length, true, false, chars, random);
        assertEquals("a", result);
    }

    @Test
    public void testRandomWithControlledRandomDigitFilter() {
        char[] chars = {'a', '1'};
        ControlledRandom random = new ControlledRandom(0, 1);
        String result = RandomStringUtils.random(1, 0, chars.length, false, true, chars, random);
        assertEquals("1", result);
    }

    @Test
    public void testRandomWithControlledRandomNoFilter() {
        char[] chars = {'X', 'Y'};
        ControlledRandom random = new ControlledRandom(0, 1);
        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, random);
        assertEquals("XY", result);
    }

    @Test
    public void testLowSurrogateInsertion() {
        // low surrogate: \uDC00 (56320)
        char[] chars = {'\uDC00', 'A'};
        // nextInt calls:
        // 1st: index 0 -> low surrogate (count=2, after decrement count=1)
        // 2nd: for high surrogate generation: nextInt(128) -> return 0 -> high surrogate = 55296 + 0 = \uD800
        ControlledRandom random = new ControlledRandom(0, 0);
        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, random);
        assertEquals(2, result.length());
        assertTrue(Character.isHighSurrogate(result.charAt(0)));
        assertTrue(Character.isLowSurrogate(result.charAt(1)));
        assertEquals('\uD800', result.charAt(0));
        assertEquals('\uDC00', result.charAt(1));
    }

    @Test
    public void testHighSurrogateInsertion() {
        // high surrogate: \uD800 (55296)
        char[] chars = {'\uD800', 'B'};
        // nextInt calls:
        // 1st: index 0 -> high surrogate (count=2, after decrement count=1)
        // 2nd: for low surrogate generation: nextInt(128) -> return 0 -> low surrogate = 56320 + 0 = \uDC00
        ControlledRandom random = new ControlledRandom(0, 0);
        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, random);
        assertEquals(2, result.length());
        assertTrue(Character.isHighSurrogate(result.charAt(0)));
        assertTrue(Character.isLowSurrogate(result.charAt(1)));
        assertEquals('\uD800', result.charAt(0));
        assertEquals('\uDC00', result.charAt(1));
    }

    @Test
    public void testLowSurrogateAtEndOfBuffer() {
        // count=1, low surrogate appears when count becomes 0
        char[] chars = {'\uDC00'};
        // nextInt returns 0
        ControlledRandom random = new ControlledRandom(0);
        String result = RandomStringUtils.random(1, 0, chars.length, false, false, chars, random);
        assertEquals(1, result.length());
        assertEquals('\0', result.charAt(0)); // buffer not filled, default null char
    }

    @Test
    public void testHighSurrogateAtEndOfBuffer() {
        char[] chars = {'\uD800'};
        ControlledRandom random = new ControlledRandom(0);
        String result = RandomStringUtils.random(1, 0, chars.length, false, false, chars, random);
        assertEquals(1, result.length());
        assertEquals('\0', result.charAt(0));
    }

    @Test
    public void testPrivateHighSurrogateSkip() {
        // private high surrogate: \uDB80 (56192)
        char[] chars = {'\uDB80', 'Z'};
        // nextInt: first 0 (private high surrogate), then 1 (Z)
        ControlledRandom random = new ControlledRandom(0, 1);
        String result = RandomStringUtils.random(1, 0, chars.length, false, false, chars, random);
        assertEquals("Z", result);
    }

    @Test
    public void testPrivateHighSurrogateSkipAtEnd() {
        char[] chars = {'\uDB80'};
        ControlledRandom random = new ControlledRandom(0);
        String result = RandomStringUtils.random(1, 0, chars.length, false, false, chars, random);
        assertEquals(1, result.length());
        assertEquals('\0', result.charAt(0));
    }

    @Test
    public void testStartEndZeroWithChars() {
        char[] chars = {'x', 'y', 'z'};
        String result = RandomStringUtils.random(5, 0, 0, false, false, chars);
        for (char c : result.toCharArray()) {
            assertTrue(c == 'x' || c == 'y' || c == 'z');
        }
    }

    @Test
    public void testStartEndZeroNoLettersNoNumbers() {
        String result = RandomStringUtils.random(5, 0, 0, false, false);
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    @Test
    public void testStartEndZeroLettersOnly() {
        String result = RandomStringUtils.random(5, 0, 0, true, false);
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }
    }

    @Test
    public void testStartEndZeroNumbersOnly() {
        String result = RandomStringUtils.random(5, 0, 0, false, true);
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test
    public void testRandomWithCharsArrayAndStartEnd() {
        char[] chars = {'a', 'b', 'c', 'd'};
        String result = RandomStringUtils.random(5, 1, 3, false, false, chars);
        for (char c : result.toCharArray()) {
            assertTrue(c == 'b' || c == 'c');
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithNullCharsAndEmptyArray() {
        RandomStringUtils.random(5, 0, 0, false, false, new char[0]);
    }

    // Helper class to control Random values
    private static class ControlledRandom extends Random {
        private final int[] values;
        private int index = 0;

        ControlledRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int val = values[index++ % values.length];
            // Ensure val is within bound for the test to be valid
            if (val >= bound) {
                throw new IllegalArgumentException("ControlledRandom value out of bound: " + val + " >= " + bound);
            }
            return val;
        }
    }
}
