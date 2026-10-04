package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompilerOptionsTest {

  @Test
  public void testDefaultValues() {
    CompilerOptions options = new CompilerOptions();
    Assert.assertFalse(options.ideMode);
    Assert.assertFalse(options.skipAllPasses);
    Assert.assertFalse(options.nameAnonymousFunctionsOnly);
    Assert.assertEquals(CompilerOptions.DevMode.OFF, options.devMode);
    Assert.assertFalse(options.manageClosureDependencies);
    Assert.assertNull(options.messageBundle);
    Assert.assertFalse(options.checkSymbols);
    Assert.assertEquals(CheckLevel.OFF, options.checkShadowVars);
    Assert.assertEquals(CheckLevel.OFF, options.aggressiveVarCheck);
    Assert.assertEquals(CheckLevel.OFF, options.checkFunctions);
    Assert.assertEquals(CheckLevel.OFF, options.checkMethods);
    Assert.assertFalse(options.checkDuplicateMessages);
    Assert.assertFalse(options.allowLegacyJsMessages);
    Assert.assertFalse(options.strictMessageReplacement);
    Assert.assertFalse(options.checkSuspiciousCode);
    Assert.assertFalse(options.checkControlStructures);
    Assert.assertEquals(CheckLevel.OFF, options.checkUndefinedProperties);
    Assert.assertFalse(options.checkUnusedPropertiesEarly);
    Assert.assertFalse(options.checkTypes);
    Assert.assertFalse(options.tightenTypes);
    Assert.assertFalse(options.inferTypesInGlobalScope);
    Assert.assertFalse(options.checkTypedPropertyCalls);
    Assert.assertEquals(CheckLevel.OFF, options.reportMissingOverride);
    Assert.assertEquals(CheckLevel.OFF, options.reportUnknownTypes);
    Assert.assertEquals(CheckLevel.OFF, options.checkRequires);
    Assert.assertEquals(CheckLevel.OFF, options.checkProvides);
    Assert.assertEquals(CheckLevel.OFF, options.checkGlobalNamesLevel);
    Assert.assertEquals(CheckLevel.ERROR, options.brokenClosureRequiresLevel);
    Assert.assertEquals(CheckLevel.OFF, options.checkGlobalThisLevel);
    Assert.assertEquals(CheckLevel.OFF, options.checkUnreachableCode);
    Assert.assertEquals(CheckLevel.OFF, options.checkMissingReturn);
    Assert.assertEquals(CheckLevel.OFF, options.checkMissingGetCssNameLevel);
    Assert.assertNull(options.checkMissingGetCssNameBlacklist);
    Assert.assertFalse(options.checkEs5Strict);
    Assert.assertFalse(options.checkCaja);
    Assert.assertFalse(options.computeFunctionSideEffects);
    Assert.assertFalse(options.chainCalls);
    Assert.assertTrue(options.removeUnusedVarsInGlobalScope);
    Assert.assertEquals(VariableRenamingPolicy.OFF, options.variableRenaming);
    Assert.assertEquals(PropertyRenamingPolicy.OFF, options.propertyRenaming);
    Assert.assertEquals(AnonymousFunctionNamingPolicy.OFF, options.anonymousFunctionNaming);
    Assert.assertTrue(options.rewriteNewDateGoogNow);
    Assert.assertTrue(options.removeAbstractMethods);
    Assert.assertEquals(CompilerOptions.TracerMode.OFF, options.tracer);
    Assert.assertEquals(ErrorFormat.SINGLELINE, options.errorFormat);
    Assert.assertNull(options.getWarningsGuard());
    Assert.assertEquals(1, options.summaryDetailLevel);
    Assert.assertFalse(options.isExternExportsEnabled());
    Assert.assertEquals(SourceMap.DetailLevel.SYMBOLS, options.sourceMapDetailLevel);
    Assert.assertFalse(options.looseTypes);
  }

  @Test
  public void testDefineReplacements() {
    CompilerOptions options = new CompilerOptions();
    options.setDefineToBooleanLiteral("DEF_TRUE", true);
    options.setDefineToBooleanLiteral("DEF_FALSE", false);
    options.setDefineToNumberLiteral("DEF_INT", 42);
    options.setDefineToDoubleLiteral("DEF_DOUBLE", 3.14);
    options.setDefineToStringLiteral("DEF_STR", "hello");

    Map<String, Node> map = options.getDefineReplacements();
    Assert.assertEquals(5, map.size());

    Node trueNode = map.get("DEF_TRUE");
    Assert.assertEquals(Token.TRUE, trueNode.getType());

    Node falseNode = map.get("DEF_FALSE");
    Assert.assertEquals(Token.FALSE, falseNode.getType());

    Node intNode = map.get("DEF_INT");
    Assert.assertEquals(Token.NUMBER, intNode.getType());
    Assert.assertEquals(42.0, intNode.getDouble(), 0.0);

    Node doubleNode = map.get("DEF_DOUBLE");
    Assert.assertEquals(Token.NUMBER, doubleNode.getType());
    Assert.assertEquals(3.14, doubleNode.getDouble(), 0.0);

    Node strNode = map.get("DEF_STR");
    Assert.assertEquals(Token.STRING, strNode.getType());
    Assert.assertEquals("hello", strNode.getString());
  }

  @Test
  public void testPassAndExecutionSettings() {
    CompilerOptions options = new CompilerOptions();
    options.skipAllCompilerPasses();
    Assert.assertTrue(options.skipAllPasses);

    options.setNameAnonymousFunctionsOnly(true);
    Assert.assertTrue(options.nameAnonymousFunctionsOnly);

    options.setChainCalls(true);
    Assert.assertTrue(options.chainCalls);

    options.setManageClosureDependencies(true);
    Assert.assertTrue(options.manageClosureDependencies);

    options.setRewriteNewDateGoogNow(false);
    Assert.assertFalse(options.rewriteNewDateGoogNow);

    options.setRemoveAbstractMethods(false);
    Assert.assertFalse(options.removeAbstractMethods);

    options.setSummaryDetailLevel(3);
    Assert.assertEquals(3, options.summaryDetailLevel);
  }

  @Test
  public void testWarningsGuardsAndLevels() {
    CompilerOptions options = new CompilerOptions();
    DiagnosticGroup group = DiagnosticGroups.NON_STANDARD_JSDOC;

    Assert.assertFalse(options.enables(group));
    Assert.assertFalse(options.disables(group));

    options.setWarningLevel(group, CheckLevel.WARNING);
    Assert.assertNotNull(options.getWarningsGuard());
    Assert.assertTrue(options.enables(group));

    options.setWarningLevel(group, CheckLevel.OFF);
    Assert.assertTrue(options.disables(group));

    WarningsGuard guard = new DiagnosticGroupWarningsGuard(DiagnosticGroups.ACCESS_CONTROLS, CheckLevel.ERROR);
    options.addWarningsGuard(guard);
    Assert.assertTrue(options.enables(DiagnosticGroups.ACCESS_CONTROLS));
  }

  @Test
  public void testRenamingAndTypeProperties() {
    CompilerOptions options = new CompilerOptions();
    options.setRenamingPolicy(VariableRenamingPolicy.ALL, PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC);
    Assert.assertEquals(VariableRenamingPolicy.ALL, options.variableRenaming);
    Assert.assertEquals(PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC, options.propertyRenaming);

    options.setCollapsePropertiesOnExternTypes(true);
    Assert.assertTrue(options.collapsePropertiesOnExternTypes);

    options.setProcessObjectPropertyString(true);
    Assert.assertTrue(options.processObjectPropertyString);

    Set<String> idGens = new HashSet<String>(Arrays.asList("gen1", "gen2"));
    options.setIdGenerators(idGens);
    Assert.assertEquals(idGens, options.idGenerators);

    options.setReplaceStringsConfiguration("?", Arrays.asList("desc1", "desc2"));
    Assert.assertEquals("?", options.replaceStringsPlaceholderToken);
    Assert.assertEquals(Arrays.asList("desc1", "desc2"), options.replaceStringsFunctionDescriptions);

    options.setLooseTypes(true);
    Assert.assertTrue(options.looseTypes);

    options.outputCharset = Charset.forName("UTF-8");
    Assert.assertEquals(Charset.forName("UTF-8"), options.outputCharset);
  }

  @Test
  public void testRuntimeTypeCheckAndCodingConvention() {
    CompilerOptions options = new CompilerOptions();
    options.enableRuntimeTypeCheck("myLogFunc");
    Assert.assertTrue(options.runtimeTypeCheck);
    Assert.assertEquals("myLogFunc", options.runtimeTypeCheckLogFunction);

    options.disableRuntimeTypeCheck();
    Assert.assertFalse(options.runtimeTypeCheck);

    CodingConvention convention = new GoogleCodingConvention();
    options.setCodingConvention(convention);
    Assert.assertSame(convention, options.getCodingConvention());
  }

  @Test
  public void testColorizeErrorOutputAndExternExports() {
    CompilerOptions options = new CompilerOptions();
    Assert.assertFalse(options.shouldColorizeErrorOutput());
    options.setColorizeErrorOutput(true);
    Assert.assertTrue(options.shouldColorizeErrorOutput());

    Assert.assertFalse(options.isExternExportsEnabled());
    options.enableExternExports(true);
    Assert.assertTrue(options.isExternExportsEnabled());
    options.enableExternExports(false);
    Assert.assertFalse(options.isExternExportsEnabled());
  }

  @Test
  public void testClone() throws Exception {
    CompilerOptions options = new CompilerOptions();
    options.checkSymbols = true;
    options.setDefineToStringLiteral("DEF", "VAL");
    options.setSummaryDetailLevel(2);

    CompilerOptions clone = (CompilerOptions) options.clone();
    Assert.assertNotSame(options, clone);
    Assert.assertTrue(clone.checkSymbols);
    Assert.assertEquals(2, clone.summaryDetailLevel);
    Assert.assertEquals("VAL", clone.getDefineReplacements().get("DEF").getString());
  }

  @Test
  public void testTracerModeEnum() {
    Assert.assertTrue(CompilerOptions.TracerMode.ALL.isOn());
    Assert.assertTrue(CompilerOptions.TracerMode.FAST.isOn());
    Assert.assertFalse(CompilerOptions.TracerMode.OFF.isOn());
  }

  @Test
  public void testDevModeEnum() {
    Assert.assertEquals(4, CompilerOptions.DevMode.values().length);
    Assert.assertEquals(CompilerOptions.DevMode.OFF, CompilerOptions.DevMode.valueOf("OFF"));
    Assert.assertEquals(CompilerOptions.DevMode.START, CompilerOptions.DevMode.valueOf("START"));
    Assert.assertEquals(CompilerOptions.DevMode.START_AND_END, CompilerOptions.DevMode.valueOf("START_AND_END"));
    Assert.assertEquals(CompilerOptions.DevMode.EVERY_PASS, CompilerOptions.DevMode.valueOf("EVERY_PASS"));
  }
}
