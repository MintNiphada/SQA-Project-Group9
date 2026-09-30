package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CodeGeneratorTest {

  private static class DummyCodeConsumer extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();
    boolean continueProcessing = true;
    boolean preserveExtraBlocks = false;

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    @Override
    char getLastChar() {
      return sb.length() == 0 ? '\0' : sb.charAt(sb.length() - 1);
    }

    @Override
    void append(String str) {
      sb.append(str);
    }

    @Override
    void appendOp(String op, boolean binOp) {
      sb.append(op);
    }

    @Override
    void appendNumber(double x) {
      long l = (long) x;
      if (l == x) {
        sb.append(l);
      } else {
        sb.append(x);
      }
    }

    @Override
    void appendBlockStart() {
      sb.append("{");
    }

    @Override
    void appendBlockEnd() {
      sb.append("}");
    }

    @Override
    void startSourceMapping(Node n) {}

    @Override
    void endSourceMapping(Node n) {}

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    @Override
    boolean breakAfterBlockFor(Node n, boolean isStatement) {
      return false;
    }
  }

  private DummyCodeConsumer consumer;
  private CodeGenerator generator;

  @Before
  public void setUp() {
    consumer = new DummyCodeConsumer();
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
    Assert.assertEquals("'use strict';", consumer.sb.toString());
  }

  @Test
  public void testAddString() {
    generator.add("var x = 1;");
    Assert.assertEquals("var x = 1;", consumer.sb.toString());
  }

  @Test
  public void testContinueProcessingFalse() {
    consumer.continueProcessing = false;
    Node n = Node.newString(Token.NAME, "x");
    generator.add(n);
    Assert.assertEquals("", consumer.sb.toString());
  }

  @Test
  public void testIsSimpleNumberAndGetSimpleNumber() {
    Assert.assertTrue(CodeGenerator.isSimpleNumber("123"));
    Assert.assertTrue(CodeGenerator.isSimpleNumber("0"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(""));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("12a"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("-1"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("+1"));

    Assert.assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
    Assert.assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    // Very large number exceeding MAX_POSITIVE_INTEGER_NUMBER
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("99999999999999999999999999999999")));
  }

  @Test
  public void testRegexpEscape() {
    String escaped = CodeGenerator.regexpEscape("/hello/");
    Assert.assertEquals("///hello//", escaped);

    CharsetEncoder encoder = Charsets.US_ASCII.newEncoder();
    String escapedEncoder = CodeGenerator.regexpEscape("hello \u1234", encoder);
    Assert.assertTrue(escapedEncoder.contains("\\u1234"));
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    String res = CodeGenerator.escapeToDoubleQuotedJsString("a\"b'c\n\r\t\0\\");
    Assert.assertEquals("\"a\\\"b'c\\n\\r\\t\\0\\\\\"", res);
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("myVar_123$", CodeGenerator.identifierEscape("myVar_123$"));
    String escaped = CodeGenerator.identifierEscape("var_\u00E9");
    Assert.assertTrue(escaped.startsWith("var_\\u00e9") || escaped.startsWith("var_\\u00E9"));
  }

  @Test
  public void testJsStringEscapingAndQuotes() {
    // Single quotes preferred when double quotes count is higher
    String s1 = generator.jsString("Hello \"World\" \"Again\"");
    Assert.assertTrue(s1.startsWith("'") && s1.endsWith("'"));

    // Double quotes preferred when single quotes count is higher or equal
    String s2 = generator.jsString("Hello 'World'");
    Assert.assertTrue(s2.startsWith("\"") && s2.endsWith("\""));

    // Escaping special characters: -->, ]]>, </script, <!--
    String s3 = generator.jsString("--> ]]> </script> <!--");
    Assert.assertTrue(s3.contains("--\\>"));
    Assert.assertTrue(s3.contains("]]\\>"));
    Assert.assertTrue(s3.contains("<\\/script"));
    Assert.assertTrue(s3.contains("<\\!--"));

    // Test cached string retrieval
    generator.addJsString("test_cached");
    generator.addJsString("test_cached");
  }

  @Test
  public void testStrEscapeWithEncoder() {
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String escaped = CodeGenerator.strEscape(
        "ascii \u0100 \uD834\uDD1E", '"', "\\\"", "'", "\\\\", asciiEncoder);
    Assert.assertTrue(escaped.contains("\\u0100"));
    Assert.assertTrue(escaped.contains("\\ud834\\udd1e"));
  }

  @Test
  public void testBinaryOperatorsAndAssociativity() {
    // a + b
    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    generator.add(add);
    Assert.assertEquals("a+b", consumer.sb.toString());

    consumer.sb.setLength(0);
    // a + (b + c) - associative
    Node addInner = new Node(Token.ADD, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node addOuter = new Node(Token.ADD, Node.newString(Token.NAME, "a"), addInner);
    generator.add(addOuter);
    Assert.assertEquals("a+b+c", consumer.sb.toString());

    consumer.sb.setLength(0);
    // a = (b = c) - assignment associativity
    Node assignInner = new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node assignOuter = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), assignInner);
    generator.add(assignOuter);
    Assert.assertEquals("a=b=c", consumer.sb.toString());

    consumer.sb.setLength(0);
    // a - (b - c) - non-associative requires parens
    Node subInner = new Node(Token.SUB, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node subOuter = new Node(Token.SUB, Node.newString(Token.NAME, "a"), subInner);
    generator.add(subOuter);
    Assert.assertEquals("a-(b-c)", consumer.sb.toString());
  }

  @Test
  public void testTryCatchFinally() {
    // try { } catch(e) { } finally { }
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    generator.add(tryNode);
    Assert.assertTrue(consumer.sb.toString().contains("try"));
    Assert.assertTrue(consumer.sb.toString().contains("catch(e)"));
    Assert.assertTrue(consumer.sb.toString().contains("finally"));

    consumer.sb.setLength(0);
    // try { } finally { } (no catch block)
    Node tryNodeNoCatch = new Node(Token.TRY, new Node(Token.BLOCK), new Node(Token.BLOCK), new Node(Token.BLOCK));
    generator.add(tryNodeNoCatch);
    Assert.assertTrue(consumer.sb.toString().contains("try"));
    Assert.assertTrue(consumer.sb.toString().contains("finally"));
  }

  @Test
  public void testThrowAndReturn() {
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "e"));
    generator.add(throwNode);
    Assert.assertEquals("throw e;", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node returnVal = new Node(Token.RETURN, Node.newNumber(42));
    generator.add(returnVal);
    Assert.assertEquals("return 42;", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node returnEmpty = new Node(Token.RETURN);
    generator.add(returnEmpty);
    Assert.assertEquals("return;", consumer.sb.toString());
  }

  @Test
  public void testVarAndName() {
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    generator.add(varNode);
    Assert.assertEquals("var x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node nameWithInit = Node.newString(Token.NAME, "x");
    nameWithInit.addChildToBack(Node.newNumber(1));
    Node varWithInit = new Node(Token.VAR, nameWithInit);
    generator.add(varWithInit);
    Assert.assertEquals("var x=1", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node nameWithComma = Node.newString(Token.NAME, "y");
    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    nameWithComma.addChildToBack(comma);
    generator.add(nameWithComma);
    Assert.assertEquals("y=(1,2)", consumer.sb.toString());
  }

  @Test
  public void testArrayLitAndComma() {
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY), Node.newNumber(2));
    generator.add(arr);
    Assert.assertEquals("[1,,2]", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node arrEmptyEnd = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY));
    generator.add(arrEmptyEnd);
    Assert.assertEquals("[1,,]", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    generator.add(comma);
    Assert.assertEquals("1,2", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node lp = new Node(Token.LP, Node.newNumber(1), Node.newNumber(2));
    generator.add(lp);
    Assert.assertEquals("(1,2)", consumer.sb.toString());
  }

  @Test
  public void testUnaryOperators() {
    Node typeofNode = new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"));
    generator.add(typeofNode);
    Assert.assertEquals("typeof x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    generator.add(voidNode);
    Assert.assertEquals("void 0", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node notNode = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    generator.add(notNode);
    Assert.assertEquals("!x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node bitnotNode = new Node(Token.BITNOT, Node.newString(Token.NAME, "x"));
    generator.add(bitnotNode);
    Assert.assertEquals("~x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node posNode = new Node(Token.POS, Node.newNumber(5));
    generator.add(posNode);
    Assert.assertEquals("+5", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node negNum = new Node(Token.NEG, Node.newNumber(5));
    generator.add(negNum);
    Assert.assertEquals("-5", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    generator.add(negVar);
    Assert.assertEquals("-x", consumer.sb.toString());
  }

  @Test
  public void testIncDec() {
    Node incPre = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    generator.add(incPre);
    Assert.assertEquals("++x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node incPost = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    incPost.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(incPost);
    Assert.assertEquals("x++", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node decPre = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    generator.add(decPre);
    Assert.assertEquals("--x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node decPost = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    decPost.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(decPost);
    Assert.assertEquals("x--", consumer.sb.toString());
  }

  @Test
  public void testHook() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        Node.newNumber(1),
        Node.newNumber(2));
    generator.add(hook);
    Assert.assertEquals("cond?1:2", consumer.sb.toString());
  }

  @Test
  public void testRegexpNode() {
    Node re = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("g"));
    generator.add(re);
    Assert.assertEquals("/abc/g", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node reNoFlags = new Node(Token.REGEXP, Node.newString("abc"));
    generator.add(reNoFlags);
    Assert.assertEquals("/abc/", consumer.sb.toString());
  }

  @Test
  public void testFunction() {
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.LP, Node.newString(Token.NAME, "a")),
        new Node(Token.BLOCK));
    generator.add(fn, CodeGenerator.Context.STATEMENT);
    Assert.assertEquals("function foo(a){}", consumer.sb.toString());

    consumer.sb.setLength(0);
    generator.add(fn, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("(function foo(a){})", consumer.sb.toString());
  }

  @Test
  public void testGetAndSetInObjectLit() {
    Node objLit = new Node(Token.OBJECTLIT);

    // Getter
    Node getFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        new Node(Token.BLOCK));
    Node getNode = new Node(Token.GET, getFn);
    getNode.setString("prop");
    objLit.addChildToBack(getNode);

    // Setter
    Node setFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP, Node.newString(Token.NAME, "val")),
        new Node(Token.BLOCK));
    Node setNode = new Node(Token.SET, setFn);
    setNode.setString("prop");
    objLit.addChildToBack(setNode);

    generator.add(objLit);
    Assert.assertEquals("{get prop(){},set prop(val){}}", consumer.sb.toString());
  }

  @Test
  public void testObjectLiteralProperties() {
    Node objLit = new Node(Token.OBJECTLIT);

    Node strKey = Node.newString("a");
    strKey.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(strKey);

    Node numKey = Node.newString("123");
    numKey.addChildToBack(Node.newNumber(2));
    objLit.addChildToBack(numKey);

    Node kwKey = Node.newString("default");
    kwKey.addChildToBack(Node.newNumber(3));
    objLit.addChildToBack(kwKey);

    generator.add(objLit, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("({a:1,123:2,\"default\":3})", consumer.sb.toString());
  }

  @Test
  public void testScriptAndBlock() {
    Node script = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    script.addChildToBack(varNode);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    script.addChildToBack(fn);

    generator.add(script);
    Assert.assertEquals("var x;function f(){}", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node block = new Node(Token.BLOCK, Node.newString(Token.NAME, "x"));
    generator.add(block, CodeGenerator.Context.PRESERVE_BLOCK);
    Assert.assertEquals("{x}", consumer.sb.toString());
  }

  @Test
  public void testControlFlowLoops() {
    // For-4
    Node for4 = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "step"),
        new Node(Token.BLOCK));
    generator.add(for4);
    Assert.assertEquals("for(var i;cond;step);", consumer.sb.toString());

    consumer.sb.setLength(0);
    // For-3 (for-in)
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "k"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK));
    generator.add(forIn);
    Assert.assertEquals("for(k in obj);", consumer.sb.toString());

    consumer.sb.setLength(0);
    // While
    Node whileNode = new Node(Token.WHILE,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK));
    generator.add(whileNode);
    Assert.assertEquals("while(cond);", consumer.sb.toString());

    consumer.sb.setLength(0);
    // Do-while
    Node doWhile = new Node(Token.DO,
        new Node(Token.BLOCK),
        Node.newString(Token.NAME, "cond"));
    generator.add(doWhile);
    Assert.assertEquals("do;while(cond);", consumer.sb.toString());
  }

  @Test
  public void testGetPropAndGetElem() {
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    generator.add(getProp);
    Assert.assertEquals("a.b", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node numGetProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("toString"));
    generator.add(numGetProp);
    Assert.assertEquals("(1).toString", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newNumber(0));
    generator.add(getElem);
    Assert.assertEquals("a[0]", consumer.sb.toString());
  }

  @Test
  public void testWithNode() {
    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "o"), new Node(Token.BLOCK));
    generator.add(withNode);
    Assert.assertEquals("with(o);", consumer.sb.toString());
  }

  @Test
  public void testCallAndDirectIndirectEval() {
    // Normal call
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), Node.newNumber(1));
    generator.add(call);
    Assert.assertEquals("foo(1)", consumer.sb.toString());

    consumer.sb.setLength(0);
    // Indirect eval: NAME 'eval' without DIRECT_EVAL prop
    Node indirectEval = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("x"));
    generator.add(indirectEval);
    Assert.assertEquals("(0,eval)(\"x\")", consumer.sb.toString());

    consumer.sb.setLength(0);
    // Direct eval
    Node directEvalName = Node.newString(Token.NAME, "eval");
    directEvalName.putBooleanProp(Node.DIRECT_EVAL, true);
    Node directEval = new Node(Token.CALL, directEvalName, Node.newString("x"));
    generator.add(directEval);
    Assert.assertEquals("eval(\"x\")", consumer.sb.toString());

    consumer.sb.setLength(0);
    // Free call on GETPROP
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    Node freeCall = new Node(Token.CALL, getProp, Node.newNumber(1));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    generator.add(freeCall);
    Assert.assertEquals("(0,a.b)(1)", consumer.sb.toString());
  }

  @Test
  public void testIfElse() {
    // If without else
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK));
    generator.add(ifNode);
    Assert.assertEquals("if(cond);", consumer.sb.toString());

    consumer.sb.setLength(0);
    // If with else
    Node ifElse = new Node(Token.IF,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK),
        new Node(Token.BLOCK));
    generator.add(ifElse);
    Assert.assertEquals("if(cond);else;", consumer.sb.toString());

    consumer.sb.setLength(0);
    // Dangling else disambiguation
    generator.add(ifNode, CodeGenerator.Context.BEFORE_DANGLING_ELSE);
    Assert.assertEquals("{if(cond);}", consumer.sb.toString());
  }

  @Test
  public void testLiteralsAndKeywords() {
    generator.add(new Node(Token.NULL));
    generator.add(new Node(Token.THIS));
    generator.add(new Node(Token.FALSE));
    generator.add(new Node(Token.TRUE));
    generator.add(new Node(Token.DEBUGGER));
    Assert.assertEquals("nullthisfalsetruedebugger;", consumer.sb.toString());
  }

  @Test
  public void testBreakAndContinue() {
    generator.add(new Node(Token.CONTINUE));
    Node labelName = Node.newString(Token.LABEL_NAME, "lbl");
    generator.add(new Node(Token.CONTINUE, labelName));
    generator.add(new Node(Token.BREAK));
    generator.add(new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "lbl2")));
    Assert.assertEquals("continue;continue lbl;break;break lbl2;", consumer.sb.toString());
  }

  @Test
  public void testExprResult() {
    Node exprResult = new Node(Token.EXPR_RESULT, Node.newNumber(123));
    generator.add(exprResult);
    Assert.assertEquals("123;", consumer.sb.toString());
  }

  @Test
  public void testNewNode() {
    Node newSimple = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    generator.add(newSimple);
    Assert.assertEquals("new Foo", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node newWithArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1), Node.newNumber(2));
    generator.add(newWithArgs);
    Assert.assertEquals("new Foo(1,2)", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "getConstructor"));
    Node newWithCall = new Node(Token.NEW, call, Node.newNumber(1));
    generator.add(newWithCall);
    Assert.assertEquals("new (getConstructor())(1)", consumer.sb.toString());
  }

  @Test
  public void testDelpropRefSpecialAndLabel() {
    Node delprop = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    generator.add(delprop);
    Assert.assertEquals("delete x", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node refSpecial = new Node(Token.REF_SPECIAL, Node.newString(Token.NAME, "x"));
    refSpecial.putProp(Node.NAME_PROP, "specialProp");
    generator.add(refSpecial);
    Assert.assertEquals("x.specialProp", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node getRef = new Node(Token.GET_REF, Node.newString(Token.NAME, "y"));
    generator.add(getRef);
    Assert.assertEquals("y", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node label = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "myLabel"),
        new Node(Token.BLOCK));
    generator.add(label);
    Assert.assertEquals("myLabel:;", consumer.sb.toString());
  }

  @Test
  public void testSwitchCaseDefault() {
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "val"));
    Node caseNode = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK, new Node(Token.BREAK)));
    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK, new Node(Token.BREAK)));
    switchNode.addChildToBack(caseNode);
    switchNode.addChildToBack(defaultNode);

    generator.add(switchNode);
    Assert.assertTrue(consumer.sb.toString().contains("switch(val){case 1:break;default:break;}"));
  }

  @Test
  public void testPreserveExtraBlocks() {
    consumer.preserveExtraBlocks = true;
    Node block = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "c"), block);
    generator.add(ifNode);
    Assert.assertEquals("if(c){}", consumer.sb.toString());
  }

  @Test
  public void testOneExactlyFunctionOrDoInBlock() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    Node block = new Node(Token.BLOCK, fn);
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "c"), block);
    generator.add(ifNode);
    Assert.assertEquals("if(c){function f(){}}", consumer.sb.toString());

    consumer.sb.setLength(0);
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "d"));
    Node blockDo = new Node(Token.BLOCK, doNode);
    Node ifDo = new Node(Token.IF, Node.newString(Token.NAME, "c"), blockDo);
    generator.add(ifDo);
    Assert.assertEquals("if(c){do;while(d);}", consumer.sb.toString());
  }

  @Test
  public void testInOperatorInForInitClause() {
    Node inNode = new Node(Token.IN, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Node for4 = new Node(Token.FOR,
        inNode,
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "step"),
        new Node(Token.BLOCK));
    generator.add(for4);
    Assert.assertEquals("for((a in b);cond;step);", consumer.sb.toString());
  }

  @Test(expected = Error.class)
  public void testUnknownNodeTypeThrowsError() {
    Node unknownNode = new Node(9999);
    generator.add(unknownNode);
  }

  @Test(expected = Error.class)
  public void testExprVoidThrowsError() {
    Node exprVoid = new Node(Token.EXPR_VOID);
    generator.add(exprVoid);
  }
}