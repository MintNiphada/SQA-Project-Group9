package com.google.javascript.jscomp;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CommandLineRunnerTest {

  private static class TestableCommandLineRunner extends CommandLineRunner {
    TestableCommandLineRunner(String[] args) {
      super(args);
    }

    TestableCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
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

  @Test
  public void testDefaultConfiguration() {
    String[] args = new String[] {};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args);
    Assert.assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
    Assert.assertFalse(options.prettyPrint);
    Assert.assertFalse(options.printInputDelimiter);
    Assert.assertTrue(options.closurePass);

    Compiler compiler = runner.createCompiler();
    Assert.assertNotNull(compiler);
  }

  @Test
  public void testHelpFlag() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream errStream = new PrintStream(err);
    String[] args = new String[] {"--help"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, System.out, errStream);
    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().length() > 0);
  }

  @Test
  public void testInvalidFlag() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream errStream = new PrintStream(err);
    String[] args = new String[] {"--non_existent_flag"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, System.out, errStream);
    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().length() > 0);
  }

  @Test
  public void testQuotedArguments() {
    String[] args = new String[] {
        "--js='input.js'",
        "--output_wrapper=\"(function(){%output%})();\"",
        "--define='FOO=1'"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args);
    Assert.assertTrue(runner.shouldRunCompiler());
  }

  @Test
  public void testAllFlags() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream errStream = new PrintStream(err);
    String[] args = new String[] {
        "--print_tree=true",
        "--compute_phase_ordering=true",
        "--print_ast=true",
        "--print_pass_graph=true",
        "--jscomp_dev_mode=OFF",
        "--logging_level=INFO",
        "--externs=extern.js",
        "--js=input.js",
        "--js_output_file=out.js",
        "--module=m1:1:",
        "--variable_map_input_file=v_in.txt",
        "--property_map_input_file=p_in.txt",
        "--variable_map_output_file=v_out.txt",
        "--property_map_output_file=p_out.txt",
        "--third_party=true",
        "--summary_detail_level=3",
        "--output_wrapper=%output%",
        "--output_wrapper_marker=%output%",
        "--module_wrapper=m1:%s",
        "--module_output_path_prefix=./bin/",
        "--create_source_map=map.txt",
        "--jscomp_error=checkVars",
        "--jscomp_warning=checkTypes",
        "--jscomp_off=deprecated",
        "--define=FLAG=true",
        "--charset=UTF-8",
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--warning_level=VERBOSE",
        "--use_only_custom_externs=true",
        "--debug=true",
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER",
        "--process_closure_primitives=false",
        "--manage_closure_dependencies=true",
        "--output_manifest=manifest.txt"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args, System.out, errStream);
    Assert.assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    Assert.assertTrue(options.prettyPrint);
    Assert.assertTrue(options.printInputDelimiter);
    Assert.assertFalse(options.closurePass);
  }

  @Test
  public void testCompilationLevelsAndWarnings() {
    String[] whitespaceArgs = new String[] {
        "--compilation_level=WHITESPACE_ONLY",
        "--warning_level=QUIET"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(whitespaceArgs);
    Assert.assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);

    String[] simpleArgs = new String[] {
        "--compilation_level=SIMPLE_OPTIMIZATIONS",
        "--warning_level=DEFAULT"
    };
    TestableCommandLineRunner runner2 = new TestableCommandLineRunner(simpleArgs);
    Assert.assertTrue(runner2.shouldRunCompiler());
    CompilerOptions options2 = runner2.createOptions();
    Assert.assertNotNull(options2);
  }

  @Test
  public void testCreateNameMapFilesFlag() {
    String[] args = new String[] {"--create_name_map_files=true"};
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args);
    Assert.assertTrue(runner.shouldRunCompiler());
  }

  @Test
  public void testBooleanOptionHandlerValues() {
    String[] truthy = new String[] {
        "--debug=true",
        "--print_tree=on",
        "--print_ast=yes",
        "--third_party=1"
    };
    TestableCommandLineRunner runner1 = new TestableCommandLineRunner(truthy);
    Assert.assertTrue(runner1.shouldRunCompiler());

    String[] falsy = new String[] {
        "--debug=false",
        "--print_tree=off",
        "--print_ast=no",
        "--third_party=0"
    };
    TestableCommandLineRunner runner2 = new TestableCommandLineRunner(falsy);
    Assert.assertTrue(runner2.shouldRunCompiler());

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String[] invalid = new String[] {"--debug=not_a_boolean"};
    TestableCommandLineRunner runner3 = new TestableCommandLineRunner(invalid, System.out, new PrintStream(err));
    Assert.assertFalse(runner3.shouldRunCompiler());
  }

  @Test
  public void testBooleanOptionHandlerDirectly() throws Exception {
    class DummyTarget {
      boolean value = false;
    }
    final DummyTarget target = new DummyTarget();
    Setter<Boolean> setter = new Setter<Boolean>() {
      @Override
      public Class<Boolean> getType() {
        return Boolean.class;
      }
      @Override
      public boolean isMultiValued() {
        return false;
      }
      @Override
      public void addValue(Boolean value) {
        target.value = value;
      }
    };

    CmdLineParser parser = new CmdLineParser(new Object());
    CommandLineRunner.Flags.BooleanOptionHandler handler =
        new CommandLineRunner.Flags.BooleanOptionHandler(parser, null, setter);

    Assert.assertNull(handler.getDefaultMetaVariable());

    Parameters nullParam = new Parameters() {
      @Override
      public String getParameter(int idx) {
        return null;
      }
      @Override
      public int size() {
        return 1;
      }
    };
    int consumed = handler.parseArguments(nullParam);
    Assert.assertEquals(0, consumed);
    Assert.assertTrue(target.value);

    Parameters falseParam = new Parameters() {
      @Override
      public String getParameter(int idx) {
        return "false";
      }
      @Override
      public int size() {
        return 1;
      }
    };
    consumed = handler.parseArguments(falseParam);
    Assert.assertEquals(1, consumed);
    Assert.assertFalse(target.value);

    Parameters invalidParam = new Parameters() {
      @Override
      public String getParameter(int idx) {
        return "invalid";
      }
      @Override
      public int size() {
        return 1;
      }
    };
    try {
      handler.parseArguments(invalidParam);
      Assert.fail("Expected CmdLineException for invalid boolean string");
    } catch (CmdLineException e) {
      Assert.assertTrue(e.getMessage().contains("Illegal boolean value"));
    }
  }

  @Test
  public void testGetDefaultExterns() throws IOException {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    Assert.assertNotNull(externs);
    Assert.assertTrue(externs.size() > 0);
  }

  @Test
  public void testCreateExternsWithOnlyCustomExterns() throws Exception {
    String[] args = new String[] {
        "--use_only_custom_externs=true",
        "--externs=custom_extern.js"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args);
    List<JSSourceFile> externs = runner.createExterns();
    Assert.assertNotNull(externs);
    Assert.assertEquals(1, externs.size());
    Assert.assertEquals("custom_extern.js", externs.get(0).getName());
  }

  @Test
  public void testCreateExternsWithDefaultAndCustomExterns() throws Exception {
    String[] args = new String[] {
        "--externs=custom_extern.js"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(args);
    List<JSSourceFile> externs = runner.createExterns();
    Assert.assertNotNull(externs);
    Assert.assertTrue(externs.size() > 1);
    Assert.assertEquals("custom_extern.js", externs.get(externs.size() - 1).getName());
  }
}
