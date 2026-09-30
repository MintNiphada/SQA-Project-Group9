package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

public class PeepholeSubstituteAlternateSyntaxTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new PeepholeOptimizationsPass(compiler, new PeepholeSubstituteAlternateSyntax());
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    enableNormalize();
  }

  @Test
  public void testFoldReturnUndefined() {
    test("function f() { return undefined; }", "function f() { return; }");
    test("function f() { return void 0; }", "function f() { return; }");
    testSame("function f() { return void foo(); }");
    testSame("function f() { return 1; }");
    testSame("function f() { return true; }");
  }

  @Test
  public void testFoldReturnInLoop() {
    test("function f() { while (true) { return; } }", "function f() { while (1) { break; } }");
    test("function f() { for (;;) { return; } }", "function f() { for (;;) { break; } }");
    test("function f() { do { return; } while (true); }", "function f() { do { break; } while (1); }");
    test("function f() { while (true) { return 1; } return 1; }", "function f() { while (1) { break; } return 1; }");
  }

  @Test
  public void testMinimizeNot() {
    test("!(x == y)", "x != y");
    test("!(x != y)", "x == y");
    test("!(x === y)", "x !== y");
    test("!(x !== y)", "x === y");
    testSame("!(x < y)");
    testSame("!(x <= y)");
    testSame("!(x > y)");
    testSame("!(x >= y)");
    test("!(x || y)", "!x && !y");
    test("!(x && y)", "!x || !y");
    test("!(!x && !y)", "x || y");
    test("!(!x || !y)", "x && y");
    test("!(!x)", "x");
  }

  @Test
  public void testMinimizeIfWithoutElse() {
    test("if (x) foo();", "x && foo();");
    test("if (!x) bar();", "x || bar();");
    testSame("if (true) foo();");
    testSame("if (false) foo();");
    testSame("if (x) a.b = 1;");
    testSame("if (x && y) foo();");
  }

  @Test
  public void testMinimizeIfWithElse() {
    test("if (!x) foo(); else bar();", "if (x) bar(); else foo();");
    test("if (x) return 1; else return 2;", "return x ? 1 : 2;");
    test("if (x) a = 1; else a = 2;", "a = x ? 1 : 2;");
    test("if (x) foo(); else bar();", "x ? foo() : bar();");
    test("if (x) var y = 1; else y = 2;", "var y = x ? 1 : 2;");
    test("if (x) y = 1; else var y = 2;", "var y = x ? 1 : 2;");
    testSame("if (x) var y = 1; else var z = 2;");
  }

  @Test
  public void testMinimizeIfRepeatedStatements() {
    test("if (a) { x = 1; return true; } else { x = 2; return true; }",
         "if (a) x = 1; else x = 2; return true;");
    test("if (a) { x = 1; y = 2; return true; } else { z = 1; y = 2; return true; }",
         "if (a) x = 1; else z = 1; y = 2; return true;");
  }

  @Test
  public void testMinimizeHook() {
    test("x ? true : false", "x");
    test("x ? false : true", "!x");
    test("x ? true : y", "x || y");
    test("x ? y : false", "x && y");
    testSame("x ? y : z");
  }

  @Test
  public void testMinimizeConditionInControlStructures() {
    test("while (true) {}", "while (1) {}");
    test("do {} while (true);", "do {} while (1);");
    test("for (; true;) {}", "for (; 1;) {}");
    test("for (var k in obj) {}", "for (var k in obj) {}");
    test("if (x || false) foo();", "if (x) foo();");
    test("if (x && true) foo();", "if (x) foo();");
  }

  @Test
  public void testFoldStandardConstructors() {
    test("new Object()", "({})");
    test("new Array()", "[]");
    test("new Array(0)", "[]");
    test("new Array('a')", "['a']");
    test("new Array(1, 2)", "[1, 2]");
    test("new Array([1])", "[[1]]");
    test("new RegExp('abc')", "/abc/");
    test("new RegExp('abc', 'i')", "/abc/i");
    testSame("new RegExp('abc', 'g')");
    test("new Error()", "Error()");
  }

  @Test
  public void testFoldLiteralConstructor() {
    test("Object()", "({})");
    test("Array()", "[]");
    test("Array(0)", "[]");
    test("Array('hello')", "['hello']");
    test("Array(1, 2, 3)", "[1, 2, 3]");
    test("RegExp('foo')", "/foo/");
    test("RegExp('foo', 'm')", "/foo/m");
    test("RegExp('a/b')", "/a\\/b/");
    testSame("RegExp('foo', 'g')");
    testSame("RegExp()");
    testSame("RegExp('a', 'b', 'c')");
  }

  @Test
  public void testInvalidRegExpFlags() {
    test("new RegExp('foo', 'invalid_flag')", "new RegExp('foo', 'invalid_flag')",
        PeepholeSubstituteAlternateSyntax.INVALID_REGULAR_EXPRESSION_FLAGS);
  }

  @Test
  public void testContainsUnicodeEscape() {
    Assert.assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\u0020"));
    Assert.assertTrue(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("test\\u1234test"));
    Assert.assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("plain string"));
    Assert.assertFalse(PeepholeSubstituteAlternateSyntax.containsUnicodeEscape("\\\\u0020"));
  }

  @Test
  public void testOptimizeSubtreeFallback() {
    PeepholeSubstituteAlternateSyntax pass = new PeepholeSubstituteAlternateSyntax();
    Node numberNode = Node.newNumber(42);
    Node result = pass.optimizeSubtree(numberNode);
    Assert.assertSame(numberNode, result);
  }
}