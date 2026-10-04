package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.testing.TestErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class ArrowTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType unknownType;
  private JSType noType;
  private TemplateType templateType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new TestErrorReporter(null, null));
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    templateType = new TemplateType(registry, "T");
  }

  @Test
  public void testConstructorWithNulls() {
    ArrowType arrow = new ArrowType(registry, null, null);
    assertNotNull(arrow.parameters);
    assertNotNull(arrow.returnType);
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
  public void testIsSubtypeReturnMismatch() {
    ArrowType arrow1 = new ArrowType(registry, null, numberType);
    ArrowType arrow2 = new ArrowType(registry, null, stringType);
    assertFalse(arrow1.isSubtype(arrow2));
  }

  @Test
  public void testIsSubtypeParamContravariantAndLengths() {
    Node p1 = registry.createParameters(numberType);
    Node p2 = registry.createParameters(numberType, stringType);
    ArrowType arrow1 = new ArrowType(registry, p1, numberType);
    ArrowType arrow2 = new ArrowType(registry, p2, numberType);

    assertTrue(arrow1.isSubtype(arrow2));
    assertFalse(arrow2.isSubtype(arrow1));

    Node pNullType = new Node(105);
    pNullType.addChildToBack(new Node(38));
    ArrowType arrowNullParam = new ArrowType(registry, pNullType, numberType);
    assertFalse(arrow1.isSubtype(arrowNullParam));
  }

  @Test
  public void testIsSubtypeOptionalAndVarArgs() {
    Node optParam = registry.createOptionalParameters(numberType);
    Node reqParam = registry.createParameters(numberType);
    Node varParam = registry.createParametersWithVarArgs(numberType);
    Node varUnknown = registry.createParametersWithVarArgs(unknownType);
    Node varNoType = registry.createParametersWithVarArgs(noType);

    ArrowType arrowOpt = new ArrowType(registry, optParam, numberType);
    ArrowType arrowReq = new ArrowType(registry, reqParam, numberType);
    ArrowType arrowVar = new ArrowType(registry, varParam, numberType);
    ArrowType arrowVarUnknown = new ArrowType(registry, varUnknown, numberType);
    ArrowType arrowVarNoType = new ArrowType(registry, varNoType, numberType);

    assertFalse(arrowReq.isSubtype(arrowOpt));
    assertFalse(arrowReq.isSubtype(arrowVar));
    assertTrue(arrowReq.isSubtype(arrowVarUnknown));
    assertTrue(arrowReq.isSubtype(arrowVarNoType));

    ArrowType arrowVar1 = new ArrowType(registry, registry.createParametersWithVarArgs(numberType), numberType);
    ArrowType arrowVar2 = new ArrowType(registry, registry.createParametersWithVarArgs(numberType), numberType);
    assertTrue(arrowVar1.isSubtype(arrowVar2));
  }

  @Test
  public void testHasEqualParameters() {
    Node p1 = registry.createParameters(numberType, stringType);
    Node p2 = registry.createParameters(numberType, stringType);
    Node p3 = registry.createParameters(numberType, numberType);
    Node p4 = registry.createParameters(numberType);

    ArrowType a1 = new ArrowType(registry, p1, numberType);
    ArrowType a2 = new ArrowType(registry, p2, numberType);
    ArrowType a3 = new ArrowType(registry, p3, numberType);
    ArrowType a4 = new ArrowType(registry, p4, numberType);

    assertTrue(a1.hasEqualParameters(a2, false));
    assertFalse(a1.hasEqualParameters(a3, false));
    assertFalse(a1.hasEqualParameters(a4, false));
    assertFalse(a4.hasEqualParameters(a1, false));

    Node pNoType1 = new Node(105);
    pNoType1.addChildToBack(new Node(38));
    Node pNoType2 = new Node(105);
    pNoType2.addChildToBack(new Node(38));
    ArrowType aNo1 = new ArrowType(registry, pNoType1, numberType);
    ArrowType aNo2 = new ArrowType(registry, pNoType2, numberType);
    assertTrue(aNo1.hasEqualParameters(aNo2, false));
    assertFalse(aNo1.hasEqualParameters(a4, false));
  }

  @Test
  public void testCheckArrowEquivalenceHelper() {
    ArrowType a1 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType a2 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    ArrowType a3 = new ArrowType(registry, registry.createParameters(numberType), stringType);

    assertTrue(a1.checkArrowEquivalenceHelper(a2, false));
    assertFalse(a1.checkArrowEquivalenceHelper(a3, false));
  }

  @Test
  public void testHashCode() {
    ArrowType a1 = new ArrowType(registry, registry.createParameters(numberType), numberType, false);
    ArrowType a2 = new ArrowType(registry, registry.createParameters(numberType), numberType, true);
    assertTrue(a1.hashCode() != 0);
    assertEquals(a1.hashCode() + 1, a2.hashCode());

    Node pNullType = new Node(105);
    pNullType.addChildToBack(new Node(38));
    ArrowType aNullParam = new ArrowType(registry, pNullType, null, false);
    assertTrue(aNullParam.hashCode() >= 0);
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
    NamedType namedParam = new NamedType(registry, "NamedParam", "test.js", 1, 1);
    NamedType namedReturn = new NamedType(registry, "NamedReturn", "test.js", 1, 1);
    Node paramNode = Node.newString(38, "p");
    paramNode.setJSType(namedParam);
    Node params = new Node(105, paramNode);

    ArrowType arrow = new ArrowType(registry, params, namedReturn);
    ErrorReporter reporter = new TestErrorReporter(null, null);
    arrow.resolveInternal(reporter, null);

    assertNotNull(arrow.returnType);
    assertNotNull(arrow.parameters.getFirstChild().getJSType());
  }

  @Test
  public void testHasUnknownParamsOrReturn() {
    ArrowType a1 = new ArrowType(registry, registry.createParameters(numberType), numberType);
    assertFalse(a1.hasUnknownParamsOrReturn());

    ArrowType a2 = new ArrowType(registry, registry.createParameters(unknownType), numberType);
    assertTrue(a2.hasUnknownParamsOrReturn());

    ArrowType a3 = new ArrowType(registry, registry.createParameters(numberType), unknownType);
    assertTrue(a3.hasUnknownParamsOrReturn());

    Node pNullType = new Node(105);
    pNullType.addChildToBack(new Node(38));
    ArrowType a4 = new ArrowType(registry, pNullType, numberType);
    assertTrue(a4.hasUnknownParamsOrReturn());
  }

  @Test
  public void testToStringHelper() {
    ArrowType a = new ArrowType(registry, null, numberType);
    assertEquals("[ArrowType]", a.toStringHelper(false));
    assertEquals("[ArrowType]", a.toStringHelper(true));
  }

  @Test
  public void testHasAnyTemplateInternal() {
    ArrowType noTemplate = new ArrowType(registry, registry.createParameters(numberType), numberType);
    assertFalse(noTemplate.hasAnyTemplateInternal());

    ArrowType returnTemplate = new ArrowType(registry, registry.createParameters(numberType), templateType);
    assertTrue(returnTemplate.hasAnyTemplateInternal());

    ArrowType paramTemplate = new ArrowType(registry, registry.createParameters(templateType), numberType);
    assertTrue(paramTemplate.hasAnyTemplateInternal());

    Node pNullType = new Node(105);
    pNullType.addChildToBack(new Node(38));
    ArrowType nullParamType = new ArrowType(registry, pNullType, numberType);
    assertFalse(nullParamType.hasAnyTemplateInternal());
  }
}
