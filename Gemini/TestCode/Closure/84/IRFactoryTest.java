package com.google.javascript.jscomp.parsing;

import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.Context;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.ast.ArrayLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstNode;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.jscomp.mozilla.rhino.ast.CatchClause;
import com.google.javascript.jscomp.mozilla.rhino.ast.Comment;
import com.google.javascript.jscomp.mozilla.rhino.ast.Name;
import com.google.javascript.jscomp.mozilla.rhino.ast.NodeVisitor;
import com.google.javascript.jscomp.mozilla.rhino.ast.ObjectLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.ObjectProperty;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IRFactoryTest {

  private TestErrorReporter errorReporter;

  @Before
  public void setUp() {
    errorReporter = new TestErrorReporter();
  }

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> errors = new ArrayList<String>();
    final List<String> warnings = new ArrayList<String>();

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }
  }

  private Config createConfig(boolean acceptES5) {
    try {
      Constructor<?>[] ctors = Config.class.getDeclaredConstructors();
      for (Constructor<?> ctor : ctors) {
        ctor.setAccessible(true);
        Class<?>[] pTypes = ctor.getParameterTypes();
        Object[] args = new Object[pTypes.length];
        for (int i = 0; i < pTypes.length; i++) {
          if (pTypes[i] == boolean.class || pTypes[i] == Boolean.class) {
            args[i] = acceptES5;
          } else if (pTypes[i] == Set.class) {
            args[i] = Collections.emptySet();
          } else if (pTypes[i].isEnum()) {
            Object[] constants = pTypes[i].getEnumConstants();
            args[i] = (constants != null && constants.length > 0) ? constants[0] : null;
          } else {
            args[i] = null;
          }
        }
        try {
          Config config = (Config) ctor.newInstance(args);
          try {
            Field f = Config.class.getDeclaredField("acceptES5");
            f.setAccessible(true);
            f.setBoolean(config, acceptES5);
          } catch (Exception ignored) {
          }
          return config;
        } catch (Exception ignored) {
        }
      }
    } catch (Exception ignored) {
    }
    return null;
  }

  private AstRoot parseRhino(String js, ErrorReporter reporter) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setLanguageVersion(Context.VERSION_1_8);
    env.setReservedKeywordAsIdentifier(true);
    env.setIdeMode(true);
    Parser parser = new Parser(env, reporter);
    return parser.parse(js, "test.js", 1);
  }

  private Node testTransform(String js) {
    return testTransform(js, true);
  }

  private Node testTransform(String js, boolean acceptES5) {
    AstRoot root = parseRhino(js, errorReporter);
    Config config = createConfig(acceptES5);
    Node ir = IRFactory.transformTree(root, js, config, errorReporter);
    Assert.assertNotNull(ir);
    Assert.assertEquals(Token.SCRIPT, ir.getType());
    return ir;
  }

  @Test
  public void testEmptyScript() {
    Node node = testTransform("");
    Assert.assertEquals(Token.SCRIPT, node.getType());
    Assert.assertFalse(node.hasChildren());
  }

  @Test
  public void testDirectivesInScriptAndFunction() {
    String js = "'use strict'; function f() { 'use strict'; var a = 1; }";
    Node script = testTransform(js);
    Set<String> directives = script.getDirectives();
    Assert.assertNotNull(directives);
    Assert.assertTrue(directives.contains("use strict"));

    Node fn = script.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, fn.getType());
    Node body = fn.getLastChild();
    Assert.assertNotNull(body.getDirectives());
    Assert.assertTrue(body.getDirectives().contains("use strict"));
  }

  @Test
  public void testFileOverviewAndLicenseJSDoc() {
    String js = "/**\n * @fileoverview File overview description\n * @license Apache 2.0\n */\nvar x = 1;";
    Node script = testTransform(js);
    JSDocInfo info = script.getJSDocInfo();
    Assert.assertNotNull(info);
    Assert.assertEquals("Apache 2.0", info.getLicense());
  }

  @Test
  public void testCommentWithoutJSDoc() {
    String js = "// line comment\n/* block comment */\nvar x = 1;";
    Node script = testTransform(js);
    Assert.assertNotNull(script.getFirstChild());
  }

  @Test
  public void testNodeJSDoc() {
    String js = "/** @type {number} */ var x = 1;";
    Node script = testTransform(js);
    Node varNode = script.getFirstChild();
    Assert.assertNotNull(varNode.getJSDocInfo());
  }

  @Test
  public void testArrayLiteralDenseAndSparse() {
    String js = "var a = [1, 2, 3]; var b = [1, , 3]; var c = [];";
    Node script = testTransform(js);
    Node var1 = script.getFirstChild();
    Node name1 = var1.getFirstChild();
    Node arr1 = name1.getFirstChild();
    Assert.assertEquals(Token.ARRAYLIT, arr1.getType());
    Assert.assertEquals(3, arr1.getChildCount());

    Node var2 = var1.getNext();
    Node name2 = var2.getFirstChild();
    Node arr2 = name2.getFirstChild();
    Assert.assertEquals(Token.ARRAYLIT, arr2.getType());
    int[] skipIndexes = (int[]) arr2.getProp(Node.SKIP_INDEXES_PROP);
    Assert.assertNotNull(skipIndexes);
    Assert.assertEquals(1, skipIndexes.length);
    Assert.assertEquals(1, skipIndexes[0]);
  }

  @Test
  public void testObjectLiteral() {
    String js = "var o = {a: 1, 'b': 2, 3: 4, get x() { return 1; }, set x(v) {}};";
    Node script = testTransform(js, true);
    Node varNode = script.getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    Assert.assertEquals(Token.OBJECTLIT, objLit.getType());

    boolean foundGet = false;
    boolean foundSet = false;
    for (Node key = objLit.getFirstChild(); key != null; key = key.getNext()) {
      if (key.getType() == Token.GET) foundGet = true;
      if (key.getType() == Token.SET) foundSet = true;
      if ("b".equals(key.getString())) {
        Assert.assertTrue(key.getBooleanProp(Node.QUOTED_PROP));
      }
    }
    Assert.assertTrue(foundGet);
    Assert.assertTrue(foundSet);
  }

  @Test
  public void testObjectLiteralGetSetDisallowed() {
    String js = "var o = {get x() { return 1; }, set x(v) {}};";
    testTransform(js, false);
    Assert.assertTrue(errorReporter.errors.size() >= 2);
  }

  @Test
  public void testAssignments() {
    String js = "a = 1; a += 2; a -= 3; a *= 4; a /= 5; a %= 6; a &= 7; a |= 8; a ^= 9; a <<= 1; a >>= 2; a >>>= 3;";
    Node script = testTransform(js);
    int count = 0;
    for (Node c = script.getFirstChild(); c != null; c = c.getNext()) {
      Assert.assertEquals(Token.EXPR_RESULT, c.getType());
      Node assign = c.getFirstChild();
      Assert.assertTrue(assign.getType() == Token.ASSIGN || assign.getType() == Token.ASSIGN_ADD ||
          assign.getType() == Token.ASSIGN_SUB || assign.getType() == Token.ASSIGN_MUL ||
          assign.getType() == Token.ASSIGN_DIV || assign.getType() == Token.ASSIGN_MOD ||
          assign.getType() == Token.ASSIGN_BITAND || assign.getType() == Token.ASSIGN_BITOR ||
          assign.getType() == Token.ASSIGN_BITXOR || assign.getType() == Token.ASSIGN_LSH ||
          assign.getType() == Token.ASSIGN_RSH || assign.getType() == Token.ASSIGN_URSH);
      count++;
    }
    Assert.assertEquals(12, count);
  }

  @Test
  public void testUnaryExpressions() {
    String js = "+a; -a; !a; ~a; typeof a; void a; delete a.p; ++a; a++; --a; a--; -5;";
    Node script = testTransform(js);
    Node negNum = script.getLastChild().getFirstChild();
    Assert.assertEquals(Token.NUMBER, negNum.getType());
    Assert.assertEquals(-5.0, negNum.getDouble(), 0.0001);

    for (Node c = script.getFirstChild(); c != script.getLastChild(); c = c.getNext()) {
      Node expr = c.getFirstChild();
      if (expr.getType() == Token.INC || expr.getType() == Token.DEC) {
        if (expr.getBooleanProp(Node.INCRDECR_PROP)) {
          Assert.assertTrue(expr.getBooleanProp(Node.INCRDECR_PROP));
        }
      }
    }
  }

  @Test
  public void testInfixExpressions() {
    String js = "1 + 2; 1 - 2; 1 * 2; 1 / 2; 1 % 2; 1 == 2; 1 != 2; 1 === 2; 1 !== 2; 1 < 2; 1 <= 2; " +
        "1 > 2; 1 >= 2; 1 & 2; 1 | 2; 1 ^ 2; 1 << 2; 1 >> 2; 1 >>> 2; 1 && 2; 1 || 2; 'a' in b; c instanceof d;";
    Node script = testTransform(js);
    int count = 0;
    for (Node c = script.getFirstChild(); c != null; c = c.getNext()) {
      count++;
    }
    Assert.assertEquals(23, count);
  }

  @Test
  public void testConditionalExpression() {
    String js = "var x = a ? b : c;";
    Node script = testTransform(js);
    Node hook = script.getFirstChild().getFirstChild().getFirstChild();
    Assert.assertEquals(Token.HOOK, hook.getType());
    Assert.assertEquals(3, hook.getChildCount());
  }

  @Test
  public void testIfStatementWithAndWithoutElse() {
    String js = "if (a) { b(); } else if (c) d(); else { e(); }";
    Node script = testTransform(js);
    Node ifNode = script.getFirstChild();
    Assert.assertEquals(Token.IF, ifNode.getType());
    Assert.assertEquals(3, ifNode.getChildCount());
  }

  @Test
  public void testLoops() {
    String js = "while (a) { b(); } do { b(); } while (a); for (var i = 0; i < 10; i++) { b(); } " +
        "for (;;); for (var x in y) { z(); }";
    Node script = testTransform(js);

    Node whileNode = script.getFirstChild();
    Assert.assertEquals(Token.WHILE, whileNode.getType());

    Node doNode = whileNode.getNext();
    Assert.assertEquals(Token.DO, doNode.getType());

    Node forNode = doNode.getNext();
    Assert.assertEquals(Token.FOR, forNode.getType());
    Assert.assertEquals(4, forNode.getChildCount());

    Node emptyFor = forNode.getNext();
    Assert.assertEquals(Token.FOR, emptyFor.getType());

    Node forIn = emptyFor.getNext();
    Assert.assertEquals(Token.FOR, forIn.getType());
    Assert.assertEquals(3, forIn.getChildCount());
  }

  @Test
  public void testSwitchStatement() {
    String js = "switch (x) { case 1: a(); break; case 2: break; default: c(); }";
    Node script = testTransform(js);
    Node switchNode = script.getFirstChild();
    Assert.assertEquals(Token.SWITCH, switchNode.getType());
    Assert.assertEquals(4, switchNode.getChildCount());

    Node case1 = switchNode.getFirstChild().getNext();
    Assert.assertEquals(Token.CASE, case1.getType());
    Node block1 = case1.getLastChild();
    Assert.assertTrue(block1.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    Node defCase = switchNode.getLastChild();
    Assert.assertEquals(Token.DEFAULT, defCase.getType());
  }

  @Test
  public void testTryCatchFinally() {
    String js1 = "try { a(); } catch (e) { b(); } finally { c(); }";
    Node script1 = testTransform(js1);
    Node try1 = script1.getFirstChild();
    Assert.assertEquals(Token.TRY, try1.getType());
    Assert.assertEquals(3, try1.getChildCount());

    String js2 = "try { a(); } finally { c(); }";
    Node script2 = testTransform(js2);
    Node try2 = script2.getFirstChild();
    Assert.assertEquals(Token.TRY, try2.getType());
    Assert.assertEquals(3, try2.getChildCount());

    String js3 = "try { a(); } catch (e) { b(); }";
    Node script3 = testTransform(js3);
    Node try3 = script3.getFirstChild();
    Assert.assertEquals(Token.TRY, try3.getType());
    Assert.assertEquals(2, try3.getChildCount());
  }

  @Test
  public void testFunctionDeclarationsAndCalls() {
    String js = "function f(a, b) { return a + b; } (function() { return 1; })(); new Foo(1, 2);";
    Node script = testTransform(js);

    Node fnDef = script.getFirstChild();
    Assert.assertEquals(Token.FUNCTION, fnDef.getType());
    Assert.assertEquals("f", fnDef.getFirstChild().getString());
    Node lp = fnDef.getFirstChild().getNext();
    Assert.assertEquals(Token.LP, lp.getType());
    Assert.assertEquals(2, lp.getChildCount());

    Node exprCall = fnDef.getNext();
    Assert.assertEquals(Token.EXPR_RESULT, exprCall.getType());
    Assert.assertEquals(Token.CALL, exprCall.getFirstChild().getType());

    Node exprNew = exprCall.getNext();
    Assert.assertEquals(Token.EXPR_RESULT, exprNew.getType());
    Assert.assertEquals(Token.NEW, exprNew.getFirstChild().getType());
  }

  @Test
  public void testLabelsAndBreakContinue() {
    String js = "label1: while (true) { if (a) break label1; else continue label1; } " +
        "labelA: labelB: var z = 1;";
    Node script = testTransform(js);

    Node labelStmt = script.getFirstChild();
    Assert.assertEquals(Token.LABEL, labelStmt.getType());
    Node labelName = labelStmt.getFirstChild();
    Assert.assertEquals(Token.LABEL_NAME, labelName.getType());
    Assert.assertEquals("label1", labelName.getString());

    Node doubleLabel = labelStmt.getNext();
    Assert.assertEquals(Token.LABEL, doubleLabel.getType());
    Assert.assertEquals(Token.LABEL, doubleLabel.getFirstChild().getNext().getType());
  }

  @Test
  public void testBreakAndContinueWithoutLabel() {
    String js = "while (true) { break; continue; }";
    Node script = testTransform(js);
    Node body = script.getFirstChild().getLastChild();
    Node breakNode = body.getFirstChild();
    Assert.assertEquals(Token.BREAK, breakNode.getType());
    Assert.assertFalse(breakNode.hasChildren());

    Node continueNode = breakNode.getNext();
    Assert.assertEquals(Token.CONTINUE, continueNode.getType());
    Assert.assertFalse(continueNode.hasChildren());
  }

  @Test
  public void testPropertyAndElementGet() {
    String js = "var x = a.b; var y = a['b'];";
    Node script = testTransform(js);
    Node getprop = script.getFirstChild().getFirstChild().getFirstChild();
    Assert.assertEquals(Token.GETPROP, getprop.getType());

    Node getelem = script.getFirstChild().getNext().getFirstChild().getFirstChild();
    Assert.assertEquals(Token.GETELEM, getelem.getType());
  }

  @Test
  public void testRegExpLiteral() {
    String js = "var r1 = /abc/g; var r2 = /abc/;";
    Node script = testTransform(js);
    Node regex1 = script.getFirstChild().getFirstChild().getFirstChild();
    Assert.assertEquals(Token.REGEXP, regex1.getType());
    Assert.assertEquals(2, regex1.getChildCount());
    Assert.assertEquals("abc", regex1.getFirstChild().getString());
    Assert.assertEquals("g", regex1.getLastChild().getString());

    Node regex2 = script.getFirstChild().getNext().getFirstChild().getFirstChild();
    Assert.assertEquals(Token.REGEXP, regex2.getType());
    Assert.assertEquals(1, regex2.getChildCount());
  }

  @Test
  public void testParenthesizedExpression() {
    String js = "var x = (a + b) * c;";
    Node script = testTransform(js);
    Node addNode = script.getFirstChild().getFirstChild().getFirstChild().getFirstChild();
    Assert.assertTrue(Boolean.TRUE.equals(addNode.getProp(Node.PARENTHESIZED_PROP)));
  }

  @Test
  public void testWithAndThrowAndReturn() {
    String js = "with (o) { throw new Error(); return; return x; }";
    Node script = testTransform(js);
    Node withNode = script.getFirstChild();
    Assert.assertEquals(Token.WITH, withNode.getType());

    Node block = withNode.getLastChild();
    Node throwNode = block.getFirstChild();
    Assert.assertEquals(Token.THROW, throwNode.getType());

    Node ret1 = throwNode.getNext();
    Assert.assertEquals(Token.RETURN, ret1.getType());
    Assert.assertFalse(ret1.hasChildren());

    Node ret2 = ret1.getNext();
    Assert.assertEquals(Token.RETURN, ret2.getType());
    Assert.assertTrue(ret2.hasChildren());
  }

  @Test
  public void testKeywordLiterals() {
    String js = "this; null; true; false; debugger;";
    Node script = testTransform(js);
    Node c = script.getFirstChild();
    Assert.assertEquals(Token.THIS, c.getFirstChild().getType());
    c = c.getNext();
    Assert.assertEquals(Token.NULL, c.getFirstChild().getType());
    c = c.getNext();
    Assert.assertEquals(Token.TRUE, c.getFirstChild().getType());
    c = c.getNext();
    Assert.assertEquals(Token.FALSE, c.getFirstChild().getType());
    c = c.getNext();
    Assert.assertEquals(Token.DEBUGGER, c.getType());
  }

  @Test
  public void testMultilinePositionToCharno() {
    String js = "var a = 1;\nvar b = 2;\n\nvar c = 3;";
    Node script = testTransform(js);
    Node varC = script.getLastChild();
    Assert.assertEquals(4, varC.getLineno());
    Assert.assertEquals(0, varC.getCharno());
  }

  @Test
  public void testTransformTokenTypeAllTokens() throws Exception {
    Method transformTokenType = IRFactory.class.getDeclaredMethod("transformTokenType", int.class);
    transformTokenType.setAccessible(true);

    int[] rhinoTokens = {
        com.google.javascript.jscomp.mozilla.rhino.Token.ERROR,
        com.google.javascript.jscomp.mozilla.rhino.Token.EOF,
        com.google.javascript.jscomp.mozilla.rhino.Token.EOL,
        com.google.javascript.jscomp.mozilla.rhino.Token.ENTERWITH,
        com.google.javascript.jscomp.mozilla.rhino.Token.LEAVEWITH,
        com.google.javascript.jscomp.mozilla.rhino.Token.RETURN,
        com.google.javascript.jscomp.mozilla.rhino.Token.GOTO,
        com.google.javascript.jscomp.mozilla.rhino.Token.IFEQ,
        com.google.javascript.jscomp.mozilla.rhino.Token.IFNE,
        com.google.javascript.jscomp.mozilla.rhino.Token.SETNAME,
        com.google.javascript.jscomp.mozilla.rhino.Token.BITOR,
        com.google.javascript.jscomp.mozilla.rhino.Token.BITXOR,
        com.google.javascript.jscomp.mozilla.rhino.Token.BITAND,
        com.google.javascript.jscomp.mozilla.rhino.Token.EQ,
        com.google.javascript.jscomp.mozilla.rhino.Token.NE,
        com.google.javascript.jscomp.mozilla.rhino.Token.LT,
        com.google.javascript.jscomp.mozilla.rhino.Token.LE,
        com.google.javascript.jscomp.mozilla.rhino.Token.GT,
        com.google.javascript.jscomp.mozilla.rhino.Token.GE,
        com.google.javascript.jscomp.mozilla.rhino.Token.LSH,
        com.google.javascript.jscomp.mozilla.rhino.Token.RSH,
        com.google.javascript.jscomp.mozilla.rhino.Token.URSH,
        com.google.javascript.jscomp.mozilla.rhino.Token.ADD,
        com.google.javascript.jscomp.mozilla.rhino.Token.SUB,
        com.google.javascript.jscomp.mozilla.rhino.Token.MUL,
        com.google.javascript.jscomp.mozilla.rhino.Token.DIV,
        com.google.javascript.jscomp.mozilla.rhino.Token.MOD,
        com.google.javascript.jscomp.mozilla.rhino.Token.NOT,
        com.google.javascript.jscomp.mozilla.rhino.Token.BITNOT,
        com.google.javascript.jscomp.mozilla.rhino.Token.POS,
        com.google.javascript.jscomp.mozilla.rhino.Token.NEG,
        com.google.javascript.jscomp.mozilla.rhino.Token.NEW,
        com.google.javascript.jscomp.mozilla.rhino.Token.DELPROP,
        com.google.javascript.jscomp.mozilla.rhino.Token.TYPEOF,
        com.google.javascript.jscomp.mozilla.rhino.Token.GETPROP,
        com.google.javascript.jscomp.mozilla.rhino.Token.SETPROP,
        com.google.javascript.jscomp.mozilla.rhino.Token.GETELEM,
        com.google.javascript.jscomp.mozilla.rhino.Token.SETELEM,
        com.google.javascript.jscomp.mozilla.rhino.Token.CALL,
        com.google.javascript.jscomp.mozilla.rhino.Token.NAME,
        com.google.javascript.jscomp.mozilla.rhino.Token.NUMBER,
        com.google.javascript.jscomp.mozilla.rhino.Token.STRING,
        com.google.javascript.jscomp.mozilla.rhino.Token.NULL,
        com.google.javascript.jscomp.mozilla.rhino.Token.THIS,
        com.google.javascript.jscomp.mozilla.rhino.Token.FALSE,
        com.google.javascript.jscomp.mozilla.rhino.Token.TRUE,
        com.google.javascript.jscomp.mozilla.rhino.Token.SHEQ,
        com.google.javascript.jscomp.mozilla.rhino.Token.SHNE,
        com.google.javascript.jscomp.mozilla.rhino.Token.REGEXP,
        com.google.javascript.jscomp.mozilla.rhino.Token.BINDNAME,
        com.google.javascript.jscomp.mozilla.rhino.Token.THROW,
        com.google.javascript.jscomp.mozilla.rhino.Token.RETHROW,
        com.google.javascript.jscomp.mozilla.rhino.Token.IN,
        com.google.javascript.jscomp.mozilla.rhino.Token.INSTANCEOF,
        com.google.javascript.jscomp.mozilla.rhino.Token.LOCAL_LOAD,
        com.google.javascript.jscomp.mozilla.rhino.Token.GETVAR,
        com.google.javascript.jscomp.mozilla.rhino.Token.SETVAR,
        com.google.javascript.jscomp.mozilla.rhino.Token.CATCH_SCOPE,
        com.google.javascript.jscomp.mozilla.rhino.Token.ENUM_INIT_KEYS,
        com.google.javascript.jscomp.mozilla.rhino.Token.ENUM_INIT_VALUES,
        com.google.javascript.jscomp.mozilla.rhino.Token.ENUM_NEXT,
        com.google.javascript.jscomp.mozilla.rhino.Token.ENUM_ID,
        com.google.javascript.jscomp.mozilla.rhino.Token.THISFN,
        com.google.javascript.jscomp.mozilla.rhino.Token.RETURN_RESULT,
        com.google.javascript.jscomp.mozilla.rhino.Token.ARRAYLIT,
        com.google.javascript.jscomp.mozilla.rhino.Token.OBJECTLIT,
        com.google.javascript.jscomp.mozilla.rhino.Token.GET_REF,
        com.google.javascript.jscomp.mozilla.rhino.Token.SET_REF,
        com.google.javascript.jscomp.mozilla.rhino.Token.DEL_REF,
        com.google.javascript.jscomp.mozilla.rhino.Token.REF_CALL,
        com.google.javascript.jscomp.mozilla.rhino.Token.REF_SPECIAL,
        com.google.javascript.jscomp.mozilla.rhino.Token.DEFAULTNAMESPACE,
        com.google.javascript.jscomp.mozilla.rhino.Token.ESCXMLTEXT,
        com.google.javascript.jscomp.mozilla.rhino.Token.ESCXMLATTR,
        com.google.javascript.jscomp.mozilla.rhino.Token.REF_MEMBER,
        com.google.javascript.jscomp.mozilla.rhino.Token.REF_NS_MEMBER,
        com.google.javascript.jscomp.mozilla.rhino.Token.REF_NAME,
        com.google.javascript.jscomp.mozilla.rhino.Token.REF_NS_NAME,
        com.google.javascript.jscomp.mozilla.rhino.Token.TRY,
        com.google.javascript.jscomp.mozilla.rhino.Token.SEMI,
        com.google.javascript.jscomp.mozilla.rhino.Token.LB,
        com.google.javascript.jscomp.mozilla.rhino.Token.RB,
        com.google.javascript.jscomp.mozilla.rhino.Token.LC,
        com.google.javascript.jscomp.mozilla.rhino.Token.RC,
        com.google.javascript.jscomp.mozilla.rhino.Token.LP,
        com.google.javascript.jscomp.mozilla.rhino.Token.RP,
        com.google.javascript.jscomp.mozilla.rhino.Token.COMMA,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_BITOR,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_BITXOR,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_BITAND,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_LSH,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_RSH,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_URSH,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_ADD,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_SUB,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_MUL,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_DIV,
        com.google.javascript.jscomp.mozilla.rhino.Token.ASSIGN_MOD,
        com.google.javascript.jscomp.mozilla.rhino.Token.HOOK,
        com.google.javascript.jscomp.mozilla.rhino.Token.COLON,
        com.google.javascript.jscomp.mozilla.rhino.Token.OR,
        com.google.javascript.jscomp.mozilla.rhino.Token.AND,
        com.google.javascript.jscomp.mozilla.rhino.Token.INC,
        com.google.javascript.jscomp.mozilla.rhino.Token.DEC,
        com.google.javascript.jscomp.mozilla.rhino.Token.DOT,
        com.google.javascript.jscomp.mozilla.rhino.Token.FUNCTION,
        com.google.javascript.jscomp.mozilla.rhino.Token.EXPORT,
        com.google.javascript.jscomp.mozilla.rhino.Token.IMPORT,
        com.google.javascript.jscomp.mozilla.rhino.Token.IF,
        com.google.javascript.jscomp.mozilla.rhino.Token.ELSE,
        com.google.javascript.jscomp.mozilla.rhino.Token.SWITCH,
        com.google.javascript.jscomp.mozilla.rhino.Token.CASE,
        com.google.javascript.jscomp.mozilla.rhino.Token.DEFAULT,
        com.google.javascript.jscomp.mozilla.rhino.Token.WHILE,
        com.google.javascript.jscomp.mozilla.rhino.Token.DO,
        com.google.javascript.jscomp.mozilla.rhino.Token.FOR,
        com.google.javascript.jscomp.mozilla.rhino.Token.BREAK,
        com.google.javascript.jscomp.mozilla.rhino.Token.CONTINUE,
        com.google.javascript.jscomp.mozilla.rhino.Token.VAR,
        com.google.javascript.jscomp.mozilla.rhino.Token.WITH,
        com.google.javascript.jscomp.mozilla.rhino.Token.CATCH,
        com.google.javascript.jscomp.mozilla.rhino.Token.FINALLY,
        com.google.javascript.jscomp.mozilla.rhino.Token.VOID,
        com.google.javascript.jscomp.mozilla.rhino.Token.RESERVED,
        com.google.javascript.jscomp.mozilla.rhino.Token.EMPTY,
        com.google.javascript.jscomp.mozilla.rhino.Token.BLOCK,
        com.google.javascript.jscomp.mozilla.rhino.Token.LABEL,
        com.google.javascript.jscomp.mozilla.rhino.Token.TARGET,
        com.google.javascript.jscomp.mozilla.rhino.Token.LOOP,
        com.google.javascript.jscomp.mozilla.rhino.Token.EXPR_VOID,
        com.google.javascript.jscomp.mozilla.rhino.Token.EXPR_RESULT,
        com.google.javascript.jscomp.mozilla.rhino.Token.JSR,
        com.google.javascript.jscomp.mozilla.rhino.Token.SCRIPT,
        com.google.javascript.jscomp.mozilla.rhino.Token.TYPEOFNAME,
        com.google.javascript.jscomp.mozilla.rhino.Token.USE_STACK,
        com.google.javascript.jscomp.mozilla.rhino.Token.SETPROP_OP,
        com.google.javascript.jscomp.mozilla.rhino.Token.SETELEM_OP,
        com.google.javascript.jscomp.mozilla.rhino.Token.LOCAL_BLOCK,
        com.google.javascript.jscomp.mozilla.rhino.Token.SET_REF_OP,
        com.google.javascript.jscomp.mozilla.rhino.Token.DOTDOT,
        com.google.javascript.jscomp.mozilla.rhino.Token.COLONCOLON,
        com.google.javascript.jscomp.mozilla.rhino.Token.XML,
        com.google.javascript.jscomp.mozilla.rhino.Token.DOTQUERY,
        com.google.javascript.jscomp.mozilla.rhino.Token.XMLATTR,
        com.google.javascript.jscomp.mozilla.rhino.Token.XMLEND,
        com.google.javascript.jscomp.mozilla.rhino.Token.TO_OBJECT,
        com.google.javascript.jscomp.mozilla.rhino.Token.TO_DOUBLE,
        com.google.javascript.jscomp.mozilla.rhino.Token.GET,
        com.google.javascript.jscomp.mozilla.rhino.Token.SET,
        com.google.javascript.jscomp.mozilla.rhino.Token.CONST,
        com.google.javascript.jscomp.mozilla.rhino.Token.SETCONST,
        com.google.javascript.jscomp.mozilla.rhino.Token.DEBUGGER
    };

    for (int t : rhinoTokens) {
      Object res = transformTokenType.invoke(null, t);
      Assert.assertNotNull(res);
    }

    try {
      transformTokenType.invoke(null, 999999);
      Assert.fail("Expected exception for unknown token");
    } catch (InvocationTargetException e) {
      Assert.assertTrue(e.getCause() instanceof IllegalStateException);
    }
  }

  static class DummyAstNode extends AstNode {
    DummyAstNode(int type) {
      super();
      this.type = type;
    }

    @Override
    public String toSource(int depth) {
      return "";
    }

    @Override
    public void visit(NodeVisitor visitor) {
      visitor.visit(this);
    }
  }

  @Test
  public void testCatchClauseConditionReporting() {
    AstRoot root = new AstRoot();
    root.setSourceName("test.js");

    CatchClause catchClause = new CatchClause();
    Name varName = new Name(0, "e");
    catchClause.setVarName(varName);
    Name cond = new Name(0, "cond");
    cond.setLineno(1);
    catchClause.setCatchCondition(cond);

    com.google.javascript.jscomp.mozilla.rhino.ast.Block body =
        new com.google.javascript.jscomp.mozilla.rhino.ast.Block();
    catchClause.setBody(body);

    root.addChild(catchClause);

    Config config = createConfig(true);
    IRFactory.transformTree(root, "catch(e if cond) {}", config, errorReporter);
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testDestructuringAssignmentReporting() {
    AstRoot root = new AstRoot();
    root.setSourceName("test.js");

    ArrayLiteral arrayLit = new ArrayLiteral();
    arrayLit.setIsDestructuring(true);
    arrayLit.setLineno(1);
    root.addChild(arrayLit);

    Config config = createConfig(true);
    IRFactory.transformTree(root, "[]", config, errorReporter);
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testDestructuringObjectReporting() {
    AstRoot root = new AstRoot();
    root.setSourceName("test.js");

    ObjectLiteral objLit = new ObjectLiteral();
    objLit.setIsDestructuring(true);
    objLit.setLineno(1);
    root.addChild(objLit);

    Config config = createConfig(true);
    IRFactory.transformTree(root, "{}", config, errorReporter);
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }

  @Test
  public void testProcessIllegalToken() {
    AstRoot root = new AstRoot();
    root.setSourceName("test.js");
    DummyAstNode illegalNode = new DummyAstNode(com.google.javascript.jscomp.mozilla.rhino.Token.RESERVED);
    illegalNode.setLineno(1);
    root.addChild(illegalNode);

    Config config = createConfig(true);
    Node result = IRFactory.transformTree(root, "", config, errorReporter);
    Assert.assertNotNull(result);
    Assert.assertTrue(errorReporter.errors.size() > 0);
  }
}