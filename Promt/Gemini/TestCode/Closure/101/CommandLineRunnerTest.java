package com.google.javascript.jscomp;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class CommandLineRunnerTest {

  private static class TestableCommandLineRunner extends CommandLineRunner {
    public TestableCommandLineRunner(String[] args) throws CmdLineException {
      super(args);
    }

    public TestableCommandLineRunner(String[] args, PrintStream out, PrintStream err)
        throws CmdLineException {
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
    public List<JSSourceFile> createExterns() throws FlagUsageException, IOException {
      return super.createExterns();
    }
  }

  private TestableCommandLineRunner createRunner(String[] args) throws CmdLineException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    return new TestableCommandLineRunner(args, new PrintStream(out), new PrintStream(err));
  }

  @Test
  public void testDefaultOptions() throws Exception {
    String[] args = new String[] {};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertNotNull(options);
    Assert.assertTrue(options.closurePass);
    Assert.assertFalse(options.prettyPrint);
    Assert.assertFalse(options.printInputDelimiter);
    Assert.assertTrue(options.getCodingConvention() instanceof ClosureCodingConvention);
  }

  @Test
  public void testCustomConstructorWithDefaults() throws Exception {
    TestableCommandLineRunner runner = new TestableCommandLineRunner(new String[] {});
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
    Assert.assertNotNull(runner.createCompiler());
  }

  @Test
  public void testCompilationLevelWhitespaceOnly() throws Exception {
    String[] args = new String[] {"--compilation_level=WHITESPACE_ONLY"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertNotNull(options);
    Assert.assertFalse(options.checkGlobalThisLevel.isOn());
  }

  @Test
  public void testCompilationLevelAdvancedOptimizations() throws Exception {
    String[] args = new String[] {"--compilation_level=ADVANCED_OPTIMIZATIONS"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertNotNull(options);
    Assert.assertTrue(options.reserveRawExports);
    Assert.assertTrue(options.removeUnusedVars);
  }

  @Test
  public void testCompilationLevelSimpleOptimizations() throws Exception {
    String[] args = new String[] {"--compilation_level=SIMPLE_OPTIMIZATIONS"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertNotNull(options);
  }

  @Test
  public void testWarningLevelQuiet() throws Exception {
    String[] args = new String[] {"--warning_level=QUIET"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertNotNull(options);
    Assert.assertFalse(options.checkSuspiciousCode);
  }

  @Test
  public void testWarningLevelVerbose() throws Exception {
    String[] args = new String[] {"--warning_level=VERBOSE"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertNotNull(options);
    Assert.assertTrue(options.checkSuspiciousCode);
    Assert.assertTrue(options.checkGlobalThisLevel.isOn());
  }

  @Test
  public void testDebugFlag() throws Exception {
    String[] args = new String[] {
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--debug=true"
    };
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertNotNull(options);
    Assert.assertNotNull(options.anonymousFunctionNaming);
  }

  @Test
  public void testFormattingOptionsPrettyPrint() throws Exception {
    String[] args = new String[] {"--formatting=PRETTY_PRINT"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertTrue(options.prettyPrint);
    Assert.assertFalse(options.printInputDelimiter);
  }

  @Test
  public void testFormattingOptionsPrintInputDelimiter() throws Exception {
    String[] args = new String[] {"--formatting=PRINT_INPUT_DELIMITER"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertFalse(options.prettyPrint);
    Assert.assertTrue(options.printInputDelimiter);
  }

  @Test
  public void testFormattingOptionsMultiple() throws Exception {
    String[] args = new String[] {
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER"
    };
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertTrue(options.prettyPrint);
    Assert.assertTrue(options.printInputDelimiter);
  }

  @Test
  public void testProcessClosurePrimitivesDisabled() throws Exception {
    String[] args = new String[] {"--process_closure_primitives=false"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertFalse(options.closurePass);
  }

  @Test
  public void testProcessClosurePrimitivesEnabled() throws Exception {
    String[] args = new String[] {"--process_closure_primitives=true"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();

    Assert.assertTrue(options.closurePass);
  }

  @Test
  public void testBooleanFlagVariations() throws Exception {
    String[][] validTrueValues = new String[][] {
        {"--debug=true"},
        {"--debug=on"},
        {"--debug=yes"},
        {"--debug=1"}
    };

    for (String[] arg : validTrueValues) {
      TestableCommandLineRunner runner = createRunner(arg);
      CompilerOptions options = runner.createOptions();
      Assert.assertNotNull(options);
    }

    String[][] validFalseValues = new String[][] {
        {"--debug=false"},
        {"--debug=off"},
        {"--debug=no"},
        {"--debug=0"}
    };

    for (String[] arg : validFalseValues) {
      TestableCommandLineRunner runner = createRunner(arg);
      CompilerOptions options = runner.createOptions();
      Assert.assertNotNull(options);
    }
  }

  @Test(expected = CmdLineException.class)
  public void testInvalidBooleanFlagThrowsException() throws Exception {
    createRunner(new String[] {"--debug=not_a_boolean"});
  }

  @Test(expected = CmdLineException.class)
  public void testUnknownFlagThrowsException() throws Exception {
    createRunner(new String[] {"--unknown_flag_xyz=123"});
  }

  @Test
  public void testQuotedArgumentParsing() throws Exception {
    String[] args = new String[] {
        "--charset='UTF-8'",
        "--output_wrapper=\"%output%\""
    };
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }

  @Test
  public void testUnquotedEqualsArgumentParsing() throws Exception {
    String[] args = new String[] {
        "--charset=UTF-8",
        "--logging_level=INFO"
    };
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }

  @Test
  public void testPositionalStyleFlags() throws Exception {
    String[] args = new String[] {
        "--charset", "UTF-8",
        "--logging_level", "FINE"
    };
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }

  @Test
  public void testDevModeFlags() throws Exception {
    String[] args = new String[] {"--jscomp_dev_mode=START"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);

    String[] aliasArgs = new String[] {"--dev_mode=EVERY_PASS"};
    TestableCommandLineRunner aliasRunner = createRunner(aliasArgs);
    CompilerOptions aliasOptions = aliasRunner.createOptions();
    Assert.assertNotNull(aliasOptions);
  }

  @Test
  public void testDiagnosticFlags() throws Exception {
    String[] args = new String[] {
        "--jscomp_error=checkVars",
        "--jscomp_warning=undefinedVars",
        "--jscomp_off=deprecated"
    };
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }

  @Test
  public void testDefineFlagVariations() throws Exception {
    String[] args = new String[] {
        "--define=FLAG_BOOL",
        "-D", "FLAG_NUM=123",
        "--D", "FLAG_STR='abc'"
    };
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }

  @Test
  public void testCreateCompiler() throws Exception {
    TestableCommandLineRunner runner = createRunner(new String[] {});
    Compiler compiler = runner.createCompiler();
    Assert.assertNotNull(compiler);
  }

  @Test
  public void testCreateExternsDefault() throws Exception {
    TestableCommandLineRunner runner = createRunner(new String[] {});
    List<JSSourceFile> defaultExterns = runner.createExterns();
    Assert.assertNotNull(defaultExterns);
    Assert.assertFalse(defaultExterns.isEmpty());
  }

  @Test
  public void testCreateExternsCustomOnly() throws Exception {
    String[] args = new String[] {"--use_only_custom_externs=true"};
    TestableCommandLineRunner runner = createRunner(args);
    List<JSSourceFile> externs = runner.createExterns();
    Assert.assertNotNull(externs);
    Assert.assertTrue(externs.isEmpty());
  }

  @Test
  public void testAllRemainingFlagsConfigured() throws Exception {
    String[] args = new String[] {
        "--print_tree=true",
        "--compute_phase_ordering=true",
        "--print_ast=true",
        "--print_pass_graph=true",
        "--externs=extern1.js",
        "--externs=extern2.js",
        "--js=input1.js",
        "--js=input2.js",
        "--js_output_file=out.js",
        "--module=mod1:1",
        "--variable_map_input_file=v_in.txt",
        "--property_map_input_file=p_in.txt",
        "--variable_map_output_file=v_out.txt",
        "--property_map_output_file=p_out.txt",
        "--third_party=true",
        "--summary_detail_level=3",
        "--output_wrapper=(function(){%output%})();",
        "--output_wrapper_marker=%output%",
        "--module_wrapper=mod1:%s",
        "--module_output_path_prefix=prefix_",
        "--create_source_map=map.out"
    };

    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }

  @Test
  public void testCreateNameMapFilesFlag() throws Exception {
    String[] args = new String[] {"--create_name_map_files=true"};
    TestableCommandLineRunner runner = createRunner(args);
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }
}