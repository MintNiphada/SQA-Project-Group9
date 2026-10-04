package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.TracerMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
  public void testDefaultConstructorAndInitOptions() {
    compiler.initOptions(options);
    Assert.assertNotNull(compiler.getErrorManager());
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
    Assert.assertFalse(compiler.hasErrors());
    Assert.assertFalse(compiler.hasHaltingErrors());
  }

  @Test
  public void testConstructorWithPrintStream() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);
    customCompiler.initOptions(options);
    Assert.assertNotNull(customCompiler.getErrorManager());
  }

  @Test
  public void testConstructorWithErrorManager() {
    PrintStreamErrorManager em = new PrintStreamErrorManager(new LightweightMessageFormatter(compiler), System.err);
    Compiler customCompiler = new Compiler(em);
    Assert.assertSame(em, customCompiler.getErrorManager());
  }

  @Test(expected = NullPointerException.class)
  public void testSetErrorManagerNull() {
    compiler.setErrorManager(null);
  }

  @Test
  public void testLoggingLevel() {
    Compiler.setLoggingLevel(Level.WARNING);
  }

  @Test
  public void testCompileSingleFileInput() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var window;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1; function foo() { return x; }");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertNotNull(compiler.getRoot());
    Assert.assertNotNull(compiler.toSource());
  }

  @Test
  public void testCompileArrayInput() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("in1.js", "var a = 1;"),
        JSSourceFile.fromCode("in2.js", "var b = 2;")
    };
    Result result = compiler.compile(extern, inputs, options);
    Assert.assertTrue(result.success);
    String[] sources = compiler.toSourceArray();
    Assert.assertEquals(2, sources.length);
  }

  @Test
  public void testCompileModules() {
    JSModule mod1 = new JSModule("m1");
    mod1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));
    JSModule mod2 = new JSModule("m2");
    mod2.add(JSSourceFile.fromCode("m2.js", "var b = a + 1;"));
    mod2.addDependency(mod1);

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    Result result = compiler.compile(extern, new JSModule[] { mod1, mod2 }, options);
    Assert.assertTrue(result.success);

    String m1Src = compiler.toSource(mod1);
    Assert.assertTrue(m1Src.contains("a = 1"));
    String[] m2Array = compiler.toSourceArray(mod2);
    Assert.assertEquals(1, m2Array.length);

    Node modNode = compiler.getNodeForCodeInsertion(mod2);
    Assert.assertNotNull(modNode);
    Node defaultNode = compiler.getNodeForCodeInsertion(null);
    Assert.assertNotNull(defaultNode);
  }

  @Test
  public void testEmptyModuleCompile() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    compiler.compile(extern, new JSModule[0], options);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testEmptyRootModuleCompile() {
    JSModule m1 = new JSModule("m1");
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    compiler.compile(extern, new JSModule[] { m1 }, options);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testDuplicateInputInModules() {
    JSModule m1 = new JSModule("m1");
    JSSourceFile shared = JSSourceFile.fromCode("shared.js", "var a = 1;");
    m1.add(shared);

    JSModule m2 = new JSModule("m2");
    m2.add(shared);
    m2.addDependency(m1);

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    compiler.compile(extern, new JSModule[] { m1, m2 }, options);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testDuplicateInputNames() {
    JSSourceFile extern = JSSourceFile.fromCode("shared.js", "var a;");
    JSSourceFile in1 = JSSourceFile.fromCode("input.js", "var b;");
    JSSourceFile in2 = JSSourceFile.fromCode("input.js", "var c;");

    compiler.compile(new JSSourceFile[] { extern }, new JSSourceFile[] { in1, in2 }, options);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testDuplicateExternNames() {
    JSSourceFile extern1 = JSSourceFile.fromCode("shared.js", "var a;");
    JSSourceFile extern2 = JSSourceFile.fromCode("shared.js", "var b;");
    JSSourceFile in = JSSourceFile.fromCode("input.js", "var c;");

    compiler.compile(new JSSourceFile[] { extern1, extern2 }, new JSSourceFile[] { in }, options);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testCompileWithOptions() {
    options.sourceMapOutputPath = "%outname%.map";
    options.devMode = DevMode.START_AND_END;
    options.checkTypes = true;
    options.closurePass = true;
    options.removeTryCatchFinally = true;
    options.stripTypes = Collections.singleton("goog.debug.Logger");
    options.recordFunctionInformation = true;

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var window;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "try { var x = 1; } catch(e) {}");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertNotNull(compiler.getSourceMap());
    Assert.assertNotNull(compiler.getTypeRegistry());
    Assert.assertNotNull(compiler.getReverseAbstractInterpreter());
    Assert.assertNotNull(compiler.getTypeValidator());
    Assert.assertTrue(compiler.isTypeCheckingEnabled());
  }

  @Test
  public void testNormalizeState() {
    Assert.assertFalse(compiler.isNormalized());
    compiler.setNormalized();
    Assert.assertTrue(compiler.isNormalized());
    compiler.setUnnormalized();
    Assert.assertFalse(compiler.isNormalized());
  }

  @Test
  public void testUniqueNameId() {
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    Assert.assertEquals("0", supplier.get());
    Assert.assertEquals("1", supplier.get());
    compiler.resetUniqueNameId();
    Assert.assertEquals("0", supplier.get());
  }

  @Test
  public void testParseSyntheticAndTestCode() {
    Node node = compiler.parseSyntheticCode("synthetic.js", "var y = 2;");
    Assert.assertNotNull(node);
    Node testCodeNode = compiler.parseTestCode("var z = 3;");
    Assert.assertNotNull(testCodeNode);
    Node synthCodeNode = compiler.parseSyntheticCode("var w = 4;");
    Assert.assertNotNull(synthCodeNode);
    Assert.assertNotNull(compiler.getDefaultErrorReporter());
  }

  @Test
  public void testNewExternInput() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var a;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var b;");
    compiler.compile(extern, input, options);

    CompilerInput newExt = compiler.newExternInput("dynamicExtern.js");
    Assert.assertNotNull(newExt);
    Assert.assertNotNull(compiler.getInput("dynamicExtern.js"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInputDuplicate() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var a;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var b;");
    compiler.compile(extern, input, options);

    compiler.newExternInput("extern.js");
  }

  @Test
  public void testAddIncrementalSourceAst() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var a;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var b;");
    compiler.compile(extern, input, options);

    JsAst ast = new JsAst(JSSourceFile.fromCode("incremental.js", "var inc = 1;"));
    compiler.addIncrementalSourceAst(ast);
    Assert.assertNotNull(compiler.getInput("incremental.js"));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddIncrementalSourceAstDuplicate() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var a;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var b;");
    compiler.compile(extern, input, options);

    JsAst ast = new JsAst(JSSourceFile.fromCode("input.js", "var b;"));
    compiler.addIncrementalSourceAst(ast);
  }

  @Test
  public void testDisableThreads() {
    compiler.disableThreads();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 10;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
  }

  @Test
  public void testTracerOptions() {
    options.tracer = TracerMode.ALL;
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 10;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertNotNull(compiler.tracker);
  }

  @Test
  public void testGetSourceLineAndRegion() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "line1\nline2\nline3");
    compiler.compile(extern, input, options);

    Assert.assertNull(compiler.getSourceLine("input.js", 0));
    Assert.assertNull(compiler.getSourceRegion("input.js", 0));
    Assert.assertEquals("line2", compiler.getSourceLine("input.js", 2));
    Assert.assertNotNull(compiler.getSourceRegion("input.js", 2));
    Assert.assertNull(compiler.getSourceLine("nonexistent.js", 1));
  }

  @Test
  public void testSymbolTable() {
    SymbolTable st = compiler.acquireSymbolTable();
    Assert.assertNotNull(st);
    SymbolTable st2 = compiler.acquireSymbolTable();
    Assert.assertSame(st, st2);
  }

  @Test
  public void testCssRenamingMap() {
    CssRenamingMap map = new CssRenamingMap() {
      public String get(String value) {
        return value + "-renamed";
      }
    };
    compiler.initOptions(options);
    compiler.setCssRenamingMap(map);
    Assert.assertSame(map, compiler.getCssRenamingMap());
  }

  @Test
  public void testCodeChangeHandler() {
    final boolean[] changed = new boolean[] { false };
    CodeChangeHandler handler = new CodeChangeHandler() {
      public void reportChange() {
        changed[0] = true;
      }
    };
    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    Assert.assertTrue(changed[0]);
    compiler.removeChangeHandler(handler);
  }

  @Test
  public void testGetAstDotGraph() throws IOException {
    Assert.assertEquals("", compiler.getAstDotGraph());
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);
    String dot = compiler.getAstDotGraph();
    Assert.assertNotNull(dot);
    Assert.assertTrue(dot.contains("digraph"));
  }

  @Test
  public void testIntermediateStateSaveAndRestore() {
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "var a;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var b = 1;");
    compiler.compile(extern, input, options);

    Compiler.IntermediateState state = compiler.getState();
    Assert.assertNotNull(state);

    Compiler compiler2 = new Compiler();
    compiler2.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler2.setState(state);
    Assert.assertEquals(state.externsRoot, compiler2.externsRoot);
  }

  @Test
  public void testToSourceWithDelimitersAndLicense() {
    options.printInputDelimiter = true;
    options.inputDelimiter = "// [%name%:%num%]";
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "/** @license License Text */\nvar x = 1;");
    compiler.compile(extern, input, options);

    String src = compiler.toSource();
    Assert.assertTrue(src.contains("// [input.js:0]"));
    Assert.assertTrue(src.contains("License Text"));
  }

  @Test
  public void testAreNodesEqualForInlining() {
    compiler.initOptions(options);
    Node n1 = new Node(Token.NAME);
    n1.setString("foo");
    Node n2 = new Node(Token.NAME);
    n2.setString("foo");
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = true;
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
  }

  @Test
  public void testPassConfigManagement() {
    PassConfig pc = new DefaultPassConfig(options);
    compiler.setPassConfig(pc);
    Assert.assertSame(pc, compiler.getPassConfig());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPassConfigTwice() {
    PassConfig pc1 = new DefaultPassConfig(options);
    PassConfig pc2 = new DefaultPassConfig(options);
    compiler.setPassConfig(pc1);
    compiler.setPassConfig(pc2);
  }

  @Test(expected = RuntimeException.class)
  public void testThrowInternalError() {
    compiler.throwInternalError("test message", new Exception("cause"));
  }

  @Test
  public void testCodeBuilderCoverage() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("foo\nbar\nbaz");
    Assert.assertEquals(2, cb.getLineIndex());
    Assert.assertEquals(3, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith("baz"));
    Assert.assertFalse(cb.endsWith("unknown"));
    Assert.assertEquals(11, cb.getLength());
    cb.reset();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(2, cb.getLineIndex());
  }

  @Test
  public void testIsInliningForbidden() {
    compiler.initOptions(options);
    Assert.assertFalse(compiler.isInliningForbidden());
    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    Assert.assertTrue(compiler.isInliningForbidden());
    options.propertyRenaming = PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC;
    Assert.assertTrue(compiler.isInliningForbidden());
  }

  @Test
  public void testCheckCustomPassesAndStripCode() {
    options.stripNameSuffixes = new HashSet<String>(Collections.singletonList("StripSuffix"));
    options.stripTypePrefixes = new HashSet<String>(Collections.singletonList("StripPrefix"));
    options.stripNamePrefixes = new HashSet<String>(Collections.singletonList("stripPrefix_"));
    options.customPasses = com.google.common.collect.ArrayListMultimap.create();
    options.customPasses.put(CustomPassExecutionTime.BEFORE_CHECKS, new CompilerPass() {
      public void process(Node externs, Node root) {}
    });

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a_StripSuffix = 1;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
  }
}
