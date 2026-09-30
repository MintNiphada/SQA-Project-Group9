package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;

public class CheckAccessControlsTest extends CompilerTestCase {

  public CheckAccessControlsTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new CheckAccessControls(compiler);
  }

  @Override
  protected CompilerOptions getOptions() {
    CompilerOptions options = super.getOptions();
    options.checkAccessControls = true;
    return options;
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableTypeCheck(CheckLevel.WARNING);
  }

  public void testDeprecatedNameGlobalCall() {
    test("/** @deprecated */ var foo = function() {}; foo();",
        CheckAccessControls.DEPRECATED_NAME);
  }

  public void testDeprecatedNameGlobalCallWithReason() {
    test("/** @deprecated Use bar instead */ var foo = function() {}; foo();",
        CheckAccessControls.DEPRECATED_NAME_REASON);
  }

  public void testDeprecatedNameInsideFunction() {
    test("/** @deprecated */ var x = 1; function f() { return x; }",
        CheckAccessControls.DEPRECATED_NAME);
  }

  public void testDeprecatedNameInsideFunctionWithReason() {
    test("/** @deprecated Use y instead */ var x = 1; function f() { return x; }",
        CheckAccessControls.DEPRECATED_NAME_REASON);
  }

  public void testDeprecatedNameInsideDeprecatedFunctionAllowed() {
    testSame("/** @deprecated */ var x = 1; /** @deprecated */ function f() { return x; }");
  }

  public void testDeprecatedConstructor() {
    test("/** @constructor\n * @deprecated */ function Foo() {} var x = new Foo();",
        CheckAccessControls.DEPRECATED_CLASS);
  }

  public void testDeprecatedConstructorWithReason() {
    test("/** @constructor\n * @deprecated Use Bar */ function Foo() {} var x = new Foo();",
        CheckAccessControls.DEPRECATED_CLASS_REASON);
  }

  public void testDeprecatedProperty() {
    test("/** @constructor */ function Foo() {}\n"
        + "/** @deprecated */ Foo.prototype.prop = 1;\n"
        + "function f() { var x = new Foo(); return x.prop; }",
        CheckAccessControls.DEPRECATED_PROP);
  }

  public void testDeprecatedPropertyWithReason() {
    test("/** @constructor */ function Foo() {}\n"
        + "/** @deprecated Use prop2 */ Foo.prototype.prop = 1;\n"
        + "function f() { var x = new Foo(); return x.prop; }",
        CheckAccessControls.DEPRECATED_PROP_REASON);
  }

  public void testDeprecatedPropertyAssignmentAllowed() {
    testSame("/** @constructor */ function Foo() {}\n"
        + "/** @deprecated */ Foo.prototype.prop = 1;\n"
        + "function f() { var x = new Foo(); x.prop = 2; }");
  }

  public void testDeprecatedClassMethodAccessAllowed() {
    testSame("/** @constructor\n * @deprecated */ function Foo() {}\n"
        + "Foo.prototype.bar = function() {};\n"
        + "Foo.prototype.baz = function() { this.bar(); };");
  }

  public void testDeprecatedStaticMethodAccessAllowed() {
    testSame("/** @constructor\n * @deprecated */ function Foo() {}\n"
        + "Foo.bar = function() {};\n"
        + "Foo.baz = function() { Foo.bar(); };");
  }

  public void testPrivateGlobalAccessDifferentFile() {
    test(new String[] {
      "/** @private */ var secret = 10;",
      "function f() { return secret; }"
    }, CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

  public void testPrivateGlobalAccessSameFileAllowed() {
    testSame("/** @private */ var secret = 10; function f() { return secret; }");
  }

  public void testPrivateConstructorDifferentFile() {
    test(new String[] {
      "/** @constructor\n * @private */ function Foo() {}",
      "var x = new Foo();"
    }, CheckAccessControls.BAD_PRIVATE_GLOBAL_ACCESS);
  }

  public void testPrivateConstructorLegalAccessDifferentFile() {
    testSame(new String[] {
      "/** @constructor\n * @private */ function Foo() {}\n"
          + "Foo.makeInstance = function() { return new Foo(); };",
      "var x = Foo.makeInstance();"
    });
  }

  public void testPrivatePropertyAccessDifferentFile() {
    test(new String[] {
      "/** @constructor */ function Foo() { /** @private */ this.x_ = 1; }",
      "function g() { var f = new Foo(); return f.x_; }"
    }, CheckAccessControls.BAD_PRIVATE_PROPERTY_ACCESS);
  }

  public void testPrivatePropertyAccessSameFileAllowed() {
    testSame("/** @constructor */ function Foo() { /** @private */ this.x_ = 1; }\n"
        + "function g() { var f = new Foo(); return f.x_; }");
  }

  public void testPrivatePropertyAccessFromClassMethodAllowed() {
    testSame(new String[] {
      "/** @constructor */ function Foo() { /** @private */ this.x_ = 1; }",
      "Foo.prototype.getX = function() { return this.x_; };"
    });
  }

  public void testProtectedPropertyAccessDifferentFileNonSubclass() {
    test(new String[] {
      "/** @constructor */ function Foo() { /** @protected */ this.x = 1; }",
      "function g() { var f = new Foo(); return f.x; }"
    }, CheckAccessControls.BAD_PROTECTED_PROPERTY_ACCESS);
  }

  public void testProtectedPropertyAccessSubclassAllowed() {
    testSame(new String[] {
      "/** @constructor */ function Foo() { /** @protected */ this.x = 1; }",
      "/** @constructor\n * @extends {Foo} */ function Sub() {\n"
          + "  Foo.call(this);\n"
          + "  this.x = 2;\n"
          + "}"
    });
  }

  public void testPrivateOverrideDifferentFile() {
    test(new String[] {
      "/** @constructor */ function Foo() {}\n"
          + "/** @private */ Foo.prototype.bar = function() {};",
      "/** @constructor\n * @extends {Foo} */ function Sub() {}\n"
          + "Sub.prototype.bar = function() {};"
    }, CheckAccessControls.PRIVATE_OVERRIDE);
  }

  public void testVisibilityMismatch() {
    test(new String[] {
      "/** @constructor */ function Foo() {}\n"
          + "/** @protected */ Foo.prototype.bar = function() {};",
      "/** @constructor\n * @extends {Foo} */ function Sub() {}\n"
          + "/** @public */ Sub.prototype.bar = function() {};"
    }, CheckAccessControls.VISIBILITY_MISMATCH);
  }

  public void testConstantPropertyReassigned() {
    test("/** @constructor */ function Foo() {}\n"
        + "/** @const */ Foo.prototype.BAR = 1;\n"
        + "var f = new Foo();\n"
        + "f.BAR = 2;",
        CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  public void testConstantPropertyOnObjectLiteralReassigned() {
    test("var obj = { /** @const */ a: 1 }; obj.a = 2;",
        CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  public void testConstantPropertyIncremented() {
    test("var obj = { /** @const */ a: 1 }; obj.a++;",
        CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  public void testConstantPropertyDecremented() {
    test("var obj = { /** @const */ a: 1 }; obj.a--;",
        CheckAccessControls.CONST_PROPERTY_REASSIGNED_VALUE);
  }

  public void testHotSwapScript() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkAccessControls = true;
    compiler.initOptions(options);

    CheckAccessControls pass = new CheckAccessControls(compiler);
    Node scriptRoot = compiler.parseTestCode("var a = 1;");
    pass.hotSwapScript(scriptRoot);
  }

  public void testProcessTraversal() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkAccessControls = true;
    compiler.initOptions(options);

    CheckAccessControls pass = new CheckAccessControls(compiler);
    Node root = compiler.parseTestCode("var a = 1; function foo() { return a; }");
    pass.process(null, root);
  }
}