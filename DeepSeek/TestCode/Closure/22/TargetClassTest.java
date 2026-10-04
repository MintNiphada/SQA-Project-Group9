package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CheckSideEffects;
import com.google.javascript.jscomp.DiagnosticType;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.CompilerInput;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.ArrayList;
import java.util.List;

public class CheckSideEffectsTest {

  private MockCompiler compiler;
  private Node externRoot;

  @Before
   public voidsetUp() {
    compiler = new MockCompiler();
    externRoot = compiler.getSynthesizedExternsInput().getAstRoot(null);
  }

  // --- Tests forvisit method via process traversal --- 

  @Test
  public void testEmptyNodeDoesNotReport() {
    CheckSideEffects pass = createPass(false);
    Node emptyNode = IR.empty(); // actually IR.empty returns a Node with Token.EMPTY
    Node expr = IR.exprResult(emptyNode);
    Node script = IR.script(expr);
    // process will traverse and visit emptyNode, should return immediately
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testCommaNodeDoesNotReport() {
    CheckSideEffects pass = createPass(false);
    Node comma = IR.comma(IR.name("a"), IR.name("b"));
    Node expr = IR.exprResult(comma);
    Node script = IR.script(expr);
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testRootNodeParentNullNoReport() {
    CheckSideEffects pass = createPass(false);
    // just a script node; its parent is null during traversal
    Node script = IR.script();
    // traversal will call visit with n=script, parent=null -> return
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testEvalSpecialCase() {
    CheckSideEffects pass = createPass(false);
    // (0, eval)()
    Node comma = newNode(Token.COMMA, IR.number(0)., IR.name("eval"));
    Node call = IR.call(comma); // call node with comma as target
    Node expr = IR.exprResult(call);
    Node script = IR.script(expr);
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testCommaLastChildInExprResultReports() {
    CheckSideEffects pass = createPass(false);
    // comma(a,b) in expr_result, b is last child, should not return early
    Node name = IR.name("x");
    Node comma = IR.comma(IR.name("y"), name);
    Node expr = IR.exprResult(comma);
    Node script = IR.script(expr);
    process(pass, script);
    assertEquals(1, compiler.getErrors().size());
  }

  @Test
  public void testCommaLastChildInBlockReports() {
    CheckSideEffects pass = createPass(false);
    Node name = IR.name("x");
    Node comma = IR.comma(name, IR.name("y")); // name is not last child? we need name last
    // adjust: make name last
    comma = IR.comma(IR.name("a"), name);
    Node block = IR.block(IR.exprResult(comma));
    Node script = IR.script(block);
    process(pass, script);
    assertEquals(1, compiler.getErrors().size());
  }

  @Test
  public void testCommaLastChildInForDoesNotReport() {
    CheckSideEffects pass = createPass(false);
    // for (a,b in ...) ? Actually we need a comma inside a for that is not itself an expr_result/block
    // We'll put comma as the init of a for: for(comma;;)
    Node name = IR.name("x");
    Node comma = IR.comma(IR.name("y"), name);
    Node forNode = new Node(Token.FOR, comma, IR.empty(), IR.empty(), IR.block());
    Node expr = IR.exprResult(forNode);
    Node script = IR.script(expr);
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testForInitDoesNotReturn() {
    CheckSideEffects pass = createPass(false);
    Node init = IR.name("i");
    Node forNode = new Node(Token.FOR, init, IR.empty(), IR.empty(), IR.block());
    Node script = IR.script(IR.exprResult(forNode));
    // This node (init) should be visited and not return early, report if appropriate
    // since it's a name, may have side effects? but we'll test that it doesn't get blocked
    process(pass, script);
    // We expect a report because init is a name inside an expr_result (within for)
    // but actually for node isn't expr_result, but for's children are traversed.
    // When visiting init, parent is for, not expr_result. The check for parent not expr/block
    // will match the for case and since init is firstChild, it will not return.
    // So it will proceed to report. So we expect one error.
    assertFalse(compiler.getErrors().isEmpty());
  }

  @Test
  public void testForConditionReturns() {
    CheckSideEffects pass = createPass(false);
    Node cond = IR.name("c");
    Node forNode = new Node(Token.FOR, IR.empty(), cond, IR.empty(), IR.block());
    Node script = IR.script(IR.exprResult(forNode));
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testParentNotExprNotBlockNotForNoReport() {
    CheckSideEffects pass = createPass(false);
    // e.g., a LABEL statement
    Node label = new Node(Token.LABEL, IR.labelName("l"), IR.name("x"));
    Node script = IR.script(IR.exprResult(label));
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testQualifiedNameWithJSDocNoReport() {
    CheckSideEffects pass = createPass(false);
    Node name = IR.name("ns.item");
    name.setJSDocInfo(new JSDocInfoBuilder(false).build(name));
    Node expr = IR.exprResult(name);
    Node script = IR.script(expr);
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testExprResultNodeNoReport() {
    CheckSideEffects pass = createPass(false);
    // n itself is an EXPR_RESULT (which shouldn't happen as child, but we can force)
    Node expr = IR.exprResult(); // an empty expression result? Its type is EXPR_RESULT
    Node block = IR.block(expr);
    Node script = IR.script(block);
    processAndAssertNoErrors(pass, script);
  }

  @Test
  public void testStringNodeReportsSpecialMessage() {
    CheckSideEffects pass = createPass(false);
    Node string = IR.string("hello");
    Node expr = IR.exprResult(string);
    Node script = IR.script(expr);
    process(pass, script);
    List<JSError> errors = compiler.getErrors();
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).description.contains("missing '+'"));
  }

  @Test
  public void testSimpleOperatorReportsMessage() {
    CheckSideEffects pass = createPass(false);
    Node add = IR.add(IR.number(1), IR.number(2));
    Node expr = IR.exprResult(add);
    Node script = IR.script(expr);
    process(pass, script);
    List<JSError> errors = compiler.getErrors();
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).description.contains("operator"));
  }

  @Test
  public void testGenericSideEffectFreeCodeReportsAndAddsToProblemNodes() {
    // Use protectSideEffectFreeCode = true to verify wrapping later
    CheckSideEffects pass = createPass(true);
    Node name = IR.name("x"); // side-effect free
    Node expr = IR.exprResult(name);
    Node script = IR.script(expr);
    process(pass, script);
    // After process, because protectSideEffectFreeCode is true and problemNodes not empty,
    // the name should be wrapped in a call to JSCOMPILER_PRESERVE.
    // Verify that the tree has been modified: the expr_result's child should now be a call.
    Node possiblyCall = expr.getFirstChild();
    assertTrue(possiblyCall.isCall());
    assertEquals(CheckSideEffects.PROTECTOR_FN, possiblyCall.getFirstChild().getString());
    assertEquals(name, possiblyCall.getLastChild()); // the wrapped node
  }

  // --- Tests for protectSideEffects directly using process --- 
  @Test
  public void testProtectSideEffectsWithEmptyProblemNodes() {
    CheckSideEffects pass = createPass(true);
    Node script = IR.script(); // no side-effect free nodes
    process(pass, script);
    // externRoot should not have any children (or the initial script).
    assertFalse(externRoot.hasChildren(); // originally empty script has children? externRoot is a script node, we didn't add any
  }

  // --- Test for StripProtection ---

  @Test
  public void testStripProtectionRemovesWrapper() {
    CheckSideEffects.StripProtection strip = new CheckSideEffects.StripProtection(compiler);
    // Build: JSCOMPILER_PRESERVE(name)
    Node name = IR.name("x");
    Node call = IR.call(IR.name(CheckSideEffects.PROTECTOR_FN), name);
    call.putBooleanProp(Node.FREE_CALL, true);
    Node expr = IR.exprResult(call);
    Node script = IR.script(expr);
    strip.process(null, script);
    // After stripping, the expr_result should directly contain the name
    Node newChild = expr.getFirstChild();
    assertEquals(name, newChild);
  }

  // --- Helper methods ---

  private CheckSideEffects createPass(bolean protect) {
    return new CheckideEffects(compiler, CheckLevel.WARNING, protect);
  }

  private void process(CheckSideEffects pass, Node root) {
    pass.process(null, root);
  }

  private void processAndAssertNoErrors(CheckSideEffects pass, Node root) {
    process(pass, root);
    assertTrue(compiler.getErrors().isEmpty());
  }

  // --- MockCompiler inner class ---

  private static class MockCompiler extends AbstractCompiler {
    private List<JSError> errors = new ArrayList<>();
    private Node externRoot = IR.script();

    MockCompiler() {
      super(new CompilerOptions());
    }

    @Override
    ErrorManager getErrorManager() { return new TestErrorManager(); }

    @Override
    public void report(JSError error) {
      errors.add(error);
    }

    @Override
    public void reportCodeChange() {
   }

    @Override
    CompilerInput getSynthesizedExternsInput() {
      return new CompilerInput(new SourceFile("externs")) {
        @Override public NodegetAstRoot(AbstractCompiler compiler) {
          return externRoot;
        }
      };
    }

    @Override
    public JsAst getAst() { return null; }

    @Override
    public List<CompilerInput> getInputsForTesting() { return null; }

    // dummy implementations for other abstract methods
    @Override
    public void init() {}
    @Override
    public void compile() {}
    @Override
    public void printConfig() {}
    @Override
    public GlobalVar getGlobalVar() { return null; }
    @Override
    public Node getRoot() { return null; }
    @Override
    public int getUniqueNameId() { return 0; }
    @Override
    public boolean hasRegExpGlobalReferences() { return false; }
    @Override
    public void setLoggingLevel(Level level) {}
    @Override
    public void setErrorManager(ErrorManager errorManager) {}
    @Override
    public void setPrintStream(PrintStream stream) {}
    @Override
    public void setErrorFormat(ErrorFormat errorFormat) {}
    // ... any other required overrides can be added similarly if needed

    List<JSError> getErrors() { return errors; }
  }

  private static class TestErrorManagerimplements ErrorManager {
    private List<JSError> errors = new ArrayList<>();

    @Override
    public void report(CheckLevel level, JSError error) {
      errors.add(error);
    }

    @Override
    public void generateReport() { }
    @Override
    public int getErrorCount() { return errors.size(); }
    @Override
    public int getWarningCount() { return0; }
    @Override
    public JSError[] getErrors() { return errors.toArray(new JSError[0]); }
    @Override
    public JSError[] getWarnings() { return new JSError[0]; }
    @Override
    public void setTypedPercent(double typedPercent) { }
    @Override
    public double getTypedPercent() { return 0.0; }
  }

  // Minimal import for SourceFile
  private static class TestSourceFile extends SourceFile {
    TestSourceFile(String name) {
      super(name);
    }
    @Override
    public String getCode() { return ""; }
    @Override
    public void clearCachedSource() {}
    @Override
    public CodeBuilder append(String code) { return null; }
  }
}
```
