package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Random;

public class RandomStringUtilsTest {

    @Test
    public void testConstructor() {
        RandomStringUtils instance = new RandomStringUtils();
        Assert.assertNotNull(instance);
        Constructor<?>[] constructors = RandomStringUtils.class.getDeclaredConstructors();
        Assert.assertEquals(1, constructors.length);
        Assert.assertTrue(Modifier.isPublic(constructors[0].getModifiers()));
    }

    @Test
    public void testRandomZeroCount() {
        Assert.assertEquals("", RandomStringUtils.random(0));
        Assert.assertEquals("", RandomStringUtils.randomAscii(0));
        Assert.assertEquals("", RandomStringUtils.randomAlphabetic(0));
        Assert.assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        Assert.assertEquals("", RandomStringUtils.randomNumeric(0));
        Assert.assertEquals("", RandomStringUtils.random(0, true, true));
        Assert.assertEquals("", RandomStringUtils.random(0, 0, 0, true, true));
        Assert.assertEquals("", RandomStringUtils.random(0, 0, 0, true, true, new char[]{'a'}));
        Assert.assertEquals("", RandomStringUtils.random(0, "abc"));
        Assert.assertEquals("", RandomStringUtils.random(0, (String) null));
        Assert.assertEquals("", RandomStringUtils.random(0, new char[]{'a', 'b'}));
        Assert.assertEquals("", RandomStringUtils.random(0, (char[]) null));
        Assert.assertEquals("", RandomStringUtils.random(0, 0, 10, false, false, new char[]{'a'}, new Random()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCount() {
        RandomStringUtils.random(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAsciiNegativeCount() {
        RandomStringUtils.randomAscii(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphabeticNegativeCount() {
        RandomStringUtils.randomAlphabetic(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomAlphanumericNegativeCount() {
        RandomStringUtils.randomAlphanumeric(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNumericNegativeCount() {
        RandomStringUtils.randomNumeric(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomStringCharsEmpty() {
        RandomStringUtils.random(5, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomCharsEmpty() {
        RandomStringUtils.random(5, new char[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomFullNegativeCount() {
        RandomStringUtils.random(-1, 0, 0, false, false, new char[]{'a'}, new Random());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomFullEmptyChars() {
        RandomStringUtils.random(5, 0, 0, false, false, new char[0], new Random());
    }

    @Test
    public void testRandomAscii() {
        int length = 50;
        String result = RandomStringUtils.randomAscii(length);
        Assert.assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            Assert.assertTrue("Char is not ASCII printable: " + (int) c, c >= 32 && c <= 126);
        }
    }

    @Test
    public void testRandomAlphabetic() {
        int length = 50;
        String result = RandomStringUtils.randomAlphabetic(length);
        Assert.assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            Assert.assertTrue("Char is not alphabetic: " + c, Character.isLetter(c));
        }
    }

    @Test
    public void testRandomAlphanumeric() {
        int length = 50;
        String result = RandomStringUtils.randomAlphanumeric(length);
        Assert.assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            Assert.assertTrue("Char is not alphanumeric: " + c, Character.isLetterOrDigit(c));
        }
    }

    @Test
    public void testRandomNumeric() {
        int length = 50;
        String result = RandomStringUtils.randomNumeric(length);
        Assert.assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            Assert.assertTrue("Char is not digit: " + c, Character.isDigit(c));
        }
    }

    @Test
    public void testRandomLettersNumbersCombinations() {
        String lettersOnly = RandomStringUtils.random(30, true, false);
        Assert.assertEquals(30, lettersOnly.length());
        for (char c : lettersOnly.toCharArray()) {
            Assert.assertTrue(Character.isLetter(c));
        }

        String numbersOnly = RandomStringUtils.random(30, false, true);
        Assert.assertEquals(30, numbersOnly.length());
        for (char c : numbersOnly.toCharArray()) {
            Assert.assertTrue(Character.isDigit(c));
        }

        String neither = RandomStringUtils.random(30, false, false);
        Assert.assertEquals(30, neither.length());

        String both = RandomStringUtils.random(30, true, true);
        Assert.assertEquals(30, both.length());
        for (char c : both.toCharArray()) {
            Assert.assertTrue(Character.isLetterOrDigit(c));
        }
    }

    @Test
    public void testRandomWithCharRange() {
        String result = RandomStringUtils.random(20, 'a', 'd' + 1, false, false);
        Assert.assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            Assert.assertTrue(c >= 'a' && c <= 'd');
        }
    }

    @Test
    public void testRandomWithCharRangeAndFilter() {
        String result = RandomStringUtils.random(20, 'a', 'z' + 1, true, false);
        Assert.assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            Assert.assertTrue(Character.isLetter(c));
        }
    }

    @Test
    public void testRandomWithCharsArray() {
        char[] set = new char[]{'a', 'b', 'c'};
        String result = RandomStringUtils.random(25, 0, set.length, false, false, set);
        Assert.assertEquals(25, result.length());
        for (char c : result.toCharArray()) {
            Assert.assertTrue(c == 'a' || c == 'b' || c == 'c');
        }
    }

    @Test
    public void testRandomStringChars() {
        String chars = "xyz123";
        String result = RandomStringUtils.random(15, chars);
        Assert.assertEquals(15, result.length());
        for (char c : result.toCharArray()) {
            Assert.assertTrue(chars.indexOf(c) >= 0);
        }

        String nullCharsResult = RandomStringUtils.random(10, (String) null);
        Assert.assertEquals(10, nullCharsResult.length());
    }

    @Test
    public void testRandomVarArgsChars() {
        String result = RandomStringUtils.random(15, 'x', 'y', 'z');
        Assert.assertEquals(15, result.length());
        for (char c : result.toCharArray()) {
            Assert.assertTrue(c == 'x' || c == 'y' || c == 'z');
        }

        String nullCharsResult = RandomStringUtils.random(10, (char[]) null);
        Assert.assertEquals(10, nullCharsResult.length());
    }

    @Test
    public void testRandomFixedSeed() {
        long seed = 123456789L;
        String r1 = RandomStringUtils.random(20, 0, 0, true, true, null, new Random(seed));
        String r2 = RandomStringUtils.random(20, 0, 0, true, true, null, new Random(seed));
        Assert.assertEquals(r1, r2);
    }

    @Test
    public void testRandomStartEndZeroWithChars() {
        char[] chars = new char[]{'1', '2', '3'};
        String result = RandomStringUtils.random(10, 0, 0, false, false, chars, new Random(100));
        Assert.assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            Assert.assertTrue(c == '1' || c == '2' || c == '3');
        }
    }

    @Test
    public void testSurrogateHandlingHighSurrogate() {
        // High surrogate: 55296 (0xd800) to 56191 (0xdb7f)
        Random deterministicRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 0; // produces char 55296
                }
                return 10; // offset for low surrogate
            }
        };

        String result = RandomStringUtils.random(2, 55296, 55297, false, false, null, deterministicRandom);
        Assert.assertEquals(2, result.length());
        Assert.assertEquals(55296, (int) result.charAt(0));
        Assert.assertEquals(56320 + 10, (int) result.charAt(1));
    }

    @Test
    public void testSurrogateHandlingLowSurrogate() {
        // Low surrogate: 56320 (0xdc00) to 57343 (0xdfff)
        Random deterministicRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 0; // produces char 56320
                }
                return 20; // offset for high surrogate
            }
        };

        String result = RandomStringUtils.random(2, 56320, 56321, false, false, null, deterministicRandom);
        Assert.assertEquals(2, result.length());
        Assert.assertEquals(55296 + 20, (int) result.charAt(0));
        Assert.assertEquals(56320, (int) result.charAt(1));
    }

    @Test
    public void testSurrogateHandlingHighSurrogateCountZeroBranch() {
        // When high surrogate is generated on count == 0 (which is the last character needed),
        // it must retry (count++) so it can fit both surrogates.
        Random deterministicRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 0; // Generates 55296 when count == 0
                }
                if (call == 2) {
                    return 'A' - 55296; // Generates 'A' (65) when count == 0
                }
                return 0;
            }
        };

        String result = RandomStringUtils.random(1, 55296, 55297 + ('A' - 55296), false, false, null, deterministicRandom);
        Assert.assertEquals(1, result.length());
        Assert.assertEquals("A", result);
    }

    @Test
    public void testSurrogateHandlingLowSurrogateCountZeroBranch() {
        // When low surrogate is generated on count == 0, count is incremented to retry
        Random deterministicRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 0; // Generates 56320 when count == 0
                }
                if (call == 2) {
                    return 'B' - 56320; // Generates 'B'
                }
                return 0;
            }
        };

        String result = RandomStringUtils.random(1, 56320, 56321 + ('B' - 56320), false, false, null, deterministicRandom);
        Assert.assertEquals(1, result.length());
        Assert.assertEquals("B", result);
    }

    @Test
    public void testPrivateHighSurrogateSkipped() {
        // Private high surrogate: 56192 (0xdb80) to 56319 (0xdbff)
        Random deterministicRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 0; // Generates 56192 (private high surrogate) -> should be skipped
                }
                return 'Z' - 56192; // Generates 'Z'
            }
        };

        String result = RandomStringUtils.random(1, 56192, 56193 + ('Z' - 56192), false, false, null, deterministicRandom);
        Assert.assertEquals(1, result.length());
        Assert.assertEquals("Z", result);
    }

    @Test
    public void testFilteringRejectionRetries() {
        // When requesting letters only, non-letter characters are skipped
        Random deterministicRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                call++;
                if (call == 1) {
                    return 0; // generates '1' (digit)
                }
                return 1; // generates 'A' (letter)
            }
        };

        char[] chars = new char[]{'1', 'A'};
        String result = RandomStringUtils.random(1, 0, 2, true, false, chars, deterministicRandom);
        Assert.assertEquals("A", result);
    }
}
