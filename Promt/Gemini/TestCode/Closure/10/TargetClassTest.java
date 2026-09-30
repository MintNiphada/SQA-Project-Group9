package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TargetClassTest {

  private final CodingConvention convention = new DefaultCodingConvention();

  @Test
  public void testGetImpureBooleanValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.string("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.string("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.number(1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.number(0.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.trueNode()));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.falseNode()));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.nullNode()));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.voidNode(IR.number(0))));

    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.OBJECTLIT)));

    Node assign = new Node(Token.ASSIGN, IR.name("x"), IR.number(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    Node comma = new Node(Token.COMMA, IR.number(0), IR.string("abc"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(comma));

    Node not = new Node(Token.NOT, IR.trueNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(not));

    Node and = new Node(Token.AND, IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(and));

    Node or = new Node(Token.OR, IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(or));

    Node hookSame = new Node(Token.HOOK, IR.name("cond"), IR.trueNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, IR.name("cond"), IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDiff));
  }

  @Test
  public void testGetPureBooleanValue() {
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(IR.name("NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.name("Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(IR.name("other")));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.regexp(IR.string("a"))));

    Node pureArray = new Node(Token.ARRAYLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(pureArray));

    Node pureObject = new Node(Token.OBJECTLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(pureObject));

    Node voidPure = IR.voidNode(IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidPure));

    Node voidImpure = IR.voidNode(IR.call(IR.name("foo")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(voidImpure));
  }

  @Test
  public void testGetStringValue() {
    assertEquals("foo", NodeUtil.getStringValue(IR.string("foo")));
    assertEquals("undefined", NodeUtil.getStringValue(IR.name("undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(IR.name("Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(IR.name("NaN")));
    assertNull(NodeUtil.getStringValue(IR.name("other")));

    assertEquals("10", NodeUtil.getStringValue(IR.number(10.0)));
    assertEquals("10.5", NodeUtil.getStringValue(IR.number(10.5)));
    assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
    assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
    assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
    assertEquals("undefined", NodeUtil.getStringValue(IR.voidNode(IR.number(0))));
    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));

    Node notTrue = new Node(Token.NOT, IR.trueNode());
    assertEquals("false", NodeUtil.getStringValue(notTrue));
    Node notFalse = new Node(Token.NOT, IR.falseNode());
    assertEquals("true", NodeUtil.getStringValue(notFalse));

    Node arr = new Node(Token.ARRAYLIT, IR.number(1), IR.nullNode(), IR.string("a"));
    assertEquals("1,,a", NodeUtil.getStringValue(arr));
  }

  @Test
  public void testGetNumberValue() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(IR.trueNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.falseNode()));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(IR.nullNode()));
    assertEquals(Double.valueOf(42.0), NodeUtil.getNumberValue(IR.number(42.0)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.voidNode(IR.number(0)))));
    assertNull(NodeUtil.getNumberValue(IR.voidNode(IR.call(IR.name("f")))));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.name("NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(IR.name("Infinity")));
    assertNull(NodeUtil.getNumberValue(IR.name("foo")));

    Node negInf = IR.neg(IR.name("Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));
    assertNull(NodeUtil.getNumberValue(IR.neg(IR.name("other"))));

    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NOT, IR.trueNode())));
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.NOT, IR.falseNode())));

    assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(IR.string("123")));
  }

  @Test
  public void testGetStringNumberValue() {
    assertNull(NodeUtil.getStringNumberValue("hello\u000bworld"));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0xff"));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0XFF"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));
    assertNull(NodeUtil.getStringNumberValue("+0x10"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));
    assertEquals(Double.valueOf(12.34), NodeUtil.getStringNumberValue(" 12.34 "));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not_a_number")));
  }

  @Test
  public void testIsStrWhiteSpaceChar() {
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\n'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\r'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\t'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u00A0'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u000C'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2028'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2029'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\uFEFF'));
    assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('A'));
  }

  @Test
  public void testFunctionNames() {
    Node fn1 = IR.function(IR.name("fnName"), IR.paramList(), IR.block());
    assertEquals("fnName", NodeUtil.getFunctionName(fn1));
    assertEquals("fnName", NodeUtil.getNearestFunctionName(fn1));

    Node var = IR.var(IR.name("vName"), IR.function(IR.name(""), IR.paramList(), IR.block()));
    Node fn2 = var.getFirstChild().getFirstChild();
    assertEquals("vName", NodeUtil.getFunctionName(fn2));
    assertEquals("vName", NodeUtil.getNearestFunctionName(fn2));

    Node assign = IR.assign(IR.getprop(IR.name("a"), IR.string("b")), IR.function(IR.name(""), IR.paramList(), IR.block()));
    Node fn3 = assign.getLastChild();
    assertEquals("a.b", NodeUtil.getFunctionName(fn3));
    assertEquals("a.b", NodeUtil.getNearestFunctionName(fn3));

    Node key = IR.stringKey("propKey", IR.function(IR.name(""), IR.paramList(), IR.block()));
    Node fn4 = key.getFirstChild();
    assertNull(NodeUtil.getNearestFunctionName(IR.name("notFn")));
    assertEquals("propKey", NodeUtil.getNearestFunctionName(fn4));
  }

  @Test
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(IR.string("str")));
    assertTrue(NodeUtil.isImmutableValue(IR.number(1)));
    assertTrue(NodeUtil.isImmutableValue(IR.nullNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.trueNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.falseNode()));
    assertTrue(NodeUtil.isImmutableValue(IR.name("undefined")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("Infinity")));
    assertTrue(NodeUtil.isImmutableValue(IR.name("NaN")));
    assertFalse(NodeUtil.isImmutableValue(IR.name("x")));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, IR.trueNode())));
    assertTrue(NodeUtil.isImmutableValue(IR.voidNode(IR.number(0))));
    assertTrue(NodeUtil.isImmutableValue(IR.neg(IR.number(5))));
  }

  @Test
  public void testSymmetricAndRelationalOps() {
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.EQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.NE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHEQ)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.SHNE)));
    assertTrue(NodeUtil.isSymmetricOperation(new Node(Token.MUL)));
    assertFalse(NodeUtil.isSymmetricOperation(new Node(Token.SUB)));

    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.GE)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LT)));
    assertTrue(NodeUtil.isRelationalOperation(new Node(Token.LE)));
    assertFalse(NodeUtil.isRelationalOperation(new Node(Token.EQ)));

    assertEquals(Token.LT, NodeUtil.getInverseOperator(Token.GT));
    assertEquals(Token.GT, NodeUtil.getInverseOperator(Token.LT));
    assertEquals(Token.LE, NodeUtil.getInverseOperator(Token.GE));
    assertEquals(Token.GE, NodeUtil.getInverseOperator(Token.LE));
    assertEquals(Token.ERROR, NodeUtil.getInverseOperator(Token.EQ));
  }

  @Test
  public void testIsLiteralValue() {
    assertTrue(NodeUtil.isLiteralValue(IR.number(1), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.ARRAYLIT, IR.number(1)), false));
    assertFalse(NodeUtil.isLiteralValue(new Node(Token.ARRAYLIT, IR.name("x")), false));
    assertTrue(NodeUtil.isLiteralValue(IR.regexp(IR.string("abc")), false));

    Node objLit = new Node(Token.OBJECTLIT, IR.stringKey("k", IR.number(1)));
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fnExpr = IR.function(IR.name(""), IR.paramList(), IR.block());
    assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<String>(Collections.singletonList("DEF"));
    assertTrue(NodeUtil.isValidDefineValue(IR.number(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.string("s"), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.trueNode(), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.falseNode(), defines));
    assertTrue(NodeUtil.isValidDefineValue(IR.name("DEF"), defines));
    assertFalse(NodeUtil.isValidDefineValue(IR.name("OTHER"), defines));

    Node add = IR.add(IR.number(1), IR.name("DEF"));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));
    Node not = new Node(Token.NOT, IR.trueNode());
    assertTrue(NodeUtil.isValidDefineValue(not, defines));
    assertFalse(NodeUtil.isValidDefineValue(IR.nullNode(), defines));
  }

  @Test
  public void testIsEmptyBlockAndSimpleOperator() {
    assertTrue(NodeUtil.isEmptyBlock(IR.block()));
    assertTrue(NodeUtil.isEmptyBlock(IR.block(IR.empty())));
    assertFalse(NodeUtil.isEmptyBlock(IR.block(IR.exprResult(IR.number(1)))));
    assertFalse(NodeUtil.isEmptyBlock(IR.number(1)));

    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.BITAND));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));

    assertTrue(NodeUtil.isSimpleOperator(IR.add(IR.number(1), IR.number(2))));
  }

  @Test
  public void testSideEffectsAndMutableState() {
    Node num = IR.number(1);
    assertFalse(NodeUtil.mayHaveSideEffects(num));
    assertFalse(NodeUtil.mayEffectMutableState(num));

    Node throwNode = new Node(Token.THROW, IR.name("e"));
    assertTrue(NodeUtil.mayHaveSideEffects(throwNode));

    Node newArray = new Node(Token.ARRAYLIT);
    assertFalse(NodeUtil.mayHaveSideEffects(newArray));
    assertTrue(NodeUtil.mayEffectMutableState(newArray));

    Node newObject = new Node(Token.OBJECTLIT);
    assertFalse(NodeUtil.mayHaveSideEffects(newObject));
    assertTrue(NodeUtil.mayEffectMutableState(newObject));

    Node varNode = IR.var(IR.name("x"), IR.number(1));
    assertTrue(NodeUtil.mayHaveSideEffects(varNode));

    Node newCall = new Node(Token.NEW, IR.name("Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newCall));

    Node customNew = new Node(Token.NEW, IR.name("CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(customNew));

    Node callBuiltin = IR.call(IR.name("String"), IR.number(1));
    assertFalse(NodeUtil.functionCallHasSideEffects(callBuiltin));

    Node callCustom = IR.call(IR.name("customFn"));
    assertTrue(NodeUtil.functionCallHasSideEffects(callCustom));

    Node callMathFloor = IR.call(IR.getprop(IR.name("Math"), IR.string("floor")), IR.number(1.5));
    assertFalse(NodeUtil.functionCallHasSideEffects(callMathFloor));
  }

  @Test
  public void testPrecedence() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN_ADD));
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
    assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  @Test(expected = Error.class)
  public void testPrecedenceUnknown() {
    NodeUtil.precedence(Token.ERROR);
  }

  @Test
  public void testNumericBooleanStringPredicates() {
    assertTrue(NodeUtil.isNumericResult(IR.number(5)));
    assertTrue(NodeUtil.isNumericResult(IR.add(IR.number(1), IR.number(2))));
    assertTrue(NodeUtil.isNumericResult(IR.name("NaN")));
    assertTrue(NodeUtil.isNumericResult(IR.name("Infinity")));
    assertFalse(NodeUtil.isNumericResult(IR.string("str")));

    assertTrue(NodeUtil.isBooleanResult(IR.trueNode()));
    assertTrue(NodeUtil.isBooleanResult(IR.falseNode()));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, IR.number(1), IR.number(1))));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, IR.number(1))));
    assertFalse(NodeUtil.isBooleanResult(IR.number(5)));

    assertTrue(NodeUtil.mayBeString(IR.string("abc")));
    assertFalse(NodeUtil.mayBeString(IR.number(123)));
    assertFalse(NodeUtil.mayBeString(IR.trueNode()));
    assertFalse(NodeUtil.mayBeString(IR.nullNode()));
  }

  @Test
  public void testAssociativeCommutative() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.DIV));
  }

  @Test
  public void testAssignmentOps() {
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));

    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetOpFromAssignmentOpInvalid() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test
  public void testAstQueryUtils() {
    Node exprAssign = IR.exprResult(IR.assign(IR.name("a"), IR.number(1)));
    assertTrue(NodeUtil.isExprAssign(exprAssign));
    assertFalse(NodeUtil.isExprCall(exprAssign));

    Node exprCall = IR.exprResult(IR.call(IR.name("a")));
    assertTrue(NodeUtil.isExprCall(exprCall));
    assertFalse(NodeUtil.isExprAssign(exprCall));

    Node forNode = new Node(Token.FOR, IR.name("x"), IR.name("arr"), IR.block());
    assertTrue(NodeUtil.isForIn(forNode));
    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertEquals(forNode.getLastChild(), NodeUtil.getLoopCodeBlock(forNode));

    Node whileNode = new Node(Token.WHILE, IR.trueNode(), IR.block());
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertEquals(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));

    Node doNode = new Node(Token.DO, IR.block(), IR.trueNode());
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertEquals(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));
    assertNull(NodeUtil.getLoopCodeBlock(IR.number(1)));

    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    assertFalse(NodeUtil.isControlStructure(IR.number(1)));
  }

  @Test
  public void testConditionExpression() {
    Node ifNode = IR.ifNode(IR.trueNode(), IR.block());
    assertEquals(ifNode.getFirstChild(), NodeUtil.getConditionExpression(ifNode));

    Node whileNode = new Node(Token.WHILE, IR.falseNode(), IR.block());
    assertEquals(whileNode.getFirstChild(), NodeUtil.getConditionExpression(whileNode));

    Node doNode = new Node(Token.DO, IR.block(), IR.trueNode());
    assertEquals(doNode.getLastChild(), NodeUtil.getConditionExpression(doNode));

    Node forNode4 = new Node(Token.FOR, IR.var(IR.name("i")), IR.trueNode(), IR.inc(IR.name("i"), false), IR.block());
    assertEquals(forNode4.getFirstChild().getNext(), NodeUtil.getConditionExpression(forNode4));

    Node forIn = new Node(Token.FOR, IR.name("x"), IR.name("obj"), IR.block());
    assertNull(NodeUtil.getConditionExpression(forIn));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryNode = new Node(Token.TRY, IR.block(), IR.block(new Node(Token.CATCH, IR.name("e"), IR.block())), IR.block());
    assertTrue(NodeUtil.hasFinally(tryNode));
    assertEquals(tryNode.getFirstChild().getNext(), NodeUtil.getCatchBlock(tryNode));
    assertTrue(NodeUtil.hasCatchHandler(NodeUtil.getCatchBlock(tryNode)));

    Node tryNoFinally = new Node(Token.TRY, IR.block(), IR.block(new Node(Token.CATCH, IR.name("e"), IR.block())));
    assertFalse(NodeUtil.hasFinally(tryNoFinally));
    NodeUtil.maybeAddFinally(tryNoFinally);
    assertTrue(NodeUtil.hasFinally(tryNoFinally));
  }

  @Test
  public void testFunctions() {
    Node fnDecl = IR.function(IR.name("f"), IR.paramList(IR.name("p1")), IR.block());
    IR.script(fnDecl);
    assertTrue(NodeUtil.isFunctionDeclaration(fnDecl));
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(fnDecl));
    assertFalse(NodeUtil.isFunctionExpression(fnDecl));
    assertEquals(fnDecl.getLastChild(), NodeUtil.getFunctionBody(fnDecl));
    assertEquals(fnDecl.getFirstChild().getNext(), NodeUtil.getFunctionParameters(fnDecl));

    Node fnExpr = IR.function(IR.name(""), IR.paramList(), IR.block());
    IR.exprResult(fnExpr);
    assertTrue(NodeUtil.isFunctionExpression(fnExpr));
    assertTrue(NodeUtil.isEmptyFunctionExpression(fnExpr));

    Node bleedingName = IR.name("bleed");
    Node bleedingFn = IR.function(bleedingName, IR.paramList(), IR.block());
    IR.exprResult(bleedingFn);
    assertTrue(NodeUtil.isBleedingFunctionName(bleedingName));

    Node varArgsFn = IR.function(IR.name("vargs"), IR.paramList(), IR.block(IR.exprResult(IR.name("arguments"))));
    assertTrue(NodeUtil.isVarArgsFunction(varArgsFn));
  }

  @Test
  public void testObjectCallsAndLValues() {
    Node callObj = IR.call(IR.getprop(IR.name("obj"), IR.string("call")), IR.name("thisArg"));
    assertTrue(NodeUtil.isFunctionObjectCall(callObj));
    assertFalse(NodeUtil.isFunctionObjectApply(callObj));

    Node applyObj = IR.call(IR.getprop(IR.name("obj"), IR.string("apply")), IR.name("thisArg"));
    assertTrue(NodeUtil.isFunctionObjectApply(applyObj));
    assertFalse(NodeUtil.isFunctionObjectCall(applyObj));

    Node name = IR.name("target");
    Node assign = IR.assign(name, IR.number(10));
    assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, assign));
    assertTrue(NodeUtil.isLValue(name));
    assertEquals(assign.getLastChild(), NodeUtil.getAssignedValue(name));
  }

  @Test
  public void testObjectLitKeys() {
    Node strKey = IR.stringKey("myKey", IR.number(1));
    Node getDef = new Node(Token.GETTER_DEF);
    getDef.setString("getterKey");
    Node setDef = new Node(Token.SETTER_DEF);
    setDef.setString("setterKey");

    assertTrue(NodeUtil.isObjectLitKey(strKey, null));
    assertTrue(NodeUtil.isObjectLitKey(getDef, null));
    assertTrue(NodeUtil.isObjectLitKey(setDef, null));
    assertFalse(NodeUtil.isObjectLitKey(IR.number(1), null));

    assertEquals("myKey", NodeUtil.getObjectLitKeyName(strKey));
    assertEquals("getterKey", NodeUtil.getObjectLitKeyName(getDef));
    assertEquals("setterKey", NodeUtil.getObjectLitKeyName(setDef));

    assertTrue(NodeUtil.isGetOrSetKey(getDef));
    assertTrue(NodeUtil.isGetOrSetKey(setDef));
    assertFalse(NodeUtil.isGetOrSetKey(strKey));
  }

  @Test
  public void testOpToStr() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("*", NodeUtil.opToStr(Token.MUL));
    assertEquals("/", NodeUtil.opToStr(Token.DIV));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    assertEquals("==", NodeUtil.opToStr(Token.EQ));
    assertEquals("!=", NodeUtil.opToStr(Token.NE));
    assertEquals("!", NodeUtil.opToStr(Token.NOT));
    assertEquals("=", NodeUtil.opToStr(Token.ASSIGN));
    assertNull(NodeUtil.opToStr(Token.NAME));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(expected = Error.class)
  public void testOpToStrNoFailError() {
    NodeUtil.opToStrNoFail(Token.NAME);
  }

  @Test
  public void testQualifiedNames() {
    assertTrue(NodeUtil.isLatin("Hello_World123"));
    assertFalse(NodeUtil.isLatin("Hello\u0100"));

    assertTrue(NodeUtil.isValidSimpleName("validVar"));
    assertFalse(NodeUtil.isValidSimpleName("class")); // reserved keyword
    assertFalse(NodeUtil.isValidSimpleName("123bad"));

    assertTrue(NodeUtil.isValidQualifiedName("a.b.c"));
    assertFalse(NodeUtil.isValidQualifiedName(".a.b"));
    assertFalse(NodeUtil.isValidQualifiedName("a.b."));
    assertFalse(NodeUtil.isValidQualifiedName("a..b"));

    Node qname = NodeUtil.newQualifiedNameNode(convention, "a.b.c");
    assertTrue(qname.isGetProp());
    assertEquals("a", NodeUtil.getRootOfQualifiedName(qname).getString());

    Node simpleQname = NodeUtil.newQualifiedNameNode(convention, "simple");
    assertTrue(simpleQname.isName());
    assertEquals("simple", NodeUtil.getRootOfQualifiedName(simpleQname).getString());
  }

  @Test
  public void testPrototypeHelpers() {
    Node protoProp = IR.getprop(IR.getprop(IR.name("MyClass"), IR.string("prototype")), IR.string("myMethod"));
    Node assign = IR.exprResult(IR.assign(protoProp, IR.function(IR.name(""), IR.paramList(), IR.block())));
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(assign));
    assertTrue(NodeUtil.isPrototypeProperty(protoProp));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoProp).getString());
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(protoProp));
  }

  @Test
  public void testNewNodesAndTraversal() {
    Node undef = NodeUtil.newUndefinedNode(null);
    assertTrue(undef.isVoid());

    Node varNode = NodeUtil.newVarNode("myVar", IR.number(10));
    assertTrue(varNode.isVar());
    assertEquals("myVar", varNode.getFirstChild().getString());

    Node script = IR.script(varNode, IR.exprResult(IR.call(IR.name("myVar"))));
    Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(script);
    assertEquals(1, vars.size());

    assertTrue(NodeUtil.containsType(script, Token.VAR));
    assertTrue(NodeUtil.containsType(script, Token.CALL));
    assertFalse(NodeUtil.containsType(script, Token.FOR));

    assertTrue(NodeUtil.isNameReferenced(script, "myVar"));
    assertFalse(NodeUtil.isNameReferenced(script, "nonExistent"));
    assertEquals(2, NodeUtil.getNameReferenceCount(script, "myVar"));
    assertEquals(1, NodeUtil.getNodeTypeReferenceCount(script, Token.VAR, Predicates.<Node>alwaysTrue()));
  }

  @Test
  public void testTryMergeBlock() {
    Node inner = IR.block(IR.exprResult(IR.number(1)));
    Node parent = IR.block(inner);
    assertTrue(NodeUtil.tryMergeBlock(inner));
    assertEquals(1, parent.getChildCount());
    assertTrue(parent.getFirstChild().isExprResult());
  }

  @Test
  public void testRemoveChild() {
    Node stmt1 = IR.exprResult(IR.number(1));
    Node stmt2 = IR.exprResult(IR.number(2));
    Node block = IR.block(stmt1, stmt2);
    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertEquals(stmt2, block.getFirstChild());
  }

  @Test
  public void testBooleanAndNumberNodes() {
    Node t = NodeUtil.booleanNode(true);
    assertTrue(t.isTrue());
    Node f = NodeUtil.booleanNode(false);
    assertTrue(f.isFalse());

    Node num = NodeUtil.numberNode(12.5, null);
    assertTrue(num.isNumber());
    assertEquals(12.5, num.getDouble(), 0.0);

    Node nan = NodeUtil.numberNode(Double.NaN, null);
    assertTrue(nan.isName());
    assertEquals("NaN", nan.getString());

    Node posInf = NodeUtil.numberNode(Double.POSITIVE_INFINITY, null);
    assertTrue(posInf.isName());
    assertEquals("Infinity", posInf.getString());

    Node negInf = NodeUtil.numberNode(Double.NEGATIVE_INFINITY, null);
    assertTrue(negInf.isNeg());
    assertEquals("Infinity", negInf.getFirstChild().getString());
  }

  @Test
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.number(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.string("hello")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(IR.function(IR.name(""), IR.paramList(), IR.block())));

    Node comma = new Node(Token.COMMA, IR.name("x"), IR.number(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(comma));

    Node hook = new Node(Token.HOOK, IR.name("c"), IR.number(1), IR.number(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(hook));
  }

  @Test
  public void testIsExecutedExactlyOnce() {
    Node target = IR.exprResult(IR.number(1));
    Node script = IR.script(target);
    assertTrue(NodeUtil.isExecutedExactlyOnce(target));

    Node whileBody = IR.block(target);
    Node whileNode = new Node(Token.WHILE, IR.trueNode(), whileBody);
    IR.script(whileNode);
    assertFalse(NodeUtil.isExecutedExactlyOnce(target));
  }

  @Test
  public void testIsExpressionResultUsed() {
    Node expr = IR.number(1);
    Node exprRes = IR.exprResult(expr);
    assertFalse(NodeUtil.isExpressionResultUsed(expr));

    Node assign = IR.assign(IR.name("x"), expr);
    assertTrue(NodeUtil.isExpressionResultUsed(expr));
  }
}