package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class ExtendedBufferedReaderTest {

    private ExtendedBufferedReader createBufferedReader(String input) {
        return new ExtendedBufferedReader(new StringReader(input));
    }

    @Test
    public void testInitialState() {
        ExtendedBufferedReader reader = createBufferedReader("abc");
        Assert.assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        Assert.assertEquals(0, reader.getLineNumber());
    }

    @Test
    public void testReadCharByChar() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("a\nb\nc");
        Assert.assertEquals('a', reader.read());
        Assert.assertEquals('a', reader.readAgain());
        Assert.assertEquals(0, reader.getLineNumber());

        Assert.assertEquals('\n', reader.read());
        Assert.assertEquals('\n', reader.readAgain());
        Assert.assertEquals(1, reader.getLineNumber());

        Assert.assertEquals('b', reader.read());
        Assert.assertEquals('b', reader.readAgain());
        Assert.assertEquals(1, reader.getLineNumber());

        Assert.assertEquals('\n', reader.read());
        Assert.assertEquals('\n', reader.readAgain());
        Assert.assertEquals(2, reader.getLineNumber());

        Assert.assertEquals('c', reader.read());
        Assert.assertEquals('c', reader.readAgain());
        Assert.assertEquals(2, reader.getLineNumber());

        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        Assert.assertEquals(2, reader.getLineNumber());
    }

    @Test
    public void testLookAhead() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("123");
        Assert.assertEquals('1', reader.lookAhead());
        Assert.assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        Assert.assertEquals('1', reader.read());
        Assert.assertEquals('1', reader.readAgain());

        Assert.assertEquals('2', reader.lookAhead());
        Assert.assertEquals('2', reader.lookAhead());
        Assert.assertEquals('2', reader.read());
        Assert.assertEquals('2', reader.readAgain());

        Assert.assertEquals('3', reader.read());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
    }

    @Test
    public void testReadCharArrayZeroLength() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("abc");
        char[] buf = new char[5];
        int read = reader.read(buf, 0, 0);
        Assert.assertEquals(0, read);
        Assert.assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        Assert.assertEquals(0, reader.getLineNumber());
    }

    @Test
    public void testReadCharArray() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("hello\r\nworld\r\n");
        char[] buf = new char[7];
        int read = reader.read(buf, 0, 7);
        Assert.assertEquals(7, read);
        Assert.assertEquals("hello\r\n", new String(buf, 0, read));
        Assert.assertEquals('\n', reader.readAgain());
        Assert.assertEquals(1, reader.getLineNumber());

        char[] buf2 = new char[7];
        int read2 = reader.read(buf2, 0, 7);
        Assert.assertEquals(7, read2);
        Assert.assertEquals("world\r\n", new String(buf2, 0, read2));
        Assert.assertEquals('\n', reader.readAgain());
        Assert.assertEquals(2, reader.getLineNumber());

        int read3 = reader.read(buf2, 0, 7);
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, read3);
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testReadCharArrayWithCarriageReturnSplit() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("a\r\nb");
        char[] buf = new char[2];
        
        int len1 = reader.read(buf, 0, 2);
        Assert.assertEquals(2, len1);
        Assert.assertEquals('a', buf[0]);
        Assert.assertEquals('\r', buf[1]);
        Assert.assertEquals('\r', reader.readAgain());
        Assert.assertEquals(1, reader.getLineNumber());

        int len2 = reader.read(buf, 0, 2);
        Assert.assertEquals(2, len2);
        Assert.assertEquals('\n', buf[0]);
        Assert.assertEquals('b', buf[1]);
        Assert.assertEquals('b', reader.readAgain());
        Assert.assertEquals(1, reader.getLineNumber());

        int len3 = reader.read(buf, 0, 2);
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, len3);
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testReadCharArrayWithOffset() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("test\rline");
        char[] buf = new char[10];
        int read = reader.read(buf, 2, 6);
        Assert.assertEquals(6, read);
        Assert.assertEquals("test\rl", new String(buf, 2, read));
        Assert.assertEquals('l', reader.readAgain());
        Assert.assertEquals(1, reader.getLineNumber());
    }

    @Test
    public void testReadLine() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("line1\r\n\r\nline3");

        String line1 = reader.readLine();
        Assert.assertEquals("line1", line1);
        Assert.assertEquals('1', reader.readAgain());
        Assert.assertEquals(1, reader.getLineNumber());

        String line2 = reader.readLine();
        Assert.assertEquals("", line2);
        Assert.assertEquals('1', reader.readAgain());
        Assert.assertEquals(2, reader.getLineNumber());

        String line3 = reader.readLine();
        Assert.assertEquals("line3", line3);
        Assert.assertEquals('3', reader.readAgain());
        Assert.assertEquals(3, reader.getLineNumber());

        String line4 = reader.readLine();
        Assert.assertNull(line4);
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        Assert.assertEquals(3, reader.getLineNumber());
    }

    @Test
    public void testMixedReadsAndLineCount() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("a\nb\r\nc\rd");
        
        Assert.assertEquals('a', reader.read());
        Assert.assertEquals('\n', reader.read());
        Assert.assertEquals(1, reader.getLineNumber());

        char[] buf = new char[4];
        int count = reader.read(buf, 0, 4);
        Assert.assertEquals(4, count);
        Assert.assertEquals("b\r\nc", new String(buf, 0, 4));
        Assert.assertEquals(2, reader.getLineNumber());

        String line = reader.readLine();
        Assert.assertEquals("", line);
        Assert.assertEquals(3, reader.getLineNumber());

        String lastLine = reader.readLine();
        Assert.assertEquals("d", lastLine);
        Assert.assertEquals(4, reader.getLineNumber());

        Assert.assertNull(reader.readLine());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
    }

    @Test
    public void testEmptyReader() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("");
        Assert.assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        Assert.assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        Assert.assertNull(reader.readLine());
        Assert.assertEquals(0, reader.getLineNumber());
    }

    @Test
    public void testMultipleNewlinesInCharArray() throws IOException {
        ExtendedBufferedReader reader = createBufferedReader("\n\n\r\r\n\n");
        char[] buf = new char[6];
        int count = reader.read(buf, 0, 6);
        Assert.assertEquals(6, count);
        Assert.assertEquals(5, reader.getLineNumber());
        Assert.assertEquals('\n', reader.readAgain());
    }
}