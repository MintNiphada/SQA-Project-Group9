package com.google.javascript.jscomp;

import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.NodeTraversal.AbstractPostOrderCallback;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

class MethodCompilerPassTest {

  private Compiler compiler;
  private TestSignatureStore signatureStore;
  private TestMethodCompilerPass pass;

  static class TestSignatureStore implements MethodCompilerPass.SignatureStore {
    final Map<String, List<Node>> signatures = new HashMap<String, List<Node>>();
    final List<String> sourceFiles = new ArrayList<String>();
    int resetCount = 0;

    @Override
    public void reset() {
      resetCount++;
      signatures.clear();
      sourceFiles.clear();
    }

    @Override
    public void addSignature(String functionName, Node functionNode, String sourceFile) {
      if (!signatures.containsKey(functionName)) {
        signatures.put(functionName, new ArrayList<Node>());
      }
      signatures.get(functionName).add(functionNode);
      sourceFiles.add(sourceFile);
    }

    @Override
    public void removeSignature(String functionName) {
      signatures.remove(functionName);
    }
  }

  static class TestMethodCompilerPass extends MethodCompilerPass {
    final TestSignatureStore store;
    final Callback actingCallback;
    int actingCallbackVisitCount = 0;

    TestMethodCompilerPass(Compiler compiler, TestSignatureStore store) {
      super(compiler);
      this.store = store;
      this.actingCallback = new AbstractPostOrderCallback() {
        @Override
        public void visit(NodeTraversal t, Node n, Node parent) {
          actingCallbackVisitCount++;
        }
      };
    }

    TestMethodCompilerPass(Compiler compiler, TestSignatureStore store, Callback callback) {
      super(compiler);
      this.store = store;
      this.actingCallback = callback;
    }

    @Override
    Callback getActingCallback() {
      return actingCallback;
    }

    @Override
    SignatureStore getSignatureStore() {
      return store;
    }
  }

  @Before
  public void setUp() {
    compiler = new Compiler();
    signatureStore = new TestSignatureStore();
    pass = new TestMethodCompilerPass(compiler, signatureStore);
  }

  private Node createFunctionNode(String name) {
    Node fn = new Node(Token.FUNCTION);
    Node fnName = Node.newString(Token.NAME, name);
    Node params = new Node(Token.LP);
    Node body = new Node(Token.BLOCK);
    fn.addChildToBack(fnName);
    fn.addChildToBack(params);
    fn.addChildToBack(body);
    return fn;
  }

