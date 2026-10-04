package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.StaticSlot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TypeInference.class, NodeUtil.class})
public class TypeInferenceTest {

    @Mock
    private AbstractCompiler compiler;
    @Mock
    private JSTypeRegistry registry;
    @Mock
    private ReverseAbstractInterpreter reverseInterpreter;
    @Mock
    private Scope syntacticScope;
    @Mock
    private ControlFlowGraph<Node> cfg;
    @Mock
    private Map<String, AssertionFunctionSpec> assertionFunctionsMap;
    @Mock
    private FlowScope mockFlowScope;
    @Mock
    private FlowScope childFlowScope;
    @Mock
    private FlowScope optimizedFlowScope;
    @Mock
    private Node mockNode;
    @Mock
    private JSType mockJSType;
    @Mock
    private JSType mockStringType;
    @Mock
    private JSType mockNumberType;
    @Mock
    private JSType mockBooleanType;
    @Mock
    private JSType mockUnknownType;
    @Mock
    private JSType mockCheckedUnknownType;
    @Mock
    private JSType mockVoidType;
    @Mock
    private JSType mockNullType;
    @Mock
    private JSType mockArrayType;
    @Mock
    private JSType mockUnionType;
    @Mock
    private FunctionType mockFunctionType;
    @Mock
    private ObjectType mockObjectType;
    @Mock
    private Var mockVar;
    @Mock
    private JSDocInfo mockJSDocInfo;
    @Mock
    private CodingConvention codingConvention;
    @Mock
    private CodingConvention.Bind mockBind;
    @Mock
    private AssertionFunctionSpec mockAssertionSpec;
    @Mock
    private DiGraphEdge<Node, Branch> mockEdge;
    @Mock
    private StaticSlot<JSType> mockSlot;

    private TypeInference typeInference;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(NodeUtil.class);

        when(compiler.getTypeRegistry()).thenReturn(registry);
        when(compiler.getCodingConvention()).thenReturn(codingConvention);

