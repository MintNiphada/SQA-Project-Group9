package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CodeConsumerTest {

  private static class ConcreteCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    int lineCuts = 0;
    int endLines = 0;
    int preferredBreaks = 0;
    int newLines = 0;

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    void maybeCutLine() {
      lineCuts++;
    }

    @Override
    void endLine() {
      endLines++;
    }

    @Override
    void startNewLine() {
      newLines++;
      buffer.append("\n");
    }

    @Override
    void notePreferredLineBreak() {
      preferredBreaks++;
    }

    String getOutput() {
      return buffer.toString();
    }
  }

  private ConcreteCodeConsumer consumer;

  @Before
  public void setUp() {
    consumer = new ConcreteCodeConsumer();
  }

  @Test
  public void testDefaultMethods() {
    Assert.assertTrue(consumer.continueProcessing());
    Assert.assertFalse(consumer.shouldPreserveExtraBlocks());

    Node dummyNode = new Node(0);
    consumer.startSourceMapping(dummyNode);
    consumer.endSourceMapping(dummyNode);
    consumer.endFile();

    Assert.assertTrue(consumer.breakAfterBlockFor(dummyNode, true));
    Assert.assertFalse(consumer.breakAfterBlockFor(dummyNode, false));
  }

  @Test
  public void testAddIdentifier() {
    consumer.addIdentifier("foo");
    Assert.assertEquals("foo", consumer.getOutput());
    consumer.addIdentifier("bar");
    Assert.assertEquals("foo bar", consumer.getOutput());
  }

  @Test
  public void testEmptyAdd() {
    consumer.add("");
    Assert.assertEquals("", consumer.getOutput());
  }

  @Test
  public void testAddWordSpacing() {
    consumer.add("var");
    consumer.add("x");
    Assert.assertEquals("var x", consumer.getOutput());

    consumer.add(";");
    consumer.add("y");
    Assert.assertEquals("var x;y", consumer.getOutput());
  }

  @Test
  public void testAddEscapeSpacing() {
    consumer.add("a");
    consumer.add("\\u0061");
    Assert.assertEquals("a \\u0061", consumer.getOutput());
  }

  @Test
  public void testAddSlashSpacing() {
    consumer.add("/");
    consumer.add("/");
    Assert.assertEquals("/ /", consumer.getOutput());
  }

  @Test
  public void testAddOpPlusPlus() {
    consumer.add("x");
    consumer.addOp("+", false);
    consumer.addOp("++", false);
    consumer.add("y");
    Assert.assertEquals("x+ ++y", consumer.getOutput());
  }

  @Test
  public void testAddOpMinusMinus() {
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addOp("-", false);
    consumer.add("y");
    Assert.assertEquals("x- -y", consumer.getOutput());
  }

  @Test
  public void testAddOpLetterSpacing() {
    consumer.add("x");
    consumer.addOp("instanceof", false);
    consumer.add("y");
    Assert.assertEquals("x instanceof y", consumer.getOutput());
  }

  @Test
  public void testAddOpMinusGreaterThan() {
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addOp(">", false);
    consumer.add("y");
    Assert.assertEquals("x- >y", consumer.getOutput());
  }

  @Test
  public void testAddOpBinaryOpCutsLine() {
    int initialCuts = consumer.lineCuts;
    consumer.addOp("+", true);
    Assert.assertEquals(initialCuts + 1, consumer.lineCuts);

    consumer.addOp("*", false);
    Assert.assertEquals(initialCuts + 1, consumer.lineCuts);
  }

  @Test
  public void testAddNumberInteger() {
    consumer.addNumber(0);
    Assert.assertEquals("0", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.addNumber(42);
    Assert.assertEquals("42", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.addNumber(-42);
    Assert.assertEquals("-42", consumer.getOutput());
  }

  @Test
  public void testAddNumberScientificNotation() {
    consumer.addNumber(100);
    Assert.assertEquals("100", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.addNumber(1000);
    Assert.assertEquals("1E3", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.addNumber(1200000);
    Assert.assertEquals("12E5", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.addNumber(-1000);
    Assert.assertEquals("-1E3", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.addNumber(105);
    Assert.assertEquals("105", consumer.getOutput());
  }

  @Test
  public void testAddNumberDecimalAndNegativeZero() {
    consumer.addNumber(1.5);
    Assert.assertEquals("1.5", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.addNumber(-0.0);
    Assert.assertEquals("-0.0", consumer.getOutput());
    Assert.assertTrue(CodeConsumer.isNegativeZero(-0.0));
    Assert.assertFalse(CodeConsumer.isNegativeZero(0.0));
    Assert.assertFalse(CodeConsumer.isNegativeZero(1.0));
  }

  @Test
  public void testAddNumberNegativeAfterMinus() {
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addNumber(-4);
    Assert.assertEquals("x- -4", consumer.getOutput());
  }

  @Test
  public void testIsWordChar() {
    Assert.assertTrue(CodeConsumer.isWordChar('a'));
    Assert.assertTrue(CodeConsumer.isWordChar('Z'));
    Assert.assertTrue(CodeConsumer.isWordChar('0'));
    Assert.assertTrue(CodeConsumer.isWordChar('_'));
    Assert.assertTrue(CodeConsumer.isWordChar('$'));

    Assert.assertFalse(CodeConsumer.isWordChar('+'));
    Assert.assertFalse(CodeConsumer.isWordChar('-'));
    Assert.assertFalse(CodeConsumer.isWordChar(' '));
    Assert.assertFalse(CodeConsumer.isWordChar(';'));
    Assert.assertFalse(CodeConsumer.isWordChar('\0'));
  }

  @Test
  public void testBlocks() {
    consumer.beginBlock();
    consumer.add("foo");
    consumer.endBlock();
    Assert.assertEquals("{foo}", consumer.getOutput());

    consumer = new ConcreteCodeConsumer();
    consumer.statementStarted = true;
    consumer.endStatement();
    consumer.beginBlock();
    consumer.endBlock(true);
    Assert.assertEquals(";{}", consumer.getOutput());
    Assert.assertEquals(2, consumer.endLines);
  }

  @Test
  public void testStatements() {
    consumer.add("x");
    consumer.endStatement();
    Assert.assertTrue(consumer.statementNeedsEnded);

    consumer.add("y");
    Assert.assertEquals("x;y", consumer.getOutput());

    consumer.endStatement(true);
    Assert.assertEquals("x;y;", consumer.getOutput());
    Assert.assertFalse(consumer.statementNeedsEnded);
  }

  @Test
  public void testFunctionAndCaseBody() {
    consumer.endFunction();
    Assert.assertTrue(consumer.sawFunction);
    Assert.assertEquals(0, consumer.endLines);

    consumer.endFunction(true);
    Assert.assertEquals(1, consumer.endLines);

    consumer.beginCaseBody();
    consumer.endCaseBody();
    Assert.assertEquals(":", consumer.getOutput());
  }

  @Test
  public void testListSeparator() {
    consumer.add("a");
    consumer.listSeparator();
    consumer.add("b");
    Assert.assertEquals("a,b", consumer.getOutput());
    Assert.assertEquals(1, consumer.lineCuts);
  }

  @Test
  public void testBaseClassNoOpMethods() {
    CodeConsumer minimalConsumer = new CodeConsumer() {
      private char last = '\0';
      @Override
      char getLastChar() {
        return last;
      }
      @Override
      void append(String str) {
        if (str.length() > 0) {
          last = str.charAt(str.length() - 1);
        }
      }
    };

    minimalConsumer.startNewLine();
    minimalConsumer.maybeLineBreak();
    minimalConsumer.maybeCutLine();
    minimalConsumer.endLine();
    minimalConsumer.notePreferredLineBreak();
    minimalConsumer.endCaseBody();
    minimalConsumer.endFile();
    Assert.assertEquals('\0', minimalConsumer.getLastChar());
  }
}