  @Test
  public void testProcessWithNullExterns() {
    Node root = new Node(Token.BLOCK);
    pass.process(null, root);

    assertEquals(1, signatureStore.resetCount);
    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.externMethodsWithoutSignatures.isEmpty());
    assertTrue(pass.methodDefinitions.isEmpty());
    assertTrue(pass.nonMethodProperties.isEmpty());
  }

  @Test
  public void testExternsGetPropFunctionSignature() {
    Node externs = new Node(Token.BLOCK);
    Node target = new Node(Token.GETPROP, Node.newString(Token.NAME, "window"), Node.newString("setTimeout"));
    Node fn = createFunctionNode("setTimeout");
    Node assign = new Node(Token.ASSIGN, target, fn);
    externs.addChildToBack(assign);

    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("setTimeout"));
    assertFalse(pass.externMethodsWithoutSignatures.contains("setTimeout"));
    assertTrue(signatureStore.signatures.containsKey("setTimeout"));
    assertTrue(pass.methodDefinitions.containsKey("setTimeout"));
    assertEquals(fn, pass.methodDefinitions.get("setTimeout").iterator().next());
  }

  @Test
  public void testExternsGetPropNonFunctionSignature() {
    Node externs = new Node(Token.BLOCK);
    Node target = new Node(Token.GETPROP, Node.newString(Token.NAME, "window"), Node.newString("location"));
    Node value = Node.newString("http://localhost");
    Node assign = new Node(Token.ASSIGN, target, value);
    externs.addChildToBack(assign);

    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("location"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("location"));
    assertFalse(signatureStore.signatures.containsKey("location"));
    assertFalse(pass.methodDefinitions.containsKey("location"));
  }

  @Test
  public void testExternsGetElemNonStringTargetIgnored() {
    Node externs = new Node(Token.BLOCK);
    Node target = new Node(Token.GETELEM, Node.newString(Token.NAME, "window"), Node.newNumber(0));
    Node fn = createFunctionNode("fn");
    Node assign = new Node(Token.ASSIGN, target, fn);
    externs.addChildToBack(assign);

    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);

    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.externMethodsWithoutSignatures.isEmpty());
  }

  @Test
  public void testExternsObjectLitSignatures() {
    Node externs = new Node(Token.BLOCK);
    Node objLit = new Node(Token.OBJECTLIT);
    Node fn = createFunctionNode("alert");
    objLit.addChildToBack(Node.newString("alert"));
    objLit.addChildToBack(fn);
    objLit.addChildToBack(Node.newString("version"));
    objLit.addChildToBack(Node.newNumber(1.0));
    externs.addChildToBack(objLit);

    Node root = new Node(Token.BLOCK);
    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("alert"));
    assertTrue(pass.externMethods.contains("version"));
    assertFalse(pass.externMethodsWithoutSignatures.contains("alert"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("version"));
    assertTrue(signatureStore.signatures.containsKey("alert"));
    assertFalse(signatureStore.signatures.containsKey("version"));
  }

  @Test
  public void testSourceStaticMethodAssignFunction() {
    Node root = new Node(Token.BLOCK);
    Node target = new Node(Token.GETPROP, Node.newString(Token.NAME, "MyClass"), Node.newString("myMethod"));
    Node fn = createFunctionNode("myMethod");
    Node assign = new Node(Token.ASSIGN, target, fn);
    root.addChildToBack(assign);

    pass.process(null, root);

    assertTrue(signatureStore.signatures.containsKey("myMethod"));
    assertTrue(pass.methodDefinitions.containsKey("myMethod"));
    assertEquals(fn, pass.methodDefinitions.get("myMethod").iterator().next());
    assertFalse(pass.nonMethodProperties.contains("myMethod"));
  }

  @Test
  public void testSourceStaticMethodAssignNonFunctionAddsToNonMethodProperties() {
    Node root = new Node(Token.BLOCK);
    Node target = new Node(Token.GETPROP, Node.newString(Token.NAME, "MyClass"), Node.newString("myProp"));
    Node num = Node.newNumber(42);
    Node assign = new Node(Token.ASSIGN, target, num);
    root.addChildToBack(assign);

    pass.process(null, root);

    assertFalse(signatureStore.signatures.containsKey("myProp"));
    assertTrue(pass.nonMethodProperties.contains("myProp"));
  }

  @Test
  public void testSourceObjectLitSignaturesAndProperties() {
    Node root = new Node(Token.BLOCK);
    Node objLit = new Node(Token.OBJECTLIT);
    Node fn = createFunctionNode("render");
    objLit.addChildToBack(Node.newString("render"));
    objLit.addChildToBack(fn);
    objLit.addChildToBack(Node.newString("count"));
    objLit.addChildToBack(Node.newNumber(10));
    root.addChildToBack(objLit);

    pass.process(null, root);

    assertTrue(signatureStore.signatures.containsKey("render"));
    assertTrue(pass.methodDefinitions.containsKey("render"));
    assertTrue(pass.nonMethodProperties.contains("count"));
  }

  @Test
  public void testSourcePrototypeParentAssign() {
    Node root = new Node(Token.BLOCK);

    Node proto = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("prototype"));
    Node protoMethod = new Node(Token.GETPROP, proto, Node.newString("bar"));

    Node intermediate = new Node(Token.EXPR_RESULT, protoMethod);
    Node fn = createFunctionNode("bar");
    Node assign = new Node(Token.ASSIGN, intermediate, fn);
    root.addChildToBack(assign);

    pass.process(null, root);

    assertTrue(signatureStore.signatures.containsKey("bar"));
    assertTrue(pass.methodDefinitions.containsKey("bar"));
  }

  @Test
  public void testSourceMethodExcludedIfExternWithoutSignature() {
    Node externs = new Node(Token.BLOCK);
    Node extTarget = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("doAction"));
    externs.addChildToBack(extTarget);

    Node root = new Node(Token.BLOCK);
    Node srcTarget = new Node(Token.GETPROP, Node.newString(Token.NAME, "Foo"), Node.newString("doAction"));
    Node fn = createFunctionNode("doAction");
    Node assign = new Node(Token.ASSIGN, srcTarget, fn);
    root.addChildToBack(assign);

    pass.process(externs, root);

    assertTrue(pass.externMethods.contains("doAction"));
    assertTrue(pass.externMethodsWithoutSignatures.contains("doAction"));
    assertFalse(signatureStore.signatures.containsKey("doAction"));
    assertFalse(pass.methodDefinitions.containsKey("doAction"));
  }

  @Test(expected = IllegalStateException.class)
  public void testAddPossibleSignatureThrowsWhenUndefinedFunctionName() {
    Node root = new Node(Token.BLOCK);
    Node target = new Node(Token.GETPROP, Node.newString(Token.NAME, "MyClass"), Node.newString("action"));
    Node nameNode = Node.newString(Token.NAME, "undefinedFunction");
    Node assign = new Node(Token.ASSIGN, target, nameNode);
    root.addChildToBack(assign);

    pass.process(null, root);
  }

  @Test
  public void testResetClearsPreviousState() {
    Node root1 = new Node(Token.BLOCK);
    Node target1 = new Node(Token.GETPROP, Node.newString(Token.NAME, "A"), Node.newString("m1"));
    Node fn1 = createFunctionNode("m1");
    root1.addChildToBack(new Node(Token.ASSIGN, target1, fn1));

    pass.process(null, root1);
    assertEquals(1, pass.methodDefinitions.size());

    Node root2 = new Node(Token.BLOCK);
    pass.process(null, root2);
    assertEquals(2, signatureStore.resetCount);
    assertEquals(0, pass.methodDefinitions.size());
    assertTrue(pass.externMethods.isEmpty());
    assertTrue(pass.externMethodsWithoutSignatures.isEmpty());
  }

  @Test
  public void testActingCallbackIsInvoked() {
    Node root = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    pass.process(null, root);

    assertTrue(pass.actingCallbackVisitCount > 0);
  }
}