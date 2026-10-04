package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PeepholeFoldConstantsTest {

  private PeepholeFoldConstants folder;
  private AbstractCompiler compiler;

  @Before
  public void setUp() {
    folder = new PeepholeFoldConstants();
    compiler = new Compiler();
    folder.beginTraversal(new NodeTraversal(compiler, null));
  }

  private Node wrapInRoot(Node n) {
    Node root = new Node(Token.BLOCK, n);
    return n;
  }

  private Node wrapInExpr(Node n) {
    Node expr = new Node(Token.EXPR_RESULT, n);
    new Node(Token.BLOCK, expr);
    return n;
  }

  @Test
  public void testOptimizeSubtreeNullChildren() {
    Node n = wrapInRoot(new Node(Token.ADD));
    Node res = folder.optimizeSubtree(n);
    Assert.assertEquals(Token.ADD, res.getType());

    Node singleChild = wrapInRoot(new Node(Token.ADD, Node.newNumber(1)));
    Node resSingle = folder.optimizeSubtree(singleChild);
    Assert.assertEquals(Token.ADD, resSingle.getType());
  }

  @Test
  public void testTypeofFolding() {
    Node stringType = wrapInRoot(new Node(Token.TYPEOF, Node.newString("hello")));
    Assert.assertEquals("string", folder.optimizeSubtree(stringType).getString());

    Node numType = wrapInRoot(new Node(Token.TYPEOF, Node.newNumber(123)));
    Assert.assertEquals("number", folder.optimizeSubtree(numType).getString());

    Node boolType = wrapInRoot(new Node(Token.TYPEOF, new Node(Token.TRUE)));
    Assert.assertEquals("boolean", folder.optimizeSubtree(boolType).getString());

    Node boolTypeFalse = wrapInRoot(new Node(Token.TYPEOF, new Node(Token.FALSE)));
    Assert.assertEquals("boolean", folder.optimizeSubtree(boolTypeFalse).getString());

    Node nullType = wrapInRoot(new Node(Token.TYPEOF, new Node(Token.NULL)));
    Assert.assertEquals("object", folder.optimizeSubtree(nullType).getString());

    Node objType = wrapInRoot(new Node(Token.TYPEOF, new Node(Token.OBJECTLIT)));
    Assert.assertEquals("object", folder.optimizeSubtree(objType).getString());

    Node arrType = wrapInRoot(new Node(Token.TYPEOF, new Node(Token.ARRAYLIT)));
    Assert.assertEquals("object", folder.optimizeSubtree(arrType).getString());

    Node undefType = wrapInRoot(new Node(Token.TYPEOF, Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals("undefined", folder.optimizeSubtree(undefType).getString());

    Node nonLiteral = wrapInRoot(new Node(Token.TYPEOF, Node.newString(Token.NAME, "foo")));
    Assert.assertEquals(Token.TYPEOF, folder.optimizeSubtree(nonLiteral).getType());
  }

  @Test
  public void testUnaryOperatorInExpressionNode() {
    Node child = Node.newNumber(1);
    Node not = wrapInExpr(new Node(Token.NOT, child));
    Node res = folder.optimizeSubtree(not);
    Assert.assertNull(res);
  }

  @Test
  public void testUnaryNot() {
    Node notTrue = wrapInRoot(new Node(Token.NOT, new Node(Token.TRUE)));
    Assert.assertEquals(Token.FALSE, folder.optimizeSubtree(notTrue).getType());

    Node notFalse = wrapInRoot(new Node(Token.NOT, new Node(Token.FALSE)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(notFalse).getType());

    Node notUnknown = wrapInRoot(new Node(Token.NOT, Node.newString(Token.NAME, "x")));
    Assert.assertEquals(Token.NOT, folder.optimizeSubtree(notUnknown).getType());
  }

  @Test
  public void testUnaryNeg() {
    Node negNum = wrapInRoot(new Node(Token.NEG, Node.newNumber(5.5)));
    Assert.assertEquals(-5.5, folder.optimizeSubtree(negNum).getDouble(), 0.0);

    Node negInf = wrapInRoot(new Node(Token.NEG, Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals(Token.NEG, folder.optimizeSubtree(negInf).getType());

    Node negNan = wrapInRoot(new Node(Token.NEG, Node.newString(Token.NAME, "NaN")));
    Node resNan = folder.optimizeSubtree(negNan);
    Assert.assertEquals(Token.NAME, resNan.getType());
    Assert.assertEquals("NaN", resNan.getString());

    Node negInvalid = wrapInRoot(new Node(Token.NEG, Node.newString("str")));
    Assert.assertEquals(Token.NEG, folder.optimizeSubtree(negInvalid).getType());
  }

  @Test
  public void testUnaryBitNot() {
    Node bitNot = wrapInRoot(new Node(Token.BITNOT, Node.newNumber(5)));
    Assert.assertEquals(~5, (int) folder.optimizeSubtree(bitNot).getDouble());

    Node bitNotFractional = wrapInRoot(new Node(Token.BITNOT, Node.newNumber(5.5)));
    Assert.assertEquals(Token.BITNOT, folder.optimizeSubtree(bitNotFractional).getType());

    Node bitNotOutOfRange = wrapInRoot(new Node(Token.BITNOT, Node.newNumber(1e12)));
    Assert.assertEquals(Token.BITNOT, folder.optimizeSubtree(bitNotOutOfRange).getType());

    Node bitNotInvalid = wrapInRoot(new Node(Token.BITNOT, Node.newString("str")));
    Assert.assertEquals(Token.BITNOT, folder.optimizeSubtree(bitNotInvalid).getType());
  }

  @Test
  public void testInstanceof() {
    Node inst1 = wrapInRoot(new Node(Token.INSTANCEOF, Node.newString("a"), Node.newString(Token.NAME, "Object")));
    Assert.assertEquals(Token.FALSE, folder.optimizeSubtree(inst1).getType());

    Node inst2 = wrapInRoot(new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "Object")));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(inst2).getType());

    Node inst3 = wrapInRoot(new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "Array")));
    Assert.assertEquals(Token.INSTANCEOF, folder.optimizeSubtree(inst3).getType());
  }

  @Test
  public void testAssignInlining() {
    Node name1 = Node.newString(Token.NAME, "x");
    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Node assign = wrapInRoot(new Node(Token.ASSIGN, name1, add));
    Node res = folder.optimizeSubtree(assign);
    Assert.assertEquals(Token.ASSIGN_ADD, res.getType());

    int[] ops = {Token.BITAND, Token.BITOR, Token.BITXOR, Token.DIV, Token.LSH, Token.MOD, Token.MUL, Token.RSH, Token.SUB, Token.URSH};
    int[] expected = {Token.ASSIGN_BITAND, Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_DIV, Token.ASSIGN_LSH, Token.ASSIGN_MOD, Token.ASSIGN_MUL, Token.ASSIGN_RSH, Token.ASSIGN_SUB, Token.ASSIGN_URSH};
    for (int i = 0; i < ops.length; i++) {
      Node lhs = Node.newString(Token.NAME, "x");
      Node rhs = new Node(ops[i], Node.newString(Token.NAME, "x"), Node.newNumber(2));
      Node a = wrapInRoot(new Node(Token.ASSIGN, lhs, rhs));
      Assert.assertEquals(expected[i], folder.optimizeSubtree(a).getType());
    }

    Node assignMismatch = wrapInRoot(new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), new Node(Token.ADD, Node.newString(Token.NAME, "y"), Node.newNumber(1))));
    Assert.assertEquals(Token.ASSIGN, folder.optimizeSubtree(assignMismatch).getType());
  }

  @Test
  public void testAndOr() {
    Node orTrue = wrapInRoot(new Node(Token.OR, new Node(Token.TRUE), Node.newString(Token.NAME, "b")));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(orTrue).getType());

    Node andFalse = wrapInRoot(new Node(Token.AND, new Node(Token.FALSE), Node.newString(Token.NAME, "b")));
    Assert.assertEquals(Token.FALSE, folder.optimizeSubtree(andFalse).getType());

    Node orFalse = wrapInRoot(new Node(Token.OR, new Node(Token.FALSE), Node.newString(Token.NAME, "b")));
    Assert.assertEquals("b", folder.optimizeSubtree(orFalse).getString());

    Node andTrue = wrapInRoot(new Node(Token.AND, new Node(Token.TRUE), Node.newString(Token.NAME, "b")));
    Assert.assertEquals("b", folder.optimizeSubtree(andTrue).getString());

    Node ifParent = new Node(Token.IF);
    Node orInIf = new Node(Token.OR, Node.newString(Token.NAME, "a"), new Node(Token.FALSE));
    ifParent.addChildToBack(orInIf);
    wrapInRoot(ifParent);
    Assert.assertEquals("a", folder.optimizeSubtree(orInIf).getString());

    Node ifParent2 = new Node(Token.IF);
    Node orInIf2 = new Node(Token.OR, Node.newString(Token.NAME, "a"), new Node(Token.TRUE));
    ifParent2.addChildToBack(orInIf2);
    wrapInRoot(ifParent2);
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(orInIf2).getType());
  }

  @Test
  public void testAdd() {
    Node addConst = wrapInRoot(new Node(Token.ADD, Node.newString("a"), Node.newString("b")));
    Assert.assertEquals("ab", folder.optimizeSubtree(addConst).getString());

    Node addNumbers = wrapInRoot(new Node(Token.ADD, Node.newNumber(2), Node.newNumber(3)));
    Assert.assertEquals(5.0, folder.optimizeSubtree(addNumbers).getDouble(), 0.0);

    Node nestedLeft = new Node(Token.ADD, Node.newString(Token.NAME, "foo"), Node.newString("a"));
    Node addLeftTree = wrapInRoot(new Node(Token.ADD, nestedLeft, Node.newString("b")));
    Node resAdd = folder.optimizeSubtree(addLeftTree);
    Assert.assertEquals("ab", resAdd.getLastChild().getString());
  }

  @Test
  public void testArithmetic() {
    Node sub = wrapInRoot(new Node(Token.SUB, Node.newNumber(5), Node.newNumber(3)));
    Assert.assertEquals(2.0, folder.optimizeSubtree(sub).getDouble(), 0.0);

    Node mul = wrapInRoot(new Node(Token.MUL, Node.newNumber(4), Node.newNumber(2.5)));
    Assert.assertEquals(10.0, folder.optimizeSubtree(mul).getDouble(), 0.0);

    Node div = wrapInRoot(new Node(Token.DIV, Node.newNumber(10), Node.newNumber(2)));
    Assert.assertEquals(5.0, folder.optimizeSubtree(div).getDouble(), 0.0);

    Node divZero = wrapInRoot(new Node(Token.DIV, Node.newNumber(10), Node.newNumber(0)));
    Assert.assertEquals(Token.DIV, folder.optimizeSubtree(divZero).getType());

    Node overflow = wrapInRoot(new Node(Token.MUL, Node.newNumber(1e15), Node.newNumber(1e15)));
    Assert.assertEquals(Token.MUL, folder.optimizeSubtree(overflow).getType());
  }

  @Test
  public void testBitwise() {
    Node bitAnd = wrapInRoot(new Node(Token.BITAND, Node.newNumber(6), Node.newNumber(3)));
    Assert.assertEquals(2.0, folder.optimizeSubtree(bitAnd).getDouble(), 0.0);

    Node bitOr = wrapInRoot(new Node(Token.BITOR, Node.newNumber(6), Node.newNumber(3)));
    Assert.assertEquals(7.0, folder.optimizeSubtree(bitOr).getDouble(), 0.0);

    Node bitFractional = wrapInRoot(new Node(Token.BITAND, Node.newNumber(6.5), Node.newNumber(3)));
    Assert.assertEquals(Token.BITAND, folder.optimizeSubtree(bitFractional).getType());

    Node bitOutRange = wrapInRoot(new Node(Token.BITAND, Node.newNumber(1e12), Node.newNumber(3)));
    Assert.assertEquals(Token.BITAND, folder.optimizeSubtree(bitOutRange).getType());
  }

  @Test
  public void testShift() {
    Node lsh = wrapInRoot(new Node(Token.LSH, Node.newNumber(1), Node.newNumber(2)));
    Assert.assertEquals(4.0, folder.optimizeSubtree(lsh).getDouble(), 0.0);

    Node rsh = wrapInRoot(new Node(Token.RSH, Node.newNumber(-4), Node.newNumber(1)));
    Assert.assertEquals(-2.0, folder.optimizeSubtree(rsh).getDouble(), 0.0);

    Node ursh = wrapInRoot(new Node(Token.URSH, Node.newNumber(-1), Node.newNumber(1)));
    Assert.assertEquals(2147483647.0, folder.optimizeSubtree(ursh).getDouble(), 0.0);

    Node shiftFractional = wrapInRoot(new Node(Token.LSH, Node.newNumber(1.5), Node.newNumber(2)));
    Assert.assertEquals(Token.LSH, folder.optimizeSubtree(shiftFractional).getType());

    Node shiftOutRange = wrapInRoot(new Node(Token.LSH, Node.newNumber(1), Node.newNumber(33)));
    Assert.assertEquals(Token.LSH, folder.optimizeSubtree(shiftOutRange).getType());
  }

  @Test
  public void testComparison() {
    Node eqStr = wrapInRoot(new Node(Token.EQ, Node.newString("a"), Node.newString("a")));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(eqStr).getType());

    Node neStr = wrapInRoot(new Node(Token.NE, Node.newString("a"), Node.newString("b")));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(neStr).getType());

    Node ltNum = wrapInRoot(new Node(Token.LT, Node.newNumber(1), Node.newNumber(2)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(ltNum).getType());

    Node leNum = wrapInRoot(new Node(Token.LE, Node.newNumber(2), Node.newNumber(2)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(leNum).getType());

    Node gtNum = wrapInRoot(new Node(Token.GT, Node.newNumber(1), Node.newNumber(2)));
    Assert.assertEquals(Token.FALSE, folder.optimizeSubtree(gtNum).getType());

    Node geNum = wrapInRoot(new Node(Token.GE, Node.newNumber(2), Node.newNumber(2)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(geNum).getType());

    Node eqVoid = wrapInRoot(new Node(Token.EQ, new Node(Token.VOID, Node.newNumber(0)), new Node(Token.NULL)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(eqVoid).getType());

    Node sheqVoid = wrapInRoot(new Node(Token.SHEQ, new Node(Token.VOID, Node.newNumber(0)), new Node(Token.NULL)));
    Assert.assertEquals(Token.FALSE, folder.optimizeSubtree(sheqVoid).getType());

    Node shneVoid = wrapInRoot(new Node(Token.SHNE, new Node(Token.VOID, Node.newNumber(0)), new Node(Token.NULL)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(shneVoid).getType());

    Node ltVoid = wrapInRoot(new Node(Token.LT, new Node(Token.VOID, Node.newNumber(0)), new Node(Token.NULL)));
    Assert.assertEquals(Token.FALSE, folder.optimizeSubtree(ltVoid).getType());

    Node eqUndefName = wrapInRoot(new Node(Token.EQ, Node.newString(Token.NAME, "undefined"), new Node(Token.NULL)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(eqUndefName).getType());

    Node eqThis = wrapInRoot(new Node(Token.EQ, new Node(Token.THIS), new Node(Token.THIS)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(eqThis).getType());

    Node neThis = wrapInRoot(new Node(Token.NE, new Node(Token.THIS), new Node(Token.NULL)));
    Assert.assertEquals(Token.TRUE, folder.optimizeSubtree(neThis).getType());

    Node ltName = wrapInRoot(new Node(Token.LT, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "x")));
    Assert.assertEquals(Token.FALSE, folder.optimizeSubtree(ltName).getType());
  }

  @Test
  public void testStringIndexOf() {
    Node getProp = new Node(Token.GETPROP, Node.newString("hello world"), Node.newString("indexOf"));
    Node call = wrapInRoot(new Node(Token.CALL, getProp, Node.newString("world")));
    Assert.assertEquals(6.0, folder.optimizeSubtree(call).getDouble(), 0.0);

    Node getPropLast = new Node(Token.GETPROP, Node.newString("hello world world"), Node.newString("lastIndexOf"));
    Node callLast = wrapInRoot(new Node(Token.CALL, getPropLast, Node.newString("world"), Node.newNumber(15)));
    Assert.assertEquals(12.0, folder.optimizeSubtree(callLast).getDouble(), 0.0);
  }

  @Test
  public void testStringJoin() {
    Node arr = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
    Node getProp = new Node(Token.GETPROP, arr, Node.newString("join"));
    Node call = wrapInRoot(new Node(Token.CALL, getProp, Node.newString(",")));
    Assert.assertEquals("a,b", folder.optimizeSubtree(call).getString());

    Node emptyArr = new Node(Token.ARRAYLIT);
    Node getPropEmpty = new Node(Token.GETPROP, emptyArr, Node.newString("join"));
    Node callEmpty = wrapInRoot(new Node(Token.CALL, getPropEmpty, Node.newString(",")));
    Assert.assertEquals("", folder.optimizeSubtree(callEmpty).getString());
  }

  @Test
  public void testGetElem() {
    Node arr = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"), Node.newString("c"));
    Node getElem = wrapInRoot(new Node(Token.GETELEM, arr, Node.newNumber(1)));
    Assert.assertEquals("b", folder.optimizeSubtree(getElem).getString());

    Node getElemOob = wrapInRoot(new Node(Token.GETELEM, arr.cloneTree(), Node.newNumber(5)));
    Assert.assertEquals(Token.GETELEM, folder.optimizeSubtree(getElemOob).getType());

    Node getElemNeg = wrapInRoot(new Node(Token.GETELEM, arr.cloneTree(), Node.newNumber(-1)));
    Assert.assertEquals(Token.GETELEM, folder.optimizeSubtree(getElemNeg).getType());

    Node getElemFract = wrapInRoot(new Node(Token.GETELEM, arr.cloneTree(), Node.newNumber(1.2)));
    Assert.assertEquals(Token.GETELEM, folder.optimizeSubtree(getElemFract).getType());
  }

  @Test
  public void testGetPropLength() {
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    Node getPropArr = wrapInRoot(new Node(Token.GETPROP, arr, Node.newString("length")));
    Assert.assertEquals(2.0, folder.optimizeSubtree(getPropArr).getDouble(), 0.0);

    Node str = Node.newString("hello");
    Node getPropStr = wrapInRoot(new Node(Token.GETPROP, str, Node.newString("length")));
    Assert.assertEquals(5.0, folder.optimizeSubtree(getPropStr).getDouble(), 0.0);

    Node otherProp = wrapInRoot(new Node(Token.GETPROP, str, Node.newString("other")));
    Assert.assertEquals(Token.GETPROP, folder.optimizeSubtree(otherProp).getType());
  }
}
