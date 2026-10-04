package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ObjectTypeTest {

  private JSTypeRegistry registry;

  private static class TestObjectType extends ObjectType {
    private static final long serialVersionUID = 1L;
    private final String referenceName;
    private ObjectType implicitPrototype;
    private final Map<String, Property> properties = new HashMap<String, Property>();
    private final Map<String, Boolean> inferred = new HashMap<String, Boolean>();
    private final Map<String, Boolean> declared = new HashMap<String, Boolean>();
    private final Map<String, Node> propertyNodes = new HashMap<String, Node>();
    private final Map<String, JSDocInfo> propertyDocInfos = new HashMap<String, JSDocInfo>();
    private FunctionType constructor;
    private FunctionType ownerFunction;
    private Iterable<ObjectType> extendedInterfaces = ImmutableSet.of();
    private Iterable<ObjectType> implementedInterfaces = ImmutableSet.of();
    private boolean nativeType = false;

    TestObjectType(JSTypeRegistry registry, String referenceName, ObjectType implicitPrototype) {
      super(registry);
      this.referenceName = referenceName;
      this.implicitPrototype = implicitPrototype;
    }

    void setImplicitPrototype(ObjectType proto) {
      this.implicitPrototype = proto;
    }

    void setNativeObjectType(boolean isNative) {
      this.nativeType = isNative;
    }

    void setExtendedInterfaces(Iterable<ObjectType> interfaces) {
      this.extendedInterfaces = interfaces;
    }

    void setImplementedInterfaces(Iterable<ObjectType> interfaces) {
      this.implementedInterfaces = interfaces;
    }

    @Override
    public Property getSlot(String name) {
      if (properties.containsKey(name)) {
        return properties.get(name);
      }
      if (implicitPrototype != null) {
        return implicitPrototype.getSlot(name);
      }
      return null;
    }

    @Override
    public String getReferenceName() {
      return referenceName;
    }

    @Override
    public FunctionType getConstructor() {
      return constructor;
    }

    void setConstructor(FunctionType constructor) {
      this.constructor = constructor;
    }

    @Override
    public ObjectType getImplicitPrototype() {
      return implicitPrototype;
    }

    @Override
    boolean defineProperty(String propertyName, JSType type, boolean inferred, Node propertyNode) {
      Property p = new Property(propertyName, type, inferred, propertyNode);
      properties.put(propertyName, p);
      this.inferred.put(propertyName, inferred);
      this.declared.put(propertyName, !inferred);
      this.propertyNodes.put(propertyName, propertyNode);
      return true;
    }

    @Override
    public JSType getPropertyType(String propertyName) {
      Property p = getSlot(propertyName);
      return p != null ? p.getType() : registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    }

    @Override
    public boolean hasProperty(String propertyName) {
      if (properties.containsKey(propertyName)) {
        return true;
      }
      return implicitPrototype != null && implicitPrototype.hasProperty(propertyName);
    }

    @Override
    public boolean hasOwnProperty(String propertyName) {
      return properties.containsKey(propertyName);
    }

    @Override
    public Set<String> getOwnPropertyNames() {
      return properties.keySet();
    }

    @Override
    public boolean isPropertyTypeInferred(String propertyName) {
      return Boolean.TRUE.equals(inferred.get(propertyName));
    }

    @Override
    public boolean isPropertyTypeDeclared(String propertyName) {
      return Boolean.TRUE.equals(declared.get(propertyName));
    }

    @Override
    public int getPropertiesCount() {
      return properties.size();
    }

    @Override
    void collectPropertyNames(Set<String> props) {
      props.addAll(properties.keySet());
      if (implicitPrototype != null) {
        implicitPrototype.collectPropertyNames(props);
      }
    }

    @Override
    public Node getPropertyNode(String propertyName) {
      return propertyNodes.get(propertyName);
    }

    @Override
    public JSDocInfo getOwnPropertyJSDocInfo(String propertyName) {
      return propertyDocInfos.get(propertyName);
    }

    @Override
    public void setPropertyJSDocInfo(String propertyName, JSDocInfo info) {
      propertyDocInfos.put(propertyName, info);
    }

    @Override
    public boolean isNativeObjectType() {
      return nativeType;
    }

    @Override
    public FunctionType getOwnerFunction() {
      return ownerFunction;
    }

    @Override
    void setOwnerFunction(FunctionType type) {
      this.ownerFunction = type;
    }

    @Override
    public Iterable<ObjectType> getCtorExtendedInterfaces() {
      return extendedInterfaces;
    }

    @Override
    public Iterable<ObjectType> getCtorImplementedInterfaces() {
      return implementedInterfaces;
    }
  }

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
  }

  @Test
  public void testBasicPropertiesAndDefaults() {
    TestObjectType obj = new TestObjectType(registry, "Foo", null);
    Assert.assertNull(obj.getRootNode());
    Assert.assertNull(obj.getParentScope());
    Assert.assertNull(obj.getTypeOfThis());
    Assert.assertNull(obj.getParameterType());
    Assert.assertNull(obj.getIndexType());
    Assert.assertNull(obj.getConstructor());
    Assert.assertFalse(obj.hasReferenceName());
    Assert.assertFalse(obj.removeProperty("prop"));
    Assert.assertNull(obj.getPropertyNode("prop"));
    Assert.assertNull(obj.getOwnPropertyJSDocInfo("prop"));
    Assert.assertFalse(obj.isPropertyInExterns("prop"));
    Assert.assertTrue(obj.isObject());
    Assert.assertEquals(BooleanLiteralSet.TRUE, obj.getPossibleToBooleanOutcomes());
    Assert.assertNotNull(obj.getCtorImplementedInterfaces());
    Assert.assertFalse(obj.getCtorImplementedInterfaces().iterator().hasNext());
    Assert.assertNotNull(obj.getCtorExtendedInterfaces());
    Assert.assertFalse(obj.getCtorExtendedInterfaces().iterator().hasNext());
    Assert.assertEquals(0, obj.getOwnPropertyNames().size());
  }

  @Test
  public void testParentScope() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    TestObjectType child = new TestObjectType(registry, "Child", parent);
    Assert.assertEquals(parent, child.getParentScope());
  }

  @Test
  public void testGetOwnSlot() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    parent.defineDeclaredProperty("parentProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    TestObjectType child = new TestObjectType(registry, "Child", parent);
    child.defineDeclaredProperty("childProp", registry.getNativeType(JSTypeNative.STRING_TYPE), null);

    Assert.assertNotNull(child.getOwnSlot("childProp"));
    Assert.assertEquals("childProp", child.getOwnSlot("childProp").getName());
    Assert.assertNull(child.getOwnSlot("parentProp"));
    Assert.assertNotNull(child.getSlot("parentProp"));
    Assert.assertNull(child.getOwnSlot("nonExistent"));
  }

  @Test
  public void testJSDocInfoInheritance() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    TestObjectType child = new TestObjectType(registry, "Child", parent);

    Assert.assertNull(child.getJSDocInfo());

    JSDocInfo parentDoc = new JSDocInfo();
    parent.setJSDocInfo(parentDoc);
    Assert.assertEquals(parentDoc, child.getJSDocInfo());

    JSDocInfo childDoc = new JSDocInfo();
    child.setJSDocInfo(childDoc);
    Assert.assertEquals(childDoc, child.getJSDocInfo());
  }

  @Test
  public void testDetectImplicitPrototypeCycle() {
    TestObjectType a = new TestObjectType(registry, "A", null);
    TestObjectType b = new TestObjectType(registry, "B", a);
    TestObjectType c = new TestObjectType(registry, "C", b);

    Assert.assertFalse(c.detectImplicitPrototypeCycle());
    Assert.assertFalse(b.detectImplicitPrototypeCycle());
    Assert.assertFalse(a.detectImplicitPrototypeCycle());

    a.setImplicitPrototype(c);
    Assert.assertTrue(c.detectImplicitPrototypeCycle());
  }

  @Test
  public void testNormalizedReferenceName() {
    TestObjectType obj1 = new TestObjectType(registry, "MyClass(suffix)", null);
    Assert.assertEquals("MyClass", obj1.getNormalizedReferenceName());
    Assert.assertEquals("MyClass", obj1.getDisplayName());

    TestObjectType obj2 = new TestObjectType(registry, "MyClass", null);
    Assert.assertEquals("MyClass", obj2.getNormalizedReferenceName());
    Assert.assertEquals("MyClass", obj2.getDisplayName());

    TestObjectType obj3 = new TestObjectType(registry, null, null);
    Assert.assertNull(obj3.getNormalizedReferenceName());
    Assert.assertNull(obj3.getDisplayName());

    Assert.assertEquals("(testSuffix)", ObjectType.createDelegateSuffix("testSuffix"));
  }

  @Test
  public void testTestForEquality() {
    TestObjectType obj1 = new TestObjectType(registry, "A", null);
    TestObjectType obj2 = new TestObjectType(registry, "B", null);

    Assert.assertEquals(TernaryValue.TRUE, obj1.testForEquality(obj1));
    Assert.assertEquals(TernaryValue.UNKNOWN, obj1.testForEquality(obj2));
    Assert.assertEquals(TernaryValue.UNKNOWN, obj1.testForEquality(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
    Assert.assertEquals(TernaryValue.FALSE, obj1.testForEquality(registry.getNativeType(JSTypeNative.NULL_TYPE)));
    Assert.assertEquals(TernaryValue.FALSE, obj1.testForEquality(registry.getNativeType(JSTypeNative.VOID_TYPE)));
  }

  @Test
  public void testPropertyDefinitions() {
    TestObjectType obj = new TestObjectType(registry, "A", null);
    Node node1 = Node.newString("prop1");
    Node node2 = Node.newString("prop2");

    boolean d1 = obj.defineDeclaredProperty("prop1", registry.getNativeType(JSTypeNative.NUMBER_TYPE), node1);
    Assert.assertTrue(d1);
    Assert.assertTrue(obj.hasOwnDeclaredProperty("prop1"));
    Assert.assertFalse(obj.isPropertyTypeInferred("prop1"));
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), obj.findPropertyType("prop1"));

    boolean i1 = obj.defineInferredProperty("prop2", registry.getNativeType(JSTypeNative.STRING_TYPE), node2);
    Assert.assertTrue(i1);
    Assert.assertTrue(obj.isPropertyTypeInferred("prop2"));
    Assert.assertFalse(obj.hasOwnDeclaredProperty("prop2"));
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), obj.findPropertyType("prop2"));

    obj.defineInferredProperty("prop2", registry.getNativeType(JSTypeNative.NUMBER_TYPE), node2);
    JSType combinedType = obj.findPropertyType("prop2");
    Assert.assertTrue(combinedType.isUnionType());

    Assert.assertNull(obj.findPropertyType("nonExistent"));
  }

  @Test
  public void testGetPropertyNames() {
    TestObjectType parent = new TestObjectType(registry, "Parent", null);
    parent.defineDeclaredProperty("b", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    parent.defineDeclaredProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    TestObjectType child = new TestObjectType(registry, "Child", parent);
    child.defineDeclaredProperty("c", registry.getNativeType(JSTypeNative.STRING_TYPE), null);

    Set<String> names = child.getPropertyNames();
    Assert.assertEquals(3, names.size());
    Assert.assertTrue(names.contains("a"));
    Assert.assertTrue(names.contains("b"));
    Assert.assertTrue(names.contains("c"));
  }

  @Test
  public void testVisitor() {
    TestObjectType obj = new TestObjectType(registry, "A", null);
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType() { return null; }
      @Override public String caseEnumElementType(EnumElementType enumElementType) { return null; }
      @Override public String caseAllType() { return null; }
      @Override public String caseBooleanType() { return null; }
      @Override public String caseNoObjectType() { return null; }
      @Override public String caseFunctionType(FunctionType type) { return null; }
      @Override public String caseObjectType(ObjectType type) { return "visitedObject"; }
      @Override public String caseUnknownType() { return null; }
      @Override public String caseNullType() { return null; }
      @Override public String caseNamedType(NamedType type) { return null; }
      @Override public String caseProxyObjectType(ProxyObjectType type) { return null; }
      @Override public String caseNumberType() { return null; }
      @Override public String caseStringType() { return null; }
      @Override public String caseVoidType() { return null; }
      @Override public String caseUnionType(UnionType type) { return null; }
      @Override public String caseRecordType(RecordType type) { return null; }
      @Override public String caseTemplateType(TemplateType templateType) { return null; }
    };
    Assert.assertEquals("visitedObject", obj.visit(visitor));
  }

  @Test
  public void testIsImplicitPrototype() {
    TestObjectType proto1 = new TestObjectType(registry, "Proto1", null);
    TestObjectType proto2 = new TestObjectType(registry, "Proto2", proto1);
    TestObjectType obj = new TestObjectType(registry, "Obj", proto2);
    TestObjectType unrelated = new TestObjectType(registry, "Unrelated", null);

    Assert.assertTrue(obj.isImplicitPrototype(obj));
    Assert.assertTrue(obj.isImplicitPrototype(proto2));
    Assert.assertTrue(obj.isImplicitPrototype(proto1));
    Assert.assertFalse(obj.isImplicitPrototype(unrelated));
  }

  @Test
  public void testIsUnknownTypeAndCache() {
    TestObjectType obj = new TestObjectType(registry, "A", null);
    Assert.assertFalse(obj.hasCachedValues());
    Assert.assertFalse(obj.isUnknownType());
    Assert.assertTrue(obj.hasCachedValues());

    obj.clearCachedValues();
    Assert.assertFalse(obj.hasCachedValues());

    TestObjectType nativeParent = new TestObjectType(registry, "NativeParent", null);
    nativeParent.setNativeObjectType(true);
    TestObjectType childOfNative = new TestObjectType(registry, "ChildNative", nativeParent);
    Assert.assertFalse(childOfNative.isUnknownType());

    TestObjectType unknownInterface = new TestObjectType(registry, "UnknownInterface", null) {
      private static final long serialVersionUID = 1L;
      @Override
      public boolean isUnknownType() {
        return true;
      }
    };
    TestObjectType objWithExtended = new TestObjectType(registry, "Ext", null);
    objWithExtended.setExtendedInterfaces(Collections.<ObjectType>singleton(unknownInterface));
    Assert.assertTrue(objWithExtended.isUnknownType());

    TestObjectType unknownParent = new TestObjectType(registry, "UnknownParent", null) {
      private static final long serialVersionUID = 1L;
      @Override
      public boolean isUnknownType() {
        return true;
      }
    };
    TestObjectType childOfUnknown = new TestObjectType(registry, "ChildUnknown", unknownParent);
    Assert.assertTrue(childOfUnknown.isUnknownType());
  }

  @Test
  public void testCast() {
    Assert.assertNull(ObjectType.cast(null));
    TestObjectType obj = new TestObjectType(registry, "A", null);
    Assert.assertSame(obj, ObjectType.cast(obj));
    Assert.assertNull(ObjectType.cast(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
  }

  @Test
  public void testFunctionPrototypeOwner() {
    TestObjectType obj = new TestObjectType(registry, "Proto", null);
    Assert.assertFalse(obj.isFunctionPrototypeType());
    Assert.assertNull(obj.getOwnerFunction());

    FunctionType fn = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    obj.setOwnerFunction(fn);
    Assert.assertEquals(fn, obj.getOwnerFunction());
    Assert.assertTrue(obj.isFunctionPrototypeType());
  }

  @Test
  public void testSetPropertyJSDocInfo() {
    TestObjectType obj = new TestObjectType(registry, "A", null);
    JSDocInfo info = new JSDocInfo();
    obj.setPropertyJSDocInfo("prop", info);
    Assert.assertEquals(info, obj.getOwnPropertyJSDocInfo("prop"));
  }

  @Test
  public void testPropertyInnerClass() {
    Node node = Node.newString("foo");
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    ObjectType.Property prop = new ObjectType.Property("testProp", numType, true, node);
    Assert.assertEquals("testProp", prop.getName());
    Assert.assertEquals(node, prop.getNode());
    Assert.assertEquals(numType, prop.getType());
    Assert.assertTrue(prop.isTypeInferred());
    Assert.assertSame(prop, prop.getSymbol());
    Assert.assertSame(prop, prop.getDeclaration());
    Assert.assertFalse(prop.isFromExterns());
    Assert.assertNull(prop.getSourceFile());

    prop.setType(strType);
    Assert.assertEquals(strType, prop.getType());

    JSDocInfo doc = new JSDocInfo();
    prop.setJSDocInfo(doc);
    Assert.assertEquals(doc, prop.getJSDocInfo());

    Node newNode = Node.newNumber(42);
    prop.setNode(newNode);
    Assert.assertEquals(newNode, prop.getNode());

    ObjectType.Property nullNodeProp = new ObjectType.Property("nullProp", numType, false, null);
    Assert.assertNull(nullNodeProp.getNode());
    Assert.assertNull(nullNodeProp.getDeclaration());
    Assert.assertNull(nullNodeProp.getSourceFile());
    Assert.assertFalse(nullNodeProp.isFromExterns());
  }
}
