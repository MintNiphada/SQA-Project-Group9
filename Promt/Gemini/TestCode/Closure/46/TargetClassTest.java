package com.google.javascript.rhino.jstype;

import com.google.common.collect.Maps;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.jstype.RecordTypeBuilder.RecordProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecordTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;
  private JSType allType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  private RecordType createRecordType(Map<String, JSType> props) {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    for (Map.Entry<String, JSType> entry : props.entrySet()) {
      builder.addProperty(entry.getKey(), entry.getValue(), null);
    }
    return (RecordType) builder.build();
  }

  @Test
  public void testConstructorWithNullPropertyThrowsException() {
    Map<String, RecordProperty> map = new HashMap<String, RecordProperty>();
    map.put("a", null);
    try {
      new RecordType(registry, map);
      Assert.fail("Expected IllegalStateException for null RecordProperty");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("RecordProperty associated with a property should not be null"));
    }
  }

  @Test
  public void testGetImplicitPrototype() {
    RecordType record = createRecordType(Collections.<String, JSType>singletonMap("a", numberType));
    Assert.assertEquals(objectType, record.getImplicitPrototype());
  }

  @Test
  public void testDefinePropertyFailsWhenFrozen() {
    RecordType record = createRecordType(Collections.<String, JSType>singletonMap("a", numberType));
    boolean result = record.defineProperty("b", stringType, false, null);
    Assert.assertFalse(result);
    Assert.assertFalse(record.hasProperty("b"));
  }

  @Test
  public void testToMaybeRecordType() {
    RecordType record = createRecordType(Collections.<String, JSType>singletonMap("a", numberType));
    Assert.assertSame(record, record.toMaybeRecordType());
  }

  @Test
  public void testIsEquivalentTo() {
    Map<String, JSType> propsA = new HashMap<String, JSType>();
    propsA.put("a", numberType);
    propsA.put("b", stringType);

    Map<String, JSType> propsB = new HashMap<String, JSType>();
    propsB.put("a", numberType);
    propsB.put("b", stringType);

    Map<String, JSType> propsDiffType = new HashMap<String, JSType>();
    propsDiffType.put("a", numberType);
    propsDiffType.put("b", booleanType);

    Map<String, JSType> propsDiffKeys = new HashMap<String, JSType>();
    propsDiffKeys.put("a", numberType);
    propsDiffKeys.put("c", stringType);

    Map<String, JSType> propsSubset = new HashMap<String, JSType>();
    propsSubset.put("a", numberType);

    RecordType recA = createRecordType(propsA);
    RecordType recB = createRecordType(propsB);
    RecordType recDiffType = createRecordType(propsDiffType);
    RecordType recDiffKeys = createRecordType(propsDiffKeys);
    RecordType recSubset = createRecordType(propsSubset);

    Assert.assertTrue(recA.isEquivalentTo(recA));
    Assert.assertTrue(recA.isEquivalentTo(recB));
    Assert.assertTrue(recB.isEquivalentTo(recA));

    Assert.assertFalse(recA.isEquivalentTo(recDiffType));
    Assert.assertFalse(recA.isEquivalentTo(recDiffKeys));
    Assert.assertFalse(recA.isEquivalentTo(recSubset));
    Assert.assertFalse(recA.isEquivalentTo(numberType));
    Assert.assertFalse(recA.isEquivalentTo(objectType));
  }

  @Test
  public void testGetLeastSupertype() {
    Map<String, JSType> mapA = new HashMap<String, JSType>();
    mapA.put("a", numberType);
    mapA.put("b", stringType);
    mapA.put("c", booleanType);
    RecordType recA = createRecordType(mapA);

    Map<String, JSType> mapB = new HashMap<String, JSType>();
    mapB.put("a", numberType);
    mapB.put("b", numberType); // different type
    mapB.put("d", stringType); // different property
    RecordType recB = createRecordType(mapB);

    JSType leastSuperRecord = recA.getLeastSupertype(recB);
    Assert.assertTrue(leastSuperRecord.isRecordType());
    RecordType leastRecord = leastSuperRecord.toMaybeRecordType();
    Assert.assertTrue(leastRecord.hasProperty("a"));
    Assert.assertEquals(numberType, leastRecord.getPropertyType("a"));
    Assert.assertFalse(leastRecord.hasProperty("b"));
    Assert.assertFalse(leastRecord.hasProperty("c"));
    Assert.assertFalse(leastRecord.hasProperty("d"));

    // Least supertype with non-record type
    JSType leastSuperNonRecord = recA.getLeastSupertype(numberType);
    Assert.assertFalse(leastSuperNonRecord.isRecordType());
  }

  @Test
  public void testGetGreatestSubtypeHelperWithRecordTypes() {
    Map<String, JSType> mapA = new HashMap<String, JSType>();
    mapA.put("a", numberType);
    RecordType recA = createRecordType(mapA);

    Map<String, JSType> mapB = new HashMap<String, JSType>();
    mapB.put("b", stringType);
    RecordType recB = createRecordType(mapB);

    // Compatible properties combination
    JSType greatest = recA.getGreatestSubtypeHelper(recB);
    Assert.assertTrue(greatest.isRecordType());
    RecordType greatestRec = greatest.toMaybeRecordType();
    Assert.assertTrue(greatestRec.hasProperty("a"));
    Assert.assertTrue(greatestRec.hasProperty("b"));
    Assert.assertEquals(numberType, greatestRec.getPropertyType("a"));
    Assert.assertEquals(stringType, greatestRec.getPropertyType("b"));

    // Conflicting properties
    Map<String, JSType> mapConflict = new HashMap<String, JSType>();
    mapConflict.put("a", stringType);
    RecordType recConflict = createRecordType(mapConflict);

    JSType conflictSubtype = recA.getGreatestSubtypeHelper(recConflict);
    Assert.assertTrue(conflictSubtype.isNoType());
  }

  @Test
  public void testGetGreatestSubtypeHelperWithNonRecordTypes() {
    Map<String, JSType> mapA = new HashMap<String, JSType>();
    mapA.put("a", numberType);
    RecordType recA = createRecordType(mapA);

    // Number type restricted to Object is empty (not an object type)
    JSType subNumber = recA.getGreatestSubtypeHelper(numberType);
    Assert.assertTrue(subNumber.isNoObjectType());

    // Object type containing reference types
    JSType subObject = recA.getGreatestSubtypeHelper(objectType);
    Assert.assertNotNull(subObject);
  }

  @Test
  public void testIsSubtype() {
    Map<String, JSType> mapAB = new HashMap<String, JSType>();
    mapAB.put("a", numberType);
    mapAB.put("b", stringType);
    RecordType recAB = createRecordType(mapAB);

    Map<String, JSType> mapA = new HashMap<String, JSType>();
    mapA.put("a", numberType);
    RecordType recA = createRecordType(mapA);

    // {a: number, b: string} is a subtype of {a: number}
    Assert.assertTrue(recAB.isSubtype(recA));
    // {a: number} is NOT a subtype of {a: number, b: string}
    Assert.assertFalse(recA.isSubtype(recAB));

    // Subtype with Object and All types
    Assert.assertTrue(recAB.isSubtype(objectType));
    Assert.assertTrue(recAB.isSubtype(allType));
    Assert.assertTrue(recAB.isSubtype(unknownType));
    Assert.assertFalse(recAB.isSubtype(numberType));

    // Empty record type is supertype of any record type
    RecordType emptyRecord = createRecordType(Collections.<String, JSType>emptyMap());
    Assert.assertTrue(recAB.isSubtype(emptyRecord));
    Assert.assertTrue(recA.isSubtype(emptyRecord));
  }

  @Test
  public void testStaticIsSubtypeDeclaredAndInferredProperties() {
    Map<String, JSType> map = new HashMap<String, JSType>();
    map.put("a", registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE));
    RecordType rec = createRecordType(map);

    // Object with declared property
    ObjectType declaredObj = new FunctionBuilder(registry).build().getInstanceType();
    declaredObj.defineDeclaredProperty("a", registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE), null);
    Assert.assertTrue(RecordType.isSubtype(declaredObj, rec));

    // Object with declared property of incompatible type
    ObjectType badDeclaredObj = new FunctionBuilder(registry).build().getInstanceType();
    badDeclaredObj.defineDeclaredProperty("a", stringType, null);
    Assert.assertFalse(RecordType.isSubtype(badDeclaredObj, rec));

    // Object with inferred property (subtype allowed)
    ObjectType inferredObj = new FunctionBuilder(registry).build().getInstanceType();
    inferredObj.defineInferredProperty("a", registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE), null);
    Assert.assertTrue(RecordType.isSubtype(inferredObj, rec));

    // Object missing property
    ObjectType missingPropObj = new FunctionBuilder(registry).build().getInstanceType();
    Assert.assertFalse(RecordType.isSubtype(missingPropObj, rec));
  }

  @Test
  public void testStaticIsSubtypeWithUnknownTypes() {
    Map<String, JSType> mapUnknown = new HashMap<String, JSType>();
    mapUnknown.put("a", unknownType);
    RecordType recUnknown = createRecordType(mapUnknown);

    ObjectType objNumber = new FunctionBuilder(registry).build().getInstanceType();
    objNumber.defineDeclaredProperty("a", numberType, null);

    // When either prop is unknown, comparison succeeds
    Assert.assertTrue(RecordType.isSubtype(objNumber, recUnknown));

    Map<String, JSType> mapNumber = new HashMap<String, JSType>();
    mapNumber.put("a", numberType);
    RecordType recNumber = createRecordType(mapNumber);

    ObjectType objUnknown = new FunctionBuilder(registry).build().getInstanceType();
    objUnknown.defineDeclaredProperty("a", unknownType, null);
    Assert.assertTrue(RecordType.isSubtype(objUnknown, recNumber));
  }

  @Test
  public void testResolveInternal() {
    NamedType unresolvedNamedType = new NamedType(registry, "SomeType", null, 0, 0);
    Map<String, RecordProperty> propMap = Maps.newHashMap();
    Node node = new Node(0);
    propMap.put("prop", new RecordProperty(unresolvedNamedType, node));

    RecordType record = new RecordType(registry, propMap);

    // Resolve internal with scope
    StaticScope<JSType> scope = registry.getTopScope();
    JSType resolved = record.resolve(errorReporter, scope);
    Assert.assertNotNull(resolved);
    Assert.assertTrue(resolved.isRecordType());
  }
}