package org.apache.commons.csv;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class ExtendedBufferedReaderTest {

    private ExtendedBufferedReader reader;
    private static final int UNDEFINED = -2;
    private static final int END_OF_STREAM = -1;

    @Before
    public void setUp() {
        // reader will be set in each test as needed
    }

    @Test
    public void testConstructorPass() {
        Reader r = new StringReader("");
        reader = new ExtendedBufferedReader(r);
        assertNotNull(reader);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReader() {
        new ExtendedBufferedReader(null);
    }

    @Test
    public void testReadAgainInitially() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc"));
        assertEquals(UNDEFINED, reader.readAgain());
    }

    @Test
    public void testReadAgainAfterRead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc"));
        assertEquals('a', reader.read());
        assertEquals('a', reader.readAgain());
    }

    @Test
    public void testReadAgainAfterReadEOF() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("a"));
        assertEquals('a', reader.read());
        assertEquals(END_OF_STREAM, reader.read());
        assertEquals(END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testReadAgainAfterReadCharArray() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abcdef"));
        char[] buf = new char[3];
        int len = reader.read(buf, 0, 3);
        assertEquals(3, len);
        assertEquals('c', reader.readAgain());
    }

    @Test
    public void testReadAgainAfterReadCharArrayEOF() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("ab"));
        char[] buf = new char[5];
        int len = reader.read(buf, 0, 5);
        assertEquals(2, len);
        assertEquals('b', reader.readAgain());
        len = reader.read(buf, 0, 5);
        assertEquals(END_OF_STREAM, len);
        assertEquals(END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testReadAgainAfterReadLine() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("hello\nworld"));
        String line = reader.readLine();
        assertEquals("hello", line);
        assertEquals('o', reader.readAgain());
        line = reader.readLine();
        assertEquals("world", line);
        assertEquals('d', reader.readAgain());
        line = reader.readLine();
        assertNull(line);
        assertEquals(END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testReadAgainAfterReadLineEmptyLine() throws IOException {
        // empty line: readLine returns "", lastChar unchanged from previous state
        reader = new ExtendedBufferedReader(new StringReader("hello\n\nworld"));
        String line = reader.readLine(); // "hello"
        assertEquals('o', reader.readAgain());
        line = reader.readLine(); // ""
        // lastChar not updated, still 'o'
        assertEquals('o', reader.readAgain());
        line = reader.readLine(); // "world"
        assertEquals('d', reader.readAgain());
    }

    @Test
    public void testReadAgainAfterLookAhead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc"));
        assertEquals('a', reader.lookAhead());
        // lookAhead should not set lastChar
        assertEquals(UNDEFINED, reader.readAgain());
    }

    @Test
    public void testReadSingleCharNewline() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("a\nb"));
        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.read());
        assertEquals(1, reader.getLineNumber());
        assertEquals(END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
    }

    @Test
    public void testReadSingleCharMultipleNewlines() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("\n\n\n"));
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.read());
        assertEquals(3, reader.getLineNumber());
        assertEquals(END_OF_STREAM, reader.read());
    }

    @Test
    public void testReadCharArrayLengthZero() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[2];
        int len = reader.read(buf, 0, 0);
        assertEquals(0, len);
        // nothing changed; lastChar stays UNDEFINED
        assertEquals(UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    @Test
    public void testReadCharArrayNormal() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abcdef"));
        char[] buf = new char[3];
        int len = reader.read(buf, 0, 3);
        assertEquals(3, len);
        assertArrayEquals(new char[]{'a','b','c'}, buf);
        assertEquals('c', reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    @Test
    public void testReadCharArraySingleCR() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("a\rb"));
        char[] buf = new char[3];
        int len = reader.read(buf, 0, 3);
        assertEquals(3, len);
        // a, \r, b
        assertEquals(1, reader.getLineNumber()); // \r increments
        assertEquals('b', reader.readAgain());
    }

    @Test
    public void testReadCharArraySingleLF() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("a\nb"));
        char[] buf = new char[3];
        int len = reader.read(buf, 0, 3);
        assertEquals(3, len);
        // a, \n, b -> \n increments line, because previous char 'a' != \r
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
    }

    @Test
    public void testReadCharArrayCRLFCombined() throws IOException {
        // \r\n should count as one line
        reader = new ExtendedBufferedReader(new StringReader("a\r\nb"));
        char[] buf = new char[4];
        int len = reader.read(buf, 0, 4);
        assertEquals(4, len);
        // a, \r, \n, b
        // \r increments line to 1, then \n sees previous char \r, no increment
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
    }

    @Test
    public void testReadCharArrayCRLFComplex() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[4];
        int len = reader.read(buf, 0, 4);
        assertEquals(4, len);
        // first \r -> line 1, \n -> previous \r, no inc. second \r -> line 2, \n -> no inc.
        assertEquals(2, reader.getLineNumber());
    }

    @Test
    public void testReadCharArrayCRLFAtBufferBoundary() throws IOException {
        // break \r\n across buffer reads: first read gets \r, next read gets \n
        // The code checks i>0 ? buf[i-1]: lastChar, so cross-boundary detection works via lastChar
        reader = new ExtendedBufferedReader(new StringReader("a\r\nb"));
        char[] buf1 = new char[2];
        int len1 = reader.read(buf1, 0, 2);
        assertEquals(2, len1);
        assertEquals('a', buf1[0]);
        assertEquals('\r', buf1[1]);
        assertEquals(0, reader.getLineNumber()); // \r hasn't been processed yet? In read(char[],...), line is incremented during loop, so \r should increment line. So after reading 'a','\r', lineCounter should be 1.
        assertEquals(1, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());

        char[] buf2 = new char[2];
        int len2 = reader.read(buf2, 0, 2);
        assertEquals(2, len2);
        assertEquals('\n', buf2[0]);
        assertEquals('b', buf2[1]);
        // \n sees lastChar = '\r', so no increment.
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
    }

    @Test
    public void testReadCharArrayCRLFInMiddle() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("ab\r\ncd"));
        char[] buf = new char[6];
        int len = reader.read(buf, 0, 6);
        assertEquals(6, len);
        // ab, then \r increments line (1), \n no inc (prev \r), c,d. So line count 1.
        assertEquals(1, reader.getLineNumber());
    }

    @Test
    public void testReadCharArrayEndOfStream() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[5];
        int len = reader.read(buf, 0, 5);
        assertEquals(3, len);
        assertEquals('c', reader.readAgain());
        // next read returns -1
        len = reader.read(buf, 0, 5);
        assertEquals(END_OF_STREAM, len);
        assertEquals(END_OF_STREAM, reader.readAgain());
        // lineCounter unchanged on EOF
        assertEquals(0, reader.getLineNumber());
    }

    @Test
    public void testReadLineNormal() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("hello\nworld\nfoo"));
        assertEquals("hello", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('o', reader.readAgain());
        assertEquals("world", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('d', reader.readAgain());
        assertEquals("foo", reader.readLine());
        assertEquals(3, reader.getLineNumber());
        assertEquals('o', reader.readAgain());
        assertNull(reader.readLine());
        assertEquals(END_OF_STREAM, reader.readAgain());
        assertEquals(3, reader.getLineNumber()); // line number doesn't increase on null
    }

    @Test
    public void testReadLineEmptyLine() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        String line = reader.readLine(); // first line is empty
        assertEquals("", line);
        assertEquals(1, reader.getLineNumber());
        // lastChar unchanged? lastChar was UNDEFINED, then line.length()==0 so not set => stays UNDEFINED
        assertEquals(UNDEFINED, reader.readAgain());

        line = reader.readLine(); // second empty
        assertEquals("", line);
        assertEquals(2, reader.getLineNumber());
        assertEquals(UNDEFINED, reader.readAgain());

        assertNull(reader.readLine());
        assertEquals(END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testReadLineEOF() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(""));
        assertNull(reader.readLine());
        assertEquals(END_OF_STREAM, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    @Test
    public void testReadLineAfterPartialRead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("hello\nworld"));
        // read some chars first
        assertEquals('h', reader.read());
        assertEquals('e', reader.read());
        // now readLine, should read the rest "llo"
        String line = reader.readLine();
        assertEquals("llo", line);
        assertEquals(1, reader.getLineNumber());
        assertEquals('o', reader.readAgain());
        line = reader.readLine();
        assertEquals("world", line);
        assertEquals(2, reader.getLineNumber());
        assertEquals('d', reader.readAgain());
        assertNull(reader.readLine());
        assertEquals(END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testLookAhead() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abc"));
        assertEquals('a', reader.lookAhead());
        // should still be 'a'
        assertEquals('a', reader.read());
        assertEquals('b', reader.lookAhead());
        assertEquals('b', reader.read());
        assertEquals('c', reader.lookAhead());
        assertEquals('c', reader.read());
        assertEquals(END_OF_STREAM, reader.lookAhead());
        assertEquals(END_OF_STREAM, reader.read());
    }

    @Test
    public void testLookAheadAtEOF() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader(""));
        assertEquals(END_OF_STREAM, reader.lookAhead());
        assertEquals(END_OF_STREAM, reader.read());
    }

    @Test
    public void testGetLineNumber() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("\n\n\n"));
        assertEquals(0, reader.getLineNumber());
        reader.read(); // \n
        assertEquals(1, reader.getLineNumber());
        reader.read(); // \n
        assertEquals(2, reader.getLineNumber());
        reader.read(); // \n
        assertEquals(3, reader.getLineNumber());
    }

    @Test
    public void testReadCharArrayLineCountingSingleCharReadsVersusBulk() throws IOException {
        // Verify that line counting works the same when reading character by character
        String data = "a\rb\nc\r\nd\ne\n\rf";
        // expected lines: a\r -> line, b\n -> line, c\r\n -> one line, d\n -> line, e\n -> line, \r -> line, f
        // So lines: a, b, c, d, e, (empty line after \r?), f? Actually \r and \n are line separators.
        // Let's analyze manually using BufferedReader.readLine behavior:
        // BufferedReader readLine: a\r -> "a", b\n -> "b", c\r\n -> "c", d\n -> "d", e\n -> "e", \r -> "", f -> "f"
        // So total 7 lines (including empty line). According to our read(char[]) logic:
        // a: char, \r: line++, \n? after \r no inc. So after a\r: line=1.
        // b: char, \n: line++ (prev char b != \r) -> line=2.
        // c: char, \r: line++ ->3, \n: previous \r so no inc -> line=3.
        // d: char, \n: line++ ->4.
        // e: char, \n: line++ ->5.
        // \r: line++ ->6.
        // f: char. So lineCounter=6. That matches readLine? readLine would count lines as:
        // "a" (1), "b" (2), "c" (3), "d" (4), "e" (5), "" (6), "f" (7). Indeed readLine increments for each non-null line, even empty. So readLine count would be 7.
        // Our read(char[]) count is 6 because the last line "f" did not end with a line separator, so no increment for that. This is consistent with typical line counting where only terminated lines are counted. The readLine method increments per call. So we need to check both.
        // For this test, we'll validate the read(char[]) line counting.

        reader = new ExtendedBufferedReader(new StringReader(data));
        char[] buf = new char[data.length()];
        int len = reader.read(buf, 0, buf.length);
        assertEquals(data.length(), len);
        // Expected line count: line increments on each \r (3 occurrences: after a, c, and one standalone) and on \n unless preceded by \r (b's \n, d's \n, e's \n) -> that's 3+3=6? Actually: 
        // \r after a: inc ->1
        // \n after b: inc ->2 (prev b != \r)
        // \r after c: inc ->3
        // \n after c's \r: no inc (prev \r) ->3
        // \n after d: inc ->4
        // \n after e: inc ->5
        // \r before f: inc ->6
        // So lineCounter = 6.
        assertEquals(6, reader.getLineNumber());
    }

    @Test
    public void testReadLineLineNumberEmptyLinesMix() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("a\n\nb\n"));
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals("b", reader.readLine());
        assertEquals(3, reader.getLineNumber());
        assertNull(reader.readLine()); // EOF
        assertEquals(END_OF_STREAM, reader.readAgain());
        assertEquals(3, reader.getLineNumber()); // no increase
    }

    @Test
    public void testReadAgainAfterReadLineWithCR() throws IOException {
        // line ending with \r is still a line; readLine returns line content without \r
        reader = new ExtendedBufferedReader(new StringReader("hello\rworld\n"));
        assertEquals("hello", reader.readLine());
        assertEquals('o', reader.readAgain());
        assertEquals("world", reader.readLine());
        assertEquals('d', reader.readAgain());
        assertNull(reader.readLine());
        assertEquals(END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testReadCharArrayLastCharWhenBufferExactFits() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abcdef"));
        char[] buf = new char[6];
        int len = reader.read(buf, 0, 6);
        assertEquals(6, len);
        assertEquals('f', reader.readAgain());
    }

    @Test
    public void testReadCharArrayMultipleReadsLastChar() throws IOException {
        reader = new ExtendedBufferedReader(new StringReader("abcdefgh"));
        char[] buf1 = new char[3];
        reader.read(buf1, 0, 3);
        assertEquals('c', reader.readAgain());
        char[] buf2 = new char[3];
        reader.read(buf2, 0, 3);
        assertEquals('f', reader.readAgain());
        char[] buf3 = new char[3];
        int len3 = reader.read(buf3, 0, 3);
        assertEquals(2, len3); // "gh"
        assertEquals('h', reader.readAgain());
        // next read should get EOF
        assertEquals(END_OF_STREAM, reader.read());
    }

    @Test
    public void testIOExceptionOnRead() {
        Reader brokenReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test exception");
            }
            @Override
            public void close() throws IOException { }
        };
        reader = new ExtendedBufferedReader(brokenReader);
        try {
            reader.read();
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("test exception", e.getMessage());
        }
    }

    @Test
    public void testIOExceptionOnReadCharArray() {
        Reader brokenReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test exception");
            }
            @Override
            public void close() throws IOException { }
        };
        reader = new ExtendedBufferedReader(brokenReader);
        try {
            reader.read(new char[5], 0, 5);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("test exception", e.getMessage());
        }
    }

    @Test
    public void testIOExceptionOnReadLine() {
        Reader brokenReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test exception");
            }
            @Override
            public void close() throws IOException { }
        };
        reader = new ExtendedBufferedReader(brokenReader);
        try {
            reader.readLine();
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("test exception", e.getMessage());
        }
    }

    @Test
    public void testIOExceptionOnLookAhead() {
        Reader brokenReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test exception");
            }
            @Override
            public void close() throws IOException { }
        };
        reader = new ExtendedBufferedReader(brokenReader);
        try {
            reader.lookAhead();
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("test exception", e.getMessage());
        }
    }
}