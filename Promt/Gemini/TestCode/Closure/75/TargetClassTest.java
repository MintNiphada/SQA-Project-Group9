package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TargetClassTest {

  private final CodingConvention convention = new DefaultCodingConvention();

  @Test
  public void testGetImpureAndPureBooleanValue() {
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString("hello")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString("")));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newNumber(1)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(0)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.NULL)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.FALSE)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.VOID, Node.newNumber(0))));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.TRUE)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.REGEXP)));

    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "someVar")));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notTrue));

    Node arr = new Node(Token.ARRAYLIT);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(arr));
    Node obj = new Node(Token.OBJECTLIT);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(obj));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    Node comma = new Node(Token.COMMA, Node.newNumber(0), Node.newString("yes"));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(comma));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hook1 = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), new Node(Token.TRUE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hook1));

    Node hook2 = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hook2));
  }

  @Test
  public void testGetStringValue() {
    Assert.assertEquals("str", NodeUtil.getStringValue(Node.newString("str")));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "other")));

    Assert.assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123.0)));
    Assert.assertEquals("123.45", NodeUtil.getStringValue(Node.newNumber(123.45)));
    Assert.assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    Assert.assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    Assert.assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals("false", NodeUtil.getStringValue(not));
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals("true", NodeUtil.getStringValue(notFalse));

    Node arr = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newNumber(1), new Node(Token.NULL), new Node(Token.EMPTY));
    Assert.assertEquals("a,1,,", NodeUtil.getStringValue(arr));

    Assert.assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));
  }

  @Test
  public void testGetNumberValue() {
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    Assert.assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(Node.newNumber(42.5)));

    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidNode)));

    Node voidWithSideEffect = new Node(Token.VOID, new Node(Token.CALL, Node.newString(Token.NAME, "foo")));
    Assert.assertNull(NodeUtil.getNumberValue(voidWithSideEffect));

    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    Assert.assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "foo")));

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    Assert.assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notTrue));
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notFalse));

    Assert.assertEquals(Double.valueOf(10.0), NodeUtil.getNumberValue(Node.newString("10")));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(new Node(Token.OBJECTLIT))));
  }

  @Test
  public void testGetStringNumberValueAndTrimming() {
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   \t\n\r  "));
    Assert.assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
    Assert.assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    Assert.assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));
    Assert.assertNull(NodeUtil.getStringNumberValue("+0x10"));
    Assert.assertNull(NodeUtil.getStringNumberValue("-0x10"));
    Assert.assertNull(NodeUtil.getStringNumberValue("infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("-infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("+infinity"));
    Assert.assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue("  123.45 \u00A0 \uFEFF "));
    Assert.assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not_a_number")));

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u000B'));
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
  public void testGetFunctionNameAndNearestFunctionName() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);
    Assert.assertEquals("foo", NodeUtil.getFunctionName(fn));
    Assert.assertEquals("foo", NodeUtil.getNearestFunctionName(fn));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node varName = Node.newString(Token.NAME, "myVar");
    varName.addChildToBack(anonFn);
    new Node(Token.VAR, varName);
    Assert.assertEquals("myVar", NodeUtil.getFunctionName(anonFn));

    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    Node assignFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.ASSIGN, getProp, assignFn);
    Assert.assertEquals("a.b", NodeUtil.getFunctionName(assignFn));

    Node key = Node.newString(Token.STRING, "propKey");
    Node objFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    key.addChildToBack(objFn);
    new Node(Token.OBJECTLIT, key);
    Assert.assertEquals("propKey", NodeUtil.getNearestFunctionName(objFn));

    Node numKey = Node.newNumber(12);
    numKey.addChildToBack(objFn);
    new Node(Token.OBJECTLIT, numKey);
    Assert.assertEquals("12", NodeUtil.getNearestFunctionName(objFn));
  }

  @Test
  public void testIsImmutableAndLiteralValue() {
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, new Node(Token.TRUE))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "custom")));

    Node arrLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    Assert.assertTrue(NodeUtil.isLiteralValue(arrLit, false));
    Node badArrLit = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "custom"));
    Assert.assertFalse(NodeUtil.isLiteralValue(badArrLit, false));

    Node regExp = new Node(Token.REGEXP, Node.newString("abc"));
    Assert.assertTrue(NodeUtil.isLiteralValue(regExp, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node k = Node.newString(Token.STRING, "k");
    k.addChildToBack(Node.newNumber(10));
    objLit.addChildToBack(k);
    Assert.assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fnExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.EXPR_RESULT, fnExpr);
    Assert.assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    Assert.assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = ImmutableSet.of("DEF_A", "DEF_B", "pkg.DEF_C");
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString("str"), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    Assert.assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN"), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertTrue(NodeUtil.isValidDefineValue(not, defines));

    Node qname = new Node(Token.GETPROP, Node.newString(Token.NAME, "pkg"), Node.newString(Token.STRING, "DEF_C"));
    Assert.assertTrue(NodeUtil.isValidDefineValue(qname, defines));
  }

  @Test
  public void testIsEmptyBlockAndSimpleOperator() {
    Node block = new Node(Token.BLOCK);
    Assert.assertTrue(NodeUtil.isEmptyBlock(block));
    block.addChildToBack(new Node(Token.EMPTY));
    Assert.assertTrue(NodeUtil.isEmptyBlock(block));
    block.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Assert.assertFalse(NodeUtil.isEmptyBlock(block));
    Assert.assertFalse(NodeUtil.isEmptyBlock(new Node(Token.EXPR_RESULT)));

    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITAND));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITNOT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITOR));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITXOR));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.COMMA));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.DIV));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.EQ));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GETELEM));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GETPROP));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.INSTANCEOF));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.LE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.LSH));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.LT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.MOD));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.MUL));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.NE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.NOT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.RSH));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.SHEQ));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.SHNE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.VOID));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.POS));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.NEG));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.URSH));
    Assert.assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.isSimpleOperator(addNode));
  }

  @Test
  public void testNewExpr() {
    Node child = Node.newNumber(1);
    Node expr = NodeUtil.newExpr(child);
    Assert.assertEquals(Token.EXPR_RESULT, expr.getType());
    Assert.assertSame(child, expr.getFirstChild());
  }

  @Test
  public void testSideEffectsAndMutableState() {
    Node num = Node.newNumber(42);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(num));
    Assert.assertFalse(NodeUtil.mayEffectMutableState(num));

    Node throwNode = new Node(Token.THROW, Node.newString("err"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(throwNode));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    Assert.assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(newArray));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(newArray));

    Node customNew = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    Assert.assertTrue(NodeUtil.constructorCallHasSideEffects(customNew));

    Node callMath = new Node(Token.CALL, new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString(Token.STRING, "sin")), Node.newNumber(1));
    Assert.assertFalse(NodeUtil.functionCallHasSideEffects(callMath));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "customFn"));
    Assert.assertTrue(NodeUtil.functionCallHasSideEffects(customCall));

    Node builtinCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"), Node.newNumber(123));
    Assert.assertFalse(NodeUtil.functionCallHasSideEffects(builtinCall));

    Node assignName = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(assignName));

    Node decNode = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(decNode));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.INC, Node.newString(Token.NAME, "x"))));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DELPROP, Node.newString(Token.NAME, "x"))));

    Node nameWithChild = Node.newString(Token.NAME, "x");
    nameWithChild.addChildToBack(Node.newNumber(1));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(nameWithChild));
  }

  @Test
  public void testCanBeSideEffected() {
    Node name = Node.newString(Token.NAME, "x");
    Assert.assertTrue(NodeUtil.canBeSideEffected(name));
    Assert.assertFalse(NodeUtil.canBeSideEffected(name, Collections.singleton("x")));

    Node num = Node.newNumber(10);
    Assert.assertFalse(NodeUtil.canBeSideEffected(num));

    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "p"));
    Assert.assertTrue(NodeUtil.canBeSideEffected(getProp));
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
    Assert.assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  @Test(expected = Error.class)
  public void testPrecedenceUnknown() {
    NodeUtil.precedence(Token.LABEL);
  }

  @Test
  public void testResultTypesPredicates() {
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newNumber(10)));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "NaN")));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "Infinity")));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.SUB, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.MUL, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.DIV, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.MOD, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.BITNOT, Node.newNumber(1))));

    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.INSTANCEOF, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.DELPROP, Node.newString(Token.NAME, "x"))));

    Assert.assertTrue(NodeUtil.isUndefined(new Node(Token.VOID, Node.newNumber(0))));
    Assert.assertTrue(NodeUtil.isUndefined(Node.newString(Token.NAME, "undefined")));
    Assert.assertTrue(NodeUtil.isNull(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.VOID)));

    Assert.assertTrue(NodeUtil.mayBeString(Node.newString("str")));
    Assert.assertFalse(NodeUtil.mayBeString(Node.newNumber(1)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.TRUE)));
  }

  @Test
  public void testAssociativeAndCommutative() {
    Assert.assertTrue(NodeUtil.isAssociative(Token.MUL));
    Assert.assertTrue(NodeUtil.isAssociative(Token.AND));
    Assert.assertTrue(NodeUtil.isAssociative(Token.OR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITOR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITXOR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITAND));
    Assert.assertFalse(NodeUtil.isAssociative(Token.SUB));

    Assert.assertTrue(NodeUtil.isCommutative(Token.MUL));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITOR));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITXOR));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITAND));
    Assert.assertFalse(NodeUtil.isCommutative(Token.ADD));
  }

  @Test
  public void testAssignmentOps() {
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_SUB)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MUL)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_DIV)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MOD)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_LSH)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_RSH)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_URSH)));
    Assert.assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));

    Assert.assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    Assert.assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    Assert.assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    Assert.assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH)));
    Assert.assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH)));
    Assert.assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH)));
    Assert.assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    Assert.assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    Assert.assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    Assert.assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    Assert.assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetOpFromAssignmentOpFail() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test
  public void testNodeClassificationHelpers() {
    Node nameNode = Node.newString(Token.NAME, "x");
    Assert.assertTrue(NodeUtil.isName(nameNode));
    Assert.assertFalse(NodeUtil.isName(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isReferenceName(nameNode));
    Assert.assertFalse(NodeUtil.isReferenceName(Node.newString(Token.NAME, "")));

    Node varNode = new Node(Token.VAR, nameNode);
    Assert.assertTrue(NodeUtil.isVar(varNode));
    Assert.assertTrue(NodeUtil.isVarDeclaration(nameNode));

    Node valNode = Node.newNumber(42);
    nameNode.addChildToBack(valNode);
    Assert.assertSame(valNode, NodeUtil.getAssignedValue(nameNode));

    Node assignTarget = Node.newString(Token.NAME, "y");
    Node assignVal = Node.newNumber(99);
    Node assignNode = new Node(Token.ASSIGN, assignTarget, assignVal);
    Assert.assertTrue(NodeUtil.isAssign(assignNode));
    Assert.assertSame(assignVal, NodeUtil.getAssignedValue(assignTarget));

    Node exprAssign = new Node(Token.EXPR_RESULT, assignNode);
    Assert.assertTrue(NodeUtil.isExprAssign(exprAssign));
    Assert.assertTrue(NodeUtil.isExpressionNode(exprAssign));

    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    Node exprCall = new Node(Token.EXPR_RESULT, callNode);
    Assert.assertTrue(NodeUtil.isExprCall(exprCall));
    Assert.assertTrue(NodeUtil.isCall(callNode));
    Assert.assertTrue(NodeUtil.isCallOrNew(callNode));

    Node newNode = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    Assert.assertTrue(NodeUtil.isNew(newNode));
    Assert.assertTrue(NodeUtil.isCallOrNew(newNode));

    Node strNode = Node.newString("hello");
    Assert.assertTrue(NodeUtil.isString(strNode));

    Node thisNode = new Node(Token.THIS);
    Assert.assertTrue(NodeUtil.isThis(thisNode));

    Node arrayLit = new Node(Token.ARRAYLIT);
    Assert.assertTrue(NodeUtil.isArrayLiteral(arrayLit));

    Node labelName = Node.newString(Token.LABEL_NAME, "lbl");
    Assert.assertTrue(NodeUtil.isLabelName(labelName));
    Assert.assertFalse(NodeUtil.isLabelName(null));
  }

  @Test
  public void testControlStructuresAndLoops() {
    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "i"), Node.newString(Token.NAME, "obj"), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isForIn(forIn));
    Assert.assertTrue(NodeUtil.isLoopStructure(forIn));

    Node forLoop = new Node(Token.FOR, new Node(Token.EMPTY), Node.newNumber(1), new Node(Token.EMPTY), new Node(Token.BLOCK));
    Assert.assertFalse(NodeUtil.isForIn(forLoop));
    Assert.assertTrue(NodeUtil.isLoopStructure(forLoop));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(forLoop).getType());
    Assert.assertEquals(Token.NUMBER, NodeUtil.getConditionExpression(forLoop).getType());

    Node whileLoop = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isLoopStructure(whileLoop));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(whileLoop).getType());
    Assert.assertEquals(Token.TRUE, NodeUtil.getConditionExpression(whileLoop).getType());

    Node doLoop = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.FALSE));
    Assert.assertTrue(NodeUtil.isLoopStructure(doLoop));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(doLoop).getType());
    Assert.assertEquals(Token.FALSE, NodeUtil.getConditionExpression(doLoop).getType());

    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isControlStructure(ifNode));
    Assert.assertEquals(Token.TRUE, NodeUtil.getConditionExpression(ifNode));

    Node childInLoop = Node.newNumber(1);
    whileLoop.getLastChild().addChildToBack(childInLoop);
    Assert.assertTrue(NodeUtil.isWithinLoop(childInLoop));

    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(whileLoop, whileLoop.getLastChild()));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(doLoop, doLoop.getFirstChild()));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getLastChild()));
  }

  @Test
  public void testStatementsAndBlocks() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    script.addChildToBack(block);
    Assert.assertTrue(NodeUtil.isStatementBlock(script));
    Assert.assertTrue(NodeUtil.isStatementBlock(block));
    Assert.assertTrue(NodeUtil.isStatement(block));

    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    block.addChildToBack(expr);
    Assert.assertTrue(NodeUtil.isStatement(expr));

    Node caseNode = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isSwitchCase(caseNode));
    Assert.assertTrue(NodeUtil.isSwitchCase(defaultNode));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBody = new Node(Token.BLOCK);
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    catchBody.addChildToBack(catchNode);
    Node finallyBody = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBody, catchBody, finallyBody);

    Assert.assertTrue(NodeUtil.hasFinally(tryNode));
    Assert.assertSame(catchBody, NodeUtil.getCatchBlock(tryNode));
    Assert.assertTrue(NodeUtil.hasCatchHandler(catchBody));
    Assert.assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBody));
    Assert.assertTrue(NodeUtil.isTryCatchNodeContainer(catchBody));

    Node tryWithoutFinally = new Node(Token.TRY, new Node(Token.BLOCK), new Node(Token.BLOCK));
    Assert.assertFalse(NodeUtil.hasFinally(tryWithoutFinally));
    NodeUtil.maybeAddFinally(tryWithoutFinally);
    Assert.assertTrue(NodeUtil.hasFinally(tryWithoutFinally));
  }

  @Test
  public void testRemoveChildAndTryMergeBlock() {
    Node block = new Node(Token.BLOCK);
    Node s1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node s2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    block.addChildToBack(s1);
    block.addChildToBack(s2);

    NodeUtil.removeChild(block, s1);
    Assert.assertEquals(1, block.getChildCount());

    Node rootBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK, Node.newNumber(10));
    rootBlock.addChildToBack(innerBlock);
    Assert.assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    Assert.assertEquals(1, rootBlock.getChildCount());
    Assert.assertEquals(Token.NUMBER, rootBlock.getFirstChild().getType());
  }

  @Test
  public void testFunctionPredicatesAndInfo() {
    Node body = new Node(Token.BLOCK);
    Node fn = NodeUtil.newFunctionNode("testFn", Collections.<Node>emptyList(), body, 1, 0);
    Assert.assertTrue(NodeUtil.isFunction(fn));
    Assert.assertSame(body, NodeUtil.getFunctionBody(fn));
    Assert.assertEquals(Token.LP, NodeUtil.getFnParameters(fn).getType());

    Node script = new Node(Token.SCRIPT, fn);
    Assert.assertTrue(NodeUtil.isFunctionDeclaration(fn));
    Assert.assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));
    Assert.assertFalse(NodeUtil.isFunctionExpression(fn));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.EXPR_RESULT, anonFn);
    Assert.assertTrue(NodeUtil.isFunctionExpression(anonFn));
    Assert.assertTrue(NodeUtil.isEmptyFunctionExpression(anonFn));

    Node fnWithArgs = new Node(Token.FUNCTION, Node.newString(Token.NAME, "fn"), new Node(Token.LP),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "arguments"))));
    Assert.assertTrue(NodeUtil.isVarArgsFunction(fnWithArgs));
  }

  @Test
  public void testMethodCallsAndLhs() {
    Node objCall = new Node(Token.CALL, new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "call")), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isFunctionObjectCall(objCall));
    Assert.assertTrue(NodeUtil.isFunctionObjectCallOrApply(objCall));
    Assert.assertTrue(NodeUtil.isSimpleFunctionObjectCall(objCall));

    Node objApply = new Node(Token.CALL, new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "apply")), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isFunctionObjectApply(objApply));
    Assert.assertTrue(NodeUtil.isFunctionObjectCallOrApply(objApply));

    Node name = Node.newString(Token.NAME, "v");
    Node var = new Node(Token.VAR, name);
    Assert.assertTrue(NodeUtil.isLhs(name, var));

    Node assign = new Node(Token.ASSIGN, name, Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isLhs(name, assign));
  }

  @Test
  public void testObjectLitKey() {
    Node strKey = Node.newString(Token.STRING, "myKey");
    Node objLit = new Node(Token.OBJECTLIT, strKey);
    Assert.assertTrue(NodeUtil.isObjectLitKey(strKey, objLit));
    Assert.assertEquals("myKey", NodeUtil.getObjectLitKeyName(strKey));

    Node getKey = Node.newString(Token.GET, "getProp");
    Assert.assertTrue(NodeUtil.isGetOrSetKey(getKey));
    Assert.assertEquals("getProp", NodeUtil.getObjectLitKeyName(getKey));
  }

  @Test
  public void testOpToStr() {
    Assert.assertEquals("+", NodeUtil.opToStr(Token.ADD));
    Assert.assertEquals("-", NodeUtil.opToStr(Token.SUB));
    Assert.assertEquals("==", NodeUtil.opToStr(Token.EQ));
    Assert.assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    Assert.assertEquals("!=", NodeUtil.opToStr(Token.NE));
    Assert.assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    Assert.assertEquals("+=", NodeUtil.opToStr(Token.ASSIGN_ADD));
    Assert.assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    Assert.assertEquals("void", NodeUtil.opToStr(Token.VOID));
    Assert.assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    Assert.assertNull(NodeUtil.opToStr(Token.CALL));

    Assert.assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(expected = Error.class)
  public void testOpToStrNoFailError() {
    NodeUtil.opToStrNoFail(Token.CALL);
  }

  @Test
  public void testQualifiedNames() {
    Node qname = NodeUtil.newQualifiedNameNode(convention, "a.b.c", 1, 2);
    Assert.assertEquals(Token.GETPROP, qname.getType());
    Assert.assertEquals("a.b.c", qname.getQualifiedName());

    Node root = NodeUtil.getRootOfQualifiedName(qname);
    Assert.assertEquals("a", root.getString());

    Node singleName = NodeUtil.newQualifiedNameNode(convention, "simpleName", 1, 2);
    Assert.assertEquals(Token.NAME, singleName.getType());
    Assert.assertEquals("simpleName", singleName.getString());
  }

  @Test
  public void testLatinAndPropertyName() {
    Assert.assertTrue(NodeUtil.isLatin("HelloWorld123_"));
    Assert.assertFalse(NodeUtil.isLatin("Hello\u1234"));

    Assert.assertTrue(NodeUtil.isValidPropertyName("validProp"));
    Assert.assertTrue(NodeUtil.isValidPropertyName("$var"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("var")); // keyword
    Assert.assertFalse(NodeUtil.isValidPropertyName("123bad"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("prop\u1234"));
  }

  @Test
  public void testPrototypeHelpers() {
    Node qname = NodeUtil.newQualifiedNameNode(convention, "MyClass.prototype.myMethod", 0, 0);
    Assert.assertTrue(NodeUtil.isPrototypeProperty(qname));

    Node exprAssign = new Node(Token.EXPR_RESULT, new Node(Token.ASSIGN, qname, Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));

    Node classNameNode = NodeUtil.getPrototypeClassName(qname);
    Assert.assertNotNull(classNameNode);
    Assert.assertEquals("MyClass", classNameNode.getQualifiedName());
    Assert.assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qname));
  }

  @Test
  public void testTreeTraversalAndCounts() {
    Node root = new Node(Token.BLOCK);
    Node name1 = Node.newString(Token.NAME, "foo");
    Node name2 = Node.newString(Token.NAME, "bar");
    Node name3 = Node.newString(Token.NAME, "foo");
    root.addChildToBack(name1);
    root.addChildToBack(name2);
    root.addChildToBack(name3);

    Assert.assertTrue(NodeUtil.isNameReferenced(root, "foo"));
    Assert.assertFalse(NodeUtil.isNameReferenced(root, "baz"));
    Assert.assertEquals(2, NodeUtil.getNameReferenceCount(root, "foo"));
    Assert.assertEquals(3, NodeUtil.getNodeTypeReferenceCount(root, Token.NAME, Predicates.<Node>alwaysTrue()));

    final List<Node> visited = new ArrayList<Node>();
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visited.add(node);
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertEquals(4, visited.size());

    visited.clear();
    NodeUtil.visitPostOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visited.add(node);
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertEquals(4, visited.size());
    Assert.assertSame(root, visited.get(visited.size() - 1));
  }

  @Test
  public void testNewCallNodeAndUndefined() {
    Node target = Node.newString(Token.NAME, "func");
    Node arg1 = Node.newNumber(1);
    Node arg2 = Node.newNumber(2);
    Node call = NodeUtil.newCallNode(target, arg1, arg2);
    Assert.assertEquals(Token.CALL, call.getType());
    Assert.assertTrue(call.getBooleanProp(Node.FREE_CALL));
    Assert.assertEquals(3, call.getChildCount());

    Node undef = NodeUtil.newUndefinedNode(null);
    Assert.assertEquals(Token.VOID, undef.getType());

    Node varNode = NodeUtil.newVarNode("v", Node.newNumber(10));
    Assert.assertEquals(Token.VAR, varNode.getType());
    Assert.assertEquals("v", varNode.getFirstChild().getString());
  }

  @Test
  public void testEvaluatesToLocalValue() {
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.REGEXP, Node.newString("a"))));

    Node comma = new Node(Token.COMMA, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(comma));

    Node hook = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(hook));

    Node and = new Node(Token.AND, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(and));
  }

  @Test
  public void testGetArgumentForFunctionAndCall() {
    Node p1 = Node.newString(Token.NAME, "a");
    Node p2 = Node.newString(Token.NAME, "b");
    Node lp = new Node(Token.LP, p1, p2);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), lp, new Node(Token.BLOCK));
    Assert.assertSame(p1, NodeUtil.getArgumentForFunction(fn, 0));
    Assert.assertSame(p2, NodeUtil.getArgumentForFunction(fn, 1));
    Assert.assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node a1 = Node.newNumber(10);
    Node a2 = Node.newNumber(20);
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), a1, a2);
    Assert.assertSame(a1, NodeUtil.getArgumentForCallOrNew(call, 0));
    Assert.assertSame(a2, NodeUtil.getArgumentForCallOrNew(call, 1));
    Assert.assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));
  }

  @Test
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node ifNode = new Node(Token.IF, Node.newNumber(1), new Node(Token.BLOCK));
    script.addChildToBack(ifNode);

    Node block = ifNode.getLastChild();
    Node varNode = NodeUtil.newVarNode("declaredVar", Node.newNumber(5));
    block.addChildToBack(varNode);

    NodeUtil.redeclareVarsInsideBranch(ifNode);
    Assert.assertEquals(Token.VAR, script.getFirstChild().getType());
    Assert.assertEquals("declaredVar", script.getFirstChild().getFirstChild().getString());
  }

  @Test
  public void testConstantHelpers() {
    Node nameNode = Node.newString(Token.NAME, "CONST_VAL");
    nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Assert.assertTrue(NodeUtil.isConstantName(nameNode));

    Node nonConst = Node.newString(Token.NAME, "val");
    Assert.assertFalse(NodeUtil.isConstantName(nonConst));
  }
}