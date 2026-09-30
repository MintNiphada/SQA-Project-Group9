package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class TargetClassTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Node testProcess(String js, CheckLevel requiresLevel) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);
    ProcessClosurePrimitives pass =
        new ProcessClosurePrimitives(compiler, null, requiresLevel);
    pass.process(externs, root);
    return root;
  }

  private Node testProcess(String js) {
    return testProcess(js, CheckLevel.ERROR);
  }

  private Node testProcessWithSymbolTable(String js) {
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);
    PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(root);
    ProcessClosurePrimitives pass =
        new ProcessClosurePrimitives(compiler, symbolTable, CheckLevel.ERROR);
    pass.process(externs, root);
    return root;
  }

  @Test
  public void testSimpleProvide() {
    testProcess("goog.provide('foo.bar');");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testProvideWithVarDeclaration() {
    testProcess("goog.provide('foo'); var foo = 1;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testProvideWithAssignment() {
    testProcess("goog.provide('foo.bar'); foo.bar = function() {};");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDuplicateProvideError() {
    testProcess("goog.provide('foo.bar'); goog.provide('foo.bar');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.DUPLICATE_NAMESPACE_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testInvalidProvideError() {
    testProcess("goog.provide('foo.123');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.INVALID_PROVIDE_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testFunctionNamespaceError() {
    testProcess("goog.provide('foo'); function foo() {}");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.FUNCTION_NAMESPACE_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSimpleRequire() {
    testProcess("goog.provide('foo.bar'); goog.require('foo.bar');");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testMissingRequireError() {
    testProcess("goog.require('missing.namespace');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.MISSING_PROVIDE_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testLateRequireError() {
    testProcess("goog.require('foo.bar'); goog.provide('foo.bar');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.LATE_PROVIDE_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testRequireCheckLevelOff() {
    testProcess("goog.require('missing.namespace');", CheckLevel.OFF);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testExportSymbol() {
    Node root = compiler.parseTestCode(
        "goog.exportSymbol('myVar', x); goog.exportSymbol('foo.bar.baz', y);");
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);
    ProcessClosurePrimitives pass =
        new ProcessClosurePrimitives(compiler, null, CheckLevel.ERROR);
    pass.process(externs, root);

    Set<String> exported = pass.getExportedVariableNames();
    Assert.assertTrue(exported.contains("myVar"));
    Assert.assertTrue(exported.contains("foo"));
    Assert.assertEquals(2, exported.size());
  }

  @Test
  public void testAddDependency() {
    testProcess("goog.addDependency('foo.js', ['foo'], ['bar']);");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testGoogBaseInConstructorValid() {
    testProcess(
        "function Foo() { goog.base(this); }\n" +
        "goog.inherits(Foo, BaseFoo);");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testGoogBaseInMethodValid() {
    testProcess(
        "function Foo() {}\n" +
        "goog.inherits(Foo, BaseFoo);\n" +
        "Foo.prototype.bar = function() { goog.base(this, 'bar', 1); };");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testGoogBaseDirectPropertyAccess() {
    testProcess("var x = goog.base;");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testGoogBaseMissingThis() {
    testProcess("function Foo() { goog.base(); }");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testGoogBaseOutsideMethod() {
    testProcess("goog.base(this);");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testGoogBaseNoInherits() {
    testProcess("function Foo() { goog.base(this); }");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testGoogBaseMethodMissingMethodName() {
    testProcess(
        "Foo.prototype.bar = function() { goog.base(this); };");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testGoogBaseMethodMismatch() {
    testProcess(
        "Foo.prototype.bar = function() { goog.base(this, 'baz'); };");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.BASE_CLASS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSetCssNameMappingValidByPart() {
    testProcess("goog.setCssNameMapping({'button': 'btn'}, 'BY_PART');");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertNotNull(compiler.getCssRenamingMap());
    Assert.assertEquals("btn", compiler.getCssRenamingMap().get("button"));
    Assert.assertEquals("other", compiler.getCssRenamingMap().get("other"));
  }

  @Test
  public void testSetCssNameMappingValidByWhole() {
    testProcess("goog.setCssNameMapping({'a': 'x', 'b': 'y', 'a-b': 'x-y'}, 'BY_WHOLE');");
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertNotNull(compiler.getCssRenamingMap());
    Assert.assertEquals("x-y", compiler.getCssRenamingMap().get("a-b"));
  }

  @Test
  public void testSetCssNameMappingNullArg() {
    testProcess("goog.setCssNameMapping();");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.NULL_ARGUMENT_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSetCssNameMappingNonObjectLit() {
    testProcess("goog.setCssNameMapping('test');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.EXPECTED_OBJECTLIT_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSetCssNameMappingSecondArgNotString() {
    testProcess("goog.setCssNameMapping({}, 123);");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.EXPECTED_STRING_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSetCssNameMappingTooManyArgs() {
    testProcess("goog.setCssNameMapping({}, 'BY_PART', 'extra');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSetCssNameMappingNonStringValue() {
    testProcess("goog.setCssNameMapping({'a': 123});");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(
        ProcessClosurePrimitives.NON_STRING_PASSED_TO_SET_CSS_NAME_MAPPING_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSetCssNameMappingInvalidStyle() {
    testProcess("goog.setCssNameMapping({}, 'INVALID_STYLE');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.INVALID_STYLE_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testSetCssNameMappingInvalidByPartKeyWithDash() {
    testProcess("goog.setCssNameMapping({'btn-primary': 'btnp'}, 'BY_PART');");
    Assert.assertEquals(1, compiler.getWarningCount());
    Assert.assertEquals(ProcessClosurePrimitives.INVALID_CSS_RENAMING_MAP,
        compiler.getWarnings()[0].getType());
  }

  @Test
  public void testSetCssNameMappingInvalidByWholeCombination() {
    testProcess("goog.setCssNameMapping({'a': 'x', 'b': 'y', 'a-b': 'z'}, 'BY_WHOLE');");
    Assert.assertEquals(1, compiler.getWarningCount());
    Assert.assertEquals(ProcessClosurePrimitives.INVALID_CSS_RENAMING_MAP,
        compiler.getWarnings()[0].getType());
  }

  @Test
  public void testDefineValid() {
    testProcess("/** @define {boolean} */ goog.define('FLAG', true);");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testDefineMissingAnnotation() {
    testProcess("goog.define('FLAG', true);");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.MISSING_DEFINE_ANNOTATION,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testDefineInvalidName() {
    testProcess("/** @define {boolean} */ goog.define('invalid-name', true);");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.INVALID_DEFINE_NAME_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testDefineNullArgs() {
    testProcess("/** @define {boolean} */ goog.define();");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.NULL_ARGUMENT_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testDefineInvalidArgType() {
    testProcess("/** @define {boolean} */ goog.define(123, true);");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testDefineTooManyArgs() {
    testProcess("/** @define {boolean} */ goog.define('FLAG', true, 'extra');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testProvideWithTypedef() {
    testProcess("goog.provide('foo.Bar'); /** @typedef {string} */ foo.Bar;");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testProvideAndRequireWithSymbolTable() {
    testProcessWithSymbolTable("goog.provide('foo.bar'); goog.require('foo.bar');");
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testHotSwapScript() {
    Node root = compiler.parseTestCode("goog.provide('foo');");
    Node externs = new Node(Token.BLOCK);
    Node parent = new Node(Token.BLOCK, externs, root);
    ProcessClosurePrimitives pass =
        new ProcessClosurePrimitives(compiler, null, CheckLevel.ERROR);
    pass.hotSwapScript(root, null);
    Assert.assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testProvideNullArgument() {
    testProcess("goog.provide();");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.NULL_ARGUMENT_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testProvideInvalidArgument() {
    testProcess("goog.provide(123);");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.INVALID_ARGUMENT_ERROR,
        compiler.getErrors()[0].getType());
  }

  @Test
  public void testProvideTooManyArguments() {
    testProcess("goog.provide('foo', 'bar');");
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(ProcessClosurePrimitives.TOO_MANY_ARGUMENTS_ERROR,
        compiler.getErrors()[0].getType());
  }
}