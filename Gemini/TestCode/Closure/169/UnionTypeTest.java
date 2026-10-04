package com.google.javascript.rhino.jstype;

import static com.google.javascript.rhino.jstype.JSTypeNative.ALL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.CHECKED_UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.ERROR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.EVAL_ERROR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.URI_ERROR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

public class UnionTypeTest {
  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType nullType;
  private JSType voidType;
  private JSType unknownType;
  private JSType allType;
  private JSType noType;
  private JSType noObjectType;
  private ObjectType objectType;
  private ObjectType errorType;
  private ObjectType evalErrorType;
  private ObjectType uriErrorType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(NUMBER_TYPE);
    stringType = registry.getNativeType(STRING_TYPE);
    booleanType = registry.getNativeType(BOOLEAN_TYPE);
    nullType = registry.getNativeType(NULL_TYPE);
    voidType = registry.getNativeType(VOID_TYPE);
    unknownType = registry.getNativeType(UNKNOWN_TYPE);
    allType = registry.getNativeType(ALL_TYPE);
    noType = registry.getNativeType(NO_TYPE);
    noObjectType = registry.getNativeType(NO_OBJECT_TYPE);
    objectType = (ObjectType) registry.getNativeType(OBJECT_TYPE);
    errorType = (ObjectType) registry.getNativeType(ERROR_TYPE);
    evalErrorType = (ObjectType) registry.getNativeType(EVAL_ERROR_TYPE);
    uriErrorType = (ObjectType) registry.getNativeType(URI_ERROR_TYPE);
  }

  private UnionType createUnion(JSType... types) {
    return (UnionType) registry.createUnionType(types);
  }

  @Test
  public void testGetAlternatesAndHashCode() {
    UnionType union = createUnion(numberType, stringType);
    assertNotNull(union.getAlternates());
    assertEquals(union.hashCode(), union.alternates.hashCode());
  }

  @Test
  public void testMatchesContexts() {
    UnionType numOrStr = createUnion(numberType, stringType);
    assertTrue(numOrStr.matchesNumberContext());
    assertTrue(numOrStr.matchesStringContext());
    assertTrue(numOrStr.matchesObjectContext());

    UnionType voidOrNull = (UnionType) registry.createUnionType(voidType, nullType);
    assertFalse(voidOrNull.matchesObjectContext());

    UnionType voidUnion = new UnionType(registry, ImmutableList.of(voidType));
    assertFalse(voidUnion.matchesNumberContext());
    assertFalse(voidUnion.matchesStringContext());
  }

  @Test
  public void testFindPropertyType() {
    ObjectType objA = registry.createAnonymousObjectType();
    objA.defineDeclaredProperty("prop", numberType, null);
    ObjectType objB = registry.createAnonymousObjectType();
    objB.defineDeclaredProperty("prop", stringType, null);

    UnionType union = createUnion(objA, objB, nullType, voidType);
    JSType propType = union.findPropertyType("prop");
    assertTrue(propType.isUnionType());
    assertTrue(propType.toMaybeUnionType().contains(numberType));
    assertTrue(propType.toMaybeUnionType().contains(stringType));

    ObjectType objC = registry.createAnonymousObjectType();
    UnionType unionNoProp = createUnion(objC, nullType);
    assertNull(unionNoProp.findPropertyType("nonExistent"));
  }

  @Test
  public void testCanAssignTo() {
    UnionType numOrStr = createUnion(numberType, stringType);
    assertTrue(numOrStr.canAssignTo(allType));
    assertFalse(numOrStr.canAssignTo(numberType));

    UnionType withUnknown = new UnionType(registry, ImmutableList.of(numberType, unknownType));
    assertTrue(withUnknown.canAssignTo(numberType));
  }

  @Test
  public void testCanBeCalled() {
    FunctionType fn1 = registry.createFunctionType(numberType);
    FunctionType fn2 = registry.createFunctionType(stringType);
    UnionType callableUnion = (UnionType) registry.createUnionType(fn1, fn2);
    assertTrue(callableUnion.canBeCalled());

    UnionType mixedUnion = createUnion(fn1, numberType);
    assertFalse(mixedUnion.canBeCalled());
  }

  @Test
  public void testAutoboxAndRestrictNotNullOrUndefined() {
    UnionType union = createUnion(numberType, stringType, nullType, voidType);
    JSType autoboxed = union.autobox();
    assertTrue(autoboxed.isUnionType());
    for (JSType alt : autoboxed.toMaybeUnionType().getAlternates()) {
      assertTrue(alt.isObject() || alt.isNullType() || alt.isVoidType());
    }

    JSType restricted = union.restrictByNotNullOrUndefined();
    assertTrue(restricted.isUnionType());
    assertFalse(restricted.toMaybeUnionType().contains(nullType));
    assertFalse(restricted.toMaybeUnionType().contains(voidType));
  }

  @Test
  public void testTestForEquality() {
    UnionType numStr = createUnion(numberType, stringType);
    assertEquals(TernaryValue.UNKNOWN, numStr.testForEquality(numberType));

    UnionType onlyNull = new UnionType(registry, ImmutableList.of(nullType));
    assertEquals(TernaryValue.TRUE, onlyNull.testForEquality(nullType));
    assertEquals(TernaryValue.FALSE, onlyNull.testForEquality(numberType));
  }

  @Test
  public void testIsNullableAndIsUnknownType() {
    UnionType numOrNull = createUnion(numberType, nullType);
    assertTrue(numOrNull.isNullable());

    UnionType numOrStr = createUnion(numberType, stringType);
    assertFalse(numOrStr.isNullable());
    assertFalse(numOrStr.isUnknownType());

    UnionType withUnknown = new UnionType(registry, ImmutableList.of(numberType, unknownType));
    assertTrue(withUnknown.isUnknownType());
  }

  @Test
  public void testIsStructAndIsDict() {
    ObjectType structObj = registry.createObjectType("StructObj", null, null);
    structObj.setStruct();
    UnionType structUnion = createUnion(structObj, numberType);
    assertTrue(structUnion.isStruct());
    assertFalse(structUnion.isDict());

    ObjectType dictObj = registry.createObjectType("DictObj", null, null);
    dictObj.setDict();
    UnionType dictUnion = createUnion(dictObj, numberType);
    assertTrue(dictUnion.isDict());
    assertFalse(dictUnion.isStruct());
  }

  @Test
  public void testGetLeastSupertype() {
    UnionType numOrStr = createUnion(numberType, stringType);
    assertSame(numOrStr, numOrStr.getLeastSupertype(numberType));
    JSType superType = numOrStr.getLeastSupertype(booleanType);
    assertTrue(superType.isUnionType());
    assertTrue(superType.toMaybeUnionType().contains(booleanType));
  }

  @Test
  public void testMeet() {
    UnionType errors = createUnion(evalErrorType, uriErrorType);
    JSType meetResult = errors.meet(errorType);
    assertEquals(errors, meetResult);

    UnionType singleEval = new UnionType(registry, ImmutableList.<JSType>of(evalErrorType));
    JSType meetUnion = singleEval.meet(createUnion(evalErrorType, uriErrorType));
    assertTrue(meetUnion.isEquivalentTo(evalErrorType));

    UnionType objUnion = createUnion(evalErrorType, uriErrorType);
    ObjectType nonRelatedObj = registry.createAnonymousObjectType();
    JSType objMeet = objUnion.meet(nonRelatedObj);
    assertEquals(noObjectType, objMeet);

    UnionType numStr = createUnion(numberType, stringType);
    JSType primMeet = numStr.meet(booleanType);
    assertEquals(noType, primMeet);
  }

  @Test
  public void testCheckUnionEquivalenceHelper() {
    UnionType u1 = createUnion(numberType, stringType);
    UnionType u2 = createUnion(stringType, numberType);
    UnionType u3 = createUnion(numberType, booleanType);
    UnionType u4 = (UnionType) registry.createUnionType(numberType, stringType, booleanType);

    assertTrue(u1.checkUnionEquivalenceHelper(u2, false));
    assertFalse(u1.checkUnionEquivalenceHelper(u3, false));
    assertFalse(u1.checkUnionEquivalenceHelper(u4, false));

    JSType checkedUnknown = registry.getNativeType(CHECKED_UNKNOWN_TYPE);
    UnionType uUnknown1 = new UnionType(registry, ImmutableList.of(unknownType, numberType));
    UnionType uUnknown2 = new UnionType(registry, ImmutableList.of(checkedUnknown, numberType));
    assertTrue(uUnknown1.checkUnionEquivalenceHelper(uUnknown2, true));
  }

  @Test
  public void testHasProperty() {
    ObjectType obj = registry.createAnonymousObjectType();
    obj.defineDeclaredProperty("foo", numberType, null);
    UnionType union = createUnion(obj, stringType);
    assertTrue(union.hasProperty("foo"));
    assertFalse(union.hasProperty("bar"));
  }

  @Test
  public void testToMaybeUnionTypeAndIsObject() {
    UnionType union = createUnion(objectType, errorType);
    assertSame(union, union.toMaybeUnionType());
    assertTrue(union.isObject());

    UnionType nonObjUnion = createUnion(objectType, numberType);
    assertFalse(nonObjUnion.isObject());
  }

  @Test
  public void testContainsAndGetRestrictedUnion() {
    UnionType union = createUnion(numberType, stringType);
    assertTrue(union.contains(numberType));
    assertFalse(union.contains(booleanType));

    UnionType errors = createUnion(nullType, evalErrorType, uriErrorType);
    JSType restricted = errors.getRestrictedUnion(errorType);
    assertEquals(nullType, restricted);

    UnionType withUnknown = new UnionType(registry, ImmutableList.of(unknownType, numberType));
    JSType restrictedUnknown = withUnknown.getRestrictedUnion(numberType);
    assertTrue(restrictedUnknown.isUnknownType());
  }

  @Test
  public void testToStringHelper() {
    UnionType union = createUnion(numberType, stringType);
    assertEquals("(number|string)", union.toStringHelper(false));
  }

  @Test
  public void testIsSubtype() {
    UnionType union = createUnion(evalErrorType, uriErrorType);
    assertTrue(union.isSubtype(unknownType));
    assertTrue(union.isSubtype(allType));
    assertTrue(union.isSubtype(errorType));
    assertFalse(union.isSubtype(numberType));
  }

  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    UnionType union = createUnion(nullType, numberType);
    JSType trueOutcome = union.getRestrictedTypeGivenToBooleanOutcome(true);
    assertEquals(numberType, trueOutcome);

    JSType falseOutcome = union.getRestrictedTypeGivenToBooleanOutcome(false);
    assertTrue(falseOutcome.isUnionType());
  }

  @Test
  public void testGetPossibleToBooleanOutcomes() {
    UnionType union = createUnion(stringType, numberType);
    assertEquals(BooleanLiteralSet.BOTH, union.getPossibleToBooleanOutcomes());
  }

  @Test
  public void testTypePairsUnderEqualityAndInequality() {
    UnionType union = createUnion(numberType, nullType);
    TypePair eq = union.getTypesUnderEquality(nullType);
    assertNotNull(eq.typeA);
    assertNotNull(eq.typeB);

    TypePair ineq = union.getTypesUnderInequality(nullType);
    assertNotNull(ineq.typeA);
    assertNotNull(ineq.typeB);

    TypePair shallowIneq = union.getTypesUnderShallowInequality(nullType);
    assertNotNull(shallowIneq.typeA);
    assertNotNull(shallowIneq.typeB);
  }

  @Test
  public void testVisit() {
    UnionType union = createUnion(numberType, stringType);
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseUnionType(UnionType type) { return "union"; }
      @Override public String caseNoType(NoType type) { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enum"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "no_obj"; }
      @Override public String caseFunctionType(FunctionType type) { return "fn"; }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNamedType(NamedType type) { return "named"; }
      @Override public String caseNumberType() { return "num"; }
      @Override public String caseStringType() { return "str"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseTemplatizedType(TemplatizedType type) { return "templated"; }
      @Override public String caseTemplateType(TemplateType type) { return "template"; }
    };
    assertEquals("union", union.visit(visitor));
  }

  @Test
  public void testResolveInternal() {
    UnionType union = createUnion(numberType, stringType);
    assertSame(union, union.resolveInternal(null, null));
  }

  @Test
  public void testToDebugHashCodeString() {
    UnionType union = createUnion(numberType, stringType);
    String debugStr = union.toDebugHashCodeString();
    assertTrue(debugStr.startsWith("{("));
    assertTrue(debugStr.endsWith(")}"));
  }

  @Test
  public void testSetValidatorAndMatchConstraint() {
    UnionType union = createUnion(numberType, stringType);
    Predicate<JSType> dummyPredicate = new Predicate<JSType>() {
      @Override public boolean apply(JSType input) { return true; }
    };
    assertTrue(union.setValidator(dummyPredicate));
    union.matchConstraint(numberType);
  }

  @Test
  public void testCollapseUnion() {
    UnionType withUnknown = new UnionType(registry, ImmutableList.of(unknownType, numberType));
    assertEquals(unknownType, withUnknown.collapseUnion());

    UnionType singleValue = new UnionType(registry, ImmutableList.of(numberType));
    assertEquals(numberType, singleValue.collapseUnion());

    UnionType multiValue = createUnion(numberType, stringType);
    assertEquals(allType, multiValue.collapseUnion());

    UnionType mixValueObj = createUnion(numberType, objectType);
    assertEquals(allType, mixValueObj.collapseUnion());

    UnionType multiObj = createUnion(evalErrorType, uriErrorType);
    assertEquals(errorType, multiObj.collapseUnion());
  }

  @Test
  public void testHasAnyTemplateInternal() {
    UnionType union = createUnion(numberType, stringType);
    assertFalse(union.hasAnyTemplateInternal());

    TemplateType templateType = new TemplateType(registry, "T");
    UnionType templatedUnion = new UnionType(registry, ImmutableList.of(numberType, templateType));
    assertTrue(templatedUnion.hasAnyTemplateInternal());
  }
}
