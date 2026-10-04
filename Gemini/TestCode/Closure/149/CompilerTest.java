package com.google.javascript.jscomp;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class CompilerTest {

  @Test
  public void testDefaultConstructorAndInitialization() {
    Compiler compiler = new Compiler();
    Assert.assertNotNull(compiler.getErrorManager());
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertEquals(0, compiler.getWarningCount());
    Assert.assertFalse(compiler.hasErrors());
    Assert.assertNotNull(compiler.getCodingConvention());
    Assert.assertNotNull(compiler.getParserConfig());
    Assert.assertNotNull(compiler.getDefaultErrorReporter());
    Assert.assertNotNull(compiler.getUniqueNameIdSupplier());
    Assert.assertEquals("0", compiler.getUniqueNameIdSupplier().get());
  }

  @Test
  public void testPrintStreamConstructorAndLoggingLevel() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Compiler.setLoggingLevel(Level.OFF);
    Assert.assertNotNull(compiler.getErrorManager());
  }

  @Test
  public void testErrorManagerConstructor() {
    MessageFormatter formatter = new BasicErrorFormatter();
    ErrorManager em = new PrintStreamErrorManager(formatter, System.err);
    Compiler compiler = new Compiler(em);
    Assert.assertSame(em, compiler.getErrorManager());
  }

  @Test(expected = NullPointerException.class)
  public void testSetErrorManagerNull() {
    Compiler compiler = new Compiler();
    compiler.setErrorManager(null);
  }

  @Test
  public void testCompileSimpleString() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1 + 1;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertEquals(0, result.errors.length);
    String source = compiler.toSource();
    Assert.assertNotNull(source);
    Assert.assertTrue(source.contains("var x=2") || source.contains("var x = 2") || source.contains("var x=1+1") || source.contains("var x = 1 + 1"));
  }

  @Test
  public void testCompileArrayArguments() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("externs.js", "") };
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("input.js", "function test() { return 1; }") };
    Result result = compiler.compile(externs, inputs, options);
    Assert.assertTrue(result.success);
    String[] sources = compiler.toSourceArray();
    Assert.assertEquals(1, sources.length);
  }

  @Test
  public void testCompileModules() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));
    m2.addDependency(m1);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    Result result = compiler.compile(extern, new JSModule[] { m1, m2 }, options);
    Assert.assertTrue(result.success);
    Assert.assertEquals(2, compiler.getModuleGraph().getModuleCount());

    String srcM1 = compiler.toSource(m1);
    Assert.assertNotNull(srcM1);
    String[] srcArrayM1 = compiler.toSourceArray(m1);
    Assert.assertEquals(1, srcArrayM1.length);
  }

  @Test
  public void testCompileModuleDependencyError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));
    m1.addDependency(m2);

    Result result = compiler.compileModules(
        Collections.<JSSourceFile>emptyList(),
        Lists.newArrayList(m1, m2),
        options);
    Assert.assertFalse(result.success);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testCompileEmptyModuleList() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    Result result = compiler.compileModules(
        Collections.<JSSourceFile>emptyList(),
        Collections.<JSModule>emptyList(),
        options);
    Assert.assertFalse(result.success);
  }

  @Test
  public void testCompileEmptyRootModuleError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSModule m1 = new JSModule("root");
    JSModule m2 = new JSModule("child");
    m2.add(JSSourceFile.fromCode("c.js", "var c = 3;"));
    m2.addDependency(m1);

    Result result = compiler.compileModules(
        Collections.<JSSourceFile>emptyList(),
        Lists.newArrayList(m1, m2),
        options);
    Assert.assertFalse(result.success);
  }

  @Test
  public void testDuplicateInputError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile f1 = JSSourceFile.fromCode("dup.js", "var a = 1;");
    JSSourceFile f2 = JSSourceFile.fromCode("dup.js", "var b = 2;");
    Result result = compiler.compile(new JSSourceFile[0], new JSSourceFile[] { f1, f2 }, options);
    Assert.assertFalse(result.success);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testDuplicateExternInputError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile e1 = JSSourceFile.fromCode("dupExt.js", "var ext1;");
    JSSourceFile e2 = JSSourceFile.fromCode("dupExt.js", "var ext2;");
    JSSourceFile f1 = JSSourceFile.fromCode("input.js", "var b = 2;");
    Result result = compiler.compile(new JSSourceFile[] { e1, e2 }, new JSSourceFile[] { f1 }, options);
    Assert.assertFalse(result.success);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testCodeBuilderOperations() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(0, cb.getLineIndex());
    Assert.assertEquals(0, cb.getColumnIndex());

    cb.append("foo\nbar\nbaz");
    Assert.assertEquals(11, cb.getLength());
    Assert.assertEquals(2, cb.getLineIndex());
    Assert.assertEquals(3, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith("baz"));
    Assert.assertFalse(cb.endsWith("bar"));
    Assert.assertEquals("foo\nbar\nbaz", cb.toString());

    cb.reset();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(2, cb.getLineIndex());
    Assert.assertEquals(3, cb.getColumnIndex());

    cb.append("singleline");
    Assert.assertEquals(13, cb.getColumnIndex());
  }

  @Test
  public void testNormalizeAndUnnormalizeFlags() {
    Compiler compiler = new Compiler();
    Assert.assertFalse(compiler.isNormalized());
    compiler.setNormalized();
    Assert.assertTrue(compiler.isNormalized());
    compiler.setUnnormalized();
    Assert.assertFalse(compiler.isNormalized());
  }

  @Test
  public void testRegExpGlobalReferences() {
    Compiler compiler = new Compiler();
    Assert.assertTrue(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    Assert.assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test
  public void testNewExternInput() {
    Compiler compiler = new Compiler();
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], new CompilerOptions());
    compiler.externsRoot = new Node(Token.BLOCK);
    CompilerInput input = compiler.newExternInput("synthesized_extern.js");
    Assert.assertNotNull(input);
    Assert.assertEquals("synthesized_extern.js", input.getName());
    Assert.assertNotNull(compiler.getInput("synthesized_extern.js"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInputDuplicateThrows() {
    Compiler compiler = new Compiler();
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], new CompilerOptions());
    compiler.externsRoot = new Node(Token.BLOCK);
    compiler.newExternInput("dup.js");
    compiler.newExternInput("dup.js");
  }

  @Test
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    Node node1 = compiler.parseSyntheticCode("var a = 10;");
    Assert.assertNotNull(node1);

    Node node2 = compiler.parseSyntheticCode("custom.js", "var b = 20;");
    Assert.assertNotNull(node2);

    Node node3 = compiler.parseTestCode("var c = 30;");
    Assert.assertNotNull(node3);
  }

  @Test
  public void testSourceLineAndRegionRetrieval() {
    Compiler compiler = new Compiler();
    JSSourceFile input = JSSourceFile.fromCode("lines.js", "line 1\nline 2\nline 3\n");
    compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input }, new CompilerOptions());

    Assert.assertNull(compiler.getSourceLine("lines.js", 0));
    Assert.assertEquals("line 1", compiler.getSourceLine("lines.js", 1));
    Assert.assertEquals("line 2", compiler.getSourceLine("lines.js", 2));
    Assert.assertNull(compiler.getSourceLine("nonexistent.js", 1));

    Assert.assertNull(compiler.getSourceRegion("lines.js", 0));
    Region region = compiler.getSourceRegion("lines.js", 2);
    Assert.assertNotNull(region);
    Assert.assertNull(compiler.getSourceRegion("nonexistent.js", 1));
  }

  @Test
  public void testGetNodeForCodeInsertion() {
    Compiler compiler = new Compiler();
    JSSourceFile input = JSSourceFile.fromCode("main.js", "var x;");
    compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input }, new CompilerOptions());

    Node defaultNode = compiler.getNodeForCodeInsertion(null);
    Assert.assertNotNull(defaultNode);

    JSModule mod = new JSModule("mod");
    mod.add(JSSourceFile.fromCode("mod.js", "var y;"));
    Node modNode = compiler.getNodeForCodeInsertion(mod);
    Assert.assertNotNull(modNode);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetNodeForCodeInsertionEmptyModuleThrows() {
    Compiler compiler = new Compiler();
    compiler.getNodeForCodeInsertion(new JSModule("empty"));
  }

  @Test
  public void testGetReverseAbstractInterpreterAndTypeValidator() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Assert.assertNotNull(compiler.getTypeValidator());
    Assert.assertNotNull(compiler.getReverseAbstractInterpreter());

    options.closurePass = true;
    Compiler compiler2 = new Compiler();
    compiler2.initOptions(options);
    Assert.assertNotNull(compiler2.getReverseAbstractInterpreter());
  }

  @Test
  public void testTypeRegistry() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    JSTypeRegistry registry1 = compiler.getTypeRegistry();
    Assert.assertNotNull(registry1);
    JSTypeRegistry registry2 = compiler.getTypeRegistry();
    Assert.assertSame(registry1, registry2);
  }

  @Test
  public void testSetPassConfig() {
    Compiler compiler = new Compiler();
    PassConfig passConfig = new DefaultPassConfig(new CompilerOptions());
    compiler.setPassConfig(passConfig);
    Assert.assertSame(passConfig, compiler.getPassConfig());
  }

  @Test(expected = NullPointerException.class)
  public void testSetPassConfigNullThrows() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPassConfigTwiceThrows() {
    Compiler compiler = new Compiler();
    PassConfig passConfig = new DefaultPassConfig(new CompilerOptions());
    compiler.setPassConfig(passConfig);
    compiler.setPassConfig(passConfig);
  }

  @Test
  public void testSaveAndRestoreState() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile input = JSSourceFile.fromCode("state.js", "var stateVar = 1;");
    compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input }, options);

    Compiler.IntermediateState state = compiler.getState();
    Assert.assertNotNull(state);

    Compiler compiler2 = new Compiler();
    compiler2.init(new JSSourceFile[0], new JSSourceFile[] { input }, options);
    compiler2.setState(state);
    Assert.assertEquals(state.externsRoot, compiler2.externsRoot);
    Assert.assertNotNull(compiler2.getPassConfig());
  }

  @Test
  public void testCodeChangeHandlerReporting() {
    Compiler compiler = new Compiler();
    final boolean[] changed = new boolean[] { false };
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
  public void testCustomPassesExecution() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    final boolean[] executed = new boolean[] { false };
    options.customPasses = ArrayListMultimap.create();
    options.customPasses.put(CustomPassExecutionTime.BEFORE_CHECKS, new CompilerPass() {
      public void process(Node externs, Node root) {
        executed[0] = true;
      }
    });
    JSSourceFile input = JSSourceFile.fromCode("cust.js", "var a = 1;");
    compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input }, options);
    Assert.assertTrue(executed[0]);
  }

  @Test
  public void testStripCodeAndRemoveTryCatchOptions() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.removeTryCatchFinally = true;
    options.stripTypes = Collections.singleton("goog.debug.Logger");
    JSSourceFile input = JSSourceFile.fromCode("strip.js", "try { goog.debug.Logger(); } catch(e) {}");
    Result result = compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input }, options);
    Assert.assertTrue(result.success);
  }

  @Test
  public void testSourceMapAndDelimiters() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.sourceMapOutputPath = "sourcemap.map";
    options.printInputDelimiter = true;
    options.inputDelimiter = "// %name% : %num%";

    JSSourceFile input = JSSourceFile.fromCode("testmap.js", "/** @license License Text */ var mapped = 10;");
    Result result = compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input }, options);
    Assert.assertTrue(result.success);
    Assert.assertNotNull(compiler.getSourceMap());

    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    compiler.toSource(cb, 1, compiler.jsRoot.getFirstChild());
    Assert.assertTrue(cb.toString().contains("testmap.js : 1"));
    Assert.assertTrue(cb.toString().contains("License Text"));
  }

  @Test
  public void testManageClosureDependencies() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.manageClosureDependencies = true;
    JSSourceFile f1 = JSSourceFile.fromCode("f1.js", "goog.provide('a'); var a = 1;");
    JSSourceFile f2 = JSSourceFile.fromCode("f2.js", "goog.require('a'); var b = a;");
    Result result = compiler.compile(new JSSourceFile[0], new JSSourceFile[] { f2, f1 }, options);
    Assert.assertTrue(result.success);
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
    Node n3 = new Node(Token.NAME);
    n3.setString("bar");

    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    Assert.assertFalse(compiler.areNodesEqualForInlining(n1, n3));

    options.ambiguateProperties = true;
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    Assert.assertFalse(compiler.areNodesEqualForInlining(n1, n3));
  }

  @Test
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    Exception cause = new Exception("Root cause");
    try {
      compiler.throwInternalError("Something failed", cause);
      Assert.fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
      Assert.assertTrue(e.getMessage().contains("Something failed"));
      Assert.assertEquals(cause, e.getCause());
    }
  }

  @Test
  public void testComputeCFGAndAstDotGraph() throws IOException {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile input = JSSourceFile.fromCode("cfg.js", "function f(x) { if (x) { return 1; } else { return 2; } }");
    compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input }, options);

    ControlFlowGraph<Node> cfg = compiler.computeCFG();
    Assert.assertNotNull(cfg);

    String dotGraph = compiler.getAstDotGraph();
    Assert.assertNotNull(dotGraph);
    Assert.assertTrue(dotGraph.contains("digraph"));
  }

  @Test
  public void testCssRenamingMap() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    CssRenamingMap map = new CssRenamingMap() {
      public String get(String value) {
        return "renamed-" + value;
      }
      public Style getStyle() {
        return Style.BY_WHOLE;
      }
    };
    compiler.setCssRenamingMap(map);
    Assert.assertSame(map, compiler.getCssRenamingMap());
  }

  @Test
  public void testGetInputsAndExternsForTesting() {
    Compiler compiler = new Compiler();
    JSSourceFile ext = JSSourceFile.fromCode("ext.js", "");
    JSSourceFile in = JSSourceFile.fromCode("in.js", "var z = 0;");
    compiler.compile(new JSSourceFile[] { ext }, new JSSourceFile[] { in }, new CompilerOptions());

    Assert.assertEquals(1, compiler.getInputsForTesting().size());
    Assert.assertEquals(1, compiler.getExternsForTesting().size());
    Assert.assertEquals(1, compiler.getInputsInOrder().size());
    Assert.assertNotNull(compiler.getRoot());
  }

  @Test
  public void testResetUniqueNameId() {
    Compiler compiler = new Compiler();
    compiler.resetUniqueNameId();
    Assert.assertEquals("0", compiler.getUniqueNameIdSupplier().get());
    Assert.assertEquals("1", compiler.getUniqueNameIdSupplier().get());
    compiler.resetUniqueNameId();
    Assert.assertEquals("0", compiler.getUniqueNameIdSupplier().get());
  }
}
