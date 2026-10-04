package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class TargetClassTest {

  private static class DummyCodeConsumer extends CodeConsumer {
    private final StringBuilder sb = new StringBuilder();
    private boolean continueProcess = true;
    private boolean preserveExtraBlocks = false;

    @Override
    char getLastChar() {
      return sb.length() > 0 ? sb.charAt(sb.length() - 1) : '\0';
    }

    @Override
    void append(String newcode) {
      sb.append(newcode);
    }

    @Override
    boolean continueProcessing() {
      return continueProcess;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    String getOutput() {
      return sb.toString();
    }
  }

  private DummyCodeConsumer consumer;
  private CompilerOptions options;
  private CodeGenerator generator;

  @Before
  public void setUp() {
    consumer = new DummyCodeConsumer();
    options = new CompilerOptions();
    generator = new CodeGenerator(consumer, options);
  }

  @Test
  public void testForCostEstimation() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    cg.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer.getOutput());
  }

  @Test
  public void testIsSimpleNumber() {
    Assert.assertTrue(CodeGenerator.isSimpleNumber("0"));
    Assert.assertTrue(CodeGenerator.isSimpleNumber("123"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(""));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("01"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("-1"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("12a"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("1.2"));
  }

  @Test
  public void testGetSimpleNumber() {
    Assert.assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0001);
    Assert.assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0001);
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("012")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9999999999999999999999999999999999999999")));
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("simpleIdent", CodeGenerator.identifierEscape("simpleIdent"));
    String escaped = CodeGenerator.identifierEscape("a\u1234b");
    Assert.assertTrue(escaped.contains("\\u1234"));
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    String escaped = generator.escapeToDoubleQuotedJsString("hello \"world\"\n\r\t\b\f\\");
    Assert.assertTrue(escaped.startsWith("\""));
    Assert.assertTrue(escaped.endsWith("\""));
    Assert.assertTrue(escaped.contains("\\\""));
  }

  @Test
  public void testRegexpEscape() {
    String escaped = generator.regexpEscape("foo/bar</script><!--]]>-->=");
    Assert.assertTrue(escaped.startsWith("/"));
    Assert.assertTrue(escaped.endsWith("/"));
    Assert.assertTrue(escaped.contains("\\x3c"));
    Assert.assertTrue(escaped.contains("\\x3e"));
  }

  @Test
  public void testRegexpEscapeWithEncoder() {
    CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
    String escaped = generator.regexpEscape("\u2028\u2029\u1234\0\u000B", encoder);
    Assert.assertTrue(escaped.contains("\\u2028"));
    Assert.assertTrue(escaped.contains("\\u2029"));
    Assert.assertTrue(escaped.contains("\\x00"));
    Assert.assertTrue(escaped.contains("\\x0B"));
  }

  @Test
  public void testOptionsWithOutputCharset() {
    options.setOutputCharset(Charsets.UTF_8);
    options.preferSingleQuotes = true;
    options.trustedStrings = false;
    CodeGenerator cg = new CodeGenerator(consumer, options);
    Node strNode = IR.string("&<>=]]>--");
    cg.add(strNode);
    Assert.assertTrue(consumer.getOutput().contains("\\x26"));
    Assert.assertTrue(consumer.getOutput().contains("\\x3d"));
  }

  @Test
  public void testAddStringWithSlashV() {
    Node strNode = IR.string("\u000B");
    strNode.putBooleanProp(Node.SLASH_V, true);
    generator.add(strNode);
    Assert.assertTrue(consumer.getOutput().contains("\\v"));
  }

  @Test
  public void testBinaryOperators() {
    Node add = IR.add(IR.name("a"), IR.name("b"));
    generator.add(add);
    Assert.assertTrue(consumer.getOutput().contains("+"));

    consumer.sb.setLength(0);
    Node nestedAdd = IR.add(IR.add(IR.name("a"), IR.name("b")), IR.name("c"));
    generator.add(nestedAdd);
    Assert.assertTrue(consumer.getOutput().contains("+"));

    consumer.sb.setLength(0);
    Node assignChain = IR.assign(IR.name("a"), IR.assign(IR.name("b"), IR.number(1)));
    generator.add(assignChain);
    Assert.assertTrue(consumer.getOutput().contains("="));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = IR.block(IR.exprResult(IR.number(1)));
    Node catchBody = IR.block(IR.exprResult(IR.number(2)));
    Node catchNode = new Node(Token.CATCH, IR.name("e"), catchBody);
    Node catchBlock = IR.block(catchNode);
    Node finallyBlock = IR.block(IR.exprResult(IR.number(3)));
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);

    generator.add(tryNode);
    String out = consumer.getOutput();
    Assert.assertTrue(out.contains("try"));
    Assert.assertTrue(out.contains("catch(e)"));
    Assert.assertTrue(out.contains("finally"));
  }

  @Test
  public void testThrowAndReturn() {
    Node throwNode = IR.throwNode(IR.name("err"));
    generator.add(throwNode);
    Assert.assertTrue(consumer.getOutput().contains("throw err;"));

    consumer.sb.setLength(0);
    Node retNode = IR.returnNode(IR.number(42));
    generator.add(retNode);
    Assert.assertTrue(consumer.getOutput().contains("return 42"));

    consumer.sb.setLength(0);
    Node retEmpty = IR.returnNode();
    generator.add(retEmpty);
    Assert.assertTrue(consumer.getOutput().contains("return"));
  }

  @Test
  public void testVarAndName() {
    Node varNode = IR.var(IR.name("x"), IR.number(10));
    generator.add(varNode);
    Assert.assertTrue(consumer.getOutput().contains("var x=10"));

    consumer.sb.setLength(0);
    Node commaAssign = IR.name("y");
    commaAssign.addChildToFront(IR.comma(IR.number(1), IR.number(2)));
    generator.add(commaAssign);
    Assert.assertTrue(consumer.getOutput().contains("y="));
  }

  @Test
  public void testArrayLitAndParamList() {
    Node arr = IR.arraylit(IR.number(1), IR.empty(), IR.number(2), IR.empty());
    generator.add(arr);
    Assert.assertTrue(consumer.getOutput().startsWith("["));
    Assert.assertTrue(consumer.getOutput().endsWith("]"));

    consumer.sb.setLength(0);
    Node params = IR.paramList(IR.name("p1"), IR.name("p2"));
    generator.add(params);
    Assert.assertTrue(consumer.getOutput().contains("(p1,p2)"));
  }

  @Test
  public void testUnaryOpsAndNeg() {
    Node not = IR.not(IR.name("a"));
    generator.add(not);
    Assert.assertTrue(consumer.getOutput().contains("!a"));

    consumer.sb.setLength(0);
    Node negNum = IR.neg(IR.number(5));
    generator.add(negNum);
    Assert.assertTrue(consumer.getOutput().contains("-5"));

    consumer.sb.setLength(0);
    Node negVar = IR.neg(IR.name("b"));
    generator.add(negVar);
    Assert.assertTrue(consumer.getOutput().contains("-b"));

    consumer.sb.setLength(0);
    Node incPre = IR.inc(IR.name("c"), false);
    incPre.putIntProp(Node.INCRDECR_PROP, 0);
    generator.add(incPre);
    Assert.assertTrue(consumer.getOutput().contains("++c"));

    consumer.sb.setLength(0);
    Node incPost = IR.inc(IR.name("c"), true);
    incPost.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(incPost);
    Assert.assertTrue(consumer.getOutput().contains("c++"));
  }

  @Test
  public void testHook() {
    Node hook = IR.hook(IR.name("a"), IR.number(1), IR.number(2));
    generator.add(hook);
    Assert.assertTrue(consumer.getOutput().contains("?1:2"));
  }

  @Test
  public void testFunction() {
    Node fn = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    generator.add(fn, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertTrue(consumer.getOutput().startsWith("(function foo()"));
    Assert.assertTrue(consumer.getOutput().endsWith(")"));
  }

  @Test
  public void testGetterSetterDef() {
    Node getFn = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node getDef = IR.getterDef("prop", getFn);
    Node obj = IR.objectlit(getDef);
    generator.add(obj);
    Assert.assertTrue(consumer.getOutput().contains("get prop()"));

    consumer.sb.setLength(0);
    Node setFn = IR.function(IR.name(""), IR.paramList(IR.name("val")), IR.block());
    Node setDef = IR.setterDef("123", setFn);
    Node obj2 = IR.objectlit(setDef);
    generator.add(obj2);
    Assert.assertTrue(consumer.getOutput().contains("set 123(val)"));
  }

  @Test
  public void testObjectLit() {
    Node key1 = IR.stringKey("a", IR.number(1));
    Node key2 = IR.stringKey("123", IR.number(2));
    Node key3 = IR.stringKey("a b", IR.number(3));
    Node obj = IR.objectlit(key1, key2, key3);
    generator.add(obj, CodeGenerator.Context.START_OF_EXPR);
    String out = consumer.getOutput();
    Assert.assertTrue(out.startsWith("({"));
    Assert.assertTrue(out.contains("a:1"));
    Assert.assertTrue(out.contains("123:2"));
    Assert.assertTrue(out.contains("\"a b\":3") || out.contains("'a b':3"));
  }

  @Test
  public void testForLoop() {
    Node for4 = IR.forNode(
        IR.var(IR.name("i"), IR.number(0)),
        IR.lt(IR.name("i"), IR.number(10)),
        IR.inc(IR.name("i"), false),
        IR.block()
    );
    generator.add(for4);
    Assert.assertTrue(consumer.getOutput().contains("for(var i=0;i<10;++i)"));

    consumer.sb.setLength(0);
    Node forIn = IR.forIn(IR.name("k"), IR.name("obj"), IR.block());
    generator.add(forIn);
    Assert.assertTrue(consumer.getOutput().contains("for(k in obj)"));
  }

  @Test
  public void testDoWhileAndWhile() {
    Node doWhileNode = IR.doNode(IR.block(), IR.name("a"));
    generator.add(doWhileNode);
    Assert.assertTrue(consumer.getOutput().contains("do;while(a);"));

    consumer.sb.setLength(0);
    Node whileNode = IR.whileNode(IR.name("b"), IR.block());
    generator.add(whileNode);
    Assert.assertTrue(consumer.getOutput().contains("while(b);"));
  }

  @Test
  public void testGetPropAndGetElem() {
    Node getProp = IR.getprop(IR.number(1), IR.string("toString"));
    generator.add(getProp);
    Assert.assertTrue(consumer.getOutput().contains("(1).toString"));

    options.setLanguageOut(LanguageMode.ECMASCRIPT3);
    CodeGenerator es3Gen = new CodeGenerator(consumer, options);
    consumer.sb.setLength(0);
    Node kwProp = IR.getprop(IR.name("obj"), IR.string("delete"));
    es3Gen.add(kwProp);
    Assert.assertTrue(consumer.getOutput().contains("obj[\"delete\"]") || consumer.getOutput().contains("obj['delete']"));

    consumer.sb.setLength(0);
    Node getElem = IR.getelem(IR.name("arr"), IR.number(0));
    generator.add(getElem);
    Assert.assertTrue(consumer.getOutput().contains("arr[0]"));
  }

  @Test
  public void testWithAndDelProp() {
    Node withNode = new Node(Token.WITH, IR.name("o"), IR.block());
    generator.add(withNode);
    Assert.assertTrue(consumer.getOutput().contains("with(o);"));

    consumer.sb.setLength(0);
    Node del = IR.delprop(IR.name("o"));
    generator.add(del);
    Assert.assertTrue(consumer.getOutput().contains("delete o"));
  }

  @Test
  public void testCallAndIndirectEval() {
    Node evalCall = IR.call(IR.name("eval"), IR.string("1+1"));
    generator.add(evalCall);
    Assert.assertTrue(consumer.getOutput().contains("(0,eval)(\"1+1\")"));

    consumer.sb.setLength(0);
    Node directEvalCall = IR.call(IR.name("eval"), IR.string("1+1"));
    directEvalCall.getFirstChild().putBooleanProp(Node.DIRECT_EVAL, true);
    generator.add(directEvalCall);
    Assert.assertEquals("eval(\"1+1\")", consumer.getOutput());

    consumer.sb.setLength(0);
    Node freeCall = IR.call(IR.getprop(IR.name("a"), IR.string("b")));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    generator.add(freeCall);
    Assert.assertTrue(consumer.getOutput().contains("(0,a.b)()"));
  }

  @Test
  public void testIfElseDangling() {
    Node innerIf = IR.ifNode(IR.name("c2"), IR.block());
    Node outerIf = IR.ifNode(IR.name("c1"), innerIf, IR.block());
    generator.add(outerIf, CodeGenerator.Context.BEFORE_DANGLING_ELSE);
    Assert.assertTrue(consumer.getOutput().contains("if(c1)"));
    Assert.assertTrue(consumer.getOutput().contains("else"));
  }

  @Test
  public void testConstantsAndKeywords() {
    generator.add(IR.nullNode());
    generator.add(IR.thisNode());
    generator.add(IR.falseNode());
    generator.add(IR.trueNode());
    generator.add(IR.debugger());
    String out = consumer.getOutput();
    Assert.assertTrue(out.contains("null"));
    Assert.assertTrue(out.contains("this"));
    Assert.assertTrue(out.contains("false"));
    Assert.assertTrue(out.contains("true"));
    Assert.assertTrue(out.contains("debugger;"));
  }

  @Test
  public void testBreakContinueAndLabel() {
    Node labelName = IR.labelName("myLabel");
    Node breakNode = IR.breakNode(labelName);
    generator.add(breakNode);
    Assert.assertTrue(consumer.getOutput().contains("break myLabel;"));

    consumer.sb.setLength(0);
    Node contNode = IR.continueNode(IR.labelName("myLabel2"));
    generator.add(contNode);
    Assert.assertTrue(consumer.getOutput().contains("continue myLabel2;"));

    consumer.sb.setLength(0);
    Node labeledStmt = IR.label(IR.labelName("loop"), IR.block());
    generator.add(labeledStmt);
    Assert.assertTrue(consumer.getOutput().contains("loop:;"));
  }

  @Test
  public void testNewAndCast() {
    Node newCall = IR.newNode(IR.call(IR.name("getConstructor")));
    generator.add(newCall);
    Assert.assertTrue(consumer.getOutput().contains("new (getConstructor())"));

    consumer.sb.setLength(0);
    Node cast = IR.cast(IR.name("x"), "Type");
    generator.add(cast);
    Assert.assertTrue(consumer.getOutput().contains("(x)"));
  }

  @Test
  public void testSwitchCaseDefault() {
    Node case1 = IR.caseNode(IR.number(1), IR.block());
    Node defCase = IR.defaultCase(IR.block());
    Node switchNode = IR.switchNode(IR.name("x"), case1, defCase);
    generator.add(switchNode);
    String out = consumer.getOutput();
    Assert.assertTrue(out.contains("switch(x)"));
    Assert.assertTrue(out.contains("case 1:;"));
    Assert.assertTrue(out.contains("default:;"));
  }

  @Test
  public void testScriptAndBlockHandling() {
    Node script = IR.script(
        IR.var(IR.name("a")),
        IR.function(IR.name("foo"), IR.paramList(), IR.block()),
        IR.block(IR.exprResult(IR.number(1)), IR.exprResult(IR.number(2)))
    );
    generator.add(script);
    String out = consumer.getOutput();
    Assert.assertTrue(out.contains("var a;"));
    Assert.assertTrue(out.contains("function foo()"));
  }

  @Test
  public void testContinueProcessingFalse() {
    consumer.continueProcess = false;
    generator.add(IR.number(42));
    Assert.assertEquals("", consumer.getOutput());
  }

  @Test(expected = Error.class)
  public void testUnknownNodeTypeThrows() {
    Node customNode = new Node(Token.GENERIC_COMMENT);
    generator.add(customNode);
  }

  @Test
  public void testPreserveExtraBlocks() {
    consumer.preserveExtraBlocks = true;
    Node emptyBlock = IR.block();
    generator.add(emptyBlock, CodeGenerator.Context.PRESERVE_BLOCK);
    Assert.assertTrue(consumer.getOutput().contains("{}"));
  }

  @Test
  public void testAddCaseBodyAndSiblings() {
    Node block1 = IR.block(IR.exprResult(IR.number(1)));
    Node block2 = IR.block(IR.exprResult(IR.number(2)));
    block1.setNext(block2);
    generator.addCaseBody(block1);
    generator.addAllSiblings(block1);
    Assert.assertFalse(consumer.getOutput().isEmpty());
  }
}
