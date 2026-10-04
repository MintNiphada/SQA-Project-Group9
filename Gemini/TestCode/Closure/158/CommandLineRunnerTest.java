package com.google.javascript.jscomp;

import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
  public void testDefaultConstruction() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{}, new PrintStream(out), new PrintStream(err));
    Assert.assertTrue(runner.shouldRunCompiler());
    CompilerOptions options = runner.createOptions();
    Assert.assertNotNull(options);
    Compiler compiler = runner.createCompiler();
    Assert.assertNotNull(compiler);
  }

  @Test
  public void testVersionOutput() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{"--version"}, new PrintStream(out), new PrintStream(err));
    Assert.assertTrue(runner.shouldRunCompiler());
    String errStr = err.toString();
    Assert.assertTrue(errStr.contains("Closure Compiler"));
    Assert.assertTrue(errStr.contains("Version:"));
  }

  @Test
  public void testHelpFlag() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{"--help"}, new PrintStream(out), new PrintStream(err));
    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().length() > 0);
  }

  @Test
  public void testInvalidCommandLineFlag() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{"--non_existent_flag=true"}, new PrintStream(out), new PrintStream(err));
    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().length() > 0);
  }

  @Test
  public void testComprehensiveOptionsParsing() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String[] args = new String[]{
        "--print_tree=true",
        "--compute_phase_ordering=true",
        "--print_ast=true",
        "--print_pass_graph=true",
        "--jscomp_dev_mode=START_AND_END",
        "--logging_level=INFO",
        "--externs=ext1.js",
        "--externs=ext2.js",
        "--js=file1.js",
        "--js_output_file=out.js",
        "--module=mod1:1",
        "--variable_map_input_file=vmap_in.txt",
        "--property_map_input_file=pmap_in.txt",
        "--variable_map_output_file=vmap_out.txt",
        "--property_map_output_file=pmap_out.txt",
        "--third_party=true",
        "--summary_detail_level=3",
        "--output_wrapper=%output%",
        "--module_wrapper=mod1:%s",
        "--module_output_path_prefix=./dist/",
        "--create_source_map=map.out",
        "--jscomp_error=checkTypes",
        "--jscomp_warning=checkVars",
        "--jscomp_off=deprecated",
        "--define=FLAG=true",
        "--define=STR_FLAG='hello'",
        "--define=\"QUOTED=1\"",
        "--charset=UTF-8",
        "--compilation_level=ADVANCED_OPTIMIZATIONS",
        "--warning_level=VERBOSE",
        "--use_only_custom_externs=true",
        "--debug=true",
        "--generate_exports=true",
        "--formatting=PRETTY_PRINT",
        "--formatting=PRINT_INPUT_DELIMITER",
        "--process_closure_primitives=false",
        "--manage_closure_dependencies=true",
        "--closure_entry_point=app.main",
        "--output_manifest=manifest.out",
        "--accept_const_keyword=true",
        "--language_in=ECMASCRIPT5"
    };
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        args, new PrintStream(out), new PrintStream(err));
    Assert.assertTrue(runner.shouldRunCompiler());

    CompilerOptions options = runner.createOptions();
    Assert.assertTrue(options.prettyPrint);
    Assert.assertTrue(options.printInputDelimiter);
    Assert.assertFalse(options.closurePass);
  }

  @Test
  public void testFlagFileParsing() throws IOException {
    File tempFlagFile = File.createTempFile("flags", ".txt");
    tempFlagFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write("--charset UTF-8 --debug=true".getBytes("UTF-8"));
    fos.close();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{"--flagfile=" + tempFlagFile.getAbsolutePath()},
        new PrintStream(out), new PrintStream(err));

    Assert.assertTrue(runner.shouldRunCompiler());
  }

  @Test
  public void testRecursiveFlagFileError() throws IOException {
    File tempFlagFile = File.createTempFile("flags_nested", ".txt");
    tempFlagFile.deleteOnExit();
    FileOutputStream fos = new FileOutputStream(tempFlagFile);
    fos.write(("--flagfile " + tempFlagFile.getAbsolutePath()).getBytes("UTF-8"));
    fos.close();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{"--flagfile=" + tempFlagFile.getAbsolutePath()},
        new PrintStream(out), new PrintStream(err));

    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().contains("ERROR - Arguments in the file cannot contain"));
  }

  @Test
  public void testInvalidFlagFileRead() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{"--flagfile=non_existent_flags_file_path.txt"},
        new PrintStream(out), new PrintStream(err));

    Assert.assertFalse(runner.shouldRunCompiler());
    Assert.assertTrue(err.toString().contains("read error"));
  }

  @Test
  public void testCreateExternsDefaultAndCustom() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    TestableCommandLineRunner runner = new TestableCommandLineRunner(
        new String[]{}, new PrintStream(out), new PrintStream(err));

    List<JSSourceFile> externs = runner.createExterns();
    Assert.assertNotNull(externs);
    Assert.assertTrue(externs.size() > 0);

    TestableCommandLineRunner customRunner = new TestableCommandLineRunner(
        new String[]{"--use_only_custom_externs=true"}, new PrintStream(out), new PrintStream(err));
    List<JSSourceFile> customExterns = customRunner.createExterns();
    Assert.assertEquals(0, customExterns.size());
  }

  @Test
  public void testGetDefaultExterns() throws IOException {
    List<JSSourceFile> defaultExterns = CommandLineRunner.getDefaultExterns();
    Assert.assertNotNull(defaultExterns);
    Assert.assertTrue(defaultExterns.size() > 0);
  }

  @Test
  public void testFormattingOptionReflection() throws Exception {
    Class<?> formattingEnum = Class.forName("com.google.javascript.jscomp.CommandLineRunner$FormattingOption");
    Object[] constants = formattingEnum.getEnumConstants();
    Method applyToOptionsMethod = formattingEnum.getDeclaredMethod("applyToOptions", CompilerOptions.class);
    applyToOptionsMethod.setAccessible(true);

    for (Object constant : constants) {
      CompilerOptions options = new CompilerOptions();
      applyToOptionsMethod.invoke(constant, options);
      if (constant.toString().equals("PRETTY_PRINT")) {
        Assert.assertTrue(options.prettyPrint);
      } else if (constant.toString().equals("PRINT_INPUT_DELIMITER")) {
        Assert.assertTrue(options.printInputDelimiter);
      }
    }
  }

  @Test
  public void testBooleanOptionHandler() throws Exception {
    Class<?> handlerClass = CommandLineRunner.Flags.BooleanOptionHandler.class;
    Constructor<?> ctor = handlerClass.getConstructor(CmdLineParser.class, OptionDef.class, Setter.class);

    final List<Object> values = new ArrayList<Object>();
    Setter<Boolean> setter = new Setter<Boolean>() {
      @Override
      public void addValue(Boolean value) {
        values.add(value);
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
        (CommandLineRunner.Flags.BooleanOptionHandler) ctor.newInstance(null, null, setter);

    Assert.assertNull(handler.getDefaultMetaVariable());

    Parameters emptyParams = new Parameters() {
      @Override
      public String getParameter(int idx) throws CmdLineException {
        throw new CmdLineException((CmdLineParser) null, "No parameter");
      }

      @Override
      public int size() {
        return 0;
      }
    };
    int consumed = handler.parseArguments(emptyParams);
    Assert.assertEquals(0, consumed);
    Assert.assertEquals(Boolean.TRUE, values.get(values.size() - 1));

    String[] truthy = new String[]{"true", "on", "yes", "1"};
    for (final String t : truthy) {
      Parameters p = new Parameters() {
        @Override
        public String getParameter(int idx) {
          return t;
        }

        @Override
        public int size() {
          return 1;
        }
      };
      consumed = handler.parseArguments(p);
      Assert.assertEquals(1, consumed);
      Assert.assertEquals(Boolean.TRUE, values.get(values.size() - 1));
    }

    String[] falsy = new String[]{"false", "off", "no", "0"};
    for (final String f : falsy) {
      Parameters p = new Parameters() {
        @Override
        public String getParameter(int idx) {
          return f;
        }

        @Override
        public int size() {
          return 1;
        }
      };
      consumed = handler.parseArguments(p);
      Assert.assertEquals(1, consumed);
      Assert.assertEquals(Boolean.FALSE, values.get(values.size() - 1));
    }

    Parameters otherParam = new Parameters() {
      @Override
      public String getParameter(int idx) {
        return "some_other_flag_value";
      }

      @Override
      public int size() {
        return 1;
      }
    };
    consumed = handler.parseArguments(otherParam);
    Assert.assertEquals(0, consumed);
    Assert.assertEquals(Boolean.TRUE, values.get(values.size() - 1));
  }
}
