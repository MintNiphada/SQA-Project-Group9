package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.*;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeCheckTest {
    private AbstractCompiler compiler;
    private ReverseAbstractInterpreter reverseInterpreter;
    private JSTypeRegistry registry;
    private TypeValidator validator;
    private TypeCheck typeCheck;
    private Scope topScope;
    private ScopeCreator scopeCreator;
    private CheckLevel reportMissingOverride = CheckLevel.WARNING;
    private CheckLevel reportUnknownTypes = CheckLevel.WARNING;

    @Before
    public void setUp() {
        compiler = mock(AbstractCompiler.class);
        reverseInterpreter = mock(ReverseAbstractInterpreter.class);
        validator = mock(TypeValidator.class);
        when(compiler.getTypeValidator()).thenReturn(validator);
        registry = new JSTypeRegistry(mock(com.google.javascript.rhino.ErrorReporter.class), new JSTypeRegistry(null));

        topScope = mock(Scope.class);
        scopeCreator = mock(ScopeCreator.class);
        when(scopeCreator.createScope(any(Node.class), any(Scope.class))).thenReturn(topScope);

        typeCheck = new TypeCheck(compiler, reverseInterpreter, registry,
                topScope, scopeCreator, reportMissingOverride, reportUnknownTypes);
    }

    private Node createNode(int type, Node... children) {
        Node node = new Node(type);
        for (Node child : children) {
            node.addChildToBack(child);
        }
        return node;
    }

    private Node createName(String name) {
        Node node = Node.newString(Token.NAME, name);
        return node;
    }

    private Node createString(String str) {
        return Node.newString(str);
    }

    private Node createNumber(double num) {
        return Node.newNumber(num);
    }

    // Helper to set a private field
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    public void testVisitName_functionParent_returnsFalseTypeable() throws Exception {
        Node function = createNode(Token.FUNCTION);
        Node name = createName("myFunc");
        function.addChildToBack(name);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        boolean typeable = typeCheck.visitName(t, name, function);
        assertFalse(typeable);
    }

    @Test
    public void testVisitName_catchParent_returnsFalseTypeable() throws Exception {
        Node catchNode = createNode(Token.CATCH);
        Node name = createName("e");
        catchNode.addChildToBack(name);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        boolean typeable = typeCheck.visitName(t, name, catchNode);
        assertFalse(typeable);
    }

    @Test
    public void testVisitName_varParent_returnsFalseTypeable() throws Exception {
        Node varNode = createNode(Token.VAR);
        Node name = createName("x");
        varNode.addChildToBack(name);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        boolean typeable = typeCheck.visitName(t, name, varNode);
        assertFalse(typeable);
    }

    @Test
    public void testVisitName_normalParent_setsTypeAndReturnsTrue() throws Exception {
        Node parent = createNode(Token.EXPR_RESULT);
        Node name = createName("a");
        parent.addChildToBack(name);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        when(topScope.getVar("a")).thenReturn(null);
        boolean typeable = typeCheck.visitName(t, name, parent);
        assertTrue(typeable);
        assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), name.getJSType());
    }

    @Test
    public void testVisitName_withVarType_setsVarType() throws Exception {
        Node parent = createNode(Token.EXPR_RESULT);
        Node name = createName("b");
        parent.addChildToBack(name);
        Var var = mock(Var.class);
        when(var.getType()).thenReturn(registry.getNativeType(JSTypeNative.STRING_TYPE));
        when(topScope.getVar("b")).thenReturn(var);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        boolean typeable = typeCheck.visitName(t, name, parent);
        assertTrue(typeable);
        assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), name.getJSType());
    }

    @Test
    public void testVisitGetProp_validObject_propertyCheckCalled() {
        Node obj = createName("obj");
        Node prop = createString("length");
        Node getProp = createNode(Token.GETPROP, obj, prop);
        Node parent = createNode(Token.EXPR_RESULT, getProp);
        NodeTraversal t = mock(NodeTraversal.class);
        obj.setJSType(registry.getNativeType(JSTypeNative.OBJECT_TYPE));
        getProp.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        when(t.getScope()).thenReturn(topScope);
        typeCheck.visit(t, getProp, parent);
        // expectation that no exception; ensureTyped is called.
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), getProp.getJSType());
    }

    @Test
    public void testVisitGetProp_dictObject_illegalAccessReported() {
        Node obj = createName("dict");
        Node prop = createString("key");
        Node getProp = createNode(Token.GETPROP, obj, prop);
        Node parent = createNode(Token.EXPR_RESULT, getProp);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        obj.setJSType(registry.createObjectType("{...}").toMaybeObjectType());
        // Make it a dict type; assume registry has a way; here we mock isDict
        JSType dictType = mock(ObjectType.class);
        when(dictType.isDict()).thenReturn(true);
        obj.setJSType(dictType);
        typeCheck.visit(t, getProp, parent);
        verify(validator, times(1)).report(any(NodeTraversal.class), any(Node.class),
                eq(TypeValidator.ILLEGAL_PROPERTY_ACCESS), anyString(), anyString());
    }

    @Test
    public void testVisitGetProp_nullOrUndefined_validateCalled() {
        Node obj = createName("obj");
        Node prop = createString("x");
        Node getProp = createNode(Token.GETPROP, obj, prop);
        Node parent = createNode(Token.EXPR_RESULT, getProp);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        obj.setJSType(registry.getNativeType(JSTypeNative.NULL_TYPE));
        getProp.setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        when(validator.expectNotNullOrUndefined(any(NodeTraversal.class), any(Node.class),
                any(JSType.class), anyString(), any(JSType.class))).thenReturn(true);
        typeCheck.visit(t, getProp, parent);
        verify(validator).expectNotNullOrUndefined(eq(t), eq(getProp), any(JSType.class), anyString(), any(JSType.class));
    }

    @Test
    public void testVisitAssign_simpleAssignment_typeCheckCalled() {
        Node lhs = createName("x");
        Node rhs = createName("y");
        Node assign = createNode(Token.ASSIGN, lhs, rhs);
        Node parent = createNode(Token.EXPR_RESULT, assign);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        JSType leftType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType rightType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        lhs.setJSType(leftType);
        rhs.setJSType(rightType);
        when(validator.expectCanAssignTo(any(NodeTraversal.class), any(Node.class),
                any(JSType.class), any(JSType.class), anyString())).thenReturn(true);
        typeCheck.visit(t, assign, parent);
        assertEquals(rightType, assign.getJSType());
    }

    @Test
    public void testVisitAssign_prototypeOverriding_nonObject_validateCalled() {
        Node obj = createName("Foo");
        Node prototype = createString("prototype");
        Node getProp = createNode(Token.GETPROP, obj, prototype);
        Node rhs = createName("obj");
        Node assign = createNode(Token.ASSIGN, getProp, rhs);
        Node parent = createNode(Token.EXPR_RESULT, assign);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        // Make obj a function that is constructor
        FunctionType funcType = new FunctionType(registry, null, null, null, null, null, false, false);
        funcType.setConstructor(true);
        obj.setJSType(funcType);
        rhs.setJSType(registry.getNativeType(JSTypeNative.OBJECT_TYPE));
        typeCheck.visit(t, assign, parent);
        verify(validator).expectObject(eq(t), eq(rhs), any(JSType.class), eq(TypeCheck.OVERRIDING_PROTOTYPE_WITH_NON_OBJECT));
    }

    @Test
    public void testVisitNew_constructor_invokesParameterCheck() {
        Node ctor = createName("Foo");
        Node newnode = createNode(Token.NEW, ctor);
        Node parent = createNode(Token.EXPR_RESULT, newnode);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        FunctionType fnType = new FunctionType(registry, null, null, null, null, null, false, false);
        fnType.setConstructor(true);
        ctor.setJSType(fnType);
        // Mock parameters
        when(fnType.getParameters()).thenReturn(new ArrayList<Node>().iterator());
        when(fnType.getInstanceType()).thenReturn(mock(JSType.class));
        typeCheck.visit(t, newnode, parent);
        assertEquals(fnType.getInstanceType(), newnode.getJSType());
    }

    @Test
    public void testVisitNew_notConstructor_reportWarning() {
        Node ctor = createName("notCtor");
        Node newnode = createNode(Token.NEW, ctor);
        Node parent = createNode(Token.EXPR_RESULT, newnode);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        ctor.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        typeCheck.visit(t, newnode, parent);
        verify(t).report(eq(newnode), eq(TypeCheck.NOT_A_CONSTRUCTOR));
    }

    @Test
    public void testVisitCall_notCallable_reportWarning() {
        Node calltarget = createName("x");
        Node call = createNode(Token.CALL, calltarget);
        Node parent = createNode(Token.EXPR_RESULT, call);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        calltarget.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        typeCheck.visit(t, call, parent);
        verify(t).report(eq(call), eq(TypeCheck.NOT_CALLABLE), anyString());
    }

    @Test
    public void testVisitCall_constructorNotCalledDirectly_reportWarning() {
        Node calltarget = createName("Foo");
        Node call = createNode(Token.CALL, calltarget);
        Node parent = createNode(Token.EXPR_RESULT, call);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        FunctionType fnType = new FunctionType(registry, null, null, null, null, null, false, false);
        fnType.setConstructor(true);
        fnType.setReturnType(registry.getNativeType(JSTypeNative.VOID_TYPE));
        calltarget.setJSType(fnType);
        typeCheck.visit(t, call, parent);
        verify(t).report(eq(call), eq(TypeCheck.CONSTRUCTOR_NOT_CALLABLE), anyString());
    }

    @Test
    public void testVisitCall_withExpectedThisType_reportWarning() {
        Node calltarget = createName("method");
        Node call = createNode(Token.CALL, calltarget);
        Node parent = createNode(Token.EXPR_RESULT, call);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        FunctionType fnType = new FunctionType(registry, null, null, null, null, null, false, false);
        ObjectType thisType = mock(ObjectType.class);
        when(thisType.isUnknownType()).thenReturn(false);
        when(thisType.isNativeObjectType()).thenReturn(false);
        fnType.setTypeOfThis(thisType);
        calltarget.setJSType(fnType);
        typeCheck.visit(t, call, parent);
        verify(t).report(eq(call), eq(TypeCheck.EXPECTED_THIS_TYPE), anyString());
    }

    @Test
    public void testVisitParameterList_wrongArgumentCount_reportWarning() {
        Node func = createName("func");
        Node arg1 = createName("a");
        Node call = createNode(Token.CALL, func, arg1);
        Node parent = createNode(Token.EXPR_RESULT, call);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        FunctionType fnType = new FunctionType(registry, null, null, null, null, null, false, false);
        // minArgs = 2, maxArgs = 2
        fnType.setMinArguments(2);
        fnType.setMaxArguments(2);
        List<Node> params = new ArrayList<>();
        params.add(createName("p1"));
        params.add(createName("p2"));
        when(fnType.getParameters()).thenReturn(params.iterator());
        call.setJSType(fnType);
        typeCheck.visit(t, call, parent);
        verify(t).report(eq(call), eq(TypeCheck.WRONG_ARGUMENT_COUNT), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testVisitBinaryOperator_stringConcatenation_noProblem() {
        Node left = createString("a");
        Node right = createString("b");
        Node add = createNode(Token.ADD, left, right);
        Node parent = createNode(Token.EXPR_RESULT, add);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        left.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
        right.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
        typeCheck.visit(t, add, parent);
        // ensureTyped called
        assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), add.getJSType());
    }

    @Test
    public void testVisitBinaryOperator_bitwise_nonInt32_reportWarning() {
        Node left = createNumber(3);
        Node right = createNumber(5);
        Node lsh = createNode(Token.LSH, left, right);
        Node parent = createNode(Token.EXPR_RESULT, lsh);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        // Set left type to something that does not match int32 context
        JSType notInt32 = mock(JSType.class);
        when(notInt32.matchesInt32Context()).thenReturn(false);
        left.setJSType(notInt32);
        right.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        typeCheck.visit(t, lsh, parent);
        verify(t).report(eq(left), eq(TypeCheck.BIT_OPERATION), anyString(), anyString());
        verify(t).report(eq(right), eq(TypeCheck.BIT_OPERATION), anyString(), anyString());
    }

    @Test
    public void testVisitReturn_voidFunction_withValue_reportInconsistent() {
        Node returnNode = createNode(Token.RETURN);
        Node value = createNumber(42);
        returnNode.addChildToBack(value);
        Node parent = createNode(Token.FUNCTION, returnNode);
        NodeTraversal t = mock(NodeTraversal.class);
        Node enclosingFunc = mock(Node.class);
        when(t.getEnclosingFunction()).thenReturn(enclosingFunc);
        FunctionType fnType = new FunctionType(registry, null, null, null, null, null, false, false);
        fnType.setReturnType(registry.getNativeType(JSTypeNative.VOID_TYPE));
        when(enclosingFunc.getJSType()).thenReturn(fnType);
        value.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
        typeCheck.visit(t, returnNode, parent);
        verify(validator).expectCanAssignTo(eq(t), eq(value), any(JSType.class), eq(registry.getNativeType(JSTypeNative.VOID_TYPE)), anyString());
    }

    @Test
    public void testCheckNoTypeCheckSection_enterAndLeave() throws Exception {
        Node script = createNode(Token.SCRIPT);
        JSDocInfo info = new JSDocInfo(false, false); // minimal
        info.setNoTypeCheck(true);
        script.setJSDocInfo(info);
        typeCheck.visit(mock(NodeTraversal.class), script, null);
        assertEquals(0, getPrivateIntField(typeCheck, "noTypeCheckSection"));
    }

    private int getPrivateIntField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(obj);
    }

    @Test
    public void testVisitObjLitKey_extern_doesNotValidate() {
        Node key = createNode(Token.STRING_KEY, createString("k"), createString("v"));
        Node objlit = createNode(Token.OBJECTLIT, key);
        objlit.setIsFromExterns(true);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        typeCheck.visit(t, key, objlit);
        // ensureTyped called but no validation
        assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), key.getJSType());
    }

    @Test
    public void testVisitAssign_enumAlias() {
        Node lhs = createName("myEnum");
        Node rhs = createName("otherEnum");
        Node assign = createNode(Token.ASSIGN, lhs, rhs);
        Node parent = createNode(Token.EXPR_RESULT, assign);
        NodeTraversal t = mock(NodeTraversal.class);
        when(t.getScope()).thenReturn(topScope);
        JSDocInfo declInfo = new JSDocInfo(false, false);
        // Setup enum parameter type; hack to make hasEnumParameterType true
        // In actual code, we would mock or create proper JSTypeExpression etc.
        // For simplicity, we skip full enum check but ensure the method runs.
        // Will reflect to call private checkEnumAlias? Not necessary.
        // We just test that no exception occurs.
        assign.setJSDocInfo(declInfo); // but hasEnumParameterType false, so checkEnumAlias returns early
        lhs.setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        rhs.setJSType(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE));
        typeCheck.visit(t, assign, parent);
        // verify nothing special.
    }

    @Test
    public void testGetTypedPercent_notCounted() {
        // Initial state: typedCount=0, nullCount=0, unknownCount=0
        assertEquals(0.0, typeCheck.getTypedPercent(), 0.0);
        // Simulate some counting
        Node node = createName("x");
        // doPercentTypedAccounting is private, but we can invoke visit with a node and check percent updates.
        // We'll just rely on default.
    }
}
