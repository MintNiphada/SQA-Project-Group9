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
    final StringBuilder buffer = new StringBuilder();
    boolean preserveExtraBlocks = false;
    boolean breakAfterBlock = false;
    boolean continueProcessing = true;

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    @Override
    boolean breakAfterBlockFor(Node n, boolean statementContext) {
      return breakAfterBlock;
    }

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    String getCode() {
      return buffer.toString();
    }
  }

  private static Node name(String str) {
    return Node.newString(Token.NAME, str);
  }

  private static Node str(String str) {
    return Node.newString(Token.STRING, str);
  }

  private static Node labelName(String str) {
    return Node.newString(Token.LABEL_NAME, str);
  }

  private static Node num(double d) {
    return Node.newNumber(d);
  }

  private static Node block(Node... children) {
    Node b = new Node(Token.BLOCK);
    for (Node c : children) {
      b.addChildToBack(c);
    }
    return b;
  }

  private static Node expr(Node c) {
    return new Node(Token.EXPR_RESULT, c);
  }

  @Test
  public void testConstructorsAndTagAsStrict() {
    TestCodeConsumer consumer1 = new TestCodeConsumer();
    CodeGenerator cg1 = new CodeGenerator(consumer1);
    cg1.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer1.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2, Charsets.US_ASCII);
    cg2.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer2.getCode());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3, Charsets.UTF_8);
    cg3.add("test");
    Assert.assertEquals("test", consumer3.getCode());
  }

  @Test
  public void testContinueProcessingFalse() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.continueProcessing = false;
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(name("a"));
    Assert.assertEquals("", consumer.getCode());
  }

  @Test
  public void testBinaryOperatorsAndAssociativity() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // a + (b + c) -> associative
    Node addTree = new Node(Token.ADD, name("a"), new Node(Token.ADD, name("b"), name("c")));
    cg.add(addTree);
    Assert.assertEquals("a+b+c", consumer.getCode());

    // a - (b - c) -> non-associative, RHS has higher precedence
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node subTree = new Node(Token.SUB, name("a"), new Node(Token.SUB, name("b"), name("c")));
    cg2.add(subTree);
    Assert.assertEquals("a-(b-c)", consumer2.getCode());

    // a = (b = c) -> right-associative assignments
    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node assignTree = new Node(Token.ASSIGN, name("a"), new Node(Token.ASSIGN, name("b"), name("c")));
    cg3.add(assignTree);
    Assert.assertEquals("a=b=c", consumer3.getCode());
  }

  @Test(expected = IllegalStateException.class)
  public void testBadBinaryOperatorArgCount() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node badAdd = new Node(Token.ADD, name("a"));
    cg.add(badAdd);
  }

  @Test
  public void testTryCatchFinally() {
    // try { a; } catch(e) { b; } finally { c; }
    Node tryNode = new Node(Token.TRY,
        block(expr(name("a"))),
        block(new Node(Token.CATCH, name("e"), block(expr(name("b"))))),
        block(expr(name("c"))));

    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(tryNode);
    Assert.assertEquals("try{a;}catch(e){b;}finally{c;}", consumer.getCode());

    // try { a; } finally { c; } (no catch)
    Node tryFinally = new Node(Token.TRY,
        block(expr(name("a"))),
        block(),
        block(expr(name("c"))));

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    cg2.add(tryFinally);
    Assert.assertEquals("try{a;}finally{c;}", consumer2.getCode());
  }

  @Test
  public void testThrowAndReturn() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node throwNode = new Node(Token.THROW, name("err"));
    cg.add(throwNode);
    Assert.assertEquals("throw err;", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node returnVal = new Node(Token.RETURN, num(42.0));
    cg2.add(returnVal);
    Assert.assertEquals("return 42;", consumer2.getCode());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node returnEmpty = new Node(Token.RETURN);
    cg3.add(returnEmpty);
    Assert.assertEquals("return;", consumer3.getCode());
  }

  @Test
  public void testVarAndNames() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // var x;
    Node varNode = new Node(Token.VAR, name("x"));
    cg.add(varNode);
    Assert.assertEquals("var x", consumer.getCode());

    // var a = (1, 2);
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node comma = new Node(Token.COMMA, num(1), num(2));
    Node varWithComma = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
    varWithComma.getFirstChild().addChildToBack(comma);
    cg2.add(varWithComma);
    Assert.assertEquals("var a=(1,2)", consumer2.getCode());

    // var b = 5, c;
    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node varMultiple = new Node(Token.VAR);
    Node bName = name("b");
    bName.addChildToBack(num(5));
    Node cName = name("c");
    varMultiple.addChildToBack(bName);
    varMultiple.addChildToBack(cName);
    cg3.add(varMultiple);
    Assert.assertEquals("var b=5,c", consumer3.getCode());

    // var with empty name child
    TestCodeConsumer consumer4 = new TestCodeConsumer();
    CodeGenerator cg4 = new CodeGenerator(consumer4);
    Node emptyVar = new Node(Token.VAR, Node.newString(Token.NAME, "d"));
    emptyVar.getFirstChild().addChildToBack(new Node(Token.EMPTY));
    cg4.add(emptyVar);
    Assert.assertEquals("var d", consumer4.getCode());

    // empty var node
    TestCodeConsumer consumer5 = new TestCodeConsumer();
    CodeGenerator cg5 = new CodeGenerator(consumer5);
    cg5.add(new Node(Token.VAR));
    Assert.assertEquals("", consumer5.getCode());
  }

  @Test
  public void testArraysAndParenLists() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // [1, , 2, ]
    Node arrayLit = new Node(Token.ARRAYLIT, num(1), new Node(Token.EMPTY), num(2), new Node(Token.EMPTY));
    cg.add(arrayLit);
    Assert.assertEquals("[1,,2,,]", consumer.getCode());

    // (1, 2)
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node lp = new Node(Token.LP, num(1), num(2));
    cg2.add(lp);
    Assert.assertEquals("(1,2)", consumer2.getCode());
  }

  @Test
  public void testUnaryOperators() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    cg.add(new Node(Token.TYPEOF, name("x")));
    Assert.assertEquals("typeof x", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    cg2.add(new Node(Token.VOID, num(0)));
    Assert.assertEquals("void 0", consumer2.getCode());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    cg3.add(new Node(Token.NEG, num(5)));
    Assert.assertEquals("-5", consumer3.getCode());

    TestCodeConsumer consumer4 = new TestCodeConsumer();
    CodeGenerator cg4 = new CodeGenerator(consumer4);
    cg4.add(new Node(Token.NEG, name("x")));
    Assert.assertEquals("-x", consumer4.getCode());

    TestCodeConsumer consumer5 = new TestCodeConsumer();
    CodeGenerator cg5 = new CodeGenerator(consumer5);
    cg5.add(new Node(Token.NOT, name("x")));
    Assert.assertEquals("!x", consumer5.getCode());

    TestCodeConsumer consumer6 = new TestCodeConsumer();
    CodeGenerator cg6 = new CodeGenerator(consumer6);
    cg6.add(new Node(Token.BITNOT, name("x")));
    Assert.assertEquals("~x", consumer6.getCode());

    TestCodeConsumer consumer7 = new TestCodeConsumer();
    CodeGenerator cg7 = new CodeGenerator(consumer7);
    cg7.add(new Node(Token.POS, name("x")));
    Assert.assertEquals("+x", consumer7.getCode());
  }

  @Test
  public void testHookOperator() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node hook = new Node(Token.HOOK, name("cond"), name("a"), name("b"));
    cg.add(hook);
    Assert.assertEquals("cond?a:b", consumer.getCode());
  }

  @Test
  public void testRegexp() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node regex2 = new Node(Token.REGEXP, str("abc"), str("gi"));
    cg.add(regex2);
    Assert.assertEquals("/abc/gi", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node regex1 = new Node(Token.REGEXP, str("abc"));
    cg2.add(regex1);
    Assert.assertEquals("/abc/", consumer2.getCode());
  }

  @Test(expected = Error.class)
  public void testRegexpInvalidChildren() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node badRegex = new Node(Token.REGEXP, num(1), num(2));
    cg.add(badRegex);
  }

  @Test
  public void testFunction() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // function foo(x) { return x; }
    Node fn = new Node(Token.FUNCTION,
        name("foo"),
        new Node(Token.LP, name("x")),
        block(new Node(Token.RETURN, name("x"))));
    cg.add(fn, CodeGenerator.Context.STATEMENT);
    Assert.assertEquals("function foo(x){return x;}", consumer.getCode());

    // function as START_OF_EXPR
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    cg2.add(fn, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("(function foo(x){return x;})", consumer2.getCode());
  }

  @Test
  public void testGetAndSetInObjectLit() {
    // Object lit with get and set
    Node fnGet = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        block());
    Node getNode = Node.newString(Token.GET, "foo");
    getNode.addChildToBack(fnGet);

    Node fnSet = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP, name("val")),
        block());
    Node setNode = Node.newString(Token.SET, "123");
    setNode.addChildToBack(fnSet);

    Node objLit = new Node(Token.OBJECTLIT, getNode, setNode);

    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(objLit, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("({get foo(){},set 123(val){}})", consumer.getCode());

    // Non-latin / quoted get
    Node fnGet2 = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        block());
    Node getNode2 = Node.newString(Token.GET, "get-prop");
    getNode2.setQuotedString();
    getNode2.addChildToBack(fnGet2);
    Node objLit2 = new Node(Token.OBJECTLIT, getNode2);

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    cg2.add(objLit2);
    Assert.assertEquals("{get \"get-prop\"(){}}", consumer2.getCode());
  }

  @Test
  public void testObjectLitKeys() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // { a: 1, 'var': 2, '456': 3, 'hello world': 4 }
    Node propA = str("a");
    propA.addChildToBack(num(1));

    Node propVar = str("var"); // keyword
    propVar.addChildToBack(num(2));

    Node propNum = str("456");
    propNum.addChildToBack(num(3));

    Node propStr = str("hello world");
    propStr.addChildToBack(num(4));

    Node obj = new Node(Token.OBJECTLIT, propA, propVar, propNum, propStr);
    cg.add(obj);
    Assert.assertEquals("{a:1,\"var\":2,456:3,\"hello world\":4}", consumer.getCode());
  }

  @Test
  public void testScriptAndBlock() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node script = new Node(Token.SCRIPT,
        new Node(Token.VAR, name("x")),
        new Node(Token.FUNCTION, name("f"), new Node(Token.LP), block()),
        expr(name("a")));
    cg.add(script);
    Assert.assertEquals("var x;function f(){}a;", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node b = block(expr(name("a")), expr(name("b")));
    cg2.add(b, CodeGenerator.Context.PRESERVE_BLOCK);
    Assert.assertEquals("{a;b;}", consumer2.getCode());
  }

  @Test
  public void testForLoops() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // for (var i; cond; inc) body;
    Node for4 = new Node(Token.FOR,
        new Node(Token.VAR, name("i")),
        name("cond"),
        name("inc"),
        block(expr(name("body"))));
    cg.add(for4);
    Assert.assertEquals("for(var i;cond;inc)body;", consumer.getCode());

    // for (i = 0; cond; inc) body;
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node for4Expr = new Node(Token.FOR,
        new Node(Token.ASSIGN, name("i"), num(0)),
        name("cond"),
        name("inc"),
        block(expr(name("body"))));
    cg2.add(for4Expr);
    Assert.assertEquals("for(i=0;cond;inc)body;", consumer2.getCode());

    // for (var k in obj) body;
    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node forIn = new Node(Token.FOR,
        new Node(Token.VAR, name("k")),
        name("obj"),
        block(expr(name("body"))));
    cg3.add(forIn);
    Assert.assertEquals("for(var k in obj)body;", consumer3.getCode());
  }

  @Test
  public void testDoWhileAndWith() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node doNode = new Node(Token.DO, block(expr(name("body"))), name("cond"));
    cg.add(doNode);
    Assert.assertEquals("do body;while(cond);", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node whileNode = new Node(Token.WHILE, name("cond"), block(expr(name("body"))));
    cg2.add(whileNode);
    Assert.assertEquals("while(cond)body;", consumer2.getCode());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node withNode = new Node(Token.WITH, name("scope"), block(expr(name("body"))));
    cg3.add(withNode);
    Assert.assertEquals("with(scope)body;", consumer3.getCode());
  }

  @Test
  public void testGetPropAndGetElem() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // (0).toString
    Node getPropNum = new Node(Token.GETPROP, num(0), str("toString"));
    cg.add(getPropNum);
    Assert.assertEquals("(0).toString", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node getPropName = new Node(Token.GETPROP, name("obj"), str("prop"));
    cg2.add(getPropName);
    Assert.assertEquals("obj.prop", consumer2.getCode());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node getElem = new Node(Token.GETELEM, name("arr"), num(0));
    cg3.add(getElem);
    Assert.assertEquals("arr[0]", consumer3.getCode());
  }

  @Test
  public void testIncAndDec() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node preInc = new Node(Token.INC, name("x"));
    preInc.putIntProp(Node.INCRDECR_PROP, 0);
    cg.add(preInc);
    Assert.assertEquals("++x", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node postInc = new Node(Token.INC, name("x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    cg2.add(postInc);
    Assert.assertEquals("x++", consumer2.getCode());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node preDec = new Node(Token.DEC, name("x"));
    preDec.putIntProp(Node.INCRDECR_PROP, 0);
    cg3.add(preDec);
    Assert.assertEquals("--x", consumer3.getCode());

    TestCodeConsumer consumer4 = new TestCodeConsumer();
    CodeGenerator cg4 = new CodeGenerator(consumer4);
    Node postDec = new Node(Token.DEC, name("x"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    cg4.add(postDec);
    Assert.assertEquals("x--", consumer4.getCode());
  }

  @Test
  public void testCallsAndEval() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node call = new Node(Token.CALL, name("foo"), num(1), num(2));
    cg.add(call);
    Assert.assertEquals("foo(1,2)", consumer.getCode());

    // Indirect eval: eval("x") without DIRECT_EVAL prop
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node indirectEval = new Node(Token.CALL, name("eval"), str("x"));
    cg2.add(indirectEval);
    Assert.assertEquals("(0,eval)(\"x\")", consumer2.getCode());

    // Direct eval: eval("x") with DIRECT_EVAL prop
    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node directEval = new Node(Token.CALL, name("eval"), str("x"));
    directEval.getFirstChild().putBooleanProp(Node.DIRECT_EVAL, true);
    cg3.add(directEval);
    Assert.assertEquals("eval(\"x\")", consumer3.getCode());

    // Free call on getprop: o.p()
    TestCodeConsumer consumer4 = new TestCodeConsumer();
    CodeGenerator cg4 = new CodeGenerator(consumer4);
    Node freeCall = new Node(Token.CALL,
        new Node(Token.GETPROP, name("obj"), str("prop")),
        str("arg"));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    cg4.add(freeCall);
    Assert.assertEquals("(0,obj.prop)(\"arg\")", consumer4.getCode());
  }

  @Test
  public void testIfElseAndDanglingElse() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // if (cond) a;
    Node ifSimple = new Node(Token.IF,
        name("cond"),
        block(expr(name("a"))));
    cg.add(ifSimple);
    Assert.assertEquals("if(cond)a;", consumer.getCode());

    // if (cond) a; else b;
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node ifElse = new Node(Token.IF,
        name("cond"),
        block(expr(name("a"))),
        block(expr(name("b"))));
    cg2.add(ifElse);
    Assert.assertEquals("if(cond)a;else b;", consumer2.getCode());

    // Ambiguous dangling else
    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    cg3.add(ifSimple, CodeGenerator.Context.BEFORE_DANGLING_ELSE);
    Assert.assertEquals("{if(cond)a;}", consumer3.getCode());
  }

  @Test
  public void testConstantsAndKeywords() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    cg.add(new Node(Token.NULL));
    cg.add(new Node(Token.THIS));
    cg.add(new Node(Token.FALSE));
    cg.add(new Node(Token.TRUE));
    cg.add(new Node(Token.DEBUGGER));
    Assert.assertEquals("nullthisfalsetruedebugger;", consumer.getCode());
  }

  @Test
  public void testBreakAndContinue() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    cg.add(new Node(Token.BREAK));
    cg.add(new Node(Token.BREAK, labelName("lbl")));
    cg.add(new Node(Token.CONTINUE));
    cg.add(new Node(Token.CONTINUE, labelName("lbl2")));
    Assert.assertEquals("break;break lbl;continue;continue lbl2;", consumer.getCode());
  }

  @Test
  public void testLabels() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node label = new Node(Token.LABEL,
        labelName("myLabel"),
        block(expr(name("a"))));
    cg.add(label);
    Assert.assertEquals("myLabel:a;", consumer.getCode());
  }

  @Test
  public void testNewOperator() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node newSimple = new Node(Token.NEW, name("Foo"));
    cg.add(newSimple);
    Assert.assertEquals("new Foo", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node newWithArgs = new Node(Token.NEW, name("Foo"), num(1));
    cg2.add(newWithArgs);
    Assert.assertEquals("new Foo(1)", consumer2.getCode());

    // new with call child: new (foo())()
    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node newWithCall = new Node(Token.NEW,
        new Node(Token.CALL, name("foo")),
        num(2));
    cg3.add(newWithCall);
    Assert.assertEquals("new (foo())(2)", consumer3.getCode());
  }

  @Test
  public void testDeleteAndRef() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node del = new Node(Token.DELPROP, new Node(Token.GETPROP, name("o"), str("p")));
    cg.add(del);
    Assert.assertEquals("delete o.p", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node getRef = new Node(Token.GET_REF, name("x"));
    cg2.add(getRef);
    Assert.assertEquals("x", consumer2.getCode());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node refSpecial = new Node(Token.REF_SPECIAL, name("super"));
    refSpecial.putProp(Node.NAME_PROP, "prop");
    cg3.add(refSpecial);
    Assert.assertEquals("super.prop", consumer3.getCode());
  }

  @Test
  public void testSwitchCaseDefault() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node switchNode = new Node(Token.SWITCH,
        name("x"),
        new Node(Token.CASE, num(1), block(expr(name("a")))),
        new Node(Token.DEFAULT, block(expr(name("b")))));
    cg.add(switchNode);
    Assert.assertEquals("switch(x){case 1:a;default:b;}", consumer.getCode());
  }

  @Test
  public void testInOperatorInForContext() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // In operator inside for init clause requires parens: (a in b)
    Node inOp = new Node(Token.IN, name("a"), name("b"));
    Node forNode = new Node(Token.FOR,
        inOp,
        name("cond"),
        name("inc"),
        block(expr(name("body"))));
    cg.add(forNode);
    Assert.assertEquals("for((a in b);cond;inc)body;", consumer.getCode());
  }

  @Test
  public void testBlockSimplificationAndBrowserBugs() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Block containing exactly one function in if statement should be preserved in block
    Node fn = new Node(Token.FUNCTION, name("f"), new Node(Token.LP), block());
    Node blockWithFn = block(fn);
    Node ifNode = new Node(Token.IF, name("cond"), blockWithFn);
    cg.add(ifNode);
    Assert.assertEquals("if(cond){function f(){}}", consumer.getCode());

    // Block containing exactly one do statement inside while
    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    Node doNode = new Node(Token.DO, block(), name("cond"));
    Node blockWithDo = block(doNode);
    Node whileNode = new Node(Token.WHILE, name("c"), blockWithDo);
    cg2.add(whileNode);
    Assert.assertEquals("while(c){do;while(cond);}", consumer2.getCode());

    // Labeled function
    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(consumer3);
    Node labelWithFn = new Node(Token.LABEL, labelName("lbl"), block(fn));
    Node blockWithLabelFn = block(labelWithFn);
    Node ifLabeled = new Node(Token.IF, name("c"), blockWithLabelFn);
    cg3.add(ifLabeled);
    Assert.assertEquals("if(c){lbl:function f(){}}", consumer3.getCode());

    // Empty block in if: preserveExtraBlocks = true vs false
    TestCodeConsumer consumer4 = new TestCodeConsumer();
    consumer4.preserveExtraBlocks = true;
    CodeGenerator cg4 = new CodeGenerator(consumer4);
    Node emptyBlock = block();
    Node ifEmpty = new Node(Token.IF, name("c"), emptyBlock);
    cg4.add(ifEmpty);
    Assert.assertEquals("if(c){}", consumer4.getCode());

    TestCodeConsumer consumer5 = new TestCodeConsumer();
    consumer5.preserveExtraBlocks = false;
    CodeGenerator cg5 = new CodeGenerator(consumer5);
    cg5.add(ifEmpty);
    Assert.assertEquals("if(c);", consumer5.getCode());
  }

  @Test(expected = Error.class)
  public void testExprVoidThrows() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.EXPR_VOID));
  }

  @Test(expected = Error.class)
  public void testUnknownNodeTypeThrows() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(9999));
  }

  @Test
  public void testSetNameIgnored() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.SETNAME));
    Assert.assertEquals("", consumer.getCode());
  }

  @Test
  public void testIsSimpleNumberAndGetSimpleNumber() {
    Assert.assertTrue(CodeGenerator.isSimpleNumber("0"));
    Assert.assertTrue(CodeGenerator.isSimpleNumber("123456789"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(""));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("12a"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("-5"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("1.5"));

    Assert.assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.001);
    Assert.assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.001);
    // Number too large for long
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9999999999999999999999999999")));
    // Number larger than MAX_POSITIVE_INTEGER_NUMBER (2^53 = 9007199254740992)
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9007199254740993")));
  }

  @Test
  public void testStringAndIdentifierEscaping() {
    // Basic escapes
    Assert.assertEquals("\"hello\"", CodeGenerator.escapeToDoubleQuotedJsString("hello"));
    Assert.assertEquals("\"\\x00\\n\\r\\t\\\\\\\"'\"", CodeGenerator.escapeToDoubleQuotedJsString("\0\n\r\t\\\"'"));

    // Quotes selection in jsString
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.addJsString("He said 'hello'"); // single quote inside -> double quoted
    Assert.assertEquals("\"He said 'hello'\"", consumer.getCode());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2);
    cg2.addJsString("He said \"hello\""); // double quote inside -> single quoted
    Assert.assertEquals("'He said \"hello\"'", consumer2.getCode());

    // Caching check
    cg2.addJsString("He said \"hello\"");
    Assert.assertEquals("'He said \"hello\"''He said \"hello\"'", consumer2.getCode());

    // Security escapes: -->, ]]>, </script, <!--
    Assert.assertEquals("\"--\\>\"", CodeGenerator.escapeToDoubleQuotedJsString("-->"));
    Assert.assertEquals("\"]]\\>\"", CodeGenerator.escapeToDoubleQuotedJsString("]]>"));
    Assert.assertEquals("\"<\\/script>\"", CodeGenerator.escapeToDoubleQuotedJsString("</script>"));
    Assert.assertEquals("\"<\\!--\"", CodeGenerator.escapeToDoubleQuotedJsString("<!--"));

    // Regexp escaping
    Assert.assertEquals("/foo\\/bar/gi", CodeGenerator.regexpEscape("foo/bar", null) + "gi");

    // Identifier escaping
    Assert.assertEquals("validId", CodeGenerator.identifierEscape("validId"));
    Assert.assertEquals("valid_123", CodeGenerator.identifierEscape("valid_123"));
    Assert.assertEquals("a\\u00e9b", CodeGenerator.identifierEscape("a\u00e9b"));

    // Output charset encoder
    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String escapedUtf8 = CodeGenerator.strEscape("h\u00e9llo", '"', "\\\"", "'", "\\\\", utf8Encoder);
    Assert.assertEquals("\"h\u00e9llo\"", escapedUtf8);

    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String escapedAscii = CodeGenerator.strEscape("h\u00e9llo", '"', "\\\"", "'", "\\\\", asciiEncoder);
    Assert.assertEquals("\"h\\u00e9llo\"", escapedAscii);

    // Supplementary code point (Surrogate pair) e.g. U+1F600 (GRINNING FACE)
    String surrogateStr = new String(Character.toChars(0x1F600));
    String escapedSurrogate = CodeGenerator.strEscape(surrogateStr, '"', "\\\"", "'", "\\\\", asciiEncoder);
    Assert.assertEquals("\"\\ud83d\\ude00\"", escapedSurrogate);
  }
}