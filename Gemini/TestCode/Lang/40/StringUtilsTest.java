package org.apache.commons.lang;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new StringUtils());
    }

    @Test
    public void testIsEmptyAndIsNotEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("abc"));
    }

    @Test
    public void testIsBlankAndIsNotBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertTrue(StringUtils.isBlank("\t\r\n"));
        assertFalse(StringUtils.isBlank("  a  "));
        assertFalse(StringUtils.isBlank("abc"));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("  a  "));
        assertTrue(StringUtils.isNotBlank("abc"));
    }

    @Test
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("   \t  "));
        assertEquals("abc", StringUtils.trim("  abc  "));
    }

    @Test
    public void testTrimToNullAndTrimToEmpty() {
        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }

    @Test
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("xyabcxz", "xyz"));
        assertNull(StringUtils.strip(null, "xyz"));
        assertEquals("", StringUtils.strip("", "xyz"));

        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertNull(StringUtils.stripToNull(""));
        assertEquals("abc", StringUtils.stripToNull("  abc  "));

        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("  abc  "));

        assertNull(StringUtils.stripStart(null, "a"));
        assertEquals("", StringUtils.stripStart("", "a"));
        assertEquals("abc", StringUtils.stripStart("abc", ""));
        assertEquals("abc", StringUtils.stripStart("   abc", null));
        assertEquals("abc", StringUtils.stripStart("xxabc", "x"));

        assertNull(StringUtils.stripEnd(null, "a"));
        assertEquals("", StringUtils.stripEnd("", "a"));
        assertEquals("abc", StringUtils.stripEnd("abc", ""));
        assertEquals("abc", StringUtils.stripEnd("abc   ", null));
        assertEquals("abc", StringUtils.stripEnd("abcxx", "x"));
    }

    @Test
    public void testStripAll() {
        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
        assertArrayEquals(new String[]{"a", "b", null}, StringUtils.stripAll(new String[]{" a ", "  b", null}));
        assertArrayEquals(new String[]{"a", "b", null}, StringUtils.stripAll(new String[]{"xay", "yb", null}, "xy"));
        assertNull(StringUtils.stripAll(null, "x"));
    }

    @Test
    public void testEqualsAndEqualsIgnoreCase() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "def"));
    }

    @Test
    public void testIndexOfAndLastIndexOfChar() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', -1));
    }

    @Test
    public void testIndexOfAndLastIndexOfString() {
        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("a", null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));

        assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        assertEquals(-1, StringUtils.indexOf("a", null, 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "", 2));
        assertEquals(3, StringUtils.indexOf("abc", "", 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));

        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("a", null, 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("a", "a", 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("a", "a", -1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "z", 1));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("a", null));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(1, StringUtils.lastIndexOf("aabaabaa", "ab"));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a", 0));
        assertEquals(-1, StringUtils.lastIndexOf("a", null, 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", "a", 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", 0));
    }

    @Test
    public void testContainsAndContainsIgnoreCase() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("a", null));
        assertTrue(StringUtils.contains("", ""));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "d"));

        assertFalse(StringUtils.containsIgnoreCase(null, "A"));
        assertFalse(StringUtils.containsIgnoreCase("a", null));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertTrue(StringUtils.containsIgnoreCase("ABC", "a"));
        assertFalse(StringUtils.containsIgnoreCase("ABC", "z"));
    }

    @Test
    public void testIndexOfAnyAndContainsAny() {
        assertEquals(-1, StringUtils.indexOfAny(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("", new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", new char[0]));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", new char[]{'b', 'y'}));
        assertEquals(-1, StringUtils.indexOfAny("aba", new char[]{'z'}));

        assertEquals(-1, StringUtils.indexOfAny(null, "a"));
        assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));

        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse(StringUtils.containsAny("", new char[]{'a'}));
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
        assertFalse(StringUtils.containsAny("abc", new char[0]));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertFalse(StringUtils.containsAny("aba", new char[]{'z'}));

        assertFalse(StringUtils.containsAny(null, "a"));
        assertFalse(StringUtils.containsAny("abc", (String) null));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertFalse(StringUtils.containsAny("aba", "z"));
    }

    @Test
    public void testIndexOfAnyBut() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("", new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", new char[0]));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", new char[]{'a', 'b'}));

        assertEquals(-1, StringUtils.indexOfAnyBut(null, "a"));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", ""));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));
    }

    @Test
    public void testContainsOnlyAndContainsNone() {
        assertFalse(StringUtils.containsOnly(null, new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("a", (char[]) null));
        assertTrue(StringUtils.containsOnly("", new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("ab", new char[0]));
        assertTrue(StringUtils.containsOnly("abab", new char[]{'a', 'b', 'c'}));
        assertFalse(StringUtils.containsOnly("ab1", new char[]{'a', 'b', 'c'}));

        assertFalse(StringUtils.containsOnly(null, "a"));
        assertFalse(StringUtils.containsOnly("a", (String) null));
        assertTrue(StringUtils.containsOnly("abab", "abc"));

        assertTrue(StringUtils.containsNone(null, new char[]{'a'}));
        assertTrue(StringUtils.containsNone("a", (char[]) null));
        assertTrue(StringUtils.containsNone("", new char[]{'a'}));
        assertTrue(StringUtils.containsNone("abab", new char[]{'x', 'y'}));
        assertFalse(StringUtils.containsNone("abz", new char[]{'x', 'y', 'z'}));

        assertTrue(StringUtils.containsNone(null, "a"));
        assertTrue(StringUtils.containsNone("a", (String) null));
        assertTrue(StringUtils.containsNone("abab", "xyz"));
        assertFalse(StringUtils.containsNone("abz", "xyz"));
    }

    @Test
    public void testIndexOfAnyStrings() {
        assertEquals(-1, StringUtils.indexOfAny(null, new String[]{"a"}));
        assertEquals(-1, StringUtils.indexOfAny("a", (String[]) null));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", new String[]{"cd", "ab"}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new String[]{"mn", "op"}));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new String[]{"", null}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new String[]{null}));

        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[]{"a"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("a", (String[]) null));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"mn", "op"}));
        assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"mn", ""}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{null}));
    }

    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));

        assertNull(StringUtils.substring(null, 0, 1));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("", StringUtils.substring("abc", 2, 2));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));
        assertEquals("", StringUtils.substring("abc", -2, -3));
        assertEquals("", StringUtils.substring("abc", -4, -4));
    }

    @Test
    public void testLeftRightMid() {
        assertNull(StringUtils.left(null, 1));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("abc", StringUtils.left("abc", 4));
        assertEquals("ab", StringUtils.left("abc", 2));

        assertNull(StringUtils.right(null, 1));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("abc", StringUtils.right("abc", 4));
        assertEquals("bc", StringUtils.right("abc", 2));

        assertNull(StringUtils.mid(null, 0, 1));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("", StringUtils.mid("abc", 4, 2));
        assertEquals("ab", StringUtils.mid("abc", -2, 2));
        assertEquals("c", StringUtils.mid("abc", 2, 4));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
    }

    @Test
    public void testSubstringBeforeAndAfter() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("", StringUtils.substringBefore("abc", ""));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("abc", StringUtils.substringBefore("abc", "d"));

        assertNull(StringUtils.substringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("", StringUtils.substringAfter("abc", null));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "d"));

        assertNull(StringUtils.substringBeforeLast(null, "a"));
        assertEquals("", StringUtils.substringBeforeLast("", "a"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", null));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", ""));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", "d"));

        assertNull(StringUtils.substringAfterLast(null, "a"));
        assertEquals("", StringUtils.substringAfterLast("", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", null));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtils.substringAfterLast("abc", "d"));
        assertEquals("", StringUtils.substringAfterLast("abc", "c"));
    }

    @Test
    public void testSubstringBetween() {
        assertNull(StringUtils.substringBetween(null, "a"));
        assertEquals("b", StringUtils.substringBetween("aba", "a"));

        assertNull(StringUtils.substringBetween(null, "[", "]"));
        assertNull(StringUtils.substringBetween("abc", null, "]"));
        assertNull(StringUtils.substringBetween("abc", "[", null));
        assertEquals("", StringUtils.substringBetween("abc", "", ""));
        assertNull(StringUtils.substringBetween("abc", "[", "]"));
        assertEquals("b", StringUtils.substringBetween("a[b]c", "[", "]"));

        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", "", "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", ""));
        assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", "]"));
        assertNull(StringUtils.substringsBetween("a[b", "[", "]"));
        assertArrayEquals(new String[]{"b", "d"}, StringUtils.substringsBetween("a[b]c[d]e", "[", "]"));
    }

    @Test
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a:b;c", ":;"));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":", 0));
        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.split("ab cd ef", null, 2));

        assertNull(StringUtils.splitPreserveAllTokens(null));
        assertArrayEquals(new String[0], StringUtils.splitPreserveAllTokens(""));
        assertArrayEquals(new String[]{"abc", "", "def"}, StringUtils.splitPreserveAllTokens("abc  def"));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"", "a", ""}, StringUtils.splitPreserveAllTokens(".a.", '.'));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a::b;c", ":;"));
        assertArrayEquals(new String[]{"a", ":b:c"}, StringUtils.splitPreserveAllTokens("a::b:c", ":", 2));
        assertArrayEquals(new String[]{"", "a", "b"}, StringUtils.splitPreserveAllTokens(" a b", null, 3));
    }

    @Test
    public void testSplitByWholeSeparator() {
        assertNull(StringUtils.splitByWholeSeparator(null, " "));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", " "));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab de fg", null));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab de fg", ""));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 5));
        assertArrayEquals(new String[]{"ab", "cd"}, StringUtils.splitByWholeSeparator("ab-!-!-cd", "-!-"));

        assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, " "));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparatorPreserveAllTokens("", " "));
        assertArrayEquals(new String[]{"ab", "", "de", "fg"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab  de fg", null));
        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-!-cd", "-!-"));
        assertArrayEquals(new String[]{"ab", "-!-cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-!-cd", "-!-", 2));
    }

    @Test
    public void testSplitByCharacterType() {
        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
        assertArrayEquals(new String[]{"ab", " ", "de"}, StringUtils.splitByCharacterType("ab de"));
        assertArrayEquals(new String[]{"number", "5"}, StringUtils.splitByCharacterType("number5"));
        assertArrayEquals(new String[]{"foo", "B", "ar"}, StringUtils.splitByCharacterType("fooBar"));

        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterTypeCamelCase(""));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    @Test
    public void testJoin() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[0]));
        assertEquals("", StringUtils.join(new Object[]{null}));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("", StringUtils.join(new Object[]{"a", "b"}, ';', 1, 1));
        assertEquals("a, b, c", StringUtils.join(new Object[]{"a", "b", "c"}, ", "));
        assertEquals("a;b", StringUtils.join(new Object[]{"a", "b"}, ";", 0, 2));
        assertEquals("", StringUtils.join(new Object[]{"a", "b"}, ";", 2, 1));
        assertEquals("a", StringUtils.join(new Object[]{null, "a"}, ";", 0, 2));

        assertNull(StringUtils.join((java.util.Iterator<?>) null, ';'));
        assertEquals("", StringUtils.join(Collections.emptyList().iterator(), ';'));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ';'));
        assertEquals("a;b", StringUtils.join(Arrays.asList("a", "b").iterator(), ';'));
        assertEquals("a;b", StringUtils.join(Arrays.asList("a", null, "b").iterator(), ';'));

        assertNull(StringUtils.join((java.util.Iterator<?>) null, "--"));
        assertEquals("", StringUtils.join(Collections.emptyList().iterator(), "--"));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), "--"));
        assertEquals("a--b", StringUtils.join(Arrays.asList("a", "b").iterator(), "--"));

        assertNull(StringUtils.join((List<?>) null, ';'));
        assertEquals("a;b", StringUtils.join(Arrays.asList("a", "b"), ';'));
        assertNull(StringUtils.join((List<?>) null, "--"));
        assertEquals("a--b", StringUtils.join(Arrays.asList("a", "b"), "--"));
    }

    @Test
    public void testDeleteWhitespace() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace("  a  b  c  "));
        assertEquals("abc", StringUtils.deleteWhitespace("abc"));
    }

    @Test
    public void testRemove() {
        assertNull(StringUtils.removeStart(null, "a"));
        assertEquals("", StringUtils.removeStart("", "a"));
        assertEquals("abc", StringUtils.removeStart("abc", null));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStart("domain.com", "www."));

        assertNull(StringUtils.removeStartIgnoreCase(null, "a"));
        assertEquals("", StringUtils.removeStartIgnoreCase("", "a"));
        assertEquals("abc", StringUtils.removeStartIgnoreCase("abc", null));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("domain.com", "www."));

        assertNull(StringUtils.removeEnd(null, "a"));
        assertEquals("", StringUtils.removeEnd("", "a"));
        assertEquals("abc", StringUtils.removeEnd("abc", null));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain.com", StringUtils.removeEnd("www.domain.com", ".net"));

        assertNull(StringUtils.removeEndIgnoreCase(null, "a"));
        assertEquals("", StringUtils.removeEndIgnoreCase("", "a"));
        assertEquals("abc", StringUtils.removeEndIgnoreCase("abc", null));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));
        assertEquals("www.domain.com", StringUtils.removeEndIgnoreCase("www.domain.com", ".net"));

        assertNull(StringUtils.remove((String) null, "a"));
        assertEquals("", StringUtils.remove("", "a"));
        assertEquals("abc", StringUtils.remove("abc", (String) null));
        assertEquals("qd", StringUtils.remove("queued", "ue"));

        assertNull(StringUtils.remove((String) null, 'a'));
        assertEquals("", StringUtils.remove("", 'a'));
        assertEquals("abc", StringUtils.remove("abc", 'z'));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
    }

    @Test
    public void testReplace() {
        assertNull(StringUtils.replaceOnce(null, "a", "b"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
        assertEquals("aba", StringUtils.replace("aba", null, "z"));
        assertEquals("aba", StringUtils.replace("aba", "a", null));
        assertEquals("aba", StringUtils.replace("aba", "", "z"));
        assertEquals("aba", StringUtils.replace("aba", "a", "z", 0));
        assertEquals("zbaa", StringUtils.replace("abaa", "a", "z", 1));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));

        assertNull(StringUtils.replaceChars((String) null, 'a', 'b'));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertNull(StringUtils.replaceChars(null, "a", "b"));
        assertEquals("", StringUtils.replaceChars("", "a", "b"));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "b"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));
        assertEquals("abcba", StringUtils.replaceChars("abcba", "z", "x"));
    }

    @Test
    public void testReplaceEach() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, null));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], null));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[0]));
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null, "z"}, new String[]{"a", "b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{null}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"z"}, new String[]{"x"}));
        assertEquals("longer-longer-a", StringUtils.replaceEach("a-a-a", new String[]{"a"}, new String[]{"longer"}));

        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        assertNull(StringUtils.replaceEachRepeatedly(null, new String[]{"a"}, new String[]{"b"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEachLengthMismatch() {
        StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{"b", "c"});
    }

    @Test(expected = IllegalStateException.class)
    public void testReplaceEachCircular() {
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"a", "b"}, new String[]{"b", "a"});
    }

    @Test
    public void testOverlay() {
        assertNull(StringUtils.overlay(null, "abc", 0, 0));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("abzzzz", StringUtils.overlay("abcdef", "zzzz", 2, 8));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
        assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
    }

    @Test
    public void testChompAndChop() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc"));
        assertEquals("", StringUtils.chomp("\r"));
        assertEquals("", StringUtils.chomp("\n"));
        assertEquals("a", StringUtils.chomp("a"));

        assertNull(StringUtils.chomp(null, "a"));
        assertEquals("", StringUtils.chomp("", "a"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("foobar", StringUtils.chomp("foobar", "baz"));
        assertEquals("foo", StringUtils.chomp("foo", null));

        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\n"));
    }

    @Test
    public void testRepeat() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("", StringUtils.repeat("abc", -1));
        assertEquals("abc", StringUtils.repeat("abc", 1));
        assertEquals("", StringUtils.repeat("", 3));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("abcabcabc", StringUtils.repeat("abc", 3));

        assertNull(StringUtils.repeat(null, ",", 2));
        assertEquals("a,a,a", StringUtils.repeat("a", ",", 3));
        assertEquals("aaa", StringUtils.repeat("a", null, 3));
    }

    @Test
    public void testRightPadAndLeftPad() {
        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("bat", StringUtils.rightPad("bat", 3));
        assertEquals("bat", StringUtils.rightPad("bat", 1));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("bat", StringUtils.rightPad("bat", 1, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5, ""));

        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("bat", StringUtils.leftPad("bat", 3));
        assertEquals("bat", StringUtils.leftPad("bat", 1));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("bat", StringUtils.leftPad("bat", 1, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        assertEquals("  bat", StringUtils.leftPad("bat", 5, ""));
    }

    @Test
    public void testLengthAndCenter() {
        assertEquals(0, StringUtils.length(null));
        assertEquals(3, StringUtils.length("abc"));

        assertNull(StringUtils.center(null, 4));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals("abcd", StringUtils.center("abcd", 2));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("  abc  ", StringUtils.center("abc", 7, ""));
        assertEquals("abc", StringUtils.center("abc", 2, " "));
    }

    @Test
    public void testCaseConversions() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("aBc"));
        assertNull(StringUtils.upperCase(null, Locale.ENGLISH));
        assertEquals("ABC", StringUtils.upperCase("aBc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("aBc"));
        assertNull(StringUtils.lowerCase(null, Locale.ENGLISH));
        assertEquals("abc", StringUtils.lowerCase("aBc", Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Cat", StringUtils.capitalize("cat"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    @Test
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abc", null));
        assertEquals(0, StringUtils.countMatches("abc", ""));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "ab"));
        assertEquals(0, StringUtils.countMatches("abba", "z"));
    }

    @Test
    public void testCharacterTests() {
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));

        assertFalse(StringUtils.isAlphaSpace(null));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab2c"));

        assertFalse(StringUtils.isAlphanumeric(null));
        assertTrue(StringUtils.isAlphanumeric(""));
        assertTrue(StringUtils.isAlphanumeric("ab2c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));

        assertFalse(StringUtils.isAlphanumericSpace(null));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertTrue(StringUtils.isAlphanumericSpace("ab 2 c"));
        assertFalse(StringUtils.isAlphanumericSpace("ab-c"));

        assertFalse(StringUtils.isAsciiPrintable(null));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertTrue(StringUtils.isAsciiPrintable("abc 123 !~"));
        assertFalse(StringUtils.isAsciiPrintable("\u007f"));

        assertFalse(StringUtils.isNumeric(null));
        assertTrue(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12 3"));

        assertFalse(StringUtils.isNumericSpace(null));
        assertTrue(StringUtils.isNumericSpace(""));
        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertFalse(StringUtils.isNumericSpace("12-3"));

        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace("  \t\n"));
        assertFalse(StringUtils.isWhitespace(" a "));

        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("aBc"));

        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(""));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    @Test
    public void testDefaults() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("abc", StringUtils.defaultString("abc", "default"));

        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "default"));
    }

    @Test
    public void testReversing() {
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("cba", StringUtils.reverse("abc"));

        assertNull(StringUtils.reverseDelimited(null, '.'));
        assertEquals("", StringUtils.reverseDelimited("", '.'));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
        assertEquals("a.b.c", StringUtils.reverseDelimited("a.b.c", 'x'));
    }

    @Test
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 5));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateInvalidWidth() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateInvalidOffsetWidth() {
        StringUtils.abbreviate("abcdefghij", 5, 6);
    }

    @Test
    public void testDifferenceAndCommonPrefix() {
        assertNull(StringUtils.difference(null, null));
        assertEquals("abc", StringUtils.difference(null, "abc"));
        assertEquals("abc", StringUtils.difference("abc", null));
        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.difference("abc", "abc"));

        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", null));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(1, StringUtils.indexOfDifference("abc", "axc"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));

        assertEquals(-1, StringUtils.indexOfDifference((String[]) null));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"abc"}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{null, null}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"", ""}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{"abc", null}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{"", "abc"}));
        assertEquals(7, StringUtils.indexOfDifference(new String[]{"i am a machine", "i am a robot"}));
        assertEquals(1, StringUtils.indexOfDifference(new String[]{"abc", "a"}));

        assertEquals("", StringUtils.getCommonPrefix(null));
        assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{null, null}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[]{"abc", "abc"}));
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"xyz", "abcde"}));
    }

    @Test
    public void testLevenshteinDistance() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinDistanceNull1() {
        StringUtils.getLevenshteinDistance(null, "a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLevenshteinDistanceNull2() {
        StringUtils.getLevenshteinDistance("a", null);
    }

    @Test
    public void testStartsWithAndEndsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
        assertFalse(StringUtils.startsWith("abc", "abcdef"));

        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));

        assertFalse(StringUtils.startsWithAny(null, new String[]{"abc"}));
        assertFalse(StringUtils.startsWithAny("abc", null));
        assertFalse(StringUtils.startsWithAny("abc", new String[0]));
        assertFalse(StringUtils.startsWithAny("abc", new String[]{"def"}));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{null, "xyz", "abc"}));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        assertFalse(StringUtils.endsWith("def", "abcdef"));

        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
    }
}
