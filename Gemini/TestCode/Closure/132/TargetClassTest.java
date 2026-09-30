package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class PeepholeSubstituteAlternateSyntaxTest {

  private Node parseAndFold(String js, boolean late, boolean normalize) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    if (normalize) {
      compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    }
    Node root = compiler.parseTestCode(js);
    Node script = root.getFirstChild();
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(late);
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, peephole);
    pass.process(null, script);
    return script;
  }

  private String toSource(Node n) {
    Compiler compiler = new Compiler();
    return compiler.toSource(n);
  }

  @Test
  public void testTrueFalseReduction() {
    Node nodeEarly = parseAndFold("var x = true; var y = false;", false, false);
    String srcEarly = toSource(nodeEarly);
    Assert.assertTrue(srcEarly.contains("true"));
    Assert.assertTrue(srcEarly.contains("false"));

    Node nodeLate = parseAndFold("var x = true; var y = false;", true, false);
    String srcLate = toSource(nodeLate);
    Assert.assertTrue(srcLate.contains("!0"));
    Assert.assertTrue(srcLate.contains("!1"));
  }

  @Test
  public void testFoldNotOperator() {
    Node node = parseAndFold("var a = !(x == y); var b = !(x != y); var c = !(x === y); var d = !(x !== y);", true, false);
    String src = toSource(node);
    Assert.assertTrue(src.contains("x!=y") || src.contains("x != y"));
    Assert.assertTrue(src.contains("x==y") || src.contains("x == y"));
    Assert.assertTrue(src.contains("x!==y") || src.contains("x !== y"));
    Assert.assertTrue(src.contains("x===y") || src.contains("x === y"));
  }

  @Test
  public void testFoldNotWithNonEq() {
    Node node = parseAndFold("var a = !(x > y); var b = !(x < y);", true, false);
    String src = toSource(node);
    Assert.assertTrue(src.contains("!(x>y)") || src.contains("!(x > y)"));
  }

  @Test
  public void testFoldNotConditions() {
    Node node1 = parseAndFold("if (!(!a && !b)) { foo(); }", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("a||b") || src1.contains("a || b"));

    Node node2 = parseAndFold("if (!(!a || !b)) { foo(); }", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("a&&b") || src2.contains("a && b"));

    Node node3 = parseAndFold("if (!(a && b)) { foo(); }", true, false);
    String src3 = toSource(node3);
    Assert.assertTrue(src3.contains("!a||!b") || src3.contains("!a || !b"));
  }

  @Test
  public void testReduceReturn() {
    Node node1 = parseAndFold("function f() { return undefined; }", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("return;"));

    Node node2 = parseAndFold("function f() { return void 0; }", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("return;"));

    Node node3 = parseAndFold("function f() { return void foo(); }", true, false);
    String src3 = toSource(node3);
    Assert.assertTrue(src3.contains("void foo()"));
  }

  @Test
  public void testReplaceIfWithHook() {
    Node node1 = parseAndFold("function f(x) { if (x) return 1; else return 2; }", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("return x?1:2") || src1.contains("return x ? 1 : 2"));

    Node node2 = parseAndFold("function f(x) { if (x) return 1; return 2; }", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("return x?1:2") || src2.contains("return x ? 1 : 2"));

    Node node3 = parseAndFold("function f(x) { if (x) return; return 2; }", true, false);
    String src3 = toSource(node3);
    Assert.assertTrue(src3.contains("void 0:2") || src3.contains("void 0 : 2"));

    Node node4 = parseAndFold("if (x) a = 1; else a = 2;", true, false);
    String src4 = toSource(node4);
    Assert.assertTrue(src4.contains("a=x?1:2") || src4.contains("a = x ? 1 : 2"));

    Node node5 = parseAndFold("if (x) foo(); else bar();", true, false);
    String src5 = toSource(node5);
    Assert.assertTrue(src5.contains("x?foo():bar()") || src5.contains("x ? foo() : bar()"));
  }

  @Test
  public void testReplaceIfWithVarAndAssign() {
    Node node1 = parseAndFold("if (x) var y = 1; else y = 2;", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("var y=x?1:2") || src1.contains("var y = x ? 1 : 2"));

    Node node2 = parseAndFold("if (x) y = 1; else var y = 2;", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("var y=x?1:2") || src2.contains("var y = x ? 1 : 2"));
  }

  @Test
  public void testCombineIfOr() {
    Node node1 = parseAndFold("function f(x, y) { if (x) return 1; if (y) return 1; }", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("x||y") || src1.contains("x || y"));

    Node node2 = parseAndFold("function f(x, y) { if (x) return 1; if (y) foo(); else return 1; }", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("!x&&y") || src2.contains("!x && y"));
  }

  @Test
  public void testCombineNestedIf() {
    Node node = parseAndFold("if (x) { if (y) { foo(); } }", true, false);
    String src = toSource(node);
    Assert.assertTrue(src.contains("x&&y") || src.contains("x && y"));
  }

  @Test
  public void testIfToAndOr() {
    Node node1 = parseAndFold("if (x) foo();", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("x&&foo()") || src1.contains("x && foo()"));

    Node node2 = parseAndFold("if (!x) foo();", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("x||foo()") || src2.contains("x || foo()"));
  }

  @Test
  public void testInvertNotIfElse() {
    Node node = parseAndFold("if (!x) foo(); else bar();", true, false);
    String src = toSource(node);
    Assert.assertTrue(src.contains("if(x)bar();else foo()") || src.contains("if (x) bar(); else foo()"));
  }

  @Test
  public void testRemoveRepeatedStatementsInIf() {
    Node node = parseAndFold("function f(a) { if (a) { x = 1; return true; } else { x = 2; return true; } }", true, false);
    String src = toSource(node);
    Assert.assertTrue(src.endsWith("return !0;") || src.endsWith("return true;"));
  }

  @Test
  public void testTryJoinForCondition() {
    Node nodeLate = parseAndFold("for (var i = 0; i < 10; i++) { if (i > 5) break; foo(); }", true, false);
    String srcLate = toSource(nodeLate);
    Assert.assertTrue(srcLate.contains("!(i>5)") || srcLate.contains("!(i > 5)"));

    Node nodeEmptyCond = parseAndFold("for (;;) { if (foo) break; bar(); }", true, false);
    String srcEmpty = toSource(nodeEmptyCond);
    Assert.assertTrue(srcEmpty.contains("!foo"));
  }

  @Test
  public void testFoldStandardConstructors() {
    Node node1 = parseAndFold("var x = new Object();", true, true);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("var x={};") || src1.contains("var x = {};"));

    Node node2 = parseAndFold("var x = new Array();", true, true);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("var x=[];") || src2.contains("var x = [];"));

    Node node3 = parseAndFold("var x = new Array(1, 2, 3);", true, true);
    String src3 = toSource(node3);
    Assert.assertTrue(src3.contains("[1,2,3]") || src3.contains("[1, 2, 3]"));

    Node node4 = parseAndFold("var x = new Array('hello');", true, true);
    String src4 = toSource(node4);
    Assert.assertTrue(src4.contains("['hello']") || src4.contains("[\"hello\"]"));

    Node node5 = parseAndFold("var x = new Array(0);", true, true);
    String src5 = toSource(node5);
    Assert.assertTrue(src5.contains("var x=[];") || src5.contains("var x = [];"));

    Node node6 = parseAndFold("var x = new Array(5);", true, true);
    String src6 = toSource(node6);
    Assert.assertFalse(src6.contains("var x=[];"));
  }

  @Test
  public void testFoldRegExpConstructor() {
    Node node1 = parseAndFold("var x = new RegExp('foobar');", true, true);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("/foobar/"));

    Node node2 = parseAndFold("var x = new RegExp('foobar', 'i');", true, true);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("/foobar/i"));

    Node node3 = parseAndFold("var x = new RegExp('/', 'i');", true, true);
    String src3 = toSource(node3);
    Assert.assertTrue(src3.contains("/\\//i"));

    Node node4 = parseAndFold("var x = new RegExp('\n');", true, true);
    String src4 = toSource(node4);
    Assert.assertTrue(src4.contains("\\n"));
  }

  @Test
  public void testFoldStringCall() {
    Node node1 = parseAndFold("var x = String('abc');", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("''+\"abc\"") || src1.contains("\"\" + 'abc'") || src1.contains("''+'abc'"));

    Node node2 = parseAndFold("var x = String(123);", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("''+123") || src2.contains("\"\" + 123"));
  }

  @Test
  public void testFoldImmediateCallToBoundFunction() {
    Node node1 = parseAndFold("(goog.bind(fn, obj, 1, 2))();", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("fn.call(obj,1,2)") || src1.contains("fn.call(obj, 1, 2)"));

    Node node2 = parseAndFold("(goog.bind(fn, undefined, 1))();", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("fn(1)") || src2.contains("fn.call") || src2.contains("fn(1)"));
  }

  @Test
  public void testSplitComma() {
    Node nodeEarly = parseAndFold("a = 1, b = 2;", false, false);
    String srcEarly = toSource(nodeEarly);
    Assert.assertTrue(srcEarly.contains("a=1;b=2;") || srcEarly.contains("a = 1;b = 2;"));

    Node nodeLate = parseAndFold("a = 1, b = 2;", true, false);
    String srcLate = toSource(nodeLate);
    Assert.assertTrue(srcLate.contains("a=1,b=2") || srcLate.contains("a = 1, b = 2"));
  }

  @Test
  public void testReplaceUndefined() {
    Node node = parseAndFold("var x = undefined;", true, true);
    String src = toSource(node);
    Assert.assertTrue(src.contains("void 0"));
  }

  @Test
  public void testMinimizeStringArrayLiteral() {
    Node nodeLate = parseAndFold("var x = ['a', 'b', 'c', 'd', 'e', 'f', 'g'];", true, false);
    String srcLate = toSource(nodeLate);
    Assert.assertTrue(srcLate.contains(".split("));

    Node nodeEarly = parseAndFold("var x = ['a', 'b', 'c', 'd', 'e', 'f', 'g'];", false, false);
    String srcEarly = toSource(nodeEarly);
    Assert.assertFalse(srcEarly.contains(".split("));
  }

  @Test
  public void testHookSimplification() {
    Node node1 = parseAndFold("var x = a ? true : false;", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("var x=a;") || src1.contains("var x = a;"));

    Node node2 = parseAndFold("var x = a ? false : true;", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("var x=!a;") || src2.contains("var x = !a;"));

    Node node3 = parseAndFold("var x = a ? true : b;", true, false);
    String src3 = toSource(node3);
    Assert.assertTrue(src3.contains("a||b") || src3.contains("a || b"));

    Node node4 = parseAndFold("var x = a ? b : false;", true, false);
    String src4 = toSource(node4);
    Assert.assertTrue(src4.contains("a&&b") || src4.contains("a && b"));
  }

  @Test
  public void testWhileAndConditionMinimization() {
    Node node1 = parseAndFold("while (true) { foo(); }", true, false);
    String src1 = toSource(node1);
    Assert.assertTrue(src1.contains("while(1)") || src1.contains("while (1)"));

    Node node2 = parseAndFold("do { foo(); } while (false);", true, false);
    String src2 = toSource(node2);
    Assert.assertTrue(src2.contains("while(0)") || src2.contains("while (0)"));
  }

  @Test
  public void testRedundantExitInLoop() {
    Node node = parseAndFold("while (a) { if (b) { return f(); } } return f();", true, false);
    String src = toSource(node);
    Assert.assertTrue(src.contains("break"));
  }

  @Test
  public void testPureAndUnicodeEscapeHelper() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    Assert.assertTrue(peephole.isPure(null));
    Assert.assertTrue(peephole.isPure(IR.number(1)));
    Assert.assertFalse(peephole.isPure(IR.call(IR.name("foo"))));

    Assert.assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("abc"));
    Assert.assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\u0020"));
  }

  @Test
  public void testExitMatchingAndExceptionPossible() {
    PeepholeSubstituteAlternateSyntax peephole = new PeepholeSubstituteAlternateSyntax(false);
    Node ret1 = IR.returnNode(IR.number(1));
    Node ret2 = IR.returnNode(IR.number(1));
    Node ret3 = IR.returnNode(IR.number(2));
    Node thr1 = IR.throwNode(IR.string("err"));

    Assert.assertTrue(peephole.areMatchingExits(ret1, ret2));
    Assert.assertFalse(peephole.areMatchingExits(ret1, ret3));
    Assert.assertTrue(peephole.isExceptionPossible(thr1));
    Assert.assertFalse(peephole.isExceptionPossible(ret1));
  }

  @Test
  public void testDontTraverseFunctionsPredicate() {
    Node fn = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node num = IR.number(1);
    Assert.assertFalse(PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(fn));
    Assert.assertTrue(PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(num));
  }
}