package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

@RunWith(JUnit4.class)
public class ProcessCommonJSModulesTest extends CompilerTestCase {

  private String filenamePrefix = ProcessCommonJSModules.DEFAULT_FILENAME_PREFIX;
  private boolean reportDependencies = true;
  private ProcessCommonJSModules lastPass;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    lastPass = null;
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    lastPass = new ProcessCommonJSModules(compiler, filenamePrefix, reportDependencies);
    return lastPass;
  }

  @Test
  public void testBasicModule() {
    test("var x = 1;",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "var x$$module$test = 1;" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testRequire() {
    test("var foo = require('bar');",
        "goog.require('module$bar');" +
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "var foo$$module$test = module$bar;" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testModuleExports() {
    test("module.exports = {};",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "module$test.module$exports = {};" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testRelativeRequire() {
    test(SourceFile.fromCode("dir/test.js", "var foo = require('./bar');"),
        "goog.require('module$dir$bar');" +
        "goog.provide('module$dir$test');" +
        "var module$dir$test = {};" +
        "var foo$$module$dir$test = module$dir$bar;" +
        "if(module$dir$test.module$exports) module$dir$test = module$dir$test.module$exports;");
  }

  @Test
  public void testRelativeRequireParent() {
    test(SourceFile.fromCode("dir/sub/test.js", "var foo = require('../bar');"),
        "goog.require('module$dir$bar');" +
        "goog.provide('module$dir$sub$test');" +
        "var module$dir$sub$test = {};" +
        "var foo$$module$dir$sub$test = module$dir$bar;" +
        "if(module$dir$sub$test.module$exports) module$dir$sub$test = module$dir$sub$test.module$exports;");
  }

  @Test
  public void testNoReportDependencies() {
    reportDependencies = false;
    test("var x = 1;",
        "var module$test = {};" +
        "var x$$module$test = 1;" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
    assertNull(lastPass.getModule());
  }

  @Test
  public void testMultipleScriptsThrows() {
    try {
      Compiler compiler = new Compiler();
      compiler.initOptions(new CompilerOptions());
      Node root = new Node(Token.BLOCK);
      root.addChildToBack(new Node(Token.SCRIPT));
      root.addChildToBack(new Node(Token.SCRIPT));
      ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, filenamePrefix, reportDependencies);
      pass.process(null, root);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("supports only one invocation"));
    }
  }

  @Test
  public void testToModuleName() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo.js"));
    assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("foo/bar.js"));
    assertEquals("module$foo_bar", ProcessCommonJSModules.toModuleName("foo-bar.js"));
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("./foo.js"));
    assertEquals("module$dir$foo", ProcessCommonJSModules.toModuleName("dir/foo.js"));
    assertEquals("module$dir$foo", ProcessCommonJSModules.toModuleName("./dir/foo.js"));
    assertEquals("module$dir$bar", ProcessCommonJSModules.toModuleName("./bar", "dir/foo.js"));
    assertEquals("module$bar", ProcessCommonJSModules.toModuleName("../bar", "dir/foo.js"));
    assertEquals("module$dir$bar", ProcessCommonJSModules.toModuleName("./bar", "dir/foo"));
    assertEquals("module$bar", ProcessCommonJSModules.toModuleName("../bar", "dir/foo"));
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo", "foo.js"));
  }

  @Test
  public void testGuessCJSModuleName() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(new Compiler(), "prefix/");
    assertEquals("module$test", pass.guessCJSModuleName("prefix/test.js"));
    assertEquals("module$test", pass.guessCJSModuleName("prefix/test"));
    pass = new ProcessCommonJSModules(new Compiler(), ProcessCommonJSModules.DEFAULT_FILENAME_PREFIX);
    assertEquals("module$test", pass.guessCJSModuleName("./test.js"));
    assertEquals("module$test", pass.guessCJSModuleName("test.js"));
  }

  @Test
  public void testGetModule() {
    test("var x = 1;", "");
    assertNotNull(lastPass.getModule());
    assertEquals("module$test", lastPass.getModule().getName());
  }

  @Test
  public void testSuffixVarsExportsGlobal() {
    test("var exports = {};",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "var module$test = {};" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testSuffixVarsGlobal() {
    test("var global = 1;",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "var global$$module$test = 1;" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testSuffixVarsLocalNotRenamed() {
    test("function f() { var local = 1; }",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "function f() { var local$$module$test = 1; }" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testModuleNameNotRenamed() {
    test("var module$test = 1;",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "var module$test = 1;" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testExportsLocalRenamed() {
    test("function f() { var exports = {}; }",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "function f() { var module$test = {}; }" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testConstructorFilenamePrefixWithoutSeparator() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(new Compiler(), "prefix");
    assertEquals("module$test", pass.guessCJSModuleName("prefix" + File.separator + "test.js"));
  }

  @Test
  public void testToModuleNameWithHyphen() {
    assertEquals("module$foo_bar_baz", ProcessCommonJSModules.toModuleName("foo-bar-baz.js"));
  }

  @Test
  public void testToModuleNameNoJsExtension() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo"));
  }

  @Test
  public void testToModuleNameLeadingDotSlash() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("./foo"));
  }

  @Test
  public void testToModuleNameRelativeWithCurrent() {
    assertEquals("module$dir$bar", ProcessCommonJSModules.toModuleName("./bar", "dir/foo.js"));
    assertEquals("module$bar", ProcessCommonJSModules.toModuleName("../bar", "dir/foo.js"));
    assertEquals("module$dir$sub$bar", ProcessCommonJSModules.toModuleName("./sub/bar", "dir/foo.js"));
  }

  @Test
  public void testOptionalExportsOverride() {
    test("module.exports = 1;",
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "module$test.module$exports = 1;" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testRequireInsideFunction() {
    test("function f() { var x = require('bar'); }",
        "goog.require('module$bar');" +
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "function f() { var x$$module$test = module$bar; }" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testMultipleRequires() {
    test("var a = require('foo'); var b = require('bar');",
        "goog.require('module$foo');" +
        "goog.require('module$bar');" +
        "goog.provide('module$test');" +
        "var module$test = {};" +
        "var a$$module$test = module$foo;" +
        "var b$$module$test = module$bar;" +
        "if(module$test.module$exports) module$test = module$test.module$exports;");
  }

  @Test
  public void testNormalizeSourceName() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(new Compiler(), "prefix/");
    assertEquals("test.js", pass.guessCJSModuleName("prefix/test.js"));
    assertEquals("test", pass.guessCJSModuleName("prefix/test"));
    assertEquals("dir/test.js", pass.guessCJSModuleName("prefix/dir/test.js"));
  }
}
