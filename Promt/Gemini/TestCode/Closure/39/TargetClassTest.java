package com.google.javascript.rhino.jstype;

import com.google.common.collect.Sets;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PrototypeObjectTypeTest {

  private JSTypeRegistry registry;
  private ObjectType objectPrototype;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
    objectPrototype = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
  }

  @Test
  public void testConstructorAndImplicitPrototypeDefault() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", null);
    assertEquals("Foo", type.getReferenceName());
    assertTrue(type.hasReferenceName());
    assertEquals(objectPrototype, type.getImplicitPrototype());
    assertFalse(type.isNativeObjectType());
    assertNull(type.getConstructor());
  }

  @Test
  public void testConstructorNativeType() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "NativeFoo", null, true);
    assertTrue(type.isNativeObjectType());
    assertNull(type.getImplicitPrototype());

    PrototypeObjectType customProto = new PrototypeObjectType(registry, "Custom", objectPrototype, false);
    assertEquals(objectPrototype, customProto.getImplicitPrototype());
    assertFalse(customProto.isNativeObjectType());
  }

  @Test
  public void testPropertyDefinitionAndRemoval() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null);
    Node node = Node.newString("prop");

    assertTrue(type.defineProperty("prop", numberType, false, node));
    assertFalse(type.defineProperty("prop", numberType, false, node)); // already declared

    assertTrue(type.hasProperty("prop"));
    assertTrue(type.hasOwnProperty("prop"));
    assertEquals(numberType, type.getPropertyType("prop"));
    assertEquals(node, type.getPropertyNode("prop"));
    assertTrue(type.isPropertyTypeDeclared("prop"));
    assertFalse(type.isPropertyTypeInferred("prop"));
    assertEquals(1, type.getPropertiesCount());

    assertTrue(type.removeProperty("prop"));
    assertFalse(type.removeProperty("prop"));
    assertFalse(type.hasOwnProperty("prop"));
  }

  @Test
  public void testInferredProperty() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null);
    type.defineInferredProperty("inferredProp", stringType, null);

    assertTrue(type.hasProperty("inferredProp"));
    assertTrue(type.hasOwnProperty("inferredProp"));
    assertFalse(type.isPropertyTypeDeclared("inferredProp"));
    assertTrue(type.isPropertyTypeInferred("inferredProp"));
    assertEquals(stringType, type.getPropertyType("inferredProp"));

    // Redefine inferred property to ensure JSDoc preservation branch is hit
    JSDocInfo info = new JSDocInfo();
    type.setPropertyJSDocInfo("inferredProp", info);
    assertEquals(info, type.getOwnPropertyJSDocInfo("inferredProp"));

    type.defineProperty("inferredProp", stringType, true, null);
    assertEquals(info, type.getOwnPropertyJSDocInfo("inferredProp"));
  }

  @Test
  public void testPropertyInheritanceAndSlots() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    parent.defineProperty("parentProp", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("childProp", stringType, false, null);

    assertTrue(child.hasProperty("parentProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertTrue(child.hasProperty("childProp"));
    assertTrue(child.hasOwnProperty("childProp"));

    assertEquals(numberType, child.getPropertyType("parentProp"));
    assertEquals(stringType, child.getPropertyType("childProp"));

    assertEquals(parent.getPropertiesCount() + 1, child.getPropertiesCount());

    Set<String> names = Sets.newHashSet();
    child.collectPropertyNames(names);
    assertTrue(names.contains("parentProp"));
    assertTrue(names.contains("childProp"));

    Set<String> ownNames = child.getOwnPropertyNames();
    assertTrue(ownNames.contains("childProp"));
    assertFalse(ownNames.contains("parentProp"));
  }

  @Test
  public void testNonExistentPropertyQueries() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null, true);
    assertNull(type.getSlot("nonExistent"));
    assertFalse(type.hasProperty("nonExistent"));
    assertFalse(type.hasOwnProperty("nonExistent"));
    assertFalse(type.isPropertyTypeDeclared("nonExistent"));
    assertFalse(type.isPropertyTypeInferred("nonExistent"));
    assertFalse(type.isPropertyInExterns("nonExistent"));
    assertNull(type.getPropertyNode("nonExistent"));
    assertNull(type.getOwnPropertyJSDocInfo("nonExistent"));
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), type.getPropertyType("nonExistent"));
  }

  @Test
  public void testPropertyInExterns() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    Node externNode = Node.newString("extProp");
    externNode.putIntProp(Node.SOURCENAME_PROP, 1);
    parent.defineProperty("extProp", numberType, false, externNode);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    assertTrue(child.isPropertyInExterns("extProp"));
    assertFalse(child.isPropertyInExterns("unknown"));
  }

  @Test
  public void testSetPropertyJSDocInfoOnNewProperty() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Obj", null);
    JSDocInfo info = new JSDocInfo();
    type.setPropertyJSDocInfo("newProp", info);
    assertTrue(type.hasOwnProperty("newProp"));
    assertEquals(info, type.getOwnPropertyJSDocInfo("newProp"));
  }

  @Test
  public void testContextMatching() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "PlainObj", null);
    assertTrue(type.matchesObjectContext());
    assertFalse(type.canBeCalled());

    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertTrue(numObj.matchesNumberContext());
    assertTrue(numObj.matchesStringContext());
    assertEquals(numberType, numObj.unboxesTo());

    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    assertTrue(strObj.matchesNumberContext());
    assertTrue(strObj.matchesStringContext());
    assertEquals(stringType, strObj.unboxesTo());

    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    assertTrue(boolObj.matchesNumberContext());
    assertTrue(boolObj.matchesStringContext());
    assertEquals(booleanType, boolObj.unboxesTo());

    ObjectType dateObj = registry.getNativeObjectType(JSTypeNative.DATE_TYPE);
    assertTrue(dateObj.matchesNumberContext());
    assertTrue(dateObj.matchesStringContext());

    ObjectType regexpObj = registry.getNativeObjectType(JSTypeNative.REGEXP_TYPE);
    assertTrue(regexpObj.matchesStringContext());
    assertTrue(regexpObj.canBeCalled());

    ObjectType arrayObj = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    assertTrue(arrayObj.matchesStringContext());

    ObjectType theObj = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    assertTrue(theObj.matchesStringContext());
    assertNull(type.unboxesTo());
  }

  @Test
  public void testToStringAndPrettyPrint() {
    PrototypeObjectType named = new PrototypeObjectType(registry, "MyClass", null);
    assertEquals("MyClass", named.toStringHelper(false));

    PrototypeObjectType anonymous = new PrototypeObjectType(registry, null, null, true);
    assertEquals("{...}", anonymous.toStringHelper(false));

    anonymous.setPrettyPrint(true);
    assertTrue(anonymous.isPrettyPrint());
    anonymous.defineProperty("a", numberType, false, null);
    anonymous.defineProperty("b", stringType, false, null);
    String pretty = anonymous.toStringHelper(false);
    assertTrue(pretty.contains("a: number"));
    assertTrue(pretty.contains("b: string"));

    anonymous.defineProperty("c", booleanType, false, null);
    anonymous.defineProperty("d", numberType, false, null);
    anonymous.defineProperty("e", stringType, false, null);
    String truncated = anonymous.toStringHelper(false);
    assertTrue(truncated.contains("..."));
  }

  @Test
  public void testOwnerFunctionAndReferenceName() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null);
    assertNull(type.getReferenceName());
    assertFalse(type.hasReferenceName());

    FunctionType fn = registry.createFunctionType(numberType);
    type.setOwnerFunction(fn);
    assertEquals(fn, type.getOwnerFunction());
    assertTrue(type.hasReferenceName());
    assertEquals(fn.getReferenceName() + ".prototype", type.getReferenceName());

    type.setOwnerFunction(null);
    assertNull(type.getOwnerFunction());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetOwnerFunctionThrowsWhenAlreadySet() {
    PrototypeObjectType type = new PrototypeObjectType(registry, null, null);
    FunctionType fn1 = registry.createFunctionType(numberType);
    FunctionType fn2 = registry.createFunctionType(stringType);
    type.setOwnerFunction(fn1);
    type.setOwnerFunction(fn2);
  }

  @Test
  public void testSetImplicitPrototype() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "A", null);
    PrototypeObjectType newProto = new PrototypeObjectType(registry, "B", null);
    type.setImplicitPrototype(newProto);
    assertEquals(newProto, type.getImplicitPrototype());
  }

  @Test
  public void testSubtyping() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    assertTrue(child.isSubtype(parent));
    assertTrue(child.isSubtype(child));
    assertFalse(parent.isSubtype(child));

    // Union type check
    UnionType union = new UnionType(registry, Sets.newHashSet((JSType) parent, stringType));
    assertFalse(parent.isSubtype(union));

    // Unknown type prototype chain
    PrototypeObjectType unknownProto = new PrototypeObjectType(registry, "UnknownProto", registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE));
    PrototypeObjectType withUnknownProto = new PrototypeObjectType(registry, "ChildOfUnknown", unknownProto);
    assertTrue(withUnknownProto.isSubtype(parent));
  }

  @Test
  public void testInterfacesHandling() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "NormalObj", null);
    assertFalse(type.getCtorImplementedInterfaces().iterator().hasNext());
    assertFalse(type.getCtorExtendedInterfaces().iterator().hasNext());

    FunctionType ctor = registry.createConstructorType("CustomCtor", null, null, null);
    ObjectType proto = ctor.getPrototype();
    assertNotNull(proto.getCtorImplementedInterfaces());
    assertNotNull(proto.getCtorExtendedInterfaces());
  }

  @Test
  public void testResolveInternal() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Resolvable", objectPrototype);
    type.defineProperty("p", numberType, false, null);
    ErrorReporter reporter = registry.getErrorReporter();
    JSType resolved = type.resolveInternal(reporter, null);
    assertEquals(type, resolved);
    assertTrue(type.isResolved());
  }

  @Test
  public void testHasOverriddenNativeProperty() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "CustomObj", objectPrototype);
    type.defineProperty("toString", stringType, false, null);
    assertTrue(type.matchesStringContext());
  }
}