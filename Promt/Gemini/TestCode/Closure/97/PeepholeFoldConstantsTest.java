package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PeepholeFoldConstantsTest {

  private PeepholeFoldConstants folder;

  @Before
  public void setUp() {
    folder = new PeepholeFoldConstants();
  }

  private Node wrapInBlock(Node n) {
    Node block = new Node(Token.BLOCK);
    block.addChildToBack(n);
    return block;
  }

  private Node wrapInExprResult(Node n) {
    Node expr = new Node(Token.EXPR_RESULT);
    expr.addChildToBack(n);
    return expr;
  }

  private Node wrapInParent(Node parent, Node child) {
    parent.addChildToBack(child);
    return parent;
  }

  // ===================== TYPEOF TESTS =====================

  @Test
  public void testFoldTypeof() {
    // typeof "hello" -> "string"
    Node strNode = Node.newString("hello");
    Node typeofNode = new Node(Token.TYPEOF, strNode);
    wrapInBlock(typeofNode);
    Node result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("string", result.getString());

    // typeof 123 -> "number"
    Node numNode = Node.newNumber(123);
    typeofNode = new Node(Token.TYPEOF, numNode);
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("number", result.getString());

    // typeof true -> "boolean"
    typeofNode = new Node(Token.TYPEOF, new Node(Token.TRUE));
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("boolean", result.getString());

    // typeof false -> "boolean"
    typeofNode = new Node(Token.TYPEOF, new Node(Token.FALSE));
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("boolean", result.getString());

    // typeof null -> "object"
    typeofNode = new Node(Token.TYPEOF, new Node(Token.NULL));
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("object", result.getString());

    // typeof {} -> "object"
    typeofNode = new Node(Token.TYPEOF, new Node(Token.OBJECTLIT));
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("object", result.getString());

    // typeof [] -> "object"
    typeofNode = new Node(Token.TYPEOF, new Node(Token.ARRAYLIT));
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("object", result.getString());

    // typeof void 0 -> "undefined"
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    typeofNode = new Node(Token.TYPEOF, voidNode);
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("undefined", result.getString());

    // typeof undefined (NAME) -> "undefined"
    typeofNode = new Node(Token.TYPEOF, Node.newString(Token.NAME, "undefined"));
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("undefined", result.getString());

    // typeof non-literal or other name -> no fold
    typeofNode = new Node(Token.TYPEOF, Node.newString(Token.NAME, "foo"));
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.TYPEOF, result.getType());

    // typeof with empty children
    typeofNode = new Node(Token.TYPEOF);
    wrapInBlock(typeofNode);
    result = folder.optimizeSubtree(typeofNode);
    Assert.assertEquals(Token.TYPEOF, result.getType());
  }

  // ===================== UNARY OPERATOR TESTS =====================

  @Test
  public void testFoldUnaryNot() {
    // !true -> false
    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    wrapInBlock(notNode);
    Node result = folder.optimizeSubtree(notNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // !false -> true
    notNode = new Node(Token.NOT, new Node(Token.FALSE));
    wrapInBlock(notNode);
    result = folder.optimizeSubtree(notNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // !0 -> true
    notNode = new Node(Token.NOT, Node.newNumber(0));
    wrapInBlock(notNode);
    result = folder.optimizeSubtree(notNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // !1 -> false
    notNode = new Node(Token.NOT, Node.newNumber(1));
    wrapInBlock(notNode);
    result = folder.optimizeSubtree(notNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // !x (unknown) -> no fold
    notNode = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    wrapInBlock(notNode);
    result = folder.optimizeSubtree(notNode);
    Assert.assertEquals(Token.NOT, result.getType());

    // In expression statement: (!true;) -> drop unary operator
    notNode = new Node(Token.NOT, new Node(Token.TRUE));
    Node expr = wrapInExprResult(notNode);
    result = folder.optimizeSubtree(notNode);
    Assert.assertNull(result);
    Assert.assertEquals(Token.TRUE, expr.getFirstChild().getType());
  }

  @Test
  public void testFoldUnaryNeg() {
    // -5 -> -5.0
    Node negNode = new Node(Token.NEG, Node.newNumber(5));
    wrapInBlock(negNode);
    Node result = folder.optimizeSubtree(negNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(-5.0, result.getDouble(), 1e-9);

    // -Infinity -> return n
    negNode = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    wrapInBlock(negNode);
    result = folder.optimizeSubtree(negNode);
    Assert.assertEquals(Token.NEG, result.getType());

    // -NaN -> NaN
    negNode = new Node(Token.NEG, Node.newString(Token.NAME, "NaN"));
    wrapInBlock(negNode);
    result = folder.optimizeSubtree(negNode);
    Assert.assertEquals(Token.NAME, result.getType());
    Assert.assertEquals("NaN", result.getString());

    // - "abc" (non-number) -> warning error, return n
    negNode = new Node(Token.NEG, Node.newString("abc"));
    wrapInBlock(negNode);
    result = folder.optimizeSubtree(negNode);
    Assert.assertEquals(Token.NEG, result.getType());
  }

  @Test
  public void testFoldUnaryBitnot() {
    // ~0 -> -1
    Node bitnotNode = new Node(Token.BITNOT, Node.newNumber(0));
    wrapInBlock(bitnotNode);
    Node result = folder.optimizeSubtree(bitnotNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(-1.0, result.getDouble(), 1e-9);

    // ~5 -> -6
    bitnotNode = new Node(Token.BITNOT, Node.newNumber(5));
    wrapInBlock(bitnotNode);
    result = folder.optimizeSubtree(bitnotNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(-6.0, result.getDouble(), 1e-9);

    // ~1.5 (fractional) -> error, no fold
    bitnotNode = new Node(Token.BITNOT, Node.newNumber(1.5));
    wrapInBlock(bitnotNode);
    result = folder.optimizeSubtree(bitnotNode);
    Assert.assertEquals(Token.BITNOT, result.getType());

    // ~1e12 (out of range) -> error, no fold
    bitnotNode = new Node(Token.BITNOT, Node.newNumber(1e12));
    wrapInBlock(bitnotNode);
    result = folder.optimizeSubtree(bitnotNode);
    Assert.assertEquals(Token.BITNOT, result.getType());

    // ~"abc" (non-number) -> error, no fold
    bitnotNode = new Node(Token.BITNOT, Node.newString("abc"));
    wrapInBlock(bitnotNode);
    result = folder.optimizeSubtree(bitnotNode);
    Assert.assertEquals(Token.BITNOT, result.getType());
  }

  // ===================== INSTANCEOF TESTS =====================

  @Test
  public void testFoldInstanceof() {
    // 5 instanceof Object -> false (immutable is never instance)
    Node instNode = new Node(Token.INSTANCEOF, Node.newNumber(5), Node.newString(Token.NAME, "Object"));
    wrapInBlock(instNode);
    Node result = folder.optimizeSubtree(instNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // "str" instanceof Object -> false
    instNode = new Node(Token.INSTANCEOF, Node.newString("str"), Node.newString(Token.NAME, "Object"));
    wrapInBlock(instNode);
    result = folder.optimizeSubtree(instNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // {} instanceof Object -> true
    instNode = new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "Object"));
    wrapInBlock(instNode);
    result = folder.optimizeSubtree(instNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // {} instanceof OtherClass -> no fold
    instNode = new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "OtherClass"));
    wrapInBlock(instNode);
    result = folder.optimizeSubtree(instNode);
    Assert.assertEquals(Token.INSTANCEOF, result.getType());

    // x instanceof Object -> no fold
    instNode = new Node(Token.INSTANCEOF, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "Object"));
    wrapInBlock(instNode);
    result = folder.optimizeSubtree(instNode);
    Assert.assertEquals(Token.INSTANCEOF, result.getType());
  }

  // ===================== ASSIGN TESTS =====================

  @Test
  public void testFoldAssign() {
    // x = x + y -> x += y
    Node nameLeft = Node.newString(Token.NAME, "x");
    Node addRight = new Node(Token.ADD, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y"));
    Node assign = new Node(Token.ASSIGN, nameLeft, addRight);
    wrapInBlock(assign);
    Node result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_ADD, result.getType());
    Assert.assertEquals("x", result.getFirstChild().getString());
    Assert.assertEquals("y", result.getLastChild().getString());

    // x = x - y -> x -= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.SUB, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_SUB, result.getType());

    // x = x * y -> x *= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.MUL, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_MUL, result.getType());

    // x = x / y -> x /= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.DIV, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_DIV, result.getType());

    // x = x % y -> x %= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.MOD, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_MOD, result.getType());

    // x = x & y -> x &= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.BITAND, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_BITAND, result.getType());

    // x = x | y -> x |= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.BITOR, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_BITOR, result.getType());

    // x = x ^ y -> x ^= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.BITXOR, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_BITXOR, result.getType());

    // x = x << y -> x <<= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.LSH, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_LSH, result.getType());

    // x = x >> y -> x >>= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.RSH, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_RSH, result.getType());

    // x = x >>> y -> x >>>= y
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.URSH, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_URSH, result.getType());

    // Non-matching target: x = z + y -> no fold
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.ADD, Node.newString(Token.NAME, "z"), Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN, result.getType());

    // Right child does not have two children: x = +y -> no fold
    assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"),
        new Node(Token.POS, Node.newString(Token.NAME, "y")));
    wrapInBlock(assign);
    result = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN, result.getType());
  }

  // ===================== AND / OR TESTS =====================

  @Test
  public void testFoldAndOr() {
    // true || x -> true
    Node orNode = new Node(Token.OR, new Node(Token.TRUE), Node.newString(Token.NAME, "x"));
    wrapInBlock(orNode);
    Node result = folder.optimizeSubtree(orNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // false || x -> x
    orNode = new Node(Token.OR, new Node(Token.FALSE), Node.newString(Token.NAME, "x"));
    wrapInBlock(orNode);
    result = folder.optimizeSubtree(orNode);
    Assert.assertEquals(Token.NAME, result.getType());
    Assert.assertEquals("x", result.getString());

    // false && x -> false
    Node andNode = new Node(Token.AND, new Node(Token.FALSE), Node.newString(Token.NAME, "x"));
    wrapInBlock(andNode);
    result = folder.optimizeSubtree(andNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // true && x -> x
    andNode = new Node(Token.AND, new Node(Token.TRUE), Node.newString(Token.NAME, "x"));
    wrapInBlock(andNode);
    result = folder.optimizeSubtree(andNode);
    Assert.assertEquals(Token.NAME, result.getType());
    Assert.assertEquals("x", result.getString());

    // Inside IF condition: if (x || false) -> if (x)
    orNode = new Node(Token.OR, Node.newString(Token.NAME, "x"), new Node(Token.FALSE));
    Node ifNode = new Node(Token.IF, orNode, new Node(Token.BLOCK));
    result = folder.optimizeSubtree(orNode);
    Assert.assertEquals(Token.NAME, result.getType());
    Assert.assertEquals("x", result.getString());

    // Inside IF condition: if (x && true) -> if (x)
    andNode = new Node(Token.AND, Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    ifNode = new Node(Token.IF, andNode, new Node(Token.BLOCK));
    result = folder.optimizeSubtree(andNode);
    Assert.assertEquals(Token.NAME, result.getType());
    Assert.assertEquals("x", result.getString());

    // Inside IF condition: if (x || true) -> if (true)
    orNode = new Node(Token.OR, Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    ifNode = new Node(Token.IF, orNode, new Node(Token.BLOCK));
    result = folder.optimizeSubtree(orNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // Inside IF condition: if (x && false) -> if (false)
    andNode = new Node(Token.AND, Node.newString(Token.NAME, "x"), new Node(Token.FALSE));
    ifNode = new Node(Token.IF, andNode, new Node(Token.BLOCK));
    result = folder.optimizeSubtree(andNode);
    Assert.assertEquals(Token.FALSE, result.getType());
  }

  // ===================== ADD / STRING CONCAT TESTS =====================

  @Test
  public void testFoldAdd() {
    // "a" + "b" -> "ab"
    Node addNode = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
    wrapInBlock(addNode);
    Node result = folder.optimizeSubtree(addNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("ab", result.getString());

    // "a" + 1 -> "a1"
    addNode = new Node(Token.ADD, Node.newString("a"), Node.newNumber(1));
    wrapInBlock(addNode);
    result = folder.optimizeSubtree(addNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("a1", result.getString());

    // 1 + "b" -> "1b"
    addNode = new Node(Token.ADD, Node.newNumber(1), Node.newString("b"));
    wrapInBlock(addNode);
    result = folder.optimizeSubtree(addNode);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("1b", result.getString());

    // 1 + 2 -> 3
    addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    wrapInBlock(addNode);
    result = folder.optimizeSubtree(addNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(3.0, result.getDouble(), 1e-9);

    // Left child add: (foo() + "a") + "b" -> foo() + "ab"
    Node callFoo = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node innerAdd = new Node(Token.ADD, callFoo, Node.newString("a"));
    Node outerAdd = new Node(Token.ADD, innerAdd, Node.newString("b"));
    wrapInBlock(outerAdd);
    result = folder.optimizeSubtree(outerAdd);
    Assert.assertEquals(Token.ADD, result.getType());
    Assert.assertEquals(Token.CALL, result.getFirstChild().getType());
    Assert.assertEquals("ab", result.getLastChild().getString());
  }

  // ===================== ARITHMETIC TESTS =====================

  @Test
  public void testFoldArithmetic() {
    // 10 - 4 -> 6
    Node subNode = new Node(Token.SUB, Node.newNumber(10), Node.newNumber(4));
    wrapInBlock(subNode);
    Node result = folder.optimizeSubtree(subNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(6.0, result.getDouble(), 1e-9);

    // 3 * 4 -> 12
    Node mulNode = new Node(Token.MUL, Node.newNumber(3), Node.newNumber(4));
    wrapInBlock(mulNode);
    result = folder.optimizeSubtree(mulNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(12.0, result.getDouble(), 1e-9);

    // 12 / 3 -> 4
    Node divNode = new Node(Token.DIV, Node.newNumber(12), Node.newNumber(3));
    wrapInBlock(divNode);
    result = folder.optimizeSubtree(divNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(4.0, result.getDouble(), 1e-9);

    // 12 / 0 -> Divide by 0 error, no fold
    divNode = new Node(Token.DIV, Node.newNumber(12), Node.newNumber(0));
    wrapInBlock(divNode);
    result = folder.optimizeSubtree(divNode);
    Assert.assertEquals(Token.DIV, result.getType());
  }

  // ===================== BITWISE TESTS =====================

  @Test
  public void testFoldBitAndOr() {
    // 5 & 3 -> 1
    Node bitandNode = new Node(Token.BITAND, Node.newNumber(5), Node.newNumber(3));
    wrapInBlock(bitandNode);
    Node result = folder.optimizeSubtree(bitandNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(1.0, result.getDouble(), 1e-9);

    // 4 | 2 -> 6
    Node bitorNode = new Node(Token.BITOR, Node.newNumber(4), Node.newNumber(2));
    wrapInBlock(bitorNode);
    result = folder.optimizeSubtree(bitorNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(6.0, result.getDouble(), 1e-9);

    // Fractional numbers: 1.5 & 2 -> no fold
    bitandNode = new Node(Token.BITAND, Node.newNumber(1.5), Node.newNumber(2));
    wrapInBlock(bitandNode);
    result = folder.optimizeSubtree(bitandNode);
    Assert.assertEquals(Token.BITAND, result.getType());

    // Out of range: 1e12 | 2 -> no fold
    bitorNode = new Node(Token.BITOR, Node.newNumber(1e12), Node.newNumber(2));
    wrapInBlock(bitorNode);
    result = folder.optimizeSubtree(bitorNode);
    Assert.assertEquals(Token.BITOR, result.getType());
  }

  // ===================== SHIFT TESTS =====================

  @Test
  public void testFoldShift() {
    // 1 << 2 -> 4
    Node lshNode = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(2));
    wrapInBlock(lshNode);
    Node result = folder.optimizeSubtree(lshNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(4.0, result.getDouble(), 1e-9);

    // 8 >> 2 -> 2
    Node rshNode = new Node(Token.RSH, Node.newNumber(8), Node.newNumber(2));
    wrapInBlock(rshNode);
    result = folder.optimizeSubtree(rshNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(2.0, result.getDouble(), 1e-9);

    // -1 >>> 0 -> 4294967295
    Node urshNode = new Node(Token.URSH, Node.newNumber(-1), Node.newNumber(0));
    wrapInBlock(urshNode);
    result = folder.optimizeSubtree(urshNode);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(4294967295.0, result.getDouble(), 1e-9);

    // Shift amount out of bounds: 1 << 32 -> error, no fold
    lshNode = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(32));
    wrapInBlock(lshNode);
    result = folder.optimizeSubtree(lshNode);
    Assert.assertEquals(Token.LSH, result.getType());

    // Shift operand out of range: 1e12 << 2 -> error, no fold
    lshNode = new Node(Token.LSH, Node.newNumber(1e12), Node.newNumber(2));
    wrapInBlock(lshNode);
    result = folder.optimizeSubtree(lshNode);
    Assert.assertEquals(Token.LSH, result.getType());

    // Fractional operand: 1.5 << 2 -> error, no fold
    lshNode = new Node(Token.LSH, Node.newNumber(1.5), Node.newNumber(2));
    wrapInBlock(lshNode);
    result = folder.optimizeSubtree(lshNode);
    Assert.assertEquals(Token.LSH, result.getType());
  }

  // ===================== COMPARISON TESTS =====================

  @Test
  public void testFoldComparisonNumbers() {
    // 1 == 1 -> true
    Node cmpNode = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(1));
    wrapInBlock(cmpNode);
    Node result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // 1 != 2 -> true
    cmpNode = new Node(Token.NE, Node.newNumber(1), Node.newNumber(2));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // 1 < 2 -> true
    cmpNode = new Node(Token.LT, Node.newNumber(1), Node.newNumber(2));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // 2 <= 1 -> false
    cmpNode = new Node(Token.LE, Node.newNumber(2), Node.newNumber(1));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // 2 > 1 -> true
    cmpNode = new Node(Token.GT, Node.newNumber(2), Node.newNumber(1));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // 1 >= 2 -> false
    cmpNode = new Node(Token.GE, Node.newNumber(1), Node.newNumber(2));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // 1 === 1 -> true
    cmpNode = new Node(Token.SHEQ, Node.newNumber(1), Node.newNumber(1));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // 1 !== 1 -> false
    cmpNode = new Node(Token.SHNE, Node.newNumber(1), Node.newNumber(1));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // 1 == undefined -> false
    cmpNode = new Node(Token.EQ, Node.newNumber(1), Node.newString(Token.NAME, "undefined"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());
  }

  @Test
  public void testFoldComparisonStrings() {
    // "a" == "a" -> true
    Node cmpNode = new Node(Token.EQ, Node.newString("a"), Node.newString("a"));
    wrapInBlock(cmpNode);
    Node result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // "a" == "b" -> false
    cmpNode = new Node(Token.EQ, Node.newString("a"), Node.newString("b"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // "a" != "b" -> true
    cmpNode = new Node(Token.NE, Node.newString("a"), Node.newString("b"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // "a" === "a" -> true
    cmpNode = new Node(Token.SHEQ, Node.newString("a"), Node.newString("a"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // "a" !== "a" -> false
    cmpNode = new Node(Token.SHNE, Node.newString("a"), Node.newString("a"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // "a" == undefined -> false
    cmpNode = new Node(Token.EQ, Node.newString("a"), Node.newString(Token.NAME, "undefined"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());
  }

  @Test
  public void testFoldComparisonVoidNullAndBooleans() {
    // void 0 == void 0 -> true
    Node void1 = new Node(Token.VOID, Node.newNumber(0));
    Node void2 = new Node(Token.VOID, Node.newNumber(0));
    Node cmpNode = new Node(Token.EQ, void1, void2);
    wrapInBlock(cmpNode);
    Node result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // void 0 == null -> true
    void1 = new Node(Token.VOID, Node.newNumber(0));
    cmpNode = new Node(Token.EQ, void1, new Node(Token.NULL));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // void 0 === null -> false
    void1 = new Node(Token.VOID, Node.newNumber(0));
    cmpNode = new Node(Token.SHEQ, void1, new Node(Token.NULL));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // null == undefined (NAME) -> true
    cmpNode = new Node(Token.EQ, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // true == undefined (NAME) -> false
    cmpNode = new Node(Token.EQ, new Node(Token.TRUE), Node.newString(Token.NAME, "undefined"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // true === true -> true
    cmpNode = new Node(Token.SHEQ, new Node(Token.TRUE), new Node(Token.TRUE));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // true === false -> false
    cmpNode = new Node(Token.SHEQ, new Node(Token.TRUE), new Node(Token.FALSE));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // this == this -> true
    cmpNode = new Node(Token.EQ, new Node(Token.THIS), new Node(Token.THIS));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // undefined == null -> true
    cmpNode = new Node(Token.EQ, Node.newString(Token.NAME, "undefined"), new Node(Token.NULL));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.TRUE, result.getType());

    // x < x -> false
    cmpNode = new Node(Token.LT, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "x"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());

    // x > x -> false
    cmpNode = new Node(Token.GT, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "x"));
    wrapInBlock(cmpNode);
    result = folder.optimizeSubtree(cmpNode);
    Assert.assertEquals(Token.FALSE, result.getType());
  }

  // ===================== GETELEM TESTS =====================

  @Test
  public void testFoldGetElem() {
    // [10, 20, 30][1] -> 20
    Node array = new Node(Token.ARRAYLIT, Node.newNumber(10), Node.newNumber(20), Node.newNumber(30));
    Node getelem = new Node(Token.GETELEM, array, Node.newNumber(1));
    wrapInBlock(getelem);
    Node result = folder.optimizeSubtree(getelem);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(20.0, result.getDouble(), 1e-9);

    // [10, 20][0] -> 10
    array = new Node(Token.ARRAYLIT, Node.newNumber(10), Node.newNumber(20));
    getelem = new Node(Token.GETELEM, array, Node.newNumber(0));
    wrapInBlock(getelem);
    result = folder.optimizeSubtree(getelem);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(10.0, result.getDouble(), 1e-9);

    // [10, 20][-1] -> error, no fold
    array = new Node(Token.ARRAYLIT, Node.newNumber(10), Node.newNumber(20));
    getelem = new Node(Token.GETELEM, array, Node.newNumber(-1));
    wrapInBlock(getelem);
    result = folder.optimizeSubtree(getelem);
    Assert.assertEquals(Token.GETELEM, result.getType());

    // [10, 20][5] -> error, no fold
    array = new Node(Token.ARRAYLIT, Node.newNumber(10), Node.newNumber(20));
    getelem = new Node(Token.GETELEM, array, Node.newNumber(5));
    wrapInBlock(getelem);
    result = folder.optimizeSubtree(getelem);
    Assert.assertEquals(Token.GETELEM, result.getType());

    // [10, 20][1.5] -> error, no fold
    array = new Node(Token.ARRAYLIT, Node.newNumber(10), Node.newNumber(20));
    getelem = new Node(Token.GETELEM, array, Node.newNumber(1.5));
    wrapInBlock(getelem);
    result = folder.optimizeSubtree(getelem);
    Assert.assertEquals(Token.GETELEM, result.getType());

    // [10, 20]["foo"] -> no fold
    array = new Node(Token.ARRAYLIT, Node.newNumber(10), Node.newNumber(20));
    getelem = new Node(Token.GETELEM, array, Node.newString("foo"));
    wrapInBlock(getelem);
    result = folder.optimizeSubtree(getelem);
    Assert.assertEquals(Token.GETELEM, result.getType());
  }

  // ===================== GETPROP TESTS =====================

  @Test
  public void testFoldGetProp() {
    // [1, 2, 3].length -> 3
    Node array = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2), Node.newNumber(3));
    Node getprop = new Node(Token.GETPROP, array, Node.newString("length"));
    wrapInBlock(getprop);
    Node result = folder.optimizeSubtree(getprop);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(3.0, result.getDouble(), 1e-9);

    // "hello".length -> 5
    Node str = Node.newString("hello");
    getprop = new Node(Token.GETPROP, str, Node.newString("length"));
    wrapInBlock(getprop);
    result = folder.optimizeSubtree(getprop);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(5.0, result.getDouble(), 1e-9);

    // [foo()].length (side effects) -> no fold
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    array = new Node(Token.ARRAYLIT, call);
    getprop = new Node(Token.GETPROP, array, Node.newString("length"));
    wrapInBlock(getprop);
    result = folder.optimizeSubtree(getprop);
    Assert.assertEquals(Token.GETPROP, result.getType());

    // [1, 2].otherProp -> no fold
    array = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    getprop = new Node(Token.GETPROP, array, Node.newString("otherProp"));
    wrapInBlock(getprop);
    result = folder.optimizeSubtree(getprop);
    Assert.assertEquals(Token.GETPROP, result.getType());
  }

  // ===================== KNOWN METHODS: INDEXOF / JOIN =====================

  @Test
  public void testFoldStringIndexOf() {
    // "abcdef".indexOf("bc") -> 1
    Node getprop = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("indexOf"));
    Node call = new Node(Token.CALL, getprop, Node.newString("bc"));
    wrapInBlock(call);
    Node result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.NUMBER, result.getType());
    Assert.assertEquals(1.0, result.getDouble(), 1e-9);

    // "abcdefbc".indexOf("bc", 3) -> 6
    getprop = new Node(Token.GETPROP, Node.newString("abcdefbc"), Node.newString("indexOf"));
    call = new Node(Token.CALL, getprop, Node.newString("bc"), Node.newNumber(3));
    wrapInBlock(call);
    result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.NUMBER, result.getDouble(), 1e-9);

    // "abcdef".lastIndexOf("cd") -> 2
    getprop = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("lastIndexOf"));
    call = new Node(Token.CALL, getprop, Node.newString("cd"));
    wrapInBlock(call);
    result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.NUMBER, result.getDouble(), 1e-9);

    // "abcdef".lastIndexOf("cd", 1) -> -1
    getprop = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("lastIndexOf"));
    call = new Node(Token.CALL, getprop, Node.newString("cd"), Node.newNumber(1));
    wrapInBlock(call);
    result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.NUMBER, result.getDouble(), 1e-9);
  }

  @Test
  public void testFoldStringJoin() {
    // ["a", "b", "c"].join("") -> "abc"
    Node array = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"), Node.newString("c"));
    Node getprop = new Node(Token.GETPROP, array, Node.newString("join"));
    Node call = new Node(Token.CALL, getprop, Node.newString(""));
    wrapInBlock(call);
    Node result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("abc", result.getString());

    // [].join(",") -> ""
    array = new Node(Token.ARRAYLIT);
    getprop = new Node(Token.GETPROP, array, Node.newString("join"));
    call = new Node(Token.CALL, getprop, Node.newString(","));
    wrapInBlock(call);
    result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("", result.getString());

    // ["a"].join(",") -> "a"
    array = new Node(Token.ARRAYLIT, Node.newString("a"));
    getprop = new Node(Token.GETPROP, array, Node.newString("join"));
    call = new Node(Token.CALL, getprop, Node.newString(","));
    wrapInBlock(call);
    result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("a", result.getString());

    // [1, 2, 3].join(",") -> "1,2,3"
    array = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2), Node.newNumber(3));
    getprop = new Node(Token.GETPROP, array, Node.newString("join"));
    call = new Node(Token.CALL, getprop, Node.newString(","));
    wrapInBlock(call);
    result = folder.optimizeSubtree(call);
    Assert.assertEquals(Token.STRING, result.getType());
    Assert.assertEquals("1,2,3", result.getString());
  }

  // ===================== EDGE CASES & NULL CHECKS =====================

  @Test
  public void testEmptyBinaryOperators() {
    // Binary operator with 0 children
    Node emptyAdd = new Node(Token.ADD);
    Node result = folder.optimizeSubtree(emptyAdd);
    Assert.assertEquals(Token.ADD, result.getType());

    // Binary operator with 1 child
    Node singleChildAdd = new Node(Token.ADD, Node.newNumber(1));
    result = folder.optimizeSubtree(singleChildAdd);
    Assert.assertEquals(Token.ADD, result.getType());
  }

  @Test
  public void testDefaultFallback() {
    // Token that is not explicitly handled (e.g. EMPTY or VAR)
    Node varNode = new Node(Token.VAR);
    Node result = folder.optimizeSubtree(varNode);
    Assert.assertEquals(Token.VAR, result.getType());
  }
}