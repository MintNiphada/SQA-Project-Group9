package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSType.TypePair;
import org.junit.Before;
import org.junit.Test;

public class JSTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType allType;
  private JSType unknownType;
  private JSType checkedUnknownType;
  private ObjectType objectType;
  private JSType noType;
  private JSType noObjectType;
  private JSType noResolvedType;
  private FunctionType fnType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    checkedUnknownType = registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE);
    objectType = (ObjectType) registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    fnType = registry.getNativeFunctionType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
  }

  @Test
  public void testAlphaComparator() {
    assertTrue(JSType.ALPHA.compare(numberType, stringType) < 0);
    assertTrue(JSType.ALPHA.compare(stringType, numberType) > 0);
    assertEquals(0, JSType.ALPHA.compare(numberType, numberType));
  }

  @Test
  public void testGetNativeType() {
    assertSame(numberType, numberType.getNativeType(JSTypeNative.NUMBER_TYPE));
  }

  @Test
  public void testGetJSDocInfoAndDisplayName() {
    assertNull(numberType.getJSDocInfo());
    assertNull(numberType.getDisplayName());
    assertFalse(numberType.hasDisplayName());
  }

  @Test
  public void testHasProperty() {
    assertFalse(numberType.hasProperty("foo"));
  }

  @Test
  public void testTypeClassifiers() {
    assertTrue(noType.isNoType());
    assertFalse(numberType.isNoType());

    assertTrue(noResolvedType.isNoResolvedType());
    assertFalse(numberType.isNoResolvedType());

    assertTrue(noObjectType.isNoObjectType());
    assertFalse(numberType.isNoObjectType());

    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
    JSType leastFn = registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE);
    assertTrue(leastFn.isEmptyType());
    assertFalse(numberType.isEmptyType());

    assertFalse(numberType.isNumberObjectType());
    assertTrue(numberType.isNumberValueType());

    assertFalse(numberType.isFunctionPrototypeType());
    assertFalse(stringType.isStringObjectType());
    assertTrue(stringType.isStringValueType());
    assertFalse(numberType.isTheObjectType());

    assertTrue(stringType.isString());
    assertFalse(numberType.isString());

    assertTrue(numberType.isNumber());
    assertFalse(stringType.isNumber());

    assertFalse(numberType.isArrayType());
    assertFalse(booleanType.isBooleanObjectType());
    assertTrue(booleanType.isBooleanValueType());
    assertFalse(numberType.isRegexpType());
    assertFalse(numberType.isDateType());

    assertTrue(nullType.isNullType());
    assertFalse(numberType.isNullType());

    assertTrue(voidType.isVoidType());
    assertFalse(numberType.isVoidType());

    assertTrue(allType.isAllType());
    assertFalse(numberType.isAllType());

    assertTrue(unknownType.isUnknownType());
    assertFalse(numberType.isUnknownType());

    assertTrue(checkedUnknownType.isCheckedUnknownType());
    assertFalse(numberType.isCheckedUnknownType());

    assertFalse(numberType.isGlobalThisType());
    JSType globalThis = registry.getNativeType(JSTypeNative.GLOBAL_THIS);
    assertTrue(globalThis.isGlobalThisType());

    assertFalse(numberType.isNamedType());
    assertFalse(numberType.isRecordType());
    assertNull(numberType.toMaybeRecordType());
    assertFalse(numberType.isParameterizedType());
    assertNull(numberType.toMaybeParameterizedType());
    assertNull(JSType.toMaybeParameterizedType(null));
    assertNull(JSType.toMaybeParameterizedType(numberType));
    assertFalse(numberType.isTemplateType());
    assertNull(numberType.toMaybeTemplateType());
    assertNull(JSType.toMaybeTemplateType(null));
    assertNull(JSType.toMaybeTemplateType(numberType));

    assertFalse(numberType.hasAnyTemplate());
    assertFalse(numberType.hasAnyTemplateInternal());

    assertFalse(numberType.isConstructor());
    assertFalse(numberType.isNominalType());
    assertFalse(numberType.isNominalConstructor());
    assertFalse(numberType.isInstanceType());
    assertFalse(numberType.isInterface());
    assertFalse(numberType.isOrdinaryFunction());
  }

  @Test
  public void testUnionAndEnumDowncasts() {
    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(union.isUnionType());
    assertNotNull(union.toMaybeUnionType());
    assertFalse(numberType.isUnionType());
    assertNull(numberType.toMaybeUnionType());

    assertFalse(numberType.isEnumElementType());
    assertNull(numberType.toMaybeEnumElementType());
    assertFalse(numberType.isEnumType());
    assertNull(numberType.toMaybeEnumType());
  }

  @Test
  public void testFunctionDowncasts() {
    assertTrue(fnType.isFunctionType());
    assertSame(fnType, fnType.toMaybeFunctionType());
    assertSame(fnType, JSType.toMaybeFunctionType(fnType));
    assertNull(JSType.toMaybeFunctionType(null));
    assertFalse(numberType.isFunctionType());
    assertNull(numberType.toMaybeFunctionType());
  }

  @Test
  public void testStructAndDict() {
    assertFalse(numberType.isStruct());
    assertFalse(numberType.isDict());
    assertFalse(objectType.isStruct());
    assertFalse(objectType.isDict());
  }

  @Test
  public void testEquivalenceAndDiffers() {
    assertTrue(numberType.isEquivalentTo(numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertFalse(numberType.isEquivalentTo(stringType));

    assertTrue(numberType.isInvariant(numberType));
    assertFalse(numberType.isInvariant(stringType));

    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertFalse(unknownType.differsFrom(unknownType));
    assertTrue(numberType.differsFrom(unknownType));

    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(stringType));
    assertFalse(numberType.equals("not a type"));
    assertEquals(System.identityHashCode(numberType), numberType.hashCode());
  }

  @Test
  public void testContextMatches() {
    assertTrue(numberType.matchesNumberContext());
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertFalse(stringType.matchesNumberContext());
    assertFalse(stringType.matchesInt32Context());
    assertFalse(stringType.matchesUint32Context());

    assertTrue(stringType.matchesStringContext());
    assertFalse(numberType.matchesStringContext());

    assertTrue(objectType.matchesObjectContext());
    assertFalse(numberType.matchesObjectContext());
    assertFalse(nullType.matchesObjectContext());
  }

  @Test
  public void testAutoboxingAndDereferencing() {
    assertNull(numberType.unboxesTo());
    assertNotNull(numberType.autoboxesTo());
    assertNotNull(numberType.autobox());
    assertNotNull(numberType.dereference());
    assertNull(numberType.toObjectType());
    assertSame(objectType, objectType.toObjectType());
    assertNull(numberType.findPropertyType("toString"));
    assertNotNull(objectType.findPropertyType("toString"));
    assertFalse(numberType.canBeCalled());
    assertTrue(fnType.canBeCalled());
  }

  @Test
  public void testSubtypingAndAssignment() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertFalse(numberType.canAssignTo(stringType));

    assertTrue(noType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(numberType.isSubtype(numberType));
    assertFalse(numberType.isSubtype(stringType));

    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(numberType.isSubtype(union));
    assertTrue(stringType.isSubtype(union));
  }

  @Test
  public void testEqualityAndComparisons() {
    assertTrue(numberType.canTestForEqualityWith(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(numberType));
    assertEquals(TernaryValue.FALSE, numberType.testForEquality(stringType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(allType));
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(numberType));
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));

    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
    assertTrue(noType.canTestForShallowEqualityWith(noType));
    assertTrue(noType.canTestForShallowEqualityWith(numberType));
  }

  @Test
  public void testTypePairsUnderEquality() {
    TypePair pairEqual = numberType.getTypesUnderEquality(numberType);
    assertSame(numberType, pairEqual.typeA);
    assertSame(numberType, pairEqual.typeB);

    TypePair pairFalse = numberType.getTypesUnderEquality(stringType);
    assertNull(pairFalse.typeA);
    assertNull(pairFalse.typeB);

    JSType union = registry.createUnionType(numberType, stringType);
    TypePair pairUnion = numberType.getTypesUnderEquality(union);
    assertNotNull(pairUnion);

    TypePair pairIneq = numberType.getTypesUnderInequality(stringType);
    assertSame(numberType, pairIneq.typeA);
    assertSame(stringType, pairIneq.typeB);

    TypePair pairShallowEq = numberType.getTypesUnderShallowEquality(numberType);
    assertSame(numberType, pairShallowEq.typeA);
    assertSame(numberType, pairShallowEq.typeB);

    TypePair pairShallowIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(pairShallowIneq.typeA);
    assertNull(pairShallowIneq.typeB);

    TypePair pairShallowIneqDiff = numberType.getTypesUnderShallowInequality(stringType);
    assertSame(numberType, pairShallowIneqDiff.typeA);
    assertSame(stringType, pairShallowIneqDiff.typeB);

    TypePair pairShallowIneqUnion = numberType.getTypesUnderShallowInequality(union);
    assertNotNull(pairShallowIneqUnion);
  }

  @Test
  public void testSuperAndSubtypes() {
    assertSame(numberType, numberType.collapseUnion());
    assertSame(numberType, numberType.getLeastSupertype(numberType));
    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());

    assertSame(numberType, numberType.getGreatestSubtype(numberType));
    assertSame(noType, numberType.getGreatestSubtype(stringType));
    assertSame(numberType, numberType.getGreatestSubtype(allType));
    assertSame(unknownType, numberType.getGreatestSubtype(unknownType));

    assertSame(noResolvedType, JSType.filterNoResolvedType(noResolvedType));
    assertSame(numberType, JSType.filterNoResolvedType(numberType));
  }

  @Test
  public void testBooleanOutcomes() {
    BooleanLiteralSet numSet = numberType.getPossibleToBooleanOutcomes();
    assertTrue(numSet.contains(true));
    assertTrue(numSet.contains(false));

    assertSame(numberType, numberType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(checkedUnknownType, unknownType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(noType, nullType.getRestrictedTypeGivenToBooleanOutcome(true));
  }

  @Test
  public void testResolutionLifecycle() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(null, null);
    assertTrue(numberType.isResolved());
    assertSame(resolved, numberType.resolve(null, null));

    numberType.clearResolved();
    assertFalse(numberType.isResolved());

    JSType forceResolved = numberType.forceResolve(null, null);
    assertTrue(numberType.isResolved());
    assertSame(forceResolved, numberType);

    assertNull(JSType.safeResolve(null, null, null));
    assertSame(numberType, JSType.safeResolve(numberType, null, null));
  }

  @Test
  public void testValidatorAndMisc() {
    Predicate<JSType> acceptAll = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return true;
      }
    };
    assertTrue(numberType.setValidator(acceptAll));

    numberType.matchConstraint(stringType);
    assertFalse(numberType.isNullable());
    assertTrue(nullType.isNullable());

    assertEquals("number", numberType.toString());
    assertEquals("number", numberType.toAnnotationString());
    assertEquals("{" + numberType.hashCode() + "}", numberType.toDebugHashCodeString());
  }
}
