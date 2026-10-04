package com.google.javascript.jscomp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DiagnosticGroupsTest {

  private DiagnosticGroups diagnosticGroups;

  @Before
  public void setUp() {
    diagnosticGroups = new DiagnosticGroups();
  }

  @Test
  public void testConstructor() {
    DiagnosticGroups groups = new DiagnosticGroups();
    Assert.assertNotNull(groups);
  }

  @Test
  public void testDiagnosticGroupNamesConstant() {
    Assert.assertNotNull(DiagnosticGroups.DIAGNOSTIC_GROUP_NAMES);
    Assert.assertTrue(DiagnosticGroups.DIAGNOSTIC_GROUP_NAMES.contains("accessControls"));
    Assert.assertTrue(DiagnosticGroups.DIAGNOSTIC_GROUP_NAMES.contains("visibility"));
  }

  @Test
  public void testStaticDiagnosticGroupInstances() {
    Assert.assertNotNull(DiagnosticGroups.GLOBAL_THIS);
    Assert.assertNotNull(DiagnosticGroups.DEPRECATED);
    Assert.assertNotNull(DiagnosticGroups.VISIBILITY);
    Assert.assertNotNull(DiagnosticGroups.CONSTANT_PROPERTY);
    Assert.assertNotNull(DiagnosticGroups.NON_STANDARD_JSDOC);
    Assert.assertNotNull(DiagnosticGroups.ACCESS_CONTROLS);
    Assert.assertNotNull(DiagnosticGroups.INVALID_CASTS);
    Assert.assertNotNull(DiagnosticGroups.FILEOVERVIEW_JSDOC);
    Assert.assertNotNull(DiagnosticGroups.STRICT_MODULE_DEP_CHECK);
    Assert.assertNotNull(DiagnosticGroups.EXTERNS_VALIDATION);
    Assert.assertNotNull(DiagnosticGroups.AMBIGUOUS_FUNCTION_DECL);
    Assert.assertNotNull(DiagnosticGroups.UNKNOWN_DEFINES);
    Assert.assertNotNull(DiagnosticGroups.TWEAKS);
    Assert.assertNotNull(DiagnosticGroups.MISSING_PROPERTIES);
    Assert.assertNotNull(DiagnosticGroups.INTERNET_EXPLORER_CHECKS);
    Assert.assertNotNull(DiagnosticGroups.UNDEFINED_VARIABLES);
    Assert.assertNotNull(DiagnosticGroups.CHECK_REGEXP);
    Assert.assertNotNull(DiagnosticGroups.CHECK_TYPES);
    Assert.assertNotNull(DiagnosticGroups.CHECK_VARIABLES);
    Assert.assertNotNull(DiagnosticGroups.CHECK_USELESS_CODE);
    Assert.assertNotNull(DiagnosticGroups.TYPE_INVALIDATION);
  }

  @Test
  public void testForName() {
    Assert.assertEquals(DiagnosticGroups.GLOBAL_THIS, diagnosticGroups.forName("globalThis"));
    Assert.assertEquals(DiagnosticGroups.DEPRECATED, diagnosticGroups.forName("deprecated"));
    Assert.assertEquals(DiagnosticGroups.VISIBILITY, diagnosticGroups.forName("visibility"));
    Assert.assertEquals(DiagnosticGroups.ACCESS_CONTROLS, diagnosticGroups.forName("accessControls"));
    Assert.assertNull(diagnosticGroups.forName("nonExistentGroupName12345"));
  }

  @Test
  public void testGetRegisteredGroups() {
    Map<String, DiagnosticGroup> registered = diagnosticGroups.getRegisteredGroups();
    Assert.assertNotNull(registered);
    Assert.assertTrue(registered.containsKey("globalThis"));
    Assert.assertTrue(registered.containsKey("accessControls"));
    Assert.assertTrue(registered.containsKey("typeInvalidation"));
    Assert.assertEquals(DiagnosticGroups.GLOBAL_THIS, registered.get("globalThis"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetRegisteredGroupsIsImmutable() {
    Map<String, DiagnosticGroup> registered = diagnosticGroups.getRegisteredGroups();
    registered.put("test", DiagnosticGroups.GLOBAL_THIS);
  }

  @Test
  public void testRegisterGroupWithDiagnosticGroupObject() {
    DiagnosticGroup customGroup = new DiagnosticGroup("customGroup1", CheckGlobalThis.GLOBAL_THIS);
    DiagnosticGroup registered = DiagnosticGroups.registerGroup("customGroup1", customGroup);
    Assert.assertSame(customGroup, registered);
    Assert.assertSame(customGroup, diagnosticGroups.forName("customGroup1"));
  }

  @Test
  public void testRegisterGroupWithDiagnosticTypes() {
    DiagnosticGroup registered = DiagnosticGroups.registerGroup(
        "customGroupTypes",
        CheckGlobalThis.GLOBAL_THIS,
        RhinoErrorReporter.TRAILING_COMMA);
    Assert.assertNotNull(registered);
    Assert.assertSame(registered, diagnosticGroups.forName("customGroupTypes"));
  }

  @Test
  public void testRegisterGroupWithNestedDiagnosticGroups() {
    DiagnosticGroup registered = DiagnosticGroups.registerGroup(
        "customGroupNested",
        DiagnosticGroups.GLOBAL_THIS,
        DiagnosticGroups.DEPRECATED);
    Assert.assertNotNull(registered);
    Assert.assertSame(registered, diagnosticGroups.forName("customGroupNested"));
  }

  @Test
  public void testSetWarningLevelsSuccess() {
    CompilerOptions options = new CompilerOptions();
    List<String> names = Arrays.asList("globalThis", "deprecated", "visibility");
    diagnosticGroups.setWarningLevels(options, names, CheckLevel.ERROR);

    Assert.assertEquals(CheckLevel.ERROR, options.getDiagnosticGroupWarningLevel(DiagnosticGroups.GLOBAL_THIS));
    Assert.assertEquals(CheckLevel.ERROR, options.getDiagnosticGroupWarningLevel(DiagnosticGroups.DEPRECATED));
    Assert.assertEquals(CheckLevel.ERROR, options.getDiagnosticGroupWarningLevel(DiagnosticGroups.VISIBILITY));
  }

  @Test
  public void testSetWarningLevelsEmptyList() {
    CompilerOptions options = new CompilerOptions();
    diagnosticGroups.setWarningLevels(options, Collections.<String>emptyList(), CheckLevel.WARNING);
  }

  @Test(expected = NullPointerException.class)
  public void testSetWarningLevelsNonExistentGroup() {
    CompilerOptions options = new CompilerOptions();
    List<String> names = Arrays.asList("globalThis", "invalidGroupName");
    diagnosticGroups.setWarningLevels(options, names, CheckLevel.WARNING);
  }

  @Test(expected = NullPointerException.class)
  public void testSetWarningLevelsNullGroupNameInList() {
    CompilerOptions options = new CompilerOptions();
    List<String> names = Collections.singletonList(null);
    diagnosticGroups.setWarningLevels(options, names, CheckLevel.OFF);
  }
}
