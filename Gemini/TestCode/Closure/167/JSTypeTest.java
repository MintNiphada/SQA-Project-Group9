package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.JSType.TypePair;
import org.junit.Before;
import org.junit.Test;

public class JSTypeTest {
  private JSTypeRegistry registry;
  private SimpleErrorReporter reporter;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType allType;
  private JSType unknownType;
  private JSType objectType;
  private JSType noType;
  private JSType noObjectType;
  private JSType noResolvedType;

  @Before
  public void setUp() {
    reporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(reporter);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    noResolvedType = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
  }

  @Test
  public void testTypeQueriesBasic() {
    assertFalse(numberType.isNoType());
    assertTrue(noType.isNoType());
    assertTrue(noObjectType.isNoObjectType());
    assertTrue(noResolvedType.isNoResolvedType());

    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
    assertTrue(registry.getNativeFunctionType(JSTypeNative.LEAST_FUNCTION_TYPE).isEmptyType());
    assertFalse(numberType.isEmptyType());

    assertTrue(numberType.isNumberValueType());
    assertFalse(numberType.isNumberObjectType());
    assertTrue(stringType.isStringValueType());
    assertFalse(stringType.isStringObjectType());
    assertTrue(booleanType.isBooleanValueType());
    assertFalse(booleanType.isBooleanObjectType());

    assertTrue(nullType.isNullType());
    assertTrue(voidType.isVoidType());
    assertTrue(allType.isAllType());
    assertTrue(unknownType.isUnknownType());
    assertFalse(unknownType.isCheckedUnknownType());

    assertTrue(numberType.isNumber());
    assertTrue(stringType.isString());
    assertFalse(booleanType.isNumber());
    assertFalse(booleanType.isString());
  }

  @Test
  public void testObjectAndFunctionQueries() {
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    assertTrue(objType.isObject());
    assertFalse(numberType.isObject());
    assertFalse(numberType.isFunctionPrototypeType());
    assertFalse(numberType.isTheObjectType());
    assertFalse(numberType.isArrayType());
    assertFalse(numberType.isRegexpType());
    assertFalse(numberType.isDateType());
    assertFalse(numberType.isConstructor());
    assertFalse(numberType.isNominalType());
    assertFalse(numberType.isNominalConstructor());
    assertFalse(numberType.isInstanceType());
    assertFalse(numberType.isInterface());
    assertFalse(numberType.isOrdinaryFunction());

    FunctionType fnType = registry.createFunctionType(numberType);
    assertTrue(fnType.isFunctionType());
    assertNotNull(fnType.toMaybeFunctionType());
    assertNotNull(JSType.toMaybeFunctionType(fnType));
    assertNull(JSType.toMaybeFunctionType(null));
    assertNull(numberType.toMaybeFunctionType());
    assertTrue(fnType.canBeCalled());
    assertFalse(numberType.canBeCalled());
  }

  @Test
  public void testUnionAndSpecialTypeDowncasts() {
    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(union.isUnionType());
    assertNotNull(union.toMaybeUnionType());
    assertNull(numberType.toMaybeUnionType());

    EnumType enumType = registry.createEnumType("MyEnum", null, numberType);
    assertTrue(enumType.isEnumType());
    assertNotNull(enumType.toMaybeEnumType());
    assertNull(numberType.toMaybeEnumType());

    EnumElementType enumElem = enumType.getElementsType();
    assertTrue(enumElem.isEnumElementType());
    assertNotNull(enumElem.toMaybeEnumElementType());
    assertNull(numberType.toMaybeEnumElementType());

    RecordType record = registry.createRecordTypeBuilder().build();
    assertTrue(record.isRecordType());
    assertNotNull(record.toMaybeRecordType());
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
    assertFalse(numberType.isNamedType());
  }

  @Test
  public void testGlobalThisAndDocInfoAndDisplayName() {
    JSType globalThis = registry.getNativeType(JSTypeNative.GLOBAL_THIS);
    assertTrue(globalThis.isGlobalThisType());
    assertFalse(numberType.isGlobalThisType());

    assertNull(numberType.getJSDocInfo());
    assertNull(numberType.getDisplayName());
    assertFalse(numberType.hasDisplayName());

    EnumType namedEnum = registry.createEnumType("TestEnum", null, numberType);
    assertTrue(namedEnum.hasDisplayName());
    assertEquals("TestEnum", namedEnum.getDisplayName());
  }

  @Test
  public void testEquivalenceAndEquality() {
    assertTrue(numberType.isEquivalentTo(numberType));
    assertFalse(numberType.isEquivalentTo(stringType));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(numberType, null));
    assertFalse(JSType.isEquivalent(null, numberType));
    assertTrue(JSType.isEquivalent(numberType, numberType));

