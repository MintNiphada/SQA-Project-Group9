package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ProcessCommonJSModulesTest {

  @Test
  public void testToModuleNameSingleFile() {
    Assert.assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo"));
    Assert.assertEquals("module$foo", ProcessCommonJSModules.toModuleName("./foo"));
    Assert.assertEquals("module$foo", ProcessCommonJSModules.toModuleName("foo.js"));
    Assert.assertEquals("module$foo", ProcessCommonJSModules.toModuleName("./foo.js"));
  }

  @Test
  public void testToModuleNameComplexPath() {
    Assert.assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("foo/bar"));
    Assert.assertEquals("module$foo$bar", ProcessCommonJSModules.toModuleName("./foo/bar.js"));
    Assert.assertEquals("module$foo_bar$baz", ProcessCommonJSModules.toModuleName("foo-bar/baz.js"));
    Assert.assertEquals("module$nested$dir_name$my_module",
        ProcessCommonJSModules.toModuleName("nested/dir-name/my-module.js"));
  }

  @Test
  public void testToModuleNameWithCurrentFilenameRelative() {
    Assert.assertEquals("module$foo$bar",
        ProcessCommonJSModules.toModuleName("./bar", "foo/index.js"));
    Assert.assertEquals("module$foo$bar",
        ProcessCommonJSModules.toModuleName("./bar.js", "foo/index.js"));
    Assert.assertEquals("module$bar",
        ProcessCommonJSModules.toModuleName("../bar", "foo/index.js"));
    Assert.assertEquals("module$bar",
        ProcessCommonJSModules.toModuleName("../bar.js", "foo/index.js"));
    Assert.assertEquals("module$other$module",
        ProcessCommonJSModules.toModuleName("other/module", "foo/index.js"));
    Assert.assertEquals("module$other$module",
        ProcessCommonJSModules.toModuleName("other/module.js", "foo/index.js"));
  }

  @Test
  public void testToModuleNameInvalidUriThrowsException() {
    try {
      ProcessCommonJSModules.toModuleName("./test", "http://invalid URI with spaces");
      Assert.fail("Expected RuntimeException due to URISyntaxException");
    } catch (RuntimeException e) {
      Assert.assertNotNull(e.getCause());
    }
  }

  @Test
  public void testGuessCJSModuleNameAndPrefixNormalization() {
    Compiler compiler = new Compiler();
    ProcessCommonJSModules passWithSlash = new ProcessCommonJSModules(compiler, "app/src/");
    Assert.assertEquals("module$main", passWithSlash.guessCJSModuleName("app/src/main.js"));
    Assert.assertEquals("module$other$main", passWithSlash.guessCJSModuleName("other/main.js"));

    ProcessCommonJSModules passWithoutSlash = new ProcessCommonJSModules(compiler, "app/src");
    Assert.assertEquals("module$main", passWithoutSlash.guessCJSModuleName("app/src/main.js"));
    Assert.assertEquals("module$lib$util", passWithoutSlash.guessCJSModuleName("app/src/lib/util.js"));
  }

  @Test
  public void testProcessSimpleModuleWithDependencies() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var name = 42; exports.name = name;";
    SourceFile sourceFile = SourceFile.fromCode("app/test.js", js);
    CompilerInput input = new CompilerInput(sourceFile);

    Node scriptNode = compiler.parse(sourceFile);
    Node root = IR.block(scriptNode);
    Node externs = IR.block();

    List<CompilerInput> inputs = new ArrayList<CompilerInput>();
    inputs.add(input);
    Compiler.IntermediateState state = compiler.getState();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "app/", true);
    pass.process(externs, root);

    JSModule module = pass.getModule();
    Assert.assertNotNull(module);
    Assert.assertEquals("module$test", module.getName());
    Assert.assertTrue(input.getProvides().contains("module$test"));
  }

  @Test
  public void testProcessRequireCall() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var other = require('./other');";
    SourceFile sourceFile = SourceFile.fromCode("app/main.js", js);
    CompilerInput input = new CompilerInput(sourceFile);

    Node scriptNode = compiler.parse(sourceFile);
    Node root = IR.block(scriptNode);
    Node externs = IR.block();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "app/", true);
    pass.process(externs, root);

    Assert.assertTrue(input.getRequires().contains("module$other"));
  }

  @Test
  public void testProcessModuleExportsOverride() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "module.exports = function() { return 1; };";
    SourceFile sourceFile = SourceFile.fromCode("app/func.js", js);
    CompilerInput input = new CompilerInput(sourceFile);

    Node scriptNode = compiler.parse(sourceFile);
    Node root = IR.block(scriptNode);
    Node externs = IR.block();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "app/", false);
    pass.process(externs, root);

    Assert.assertNull(pass.getModule());
  }

  @Test
  public void testProcessMultipleScriptNodesInSingleInvocationFails() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    SourceFile sf1 = SourceFile.fromCode("app/first.js", "var a = 1;");
    SourceFile sf2 = SourceFile.fromCode("app/second.js", "var b = 2;");

    Node script1 = compiler.parse(sf1);
    Node script2 = compiler.parse(sf2);
    Node root = IR.block(script1, script2);
    Node externs = IR.block();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, "app/");
    try {
      pass.process(externs, root);
      Assert.fail("Expected IllegalArgumentException for multiple script nodes");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage().contains("supports only one invocation per"));
    }
  }

  @Test
  public void testSuffixVarsCallbackScoping() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var x = 10; function foo() { var x = 20; return x; } exports.x = x;";
    SourceFile sourceFile = SourceFile.fromCode("foo.js", js);

    Node scriptNode = compiler.parse(sourceFile);
    Node root = IR.block(scriptNode);
    Node externs = IR.block();

    ProcessCommonJSModules pass = new ProcessCommonJSModules(compiler, ".");
    pass.process(externs, root);
  }
}