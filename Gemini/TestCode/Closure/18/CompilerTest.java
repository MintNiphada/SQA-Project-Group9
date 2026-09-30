package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

public class CompilerTest {

  @Test
  public void testVersionAndDate() {
    String version = Compiler.getReleaseVersion();
    Assert.assertNotNull(version);
    String date = Compiler.getReleaseDate();
    Assert.assertNotNull(date);
  }

  @Test
  public void testCodeBuilderOperations() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(0, cb.getLineIndex());
    Assert.assertEquals(0, cb.getColumnIndex());
    Assert.assertFalse(cb.endsWith(";"));

    cb.append("var a = 1;\nvar b = 2;");
    Assert.assertEquals(1, cb.getLineIndex());
    Assert.assertEquals(10, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith(";"));
    Assert.assertFalse(cb.endsWith("xyz"));
    Assert.assertEquals("var a = 1;\nvar b = 2;", cb.toString());

    cb.reset();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(1, cb.getLineIndex());

    cb.append("foo");
    Assert.assertEquals(3, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith("oo"));
  }

  @Test
  public void testBasicCompilationSingleFile() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5);

    SourceFile extern = SourceFile.fromCode("externs.js", "function alert(x) {}");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1 + 2; alert(x);");

    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());

    String source = compiler.toSource();
    Assert.assertTrue(source.contains("alert(3)") || source.contains("var x=3;alert(x)") || source.contains("alert(3)"));
  }

  @Test
  public void testCompileWithLists() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    List<SourceFile> externs = Collections.singletonList(SourceFile.fromCode("externs.js", ""));
    List<SourceFile> inputs = Collections.singletonList(SourceFile.fromCode("input.js", "var a = 1;"));

    Result result = compiler.compile(externs, inputs, options);
    Assert.assertTrue(result.success);
    Assert.assertNotNull(compiler.getRoot());
    Assert.assertNotNull(compiler.getMessages());
    Assert.assertEquals(0, compiler.getMessages().length);
  }

  @Test
  public void testCompileWithModules() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var a = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var b = a + 1;"));
    m2.addDependency(m1);

    List<SourceFile> externs = ImmutableList.of(SourceFile.fromCode("externs.js", ""));
    List<JSModule> modules = ImmutableList.of(m1, m2);

    Result result = compiler.compileModules(externs, modules, options);
    Assert.assertTrue(result.success);
    Assert.assertNotNull(compiler.getModuleGraph());
    Assert.assertEquals(2, compiler.getDegenerateModuleGraph().getModuleCount());

    String s1 = compiler.toSource(m1);
    Assert.assertTrue(s1.contains("var a=1"));

    String[] sourceArray = compiler.toSourceArray(m2);
    Assert.assertEquals(1, sourceArray.length);
  }

  @Test
  public void testEmptyModulesFilling() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("root");
    m1.add(SourceFile.fromCode("input.js", "var x = 10;"));
    JSModule m2 = new JSModule("emptyMod");

    List<SourceFile> externs = Collections.emptyList();
    List<JSModule> modules = Lists.newArrayList(m1, m2);

    compiler.initModules(externs, modules, options);
    Assert.assertEquals(1, m2.getInputs().size());
    Assert.assertEquals(Compiler.createFillFileName("emptyMod"), m2.getInputs().get(0).getName());
  }

  @Test
  public void testProgressAndState() {
    Compiler compiler = new Compiler();
    compiler.setProgress(-0.5);
    Assert.assertEquals(0.0, compiler.getProgress(), 0.0001);
    compiler.setProgress(1.5);
    Assert.assertEquals(1.0, compiler.getProgress(), 0.0001);
    compiler.setProgress(0.5);
    Assert.assertEquals(0.5, compiler.getProgress(), 0.0001);

    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(SourceFile.fromCode("input.js", "var a = 1;")),
        options);

    compiler.parseInputs();
    Compiler.IntermediateState state = compiler.getState();
    Assert.assertNotNull(state);

    compiler.setState(state);
    Assert.assertEquals(1, compiler.getInputsForTesting().size());
    Assert.assertEquals(1, compiler.getExternsForTesting().size());
  }

  @Test
  public void testPrintStreamConstructorAndLogging() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);
    Compiler.setLoggingLevel(Level.FINE);

    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Assert.assertNotNull(compiler.getErrorManager());

    compiler.addToDebugLog("Debug message 1");
  }

  @Test
  public void testErrorManagerSetup() {
    BasicErrorManager customErrorManager = new BasicErrorManager() {
      @Override
      public void println(CheckLevel level, JSError error) {}
      @Override
      protected void printSummary() {}
    };
    Compiler compiler = new Compiler(customErrorManager);
    Assert.assertSame(customErrorManager, compiler.getErrorManager());
  }

  @Test
  public void testSourceRegionAndLines() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    String code = "line 1\nline 2\nline 3\nline 4";
    SourceFile sf = SourceFile.fromCode("test.js", code);
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(sf),
        options);

    Assert.assertNull(compiler.getSourceLine("test.js", 0));
    Assert.assertEquals("line 1", compiler.getSourceLine("test.js", 1));
    Assert.assertEquals("line 3", compiler.getSourceLine("test.js", 3));
    Assert.assertNull(compiler.getSourceLine("nonexistent.js", 1));

    Assert.assertNull(compiler.getSourceRegion("test.js", 0));
    Region region = compiler.getSourceRegion("test.js", 2);
    Assert.assertNotNull(region);
  }

  @Test
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();
    Node n1 = compiler.parseSyntheticCode("var synthetic = 1;");
    Assert.assertNotNull(n1);

    Node n2 = compiler.parseSyntheticCode("syn.js", "var syn = 2;");
    Assert.assertNotNull(n2);

    Node n3 = compiler.parseTestCode("var test = 3;");
    Assert.assertNotNull(n3);

    Node n4 = compiler.parse(SourceFile.fromCode("parseTest.js", "var x = 4;"));
    Assert.assertNotNull(n4);
  }

  @Test
  public void testTypeRegistryAndScopes() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();

    JSTypeRegistry registry = compiler.getTypeRegistry();
    Assert.assertNotNull(registry);
    Assert.assertSame(registry, compiler.getTypeRegistry());

    Assert.assertNotNull(compiler.getDefaultErrorReporter());
    Assert.assertNotNull(compiler.getReverseAbstractInterpreter());
    Assert.assertNotNull(compiler.getTypeValidator());
    Assert.assertNotNull(compiler.getCodingConvention());
  }

  @Test
  public void testPassConfigSetup() {
    Compiler compiler = new Compiler();
    PassConfig defaultPassConfig = compiler.getPassConfig();
    Assert.assertNotNull(defaultPassConfig);

    try {
      compiler.setPassConfig(new DefaultPassConfig(new CompilerOptions()));
      Assert.fail("Expected IllegalStateException on reassigning PassConfig");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void testDisableThreadsAndCallable() throws Exception {
    Compiler compiler = new Compiler();
    compiler.disableThreads();

    String result = Compiler.runCallableWithLargeStack(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "success";
      }
    });
    Assert.assertEquals("success", result);

    String normalResult = Compiler.runCallable(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "no threads";
      }
    }, false, false);
    Assert.assertEquals("no threads", normalResult);
  }

  @Test
  public void testUniqueNameSupplier() {
    Compiler compiler = new Compiler();
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    Assert.assertEquals("0", supplier.get());
    Assert.assertEquals("1", supplier.get());
    compiler.resetUniqueNameId();
    Assert.assertEquals("0", supplier.get());
  }

  @Test
  public void testLanguageModeAndEcmaScript5Support() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);

    Assert.assertTrue(compiler.acceptEcmaScript5());
    Assert.assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
    Assert.assertNotNull(compiler.getParserConfig());

    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
    compiler.options = null;
    compiler.initOptions(options);
    Assert.assertFalse(compiler.acceptEcmaScript5());
  }

  @Test
  public void testInputManagementDynamicAst() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(SourceFile.fromCode("input.js", "var a = 1;")),
        options);

    compiler.parseInputs();

    JsAst newAst = new JsAst(SourceFile.fromCode("input2.js", "var b = 2;"));
    boolean added = compiler.addNewSourceAst(newAst);
    Assert.assertTrue(added);
    Assert.assertNotNull(compiler.getInput(newAst.getInputId()));

    JsAst replacementAst = new JsAst(SourceFile.fromCode("input2.js", "var b = 3;"));
    boolean replaced = compiler.replaceIncrementalSourceAst(replacementAst);
    Assert.assertTrue(replaced);

    CompilerInput newExtern = compiler.newExternInput("synthetic_externs.js");
    Assert.assertNotNull(newExtern);
    Assert.assertTrue(newExtern.isExtern());

    compiler.removeExternInput(newExtern.getInputId());
    Assert.assertNull(compiler.getInput(newExtern.getInputId()));
  }

  @Test
  public void testDuplicateInputsReported() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    List<SourceFile> externs = Collections.emptyList();
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("duplicate.js", "var a = 1;"),
        SourceFile.fromCode("duplicate.js", "var b = 2;")
    );

    compiler.init(externs, inputs, options);
    Assert.assertTrue(compiler.getErrorCount() > 0);
  }

  @Test
  public void testAreNodesEqualForInlining() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
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
  public void testChangeHandlerAndCodeChange() {
    Compiler compiler = new Compiler();
    final boolean[] changed = new boolean[1];
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
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
  public void testToSourceArray() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("file1.js", "var a = 1;"),
        SourceFile.fromCode("file2.js", "var b = 2;")
    );

    compiler.compile(Collections.singletonList(extern), inputs, options);
    String[] sources = compiler.toSourceArray();
    Assert.assertEquals(2, sources.length);
    Assert.assertTrue(sources[0].contains("var a=1"));
    Assert.assertTrue(sources[1].contains("var b=2"));
  }

  @Test
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    try {
      compiler.throwInternalError("Test error", new IllegalArgumentException("cause"));
      Assert.fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
      Assert.assertTrue(e.getMessage().contains("Test error"));
      Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testAstDotGraph() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.compile(
        SourceFile.fromCode("externs.js", ""),
        SourceFile.fromCode("input.js", "var x = 1;"),
        options);

    String dotGraph = compiler.getAstDotGraph();
    Assert.assertNotNull(dotGraph);
    Assert.assertTrue(dotGraph.contains("digraph"));
  }

  @Test
  public void testDelimiterAndCodeBuilder() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.printInputDelimiter = true;
    options.inputDelimiter = "// [%name%]";
    compiler.initOptions(options);

    Node scriptNode = new Node(Token.SCRIPT);
    scriptNode.setInputId(new InputId("test.js"));
    scriptNode.setStaticSourceFile(SourceFile.fromCode("test.js", "var x = 1;"));

    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    compiler.toSource(cb, 0, scriptNode);
    Assert.assertTrue(cb.toString().contains("// [test.js]"));
  }
}