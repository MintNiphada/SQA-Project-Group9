package com.google.javascript.jscomp;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.AbstractCommandLineRunner.CommandLineConfig;
import com.google.javascript.jscomp.AbstractCommandLineRunner.FlagUsageException;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.CompilerOptions.TweakProcessing;
import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbstractCommandLineRunnerTest {

  private static class TestRunner extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
    private Compiler mockCompiler;
    private CompilerOptions mockOptions;

    TestRunner() {
      super();
    }

    TestRunner(PrintStream out, PrintStream err) {
      super(out, err);
    }

    void setMockCompiler(Compiler compiler) {
      this.mockCompiler = compiler;
    }

    void setMockOptions(CompilerOptions options) {
      this.mockOptions = options;
    }

    @Override
    protected Compiler createCompiler() {
      return mockCompiler != null ? mockCompiler : new Compiler();
    }

    @Override
    protected CompilerOptions createOptions() {
      return mockOptions != null ? mockOptions : new CompilerOptions();
    }
  }

  private TestRunner runner;
  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    runner = new TestRunner(new PrintStream(outStream), new PrintStream(errStream));
  }

  @Test
  public void testFlagUsageExceptionMessage() {
    FlagUsageException ex = new FlagUsageException("custom error message");
    Assert.assertEquals("custom error message", ex.getMessage());
  }

  @Test
  public void testCreateDefineOrTweakReplacementsBooleans() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_TRUE=true", "DEF_FALSE=false", "DEF_IMPLICIT");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);

    List<String> tweaks = Lists.newArrayList("TWK_TRUE=true", "TWK_FALSE=false", "TWK_IMPLICIT");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  @Test
  public void testCreateDefineOrTweakReplacementsStrings() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_STR='val'", "DEF_DSTR=\"val2\"");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);

    List<String> tweaks = Lists.newArrayList("TWK_STR='val'", "TWK_DSTR=\"val2\"");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  @Test
  public void testCreateDefineOrTweakReplacementsNumbers() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_NUM=123.45", "DEF_INT=42");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);

    List<String> tweaks = Lists.newArrayList("TWK_NUM=123.45", "TWK_INT=42");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsInvalid() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("DEF_BAD=bad_value_without_quotes");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateTweakReplacementsInvalid() {
    CompilerOptions options = new CompilerOptions();
    List<String> tweaks = Lists.newArrayList("TWK_BAD='unclosed");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(tweaks, options, true);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsEmptyDef() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList("");
    AbstractCommandLineRunner.createDefineOrTweakReplacements(defs, options, false);
  }

  @Test
  public void testParseModuleWrappers() throws Exception {
    List<JSModule> modules = Lists.newArrayList(new JSModule("m1"), new JSModule("m2"));
    List<String> specs = Lists.newArrayList("m1:(function(){%s})();");
    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    Assert.assertEquals(2, wrappers.size());
    Assert.assertEquals("(function(){%s})();", wrappers.get("m1"));
    Assert.assertEquals("", wrappers.get("m2"));
  }

  @Test(expected = FlagUsageException.class)
  public void testParseModuleWrappersInvalidFormat() throws Exception {
    List<JSModule> modules = Lists.newArrayList(new JSModule("m1"));
    List<String> specs = Lists.newArrayList("m1_wrapper_without_colon");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  @Test(expected = FlagUsageException.class)
  public void testParseModuleWrappersUnknownModule() throws Exception {
    List<JSModule> modules = Lists.newArrayList(new JSModule("m1"));
    List<String> specs = Lists.newArrayList("unknown_mod:%s");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  @Test(expected = FlagUsageException.class)
  public void testParseModuleWrappersMissingPlaceholder() throws Exception {
    List<JSModule> modules = Lists.newArrayList(new JSModule("m1"));
    List<String> specs = Lists.newArrayList("m1:no_placeholder_here");
    AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
  }

  @Test
  public void testWriteOutputWithWrapper() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "var a = 1;", "prefix(%output%)suffix", "%output%");
    Assert.assertEquals("prefix(var a = 1;)suffix\n", sb.toString());
  }

  @Test
  public void testWriteOutputWithoutWrapper() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "var a = 1;", "", "%output%");
    Assert.assertEquals("var a = 1;\n", sb.toString());
  }

  @Test
  public void testWriteOutputPrefixOnly() throws IOException {
    StringBuilder sb = new StringBuilder();
    AbstractCommandLineRunner.writeOutput(sb, null, "var a = 1;", "prefix:%output%", "%output%");
    Assert.assertEquals("prefix:var a = 1;\n", sb.toString());
  }

  @Test
  public void testCheckModuleNameValid() throws Exception {
    runner.checkModuleName("validModule_123");
  }

  @Test(expected = FlagUsageException.class)
  public void testCheckModuleNameInvalid() throws Exception {
    runner.checkModuleName("invalid-module.name");
  }

  @Test
  public void testCreateJsModulesSuccess() throws Exception {
    List<String> specs = Lists.newArrayList("m1:1", "m2:2:m1");
    List<String> jsFiles = Lists.newArrayList("file1.js", "file2.js", "file3.js");
    List<JSModule> modules = runner.createJsModules(specs, jsFiles);
    Assert.assertEquals(2, modules.size());
    Assert.assertEquals("m1", modules.get(0).getName());
    Assert.assertEquals("m2", modules.get(1).getName());
    Assert.assertEquals(1, modules.get(0).getInputs().size());
    Assert.assertEquals(2, modules.get(1).getInputs().size());
    Assert.assertTrue(modules.get(1).getDependencies().contains(modules.get(0)));
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateJsModulesInvalidParts() throws Exception {
    runner.createJsModules(Lists.newArrayList("m1"), Lists.newArrayList("file1.js"));
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateJsModulesDuplicateName() throws Exception {
    runner.createJsModules(Lists.newArrayList("m1:1", "m1:1"), Lists.newArrayList("file1.js", "file2.js"));
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateJsModulesInvalidCount() throws Exception {
    runner.createJsModules(Lists.newArrayList("m1:abc"), Lists.newArrayList("file1.js"));
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateJsModulesNotEnoughFiles() throws Exception {
    runner.createJsModules(Lists.newArrayList("m1:2"), Lists.newArrayList("file1.js"));
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateJsModulesTooManyFiles() throws Exception {
    runner.createJsModules(Lists.newArrayList("m1:1"), Lists.newArrayList("file1.js", "file2.js"));
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateJsModulesUnknownDep() throws Exception {
    runner.createJsModules(Lists.newArrayList("m1:1:unknownModule"), Lists.newArrayList("file1.js"));
  }

  @Test
  public void testCreateInputsAllowStdin() throws Exception {
    List<String> files = Lists.newArrayList("-");
    List<JSSourceFile> inputs = runner.createInputs(files, true);
    Assert.assertEquals(1, inputs.size());
    Assert.assertEquals("stdin", inputs.get(0).getName());
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateInputsDisallowStdin() throws Exception {
    runner.createInputs(Lists.newArrayList("-"), false);
  }

  @Test(expected = FlagUsageException.class)
  public void testCreateInputsDuplicateStdin() throws Exception {
    runner.createInputs(Lists.newArrayList("-", "-"), true);
  }

  @Test
  public void testExpandCommandLinePaths() {
    CommandLineConfig config = runner.getCommandLineConfig();
    config.setJsOutputFile("out.js");
    config.setModuleOutputPathPrefix("mod_");
    config.setOutputManifest("%outname%.manifest");
    CompilerOptions options = new CompilerOptions();
    options.sourceMapOutputPath = "%outname%.map";

    Assert.assertEquals("out.js.map", runner.expandSourceMapPath(options, null));
    Assert.assertEquals("out.js.manifest", runner.expandManifest(null));

    JSModule mod = new JSModule("core");
    Assert.assertEquals("mod_core.js.map", runner.expandSourceMapPath(options, mod));
    Assert.assertEquals("mod_core.js.manifest", runner.expandManifest(mod));
  }

  @Test
  public void testSetRunOptionsLanguageModes() throws Exception {
    CommandLineConfig config = runner.getCommandLineConfig();
    CompilerOptions options = new CompilerOptions();

    config.setLanguageIn("ECMASCRIPT5");
    runner.setRunOptions(options);
    Assert.assertEquals(LanguageMode.ECMASCRIPT5, options.getLanguageIn());

    config.setLanguageIn("ES5_STRICT");
    runner.setRunOptions(options);
    Assert.assertEquals(LanguageMode.ECMASCRIPT5, options.getLanguageIn());

    config.setLanguageIn("ECMASCRIPT3");
    runner.setRunOptions(options);
    Assert.assertEquals(LanguageMode.ECMASCRIPT3, options.getLanguageIn());
  }

  @Test(expected = FlagUsageException.class)
  public void testSetRunOptionsInvalidLanguage() throws Exception {
    CommandLineConfig config = runner.getCommandLineConfig();
    CompilerOptions options = new CompilerOptions();
    config.setLanguageIn("FORTRAN");
    runner.setRunOptions(options);
  }

  @Test(expected = FlagUsageException.class)
  public void testSetRunOptionsInvalidCharset() throws Exception {
    CommandLineConfig config = runner.getCommandLineConfig();
    CompilerOptions options = new CompilerOptions();
    config.setCharset("INVALID_CHARSET_NAME_123");
    runner.setRunOptions(options);
  }

  @Test
  public void testCommandLineConfigSetters() {
    CommandLineConfig config = new CommandLineConfig();
    config.setPrintTree(true)
          .setComputePhaseOrdering(true)
          .setPrintAst(true)
          .setPrintPassGraph(true)
          .setJscompDevMode(CompilerOptions.DevMode.EVERY_PASS)
          .setLoggingLevel("INFO")
          .setExterns(Collections.singletonList("ext.js"))
          .setJs(Collections.singletonList("src.js"))
          .setJsOutputFile("bundle.js")
          .setModule(Collections.singletonList("m:1"))
          .setVariableMapInputFile("var.in")
          .setPropertyMapInputFile("prop.in")
          .setVariableMapOutputFile("var.out")
          .setPropertyMapOutputFile("prop.out")
          .setCreateNameMapFiles(true)
          .setCodingConvention(new ClosureCodingConvention())
          .setSummaryDetailLevel(2)
          .setOutputWrapper("(function(){%output%})()")
          .setModuleWrapper(Collections.singletonList("m:%s"))
          .setModuleOutputPathPrefix("dist/")
          .setCreateSourceMap("map.out")
          .setSourceMapDetailLevel(SourceMap.DetailLevel.SYMBOLS)
          .setSourceMapFormat(SourceMap.Format.V3)
          .setJscompError(Collections.singletonList("checkVars"))
          .setJscompWarning(Collections.singletonList("checkTypes"))
          .setJscompOff(Collections.singletonList("deprecated"))
          .setDefine(Collections.singletonList("FLAG=true"))
          .setTweak(Collections.singletonList("TWK=1"))
          .setTweakProcessing(TweakProcessing.CHECK)
          .setCharset("UTF-8")
          .setManageClosureDependencies(true)
          .setClosureEntryPoints(Collections.singletonList("goog.dom"))
          .setOutputManifest("manifest.MF")
          .setAcceptConstKeyword(true)
          .setLanguageIn("ES5");
    Assert.assertNotNull(config);
  }

  @Test
  public void testTestModeEnablingAndExecution() {
    final int[] exitCode = new int[]{-999};
    Supplier<List<JSSourceFile>> externsSupplier = () -> Lists.newArrayList();
    Supplier<List<JSSourceFile>> inputsSupplier = () -> Lists.newArrayList(JSSourceFile.fromCode("in.js", "var x = 1;"));
    Function<Integer, Boolean> exitReceiver = code -> {
      exitCode[0] = code;
      return true;
    };

    runner.enableTestMode(externsSupplier, inputsSupplier, null, exitReceiver);
    Assert.assertTrue(runner.isInTestMode());
    runner.run();
    Assert.assertEquals(0, exitCode[0]);
  }

  @Test
  public void testPrintModuleGraphManifestTo() throws IOException {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    m1.add(JSSourceFile.fromCode("m1_f1.js", ""));
    m2.add(JSSourceFile.fromCode("m2_f1.js", ""));

    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{m1, m2});
    StringBuilder sb = new StringBuilder();
    runner.printModuleGraphManifestTo(graph, sb);

    String manifest = sb.toString();
    Assert.assertTrue(manifest.contains("{m1}"));
    Assert.assertTrue(manifest.contains("{m2:m1}"));
    Assert.assertTrue(manifest.contains("m1_f1.js"));
    Assert.assertTrue(manifest.contains("m2_f1.js"));
  }

  @Test
  public void testProcessResultsPrintTreeNullRoot() throws Exception {
    Compiler compiler = new Compiler();
    runner.setMockCompiler(compiler);
    CommandLineConfig config = runner.getCommandLineConfig();
    config.setPrintTree(true);

    Result result = new Result(new JSError[0], new JSError[0], "", null, null, null, null);
    int code = runner.processResults(result, null, new CompilerOptions());
    Assert.assertEquals(1, code);
  }

  @Test
  public void testProcessResultsPrintAstNullRoot() throws Exception {
    Compiler compiler = new Compiler();
    runner.setMockCompiler(compiler);
    CommandLineConfig config = runner.getCommandLineConfig();
    config.setPrintAst(true);

    Result result = new Result(new JSError[0], new JSError[0], "", null, null, null, null);
    int code = runner.processResults(result, null, new CompilerOptions());
    Assert.assertEquals(1, code);
  }

  @Test
  public void testProcessResultsPrintPassGraphNullRoot() throws Exception {
    Compiler compiler = new Compiler();
    runner.setMockCompiler(compiler);
    CommandLineConfig config = runner.getCommandLineConfig();
    config.setPrintPassGraph(true);

    Result result = new Result(new JSError[0], new JSError[0], "", null, null, null, null);
    int code = runner.processResults(result, null, new CompilerOptions());
    Assert.assertEquals(1, code);
  }

  @Test
  public void testInitOptionsFromFlags() {
    runner.initOptionsFromFlags(new CompilerOptions());
  }

  @Test
  public void testGetDiagnosticGroups() {
    DiagnosticGroups groups = runner.getDiagnosticGroups();
    Assert.assertNotNull(groups);
  }

  @Test
  public void testGetErrorPrintStream() {
    PrintStream stream = runner.getErrorPrintStream();
    Assert.assertNotNull(stream);
  }
}
