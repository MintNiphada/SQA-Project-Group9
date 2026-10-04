package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenQueueTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {
        new TokenQueue(null);
    }

    @Test
    public void testIsEmptyAndRemainingLength() {
        TokenQueue tq = new TokenQueue("");
        assertTrue(tq.isEmpty());
        assertEquals("", tq.toString());

        TokenQueue tq2 = new TokenQueue("abc");
        assertFalse(tq2.isEmpty());
        tq2.consume();
        tq2.consume();
        tq2.consume();
        assertTrue(tq2.isEmpty());
    }

    @Test
    public void testPeek() {
        TokenQueue tq = new TokenQueue("");
        assertEquals(0, tq.peek());

        TokenQueue tq2 = new TokenQueue("test");
        assertEquals('t', tq2.peek());
        tq2.advance();
        assertEquals('e', tq2.peek());
    }

    @Test
    public void testAddFirst() {
        TokenQueue tq = new TokenQueue("world");
        tq.addFirst("hello ");
        assertEquals("hello world", tq.remainder());

        tq = new TokenQueue("bc");
        tq.addFirst(Character.valueOf('a'));
        assertEquals("abc", tq.remainder());

        tq = new TokenQueue("12345");
        tq.consume();
        tq.consume();
        tq.addFirst("99");
        assertEquals("99345", tq.remainder());
    }

    @Test
    public void testMatchesAndMatchesCS() {
        TokenQueue tq = new TokenQueue("HelloWorld");
        assertTrue(tq.matches("HELLO"));
        assertTrue(tq.matches("hello"));
        assertTrue(tq.matchesCS("Hello"));
        assertFalse(tq.matchesCS("hello"));
        assertFalse(tq.matches("other"));
    }

    @Test
    public void testMatchesAnyString() {
        TokenQueue tq = new TokenQueue("One Two Three");
        assertTrue(tq.matchesAny("foo", "one", "bar"));
        assertFalse(tq.matchesAny("foo", "bar"));
        tq.consumeTo("Two");
        assertTrue(tq.matchesAny("two", "three"));
        assertFalse(tq.matchesAny("One"));
    }

    @Test
    public void testMatchesAnyChar() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny('x', 'a', 'z'));
        assertFalse(tq.matchesAny('x', 'y', 'z'));
        
        TokenQueue empty = new TokenQueue("");
        assertFalse(empty.matchesAny('a', 'b'));
    }

    @Test
    public void testMatchesStartTag() {
        TokenQueue tq1 = new TokenQueue("<div>");
        assertTrue(tq1.matchesStartTag());

        TokenQueue tq2 = new TokenQueue("<DIV>");
        assertTrue(tq2.matchesStartTag());

        TokenQueue tq3 = new TokenQueue("<123>");
        assertFalse(tq3.matchesStartTag());

        TokenQueue tq4 = new TokenQueue("div");
        assertFalse(tq4.matchesStartTag());

        TokenQueue tq5 = new TokenQueue("<");
        assertFalse(tq5.matchesStartTag());

        TokenQueue tq6 = new TokenQueue("");
        assertFalse(tq6.matchesStartTag());
    }

    @Test
    public void testMatchChomp() {
        TokenQueue tq = new TokenQueue("One Two Three");
        assertTrue(tq.matchChomp("one"));
        assertEquals(" Two Three", tq.remainder());

        TokenQueue tq2 = new TokenQueue("One Two Three");
        assertFalse(tq2.matchChomp("two"));
        assertEquals("One Two Three", tq2.remainder());
    }

    @Test
    public void testMatchesWhitespaceAndWord() {
        TokenQueue tq = new TokenQueue(" \t\n\rword123!");
        assertTrue(tq.matchesWhitespace());
        assertFalse(tq.matchesWord());
        tq.consumeWhitespace();

        assertTrue(tq.matchesWord());
        assertFalse(tq.matchesWhitespace());

        TokenQueue empty = new TokenQueue("");
        assertFalse(empty.matchesWhitespace());
        assertFalse(empty.matchesWord());
    }

    @Test
    public void testAdvanceAndConsume() {
        TokenQueue tq = new TokenQueue("abc");
        tq.advance();
        assertEquals('b', tq.consume());
        assertEquals('c', tq.consume());

        TokenQueue empty = new TokenQueue("");
        empty.advance();
        assertTrue(empty.isEmpty());
    }

    @Test
    public void testConsumeSequence() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("HELLO");
        assertEquals(" world", tq.remainder());
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeSequenceMismatch() {
        TokenQueue tq = new TokenQueue("hello world");
        tq.consume("world");
    }

    @Test
    public void testConsumeTo() {
        TokenQueue tq = new TokenQueue("One Two Three");
        assertEquals("One ", tq.consumeTo("Two"));
        assertEquals("Two Three", tq.toString());

        assertEquals("Two Three", tq.consumeTo("Four"));
        assertTrue(tq.isEmpty());
    }

    @Test
    public void testConsumeToIgnoreCase() {
        TokenQueue tq1 = new TokenQueue("One TWO Three");
        assertEquals("One ", tq1.consumeToIgnoreCase("two"));
        assertEquals("TWO Three", tq1.toString());

        TokenQueue tq2 = new TokenQueue("abc 123 def");
        assertEquals("abc ", tq2.consumeToIgnoreCase("123"));
        assertEquals("123 def", tq2.remainder());

        TokenQueue tq3 = new TokenQueue("abc def");
        assertEquals("abc def", tq3.consumeToIgnoreCase("xyz"));
        assertTrue(tq3.isEmpty());

        TokenQueue tq4 = new TokenQueue("112233");
        assertEquals("1", tq4.consumeToIgnoreCase("12"));
        assertEquals("12233", tq4.remainder());

        TokenQueue tq5 = new TokenQueue("12345");
        assertEquals("12345", tq5.consumeToIgnoreCase("99"));
        assertTrue(tq5.isEmpty());
    }

    @Test
    public void testConsumeToAny() {
        TokenQueue tq = new TokenQueue("foo * bar + baz");
        assertEquals("foo ", tq.consumeToAny("*", "+"));
        assertEquals("* bar + baz", tq.remainder());

        TokenQueue tq2 = new TokenQueue("no match here");
        assertEquals("no match here", tq2.consumeToAny("x", "y"));
        assertTrue(tq2.isEmpty());
    }

    @Test
    public void testChompTo() {
        TokenQueue tq = new TokenQueue("One Two Three");
        assertEquals("One ", tq.chompTo("Two"));
        assertEquals(" Three", tq.remainder());

        TokenQueue tq2 = new TokenQueue("One Two Three");
        assertEquals("One Two Three", tq2.chompTo("NotFound"));
        assertTrue(tq2.isEmpty());
    }

    @Test
    public void testChompToIgnoreCase() {
        TokenQueue tq = new TokenQueue("One TWO Three");
        assertEquals("One ", tq.chompToIgnoreCase("two"));
        assertEquals(" Three", tq.remainder());
    }

    @Test
    public void testChompBalanced() {
        TokenQueue tq1 = new TokenQueue("(one (two) three) four");
        assertEquals("one (two) three", tq1.chompBalanced('(', ')'));
        assertEquals(" four", tq1.remainder());

        TokenQueue tq2 = new TokenQueue("('one)' (two)) three");
        assertEquals("'one)' (two)", tq2.chompBalanced('(', ')'));

        TokenQueue tq3 = new TokenQueue("(\"one)\" (two)) three");
        assertEquals("\"one)\" (two)", tq3.chompBalanced('(', ')'));

        TokenQueue tq4 = new TokenQueue("(\\(one\\) two) three");
        assertEquals("\\(one\\) two", tq4.chompBalanced('(', ')'));

        TokenQueue tq5 = new TokenQueue("(unbalanced");
        assertEquals("", tq5.chompBalanced('(', ')'));

        TokenQueue empty = new TokenQueue("");
        assertEquals("", empty.chompBalanced('(', ')'));
    }

    @Test
    public void testUnescape() {
        assertEquals("hello world", TokenQueue.unescape("hello world"));
        assertEquals("one\\two", TokenQueue.unescape("one\\\\two"));
        assertEquals("quote'double\"", TokenQueue.unescape("quote\\'double\\\""));
        assertEquals("", TokenQueue.unescape(""));
    }

    @Test
    public void testConsumeWhitespace() {
        TokenQueue tq = new TokenQueue("   \t\nword");
        assertTrue(tq.consumeWhitespace());
        assertEquals("word", tq.remainder());

        TokenQueue tq2 = new TokenQueue("word");
        assertFalse(tq2.consumeWhitespace());
        assertEquals("word", tq2.remainder());
    }

    @Test
    public void testConsumeWord() {
        TokenQueue tq = new TokenQueue("word123 456");
        assertEquals("word123", tq.consumeWord());
        assertEquals(" 456", tq.remainder());

        TokenQueue tq2 = new TokenQueue("!@#");
        assertEquals("", tq2.consumeWord());
    }

    @Test
    public void testConsumeTagName() {
        TokenQueue tq = new TokenQueue("tag_name:sub-1 > div");
        assertEquals("tag_name:sub-1", tq.consumeTagName());
        assertEquals(" > div", tq.remainder());
    }

    @Test
    public void testConsumeElementSelector() {
        TokenQueue tq1 = new TokenQueue("ns|tag.class");
        assertEquals("ns|tag", tq1.consumeElementSelector());
        assertEquals(".class", tq1.remainder());

        TokenQueue tq2 = new TokenQueue("*|tag.class");
        assertEquals("*|tag", tq2.consumeElementSelector());
        assertEquals(".class", tq2.remainder());

        TokenQueue tq3 = new TokenQueue("div_tag-sub");
        assertEquals("div_tag-sub", tq3.consumeElementSelector());
    }

    @Test
    public void testConsumeCssIdentifier() {
        TokenQueue tq = new TokenQueue("my-class_name123.other");
        assertEquals("my-class_name123", tq.consumeCssIdentifier());
        assertEquals(".other", tq.remainder());
    }

    @Test
    public void testConsumeAttributeKey() {
        TokenQueue tq = new TokenQueue("xml:lang_attr-1='val'");
        assertEquals("xml:lang_attr-1", tq.consumeAttributeKey());
        assertEquals("='val'", tq.remainder());
    }

    @Test
    public void testRemainderAndToString() {
        TokenQueue tq = new TokenQueue("sample text");
        assertEquals("sample text", tq.toString());
        tq.consumeWord();
        assertEquals(" text", tq.toString());
        assertEquals(" text", tq.remainder());
        assertTrue(tq.isEmpty());
        assertEquals("", tq.remainder());
        assertEquals("", tq.toString());
    }
}
