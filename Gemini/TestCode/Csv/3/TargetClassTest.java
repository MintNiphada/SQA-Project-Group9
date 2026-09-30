package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.BACKSPACE;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.END_OF_STREAM;
import static org.apache.commons.csv.Constants.FF;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.TAB;
import static org.apache.commons.csv.Constants.UNDEFINED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

/**
 * Unit test for {@link Lexer}.
 */
public class LexerTest {

    private static class TestLexer extends Lexer {
        TestLexer(final CSVFormat format, final ExtendedBufferedReader in) {
            super(format, in);
        }

        @Override
        Token nextToken(final Token reusableToken) throws IOException {
            return reusableToken;
        }
    }

    private TestLexer createLexer(final String input, final CSVFormat format) {
        return new TestLexer(format, new ExtendedBufferedReader(new StringReader(input)));
    }

    @Test
    public void testGetLineNumber() throws IOException {
        final String input = "a,b,c\n1,2,3\n4,5,6";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT);
        assertEquals(0, lexer.getLineNumber());

        while (lexer.in.read() != END_OF_STREAM) {
            // consume stream
        }
        assertEquals(2, lexer.getLineNumber());
    }

    @Test
    public void testReadEscapeSpecialChars() throws IOException {
        final String input = "rntbf";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT.withEscape('\\'));

        assertEquals(CR, lexer.readEscape());
        assertEquals(LF, lexer.readEscape());
        assertEquals(TAB, lexer.readEscape());
        assertEquals(BACKSPACE, lexer.readEscape());
        assertEquals(FF, lexer.readEscape());
    }

    @Test
    public void testReadEscapeControlChars() throws IOException {
        final String input = "" + CR + LF + FF + TAB + BACKSPACE;
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT.withEscape('\\'));

        assertEquals(CR, lexer.readEscape());
        assertEquals(LF, lexer.readEscape());
        assertEquals(FF, lexer.readEscape());
        assertEquals(TAB, lexer.readEscape());
        assertEquals(BACKSPACE, lexer.readEscape());
    }

    @Test
    public void testReadEscapeMetaAndRegularChars() throws IOException {
        final String input = "\\,'\"abc";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT.withEscape('\\'));

        assertEquals('\\', lexer.readEscape());
        assertEquals(',', lexer.readEscape());
        assertEquals('\'', lexer.readEscape());
        assertEquals('\"', lexer.readEscape());
        assertEquals('a', lexer.readEscape());
        assertEquals('b', lexer.readEscape());
        assertEquals('c', lexer.readEscape());
    }

    @Test(expected = IOException.class)
    public void testReadEscapeAtEOF() throws IOException {
        final String input = "";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT.withEscape('\\'));
        lexer.readEscape();
    }

    @Test
    public void testTrimTrailingSpaces() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);

        final StringBuilder sb = new StringBuilder();
        lexer.trimTrailingSpaces(sb);
        assertEquals("", sb.toString());

        sb.setLength(0);
        sb.append("   ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("", sb.toString());

        sb.setLength(0);
        sb.append("no-trailing-spaces");
        lexer.trimTrailingSpaces(sb);
        assertEquals("no-trailing-spaces", sb.toString());

        sb.setLength(0);
        sb.append("trailing spaces   ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("trailing spaces", sb.toString());

        sb.setLength(0);
        sb.append("multiple whitespaces \t \r \n ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("multiple whitespaces", sb.toString());
    }

    @Test
    public void testReadEndOfLineCRLF() throws IOException {
        final String input = "\r\nNextLine";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT);

        final int firstChar = lexer.in.read();
        assertEquals(CR, firstChar);
        assertTrue(lexer.readEndOfLine(firstChar));
        assertEquals('N', lexer.in.read());
    }

    @Test
    public void testReadEndOfLineLF() throws IOException {
        final String input = "\nNextLine";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT);

        final int firstChar = lexer.in.read();
        assertEquals(LF, firstChar);
        assertTrue(lexer.readEndOfLine(firstChar));
        assertEquals('N', lexer.in.read());
    }

    @Test
    public void testReadEndOfLineCR() throws IOException {
        final String input = "\rNextLine";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT);

        final int firstChar = lexer.in.read();
        assertEquals(CR, firstChar);
        assertTrue(lexer.readEndOfLine(firstChar));
        assertEquals('N', lexer.in.read());
    }

    @Test
    public void testReadEndOfLineNonEol() throws IOException {
        final String input = "abc";
        final TestLexer lexer = createLexer(input, CSVFormat.DEFAULT);

        final int firstChar = lexer.in.read();
        assertEquals('a', firstChar);
        assertFalse(lexer.readEndOfLine(firstChar));
        assertEquals('b', lexer.in.read());
    }

    @Test
    public void testIsWhitespace() {
        final CSVFormat format = CSVFormat.DEFAULT; // comma delimiter
        final TestLexer lexer = createLexer("", format);

        assertTrue(lexer.isWhitespace(' '));
        assertTrue(lexer.isWhitespace('\t'));
        assertTrue(lexer.isWhitespace('\r'));
        assertTrue(lexer.isWhitespace('\n'));
        assertTrue(lexer.isWhitespace('\f'));
        assertFalse(lexer.isWhitespace(','));
        assertFalse(lexer.isWhitespace('a'));
        assertFalse(lexer.isWhitespace('1'));

        final CSVFormat spaceDelimFormat = CSVFormat.newFormat(' ');
        final TestLexer spaceLexer = createLexer("", spaceDelimFormat);
        assertFalse(spaceLexer.isWhitespace(' '));
        assertTrue(spaceLexer.isWhitespace('\t'));

        final CSVFormat tabDelimFormat = CSVFormat.TDF;
        final TestLexer tabLexer = createLexer("", tabDelimFormat);
        assertFalse(tabLexer.isWhitespace('\t'));
        assertTrue(tabLexer.isWhitespace(' '));
    }

    @Test
    public void testIsStartOfLine() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);

        assertTrue(lexer.isStartOfLine(CR));
        assertTrue(lexer.isStartOfLine(LF));
        assertTrue(lexer.isStartOfLine(UNDEFINED));
        assertFalse(lexer.isStartOfLine(' '));
        assertFalse(lexer.isStartOfLine('a'));
        assertFalse(lexer.isStartOfLine(END_OF_STREAM));
    }

    @Test
    public void testIsEndOfFile() {
        final TestLexer lexer = createLexer("", CSVFormat.DEFAULT);

        assertTrue(lexer.isEndOfFile(END_OF_STREAM));
        assertFalse(lexer.isEndOfFile(0));
        assertFalse(lexer.isEndOfFile('a'));
        assertFalse(lexer.isEndOfFile(CR));
        assertFalse(lexer.isEndOfFile(LF));
    }

    @Test
    public void testIsDelimiter() {
        final TestLexer commaLexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue(commaLexer.isDelimiter(','));
        assertFalse(commaLexer.isDelimiter(';'));
        assertFalse(commaLexer.isDelimiter(' '));

        final TestLexer semicolonLexer = createLexer("", CSVFormat.newFormat(';'));
        assertTrue(semicolonLexer.isDelimiter(';'));
        assertFalse(semicolonLexer.isDelimiter(','));
    }

    @Test
    public void testIsEscape() {
        final TestLexer lexerWithEscape = createLexer("", CSVFormat.DEFAULT.withEscape('\\'));
        assertTrue(lexerWithEscape.isEscape('\\'));
        assertFalse(lexerWithEscape.isEscape('/'));
        assertFalse(lexerWithEscape.isEscape('\ufffe'));

        final TestLexer lexerWithoutEscape = createLexer("", CSVFormat.DEFAULT.withEscape(null));
        assertFalse(lexerWithoutEscape.isEscape('\\'));
        assertTrue(lexerWithoutEscape.isEscape('\ufffe'));
    }

    @Test
    public void testIsQuoteChar() {
        final TestLexer lexerWithQuote = createLexer("", CSVFormat.DEFAULT.withQuote('\"'));
        assertTrue(lexerWithQuote.isQuoteChar('\"'));
        assertFalse(lexerWithQuote.isQuoteChar('\''));
        assertFalse(lexerWithQuote.isQuoteChar('\ufffe'));

        final TestLexer lexerWithoutQuote = createLexer("", CSVFormat.DEFAULT.withQuote(null));
        assertFalse(lexerWithoutQuote.isQuoteChar('\"'));
        assertTrue(lexerWithoutQuote.isQuoteChar('\ufffe'));
    }

    @Test
    public void testIsCommentStart() {
        final TestLexer lexerWithComment = createLexer("", CSVFormat.DEFAULT.withCommentStart('#'));
        assertTrue(lexerWithComment.isCommentStart('#'));
        assertFalse(lexerWithComment.isCommentStart('/'));
        assertFalse(lexerWithComment.isCommentStart('\ufffe'));

        final TestLexer lexerWithoutComment = createLexer("", CSVFormat.DEFAULT.withCommentStart(null));
        assertFalse(lexerWithoutComment.isCommentStart('#'));
        assertTrue(lexerWithoutComment.isCommentStart('\ufffe'));
    }

    @Test
    public void testFormatPropertiesPropagated() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false);
        final TestLexer lexer = createLexer("", format);

        assertTrue(lexer.ignoreSurroundingSpaces);
        assertFalse(lexer.ignoreEmptyLines);
        assertEquals(format, lexer.format);

        final CSVFormat format2 = CSVFormat.DEFAULT
                .withIgnoreSurroundingSpaces(false)
                .withIgnoreEmptyLines(true);
        final TestLexer lexer2 = createLexer("", format2);

        assertFalse(lexer2.ignoreSurroundingSpaces);
        assertTrue(lexer2.ignoreEmptyLines);
    }
}
