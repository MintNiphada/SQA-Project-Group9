package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

public class LexerTest {

    private static class TestLexer extends Lexer {
        TestLexer(CSVFormat format, ExtendedBufferedReader in) {
            super(format, in);
        }

        @Override
        Token nextToken(Token reusableToken) throws IOException {
            return null;
        }
    }

    private ExtendedBufferedReader createReader(String input) {
        return new ExtendedBufferedReader(new StringReader(input));
    }

    private TestLexer createLexer(CSVFormat format, String input) {
        return new TestLexer(format, createReader(input));
    }

    @Test
    public void testConstructorWithNullEscapeQuoteComment() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null).withQuoteChar(null).withCommentStart(null);
        TestLexer lexer = createLexer(format, "");
        // DISABLED is '\ufffe', but we can't access it directly. Test that isEscape returns false for any char.
        for (int c = 0; c <= 0xFFFF; c++) {
            assertFalse(lexer.isEscape(c));
            assertFalse(lexer.isQuoteChar(c));
            assertFalse(lexer.isCommentStart(c));
        }
        // Delimiter should be the default comma
        assertTrue(lexer.isDelimiter(','));
        assertFalse(lexer.isDelimiter(';'));
        assertFalse(lexer.ignoreSurroundingSpaces);
        assertFalse(lexer.ignoreEmptyLines);
    }

    @Test
    public void testConstructorWithSpecificChars() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|').withEscape('\\').withQuoteChar('"').withCommentStart('#')
                .withIgnoreSurroundingSpaces(true).withIgnoreEmptyLines(true);
        TestLexer lexer = createLexer(format, "");
        assertTrue(lexer.isDelimiter('|'));
        assertFalse(lexer.isDelimiter(','));
        assertTrue(lexer.isEscape('\\'));
        assertFalse(lexer.isEscape('!'));
        assertTrue(lexer.isQuoteChar('"'));
        assertFalse(lexer.isQuoteChar('\''));
        assertTrue(lexer.isCommentStart('#'));
        assertFalse(lexer.isCommentStart('!'));
        assertTrue(lexer.ignoreSurroundingSpaces);
        assertTrue(lexer.ignoreEmptyLines);
    }

    @Test
    public void testGetLineNumber() throws IOException {
        ExtendedBufferedReader reader = createReader("line1\nline2\n");
        TestLexer lexer = new TestLexer(CSVFormat.DEFAULT, reader);
        assertEquals(1, lexer.getLineNumber()); // initially line 1
        reader.read(); // consume 'l'
        assertEquals(1, lexer.getLineNumber());
        reader.read(); // consume 'i'
        // read until newline
        while (reader.read() != '\n') {}
        assertEquals(2, lexer.getLineNumber());
    }

    @Test
    public void testReadEscapeLowerCaseR() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "r");
        int result = lexer.readEscape();
        assertEquals(CR, result);
        assertEquals('r', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeLowerCaseN() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "n");
        int result = lexer.readEscape();
        assertEquals(LF, result);
        assertEquals('n', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeLowerCaseT() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "t");
        int result = lexer.readEscape();
        assertEquals(TAB, result);
        assertEquals('t', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeLowerCaseB() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "b");
        int result = lexer.readEscape();
        assertEquals(BACKSPACE, result);
        assertEquals('b', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeLowerCaseF() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "f");
        int result = lexer.readEscape();
        assertEquals(FF, result);
        assertEquals('f', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeCR() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\r");
        int result = lexer.readEscape();
        assertEquals(CR, result);
        assertEquals('\r', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeLF() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\n");
        int result = lexer.readEscape();
        assertEquals(LF, result);
        assertEquals('\n', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeFF() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\f");
        int result = lexer.readEscape();
        assertEquals(FF, result);
        assertEquals('\f', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeTAB() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\t");
        int result = lexer.readEscape();
        assertEquals(TAB, result);
        assertEquals('\t', lexer.in.getLastChar());
    }

    @Test
    public void testReadEscapeBACKSPACE() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\b");
        int result = lexer.readEscape();
        assertEquals(BACKSPACE, result);
        assertEquals('\b', lexer.in.getLastChar());
    }

    @Test(expected = IOException.class)
    public void testReadEscapeEndOfStream() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        lexer.readEscape();
    }

    @Test
    public void testReadEscapeDefault() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "x");
        int result = lexer.readEscape();
        assertEquals('x', result);
        assertEquals('x', lexer.in.getLastChar());
    }

    @Test
    public void testTrimTrailingSpacesNoTrailingSpaces() {
        StringBuilder sb = new StringBuilder("abc");
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        lexer.trimTrailingSpaces(sb);
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testTrimTrailingSpacesWithSpaces() {
        StringBuilder sb = new StringBuilder("abc   ");
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        lexer.trimTrailingSpaces(sb);
        assertEquals("abc", sb.toString());
    }

    @Test
    public void testTrimTrailingSpacesOnlySpaces() {
        StringBuilder sb = new StringBuilder("   ");
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        lexer.trimTrailingSpaces(sb);
        assertEquals("", sb.toString());
    }

    @Test
    public void testTrimTrailingSpacesEmpty() {
        StringBuilder sb = new StringBuilder("");
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        lexer.trimTrailingSpaces(sb);
        assertEquals("", sb.toString());
    }

    @Test
    public void testTrimTrailingSpacesMixedWhitespace() {
        StringBuilder sb = new StringBuilder("a\t\n ");
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        lexer.trimTrailingSpaces(sb);
        assertEquals("a", sb.toString());
    }

    @Test
    public void testReadEndOfLineCRLF() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\r\n");
        boolean result = lexer.readEndOfLine(CR);
        assertTrue(result);
        // The LF should have been consumed, next char should be end of stream
        assertEquals(END_OF_STREAM, lexer.in.read());
    }

    @Test
    public void testReadEndOfLineCR() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\r");
        boolean result = lexer.readEndOfLine(CR);
        assertTrue(result);
        // No LF to consume, next char is end of stream
        assertEquals(END_OF_STREAM, lexer.in.read());
    }

    @Test
    public void testReadEndOfLineLF() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\n");
        boolean result = lexer.readEndOfLine(LF);
        assertTrue(result);
        assertEquals(END_OF_STREAM, lexer.in.read());
    }

    @Test
    public void testReadEndOfLineOtherChar() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "a");
        boolean result = lexer.readEndOfLine('a');
        assertFalse(result);
        // The character should not be consumed
        assertEquals('a', lexer.in.read());
    }

    @Test
    public void testReadEndOfLineCRNotFollowedByLF() throws IOException {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "\rX");
        boolean result = lexer.readEndOfLine(CR);
        assertTrue(result);
        // The next char should be 'X', not consumed
        assertEquals('X', lexer.in.read());
    }

    @Test
    public void testIsWhitespaceDelimiter() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withDelimiter(' '), "");
        assertFalse(lexer.isWhitespace(' '));
    }

    @Test
    public void testIsWhitespaceSpace() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertTrue(lexer.isWhitespace(' '));
    }

    @Test
    public void testIsWhitespaceTab() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertTrue(lexer.isWhitespace('\t'));
    }

    @Test
    public void testIsWhitespaceNonWhitespace() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertFalse(lexer.isWhitespace('a'));
    }

    @Test
    public void testIsStartOfLineLF() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertTrue(lexer.isStartOfLine(LF));
    }

    @Test
    public void testIsStartOfLineCR() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertTrue(lexer.isStartOfLine(CR));
    }

    @Test
    public void testIsStartOfLineUNDEFINED() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertTrue(lexer.isStartOfLine(UNDEFINED));
    }

    @Test
    public void testIsStartOfLineOther() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertFalse(lexer.isStartOfLine('a'));
    }

    @Test
    public void testIsEndOfFileEOF() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertTrue(lexer.isEndOfFile(END_OF_STREAM));
    }

    @Test
    public void testIsEndOfFileNotEOF() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT, "");
        assertFalse(lexer.isEndOfFile('a'));
    }

    @Test
    public void testIsDelimiterMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withDelimiter(';'), "");
        assertTrue(lexer.isDelimiter(';'));
    }

    @Test
    public void testIsDelimiterNoMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withDelimiter(';'), "");
        assertFalse(lexer.isDelimiter(','));
    }

    @Test
    public void testIsEscapeMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withEscape('\\'), "");
        assertTrue(lexer.isEscape('\\'));
    }

    @Test
    public void testIsEscapeNoMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withEscape('\\'), "");
        assertFalse(lexer.isEscape('!'));
    }

    @Test
    public void testIsQuoteCharMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withQuoteChar('"'), "");
        assertTrue(lexer.isQuoteChar('"'));
    }

    @Test
    public void testIsQuoteCharNoMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withQuoteChar('"'), "");
        assertFalse(lexer.isQuoteChar('\''));
    }

    @Test
    public void testIsCommentStartMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withCommentStart('#'), "");
        assertTrue(lexer.isCommentStart('#'));
    }

    @Test
    public void testIsCommentStartNoMatch() {
        TestLexer lexer = createLexer(CSVFormat.DEFAULT.withCommentStart('#'), "");
        assertFalse(lexer.isCommentStart('!'));
    }
}
