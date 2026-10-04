package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilTest {

  private final CodingConvention convention = new DefaultCodingConvention();

  @Test
  public void testGetImpureBooleanValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.string("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.string("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.number(1)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.number(0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.OBJECTLIT)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.voidNode(IR.number(0))));

    Node assign = IR.assign(IR.name("x"), IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    Node comma = IR.comma(IR.number(0), IR.string("a"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(comma));

    Node notNode = IR.not(IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(notNode));

    Node andNode = IR.and(IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = IR.or(IR.falseNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hookSame = IR.hook(IR.name("cond"), IR.trueNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookSame));

    Node hookDiff = IR.hook(IR.name("cond"), IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDiff));
  }

  @Test
  public void testGetPureBooleanValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.string("abc")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.string("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.number(42)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.number(0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.nullNode()));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.falseNode()));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.trueNode()));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.regexp(IR.string("foo"))));

    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.name("Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(IR.name("other")));

    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.voidNode(IR.number(0))));

    Node arrayLit = new Node(Token.ARRAYLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(arrayLit));

    Node objLit = new Node(Token.OBJECTLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(objLit));

    Node not = IR.not(IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(not));
  }

  @Test
  public void testGetStringValue() {
    assertEquals("test", NodeUtil.getStringValue(IR.string("test")));
    assertEquals("testKey", NodeUtil.getStringValue(IR.stringKey("testKey")));
    assertEquals("undefined", NodeUtil.getStringValue(IR.name("undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(IR.name("Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(IR.name("NaN")));
    assertNull(NodeUtil.getStringValue(IR.name("random")));

    assertEquals("10", NodeUtil.getStringValue(IR.number(10.0)));
    assertEquals("10.5", NodeUtil.getStringValue(IR.number(10.5)));
    assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
    assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
    assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
    assertEquals("undefined", NodeUtil.getStringValue(IR.voidNode(IR.number(0))));
    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));

    Node not = IR.not(IR.trueNode());
    assertEquals("false", NodeUtil.getStringValue(not));

    Node arrayLit = new Node(Token.ARRAYLIT, IR.string("a"), IR.number(1), IR.nullNode(), IR.empty());
    assertEquals("a,1,,", NodeUtil.getStringValue(arrayLit));
  }

  @Test
  public void testGetStringValueDouble() {
    assertEquals("0", NodeUtil.getStringValue(0.0));
    assertEquals("1", NodeUtil.getStringValue(1.0));
    assertEquals("-5", NodeUtil.getStringValue(-5.0));
    assertEquals("1.25", NodeUtil.getStringValue(1.25));
  }

  @Test
  public void testGetNumberValue() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.trueNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.falseNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.nullNode()));
    assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(IR.number(123.0)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.voidNode(IR.number(0)))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(IR.name("Infinity")));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(IR.neg(IR.name("Infinity"))));
    assertNull(NodeUtil.getNumberValue(IR.neg(IR.name("x"))));

    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.not(IR.trueNode())));
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.not(IR.falseNode())));
    assertEquals(Double.valueOf(42.0), NodeUtil.getNumberValue(IR.string("42")));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.ARRAYLIT)));
    assertNull(NodeUtil.getNumberValue(new Node(Token.OBJECTLIT)));
  }

  @Test
  public void testGetStringNumberValue() {
    assertNull(NodeUtil.getStringNumberValue("abc\u000bdef"));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xFF"));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0Xff"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));
    assertNull(NodeUtil.getStringNumberValue("+0x12"));
    assertNull(NodeUtil.getStringNumberValue("-0x12"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));
    assertEquals(Double.valueOf(12.34), NodeUtil.getStringNumberValue("  12.34  "));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("invalid")));
  }

  @Test
  public void testTrimJsWhiteSpaceAndIsStrWhiteSpaceChar() {
    assertEquals("foo", NodeUtil.trimJsWhiteSpace(" \t\n\r\u00A0\u000C\u2028\u2029\uFEFFfoo \t"));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('A'));
  }

  @Test
  public void testGetFunctionNameAndGetNearestFunctionName() {
    Node fn = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    assertEquals("foo", NodeUtil.getFunctionName(fn));

    Node varParent = IR.var(IR.name("bar"), fn);
    assertEquals("bar", NodeUtil.getFunctionName(fn));

    Node assignParent = IR.assign(IR.name("baz"), fn);
    assertEquals("baz", NodeUtil.getFunctionName(fn));

    assertNull(NodeUtil.getNearestFunctionName(IR.number(1)));
    assertEquals("baz", NodeUtil.getNearestFunctionName(fn));

    Node objKey = IR.stringKey("myKey", IR.function(IR.name(""), IR.paramList(), IR.block()));
    assertEquals("myKey", NodeUtil.getNearestFunctionName(objKey.getFirstChild()));

    Node numKey = IR.number(123);
    numKey.addChildToBack(IR.function(IR.name(""), IR.paramList(), IR.block()));
    assertEquals("123", NodeUtil.getNearestFunctionName(numKey.getFirstChild()));
  }

  @Test
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(IR.string("s")));
    assertTrue(NodeUtil.isImmutableValue(IR.number(1)));
    assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.cast(IR.number(1))));
    assertTrue(NodeUtil.isImmutableValue(IR.not(IR.trueNode())));
    assertTrue(NodeUtil.isImmutableValue(IR.voidNode(IR.number(0))));
    assertTrue(NodeUtil.isImmutableValue(IR.neg(IR.number(1))));
    assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("Infinity")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("NaN")));
    assertFalse(NodeUtil.isImmutableValue(IR.name("x")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testIsSymmetricAndRelationalAndInverse() {
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.EQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.NE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHEQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHNE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.MUL)));
    assertFalse(NodeUtil.isSymmetricOperation(new Node(Token.ADD)));

    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GE)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LE)));
    assertFalse(NodeUtil.isRelationalOperation(new Node(Token.EQ)));

    assertEquals(Token.LT, NodeUtil.getInverseOperator(Token.GT));
    assertEquals(Token.GT, NodeUtil.getInverseOperator(Token.LT));
    assertEquals(Token.LE, NodeUtil.getInverseOperator(Token.GE));
    assertEquals(Token.GE, NodeUtil.getInverseOperator(Token.LE));
    assertEquals(Token.ERROR, NodeUtil.getInverseOperator(Token.ADD));
  }

  @Test
  public void testIsLiteralValue() {
    assertTrue(NodeUtil.isLiteralValue(IR.cast(IR.number(5)), false));
    Node array = new Node(Token.ARRAYLIT, IR.number(1), IR.string("a"));
    assertTrue(NodeUtil.isLiteralValue(array, false));
    Node arrayNonConst = new Node(Token.ARRAYLIT, IR.name("x"));
    assertFalse(NodeUtil.isLiteralValue(arrayNonConst, false));

    Node regexp = IR.regexp(IR.string("abc"));
    assertTrue(NodeUtil.isLiteralValue(regexp, false));

    Node obj = new Node(Token.OBJECTLIT, IR.stringKey("a", IR.number(1)));
    assertTrue(NodeUtil.isLiteralValue(obj, false));

    Node fnExpr = IR.function(IR.name(""), IR.paramList(), IR.block());
    IR.exprResult(fnExpr);
    assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = Sets.newHashSet("DEF1", "DEF2");
    assertTrue(NodeUtil.isValidDefineValue(IR.string("str"), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.number(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.trueNode(), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.falseNode(), defines));

    Node add = IR.add(IR.number(1), IR.number(2));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = IR.not(IR.trueNode());
    assertTrue(NodeUtil.isValidDefineValue(not, defines));

    assertTrue(NodeUtil.isValidDefineValue(IR.name("DEF1"), defines));
    assertFalse(NodeUtil.isValidDefineValue(IR.name("OTHER"), defines));
    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.ARRAYLIT), defines));
  }

  @Test
  public void testIsEmptyBlock() {
    Node block = IR.block();
    assertTrue(NodeUtil.isEmptyBlock(block));
    block.addChildToBack(IR.empty());
    assertTrue(NodeUtil.isEmptyBlock(block));
    block.addChildToBack(IR.exprResult(IR.number(1)));
    assertFalse(NodeUtil.isEmptyBlock(block));
    assertFalse(NodeUtil.isEmptyBlock(IR.number(1)));
  }

  @Test
  public void testIsSimpleOperator() {
    assertTrue(NodeUtil.isSimpleOperator(IR.add(IR.number(1), IR.number(2))));
    assertTrue(NodeUtil.isSimpleOperator(IR.sub(IR.number(1), IR.number(2))));
    assertTrue(NodeUtil.isSimpleOperator(IR.not(IR.trueNode())));
    assertFalse(NodeUtil.isSimpleOperator(IR.assign(IR.name("a"), IR.number(1))));
  }

  @Test
  public void testNewExpr() {
    Node child = IR.number(1);
    Node expr = NodeUtil.newExpr(child);
    assertTrue(expr.isExprResult());
    assertEquals(child, expr.getFirstChild());
  }

  @Test
  public void testMayEffectMutableStateAndMayHaveSideEffects() {
    Node num = IR.number(1);
    assertFalse(NodeUtil.mayEffectMutableState(num));
    assertFalse(NodeUtil.mayHaveSideEffects(num));

    Node throwNode = new Node(Token.THROW, IR.string("error"));
    assertTrue(NodeUtil.mayEffectMutableState(throwNode));
    assertTrue(NodeUtil.mayHaveSideEffects(throwNode));

    Node objLit = new Node(Token.OBJECTLIT);
    assertTrue(NodeUtil.mayEffectMutableState(objLit));
    assertFalse(NodeUtil.mayHaveSideEffects(objLit));

    Node arrLit = new Node(Token.ARRAYLIT);
    assertTrue(NodeUtil.mayEffectMutableState(arrLit));
    assertFalse(NodeUtil.mayHaveSideEffects(arrLit));

    Node varNode = IR.var(IR.name("x"), IR.number(1));
    assertTrue(NodeUtil.mayHaveSideEffects(varNode));

    Node callNoSideEffect = IR.call(IR.name("Math.floor"), IR.number(1));
    callNoSideEffect.getFirstChild().putBooleanProp(Node.IS_CONSTANT_NAME, true);

    Node getProp = IR.getprop(IR.name("Math"), IR.string("floor"));
    Node mathFloorCall = IR.call(getProp, IR.number(1.5));
    assertFalse(NodeUtil.mayHaveSideEffects(mathFloorCall));
  }

  @Test
  public void testConstructorCallHasSideEffects() {
    Node newArray = IR.newNode(IR.name("Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = IR.newNode(IR.name("CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    newCustom.setSideEffectFlags(Node.NO_SIDE_EFFECTS);
    assertFalse(NodeUtil.constructorCallHasSideEffects(newCustom));
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructorCallHasSideEffectsFail() {
    NodeUtil.constructorCallHasSideEffects(IR.call(IR.name("foo")));
  }

  @Test
  public void testFunctionCallHasSideEffects() {
    Node callObject = IR.call(IR.name("Object"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callObject));

    Node callCustom = IR.call(IR.name("customFunc"));
    assertTrue(NodeUtil.functionCallHasSideEffects(callCustom));

    callCustom.setSideEffectFlags(Node.NO_SIDE_EFFECTS);
    assertFalse(NodeUtil.functionCallHasSideEffects(callCustom));

    Node toStringCall = IR.call(IR.getprop(IR.name("x"), IR.string("toString")));
    assertFalse(NodeUtil.functionCallHasSideEffects(toStringCall));
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionCallHasSideEffectsFail() {
    NodeUtil.functionCallHasSideEffects(IR.newNode(IR.name("foo")));
  }

  @Test
  public void testCallAndNewHasLocalResult() {
    Node call = IR.call(IR.name("f"));
    call.setSideEffectFlags(Node.FLAG_LOCAL_RESULTS);
    assertTrue(NodeUtil.callHasLocalResult(call));

    Node newNode = IR.newNode(IR.name("f"));
    newNode.setSideEffectFlags(Node.FLAG_MODIFIES_THIS);
    assertTrue(NodeUtil.newHasLocalResult(newNode));
  }

  @Test
  public void testNodeTypeMayHaveSideEffects() {
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(IR.delprop(IR.name("x"), IR.string("p"))));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(IR.inc(IR.name("x"), false)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(IR.dec(IR.name("x"), false)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.THROW, IR.name("e"))));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(IR.var(IR.name("x"), IR.number(1)).getFirstChild()));
    assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(IR.name("x")));
    assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(IR.number(1)));
  }

  @Test
  public void testAllArgsUnescapedLocal() {
    Node call = IR.call(IR.name("f"), IR.number(1), IR.string("a"));
    assertTrue(NodeUtil.allArgsUnescapedLocal(call));

    Node callNonLocal = IR.call(IR.name("f"), IR.name("globalVar"));
    assertFalse(NodeUtil.allArgsUnescapedLocal(callNonLocal));
  }

  @Test
  public void testCanBeSideEffected() {
    Node num = IR.number(1);
    assertFalse(NodeUtil.canBeSideEffected(num));

    Node call = IR.call(IR.name("f"));
    assertTrue(NodeUtil.canBeSideEffected(call));

    Node name = IR.name("x");
    assertTrue(NodeUtil.canBeSideEffected(name));
    assertFalse(NodeUtil.canBeSideEffected(name, ImmutableSet.of("x")));

    Node getprop = IR.getprop(IR.name("x"), IR.string("y"));
    assertTrue(NodeUtil.canBeSideEffected(getprop));

    Node fn = IR.function(IR.name(""), IR.paramList(), IR.block());
    IR.exprResult(fn);
    assertFalse(NodeUtil.canBeSideEffected(fn));
  }

  @Test
  public void testPrecedence() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(5, NodeUtil.precedence(Token.BITOR));
    assertEquals(6, NodeUtil.precedence(Token.BITXOR));
    assertEquals(7, NodeUtil.precedence(Token.BITAND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(9, NodeUtil.precedence(Token.LT));
    assertEquals(10, NodeUtil.precedence(Token.LSH));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.CALL));
    assertEquals(16, NodeUtil.precedence(Token.CAST));
    assertEquals(-1, NodeUtil.precedenceWithDefault(Token.BLOCK));
  }

  @Test(expected = Error.class)
  public void testPrecedenceFail() {
    NodeUtil.precedence(Token.BLOCK);
  }

  @Test
  public void testIsUndefinedAndNull() {
    assertTrue(NodeUtil.isUndefined(IR.voidNode(IR.number(0))));
    assertTrue(NodeUtil.isUndefined(IR.name("undefined")));
    assertFalse(NodeUtil.isUndefined(IR.name("x")));

    assertTrue(NodeUtil.isNullOrUndefined(IR.nullNode()));
    assertTrue(NodeUtil.isNullOrUndefined(IR.voidNode(IR.number(0))));
    assertFalse(NodeUtil.isNullOrUndefined(IR.number(1)));
  }

  @Test
  public void testAllAndAnyResultsMatch() {
    Node hook = IR.hook(IR.name("c"), IR.number(1), IR.number(2));
    assertTrue(NodeUtil.isNumericResult(hook));
    assertTrue(NodeUtil.isImmutableResult(hook));

    Node comma = IR.comma(IR.string("a"), IR.number(1));
    assertTrue(NodeUtil.isNumericResult(comma));

    Node cast = IR.cast(IR.number(1));
    assertTrue(NodeUtil.isNumericResult(cast));

    Node orNode = IR.or(IR.trueNode(), IR.falseNode());
    assertTrue(NodeUtil.isBooleanResult(orNode));

    Node andNode = IR.and(IR.trueNode(), IR.falseNode());
    assertTrue(NodeUtil.isBooleanResult(andNode));

    assertTrue(NodeUtil.mayBeString(IR.string("abc")));
    assertFalse(NodeUtil.mayBeString(IR.number(123)));
  }

  @Test
  public void testIsNumericResultHelper() {
    assertTrue(NodeUtil.isNumericResultHelper(IR.add(IR.number(1), IR.number(2))));
    assertFalse(NodeUtil.isNumericResultHelper(IR.add(IR.string("a"), IR.number(2))));
    assertTrue(NodeUtil.isNumericResultHelper(IR.name("NaN")));
    assertTrue(NodeUtil.isNumericResultHelper(IR.name("Infinity")));
    assertFalse(NodeUtil.isNumericResultHelper(IR.name("other")));
  }

  @Test
  public void testIsBooleanResultHelper() {
    assertTrue(NodeUtil.isBooleanResultHelper(IR.trueNode()));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.falseNode()));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.eq(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.ne(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.sheq(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.shne(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.lt(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.gt(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.le(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.ge(IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResultHelper(new Node(Token.IN, IR.string("x"), IR.name("y"))));
    assertTrue(NodeUtil.isBooleanResultHelper(new Node(Token.INSTANCEOF, IR.name("x"), IR.name("y"))));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.not(IR.trueNode())));
    assertTrue(NodeUtil.isBooleanResultHelper(IR.delprop(IR.name("x"), IR.string("p"))));
    assertFalse(NodeUtil.isBooleanResultHelper(IR.number(1)));
  }

  @Test
  public void testIsAssociativeAndCommutative() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertTrue(NodeUtil.isAssociative(Token.BITXOR));
    assertTrue(NodeUtil.isAssociative(Token.BITAND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertTrue(NodeUtil.isCommutative(Token.BITXOR));
    assertTrue(NodeUtil.isCommutative(Token.BITAND));
    assertFalse(NodeUtil.isCommutative(Token.AND));
  }

  @Test
  public void testAssignmentOps() {
    Node assignAdd = new Node(Token.ASSIGN_ADD, IR.name("x"), IR.number(1));
    assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR, IR.name("x"), IR.number(1))));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR, IR.name("x"), IR.number(1))));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND, IR.name("x"), IR.number(1))));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH, IR.name("x"), IR.number(1))));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH, IR.name("x"), IR.number(1))));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH, IR.name("x"), IR.number(1))));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB, IR.name("x"), IR.number(1))));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL, IR.name("x"), IR.number(1))));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV, IR.name("x"), IR.number(1))));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD, IR.name("x"), IR.number(1))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetOpFromAssignmentOpFail() {
    NodeUtil.getOpFromAssignmentOp(IR.number(1));
  }

  @Test
  public void testContainsFunctionAndReferencesThis() {
    Node block = IR.block(IR.function(IR.name("f"), IR.paramList(), IR.block()));
    assertTrue(NodeUtil.containsFunction(block));

    Node thisNode = IR.thisNode();
    Node expr = IR.exprResult(thisNode);
    assertTrue(NodeUtil.referencesThis(expr));
    assertFalse(NodeUtil.referencesThis(IR.number(1)));
  }

  @Test
  public void testIsGetAndIsVarDeclarationAndGetAssignedValue() {
    Node getprop = IR.getprop(IR.name("a"), IR.string("b"));
    Node getelem = IR.getelem(IR.name("a"), IR.string("b"));
    assertTrue(NodeUtil.isGet(getprop));
    assertTrue(NodeUtil.isGet(getelem));
    assertFalse(NodeUtil.isGet(IR.name("a")));

    Node varName = IR.name("x");
    Node varNode = IR.var(varName, IR.number(1));
    assertTrue(NodeUtil.isVarDeclaration(varName));
    assertEquals(IR.number(1).getDouble(), NodeUtil.getAssignedValue(varName).getDouble(), 0.0);

    Node assignName = IR.name("y");
    IR.assign(assignName, IR.number(2));
    assertEquals(IR.number(2).getDouble(), NodeUtil.getAssignedValue(assignName).getDouble(), 0.0);
    assertNull(NodeUtil.getAssignedValue(IR.name("z")));
  }

  @Test
  public void testIsExprAssignAndExprCall() {
    Node exprAssign = IR.exprResult(IR.assign(IR.name("x"), IR.number(1)));
    assertTrue(NodeUtil.isExprAssign(exprAssign));
    assertFalse(NodeUtil.isExprCall(exprAssign));

    Node exprCall = IR.exprResult(IR.call(IR.name("f")));
    assertTrue(NodeUtil.isExprCall(exprCall));
    assertFalse(NodeUtil.isExprAssign(exprCall));
  }

  @Test
  public void testLoopsAndControlStructures() {
    Node forNode = IR.forNode(IR.var(IR.name("i"), IR.number(0)), IR.lt(IR.name("i"), IR.number(10)), IR.inc(IR.name("i"), false), IR.block());
    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertFalse(NodeUtil.isForIn(forNode));
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(forNode).getType());

    Node whileNode = IR.whileNode(IR.trueNode(), IR.block());
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(whileNode).getType());

    Node doNode = IR.doNode(IR.block(), IR.trueNode());
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(doNode).getType());
    assertNull(NodeUtil.getLoopCodeBlock(IR.number(1)));

    Node forInNode = new Node(Token.FOR, IR.name("k"), IR.name("obj"), IR.block());
    assertTrue(NodeUtil.isForIn(forInNode));

    Node nameInLoop = IR.name("x");
    IR.block(nameInLoop);
    whileNode.getLastChild().addChildToBack(nameInLoop);
    assertTrue(NodeUtil.isWithinLoop(nameInLoop));

    assertTrue(NodeUtil.isControlStructure(IR.ifNode(IR.trueNode(), IR.block())));
    assertTrue(NodeUtil.isControlStructureCodeBlock(whileNode, whileNode.getLastChild()));
  }

  @Test
  public void testGetConditionExpression() {
    Node ifNode = IR.ifNode(IR.trueNode(), IR.block());
    assertEquals(Token.TRUE, NodeUtil.getConditionExpression(ifNode).getType());

    Node whileNode = IR.whileNode(IR.trueNode(), IR.block());
    assertEquals(Token.TRUE, NodeUtil.getConditionExpression(whileNode).getType());

    Node doNode = IR.doNode(IR.block(), IR.falseNode());
    assertEquals(Token.FALSE, NodeUtil.getConditionExpression(doNode).getType());

    Node forNode = IR.forNode(IR.var(IR.name("i")), IR.trueNode(), IR.empty(), IR.block());
    assertEquals(Token.TRUE, NodeUtil.getConditionExpression(forNode).getType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetConditionExpressionFail() {
    NodeUtil.getConditionExpression(IR.number(1));
  }

  @Test
  public void testStatements() {
    Node script = IR.script();
    Node stmt = IR.exprResult(IR.number(1));
    script.addChildToBack(stmt);

    assertTrue(NodeUtil.isStatementBlock(script));
    assertTrue(NodeUtil.isStatementBlock(IR.block()));
    assertTrue(NodeUtil.isStatement(stmt));
    assertTrue(NodeUtil.isStatementParent(script));
    assertFalse(NodeUtil.isStatementParent(IR.name("a")));
  }

  @Test
  public void testSwitchAndTryHelpers() {
    assertTrue(NodeUtil.isSwitchCase(IR.caseNode(IR.number(1), IR.block())));
    assertTrue(NodeUtil.isSwitchCase(IR.defaultCase(IR.block())));

    Node tryNode = IR.tryFinally(IR.block(), IR.block());
    assertTrue(NodeUtil.hasFinally(tryNode));
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, tryNode.getLastChild()));

    Node tryCatch = IR.tryCatch(IR.block(), IR.catchNode(IR.name("e"), IR.block()));
    Node catchContainer = tryCatch.getFirstChild().getNext();
    assertTrue(NodeUtil.isTryCatchNodeContainer(catchContainer));
    assertTrue(NodeUtil.hasCatchHandler(catchContainer));
    assertEquals(catchContainer, NodeUtil.getCatchBlock(tryCatch));

    Node trySimple = new Node(Token.TRY, IR.block(), IR.block());
    NodeUtil.maybeAddFinally(trySimple);
    assertEquals(3, trySimple.getChildCount());
  }

  @Test
  public void testTryMergeBlock() {
    Node script = IR.script();
    Node block = IR.block(IR.exprResult(IR.number(1)));
    script.addChildToBack(block);

    assertTrue(NodeUtil.tryMergeBlock(block));
    assertEquals(1, script.getChildCount());
    assertTrue(script.getFirstChild().isExprResult());
    assertFalse(NodeUtil.tryMergeBlock(IR.block()));
  }

  @Test
  public void testRemoveChild() {
    Node script = IR.script();
    Node stmt1 = IR.exprResult(IR.number(1));
    Node stmt2 = IR.exprResult(IR.number(2));
    script.addChildToBack(stmt1);
    script.addChildToBack(stmt2);
    NodeUtil.removeChild(script, stmt1);
    assertEquals(1, script.getChildCount());

    Node varNode = IR.var(IR.name("a"), IR.name("b"));
    script.addChildToBack(varNode);
    NodeUtil.removeChild(varNode, varNode.getFirstChild());
    assertEquals(1, varNode.getChildCount());

    Node singleVar = IR.var(IR.name("x"));
    script.addChildToBack(singleVar);
    NodeUtil.removeChild(singleVar, singleVar.getFirstChild());
    assertEquals(1, script.getChildCount());

    Node forNode = IR.forNode(IR.var(IR.name("i")), IR.trueNode(), IR.inc(IR.name("i"), false), IR.block());
    Node cond = forNode.getFirstChild().getNext();
    NodeUtil.removeChild(forNode, cond);
    assertTrue(forNode.getFirstChild().getNext().isEmpty());
  }

  @Test
  public void testFunctionCharacteristics() {
    Node fn = IR.function(IR.name("myFunc"), IR.paramList(IR.name("p1")), IR.block());
    assertTrue(NodeUtil.isCallOrNew(IR.call(IR.name("f"))));
    assertTrue(NodeUtil.isCallOrNew(IR.newNode(IR.name("f"))));
    assertEquals(Token.BLOCK, NodeUtil.getFunctionBody(fn).getType());
    assertEquals(Token.PARAM_LIST, NodeUtil.getFunctionParameters(fn).getType());

    IR.script(fn);
    assertTrue(NodeUtil.isFunctionDeclaration(fn));
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));
    assertFalse(NodeUtil.isFunctionExpression(fn));

    Node fnExpr = IR.function(IR.name("bleeding"), IR.paramList(), IR.block());
    IR.assign(IR.name("v"), fnExpr);
    assertTrue(NodeUtil.isFunctionExpression(fnExpr));
    assertTrue(NodeUtil.isBleedingFunctionName(fnExpr.getFirstChild()));
    assertTrue(NodeUtil.isEmptyFunctionExpression(fnExpr));

    Node varArgsFn = IR.function(IR.name(""), IR.paramList(), IR.block(IR.exprResult(IR.name("arguments"))));
    assertTrue(NodeUtil.isVarArgsFunction(varArgsFn));

    Node callMethod = IR.call(IR.getprop(IR.name("obj"), IR.string("call")));
    assertTrue(NodeUtil.isFunctionObjectCall(callMethod));

    Node applyMethod = IR.call(IR.getprop(IR.name("obj"), IR.string("apply")));
    assertTrue(NodeUtil.isFunctionObjectApply(applyMethod));
  }

  @Test
  public void testLValue() {
    Node name = IR.name("x");
    IR.var(name);
    assertTrue(NodeUtil.isLValue(name));
    assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, name.getParent()));

    Node assign = IR.assign(name, IR.number(1));
    assertTrue(NodeUtil.isLValue(name));
    assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, assign));
  }

  @Test
  public void testObjectLitKeys() {
    Node stringKey = IR.stringKey("foo", IR.number(1));
    Node getter = new Node(Token.GETTER_DEF);
    getter.setString("bar");
    Node setter = new Node(Token.SETTER_DEF);
    setter.setString("baz");

    assertTrue(NodeUtil.isObjectLitKey(stringKey));
    assertTrue(NodeUtil.isObjectLitKey(getter));
    assertTrue(NodeUtil.isObjectLitKey(setter));
    assertTrue(NodeUtil.isGetOrSetKey(getter));
    assertTrue(NodeUtil.isGetOrSetKey(setter));
    assertFalse(NodeUtil.isGetOrSetKey(stringKey));

    assertEquals("foo", NodeUtil.getObjectLitKeyName(stringKey));
    assertEquals("bar", NodeUtil.getObjectLitKeyName(getter));
    assertEquals("baz", NodeUtil.getObjectLitKeyName(setter));

    JSTypeRegistry registry = new JSTypeRegistry(new SimpleErrorReporter());
    FunctionType getterFn = registry.createFunctionType(registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE));
    JSType res = NodeUtil.getObjectLitKeyTypeFromValueType(getter, getterFn);
    assertNotNull(res);
  }

  @Test
  public void testOpToStr() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertNull(NodeUtil.opToStr(Token.SCRIPT));
    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(expected = Error.class)
  public void testOpToStrNoFailError() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  @Test
  public void testLatinAndValidNames() {
    assertTrue(NodeUtil.isLatin("abcXYZ123_$-"));
    assertFalse(NodeUtil.isLatin("abc\u0080"));

    assertTrue(NodeUtil.isValidSimpleName("validVar"));
    assertFalse(NodeUtil.isValidSimpleName("class"));
    assertFalse(NodeUtil.isValidSimpleName("var-invalid"));

    assertTrue(NodeUtil.isValidQualifiedName("a.b.c"));
    assertFalse(NodeUtil.isValidQualifiedName(".a.b"));
    assertFalse(NodeUtil.isValidQualifiedName("a.b."));
    assertTrue(NodeUtil.isValidPropertyName("propName"));
  }

  @Test
  public void testRedeclareVarsInsideBranchAndCopyAnnotations() {
    Node script = IR.script();
    Node ifNode = IR.ifNode(IR.trueNode(), IR.block(IR.var(IR.name("innerVar"))));
    script.addChildToBack(ifNode);

    NodeUtil.redeclareVarsInsideBranch(ifNode.getLastChild());
    assertEquals(2, script.getChildCount());
    assertTrue(script.getFirstChild().isVar());
    assertEquals("innerVar", script.getFirstChild().getFirstChild().getString());

    Node src = IR.name("x");
    src.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Node dst = IR.name("x");
    NodeUtil.copyNameAnnotations(src, dst);
    assertTrue(dst.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test
  public void testQualifiedNameNodes() {
    Node qname = NodeUtil.newQualifiedNameNode(convention, "a.b.c");
    assertTrue(qname.isGetProp());
    assertEquals("a.b.c", qname.getQualifiedName());

    Node thisQname = NodeUtil.newQualifiedNameNode(convention, "this.foo");
    assertTrue(thisQname.isGetProp());
    assertTrue(thisQname.getFirstChild().isThis());

    Node decl = NodeUtil.newQualifiedNameNodeDeclaration(convention, "x", IR.number(1), null);
    assertTrue(decl.isVar());

    Node declProp = NodeUtil.newQualifiedNameNodeDeclaration(convention, "a.b", IR.number(1), null);
    assertTrue(declProp.isExprResult());

    Node root = NodeUtil.getRootOfQualifiedName(qname);
    assertTrue(root.isName());
    assertEquals("a", root.getString());

    Node basis = IR.name("basis");
    basis.setSourceEncodedFileName("test.js");
    Node qnameWithDebug = NodeUtil.newQualifiedNameNode(convention, "foo.bar", basis, "orig");
    assertEquals("test.js", qnameWithDebug.getSourceFileName());
    assertEquals("orig", qnameWithDebug.getProp(Node.ORIGINALNAME_PROP));
  }

  @Test
  public void testPrototypeHelpers() {
    Node protoProp = IR.getprop(IR.getprop(IR.name("MyClass"), IR.string("prototype")), IR.string("method"));
    assertTrue(NodeUtil.isPrototypeProperty(protoProp));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoProp).getString());
    assertEquals("method", NodeUtil.getPrototypePropertyName(protoProp));

    Node expr = IR.exprResult(IR.assign(protoProp, IR.number(1)));
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(expr));
  }

  @Test
  public void testNewUndefinedAndVarNode() {
    Node undef = NodeUtil.newUndefinedNode(IR.number(1));
    assertTrue(undef.isVoid());

    Node var = NodeUtil.newVarNode("v", IR.number(42));
    assertTrue(var.isVar());
    assertEquals("v", var.getFirstChild().getString());
  }

  @Test
  public void testPredicatesAndCounts() {
    Node root = IR.block(
        IR.var(IR.name("a")),
        IR.var(IR.name("a")),
        IR.exprResult(IR.name("b"))
    );

    assertEquals(2, NodeUtil.getNameReferenceCount(root, "a"));
    assertTrue(NodeUtil.isNameReferenced(root, "b"));
    assertFalse(NodeUtil.isNameReferenced(root, "c"));
    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(root, Token.VAR, Predicates.<Node>alwaysTrue()));
    assertEquals(2, NodeUtil.getVarsDeclaredInBranch(root).size());
  }

  @Test
  public void testConstantsAndJSDoc() {
    Node constName = IR.name("CONSTANT");
    constName.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(NodeUtil.isConstantName(constName));
    assertTrue(NodeUtil.isConstantByConvention(convention, IR.name("FOO"), IR.var(IR.name("FOO"))));

    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    builder.recordConstancy();
    JSDocInfo info = builder.build(null);

    Node fn = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node assign = IR.assign(IR.name("f"), fn);
    assign.setJSDocInfo(info);
    assertEquals(info, NodeUtil.getFunctionJSDocInfo(fn));
    assertEquals(info, NodeUtil.getBestJSDocInfo(fn));
  }

  @Test
  public void testSourceInfoAndInputId() {
    Node script = IR.script();
    InputId inputId = new InputId("file.js");
    script.setInputId(inputId);
    script.setSourceEncodedFileName("file.js");

    Node child = IR.exprResult(IR.number(1));
    script.addChildToBack(child);

    assertEquals("file.js", NodeUtil.getSourceName(child));
    assertEquals(inputId, NodeUtil.getInputId(child));
  }

  @Test
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.number(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.string("a")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.regexp(IR.string("foo"))));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.add(IR.number(1), IR.number(2))));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.comma(IR.name("g"), IR.number(1))));

    Node inc = IR.inc(IR.name("x"), false);
    assertTrue(NodeUtil.evaluatesToLocalValue(inc));
    assertFalse(NodeUtil.evaluatesToLocalValue(IR.name("global")));
  }

  @Test
  public void testArgumentsHelpers() {
    Node fn = IR.function(IR.name("f"), IR.paramList(IR.name("a"), IR.name("b")), IR.block());
    assertEquals("a", NodeUtil.getArgumentForFunction(fn, 0).getString());
    assertEquals("b", NodeUtil.getArgumentForFunction(fn, 1).getString());
    assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node call = IR.call(IR.name("f"), IR.number(10), IR.number(20));
    assertEquals(10.0, NodeUtil.getArgumentForCallOrNew(call, 0).getDouble(), 0.0);
    assertEquals(20.0, NodeUtil.getArgumentForCallOrNew(call, 1).getDouble(), 0.0);
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));
    assertTrue(NodeUtil.isCallOrNewTarget(call.getFirstChild()));
  }

  @Test
  public void testBestLValue() {
    Node fn = IR.function(IR.name("foo"), IR.paramList(), IR.block());
    IR.script(fn);
    assertEquals(fn.getFirstChild(), NodeUtil.getBestLValue(fn));

    Node assign = IR.assign(IR.name("x"), IR.number(1));
    assertEquals(assign.getFirstChild(), NodeUtil.getBestLValue(assign.getLastChild()));
    assertEquals(assign.getLastChild(), NodeUtil.getRValueOfLValue(assign.getFirstChild()));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = IR.stringKey("k", IR.number(1));
    objLit.addChildToBack(key);
    IR.assign(IR.name("obj"), objLit);
    assertEquals("obj.k", NodeUtil.getBestLValueName(key));
    assertEquals(IR.name("obj").getString(), NodeUtil.getBestLValueOwner(key).getString());
  }

  @Test
  public void testIsExpressionResultUsedAndExecutedExactlyOnce() {
    Node expr = IR.number(1);
    Node parent = IR.exprResult(expr);
    assertFalse(NodeUtil.isExpressionResultUsed(expr));

    Node ifCond = IR.number(1);
    IR.ifNode(ifCond, IR.block());
    assertTrue(NodeUtil.isExpressionResultUsed(ifCond));
    assertTrue(NodeUtil.isExecutedExactlyOnce(ifCond));

    Node script = IR.script();
    Node stmt = IR.exprResult(IR.number(1));
    script.addChildToBack(stmt);
    assertTrue(NodeUtil.isExecutedExactlyOnce(stmt.getFirstChild()));
  }

  @Test
  public void testNumberNodeAndIsNaN() {
    Node nanNode = NodeUtil.numberNode(Double.NaN, null);
    assertTrue(NodeUtil.isNaN(nanNode));

    Node infNode = NodeUtil.numberNode(Double.POSITIVE_INFINITY, null);
    assertEquals("Infinity", infNode.getString());

    Node negInfNode = NodeUtil.numberNode(Double.NEGATIVE_INFINITY, null);
    assertTrue(negInfNode.isNeg());

    Node numNode = NodeUtil.numberNode(42.0, null);
    assertEquals(42.0, numNode.getDouble(), 0.0);

    Node zeroDivZero = IR.divide(IR.number(0), IR.number(0));
    assertTrue(NodeUtil.isNaN(zeroDivZero));
    assertFalse(NodeUtil.isNaN(IR.number(1)));
  }

  @Test
  public void testBooleanNode() {
    assertTrue(NodeUtil.booleanNode(true).isTrue());
    assertTrue(NodeUtil.booleanNode(false).isFalse());
  }

  @Test
  public void testMapMainToCloneAndVerifyScopeChanges() {
    Node main = IR.script(IR.function(IR.name("f"), IR.paramList(), IR.block()));
    Node clone = main.cloneTree();

    Map<Node, Node> map = NodeUtil.mapMainToClone(main, clone);
    assertEquals(2, map.size());
    NodeUtil.verifyScopeChanges(map, main, true, null);
  }
}
