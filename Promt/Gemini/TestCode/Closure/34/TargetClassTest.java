package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CodeGeneratorTest {

  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder sb = new StringBuilder();
    boolean continueProc = true;
    boolean preserveBlocks = false;
    int statementCount = 0;
    int blockCount = 0;

    @Override
    void append(String op) {
      sb.append(op);
    }

    @Override
    void addIdentifier(String identifier) {
      sb.append(identifier);
    }

    @Override
    void addNumber(double x) {
      long l = (long) x;
      if (l == x) {
        sb.append(l);
      } else {
        sb.append(x);
      }
    }

    @Override
    void endStatement(boolean needSemi) {
      if (needSemi) {
        sb.append(';');
      }
      statementCount++;
    }

    @Override
    void endFunction(boolean isStatement) {
      if (isStatement) {
        sb.append(';');
      }
    }

    @Override
    void beginBlock() {
      sb.append('{');
      blockCount++;
    }

    @Override
    void endBlock(boolean needSemi) {
      sb.append('}');
      if (needSemi) {
        sb.append(';');
      }
    }

    @Override
    void listSeparator() {
      sb.append(',');
    }

    @Override
    void beginCaseBody() {
      sb.append(':');
    }

    @Override
    void endCaseBody() {
      sb.append(';');
    }

    @Override
    boolean continueProcessing() {
      return continueProc;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveBlocks;
    }

    @Override
    boolean breakAfterBlockFor(Node n, boolean statementContext) {
      return statementContext;
    }

    String getOutput() {
      return sb.toString();
    }
  }

  private String generate(Node n) {
    TestCodeConsumer cc = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(cc);
    cg.add(n);
    return cc.getOutput();
  }

  private String generate(Node n, CodeGenerator.Context ctx) {
    TestCodeConsumer cc = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(cc);
    cg.add(n, ctx);
    return cc.getOutput();
  }

  @Test
  public void testSimpleNumber() {
    Assert.assertTrue(CodeGenerator.isSimpleNumber("123"));
    Assert.assertTrue(CodeGenerator.isSimpleNumber("9"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("0123"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(""));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("abc"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("12a"));

    Assert.assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("012")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("999999999999999999999999999999")));
  }

  @Test
  public void testTagAsStrict() {
    TestCodeConsumer cc = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(cc);
    cg.tagAsStrict();
    Assert.assertEquals("'use strict';", cc.getOutput());
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    Assert.assertEquals("\"hello\"", CodeGenerator.escapeToDoubleQuotedJsString("hello"));
    Assert.assertEquals("\"hello\\\"world\"", CodeGenerator.escapeToDoubleQuotedJsString("hello\"world"));
    Assert.assertEquals("\"line1\\nline2\"", CodeGenerator.escapeToDoubleQuotedJsString("line1\nline2"));
    Assert.assertEquals("\"line1\\rline2\"", CodeGenerator.escapeToDoubleQuotedJsString("line1\rline2"));
    Assert.assertEquals("\"tab\\ttab\"", CodeGenerator.escapeToDoubleQuotedJsString("tab\ttab"));
    Assert.assertEquals("\"null\\x00byte\"", CodeGenerator.escapeToDoubleQuotedJsString("null\0byte"));
    Assert.assertEquals("\"vert\\x0Btab\"", CodeGenerator.escapeToDoubleQuotedJsString("vert\u000btab"));
    Assert.assertEquals("\"backslash\\\\test\"", CodeGenerator.escapeToDoubleQuotedJsString("backslash\\test"));
  }

  @Test
  public void testRegexpEscape() {
    Assert.assertEquals("/hello/i", CodeGenerator.regexpEscape("hello") + "i");
    Assert.assertEquals("/a\\/b/", CodeGenerator.regexpEscape("a/b"));
    Assert.assertEquals("/a-->b/", CodeGenerator.regexpEscape("a-->b"));
    Assert.assertEquals("/a]]>b/", CodeGenerator.regexpEscape("a]]>b"));
    Assert.assertEquals("/<\\/script>/", CodeGenerator.regexpEscape("</script>"));
    Assert.assertEquals("/<\\!--comment/", CodeGenerator.regexpEscape("<!--comment"));
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("validIdent", CodeGenerator.identifierEscape("validIdent"));
    Assert.assertEquals("\\u00e9", CodeGenerator.identifierEscape("\u00e9"));
  }

  @Test
  public void testVariousNodes() {
    Node num = Node.newNumber(42);
    Assert.assertEquals("42", generate(num));

    Node nullNode = new Node(Token.NULL);
    Assert.assertEquals("null", generate(nullNode));

    Node thisNode = new Node(Token.THIS);
    Assert.assertEquals("this", generate(thisNode));

    Node falseNode = new Node(Token.FALSE);
    Assert.assertEquals("false", generate(falseNode));

    Node trueNode = new Node(Token.TRUE);
    Assert.assertEquals("true", generate(trueNode));

    Node debuggerNode = new Node(Token.DEBUGGER);
    Assert.assertEquals("debugger", generate(debuggerNode));

    Node emptyNode = new Node(Token.EMPTY);
    Assert.assertEquals("", generate(emptyNode));
  }

  @Test
  public void testBinaryOperators() {
    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertEquals("1+2", generate(add));

    Node chainedAdd = new Node(Token.ADD, new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2)), Node.newNumber(3));
    Assert.assertEquals("1+2+3", generate(chainedAdd));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newNumber(1)));
    Assert.assertEquals("a=b=1", generate(assign));

    Node mul = new Node(Token.MUL, Node.newNumber(2), new Node(Token.ADD, Node.newNumber(3), Node.newNumber(4)));
    Assert.assertEquals("2*(3+4)", generate(mul));
  }

  @Test
  public void testUnaryOperators() {
    Node not = new Node(Token.NOT, Node.newString(Token.NAME, "a"));
    Assert.assertEquals("!a", generate(not));

    Node bitnot = new Node(Token.BITNOT, Node.newString(Token.NAME, "a"));
    Assert.assertEquals("~a", generate(bitnot));

    Node typeof = new Node(Token.TYPEOF, Node.newString(Token.NAME, "a"));
    Assert.assertEquals("typeof a", generate(typeof));

    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    Assert.assertEquals("void 0", generate(voidNode));

    Node pos = new Node(Token.POS, Node.newString(Token.NAME, "a"));
    Assert.assertEquals("+a", generate(pos));

    Node negNum = new Node(Token.NEG, Node.newNumber(5));
    Assert.assertEquals("-5", generate(negNum));

    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "a"));
    Assert.assertEquals("-a", generate(negVar));

    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "a"));
    Assert.assertEquals("delete a", generate(del));
  }

  @Test
  public void testIncDec() {
    Node incPre = new Node(Token.INC, Node.newString(Token.NAME, "a"));
    incPre.putIntProp(Node.INCRDECR_PROP, 0);
    Assert.assertEquals("++a", generate(incPre));

    Node incPost = new Node(Token.INC, Node.newString(Token.NAME, "a"));
    incPost.putIntProp(Node.INCRDECR_PROP, 1);
    Assert.assertEquals("a++", generate(incPost));

    Node decPre = new Node(Token.DEC, Node.newString(Token.NAME, "a"));
    decPre.putIntProp(Node.INCRDECR_PROP, 0);
    Assert.assertEquals("--a", generate(decPre));

    Node decPost = new Node(Token.DEC, Node.newString(Token.NAME, "a"));
    decPost.putIntProp(Node.INCRDECR_PROP, 1);
    Assert.assertEquals("a--", generate(decPost));
  }

  @Test
  public void testHook() {
    Node hook = new Node(Token.HOOK, Node.newString(Token.NAME, "a"), Node.newNumber(1), Node.newNumber(2));
    Assert.assertEquals("a?1:2", generate(hook));
  }

  @Test
  public void testArrayLit() {
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY), Node.newNumber(2));
    Assert.assertEquals("[1,,2]", generate(arr));

    Node arrTrailingEmpty = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY));
    Assert.assertEquals("[1,,]", generate(arrTrailingEmpty));
  }

  @Test
  public void testCallAndNew() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), Node.newNumber(1), Node.newNumber(2));
    Assert.assertEquals("foo(1,2)", generate(call));

    Node newNoArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    Assert.assertEquals("new Foo", generate(newNoArgs));

    Node newWithArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1));
    Assert.assertEquals("new Foo(1)", generate(newWithArgs));

    Node evalCall = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1+1"));
    Assert.assertEquals("(0,eval)(\"1+1\")", generate(evalCall));

    Node directEvalCall = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1+1"));
    directEvalCall.getFirstChild().putBooleanProp(Node.DIRECT_EVAL, true);
    Assert.assertEquals("eval(\"1+1\")", generate(directEvalCall));

    Node prop = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("fn"));
    Node freeCall = new Node(Token.CALL, prop, Node.newNumber(1));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    Assert.assertEquals("(0,obj.fn)(1)", generate(freeCall));
  }

  @Test
  public void testControlStructures() {
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))));
    Assert.assertEquals("if(cond)1;", generate(ifNode));

    Node ifElse = new Node(Token.IF, Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(2))));
    Assert.assertEquals("if(cond)1;else 2;", generate(ifElse));

    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))));
    Assert.assertEquals("while(cond)1;", generate(whileNode));

    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))),
        Node.newString(Token.NAME, "cond"));
    Assert.assertEquals("do 1;while(cond);", generate(doNode));

    Node forNode = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        Node.newString(Token.NAME, "cond"),
        new Node(Token.INC, Node.newString(Token.NAME, "i")),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))));
    forNode.getChildAtIndex(2).putIntProp(Node.INCRDECR_PROP, 1);
    Assert.assertEquals("for(var i;cond;i++)1;", generate(forNode));

    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "k"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))));
    Assert.assertEquals("for(k in obj)1;", generate(forIn));

    Node withNode = new Node(Token.WITH,
        Node.newString(Token.NAME, "o"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))));
    Assert.assertEquals("with(o)1;", generate(withNode));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node catchBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(2)));
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node catchOuter = new Node(Token.BLOCK, catchNode);
    Node tryCatch = new Node(Token.TRY, tryBlock, catchOuter);
    Assert.assertEquals("try{1;}catch(e){2;}", generate(tryCatch));

    Node finallyBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(3)));
    Node tryCatchFinally = new Node(Token.TRY, tryBlock.cloneTree(), catchOuter.cloneTree(), finallyBlock);
    Assert.assertEquals("try{1;}catch(e){2;}finally{3;}", generate(tryCatchFinally));
  }

  @Test
  public void testSwitchCase() {
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(10))));
    Node def = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(20))));
    Node sw = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), case1, def);
    Assert.assertEquals("switch(x){case 1:{10;};default:{20;};}", generate(sw));
  }

  @Test
  public void testObjectLit() {
    Node key1 = Node.newString("a");
    key1.addChildToBack(Node.newNumber(1));
    Node key2 = Node.newString("123");
    key2.addChildToBack(Node.newNumber(2));
    Node obj = new Node(Token.OBJECTLIT, key1, key2);
    Assert.assertEquals("{a:1,123:2}", generate(obj));
  }

  @Test
  public void testGetPropGetElem() {
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    Assert.assertEquals("a.b", generate(getprop));

    Node getelem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newNumber(0));
    Assert.assertEquals("a[0]", generate(getelem));

    Node numProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("toString"));
    Assert.assertEquals("(1).toString", generate(numProp));
  }

  @Test
  public void testFunctionAndReturns() {
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "a")),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newString(Token.NAME, "a"))));
    Assert.assertEquals("function foo(a){return a;};", generate(fn, CodeGenerator.Context.STATEMENT));

    Node retEmpty = new Node(Token.RETURN);
    Assert.assertEquals("return", generate(retEmpty));

    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "err"));
    Assert.assertEquals("throw err;", generate(throwNode));

    Node breakNode = new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "lbl"));
    Assert.assertEquals("break lbl", generate(breakNode));

    Node contNode = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "lbl"));
    Assert.assertEquals("continue lbl", generate(contNode));
  }

  @Test
  public void testCustomCharsetEncoder() {
    Charset latin1 = Charset.forName("ISO-8859-1");
    TestCodeConsumer cc = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(cc, latin1);
    cg.add(Node.newString("test \u00e9 \u2603"));
    Assert.assertTrue(cc.getOutput().contains("\u00e9"));
    Assert.assertTrue(cc.getOutput().contains("\\u2603"));
  }

  @Test
  public void testStopProcessing() {
    TestCodeConsumer cc = new TestCodeConsumer();
    cc.continueProc = false;
    CodeGenerator cg = new CodeGenerator(cc);
    cg.add(Node.newNumber(123));
    Assert.assertEquals("", cc.getOutput());
  }

  @Test
  public void testLabels() {
    Node label = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "myLabel"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))));
    Assert.assertEquals("myLabel:1;", generate(label));
  }

  @Test
  public void testGetterSetter() {
    Node getFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newNumber(1))));
    Node getter = Node.newString(Token.GETTER_DEF, "prop");
    getter.addChildToBack(getFn);

    Node setFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "v")),
        new Node(Token.BLOCK));
    Node setter = Node.newString(Token.SETTER_DEF, "prop");
    setter.addChildToBack(setFn);

    Node obj = new Node(Token.OBJECTLIT, getter, setter);
    Assert.assertEquals("{get prop(){return 1;},set prop(v){}}", generate(obj));
  }
}