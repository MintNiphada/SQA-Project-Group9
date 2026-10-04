package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.CompilerOptions.TracerMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public class CompilerTest {

  @Test
  public void testConstructorsAndErrorManager() {
    Compiler compiler1 = new Compiler();
    Assert.assertNotNull(compiler1.getErrorManager());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    Compiler compiler2 = new Compiler(ps);
    Assert.assertNotNull(compiler2.getErrorManager());

    BasicErrorManager customManager = new PrintStreamErrorManager(ps);
    Compiler compiler3 = new Compiler(customManager);
    Assert.assertSame(customManager, compiler3.getErrorManager());
  }

  @Test(expected = NullPointerException.class)
  public void testSetErrorManagerNull() {
    Compiler compiler = new Compiler();
    compiler.setErrorManager(null);
  }

  @Test
  public void testInitOptionsAndFlags() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = false;
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    options.checkSymbols = false;
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    options.setLanguageOut(LanguageMode.ECMASCRIPT5_STRICT);
    options.acceptConstKeyword = true;

    compiler.initOptions(options);

    Assert.assertTrue(compiler.acceptEcmaScript5());
    Assert.assertEquals(LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
    Assert.assertTrue(compiler.acceptConstKeyword());
    Assert.assertFalse(compiler.isTypeCheckingEnabled());
    Assert.assertNotNull(compiler.getParserConfig());
    Assert.assertNotNull(compiler.getCodingConvention());
    Assert.assertFalse(compiler.isIdeMode());
  }

  @Test
  public void testInitOptionsCheckTypesOverride() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
    compiler.initOptions(options);
    Assert.assertTrue(compiler.isTypeCheckingEnabled());

    Compiler compiler2 = new Compiler();
    CompilerOptions options2 = new CompilerOptions();
    options2.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
    compiler2.initOptions(options2);
    Assert.assertFalse(compiler2.isTypeCheckingEnabled());
  }

  @Test
  public void testCodeBuilderMethods() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(0, cb.getLineIndex());
    Assert.assertEquals(0, cb.getColumnIndex());

    cb.append("var a = 10;\nvar b = 20;");
    Assert.assertEquals(1, cb.getLineIndex());
    Assert.assertEquals(11, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith("20;"));
    Assert.assertFalse(cb.endsWith("var"));
    Assert.assertEquals("var a = 10;\nvar b = 20;", cb.toString());

    cb.reset();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(1, cb.getLineIndex());
  }

  @Test
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();
    Node n1 = compiler.parseSyntheticCode("var x = 1;");
    Assert.assertNotNull(n1);
    Assert.assertEquals(Token.SCRIPT, n1.getType());

    Node n2 = compiler.parseSyntheticCode("file.js", "var y = 2;");
    Assert.assertNotNull(n2);

    Node n3 = compiler.parseTestCode("function foo() { return 3; }");
    Assert.assertNotNull(n3);
    Assert.assertNotNull(compiler.getInput(" [testcode] "));
  }

  @Test
  public void testBasicCompile() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var customExtern;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1 + 2;");

    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertEquals(0, result.errors.length);
    String source = compiler.toSource();
    Assert.assertNotNull(source);
    Assert.assertTrue(source.contains("var a=3") || source.contains("var a = 3") || source.contains("var a"));

    String[] sourceArray = compiler.toSourceArray();
    Assert.assertEquals(1, sourceArray.length);
  }

  @Test
  public void testCompileModules() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var y = x + 1;"));
    m2.addDependency(m1);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    Result result = compiler.compile(extern, new JSModule[] { m1, m2 }, options);
    Assert.assertTrue(result.success);

    String m1Source = compiler.toSource(m1);
    Assert.assertNotNull(m1Source);
    String[] m2Array = compiler.toSourceArray(m2);
    Assert.assertEquals(1, m2Array.length);

    Node node = compiler.getNodeForCodeInsertion(m1);
    Assert.assertNotNull(node);
  }

  @Test
  public void testModuleDependencyError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var y = 2;"));
    m1.addDependency(m2);

    Result result = compiler.compile(
        new JSSourceFile[] { JSSourceFile.fromCode("ext.js", "") },
        new JSModule[] { m1, m2 },
        options);
    Assert.assertFalse(result.success);
    Assert.assertTrue(compiler.getErrorCount() > 0);
  }

  @Test
  public void testDuplicateInputs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile f1 = JSSourceFile.fromCode("file.js", "var x = 1;");
    JSSourceFile f2 = JSSourceFile.fromCode("file.js", "var y = 2;");

    Result result = compiler.compile(
        new JSSourceFile[] { JSSourceFile.fromCode("ext.js", "") },
        new JSSourceFile[] { f1, f2 },
        options);
    Assert.assertFalse(result.success);
    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testEmptyModuleListAndEmptyRoot() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    compiler.initModules(
        Collections.<JSSourceFile>emptyList(),
        Collections.<JSModule>emptyList(),
        options);
    Assert.assertEquals(1, compiler.getErrorCount());

    Compiler compiler2 = new Compiler();
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var a = 1;"));
    List<JSModule> modules = new ArrayList<JSModule>();
    modules.add(m1);
    modules.add(m2);

    compiler2.initModules(
        Collections.<JSSourceFile>emptyList(),
        modules,
        options);
    Assert.assertEquals(1, compiler2.getErrorCount());
  }

  @Test
  public void testInputManagementAndDynamicSourceAst() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("extern.js", "var e;") },
        new JSSourceFile[] { JSSourceFile.fromCode("src.js", "var a = 1;") },
        options);

    Assert.assertNotNull(compiler.getInput("src.js"));
    Assert.assertNotNull(compiler.getInput("extern.js"));
    Assert.assertNull(compiler.getInput("unknown.js"));

    compiler.parseInputs();

    CompilerInput newExt = compiler.newExternInput("synthetic_extern.js");
    Assert.assertNotNull(newExt);
    Assert.assertNotNull(compiler.getInput("synthetic_extern.js"));

    JsAst ast = new JsAst(JSSourceFile.fromCode("src2.js", "var b = 2;"));
    compiler.addIncrementalSourceAst(ast);
    Assert.assertNotNull(compiler.getInput("src2.js"));

    JsAst astReplace = new JsAst(JSSourceFile.fromCode("src.js", "var a = 3;"));
    boolean replaced = compiler.replaceIncrementalSourceAst(astReplace);
    Assert.assertTrue(replaced);

    compiler.removeInput("src2.js");
    Assert.assertNull(compiler.getInput("src2.js"));
    compiler.removeInput("nonexistent.js");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInputDuplicate() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("extern.js", "var e;") },
        new JSSourceFile[] { JSSourceFile.fromCode("src.js", "var a = 1;") },
        options);
    compiler.parseInputs();
    compiler.newExternInput("extern.js");
  }

  @Test
  public void testNodeComparisonsAndUniqueIds() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node n1 = Node.newString("a");
    Node n2 = Node.newString("a");
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = true;
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    Assert.assertEquals("0", supplier.get());
    Assert.assertEquals("1", supplier.get());
    compiler.resetUniqueNameId();
    Assert.assertEquals("0", supplier.get());
  }

  @Test
  public void testTypeRegistryAndHelpers() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    JSTypeRegistry registry = compiler.getTypeRegistry();
    Assert.assertNotNull(registry);
    Assert.assertSame(registry, compiler.getTypeRegistry());

    Assert.assertNotNull(compiler.getReverseAbstractInterpreter());
    Assert.assertNotNull(compiler.getTypeValidator());
    Assert.assertNotNull(compiler.getDefaultErrorReporter());
    Assert.assertNull(compiler.getModuleGraph());
  }

  @Test
  public void testSourceLineAndRegion() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] {},
        new JSSourceFile[] { JSSourceFile.fromCode("test.js", "line1\nline2\nline3\n") },
        options);

    Assert.assertNull(compiler.getSourceLine("test.js", 0));
    Assert.assertNull(compiler.getSourceLine("unknown.js", 1));
    Assert.assertEquals("line2", compiler.getSourceLine("test.js", 2));

    Assert.assertNull(compiler.getSourceRegion("test.js", -1));
    Assert.assertNull(compiler.getSourceRegion("unknown.js", 1));
    Region region = compiler.getSourceRegion("test.js", 2);
    Assert.assertNotNull(region);
  }

  @Test
  public void testCodeChangeHandlers() {
    Compiler compiler = new Compiler();
    final boolean[] changed = new boolean[1];
    CodeChangeHandler handler = new CodeChangeHandler() {
      public void reportChange() {
        changed[0] = true;
      }
    };
    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    Assert.assertTrue(changed[0]);

    changed[0] = false;
    compiler.removeChangeHandler(handler);
    compiler.reportCodeChange();
    Assert.assertFalse(changed[0]);
  }

  @Test
  public void testErrorReportingAndGuards() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    JSError error = JSError.make("test.js", 1, 1, CheckLevel.ERROR, Compiler.OPTIMIZE_LOOP_ERROR, "100");
    compiler.report(error);
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertTrue(compiler.hasErrors());
    Assert.assertEquals(1, compiler.getErrors().length);
    Assert.assertEquals(1, compiler.getMessages().length);
    Assert.assertEquals(0, compiler.getWarningCount());
    Assert.assertEquals(0, compiler.getWarnings().length);

    Assert.assertEquals(CheckLevel.ERROR, compiler.getErrorLevel(error));

    compiler.addToDebugLog("Debug message");
  }

  @Test
  public void testRunCallableLargeStack() {
    String res = Compiler.runCallableWithLargeStack(new Callable<String>() {
      public String call() {
        return "success";
      }
    });
    Assert.assertEquals("success", res);
  }

  @Test(expected = RuntimeException.class)
  public void testRunCallableException() {
    Compiler.runCallableWithLargeStack(new Callable<Void>() {
      public Void call() throws Exception {
        throw new IOException("fail");
      }
    });
  }

  @Test(expected = RuntimeException.class)
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    compiler.throwInternalError("Error", new IllegalStateException("cause"));
  }

  @Test
  public void testStateSaveAndRestore() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("extern.js", "") },
        new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var a = 1;") },
        options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    Assert.assertNotNull(state);

    Compiler compiler2 = new Compiler();
    compiler2.init(
        new JSSourceFile[] { JSSourceFile.fromCode("extern.js", "") },
        new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var a = 1;") },
        options);
    compiler2.setState(state);
    Assert.assertNotNull(compiler2.getRoot());
    Assert.assertEquals(1, compiler2.getInputsForTesting().size());
    Assert.assertEquals(1, compiler2.getExternsForTesting().size());
  }

  @Test
  public void testPassConfigManagement() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    PassConfig config = compiler.getPassConfig();
    Assert.assertNotNull(config);

    Compiler compiler2 = new Compiler();
    compiler2.initOptions(options);
    DefaultPassConfig newConfig = new DefaultPassConfig(options);
    compiler2.setPassConfig(newConfig);
    Assert.assertSame(newConfig, compiler2.getPassConfig());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPassConfigTwice() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    compiler.setPassConfig(new DefaultPassConfig(options));
    compiler.setPassConfig(new DefaultPassConfig(options));
  }

  @Test(expected = NullPointerException.class)
  public void testSetPassConfigNull() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(null);
  }

  @Test
  public void testCssRenamingMapAndRegExpFlag() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    CssRenamingMap map = new CssRenamingMap() {
      public String get(String value) {
        return "a";
      }
      public Style getStyle() {
        return Style.BY_WHOLE;
      }
    };
    compiler.setCssRenamingMap(map);
    Assert.assertSame(map, compiler.getCssRenamingMap());

    Assert.assertTrue(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    Assert.assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test
  public void testAstDotGraphAndCFG() throws IOException {
    Compiler compiler = new Compiler();
    Assert.assertEquals("", compiler.getAstDotGraph());

    CompilerOptions options = new CompilerOptions();
    compiler.compile(
        JSSourceFile.fromCode("ext.js", ""),
        JSSourceFile.fromCode("in.js", "function foo() { return 1; } foo();"),
        options);

    String dot = compiler.getAstDotGraph();
    Assert.assertNotNull(dot);
    Assert.assertTrue(dot.contains("digraph"));

    ControlFlowGraph<Node> cfg = compiler.computeCFG();
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testManageClosureDependencies() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.manageClosureDependencies = true;
    options.manageClosureDependenciesEntryPoints = Collections.singletonList("app");

    JSSourceFile f1 = JSSourceFile.fromCode("lib.js", "goog.provide('lib'); var lib = 1;");
    JSSourceFile f2 = JSSourceFile.fromCode("app.js", "goog.provide('app'); goog.require('lib'); var app = lib;");
    JSSourceFile ext = JSSourceFile.fromCode("ext.js", "var goog = {}; goog.provide = function(x){}; goog.require = function(x){};");

    Result result = compiler.compile(
        new JSSourceFile[] { ext },
        new JSSourceFile[] { f1, f2 },
        options);
    Assert.assertTrue(result.success);
  }

  @Test
  public void testManageClosureDependenciesMissingProvide() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.manageClosureDependencies = true;
    options.manageClosureDependenciesEntryPoints = Collections.singletonList("missing");

    JSSourceFile f1 = JSSourceFile.fromCode("app.js", "var app = 1;");
    JSSourceFile ext = JSSourceFile.fromCode("ext.js", "");

    Result result = compiler.compile(
        new JSSourceFile[] { ext },
        new JSSourceFile[] { f1 },
        options);
    Assert.assertFalse(result.success);
    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testJSDocExternAndNoCompile() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile f1 = JSSourceFile.fromCode("ext_in_src.js", "/** @externs */ var externVar;");
    JSSourceFile f2 = JSSourceFile.fromCode("nocompile.js", "/** @nocompile */ var ignored;");
    JSSourceFile f3 = JSSourceFile.fromCode("app.js", "var normal = 1;");
    JSSourceFile ext = JSSourceFile.fromCode("ext.js", "");

    Result result = compiler.compile(
        new JSSourceFile[] { ext },
        new JSSourceFile[] { f1, f2, f3 },
        options);
    Assert.assertTrue(result.success);
    Assert.assertEquals(1, compiler.getInputsForTesting().size());
    Assert.assertEquals(2, compiler.getExternsForTesting().size());
  }

  @Test
  public void testStripCodeAndRemoveTryCatch() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.stripTypes = Collections.singleton("debug.Type");
    options.removeTryCatchFinally = true;

    JSSourceFile ext = JSSourceFile.fromCode("ext.js", "");
    JSSourceFile in = JSSourceFile.fromCode("in.js", "try { var debug_Type = 1; } catch(e) {}");

    Result result = compiler.compile(ext, in, options);
    Assert.assertTrue(result.success);
  }

  @Test
  public void testPerformanceTrackerAndTracers() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.tracer = TracerMode.ALL;

    JSSourceFile ext = JSSourceFile.fromCode("ext.js", "");
    JSSourceFile in = JSSourceFile.fromCode("in.js", "var a = 1;");

    Result result = compiler.compile(ext, in, options);
    Assert.assertTrue(result.success);
    Assert.assertNotNull(compiler.tracker);
  }

  @Test
  public void testLoggingLevel() {
    Compiler.setLoggingLevel(Level.OFF);
    Compiler.setLoggingLevel(Level.INFO);
  }

  @Test
  public void testGlobalVarReferences() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("ext.js", "") },
        new JSSourceFile[] { JSSourceFile.fromCode("in.js", "var a = 1;") },
        options);
    compiler.parseInputs();

    Assert.assertNull(compiler.getGlobalVarReferences());
    compiler.updateGlobalVarReferences(new HashMap<Scope.Var, ReferenceCollectingCallback.ReferenceCollection>(), compiler.jsRoot);
    Assert.assertNotNull(compiler.getGlobalVarReferences());
  }
}
