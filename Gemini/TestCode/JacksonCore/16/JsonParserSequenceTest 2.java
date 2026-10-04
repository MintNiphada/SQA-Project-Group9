package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonParserSequenceTest {

    private final JsonFactory JSON_F = new JsonFactory();

    @Test
    public void testCreateFlattenedSimple() throws IOException {
        JsonParser p1 = JSON_F.createParser("[1]");
        JsonParser p2 = JSON_F.createParser("[2]");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        Assert.assertNotNull(seq);
        Assert.assertEquals(2, seq.containedParsersCount());

        seq.close();
    }

    @Test
    public void testCreateFlattenedWithFirstAsSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seqCombined = JsonParserSequence.createFlattened(seq1, p3);

        Assert.assertEquals(3, seqCombined.containedParsersCount());
        seqCombined.close();
    }

    @Test
    public void testCreateFlattenedWithSecondAsSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p2, p3);
        JsonParserSequence seqCombined = JsonParserSequence.createFlattened(p1, seq2);

        Assert.assertEquals(3, seqCombined.containedParsersCount());
        seqCombined.close();
    }

    @Test
    public void testCreateFlattenedWithBothAsSequences() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");
        JsonParser p4 = JSON_F.createParser("4");

        JsonParserSequence seq1 = JsonParserSequence.createFlattened(p1, p2);
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(p3, p4);
        JsonParserSequence seqCombined = JsonParserSequence.createFlattened(seq1, seq2);

        Assert.assertEquals(4, seqCombined.containedParsersCount());
        seqCombined.close();
    }

    @Test
    public void testCreateFlattenedWithNestedSequences() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        JsonParserSequence innerSeq = JsonParserSequence.createFlattened(p1, p2);
        // Create an array containing a sequence directly to hit the nested branch in addFlattenedActiveParsers
        JsonParserSequence outerSeq = new JsonParserSequence(new JsonParser[] { innerSeq, p3 });

        JsonParser p4 = JSON_F.createParser("4");
        JsonParserSequence flattened = JsonParserSequence.createFlattened(outerSeq, p4);

        Assert.assertEquals(4, flattened.containedParsersCount());
        flattened.close();
    }

    @Test
    public void testFlattenedPartiallyConsumedSequence() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParser p3 = JSON_F.createParser("3");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        // Consume p1 completely to switch to p2
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        Assert.assertEquals(1, seq.getIntValue());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        Assert.assertEquals(2, seq.getIntValue());

        // Now seq's _nextParser is 2 (delegate is p2, active index is 1)
        JsonParserSequence seq2 = JsonParserSequence.createFlattened(seq, p3);
        // Only active parser (p2) and p3 should be flattened into seq2
        Assert.assertEquals(2, seq2.containedParsersCount());
        seq2.close();
    }

    @Test
    public void testNextTokenMultiParsers() throws IOException {
        JsonParser p1 = JSON_F.createParser("1 2");
        JsonParser p2 = JSON_F.createParser("3");
        JsonParser p3 = JSON_F.createParser("\"abc\"");

        JsonParserSequence seq = JsonParserSequence.createFlattened(p1, p2);
        seq = JsonParserSequence.createFlattened(seq, p3);

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        Assert.assertEquals(1, seq.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        Assert.assertEquals(2, seq.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        Assert.assertEquals(3, seq.getIntValue());

        Assert.assertEquals(JsonToken.VALUE_STRING, seq.nextToken());
        Assert.assertEquals("abc", seq.getText());

        Assert.assertNull(seq.nextToken());
        Assert.assertNull(seq.nextToken());

        seq.close();
    }

    @Test
    public void testNextTokenWithEmptyParsers() throws IOException {
        JsonParser p1 = JSON_F.createParser("");
        JsonParser p2 = JSON_F.createParser("123");
        JsonParser p3 = JSON_F.createParser("");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        Assert.assertEquals(123, seq.getIntValue());

        Assert.assertNull(seq.nextToken());
        seq.close();
    }

    @Test
    public void testNextTokenAllEmptyParsers() throws IOException {
        JsonParser p1 = JSON_F.createParser("");
        JsonParser p2 = JSON_F.createParser("");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2 });
        Assert.assertNull(seq.nextToken());
        seq.close();
    }

    @Test
    public void testCloseClosesAllRemainingParsers() throws IOException {
        final boolean[] closed = new boolean[3];
        JsonParser p1 = new TestCloseParser(JSON_F.createParser("1"), () -> closed[0] = true);
        JsonParser p2 = new TestCloseParser(JSON_F.createParser("2"), () -> closed[1] = true);
        JsonParser p3 = new TestCloseParser(JSON_F.createParser("3"), () -> closed[2] = true);

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });
        Assert.assertFalse(closed[0]);
        Assert.assertFalse(closed[1]);
        Assert.assertFalse(closed[2]);

        seq.close();

        Assert.assertTrue(closed[0]);
        Assert.assertTrue(closed[1]);
        Assert.assertTrue(closed[2]);
    }

    @Test
    public void testClosePartiallyConsumedSequence() throws IOException {
        final boolean[] closed = new boolean[3];
        JsonParser p1 = new TestCloseParser(JSON_F.createParser("1"), () -> closed[0] = true);
        JsonParser p2 = new TestCloseParser(JSON_F.createParser("2"), () -> closed[1] = true);
        JsonParser p3 = new TestCloseParser(JSON_F.createParser("3"), () -> closed[2] = true);

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2, p3 });

        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken());
        Assert.assertEquals(JsonToken.VALUE_NUMBER_INT, seq.nextToken()); // switches to p2

        seq.close();

        // p2 and p3 must be closed via the close loop
        Assert.assertTrue(closed[1]);
        Assert.assertTrue(closed[2]);
    }

    @Test
    public void testSwitchToNextWhenExhausted() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");

        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2 });

        Assert.assertTrue(seq.switchToNext());
        Assert.assertFalse(seq.switchToNext());
        Assert.assertFalse(seq.switchToNext());

        seq.close();
    }

    @Test
    public void testAddFlattenedActiveParsersDirectly() throws IOException {
        JsonParser p1 = JSON_F.createParser("1");
        JsonParser p2 = JSON_F.createParser("2");
        JsonParserSequence seq = new JsonParserSequence(new JsonParser[] { p1, p2 });

        List<JsonParser> list = new ArrayList<JsonParser>();
        seq.addFlattenedActiveParsers(list);

        Assert.assertEquals(2, list.size());
        Assert.assertSame(p1, list.get(0));
        Assert.assertSame(p2, list.get(1));

        seq.close();
    }

    private static class TestCloseParser extends JsonParserDelegate {
        private final Runnable onClose;

        public TestCloseParser(JsonParser delegate, Runnable onClose) {
            super(delegate);
            this.onClose = onClose;
        }

        @Override
        public void close() throws IOException {
            super.close();
            onClose.run();
        }
    }
}
