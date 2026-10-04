package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.javascript.rhino.FunctionNode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeUtilTest {

  @Test
  public void testGetBooleanValue() {
    Assert.assertTrue(NodeUtil.getBooleanValue(Node.newString("a")));
    Assert.assertFalse(NodeUtil.getBooleanValue(Node.newString("")));
    Assert.assertTrue(NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    Assert.assertFalse(NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    Assert.assertFalse(NodeUtil.getBooleanValue(new Node(Token.NULL)));
    Assert.assertFalse(NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    Assert.assertFalse(NodeUtil.getBooleanValue(new Node(Token.VOID)));
    Assert.assertTrue(NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    Assert.assertTrue(NodeUtil.getBooleanValue(new Node(Token.REGEXP)));
    Assert.assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetBooleanValueNonLiteral() {
    NodeUtil.getBooleanValue(Node.newString(Token.NAME, "other"));
  }

  @Test
  public void testGetStringValue() {
    Assert.assertEquals("foo", NodeUtil.getStringValue(Node.newString("foo")));
    Assert.assertEquals("bar", NodeUtil.getStringValue(Node.newString(Token.NAME, "bar")));
    Assert.assertEquals("10", NodeUtil.getStringValue(Node.newNumber(10.0)));
    Assert.assertEquals("10.5", NodeUtil.getStringValue(Node.newNumber(10.5)));
    Assert.assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    Assert.assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    Assert.assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));
    Assert.assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testGetFunctionName() {
    Node fn1 = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f1"));
    Node parent1 = new Node(Token.BLOCK, fn1);
    Assert.assertEquals("f1", NodeUtil.getFunctionName(fn1, parent1));

    Node fnEmpty = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""));
    Node parentEmpty = new Node(Token.BLOCK, fnEmpty);
    Assert.assertNull(NodeUtil.getFunctionName(fnEmpty, parentEmpty));

    Node varName = Node.newString(Token.NAME, "varFn");
    varName.addChildToBack(fnEmpty);
    Node parentVar = new Node(Token.VAR, varName);
    Assert.assertEquals("varFn", NodeUtil.getFunctionName(fnEmpty, varName));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "assignedFn"), fnEmpty);
    Assert.assertEquals("assignedFn", NodeUtil.getFunctionName(fnEmpty, assign));
  }

  @Test
  public void testIsImmutableAndLiteralValue() {
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newNumber(5)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));

    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("s"));
    Assert.assertTrue(NodeUtil.isLiteralValue(arr));
    Node badArr = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    Assert.assertFalse(NodeUtil.isLiteralValue(badArr));
    Assert.assertTrue(NodeUtil.isLiteralValue(new Node(Token.OBJECTLIT)));
    Assert.assertTrue(NodeUtil.isLiteralValue(new Node(Token.REGEXP)));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<String>(Arrays.asList("DEF1", "a.b"));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString("s"), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.NOT, new Node(Token.TRUE)), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITAND, Node.newNumber(1)), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITNOT, Node.newNumber(1)), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITOR, Node.newNumber(1)), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.BITXOR, Node.newNumber(1)), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.NEG, Node.newNumber(1)), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF1"), defines));
    Assert.assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF2"), defines));
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    Assert.assertTrue(NodeUtil.isValidDefineValue(getProp, defines));
    Assert.assertFalse(NodeUtil.isValidDefineValue(new Node(Token.NULL), defines));
  }

  @Test
  public void testIsEmptyBlock() {
    Node block1 = new Node(Token.BLOCK);
    Assert.assertTrue(NodeUtil.isEmptyBlock(block1));
    Node block2 = new Node(Token.BLOCK, new Node(Token.EMPTY));
    Assert.assertTrue(NodeUtil.isEmptyBlock(block2));
    Node block3 = new Node(Token.BLOCK, Node.newNumber(1));
    Assert.assertFalse(NodeUtil.isEmptyBlock(block3));
    Assert.assertFalse(NodeUtil.isEmptyBlock(new Node(Token.EXPR_RESULT)));
  }

  @Test
  public void testIsSimpleOperatorType() {
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.MUL));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.DIV));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.MOD));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITAND));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITNOT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITOR));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.BITXOR));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.COMMA));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.EQ));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GETELEM));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GETPROP));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.GT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.INSTANCEOF));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.LE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.LSH));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.LT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.NE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.NOT));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.RSH));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.SHEQ));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.SHNE));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.VOID));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.POS));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.NEG));
    Assert.assertTrue(NodeUtil.isSimpleOperatorType(Token.URSH));
    Assert.assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    Assert.assertFalse(NodeUtil.isSimpleOperatorType(Token.HOOK));
  }

  @Test
  public void testNewExpr() {
    Node child = Node.newNumber(1);
    child.setLineno(5);
    Node expr = NodeUtil.newExpr(child);
    Assert.assertEquals(Token.EXPR_RESULT, expr.getType());
    Assert.assertEquals(child, expr.getFirstChild());
    Assert.assertEquals(5, expr.getLineno());
  }

  @Test
  public void testMayHaveSideEffectsAndMayEffectMutableState() {
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.OBJECTLIT)));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.REGEXP)));

    Node varWithoutChild = new Node(Token.VAR);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(varWithoutChild));
    Node varWithChild = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(varWithChild));

    Node namedFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, namedFn);
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(namedFn));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node expr = new Node(Token.EXPR_RESULT, anonFn);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(anonFn));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(newArray));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "Custom"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(newCustom));

    Node callNoSideEffects = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    callNoSideEffects.setIsNoSideEffectsCall();
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(callNoSideEffects));

    Node assignLiteral = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(assignLiteral));
    Node assignToProp = new Node(Token.ASSIGN,
        new Node(Token.GETPROP, Node.newNumber(1), Node.newString(Token.STRING, "p")),
        Node.newNumber(2));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(assignToProp));
  }

  @Test
  public void testConstructorAndFunctionCallHasSideEffects() {
    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    Assert.assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "Custom"));
    Assert.assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    Node newNoSideEffect = new Node(Token.NEW, Node.newString(Token.NAME, "Custom"));
    newNoSideEffect.setIsNoSideEffectsCall();
    Assert.assertFalse(NodeUtil.constructorCallHasSideEffects(newNoSideEffect));

    Node callString = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    Assert.assertFalse(NodeUtil.functionCallHasSideEffects(callString));

    Node mathFloor = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString(Token.STRING, "floor")));
    Assert.assertFalse(NodeUtil.functionCallHasSideEffects(mathFloor));

    Node callOther = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Assert.assertTrue(NodeUtil.functionCallHasSideEffects(callOther));
  }

  @Test
  public void testNodeTypeMayHaveSideEffects() {
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.ASSIGN)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.CALL)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DELPROP)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.NEW)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DEC)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.INC)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.THROW)));
    Node nameNode = Node.newString(Token.NAME, "x");
    Assert.assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(nameNode));
    nameNode.addChildToBack(Node.newNumber(1));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(nameNode));
    Assert.assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(Node.newNumber(1)));
  }

  @Test
  public void testCanBeSideEffected() {
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.CALL)));
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.NEW)));
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.GETPROP)));
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.GETELEM)));

    Node nameX = Node.newString(Token.NAME, "x");
    Assert.assertTrue(NodeUtil.canBeSideEffected(nameX));
    Assert.assertFalse(NodeUtil.canBeSideEffected(nameX, Collections.singleton("x")));

    Node constName = Node.newString(Token.NAME, "y");
    constName.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Assert.assertFalse(NodeUtil.canBeSideEffected(constName));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertFalse(NodeUtil.canBeSideEffected(add));
  }

  @Test
  public void testPrecedenceAndAssociative() {
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

    Assert.assertTrue(NodeUtil.isAssociative(Token.MUL));
    Assert.assertTrue(NodeUtil.isAssociative(Token.AND));
    Assert.assertTrue(NodeUtil.isAssociative(Token.OR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITOR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITAND));
    Assert.assertFalse(NodeUtil.isAssociative(Token.ADD));
  }

  @Test(expected = Error.class)
  public void testPrecedenceUnknown() {
    NodeUtil.precedence(-9999);
  }

  @Test
  public void testAssignmentOps() {
    Node addAssign = new Node(Token.ASSIGN_ADD);
    Assert.assertTrue(NodeUtil.isAssignmentOp(addAssign));
    Assert.assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(addAssign));
    Assert.assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    Assert.assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    Assert.assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    Assert.assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_LSH)));
    Assert.assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_RSH)));
    Assert.assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_URSH)));
    Assert.assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    Assert.assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    Assert.assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    Assert.assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));
    Assert.assertFalse(NodeUtil.isAssignmentOp(Node.newNumber(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetOpFromNonAssignmentOp() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test
  public void testPredicatesAndChecks() {
    Assert.assertTrue(NodeUtil.isExpressionNode(new Node(Token.EXPR_RESULT)));
    Assert.assertTrue(NodeUtil.isGet(new Node(Token.GETPROP)));
    Assert.assertTrue(NodeUtil.isGet(new Node(Token.GETELEM)));
    Assert.assertTrue(NodeUtil.isGetProp(new Node(Token.GETPROP)));
    Assert.assertTrue(NodeUtil.isName(Node.newString(Token.NAME, "n")));
    Assert.assertTrue(NodeUtil.isNew(new Node(Token.NEW)));
    Assert.assertTrue(NodeUtil.isVar(new Node(Token.VAR)));
    Assert.assertTrue(NodeUtil.isString(Node.newString("s")));
    Assert.assertTrue(NodeUtil.isAssign(new Node(Token.ASSIGN)));
    Assert.assertTrue(NodeUtil.isCall(new Node(Token.CALL)));
    Assert.assertTrue(NodeUtil.isFunction(new Node(Token.FUNCTION)));
    Assert.assertTrue(NodeUtil.isThis(new Node(Token.THIS)));
    Assert.assertTrue(NodeUtil.isSwitchCase(new Node(Token.CASE)));
    Assert.assertTrue(NodeUtil.isSwitchCase(new Node(Token.DEFAULT)));

    Node varNode = new Node(Token.VAR);
    Node nameChild = Node.newString(Token.NAME, "x");
    varNode.addChildToBack(nameChild);
    Assert.assertTrue(NodeUtil.isVarDeclaration(nameChild));

    Assert.assertTrue(NodeUtil.isExprAssign(new Node(Token.EXPR_RESULT, new Node(Token.ASSIGN))));
    Assert.assertTrue(NodeUtil.isExprCall(new Node(Token.EXPR_RESULT, new Node(Token.CALL))));

    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "i"), Node.newString(Token.NAME, "obj"), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isForIn(forIn));

    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    Assert.assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newNumber(1));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(doNode).getType());
    Node whileNode = new Node(Token.WHILE, Node.newNumber(1), new Node(Token.BLOCK));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getLoopCodeBlock(whileNode).getType());
    Assert.assertNull(NodeUtil.getLoopCodeBlock(new Node(Token.EMPTY)));
  }

  @Test
  public void testControlStructureAndCodeBlock() {
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.DO)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.WITH)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.LABEL)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.CATCH)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.CASE)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.DEFAULT)));
    Assert.assertFalse(NodeUtil.isControlStructure(new Node(Token.CALL)));

    Node ifCond = Node.newNumber(1);
    Node ifBody = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, ifCond, ifBody);
    Assert.assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCond));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifBody));

    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(defaultNode, defaultNode.getFirstChild()));
  }

  @Test
  public void testGetConditionExpression() {
    Node ifCond = Node.newNumber(1);
    Node ifNode = new Node(Token.IF, ifCond, new Node(Token.BLOCK));
    Assert.assertEquals(ifCond, NodeUtil.getConditionExpression(ifNode));

    Node for4 = new Node(Token.FOR, new Node(Token.EMPTY), ifCond, new Node(Token.EMPTY), new Node(Token.BLOCK));
    Assert.assertEquals(ifCond, NodeUtil.getConditionExpression(for4));

    Node for3 = new Node(Token.FOR, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y"), new Node(Token.BLOCK));
    Assert.assertNull(NodeUtil.getConditionExpression(for3));
    Assert.assertNull(NodeUtil.getConditionExpression(new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK))));
  }

  @Test
  public void testStatementAndReferenceName() {
    Node script = new Node(Token.SCRIPT);
    Node stmt = Node.newNumber(1);
    script.addChildToBack(stmt);
    Assert.assertTrue(NodeUtil.isStatement(stmt));

    Node labelName = Node.newString(Token.NAME, "lbl");
    Node label = new Node(Token.LABEL, labelName, new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isLabelName(labelName));
    Assert.assertFalse(NodeUtil.isReferenceName(labelName));

    Node refName = Node.newString(Token.NAME, "ref");
    script.addChildToBack(refName);
    Assert.assertTrue(NodeUtil.isReferenceName(refName));
  }

  @Test
  public void testRemoveChildAndTryMergeBlock() {
    Node block = new Node(Token.BLOCK);
    Node stmt1 = Node.newNumber(1);
    Node stmt2 = Node.newNumber(2);
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);
    NodeUtil.removeChild(block, stmt1);
    Assert.assertEquals(1, block.getChildCount());

    Node varNode = new Node(Token.VAR);
    Node n1 = Node.newString(Token.NAME, "a");
    Node n2 = Node.newString(Token.NAME, "b");
    varNode.addChildToBack(n1);
    varNode.addChildToBack(n2);
    NodeUtil.removeChild(varNode, n1);
    Assert.assertEquals(1, varNode.getChildCount());

    Node script = new Node(Token.SCRIPT);
    Node singleVar = new Node(Token.VAR, Node.newString(Token.NAME, "c"));
    script.addChildToBack(singleVar);
    NodeUtil.removeChild(singleVar, singleVar.getFirstChild());
    Assert.assertEquals(0, script.getChildCount());

    Node blockWrapper = new Node(Token.BLOCK);
    Node childBlock = new Node(Token.BLOCK, Node.newNumber(1), Node.newNumber(2));
    blockWrapper.addChildToBack(childBlock);
    Assert.assertTrue(NodeUtil.tryMergeBlock(childBlock));
    Assert.assertEquals(2, blockWrapper.getChildCount());
  }

  @Test
  public void testFunctionsAndMethodCalls() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fn);
    Assert.assertTrue(NodeUtil.isFunctionDeclaration(fn));
    Assert.assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));
    Assert.assertEquals(Token.BLOCK, NodeUtil.getFunctionBody(fn).getType());
    Assert.assertEquals(Token.LP, NodeUtil.getFnParameters(fn).getType());

    Node call1 = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "call")),
        Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isObjectCallMethod(call1, "call"));
    Assert.assertTrue(NodeUtil.isFunctionObjectCall(call1));
    Assert.assertTrue(NodeUtil.isSimpleFunctionObjectCall(call1));

    Node call2 = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "apply")),
        Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isFunctionObjectApply(call2));
  }

  @Test
  public void testLhsAndObjectLitKey() {
    Node name = Node.newString(Token.NAME, "a");
    Node assign = new Node(Token.ASSIGN, name, Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isLhs(name, assign));
    Assert.assertFalse(NodeUtil.isLhs(assign.getLastChild(), assign));

    Node k1 = Node.newString("key1");
    Node v1 = Node.newNumber(1);
    Node obj = new Node(Token.OBJECTLIT, k1, v1);
    Assert.assertTrue(NodeUtil.isObjectLitKey(k1, obj));
    Assert.assertFalse(NodeUtil.isObjectLitKey(v1, obj));
  }

  @Test
  public void testOpToStr() {
    Assert.assertEquals("+", NodeUtil.opToStr(Token.ADD));
    Assert.assertEquals("-", NodeUtil.opToStr(Token.SUB));
    Assert.assertEquals("==", NodeUtil.opToStr(Token.EQ));
    Assert.assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    Assert.assertEquals("!=", NodeUtil.opToStr(Token.NE));
    Assert.assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    Assert.assertEquals("void", NodeUtil.opToStr(Token.VOID));
    Assert.assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(expected = Error.class)
  public void testOpToStrNoFailError() {
    NodeUtil.opToStrNoFail(-999);
  }

  @Test
  public void testRedeclareVarsAndNewConstructs() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node varNode = NodeUtil.newVarNode("v", Node.newNumber(10));
    block.addChildToBack(varNode);
    script.addChildToBack(block);

    NodeUtil.redeclareVarsInsideBranch(block);
    Assert.assertEquals(2, script.getChildCount());

    FunctionNode fnNode = NodeUtil.newFunctionNode("fn", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 2);
    Assert.assertNotNull(fnNode);

    Node qName = NodeUtil.newQualifiedNameNode("a.b.c", 1, 1);
    Assert.assertEquals(Token.GETPROP, qName.getType());
    Assert.assertEquals("a.b.c", qName.getQualifiedName());

    Node undefinedNode = NodeUtil.newUndefinedNode();
    Assert.assertEquals(Token.VOID, undefinedNode.getType());
  }

  @Test
  public void testPrototypeHelpers() {
    Node qName = NodeUtil.newQualifiedNameNode("MyClass.prototype.myMethod", 0, 0);
    Assert.assertTrue(NodeUtil.isPrototypeProperty(qName));
    Assert.assertEquals("MyClass", NodeUtil.getPrototypeClassName(qName).getQualifiedName());
    Assert.assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qName));

    Node exprAssign = NodeUtil.newExpr(new Node(Token.ASSIGN, qName, Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));
  }

  @Test
  public void testTraversalAndCounts() {
    Node tree = new Node(Token.BLOCK,
        Node.newString(Token.NAME, "x"),
        new Node(Token.CALL, Node.newString(Token.NAME, "foo")),
        Node.newString(Token.NAME, "x"));

    Assert.assertTrue(NodeUtil.containsType(tree, Token.CALL));
    Assert.assertTrue(NodeUtil.containsCall(tree));
    Assert.assertEquals(2, NodeUtil.getNameReferenceCount(tree, "x"));
    Assert.assertEquals(1, NodeUtil.getNodeTypeReferenceCount(tree, Token.CALL));
    Assert.assertTrue(NodeUtil.isNameReferenced(tree, "foo"));
    Assert.assertTrue(NodeUtil.isNodeTypeReferenced(tree, Token.CALL));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryNode = new Node(Token.TRY, new Node(Token.BLOCK), new Node(Token.BLOCK), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.hasFinally(tryNode));
    Assert.assertEquals(tryNode.getFirstChild().getNext(), NodeUtil.getCatchBlock(tryNode));

    Node catchHandlerBlock = new Node(Token.BLOCK, new Node(Token.CATCH));
    Assert.assertTrue(NodeUtil.hasCatchHandler(catchHandlerBlock));
  }

  @Test
  public void testMiscUtilities() {
    Assert.assertTrue(NodeUtil.isLatin("abcXYZ_0129"));
    Assert.assertFalse(NodeUtil.isLatin("abc\u0100"));
    Assert.assertTrue(NodeUtil.isValidPropertyName("validPropName"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("class"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("bad prop"));

    Node nameNode = Node.newString(Token.NAME, "varName");
    JSDocInfo info = new JSDocInfo();
    nameNode.setJSDocInfo(info);
    Assert.assertSame(info, NodeUtil.getInfoForNameNode(nameNode));

    Node parent = new Node(Token.SCRIPT);
    parent.putProp(Node.SOURCENAME_PROP, "source.js");
    parent.addChildToBack(nameNode);
    Assert.assertEquals("source.js", NodeUtil.getSourceName(nameNode));

    Node basis = Node.newString(Token.NAME, "orig");
    basis.setLineno(10);
    Node named = NodeUtil.newName("newN", basis, "origName");
    Assert.assertEquals("newN", named.getString());
    Assert.assertEquals("origName", named.getProp(Node.ORIGINALNAME_PROP));
  }
}
