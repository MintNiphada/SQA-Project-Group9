package org.apache.commons.lang.text;

import org.apache.commons.lang.ArrayUtils;
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

        StrBuilder sb2 = new StrBuilder(-5);
        Assert.assertEquals(32, sb2.capacity());
        Assert.assertEquals(0, sb2.length());

        StrBuilder sb3 = new StrBuilder(64);
        Assert.assertEquals(64, sb3.capacity());
        Assert.assertEquals(0, sb3.length());

        StrBuilder sb4 = new StrBuilder((String) null);
        Assert.assertEquals(32, sb4.capacity());
        Assert.assertEquals(0, sb4.length());

        StrBuilder sb5 = new StrBuilder("Hello");
        Assert.assertEquals(37, sb5.capacity());
        Assert.assertEquals(5, sb5.length());
        Assert.assertEquals("Hello", sb5.toString());
    }

    @Test
    public void testNewLineText() {
        StrBuilder sb = new StrBuilder();
        Assert.assertNull(sb.getNewLineText());
        sb.setNewLineText("\r\n");
        Assert.assertEquals("\r\n", sb.getNewLineText());
        sb.appendNewLine();
        Assert.assertEquals("\r\n", sb.toString());

        sb.setNewLineText(null);
        sb.clear();
        sb.appendNewLine();
        Assert.assertTrue(sb.length() > 0);
    }

    @Test
    public void testNullText() {
        StrBuilder sb = new StrBuilder();
        Assert.assertNull(sb.getNullText());
        sb.setNullText("");
        Assert.assertNull(sb.getNullText());
        sb.setNullText("NULL");
        Assert.assertEquals("NULL", sb.getNullText());
        sb.appendNull();
        Assert.assertEquals("NULL", sb.toString());

        sb.setNullText(null);
        sb.appendNull();
        Assert.assertEquals("NULL", sb.toString());
    }

    @Test
    public void testLengthAndCapacity() {
        StrBuilder sb = new StrBuilder("Hello World");
        Assert.assertEquals(11, sb.length());
        Assert.assertEquals(11, sb.size());
        Assert.assertFalse(sb.isEmpty());

        sb.setLength(5);
        Assert.assertEquals(5, sb.length());
        Assert.assertEquals("Hello", sb.toString());

        sb.setLength(8);
        Assert.assertEquals(8, sb.length());
        Assert.assertEquals("Hello\0\0\0", sb.toString());

        sb.setLength(8);
        Assert.assertEquals(8, sb.length());

        sb.minimizeCapacity();
        Assert.assertEquals(8, sb.capacity());

        sb.ensureCapacity(100);
        Assert.assertTrue(sb.capacity() >= 100);
        sb.ensureCapacity(10);
        Assert.assertTrue(sb.capacity() >= 100);

        sb.clear();
        Assert.assertEquals(0, sb.length());
        Assert.assertTrue(sb.isEmpty());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetLengthNegative() {
        new StrBuilder().setLength(-1);
    }

    @Test
    public void testCharAtAndSetCharAt() {
        StrBuilder sb = new StrBuilder("abcde");
        Assert.assertEquals('a', sb.charAt(0));
        Assert.assertEquals('e', sb.charAt(4));

        sb.setCharAt(1, 'x');
        Assert.assertEquals("axcde", sb.toString());

        sb.deleteCharAt(1);
        Assert.assertEquals("acde", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtNegative() {
        new StrBuilder("abc").charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtOverflow() {
        new StrBuilder("abc").charAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtNegative() {
        new StrBuilder("abc").setCharAt(-1, 'z');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtOverflow() {
        new StrBuilder("abc").setCharAt(3, 'z');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtNegative() {
        new StrBuilder("abc").deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtOverflow() {
        new StrBuilder("abc").deleteCharAt(3);
    }

    @Test
    public void testToCharArray() {
        StrBuilder sbEmpty = new StrBuilder();
        Assert.assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sbEmpty.toCharArray());
        Assert.assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sbEmpty.toCharArray(0, 0));

        StrBuilder sb = new StrBuilder("hello");
        Assert.assertArrayEquals(new char[]{'h', 'e', 'l', 'l', 'o'}, sb.toCharArray());
        Assert.assertArrayEquals(new char[]{'e', 'l', 'l'}, sb.toCharArray(1, 4));
        Assert.assertArrayEquals(new char[]{'l', 'o'}, sb.toCharArray(3, 10));
    }

    @Test
    public void testGetChars() {
        StrBuilder sb = new StrBuilder("abcdef");
        char[] result1 = sb.getChars(null);
        Assert.assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, result1);

        char[] dest = new char[10];
        char[] result2 = sb.getChars(dest);
        Assert.assertSame(dest, result2);
        Assert.assertEquals('a', dest[0]);
        Assert.assertEquals('f', dest[5]);

        char[] sub = new char[4];
        sb.getChars(1, 4, sub, 1);
        Assert.assertEquals('\0', sub[0]);
        Assert.assertEquals('b', sub[1]);
        Assert.assertEquals('c', sub[2]);
        Assert.assertEquals('d', sub[3]);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsInvalidStart() {
        new StrBuilder("abc").getChars(-1, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsInvalidEndNegative() {
        new StrBuilder("abc").getChars(0, -1, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsInvalidEndOverflow() {
        new StrBuilder("abc").getChars(0, 5, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsStartGreaterThanEnd() {
        new StrBuilder("abc").getChars(2, 1, new char[5], 0);
    }

    @Test
    public void testAppendObjectsAndStrings() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) null);
        Assert.assertEquals("", sb.toString());

        sb.setNullText("<null>");
        sb.append((Object) null);
        Assert.assertEquals("<null>", sb.toString());

        sb.clear();
        sb.append(new StringBuilder("Hello"));
        Assert.assertEquals("Hello", sb.toString());

        sb.append((String) null);
        Assert.assertEquals("Hello<null>", sb.toString());

        sb.append(" World");
        Assert.assertEquals("Hello<null> World", sb.toString());

        sb.clear();
        sb.append((String) null, 0, 0);
        Assert.assertEquals("<null>", sb.toString());

        sb.clear();
        sb.append("0123456789", 2, 5);
        Assert.assertEquals("23456", sb.toString());

        sb.append("0123456789", 0, 0);
        Assert.assertEquals("23456", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringInvalidStartNegative() {
        new StrBuilder().append("abc", -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringInvalidStartOverflow() {
        new StrBuilder().append("abc", 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringInvalidLengthNegative() {
        new StrBuilder().append("abc", 1, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringInvalidLengthOverflow() {
        new StrBuilder().append("abc", 2, 3);
    }

    @Test
    public void testAppendStringBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.append((StringBuffer) null);
        Assert.assertEquals("", sb.toString());

        StringBuffer buf = new StringBuffer("testBuf");
        sb.append(buf);
        Assert.assertEquals("testBuf", sb.toString());

        sb.clear();
        sb.append((StringBuffer) null, 0, 0);
        Assert.assertEquals("", sb.toString());

        sb.append(buf, 1, 4);
        Assert.assertEquals("estB", sb.toString());

        sb.append(buf, 0, 0);
        Assert.assertEquals("estB", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferInvalidStartNegative() {
        new StrBuilder().append(new StringBuffer("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferInvalidStartOverflow() {
        new StrBuilder().append(new StringBuffer("abc"), 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferInvalidLengthNegative() {
        new StrBuilder().append(new StringBuffer("abc"), 1, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferInvalidLengthOverflow() {
        new StrBuilder().append(new StringBuffer("abc"), 2, 3);
    }

    @Test
    public void testAppendStrBuilder() {
        StrBuilder sb = new StrBuilder();
        sb.append((StrBuilder) null);
        Assert.assertEquals("", sb.toString());

        StrBuilder other = new StrBuilder("other");
        sb.append(other);
        Assert.assertEquals("other", sb.toString());

        sb.clear();
        sb.append((StrBuilder) null, 0, 0);
        Assert.assertEquals("", sb.toString());

        sb.append(other, 1, 3);
        Assert.assertEquals("the", sb.toString());

        sb.append(other, 0, 0);
        Assert.assertEquals("the", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderInvalidStartNegative() {
        new StrBuilder().append(new StrBuilder("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderInvalidStartOverflow() {
        new StrBuilder().append(new StrBuilder("abc"), 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderInvalidLengthNegative() {
        new StrBuilder().append(new StrBuilder("abc"), 1, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderInvalidLengthOverflow() {
        new StrBuilder().append(new StrBuilder("abc"), 2, 3);
    }

    @Test
    public void testAppendCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append((char[]) null);
        Assert.assertEquals("", sb.toString());

        sb.append(new char[]{'a', 'b', 'c'});
        Assert.assertEquals("abc", sb.toString());

        sb.clear();
        sb.append((char[]) null, 0, 0);
        Assert.assertEquals("", sb.toString());

        sb.append(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        Assert.assertEquals("bc", sb.toString());

        sb.append(new char[]{'x'}, 0, 0);
        Assert.assertEquals("bc", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayInvalidStartNegative() {
        new StrBuilder().append(new char[]{'a', 'b'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayInvalidStartOverflow() {
        new StrBuilder().append(new char[]{'a', 'b'}, 3, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayInvalidLengthNegative() {
        new StrBuilder().append(new char[]{'a', 'b'}, 1, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayInvalidLengthOverflow() {
        new StrBuilder().append(new char[]{'a', 'b'}, 1, 2);
    }

    @Test
    public void testAppendPrimitives() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false).append('!').append(123).append(456789L).append(1.5f).append(2.5d);
        Assert.assertEquals("truefalse!1234567891.52.5", sb.toString());
    }

    @Test
    public void testAppendWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        sb.appendWithSeparators(new Object[0], ",");
        Assert.assertEquals("", sb.toString());

        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        Assert.assertEquals("a,b,c", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"a", "b"}, null);
        Assert.assertEquals("ab", sb.toString());

        sb.clear();
        sb.appendWithSeparators((List<?>) null, ",");
        sb.appendWithSeparators(Collections.emptyList(), ",");
        Assert.assertEquals("", sb.toString());

        sb.appendWithSeparators(Arrays.asList("1", "2", "3"), "-");
        Assert.assertEquals("1-2-3", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("1", "2"), null);
        Assert.assertEquals("12", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator<?>) null, ",");
        sb.appendWithSeparators(Collections.emptyIterator(), ",");
        Assert.assertEquals("", sb.toString());

        sb.appendWithSeparators(Arrays.asList("x", "y", "z").iterator(), ":");
        Assert.assertEquals("x:y:z", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("x", "y").iterator(), null);
        Assert.assertEquals("xy", sb.toString());
    }

    @Test
    public void testAppendPadding() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-1, 'a');
        sb.appendPadding(0, 'a');
        Assert.assertEquals("", sb.toString());

        sb.appendPadding(3, 'z');
        Assert.assertEquals("zzz", sb.toString());
    }

    @Test
    public void testAppendFixedWidthPadLeftAndRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 5, '0');
        Assert.assertEquals("00abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 3, '0');
        Assert.assertEquals("def", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(null, 4, '-');
        Assert.assertEquals("----", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(123, 5, '0');
        Assert.assertEquals("00123", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 0, 'x');
        sb.appendFixedWidthPadLeft("abc", -1, 'x');
        Assert.assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 5, '0');
        Assert.assertEquals("abc00", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 3, '0');
        Assert.assertEquals("abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(null, 4, '-');
        Assert.assertEquals("----", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(123, 5, '0');
        Assert.assertEquals("12300", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 0, 'x');
        sb.appendFixedWidthPadRight("abc", -1, 'x');
        Assert.assertEquals("", sb.toString());
    }

    @Test
    public void testInsert() {
        StrBuilder sb = new StrBuilder("0123");
        sb.insert(1, (Object) null);
        Assert.assertEquals("0123", sb.toString());

        sb.insert(1, "x");
        Assert.assertEquals("0x123", sb.toString());

        sb.insert(0, (String) null);
        Assert.assertEquals("0x123", sb.toString());

        sb.insert(2, (char[]) null);
        Assert.assertEquals("0x123", sb.toString());

        sb.insert(2, new char[]{'y', 'z'});
        Assert.assertEquals("0xyz123", sb.toString());

        sb.insert(0, (char[]) null, 0, 0);
        Assert.assertEquals("0xyz123", sb.toString());

        sb.insert(0, new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        Assert.assertEquals("bc0xyz123", sb.toString());

        sb.insert(0, new char[]{'a'}, 0, 0);
        Assert.assertEquals("bc0xyz123", sb.toString());

        sb.insert(0, true);
        Assert.assertEquals("truebc0xyz123", sb.toString());

        sb.insert(0, false);
        Assert.assertEquals("falsetruebc0xyz123", sb.toString());

        sb.insert(0, '!');
        Assert.assertEquals("!falsetruebc0xyz123", sb.toString());

        sb.clear();
        sb.insert(0, 10).insert(2, 20L).insert(4, 3.5f).insert(8, 4.5d);
        Assert.assertEquals("10203.54.5", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertInvalidIndexNegative() {
        new StrBuilder("abc").insert(-1, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertInvalidIndexOverflow() {
        new StrBuilder("abc").insert(4, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArrayInvalidOffsetNegative() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArrayInvalidOffsetOverflow() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, 2, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArrayInvalidLengthNegative() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, 0, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArrayInvalidLengthOverflow() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, 0, 2);
    }

    @Test
    public void testDelete() {
        StrBuilder sb = new StrBuilder("0123456789");
        sb.delete(2, 5);
        Assert.assertEquals("0156789", sb.toString());

        sb.delete(0, 0);
        Assert.assertEquals("0156789", sb.toString());

        sb.delete(5, 100);
        Assert.assertEquals("01567", sb.toString());

        sb = new StrBuilder("aabaa");
        sb.deleteAll('a');
        Assert.assertEquals("b", sb.toString());

        sb = new StrBuilder("aabaa");
        sb.deleteFirst('a');
        Assert.assertEquals("abaa", sb.toString());

        sb = new StrBuilder("a123a123a");
        sb.deleteAll("123");
        Assert.assertEquals("aaa", sb.toString());

        sb.deleteAll((String) null);
        sb.deleteAll("");
        Assert.assertEquals("aaa", sb.toString());

        sb = new StrBuilder("a123a123a");
        sb.deleteFirst("123");
        Assert.assertEquals("aa123a", sb.toString());

        sb.deleteFirst((String) null);
        sb.deleteFirst("");
        Assert.assertEquals("aa123a", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.deleteAll(StrMatcher.charSetMatcher("123"));
        Assert.assertEquals("abc", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.deleteFirst(StrMatcher.charSetMatcher("123"));
        Assert.assertEquals("ab2c3", sb.toString());
    }

    @Test
    public void testReplace() {
        StrBuilder sb = new StrBuilder("012345");
        sb.replace(2, 4, "XYZ");
        Assert.assertEquals("01XYZ45", sb.toString());

        sb.replace(0, 2, null);
        Assert.assertEquals("XYZ45", sb.toString());

        sb = new StrBuilder("banana");
        sb.replaceAll('a', 'o');
        Assert.assertEquals("bonono", sb.toString());

        sb.replaceAll('z', 'x');
        Assert.assertEquals("bonono", sb.toString());

        sb.replaceAll('b', 'b');
        Assert.assertEquals("bonono", sb.toString());

        sb = new StrBuilder("banana");
        sb.replaceFirst('a', 'o');
        Assert.assertEquals("bonana", sb.toString());

        sb.replaceFirst('z', 'x');
        Assert.assertEquals("bonana", sb.toString());

        sb.replaceFirst('b', 'b');
        Assert.assertEquals("bonana", sb.toString());

        sb = new StrBuilder("aba ba ba");
        sb.replaceAll("ba", "xy");
        Assert.assertEquals("axy xy xy", sb.toString());

        sb.replaceAll((String) null, "z");
        sb.replaceAll("", "z");
        Assert.assertEquals("axy xy xy", sb.toString());

        sb = new StrBuilder("aba ba ba");
        sb.replaceFirst("ba", "xy");
        Assert.assertEquals("axy ba ba", sb.toString());

        sb.replaceFirst((String) null, "z");
        sb.replaceFirst("", "z");
        Assert.assertEquals("axy ba ba", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.replaceAll(StrMatcher.charSetMatcher("123"), "X");
        Assert.assertEquals("aXbXcX", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.replaceFirst(StrMatcher.charSetMatcher("123"), "X");
        Assert.assertEquals("aXb2c3", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.replace((StrMatcher) null, "X", 0, 6, 2);
        Assert.assertEquals("a1b2c3", sb.toString());

        sb = new StrBuilder();
        sb.replace(StrMatcher.charSetMatcher("123"), "X", 0, 0, 2);
        Assert.assertEquals("", sb.toString());

        sb = new StrBuilder("a1b2c3d4");
        sb.replace(StrMatcher.charSetMatcher("1234"), "XX", 0, 8, 2);
        Assert.assertEquals("aXXbXXc3d4", sb.toString());

        sb = new StrBuilder("a1b2c3d4");
        sb.replace(StrMatcher.charSetMatcher("1234"), null, 0, 8, 2);
        Assert.assertEquals("abc3d4", sb.toString());
    }

    @Test
    public void testReverseAndTrim() {
        StrBuilder sb = new StrBuilder();
        sb.reverse();
        Assert.assertEquals("", sb.toString());

        sb.append("12345");
        sb.reverse();
        Assert.assertEquals("54321", sb.toString());

        sb.setLength(4);
        sb.reverse();
        Assert.assertEquals("2345", sb.toString());

        sb.clear();
        sb.trim();
        Assert.assertEquals("", sb.toString());

        sb.append("   hello world \t \n");
        sb.trim();
        Assert.assertEquals("hello world", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.trim();
        Assert.assertEquals("abc", sb.toString());
    }

    @Test
    public void testStartsAndEndsWith() {
        StrBuilder sb = new StrBuilder("hello world");
        Assert.assertFalse(sb.startsWith(null));
        Assert.assertTrue(sb.startsWith(""));
        Assert.assertTrue(sb.startsWith("hello"));
        Assert.assertFalse(sb.startsWith("world"));
        Assert.assertFalse(sb.startsWith("hello world foo"));

        Assert.assertFalse(sb.endsWith(null));
        Assert.assertTrue(sb.endsWith(""));
        Assert.assertTrue(sb.endsWith("world"));
        Assert.assertFalse(sb.endsWith("hello"));
        Assert.assertFalse(sb.endsWith("foo hello world"));
    }

    @Test
    public void testSubstrings() {
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

        Assert.assertEquals("", sb.midString(0, -1));
        Assert.assertEquals("", sb.midString(20, 5));
        Assert.assertEquals("hel", sb.midString(-5, 3));
        Assert.assertEquals("world", sb.midString(6, 20));
        Assert.assertEquals("lo", sb.midString(3, 2));
    }

    @Test
    public void testContains() {
        StrBuilder sb = new StrBuilder("hello world");
        Assert.assertTrue(sb.contains('e'));
        Assert.assertFalse(sb.contains('z'));

        Assert.assertTrue(sb.contains("world"));
        Assert.assertFalse(sb.contains("foo"));
        Assert.assertFalse(sb.contains((String) null));

        Assert.assertTrue(sb.contains(StrMatcher.stringMatcher("world")));
        Assert.assertFalse(sb.contains(StrMatcher.stringMatcher("foo")));
        Assert.assertFalse(sb.contains((StrMatcher) null));
    }

    @Test
    public void testIndexOfAndLastIndexOfChar() {
        StrBuilder sb = new StrBuilder("banana");
        Assert.assertEquals(1, sb.indexOf('a'));
        Assert.assertEquals(3, sb.indexOf('a', 2));
        Assert.assertEquals(-1, sb.indexOf('a', 10));
        Assert.assertEquals(1, sb.indexOf('a', -5));
        Assert.assertEquals(-1, sb.indexOf('z'));

        Assert.assertEquals(5, sb.lastIndexOf('a'));
        Assert.assertEquals(3, sb.lastIndexOf('a', 4));
        Assert.assertEquals(5, sb.lastIndexOf('a', 10));
        Assert.assertEquals(-1, sb.lastIndexOf('a', -1));
        Assert.assertEquals(-1, sb.lastIndexOf('z'));
    }

    @Test
    public void testIndexOfAndLastIndexOfString() {
        StrBuilder sb = new StrBuilder("banana");
        Assert.assertEquals(1, sb.indexOf("an"));
        Assert.assertEquals(3, sb.indexOf("an", 2));
        Assert.assertEquals(-1, sb.indexOf("an", 10));
        Assert.assertEquals(1, sb.indexOf("an", -5));
        Assert.assertEquals(0, sb.indexOf("b"));
        Assert.assertEquals(2, sb.indexOf("", 2));
        Assert.assertEquals(-1, sb.indexOf((String) null));
        Assert.assertEquals(-1, sb.indexOf("longerstringthanbuilder"));
        Assert.assertEquals(-1, sb.indexOf("xyz"));

        Assert.assertEquals(3, sb.lastIndexOf("an"));
        Assert.assertEquals(1, sb.lastIndexOf("an", 2));
        Assert.assertEquals(3, sb.lastIndexOf("an", 10));
        Assert.assertEquals(-1, sb.lastIndexOf("an", -1));
        Assert.assertEquals(0, sb.lastIndexOf("b"));
        Assert.assertEquals(2, sb.lastIndexOf("", 2));
        Assert.assertEquals(-1, sb.lastIndexOf((String) null));
        Assert.assertEquals(-1, sb.lastIndexOf("longerstringthanbuilder"));
        Assert.assertEquals(-1, sb.lastIndexOf("xyz"));
    }

    @Test
    public void testIndexOfAndLastIndexOfMatcher() {
        StrBuilder sb = new StrBuilder("banana");
        StrMatcher matcher = StrMatcher.charSetMatcher("an");
        Assert.assertEquals(1, sb.indexOf(matcher));
        Assert.assertEquals(2, sb.indexOf(matcher, 2));
        Assert.assertEquals(-1, sb.indexOf(matcher, 10));
        Assert.assertEquals(1, sb.indexOf(matcher, -5));
        Assert.assertEquals(-1, sb.indexOf((StrMatcher) null));
        Assert.assertEquals(-1, sb.indexOf(StrMatcher.charSetMatcher("z")));

        Assert.assertEquals(5, sb.lastIndexOf(matcher));
        Assert.assertEquals(3, sb.lastIndexOf(matcher, 3));
        Assert.assertEquals(5, sb.lastIndexOf(matcher, 10));
        Assert.assertEquals(-1, sb.lastIndexOf(matcher, -1));
        Assert.assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        Assert.assertEquals(-1, sb.lastIndexOf(StrMatcher.charSetMatcher("z")));
    }

    @Test
    public void testTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tok = sb.asTokenizer();
        Assert.assertEquals("a b c", tok.getContent());
        Assert.assertEquals(3, tok.getTokenList().size());
        Assert.assertEquals("a", tok.next());
        Assert.assertEquals("b", tok.next());
        Assert.assertEquals("c", tok.next());

        tok.reset(new char[]{'x', 'y'});
        Assert.assertEquals("x", tok.next());
    }

    @Test
    public void testReader() throws Exception {
        StrBuilder sb = new StrBuilder("abc");
        Reader reader = sb.asReader();
        Assert.assertTrue(reader.ready());
        Assert.assertTrue(reader.markSupported());

        Assert.assertEquals('a', reader.read());
        reader.mark(10);
        Assert.assertEquals('b', reader.read());
        reader.reset();
        Assert.assertEquals('b', reader.read());
        Assert.assertEquals('c', reader.read());
        Assert.assertEquals(-1, reader.read());
        Assert.assertFalse(reader.ready());

        reader.reset();
        char[] buf = new char[5];
        Assert.assertEquals(0, reader.read(buf, 0, 0));
        Assert.assertEquals(2, reader.read(buf, 1, 3));
        Assert.assertEquals('b', buf[1]);
        Assert.assertEquals('c', buf[2]);
        Assert.assertEquals(-1, reader.read(buf, 0, 1));

        reader.reset();
        Assert.assertEquals(0, reader.skip(-5));
        Assert.assertEquals(2, reader.skip(10));
        Assert.assertEquals(-1, reader.read());
        reader.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReaderReadInvalidOffLen() throws Exception {
        Reader reader = new StrBuilder("abc").asReader();
        reader.read(new char[5], -1, 2);
    }

    @Test
    public void testWriter() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();
        writer.write('a');
        writer.write(new char[]{'b', 'c'});
        writer.write(new char[]{'d', 'e', 'f'}, 1, 1);
        writer.write("gh");
        writer.write("ijkl", 1, 2);
        writer.flush();
        writer.close();
        Assert.assertEquals("abceghjk", sb.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("ABC");
        StrBuilder sb4 = new StrBuilder("abcd");

        Assert.assertTrue(sb1.equals(sb1));
        Assert.assertTrue(sb1.equals(sb2));
        Assert.assertFalse(sb1.equals(sb3));
        Assert.assertFalse(sb1.equals(sb4));
        Assert.assertFalse(sb1.equals((StrBuilder) null));
        Assert.assertFalse(sb1.equals("abc"));

        Assert.assertTrue(sb1.equalsIgnoreCase(sb1));
        Assert.assertTrue(sb1.equalsIgnoreCase(sb2));
        Assert.assertTrue(sb1.equalsIgnoreCase(sb3));
        Assert.assertFalse(sb1.equalsIgnoreCase(sb4));
        Assert.assertFalse(sb1.equalsIgnoreCase(null));
        Assert.assertFalse(new StrBuilder("abc").equalsIgnoreCase(new StrBuilder("abx")));

        Assert.assertEquals(sb1.hashCode(), sb2.hashCode());

        StringBuffer sbuf = sb1.toStringBuffer();
        Assert.assertEquals("abc", sbuf.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateRangeStartNegative() {
        new StrBuilder("abc").substring(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateRangeStartGreaterThanEnd() {
        new StrBuilder("abc").substring(2, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateIndexNegative() {
        new StrBuilder("abc").insert(-1, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateIndexOverflow() {
        new StrBuilder("abc").insert(4, "x");
    }
}
