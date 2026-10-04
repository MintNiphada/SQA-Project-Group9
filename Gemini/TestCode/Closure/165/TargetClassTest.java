package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableMap;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TargetClassTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType unknownType;
  private JSType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  private RecordType createRecordType(Map<String, JSType> props) {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (Map.Entry<String, JSType> entry : props.entrySet()) {
      builder.addProperty(entry.getKey(), entry.getValue(), null);
    }
    return (RecordType) builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructorWithNullRecordPropertyThrowsException() {
    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("prop", null);
    new RecordType(registry, map);
  }

  @Test
  public void testGetImplicitPrototype() {
    RecordType rec = createRecordType(Collections.<String, JSType>emptyMap());
    Assert.assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), rec.getImplicitPrototype());
  }

  @Test
  public void testToMaybeRecordType() {
    RecordType rec = createRecordType(Collections.<String, JSType>emptyMap());
    Assert.assertSame(rec, rec.toMaybeRecordType());
  }

  @Test
  public void testDefinePropertyWhenFrozen() {
    RecordType rec = createRecordType(Collections.<String, JSType>emptyMap());
    boolean result = rec.defineProperty("newProp", numberType, false, null);
    Assert.assertFalse(result);
    Assert.assertFalse(rec.hasProperty("newProp"));
  }

  @Test
  public void testIsEquivalentTo() {
    RecordType rec1 = createRecordType(ImmutableMap.of("a", numberType, "b", stringType));
    RecordType rec2 = createRecordType(ImmutableMap.of("a", numberType, "b", stringType));
    RecordType rec3 = createRecordType(ImmutableMap.of("a", numberType));
    RecordType rec4 = createRecordType(ImmutableMap.of("a", stringType, "b", stringType));

    Assert.assertTrue(rec1.isEquivalentTo(rec1));
    Assert.assertTrue(rec1.isEquivalentTo(rec2));
    Assert.assertTrue(rec2.isEquivalentTo(rec1));

    Assert.assertFalse(rec1.isEquivalentTo(numberType));
    Assert.assertFalse(rec1.isEquivalentTo(rec3));
    Assert.assertFalse(rec1.isEquivalentTo(rec4));
  }

  @Test
  public void testIsSubtype() {
    RecordType recA = createRecordType(ImmutableMap.of("a", numberType));
    RecordType recAB = createRecordType(ImmutableMap.of("a", numberType, "b", stringType));
    RecordType recDifferent = createRecordType(ImmutableMap.of("a", stringType));

    Assert.assertTrue(recA.isSubtype(recA));
    Assert.assertTrue(recAB.isSubtype(recA));
    Assert.assertFalse(recA.isSubtype(recAB));
    Assert.assertFalse(recA.isSubtype(recDifferent));

    Assert.assertTrue(recA.isSubtype(objectType));
    Assert.assertTrue(recA.isSubtype(registry.getNativeType(JSTypeNative.ALL_TYPE)));
    Assert.assertFalse(recA.isSubtype(numberType));
  }

  @Test
  public void testIsSubtypeStaticBranching() {
    RecordType recExpected = createRecordType(ImmutableMap.of("a", numberType));
    
    ObjectType objWithInferredProp = new PrototypeObjectType(registry, "InferredObj", null);
    objWithInferredProp.defineProperty("a", numberType, true, null);
    Assert.assertTrue(RecordType.isSubtype(objWithInferredProp, recExpected));

    ObjectType objWithInferredMismatch = new PrototypeObjectType(registry, "InferredMismatch", null);
    objWithInferredMismatch.defineProperty("a", stringType, true, null);
    Assert.assertFalse(RecordType.isSubtype(objWithInferredMismatch, recExpected));

    ObjectType objMissingProp = new PrototypeObjectType(registry, "MissingProp", null);
    Assert.assertFalse(RecordType.isSubtype(objMissingProp, recExpected));

    RecordType recUnknown = createRecordType(ImmutableMap.of("a", unknownType));
    Assert.assertTrue(RecordType.isSubtype(recExpected, recUnknown));
    Assert.assertTrue(RecordType.isSubtype(recUnknown, recExpected));
  }

  @Test
  public void testGetGreatestSubtypeHelperWithRecordType() {
    RecordType rec1 = createRecordType(ImmutableMap.of("a", numberType));
    RecordType rec2 = createRecordType(ImmutableMap.of("b", stringType));
    JSType combined = rec1.getGreatestSubtypeHelper(rec2);
    Assert.assertTrue(combined.isRecordType());
    Assert.assertTrue(combined.toMaybeRecordType().hasProperty("a"));
    Assert.assertTrue(combined.toMaybeRecordType().hasProperty("b"));

    RecordType recConflict = createRecordType(ImmutableMap.of("a", stringType));
    JSType conflictRes = rec1.getGreatestSubtypeHelper(recConflict);
    Assert.assertTrue(conflictRes.isNoType());
  }

  @Test
  public void testGetGreatestSubtypeHelperWithNonRecordType() {
    RecordType rec = createRecordType(ImmutableMap.of("prop", numberType));
    JSType res = rec.getGreatestSubtypeHelper(numberType);
    Assert.assertTrue(res.isNoObjectType());

    ObjectType customObj = new PrototypeObjectType(registry, "custom", null);
    customObj.defineProperty("prop", numberType, false, null);
    registry.registerIndex("prop", customObj);

    JSType subRes = rec.getGreatestSubtypeHelper(customObj);
    Assert.assertNotNull(subRes);
  }

  @Test
  public void testResolveInternal() {
    NamedType unres = new NamedType(registry, "CustomType", null, 0, 0);
    RecordType rec = createRecordType(ImmutableMap.<String, JSType>of("x", unres));
    ObjectType resolvedObj = new PrototypeObjectType(registry, "CustomType", null);
    registry.declareType("CustomType", resolvedObj);

    JSType resolved = rec.resolve(null, null);
    Assert.assertTrue(resolved.isRecordType());
    Assert.assertEquals(resolvedObj, resolved.toMaybeRecordType().getPropertyType("x"));
  }
}
