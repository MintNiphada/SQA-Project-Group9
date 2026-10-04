package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbstractCommandLineRunnerTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private static class DummyRunner extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
    private final Compiler compilerInstance;
    private final CompilerOptions optionsInstance;

    DummyRunner(Compiler compiler, CompilerOptions options, PrintStream out, PrintStream err) {
      super(out, err);
      this.compilerInstance = compiler;
      this.optionsInstance = options;
    }

    DummyRunner() {
      super();
      this.compilerInstance = new Compiler();
      this.optionsInstance = new CompilerOptions();
    }

    @Override
    protected Compiler createCompiler() {
      return compilerInstance;
    }

    @Override
    protected CompilerOptions createOptions() {
      return optionsInstance;
    }

    public int runDoRun() throws Exception {
      return doRun();
    }
  }

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream out;
  private PrintStream err;

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    out = new PrintStream(outStream);
    err = new PrintStream(errStream);
  }

  @Test
  public void testCreateDefineReplacementsBoolean() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_FLAG", "DEF_TRUE=true", "DEF_FALSE=false");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  @Test
  public void testCreateDefineReplacementsString() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_STR='hello'", "EMPTY_STR=''");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  @Test
  public void testCreateDefineReplacementsNumber() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_INT=42", "DEF_DOUBLE=3.1415", "DEF_NEG=-10.5");
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsEmptyDefName() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(Lists.newArrayList("=value"), options);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsInvalidDouble() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(Lists.newArrayList("DEF=not_a_number_or_bool"), options);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsNestedQuotes() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(Lists.newArrayList("DEF='a'b'"), options);
  }

  @Test
  public void testParseModuleWrappersValid() throws Exception {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    JSModule[] modules = new JSModule[]{m1, m2};
    List<String> specs = Lists.newArrayList("m1:(function(){%s})();");
    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    Assert.assertEquals("(function(){%s})();", wrappers.get("m1"));
    Assert.assertEquals("", wrappers.get("m2"));
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappersMissingColon() throws Exception {
    JSModule m1 = new JSModule("m1");
    AbstractCommandLineRunner.parseModuleWrappers(Lists.newArrayList("m1_no_colon"), new JSModule[]{m1});
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappersUnknownModule() throws Exception {
    JSModule m1 = new JSModule("m1");
    AbstractCommandLineRunner.parseModuleWrappers(Lists.newArrayList("unknown:%s"), new JSModule[]{m1});
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappersMissingPlaceholder() throws Exception {
    JSModule m1 = new JSModule("m1");
    AbstractCommandLineRunner.parseModuleWrappers(Lists.newArrayList("m1:no_placeholder"), new JSModule[]{m1});
  }

  @Test
  public void testCreateJsModulesValid() throws Exception {
    File f1 = tmp.newFile("f1.js");
    File f2 = tmp.newFile("f2.js");
    File f3 = tmp.newFile("f3.js");
    List<String> jsFiles = Lists.newArrayList(f1.getAbsolutePath(), f2.getAbsolutePath(), f3.getAbsolutePath());
    List<String> specs = Lists.newArrayList("mod1:2", "mod2:1:mod1");
    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    Assert.assertEquals(2, modules.length);
    Assert.assertEquals("mod1", modules[0].getName());
    Assert.assertEquals(2, modules[0].getInputs().size());
    Assert.assertEquals("mod2", modules[1].getName());
    Assert.assertEquals(1, modules[1].getInputs().size());
    Assert.assertTrue(modules[1].getDependencies().contains(modules[0]));
  }

  @Test
  public void testCreateJsModulesWithTrailingColon() throws Exception {
    File f1 = tmp.newFile("f1.js");
    List<String> jsFiles = Lists.newArrayList(f1.getAbsolutePath());
    List<String> specs = Lists.newArrayList("mod1:1::");
    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, jsFiles);
    Assert.assertEquals(1, modules.length);
    Assert.assertEquals("mod1", modules[0].getName());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesInvalidPartsCount() throws Exception {
    AbstractCommandLineRunner.createJsModules(Lists.newArrayList("mod1"), Lists.<String>newArrayList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesInvalidModuleName() throws Exception {
    AbstractCommandLineRunner.createJsModules(Lists.newArrayList("123invalid:0"), Lists.<String>newArrayList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesDuplicateName() throws Exception {
    AbstractCommandLineRunner.createJsModules(Lists.newArrayList("m:0", "m:0"), Lists.<String>newArrayList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesInvalidNumber() throws Exception {
    AbstractCommandLineRunner.createJsModules(Lists.newArrayList("m:abc"), Lists.<String>newArrayList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesNotEnoughFiles() throws Exception {
    AbstractCommandLineRunner.createJsModules(Lists.newArrayList("m:2"), Lists.<String>newArrayList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesTooManyFiles() throws Exception {
    File f1 = tmp.newFile("f1.js");
    AbstractCommandLineRunner.createJsModules(Lists.newArrayList("m:0"), Lists.newArrayList(f1.getAbsolutePath()));
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesUnknownDep() throws Exception {
    AbstractCommandLineRunner.createJsModules(Lists.newArrayList("m:0:nonexistent"), Lists.<String>newArrayList());
  }

  @Test
  public void testWriteOutputWithWrapper() {
    Compiler compiler = new Compiler();
    AbstractCommandLineRunner.writeOutput(out, compiler, "var x = 1;", "/* prefix */%s/* suffix */", "%s");
    String output = outStream.toString();
    Assert.assertTrue(output.contains("/* prefix */var x = 1;/* suffix */"));
  }

  @Test
  public void testWriteOutputWithWrapperPlaceholderAtEnd() {
    Compiler compiler = new Compiler();
    AbstractCommandLineRunner.writeOutput(out, compiler, "var x = 1;", "prefix:%s", "%s");
    String output = outStream.toString();
    Assert.assertTrue(output.startsWith("prefix:var x = 1;"));
  }

  @Test
  public void testWriteOutputNoPlaceholder() {
    Compiler compiler = new Compiler();
    AbstractCommandLineRunner.writeOutput(out, compiler, "var x = 1;", "no placeholder", "%s");
    String output = outStream.toString().trim();
    Assert.assertEquals("var x = 1;", output);
  }

  @Test
  public void testCommandLineConfigSetters() {
    AbstractCommandLineRunner.CommandLineConfig config = new AbstractCommandLineRunner.CommandLineConfig();
    config.setPrintTree(true)
          .setComputePhaseOrdering(true)
          .setPrintAst(true)
          .setPrintPassGraph(true)
          .setJscompDevMode(CompilerOptions.DevMode.EVERY_PASS)
          .setLoggingLevel("INFO")
          .setExterns(Collections.singletonList("ext.js"))
          .setJs(Collections.singletonList("src.js"))
          .setJsOutputFile("out.js")
          .setModule(Collections.singletonList("m:1"))
          .setVariableMapInputFile("vars.in")
          .setPropertyMapInputFile("props.in")
          .setVariableMapOutputFile("vars.out")
          .setPropertyMapOutputFile("props.out")
          .setCreateNameMapFiles(true)
          .setCodingConvention(new ClosureCodingConvention())
          .setSummaryDetailLevel(2)
          .setOutputWrapper("(%output%)")
          .setOutputWrapperMarker("%output%")
          .setModuleWrapper(Collections.singletonList("m:%s"))
          .setModuleOutputPathPrefix("mod_")
          .setCreateSourceMap("map.out")
          .setJscompError(Collections.singletonList("checkVars"))
          .setJscompWarning(Collections.singletonList("checkTypes"))
          .setJscompOff(Collections.singletonList("deprecated"))
          .setDefine(Collections.singletonList("FLAG=true"))
          .setCharset("UTF-8");

    Assert.assertNotNull(config);
  }

  @Test
  public void testInitOptionsFromFlags() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    DummyRunner runner = new DummyRunner(compiler, options, out, err);
    runner.getCommandLineConfig()
          .setJscompError(Collections.singletonList("checkVars"))
          .setJscompWarning(Collections.singletonList("checkTypes"))
          .setJscompOff(Collections.singletonList("deprecated"))
          .setDefine(Collections.singletonList("FLAG=true"));
    runner.initOptionsFromFlags(options);
    Assert.assertEquals(CheckLevel.ERROR, options.getWarningLevel(DiagnosticGroups.CHECK_VARIABLES));
    Assert.assertEquals(CheckLevel.WARNING, options.getWarningLevel(DiagnosticGroups.CHECK_TYPES));
    Assert.assertEquals(CheckLevel.OFF, options.getWarningLevel(DiagnosticGroups.DEPRECATED));
  }

  @Test
  public void testSetRunOptions() throws Exception {
    File varMapIn = tmp.newFile("vars_in.txt");
    VariableMap vm = new VariableMap(Collections.singletonMap("a", "b"));
    vm.save(varMapIn.getAbsolutePath());

    File propMapIn = tmp.newFile("props_in.txt");
    VariableMap pm = new VariableMap(Collections.singletonMap("x", "y"));
    pm.save(propMapIn.getAbsolutePath());

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    DummyRunner runner = new DummyRunner(compiler, options, out, err);
    runner.getCommandLineConfig()
          .setJsOutputFile("output.js")
          .setCreateSourceMap("source.map")
          .setVariableMapInputFile(varMapIn.getAbsolutePath())
          .setPropertyMapInputFile(propMapIn.getAbsolutePath())
          .setCodingConvention(new ClosureCodingConvention())
          .setSummaryDetailLevel(3)
          .setCharset("UTF-8");

    runner.setRunOptions(options);
    Assert.assertEquals("output.js", options.jsOutputFile);
    Assert.assertEquals("source.map", options.sourceMapOutputPath);
    Assert.assertNotNull(options.inputVariableMapSerialized);
    Assert.assertNotNull(options.inputPropertyMapSerialized);
    Assert.assertTrue(options.getCodingConvention() instanceof ClosureCodingConvention);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testSetRunOptionsInvalidCharset() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    DummyRunner runner = new DummyRunner(compiler, options, out, err);
    runner.getCommandLineConfig().setCharset("INVALID_CHARSET_NAME_123");
    runner.setRunOptions(options);
  }

  @Test
  public void testDoRunWithEmptyInputs() throws Exception {
    File externFile = tmp.newFile("extern.js");
    File jsFile = tmp.newFile("input.js");
    FileOutputStream fos = new FileOutputStream(jsFile);
    fos.write("var a = 1;".getBytes());
    fos.close();

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    DummyRunner runner = new DummyRunner(compiler, options, out, err);
    runner.getCommandLineConfig()
          .setExterns(Collections.singletonList(externFile.getAbsolutePath()))
          .setJs(Collections.singletonList(jsFile.getAbsolutePath()));

    int exitCode = runner.runDoRun();
    Assert.assertEquals(0, exitCode);
    Assert.assertSame(compiler, runner.getCompiler());
    Assert.assertSame(err, runner.getErrorPrintStream());
  }

  @Test
  public void testDoRunWithModules() throws Exception {
    File jsFile = tmp.newFile("mod_input.js");
    FileOutputStream fos = new FileOutputStream(jsFile);
    fos.write("var a = 1;".getBytes());
    fos.close();

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    DummyRunner runner = new DummyRunner(compiler, options, out, err);
    runner.getCommandLineConfig()
          .setModule(Collections.singletonList("m1:1"))
          .setJs(Collections.singletonList(jsFile.getAbsolutePath()))
          .setModuleOutputPathPrefix(tmp.getRoot().getAbsolutePath() + File.separator + "out_");

    int exitCode = runner.runDoRun();
    Assert.assertEquals(0, exitCode);
  }

  @Test
  public void testProcessResultsPrintFlags() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    DummyRunner runner = new DummyRunner(compiler, options, out, err);

    runner.getCommandLineConfig().setComputePhaseOrdering(true);
    Assert.assertEquals(0, runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options));

    runner.getCommandLineConfig().setComputePhaseOrdering(false).setPrintPassGraph(true);
    Assert.assertEquals(1, runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options));

    runner.getCommandLineConfig().setPrintPassGraph(false).setPrintAst(true);
    Assert.assertEquals(1, runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options));

    runner.getCommandLineConfig().setPrintAst(false).setPrintTree(true);
    Assert.assertEquals(1, runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options));
  }

  @Test
  public void testProcessResultsWithOutputMaps() throws Exception {
    File jsOut = tmp.newFile("final_out.js");
    File varMapOut = tmp.newFile("vars_out.txt");
    File propMapOut = tmp.newFile("props_out.txt");

    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.jsOutputFile = jsOut.getAbsolutePath();

    DummyRunner runner = new DummyRunner(compiler, options, out, err);
    runner.getCommandLineConfig()
          .setVariableMapOutputFile(varMapOut.getAbsolutePath())
          .setPropertyMapOutputFile(propMapOut.getAbsolutePath());

    Result result = new Result(new JSError[0], new JSError[0], null, null, null, null, null);
    int code = runner.processResults(result, null, options);
    Assert.assertEquals(0, code);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testProcessResultsConflictMapFlags() throws Exception {
    File jsOut = tmp.newFile("conflict_out.js");
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.jsOutputFile = jsOut.getAbsolutePath();

    DummyRunner runner = new DummyRunner(compiler, options, out, err);
    runner.getCommandLineConfig()
          .setCreateNameMapFiles(true)
          .setVariableMapOutputFile("custom_vars.out");

    Result result = new Result(new JSError[0], new JSError[0], null, null, null, null, null);
    runner.processResults(result, null, options);
  }

  @Test
  public void testDefaultConstructor() {
    DummyRunner runner = new DummyRunner();
    Assert.assertNotNull(runner.getCommandLineConfig());
    Assert.assertNotNull(runner.getErrorPrintStream());
  }

  @Test
  public void testCreateExternsEmpty() throws Exception {
    DummyRunner runner = new DummyRunner();
    List<JSSourceFile> externs = runner.createExterns();
    Assert.assertEquals(1, externs.size());
    Assert.assertEquals("/dev/null", externs.get(0).getName());
  }
}
