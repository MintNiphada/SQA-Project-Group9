package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

class ReferenceCollectingCallbackTest {

  @Test
  public void testDoNothingBehavior() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1;");
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.process(null, root);
    ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR.afterExitScope(null, new HashMap<Var, ReferenceCollection>());
  }

  @Test
  public void testBasicBlockHierarchyAndProvablyExecutesBefore() {
    Node rootNode = new Node(Token.BLOCK);
    BasicBlock rootBlock = new BasicBlock(null, rootNode);
    Assert.assertNull(rootBlock.getParent());
    Assert.assertTrue(rootBlock.provablyExecutesBefore(rootBlock));

    Node childNode = new Node(Token.BLOCK);
    BasicBlock childBlock = new BasicBlock(rootBlock, childNode);
    Assert.assertEquals(rootBlock, childBlock.getParent());
    Assert.assertTrue(rootBlock.provablyExecutesBefore(childBlock));
    Assert.assertFalse(childBlock.provablyExecutesBefore(rootBlock));

    Node grandchildNode = new Node(Token.BLOCK);
    BasicBlock grandchildBlock = new BasicBlock(childBlock, grandchildNode);
    Assert.assertTrue(rootBlock.provablyExecutesBefore(grandchildBlock));
    Assert.assertTrue(childBlock.provablyExecutesBefore(grandchildBlock));
    Assert.assertFalse(grandchildBlock.provablyExecutesBefore(childBlock));

    // Hoisted function block
    Node funcName = Node.newString(Token.NAME, "hoistedFn");
    Node hoistedFunc = new Node(Token.FUNCTION, funcName, new Node(Token.LP), new Node(Token.BLOCK));
    Node blockParent = new Node(Token.BLOCK, hoistedFunc);
    BasicBlock hoistedBlock = new BasicBlock(rootBlock, hoistedFunc);

    // rootBlock provablyExecutesBefore hoistedBlock should be false because hoistedBlock is hoisted
    Assert.assertFalse(rootBlock.provablyExecutesBefore(hoistedBlock));

    BasicBlock unrelatedBlock = new BasicBlock(null, new Node(Token.BLOCK));
    Assert.assertFalse(rootBlock.provablyExecutesBefore(unrelatedBlock));
    Assert.assertFalse(unrelatedBlock.provablyExecutesBefore(rootBlock));
  }

  @Test
  public void testProcessWithCompiler() {
    Compiler compiler = new Compiler();
    String js = "var x = 1; function foo(y) { var z = x + y; z++; z = 5; return z; } foo(2);";
    Node root = compiler.parseTestCode(js);

    final Map<String, ReferenceCollection> found = new HashMap<String, ReferenceCollection>();

    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        for (Map.Entry<Var, ReferenceCollection> entry : referenceMap.entrySet()) {
          found.put(entry.getKey().getName(), entry.getValue());
        }
      }
    };

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(null, root);

    Assert.assertTrue(found.containsKey("x"));
    Assert.assertTrue(found.containsKey("foo"));
    Assert.assertTrue(found.containsKey("y"));
    Assert.assertTrue(found.containsKey("z"));

    ReferenceCollection xRefs = found.get("x");
    Assert.assertNotNull(xRefs);
    Assert.assertTrue(xRefs.isWellDefined());
    Assert.assertTrue(xRefs.isEscaped());
    Assert.assertTrue(xRefs.isAssignedOnceInLifetime());
    Assert.assertFalse(xRefs.isNeverAssigned());
    Assert.assertTrue(xRefs.firstReferenceIsAssigningDeclaration());

    ReferenceCollection zRefs = found.get("z");
    Assert.assertNotNull(zRefs);
    Assert.assertFalse(zRefs.isAssignedOnceInLifetime());
    Assert.assertFalse(zRefs.isNeverAssigned());
  }

  @Test
  public void testVarFilter() {
    Compiler compiler = new Compiler();
    String js = "var keep = 1; var ignore = 2;";
    Node root = compiler.parseTestCode(js);

    Predicate<Var> filter = new Predicate<Var>() {
      @Override
      public boolean apply(Var input) {
        return input != null && "keep".equals(input.getName());
      }
    };

    final Map<String, ReferenceCollection> found = new HashMap<String, ReferenceCollection>();

    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        for (Map.Entry<Var, ReferenceCollection> entry : referenceMap.entrySet()) {
          found.put(entry.getKey().getName(), entry.getValue());
        }
      }
    };

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior, filter);
    callback.process(null, root);

    Assert.assertTrue(found.containsKey("keep"));
    Assert.assertFalse(found.containsKey("ignore"));
  }

  @Test
  public void testBlockBoundaries() {
    Compiler compiler = new Compiler();
    String js = ""
        + "var a = 0;\n"
        + "if (a) { a = 1; } else { a = 2; }\n"
        + "while (a < 5) { a++; }\n"
        + "do { a++; } while (a < 10);\n"
        + "for (var i = 0; i < 5; i++) { a += i; }\n"
        + "for (var k in [1, 2]) { a += k; }\n"
        + "try { a = 3; } catch (e) { a = 4; } finally { a = 5; }\n"
        + "switch (a) { case 1: a = 2; break; default: a = 3; }\n"
        + "var b = (a > 0) ? (a && 1) : (a || 2);\n"
        + "with ({}) { a = 6; }\n";

    Node root = compiler.parseTestCode(js);
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.process(null, root);

    NodeTraversal t = new NodeTraversal(compiler, callback);
    t.traverse(root);
  }

  @Test
  public void testReferenceCollectionMethods() {
    ReferenceCollection col = new ReferenceCollection();
    Assert.assertFalse(col.isWellDefined());
    Assert.assertFalse(col.isEscaped());
    Assert.assertNull(col.getInitializingReference());
    Assert.assertNull(col.getInitializingReferenceForConstants());
    Assert.assertFalse(col.isAssignedOnceInLifetime());
    Assert.assertTrue(col.isNeverAssigned());
    Assert.assertFalse(col.firstReferenceIsAssigningDeclaration());

    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("var a; a = 1; a = 2;");
    final Map<Var, ReferenceCollection> map = new HashMap<Var, ReferenceCollection>();

    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        map.putAll(referenceMap);
      }
    };

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(null, script);

    Var aVar = null;
    for (Var v : map.keySet()) {
      if ("a".equals(v.getName())) {
        aVar = v;
        break;
      }
    }
    Assert.assertNotNull(aVar);
    ReferenceCollection aCol = callback.getReferenceCollection(aVar);
    Assert.assertNotNull(aCol);

    Assert.assertFalse(aCol.firstReferenceIsAssigningDeclaration());
    Assert.assertNotNull(aCol.getInitializingReference());
    Assert.assertEquals(aCol.references.get(1), aCol.getInitializingReference());
    Assert.assertNotNull(aCol.getInitializingReferenceForConstants());
    Assert.assertFalse(aCol.isAssignedOnceInLifetime());
    Assert.assertFalse(aCol.isNeverAssigned());
  }

  @Test
  public void testReferenceGetAssignedValueAndLvalue() {
    Compiler compiler = new Compiler();
    String js = ""
        + "function f(p) {\n"
        + "  var x = 10;\n"
        + "  x += 5;\n"
        + "  x--;\n"
        + "  for (x in [1, 2]) {}\n"
        + "  for (var y in [1, 2]) {}\n"
        + "}\n";
    Node root = compiler.parseTestCode(js);

    final Map<Var, ReferenceCollection> map = new HashMap<Var, ReferenceCollection>();
    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        map.putAll(referenceMap);
      }
    };

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(null, root);

    for (Map.Entry<Var, ReferenceCollection> entry : map.entrySet()) {
      Var v = entry.getKey();
      ReferenceCollection rc = entry.getValue();
      if ("f".equals(v.getName())) {
        Reference ref = rc.references.get(0);
        Assert.assertTrue(ref.isDeclaration());
        Assert.assertTrue(ref.isInitializingDeclaration());
        Assert.assertNotNull(ref.getAssignedValue());
        Assert.assertEquals(Token.FUNCTION, ref.getAssignedValue().getType());
        Assert.assertNotNull(ref.getSourceName());
        Assert.assertNotNull(ref.getParent());
        Assert.assertNotNull(ref.getNameNode());
      } else if ("p".equals(v.getName())) {
        Reference ref = rc.references.get(0);
        Assert.assertTrue(ref.isDeclaration());
        Assert.assertFalse(ref.isVarDeclaration());
        Assert.assertTrue(ref.isInitializingDeclaration());
      } else if ("x".equals(v.getName())) {
        for (Reference ref : rc.references) {
          Assert.assertNotNull(ref.getBasicBlock());
          if (ref.getParent().getType() == Token.VAR) {
            Assert.assertTrue(ref.isVarDeclaration());
            Assert.assertTrue(ref.isLvalue());
            Assert.assertNotNull(ref.getAssignedValue());
          }
        }
      }
    }
  }

  @Test
  public void testBleedingFunctionReference() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var g = function namedFn() { namedFn(); };");

    final Map<Var, ReferenceCollection> map = new HashMap<Var, ReferenceCollection>();
    Behavior behavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        map.putAll(referenceMap);
      }
    };

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, behavior);
    callback.process(null, root);

    Node funcNode = null;
    for (Var v : map.keySet()) {
      if ("namedFn".equals(v.getName())) {
        funcNode = v.getParentNode();
      }
    }

    if (funcNode != null) {
      NodeTraversal t = new NodeTraversal(compiler, callback);
      BasicBlock block = new BasicBlock(null, funcNode);
      Reference bleedingRef = Reference.newBleedingFunction(t, block, funcNode);
      Assert.assertNotNull(bleedingRef);
      Assert.assertEquals(funcNode, bleedingRef.getParent());
      Assert.assertEquals(block, bleedingRef.getBasicBlock());
    }
  }

  @Test
  public void testReferenceGetGrandparent() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1;");
    final Map<Var, ReferenceCollection> map = new HashMap<Var, ReferenceCollection>();

    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        map.putAll(referenceMap);
      }
    });
    callback.process(null, root);

    for (ReferenceCollection col : map.values()) {
      for (Reference ref : col.references) {
        Assert.assertNotNull(ref.getGrandparent());
        Assert.assertNotNull(ref.getScope());
      }
    }
  }
}