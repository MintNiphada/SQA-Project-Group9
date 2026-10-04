package org.apache.commons.lang3;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("bob"));
    }

    @Test
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank("\t\n\r"));
        assertFalse(StringUtils.isBlank("bob"));
        assertFalse(StringUtils.isBlank("  bob  "));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" "));
        assertTrue(StringUtils.isNotBlank("bob"));
    }

    @Test
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("     "));
        assertEquals("abc", StringUtils.trim("abc"));
        assertEquals("abc", StringUtils.trim("    abc    "));
    }

    @Test
    public void testTrimToNull() {
        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("     "));
        assertEquals("abc", StringUtils.trimToNull("abc"));
        assertEquals("abc", StringUtils.trimToNull("    abc    "));
    }

    @Test
    public void testTrimToEmpty() {
        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("     "));
        assertEquals("abc", StringUtils.trimToEmpty("abc"));
        assertEquals("abc", StringUtils.trimToEmpty("    abc    "));
    }

    @Test
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("abc"));
        assertEquals("abc", StringUtils.strip("  abc"));
        assertEquals("abc", StringUtils.strip("abc  "));
        assertEquals("abc", StringUtils.strip(" abc "));
        assertEquals("ab c", StringUtils.strip(" ab c "));
    }

    @Test
    public void testStripToNull() {
        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull(""));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("abc"));
        assertEquals("abc", StringUtils.stripToNull("  abc"));
        assertEquals("abc", StringUtils.stripToNull("abc  "));
        assertEquals("abc", StringUtils.stripToNull(" abc "));
        assertEquals("ab c", StringUtils.stripToNull(" ab c "));
    }

    @Test
    public void testStripToEmpty() {
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty(""));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("abc"));
        assertEquals("abc", StringUtils.stripToEmpty("  abc"));
        assertEquals("abc", StringUtils.stripToEmpty("abc  "));
        assertEquals("abc", StringUtils.stripToEmpty(" abc "));
        assertEquals("ab c", StringUtils.stripToEmpty(" ab c "));
    }

    @Test
    public void testStripWithChars() {
        assertEquals("abc", StringUtils.strip("  abc", null));
        assertEquals("abc", StringUtils.strip("abc  ", null));
        assertEquals("abc", StringUtils.strip(" abc ", null));
        assertEquals("  abc", StringUtils.strip("  abcyx", "xyz"));
        assertEquals("abc", StringUtils.strip("  abc", " "));
    }

    @Test
    public void testStripStart() {
        assertNull(StringUtils.stripStart(null, null));
        assertEquals("", StringUtils.stripStart("", null));
        assertEquals("abc", StringUtils.stripStart("abc", ""));
        assertEquals("abc", StringUtils.stripStart("abc", null));
        assertEquals("abc", StringUtils.stripStart("  abc", null));
        assertEquals("abc  ", StringUtils.stripStart("abc  ", null));
        assertEquals("abc ", StringUtils.stripStart(" abc ", null));
        assertEquals("abc  ", StringUtils.stripStart("yxabc  ", "xyz"));
    }

    @Test
    public void testStripEnd() {
        assertNull(StringUtils.stripEnd(null, null));
        assertEquals("", StringUtils.stripEnd("", null));
        assertEquals("abc", StringUtils.stripEnd("abc", ""));
        assertEquals("abc", StringUtils.stripEnd("abc", null));
        assertEquals("  abc", StringUtils.stripEnd("  abc", null));
        assertEquals("abc", StringUtils.stripEnd("abc  ", null));
        assertEquals(" abc", StringUtils.stripEnd(" abc ", null));
        assertEquals("  abc", StringUtils.stripEnd("  abcyx", "xyz"));
        assertEquals("12", StringUtils.stripEnd("120.00", ".0"));
    }

    @Test
    public void testStripAll() {
        assertNull(StringUtils.stripAll((String[]) null));
        assertArrayEquals(new String[]{}, StringUtils.stripAll());
        assertArrayEquals(new String[]{"abc", "abc"}, StringUtils.stripAll(new String[]{"abc", "  abc"}));
        assertArrayEquals(new String[]{"abc", null}, StringUtils.stripAll(new String[]{"abc  ", null}));
    }

    @Test
    public void testStripAllWithChars() {
        assertNull(StringUtils.stripAll((String[]) null, null));
        assertArrayEquals(new String[]{}, StringUtils.stripAll(new String[]{}, null));
        assertArrayEquals(new String[]{"abc", "abc"}, StringUtils.stripAll(new String[]{"abc", "  abc"}, null));
        assertArrayEquals(new String[]{"abc  ", null}, StringUtils.stripAll(new String[]{"abc  ", null}, "yz"));
        assertArrayEquals(new String[]{"abc", null}, StringUtils.stripAll(new String[]{"yabcz", null}, "yz"));
    }

    @Test
    public void testEquals() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
    }

    @Test
    public void testEqualsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
    }

    @Test
    public void testIndexOf() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
    }

    @Test
    public void testIndexOfWithStart() {
        assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));
    }

    @Test
    public void testIndexOfString() {
        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", null));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
    }

    @Test
    public void testIndexOfStringWithStart() {
        assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", null, 0));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a", 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab", 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "b", 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "", 2));
        assertEquals(3, StringUtils.indexOf("abc", "", 9));
    }

    @Test
    public void testOrdinalIndexOf() {
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("a", null, 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(2, StringUtils.ordinalIndexOf("aabaabaa", "b", 1));
        assertEquals(5, StringUtils.ordinalIndexOf("aabaabaa", "b", 2));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "ab", 1));
        assertEquals(4, StringUtils.ordinalIndexOf("aabaabaa", "ab", 2));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 2));
    }

    @Test
    public void testLastOrdinalIndexOf() {
        assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", null, 1));
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
        assertEquals(5, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 1));
        assertEquals(2, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 2));
        assertEquals(4, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 1));
        assertEquals(1, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 2));
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1));
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 2));
    }

    @Test
    public void testIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", null));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "ab"));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB"));
    }

    @Test
    public void testIndexOfIgnoreCaseWithStart() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a", 0));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", null, 0));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "a", 0));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "b", 0));
        assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "b", 9));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "b", -1));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "", 2));
        assertEquals(3, StringUtils.indexOfIgnoreCase("abc", "", 9));
    }

    @Test
    public void testLastIndexOf() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
    }

    @Test
    public void testLastIndexOfWithStart() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', -1));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", 'a', 0));
    }

    @Test
    public void testLastIndexOfString() {
        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", null));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a"));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
    }

    @Test
    public void testLastIndexOfStringWithStart() {
        assertEquals(-1, StringUtils.lastIndexOf(null, "a", 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", null, 0));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a", 8));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", "a", 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", 0));
    }

    @Test
    public void testLastIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB"));
    }

    @Test
    public void testLastIndexOfIgnoreCaseWithStart() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null, 0));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 8));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB", 8));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0));
    }

    @Test
    public void testContainsChar() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));
    }

    @Test
    public void testContainsString() {
        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "a"));
        assertFalse(StringUtils.contains("abc", "z"));
    }

    @Test
    public void testContainsIgnoreCase() {
        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "z"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
    }

    @Test
    public void testContainsWhitespace() {
        assertFalse(StringUtils.containsWhitespace(null));
        assertFalse(StringUtils.containsWhitespace(""));
        assertTrue(StringUtils.containsWhitespace(" "));
        assertTrue(StringUtils.containsWhitespace("abc def"));
        assertFalse(StringUtils.containsWhitespace("abcdef"));
    }

    @Test
    public void testIndexOfAnyCharArray() {
        assertEquals(-1, StringUtils.indexOfAny(null, 'a'));
        assertEquals(-1, StringUtils.indexOfAny("", 'a'));
        assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", 'b', 'y'));
        assertEquals(-1, StringUtils.indexOfAny("aba", 'z'));
    }

    @Test
    public void testIndexOfAnyString() {
        assertEquals(-1, StringUtils.indexOfAny(null, "za"));
        assertEquals(-1, StringUtils.indexOfAny("", "za"));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtils.indexOfAny("aba", "z"));
    }

    @Test
    public void testContainsAnyCharArray() {
        assertFalse(StringUtils.containsAny(null, 'a'));
        assertFalse(StringUtils.containsAny("", 'a'));
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        assertTrue(StringUtils.containsAny("zzabyycdxx", 'b', 'y'));
        assertFalse(StringUtils.containsAny("aba", 'z'));
    }

    @Test
    public void testContainsAnyString() {
        assertFalse(StringUtils.containsAny(null, "za"));
        assertFalse(StringUtils.containsAny("", "za"));
        assertFalse(StringUtils.containsAny("abc", (String) null));
        assertFalse(StringUtils.containsAny("abc", ""));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "by"));
        assertFalse(StringUtils.containsAny("aba", "z"));
    }

    @Test
    public void testIndexOfAnyButCharArray() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, 'z', 'a'));
        assertEquals(-1, StringUtils.indexOfAnyBut("", 'z', 'a'));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", (char[]) null));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", 'z', 'a'));
        assertEquals(0, StringUtils.indexOfAnyBut("aba", 'z'));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", 'a', 'b'));
    }

    @Test
    public void testIndexOfAnyButString() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", (String) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzabyycdxx", ""));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));
    }

    @Test
    public void testContainsOnlyCharArray() {
        assertFalse(StringUtils.containsOnly(null, 'a'));
        assertFalse(StringUtils.containsOnly("ab", (char[]) null));
        assertTrue(StringUtils.containsOnly("", 'a'));
        assertFalse(StringUtils.containsOnly("ab", new char[]{}));
        assertTrue(StringUtils.containsOnly("abab", 'a', 'b', 'c'));
        assertFalse(StringUtils.containsOnly("ab1", 'a', 'b', 'c'));
        assertFalse(StringUtils.containsOnly("abz", 'a', 'b', 'c'));
    }

    @Test
    public void testContainsOnlyString() {
        assertFalse(StringUtils.containsOnly(null, "abc"));
        assertFalse(StringUtils.containsOnly("ab", (String) null));
        assertTrue(StringUtils.containsOnly("", "abc"));
        assertFalse(StringUtils.containsOnly("ab", ""));
        assertTrue(StringUtils.containsOnly("abab", "abc"));
        assertFalse(StringUtils.containsOnly("ab1", "abc"));
        assertFalse(StringUtils.containsOnly("abz", "abc"));
    }

    @Test
    public void testContainsNoneCharArray() {
        assertTrue(StringUtils.containsNone(null, 'a'));
        assertTrue(StringUtils.containsNone("ab", (char[]) null));
        assertTrue(StringUtils.containsNone("", 'a'));
        assertTrue(StringUtils.containsNone("ab", new char[]{}));
        assertTrue(StringUtils.containsNone("abab", 'x', 'y', 'z'));
        assertFalse(StringUtils.containsNone("abz", 'x', 'y', 'z'));
    }

    @Test
    public void testContainsNoneString() {
        assertTrue(StringUtils.containsNone(null, "xyz"));
        assertTrue(StringUtils.containsNone("ab", (String) null));
        assertTrue(StringUtils.containsNone("", "xyz"));
        assertTrue(StringUtils.containsNone("ab", ""));
        assertTrue(StringUtils.containsNone("abab", "xyz"));
        assertFalse(StringUtils.containsNone("abz", "xyz"));
    }

    @Test
    public void testIndexOfAnyStrings() {
        assertEquals(-1, StringUtils.indexOfAny(null, "ab", "cd"));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (CharSequence[]) null));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx"));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "ab", "cd"));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "cd", "ab"));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", "mn", "op"));
        assertEquals(1, StringUtils.indexOfAny("zzabyycdxx", "zab", "aby"));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", ""));
        assertEquals(0, StringUtils.indexOfAny("", ""));
        assertEquals(-1, StringUtils.indexOfAny("", "a"));
    }

    @Test
    public void testLastIndexOfAny() {
        assertEquals(-1, StringUtils.lastIndexOfAny(null, "ab", "cd"));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", (CharSequence[]) null));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx"));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", "ab", "cd"));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", "cd", "ab"));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", "op"));
        assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", ""));
    }

    @Test
    public void testSubstring() {
        assertNull(StringUtils.subtring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));
    }

    @Test
    public void testSubstringWithEnd() {
        assertNull(StringUtils.subtring(null, 0, 2));
        assertEquals("", StringUtils.substring("", 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("", StringUtils.substring("abc", 2, 2));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));
    }

    @Test
    public void testLeft() {
        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("", 2));
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 4));
    }

    @Test
    public void testRight() {
        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("", 2));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 4));
    }

    @Test
    public void testMid() {
        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("", StringUtils.mid("", 0, 0));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("abc", StringUtils.mid("abc", 0, 4));
        assertEquals("c", StringUtils.mid("abc", 2, 4));
        assertEquals("", StringUtils.mid("abc", 4, 2));
        assertEquals("ab", StringUtils.mid("abc", -2, 2));
    }

    @Test
    public void testSubstringBefore() {
        assertNull(StringUtils.subtringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("", StringUtils.substringBefore("abc", ""));
        assertEquals("", StringUtils.substringBefore("abc", "a"));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("ab", StringUtils.substringBefore("abc", "c"));
        assertEquals("abc", StringUtils.substringBefore("abc", "d"));
    }

    @Test
    public void testSubstringAfter() {
        assertNull(StringUtils.subtringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("", StringUtils.substringAfter("abc", null));
        assertEquals("bc", StringUtils.substringAfter("abc", "a"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "c"));
        assertEquals("", StringUtils.substringAfter("abc", "d"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
    }

    @Test
    public void testSubstringBeforeLast() {
        assertNull(StringUtils.subtringBeforeLast(null, "a"));
        assertEquals("", StringUtils.substringBeforeLast("", "a"));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("ab", StringUtils.substringBeforeLast("abc", "c"));
        assertEquals("", StringUtils.substringBeforeLast("a", "a"));
        assertEquals("a", StringUtils.substringBeforeLast("a", "z"));
        assertEquals("a", StringUtils.substringBeforeLast("a", null));
        assertEquals("a", StringUtils.substringBeforeLast("a", ""));
    }

    @Test
    public void testSubstringAfterLast() {
        assertNull(StringUtils.substringAfterLast(null, "a"));
        assertEquals("", StringUtils.substringAfterLast("", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", null));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));
        assertEquals("bc", StringUtils.substringAfterLast("abc", "a"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtils.substringAfterLast("abc", "c"));
        assertEquals("", StringUtils.substringAfterLast("a", "a"));
        assertEquals("", StringUtils.substringAfterLast("a", "z"));
    }

    @Test
    public void testSubstringBetween() {
        assertNull(StringUtils.substringBetween(null, "tag"));
        assertEquals("", StringUtils.substringBetween("", ""));
        assertNull(StringUtils.substringBetween("", "tag"));
        assertNull(StringUtils.substringBetween("tagabctag", null));
        assertEquals("", StringUtils.substringBetween("tagabctag", ""));
        assertEquals("abc", StringUtils.substringBetween("tagabctag", "tag"));
    }

    @Test
    public void testSubstringBetweenWithOpenClose() {
        assertNull(StringUtils.substringBetween(null, "[", "]"));
        assertNull(StringUtils.substringBetween("wx[b]yz", null, "]"));
        assertNull(StringUtils.substringBetween("wx[b]yz", "[", null));
        assertEquals("", StringUtils.substringBetween("", "", ""));
        assertNull(StringUtils.substringBetween("", "", "]"));
        assertNull(StringUtils.substringBetween("", "[", "]"));
        assertEquals("", StringUtils.substringBetween("yabcz", "", ""));
        assertEquals("abc", StringUtils.substringBetween("yabcz", "y", "z"));
        assertEquals("abc", StringUtils.substringBetween("yabczyabcz", "y", "z"));
    }

    @Test
    public void testSubstringsBetween() {
        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("", "[", "]"));
        assertNull(StringUtils.substringsBetween("", "[", "]"));
        assertNull(StringUtils.substringsBetween("[a][b][c]", null, "]"));
        assertNull(StringUtils.substringsBetween("[a][b][c]", "[", null));
        assertNull(StringUtils.substringsBetween("[a][b][c]", "", "]"));
        assertArrayEquals(new String[]{"a","b","c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
        assertEquals(0, StringUtils.substringsBetween("", "[", "]").length);
 }
