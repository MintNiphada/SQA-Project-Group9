package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.CompilerOptions.TracerMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  @Test
  public void testConstructors() {
    Compiler c1 = new Compiler();
    Assert.assertNotNull(c1);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    Compiler c2 = new Compiler(ps);
    Assert.assertNotNull(c2);

    BasicErrorManager errorManager = new PrintStreamErrorManager(ps);
    Compiler c3 = new Compiler(errorManager);
    Assert.assertEquals(errorManager, c3.getErrorManager());
  }

  @Test(expected = NullPointerException.class)
  public void testSetErrorManagerNull() {
    compiler.setErrorManager(null);
  }

  @Test
  public void testInitOptions() {
    options.checkTypes = false;
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);

    Assert.assertNotNull(compiler.getErrorManager());
    Assert.assertEquals(CheckLevel.WARNING, options.checkGlobalThisLevel);
    Assert.assertTrue(compiler.acceptEcmaScript5());
    Assert.assertEquals(LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
  }

  @Test
  public void testInitOptionsDiagnosticGroups() {
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
    Assert.assertTrue(options.checkTypes);

    Compiler c2 = new Compiler();
    CompilerOptions o2 = new CompilerOptions();
    o2.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
    c2.initOptions(o2);
    Assert.assertFalse(o2.checkTypes);
  }

  @Test
  public void testInitOptionsWithPrintStream() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Compiler c = new Compiler(new PrintStream(out));
    CompilerOptions opt = new CompilerOptions();
    opt.summaryDetailLevel = 0;
    c.initOptions(opt);
    Assert.assertNotNull(c.getErrorManager());
  }

  @Test
  public void testCompileSimple() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var window;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1 + 2;");
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    options.setLanguageOut(LanguageMode.ECMASCRIPT3);
    Result result = compiler.compile(extern, input, options);

    Assert.assertTrue(result.success);
    Assert.assertEquals(0, result.errors.length);
    Assert.assertEquals(0, result.warnings.length);
    String source = compiler.toSource();
    Assert.assertTrue(source.contains("var a=3") || source.contains("var a = 3") || source.contains("var a"));
  }

  @Test
  public void testCompileArray() {
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("extern.js", "function alert(x) {}")
    };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("in1.js", "var x = 1;"),
        JSSourceFile.fromCode("in2.js", "alert(x);")
    };
    Result result = compiler.compile(externs, inputs, options);
    Assert.assertTrue(result.success);
    String[] sources = compiler.toSourceArray();
    Assert.assertEquals(2, sources.length);
  }

  @Test
  public void testCompileModules() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var m1_var = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var m2_var = 2;"));
    m2.addDependency(m1);

    JSModule[] modules = new JSModule[] { m1, m2 };
    Result result = compiler.compile(extern, modules, options);
    Assert.assertTrue(result.success);

    String m1Source = compiler.toSource(m1);
    Assert.assertTrue(m1Source.contains("m1_var"));

    String[] m2Array = compiler.toSourceArray(m2);
    Assert.assertEquals(1, m2Array.length);
    Assert.assertTrue(m2Array[0].contains("m2_var"));
  }

  @Test
  public void testModuleDependencyError() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));
    m1.addDependency(m2); // m1 depends on m2, but m1 is listed first

    List<JSSourceFile> externs = Lists.newArrayList(extern);
    List<JSModule> modules = Lists.newArrayList(m1, m2);
    Result result = compiler.compileModules(externs, modules, options);
    Assert.assertFalse(result.success);
    Assert.assertTrue(compiler.hasErrors());
    Assert.assertTrue(compiler.getErrorCount() > 0);
  }

  @Test
  public void testEmptyModuleListError() {
    List<JSSourceFile> externs = Lists.newArrayList();
    List<JSModule> modules = Lists.newArrayList();
    Result result = compiler.compileModules(externs, modules, options);
    Assert.assertFalse(result.success);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testEmptyRootModuleError() {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var x = 1;"));
    m2.addDependency(m1);

    List<JSSourceFile> externs = Lists.newArrayList();
    List<JSModule> modules = Lists.newArrayList(m1, m2);
    Result result = compiler.compileModules(externs, modules, options);
    Assert.assertFalse(result.success);
  }

  @Test
  public void testDuplicateInputs() {
    JSSourceFile extern1 = JSSourceFile.fromCode("common.js", "var ext1;");
    JSSourceFile extern2 = JSSourceFile.fromCode("common.js", "var ext2;");
    JSSourceFile in1 = JSSourceFile.fromCode("test.js", "var a = 1;");
    JSSourceFile in2 = JSSourceFile.fromCode("test.js", "var b = 2;");

    compiler.init(new JSSourceFile[] { extern1, extern2 },
                  new JSSourceFile[] { in1, in2 }, options);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test(expected = IllegalStateException.class)
  public void testMultipleCompilationsForbidden() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.compile(extern, input, options);
    compiler.compile(extern, input, options);
  }

  @Test
  public void testCodeBuilder() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(0, cb.getLineIndex());
    Assert.assertEquals(0, cb.getColumnIndex());
    Assert.assertEquals("", cb.toString());

    cb.append("hello\nworld");
    Assert.assertEquals(11, cb.getLength());
    Assert.assertEquals(1, cb.getLineIndex());
    Assert.assertEquals(5, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith("world"));
    Assert.assertFalse(cb.endsWith("hello"));
    Assert.assertEquals("hello\nworld", cb.toString());

    cb.reset();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(1, cb.getLineIndex());
    Assert.assertEquals("", cb.toString());

    cb.append("single_line");
    Assert.assertEquals(1, cb.getLineIndex());
    Assert.assertEquals(11, cb.getColumnIndex());
  }

  @Test
  public void testToSourceWithDelimitersAndLicense() {
    options.printInputDelimiter = true;
    options.inputDelimiter = "// [%name% - %num%]";
    compiler.initOptions(options);

    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Node script = new Node(Token.SCRIPT);
    script.setSourceFileName("test_file.js");
    com.google.javascript.rhino.JSDocInfoBuilder doc = new com.google.javascript.rhino.JSDocInfoBuilder(true);
    doc.recordLicense("Sample License");
    script.setJSDocInfo(doc.build(script));

    compiler.toSource(cb, 1, script);
    String code = cb.toString();
    Assert.assertTrue(code.contains("// [test_file.js - 1]"));
    Assert.assertTrue(code.contains("Sample License"));
  }

  @Test
  public void testParseSyntheticAndTestCode() {
    compiler.initCompilerOptionsIfTesting();
    Node n1 = compiler.parseSyntheticCode("var x = 1;");
    Assert.assertNotNull(n1);
    Assert.assertEquals(Token.SCRIPT, n1.getType());

    Node n2 = compiler.parseSyntheticCode("custom.js", "var y = 2;");
    Assert.assertNotNull(n2);
    Assert.assertEquals("custom.js", n2.getSourceFileName());

    Node n3 = compiler.parseTestCode("var z = 3;");
    Assert.assertNotNull(n3);
    Assert.assertEquals(" [testcode] ", n3.getSourceFileName());
  }

  @Test
  public void testUniqueNameIdSupplier() {
    Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    Assert.assertEquals("0", supplier.get());
    Assert.assertEquals("1", supplier.get());
    compiler.resetUniqueNameId();
    Assert.assertEquals("0", supplier.get());
  }

  @Test
  public void testAreNodesEqualForInlining() {
    Node n1 = Node.newString("a");
    Node n2 = Node.newString("a");
    Node n3 = Node.newString("b");

    options.ambiguateProperties = false;
    options.disambiguateProperties = false;
    compiler.initOptions(options);
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    Assert.assertFalse(compiler.areNodesEqualForInlining(n1, n3));

    options.ambiguateProperties = true;
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
  }

  @Test
  public void testInputManagement() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var ext;");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);

    Assert.assertNotNull(compiler.getInput("extern.js"));
    Assert.assertNotNull(compiler.getInput("in.js"));
    Assert.assertNull(compiler.getInput("nonexistent.js"));

    Assert.assertEquals(1, compiler.getInputsInOrder().size());
    Assert.assertEquals(1, compiler.getExternsInOrder().size());

    CompilerInput newExt = compiler.newExternInput("synthetic_extern.js");
    Assert.assertNotNull(newExt);
    Assert.assertEquals(2, compiler.getExternsInOrder().size());

    compiler.removeExternInput("synthetic_extern.js");
    Assert.assertEquals(1, compiler.getExternsInOrder().size());
    compiler.removeExternInput("nonexistent.js"); // Should not throw
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInputConflict() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var ext;");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.newExternInput("extern.js");
  }

  @Test
  public void testAddAndReplaceIncrementalSourceAst() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    JsAst newAst = new JsAst(JSSourceFile.fromCode("in2.js", "var b = 2;"));
    compiler.addIncrementalSourceAst(newAst);
    Assert.assertNotNull(compiler.getInput("in2.js"));

    JsAst replacementAst = new JsAst(JSSourceFile.fromCode("in.js", "var a = 100;"));
    boolean replaced = compiler.replaceIncrementalSourceAst(replacementAst);
    Assert.assertTrue(replaced);
  }

  @Test
  public void testStateSaveAndRestore() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    Assert.assertNotNull(state);

    compiler.setState(state);
    Assert.assertEquals(state.externsRoot, compiler.externsRoot);
  }

  @Test
  public void testSourceLineAndRegion() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;\nvar b = 2;\nvar c = 3;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);

    Assert.assertNull(compiler.getSourceLine("in.js", 0));
    Assert.assertEquals("var a = 1;", compiler.getSourceLine("in.js", 1));
    Assert.assertEquals("var b = 2;", compiler.getSourceLine("in.js", 2));
    Assert.assertNull(compiler.getSourceLine("nonexistent.js", 1));

    Assert.assertNull(compiler.getSourceRegion("in.js", -1));
    Region region = compiler.getSourceRegion("in.js", 2);
    Assert.assertNotNull(region);
  }

  @Test
  public void testAstDotGraphAndCFG() throws IOException {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "function foo(x) { if (x) return 1; else return 2; }");
    compiler.compile(extern, input, options);

    String dotGraph = compiler.getAstDotGraph();
    Assert.assertNotNull(dotGraph);
    Assert.assertTrue(dotGraph.contains("digraph"));

    ControlFlowGraph<Node> cfg = compiler.computeCFG();
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testGetAstDotGraphWhenNull() throws IOException {
    Assert.assertEquals("", compiler.getAstDotGraph());
  }

  @Test
  public void testGetNodeForCodeInsertion() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    Node insertionNode = compiler.getNodeForCodeInsertion(null);
    Assert.assertNotNull(insertionNode);
    Assert.assertEquals(Token.SCRIPT, insertionNode.getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetNodeForCodeInsertionEmptyThrows() {
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], options);
    compiler.getNodeForCodeInsertion(null);
  }

  @Test
  public void testCodingConventionAndIdeMode() {
    compiler.initCompilerOptionsIfTesting();
    Assert.assertNotNull(compiler.getCodingConvention());
    Assert.assertFalse(compiler.isIdeMode());
    Assert.assertFalse(compiler.acceptConstKeyword());

    options.ideMode = true;
    options.acceptConstKeyword = true;
    Assert.assertTrue(compiler.isIdeMode());
    Assert.assertTrue(compiler.acceptConstKeyword());
  }

  @Test
  public void testParserConfig() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    Assert.assertNotNull(compiler.getParserConfig());

    Compiler c2 = new Compiler();
    CompilerOptions o2 = new CompilerOptions();
    o2.setLanguageIn(LanguageMode.ECMASCRIPT5);
    c2.initOptions(o2);
    Assert.assertNotNull(c2.getParserConfig());
  }

  @Test
  public void testPassConfigManagement() {
    compiler.initCompilerOptionsIfTesting();
    PassConfig passConfig = compiler.getPassConfig();
    Assert.assertNotNull(passConfig);

    Compiler freshCompiler = new Compiler();
    DefaultPassConfig customPassConfig = new DefaultPassConfig(new CompilerOptions());
    freshCompiler.setPassConfig(customPassConfig);
    Assert.assertEquals(customPassConfig, freshCompiler.getPassConfig());
  }

  @Test(expected = NullPointerException.class)
  public void testSetPassConfigNull() {
    compiler.setPassConfig(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPassConfigTwice() {
    compiler.initCompilerOptionsIfTesting();
    compiler.getPassConfig(); // triggers creation
    compiler.setPassConfig(new DefaultPassConfig(options));
  }

  @Test
  public void testChangeHandlers() {
    final int[] changeCount = new int[1];
    CodeChangeHandler handler = new CodeChangeHandler() {
      public void reportChange() {
        changeCount[0]++;
      }
    };

    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    Assert.assertEquals(1, changeCount[0]);

    compiler.removeChangeHandler(handler);
    compiler.reportCodeChange();
    Assert.assertEquals(1, changeCount[0]);
  }

  @Test
  public void testRunCallable() {
    compiler.disableThreads();
    String val = Compiler.runCallable(new Callable<String>() {
      public String call() {
        return "result";
      }
    }, false, false);
    Assert.assertEquals("result", val);

    String valLargeStack = Compiler.runCallableWithLargeStack(new Callable<String>() {
      public String call() {
        return "largeStackResult";
      }
    });
    Assert.assertEquals("largeStackResult", valLargeStack);
  }

  @Test(expected = RuntimeException.class)
  public void testRunCallableException() {
    Compiler.runCallable(new Callable<Void>() {
      public Void call() throws Exception {
        throw new IOException("simulated failure");
      }
    }, false, false);
  }

  @Test
  public void testThrowInternalError() {
    try {
      compiler.throwInternalError("custom message", new IllegalArgumentException("cause"));
      Assert.fail("Expected exception");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
      Assert.assertTrue(e.getMessage().contains("custom message"));
    }
  }

  @Test
  public void testTypeRegistryAndValidator() {
    compiler.initCompilerOptionsIfTesting();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    Assert.assertNotNull(registry);
    Assert.assertNotNull(compiler.getTypeValidator());
    Assert.assertNotNull(compiler.getReverseAbstractInterpreter());
  }

  @Test
  public void testLoggingLevel() {
    Compiler.setLoggingLevel(Level.WARNING);
    Compiler.setLoggingLevel(Level.INFO);
  }

  @Test
  public void testRegExpGlobalReferences() {
    Assert.assertTrue(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    Assert.assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test
  public void testGlobalVarReferencesUpdate() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    Node script = compiler.parseInputs();

    compiler.updateGlobalVarReferences(new HashMap<Scope.Var, ReferenceCollectingCallback.ReferenceCollection>(), script);
    Assert.assertNotNull(compiler.getGlobalVarReferences());
  }

  @Test
  public void testStripCodeAndRemoveTryCatch() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "try { var debug_x = 1; } catch(e) {}");
    options.stripTypes = Sets.newHashSet("debug_");
    options.removeTryCatchFinally = true;
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    compiler.removeTryCatchFinally();
    compiler.stripCode(options.stripTypes, Collections.<String>emptySet(),
        Collections.<String>emptySet(), Collections.<String>emptySet());
  }

  @Test
  public void testCustomPassesExecution() {
    final boolean[] passRun = new boolean[1];
    CompilerPass pass = new CompilerPass() {
      public void process(Node externs, Node root) {
        passRun[0] = true;
      }
    };
    options.addCustomPass(CustomPassExecutionTime.BEFORE_CHECKS, pass);

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.compile(extern, input, options);
    Assert.assertTrue(passRun[0]);
  }

  @Test
  public void testManageClosureDependencies() {
    options.manageClosureDependencies = true;
    options.manageClosureDependenciesEntryPoints = Lists.newArrayList("entry");

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile dep = JSSourceFile.fromCode("dep.js", "goog.provide('dep'); var dep_val = 1;");
    JSSourceFile entry = JSSourceFile.fromCode("entry.js", "goog.provide('entry'); goog.require('dep'); var entry_val = 2;");
    JSSourceFile unused = JSSourceFile.fromCode("unused.js", "goog.provide('unused'); var unused_val = 3;");

    compiler.compile(new JSSourceFile[] { extern }, new JSSourceFile[] { unused, dep, entry }, options);
    String source = compiler.toSource();
    Assert.assertTrue(source.contains("entry_val"));
    Assert.assertTrue(source.contains("dep_val"));
    Assert.assertFalse(source.contains("unused_val"));
  }

  @Test
  public void testManageClosureDependenciesMissingProvide() {
    options.manageClosureDependencies = true;
    options.manageClosureDependenciesEntryPoints = Lists.newArrayList("nonexistent_entry");

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");

    Result result = compiler.compile(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    Assert.assertFalse(result.success);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testDevModeSanityChecks() {
    options.devMode = DevMode.EVERY_PASS;
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);

    Compiler c2 = new Compiler();
    CompilerOptions o2 = new CompilerOptions();
    o2.devMode = DevMode.START_AND_END;
    Result r2 = c2.compile(extern, input, o2);
    Assert.assertTrue(r2.success);
  }

  @Test
  public void testTracerModeOptions() {
    options.tracer = TracerMode.ALL;
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
  }

  @Test
  public void testGetMessages() {
    compiler.initCompilerOptionsIfTesting();
    Assert.assertNotNull(compiler.getMessages());
    Assert.assertNotNull(compiler.getErrors());
    Assert.assertNotNull(compiler.getWarnings());
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testRootGetters() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    compiler.compile(extern, input, options);

    Assert.assertNotNull(compiler.getRoot());
    Assert.assertNotNull(compiler.externsRoot);
    Assert.assertNotNull(compiler.jsRoot);
  }

  @Test
  public void testCssRenamingMap() {
    CssRenamingMap map = new CssRenamingMap() {
      public String get(String value) {
        return "renamed_" + value;
      }
      public CssRenamingMap.Style getStyle() {
        return CssRenamingMap.Style.BY_WHOLE;
      }
    };
    compiler.setCssRenamingMap(map);
    Assert.assertEquals(map, compiler.getCssRenamingMap());
  }

  @Test
  public void testInliningForbidden() {
    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    compiler.initOptions(options);
    Assert.assertTrue(compiler.isInliningForbidden());

    options.propertyRenaming = PropertyRenamingPolicy.OFF;
    Assert.assertFalse(compiler.isInliningForbidden());
  }

  @Test
  public void testProcessDefines() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "/** @define {boolean} */ var DEF = true;");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();
    compiler.processDefines();
  }

  @Test
  public void testExternExports() {
    options.externExportsPath = "exports.js";
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "goog.exportSymbol('myExport', 1);");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
  }

  @Test
  public void testSourceMapGeneration() {
    options.sourceMapOutputPath = "test.map";
    options.sourceMapFormat = SourceMap.Format.V3;
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertNotNull(compiler.getSourceMap());
  }
}