        when(registry.getNativeType(JSTypeNative.VOID_TYPE)).thenReturn(mockVoidType);
        when(registry.getNativeType(JSTypeNative.STRING_TYPE)).thenReturn(mockStringType);
        when(registry.getNativeType(JSTypeNative.NUMBER_TYPE)).thenReturn(mockNumberType);
        when(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)).thenReturn(mockBooleanType);
        when(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE)).thenReturn(mockUnknownType);
        when(registry.getNativeType(JSTypeNative.CHECKED_UNKNOWN_TYPE)).thenReturn(mockCheckedUnknownType);
        when(registry.getNativeType(JSTypeNative.ARRAY_TYPE)).thenReturn(mockArrayType);
        when(registry.getNativeType(JSTypeNative.NULL_TYPE)).thenReturn(mockNullType);
        when(registry.getNativeType(JSTypeNative.NUMBER_VALUE_OR_OBJECT_TYPE)).thenReturn(mockNumberType);
        when(registry.getNativeType(JSTypeNative.BOOLEAN_OBJECT_TYPE)).thenReturn(mockBooleanType);

        when(mockUnknownType.isUnknownType()).thenReturn(true);
        when(mockStringType.isString()).thenReturn(true);
        when(mockNumberType.isSubtype(any(JSType.class))).thenReturn(true);
        when(mockBooleanType.isSubtype(any(JSType.class))).thenReturn(true);
        when(mockVoidType.isSubtype(any(JSType.class))).thenReturn(true);
        when(mockNullType.isSubtype(any(JSType.class))).thenReturn(true);

        when(syntacticScope.getRootNode()).thenReturn(mockNode);
        when(syntacticScope.getTypeOfThis()).thenReturn(mockJSType);
        when(syntacticScope.getDeclarativelyUnboundVarsWithoutTypes()).thenReturn(Collections.<Var>emptyIterator());

        when(mockFlowScope.createChildFlowScope()).thenReturn(childFlowScope);
        when(childFlowScope.createChildFlowScope()).thenReturn(childFlowScope);
        when(childFlowScope.optimize()).thenReturn(optimizedFlowScope);
        when(optimizedFlowScope.optimize()).thenReturn(optimizedFlowScope);

        when(mockNode.getJSType()).thenReturn(mockJSType);
        when(mockJSType.restrictByNotNullOrUndefined()).thenReturn(mockJSType);

        typeInference = new TypeInference(compiler, cfg, reverseInterpreter, syntacticScope, assertionFunctionsMap);
    }

    @Test
    public void testCreateInitialEstimateLattice() {
        FlowScope result = typeInference.createInitialEstimateLattice();
        assertNotNull(result);
    }

    @Test
    public void testCreateEntryLattice() {
        FlowScope result = typeInference.createEntryLattice();
        assertNotNull(result);
    }

    @Test
    public void testFlowThroughBottomScopeReturnsInput() {
        FlowScope bottom = Whitebox.getInternalState(typeInference, "bottomScope");
        FlowScope result = typeInference.flowThrough(mockNode, bottom);
        assertSame(bottom, result);
    }

    @Test
    public void testFlowThroughNonBottomScope() throws Exception {
        FlowScope input = mockFlowScope;
        when(input.createChildFlowScope()).thenReturn(childFlowScope);
        when(mockNode.getType()).thenReturn(Token.EXPR_RESULT);
        when(mockNode.getFirstChild()).thenReturn(mockNode);
        when(mockNode.isGetProp()).thenReturn(false);
        when(mockNode.isFunction()).thenReturn(false);
        when(mockNode.getJSDocInfo()).thenReturn(null);

        FlowScope result = typeInference.flowThrough(mockNode, input);
        assertNotNull(result);
        verify(input).createChildFlowScope();
    }

    @Test
    public void testBranchedFlowThroughOnTrueForIn() throws Exception {
        Node source = mock(Node.class);
        when(source.getType()).thenReturn(Token.FOR);
        when(cfg.getOutEdges(source)).thenReturn(Collections.singletonList(mockEdge));
        when(mockEdge.getValue()).thenReturn(Branch.ON_TRUE);
        PowerMockito.when(NodeUtil.isForIn(source)).thenReturn(true);

        Node item = mock(Node.class);
        Node obj = mock(Node.class);
        when(source.getFirstChild()).thenReturn(item);
        when(item.getNext()).thenReturn(obj);
        when(item.isVar()).thenReturn(false);
        when(item.isName()).thenReturn(true);
        when(item.getString()).thenReturn("x");
        when(obj.getJSType()).thenReturn(mockObjectType);
        when(mockObjectType.dereference()).thenReturn(mockObjectType);
        when(mockObjectType.getIndexType()).thenReturn(mockStringType);
        when(mockStringType.isUnknownType()).thenReturn(false);
        when(mockStringType.getGreatestSubtype(mockStringType)).thenReturn(mockStringType);
        when(mockStringType.isEmptyType()).thenReturn(false);

        FlowScope input = mockFlowScope;
        when(input.createChildFlowScope()).thenReturn(childFlowScope);
        when(childFlowScope.createChildFlowScope()).thenReturn(childFlowScope);

        List<FlowScope> result = typeInference.branchedFlowThrough(source, input);
        assertEquals(1, result.size());
        assertSame(optimizedFlowScope, result.get(0));
    }

    @Test
    public void testBranchedFlowThroughOnTrueWithCondition() throws Exception {
        Node source = mock(Node.class);
        when(source.getType()).thenReturn(Token.IF);
        when(cfg.getOutEdges(source)).thenReturn(Collections.singletonList(mockEdge));
        when(mockEdge.getValue()).thenReturn(Branch.ON_TRUE);
        PowerMockito.when(NodeUtil.isForIn(source)).thenReturn(false);
        PowerMockito.when(NodeUtil.getConditionExpression(source)).thenReturn(mockNode);
        when(mockNode.isAnd()).thenReturn(false);
        when(mockNode.isOr()).thenReturn(false);
        when(mockNode.getType()).thenReturn(Token.EQ);

        FlowScope input = mockFlowScope;
        when(input.createChildFlowScope()).thenReturn(childFlowScope);
        when(childFlowScope.createChildFlowScope()).thenReturn(childFlowScope);
        when(reverseInterpreter.getPreciserScopeKnowingConditionOutcome(eq(mockNode), any(FlowScope.class), eq(true)))
                .thenReturn(childFlowScope);

        List<FlowScope> result = typeInference.branchedFlowThrough(source, input);
        assertEquals(1, result.size());
        assertSame(optimizedFlowScope, result.get(0));
    }

    @Test
    public void testBranchedFlowThroughOnFalseWithCondition() throws Exception {
        Node source = mock(Node.class);
        when(source.getType()).thenReturn(Token.IF);
        when(cfg.getOutEdges(source)).thenReturn(Collections.singletonList(mockEdge));
        when(mockEdge.getValue()).thenReturn(Branch.ON_FALSE);
        PowerMockito.when(NodeUtil.isForIn(source)).thenReturn(false);
        PowerMockito.when(NodeUtil.getConditionExpression(source)).thenReturn(mockNode);
        when(mockNode.isAnd()).thenReturn(false);
        when(mockNode.isOr()).thenReturn(false);
        when(mockNode.getType()).thenReturn(Token.EQ);

        FlowScope input = mockFlowScope;
        when(input.createChildFlowScope()).thenReturn(childFlowScope);
        when(childFlowScope.createChildFlowScope()).thenReturn(childFlowScope);
        when(reverseInterpreter.getPreciserScopeKnowingConditionOutcome(eq(mockNode), any(FlowScope.class), eq(false)))
                .thenReturn(childFlowScope);

        List<FlowScope> result = typeInference.branchedFlowThrough(source, input);
        assertEquals(1, result.size());
        assertSame(optimizedFlowScope, result.get(0));
    }

    @Test
    public void testTraverseAssign() throws Exception {
        Node assignNode = mock(Node.class);
        when(assignNode.getType()).thenReturn(Token.ASSIGN);
        Node left = mock(Node.class);
        Node right = mock(Node.class);
        when(assignNode.getFirstChild()).thenReturn(left);
        when(assignNode.getLastChild()).thenReturn(right);
        when(left.getType()).thenReturn(Token.NAME);
        when(left.getString()).thenReturn("a");
        when(left.hasChildren()).thenReturn(false);
        when(left.getJSType()).thenReturn(mockJSType);
        when(right.getJSType()).thenReturn(mockStringType);
        when(mockStringType.restrictByNotNullOrUndefined()).thenReturn(mockStringType);

        when(syntacticScope.getVar("a")).thenReturn(mockVar);
        when(mockVar.isTypeInferred()).thenReturn(true);
        when(mockVar.getType()).thenReturn(mockJSType);
        when(mockJSType.getLeastSupertype(mockStringType)).thenReturn(mockStringType);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseAssign", assignNode, scope);
        assertNotNull(result);
        verify(left).setJSType(mockStringType);
        verify(assignNode).setJSType(mockStringType);
    }

    @Test
    public void testTraverseNameWithValue() throws Exception {
        Node nameNode = mock(Node.class);
        when(nameNode.getType()).thenReturn(Token.NAME);
        when(nameNode.getString()).thenReturn("x");
        Node value = mock(Node.class);
        when(nameNode.getFirstChild()).thenReturn(value);
        when(nameNode.getJSType()).thenReturn(null);
        when(value.getJSType()).thenReturn(mockStringType);
        when(mockStringType.restrictByNotNullOrUndefined()).thenReturn(mockStringType);

        when(syntacticScope.getVar("x")).thenReturn(mockVar);
        when(mockVar.isTypeInferred()).thenReturn(true);
        when(mockVar.getType()).thenReturn(null);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseName", nameNode, scope);
        assertNotNull(result);
        verify(nameNode).setJSType(mockStringType);
    }

    @Test
    public void testTraverseNameWithoutValue() throws Exception {
        Node nameNode = mock(Node.class);
        when(nameNode.getType()).thenReturn(Token.NAME);
        when(nameNode.getString()).thenReturn("x");
        when(nameNode.getFirstChild()).thenReturn(null);
        when(nameNode.getJSType()).thenReturn(null);

        when(mockFlowScope.getSlot("x")).thenReturn(mockSlot);
        when(mockSlot.isTypeInferred()).thenReturn(false);
        when(mockSlot.getType()).thenReturn(mockStringType);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseName", nameNode, mockFlowScope);
        assertNotNull(result);
        verify(nameNode).setJSType(mockStringType);
    }

    @Test
    public void testTraverseAddBothUnknown() throws Exception {
        Node addNode = mock(Node.class);
        when(addNode.getType()).thenReturn(Token.ADD);
        when(addNode.isAssignAdd()).thenReturn(false);
        Node left = mock(Node.class);
        Node right = mock(Node.class);
        when(addNode.getFirstChild()).thenReturn(left);
        when(left.getNext()).thenReturn(right);
        when(left.getJSType()).thenReturn(mockUnknownType);
        when(right.getJSType()).thenReturn(mockUnknownType);
        when(mockUnknownType.isUnknownType()).thenReturn(true);
        when(mockUnknownType.isString()).thenReturn(false);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseAdd", addNode, scope);
        assertNotNull(result);
        verify(addNode).setJSType(mockUnknownType);
    }

    @Test
    public void testTraverseAddOneString() throws Exception {
        Node addNode = mock(Node.class);
        when(addNode.getType()).thenReturn(Token.ADD);
        when(addNode.isAssignAdd()).thenReturn(false);
        Node left = mock(Node.class);
        Node right = mock(Node.class);
        when(addNode.getFirstChild()).thenReturn(left);
        when(left.getNext()).thenReturn(right);
        when(left.getJSType()).thenReturn(mockStringType);
        when(right.getJSType()).thenReturn(mockNumberType);
        when(mockStringType.isUnknownType()).thenReturn(false);
        when(mockStringType.isString()).thenReturn(true);
        when(mockNumberType.isUnknownType()).thenReturn(false);
        when(mockNumberType.isString()).thenReturn(false);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseAdd", addNode, scope);
        assertNotNull(result);
        verify(addNode).setJSType(mockStringType);
    }

    @Test
    public void testTraverseAddBothNumbers() throws Exception {
        Node addNode = mock(Node.class);
        when(addNode.getType()).thenReturn(Token.ADD);
        when(addNode.isAssignAdd()).thenReturn(false);
        Node left = mock(Node.class);
        Node right = mock(Node.class);
        when(addNode.getFirstChild()).thenReturn(left);
        when(left.getNext()).thenReturn(right);
        when(left.getJSType()).thenReturn(mockNumberType);
        when(right.getJSType()).thenReturn(mockNumberType);
        when(mockNumberType.isUnknownType()).thenReturn(false);
        when(mockNumberType.isString()).thenReturn(false);
        when(mockNumberType.isSubtype(any(JSType.class))).thenReturn(true);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseAdd", addNode, scope);
        assertNotNull(result);
        verify(addNode).setJSType(mockNumberType);
    }

    @Test
    public void testTraverseHook() throws Exception {
        Node hookNode = mock(Node.class);
        when(hookNode.getType()).thenReturn(Token.HOOK);
        Node condition = mock(Node.class);
        Node trueNode = mock(Node.class);
        Node falseNode = mock(Node.class);
        when(hookNode.getFirstChild()).thenReturn(condition);
        when(condition.getNext()).thenReturn(trueNode);
        when(hookNode.getLastChild()).thenReturn(falseNode);
        when(condition.getType()).thenReturn(Token.EQ);
        when(trueNode.getJSType()).thenReturn(mockStringType);
        when(falseNode.getJSType()).thenReturn(mockNumberType);
        when(mockStringType.getLeastSupertype(mockNumberType)).thenReturn(mockUnionType);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);
        when(reverseInterpreter.getPreciserScopeKnowingConditionOutcome(eq(condition), any(FlowScope.class), eq(true)))
                .thenReturn(childFlowScope);
        when(reverseInterpreter.getPreciserScopeKnowingConditionOutcome(eq(condition), any(FlowScope.class), eq(false)))
                .thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseHook", hookNode, scope);
        assertNotNull(result);
        verify(hookNode).setJSType(mockUnionType);
    }

    @Test
    public void testTraverseCallWithFunctionType() throws Exception {
        Node callNode = mock(Node.class);
        when(callNode.getType()).thenReturn(Token.CALL);
        Node left = mock(Node.class);
        when(callNode.getFirstChild()).thenReturn(left);
        when(left.getNext()).thenReturn(null);
        when(left.getJSType()).thenReturn(mockFunctionType);
        when(mockFunctionType.restrictByNotNullOrUndefined()).thenReturn(mockFunctionType);
        when(mockFunctionType.isFunctionType()).thenReturn(true);
        when(mockFunctionType.toMaybeFunctionType()).thenReturn(mockFunctionType);
        when(mockFunctionType.getReturnType()).thenReturn(mockStringType);
        when(mockFunctionType.getParameters()).thenReturn(Collections.<Node>emptyList());
        when(left.getQualifiedName()).thenReturn(null);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseCall", callNode, scope);
        assertNotNull(result);
        verify(callNode).setJSType(mockStringType);
    }

    @Test
    public void testTraverseNew() throws Exception {
        Node newNode = mock(Node.class);
        when(newNode.getType()).thenReturn(Token.NEW);
        Node constructor = mock(Node.class);
        when(newNode.getFirstChild()).thenReturn(constructor);
        when(constructor.getNext()).thenReturn(null);
        when(constructor.getJSType()).thenReturn(mockFunctionType);
        when(mockFunctionType.restrictByNotNullOrUndefined()).thenReturn(mockFunctionType);
        when(mockFunctionType.isUnknownType()).thenReturn(false);
        when(mockFunctionType.toMaybeFunctionType()).thenReturn(mockFunctionType);
        when(mockFunctionType.isConstructor()).thenReturn(true);
        when(mockFunctionType.getInstanceType()).thenReturn(mockObjectType);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseNew", newNode, scope);
        assertNotNull(result);
        verify(newNode).setJSType(mockObjectType);
    }

    @Test
    public void testTraverseObjectLiteral() throws Exception {
        Node objLitNode = mock(Node.class);
        when(objLitNode.getType()).thenReturn(Token.OBJECTLIT);
        when(objLitNode.getJSType()).thenReturn(mockObjectType);
        when(objLitNode.getFirstChild()).thenReturn(null);
        when(objLitNode.getJSDocInfo()).thenReturn(null);
        when(mockObjectType.cast(mockObjectType)).thenReturn(mockObjectType);
        when(mockObjectType.hasReferenceName()).thenReturn(true);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseObjectLiteral", objLitNode, scope);
        assertNotNull(result);
    }

    @Test
    public void testTraverseArrayLiteral() throws Exception {
        Node arrayNode = mock(Node.class);
        when(arrayNode.getType()).thenReturn(Token.ARRAYLIT);
        when(arrayNode.getFirstChild()).thenReturn(null);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseArrayLiteral", arrayNode, scope);
        assertNotNull(result);
        verify(arrayNode).setJSType(mockArrayType);
    }

    @Test
    public void testTraverseGetProp() throws Exception {
        Node getPropNode = mock(Node.class);
        when(getPropNode.getType()).thenReturn(Token.GETPROP);
        Node objNode = mock(Node.class);
        Node propNode = mock(Node.class);
        when(getPropNode.getFirstChild()).thenReturn(objNode);
        when(getPropNode.getLastChild()).thenReturn(propNode);
        when(propNode.getString()).thenReturn("prop");
        when(objNode.getJSType()).thenReturn(mockObjectType);
        when(mockObjectType.findPropertyType("prop")).thenReturn(mockStringType);
        when(getPropNode.getQualifiedName()).thenReturn("obj.prop");
        when(mockFlowScope.getSlot("obj.prop")).thenReturn(null);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseGetProp", getPropNode, scope);
        assertNotNull(result);
        verify(getPropNode).setJSType(mockStringType);
    }

    @Test
    public void testTraverseGetElem() throws Exception {
        Node getElemNode = mock(Node.class);
        when(getElemNode.getType()).thenReturn(Token.GETELEM);
        Node objNode = mock(Node.class);
        Node indexNode = mock(Node.class);
        when(getElemNode.getFirstChild()).thenReturn(objNode);
        when(objNode.getNext()).thenReturn(indexNode);
        when(indexNode.getNext()).thenReturn(null);
        when(objNode.getJSType()).thenReturn(mockObjectType);
        when(mockObjectType.restrictByNotNullOrUndefined()).thenReturn(mockObjectType);
        when(mockObjectType.cast(mockObjectType)).thenReturn(mockObjectType);
        when(mockObjectType.getParameterType()).thenReturn(mockStringType);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseGetElem", getElemNode, scope);
        assertNotNull(result);
        verify(getElemNode).setJSType(mockStringType);
    }

    @Test
    public void testTraverseReturn() throws Exception {
        Node returnNode = mock(Node.class);
        when(returnNode.getType()).thenReturn(Token.RETURN);
        Node retValue = mock(Node.class);
        when(returnNode.getFirstChild()).thenReturn(retValue);
        when(retValue.getNext()).thenReturn(null);
        when(retValue.getJSType()).thenReturn(mockStringType);
        when(mockNode.getJSType()).thenReturn(mockFunctionType);
        when(mockFunctionType.toMaybeFunctionType()).thenReturn(mockFunctionType);
        when(mockFunctionType.getReturnType()).thenReturn(mockNumberType);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseReturn", returnNode, scope);
        assertNotNull(result);
    }

    @Test
    public void testTraverseCatch() throws Exception {
        Node catchNode = mock(Node.class);
        when(catchNode.getType()).thenReturn(Token.CATCH);
        Node nameNode = mock(Node.class);
        when(catchNode.getFirstChild()).thenReturn(nameNode);
        when(nameNode.getNext()).thenReturn(null);
        when(nameNode.isName()).thenReturn(true);
        when(nameNode.getString()).thenReturn("e");

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "traverseCatch", catchNode, scope);
        assertNotNull(result);
        verify(nameNode).setJSType(mockUnknownType);
    }

    @Test
    public void testUpdateScopeForTypeChangeName() throws Exception {
        Node left = mock(Node.class);
        when(left.getType()).thenReturn(Token.NAME);
        when(left.getString()).thenReturn("x");
        when(left.hasChildren()).thenReturn(false);
        when(left.getJSType()).thenReturn(mockJSType);
        JSType resultType = mockStringType;
        when(mockStringType.restrictByNotNullOrUndefined()).thenReturn(mockStringType);

        when(syntacticScope.getVar("x")).thenReturn(mockVar);
        when(mockVar.isTypeInferred()).thenReturn(true);
        when(mockVar.getType()).thenReturn(null);

        FlowScope scope = mockFlowScope;
        Whitebox.invokeMethod(typeInference, "updateScopeForTypeChange", scope, left, mockJSType, resultType);
        verify(left).setJSType(resultType);
        verify(scope).inferSlotType("x", resultType);
    }

    @Test
    public void testUpdateScopeForTypeChangeGetProp() throws Exception {
        Node left = mock(Node.class);
        when(left.getType()).thenReturn(Token.GETPROP);
        when(left.getQualifiedName()).thenReturn("a.b");
        when(left.getLastChild()).thenReturn(mockNode);
        when(mockNode.getString()).thenReturn("b");
        when(left.getFirstChild()).thenReturn(mockNode);
        when(mockNode.getJSType()).thenReturn(mockObjectType);
        when(mockObjectType.restrictByNotNullOrUndefined()).thenReturn(mockObjectType);
        when(mockObjectType.cast(mockObjectType)).thenReturn(mockObjectType);
        when(mockObjectType.isPropertyTypeDeclared("b")).thenReturn(true);

        JSType resultType = mockStringType;
        when(mockStringType.restrictByNotNullOrUndefined()).thenReturn(mockStringType);

        FlowScope scope = mockFlowScope;
        Whitebox.invokeMethod(typeInference, "updateScopeForTypeChange", scope, left, mockJSType, resultType);
        verify(left).setJSType(resultType);
        verify(scope).inferQualifiedSlot(left, "a.b", mockJSType, resultType);
    }

    @Test
    public void testEnsurePropertyDefinedNewProperty() throws Exception {
        Node getprop = mock(Node.class);
        when(getprop.getLastChild()).thenReturn(mockNode);
        when(mockNode.getString()).thenReturn("newProp");
        when(getprop.getFirstChild()).thenReturn(mockNode);
        when(mockNode.getJSType()).thenReturn(mockObjectType);
        when(mockObjectType.restrictByNotNullOrUndefined()).thenReturn(mockObjectType);
        when(mockObjectType.cast(mockObjectType)).thenReturn(mockObjectType);
        when(mockObjectType.isPropertyTypeDeclared("newProp")).thenReturn(false);
        when(mockObjectType.hasProperty("newProp")).thenReturn(false);
        when(mockObjectType.isInstanceType()).thenReturn(true);
        when(mockNode.isThis()).thenReturn(false);

        JSType rightType = mockStringType;
        Whitebox.invokeMethod(typeInference, "ensurePropertyDefined", getprop, rightType);
        verify(registry).registerPropertyOnType("newProp", mockObjectType);
    }

    @Test
    public void testEnsurePropertyDeclaredHelper() throws Exception {
        Node getprop = mock(Node.class);
        when(getprop.getLastChild()).thenReturn(mockNode);
        when(mockNode.getString()).thenReturn("prop");
        when(getprop.getQualifiedName()).thenReturn("a.b.prop");
        when(syntacticScope.getVar("a.b.prop")).thenReturn(mockVar);
        when(mockVar.isTypeInferred()).thenReturn(false);
        when(mockVar.getType()).thenReturn(mockStringType);
        when(mockObjectType.hasOwnProperty("prop")).thenReturn(false);
        when(mockObjectType.isInstanceType()).thenReturn(true);
        when(mockVar.isExtern()).thenReturn(true);
        when(mockObjectType.isNativeObjectType()).thenReturn(false);

        boolean result = Whitebox.invokeMethod(typeInference, "ensurePropertyDeclaredHelper", getprop, mockObjectType);
        assertTrue(result);
        verify(mockObjectType).defineDeclaredProperty("prop", mockStringType, getprop);
    }

    @Test
    public void testGetBooleanOutcomes() {
        BooleanLiteralSet left = BooleanLiteralSet.TRUE;
        BooleanLiteralSet right = BooleanLiteralSet.FALSE;
        BooleanLiteralSet result = TypeInference.getBooleanOutcomes(left, right, true);
        assertEquals(BooleanLiteralSet.BOTH, result);
    }

    @Test
    public void testGetBooleanOutcomePair() throws Exception {
        BooleanOutcomePair leftPair = new BooleanOutcomePair(BooleanLiteralSet.TRUE, BooleanLiteralSet.EMPTY, mockFlowScope, mockFlowScope);
        BooleanOutcomePair rightPair = new BooleanOutcomePair(BooleanLiteralSet.FALSE, BooleanLiteralSet.EMPTY, mockFlowScope, mockFlowScope);
        BooleanOutcomePair result = Whitebox.invokeMethod(typeInference, "getBooleanOutcomePair", leftPair, rightPair, true);
        assertEquals(BooleanLiteralSet.BOTH, result.toBooleanOutcomes);
    }

    @Test
    public void testNewBooleanOutcomePair() throws Exception {
        when(mockJSType.getPossibleToBooleanOutcomes()).thenReturn(BooleanLiteralSet.TRUE);
        when(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)).thenReturn(mockBooleanType);
        when(mockBooleanType.isSubtype(mockJSType)).thenReturn(false);

        BooleanOutcomePair result = Whitebox.invokeMethod(typeInference, "newBooleanOutcomePair", mockJSType, mockFlowScope);
        assertEquals(BooleanLiteralSet.TRUE, result.toBooleanOutcomes);
        assertEquals(BooleanLiteralSet.EMPTY, result.booleanValues);
    }

    @Test
    public void testRedeclareSimpleVar() throws Exception {
        Node nameNode = mock(Node.class);
        when(nameNode.isName()).thenReturn(true);
        when(nameNode.getString()).thenReturn("x");
        when(syntacticScope.getVar("x")).thenReturn(mockVar);
        when(mockVar.isLocal()).thenReturn(true);
        when(mockVar.isMarkedEscaped()).thenReturn(true);
        when(mockVar.getScope()).thenReturn(syntacticScope);

        Whitebox.invokeMethod(typeInference, "redeclareSimpleVar", mockFlowScope, nameNode, mockStringType);
        verify(mockFlowScope).inferSlotType("x", mockStringType);
    }

    @Test
    public void testIsUnflowable() throws Exception {
        when(mockVar.isLocal()).thenReturn(true);
        when(mockVar.isMarkedEscaped()).thenReturn(true);
        when(mockVar.getScope()).thenReturn(syntacticScope);
        boolean result = Whitebox.invokeMethod(typeInference, "isUnflowable", mockVar);
        assertTrue(result);
    }

    @Test
    public void testGetJSTypeNull() throws Exception {
        when(mockNode.getJSType()).thenReturn(null);
        JSType result = Whitebox.invokeMethod(typeInference, "getJSType", mockNode);
        assertSame(mockUnknownType, result);
    }

    @Test
    public void testGetJSTypeNonNull() throws Exception {
        when(mockNode.getJSType()).thenReturn(mockStringType);
        JSType result = Whitebox.invokeMethod(typeInference, "getJSType", mockNode);
        assertSame(mockStringType, result);
    }

    @Test
    public void testGetNativeType() throws Exception {
        JSType result = Whitebox.invokeMethod(typeInference, "getNativeType", JSTypeNative.STRING_TYPE);
        assertSame(mockStringType, result);
    }

    @Test
    public void testInferPropertyTypesToMatchConstraint() throws Exception {
        JSType type = mockStringType;
        JSType constraint = mockObjectType;
        when(mockObjectType.restrictByNotNullOrUndefined()).thenReturn(mockObjectType);
        when(mockObjectType.cast(mockObjectType)).thenReturn(mockObjectType);

        Whitebox.invokeMethod(typeInference, "inferPropertyTypesToMatchConstraint", type, constraint);
        verify(type).matchConstraint(mockObjectType);
    }

    @Test
    public void testDereferencePointer() throws Exception {
        Node n = mock(Node.class);
        when(n.isQualifiedName()).thenReturn(true);
        when(n.getJSType()).thenReturn(mockJSType);
        when(mockJSType.restrictByNotNullOrUndefined()).thenReturn(mockStringType);
        when(mockJSType.equals(mockStringType)).thenReturn(false);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "dereferencePointer", n, scope);
        assertNotNull(result);
    }

    @Test
    public void testGetPropertyTypeFromScope() throws Exception {
        Node n = mock(Node.class);
        when(n.getQualifiedName()).thenReturn("a.b");
        when(mockFlowScope.getSlot("a.b")).thenReturn(mockSlot);
        when(mockSlot.getType()).thenReturn(mockStringType);
        when(mockStringType.equals(mockUnknownType)).thenReturn(false);

        JSType result = Whitebox.invokeMethod(typeInference, "getPropertyType", mockObjectType, "b", n, mockFlowScope);
        assertSame(mockStringType, result);
    }

    @Test
    public void testGetPropertyTypeFromObject() throws Exception {
        Node n = mock(Node.class);
        when(n.getQualifiedName()).thenReturn("a.b");
        when(mockFlowScope.getSlot("a.b")).thenReturn(null);
        when(mockObjectType.findPropertyType("b")).thenReturn(mockStringType);

        JSType result = Whitebox.invokeMethod(typeInference, "getPropertyType", mockObjectType, "b", n, mockFlowScope);
        assertSame(mockStringType, result);
    }

    @Test
    public void testBackwardsInferenceFromCallSiteUpdateTypeOfParameters() throws Exception {
        Node callNode = mock(Node.class);
        when(callNode.getChildCount()).thenReturn(2);
        Node firstParam = mock(Node.class);
        when(callNode.getChildAtIndex(1)).thenReturn(firstParam);
        when(firstParam.getJSType()).thenReturn(mockStringType);
        when(mockFunctionType.getParameters()).thenReturn(Collections.singletonList(mockNode));
        when(mockNode.getJSType()).thenReturn(mockFunctionType);
        when(mockFunctionType.isFunctionType()).thenReturn(true);
        when(mockFunctionType.toMaybeFunctionType()).thenReturn(mockFunctionType);
        when(firstParam.isFunction()).thenReturn(true);
        when(firstParam.getJSDocInfo()).thenReturn(null);
        when(mockStringType.isFunctionType()).thenReturn(true);

        Whitebox.invokeMethod(typeInference, "backwardsInferenceFromCallSite", callNode, mockFunctionType);
        verify(firstParam).setJSType(mockFunctionType);
    }

    @Test
    public void testUpdateBind() throws Exception {
        Node callNode = mock(Node.class);
        when(codingConvention.describeFunctionBind(callNode, true)).thenReturn(mockBind);
        when(mockBind.target).thenReturn(mockNode);
        when(mockNode.getJSType()).thenReturn(mockFunctionType);
        when(mockFunctionType.restrictByNotNullOrUndefined()).thenReturn(mockFunctionType);
        when(mockFunctionType.toMaybeFunctionType()).thenReturn(mockFunctionType);
        when(mockBind.getBoundParameterCount()).thenReturn(1);
        when(mockFunctionType.getBindReturnType(2)).thenReturn(mockObjectType);

        Whitebox.invokeMethod(typeInference, "updateBind", callNode, mockFunctionType);
        verify(callNode).setJSType(mockObjectType);
    }

    @Test
    public void testTightenTypesAfterAssertions() throws Exception {
        Node callNode = mock(Node.class);
        Node left = mock(Node.class);
        when(callNode.getFirstChild()).thenReturn(left);
        when(left.getNext()).thenReturn(mockNode);
        when(left.getQualifiedName()).thenReturn("assert");
        when(assertionFunctionsMap.get("assert")).thenReturn(mockAssertionSpec);
        when(mockAssertionSpec.getAssertedParam(mockNode)).thenReturn(mockNode);
        when(mockAssertionSpec.getAssertedType()).thenReturn(JSTypeNative.STRING_TYPE);
        when(mockNode.getQualifiedName()).thenReturn("x");
        when(mockNode.getJSType()).thenReturn(mockJSType);
        when(mockJSType.getGreatestSubtype(mockStringType)).thenReturn(mockStringType);
        when(mockJSType.differsFrom(mockStringType)).thenReturn(true);
        when(callNode.getJSType()).thenReturn(mockJSType);
        when(mockJSType.differsFrom(mockStringType)).thenReturn(true);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "tightenTypesAfterAssertions", scope, callNode);
        assertNotNull(result);
        verify(callNode).setJSType(mockStringType);
    }

    @Test
    public void testNarrowScope() throws Exception {
        Node node = mock(Node.class);
        when(node.isThis()).thenReturn(false);
        when(node.isGetProp()).thenReturn(false);
        when(node.isName()).thenReturn(true);
        when(node.getString()).thenReturn("x");
        when(syntacticScope.getVar("x")).thenReturn(mockVar);
        when(mockVar.isLocal()).thenReturn(true);
        when(mockVar.isMarkedEscaped()).thenReturn(true);
        when(mockVar.getScope()).thenReturn(syntacticScope);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        FlowScope result = Whitebox.invokeMethod(typeInference, "narrowScope", scope, node, mockStringType);
        assertNotNull(result);
        verify(childFlowScope).inferSlotType("x", mockStringType);
    }

    @Test
    public void testTraverseAnd() throws Exception {
        Node andNode = mock(Node.class);
        when(andNode.getType()).thenReturn(Token.AND);
        Node left = mock(Node.class);
        Node right = mock(Node.class);
        when(andNode.getFirstChild()).thenReturn(left);
        when(andNode.getLastChild()).thenReturn(right);
        when(left.getType()).thenReturn(Token.TRUE);
        when(right.getType()).thenReturn(Token.FALSE);
        when(left.getJSType()).thenReturn(mockBooleanType);
        when(right.getJSType()).thenReturn(mockBooleanType);
        when(mockBooleanType.getRestrictedTypeGivenToBooleanOutcome(false)).thenReturn(mockBooleanType);
        when(mockBooleanType.getPossibleToBooleanOutcomes()).thenReturn(BooleanLiteralSet.TRUE);
        when(mockBooleanType.getLeastSupertype(mockBooleanType)).thenReturn(mockBooleanType);
        when(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)).thenReturn(mockBooleanType);
        when(mockBooleanType.isSubtype(mockBooleanType)).thenReturn(true);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);
        when(reverseInterpreter.getPreciserScopeKnowingConditionOutcome(eq(left), any(FlowScope.class), eq(true)))
                .thenReturn(childFlowScope);

        BooleanOutcomePair result = Whitebox.invokeMethod(typeInference, "traverseAnd", andNode, scope);
        assertNotNull(result);
        verify(andNode).setJSType(mockBooleanType);
    }

    @Test
    public void testTraverseOr() throws Exception {
        Node orNode = mock(Node.class);
        when(orNode.getType()).thenReturn(Token.OR);
        Node left = mock(Node.class);
        Node right = mock(Node.class);
        when(orNode.getFirstChild()).thenReturn(left);
        when(orNode.getLastChild()).thenReturn(right);
        when(left.getType()).thenReturn(Token.TRUE);
        when(right.getType()).thenReturn(Token.FALSE);
        when(left.getJSType()).thenReturn(mockBooleanType);
        when(right.getJSType()).thenReturn(mockBooleanType);
        when(mockBooleanType.getRestrictedTypeGivenToBooleanOutcome(true)).thenReturn(mockBooleanType);
        when(mockBooleanType.getPossibleToBooleanOutcomes()).thenReturn(BooleanLiteralSet.FALSE);
        when(mockBooleanType.getLeastSupertype(mockBooleanType)).thenReturn(mockBooleanType);
        when(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)).thenReturn(mockBooleanType);
        when(mockBooleanType.isSubtype(mockBooleanType)).thenReturn(true);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);
        when(reverseInterpreter.getPreciserScopeKnowingConditionOutcome(eq(left), any(FlowScope.class), eq(false)))
                .thenReturn(childFlowScope);

        BooleanOutcomePair result = Whitebox.invokeMethod(typeInference, "traverseOr", orNode, scope);
        assertNotNull(result);
        verify(orNode).setJSType(mockBooleanType);
    }

    @Test
    public void testTraverseShortCircuitingBinOpLeftLiteralTrue() throws Exception {
        Node andNode = mock(Node.class);
        when(andNode.getType()).thenReturn(Token.AND);
        Node left = mock(Node.class);
        Node right = mock(Node.class);
        when(andNode.getFirstChild()).thenReturn(left);
        when(andNode.getLastChild()).thenReturn(right);
        when(left.getType()).thenReturn(Token.TRUE);
        when(right.getType()).thenReturn(Token.FALSE);
        when(left.getJSType()).thenReturn(mockBooleanType);
        when(right.getJSType()).thenReturn(mockBooleanType);
        when(mockBooleanType.getRestrictedTypeGivenToBooleanOutcome(false)).thenReturn(mockBooleanType);
        when(mockBooleanType.getPossibleToBooleanOutcomes()).thenReturn(BooleanLiteralSet.TRUE);
        when(mockBooleanType.getLeastSupertype(mockBooleanType)).thenReturn(mockBooleanType);
        when(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)).thenReturn(mockBooleanType);
        when(mockBooleanType.isSubtype(mockBooleanType)).thenReturn(true);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);
        when(reverseInterpreter.getPreciserScopeKnowingConditionOutcome(eq(left), any(FlowScope.class), eq(true)))
                .thenReturn(childFlowScope);

        BooleanOutcomePair result = Whitebox.invokeMethod(typeInference, "traverseShortCircuitingBinOp", andNode, scope, true);
        assertNotNull(result);
        verify(andNode).setJSType(mockBooleanType);
    }

    @Test
    public void testTraverseWithinShortCircuitingBinOpDefault() throws Exception {
        Node node = mock(Node.class);
        when(node.getType()).thenReturn(Token.NAME);
        when(node.getJSType()).thenReturn(mockStringType);
        when(mockStringType.getPossibleToBooleanOutcomes()).thenReturn(BooleanLiteralSet.TRUE);
        when(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE)).thenReturn(mockBooleanType);
        when(mockBooleanType.isSubtype(mockStringType)).thenReturn(false);

        FlowScope scope = mockFlowScope;
        when(scope.createChildFlowScope()).thenReturn(childFlowScope);

        BooleanOutcomePair result = Whitebox.invokeMethod(typeInference, "traverseWithinShortCircuitingBinOp", node, scope);
        assertNotNull(result);
        assertEquals(BooleanLiteralSet.TRUE, result.toBooleanOutcomes);
    }

    @Test
    public void testBooleanOutcomePairGetJoinedFlowScope() {
        FlowScope leftScope = mock(FlowScope.class);
        FlowScope rightScope = mock(FlowScope.class);
        BooleanOutcomePair pair = new BooleanOutcomePair(BooleanLiteralSet.BOTH, BooleanLiteralSet.BOTH, leftScope, rightScope);
        FlowScope joined = pair.getJoinedFlowScope();
        assertNotNull(joined);
    }

    @Test
    public void testBooleanOutcomePairGetOutcomeFlowScopeAndTrue() {
        FlowScope leftScope = mock(FlowScope.class);
        FlowScope rightScope = mock(FlowScope.class);
        BooleanOutcomePair pair = new BooleanOutcomePair(BooleanLiteralSet.BOTH, BooleanLiteralSet.BOTH, leftScope, rightScope);
        FlowScope result = pair.getOutcomeFlowScope(Token.AND, true);
        assertSame(rightScope, result);
    }

    @Test
    public void testBooleanOutcomePairGetOutcomeFlowScopeOrFalse() {
        FlowScope leftScope = mock(FlowScope.class);
        FlowScope rightScope = mock(FlowScope.class);
        BooleanOutcomePair pair = new BooleanOutcomePair(BooleanLiteralSet.BOTH, BooleanLiteralSet.BOTH, leftScope, rightScope);
        FlowScope result = pair.getOutcomeFlowScope(Token.OR, false);
        assertSame(rightScope, result);
    }

    @Test
    public void testBooleanOutcomePairGetOutcomeFlowScopeOther() {
        FlowScope leftScope = mock(FlowScope.class);
        FlowScope rightScope = mock(FlowScope.class);
        BooleanOutcomePair pair = new BooleanOutcomePair(BooleanLiteralSet.BOTH, BooleanLiteralSet.BOTH, leftScope, rightScope);
        FlowScope result = pair.getOutcomeFlowScope(Token.AND, false);
        assertNotNull(result);
    }
}
