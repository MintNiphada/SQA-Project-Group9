package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeUtilTest {

  private final CodingConvention convention = new DefaultCodingConvention();

  @Test
  public void testGetImpureBooleanValue() {
    Node trueNode = new Node(Token.TRUE);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(trueNode));

    Node falseNode = new Node(Token.FALSE);
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(falseNode));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    Node comma = new Node(Token.COMMA, new Node(Token.FALSE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(comma));

    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(notNode));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hook1 = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hook1));

    Node hook2 = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hook2));

    Node arrayLit = new Node(Token.ARRAYLIT);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(arrayLit));

    Node objLit = new Node(Token.OBJECTLIT);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(objLit));
  }

  @Test
  public void testGetPureBooleanValue() {
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString("hello")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString("")));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newNumber(1.0)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(0.0)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.NULL)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.FALSE)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.VOID, Node.newNumber(0))));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.TRUE)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.REGEXP)));

    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "other")));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(not));
  }

  @Test
  public void testGetStringValue() {
    Assert.assertEquals("test", NodeUtil.getStringValue(Node.newString("test")));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "foo")));

    Assert.assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123)));
    Assert.assertEquals("123.45", NodeUtil.getStringValue(Node.newNumber(123.45)));
    Assert.assertEquals("123", NodeUtil.getStringValue(123.0));
    Assert.assertEquals("123.5", NodeUtil.getStringValue(123.5));

    Assert.assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    Assert.assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    Assert.assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals("false", NodeUtil.getStringValue(notTrue));
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals("true", NodeUtil.getStringValue(notFalse));

    Node arr = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
    Assert.assertEquals("a,b", NodeUtil.getStringValue(arr));

    Node arrWithNull = new Node(Token.ARRAYLIT, new Node(Token.NULL), Node.newString("b"));
    Assert.assertEquals(",b", NodeUtil.getStringValue(arrWithNull));

    Node emptyArr = new Node(Token.ARRAYLIT);
    Assert.assertEquals("", NodeUtil.getStringValue(emptyArr));

    Node objLit = new Node(Token.OBJECTLIT);
    Assert.assertEquals("[object Object]", NodeUtil.getStringValue(objLit));
  }

  @Test
  public void testGetNumberValue() {
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    Assert.assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(Node.newNumber(42.5)));

    Node voidPure = new Node(Token.VOID, Node.newNumber(0));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidPure)));

    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    Assert.assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "foo")));

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    Assert.assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));

    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notNode));
    Node notFalseNode = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notFalseNode));

    Assert.assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(Node.newString("123")));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testGetStringNumberValue() {
    Assert.assertNull(NodeUtil.getStringNumberValue("12\u000b34"));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    Assert.assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    Assert.assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0XFF"));
    Assert.assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));
    Assert.assertNull(NodeUtil.getStringNumberValue("-0x10"));
    Assert.assertNull(NodeUtil.getStringNumberValue("+0x10"));
    Assert.assertNull(NodeUtil.getStringNumberValue("infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("-infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("+infinity"));
    Assert.assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue(" 123.45 "));
    Assert.assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("abc")));
  }

  @Test
  public void testIsStrWhiteSpaceChar() {
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\n'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\r'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\t'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u00A0'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u000C'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2028'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2029'));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\uFEFF'));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('a'));
  }

  @Test
  public void testGetFunctionName() {
    // function foo() {}
    Node fn = NodeUtil.newFunctionNode("foo", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Assert.assertEquals("foo", NodeUtil.getFunctionName(fn));

    // var bar = function() {}
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "bar");
    Node anonFn = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    nameNode.addChildToBack(anonFn);
    varNode.addChildToBack(nameNode);
    Assert.assertEquals("bar", NodeUtil.getFunctionName(anonFn));

    // a.b = function() {}
    Node assignNode = new Node(Token.ASSIGN);
    Node qName = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    Node anonFn2 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    assignNode.addChildToBack(qName);
    assignNode.addChildToBack(anonFn2);
    Assert.assertEquals("a.b", NodeUtil.getFunctionName(anonFn2));

    // Nearest function name for object literal key { prop: function() {} }
    Node strKey = Node.newString(Token.STRING, "propKey");
    Node anonFn3 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    strKey.addChildToBack(anonFn3);
    Node objLit = new Node(Token.OBJECTLIT, strKey);
    Assert.assertEquals("propKey", NodeUtil.getNearestFunctionName(anonFn3));

    Node numKey = Node.newNumber(42);
    Node anonFn4 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    numKey.addChildToBack(anonFn4);
    Node objLit2 = new Node(Token.OBJECTLIT, numKey);
    Assert.assertEquals("42", NodeUtil.getNearestFunctionName(anonFn4));
  }

  @Test
  public void testIsImmutableValue() {
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1.0)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, new Node(Token.TRUE))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "customVar")));
  }

  @Test
  public void testIsLiteralValue() {
    Assert.assertTrue(NodeUtil.isLiteralValue(Node.newString("str"), false));
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    Assert.assertTrue(NodeUtil.isLiteralValue(arr, false));

    Node obj = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    key.addChildToBack(Node.newNumber(10));
    obj.addChildToBack(key);
    Assert.assertTrue(NodeUtil.isLiteralValue(obj, false));

    Node regexp = new Node(Token.REGEXP, Node.newString("abc"));
    Assert.assertTrue(NodeUtil.isLiteralValue(regexp, false));

    Node fnExpr = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Assert.assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    Assert.assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<String>(Arrays.asList("DEF1", "a.b.DEF2"));

    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString("hello"), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(10), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertTrue(NodeUtil.isValidDefineValue(not, defines));

    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF1"), defines));
    Assert.assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN_DEF"), defines));

    Node qname = new Node(Token.GETPROP, new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b")), Node.newString(Token.STRING, "DEF2"));
    Assert.assertTrue(NodeUtil.isValidDefineValue(qname, defines));
  }

  @Test
  public void testIsEmptyBlock() {
    Node block = new Node(Token.BLOCK);
    Assert.assertTrue(NodeUtil.isEmptyBlock(block));

    block.addChildToBack(new Node(Token.EMPTY));
    Assert.assertTrue(NodeUtil.isEmptyBlock(block));

    block.addChildToBack(Node.newNumber(1));
    Assert.assertFalse(NodeUtil.isEmptyBlock(block));

    Assert.assertFalse(NodeUtil.isEmptyBlock(Node.newNumber(1)));
  }

  @Test
  public void testIsSimpleOperator() {
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.SUB)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.MUL)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.DIV)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.MOD)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.EQ)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.NE)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.SHEQ)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.SHNE)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.LT)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.LE)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GT)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GE)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.NOT)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.BITNOT)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.BITAND)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.BITOR)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.BITXOR)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.LSH)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.RSH)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.URSH)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.TYPEOF)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.INSTANCEOF)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETPROP)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETELEM)));
    Assert.assertFalse(NodeUtil.isSimpleOperator(new Node(Token.CALL)));
  }

  @Test
  public void testNewExpr() {
    Node child = Node.newNumber(1);
    Node expr = NodeUtil.newExpr(child);
    Assert.assertEquals(Token.EXPR_RESULT, expr.getType());
    Assert.assertEquals(child, expr.getFirstChild());
  }

  @Test
  public void testMayHaveSideEffectsAndMayEffectMutableState() {
    Node num = Node.newNumber(1);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(num));
    Assert.assertFalse(NodeUtil.mayEffectMutableState(num));

    Node throwNode = new Node(Token.THROW, Node.newString("err"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(throwNode));

    Node objLit = new Node(Token.OBJECTLIT);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(objLit));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(objLit));

    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(call));

    Node newCall = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    Assert.assertFalse(NodeUtil.constructorCallHasSideEffects(newCall));

    Node customNew = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    Assert.assertTrue(NodeUtil.constructorCallHasSideEffects(customNew));

    Node mathCall = new Node(Token.CALL, Node.newString(Token.NAME, "Math"));
    Assert.assertTrue(NodeUtil.functionCallHasSideEffects(mathCall));

    Node builtinCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    Assert.assertFalse(NodeUtil.functionCallHasSideEffects(builtinCall));
  }

  @Test
  public void testPrecedence() {
    Assert.assertEquals(0, NodeUtil.precedence(Token.COMMA));
    Assert.assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    Assert.assertEquals(2, NodeUtil.precedence(Token.HOOK));
    Assert.assertEquals(3, NodeUtil.precedence(Token.OR));
    Assert.assertEquals(4, NodeUtil.precedence(Token.AND));
    Assert.assertEquals(5, NodeUtil.precedence(Token.BITOR));
    Assert.assertEquals(6, NodeUtil.precedence(Token.BITXOR));
    Assert.assertEquals(7, NodeUtil.precedence(Token.BITAND));
    Assert.assertEquals(8, NodeUtil.precedence(Token.EQ));
    Assert.assertEquals(9, NodeUtil.precedence(Token.LT));
    Assert.assertEquals(10, NodeUtil.precedence(Token.LSH));
    Assert.assertEquals(11, NodeUtil.precedence(Token.ADD));
    Assert.assertEquals(12, NodeUtil.precedence(Token.MUL));
    Assert.assertEquals(13, NodeUtil.precedence(Token.NOT));
    Assert.assertEquals(15, NodeUtil.precedence(Token.CALL));
    Assert.assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  @Test
  public void testNumericResult() {
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newNumber(1.0)));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "NaN")));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "Infinity")));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.SUB, Node.newNumber(2), Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.MUL, Node.newNumber(2), Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertFalse(NodeUtil.isNumericResult(Node.newString("str")));
  }

  @Test
  public void testBooleanResult() {
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.SHEQ, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.DELPROP, Node.newString(Token.NAME, "x"))));
    Assert.assertFalse(NodeUtil.isBooleanResult(Node.newNumber(1)));
  }

  @Test
  public void testNullAndUndefined() {
    Node nullNode = new Node(Token.NULL);
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    Node undefName = Node.newString(Token.NAME, "undefined");
    Node regular = Node.newString(Token.NAME, "foo");

    Assert.assertTrue(NodeUtil.isNull(nullNode));
    Assert.assertFalse(NodeUtil.isNull(voidNode));
    Assert.assertTrue(NodeUtil.isUndefined(voidNode));
    Assert.assertTrue(NodeUtil.isUndefined(undefName));
    Assert.assertFalse(NodeUtil.isUndefined(regular));
    Assert.assertTrue(NodeUtil.isNullOrUndefined(nullNode));
    Assert.assertTrue(NodeUtil.isNullOrUndefined(voidNode));
    Assert.assertFalse(NodeUtil.isNullOrUndefined(regular));
  }

  @Test
  public void testMayBeString() {
    Assert.assertTrue(NodeUtil.mayBeString(Node.newString("str")));
    Assert.assertTrue(NodeUtil.mayBeString(Node.newString(Token.NAME, "unknownVar")));
    Assert.assertFalse(NodeUtil.mayBeString(Node.newNumber(1)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.TRUE)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.NULL)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.VOID, Node.newNumber(0))));
  }

  @Test
  public void testIsAssociativeAndCommutative() {
    Assert.assertTrue(NodeUtil.isAssociative(Token.MUL));
    Assert.assertTrue(NodeUtil.isAssociative(Token.AND));
    Assert.assertTrue(NodeUtil.isAssociative(Token.OR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITOR));
    Assert.assertFalse(NodeUtil.isAssociative(Token.ADD));
    Assert.assertFalse(NodeUtil.isAssociative(Token.SUB));

    Assert.assertTrue(NodeUtil.isCommutative(Token.MUL));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITOR));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITXOR));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITAND));
    Assert.assertFalse(NodeUtil.isCommutative(Token.ADD));
    Assert.assertFalse(NodeUtil.isCommutative(Token.SUB));
  }

  @Test
  public void testAssignmentOps() {
    Node assignAdd = new Node(Token.ASSIGN_ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    Assert.assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));

    Node assignSub = new Node(Token.ASSIGN_SUB, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(assignSub));

    Node assignMul = new Node(Token.ASSIGN_MUL, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(assignMul));

    Node assignDiv = new Node(Token.ASSIGN_DIV, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(assignDiv));

    Node assignMod = new Node(Token.ASSIGN_MOD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(assignMod));

    Node assignBitOr = new Node(Token.ASSIGN_BITOR, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(assignBitOr));

    Node assignBitXor = new Node(Token.ASSIGN_BITXOR, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(assignBitXor));

    Node assignBitAnd = new Node(Token.ASSIGN_BITAND, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(assignBitAnd));

    Node assignLsh = new Node(Token.ASSIGN_LSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(assignLsh));

    Node assignRsh = new Node(Token.ASSIGN_RSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(assignRsh));

    Node assignUrsh = new Node(Token.ASSIGN_URSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(assignUrsh));
  }

  @Test
  public void testStructurePredicates() {
    Node expr = NodeUtil.newExpr(new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isExprAssign(expr));
    Assert.assertTrue(NodeUtil.isExpressionNode(expr));

    Node callExpr = NodeUtil.newExpr(new Node(Token.CALL, Node.newString(Token.NAME, "foo")));
    Assert.assertTrue(NodeUtil.isExprCall(callExpr));

    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isForIn(forIn));

    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    Assert.assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));

    Node forNode = new Node(Token.FOR, new Node(Token.VAR), Node.newNumber(1), Node.newNumber(2), new Node(Token.BLOCK));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(forNode).getType());

    Node whileNode = new Node(Token.WHILE, Node.newNumber(1), new Node(Token.BLOCK));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(whileNode).getType());

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newNumber(1));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(doNode).getType());
  }

  @Test
  public void testIsControlStructureAndCodeBlock() {
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.DO)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.WITH)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));

    Node body = new Node(Token.BLOCK);
    Node cond = Node.newNumber(1);
    Node ifNode = new Node(Token.IF, cond, body);
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, body));
    Assert.assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, cond));
  }

  @Test
  public void testGetConditionExpression() {
    Node cond = Node.newNumber(1);
    Node body = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, cond, body);
    Assert.assertEquals(cond, NodeUtil.getConditionExpression(ifNode));

    Node whileNode = new Node(Token.WHILE, cond, body);
    Assert.assertEquals(cond, NodeUtil.getConditionExpression(whileNode));

    Node doNode = new Node(Token.DO, body, cond);
    Assert.assertEquals(cond, NodeUtil.getConditionExpression(doNode));

    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), cond, new Node(Token.EMPTY), body);
    Assert.assertEquals(cond, NodeUtil.getConditionExpression(forNode));
  }

  @Test
  public void testIsFunctionClassification() {
    Node script = new Node(Token.SCRIPT);
    Node fnDecl = NodeUtil.newFunctionNode("decl", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    script.addChildToBack(fnDecl);

    Assert.assertTrue(NodeUtil.isFunction(fnDecl));
    Assert.assertTrue(NodeUtil.isFunctionDeclaration(fnDecl));
    Assert.assertTrue(NodeUtil.isHoistedFunctionDeclaration(fnDecl));
    Assert.assertFalse(NodeUtil.isFunctionExpression(fnDecl));

    Node expr = NodeUtil.newExpr(NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0));
    Node fnExpr = expr.getFirstChild();
    Assert.assertTrue(NodeUtil.isFunctionExpression(fnExpr));
    Assert.assertTrue(NodeUtil.isEmptyFunctionExpression(fnExpr));
  }

  @Test
  public void testFunctionObjectCalls() {
    Node callTarget1 = new Node(Token.GETPROP, Node.newString(Token.NAME, "foo"), Node.newString(Token.STRING, "call"));
    Node call1 = new Node(Token.CALL, callTarget1);
    Assert.assertTrue(NodeUtil.isFunctionObjectCall(call1));
    Assert.assertTrue(NodeUtil.isSimpleFunctionObjectCall(call1));

    Node callTarget2 = new Node(Token.GETPROP, Node.newString(Token.NAME, "foo"), Node.newString(Token.STRING, "apply"));
    Node call2 = new Node(Token.CALL, callTarget2);
    Assert.assertTrue(NodeUtil.isFunctionObjectApply(call2));
    Assert.assertTrue(NodeUtil.isFunctionObjectCallOrApply(call2));
  }

  @Test
  public void testOpToStr() {
    Assert.assertEquals("+", NodeUtil.opToStr(Token.ADD));
    Assert.assertEquals("-", NodeUtil.opToStr(Token.SUB));
    Assert.assertEquals("*", NodeUtil.opToStr(Token.MUL));
    Assert.assertEquals("/", NodeUtil.opToStr(Token.DIV));
    Assert.assertEquals("==", NodeUtil.opToStr(Token.EQ));
    Assert.assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    Assert.assertEquals("!=", NodeUtil.opToStr(Token.NE));
    Assert.assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    Assert.assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    Assert.assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    Assert.assertEquals("void", NodeUtil.opToStr(Token.VOID));
    Assert.assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(expected = Error.class)
  public void testOpToStrNoFailException() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  @Test
  public void testIsLatinAndValidPropertyName() {
    Assert.assertTrue(NodeUtil.isLatin("abcXYZ123_"));
    Assert.assertFalse(NodeUtil.isLatin("abc\u1234"));

    Assert.assertTrue(NodeUtil.isValidPropertyName("validProp"));
    Assert.assertTrue(NodeUtil.isValidPropertyName("$prop_1"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("for")); // keyword
    Assert.assertFalse(NodeUtil.isValidPropertyName("123abc")); // invalid ident
    Assert.assertFalse(NodeUtil.isValidPropertyName("prop\u03A0")); // non-latin
  }

  @Test
  public void testQualifiedNameAndPrototypeHelpers() {
    Node qname = NodeUtil.newQualifiedNameNode(convention, "a.b.c", 1, 0);
    Assert.assertTrue(qname.isGetProp());
    Assert.assertEquals("a.b.c", qname.getQualifiedName());

    Node root = NodeUtil.getRootOfQualifiedName(qname);
    Assert.assertTrue(root.isName());
    Assert.assertEquals("a", root.getString());

    Node protoQname = NodeUtil.newQualifiedNameNode(convention, "MyClass.prototype.myMethod", 1, 0);
    Assert.assertTrue(NodeUtil.isPrototypeProperty(protoQname));
    Assert.assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoQname).getQualifiedName());
    Assert.assertEquals("myMethod", NodeUtil.getPrototypePropertyName(protoQname));

    Node exprAssign = NodeUtil.newExpr(new Node(Token.ASSIGN, protoQname, Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));
  }

  @Test
  public void testNewVarAndUndefinedNodes() {
    Node undef = NodeUtil.newUndefinedNode(null);
    Assert.assertEquals(Token.VOID, undef.getType());

    Node varNode = NodeUtil.newVarNode("x", Node.newNumber(42));
    Assert.assertEquals(Token.VAR, varNode.getType());
    Assert.assertEquals("x", varNode.getFirstChild().getString());
    Assert.assertEquals(42.0, varNode.getFirstChild().getFirstChild().getDouble(), 0.0);
  }

  @Test
  public void testTraversalAndReferenceCounting() {
    Node root = new Node(Token.BLOCK);
    Node name1 = Node.newString(Token.NAME, "target");
    Node name2 = Node.newString(Token.NAME, "target");
    Node name3 = Node.newString(Token.NAME, "other");
    root.addChildToBack(name1);
    root.addChildToBack(name2);
    root.addChildToBack(name3);

    Assert.assertTrue(NodeUtil.isNameReferenced(root, "target"));
    Assert.assertFalse(NodeUtil.isNameReferenced(root, "nonexistent"));
    Assert.assertEquals(2, NodeUtil.getNameReferenceCount(root, "target"));
    Assert.assertEquals(3, NodeUtil.getNodeTypeReferenceCount(root, Token.NAME, Predicates.<Node>alwaysTrue()));

    final List<Node> visited = new ArrayList<Node>();
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visited.add(node);
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertEquals(4, visited.size());
  }

  @Test
  public void testRemoveChildAndTryCatchFinally() {
    Node tryNode = new Node(Token.TRY);
    Node body = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    catchBlock.addChildToBack(catchNode);
    Node finallyBlock = new Node(Token.BLOCK);

    tryNode.addChildToBack(body);
    tryNode.addChildToBack(catchBlock);
    tryNode.addChildToBack(finallyBlock);

    Assert.assertTrue(NodeUtil.hasFinally(tryNode));
    Assert.assertEquals(catchBlock, NodeUtil.getCatchBlock(tryNode));
    Assert.assertTrue(NodeUtil.hasCatchHandler(catchBlock));
    Assert.assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));

    NodeUtil.removeChild(tryNode, finallyBlock);
    Assert.assertEquals(2, tryNode.getChildCount());

    Node blockParent = new Node(Token.BLOCK);
    Node stmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node stmt2 = NodeUtil.newExpr(Node.newNumber(2));
    blockParent.addChildToBack(stmt1);
    blockParent.addChildToBack(stmt2);

    NodeUtil.removeChild(blockParent, stmt1);
    Assert.assertEquals(1, blockParent.getChildCount());
    Assert.assertEquals(stmt2, blockParent.getFirstChild());
  }

  @Test
  public void testTryMergeBlock() {
    Node parentBlock = new Node(Token.BLOCK);
    Node childBlock = new Node(Token.BLOCK);
    Node stmt = NodeUtil.newExpr(Node.newNumber(1));
    childBlock.addChildToBack(stmt);
    parentBlock.addChildToBack(childBlock);

    boolean merged = NodeUtil.tryMergeBlock(childBlock);
    Assert.assertTrue(merged);
    Assert.assertEquals(1, parentBlock.getChildCount());
    Assert.assertEquals(stmt, parentBlock.getFirstChild());
  }

  @Test
  public void testEvaluatesToLocalValue() {
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("s")));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(add));
  }

  @Test
  public void testGetArgumentForCallOrNewAndFunction() {
    Node fn = NodeUtil.newFunctionNode("fn", Arrays.asList(Node.newString(Token.NAME, "p1"), Node.newString(Token.NAME, "p2")), new Node(Token.BLOCK), 1, 0);
    Assert.assertEquals("p1", NodeUtil.getArgumentForFunction(fn, 0).getString());
    Assert.assertEquals("p2", NodeUtil.getArgumentForFunction(fn, 1).getString());
    Assert.assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node call = NodeUtil.newCallNode(Node.newString(Token.NAME, "fn"), Node.newNumber(10), Node.newNumber(20));
    Assert.assertEquals(10.0, NodeUtil.getArgumentForCallOrNew(call, 0).getDouble(), 0.0);
    Assert.assertEquals(20.0, NodeUtil.getArgumentForCallOrNew(call, 1).getDouble(), 0.0);
    Assert.assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));
  }

  @Test
  public void testReferencesThisAndContainsCall() {
    Node fnNoThis = NodeUtil.newFunctionNode("fn", Collections.<Node>emptyList(), new Node(Token.BLOCK, Node.newNumber(1)), 1, 0);
    Assert.assertFalse(NodeUtil.referencesThis(fnNoThis));

    Node fnWithThis = NodeUtil.newFunctionNode("fn", Collections.<Node>emptyList(), new Node(Token.BLOCK, new Node(Token.THIS)), 1, 0);
    Assert.assertTrue(NodeUtil.referencesThis(fnWithThis));

    Assert.assertFalse(NodeUtil.containsCall(fnNoThis));
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    fnNoThis.getLastChild().addChildToBack(call);
    Assert.assertTrue(NodeUtil.containsCall(fnNoThis));
  }

  @Test
  public void testGetVarsDeclaredInBranch() {
    Node block = new Node(Token.BLOCK);
    Node varNode = NodeUtil.newVarNode("v1", Node.newNumber(1));
    Node varNode2 = NodeUtil.newVarNode("v2", Node.newNumber(2));
    block.addChildToBack(varNode);
    block.addChildToBack(varNode2);

    java.util.Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(block);
    Assert.assertEquals(2, vars.size());
  }

  @Test
  public void testIsLValue() {
    Node name = Node.newString(Token.NAME, "x");
    Node assign = new Node(Token.ASSIGN, name, Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isLValue(name));

    Node inc = new Node(Token.INC, name);
    Assert.assertTrue(NodeUtil.isLValue(name));
  }
}