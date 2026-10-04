package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

import org.junit.Before;
import org.junit.Test;

public class PeepholeFoldConstantsTest {

    private PeepholeFoldConstants optimizer;
    private Compiler compiler;

    @Before
    public void setUp() {
        compiler = new Compiler(new BasicErrorManager());
        optimizer = new PeepholeFoldConstants(false);
        optimizer.setCompiler(compiler);
    }

    private static class BasicErrorManager implements ErrorManager {
        @Override
        public void report(CheckLevel level, JSError error) {
        }
        @Override
        public void generateReport() {
        }
        @Override
        public int getErrorCount() {
            return 0;
        }
        @Override
        public int getWarningCount() {
            return 0;
        }
        @Override
        public JSError[] getErrors() {
            return new JSError[0];
        }
        @Override
        public JSError[] getWarnings() {
            return new JSError[0];
        }
        @Override
        public void setTypedPercent(double typedPercent) {
        }
        @Override
        public double getTypedPercent() {
            return 0;
        }
    }

    @Test
    public void testOptimizeSubtreeNew() {
        Node newExpr = IR.newNode(IR.name("String"), IR.string("test"));
        Node parent = IR.exprResult(newExpr);
        Node result = optimizer.optimizeSubtree(newExpr);
        assertNotNull(result);
    }

    @Test
    public void testOptimizeSubtreeTypeof() {
        Node typeof = new Node(Token.TYPEOF, IR.string("hello"));
        Node parent = IR.exprResult(typeof);
        Node result = optimizer.optimizeSubtree(typeof);
        assertEquals(Token.STRING, result.getType());
        assertEquals("string", result.getString());
    }

    @Test
    public void testOptimizeSubtreeNot() {
        Node not = new Node(Token.NOT, IR.trueNode());
        Node parent = IR.exprResult(not);
        Node result = optimizer.optimizeSubtree(not);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test
    public void testOptimizeSubtreePos() {
        Node pos = new Node(Token.POS, IR.number(5));
        Node parent = IR.exprResult(pos);
        Node result = optimizer.optimizeSubtree(pos);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(5.0, result.getDouble(), 0.0);
    }

    @Test
    public void testOptimizeSubtreeNeg() {
        Node neg = new Node(Token.NEG, IR.number(5));
        Node parent = IR.exprResult(neg);
        Node result = optimizer.optimizeSubtree(neg);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-5.0, result.getDouble(), 0.0);
    }

    @Test
    public void testOptimizeSubtreeBitnot() {
        Node bitnot = new Node(Token.BITNOT, IR.number(5));
        Node parent = IR.exprResult(bitnot);
        Node result = optimizer.optimizeSubtree(bitnot);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(~5, (int) result.getDouble());
    }

    @Test
    public void testOptimizeSubtreeVoid() {
        Node voidNode = new Node(Token.VOID, IR.number(0));
        Node parent = IR.exprResult(voidNode);
        Node result = optimizer.optimizeSubtree(voidNode);
        assertEquals(Token.VOID, result.getType());
    }

