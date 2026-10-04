package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

public class StringUtilsTest {

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new StringUtils());
    }

    @Test
    public void testIsEmptyAndIsNotEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertFalse(StringUtils.isEmpty(" "));
        Assert.assertFalse(StringUtils.isEmpty("a"));

        Assert.assertFalse(StringUtils.isNotEmpty(null));
        Assert.assertFalse(StringUtils.isNotEmpty(""));
        Assert.assertTrue(StringUtils.isNotEmpty(" "));
        Assert.assertTrue(StringUtils.isNotEmpty("a"));
    }

    @Test
    public void testIsBlankAndIsNotBlank() {
        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank("   \t\r\n"));
        Assert.assertFalse(StringUtils.isBlank("  a  "));

        Assert.assertFalse(StringUtils.isNotBlank(null));
        Assert.assertFalse(StringUtils.isNotBlank(""));
        Assert.assertFalse(StringUtils.isNotBlank("   "));
        Assert.assertTrue(StringUtils.isNotBlank("  a  "));
    }

    @Test
    public void testTrimAndTrimToNullAndTrimToEmpty() {
        Assert.assertNull(StringUtils.trim(null));
        Assert.assertEquals("", StringUtils.trim(""));
        Assert.assertEquals("abc", StringUtils.trim("  abc  "));

        Assert.assertNull(StringUtils.trimToNull(null));
        Assert.assertNull(StringUtils.trimToNull(""));
        Assert.assertNull(StringUtils.trimToNull("   "));
        Assert.assertEquals("abc", StringUtils.trimToNull("  abc  "));

        Assert.assertEquals("", StringUtils.trimToEmpty(null));
        Assert.assertEquals("", StringUtils.trimToEmpty(""));
        Assert.assertEquals("", StringUtils.trimToEmpty("   "));
        Assert.assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }

    @Test
    public void testStripAndStripToNullAndStripToEmpty() {
        Assert.assertNull(StringUtils.strip(null));
        Assert.assertEquals("", StringUtils.strip(""));
        Assert.assertEquals("abc", StringUtils.strip(" \t abc \n "));
        Assert.assertEquals("abc", StringUtils.strip("  abc", null));

        Assert.assertNull(StringUtils.stripToNull(null));
        Assert.assertNull(StringUtils.stripToNull(""));
        Assert.assertNull(StringUtils.stripToNull("   "));
        Assert.assertEquals("abc", StringUtils.stripToNull("  abc  "));

        Assert.assertEquals("", StringUtils.stripToEmpty(null));
        Assert.assertEquals("", StringUtils.stripToEmpty(""));
        Assert.assertEquals("", StringUtils.stripToEmpty("   "));
        Assert.assertEquals("abc", StringUtils.stripToEmpty("  abc  "));

        Assert.assertNull(StringUtils.strip(null, "xyz"));
        Assert.assertEquals("", StringUtils.strip("", "xyz"));
        Assert.assertEquals("abc", StringUtils.strip("yxzabczyx", "xyz"));
    }

    @Test
    public void testStripStartAndStripEnd() {
        Assert.assertNull(StringUtils.stripStart(null, "a"));
        Assert.assertEquals("", StringUtils.stripStart("", "a"));
        Assert.assertEquals("abc", StringUtils.stripStart("  abc", null));
        Assert.assertEquals("abc", StringUtils.stripStart("abc", ""));
        Assert.assertEquals("abc", StringUtils.stripStart("xxabc", "x"));
        Assert.assertEquals("", StringUtils.stripStart("xxx", "x"));

        Assert.assertNull(StringUtils.stripEnd(null, "a"));
        Assert.assertEquals("", StringUtils.stripEnd("", "a"));
        Assert.assertEquals("abc", StringUtils.stripEnd("abc  ", null));
        Assert.assertEquals("abc", StringUtils.stripEnd("abc", ""));
        Assert.assertEquals("abc", StringUtils.stripEnd("abcxx", "x"));
        Assert.assertEquals("", StringUtils.stripEnd("xxx", "x"));
    }

    @Test
    public void testStripAll() {
        Assert.assertNull(StringUtils.stripAll((String[]) null));
        Assert.assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
        Assert.assertArrayEquals(new String[]{"abc", "def", null}, StringUtils.stripAll(" abc ", "def ", null));
        Assert.assertArrayEquals(new String[]{"bc", "ef", null}, StringUtils.stripAll(new String[]{"abc", "def", null}, "ad"));
    }

    @Test
    public void testStripAccents() {
        Assert.assertNull(StringUtils.stripAccents(null));
        Assert.assertEquals("", StringUtils.stripAccents(""));
        Assert.assertEquals("control", StringUtils.stripAccents("control"));
        Assert.assertEquals("eclair", StringUtils.stripAccents("\u00e9clair"));
    }

    @Test
    public void testEqualsAndEqualsIgnoreCase() {
        Assert.assertTrue(StringUtils.equals(null, null));
        Assert.assertFalse(StringUtils.equals(null, "abc"));
        Assert.assertFalse(StringUtils.equals("abc", null));
        Assert.assertTrue(StringUtils.equals("abc", "abc"));
        Assert.assertFalse(StringUtils.equals("abc", "ABC"));

        Assert.assertTrue(StringUtils.equalsIgnoreCase(null, null));
        Assert.assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        Assert.assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        Assert.assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        Assert.assertFalse(StringUtils.equalsIgnoreCase("abc", "abcd"));
    }

    @Test
    public void testIndexOfCharAndString() {
        Assert.assertEquals(-1, StringUtils.indexOf(null, 'a'));
        Assert.assertEquals(-1, StringUtils.indexOf("", 'a'));
        Assert.assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        Assert.assertEquals(-1, StringUtils.indexOf("aabaabaa", 'z'));

        Assert.assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        Assert.assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        Assert.assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        Assert.assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));

        Assert.assertEquals(-1, StringUtils.indexOf(null, "a"));
        Assert.assertEquals(-1, StringUtils.indexOf("a", null));
        Assert.assertEquals(0, StringUtils.indexOf("", ""));
        Assert.assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        Assert.assertEquals(0, StringUtils.indexOf("aabaabaa", ""));

        Assert.assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.indexOf("a", null, 0));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", "", 2));
        Assert.assertEquals(3, StringUtils.indexOf("abc", "", 9));
        Assert.assertEquals(-1, StringUtils.indexOf("abc", "d", 0));
    }

    @Test
    public void testOrdinalIndexOf() {
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf("a", null, 1));
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf("a", "a", 0));
        Assert.assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));
        Assert.assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        Assert.assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        Assert.assertEquals(3, StringUtils.ordinalIndexOf("aabaabaa", "a", 3));
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", 10));

        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "a", 1));
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", null, 1));
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", "a", 0));
        Assert.assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1));
        Assert.assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        Assert.assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
        Assert.assertEquals(4, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 3));
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 10));
    }

    @Test
    public void testIndexOfIgnoreCase() {
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("a", null));
        Assert.assertEquals(0, StringUtils.indexOfIgnoreCase("", ""));
        Assert.assertEquals(1, StringUtils.indexOfIgnoreCase("aABaabaa", "ab"));

        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("a", null, 0));
        Assert.assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9));
        Assert.assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "", 2));
        Assert.assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "Z", 0));
    }

    @Test
    public void testLastIndexOf() {
        Assert.assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        Assert.assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        Assert.assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));

        Assert.assertEquals(-1, StringUtils.lastIndexOf(null, 'a', 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("", 'a', 0));
        Assert.assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        Assert.assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', -1));

        Assert.assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("a", null));
        Assert.assertEquals(0, StringUtils.lastIndexOf("", ""));
        Assert.assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
        Assert.assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));

        Assert.assertEquals(-1, StringUtils.lastIndexOf(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("a", null, 0));
        Assert.assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 9));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));
        Assert.assertEquals(0, StringUtils.lastIndexOf("aabaabaa", "a", 0));
    }

    @Test
    public void testLastIndexOfIgnoreCase() {
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a"));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null));
        Assert.assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaABaa", "AB"));

        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null, 0));
        Assert.assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        Assert.assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "", 2));
        Assert.assertEquals(0, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0));
    }

    @Test
    public void testContainsAndContainsIgnoreCaseAndWhitespace() {
        Assert.assertFalse(StringUtils.contains(null, 'a'));
        Assert.assertFalse(StringUtils.contains("", 'a'));
        Assert.assertTrue(StringUtils.contains("abc", 'b'));
        Assert.assertFalse(StringUtils.contains("abc", 'z'));

        Assert.assertFalse(StringUtils.contains(null, "a"));
        Assert.assertFalse(StringUtils.contains("a", null));
        Assert.assertTrue(StringUtils.contains("", ""));
        Assert.assertTrue(StringUtils.contains("abc", "b"));
        Assert.assertFalse(StringUtils.contains("abc", "z"));

        Assert.assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        Assert.assertFalse(StringUtils.containsIgnoreCase("a", null));
        Assert.assertTrue(StringUtils.containsIgnoreCase("", ""));
        Assert.assertTrue(StringUtils.containsIgnoreCase("abc", "B"));
        Assert.assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
        Assert.assertFalse(StringUtils.containsIgnoreCase("a", "abc"));

        Assert.assertFalse(StringUtils.containsWhitespace(null));
        Assert.assertFalse(StringUtils.containsWhitespace(""));
        Assert.assertFalse(StringUtils.containsWhitespace("abc"));
        Assert.assertTrue(StringUtils.containsWhitespace("a b c"));
    }

    @Test
    public void testIndexOfAnyCharsAndString() {
        Assert.assertEquals(-1, StringUtils.indexOfAny(null, 'a'));
        Assert.assertEquals(-1, StringUtils.indexOfAny("", 'a'));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", new char[0]));
        Assert.assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        Assert.assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", 'b', 'y'));
        Assert.assertEquals(-1, StringUtils.indexOfAny("aba", 'z'));

        Assert.assertEquals(-1, StringUtils.indexOfAny(null, "a"));
        Assert.assertEquals(-1, StringUtils.indexOfAny("", "a"));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", (String) null));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        Assert.assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));

        String highSurrogate = "\uD83D\uDE00";
        Assert.assertEquals(0, StringUtils.indexOfAny(highSurrogate, '\uD83D', '\uDE00'));
        Assert.assertEquals(0, StringUtils.indexOfAny(highSurrogate, '\uD83D'));
    }

    @Test
    public void testContainsAny() {
        Assert.assertFalse(StringUtils.containsAny(null, 'a'));
        Assert.assertFalse(StringUtils.containsAny("", 'a'));
        Assert.assertFalse(StringUtils.containsAny("abc", (char[]) null));
        Assert.assertFalse(StringUtils.containsAny("abc", new char[0]));
        Assert.assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        Assert.assertTrue(StringUtils.containsAny("zzabyycdxx", 'b', 'y'));
        Assert.assertFalse(StringUtils.containsAny("aba", 'z'));

        Assert.assertFalse(StringUtils.containsAny(null, "a"));
        Assert.assertFalse(StringUtils.containsAny("a", (CharSequence) null));
        Assert.assertFalse(StringUtils.containsAny("a", ""));
        Assert.assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));

        String highSurrogate = "\uD83D\uDE00";
        Assert.assertTrue(StringUtils.containsAny(highSurrogate, '\uD83D', '\uDE00'));
        Assert.assertTrue(StringUtils.containsAny(highSurrogate, '\uD83D'));
        Assert.assertFalse(StringUtils.containsAny(highSurrogate, '\uD83D', 'z'));
    }

    @Test
    public void testIndexOfAnyButCharsAndString() {
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut(null, 'a'));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("", 'a'));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", (char[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", new char[0]));
        Assert.assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", 'z', 'a'));
        Assert.assertEquals(0, StringUtils.indexOfAnyBut("aba", 'z'));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("aba", 'a', 'b'));

        Assert.assertEquals(-1, StringUtils.indexOfAnyBut(null, "a"));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("", "a"));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", (CharSequence) null));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", ""));
        Assert.assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));

        String highSurrogate = "\uD83D\uDE00";
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut(highSurrogate, '\uD83D', '\uDE00'));
        Assert.assertEquals(0, StringUtils.indexOfAnyBut(highSurrogate, "z"));
        Assert.assertEquals(0, StringUtils.indexOfAnyBut(highSurrogate + "a", "\uD83D\uDE01"));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut(highSurrogate, highSurrogate));
    }

    @Test
    public void testContainsOnlyAndNone() {
        Assert.assertFalse(StringUtils.containsOnly(null, 'a'));
        Assert.assertFalse(StringUtils.containsOnly("a", (char[]) null));
        Assert.assertTrue(StringUtils.containsOnly("", 'a'));
        Assert.assertFalse(StringUtils.containsOnly("ab", new char[0]));
        Assert.assertTrue(StringUtils.containsOnly("abab", 'a', 'b', 'c'));
        Assert.assertFalse(StringUtils.containsOnly("ab1", 'a', 'b', 'c'));

        Assert.assertFalse(StringUtils.containsOnly(null, "a"));
        Assert.assertFalse(StringUtils.containsOnly("a", (String) null));
        Assert.assertTrue(StringUtils.containsOnly("", "a"));
        Assert.assertFalse(StringUtils.containsOnly("ab", ""));
        Assert.assertTrue(StringUtils.containsOnly("abab", "abc"));

        Assert.assertTrue(StringUtils.containsNone(null, 'a'));
        Assert.assertTrue(StringUtils.containsNone("a", (char[]) null));
        Assert.assertTrue(StringUtils.containsNone("", 'a'));
        Assert.assertTrue(StringUtils.containsNone("ab", new char[0]));
        Assert.assertTrue(StringUtils.containsNone("abab", 'x', 'y', 'z'));
        Assert.assertFalse(StringUtils.containsNone("abz", 'x', 'y', 'z'));

        String highSurrogate = "\uD83D\uDE00";
        Assert.assertFalse(StringUtils.containsNone(highSurrogate, '\uD83D'));
        Assert.assertFalse(StringUtils.containsNone(highSurrogate, '\uD83D', '\uDE00'));
        Assert.assertTrue(StringUtils.containsNone(highSurrogate, '\uD83D', 'a'));

        Assert.assertTrue(StringUtils.containsNone(null, "a"));
        Assert.assertTrue(StringUtils.containsNone("a", (String) null));
        Assert.assertTrue(StringUtils.containsNone("", "a"));
        Assert.assertTrue(StringUtils.containsNone("ab", ""));
        Assert.assertTrue(StringUtils.containsNone("abab", "xyz"));
        Assert.assertFalse(StringUtils.containsNone("abz", "xyz"));
    }

    @Test
    public void testIndexOfAnyAndLastIndexOfAnyStrings() {
        Assert.assertEquals(-1, StringUtils.indexOfAny(null, "ab"));
        Assert.assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (CharSequence[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new CharSequence[0]));
        Assert.assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", (CharSequence) null, "ab", "cd"));
        Assert.assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", "mn", "op"));

        Assert.assertEquals(-1, StringUtils.lastIndexOfAny(null, "ab"));
        Assert.assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", (CharSequence[]) null));
        Assert.assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new CharSequence[0]));
        Assert.assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new CharSequence[]{null}));
        Assert.assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", "ab", "cd"));
        Assert.assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", ""));
        Assert.assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", "op"));
    }

    @Test
    public void testSubstring() {
        Assert.assertNull(StringUtils.substring(null, 0));
        Assert.assertEquals("", StringUtils.substring("", 0));
        Assert.assertEquals("c", StringUtils.substring("abc", 2));
        Assert.assertEquals("", StringUtils.substring("abc", 4));
        Assert.assertEquals("bc", StringUtils.substring("abc", -2));
        Assert.assertEquals("abc", StringUtils.substring("abc", -4));

        Assert.assertNull(StringUtils.substring(null, 0, 1));
        Assert.assertEquals("", StringUtils.substring("", 0, 1));
        Assert.assertEquals("ab", StringUtils.substring("abc", 0, 2));
        Assert.assertEquals("", StringUtils.substring("abc", 2, 0));
        Assert.assertEquals("c", StringUtils.substring("abc", 2, 4));
        Assert.assertEquals("", StringUtils.substring("abc", 4, 6));
        Assert.assertEquals("b", StringUtils.substring("abc", -2, -1));
        Assert.assertEquals("ab", StringUtils.substring("abc", -4, 2));
        Assert.assertEquals("", StringUtils.substring("abc", -1, -2));
    }

    @Test
    public void testLeftRightMid() {
        Assert.assertNull(StringUtils.left(null, 1));
        Assert.assertEquals("", StringUtils.left("abc", -1));
        Assert.assertEquals("abc", StringUtils.left("abc", 4));
        Assert.assertEquals("ab", StringUtils.left("abc", 2));

        Assert.assertNull(StringUtils.right(null, 1));
        Assert.assertEquals("", StringUtils.right("abc", -1));
        Assert.assertEquals("abc", StringUtils.right("abc", 4));
        Assert.assertEquals("bc", StringUtils.right("abc", 2));

        Assert.assertNull(StringUtils.mid(null, 0, 1));
        Assert.assertEquals("", StringUtils.mid("abc", 0, -1));
        Assert.assertEquals("", StringUtils.mid("abc", 4, 1));
        Assert.assertEquals("ab", StringUtils.mid("abc", -2, 2));
        Assert.assertEquals("bc", StringUtils.mid("abc", 1, 4));
        Assert.assertEquals("b", StringUtils.mid("abc", 1, 1));
    }

    @Test
    public void testSubstringBeforeAndAfter() {
        Assert.assertNull(StringUtils.substringBefore(null, "a"));
        Assert.assertEquals("", StringUtils.substringBefore("", "a"));
        Assert.assertEquals("abc", StringUtils.substringBefore("abc", null));
        Assert.assertEquals("", StringUtils.substringBefore("abc", ""));
        Assert.assertEquals("ab", StringUtils.substringBefore("abc", "c"));
        Assert.assertEquals("abc", StringUtils.substringBefore("abc", "d"));

        Assert.assertNull(StringUtils.substringAfter(null, "a"));
        Assert.assertEquals("", StringUtils.substringAfter("", "a"));
        Assert.assertEquals("", StringUtils.substringAfter("abc", null));
        Assert.assertEquals("abc", StringUtils.substringAfter("abc", ""));
        Assert.assertEquals("bc", StringUtils.substringAfter("abc", "a"));
        Assert.assertEquals("", StringUtils.substringAfter("abc", "d"));

        Assert.assertNull(StringUtils.substringBeforeLast(null, "a"));
        Assert.assertEquals("", StringUtils.substringBeforeLast("", "a"));
        Assert.assertEquals("abc", StringUtils.substringBeforeLast("abc", null));
        Assert.assertEquals("abc", StringUtils.substringBeforeLast("abc", ""));
        Assert.assertEquals("ab", StringUtils.substringBeforeLast("abcba", "c"));
        Assert.assertEquals("abcba", StringUtils.substringBeforeLast("abcba", "d"));

        Assert.assertNull(StringUtils.substringAfterLast(null, "a"));
        Assert.assertEquals("", StringUtils.substringAfterLast("", "a"));
        Assert.assertEquals("", StringUtils.substringAfterLast("abc", null));
        Assert.assertEquals("", StringUtils.substringAfterLast("abc", ""));
        Assert.assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        Assert.assertEquals("", StringUtils.substringAfterLast("abcba", "d"));
        Assert.assertEquals("", StringUtils.substringAfterLast("abc", "c"));
    }

    @Test
    public void testSubstringBetween() {
        Assert.assertNull(StringUtils.substringBetween(null, "a"));
        Assert.assertNull(StringUtils.substringBetween("abc", null));
        Assert.assertEquals("b", StringUtils.substringBetween("aba", "a"));

        Assert.assertNull(StringUtils.substringBetween(null, "a", "b"));
        Assert.assertNull(StringUtils.substringBetween("abc", null, "b"));
        Assert.assertNull(StringUtils.substringBetween("abc", "a", null));
        Assert.assertEquals("", StringUtils.substringBetween("", "", ""));
        Assert.assertNull(StringUtils.substringBetween("", "", "]"));
        Assert.assertNull(StringUtils.substringBetween("wx[b]yz", "[", "x"));
        Assert.assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));

        Assert.assertNull(StringUtils.substringsBetween(null, "[", "]"));
        Assert.assertNull(StringUtils.substringsBetween("[a]", null, "]"));
        Assert.assertNull(StringUtils.substringsBetween("[a]", "[", null));
        Assert.assertNull(StringUtils.substringsBetween("[a]", "", "]"));
        Assert.assertNull(StringUtils.substringsBetween("[a]", "[", ""));
        Assert.assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        Assert.assertArrayEquals(new String[]{"a", "b"}, StringUtils.substringsBetween("[a][b]", "[", "]"));
        Assert.assertNull(StringUtils.substringsBetween("abc", "[", "]"));
    }

    @Test
    public void testSplit() {
        Assert.assertNull(StringUtils.split(null));
        Assert.assertArrayEquals(new String[0], StringUtils.split(""));
        Assert.assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a:b:c", ":"));
        Assert.assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a b c", null, 0));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a;b:c", ";:", 0));
    }

    @Test
    public void testSplitPreserveAllTokens() {
        Assert.assertNull(StringUtils.splitPreserveAllTokens(null));
        Assert.assertArrayEquals(new String[0], StringUtils.splitPreserveAllTokens(""));
        Assert.assertArrayEquals(new String[]{"", "a", "", "b", ""}, StringUtils.splitPreserveAllTokens(" a  b "));
        Assert.assertArrayEquals(new String[]{"a", "", "b"}, StringUtils.splitPreserveAllTokens("a..b", '.'));
        Assert.assertArrayEquals(new String[]{"a", "", "b"}, StringUtils.splitPreserveAllTokens("a::b", ":"));
        Assert.assertArrayEquals(new String[]{"a", ":b::"}, StringUtils.splitPreserveAllTokens("a::b::", ":", 2));
        Assert.assertArrayEquals(new String[]{"a", "", "b", " c"}, StringUtils.splitPreserveAllTokens("a  b  c", null, 4));
        Assert.assertArrayEquals(new String[]{"a", "", "b", ":c"}, StringUtils.splitPreserveAllTokens("a;;b::c", ";:", 4));
    }

    @Test
    public void testSplitByWholeSeparator() {
        Assert.assertNull(StringUtils.splitByWholeSeparator(null, "::"));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", "::"));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab::cd::ef", "::"));
        Assert.assertArrayEquals(new String[]{"ab", "cd::ef"}, StringUtils.splitByWholeSeparator("ab::cd::ef", "::", 2));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab  cd ef", null));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab  cd ef", ""));

        Assert.assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, "::"));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByWholeSeparatorPreserveAllTokens("", "::"));
        Assert.assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab::::cd", "::"));
        Assert.assertArrayEquals(new String[]{"ab", "::cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab::::cd", "::", 2));
        Assert.assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab  cd", null));
    }

    @Test
    public void testSplitByCharacterType() {
        Assert.assertNull(StringUtils.splitByCharacterType(null));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
        Assert.assertArrayEquals(new String[]{"ab", " ", "de", " ", "5"}, StringUtils.splitByCharacterType("ab de 5"));
        Assert.assertArrayEquals(new String[]{"foo", "B", "ar"}, StringUtils.splitByCharacterType("fooBar"));

        Assert.assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByCharacterTypeCamelCase(""));
        Assert.assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        Assert.assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    @Test
    public void testJoin() {
        Assert.assertNull(StringUtils.join((Object[]) null));
        Assert.assertEquals("", StringUtils.join(new Object[0]));
        Assert.assertEquals("abc", StringUtils.join("a", "b", "c"));
        Assert.assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        Assert.assertEquals("", StringUtils.join(new Object[]{"a", "b"}, ';', 1, 1));
        Assert.assertEquals("a--b", StringUtils.join(new Object[]{"a", "b"}, "--"));
        Assert.assertEquals("a--b", StringUtils.join(new Object[]{"a", "b"}, "--", 0, 2));
        Assert.assertEquals("ab", StringUtils.join(new Object[]{"a", "b"}, (String) null, 0, 2));

        Assert.assertNull(StringUtils.join((Iterator<?>) null, ';'));
        Assert.assertEquals("", StringUtils.join(Collections.emptyIterator(), ';'));
        Assert.assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ';'));
        Assert.assertEquals("a;b", StringUtils.join(Arrays.asList("a", "b").iterator(), ';'));

        Assert.assertNull(StringUtils.join((Iterator<?>) null, ","));
        Assert.assertEquals("", StringUtils.join(Collections.emptyIterator(), ","));
        Assert.assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ","));
        Assert.assertEquals("a,b", StringUtils.join(Arrays.asList("a", "b").iterator(), ","));
        Assert.assertEquals("ab", StringUtils.join(Arrays.asList("a", "b").iterator(), (String) null));

        Assert.assertNull(StringUtils.join((Iterable<?>) null, ';'));
        Assert.assertEquals("a;b", StringUtils.join(Arrays.asList("a", "b"), ';'));
        Assert.assertNull(StringUtils.join((Iterable<?>) null, ","));
        Assert.assertEquals("a,b", StringUtils.join(Arrays.asList("a", "b"), ","));
    }

    @Test
    public void testDeleteWhitespace() {
        Assert.assertNull(StringUtils.deleteWhitespace(null));
        Assert.assertEquals("", StringUtils.deleteWhitespace(""));
        Assert.assertEquals("abc", StringUtils.deleteWhitespace(" a b c "));
        Assert.assertEquals("abc", StringUtils.deleteWhitespace("abc"));
    }

    @Test
    public void testRemove() {
        Assert.assertNull(StringUtils.removeStart(null, "a"));
        Assert.assertEquals("", StringUtils.removeStart("", "a"));
        Assert.assertEquals("abc", StringUtils.removeStart("abc", null));
        Assert.assertEquals("bc", StringUtils.removeStart("abc", "a"));
        Assert.assertEquals("abc", StringUtils.removeStart("abc", "z"));

        Assert.assertNull(StringUtils.removeStartIgnoreCase(null, "a"));
        Assert.assertEquals("", StringUtils.removeStartIgnoreCase("", "a"));
        Assert.assertEquals("abc", StringUtils.removeStartIgnoreCase("abc", null));
        Assert.assertEquals("bc", StringUtils.removeStartIgnoreCase("Abc", "a"));
        Assert.assertEquals("Abc", StringUtils.removeStartIgnoreCase("Abc", "z"));

        Assert.assertNull(StringUtils.removeEnd(null, "a"));
        Assert.assertEquals("", StringUtils.removeEnd("", "a"));
        Assert.assertEquals("abc", StringUtils.removeEnd("abc", null));
        Assert.assertEquals("ab", StringUtils.removeEnd("abc", "c"));
        Assert.assertEquals("abc", StringUtils.removeEnd("abc", "z"));

        Assert.assertNull(StringUtils.removeEndIgnoreCase(null, "a"));
        Assert.assertEquals("", StringUtils.removeEndIgnoreCase("", "a"));
        Assert.assertEquals("abc", StringUtils.removeEndIgnoreCase("abc", null));
        Assert.assertEquals("ab", StringUtils.removeEndIgnoreCase("abC", "c"));
        Assert.assertEquals("abC", StringUtils.removeEndIgnoreCase("abC", "z"));

        Assert.assertNull(StringUtils.remove(null, "a"));
        Assert.assertEquals("", StringUtils.remove("", "a"));
        Assert.assertEquals("abc", StringUtils.remove("abc", null));
        Assert.assertEquals("abc", StringUtils.remove("abc", ""));
        Assert.assertEquals("ac", StringUtils.remove("abcba", "b"));

        Assert.assertNull(StringUtils.remove(null, 'a'));
        Assert.assertEquals("", StringUtils.remove("", 'a'));
        Assert.assertEquals("abc", StringUtils.remove("abc", 'z'));
        Assert.assertEquals("ac", StringUtils.remove("abcba", 'b'));
    }

    @Test
    public void testReplace() {
        Assert.assertNull(StringUtils.replaceOnce(null, "a", "b"));
        Assert.assertEquals("bba", StringUtils.replaceOnce("aba", "a", "b"));

        Assert.assertNull(StringUtils.replace(null, "a", "b"));
        Assert.assertEquals("bbb", StringUtils.replace("aba", "a", "b"));

        Assert.assertNull(StringUtils.replace(null, "a", "b", 1));
        Assert.assertEquals("", StringUtils.replace("", "a", "b", 1));
        Assert.assertEquals("aba", StringUtils.replace("aba", null, "b", 1));
        Assert.assertEquals("aba", StringUtils.replace("aba", "a", null, 1));
        Assert.assertEquals("aba", StringUtils.replace("aba", "a", "b", 0));
        Assert.assertEquals("aba", StringUtils.replace("aba", "z", "b", 1));
        Assert.assertEquals("bba", StringUtils.replace("aba", "a", "b", 1));
        Assert.assertEquals("bbb", StringUtils.replace("aba", "a", "b", 2));
    }

    @Test
    public void testReplaceEach() {
        Assert.assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        Assert.assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", null, null));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[0], null));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, new String[0]));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null}, new String[]{"x"}));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"z"}, new String[]{"x"}));
        Assert.assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        Assert.assertEquals("dcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));

        Assert.assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        Assert.assertNull(StringUtils.replaceEachRepeatedly(null, new String[]{"a"}, new String[]{"b"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEachMismatchedArrays() {
        StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{"b", "c"});
    }

    @Test(expected = IllegalStateException.class)
    public void testReplaceEachLoop() {
        StringUtils.replaceEachRepeatedly("a", new String[]{"a"}, new String[]{"a"});
    }

    @Test
    public void testReplaceChars() {
        Assert.assertNull(StringUtils.replaceChars(null, 'a', 'b'));
        Assert.assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));

        Assert.assertNull(StringUtils.replaceChars(null, "a", "b"));
        Assert.assertEquals("", StringUtils.replaceChars("", "a", "b"));
        Assert.assertEquals("abc", StringUtils.replaceChars("abc", null, "b"));
        Assert.assertEquals("abc", StringUtils.replaceChars("abc", "", "b"));
        Assert.assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        Assert.assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        Assert.assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        Assert.assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));
        Assert.assertEquals("abcba", StringUtils.replaceChars("abcba", "z", "y"));
    }

    @Test
    public void testOverlay() {
        Assert.assertNull(StringUtils.overlay(null, "abc", 0, 0));
        Assert.assertEquals("abc", StringUtils.overlay("", "abc", 0, 0));
        Assert.assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
        Assert.assertEquals("abef", StringUtils.overlay("abcdef", "", 4, 2));
        Assert.assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        Assert.assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        Assert.assertEquals("abzzzz", StringUtils.overlay("abcdef", "zzzz", 2, 8));
        Assert.assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        Assert.assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
    }

    @Test
    public void testChompAndChop() {
        Assert.assertNull(StringUtils.chomp(null));
        Assert.assertEquals("", StringUtils.chomp(""));
        Assert.assertEquals("", StringUtils.chomp("\r"));
        Assert.assertEquals("", StringUtils.chomp("\n"));
        Assert.assertEquals("", StringUtils.chomp("\r\n"));
        Assert.assertEquals("a", StringUtils.chomp("a"));
        Assert.assertEquals("abc", StringUtils.chomp("abc\r\n"));
        Assert.assertEquals("abc\n", StringUtils.chomp("abc\n\r"));
        Assert.assertEquals("abc", StringUtils.chomp("abc\r"));

        Assert.assertNull(StringUtils.chomp(null, "a"));
        Assert.assertEquals("", StringUtils.chomp("", "a"));
        Assert.assertEquals("foo", StringUtils.chomp("foo", null));
        Assert.assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        Assert.assertEquals("foobar", StringUtils.chomp("foobar", "baz"));

        Assert.assertNull(StringUtils.chop(null));
        Assert.assertEquals("", StringUtils.chop(""));
        Assert.assertEquals("", StringUtils.chop("a"));
        Assert.assertEquals("", StringUtils.chop("\r\n"));
        Assert.assertEquals("abc", StringUtils.chop("abc\r\n"));
        Assert.assertEquals("abc", StringUtils.chop("abcd"));
    }

    @Test
    public void testRepeat() {
        Assert.assertNull(StringUtils.repeat(null, 2));
        Assert.assertEquals("", StringUtils.repeat("a", -1));
        Assert.assertEquals("a", StringUtils.repeat("a", 1));
        Assert.assertEquals("aaa", StringUtils.repeat("a", 3));
        Assert.assertEquals("abab", StringUtils.repeat("ab", 2));
        Assert.assertEquals("abcabc", StringUtils.repeat("abc", 2));

        Assert.assertNull(StringUtils.repeat(null, ",", 2));
        Assert.assertEquals("aaa", StringUtils.repeat("a", null, 3));
        Assert.assertEquals("a, a, a", StringUtils.repeat("a", ", ", 3));

        Assert.assertEquals("", StringUtils.repeat('e', -1));
        Assert.assertEquals("eee", StringUtils.repeat('e', 3));
    }

    @Test
    public void testPadAndCenter() {
        Assert.assertNull(StringUtils.rightPad(null, 5));
        Assert.assertEquals("bat", StringUtils.rightPad("bat", 2));
        Assert.assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        Assert.assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        Assert.assertEquals("bat  ", StringUtils.rightPad("bat", 5, (String) null));
        Assert.assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        Assert.assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        Assert.assertEquals("baty", StringUtils.rightPad("bat", 4, "yz"));

        Assert.assertNull(StringUtils.leftPad(null, 5));
        Assert.assertEquals("bat", StringUtils.leftPad("bat", 2));
        Assert.assertEquals("  bat", StringUtils.leftPad("bat", 5));
        Assert.assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        Assert.assertEquals("  bat", StringUtils.leftPad("bat", 5, (String) null));
        Assert.assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        Assert.assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        Assert.assertEquals("ybat", StringUtils.leftPad("bat", 4, "yz"));

        Assert.assertNull(StringUtils.center(null, 5));
        Assert.assertEquals("bat", StringUtils.center("bat", -1));
        Assert.assertEquals("bat", StringUtils.center("bat", 2));
        Assert.assertEquals(" bat ", StringUtils.center("bat", 5));
        Assert.assertEquals("zbatzz", StringUtils.center("bat", 6, 'z'));
        Assert.assertEquals("  bat  ", StringUtils.center("bat", 7, (String) null));
        Assert.assertEquals("yabcz", StringUtils.center("abc", 5, "yz"));
    }

    @Test
    public void testLength() {
        Assert.assertEquals(0, StringUtils.length(null));
        Assert.assertEquals(3, StringUtils.length("abc"));
    }

    @Test
    public void testCaseConversions() {
        Assert.assertNull(StringUtils.upperCase(null));
        Assert.assertEquals("ABC", StringUtils.upperCase("aBc"));
        Assert.assertNull(StringUtils.upperCase(null, Locale.ENGLISH));
        Assert.assertEquals("ABC", StringUtils.upperCase("aBc", Locale.ENGLISH));

        Assert.assertNull(StringUtils.lowerCase(null));
        Assert.assertEquals("abc", StringUtils.lowerCase("aBc"));
        Assert.assertNull(StringUtils.lowerCase(null, Locale.ENGLISH));
        Assert.assertEquals("abc", StringUtils.lowerCase("aBc", Locale.ENGLISH));

        Assert.assertNull(StringUtils.capitalize(null));
        Assert.assertEquals("", StringUtils.capitalize(""));
        Assert.assertEquals("Cat", StringUtils.capitalize("cat"));

        Assert.assertNull(StringUtils.uncapitalize(null));
        Assert.assertEquals("", StringUtils.uncapitalize(""));
        Assert.assertEquals("cat", StringUtils.uncapitalize("Cat"));

        Assert.assertNull(StringUtils.swapCase(null));
        Assert.assertEquals("", StringUtils.swapCase(""));
        Assert.assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    @Test
    public void testCountMatches() {
        Assert.assertEquals(0, StringUtils.countMatches(null, "a"));
        Assert.assertEquals(0, StringUtils.countMatches("a", null));
        Assert.assertEquals(0, StringUtils.countMatches("a", ""));
        Assert.assertEquals(2, StringUtils.countMatches("abba", "a"));
        Assert.assertEquals(1, StringUtils.countMatches("abba", "ab"));
        Assert.assertEquals(0, StringUtils.countMatches("abba", "z"));
    }

    @Test
    public void testCharacterTypeChecks() {
        Assert.assertFalse(StringUtils.isAlpha(null));
        Assert.assertFalse(StringUtils.isAlpha(""));
        Assert.assertTrue(StringUtils.isAlpha("abc"));
        Assert.assertFalse(StringUtils.isAlpha("ab1c"));

        Assert.assertFalse(StringUtils.isAlphaSpace(null));
        Assert.assertTrue(StringUtils.isAlphaSpace(""));
        Assert.assertTrue(StringUtils.isAlphaSpace("ab c"));
        Assert.assertFalse(StringUtils.isAlphaSpace("ab1 c"));

        Assert.assertFalse(StringUtils.isAlphanumeric(null));
        Assert.assertFalse(StringUtils.isAlphanumeric(""));
        Assert.assertTrue(StringUtils.isAlphanumeric("ab1c"));
        Assert.assertFalse(StringUtils.isAlphanumeric("ab 1c"));

        Assert.assertFalse(StringUtils.isAlphanumericSpace(null));
        Assert.assertTrue(StringUtils.isAlphanumericSpace(""));
        Assert.assertTrue(StringUtils.isAlphanumericSpace("ab 1c"));
        Assert.assertFalse(StringUtils.isAlphanumericSpace("ab-1c"));

        Assert.assertFalse(StringUtils.isAsciiPrintable(null));
        Assert.assertTrue(StringUtils.isAsciiPrintable(""));
        Assert.assertTrue(StringUtils.isAsciiPrintable("!ab-c~ "));
        Assert.assertFalse(StringUtils.isAsciiPrintable("\u007f"));

        Assert.assertFalse(StringUtils.isNumeric(null));
        Assert.assertFalse(StringUtils.isNumeric(""));
        Assert.assertTrue(StringUtils.isNumeric("123"));
        Assert.assertFalse(StringUtils.isNumeric("12 3"));

        Assert.assertFalse(StringUtils.isNumericSpace(null));
        Assert.assertTrue(StringUtils.isNumericSpace(""));
        Assert.assertTrue(StringUtils.isNumericSpace("12 3"));
        Assert.assertFalse(StringUtils.isNumericSpace("12a3"));

        Assert.assertFalse(StringUtils.isWhitespace(null));
        Assert.assertTrue(StringUtils.isWhitespace(""));
        Assert.assertTrue(StringUtils.isWhitespace("   \t\n"));
        Assert.assertFalse(StringUtils.isWhitespace("  a  "));

        Assert.assertFalse(StringUtils.isAllLowerCase(null));
        Assert.assertFalse(StringUtils.isAllLowerCase(""));
        Assert.assertTrue(StringUtils.isAllLowerCase("abc"));
        Assert.assertFalse(StringUtils.isAllLowerCase("aBc"));

        Assert.assertFalse(StringUtils.isAllUpperCase(null));
        Assert.assertFalse(StringUtils.isAllUpperCase(""));
        Assert.assertTrue(StringUtils.isAllUpperCase("ABC"));
        Assert.assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    @Test
    public void testDefaults() {
        Assert.assertEquals("", StringUtils.defaultString(null));
        Assert.assertEquals("bat", StringUtils.defaultString("bat"));

        Assert.assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        Assert.assertEquals("bat", StringUtils.defaultString("bat", "NULL"));

        Assert.assertEquals("NULL", StringUtils.defaultIfBlank(null, "NULL"));
        Assert.assertEquals("NULL", StringUtils.defaultIfBlank("   ", "NULL"));
        Assert.assertEquals("bat", StringUtils.defaultIfBlank("bat", "NULL"));

        Assert.assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        Assert.assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        Assert.assertEquals("   ", StringUtils.defaultIfEmpty("   ", "NULL"));
        Assert.assertEquals("bat", StringUtils.defaultIfEmpty("bat", "NULL"));
    }

    @Test
    public void testReverse() {
        Assert.assertNull(StringUtils.reverse(null));
        Assert.assertEquals("", StringUtils.reverse(""));
        Assert.assertEquals("tab", StringUtils.reverse("bat"));

        Assert.assertNull(StringUtils.reverseDelimited(null, '.'));
        Assert.assertEquals("", StringUtils.reverseDelimited("", '.'));
        Assert.assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
    }

    @Test
    public void testAbbreviate() {
        Assert.assertNull(StringUtils.abbreviate(null, 4));
        Assert.assertEquals("", StringUtils.abbreviate("", 4));
        Assert.assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        Assert.assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        Assert.assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        Assert.assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        Assert.assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));

        Assert.assertNull(StringUtils.abbreviateMiddle(null, ".", 4));
        Assert.assertEquals("abc", StringUtils.abbreviateMiddle("abc", null, 4));
        Assert.assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 4));
        Assert.assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
        Assert.assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateSmallWidth() {
        StringUtils.abbreviate("abcdef", 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateSmallWidthWithOffset() {
        StringUtils.abbreviate("abcdefghijklmno", 5, 6);
    }

    @Test
    public void testDifferenceAndCommonPrefix() {
        Assert.assertNull(StringUtils.difference(null, null));
        Assert.assertEquals("abc", StringUtils.difference(null, "abc"));
        Assert.assertEquals("abc", StringUtils.difference("abc", null));
        Assert.assertEquals("", StringUtils.difference("abc", "abc"));
        Assert.assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));

        Assert.assertEquals(-1, StringUtils.indexOfDifference(null, null));
        Assert.assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        Assert.assertEquals(0, StringUtils.indexOfDifference("abc", null));
        Assert.assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        Assert.assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
        Assert.assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));

        Assert.assertEquals(-1, StringUtils.indexOfDifference((CharSequence[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfDifference(new CharSequence[0]));
        Assert.assertEquals(-1, StringUtils.indexOfDifference("abc"));
        Assert.assertEquals(-1, StringUtils.indexOfDifference(null, null));
        Assert.assertEquals(0, StringUtils.indexOfDifference("abc", null));
        Assert.assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        Assert.assertEquals(1, StringUtils.indexOfDifference("abc", "a"));
        Assert.assertEquals(2, StringUtils.indexOfDifference("abcde", "abxyz"));
        Assert.assertEquals(-1, StringUtils.indexOfDifference("", ""));

        Assert.assertEquals("", StringUtils.getCommonPrefix((String[]) null));
        Assert.assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        Assert.assertEquals("abc", StringUtils.getCommonPrefix("abc"));
        Assert.assertEquals("", StringUtils.getCommonPrefix(null, null));
        Assert.assertEquals("i am a ", StringUtils.getCommonPrefix("i am a machine", "i am a robot"));
        Assert.assertEquals("", StringUtils.getCommonPrefix("abc", "xyz"));
    }

    @Test
    public void testLevenshteinDistance() {
        Assert.assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        Assert.assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        Assert.assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        Assert.assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant"));

        Assert.assertEquals(0, StringUtils.getLevenshteinDistance("", "", 0));
        Assert.assertEquals(1, StringUtils.getLevenshteinDistance("", "a", 1));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("", "a", 0));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 8));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("aaapppp", "", 6));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo", 7));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("elephant", "hippo", 6));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant", 7));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("hippo", "elephant", 6));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("a", "12345", 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinNull1() {
        StringUtils.getLevenshteinDistance(null, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinNull2() {
        StringUtils.getLevenshteinDistance("", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinThresholdNegative() {
        StringUtils.getLevenshteinDistance("", "", -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinThresholdNull() {
        StringUtils.getLevenshteinDistance(null, "", 1);
    }

    @Test
    public void testStartsWith() {
        Assert.assertTrue(StringUtils.startsWith(null, null));
        Assert.assertFalse(StringUtils.startsWith(null, "abc"));
        Assert.assertFalse(StringUtils.startsWith("abcdef", null));
        Assert.assertTrue(StringUtils.startsWith("abcdef", "abc"));
        Assert.assertFalse(StringUtils.startsWith("abcdef", "def"));
        Assert.assertFalse(StringUtils.startsWith("abc", "abcdef"));

        Assert.assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        Assert.assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        Assert.assertFalse(StringUtils.startsWithIgnoreCase("abcdef", null));
        Assert.assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        Assert.assertFalse(StringUtils.startsWithIgnoreCase("ABCDEF", "def"));

        Assert.assertFalse(StringUtils.startsWithAny(null, "abc"));
        Assert.assertFalse(StringUtils.startsWithAny("abcxyz", (CharSequence[]) null));
        Assert.assertFalse(StringUtils.startsWithAny("abcxyz", new CharSequence[0]));
        Assert.assertTrue(StringUtils.startsWithAny("abcxyz", (CharSequence) null, "xyz", "abc"));
        Assert.assertFalse(StringUtils.startsWithAny("abcxyz", "def", "xyz"));
    }

    @Test
    public void testEndsWith() {
        Assert.assertTrue(StringUtils.endsWith(null, null));
        Assert.assertFalse(StringUtils.endsWith(null, "def"));
        Assert.assertFalse(StringUtils.endsWith("abcdef", null));
        Assert.assertTrue(StringUtils.endsWith("abcdef", "def"));
        Assert.assertFalse(StringUtils.endsWith("abcdef", "abc"));
        Assert.assertFalse(StringUtils.endsWith("def", "abcdef"));

        Assert.assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase("abcdef", null));
        Assert.assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase("ABCDEF", "abc"));

        Assert.assertFalse(StringUtils.endsWithAny(null, "def"));
        Assert.assertFalse(StringUtils.endsWithAny("abcxyz", (CharSequence[]) null));
        Assert.assertFalse(StringUtils.endsWithAny("abcxyz", new CharSequence[0]));
        Assert.assertTrue(StringUtils.endsWithAny("abcxyz", (CharSequence) null, "abc", "xyz"));
        Assert.assertFalse(StringUtils.endsWithAny("abcxyz", "def", "abc"));
    }

    @Test
    public void testNormalizeSpace() {
        Assert.assertNull(StringUtils.normalizeSpace(null));
        Assert.assertEquals("", StringUtils.normalizeSpace(""));
        Assert.assertEquals("a b c", StringUtils.normalizeSpace("  a \t\r\n b   c  "));
    }
}
