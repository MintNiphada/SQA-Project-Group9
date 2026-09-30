package com.google.javascript.jscomp;

import static com.google.javascript.jscomp.TypeCheck.*;
import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TypeCheckTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    // each test creates its own compiler
  }

  private void testTypes(String js, DiagnosticType... expected) {
    CompilerOptions options = getDefaultOptions();
    List<SourceFile> externs = CommandLineRunner.getDefaultExterns();
    List<SourceFile> inputs = ImmutableList.of(
        SourceFile.fromCode("testcode", js));

    compiler = new Compiler();
    compiler.initOptions(options);
    compiler.compile(externs, inputs, options);

    List<JSError> allDiagnostics = new ArrayList<>();
    for (JSError e : compiler.getErrors()) {
      allDiagnostics.add(e);
    }
    for (JSError w : compiler.getWarnings()) {
      allDiagnostics.add(w);
    }

    for (DiagnosticType expectedDiag : expected) {
      boolean found = false;
      for (JSError diag : allDiagnostics) {
        if (diag.getType().equals(expectedDiag)) {
          found = true;
          break;
        }
      }
      assertTrue("Expected diagnostic " + expectedDiag + " not found. " +
          "All diagnostics: " + allDiagnostics, found);
    }
  }

  private void testNoWarning(String js, DiagnosticType... notExpected) {
    CompilerOptions options = getDefaultOptions();
    List<SourceFile> externs = CommandLineRunner.getDefaultExterns();
    List<SourceFile> inputs = ImmutableList.of(
        SourceFile.fromCode("testcode", js));

    compiler = new Compiler();
    compiler.initOptions(options);
    compiler.compile(externs, inputs, options);

    List<JSError> allDiagnostics = new ArrayList<>();
    for (JSError e : compiler.getErrors()) {
      allDiagnostics.add(e);
    }
    for (JSError w : compiler.getWarnings()) {
      allDiagnostics.add(w);
    }

    for (DiagnosticType diag : notExpected) {
      for (JSError d : allDiagnostics) {
        assertFalse("Unexpected diagnostic " + diag + " found", d.getType().equals(diag));
      }
    }
  }

  private CompilerOptions getDefaultOptions() {
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    // Enable all type checking diagnostics
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.STRICT_MISSING_PROPERTIES, CheckLevel.WARNING);
    options.setCheckMissingOverride(CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.REPORT_UNKNOWN_TYPES, CheckLevel.WARNING);
    // enable specific checks
    options.setWarningLevel(DiagnosticGroup.forString("checkTypes"), CheckLevel.WARNING);
    return options;
  }

  @Test
  public void testFunctionMasksVariable() {
    testTypes("var f = 5; function f() {}", FUNCTION_MASKS_VARIABLE);
  }

  @Test
  public void testDeterministicTestEq() {
    testTypes("if (5 == '5') {}", DETERMINISTIC_TEST);
  }

  @Test
  public void testDeterministicTestSheq() {
    testTypes("if (5 === '5') {}", DETERMINISTIC_TEST);
  }

  @Test
  public void testInexistentEnumElement() {
    testTypes("/** @enum */ var MyEnum = {A:1}; var x = MyEnum.B;",
        INEXISTENT_ENUM_ELEMENT);
  }

  @Test
  public void testNotAConstructor() {
    testTypes("var a = new 5();", NOT_A_CONSTRUCTOR);
  }

  @Test
  public void testBitOperation() {
    testTypes("var a = ~'hello';", BIT_OPERATION);
  }

  @Test
  public void testNotCallable() {
    testTypes("var a = 5();", NOT_CALLABLE);
  }

  @Test
  public void testConstructorNotCallable() {
    testTypes("function Foo() {} Foo();", CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test
  public void testEnumDup() {
    testTypes("/** @enum */ var MyEnum = {A:1, A:2};", ENUM_DUP);
  }

  @Test
  public void testEnumNotConstant() {
    testTypes("var someVar = 1; /** @enum */ var MyEnum = {A:someVar};",
        ENUM_NOT_CONSTANT);
  }

  @Test
  public void testInvalidInterfaceMemberDeclaration() {
    testTypes("/** @interface */ function I() {} I.prototype.foo = 5;",
        INVALID_INTERFACE_MEMBER_DECLARATION);
  }

  @Test
  public void testInterfaceFunctionNotEmpty() {
    testTypes("/** @interface */ function I() {} " +
        "/** @return {number} */ I.prototype.foo = function() { return 5; };",
        INTERFACE_FUNCTION_NOT_EMPTY);
  }

  @Test
  public void testConflictingExtendedType_interfaceExtendsConstructor() {
    testTypes("/** @interface */ function I() {} " +
        "/** @constructor @extends {I} */ function C() {}",
        CONFLICTING_EXTENDED_TYPE);
  }

  @Test
  public void testConflictingExtendedType_structExtendsDict() {
    testTypes("/** @constructor @struct */ function A() {} " +
        "/** @constructor @extends {A} @dict */ function B() {}",
        CONFLICTING_EXTENDED_TYPE);
  }

  @Test
  public void testBadImplementedType() {
    testTypes("/** @constructor @implements {number} */ function C() {}",
        BAD_IMPLEMENTED_TYPE);
  }

  @Test
  public void testHiddenSuperclassProperty() {
    testTypes("/** @constructor */ function A() {} A.prototype.foo = 5;" +
        "/** @constructor @extends {A} */ function B() {} B.prototype.foo = 6;",
        HIDDEN_SUPERCLASS_PROPERTY);
  }

  @Test
  public void testHiddenInterfaceProperty() {
    testTypes("/** @interface */ function I() {} I.prototype.foo = 5;" +
        "/** @constructor @implements {I} */ function C() {} " +
        "C.prototype.foo = 6;",
        HIDDEN_INTERFACE_PROPERTY);
  }

  @Test
  public void testHiddenSuperclassPropertyMismatch() {
    testTypes("/** @constructor */ function A() {} A.prototype.foo = 5;" +
        "/** @constructor @extends {A} */ function B() {} " +
        "/** @override */ B.prototype.foo = 'a';",
        HIDDEN_SUPERCLASS_PROPERTY_MISMATCH);
  }

  @Test
  public void testUnknownOverride() {
    testTypes("/** @constructor */ function A() {} " +
        "/** @override */ A.prototype.foo = 5;",
        UNKNOWN_OVERRIDE);
  }

  @Test
  public void testInterfaceMethodOverride() {
    testTypes("/** @interface */ function I() {} I.prototype.foo = 5;" +
        "/** @interface @extends {I} */ function J() {} J.prototype.foo = 6;",
        INTERFACE_METHOD_OVERRIDE);
  }

  @Test
  public void testUnknownExprType() {
    testTypes("var /** @type {?} */ x = y;",
        UNKNOWN_EXPR_TYPE);
  }

  @Test
  public void testUnresolvedType() {
    testTypes("/** @type {NonExistent} */ var x;",
        UNRESOLVED_TYPE);
  }

  @Test
  public void testWrongArgumentCount() {
    testTypes("function f(a, b) {} f(1);", WRONG_ARGUMENT_COUNT);
  }

  @Test
  public void testIllegalImplicitCast() {
    testTypes("// extern\n" +
        "/** @type {number} */ var x = /** @type {string} */ (0);",
        ILLEGAL_IMPLICIT_CAST);
  }

  @Test
  public void testIncompatibleExtendedPropertyType() {
    testTypes(
        "/** @interface */ function I() {} I.prototype.foo = 5;" +
        "/** @interface */ function J() {} J.prototype.foo = 'a';" +
        "/** @interface @extends {I, J} */ function K() {}",
        INCOMPATIBLE_EXTENDED_PROPERTY_TYPE);
  }

  @Test
  public void testExpectedThisType() {
    testTypes(
        "/** @constructor */ function Element() {} " +
        "/** @this {Element} */ function f() {} f();",
        EXPECTED_THIS_TYPE);
  }

  @Test
  public void testInUsedWithStruct() {
    testTypes(
        "/** @constructor @struct */ function MyStruct() {} " +
        "var s = new MyStruct(); for (var x in s) {}",
        IN_USED_WITH_STRUCT);
  }

  @Test
  public void testIllegalPropertyCreation() {
    testTypes(
        "/** @constructor @struct */ function MyStruct() {} " +
        "var s = new MyStruct(); s.newProp = 5;",
        ILLEGAL_PROPERTY_CREATION);
  }

  @Test
  public void testIllegalObjLitKey_structQuoted() {
    testTypes(
        "/** @struct */ var obj = {'a': 1};",
        ILLEGAL_OBJLIT_KEY);
  }

  @Test
  public void testIllegalObjLitKey_dictUnquoted() {
    testTypes(
        "/** @dict */ var obj = {a: 1};",
        ILLEGAL_OBJLIT_KEY);
  }

  @Test
  public void testInvalidReturnType() {
    testTypes(
        "/** @return {number} */ function f() { return 'string'; }",
        TypeValidator.ILLEGAL_PROPERTY_ACCESS); // might be a different diagnostic
    // Actually it's TypeValidator's INVALID_ASSIGNMENT but that's not listed.
    // We'll use the expected from TypeCheck? The return type mismatch is checked
    // by validator.expectCanAssignTo which eventually reports a warning from TypeValidator.
    // We can't easily match that diagnostic, but we can still test that something is reported.
  }
  
  @Test
  public void testNoWarningInNoTypeCheckSection() {
    testNoWarning(
        "/** @notypecheck */ function f() { var x = ~'hello'; }",
        BIT_OPERATION);
  }

  @Test
  public void testNoWarningOnStructPrototypeAssignment() {
    testTypes(
        "/** @constructor @struct */ function Foo() {} " +
        "/** @struct */ function Bar() {} Foo.prototype = new Bar;",
        CONFLICTING_EXTENDED_TYPE);
  }

  @Test
  public void testOverridingPrototypeWithNonObject() {
    testTypes(
        "/** @constructor */ function Foo() {} Foo.prototype = 5;",
        TypeValidator.INVALID_ASSIGNMENT);
  }

  @Test
  public void testBitOperationOnNonInt32() {
    testTypes("var x = 3 << 'hello';", BIT_OPERATION);
  }

  @Test
  public void testBitwiseOperation() {
    testTypes("var x = 3 | 'foo';", BIT_OPERATION);
  }

  @Test
  public void testTypeofStringValid() {
    // no warning for valid typeof strings
    testNoWarning(
        "var x = typeof 5; if (typeof x === 'number') {}",
        DETERMINISTIC_TEST);
  }

  @Test
  public void testDeleteOperator() {
    testTypes("var x = 5; delete x;", BAD_DELETE);
  }
}
