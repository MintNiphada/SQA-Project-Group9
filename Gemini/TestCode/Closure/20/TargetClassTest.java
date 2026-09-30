package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class PeepholeSubstituteAlternateSyntaxTest extends CompilerTestCase {

  private boolean late = true;

  public PeepholeSubstituteAlternateSyntaxTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler,
        new PeepholeSubstituteAlternateSyntax(late));
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    late = true;
    enableNormalize();
  }

  @Test
  public void testFoldStandardConstructors() {
    test("new Object()", "Object()");
    test("new Array()", "[]");
    test("new Array(0)", "[]");
    test("new Array(1, 2, 3)", "[1, 2, 3]");
    test("new Array('a')", "['a']");
    test("new Array([1, 2])", "[[1, 2]]");
    testSame("new Array(10)");
    testSame("new Array(x)");
    testSame("new Custom()");
  }

  @Test
  public void testFoldLiteralConstructor() {
    test("Object()", "({})");
    test("Array()", "[]");
    test("Array(0)", "[]");
    test("Array('abc')", "['abc']");
    test("Array(1, 2, 3)", "[1, 2, 3]");
    testSame("Array(5)");
    testSame("Object(1)");
  }

  @Test
  public void testFoldRegularExpressionConstructor() {
    test("RegExp('abc')", "/abc/");
    test("RegExp('abc', 'i')", "/abc/i");
    test("RegExp('abc', 'g')", "/abc/g");
    test("RegExp('abc', 'm')", "/abc/m");
    test("RegExp('abc', 'gim')", "/abc/gim");
    test("RegExp('/')", "/\\//");
    test("RegExp('[/]')", "/[/]/");
    test("RegExp('\\n')", "/\\n/");
    test("RegExp('\\r')", "/\\r/");
    test("RegExp('\\u2028')", "/\\u2028/");
    test("RegExp('\\u2029')", "/\\u2029/");
    testSame("RegExp('')");
    testSame("RegExp('a', 'xyz')");
    testSame("RegExp(a)");
    testSame("RegExp('a', 'b', 'c')");
  }

  @Test
  public void testFoldSimpleFunctionCall() {
    test("String('a')", "'' + ('a')");
    test("String(123)", "'' + (123)");
    test("String(true)", "'' + (true)");
    testSame("String()");
    testSame("String('a', 'b')");
    testSame("Other('a')");
  }

  @Test
  public void testFoldImmediateCallToBoundFunction() {
    test("(fn.bind(obj))()", "fn.call(obj)");
    test("(fn.bind(obj, 1, 2))()", "fn.call(obj, 1, 2)");
    test("(fn.bind(obj, 1))(2, 3)", "fn.call(obj, 1, 2, 3)");
    test("(fn.bind(null, 1))()", "fn(1)");
    test("(fn.bind(undefined, 1, 2))()", "fn(1, 2)");
  }

  @Test
  public void testTryMinimizeNot() {
    test("!(x == y)", "x != y");
    test("!(x != y)", "x == y");
    test("!(x === y)", "x !== y");
    test("!(x !== y)", "x === y");
    testSame("!(x > y)");
    testSame("!(x < y)");
    testSame("!(x >= y)");
    testSame("!(x <= y)");
  }

  @Test
  public void testTryMinimizeCondition() {
    test("if (!!x) foo();", "x && foo();");
    test("if (!(x || y)) foo();", "(!x && !y) && foo();");
    test("if (!(x && y)) foo();", "(!x || !y) && foo();");
    test("if (!(!x && !y)) foo();", "(x || y) && foo();");
    test("if (!(!x || !y)) foo();", "(x && y) && foo();");
    test("if (!(!x && y)) foo();", "(x || !y) && foo();");
    test("if (!(!x || y)) foo();", "(x && !y) && foo();");
    test("if (!(x && !y)) foo();", "(!x || y) && foo();");
    test("if (!(x || !y)) foo();", "(!x && y) && foo();");
    test("if (x ? true : false) foo();", "x && foo();");
    test("if (x ? false : true) foo();", "!x && foo();");
    test("if (x ? true : y) foo();", "(x || y) && foo();");
    test("if (x ? y : false) foo();", "(x && y) && foo();");
    test("if (x || false) foo();", "x && foo();");
    test("if (x && true) foo();", "x && foo();");
    test("if (false || x) foo();", "x && foo();");
    test("if (true && x) foo();", "x && foo();");
  }

  @Test
  public void testTryMinimizeIf() {
    test("if (x) foo();", "x && foo();");
    test("if (!x) foo();", "x || foo();");
    test("if (x) foo(); else bar();", "x ? foo() : bar();");
    test("if (!x) foo(); else bar();", "if (x) bar(); else foo();");
    test("if (x) return 1; else return 2;", "return x ? 1 : 2;");
    test("if (x) a = 1; else a = 2;", "a = x ? 1 : 2;");
    test("if (x) var y = 1; else y = 2;", "var y = x ? 1 : 2;");
    test("if (x) y = 1; else var y = 2;", "var y = x ? 1 : 2;");
    test("if (x) { if (y) foo(); }", "(x && y) && foo();");
    test("function f() { if (x) return 1; return 2; }",
         "function f() { return x ? 1 : 2; }");
    test("function f() { if (x) return; return 1; }",
         "function f() { return x ? void 0 : 1; }");
    test("function f() { if (x) return 1; if (y) return 1; }",
         "function f() { if (x || y) return 1; }");
    test("function f() { if (x) return 1; if (y) foo(); else return 1; }",
         "function f() { if (!x && y) foo(); else return 1; }");
    test("function f() { if (x) { a = 1; return 2; } else { a = 1; return 3; } }",
         "function f() { a = 1; if (x) { return 2; } else { return 3; } }");
  }

  @Test
  public void testTryReduceReturn() {
    test("function f() { return undefined; }", "function f() { return; }");
    test("function f() { return void 0; }", "function f() { return; }");
    testSame("function f() { return void foo(); }");
    testSame("function f() { return 1; }");
  }

  @Test
  public void testTryReplaceExitWithBreakAndRedundantExit() {
    test("while (x) { return 1; break; }", "while (x) { return 1; }");
    test("function f() { while (a) { return; } }",
         "function f() { while (a) { break; } }");
    test("function f() { if (a) { return; } }",
         "function f() { if (a) {} }");
  }

  @Test
  public void testTryJoinForCondition() {
    late = true;
    test("for (;;) { if (x) break; }", "for (; !x;) {}");
    test("for (; y;) { if (x) break; }", "for (; y && !x;) {}");
    test("for (;;) { if (x) break; else foo(); }", "for (; !x;) foo();");

    late = false;
    testSame("for (;;) { if (x) break; }");
  }

  @Test
  public void testTrySplitComma() {
    late = false;
    test("a, b;", "a; b;");
    test("a, b, c;", "a; b; c;");

    late = true;
    testSame("a, b;");
  }

  @Test
  public void testReduceTrueFalse() {
    late = true;
    test("var x = true;", "var x = !0;");
    test("var x = false;", "var x = !1;");

    late = false;
    testSame("var x = true;");
    testSame("var x = false;");
  }

  @Test
  public void testTryMinimizeStringArrayLiteral() {
    late = true;
    test("var x = ['a', 'b', 'c', 'd', 'e', 'f', 'g'];",
         "var x = 'abcdefg'.split('');");
    test("var x = ['apple', 'banana', 'cherry', 'date', 'elderberry'];",
         "var x = 'apple banana cherry date elderberry'.split(' ');");
    testSame("var x = ['a', 'b'];");
    testSame("var x = [1, 2, 3, 4, 5, 6, 7];");

    late = false;
    testSame("var x = ['a', 'b', 'c', 'd', 'e', 'f', 'g'];");
  }

  @Test
  public void testTryReplaceUndefined() {
    test("var x = undefined;", "var x = void 0;");
    testSame("undefined = 1;");
  }

  @Test
  public void testContainsUnicodeEscape() {
    Assert.assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\u0000"));
    Assert.assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\u1234"));
    Assert.assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("abc"));
    Assert.assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape(""));
  }

  @Test
  public void testDontTraverseFunctionsPredicate() {
    Node fn = new Node(com.google.javascript.rhino.Token.FUNCTION);
    Node block = new Node(com.google.javascript.rhino.Token.BLOCK);
    Assert.assertFalse(PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(fn));
    Assert.assertTrue(PeepholeSubstituteAlternateSyntax.DONT_TRAVERSE_FUNCTIONS_PREDICATE.apply(block));
  }
}