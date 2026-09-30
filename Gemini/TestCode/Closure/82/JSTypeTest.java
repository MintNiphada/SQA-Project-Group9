package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSType.TypePair;
import com.google.javascript.rhino.jstype.JSTypeRegistry.ResolveMode;

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
  private JSType noType;
  private JSType noObjectType;
  private JSType noResolvedType;
  private JSType objectType;
  private JSType functionType;

  private static class DummyJSType extends JSType {
    private static final long serialVersionUID = 1L;
    private final BooleanLiteralSet boolSet;
    private final boolean isSubtypeResult;

    DummyJSType(JSTypeRegistry registry, BooleanLiteralSet boolSet, boolean isSubtypeResult) {
      super(registry);
      this.boolSet = boolSet;
      this.isSubtypeResult = isSubtypeResult;
    }

    @Override
    public BooleanLiteralSet getPossibleToBooleanOutcomes() {
      return boolSet;
    }

    @Override
    public boolean isSubtype(JSType that) {
      return isSubtypeResult;
    }

    @Override
    public <T> T visit(Visitor<T> visitor) {
      return null;
    }

    @Override
    JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
      return this;
    }
  }

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
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    functionType = registry.getNativeType(JSTypeNative.U2U_CONSTRUCTOR_TYPE);
  }

  @Test
  public void testDefaultTypeFlagsAndProperties() {
    DummyJSType dummy = new DummyJSType(registry, BooleanLiteralSet.BOTH, false);

    assertNull(dummy.getJSDocInfo());
    assertNull(dummy.getDisplayName());
    assertFalse(dummy.hasDisplayName());

    dummy.forgiveUnknownNames();

    assertFalse(dummy.isNoType());
    assertFalse(dummy.isNoResolvedType());
    assertFalse(dummy.isNoObjectType());
    assertFalse(dummy.isEmptyType());

    assertFalse(dummy.isNumberObjectType());
    assertFalse(dummy.isNumberValueType());
    assertFalse(dummy.isFunctionPrototypeType());
    assertFalse(dummy.isStringObjectType());
    assertFalse(dummy.isTheObjectType());
    assertFalse(dummy.isStringValueType());
    assertFalse(dummy.isArrayType());
    assertFalse(dummy.isBooleanObjectType());
    assertFalse(dummy.isBooleanValueType());
    assertFalse(dummy.isRegexpType());
    assertFalse(dummy.isDateType());
    assertFalse(dummy.isNullType());
    assertFalse(dummy.isVoidType());
    assertFalse(dummy.isAllType());
    assertFalse(dummy.isUnknownType());
    assertFalse(dummy.isCheckedUnknownType());
    assertFalse(dummy.isUnionType());
    assertFalse(dummy.isFunctionType());
    assertFalse(dummy.isEnumElementType());
    assertFalse(dummy.isEnumType());
    assertFalse(dummy.isNamedType());
    assertFalse(dummy.isRecordType());
    assertFalse(dummy.isTemplateType());
    assertFalse(dummy.isObject());
    assertFalse(dummy.isConstructor());
    assertFalse(dummy.isNominalType());
    assertFalse(dummy.isInstanceType());
    assertFalse(dummy.isInterface());
    assertFalse(dummy.isOrdinaryFunction());
    assertFalse(dummy.canBeCalled());
    assertNull(dummy.autoboxesTo());
    assertNull(dummy.unboxesTo());
    assertNull(dummy.toObjectType());
    assertNull(dummy.findPropertyType("foo"));

    assertFalse(dummy.matchesInt32Context());
    assertFalse(dummy.matchesUint32Context());
    assertFalse(dummy.matchesNumberContext());
    assertFalse(dummy.matchesStringContext());
    assertFalse(dummy.matchesObjectContext());
    assertFalse(dummy.isNullable());
    assertSame(dummy, dummy.restrictByNotNullOrUndefined());
  }

  @Test
  public void testEmptyTypeChecks() {
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
    assertFalse(numberType.isEmptyType());
  }

  @Test
  public void testStringAndNumberPredicates() {
    assertTrue(stringType.isString());
    assertTrue(numberType.isNumber());
    assertFalse(booleanType.isString());
    assertFalse(booleanType.isNumber());

    JSType stringObj = registry.getNativeType(JSTypeNative.STRING_OBJECT_TYPE);
    JSType numberObj = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertTrue(stringObj.isString());
    assertTrue(numberObj.isNumber());
  }

  @Test
  public void testEqualityAndEquivalence() {
    assertTrue(numberType.isEquivalentTo(numberType));
    assertFalse(numberType.isEquivalentTo(stringType));
    assertTrue(JSType.isEquivalent(numberType, numberType));
    assertFalse(JSType.isEquivalent(numberType, stringType));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));

    assertTrue(numberType.equals(numberType));
    assertFalse(numberType.equals(stringType));
    assertFalse(numberType.equals("not a JSType"));
    assertFalse(numberType.equals(null));

    assertEquals(System.identityHashCode(numberType), numberType.hashCode());
  }

  @Test
  public void testAlphaComparator() {
    int cmp = JSType.ALPHA.compare(numberType, stringType);
    int expected = numberType.toString().compareTo(stringType.toString());
    assertEquals(expected, cmp);
  }

  @Test
  public void testDereferenceAndFindPropertyType() {
    assertNotNull(stringType.dereference());
    assertTrue(stringType.dereference().isObject());

    DummyJSType dummy = new DummyJSType(registry, BooleanLiteralSet.EMPTY, false);
    assertNull(dummy.dereference());
    assertNull(dummy.findPropertyType("length"));

    assertNotNull(stringType.findPropertyType("length"));
    assertNull(stringType.findPropertyType("nonExistentPropertyXYZ"));
  }

  @Test
  public void testCanAssignTo() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertFalse(numberType.canAssignTo(stringType));
  }

  @Test
  public void testTestForEqualityAndShallowEquality() {
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(allType));
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
    assertEquals(TernaryValue.UNKNOWN, unknownType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(noResolvedType));
    assertEquals(TernaryValue.UNKNOWN, noResolvedType.testForEquality(numberType));

    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.TRUE, noType.testForEquality(noObjectType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(noType));

    assertEquals(TernaryValue.FALSE, functionType.testForEquality(nullType));
    assertEquals(TernaryValue.FALSE, nullType.testForEquality(functionType));
    assertEquals(TernaryValue.UNKNOWN, functionType.testForEquality(objectType));
    assertEquals(TernaryValue.UNKNOWN, objectType.testForEquality(functionType));

    JSType union = registry.createUnionType(numberType, stringType);
    assertEquals(TernaryValue.UNKNOWN, union.testForEquality(numberType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(union));

    assertTrue(numberType.canTestForEqualityWith(stringType));

    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertTrue(numberType.canTestForShallowEqualityWith(allType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
  }

  @Test
  public void testGetLeastSupertype() {
    assertSame(numberType, numberType.getLeastSupertype(numberType));
    JSType join = numberType.getLeastSupertype(stringType);
    assertTrue(join.isUnionType());

    JSType union = registry.createUnionType(numberType, stringType);
    JSType joinWithUnion = booleanType.getLeastSupertype(union);
    assertTrue(joinWithUnion.isUnionType());
  }

  @Test
  public void testGetGreatestSubtype() {
    assertSame(numberType, numberType.getGreatestSubtype(numberType));
    assertSame(unknownType, numberType.getGreatestSubtype(unknownType));
    assertSame(unknownType, unknownType.getGreatestSubtype(numberType));

    assertSame(numberType, numberType.getGreatestSubtype(allType));
    assertSame(numberType, allType.getGreatestSubtype(numberType));

    JSType union = registry.createUnionType(numberType, stringType);
    assertSame(numberType, union.getGreatestSubtype(numberType));
    assertSame(numberType, numberType.getGreatestSubtype(union));

    assertSame(noObjectType, objectType.getGreatestSubtype(functionType));
    assertSame(noType, numberType.getGreatestSubtype(stringType));
  }

  @Test
  public void testFilterNoResolvedType() {
    assertSame(noResolvedType, JSType.filterNoResolvedType(noResolvedType));

    DummyJSType dummy = new DummyJSType(registry, BooleanLiteralSet.BOTH, false) {
      private static final long serialVersionUID = 1L;
      @Override public boolean isNoResolvedType() { return true; }
    };
    assertSame(noResolvedType, JSType.filterNoResolvedType(dummy));

    JSType unionWithNoResolved = registry.createUnionType(numberType, noResolvedType);
    JSType filtered = JSType.filterNoResolvedType(unionWithNoResolved);
    assertFalse(filtered.isNoResolvedType());

    assertSame(numberType, JSType.filterNoResolvedType(numberType));
  }

  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    DummyJSType alwaysTrue = new DummyJSType(registry, BooleanLiteralSet.TRUE, false);
    assertSame(alwaysTrue, alwaysTrue.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(noType, alwaysTrue.getRestrictedTypeGivenToBooleanOutcome(false));

    DummyJSType alwaysFalse = new DummyJSType(registry, BooleanLiteralSet.FALSE, false);
    assertSame(noType, alwaysFalse.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(alwaysFalse, alwaysFalse.getRestrictedTypeGivenToBooleanOutcome(false));
  }

  @Test
  public void testGetTypesUnderEqualityAndInequality() {
    TypePair eqPair = numberType.getTypesUnderEquality(stringType);
    assertSame(numberType, eqPair.typeA);
    assertSame(stringType, eqPair.typeB);

    JSType union = registry.createUnionType(numberType, stringType);
    TypePair unionEqPair = numberType.getTypesUnderEquality(union);
    assertNotNull(unionEqPair.typeA);
    assertNotNull(unionEqPair.typeB);

    TypePair ineqPair = numberType.getTypesUnderInequality(stringType);
    assertSame(numberType, ineqPair.typeA);
    assertSame(stringType, ineqPair.typeB);

    TypePair trueIneqPair = noType.getTypesUnderInequality(noObjectType);
    assertSame(noType, trueIneqPair.typeA);
    assertSame(noType, trueIneqPair.typeB);

    TypePair unionIneqPair = numberType.getTypesUnderInequality(union);
    assertNotNull(unionIneqPair.typeA);
    assertNotNull(unionIneqPair.typeB);
  }

  @Test
  public void testGetTypesUnderShallowEqualityAndInequality() {
    TypePair shallowEq = numberType.getTypesUnderShallowEquality(numberType);
    assertSame(numberType, shallowEq.typeA);
    assertSame(numberType, shallowEq.typeB);

    TypePair nullIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(nullIneq.typeA);
    assertNull(nullIneq.typeB);

    TypePair voidIneq = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(voidIneq.typeA);
    assertNull(voidIneq.typeB);

    TypePair otherIneq = numberType.getTypesUnderShallowInequality(stringType);
    assertSame(numberType, otherIneq.typeA);
    assertSame(stringType, otherIneq.typeB);

    JSType union = registry.createUnionType(numberType, stringType);
    TypePair unionShallowIneq = numberType.getTypesUnderShallowInequality(union);
    assertNotNull(unionShallowIneq.typeA);
    assertNotNull(unionShallowIneq.typeB);
  }

  @Test
  public void testDiffersFrom() {
    assertFalse(numberType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(stringType));
    assertTrue(unknownType.differsFrom(numberType));
    assertTrue(numberType.differsFrom(unknownType));
    assertFalse(unknownType.differsFrom(unknownType));
  }

  @Test
  public void testIsSubtypeHelper() {
    assertTrue(JSType.isSubtype(numberType, unknownType));
    assertTrue(JSType.isSubtype(numberType, numberType));
    assertTrue(JSType.isSubtype(numberType, allType));

    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(JSType.isSubtype(numberType, union));
    assertFalse(JSType.isSubtype(booleanType, union));
    assertFalse(JSType.isSubtype(numberType, stringType));

    NamedType namedType = new NamedType(registry, "Number", "source.js", 1, 1);
    namedType.resolve(null, null);
    assertTrue(JSType.isSubtype(numberType, namedType));
  }

  @Test
  public void testResolutionLifecycle() {
    DummyJSType dummy = new DummyJSType(registry, BooleanLiteralSet.BOTH, false);
    assertFalse(dummy.isResolved());

    dummy.resolve(null, null);
    assertTrue(dummy.isResolved());

    assertSame(dummy, dummy.resolve(null, null));

    dummy.clearResolved();
    assertFalse(dummy.isResolved());

    DummyJSType nullResolveDummy = new DummyJSType(registry, BooleanLiteralSet.BOTH, false) {
      private static final long serialVersionUID = 1L;
      @Override
      JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
        setResolvedTypeInternal(null);
        return null;
      }
    };
    nullResolveDummy.setResolvedTypeInternal(null);
    assertSame(unknownType, nullResolveDummy.resolve(null, null));

    assertNull(JSType.safeResolve(null, null, null));
    assertSame(dummy, JSType.safeResolve(dummy, null, null));
  }

  @Test
  public void testForceResolve() {
    DummyJSType dummy = new DummyJSType(registry, BooleanLiteralSet.BOTH, false);
    ResolveMode oldMode = registry.getResolveMode();
    JSType resolved = dummy.forceResolve(null, null);
    assertSame(dummy, resolved);
    assertEquals(oldMode, registry.getResolveMode());
  }

  @Test
  public void testSetValidator() {
    DummyJSType dummy = new DummyJSType(registry, BooleanLiteralSet.BOTH, false);
    assertTrue(dummy.setValidator(Predicates.<JSType>alwaysTrue()));
    assertFalse(dummy.setValidator(Predicates.<JSType>alwaysFalse()));
  }

  @Test
  public void testToDebugHashCodeString() {
    DummyJSType dummy = new DummyJSType(registry, BooleanLiteralSet.BOTH, false);
    assertEquals("{" + dummy.hashCode() + "}", dummy.toDebugHashCodeString());
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