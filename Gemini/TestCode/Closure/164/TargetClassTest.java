package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.testing.TestErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class ArrowTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType unknownType;
  private JSType allType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new TestErrorReporter(null, null));
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
  }

  @Test
  public void testConstructorDefaults() {
    ArrowType arrow = new ArrowType(registry, null, null);
    assertNotNull(arrow.parameters);
    assertEquals(unknownType, arrow.returnType);
    assertFalse(arrow.returnTypeInferred);

    ArrowType arrowInferred = new ArrowType(registry, null, null, true);
    assertTrue(arrowInferred.returnTypeInferred);
  }

  @Test
  public void testIsSubtypeNonArrow() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    assertFalse(arrow.isSubtype(numberType));
  }

  @Test
  public void testIsSubtypeReturnType() {
    ArrowType numReturn = new ArrowType(registry, null, numberType);
    ArrowType allReturn = new ArrowType(registry, null, allType);
    assertTrue(numReturn.isSubtype(allReturn));
    assertFalse(allReturn.isSubtype(numReturn));
  }

  @Test
  public void testIsSubtypeParametersContravariant() {
    Node paramNum1 = registry.createParameters(numberType);
    Node paramAll1 = registry.createParameters(allType);
    ArrowType fnAcceptsNum = new ArrowType(registry, paramNum1, numberType);
    ArrowType fnAcceptsAll = new ArrowType(registry, paramAll1, numberType);

    assertTrue(fnAcceptsAll.isSubtype(fnAcceptsNum));
    assertFalse(fnAcceptsNum.isSubtype(fnAcceptsAll));
  }

  @Test
  public void testIsSubtypeNullParamTypes() {
    Node param1 = new Node(105);
    Node paramHolder1 = new Node(83, param1);
    ArrowType arrow1 = new ArrowType(registry, paramHolder1, numberType);

    Node param2 = registry.createParameters(numberType);
    ArrowType arrow2 = new ArrowType(registry, param2, numberType);

    assertTrue(arrow1.isSubtype(arrow2));
    assertFalse(arrow2.isSubtype(arrow1));
  }

  @Test
  public void testIsSubtypeVarArgs() {
    Node varArgsNum = registry.createParametersWithVarArgs(numberType);
    Node varArgsAll = registry.createParametersWithVarArgs(allType);
    ArrowType fnVarNum = new ArrowType(registry, varArgsNum, numberType);
    ArrowType fnVarAll = new ArrowType(registry, varArgsAll, numberType);

    assertTrue(fnVarAll.isSubtype(fnVarNum));
    assertFalse(fnVarNum.isSubtype(fnVarAll));

    Node twoNums = registry.createParameters(numberType, numberType);
    ArrowType fnTwoNums = new ArrowType(registry, twoNums, numberType);
    assertTrue(fnVarNum.isSubtype(fnTwoNums));
  }

  @Test
  public void testHasEqualParameters() {
    Node p1 = registry.createParameters(numberType, stringType);
    Node p2 = registry.createParameters(numberType, stringType);
    Node p3 = registry.createParameters(numberType);
    Node p4 = registry.createParameters(stringType, numberType);

    ArrowType a1 = new ArrowType(registry, p1, numberType);
    ArrowType a2 = new ArrowType(registry, p2, numberType);
    ArrowType a3 = new ArrowType(registry, p3, numberType);
    ArrowType a4 = new ArrowType(registry, p4, numberType);

    assertTrue(a1.hasEqualParameters(a2));
    assertFalse(a1.hasEqualParameters(a3));
    assertFalse(a1.hasEqualParameters(a4));
  }

  @Test
  public void testHasEqualParametersWithUntypedNodes() {
    Node p1 = new Node(105);
    Node holder1 = new Node(83, p1);
    Node p2 = new Node(105);
    Node holder2 = new Node(83, p2);
    Node p3 = new Node(105);
    p3.setJSType(numberType);
    Node holder3 = new Node(83, p3);

    ArrowType a1 = new ArrowType(registry, holder1, numberType);
    ArrowType a2 = new ArrowType(registry, holder2, numberType);
    ArrowType a3 = new ArrowType(registry, holder3, numberType);

    assertTrue(a1.hasEqualParameters(a2));
    assertFalse(a1.hasEqualParameters(a3));
    assertFalse(a3.hasEqualParameters(a1));
  }

  @Test
  public void testIsEquivalentTo() {
    Node p1 = registry.createParameters(numberType);
    Node p2 = registry.createParameters(numberType);
    ArrowType a1 = new ArrowType(registry, p1, stringType);
    ArrowType a2 = new ArrowType(registry, p2, stringType);
    ArrowType a3 = new ArrowType(registry, p1, numberType);

    assertFalse(a1.isEquivalentTo(numberType));
    assertTrue(a1.isEquivalentTo(a2));
    assertFalse(a1.isEquivalentTo(a3));
  }

  @Test
  public void testHashCode() {
    Node p1 = registry.createParameters(numberType);
    Node p2 = registry.createParameters(numberType);
    ArrowType a1 = new ArrowType(registry, p1, stringType, false);
    ArrowType a2 = new ArrowType(registry, p2, stringType, false);
    ArrowType a3 = new ArrowType(registry, p1, stringType, true);

    assertEquals(a1.hashCode(), a2.hashCode());
    assertTrue(a1.hashCode() != a3.hashCode());

    Node untyped = new Node(105);
    Node holder = new Node(83, untyped);
    ArrowType a4 = new ArrowType(registry, holder, stringType);
    assertTrue(a4.hashCode() != 0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetLeastSupertype() {
    ArrowType a = new ArrowType(registry, null, numberType);
    a.getLeastSupertype(numberType);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetGreatestSubtype() {
    ArrowType a = new ArrowType(registry, null, numberType);
    a.getGreatestSubtype(numberType);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testTestForEquality() {
    ArrowType a = new ArrowType(registry, null, numberType);
    a.testForEquality(numberType);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testVisit() {
    ArrowType a = new ArrowType(registry, null, numberType);
    a.visit(null);
  }

  @Test
  public void testGetPossibleToBooleanOutcomes() {
    ArrowType a = new ArrowType(registry, null, numberType);
    assertEquals(BooleanLiteralSet.TRUE, a.getPossibleToBooleanOutcomes());
  }

  @Test
  public void testResolveInternal() {
    StaticScope<JSType> scope = null;
    TestErrorReporter reporter = new TestErrorReporter(null, null);

    Node p = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, p, stringType);
    JSType resolved = arrow.resolveInternal(reporter, scope);
    assertEquals(arrow, resolved);
  }

  @Test
  public void testHasUnknownParamsOrReturn() {
    ArrowType known = new ArrowType(registry, registry.createParameters(numberType), stringType);
    assertFalse(known.hasUnknownParamsOrReturn());

    ArrowType unknownRet = new ArrowType(registry, registry.createParameters(numberType), unknownType);
    assertTrue(unknownRet.hasUnknownParamsOrReturn());

    ArrowType unknownParam = new ArrowType(registry, registry.createParameters(unknownType), stringType);
    assertTrue(unknownParam.hasUnknownParamsOrReturn());

    Node untypedParam = new Node(105);
    Node holder = new Node(83, untypedParam);
    ArrowType untypedParamArrow = new ArrowType(registry, holder, stringType);
    assertTrue(untypedParamArrow.hasUnknownParamsOrReturn());
  }

  @Test
  public void testToStringHelper() {
    ArrowType arrow = new ArrowType(registry, null, numberType);
    assertNotNull(arrow.toStringHelper(false));
    assertNotNull(arrow.toStringHelper(true));
  }
}
