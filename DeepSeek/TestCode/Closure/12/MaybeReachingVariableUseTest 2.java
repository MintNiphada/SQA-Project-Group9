package com.google.javascript.jscomp;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.graph.GraphNode;
import com.google.javascript.jscomp.graph.LatticeElement;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({NodeUtil.class, Preconditions.class})
public class MaybeReachingVariableUseTest {

    @Mock
    private ControlFlowGraph<Node> mockCfg;
    @Mock
    private Scope mockScope;
    @Mock
    private AbstractCompiler mockCompiler;
    @Mock
    private GraphNode<Node, Branch> mockGraphNode;
    @Mock
    private FlowState<MaybeReachingVariableUse.ReachingUses> mockFlowState;
    @Mock
    private Var mockVar;

    private MaybeReachingVariableUse analysis;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(NodeUtil.class);
        PowerMockito.mockStatic(Preconditions.class);
        when(mockScope.getVar(anyString())).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        analysis = new MaybeReachingVariableUse(mockCfg, mockScope, mockCompiler);
    }

    @Test
    public void testReachingUsesEqualsAndHashCode() {
        MaybeReachingVariableUse.ReachingUses uses1 = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses uses2 = new MaybeReachingVariableUse.ReachingUses();
        assertEquals(uses1, uses2);
        assertEquals(uses1.hashCode(), uses2.hashCode());

        Node node = new Node(Token.NAME);
        uses1.mayUseMap.put(mockVar, node);
        assertNotEquals(uses1, uses2);
        assertNotEquals(uses1.hashCode(), uses2.hashCode());

        uses2.mayUseMap.put(mockVar, node);
        assertEquals(uses1, uses2);
        assertEquals(uses1.hashCode(), uses2.hashCode());

        assertFalse(uses1.equals(null));
        assertFalse(uses1.equals("string"));
    }

    @Test
    public void testReachingUsesCopyConstructor() {
        MaybeReachingVariableUse.ReachingUses original = new MaybeReachingVariableUse.ReachingUses();
        Node node = new Node(Token.NAME);
        original.mayUseMap.put(mockVar, node);
        MaybeReachingVariableUse.ReachingUses copy = new MaybeReachingVariableUse.ReachingUses(original);
        assertEquals(original, copy);
        assertNotSame(original.mayUseMap, copy.mayUseMap);
    }

    @Test
    public void testReachingUsesJoinOp() {
        MaybeReachingVariableUse.ReachingUsesJoinOp joinOp = new MaybeReachingVariableUse.ReachingUsesJoinOp();
        List<MaybeReachingVariableUse.ReachingUses> inputs = new ArrayList<>();
        MaybeReachingVariableUse.ReachingUses uses1 = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses uses2 = new MaybeReachingVariableUse.ReachingUses();
        Node node1 = new Node(Token.NAME);
        Node node2 = new Node(Token.NAME);
        uses1.mayUseMap.put(mockVar, node1);
        uses2.mayUseMap.put(mockVar, node2);
        inputs.add(uses1);
        inputs.add(uses2);
        MaybeReachingVariableUse.ReachingUses result = joinOp.apply(inputs);
        assertTrue(result.mayUseMap.containsEntry(mockVar, node1));
        assertTrue(result.mayUseMap.containsEntry(mockVar, node2));
    }

    @Test
    public void testIsForward() {
        assertFalse(analysis.isForward());
    }

    @Test
    public void testCreateEntryLattice() {
        MaybeReachingVariableUse.ReachingUses entry = analysis.createEntryLattice();
        assertNotNull(entry);
        assertTrue(entry.mayUseMap.isEmpty());
    }

    @Test
    public void testCreateInitialEstimateLattice() {
        MaybeReachingVariableUse.ReachingUses estimate = analysis.createInitialEstimateLattice();
        assertNotNull(estimate);
        assertTrue(estimate.mayUseMap.isEmpty());
    }

    @Test
    public void testFlowThroughWithNameNode() {
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("testVar");
        when(mockScope.getVar("testVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(nameNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, nameNode));
    }

    @Test
    public void testFlowThroughWithVarDeclaration() {
        Node varNode = new Node(Token.VAR);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("testVar");
        varNode.addChildToBack(nameNode);

        when(mockScope.getVar("testVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        input.mayUseMap.put(mockVar, new Node(Token.NAME));
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(varNode, input);

        assertFalse(output.mayUseMap.containsKey(mockVar));
    }

    @Test
    public void testFlowThroughWithVarDeclarationWithInitializer() {
        Node varNode = new Node(Token.VAR);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("testVar");
        Node initializer = new Node(Token.NUMBER);
        nameNode.addChildToBack(initializer);
        varNode.addChildToBack(nameNode);

        when(mockScope.getVar("testVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(varNode, input);

        assertFalse(output.mayUseMap.containsKey(mockVar));
    }

    @Test
    public void testFlowThroughWithAssignment() {
        Node assignNode = new Node(Token.ASSIGN);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("testVar");
        Node valueNode = new Node(Token.NUMBER);
        assignNode.addChildToBack(nameNode);
        assignNode.addChildToBack(valueNode);

        when(mockScope.getVar("testVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isAssignmentOp(assignNode)).thenReturn(true);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        input.mayUseMap.put(mockVar, new Node(Token.NAME));
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(assignNode, input);

        assertFalse(output.mayUseMap.containsKey(mockVar));
    }

    @Test
    public void testFlowThroughWithCompoundAssignment() {
        Node assignNode = new Node(Token.ASSIGN_ADD);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("testVar");
        Node valueNode = new Node(Token.NUMBER);
        assignNode.addChildToBack(nameNode);
        assignNode.addChildToBack(valueNode);

        when(mockScope.getVar("testVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isAssignmentOp(assignNode)).thenReturn(true);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(assignNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, assignNode));
    }

    @Test
    public void testFlowThroughWithAndNode() {
        Node andNode = new Node(Token.AND);
        Node left = new Node(Token.NAME);
        left.setString("leftVar");
        Node right = new Node(Token.NAME);
        right.setString("rightVar");
        andNode.addChildToBack(left);
        andNode.addChildToBack(right);

        when(mockScope.getVar("leftVar")).thenReturn(mockVar);
        when(mockScope.getVar("rightVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(andNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, right));
        assertTrue(output.mayUseMap.containsEntry(mockVar, left));
    }

    @Test
    public void testFlowThroughWithOrNode() {
        Node orNode = new Node(Token.OR);
        Node left = new Node(Token.NAME);
        left.setString("leftVar");
        Node right = new Node(Token.NAME);
        right.setString("rightVar");
        orNode.addChildToBack(left);
        orNode.addChildToBack(right);

        when(mockScope.getVar("leftVar")).thenReturn(mockVar);
        when(mockScope.getVar("rightVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(orNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, right));
        assertTrue(output.mayUseMap.containsEntry(mockVar, left));
    }

    @Test
    public void testFlowThroughWithHookNode() {
        Node hookNode = new Node(Token.HOOK);
        Node condition = new Node(Token.NAME);
        condition.setString("condVar");
        Node trueBranch = new Node(Token.NAME);
        trueBranch.setString("trueVar");
        Node falseBranch = new Node(Token.NAME);
        falseBranch.setString("falseVar");
        hookNode.addChildToBack(condition);
        hookNode.addChildToBack(trueBranch);
        hookNode.addChildToBack(falseBranch);

        when(mockScope.getVar("condVar")).thenReturn(mockVar);
        when(mockScope.getVar("trueVar")).thenReturn(mockVar);
        when(mockScope.getVar("falseVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(hookNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, falseBranch));
        assertTrue(output.mayUseMap.containsEntry(mockVar, trueBranch));
        assertTrue(output.mayUseMap.containsEntry(mockVar, condition));
    }

    @Test
    public void testFlowThroughWithWhileNode() {
        Node whileNode = new Node(Token.WHILE);
        Node condition = new Node(Token.NAME);
        condition.setString("condVar");
        whileNode.addChildToBack(condition);
        whileNode.addChildToBack(new Node(Token.BLOCK));

        when(mockScope.getVar("condVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.getConditionExpression(whileNode)).thenReturn(condition);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(whileNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, condition));
    }

    @Test
    public void testFlowThroughWithDoNode() {
        Node doNode = new Node(Token.DO);
        Node condition = new Node(Token.NAME);
        condition.setString("condVar");
        doNode.addChildToBack(new Node(Token.BLOCK));
        doNode.addChildToBack(condition);

        when(mockScope.getVar("condVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.getConditionExpression(doNode)).thenReturn(condition);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(doNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, condition));
    }

    @Test
    public void testFlowThroughWithIfNode() {
        Node ifNode = new Node(Token.IF);
        Node condition = new Node(Token.NAME);
        condition.setString("condVar");
        ifNode.addChildToBack(condition);
        ifNode.addChildToBack(new Node(Token.BLOCK));

        when(mockScope.getVar("condVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.getConditionExpression(ifNode)).thenReturn(condition);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(ifNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, condition));
    }

    @Test
    public void testFlowThroughWithForNode() {
        Node forNode = new Node(Token.FOR);
        Node condition = new Node(Token.NAME);
        condition.setString("condVar");
        forNode.addChildToBack(new Node(Token.EMPTY));
        forNode.addChildToBack(condition);
        forNode.addChildToBack(new Node(Token.EMPTY));
        forNode.addChildToBack(new Node(Token.BLOCK));

        when(mockScope.getVar("condVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isForIn(forNode)).thenReturn(false);
        when(NodeUtil.getConditionExpression(forNode)).thenReturn(condition);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(forNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, condition));
    }

    @Test
    public void testFlowThroughWithForInNode() {
        Node forInNode = new Node(Token.FOR);
        Node lhs = new Node(Token.NAME);
        lhs.setString("iterVar");
        Node rhs = new Node(Token.NAME);
        rhs.setString("objVar");
        forInNode.addChildToBack(lhs);
        forInNode.addChildToBack(rhs);
        forInNode.addChildToBack(new Node(Token.BLOCK));

        when(mockScope.getVar("iterVar")).thenReturn(mockVar);
        when(mockScope.getVar("objVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isForIn(forInNode)).thenReturn(true);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        input.mayUseMap.put(mockVar, new Node(Token.NAME));
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(forInNode, input);

        assertFalse(output.mayUseMap.containsKey(mockVar));
        assertTrue(output.mayUseMap.containsEntry(mockVar, rhs));
    }

    @Test
    public void testFlowThroughWithForInVarNode() {
        Node forInNode = new Node(Token.FOR);
        Node varNode = new Node(Token.VAR);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("iterVar");
        varNode.addChildToBack(nameNode);
        Node rhs = new Node(Token.NAME);
        rhs.setString("objVar");
        forInNode.addChildToBack(varNode);
        forInNode.addChildToBack(rhs);
        forInNode.addChildToBack(new Node(Token.BLOCK));

        when(mockScope.getVar("iterVar")).thenReturn(mockVar);
        when(mockScope.getVar("objVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isForIn(forInNode)).thenReturn(true);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        input.mayUseMap.put(mockVar, new Node(Token.NAME));
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(forInNode, input);

        assertFalse(output.mayUseMap.containsKey(mockVar));
        assertTrue(output.mayUseMap.containsEntry(mockVar, rhs));
    }

    @Test
    public void testFlowThroughWithBlockNode() {
        Node blockNode = new Node(Token.BLOCK);
        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        input.mayUseMap.put(mockVar, new Node(Token.NAME));
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(blockNode, input);
        assertEquals(input, output);
    }

    @Test
    public void testFlowThroughWithFunctionNode() {
        Node funcNode = new Node(Token.FUNCTION);
        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        input.mayUseMap.put(mockVar, new Node(Token.NAME));
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(funcNode, input);
        assertEquals(input, output);
    }

    @Test
    public void testFlowThroughWithDefaultNode() {
        Node defaultNode = new Node(Token.CALL);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("funcVar");
        defaultNode.addChildToBack(nameNode);

        when(mockScope.getVar("funcVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isAssignmentOp(defaultNode)).thenReturn(false);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(defaultNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, nameNode));
    }

    @Test
    public void testAddToUseIfLocalWithNullVar() {
        when(mockScope.getVar("unknownVar")).thenReturn(null);
        MaybeReachingVariableUse.ReachingUses uses = new MaybeReachingVariableUse.ReachingUses();
        Node node = new Node(Token.NAME);
        analysis.flowThrough(node, uses);
        assertFalse(uses.mayUseMap.containsKey(null));
    }

    @Test
    public void testAddToUseIfLocalWithDifferentScope() {
        Var differentScopeVar = mock(Var.class);
        when(differentScopeVar.scope).thenReturn(mock(Scope.class));
        when(mockScope.getVar("differentScopeVar")).thenReturn(differentScopeVar);
        Node node = new Node(Token.NAME);
        node.setString("differentScopeVar");
        MaybeReachingVariableUse.ReachingUses uses = new MaybeReachingVariableUse.ReachingUses();
        analysis.flowThrough(node, uses);
        assertFalse(uses.mayUseMap.containsKey(differentScopeVar));
    }

    @Test
    public void testRemoveFromUseIfLocalWithNullVar() {
        when(mockScope.getVar("unknownVar")).thenReturn(null);
        MaybeReachingVariableUse.ReachingUses uses = new MaybeReachingVariableUse.ReachingUses();
        uses.mayUseMap.put(mockVar, new Node(Token.NAME));
        Node varNode = new Node(Token.VAR);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("unknownVar");
        varNode.addChildToBack(nameNode);
        analysis.flowThrough(varNode, uses);
        assertTrue(uses.mayUseMap.containsKey(mockVar));
    }

    @Test
    public void testRemoveFromUseIfLocalWithDifferentScope() {
        Var differentScopeVar = mock(Var.class);
        when(differentScopeVar.scope).thenReturn(mock(Scope.class));
        when(mockScope.getVar("differentScopeVar")).thenReturn(differentScopeVar);
        MaybeReachingVariableUse.ReachingUses uses = new MaybeReachingVariableUse.ReachingUses();
        uses.mayUseMap.put(differentScopeVar, new Node(Token.NAME));
        Node varNode = new Node(Token.VAR);
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("differentScopeVar");
        varNode.addChildToBack(nameNode);
        analysis.flowThrough(varNode, uses);
        assertTrue(uses.mayUseMap.containsKey(differentScopeVar));
    }

    @Test
    public void testGetUses() {
        Node defNode = new Node(Token.NAME);
        when(mockCfg.getNode(defNode)).thenReturn(mockGraphNode);
        when(mockGraphNode.getAnnotation()).thenReturn(mockFlowState);
        MaybeReachingVariableUse.ReachingUses outState = new MaybeReachingVariableUse.ReachingUses();
        Node useNode = new Node(Token.NAME);
        outState.mayUseMap.put(mockVar, useNode);
        when(mockFlowState.getOut()).thenReturn(outState);
        when(mockScope.getVar("testVar")).thenReturn(mockVar);

        Collection<Node> uses = analysis.getUses("testVar", defNode);
        assertNotNull(uses);
        assertTrue(uses.contains(useNode));
    }

    @Test(expected = NullPointerException.class)
    public void testGetUsesWithNullDefNode() {
        when(mockCfg.getNode(null)).thenReturn(null);
        analysis.getUses("testVar", null);
    }

    @Test
    public void testFlowThroughPreservesInput() {
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("testVar");
        when(mockScope.getVar("testVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        Node existingNode = new Node(Token.NAME);
        input.mayUseMap.put(mockVar, existingNode);
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(nameNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, existingNode));
        assertTrue(output.mayUseMap.containsEntry(mockVar, nameNode));
    }

    @Test
    public void testFlowThroughWithEscapedVariable() {
        Node nameNode = new Node(Token.NAME);
        nameNode.setString("escapedVar");
        when(mockScope.getVar("escapedVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);

        Set<Var> escapedSet = Collections.singleton(mockVar);
        analysis = new MaybeReachingVariableUse(mockCfg, mockScope, mockCompiler) {
            @Override
            void computeEscaped(Scope jsScope, Set<Var> escaped, AbstractCompiler compiler) {
                escaped.addAll(escapedSet);
            }
        };

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(nameNode, input);

        assertFalse(output.mayUseMap.containsKey(mockVar));
    }

    @Test
    public void testFlowThroughWithAssignmentAndNonNameLhs() {
        Node assignNode = new Node(Token.ASSIGN);
        Node propNode = new Node(Token.GETPROP);
        Node objNode = new Node(Token.NAME);
        objNode.setString("objVar");
        Node propNameNode = new Node(Token.STRING);
        propNameNode.setString("prop");
        propNode.addChildToBack(objNode);
        propNode.addChildToBack(propNameNode);
        Node valueNode = new Node(Token.NUMBER);
        assignNode.addChildToBack(propNode);
        assignNode.addChildToBack(valueNode);

        when(mockScope.getVar("objVar")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isAssignmentOp(assignNode)).thenReturn(true);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(assignNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, objNode));
    }

    @Test
    public void testFlowThroughWithMultipleChildren() {
        Node callNode = new Node(Token.CALL);
        Node funcNode = new Node(Token.NAME);
        funcNode.setString("funcVar");
        Node arg1 = new Node(Token.NAME);
        arg1.setString("arg1Var");
        Node arg2 = new Node(Token.NAME);
        arg2.setString("arg2Var");
        callNode.addChildToBack(funcNode);
        callNode.addChildToBack(arg1);
        callNode.addChildToBack(arg2);

        when(mockScope.getVar("funcVar")).thenReturn(mockVar);
        when(mockScope.getVar("arg1Var")).thenReturn(mockVar);
        when(mockScope.getVar("arg2Var")).thenReturn(mockVar);
        when(mockVar.scope).thenReturn(mockScope);
        when(NodeUtil.isAssignmentOp(callNode)).thenReturn(false);

        MaybeReachingVariableUse.ReachingUses input = new MaybeReachingVariableUse.ReachingUses();
        MaybeReachingVariableUse.ReachingUses output = analysis.flowThrough(callNode, input);

        assertTrue(output.mayUseMap.containsEntry(mockVar, arg2));
        assertTrue(output.mayUseMap.containsEntry(mockVar, arg1));
        assertTrue(output.mayUseMap.containsEntry(mockVar, funcNode));
    }
}
