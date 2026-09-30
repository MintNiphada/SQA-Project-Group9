package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TargetClassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node parseAndFold(String js) {
    Node root = compiler.parseTestCode(js);
    FoldConstants foldConstants = new FoldConstants(compiler);
    foldConstants.process(null, root);
    return root;
  }

  @Test
  public void testTypeofFolding() {
    parseAndFold("var a = typeof 'hello';");
    parseAndFold("var b = typeof 123;");
    parseAndFold("var c = typeof true;");
    parseAndFold("var d = typeof false;");
    parseAndFold("var e = typeof null;");
    parseAndFold("var f = typeof {};");
    parseAndFold("var g = typeof [];");
    parseAndFold("var h = typeof undefined;");
  }

  @Test
  public void testNotNegBitnotFolding() {
    parseAndFold("!true;");
    parseAndFold("!false;");
    parseAndFold("!0;");
    parseAndFold("!1;");
    parseAndFold("var a = !true;");
    parseAndFold("var b = !false;");
    parseAndFold("var c = -5;");
    parseAndFold("var d = -(-5);");
    parseAndFold("var e = -Infinity;");
    parseAndFold("var f = -NaN;");
    parseAndFold("var g = ~10;");
    parseAndFold("var h = ~1.5;");
    parseAndFold("var i = ~5000000000;");
    parseAndFold("var j = !(x == y);");
    parseAndFold("var k = !(x != y);");
    parseAndFold("var l = !(x === y);");
    parseAndFold("var m = !(x !== y);");
    parseAndFold("var n = !(x > y);");
  }

  @Test
  public void testNewRegExpAndConstructors() {
    parseAndFold("var a = new RegExp('abc');");
    parseAndFold("var b = new RegExp('abc', 'i');");
    parseAndFold("var c = new RegExp('abc', 'g');");
    parseAndFold("var d = new RegExp('abc', 'z');");
    parseAndFold("var e = new RegExp('a/b/c');");
    parseAndFold("var f = new RegExp('\\\\u0041');");
    parseAndFold("var g = new Array();");
    parseAndFold("var h = new Object();");
  }

  @Test
  public void testReturnReduction() {
    parseAndFold("function f() { return undefined; }");
    parseAndFold("function g() { return void 0; }");
    parseAndFold("function h() { return void(x()); }");
    parseAndFold("function i() { return 1; }");
    parseAndFold("function j() { return; }");
  }

  @Test
  public void testInstanceOfFolding() {
    parseAndFold("var a = 'str' instanceof Object;");
    parseAndFold("var b = 123 instanceof Object;");
    parseAndFold("var c = true instanceof Object;");
    parseAndFold("var d = null instanceof Object;");
    parseAndFold("var e = ({}) instanceof Object;");
    parseAndFold("var f = ([]) instanceof Object;");
  }

  @Test
  public void testIfAndHookFolding() {
    parseAndFold("if (true) { x = 1; }");
    parseAndFold("if (false) { x = 1; }");
    parseAndFold("if (true) { x = 1; } else { x = 2; }");
    parseAndFold("if (false) { x = 1; } else { x = 2; }");
    parseAndFold("if (x) { } else { y = 1; }");
    parseAndFold("if (x) { y = 1; } else { }");
    parseAndFold("if (x()) { }");
    parseAndFold("if (!x) foo();");
    parseAndFold("if (x) foo();");
    parseAndFold("if (!x) foo(); else bar();");
    parseAndFold("if (x) return 1; else return 2;");
    parseAndFold("if (x) a = 1; else a = 2;");
    parseAndFold("if (x) foo(); else bar();");
    parseAndFold("if (x) var y = 1; else y = 2;");
    parseAndFold("if (x) y = 1; else var y = 2;");
    parseAndFold("var a = true ? 1 : 2;");
    parseAndFold("var b = false ? 1 : 2;");
    parseAndFold("x ? void 0 : y();");
    parseAndFold("x ? y() : void 0;");
    parseAndFold("!x ? void 0 : y();");
  }

  @Test
  public void testRepeatedStatementsInIf() {
    parseAndFold("if (a) { x = 1; return true; } else { x = 2; return true; }");
  }

  @Test
  public void testLoopsFolding() {
    parseAndFold("while (false) { x = 1; }");
    parseAndFold("while (true) { x = 1; break; }");
    parseAndFold("do { x = 1; } while (false);");
    parseAndFold("do { x = 1; break; } while (false);");
    parseAndFold("for (;false;) { x = 1; }");
    parseAndFold("for (;true;) { x = 1; break; }");
  }

  @Test
  public void testLogicalAndOrFolding() {
    parseAndFold("var a = true && x;");
    parseAndFold("var b = false && x;");
    parseAndFold("var c = true || x;");
    parseAndFold("var d = false || x;");
    parseAndFold("if (x && true) {}");
    parseAndFold("if (x || false) {}");
    parseAndFold("if (x && false) {}");
    parseAndFold("if (x || true) {}");
  }

  @Test
  public void testBitwiseAndOrShiftFolding() {
    parseAndFold("var a = 1 & 3;");
    parseAndFold("var b = 1 | 2;");
    parseAndFold("var c = 1.5 & 2;");
    parseAndFold("var d = 1 << 2;");
    parseAndFold("var e = 8 >> 1;");
    parseAndFold("var f = -8 >>> 1;");
    parseAndFold("var g = 1 << 35;");
    parseAndFold("var h = 1.5 << 2;");
    parseAndFold("var i = 1 << 1.5;");
    parseAndFold("var j = 5000000000 << 1;");
  }

  @Test
  public void testArithmeticFolding() {
    parseAndFold("var a = 1 + 2;");
    parseAndFold("var b = 5 - 3;");
    parseAndFold("var c = 4 * 2;");
    parseAndFold("var d = 8 / 2;");
    parseAndFold("var e = 8 / 0;");
    parseAndFold("var f = 'a' + 'b';");
    parseAndFold("var g = 'a' + 1;");
    parseAndFold("var h = foo() + 'a' + 'b';");
    parseAndFold("var i = foo() + 2 + 'a';");
  }

  @Test
  public void testAssignCompoundFolding() {
    parseAndFold("x = x + y;");
    parseAndFold("x = x - y;");
    parseAndFold("x = x * y;");
    parseAndFold("x = x / y;");
    parseAndFold("x = x % y;");
    parseAndFold("x = x & y;");
    parseAndFold("x = x | y;");
    parseAndFold("x = x ^ y;");
    parseAndFold("x = x << y;");
    parseAndFold("x = x >> y;");
    parseAndFold("x = x >>> y;");
  }

  @Test
  public void testComparisonFolding() {
    parseAndFold("var a = (void 0) == (void 0);");
    parseAndFold("var b = (void 0) == null;");
    parseAndFold("var c = (void 0) === (void 0);");
    parseAndFold("var d = (void 0) != 1;");
    parseAndFold("var e = (void 0) !== 1;");
    parseAndFold("var f = (void 0) < 1;");
    parseAndFold("var g = null == (void 0);");
    parseAndFold("var h = null == null;");
    parseAndFold("var i = true == false;");
    parseAndFold("var j = this == this;");
    parseAndFold("var k = 'abc' == 'abc';");
    parseAndFold("var l = 'abc' != 'def';");
    parseAndFold("var m = 1 == 1;");
    parseAndFold("var n = 1 != 2;");
    parseAndFold("var o = 1 < 2;");
    parseAndFold("var p = 2 <= 2;");
    parseAndFold("var q = 3 > 2;");
    parseAndFold("var r = 3 >= 3;");
    parseAndFold("var s = undefined == undefined;");
    parseAndFold("var t = undefined == null;");
    parseAndFold("var u = undefined === undefined;");
    parseAndFold("var v = undefined < 1;");
    parseAndFold("var w = undefined == 1;");
    parseAndFold("var x = 'a' == 'b';");
    parseAndFold("var y = 1 == 2;");
    parseAndFold("var z = 2 < 1;");
  }

  @Test
  public void testGetPropAndGetElemFolding() {
    parseAndFold("var a = [1, 2, 3].length;");
    parseAndFold("var b = 'hello'.length;");
    parseAndFold("var c = [1, 2, 3][0];");
    parseAndFold("var d = [1, 2, 3][1];");
    parseAndFold("var e = [1, 2, 3][2];");
    parseAndFold("var f = [1, 2, 3][3];");
    parseAndFold("var g = [1, 2, 3][-1];");
    parseAndFold("var h = [1, 2, 3][1.5];");
  }

  @Test
  public void testStringMethodsFolding() {
    parseAndFold("var a = 'abcdef'.indexOf('bc');");
    parseAndFold("var b = 'abcdefbc'.indexOf('bc', 3);");
    parseAndFold("var c = 'abcdefbc'.lastIndexOf('bc');");
    parseAndFold("var d = 'abcdefbc'.lastIndexOf('bc', 3);");
    parseAndFold("var e = ['a', 'b', 'c'].join('');");
    parseAndFold("var f = ['a', 'b', 'c'].join(',');");
    parseAndFold("var g = [].join('');");
    parseAndFold("var h = ['a'].join('');");
    parseAndFold("var i = [x, 'a'].join('');");
  }

  @Test
  public void testContainsUnicodeEscape() {
    Assert.assertTrue(FoldConstants.containsUnicodeEscape("\u0000"));
    Assert.assertTrue(FoldConstants.containsUnicodeEscape("\uFFFF"));
    Assert.assertFalse(FoldConstants.containsUnicodeEscape("abc"));
  }

  @Test
  public void testTryFoldBlock() {
    parseAndFold("{ ;;; }");
    parseAndFold("{ var a = 1; { var b = 2; } }");
  }

  @Test
  public void testConditionMinimization() {
    parseAndFold("if (!(!x)) foo();");
    parseAndFold("if (!(!x && !y)) foo();");
    parseAndFold("if (!(!x || !y)) foo();");
  }
}