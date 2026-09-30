package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class PeepholeOptimizationsPassTest {

  private static class DummyOptimization extends AbstractPeepholeOptimization {
    private int beginCount = 0;
    private int endCount = 0;
    private int optimizeCount = 0;
    private boolean returnNull = false;
    private boolean reportChange = false;
    private boolean replaceNode = false;

    @Override
    void beginTraversal(AbstractCompiler compiler) {
      super.beginTraversal(compiler);
      beginCount++;
    }

    @Override
    void endTraversal(AbstractCompiler compiler) {
      super.endTraversal(compiler);
      endCount++;
    }

    @Override
    Node optimizeSubtree(Node subtree) {
      optimizeCount++;
      if (returnNull) {
        return null;
      }
      if (reportChange) {
        reportCodeChange();
      }
      if (replaceNode) {
        replaceNode = false;
        Node newNode = new Node(Token.EMPTY);
        if (subtree.getParent() != null) {
          subtree.getParent().replaceChild(subtree, newNode);
        }
        return newNode;
      }
      return subtree;
    }
  }

  @Test
  public void testGetCompiler() {
    Compiler compiler = new Compiler();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler);
    Assert.assertSame(compiler, pass.getCompiler());
  }

  @Test
  public void testProcessEmpty() {
    Compiler compiler = new Compiler();
    DummyOptimization opt = new DummyOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);

    pass.process(externs, root);

    Assert.assertEquals(1, opt.beginCount);
    Assert.assertEquals(1, opt.endCount);
    Assert.assertTrue(opt.optimizeCount > 0);
  }

  @Test
  public void testProcessWithOptimizationReturningNull() {
    Compiler compiler = new Compiler();
    DummyOptimization opt = new DummyOptimization();
    opt.returnNull = true;

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR);
    root.addChildToBack(varNode);

    pass.process(externs, root);

    Assert.assertEquals(1, opt.beginCount);
    Assert.assertEquals(1, opt.endCount);
    Assert.assertTrue(opt.optimizeCount > 0);
  }

  @Test
  public void testProcessWithNodeReplacement() {
    Compiler compiler = new Compiler();
    DummyOptimization opt = new DummyOptimization();
    opt.replaceNode = true;

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR);
    root.addChildToBack(varNode);

    pass.process(externs, root);

    Assert.assertEquals(1, opt.beginCount);
    Assert.assertEquals(1, opt.endCount);
  }

  @Test
  public void testRetraverseOnScriptChange() {
    Compiler compiler = new Compiler();
    final int[] optimizeCalls = {0};

    AbstractPeepholeOptimization opt = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        optimizeCalls[0]++;
        if (subtree.isScript() && optimizeCalls[0] == 1) {
          reportCodeChange();
        }
        return subtree;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);

    pass.process(externs, root);

    Assert.assertTrue(optimizeCalls[0] >= 2);
  }

  @Test
  public void testRetraverseOnFunctionChange() {
    Compiler compiler = new Compiler();
    final int[] functionVisits = {0};

    AbstractPeepholeOptimization opt = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        if (subtree.isFunction()) {
          functionVisits[0]++;
          if (functionVisits[0] == 1) {
            reportCodeChange();
          }
        }
        return subtree;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    root.addChildToBack(fn);

    pass.process(externs, root);

    Assert.assertEquals(2, functionVisits[0]);
  }

  @Test
  public void testNestedFunctionsAndStackDepth() {
    Compiler compiler = new Compiler();
    final int[] functionVisits = {0};

    AbstractPeepholeOptimization opt = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        if (subtree.isFunction()) {
          functionVisits[0]++;
        }
        return subtree;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);

    Node outerFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "outer"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node innerFn1 = new Node(Token.FUNCTION, Node.newString(Token.NAME, "inner1"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node innerFn2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, "inner2"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));

    outerFn.getLastChild().addChildToBack(innerFn1);
    root.addChildToBack(outerFn);
    root.addChildToBack(innerFn2);

    pass.process(externs, root);

    Assert.assertEquals(3, functionVisits[0]);

    // Process again to exercise state reuse in StateStack
    pass.process(externs, root);
    Assert.assertEquals(6, functionVisits[0]);
  }

  @Test
  public void testRetraverseDoesNotTraverseChildScopes() {
    Compiler compiler = new Compiler();
    final int[] innerVisits = {0};
    final int[] outerVisits = {0};

    AbstractPeepholeOptimization opt = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        if (subtree.isFunction()) {
          String name = subtree.getFirstChild().getString();
          if ("outer".equals(name)) {
            outerVisits[0]++;
            if (outerVisits[0] == 1) {
              reportCodeChange();
            }
          } else if ("inner".equals(name)) {
            innerVisits[0]++;
          }
        }
        return subtree;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);

    Node outerFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "outer"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node innerFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "inner"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    outerFn.getLastChild().addChildToBack(innerFn);
    root.addChildToBack(outerFn);

    pass.process(externs, root);

    Assert.assertEquals(2, outerVisits[0]);
    // Inner function should only be visited during the initial traversal, not during retraversal of outer
    Assert.assertEquals(1, innerVisits[0]);
  }

  @Test
  public void testMultipleOptimizationsInVisit() {
    Compiler compiler = new Compiler();

    final int[] opt1Calls = {0};
    final int[] opt2Calls = {0};

    AbstractPeepholeOptimization opt1 = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        opt1Calls[0]++;
        return subtree;
      }
    };

    AbstractPeepholeOptimization opt2 = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        opt2Calls[0]++;
        return subtree;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt1, opt2);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);
    root.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(42)));

    pass.process(externs, root);

    Assert.assertTrue(opt1Calls[0] > 0);
    Assert.assertTrue(opt2Calls[0] > 0);
    Assert.assertEquals(opt1Calls[0], opt2Calls[0]);
  }

  @Test
  public void testMaxIterationExceededThrowsException() {
    Compiler compiler = new Compiler();

    AbstractPeepholeOptimization opt = new AbstractPeepholeOptimization() {
      @Override
      Node optimizeSubtree(Node subtree) {
        if (subtree.isScript()) {
          reportCodeChange();
        }
        return subtree;
      }
    };

    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node externs = new Node(Token.SCRIPT);
    Node root = new Node(Token.SCRIPT);

    try {
      pass.process(externs, root);
      Assert.fail("Expected IllegalStateException due to too many iterations");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("too many interations"));
    }
  }

  @Test
  public void testDirectVisitMethod() {
    Compiler compiler = new Compiler();
    DummyOptimization opt = new DummyOptimization();
    PeepholeOptimizationsPass pass = new PeepholeOptimizationsPass(compiler, opt);

    Node node = new Node(Token.SCRIPT);
    pass.visit(node);

    Assert.assertEquals(1, opt.optimizeCount);
  }
}