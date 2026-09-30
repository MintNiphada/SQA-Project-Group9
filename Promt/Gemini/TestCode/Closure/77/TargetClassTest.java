package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

class CodeGeneratorTest {

  private static class TestCodeConsumer extends CodeConsumer {
    final StringBuilder buffer = new StringBuilder();

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String op) {
      buffer.append(op);
    }

    String getOutput() {
      return buffer.toString();
    }

    void clear() {
      buffer.setLength(0);
    }
  }

  private TestCodeConsumer consumer;
  private CodeGenerator generator;

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
    generator = new CodeGenerator(consumer);
  }

  @Test
  public void testTagAsStrict() {
    generator.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer.getOutput());
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
  public void testJsStringOptimalQuoteSelection() {
    String singleMore = "a'b\"c\"d";
    String doubleMore = "a'b'c\"d";
    String equalCount = "a'b\"c";

    String resSingle = CodeGenerator.jsString(singleMore, null);
    Assert.assertTrue(resSingle.startsWith("'") && resSingle.endsWith("'"));

    String resDouble = CodeGenerator.jsString(doubleMore, null);
    Assert.assertTrue(resDouble.startsWith("\"") && resDouble.endsWith("\""));

    String resEqual = CodeGenerator.jsString(equalCount, null);
    Assert.assertTrue(resEqual.startsWith("\"") && resEqual.endsWith("\""));
  }

  @Test
  public void testJsStringEscapes() {
    String raw = "\n\r\t\\\"\'";
    String escaped = CodeGenerator.jsString(raw, null);
    Assert.assertTrue(escaped.contains("\\n"));
    Assert.assertTrue(escaped.contains("\\r"));
    Assert.assertTrue(escaped.contains("\\t"));
    Assert.assertTrue(escaped.contains("\\\\"));
  }

  @Test
  public void testHtmlTagEscapes() {
    String scriptEnd = "test</script>end";
    String res = CodeGenerator.jsString(scriptEnd, null);
    Assert.assertTrue(res.contains("<\\/script"));

    String commentStart = "test<!--end";
    res = CodeGenerator.jsString(commentStart, null);
    Assert.assertTrue(res.contains("<\\!--"));

    String dashDashClose = "test-->end";
    res = CodeGenerator.jsString(dashDashClose, null);
    Assert.assertTrue(res.contains("--\\>"));

    String cdataClose = "test]]>end";
    res = CodeGenerator.jsString(cdataClose, null);
    Assert.assertTrue(res.contains("]]\\>"));

    String normalTag = "<div>test</div>";
    res = CodeGenerator.jsString(normalTag, null);
    Assert.assertTrue(res.contains("<div>"));
  }

  @Test
  public void testCharsetEncoderHandling() {
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String unicode = "\u00e9\u3042"; // e-acute, hiragana 'a'
    String resAscii = CodeGenerator.jsString(unicode, asciiEncoder);
    Assert.assertTrue(resAscii.contains("\\u00e9"));
    Assert.assertTrue(resAscii.contains("\\u3042"));

    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String resUtf8 = CodeGenerator.jsString(unicode, utf8Encoder);
    Assert.assertTrue(resUtf8.contains("\u00e9"));
    Assert.assertTrue(resUtf8.contains("\u3042"));
  }

  @Test
  public void testSupplementaryCodePoints() {
    // Code point 0x1F600 (Grinning Face emoji)
    String emoji = new String(Character.toChars(0x1F600));
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String escaped = CodeGenerator.jsString(emoji, asciiEncoder);
    Assert.assertTrue(escaped.contains("\\ud83d\\ude00"));
  }

  @Test
  public void testRegexpEscape() {
    String regex = "abc/def\n";
    String escaped = CodeGenerator.regexpEscape(regex);
    Assert.assertTrue(escaped.startsWith("/"));
    Assert.assertTrue(escaped.endsWith("/"));
    Assert.assertTrue(escaped.contains("\\n"));

    String escaped2 = CodeGenerator.regexpEscape(regex, Charsets.US_ASCII.newEncoder());
    Assert.assertTrue(escaped2.startsWith("/"));
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    String input = "a\"b\'c\n";
    String res = CodeGenerator.escapeToDoubleQuotedJsString(input);
    Assert.assertTrue(res.startsWith("\""));
    Assert.assertTrue(res.endsWith("\""));
    Assert.assertTrue(res.contains("\\\""));
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("simpleIdent", CodeGenerator.identifierEscape("simpleIdent"));
    String nonLatin = "\u00e9_var";
    String escaped = CodeGenerator.identifierEscape(nonLatin);
    Assert.assertTrue(escaped.startsWith("\\u00e9"));

    String controlChar = "\u0005var";
    Assert.assertTrue(CodeGenerator.identifierEscape(controlChar).startsWith("\\u0005"));
  }

  @Test
  public void testBinaryOperators() {
    Node left = Node.newString(Token.NAME, "a");
    Node right = Node.newString(Token.NAME, "b");
    Node add = new Node(Token.ADD, left, right);
    generator.add(add);
    Assert.assertEquals("a+b", consumer.getOutput());

    consumer.clear();
    Node assign1 = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.ASSIGN, Node.newString(Token.NAME, "y"), Node.newNumber(1)));
    generator.add(assign1);
    Assert.assertEquals("x=y=1", consumer.getOutput());

    consumer.clear();
    Node mul = new Node(Token.MUL, Node.newString(Token.NAME, "a"),
        new Node(Token.MUL, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c")));
    generator.add(mul);
    Assert.assertEquals("a*b*c", consumer.getOutput());
  }

  @Test(expected = IllegalStateException.class)
  public void testBadBinaryOperatorChildCount() {
    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "a"));
    generator.add(add);
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x")));
    Node catchBlock = new Node(Token.CATCH, Node.newString(Token.NAME, "e"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "y"))));
    Node catchContainer = new Node(Token.BLOCK, catchBlock);
    Node finallyBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "z")));

    Node tryNode = new Node(Token.TRY, tryBlock, catchContainer, finallyBlock);
    generator.add(tryNode);
    String out = consumer.getOutput();
    Assert.assertTrue(out.contains("try"));
    Assert.assertTrue(out.contains("catch(e)"));
    Assert.assertTrue(out.contains("finally"));

    consumer.clear();
    Node tryNoCatch = new Node(Token.TRY, tryBlock.cloneTree(), new Node(Token.BLOCK), finallyBlock.cloneTree());
    generator.add(tryNoCatch);
    Assert.assertTrue(consumer.getOutput().contains("try"));
    Assert.assertTrue(consumer.getOutput().contains("finally"));
  }

  @Test
  public void testThrowAndReturn() {
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "err"));
    generator.add(throwNode);
    Assert.assertEquals("throw err;", consumer.getOutput());

    consumer.clear();
    Node returnNode = new Node(Token.RETURN, Node.newNumber(5));
    generator.add(returnNode);
    Assert.assertEquals("return 5;", consumer.getOutput());

    consumer.clear();
    Node emptyReturn = new Node(Token.RETURN);
    generator.add(emptyReturn);
    Assert.assertEquals("return;", consumer.getOutput());
  }

  @Test
  public void testVarAndName() {
    Node varNode = new Node(Token.VAR);
    Node name1 = Node.newString(Token.NAME, "a");
    Node name2 = Node.newString(Token.NAME, "b");
    name2.addChildToBack(Node.newNumber(10));
    varNode.addChildToBack(name1);
    varNode.addChildToBack(name2);

    generator.add(varNode);
    Assert.assertEquals("var a,b=10", consumer.getOutput());

    consumer.clear();
    Node commaAssign = Node.newString(Token.NAME, "c");
    commaAssign.addChildToBack(new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2)));
    generator.add(commaAssign);
    Assert.assertEquals("c=(1,2)", consumer.getOutput());
  }

  @Test
  public void testArrayAndList() {
    Node arrayLit = new Node(Token.ARRAYLIT);
    arrayLit.addChildToBack(Node.newNumber(1));
    arrayLit.addChildToBack(new Node(Token.EMPTY));
    arrayLit.addChildToBack(Node.newNumber(3));
    generator.add(arrayLit);
    Assert.assertEquals("[1,,3]", consumer.getOutput());

    consumer.clear();
    Node lp = new Node(Token.LP, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    generator.add(lp);
    Assert.assertEquals("(a,b)", consumer.getOutput());

    consumer.clear();
    Node comma = new Node(Token.COMMA, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    generator.add(comma);
    Assert.assertEquals("a,b", consumer.getOutput());
  }

  @Test
  public void testUnaryOperators() {
    int[] unaries = {Token.TYPEOF, Token.VOID, Token.NOT, Token.BITNOT, Token.POS};
    for (int op : unaries) {
      consumer.clear();
      Node n = new Node(op, Node.newString(Token.NAME, "x"));
      generator.add(n);
      Assert.assertFalse(consumer.getOutput().isEmpty());
    }

    consumer.clear();
    Node negNum = new Node(Token.NEG, Node.newNumber(5));
    generator.add(negNum);
    Assert.assertEquals("-5", consumer.getOutput());

    consumer.clear();
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    generator.add(negVar);
    Assert.assertEquals("-x", consumer.getOutput());
  }

  @Test
  public void testIncDec() {
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    preInc.putIntProp(Node.INCRDECR_PROP, 0);
    generator.add(preInc);
    Assert.assertEquals("++x", consumer.getOutput());

    consumer.clear();
    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postInc);
    Assert.assertEquals("x++", consumer.getOutput());

    consumer.clear();
    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postDec);
    Assert.assertEquals("x--", consumer.getOutput());
  }

  @Test
  public void testHook() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    generator.add(hook);
    Assert.assertEquals("cond?a:b", consumer.getOutput());
  }

  @Test
  public void testRegexpNode() {
    Node regex1 = new Node(Token.REGEXP, Node.newString("abc"));
    generator.add(regex1);
    Assert.assertEquals("/abc/", consumer.getOutput());

    consumer.clear();
    Node regex2 = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("gi"));
    generator.add(regex2);
    Assert.assertEquals("/abc/gi", consumer.getOutput());
  }

  @Test
  public void testFunction() {
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.LP),
        new Node(Token.BLOCK));
    generator.add(fn, CodeGenerator.Context.STATEMENT);
    Assert.assertEquals("function foo(){}", consumer.getOutput());

    consumer.clear();
    generator.add(fn, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("(function foo(){})", consumer.getOutput());
  }

  @Test
  public void testGetAndSet() {
    Node obj = new Node(Token.OBJECTLIT);

    Node getterFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        new Node(Token.BLOCK));
    Node getter = Node.newString(Token.GET, "p");
    getter.addChildToBack(getterFn);
    obj.addChildToBack(getter);

    Node setterFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP, Node.newString(Token.NAME, "val")),
        new Node(Token.BLOCK));
    Node setter = Node.newString(Token.SET, "p");
    setter.addChildToBack(setterFn);
    obj.addChildToBack(setter);

    generator.add(obj);
    Assert.assertEquals("{get p(){},set p(val){}}", consumer.getOutput());
  }

  @Test
  public void testScriptAndBlock() {
    Node script = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    script.addChildToBack(varNode);
    generator.add(script);
    Assert.assertEquals("var x;", consumer.getOutput());

    consumer.clear();
    Node block = new Node(Token.BLOCK,
        new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a")),
        new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b")));
    generator.add(block, CodeGenerator.Context.PRESERVE_BLOCK);
    Assert.assertEquals("{a;b;}", consumer.getOutput());
  }

  @Test
  public void testForLoops() {
    Node for4 = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        Node.newString(Token.NAME, "cond"),
        new Node(Token.INC, Node.newString(Token.NAME, "i")),
        new Node(Token.BLOCK));
    generator.add(for4);
    Assert.assertEquals("for(var i;cond;i++;);", consumer.getOutput());

    consumer.clear();
    Node for3 = new Node(Token.FOR,
        Node.newString(Token.NAME, "k"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK));
    generator.add(for3);
    Assert.assertEquals("for(k in obj);", consumer.getOutput());
  }

  @Test
  public void testDoAndWhile() {
    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK),
        Node.newString(Token.NAME, "cond"));
    generator.add(doNode);
    Assert.assertEquals("do;while(cond);", consumer.getOutput());

    consumer.clear();
    Node whileNode = new Node(Token.WHILE,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK));
    generator.add(whileNode);
    Assert.assertEquals("while(cond);", consumer.getOutput());
  }

  @Test
  public void testGetPropAndGetElem() {
    Node numProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("toString"));
    generator.add(numProp);
    Assert.assertEquals("(1).toString", consumer.getOutput());

    consumer.clear();
    Node varProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("prop"));
    generator.add(varProp);
    Assert.assertEquals("obj.prop", consumer.getOutput());

    consumer.clear();
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "arr"), Node.newNumber(0));
    generator.add(getElem);
    Assert.assertEquals("arr[0]", consumer.getOutput());
  }

  @Test
  public void testCallNode() {
    Node directEval = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1"));
    directEval.putBooleanProp(Node.DIRECT_EVAL, true);
    generator.add(directEval);
    Assert.assertEquals("eval(\"1\")", consumer.getOutput());

    consumer.clear();
    Node indirectEval = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1"));
    generator.add(indirectEval);
    Assert.assertEquals("(0,eval)(\"1\")", consumer.getOutput());

    consumer.clear();
    Node freeCall = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b")));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    generator.add(freeCall);
    Assert.assertEquals("(0,a.b)()", consumer.getOutput());
  }

  @Test
  public void testIfElse() {
    Node ifNode = new Node(Token.IF,
        Node.newString(Token.NAME, "c"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))));
    generator.add(ifNode);
    Assert.assertEquals("if(c)x;", consumer.getOutput());

    consumer.clear();
    Node ifElse = new Node(Token.IF,
        Node.newString(Token.NAME, "c"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "y"))));
    generator.add(ifElse);
    Assert.assertEquals("if(c)x;else y;", consumer.getOutput());
  }

  @Test
  public void testLiteralsAndKeywords() {
    generator.add(new Node(Token.NULL));
    Assert.assertEquals("null", consumer.getOutput());

    consumer.clear();
    generator.add(new Node(Token.THIS));
    Assert.assertEquals("this", consumer.getOutput());

    consumer.clear();
    generator.add(new Node(Token.FALSE));
    Assert.assertEquals("false", consumer.getOutput());

    consumer.clear();
    generator.add(new Node(Token.TRUE));
    Assert.assertEquals("true", consumer.getOutput());

    consumer.clear();
    generator.add(new Node(Token.DEBUGGER));
    Assert.assertEquals("debugger;", consumer.getOutput());

    consumer.clear();
    generator.add(new Node(Token.EMPTY));
    Assert.assertEquals("", consumer.getOutput());
  }

  @Test
  public void testBreakAndContinue() {
    generator.add(new Node(Token.BREAK));
    Assert.assertEquals("break;", consumer.getOutput());

    consumer.clear();
    Node breakLabel = new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "loop"));
    generator.add(breakLabel);
    Assert.assertEquals("break loop;", consumer.getOutput());

    consumer.clear();
    generator.add(new Node(Token.CONTINUE));
    Assert.assertEquals("continue;", consumer.getOutput());

    consumer.clear();
    Node contLabel = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "loop"));
    generator.add(contLabel);
    Assert.assertEquals("continue loop;", consumer.getOutput());
  }

  @Test
  public void testNewNode() {
    Node newNoArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    generator.add(newNoArgs);
    Assert.assertEquals("new Foo", consumer.getOutput());

    consumer.clear();
    Node newWithArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1));
    generator.add(newWithArgs);
    Assert.assertEquals("new Foo(1)", consumer.getOutput());
  }

  @Test
  public void testObjectLit() {
    Node obj = new Node(Token.OBJECTLIT);
    Node prop1 = Node.newString("a");
    prop1.addChildToBack(Node.newNumber(1));
    Node prop2 = Node.newString("default"); // keyword
    prop2.addChildToBack(Node.newNumber(2));
    obj.addChildToBack(prop1);
    obj.addChildToBack(prop2);

    generator.add(obj, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("({a:1,\"default\":2})", consumer.getOutput());
  }

  @Test
  public void testSwitchCaseDefault() {
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"));
    Node caseNode = new Node(Token.CASE, Node.newNumber(1),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    Node defNode = new Node(Token.DEFAULT,
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"))));
    switchNode.addChildToBack(caseNode);
    switchNode.addChildToBack(defNode);

    generator.add(switchNode);
    String out = consumer.getOutput();
    Assert.assertTrue(out.startsWith("switch(x){"));
    Assert.assertTrue(out.contains("case 1:a;"));
    Assert.assertTrue(out.contains("default:b;"));
  }

  @Test
  public void testLabelAndWith() {
    Node label = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "myLabel"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))));
    generator.add(label);
    Assert.assertEquals("myLabel:x;", consumer.getOutput());

    consumer.clear();
    Node withNode = new Node(Token.WITH,
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))));
    generator.add(withNode);
    Assert.assertEquals("with(obj)x;", consumer.getOutput());
  }

  @Test
  public void testDelPropAndRefs() {
    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    generator.add(del);
    Assert.assertEquals("delete x", consumer.getOutput());

    consumer.clear();
    Node getRef = new Node(Token.GET_REF, Node.newString(Token.NAME, "y"));
    generator.add(getRef);
    Assert.assertEquals("y", consumer.getOutput());

    consumer.clear();
    Node refSpecial = new Node(Token.REF_SPECIAL, Node.newString(Token.NAME, "z"));
    refSpecial.putProp(Node.NAME_PROP, "prototype");
    generator.add(refSpecial);
    Assert.assertEquals("z.prototype", consumer.getOutput());
  }

  @Test
  public void testContextHandling() {
    Node inNode = new Node(Token.IN, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Node forInit = new Node(Token.FOR,
        inNode,
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "inc"),
        new Node(Token.BLOCK));
    generator.add(forInit);
    Assert.assertEquals("for((a in b);cond;inc;);", consumer.getOutput());
  }

  @Test
  public void testAddListAndAddAllSiblings() {
    Node n1 = Node.newString(Token.NAME, "a");
    Node n2 = Node.newString(Token.NAME, "b");
    Node parent = new Node(Token.LP, n1, n2);

    generator.addList(parent.getFirstChild());
    Assert.assertEquals("a,b", consumer.getOutput());

    consumer.clear();
    generator.addAllSiblings(parent.getFirstChild());
    Assert.assertEquals("ab", consumer.getOutput());
  }

  @Test(expected = Error.class)
  public void testExprVoidThrows() {
    generator.add(new Node(Token.EXPR_VOID));
  }
}