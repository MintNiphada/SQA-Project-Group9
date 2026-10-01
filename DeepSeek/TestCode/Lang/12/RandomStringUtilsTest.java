package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

public class RandomStringUtilsTest {

    @Test
    public void testRandomCountZero() {
        Assert.assertEquals("", RandomStringUtils.random(0));
    }

    @Test
    public void testRandomCountPositive() {
        String result = RandomStringUtils.random(5);
        Assert.assertNotNull(result);
        Assert.assertEquals(5, result.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomCountNegative() {
        RandomStringUtils.random(-1);
    }

    @Test
    public void testRandomAsciiCountZero() {
        Assert.assertEquals("", RandomStringUtils.randomAscii(0));
    }

    @Test
    public void testRandomAsciiCountPositive() {
        String result = RandomStringUtils.randomAscii(10);
        Assert.assertNotNull(result);
        Assert.assertEquals(10, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue("Character not in ASCII range 32-126", ch >= 32 && ch <= 126);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAsciiCountNegative() {
        RandomStringUtils.randomAscii(-1);
    }

    @Test
    public void testRandomAlphabeticCountZero() {
        Assert.assertEquals("", RandomStringUtils.randomAlphabetic(0));
    }

    @Test
    public void testRandomAlphabeticCountPositive() {
        String result = RandomStringUtils.randomAlphabetic(8);
        Assert.assertNotNull(result);
        Assert.assertEquals(8, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue("Character not alphabetic", Character.isLetter(ch));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphabeticCountNegative() {
        RandomStringUtils.randomAlphabetic(-1);
    }

    @Test
    public void testRandomAlphanumericCountZero() {
        Assert.assertEquals("", RandomStringUtils.randomAlphanumeric(0));
    }

    @Test
    public void testRandomAlphanumericCountPositive() {
        String result = RandomStringUtils.randomAlphanumeric(12);
        Assert.assertNotNull(result);
        Assert.assertEquals(12, result.length());
        for (char ch:result.toCharArray()){            Assert.assertTrue("Character not alphanumeric", Character.isLetterOrDigit(ch));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphanumericCountNegative() {
        RandomStringUtils.randomAlphanumeric(-1);
    }

    @Test
    public void testRandomNumericCountZero() {
        Assert.assertEquals("", RandomStringUtils.randomNumeric(0));
    }

    @Test
    public void testRandomNumericCountPositive() {
        String result = RandomStringUtils.randomNumeric(7);
        Assert.assertNotNull(result);
        Assert.assertEquals(7, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue("Character not numeric", CharacterisDigit(ch));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNumericCountNegative() {
        RandomStringUtils.randomNumeric(-1);
    }

    @Test
    public void testRandomWithLettersOnly() {
        String result = RandomStringUtils.random(5, true, false);
        Assert.assertEquals(5, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue(Character.isLetter(ch));
        }
    }

    @Test
    public void testRandomWithNumbersOnly() {
        String result = RandomStringUtils.random(5, false, true);
        Assert.assertEquals(5, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue(Character.isDigit(ch));
        }
    }

    @Test
    public void testRandomWithLettersAndNumbers() {
        String result = RandomStringUtils.random(10, true, true);
        Assert.assertEquals(10, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue(Character.isLetterOrDigit(ch));
        }
    }

    @Test
    public void testRandomWithNoLettersNoNumbers() {
        String result = RandomStringUtils.random(5, false, false);
        Assert.assertEquals(5, result.length());
        // Should be any characters from Integer.MIN_VALUE to Integer.MAX_VALUE range (but only unicode chars)
    }

    @Test
    public void testRandomWithStartEndAllChars() {
        int count = 3;
        String result = RandomStringUtils.random(count, 0, 256, false, false);
        Assert.assertEquals(count, result.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithInvalidCount() {
        RandomStringUtils.random(-1, 0, 0, false, false);
    }

    @Test
    public void testRandomWithCharsArrayNull() {
        String result = RandomStringUtils.random(4, 0, 0, true, false, (char[]) null);
        Assert.assertEquals(4, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue(Character.isLetter(ch));
        }
    }

    @Test
    public void testRandomWithCharsArrayEmpty() {
        try {
            RandomStringUtils.random(1, 0, 0, false, false, new char[]{});
            Assert.fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testRandomWithCharsArrayProvided() {
        char[] chars = {'a', 'b', 'c', '1', '2', '3'};
        String result = RandomStringUtils.random(5, 0, chars.length, false, false, chars);
        Assert.assertEquals(5, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue(new String(chars).indexOf(ch) != -1);
        }
    }

    @Test
    public void testRandomWithCharsArrayAndLettersNumbersFiltering() {
        char[] chars = {'a', '1', '!', 'B', '2'};
        // only letters and numbers should pass the filter
        String result = RandomStringUtils.random(10, 0, chars.length, true, true, chars);
        Assert.assertEquals(10, result.length());
        for (char ch:result.toCharArray()){            Assert.assertTrue(Character.isLetterOrDigit(ch));
        }
    }

    @Test
    public void testRandomWithFixedRandomForDeterminism() {
        Random fixed = new Random(12345L);
        String s1 = RandomStringUtils.random(6, 0, 0, false, false, null, fixed);
        // reset seed to get same sequence
        fixed = new Random(12345L);
        String s2 = RandomStringUtils.random(6, 0, 0, false, false, null, fixed);
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void testRandomSurrogateLowSurrogate() {
        // Force the selection of low surrogate by providing a range that includes low surrogates
        char[] chars = new char[128];
        // Fill array with low surrogate characters starting from \uDC00
        for (int i = 0; i < 128; i++) {
            chars[i] = (char) (0xDC00 + i);
        }
        // Create Random that always returns 0 so we select low surrogate at position 0
        Random zeroRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, zeroRandom);
        // Expect two characters: the low surrogate and a preceding high surrogate
        Assert.assertEquals(2, result.length());
        char first = result.charAt(0);
        char second = result.charAt(1);
        Assert.assertTrue("First should be high surrogate", Character.isHighSurrogate(first));
        Assert.assertTrue("Second should be low surrogate", Character.isLowSurrogate(second));
        Assert.assertEquals(chars[0], second);
    }

    @Test
    public void testRandomSurrogateHighSurrogate() {
        // Force high surrogate selection
        char[] chars = new char[128];
        for (int i = 0; i < 128; i++) {
            chars[i] = (char) (0xD800 + i);
        }
        Random zeroRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, zeroRandom);
        Assert.assertEquals(2, result.length());
        char first = result.charAt(0);
        char second = result.charAt(1);
        Assert.assertTrue("First should be high surrogate", Character.isHighSurrogate(first));
        Assert.assertTrue("Second should be low surrogate", Character.isLowSurrogate(second));
        Assert.assertEquals(chars[0], first);
    }

    @Test
    public void testRandomSurrogatePrivateHighSurrogateSkip() {
        // Private high surrogates should be skipped
        char[] chars = new char[1];
        chars[0] = 0xDB80; // private high surrogate
        Random zeroRandom = new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
        // The loop will skip if letter/numbers check is satisfied (which it will because !letters && !numbers is true)
        // But the private high surrogate handling increments count, effectively retrying.
        // We need exactly one character, so count will bounce to 0 eventually after many retries? Actually if chars only has private high surrogate,
        // it will keep incrementing count, causing infinite loop? But the random is deterministic and surrogate is private high; it will skip and increment count.
        // We'll test with count=1, but need to avoid infinite loop. Use a Random that returns 0 which selects the private high surrogate, then count++,
        // and loop continues, again selects same char, infinite. Not good. So we'll craft random to return 0 only once, then something else.
        // Instead, we can test that the surrogate is skipped by using a count of 1 but a custom random that returns first 0 (private high) then next returns 1 (some valid char).
        // But chars array only has one element, returns 0 always. We'll use Random with seed that ensures after first call, nextInt(1) returns 0 again? No.
        // Better: set gap large enough and start=0, chars null, and set range to include private high surrogates. But start/end might not be inclusive.
        // Simpler: Use letters = true, numbers = false, so only letters pass. Private high surrogate is not a letter, so it will be skipped by the else branch (count++) not the surrogate handling. Actually private high surrogate is handled only if (letters && Character.isLetter(ch) || numbers && Character.isDigit(ch) || !letters && !numbers) is true. If we set letters=true, numbers=false, then isLetter(private high) false, so else count++ occurs, skipping it similarly. So we can test skipping via letters filter without hitting infinite loop.
        // So we'll test that when chars array contains only invalid characters (filter eliminates them), it eventually ends with count 0 after many skips? But we need a finite deterministic exits. This is tricky.
        // We'll test the private high surrogate branch directly by setting !letters && !numbers (so it passes the first condition) and making it choose a private high surrogate, then skipping it. To avoid infinite loop, we'll use a custom Random that returns 0 (private high) on first call, then returns something else (like 1, which is a valid character) on subsequent calls.
        // Build Random that alternates values.
        Random mock = new Random() {
            private int callCount = 0;
            @Override
            public int nextInt(int bound) {
                if (callCount++ == 0) {
                    return 0; // private high surrogate
                }
                return 1; // next character (some valid one)
            }
        };
        // chars array of length 2: [privateHigh, validChar]
        char[] testChars = new char[2];
        testChars[0] = 0xDB80; // private high surrogate
        testChars[1] = 'a'; // valid
        String result = RandomStringUtils.random(1, 0, testChars.length, false, false, testChars, mock);
        // It should skip the private high surrogate and eventually pick the valid 'a', leaving count 0.
        Assert.assertEquals(1, result.length());
        Assert.assertEquals('a', result.charAt(0));
    }

    @Test
    public void testRandomWithNegativeGap() {
        // start > end will cause gap negative, Random.nextInt(negative) throws IllegalArgumentException
        try {
            RandomStringUtils.random(1, 10, 5, false, false);
            Assert.fail("Expected exception due to negative gap");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRandomWithStringCharsNull() {
        String result = RandomStringUtils.random(3, (String) null);
        Assert.assertEquals(3, result.length());
    }

    @Test
    public void testRandomWithStringChars() {
        String chars = "abc123";
        String result = RandomStringUtils.random(5, chars);
        Assert.assertEquals(5, result.length());
        for (char ch : result.toCharArray()) {
            Assert.assertTrue(chars.indexOf(ch) != -1);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithEmptyStringChars() {
        RandomStringUtils.random(1, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomWithEmptyCharArrayDirect() {
        RandomStringUtils.random(1, new char[0]);
    }

    @Test
    public void testRandomWithStringCharsAndCountZero() {
        Assert.assertEquals("", RandomStringUtils.random(0, "abc"));
    }

    @Test
    public void testRandomWithCharArrayAndCountZero() {
        Assert.assertEquals("", RandomStringUtils.random(0, new char[]{'a','b'}));
    }

    @Test
    public void testConstructor() {
        // just to cover constructor
        new RandomStringUtils();
    }

    @Test
    public void testRandomWithLettersOnlyAndSurrogate() {
        // Test that when letters=true, low surrogate (non-letter) is skipped (retried).
        // Use chars array with low surrogate as first element, letters=true.
        char[] chars = new char[2];
        chars[0] = 0xDC00; // low surrogate
        chars[1] = 'A';
        Random zeroRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                if (call++ == 0) return 0; // low surrogate, not a letter, will be skipped
                return 1; // 'A'
            }
        };
        String result = RandomStringUtils.random(1, 0, chars.length, true, false, chars, zeroRandom);
        Assert.assertEquals(1, result.length());
        Assert.assertEquals('A', result.charAt(0));
    }
}
