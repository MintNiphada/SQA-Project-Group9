package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.io.File;

public class ProcessCommonJSModulesTest {

  @Test
  public void testToModuleNameSimple() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo.js"));
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo"));
    assertEquals("module$foo_bar", ProcessCommonJSModules.toModuleName("foo-bar.js"));
  }

  @Test
  public void testToModuleNameWithLeadingDotSlash() {
    String filename = "." + File.separator + "foo.js";
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName(filename));
  }

  @Test
  public void testToModuleNameWithSubdirectory() {
    String filename = "dir" + File.separator + "sub" + File.separator + "foo.js";
    assertEquals("module$dir$sub$foo", ProcessCommonJSModules.toModuleName(filename));
  }

  @Test
  public void testToModuleNameRelative() {
    String current = "app" + File.separator + "index.js";
    String required = "." + File.separator + "util.js";
    String result = ProcessCommonJSModules.toModuleName(required, current);
    assertEquals("module$app$util", result);

    String requiredParent = ".." + File.separator + "common.js";
    String resultParent = ProcessCommonJSModules.toModuleName(requiredParent, current);
    assertEquals("module$common", resultParent);

    String nonRelative = "other" + File.separator + "lib.js";
    String resultNonRelative = ProcessCommonJSModules.toModuleName(nonRelative, current);
    assertEquals("module$other$lib", resultNonRelative);
  }

  @Test(expected = RuntimeException.class)
  public void testToModuleNameInvalidUri() {
    String invalidRequired = "." + File.separator + "invalid^uri";
    ProcessCommonJSModules.toModuleName(invalidRequired, "somefile.js");
  }

  @Test
  public void testGuessCJSModuleNameAndNormalizeSourceName() {
    Compiler compiler = new Compiler();
    String prefix = "src" + File.separator + "js";
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, prefix);

    String sourceFileWithPrefix = "src" + File.separator + "js" + File.separator + "foo" + File.separator + "bar.js";
    assertEquals("module$foo$bar", pass.guessCJSModuleName(sourceFileWithPrefix));

    String sourceFileWithoutPrefix = "other" + File.separator + "path" + File.separator + "bar.js";
    assertEquals("module$other$path$bar", pass.guessCJSModuleName(sourceFileWithoutPrefix));
  }

  @Test
  public void testConstructorsAndGetModule() {
    Compiler compiler = new Compiler();
    String prefix = "src" + File.separator;
    ProcessCommonJSModules passWithDeps = new ProcessCommonJSModules(compiler, prefix);
    assertNull(passWithDeps.getModule());

    ProcessCommonJSModules passNoDeps = new ProcessCommonJSModules(compiler, prefix, false);
    assertNull(passNoDeps.getModule());
  }

  @Test
  public void testProcessSimpleModuleExports() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String fileName = "module1.js";
    String js = "var a = 10; exports.foo = a; module.exports = exports.foo;";
    JSSourceFile input = JSSourceFile.fromCode(fileName, js);

    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{input}, options);
    Node root = compiler.parseInputs();
    Node scriptNode = root.getLastChild().getFirstChild();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "." + File.separator, true);
    pass.process(null, scriptNode);

    assertNotNull(pass.getModule());
    assertEquals("module$module1", pass.getModule().getName());
    String generatedCode = compiler.toSource(scriptNode);
    assertTrue(generatedCode.contains("goog.provide(\"module$module1\")"));
    assertTrue(generatedCode.contains("var module$module1 = {}"));
    assertTrue(generatedCode.contains("a$$module$module1 = 10"));
    assertTrue(generatedCode.contains("module$module1.foo = a$$module$module1"));
    assertTrue(generatedCode.contains("module$module1.module$exports = module$module1.foo"));
    assertTrue(generatedCode.contains("if(module$module1.module$exports)"));
  }

  @Test
  public void testProcessRequireCall() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String fileName = "foo" + File.separator + "bar.js";
    String js = "var dep = require('./baz'); function test() { var local = dep; return local; }";
    JSSourceFile input = JSSourceFile.fromCode(fileName, js);

    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{input}, options);
    Node root = compiler.parseInputs();
    Node scriptNode = root.getLastChild().getFirstChild();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "");
    pass.process(null, scriptNode);

    String generatedCode = compiler.toSource(scriptNode);
    assertTrue(generatedCode.contains("goog.require(\"module$foo$baz\")"));
    assertTrue(generatedCode.contains("dep$$module$foo$bar = module$foo$baz"));
    assertTrue(generatedCode.contains("var local = dep$$module$foo$bar"));
  }

  @Test
  public void testProcessWithoutReportingDependencies() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String fileName = "test.js";
    String js = "var req = require('./other'); exports.val = req;";
    JSSourceFile input = JSSourceFile.fromCode(fileName, js);

    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{input}, options);
    Node root = compiler.parseInputs();
    Node scriptNode = root.getLastChild().getFirstChild();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "", false);
    pass.process(null, scriptNode);

    assertNull(pass.getModule());
    String generatedCode = compiler.toSource(scriptNode);
    assertTrue(generatedCode.contains("goog.provide(\"module$test\")"));
    assertTrue(generatedCode.contains("goog.require(\"module$other\")"));
  }

  @Test
  public void testProcessMultipleScriptNodesThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile input1 = JSSourceFile.fromCode("mod1.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("mod2.js", "var b = 2;");

    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{input1, input2}, options);
    Node root = compiler.parseInputs();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "");
    try {
      pass.process(null, root.getLastChild());
      fail("Expected IllegalArgumentException when multiple script nodes are visited in single pass");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("ProcessCommonJSModules supports only one invocation"));
    }
  }

  @Test
  public void testGlobalVarAlreadyMatchingSuffixIsNotRenamed() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String fileName = "mod.js";
    String js = "var module$mod = 123;";
    JSSourceFile input = JSSourceFile.fromCode(fileName, js);

    compiler.init(new JSSourceFile[]{}, new JSSourceFile[]{input}, options);
    Node root = compiler.parseInputs();
    Node scriptNode = root.getLastChild().getFirstChild();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "");
    pass.process(null, scriptNode);

    String generatedCode = compiler.toSource(scriptNode);
    assertTrue(generatedCode.contains("goog.provide(\"module$mod\")"));
  }
}