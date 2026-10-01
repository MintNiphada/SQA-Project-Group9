package com.fasterxml.jackson.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonParseException;

public class JsonParserSequenceTest {

    // Helper class to expose protected members for testing
    private static class TestableJsonParserSequence extends JsonParserSequence {
        public TestableJsonParserSequence(JsonParser[] parsers) {
            super(parsers);
        }

        public boolean switchToNextPublic() {
            return switchToNext();
        }

        public void addFlattenedActiveParsersPublic(List<JsonParser> result) {
            addFlattenedActiveParsers(result);
        }

        public JsonParser getDelegate() {
            return delegate;
        }

        public int getNextParserIndex() {
            return _nextParser;
        }
    }

    private static JsonFactory JSON_FACTORY = new JsonFactory();

    private static JsonParser parser(String content) throws IOException {
        return JSON_FACTORY.createParser(content);
    }

    @Test
    public void testCreateFlattenedSimpleNoSequences() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(2, seq.containedParsersCount());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertNull(seq.nextToken());
        seq.close();
    }

    @Test
    public void testCreateFlattenedFirstSequence() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(seq1, p3);
        assertEquals(3, seq2.containedParsersCount());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken());
        assertNull(seq2.nextToken());
        seq2.close();
    }

    @Test
    public void testCreateFlattenedSecondSequence() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p2, p3);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p1, seq1);
        assertEquals(3, seq2.containedParsersCount());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken());
        assertNull(seq2.nextToken());
        seq2.close();
    }

    @Test
    public void testCreateFlattenedBothSequences() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParser p4 = parser("4");
        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p3, p4);
        JsonParserSequence seq3 = JsonParserSequence.createFlattened(seq1, seq2);
        assertEquals(4, seq3.containedParsersCount());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertNull(seq3.nextToken());
        seq3.close();
    }

    @Test
    public void testCreateFlattenedNestedSequence() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParser p4 = parser("4");
        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(seq1, p3);
        JsonParserSequence seq3 = JsonParserSequence.createFlattened(seq2, p4);
        assertEquals(4, seq3.containedParsersCount());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq3.nextToken());
        assertNull(seq3.nextToken());
        seq3.close();
    }

    @Test
    public void testCreateFlattenedSkipsConsumedParsers() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParser p4 = parser("4");
        TestableJsonParserSequence seq = new TestableJsonParserSequence(new JsonParser[]{p1, p2, p3});
        // Consume first parser (p1) to advance _nextParser to 2
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        // Now create flattened with consumed sequence
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(seq, p4);
        assertEquals(3, seq2.containedParsersCount()); // p2,p3,p4
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken()); // p2
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken()); // p3
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq2.nextToken()); // p4
        assertNull(seq2.nextToken());
        seq2.close();
    }

    @Test
    public void testNextTokenBasic() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertNull(seq.nextToken());
    }

    @Test
    public void testNextTokenFirstParserReturnsNullImmediately() throws Exception {
        JsonParser p1 = parser(""); // empty, nextToken returns null
        JsonParser p2 = parser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        assertNull(seq.nextToken());
    }

    @Test
    public void testNextTokenAllEmpty() throws Exception {
        JsonParser p1 = parser("");
        JsonParser p2 = parser("");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertNull(seq.nextToken());
    }

    @Test
    public void testNextTokenMultipleParsers() throws Exception {
        JsonParser p1 = parser("true");
        JsonParser p2 = parser("false");
        JsonParser p3 = parser("null");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq = JsonParserSequence.createFlattened(seq, p3);
        assertEquals(JsonToken.VALUE_TRUE, seq.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, seq.nextToken());
        assertEquals(JsonToken.VALUE_NULL, seq.nextToken());
        assertNull(seq.nextToken());
    }

    @Test
    public void testNextTokenAfterEnd() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        assertNotNull(seq.nextToken());
        assertNotNull(seq.nextToken());
        assertNull(seq.nextToken());
        assertNull(seq.nextToken()); // repeated call after end
        assertNull(seq.nextToken());
    }

    @Test
    public void testCloseClosesAllParsers() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq = JsonParserSequence.createFlattened(seq, p3);
        seq.close();
        assertTrue(p1.isClosed());
        assertTrue(p2.isClosed());
        assertTrue(p3.isClosed());
    }

    @Test
    public void testSwitchToNext() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        TestableJsonParserSequence seq = new TestableJsonParserSequence(new JsonParser[]{p1, p2, p3});
        assertSame(p1, seq.getDelegate());
        assertEquals(1, seq.getNextParserIndex());
        assertTrue(seq.switchToNextPublic());
        assertSame(p2, seq.getDelegate());
        assertEquals(2, seq.getNextParserIndex());
        assertTrue(seq.switchToNextPublic());
        assertSame(p3, seq.getDelegate());
        assertEquals(3, seq.getNextParserIndex());
        assertFalse(seq.switchToNextPublic());
        assertSame(p3, seq.getDelegate());
        assertEquals(3, seq.getNextParserIndex());
    }

    @Test
    public void testAddFlattenedActiveParsers() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParser p4 = parser("4");
        TestableJsonParserSequence seq = new TestableJsonParserSequence(new JsonParser[]{p1, p2});
        List<JsonParser> result = new ArrayList<JsonParser>();
        result.add(p3); // start with an existing parser
        seq.addFlattenedActiveParsersPublic(result);
        assertEquals(3, result.size());
        assertSame(p1, result.get(0));
        assertSame(p2, result.get(1));
        assertSame(p3, result.get(2)); // unchanged
    }

    @Test
    public void testAddFlattenedActiveParsersWithNestedSequenceAndSkip() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        // Create nested sequence: seqInner = (p1,p2); seqOuter = (seqInner,p3)
        JsonParserSequence seqInner = JsonParserSequence.createFlattened(p1, p2);
        TestableJsonParserSequence seqOuter = new TestableJsonParserSequence(new JsonParser[]{seqInner, p3});
        // Consume first token from seqInner to advance seqInner's _nextParser
        assertEquals(JsonToken.VALUE_NUMBER_INT, seqOuter.nextToken()); // this delegates to seqInner, consumes p1
        // Now seqInner._nextParser=2; addFlattenedActiveParsers on seqOuter should include p2 (from seqInner) and p3
        List<JsonParser> result = new ArrayList<JsonParser>();
        seqOuter.addFlattenedActiveParsersPublic(result);
        assertEquals(2, result.size());
        assertSame(p2, result.get(0));
        assertSame(p3, result.get(1));
    }

    @Test
    public void testContainedParsersCount() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        JsonParser p3 = parser("3");
        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq = JsonParserSequence.createFlattened(seq, p3);
        assertEquals(3, seq.containedParsersCount());
    }

    @Test
    public void testConstructorDirectly() throws Exception {
        JsonParser p1 = parser("1");
        JsonParser p2 = parser("2");
        TestableJsonParserSequence seq = new TestableJsonParserSequence(new JsonParser[]{p1, p2});
        assertSame(p1, seq.getDelegate());
        assertEquals(1, seq.getNextParserIndex());
        assertEquals(2, seq.containedParsersCount());
    }
}
