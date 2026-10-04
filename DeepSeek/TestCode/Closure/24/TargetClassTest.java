package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import com.google.javascript.rhino.Token;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ScopedAliasesTest {

    private TestCompiler compiler;
    private TestPreprocessorSymbolTable preprocessorSymbolTable;
    private TestAliasTransformationHandler transformationHandler;
    private ScopedAliases pass;

    @Before
    public void setUp() {
        compiler = new TestCompiler();
        preprocessorSymbolTable = new TestPreprocessorSymbolTable();
        transformationHandler = new TestAliasTransformationHandler();
        pass = new ScopedAliases(compiler, preprocessorSymbolTable, transformationHandler);
    }

    private Node script(Node... children) {
        Node s = new Node(Token.SCRIPT);
        for (Node child : children) {
            s.addChildToBack(child);
        }
        return s;
    }

    private Node exprResult(Node expr) {
        Node er = new Node(Token.EXPR_RESULT);
        er.addChildToBack(expr);
        return er;
    }

    private Node call(Node target, Node... args) {
        Node c = new Node(Token.CALL);
        c.addChildToBack(target);
        for (Node arg : args) {
            c.addChildToBack(arg);
        }
        return c;
    }

    private Node getprop(Node obj, String prop) {
        Node gp = new Node(Token.GETPROP);
        gp.addChildToBack(obj);
        gp.addChildToBack(Node.newString(Token.STRING, prop));
        return gp;
    }

    private Node name(String name) {
        return Node.newString(Token.NAME, name);
    }

    private Node function(boolean hasName, boolean hasParams, Node body) {
        Node func = new Node(Token.FUNCTION);
        if (hasName) {
            func.addChildToBack(Node.newString(Token.NAME, "named"));
        } else {
            func.addChildToBack(new Node(Token.NAME));
        }
        if (hasParams) {
            Node params = new Node(Token.LP);
            params.addChildToBack(name("param"));
            func.addChildToBack(params);
        } else {
            func.addChildToBack(new Node(Token.LP));
        }
        func.addChildToBack(body);
        return func;
    }

    private Node block(Node... stmts) {
        Node b = new Node(Token.BLOCK);
        for (Node stmt : stmts) {
            b.addChildToBack(stmt);
        }
        return b;
    }

    private Node var(String name, Node init) {
        Node v = new Node(Token.VAR);
        Node nameNode = Node.newString(Token.NAME, name);
        if (init != null) {
            nameNode.addChildToBack(init);
        }
        v.addChildToBack(nameNode);
        return v;
    }

    private Node assign(Node lhs, Node rhs) {
        Node a = new Node(Token.ASSIGN);
        a.addChildToBack(lhs);
        a.addChildToBack(rhs);
        return a;
    }

    private Node string(String value) {
        return Node.newString(Token.STRING, value);
    }

    private Node thisNode() {
        return new Node(Token.THIS);
    }

    private Node returnNode(Node value) {
        Node r = new Node(Token.RETURN);
        if (value != null) {
            r.addChildToBack(value);
        }
        return r;
    }

    private Node throwNode(Node value) {
        Node t = new Node(Token.THROW);
        t.addChildToBack(value);
        return t;
    }

    private void setSourcePosition(Node n, int lineno, int charno) {
        n.setLineno(lineno);
        n.setCharno(charno);
    }

    private void process(Node root) {
        pass.process(null, root);
    }

    @Test
    public void testValidAliasReplacement() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(
                                exprResult(assign(name("dom"), getprop(name("goog"), "dom"))),
                                exprResult(call(getprop(name("dom"), "createElement"), string("div")))
                        )
                )
        );
        setSourcePosition(googScopeCall, 1, 0);
        Node root = script(exprResult(googScopeCall));
        process(root);
        Node scriptBody = root.getFirstChild();
        assertTrue(scriptBody.isBlock());
        Node firstStmt = scriptBody.getFirstChild();
        assertTrue(firstStmt.isExprResult());
        Node callNode = firstStmt.getFirstChild();
        assertTrue(callNode.isCall());
        Node callee = callNode.getFirstChild();
        assertTrue(callee.isGetProp());
        assertEquals("createElement", callee.getLastChild().getString());
        Node receiver = callee.getFirstChild();
        assertTrue(receiver.isGetProp());
        assertEquals("dom", receiver.getLastChild().getString());
        assertEquals("goog", receiver.getFirstChild().getString());
        assertTrue(compiler.codeChanged);
    }

    @Test
    public void testMultipleAliases() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(
                                exprResult(assign(name("a"), getprop(name("goog"), "a"))),
                                exprResult(assign(name("b"), getprop(name("goog"), "b"))),
                                exprResult(call(name("a"))),
                                exprResult(call(name("b")))
                        )
                )
        );
        setSourcePosition(googScopeCall, 1, 0);
        Node root = script(exprResult(googScopeCall));
        process(root);
        Node scriptBody = root.getFirstChild();
        Node firstCall = scriptBody.getFirstChild().getFirstChild();
        assertTrue(firstCall.isCall());
        assertEquals("goog", firstCall.getFirstChild().getFirstChild().getString());
        assertEquals("a", firstCall.getFirstChild().getLastChild().getString());
        Node secondCall = scriptBody.getChildAtIndex(1).getFirstChild();
        assertTrue(secondCall.isCall());
        assertEquals("goog", secondCall.getFirstChild().getFirstChild().getString());
        assertEquals("b", secondCall.getFirstChild().getLastChild().getString());
    }

    @Test
    public void testTransitiveAlias() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(
                                exprResult(assign(name("g"), name("goog"))),
                                exprResult(assign(name("d"), getprop(name("g"), "dom"))),
                                exprResult(call(getprop(name("d"), "createElement"), string("div")))
                        )
                )
        );
        setSourcePosition(googScopeCall, 1, 0);
        Node root = script(exprResult(googScopeCall));
        process(root);
        Node scriptBody = root.getFirstChild();
        Node callNode = scriptBody.getFirstChild().getFirstChild();
        assertTrue(callNode.isCall());
        Node callee = callNode.getFirstChild();
        assertTrue(callee.isGetProp());
        assertEquals("createElement", callee.getLastChild().getString());
        Node receiver = callee.getFirstChild();
        assertTrue(receiver.isGetProp());
        assertEquals("dom", receiver.getLastChild().getString());
        assertEquals("goog", receiver.getFirstChild().getString());
    }

    @Test
    public void testAliasNotUsed() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(
                                exprResult(assign(name("dom"), getprop(name("goog"), "dom")))
                        )
                )
        );
        setSourcePosition(googScopeCall, 1, 0);
        Node root = script(exprResult(googScopeCall));
        process(root);
        Node scriptBody = root.getFirstChild();
        assertFalse(scriptBody.hasChildren());
    }

    @Test
    public void testScopeCallNotAlone() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false, block())
        );
        Node blockStmt = block(exprResult(googScopeCall), exprResult(name("x")));
        Node root = script(blockStmt);
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY, compiler.errors.get(0).getType());
    }

    @Test
    public void testScopeCallBadParametersCount() {
        Node googScopeCall = call(getprop(name("goog"), "scope"));
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS, compiler.errors.get(0).getType());
    }

    @Test
    public void testScopeCallNonFunctionParameter() {
        Node googScopeCall = call(getprop(name("goog"), "scope"), name("notAFunction"));
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS, compiler.errors.get(0).getType());
    }

    @Test
    public void testScopeCallNamedFunction() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(true, false, block())
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS, compiler.errors.get(0).getType());
    }

    @Test
    public void testScopeCallFunctionWithParameters() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, true, block())
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS, compiler.errors.get(0).getType());
    }

    @Test
    public void testScopeBodyUsesThis() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(exprResult(thisNode()))
                )
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_REFERENCES_THIS, compiler.errors.get(0).getType());
    }

    @Test
    public void testScopeBodyUsesReturn() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(returnNode(null))
                )
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_USES_RETURN, compiler.errors.get(0).getType());
    }

    @Test
    public void testScopeBodyUsesThrow() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(throwNode(string("error")))
                )
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_USES_THROW, compiler.errors.get(0).getType());
    }

    @Test
    public void testAliasRedefined() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(
                                exprResult(assign(name("dom"), getprop(name("goog"), "dom"))),
                                exprResult(assign(name("dom"), getprop(name("goog"), "dom2")))
                        )
                )
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED, compiler.errors.get(0).getType());
    }

    @Test
    public void testNonAliasLocal() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(
                                exprResult(assign(name("x"), string("not qualified")))
                        )
                )
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(compiler.errors.size() > 0);
        assertEquals(ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL, compiler.errors.get(0).getType());
    }

    @Test
    public void testTypeAnnotationAlias() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false,
                        block(
                                exprResult(assign(name("dom"), getprop(name("goog"), "dom"))),
                                exprResult(name("x"))
                        )
                )
        );
        setSourcePosition(googScopeCall, 1, 0);
        Node root = script(exprResult(googScopeCall));
        Node xNode = root.getFirstChild().getFirstChild().getLastChild().getLastChild().getFirstChild().getFirstChild();
        JSDocInfo info = new JSDocInfo();
        Node typeNode = Node.newString(Token.STRING, "dom.TagName");
        info.setType(typeNode);
        xNode.setJSDocInfo(info);
        process(root);
        assertEquals("goog.dom.TagName", typeNode.getString());
    }

    @Test
    public void testShouldTraverseSkipsNonScopeFunctions() {
        Node globalFunc = function(false, false, block(exprResult(name("x"))));
        Node root = script(exprResult(globalFunc));
        process(root);
        Node scriptBody = root.getFirstChild();
        assertTrue(scriptBody.getFirstChild().isExprResult());
        assertTrue(scriptBody.getFirstChild().getFirstChild().isFunction());
        assertTrue(scriptBody.getFirstChild().getFirstChild().getLastChild().getFirstChild().isExprResult());
    }

    @Test
    public void testGetSourceRegion() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false, block())
        );
        setSourcePosition(googScopeCall, 5, 10);
        Node nextStmt = exprResult(name("x"));
        setSourcePosition(nextStmt, 10, 0);
        Node root = script(exprResult(googScopeCall), nextStmt);
        process(root);
        assertTrue(transformationHandler.lastTransformation != null);
        SourcePosition<AliasTransformation> pos = transformationHandler.lastSourcePosition;
        assertEquals(5, pos.getStartLine());
        assertEquals(10, pos.getStartChar());
        assertEquals(10, pos.getEndLine());
        assertEquals(0, pos.getEndChar());
    }

    @Test
    public void testPreprocessorSymbolTableReference() {
        Node googScopeCall = call(getprop(name("goog"), "scope"),
                function(false, false, block())
        );
        Node root = script(exprResult(googScopeCall));
        process(root);
        assertTrue(preprocessorSymbolTable.references.size() > 0);
        assertEquals("goog", preprocessorSymbolTable.references.get(0).getString());
    }

    private static class TestCompiler extends Compiler {
        List<JSError> errors = new ArrayList<>();
        boolean codeChanged = false;

        @Override
        public void report(JSError error) {
            errors.add(error);
        }

        @Override
        void reportCodeChange() {
            codeChanged = true;
        }
    }

    private static class TestPreprocessorSymbolTable implements PreprocessorSymbolTable {
        List<Node> references = new ArrayList<>();

        @Override
        public void addReference(Node node) {
            references.add(node);
        }
    }

    private static class TestAliasTransformationHandler implements AliasTransformationHandler {
        AliasTransformation lastTransformation;
        SourcePosition<AliasTransformation> lastSourcePosition;

        @Override
        public AliasTransformation logAliasTransformation(String sourceFile, SourcePosition<AliasTransformation> position) {
            lastSourcePosition = position;
            lastTransformation = new TestAliasTransformation();
            return lastTransformation;
        }
    }

    private static class TestAliasTransformation implements AliasTransformation {
        List<String[]> aliases = new ArrayList<>();

        @Override
        public void addAlias(String alias, String definition) {
            aliases.add(new String[]{alias, definition});
        }
    }
}
