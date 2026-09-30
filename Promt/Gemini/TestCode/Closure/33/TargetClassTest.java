package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrototypeObjectTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private ObjectType objectPrototype;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    objectPrototype = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
  }

  @Test
  public void testConstructorAndDefaults() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", null);
    Assert.assertEquals("Foo", type.getReferenceName());
    Assert.assertTrue(type.hasReferenceName());
    Assert.assertFalse(type.isNativeObjectType());
    Assert.assertEquals(objectPrototype, type.getImplicitPrototype());
    Assert.assertNull(type.getConstructor());
    Assert.assertNull(type.getOwnerFunction());
    Assert.assertFalse(type.isPrettyPrint());

    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    Assert.assertNull(anon.getReferenceName());
    Assert.assertFalse(anon.hasReferenceName());

    PrototypeObjectType nativeObj = new PrototypeObjectType(registry, "NativeObj", null, true);
    Assert.assertTrue(nativeObj.isNativeObjectType());
    Assert.assertNull(nativeObj.getImplicitPrototype());
  }

  @Test
  public void testPropertyDefinitionAndRemoval() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", null);
    Node node = Node.newString("prop");

    Assert.assertFalse(type.hasOwnProperty("prop"));
    Assert.assertFalse(type.hasProperty("prop"));
    Assert.assertEquals(0, type.getPropertiesCount());

    boolean defined = type.defineProperty("prop", numberType, false, node);
    Assert.assertTrue(defined);
    Assert.assertTrue(type.hasOwnProperty("prop"));
    Assert.assertTrue(type.hasProperty("prop"));
    Assert.assertEquals(1, type.getPropertiesCount());
    Assert.assertEquals(numberType, type.getPropertyType("prop"));
    Assert.assertEquals(node, type.getPropertyNode("prop"));
    Assert.assertTrue(type.isPropertyTypeDeclared("prop"));
    Assert.assertFalse(type.isPropertyTypeInferred("prop"));
    Assert.assertFalse(type.isPropertyInExterns("prop"));

    // Redefining a declared property should return false
    boolean reDefined = type.defineProperty("prop", stringType, true, node);
    Assert.assertFalse(reDefined);

    // Remove property
    boolean removed = type.removeProperty("prop");
    Assert.assertTrue(removed);
    Assert.assertFalse(type.hasOwnProperty("prop"));
    Assert.assertFalse(type.removeProperty("prop"));
  }

  @Test
  public void testInferredPropertyAndDocInfo() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", null);
    Node node = Node.newString("inferredProp");

    boolean defined = type.defineProperty("inferredProp", stringType, true, node);
    Assert.assertTrue(defined);
    Assert.assertTrue(type.isPropertyTypeInferred("inferredProp"));
    Assert.assertFalse(type.isPropertyTypeDeclared("inferredProp"));

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordDescription("test doc");
    JSDocInfo docInfo = docBuilder.build(node);

    type.setPropertyJSDocInfo("inferredProp", docInfo);
    Assert.assertEquals(docInfo, type.getOwnPropertyJSDocInfo("inferredProp"));

    // Redefining inferred property keeps old JSDocInfo
    type.defineProperty("inferredProp", numberType, true, node);
    Assert.assertEquals(docInfo, type.getOwnPropertyJSDocInfo("inferredProp"));

    // Setting doc info on a non-existent property defines it as inferred
    JSDocInfo info2 = new JSDocInfoBuilder(true).build(node);
    type.setPropertyJSDocInfo("newDocProp", info2);
    Assert.assertTrue(type.hasOwnProperty("newDocProp"));
    Assert.assertTrue(type.isPropertyTypeInferred("newDocProp"));
    Assert.assertEquals(info2, type.getOwnPropertyJSDocInfo("newDocProp"));

    // Non-existent property doc info lookup
    Assert.assertNull(type.getOwnPropertyJSDocInfo("nonExistent"));
  }

  @Test
  public void testPrototypeHierarchyAndPropertyInheritance() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    Node parentNode = Node.newString("parentProp");
    parent.defineProperty("parentProp", numberType, false, parentNode);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    Node childNode = Node.newString("childProp");
    child.defineProperty("childProp", stringType, false, childNode);

    Assert.assertTrue(child.hasProperty("childProp"));
    Assert.assertTrue(child.hasProperty("parentProp"));
    Assert.assertFalse(child.hasOwnProperty("parentProp"));
    Assert.assertTrue(child.hasOwnProperty("childProp"));

    Assert.assertEquals(numberType, child.getPropertyType("parentProp"));
    Assert.assertEquals(stringType, child.getPropertyType("childProp"));
    Assert.assertEquals(unknownType, child.getPropertyType("unknownProp"));

    Assert.assertEquals(parentNode, child.getPropertyNode("parentProp"));
    Assert.assertEquals(childNode, child.getPropertyNode("childProp"));
    Assert.assertNull(child.getPropertyNode("nonExistent"));

    Assert.assertEquals(2, child.getPropertiesCount());

    Set<String> collectedNames = Sets.newHashSet();
    child.collectPropertyNames(collectedNames);
    Assert.assertTrue(collectedNames.contains("parentProp"));
    Assert.assertTrue(collectedNames.contains("childProp"));

    Set<String> ownNames = child.getOwnPropertyNames();
    Assert.assertTrue(ownNames.contains("childProp"));
    Assert.assertFalse(ownNames.contains("parentProp"));
  }

  @Test
  public void testOwnerFunctionAndReferenceName() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null);
    Assert.assertNull(type.getReferenceName());
    Assert.assertFalse(type.hasReferenceName());

    FunctionType fnType = registry.createFunctionType(numberType);
    type.setOwnerFunction(fnType);
    Assert.assertEquals(fnType, type.getOwnerFunction());
    Assert.assertTrue(type.hasReferenceName());
    Assert.assertNotNull(type.getReferenceName());
    Assert.assertTrue(type.getReferenceName().endsWith(".prototype"));

    // Cannot set another owner function unless cleared to null
    try {
      FunctionType fnType2 = registry.createFunctionType(stringType);
      type.setOwnerFunction(fnType2);
      Assert.fail("Expected IllegalStateException on re-setting owner function");
    } catch (IllegalStateException expected) {
    }

    type.setOwnerFunction(null);
    Assert.assertNull(type.getOwnerFunction());
  }

  @Test
  public void testPrettyPrintToStringHelper() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, objectPrototype);
    Assert.assertEquals("{...}", anon.toStringHelper(false));
    Assert.assertEquals("?", anon.toStringHelper(true));

    anon.setPrettyPrint(true);
    Assert.assertTrue(anon.isPrettyPrint());
    Assert.assertEquals("{}", anon.toStringHelper(false));

    anon.defineProperty("a", numberType, false, null);
    anon.defineProperty("b", stringType, false, null);
    Assert.assertEquals("{a: number, b: string}", anon.toStringHelper(false));
    Assert.assertEquals("{a: number, b: string}", anon.toStringHelper(true));

    anon.defineProperty("c", booleanType, false, null);
    anon.defineProperty("d", numberType, false, null);
    anon.defineProperty("e", stringType, false, null);
    // 5 properties - over MAX_PRETTY_PRINTED_PROPERTIES (4)
    String formatted = anon.toStringHelper(false);
    Assert.assertTrue(formatted.contains("..."));
    Assert.assertTrue(formatted.startsWith("{"));
    Assert.assertTrue(formatted.endsWith("}"));

    // Named type ignores prettyPrint
    PrototypeObjectType named = new PrototypeObjectType(registry, "MyClass", null);
    named.setPrettyPrint(true);
    Assert.assertEquals("MyClass", named.toStringHelper(false));
  }

  @Test
  public void testContextMatching() {
    PrototypeObjectType custom = new PrototypeObjectType(registry, "Custom", null);
    Assert.assertTrue(custom.matchesObjectContext());
    Assert.assertFalse(custom.canBeCalled());

    ObjectType numberObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    Assert.assertTrue(numberObj.matchesNumberContext());
    Assert.assertTrue(numberObj.matchesStringContext());
    Assert.assertEquals(numberType, numberObj.unboxesTo());

    ObjectType stringObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    Assert.assertTrue(stringObj.matchesNumberContext());
    Assert.assertTrue(stringObj.matchesStringContext());
    Assert.assertEquals(stringType, stringObj.unboxesTo());

    ObjectType booleanObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    Assert.assertTrue(booleanObj.matchesNumberContext());
    Assert.assertTrue(booleanObj.matchesStringContext());
    Assert.assertEquals(booleanType, booleanObj.unboxesTo());

    ObjectType dateObj = registry.getNativeObjectType(JSTypeNative.DATE_TYPE);
    Assert.assertTrue(dateObj.matchesNumberContext());
    Assert.assertTrue(dateObj.matchesStringContext());

    ObjectType regexpObj = registry.getNativeObjectType(JSTypeNative.REGEXP_TYPE);
    Assert.assertTrue(regexpObj.matchesStringContext());
    Assert.assertTrue(regexpObj.canBeCalled());

    ObjectType arrayObj = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    Assert.assertTrue(arrayObj.matchesStringContext());

    Assert.assertNull(custom.unboxesTo());
  }

  @Test
  public void testSubtyping() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", objectPrototype);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    Assert.assertTrue(child.isSubtype(parent));
    Assert.assertTrue(child.isSubtype(objectPrototype));
    Assert.assertFalse(parent.isSubtype(child));

    // Record type subtyping
    RecordTypeBuilder recordBuilder = new RecordTypeBuilder(registry);
    recordBuilder.addProperty("foo", numberType, null);
    RecordType recordType = recordBuilder.build();

    Assert.assertFalse(parent.isSubtype(recordType));
    parent.defineProperty("foo", numberType, false, null);
    Assert.assertTrue(parent.isSubtype(recordType));

    // Union type subtyping returns false directly in PrototypeObjectType
    JSType union = registry.createUnionType(numberType, stringType);
    Assert.assertFalse(parent.isSubtype(union));
  }

  @Test
  public void testMatchConstraint() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, objectPrototype);

    RecordTypeBuilder recordBuilder = new RecordTypeBuilder(registry);
    recordBuilder.addProperty("propA", numberType, null);
    recordBuilder.addProperty("propB", stringType, null);
    RecordType recordType = recordBuilder.build();

    anon.matchConstraint(recordType);

    Assert.assertTrue(anon.hasProperty("propA"));
    Assert.assertTrue(anon.hasProperty("propB"));
    Assert.assertTrue(anon.isPropertyTypeInferred("propA"));
    Assert.assertTrue(anon.isPropertyTypeInferred("propB"));

    // Matching again should not override declared/inferred properties
    anon.matchConstraint(recordType);
    Assert.assertTrue(anon.hasProperty("propA"));

    // Matching non-record constraint should do nothing
    PrototypeObjectType otherObj = new PrototypeObjectType(registry, "Other", null);
    anon.matchConstraint(otherObj);
  }

  @Test
  public void testResolveInternal() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "TestType", objectPrototype);
    type.defineProperty("prop", numberType, false, null);

    JSType resolved = type.resolveInternal(errorReporter, null);
    Assert.assertSame(type, resolved);
    Assert.assertTrue(type.isResolved());
  }

  @Test
  public void testSetImplicitPrototype() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Custom", null);
    PrototypeObjectType newProto = new PrototypeObjectType(registry, "NewProto", null);

    type.setImplicitPrototype(newProto);
    Assert.assertSame(newProto, type.getImplicitPrototype());
  }

  @Test
  public void testOverriddenNativeProperty() {
    PrototypeObjectType custom = new PrototypeObjectType(registry, "Custom", objectPrototype);
    Assert.assertFalse(custom.matchesNumberContext());

    // Override valueOf with a custom type
    custom.defineProperty("valueOf", stringType, false, null);
    Assert.assertTrue(custom.matchesNumberContext());

    // Override toString with a custom type
    custom.defineProperty("toString", numberType, false, null);
    Assert.assertTrue(custom.matchesStringContext());
  }
}