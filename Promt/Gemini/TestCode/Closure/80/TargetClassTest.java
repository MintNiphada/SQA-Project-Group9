package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeUtilTest {

  @Test
  public void testGetBooleanValue() {
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID)));

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("hello")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(-1.0)));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "other")));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(notTrue));

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(notFalse));

    Node unknownNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(unknownNode));
  }

  @Test
  public void testGetExpressionBooleanValue() {
    Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assignNode));

    Node commaNode = new Node(Token.COMMA, Node.newNumber(0), Node.newString("str"));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(commaNode));

    Node notNode = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(notNode));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(orNode));

    Node hookSame = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.FALSE));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiff));

    Node numNode = Node.newNumber(42);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(numNode));
  }

  @Test
  public void testGetStringValue() {
    Assert.assertEquals("foo", NodeUtil.getStringValue(Node.newString("foo")));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "other")));

    Assert.assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123.0)));
    Assert.assertEquals("123.5", NodeUtil.getStringValue(Node.newNumber(123.5)));

    Assert.assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    Assert.assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    Assert.assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals("false", NodeUtil.getStringValue(notTrue));
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals("true", NodeUtil.getStringValue(notFalse));
    Node notUnknown = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    Assert.assertNull(NodeUtil.getStringValue(notUnknown));

    Assert.assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newNumber(1));
    Assert.assertEquals("a,1", NodeUtil.getStringValue(arrayLit));

    Node emptyArrayLit = new Node(Token.ARRAYLIT);
    Assert.assertEquals("", NodeUtil.getStringValue(emptyArrayLit));

    Assert.assertNull(NodeUtil.getStringValue(new Node(Token.ADD)));
  }

  @Test
  public void testArrayToStringWithSkips() {
    Node arrayLit = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
    int[] skipIndexes = new int[]{1};
    arrayLit.putProp(Node.SKIP_INDEXES_PROP, skipIndexes);
    Assert.assertEquals("a,,b", NodeUtil.getStringValue(arrayLit));

    Node arrayWithNull = new Node(Token.ARRAYLIT, new Node(Token.NULL), Node.newString(Token.NAME, "undefined"));
    Assert.assertEquals(",", NodeUtil.getStringValue(arrayWithNull));

    Node arrayWithUnknown = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "unknown"));
    Assert.assertNull(NodeUtil.getStringValue(arrayWithUnknown));
  }

  @Test
  public void testGetNumberValue() {
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    Assert.assertEquals(Double.valueOf(42.0), NodeUtil.getNumberValue(Node.newNumber(42.0)));

    Node voidNodeNoSideEffects = new Node(Token.VOID, Node.newNumber(0));
    Assert.assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(voidNodeNoSideEffects));

    Node voidNodeSideEffects = new Node(Token.VOID, new Node(Token.CALL, Node.newString(Token.NAME, "foo")));
    Assert.assertNull(NodeUtil.getNumberValue(voidNodeSideEffects));

    Assert.assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "foo")));

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    Assert.assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));

    Node negOther = new Node(Token.NEG, Node.newString(Token.NAME, "foo"));
    Assert.assertNull(NodeUtil.getNumberValue(negOther));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notTrue));
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notFalse));
    Node notUnknown = new Node(Token.NOT, Node.newString(Token.NAME, "bar"));
    Assert.assertNull(NodeUtil.getNumberValue(notUnknown));

    Assert.assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(Node.newString("123")));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.ARRAYLIT)));
    Assert.assertEquals(Double.valueOf(Double.NaN), NodeUtil.getNumberValue(new Node(Token.OBJECTLIT)));

    Assert.assertNull(NodeUtil.getNumberValue(new Node(Token.ASSIGN)));
  }

  @Test
  public void testGetStringNumberValue() {
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   \t\n\r  "));
    Assert.assertEquals(Double.valueOf(15.0), NodeUtil.getStringNumberValue("0xf"));
    Assert.assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0X10"));
    Assert.assertEquals(Double.valueOf(Double.NaN), NodeUtil.getStringNumberValue("0xZZZ"));

    Assert.assertNull(NodeUtil.getStringNumberValue("+0x10"));
    Assert.assertNull(NodeUtil.getStringNumberValue("-0x10"));

    Assert.assertNull(NodeUtil.getStringNumberValue("infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("-infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("+infinity"));

    Assert.assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue("  123.45  "));
    Assert.assertEquals(Double.valueOf(Double.NaN), NodeUtil.getStringNumberValue("not_a_number"));
  }

  @Test
  public void testIsStrWhiteSpaceChar() {
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar(' '));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\n'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\r'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\t'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\u00A0'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\u000C'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\u000B'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\u2028'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\u2029'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\uFEFF'));
    Assert.assertTrue(NodeUtil.isStrWhiteSpaceChar('\u2000'));
    Assert.assertFalse(NodeUtil.isStrWhiteSpaceChar('A'));
    Assert.assertFalse(NodeUtil.isStrWhiteSpaceChar('0'));
  }

  @Test
  public void testGetFunctionNameAndNearest() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "myFunc"), new Node(Token.LP), new Node(Token.BLOCK));
    Node varParent = Node.newString(Token.NAME, "varName");
    varParent.addChildToBack(fn);
    Assert.assertEquals("varName", NodeUtil.getFunctionName(fn));
    Assert.assertEquals("varName", NodeUtil.getNearestFunctionName(fn));

    Node fn2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node assignParent = new Node(Token.ASSIGN, Node.newString(Token.NAME, "assignedName"), fn2);
    Assert.assertEquals("assignedName", NodeUtil.getFunctionName(fn2));

    Node fn3 = new Node(Token.FUNCTION, Node.newString(Token.NAME, "named"), new Node(Token.LP), new Node(Token.BLOCK));
    Node block = new Node(Token.BLOCK, fn3);
    Assert.assertEquals("named", NodeUtil.getFunctionName(fn3));

    Node fnAnon = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node block2 = new Node(Token.BLOCK, fnAnon);
    Assert.assertNull(NodeUtil.getFunctionName(fnAnon));

    Node fnGetter = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node getProp = Node.newString(Token.GET, "getterProp");
    getProp.addChildToBack(fnGetter);
    Assert.assertEquals("getterProp", NodeUtil.getNearestFunctionName(fnGetter));

    Node fnSetter = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node setProp = Node.newString(Token.SET, "setterProp");
    setProp.addChildToBack(fnSetter);
    Assert.assertEquals("setterProp", NodeUtil.getNearestFunctionName(fnSetter));

    Node fnStringKey = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node stringProp = Node.newString(Token.STRING, "stringProp");
    stringProp.addChildToBack(fnStringKey);
    Assert.assertEquals("stringProp", NodeUtil.getNearestFunctionName(fnStringKey));

    Node fnNumKey = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node numProp = Node.newNumber(100);
    numProp.addChildToBack(fnNumKey);
    Assert.assertEquals("100", NodeUtil.getNearestFunctionName(fnNumKey));

    Node fnExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node exprResult = new Node(Token.EXPR_RESULT, fnExpr);
    Assert.assertNull(NodeUtil.getNearestFunctionName(fnExpr));
  }

  @Test
  public void testIsImmutableValue() {
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1.0)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));

    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1))));

    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));
    Assert.assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));
  }

  @Test
  public void testIsLiteralValue() {
    Assert.assertTrue(NodeUtil.isLiteralValue(Node.newNumber(10), false));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    Assert.assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node arrayLitNonConst = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    Assert.assertFalse(NodeUtil.isLiteralValue(arrayLitNonConst, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key1 = Node.newString(Token.STRING, "k1");
    key1.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(key1);
    Assert.assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node objLitNonConst = new Node(Token.OBJECTLIT);
    Node key2 = Node.newString(Token.STRING, "k2");
    key2.addChildToBack(Node.newString(Token.NAME, "x"));
    objLitNonConst.addChildToBack(key2);
    Assert.assertFalse(NodeUtil.isLiteralValue(objLitNonConst, false));

    Node fnDecl = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fnDecl);
    Assert.assertFalse(NodeUtil.isLiteralValue(fnDecl, true));

    Node fnExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node varN = Node.newString(Token.NAME, "f");
    varN.addChildToBack(fnExpr);
    Node varStmt = new Node(Token.VAR, varN);
    Assert.assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    Assert.assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<String>();
    defines.add("DEF_A");
    defines.add("ns.DEF_B");

    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString("s"), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.isValidDefineValue(addNode, defines));

    Node notNode = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertTrue(NodeUtil.isValidDefineValue(notNode, defines));

    Node nameDef = Node.newString(Token.NAME, "DEF_A");
    Assert.assertTrue(NodeUtil.isValidDefineValue(nameDef, defines));

    Node nameNonDef = Node.newString(Token.NAME, "NOT_DEF");
    Assert.assertFalse(NodeUtil.isValidDefineValue(nameNonDef, defines));

    Node getPropDef = new Node(Token.GETPROP, Node.newString(Token.NAME, "ns"), Node.newString(Token.STRING, "DEF_B"));
    Assert.assertTrue(NodeUtil.isValidDefineValue(getPropDef, defines));

    Assert.assertFalse(NodeUtil.isValidDefineValue(new Node(Token.ARRAYLIT), defines));
  }

  @Test
  public void testIsEmptyBlock() {
    Node nonBlock = new Node(Token.EXPR_RESULT);
    Assert.assertFalse(NodeUtil.isEmptyBlock(nonBlock));

    Node emptyBlock = new Node(Token.BLOCK);
    Assert.assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmpty = new Node(Token.BLOCK, new Node(Token.EMPTY), new Node(Token.EMPTY));
    Assert.assertTrue(NodeUtil.isEmptyBlock(blockWithEmpty));

    Node blockWithStmt = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT));
    Assert.assertFalse(NodeUtil.isEmptyBlock(blockWithStmt));
  }

  @Test
  public void testIsSimpleOperator() {
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.SUB)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETPROP)));
    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETELEM)));
    Assert.assertFalse(NodeUtil.isSimpleOperator(new Node(Token.CALL)));
    Assert.assertFalse(NodeUtil.isSimpleOperator(new Node(Token.ASSIGN)));
  }

  @Test
  public void testNewExpr() {
    Node child = Node.newNumber(5);
    child.setLineno(10);
    child.setCharno(20);
    Node expr = NodeUtil.newExpr(child);
    Assert.assertEquals(Token.EXPR_RESULT, expr.getType());
    Assert.assertEquals(child, expr.getFirstChild());
    Assert.assertEquals(10, expr.getLineno());
    Assert.assertEquals(20, expr.getCharno());
  }

  @Test
  public void testMayHaveSideEffectsAndStateChange() {
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(Node.newString("str")));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node objLit = new Node(Token.OBJECTLIT);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(objLit));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(objLit));

    Node arrLit = new Node(Token.ARRAYLIT);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(arrLit));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(arrLit));

    Node regexLit = new Node(Token.REGEXP);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(regexLit));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(regexLit));

    Node varNoInit = new Node(Token.NAME, "x");
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(varNoInit));

    Node varWithInit = Node.newString(Token.NAME, "x");
    varWithInit.addChildToBack(Node.newNumber(1));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(varWithInit));

    Node fnDecl = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), new Node(Token.BLOCK));
    Node script = new Node(Token.SCRIPT, fnDecl);
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(fnDecl));

    Node fnExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node varN = Node.newString(Token.NAME, "f");
    varN.addChildToBack(fnExpr);
    Node varStmt = new Node(Token.VAR, varN);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(fnExpr));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(newCustom));

    Node callMath = new Node(Token.CALL, new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString(Token.STRING, "sin")), Node.newNumber(1));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(callMath));

    Node callCustom = new Node(Token.CALL, Node.newString(Token.NAME, "customFunc"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(callCustom));

    Node callBuiltin = new Node(Token.CALL, Node.newString(Token.NAME, "String"), Node.newNumber(1));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(callBuiltin));

    Node callToString = new Node(Token.CALL, new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "toString")));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(callToString));

    Node assignName = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(assignName));

    Node assignLocalProp = new Node(Token.ASSIGN,
        new Node(Token.GETPROP, new Node(Token.OBJECTLIT), Node.newString(Token.STRING, "p")),
        Node.newNumber(1));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(assignLocalProp));
  }

  @Test
  public void testConstructorAndFunctionCallSideEffectsExceptions() {
    try {
      NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
      Assert.fail();
    } catch (IllegalStateException e) {
    }

    try {
      NodeUtil.functionCallHasSideEffects(new Node(Token.NEW));
      Assert.fail();
    } catch (IllegalStateException e) {
    }
  }

  @Test
  public void testCallAndNewLocalResult() {
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    callNode.setSideEffectFlags(Node.FLAG_LOCAL_RESULTS);
    Assert.assertTrue(NodeUtil.callHasLocalResult(callNode));

    Node callNodeNonLocal = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    Assert.assertFalse(NodeUtil.callHasLocalResult(callNodeNonLocal));

    Node newNode = new Node(Token.NEW, Node.newString(Token.NAME, "Object"));
    newNode.setSideEffectFlags(Node.FLAG_THIS_UNMODIFIED);
    Assert.assertTrue(NodeUtil.newHasLocalResult(newNode));
  }

  @Test
  public void testNodeTypeMayHaveSideEffects() {
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DELPROP)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DEC)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.INC)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.THROW)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.ASSIGN)));
    Assert.assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(Node.newNumber(1)));
  }

  @Test
  public void testCanBeSideEffected() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    Assert.assertTrue(NodeUtil.canBeSideEffected(call));

    Node newN = new Node(Token.NEW, Node.newString(Token.NAME, "c"));
    Assert.assertTrue(NodeUtil.canBeSideEffected(newN));

    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    Assert.assertTrue(NodeUtil.canBeSideEffected(getProp));

    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newNumber(0));
    Assert.assertTrue(NodeUtil.canBeSideEffected(getElem));

    Node nameNode = Node.newString(Token.NAME, "varName");
    Assert.assertTrue(NodeUtil.canBeSideEffected(nameNode, Collections.<String>emptySet()));
    Assert.assertFalse(NodeUtil.canBeSideEffected(nameNode, Collections.singleton("varName")));

    Node constName = Node.newString(Token.NAME, "CONST_VAL");
    constName.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Assert.assertFalse(NodeUtil.canBeSideEffected(constName));
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

    try {
      NodeUtil.precedence(-999);
      Assert.fail();
    } catch (Error e) {
    }
  }

  @Test
  public void testIsNumericResult() {
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.BITNOT, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.SUB, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "NaN")));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "Infinity")));
    Assert.assertFalse(NodeUtil.isNumericResult(Node.newString(Token.NAME, "foo")));

    Node addNumbers = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.isNumericResult(addNumbers));

    Node addStrings = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
    Assert.assertFalse(NodeUtil.isNumericResult(addStrings));
  }

  @Test
  public void testIsBooleanResult() {
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.NE, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.SHEQ, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.SHNE, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.LT, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.IN, Node.newString("a"), Node.newString(Token.NAME, "b"))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.INSTANCEOF, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, Node.newNumber(1))));
    Assert.assertFalse(NodeUtil.isBooleanResult(Node.newNumber(1)));
  }

  @Test
  public void testNullAndUndefinedChecks() {
    Assert.assertTrue(NodeUtil.isUndefined(new Node(Token.VOID)));
    Assert.assertTrue(NodeUtil.isUndefined(Node.newString(Token.NAME, "undefined")));
    Assert.assertFalse(NodeUtil.isUndefined(Node.newString(Token.NAME, "defined")));

    Assert.assertTrue(NodeUtil.isNull(new Node(Token.NULL)));
    Assert.assertFalse(NodeUtil.isNull(new Node(Token.VOID)));

    Assert.assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.VOID)));
    Assert.assertFalse(NodeUtil.isNullOrUndefined(Node.newNumber(1)));
  }

  @Test
  public void testMayBeString() {
    Assert.assertTrue(NodeUtil.mayBeString(Node.newString("foo")));
    Assert.assertTrue(NodeUtil.mayBeString(Node.newString(Token.NAME, "bar")));
    Assert.assertFalse(NodeUtil.mayBeString(Node.newNumber(123)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.TRUE)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.NULL)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.VOID)));
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
    Assert.assertFalse(NodeUtil.isCommutative(Token.AND));
    Assert.assertFalse(NodeUtil.isCommutative(Token.SUB));
  }

  @Test
  public void testIsAssignmentOpAndGetOp() {
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_SUB)));
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
      NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN));
      Assert.fail();
    } catch (IllegalArgumentException e) {
    }
  }

  @Test
  public void testAstQueryUtilities() {
    Assert.assertTrue(NodeUtil.isExpressionNode(new Node(Token.EXPR_RESULT)));
    Assert.assertFalse(NodeUtil.isExpressionNode(new Node(Token.BLOCK)));

    Assert.assertTrue(NodeUtil.isGet(new Node(Token.GETPROP)));
    Assert.assertTrue(NodeUtil.isGet(new Node(Token.GETELEM)));
    Assert.assertFalse(NodeUtil.isGet(new Node(Token.NAME)));

    Assert.assertTrue(NodeUtil.isGetProp(new Node(Token.GETPROP)));
    Assert.assertFalse(NodeUtil.isGetProp(new Node(Token.GETELEM)));

    Assert.assertTrue(NodeUtil.isName(new Node(Token.NAME)));
    Assert.assertTrue(NodeUtil.isNew(new Node(Token.NEW)));
    Assert.assertTrue(NodeUtil.isVar(new Node(Token.VAR)));
    Assert.assertTrue(NodeUtil.isString(Node.newString("s")));
    Assert.assertTrue(NodeUtil.isAssign(new Node(Token.ASSIGN)));
    Assert.assertTrue(NodeUtil.isCall(new Node(Token.CALL)));
    Assert.assertTrue(NodeUtil.isFunction(new Node(Token.FUNCTION)));
    Assert.assertTrue(NodeUtil.isThis(new Node(Token.THIS)));
    Assert.assertTrue(NodeUtil.isArrayLiteral(new Node(Token.ARRAYLIT)));

    Node nameInVar = Node.newString(Token.NAME, "x");
    Node varNode = new Node(Token.VAR, nameInVar);
    Assert.assertTrue(NodeUtil.isVarDeclaration(nameInVar));

    Node val = Node.newNumber(10);
    nameInVar.addChildToBack(val);
    Assert.assertEquals(val, NodeUtil.getAssignedValue(nameInVar));

    Node nameInAssign = Node.newString(Token.NAME, "y");
    Node assignNode = new Node(Token.ASSIGN, nameInAssign, val);
    Assert.assertEquals(val, NodeUtil.getAssignedValue(nameInAssign));

    Node exprAssign = new Node(Token.EXPR_RESULT, new Node(Token.ASSIGN));
    Assert.assertTrue(NodeUtil.isExprAssign(exprAssign));

    Node exprCall = new Node(Token.EXPR_RESULT, new Node(Token.CALL));
    Assert.assertTrue(NodeUtil.isExprCall(exprCall));

    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isForIn(forIn));

    Node forRegular = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK));
    Assert.assertFalse(NodeUtil.isForIn(forRegular));

    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    Assert.assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));
  }

  @Test
  public void testLoopAndControlStructureUtilities() {
    Node block1 = new Node(Token.BLOCK);
    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.EMPTY), block1);
    Assert.assertEquals(block1, NodeUtil.getLoopCodeBlock(forNode));

    Node block2 = new Node(Token.BLOCK);
    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), block2);
    Assert.assertEquals(block2, NodeUtil.getLoopCodeBlock(whileNode));

    Node block3 = new Node(Token.BLOCK);
    Node doNode = new Node(Token.DO, block3, new Node(Token.TRUE));
    Assert.assertEquals(block3, NodeUtil.getLoopCodeBlock(doNode));
    Assert.assertNull(NodeUtil.getLoopCodeBlock(new Node(Token.IF)));

    Node innerNode = Node.newNumber(1);
    block1.addChildToBack(innerNode);
    Assert.assertTrue(NodeUtil.isWithinLoop(innerNode));

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
    Assert.assertFalse(NodeUtil.isControlStructure(new Node(Token.EXPR_RESULT)));

    Node ifCond = new Node(Token.TRUE);
    Node ifThen = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, ifCond, ifThen);
    Assert.assertEquals(ifCond, NodeUtil.getConditionExpression(ifNode));
    Assert.assertEquals(ifCond, NodeUtil.getConditionExpression(whileNode));
    Assert.assertEquals(ifCond, NodeUtil.getConditionExpression(doNode));
    Assert.assertNull(NodeUtil.getConditionExpression(forIn));
    Assert.assertEquals(new Node(Token.EMPTY).getType(), NodeUtil.getConditionExpression(forRegular).getType());

    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(forNode, block1));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(doNode, block3));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifThen));
    Assert.assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCond));

    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(defaultNode, defaultNode.getFirstChild()));
  }

  @Test
  public void testStatementAndFunctionPredicates() {
    Node script = new Node(Token.SCRIPT);
    Node stmt = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    script.addChildToBack(stmt);

    Assert.assertTrue(NodeUtil.isStatementBlock(script));
    Assert.assertTrue(NodeUtil.isStatement(stmt));

    Assert.assertTrue(NodeUtil.isSwitchCase(new Node(Token.CASE)));
    Assert.assertTrue(NodeUtil.isSwitchCase(new Node(Token.DEFAULT)));
    Assert.assertFalse(NodeUtil.isSwitchCase(new Node(Token.SWITCH)));

    Assert.assertTrue(NodeUtil.isReferenceName(Node.newString(Token.NAME, "foo")));
    Assert.assertFalse(NodeUtil.isReferenceName(Node.newString(Token.NAME, "")));
    Assert.assertFalse(NodeUtil.isReferenceName(Node.newNumber(1)));

    Assert.assertTrue(NodeUtil.isLabelName(new Node(Token.LABEL_NAME)));
    Assert.assertFalse(NodeUtil.isLabelName(null));
    Assert.assertFalse(NodeUtil.isLabelName(new Node(Token.NAME)));

    Node fnBody = new Node(Token.BLOCK);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP), fnBody);
    script.addChildToBack(fn);

    Assert.assertTrue(NodeUtil.isFunctionDeclaration(fn));
    Assert.assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));
    Assert.assertEquals(fnBody, NodeUtil.getFunctionBody(fn));

    Node fnExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node callExpr = new Node(Token.CALL, fnExpr);
    Assert.assertTrue(NodeUtil.isFunctionExpression(fnExpr));
    Assert.assertTrue(NodeUtil.isEmptyFunctionExpression(fnExpr));

    Node varArgsFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "v"), new Node(Token.LP),
        new Node(Token.BLOCK, Node.newString(Token.NAME, "arguments")));
    Assert.assertTrue(NodeUtil.isVarArgsFunction(varArgsFn));

    Node normalFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "v"), new Node(Token.LP),
        new Node(Token.BLOCK, Node.newString(Token.NAME, "x")));
    Assert.assertFalse(NodeUtil.isVarArgsFunction(normalFn));
  }

  @Test
  public void testRemoveChild() {
    Node script = new Node(Token.SCRIPT);
    Node stmt1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node stmt2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    script.addChildToBack(stmt1);
    script.addChildToBack(stmt2);

    NodeUtil.removeChild(script, stmt1);
    Assert.assertEquals(1, script.getChildCount());
    Assert.assertEquals(stmt2, script.getFirstChild());

    Node block = new Node(Token.BLOCK, Node.newNumber(1), Node.newNumber(2));
    NodeUtil.removeChild(script, block);
    Assert.assertFalse(block.hasChildren());

    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    script.addChildToBack(varNode);
    NodeUtil.removeChild(varNode, varNode.getFirstChild());
    Assert.assertEquals(1, varNode.getChildCount());

    Node singleVar = new Node(Token.VAR, Node.newString(Token.NAME, "c"));
    script.addChildToBack(singleVar);
    NodeUtil.removeChild(singleVar, singleVar.getFirstChild());
    Assert.assertFalse(script.hasChild(singleVar));

    Node forNode = new Node(Token.FOR, Node.newNumber(1), Node.newNumber(2), Node.newNumber(3), new Node(Token.BLOCK));
    NodeUtil.removeChild(forNode, forNode.getFirstChild());
    Assert.assertEquals(Token.EMPTY, forNode.getFirstChild().getType());
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBody = new Node(Token.BLOCK);
    Node catchContainer = new Node(Token.BLOCK);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBody, catchContainer, finallyBlock);

    Assert.assertTrue(NodeUtil.hasFinally(tryNode));
    Assert.assertEquals(catchContainer, NodeUtil.getCatchBlock(tryNode));
    Assert.assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));
    Assert.assertTrue(NodeUtil.isTryCatchNodeContainer(catchContainer));

    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    catchContainer.addChildToBack(catchNode);
    Assert.assertTrue(NodeUtil.hasCatchHandler(catchContainer));

    NodeUtil.removeChild(tryNode, finallyBlock);
    Assert.assertEquals(2, tryNode.getChildCount());

    NodeUtil.maybeAddFinally(tryNode);
    Assert.assertTrue(NodeUtil.hasFinally(tryNode));

    NodeUtil.removeChild(catchContainer, catchNode);
    Assert.assertFalse(catchContainer.hasChildren());
  }

  @Test
  public void testTryMergeBlock() {
    Node script = new Node(Token.SCRIPT);
    Node innerBlock = new Node(Token.BLOCK, Node.newNumber(1), Node.newNumber(2));
    script.addChildToBack(innerBlock);

    Assert.assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    Assert.assertEquals(2, script.getChildCount());

    Node standaloneBlock = new Node(Token.BLOCK);
    Assert.assertFalse(NodeUtil.tryMergeBlock(standaloneBlock));
  }

  @Test
  public void testObjectMethodAndFunctionCallChecks() {
    Node callTarget = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "call"));
    Node callNode = new Node(Token.CALL, callTarget, Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isObjectCallMethod(callNode, "call"));
    Assert.assertTrue(NodeUtil.isFunctionObjectCall(callNode));
    Assert.assertFalse(NodeUtil.isFunctionObjectApply(callNode));
    Assert.assertTrue(NodeUtil.isFunctionObjectCallOrApply(callNode));
    Assert.assertTrue(NodeUtil.isSimpleFunctionObjectCall(callNode));

    Node applyTarget = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "apply"));
    Node applyNode = new Node(Token.CALL, applyTarget);
    Assert.assertTrue(NodeUtil.isFunctionObjectApply(applyNode));

    Node nonCall = Node.newNumber(1);
    Assert.assertFalse(NodeUtil.isObjectCallMethod(nonCall, "call"));
  }

  @Test
  public void testIsLhsAndObjectLitKey() {
    Node lhs = Node.newString(Token.NAME, "x");
    Node rhs = Node.newNumber(1);
    Node assign = new Node(Token.ASSIGN, lhs, rhs);
    Assert.assertTrue(NodeUtil.isLhs(lhs, assign));
    Assert.assertFalse(NodeUtil.isLhs(rhs, assign));

    Node var = new Node(Token.VAR, lhs);
    Assert.assertTrue(NodeUtil.isLhs(lhs, var));

    Node objLit = new Node(Token.OBJECTLIT);
    Node keyStr = Node.newString(Token.STRING, "myKey");
    objLit.addChildToBack(keyStr);
    Assert.assertTrue(NodeUtil.isObjectLitKey(keyStr, objLit));
    Assert.assertEquals("myKey", NodeUtil.getObjectLitKeyName(keyStr));

    Node keyNum = Node.newNumber(123);
    Assert.assertEquals("123", NodeUtil.getObjectLitKeyName(keyNum));

    Node getProp = Node.newString(Token.GET, "getter");
    Assert.assertTrue(NodeUtil.isGetOrSetKey(getProp));
    Assert.assertEquals("getter", NodeUtil.getObjectLitKeyName(getProp));
  }

  @Test
  public void testOpToStr() {
    Assert.assertEquals("|", NodeUtil.opToStr(Token.BITOR));
    Assert.assertEquals("||", NodeUtil.opToStr(Token.OR));
    Assert.assertEquals("^", NodeUtil.opToStr(Token.BITXOR));
    Assert.assertEquals("&&", NodeUtil.opToStr(Token.AND));
    Assert.assertEquals("&", NodeUtil.opToStr(Token.BITAND));
    Assert.assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    Assert.assertEquals("==", NodeUtil.opToStr(Token.EQ));
    Assert.assertEquals("!", NodeUtil.opToStr(Token.NOT));
    Assert.assertEquals("!=", NodeUtil.opToStr(Token.NE));
    Assert.assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    Assert.assertEquals("<<", NodeUtil.opToStr(Token.LSH));
    Assert.assertEquals("in", NodeUtil.opToStr(Token.IN));
    Assert.assertEquals("<=", NodeUtil.opToStr(Token.LE));
    Assert.assertEquals("<", NodeUtil.opToStr(Token.LT));
    Assert.assertEquals(">>>", NodeUtil.opToStr(Token.URSH));
    Assert.assertEquals(">>", NodeUtil.opToStr(Token.RSH));
    Assert.assertEquals(">=", NodeUtil.opToStr(Token.GE));
    Assert.assertEquals(">", NodeUtil.opToStr(Token.GT));
    Assert.assertEquals("*", NodeUtil.opToStr(Token.MUL));
    Assert.assertEquals("/", NodeUtil.opToStr(Token.DIV));
    Assert.assertEquals("%", NodeUtil.opToStr(Token.MOD));
    Assert.assertEquals("~", NodeUtil.opToStr(Token.BITNOT));
    Assert.assertEquals("+", NodeUtil.opToStr(Token.ADD));
    Assert.assertEquals("-", NodeUtil.opToStr(Token.SUB));
    Assert.assertEquals("+", NodeUtil.opToStr(Token.POS));
    Assert.assertEquals("-", NodeUtil.opToStr(Token.NEG));
    Assert.assertEquals("=", NodeUtil.opToStr(Token.ASSIGN));
    Assert.assertEquals("|=", NodeUtil.opToStr(Token.ASSIGN_BITOR));
    Assert.assertEquals("^=", NodeUtil.opToStr(Token.ASSIGN_BITXOR));
    Assert.assertEquals("&=", NodeUtil.opToStr(Token.ASSIGN_BITAND));
    Assert.assertEquals("<<=", NodeUtil.opToStr(Token.ASSIGN_LSH));
    Assert.assertEquals(">>=", NodeUtil.opToStr(Token.ASSIGN_RSH));
    Assert.assertEquals(">>>=", NodeUtil.opToStr(Token.ASSIGN_URSH));
    Assert.assertEquals("+=", NodeUtil.opToStr(Token.ASSIGN_ADD));
    Assert.assertEquals("-=", NodeUtil.opToStr(Token.ASSIGN_SUB));
    Assert.assertEquals("*=", NodeUtil.opToStr(Token.ASSIGN_MUL));
    Assert.assertEquals("/=", NodeUtil.opToStr(Token.ASSIGN_DIV));
    Assert.assertEquals("%=", NodeUtil.opToStr(Token.ASSIGN_MOD));
    Assert.assertEquals("void", NodeUtil.opToStr(Token.VOID));
    Assert.assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    Assert.assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    Assert.assertNull(NodeUtil.opToStr(Token.CALL));

    Assert.assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
    try {
      NodeUtil.opToStrNoFail(Token.CALL);
      Assert.fail();
    } catch (Error e) {
    }
  }

  @Test
  public void testContainsTypeAndVisitor() {
    Node tree = new Node(Token.BLOCK,
        new Node(Token.EXPR_RESULT, new Node(Token.CALL, Node.newString(Token.NAME, "foo"))),
        new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "bar"))
    );

    Assert.assertTrue(NodeUtil.containsType(tree, Token.CALL));
    Assert.assertTrue(NodeUtil.containsCall(tree));
    Assert.assertFalse(NodeUtil.containsType(tree, Token.RETURN));
    Assert.assertEquals(2, NodeUtil.getNodeTypeReferenceCount(tree, Token.NAME, Predicates.<Node>alwaysTrue()));
    Assert.assertTrue(NodeUtil.isNameReferenced(tree, "foo"));
    Assert.assertEquals(1, NodeUtil.getNameReferenceCount(tree, "foo"));

    final List<Node> visitedPre = new ArrayList<Node>();
    NodeUtil.visitPreOrder(tree, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visitedPre.add(node);
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertFalse(visitedPre.isEmpty());

    final List<Node> visitedPost = new ArrayList<Node>();
    NodeUtil.visitPostOrder(tree, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visitedPost.add(node);
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertEquals(visitedPre.size(), visitedPost.size());
  }

  @Test
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node ifBlock = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "redeclaredVar"));
    ifBlock.addChildToBack(varNode);
    script.addChildToBack(ifBlock);

    NodeUtil.redeclareVarsInsideBranch(ifBlock);
    Assert.assertEquals(Token.VAR, script.getFirstChild().getType());
    Assert.assertEquals("redeclaredVar", script.getFirstChild().getFirstChild().getString());
  }

  @Test
  public void testNewFunctionAndQualifiedNameNodes() {
    CodingConvention convention = new ClosureCodingConvention();
    List<Node> params = new ArrayList<Node>();
    params.add(Node.newString(Token.NAME, "p1"));
    Node fn = NodeUtil.newFunctionNode("testFn", params, new Node(Token.BLOCK), 1, 2);
    Assert.assertEquals(Token.FUNCTION, fn.getType());
    Assert.assertEquals("testFn", fn.getFirstChild().getString());
    Assert.assertEquals(1, NodeUtil.getFnParameters(fn).getChildCount());

    Node qname = NodeUtil.newQualifiedNameNode(convention, "a.b.c", 10, 20);
    Assert.assertEquals(Token.GETPROP, qname.getType());
    Assert.assertEquals("c", qname.getLastChild().getString());
    Assert.assertEquals("a", NodeUtil.getRootOfQualifiedName(qname).getString());

    Node simpleQName = NodeUtil.newQualifiedNameNode(convention, "simple", 10, 20);
    Assert.assertEquals(Token.NAME, simpleQName.getType());
    Assert.assertEquals(simpleQName, NodeUtil.getRootOfQualifiedName(simpleQName));

    Node basis = Node.newString(Token.NAME, "orig");
    basis.setLineno(5);
    basis.setCharno(15);
    Node qnameDebug = NodeUtil.newQualifiedNameNode(convention, "x.y", basis, "origVar");
    Assert.assertEquals(5, qnameDebug.getLineno());
    Assert.assertEquals("origVar", qnameDebug.getProp(Node.ORIGINALNAME_PROP));
  }

  @Test
  public void testIsLatinAndPropertyName() {
    Assert.assertTrue(NodeUtil.isLatin("abcXYZ123_$"));
    Assert.assertFalse(NodeUtil.isLatin("abc\u0080def"));

    Assert.assertTrue(NodeUtil.isValidPropertyName("propName"));
    Assert.assertTrue(NodeUtil.isValidPropertyName("$foo_123"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("while"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("123bad"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("bad-prop"));
  }

  @Test
  public void testPrototypePropertyHelpers() {
    Node target = new Node(Token.GETPROP,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "MyClass"), Node.newString(Token.STRING, "prototype")),
        Node.newString(Token.STRING, "myMethod")
    );
    Node assignExpr = new Node(Token.EXPR_RESULT, new Node(Token.ASSIGN, target, Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isPrototypePropertyDeclaration(assignExpr));
    Assert.assertTrue(NodeUtil.isPrototypeProperty(target));
    Assert.assertEquals("MyClass", NodeUtil.getPrototypeClassName(target).getString());
    Assert.assertEquals("myMethod", NodeUtil.getPrototypePropertyName(target));
  }

  @Test
  public void testNewUndefinedAndNewVarNode() {
    Node basis = Node.newNumber(0);
    basis.setLineno(7);
    basis.setCharno(8);
    Node undef = NodeUtil.newUndefinedNode(basis);
    Assert.assertEquals(Token.VOID, undef.getType());
    Assert.assertEquals(7, undef.getLineno());
    Assert.assertEquals(8, undef.getCharno());

    Node varNode = NodeUtil.newVarNode("myVar", Node.newNumber(42));
    Assert.assertEquals(Token.VAR, varNode.getType());
    Assert.assertEquals("myVar", varNode.getFirstChild().getString());
    Assert.assertEquals(42.0, varNode.getFirstChild().getFirstChild().getDouble(), 0.0);
  }

  @Test
  public void testNewCallNodeAndEvaluatesToLocalValue() {
    Node callTarget = Node.newString(Token.NAME, "foo");
    Node call = NodeUtil.newCallNode(callTarget, Node.newNumber(1), Node.newNumber(2));
    Assert.assertEquals(Token.CALL, call.getType());
    Assert.assertTrue(call.getBooleanProp(Node.FREE_CALL));
    Assert.assertEquals(3, call.getChildCount());

    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.REGEXP)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK))));
    Assert.assertFalse(NodeUtil.evaluatesToLocalValue(Node.newString(Token.NAME, "nonImmutableVar")));
  }

  @Test
  public void testGetArgumentForFunctionAndCall() {
    Node p1 = Node.newString(Token.NAME, "param1");
    Node p2 = Node.newString(Token.NAME, "param2");
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "f"), new Node(Token.LP, p1, p2), new Node(Token.BLOCK));
    Assert.assertEquals(p1, NodeUtil.getArgumentForFunction(fn, 0));
    Assert.assertEquals(p2, NodeUtil.getArgumentForFunction(fn, 1));
    Assert.assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node arg1 = Node.newNumber(10);
    Node arg2 = Node.newNumber(20);
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "f"), arg1, arg2);
    Assert.assertEquals(arg1, NodeUtil.getArgumentForCallOrNew(call, 0));
    Assert.assertEquals(arg2, NodeUtil.getArgumentForCallOrNew(call, 1));
    Assert.assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));
  }

  @Test
  public void testSparseArray() {
    Node denseArray = new Node(Token.ARRAYLIT, Node.newNumber(1));
    Assert.assertFalse(NodeUtil.isSparseArray(denseArray));

    Node sparseArray = new Node(Token.ARRAYLIT, Node.newNumber(1));
    sparseArray.putProp(Node.SKIP_INDEXES_PROP, new int[]{0});
    Assert.assertTrue(NodeUtil.isSparseArray(sparseArray));
  }

  @Test
  public void testConstantByConvention() {
    CodingConvention convention = new ClosureCodingConvention();
    Node constName = Node.newString(Token.NAME, "CONSTANT_VAR");
    Assert.assertTrue(NodeUtil.isConstantByConvention(convention, constName, new Node(Token.EXPR_RESULT)));

    Node normalName = Node.newString(Token.NAME, "normalVar");
    Assert.assertFalse(NodeUtil.isConstantByConvention(convention, normalName, new Node(Token.EXPR_RESULT)));
  }

  @Test
  public void testGetSourceName() {
    Node child = Node.newNumber(1);
    Node parent = new Node(Token.EXPR_RESULT, child);
    parent.putProp(Node.SOURCENAME_PROP, "test.js");
    Assert.assertEquals("test.js", NodeUtil.getSourceName(child));
  }
}