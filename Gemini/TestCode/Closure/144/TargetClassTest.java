package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class FunctionTypeBuilderTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Scope scope;
  private Node rootNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    rootNode = new Node(Token.BLOCK);
    scope = Scope.createGlobalScope(rootNode);
  }

  @Test
  public void testIsFunctionTypeDeclaration() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordConstructor();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordInterface();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordReturnType(new JSTypeExpression(new Node(Token.STAR), "test"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordParameter("x", new JSTypeExpression(new Node(Token.STAR), "test"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordThisType(new JSTypeExpression(new Node(Token.STAR), "test"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));
  }

  @Test
  public void testBasicFunctionBuild() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    builder.inferReturnType(null);
    FunctionType fn = builder.buildAndRegister();
    assertNotNull(fn);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildWithoutParametersThrowsException() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.buildAndRegister();
  }

  @Test
  public void testInferFromOverriddenFunctionNullParamsParent() {
    FunctionType oldType = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("overrideFn", compiler, rootNode, "test.js", scope);
    builder.inferFromOverriddenFunction(oldType, null);
    FunctionType fn = builder.buildAndRegister();
    assertNotNull(fn);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fn.getReturnType());
  }

  @Test
  public void testInferFromOverriddenFunctionWithParams() {
    FunctionType oldType = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.createParameters(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
    Node paramsParent = new Node(Token.LP, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("overrideFn", compiler, rootNode, "test.js", scope);
    builder.inferFromOverriddenFunction(oldType, paramsParent);
    FunctionType fn = builder.buildAndRegister();
    assertNotNull(fn);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fn.getReturnType());
    assertEquals(2, fn.getParameters().size());
  }

  @Test
  public void testInferInheritanceExtendsWithoutTypedef() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordBaseType(new JSTypeExpression(new Node(Token.NAME, "Object"), "test"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(doc.build(null));
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferInheritanceImplementsWithoutConstructor() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordImplementedInterface(new JSTypeExpression(new Node(Token.NAME, "Object"), "test"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(doc.build(null));
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferInheritanceConstructorWithValidBaseAndInterface() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordConstructor();
    doc.recordBaseType(new JSTypeExpression(new Node(Token.NAME, "Object"), "test"));
    doc.recordImplementedInterface(new JSTypeExpression(new Node(Token.NAME, "Object"), "test"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder("MyClass", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(doc.build(null));
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.isConstructor());
    assertNotNull(registry.getType("MyClass"));
  }

  @Test
  public void testInferInheritanceInterface() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordInterface();
    FunctionTypeBuilder builder = new FunctionTypeBuilder("MyInterface", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(doc.build(null));
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.isInterface());
    assertNotNull(registry.getType("MyInterface"));
  }

  @Test
  public void testInferThisType() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    builder.inferThisType(null, objType);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertEquals(objType, fn.getTypeOfThis());
  }

  @Test
  public void testInferThisTypeFromOwner() {
    registry.declareType("MyOwner", registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
    Node ownerNode = Node.newString(Token.NAME, "MyOwner");
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferThisType(null, ownerNode);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), fn.getTypeOfThis());
  }

  @Test
  public void testInferParameterTypesWithJSDocOnly() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordParameter("p1", new JSTypeExpression(new Node(Token.NAME, "number"), "test"));
    doc.recordParameter("p2", new JSTypeExpression(new Node(Token.NAME, "string"), "test"));
    JSDocInfo info = doc.build(null);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(info);
    FunctionType fn = builder.buildAndRegister();
    assertEquals(2, fn.getParameters().size());
  }

  @Test
  public void testInexistentParameterWarning() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordParameter("nonExistent", new JSTypeExpression(new Node(Token.NAME, "number"), "test"));
    JSDocInfo info = doc.build(null);

    Node params = new Node(Token.LP, Node.newString(Token.NAME, "actualParam"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(params, info);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInvalidArgOrderOptionalBeforeRequired() {
    Node params = new Node(Token.LP, Node.newString(Token.NAME, "opt_a"), Node.newString(Token.NAME, "b"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(params, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInvalidArgOrderVarArgsNotLast() {
    Node params = new Node(Token.LP, Node.newString(Token.NAME, "var_args"), Node.newString(Token.NAME, "b"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(params, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testTemplateTypeInference() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordTemplateTypeName("T");
    doc.recordParameter("a", new JSTypeExpression(new Node(Token.NAME, "T"), "test"));
    doc.recordReturnType(new JSTypeExpression(new Node(Token.NAME, "T"), "test"));
    JSDocInfo info = doc.build(null);

    Node params = new Node(Token.LP, Node.newString(Token.NAME, "a"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferTemplateTypeName(info);
    builder.inferReturnType(info);
    builder.inferParameterTypes(params, info);
    FunctionType fn = builder.buildAndRegister();
    assertNotNull(fn);
  }

  @Test
  public void testTemplateTypeExpectedError() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordTemplateTypeName("T");
    doc.recordParameter("a", new JSTypeExpression(new Node(Token.NAME, "number"), "test"));
    doc.recordReturnType(new JSTypeExpression(new Node(Token.NAME, "T"), "test"));
    JSDocInfo info = doc.build(null);

    Node params = new Node(Token.LP, Node.newString(Token.NAME, "a"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferTemplateTypeName(info);
    builder.inferReturnType(info);
    builder.inferParameterTypes(params, info);
    assertEquals(2, compiler.getErrorCount());
  }

  @Test
  public void testTemplateTypeDuplicatedError() {
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordTemplateTypeName("T");
    doc.recordParameter("a", new JSTypeExpression(new Node(Token.NAME, "T"), "test"));
    doc.recordParameter("b", new JSTypeExpression(new Node(Token.NAME, "T"), "test"));
    JSDocInfo info = doc.build(null);

    Node params = new Node(Token.LP, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferTemplateTypeName(info);
    builder.inferParameterTypes(params, info);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testGetOrCreateConstructorExistingFunctionType() {
    FunctionTypeBuilder builder1 = new FunctionTypeBuilder("Function", compiler, rootNode, "test.js", scope);
    JSDocInfoBuilder doc = new JSDocInfoBuilder(true);
    doc.recordConstructor();
    builder1.inferInheritance(doc.build(null));
    Node params = new Node(Token.LP, Node.newString(Token.NAME, "arg"));
    builder1.inferParameterTypes(params, null);
    FunctionType fn1 = builder1.buildAndRegister();
    assertNotNull(fn1);
  }

  @Test
  public void testSetSourceNode() {
    Node srcNode = new Node(Token.FUNCTION);
    FunctionTypeBuilder builder = new FunctionTypeBuilder("foo", compiler, rootNode, "test.js", scope);
    builder.setSourceNode(srcNode);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertEquals(srcNode, fn.getSource());
  }
}
