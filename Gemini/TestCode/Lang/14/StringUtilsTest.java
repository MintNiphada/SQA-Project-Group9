package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
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
        Assert.assertFalse(StringUtils.isEmpty("bob"));

        Assert.assertFalse(StringUtils.isNotEmpty(null));
        Assert.assertFalse(StringUtils.isNotEmpty(""));
        Assert.assertTrue(StringUtils.isNotEmpty(" "));
        Assert.assertTrue(StringUtils.isNotEmpty("bob"));
    }

    @Test
    public void testIsBlankAndIsNotBlank() {
        Assert.assertTrue(StringUtils.isBlank(null));
        Assert.assertTrue(StringUtils.isBlank(""));
        Assert.assertTrue(StringUtils.isBlank(" \t\r\n "));
        Assert.assertFalse(StringUtils.isBlank("  bob  "));
        Assert.assertFalse(StringUtils.isBlank("a"));

        Assert.assertFalse(StringUtils.isNotBlank(null));
        Assert.assertFalse(StringUtils.isNotBlank(""));
        Assert.assertFalse(StringUtils.isNotBlank("   "));
        Assert.assertTrue(StringUtils.isNotBlank("bob"));
    }

    @Test
    public void testTrimMethods() {
        Assert.assertNull(StringUtils.trim(null));
        Assert.assertEquals("", StringUtils.trim(""));
        Assert.assertEquals("", StringUtils.trim("   "));
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
    public void testStripMethods() {
        Assert.assertNull(StringUtils.strip(null));
        Assert.assertEquals("", StringUtils.strip(""));
        Assert.assertEquals("", StringUtils.strip("   "));
        Assert.assertEquals("abc", StringUtils.strip("  abc  "));
        Assert.assertEquals("a b c", StringUtils.strip("  a b c  "));

        Assert.assertNull(StringUtils.stripToNull(null));
        Assert.assertNull(StringUtils.stripToNull(""));
        Assert.assertNull(StringUtils.stripToNull("   "));
        Assert.assertEquals("abc", StringUtils.stripToNull("  abc  "));

        Assert.assertEquals("", StringUtils.stripToEmpty(null));
        Assert.assertEquals("", StringUtils.stripToEmpty(""));
        Assert.assertEquals("", StringUtils.stripToEmpty("   "));
        Assert.assertEquals("abc", StringUtils.stripToEmpty("  abc  "));

        Assert.assertNull(StringUtils.strip(null, "x"));
        Assert.assertEquals("", StringUtils.strip("", "x"));
        Assert.assertEquals("abc", StringUtils.strip("  abc  ", null));
        Assert.assertEquals("abc", StringUtils.strip("xyzabcxyz", "xyz"));

        Assert.assertNull(StringUtils.stripStart(null, "x"));
        Assert.assertEquals("", StringUtils.stripStart("", "x"));
        Assert.assertEquals("abc", StringUtils.stripStart("abc", ""));
        Assert.assertEquals("abc  ", StringUtils.stripStart("  abc  ", null));
        Assert.assertEquals("abcxyz", StringUtils.stripStart("xyzabcxyz", "xyz"));

        Assert.assertNull(StringUtils.stripEnd(null, "x"));
        Assert.assertEquals("", StringUtils.stripEnd("", "x"));
        Assert.assertEquals("abc", StringUtils.stripEnd("abc", ""));
        Assert.assertEquals("  abc", StringUtils.stripEnd("  abc  ", null));
        Assert.assertEquals("xyzabc", StringUtils.stripEnd("xyzabcxyz", "xyz"));
    }

    @Test
    public void testStripAll() {
        Assert.assertNull(StringUtils.stripAll((String[]) null));
        Assert.assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
        Assert.assertArrayEquals(new String[]{"abc", "def", null}, StringUtils.stripAll("  abc ", " def  ", null));
        Assert.assertArrayEquals(new String[]{"abc", null}, StringUtils.stripAll(new String[]{"xxabcxx", null}, "x"));
    }

    @Test
    public void testStripAccents() {
        Assert.assertNull(StringUtils.stripAccents(null));
        Assert.assertEquals("", StringUtils.stripAccents(""));
        Assert.assertEquals("control", StringUtils.stripAccents("control"));
        Assert.assertEquals("eclair", StringUtils.stripAccents("éclair"));
        Assert.assertEquals("ALNA", StringUtils.stripAccents("ÅLNA"));
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
    public void testIndexOfChar() {
        Assert.assertEquals(-1, StringUtils.indexOf(null, 'a'));
        Assert.assertEquals(-1, StringUtils.indexOf("", 'a'));
        Assert.assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));

        Assert.assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        Assert.assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        Assert.assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        Assert.assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        Assert.assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));
    }

    @Test
    public void testIndexOfString() {
        Assert.assertEquals(-1, StringUtils.indexOf(null, "a"));
        Assert.assertEquals(-1, StringUtils.indexOf("a", (String) null));
        Assert.assertEquals(0, StringUtils.indexOf("", ""));
        Assert.assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));

        Assert.assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.indexOf("a", null, 0));
        Assert.assertEquals(0, StringUtils.indexOf("", "", 0));
        Assert.assertEquals(1, StringUtils.indexOf("aabaabaa", "ab", 0));
        Assert.assertEquals(4, StringUtils.indexOf("aabaabaa", "ab", 2));
        Assert.assertEquals(-1, StringUtils.indexOf("aabaabaa", "ab", 9));
    }

    @Test
    public void testOrdinalIndexOf() {
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf("a", null, 1));
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf("a", "a", 0));
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf("a", "a", -1));
        Assert.assertEquals(0, StringUtils.ordinalIndexOf("", "", 1));
        Assert.assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        Assert.assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        Assert.assertEquals(2, StringUtils.ordinalIndexOf("aabaabaa", "b", 1));
        Assert.assertEquals(5, StringUtils.ordinalIndexOf("aabaabaa", "b", 2));
        Assert.assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "b", 3));
    }

    @Test
    public void testIndexOfIgnoreCase() {
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("a", null));
        Assert.assertEquals(0, StringUtils.indexOfIgnoreCase("", ""));
        Assert.assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        Assert.assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        Assert.assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB"));

        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("a", null, 0));
        Assert.assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 0));
        Assert.assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9));
        Assert.assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1));
        Assert.assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "", 2));
        Assert.assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "Z", 0));
    }

    @Test
    public void testLastIndexOfChar() {
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
    }

    @Test
    public void testLastIndexOfString() {
        Assert.assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("a", (String) null));
        Assert.assertEquals(0, StringUtils.lastIndexOf("", ""));
        Assert.assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a"));
        Assert.assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));

        Assert.assertEquals(-1, StringUtils.lastIndexOf(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("a", null, 0));
        Assert.assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab", 8));
        Assert.assertEquals(1, StringUtils.lastIndexOf("aabaabaa", "ab", 3));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "ab", 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "ab", -1));
    }

    @Test
    public void testLastOrdinalIndexOf() {
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "a", 1));
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", null, 1));
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", "a", 0));
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", "a", -1));
        Assert.assertEquals(0, StringUtils.lastOrdinalIndexOf("", "", 1));
        Assert.assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        Assert.assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
        Assert.assertEquals(5, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 1));
        Assert.assertEquals(2, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 2));
        Assert.assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 3));
        Assert.assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1));
    }

    @Test
    public void testLastIndexOfIgnoreCase() {
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a"));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null));
        Assert.assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        Assert.assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B"));
        Assert.assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB"));

        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a", 0));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null, 0));
        Assert.assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        Assert.assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0));
        Assert.assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "", 2));
        Assert.assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "Z", 5));
    }

    @Test
    public void testContainsMethods() {
        Assert.assertFalse(StringUtils.contains(null, 'a'));
        Assert.assertFalse(StringUtils.contains("", 'a'));
        Assert.assertTrue(StringUtils.contains("abc", 'a'));
        Assert.assertFalse(StringUtils.contains("abc", 'z'));

        Assert.assertFalse(StringUtils.contains(null, "a"));
        Assert.assertFalse(StringUtils.contains("a", null));
        Assert.assertTrue(StringUtils.contains("", ""));
        Assert.assertTrue(StringUtils.contains("abc", "a"));
        Assert.assertFalse(StringUtils.contains("abc", "z"));

        Assert.assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        Assert.assertFalse(StringUtils.containsIgnoreCase("a", null));
        Assert.assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        Assert.assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
        Assert.assertTrue(StringUtils.containsIgnoreCase("abc", ""));

        Assert.assertFalse(StringUtils.containsWhitespace(null));
        Assert.assertFalse(StringUtils.containsWhitespace(""));
        Assert.assertFalse(StringUtils.containsWhitespace("abc"));
        Assert.assertTrue(StringUtils.containsWhitespace("a b c"));
    }

    @Test
    public void testIndexOfAnyAndContainsAnyCharArray() {
        Assert.assertEquals(-1, StringUtils.indexOfAny(null, 'a', 'b'));
        Assert.assertEquals(-1, StringUtils.indexOfAny("", 'a', 'b'));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", new char[0]));
        Assert.assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        Assert.assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", 'b', 'y'));
        Assert.assertEquals(-1, StringUtils.indexOfAny("aba", 'z'));

        Assert.assertEquals(-1, StringUtils.indexOfAny(null, "ab"));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", (String) null));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        Assert.assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));

        Assert.assertFalse(StringUtils.containsAny(null, 'a', 'b'));
        Assert.assertFalse(StringUtils.containsAny("", 'a', 'b'));
        Assert.assertFalse(StringUtils.containsAny("abc", (char[]) null));
        Assert.assertFalse(StringUtils.containsAny("abc", new char[0]));
        Assert.assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        Assert.assertFalse(StringUtils.containsAny("aba", 'z'));

        Assert.assertFalse(StringUtils.containsAny(null, "ab"));
        Assert.assertFalse(StringUtils.containsAny("abc", (CharSequence) null));
        Assert.assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        Assert.assertFalse(StringUtils.containsAny("aba", "z"));

        char high = '\uD83D';
        char low = '\uDE00';
        String surrogate = new String(new char[]{high, low});
        Assert.assertTrue(StringUtils.containsAny(surrogate, high, low));
        Assert.assertTrue(StringUtils.containsAny(surrogate, high));
        Assert.assertEquals(0, StringUtils.indexOfAny(surrogate, high, low));
    }

    @Test
    public void testIndexOfAnyButAndContainsOnlyNone() {
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut(null, 'a', 'b'));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("", 'a', 'b'));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", (char[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", new char[0]));
        Assert.assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", 'z', 'a'));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("aba", 'a', 'b'));

        Assert.assertEquals(-1, StringUtils.indexOfAnyBut(null, "ab"));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", (CharSequence) null));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("abc", ""));
        Assert.assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));

        Assert.assertFalse(StringUtils.containsOnly(null, 'a', 'b'));
        Assert.assertFalse(StringUtils.containsOnly("abc", (char[]) null));
        Assert.assertTrue(StringUtils.containsOnly("", 'a', 'b'));
        Assert.assertFalse(StringUtils.containsOnly("abc", new char[0]));
        Assert.assertTrue(StringUtils.containsOnly("abab", 'a', 'b'));
        Assert.assertFalse(StringUtils.containsOnly("abzc", 'a', 'b'));

        Assert.assertFalse(StringUtils.containsOnly(null, "ab"));
        Assert.assertFalse(StringUtils.containsOnly("abc", (String) null));
        Assert.assertTrue(StringUtils.containsOnly("abab", "ab"));

        Assert.assertTrue(StringUtils.containsNone(null, 'a', 'b'));
        Assert.assertTrue(StringUtils.containsNone("abc", (char[]) null));
        Assert.assertTrue(StringUtils.containsNone("", 'a', 'b'));
        Assert.assertTrue(StringUtils.containsNone("abab", 'x', 'y'));
        Assert.assertFalse(StringUtils.containsNone("abzc", 'z', 'y'));

        Assert.assertTrue(StringUtils.containsNone(null, "ab"));
        Assert.assertTrue(StringUtils.containsNone("abc", (String) null));
        Assert.assertTrue(StringUtils.containsNone("abab", "xy"));
        Assert.assertFalse(StringUtils.containsNone("abzc", "zy"));

        char high = '\uD83D';
        char low = '\uDE00';
        String surrogate = new String(new char[]{high, low});
        Assert.assertEquals(0, StringUtils.indexOfAnyBut(surrogate, 'a', 'b'));
        Assert.assertEquals(-1, StringUtils.indexOfAnyBut(surrogate, high, low));
        Assert.assertFalse(StringUtils.containsNone(surrogate, high, low));
        Assert.assertFalse(StringUtils.containsNone(surrogate, high));
    }

    @Test
    public void testIndexOfAnyAndLastIndexOfAnyStrings() {
        Assert.assertEquals(-1, StringUtils.indexOfAny(null, "ab", "cd"));
        Assert.assertEquals(-1, StringUtils.indexOfAny("abc", (CharSequence[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", "mn", "op"));
        Assert.assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "ab", "cd"));
        Assert.assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", (CharSequence) null, ""));

        Assert.assertEquals(-1, StringUtils.lastIndexOfAny(null, "ab", "cd"));
        Assert.assertEquals(-1, StringUtils.lastIndexOfAny("abc", (CharSequence[]) null));
        Assert.assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", "op"));
        Assert.assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", "ab", "cd"));
        Assert.assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", (CharSequence) null, ""));
    }

    @Test
    public void testSubstringMethods() {
        Assert.assertNull(StringUtils.substring(null, 0));
        Assert.assertEquals("", StringUtils.substring("", 0));
        Assert.assertEquals("c", StringUtils.substring("abc", 2));
        Assert.assertEquals("", StringUtils.substring("abc", 4));
        Assert.assertEquals("bc", StringUtils.substring("abc", -2));
        Assert.assertEquals("abc", StringUtils.substring("abc", -4));

        Assert.assertNull(StringUtils.substring(null, 0, 2));
        Assert.assertEquals("", StringUtils.substring("", 0, 2));
        Assert.assertEquals("ab", StringUtils.substring("abc", 0, 2));
        Assert.assertEquals("", StringUtils.substring("abc", 2, 0));
        Assert.assertEquals("c", StringUtils.substring("abc", 2, 4));
        Assert.assertEquals("", StringUtils.substring("abc", 4, 6));
        Assert.assertEquals("b", StringUtils.substring("abc", -2, -1));
        Assert.assertEquals("ab", StringUtils.substring("abc", -4, 2));
        Assert.assertEquals("", StringUtils.substring("abc", -1, -2));
        Assert.assertEquals("", StringUtils.substring("abc", 2, -4));
    }

    @Test
    public void testLeftRightMid() {
        Assert.assertNull(StringUtils.left(null, 2));
        Assert.assertEquals("", StringUtils.left("abc", -1));
        Assert.assertEquals("", StringUtils.left("", 2));
        Assert.assertEquals("ab", StringUtils.left("abc", 2));
        Assert.assertEquals("abc", StringUtils.left("abc", 4));

        Assert.assertNull(StringUtils.right(null, 2));
        Assert.assertEquals("", StringUtils.right("abc", -1));
        Assert.assertEquals("", StringUtils.right("", 2));
        Assert.assertEquals("bc", StringUtils.right("abc", 2));
        Assert.assertEquals("abc", StringUtils.right("abc", 4));

        Assert.assertNull(StringUtils.mid(null, 0, 2));
        Assert.assertEquals("", StringUtils.mid("abc", 0, -1));
        Assert.assertEquals("", StringUtils.mid("abc", 4, 2));
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
        Assert.assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        Assert.assertEquals("abc", StringUtils.substringBefore("abc", "z"));

        Assert.assertNull(StringUtils.substringAfter(null, "a"));
        Assert.assertEquals("", StringUtils.substringAfter("", "a"));
        Assert.assertEquals("", StringUtils.substringAfter("abc", null));
        Assert.assertEquals("abc", StringUtils.substringAfter("abc", ""));
        Assert.assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        Assert.assertEquals("", StringUtils.substringAfter("abc", "z"));

        Assert.assertNull(StringUtils.substringBeforeLast(null, "a"));
        Assert.assertEquals("", StringUtils.substringBeforeLast("", "a"));
        Assert.assertEquals("abc", StringUtils.substringBeforeLast("abc", null));
        Assert.assertEquals("abc", StringUtils.substringBeforeLast("abc", ""));
        Assert.assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        Assert.assertEquals("abc", StringUtils.substringBeforeLast("abc", "z"));

        Assert.assertNull(StringUtils.substringAfterLast(null, "a"));
        Assert.assertEquals("", StringUtils.substringAfterLast("", "a"));
        Assert.assertEquals("", StringUtils.substringAfterLast("abc", null));
        Assert.assertEquals("", StringUtils.substringAfterLast("abc", ""));
        Assert.assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        Assert.assertEquals("", StringUtils.substringAfterLast("abc", "c"));
        Assert.assertEquals("", StringUtils.substringAfterLast("abc", "z"));
    }

    @Test
    public void testSubstringBetween() {
        Assert.assertNull(StringUtils.substringBetween(null, "tag"));
        Assert.assertNull(StringUtils.substringBetween("tagabctag", null));
        Assert.assertEquals("", StringUtils.substringBetween("", ""));
        Assert.assertNull(StringUtils.substringBetween("", "tag"));
        Assert.assertEquals("abc", StringUtils.substringBetween("tagabctag", "tag"));

        Assert.assertNull(StringUtils.substringBetween(null, "[", "]"));
        Assert.assertNull(StringUtils.substringBetween("abc", null, "]"));
        Assert.assertNull(StringUtils.substringBetween("abc", "[", null));
        Assert.assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        Assert.assertNull(StringUtils.substringBetween("wx[byz", "[", "]"));
        Assert.assertNull(StringUtils.substringBetween("wxbyz", "[", "]"));

        Assert.assertNull(StringUtils.substringsBetween(null, "[", "]"));
        Assert.assertNull(StringUtils.substringsBetween("abc", null, "]"));
        Assert.assertNull(StringUtils.substringsBetween("abc", "[", null));
        Assert.assertNull(StringUtils.substringsBetween("abc", "", "]"));
        Assert.assertNull(StringUtils.substringsBetween("abc", "[", ""));
        Assert.assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
        Assert.assertNull(StringUtils.substringsBetween("abc", "[", "]"));
    }

    @Test
    public void testSplit() {
        Assert.assertNull(StringUtils.split(null));
        Assert.assertArrayEquals(new String[0], StringUtils.split(""));
        Assert.assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a:b:c", ":"));
        Assert.assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":", 0));
        Assert.assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.split("ab cd:ef", null, 2));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab,cd;ef", ",;"));
    }

    @Test
    public void testSplitByWholeSeparator() {
        Assert.assertNull(StringUtils.splitByWholeSeparator(null, "."));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", "."));
        Assert.assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab de fg", null));
        Assert.assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab de fg", ""));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        Assert.assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 5));
        Assert.assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-!-cd-!-ef", "-!-"));

        Assert.assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, "."));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByWholeSeparatorPreserveAllTokens("", "."));
        Assert.assertArrayEquals(new String[]{"ab", "", "cd", "ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-!-!-cd-!-ef", "-!-"));
        Assert.assertArrayEquals(new String[]{"ab", "-!-cd-!-ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-!-cd-!-ef", "-!-", 2));
        Assert.assertArrayEquals(new String[]{"ab", "", "cd", "ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-!-cd-!-ef", "-!-", 5));
        Assert.assertArrayEquals(new String[]{"ab", "", "", "de", "fg"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null));
    }

    @Test
    public void testSplitPreserveAllTokens() {
        Assert.assertNull(StringUtils.splitPreserveAllTokens(null));
        Assert.assertArrayEquals(new String[0], StringUtils.splitPreserveAllTokens(""));
        Assert.assertArrayEquals(new String[]{"abc", "", "def"}, StringUtils.splitPreserveAllTokens("abc  def"));
        Assert.assertArrayEquals(new String[]{"", "abc", ""}, StringUtils.splitPreserveAllTokens(" abc "));
        Assert.assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        Assert.assertArrayEquals(new String[]{"a", "b", "c", ""}, StringUtils.splitPreserveAllTokens("a b c ", ' '));
        Assert.assertArrayEquals(new String[]{"ab", "", "cd", "ef"}, StringUtils.splitPreserveAllTokens("ab::cd:ef", ":"));
        Assert.assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 2));
        Assert.assertArrayEquals(new String[]{"ab", "  de fg"}, StringUtils.splitPreserveAllTokens("ab   de fg", null, 2));
        Assert.assertArrayEquals(new String[]{"ab", "", " de fg"}, StringUtils.splitPreserveAllTokens("ab   de fg", null, 3));
        Assert.assertArrayEquals(new String[]{"ab", "", "", "de fg"}, StringUtils.splitPreserveAllTokens("ab   de fg", null, 4));
        Assert.assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a,;b,c", ",;"));
    }

    @Test
    public void testSplitByCharacterType() {
        Assert.assertNull(StringUtils.splitByCharacterType(null));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
        Assert.assertArrayEquals(new String[]{"ab", " ", "de", " ", "fg"}, StringUtils.splitByCharacterType("ab de fg"));
        Assert.assertArrayEquals(new String[]{"number", "5"}, StringUtils.splitByCharacterType("number5"));
        Assert.assertArrayEquals(new String[]{"foo", "B", "ar"}, StringUtils.splitByCharacterType("fooBar"));

        Assert.assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        Assert.assertArrayEquals(new String[0], StringUtils.splitByCharacterTypeCamelCase(""));
        Assert.assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        Assert.assertArrayEquals(new String[]{"foo", "200", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("foo200Bar"));
        Assert.assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    @Test
    public void testJoinMethods() {
        Assert.assertNull(StringUtils.join((Object[]) null));
        Assert.assertEquals("", StringUtils.join(new Object[0]));
        Assert.assertEquals("abc", StringUtils.join("a", "b", "c"));
        Assert.assertEquals("a", StringUtils.join(null, "", "a"));

        Assert.assertNull(StringUtils.join((Object[]) null, ';'));
        Assert.assertEquals("", StringUtils.join(new Object[0], ';'));
        Assert.assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        Assert.assertEquals(";;a", StringUtils.join(new Object[]{null, "", "a"}, ';'));
        Assert.assertEquals("b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';', 1, 3));
        Assert.assertEquals("", StringUtils.join(new Object[]{"a", "b", "c"}, ';', 2, 1));

        Assert.assertNull(StringUtils.join((Object[]) null, "--"));
        Assert.assertEquals("", StringUtils.join(new Object[0], "--"));
        Assert.assertEquals("a--b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--"));
        Assert.assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, (String) null));
        Assert.assertEquals("b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--", 1, 3));
        Assert.assertEquals("", StringUtils.join(new Object[]{"a", "b", "c"}, "--", 2, 1));

        Assert.assertNull(StringUtils.join((Iterable<?>) null, ';'));
        Assert.assertEquals("", StringUtils.join(Collections.emptyList(), ';'));
        Assert.assertEquals("a", StringUtils.join(Collections.singletonList("a"), ';'));
        Assert.assertEquals("a;b", StringUtils.join(Arrays.asList("a", "b"), ';'));
        Assert.assertEquals(";b", StringUtils.join(Arrays.asList(null, "b"), ';'));

        Assert.assertNull(StringUtils.join((Iterable<?>) null, ","));
        Assert.assertEquals("", StringUtils.join(Collections.emptyList(), ","));
        Assert.assertEquals("a", StringUtils.join(Collections.singletonList("a"), ","));
        Assert.assertEquals("a,b", StringUtils.join(Arrays.asList("a", "b"), ","));
        Assert.assertEquals("ab", StringUtils.join(Arrays.asList("a", "b"), null));
        Assert.assertEquals(",b", StringUtils.join(Arrays.asList(null, "b"), ","));
    }

    @Test
    public void testDeleteWhitespace() {
        Assert.assertNull(StringUtils.deleteWhitespace(null));
        Assert.assertEquals("", StringUtils.deleteWhitespace(""));
        Assert.assertEquals("abc", StringUtils.deleteWhitespace("abc"));
        Assert.assertEquals("abc", StringUtils.deleteWhitespace("   ab  c  "));
    }

    @Test
    public void testRemoveMethods() {
        Assert.assertNull(StringUtils.removeStart(null, "www."));
        Assert.assertEquals("", StringUtils.removeStart("", "www."));
        Assert.assertEquals("domain.com", StringUtils.removeStart("domain.com", null));
        Assert.assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        Assert.assertEquals("domain.com", StringUtils.removeStart("domain.com", "www."));

        Assert.assertNull(StringUtils.removeStartIgnoreCase(null, "www."));
        Assert.assertEquals("", StringUtils.removeStartIgnoreCase("", "www."));
        Assert.assertEquals("domain.com", StringUtils.removeStartIgnoreCase("domain.com", null));
        Assert.assertEquals("domain.com", StringUtils.removeStartIgnoreCase("www.domain.com", "WWW."));
        Assert.assertEquals("domain.com", StringUtils.removeStartIgnoreCase("domain.com", "WWW."));

        Assert.assertNull(StringUtils.removeEnd(null, ".com"));
        Assert.assertEquals("", StringUtils.removeEnd("", ".com"));
        Assert.assertEquals("domain.com", StringUtils.removeEnd("domain.com", null));
        Assert.assertEquals("domain", StringUtils.removeEnd("domain.com", ".com"));
        Assert.assertEquals("domain.com", StringUtils.removeEnd("domain.com", ".org"));

        Assert.assertNull(StringUtils.removeEndIgnoreCase(null, ".com"));
        Assert.assertEquals("", StringUtils.removeEndIgnoreCase("", ".com"));
        Assert.assertEquals("domain.com", StringUtils.removeEndIgnoreCase("domain.com", null));
        Assert.assertEquals("domain", StringUtils.removeEndIgnoreCase("domain.com", ".COM"));
        Assert.assertEquals("domain.com", StringUtils.removeEndIgnoreCase("domain.com", ".ORG"));

        Assert.assertNull(StringUtils.remove(null, "a"));
        Assert.assertEquals("", StringUtils.remove("", "a"));
        Assert.assertEquals("abc", StringUtils.remove("abc", null));
        Assert.assertEquals("abc", StringUtils.remove("abc", ""));
        Assert.assertEquals("qd", StringUtils.remove("queued", "ue"));
        Assert.assertEquals("queued", StringUtils.remove("queued", "zz"));

        Assert.assertNull(StringUtils.remove(null, 'a'));
        Assert.assertEquals("", StringUtils.remove("", 'a'));
        Assert.assertEquals("queued", StringUtils.remove("queued", 'z'));
        Assert.assertEquals("qeed", StringUtils.remove("queued", 'u'));
    }

    @Test
    public void testReplaceMethods() {
        Assert.assertNull(StringUtils.replaceOnce(null, "a", "b"));
        Assert.assertEquals("ba", StringUtils.replaceOnce("aba", "a", ""));
        Assert.assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));

        Assert.assertNull(StringUtils.replace(null, "a", "b"));
        Assert.assertEquals("", StringUtils.replace("", "a", "b"));
        Assert.assertEquals("any", StringUtils.replace("any", null, "b"));
        Assert.assertEquals("any", StringUtils.replace("any", "a", null));
        Assert.assertEquals("any", StringUtils.replace("any", "", "b"));
        Assert.assertEquals("any", StringUtils.replace("any", "a", "b", 0));
        Assert.assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        Assert.assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));

        Assert.assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        Assert.assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", null, null));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[0], null));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", null, new String[0]));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        Assert.assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}));
        Assert.assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}));
        Assert.assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));

        Assert.assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        Assert.assertEquals("aba", StringUtils.replaceEachRepeatedly("aba", null, null));

        Assert.assertNull(StringUtils.replaceChars(null, 'b', 'y'));
        Assert.assertEquals("", StringUtils.replaceChars("", 'b', 'y'));
        Assert.assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));

        Assert.assertNull(StringUtils.replaceChars(null, "b", "y"));
        Assert.assertEquals("", StringUtils.replaceChars("", "b", "y"));
        Assert.assertEquals("abc", StringUtils.replaceChars("abc", null, "y"));
        Assert.assertEquals("abc", StringUtils.replaceChars("abc", "", "y"));
        Assert.assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        Assert.assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        Assert.assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        Assert.assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEachThrowsExceptionOnMismatchedLengths() {
        StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w"});
    }

    @Test(expected = IllegalStateException.class)
    public void testReplaceEachRepeatedlyThrowsExceptionOnLoop() {
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"});
    }

    @Test
    public void testOverlay() {
        Assert.assertNull(StringUtils.overlay(null, "abc", 0, 0));
        Assert.assertEquals("abc", StringUtils.overlay("", "abc", 0, 0));
        Assert.assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
        Assert.assertEquals("abef", StringUtils.overlay("abcdef", "", 2, 4));
        Assert.assertEquals("abef", StringUtils.overlay("abcdef", "", 4, 2));
        Assert.assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        Assert.assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        Assert.assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        Assert.assertEquals("abzzzz", StringUtils.overlay("abcdef", "zzzz", 2, 8));
        Assert.assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        Assert.assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testChompAndChop() {
        Assert.assertNull(StringUtils.chomp(null));
        Assert.assertEquals("", StringUtils.chomp(""));
        Assert.assertEquals("abc ", StringUtils.chomp("abc \r"));
        Assert.assertEquals("abc", StringUtils.chomp("abc\n"));
        Assert.assertEquals("abc", StringUtils.chomp("abc\r\n"));
        Assert.assertEquals("abc\r\n", StringUtils.chomp("abc\r\n\r\n"));
        Assert.assertEquals("abc\n", StringUtils.chomp("abc\n\r"));
        Assert.assertEquals("", StringUtils.chomp("\r"));
        Assert.assertEquals("", StringUtils.chomp("\n"));
        Assert.assertEquals("a", StringUtils.chomp("a"));

        Assert.assertNull(StringUtils.chomp(null, "bar"));
        Assert.assertEquals("foo", StringUtils.chomp("foobar", "bar"));

        Assert.assertNull(StringUtils.chop(null));
        Assert.assertEquals("", StringUtils.chop(""));
        Assert.assertEquals("", StringUtils.chop("a"));
        Assert.assertEquals("abc ", StringUtils.chop("abc \r"));
        Assert.assertEquals("abc", StringUtils.chop("abc\n"));
        Assert.assertEquals("abc", StringUtils.chop("abc\r\n"));
        Assert.assertEquals("ab", StringUtils.chop("abc"));
    }

    @Test
    public void testRepeat() {
        Assert.assertNull(StringUtils.repeat(null, 2));
        Assert.assertEquals("", StringUtils.repeat("", 0));
        Assert.assertEquals("", StringUtils.repeat("", 2));
        Assert.assertEquals("", StringUtils.repeat("a", -2));
        Assert.assertEquals("a", StringUtils.repeat("a", 1));
        Assert.assertEquals("aaa", StringUtils.repeat("a", 3));
        Assert.assertEquals("abab", StringUtils.repeat("ab", 2));
        Assert.assertEquals("abcabcabc", StringUtils.repeat("abc", 3));

        Assert.assertNull(StringUtils.repeat(null, ",", 2));
        Assert.assertEquals("a", StringUtils.repeat("a", null, 1));
        Assert.assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));

        Assert.assertEquals("", StringUtils.repeat('e', 0));
        Assert.assertEquals("eee", StringUtils.repeat('e', 3));
    }

    @Test
    public void testPadMethods() {
        Assert.assertNull(StringUtils.rightPad(null, 3));
        Assert.assertEquals("   ", StringUtils.rightPad("", 3));
        Assert.assertEquals("bat", StringUtils.rightPad("bat", 3));
        Assert.assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        Assert.assertEquals("bat", StringUtils.rightPad("bat", 1));

        Assert.assertNull(StringUtils.rightPad(null, 3, 'z'));
        Assert.assertEquals("zzz", StringUtils.rightPad("", 3, 'z'));
        Assert.assertEquals("bat", StringUtils.rightPad("bat", 3, 'z'));
        Assert.assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));

        Assert.assertNull(StringUtils.rightPad(null, 3, "z"));
        Assert.assertEquals("bat  ", StringUtils.rightPad("bat", 5, null));
        Assert.assertEquals("bat  ", StringUtils.rightPad("bat", 5, ""));
        Assert.assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        Assert.assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        Assert.assertEquals("baty", StringUtils.rightPad("bat", 4, "yz"));

        Assert.assertNull(StringUtils.leftPad(null, 3));
        Assert.assertEquals("   ", StringUtils.leftPad("", 3));
        Assert.assertEquals("bat", StringUtils.leftPad("bat", 3));
        Assert.assertEquals("  bat", StringUtils.leftPad("bat", 5));

        Assert.assertNull(StringUtils.leftPad(null, 3, 'z'));
        Assert.assertEquals("zzz", StringUtils.leftPad("", 3, 'z'));
        Assert.assertEquals("bat", StringUtils.leftPad("bat", 3, 'z'));
        Assert.assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));

        Assert.assertNull(StringUtils.leftPad(null, 3, "z"));
        Assert.assertEquals("  bat", StringUtils.leftPad("bat", 5, null));
        Assert.assertEquals("  bat", StringUtils.leftPad("bat", 5, ""));
        Assert.assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        Assert.assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        Assert.assertEquals("ybat", StringUtils.leftPad("bat", 4, "yz"));
    }

    @Test
    public void testLengthAndCenter() {
        Assert.assertEquals(0, StringUtils.length(null));
        Assert.assertEquals(3, StringUtils.length("abc"));

        Assert.assertNull(StringUtils.center(null, 4));
        Assert.assertEquals("    ", StringUtils.center("", 4));
        Assert.assertEquals("ab", StringUtils.center("ab", -1));
        Assert.assertEquals(" ab ", StringUtils.center("ab", 4));
        Assert.assertEquals("abcd", StringUtils.center("abcd", 2));
        Assert.assertEquals(" a  ", StringUtils.center("a", 4));

        Assert.assertNull(StringUtils.center(null, 4, ' '));
        Assert.assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        Assert.assertEquals("ab", StringUtils.center("ab", -1, ' '));
        Assert.assertEquals("abcd", StringUtils.center("abcd", 2, ' '));

        Assert.assertNull(StringUtils.center(null, 4, " "));
        Assert.assertEquals("ab", StringUtils.center("ab", -1, " "));
        Assert.assertEquals("abcd", StringUtils.center("abcd", 2, " "));
        Assert.assertEquals("  abc  ", StringUtils.center("abc", 7, null));
        Assert.assertEquals("  abc  ", StringUtils.center("abc", 7, ""));
        Assert.assertEquals("yayz", StringUtils.center("a", 4, "yz"));
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
        Assert.assertEquals("aBcD", StringUtils.swapCase("AbCd"));
    }

    @Test
    public void testCountMatches() {
        Assert.assertEquals(0, StringUtils.countMatches(null, "a"));
        Assert.assertEquals(0, StringUtils.countMatches("", "a"));
        Assert.assertEquals(0, StringUtils.countMatches("abba", null));
        Assert.assertEquals(0, StringUtils.countMatches("abba", ""));
        Assert.assertEquals(2, StringUtils.countMatches("abba", "a"));
        Assert.assertEquals(1, StringUtils.countMatches("abba", "ab"));
        Assert.assertEquals(0, StringUtils.countMatches("abba", "xxx"));
    }

    @Test
    public void testCharacterPredicates() {
        Assert.assertFalse(StringUtils.isAlpha(null));
        Assert.assertFalse(StringUtils.isAlpha(""));
        Assert.assertTrue(StringUtils.isAlpha("abc"));
        Assert.assertFalse(StringUtils.isAlpha("ab2c"));

        Assert.assertFalse(StringUtils.isAlphaSpace(null));
        Assert.assertTrue(StringUtils.isAlphaSpace(""));
        Assert.assertTrue(StringUtils.isAlphaSpace("ab c"));
        Assert.assertFalse(StringUtils.isAlphaSpace("ab2c"));

        Assert.assertFalse(StringUtils.isAlphanumeric(null));
        Assert.assertFalse(StringUtils.isAlphanumeric(""));
        Assert.assertTrue(StringUtils.isAlphanumeric("ab2c"));
        Assert.assertFalse(StringUtils.isAlphanumeric("ab-c"));

        Assert.assertFalse(StringUtils.isAlphanumericSpace(null));
        Assert.assertTrue(StringUtils.isAlphanumericSpace(""));
        Assert.assertTrue(StringUtils.isAlphanumericSpace("ab 2c"));
        Assert.assertFalse(StringUtils.isAlphanumericSpace("ab-c"));

        Assert.assertFalse(StringUtils.isAsciiPrintable(null));
        Assert.assertTrue(StringUtils.isAsciiPrintable(""));
        Assert.assertTrue(StringUtils.isAsciiPrintable("!ab-c~"));
        Assert.assertFalse(StringUtils.isAsciiPrintable("\u007f"));

        Assert.assertFalse(StringUtils.isNumeric(null));
        Assert.assertFalse(StringUtils.isNumeric(""));
        Assert.assertTrue(StringUtils.isNumeric("123"));
        Assert.assertFalse(StringUtils.isNumeric("12 3"));

        Assert.assertFalse(StringUtils.isNumericSpace(null));
        Assert.assertTrue(StringUtils.isNumericSpace(""));
        Assert.assertTrue(StringUtils.isNumericSpace("12 3"));
        Assert.assertFalse(StringUtils.isNumericSpace("12-3"));

        Assert.assertFalse(StringUtils.isWhitespace(null));
        Assert.assertTrue(StringUtils.isWhitespace(""));
        Assert.assertTrue(StringUtils.isWhitespace("  \t\n "));
        Assert.assertFalse(StringUtils.isWhitespace(" abc "));

        Assert.assertFalse(StringUtils.isAllLowerCase(null));
        Assert.assertFalse(StringUtils.isAllLowerCase(""));
        Assert.assertTrue(StringUtils.isAllLowerCase("abc"));
        Assert.assertFalse(StringUtils.isAllLowerCase("abC"));

        Assert.assertFalse(StringUtils.isAllUpperCase(null));
        Assert.assertFalse(StringUtils.isAllUpperCase(""));
        Assert.assertTrue(StringUtils.isAllUpperCase("ABC"));
        Assert.assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    @Test
    public void testDefaultMethods() {
        Assert.assertEquals("", StringUtils.defaultString(null));
        Assert.assertEquals("bat", StringUtils.defaultString("bat"));
        Assert.assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        Assert.assertEquals("bat", StringUtils.defaultString("bat", "NULL"));

        Assert.assertEquals("NULL", StringUtils.defaultIfBlank(null, "NULL"));
        Assert.assertEquals("NULL", StringUtils.defaultIfBlank("   ", "NULL"));
        Assert.assertEquals("bat", StringUtils.defaultIfBlank("bat", "NULL"));

        Assert.assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        Assert.assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        Assert.assertEquals(" ", StringUtils.defaultIfEmpty(" ", "NULL"));
        Assert.assertEquals("bat", StringUtils.defaultIfEmpty("bat", "NULL"));
    }

    @Test
    public void testReverseMethods() {
        Assert.assertNull(StringUtils.reverse(null));
        Assert.assertEquals("", StringUtils.reverse(""));
        Assert.assertEquals("tab", StringUtils.reverse("bat"));

        Assert.assertNull(StringUtils.reverseDelimited(null, '.'));
        Assert.assertEquals("", StringUtils.reverseDelimited("", '.'));
        Assert.assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
    }

    @Test
    public void testAbbreviateMethods() {
        Assert.assertNull(StringUtils.abbreviate(null, 4));
        Assert.assertEquals("", StringUtils.abbreviate("", 4));
        Assert.assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        Assert.assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        Assert.assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));

        Assert.assertNull(StringUtils.abbreviate(null, 0, 4));
        Assert.assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", -1, 10));
        Assert.assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        Assert.assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        Assert.assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 10, 10));
        Assert.assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));

        Assert.assertNull(StringUtils.abbreviateMiddle(null, ".", 0));
        Assert.assertEquals("abc", StringUtils.abbreviateMiddle("abc", null, 0));
        Assert.assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 0));
        Assert.assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 3));
        Assert.assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateThrowsExceptionOnSmallWidth() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateWithOffsetThrowsExceptionOnSmallWidth() {
        StringUtils.abbreviate("abcdefghij", 5, 6);
    }

    @Test
    public void testDifferenceAndCommonPrefix() {
        Assert.assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        Assert.assertEquals("abc", StringUtils.difference(null, "abc"));
        Assert.assertEquals("abc", StringUtils.difference("abc", null));
        Assert.assertEquals("", StringUtils.difference("abc", "abc"));

        Assert.assertEquals(-1, StringUtils.indexOfDifference(null, null));
        Assert.assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        Assert.assertEquals(0, StringUtils.indexOfDifference("abc", null));
        Assert.assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        Assert.assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));
        Assert.assertEquals(2, StringUtils.indexOfDifference("abcde", "abxyz"));

        Assert.assertEquals(-1, StringUtils.indexOfDifference((CharSequence[]) null));
        Assert.assertEquals(-1, StringUtils.indexOfDifference(new String[0]));
        Assert.assertEquals(-1, StringUtils.indexOfDifference("abc"));
        Assert.assertEquals(-1, StringUtils.indexOfDifference(null, null));
        Assert.assertEquals(-1, StringUtils.indexOfDifference("", ""));
        Assert.assertEquals(0, StringUtils.indexOfDifference("", null));
        Assert.assertEquals(0, StringUtils.indexOfDifference("abc", null, null));
        Assert.assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
        Assert.assertEquals(3, StringUtils.indexOfDifference("abc", "abcdef"));

        Assert.assertEquals("", StringUtils.getCommonPrefix((String[]) null));
        Assert.assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        Assert.assertEquals("abc", StringUtils.getCommonPrefix("abc"));
        Assert.assertEquals("", StringUtils.getCommonPrefix(null, null));
        Assert.assertEquals("i am a ", StringUtils.getCommonPrefix("i am a machine", "i am a robot"));
        Assert.assertEquals("", StringUtils.getCommonPrefix("abcde", "xyz"));
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
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 8));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 7));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("aaapppp", "", 6));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo", 7));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("elephant", "hippo", 6));
        Assert.assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant", 7));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("hippo", "elephant", 6));
        Assert.assertEquals(-1, StringUtils.getLevenshteinDistance("a", "abcdef", 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinNullInput1() {
        StringUtils.getLevenshteinDistance(null, "a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinNullInput2() {
        StringUtils.getLevenshteinDistance("a", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinWithThresholdNegative() {
        StringUtils.getLevenshteinDistance("a", "b", -1);
    }

    @Test
    public void testStartsAndEndsWith() {
        Assert.assertTrue(StringUtils.startsWith(null, null));
        Assert.assertFalse(StringUtils.startsWith(null, "abc"));
        Assert.assertFalse(StringUtils.startsWith("abcdef", null));
        Assert.assertTrue(StringUtils.startsWith("abcdef", "abc"));
        Assert.assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
        Assert.assertFalse(StringUtils.startsWith("abc", "abcdef"));

        Assert.assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        Assert.assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        Assert.assertFalse(StringUtils.startsWithIgnoreCase("abcdef", null));
        Assert.assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));

        Assert.assertFalse(StringUtils.startsWithAny(null, "abc"));
        Assert.assertFalse(StringUtils.startsWithAny("abcxyz", (CharSequence[]) null));
        Assert.assertTrue(StringUtils.startsWithAny("abcxyz", "abc", "def"));
        Assert.assertTrue(StringUtils.startsWithAny("abcxyz", null, "xyz", "abc"));
        Assert.assertFalse(StringUtils.startsWithAny("abcxyz", "def", "ghi"));

        Assert.assertTrue(StringUtils.endsWith(null, null));
        Assert.assertFalse(StringUtils.endsWith(null, "def"));
        Assert.assertFalse(StringUtils.endsWith("abcdef", null));
        Assert.assertTrue(StringUtils.endsWith("abcdef", "def"));
        Assert.assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        Assert.assertFalse(StringUtils.endsWith("def", "abcdef"));

        Assert.assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        Assert.assertFalse(StringUtils.endsWithIgnoreCase("abcdef", null));
        Assert.assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));

        Assert.assertFalse(StringUtils.endsWithAny(null, "def"));
        Assert.assertFalse(StringUtils.endsWithAny("abcxyz", (CharSequence[]) null));
        Assert.assertTrue(StringUtils.endsWithAny("abcxyz", "xyz", "def"));
        Assert.assertTrue(StringUtils.endsWithAny("abcxyz", null, "abc", "xyz"));
        Assert.assertFalse(StringUtils.endsWithAny("abcxyz", "abc", "def"));
    }

    @Test
    public void testNormalizeSpace() {
        Assert.assertNull(StringUtils.normalizeSpace(null));
        Assert.assertEquals("", StringUtils.normalizeSpace(""));
        Assert.assertEquals("a b c", StringUtils.normalizeSpace("  a \t  b \n  c  "));
    }

    @Test
    public void testToStringWithCharset() throws UnsupportedEncodingException {
        byte[] bytes = "test".getBytes("UTF-8");
        Assert.assertEquals("test", StringUtils.toString(bytes, "UTF-8"));
        Assert.assertEquals("test", StringUtils.toString(bytes, null));
    }

    @Test(expected = NullPointerException.class)
    public void testToStringWithCharsetNPE() throws UnsupportedEncodingException {
        StringUtils.toString(null, "UTF-8");
    }
}
