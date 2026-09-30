package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.FunctionTypeBuilder.AstFunctionContents;
import com.google.javascript.jscomp.FunctionTypeBuilder.UnknownFunctionContents;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionBuilder;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.NamedType;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

public class FunctionTypeBuilderTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Node rootNode;
  private Scope globalScope;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    rootNode = IR.block();
    globalScope = Scope.createGlobalScope(rootNode);
  }

  private FunctionTypeBuilder newBuilder(String name) {
    return new FunctionTypeBuilder(name, compiler, rootNode, "test.js", globalScope);
  }

  @Test
  public void testBasicFunctionBuild() {
    FunctionTypeBuilder builder = newBuilder("testFn");
    Node params = IR.paramList(IR.name("a"), IR.name("b"));
    builder.inferParameterTypes(params, null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertNotNull(type);
    Assert.assertFalse(type.isConstructor());
    Assert.assertFalse(type.isInterface());
    Assert.assertEquals(2, type.getParameters().size());
  }

  @Test
  public void testNullFnNameHandled() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(null, compiler, rootNode, "test.js", globalScope);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();
    Assert.assertNotNull(type);
  }

  @Test(expected = NullPointerException.class)
  public void testNullErrorRootThrowsException() {
    new FunctionTypeBuilder("fn", compiler, null, "test.js", globalScope);
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildWithoutParamsThrowsException() {
    FunctionTypeBuilder builder = newBuilder("testFn");
    builder.buildAndRegister();
  }

  @Test
  public void testSetContents() {
    FunctionTypeBuilder builder = newBuilder("testFn");
    Assert.assertSame(builder, builder.setContents(null));

    Node fnNode = IR.function(IR.name("testFn"), IR.paramList(), IR.block());
    AstFunctionContents contents = new AstFunctionContents(fnNode);
    contents.recordNonEmptyReturn();
    contents.recordEscapedVarName("x");
    contents.recordEscapedVarName("y");

    Assert.assertSame(fnNode, contents.getSourceNode());
    Assert.assertTrue(contents.mayHaveNonEmptyReturns());
    Assert.assertFalse(contents.mayBeFromExterns());
    Assert.assertTrue(Sets.newHashSet(contents.getEscapedVarNames()).contains("x"));
    Assert.assertTrue(Sets.newHashSet(contents.getEscapedVarNames()).contains("y"));

    builder.setContents(contents);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();
    Assert.assertNotNull(type);
  }

  @Test
  public void testUnknownFunctionContents() {
    FunctionTypeBuilder.FunctionContents contents = UnknownFunctionContents.get();
    Assert.assertNull(contents.getSourceNode());
    Assert.assertTrue(contents.mayBeFromExterns());
    Assert.assertTrue(contents.mayHaveNonEmptyReturns());
    Assert.assertFalse(contents.getEscapedVarNames().iterator().hasNext());
  }

  @Test
  public void testAstFunctionContentsEmptyEscaped() {
    Node fnNode = IR.function(IR.name("fn"), IR.paramList(), IR.block());
    AstFunctionContents contents = new AstFunctionContents(fnNode);
    Assert.assertFalse(contents.getEscapedVarNames().iterator().hasNext());
    Assert.assertFalse(contents.mayHaveNonEmptyReturns());
  }

  @Test
  public void testInferReturnTypeFromDoc() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordReturnType(new JSTypeExpression(new Node(Token.STRING, "string"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("testFn");
    builder.inferReturnType(info);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(registry.getNativeType(STRING_TYPE), type.getReturnType());
  }

  @Test
  public void testInferReturnTypeVoidWhenNoReturns() {
    Node fnNode = IR.function(IR.name("testFn"), IR.paramList(), IR.block());
    AstFunctionContents contents = new AstFunctionContents(fnNode);

    FunctionTypeBuilder builder = newBuilder("testFn");
    builder.setContents(contents);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(registry.getNativeType(VOID_TYPE), type.getReturnType());
    Assert.assertTrue(type.isReturnTypeInferred());
  }

  @Test
  public void testInferInheritanceConstructorWithBaseType() {
    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    registry.declareType("SuperClass", objType);

    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    infoBuilder.recordBaseType(new JSTypeExpression(new Node(Token.STRING, "SuperClass"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("SubClass");
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertTrue(type.isConstructor());
    Assert.assertNotNull(type.getPrototype());
  }

  @Test
  public void testInferInheritanceExtendsWithoutConstructorWarns() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordBaseType(new JSTypeExpression(new Node(Token.STRING, "Object"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("regularFn");
    builder.inferInheritance(info);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferInheritanceImplementsWithoutConstructorWarns() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordImplementedInterface(new JSTypeExpression(new Node(Token.STRING, "Object"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("regularFn");
    builder.inferInheritance(info);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferInheritanceInterfaceWithExtendedInterfaces() {
    ObjectType iface1 = registry.createInterfaceType("Interface1", null).getInstanceType();
    registry.declareType("Interface1", iface1);

    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordInterface();
    infoBuilder.recordExtendedInterface(new JSTypeExpression(new Node(Token.STRING, "Interface1"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("Interface2");
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertTrue(type.isInterface());
    Assert.assertFalse(type.getExtendedInterfaces().isEmpty());
  }

  @Test
  public void testInferThisTypeFromDoc() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordThisType(new JSTypeExpression(new Node(Token.STRING, "Object"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("fnWithThis");
    builder.inferThisType(info);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(registry.getNativeObjectType(OBJECT_TYPE), type.getTypeOfThis());
  }

  @Test
  public void testInferThisTypeFromTypeArgument() {
    FunctionTypeBuilder builder = newBuilder("fnWithThis");
    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    builder.inferThisType(null, objType);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(objType, type.getTypeOfThis());
  }

  @Test
  public void testInferThisTypeNonObjectWarns() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordThisType(new JSTypeExpression(new Node(Token.STRING, "number"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("fnBadThis");
    builder.inferThisType(info);
    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferParameterTypesFromDocAlone() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordParameter("param1", new JSTypeExpression(new Node(Token.STRING, "number"), "test.js"));
    infoBuilder.recordParameter("param2", new JSTypeExpression(new Node(Token.STRING, "string"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("fnParams");
    builder.inferParameterTypes(info);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(2, type.getParameters().size());
  }

  @Test
  public void testInferParameterTypesWithMissingDocParamWarns() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordParameter("paramNotInArgs", new JSTypeExpression(new Node(Token.STRING, "number"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("fnMissingParam");
    Node params = IR.paramList(IR.name("actualParam"));
    builder.inferParameterTypes(params, info);
    builder.buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferParameterTypesOrderMismatchWarns() {
    FunctionTypeBuilder builder = newBuilder("fnBadOrder");
    Node optParam = IR.name("opt");
    Node reqParam = IR.name("req");
    Node params = IR.paramList(optParam, reqParam);

    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    Node optTypeNode = new Node(Token.EQUALS, new Node(Token.STRING, "number"));
    infoBuilder.recordParameter("opt", new JSTypeExpression(optTypeNode, "test.js"));
    infoBuilder.recordParameter("req", new JSTypeExpression(new Node(Token.STRING, "string"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    builder.inferParameterTypes(params, info);
    builder.buildAndRegister();

    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInferParameterTypesVarArgsNotLastWarns() {
    FunctionTypeBuilder builder = newBuilder("fnVarArgsNotLast");
    Node varParam = IR.name("rest");
    Node reqParam = IR.name("req");
    Node params = IR.paramList(varParam, reqParam);

    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    Node varTypeNode = new Node(Token.ELLIPSIS, new Node(Token.STRING, "number"));
    infoBuilder.recordParameter("rest", new JSTypeExpression(varTypeNode, "test.js"));
    infoBuilder.recordParameter("req", new JSTypeExpression(new Node(Token.STRING, "string"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    builder.inferParameterTypes(params, info);
    builder.buildAndRegister();

    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testInferFromOverriddenFunctionNull() {
    FunctionTypeBuilder builder = newBuilder("fn");
    Assert.assertSame(builder, builder.inferFromOverriddenFunction(null, null));
  }

  @Test
  public void testInferFromOverriddenFunctionNoParamsParent() {
    FunctionType parentType = new FunctionBuilder(registry)
        .withParamsNode(IR.paramList(IR.name("a"), IR.name("b")))
        .withReturnType(registry.getNativeType(NUMBER_TYPE))
        .build();

    FunctionTypeBuilder builder = newBuilder("childFn");
    builder.inferFromOverriddenFunction(parentType, null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE), type.getReturnType());
    Assert.assertEquals(2, type.getParameters().size());
  }

  @Test
  public void testInferFromOverriddenFunctionWithLiteralParams() {
    FunctionType parentType = new FunctionBuilder(registry)
        .withParamsNode(IR.paramList(IR.name("a"), IR.name("b")))
        .withReturnType(registry.getNativeType(STRING_TYPE))
        .build();

    FunctionTypeBuilder builder = newBuilder("childFn");
    Node literalParams = IR.paramList(IR.name("a"), IR.name("b"), IR.name("c"));
    builder.inferFromOverriddenFunction(parentType, literalParams);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(registry.getNativeType(STRING_TYPE), type.getReturnType());
    Assert.assertEquals(3, type.getParameters().size());
  }

  @Test
  public void testInferFromOverriddenFunctionVarArgsToOptional() {
    Node varArgParam = IR.name("args");
    varArgParam.setVarArgs(true);
    FunctionType parentType = new FunctionBuilder(registry)
        .withParamsNode(IR.paramList(varArgParam))
        .build();

    FunctionTypeBuilder builder = newBuilder("childFn");
    Node literalParams = IR.paramList(IR.name("arg1"), IR.name("arg2"));
    builder.inferFromOverriddenFunction(parentType, literalParams);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals(2, type.getParameters().size());
  }

  @Test
  public void testInferTemplateTypeName() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordTemplateTypeName("T");
    infoBuilder.recordParameter("val", new JSTypeExpression(new Node(Token.STRING, "T"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("identity");
    builder.inferTemplateTypeName(info);
    builder.inferParameterTypes(IR.paramList(IR.name("val")), info);
    FunctionType type = builder.buildAndRegister();

    Assert.assertEquals("T", type.getTemplateTypeName());
  }

  @Test
  public void testInferTemplateTypeDuplicatedError() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordTemplateTypeName("T");
    infoBuilder.recordParameter("a", new JSTypeExpression(new Node(Token.STRING, "T"), "test.js"));
    infoBuilder.recordParameter("b", new JSTypeExpression(new Node(Token.STRING, "T"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("testTemplateDup");
    builder.inferTemplateTypeName(info);
    builder.inferParameterTypes(IR.paramList(IR.name("a"), IR.name("b")), info);
    builder.buildAndRegister();

    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testInferTemplateTypeExpectedError() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordTemplateTypeName("T");
    infoBuilder.recordParameter("a", new JSTypeExpression(new Node(Token.STRING, "number"), "test.js"));
    infoBuilder.recordReturnType(new JSTypeExpression(new Node(Token.STRING, "T"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("testTemplateExpected");
    builder.inferTemplateTypeName(info);
    builder.inferReturnType(info);
    builder.inferParameterTypes(IR.paramList(IR.name("a")), info);
    builder.buildAndRegister();

    Assert.assertEquals(2, compiler.getErrorCount());
  }

  @Test
  public void testGetOrCreateConstructorExistingType() {
    registry.declareType("ExistingClass", registry.getNativeObjectType(OBJECT_TYPE));

    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("ExistingClass");
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertNotNull(type);
  }

  @Test
  public void testGetOrCreateConstructorFunctionNative() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("Function");
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertNotNull(type);
  }

  @Test
  public void testScopeDeclaredInWithDotIndex() {
    Node varNode = IR.var(IR.name("ns"));
    globalScope.declare("ns", varNode, null, null);

    FunctionTypeBuilder builder = newBuilder("ns.MyClass");
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    JSDocInfo info = infoBuilder.build(rootNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType type = builder.buildAndRegister();

    Assert.assertNotNull(type);
  }

  @Test
  public void testIsFunctionTypeDeclaration() {
    JSDocInfoBuilder b1 = new JSDocInfoBuilder(true);
    b1.recordConstructor();
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b1.build(rootNode)));

    JSDocInfoBuilder b2 = new JSDocInfoBuilder(true);
    b2.recordInterface();
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b2.build(rootNode)));

    JSDocInfoBuilder b3 = new JSDocInfoBuilder(true);
    b3.recordReturnType(new JSTypeExpression(new Node(Token.STRING, "boolean"), "test.js"));
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b3.build(rootNode)));

    JSDocInfoBuilder b4 = new JSDocInfoBuilder(true);
    b4.recordThisType(new JSTypeExpression(new Node(Token.STRING, "Object"), "test.js"));
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b4.build(rootNode)));

    JSDocInfoBuilder b5 = new JSDocInfoBuilder(true);
    b5.recordParameter("x", new JSTypeExpression(new Node(Token.STRING, "number"), "test.js"));
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b5.build(rootNode)));

    JSDocInfoBuilder b6 = new JSDocInfoBuilder(true);
    Assert.assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(b6.build(rootNode)));
  }

  @Test
  public void testExtendedTypeValidatorNonObjectWarns() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    infoBuilder.recordBaseType(new JSTypeExpression(new Node(Token.STRING, "number"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("BadCtor");
    builder.inferInheritance(info);
    Assert.assertTrue(compiler.getWarningCount() > 0);
  }

  @Test
  public void testImplementedTypeValidatorNonObjectErrors() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    infoBuilder.recordImplementedInterface(new JSTypeExpression(new Node(Token.STRING, "number"), "test.js"));
    JSDocInfo info = infoBuilder.build(rootNode);

    FunctionTypeBuilder builder = newBuilder("BadCtor");
    builder.inferInheritance(info);
    Assert.assertTrue(compiler.getErrorCount() > 0);
  }
}