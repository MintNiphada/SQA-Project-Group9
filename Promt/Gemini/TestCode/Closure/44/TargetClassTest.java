package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CodeConsumerTest {

  private static class ConcreteCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    int cutLineCount = 0;
    int endLineCount = 0;
    int startNewLineCount = 0;
    int notePreferredLineBreakCount = 0;

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

    String getOutput() {
      return buffer.toString();
    }

    void clear() {
      buffer.setLength(0);
      cutLineCount = 0;
      endLineCount = 0;
      startNewLineCount = 0;
      notePreferredLineBreakCount = 0;
      statementNeedsEnded = false;
      statementStarted = false;
      sawFunction = false;
    }
  }

  private ConcreteCodeConsumer consumer;

  @Before
  public void setUp() {
    consumer = new ConcreteCodeConsumer();
  }

  @Test
  public void testDefaultMethodsAndFlags() {
    assertTrue(consumer.continueProcessing());
    assertFalse(consumer.shouldPreserveExtraBlocks());

    Node dummyNode = new Node(0);
    consumer.startSourceMapping(dummyNode);
    consumer.endSourceMapping(dummyNode);
    consumer.endFile();
    consumer.startNewLine();
    assertEquals(1, consumer.startNewLineCount);
    consumer.notePreferredLineBreak();
    assertEquals(1, consumer.notePreferredLineBreakCount);
    consumer.maybeLineBreak();
    assertEquals(1, consumer.cutLineCount);
    consumer.endCaseBody();

    assertTrue(consumer.breakAfterBlockFor(dummyNode, true));
    assertFalse(consumer.breakAfterBlockFor(dummyNode, false));
  }

  @Test
  public void testIsNegativeZero() {
    assertTrue(CodeConsumer.isNegativeZero(-0.0));
    assertFalse(CodeConsumer.isNegativeZero(0.0));
    assertFalse(CodeConsumer.isNegativeZero(-1.0));
    assertFalse(CodeConsumer.isNegativeZero(1.0));
    assertFalse(CodeConsumer.isNegativeZero(Double.NaN));
    assertFalse(CodeConsumer.isNegativeZero(Double.NEGATIVE_INFINITY));
    assertFalse(CodeConsumer.isNegativeZero(Double.POSITIVE_INFINITY));
  }

  @Test
  public void testIsWordChar() {
    assertTrue(CodeConsumer.isWordChar('a'));
    assertTrue(CodeConsumer.isWordChar('Z'));
    assertTrue(CodeConsumer.isWordChar('0'));
    assertTrue(CodeConsumer.isWordChar('9'));
    assertTrue(CodeConsumer.isWordChar('_'));
    assertTrue(CodeConsumer.isWordChar('$'));

    assertFalse(CodeConsumer.isWordChar(' '));
    assertFalse(CodeConsumer.isWordChar('-'));
    assertFalse(CodeConsumer.isWordChar('+'));
    assertFalse(CodeConsumer.isWordChar('{'));
    assertFalse(CodeConsumer.isWordChar(';'));
    assertFalse(CodeConsumer.isWordChar('\\'));
    assertFalse(CodeConsumer.isWordChar('\0'));
  }

  @Test
  public void testAddAndWordCharSeparation() {
    consumer.add("");
    assertEquals("", consumer.getOutput());

    consumer.add("var");
    assertEquals("var", consumer.getOutput());

    consumer.add("x");
    assertEquals("var x", consumer.getOutput());

    consumer.clear();
    consumer.add("a");
    consumer.add("\\u0041");
    assertEquals("a \\u0041", consumer.getOutput());

    consumer.clear();
    consumer.add("a");
    consumer.add(";");
    assertEquals("a;", consumer.getOutput());
  }

  @Test
  public void testAddIdentifier() {
    consumer.addIdentifier("myVar");
    assertEquals("myVar", consumer.getOutput());
  }

  @Test
  public void testAppendBlockStartAndEnd() {
    consumer.appendBlockStart();
    consumer.appendBlockEnd();
    assertEquals("{}", consumer.getOutput());
  }

  @Test
  public void testBeginBlockAndEndBlock() {
    consumer.beginBlock();
    assertEquals("{", consumer.getOutput());
    assertEquals(1, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);

    consumer.clear();
    consumer.statementNeedsEnded = true;
    consumer.beginBlock();
    assertEquals(";{", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
    assertEquals(1, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);

    consumer.clear();
    consumer.endBlock();
    assertEquals("}", consumer.getOutput());
    assertEquals(0, consumer.endLineCount);

    consumer.clear();
    consumer.statementNeedsEnded = true;
    consumer.endBlock(true);
    assertEquals("}", consumer.getOutput());
    assertEquals(1, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);

    consumer.clear();
    consumer.endBlock(false);
    assertEquals("}", consumer.getOutput());
    assertEquals(0, consumer.endLineCount);
  }

  @Test
  public void testListSeparator() {
    consumer.listSeparator();
    assertEquals(",", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
  }

  @Test
  public void testEndStatement() {
    consumer.statementStarted = true;
    consumer.endStatement();
    assertTrue(consumer.statementNeedsEnded);

    consumer.clear();
    consumer.statementStarted = false;
    consumer.endStatement();
    assertFalse(consumer.statementNeedsEnded);

    consumer.clear();
    consumer.statementNeedsEnded = true;
    consumer.endStatement(true);
    assertEquals(";", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
    assertFalse(consumer.statementNeedsEnded);

    consumer.clear();
    consumer.statementStarted = true;
    consumer.endStatement(false);
    assertTrue(consumer.statementNeedsEnded);
    assertEquals("", consumer.getOutput());
  }

  @Test
  public void testMaybeEndStatement() {
    consumer.statementNeedsEnded = true;
    consumer.maybeEndStatement();
    assertEquals(";", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
    assertEquals(1, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);
    assertTrue(consumer.statementStarted);

    consumer.clear();
    consumer.statementNeedsEnded = false;
    consumer.maybeEndStatement();
    assertEquals("", consumer.getOutput());
    assertEquals(0, consumer.cutLineCount);
    assertEquals(0, consumer.endLineCount);
    assertTrue(consumer.statementStarted);
  }

  @Test
  public void testEndFunction() {
    consumer.endFunction();
    assertTrue(consumer.sawFunction);
    assertEquals(0, consumer.endLineCount);

    consumer.clear();
    consumer.endFunction(false);
    assertTrue(consumer.sawFunction);
    assertEquals(0, consumer.endLineCount);

    consumer.clear();
    consumer.endFunction(true);
    assertTrue(consumer.sawFunction);
    assertEquals(1, consumer.endLineCount);
  }

  @Test
  public void testBeginCaseBody() {
    consumer.beginCaseBody();
    assertEquals(":", consumer.getOutput());
  }

  @Test
  public void testAddOp() {
    consumer.add("x");
    consumer.addOp("++", false);
    assertEquals("x++", consumer.getOutput());
    assertEquals(0, consumer.cutLineCount);

    consumer.addOp("+", false);
    assertEquals("x++ +", consumer.getOutput());

    consumer.clear();
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addOp("-", false);
    assertEquals("x- -", consumer.getOutput());

    consumer.clear();
    consumer.add("x");
    consumer.addOp("instanceof", true);
    assertEquals("x instanceof", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);

    consumer.clear();
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addOp(">", false);
    assertEquals("x- >", consumer.getOutput());

    consumer.clear();
    consumer.statementNeedsEnded = true;
    consumer.addOp("+", true);
    assertEquals(";+", consumer.getOutput());
    assertEquals(2, consumer.cutLineCount);
  }

  @Test
  public void testAddNumber() {
    consumer.addNumber(0.0);
    assertEquals("0", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(-0.0);
    assertEquals("-0.0", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(5.0);
    assertEquals("5", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(-5.0);
    assertEquals("-5", consumer.getOutput());

    consumer.clear();
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addNumber(-4.0);
    assertEquals("x- -4", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(100.0);
    assertEquals("100", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(1000.0);
    assertEquals("1E3", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(12000.0);
    assertEquals("12E3", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(1000000.0);
    assertEquals("1E6", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(-1000.0);
    assertEquals("-1E3", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(1234.0);
    assertEquals("1234", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(1.5);
    assertEquals("1.5", consumer.getOutput());

    consumer.clear();
    consumer.addNumber(-1.5);
    assertEquals("-1.5", consumer.getOutput());

    consumer.clear();
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addNumber(-1.5);
    assertEquals("x- -1.5", consumer.getOutput());
  }

  @Test
  public void testAddOpAppendOpOverride() {
    final StringBuilder opBuffer = new StringBuilder();
    CodeConsumer customOpConsumer = new ConcreteCodeConsumer() {
      @Override
      void appendOp(String op, boolean binOp) {
        opBuffer.append("[").append(op).append(":").append(binOp).append("]");
      }
    };
    customOpConsumer.addOp("+", true);
    assertEquals("[+:true]", opBuffer.toString());
  }
}