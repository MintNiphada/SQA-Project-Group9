package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PeepholeFoldConstantsTest {

  private PeepholeFoldConstants folder;

  @Before
  public void setUp() {
    folder = new PeepholeFoldConstants();
  }

  private Node fold(Node n) {
    Node parent = new Node(Token.EXPR_RESULT, n);
    Node result = folder.optimizeSubtree(n);
    if (result != null && result.getParent() == null) {
      return result;
    }
    return parent.getFirstChild();
  }

  @Test
  public void testOptimizeSubtreeNullAndBasic() {
    Node node = new Node(Token.EMPTY);
    Node folded = fold(node);
    assertNotNull(folded);
    assertEquals(Token.EMPTY, folded.getType());
  }

  @Test
  public void testTryReduceVoid() {
    Node void0 = new Node(Token.VOID, Node.newNumber(0));
    Node folded0 = fold(void0);
    assertEquals(Token.VOID, folded0.getType());
    assertEquals(0.0, folded0.getFirstChild().getDouble(), 0.0);

    Node void1 = new Node(Token.VOID, Node.newNumber(10));
    Node folded1 = fold(void1);
    assertEquals(Token.VOID, folded1.getType());
    assertEquals(0.0, folded1.getFirstChild().getDouble(), 0.0);

    Node voidStr = new Node(Token.VOID, Node.newString("hello"));
    Node foldedStr = fold(voidStr);
    assertEquals(Token.VOID, foldedStr.getType());
    assertEquals(0.0, foldedStr.getFirstChild().getDouble(), 0.0);
  }

  @Test
  public void testTryFoldTypeof() {
    Node typeofStr = new Node(Token.TYPEOF, Node.newString("abc"));
    Node res = fold(typeofStr);
    assertEquals(Token.STRING, res.getType());
    assertEquals("string", res.getString());

    Node typeofNum = new Node(Token.TYPEOF, Node.newNumber(123));
    res = fold(typeofNum);
    assertEquals("number", res.getString());

    Node typeofTrue = new Node(Token.TYPEOF, new Node(Token.TRUE));
    res = fold(typeofTrue);
    assertEquals("boolean", res.getString());

    Node typeofFalse = new Node(Token.TYPEOF, new Node(Token.FALSE));
    res = fold(typeofFalse);
    assertEquals("boolean", res.getString());

    Node typeofNull = new Node(Token.TYPEOF, new Node(Token.NULL));
    res = fold(typeofNull);
    assertEquals("object", res.getString());

    Node typeofObj = new Node(Token.TYPEOF, new Node(Token.OBJECTLIT));
    res = fold(typeofObj);
    assertEquals("object", res.getString());

    Node typeofArr = new Node(Token.TYPEOF, new Node(Token.ARRAYLIT));
    res = fold(typeofArr);
    assertEquals("object", res.getString());

    Node typeofVoid = new Node(Token.TYPEOF, new Node(Token.VOID, Node.newNumber(0)));
    res = fold(typeofVoid);
    assertEquals("undefined", res.getString());

    Node typeofUndefName = new Node(Token.TYPEOF, Node.newString(Token.NAME, "undefined"));
    res = fold(typeofUndefName);
    assertEquals("undefined", res.getString());

    Node fnNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node typeofFn = new Node(Token.TYPEOF, fnNode);
    res = fold(typeofFn);
    assertEquals("function", res.getString());

    Node typeofVar = new Node(Token.TYPEOF, Node.newString(Token.NAME, "variable"));
    res = fold(typeofVar);
    assertEquals(Token.TYPEOF, res.getType());
  }

  @Test
  public void testTryFoldUnaryNot() {
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Node res = fold(notTrue);
    assertEquals(Token.FALSE, res.getType());

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    res = fold(notFalse);
    assertEquals(Token.TRUE, res.getType());

    Node not0 = new Node(Token.NOT, Node.newNumber(0));
    res = fold(not0);
    assertEquals(Token.NOT, res.getType());

    Node not1 = new Node(Token.NOT, Node.newNumber(1));
    res = fold(not1);
    assertEquals(Token.NOT, res.getType());

    Node not2 = new Node(Token.NOT, Node.newNumber(2));
    res = fold(not2);
    assertEquals(Token.FALSE, res.getType());

    Node notStr = new Node(Token.NOT, Node.newString("hello"));
    res = fold(notStr);
    assertEquals(Token.FALSE, res.getType());

    Node notEmptyStr = new Node(Token.NOT, Node.newString(""));
    res = fold(notEmptyStr);
    assertEquals(Token.TRUE, res.getType());

    Node notVar = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    res = fold(notVar);
    assertEquals(Token.NOT, res.getType());
  }

  @Test
  public void testTryFoldUnaryPos() {
    Node posNum = new Node(Token.POS, Node.newNumber(42));
    Node res = fold(posNum);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(42.0, res.getDouble(), 0.0);

    Node posStr = new Node(Token.POS, Node.newString("42"));
    res = fold(posStr);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(42.0, res.getDouble(), 0.0);

    Node posVar = new Node(Token.POS, Node.newString(Token.NAME, "foo"));
    res = fold(posVar);
    assertEquals(Token.POS, res.getType());
  }

  @Test
  public void testTryFoldUnaryNeg() {
    Node negNum = new Node(Token.NEG, Node.newNumber(42));
    Node res = fold(negNum);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(-42.0, res.getDouble(), 0.0);

    Node negNegNum = new Node(Token.NEG, Node.newNumber(-42));
    res = fold(negNegNum);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(42.0, res.getDouble(), 0.0);

    Node negInfinity = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    res = fold(negInfinity);
    assertEquals(Token.NEG, res.getType());
    assertEquals("Infinity", res.getFirstChild().getString());

    Node negNaN = new Node(Token.NEG, Node.newString(Token.NAME, "NaN"));
    res = fold(negNaN);
    assertEquals(Token.NAME, res.getType());
    assertEquals("NaN", res.getString());

    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "foo"));
    res = fold(negVar);
    assertEquals(Token.NEG, res.getType());
  }

  @Test
  public void testTryFoldUnaryBitNot() {
    Node bitnot0 = new Node(Token.BITNOT, Node.newNumber(0));
    Node res = fold(bitnot0);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(-1.0, res.getDouble(), 0.0);

    Node bitnot1 = new Node(Token.BITNOT, Node.newNumber(1));
    res = fold(bitnot1);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(-2.0, res.getDouble(), 0.0);

    Node bitnotFrac = new Node(Token.BITNOT, Node.newNumber(1.5));
    res = fold(bitnotFrac);
    assertEquals(Token.BITNOT, res.getType());

    Node bitnotLarge = new Node(Token.BITNOT, Node.newNumber(1e12));
    res = fold(bitnotLarge);
    assertEquals(Token.BITNOT, res.getType());

    Node bitnotVar = new Node(Token.BITNOT, Node.newString(Token.NAME, "foo"));
    res = fold(bitnotVar);
    assertEquals(Token.BITNOT, res.getType());
  }

  @Test
  public void testTryFoldInstanceof() {
    Node inst1 = new Node(Token.INSTANCEOF, Node.newString("hello"), Node.newString(Token.NAME, "Object"));
    Node res = fold(inst1);
    assertEquals(Token.FALSE, res.getType());

    Node inst2 = new Node(Token.INSTANCEOF, Node.newNumber(123), Node.newString(Token.NAME, "Object"));
    res = fold(inst2);
    assertEquals(Token.FALSE, res.getType());

    Node inst3 = new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "Object"));
    res = fold(inst3);
    assertEquals(Token.TRUE, res.getType());

    Node inst4 = new Node(Token.INSTANCEOF, new Node(Token.OBJECTLIT), Node.newString(Token.NAME, "String"));
    res = fold(inst4);
    assertEquals(Token.INSTANCEOF, res.getType());

    Node inst5 = new Node(Token.INSTANCEOF, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "Object"));
    res = fold(inst5);
    assertEquals(Token.INSTANCEOF, res.getType());
  }

  @Test
  public void testTryFoldAssign() {
    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), add);
    Node res = fold(assign);
    assertEquals(Token.ASSIGN_ADD, res.getType());
    assertEquals("x", res.getFirstChild().getString());
    assertEquals(1.0, res.getLastChild().getDouble(), 0.0);

    Node mulComm = new Node(Token.MUL, Node.newNumber(2), Node.newString(Token.NAME, "y"));
    Node assignMul = new Node(Token.ASSIGN, Node.newString(Token.NAME, "y"), mulComm);
    res = fold(assignMul);
    assertEquals(Token.ASSIGN_MUL, res.getType());
    assertEquals("y", res.getFirstChild().getString());
    assertEquals(2.0, res.getLastChild().getDouble(), 0.0);

    Node sub = new Node(Token.SUB, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignSub = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), sub);
    res = fold(assignSub);
    assertEquals(Token.ASSIGN_SUB, res.getType());

    Node div = new Node(Token.DIV, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignDiv = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), div);
    res = fold(assignDiv);
    assertEquals(Token.ASSIGN_DIV, res.getType());

    Node mod = new Node(Token.MOD, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignMod = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), mod);
    res = fold(assignMod);
    assertEquals(Token.ASSIGN_MOD, res.getType());

    Node bitand = new Node(Token.BITAND, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignBitand = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), bitand);
    res = fold(assignBitand);
    assertEquals(Token.ASSIGN_BITAND, res.getType());

    Node bitor = new Node(Token.BITOR, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignBitor = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), bitor);
    res = fold(assignBitor);
    assertEquals(Token.ASSIGN_BITOR, res.getType());

    Node bitxor = new Node(Token.BITXOR, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignBitxor = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), bitxor);
    res = fold(assignBitxor);
    assertEquals(Token.ASSIGN_BITXOR, res.getType());

    Node lsh = new Node(Token.LSH, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignLsh = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), lsh);
    res = fold(assignLsh);
    assertEquals(Token.ASSIGN_LSH, res.getType());

    Node rsh = new Node(Token.RSH, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignRsh = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), rsh);
    res = fold(assignRsh);
    assertEquals(Token.ASSIGN_RSH, res.getType());

    Node ursh = new Node(Token.URSH, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    Node assignUrsh = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), ursh);
    res = fold(assignUrsh);
    assertEquals(Token.ASSIGN_URSH, res.getType());
  }

  @Test
  public void testTryFoldAndOr() {
    Node and1 = new Node(Token.AND, new Node(Token.TRUE), Node.newString(Token.NAME, "x"));
    Node res = fold(and1);
    assertEquals(Token.NAME, res.getType());
    assertEquals("x", res.getString());

    Node and2 = new Node(Token.AND, new Node(Token.FALSE), Node.newString(Token.NAME, "x"));
    res = fold(and2);
    assertEquals(Token.FALSE, res.getType());

    Node or1 = new Node(Token.OR, new Node(Token.TRUE), Node.newString(Token.NAME, "x"));
    res = fold(or1);
    assertEquals(Token.TRUE, res.getType());

    Node or2 = new Node(Token.OR, new Node(Token.FALSE), Node.newString(Token.NAME, "x"));
    res = fold(or2);
    assertEquals(Token.NAME, res.getType());
    assertEquals("x", res.getString());

    Node or3 = new Node(Token.OR, Node.newNumber(3), Node.newString(Token.NAME, "x"));
    res = fold(or3);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(3.0, res.getDouble(), 0.0);

    Node and3 = new Node(Token.AND, Node.newNumber(0), Node.newString(Token.NAME, "x"));
    res = fold(and3);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(0.0, res.getDouble(), 0.0);
  }

  @Test
  public void testTryFoldArithmeticOps() {
    Node addNum = new Node(Token.ADD, Node.newNumber(10), Node.newNumber(20));
    Node res = fold(addNum);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(30.0, res.getDouble(), 0.0);

    Node addStr = new Node(Token.ADD, Node.newString("foo"), Node.newString("bar"));
    res = fold(addStr);
    assertEquals(Token.STRING, res.getType());
    assertEquals("foobar", res.getString());

    Node addStrNum = new Node(Token.ADD, Node.newString("a"), Node.newNumber(1));
    res = fold(addStrNum);
    assertEquals(Token.STRING, res.getType());
    assertEquals("a1", res.getString());

    Node sub = new Node(Token.SUB, Node.newNumber(50), Node.newNumber(20));
    res = fold(sub);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(30.0, res.getDouble(), 0.0);

    Node mul = new Node(Token.MUL, Node.newNumber(5), Node.newNumber(6));
    res = fold(mul);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(30.0, res.getDouble(), 0.0);

    Node div = new Node(Token.DIV, Node.newNumber(100), Node.newNumber(4));
    res = fold(div);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(25.0, res.getDouble(), 0.0);

    Node divZero = new Node(Token.DIV, Node.newNumber(100), Node.newNumber(0));
    res = fold(divZero);
    assertEquals(Token.DIV, res.getType());

    Node mod = new Node(Token.MOD, Node.newNumber(10), Node.newNumber(3));
    res = fold(mod);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(1.0, res.getDouble(), 0.0);

    Node modZero = new Node(Token.MOD, Node.newNumber(10), Node.newNumber(0));
    res = fold(modZero);
    assertEquals(Token.MOD, res.getType());

    Node bitand = new Node(Token.BITAND, Node.newNumber(7), Node.newNumber(3));
    res = fold(bitand);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(3.0, res.getDouble(), 0.0);

    Node bitor = new Node(Token.BITOR, Node.newNumber(4), Node.newNumber(2));
    res = fold(bitor);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(6.0, res.getDouble(), 0.0);

    Node bitxor = new Node(Token.BITXOR, Node.newNumber(7), Node.newNumber(3));
    res = fold(bitxor);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(4.0, res.getDouble(), 0.0);
  }

  @Test
  public void testTryFoldLeftChildOp() {
    Node leftMul = new Node(Token.MUL, Node.newString(Token.NAME, "x"), Node.newNumber(10));
    Node outerMul = new Node(Token.MUL, leftMul, Node.newNumber(20));
    Node res = fold(outerMul);
    assertEquals(Token.MUL, res.getType());
    assertEquals("x", res.getFirstChild().getString());
    assertEquals(200.0, res.getLastChild().getDouble(), 0.0);

    Node leftAdd = new Node(Token.ADD, Node.newString(Token.NAME, "x"), Node.newString("a"));
    Node outerAdd = new Node(Token.ADD, leftAdd, Node.newString("b"));
    res = fold(outerAdd);
    assertEquals(Token.ADD, res.getType());
    assertEquals("x", res.getFirstChild().getString());
    assertEquals("ab", res.getLastChild().getString());

    Node rightAdd = new Node(Token.ADD, Node.newString("b"), Node.newString(Token.NAME, "x"));
    Node outerAdd2 = new Node(Token.ADD, Node.newString("a"), rightAdd);
    res = fold(outerAdd2);
    assertEquals(Token.ADD, res.getType());
    assertEquals("ab", res.getFirstChild().getString());
    assertEquals("x", res.getLastChild().getString());
  }

  @Test
  public void testTryFoldShift() {
    Node lsh = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(3));
    Node res = fold(lsh);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(8.0, res.getDouble(), 0.0);

    Node rsh = new Node(Token.RSH, Node.newNumber(16), Node.newNumber(2));
    res = fold(rsh);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(4.0, res.getDouble(), 0.0);

    Node ursh = new Node(Token.URSH, Node.newNumber(-1), Node.newNumber(0));
    res = fold(ursh);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(4294967295.0, res.getDouble(), 0.0);

    Node shiftOutOfBounds = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(35));
    res = fold(shiftOutOfBounds);
    assertEquals(Token.LSH, res.getType());

    Node bitwiseOutOfRange = new Node(Token.LSH, Node.newNumber(1e12), Node.newNumber(1));
    res = fold(bitwiseOutOfRange);
    assertEquals(Token.LSH, res.getType());

    Node fracLeft = new Node(Token.LSH, Node.newNumber(1.5), Node.newNumber(1));
    res = fold(fracLeft);
    assertEquals(Token.LSH, res.getType());

    Node fracRight = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(1.5));
    res = fold(fracRight);
    assertEquals(Token.LSH, res.getType());
  }

  @Test
  public void testTryFoldComparison() {
    Node eqNum = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(1));
    assertEquals(Token.TRUE, fold(eqNum).getType());

    Node eqNumDiff = new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2));
    assertEquals(Token.FALSE, fold(eqNumDiff).getType());

    Node neNum = new Node(Token.NE, Node.newNumber(1), Node.newNumber(2));
    assertEquals(Token.TRUE, fold(neNum).getType());

    Node ltNum = new Node(Token.LT, Node.newNumber(1), Node.newNumber(2));
    assertEquals(Token.TRUE, fold(ltNum).getType());

    Node gtNum = new Node(Token.GT, Node.newNumber(1), Node.newNumber(2));
    assertEquals(Token.FALSE, fold(gtNum).getType());

    Node leNum = new Node(Token.LE, Node.newNumber(2), Node.newNumber(2));
    assertEquals(Token.TRUE, fold(leNum).getType());

    Node geNum = new Node(Token.GE, Node.newNumber(2), Node.newNumber(3));
    assertEquals(Token.FALSE, fold(geNum).getType());

    Node eqStr = new Node(Token.SHEQ, Node.newString("a"), Node.newString("a"));
    assertEquals(Token.TRUE, fold(eqStr).getType());

    Node neStr = new Node(Token.SHNE, Node.newString("a"), Node.newString("b"));
    assertEquals(Token.TRUE, fold(neStr).getType());

    Node eqNullUndef = new Node(Token.EQ, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    assertEquals(Token.TRUE, fold(eqNullUndef).getType());

    Node sheqNullUndef = new Node(Token.SHEQ, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    assertEquals(Token.FALSE, fold(sheqNullUndef).getType());

    Node void0 = new Node(Token.VOID, Node.newNumber(0));
    Node eqVoid = new Node(Token.EQ, void0, new Node(Token.NULL));
    assertEquals(Token.TRUE, fold(eqVoid).getType());

    Node thisEqThis = new Node(Token.EQ, new Node(Token.THIS), new Node(Token.THIS));
    assertEquals(Token.TRUE, fold(thisEqThis).getType());

    Node thisNeThis = new Node(Token.NE, new Node(Token.THIS), new Node(Token.THIS));
    assertEquals(Token.FALSE, fold(thisNeThis).getType());
  }

  @Test
  public void testTryFoldCtorCall() {
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "obj"));
    Node newString = new Node(Token.NEW, Node.newString(Token.NAME, "String"), Node.newString("eval"));
    getElem.addChildToBack(newString);
    Node parent = new Node(Token.EXPR_RESULT, getElem);

    Node res = folder.optimizeSubtree(newString);
    assertEquals(Token.STRING, res.getType());
    assertEquals("eval", res.getString());
  }

  @Test
  public void testTryFoldStringMethods() {
    Node toLowerTarget = new Node(Token.GETPROP, Node.newString("HELLO"), Node.newString("toLowerCase"));
    Node toLowerCall = new Node(Token.CALL, toLowerTarget);
    Node res = fold(toLowerCall);
    assertEquals(Token.STRING, res.getType());
    assertEquals("hello", res.getString());

    Node toUpperTarget = new Node(Token.GETPROP, Node.newString("world"), Node.newString("toUpperCase"));
    Node toUpperCall = new Node(Token.CALL, toUpperTarget);
    res = fold(toUpperCall);
    assertEquals(Token.STRING, res.getType());
    assertEquals("WORLD", res.getString());

    Node indexOfTarget = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("indexOf"));
    Node indexOfCall = new Node(Token.CALL, indexOfTarget, Node.newString("cd"));
    res = fold(indexOfCall);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(2.0, res.getDouble(), 0.0);

    Node lastIndexOfTarget = new Node(Token.GETPROP, Node.newString("abcbcd"), Node.newString("lastIndexOf"));
    Node lastIndexOfCall = new Node(Token.CALL, lastIndexOfTarget, Node.newString("bc"));
    res = fold(lastIndexOfCall);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(3.0, res.getDouble(), 0.0);

    Node substrTarget = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("substr"));
    Node substrCall = new Node(Token.CALL, substrTarget, Node.newNumber(2), Node.newNumber(3));
    res = fold(substrCall);
    assertEquals(Token.STRING, res.getType());
    assertEquals("cde", res.getString());

    Node substringTarget = new Node(Token.GETPROP, Node.newString("abcdef"), Node.newString("substring"));
    Node substringCall = new Node(Token.CALL, substringTarget, Node.newNumber(1), Node.newNumber(4));
    res = fold(substringCall);
    assertEquals(Token.STRING, res.getType());
    assertEquals("bcd", res.getString());
  }

  @Test
  public void testTryFoldArrayJoin() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"), Node.newString("c"));
    Node joinTarget = new Node(Token.GETPROP, arrayLit, Node.newString("join"));
    Node joinCall = new Node(Token.CALL, joinTarget, Node.newString(","));
    Node res = fold(joinCall);
    assertEquals(Token.STRING, res.getType());
    assertEquals("a,b,c", res.getString());

    Node emptyArray = new Node(Token.ARRAYLIT);
    Node emptyJoinTarget = new Node(Token.GETPROP, emptyArray, Node.newString("join"));
    Node emptyJoinCall = new Node(Token.CALL, emptyJoinTarget, Node.newString(","));
    res = fold(emptyJoinCall);
    assertEquals(Token.STRING, res.getType());
    assertEquals("", res.getString());
  }

  @Test
  public void testTryFoldGetElemAndGetProp() {
    Node array = new Node(Token.ARRAYLIT, Node.newString("first"), Node.newString("second"));
    Node getElem = new Node(Token.GETELEM, array, Node.newNumber(0));
    Node res = fold(getElem);
    assertEquals(Token.STRING, res.getType());
    assertEquals("first", res.getString());

    Node arrayLength = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2), Node.newNumber(3));
    Node getPropLength = new Node(Token.GETPROP, arrayLength, Node.newString("length"));
    res = fold(getPropLength);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(3.0, res.getDouble(), 0.0);

    Node strLength = new Node(Token.GETPROP, Node.newString("hello"), Node.newString("length"));
    res = fold(strLength);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(5.0, res.getDouble(), 0.0);

    Node objKey = Node.newString(Token.STRING, "foo");
    objKey.addChildToBack(Node.newNumber(123));
    Node objLit = new Node(Token.OBJECTLIT, objKey);
    Node getPropObj = new Node(Token.GETPROP, objLit, Node.newString("foo"));
    res = fold(getPropObj);
    assertEquals(Token.NUMBER, res.getType());
    assertEquals(123.0, res.getDouble(), 0.0);
  }
}