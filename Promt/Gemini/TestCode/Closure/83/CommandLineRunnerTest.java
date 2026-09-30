package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class CommandLineRunnerTest {

  private ByteArrayOutputStream outStream;
  private ByteArrayOutputStream errStream;
  private PrintStream out;
  private PrintStream err;

  private static class SubCommandLineRunner extends CommandLineRunner {
    SubCommandLineRunner(String[] args) {
      super(args);
    }

    SubCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
      super(args, out, err);
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
    public List<JSSourceFile> createExterns() throws AbstractCommandLineRunner.FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  @Before
  public void setUp() {
    outStream = new ByteArrayOutputStream();
    errStream = new ByteArrayOutputStream();
    out = new PrintStream(outStream);
    err = new PrintStream(errStream);
  }

  @After
  public void tearDown() {
    out.close();
    err.close();
  }

  @Test
  public void testDefaultConstructorAndFlags() {
    SubCommandLineRunner runner = new SubCommandLineRunner(new String[] {});
    assertTrue(runner.shouldRunCompiler());
    assertNotNull(runner.createCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
    assertTrue(options.closurePass);
    assertFalse(options.prettyPrint);
    assertFalse(options.printInputDelimiter);
  }

  @Test
  public void testHelpFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--help"}, out, err);
    assertFalse(runner.shouldRunCompiler());
    String errOutput = errStream.toString();
    assertTrue(errOutput.contains("--help"));
  }

  @Test
  public void testVersionFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--version"}, out, err);
    assertTrue(runner.shouldRunCompiler());
    String errOutput = errStream.toString();
    assertTrue(errOutput.contains("Closure Compiler"));
    assertTrue(errOutput.contains("Version:"));
  }

  @Test
  public void testInvalidFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--non_existent_flag"}, out, err);
    assertFalse(runner.shouldRunCompiler());
  }

  @Test
  public void testQuotedArguments() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--charset='UTF-8'", "--js_output_file=\"out.js\""}, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  @Test
  public void testCompilationLevelAndDebug() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--compilation_level", "ADVANCED_OPTIMIZATIONS",
            "--debug=true",
            "--warning_level", "VERBOSE"
        }, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertNotNull(options);
  }

  @Test
  public void testFormattingOptions() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {
            "--formatting", "PRETTY_PRINT",
            "--formatting", "PRINT_INPUT_DELIMITER"
        }, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertTrue(options.prettyPrint);
    assertTrue(options.printInputDelimiter);
  }

  @Test
  public void testBooleanOptionHandlerVariations() {
    String[] trues = {"true", "on", "yes", "1"};
    for (String val : trues) {
      SubCommandLineRunner runner = new SubCommandLineRunner(
          new String[] {"--debug=" + val}, out, err);
      assertTrue(runner.shouldRunCompiler());
      CompilerOptions options = runner.createOptions();
      assertNotNull(options);
    }

    String[] falses = {"false", "off", "no", "0"};
    for (String val : falses) {
      SubCommandLineRunner runner = new SubCommandLineRunner(
          new String[] {"--debug=" + val}, out, err);
      assertTrue(runner.shouldRunCompiler());
    }

    SubCommandLineRunner runnerDefault = new SubCommandLineRunner(
        new String[] {"--debug"}, out, err);
    assertTrue(runnerDefault.shouldRunCompiler());

    SubCommandLineRunner runnerUnknownVal = new SubCommandLineRunner(
        new String[] {"--debug=unknown_val"}, out, err);
    assertTrue(runnerUnknownVal.shouldRunCompiler());
  }

  @Test
  public void testProcessClosurePrimitivesFlag() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--process_closure_primitives=false"}, out, err);
    assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    assertFalse(options.closurePass);
  }

  @Test
  public void testThirdPartyCodingConvention() {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--third_party=true"}, out, err);
    assertTrue(runner.shouldRunCompiler());
  }

  @Test
  public void testGetDefaultExterns() throws Exception {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    assertNotNull(externs);
    assertFalse(externs.isEmpty());
    assertEquals(32, externs.size());
  }

  @Test
  public void testCreateExternsDefault() throws Exception {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {}, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertTrue(externs.size() >= 32);
  }

  @Test
  public void testCreateExternsOnlyCustom() throws Exception {
    SubCommandLineRunner runner = new SubCommandLineRunner(
        new String[] {"--use_only_custom_externs=true"}, out, err);
    List<JSSourceFile> externs = runner.createExterns();
    assertNotNull(externs);
    assertEquals(0, externs.size());
  }

  @Test
  public void testFlagsConfigurationSetting() {
    String[] args = new String[] {
        "--print_tree",
        "--compute_phase_ordering",
        "--print_ast",
        "--print_pass_graph",
        "--jscomp_dev_mode=OFF",
        "--logging_level=INFO",
        "--summary_detail_level=3",
        "--output_wrapper=(function(){%output%})();",
        "--output_wrapper_marker=%output%",
        "--module_output_path_prefix=./bin/",
        "--create_source_map=map.out",
        "--jscomp_error=checkTypes",
        "--jscomp_warning=deprecated",
        "--jscomp_off=visibility",
        "--define=DEBUG=false",
        "--manage_closure_dependencies",
        "--closure_entry_point=goog.events",
        "--output_manifest=manifest.out"
    };

    SubCommandLineRunner runner = new SubCommandLineRunner(args, out, err);
    assertTrue(runner.shouldRunCompiler());
  }
}