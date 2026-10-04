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
import java.util.List;

public class CommandLineRunnerTest {

  private static class TestRunner extends CommandLineRunner {
    TestRunner(String[] args) {
      super(args);
    }

    TestRunner(String[] args, PrintStream out, PrintStream err) {
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
    public List<JSSourceFile> createExterns() throws IOException, AbstractCommandLineRunner.FlagUsageException {
      return super.createExterns();
    }
  }

  @Test
  public void testDefaultConfiguration() {
    TestRunner runner = new TestRunner(new String[] {});
    Assert.assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
    Assert.assertTrue(options.closurePass);
    Assert.assertFalse(options.prettyPrint);
    Assert.assertFalse(options.printInputDelimiter);
    Compiler compiler = runner.createCompiler();
    Assert.assertNotNull(compiler);
  }

  @Test
  public void testHelpFlag() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream errStream = new PrintStream(err);
    TestRunner runner = new TestRunner(new String[] {"--help"}, System.out, errStream);
    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().contains("--help"));
  }

  @Test
  public void testInvalidFlag() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    PrintStream errStream = new PrintStream(err);
    TestRunner runner = new TestRunner(new String[] {"--non_existent_flag"}, System.out, errStream);
    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().length() > 0);
  }

  @Test
  public void testArgParsingWithEqualsAndQuotes() {
    String[] args = new String[] {
        "--js=\"input.js\"",
        "--js='input2.js'",
        "--charset=UTF-8",
        "--debug=true",
        "--third_party=true"
    };
    TestRunner runner = new TestRunner(args);
    Assert.assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
  }

  @Test
  public void testFlagsConfiguration() {
    String[] args = new String[] {
        "--print_tree=true",
        "--compute_phase_ordering=true",
        "--print_ast=true",
        "--print_pass_graph=true",
        "--jscomp_dev_mode=START",
        "--logging_level=FINE",
        "--externs=extern.js",
        "--js=file.js",
        "--js_output_file=out.js",
        "--module=mod1:1",
        "--variable_map_input_file=v_in.map",
        "--property_map_input_file=p_in.map",
        "--variable_map_output_file=v_out.map",
        "--property_map_output_file=p_out.map",
        "--summary_detail_level=2",
        "--output_wrapper=%output%",
        "--output_wrapper_marker=%output%",
        "--module_wrapper=mod1:%s",
        "--module_output_path_prefix=./out/",
        "--create_source_map=map.out",
        "--jscomp_error=checkVars",
        "--jscomp_warning=checkRegExp",
        "--jscomp_off=checkTypes",
        "--define=FLAG=1",
        "--charset=US-ASCII",
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--warning_level=VERBOSE",
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER",
        "--process_closure_primitives=false",
        "--manage_closure_dependencies=true",
        "--output_manifest=manifest.txt"
    };
    TestRunner runner = new TestRunner(args);
    Assert.assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    Assert.assertTrue(options.prettyPrint);
    Assert.assertTrue(options.printInputDelimiter);
    Assert.assertFalse(options.closurePass);
  }

  @Test
  public void testCreateNameMapFilesFlag() {
    String[] args = new String[] {
        "--create_name_map_files=true"
    };
    TestRunner runner = new TestRunner(args);
    Assert.assertTrue(runner.shouldRunCompiler());
  }

  @Test
  public void testDefaultExternsLoading() throws Exception {
    List<JSSourceFile> externs = CommandLineRunner.getDefaultExterns();
    Assert.assertNotNull(externs);
    Assert.assertFalse(externs.isEmpty());
    Assert.assertEquals("externs.zip//es3.js", externs.get(0).getName());
  }

  @Test
  public void testCreateExternsDefaultAndCustom() throws Exception {
    TestRunner runnerDefault = new TestRunner(new String[] {});
    List<JSSourceFile> defaultExterns = runnerDefault.createExterns();
    Assert.assertNotNull(defaultExterns);
    Assert.assertFalse(defaultExterns.isEmpty());

    TestRunner runnerCustomOnly = new TestRunner(new String[] {"--use_only_custom_externs=true"});
    List<JSSourceFile> customExterns = runnerCustomOnly.createExterns();
    Assert.assertNotNull(customExterns);
    Assert.assertTrue(customExterns.isEmpty());
  }

  @Test
  public void testBooleanOptionHandler() throws Exception {
    final boolean[] assignedValue = new boolean[1];
    Setter<Boolean> setter = new Setter<Boolean>() {
      @Override
      public void addValue(Boolean value) {
        assignedValue[0] = value;
      }
      @Override
      public Class<Boolean> getType() {
        return Boolean.class;
      }
      @Override
      public boolean isMultiValued() {
        return false;
      }
    };

    CommandLineRunner.Flags.BooleanOptionHandler handler =
        new CommandLineRunner.Flags.BooleanOptionHandler(
            new CmdLineParser(new Object()),
            null,
            setter);

    Assert.assertNull(handler.getDefaultMetaVariable());

    Parameters paramsNull = new Parameters() {
      @Override
      public String getParameter(int idx) {
        return null;
      }
      @Override
      public int size() {
        return 1;
      }
    };
    Assert.assertEquals(0, handler.parseArguments(paramsNull));
    Assert.assertTrue(assignedValue[0]);

    for (String truthy : new String[] {"true", "on", "yes", "1", "TRUE", "On"}) {
      final String val = truthy;
      Parameters p = new Parameters() {
        @Override
        public String getParameter(int idx) {
          return val;
        }
        @Override
        public int size() {
          return 1;
        }
      };
      Assert.assertEquals(1, handler.parseArguments(p));
      Assert.assertTrue(assignedValue[0]);
    }

    for (String falsy : new String[] {"false", "off", "no", "0", "FALSE", "Off"}) {
      final String val = falsy;
      Parameters p = new Parameters() {
        @Override
        public String getParameter(int idx) {
          return val;
        }
        @Override
        public int size() {
          return 1;
        }
      };
      Assert.assertEquals(1, handler.parseArguments(p));
      Assert.assertFalse(assignedValue[0]);
    }

    Parameters pInvalid = new Parameters() {
      @Override
      public String getParameter(int idx) {
        return "invalid_boolean";
      }
      @Override
      public int size() {
        return 1;
      }
    };
    try {
      handler.parseArguments(pInvalid);
      Assert.fail("Expected CmdLineException for invalid boolean param");
    } catch (CmdLineException expected) {
      Assert.assertTrue(expected.getMessage().contains("Illegal boolean value"));
    }
  }
}
