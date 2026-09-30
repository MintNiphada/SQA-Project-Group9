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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeUtilTest {

  private final CodingConvention convention = new DefaultCodingConvention();

  @Test
  public void testGetBooleanValue() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(-1.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.REGEXP, Node.newString("foo"))));

    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "someVar")));

    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2))));
  }

  @Test
  public void testGetExpressionBooleanValue() {
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assign));

    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newString(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(comma));

    Node not = new Node(Token.NOT, Node.newNumber(0));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(not));

    Node and = new Node(Token.AND, Node.newNumber(1), Node.newNumber(2));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(and));

    Node or = new Node(Token.OR, Node.newNumber(0), Node.newString(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(or));

    Node hookSame = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(1), Node.newNumber(2));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(1), Node.newNumber(0));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiff));
  }

  @Test
  public void testGetStringValue() {
    assertEquals("foo", NodeUtil.getStringValue(Node.newString("foo")));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "other")));

    assertEquals("1", NodeUtil.getStringValue(Node.newNumber(1.0)));
    assertEquals("1.5", NodeUtil.getStringValue(Node.newNumber(1.5)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));
    assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testGetNumberValue() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(Node.newNumber(42.5)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(new Node(Token.VOID, Node.newNumber(0)))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "foo")));
    assertNull(NodeUtil.getNumberValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testGetFunctionNameAndNearest() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);
    assertEquals("foo", NodeUtil.getFunctionName(fn));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node varName = Node.newString(Token.NAME, "v");
    varName.addChildToBack(anonFn);
    Node var = new Node(Token.VAR, varName);
    assertEquals("v", NodeUtil.getFunctionName(anonFn));

    Node namedFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "inner"), new Node(Token.LP), new Node(Token.BLOCK));
    Node assignLhs = NodeUtil.newQualifiedNameNode(convention, "a.b", -1, -1);
    Node assign = new Node(Token.ASSIGN, assignLhs, namedFn);
    assertEquals("a.b", NodeUtil.getFunctionName(namedFn));

    Node anonFn2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.EXPR_RESULT, anonFn2);
    assertNull(NodeUtil.getFunctionName(anonFn2));

    Node keyNode = Node.newString("key");
    keyNode.addChildToBack(anonFn2);
    assertEquals("key", NodeUtil.getNearestFunctionName(anonFn2));
  }

  @Test
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("a")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "other")));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testIsLiteralValue() {
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arr, false));

    Node arrNonLit = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(arrNonLit, false));

    Node obj = new Node(Token.OBJECTLIT);
    Node objKey = Node.newString("k");
    objKey.addChildToBack(Node.newNumber(1));
    obj.addChildToBack(objKey);
    assertTrue(NodeUtil.isLiteralValue(obj, false));

    Node objKeyNonLit = Node.newString("k2");
    objKeyNonLit.addChildToBack(Node.newString(Token.NAME, "y"));
    obj.addChildToBack(objKeyNonLit);
    assertFalse(NodeUtil.isLiteralValue(obj, false));

    Node fnExp = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.ASSIGN, Node.newString(Token.NAME, "f"), fnExp);
    assertTrue(NodeUtil.isLiteralValue(fnExp, true));
    assertFalse(NodeUtil.isLiteralValue(fnExp, false));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = Sets.newHashSet("DEF1", "a.b.DEF2");
    assertTrue(NodeUtil.isValidDefineValue(Node.newString("s"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(10), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(not, defines));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF1"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNDEF"), defines));

    Node getprop = NodeUtil.newQualifiedNameNode(convention, "a.b.DEF2", -1, -1);
    assertTrue(NodeUtil.isValidDefineValue(getprop, defines));

    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.OBJECTLIT), defines));
  }

  @Test
  public void testIsEmptyBlock() {
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.EXPR_RESULT)));
    assertTrue(NodeUtil.isEmptyBlock(new Node(Token.BLOCK)));
    assertTrue(NodeUtil.isEmptyBlock(new Node(Token.BLOCK, new Node(Token.EMPTY))));
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.BLOCK, new Node(Token.EXPR_RESULT))));
  }

  @Test
  public void testIsSimpleOperator() {
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.BITAND)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETELEM)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.TYPEOF)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.POS)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.URSH)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.isSimpleOperator(new Node(Token.CALL)));
  }

  @Test
  public void testNewExpr() {
    Node child = Node.newNumber(42);
    Node expr = NodeUtil.newExpr(child);
    assertEquals(Token.EXPR_RESULT, expr.getType());
    assertSame(child, expr.getFirstChild());
  }

  @Test
  public void testSideEffectsAndMutableState() {
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node objLit = new Node(Token.OBJECTLIT);
    assertTrue(NodeUtil.mayEffectMutableState(objLit));
    assertFalse(NodeUtil.mayHaveSideEffects(objLit));

    Node arrLit = new Node(Token.ARRAYLIT);
    assertTrue(NodeUtil.mayEffectMutableState(arrLit));
    assertFalse(NodeUtil.mayHaveSideEffects(arrLit));

    Node varNoInit = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
    assertTrue(NodeUtil.mayHaveSideEffects(varNoInit));

    Node nameWithoutChild = Node.newString(Token.NAME, "a");
    assertFalse(NodeUtil.mayHaveSideEffects(nameWithoutChild));

    Node fnDecl = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.SCRIPT, fnDecl);
    assertTrue(NodeUtil.mayHaveSideEffects(fnDecl));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    Node callBuiltin = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callBuiltin));

    Node mathProp = NodeUtil.newQualifiedNameNode(convention, "Math.sin", -1, -1);
    Node callMath = new Node(Token.CALL, mathProp, Node.newNumber(0));
    assertFalse(NodeUtil.functionCallHasSideEffects(callMath));

    Node objToString = NodeUtil.newQualifiedNameNode(convention, "x.toString", -1, -1);
    Node callToString = new Node(Token.CALL, objToString);
    assertFalse(NodeUtil.functionCallHasSideEffects(callToString));
  }

  @Test
  public void testConstructorCallHasSideEffectsException() {
    try {
      NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testFunctionCallHasSideEffectsException() {
    try {
      NodeUtil.functionCallHasSideEffects(new Node(Token.NEW));
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testCallHasLocalResult() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    assertFalse(NodeUtil.callHasLocalResult(call));
    call.setSideEffectFlags(Node.FLAG_LOCAL_RESULTS);
    assertTrue(NodeUtil.callHasLocalResult(call));
  }

  @Test
  public void testNodeTypeMayHaveSideEffects() {
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DELPROP)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.INC)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DEC)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.ASSIGN)));
    assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(Node.newNumber(1)));
  }

  @Test
  public void testCanBeSideEffected() {
    Node nameNode = Node.newString(Token.NAME, "foo");
    assertTrue(NodeUtil.canBeSideEffected(nameNode));
    assertFalse(NodeUtil.canBeSideEffected(nameNode, Sets.newHashSet("foo")));

    nameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertFalse(NodeUtil.canBeSideEffected(nameNode));

    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    assertTrue(NodeUtil.canBeSideEffected(callNode));

    Node getProp = NodeUtil.newQualifiedNameNode(convention, "a.b", -1, -1);
    assertTrue(NodeUtil.canBeSideEffected(getProp));
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
    assertEquals(15, NodeUtil.precedence(Token.NAME));

    try {
      NodeUtil.precedence(-999);
      fail("Expected Error for unknown token");
    } catch (Error expected) {
    }
  }

  @Test
  public void testAssociativeAndCommutative() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertTrue(NodeUtil.isAssociative(Token.BITAND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertTrue(NodeUtil.isCommutative(Token.BITAND));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.AND));
  }

  @Test
  public void testIsAssignmentOpAndGetOp() {
    int[] assignTokens = {
      Token.ASSIGN, Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_BITAND,
      Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH, Token.ASSIGN_ADD,
      Token.ASSIGN_SUB, Token.ASSIGN_MUL, Token.ASSIGN_DIV, Token.ASSIGN_MOD
    };
    for (int t : assignTokens) {
      assertTrue(NodeUtil.isAssignmentOp(new Node(t)));
    }
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));

    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH)));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH)));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH)));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));

    try {
      NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN));
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testNodeChecks() {
    assertTrue(NodeUtil.isExpressionNode(new Node(Token.EXPR_RESULT)));
    assertTrue(NodeUtil.isGet(new Node(Token.GETPROP)));
    assertTrue(NodeUtil.isGet(new Node(Token.GETELEM)));
    assertTrue(NodeUtil.isGetProp(new Node(Token.GETPROP)));
    assertFalse(NodeUtil.isGetProp(new Node(Token.GETELEM)));
    assertTrue(NodeUtil.isName(Node.newString(Token.NAME, "a")));
    assertTrue(NodeUtil.isNew(new Node(Token.NEW)));
    assertTrue(NodeUtil.isVar(new Node(Token.VAR)));
    assertTrue(NodeUtil.isString(Node.newString("s")));
    assertTrue(NodeUtil.isAssign(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isCall(new Node(Token.CALL)));
    assertTrue(NodeUtil.isCallOrNew(new Node(Token.CALL)));
    assertTrue(NodeUtil.isCallOrNew(new Node(Token.NEW)));
    assertTrue(NodeUtil.isThis(new Node(Token.THIS)));

    Node varNode = new Node(Token.VAR);
    Node nameInVar = Node.newString(Token.NAME, "x");
    varNode.addChildToBack(nameInVar);
    assertTrue(NodeUtil.isVarDeclaration(nameInVar));

    Node exprAssign = new Node(Token.EXPR_RESULT, new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), Node.newNumber(1)));
    assertTrue(NodeUtil.isExprAssign(exprAssign));

    Node exprCall = new Node(Token.EXPR_RESULT, new Node(Token.CALL, Node.newString(Token.NAME, "f")));
    assertTrue(NodeUtil.isExprCall(exprCall));
  }

  @Test
  public void testGetAssignedValue() {
    Node varName = Node.newString(Token.NAME, "x");
    Node varVal = Node.newNumber(10);
    varName.addChildToBack(varVal);
    new Node(Token.VAR, varName);
    assertSame(varVal, NodeUtil.getAssignedValue(varName));

    Node assignName = Node.newString(Token.NAME, "y");
    Node assignVal = Node.newNumber(20);
    new Node(Token.ASSIGN, assignName, assignVal);
    assertSame(assignVal, NodeUtil.getAssignedValue(assignName));

    Node other = Node.newString(Token.NAME, "z");
    new Node(Token.EXPR_RESULT, other);
    assertNull(NodeUtil.getAssignedValue(other));
  }

  @Test
  public void testLoopAndControlStructures() {
    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forIn));
    assertTrue(NodeUtil.isLoopStructure(forIn));

    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(whileNode).getType());

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.TRUE));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(doNode).getType());
    assertNull(NodeUtil.getLoopCodeBlock(new Node(Token.EXPR_RESULT)));

    Node innerNode = Node.newString(Token.NAME, "inside");
    whileNode.getLastChild().addChildToBack(innerNode);
    assertTrue(NodeUtil.isWithinLoop(innerNode));

    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.WITH)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.CASE)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.DEFAULT)));

    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "c"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getLastChild()));
    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getFirstChild()));

    assertEquals(Token.NAME, NodeUtil.getConditionExpression(ifNode).getType());
    assertEquals(Token.TRUE, NodeUtil.getConditionExpression(whileNode).getType());
    assertEquals(Token.TRUE, NodeUtil.getConditionExpression(doNode).getType());
  }

  @Test
  public void testStatementsAndBlocks() {
    assertTrue(NodeUtil.isStatementBlock(new Node(Token.SCRIPT)));
    assertTrue(NodeUtil.isStatementBlock(new Node(Token.BLOCK)));

    Node script = new Node(Token.SCRIPT);
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    script.addChildToBack(expr);
    assertTrue(NodeUtil.isStatement(expr));

    assertTrue(NodeUtil.isSwitchCase(new Node(Token.CASE)));
    assertTrue(NodeUtil.isSwitchCase(new Node(Token.DEFAULT)));
    assertFalse(NodeUtil.isSwitchCase(new Node(Token.SWITCH)));

    assertTrue(NodeUtil.isReferenceName(Node.newString(Token.NAME, "foo")));
    assertFalse(NodeUtil.isReferenceName(Node.newString(Token.NAME, "")));
    assertTrue(NodeUtil.isLabelName(new Node(Token.LABEL_NAME)));
    assertFalse(NodeUtil.isLabelName(Node.newString(Token.NAME, "foo")));
  }

  @Test
  public void testRemoveChildAndTryMergeBlock() {
    Node block = new Node(Token.BLOCK);
    Node s1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node s2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    block.addChildToBack(s1);
    block.addChildToBack(s2);

    NodeUtil.removeChild(block, s1);
    assertEquals(1, block.getChildCount());

    Node varMulti = new Node(Token.VAR);
    Node v1 = Node.newString(Token.NAME, "a");
    Node v2 = Node.newString(Token.NAME, "b");
    varMulti.addChildToBack(v1);
    varMulti.addChildToBack(v2);
    block.addChildToBack(varMulti);
    NodeUtil.removeChild(varMulti, v1);
    assertEquals(1, varMulti.getChildCount());

    Node parentBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK);
    Node innerChild = new Node(Token.EXPR_RESULT, Node.newNumber(42));
    innerBlock.addChildToBack(innerChild);
    parentBlock.addChildToBack(innerBlock);

    assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    assertEquals(1, parentBlock.getChildCount());
    assertSame(innerChild, parentBlock.getFirstChild());
  }

  @Test
  public void testFunctionAnalysis() {
    Node fn = NodeUtil.newFunctionNode("myFunc", Arrays.asList(Node.newString(Token.NAME, "p1")), new Node(Token.BLOCK), 1, 0);
    assertTrue(NodeUtil.isFunction(fn));
    assertSame(fn.getLastChild(), NodeUtil.getFunctionBody(fn));
    assertEquals(1, NodeUtil.getFnParameters(fn).getChildCount());
    assertSame(NodeUtil.getFnParameters(fn).getFirstChild(), NodeUtil.getArgumentForFunction(fn, 0));
    assertNull(NodeUtil.getArgumentForFunction(fn, 1));

    Node script = new Node(Token.SCRIPT, fn);
    assertTrue(NodeUtil.isFunctionDeclaration(fn));
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "fn"), anonFn);
    assertTrue(NodeUtil.isFunctionExpression(anonFn));
    assertTrue(NodeUtil.isEmptyFunctionExpression(anonFn));

    Node call = NodeUtil.newCallNode(NodeUtil.newQualifiedNameNode(convention, "f.call", -1, -1), Node.newNumber(1));
    assertTrue(NodeUtil.isObjectCallMethod(call, "call"));
    assertTrue(NodeUtil.isFunctionObjectCall(call));
    assertTrue(NodeUtil.isFunctionObjectCallOrApply(call));
    assertTrue(NodeUtil.isSimpleFunctionObjectCall(call));

    Node apply = NodeUtil.newCallNode(NodeUtil.newQualifiedNameNode(convention, "f.apply", -1, -1), Node.newNumber(1));
    assertTrue(NodeUtil.isFunctionObjectApply(apply));
    assertTrue(NodeUtil.isFunctionObjectCallOrApply(apply));
    assertFalse(NodeUtil.isFunctionObjectCall(apply));

    assertSame(call.getLastChild(), NodeUtil.getArgumentForCallOrNew(call, 0));
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 5));
  }

  @Test
  public void testLhsAndObjectLitKey() {
    Node target = Node.newString(Token.NAME, "lhs");
    Node assign = new Node(Token.ASSIGN, target, Node.newNumber(1));
    assertTrue(NodeUtil.isLhs(target, assign));

    Node objLit = new Node(Token.OBJECTLIT);
    Node strKey = Node.newString("prop");
    objLit.addChildToBack(strKey);
    assertTrue(NodeUtil.isObjectLitKey(strKey, objLit));

    Node getKey = new Node(Token.GET);
    Node setKey = new Node(Token.SET);
    assertTrue(NodeUtil.isGetOrSetKey(getKey));
    assertTrue(NodeUtil.isGetOrSetKey(setKey));
    assertFalse(NodeUtil.isGetOrSetKey(strKey));
  }

  @Test
  public void testOpToStr() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("+=", NodeUtil.opToStr(Token.ASSIGN_ADD));
    assertNull(NodeUtil.opToStr(Token.NAME));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
    try {
      NodeUtil.opToStrNoFail(Token.NAME);
      fail("Expected Error");
    } catch (Error expected) {
    }
  }

  @Test
  public void testQualifiedNameAndLatin() {
    Node qname = NodeUtil.newQualifiedNameNode(convention, "foo.bar.baz", 1, 2);
    assertEquals(Token.GETPROP, qname.getType());
    assertEquals(Token.NAME, NodeUtil.getRootOfQualifiedName(qname).getType());
    assertEquals("foo", NodeUtil.getRootOfQualifiedName(qname).getString());

    assertTrue(NodeUtil.isLatin("abcXYZ_123"));
    assertFalse(NodeUtil.isLatin("abc\u0100"));

    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertFalse(NodeUtil.isValidPropertyName("class")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("prop\u0100"));
  }

  @Test
  public void testVarsAndDeclarations() {
    Node block = new Node(Token.BLOCK);
    Node v1 = NodeUtil.newVarNode("v1", Node.newNumber(1));
    Node v2 = NodeUtil.newVarNode("v2", null);
    block.addChildToBack(v1);
    block.addChildToBack(v2);

    Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(block);
    assertEquals(2, vars.size());

    Node script = new Node(Token.SCRIPT, block);
    NodeUtil.redeclareVarsInsideBranch(block);
    assertTrue(script.getFirstChild().getType() == Token.VAR);
  }

  @Test
  public void testPrototypeProperties() {
    Node getprop = NodeUtil.newQualifiedNameNode(convention, "MyClass.prototype.foo", -1, -1);
    Node assign = new Node(Token.ASSIGN, getprop, Node.newNumber(1));
    Node expr = new Node(Token.EXPR_RESULT, assign);

    assertTrue(NodeUtil.isPrototypePropertyDeclaration(expr));
    assertTrue(NodeUtil.isPrototypeProperty(getprop));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(getprop).getQualifiedName());
    assertEquals("foo", NodeUtil.getPrototypePropertyName(getprop));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryNode = new Node(Token.TRY, new Node(Token.BLOCK), new Node(Token.BLOCK), new Node(Token.BLOCK));
    assertTrue(NodeUtil.hasFinally(tryNode));
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, tryNode.getLastChild()));
    assertSame(tryNode.getFirstChild().getNext(), NodeUtil.getCatchBlock(tryNode));

    Node catchBlock = new Node(Token.BLOCK, new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK)));
    assertTrue(NodeUtil.hasCatchHandler(catchBlock));
    assertFalse(NodeUtil.hasCatchHandler(new Node(Token.BLOCK)));
  }

  @Test
  public void testUndefinedAndNewCall() {
    Node undef = NodeUtil.newUndefinedNode(Node.newNumber(0));
    assertEquals(Token.VOID, undef.getType());

    Node freeCall = NodeUtil.newCallNode(Node.newString(Token.NAME, "foo"), Node.newNumber(1));
    assertTrue(freeCall.getBooleanProp(Node.FREE_CALL));

    Node methodCall = NodeUtil.newCallNode(NodeUtil.newQualifiedNameNode(convention, "a.b", -1, -1));
    assertFalse(methodCall.getBooleanProp(Node.FREE_CALL));
  }

  @Test
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("s")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.REGEXP, Node.newString("foo"))));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.NEW, Node.newString(Token.NAME, "Object"))));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.IN, Node.newString("a"), new Node(Token.OBJECTLIT))));

    Node inc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    assertTrue(NodeUtil.evaluatesToLocalValue(inc));
    inc.putBooleanProp(Node.INCRDECR_PROP, true);
    assertFalse(NodeUtil.evaluatesToLocalValue(inc));

    Node hook = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(hook));

    Node comma = new Node(Token.COMMA, Node.newString(Token.NAME, "a"), Node.newNumber(1));
    assertTrue(NodeUtil.evaluatesToLocalValue(comma));
  }

  @Test
  public void testVisitorAndCounts() {
    Node tree = new Node(Token.BLOCK,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "y"),
        Node.newString(Token.NAME, "x")
    );

    assertEquals(2, NodeUtil.getNameReferenceCount(tree, "x"));
    assertEquals(1, NodeUtil.getNameReferenceCount(tree, "y"));
    assertTrue(NodeUtil.isNameReferenced(tree, "x"));
    assertFalse(NodeUtil.isNameReferenced(tree, "z"));

    final List<Integer> preVisited = new ArrayList<Integer>();
    NodeUtil.visitPreOrder(tree, new NodeUtil.Visitor() {
      @Override
      public void visit(Node node) {
        preVisited.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    assertEquals(4, preVisited.size());

    final List<Integer> postVisited = new ArrayList<Integer>();
    NodeUtil.visitPostOrder(tree, new NodeUtil.Visitor() {
      @Override
      public void visit(Node node) {
        postVisited.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    assertEquals(4, postVisited.size());
  }

  @Test
  public void testSourceNameAndDocInfo() {
    Node node = Node.newString(Token.NAME, "foo");
    node.putProp(Node.SOURCENAME_PROP, "test.js");
    Node child = Node.newNumber(1);
    node.addChildToBack(child);

    assertEquals("test.js", NodeUtil.getSourceName(child));

    JSDocInfo info = new JSDocInfo();
    node.setJSDocInfo(info);
    assertSame(info, NodeUtil.getInfoForNameNode(node));

    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    fn.setJSDocInfo(info);
    assertSame(info, NodeUtil.getFunctionInfo(fn));
  }
}