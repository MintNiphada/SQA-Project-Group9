package com.google.javascript.jscomp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompilerTest {

  private Compiler compiler;
  private CompilerOptions options;
  private JSSourceFile extern;
  private JSSourceFile input;
  private JSModule module;

  @Before
  public void setUp() {
    compiler = new Compiler();
    options = new CompilerOptions();
    extern = JSSourceFile.fromCode("extern.js", "function alert(x) {}");
    input = JSSourceFile.fromCode("input.js", "var a = 1; alert(a);");
    module = new JSModule("testModule");
    module.add(input);
  }

  @Test
  public void testDefaultConstructor() {
    assertNotNull(compiler);
  }

  @Test
  public void testPrintStreamConstructor() {
    PrintStream ps = new PrintStream(new ByteArrayOutputStream());
    Compiler c = new Compiler(ps);
    assertNotNull(c);
    c.initOptions(options);
    assertNotNull(c.getErrorManager());
  }

  @Test
  public void testErrorManagerConstructor() {
    ErrorManager em = new LoggerErrorManager(
        new LightweightMessageFormatter(compiler), null);
    Compiler c = new Compiler(em);
    c.initOptions(options);
    assertSame(em, c.getErrorManager());
  }

  @Test(expected = NullPointerException.class)
  public void testSetErrorManagerNull() {
    compiler.setErrorManager(null);
  }

  @Test
  public void testInitOptionsDefaultErrorManager() {
    compiler.initOptions(options);
    assertNotNull(compiler.getErrorManager());
  }

  @Test
  public void testInitOptionsPrintStream() {
    PrintStream ps = new PrintStream(new ByteArrayOutputStream());
    Compiler c = new Compiler(ps);
    c.initOptions(options);
    assertTrue(c.getErrorManager() instanceof PrintStreamErrorManager);
  }

  @Test
  public void testInitOptionsLogger() {
    Compiler c = new Compiler();
    c.initOptions(options);
    assertTrue(c.getErrorManager() instanceof LoggerErrorManager);
  }

  @Test
  public void testInitWithArrays() {
    JSSourceFile[] externs = { extern };
    JSSourceFile[] inputs = { input };
    compiler.init(externs, inputs, options);
    assertNotNull(compiler.getInputsForTesting());
    assertEquals(1, compiler.getInputsForTesting().size());
  }

  @Test
  public void testInitWithList() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    assertNotNull(compiler.getInputsForTesting());
  }

  @Test
  public void testInitModules() {
    JSSourceFile[] externs = { extern };
    JSModule[] modules = { module };
    compiler.init(externs, modules, options);
    assertNotNull(compiler.getModuleGraph());
  }

  @Test
  public void testInitModulesSingleton() {
    JSSourceFile[] externs = { extern };
    JSModule[] modules = { module };
    compiler.init(externs, modules, options);
    assertNull(compiler.getModuleGraph());
  }

  @Test
  public void testInitModulesMultiple() {
    JSModule mod2 = new JSModule("mod2");
    mod2.add(JSSourceFile.fromCode("mod2.js", "var b = 2;"));
    JSSourceFile[] externs = { extern };
    JSModule[] modules = { module, mod2 };
    compiler.init(externs, modules, options);
    assertNotNull(compiler.getModuleGraph());
  }

  @Test
  public void testInitModulesEmptyModuleThrows() {
    JSModule emptyModule = new JSModule("empty");
    JSSourceFile[] externs = { extern };
    JSModule[] modules = { module, emptyModule };
    compiler.init(externs, modules, options);
    assertFalse(compiler.hasErrors());
  }

  @Test
  public void testInitModulesEmptyRootModuleThrows() {
    JSModule emptyRoot = new JSModule("emptyRoot");
    JSModule mod2 = new JSModule("mod2");
    mod2.add(JSSourceFile.fromCode("mod2.js", "var b = 2;"));
    JSSourceFile[] externs = { extern };
    JSModule[] modules = { emptyRoot, mod2 };
    compiler.init(externs, modules, options);
    assertTrue(compiler.hasErrors());
  }

  @Test
  public void testInitModulesNoModulesError() {
    JSSourceFile[] externs = { extern };
    JSModule[] modules = {};
    compiler.init(externs, modules, options);
    assertTrue(compiler.hasErrors());
  }

  @Test
  public void testCompileSimple() {
    Result res = compiler.compile(extern, input, options);
    assertNotNull(res);
    assertFalse(compiler.hasErrors());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test
  public void testCompileWithModules() {
    Result res = compiler.compile(extern, new JSModule[]{ module }, options);
    assertNotNull(res);
  }

  @Test
  public void testHasErrorsWhenParseFails() {
    CompilerOptions opt = new CompilerOptions();
    JSSourceFile bad = JSSourceFile.fromCode("bad.js", "var a = ;");
    compiler.compile(extern, bad, opt);
    assertTrue(compiler.hasErrors());
  }

  @Test
  public void testGetErrorsAndWarnings() {
    compiler.compile(extern, input, options);
    assertNotNull(compiler.getErrors());
    assertNotNull(compiler.getWarnings());
  }

  @Test
  public void testGetResult() {
    Result res = compiler.compile(extern, input, options);
    assertNotNull(res.variableMap);
    assertNotNull(res.propertyMap);
    assertNotNull(res.stringMap);
    assertNotNull(res.sourceMap);
  }

  @Test
  public void testParseInputs() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    assertNotNull(compiler.getRoot());
    assertEquals(Token.BLOCK, compiler.getRoot().getType());
  }

  @Test
  public void testParseFile() {
    compiler.initCompilerOptionsIfTesting();
    Node root = compiler.parse(extern);
    assertNotNull(root);
  }

  @Test
  public void testParseSyntheticCode() {
    compiler.initCompilerOptionsIfTesting();
    Node root = compiler.parseSyntheticCode("var x = 1;");
    assertNotNull(root);
  }

  @Test
  public void testParseSyntheticCodeWithFileName() {
    compiler.initCompilerOptionsIfTesting();
    Node root = compiler.parseSyntheticCode("test.js", "var y = 2;");
    assertNotNull(root);
  }

  @Test
  public void testToSource() {
    compiler.compile(extern, input, options);
    String source = compiler.toSource();
    assertTrue(source.contains("a = 1"));
  }

  @Test
  public void testToSourceArray() {
    compiler.compile(extern, input, options);
    String[] sources = compiler.toSourceArray();
    assertEquals(1, sources.length);
    assertTrue(sources[0].contains("a = 1"));
  }

  @Test
  public void testToSourceModule() {
    JSModule[] modules = { module };
    compiler.compile(extern, modules, options);
    String source = compiler.toSource(module);
    assertTrue(source.contains("a = 1"));
  }

  @Test
  public void testToSourceArrayModule() {
    JSModule[] modules = { module };
    compiler.compile(extern, modules, options);
    String[] sources = compiler.toSourceArray(module);
    assertEquals(1, sources.length);
  }

  @Test
  public void testToSourceCodeBuilder() {
    compiler.compile(extern, input, options);
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    Node root = compiler.getRoot();
    compiler.toSource(cb, 0, root.getFirstChild().getLastChild());
    assertTrue(cb.toString().contains("a = 1"));
  }

  @Test
  public void testNewExternInput() {
    compiler.initOptions(options);
    CompilerInput newInput = compiler.newExternInput("newExtern.js");
    assertNotNull(newInput);
    assertTrue(newInput.isExtern());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewExternInputDuplicate() {
    compiler.initOptions(options);
    compiler.newExternInput("dup.js");
    compiler.newExternInput("dup.js");
  }

  @Test
  public void testRemoveExternInput() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    InputId externId = extern.getInputId();
    assertNotNull(compiler.getInput(externId));
    compiler.removeExternInput(externId);
    assertNull(compiler.getInput(externId));
  }

  @Test
  public void testAddIncrementalSourceAst() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    JsAst newAst = new JsAst(SourceFile.fromCode("inc.js", "var z = 3;"));
    compiler.addIncrementalSourceAst(newAst);
    assertNotNull(compiler.getInput(newAst.getInputId()));
  }

  @Test
  public void testReplaceIncrementalSourceAst() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    JsAst newAst = new JsAst(SourceFile.fromCode("input.js", "var a = 2;"));
    boolean result = compiler.replaceIncrementalSourceAst(newAst);
    assertTrue(result);
  }

  @Test
  public void testAddNewSourceAst() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    JsAst newAst = new JsAst(SourceFile.fromCode("new.js", "var n = 1;"));
    boolean added = compiler.addNewSourceAst(newAst);
    assertTrue(added);
    assertNotNull(compiler.getInput(newAst.getInputId()));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddNewSourceAstDuplicate() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    JsAst dupAst = new JsAst(SourceFile.fromCode("input.js", "var a = 1;"));
    compiler.addNewSourceAst(dupAst);
  }

  @Test
  public void testGetModuleGraphNullSingleton() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    assertNull(compiler.getModuleGraph());
  }

  @Test
  public void testGetDegenerateModuleGraph() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    assertNotNull(compiler.getDegenerateModuleGraph());
  }

  @Test
  public void testProcessDefines() {
    compiler.compile(extern, input, options);
    compiler.processDefines();
  }

  @Test
  public void testNormalize() {
    compiler.compile(extern, input, options);
    compiler.normalize();
  }

  @Test
  public void testComputeCFG() {
    compiler.compile(extern, input, options);
    ControlFlowGraph<Node> cfg = compiler.computeCFG();
    assertNotNull(cfg);
  }

  @Test
  public void testBuildKnownSymbolTable() {
    compiler.compile(extern, input, options);
    SymbolTable table = compiler.buildKnownSymbolTable();
    assertNotNull(table);
  }

  @Test
  public void testOptimize() {
    compiler.compile(extern, input, options);
    compiler.optimize();
  }

  @Test
  public void testRecordFunctionInformation() {
    options.recordFunctionInformation = true;
    compiler.compile(extern, input, options);
    assertNotNull(compiler.getFunctionalInformationMap());
  }

  @Test
  public void testGetTypeRegistry() {
    compiler.initOptions(options);
    assertNotNull(compiler.getTypeRegistry());
  }

  @Test
  public void testGetTypedScopeCreator() {
    compiler.initOptions(options);
    assertNull(compiler.getTypedScopeCreator());
  }

  @Test
  public void testGetTopScope() {
    compiler.compile(extern, input, options);
    assertNull(compiler.getTopScope());
  }

  @Test
  public void testGetReverseAbstractInterpreter() {
    compiler.initOptions(options);
    assertNotNull(compiler.getReverseAbstractInterpreter());
  }

  @Test
  public void testGetTypeValidator() {
    compiler.initOptions(options);
    assertNotNull(compiler.getTypeValidator());
  }

  @Test
  public void testGetCodingConvention() {
    assertNotNull(compiler.getCodingConvention());
  }

  @Test
  public void testIsIdeMode() {
    assertFalse(compiler.isIdeMode());
  }

  @Test
  public void testAcceptEcmaScript5() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
    assertTrue(compiler.acceptEcmaScript5());
  }

  @Test
  public void testLanguageMode() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertEquals(LanguageMode.ECMASCRIPT3, compiler.languageMode());
  }

  @Test
  public void testAcceptConstKeyword() {
    options.acceptConstKeyword = true;
    compiler.initOptions(options);
    assertTrue(compiler.acceptConstKeyword());
  }

  @Test
  public void testGetParserConfig() {
    assertNotNull(compiler.getParserConfig());
  }

  @Test
  public void testIsTypeCheckingEnabled() {
    options.checkTypes = true;
    compiler.initOptions(options);
    assertTrue(compiler.isTypeCheckingEnabled());
  }

  @Test
  public void testReportError() {
    compiler.initOptions(options);
    JSError error = JSError.make("input.js", 1, 0, Compiler.DUPLICATE_INPUT, "test");
    compiler.report(error);
    assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testGetErrorLevel() {
    compiler.initOptions(options);
    JSError error = JSError.make("input.js", 1, 0, Compiler.DUPLICATE_INPUT, "test");
    assertNotNull(compiler.getErrorLevel(error));
  }

  @Test
  public void testThrowInternalError() {
    try {
      compiler.throwInternalError("test error", new Exception("cause"));
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
    }
  }

  @Test
  public void testHasHaltingErrors() {
    compiler.initOptions(options);
    assertFalse(compiler.hasHaltingErrors());
    JSError error = JSError.make("input.js", 1, 0, Compiler.DUPLICATE_INPUT, "test");
    compiler.report(error);
    assertTrue(compiler.hasHaltingErrors());
  }

  @Test
  public void testAddToDebugLog() {
    compiler.addToDebugLog("debug message");
    String log = compiler.getResult().debugLog;
    assertTrue(log.contains("debug message"));
  }

  @Test
  public void testGetSourceLine() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    String line = compiler.getSourceLine("input.js", 1);
    assertEquals("var a = 1; alert(a);", line);
  }

  @Test
  public void testGetSourceRegion() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    Region region = compiler.getSourceRegion("input.js", 1);
    assertNotNull(region);
  }

  @Test
  public void testGetNodeForCodeInsertion() {
    compiler.compile(extern, input, options);
    Node node = compiler.getNodeForCodeInsertion(null);
    assertNotNull(node);
    assertEquals(Token.SCRIPT, node.getType());
  }

  @Test
  public void testGetSourceMap() {
    options.sourceMapOutputPath = "/dev/null";
    compiler.compile(extern, input, options);
    assertNotNull(compiler.getSourceMap());
  }

  @Test
  public void testGetVariableMap() {
    compiler.compile(extern, input, options);
    assertNotNull(compiler.getVariableMap());
  }

  @Test
  public void testGetPropertyMap() {
    compiler.compile(extern, input, options);
    assertNotNull(compiler.getPropertyMap());
  }

  @Test
  public void testSetLoggingLevel() {
    Compiler.setLoggingLevel(java.util.logging.Level.OFF);
  }

  @Test
  public void testGetAstDotGraph() throws Exception {
    compiler.compile(extern, input, options);
    String dot = compiler.getAstDotGraph();
    assertNotNull(dot);
    assertFalse(dot.isEmpty());
  }

  @Test
  public void testGetInputsInOrder() {
    compiler.compile(extern, input, options);
    List<CompilerInput> inputs = compiler.getInputsInOrder();
    assertEquals(1, inputs.size());
  }

  @Test
  public void testGetInputsById() {
    compiler.compile(extern, input, options);
    Map<InputId, CompilerInput> map = compiler.getInputsById();
    assertTrue(map.containsKey(input.getInputId()));
  }

  @Test
  public void testGetExternsInOrder() {
    compiler.compile(extern, input, options);
    List<CompilerInput> externs = compiler.getExternsInOrder();
    assertEquals(1, externs.size());
  }

  @Test
  public void testStateSerialization() {
    compiler.compile(extern, input, options);
    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);
    Compiler compiler2 = new Compiler();
    compiler2.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler2.setState(state);
    assertEquals(compiler.getErrorCount(), compiler2.getErrorCount());
  }

  @Test
  public void testSetPassConfig() {
    Compiler c = new Compiler();
    c.initOptions(options);
    PassConfig pc = new DefaultPassConfig(options);
    c.setPassConfig(pc);
    assertSame(pc, c.getPassConfig());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetPassConfigTwice() {
    Compiler c = new Compiler();
    c.initOptions(options);
    c.setPassConfig(new DefaultPassConfig(options));
    c.setPassConfig(new DefaultPassConfig(options));
  }

  @Test(expected = NullPointerException.class)
  public void testSetPassConfigNull() {
    compiler.setPassConfig(null);
  }

  @Test
  public void testHasRegExpGlobalReferencesDefault() {
    assertTrue(compiler.hasRegExpGlobalReferences());
  }

  @Test
  public void testSetHasRegExpGlobalReferences() {
    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test
  public void testGetGlobalVarReferencesInitiallyNull() {
    assertNull(compiler.getGlobalVarReferences());
  }

  @Test
  public void testUpdateGlobalVarReferences() {
    compiler.compile(extern, input, options);
    compiler.updateGlobalVarReferences(
        new HashMap<Scope.Var, ReferenceCollection>(),
        compiler.getRoot().getFirstChild().getLastChild());
    assertNotNull(compiler.getGlobalVarReferences());
  }

  @Test
  public void testGetSynthesizedExternsInput() {
    compiler.initOptions(options);
    CompilerInput synth = compiler.getSynthesizedExternsInput();
    assertNotNull(synth);
    assertTrue(synth.getName().contains(Compiler.SYNTHETIC_EXTERNS));
  }

  @Test
  public void testProgress() {
    assertEquals(0.0, compiler.getProgress(), 0.001);
    compiler.setProgress(0.5);
    assertEquals(0.5, compiler.getProgress(), 0.001);
    compiler.setProgress(1.5);
    assertEquals(1.0, compiler.getProgress(), 0.001);
    compiler.setProgress(-0.5);
    assertEquals(0.0, compiler.getProgress(), 0.001);
  }

  @Test
  public void testReplaceScript() {
    compiler.compile(extern, input, options);
    JsAst newAst = new JsAst(SourceFile.fromCode("input.js", "var a = 2;"));
    compiler.replaceScript(newAst);
    String source = compiler.toSource();
    assertTrue(source.contains("a = 2"));
  }

  @Test
  public void testAddNewScript() {
    compiler.compile(extern, input, options);
    JsAst newAst = new JsAst(SourceFile.fromCode("newFile.js", "var n = 3;"));
    compiler.addNewScript(newAst);
    String source = compiler.toSource();
    assertTrue(source.contains("n = 3"));
  }

  @Test
  public void testEnsureLibraryInjected() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    Node lastChild = compiler.ensureLibraryInjected("base");
    assertNotNull(lastChild);
  }

  @Test
  public void testLoadLibraryCode() {
    Node lib = compiler.loadLibraryCode("base");
    assertNotNull(lib);
  }

  @Test
  public void testInitInputsByIdMapDuplicateExtern() {
    compiler.init(Lists.newArrayList(extern, extern), Lists.newArrayList(input), options);
    assertTrue(compiler.hasErrors());
  }

  @Test
  public void testInitInputsByIdMapDuplicateInput() {
    JSSourceFile dupInput = JSSourceFile.fromCode("input.js", "var b = 2;");
    List<JSModule> modules = Lists.newArrayList(module);
    module.add(dupInput);
    compiler.init(Lists.newArrayList(extern), modules, options);
    assertTrue(compiler.hasErrors());
  }

  @Test
  public void testCreateFillFileName() {
    assertEquals("[mod]", Compiler.createFillFileName("mod"));
  }

  @Test
  public void testFillEmptyModules() {
    JSModule empty = new JSModule("empty");
    List<JSModule> mods = Lists.newArrayList(module, empty);
    Compiler.fillEmptyModules(mods);
    assertFalse(empty.getInputs().isEmpty());
  }

  @Test
  public void testCheckFirstModule() {
    List<JSModule> mods = Lists.newArrayList();
    compiler.init(Lists.newArrayList(extern), mods, options);
    assertTrue(compiler.hasErrors());
  }

  @Test
  public void testCodeBuilder() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    cb.append("hello");
    cb.append("\nworld");
    assertEquals(6, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(5, cb.getColumnIndex());
    assertTrue(cb.endsWith("world"));
    assertFalse(cb.endsWith("hello"));
    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals("", cb.toString());
  }

  @Test
  public void testProcessDefinesAfterCompile() {
    compiler.compile(extern, input, options);
    compiler.processDefines();
  }

  @Test
  public void testIsInliningForbidden() {
    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    compiler.initOptions(options);
    assertTrue(compiler.isInliningForbidden());
    options.propertyRenaming = PropertyRenamingPolicy.ALL_UNQUOTED;
    compiler.initOptions(options);
    assertFalse(compiler.isInliningForbidden());
  }

  @Test
  public void testGetMessages() {
    compiler.compile(extern, input, options);
    JSError[] messages = compiler.getMessages();
    assertNotNull(messages);
  }

  @Test
  public void testUniqueNameId() {
    compiler.initOptions(options);
    Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    String id1 = supplier.get();
    String id2 = supplier.get();
    assertFalse(id1.equals(id2));
    compiler.resetUniqueNameId();
    String id3 = supplier.get();
    assertEquals(id1, id3);
  }

  @Test
  public void testPrecheckReturnsTrue() {
    assertTrue(compiler.precheck());
  }

  @Test
  public void testProcessAMDAndCommonJSModules() {
    options.processCommonJSModules = true;
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
  }

  @Test
  public void testSetCssRenamingMap() {
    CssRenamingMap map = new CssRenamingMap.ByWhole();
    compiler.setCssRenamingMap(map);
    assertSame(map, compiler.getCssRenamingMap());
  }

  @Test
  public void testGetPassConfig_createsDefaultIfNull() {
    Compiler c = new Compiler();
    assertNotNull(c.getPassConfig());
  }

  @Test
  public void testGetNodeForCodeInsertionModule() {
    compiler.compile(extern, new JSModule[]{module}, options);
    Node node = compiler.getNodeForCodeInsertion(module);
    assertNotNull(node);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetNodeForCodeInsertionEmptyModule() {
    JSModule emptyModule = new JSModule("emptyModule");
    compiler.compile(extern, new JSModule[]{module}, options);
    compiler.getNodeForCodeInsertion(emptyModule);
  }

  @Test
  public void testHasErrorsWithIdeMode() {
    options.ideMode = true;
    compiler.compile(extern, JSSourceFile.fromCode("bad.js", "var a = ;"), options);
    assertFalse(compiler.hasErrors());
  }

  @Test
  public void testAcceptEcmaScript5False() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertFalse(compiler.acceptEcmaScript5());
  }

  @Test
  public void testGetParserConfigWithDifferentLanguages() {
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    compiler.initOptions(options);
    assertNotNull(compiler.getParserConfig());
  }

  @Test
  public void testIsTypeCheckingEnabledFalse() {
    compiler.initOptions(options);
    assertFalse(compiler.isTypeCheckingEnabled());
  }

  @Test
  public void testToSourceWithInputDelimiter() {
    options.printInputDelimiter = true;
    options.inputDelimiter = "// Input %num%: %name%";
    compiler.compile(extern, input, options);
    String src = compiler.toSource();
    assertTrue(src.contains("// Input 0: input.js"));
  }

  @Test
  public void testToSourceWithSourceMap() {
    options.sourceMapOutputPath = "/dev/null";
    compiler.compile(extern, input, options);
    compiler.toSource();
  }

  @Test
  public void testToSourceModuleNoInputs() {
    JSModule empty = new JSModule("empty");
    compiler.compile(extern, new JSModule[]{empty}, options);
    String src = compiler.toSource(empty);
    assertEquals("", src);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToSourceModuleNullAst() {
    JSModule moduleWithoutAst = new JSModule("mod");
    moduleWithoutAst.add(new CompilerInput(SourceFile.fromCode("nonexistent.js", ""), false));
    compiler.compile(extern, new JSModule[]{moduleWithoutAst}, options);
    compiler.toSource(moduleWithoutAst);
  }

  @Test
  public void testEnsureLibraryInjectedTwice() {
    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();
    Node first = compiler.ensureLibraryInjected("base");
    assertNotNull(first);
    Node second = compiler.ensureLibraryInjected("base");
    assertNull(second);
  }

  @Test
  public void testLoadLibraryCodeInvalidResource() {
    try {
      compiler.loadLibraryCode("nonexistent");
      fail("Expected RuntimeException");
    } catch (RuntimeException expected) {
    }
  }
}
