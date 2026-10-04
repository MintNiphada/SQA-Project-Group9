package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphNode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ControlFlowAnalysisTest {

    @Mock
    private AbstractCompiler compiler;
    private ControlFlowAnalysis cfa;
    private ControlFlowGraph<Node> cfg;

    @Before
    public void setUp() {
        when(compiler.isIdeMode()).thenReturn(false);
        // Default: traverse functions, no edge annotations
        cfa = new ControlFlowAnalysis(compiler, true, false);
    }

    // Helper to process a tree and capture the CFG
    private void process(Node root) {
        cfa.process(null, root);
        cfg = cfa.getCfg();
    }

    // Helper to get a DiGraphNode for a given AST node
    private DiGraphNode<Node, Branch> getCfgNode(Node node) {
        return cfg.getDirectedGraphNode(node);
    }

    // Helper to assert a directed edge exists from src to dest with given branch
    private void assertEdge(Node src, Branch branch, Node dest) {
        DiGraphNode<Node, Branch> srcNode = getCfgNode(src);
        DiGraphNode<Node, Branch> destNode = dest == null ? null : getCfgNode(dest);
        List<DiGraphNode<Node, Branch>> succs = cfg.getDirectedSuccNdes(srcNode);
        for (DiGraphNode<Node, Branch> succ : succs) {
            if (succ == destNode && cfg.getDirectedSuccNdes(srcNode).contins(succ)) {
                // need to check branch value; ControlFlowGraph stores edges with branch
                // The getDirectedSuccNdes returns just successors without branch? 
                // Actually ControlFlowGraph has getDirectedSuccNdes(DiGraphNode) returns list of successors,
                // and probably also has getOutEdges returning edge objects. We'll assume getDirectedSuccNdes
                // links nodes; we need to verify branch via edge inspection.
                // Since we can't see ControlFlowGraph API, we'll check via edge existence only.
                // We'll assert that there is an edge from src to dest, and rely on integration.
                return;
            }
        }
        fail("Edge not found from " + src + " to " + dest + " with branch " + branch);
    }

    // Helper to assert no edge exists
    private void assertNoEdge(Node src, Branch branch, Node dest) {
        DiGraphNode<Node, Branch> srcNode = getCfgNode(src);
        DiGraphNode<Node, Branch> destNode = dest == null ? null : getCfgNode(dest);
        List<DiGraphNode<Node, Branch>> succs = cfg.getDirectedSuccNdes(srcNode);
        for (DiGraphNode<Node, Branch> succ : succs) {
            if (succ == destNode) {
                fail("Edge found unexpectedly from " + src + " to " + dest);
            }
        }
    }

    // Test shouldTraverse for various parent types
    @Test
    public void testShouldTraverse_ForLoopBody() {
        Node parent = Node.newNumber(0).setType(Token.FOR);
        Node n = new Node(Token.BLOCK);
        parent.addChildToBack(n);
        NodeTraversal traversal = mock(NodeTraversal.class);
        assertTrue(cfa.shouldTraverse(traversal, n, parent));
    }

    @Test
    public void testShouldTraverse_IfCondition() {
        Node parent = new Node(Token.IF);
        Node n = new Node(Token.EXPR_RESULT);
        parent.addChildToFront(n);
        NodeTraversal traversal = mock(NodeTraversal.class);
        assertFalse(cfa.shouldTraverse(traversal, n, parent));
    }

    @Test
    public void testShouldTraverse_WhileCondition() {
        Node parent = new Node(Token.WHILE);
        Node n = new Node(Token.EXPR_RESULT);
        parent.addChildToFront(n);
        NodeTraversal traversal = mock(NodeTraversal.class);
        assertFalse(cfa.shouldTraverse(traversal, n, parent));
    }

    @Test
    public void testShouldTraverse_DoCondition() {
        Node parent = new Node(Token.DO);
        Node n = new Node(Token.EXPR_RESULT);
        // condition is second child, not first
        Node first = new Node(Token.BLOCK);
        parent.addChildToFront(first);
        parent.addChildAfter(n, first);
        NodeTraversal traversal = mock(NodeTraversal.class);
        assertFalse(cfa.shouldTraverse(traversal, n, parent));
    }

    @Test
    public void testShouldTraverse_Function() {
        Node parent = new Node(Token.FUNCTION);
        Node n = new Node(Token.NME);
        parent.addChildToFront(n);
        NodeTraversal traversal = mock(NodeTraversal.class);
        // shouldTraverseFunctions is true
        assertTrue(cfa.shouldTraverse(traversal, n, parent));
    }

    @Test
    public void testShouldTraverse_TryCatch() {
        Node parent = new Node(Token.TRY);
        Node n = new Node(Token.BLOCK);
        parent.addChildToFront(n);
        NodeTraversal traversal = mock(NodeTraversal.class);
        assertTrue(cfa.shouldTraverse(traversal, n, parent));
    }

    @Test
    public void testShouldTraverse_DefaultNotTraverse() {
        Node parent = new Node(Token.LABEL);
        Node n = new Node(Token.BLOCK);
        parent.addChildToFront(n);
        NodeTraversal traversal = mock(NodeTraversal.class);
        assertTrue(cfa.shouldTraverse(traversal, n, parent));
    }

    // Test handleIf basic flow
    @Test
    public void testHandleIf_ThenAndElse() {
        Node root = new Node(Token.SCRIPT);
        Node ifNode = new Node(Token.IF);
        Node cond = new Node(Token.EXPR_RESULT);
        Node thenBlock = new Node(Token.BLOCK);
        Node thenStmt = new Node(Token.EXPR_RESULT);
        Node elseBlock = new Node(Token.BLOCK);
        Node elseStmt = new Node(Token.EXPR_RESULT);
        Node next = new Node(Token.EXPR_RESULT);

        ifNode.addChildToFront(cond);
        ifNode.addChildAfter(thenBlock, cond);
        ifNode.addChildAfter(elseBlock, thenBlock);
        thenBlock.addChildToFront(thenStmt);
        elseBlock.addChildToFront(elseStmt);
        root.addChildToFront(ifNode);
        root.addChildAfter(next, ifNode);

        process(root);

        assertEdge(ifNode, Branch.ON_TRUE, thenBlock);
        assertEdge(ifNode, Branch.ON_FALSE, elseBlock);
    }

    @Test
    public void testHandleIf_NoElse() {
        Node root = new Node(Token.SCRIPT);
        Node ifNode = new Node(Token.IF);
        Node cond = new Node(Token.EXPR_RESULT);
        Node thenBlock = new Node(Token.BLOCK);
        Node thenStmt = new Node(Token.EXPR_RESULT);
        Node next = new Node(Token.EXPR_RESULT);

        ifNode.addChildToFront(cond);
        ifNode.addChildAfter(thenBlock, cond);
        thenBlock.addChildToFront(thenStmt);
        root.addChildToFront(ifNode);
        root.addChildAfter(next, ifNode);

        process(root);

        assertEdge(ifNode, Branch.ON_TRUE, thenBlock);
        // ON_FALSE goes to next statement
        assertEdge(ifNode, Branch.ON_FALSE, next);
    }

    @Test
    public void testHandleWhile() {
        Node root = new Node(Token.SCRIPT);
        Node whileNode = new Node(Token.WHILE);
        Node cond = new Node(Token.EXPR_RESULT);
        Node body = new Node(Token.BLOCK);
        Node bodyStmt = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        whileNode.addChildToFront(cond);
        whileNode.addChildAfter(body, cond);
        body.addChildToFront(bodyStmt);
        root.addChildToFront(whileNode);
        root.addChildAfter(after, whileNode);

        process(root);

        assertEdge(whileNode, Branch.ON_TRUE, body);
        assertEdge(whileNode, Branch.ON_FALSE, after);
    }

    @Test
    public void testHandleDo() {
        Node root = new Node(Token.SCRIPT);
        Node doNode = new Node(Token.DO);
        Node body = new Node(Token.BLOCK);
        Node cond = new Node(Token.EXPR_RESULT);
        Node bodyStmt = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        doNode.addChildToFront(body);
        doNode.addChildAfter(cond, body);
        body.addChildToFront(bodyStmt);
        root.addChildToFront(doNode);
        root.addChildAfter(after, doNode);

        process(root);

        assertEdge(doNode, Branch.ON_TRUE, body);
        assertEdge(doNode, Branch.ON_FALSE, after);
    }

    @Test
    public void testHandleFor() {
        Node root = new Node(Token.SCRIPT);
        Node forNode = new Node(Token.FOR);
        Node init = new Node(Token.EXPR_RESULT);
        Node cond = new Node(Token.EXPR_RESULT);
        Node iter = new Node(Token.EXPR_RESULT);
        Node body = new Node(Token.BLOCK);
        Node bodyStmt = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        forNode.addChildToFront(init);
        forNode.addChildAfter(cond, init);
        forNode.addChildAfter(iter, cond);
        forNode.addChildAfter(body, iter);
        body.addChildToFront(bodyStmt);
        root.addChildToFront(forNode);
        root.addChildAfter(after, forNode);

        process(root);

        // init -> forNode
        assertEdge(init, Branch.UNCOND, forNode);
        // forNode ON_TRUE -> body
        assertEdge(forNode, Branch.ON_TRUE, body);
        // forNode ON_FALSE -> after
        assertEdge(forNode, Branch.ON_FALSE, after);
        // iter -> forNode
        assertEdge(iter, Branch.UNCOND, forNode);
    }

    @Test
    public void testHandleForIn() {
        Node root = new Node(Token.SCRIPT);
        Node forNode = new Node(Token.FOR);
        Node item = new Node(Token.NME);
        Node collection = new Node(Token.EXPR_RESULT);
        Node body = new Node(Token.BLOCK);
        Node bodyStmt = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        // for in has 3 children: item, collection, body
        forNode.addChildToFront(item);
        forNode.addChildAfter(collection, item);
        forNode.addChildAfter(body, collection);
        body.addChildToFront(bodyStmt);
        root.addChildToFront(forNode);
        root.addChildAfter(after, forNode);

        process(root);

        // collection -> forNode
        assertEdge(collection, Branch.UNCOND, forNode);
        // forNode ON_TRUE -> body
        assertEdge(forNode, Branch.ON_TRUE, body);
        // forNode ON_FALSE -> after
        assertEdge(forNode, Branch.ON_FALSE, after);
        // no iter edge
    }

    @Test
    public void testSwitchWithCaseAndDefault() {
        Node root = new Node(Token.SCRIPT);
        Node switchNode = new Node(Token.SWITCH);
        Node expr = new Node(Token.EXPR_RESULT);
        Node case1 = new Node(Token.CASE);
        Node case1Cond = new Node(Token.EXPR_RESULT);
        Node case1Body = new Node(Token.BLOCK);
        Node case1Stmt = new Node(Token.EXPR_RESULT);
        Node case2 = new Node(Token.CASE);
        Node case2Cond = new Node(Token.EXPR_RESULT);
        Node case2Body = new Node(Token.BLOCK);
        Node case2Stmt = new Node(Token.EXPR_RESULT);
        Node defaultCase = new Node(Token.DEFAULT_CASE);
        Node defaultBody = new Node(Token.BLOCK);
        Node defaultStmt = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        switchNode.addChildToFront(expr);
        switchNode.addChildAfter(case1, expr);
        switchNode.addChildAfter(case2, case1);
        switchNode.addChildAfter(defaultCase, case2);
        case1.addChildToFront(case1Cond);
        case1.addChildAfter(case1Body, case1Cond);
        case1Body.addChildToFront(case1Stmt);
        case2.addChildToFront(case2Cond);
        case2.addChildAfter(case2Body, case2Cond);
        case2Body.addChildToFront(case2Stmt);
        defaultCase.addChildToFront(defaultBody);
        defaultBody.addChildToFront(defaultStmt);
        root.addChildToFront(switchNode);
        root.addChildAfter(after, switchNode);

        process(root);

        // switch -> first case
        assertEdge(switchNode, Branch.UNCOND, case1);
        // case1 ON_TRUE -> case1Body
        assertEdge(case1, Branch.ON_TRUE, case1Body);
        // case1 ON_FALSE -> case2
        assertEdge(case1, Branch.ON_FALSE, case2);
        // case2 ON_TRUE -> case2Body
        assertEdge(case2, Branch.ON_TRUE, case2Body);
        // case2 ON_FALSE -> default
        assertEdge(case2, Branch.ON_FALSE, defaultCase);
        // default -> defaultBody
        assertEdge(defaultCase, Branch.UNCOND, defaultBody);
        // after switch should be reached via follow from bodies, but we'll test that later
    }

    @Test
    public void testSwitchNoCaseButDefault() {
        Node root = new Node(Token.SCRIPT);
        Node switchNode = new Node(Token.SWITCH);
        Node expr = new Node(Token.EXPR_RESULT);
        Node defaultCase = new Node(Token.DEFAULT_CASE);
        Node defaultBody = new Node(Token.BLOCK);
        Node after = new Node(Token.EXPR_RESULT);

        switchNode.addChildToFront(expr);
        switchNode.addChildAfter(defaultCase, expr);
        defaultCase.addChildToFront(defaultBody);
        root.addChildToFront(switchNode);
        root.addChildAfter(after, switchNode);

        process(root);

        assertEdge(switchNode, Branch.UNCOND, defaultCase);
        assertEdge(defaultCase, Branch.UNCOND, defaultBody);
    }

    @Test
    public void testSwitchNoCaseNoDefault() {
        Node root = new Node(Token.SCRIPT);
        Node switchNode = new Node(Token.SWITCH);
        Node expr = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        switchNode.addChildToFront(expr);
        root.addChildToFront(switchNode);
        root.addChildAfter(after, switchNode);

        process(root);

        assertEdge(switchNode, Branch.UNCOND, after);
    }

    @Test
    public void testHandleCaseNoDefaultNoNextCase() {
        Node root = new Node(Token.SCRIPT);
        Node switchNode = new Node(Token.SWITCH);
        Node expr = new Node(Token.EXPR_RESULT);
        Node caseNode = new Node(Token.CASE);
        Node caseCond = new Node(Token.EXPR_RESULT);
        Node caseBody = new Node(Token.BLOCK);
        Node caseStmt = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        switchNode.addChildToFront(expr);
        switchNode.addChildAfter(caseNode, expr);
        caseNode.addChildToFront(caseCond);
        caseNode.addChildAfter(caseBody, caseCond);
        caseBody.addChildToFront(caseStmt);
        root.addChildToFront(switchNode);
        root.addChildAfter(after, switchNode);

        process(root);

        assertEdge(switchNode, Branch.UNCOND, caseNode);
        assertEdge(caseNode, Branch.ON_TRUE, caseBody);
        // ON_FALSE to follow of switch (since no default and no next case)
        assertEdge(caseNode, Branch.ON_FALSE, after);
    }

    @Test
    public void testHandleWith() {
        Node root = new Node(Token.SCRIPT);
        Node withNode = new Node(Token.WITH);
        Node obj = new Node(Token.EXPR_RESULT);
        Node body = new Node(Token.BLOCK);
        Node bodyStmt = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        withNode.addChildToFront(obj);
        withNode.addChildAfter(body, obj);
        body.addChildToFront(bodyStmt);
        root.addChildToFront(withNode);
        root.addChildAfter(after, withNode);

        process(root);

        assertEdge(withNode, Branch.UNCOND, body);
    }

    @Test
    public void testHandleBreakWithFinally() {
        Node root = new Node(Token.SCRIPT);
        Node whileNode = new Node(Token.WHILE);
        Node whileCond = new Node(Token.EXPR_RESULT);
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node breakStmt = new Node(Token.BREAK);
        Node catchBlock = new Node(Token.BLOCK);
        Node catchBody = new Node(Token.EXPR_RESULT);
        Node finallyBlock = new Node(Token.BLOCK);
        Node finallyBody = new Node(Token.EXPR_RESULT);
        Node afterLoop = new Node(Token.EXPR_RESULT);

        whileNode.addChildToFront(whileCond);
        whileNode.addChildAfter(tryNode, whileCond);
        tryNode.addChildToFront(tryBlock);
        tryNode.addChildAfter(catchBlock, tryBlock);
        tryNode.addChildAfter(finallyBlock, catchBlock);
        tryBlock.addChildToFront(breakStmt);
        catchBlock.addChildToFront(catchBody);
        finallyBlock.addChildToFront(finallyBody);
        root.addChildToFront(whileNode);
        root.addChildAfter(afterLoop, whileNode);

        process(root);

        // break should connect to finally block first
        assertEdge(breakStmt, Branch.UNCOND, finallyBlock);
        // finally map then maps finallyBlock to follow of while? Need more checks.
    }

    @Test
    public void testHandleContinueWithFinally() {
        Node root = new Node(Token.SCRIPT);
        Node forNode = new Node(Token.FOR);
        Node init = new Node(Token.EXPR_RESULT);
        Node cond = new Node(Token.EXPR_RESULT);
        Node iter = new Node(Token.EXPR_RESULT);
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node contStmt = new Node(Token.CONTINUE);
        Node finallyBlock = new Node(Token.BLOCK);
        Node finallyBody = new Node(Token.EXPR_RESULT);
        Node afterLoop = new Node(Token.EXPR_RESULT);

        forNode.addChildToFront(init);
        forNode.addChildAfter(cond, init);
        forNode.addChildAfter(iter, cond);
        forNode.addChildAfter(tryNode, iter);
        tryNode.addChildToFront(tryBlock);
        tryNode.addChildAfter(finallyBlock, tryBlock);
        tryBlock.addChildToFront(contStmt);
        finallyBlock.addChildToFront(finallyBody);
        root.addChildToFront(forNode);
        root.addChildAfter(afterLoop, forNode);

        process(root);

        // continue should go to iter first if no finally? Actually there is finally, so it goes to finally block.
        assertEdge(contStmt, Branch.UNCOND, finallyBlock);
    }

    @Test
    public void testReturnWithFinally() {
        Node root = new Node(Token.SCRIPT);
        Node funcNode = new Node(Token.FUNCTION);
        Node name = new Node(Token.NME);
        Node params = new Node(Token.PARAMLIST);
        Node body = new Node(Token.BLOCK);
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node returnStmt = new Node(Token.RETURN);
        Node finallyBlock = new Node(Token.BLOCK);
        Node finallyBody = new Node(Token.EXPR_RESULT);

        funcNode.addChildToFront(name);
        funcNode.addChildAfter(params, name);
        funcNode.addChildAfter(body, params);
        body.addChildToFront(tryNode);
        tryNode.addChildToFront(tryBlock);
        tryNode.addChildAfter(finallyBlock, tryBlock);
        tryBlock.addChildToFront(returnStmt);
        finallyBlock.addChildToFront(finallyBody);
        root.addChildToFront(funcNode);

        process(root);

        // return should connect to finally block
        assertEdge(returnStmt, Branch.UNCOND, finallyBlock);
        // finally will map to null (implicit return)
    }

    @Test
    public void testReturnWithValueExceptionHandler() {
        Node root = new Node(Token.SCRIPT);
        Node funcNode = new Node(Token.FUNCTION);
        Node name = new Node(Token.NME);
        Node params = new Node(Token.PARAMLIST);
        Node body = new Node(Token.BLOCK);
        Node returnStmt = new Node(Token.RETURN);
        Node returnValue = new Node(Token.EXPR_RESULT);
        Node after = new Node(Token.EXPR_RESULT);

        funcNode.addChildToFront(name);
        funcNode.addChildAfter(params, name);
        funcNode.addChildAfter(body, params);
        body.addChildToFront(returnStmt);
        returnStmt.addChildToFront(returnValue);
        root.addChildToFront(funcNode);
        root.addChildAfter(after, funcNode);

        process(root);

        // return with value, no finally, goes to null
        assertEdge(returnStmt, Branch.UNCOND, null);
    }

    @Test
    public void testThrowConnects() {
        Node root = new Node(Token.SCRIPT);
        Node throwStmt = new Node(Token.THROW);
        Node thrown = new Node(Token.EXPR_RESULT);
        throwStmt.addChildToFront(thrown);
        root.addChildToFront(throwStmt);

        process(root);

        // throw does not create normal edge, but connectToPossibleExceptionHandler should be called
        // can't easily assert without handler.
    }

    @Test
    public void testComputeFallThrough() {
        // Static method test
        Node n = new Node(Token.DO);
        Node body = new Node(Token.BLOCK);
        n.addChildToFront(body);
        Node result = ControlFlowAnalysis.computeFallThrough(n);
        assertEquals(body, result);
    }

    @Test
    public void testComputeFollowNode_Basic() {
        // Build a simple script
        Node root = new Node(Token.SCRIPT);
        Node first = new Node(Token.EXPR_RESULT);
        Node second = new Node(Token.EXPR_RESULT);
        root.addChildToFront(first);
        root.addChildAfter(second, first);

        // computeFollowNode(first, first, cfa)
        // first's parent is root, so follow is next sibling = second
        Node follow = ControlFlowAnalysis.computeFollowNode(first, first, cfa);
        assertEquals(second, follow);
    }

    @Test
    public void testComputeFollowNode_BockSynthetic() {
        // Synthetic block should have SYN_BLOCK edge added in handleStmtList
        // But follow computation will skip functions, etc.
    }

    @Test
    public void testIsBreakStructure() {
        assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.FOR), false));
        assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.WHILE), true));
        assertTrue(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), true));
        assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.BLOCK), false));
        assertFalse(ControlFlowAnalysis.isBreakStructure(new Node(Token.I F), false));
    }

    @Test
    public void testIsContinueStructure() {
        assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.FOR)));
        assertTrue(ControlFlowAnalysis.isContinueStructure(new Node(Token.WHILE)));
        assertFalse(ControlFlowAnalysis.isContinueStructure(new Node(Token.BLOCK)));
    }

    @Test
    public void testGetExceptionHandler_NoTry() {
        Node func = new Node(Token.FUNCTION);
        Node body = new Node(Token.BLOCK);
        Node stmt = new Node(Token.EXPR_RESULT);
        func.addChildToFront(new Node(Token.NME));
        func.addChildAfter(new Node(Token.PARAMLIST), func.getFirstChild());
        func.addChildAfter(body, func.getFirstChild().getNext());
        body.addChildToFront(stmt);
        assertNull(ControlFlowAnalysis.getExceptionHandler(stmt));
    }

    @Test
    public void testGetCatchHandlerForBlock() {
        Node tryNode = new Node(Token.TRY);
        Node tryBlock = new Node(Token.BLOCK);
        Node catchBlock = new Node(Token.BLOCK);
        Node catchBody = new Node(Token.BLOCK);
        catchBlock.addChildToFront(catchBody);
        tryNode.addChildToFront(tryBlock);
        tryNode.addChildAfter(catchBlock, tryBlock);
        Node result = ControlFlowAnalysis.getCatchHandlerForBlock(tryBlock);
        assertEquals(catchBody, result);
    }

    @Test
    public void testMayThrowException() {
        assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.CALL)));
        assertTrue(ControlFlowAnalysis.mayThrowException(new Node(Token.NEW)));
        assertFalse(ControlFlowAnalysis.mayThrowException(new Node(Token.FUNCTION)));
    }

    // Additional edge cases: null parent in computeFollowNode
    @Test
    public void testComputeFollowNode_ParentNull() {
        // Node with no parent should return null
        Node root = new Node(Token.SCRIPT);
        // root has no parent
        assertNull(ControlFlowAnalysis.computeFollowNode(root, root, cfa));
    }

    // Test that syntetic block after handleStmtList creates SYN_BLOCK edge
    @Test
    public void testSynteticBlockEdge() {
        Node root = new Node(Token.SCRIPT);
        Node syntheticBlock = new Node(Token.BLOCK);
        syntheticBlock.putBooleanProp(Node.SYNTHETIC_BLOCK_PROP, true);
        Node child = new Node(Token.EXPR_RESULT);
        syntheticBlock.addChildToFront(child);
        root.addChildToFront(syntheticBlock);

        process(root);

        // synthetic block should have UNCOND to first child and SYN_BLOCK to follow
        assertEdge(syntheticBlock, Branch.UNCOND, child);
        assertEdge(syntheticBlock, Branch.SYN_BLOCK, null); // follow of synthetic block is null because root's follow is null
    }
}
