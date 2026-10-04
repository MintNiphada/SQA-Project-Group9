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

        StrBuilder sb4 = new StrBuilder(64);
        Assert.assertEquals(64, sb4.capacity());

        StrBuilder sb5 = new StrBuilder((String) null);
        Assert.assertEquals(32, sb5.capacity());
        Assert.assertEquals(0, sb5.length());

        StrBuilder sb6 = new StrBuilder("Hello");
        Assert.assertEquals(5 + 32, sb6.capacity());
        Assert.assertEquals("Hello", sb6.toString());
    }

    @Test
    public void testNewLineAndNullText() {
        StrBuilder sb = new StrBuilder();
        Assert.assertNull(sb.getNewLineText());
        sb.setNewLineText("\r\n");
        Assert.assertEquals("\r\n", sb.getNewLineText());
        sb.appendNewLine();
        Assert.assertEquals("\r\n", sb.toString());

        sb.clear();
        sb.setNewLineText(null);
        sb.appendNewLine();
        Assert.assertEquals(SystemUtils.LINE_SEPARATOR, sb.toString());

        Assert.assertNull(sb.getNullText());
        sb.setNullText("NULL");
        Assert.assertEquals("NULL", sb.getNullText());
        sb.setNullText("");
        Assert.assertNull(sb.getNullText());

        sb.setNullText("N/A");
        sb.clear();
        sb.appendNull();
        Assert.assertEquals("N/A", sb.toString());

        sb.setNullText(null);
        sb.clear();
        sb.appendNull();
        Assert.assertEquals(0, sb.length());
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

        sb.ensureCapacity(100);
        Assert.assertTrue(sb.capacity() >= 100);

        sb.minimizeCapacity();
        Assert.assertEquals(sb.length(), sb.capacity());

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

        sb.setCharAt(1, 'x');
        Assert.assertEquals("axc", sb.toString());

        sb.deleteCharAt(1);
        Assert.assertEquals("ac", sb.toString());
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
    public void testToCharArrayAndGetChars() {
        StrBuilder empty = new StrBuilder();
        Assert.assertArrayEquals(new char[0], empty.toCharArray());
        Assert.assertArrayEquals(new char[0], empty.toCharArray(0, 0));

        StrBuilder sb = new StrBuilder("Hello World");
        Assert.assertArrayEquals("Hello World".toCharArray(), sb.toCharArray());
        Assert.assertArrayEquals("Hello".toCharArray(), sb.toCharArray(0, 5));
        Assert.assertArrayEquals("World".toCharArray(), sb.toCharArray(6, 20));

        char[] dest = sb.getChars(null);
        Assert.assertEquals("Hello World", new String(dest));

        char[] smallDest = new char[2];
        char[] returned = sb.getChars(smallDest);
        Assert.assertEquals("Hello World", new String(returned));

        char[] exactDest = new char[11];
        Assert.assertSame(exactDest, sb.getChars(exactDest));

        char[] custom = new char[10];
        sb.getChars(0, 5, custom, 2);
        Assert.assertEquals("Hello", new String(custom, 2, 5));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsStartIndexNegative() {
        new StrBuilder("test").getChars(-1, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsEndIndexNegative() {
        new StrBuilder("test").getChars(0, -1, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsEndIndexOverflow() {
        new StrBuilder("test").getChars(0, 10, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsStartGreaterThanEnd() {
        new StrBuilder("test").getChars(3, 2, new char[5], 0);
    }

    @Test
    public void testAppendPrimitivesAndObjects() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) "Obj");
        sb.append((String) null);
        sb.append("Str");
        sb.append((String) null, 0, 0);
        sb.append("Substring", 3, 6);
        sb.append((StringBuffer) null);
        sb.append(new StringBuffer("Buf"));
        sb.append((StringBuffer) null, 0, 0);
        sb.append(new StringBuffer("BufSub"), 3, 3);
        sb.append((StrBuilder) null);
        sb.append(new StrBuilder("Bld"));
        sb.append((StrBuilder) null, 0, 0);
        sb.append(new StrBuilder("BldSub"), 3, 3);
        sb.append((char[]) null);
        sb.append(new char[]{'C', 'h', 'a', 'r'});
        sb.append((char[]) null, 0, 0);
        sb.append(new char[]{'a', 'C', 'h', 'a', 'r', 'z'}, 1, 4);
        sb.append(true);
        sb.append(false);
        sb.append('!');
        sb.append(123);
        sb.append(12345678901L);
        sb.append(1.5f);
        sb.append(2.5d);

        Assert.assertEquals("ObjStrstringBufSubBldSubCharChartruefalse!123123456789011.52.5", sb.toString());

        sb.setNullText("null");
        sb.clear();
        sb.append((Object) null);
        sb.append((String) null);
        Assert.assertEquals("nullnull", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringOutOfBounds1() {
        new StrBuilder().append("abc", -1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringOutOfBounds2() {
        new StrBuilder().append("abc", 4, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringOutOfBounds3() {
        new StrBuilder().append("abc", 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferOutOfBounds() {
        new StrBuilder().append(new StringBuffer("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderOutOfBounds() {
        new StrBuilder().append(new StrBuilder("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayOutOfBounds() {
        new StrBuilder().append(new char[]{'a', 'b'}, 1, 5);
    }

    @Test
    public void testAppendWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        sb.appendWithSeparators(new Object[0], ",");
        Assert.assertEquals("", sb.toString());

        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        Assert.assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"A", "B"}, null);
        Assert.assertEquals("AB", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Collection) null, ",");
        sb.appendWithSeparators(Collections.emptyList(), ",");
        sb.appendWithSeparators(Arrays.asList("X", "Y"), "-");
        Assert.assertEquals("X-Y", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("X", "Y"), null);
        Assert.assertEquals("XY", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator) null, ",");
        sb.appendWithSeparators(Arrays.asList("1", "2").iterator(), ":");
        Assert.assertEquals("1:2", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("1", "2").iterator(), null);
        Assert.assertEquals("12", sb.toString());
    }

    @Test
    public void testPaddingAndFixedWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-1, 'a');
        sb.appendPadding(3, '-');
        Assert.assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", -1, ' ');
        sb.appendFixedWidthPadLeft("abcdef", 4, ' ');
        sb.appendFixedWidthPadLeft("ab", 4, '0');
        Assert.assertEquals("cdef00ab", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadLeft(null, 6, ' ');
        Assert.assertEquals("  null", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(42, 4, '0');
        Assert.assertEquals("0042", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 4, ' ');
        sb.appendFixedWidthPadRight("ab", 4, '0');
        Assert.assertEquals("abcdab00", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(null, 6, ' ');
        Assert.assertEquals("null  ", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(42, 4, '0');
        Assert.assertEquals("4200", sb.toString());
    }

    @Test
    public void testInsertMethods() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, (Object) "b");
        Assert.assertEquals("abc", sb.toString());

        sb.insert(3, (Object) null);
        Assert.assertEquals("abc", sb.toString());

        sb.insert(0, (String) null);
        sb.insert(1, "1");
        Assert.assertEquals("a1bc", sb.toString());

        sb.insert(0, (char[]) null);
        sb.insert(2, new char[]{'2', '3'});
        Assert.assertEquals("a123bc", sb.toString());

        sb.insert(0, (char[]) null, 0, 0);
        sb.insert(4, new char[]{'x', '4', '5', 'y'}, 1, 2);
        Assert.assertEquals("a12345bc", sb.toString());

        sb.insert(0, true);
        sb.insert(sb.length(), false);
        Assert.assertEquals("truea12345bcfalse", sb.toString());

        sb.clear();
        sb.append("ac");
        sb.insert(1, 'b');
        sb.insert(0, 1);
        sb.insert(sb.length(), 2L);
        sb.insert(1, 3.0f);
        sb.insert(2, 4.0d);
        Assert.assertTrue(sb.length() > 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertIndexNegative() {
        new StrBuilder("abc").insert(-1, "test");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertIndexOverflow() {
        new StrBuilder("abc").insert(4, "test");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharsInvalidOffset() {
        new StrBuilder("abc").insert(0, new char[]{'a'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharsInvalidLength() {
        new StrBuilder("abc").insert(0, new char[]{'a'}, 0, 2);
    }

    @Test
    public void testDeleteAndReplace() {
        StrBuilder sb = new StrBuilder("Hello Beautiful World Beautiful");
        sb.delete(5, 15);
        Assert.assertEquals("Hello World Beautiful", sb.toString());
        sb.delete(11, 100);
        Assert.assertEquals("Hello World", sb.toString());
        sb.delete(0, 0);
        Assert.assertEquals("Hello World", sb.toString());

        sb.set("banana");
        sb.deleteAll('a');
        Assert.assertEquals("bnn", sb.toString());

        sb.set("banana");
        sb.deleteFirst('a');
        Assert.assertEquals("bnana", sb.toString());

        sb.set("banana");
        sb.deleteAll((String) null);
        sb.deleteAll("");
        sb.deleteAll("an");
        Assert.assertEquals("ba", sb.toString());

        sb.set("banana");
        sb.deleteFirst((String) null);
        sb.deleteFirst("");
        sb.deleteFirst("an");
        Assert.assertEquals("bana", sb.toString());

        sb.set("banana");
        sb.deleteAll(StrMatcher.stringMatcher("an"));
        Assert.assertEquals("ba", sb.toString());

        sb.set("banana");
        sb.deleteFirst(StrMatcher.stringMatcher("an"));
        Assert.assertEquals("bana", sb.toString());

        sb.set("banana");
        sb.replace(1, 3, "ox");
        Assert.assertEquals("boxana", sb.toString());
        sb.replace(0, 3, null);
        Assert.assertEquals("ana", sb.toString());

        sb.set("banana");
        sb.replaceAll('a', 'o');
        Assert.assertEquals("bonono", sb.toString());
        sb.replaceAll('z', 'x');
        Assert.assertEquals("bonono", sb.toString());

        sb.set("banana");
        sb.replaceFirst('a', 'o');
        Assert.assertEquals("bonana", sb.toString());

        sb.set("banana");
        sb.replaceAll((String) null, "x");
        sb.replaceAll("", "x");
        sb.replaceAll("an", "ox");
        Assert.assertEquals("boxoxa", sb.toString());

        sb.set("banana");
        sb.replaceFirst((String) null, "x");
        sb.replaceFirst("", "x");
        sb.replaceFirst("an", "ox");
        Assert.assertEquals("boxana", sb.toString());

        sb.set("banana");
        sb.replaceAll(StrMatcher.stringMatcher("an"), "ox");
        Assert.assertEquals("boxoxa", sb.toString());

        sb.set("banana");
        sb.replaceFirst(StrMatcher.stringMatcher("an"), "ox");
        Assert.assertEquals("boxana", sb.toString());

        sb.set("banana");
        sb.replace(null, "x", 0, 6, -1);
        sb.replace(StrMatcher.stringMatcher("an"), null, 0, 6, 1);
        Assert.assertEquals("bana", sb.toString());
    }

    private void set(StrBuilder sb, String str) {
        sb.clear();
        sb.append(str);
    }

    @Test
    public void testReverseAndTrim() {
        StrBuilder sb = new StrBuilder("");
        sb.reverse();
        Assert.assertEquals("", sb.toString());
        sb.append("12345");
        sb.reverse();
        Assert.assertEquals("54321", sb.toString());
        sb.append("6");
        sb.reverse();
        Assert.assertEquals("612345", sb.toString());

        StrBuilder trimSb = new StrBuilder("");
        trimSb.trim();
        Assert.assertEquals("", trimSb.toString());

        trimSb.append("   hello world  \t\n");
        trimSb.trim();
        Assert.assertEquals("hello world", trimSb.toString());

        trimSb.setLength(0);
        trimSb.append("   ");
        trimSb.trim();
        Assert.assertEquals("", trimSb.toString());
    }

    @Test
    public void testStartsAndEndsWith() {
        StrBuilder sb = new StrBuilder("Hello World");
        Assert.assertFalse(sb.startsWith(null));
        Assert.assertTrue(sb.startsWith(""));
        Assert.assertTrue(sb.startsWith("Hello"));
        Assert.assertFalse(sb.startsWith("World"));
        Assert.assertFalse(sb.startsWith("Hello World Long"));

        Assert.assertFalse(sb.endsWith(null));
        Assert.assertTrue(sb.endsWith(""));
        Assert.assertTrue(sb.endsWith("World"));
        Assert.assertFalse(sb.endsWith("Hello"));
        Assert.assertFalse(sb.endsWith("Hello World Long"));
    }

    @Test
    public void testSubstrings() {
        StrBuilder sb = new StrBuilder("Hello World");
        Assert.assertEquals("Hello World", sb.substring(0));
        Assert.assertEquals("World", sb.substring(6));
        Assert.assertEquals("Hello", sb.substring(0, 5));
        Assert.assertEquals("World", sb.substring(6, 50));

        Assert.assertEquals("", sb.leftString(-1));
        Assert.assertEquals("", sb.leftString(0));
        Assert.assertEquals("Hello", sb.leftString(5));
        Assert.assertEquals("Hello World", sb.leftString(50));

        Assert.assertEquals("", sb.rightString(-1));
        Assert.assertEquals("", sb.rightString(0));
        Assert.assertEquals("World", sb.rightString(5));
        Assert.assertEquals("Hello World", sb.rightString(50));

        Assert.assertEquals("", sb.midString(-1, 0));
        Assert.assertEquals("", sb.midString(0, -1));
        Assert.assertEquals("", sb.midString(50, 5));
        Assert.assertEquals("Hello", sb.midString(-5, 5));
        Assert.assertEquals("World", sb.midString(6, 50));
        Assert.assertEquals("lo", sb.midString(3, 2));
    }

    @Test
    public void testContainsAndIndexOf() {
        StrBuilder sb = new StrBuilder("banana");
        Assert.assertTrue(sb.contains('a'));
        Assert.assertFalse(sb.contains('z'));
        Assert.assertTrue(sb.contains("nan"));
        Assert.assertFalse(sb.contains("xyz"));
        Assert.assertTrue(sb.contains(StrMatcher.charMatcher('b')));
        Assert.assertFalse(sb.contains(StrMatcher.charMatcher('z')));

        Assert.assertEquals(1, sb.indexOf('a'));
        Assert.assertEquals(3, sb.indexOf('a', 2));
        Assert.assertEquals(-1, sb.indexOf('a', 10));
        Assert.assertEquals(1, sb.indexOf('a', -5));

        Assert.assertEquals(-1, sb.indexOf((String) null));
        Assert.assertEquals(0, sb.indexOf(""));
        Assert.assertEquals(1, sb.indexOf("an"));
        Assert.assertEquals(3, sb.indexOf("an", 2));
        Assert.assertEquals(-1, sb.indexOf("banana extra"));
        Assert.assertEquals(-1, sb.indexOf("xyz"));
        Assert.assertEquals(1, sb.indexOf("a"));

        Assert.assertEquals(-1, sb.indexOf((StrMatcher) null));
        Assert.assertEquals(1, sb.indexOf(StrMatcher.charMatcher('a')));
        Assert.assertEquals(3, sb.indexOf(StrMatcher.charMatcher('a'), 2));
        Assert.assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 10));

        Assert.assertEquals(5, sb.lastIndexOf('a'));
        Assert.assertEquals(3, sb.lastIndexOf('a', 4));
        Assert.assertEquals(-1, sb.lastIndexOf('a', -1));
        Assert.assertEquals(-1, sb.lastIndexOf('z'));

        Assert.assertEquals(-1, sb.lastIndexOf((String) null));
        Assert.assertEquals(-1, sb.lastIndexOf("a", -1));
        Assert.assertEquals(5, sb.lastIndexOf(""));
        Assert.assertEquals(3, sb.lastIndexOf("an"));
        Assert.assertEquals(1, sb.lastIndexOf("an", 2));
        Assert.assertEquals(5, sb.lastIndexOf("a"));
        Assert.assertEquals(-1, sb.lastIndexOf("banana extra"));
        Assert.assertEquals(-1, sb.lastIndexOf("xyz"));

        Assert.assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        Assert.assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), -1));
        Assert.assertEquals(5, sb.lastIndexOf(StrMatcher.charMatcher('a')));
        Assert.assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('a'), 4));
        Assert.assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('z')));
    }

    @Test
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        StrBuilder sb3 = new StrBuilder("HELLO");
        StrBuilder sb4 = new StrBuilder("Hello World");

        Assert.assertTrue(sb1.equals(sb1));
        Assert.assertTrue(sb1.equals(sb2));
        Assert.assertFalse(sb1.equals(sb3));
        Assert.assertFalse(sb1.equals(sb4));
        Assert.assertFalse(sb1.equals("Hello"));
        Assert.assertFalse(sb1.equals((Object) null));

        Assert.assertTrue(sb1.equalsIgnoreCase(sb1));
        Assert.assertTrue(sb1.equalsIgnoreCase(sb2));
        Assert.assertTrue(sb1.equalsIgnoreCase(sb3));
        Assert.assertFalse(sb1.equalsIgnoreCase(sb4));
        Assert.assertFalse(sb1.equalsIgnoreCase(null));

        Assert.assertEquals(sb1.hashCode(), sb2.hashCode());
        Assert.assertEquals("Hello", sb1.toStringBuffer().toString());
    }

    @Test
    public void testTokenizerView() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tok = sb.asTokenizer();
        Assert.assertEquals(3, tok.getTokenArray().length);
        Assert.assertEquals("a b c", tok.getContent());

        tok.reset(new char[]{'x', ' ', 'y'});
        Assert.assertEquals(2, tok.getTokenArray().length);
        Assert.assertEquals("x y", tok.getContent());
    }

    @Test
    public void testReaderView() throws Exception {
        StrBuilder sb = new StrBuilder("Reading Test");
        Reader reader = sb.asReader();

        Assert.assertTrue(reader.ready());
        Assert.assertTrue(reader.markSupported());
        Assert.assertEquals('R', (char) reader.read());

        char[] buf = new char[4];
        Assert.assertEquals(4, reader.read(buf, 0, 4));
        Assert.assertEquals("eadi", new String(buf));

        reader.mark(0);
        Assert.assertEquals(2, reader.skip(2));
        Assert.assertEquals(' ', (char) reader.read());
        reader.reset();
        Assert.assertEquals('n', (char) reader.read());

        Assert.assertEquals(0, reader.read(buf, 0, 0));
        Assert.assertEquals(0, reader.skip(-5));
        Assert.assertEquals(6, reader.skip(100));
        Assert.assertFalse(reader.ready());
        Assert.assertEquals(-1, reader.read());
        Assert.assertEquals(-1, reader.read(buf, 0, 4));

        reader.close();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testReaderOutOfBounds() throws Exception {
        Reader reader = new StrBuilder("test").asReader();
        reader.read(new char[2], 0, 5);
    }

    @Test
    public void testWriterView() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();

        writer.write('H');
        writer.write(new char[]{'e', 'l'});
        writer.write(new char[]{'l', 'o', '!'}, 0, 2);
        writer.write(" World");
        writer.write("! extra", 0, 1);
        writer.flush();
        writer.close();

        Assert.assertEquals("Hello World!", sb.toString());
    }
}
