package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeTraversalTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  @Test
  public void testTraverseBasic() {
    String js = "var x = 10; function foo(y) { return y + x; } foo(5);";
    Node root = compiler.parseSyntheticCode("test.js", js);

    final List<Integer> visitedTypes = new ArrayList<Integer>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visitedTypes.add(n.getType());
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(root);

    Assert.assertFalse(visitedTypes.isEmpty());
    Assert.assertTrue(visitedTypes.contains(Token.FUNCTION));
    Assert.assertTrue(visitedTypes.contains(Token.VAR));
    Assert.assertEquals(compiler, t.getCompiler());
  }

  @Test
  public void testStaticTraverse() {
    String js = "var a = 1;";
    Node root = compiler.parseSyntheticCode("test.js", js);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    Assert.assertFalse(visited.isEmpty());
  }

  @Test
  public void testStaticTraverseRootsVarargs() {
    Node script1 = compiler.parseSyntheticCode("test1.js", "var a = 1;");
    Node script2 = compiler.parseSyntheticCode("test2.js", "var b = 2;");
    Node parent = new Node(Token.BLOCK, script1, script2);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverseRoots(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parentNode) {
        visited.add(n);
      }
    }, script1, script2);

    Assert.assertTrue(visited.contains(script1));
    Assert.assertTrue(visited.contains(script2));
  }

  @Test
  public void testStaticTraverseRootsList() {
    Node script1 = compiler.parseSyntheticCode("test1.js", "var a = 1;");
    Node script2 = compiler.parseSyntheticCode("test2.js", "var b = 2;");
    Node parent = new Node(Token.BLOCK, script1, script2);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverseRoots(compiler, Lists.newArrayList(script1, script2),
        new NodeTraversal.AbstractPostOrderCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parentNode) {
            visited.add(n);
          }
        });

    Assert.assertTrue(visited.contains(script1));
    Assert.assertTrue(visited.contains(script2));
  }

  @Test
  public void testTraverseRootsEmpty() {
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
    t.traverseRoots(Collections.<Node>emptyList());
    Assert.assertNull(t.getCurrentNode());
  }

  @Test
  public void testScopedCallback() {
    String js = "var g = 1; function f(a) { var b = 2; function inner() { return a + b; } }";
    Node root = compiler.parseSyntheticCode("scoped.js", js);

    final List<String> events = new ArrayList<String>();
    NodeTraversal.ScopedCallback scb = new NodeTraversal.ScopedCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}

      @Override
      public void enterScope(NodeTraversal t) {
        events.add("enter:" + t.getScopeDepth());
      }

      @Override
      public void exitScope(NodeTraversal t) {
        events.add("exit:" + t.getScopeDepth());
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, scb);
    t.traverse(root);

    Assert.assertTrue(events.contains("enter:1"));
    Assert.assertTrue(events.contains("enter:2"));
    Assert.assertTrue(events.contains("enter:3"));
    Assert.assertTrue(events.contains("exit:3"));
    Assert.assertTrue(events.contains("exit:2"));
    Assert.assertTrue(events.contains("exit:1"));
  }

  @Test
  public void testAbstractScopedCallbackDefaults() {
    Node root = compiler.parseSyntheticCode("test.js", "var x = 1;");
    final boolean[] visited = new boolean[1];
    NodeTraversal.AbstractScopedCallback cb = new NodeTraversal.AbstractScopedCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited[0] = true;
      }
    };
    NodeTraversal.traverse(compiler, root, cb);
    Assert.assertTrue(visited[0]);
  }

  @Test
  public void testAbstractShallowCallback() {
    String js = "var x = 1; function f(a, b) { var inside = 2; }";
    Node root = compiler.parseSyntheticCode("shallow.js", js);

    final List<String> visitedNames = new ArrayList<String>();
    NodeTraversal.AbstractShallowCallback cb = new NodeTraversal.AbstractShallowCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isName()) {
          visitedNames.add(n.getString());
        }
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    Assert.assertTrue(visitedNames.contains("x"));
    Assert.assertTrue(visitedNames.contains("f"));
    Assert.assertFalse(visitedNames.contains("inside"));
    Assert.assertFalse(visitedNames.contains("a"));
    Assert.assertFalse(visitedNames.contains("b"));
  }

  @Test
  public void testAbstractShallowStatementCallback() {
    String js = "var x = 1; if (x) { while (x > 0) { x--; } }";
    Node root = compiler.parseSyntheticCode("stmt.js", js);

    final List<Integer> visitedTypes = new ArrayList<Integer>();
    NodeTraversal.AbstractShallowStatementCallback cb = new NodeTraversal.AbstractShallowStatementCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visitedTypes.add(n.getType());
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    Assert.assertTrue(visitedTypes.contains(Token.IF));
    Assert.assertTrue(visitedTypes.contains(Token.WHILE));
    Assert.assertTrue(visitedTypes.contains(Token.BLOCK));
  }

  @Test
  public void testAbstractNodeTypePruningCallback() {
    String js = "var x = 1; var y = 2; function f() {}";
    Node root = compiler.parseSyntheticCode("prune.js", js);

    Set<Integer> types = new HashSet<Integer>();
    types.add(Token.VAR);
    types.add(Token.SCRIPT);

    final List<Integer> visitedTypes = new ArrayList<Integer>();
    NodeTraversal.AbstractNodeTypePruningCallback cb =
        new NodeTraversal.AbstractNodeTypePruningCallback(types) {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visitedTypes.add(n.getType());
          }
        };

    NodeTraversal.traverse(compiler, root, cb);
    Assert.assertTrue(visitedTypes.contains(Token.VAR));
    Assert.assertFalse(visitedTypes.contains(Token.FUNCTION));

    // Test exclude constructor
    final List<Integer> visitedExcludeTypes = new ArrayList<Integer>();
    Set<Integer> excludedTypes = Sets.newHashSet(Token.FUNCTION);
    NodeTraversal.AbstractNodeTypePruningCallback cbExclude =
        new NodeTraversal.AbstractNodeTypePruningCallback(excludedTypes, false) {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            visitedExcludeTypes.add(n.getType());
          }
        };
    NodeTraversal.traverse(compiler, root, cbExclude);
    Assert.assertFalse(visitedExcludeTypes.contains(Token.FUNCTION));
  }

  @Test
  public void testGetScopeAndCFG() {
    String js = "var x = 10; function foo() { var y = 20; return y; }";
    Node root = compiler.parseSyntheticCode("scope_test.js", js);

    final List<Scope> scopesEncountered = new ArrayList<Scope>();
    final List<ControlFlowGraph<Node>> cfgsEncountered = new ArrayList<ControlFlowGraph<Node>>();
    final List<Boolean> inGlobalScopeList = new ArrayList<Boolean>();

    NodeTraversal.ScopedCallback cb = new NodeTraversal.ScopedCallback() {
      @Override
      public boolean shouldTraverse(NodeTraversal nodeTraversal, Node n, Node parent) {
        return true;
      }

      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}

      @Override
      public void enterScope(NodeTraversal t) {
        Assert.assertTrue(t.hasScope());
        scopesEncountered.add(t.getScope());
        cfgsEncountered.add(t.getControlFlowGraph());
        inGlobalScopeList.add(t.inGlobalScope());
        Assert.assertNotNull(t.getScopeRoot());
      }

      @Override
      public void exitScope(NodeTraversal t) {}
    };

    NodeTraversal.traverse(compiler, root, cb);
    Assert.assertEquals(2, scopesEncountered.size());
    Assert.assertEquals(2, cfgsEncountered.size());
    Assert.assertEquals(Boolean.TRUE, inGlobalScopeList.get(0));
    Assert.assertEquals(Boolean.FALSE, inGlobalScopeList.get(1));
  }

  @Test
  public void testGetEnclosingFunction() {
    String js = "function outer() { function inner() { var a = 1; } }";
    Node root = compiler.parseSyntheticCode("enclosing.js", js);

    final List<Node> enclosingFunctions = new ArrayList<Node>();
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isVar()) {
          enclosingFunctions.add(t.getEnclosingFunction());
        }
      }
    };

    NodeTraversal.traverse(compiler, root, cb);
    Assert.assertEquals(1, enclosingFunctions.size());
    Assert.assertNotNull(enclosingFunctions.get(0));
    Assert.assertTrue(enclosingFunctions.get(0).isFunction());
  }

  @Test
  public void testTraverseAtScopeFunction() {
    String js = "function target(p1) { return p1; }";
    Node root = compiler.parseSyntheticCode("target.js", js);

    final Scope[] functionScope = new Scope[1];
    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isFunction()) {
          // forces scope creation
          t.getScope();
        }
      }
    };

    NodeTraversal t = new NodeTraversal(compiler, cb);
    t.traverse(root);

    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    Node fnNode = root.getFirstChild();
    Scope fnScope = creator.createScope(fnNode, globalScope);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t2 = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    t2.traverseAtScope(fnScope);
    Assert.assertFalse(visited.isEmpty());
  }

  @Test
  public void testTraverseInnerNode() {
    String js = "function f() { var x = 1; }";
    Node root = compiler.parseSyntheticCode("inner.js", js);

    final List<Node> visitedInner = new ArrayList<Node>();
    final NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });

    t.traverse(root);

    Node fnNode = root.getFirstChild();
    Node bodyNode = fnNode.getLastChild();

    NodeTraversal t2 = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visitedInner.add(n);
      }
    });

    t2.traverse(root);
    t2.traverseInnerNode(bodyNode, fnNode, null);
    Assert.assertTrue(visitedInner.contains(bodyNode));
  }

  @Test
  public void testTraverseInnerNodeWithRefinedScope() {
    String js = "var a = 1; function f() { var b = 2; }";
    Node root = compiler.parseSyntheticCode("refined.js", js);
    SyntacticScopeCreator creator = new SyntacticScopeCreator(compiler);
    Scope globalScope = creator.createScope(root, null);
    Node fnNode = root.getFirstChild().getNext();
    Scope fnScope = creator.createScope(fnNode, globalScope);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal t = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    t.traverseWithScope(root, globalScope);
    t.traverseInnerNode(fnNode.getLastChild(), fnNode, fnScope);
    Assert.assertTrue(visited.contains(fnNode.getLastChild()));
  }

  @Test
  public void testFunctionExpressionTraversal() {
    String js = "var f = function myNamedExpr(x) { return x; };";
    Node root = compiler.parseSyntheticCode("fnexpr.js", js);

    final List<Node> visited = new ArrayList<Node>();
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        visited.add(n);
      }
    });

    Assert.assertFalse(visited.isEmpty());
  }

  @Test
  public void testGetLineNumberAndSourceName() {
    String js = "var a = 1;\nvar b = 2;";
    Node root = compiler.parseSyntheticCode("test_lines.js", js);

    final List<Integer> lines = new ArrayList<Integer>();
    final List<String> sourceNames = new ArrayList<String>();

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        lines.add(t.getLineNumber());
        sourceNames.add(t.getSourceName());
        Assert.assertEquals(n, t.getCurrentNode());
      }
    });

    Assert.assertFalse(lines.isEmpty());
    Assert.assertTrue(sourceNames.contains("test_lines.js"));
  }

  @Test
  public void testGetInputAndModule() {
    String js = "var a = 1;";
    Node root = compiler.parseSyntheticCode("mod.js", js);

    final CompilerInput[] retrievedInput = new CompilerInput[1];
    final JSModule[] retrievedModule = new JSModule[1];

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (retrievedInput[0] == null) {
          retrievedInput[0] = t.getInput();
          retrievedModule[0] = t.getModule();
        }
      }
    });

    Assert.assertNotNull(retrievedInput[0]);
    Assert.assertEquals("mod.js", retrievedInput[0].getName());
  }

  @Test
  public void testMakeErrorAndReport() {
    String js = "var errorNode = 1;";
    Node root = compiler.parseSyntheticCode("err.js", js);

    final DiagnosticType diagType = DiagnosticType.error("TEST_CODE", "Test message {0}");
    final JSError[] generatedError = new JSError[2];

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isVar()) {
          generatedError[0] = t.makeError(n, CheckLevel.WARNING, diagType, "arg1");
          generatedError[1] = t.makeError(n, diagType, "arg2");
          t.report(n, diagType, "argReport");
        }
      }
    });

    Assert.assertNotNull(generatedError[0]);
    Assert.assertEquals(diagType, generatedError[0].getType());
    Assert.assertNotNull(generatedError[1]);
    Assert.assertEquals(diagType, generatedError[1].getType());
    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test(expected = RuntimeException.class)
  public void testUnexpectedExceptionHandling() {
    String js = "var x = 1;";
    Node root = compiler.parseSyntheticCode("err_handle.js", js);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        throw new NullPointerException("Simulated internal error");
      }
    });
  }
}