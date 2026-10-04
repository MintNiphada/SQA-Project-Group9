package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CodeGeneratorTest {

  private static class MockCodeConsumer extends CodeConsumer {
    final StringBuilder buffer = new StringBuilder();
    boolean continueProcessing = true;
    boolean preserveExtraBlocks = false;

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    void appendOp(String op, boolean binOp) {
      buffer.append(op);
    }

    @Override
    void addIdentifier(String identifier) {
      buffer.append(identifier);
    }

    @Override
    void addNumber(double x) {
      long l = (long) x;
      if (l == x) {
        buffer.append(l);
      } else {
        buffer.append(x);
      }
    }

    @Override
    void endStatement(boolean needSemicolon) {
      if (needSemicolon) {
        buffer.append(";");
      }
    }

    @Override
    void endStatement() {
      buffer.append(";");
    }

    @Override
    void startSourceMapping(Node node) {}

    @Override
    void endSourceMapping(Node node) {}

    @Override
    void endFunction(boolean isStatement) {
      if (isStatement) {
        buffer.append(";");
      }
    }

    @Override
    void beginBlock() {
      buffer.append("{");
    }

    @Override
    void endBlock(boolean breakAfter) {
      buffer.append("}");
    }

    @Override
    void listSeparator() {
      buffer.append(",");
    }

    @Override
    void beginCaseBody() {
      buffer.append(":");
    }

    @Override
    void endCaseBody() {
      buffer.append(";");
    }

    @Override
    void maybeLineBreak() {}

    @Override
    void notePreferredLineBreak() {}

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    @Override
    boolean breakAfterBlockFor(Node n, boolean isStatement) {
      return false;
    }

    String getOutput() {
      return buffer.toString();
    }
  }

  private MockCodeConsumer consumer;
  private CodeGenerator generator;

  @Before
  public void setUp() {
    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
  }

  @Test
  public void testConstructors() {
    CodeGenerator cg1 = new CodeGenerator(consumer);
    Assert.assertNotNull(cg1);
    CodeGenerator cg2 = new CodeGenerator(consumer, Charsets.US_ASCII);
    Assert.assertNotNull(cg2);
    CodeGenerator cg3 = new CodeGenerator(consumer, Charsets.UTF_8);
    Assert.assertNotNull(cg3);
    CodeGenerator cg4 = new CodeGenerator(consumer, null);
    Assert.assertNotNull(cg4);
  }

  @Test
  public void testTagAsStrict() {
    generator.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer.getOutput());
  }

  @Test
  public void testJsStringEscaping() {
    Assert.assertEquals("\"hello\"", CodeGenerator.jsString("hello", null));
    Assert.assertEquals("\"\\\"quotes\\\"\"", CodeGenerator.jsString("\"quotes\"", null));
    Assert.assertEquals("'\"double\" and \\'single\\''", CodeGenerator.jsString("\"double\" and 'single'", null));
    Assert.assertEquals("\"\\0\\n\\r\\t\\\\\"", CodeGenerator.jsString("\0\n\r\t\\", null));
    Assert.assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));
    Assert.assertEquals("\"]]\\>\"", CodeGenerator.jsString("]]>", null));
    Assert.assertEquals("\"<\\/script>\"", CodeGenerator.jsString("</script>", null));
    Assert.assertEquals("\"<\\!--\"", CodeGenerator.jsString("<!--", null));
    Assert.assertEquals("\"<foo>\"", CodeGenerator.jsString("<foo>", null));
    Assert.assertEquals("\"foo>bar\"", CodeGenerator.jsString("foo>bar", null));
  }

  @Test
  public void testJsStringWithEncoder() {
    CharsetEncoder encoder = Charset.forName("ISO-8859-1").newEncoder();
    String result = CodeGenerator.jsString("hello \u0100 world", encoder);
    Assert.assertTrue(result.contains("\\u0100"));

    String asciiResult = CodeGenerator.jsString("hello \u00e9", encoder);
    Assert.assertTrue(asciiResult.contains("\u00e9"));
  }

  @Test
  public void testRegexpEscape() {
    Assert.assertEquals("/foo\\//", CodeGenerator.regexpEscape("foo/"));
    Assert.assertEquals("/<\\/script>/", CodeGenerator.regexpEscape("</script>"));
    Assert.assertEquals("/<\\!--/", CodeGenerator.regexpEscape("<!--"));
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    Assert.assertEquals("\"hello \\\"world\\\"\"", CodeGenerator.escapeToDoubleQuotedJsString("hello \"world\""));
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("foo_bar$123", CodeGenerator.identifierEscape("foo_bar$123"));
    String escaped = CodeGenerator.identifierEscape("var_\u03a9");
    Assert.assertTrue(escaped.contains("\\u03a9"));
  }

  @Test
  public void testAddInterrupted() {
    consumer.continueProcessing = false;
    generator.add(Node.newString(Token.NAME, "a"));
    Assert.assertEquals("", consumer.getOutput());
  }

  @Test
  public void testBinaryOperators() {
    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");
    Node add = new Node(Token.ADD, a, b);
    generator.add(add);
    Assert.assertEquals("a+b", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node c = Node.newString(Token.NAME, "c");
    Node mul = new Node(Token.MUL, a, new Node(Token.MUL, b, c));
    generator.add(mul);
    Assert.assertEquals("a*b*c", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node assign = new Node(Token.ASSIGN, a, new Node(Token.ASSIGN, b, c));
    generator.add(assign);
    Assert.assertEquals("a=b=c", consumer.getOutput());
  }

  @Test(expected = IllegalStateException.class)
  public void testBinaryOperatorBadChildCount() {
    Node a = Node.newString(Token.NAME, "a");
    Node add = new Node(Token.ADD, a);
    generator.add(add);
  }

  @Test
  public void testNullThisTrueFalse() {
    generator.add(new Node(Token.NULL));
    generator.add(new Node(Token.THIS));
    generator.add(new Node(Token.TRUE));
    generator.add(new Node(Token.FALSE));
    Assert.assertEquals("nullthistruefalse", consumer.getOutput());
  }

  @Test
  public void testNumberAndNeg() {
    Node num = Node.newNumber(42.0);
    generator.add(num);
    Assert.assertEquals("42", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node negNum = new Node(Token.NEG, Node.newNumber(5.0));
    generator.add(negNum);
    Assert.assertEquals("-5", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    generator.add(negVar);
    Assert.assertEquals("-x", consumer.getOutput());
  }

  @Test
  public void testUnaryOps() {
    Node x = Node.newString(Token.NAME, "x");
    generator.add(new Node(Token.NOT, x));
    generator.add(new Node(Token.TYPEOF, x.cloneNode()));
    generator.add(new Node(Token.VOID, x.cloneNode()));
    generator.add(new Node(Token.BITNOT, x.cloneNode()));
    generator.add(new Node(Token.POS, x.cloneNode()));
    Assert.assertEquals("!xtypeof xvoid ~x+x", consumer.getOutput());
  }

  @Test
  public void testIncDec() {
    Node x = Node.newString(Token.NAME, "x");
    Node preInc = new Node(Token.INC, x);
    generator.add(preInc);
    Assert.assertEquals("++x", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node postInc = new Node(Token.INC, x.cloneNode());
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postInc);
    Assert.assertEquals("x++", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node postDec = new Node(Token.DEC, x.cloneNode());
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postDec);
    Assert.assertEquals("x--", consumer.getOutput());
  }

  @Test
  public void testHook() {
    Node cond = Node.newString(Token.NAME, "c");
    Node t = Node.newNumber(1.0);
    Node e = Node.newNumber(2.0);
    Node hook = new Node(Token.HOOK, cond, t, e);
    generator.add(hook);
    Assert.assertEquals("c?1:2", consumer.getOutput());
  }

  @Test
  public void testArrayLit() {
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1.0), new Node(Token.EMPTY), Node.newNumber(3.0));
    generator.add(arr);
    Assert.assertEquals("[1,,3]", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node arrTrailingEmpty = new Node(Token.ARRAYLIT, Node.newNumber(1.0), new Node(Token.EMPTY));
    generator.add(arrTrailingEmpty);
    Assert.assertEquals("[1,,]", consumer.getOutput());
  }

  @Test
  public void testVarAndName() {
    Node name1 = Node.newString(Token.NAME, "a");
    Node name2 = Node.newString(Token.NAME, "b");
    name2.addChildToBack(Node.newNumber(5.0));
    Node name3 = Node.newString(Token.NAME, "c");
    name3.addChildToBack(new Node(Token.COMMA, Node.newNumber(1.0), Node.newNumber(2.0)));

    Node var = new Node(Token.VAR, name1, name2, name3);
    generator.add(var);
    Assert.assertEquals("var a,b=5,c=(1,2)", consumer.getOutput());
  }

  @Test
  public void testGetPropAndGetElem() {
    Node obj = Node.newString(Token.NAME, "foo");
    Node prop = Node.newString(Token.STRING, "bar");
    Node getprop = new Node(Token.GETPROP, obj, prop);
    generator.add(getprop);
    Assert.assertEquals("foo.bar", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node numObj = Node.newNumber(5.0);
    Node numGetProp = new Node(Token.GETPROP, numObj, prop.cloneNode());
    generator.add(numGetProp);
    Assert.assertEquals("(5).bar", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node getelem = new Node(Token.GETELEM, obj.cloneNode(), Node.newString(Token.STRING, "k"));
    generator.add(getelem);
    Assert.assertEquals("foo[\"k\"]", consumer.getOutput());
  }

  @Test
  public void testCallAndEval() {
    Node callee = Node.newString(Token.NAME, "fn");
    Node arg = Node.newNumber(1.0);
    Node call = new Node(Token.CALL, callee, arg);
    generator.add(call);
    Assert.assertEquals("fn(1)", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node evalIndirect = Node.newString(Token.NAME, "eval");
    Node evalCall = new Node(Token.CALL, evalIndirect, arg.cloneNode());
    generator.add(evalCall);
    Assert.assertEquals("(0,eval)(1)", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node evalDirect = Node.newString(Token.NAME, "eval");
    evalDirect.putBooleanProp(Node.DIRECT_EVAL, true);
    Node evalDirectCall = new Node(Token.CALL, evalDirect, arg.cloneNode());
    generator.add(evalDirectCall);
    Assert.assertEquals("eval(1)", consumer.getOutput());
  }

  @Test
  public void testNew() {
    Node target = Node.newString(Token.NAME, "Foo");
    Node newWithoutArgs = new Node(Token.NEW, target);
    generator.add(newWithoutArgs);
    Assert.assertEquals("new Foo", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node newWithArgs = new Node(Token.NEW, target.cloneNode(), Node.newNumber(1.0), Node.newNumber(2.0));
    generator.add(newWithArgs);
    Assert.assertEquals("new Foo(1,2)", consumer.getOutput());
  }

  @Test
  public void testObjectLit() {
    Node obj = new Node(Token.OBJECTLIT);
    Node prop = Node.newString(Token.STRING, "k");
    prop.addChildToBack(Node.newNumber(1.0));
    obj.addChildToBack(prop);
    generator.add(obj, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("({k:1})", consumer.getOutput());
  }

  @Test
  public void testIfElse() {
    Node cond = Node.newString(Token.NAME, "c");
    Node block1 = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a")));
    Node block2 = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b")));
    Node ifElse = new Node(Token.IF, cond, block1, block2);
    generator.add(ifElse);
    Assert.assertEquals("if(c)a;else b;", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node ifOnly = new Node(Token.IF, cond.cloneNode(), block1.cloneNode());
    generator.add(ifOnly);
    Assert.assertEquals("if(c)a;", consumer.getOutput());
  }

  @Test
  public void testLoops() {
    Node cond = Node.newString(Token.NAME, "c");
    Node body = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a")));
    Node whileNode = new Node(Token.WHILE, cond, body);
    generator.add(whileNode);
    Assert.assertEquals("while(c)a;", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node doNode = new Node(Token.DO, body.cloneNode(), cond.cloneNode());
    generator.add(doNode);
    Assert.assertEquals("doa;while(c);", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node init = new Node(Token.VAR, Node.newString(Token.NAME, "i"));
    Node for4 = new Node(Token.FOR, init, cond.cloneNode(), Node.newString(Token.NAME, "inc"), body.cloneNode());
    generator.add(for4);
    Assert.assertEquals("for(var i;c;inc)a;", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node for3 = new Node(Token.FOR, Node.newString(Token.NAME, "k"), Node.newString(Token.NAME, "o"), body.cloneNode());
    generator.add(for3);
    Assert.assertEquals("for(k in o)a;", consumer.getOutput());
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    catchBlock.addChildToBack(catchNode);
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    generator.add(tryNode);
    Assert.assertEquals("try;catch(e);finally;", consumer.getOutput());
  }

  @Test
  public void testSwitchCaseDefault() {
    Node switchVal = Node.newString(Token.NAME, "v");
    Node case1 = new Node(Token.CASE, Node.newNumber(1.0), new Node(Token.BLOCK));
    Node def = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, switchVal, case1, def);
    generator.add(switchNode);
    Assert.assertEquals("switch(v){case 1:;default:;}", consumer.getOutput());
  }

  @Test
  public void testFunctionAndReturn() {
    Node fnName = Node.newString(Token.NAME, "foo");
    Node params = new Node(Token.LP, Node.newString(Token.NAME, "x"));
    Node body = new Node(Token.BLOCK, new Node(Token.RETURN, Node.newString(Token.NAME, "x")));
    Node fn = new Node(Token.FUNCTION, fnName, params, body);
    generator.add(fn, CodeGenerator.Context.STATEMENT);
    Assert.assertEquals("function foo(x){return x;};", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node retVoid = new Node(Token.RETURN);
    generator.add(retVoid);
    Assert.assertEquals("return;", consumer.getOutput());
  }

  @Test
  public void testBreakContinueThrowDebugger() {
    generator.add(new Node(Token.BREAK));
    generator.add(new Node(Token.CONTINUE));
    generator.add(new Node(Token.DEBUGGER));
    generator.add(new Node(Token.THROW, Node.newString(Token.NAME, "err")));
    Assert.assertEquals("break;continue;debugger;throw err;", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node lbl = Node.newString(Token.LABEL_NAME, "lbl");
    generator.add(new Node(Token.BREAK, lbl));
    generator.add(new Node(Token.CONTINUE, lbl.cloneNode()));
    Assert.assertEquals("break lbl;continue lbl;", consumer.getOutput());
  }

  @Test
  public void testLabelAndWith() {
    Node lblName = Node.newString(Token.LABEL_NAME, "myLabel");
    Node lblStmt = new Node(Token.LABEL, lblName, new Node(Token.BLOCK));
    generator.add(lblStmt);
    Assert.assertEquals("myLabel:;", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "o"), new Node(Token.BLOCK));
    generator.add(withNode);
    Assert.assertEquals("with(o);", consumer.getOutput());
  }

  @Test
  public void testRegexpNode() {
    Node regex = new Node(Token.REGEXP, Node.newString(Token.STRING, "abc"), Node.newString(Token.STRING, "gi"));
    generator.add(regex);
    Assert.assertEquals("/abc/gi", consumer.getOutput());
  }

  @Test
  public void testGetRefAndRefSpecial() {
    Node getRef = new Node(Token.GET_REF, Node.newString(Token.NAME, "x"));
    generator.add(getRef);
    Assert.assertEquals("x", consumer.getOutput());

    consumer.buffer.setLength(0);
    Node refSpecial = new Node(Token.REF_SPECIAL, Node.newString(Token.NAME, "y"));
    refSpecial.putProp(Node.NAME_PROP, "specialProp");
    generator.add(refSpecial);
    Assert.assertEquals("y.specialProp", consumer.getOutput());
  }

  @Test
  public void testScriptAndBlockPreserve() {
    consumer.preserveExtraBlocks = true;
    Node script = new Node(Token.SCRIPT, new Node(Token.EXPR_RESULT, Node.newNumber(1.0)));
    generator.add(script);
    Assert.assertEquals("1;", consumer.getOutput());
  }

  @Test(expected = Error.class)
  public void testExprVoidFails() {
    generator.add(new Node(Token.EXPR_VOID));
  }

  @Test(expected = Error.class)
  public void testUnknownNodeTypeFails() {
    generator.add(new Node(Token.LAST_TOKEN + 100));
  }
}
