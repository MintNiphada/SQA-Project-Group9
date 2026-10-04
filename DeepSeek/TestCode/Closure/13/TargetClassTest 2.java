package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.javascript.rhino.Node;

import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class PeepholeOptimizationsPassTest {

    @Mock
    private AbstractCompiler compiler;

    @Mock
    private AbstractPeepholeOptimization optimization1;

    @Mock
    private AbstractPeepholeOptimization optimization2;

    private PeepholeOptimizationsPass pass;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        pass = new PeepholeOptimizationsPass(compiler, optimization1, optimization2);
    }

    @Test
    public void testConstructor_setsFields() {
        assertSame(compiler, pass.getCompiler());
        // access package-private field peepholeOptimizations
        AbstractPeepholeOptimization[] optimizations = pass.peepholeOptimizations;
        assertEquals(2, optimizations.length);
        assertSame(optimization1, optimizations[0]);
        assertSame(optimization2, optimizations[1]);
    }

    @Test
    public void testGetCompiler() {
        assertEquals(compiler, pass.getCompiler());
    }

    @Test
    public void testProcess_addsAndRemovesHandlerAndCallsTraversalMethods() {
        Node externs = mock(Node.class);
        Node root = mock(Node.class);

        // Stub to avoid infinite traversal
        when(root.isFunction()).thenReturn(false);
        when(root.isScript()).thenReturn(false);
        when(root.getFirstChild()).thenReturn(null);

        pass.process(externs, root);

        verify(compiler).addChangeHandler(any(PeepholeChangeHandler.class));
        verify(optimization1).beginTraversal(compiler);
        verify(optimization2).beginTraversal(compiler);
        verify(optimization1).endTraversal(compiler);
        verify(optimization2).endTraversal(compiler);
        verify(compiler).removeChangeHandler(any(PeepholeChangeHandler.class));
    }

    @Test
    public void testVisit_noChange_singleOptimization() {
        Node node = mock(Node.class);
        when(optimization1.optimizeSubtree(node)).thenReturn(node);

        pass.visit(node);

        verify(optimization1).optimizeSubtree(node);
        verify(optimization2, never()).optimizeSubtree(any(Node.class));
    }

    @Test
    public void testVisit_oneChange_thenStabilize() {
        Node original = mock(Node.class);
        Node changed = mock(Node.class);

        // first call returns changed, second call returns same changed to stabilie
        when(optimization1.optimizeSubtree(original)).thenReturn(changed);
        when(optimization1.optimizeSubtree(changed)).thenReturn(changed);
        when(optimization2.optimizeSubtree(changed)).thenReturn(changed);

        pass.visit(original);

        verify(optimization1).optimizeSubtree(original);
        verify(optimization1).optimizeSubtree(changed);
        verify(optimization2).optimizeSubtree(changed);
    }

    @Test
    public void testVisit_multipleOptimizationsCaanges() {
        Node n0 = mock(Node.class);
        Node n1 = mock(Node.class);
        Node n2 = mock(Node.class);
        Node n3 = mock(Node.class);

        when(optimization1.optimizeSubtree(n0)).thenReturn(n1);
        when(optimization2.optimizeSubtree(n1)).thenReturn(n2);
        when(optimization1.optimizeSubtree(n2)).thenReturn(n3);
        when(optimization2.optimizeSubtree(n3)).thenReturn(n3);
        when(optimization1.optimizeSubtree(n3)).thenReturn(n3);

        pass.visit(n0);

        verify(optimization1).optimizeSubtree(n0);
        verify(optimization2).optimizeSubtree(n1);
        verify(optimization1).optimizeSubtree(n2);
        verify(optimization2).optimizeSubtree(n3);
        verify(optimization1).optimizeSubtree(n3);
    }

    @Test
    public void testVisit_nodeBecomesNull_returnsImmediately() {
        Node node = mock(Node.class);
        when(optimization1.optimizeSubtree(node)).thenReturn(null);

        pass.visit(node);

        // after returning null, the loop breaks; verify no further calls
        verify(optimization1).optimizeSubtree(node);
        verify(optimization2, never()).optimizeSubtree(any(Node.class));
    }

    @Test
    public void testTrverse_simpleTree() {
        Node root = mock(Node.class);
        Node child1 = mock(Node.class);
        Node child2 = mock(Node.class);

        when(root.isScript()).thenReturn(true);
        when(root.isFunction()).thenReturn(false);
        when(root.getFirstChild()).thenReturn(child1);
        when(child1.getNext()).thenReturn(child2);
        when(child2.getNext()).thenReturn(null);
        when(child1.isFunction()).thenReturn(false);
        when(child1.isScript()).thenReturn(false);
        when(child2.isFunction()).thenReturn(false);
        when(child2.isScript()).thenReturn(false);
        // no children for child nodes
        when(child1.getFirstChild()).thenReturn(null);
        when(child2.getFirstChild()).thenReturn(null);

        // stub optimizations to return same nodes (no change) to avoi infinite loops
        when(optimization1.optimizeSubtree(any(Node.class))).thenAnswer(new Answer<Node>() {
            public Node answer(InvocationOnMock inv) {
                return (Node) inv.getArguments()[0];
            }
        });
        when(optimization2.optimizeSubtree(any(Node.class))).thenAnswer(new Answer<Node>() {
            public Node answer(InvocationOnMock inv) {
                return (Node) inv.getArguments()[0];
            }
    });

        pass.traverse(root);

        // verify that visit was called for each node
        verify(optimization1).optimizeSubtree(root);
        verify(optimization1).optimizeSubtree(child1);
        verify(optimization1).optimizeSubtree(child2);
    }

    @Test
    public void testShouldVisit_functionPushesStateAndDependsOnTrverseChildScopes() {
        // Access package-private method shouldVisit
        Node funcNode = mock(Node.class);
        when(funcNode.isFunction()).thenReturn(true);
        when(funcNode.isScript()).thenReturn(false);

        // Initially, traversal state has traverseChildScopes = true, so shouldVisit returns true and pushes
        boolean result = pass.shouldVisit(funcNode);
        assertTrue(result);
        // After push, peek state should have reset values (changed=false, traverseChildScopes=true)
        PeepholeOptimizationsPass.ScopeState state = pass.traversalState.peek();
        assertFalse(state.changed);
        assertTrue(state.traverseChildScopes);
        // Pop to clean up
        pass.exitNode(funcNode);

        // Now set current state traverseChildScopes to false and test again
        // We create a new scenario: we push a state manually, set traverseChildScopes=false, then shouldVisit should return false
        PeepholeOptimizationsPass newPass = new PeepholeOptimizationsPass(compiler, optimization1);
        // By default, traversalState has one state with traverseChildScopes=true.
        // We need to change top state traverseChildScopes to false.
        newPass.traversalState.peek().traverseChildScopes = false;
        result = newPass.shouldVisit(funcNode);
        assertFalse(result);
    }

    @Test
    public void testShouldVisit_nonFunction_nonScript_returnsTrueWithoutPush() {
        Node node = mock(Node.class);
        when(node.isFunction()).thenReturn(false);
        when(node.isScript()).thenReturn(false);

        int initialDepth = pass.traversalState.currentDepth;
        boolean result = pass.shouldVisit(node);
        assertTrue(result);
        // depth should not have changed
        assertEquals(initialDepth, pass.traversalState.currentDepth);
    }

    @Test
    public void testExitNode_functionOrScript_popsState() {
        PeepholeOptimizationsPass p = new PeepholeOptimizationsPass(compiler, optimization1);
        // push a state manually
        p.traversalState.push();
        int depthBefore = p.traversalState.currentDepth;
        Node scriptNode = mock(Node.class);
        when(scriptNode.isScript()).thenReturn(true);
        when(scriptNode.isFunction()).thenReturn(false);
        p.exitNode(scriptNode);
        assertEquals(depthBefore - 1, p.traversalState.currentDepth);
    }

    @Test
    public void testExitNode_nonFunction_nonScript_doesNothing() {
        int depthBefore = pass.traversalState.currentDepth;
        Node node = mock(Node.class);
        when(node.isFunction()).thenReturn(false);
        when(node.isScript()).thenReturn(false);
        pass.exitNode(node);
        assertEquals(depthBefore, pass.traversalState.currentDepth);
    }

    @Test
    public void testShouldRetraverse_functionNode_changedTrue_retsetsAndReturnsTrue() {
        Node functionNode = mock(Node.class);
        when(functionNode.isFunction()).thenReturn(true);
        when(functionNode.isScript()).thenReturn(false);
        when(functionNode.getParent()).thenReturn(mock(Node.class)); // non-null parent

        // Set current state changed = true
        pass.traversalState.peek().changed = true;
        boolean retraverse = pass.shouldRetraverse(functionNode);
        assertTrue(retraverse);
        // Verify state resets
        PeepholeOptimizationsPass.ScopeState state = pass.traversalState.peek();
        assertFalse(state.changed);
        assertFalse(state.traverseChildScopes);
    }

    @Test
    public void testShouldRetraverse_scriptNode_changedTrue_works() {
        Node scriptNode = mock(Node.class);
        when(scriptNode.isScript()).thenReturn(true);
        when(scriptNode.isFunction()).thenReturn(false);
        when(scriptNode.getParent()).thenReturn(mock(Node.class)); // parent non-null

        pass.traversalState.peek().changed = true;
        boolean retraverse = pass.shouldRetraverse(scriptNode);
        assertTrue(retraverse);
        PeepholeOptimizationsPass.ScopeState state = pass.traversalState.peek();
        assertFalse(state.changed);
        assertFalse(state.traverseChildScopes);
    }

    @Test
    public void testShouldRetraverse_whenParentNull_returnsFalse() {
        Node functionNode = mock(Node.class);
        when(functionNode.isFunction()).thenReturn(true);
        when(functionNode.getParent()).thenReturn(null);
        pass.traversalState.peek().changed = true;
        assertFalse(pass.shouldRetraverse(functionNode));
    }

    @Test
    public void testShouldRetraverse_whenNotFunctionOrScript_returnsFalse() {
        Node node = mock(Node.class);
        when(node.isFunction()).thenReturn(false);
        when(node.isScript()).thenReturn(false);
        when(node.getParent()).thenReturn(mock(Node.class));
        pass.traversalState.peek().changed = true;
        assertFalse(pass.shouldRetraverse(node));
    }

    @Test
    public void testShouldRetraverse_whenChangedFalse_returnsFalse() {
        Node functionNode = mock(Node.class);
        when(functionNode.isFunction()).thenReturn(true);
        when(functionNode.getParent()).thenReturn(mock(Node.class));
        pass.traversalState.peek().changed = false;
        assertFalse(pass.shouldRetraverse(functionNode));
    }

    @Test(expected = IllegalStateException.class)
    public void testTrverse_tooManyIterationsThrows() {
        // Setup a function node that will keep causing retraversal by making visit always change something
        // and the changeHandler reports change each time
        final Node funcNode = mock(Node.class);
        when(funcNode.isFunction()).thenReturn(true);
        when(funcNode.isScript()).thenReturn(false);
        when(funcNode.getParent()).thenReturn(mock(Node.class)); // non-null parent
        when(funcNode.getFirstChild()).thenReturn(null);

        // Capture the changeHandler when process is not used, we need to simulate changeHandler reporting change.
        // The traversal uses compiler.addChangeHandler to get the handler, but we are calling traverse directly.
        // To make shouldRetraverse see changed=true, we need to set state.changed = true after each visit.
        // However, shouldRetraverse resets changed to false each time.
        // To cause many retraversals, we will override shouldRetraverse to always return true and not reset changed?
        // Actually, we can't modify private methods. Better to go through process, which adds a changeHandler that we can trigger.
        // Let's use process: add a real changeHandler, and we can capture it using a spy compiler or capturing the handler.
        // We'll use doAnswer to capture handler.
        final PeepholeChangeHandler[] capturedHandler = new PeepholeChangeHandler[1];
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock inv) {
                capturedHandler[0] = (PeepholeChangeHandler) inv.getArguments()[0];
                return null;
            }
        }).when(compiler).addChangeHandler(any(PeepholeChangeHandler.class));

        // We'll need to stub optimizations to always return a new node to force somethingChanged=true in visit.
        // But that alone won't cause retraversal unless the changeHandler.reportChange is called (which sets state.changed).
        // We'll make optimization1.optimizeSubtree return a different node and also call handler.reportChange.
        when(optimization1.optimizeSubtree(any(Node.class))).thenAnswer(new Answer<Node>() {
            public Node answer(InvocationOnMock inv) throws Throwable {
                if (capturedHandler[0] != null) {
                    capturedHandler[0].reportChange();
                }
                // Return a new mock node each time to keep somethingChanged true
                return mock(Node.class);
            }
        });
        when(optimization2.optimizeSubtree(any(Node.class))).thenAnswer(new Answer<Node>() {
            public Node answer(InvocationOnMock inv) {
                return inv.getArguments()[0];
            }
    });

        // Call process on a root that is a function node to trigger traversal of that function.
        Node root = funcNode; // This is the function node itself
        // Actually process expects externs and root. We'll pass root as root, but we need to ensure that root is visited.
        // The traverse method first checks shouldVisit; for function, it pushes, then proceeds.
        // We'll stub root.isScript()? Since root is function, isScript=false. That's fine.
        // But the traverse method as implemented: when traversing a node, it first visits children, then visit(node). So if we want the function node to be visited, it will be visited after its children (none). So it's fine.
        // We'll call process using this root. However, process calls traverse(root) directly without externs involved? The code: process(Node externs, Node root) { ... traverse(root); } So root will be traversed.
        pass.process(mock(Node.class), root);

        // The loop in traverse should eventually throw because visits counter exceeds 10000.
        // The exception is IllegalStateException from Preconditions.checkState.
        // This test expects that exception.
    }

    @Test
    public void testStateStack_push_peek_pop() {
        PeepholeOptimizationsPass.StateStack stack = new PeepholeOptimizationsPass.StateStack();
        PeepholeOptimizationsPass.ScopeState initial = stack.peek();
        // push
        stack.push();
        PeepholeOptimizationsPass.ScopeState top = stack.peek();
        assertNotSame(initial, top);
        // state should be resetted
        assertFalse(top.changed);
        assertTrue(top.traverseChildScopes);
        // modify
        top.changed = true;
        top.traverseChildScopes = false;
        // push again
        stack.push();
        PeepholeOptimizationsPass.ScopeState newTop = stack.peek();
        assertFalse(newTop.changed);
        assertTrue(newTop.traverseChildScopes);
        stack.pop();
        // back to previous state that had changed values
        PeepholeOptimizationsPass.ScopeState previous = stack.peek();
        assertTrue(previous.changed);
        assertFalse(previous.traverseChildScopes);
        stack.pop();
        assertEquals(0, stack.currentDepth);
    }

    @Test
    public void testStateStack_resetOnReuse() {
        PeepholeOptimizationsPass.StateStack stack = new PeepholeOptimizationsPass.StateStack();
        stack.push();
        PeepholeOptimizationsPass.ScopeState s1 = stack.peek();
        s1.changed = true;
        s1.traverseChildScopes = false;
        stack.pop();
        // now depth 0
        stack.push(); // should reuse the index 1 state, but reset it
        PeepholeOptimizationsPass.ScopeState s2 = stack.peek();
        assertNotSame(s1, s2); // because it resets the existing instance at that index
        assertFalse(s2.changed);
        assertTrue(s2.traverseChildScopes);
    }

    @Test
    public void testScopeState_reset() {
        PeepholeOptimizationsPass.ScopeState state = new PeepholeOptimizationsPass.ScopeState();
        state.changed = true;
        state.traverseChildScopes = false;
        state.reset();
        assertFalse(state.changed);
        assertTrue(state.traverseChildScopes);
    }

    @Test
    public void testPeepholeChangeHandler_reportChange_setChangedTrue() {
        // The handler uses traversalState.peek().changed = true.
        // We'll instantiate handler (it's an inner class, accessible via pass.new PeepholeChangeHandler())
        // But pass must be created. We'll use the existing pass.
        // Before calling, ensure traversal state is at a state we can check.
        PeepholeOptimizationsPass.PeepholeChangeHandler handler = pass.new PeepholeChangeHandler();
        PeepholeOptimizationsPass.ScopeState currentState = pass.traversalState.peek();
        currentState.changed = false;
        handler.reportChange();
        assertTrue(currentState.changed);
    }
}
