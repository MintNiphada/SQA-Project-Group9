package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class PeepholeFoldConstantsTest {

  private Node fold(Node n) {
    Node parent = new Node(Token.EXPR_RESULT, n);
    PeepholeFoldConstants folder = new PeepholeFoldConstants();
    Node result = folder.optimizeSubtree(n);
    return result;
  }

  private Node wrapInParent(Node n) {
    Node parent = new Node(Token.EXPR_RESULT, n);
    return n;
  }

  @Test
  public void testOptimizeSubtreeNullChildren() {
    PeepholeFoldConstants folder = new PeepholeFoldConstants();
    Node n = new Node(Token.ADD);
    Node res = folder.optimizeSubtree(n);
    assertSame(n, res);

    Node n2 = new Node(Token.ADD, Node.newNumber(1));
    Node res2 = folder.optimizeSubtree(n2);
    assertSame(n2, res2);
  }

  @Test
  public void testFoldVoid() {
    Node voidNode = new Node(Token.VOID, Node.newString("hello"));
    wrapInParent(voidNode);
    PeepholeFoldConstants folder = new PeepholeFoldConstants();
    Node res = folder.optimizeSubtree(voidNode);
    assertEquals(Token.VOID, res.getType());
    assertEquals(0.0, res.getFirstChild().getDouble(), 0.0);

    Node voidZero = new Node(Token.VOID, Node.newNumber(0));
    wrapInParent(voidZero);
    Node resZero = folder.optimizeSubtree(voidZero);
    assertSame(voidZero, resZero);
  }

  @Test
  public void testFoldTypeof() {
    Node typeofFunc = new Node(Token.TYPEOF, new Node(Token.FUNCTION, Node.newString("f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK)));
    assertEquals("function", fold(typeofFunc).getString());

    Node typeofStr = new Node(Token.TYPEOF, Node.newString("abc"));
    assertEquals("string", fold(typeofStr).getString());

    Node typeofNum = new Node(Token.TYPEOF, Node.newNumber(42));
    assertEquals("number", fold(typeofNum).getString());

    Node typeofTrue = new Node(Token.TYPEOF, new Node(Token.TRUE));
    assertEquals("boolean", fold(typeofTrue).getString());

    Node typeofFalse = new Node(Token.TYPEOF, new Node(Token.FALSE));
    assertEquals("boolean", fold(typeofFalse).getString());

    Node typeofNull = new Node(Token.TYPEOF, new Node(Token.NULL));
    assertEquals("object", fold(typeofNull).getString());

    Node typeofObj = new Node(Token.TYPEOF, new Node(Token.OBJECTLIT));
    assertEquals("object", fold(typeofObj).getString());

    Node typeofArr = new Node(Token.TYPEOF, new Node(Token.ARRAYLIT));
    assertEquals("object", fold(typeofArr).getString());

    Node typeofVoid = new Node(Token.TYPEOF, new Node(Token.VOID, Node.newNumber(0)));
    assertEquals("undefined", fold(typeofVoid).getString());

    Node typeofUndefName = new Node(Token.TYPEOF, Node.newString(Token.NAME, "undefined"));
    assertEquals("undefined", fold(typeofUndefName).getString());

    Node typeofOtherName = new Node(Token.TYPEOF, Node.newString(Token.NAME, "foo"));
    Node res = fold(typeofOtherName);
    assertEquals(Token.TYPEOF, res.getType());
  }

  @Test
  public void testFoldUnaryNot() {
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(Token.FALSE, fold(notTrue).getType());

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals(Token.TRUE, fold(notFalse).getType());

    Node notZero = new Node(Token.NOT, Node.newNumber(0));
    assertEquals(Token.NOT, fold(notZero).getType());

    Node notOne = new Node(Token.NOT, Node.newNumber(1));
    assertEquals(Token.NOT, fold(notOne).getType());

    Node notTwo = new Node(Token.NOT, Node.newNumber(2));
    assertEquals(Token.FALSE, fold(notTwo).getType());
  }

  @Test
  public void testFoldUnaryPos() {
    Node posNum = new Node(Token.POS, Node.newNumber(5));
    Node res = fold(posNum);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(5.0, res.getDouble(), 0.0);

    Node posStr = new Node(Token.POS, Node.newString("5"));
    Node resStr = fold(posStr);
    assertEquals(Token.NUMBER, resStr.getType());
    assertEquals(5.0, resStr.getDouble(), 0.0);
  }

  @Test
  public void testFoldUnaryNeg() {
    Node negNum = new Node(Token.NEG, Node.newNumber(5));
    Node res = fold(negNum);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(-5.0, res.getDouble(), 0.0);

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    Node resInf = fold(negInf);
    assertEquals(Token.NEG, resInf.getType());

    Node negNaN = new Node(Token.NEG, Node.newString(Token.NAME, "NaN"));
    Node resNaN = fold(negNaN);
    assertEquals(Token.NAME, resNaN.getType());
    assertEquals("NaN", resNaN.getString());

    Node negStr = new Node(Token.NEG, Node.newString("non-numeric"));
    Node resErr = fold(negStr);
    assertEquals(Token.NEG, resErr.getType());
  }

  @Test
  public void testFoldUnaryBitnot() {
    Node bitnot = new Node(Token.BITNOT, Node.newNumber(1));
    Node res = fold(bitnot);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(~1, (int) res.getDouble());

    Node bitnotFrac = new Node(Token.BITNOT, Node.newNumber(1.5));
    Node resFrac = fold(bitnotFrac);
    assertEquals(Token.BITNOT, resFrac.getType());

    Node bitnotRange = new Node(Token.BITNOT, Node.newNumber(1e15));
    Node resRange = fold(bitnotRange);
    assertEquals(Token.BITNOT, resRange.getType());

    Node bitnotStr = new Node(Token.BITNOT, Node.newString("str"));
    Node resStr = fold(bitnotStr);
    assertEquals(Token.BITNOT, resStr.getType());
  }

  @Test
  public void testFoldBinaryAndOr() {
    Node orTrue = new Node(Token.OR, new Node(Token.TRUE), Node.newString(Token.NAME, "x"));
    assertEquals(Token.TRUE, fold(orTrue).getType());

    Node orFalse = new Node(Token.OR, new Node(Token.FALSE), Node.newString(Token.NAME, "x"));
    Node resOrFalse = fold(orFalse);
    assertEquals(Token.NAME, resOrFalse.getType());
    assertEquals("x", resOrFalse.getString());

    Node andTrue = new Node(Token.AND, new Node(Token.TRUE), Node.newString(Token.NAME, "x"));
    Node resAndTrue = fold(andTrue);
    assertEquals(Token.NAME, resAndTrue.getType());
    assertEquals("x", resAndTrue.getString());

    Node andFalse = new Node(Token.AND, new Node(Token.FALSE), Node.newString(Token.NAME, "x"));
    assertEquals(Token.FALSE, fold(andFalse).getType());

    Node andUnknown = new Node(Token.AND, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    assertEquals(Token.AND, fold(andUnknown).getType());
  }

  @Test
  public void testFoldArithmeticOps() {
    Node add = new Node(Token.ADD, Node.newNumber(10), Node.newNumber(20));
    assertEquals(30.0, fold(add).getDouble(), 0.0);

    Node sub = new Node(Token.SUB, Node.newNumber(10), Node.newNumber(4));
    assertEquals(6.0, fold(sub).getDouble(), 0.0);

    Node mul = new Node(Token.MUL, Node.newNumber(3), Node.newNumber(4));
    assertEquals(12.0, fold(mul).getDouble(), 0.0);

    Node div = new Node(Token.DIV, Node.newNumber(12), Node.newNumber(3));
    assertEquals(4.0, fold(div).getDouble(), 0.0);

    Node divZero = new Node(Token.DIV, Node.newNumber(12), Node.newNumber(0));
    assertEquals(Token.DIV, fold(divZero).getType());

    Node mod = new Node(Token.MOD, Node.newNumber(10), Node.newNumber(3));
    assertEquals(1.0, fold(mod).getDouble(), 0.0);

    Node modZero = new Node(Token.MOD, Node.newNumber(10), Node.newNumber(0));
    assertEquals(Token.MOD, fold(modZero).getType());

    Node bitand = new Node(Token.BITAND, Node.newNumber(7), Node.newNumber(2));
    assertEquals(2.0, fold(bitand).getDouble(), 0.0);

    Node bitor = new Node(Token.BITOR, Node.newNumber(5), Node.newNumber(2));
    assertEquals(7.0, fold(bitor).getDouble(), 0.0);

    Node bitxor = new Node(Token.BITXOR, Node.newNumber(5), Node.newNumber(3));
    assertEquals(6.0, fold(bitxor).getDouble(), 0.0);
  }

  @Test
  public void testFoldStringAdd() {
    Node addStr = new Node(Token.ADD, Node.newString("foo"), Node.newString("bar"));
    Node res = fold(addStr);
    assertEquals(Token.STRING, res.getType());
    assertEquals("foobar", res.getString());

    Node addNumStr = new Node(Token.ADD, Node.newString("a"), Node.newNumber(1));
    assertEquals("a1", fold(addNumStr).getString());

    Node leftAdd = new Node(Token.ADD, Node.newString(Token.NAME, "foo"), Node.newString("a"));
    Node nestedAdd = new Node(Token.ADD, leftAdd, Node.newString("b"));
    Node resNested = fold(nestedAdd);
    assertEquals(Token.ADD, resNested.getType());
    assertEquals("ab", resNested.getLastChild().getString());

    Node rightAdd = new Node(Token.ADD, Node.newString("b"), Node.newString(Token.NAME, "foo"));
    Node nestedAddRight = new Node(Token.ADD, Node.newString("a"), rightAdd);
    Node resNestedRight = fold(nestedAddRight);
    assertEquals(Token.ADD, resNestedRight.getType());
    assertEquals("ab", resNestedRight.getFirstChild().getString());
  }

  @Test
  public void testFoldLeftChildArithmetic() {
    Node leftMul = new Node(Token.MUL, Node.newString(Token.NAME, "foo"), Node.newNumber(2));
    Node outerMul = new Node(Token.MUL, leftMul, Node.newNumber(3));
    Node res = fold(outerMul);
    assertEquals(Token.MUL, res.getType());
    assertEquals(6.0, res.getLastChild().getDouble(), 0.0);
  }

  @Test
  public void testFoldShifts() {
    Node lsh = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(3));
    assertEquals(8.0, fold(lsh).getDouble(), 0.0);

    Node rsh = new Node(Token.RSH, Node.newNumber(8), Node.newNumber(2));
    assertEquals(2.0, fold(rsh).getDouble(), 0.0);

    Node ursh = new Node(Token.URSH, Node.newNumber(-1), Node.newNumber(0));
    assertEquals(4294967295.0, fold(ursh).getDouble(), 0.0);

    Node badLshRange = new Node(Token.LSH, Node.newNumber(1e15), Node.newNumber(1));
    assertEquals(Token.LSH, fold(badLshRange).getType());

    Node badLshAmount = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(33));
    assertEquals(Token.LSH, fold(badLshAmount).getType());

    Node badLshFrac1 = new Node(Token.LSH, Node.newNumber(1.5), Node.newNumber(1));
    assertEquals(Token.LSH, fold(badLshFrac1).getType());

    Node badLshFrac2 = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(1.5));
    assertEquals(Token.LSH, fold(badLshFrac2).getType());
  }

  @Test
  public void testFoldAssign() {
    Node x = Node.newString(Token.NAME, "x");
    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Node assign = new Node(Token.ASSIGN, x, add);
    Node res = fold(assign);
    assertEquals(Token.ASSIGN_ADD, res.getType());

    Node x2 = Node.newString(Token.NAME, "x");
    Node add2 = new Node(Token.ADD, Node.newNumber(1), Node.newString(Token.NAME, "x"));
    Node assign2 = new Node(Token.ASSIGN, x2, add2);
    Node res2 = fold(assign2);
    assertEquals(Token.ASSIGN_ADD, res2.getType());

    Node x3 = Node.newString(Token.NAME, "x");
    Node sub = new Node(Token.SUB, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Node assign3 = new Node(Token.ASSIGN, x3, sub);
    Node res3 = fold(assign3);
    assertEquals(Token.ASSIGN_SUB, res3.getType());
  }

  @Test
  public void testFoldComparison() {
    Node eqNum = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(1));
    assertEquals(Token.TRUE, fold(eqNum).getType());

    Node neNum = new Node(Token.NE, Node.newNumber(1), Node.newNumber(2));
    assertEquals(Token.TRUE, fold(neNum).getType());

    Node ltNum = new Node(Token.LT, Node.newNumber(1), Node.newNumber(2));
    assertEquals(Token.TRUE, fold(ltNum).getType());

    Node gtNum = new Node(Token.GT, Node.newNumber(2), Node.newNumber(1));
    assertEquals(Token.TRUE, fold(gtNum).getType());

    Node leNum = new Node(Token.LE, Node.newNumber(1), Node.newNumber(1));
    assertEquals(Token.TRUE, fold(leNum).getType());

    Node geNum = new Node(Token.GE, Node.newNumber(2), Node.newNumber(1));
    assertEquals(Token.TRUE, fold(geNum).getType());

    Node eqStr = new Node(Token.EQ, Node.newString("a"), Node.newString("a"));
    assertEquals(Token.TRUE, fold(eqStr).getType());

    Node neStr = new Node(Token.NE, Node.newString("a"), Node.newString("b"));
    assertEquals(Token.TRUE, fold(neStr).getType());

    Node eqNullUndef = new Node(Token.EQ, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    assertEquals(Token.TRUE, fold(eqNullUndef).getType());

    Node sheqNullUndef = new Node(Token.SHEQ, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    assertEquals(Token.FALSE, fold(sheqNullUndef).getType());

    Node eqThis = new Node(Token.EQ, new Node(Token.THIS), new Node(Token.THIS));
    assertEquals(Token.TRUE, fold(eqThis).getType());

    Node neThis = new Node(Token.NE, new Node(Token.THIS), new Node(Token.THIS));
    assertEquals(Token.FALSE, fold(neThis).getType());

    Node eqVoid = new Node(Token.EQ, new Node(Token.VOID, Node.newNumber(0)), new Node(Token.NULL));
    assertEquals(Token.TRUE, fold(eqVoid).getType());

    Node ltName = new Node(Token.LT, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "x"));
    assertEquals(Token.FALSE, fold(ltName).getType());
  }

  @Test
  public void testFoldInstanceof() {
    Node inst = new Node(Token.INSTANCEOF, Node.newString("hello"), Node.newString(Token.NAME, "Object"));
    assertEquals(Token.FALSE, fold(inst).getType());

    Node instObj = new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "Object"));
    assertEquals(Token.TRUE, fold(instObj).getType());

    Node instCustom = new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "CustomClass"));
    assertEquals(Token.INSTANCEOF, fold(instCustom).getType());
  }

  @Test
  public void testFoldGetProp() {
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    Node getProp = new Node(Token.GETPROP, arr, Node.newString("length"));
    assertEquals(2.0, fold(getProp).getDouble(), 0.0);

    Node str = Node.newString("hello");
    Node getPropStr = new Node(Token.GETPROP, str, Node.newString("length"));
    assertEquals(5.0, fold(getPropStr).getDouble(), 0.0);

    Node obj = new Node(Token.OBJECTLIT, Node.newString(Token.STRING, "a"));
    obj.getFirstChild().addChildToBack(Node.newNumber(42));
    Node getPropObj = new Node(Token.GETPROP, obj, Node.newString("a"));
    Node resObj = fold(getPropObj);
    assertEquals(42.0, resObj.getDouble(), 0.0);
  }

  @Test
  public void testFoldGetElem() {
    Node arr = new Node(Token.ARRAYLIT, Node.newString("first"), Node.newString("second"));
    Node getElem = new Node(Token.GETELEM, arr, Node.newNumber(1));
    Node res = fold(getElem);
    assertEquals("second", res.getString());

    Node arrEmpty = new Node(Token.ARRAYLIT, new Node(Token.EMPTY));
    Node getElemEmpty = new Node(Token.GETELEM, arrEmpty, Node.newNumber(0));
    Node resEmpty = fold(getElemEmpty);
    assertEquals(Token.VOID, resEmpty.getType());

    Node arrOOB = new Node(Token.ARRAYLIT, Node.newNumber(1));
    Node getElemOOB = new Node(Token.GETELEM, arrOOB, Node.newNumber(5));
    assertEquals(Token.GETELEM, fold(getElemOOB).getType());

    Node arrNeg = new Node(Token.ARRAYLIT, Node.newNumber(1));
    Node getElemNeg = new Node(Token.GETELEM, arrNeg, Node.newNumber(-1));
    assertEquals(Token.GETELEM, fold(getElemNeg).getType());

    Node arrFrac = new Node(Token.ARRAYLIT, Node.newNumber(1));
    Node getElemFrac = new Node(Token.GETELEM, arrFrac, Node.newNumber(0.5));
    assertEquals(Token.GETELEM, fold(getElemFrac).getType());
  }

  @Test
  public void testFoldCtorCall() {
    Node newString = new Node(Token.NEW, Node.newString(Token.NAME, "String"), Node.newString("prop"));
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "obj"), newString);
    wrapInParent(getElem);

    PeepholeFoldConstants folder = new PeepholeFoldConstants();
    Node res = folder.optimizeSubtree(newString);
    assertEquals(Token.STRING, res.getType());
    assertEquals("prop", res.getString());

    Node newStringEmpty = new Node(Token.NEW, Node.newString(Token.NAME, "String"));
    Node getElemEmpty = new Node(Token.GETELEM, Node.newString(Token.NAME, "obj"), newStringEmpty);
    wrapInParent(getElemEmpty);
    Node resEmpty = folder.optimizeSubtree(newStringEmpty);
    assertEquals(Token.STRING, resEmpty.getType());
    assertEquals("", resEmpty.getString());
  }

  @Test
  public void testConvertToNumberOperands() {
    Node subHook = new Node(Token.SUB, new Node(Token.HOOK, new Node(Token.TRUE), Node.newString("10"), Node.newString("20")), Node.newNumber(5));
    Node res = fold(subHook);
    assertEquals(Token.SUB, res.getType());
    assertEquals(10.0, res.getFirstChild().getChildAtIndex(1).getDouble(), 0.0);
    assertEquals(20.0, res.getFirstChild().getLastChild().getDouble(), 0.0);

    Node bitand = new Node(Token.ASSIGN_BITAND, Node.newString(Token.NAME, "x"), Node.newString("15"));
    Node resBitand = fold(bitand);
    assertEquals(15.0, resBitand.getLastChild().getDouble(), 0.0);
  }

  @Test
  public void testObjectPropAccessGetter() {
    Node getter = Node.newString(Token.GET, "prop");
    getter.addChildToBack(new Node(Token.FUNCTION, Node.newString(""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK)));
    Node obj = new Node(Token.OBJECTLIT, getter);
    Node getProp = new Node(Token.GETPROP, obj, Node.newString("prop"));
    Node res = fold(getProp);
    assertEquals(Token.GETPROP, res.getType());
  }
}
