package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

class PeepholeReplaceKnownMethodsTest {

  private Compiler compiler;
  private PeepholeReplaceKnownMethods peephole;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    peephole = new PeepholeReplaceKnownMethods();
  }

  private Node fold(String js) {
    Node root = compiler.parseTestCode(js);
    Node block = root;
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, peephole);
    pass.process(null, block);
    return block;
  }

  private String foldAndPrint(String js) {
    Node root = fold(js);
    return compiler.toSource(root).trim();
  }

  private Node foldNormalized(String js) {
    Node root = compiler.parseTestCode(js);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(null, root);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, peephole);
    pass.process(null, root);
    return root;
  }

  private String foldNormalizedAndPrint(String js) {
    Node root = foldNormalized(js);
    return compiler.toSource(root).trim();
  }

  @Test
  public void testOptimizeSubtreeNonCall() {
    Node node = Node.newString("test");
    Node result = peephole.optimizeSubtree(node);
    assertSame(node, result);
  }

  @Test
  public void testOptimizeSubtreeEmptyCall() {
    Node call = new Node(Token.CALL);
    Node parent = new Node(Token.EXPR_RESULT, call);
    Node result = peephole.optimizeSubtree(call);
    assertSame(call, result);
  }

  @Test
  public void testStringMethodsUpperCaseAndLowerCase() {
    assertEquals("\"abc\";", foldAndPrint("'ABC'.toLowerCase();"));
    assertEquals("\"ABC\";", foldAndPrint("'abc'.toUpperCase();"));
    assertEquals("\"hello world\";", foldAndPrint("'HELLO WORLD'.toLowerCase();"));
    assertEquals("\"HELLO WORLD\";", foldAndPrint("'hello world'.toUpperCase();"));
  }

  @Test
  public void testStringIndexOf() {
    assertEquals("1;", foldAndPrint("'abcdef'.indexOf('bc');"));
    assertEquals("3;", foldAndPrint("'abcdef'.indexOf('def');"));
    assertEquals("-1;", foldAndPrint("'abcdef'.indexOf('xyz');"));
    assertEquals("1;", foldAndPrint("'abcdef'.indexOf('bc', 0);"));
    assertEquals("1;", foldAndPrint("'abcdef'.indexOf('bc', 1);"));
    assertEquals("-1;", foldAndPrint("'abcdef'.indexOf('bc', 2);"));
    assertEquals("5;", foldAndPrint("'abcdefbc'.indexOf('bc', 3);"));
    assertEquals("0;", foldAndPrint("'abcdef'.indexOf('');"));
  }

  @Test
  public void testStringLastIndexOf() {
    assertEquals("1;", foldAndPrint("'abcdef'.lastIndexOf('bc');"));
    assertEquals("6;", foldAndPrint("'abcdefbc'.lastIndexOf('bc');"));
    assertEquals("1;", foldAndPrint("'abcdefbc'.lastIndexOf('bc', 5);"));
    assertEquals("-1;", foldAndPrint("'abcdef'.lastIndexOf('xyz');"));
    assertEquals("6;", foldAndPrint("'abcdef'.lastIndexOf('');"));
  }

  @Test
  public void testStringSubstr() {
    assertEquals("\"bc\";", foldAndPrint("'abcdef'.substr(1, 2);"));
    assertEquals("\"cdef\";", foldAndPrint("'abcdef'.substr(2);"));
    assertEquals("\"a\";", foldAndPrint("'abcdef'.substr(0, 1);"));
    assertEquals("\"abcdef\";", foldAndPrint("'abcdef'.substr(0, 6);"));
    assertEquals("\"abcdef\".substr(0, 7);", foldAndPrint("'abcdef'.substr(0, 7);"));
    assertEquals("\"abcdef\".substr(-1);", foldAndPrint("'abcdef'.substr(-1);"));
    assertEquals("\"abcdef\".substr(1, -1);", foldAndPrint("'abcdef'.substr(1, -1);"));
    assertEquals("\"abcdef\".substr(1, 2, 3);", foldAndPrint("'abcdef'.substr(1, 2, 3);"));
    assertEquals("\"abcdef\".substr(\"a\");", foldAndPrint("'abcdef'.substr('a');"));
  }

  @Test
  public void testStringSubstring() {
    assertEquals("\"bc\";", foldAndPrint("'abcdef'.substring(1, 3);"));
    assertEquals("\"cdef\";", foldAndPrint("'abcdef'.substring(2);"));
    assertEquals("\"abcdef\";", foldAndPrint("'abcdef'.substring(0, 6);"));
    assertEquals("\"abcdef\".substring(0, 7);", foldAndPrint("'abcdef'.substring(0, 7);"));
    assertEquals("\"abcdef\".substring(7, 2);", foldAndPrint("'abcdef'.substring(7, 2);"));
    assertEquals("\"abcdef\".substring(-1);", foldAndPrint("'abcdef'.substring(-1);"));
    assertEquals("\"abcdef\".substring(1, -1);", foldAndPrint("'abcdef'.substring(1, -1);"));
    assertEquals("\"abcdef\".substring(1, 2, 3);", foldAndPrint("'abcdef'.substring(1, 2, 3);"));
    assertEquals("\"abcdef\".substring(\"a\");", foldAndPrint("'abcdef'.substring('a');"));
  }

  @Test
  public void testStringCharAt() {
    assertEquals("\"a\";", foldAndPrint("'abcdef'.charAt(0);"));
    assertEquals("\"b\";", foldAndPrint("'abcdef'.charAt(1);"));
    assertEquals("\"f\";", foldAndPrint("'abcdef'.charAt(5);"));
    assertEquals("\"abcdef\".charAt(-1);", foldAndPrint("'abcdef'.charAt(-1);"));
    assertEquals("\"abcdef\".charAt(6);", foldAndPrint("'abcdef'.charAt(6);"));
    assertEquals("\"abcdef\".charAt(1, 2);", foldAndPrint("'abcdef'.charAt(1, 2);"));
    assertEquals("\"abcdef\".charAt(\"0\");", foldAndPrint("'abcdef'.charAt('0');"));
  }

  @Test
  public void testStringCharCodeAt() {
    assertEquals("97;", foldAndPrint("'abcdef'.charCodeAt(0);"));
    assertEquals("98;", foldAndPrint("'abcdef'.charCodeAt(1);"));
    assertEquals("102;", foldAndPrint("'abcdef'.charCodeAt(5);"));
    assertEquals("\"abcdef\".charCodeAt(-1);", foldAndPrint("'abcdef'.charCodeAt(-1);"));
    assertEquals("\"abcdef\".charCodeAt(6);", foldAndPrint("'abcdef'.charCodeAt(6);"));
    assertEquals("\"abcdef\".charCodeAt(1, 2);", foldAndPrint("'abcdef'.charCodeAt(1, 2);"));
    assertEquals("\"abcdef\".charCodeAt(\"0\");", foldAndPrint("'abcdef'.charCodeAt('0');"));
  }

  @Test
  public void testArrayJoin() {
    assertEquals("\"abc\";", foldAndPrint("['a', 'b', 'c'].join('');"));
    assertEquals("\"a,b,c\";", foldAndPrint("['a', 'b', 'c'].join(',');"));
    assertEquals("\"a,b,c\";", foldAndPrint("['a', 'b', 'c'].join();"));
    assertEquals("\"123\";", foldAndPrint("[1, 2, 3].join('');"));
    assertEquals("\"1,2,3\";", foldAndPrint("[1, 2, 3].join();"));
    assertEquals("\"a true \";", foldAndPrint("['a', true, null].join(' ');"));
    assertEquals("\"null,undefined\";", foldAndPrint("[null, undefined].join(',');"));
    assertEquals("\"1,,3\";", foldAndPrint("[1, , 3].join(',');"));
    assertEquals("\"\";", foldAndPrint("[].join();"));
    assertEquals("\"\";", foldAndPrint("[].join('');"));
    assertEquals("\"a\";", foldAndPrint("['a'].join();"));
    assertEquals("\"\" + x;", foldAndPrint("[x].join();"));
    assertEquals("\"a\" + (x + \"b\");", foldAndPrint("['a', x, 'b'].join('');"));
    assertEquals("['a', x, 'b'].join(',');", foldAndPrint("['a', x, 'b'].join(',');"));
    assertEquals("[x, y].join('');", foldAndPrint("[x, y].join('');"));
  }

  @Test
  public void testParseInt() {
    assertEquals("1;", foldNormalizedAndPrint("parseInt('1');"));
    assertEquals("1;", foldNormalizedAndPrint("parseInt('1', 10);"));
    assertEquals("16;", foldNormalizedAndPrint("parseInt('10', 16);"));
    assertEquals("16;", foldNormalizedAndPrint("parseInt('0x10');"));
    assertEquals("16;", foldNormalizedAndPrint("parseInt('0X10');"));
    assertEquals("2;", foldNormalizedAndPrint("parseInt('10', 2);"));
    assertEquals("8;", foldNormalizedAndPrint("parseInt('10', 8);"));
    assertEquals("10;", foldNormalizedAndPrint("parseInt(10);"));
    assertEquals("10;", foldNormalizedAndPrint("parseInt(10, 10);"));
    assertEquals("16;", foldNormalizedAndPrint("parseInt(10, 16);"));
    assertEquals("10;", foldNormalizedAndPrint("parseInt('  10  ');"));
    assertEquals("parseInt('010');", foldNormalizedAndPrint("parseInt('010');"));
    assertEquals("parseInt('10', -1);", foldNormalizedAndPrint("parseInt('10', -1);"));
    assertEquals("parseInt('10', 1);", foldNormalizedAndPrint("parseInt('10', 1);"));
    assertEquals("parseInt('10', 37);", foldNormalizedAndPrint("parseInt('10', 37);"));
    assertEquals("parseInt('10', 10.5);", foldNormalizedAndPrint("parseInt('10', 10.5);"));
    assertEquals("parseInt('10', 10, 10);", foldNormalizedAndPrint("parseInt('10', 10, 10);"));
    assertEquals("parseInt('abc');", foldNormalizedAndPrint("parseInt('abc');"));
  }

  @Test
  public void testParseFloat() {
    assertEquals("1.11;", foldNormalizedAndPrint("parseFloat('1.11');"));
    assertEquals("1.11;", foldNormalizedAndPrint("parseFloat(1.11);"));
    assertEquals("10;", foldNormalizedAndPrint("parseFloat('10');"));
    assertEquals("0.5;", foldNormalizedAndPrint("parseFloat('0.5');"));
    assertEquals("0.5;", foldNormalizedAndPrint("parseFloat('  0.5  ');"));
    assertEquals("parseFloat('abc');", foldNormalizedAndPrint("parseFloat('abc');"));
    assertEquals("parseFloat('1.11', 10);", foldNormalizedAndPrint("parseFloat('1.11', 10);"));
  }

  @Test
  public void testNonMatchingMethodsIgnored() {
    assertEquals("'abc'.foo();", foldAndPrint("'abc'.foo();"));
    assertEquals("foo('abc');", foldAndPrint("foo('abc');"));
    assertEquals("x.indexOf('a');", foldAndPrint("x.indexOf('a');"));
    assertEquals("'abc'[x]();", foldAndPrint("'abc'[x]();"));
  }

  @Test
  public void testNumericMethodsNotNormalized() {
    assertEquals("parseInt('10');", foldAndPrint("parseInt('10');"));
    assertEquals("parseFloat('1.11');", foldAndPrint("parseFloat('1.11');"));
  }
}