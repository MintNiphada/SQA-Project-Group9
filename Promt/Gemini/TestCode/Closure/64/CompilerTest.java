package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
  }

  @Test
  public void testConstructors() {
    Compiler c1 = new Compiler();
    Assert.assertNotNull(c1.getErrorManager());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler c2 = new Compiler(ps);
    Assert.assertNotNull(c2.getErrorManager());

    PrintStreamErrorManager psem = new PrintStreamErrorManager(ps);
    Compiler c3 = new Compiler(psem);
    Assert.assertSame(psem, c3.getErrorManager());
  }

  @Test(expected = RuntimeException.class)
  public void testSetNullErrorManager() {
    compiler.setErrorManager(null);
  }

  @Test
  public void testInitOptions() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkTypes = true;
    opts.checkSymbols = true;
    opts.setCheckGlobalThisLevel(CheckLevel.WARNING);
    c.initOptions(opts);
    Assert.assertSame(opts, c.getOptions());
    Assert.assertTrue(c.isTypeCheckingEnabled());
  }

  @Test
  public void testInitOptionsDiagnosticGroups() {
    Compiler c = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    c.initOptions(opts);
    Assert.assertTrue(opts.checkTypes);

    opts = new CompilerOptions();
    opts.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.OFF);
    c.initOptions(opts);
    Assert.assertFalse(opts.checkTypes);
  }

  @Test
  public void testCodeBuilder() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals(0, cb.getLineIndex());
    Assert.assertEquals(0, cb.getColumnIndex());
    Assert.assertEquals("", cb.toString());

    cb.append("var a = 10;");
    Assert.assertEquals("var a = 10;", cb.toString());
    Assert.assertEquals(11, cb.getLength());
    Assert.assertEquals(0, cb.getLineIndex());
    Assert.assertEquals(11, cb.getColumnIndex());
    Assert.assertTrue(cb.endsWith(";"));
    Assert.assertFalse(cb.endsWith("x"));

    cb.append("\nvar b = 20;\n");
    Assert.assertEquals(2, cb.getLineIndex());
    Assert.assertEquals(0, cb.getColumnIndex());

    cb.reset();
    Assert.assertEquals(0, cb.getLength());
    Assert.assertEquals("", cb.toString());
    // Line index remains unchanged after reset
    Assert.assertEquals(2, cb.getLineIndex());
  }

  @Test
  public void testSimpleCompile() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1 + 2; alert(x);");
    compiler.disableThreads();
    Result result = compiler.compile(extern, input, options);
    Assert.assertNotNull(result);
    Assert.assertTrue(result.success);
    Assert.assertEquals(0, result.errors.length);
    Assert.assertEquals(0, result.warnings.length);

    String source = compiler.toSource();
    Assert.assertNotNull(source);
    Assert.assertTrue(source.contains("alert"));
  }

  @Test
  public void testCompileArrayAndModules() {
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", "")
    };
    JSModule mod1 = new JSModule("m1");
    mod1.add(JSSourceFile.fromCode("input1.js", "var a = 1;"));
    JSModule mod2 = new JSModule("m2");
    mod2.add(JSSourceFile.fromCode("input2.js", "var b = 2;"));
    mod2.addDependency(mod1);

    JSModule[] modules = new JSModule[] { mod1, mod2 };
    compiler.disableThreads();
    Result result = compiler.compile(externs, modules, options);
    Assert.assertTrue(result.success);

    String[] sources = compiler.toSourceArray();
    Assert.assertEquals(2, sources.length);

    String modSource1 = compiler.toSource(mod1);
    Assert.assertTrue(modSource1.contains("var a"));

    String[] modSources1 = compiler.toSourceArray(mod1);
    Assert.assertEquals(1, modSources1.length);
  }

  @Test
  public void testCompileEmptyModule() {
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", "")
    };
    JSModule mod1 = new JSModule("m1");
    JSModule[] modules = new JSModule[] { mod1 };

    compiler.disableThreads();
    Result result = compiler.compile(externs, modules, options);
    Assert.assertTrue(result.success);

    Assert.assertEquals("", compiler.toSource(mod1));
    Assert.assertEquals(0, compiler.toSourceArray(mod1).length);
  }

  @Test
  public void testParseSyntheticCodeAndTestCode() {
    Node node1 = compiler.parseTestCode("var a = 10;");
    Assert.assertNotNull(node1);
    Assert.assertEquals(Token.SCRIPT, node1.getType());

    Node node2 = compiler.parseSyntheticCode("synthetic.js", "var b = 20;");
    Assert.assertNotNull(node2);
    Assert.assertEquals(Token.SCRIPT, node2.getType());

    Node node3 = compiler.parseSyntheticCode("var c = 30;");
    Assert.assertNotNull(node3);
    Assert.assertEquals(Token.SCRIPT, node3.getType());
  }

  @Test
  public void testParseFile() {
    JSSourceFile file = JSSourceFile.fromCode("test.js", "var x = 100;");
    Node root = compiler.parse(file);
    Assert.assertNotNull(root);
    Assert.assertEquals(Token.SCRIPT, root.getType());
  }

  @Test
  public void testUniqueNameSupplier() {
    compiler.resetUniqueNameId();
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    Assert.assertEquals("0", supplier.get());
    Assert.assertEquals("1", supplier.get());
    Assert.assertEquals("2", supplier.get());
    compiler.resetUniqueNameId();
    Assert.assertEquals("0", supplier.get());
  }

  @Test
  public void testAreNodesEqualForInlining() {
    compiler.initCompilerOptionsIfTesting();
    Node n1 = new Node(Token.NAME);
    n1.setString("foo");
    Node n2 = new Node(Token.NAME);
    n2.setString("foo");
    Node n3 = new Node(Token.NAME);
    n3.setString("bar");

    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    Assert.assertFalse(compiler.areNodesEqualForInlining(n1, n3));

    compiler.getOptions().ambiguateProperties = true;
    Assert.assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    Assert.assertFalse(compiler.areNodesEqualForInlining(n1, n3));
  }

  @Test
  public void testInputManagement() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var ext;");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var x;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);

    CompilerInput inInput = compiler.getInput("in.js");
    Assert.assertNotNull(inInput);
    Assert.assertEquals("in.js", inInput.getName());

    CompilerInput extInput = compiler.getInput("externs.js");
    Assert.assertNotNull(extInput);
    Assert.assertTrue(extInput.isExtern());

    compiler.removeInput("in.js");
    Assert.assertNull(compiler.getInput("in.js"));
  }

  @Test
  public void testNewExternInput() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var ext;");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var x;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    CompilerInput synthetic = compiler.newExternInput("synthetic_extern.js");
    Assert.assertNotNull(synthetic);
    Assert.assertEquals("synthetic_extern.js", synthetic.getName());
    Assert.assertTrue(synthetic.isExtern());
    Assert.assertSame(synthetic, compiler.getInput("synthetic_extern.js"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInputConflict() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var ext;");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var x;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    compiler.newExternInput("externs.js");
  }

  @Test
  public void testAddAndReplaceIncrementalSourceAst() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var x = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    JsAst newAst = new JsAst(JSSourceFile.fromCode("inc.js", "var y = 2;"));
    compiler.addIncrementalSourceAst(newAst);
    Assert.assertNotNull(compiler.getInput("inc.js"));

    JsAst replaceAst = new JsAst(JSSourceFile.fromCode("inc.js", "var y = 3;"));
    boolean replaced = compiler.replaceIncrementalSourceAst(replaceAst);
    Assert.assertTrue(replaced);
  }

  @Test
  public void testRunCallableWithLargeStack() {
    String res = Compiler.runCallableWithLargeStack(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return "success";
      }
    });
    Assert.assertEquals("success", res);
  }

  @Test
  public void testRunCallableException() {
    try {
      Compiler.runCallable(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          throw new IllegalArgumentException("expected fail");
        }
      }, false, false);
      Assert.fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testGetSourceLineAndRegion() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("code.js", "line1\nline2\nline3\nline4");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);

    Assert.assertNull(compiler.getSourceLine("code.js", 0));
    Assert.assertEquals("line1", compiler.getSourceLine("code.js", 1));
    Assert.assertEquals("line2", compiler.getSourceLine("code.js", 2));
    Assert.assertNull(compiler.getSourceLine("non_existent.js", 1));

    Assert.assertNull(compiler.getSourceRegion("code.js", 0));
    Region region = compiler.getSourceRegion("code.js", 2);
    Assert.assertNotNull(region);
  }

  @Test
  public void testLanguageModeAndEcmaScript() {
    compiler.initCompilerOptionsIfTesting();
    compiler.getOptions().setLanguageIn(LanguageMode.ECMASCRIPT3);
    Assert.assertFalse(compiler.acceptEcmaScript5());
    Assert.assertEquals(LanguageMode.ECMASCRIPT3, compiler.languageMode());

    compiler.getOptions().setLanguageIn(LanguageMode.ECMASCRIPT5);
    Assert.assertTrue(compiler.acceptEcmaScript5());

    compiler.getOptions().setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    Assert.assertTrue(compiler.acceptEcmaScript5());

    compiler.getOptions().acceptConstKeyword = true;
    Assert.assertTrue(compiler.acceptConstKeyword());
  }

  @Test
  public void testPassConfig() {
    PassConfig defaultPasses = compiler.getPassConfig();
    Assert.assertNotNull(defaultPasses);

    Compiler c2 = new Compiler();
    PassConfig customPasses = new DefaultPassConfig(new CompilerOptions());
    c2.setPassConfig(customPasses);
    Assert.assertSame(customPasses, c2.getPassConfig());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPassConfigTwice() {
    compiler.getPassConfig();
    compiler.setPassConfig(new DefaultPassConfig(new CompilerOptions()));
  }

  @Test
  public void testTypeRegistryAndInterpreter() {
    compiler.initCompilerOptionsIfTesting();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    Assert.assertNotNull(registry);
    Assert.assertSame(registry, compiler.getTypeRegistry());

    ReverseAbstractInterpreter rai = compiler.getReverseAbstractInterpreter();
    Assert.assertNotNull(rai);

    TypeValidator tv = compiler.getTypeValidator();
    Assert.assertNotNull(tv);
  }

  @Test
  public void testStateSaveAndRestore() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "var a = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    Assert.assertNotNull(state);

    compiler.setState(state);
    Assert.assertSame(state.externsRoot, compiler.externsRoot);
    Assert.assertSame(state.jsRoot, compiler.jsRoot);
  }

  @Test
  public void testLoggingLevel() {
    Compiler.setLoggingLevel(Level.WARNING);
    Compiler.setLoggingLevel(Level.INFO);
  }

  @Test
  public void testCssRenamingMap() {
    CssRenamingMap map = new CssRenamingMap() {
      @Override
      public String get(String value) {
        return "renamed_" + value;
      }
    };
    compiler.initCompilerOptionsIfTesting();
    compiler.setCssRenamingMap(map);
    Assert.assertSame(map, compiler.getCssRenamingMap());
  }

  @Test
  public void testRegExpGlobalReferences() {
    Assert.assertTrue(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    Assert.assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test
  public void testCodeChangeHandler() {
    final boolean[] changed = new boolean[]{false};
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
      public void reportChange() {
        changed[0] = true;
      }
    };
    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    Assert.assertTrue(changed[0]);

    changed[0] = false;
    compiler.removeChangeHandler(handler);
    compiler.reportCodeChange();
    Assert.assertFalse(changed[0]);
  }

  @Test
  public void testErrorReportingAndDiagnostics() {
    compiler.initCompilerOptionsIfTesting();
    DiagnosticGroup group = compiler.getDiagnosticGroups().forName(DiagnosticGroups.CHECK_VARIABLES);
    Assert.assertNotNull(group);

    JSError error = JSError.make("test.js", 1, 1, CheckLevel.ERROR, Compiler.DUPLICATE_INPUT, "test.js");
    compiler.report(error);
    Assert.assertEquals(1, compiler.getErrorCount());
    Assert.assertTrue(compiler.hasErrors());

    JSError[] errors = compiler.getErrors();
    Assert.assertEquals(1, errors.length);
    Assert.assertEquals(error, errors[0]);

    JSError[] messages = compiler.getMessages();
    Assert.assertEquals(1, messages.length);
    Assert.assertEquals(0, compiler.getWarningCount());
  }

  @Test
  public void testAstDotGraph() throws IOException {
    Assert.assertEquals("", compiler.getAstDotGraph());

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "var a = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    String dot = compiler.getAstDotGraph();
    Assert.assertNotNull(dot);
    Assert.assertTrue(dot.contains("digraph"));
  }

  @Test
  public void testGetNodeForCodeInsertion() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "var a = 1;");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    Node rootNode = compiler.getNodeForCodeInsertion(null);
    Assert.assertNotNull(rootNode);
    Assert.assertEquals(Token.SCRIPT, rootNode.getType());

    JSModule mod = new JSModule("m");
    mod.add(input);
    Node modNode = compiler.getNodeForCodeInsertion(mod);
    Assert.assertNotNull(modNode);
    Assert.assertEquals(Token.SCRIPT, modNode.getType());
  }

  @Test
  public void testToSourceWithDelimiters() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "/** @license MIT */ var a = 1;");
    options.printInputDelimiter = true;
    options.inputDelimiter = "// Input %num%: %name%";
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Node script = compiler.getInput("test.js").getAstRoot(compiler);
    compiler.toSource(cb, 0, script);

    String code = cb.toString();
    Assert.assertTrue(code.contains("// Input 0: test.js"));
    Assert.assertTrue(code.contains("MIT"));
  }

  @Test
  public void testCustomPasses() {
    final boolean[] passRan = new boolean[]{false};
    CompilerPass pass = new CompilerPass() {
      @Override
      public void process(Node externs, Node root) {
        passRan[0] = true;
      }
    };
    options.addCustomPass(CustomPassExecutionTime.BEFORE_CHECKS, pass);
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "var a = 1;");

    compiler.disableThreads();
    compiler.compile(extern, input, options);
    Assert.assertTrue(passRan[0]);
  }

  @Test
  public void testThrowInternalError() {
    try {
      compiler.throwInternalError("Some error message", new RuntimeException("root cause"));
      Assert.fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
      Assert.assertTrue(e.getMessage().contains("Some error message"));
    }
  }

  @Test
  public void testComputeCFG() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "function foo() { return 1; }");
    compiler.init(new JSSourceFile[]{extern}, new JSSourceFile[]{input}, options);
    compiler.parseInputs();

    ControlFlowGraph<Node> cfg = compiler.computeCFG();
    Assert.assertNotNull(cfg);
  }

  @Test
  public void testIsInliningForbidden() {
    compiler.initCompilerOptionsIfTesting();
    options.propertyRenaming = PropertyRenamingPolicy.OFF;
    Assert.assertFalse(compiler.isInliningForbidden());

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    Assert.assertTrue(compiler.isInliningForbidden());

    options.propertyRenaming = PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC;
    Assert.assertTrue(compiler.isInliningForbidden());
  }

  @Test
  public void testDuplicateInputs() {
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input1 = JSSourceFile.fromCode("dupe.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("dupe.js", "var b = 2;");

    compiler.disableThreads();
    compiler.compile(new JSSourceFile[]{extern}, new JSSourceFile[]{input1, input2}, options);
    Assert.assertTrue(compiler.hasErrors());
    Assert.assertEquals(1, compiler.getErrorCount());
  }
}