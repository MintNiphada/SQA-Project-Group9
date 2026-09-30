package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

class CodeConsumerTest {

  private static class ConcreteCodeConsumer extends CodeConsumer {
    final StringBuilder buffer = new StringBuilder();
    int cutLineCount = 0;
    int endLineCount = 0;
    int startNewLineCount = 0;
    int notePreferredLineBreakCount = 0;
    int startSourceMappingCount = 0;
    int endSourceMappingCount = 0;
    int endFileCount = 0;

    @Override
    char getLastChar() {
      if (buffer.length() == 0) {
        return '\0';
      }
      return buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    void maybeCutLine() {
      cutLineCount++;
    }

    @Override
    void endLine() {
      endLineCount++;
    }

    @Override
    void startNewLine() {
      startNewLineCount++;
    }

    @Override
    void notePreferredLineBreak() {
      notePreferredLineBreakCount++;
    }

    @Override
    void startSourceMapping(Node node) {
      startSourceMappingCount++;
    }

    @Override
    void endSourceMapping(Node node) {
      endSourceMappingCount++;
    }

    @Override
    void endFile() {
      endFileCount++;
    }
  }

  private ConcreteCodeConsumer consumer;

  @Before
  public void setUp() {
    consumer = new ConcreteCodeConsumer();
  }

  @Test
  public void testInitialStateAndDefaults() {
    Assert.assertTrue(consumer.continueProcessing());
    Assert.assertFalse(consumer.shouldPreserveExtraBlocks());
    Assert.assertEquals('\0', consumer.getLastChar());
    Assert.assertFalse(consumer.statementNeedsEnded);
    Assert.assertFalse(consumer.statementStarted);
    Assert.assertFalse(consumer.sawFunction);

    Node dummyNode = new Node(0);
    Assert.assertTrue(consumer.breakAfterBlockFor(dummyNode, true));
    Assert.assertFalse(consumer.breakAfterBlockFor(dummyNode, false));

    consumer.startSourceMapping(dummyNode);
    Assert.assertEquals(1, consumer.startSourceMappingCount);

    consumer.endSourceMapping(dummyNode);
    Assert.assertEquals(1, consumer.endSourceMappingCount);

    consumer.startNewLine();
    Assert.assertEquals(1, consumer.startNewLineCount);

    consumer.maybeLineBreak();
    Assert.assertEquals(1, consumer.cutLineCount);

    consumer.notePreferredLineBreak();
    Assert.assertEquals(1, consumer.notePreferredLineBreakCount);

    consumer.endFile();
    Assert.assertEquals(1, consumer.endFileCount);

    consumer.endCaseBody();
  }

  @Test
  public void testIsWordChar() {
    Assert.assertTrue(CodeConsumer.isWordChar('_'));
    Assert.assertTrue(CodeConsumer.isWordChar('$'));
    Assert.assertTrue(CodeConsumer.isWordChar('a'));
    Assert.assertTrue(CodeConsumer.isWordChar('Z'));
    Assert.assertTrue(CodeConsumer.isWordChar('0'));
    Assert.assertTrue(CodeConsumer.isWordChar('9'));

    Assert.assertFalse(CodeConsumer.isWordChar(' '));
    Assert.assertFalse(CodeConsumer.isWordChar('+'));
    Assert.assertFalse(CodeConsumer.isWordChar('-'));
    Assert.assertFalse(CodeConsumer.isWordChar('\\'));
    Assert.assertFalse(CodeConsumer.isWordChar('@'));
    Assert.assertFalse(CodeConsumer.isWordChar('\0'));
  }

  @Test
  public void testAddAndAddIdentifier() {
    consumer.add("var");
    Assert.assertEquals("var", consumer.buffer.toString());
    Assert.assertTrue(consumer.statementStarted);

    // Adding word char after word char should insert a space
    consumer.addIdentifier("foo");
    Assert.assertEquals("var foo", consumer.buffer.toString());

    // Adding empty string should do nothing
    consumer.add("");
    Assert.assertEquals("var foo", consumer.buffer.toString());

    // Adding unicode escape sequence starting with backslash after word char
    consumer.add("\\u0041");
    Assert.assertEquals("var foo \\u0041", consumer.buffer.toString());
  }

  @Test
  public void testBeginAndEndBlock() {
    consumer.add("if(true)");
    consumer.statementNeedsEnded = true;

    consumer.beginBlock();
    Assert.assertEquals("if(true);{", consumer.buffer.toString());
    Assert.assertFalse(consumer.statementNeedsEnded);
    Assert.assertEquals(1, consumer.cutLineCount);
    Assert.assertEquals(1, consumer.endLineCount);

    consumer.endBlock();
    Assert.assertEquals("if(true);{}", consumer.buffer.toString());
    Assert.assertEquals(1, consumer.endLineCount);

    consumer.endBlock(true);
    Assert.assertEquals("if(true){}}", consumer.buffer.toString());
    Assert.assertEquals(2, consumer.endLineCount);
  }

