package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class TokenQueueTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {
        new TokenQueue(null);
    }

    @Test
    public void testIsEmptyAndPeek() {
        TokenQueue tq = new TokenQueue("");
        Assert.assertTrue(tq.isEmpty());
        Assert.assertEquals(0, tq.peek());

        TokenQueue tq2 = new TokenQueue("abc");
        Assert.assertFalse(tq2.isEmpty());
        Assert.assertEquals('a', tq2.peek());
    }

    @Test
    public void testAddFirstCharAndString() {
        TokenQueue tq = new TokenQueue("world");
        tq.addFirst(Character.valueOf(' '));
        tq.addFirst("hello");
        Assert.assertEquals("hello world", tq.remainder());
    }

    @Test
    public void testMatchesAndMatchesCS() {
        TokenQueue tq = new TokenQueue("HelloWorld");
        Assert.assertTrue(tq.matches("hello"));
        Assert.assertTrue(tq.matches("HELLO"));
        Assert.assertFalse(tq.matchesCS("hello"));
        Assert.assertTrue(tq.matchesCS("Hello"));
        Assert.assertFalse(tq.matches("World"));
    }

    @Test
    public void testMatchesAnyString() {
        TokenQueue tq = new TokenQueue("One Two Three");
        Assert.assertTrue(tq.matchesAny("none", "ONE", "two"));
        Assert.assertFalse(tq.matchesAny("two", "three"));
    }

    @Test
    public void testMatchesAnyChar() {
        TokenQueue empty = new TokenQueue("");
        Assert.assertFalse(empty.matchesAny('a', 'b'));

        TokenQueue tq = new TokenQueue("test");
        Assert.assertTrue(tq.matchesAny('a', 't'));
        Assert.assertFalse(tq.matchesAny('a', 'b'));
    }

    @Test
    public void testMatchesStartTag() {
        TokenQueue tq1 = new TokenQueue("<a");
        Assert.assertTrue(tq1.matchesStartTag());

        TokenQueue tq2 = new TokenQueue("<1");
        Assert.assertFalse(tq2.matchesStartTag());

        TokenQueue tq3 = new TokenQueue("<");
        Assert.assertFalse(tq3.matchesStartTag());

        TokenQueue tq4 = new TokenQueue("a<a");
        Assert.assertFalse(tq4.matchesStartTag());
    }

    @Test
    public void testMatchChomp() {
        TokenQueue tq = new TokenQueue("abcdef");
        Assert.assertFalse(tq.matchChomp("bc"));
        Assert.assertTrue(tq.matchChomp("abc"));
        Assert.assertEquals("def", tq.remainder());
    }

    @Test
    public void testMatchesWhitespaceAndWord() {
        TokenQueue tq = new TokenQueue(" \t\r\n\fword123!");
        Assert.assertTrue(tq.matchesWhitespace());
        Assert.assertFalse(tq.matchesWord());

        tq.consumeWhitespace();
        Assert.assertFalse(tq.matchesWhitespace());
        Assert.assertTrue(tq.matchesWord());

        TokenQueue empty = new TokenQueue("");
        Assert.assertFalse(empty.matchesWhitespace());
        Assert.assertFalse(empty.matchesWord());
    }

    @Test
    public void testAdvanceAndConsume() {
        TokenQueue tq = new TokenQueue("abc");
        tq.advance();
        Assert.assertEquals('b', tq.consume());
        Assert.assertEquals('c', tq.consume());
        tq.advance();
        Assert.assertTrue(tq.isEmpty());
    }

    @Test
    public void testConsumeStringSuccess() {
        TokenQueue tq = new TokenQueue("abcdef");
        tq.consume("ABC");
        Assert.assertEquals("def", tq.remainder());
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeStringMismatch() {
        TokenQueue tq = new TokenQueue("abcdef");
        tq.consume("xyz");
    }

    @Test
    public void testConsumeTo() {
        TokenQueue tq = new TokenQueue("one two three");
        Assert.assertEquals("one ", tq.consumeTo("two"));
        Assert.assertEquals("two three", tq.remainder());

        TokenQueue tq2 = new TokenQueue("one two three");
        Assert.assertEquals("one two three", tq2.consumeTo("four"));
        Assert.assertTrue(tq2.isEmpty());
    }

    @Test
    public void testConsumeToIgnoreCase() {
        TokenQueue tq = new TokenQueue("one TWO three");
        Assert.assertEquals("one ", tq.consumeToIgnoreCase("two"));
        Assert.assertEquals("TWO three", tq.remainder());

        TokenQueue tq2 = new TokenQueue("one 123 four");
        Assert.assertEquals("one ", tq2.consumeToIgnoreCase("123"));
        Assert.assertEquals("123 four", tq2.remainder());

        TokenQueue tq3 = new TokenQueue("one 124 four");
        Assert.assertEquals("one 124 four", tq3.consumeToIgnoreCase("123"));
        Assert.assertTrue(tq3.isEmpty());

        TokenQueue tq4 = new TokenQueue("abcdef");
        Assert.assertEquals("abcdef", tq4.consumeToIgnoreCase("xyz"));
        Assert.assertTrue(tq4.isEmpty());

        TokenQueue tq5 = new TokenQueue("a123");
        Assert.assertEquals("a123", tq5.consumeToIgnoreCase("999"));
        Assert.assertTrue(tq5.isEmpty());
    }

    @Test
    public void testConsumeToAny() {
        TokenQueue tq = new TokenQueue("one two three");
        Assert.assertEquals("one ", tq.consumeToAny("THREE", "TWO"));
        Assert.assertEquals("two three", tq.remainder());

        TokenQueue tq2 = new TokenQueue("one two");
        Assert.assertEquals("one two", tq2.consumeToAny("four", "five"));
        Assert.assertTrue(tq2.isEmpty());
    }

    @Test
    public void testChompToAndChompToIgnoreCase() {
        TokenQueue tq1 = new TokenQueue("one two three");
        Assert.assertEquals("one ", tq1.chompTo("two"));
        Assert.assertEquals(" three", tq1.remainder());

        TokenQueue tq2 = new TokenQueue("one TWO three");
        Assert.assertEquals("one ", tq2.chompToIgnoreCase("two"));
        Assert.assertEquals(" three", tq2.remainder());
    }

    @Test
    public void testChompBalanced() {
        TokenQueue tq = new TokenQueue("(one (two) three) four");
        Assert.assertEquals("one (two) three", tq.chompBalanced('(', ')'));
        Assert.assertEquals(" four", tq.remainder());

        TokenQueue tqEscaped = new TokenQueue("(\\(one\\)) two");
        Assert.assertEquals("\\(one\\)", tqEscaped.chompBalanced('(', ')'));
        Assert.assertEquals(" two", tqEscaped.remainder());

        TokenQueue empty = new TokenQueue("");
        Assert.assertEquals("", empty.chompBalanced('(', ')'));

        TokenQueue unmatched = new TokenQueue("no parens");
        Assert.assertEquals("", unmatched.chompBalanced('(', ')'));
    }

    @Test
    public void testUnescape() {
        Assert.assertEquals("hello world", TokenQueue.unescape("hello world"));
        Assert.assertEquals("hello world", TokenQueue.unescape("hello\\ world"));
        Assert.assertEquals("hello\\world", TokenQueue.unescape("hello\\\\world"));
        Assert.assertEquals("a\\b\\c", TokenQueue.unescape("a\\\\b\\\\c"));
    }

    @Test
    public void testConsumeWhitespace() {
        TokenQueue tq = new TokenQueue("   hello");
        Assert.assertTrue(tq.consumeWhitespace());
        Assert.assertFalse(tq.consumeWhitespace());
        Assert.assertEquals("hello", tq.remainder());
    }

    @Test
    public void testConsumeWord() {
        TokenQueue tq = new TokenQueue("word123!@#");
        Assert.assertEquals("word123", tq.consumeWord());
        Assert.assertEquals("!@#", tq.remainder());
    }

    @Test
    public void testConsumeTagName() {
        TokenQueue tq = new TokenQueue("tag:name-1_2!other");
        Assert.assertEquals("tag:name-1_2", tq.consumeTagName());
        Assert.assertEquals("!other", tq.remainder());
    }

    @Test
    public void testConsumeElementSelector() {
        TokenQueue tq = new TokenQueue("ns|tag-1_2!other");
        Assert.assertEquals("ns|tag-1_2", tq.consumeElementSelector());
        Assert.assertEquals("!other", tq.remainder());
    }

    @Test
    public void testConsumeCssIdentifier() {
        TokenQueue tq = new TokenQueue("id-1_2!other");
        Assert.assertEquals("id-1_2", tq.consumeCssIdentifier());
        Assert.assertEquals("!other", tq.remainder());
    }

    @Test
    public void testConsumeAttributeKey() {
        TokenQueue tq = new TokenQueue("attr-1_2:name!other");
        Assert.assertEquals("attr-1_2:name", tq.consumeAttributeKey());
        Assert.assertEquals("!other", tq.remainder());
    }

    @Test
    public void testToString() {
        TokenQueue tq = new TokenQueue("test string");
        tq.consume("test ");
        Assert.assertEquals("string", tq.toString());
    }
}
