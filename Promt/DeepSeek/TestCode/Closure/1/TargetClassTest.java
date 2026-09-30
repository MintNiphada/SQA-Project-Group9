package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;

public class RemoveUnusedVarsTest extends CompilerTestCase {

  private boolean removeGlobals;
  private boolean preserveFunctionExpressionNames;
  private boolean modifyCallSites;

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new RemoveUnusedVars(compiler, removeGlobals,
        preserveFunctionExpressionNames, modifyCallSites);
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    removeGlobals = true;
    preserveFunctionExpressionNames = false;
    modifyCallSites = false;
  }

  @Test
  public void testRemoveUnusedLocalVar() {
    test("function f(){var x=1}", "function f(){}");
  }

  @Test
  public void testKeepUsedLocalVar() {
    testSame("function f(){var x=1; alert(x)}");
  }

  @Test
  public void testRemoveUnusedGlobalVarWhenRemoveGlobalsTrue() {
    test("var x=1;", "");
  }

  @Test
  public void testKeepUnusedGlobalVarWhenRemoveGlobalsFalse() {
    removeGlobals = false;
    testSame("var x=1;");
  }

  @Test
  public void testRemoveUnusedFunctionDeclaration() {
    test("function foo(){}", "");
  }

  @Test
  public void testKeepUsedFunctionDeclaration() {
    testSame("function foo(){} foo()");
  }

  @Test
  public void testRemoveUnusedFunctionExpressionNameWhenNotPreserved() {
    test("var f = function foo(){};", "var f = function(){};");
  }

  @Test
  public void testKeepFunctionExpressionNameWhenPreserved() {
    preserveFunctionExpressionNames = true;
    testSame("var f = function foo(){};");
  }

  @Test
  public void testRemoveAssignWithoutSecondarySideEffects() {
    test("var x=1; x=2;", "");
  }

  @Test
  public void testKeepAssignWithSecondarySideEffects() {
    testSame("var x=1; x=foo();");
  }

  @Test
  public void testPropertyAssignDoesNotCountAsReference() {
    test("var x={}; x.a=1;", "");
  }

  @Test
  public void testPropertyAssignWithUnknownValueKeepsVar() {
    testSame("var x=foo(); x.a=1;");
  }

  @Test
  public void testPropertyAssignWithAliasedValueKeepsVar() {
    testSame("var x={}; f(x); x.a=1;");
  }

  @Test
  public void testAssignToPropertyOfVarWithNoEscapeRemovesVar() {
    test("var x={}; x.a=1; x.b=2;", "");
  }

  @Test
  public void testInheritanceCallRemovedIfSubclassUnused() {
    test("goog.inherits(B, A);", "");
  }

  @Test
  public void testInheritanceCallKeptIfSubclassUsed() {
    testSame("goog.inherits(B, A); B;");
  }

  @Test
  public void testSingletonGetterCallRemovedIfClassUnused() {
    test("goog.addSingletonGetter(B);", "");
  }

  @Test
  public void testSingletonGetterCallKeptIfClassUsed() {
    testSame("goog.addSingletonGetter(B); B;");
  }

  @Test
  public void testFunctionArgsRemovalFromEnd() {
    test("function f(a,b,c){}", "function f(){}");
  }

  @Test
  public void testFunctionArgsKeepUsedOnes() {
    testSame("function f(a,b,c){return a}");
  }

  @Test
  public void testFunctionArgsRemovalWithCallSiteOptimization() {
    modifyCallSites = true;
    test("function f(a,b){} f(1,2)", "function f(){} f()");
  }

  @Test
  public void testFunctionArgsRemovalOffEndWithCallSiteOptimization() {
    modifyCallSites = true;
    test("function f(a){} f(1,2)", "function f(){} f()");
  }

  @Test
  public void testArgumentsObjectMarksAllParamsReferenced() {
    testSame("function f(a,b){return arguments.length}");
  }

  @Test
  public void testRemoveMultiVarDeclarationOneUnused() {
    test("var a=1, b=2;", "var b=2;");
  }

  @Test
  public void testVarDeclarationWithSideEffectBecomesExpressionResult() {
    test("var a = foo();", "foo();");
  }

  @Test
  public void testForInVarNotRemoved() {
    test("for(var a in b){}", "for(var a in b){}");
  }

  @Test
  public void testCatchVarNotRemoved() {
    test("try{}catch(e){}", "try{}catch(e){}");
  }

  @Test
  public void testAssignToUnknownValueWithoutPropertyAssignRemovesVar() {
    test("var x = foo();", "");
  }

  @Test
  public void testNestedFunctionScopeUnusedVarRemoval() {
    test("function f(){function g(){var y=1}}", "function f(){}");
  }

  @Test
  public void testUnusedVarInNestedFunctionRemovedWhenOuterFunctionUnused() {
    test("function f(){var x=1; function g(){var y=2}}", "");
  }

  @Test
  public void testRemoveUnusedVarWithAssignmentInNestedFunction() {
    test("function f(){var x=1; function g(){x=2}}", "");
  }

  @Test
  public void testKeepVarReferencedInNestedFunction() {
    testSame("function f(){var x=1; function g(){alert(x)}}");
  }

  @Test
  public void testRemoveUnusedVarWithPropertyAssignInNestedFunction() {
    test("function f(){var x={}; function g(){x.a=1}}", "");
  }

  @Test
  public void testKeepVarWithPropertyAssignAndUnknownValueInNestedFunction() {
    testSame("function f(){var x=foo(); function g(){x.a=1}}");
  }

  @Test
  public void testAssignWithPrototypePropertyDoesNotAffectVar() {
    test("var x={}; x.prototype.a=1;", "");
  }

  @Test
  public void testAssignToGetElemProperty() {
    test("var x={}; x[1]=2;", "");
  }

  @Test
  public void testAssignToGetPropOfGetProp() {
    test("var x={}; x.a.b=2;", "");
  }

  @Test
  public void testRemoveUnusedVarWithSideEffectInAssignRhs() {
    test("var x=1; x=foo();", "foo();");
  }

  @Test
  public void testRemoveAssignWithoutSideEffectButVarUsed() {
    testSame("var x=1; alert(x); x=2;");
  }

  @Test
  public void testRemoveUnusedVarWithExprResultUsedAssign() {
    test("var x=1; (x=2);", "");
  }
}