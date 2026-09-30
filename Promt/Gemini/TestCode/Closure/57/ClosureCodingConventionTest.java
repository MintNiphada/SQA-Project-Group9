package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.CodingConvention.Bind;
import com.google.javascript.jscomp.CodingConvention.ObjectLiteralCast;
import com.google.javascript.jscomp.CodingConvention.SubclassRelationship;
import com.google.javascript.jscomp.CodingConvention.SubclassType;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ClosureCodingConventionTest {

  private ClosureCodingConvention conv;
  private Compiler compiler;

  @Before
  public void setUp() {
    conv = new ClosureCodingConvention();
    compiler = new Compiler();
  }

  private Node parseCall(String js) {
    Node root = compiler.parseTestCode(js);
    Node expr = root.getFirstChild();
    if (expr != null && expr.getType() == Token.EXPR_RESULT) {
      return expr.getFirstChild();
    }
    return root.getFirstChild();
  }

  private Node parseExprResult(String js) {
    Node root = compiler.parseTestCode(js);
    return root.getFirstChild();
  }

  @Test
  public void testApplySubclassRelationship() {
    JSTypeRegistry registry = new JSTypeRegistry(null);
    FunctionType parentCtor = registry.createConstructorType("Super", null, null, null);
    FunctionType childCtor = registry.createConstructorType("Sub", null, null, null);

    conv.applySubclassRelationship(parentCtor, childCtor, SubclassType.INHERITS);
    assertTrue(childCtor.hasProperty("superClass_"));
    assertTrue(childCtor.getPrototype().hasProperty("constructor"));

    FunctionType mixinParent = registry.createConstructorType("MixinSuper", null, null, null);
    FunctionType mixinChild = registry.createConstructorType("MixinSub", null, null, null);
    conv.applySubclassRelationship(mixinParent, mixinChild, SubclassType.MIXIN);
    assertFalse(mixinChild.hasProperty("superClass_"));
  }

  @Test
  public void testIsSuperClassReference() {
    assertTrue(conv.isSuperClassReference("superClass_"));
    assertFalse(conv.isSuperClassReference("superClass"));
    assertFalse(conv.isSuperClassReference(""));
    assertFalse(conv.isSuperClassReference("super"));
  }

  @Test
  public void testGetClassesDefinedByCall_inheritsStandard() {
    Node call = parseCall("goog.inherits(SubClass, SuperClass)");
    SubclassRelationship rel = conv.getClassesDefinedByCall(call);
    assertNotNull(rel);
    assertEquals(SubclassType.INHERITS, rel.type);
    assertEquals("SubClass", rel.subclassName);
    assertEquals("SuperClass", rel.superclassName);
  }

  @Test
  public void testGetClassesDefinedByCall_inheritsDollar() {
    Node call = parseCall("goog$inherits(SubClass, SuperClass)");
    SubclassRelationship rel = conv.getClassesDefinedByCall(call);
    assertNotNull(rel);
    assertEquals(SubclassType.INHERITS, rel.type);
    assertEquals("SubClass", rel.subclassName);
    assertEquals("SuperClass", rel.superclassName);
  }

  @Test
  public void testGetClassesDefinedByCall_inheritsDeprecated() {
    Node call = parseCall("SubClass.inherits(SuperClass)");
    SubclassRelationship rel = conv.getClassesDefinedByCall(call);
    assertNotNull(rel);
    assertEquals(SubclassType.INHERITS, rel.type);
    assertEquals("SubClass", rel.subclassName);
    assertEquals("SuperClass", rel.superclassName);
  }

  @Test
  public void testGetClassesDefinedByCall_mixinStandard() {
    Node call = parseCall("goog.mixin(SubClass.prototype, SuperClass.prototype)");
    SubclassRelationship rel = conv.getClassesDefinedByCall(call);
    assertNotNull(rel);
    assertEquals(SubclassType.MIXIN, rel.type);
    assertEquals("SubClass", rel.subclassName);
    assertEquals("SuperClass", rel.superclassName);
  }

  @Test
  public void testGetClassesDefinedByCall_mixinDollar() {
    Node call = parseCall("goog$mixin(SubClass.prototype, SuperClass.prototype)");
    SubclassRelationship rel = conv.getClassesDefinedByCall(call);
    assertNotNull(rel);
    assertEquals(SubclassType.MIXIN, rel.type);
    assertEquals("SubClass", rel.subclassName);
    assertEquals("SuperClass", rel.superclassName);
  }

  @Test
  public void testGetClassesDefinedByCall_mixinDeprecated() {
    Node call = parseCall("SubClass.mixin(SuperClass.prototype)");
    SubclassRelationship rel = conv.getClassesDefinedByCall(call);
    assertNotNull(rel);
    assertEquals(SubclassType.MIXIN, rel.type);
    assertEquals("SubClass", rel.subclassName);
    assertEquals("SuperClass", rel.superclassName);
  }

  @Test
  public void testGetClassesDefinedByCall_mixinInvalidSuperclassNoPrototype() {
    Node call = parseCall("goog.mixin(SubClass.prototype, SuperClass)");
    assertNull(conv.getClassesDefinedByCall(call));

    Node callDep = parseCall("SubClass.mixin(SuperClass)");
    assertNull(conv.getClassesDefinedByCall(callDep));
  }

  @Test
  public void testGetClassesDefinedByCall_mixinInvalidSubclassNoPrototype() {
    Node call = parseCall("goog.mixin(SubClass, SuperClass.prototype)");
    assertNull(conv.getClassesDefinedByCall(call));
  }

  @Test
  public void testGetClassesDefinedByCall_invalidArgCounts() {
    Node call1 = parseCall("goog.inherits(SubClass)");
    assertNull(conv.getClassesDefinedByCall(call1));

    Node call4 = parseCall("goog.inherits(SubClass, SuperClass, Extra)");
    assertNull(conv.getClassesDefinedByCall(call4));

    Node callDep1 = parseCall("SubClass.inherits()");
    assertNull(conv.getClassesDefinedByCall(callDep1));
  }

  @Test
  public void testGetClassesDefinedByCall_unscopedQualifiedNameCheck() {
    Node call = parseCall("goog.inherits(SubClass, cond ? SuperClass1 : BaseClass2)");
    assertNull(conv.getClassesDefinedByCall(call));

    Node call2 = parseCall("goog.inherits(cond ? Sub1 : Sub2, SuperClass)");
    assertNull(conv.getClassesDefinedByCall(call2));
  }

  @Test
  public void testGetClassesDefinedByCall_notAClassDefiningMethod() {
    Node call = parseCall("goog.foo(SubClass, SuperClass)");
    assertNull(conv.getClassesDefinedByCall(call));

    Node call2 = parseCall("foo$bar(SubClass, SuperClass)");
    assertNull(conv.getClassesDefinedByCall(call2));

    Node call3 = parseCall("foo()");
    assertNull(conv.getClassesDefinedByCall(call3));
  }

  @Test
  public void testExtractClassNameIfProvide() {
    Node expr = parseExprResult("goog.provide('foo.bar')");
    Node call = expr.getFirstChild();
    assertEquals("foo.bar", conv.extractClassNameIfProvide(call, expr));

    Node nonExprParent = new Node(Token.BLOCK, call);
    assertNull(conv.extractClassNameIfProvide(call, nonExprParent));

    Node callOther = parseExprResult("goog.require('foo.bar')").getFirstChild();
    assertNull(conv.extractClassNameIfProvide(callOther, expr));

    Node notGetProp = parseExprResult("provide('foo.bar')").getFirstChild();
    assertNull(conv.extractClassNameIfProvide(notGetProp, expr));

    Node noArg = parseExprResult("goog.provide()").getFirstChild();
    assertNull(conv.extractClassNameIfProvide(noArg, expr));
  }

  @Test
  public void testExtractClassNameIfRequire() {
    Node expr = parseExprResult("goog.require('foo.bar')");
    Node call = expr.getFirstChild();
    assertEquals("foo.bar", conv.extractClassNameIfRequire(call, expr));

    Node callProvide = parseExprResult("goog.provide('foo.bar')").getFirstChild();
    assertNull(conv.extractClassNameIfRequire(callProvide, expr));
  }

  @Test
  public void testExportFunctions() {
    assertEquals("goog.exportProperty", conv.getExportPropertyFunction());
    assertEquals("goog.exportSymbol", conv.getExportSymbolFunction());
    assertEquals("goog.abstractMethod", conv.getAbstractMethodName());
    assertEquals("goog.global", conv.getGlobalObject());
  }

  @Test
  public void testIdentifyTypeDeclarationCall() {
    Node validCall = parseCall("goog.addDependency('path/to/file.js', ['TypeA', 'TypeB', 123], [])");
    List<String> types = conv.identifyTypeDeclarationCall(validCall);
    assertNotNull(types);
    assertEquals(2, types.size());
    assertEquals("TypeA", types.get(0));
    assertEquals("TypeB", types.get(1));

    Node notAddDep = parseCall("goog.notAddDependency('path', ['TypeA'])");
    assertNull(conv.identifyTypeDeclarationCall(notAddDep));

    Node tooFewArgs = parseCall("goog.addDependency('path')");
    assertNull(conv.identifyTypeDeclarationCall(tooFewArgs));

    Node notArrayLit = parseCall("goog.addDependency('path', 'notAnArray', [])");
    assertNull(conv.identifyTypeDeclarationCall(notArrayLit));
  }

  @Test
  public void testGetSingletonGetterClassName() {
    Node call1 = parseCall("goog.addSingletonGetter(foo.bar.MyClass)");
    assertEquals("foo.bar.MyClass", conv.getSingletonGetterClassName(call1));

    Node call2 = parseCall("goog$addSingletonGetter(foo.bar.MyClass)");
    assertEquals("foo.bar.MyClass", conv.getSingletonGetterClassName(call2));

    Node callWrongName = parseCall("goog.otherFunc(foo.bar.MyClass)");
    assertNull(conv.getSingletonGetterClassName(callWrongName));

    Node callTooManyArgs = parseCall("goog.addSingletonGetter(foo.bar.MyClass, extra)");
    assertNull(conv.getSingletonGetterClassName(callTooManyArgs));

    Node callNoArgs = parseCall("goog.addSingletonGetter()");
    assertNull(conv.getSingletonGetterClassName(callNoArgs));
  }

  @Test
  public void testApplySingletonGetter() {
    JSTypeRegistry registry = new JSTypeRegistry(null);
    FunctionType functionType = registry.createConstructorType("Foo", null, null, null);
    FunctionType getterType = registry.createFunctionType(functionType);
    ObjectType objectType = registry.createAnonymousObjectType();

    conv.applySingletonGetter(functionType, getterType, objectType);
    assertTrue(functionType.hasProperty("getInstance"));
    assertTrue(functionType.hasProperty("instance_"));
  }

  @Test
  public void testIsPropertyTestFunction() {
    String[] testFunctions = {
        "goog.isDef", "goog.isNull", "goog.isDefAndNotNull",
        "goog.isString", "goog.isNumber", "goog.isBoolean",
        "goog.isFunction", "goog.isArray", "goog.isObject"
    };

    for (String fn : testFunctions) {
      Node call = parseCall(fn + "(val)");
      assertTrue("Expected true for " + fn, conv.isPropertyTestFunction(call));
    }

    Node notTestFn = parseCall("goog.isSomethingElse(val)");
    assertFalse(conv.isPropertyTestFunction(notTestFn));

    try {
      conv.isPropertyTestFunction(Node.newString(Token.NAME, "goog.isDef"));
      fail("Expected IllegalArgumentException for non-call node");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testGetObjectLiteralCast() {
    Node callValid = parseCall("goog.reflect.object(FooType, {a: 1})");
    NodeTraversal t = new NodeTraversal(compiler, null);
    ObjectLiteralCast cast = conv.getObjectLiteralCast(t, callValid);
    assertNotNull(cast);
    assertEquals("FooType", cast.typeName);
    assertNotNull(cast.objectNode);
    assertEquals(Token.OBJECTLIT, cast.objectNode.getType());

    Node callWrongName = parseCall("goog.reflect.other(FooType, {a: 1})");
    assertNull(conv.getObjectLiteralCast(t, callWrongName));

    Node callWrongArgs = parseCall("goog.reflect.object(FooType)");
    assertNull(conv.getObjectLiteralCast(t, callWrongArgs));

    Node callNonQualifiedName = parseCall("goog.reflect.object(cond ? A : B, {a: 1})");
    assertNull(conv.getObjectLiteralCast(t, callNonQualifiedName));

    Node callNotObjectLit = parseCall("goog.reflect.object(FooType, 'notObjLit')");
    ObjectLiteralCast castError = conv.getObjectLiteralCast(t, callNotObjectLit);
    assertNull(castError);
    assertEquals(1, compiler.getWarningCount());

    try {
      conv.getObjectLiteralCast(t, Node.newString(Token.NAME, "goog.reflect.object"));
      fail("Expected IllegalArgumentException for non-call node");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testIsOptionalParameter() {
    assertFalse(conv.isOptionalParameter(Node.newString(Token.NAME, "a")));
  }

  @Test
  public void testIsVarArgsParameter() {
    assertFalse(conv.isVarArgsParameter(Node.newString(Token.NAME, "a")));
  }

  @Test
  public void testIsPrivate() {
    assertFalse(conv.isPrivate("privateProp_"));
    assertFalse(conv.isPrivate("publicProp"));
  }

  @Test
  public void testGetAssertionFunctions() {
    Collection<AssertionFunctionSpec> assertions = conv.getAssertionFunctions();
    assertNotNull(assertions);
    assertEquals(7, assertions.size());
  }

  @Test
  public void testDescribeFunctionBind() {
    Node callBind = parseCall("goog.bind(fn, self, a, b)");
    Bind bind = conv.describeFunctionBind(callBind);
    assertNotNull(bind);
    assertEquals("fn", bind.target.getString());
    assertEquals("self", bind.thisValue.getString());
    assertEquals("a", bind.parameters.getString());

    Node callBindDollar = parseCall("goog$bind(fn, self)");
    Bind bindDollar = conv.describeFunctionBind(callBindDollar);
    assertNotNull(bindDollar);
    assertEquals("fn", bindDollar.target.getString());
    assertEquals("self", bindDollar.thisValue.getString());
    assertNull(bindDollar.parameters);

    Node callBindNoTarget = parseCall("goog.bind()");
    assertNull(conv.describeFunctionBind(callBindNoTarget));

    Node callPartial = parseCall("goog.partial(fn, a, b)");
    Bind partial = conv.describeFunctionBind(callPartial);
    assertNotNull(partial);
    assertEquals("fn", partial.target.getString());
    assertNull(partial.thisValue);
    assertEquals("a", partial.parameters.getString());

    Node callPartialDollar = parseCall("goog$partial(fn)");
    Bind partialDollar = conv.describeFunctionBind(callPartialDollar);
    assertNotNull(partialDollar);
    assertEquals("fn", partialDollar.target.getString());
    assertNull(partialDollar.thisValue);
    assertNull(partialDollar.parameters);

    Node callPartialNoTarget = parseCall("goog.partial()");
    assertNull(conv.describeFunctionBind(callPartialNoTarget));

    Node otherCall = parseCall("otherFunc(fn, self)");
    assertNull(conv.describeFunctionBind(otherCall));

    Node nonCall = Node.newString(Token.NAME, "foo");
    assertNull(conv.describeFunctionBind(nonCall));
  }
}