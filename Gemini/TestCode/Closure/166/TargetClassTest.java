package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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
  public void testConstructorsAndDefaults() {
    PrototypeObjectType type1 = new PrototypeObjectType(registry, "CustomClass", null);
    assertEquals("CustomClass", type1.getClassName());
    assertSame(objectPrototype, type1.getImplicitPrototype());
    assertFalse(type1.isNativeObjectType());
    assertTrue(type1.hasReferenceName());
    assertEquals("CustomClass", type1.getReferenceName());

    PrototypeObjectType type2 = new PrototypeObjectType(registry, null, null, true);
    assertNull(type2.getClassName());
    assertNull(type2.getImplicitPrototype());
    assertTrue(type2.isNativeObjectType());
    assertFalse(type2.hasReferenceName());
    assertNull(type2.getReferenceName());
  }

  @Test
  public void testPropertyDefinitionAndLookup() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, "TestObj", null);
    Node node = Node.newString("prop");

    assertFalse(obj.hasOwnProperty("prop"));
    assertFalse(obj.hasProperty("prop"));
    assertNull(obj.getSlot("prop"));
    assertNull(obj.getPropertyNode("prop"));
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), obj.getPropertyType("prop"));

    assertTrue(obj.defineProperty("prop", numberType, false, node));
    assertTrue(obj.hasOwnProperty("prop"));
    assertTrue(obj.hasProperty("prop"));
    assertNotNull(obj.getSlot("prop"));
    assertSame(node, obj.getPropertyNode("prop"));
    assertSame(numberType, obj.getPropertyType("prop"));
    assertTrue(obj.isPropertyTypeDeclared("prop"));
    assertFalse(obj.isPropertyTypeInferred("prop"));

    assertFalse(obj.defineProperty("prop", stringType, true, null));
    assertSame(numberType, obj.getPropertyType("prop"));

    assertTrue(obj.removeProperty("prop"));
    assertFalse(obj.hasOwnProperty("prop"));
    assertFalse(obj.removeProperty("prop"));
  }

  @Test
  public void testPrototypeInheritedProperties() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    parent.defineProperty("parentProp", numberType, true, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("childProp", stringType, true, null);

    assertTrue(child.hasOwnProperty("childProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertTrue(child.hasProperty("parentProp"));

    assertSame(numberType, child.getPropertyType("parentProp"));
    assertTrue(child.isPropertyTypeInferred("parentProp"));

    Set<String> propNames = new HashSet<String>();
    child.collectPropertyNames(propNames);
    assertTrue(propNames.contains("parentProp"));
    assertTrue(propNames.contains("childProp"));

    assertEquals(1, child.getOwnPropertyNames().size());
  }

  @Test
  public void testPropertiesCount() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, true);
    parent.defineProperty("a", numberType, true, null);
    parent.defineProperty("b", numberType, true, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("b", stringType, true, null);
    child.defineProperty("c", booleanType, true, null);

    assertEquals(3, child.getPropertiesCount());

    PrototypeObjectType orphan = new PrototypeObjectType(registry, "Orphan", null, true);
    orphan.defineProperty("x", numberType, true, null);
    assertEquals(1, orphan.getPropertiesCount());
  }

  @Test
  public void testJSDocInfoHandling() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null);
    JSDocInfo info = new JSDocInfo();

    assertNull(obj.getOwnPropertyJSDocInfo("p"));
    obj.setPropertyJSDocInfo("p", info);
    assertTrue(obj.hasOwnProperty("p"));
    assertSame(info, obj.getOwnPropertyJSDocInfo("p"));

    obj.defineProperty("p", numberType, true, null);
    assertSame(info, obj.getOwnPropertyJSDocInfo("p"));

    obj.setPropertyJSDocInfo("p", null);
    assertSame(info, obj.getOwnPropertyJSDocInfo("p"));
  }

  @Test
  public void testIsPropertyInExterns() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    Node externNode = Node.newString("a");
    externNode.setStaticSourceFile(new com.google.javascript.rhino.jstype.SimpleSourceFile("externs.js", true));
    parent.defineProperty("extProp", numberType, false, externNode);

    assertTrue(parent.isPropertyInExterns("extProp"));
    assertTrue(child.isPropertyInExterns("extProp"));
    assertFalse(child.isPropertyInExterns("nonExistent"));
  }

  @Test
  public void testContextMatches() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null);
    assertTrue(obj.matchesObjectContext());
    assertFalse(obj.canBeCalled());

    ObjectType stringObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    assertTrue(stringObj.matchesStringContext());
    assertTrue(stringObj.matchesNumberContext());
    assertSame(stringType, stringObj.unboxesTo());

    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    assertTrue(boolObj.matchesStringContext());
    assertTrue(boolObj.matchesNumberContext());
    assertSame(booleanType, boolObj.unboxesTo());

    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertTrue(numObj.matchesStringContext());
    assertTrue(numObj.matchesNumberContext());
    assertSame(numberType, numObj.unboxesTo());

    assertNull(obj.unboxesTo());
  }

  @Test
  public void testToStringAndPrettyPrint() {
    PrototypeObjectType named = new PrototypeObjectType(registry, "NamedType", null);
    assertEquals("NamedType", named.toStringHelper(false));
    assertEquals("NamedType", named.toStringHelper(true));

    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, true);
    assertEquals("{...}", anon.toStringHelper(false));
    assertEquals("?", anon.toStringHelper(true));

    anon.setPrettyPrint(true);
    assertTrue(anon.isPrettyPrint());
    anon.defineProperty("a", numberType, false, null);
    anon.defineProperty("b", stringType, false, null);
    assertEquals("{a: number, b: string}", anon.toStringHelper(false));

    anon.defineProperty("c", booleanType, false, null);
    anon.defineProperty("d", numberType, false, null);
    anon.defineProperty("e", stringType, false, null);
    assertEquals("{a: number, b: string, c: boolean, d: number, ...}", anon.toStringHelper(false));
    assertEquals("{a: number, b: string, c: boolean, d: number, e: string}", anon.toStringHelper(true));
  }

  @Test
  public void testOwnerFunctionAndInterfaces() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null);
    assertNull(proto.getOwnerFunction());
    assertNull(proto.getConstructor());
    assertFalse(proto.getCtorImplementedInterfaces().iterator().hasNext());
    assertFalse(proto.getCtorExtendedInterfaces().iterator().hasNext());

    FunctionType fn = FunctionType.forInterface(registry, "IFace", null);
    proto.setOwnerFunction(fn);
    assertSame(fn, proto.getOwnerFunction());
    assertEquals("IFace.prototype", proto.getReferenceName());
    assertTrue(proto.hasReferenceName());

    proto.setOwnerFunction(null);
    assertNull(proto.getOwnerFunction());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetOwnerFunctionConflict() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null);
    FunctionType fn1 = FunctionType.forInterface(registry, "IFace1", null);
    FunctionType fn2 = FunctionType.forInterface(registry, "IFace2", null);
    proto.setOwnerFunction(fn1);
    proto.setOwnerFunction(fn2);
  }

  @Test
  public void testSubtyping() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    assertTrue(child.isSubtype(parent));
    assertTrue(child.isSubtype(objectPrototype));
    assertFalse(parent.isSubtype(child));

    UnionType union = new UnionType(registry, ImmutableList.<JSType>of(numberType, stringType));
    assertFalse(child.isSubtype(union));

    RecordTypeBuilder rtb = new RecordTypeBuilder(registry);
    rtb.addProperty("x", numberType, null);
    RecordType record = rtb.build();

    assertFalse(child.isSubtype(record));
    child.defineProperty("x", numberType, false, null);
    assertTrue(child.isSubtype(record));
  }

  @Test
  public void testMatchConstraint() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    RecordTypeBuilder rtb = new RecordTypeBuilder(registry);
    rtb.addProperty("foo", numberType, null);
    RecordType record = rtb.build();

    anon.matchConstraint(record);
    assertTrue(anon.hasOwnProperty("foo"));
    assertTrue(anon.isPropertyTypeInferred("foo"));

    PrototypeObjectType named = new PrototypeObjectType(registry, "Named", null);
    named.matchConstraint(record);
    assertFalse(named.hasOwnProperty("foo"));
  }

  @Test
  public void testResolveInternal() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, "ResolvedObj", objectPrototype);
    obj.defineProperty("num", numberType, false, null);

    JSType resolved = obj.resolve(null, null);
    assertSame(obj, resolved);
    assertTrue(obj.isResolved());
    assertSame(numberType, obj.getPropertyType("num"));
  }
}
