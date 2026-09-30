package com.google.javascript.rhino.jstype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSType.TypePair;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UnionTypeTest {

  private JSTypeRegistry registry;
  private JSType NUMBER;
  private JSType STRING;
  private JSType BOOLEAN;
  private JSType NULL;
  private JSType VOID;
  private JSType UNKNOWN_TYPE;
  private JSType ALL_TYPE;
  private JSType OBJECT_TYPE;
  private JSType NO_TYPE;
  private JSType NO_OBJECT_TYPE;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    NUMBER = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    STRING = registry.getNativeType(JSTypeNative.STRING_TYPE);
    BOOLEAN = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    NULL = registry.getNativeType(JSTypeNative.NULL_TYPE);
    VOID = registry.getNativeType(JSTypeNative.VOID_TYPE);
    UNKNOWN_TYPE = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    ALL_TYPE = registry.getNativeType(JSTypeNative.ALL_TYPE);
    OBJECT_TYPE = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    NO_TYPE = registry.getNativeType(JSTypeNative.NO_TYPE);
    NO_OBJECT_TYPE = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
  }

  private UnionType createUnion(JSType... types) {
    Set<JSType> alternates = new HashSet<JSType>();
    for (JSType type : types) {
      alternates.add(type);
    }
    return new UnionType(registry, alternates);
  }

  @Test
  public void testGetAlternates() {
    UnionType union = createUnion(NUMBER, STRING);
    Set<JSType> expected = ImmutableSet.of(NUMBER, STRING);
    Set<JSType> actual = new HashSet<JSType>();
    for (JSType t : union.getAlternates()) {
      actual.add(t);
    }
    assertEquals(expected, actual);
  }

  @Test
  public void testForgiveUnknownNames() {
    UnionType union = createUnion(NUMBER, STRING);
    union.forgiveUnknownNames();
    assertTrue(true);
  }

  @Test
  public void testMatchesNumberContext() {
    UnionType union1 = createUnion(NUMBER, STRING);
    assertTrue(union1.matchesNumberContext());

    UnionType union2 = createUnion(VOID, NULL);
    assertFalse(union2.matchesNumberContext());
  }

  @Test
  public void testMatchesStringContext() {
    UnionType union1 = createUnion(STRING, NUMBER);
    assertTrue(union1.matchesStringContext());

    UnionType unionVoid = createUnion(VOID);
    assertFalse(unionVoid.matchesStringContext());
  }

  @Test
  public void testMatchesObjectContext() {
    UnionType unionObj = createUnion(OBJECT_TYPE, NUMBER);
    assertTrue(unionObj.matchesObjectContext());

    UnionType unionNullVoid = createUnion(NULL, VOID);
    assertFalse(unionNullVoid.matchesObjectContext());
  }

  @Test
  public void testFindPropertyType() {
    ObjectType objA = registry.createAnonymousObjectType();
    objA.defineDeclaredProperty("propA", NUMBER, null);
    objA.defineDeclaredProperty("shared", STRING, null);

    ObjectType objB = registry.createAnonymousObjectType();
    objB.defineDeclaredProperty("propB", BOOLEAN, null);
    objB.defineDeclaredProperty("shared", NUMBER, null);

    UnionType union = createUnion(objA, objB, NULL, VOID);

    JSType sharedType = union.findPropertyType("shared");
    assertNotNull(sharedType);
    assertTrue(sharedType.isUnionType());

    JSType propAType = union.findPropertyType("propA");
    assertNotNull(propAType);
    assertEquals(NUMBER, propAType);

    JSType propNonExistent = union.findPropertyType("nonExistent");
    assertNull(propNonExistent);
  }

  @Test
  public void testCanAssignTo() {
    UnionType numStr = createUnion(NUMBER, STRING);
    assertTrue(numStr.canAssignTo(ALL_TYPE));
    assertFalse(numStr.canAssignTo(NUMBER));

    UnionType withUnknown = createUnion(UNKNOWN_TYPE, NUMBER);
    assertTrue(withUnknown.canAssignTo(STRING));
  }

  @Test
  public void testCanBeCalled() {
    FunctionType func1 = registry.createFunctionType(NUMBER);
    FunctionType func2 = registry.createFunctionType(STRING);

    UnionType callables = createUnion(func1, func2);
    assertTrue(callables.canBeCalled());

    UnionType mixed = createUnion(func1, NUMBER);
    assertFalse(mixed.canBeCalled());
  }

  @Test
  public void testRestrictByNotNullOrUndefined() {
    UnionType union = createUnion(NUMBER, NULL, VOID);
    JSType restricted = union.restrictByNotNullOrUndefined();
    assertEquals(NUMBER, restricted);
  }

  @Test
  public void testTestForEquality() {
    UnionType numStr = createUnion(NUMBER, STRING);
    assertEquals(TernaryValue.UNKNOWN, numStr.testForEquality(NUMBER));

    UnionType numOnly = createUnion(NUMBER);
    assertEquals(TernaryValue.UNKNOWN, numOnly.testForEquality(STRING));

    UnionType nullOnly = createUnion(NULL);
    assertEquals(TernaryValue.TRUE, nullOnly.testForEquality(NULL));
  }

  @Test
  public void testIsNullable() {
    UnionType unionWithNull = createUnion(NUMBER, NULL);
    assertTrue(unionWithNull.isNullable());

    UnionType unionNoNull = createUnion(NUMBER, BOOLEAN);
    assertFalse(unionNoNull.isNullable());
  }

  @Test
  public void testIsUnknownType() {
    UnionType unionWithUnknown = createUnion(NUMBER, UNKNOWN_TYPE);
    assertTrue(unionWithUnknown.isUnknownType());

    UnionType unionNormal = createUnion(NUMBER, STRING);
    assertFalse(unionNormal.isUnknownType());
  }

  @Test
  public void testGetLeastSupertype() {
    UnionType numStr = createUnion(NUMBER, STRING);
    assertSame(numStr, numStr.getLeastSupertype(NUMBER));

    JSType superType = numStr.getLeastSupertype(BOOLEAN);
    assertTrue(superType.isUnionType());
    UnionType unionSuper = (UnionType) superType;
    assertTrue(unionSuper.contains(NUMBER));
    assertTrue(unionSuper.contains(STRING));
    assertTrue(unionSuper.contains(BOOLEAN));
  }

  @Test
  public void testMeet() {
    UnionType numStr = createUnion(NUMBER, STRING);
    UnionType strBool = createUnion(STRING, BOOLEAN);

    JSType result1 = numStr.meet(strBool);
    assertEquals(STRING, result1);

    ObjectType objA = registry.createAnonymousObjectType();
    ObjectType objB = registry.createAnonymousObjectType();
    UnionType objUnion1 = createUnion(objA);
    UnionType objUnion2 = createUnion(objB);

    JSType objMeet = objUnion1.meet(objUnion2);
    assertEquals(NO_OBJECT_TYPE, objMeet);

    JSType primMeet = numStr.meet(BOOLEAN);
    assertEquals(NO_TYPE, primMeet);
  }

  @Test
  public void testEqualsAndHashCode() {
    UnionType union1 = createUnion(NUMBER, STRING);
    UnionType union2 = createUnion(STRING, NUMBER);
    UnionType union3 = createUnion(NUMBER, BOOLEAN);

    assertEquals(union1, union2);
    assertEquals(union1.hashCode(), union2.hashCode());
    assertFalse(union1.equals(union3));
    assertFalse(union1.equals(NUMBER));
    assertFalse(union1.equals(null));
  }

  @Test
  public void testIsUnionType() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.isUnionType());
  }

  @Test
  public void testIsObject() {
    ObjectType objA = registry.createAnonymousObjectType();
    ObjectType objB = registry.createAnonymousObjectType();

    UnionType objUnion = createUnion(objA, objB);
    assertTrue(objUnion.isObject());

    UnionType mixedUnion = createUnion(objA, NUMBER);
    assertFalse(mixedUnion.isObject());
  }

  @Test
  public void testContains() {
    UnionType union = createUnion(NUMBER, STRING);
    assertTrue(union.contains(NUMBER));
    assertTrue(union.contains(STRING));
    assertFalse(union.contains(BOOLEAN));
  }

  @Test
  public void testGetRestrictedUnion() {
    UnionType numStr = createUnion(NUMBER, STRING);
    JSType restricted = numStr.getRestrictedUnion(NUMBER);
    assertEquals(STRING, restricted);

    UnionType withUnknown = createUnion(UNKNOWN_TYPE, NUMBER);
    JSType restUnknown = withUnknown.getRestrictedUnion(NUMBER);
    assertEquals(UNKNOWN_TYPE, restUnknown);
  }

  @Test
  public void testToString() {
    UnionType union = createUnion(NUMBER, STRING);
    String str = union.toString();
    assertTrue(str.equals("(number|string)") || str.equals("(string|number)"));
  }

  @Test
  public void testIsSubtype() {
    UnionType numStr = createUnion(NUMBER, STRING);
    assertTrue(numStr.isSubtype(ALL_TYPE));
    assertFalse(numStr.isSubtype(NUMBER));

    UnionType numOnly = createUnion(NUMBER);
    assertTrue(numOnly.isSubtype(NUMBER));
  }

  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    UnionType union = createUnion(NULL, STRING);
    JSType trueOutcome = union.getRestrictedTypeGivenToBooleanOutcome(true);
    assertEquals(STRING, trueOutcome);

    JSType falseOutcome = union.getRestrictedTypeGivenToBooleanOutcome(false);
    assertEquals(NULL, falseOutcome);
  }

  @Test
  public void testGetPossibleToBooleanOutcomes() {
    UnionType numStr = createUnion(NUMBER, STRING);
    assertEquals(BooleanLiteralSet.BOTH, numStr.getPossibleToBooleanOutcomes());

    UnionType nullOnly = createUnion(NULL);
    assertEquals(BooleanLiteralSet.FALSE, nullOnly.getPossibleToBooleanOutcomes());
  }

  @Test
  public void testGetTypesUnderEqualityAndInequality() {
    UnionType numStr = createUnion(NUMBER, STRING);

    TypePair eqPair = numStr.getTypesUnderEquality(STRING);
    assertNotNull(eqPair);
    assertNotNull(eqPair.typeA);
    assertNotNull(eqPair.typeB);

    TypePair ineqPair = numStr.getTypesUnderInequality(STRING);
    assertNotNull(ineqPair);
    assertNotNull(ineqPair.typeA);
    assertNotNull(ineqPair.typeB);

    TypePair shallowIneqPair = numStr.getTypesUnderShallowInequality(STRING);
    assertNotNull(shallowIneqPair);
    assertNotNull(shallowIneqPair.typeA);
    assertNotNull(shallowIneqPair.typeB);
  }

  @Test
  public void testVisit() {
    UnionType union = createUnion(NUMBER, STRING);
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return "no"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "enumElement"; }
      @Override public String caseAllType() { return "all"; }
      @Override public String caseBooleanType() { return "bool"; }
      @Override public String caseNoObjectType() { return "noObj"; }
      @Override public String caseFunctionType(FunctionType type) { return "func"; }
      @Override public String caseObjectType(ObjectType type) { return "obj"; }
      @Override public String caseUnknownType() { return "unknown"; }
      @Override public String caseNullType() { return "null"; }
      @Override public String caseNumberType() { return "number"; }
      @Override public String caseStringType() { return "string"; }
      @Override public String caseVoidType() { return "void"; }
      @Override public String caseUnionType(UnionType type) { return "union"; }
    };
    assertEquals("union", union.visit(visitor));
  }

  @Test
  public void testResolveInternal() {
    UnionType union = createUnion(NUMBER, STRING);
    ErrorReporter reporter = null;
    StaticScope<JSType> scope = null;
    JSType resolved = union.resolveInternal(reporter, scope);
    assertSame(union, resolved);
  }
}