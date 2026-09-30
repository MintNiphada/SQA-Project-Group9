package com.google.javascript.jscomp;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.AbstractCompiler.LifeCycleStage;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class DisambiguatePropertiesTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    registry = compiler.getTypeRegistry();
  }

  @Test
  public void testForJSTypeSystemCreation() {
    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    propertiesToErrorFor.put("foo", CheckLevel.ERROR);
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);
    Assert.assertNotNull(pass);
  }

  @Test
  public void testForConcreteTypeSystemCreation() {
    TightenTypes tt = new TightenTypes(compiler);
    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<ConcreteType> pass =
        DisambiguateProperties.forConcreteTypeSystem(compiler, tt, propertiesToErrorFor);
    Assert.assertNotNull(pass);
  }

  @Test
  public void testProcessEmptyTrees() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    Node externs = IR.block();
    Node root = IR.block();

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);

    Multimap<String, Collection<JSType>> renamed = pass.getRenamedTypesForTesting();
    Assert.assertTrue(renamed.isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void testProcessUnnormalizedThrowsException() {
    compiler.setLifeCycleStage(LifeCycleStage.RAW);
    Node externs = IR.block();
    Node root = IR.block();

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);
  }

  @Test
  public void testGetTypeWithProperty() {
    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    ObjectType stringType = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    JSType result = pass.getTypeWithProperty("length", stringType);
    Assert.assertNotNull(result);

    JSType nonExistent = pass.getTypeWithProperty("nonExistentPropXYZ", stringType);
    Assert.assertNull(nonExistent);

    JSType nullLookup = pass.getTypeWithProperty("foo", null);
    Assert.assertNull(nullLookup);

    JSType protoLookup = pass.getTypeWithProperty("prototype", stringType);
    Assert.assertNull(protoLookup);
  }

  @Test
  public void testDisambiguationTwoDistinctTypes() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    ObjectType typeA = registry.createRecordTypeFromFields(
        Collections.singletonMap("prop", registry.getNativeType(JSTypeNative.STRING_TYPE)));
    ObjectType typeB = registry.createRecordTypeFromFields(
        Collections.singletonMap("prop", registry.getNativeType(JSTypeNative.NUMBER_TYPE)));

    Node objA = IR.name("a");
    objA.setJSType(typeA);
    Node propA = IR.string("prop");
    Node getPropA = IR.getprop(objA, propA);
    getPropA.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));

    Node objB = IR.name("b");
    objB.setJSType(typeB);
    Node propB = IR.string("prop");
    Node getPropB = IR.getprop(objB, propB);
    getPropB.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    Node root = IR.block(IR.exprResult(getPropA), IR.exprResult(getPropB));
    Node externs = IR.block();

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);

    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testExternPropertiesNotRenamed() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    ObjectType stringType = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);

    Node externTarget = IR.name("str");
    externTarget.setJSType(stringType);
    Node externProp = IR.string("length");
    Node externGetProp = IR.getprop(externTarget, externProp);

    Node externs = IR.block(IR.exprResult(externGetProp));

    Node codeTarget = IR.name("myStr");
    codeTarget.setJSType(stringType);
    Node codeProp = IR.string("length");
    Node codeGetProp = IR.getprop(codeTarget, codeProp);

    Node root = IR.block(IR.exprResult(codeGetProp));

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);

    Assert.assertEquals("length", codeProp.getString());
  }

  @Test
  public void testObjectLiteralDisambiguation() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    Node keyNode = IR.stringKey("foo", IR.number(1));
    Node objLit = IR.objectlit(keyNode);
    ObjectType recordType = registry.createRecordTypeFromFields(
        Collections.singletonMap("foo", registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
    objLit.setJSType(recordType);

    Node root = IR.block(IR.exprResult(objLit));
    Node externs = IR.block();

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);

    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testInvalidationErrorReporting() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    Node obj = IR.name("unknownObj");
    obj.setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
    Node prop = IR.string("invalidatedProp");
    Node getProp = IR.getprop(obj, prop);

    Node root = IR.block(IR.exprResult(getProp));
    Node externs = IR.block();

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    propertiesToErrorFor.put("invalidatedProp", CheckLevel.WARNING);

    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInterfaceAndInheritanceHierarchy() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    FunctionType interfaceType = registry.createInterfaceType("MyInterface", null);
    FunctionType ctorType = registry.createConstructorType("MyClass", null, null, null);
    ctorType.getPrototype().defineProperty("myMethod",
        registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    ObjectType instanceType = ctorType.getInstanceType();

    Node obj = IR.name("inst");
    obj.setJSType(instanceType);
    Node prop = IR.string("myMethod");
    Node getProp = IR.getprop(obj, prop);

    Node root = IR.block(IR.exprResult(getProp));
    Node externs = IR.block();

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testUnionTypeHandling() {
    compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);

    ObjectType typeA = registry.createRecordTypeFromFields(
        Collections.singletonMap("commonProp", registry.getNativeType(JSTypeNative.STRING_TYPE)));
    ObjectType typeB = registry.createRecordTypeFromFields(
        Collections.singletonMap("commonProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
    JSType unionType = registry.createUnionType(typeA, typeB);

    Node obj = IR.name("unionObj");
    obj.setJSType(unionType);
    Node prop = IR.string("commonProp");
    Node getProp = IR.getprop(obj, prop);

    Node root = IR.block(IR.exprResult(getProp));
    Node externs = IR.block();

    Map<String, CheckLevel> propertiesToErrorFor = Maps.newHashMap();
    DisambiguateProperties<JSType> pass =
        DisambiguateProperties.forJSTypeSystem(compiler, propertiesToErrorFor);

    pass.process(externs, root);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testWarningsDefinitions() {
    Assert.assertNotNull(DisambiguateProperties.Warnings.INVALIDATION);
    Assert.assertNotNull(DisambiguateProperties.Warnings.INVALIDATION_ON_TYPE);
  }
}