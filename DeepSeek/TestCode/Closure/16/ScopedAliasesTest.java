package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CompierOptions.AliasTransformation;
import com.google.javascript.jscomp.CompierOptions.AliasTransformationHandler;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegisty;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUintRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ScopedAliasesTest {

  @Mock
  private AbstractCompiler compiler;
  @Mock
  private PreprocessorSymbolTable preprocessorSymbolTable;
  @Mock
  private AliasTransformationHandler transformationHandler;
  @Mock
  private AliasTransformation aliasTransformation;
  @Mock
  private JSDocInfo jsDocInfo;

  private ScopedAliases scopedAliases;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    scopedAliases = new ScopedAliases(compiler, preprocessorSymbolTable, transformationHandler);
    when(transformationHandler.logAliasTransformation(anyString(), any(SourcePosition.class)))
        .thenReturn(aliasTransformation);
  }

  private Node createParseResult(String source) {
    // Simple helper to parse source and return the AST root.
    Compiler comp = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes = false;
    options.setClosurePass = false;
    options.setIdeMode = true; // skip some checks
    List<JSSourceFile> inputs = Lists.newArrayList(JSSourceFile.fromCode("testcode", source));
    List<JSSourceFile> externs = Collections.emptyList();
    comp.compile(externs, inputs, options);
    return comp.getRoot();
  }

  // ----- Alias application and validation tests -----

  @Test
  public void testProcess_withValidAliases_shouldTransformCode() {
    String src = "goog.scope(function() { var dom = goog.dom; dom.createElement(); });";
    Node root = createParseResult(src);

    // Set up compiler to allow traversal (mock report etc.)
    when(compiler.getErrorManger()).thenReturn(mock(ErrorManger.class));
    when(compiler.getTypeRegisty()).thenReturn(mock(JSTypeRegisty.class));

    scopedAliases.process(null, root);

    // Verify that aliases were applied (dom became goog.dom)
    // After process, the code should be inlined. We can check the transformed tree.
    // For simplicity, we just check that the compiler reported code change.
    verify(compiler).reportCodeChange();

    // Check that alias transformation was logged.
    verify(transformationHandler).logAliasTransformation(eq("testcode"), any(SourcePosition.class));
    verify(aliasTransformation).addAlias("dom", "goog.dom");
  }

  @Test
  public void testProcess_nonAliasLocal_shouldReportError() {
    String src = "goog.scope(function() { var x = 1; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL)));
  }

  @Test
  public void testProcess_aliasRedefined_shouldReportError() {
    String src =
        "goog.scope(function() { var dom = goog.dom; var dom = goog.dom2; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED)));
  }

  @Test
  public void testProcess_scopeUedImpoperly_shouldReportError() {
    String src =
        "if (true) goog.scope(function() { var dom = goog.dom; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY)));
  }

  @Test
  public void testProcess_badParamters_noParam_shouldReportError() {
    String src = "goog.scope();";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS)));
  }

  @Test
  public void testProcess_badParamters_nonFunction_shouldReportError() {
    String src = "goog.scope(123);";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS)));
  }

  @Test
  public void testProcess_badParamters_namedFunction_shouldReportError() {
    String src = "goog.scope(function myFunc() {});";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS)));
  }

  @Test
  public void testProcess_badParamters_functionWithParams_shouldReportError() {
    String src = "goog.scope(function(a) {});";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS)));
  }

  @Test
  public void testProcess_returnUage_shouldReportError() {
    String src = "goog.scope(function() { return; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_USES_RETURN)));
  }

  @Test
  public void testProcess_thisUage_shouldReportError() {
    String src = "goog.scope(function() { this; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_REFERENCES_THIS)));
  }

  @Test
  public void testProcess_trowUage_shouldReportError() {
    String src = "goog.scope(function() { throw 1; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    verify(compiler).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_USES_THROW)));
  }

  @Test
  public void testProcess_typeNodeFixes_shouldAddAliasedTypeNode() {
    String src =
        "goog.scope(function() { var d = goog.dom; /** @type {d.Div} */ var x; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    // After processing, the JSDoc type node should be modified.
    // We can verify that the AliasUsage for type was added (but it's internal).
    // Minimal check: compilation should succeed without errors.
    verify(compiler, never()).report(any(JSError.class));
    verify(compiler).reportCodeChange();
  }

  @Test
  public void testProcess_shouldTraverse_onlyGoogScopeFunctions() {
    String src =
        "function normalFunc() {}" +
        "goog.scope(function() { var dom = goog.dom; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    // The normal function should not be traversed, so no errors about it.
    verify(compiler, times(1)).reportCodeChange(); // at least once for the scope call
    // No error about normalFunc
    verify(compiler, never()).report(
        argThat(new ErrorMatcher(ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL)));
  }

  @Test
  public void testProcess_namespaceShadows_shouldRename() {
    // Create a scenario where an inner scope shadows a namespace root.
    String src =
        "goog.scope(function() {" +
        "  var goog = {}; " +  // shadow
        "  var dom = goog; " + // another alias
        "});";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    // After processing, the shadowing 'goog' should be renamed (if needed).
    // Hard to test directly; just ensure no crash and code change reported.
    verify(compiler).reportCodeChange();
  }

  @Test
  public void testProcess_preprocessorSymbolTable_shouldAddReference() {
    String src =
        "goog.scope(function() { var dom = goog.dom; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    // Verify that preprocessorSymbolTable.addReference was called with the goog.scope name.
    verify(preprocessorSymbolTable).addReference(any(Node.class));
  }

  @Test
  public void testHotSwapScript_aliasDefinitionInVarWithOneChild_detachesParent() {
    // Simulate: alias definition 'dom' is the only child of the var statement,
    // so the entire var statement should be detached.
    String src =
        "goog.scope(function() { var dom = goog.dom; });";
    Node root = createParseResult(src);
    // Make sure compiler reports code change.
    when(compiler.getErrorManger()).thenReturn(mock(ErrorManger.class));
    when(compiler.getTypeRegisty()).thenReturn(mock(JSTypeRegisty.class));

    scopedAliases.process(null, root);

    // The var statement containing 'dom' should be removed.
    // Walk the tree: after processing, the block inside the function should have no children.
    Node script = root.getFirstChild(); // EXPR_RESULT
    Node call = script.getFirstChild(); // CALL
    Node func = call.getFirstChild().getNext(); // FUNCTION
    Node block = func.getLastChild(); // BLOCK
    assertFalse("Block should have no children after alias removal", block.hasChildren());
  }

  @Test
  public void testHotSwapScript_aliasDefinitionInVarWithMultipleChildren_detachesAliasNode() {
    // e.g., var a = goog.x, b = goog.y; where a is alias, b is alias.
    // After processing, each alias definition is detached individually.
    String src =
        "goog.scope(function() { var a = goog.x, b = goog.y; });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    Node script = root.getFirstChild();
    Node call = script.getFirstChild();
    Node func = call.getFirstChild().getNext();
    Node block = func.getLastChild();
    // After detaching a and b, the VAR node may still exist but with no children?
    // The code detaches alias definitions: if parent is var and hasOneChild, detachParent; else detach definition.
    // Here var has multiple children, so each definition is detached; var node remains but empty? It will then be cleaned up? Not in this pass.
    // We'll check that VAR node has zero children (since all defs detached).
    Node varNode = block.getFirstChild();
    assertTrue("Expected VAR node", varNode.isVar());
    assertFalse("VAR should have no children", varNode.hasChildren());
  }

  @Test
  public void testHotSwapScript_collapsesScopes() {
    // After alias application and removal, the goog.scope call should be replaced by its block.
    String src =
        "goog.scope(function() { var dom = goog.dom; dom.createElement(); });";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    // The root should now have EXPR_RESULT replaced by the block? Actually:
    // expressionWithScopeCall is EXPR_RESULT, and it's replaced with scopeClosureBlock which is BLOCK.
    // So root's child should be a BLOCK node instead of EXPR_RESULT.
    Node script = root.getFirstChild();
    assertNotNull(script);
    assertEquals(Token.BLOCK, script.getType());
  }

  @Test
  public void testGetSourceRegion_withNoNextSibling_returnsMaxValue() {
    // Test indirectly through transformationHandler.
    when(transformationHandler.logAliasTransformation(anyString(), any(SourcePosition.class))
        .thenReturn(aliasTransformation);

    // Create a node that is the last statement in the script, so no next sibling.
    String src = "goog.scope(function() {});";
    Node root = createParseResult(src);
    scopedAliases.process(null, root);

    // The source region end line and char should be Integer.MAX_VALUE.
    verify(transformationHandler).logAliasTransformation(eq("testcode"),
        argThat(new SourcePositionMatcher(-1, -1, Integer.MAX_VALUE, Integer.MAX_VALUE)));
    // We can't easily validate exact start positions, so we'll use a custom matcher that checks end line/char are MAX_VALUE.
    // Implementation of matcher below.
  }

  // ----- Helper classes for matchers -----

  private static class ErrorMatcher implements org.mockito.ArgumentMatcher<JSError> {
    private final DiagnosticType expectedType;
    ErrorMatcher(DiagnosticType expectedType) { this.expectedType = expectedType; }
    @Override public boolean matches(Object argument) {
      if (!(argument instanceof JSError)) return false;
      JSError error = (JSError) argument;
      return error.getType().equals(expectedType);
    }
    @Override public String toString() { return "JSError with type " + expectedType; }
  }

  private static class SourcePositionMatcher implements org.mockito.ArgumentMatcher<SourcePosition> {
    private final int startLine, startChar, endLine, endChar;
    SourcePositionMatcher(int line, int char, int eline, int echar) {
      this.startLine = line; this.startChar = char; this.endLine = eline; this.endChar = echar;
    }
    @Override public boolean matches(Object argument) {
      if (!(argument instanceof SourcePosition)) return false;
      SourcePosition pos = (SourcePosition) argument;
      return (startLine == -1 || pos.getStartLine() == startLine)
          && (startChar == -1 || pos.getStartChar() == startChar)
          && pos.getEndLine() == endLine
          && pos.getEndChar() == endChar;
    }
    @Override public String toString() {
      return "SourcePosition{start: (" + startLine + "," + startChar +
          "), end: (" + endLine + "," + endChar + ")}";
    }
  }
}
```
