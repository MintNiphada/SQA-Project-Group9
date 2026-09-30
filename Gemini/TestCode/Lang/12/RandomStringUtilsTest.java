package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for {@link RandomStringUtils}.
 */
public class RandomStringUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new RandomStringUtils());
        assertTrue(Modifier.isPublic(RandomStringUtils.class.getConstructors()[0].getModifiers()));
    }

    @Test
    public void testRandom() {
        String r1 = RandomStringUtils.random(50);
        assertEquals(50, r1.length());

        String r2 = RandomStringUtils.random(50);
        assertEquals(50, r2.length());
        assertFalse("Two 50 character random strings should not be equal", r1.equals(r2));

        String r0 = RandomStringUtils.random(0);
        assertEquals("", r0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCount() {
        RandomStringUtils.random(-1);
    }

    @Test
    public void testRandomAscii() {
        String r = RandomStringUtils.randomAscii(50);
        assertEquals(50, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("Char should be between 32 and 126 inclusive, was: " + (int) c, c >= 32 && c <= 126);
        }
    }

    @Test
    public void testRandomAlphabetic() {
        String r = RandomStringUtils.randomAlphabetic(50);
        assertEquals(50, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("Char should be a letter: " + c, Character.isLetter(c));
        }
    }

    @Test
    public void testRandomAlphanumeric() {
        String r = RandomStringUtils.randomAlphanumeric(50);
        assertEquals(50, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("Char should be letter or digit: " + c, Character.isLetterOrDigit(c));
        }
    }

    @Test
    public void testRandomNumeric() {
        String r = RandomStringUtils.randomNumeric(50);
        assertEquals(50, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue("Char should be a digit: " + c, Character.isDigit(c));
        }
    }

    @Test
    public void testRandomLettersNumbers() {
        String r1 = RandomStringUtils.random(30, true, false);
        assertEquals(30, r1.length());
        for (int i = 0; i < r1.length(); i++) {
            assertTrue(Character.isLetter(r1.charAt(i)));
        }

        String r2 = RandomStringUtils.random(30, false, true);
        assertEquals(30, r2.length());
        for (int i = 0; i < r2.length(); i++) {
            assertTrue(Character.isDigit(r2.charAt(i)));
        }

        String r3 = RandomStringUtils.random(30, true, true);
        assertEquals(30, r3.length());
        for (int i = 0; i < r3.length(); i++) {
            assertTrue(Character.isLetterOrDigit(r3.charAt(i)));
        }

        String r4 = RandomStringUtils.random(30, false, false);
        assertEquals(30, r4.length());
    }

    @Test
    public void testRandomStartEndLettersNumbers() {
        String r = RandomStringUtils.random(20, 'a', 'g' + 1, false, false);
        assertEquals(20, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue(c >= 'a' && c <= 'g');
        }
    }

    @Test
    public void testRandomWithCharVarargs() {
        char[] set = new char[]{'a', 'b', 'c', '1', '2'};
        String r = RandomStringUtils.random(25, 0, set.length, false, false, set);
        assertEquals(25, r.length());
        for (int i = 0; i < r.length(); i++) {
            char c = r.charAt(i);
            assertTrue(c == 'a' || c == 'b' || c == 'c' || c == '1' || c == '2');
        }

        String rChars = RandomStringUtils.random(20, 'x', 'y', 'z');
        assertEquals(20, rChars.length());
        for (int i = 0; i < rChars.length(); i++) {
            char c = rChars.charAt(i);
            assertTrue(c == 'x' || c == 'y' || c == 'z');
        }

        String rNullChars = RandomStringUtils.random(10, (char[]) null);
        assertEquals(10, rNullChars.length());
    }

    @Test
    public void testRandomWithString() {
        String set = "abc123";
        String r = RandomStringUtils.random(30, set);
        assertEquals(30, r.length());
        for (int i = 0; i < r.length(); i++) {
            assertTrue(set.indexOf(r.charAt(i)) >= 0);
        }

        String rNull = RandomStringUtils.random(10, (String) null);
        assertEquals(10, rNull.length());
    }

    @Test
    public void testRandomSeeded() {
        Random seededRandom1 = new Random(12345L);
        Random seededRandom2 = new Random(12345L);

        String r1 = RandomStringUtils.random(50, 0, 0, true, true, null, seededRandom1);
        String r2 = RandomStringUtils.random(50, 0, 0, true, true, null, seededRandom2);

        assertEquals(r1, r2);
    }

    @Test
    public void testRandomCountZero() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.random(0, true, false));
        assertEquals("", RandomStringUtils.random(0, 0, 0, false, false));
        assertEquals("", RandomStringUtils.random(0, "abc"));
        assertEquals("", RandomStringUtils.random(0, 'a', 'b'));
        assertEquals("", RandomStringUtils.random(0, 0, 10, true, true, new char[]{'a'}, new Random()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomNegativeCountWithRandom() {
        RandomStringUtils.random(-5, 0, 0, false, false, null, new Random());
    }

    @Test
    public void testLowSurrogateHandling() {
        // Low surrogate: 56320 (0xDC00) to 57343 (0xDFFF)
        char lowSurrogate = 56320;
        char[] chars = new char[]{lowSurrogate, 'a'};

        // When count == 2, selecting low surrogate should prepend a high surrogate
        Random testRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                if (call++ == 0) {
                    return 0; // picks chars[0] -> low surrogate
                }
                return 0; // high surrogate offset
            }
        };

        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, testRandom);
        assertEquals(2, result.length());
        char high = result.charAt(0);
        char low = result.charAt(1);
        assertTrue("Expected high surrogate: " + (int) high, high >= 55296 && high <= 55424);
        assertEquals(lowSurrogate, low);

        // When count == 1, picking low surrogate should cause retry
        Random retryRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                if (call++ == 0) {
                    return 0; // first pick low surrogate with count == 0 remaining (triggers count++)
                }
                return 1; // second pick 'a'
            }
        };

        String resultRetry = RandomStringUtils.random(1, 0, chars.length, false, false, chars, retryRandom);
        assertEquals("a", resultRetry);
    }

    @Test
    public void testHighSurrogateHandling() {
        // High surrogate: 55296 (0xD800) to 56191 (0xDB7F)
        char highSurrogate = 55296;
        char[] chars = new char[]{highSurrogate, 'b'};

        // When count == 2, selecting high surrogate should append a low surrogate
        Random testRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                if (call++ == 0) {
                    return 0; // picks chars[0] -> high surrogate
                }
                return 0; // low surrogate offset
            }
        };

        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, testRandom);
        assertEquals(2, result.length());
        char high = result.charAt(0);
        char low = result.charAt(1);
        assertEquals(highSurrogate, high);
        assertTrue("Expected low surrogate: " + (int) low, low >= 56320 && low <= 56448);

        // When count == 1, picking high surrogate should cause retry
        Random retryRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                if (call++ == 0) {
                    return 0; // pick high surrogate with count == 0 remaining (triggers count++)
                }
                return 1; // pick 'b'
            }
        };

        String resultRetry = RandomStringUtils.random(1, 0, chars.length, false, false, chars, retryRandom);
        assertEquals("b", resultRetry);
    }

    @Test
    public void testPrivateHighSurrogateHandling() {
        // Private high surrogate: 56192 (0xDB80) to 56319 (0xDBFF) - should be skipped
        char privateHighSurrogate = 56200;
        char[] chars = new char[]{privateHighSurrogate, 'z'};

        Random skipRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                if (call++ == 0) {
                    return 0; // picks private high surrogate -> skipped
                }
                return 1; // picks 'z'
            }
        };

        String result = RandomStringUtils.random(1, 0, chars.length, false, false, chars, skipRandom);
        assertEquals("z", result);
    }

    @Test
    public void testFilteringRejectionBranch() {
        // Letters only, but random returns non-letter first
        char[] chars = new char[]{'1', 'A'};
        Random filterRandom = new Random() {
            private int call = 0;
            @Override
            public int nextInt(int bound) {
                if (call++ == 0) {
                    return 0; // '1' which fails isLetter check
                }
                return 1; // 'A' which passes
            }
        };

        String result = RandomStringUtils.random(1, 0, chars.length, true, false, chars, filterRandom);
        assertEquals("A", result);
    }
}