    @Test
    public void testTryFoldBinaryOperatorGetProp() {
        Node arrayLit = IR.arraylit(IR.number(1), IR.number(2));
        Node getProp = IR.getprop(arrayLit, IR.string("length"));
        Node parent = IR.exprResult(getProp);
        Node result = optimizer.optimizeSubtree(getProp);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldBinaryOperatorGetElem() {
        Node arrayLit = IR.arraylit(IR.string("a"), IR.string("b"));
        Node getElem = IR.getelem(arrayLit, IR.number(0));
        Node parent = IR.exprResult(getElem);
        Node result = optimizer.optimizeSubtree(getElem);
        assertEquals(Token.STRING, result.getType());
        assertEquals("a", result.getString());
    }

    @Test
    public void testTryFoldBinaryOperatorInstanceof() {
        Node obj = IR.objectlit();
        Node instanceofNode = new Node(Token.INSTANCEOF, obj, IR.name("Object"));
        Node parent = IR.exprResult(instanceofNode);
        Node result = optimizer.optimizeSubtree(instanceofNode);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testTryFoldBinaryOperatorAndOr() {
        Node and = new Node(Token.AND, IR.trueNode(), IR.falseNode());
        Node parent = IR.exprResult(and);
        Node result = optimizer.optimizeSubtree(and);
        assertEquals(Token.FALSE, result.getType());
    }

    @Test
    public void testTryFoldBinaryOperatorShift() {
        Node lsh = new Node(Token.LSH, IR.number(1), IR.number(2));
        Node parent = IR.exprResult(lsh);
        Node result = optimizer.optimizeSubtree(lsh);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(4.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldBinaryOperatorAssign() {
        optimizer = new PeepholeFoldConstants(true);
        optimizer.setCompiler(compiler);
        Node name = IR.name("x");
        Node add = new Node(Token.ADD, name.cloneNode(), IR.number(1));
        Node assign = IR.assign(name, add);
        Node parent = IR.exprResult(assign);
        Node result = optimizer.optimizeSubtree(assign);
        assertEquals(Token.ASSIGN_ADD, result.getType());
    }

    @Test
    public void testTryFoldBinaryOperatorAssignOp() {
        Node name = IR.name("x");
        Node assignAdd = new Node(Token.ASSIGN_ADD, name.cloneNode(), IR.number(1));
        Node parent = IR.exprResult(assignAdd);
        Node result = optimizer.optimizeSubtree(assignAdd);
        assertEquals(Token.ASSIGN, result.getType());
    }

    @Test
    public void testTryFoldBinaryOperatorAdd() {
        Node add = new Node(Token.ADD, IR.number(1), IR.number(2));
        Node parent = IR.exprResult(add);
        Node result = optimizer.optimizeSubtree(add);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(3.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldBinaryOperatorArithmetic() {
        Node sub = new Node(Token.SUB, IR.number(5), IR.number(3));
        Node parent = IR.exprResult(sub);
        Node result = optimizer.optimizeSubtree(sub);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldBinaryOperatorComparison() {
        Node lt = new Node(Token.LT, IR.number(1), IR.number(2));
        Node parent = IR.exprResult(lt);
        Node result = optimizer.optimizeSubtree(lt);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testTryReduceVoid() {
        Node voidNode = new Node(Token.VOID, IR.number(5));
        Node parent = IR.exprResult(voidNode);
        Node result = optimizer.optimizeSubtree(voidNode);
        assertEquals(Token.VOID, result.getType());
        assertEquals(0.0, result.getFirstChild().getDouble(), 0.0);
    }

    @Test
    public void testTryReduceOperandsForOp() {
        Node add = new Node(Token.ADD, IR.string("a"), IR.string("b"));
        Node parent = IR.exprResult(add);
        optimizer.optimizeSubtree(add);
    }

    @Test
    public void testTryConvertOperandsToNumber() {
        Node add = new Node(Token.ADD, IR.name("undefined"), IR.number(1));
        Node parent = IR.exprResult(add);
        optimizer.optimizeSubtree(add);
    }

    @Test
    public void testTryFoldTypeof() {
        Node typeof = new Node(Token.TYPEOF, IR.number(5));
        Node parent = IR.exprResult(typeof);
        Node result = optimizer.optimizeSubtree(typeof);
        assertEquals("number", result.getString());
    }

    @Test
    public void testTryFoldUnaryOperatorNot() {
        Node not = new Node(Token.NOT, IR.falseNode());
        Node parent = IR.exprResult(not);
        Node result = optimizer.optimizeSubtree(not);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testTryFoldUnaryOperatorPos() {
        Node pos = new Node(Token.POS, IR.number(10));
        Node parent = IR.exprResult(pos);
        Node result = optimizer.optimizeSubtree(pos);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(10.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldUnaryOperatorNeg() {
        Node neg = new Node(Token.NEG, IR.number(10));
        Node parent = IR.exprResult(neg);
        Node result = optimizer.optimizeSubtree(neg);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(-10.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldUnaryOperatorBitnot() {
        Node bitnot = new Node(Token.BITNOT, IR.number(10));
        Node parent = IR.exprResult(bitnot);
        Node result = optimizer.optimizeSubtree(bitnot);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(~10, (int) result.getDouble());
    }

    @Test
    public void testTryFoldInstanceof() {
        Node obj = IR.objectlit();
        Node instanceofNode = new Node(Token.INSTANCEOF, obj, IR.name("Object"));
        Node parent = IR.exprResult(instanceofNode);
        Node result = optimizer.optimizeSubtree(instanceofNode);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testTryFoldAssign() {
        optimizer = new PeepholeFoldConstants(true);
        optimizer.setCompiler(compiler);
        Node name = IR.name("x");
        Node add = new Node(Token.ADD, name.cloneNode(), IR.number(1));
        Node assign = IR.assign(name, add);
        Node parent = IR.exprResult(assign);
        Node result = optimizer.optimizeSubtree(assign);
        assertEquals(Token.ASSIGN_ADD, result.getType());
    }

    @Test
    public void testTryUnfoldAssignOp() {
        Node name = IR.name("x");
        Node assignAdd = new Node(Token.ASSIGN_ADD, name.cloneNode(), IR.number(1));
        Node parent = IR.exprResult(assignAdd);
        Node result = optimizer.optimizeSubtree(assignAdd);
        assertEquals(Token.ASSIGN, result.getType());
    }

    @Test
    public void testTryFoldAndOr() {
        Node or = new Node(Token.OR, IR.trueNode(), IR.falseNode());
        Node parent = IR.exprResult(or);
        Node result = optimizer.optimizeSubtree(or);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testTryFoldChildAddString() {
        Node left = new Node(Token.ADD, IR.string("a"), IR.string("b"));
        Node right = IR.string("c");
        Node add = new Node(Token.ADD, left, right);
        Node parent = IR.exprResult(add);
        Node result = optimizer.optimizeSubtree(add);
        assertEquals(Token.ADD, result.getType());
        assertEquals("abc", result.getLastChild().getString());
    }

    @Test
    public void testTryFoldAddConstantString() {
        Node add = new Node(Token.ADD, IR.string("a"), IR.string("b"));
        Node parent = IR.exprResult(add);
        Node result = optimizer.optimizeSubtree(add);
        assertEquals(Token.STRING, result.getType());
        assertEquals("ab", result.getString());
    }

    @Test
    public void testTryFoldArithmeticOp() {
        Node mul = new Node(Token.MUL, IR.number(2), IR.number(3));
        Node parent = IR.exprResult(mul);
        Node result = optimizer.optimizeSubtree(mul);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(6.0, result.getDouble(), 0.0);
    }

    @Test
    public void testPerformArithmeticOp() {
        Node add = new Node(Token.ADD, IR.number(1), IR.number(2));
        Node parent = IR.exprResult(add);
        Node result = optimizer.optimizeSubtree(add);
        assertEquals(3.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldLeftChildOp() {
        Node left = new Node(Token.MUL, IR.number(2), IR.number(3));
        Node right = IR.number(4);
        Node mul = new Node(Token.MUL, left, right);
        Node parent = IR.exprResult(mul);
        Node result = optimizer.optimizeSubtree(mul);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(24.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldAdd() {
        Node add = new Node(Token.ADD, IR.number(1), IR.number(2));
        Node parent = IR.exprResult(add);
        Node result = optimizer.optimizeSubtree(add);
        assertEquals(3.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldShift() {
        Node lsh = new Node(Token.LSH, IR.number(1), IR.number(2));
        Node parent = IR.exprResult(lsh);
        Node result = optimizer.optimizeSubtree(lsh);
        assertEquals(4.0, result.getDouble(), 0.0);
    }

    @Test
    public void testTryFoldComparison() {
        Node eq = new Node(Token.EQ, IR.number(1), IR.number(1));
        Node parent = IR.exprResult(eq);
        Node result = optimizer.optimizeSubtree(eq);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testAreStringsEqual() {
        Node eq = new Node(Token.EQ, IR.string("a"), IR.string("a"));
        Node parent = IR.exprResult(eq);
        Node result = optimizer.optimizeSubtree(eq);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testGetNormalizedNodeType() {
        Node not = new Node(Token.NOT, IR.trueNode());
        Node parent = IR.exprResult(not);
        optimizer.optimizeSubtree(not);
    }

    @Test
    public void testCompareAsNumbers() {
        Node lt = new Node(Token.LT, IR.number(1), IR.number(2));
        Node parent = IR.exprResult(lt);
        Node result = optimizer.optimizeSubtree(lt);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testCompareToUndefined() {
        Node eq = new Node(Token.EQ, IR.name("undefined"), IR.name("undefined"));
        Node parent = IR.exprResult(eq);
        Node result = optimizer.optimizeSubtree(eq);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testIsEqualityOp() {
        Node eq = new Node(Token.EQ, IR.number(1), IR.number(1));
        Node parent = IR.exprResult(eq);
        optimizer.optimizeSubtree(eq);
    }

    @Test
    public void testCompareToNull() {
        Node eq = new Node(Token.EQ, IR.nullNode(), IR.nullNode());
        Node parent = IR.exprResult(eq);
        Node result = optimizer.optimizeSubtree(eq);
        assertEquals(Token.TRUE, result.getType());
    }

    @Test
    public void testTryFoldCtorCall() {
        Node newExpr = IR.newNode(IR.name("String"), IR.string("test"));
        Node getElem = IR.getelem(IR.name("obj"), newExpr);
        Node parent = IR.exprResult(getElem);
        optimizer.optimizeSubtree(getElem);
    }

    @Test
    public void testInForcedStringContext() {
        Node newExpr = IR.newNode(IR.name("String"), IR.string("test"));
        Node add = new Node(Token.ADD, IR.string(""), newExpr);
        Node parent = IR.exprResult(add);
        optimizer.optimizeSubtree(add);
    }

    @Test
    public void testTryFoldInForcedStringContext() {
        Node newExpr = IR.newNode(IR.name("String"), IR.string("test"));
        Node getElem = IR.getelem(IR.name("obj"), newExpr);
        Node parent = IR.exprResult(getElem);
        optimizer.optimizeSubtree(getElem);
    }

    @Test
    public void testTryFoldGetElem() {
        Node arrayLit = IR.arraylit(IR.string("a"), IR.string("b"));
        Node getElem = IR.getelem(arrayLit, IR.number(0));
        Node parent = IR.exprResult(getElem);
        Node result = optimizer.optimizeSubtree(getElem);
        assertEquals("a", result.getString());
    }

    @Test
    public void testTryFoldGetProp() {
        Node arrayLit = IR.arraylit(IR.number(1), IR.number(2));
        Node getProp = IR.getprop(arrayLit, IR.string("length"));
        Node parent = IR.exprResult(getProp);
        Node result = optimizer.optimizeSubtree(getProp);
        assertEquals(2.0, result.getDouble(), 0.0);
    }

    @Test
    public void testIsAssignmentTarget() {
        Node name = IR.name("x");
        Node assign = IR.assign(name, IR.number(1));
        Node parent = IR.exprResult(assign);
        optimizer.optimizeSubtree(assign);
    }

    @Test
    public void testTryFoldArrayAccess() {
        Node arrayLit = IR.arraylit(IR.string("a"), IR.string("b"));
        Node getElem = IR.getelem(arrayLit, IR.number(0));
        Node parent = IR.exprResult(getElem);
        Node result = optimizer.optimizeSubtree(getElem);
        assertEquals("a", result.getString());
    }

    @Test
    public void testTryFoldObjectPropAccess() {
        Node objLit = IR.objectlit(IR.stringKey("a", IR.number(1)));
        Node getProp = IR.getprop(objLit, IR.string("a"));
        Node parent = IR.exprResult(getProp);
        Node result = optimizer.optimizeSubtree(getProp);
        assertEquals(Token.NUMBER, result.getType());
        assertEquals(1.0, result.getDouble(), 0.0);
    }
}
