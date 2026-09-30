package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
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

public class TargetClassTest {

  @Test
  public void testGetExpressionBooleanValue() {
    Node trueNode = new Node(Token.TRUE);
    Node falseNode = new Node(Token.FALSE);

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(trueNode));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(falseNode));

    // ASSIGN & COMMA
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assign));

    Node comma = new Node(Token.COMMA, new Node(Token.FALSE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(comma));

    // NOT
    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(not));

    // AND
    Node and1 = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(and1));
    Node and2 = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(and2));

    // OR
    Node or1 = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(or1));
    Node or2 = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(or2));

    // HOOK (cond ? trueVal : falseVal)
    Node hookSame = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), new Node(Token.TRUE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiff));
  }

  @Test
  public void testGetBooleanValue() {
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("hello")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));

    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID)));

    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "otherVar")));

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(new Node(Token.GETPROP)));
  }

  @Test
  public void testGetStringValue() {
    Assert.assertEquals("foo", NodeUtil.getStringValue(Node.newString("foo")));
    Assert.assertEquals("bar", NodeUtil.getStringValue(Node.newString(Token.NAME, "bar")));

    Assert.assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123.0)));
    Assert.assertEquals("123.45", NodeUtil.getStringValue(Node.newNumber(123.45)));

    Assert.assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    Assert.assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    Assert.assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    Assert.assertNull(NodeUtil.getStringValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testGetFunctionNameAndNearestFunctionName() {
    // Normal function declaration: function foo() {}
    Node fn = NodeUtil.newFunctionNode("foo", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node script = new Node(Token.SCRIPT, fn);
    Assert.assertEquals("foo", NodeUtil.getFunctionName(fn));
    Assert.assertEquals("foo", NodeUtil.getNearestFunctionName(fn));

    // Anonymous function declaration inside script
    Node anonFn = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    script = new Node(Token.SCRIPT, anonFn);
    Assert.assertNull(NodeUtil.getFunctionName(anonFn));

    // var v = function() {}
    Node varName = Node.newString(Token.NAME, "v");
    varName.addChildToBack(anonFn);
    Node varNode = new Node(Token.VAR, varName);
    Assert.assertEquals("v", NodeUtil.getFunctionName(anonFn));
    Assert.assertEquals("v", NodeUtil.getNearestFunctionName(anonFn));

    // qualified.name = function() {}
    Node qname = NodeUtil.newQualifiedNameNode("qualified.name", 0, 0);
    Node assign = new Node(Token.ASSIGN, qname, anonFn);
    Assert.assertEquals("qualified.name", NodeUtil.getFunctionName(anonFn));

    // Object literal key: { 'keyName': function() {} }
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString("keyName");
    objLit.addChildToBack(key);
    objLit.addChildToBack(anonFn);
    Assert.assertEquals("keyName", NodeUtil.getNearestFunctionName(anonFn));
  }

  @Test
  public void testIsImmutableValue() {
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newNumber(42)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));

    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    Assert.assertTrue(NodeUtil.isImmutableValue(voidNode));

    Node negNode = new Node(Token.NEG, Node.newNumber(5));
    Assert.assertTrue(NodeUtil.isImmutableValue(negNode));

    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "customVar")));
    Assert.assertFalse(NodeUtil.isImmutableValue(new Node(Token.OBJECTLIT)));
  }

  @Test
  public void testIsLiteralValue() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    Assert.assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node arrayLitNonConst = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    Assert.assertFalse(NodeUtil.isLiteralValue(arrayLitNonConst, false));

    Node fnExpr = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node expr = new Node(Token.EXPR_RESULT, fnExpr);
    Assert.assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    Assert.assertFalse(NodeUtil.isLiteralValue(fnExpr, false));

    Node fnDecl = NodeUtil.newFunctionNode("f", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node script = new Node(Token.SCRIPT, fnDecl);
    Assert.assertFalse(NodeUtil.isLiteralValue(fnDecl, true));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = Sets.newHashSet("DEF_A", "pkg.DEF_B");
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString("s"), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(10), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertTrue(NodeUtil.isValidDefineValue(notNode, defines));

    Node negNode = new Node(Token.NEG, Node.newNumber(5));
    Assert.assertTrue(NodeUtil.isValidDefineValue(negNode, defines));

    Node bitNotNode = new Node(Token.BITNOT, Node.newNumber(5));
    Assert.assertTrue(NodeUtil.isValidDefineValue(bitNotNode, defines));

    Node nameValid = Node.newString(Token.NAME, "DEF_A");
    Assert.assertTrue(NodeUtil.isValidDefineValue(nameValid, defines));

    Node nameInvalid = Node.newString(Token.NAME, "DEF_UNKNOWN");
    Assert.assertFalse(NodeUtil.isValidDefineValue(nameInvalid, defines));

    Node qnameValid = NodeUtil.newQualifiedNameNode("pkg.DEF_B", 0, 0);
    Assert.assertTrue(NodeUtil.isValidDefineValue(qnameValid, defines));

    Assert.assertFalse(NodeUtil.isValidDefineValue(new Node(Token.CALL), defines));
  }

  @Test
  public void testIsEmptyBlock() {
    Assert.assertFalse(NodeUtil.isEmptyBlock(new Node(Token.SCRIPT)));

    Node emptyBlock = new Node(Token.BLOCK);
    Assert.assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmptyNodes = new Node(Token.BLOCK, new Node(Token.EMPTY), new Node(Token.EMPTY));
    Assert.assertTrue(NodeUtil.isEmptyBlock(blockWithEmptyNodes));

    Node blockWithStmt = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Assert.assertFalse(NodeUtil.isEmptyBlock(blockWithStmt));
  }

  @Test
  public void testIsSimpleOperatorAndPrecedence() {
    int[] ops = {
        Token.ADD, Token.BITAND, Token.BITNOT, Token.BITOR, Token.BITXOR,
        Token.COMMA, Token.DIV, Token.EQ, Token.GE, Token.GETELEM, Token.GETPROP,
        Token.GT, Token.INSTANCEOF, Token.LE, Token.LSH, Token.LT, Token.MOD,
        Token.MUL, Token.NE, Token.NOT, Token.RSH, Token.SHEQ, Token.SHNE,
        Token.SUB, Token.TYPEOF, Token.VOID, Token.POS, Token.NEG, Token.URSH
    };
    for (int op : ops) {
      Assert.assertTrue("Operator " + op + " should be simple", NodeUtil.isSimpleOperatorType(op));
      Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(op)));
    }
    Assert.assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    Assert.assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));

    // Precedence testing
    Assert.assertEquals(0, NodeUtil.precedence(Token.COMMA));
    Assert.assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    Assert.assertEquals(1, NodeUtil.precedence(Token.ASSIGN_ADD));
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
    Assert.assertEquals(15, NodeUtil.precedence(Token.CALL));

    try {
      NodeUtil.precedence(Token.BLOCK);
      Assert.fail("Expected Error for unknown precedence");
    } catch (Error e) {
      // expected
    }

    // Associativity
    Assert.assertTrue(NodeUtil.isAssociative(Token.MUL));
    Assert.assertTrue(NodeUtil.isAssociative(Token.AND));
    Assert.assertTrue(NodeUtil.isAssociative(Token.OR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITOR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITAND));
    Assert.assertFalse(NodeUtil.isAssociative(Token.ADD));
    Assert.assertFalse(NodeUtil.isAssociative(Token.SUB));
  }

  @Test
  public void testAssignmentOps() {
    int[] assignOps = {
        Token.ASSIGN, Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_BITAND,
        Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH, Token.ASSIGN_ADD,
        Token.ASSIGN_SUB, Token.ASSIGN_MUL, Token.ASSIGN_DIV, Token.ASSIGN_MOD
    };
    for (int op : assignOps) {
      Node n = new Node(op);
      Assert.assertTrue(NodeUtil.isAssignmentOp(n));
    }
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

    try {
      NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
      Assert.fail("Expected exception for non-assignment op");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void testSideEffectsAndMutableState() {
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.OBJECTLIT)));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.OBJECTLIT)));

    // Constructor side effects
    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    Assert.assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    Assert.assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    // Function call side effects
    Node mathCall = new Node(Token.CALL, NodeUtil.newQualifiedNameNode("Math.sin", 0, 0));
    Assert.assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    Node builtinCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    Assert.assertFalse(NodeUtil.functionCallHasSideEffects(builtinCall));

    Node customCall = new Node(Token.CALL, Node.newString(Token.NAME, "customFn"));
    Assert.assertTrue(NodeUtil.functionCallHasSideEffects(customCall));

    // Node type may have side effects
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DELPROP)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.INC)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DEC)));
    Assert.assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(Node.newNumber(10)));

    // Can be side effected
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.CALL, Node.newString(Token.NAME, "f"))));
    Assert.assertTrue(NodeUtil.canBeSideEffected(Node.newString(Token.NAME, "variable")));
    Assert.assertFalse(NodeUtil.canBeSideEffected(Node.newString(Token.NAME, "variable"), Collections.singleton("variable")));
  }

  @Test
  public void testAstQueryMethods() {
    Node exprResult = NodeUtil.newExpr(Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isExpressionNode(exprResult));

    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    Assert.assertTrue(NodeUtil.isGet(getProp));
    Assert.assertTrue(NodeUtil.isGetProp(getProp));

    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newNumber(0));
    Assert.assertTrue(NodeUtil.isGet(getElem));
    Assert.assertFalse(NodeUtil.isGetProp(getElem));

    Node nameNode = Node.newString(Token.NAME, "x");
    Assert.assertTrue(NodeUtil.isName(nameNode));
    Assert.assertTrue(NodeUtil.isReferenceName(nameNode));
    Assert.assertFalse(NodeUtil.isReferenceName(Node.newString(Token.NAME, "")));

    Node newNode = new Node(Token.NEW, nameNode);
    Assert.assertTrue(NodeUtil.isNew(newNode));

    Node varNode = NodeUtil.newVarNode("v", Node.newNumber(42));
    Assert.assertTrue(NodeUtil.isVar(varNode));
    Assert.assertTrue(NodeUtil.isVarDeclaration(varNode.getFirstChild()));
    Assert.assertEquals(42.0, NodeUtil.getAssignedValue(varNode.getFirstChild()).getDouble(), 0.0);

    Node stringNode = Node.newString("hello");
    Assert.assertTrue(NodeUtil.isString(stringNode));

    Node assign = new Node(Token.ASSIGN, nameNode, Node.newNumber(1));
    Node exprAssign = NodeUtil.newExpr(assign);
    Assert.assertTrue(NodeUtil.isAssign(assign));
    Assert.assertTrue(NodeUtil.isExprAssign(exprAssign));
    Assert.assertEquals(1.0, NodeUtil.getAssignedValue(nameNode).getDouble(), 0.0);

    Node callNode = NodeUtil.newCallNode(Node.newString(Token.NAME, "fn"), Node.newNumber(1));
    Node exprCall = NodeUtil.newExpr(callNode);
    Assert.assertTrue(NodeUtil.isCall(callNode));
    Assert.assertTrue(NodeUtil.isExprCall(exprCall));
    Assert.assertTrue(NodeUtil.containsCall(exprCall));
    Assert.assertFalse(NodeUtil.containsCall(Node.newNumber(1)));

    Node thisNode = new Node(Token.THIS);
    Assert.assertTrue(NodeUtil.isThis(thisNode));
    Assert.assertTrue(NodeUtil.referencesThis(thisNode));
  }

  @Test
  public void testControlStructuresAndLoops() {
    Node forNode = new Node(Token.FOR, Node.newString(Token.NAME, "i"), Node.newString(Token.NAME, "arr"), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isForIn(forNode));
    Assert.assertTrue(NodeUtil.isLoopStructure(forNode));
    Assert.assertTrue(NodeUtil.isControlStructure(forNode));
    Assert.assertNotNull(NodeUtil.getLoopCodeBlock(forNode));

    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isLoopStructure(whileNode));
    Assert.assertEquals(Token.TRUE, NodeUtil.getConditionExpression(whileNode).getType());
    Assert.assertNotNull(NodeUtil.getLoopCodeBlock(whileNode));

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.TRUE));
    Assert.assertTrue(NodeUtil.isLoopStructure(doNode));
    Assert.assertEquals(Token.TRUE, NodeUtil.getConditionExpression(doNode).getType());
    Assert.assertNotNull(NodeUtil.getLoopCodeBlock(doNode));

    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isControlStructure(ifNode));
    Assert.assertEquals(Token.TRUE, NodeUtil.getConditionExpression(ifNode).getType());
    Assert.assertFalse(NodeUtil.isLoopStructure(ifNode));

    Node innerNode = Node.newNumber(1);
    whileNode.getLastChild().addChildToBack(innerNode);
    Assert.assertTrue(NodeUtil.isWithinLoop(innerNode));
  }

  @Test
  public void testMethodCallsAndObjectCalls() {
    // x.call()
    Node getPropCall = new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString("call"));
    Node call1 = new Node(Token.CALL, getPropCall);
    Assert.assertTrue(NodeUtil.isFunctionObjectCall(call1));
    Assert.assertTrue(NodeUtil.isSimpleFunctionObjectCall(call1));

    // x.apply()
    Node getPropApply = new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString("apply"));
    Node call2 = new Node(Token.CALL, getPropApply);
    Assert.assertTrue(NodeUtil.isFunctionObjectApply(call2));

    // { a: 1 } key checking
    Node key = Node.newString("a");
    Node val = Node.newNumber(1);
    Node objLit = new Node(Token.OBJECTLIT, key, val);
    Assert.assertTrue(NodeUtil.isObjectLitKey(key, objLit));
    Assert.assertFalse(NodeUtil.isObjectLitKey(val, objLit));
  }

  @Test
  public void testOpToStr() {
    Assert.assertEquals("+", NodeUtil.opToStr(Token.ADD));
    Assert.assertEquals("-", NodeUtil.opToStr(Token.SUB));
    Assert.assertEquals("==", NodeUtil.opToStr(Token.EQ));
    Assert.assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    Assert.assertEquals("!", NodeUtil.opToStr(Token.NOT));
    Assert.assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    Assert.assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));

    Assert.assertNull(NodeUtil.opToStr(Token.SCRIPT));
    try {
      NodeUtil.opToStrNoFail(Token.SCRIPT);
      Assert.fail("Expected Error for non-operator token");
    } catch (Error e) {
      // expected
    }
  }

  @Test
  public void testNodeTreeOperationsAndRemovals() {
    // tryMergeBlock
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newNumber(1)), NodeUtil.newExpr(Node.newNumber(2)));
    script.addChildToBack(block);
    Assert.assertTrue(NodeUtil.tryMergeBlock(block));
    Assert.assertEquals(2, script.getChildCount());

    // removeChild from statement block
    Node stmt1 = script.getFirstChild();
    NodeUtil.removeChild(script, stmt1);
    Assert.assertEquals(1, script.getChildCount());

    // removeChild from VAR
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    script.addChildToBack(varNode);
    NodeUtil.removeChild(varNode, varNode.getFirstChild());
    Assert.assertEquals(1, varNode.getChildCount());
    NodeUtil.removeChild(varNode, varNode.getFirstChild());
    Assert.assertEquals(1, script.getChildCount()); // varNode itself removed
  }

  @Test
  public void testFunctionsAndParameters() {
    List<Node> params = Arrays.asList(Node.newString(Token.NAME, "p1"), Node.newString(Token.NAME, "p2"));
    Node body = new Node(Token.BLOCK);
    Node fn = NodeUtil.newFunctionNode("myFunc", params, body, 10, 20);

    Assert.assertTrue(NodeUtil.isFunction(fn));
    Assert.assertEquals(body, NodeUtil.getFunctionBody(fn));
    Assert.assertEquals(Token.LP, NodeUtil.getFnParameters(fn).getType());
    Assert.assertEquals(2, NodeUtil.getFnParameters(fn).getChildCount());

    // Hoisted function declaration
    Node script = new Node(Token.SCRIPT, fn);
    Assert.assertTrue(NodeUtil.isFunctionDeclaration(fn));
    Assert.assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));
    Assert.assertFalse(NodeUtil.isFunctionExpression(fn));

    // VarArgs function
    body.addChildToBack(NodeUtil.newExpr(Node.newString(Token.NAME, "arguments")));
    Assert.assertTrue(NodeUtil.isVarArgsFunction(fn));
  }

  @Test
  public void testPrototypeProperties() {
    Node qname = NodeUtil.newQualifiedNameNode("MyClass.prototype.myMethod", 0, 0);
    Assert.assertTrue(NodeUtil.isPrototypeProperty(qname));
    Assert.assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qname));
    Assert.assertEquals("MyClass", NodeUtil.getPrototypeClassName(qname).getQualifiedName());

    Node assign = new Node(Token.ASSIGN, qname, Node.newNumber(1));
    Node exprAssign = NodeUtil.newExpr(assign);
    Assert.assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));
  }

  @Test
  public void testStringAndIdentifierUtilities() {
    Assert.assertTrue(NodeUtil.isLatin("helloWorld_123"));
    Assert.assertFalse(NodeUtil.isLatin("hello\u4e16\u754c"));

    Assert.assertTrue(NodeUtil.isValidPropertyName("validProp"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("default")); // keyword
    Assert.assertFalse(NodeUtil.isValidPropertyName("invalid-prop"));
  }

  @Test
  public void testTraversalAndQueries() {
    Node root = new Node(Token.BLOCK,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "y"),
        Node.newString(Token.NAME, "x")
    );

    Assert.assertEquals(2, NodeUtil.getNameReferenceCount(root, "x"));
    Assert.assertTrue(NodeUtil.isNameReferenced(root, "y"));
    Assert.assertFalse(NodeUtil.isNameReferenced(root, "z"));
    Assert.assertEquals(3, NodeUtil.getNodeTypeReferenceCount(root, Token.NAME, Predicates.<Node>alwaysTrue()));

    final List<Integer> preVisited = new ArrayList<Integer>();
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        preVisited.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertEquals(4, preVisited.size());

    final List<Integer> postVisited = new ArrayList<Integer>();
    NodeUtil.visitPostOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        postVisited.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertEquals(4, postVisited.size());
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK, new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK)));
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    Assert.assertTrue(NodeUtil.hasFinally(tryNode));
    Assert.assertEquals(catchBlock, NodeUtil.getCatchBlock(tryNode));
    Assert.assertTrue(NodeUtil.hasCatchHandler(catchBlock));
    Assert.assertFalse(NodeUtil.hasCatchHandler(tryBlock));
  }

  @Test
  public void testEvaluatesToLocalValue() {
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(123)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.REGEXP)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.NEW, Node.newString(Token.NAME, "Object"))));

    Node comma = new Node(Token.COMMA, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(comma));

    Node andNode = new Node(Token.AND, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(andNode));

    Node hookNode = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(hookNode));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(addNode));
  }

  @Test
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    script.addChildToBack(block);

    Node varNode = NodeUtil.newVarNode("redeclareMe", Node.newNumber(1));
    block.addChildToBack(varNode);

    Collection<Node> declaredVars = NodeUtil.getVarsDeclaredInBranch(block);
    Assert.assertEquals(1, declaredVars.size());

    NodeUtil.redeclareVarsInsideBranch(block);
    Assert.assertEquals(Token.VAR, script.getFirstChild().getType());
  }

  @Test
  public void testMiscellaneousHelpers() {
    Node undefinedNode = NodeUtil.newUndefinedNode(null);
    Assert.assertEquals(Token.VOID, undefinedNode.getType());

    Node basis = Node.newString(Token.NAME, "origName");
    basis.setLineno(42);
    basis.setCharno(10);
    basis.putProp(Node.SOURCENAME_PROP, "test.js");

    Node newNameNode = NodeUtil.newName("newName", basis, "origName");
    Assert.assertEquals("newName", newNameNode.getString());
    Assert.assertEquals("test.js", NodeUtil.getSourceName(newNameNode));

    Node qname = NodeUtil.newQualifiedNameNode("a.b.c", basis, "a.b.c");
    Assert.assertEquals("a", NodeUtil.getRootOfQualifiedName(qname).getString());

    // Constants
    basis.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Assert.assertTrue(NodeUtil.isConstantName(basis));
    Node copyDest = Node.newString(Token.NAME, "dest");
    NodeUtil.copyNameAnnotations(basis, copyDest);
    Assert.assertTrue(NodeUtil.isConstantName(copyDest));
  }
}