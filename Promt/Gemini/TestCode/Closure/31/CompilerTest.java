package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  public void testCompileBasic() {
    SourceFile extern = SourceFile.fromCode("externs.js", "var window;");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1; function foo() { return x; }");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
    Assert.assertEquals(0, compiler.getErrorCount());
    Assert.assertNotNull(compiler.toSource());
  }

  @Test
  public void testCompileModules() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var a = 10;"));
    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var b = a + 1;"));
    m2.addDependency(m1);

    Result result = compiler.compileModules(
        ImmutableList.of(extern),
        ImmutableList.of(m1, m2),
        options);

    Assert.assertTrue(result.success);
    Assert.assertEquals(0, compiler.getErrorCount());
    String srcM1 = compiler.toSource(m1);
    Assert.assertTrue(srcM1.contains("var a"));
    String[] srcM1Array = compiler.toSourceArray(m1);
    Assert.assertEquals(1, srcM1Array.length);
  }

  @Test
  public void testCustomErrorManager() {
    BasicErrorManager customManager = new BasicErrorManager() {
      @Override
      public void println(CheckLevel level, JSError error) {}
      @Override
      protected void printSummary() {}
    };
    Compiler customCompiler = new Compiler(customManager);
    Assert.assertSame(customManager, customCompiler.getErrorManager());
  }

  @Test
  public void testPrintStreamConstructor() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler customCompiler = new Compiler(ps);
    customCompiler.initOptions(options);
    Assert.assertNotNull(customCompiler.getErrorManager());
  }

  @Test
  public void testDisableThreads() {
    compiler.disableThreads();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var y = 2;");
    Result result = compiler.compile(extern, input, options);
    Assert.assertTrue(result.success);
  }

  @Test
  public void testUniqueNameIdSupplier() {
    compiler.resetUniqueNameId();
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    Assert.assertEquals("0", supplier.get());
    Assert.assertEquals("1", supplier.get());
  }

  @Test
  public void testProgressSettings() {
    compiler.setProgress(-0.5);
    Assert.assertEquals(0.0, compiler.getProgress(), 0.0001);
    compiler.setProgress(0.5);
    Assert.assertEquals(0.5, compiler.getProgress(), 0.0001);
    compiler.setProgress(1.5);
    Assert.assertEquals(1.0, compiler.getProgress(), 0.0001);
  }

  @Test
  public void testCodeBuilder() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("hello\nworld");
    Assert.assertEquals("hello\nworld", cb.toString());
    Assert.assertEquals(11, cb.getLength());
    Assert.assertEquals(1, cb.getLineIndex());
    Assert.assertEquals(5, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith("world"));
    Assert.assertFalse(cb.endsWith("hello"));
    cb.reset();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals("", cb.toString());
  }

  @Test
  public void testParseSyntheticCode() {
    Node node = compiler.parseSyntheticCode("synthetic.js", "var synth = 123;");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testParseTestCode() {
    Node node = compiler.parseTestCode("function test() {}");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testGetSourceLineAndRegion() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "line1\nline2\nline3");
    compiler.compile(extern, input, options);

    Assert.assertNull(compiler.getSourceLine("input.js", 0));
    Assert.assertEquals("line1", compiler.getSourceLine("input.js", 1));
    Assert.assertEquals("line2", compiler.getSourceLine("input.js", 2));
    Assert.assertNull(compiler.getSourceLine("nonexistent.js", 1));

    Assert.assertNull(compiler.getSourceRegion("input.js", 0));
    Assert.assertNotNull(compiler.getSourceRegion("input.js", 1));
    Assert.assertNull(compiler.getSourceRegion("nonexistent.js", 1));
  }

  @Test
  public void testIntermediateStateSaveRestore() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);

    Compiler.IntermediateState state = compiler.getState();
    Assert.assertNotNull(state);

    Compiler newComp = new Compiler();
    newComp.init(
        Lists.newArrayList(extern),
        Lists.newArrayList(input),
        options);
    newComp.setState(state);
    Assert.assertEquals(compiler.getLifeCycleStage(), newComp.getLifeCycleStage());
  }

  @Test
  public void testDiagnosticGroupsInitOptions() {
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.ERROR);
    compiler.initOptions(options);
    Assert.assertTrue(options.checkTypes);
    Assert.assertTrue(compiler.isTypeCheckingEnabled());
  }

  @Test
  public void testLanguageModesAndParserConfig() {
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);
    Assert.assertTrue(compiler.acceptEcmaScript5());
    Assert.assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
    Assert.assertNotNull(compiler.getParserConfig());
  }

  @Test
  public void testCheckLevelReporting() {
    compiler.initOptions(options);
    JSError error = JSError.make("test.js", 1, 1, CheckLevel.ERROR, Compiler.DUPLICATE_INPUT, "dummy");
    compiler.report(error);
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertEquals(1, compiler.getErrors().length);
    Assert.assertEquals(1, compiler.getMessages().length);
    Assert.assertTrue(compiler.hasErrors());
  }

  @Test
  public void testPassConfigSettings() {
    PassConfig passConfig = compiler.createPassConfigInternal();
    compiler.setPassConfig(passConfig);
    Assert.assertSame(passConfig, compiler.getPassConfig());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPassConfigTwiceThrows() {
    PassConfig passConfig = compiler.createPassConfigInternal();
    compiler.setPassConfig(passConfig);
    compiler.setPassConfig(passConfig);
  }

  @Test
  public void testAddAndRemoveChangeHandler() {
    final int[] changeCount = new int[1];
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
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
  public void testAreNodesEqualForInlining() {
    Node n1 = Node.newString("foo");
    Node n2 = Node.newString("foo");
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = true;
    compiler.initOptions(options);
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
  }

  @Test
  public void testRegExpGlobalReferences() {
    compiler.setHasRegExpGlobalReferences(false);
    Assert.assertFalse(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(true);
    Assert.assertTrue(compiler.hasRegExpGlobalReferences());
  }

  @Test
  public void testLoggingLevel() {
    Compiler.setLoggingLevel(Level.WARNING);
  }

  @Test
  public void testToSourceArray() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input1 = SourceFile.fromCode("input1.js", "var a = 1;");
    SourceFile input2 = SourceFile.fromCode("input2.js", "var b = 2;");
    compiler.compile(
        Lists.newArrayList(extern),
        Lists.newArrayList(input1, input2),
        options);
    String[] sources = compiler.toSourceArray();
    Assert.assertEquals(2, sources.length);
    Assert.assertTrue(sources[0].contains("var a"));
    Assert.assertTrue(sources[1].contains("var b"));
  }

  @Test
  public void testNewExternInput() {
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1;");
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();

    CompilerInput newInput = compiler.newExternInput("synthetic_extern.js");
    Assert.assertNotNull(newInput);
    Assert.assertEquals("synthetic_extern.js", newInput.getName());
    Assert.assertNotNull(compiler.getInput(new InputId("synthetic_extern.js")));
  }

  @Test
  public void testRemoveExternInput() {
    SourceFile extern = SourceFile.fromCode("externs.js", "var externVal;");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1;");
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();

    InputId externId = new InputId("externs.js");
    Assert.assertNotNull(compiler.getInput(externId));
    compiler.removeExternInput(externId);
    Assert.assertNull(compiler.getInput(externId));
  }

  @Test
  public void testEmptyModuleListError() {
    compiler.initModules(
        Collections.<SourceFile>emptyList(),
        Collections.<JSModule>emptyList(),
        options);
    Assert.assertEquals(1, compiler.getErrorCount());
  }
}