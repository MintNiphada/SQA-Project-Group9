package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class ProcessCommonJSModulesTest {

  @Test
  public void testToModuleNameSimple() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo.js"));
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo"));
  }

  @Test
  public void testToModuleNameWithSlash() {
    assertEquals("module$path$to$foo", ProcessCommonJSModules.toModuleName("path/to/foo.js"));
  }

  @Test
  public void testToModuleNameWithLeadingDotSlash() {
    assertEquals("module$foo", ProcessCommonJSModules.toModuleName("./foo.js"));
  }

  @Test
  public void testToModuleNameWithHyphen() {
    assertEquals("module$foo_bar", ProcessCommonJSModules.toModuleName("foo-bar.js"));
  }

  @Test
  public void testToModuleNameWithMultipleDots() {
    assertEquals("module$foo.bar", ProcessCommonJSModules.toModuleName("foo.bar.js"));
  }

  @Test
  public void testToModuleNameRelativeCurrentDir() {
    assertEquals("module$dir$foo",
        ProcessCommonJSModules.toModuleName("./foo", "dir/current.js"));
  }

  @Test
  public void testToModuleNameRelativeParentDir() {
    assertEquals("module$foo",
        ProcessCommonJSModules.toModuleName("../foo", "dir/current.js"));
  }

  @Test
  public void testToModuleNameRelativeNoDotSlash() {
    assertEquals("module$bar",
        ProcessCommonJSModules.toModuleName("bar", "dir/current.js"));
  }

  @Test
  public void testToModuleNameRelativeWithJsExtension() {
    assertEquals("module$dir$foo",
        ProcessCommonJSModules.toModuleName("./foo.js", "dir/current.js"));
  }

  @Test(expected = RuntimeException.class)
  public void testToModuleNameRelativeInvalidUri() {
    ProcessCommonJSModules.toModuleName("./foo", "invalid current");
  }

  @Test
  public void testGuessCJSModuleName() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(new Compiler(), "./");
    assertEquals("module$foo", pass.guessCJSModuleName("./foo.js"));
    assertEquals("module$bar", pass.guessCJSModuleName("bar.js"));
  }

  @Test
  public void testGuessCJSModuleNameWithPrefix() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(new Compiler(), "src/");
    assertEquals("module$foo", pass.guessCJSModuleName("src/foo.js"));
    assertEquals("module$bar", pass.guessCJSModuleName("src/bar"));
  }

  @Test
  public void testConstructorFilenamePrefixEndsWithSlash() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(new Compiler(), "prefix/");
    assertEquals("module$foo", pass.guessCJSModuleName("prefix/foo.js"));
  }

  @Test
  public void testConstructorFilenamePrefixDoesNotEndWithSlash() {
    ProcessCommonJSModules pass = new ProcessCommonJSModules(new Compiler(), "prefix");
    assertEquals("module$foo", pass.guessCJSModuleName("prefix/foo.js"));
  }

  @Test
  public void testProcessBasic() {
    String source = "var a = 1; require('bar'); module.exports = a;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "goog.require('module$bar');"
        + "var a$$module$test = 1;"
        + "module$bar;"
        + "module$test.module$exports = a$$module$test;"
        + "if(module$test.module$exports) module$test = module$test.module$exports;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testProcessWithoutReportDependencies() {
    String source = "var a = 1; require('bar'); module.exports = a;";
    String expected = ""
        + "var module$test = {};"
        + "goog.require('module$bar');"
        + "var a$$module$test = 1;"
        + "module$bar;"
        + "module$test.module$exports = a$$module$test;"
        + "if(module$test.module$exports) module$test = module$test.module$exports;";
    testProcess(source, "test.js", expected, false);
  }

  @Test
  public void testProcessExportsVariable() {
    String source = "exports = 5;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "module$test = 5;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testProcessGlobalVarSuffixed() {
    String source = "var x = 1;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "var x$$module$test = 1;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testProcessNoModuleExports() {
    String source = "var a = 1;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "var a$$module$test = 1;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testProcessMultipleRequires() {
    String source = "require('a'); require('b');";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "goog.require('module$b');"
        + "goog.require('module$a');"
        + "module$a;"
        + "module$b;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testProcessRequireWithRelativePath() {
    String source = "require('./bar');";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "goog.require('module$test$bar');"
        + "module$test$bar;";
    testProcess(source, "dir/test.js", expected, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProcessMultipleScriptNodesThrows() {
    Compiler compiler = new Compiler();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs", "")),
        Lists.newArrayList(
            SourceFile.fromCode("test1.js", "var a=1;"),
            SourceFile.fromCode("test2.js", "var b=2;")),
        new CompilerOptions());
    compiler.parse();
    Node root = compiler.getRoot();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./");
    pass.process(null, root);
  }

  @Test
  public void testGetModuleWhenReportDependenciesTrue() {
    Compiler compiler = new Compiler();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs", "")),
        Collections.singletonList(SourceFile.fromCode("test.js", "var a=1;")),
        new CompilerOptions());
    compiler.parse();
    Node root = compiler.getRoot();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", true);
    pass.process(null, root);
    assertNotNull(pass.getModule());
    assertEquals("module$test", pass.getModule().getName());
  }

  @Test
  public void testGetModuleWhenReportDependenciesFalse() {
    Compiler compiler = new Compiler();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs", "")),
        Collections.singletonList(SourceFile.fromCode("test.js", "var a=1;")),
        new CompilerOptions());
    compiler.parse();
    Node root = compiler.getRoot();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", false);
    pass.process(null, root);
    assertNull(pass.getModule());
  }

  @Test
  public void testSuffixVarsCallbackSkipsSuffixName() {
    String source = "var module$test = 1;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "var module$test = 1;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testSuffixVarsCallbackRenamesExports() {
    String source = "exports.foo = 1;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "module$test.foo = 1;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testSuffixVarsCallbackRenamesGlobalVar() {
    String source = "var global = 1; function f() { var local = 2; }";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "var global$$module$test = 1;"
        + "function f$$module$test() { var local = 2; }";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testModuleExportsOverrideAdded() {
    String source = "module.exports = 1;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "module$test.module$exports = 1;"
        + "if(module$test.module$exports) module$test = module$test.module$exports;";
    testProcess(source, "test.js", expected, true);
  }

  @Test
  public void testModuleExportsOverrideNotAddedWithoutExports() {
    String source = "var a = 1;";
    String expected = ""
        + "var module$test = {};"
        + "goog.provide('module$test');"
        + "var a$$module$test = 1;";
    testProcess(source, "test.js", expected, true);
  }

  private void testProcess(String source, String filename, String expected, boolean reportDeps) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs", "")),
        Collections.singletonList(SourceFile.fromCode(filename, source)),
        options);
    compiler.parse();
    Node root = compiler.getRoot();
    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "./", reportDeps);
    pass.process(null, root);
    String result = compiler.toSource();
    assertEquals(expected, result);
  }
}
