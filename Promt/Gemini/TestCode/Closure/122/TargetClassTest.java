package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.Context;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.ast.ArrayLiteral;
import com.google.javascript.rhino.head.ast.AstNode;
import com.google.javascript.rhino.head.ast.AstRoot;
import com.google.javascript.rhino.head.ast.Block;
import com.google.javascript.rhino.head.ast.CatchClause;
import com.google.javascript.rhino.head.ast.Comment;
import com.google.javascript.rhino.head.ast.ElementGet;
import com.google.javascript.rhino.head.ast.EmptyExpression;
import com.google.javascript.rhino.head.ast.EmptyStatement;
import com.google.javascript.rhino.head.ast.ExpressionStatement;
import com.google.javascript.rhino.head.ast.ForInLoop;
import com.google.javascript.rhino.head.ast.FunctionNode;
import com.google.javascript.rhino.head.ast.InfixExpression;
import com.google.javascript.rhino.head.ast.Name;
import com.google.javascript.rhino.head.ast.NumberLiteral;
import com.google.javascript.rhino.head.ast.ObjectLiteral;
import com.google.javascript.rhino.head.ast.ObjectProperty;
import com.google.javascript.rhino.head.ast.ParenthesizedExpression;
import com.google.javascript.rhino.head.ast.PropertyGet;
import com.google.javascript.rhino.head.ast.StringLiteral;
import com.google.javascript.rhino.head.ast.SwitchCase;
import com.google.javascript.rhino.head.ast.SwitchStatement;
import com.google.javascript.rhino.head.ast.UnaryExpression;
import com.google.javascript.rhino.head.ast.VariableDeclaration;
import com.google.javascript.rhino.head.ast.VariableInitializer;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class IRFactoryTest {

  private List<String> errors;
  private List<String> warnings;

  private final ErrorReporter errorReporter = new ErrorReporter() {
    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      error(message, sourceName, line, lineSource, lineOffset);
      return new EvaluatorException(message);
    }
  };

  @Before
  public void setUp() {
    errors = new ArrayList<String>();
    warnings = new ArrayList<String>();
  }

  private Config createConfig(LanguageMode mode, boolean isIdeMode, boolean acceptConst) {
    Set<String> empty = Collections.emptySet();
    return new Config(empty, empty, isIdeMode, mode, acceptConst);
  }

  private AstRoot parseRhino(String source, LanguageMode mode, boolean isIdeMode) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setWarnTrailingComma(true);
    if (isIdeMode) {
      env.setIdeMode(true);
      env.setRecoverFromErrors(true);
    }
    int langVer = (mode == LanguageMode.ECMASCRIPT3) ? Context.VERSION_1_3 : Context.VERSION_1_8;
    env.setLanguageVersion(langVer);

    com.google.javascript.rhino.head.Parser parser =
        new com.google.javascript.rhino.head.Parser(env, errorReporter);
    return parser.parse(source, "test.js", 1);
  }

  private Node testTransform(String js, LanguageMode mode, boolean isIdeMode, boolean acceptConst) {
    AstRoot root = parseRhino(js, mode, isIdeMode);
    StaticSourceFile sourceFile = new SimpleSourceFile("test.js", false);
    Config config = createConfig(mode, isIdeMode, acceptConst);
    return IRFactory.transformTree(root, sourceFile, js, config, errorReporter);
  }

  private Node testTransform(String js) {
    return testTransform(js, LanguageMode.ECMASCRIPT5, false, true);
  }

  @Test
  public void testSimpleStatements() {
    Node n = testTransform("var x = 1; x = 2;");
    assertNotNull(n);
    assertEquals(Token.SCRIPT, n.getType());
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testDirectives() {
    Node n = testTransform("'use strict'; var a = 10;");
    assertNotNull(n);
    Set<String> dirs = n.getDirectives();
    assertNotNull(dirs);
    assertTrue(dirs.contains("use strict"));
    assertEquals(1, n.getChildCount());
  }

  @Test
  public void testMultipleDirectives() {
    Node n = testTransform("'use strict'; 'use strict'; var a = 1;");
    Set<String> dirs = n.getDirectives();
    assertNotNull(dirs);
    assertEquals(1, dirs.size());
  }

  @Test
  public void testArrayLiteral() {
    Node n = testTransform("[1, 2, , 3];");
    assertNotNull(n);
    Node expr = n.getFirstChild();
    assertEquals(Token.EXPR_RESULT, expr.getType());
    Node array = expr.getFirstChild();
    assertEquals(Token.ARRAYLIT, array.getType());
    assertEquals(4, array.getChildCount());
    assertEquals(Token.EMPTY, array.getChildAtIndex(2).getType());
  }

  @Test
  public void testDestructuringArrayReport() {
    ArrayLiteral arr = new ArrayLiteral();
    arr.setDestructuring(true);
    arr.setLineno(1);
    AstRoot root = new AstRoot();
    root.addChild(new ExpressionStatement(arr));
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("destructuring assignment forbidden"));
  }

  @Test
  public void testDestructuringObjectReport() {
    ObjectLiteral obj = new ObjectLiteral();
    obj.setIsDestructuring(true);
    obj.setLineno(1);
    AstRoot root = new AstRoot();
    root.addChild(new ExpressionStatement(obj));
    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("destructuring assignment forbidden"));
  }

  @Test
  public void testInvalidAssignmentTarget() {
    testTransform("1 = 2;", LanguageMode.ECMASCRIPT5, true, true);
    assertTrue(errors.contains("invalid assignment target"));
  }

  @Test
  public void testValidAssignmentTargets() {
    Node n = testTransform("x = 1; obj.prop = 2; arr[0] = 3;");
    assertTrue(errors.isEmpty());
    assertEquals(3, n.getChildCount());
  }

  @Test
  public void testBreakAndContinue() {
    Node n = testTransform("outer: while (true) { break outer; continue outer; } while(true) { break; continue; }");
    assertNotNull(n);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testCatchClause() {
    Node n = testTransform("try { throw 'err'; } catch (e) { var x = e; } finally { var y = 0; }");
    assertNotNull(n);
    assertEquals(Token.SCRIPT, n.getType());
    Node tryNode = n.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
  }

  @Test
  public void testCatchWithConditionError() {
    CatchClause cc = new CatchClause();
    cc.setVarName(new Name(0, "e"));
    cc.setCatchCondition(new Name(0, "cond"));
    cc.setBody(new Block());
    cc.setLineno(1);

    AstRoot root = new AstRoot();
    com.google.javascript.rhino.head.ast.TryStatement tryStmt =
        new com.google.javascript.rhino.head.ast.TryStatement();
    tryStmt.setTryBlock(new Block());
    tryStmt.addCatchClause(cc);
    root.addChild(tryStmt);

    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("Catch clauses are not supported"));
  }

  @Test
  public void testTryWithoutCatch() {
    Node n = testTransform("try { var a = 1; } finally { var b = 2; }");
    assertNotNull(n);
    Node tryNode = n.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
    Node catchBlock = tryNode.getChildAtIndex(1);
    assertEquals(Token.BLOCK, catchBlock.getType());
    assertEquals(0, catchBlock.getChildCount());
  }

  @Test
  public void testConditionalExpression() {
    Node n = testTransform("var a = true ? 1 : 2;");
    Node varNode = n.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node hookNode = nameNode.getFirstChild();
    assertEquals(Token.HOOK, hookNode.getType());
    assertEquals(3, hookNode.getChildCount());
  }

  @Test
  public void testDoWhileLoop() {
    Node n = testTransform("do { x++; } while (x < 10);");
    Node doNode = n.getFirstChild();
    assertEquals(Token.DO, doNode.getType());
    assertEquals(Token.BLOCK, doNode.getFirstChild().getType());
  }

  @Test
  public void testForLoops() {
    Node n = testTransform("for (var i = 0; i < 10; i++) {} for (var k in obj) {}");
    assertEquals(2, n.getChildCount());
    assertEquals(Token.FOR, n.getFirstChild().getType());
    assertEquals(Token.FOR, n.getChildAtIndex(1).getType());
  }

  @Test
  public void testForEachLoopReport() {
    ForInLoop loop = new ForInLoop();
    loop.setIsForEach(true);
    loop.setIterator(new Name(0, "x"));
    loop.setIteratedObject(new Name(0, "obj"));
    loop.setBody(new Block());
    loop.setLineno(1);

    AstRoot root = new AstRoot();
    root.addChild(loop);

    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("unsupported language extension: for each"));
  }

  @Test
  public void testFunctionDeclarationsAndExpressions() {
    Node n = testTransform("function foo(a, b) { return a + b; } var f = function(c) { return c; };");
    assertEquals(2, n.getChildCount());
    assertEquals(Token.FUNCTION, n.getFirstChild().getType());
  }

  @Test
  public void testUnnamedFunctionStatementError() {
    FunctionNode fn = new FunctionNode();
    fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
    fn.setBody(new Block());
    fn.setLineno(1);

    AstRoot root = new AstRoot();
    root.addChild(fn);

    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("unnamed function statement"));
  }

  @Test
  public void testIfStatement() {
    Node n = testTransform("if (true) { x = 1; } else { x = 2; } if (false) x = 3;");
    assertEquals(2, n.getChildCount());
    Node if1 = n.getFirstChild();
    assertEquals(Token.IF, if1.getType());
    assertEquals(3, if1.getChildCount());
    Node if2 = n.getChildAtIndex(1);
    assertEquals(Token.IF, if2.getType());
    assertEquals(2, if2.getChildCount());
  }

  @Test
  public void testInfixExpressions() {
    String[] ops = {
      "1 | 2;", "1 ^ 2;", "1 & 2;", "1 == 2;", "1 != 2;", "1 < 2;", "1 <= 2;",
      "1 > 2;", "1 >= 2;", "1 << 2;", "1 >> 2;", "1 >>> 2;", "1 + 2;", "1 - 2;",
      "1 * 2;", "1 / 2;", "1 % 2;", "1 === 2;", "1 !== 2;", "1 in obj;", "1 instanceof Object;",
      "1 || 2;", "1 && 2;", "a += 1;", "a -= 1;", "a *= 1;", "a /= 1;", "a %= 1;",
      "a |= 1;", "a ^= 1;", "a &= 1;", "a <<= 1;", "a >>= 1;", "a >>>= 1;"
    };
    for (String op : ops) {
      Node n = testTransform("var a; " + op);
      assertNotNull(n);
    }
  }

  @Test
  public void testKeywordsAndLiterals() {
    Node n = testTransform("true; false; null; this; debugger;");
    assertEquals(5, n.getChildCount());
  }

  @Test
  public void testLabeledStatementMultipleLabels() {
    Node n = testTransform("label1: label2: while(true) { break label1; }");
    assertEquals(Token.LABEL, n.getFirstChild().getType());
  }

  @Test
  public void testReservedKeywordsInModes() {
    testTransform("var let = 1;", LanguageMode.ECMASCRIPT5_STRICT, false, true);
    assertTrue(errors.contains("identifier is a reserved word"));

    errors.clear();
    testTransform("var let = 1;", LanguageMode.ECMASCRIPT5, false, true);
    assertFalse(errors.contains("identifier is a reserved word"));

    errors.clear();
    testTransform("var class = 1;", LanguageMode.ECMASCRIPT3, false, true);
    assertTrue(errors.contains("identifier is a reserved word"));
  }

  @Test
  public void testNewExpression() {
    Node n = testTransform("new Foo(1, 2);");
    Node expr = n.getFirstChild();
    assertEquals(Token.EXPR_RESULT, expr.getType());
    Node newExpr = expr.getFirstChild();
    assertEquals(Token.NEW, newExpr.getType());
    assertEquals(3, newExpr.getChildCount());
  }

  @Test
  public void testNumberLiteralFormatting() {
    Node n = testTransform("1; 1.5; 0;");
    assertEquals(3, n.getChildCount());
  }

  @Test
  public void testObjectLiteralGettersAndSettersInES5() {
    Node n = testTransform("var obj = { get a() { return 1; }, set a(x) { this.val = x; }, b: 2, 'c': 3, 4: 5 };");
    Node varNode = n.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node objLit = nameNode.getFirstChild();
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(5, objLit.getChildCount());
  }

  @Test
  public void testObjectLiteralGetterSetterInES3() {
    ObjectLiteral obj = new ObjectLiteral();
    ObjectProperty getter = new ObjectProperty();
    getter.setType(com.google.javascript.rhino.head.Token.GET);
    getter.setLeft(new Name(0, "a"));
    FunctionNode fn = new FunctionNode();
    fn.setBody(new Block());
    getter.setRight(fn);
    obj.addElement(getter);

    ObjectProperty setter = new ObjectProperty();
    setter.setType(com.google.javascript.rhino.head.Token.SET);
    setter.setLeft(new Name(0, "b"));
    FunctionNode fn2 = new FunctionNode();
    fn2.addParam(new Name(0, "val"));
    fn2.setBody(new Block());
    setter.setRight(fn2);
    obj.addElement(setter);

    AstRoot root = new AstRoot();
    root.addChild(new ExpressionStatement(obj));

    Config config = createConfig(LanguageMode.ECMASCRIPT3, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains(IRFactory.GETTER_ERROR_MESSAGE));
    assertTrue(errors.contains(IRFactory.SETTER_ERROR_MESSAGE));
  }

  @Test
  public void testGetterWithParamsError() {
    ObjectLiteral obj = new ObjectLiteral();
    ObjectProperty getter = new ObjectProperty();
    getter.setType(com.google.javascript.rhino.head.Token.GET);
    getter.setLeft(new Name(0, "a"));
    FunctionNode fn = new FunctionNode();
    fn.addParam(new Name(0, "invalidParam"));
    fn.setBody(new Block());
    getter.setRight(fn);
    obj.addElement(getter);

    AstRoot root = new AstRoot();
    root.addChild(new ExpressionStatement(obj));

    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("getters may not have parameters"));
  }

  @Test
  public void testSetterWithInvalidParamsCount() {
    ObjectLiteral obj = new ObjectLiteral();
    ObjectProperty setter = new ObjectProperty();
    setter.setType(com.google.javascript.rhino.head.Token.SET);
    setter.setLeft(new Name(0, "a"));
    FunctionNode fn = new FunctionNode();
    fn.setBody(new Block()); // 0 params
    setter.setRight(fn);
    obj.addElement(setter);

    AstRoot root = new AstRoot();
    root.addChild(new ExpressionStatement(obj));

    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("setters must have exactly one parameter"));
  }

  @Test
  public void testInvalidES3PropertyNameWarning() {
    testTransform("var obj = { class: 1 };", LanguageMode.ECMASCRIPT3, false, true);
    assertTrue(warnings.contains(IRFactory.INVALID_ES3_PROP_NAME));
  }

  @Test
  public void testPropertyGetInvalidES3NameWarning() {
    testTransform("obj.class;", LanguageMode.ECMASCRIPT3, false, true);
    assertTrue(warnings.contains(IRFactory.INVALID_ES3_PROP_NAME));
  }

  @Test
  public void testRegExpLiteral() {
    Node n = testTransform("/abc/gi; /simple/;");
    assertEquals(2, n.getChildCount());
    Node re1 = n.getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, re1.getType());
    assertEquals(2, re1.getChildCount());
    Node re2 = n.getChildAtIndex(1).getFirstChild();
    assertEquals(1, re2.getChildCount());
  }

  @Test
  public void testStringLiteralVerticalTab() {
    String js = "var s = '\\v';";
    Node n = testTransform(js);
    Node strNode = n.getFirstChild().getFirstChild().getFirstChild();
    assertTrue(strNode.getBooleanProp(Node.SLASH_V));
  }

  @Test
  public void testSwitchStatement() {
    Node n = testTransform("switch (x) { case 1: y = 1; break; default: y = 2; }");
    Node switchNode = n.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
    assertEquals(3, switchNode.getChildCount());
    assertEquals(Token.CASE, switchNode.getChildAtIndex(1).getType());
    assertEquals(Token.DEFAULT_CASE, switchNode.getChildAtIndex(2).getType());
  }

  @Test
  public void testUnaryExpressions() {
    Node n = testTransform("-5; +5; !true; ~1; typeof x; void 0;");
    assertEquals(6, n.getChildCount());
    Node negNum = n.getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, negNum.getType());
    assertEquals(-5.0, negNum.getDouble(), 0.0);
  }

  @Test
  public void testUnaryDelete() {
    testTransform("delete x; delete obj.p; delete arr[0];");
    assertTrue(errors.isEmpty());

    errors.clear();
    testTransform("delete (1 + 2);", LanguageMode.ECMASCRIPT5, true, true);
    assertTrue(errors.contains("Invalid delete operand. Only properties can be deleted."));
  }

  @Test
  public void testUnaryIncDec() {
    Node n = testTransform("x++; ++x; x--; --x;");
    assertEquals(4, n.getChildCount());

    errors.clear();
    testTransform("++(1);", LanguageMode.ECMASCRIPT5, true, true);
    assertTrue(errors.contains("invalid increment target"));

    errors.clear();
    testTransform("--(1);", LanguageMode.ECMASCRIPT5, true, true);
    assertTrue(errors.contains("invalid decrement target"));
  }

  @Test
  public void testConstKeywordHandling() {
    testTransform("const x = 1;", LanguageMode.ECMASCRIPT5, false, false);
    assertTrue(errors.contains("Unsupported syntax: CONST"));
  }

  @Test
  public void testWithStatement() {
    Node n = testTransform("with (obj) { x = 1; }");
    Node withNode = n.getFirstChild();
    assertEquals(Token.WITH, withNode.getType());
    assertEquals(Token.BLOCK, withNode.getChildAtIndex(1).getType());
  }

  @Test
  public void testFileOverviewAndLicenseJSDoc() {
    String js = "/**\n * @fileoverview A file description.\n * @license Apache 2.0\n */\nvar x = 1;";
    Node n = testTransform(js);
    JSDocInfo info = n.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.hasFileOverview());
    assertNotNull(info.getLicense());
  }

  @Test
  public void testSuspiciousCommentWarning() {
    String js = "/* @type {number} */ var x = 1;";
    testTransform(js);
    assertTrue(warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));

    warnings.clear();
    String js2 = "/*\n * @type {number}\n */ var y = 2;";
    testTransform(js2);
    assertTrue(warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  @Test
  public void testMisplacedTypeAnnotation() {
    String js = "var x = /** @type {number} */ (1 + 2);";
    testTransform(js);
    assertTrue(warnings.contains(IRFactory.MISPLACED_TYPE_ANNOTATION));
  }

  @Test
  public void testCastNodeInjection() {
    String js = "var x = (/** @type {number} */ (y));";
    Node n = testTransform(js);
    Node varNode = n.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Node castNode = nameNode.getFirstChild();
    assertEquals(Token.CAST, castNode.getType());
  }

  @Test
  public void testParameterInlineJSDoc() {
    String js = "function f(/** string */ x) {}";
    Node n = testTransform(js);
    Node fn = n.getFirstChild();
    Node paramList = fn.getChildAtIndex(1);
    Node param = paramList.getFirstChild();
    assertNotNull(param.getJSDocInfo());
  }

  @Test
  public void testIdeModeLengthSetting() {
    Node n = testTransform("var x = 100;", LanguageMode.ECMASCRIPT5, true, true);
    assertNotNull(n);
    assertTrue(n.getLength() > 0);
  }

  @Test
  public void testUnknownLanguageModeException() {
    try {
      Config config = new Config(Collections.<String>emptySet(), Collections.<String>emptySet(), false, null, false);
      IRFactory.transformTree(new AstRoot(), null, "", config, errorReporter);
      fail("Expected NullPointerException or IllegalStateException");
    } catch (NullPointerException e) {
      // Expected since config.languageMode is null
    } catch (IllegalStateException e) {
      // Expected
    }
  }

  @Test
  public void testProcessIllegalToken() {
    AstNode illegal = new AstNode() {
      @Override
      public String toSource(int depth) {
        return "";
      }
    };
    illegal.setType(com.google.javascript.rhino.head.Token.ERROR);
    illegal.setLineno(10);
    AstRoot root = new AstRoot();
    root.addChild(illegal);

    Config config = createConfig(LanguageMode.ECMASCRIPT5, false, false);
    IRFactory.transformTree(root, null, "", config, errorReporter);
    assertTrue(errors.contains("Unsupported syntax: ERROR"));
  }

  @Test
  public void testEmptyBlockHandling() {
    Node n = testTransform("if (true) ; else ;");
    Node ifNode = n.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    Node thenBlock = ifNode.getChildAtIndex(1);
    assertEquals(Token.BLOCK, thenBlock.getType());
    assertTrue(thenBlock.wasEmptyNode());
  }
}