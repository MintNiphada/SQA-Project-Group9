package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CodeGeneratorTest {

  private static class MockCodeConsumer extends CodeConsumer {
    private final StringBuilder sb = new StringBuilder();
    boolean continueProc = true;
    boolean preserveExtra = false;

    @Override
    void append(String str) {
      sb.append(str);
    }

    @Override
    char getLastChar() {
      return sb.length() > 0 ? sb.charAt(sb.length() - 1) : '\0';
    }

    @Override
    boolean continueProcessing() {
      return continueProc;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtra;
    }

    @Override
    boolean breakAfterBlockFor(Node n, boolean statementContext) {
      return false;
    }

    String getOutput() {
      return sb.toString();
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
  public void testJsStringEscapes() {
    Assert.assertEquals("\"hello\"", CodeGenerator.jsString("hello", null));
    Assert.assertEquals("\"a\\\"b\"", CodeGenerator.jsString("a\"b", null));
    Assert.assertEquals("'a\"b\\\'c\"'", CodeGenerator.jsString("a\"b'c\"", null));
    Assert.assertEquals("\"a\\nb\\rc\\td\\\\e\"", CodeGenerator.jsString("a\nb\rc\td\\e", null));
    Assert.assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));
    Assert.assertEquals("\"]]\u005c>\"", CodeGenerator.jsString("]]>", null));
    Assert.assertEquals("\"<\\/script>\"", CodeGenerator.jsString("</script>", null));
    Assert.assertEquals("\"<\\/SCRIPT>\"", CodeGenerator.jsString("</SCRIPT>", null));
    Assert.assertEquals("\"\\u0000\\u001f\"", CodeGenerator.jsString("\u0000\u001F", null));
  }

  @Test
  public void testJsStringWithCharsetEncoder() {
    Charset latin1 = StandardCharsets.ISO_8859_1;
    String res = CodeGenerator.jsString("\u00e9\u4e16", latin1.newEncoder());
    Assert.assertTrue(res.contains("\u00e9"));
    Assert.assertTrue(res.contains("\\u4e16"));
  }

  @Test
  public void testRegexpEscape() {
    Assert.assertEquals("/abc/g", CodeGenerator.regexpEscape("abc") + "g");
    Assert.assertEquals("/a\\/b/", CodeGenerator.regexpEscape("a/b"));
    Assert.assertEquals("/--\\>/", CodeGenerator.regexpEscape("-->", null));
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    String res = CodeGenerator.escapeToDoubleQuotedJsString("a\"b'c");
    Assert.assertEquals("\"a\\\"b'c\"", res);
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("foo", CodeGenerator.identifierEscape("foo"));
    Assert.assertEquals("\\u4e16界", CodeGenerator.identifierEscape("\u4e16界").substring(0, 6) + "界");
    Assert.assertEquals("\\u001f", CodeGenerator.identifierEscape("\u001F"));
  }

  @Test
  public void testAddString() {
    generator.add("var x = 1;");
    Assert.assertEquals("var x = 1;", consumer.getOutput());
  }

  @Test
  public void testContinueProcessingFalse() {
    consumer.continueProc = false;
    generator.add(Node.newNumber(42));
    Assert.assertEquals("", consumer.getOutput());
  }

  @Test
  public void testBinaryOperatorAssociativity() {
    Node left = Node.newNumber(1);
    Node right = Node.newNumber(2);
    Node addNode = new Node(Token.ADD, left, right);
    Node root = new Node(Token.ADD, addNode, Node.newNumber(3));
    generator.add(root);
    Assert.assertTrue(consumer.getOutput().contains("+"));
  }

  @Test
  public void testBinaryOperatorAssignment() {
    Node target = Node.newString(Token.NAME, "a");
    Node value = Node.newString(Token.NAME, "b");
    Node assign2 = new Node(Token.ASSIGN, value, Node.newNumber(2));
    Node assign1 = new Node(Token.ASSIGN, target, assign2);
    generator.add(assign1);
    Assert.assertTrue(consumer.getOutput().contains("="));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchVar = Node.newString(Token.NAME, "e");
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, catchVar, new Node(Token.EMPTY), catchBody);
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    generator.add(tryNode);
    Assert.assertTrue(consumer.getOutput().contains("try"));
    Assert.assertTrue(consumer.getOutput().contains("catch"));
    Assert.assertTrue(consumer.getOutput().contains("finally"));
  }

  @Test(expected = Error.class)
  public void testCatchWithConditionThrows() {
    Node catchVar = Node.newString(Token.NAME, "e");
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, catchVar, Node.newString(Token.NAME, "cond"), catchBody);
    generator.add(catchNode);
  }

  @Test
  public void testThrowAndReturn() {
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "e"));
    generator.add(throwNode);
    Assert.assertTrue(consumer.getOutput().contains("throw"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node retNode = new Node(Token.RETURN);
    generator.add(retNode);
    Assert.assertTrue(consumer.getOutput().contains("return"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node retValNode = new Node(Token.RETURN, Node.newNumber(1));
    generator.add(retValNode);
    Assert.assertTrue(consumer.getOutput().contains("return"));
  }

  @Test
  public void testVarAndName() {
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToFront(Node.newNumber(10));
    Node varNode = new Node(Token.VAR, nameNode);
    generator.add(varNode);
    Assert.assertTrue(consumer.getOutput().contains("var "));
    Assert.assertTrue(consumer.getOutput().contains("x"));
  }

  @Test
  public void testNameWithCommaChild() {
    Node nameNode = Node.newString(Token.NAME, "x");
    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    nameNode.addChildToFront(comma);
    generator.add(nameNode);
    Assert.assertTrue(consumer.getOutput().contains("x"));
  }

  @Test
  public void testArrayLitWithSkipIndexes() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    arrayLit.putProp(Node.SKIP_INDEXES_PROP, new int[]{0});
    generator.add(arrayLit);
    Assert.assertTrue(consumer.getOutput().contains("["));
    Assert.assertTrue(consumer.getOutput().contains("]"));
  }

  @Test
  public void testParenthesesAndComma() {
    Node lp = new Node(Token.LP, Node.newNumber(1), Node.newNumber(2));
    generator.add(lp);
    Assert.assertTrue(consumer.getOutput().contains("("));
    Assert.assertTrue(consumer.getOutput().contains(")"));
  }

  @Test
  public void testUnaryOps() {
    int[] ops = {Token.TYPEOF, Token.VOID, Token.NOT, Token.BITNOT, Token.POS, Token.NEG};
    for (int op : ops) {
      consumer = new MockCodeConsumer();
      generator = new CodeGenerator(consumer);
      Node uNode = new Node(op, Node.newNumber(5));
      generator.add(uNode);
      Assert.assertFalse(consumer.getOutput().isEmpty());
    }
  }

  @Test
  public void testHook() {
    Node hook = new Node(Token.HOOK, Node.newString(Token.NAME, "a"), Node.newNumber(1), Node.newNumber(2));
    generator.add(hook);
    Assert.assertTrue(consumer.getOutput().contains("?"));
    Assert.assertTrue(consumer.getOutput().contains(":"));
  }

  @Test
  public void testRegexpNode() {
    Node regex = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("g"));
    generator.add(regex);
    Assert.assertTrue(consumer.getOutput().contains("/abc/g"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node regex1 = new Node(Token.REGEXP, Node.newString("xyz"));
    generator.add(regex1);
    Assert.assertTrue(consumer.getOutput().contains("/xyz/"));
  }

  @Test(expected = Error.class)
  public void testRegexpNonStringChildrenThrows() {
    Node regex = new Node(Token.REGEXP, Node.newNumber(1));
    generator.add(regex);
  }

  @Test
  public void testRefSpecialAndGetRef() {
    Node refSpecial = new Node(Token.REF_SPECIAL, Node.newString(Token.NAME, "obj"));
    refSpecial.putProp(Node.NAME_PROP, "prop");
    generator.add(refSpecial);
    Assert.assertTrue(consumer.getOutput().contains("obj.prop"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node getRef = new Node(Token.GET_REF, Node.newString(Token.NAME, "target"));
    generator.add(getRef);
    Assert.assertTrue(consumer.getOutput().contains("target"));
  }

  @Test
  public void testFunction() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    generator.add(fn, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertTrue(consumer.getOutput().contains("function"));
  }

  @Test
  public void testScriptAndBlock() {
    Node script = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    Node fnNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "g"), new Node(Token.LP), new Node(Token.BLOCK));
    script.addChildToBack(varNode);
    script.addChildToBack(fnNode);
    generator.add(script);
    Assert.assertTrue(consumer.getOutput().contains("var"));
    Assert.assertTrue(consumer.getOutput().contains("function"));
  }

  @Test
  public void testForLoops() {
    Node for4 = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "inc"),
        new Node(Token.BLOCK));
    generator.add(for4);
    Assert.assertTrue(consumer.getOutput().contains("for("));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "k"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK));
    generator.add(forIn);
    Assert.assertTrue(consumer.getOutput().contains("in"));
  }

  @Test
  public void testDoAndWhile() {
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "cond"));
    generator.add(doNode);
    Assert.assertTrue(consumer.getOutput().contains("do"));
    Assert.assertTrue(consumer.getOutput().contains("while"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK));
    generator.add(whileNode);
    Assert.assertTrue(consumer.getOutput().contains("while"));
  }

  @Test
  public void testGetPropAndGetElem() {
    Node getProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("toString"));
    generator.add(getProp);
    Assert.assertTrue(consumer.getOutput().contains("(1).toString"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "arr"), Node.newNumber(0));
    generator.add(getElem);
    Assert.assertTrue(consumer.getOutput().contains("arr[0]"));
  }

  @Test
  public void testWithAndIncDec() {
    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "ctx"), new Node(Token.BLOCK));
    generator.add(withNode);
    Assert.assertTrue(consumer.getOutput().contains("with"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node incPre = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    generator.add(incPre);
    Assert.assertTrue(consumer.getOutput().contains("++x"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node decPost = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    decPost.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(decPost);
    Assert.assertTrue(consumer.getOutput().contains("x--"));
  }

  @Test
  public void testCallNode() {
    Node evalDirect = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1"));
    evalDirect.getFirstChild().putBooleanProp(Node.DIRECT_EVAL, true);
    generator.add(evalDirect);
    Assert.assertTrue(consumer.getOutput().contains("eval(\"1\")"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node evalIndirect = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1"));
    generator.add(evalIndirect);
    Assert.assertTrue(consumer.getOutput().contains("(0,eval)(\"1\")"));
  }

  @Test
  public void testIfElse() {
    Node ifElse = new Node(Token.IF,
        Node.newString(Token.NAME, "c"),
        new Node(Token.BLOCK),
        new Node(Token.BLOCK));
    generator.add(ifElse);
    Assert.assertTrue(consumer.getOutput().contains("if"));
    Assert.assertTrue(consumer.getOutput().contains("else"));
  }

  @Test
  public void testLiterals() {
    int[] literals = {Token.NULL, Token.THIS, Token.FALSE, Token.TRUE, Token.DEBUGGER};
    for (int lit : literals) {
      consumer = new MockCodeConsumer();
      generator = new CodeGenerator(consumer);
      generator.add(new Node(lit));
      Assert.assertFalse(consumer.getOutput().isEmpty());
    }
  }

  @Test
  public void testBreakContinueAndLabel() {
    Node labelName = Node.newString(Token.LABEL_NAME, "lbl");
    Node breakNode = new Node(Token.BREAK, labelName);
    generator.add(breakNode);
    Assert.assertTrue(consumer.getOutput().contains("break lbl"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node contNode = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "lbl"));
    generator.add(contNode);
    Assert.assertTrue(consumer.getOutput().contains("continue lbl"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node labelNode = new Node(Token.LABEL, Node.newString(Token.LABEL_NAME, "myLabel"), new Node(Token.BLOCK));
    generator.add(labelNode);
    Assert.assertTrue(consumer.getOutput().contains("myLabel:"));
  }

  @Test
  public void testNewNode() {
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node newNode = new Node(Token.NEW, callTarget, Node.newNumber(1));
    generator.add(newNode);
    Assert.assertTrue(consumer.getOutput().contains("new "));
  }

  @Test
  public void testDelPropAndExprResult() {
    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    generator.add(del);
    Assert.assertTrue(consumer.getOutput().contains("delete x"));

    consumer = new MockCodeConsumer();
    generator = new CodeGenerator(consumer);
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(10));
    generator.add(expr);
    Assert.assertTrue(consumer.getOutput().contains("10"));
  }

  @Test
  public void testObjectLit() {
    Node obj = new Node(Token.OBJECTLIT,
        Node.newString("a"), Node.newNumber(1),
        Node.newString("default"), Node.newNumber(2));
    generator.add(obj, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertTrue(consumer.getOutput().contains("{"));
    Assert.assertTrue(consumer.getOutput().contains("}"));
  }

  @Test
  public void testSwitchCaseDefault() {
    Node caseNode = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node defNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), caseNode, defNode);
    generator.add(switchNode);
    Assert.assertTrue(consumer.getOutput().contains("switch(x)"));
    Assert.assertTrue(consumer.getOutput().contains("case 1:"));
    Assert.assertTrue(consumer.getOutput().contains("default:"));
  }

  @Test
  public void testSetNameNodeIgnored() {
    generator.add(new Node(Token.SETNAME));
    Assert.assertEquals("", consumer.getOutput());
  }

  @Test(expected = Error.class)
  public void testExprVoidThrows() {
    generator.add(new Node(Token.EXPR_VOID));
  }

  @Test(expected = Error.class)
  public void testUnknownTypeThrows() {
    generator.add(new Node(999999));
  }

  @Test
  public void testCharsetConstructors() {
    CodeGenerator cgAscii = new CodeGenerator(consumer, Charsets.US_ASCII);
    cgAscii.add("abc");
    Assert.assertEquals("abc", consumer.getOutput());

    CodeGenerator cgUtf8 = new CodeGenerator(consumer, StandardCharsets.UTF_8);
    cgUtf8.add("def");
    Assert.assertEquals("abcdef", consumer.getOutput());
  }
}
