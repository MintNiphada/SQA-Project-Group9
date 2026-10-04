package org.apache.commons.lang.text;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

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

        StrBuilder sb6 = new StrBuilder("hello");
        Assert.assertEquals(37, sb6.capacity());
        Assert.assertEquals("hello", sb6.toString());
    }

    @Test
    public void testNewLineTextAndNullText() {
        StrBuilder sb = new StrBuilder();
        Assert.assertNull(sb.getNewLineText());
        sb.setNewLineText("\r\n");
        Assert.assertEquals("\r\n", sb.getNewLineText());

        Assert.assertNull(sb.getNullText());
        sb.setNullText("NULL");
        Assert.assertEquals("NULL", sb.getNullText());
        sb.setNullText("");
        Assert.assertNull(sb.getNullText());
        sb.setNullText(null);
        Assert.assertNull(sb.getNullText());
    }

    @Test
    public void testLengthAndCapacity() {
        StrBuilder sb = new StrBuilder("hello");
        Assert.assertEquals(5, sb.length());
        Assert.assertEquals(5, sb.size());
        Assert.assertFalse(sb.isEmpty());

        sb.setLength(3);
        Assert.assertEquals("hel", sb.toString());
        Assert.assertEquals(3, sb.length());

        sb.setLength(5);
        Assert.assertEquals("hel\0\0", sb.toString());
        Assert.assertEquals(5, sb.length());

        sb.setLength(5);
        Assert.assertEquals(5, sb.length());

        sb.ensureCapacity(100);
        Assert.assertTrue(sb.capacity() >= 100);
        sb.ensureCapacity(10);
        Assert.assertTrue(sb.capacity() >= 100);

        sb.minimizeCapacity();
        Assert.assertEquals(5, sb.capacity());

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
        StrBuilder sb = new StrBuilder("abc");
        Assert.assertEquals('a', sb.charAt(0));
        Assert.assertEquals('b', sb.charAt(1));
        Assert.assertEquals('c', sb.charAt(2));

        sb.setCharAt(1, 'z');
        Assert.assertEquals("azc", sb.toString());

        sb.deleteCharAt(1);
        Assert.assertEquals("ac", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtNegative() {
        new StrBuilder("abc").charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtOutOfRange() {
        new StrBuilder("abc").charAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtNegative() {
        new StrBuilder("abc").setCharAt(-1, 'x');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtOutOfRange() {
        new StrBuilder("abc").setCharAt(3, 'x');
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtNegative() {
        new StrBuilder("abc").deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtOutOfRange() {
        new StrBuilder("abc").deleteCharAt(3);
    }

    @Test
    public void testToCharArrayAndGetChars() {
        StrBuilder empty = new StrBuilder();
        Assert.assertEquals(0, empty.toCharArray().length);
        Assert.assertEquals(0, empty.toCharArray(0, 0).length);

        StrBuilder sb = new StrBuilder("abcdef");
        char[] arr = sb.toCharArray();
        Assert.assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, arr);

        char[] subArr = sb.toCharArray(1, 4);
        Assert.assertArrayEquals(new char[]{'b', 'c', 'd'}, subArr);

        char[] dest = new char[6];
        char[] ret = sb.getChars(dest);
        Assert.assertSame(dest, ret);
        Assert.assertArrayEquals(arr, dest);

        char[] tooSmall = new char[2];
        char[] retNew = sb.getChars(tooSmall);
        Assert.assertNotSame(tooSmall, retNew);
        Assert.assertArrayEquals(arr, retNew);

        char[] retNull = sb.getChars(null);
        Assert.assertArrayEquals(arr, retNull);

        char[] buf = new char[5];
        sb.getChars(1, 4, buf, 1);
        Assert.assertArrayEquals(new char[]{'\0', 'b', 'c', 'd', '\0'}, buf);
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
    public void testGetCharsInvalidEndTooLarge() {
        new StrBuilder("abc").getChars(0, 5, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsInvalidStartGreaterThanEnd() {
        new StrBuilder("abc").getChars(2, 1, new char[5], 0);
    }

    @Test
    public void testAppendPrimitivesAndObjects() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) "foo");
        sb.append((Object) null);
        sb.append((String) null);
        sb.append("");
        sb.append("bar");
        sb.append((StringBuffer) null);
        sb.append(new StringBuffer("baz"));
        sb.append((StrBuilder) null);
        sb.append(new StrBuilder("qux"));
        sb.append((char[]) null);
        sb.append(new char[]{});
        sb.append(new char[]{'1', '2'});
        sb.append(true);
        sb.append(false);
        sb.append('!');
        sb.append(10);
        sb.append(20L);
        sb.append(1.5f);
        sb.append(2.5d);

        String expected = "foobarbazqux12truefalse!10201.52.5";
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void testAppendWithNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");
        sb.append((Object) null);
        sb.append((String) null);
        sb.append((StringBuffer) null);
        sb.append((StrBuilder) null);
        sb.append((char[]) null);
        Assert.assertEquals("<null><null><null><null><null>", sb.toString());
    }

    @Test
    public void testAppendSubRanges() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");
        sb.append((String) null, 0, 0);
        sb.append("012345", 1, 3);
        sb.append("012345", 0, 0);

        sb.append((StringBuffer) null, 0, 0);
        sb.append(new StringBuffer("abcdef"), 2, 2);
        sb.append(new StringBuffer("abcdef"), 0, 0);

        sb.append((StrBuilder) null, 0, 0);
        sb.append(new StrBuilder("uvwxyz"), 3, 2);
        sb.append(new StrBuilder("uvwxyz"), 0, 0);

        sb.append((char[]) null, 0, 0);
        sb.append(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        sb.append(new char[]{'a', 'b', 'c', 'd'}, 0, 0);

        Assert.assertEquals("<null>123<null>cd<null>xy<null>bc", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubInvalidStart() {
        new StrBuilder().append("abc", -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubInvalidStartTooLarge() {
        new StrBuilder().append("abc", 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubInvalidLength() {
        new StrBuilder().append("abc", 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferSubInvalidStart() {
        new StrBuilder().append(new StringBuffer("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferSubInvalidStartTooLarge() {
        new StrBuilder().append(new StringBuffer("abc"), 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferSubInvalidLength() {
        new StrBuilder().append(new StringBuffer("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderSubInvalidStart() {
        new StrBuilder().append(new StrBuilder("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderSubInvalidStartTooLarge() {
        new StrBuilder().append(new StrBuilder("abc"), 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderSubInvalidLength() {
        new StrBuilder().append(new StrBuilder("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubInvalidStart() {
        new StrBuilder().append(new char[]{'a'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubInvalidStartTooLarge() {
        new StrBuilder().append(new char[]{'a'}, 2, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubInvalidLength() {
        new StrBuilder().append(new char[]{'a'}, 0, 2);
    }

    @Test
    public void testAppendlnMethods() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\n");
        sb.appendln((Object) "A");
        sb.appendln("B");
        sb.appendln("CDE", 1, 1);
        sb.appendln(new StringBuffer("E"));
        sb.appendln(new StringBuffer("FGH"), 1, 1);
        sb.appendln(new StrBuilder("H"));
        sb.appendln(new StrBuilder("IJK"), 1, 1);
        sb.appendln(new char[]{'K'});
        sb.appendln(new char[]{'L', 'M', 'N'}, 1, 1);
        sb.appendln(true);
        sb.appendln('O');
        sb.appendln(1);
        sb.appendln(2L);
        sb.appendln(3.0f);
        sb.appendln(4.0d);

        String expected = "A\nB\nD\nE\nG\nH\nJ\nK\nM\ntrue\nO\n1\n2\n3.0\n4.0\n";
        Assert.assertEquals(expected, sb.toString());

        StrBuilder sbSys = new StrBuilder();
        sbSys.appendNewLine();
        Assert.assertEquals(System.getProperty("line.separator"), sbSys.toString());
    }

    @Test
    public void testAppendAllAndWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll((Object[]) null);
        sb.appendAll(new Object[0]);
        sb.appendAll(new Object[]{"1", "2"});
        sb.appendAll((java.util.Collection) null);
        sb.appendAll(Collections.emptyList());
        sb.appendAll(Arrays.asList("3", "4"));
        sb.appendAll((java.util.Iterator) null);
        sb.appendAll(Arrays.asList("5", "6").iterator());
        Assert.assertEquals("123456", sb.toString());

        StrBuilder sep = new StrBuilder();
        sep.appendWithSeparators((Object[]) null, ",");
        sep.appendWithSeparators(new Object[0], ",");
        sep.appendWithSeparators(new Object[]{"A", "B"}, null);
        Assert.assertEquals("AB", sep.toString());

        sep.clear();
        sep.appendWithSeparators(new Object[]{"A", "B"}, ",");
        Assert.assertEquals("A,B", sep.toString());

        sep.clear();
        sep.appendWithSeparators((java.util.Collection) null, ",");
        sep.appendWithSeparators(Collections.emptyList(), ",");
        sep.appendWithSeparators(Arrays.asList("A", "B"), ",");
        Assert.assertEquals("A,B", sep.toString());

        sep.clear();
        sep.appendWithSeparators(Arrays.asList("A", "B"), null);
        Assert.assertEquals("AB", sep.toString());

        sep.clear();
        sep.appendWithSeparators((java.util.Iterator) null, ",");
        sep.appendWithSeparators(Arrays.asList("A", "B").iterator(), ",");
        Assert.assertEquals("A,B", sep.toString());

        sep.clear();
        sep.appendWithSeparators(Arrays.asList("A", "B").iterator(), null);
        Assert.assertEquals("AB", sep.toString());
    }

    @Test
    public void testAppendSeparator() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        sb.appendSeparator(',');
        Assert.assertEquals(0, sb.length());

        sb.append("A");
        sb.appendSeparator((String) null);
        sb.appendSeparator(",");
        sb.append("B");
        sb.appendSeparator(';');
        sb.append("C");
        Assert.assertEquals("A,B;C", sb.toString());

        sb.clear();
        sb.appendSeparator(",", 0);
        sb.appendSeparator(',', 0);
        sb.appendSeparator((String) null, 1);
        sb.appendSeparator(",", 1);
        sb.appendSeparator(';', 1);
        Assert.assertEquals(",;", sb.toString());
    }

    @Test
    public void testAppendPaddingAndFixedWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-1, 'x');
        sb.appendPadding(0, 'x');
        sb.appendPadding(3, '-');
        Assert.assertEquals("---", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadLeft((Object) null, 6, '*');
        Assert.assertEquals("**null", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 4, '*');
        Assert.assertEquals("cdef", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 5, '*');
        Assert.assertEquals("**abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(12, 4, '0');
        Assert.assertEquals("0012", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 0, '*');
        sb.appendFixedWidthPadLeft("abc", -1, '*');
        Assert.assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight((Object) null, 6, '*');
        Assert.assertEquals("null**", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 4, '*');
        Assert.assertEquals("abcd", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 5, '*');
        Assert.assertEquals("abc**", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(12, 4, '0');
        Assert.assertEquals("1200", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 0, '*');
        sb.appendFixedWidthPadRight("abc", -1, '*');
        Assert.assertEquals("", sb.toString());
    }

    @Test
    public void testInsertMethods() {
        StrBuilder sb = new StrBuilder("01234");
        sb.insert(1, (Object) "X");
        Assert.assertEquals("0X1234", sb.toString());

        sb.setNullText("<null>");
        sb.insert(2, (Object) null);
        Assert.assertEquals("0X<null>1234", sb.toString());

        sb.clear();
        sb.append("01234");
        sb.insert(1, (String) null);
        Assert.assertEquals("0<null>1234", sb.toString());

        sb.insert(0, "");
        Assert.assertEquals("0<null>1234", sb.toString());

        sb.clear();
        sb.append("01234");
        sb.insert(1, new char[]{'a', 'b'});
        Assert.assertEquals("0ab1234", sb.toString());

        sb.insert(2, (char[]) null);
        Assert.assertEquals("0a<null>b1234", sb.toString());

        sb.insert(0, new char[0]);
        Assert.assertEquals("0a<null>b1234", sb.toString());

        sb.clear();
        sb.append("01234");
        sb.insert(1, new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        Assert.assertEquals("0bc1234", sb.toString());

        sb.insert(2, (char[]) null, 0, 0);
        Assert.assertEquals("0b<null>c1234", sb.toString());

        sb.insert(0, new char[]{'a'}, 0, 0);
        Assert.assertEquals("0b<null>c1234", sb.toString());

        sb.clear();
        sb.append("01234");
        sb.insert(1, true);
        sb.insert(sb.length(), false);
        sb.insert(0, 'X');
        sb.insert(1, 10);
        sb.insert(2, 20L);
        sb.insert(3, 1.5f);
        sb.insert(4, 2.5d);
        Assert.assertEquals("X1201.52.50true01234false", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertInvalidIndexNegative() {
        new StrBuilder("abc").insert(-1, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertInvalidIndexTooLarge() {
        new StrBuilder("abc").insert(4, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubOffsetNegative() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubOffsetTooLarge() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, 2, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubLengthNegative() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, 0, -1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubLengthTooLarge() {
        new StrBuilder("abc").insert(1, new char[]{'a'}, 0, 2);
    }

    @Test
    public void testDeleteMethods() {
        StrBuilder sb = new StrBuilder("0123456789");
        sb.delete(2, 5);
        Assert.assertEquals("0156789", sb.toString());

        sb.delete(2, 2);
        Assert.assertEquals("0156789", sb.toString());

        sb.delete(5, 20);
        Assert.assertEquals("01567", sb.toString());

        sb.clear();
        sb.append("aabaacaad");
        sb.deleteAll('a');
        Assert.assertEquals("bcd", sb.toString());

        sb.clear();
        sb.append("aabaacaad");
        sb.deleteFirst('a');
        Assert.assertEquals("abaacaad", sb.toString());

        sb.deleteFirst('z');
        Assert.assertEquals("abaacaad", sb.toString());

        sb.clear();
        sb.append("aabaacaad");
        sb.deleteAll((String) null);
        sb.deleteAll("");
        sb.deleteAll("aa");
        Assert.assertEquals("bacad", sb.toString());

        sb.clear();
        sb.append("aabaacaad");
        sb.deleteFirst((String) null);
        sb.deleteFirst("");
        sb.deleteFirst("aa");
        Assert.assertEquals("baacaad", sb.toString());

        sb.deleteFirst("zzz");
        Assert.assertEquals("baacaad", sb.toString());

        sb.clear();
        sb.append("aabaacaad");
        sb.deleteAll(StrMatcher.charSetMatcher("a"));
        Assert.assertEquals("bcd", sb.toString());

        sb.clear();
        sb.append("aabaacaad");
        sb.deleteFirst(StrMatcher.stringMatcher("aa"));
        Assert.assertEquals("baacaad", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteInvalidStart() {
        new StrBuilder("abc").delete(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testDeleteEndLessThanStart() {
        new StrBuilder("abc").delete(2, 1);
    }

    @Test
    public void testReplaceMethods() {
        StrBuilder sb = new StrBuilder("012345");
        sb.replace(1, 4, "XYZ");
        Assert.assertEquals("0XYZ45", sb.toString());

        sb.replace(1, 4, null);
        Assert.assertEquals("045", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replaceAll('a', 'o');
        Assert.assertEquals("bonono", sb.toString());
        sb.replaceAll('z', 'z');
        Assert.assertEquals("bonono", sb.toString());

        sb.replaceFirst('o', 'a');
        Assert.assertEquals("banono", sb.toString());
        sb.replaceFirst('z', 'z');
        Assert.assertEquals("banono", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replaceAll((String) null, "x");
        sb.replaceAll("", "x");
        sb.replaceAll("an", "XX");
        Assert.assertEquals("bXXXXa", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replaceAll("a", null);
        Assert.assertEquals("bnn", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replaceFirst((String) null, "x");
        sb.replaceFirst("", "x");
        sb.replaceFirst("an", "XX");
        Assert.assertEquals("bXXana", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replaceFirst("an", null);
        Assert.assertEquals("bana", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replaceAll(StrMatcher.stringMatcher("an"), "XX");
        Assert.assertEquals("bXXXXa", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replaceFirst(StrMatcher.stringMatcher("an"), "XX");
        Assert.assertEquals("bXXana", sb.toString());

        sb.clear();
        sb.append("banana");
        sb.replace(StrMatcher.stringMatcher("an"), "XX", 0, 6, 1);
        Assert.assertEquals("bXXana", sb.toString());

        sb.clear();
        sb.replace((StrMatcher) null, "XX", 0, 0, 1);
        Assert.assertEquals(0, sb.length());
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

        sb.append("   \r\n\t ");
        sb.trim();
        Assert.assertEquals("", sb.toString());

        sb.clear();
        sb.append("  hello world  \n");
        sb.trim();
        Assert.assertEquals("hello world", sb.toString());
    }

    @Test
    public void testStartsAndEndsWith() {
        StrBuilder sb = new StrBuilder("hello world");
        Assert.assertFalse(sb.startsWith(null));
        Assert.assertTrue(sb.startsWith(""));
        Assert.assertTrue(sb.startsWith("hello"));
        Assert.assertFalse(sb.startsWith("world"));
        Assert.assertFalse(sb.startsWith("hello world longer"));

        Assert.assertFalse(sb.endsWith(null));
        Assert.assertTrue(sb.endsWith(""));
        Assert.assertTrue(sb.endsWith("world"));
        Assert.assertFalse(sb.endsWith("hello"));
        Assert.assertFalse(sb.endsWith("hello world longer"));
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

        Assert.assertEquals("", sb.midString(-1, -1));
        Assert.assertEquals("", sb.midString(0, 0));
        Assert.assertEquals("", sb.midString(20, 5));
        Assert.assertEquals("hel", sb.midString(-1, 3));
        Assert.assertEquals("lo ", sb.midString(3, 3));
        Assert.assertEquals("world", sb.midString(6, 20));
    }

    @Test
    public void testContainsAndIndexOf() {
        StrBuilder sb = new StrBuilder("hello world hello");
        Assert.assertTrue(sb.contains('h'));
        Assert.assertFalse(sb.contains('z'));
        Assert.assertTrue(sb.contains("world"));
        Assert.assertFalse(sb.contains("foo"));
        Assert.assertTrue(sb.contains(StrMatcher.stringMatcher("world")));
        Assert.assertFalse(sb.contains(StrMatcher.stringMatcher("foo")));

        Assert.assertEquals(0, sb.indexOf('h'));
        Assert.assertEquals(-1, sb.indexOf('z'));
        Assert.assertEquals(0, sb.indexOf('h', -5));
        Assert.assertEquals(12, sb.indexOf('h', 1));
        Assert.assertEquals(-1, sb.indexOf('h', 20));

        Assert.assertEquals(-1, sb.indexOf((String) null));
        Assert.assertEquals(-1, sb.indexOf((String) null, 2));
        Assert.assertEquals(0, sb.indexOf(""));
        Assert.assertEquals(2, sb.indexOf("", 2));
        Assert.assertEquals(-1, sb.indexOf("", 20));
        Assert.assertEquals(0, sb.indexOf("h"));
        Assert.assertEquals(0, sb.indexOf("hello"));
        Assert.assertEquals(12, sb.indexOf("hello", 1));
        Assert.assertEquals(6, sb.indexOf("world"));
        Assert.assertEquals(-1, sb.indexOf("notfound"));
        Assert.assertEquals(-1, sb.indexOf("hello world hello longer"));

        Assert.assertEquals(-1, sb.indexOf((StrMatcher) null));
        Assert.assertEquals(-1, sb.indexOf((StrMatcher) null, 2));
        Assert.assertEquals(-1, sb.indexOf(StrMatcher.stringMatcher("hello"), 20));
        Assert.assertEquals(0, sb.indexOf(StrMatcher.stringMatcher("hello")));
        Assert.assertEquals(12, sb.indexOf(StrMatcher.stringMatcher("hello"), 1));

        Assert.assertEquals(12, sb.lastIndexOf('h'));
        Assert.assertEquals(-1, sb.lastIndexOf('z'));
        Assert.assertEquals(-1, sb.lastIndexOf('h', -1));
        Assert.assertEquals(0, sb.lastIndexOf('h', 5));
        Assert.assertEquals(12, sb.lastIndexOf('h', 20));

        Assert.assertEquals(-1, sb.lastIndexOf((String) null));
        Assert.assertEquals(-1, sb.lastIndexOf((String) null, 2));
        Assert.assertEquals(-1, sb.lastIndexOf("hello", -1));
        Assert.assertEquals(17, sb.lastIndexOf(""));
        Assert.assertEquals(5, sb.lastIndexOf("", 5));
        Assert.assertEquals(12, sb.lastIndexOf("h"));
        Assert.assertEquals(12, sb.lastIndexOf("hello"));
        Assert.assertEquals(0, sb.lastIndexOf("hello", 5));
        Assert.assertEquals(-1, sb.lastIndexOf("notfound"));
        Assert.assertEquals(-1, sb.lastIndexOf("hello world hello longer"));

        Assert.assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        Assert.assertEquals(-1, sb.lastIndexOf((StrMatcher) null, 2));
        Assert.assertEquals(-1, sb.lastIndexOf(StrMatcher.stringMatcher("hello"), -1));
        Assert.assertEquals(12, sb.lastIndexOf(StrMatcher.stringMatcher("hello")));
        Assert.assertEquals(0, sb.lastIndexOf(StrMatcher.stringMatcher("hello"), 5));
        Assert.assertEquals(12, sb.lastIndexOf(StrMatcher.stringMatcher("hello"), 20));
    }

    @Test
    public void testTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tok = sb.asTokenizer();
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, tok.getTokenArray());
        Assert.assertEquals("a b c", tok.getContent());

        tok.reset(new char[]{'d', 'e'});
        Assert.assertArrayEquals(new String[]{"d", "e"}, tok.getTokenArray());
        Assert.assertEquals("d e", tok.getContent());
    }

    @Test
    public void testReader() throws IOException {
        StrBuilder sb = new StrBuilder("hello world");
        Reader reader = sb.asReader();
        Assert.assertTrue(reader.ready());
        Assert.assertTrue(reader.markSupported());
        Assert.assertEquals('h', (char) reader.read());

        char[] buf = new char[4];
        int readLen = reader.read(buf, 0, 4);
        Assert.assertEquals(4, readLen);
        Assert.assertEquals("ello", new String(buf));

        reader.mark(10);
        Assert.assertEquals(1, reader.skip(1));
        Assert.assertEquals('w', (char) reader.read());
        reader.reset();
        Assert.assertEquals(' ', (char) reader.read());

        Assert.assertEquals(0, reader.skip(-5));
        Assert.assertEquals(5, reader.skip(100));
        Assert.assertEquals(-1, reader.read());
        Assert.assertEquals(-1, reader.read(buf, 0, 4));
        Assert.assertEquals(0, reader.read(buf, 0, 0));
        reader.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReaderReadInvalidRangeOffNegative() throws IOException {
        new StrBuilder("abc").asReader().read(new char[5], -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReaderReadInvalidRangeLenNegative() throws IOException {
        new StrBuilder("abc").asReader().read(new char[5], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReaderReadInvalidRangeLenTooLarge() throws IOException {
        new StrBuilder("abc").asReader().read(new char[5], 3, 3);
    }

    @Test
    public void testWriter() throws IOException {
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

        Assert.assertEquals(sb1.hashCode(), sb2.hashCode());
        Assert.assertNotEquals(0, sb1.hashCode());
    }

    @Test
    public void testToStringAndBuffer() {
        StrBuilder sb = new StrBuilder("hello");
        Assert.assertEquals("hello", sb.toString());
        StringBuffer sbuf = sb.toStringBuffer();
        Assert.assertEquals("hello", sbuf.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateIndexNegative() {
        new StrBuilder("abc").validateIndex(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testValidateIndexTooLarge() {
        new StrBuilder("abc").validateIndex(4);
    }
}
