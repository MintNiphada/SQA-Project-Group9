package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerOptions.LifeCycleStage;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CollapseVariableDeclarationsTest {

  @Mock
  private AbstractCompiler mockCompiler;

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    compiler.initOptions(options);
  }

  private Node compile(String js) {
    return compiler.parseSyntheticCode("test", js);
  }

  private void runPass(Compiler compiler, Node externs, Node root) {
    CollapseVariableDeclarations pass = new CollapseVariableDeclarations(compiler);
    pass.process(externs, root);
  }

  private String toSource(Node root) {
    return compiler.toSource(root);
  }

  private Node findFirstVar(Node root) {
    if (root.isVar()) return root;
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstVar(child);
      if (found != null) return found;
    }
    return null;
  }

  @Test
  public void testNoVarDeclarations() {
    Node root = compile("1+2;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("1+2;", toSource(root).trim());
    assertEquals(0, compiler.getChangeCount());
  }

  @Test
  public void testSingleVar() {
    Node root = compile("var a;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a;", toSource(root).trim());
    assertEquals(0, compiler.getChangeCount());
  }

  @Test
  public void testTwoAdjacentVars() {
    Node root = compile("var a; var b;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a,b;", toSource(root).trim());
    assertTrue(compiler.getChangeCount() > 0);
  }

  @Test
  public void testTwoAdjacentVarsWithInit() {
    Node root = compile("var a=1; var b=2;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a=1,b=2;", toSource(root).trim());
  }

  @Test
  public void testThreeAdjacentVars() {
    Node root = compile("var a; var b; var c;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a,b,c;", toSource(root).trim());
  }

  @Test
  public void testVarsSeparatedByStatement() {
    Node root = compile("var a; 1+2; var b;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a;1+2;var b;", toSource(root).trim());
    assertEquals(0, compiler.getChangeCount());
  }

  @Test
  public void testVarsInsideIf() {
    Node root = compile("if(x){var a;} else {var b;}");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    String source = toSource(root).trim();
    assertTrue(source.contains("var a;"));
    assertTrue(source.contains("var b;"));
    assertEquals(0, compiler.getChangeCount());
  }

  @Test
  public void testRedeclarableAssignments() {
    Node root = compile("a=true; b=true; var c=true;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a=true,b=true,c=true;", toSource(root).trim());
  }

  @Test
  public void testBlacklistedStubVar() {
    Node root = compile("var x; x=5;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var x;x=5;", toSource(root).trim());
    assertEquals(0, compiler.getChangeCount());
  }

  @Test
  public void testVarWithInitNotBlacklisted() {
    Node root = compile("var x=1; x=2;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    String source = toSource(root).trim();
    assertTrue(source.contains("var x=1,x=2;"));
    Node varNode = findFirstVar(root);
    assertNotNull(varNode);
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.getSuppressions().contains("duplicate"));
  }

  @Test
  public void testMultipleAssignmentsSameVar() {
    Node root = compile("a=1; a=2; var b=3;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    String source = toSource(root).trim();
    assertTrue(source.contains("var a=1,a=2,b=3;"));
    Node varNode = findFirstVar(root);
    assertNotNull(varNode);
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.getSuppressions().contains("duplicate"));
  }

  @Test
  public void testNoCollapseIfNoVarInChain() {
    Node root = compile("a=1; b=2;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("a=1;b=2;", toSource(root).trim());
    assertEquals(0, compiler.getChangeCount());
  }

  @Test
  public void testCollapseWithOnlyOneVarAndAssignments() {
    Node root = compile("var a; b=1; c=2;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a,b=1,c=2;", toSource(root).trim());
  }

  @Test
  public void testCollapseWithAssignmentsThenVar() {
    Node root = compile("a=1; var b=2;");
    Node externs = compiler.parseSyntheticCode("externs", "");
    runPass(compiler, externs, root);
    assertEquals("var a=1,b=2;", toSource(root).trim());
  }

  @Test(expected = IllegalStateException.class)
  public void testConstructorThrowsIfNormalized() {
    when(mockCompiler.getLifeCycleStage()).thenReturn(LifeCycleStage.NORMALIZED);
    new CollapseVariableDeclarations(mockCompiler);
  }
}
