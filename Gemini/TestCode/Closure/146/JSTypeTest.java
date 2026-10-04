package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
  private JSType noType;
  private JSType unknownType;
  private JSType objectType;
  private JSType noObjectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
  }

  @Test
  public void testDefaultPredicates() {
    JSType custom = new JSType(registry) {
      private static final long serialVersionUID = 1L;
      public BooleanLiteralSet getPossibleToBooleanOutcomes() {
        return BooleanLiteralSet.BOTH;
      }
      public boolean isSubtype(JSType that) {
        return this == that;
      }
      public <T> T visit(Visitor<T> visitor) {
        return null;
      }
      JSType resolveInternal(com.google.javascript.rhino.ErrorReporter t, StaticScope<JSType> scope) {
        return this;
      }
    };

    assertNull(custom.getJSDocInfo());
    custom.forgiveUnknownNames();
    assertFalse(custom.isNoType());
    assertFalse(custom.isNoObjectType());
    assertFalse(custom.isEmptyType());
    assertFalse(custom.isNumberObjectType());
    assertFalse(custom.isNumberValueType());
    assertFalse(custom.isFunctionPrototypeType());
    assertFalse(custom.isStringObjectType());
    assertFalse(custom.isTheObjectType());
    assertFalse(custom.isStringValueType());
    assertFalse(custom.isArrayType());
    assertFalse(custom.isBooleanObjectType());
    assertFalse(custom.isBooleanValueType());
    assertFalse(custom.isRegexpType());
    assertFalse(custom.isDateType());
    assertFalse(custom.isNullType());
    assertFalse(custom.isVoidType());
    assertFalse(custom.isAllType());
    assertFalse(custom.isUnknownType());
    assertFalse(custom.isCheckedUnknownType());
    assertFalse(custom.isUnionType());
    assertFalse(custom.isFunctionType());
    assertFalse(custom.isEnumElementType());
    assertFalse(custom.isEnumType());
    assertFalse(custom.isNamedType());
    assertFalse(custom.isRecordType());
    assertFalse(custom.isTemplateType());
    assertFalse(custom.isObject());
    assertFalse(custom.isConstructor());
    assertFalse(custom.isNominalType());
    assertFalse(custom.isInstanceType());
    assertFalse(custom.isInterface());
    assertFalse(custom.isOrdinaryFunction());
    assertFalse(custom.matchesInt32Context());
    assertFalse(custom.matchesUint32Context());
    assertFalse(custom.matchesNumberContext());
    assertFalse(custom.matchesStringContext());
    assertFalse(custom.matchesObjectContext());
    assertFalse(custom.canBeCalled());
    assertNull(custom.autoboxesTo());
    assertNull(custom.unboxesTo());
    assertNull(custom.toObjectType());
    assertNull(custom.findPropertyType("prop"));
  }

  @Test
  public void testEmptyType() {
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertFalse(numberType.isEmptyType());
  }

  @Test
  public void testIsStringAndIsNumber() {
    assertTrue(stringType.isString());
    assertFalse(numberType.isString());
    assertTrue(numberType.isNumber());
    assertFalse(stringType.isNumber());
  }

  @Test
  public void testEquivalenceAndEquality() {
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertFalse(JSType.isEquivalent(numberType, stringType));

    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(stringType));
    assertFalse(numberType.equals("string"));
    assertFalse(numberType.equals(null));
    assertEquals(System.identityHashCode(numberType), numberType.hashCode());
  }

  @Test
  public void testCanAssignTo() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertFalse(numberType.canAssignTo(stringType));
  }

  @Test
  public void testDereference() {
    ObjectType deref = numberType.dereference();
    assertNotNull(deref);
    assertNull(nullType.dereference());
    assertNull(voidType.dereference());
  }

  @Test
  public void testEqualityTesting() {
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(allType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(voidType));
    assertEquals(TernaryValue.FALSE, numberType.testForEquality(stringType));

    JSType union = registry.createUnionType(numberType, stringType);
    assertEquals(TernaryValue.FALSE, voidType.testForEquality(union));

    assertTrue(numberType.canTestForEqualityWith(allType));
    assertTrue(numberType.canTestForShallowEqualityWith(allType));
    assertTrue(allType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
  }

  @Test
  public void testIsNullable() {
    assertTrue(nullType.isNullable());
    assertFalse(numberType.isNullable());
    JSType nullableNumber = registry.createNullableType(numberType);
    assertTrue(nullableNumber.isNullable());
  }

  @Test
  public void testGetLeastSupertype() {
    assertSame(allType, numberType.getLeastSupertype(allType));
    assertSame(numberType, numberType.getLeastSupertype(noType));
    JSType union = numberType.getLeastSupertype(stringType);
    assertTrue(union.isUnionType());
    assertSame(union, union.getLeastSupertype(numberType));
  }

  @Test
  public void testGetGreatestSubtype() {
    assertSame(numberType, numberType.getGreatestSubtype(allType));
    assertSame(noType, numberType.getGreatestSubtype(noType));
    assertSame(unknownType, numberType.getGreatestSubtype(unknownType));
    assertSame(unknownType, unknownType.getGreatestSubtype(numberType));
    assertSame(unknownType, unknownType.getGreatestSubtype(unknownType));
    assertSame(numberType, numberType.getGreatestSubtype(numberType));
    assertSame(noType, numberType.getGreatestSubtype(stringType));
    assertSame(noObjectType, objectType.getGreatestSubtype(registry.getNativeType(JSTypeNative.ARRAY_TYPE)));

    JSType union = registry.createUnionType(numberType, stringType);
    assertSame(numberType, union.getGreatestSubtype(numberType));
    assertSame(numberType, numberType.getGreatestSubtype(union));
  }

  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    assertSame(numberType, numberType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(numberType, numberType.getRestrictedTypeGivenToBooleanOutcome(false));
    assertSame(noType, nullType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(nullType, nullType.getRestrictedTypeGivenToBooleanOutcome(false));
  }

  @Test
  public void testGetTypesUnderEqualityAndInequality() {
    JSType.TypePair pairEq = nullType.getTypesUnderEquality(voidType);
    assertSame(nullType, pairEq.typeA);
    assertSame(voidType, pairEq.typeB);

    JSType.TypePair pairEqFalse = numberType.getTypesUnderEquality(stringType);
    assertNull(pairEqFalse.typeA);
    assertNull(pairEqFalse.typeB);

    JSType union = registry.createUnionType(stringType, numberType);
    JSType.TypePair unionEq = numberType.getTypesUnderEquality(union);
    assertNotNull(unionEq);

    JSType.TypePair pairIneqTrue = nullType.getTypesUnderInequality(voidType);
    assertNull(pairIneqTrue.typeA);
    assertNull(pairIneqTrue.typeB);

    JSType.TypePair pairIneqFalse = numberType.getTypesUnderInequality(stringType);
    assertSame(numberType, pairIneqFalse.typeA);
    assertSame(stringType, pairIneqFalse.typeB);

    JSType.TypePair unionIneq = numberType.getTypesUnderInequality(union);
    assertNotNull(unionIneq);
  }

  @Test
  public void testGetTypesUnderShallowEqualityAndInequality() {
    JSType.TypePair shallowEq = numberType.getTypesUnderShallowEquality(numberType);
    assertSame(numberType, shallowEq.typeA);
    assertSame(numberType, shallowEq.typeB);

    JSType.TypePair shallowIneqNull = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(shallowIneqNull.typeA);
    assertNull(shallowIneqNull.typeB);

    JSType.TypePair shallowIneqVoid = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(shallowIneqVoid.typeA);
    assertNull(shallowIneqVoid.typeB);

    JSType.TypePair shallowIneq = numberType.getTypesUnderShallowInequality(stringType);
    assertSame(numberType, shallowIneq.typeA);
    assertSame(stringType, shallowIneq.typeB);

    JSType union = registry.createUnionType(nullType, stringType);
    JSType.TypePair unionShallowIneq = nullType.getTypesUnderShallowInequality(union);
    assertNotNull(unionShallowIneq);
  }

  @Test
  public void testDiffersFrom() {
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertTrue(numberType.differsFrom(unknownType));
    assertTrue(unknownType.differsFrom(numberType));
    assertFalse(unknownType.differsFrom(unknownType));
  }

  @Test
  public void testIsSubtypeStaticHelper() {
    assertTrue(JSType.isSubtype(numberType, unknownType));
    assertTrue(JSType.isSubtype(numberType, numberType));
    assertTrue(JSType.isSubtype(numberType, allType));
    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(JSType.isSubtype(numberType, union));
    assertFalse(JSType.isSubtype(numberType, stringType));
  }

  @Test
  public void testResolveAndLifecycle() {
    assertFalse(numberType.isResolved());
    assertSame(numberType, numberType.resolve(null, null));
    assertTrue(numberType.isResolved());
    assertSame(numberType, numberType.resolve(null, null));

    numberType.clearResolved();
    assertFalse(numberType.isResolved());

    assertSame(numberType, numberType.forceResolve(null, null));
    assertTrue(numberType.isResolved());

    assertNull(JSType.safeResolve(null, null, null));
    assertSame(numberType, JSType.safeResolve(numberType, null, null));
  }

  @Test
  public void testAlphaComparatorAndDebugString() {
    assertTrue(JSType.ALPHA.compare(numberType, numberType) == 0);
    assertTrue(JSType.ALPHA.compare(numberType, stringType) < 0 || JSType.ALPHA.compare(numberType, stringType) > 0);
    assertEquals("{" + numberType.hashCode() + "}", numberType.toDebugHashCodeString());
  }

  @Test
  public void testConstants() {
    assertEquals("Unknown class name", JSType.UNKNOWN_NAME);
    assertEquals("Not declared as a constructor", JSType.NOT_A_CLASS);
    assertEquals("Not declared as a type name", JSType.NOT_A_TYPE);
    assertEquals("Named type with empty name component", JSType.EMPTY_TYPE_COMPONENT);
    assertEquals(1, JSType.ENUMDECL);
    assertEquals(0, JSType.NOT_ENUMDECL);
  }
}
