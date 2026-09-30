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

  private TestCodeConsumer consumer;
  private CodeGenerator generator;

  private static class TestCodeConsumer extends CodeConsumer {
    final StringBuilder buffer = new StringBuilder();
    boolean continueProcessing = true;
    boolean preserveExtraBlocks = false;

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    @Override
    char getLastChar() {
      return buffer.length() > 0 ? buffer.charAt(buffer.length() - 1) : '\0';
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
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    String getCode() {
      return buffer.toString();
    }
  }

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
    generator = new CodeGenerator(consumer);
  }

  @Test
  public void testTagAsStrict() {
    generator.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer.getCode());
  }

  @Test
  public void testIsSimpleNumber() {
    Assert.assertTrue(CodeGenerator.isSimpleNumber("0"));
    Assert.assertTrue(CodeGenerator.isSimpleNumber("123456789"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(""));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("-5"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(" 5"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("01.5"));
  }

  @Test
  public void testGetSimpleNumber() {
    Assert.assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    Assert.assertEquals(12345.0, CodeGenerator.getSimpleNumber("12345"), 0.0);
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9223372036854775807")));
  }

  @Test
  public void testJsStringQuoteSelection() {
    // Single quotes inside -> double-quoted string
    String res1 = CodeGenerator.jsString("It's a test", null);
    Assert.assertEquals("\"It's a test\"", res1);

    // Double quotes inside -> single-quoted string
    String res2 = CodeGenerator.jsString("Say \"Hello\"", null);
    Assert.assertEquals("'Say \"Hello\"'", res2);

    // Both quotes, more double quotes -> single-quoted
    String res3 = CodeGenerator.jsString("\"\"'", null);
    Assert.assertEquals("'\"\"\\''", res3);

    // Both quotes, more single quotes -> double-quoted
    String res4 = CodeGenerator.jsString("''\"", null);
    Assert.assertEquals("\"''\\\"\"", res4);
  }

  @Test
  public void testStrEscapeSpecialCharacters() {
    Assert.assertEquals("\"\\0\\n\\r\\t\\\\\"", CodeGenerator.jsString("\0\n\r\t\\", null));
    Assert.assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));
    Assert.assertEquals("\"]]>\"", CodeGenerator.jsString("]]>", null));
    Assert.assertEquals("\"<\\/script\"", CodeGenerator.jsString("</script", null));
    Assert.assertEquals("\"<\\/SCRIPT\"", CodeGenerator.jsString("</SCRIPT", null));
    Assert.assertEquals("\"<\\!--\"", CodeGenerator.jsString("<!--", null));
    Assert.assertEquals("\"normal>text<notscript\"", CodeGenerator.jsString("normal>text<notscript", null));
  }

  @Test
  public void testStrEscapeUnicodeAndAsciiRange() {
    // 0x7f DEL character handling
    String delEscaped = CodeGenerator.jsString("\u007f", null);
    Assert.assertTrue(delEscaped.contains("\\u007f") || delEscaped.contains("\u007f"));

    // Printable ascii range
    Assert.assertEquals("\"abcXYZ123 ~\"", CodeGenerator.jsString("abcXYZ123 ~", null));

    // Non-ascii with default (null encoder)
    String nonAscii = CodeGenerator.jsString("\u00e9\u3042", null);
    Assert.assertEquals("\"\\u00e9\\u3042\"", nonAscii);

    // With UTF-8 encoder
    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String utf8Str = CodeGenerator.jsString("\u00e9\u3042", utf8Encoder);
    Assert.assertEquals("\"\u00e9\u3042\"", utf8Str);

    // With US_ASCII encoder
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String asciiStr = CodeGenerator.jsString("\u00e9", asciiEncoder);
    Assert.assertEquals("\"\\u00e9\"", asciiStr);
  }

  @Test
  public void testSupplementaryCodePointEscaping() {
    // Supplementary character (Surrogate pair e.g. U+1F600 😀)
    String emoji = "\uD83D\uDE00";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(emoji);
    Assert.assertEquals("\"\\ud83d\\ude00\"", escaped);
  }

  @Test
  public void testRegexpEscape() {
    Assert.assertEquals("/abc/g", CodeGenerator.regexpEscape("abc", null) + "g");
    Assert.assertEquals("/a\\/b/", "/" + CodeGenerator.regexpEscape("a/b") + "/");
    Assert.assertEquals("/<\\/script/", CodeGenerator.regexpEscape("</script"));
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("myVariable123", CodeGenerator.identifierEscape("myVariable123"));
    Assert.assertEquals("var_\\u00e9", CodeGenerator.identifierEscape("var_\u00e9"));
    Assert.assertEquals("\\u0001test", CodeGenerator.identifierEscape("\u0001test"));
  }

  @Test
  public void testBinaryOperatorsAndAssociativity() {
    // a + b
    Node addNode = new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    generator.add(addNode);
    Assert.assertEquals("a+b", consumer.getCode());

    // Associative: a + (b + c)
    consumer.buffer.setLength(0);
    Node innerAdd = new Node(Token.ADD, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node outerAdd = new Node(Token.ADD, Node.newString(Token.NAME, "a"), innerAdd);
    generator.add(outerAdd);
    Assert.assertEquals("a+b+c", consumer.getCode());

    // Non-associative / precedence: a * (b + c)
    consumer.buffer.setLength(0);
    Node mulNode = new Node(Token.MUL, Node.newString(Token.NAME, "a"), innerAdd);
    generator.add(mulNode);
    Assert.assertEquals("a*(b+c)", consumer.getCode());

    // Assignment right-associativity: a = b = c
    consumer.buffer.setLength(0);
    Node assignInner = new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node assignOuter = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), assignInner);
    generator.add(assignOuter);
    Assert.assertEquals("a=b=c", consumer.getCode());
  }

  @Test
  public void testUnaryOperators() {
    Node notNode = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    generator.add(notNode);
    Assert.assertEquals("!x", consumer.getCode());

    consumer.buffer.setLength(0);
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    generator.add(voidNode);
    Assert.assertEquals("void 0", consumer.getCode());

    consumer.buffer.setLength(0);
    Node bitnotNode = new Node(Token.BITNOT, Node.newString(Token.NAME, "y"));
    generator.add(bitnotNode);
    Assert.assertEquals("~y", consumer.getCode());

    consumer.buffer.setLength(0);
    Node posNode = new Node(Token.POS, Node.newString(Token.NAME, "z"));
    generator.add(posNode);
    Assert.assertEquals("+z", consumer.getCode());

    // NEG with number
    consumer.buffer.setLength(0);
    Node negNum = new Node(Token.NEG, Node.newNumber(5.0));
    generator.add(negNum);
    Assert.assertEquals("-5", consumer.getCode());

    // NEG with non-number
    consumer.buffer.setLength(0);
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    generator.add(negVar);
    Assert.assertEquals("-x", consumer.getCode());
  }

  @Test
  public void testIncDec() {
    // Pre-increment
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    preInc.putIntProp(Node.INCRDECR_PROP, 0);
    generator.add(preInc);
    Assert.assertEquals("++i", consumer.getCode());

    // Post-increment
    consumer.buffer.setLength(0);
    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postInc);
    Assert.assertEquals("i++", consumer.getCode());

    // Pre-decrement
    consumer.buffer.setLength(0);
    Node preDec = new Node(Token.DEC, Node.newString(Token.NAME, "i"));
    preDec.putIntProp(Node.INCRDECR_PROP, 0);
    generator.add(preDec);
    Assert.assertEquals("--i", consumer.getCode());

    // Post-decrement
    consumer.buffer.setLength(0);
    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "i"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postDec);
    Assert.assertEquals("i--", consumer.getCode());
  }

  @Test
  public void testHookOperator() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    generator.add(hook);
    Assert.assertEquals("cond?a:b", consumer.getCode());
  }

  @Test
  public void testLiterals() {
    generator.add(new Node(Token.NULL));
    generator.add(new Node(Token.THIS));
    generator.add(new Node(Token.FALSE));
    generator.add(new Node(Token.TRUE));
    Assert.assertEquals("nullthisfalsetrue", consumer.getCode());
  }

  @Test
  public void testArrayLiteral() {
    Node array = new Node(Token.ARRAYLIT,
        Node.newNumber(1.0),
        new Node(Token.EMPTY),
        Node.newNumber(3.0));
    generator.add(array);
    Assert.assertEquals("[1,,3]", consumer.getCode());

    consumer.buffer.setLength(0);
    Node arrayTrailing = new Node(Token.ARRAYLIT,
        Node.newNumber(1.0),
        new Node(Token.EMPTY));
    generator.add(arrayTrailing);
    Assert.assertEquals("[1,,]", consumer.getCode());
  }

  @Test
  public void testObjectLiteral() {
    Node obj = new Node(Token.OBJECTLIT);

    Node key1 = Node.newString("a");
    key1.addChildToBack(Node.newNumber(1.0));
    obj.addChildToBack(key1);

    Node key2 = Node.newString("default"); // JS keyword -> must be quoted or escaped
    key2.addChildToBack(Node.newNumber(2.0));
    obj.addChildToBack(key2);

    Node key3 = Node.newString("123"); // simple number key
    key3.addChildToBack(Node.newNumber(3.0));
    obj.addChildToBack(key3);

    generator.add(obj);
    Assert.assertEquals("{a:1,\"default\":2,123:3}", consumer.getCode());
  }

  @Test
  public void testGetterSetterInObjectLit() {
    Node obj = new Node(Token.OBJECTLIT);

    // Getter: get foo() { return 1; }
    Node getFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newNumber(1.0))));
    Node getProp = Node.newString(Token.GET, "foo");
    getProp.addChildToBack(getFn);
    obj.addChildToBack(getProp);

    // Setter: set foo(v) { }
    Node setParams = new Node(Token.LP, Node.newString(Token.NAME, "v"));
    Node setFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        setParams,
        new Node(Token.BLOCK));
    Node setProp = Node.newString(Token.SET, "foo");
    setProp.addChildToBack(setFn);
    obj.addChildToBack(setProp);

    generator.add(obj);
    Assert.assertTrue(consumer.getCode().contains("get foo(){return 1}"));
    Assert.assertTrue(consumer.getCode().contains("set foo(v){}"));
  }

  @Test
  public void testVarAndNameDeclarations() {
    Node varNode = new Node(Token.VAR);
    Node name1 = Node.newString(Token.NAME, "x");
    Node name2 = Node.newString(Token.NAME, "y");
    name2.addChildToBack(Node.newNumber(42.0));
    varNode.addChildToBack(name1);
    varNode.addChildToBack(name2);

    generator.add(varNode);
    Assert.assertEquals("var x,y=42", consumer.getCode());
  }

  @Test
  public void testControlStructures() {
    // if - else
    Node ifNode = new Node(Token.IF,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"))));
    generator.add(ifNode);
    Assert.assertTrue(consumer.getCode().startsWith("if(cond)a;else b;"));

    // while
    consumer.buffer.setLength(0);
    Node whileNode = new Node(Token.WHILE,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    generator.add(whileNode);
    Assert.assertTrue(consumer.getCode().startsWith("while(cond)a;"));

    // do-while
    consumer.buffer.setLength(0);
    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))),
        Node.newString(Token.NAME, "cond"));
    generator.add(doNode);
    Assert.assertEquals("do a;while(cond);", consumer.getCode());

    // for (var i = 0; i < 10; i++)
    consumer.buffer.setLength(0);
    Node varI = new Node(Token.VAR, Node.newString(Token.NAME, "i"));
    varI.getFirstChild().addChildToBack(Node.newNumber(0));
    Node forNode = new Node(Token.FOR,
        varI,
        new Node(Token.LT, Node.newString(Token.NAME, "i"), Node.newNumber(10)),
        new Node(Token.INC, Node.newString(Token.NAME, "i")),
        new Node(Token.BLOCK));
    generator.add(forNode);
    Assert.assertEquals("for(var i=0;i<10;i++);", consumer.getCode());

    // for-in
    consumer.buffer.setLength(0);
    Node forInNode = new Node(Token.FOR,
        Node.newString(Token.NAME, "k"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK));
    generator.add(forInNode);
    Assert.assertEquals("for(kin obj);", consumer.getCode());
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK,
        new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK)));
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryCatchFinally = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    generator.add(tryCatchFinally);
    Assert.assertEquals("try{}catch(e){}finally{}", consumer.getCode());
  }

  @Test
  public void testSwitchCaseDefault() {
    Node caseNode = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), caseNode, defaultNode);

    generator.add(switchNode);
    Assert.assertTrue(consumer.getCode().contains("switch(x)"));
    Assert.assertTrue(consumer.getCode().contains("case 1:"));
    Assert.assertTrue(consumer.getCode().contains("default:"));
  }

  @Test
  public void testCallsAndNew() {
    // Normal Call
    Node call = new Node(Token.CALL,
        Node.newString(Token.NAME, "foo"),
        Node.newString(Token.NAME, "arg1"),
        Node.newNumber(2));
    generator.add(call);
    Assert.assertEquals("foo(arg1,2)", consumer.getCode());

    // Indirect eval call
    consumer.buffer.setLength(0);
    Node evalCall = new Node(Token.CALL,
        Node.newString(Token.NAME, "eval"),
        Node.newString("1+1"));
    generator.add(evalCall);
    Assert.assertEquals("(0,eval)(\"1+1\")", consumer.getCode());

    // Direct eval call
    consumer.buffer.setLength(0);
    Node directEvalCall = new Node(Token.CALL,
        Node.newString(Token.NAME, "eval"),
        Node.newString("1+1"));
    directEvalCall.getFirstChild().putBooleanProp(Node.DIRECT_EVAL, true);
    generator.add(directEvalCall);
    Assert.assertEquals("eval(\"1+1\")", consumer.getCode());

    // New expression without args
    consumer.buffer.setLength(0);
    Node newNoArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    generator.add(newNoArgs);
    Assert.assertEquals("new Foo", consumer.getCode());

    // New expression with args
    consumer.buffer.setLength(0);
    Node newWithArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1));
    generator.add(newWithArgs);
    Assert.assertEquals("new Foo(1)", consumer.getCode());
  }

  @Test
  public void testGetPropAndGetElem() {
    // obj.prop
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("prop"));
    generator.add(getprop);
    Assert.assertEquals("obj.prop", consumer.getCode());

    // (1).prop (number needs parens)
    consumer.buffer.setLength(0);
    Node numProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("toString"));
    generator.add(numProp);
    Assert.assertEquals("(1).toString", consumer.getCode());

    // obj[elem]
    consumer.buffer.setLength(0);
    Node getelem = new Node(Token.GETELEM, Node.newString(Token.NAME, "arr"), Node.newNumber(0));
    generator.add(getelem);
    Assert.assertEquals("arr[0]", consumer.getCode());
  }

  @Test
  public void testBreakContinueDebuggerThrowReturn() {
    Node breakNode = new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "lbl"));
    generator.add(breakNode);
    Assert.assertEquals("break lbl;", consumer.getCode());

    consumer.buffer.setLength(0);
    Node contNode = new Node(Token.CONTINUE);
    generator.add(contNode);
    Assert.assertEquals("continue;", consumer.getCode());

    consumer.buffer.setLength(0);
    generator.add(new Node(Token.DEBUGGER));
    Assert.assertEquals("debugger;", consumer.getCode());

    consumer.buffer.setLength(0);
    generator.add(new Node(Token.THROW, Node.newString(Token.NAME, "err")));
    Assert.assertEquals("throw err;", consumer.getCode());

    consumer.buffer.setLength(0);
    generator.add(new Node(Token.RETURN));
    Assert.assertEquals("return;", consumer.getCode());
  }

  @Test
  public void testWithAndDelprop() {
    Node withNode = new Node(Token.WITH,
        Node.newString(Token.NAME, "scope"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "action"))));
    generator.add(withNode);
    Assert.assertEquals("with(scope)action;", consumer.getCode());

    consumer.buffer.setLength(0);
    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "target"));
    generator.add(del);
    Assert.assertEquals("delete target", consumer.getCode());
  }

  @Test
  public void testRegexpNode() {
    Node regex = new Node(Token.REGEXP, Node.newString("test"), Node.newString("gi"));
    generator.add(regex);
    Assert.assertEquals("/test/gi", consumer.getCode());
  }

  @Test
  public void testStopProcessing() {
    consumer.continueProcessing = false;
    generator.add(Node.newString(Token.NAME, "unreachable"));
    Assert.assertEquals("", consumer.getCode());
  }

  @Test
  public void testConstructorWithCharset() {
    CodeGenerator cgAscii = new CodeGenerator(consumer, Charset.forName("US-ASCII"));
    Assert.assertNotNull(cgAscii);

    CodeGenerator cgUtf8 = new CodeGenerator(consumer, Charset.forName("UTF-8"));
    Assert.assertNotNull(cgUtf8);
  }

  @Test(expected = Error.class)
  public void testUnknownNodeTypeThrows() {
    Node unknownNode = new Node(Token.EMPTY);
    unknownNode.setType(9999);
    generator.add(unknownNode);
  }
}