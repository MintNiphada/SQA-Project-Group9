package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CodeGeneratorTest {

  private static class SimpleCodeConsumer extends CodeConsumer {
    private final StringBuilder sb = new StringBuilder();
    private boolean continueProcessing = true;
    private boolean preserveBlocks = false;

    @Override
    void append(String newcode) {
      sb.append(newcode);
    }

    @Override
    char getLastChar() {
      return sb.length() > 0 ? sb.charAt(sb.length() - 1) : '\0';
    }

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveBlocks;
    }

    String getCode() {
      return sb.toString();
    }
  }

  private String generate(Node node) {
    SimpleCodeConsumer consumer = new SimpleCodeConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    cg.add(node);
    return consumer.getCode();
  }

  private String generateWithOptions(Node node, CompilerOptions options) {
    SimpleCodeConsumer consumer = new SimpleCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer, options);
    cg.add(node);
    return consumer.getCode();
  }

  @Test
  public void testSimpleNumber() {
    Assert.assertTrue(CodeGenerator.isSimpleNumber("0"));
    Assert.assertTrue(CodeGenerator.isSimpleNumber("123"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("0123"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(""));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("12a3"));

    Assert.assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.001);
    Assert.assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.001);
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("0123")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("999999999999999999999999999999")));
  }

  @Test
  public void testTagAsStrict() {
    SimpleCodeConsumer consumer = new SimpleCodeConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    cg.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer.getCode());
  }

  @Test
  public void testEscapes() {
    SimpleCodeConsumer consumer = new SimpleCodeConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Assert.assertEquals("\"hello\"", cg.escapeToDoubleQuotedJsString("hello"));
    Assert.assertEquals("\"\\\"quotes\\\"\"", cg.escapeToDoubleQuotedJsString("\"quotes\""));
    Assert.assertEquals("\"\\n\\r\\t\\b\\f\\\\\\x00\"", cg.escapeToDoubleQuotedJsString("\n\r\t\b\f\\\0"));
    Assert.assertEquals("\"\\u2028\\u2029\"", cg.escapeToDoubleQuotedJsString("\u2028\u2029"));
    Assert.assertEquals("\"\\x0B\"", cg.escapeToDoubleQuotedJsString("\u000B"));

    Assert.assertEquals("/foo/g", cg.regexpEscape("foo", null) + "g");
    Assert.assertEquals("/a\\/b/", cg.regexpEscape("a/b"));
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("abc", CodeGenerator.identifierEscape("abc"));
    Assert.assertEquals("\\u00e9", CodeGenerator.identifierEscape("\u00e9"));
    Assert.assertEquals("foo_123", CodeGenerator.identifierEscape("foo_123"));
  }

  @Test
  public void testVariousNodes() {
    Assert.assertEquals("null", generate(new Node(Token.NULL)));
    Assert.assertEquals("this", generate(new Node(Token.THIS)));
    Assert.assertEquals("false", generate(new Node(Token.FALSE)));
    Assert.assertEquals("true", generate(new Node(Token.TRUE)));
    Assert.assertEquals("debugger;", generate(new Node(Token.DEBUGGER)));
    Assert.assertEquals("continue;", generate(new Node(Token.CONTINUE)));
    Assert.assertEquals("break;", generate(new Node(Token.BREAK)));
    Assert.assertEquals("", generate(new Node(Token.EMPTY)));
    Assert.assertEquals("123", generate(Node.newNumber(123)));
    Assert.assertEquals("\"str\"", generate(Node.newString("str")));
  }

  @Test
  public void testUnaryAndBinaryOperators() {
    Node pos = new Node(Token.POS, Node.newNumber(5));
    Assert.assertEquals("+5", generate(pos));

    Node negNum = new Node(Token.NEG, Node.newNumber(5));
    Assert.assertEquals("-5", generate(negNum));

    Node negName = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    Assert.assertEquals("-x", generate(negName));

    Node notNode = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    Assert.assertEquals("!x", generate(notNode));

    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Assert.assertEquals("a+b", generate(add));

    Node sub = new Node(Token.SUB, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Assert.assertEquals("a-b", generate(sub));

    Node comma = new Node(Token.COMMA, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Assert.assertEquals("a,b", generate(comma));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Assert.assertEquals("a=b", generate(assign));

    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    Assert.assertEquals("delete x", generate(del));
  }

  @Test
  public void testControlStructures() {
    Node ifNode = new Node(Token.IF,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    Assert.assertEquals("if(cond)a;", generate(ifNode));

    Node ifElseNode = new Node(Token.IF,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"))));
    Assert.assertEquals("if(cond)a;else b;", generate(ifElseNode));

    Node whileNode = new Node(Token.WHILE,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    Assert.assertEquals("while(cond)a;", generate(whileNode));

    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))),
        Node.newString(Token.NAME, "cond"));
    Assert.assertEquals("do a;while(cond);", generate(doNode));

    Node forInNode = new Node(Token.FOR,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "c"))));
    Assert.assertEquals("for(a in b)c;", generate(forInNode));

    Node forNode = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "inc"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "c"))));
    Assert.assertEquals("for(var i;cond;inc)c;", generate(forNode));
  }

  @Test
  public void testFunctionsAndCalls() {
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "x")),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newString(Token.NAME, "x"))));
    Assert.assertEquals("function foo(x){return x;}", generate(fn));

    Node call = new Node(Token.CALL,
        Node.newString(Token.NAME, "foo"),
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Assert.assertEquals("foo(a,b)", generate(call));

    Node evalCall = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1"));
    Assert.assertEquals("(0,eval)(\"1\")", generate(evalCall));

    Node newCall = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newString(Token.NAME, "a"));
    Assert.assertEquals("new Foo(a)", generate(newCall));
  }

  @Test
  public void testArrayAndObjectLit() {
    Node arrayLit = new Node(Token.ARRAYLIT,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Assert.assertEquals("[a,b]", generate(arrayLit));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING_KEY, "k");
    key.addChildToFront(Node.newString(Token.NAME, "v"));
    objLit.addChildToFront(key);
    Assert.assertEquals("{k:v}", generate(objLit));
  }

  @Test
  public void testTryCatchFinallyAndThrow() {
    Node tryBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a")));
    Node catchBody = new Node(Token.BLOCK,
        new Node(Token.CATCH,
            Node.newString(Token.NAME, "e"),
            new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b")))));
    Node finallyBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "c")));
    Node tryNode = new Node(Token.TRY, tryBody, catchBody, finallyBody);
    Assert.assertEquals("try{a;}catch(e){b;}finally{c;}", generate(tryNode));

    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "e"));
    Assert.assertEquals("throw e;", generate(throwNode));
  }

  @Test
  public void testHookAndIncDec() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"),
        Node.newString(Token.NAME, "c"));
    Assert.assertEquals("a?b:c", generate(hook));

    Node incPre = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    Assert.assertEquals("++x", generate(incPre));

    Node incPost = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    incPost.putIntProp(Node.INCRDECR_PROP, 1);
    Assert.assertEquals("x++", generate(incPost));
  }

  @Test
  public void testSwitchAndLabels() {
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"));
    Node caseNode = new Node(Token.CASE,
        Node.newNumber(1),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    Node defaultCase = new Node(Token.DEFAULT_CASE,
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"))));
    switchNode.addChildToBack(caseNode);
    switchNode.addChildToBack(defaultCase);
    Assert.assertEquals("switch(x){case 1:a;default:b;}", generate(switchNode));

    Node label = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "lbl"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    Assert.assertEquals("lbl:a;", generate(label));

    Node breakLbl = new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "lbl"));
    Assert.assertEquals("break lbl;", generate(breakLbl));

    Node contLbl = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "lbl"));
    Assert.assertEquals("continue lbl;", generate(contLbl));
  }

  @Test
  public void testOptionsEncodingAndQuotePreferences() {
    CompilerOptions options = new CompilerOptions();
    options.preferSingleQuotes = true;
    options.trustedStrings = false;
    options.setOutputCharset("ISO-8859-1");

    Node str = Node.newString("foo 'bar' </script> -->");
    String res = generateWithOptions(str, options);
    Assert.assertTrue(res.startsWith("'"));
    Assert.assertTrue(res.contains("\\x3c/script"));
    Assert.assertTrue(res.contains("--\\x3e"));

    CompilerOptions es3Options = new CompilerOptions();
    es3Options.setLanguageOut(LanguageMode.ECMASCRIPT3);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("delete"));
    Assert.assertEquals("obj[\"delete\"]", generateWithOptions(getprop, es3Options));
  }

  @Test
  public void testCastAndGetPropGetElem() {
    Node cast = new Node(Token.CAST, Node.newString(Token.NAME, "x"));
    Assert.assertEquals("(x)", generate(cast));

    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("prop"));
    Assert.assertEquals("obj.prop", generate(getprop));

    Node getelem = new Node(Token.GETELEM, Node.newString(Token.NAME, "obj"), Node.newString(Token.NAME, "key"));
    Assert.assertEquals("obj[key]", generate(getelem));
  }

  @Test
  public void testStopProcessing() {
    SimpleCodeConsumer consumer = new SimpleCodeConsumer();
    consumer.continueProcessing = false;
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    cg.add(Node.newNumber(123));
    Assert.assertEquals("", consumer.getCode());
  }
}