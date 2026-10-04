package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AbstractCommandLineRunnerTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private static class TestRunner extends AbstractCommandLineRunner<Compiler, CompilerOptions> {
    private Compiler customCompiler;
    private CompilerOptions customOptions;

    TestRunner() {
      super();
    }

    TestRunner(PrintStream out, PrintStream err) {
      super(out, err);
    }

    @Override
    protected Compiler createCompiler() {
      if (customCompiler != null) {
        return customCompiler;
      }
      return new Compiler();
    }

    @Override
    protected CompilerOptions createOptions() {
      if (customOptions != null) {
        return customOptions;
      }
      return new CompilerOptions();
    }

    void setCustomCompiler(Compiler c) {
      this.customCompiler = c;
    }

    void setCustomOptions(CompilerOptions o) {
      this.customOptions = o;
    }
  }

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream outPrint;
  private PrintStream errPrint;
  private TestRunner runner;

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    outPrint = new PrintStream(outStream);
    errPrint = new PrintStream(errStream);
    runner = new TestRunner(outPrint, errPrint);
  }

  @After
  public void tearDown() {
    outPrint.close();
    errPrint.close();
  }

  @Test
  public void testDefaultConstructor() {
    TestRunner defaultRunner = new TestRunner();
    Assert.assertNotNull(defaultRunner.getCommandLineConfig());
    Assert.assertNotNull(defaultRunner.getErrorPrintStream());
    Assert.assertNotNull(defaultRunner.getDiagnosticGroups());
    Assert.assertNull(defaultRunner.getCompiler());
  }

  @Test
  public void testCommandLineConfigChaining() {
    AbstractCommandLineRunner.CommandLineConfig config = runner.getCommandLineConfig();
    config.setPrintTree(true)
          .setComputePhaseOrdering(true)
          .setPrintAst(true)
          .setPrintPassGraph(true)
          .setJscompDevMode(CompilerOptions.DevMode.EVERY_PASS)
          .setLoggingLevel("INFO")
          .setExterns(ImmutableList.of("extern1.js"))
          .setJs(ImmutableList.of("in1.js"))
          .setJsOutputFile("out.js")
          .setModule(ImmutableList.of("m1:1"))
          .setVariableMapInputFile("var.in")
          .setPropertyMapInputFile("prop.in")
          .setVariableMapOutputFile("var.out")
          .setCreateNameMapFiles(true)
          .setPropertyMapOutputFile("prop.out")
          .setCodingConvention(new ClosureCodingConvention())
          .setSummaryDetailLevel(2)
          .setOutputWrapper("(function(){%output%})()")
          .setOutputWrapperMarker("%output%")
          .setModuleWrapper(ImmutableList.of("m1:%s"))
          .setModuleOutputPathPrefix("mod_")
          .setCreateSourceMap("map.out")
          .setSourceMapDetailLevel(SourceMap.DetailLevel.SYMBOLS)
          .setJscompError(ImmutableList.of("checkVars"))
          .setJscompWarning(ImmutableList.of("undefinedVars"))
          .setJscompOff(ImmutableList.of("fileoverviewTags"))
          .setDefine(ImmutableList.of("DEF=1"))
          .setCharset("UTF-8")
          .setManageClosureDependencies(true)
          .setOutputManifest("manifest.out");

    Assert.assertNotNull(config);
  }

  @Test
  public void testFlagUsageException() {
    AbstractCommandLineRunner.FlagUsageException e =
        new AbstractCommandLineRunner.FlagUsageException("test error");
    Assert.assertEquals("test error", e.getMessage());
  }

  @Test
  public void testCreateDefineReplacementsValid() {
    CompilerOptions options = new CompilerOptions();
    List<String> defs = Lists.newArrayList(
        "FLAG_BOOL_IMPLICIT",
        "FLAG_BOOL_T=true",
        "FLAG_BOOL_F=false",
        "FLAG_STR1='hello'",
        "FLAG_STR2=\"world\"",
        "FLAG_NUM=42.5",
        "FLAG_INT=100"
    );
    AbstractCommandLineRunner.createDefineReplacements(defs, options);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsInvalidFormat() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(ImmutableList.of("=invalid"), options);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsInvalidNumber() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(ImmutableList.of("FLAG=not_a_number"), options);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsInvalidQuotes() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(ImmutableList.of("FLAG='open_quote"), options);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDefineReplacementsNestedQuotes() {
    CompilerOptions options = new CompilerOptions();
    AbstractCommandLineRunner.createDefineReplacements(ImmutableList.of("FLAG='nested'quote'"), options);
  }

  @Test
  public void testParseModuleWrappersValid() throws Exception {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    JSModule[] modules = new JSModule[]{m1, m2};
    List<String> specs = ImmutableList.of("m1:(function(){%s})();");
    Map<String, String> wrappers = AbstractCommandLineRunner.parseModuleWrappers(specs, modules);
    Assert.assertEquals("(function(){%s})();", wrappers.get("m1"));
    Assert.assertEquals("", wrappers.get("m2"));
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappersNoColon() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("m1")};
    AbstractCommandLineRunner.parseModuleWrappers(ImmutableList.of("m1_wrapper"), modules);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappersUnknownModule() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("m1")};
    AbstractCommandLineRunner.parseModuleWrappers(ImmutableList.of("unknown:%s"), modules);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testParseModuleWrappersNoPlaceholder() throws Exception {
    JSModule[] modules = new JSModule[]{new JSModule("m1")};
    AbstractCommandLineRunner.parseModuleWrappers(ImmutableList.of("m1:no_placeholder"), modules);
  }

  @Test
  public void testCreateJsModulesValid() throws Exception {
    File f1 = tmp.newFile("f1.js");
    File f2 = tmp.newFile("f2.js");
    List<String> specs = ImmutableList.of("mod1:1", "mod2:1:mod1");
    List<String> files = ImmutableList.of(f1.getAbsolutePath(), f2.getAbsolutePath());
    JSModule[] modules = AbstractCommandLineRunner.createJsModules(specs, files);
    Assert.assertEquals(2, modules.length);
    Assert.assertEquals("mod1", modules[0].getName());
    Assert.assertEquals("mod2", modules[1].getName());
    Assert.assertTrue(modules[1].getDependencies().contains(modules[0]));
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesInvalidParts() throws Exception {
    AbstractCommandLineRunner.createJsModules(ImmutableList.of("one_part_only"), Collections.emptyList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesInvalidModuleName() throws Exception {
    AbstractCommandLineRunner.createJsModules(ImmutableList.of("not-a-valid-ident:1"), Collections.emptyList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesDuplicateName() throws Exception {
    AbstractCommandLineRunner.createJsModules(ImmutableList.of("m:0", "m:0"), Collections.emptyList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesInvalidCount() throws Exception {
    AbstractCommandLineRunner.createJsModules(ImmutableList.of("m:abc"), Collections.emptyList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesNotEnoughFiles() throws Exception {
    AbstractCommandLineRunner.createJsModules(ImmutableList.of("m:2"), Collections.emptyList());
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesTooManyFiles() throws Exception {
    File f1 = tmp.newFile("f1.js");
    File f2 = tmp.newFile("f2.js");
    AbstractCommandLineRunner.createJsModules(
        ImmutableList.of("m:1"),
        ImmutableList.of(f1.getAbsolutePath(), f2.getAbsolutePath()));
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testCreateJsModulesUnknownDependency() throws Exception {
    AbstractCommandLineRunner.createJsModules(ImmutableList.of("m:0:dep_unknown"), Collections.emptyList());
  }

  @Test
  public void testWriteOutputWithWrapper() throws Exception {
    StringWriter sw = new StringWriter();
    AbstractCommandLineRunner.writeOutput(sw, null, "var a = 1;", "prefix(%s)suffix", "%s");
    Assert.assertEquals("prefix(var a = 1;)suffix\n", sw.toString());
  }

  @Test
  public void testWriteOutputNoPlaceholder() throws Exception {
    StringWriter sw = new StringWriter();
    AbstractCommandLineRunner.writeOutput(sw, null, "var a = 1;", "no_placeholder", "%s");
    Assert.assertEquals("var a = 1;\n", sw.toString());
  }

  @Test
  public void testWriteOutputWithSourceMap() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.sourceMapOutputPath = "test.map";
    SourceMap sourceMap = new SourceMap();
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], options);

    StringWriter sw = new StringWriter();
    AbstractCommandLineRunner.writeOutput(sw, compiler, "var a = 1;", "wrapper(%s)", "%s");
    Assert.assertEquals("wrapper(var a = 1;)\n", sw.toString());
  }

  @Test
  public void testExpandPaths() {
    CompilerOptions options = new CompilerOptions();
    options.sourceMapOutputPath = "path/%outname%.map";
    runner.getCommandLineConfig().setJsOutputFile("bundle.js");
    runner.getCommandLineConfig().setOutputManifest("manifest_%outname%.txt");

    Assert.assertEquals("path/bundle.js.map", runner.expandSourceMapPath(options, null));
    Assert.assertEquals("manifest_bundle.js.txt", runner.expandManifest(null));

    JSModule mod = new JSModule("core");
    runner.getCommandLineConfig().setModuleOutputPathPrefix("dist/");
    Assert.assertEquals("path/dist/core.js.map", runner.expandSourceMapPath(options, mod));
    Assert.assertEquals("manifest_dist/core.js.txt", runner.expandManifest(mod));

    options.sourceMapOutputPath = "";
    Assert.assertNull(runner.expandSourceMapPath(options, null));
    runner.getCommandLineConfig().setOutputManifest("");
    Assert.assertNull(runner.expandManifest(null));
  }

  @Test
  public void testPrintModuleGraphManifestTo() throws Exception {
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);

    File f1 = tmp.newFile("f1.js");
    File f2 = tmp.newFile("f2.js");
    m1.add(JSSourceFile.fromFile(f1));
    m2.add(JSSourceFile.fromFile(f2));

    JSModuleGraph graph = new JSModuleGraph(new JSModule[]{m1, m2});
    StringWriter sw = new StringWriter();
    runner.printModuleGraphManifestTo(graph, sw);
    String result = sw.toString();

    Assert.assertTrue(result.contains("{m1}"));
    Assert.assertTrue(result.contains("{m2:m1}"));
    Assert.assertTrue(result.contains(f1.getName()));
    Assert.assertTrue(result.contains(f2.getName()));
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testSetRunOptionsInvalidCharset() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig().setCharset("INVALID_CHARSET_NAME_123");
    runner.setRunOptions(options);
  }

  @Test
  public void testSetRunOptionsVariableAndPropertyMaps() throws Exception {
    File varFile = tmp.newFile("vars.map");
    File propFile = tmp.newFile("props.map");
    VariableMap vm = new VariableMap(Collections.singletonMap("a", "b"));
    vm.save(varFile.getAbsolutePath());
    vm.save(propFile.getAbsolutePath());

    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
          .setVariableMapInputFile(varFile.getAbsolutePath())
          .setPropertyMapInputFile(propFile.getAbsolutePath())
          .setJsOutputFile("out.js")
          .setCreateSourceMap("out.map")
          .setSourceMapDetailLevel(SourceMap.DetailLevel.ALL);

    runner.setRunOptions(options);
    Assert.assertEquals("out.js", options.jsOutputFile);
    Assert.assertEquals("out.map", options.sourceMapOutputPath);
    Assert.assertNotNull(options.inputVariableMapSerialized);
    Assert.assertNotNull(options.inputPropertyMapSerialized);
  }

  @Test
  public void testProcessResultsTreePassGraphAst() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    runner.setCustomCompiler(compiler);

    runner.getCommandLineConfig().setPrintTree(true);
    int res = runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options);
    Assert.assertEquals(1, res);

    runner.getCommandLineConfig().setPrintTree(false).setPrintAst(true);
    res = runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options);
    Assert.assertEquals(1, res);

    runner.getCommandLineConfig().setPrintAst(false).setPrintPassGraph(true);
    res = runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options);
    Assert.assertEquals(1, res);

    runner.getCommandLineConfig().setPrintPassGraph(false).setComputePhaseOrdering(true);
    res = runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options);
    Assert.assertEquals(0, res);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testOutputNameMapsConflictVars() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
          .setCreateNameMapFiles(true)
          .setVariableMapOutputFile("conflict.out");
    runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options);
  }

  @Test(expected = AbstractCommandLineRunner.FlagUsageException.class)
  public void testOutputNameMapsConflictProps() throws Exception {
    CompilerOptions options = new CompilerOptions();
    runner.getCommandLineConfig()
          .setCreateNameMapFiles(true)
          .setPropertyMapOutputFile("conflict.out");
    runner.processResults(new Result(new JSError[0], new JSError[0], null, null, null, null, null), null, options);
  }

  @Test
  public void testDoRunSimpleCompilation() throws Exception {
    File src = tmp.newFile("src.js");
    File out = tmp.newFile("out.js");
    runner.getCommandLineConfig()
          .setJs(ImmutableList.of(src.getAbsolutePath()))
          .setJsOutputFile(out.getAbsolutePath());

    int exitCode = runner.doRun();
    Assert.assertEquals(0, exitCode);
    Assert.assertTrue(out.exists());
  }

  @Test
  public void testDoRunModuleCompilation() throws Exception {
    File src1 = tmp.newFile("src1.js");
    File src2 = tmp.newFile("src2.js");
    File outDir = tmp.newFolder("outdir");

    runner.getCommandLineConfig()
          .setJs(ImmutableList.of(src1.getAbsolutePath(), src2.getAbsolutePath()))
          .setModule(ImmutableList.of("m1:1", "m2:1:m1"))
          .setModuleOutputPathPrefix(outDir.getAbsolutePath() + File.separatorChar);

    int exitCode = runner.doRun();
    Assert.assertEquals(0, exitCode);
    File m1Out = new File(outDir, "m1.js");
    File m2Out = new File(outDir, "m2.js");
    Assert.assertTrue(m1Out.exists());
    Assert.assertTrue(m2Out.exists());
  }

  @Test
  public void testInitOptionsFromFlagsDeprecated() {
    runner.initOptionsFromFlags(new CompilerOptions());
  }
}
