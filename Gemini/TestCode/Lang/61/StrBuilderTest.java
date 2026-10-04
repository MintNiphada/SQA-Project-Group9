package org.apache.commons.lang.text;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class StrBuilderTest {

    @Test
    public void testConstructors() {
        StrBuilder sb1 = new StrBuilder();
        Assert.assertEquals(32, sb1.capacity());
        Assert.assertEquals(0, sb1.length());

        StrBuilder sb2 = new StrBuilder(0);
        Assert.assertEquals(32, sb2.capacity());

        StrBuilder sb3 = new StrBuilder(-5);
        Assert.assertEquals(32, sb3.capacity());

        StrBuilder sb4 = new StrBuilder(50);
        Assert.assertEquals(50, sb4.capacity());

        StrBuilder sb5 = new StrBuilder((String) null);
        Assert.assertEquals(32, sb5.capacity());
        Assert.assertEquals(0, sb5.length());

        StrBuilder sb6 = new StrBuilder("Hello");
        Assert.assertEquals(37, sb6.capacity());
        Assert.assertEquals("Hello", sb6.toString());
    }

    @Test
    public void testNewLineAndNullText() {
        StrBuilder sb = new StrBuilder();
        Assert.assertNull(sb.getNewLineText());
        sb.setNewLineText("\r\n");
        Assert.assertEquals("\r\n", sb.getNewLineText());

        sb.append("A").appendNewLine().append("B");
        Assert.assertEquals("A\r\nB", sb.toString());

        sb.setNewLineText(null);
        Assert.assertNull(sb.getNewLineText());
        sb.clear().append("A").appendNewLine().append("B");
        Assert.assertEquals("A" + SystemUtils.LINE_SEPARATOR + "B", sb.toString());

        Assert.assertNull(sb.getNullText());
        sb.setNullText("NULL");
        Assert.assertEquals("NULL", sb.getNullText());
        sb.clear().appendNull().append((String) null).append((Object) null);
        Assert.assertEquals("NULLNULLNULL", sb.toString());

        sb.setNullText("");
        Assert.assertNull(sb.getNullText());
        sb.setNullText("test");
        sb.setNullText(null);
        Assert.assertNull(sb.getNullText());
    }

    @Test
    public void testLengthAndCapacity() {
        StrBuilder sb = new StrBuilder();
        Assert.assertEquals(0, sb.length());
        Assert.assertEquals(0, sb.size());
        Assert.assertTrue(sb.isEmpty());

        sb.append("Hello World");
        Assert.assertFalse(sb.isEmpty());
        Assert.assertEquals(11, sb.length());

        sb.setLength(5);
        Assert.assertEquals("Hello", sb.toString());
        Assert.assertEquals(5, sb.length());

        sb.setLength(8);
        Assert.assertEquals(8, sb.length());
        Assert.assertEquals("Hello\0\0\0", sb.toString());

        sb.setLength(8);
        Assert.assertEquals(8, sb.length());

        sb.clear();
        Assert.assertEquals(0, sb.length());
        Assert.assertTrue(sb.isEmpty());

        sb.ensureCapacity(100);
        Assert.assertTrue(sb.capacity() >= 100);
        sb.ensureCapacity(10);
        Assert.assertTrue(sb.capacity() >= 100);

        sb.append("Test");
        sb.minimizeCapacity();
        Assert.assertEquals(4, sb.capacity());
        sb.minimizeCapacity();
        Assert.assertEquals(4, sb.capacity());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetLengthNegative() {
        new StrBuilder().setLength(-1);
    }

    @Test
    public void testCharAtAndSetCharAtAndDeleteCharAt() {
        StrBuilder sb = new StrBuilder("012345");
        Assert.assertEquals('0', sb.charAt(0));
        Assert.assertEquals('5', sb.charAt(5));

        sb.setCharAt(0, 'A');
        Assert.assertEquals("A12345", sb.toString());

        sb.deleteCharAt(1);
        Assert.assertEquals("A2345", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtNegative() {
        new StrBuilder("abc").charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtTooLarge() {
        new StrBuilder("abc").charAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtNegative() {
        new StrBuilder("abc").setCharAt(-1, 'z');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtTooLarge() {
        new StrBuilder("abc").setCharAt(3, 'z');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtNegative() {
        new StrBuilder("abc").deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtTooLarge() {
        new StrBuilder("abc").deleteCharAt(3);
    }

    @Test
    public void testToCharArrayAndGetChars() {
        StrBuilder sbEmpty = new StrBuilder();
        Assert.assertArrayEquals(new char[0], sbEmpty.toCharArray());
        Assert.assertArrayEquals(new char[0], sbEmpty.toCharArray(0, 0));

        StrBuilder sb = new StrBuilder("abcdef");
        Assert.assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, sb.toCharArray());
        Assert.assertArrayEquals(new char[]{'b', 'c', 'd'}, sb.toCharArray(1, 4));
        Assert.assertArrayEquals(new char[0], sb.toCharArray(2, 2));
        Assert.assertArrayEquals(new char[]{'d', 'e', 'f'}, sb.toCharArray(3, 10));

        char[] dest = sb.getChars(null);
        Assert.assertEquals(6, dest.length);
        Assert.assertEquals("abcdef", new String(dest));

        char[] smallDest = new char[2];
        char[] resized = sb.getChars(smallDest);
        Assert.assertEquals(6, resized.length);
        Assert.assertEquals("abcdef", new String(resized));

        char[] exactDest = new char[6];
        char[] returned = sb.getChars(exactDest);
        Assert.assertSame(exactDest, returned);
        Assert.assertEquals("abcdef", new String(exactDest));

        char[] customDest = new char[10];
        sb.getChars(1, 4, customDest, 2);
        Assert.assertEquals("bcd", new String(customDest, 2, 3));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsNegativeStart() {
        new StrBuilder("abc").getChars(-1, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsNegativeEnd() {
        new StrBuilder("abc").getChars(0, -1, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsEndGreaterThanLength() {
        new StrBuilder("abc").getChars(0, 4, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsStartGreaterThanEnd() {
        new StrBuilder("abc").getChars(2, 1, new char[5], 0);
    }

    @Test
    public void testAppendPrimitivesAndObjects() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false);
        Assert.assertEquals("truefalse", sb.toString());

        sb.clear();
        sb.append('!');
        Assert.assertEquals("!", sb.toString());

        sb.clear();
        sb.append(123).append(456789012345L).append(1.5f).append(2.5d);
        Assert.assertEquals("1234567890123451.52.5", sb.toString());

        sb.clear();
        sb.append((Object) "foo");
        Assert.assertEquals("foo", sb.toString());
    }

    @Test
    public void testAppendStringsAndBuffers() {
        StrBuilder sb = new StrBuilder();
        sb.append((String) null);
        sb.append("abc");
        sb.append("");
        Assert.assertEquals("abc", sb.toString());

        sb.clear();
        sb.append((String) null, 0, 0);
        sb.append("abcdef", 1, 3);
        sb.append("abcdef", 0, 0);
        Assert.assertEquals("bcd", sb.toString());

        sb.clear();
        sb.append((StringBuffer) null);
        sb.append(new StringBuffer("xyz"));
        sb.append(new StringBuffer(""));
        Assert.assertEquals("xyz", sb.toString());

        sb.clear();
        sb.append((StringBuffer) null, 0, 0);
        sb.append(new StringBuffer("abcdef"), 1, 3);
        sb.append(new StringBuffer("abcdef"), 0, 0);
        Assert.assertEquals("bcd", sb.toString());

        sb.clear();
        sb.append((StrBuilder) null);
        sb.append(new StrBuilder("qwe"));
        sb.append(new StrBuilder(""));
        Assert.assertEquals("qwe", sb.toString());

        sb.clear();
        sb.append((StrBuilder) null, 0, 0);
        sb.append(new StrBuilder("abcdef"), 1, 3);
        sb.append(new StrBuilder("abcdef"), 0, 0);
        Assert.assertEquals("bcd", sb.toString());

        sb.clear();
        sb.append((char[]) null);
        sb.append(new char[]{'a', 'b', 'c'});
        sb.append(new char[0]);
        Assert.assertEquals("abc", sb.toString());

        sb.clear();
        sb.append((char[]) null, 0, 0);
        sb.append(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, 1, 3);
        sb.append(new char[]{'a', 'b', 'c'}, 0, 0);
        Assert.assertEquals("bcd", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringOutOfBounds1() {
        new StrBuilder().append("abc", -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringOutOfBounds2() {
        new StrBuilder().append("abc", 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringOutOfBounds3() {
        new StrBuilder().append("abc", 0, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringOutOfBounds4() {
        new StrBuilder().append("abc", 1, 3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferOutOfBounds1() {
        new StrBuilder().append(new StringBuffer("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferOutOfBounds2() {
        new StrBuilder().append(new StringBuffer("abc"), 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferOutOfBounds3() {
        new StrBuilder().append(new StringBuffer("abc"), 0, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferOutOfBounds4() {
        new StrBuilder().append(new StringBuffer("abc"), 1, 3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderOutOfBounds1() {
        new StrBuilder().append(new StrBuilder("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderOutOfBounds2() {
        new StrBuilder().append(new StrBuilder("abc"), 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderOutOfBounds3() {
        new StrBuilder().append(new StrBuilder("abc"), 0, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderOutOfBounds4() {
        new StrBuilder().append(new StrBuilder("abc"), 1, 3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayOutOfBounds1() {
        new StrBuilder().append(new char[]{'a', 'b'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayOutOfBounds2() {
        new StrBuilder().append(new char[]{'a', 'b'}, 3, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayOutOfBounds3() {
        new StrBuilder().append(new char[]{'a', 'b'}, 0, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayOutOfBounds4() {
        new StrBuilder().append(new char[]{'a', 'b'}, 1, 2);
    }

    @Test
    public void testAppendWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        sb.appendWithSeparators(new Object[0], ",");
        Assert.assertEquals(0, sb.length());

        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        Assert.assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"A", "B"}, null);
        Assert.assertEquals("AB", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Collection<?>) null, ",");
        sb.appendWithSeparators(Collections.emptyList(), ",");
        sb.appendWithSeparators(Arrays.asList("A", "B", "C"), ",");
        Assert.assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("A", "B"), null);
        Assert.assertEquals("AB", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator<?>) null, ",");
        sb.appendWithSeparators(Arrays.asList("A", "B", "C").iterator(), ",");
        Assert.assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("A", "B").iterator(), null);
        Assert.assertEquals("AB", sb.toString());
    }

    @Test
    public void testPaddingAndFixedWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-1, 'x');
        sb.appendPadding(0, 'x');
        sb.appendPadding(3, '-');
        Assert.assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(null, -1, ' ');
        sb.appendFixedWidthPadLeft(null, 0, ' ');
        sb.appendFixedWidthPadLeft("abc", 2, ' ');
        sb.appendFixedWidthPadLeft("abc", 5, '-');
        sb.setNullText("N");
        sb.appendFixedWidthPadLeft(null, 3, '.');
        sb.setNullText(null);
        Assert.assertEquals("bc--abc..N", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(12345, 3, ' ');
        sb.appendFixedWidthPadLeft(12, 4, '-');
        Assert.assertEquals("345--12", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(null, -1, ' ');
        sb.appendFixedWidthPadRight(null, 0, ' ');
        sb.appendFixedWidthPadRight("abc", 2, ' ');
        sb.appendFixedWidthPadRight("abc", 5, '-');
        sb.setNullText("N");
        sb.appendFixedWidthPadRight(null, 3, '.');
        sb.setNullText(null);
        Assert.assertEquals("ababc--N..", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(12345, 3, ' ');
        sb.appendFixedWidthPadRight(12, 4, '-');
        Assert.assertEquals("12312--", sb.toString());
    }

    @Test
    public void testInserts() {
        StrBuilder sb = new StrBuilder("123");
        sb.insert(1, (Object) "foo");
        Assert.assertEquals("1foo23", sb.toString());

        sb.insert(0, (Object) null);
        Assert.assertEquals("1foo23", sb.toString());

        sb.setNullText("NULL");
        sb.insert(0, (Object) null);
        Assert.assertEquals("NULL1foo23", sb.toString());
        sb.setNullText(null);

        sb.clear().append("123");
        sb.insert(1, (String) null);
        sb.insert(1, "");
        sb.insert(1, "x");
        Assert.assertEquals("1x23", sb.toString());

        sb.clear().append("123");
        sb.insert(1, (char[]) null);
        sb.insert(1, new char[0]);
        sb.insert(1, new char[]{'x', 'y'});
        Assert.assertEquals("1xy23", sb.toString());

        sb.clear().append("123");
        sb.insert(1, (char[]) null, 0, 0);
        sb.insert(1, new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        sb.insert(1, new char[]{'a', 'b'}, 0, 0);
        Assert.assertEquals("1bc23", sb.toString());

        sb.clear().append("12");
        sb.insert(1, true);
        sb.insert(1, false);
        Assert.assertEquals("1falsetrue2", sb.toString());

        sb.clear().append("12");
        sb.insert(1, 'A');
        sb.insert(1, 10);
        sb.insert(1, 20L);
        sb.insert(1, 1.5f);
        sb.insert(1, 2.5d);
        Assert.assertEquals("12.51.52010A2", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertStringInvalidIndex() {
        new StrBuilder().insert(1, "a");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharsInvalidOffset() {
        new StrBuilder().insert(0, new char[]{'a'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharsInvalidLength() {
        new StrBuilder().insert(0, new char[]{'a'}, 0, 2);
    }

    @Test
    public void testDeletions() {
        StrBuilder sb = new StrBuilder("0123456789");
        sb.delete(2, 5);
        Assert.assertEquals("0156789", sb.toString());
        sb.delete(5, 100);
        Assert.assertEquals("01567", sb.toString());
        sb.delete(2, 2);
        Assert.assertEquals("01567", sb.toString());

        sb = new StrBuilder("aabaacaad");
        sb.deleteAll('a');
        Assert.assertEquals("bcd", sb.toString());

        sb = new StrBuilder("aabaacaad");
        sb.deleteFirst('a');
        Assert.assertEquals("abaacaad", sb.toString());
        sb.deleteFirst('z');
        Assert.assertEquals("abaacaad", sb.toString());

        sb = new StrBuilder("foobarfoobazfoo");
        sb.deleteAll((String) null);
        sb.deleteAll("");
        sb.deleteAll("foo");
        Assert.assertEquals("barbaz", sb.toString());

        sb = new StrBuilder("foobarfoobazfoo");
        sb.deleteFirst((String) null);
        sb.deleteFirst("");
        sb.deleteFirst("foo");
        Assert.assertEquals("barfoobazfoo", sb.toString());
        sb.deleteFirst("qux");
        Assert.assertEquals("barfoobazfoo", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.deleteAll((StrMatcher) null);
        sb.deleteAll(StrMatcher.charMatcher('b'));
        Assert.assertEquals("a12c3", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.deleteFirst((StrMatcher) null);
        sb.deleteFirst(StrMatcher.charMatcher('b'));
        Assert.assertEquals("a12c3", sb.toString());
    }

    @Test
    public void testReplaces() {
        StrBuilder sb = new StrBuilder("012345");
        sb.replace(1, 4, "XYZ");
        Assert.assertEquals("0XYZ45", sb.toString());
        sb.replace(1, 4, null);
        Assert.assertEquals("045", sb.toString());

        sb = new StrBuilder("banana");
        sb.replaceAll('a', 'a');
        Assert.assertEquals("banana", sb.toString());
        sb.replaceAll('a', 'o');
        Assert.assertEquals("bonono", sb.toString());

        sb = new StrBuilder("banana");
        sb.replaceFirst('a', 'a');
        Assert.assertEquals("banana", sb.toString());
        sb.replaceFirst('a', 'o');
        Assert.assertEquals("bonana", sb.toString());
        sb.replaceFirst('z', 'x');
        Assert.assertEquals("bonana", sb.toString());

        sb = new StrBuilder("the quick brown fox the fox");
        sb.replaceAll((String) null, "a");
        sb.replaceAll("", "a");
        sb.replaceAll("fox", "cat");
        Assert.assertEquals("the quick brown cat the cat", sb.toString());
        sb.replaceAll("cat", null);
        Assert.assertEquals("the quick brown  the ", sb.toString());

        sb = new StrBuilder("the quick brown fox the fox");
        sb.replaceFirst((String) null, "a");
        sb.replaceFirst("", "a");
        sb.replaceFirst("fox", "cat");
        Assert.assertEquals("the quick brown cat the fox", sb.toString());
        sb.replaceFirst("cat", null);
        Assert.assertEquals("the quick brown  the fox", sb.toString());
        sb.replaceFirst("qux", "cat");
        Assert.assertEquals("the quick brown  the fox", sb.toString());

        sb = new StrBuilder("a-b-c-d");
        sb.replaceAll((StrMatcher) null, "x");
        StrBuilder emptySb = new StrBuilder();
        emptySb.replaceAll(StrMatcher.charMatcher('-'), "x");
        Assert.assertEquals("", emptySb.toString());

        sb.replaceAll(StrMatcher.charMatcher('-'), "+");
        Assert.assertEquals("a+b+c+d", sb.toString());

        sb = new StrBuilder("a-b-c-d");
        sb.replaceFirst(StrMatcher.charMatcher('-'), "+");
        Assert.assertEquals("a+b-c-d", sb.toString());

        sb = new StrBuilder("a-b-c-d");
        sb.replace(StrMatcher.charMatcher('-'), "+", 2, 6, 1);
        Assert.assertEquals("a-b+c-d", sb.toString());
        sb.replace(StrMatcher.charMatcher('-'), null, 0, sb.length(), -1);
        Assert.assertEquals("ab+cd", sb.toString());
    }

    @Test
    public void testReverseAndTrim() {
        StrBuilder sb = new StrBuilder();
        sb.reverse();
        Assert.assertEquals("", sb.toString());

        sb.append("12345");
        sb.reverse();
        Assert.assertEquals("54321", sb.toString());

        sb.clear();
        sb.append("1234");
        sb.reverse();
        Assert.assertEquals("4321", sb.toString());

        sb.clear();
        sb.trim();
        Assert.assertEquals("", sb.toString());

        sb.append("   hello   ");
        sb.trim();
        Assert.assertEquals("hello", sb.toString());

        sb.clear().append("   ");
        sb.trim();
        Assert.assertEquals("", sb.toString());

        sb.clear().append("no-trim");
        sb.trim();
        Assert.assertEquals("no-trim", sb.toString());
    }

    @Test
    public void testStartsAndEndsWith() {
        StrBuilder sb = new StrBuilder("hello world");
        Assert.assertFalse(sb.startsWith(null));
        Assert.assertTrue(sb.startsWith(""));
        Assert.assertTrue(sb.startsWith("hello"));
        Assert.assertFalse(sb.startsWith("hello world longer text"));
        Assert.assertFalse(sb.startsWith("world"));

        Assert.assertFalse(sb.endsWith(null));
        Assert.assertTrue(sb.endsWith(""));
        Assert.assertTrue(sb.endsWith("world"));
        Assert.assertFalse(sb.endsWith("hello world longer text"));
        Assert.assertFalse(sb.endsWith("hello"));
    }

    @Test
    public void testSubstringsAndExtracts() {
        StrBuilder sb = new StrBuilder("hello world");
        Assert.assertEquals("world", sb.substring(6));
        Assert.assertEquals("hello", sb.substring(0, 5));
        Assert.assertEquals("world", sb.substring(6, 20));

        Assert.assertEquals("", sb.leftString(-1));
        Assert.assertEquals("", sb.leftString(0));
        Assert.assertEquals("hel", sb.leftString(3));
        Assert.assertEquals("hello world", sb.leftString(20));

        Assert.assertEquals("", sb.rightString(-1));
        Assert.assertEquals("", sb.rightString(0));
        Assert.assertEquals("rld", sb.rightString(3));
        Assert.assertEquals("hello world", sb.rightString(20));

        Assert.assertEquals("", sb.midString(-5, -1));
        Assert.assertEquals("", sb.midString(20, 5));
        Assert.assertEquals("", sb.midString(2, 0));
        Assert.assertEquals("llo", sb.midString(-5, 3));
        Assert.assertEquals("llo world", sb.midString(2, 20));
        Assert.assertEquals("llo", sb.midString(2, 3));
    }

    @Test
    public void testContainsAndIndexOf() {
        StrBuilder sb = new StrBuilder("ababcdcd");
        Assert.assertTrue(sb.contains('a'));
        Assert.assertFalse(sb.contains('z'));
        Assert.assertTrue(sb.contains("abc"));
        Assert.assertFalse(sb.contains("xyz"));
        Assert.assertTrue(sb.contains(StrMatcher.stringMatcher("cd")));
        Assert.assertFalse(sb.contains((StrMatcher) null));

        Assert.assertEquals(0, sb.indexOf('a'));
        Assert.assertEquals(2, sb.indexOf('a', 1));
        Assert.assertEquals(-1, sb.indexOf('a', 10));
        Assert.assertEquals(0, sb.indexOf('a', -5));
        Assert.assertEquals(-1, sb.indexOf('z'));

        Assert.assertEquals(-1, sb.indexOf((String) null));
        Assert.assertEquals(-1, sb.indexOf("a", 10));
        Assert.assertEquals(0, sb.indexOf(""));
        Assert.assertEquals(3, sb.indexOf("", 3));
        Assert.assertEquals(2, sb.indexOf("a", 1));
        Assert.assertEquals(-1, sb.indexOf("longer than builder string"));
        Assert.assertEquals(2, sb.indexOf("ab", 1));
        Assert.assertEquals(-1, sb.indexOf("abx"));

        Assert.assertEquals(-1, sb.indexOf((StrMatcher) null));
        Assert.assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 10));
        Assert.assertEquals(0, sb.indexOf(StrMatcher.charMatcher('a'), -5));
        Assert.assertEquals(2, sb.indexOf(StrMatcher.charMatcher('a'), 1));
        Assert.assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('z')));
    }

    @Test
    public void testLastIndexOf() {
        StrBuilder sb = new StrBuilder("ababcdcd");
        Assert.assertEquals(2, sb.lastIndexOf('a'));
        Assert.assertEquals(0, sb.lastIndexOf('a', 1));
        Assert.assertEquals(2, sb.lastIndexOf('a', 10));
        Assert.assertEquals(-1, sb.lastIndexOf('a', -1));
        Assert.assertEquals(-1, sb.lastIndexOf('z'));

        Assert.assertEquals(-1, sb.lastIndexOf((String) null));
        Assert.assertEquals(-1, sb.lastIndexOf("a", -1));
        Assert.assertEquals(3, sb.lastIndexOf("", 3));
        Assert.assertEquals(7, sb.lastIndexOf("", 20));
        Assert.assertEquals(-1, sb.lastIndexOf("longer than builder string"));
        Assert.assertEquals(0, sb.lastIndexOf("a", 1));
        Assert.assertEquals(2, sb.lastIndexOf("ab", 5));
        Assert.assertEquals(-1, sb.lastIndexOf("abx", 5));

        Assert.assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        Assert.assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), -1));
        Assert.assertEquals(2, sb.lastIndexOf(StrMatcher.charMatcher('a'), 10));
        Assert.assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a'), 1));
        Assert.assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('z')));
    }

    @Test
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("hello");
        StrBuilder sb3 = new StrBuilder("Hello");
        StrBuilder sb4 = new StrBuilder("Hello World");

        Assert.assertTrue(sb1.equals(sb1));
        Assert.assertFalse(sb1.equals((Object) null));
        Assert.assertFalse(sb1.equals("Hello"));
        Assert.assertFalse(sb1.equals(sb2));
        Assert.assertTrue(sb1.equals(sb3));
        Assert.assertFalse(sb1.equals(sb4));

        Assert.assertTrue(sb1.equalsIgnoreCase(sb1));
        Assert.assertFalse(sb1.equalsIgnoreCase(null));
        Assert.assertTrue(sb1.equalsIgnoreCase(sb2));
        Assert.assertTrue(sb1.equalsIgnoreCase(sb3));
        Assert.assertFalse(sb1.equalsIgnoreCase(sb4));
        Assert.assertFalse(new StrBuilder("abc").equalsIgnoreCase(new StrBuilder("abz")));

        Assert.assertEquals(sb1.hashCode(), sb3.hashCode());
        Assert.assertNotEquals(0, sb1.hashCode());
    }

    @Test
    public void testToStringAndToStringBuffer() {
        StrBuilder sb = new StrBuilder("Hello");
        Assert.assertEquals("Hello", sb.toString());
        StringBuffer sbuf = sb.toStringBuffer();
        Assert.assertEquals("Hello", sbuf.toString());
    }

    @Test
    public void testTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tok = sb.asTokenizer();
        Assert.assertEquals("a b c", tok.getContent());
        String[] tokens = tok.getTokenArray();
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, tokens);

        tok.reset("x y");
        Assert.assertEquals("x y", tok.getContent());
        Assert.assertArrayEquals(new String[]{"x", "y"}, tok.getTokenArray());
    }

    @Test
    public void testReader() throws Exception {
        StrBuilder sb = new StrBuilder("0123456789");
        Reader reader = sb.asReader();
        Assert.assertTrue(reader.ready());
        Assert.assertTrue(reader.markSupported());
        Assert.assertEquals('0', (char) reader.read());

        reader.mark(5);
        Assert.assertEquals('1', (char) reader.read());
        Assert.assertEquals('2', (char) reader.read());
        reader.reset();
        Assert.assertEquals('1', (char) reader.read());

        char[] buf = new char[4];
        Assert.assertEquals(0, reader.read(buf, 0, 0));
        int readCount = reader.read(buf, 0, 4);
        Assert.assertEquals(4, readCount);
        Assert.assertEquals("2345", new String(buf));

        long skipped = reader.skip(-1);
        Assert.assertEquals(0, skipped);
        skipped = reader.skip(2);
        Assert.assertEquals(2, skipped);
        Assert.assertEquals('8', (char) reader.read());
        Assert.assertEquals('9', (char) reader.read());
        Assert.assertEquals(-1, reader.read());
        Assert.assertFalse(reader.ready());
        Assert.assertEquals(-1, reader.read(buf, 0, 1));

        skipped = reader.skip(10);
        Assert.assertEquals(0, skipped);
        reader.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReaderReadOutOfBounds() throws Exception {
        Reader reader = new StrBuilder("abc").asReader();
        reader.read(new char[2], 0, 5);
    }

    @Test
    public void testWriter() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();
        writer.write('a');
        writer.write(new char[]{'b', 'c'});
        writer.write(new char[]{'d', 'e', 'f'}, 1, 2);
        writer.write("gh");
        writer.write("ijkl", 1, 2);
        writer.flush();
        writer.close();
        Assert.assertEquals("abcdefghjk", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateIndexNegative() {
        new StrBuilder("abc").validateIndex(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateIndexTooLarge() {
        new StrBuilder("abc").validateIndex(4);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateRangeNegativeStart() {
        new StrBuilder("abc").validateRange(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateRangeStartGreaterThanEnd() {
        new StrBuilder("abc").validateRange(2, 1);
    }
}
