package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class FunctionTypeBuilderTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Node errorRoot;
  private Scope globalScope;
  private static final String SOURCE_NAME = "test.js";

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    errorRoot = new Node(Token.SCRIPT);
    errorRoot.setSourceEncodedName(SOURCE_NAME);
    globalScope = Scope.createGlobalScope(errorRoot);
  }

  private FunctionTypeBuilder newBuilder(String name) {
    return new FunctionTypeBuilder(name, compiler, errorRoot, SOURCE_NAME, globalScope);
  }

  private JSTypeExpression createTypeExpression(String typeString) {
    Node n = NodeUtil.newExpr(new Node(Token.NAME, typeString));
    return new JSTypeExpression(n, SOURCE_NAME);
  }

  @Test
  public void testIsFunctionTypeDeclaration() {
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    Assert.assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordConstructor();
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordInterface();
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordReturnType(createTypeExpression("number"));
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordParameter("x", createTypeExpression("number"));
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));

    builder = new JSDocInfoBuilder(true);
    builder.recordThisType(createTypeExpression("Object"));
    Assert.assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(builder.build(null)));
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildWithoutParamsThrowsException() {
    FunctionTypeBuilder builder = newBuilder("foo");
    builder.buildAndRegister();
  }

  @Test
  public void testBuildBasicFunction() {
    FunctionTypeBuilder builder = newBuilder("testFn");
    Node lp = new Node(Token.LP);
    Node param1 = Node.newString(Token.NAME, "a");
    lp.addChildToBack(param1);

    FunctionType fnType = builder
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertNotNull(fnType);
    Assert.assertEquals(1, fnType.getParametersNode().getChildCount());
    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE), fnType.getReturnType());
    Assert.assertFalse(fnType.isConstructor());
    Assert.assertFalse(fnType.isInterface());
  }

  @Test
  public void testSetSourceNode() {
    FunctionTypeBuilder builder = newBuilder("fn");
    Node sourceNode = new Node(Token.FUNCTION);
    builder.setSourceNode(sourceNode);
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder.inferParameterTypes(lp, null).buildAndRegister();
    Assert.assertEquals(sourceNode, fnType.getSource());
  }

  @Test
  public void testInferReturnTypeFromDoc() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    Node numNode = new Node(Token.NAME, "number");
    infoBuilder.recordReturnType(new JSTypeExpression(numNode, SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferReturnType(info)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE), fnType.getReturnType());
  }

  @Test
  public void testInferReturnStatementsAsLastResort() {
    Node block = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR);
    block.addChildToBack(varNode);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferReturnStatementsAsLastResort(block)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertEquals(registry.getNativeType(VOID_TYPE), fnType.getReturnType());
  }

  @Test
  public void testInferReturnStatementsWithReturnChild() {
    Node block = new Node(Token.BLOCK);
    Node ret = new Node(Token.RETURN);
    ret.addChildToBack(Node.newNumber(42));
    block.addChildToBack(ret);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferReturnStatementsAsLastResort(block)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE), fnType.getReturnType());
  }

  @Test
  public void testInferReturnStatementsWithThrow() {
    Node block = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF);
    Node ifBlock = new Node(Token.BLOCK);
    Node throwNode = new Node(Token.THROW);
    throwNode.addChildToBack(Node.newString(Token.NAME, "err"));
    ifBlock.addChildToBack(throwNode);
    ifNode.addChildToBack(Node.newString(Token.NAME, "cond"));
    ifNode.addChildToBack(ifBlock);
    block.addChildToBack(ifNode);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferReturnStatementsAsLastResort(block)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertEquals(registry.getNativeType(UNKNOWN_TYPE), fnType.getReturnType());
  }

  @Test
  public void testInferInheritanceConstructorAndInterface() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("MyClass");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferInheritance(info)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertTrue(fnType.isConstructor());
    Assert.assertNotNull(registry.getType("MyClass"));
  }

  @Test
  public void testInferInheritanceInterface() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordInterface();
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("MyInterface");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferInheritance(info)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertTrue(fnType.isInterface());
    Assert.assertNotNull(registry.getType("MyInterface"));
  }

  @Test
  public void testInferInheritanceExtendsWithoutTypedefWarning() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordBaseType(new JSTypeExpression(new Node(Token.NAME, "Object"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("myFn");
    Node lp = new Node(Token.LP);
    builder.inferInheritance(info).inferParameterTypes(lp, null).buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferInheritanceImplementsWithoutConstructorWarning() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordImplementedInterface(new JSTypeExpression(new Node(Token.NAME, "Object"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("myFn");
    Node lp = new Node(Token.LP);
    builder.inferInheritance(info).inferParameterTypes(lp, null).buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferInheritanceWithExtendsAndImplements() {
    ObjectType objectType = registry.getNativeObjectType(OBJECT_TYPE);

    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    infoBuilder.recordBaseType(new JSTypeExpression(new Node(Token.NAME, "Object"), SOURCE_NAME));
    infoBuilder.recordImplementedInterface(new JSTypeExpression(new Node(Token.NAME, "Object"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("SubClass");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferInheritance(info)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertTrue(fnType.isConstructor());
    List<ObjectType> interfaces = fnType.getImplementedInterfaces();
    Assert.assertNotNull(interfaces);
    Assert.assertTrue(interfaces.contains(objectType));
  }

  @Test
  public void testInferInheritanceExtendsNonObjectWarning() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    infoBuilder.recordBaseType(new JSTypeExpression(new Node(Token.NAME, "number"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("BadExtends");
    Node lp = new Node(Token.LP);
    builder.inferInheritance(info).inferParameterTypes(lp, null).buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferInheritanceBadImplementedTypeError() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    infoBuilder.recordImplementedInterface(new JSTypeExpression(new Node(Token.NAME, "number"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("BadImplements");
    Node lp = new Node(Token.LP);
    builder.inferInheritance(info).inferParameterTypes(lp, null).buildAndRegister();

    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testInferThisTypeExplicit() {
    ObjectType objType = registry.getNativeObjectType(OBJECT_TYPE);
    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferThisType(null, objType)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertEquals(objType, fnType.getTypeOfThis());
  }

  @Test
  public void testInferThisTypeFromDoc() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordThisType(new JSTypeExpression(new Node(Token.NAME, "Object"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferThisType(info, (Node) null)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertEquals(registry.getNativeObjectType(OBJECT_TYPE), fnType.getTypeOfThis());
  }

  @Test
  public void testInferThisTypeFromOwnerNode() {
    Node owner = Node.newString(Token.NAME, "Object");
    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder
        .inferThisType(null, owner)
        .inferParameterTypes(lp, null)
        .buildAndRegister();

    Assert.assertNotNull(fnType.getTypeOfThis());
  }

  @Test
  public void testInferThisTypeNonObjectWarning() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordThisType(new JSTypeExpression(new Node(Token.NAME, "number"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    builder.inferThisType(info, (Node) null).inferParameterTypes(lp, null).buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferParameterTypesFromDocAlone() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordParameter("a", new JSTypeExpression(new Node(Token.NAME, "number"), SOURCE_NAME));
    infoBuilder.recordParameter("b", new JSTypeExpression(new Node(Token.NAME, "string"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("fn");
    FunctionType fnType = builder
        .inferParameterTypes(info)
        .buildAndRegister();

    Assert.assertEquals(2, fnType.getParametersNode().getChildCount());
  }

  @Test
  public void testInferParameterTypesOptionalAndVarArgsOrderWarning() {
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addOptionalParams(registry.getNativeType(NUMBER_TYPE));
    paramBuilder.addRequiredParams(registry.getNativeType(STRING_TYPE));

    Node lp = new Node(Token.LP);
    Node p1 = Node.newString(Token.NAME, "opt_a");
    Node p2 = Node.newString(Token.NAME, "b");
    lp.addChildToBack(p1);
    lp.addChildToBack(p2);

    FunctionTypeBuilder builder = newBuilder("fn");
    builder.inferParameterTypes(lp, null).buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferParameterTypesVarArgsNotLastWarning() {
    Node lp = new Node(Token.LP);
    Node p1 = Node.newString(Token.NAME, "var_args");
    Node p2 = Node.newString(Token.NAME, "b");
    lp.addChildToBack(p1);
    lp.addChildToBack(p2);

    FunctionTypeBuilder builder = newBuilder("fn");
    builder.inferParameterTypes(lp, null).buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInexistentParamWarning() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordParameter("nonExistent", new JSTypeExpression(new Node(Token.NAME, "number"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    Node lp = new Node(Token.LP);
    Node p1 = Node.newString(Token.NAME, "a");
    lp.addChildToBack(p1);

    FunctionTypeBuilder builder = newBuilder("fn");
    builder.inferParameterTypes(lp, info).buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }

  @Test
  public void testInferTemplateTypeName() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordTemplateTypeName("T");
    infoBuilder.recordParameter("x", new JSTypeExpression(new Node(Token.NAME, "T"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "x"));

    FunctionType fnType = builder
        .inferTemplateTypeName(info)
        .inferParameterTypes(lp, info)
        .buildAndRegister();

    Assert.assertEquals("T", fnType.getTemplateTypeName());
  }

  @Test
  public void testTemplateTypeExpectedErrorWhenMissingFromParams() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordTemplateTypeName("T");
    infoBuilder.recordParameter("x", new JSTypeExpression(new Node(Token.NAME, "number"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "x"));

    builder
        .inferTemplateTypeName(info)
        .inferParameterTypes(lp, info);

    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testTemplateTypeDuplicatedError() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordTemplateTypeName("T");
    infoBuilder.recordParameter("x", new JSTypeExpression(new Node(Token.NAME, "T"), SOURCE_NAME));
    infoBuilder.recordParameter("y", new JSTypeExpression(new Node(Token.NAME, "T"), SOURCE_NAME));
    JSDocInfo info = infoBuilder.build(null);

    FunctionTypeBuilder builder = newBuilder("fn");
    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "x"));
    lp.addChildToBack(Node.newString(Token.NAME, "y"));

    builder
        .inferTemplateTypeName(info)
        .inferParameterTypes(lp, info);

    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testInferFromOverriddenFunctionWithoutParamsParent() {
    FunctionType oldType = registry.createFunctionType(
        registry.getNativeType(NUMBER_TYPE),
        registry.getNativeType(STRING_TYPE));

    FunctionTypeBuilder builder = newBuilder("overriddenFn");
    FunctionType newType = builder
        .inferFromOverriddenFunction(oldType, null)
        .buildAndRegister();

    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE), newType.getReturnType());
    Assert.assertEquals(1, newType.getParametersNode().getChildCount());
  }

  @Test
  public void testInferFromOverriddenFunctionWithLiteralParams() {
    FunctionType oldType = registry.createFunctionType(
        registry.getNativeType(NUMBER_TYPE),
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NUMBER_TYPE));

    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "p1"));
    lp.addChildToBack(Node.newString(Token.NAME, "p2"));
    lp.addChildToBack(Node.newString(Token.NAME, "p3Extra"));

    FunctionTypeBuilder builder = newBuilder("overriddenFn");
    FunctionType newType = builder
        .inferFromOverriddenFunction(oldType, lp)
        .buildAndRegister();

    Assert.assertEquals(registry.getNativeType(NUMBER_TYPE), newType.getReturnType());
    Assert.assertEquals(3, newType.getParametersNode().getChildCount());
  }

  @Test
  public void testInferFromOverriddenFunctionNull() {
    FunctionTypeBuilder builder = newBuilder("fn");
    builder.inferFromOverriddenFunction(null, null);
    Node lp = new Node(Token.LP);
    FunctionType fnType = builder.inferParameterTypes(lp, null).buildAndRegister();
    Assert.assertNotNull(fnType);
  }

  @Test
  public void testTypeRedefinitionWarning() {
    JSDocInfoBuilder infoBuilder = new JSDocInfoBuilder(true);
    infoBuilder.recordConstructor();
    JSDocInfo info = infoBuilder.build(null);

    Node lp1 = new Node(Token.LP);
    lp1.addChildToBack(Node.newString(Token.NAME, "a"));
    newBuilder("RedefinedClass")
        .inferInheritance(info)
        .inferParameterTypes(lp1, null)
        .buildAndRegister();

    Node lp2 = new Node(Token.LP);
    newBuilder("RedefinedClass")
        .inferInheritance(info)
        .inferParameterTypes(lp2, null)
        .buildAndRegister();

    Assert.assertEquals(1, compiler.getWarningCount());
  }
}