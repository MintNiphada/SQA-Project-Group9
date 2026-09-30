package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class ReferenceCollectingCallbackTest {

  private Node parse(Compiler compiler, String js) {
    Node root = compiler.parseTestCode(js);
    return root;
  }

  private ReferenceCollectingCallback process(Compiler compiler, String js, ReferenceCollectingCallback.Behavior behavior) {
    Node root = parse(compiler, js);
    Node externs = compiler.parseTestCode("");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(externs, root);
    return callback;
  }

  private ReferenceCollectingCallback processWithFilter(Compiler compiler, String js, ReferenceCollectingCallback.Behavior behavior, Predicate<Scope.Var> filter) {
    Node root = parse(compiler, js);
    Node externs = compiler.parseTestCode("");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior, filter);
    callback.process(externs, root);
    return callback;
  }

  @Test
  public void testDoNothingBehavior() {
    Compiler compiler = new Compiler();
    ReferenceCollectingCallback callback = process(compiler, "var x = 1; x++;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    assertNotNull(callback);
    assertNotNull(callback.getAllSymbols());
  }

  @Test
  public void testGetAllSymbolsAndGetScope() {
    Compiler compiler = new Compiler();
    final List<Scope.Var> vars = new ArrayList<Scope.Var>();
    ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceCollectingCallback.ReferenceMap referenceMap) {
      }
    };
    ReferenceCollectingCallback callback = process(compiler, "var x = 1; var y = 2;", behavior);
    for (Scope.Var v : callback.getAllSymbols()) {
      vars.add(v);
      assertNotNull(callback.getScope(v));
      assertNotNull(callback.getReferences(v));
    }
    assertTrue(vars.size() >= 2);
  }

  @Test
  public void testVarFilter() {
    Compiler compiler = new Compiler();
    Predicate<Scope.Var> filter = new Predicate<Scope.Var>() {
      @Override
      public boolean apply(Scope.Var var) {
        return "x".equals(var.getName());
      }
    };
    ReferenceCollectingCallback callback = processWithFilter(compiler, "var x = 1; var y = 2; x; y;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filter);

    boolean hasX = false;
    boolean hasY = false;
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("x".equals(v.getName())) {
        hasX = true;
      }
      if ("y".equals(v.getName())) {
        hasY = true;
      }
    }
    assertTrue(hasX);
    assertFalse(hasY);
  }

  @Test
  public void testHotSwapScript() {
    Compiler compiler = new Compiler();
    Node scriptRoot = parse(compiler, "var z = 10; z = z + 1;");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.hotSwapScript(scriptRoot, null);

    boolean foundZ = false;
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("z".equals(v.getName())) {
        foundZ = true;
        ReferenceCollectingCallback.ReferenceCollection collection = callback.getReferences(v);
        assertEquals(3, collection.references.size());
      }
    }
    assertTrue(foundZ);
  }

  @Test
  public void testArgumentsReference() {
    Compiler compiler = new Compiler();
    ReferenceCollectingCallback callback = process(compiler, "function f() { return arguments.length; }", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    boolean foundArguments = false;
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("arguments".equals(v.getName())) {
        foundArguments = true;
      }
    }
    assertTrue(foundArguments);
  }

  @Test
  public void testReferenceCollectionIsWellDefined() {
    Compiler compiler = new Compiler();
    ReferenceCollectingCallback callback = process(compiler, "var x = 1; var y = x + 1;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);

    for (Scope.Var v : callback.getAllSymbols()) {
      if ("x".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertTrue(refs.isWellDefined());
        assertTrue(refs.firstReferenceIsAssigningDeclaration());
      }
    }

    // Uninitialized var then read: not well-defined
    callback = process(compiler, "var a; var b = a;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertFalse(refs.isWellDefined());
        assertFalse(refs.firstReferenceIsAssigningDeclaration());
      }
    }

    // Uninitialized var followed immediately by assignment: well-defined
    callback = process(compiler, "var a; a = 1; var b = a;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertTrue(refs.isWellDefined());
        assertFalse(refs.firstReferenceIsAssigningDeclaration());
      }
    }

    // Empty collection check
    ReferenceCollectingCallback.ReferenceCollection emptyRefs = new ReferenceCollectingCallback.ReferenceCollection();
    assertFalse(emptyRefs.isWellDefined());
    assertFalse(emptyRefs.firstReferenceIsAssigningDeclaration());
  }

  @Test
  public void testIsEscaped() {
    Compiler compiler = new Compiler();
    ReferenceCollectingCallback callback = process(compiler,
        "function outer() { var x = 1; function inner() { return x; } return inner; }",
        ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);

    for (Scope.Var v : callback.getAllSymbols()) {
      if ("x".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertTrue(refs.isEscaped());
      }
    }

    ReferenceCollectingCallback callback2 = process(compiler,
        "function localOnly() { var x = 1; return x + 2; }",
        ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);

    for (Scope.Var v : callback2.getAllSymbols()) {
      if ("x".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback2.getReferences(v);
        assertFalse(refs.isEscaped());
      }
    }
  }

  @Test
  public void testIsAssignedOnceInLifetime() {
    Compiler compiler = new Compiler();
    // Assigned once in global
    ReferenceCollectingCallback callback = process(compiler, "var a = 1; a + 1;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertTrue(refs.isAssignedOnceInLifetime());
        assertFalse(refs.isNeverAssigned());
      }
    }

    // Assigned multiple times
    callback = process(compiler, "var a = 1; a = 2;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertFalse(refs.isAssignedOnceInLifetime());
        assertFalse(refs.isNeverAssigned());
      }
    }

    // Assigned in loop (while, for, do)
    callback = process(compiler, "var a; while(true) { a = 1; }", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertFalse(refs.isAssignedOnceInLifetime());
      }
    }

    callback = process(compiler, "var a; for(var i=0; i<10; i++) { a = 1; }", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertFalse(refs.isAssignedOnceInLifetime());
      }
    }

    callback = process(compiler, "var a; do { a = 1; } while(false);", ReferenceCollectingCallback.DO_NOTHING_BEValues(callback, "a"));
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertFalse(refs.isAssignedOnceInLifetime());
      }
    }

    // Assigned in a function inside a loop vs not in loop
    callback = process(compiler, "while(true) { function f() { var a = 1; } }", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertTrue(refs.isAssignedOnceInLifetime());
      }
    }

    // Never assigned
    callback = process(compiler, "var a; a;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertFalse(refs.isAssignedOnceInLifetime());
        assertTrue(refs.isNeverAssigned());
      }
    }
  }

  private ReferenceCollectingCallback.Behavior ReferenceCollectingCallback_DO_NOTHING_BEValues(ReferenceCollectingCallback callback, String a) {
    return ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR;
  }

  @Test
  public void testGetInitializingReferenceForConstants() {
    Compiler compiler = new Compiler();
    ReferenceCollectingCallback callback = process(compiler, "function f() { return A; } var A = 10;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);

    for (Scope.Var v : callback.getAllSymbols()) {
      if ("A".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        ReferenceCollectingCallback.Reference init = refs.getInitializingReferenceForConstants();
        assertNotNull(init);
        assertTrue(init.isInitializingDeclaration());
      }
    }

    callback = process(compiler, "function f() { return B; } var B; B = 20;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("B".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        ReferenceCollectingCallback.Reference init = refs.getInitializingReferenceForConstants();
        assertNotNull(init);
        assertTrue(init.isSimpleAssignmentToName());
      }
    }

    callback = process(compiler, "var C;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("C".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertNull(refs.getInitializingReferenceForConstants());
      }
    }
  }

  @Test
  public void testReferenceProperties() {
    Compiler compiler = new Compiler();
    ReferenceCollectingCallback callback = process(compiler,
        "function foo(param) {\n" +
        "  var a = 1;\n" +
        "  var b;\n" +
        "  b = 2;\n" +
        "  a++;\n" +
        "  --b;\n" +
        "  for (var key in {}) {}\n" +
        "  for (key in {}) {}\n" +
        "  try {} catch (e) {}\n" +
        "}", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);

    for (Scope.Var v : callback.getAllSymbols()) {
      ReferenceCollectingCallback.ReferenceCollection collection = callback.getReferences(v);
      Iterator<ReferenceCollectingCallback.Reference> it = collection.iterator();
      while (it.hasNext()) {
        ReferenceCollectingCallback.Reference ref = it.next();
        assertNotNull(ref.getNode());
        assertNotNull(ref.getParent());
        assertNotNull(ref.getInputId());
        assertEquals(v, ref.getSymbol());
        assertNotNull(ref.getScope());
        assertNotNull(ref.getBasicBlock());

        if ("foo".equals(v.getName())) {
          assertTrue(ref.isDeclaration());
          assertTrue(ref.isHoistedFunction());
          assertNotNull(ref.getAssignedValue());
        } else if ("param".equals(v.getName())) {
          assertTrue(ref.isDeclaration());
          assertTrue(ref.isInitializingDeclaration());
        } else if ("e".equals(v.getName())) {
          assertTrue(ref.isDeclaration());
          assertTrue(ref.isInitializingDeclaration());
        }
      }
    }
  }

  @Test
  public void testControlStructuresBasicBlocks() {
    Compiler compiler = new Compiler();
    String code =
        "var x = 0;\n" +
        "if (x > 0) { x = 1; } else { x = 2; }\n" +
        "x > 0 ? (x = 3) : (x = 4);\n" +
        "x && (x = 5);\n" +
        "x || (x = 6);\n" +
        "switch (x) { case 1: x = 7; break; default: x = 8; }\n" +
        "try { x = 9; } catch (e) { x = 10; } finally { x = 11; }\n" +
        "with ({}) { x = 12; }\n";

    ReferenceCollectingCallback callback = process(compiler, code, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("x".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        assertTrue(refs.references.size() > 5);
      }
    }
  }

  @Test
  public void testBasicBlockProvablyExecutesBefore() {
    Node script1 = new Node(Token.SCRIPT);
    ReferenceCollectingCallback.BasicBlock global1 = new ReferenceCollectingCallback.BasicBlock(null, script1);
    assertTrue(global1.isGlobalScopeBlock());
    assertNull(global1.getParent());

    Node script2 = new Node(Token.SCRIPT);
    ReferenceCollectingCallback.BasicBlock global2 = new ReferenceCollectingCallback.BasicBlock(null, script2);
    assertTrue(global1.provablyExecutesBefore(global2));

    Node funcNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "hoistedFunc"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node scriptWithFunc = new Node(Token.SCRIPT, funcNode);

    ReferenceCollectingCallback.BasicBlock blockHoisted = new ReferenceCollectingCallback.BasicBlock(global1, funcNode);
    assertFalse(global1.provablyExecutesBefore(blockHoisted));

    Node childBlockNode = new Node(Token.BLOCK);
    ReferenceCollectingCallback.BasicBlock childBlock = new ReferenceCollectingCallback.BasicBlock(global1, childBlockNode);
    assertTrue(global1.provablyExecutesBefore(childBlock));

    ReferenceCollectingCallback.BasicBlock grandchildBlock = new ReferenceCollectingCallback.BasicBlock(childBlock, new Node(Token.BLOCK));
    assertTrue(global1.provablyExecutesBefore(grandchildBlock));
    assertTrue(childBlock.provablyExecutesBefore(grandchildBlock));
    assertFalse(grandchildBlock.provablyExecutesBefore(childBlock));
  }

  @Test
  public void testReferenceCloneWithNewScope() {
    Compiler compiler = new Compiler();
    Node root = parse(compiler, "var a = 1; function f() { var b = 2; }");
    Node externs = compiler.parseTestCode("");
    final List<ReferenceCollectingCallback.Reference> collectedRefs = new ArrayList<ReferenceCollectingCallback.Reference>();
    ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceCollectingCallback.ReferenceMap referenceMap) {
      }
    };
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(externs, root);

    for (Scope.Var v : callback.getAllSymbols()) {
      for (ReferenceCollectingCallback.Reference ref : callback.getReferences(v)) {
        collectedRefs.add(ref);
        ReferenceCollectingCallback.Reference cloned = ref.cloneWithNewScope(ref.getScope());
        assertEquals(ref.getNode(), cloned.getNode());
        assertEquals(ref.getScope(), cloned.getScope());
        assertEquals(ref.getBasicBlock(), cloned.getBasicBlock());
        assertEquals(ref.getInputId(), cloned.getInputId());
        assertEquals(ref.getSourceFile(), cloned.getSourceFile());
      }
    }
    assertFalse(collectedRefs.isEmpty());
  }

  @Test
  public void testCreateRefForTest() {
    CompilerInput input = new CompilerInput(null, new InputId("test.js"), false);
    ReferenceCollectingCallback.Reference ref = ReferenceCollectingCallback.Reference.createRefForTest(input);
    assertNotNull(ref);
    assertEquals(new InputId("test.js"), ref.getInputId());
    assertNotNull(ref.getNode());
    assertEquals(Token.NAME, ref.getNode().getType());
  }

  @Test
  public void testBleedingFunctionReference() {
    Compiler compiler = new Compiler();
    final List<ReferenceCollectingCallback.Reference> bleedingRefs = new ArrayList<ReferenceCollectingCallback.Reference>();
    ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceCollectingCallback.ReferenceMap referenceMap) {
        if (!t.getScope().isGlobal()) {
          Node root = t.getScopeRoot();
          if (root.isFunction()) {
            ReferenceCollectingCallback.Reference ref = ReferenceCollectingCallback.Reference.newBleedingFunction(
                t, new ReferenceCollectingCallback.BasicBlock(null, root), root);
            bleedingRefs.add(ref);
          }
        }
      }
    };
    process(compiler, "var x = function myName() { return 1; };", behavior);
    assertFalse(bleedingRefs.isEmpty());
    assertEquals("myName", bleedingRefs.get(0).getNode().getString());
  }

  @Test
  public void testReferenceMapBehaviorCallback() {
    Compiler compiler = new Compiler();
    final int[] scopesExited = new int[]{0};
    ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceCollectingCallback.ReferenceMap referenceMap) {
        scopesExited[0]++;
        if (t.getScope().isGlobal()) {
          Scope.Var gVar = t.getScope().getVar("globalVar");
          assertNotNull(gVar);
          assertNotNull(referenceMap.getReferences(gVar));
        } else {
          Scope.Var lVar = t.getScope().getVar("localVar");
          if (lVar != null) {
            assertNotNull(referenceMap.getReferences(lVar));
          }
        }
      }
    };
    process(compiler, "var globalVar = 1; function f() { var localVar = 2; }", behavior);
    assertTrue(scopesExited[0] >= 2);
  }

  @Test
  public void testLvalueVariations() {
    Compiler compiler = new Compiler();
    String code =
        "var x = 1;\n" +
        "x += 2;\n" +
        "x *= 3;\n" +
        "x = 4;\n" +
        "x++;\n" +
        "--x;\n" +
        "var obj = { a: 1 };\n" +
        "for (x in obj) {}\n";

    ReferenceCollectingCallback callback = process(compiler, code, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("x".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        int lvalueCount = 0;
        for (ReferenceCollectingCallback.Reference ref : refs) {
          if (ref.isLvalue()) {
            lvalueCount++;
          }
        }
        assertTrue(lvalueCount >= 6);
      }
    }
  }

  @Test
  public void testGrandparentAndParentMethods() {
    Compiler compiler = new Compiler();
    ReferenceCollectingCallback callback = process(compiler, "var a = 1;", ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    for (Scope.Var v : callback.getAllSymbols()) {
      if ("a".equals(v.getName())) {
        ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(v);
        ReferenceCollectingCallback.Reference ref = refs.references.get(0);
        assertNotNull(ref.getParent());
        assertNotNull(ref.getGrandparent());
        assertEquals(Token.VAR, ref.getParent().getType());
      }
    }
  }
}