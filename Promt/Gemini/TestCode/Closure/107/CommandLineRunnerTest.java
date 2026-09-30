package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class CommandLineRunnerTest {

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream out;
  private PrintStream err;
  private File tempDir;

  @Before
  public void setUp() throws Exception {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    out = new PrintStream(outStream);
    err = new PrintStream(errStream);
    tempDir = Files.createTempDir();
  }

  @After
  public void tearDown() throws Exception {
    if (tempDir != null && tempDir.exists()) {
      for (File child : tempDir.listFiles()) {
        child.delete();
      }
      tempDir.delete();
    }
  }

  private static class SubCommandLineRunner extends CommandLineRunner {
    public SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
    }

    public SubCommandLineRunner(String[] args) {
      super(args);
    }

    @Override
    public CompilerOptions createOptions() {
      return super.createOptions();
    }

    @Override
    public Compiler createCompiler() {
      return super.createCompiler();
    }

    @Override
    public List<SourceFile> createExterns() throws FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  @Test
  public void testDefaultExternsLoading() throws Exception {
    List<SourceFile> externs = CommandLineRunner.getDefaultExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
    boolean foundEs3 = false;
    for (SourceFile file : externs) {
      if (file.getName().contains("es3.js")) {
        foundEs3 = true;
        break;
      }
    }
    assertTrue("Default externs should contain es3.js", foundEs3);
  }

  @Test
  public void testEmptyArgs() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{}, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertFalse(options.prettyPrint);
    assertFalse(options.closurePass);
  }

  @Test
  public void testSimpleCommandLineFlags() {
    String[] args = new String[] {
        "--js=test.js",
        "--debug=true",
        "--generate_exports=true",
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER",
        "--formatting=SINGLE_QUOTES",
        "--process_closure_primitives=true",
        "--angular_pass=true",
        "--use_types_for_optimization=true",
        "--extra_annotation_name=foobar"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
    assertTrue(options.preferSingleQuotes);
    assertTrue(options.closurePass);
    assertTrue(options.angularPass);
    assertNotNull(options.extraAnnotationNames);
    assertTrue(options.extraAnnotationNames.contains("foobar"));
  }

  @Test
  public void testHelpFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{"--help"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("--help"));
  }

  @Test
  public void testVersionFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{"--version"}, out, err);
    assertTrue(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("Closure Compiler"));
    assertTrue(errOutput.contains("Version:"));
  }

  @Test
  public void testInvalidFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{"--non_existent_flag"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("is not a valid option"));
  }

  @Test
  public void testCompilationLevelAdvancedOptions() {
    String[] args = new String[] {
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--process_jquery_primitives=true"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertTrue(options.jqueryPass);
    assertNotNull(options.messageBundle);
    assertTrue(options.messageBundle instanceof EmptyMessageBundle);
  }

  @Test
  public void testCompilationLevelWhitespace() {
    String[] args = new String[] {
        "--compilation_level=WHITESPACE_ONLY"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test
  public void testWarningLevelQuietAndVerbose() {
    SubCommandLineRunner quietRunner = new SubCommandLineRunner(new String[]{"--warning_level=QUIET"}, out, err);
    CompilerOptions quietOptions = quietRunner.createOptions();
    assertNotNull(quietOptions);

    SubCommandLineRunner verboseRunner = new SubCommandLineRunner(new String[]{"--warning_level=VERBOSE"}, out, err);
    CompilerOptions verboseOptions = verboseRunner.createOptions();
    assertNotNull(verboseOptions);
  }

  @Test
  public void testProcessCommonJsModules() {
    String[] validArgs = new String[] {
        "--process_common_js_modules=true",
        "--common_js_entry_module=foo/bar.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(validArgs, out, err);
    assertTrue(runner.shouldRunCompiler());

    String[] missingEntry = new String[] {
        "--process_common_js_modules=true"
    };
    SubCommandLineRunner runnerMissing = new SubCommandLineRunner(missingEntry, out, err);
    assertFalse(runnerMissing.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("Please specify --common_js_entry_module."));
  }

  @Test
  public void testCodingConventions() {
    SubCommandLineRunner thirdPartyRunner = new SubCommandLineRunner(new String[]{"--third_party=true"}, out, err);
    assertTrue(thirdPartyRunner.shouldRunCompiler());

    SubCommandLineRunner jqueryRunner = new SubCommandLineRunner(new String[]{"--process_jquery_primitives=true"}, out, err);
    assertTrue(jqueryRunner.shouldRunCompiler());
    CompilerOptions jqueryOptions = jqueryRunner.createOptions();
    assertTrue(jqueryOptions.getCodingConvention() instanceof JqueryCodingConvention);

    SubCommandLineRunner defaultRunner = new SubCommandLineRunner(new String[]{}, out, err);
    CompilerOptions defaultOptions = defaultRunner.createOptions();
    assertTrue(defaultOptions.getCodingConvention() instanceof ClosureCodingConvention);
  }

  @Test
  public void testWarningGuards() {
    String[] args = new String[] {
        "--jscomp_error=checkVars",
        "--jscomp_warning=checkTypes",
        "--jscomp_off=globalThis"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  @Test
  public void testFlagFileProcessing() throws Exception {
    File flagFile = new File(tempDir, "flags.txt");
    Files.write("--debug=true\n--formatting='PRETTY_PRINT'\n--js=\"my file.js\"", flagFile, Charsets.UTF_8);

    String[] args = new String[] {
        "--flagfile=" + flagFile.getAbsolutePath(),
        "--warning_level=VERBOSE"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
  }

  @Test
  public void testFlagFileRecursiveError() throws Exception {
    File flagFile = new File(tempDir, "nested_flags.txt");
    Files.write("--flagfile=other.txt", flagFile, Charsets.UTF_8);

    String[] args = new String[] {
        "--flagfile=" + flagFile.getAbsolutePath()
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("ERROR - Arguments in the file cannot contain --flagfile option."));
  }

  @Test
  public void testFlagFileReadError() {
    String[] args = new String[] {
        "--flagfile=non_existent_file_path_12345.txt"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertFalse(runner.shouldRunCompiler());
    String errOutput = new String(errStream.toByteArray());
    assertTrue(errOutput.contains("read error."));
  }

  @Test
  public void testTranslationsFile() throws Exception {
    File xtbFile = new File(tempDir, "bundle.xtb");
    String xtbContent =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE translationbundle SYSTEM \"translationbundle.dtd\">\n"
        + "<translationbundle lang=\"es\">\n"
        + "  <translation id=\"17\">Hola</translation>\n"
        + "</translationbundle>";
    Files.write(xtbContent, xtbFile, Charsets.UTF_8);

    String[] args = new String[] {
        "--translations_file=" + xtbFile.getAbsolutePath(),
        "--translations_project=testProject"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    assertNotNull(options.messageBundle);
  }

  @Test
  public void testTranslationsFileInvalid() {
    String[] args = new String[] {
        "--translations_file=" + new File(tempDir, "missing.xtb").getAbsolutePath()
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    try {
      runner.createOptions();
      fail("Expected RuntimeException for missing translation file");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Reading XTB file"));
    }
  }

  @Test
  public void testCreateCompiler() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{}, out, err);
    Compiler compiler = runner.createCompiler();
    assertNotNull(compiler);
  }

  @Test
  public void testCreateExterns() throws Exception {
    SubCommandLineRunner defaultRunner = new SubCommandLineRunner(new String[]{}, out, err);
    List<SourceFile> externs = defaultRunner.createExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());

    SubCommandLineRunner customOnlyRunner = new SubCommandLineRunner(new String[]{"--use_only_custom_externs=true"}, out, err);
    List<SourceFile> customExterns = customOnlyRunner.createExterns();
    assertNotNull(customExterns);
    assertTrue(customExterns.isEmpty());
  }

  @Test
  public void testSingleArgConstructor() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{"--help"});
    assertFalse(runner.shouldRunCompiler());
  }

  @Test
  public void testQuotedArgProcessing() throws Exception {
    Method processArgs = CommandLineRunner.class.getDeclaredMethod("processArgs", String[].class);
    processArgs.setAccessible(true);
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{}, out, err);

    String[] inputArgs = new String[] {
        "--foo='bar'",
        "--baz=\"qux\"",
        "--normal=val",
        "loose_arg"
    };

    @SuppressWarnings("unchecked")
    List<String> result = (List<String>) processArgs.invoke(runner, (Object) inputArgs);
    assertEquals(Arrays.asList("--foo", "bar", "--baz", "qux", "--normal", "val", "loose_arg"), result);
  }

  @Test
  public void testTokenizeKeepingQuotedStrings() throws Exception {
    Method tokenize = CommandLineRunner.class.getDeclaredMethod("tokenizeKeepingQuotedStrings", List.class);
    tokenize.setAccessible(true);
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[]{}, out, err);

    List<String> lines = Arrays.asList(
        "--js='foo bar/baz.js' --debug",
        "  --flag=\"val 1 2 3\" trailing"
    );

    @SuppressWarnings("unchecked")
    List<String> tokens = (List<String>) tokenize.invoke(runner, lines);
    assertEquals(Arrays.asList("--js='foo bar/baz.js'", "--debug", "--flag=\"val 1 2 3\"", "trailing"), tokens);
  }

  @Test
  public void testBooleanOptionHandler() throws Exception {
    String[] truthy = new String[] {
        "--debug=true",
        "--print_tree=on",
        "--print_ast=yes",
        "--print_pass_graph=1",
        "--third_party"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(truthy, out, err);
    assertTrue(runner.shouldRunCompiler());

    String[] falsy = new String[] {
        "--debug=false",
        "--print_tree=off",
        "--print_ast=no",
        "--print_pass_graph=0"
    };
    SubCommandLineRunner runner2 = new SubCommandLineRunner(falsy, out, err);
    assertTrue(runner2.shouldRunCompiler());
  }

  @Test
  public void testAdditionalFlags() {
    String[] args = new String[] {
        "--output_wrapper=(function(){%output%})();",
        "--module_wrapper=mod1:(function(){%s})();",
        "--module_output_path_prefix=out/",
        "--create_source_map=map.out",
        "--source_map_format=V3",
        "--define=FOO='bar'",
        "--charset=UTF-8",
        "--manage_closure_dependencies=true",
        "--only_closure_dependencies=true",
        "--closure_entry_point=goog.events",
        "--output_manifest=manifest.txt",
        "--output_module_dependencies=deps.json",
        "--accept_const_keyword",
        "--language_in=ECMASCRIPT5",
        "--transform_amd_modules",
        "--common_js_module_path_prefix=src/",
        "--warnings_whitelist_file=whitelist.txt",
        "--tracer_mode=ALL",
        "--summary_detail_level=3",
        "--variable_map_input_file=vars.in",
        "--property_map_input_file=props.in",
        "--variable_map_output_file=vars.out",
        "--property_map_output_file=props.out",
        "--create_name_map_files=false",
        "--jscomp_dev_mode=START",
        "--logging_level=INFO",
        "arg1.js",
        "arg2.js"
    };
    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }
}