    assertTrue(numberType.equals((Object) numberType));
    assertFalse(numberType.equals(new Object()));
    assertFalse(numberType.equals(stringType));
    assertEquals(System.identityHashCode(numberType), numberType.hashCode());

    ProxyObjectType proxy = new ProxyObjectType(registry, numberType);
    assertTrue(numberType.isEquivalentTo(proxy));
    assertTrue(proxy.isEquivalentTo(numberType));
  }

  @Test
  public void testContextMatching() {
    assertTrue(numberType.matchesNumberContext());
    assertTrue(numberType.matchesInt32Context());
    assertTrue(numberType.matchesUint32Context());
    assertFalse(stringType.matchesNumberContext());

    assertTrue(stringType.matchesStringContext());
    assertFalse(numberType.matchesStringContext());

    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    assertTrue(objType.matchesObjectContext());
    assertFalse(numberType.matchesObjectContext());
  }

  @Test
  public void testAutoboxAndUnboxAndDereference() {
    assertNull(numberType.unboxesTo());
    assertNotNull(numberType.autoboxesTo());
    assertNull(allType.autoboxesTo());
    assertNull(numberType.toObjectType());

    JSType autoboxed = numberType.autobox();
    assertTrue(autoboxed.isObject());
    ObjectType deref = numberType.dereference();
    assertNotNull(deref);

    assertNull(numberType.findPropertyType("nonExistentProp"));
    assertNull(allType.findPropertyType("foo"));
  }

  @Test
  public void testCanAssignToAndSubtype() {
    assertTrue(numberType.canAssignTo(numberType));
    assertTrue(numberType.canAssignTo(allType));
    assertFalse(numberType.canAssignTo(stringType));

    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(numberType.isSubtype(allType));
    assertTrue(noType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(numberType));

    JSType union = registry.createUnionType(numberType, stringType);
    assertTrue(numberType.isSubtype(union));
    assertTrue(stringType.isSubtype(union));
    assertFalse(booleanType.isSubtype(union));

    ProxyObjectType proxy = new ProxyObjectType(registry, numberType);
    assertTrue(numberType.isSubtype(proxy));
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
  public void testNullableAndCollapseUnionAndRestrict() {
    assertTrue(nullType.isNullable());
    assertFalse(numberType.isNullable());
    JSType nullableNum = registry.createNullableType(numberType);
    assertTrue(nullableNum.isNullable());

    assertSame(numberType, numberType.collapseUnion());
    assertSame(numberType, numberType.restrictByNotNullOrUndefined());
  }

  @Test
  public void testLeastSupertypeAndGreatestSubtype() {
    assertSame(numberType, numberType.getLeastSupertype(numberType));
    JSType supNumStr = numberType.getLeastSupertype(stringType);
    assertTrue(supNumStr.isUnionType());

    JSType union = registry.createUnionType(numberType, booleanType);
    JSType supUnion = numberType.getLeastSupertype(union);
    assertTrue(supUnion.isUnionType());

    assertSame(numberType, numberType.getGreatestSubtype(numberType));
    assertSame(numberType, numberType.getGreatestSubtype(allType));
    assertSame(noType, numberType.getGreatestSubtype(stringType));

    assertSame(unknownType, numberType.getGreatestSubtype(unknownType));
    assertSame(unknownType, unknownType.getGreatestSubtype(numberType));

    FunctionType fn1 = registry.createFunctionType(numberType);
    FunctionType fn2 = registry.createFunctionType(stringType);
    assertNotNull(fn1.getGreatestSubtype(fn2));

    RecordType rec = registry.createRecordTypeBuilder().build();
    assertNotNull(rec.getGreatestSubtype(rec));
    assertNotNull(numberType.getGreatestSubtype(rec));

    EnumType enumType = registry.createEnumType("E", null, numberType);
    EnumElementType elemType = enumType.getElementsType();
    assertNotNull(elemType.getGreatestSubtype(numberType));
    assertNotNull(numberType.getGreatestSubtype(elemType));

    ObjectType obj1 = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ObjectType obj2 = registry.createAnonymousObjectType();
    assertEquals(noObjectType, obj1.getGreatestSubtype(obj2));
  }

  @Test
  public void testFilterNoResolvedType() {
    assertSame(noResolvedType, JSType.filterNoResolvedType(noResolvedType));
    assertSame(numberType, JSType.filterNoResolvedType(numberType));

    JSType unionWithNoResolved = new UnionTypeBuilder(registry)
        .addAlternate(numberType)
        .addAlternate(noResolvedType)
        .build();
    JSType filtered = JSType.filterNoResolvedType(unionWithNoResolved);
    assertFalse(filtered.isNoResolvedType());
  }

  @Test
  public void testTestForEqualityAndHelpers() {
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(allType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(unknownType));
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(noResolvedType));
    assertEquals(TernaryValue.UNKNOWN, allType.testForEquality(numberType));

    assertEquals(TernaryValue.TRUE, noType.testForEquality(noType));
    assertEquals(TernaryValue.UNKNOWN, noType.testForEquality(numberType));

    FunctionType fn = registry.createFunctionType(numberType);
    assertEquals(TernaryValue.FALSE, fn.testForEquality(nullType));
    assertEquals(TernaryValue.UNKNOWN, fn.testForEquality(objectType));

    EnumType enumType = registry.createEnumType("E", null, numberType);
    EnumElementType elem = enumType.getElementsType();
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(elem));

    assertTrue(numberType.canTestForEqualityWith(unknownType));
    assertTrue(numberType.canTestForShallowEqualityWith(numberType));
    assertTrue(noType.canTestForShallowEqualityWith(numberType));
    assertFalse(numberType.canTestForShallowEqualityWith(stringType));
  }

  @Test
  public void testTypesUnderEqualityAndInequality() {
    TypePair eqPair = numberType.getTypesUnderEquality(unknownType);
    assertSame(numberType, eqPair.typeA);
    assertSame(unknownType, eqPair.typeB);

    FunctionType fn = registry.createFunctionType(numberType);
    TypePair falseEq = fn.getTypesUnderEquality(nullType);
    assertNull(falseEq.typeA);
    assertNull(falseEq.typeB);

    JSType union = registry.createUnionType(numberType, stringType);
    TypePair unionEq = numberType.getTypesUnderEquality(union);
    assertNotNull(unionEq);

    TypePair unionIneq = numberType.getTypesUnderInequality(union);
    assertNotNull(unionIneq);

    TypePair noTypeIneq = noType.getTypesUnderInequality(noType);
    assertEquals(noType, noTypeIneq.typeA);
    assertEquals(noType, noTypeIneq.typeB);

    TypePair unknownIneq = numberType.getTypesUnderInequality(unknownType);
    assertSame(numberType, unknownIneq.typeA);
    assertSame(unknownType, unknownIneq.typeB);
  }

  @Test
  public void testTypesUnderShallowEqualityAndInequality() {
    TypePair shallowEq = numberType.getTypesUnderShallowEquality(numberType);
    assertSame(numberType, shallowEq.typeA);
    assertSame(numberType, shallowEq.typeB);

    TypePair shallowNullIneq = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(shallowNullIneq.typeA);
    assertNull(shallowNullIneq.typeB);

    TypePair shallowVoidIneq = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(shallowVoidIneq.typeA);
    assertNull(shallowVoidIneq.typeB);

    TypePair shallowOtherIneq = numberType.getTypesUnderShallowInequality(stringType);
    assertSame(numberType, shallowOtherIneq.typeA);
    assertSame(stringType, shallowOtherIneq.typeB);

    JSType union = registry.createUnionType(nullType, voidType);
    TypePair unionShallow = nullType.getTypesUnderShallowInequality(union);
    assertNotNull(unionShallow);
  }

  @Test
  public void testRestrictedTypeGivenToBooleanOutcome() {
    JSType restrictedTrue = numberType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertSame(numberType, restrictedTrue);

    JSType restrictedFalse = nullType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertSame(noType, restrictedFalse);
  }

  @Test
  public void testResolutionLifecycle() {
    assertFalse(numberType.isResolved());
    JSType resolved = numberType.resolve(reporter, null);
    assertSame(numberType, resolved);
    assertTrue(numberType.isResolved());

    assertSame(numberType, numberType.resolve(reporter, null));

    numberType.clearResolved();
    assertFalse(numberType.isResolved());

    JSType forced = numberType.forceResolve(reporter, null);
    assertSame(numberType, forced);
    assertTrue(numberType.isResolved());

    assertNull(JSType.safeResolve(null, reporter, null));
    assertSame(numberType, JSType.safeResolve(numberType, reporter, null));

    numberType.clearResolved();
  }

  @Test
  public void testSetValidatorAndMatchConstraint() {
    Predicate<JSType> acceptAll = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return true;
      }
    };
    assertTrue(numberType.setValidator(acceptAll));

    Predicate<JSType> rejectAll = new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return false;
      }
    };
    assertFalse(numberType.setValidator(rejectAll));

    numberType.matchConstraint(stringType);
  }

  @Test
  public void testStringAndComparatorAndDebug() {
    assertEquals("number", numberType.toString());
    assertEquals("number", numberType.toAnnotationString());
    assertTrue(numberType.toDebugHashCodeString().startsWith("{"));
    assertTrue(numberType.toDebugHashCodeString().endsWith("}"));

    assertTrue(JSType.ALPHA.compare(numberType, stringType) < 0);
    assertTrue(JSType.ALPHA.compare(stringType, numberType) > 0);
    assertEquals(0, JSType.ALPHA.compare(numberType, numberType));
  }
}
