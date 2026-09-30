package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CodeGeneratorTest {

  private static class TestCodeConsumer extends CodeConsumer {
    final StringBuilder buffer = new StringBuilder();
    boolean continueProcessingFlag = true;
    boolean preserveExtraBlocksFlag = false;
    boolean breakAfterBlockFlag = false;

    @Override
    boolean continueProcessing() {
      return continueProcessingFlag;
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
    void add(String newcode) {
      buffer.append(newcode);
    }

    @Override
    void addIdentifier(String identifier) {
      buffer.append(identifier);
    }

    @Override
    void addOp(String op, boolean binOp) {
      buffer.append(op);
    }

    @Override
    void addNumber(double x) {
      if (x == (long) x) {
        buffer.append((long) x);
      } else {
        buffer.append(x);
      }
    }

    @Override
    void addConstant(String newcode) {
      buffer.append(newcode);
    }

    @Override
    void endStatement() {
      buffer.append(';');
    }

    @Override
    void endStatement(boolean needSemicolon) {
      if (needSemicolon) {
        buffer.append(';');
      }
    }

    @Override
    void endFunction(boolean statementContext) {
      if (statementContext) {
        buffer.append(';');
      }
    }

    @Override
    void beginBlock() {
      buffer.append('{');
    }

    @Override
    void endBlock(boolean breakAfter) {
      buffer.append('}');
    }

    @Override
    void listSeparator() {
      buffer.append(',');
    }

    @Override
    void beginCaseBody() {
      buffer.append(':');
    }

    @Override
    void endCaseBody() {
    }

    @Override
    boolean breakAfterBlockFor(Node n, boolean isStatement) {
      return breakAfterBlockFlag;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocksFlag;
    }

    @Override
    void maybeLineBreak() {}

    @Override
    void notePreferredLineBreak() {}

    @Override
    void startSourceMapping(Node node) {}

    @Override
    void endSourceMapping(Node node) {}

    String getCode() {
      return buffer.toString();
    }
  }

  private TestCodeConsumer consumer;
  private CodeGenerator generator;
  private CompilerOptions defaultOptions;

  @Before
  public void setUp() {
    consumer = new TestCodeConsumer();
    defaultOptions = new CompilerOptions();
    generator = new CodeGenerator(consumer, defaultOptions);
  }

  @Test
  public void testTagAsStrict() {
    generator.tagAsStrict();
    Assert.assertEquals("'use strict';", consumer.getCode());
  }

  @Test
  public void testForCostEstimation() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Assert.assertNotNull(cg);
    cg.add("test");
    Assert.assertEquals("test", consumer.getCode());
  }

  @Test
  public void testOptionsCharsets() {
    CompilerOptions asciiOptions = new CompilerOptions();
    asciiOptions.setOutputCharset("US-ASCII");
    CodeGenerator cgAscii = new CodeGenerator(consumer, asciiOptions);
    Assert.assertNotNull(cgAscii);

    CompilerOptions utf8Options = new CompilerOptions();
    utf8Options.setOutputCharset("UTF-8");
    CodeGenerator cgUtf8 = new CodeGenerator(consumer, utf8Options);
    Assert.assertNotNull(cgUtf8);
  }

  @Test
  public void testContinueProcessingFalse() {
    consumer.continueProcessingFlag = false;
    Node n = Node.newNumber(42);
    generator.add(n);
    Assert.assertEquals("", consumer.getCode());
  }

  @Test
  public void testIsSimpleNumber() {
    Assert.assertTrue(CodeGenerator.isSimpleNumber("1"));
    Assert.assertTrue(CodeGenerator.isSimpleNumber("123"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("0"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("0123"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber(""));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    Assert.assertFalse(CodeGenerator.isSimpleNumber("-5"));
  }

  @Test
  public void testGetSimpleNumber() {
    Assert.assertEquals(1.0, CodeGenerator.getSimpleNumber("1"), 0.0);
    Assert.assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("0")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("012")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("999999999999999999999999999999999")));
    Assert.assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9007199254740993")));
  }

  @Test
  public void testIdentifierEscape() {
    Assert.assertEquals("validIdentifier", CodeGenerator.identifierEscape("validIdentifier"));
    Assert.assertEquals("id\\u00e9e", CodeGenerator.identifierEscape("id\u00e9e"));
    Assert.assertEquals("id\\u1234", CodeGenerator.identifierEscape("id\u1234"));
  }

  @Test
  public void testEscapeToDoubleQuotedJsString() {
    Assert.assertEquals("\"hello\"", generator.escapeToDoubleQuotedJsString("hello"));
    Assert.assertEquals("\"hello\\\"world\\\"\"", generator.escapeToDoubleQuotedJsString("hello\"world\""));
    Assert.assertEquals("\"hello'world'\"", generator.escapeToDoubleQuotedJsString("hello'world'"));
    Assert.assertEquals("\"\\\\\"", generator.escapeToDoubleQuotedJsString("\\"));
  }

  @Test
  public void testRegexpEscape() {
    Assert.assertEquals("/abc/", generator.regexpEscape("abc"));
    Assert.assertEquals("/a\\/b/", generator.regexpEscape("a/b"));
    Assert.assertEquals("/\\x00\\v\\b\\f\\n\\r\\t/", generator.regexpEscape("\0\u000B\b\f\n\r\t"));
    Assert.assertEquals("/\\u2028\\u2029/", generator.regexpEscape("\u2028\u2029"));

    CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
    Assert.assertEquals("/\\u00e9/", generator.regexpEscape("\u00e9", asciiEncoder));
  }

  @Test
  public void testStrEscapeSpecialCharacters() {
    CompilerOptions untrustedOptions = new CompilerOptions();
    untrustedOptions.trustedStrings = false;
    CodeGenerator cgUntrusted = new CodeGenerator(consumer, untrustedOptions);

    Node n1 = Node.newString("a=b&c<d>e");
    cgUntrusted.add(n1);
    String code1 = consumer.getCode();
    Assert.assertTrue(code1.contains("\\x3d"));
    Assert.assertTrue(code1.contains("\\x26"));
    Assert.assertTrue(code1.contains("\\x3c"));
    Assert.assertTrue(code1.contains("\\x3e"));

    consumer.buffer.setLength(0);
    Node n2 = Node.newString("--> and ]]> and </script> and <!--");
    generator.add(n2);
    String code2 = consumer.getCode();
    Assert.assertTrue(code2.contains("\\x3e"));
    Assert.assertTrue(code2.contains("\\x3c"));
  }

  @Test
  public void testPreferSingleQuotes() {
    CompilerOptions singleQuoteOptions = new CompilerOptions();
    singleQuoteOptions.preferSingleQuotes = true;
    CodeGenerator cgSingle = new CodeGenerator(consumer, singleQuoteOptions);

    Node n = Node.newString("test\"string\"");
    cgSingle.add(n);
    Assert.assertEquals("'test\"string\"'", consumer.getCode());

    consumer.buffer.setLength(0);
    Node n2 = Node.newString("test'string'");
    cgSingle.add(n2);
    Assert.assertEquals("\"test'string'\"", consumer.getCode());
  }

  @Test
  public void testAddPrimitiveLiterals() {
    generator.add(new Node(Token.NULL));
    generator.add(new Node(Token.THIS));
    generator.add(new Node(Token.FALSE));
    generator.add(new Node(Token.TRUE));
    generator.add(new Node(Token.DEBUGGER));
    generator.add(new Node(Token.EMPTY));
    Assert.assertEquals("nullthisfalstruedebugger;", consumer.getCode());
  }

  @Test
  public void testAddNumber() {
    generator.add(Node.newNumber(123.0));
    generator.add(Node.newNumber(12.34));
    Assert.assertEquals("12312.34", consumer.getCode());
  }

  @Test
  public void testAddStringWithSlashV() {
    Node strNode = Node.newString("\u000B");
    strNode.putBooleanProp(Node.SLASH_V, true);
    generator.add(strNode);
    Assert.assertEquals("\"\\v\"", consumer.getCode());

    consumer.buffer.setLength(0);
    Node strNodeNoV = Node.newString("\u000B");
    generator.add(strNodeNoV);
    Assert.assertEquals("\"\\x0B\"", consumer.getCode());
  }

  @Test
  public void testBinaryOperatorsAssociativity() {
    Node a = Node.newString(Token.NAME, "a");
    Node b = Node.newString(Token.NAME, "b");
    Node c = Node.newString(Token.NAME, "c");
    Node innerAdd = new Node(Token.ADD, b, c);
    Node outerAdd = new Node(Token.ADD, a, innerAdd);

    generator.add(outerAdd);
    Assert.assertEquals("a+b+c", consumer.getCode());

    consumer.buffer.setLength(0);
    Node assignInner = new Node(Token.ASSIGN, b, c);
    Node assignOuter = new Node(Token.ASSIGN, a, assignInner);
    generator.add(assignOuter);
    Assert.assertEquals("a=b=c", consumer.getCode());

    consumer.buffer.setLength(0);
    Node unrollMul1 = new Node(Token.MUL, a, b);
    Node unrollMul2 = new Node(Token.MUL, unrollMul1, c);
    generator.add(unrollMul2);
    Assert.assertEquals("a*b*c", consumer.getCode());
  }

  @Test
  public void testUnaryOperators() {
    generator.add(new Node(Token.TYPEOF, Node.newString(Token.NAME, "x")));
    generator.add(new Node(Token.VOID, Node.newNumber(0)));
    generator.add(new Node(Token.NOT, Node.newString(Token.NAME, "x")));
    generator.add(new Node(Token.BITNOT, Node.newString(Token.NAME, "x")));
    generator.add(new Node(Token.POS, Node.newString(Token.NAME, "x")));
    Assert.assertEquals("typeof xvoid 0!x~x+x", consumer.getCode());

    consumer.buffer.setLength(0);
    generator.add(new Node(Token.NEG, Node.newNumber(5)));
    generator.add(new Node(Token.NEG, Node.newString(Token.NAME, "x")));
    Assert.assertEquals("-5-x", consumer.getCode());
  }

  @Test
  public void testIncDec() {
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    generator.add(preInc);

    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postInc);

    Node preDec = new Node(Token.DEC, Node.newString(Token.NAME, "y"));
    generator.add(preDec);

    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "y"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    generator.add(postDec);

    Assert.assertEquals("++xx++--yy--", consumer.getCode());
  }

  @Test
  public void testVarAndName() {
    Node varNode = new Node(Token.VAR);
    Node name1 = Node.newString(Token.NAME, "x");
    Node name2 = Node.newString(Token.NAME, "y");
    name2.addChildToBack(Node.newNumber(10));
    varNode.addChildToBack(name1);
    varNode.addChildToBack(name2);

    generator.add(varNode);
    Assert.assertEquals("var x,y=10", consumer.getCode());

    consumer.buffer.setLength(0);
    Node commaAssign = Node.newString(Token.NAME, "z");
    commaAssign.addChildToBack(new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2)));
    generator.add(commaAssign);
    Assert.assertEquals("z=(1,2)", consumer.getCode());
  }

  @Test
  public void testLabelName() {
    Node labelName = Node.newString(Token.LABEL_NAME, "myLabel");
    generator.add(labelName);
    Assert.assertEquals("myLabel", consumer.getCode());
  }

  @Test
  public void testArrayLitAndParamList() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY), Node.newNumber(3));
    generator.add(arrayLit);
    Assert.assertEquals("[1,,3]", consumer.getCode());

    consumer.buffer.setLength(0);
    Node paramList = new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "p1"), Node.newString(Token.NAME, "p2"));
    generator.add(paramList);
    Assert.assertEquals("(p1,p2)", consumer.getCode());
  }

  @Test
  public void testHook() {
    Node cond = Node.newString(Token.NAME, "a");
    Node tExpr = Node.newNumber(1);
    Node fExpr = Node.newNumber(2);
    Node hook = new Node(Token.HOOK, cond, tExpr, fExpr);
    generator.add(hook);
    Assert.assertEquals("a?1:2", consumer.getCode());
  }

  @Test
  public void testRegexpNode() {
    Node regex1 = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("g"));
    generator.add(regex1);
    Assert.assertEquals("/abc/g", consumer.getCode());

    consumer.buffer.setLength(0);
    Node regex2 = new Node(Token.REGEXP, Node.newString("xyz"));
    generator.add(regex2);
    Assert.assertEquals("/xyz/", consumer.getCode());
  }

  @Test(expected = Error.class)
  public void testRegexpInvalidChildren() {
    Node invalidRegex = new Node(Token.REGEXP, Node.newNumber(1), Node.newString("g"));
    generator.add(invalidRegex);
  }

  @Test
  public void testFunction() {
    Node fn = new Node(
        Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK));

    generator.add(fn, CodeGenerator.Context.STATEMENT);
    Assert.assertEquals("functionfoo(){};", consumer.getCode());

    consumer.buffer.setLength(0);
    generator.add(fn, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("(functionfoo(){})", consumer.getCode());
  }

  @Test
  public void testGetterAndSetter() {
    Node fnGet = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node getter = Node.newString(Token.GETTER_DEF, "foo");
    getter.addChildToBack(fnGet);

    Node paramList = new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "val"));
    Node fnSet = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), paramList, new Node(Token.BLOCK));
    Node setter = Node.newString(Token.SETTER_DEF, "foo");
    setter.addChildToBack(fnSet);

    Node objLit = new Node(Token.OBJECTLIT, getter, setter);
    generator.add(objLit);
    Assert.assertEquals("{get foo(){},set foo(val){}}", consumer.getCode());

    consumer.buffer.setLength(0);
    Node numGetter = Node.newString(Token.GETTER_DEF, "123");
    numGetter.addChildToBack(fnGet.cloneTree());
    Node objLitNum = new Node(Token.OBJECTLIT, numGetter);
    generator.add(objLitNum);
    Assert.assertEquals("{get 123(){}}", consumer.getCode());
  }

  @Test
  public void testObjectLit() {
    Node key1 = Node.newString(Token.STRING_KEY, "a");
    key1.addChildToBack(Node.newNumber(1));

    Node key2 = Node.newString(Token.STRING_KEY, "default"); // Keyword
    key2.addChildToBack(Node.newNumber(2));

    Node key3 = Node.newString(Token.STRING_KEY, "123"); // Simple number
    key3.addChildToBack(Node.newNumber(3));

    Node objLit = new Node(Token.OBJECTLIT, key1, key2, key3);
    generator.add(objLit, CodeGenerator.Context.START_OF_EXPR);
    Assert.assertEquals("({a:1,\"default\":2,123:3})", consumer.getCode());
  }

  @Test
  public void testScriptAndBlock() {
    Node script = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    Node fnNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    script.addChildToBack(varNode);
    script.addChildToBack(fnNode);

    generator.add(script);
    Assert.assertEquals("var x;functionf(){}", consumer.getCode());

    consumer.buffer.setLength(0);
    Node block = new Node(Token.BLOCK, varNode.cloneTree());
    generator.add(block, CodeGenerator.Context.PRESERVE_BLOCK);
    Assert.assertEquals("{var x;}", consumer.getCode());
  }

  @Test
  public void testForLoops() {
    Node init = new Node(Token.VAR, Node.newString(Token.NAME, "i"));
    Node cond = new Node(Token.TRUE);
    Node incr = Node.newNumber(0);
    Node body = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node forLoop = new Node(Token.FOR, init, cond, incr, body);

    generator.add(forLoop);
    Assert.assertEquals("for(var i;true;0)1;", consumer.getCode());

    consumer.buffer.setLength(0);
    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "k"), Node.newString(Token.NAME, "obj"), body.cloneTree());
    generator.add(forIn);
    Assert.assertEquals("forkinobj1;", consumer.getCode());
  }

  @Test
  public void testDoWhile() {
    Node body = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node cond = new Node(Token.TRUE);
    Node doWhile = new Node(Token.DO, body, cond);

    generator.add(doWhile);
    Assert.assertEquals("do1;while(true);", consumer.getCode());

    consumer.buffer.setLength(0);
    Node whileLoop = new Node(Token.WHILE, cond.cloneTree(), body.cloneTree());
    generator.add(whileLoop);
    Assert.assertEquals("while(true)1;", consumer.getCode());
  }

  @Test
  public void testGetPropAndGetElem() {
    Node getProp1 = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("prop"));
    generator.add(getProp1);
    Assert.assertEquals("obj.prop", consumer.getCode());

    consumer.buffer.setLength(0);
    Node getProp2 = new Node(Token.GETPROP, Node.newNumber(5), Node.newString("prop"));
    generator.add(getProp2);
    Assert.assertEquals("(5).prop", consumer.getCode());

    consumer.buffer.setLength(0);
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "arr"), Node.newNumber(0));
    generator.add(getElem);
    Assert.assertEquals("arr[0]", consumer.getCode());
  }

  @Test
  public void testWith() {
    Node body = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "obj"), body);
    generator.add(withNode);
    Assert.assertEquals("with(obj)1;", consumer.getCode());
  }

  @Test
  public void testCalls() {
    Node callNormal = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), Node.newNumber(1));
    generator.add(callNormal);
    Assert.assertEquals("foo(1)", consumer.getCode());

    consumer.buffer.setLength(0);
    Node evalName = Node.newString(Token.NAME, "eval");
    Node callIndirectEval = new Node(Token.CALL, evalName, Node.newString("code"));
    generator.add(callIndirectEval);
    Assert.assertEquals("(0,eval)(\"code\")", consumer.getCode());

    consumer.buffer.setLength(0);
    evalName.putBooleanProp(Node.DIRECT_EVAL, true);
    Node callDirectEval = new Node(Token.CALL, evalName, Node.newString("code"));
    generator.add(callDirectEval);
    Assert.assertEquals("eval(\"code\")", consumer.getCode());

    consumer.buffer.setLength(0);
    Node prop = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("foo"));
    Node freeCall = new Node(Token.CALL, prop, Node.newNumber(1));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    generator.add(freeCall);
    Assert.assertEquals("(0,obj.foo)(1)", consumer.getCode());
  }

  @Test
  public void testIfElse() {
    Node cond = Node.newString(Token.NAME, "cond");
    Node thenBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node elseBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(2)));
    Node ifElse = new Node(Token.IF, cond, thenBlock, elseBlock);

    generator.add(ifElse);
    Assert.assertEquals("if(cond)1;else 2;", consumer.getCode());

    consumer.buffer.setLength(0);
    Node ifOnly = new Node(Token.IF, cond.cloneTree(), thenBlock.cloneTree());
    generator.add(ifOnly);
    Assert.assertEquals("if(cond)1;", consumer.getCode());

    consumer.buffer.setLength(0);
    generator.add(ifOnly, CodeGenerator.Context.BEFORE_DANGLING_ELSE);
    Assert.assertEquals("{if(cond)1;}", consumer.getCode());
  }

  @Test
  public void testContinueAndBreak() {
    generator.add(new Node(Token.CONTINUE));
    generator.add(new Node(Token.BREAK));
    Assert.assertEquals("continue;break;", consumer.getCode());

    consumer.buffer.setLength(0);
    generator.add(new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "loop")));
    generator.add(new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "loop")));
    Assert.assertEquals("continue loop;break loop;", consumer.getCode());
  }

  @Test
  public void testExprResultAndDelPropAndCast() {
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(123));
    generator.add(expr);
    Assert.assertEquals("123;", consumer.getCode());

    consumer.buffer.setLength(0);
    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    generator.add(del);
    Assert.assertEquals("delete x", consumer.getCode());

    consumer.buffer.setLength(0);
    Node cast = new Node(Token.CAST, Node.newString(Token.NAME, "x"));
    generator.add(cast);
    Assert.assertEquals("(x)", consumer.getCode());
  }

  @Test
  public void testNew() {
    Node newWithoutArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    generator.add(newWithoutArgs);
    Assert.assertEquals("new Foo", consumer.getCode());

    consumer.buffer.setLength(0);
    Node newWithArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1), Node.newNumber(2));
    generator.add(newWithArgs);
    Assert.assertEquals("new Foo(1,2)", consumer.getCode());

    consumer.buffer.setLength(0);
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "getConstructor"));
    Node newWithCall = new Node(Token.NEW, callTarget, Node.newNumber(1));
    generator.add(newWithCall);
    Assert.assertEquals("new (getConstructor())(1)", consumer.getCode());
  }

  @Test
  public void testSwitchCaseDefault() {
    Node expr = Node.newString(Token.NAME, "x");
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(10))));
    Node defaultCase = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(20))));
    Node switchNode = new Node(Token.SWITCH, expr, case1, defaultCase);

    generator.add(switchNode);
    Assert.assertEquals("switch(x){case 1:10;default:20;}", consumer.getCode());
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node catchBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(2)));
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node catchBlockWrapper = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(3)));

    Node tryCatchFinally = new Node(Token.TRY, tryBlock, catchBlockWrapper, finallyBlock);
    generator.add(tryCatchFinally);
    Assert.assertEquals("try{1;}catch(e){2;}finally{3;}", consumer.getCode());

    consumer.buffer.setLength(0);
    Node emptyCatchBlock = new Node(Token.BLOCK);
    Node tryFinally = new Node(Token.TRY, tryBlock.cloneTree(), emptyCatchBlock, finallyBlock.cloneTree());
    generator.add(tryFinally);
    Assert.assertEquals("try{1;}finally{3;}", consumer.getCode());
  }

  @Test
  public void testThrowAndReturn() {
    Node throwNode = new Node(Token.THROW, Node.newString("err"));
    generator.add(throwNode);
    Assert.assertEquals("throw\"err\";", consumer.getCode());

    consumer.buffer.setLength(0);
    Node retNode = new Node(Token.RETURN, Node.newNumber(1));
    generator.add(retNode);
    Assert.assertEquals("return 1;", consumer.getCode());

    consumer.buffer.setLength(0);
    Node emptyRet = new Node(Token.RETURN);
    generator.add(emptyRet);
    Assert.assertEquals("return;", consumer.getCode());
  }

  @Test
  public void testLabel() {
    Node body = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node labelNode = new Node(Token.LABEL, Node.newString(Token.LABEL_NAME, "myLoop"), body);
    generator.add(labelNode);
    Assert.assertEquals("myLoop:1;", consumer.getCode());
  }

  @Test
  public void testAddNonEmptyStatementPreserveBlocks() {
    consumer.preserveExtraBlocksFlag = true;
    Node emptyBlock = new Node(Token.BLOCK);
    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), emptyBlock);
    generator.add(whileNode);
    Assert.assertEquals("while(true){}", consumer.getCode());

    consumer.buffer.setLength(0);
    consumer.preserveExtraBlocksFlag = false;
    generator.add(whileNode);
    Assert.assertEquals("while(true);", consumer.getCode());
  }

  @Test
  public void testAddNonEmptyStatementWithFunctionOrDoChild() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node blockWithFn = new Node(Token.BLOCK, fn);
    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), blockWithFn);
    generator.add(whileNode);
    Assert.assertEquals("while(true){functionf(){}}", consumer.getCode());

    consumer.buffer.setLength(0);
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.TRUE));
    Node blockWithDo = new Node(Token.BLOCK, doNode);
    Node whileWithDo = new Node(Token.WHILE, new Node(Token.TRUE), blockWithDo);
    generator.add(whileWithDo);
    Assert.assertEquals("while(true){do;while(true);}", consumer.getCode());
  }

  @Test(expected = Error.class)
  public void testUnknownNodeTypeThrowsError() {
    Node unknown = new Node(Token.LABEL_NAME); // Child count check or default branch
    generator.add(new Node(999999));
  }
}