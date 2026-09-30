package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.NamedType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnionType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class AmbiguatePropertiesTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    registry = compiler.getTypeRegistry();
  }

  private ObjectType createNamedObjectType(String name, ObjectType prototype) {
    ObjectType type = registry.createObjectType(name, registry.createAnonymousObjectType(), prototype);
    return type;
  }

  private Node makeGetProp(String propName, JSType targetType) {
    Node target = Node.newString(Token.NAME, "obj");
    if (targetType != null) {
      target.setJSType(targetType);
    }
    Node prop = Node.newString(Token.STRING, propName);
    Node getprop = new Node(Token.GETPROP, target, prop);
    return getprop;
  }

  @Test
  public void testBasicAmbiguationUnrelatedTypes() {
    ObjectType typeA = createNamedObjectType("TypeA", null);
    ObjectType typeB = createNamedObjectType("TypeB", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getpropA = makeGetProp("propA", typeA);
    Node getpropB = makeGetProp("propB", typeB);
    root.addChildToBack(getpropA);
    root.addChildToBack(getpropB);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertTrue(renamingMap.containsKey("propA"));
    Assert.assertTrue(renamingMap.containsKey("propB"));
    Assert.assertEquals(renamingMap.get("propA"), renamingMap.get("propB"));

    Node propNodeA = getpropA.getFirstChild().getNext();
    Node propNodeB = getpropB.getFirstChild().getNext();
    Assert.assertEquals(renamingMap.get("propA"), propNodeA.getString());
    Assert.assertEquals(renamingMap.get("propB"), propNodeB.getString());
  }

  @Test
  public void testSameTypePropertiesDoNotCollide() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop1 = makeGetProp("prop1", typeA);
    Node getprop2 = makeGetProp("prop2", typeA);
    root.addChildToBack(getprop1);
    root.addChildToBack(getprop2);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertTrue(renamingMap.containsKey("prop1"));
    Assert.assertTrue(renamingMap.containsKey("prop2"));
    Assert.assertFalse(renamingMap.get("prop1").equals(renamingMap.get("prop2")));
  }

  @Test
  public void testInheritanceHierarchyPreventsCollisions() {
    ObjectType parentType = createNamedObjectType("Parent", null);
    ObjectType childType = createNamedObjectType("Child", parentType);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getpropParent = makeGetProp("parentProp", parentType);
    Node getpropChild = makeGetProp("childProp", childType);
    root.addChildToBack(getpropParent);
    root.addChildToBack(getpropChild);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertFalse(renamingMap.get("parentProp").equals(renamingMap.get("childProp")));
  }

  @Test
  public void testImplementedInterfaces() {
    FunctionType ifaceType = registry.createInterfaceType("MyInterface", null);
    ObjectType instanceType = ifaceType.getInstanceType();

    FunctionType classType = registry.createConstructorType("MyClass", null, null, null);
    classType.setImplementedInterfaces(ImmutableList.of(instanceType));
    ObjectType classInstance = classType.getInstanceType();

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop1 = makeGetProp("ifaceProp", instanceType);
    Node getprop2 = makeGetProp("classProp", classInstance);
    root.addChildToBack(getprop1);
    root.addChildToBack(getprop2);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertFalse(renamingMap.get("ifaceProp").equals(renamingMap.get("classProp")));
  }

  @Test
  public void testFunctionTypeAndPrototypeTypeHandling() {
    FunctionType ctor = registry.createConstructorType("FooFunc", null, null, null);
    ObjectType proto = ctor.getPrototype();

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop1 = makeGetProp("funcProp", ctor);
    Node getprop2 = makeGetProp("protoProp", proto);
    root.addChildToBack(getprop1);
    root.addChildToBack(getprop2);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertTrue(renamingMap.containsKey("funcProp"));
    Assert.assertTrue(renamingMap.containsKey("protoProp"));
  }

  @Test
  public void testUnionTypeHandling() {
    ObjectType typeA = createNamedObjectType("TypeA", null);
    ObjectType typeB = createNamedObjectType("TypeB", null);
    UnionType unionType = (UnionType) registry.createUnionType(typeA, typeB);

    ObjectType typeC = createNamedObjectType("TypeC", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getpropUnion = makeGetProp("unionProp", unionType);
    Node getpropC = makeGetProp("cProp", typeC);
    root.addChildToBack(getpropUnion);
    root.addChildToBack(getpropC);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertTrue(renamingMap.containsKey("unionProp"));
    Assert.assertTrue(renamingMap.containsKey("cProp"));
  }

  @Test
  public void testUnionWithInvalidatingTypeSkipsAmbiguation() {
    ObjectType typeA = createNamedObjectType("TypeA", null);
    JSType invalidType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    UnionType union = (UnionType) registry.createUnionType(typeA, invalidType);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop = makeGetProp("invalidProp", union);
    root.addChildToBack(getprop);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertFalse(renamingMap.containsKey("invalidProp"));
  }

  @Test
  public void testSkipPrefixProperties() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop = makeGetProp(AmbiguateProperties.SKIP_PREFIX + "_custom", typeA);
    root.addChildToBack(getprop);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertFalse(renamingMap.containsKey(AmbiguateProperties.SKIP_PREFIX + "_custom"));
  }

  @Test
  public void testExternsGetProp() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node externGetProp = makeGetProp("externProp", typeA);
    externs.addChildToBack(externGetProp);

    Node root = new Node(Token.BLOCK);
    Node rootGetProp = makeGetProp("externProp", typeA);
    root.addChildToBack(rootGetProp);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertFalse(renamingMap.containsKey("externProp"));
  }

  @Test
  public void testExternsObjectLit() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "externObjLitProp");
    Node value = Node.newNumber(1);
    objLit.addChildToBack(key);
    objLit.addChildToBack(value);
    externs.addChildToBack(objLit);

    Node root = new Node(Token.BLOCK);
    Node rootGetProp = makeGetProp("externObjLitProp", typeA);
    root.addChildToBack(rootGetProp);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertFalse(renamingMap.containsKey("externObjLitProp"));
  }

  @Test
  public void testObjectLitUnquotedKey() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node target = Node.newString(Token.NAME, "myObj");
    target.setJSType(typeA);

    Node objLit = new Node(Token.OBJECTLIT, target);
    Node key = Node.newString(Token.STRING, "litProp");
    key.setQuotedString(false);
    Node val = Node.newNumber(42);
    objLit.addChildToBack(key);
    objLit.addChildToBack(val);
    root.addChildToBack(objLit);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertTrue(renamingMap.containsKey("litProp"));
    Assert.assertEquals(renamingMap.get("litProp"), key.getString());
  }

  @Test
  public void testObjectLitQuotedKeyReserved() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node target = Node.newString(Token.NAME, "myObj");
    target.setJSType(typeA);

    Node objLit = new Node(Token.OBJECTLIT, target);
    Node key = Node.newString(Token.STRING, "quotedProp");
    key.setQuotedString(true);
    Node val = Node.newNumber(42);
    objLit.addChildToBack(key);
    objLit.addChildToBack(val);

    Node getprop = makeGetProp("otherProp", typeA);
    root.addChildToBack(objLit);
    root.addChildToBack(getprop);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertTrue(renamingMap.containsKey("otherProp"));
    Assert.assertFalse("quotedProp".equals(renamingMap.get("otherProp")));
  }

  @Test
  public void testGetElemQuotedProperty() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node target = Node.newString(Token.NAME, "arr");
    Node elem = Node.newString(Token.STRING, "elemQuotedName");
    Node getElem = new Node(Token.GETELEM, target, elem);
    root.addChildToBack(getElem);

    Node getprop = makeGetProp("otherProp", typeA);
    root.addChildToBack(getprop);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> renamingMap = pass.getRenamingMap();
    Assert.assertTrue(renamingMap.containsKey("otherProp"));
    Assert.assertFalse("elemQuotedName".equals(renamingMap.get("otherProp")));
  }

  @Test
  public void testInvalidatingNativeTypes() {
    JSType[] invalidTypes = new JSType[]{
        registry.getNativeType(JSTypeNative.ALL_TYPE),
        registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE),
        registry.getNativeType(JSTypeNative.NO_TYPE),
        registry.getNativeType(JSTypeNative.NULL_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE),
        registry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE)
    };

    for (JSType invType : invalidTypes) {
      Node externs = new Node(Token.BLOCK);
      Node root = new Node(Token.BLOCK);

      Node getprop = makeGetProp("someProp", invType);
      root.addChildToBack(getprop);

      AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
      pass.process(externs, root);

      Assert.assertFalse(pass.getRenamingMap().containsKey("someProp"));
    }
  }

  @Test
  public void testTypeWithoutReferenceNameIsInvalidated() {
    ObjectType anonType = registry.createAnonymousObjectType();

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop = makeGetProp("anonProp", anonType);
    root.addChildToBack(getprop);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Assert.assertFalse(pass.getRenamingMap().containsKey("anonProp"));
  }

  @Test
  public void testEnumTypeIsInvalidated() {
    EnumType enumType = registry.createEnumType("MyEnum", registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop = makeGetProp("enumProp", enumType);
    root.addChildToBack(getprop);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Assert.assertFalse(pass.getRenamingMap().containsKey("enumProp"));
  }

  @Test
  public void testNullJSTypeFallsBackToUnknown() {
    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getprop = makeGetProp("nullTypeProp", null);
    root.addChildToBack(getprop);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Assert.assertFalse(pass.getRenamingMap().containsKey("nullTypeProp"));
  }

  @Test
  public void testTypeMismatchInvalidation() {
    ObjectType typeFoo = createNamedObjectType("FooType", null);
    ObjectType typeBar = createNamedObjectType("BarType", null);

    compiler.getTypeValidator().registerMismatch(typeFoo, typeBar, null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    Node getpropFoo = makeGetProp("mismatchPropFoo", typeFoo);
    Node getpropBar = makeGetProp("mismatchPropBar", typeBar);
    root.addChildToBack(getpropFoo);
    root.addChildToBack(getpropBar);

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Assert.assertFalse(pass.getRenamingMap().containsKey("mismatchPropFoo"));
    Assert.assertFalse(pass.getRenamingMap().containsKey("mismatchPropBar"));
  }

  @Test
  public void testFrequencyOrdering() {
    ObjectType typeA = createNamedObjectType("TypeA", null);
    ObjectType typeB = createNamedObjectType("TypeB", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    // propX occurs 3 times, propY occurs 1 time, propZ occurs 1 time
    root.addChildToBack(makeGetProp("propX", typeA));
    root.addChildToBack(makeGetProp("propX", typeA));
    root.addChildToBack(makeGetProp("propX", typeA));

    root.addChildToBack(makeGetProp("propY", typeB));
    root.addChildToBack(makeGetProp("propZ", typeB));

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> map = pass.getRenamingMap();
    Assert.assertEquals("a", map.get("propX"));
  }

  @Test
  public void testReservedCharacters() {
    ObjectType typeA = createNamedObjectType("TypeA", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    root.addChildToBack(makeGetProp("prop1", typeA));

    char[] reserved = new char[]{'a', 'b'};
    AmbiguateProperties pass = new AmbiguateProperties(compiler, reserved);
    pass.process(externs, root);

    Map<String, String> map = pass.getRenamingMap();
    String newName = map.get("prop1");
    Assert.assertNotNull(newName);
    Assert.assertFalse("a".equals(newName));
    Assert.assertFalse("b".equals(newName));
  }

  @Test
  public void testMultiplePropertiesAmbiguationGraph() {
    ObjectType typeA = createNamedObjectType("TypeA", null);
    ObjectType typeB = createNamedObjectType("TypeB", null);
    ObjectType typeC = createNamedObjectType("TypeC", null);

    Node externs = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK);

    // typeA: p1, p2
    // typeB: p3, p4
    // typeC: p5
    root.addChildToBack(makeGetProp("p1", typeA));
    root.addChildToBack(makeGetProp("p2", typeA));
    root.addChildToBack(makeGetProp("p3", typeB));
    root.addChildToBack(makeGetProp("p4", typeB));
    root.addChildToBack(makeGetProp("p5", typeC));

    AmbiguateProperties pass = new AmbiguateProperties(compiler, new char[0]);
    pass.process(externs, root);

    Map<String, String> map = pass.getRenamingMap();
    Assert.assertEquals(5, map.size());
    Assert.assertFalse(map.get("p1").equals(map.get("p2")));
    Assert.assertFalse(map.get("p3").equals(map.get("p4")));
  }
}