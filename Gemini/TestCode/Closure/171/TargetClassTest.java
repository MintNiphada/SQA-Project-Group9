package com.google.javascript.jscomp;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.SemanticReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class TypeInferenceTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Map<String, AssertionFunctionSpec> assertionsMap;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    assertionsMap = Maps.newHashMap();
  }

  private Node parseAndInfer(String js) {
    Node root = compiler.parseTestCode(js);
    Assert.assertEquals(0, compiler.getErrorCount());
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope topScope = scopeCreator.createScope(root, null);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), registry);
    TypeInference inference = new TypeInference(
        compiler, cfg, rai, topScope, assertionsMap);
    inference.analyze();
    return root;
  }

  private Node findFirstNode(Node root, int token) {
    if (root.getType() == token) {
      return root;
    }
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstNode(child, token);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @Test
  public void testGetBooleanOutcomes() {
    Assert.assertEquals(
        BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(
            BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, true));

    Assert.assertEquals(
        BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(
            BooleanLiteralSet.BOTH, BooleanLiteralSet.TRUE, true));

    Assert.assertEquals(
        BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(
            BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, false));

    Assert.assertEquals(
        BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(
            BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true));
  }

  @Test
  public void testInferVarAssignmentNumber() {
    Node root = parseAndInfer("var x = 1;");
    Node assignNode = findFirstNode(root, Token.NAME);
    Assert.assertNotNull(assignNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), assignNode.getJSType());
  }

  @Test
  public void testInferVarAssignmentString() {
    Node root = parseAndInfer("var x = 'hello';");
    Node nameNode = findFirstNode(root, Token.NAME);
    Assert.assertNotNull(nameNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), nameNode.getJSType());
  }

  @Test
  public void testInferVarAssignmentBoolean() {
    Node root = parseAndInfer("var x = true;");
    Node nameNode = findFirstNode(root, Token.NAME);
    Assert.assertNotNull(nameNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), nameNode.getJSType());
  }

  @Test
  public void testInferArrayLiteral() {
    Node root = parseAndInfer("var x = [1, 2, 3];");
    Node arrayNode = findFirstNode(root, Token.ARRAYLIT);
    Assert.assertNotNull(arrayNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), arrayNode.getJSType());
  }

  @Test
  public void testInferObjectLiteral() {
    Node root = parseAndInfer("var x = {a: 1, b: 'str'};");
    Node objNode = findFirstNode(root, Token.OBJECTLIT);
    Assert.assertNotNull(objNode);
    JSType objType = objNode.getJSType();
    Assert.assertNotNull(objType);
    Assert.assertTrue(objType.isObjectType());
  }

  @Test
  public void testInferArithmeticOperations() {
    Node root = parseAndInfer("var x = 1 + 2; var y = 1 - 2; var z = 1 * 2; var w = 1 / 2;");
    Node addNode = findFirstNode(root, Token.ADD);
    Assert.assertNotNull(addNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), addNode.getJSType());

    Node subNode = findFirstNode(root, Token.SUB);
    Assert.assertNotNull(subNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), subNode.getJSType());
  }

  @Test
  public void testInferStringAddition() {
    Node root = parseAndInfer("var x = 'foo' + 5;");
    Node addNode = findFirstNode(root, Token.ADD);
    Assert.assertNotNull(addNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), addNode.getJSType());
  }

  @Test
  public void testInferUnaryOperators() {
    Node root = parseAndInfer("var a = +1; var b = -1; var c = ~1;");
    Node posNode = findFirstNode(root, Token.POS);
    Assert.assertNotNull(posNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), posNode.getJSType());

    Node negNode = findFirstNode(root, Token.NEG);
    Assert.assertNotNull(negNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), negNode.getJSType());

    Node notNode = findFirstNode(root, Token.BITNOT);
    Assert.assertNotNull(notNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), notNode.getJSType());
  }

  @Test
  public void testInferComparisonOperators() {
    Node root = parseAndInfer("var a = (1 < 2); var b = (1 === 2); var c = (1 != 2);");
    Node ltNode = findFirstNode(root, Token.LT);
    Assert.assertNotNull(ltNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), ltNode.getJSType());

    Node sheqNode = findFirstNode(root, Token.SHEQ);
    Assert.assertNotNull(sheqNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), sheqNode.getJSType());

    Node neNode = findFirstNode(root, Token.NE);
    Assert.assertNotNull(neNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), neNode.getJSType());
  }

  @Test
  public void testInferTypeof() {
    Node root = parseAndInfer("var a = typeof 123;");
    Node typeofNode = findFirstNode(root, Token.TYPEOF);
    Assert.assertNotNull(typeofNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), typeofNode.getJSType());
  }

  @Test
  public void testInferHook() {
    Node root = parseAndInfer("var x = true ? 1 : 2;");
    Node hookNode = findFirstNode(root, Token.HOOK);
    Assert.assertNotNull(hookNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), hookNode.getJSType());
  }

  @Test
  public void testInferHookMixed() {
    Node root = parseAndInfer("var x = true ? 1 : 'foo';");
    Node hookNode = findFirstNode(root, Token.HOOK);
    Assert.assertNotNull(hookNode);
    JSType expectedUnion = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    Assert.assertEquals(expectedUnion, hookNode.getJSType());
  }

  @Test
  public void testInferLogicalAndOr() {
    Node root = parseAndInfer("var a = 1 && 2; var b = 'a' || 'b';");
    Node andNode = findFirstNode(root, Token.AND);
    Assert.assertNotNull(andNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), andNode.getJSType());

    Node orNode = findFirstNode(root, Token.OR);
    Assert.assertNotNull(orNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), orNode.getJSType());
  }

  @Test
  public void testInferIfBranches() {
    Node root = parseAndInfer("var x = null; if (true) { x = 1; } else { x = 2; }");
    Assert.assertNotNull(root);
  }

  @Test
  public void testInferForIn() {
    Node root = parseAndInfer("var obj = {a: 1}; for (var k in obj) { var val = k; }");
    Assert.assertNotNull(root);
  }

  @Test
  public void testInferCatch() {
    Node root = parseAndInfer("try { throw 1; } catch (e) { var x = e; }");
    Node catchNode = findFirstNode(root, Token.CATCH);
    Assert.assertNotNull(catchNode);
    Node eNode = catchNode.getFirstChild();
    Assert.assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), eNode.getJSType());
  }

  @Test
  public void testInferFunctionReturn() {
    Node root = parseAndInfer("function f() { return 1; } var r = f();");
    Assert.assertNotNull(root);
  }

  @Test
  public void testInferNew() {
    Node root = parseAndInfer("function Foo() {} var f = new Foo();");
    Node newNode = findFirstNode(root, Token.NEW);
    Assert.assertNotNull(newNode);
    Assert.assertNotNull(newNode.getJSType());
  }

  @Test
  public void testInferGetPropAndGetElem() {
    Node root = parseAndInfer("var obj = {prop: 10}; var p = obj.prop; var elem = obj['prop'];");
    Node getPropNode = findFirstNode(root, Token.GETPROP);
    Assert.assertNotNull(getPropNode);
    Assert.assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), getPropNode.getJSType());

    Node getElemNode = findFirstNode(root, Token.GETELEM);
    Assert.assertNotNull(getElemNode);
  }

  @Test
  public void testBitwiseAssignOps() {
    Node root = parseAndInfer("var x = 1; x += 2; x -= 1; x <<= 1; x >>= 1; x >>>= 1; x &= 1; x ^= 1; x |= 1;");
    Assert.assertNotNull(root);
  }

  @Test
  public void testDiagnosticConstant() {
    Assert.assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    Assert.assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS", TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }
}
