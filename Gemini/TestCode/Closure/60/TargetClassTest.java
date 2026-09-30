package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
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

public class NodeUtilTest {

  private static final CodingConvention CONVENTION = new DefaultCodingConvention();

  @Test
  public void testGetImpureAndPureBooleanValue() {
    Node trueNode = new Node(Token.TRUE);
    Node falseNode = new Node(Token.FALSE);
    Node nullNode = new Node(Token.NULL);
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    Node numZero = Node.newNumber(0);
    Node numOne = Node.newNumber(1);
    Node strEmpty = Node.newString("");
    Node strVal = Node.newString("hello");
    Node regexp = new Node(Token.REGEXP);

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(trueNode));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(falseNode));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nullNode));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidNode));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(numZero));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(numOne));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(strEmpty));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(strVal));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(regexp));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notTrue));

    Node nameUndefined = Node.newString(Token.NAME, "undefined");
    Node nameNaN = Node.newString(Token.NAME, "NaN");
    Node nameInfinity = Node.newString(Token.NAME, "Infinity");
    Node nameOther = Node.newString(Token.NAME, "other");
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameUndefined));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameNaN));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nameInfinity));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(nameOther));

    Node arrEmpty = new Node(Token.ARRAYLIT);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(arrEmpty));

    Node objEmpty = new Node(Token.OBJECTLIT);
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(objEmpty));

    // Impure boolean value tests
    Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assignNode));

    Node commaNode = new Node(Token.COMMA, Node.newNumber(0), Node.newNumber(1));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(commaNode));

    Node notNode = new Node(Token.NOT, Node.newNumber(0));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(notNode));

    Node andNode = new Node(Token.AND, Node.newNumber(1), Node.newNumber(0));
    Assert.assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = new Node(Token.OR, Node.newNumber(1), Node.newNumber(0));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hookSame = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(1), Node.newNumber(2));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookSame));

    Node hookDiff = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(1), Node.newNumber(0));
    Assert.assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDiff));

    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(arrEmpty));
    Assert.assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(objEmpty));
  }

  @Test
  public void testGetStringValue() {
    Assert.assertEquals("test", NodeUtil.getStringValue(Node.newString("test")));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "other")));

    Assert.assertEquals("123", NodeUtil.getStringValue(Node.newNumber(123)));
    Assert.assertEquals("123.5", NodeUtil.getStringValue(Node.newNumber(123.5)));
    Assert.assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    Assert.assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    Assert.assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    Assert.assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertEquals("false", NodeUtil.getStringValue(notTrue));
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    Assert.assertEquals("true", NodeUtil.getStringValue(notFalse));
    Node notUnknown = new Node(Token.NOT, Node.newString(Token.NAME, "unknown"));
    Assert.assertNull(NodeUtil.getStringValue(notUnknown));

    Node arr = new Node(Token.ARRAYLIT, Node.newString("a"), new Node(Token.NULL), Node.newString("b"));
    Assert.assertEquals("a,,b", NodeUtil.getStringValue(arr));

    Node arrWithEmpty = new Node(Token.ARRAYLIT, Node.newString("a"), new Node(Token.EMPTY), Node.newString("b"));
    Assert.assertEquals("a,,b", NodeUtil.getStringValue(arrWithEmpty));

    Node arrNull = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "unknown"));
    Assert.assertNull(NodeUtil.getStringValue(arrNull));

    Node obj = new Node(Token.OBJECTLIT);
    Assert.assertEquals("[object Object]", NodeUtil.getStringValue(obj));

    Assert.assertNull(NodeUtil.getStringValue(new Node(Token.FUNCTION)));
  }

  @Test
  public void testGetNumberValue() {
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    Assert.assertEquals(Double.valueOf(42.0), NodeUtil.getNumberValue(Node.newNumber(42.0)));

    Node voidPure = new Node(Token.VOID, Node.newNumber(0));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidPure)));

    Node voidImpure = new Node(Token.VOID, new Node(Token.CALL, Node.newString(Token.NAME, "foo")));
    Assert.assertNull(NodeUtil.getNumberValue(voidImpure));

    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    Assert.assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "foo")));

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    Assert.assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));

    Node negOther = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    Assert.assertNull(NodeUtil.getNumberValue(negOther));

    Node notZero = new Node(Token.NOT, Node.newNumber(0));
    Assert.assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notZero));
    Node notOne = new Node(Token.NOT, Node.newNumber(1));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notOne));
    Node notUnknown = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    Assert.assertNull(NodeUtil.getNumberValue(notUnknown));

    Assert.assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(Node.newString("123")));
    Assert.assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(new Node(Token.ARRAYLIT, Node.newString("123"))));
    Assert.assertTrue(Double.isNaN(NodeUtil.getNumberValue(new Node(Token.OBJECTLIT))));
  }

  @Test
  public void testGetStringNumberValueAndTrimJsWhiteSpace() {
    Assert.assertNull(NodeUtil.getStringNumberValue("hello\u000bworld"));
    Assert.assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   "));
    Assert.assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    Assert.assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0X10"));
    Assert.assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));
    Assert.assertNull(NodeUtil.getStringNumberValue("-0x10"));
    Assert.assertNull(NodeUtil.getStringNumberValue("+0x10"));
    Assert.assertNull(NodeUtil.getStringNumberValue("infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("-infinity"));
    Assert.assertNull(NodeUtil.getStringNumberValue("+infinity"));
    Assert.assertEquals(Double.valueOf(3.14), NodeUtil.getStringNumberValue(" 3.14 \n\t\r"));
    Assert.assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("invalid_num")));

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
  public void testGetFunctionNameAndNearestFunctionName() {
    Node fn = NodeUtil.newFunctionNode("fnName", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Assert.assertEquals("fnName", NodeUtil.getFunctionName(fn));

    Node var = new Node(Token.VAR);
    Node varName = Node.newString(Token.NAME, "vName");
    Node anonFn = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    varName.addChildToBack(anonFn);
    var.addChildToBack(varName);
    Assert.assertEquals("vName", NodeUtil.getFunctionName(anonFn));

    Node assign = new Node(Token.ASSIGN, NodeUtil.newQualifiedNameNode(CONVENTION, "pkg.sub.fn", 1, 0), anonFn);
    Assert.assertEquals("pkg.sub.fn", NodeUtil.getFunctionName(anonFn));

    Node objLit = new Node(Token.OBJECTLIT);
    Node strKey = Node.newString(Token.STRING, "keyName");
    strKey.addChildToBack(anonFn);
    objLit.addChildToBack(strKey);
    Assert.assertEquals("keyName", NodeUtil.getNearestFunctionName(anonFn));

    Node getKey = Node.newString(Token.GET, "getter");
    getKey.addChildToBack(anonFn);
    objLit.replaceChild(strKey, getKey);
    Assert.assertEquals("getter", NodeUtil.getNearestFunctionName(anonFn));

    Node setKey = Node.newString(Token.SET, "setter");
    setKey.addChildToBack(anonFn);
    objLit.replaceChild(getKey, setKey);
    Assert.assertEquals("setter", NodeUtil.getNearestFunctionName(anonFn));

    Node numKey = Node.newNumber(123);
    numKey.addChildToBack(anonFn);
    objLit.replaceChild(setKey, numKey);
    Assert.assertEquals("123", NodeUtil.getNearestFunctionName(anonFn));
  }

  @Test
  public void testIsImmutableAndLiteralValue() {
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newNumber(5)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, new Node(Token.TRUE))));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    Assert.assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    Assert.assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "other")));
    Assert.assertFalse(NodeUtil.isImmutableValue(new Node(Token.ARRAYLIT)));

    Node arrLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    Assert.assertTrue(NodeUtil.isLiteralValue(arrLit, false));
    arrLit.addChildToBack(Node.newString(Token.NAME, "x"));
    Assert.assertFalse(NodeUtil.isLiteralValue(arrLit, false));

    Node regExp = new Node(Token.REGEXP, Node.newString("abc"));
    Assert.assertTrue(NodeUtil.isLiteralValue(regExp, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    key.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(key);
    Assert.assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fnExpr = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    new Node(Token.EXPR_RESULT, fnExpr); // make it expression
    Assert.assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    Assert.assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test
  public void testIsValidDefineValue() {
    Set<String> defines = Sets.newHashSet("DEF_A", "DEF_B", "pkg.CONST");
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newString("hello"), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(12), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    Assert.assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    Assert.assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node bitNot = new Node(Token.BITNOT, Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isValidDefineValue(bitNot, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    Assert.assertTrue(NodeUtil.isValidDefineValue(not, defines));
    Node neg = new Node(Token.NEG, Node.newNumber(5));
    Assert.assertTrue(NodeUtil.isValidDefineValue(neg, defines));
    Node pos = new Node(Token.POS, Node.newNumber(5));
    Assert.assertTrue(NodeUtil.isValidDefineValue(pos, defines));

    Node defName = Node.newString(Token.NAME, "DEF_A");
    Assert.assertTrue(NodeUtil.isValidDefineValue(defName, defines));

    Node nonDefName = Node.newString(Token.NAME, "UNKNOWN");
    Assert.assertFalse(NodeUtil.isValidDefineValue(nonDefName, defines));

    Node qName = NodeUtil.newQualifiedNameNode(CONVENTION, "pkg.CONST", 1, 0);
    Assert.assertTrue(NodeUtil.isValidDefineValue(qName, defines));

    Node invalidOp = new Node(Token.HOOK, new Node(Token.TRUE), Node.newNumber(1), Node.newNumber(2));
    Assert.assertFalse(NodeUtil.isValidDefineValue(invalidOp, defines));
  }

  @Test
  public void testIsEmptyBlock() {
    Assert.assertFalse(NodeUtil.isEmptyBlock(new Node(Token.EXPR_RESULT)));
    Node emptyBlock = new Node(Token.BLOCK);
    Assert.assertTrue(NodeUtil.isEmptyBlock(emptyBlock));
    emptyBlock.addChildToBack(new Node(Token.EMPTY));
    Assert.assertTrue(NodeUtil.isEmptyBlock(emptyBlock));
    emptyBlock.addChildToBack(new Node(Token.EXPR_RESULT));
    Assert.assertFalse(NodeUtil.isEmptyBlock(emptyBlock));
  }

  @Test
  public void testIsSimpleOperator() {
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
    Assert.assertFalse(NodeUtil.isSimpleOperatorType(Token.VAR));

    Assert.assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
  }

  @Test
  public void testNewExpr() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    call.setLineno(10);
    call.setCharno(5);
    Node expr = NodeUtil.newExpr(call);
    Assert.assertEquals(Token.EXPR_RESULT, expr.getType());
    Assert.assertEquals(call, expr.getFirstChild());
    Assert.assertEquals(10, expr.getLineno());
    Assert.assertEquals(5, expr.getCharno());
  }

  @Test
  public void testMayHaveSideEffectsAndMayEffectMutableState() {
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(Node.newString("str")));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node objLit = new Node(Token.OBJECTLIT);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(objLit));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(objLit));

    Node arrLit = new Node(Token.ARRAYLIT);
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(arrLit));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(arrLit));

    Node newObj = new Node(Token.NEW, Node.newString(Token.NAME, "Object"));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(newObj));
    Assert.assertTrue(NodeUtil.mayEffectMutableState(newObj));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "Custom"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(newCustom));

    Node callFloor = new Node(Token.CALL, NodeUtil.newQualifiedNameNode(CONVENTION, "Math.floor", 1, 0), Node.newNumber(1.5));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(callFloor));

    Node callNoSideEffects = new Node(Token.CALL, Node.newString(Token.NAME, "String"), Node.newNumber(1.5));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(callNoSideEffects));

    Node callCustom = new Node(Token.CALL, Node.newString(Token.NAME, "customFn"));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(callCustom));

    Node assignLocal = new Node(Token.ASSIGN,
        new Node(Token.GETPROP, new Node(Token.ARRAYLIT), Node.newString("length")),
        Node.newNumber(0));
    Assert.assertFalse(NodeUtil.mayHaveSideEffects(assignLocal));

    Node assignGlobal = new Node(Token.ASSIGN,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "globalObj"), Node.newString("prop")),
        Node.newNumber(0));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(assignGlobal));

    Node assignName = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(0));
    Assert.assertTrue(NodeUtil.mayHaveSideEffects(assignName));
  }

  @Test
  public void testCallAndNewLocalResult() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "f"));
    call.setSideEffectFlags(Node.FLAG_LOCAL_RESULTS);
    Assert.assertTrue(NodeUtil.callHasLocalResult(call));
    call.setSideEffectFlags(Node.NO_SIDE_EFFECTS);
    Assert.assertFalse(NodeUtil.callHasLocalResult(call));

    Node newNode = new Node(Token.NEW, Node.newString(Token.NAME, "f"));
    newNode.setSideEffectFlags(Node.FLAG_MODIFIES_THIS_ONLY);
    Assert.assertTrue(NodeUtil.newHasLocalResult(newNode));
  }

  @Test
  public void testNodeTypeMayHaveSideEffects() {
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.ASSIGN)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DELPROP)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.DEC)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.INC)));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.THROW)));
    Assert.assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(Node.newNumber(1)));

    Node nameWithChild = Node.newString(Token.NAME, "a");
    nameWithChild.addChildToBack(Node.newNumber(1));
    Assert.assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(nameWithChild));

    Node nameNoChild = Node.newString(Token.NAME, "a");
    Assert.assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(nameNoChild));
  }

  @Test
  public void testCanBeSideEffected() {
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.CALL, Node.newString(Token.NAME, "f"))));
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.NEW, Node.newString(Token.NAME, "f"))));
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"))));
    Assert.assertTrue(NodeUtil.canBeSideEffected(new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newNumber(0))));

    Node nameA = Node.newString(Token.NAME, "a");
    Assert.assertTrue(NodeUtil.canBeSideEffected(nameA));
    Assert.assertFalse(NodeUtil.canBeSideEffected(nameA, Collections.singleton("a")));

    Node fnExpr = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    new Node(Token.EXPR_RESULT, fnExpr);
    Assert.assertFalse(NodeUtil.canBeSideEffected(fnExpr));
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
      Assert.fail("Expected Error for unknown precedence");
    } catch (Error e) {
      // expected
    }
  }

  @Test
  public void testIsNumericResultAndIsBooleanResult() {
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.BITNOT, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.BITOR, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(new Node(Token.SUB, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "NaN")));
    Assert.assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "Infinity")));
    Assert.assertFalse(NodeUtil.isNumericResult(Node.newString("str")));

    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.FALSE)));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, Node.newNumber(1))));
    Assert.assertTrue(NodeUtil.isBooleanResult(new Node(Token.DELPROP, Node.newString(Token.NAME, "x"))));
    Assert.assertFalse(NodeUtil.isBooleanResult(Node.newNumber(1)));
  }

  @Test
  public void testIsNullAndUndefined() {
    Assert.assertTrue(NodeUtil.isNull(new Node(Token.NULL)));
    Assert.assertFalse(NodeUtil.isNull(new Node(Token.TRUE)));

    Assert.assertTrue(NodeUtil.isUndefined(new Node(Token.VOID)));
    Assert.assertTrue(NodeUtil.isUndefined(Node.newString(Token.NAME, "undefined")));
    Assert.assertFalse(NodeUtil.isUndefined(Node.newString(Token.NAME, "other")));

    Assert.assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.NULL)));
    Assert.assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.VOID)));
    Assert.assertFalse(NodeUtil.isNullOrUndefined(new Node(Token.FALSE)));
  }

  @Test
  public void testMayBeString() {
    Assert.assertTrue(NodeUtil.mayBeString(Node.newString("a")));
    Assert.assertFalse(NodeUtil.mayBeString(Node.newNumber(1)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.TRUE)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.NULL)));
    Assert.assertFalse(NodeUtil.mayBeString(new Node(Token.VOID)));

    Node hookStrOrNum = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newString("a"), Node.newNumber(1));
    Assert.assertTrue(NodeUtil.mayBeString(hookStrOrNum, true));

    Node hookNumOnly = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(2), Node.newNumber(1));
    Assert.assertFalse(NodeUtil.mayBeString(hookNumOnly, true));
  }

  @Test
  public void testAssociativeAndCommutative() {
    Assert.assertTrue(NodeUtil.isAssociative(Token.MUL));
    Assert.assertTrue(NodeUtil.isAssociative(Token.AND));
    Assert.assertTrue(NodeUtil.isAssociative(Token.OR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITOR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITXOR));
    Assert.assertTrue(NodeUtil.isAssociative(Token.BITAND));
    Assert.assertFalse(NodeUtil.isAssociative(Token.ADD));
    Assert.assertFalse(NodeUtil.isAssociative(Token.SUB));

    Assert.assertTrue(NodeUtil.isCommutative(Token.MUL));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITOR));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITXOR));
    Assert.assertTrue(NodeUtil.isCommutative(Token.BITAND));
    Assert.assertFalse(NodeUtil.isCommutative(Token.AND));
    Assert.assertFalse(NodeUtil.isCommutative(Token.ADD));
  }

  @Test
  public void testAssignmentOps() {
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITOR)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITXOR)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_BITAND)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_LSH)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_RSH)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_URSH)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_SUB)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MUL)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_DIV)));
    Assert.assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_MOD)));
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
  public void testControlStructuresAndLoops() {
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    Assert.assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    Assert.assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));

    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.isForIn(forNode));
    Assert.assertEquals(forNode.getLastChild(), NodeUtil.getLoopCodeBlock(forNode));

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.TRUE));
    Assert.assertEquals(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));

    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    Assert.assertEquals(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));
    Assert.assertNull(NodeUtil.getLoopCodeBlock(new Node(Token.IF)));

    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    Assert.assertTrue(NodeUtil.isControlStructure(new Node(Token.TRY)));
    Assert.assertFalse(NodeUtil.isControlStructure(new Node(Token.EXPR_RESULT)));

    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK), new Node(Token.BLOCK));
    Assert.assertEquals(ifNode.getFirstChild(), NodeUtil.getConditionExpression(ifNode));
    Assert.assertEquals(whileNode.getFirstChild(), NodeUtil.getConditionExpression(whileNode));
    Assert.assertEquals(doNode.getLastChild(), NodeUtil.getConditionExpression(doNode));
    Assert.assertNull(NodeUtil.getConditionExpression(forNode));

    Node standardFor = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.TRUE), new Node(Token.EMPTY), new Node(Token.BLOCK));
    Assert.assertEquals(standardFor.getFirstChild().getNext(), NodeUtil.getConditionExpression(standardFor));
    Assert.assertNull(NodeUtil.getConditionExpression(new Node(Token.CASE, new Node(Token.NAME, "c"))));

    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(whileNode, whileNode.getLastChild()));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(doNode, doNode.getFirstChild()));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getLastChild()));
    Assert.assertTrue(NodeUtil.isControlStructureCodeBlock(new Node(Token.DEFAULT), new Node(Token.BLOCK)));
  }

  @Test
  public void testIsStatementAndStatementBlock() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node expr = new Node(Token.EXPR_RESULT);
    script.addChildToBack(expr);

    Assert.assertTrue(NodeUtil.isStatementBlock(script));
    Assert.assertTrue(NodeUtil.isStatementBlock(block));
    Assert.assertFalse(NodeUtil.isStatementBlock(expr));

    Assert.assertTrue(NodeUtil.isStatement(expr));
    Assert.assertTrue(NodeUtil.isStatementParent(script));
    Assert.assertTrue(NodeUtil.isStatementParent(block));
    Assert.assertTrue(NodeUtil.isStatementParent(new Node(Token.LABEL)));
    Assert.assertFalse(NodeUtil.isStatementParent(new Node(Token.ASSIGN)));

    Assert.assertTrue(NodeUtil.isSwitchCase(new Node(Token.CASE)));
    Assert.assertTrue(NodeUtil.isSwitchCase(new Node(Token.DEFAULT)));
    Assert.assertFalse(NodeUtil.isSwitchCase(new Node(Token.IF)));

    Assert.assertTrue(NodeUtil.isReferenceName(Node.newString(Token.NAME, "foo")));
    Assert.assertFalse(NodeUtil.isReferenceName(Node.newString(Token.NAME, "")));
    Assert.assertFalse(NodeUtil.isReferenceName(Node.newNumber(1)));

    Assert.assertTrue(NodeUtil.isLabelName(new Node(Token.LABEL_NAME)));
    Assert.assertFalse(NodeUtil.isLabelName(new Node(Token.NAME)));
  }

  @Test
  public void testTryCatchFinallyMethodsAndRemoveChild() {
    Node tryNode = new Node(Token.TRY, new Node(Token.BLOCK), new Node(Token.BLOCK), new Node(Token.BLOCK));
    Assert.assertTrue(NodeUtil.hasFinally(tryNode));
    Assert.assertTrue(NodeUtil.isTryFinallyNode(tryNode, tryNode.getLastChild()));

    Node catchContainer = tryNode.getFirstChild().getNext();
    Assert.assertTrue(NodeUtil.isTryCatchNodeContainer(catchContainer));

    Node catchBlock = NodeUtil.getCatchBlock(tryNode);
    Assert.assertEquals(catchContainer, catchBlock);
    Assert.assertFalse(NodeUtil.hasCatchHandler(catchBlock));

    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    catchBlock.addChildToBack(catchNode);
    Assert.assertTrue(NodeUtil.hasCatchHandler(catchBlock));

    // Test removeChild on finally
    Node finallyBlock = tryNode.getLastChild();
    NodeUtil.removeChild(tryNode, finallyBlock);
    Assert.assertEquals(2, tryNode.getChildCount());

    // Test maybeAddFinally
    NodeUtil.maybeAddFinally(tryNode);
    Assert.assertEquals(3, tryNode.getChildCount());

    // Test removeChild on block inside script
    Node script = new Node(Token.SCRIPT);
    Node innerBlock = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newNumber(1)));
    script.addChildToBack(innerBlock);
    Assert.assertTrue(NodeUtil.tryMergeBlock(innerBlock));
    Assert.assertEquals(1, script.getChildCount());
    Assert.assertEquals(Token.EXPR_RESULT, script.getFirstChild().getType());

    // Test removeChild on VAR
    Node varNode = NodeUtil.newVarNode("x", Node.newNumber(1));
    script.addChildToBack(varNode);
    Node nameChild = varNode.getFirstChild();
    NodeUtil.removeChild(varNode, nameChild);
    Assert.assertFalse(script.hasChildren());
  }

  @Test
  public void testFunctionPredicates() {
    Node fn = NodeUtil.newFunctionNode("testFn", Arrays.asList(Node.newString(Token.NAME, "p1")), new Node(Token.BLOCK), 1, 0);
    Assert.assertTrue(NodeUtil.isFunction(fn));
    Assert.assertNotNull(NodeUtil.getFunctionBody(fn));
    Assert.assertNotNull(NodeUtil.getFunctionParameters(fn));

    Node script = new Node(Token.SCRIPT, fn);
    Assert.assertTrue(NodeUtil.isFunctionDeclaration(fn));
    Assert.assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));
    Assert.assertFalse(NodeUtil.isFunctionExpression(fn));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "f"), fn);
    script.replaceChild(fn, assign);
    Assert.assertFalse(NodeUtil.isFunctionDeclaration(fn));
    Assert.assertTrue(NodeUtil.isFunctionExpression(fn));
    Assert.assertTrue(NodeUtil.isEmptyFunctionExpression(fn));

    Node body = NodeUtil.getFunctionBody(fn);
    body.addChildToBack(NodeUtil.newExpr(Node.newString(Token.NAME, "arguments")));
    Assert.assertTrue(NodeUtil.isVarArgsFunction(fn));

    Node callMethod = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("call")),
        Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isFunctionObjectCall(callMethod));
    Assert.assertTrue(NodeUtil.isSimpleFunctionObjectCall(callMethod));
    Assert.assertTrue(NodeUtil.isFunctionObjectCallOrApply(callMethod));
    Assert.assertFalse(NodeUtil.isFunctionObjectApply(callMethod));

    Node applyMethod = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("apply")),
        Node.newNumber(1));
    Assert.assertTrue(NodeUtil.isFunctionObjectApply(applyMethod));
    Assert.assertTrue(NodeUtil.isFunctionObjectCallOrApply(applyMethod));
  }

  @Test
  public void testLValueAndAssignedValue() {
    Node var = new Node(Token.VAR);
    Node name = Node.newString(Token.NAME, "x");
    Node value = Node.newNumber(42);
    name.addChildToBack(value);
    var.addChildToBack(name);

    Assert.assertTrue(NodeUtil.isVar(var));
    Assert.assertTrue(NodeUtil.isVarDeclaration(name));
    Assert.assertTrue(NodeUtil.isLValue(name));
    Assert.assertTrue(NodeUtil.isVarOrSimpleAssignLhs(name, var));
    Assert.assertEquals(value, NodeUtil.getAssignedValue(name));

    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "y"), Node.newNumber(10));
    Assert.assertTrue(NodeUtil.isAssign(assign));
    Assert.assertTrue(NodeUtil.isVarOrSimpleAssignLhs(assign.getFirstChild(), assign));
    Assert.assertEquals(assign.getLastChild(), NodeUtil.getAssignedValue(assign.getFirstChild()));

    Node exprAssign = new Node(Token.EXPR_RESULT, assign);
    Assert.assertTrue(NodeUtil.isExprAssign(exprAssign));

    Node exprCall = new Node(Token.EXPR_RESULT, new Node(Token.CALL, Node.newString(Token.NAME, "f")));
    Assert.assertTrue(NodeUtil.isExprCall(exprCall));
  }

  @Test
  public void testObjectLitKeysAndTypes() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node strKey = Node.newString(Token.STRING, "k1");
    Node getKey = Node.newString(Token.GET, "k2");
    Node setKey = Node.newString(Token.SET, "k3");
    objLit.addChildToBack(strKey);
    objLit.addChildToBack(getKey);
    objLit.addChildToBack(setKey);

    Assert.assertTrue(NodeUtil.isObjectLitKey(strKey, objLit));
    Assert.assertTrue(NodeUtil.isObjectLitKey(getKey, objLit));
    Assert.assertTrue(NodeUtil.isGetOrSetKey(getKey));
    Assert.assertTrue(NodeUtil.isGetOrSetKey(setKey));
    Assert.assertFalse(NodeUtil.isGetOrSetKey(strKey));

    Assert.assertEquals("k1", NodeUtil.getObjectLitKeyName(strKey));
    Assert.assertEquals("k2", NodeUtil.getObjectLitKeyName(getKey));
    Assert.assertEquals("k3", NodeUtil.getObjectLitKeyName(setKey));
  }

  @Test
  public void testOpToStr() {
    Assert.assertEquals("+", NodeUtil.opToStr(Token.ADD));
    Assert.assertEquals("-", NodeUtil.opToStr(Token.SUB));
    Assert.assertEquals("*", NodeUtil.opToStr(Token.MUL));
    Assert.assertEquals("/", NodeUtil.opToStr(Token.DIV));
    Assert.assertEquals("%", NodeUtil.opToStr(Token.MOD));
    Assert.assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    Assert.assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    Assert.assertEquals("==", NodeUtil.opToStr(Token.EQ));
    Assert.assertEquals("!=", NodeUtil.opToStr(Token.NE));
    Assert.assertEquals("&&", NodeUtil.opToStr(Token.AND));
    Assert.assertEquals("||", NodeUtil.opToStr(Token.OR));
    Assert.assertEquals("!", NodeUtil.opToStr(Token.NOT));
    Assert.assertEquals("~", NodeUtil.opToStr(Token.BITNOT));
    Assert.assertEquals("&", NodeUtil.opToStr(Token.BITAND));
    Assert.assertEquals("|", NodeUtil.opToStr(Token.BITOR));
    Assert.assertEquals("^", NodeUtil.opToStr(Token.BITXOR));
    Assert.assertEquals("<<", NodeUtil.opToStr(Token.LSH));
    Assert.assertEquals(">>", NodeUtil.opToStr(Token.RSH));
    Assert.assertEquals(">>>", NodeUtil.opToStr(Token.URSH));
    Assert.assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    Assert.assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    Assert.assertEquals("void", NodeUtil.opToStr(Token.VOID));
    Assert.assertEquals("in", NodeUtil.opToStr(Token.IN));
    Assert.assertNull(NodeUtil.opToStr(-999));

    Assert.assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
    try {
      NodeUtil.opToStrNoFail(-999);
      Assert.fail("Expected Error on invalid op");
    } catch (Error e) {
      // expected
    }
  }

  @Test
  public void testQualifiedNameAndPrototypeHelpers() {
    Node qName = NodeUtil.newQualifiedNameNode(CONVENTION, "a.b.c", 1, 2);
    Assert.assertEquals(Token.GETPROP, qName.getType());
    Assert.assertEquals(Token.NAME, NodeUtil.getRootOfQualifiedName(qName).getType());
    Assert.assertEquals("a", NodeUtil.getRootOfQualifiedName(qName).getString());

    Node protoProp = NodeUtil.newQualifiedNameNode(CONVENTION, "MyClass.prototype.myMethod", 1, 2);
    Assert.assertTrue(NodeUtil.isPrototypeProperty(protoProp));
    Assert.assertEquals("myMethod", NodeUtil.getPrototypePropertyName(protoProp));
    Assert.assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoProp).getQualifiedName());

    Node exprAssign = NodeUtil.newExpr(new Node(Token.ASSIGN, protoProp, Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));
  }

  @Test
  public void testLatinAndValidPropertyName() {
    Assert.assertTrue(NodeUtil.isLatin("abcXYZ_123"));
    Assert.assertFalse(NodeUtil.isLatin("abc\u0080def"));
    Assert.assertTrue(NodeUtil.isValidPropertyName("validProp"));
    Assert.assertFalse(NodeUtil.isValidPropertyName("class")); // keyword
    Assert.assertFalse(NodeUtil.isValidPropertyName("123abc")); // invalid identifier
    Assert.assertFalse(NodeUtil.isValidPropertyName("prop\u0080")); // non-latin
  }

  @Test
  public void testTreeTraversalAndCounts() {
    Node root = new Node(Token.BLOCK,
        NodeUtil.newVarNode("v1", Node.newNumber(1)),
        NodeUtil.newVarNode("v2", Node.newNumber(2)),
        NodeUtil.newExpr(Node.newString(Token.NAME, "v1"))
    );

    Assert.assertTrue(NodeUtil.isNameReferenced(root, "v1"));
    Assert.assertFalse(NodeUtil.isNameReferenced(root, "v3"));
    Assert.assertEquals(2, NodeUtil.getNameReferenceCount(root, "v1"));
    Assert.assertEquals(1, NodeUtil.getNameReferenceCount(root, "v2"));
    Assert.assertEquals(2, NodeUtil.getNodeTypeReferenceCount(root, Token.VAR, Predicates.<Node>alwaysTrue()));
    Assert.assertTrue(NodeUtil.containsType(root, Token.VAR));

    Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(root);
    Assert.assertEquals(2, vars.size());

    final List<Integer> visited = new ArrayList<Integer>();
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visited.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertFalse(visited.isEmpty());

    final List<Integer> postVisited = new ArrayList<Integer>();
    NodeUtil.visitPostOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        postVisited.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    Assert.assertEquals(visited.size(), postVisited.size());
  }

  @Test
  public void testEvaluatesToLocalValue() {
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(1)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("s")));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.REGEXP)));
    Assert.assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2))));
    Assert.assertFalse(NodeUtil.evaluatesToLocalValue(Node.newString(Token.NAME, "globalVar")));
  }

  @Test
  public void testGetArgumentForFunctionAndCall() {
    Node p1 = Node.newString(Token.NAME, "p1");
    Node p2 = Node.newString(Token.NAME, "p2");
    Node fn = NodeUtil.newFunctionNode("f", Arrays.asList(p1, p2), new Node(Token.BLOCK), 1, 0);

    Assert.assertEquals(p1, NodeUtil.getArgumentForFunction(fn, 0));
    Assert.assertEquals(p2, NodeUtil.getArgumentForFunction(fn, 1));
    Assert.assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node arg1 = Node.newNumber(10);
    Node arg2 = Node.newNumber(20);
    Node call = NodeUtil.newCallNode(Node.newString(Token.NAME, "f"), arg1, arg2);

    Assert.assertEquals(arg1, NodeUtil.getArgumentForCallOrNew(call, 0));
    Assert.assertEquals(arg2, NodeUtil.getArgumentForCallOrNew(call, 1));
    Assert.assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));
  }

  @Test
  public void testUndefinedNodeAndNewCallNode() {
    Node src = Node.newString(Token.NAME, "src");
    src.setLineno(5);
    Node undef = NodeUtil.newUndefinedNode(src);
    Assert.assertEquals(Token.VOID, undef.getType());
    Assert.assertEquals(5, undef.getLineno());

    Node call = NodeUtil.newCallNode(Node.newString(Token.NAME, "target"), Node.newNumber(1));
    Assert.assertTrue(call.getBooleanProp(Node.FREE_CALL));
    Assert.assertEquals(2, call.getChildCount());
  }

  @Test
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    script.addChildToBack(block);
    Node var1 = NodeUtil.newVarNode("v1", Node.newNumber(1));
    block.addChildToBack(var1);

    NodeUtil.redeclareVarsInsideBranch(block);
    Assert.assertEquals(2, script.getChildCount());
    Assert.assertEquals(Token.VAR, script.getFirstChild().getType());
  }

  @Test
  public void testReferencesThisAndWithinLoop() {
    Node thisNode = new Node(Token.THIS);
    Node block = new Node(Token.BLOCK, NodeUtil.newExpr(thisNode));
    Assert.assertTrue(NodeUtil.referencesThis(block));

    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), block);
    Assert.assertTrue(NodeUtil.isWithinLoop(thisNode));
  }
}