  @Test
  public void testListSeparator() {
    consumer.add("a");
    consumer.listSeparator();
    Assert.assertEquals("a,", consumer.buffer.toString());
    Assert.assertEquals(1, consumer.cutLineCount);
  }

  @Test
  public void testEndStatementAndMaybeEndStatement() {
    // endStatement when statement has not started
    consumer.endStatement();
    Assert.assertFalse(consumer.statementNeedsEnded);

    // start statement and end statement without semicolon needed
    consumer.add("x");
    consumer.endStatement();
    Assert.assertTrue(consumer.statementNeedsEnded);

    // next statement starts, triggers semicolon insertion
    consumer.add("y");
    Assert.assertEquals("x;y", consumer.buffer.toString());
    Assert.assertFalse(consumer.statementNeedsEnded);
    Assert.assertEquals(1, consumer.cutLineCount);
    Assert.assertEquals(1, consumer.endLineCount);

    // endStatement with needSemiColon = true
    consumer.endStatement(true);
    Assert.assertEquals("x;y;", consumer.buffer.toString());
    Assert.assertEquals(2, consumer.cutLineCount);
    Assert.assertFalse(consumer.statementNeedsEnded);
  }

  @Test
  public void testEndFunction() {
    consumer.endFunction();
    Assert.assertTrue(consumer.sawFunction);
    Assert.assertEquals(0, consumer.endLineCount);

    consumer.endFunction(true);
    Assert.assertTrue(consumer.sawFunction);
    Assert.assertEquals(1, consumer.endLineCount);
  }

  @Test
  public void testBeginCaseBody() {
    consumer.beginCaseBody();
    Assert.assertEquals(":", consumer.buffer.toString());
  }

  @Test
  public void testAddOpSpacing() {
    // '+' after '+'
    consumer.add("+");
    consumer.addOp("+", false);
    Assert.assertEquals("+ +", consumer.buffer.toString());

    // '-' after '-'
    consumer.buffer.setLength(0);
    consumer.add("-");
    consumer.addOp("-", false);
    Assert.assertEquals("- -", consumer.buffer.toString());

    // '>' after '-' (to avoid -->)
    consumer.buffer.setLength(0);
    consumer.add("-");
    consumer.addOp(">", false);
    Assert.assertEquals("- >", consumer.buffer.toString());

    // Word operator like 'instanceof' after word char
    consumer.buffer.setLength(0);
    consumer.add("x");
    consumer.addOp("instanceof", true);
    Assert.assertEquals("x instanceof", consumer.buffer.toString());
    Assert.assertEquals(1, consumer.cutLineCount);

    // Binary operator triggers maybeCutLine
    consumer.buffer.setLength(0);
    consumer.cutLineCount = 0;
    consumer.add("x");
    consumer.addOp("*", true);
    Assert.assertEquals("x*", consumer.buffer.toString());
    Assert.assertEquals(1, consumer.cutLineCount);
  }

  @Test
  public void testAddNumberFormatting() {
    // Positive integers and exponent formatting
    consumer.addNumber(0);
    Assert.assertEquals("0", consumer.buffer.toString());

    consumer.buffer.setLength(0);
    consumer.addNumber(100);
    Assert.assertEquals("100", consumer.buffer.toString());

    consumer.buffer.setLength(0);
    consumer.addNumber(1000);
    Assert.assertEquals("1E3", consumer.buffer.toString());

    consumer.buffer.setLength(0);
    consumer.addNumber(10500);
    Assert.assertEquals("10500", consumer.buffer.toString());

    consumer.buffer.setLength(0);
    consumer.addNumber(100000);
    Assert.assertEquals("1E5", consumer.buffer.toString());

    // Negative numbers
    consumer.buffer.setLength(0);
    consumer.addNumber(-1000);
    Assert.assertEquals("-1E3", consumer.buffer.toString());

    // Negative number after '-' operator should have a separating space
    consumer.buffer.setLength(0);
    consumer.add("-");
    consumer.addNumber(-5);
    Assert.assertEquals("- -5", consumer.buffer.toString());

    // Non-integer double values
    consumer.buffer.setLength(0);
    consumer.addNumber(1.25);
    Assert.assertEquals("1.25", consumer.buffer.toString());

    consumer.buffer.setLength(0);
    consumer.addNumber(-0.5);
    Assert.assertEquals("-0.5", consumer.buffer.toString());
  }
}