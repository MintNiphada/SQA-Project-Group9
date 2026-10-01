package com.google.javascript.jscomp;

import static org.junit.Assert.*;
import org.junit.Test;

import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.UnknownType;
import java.util.ArrayList;
import java.util.List;

public class TypeValidatorTest {

    // Stubs
    private static class TestCompiler extends AbstractCompiler {
        private final JSTypeRegistry registry;
        private final List<JSError> errors = new ArrayList<>();

        TestCompiler(JSTypeRegistry registry) {
            this.registry = registry;
        }

        @Override
        JSTypeRegistry getTypeRegistry() {
            return registry;
        }

        @Override
        void report(JSError error) {
            errors.add(error);
        }

        List<JSError> getErrors() {
            return errors;
        }
    }

    private static class TestRegistry extends JSTypeRegistry {
        private JSType objectType = new TestObjectType(this);
        private JSType stringType = new TestJSType(this) { { setMatchesStringContext(true); } };
        private JSType numberType = new TestJSType(this) { { setMatchesNumberContext(true); } };
        private JSType booleanType = new TestJSType(this);
        private JSType nullType = new TestJSType(this) { { setNullType(true); } };
        private JSType voidType = new TestJSType(this) { { setVoidType(true); } };
        private JSType arrayType = new TestJSType(this) { { setArrayType(true); } };
        private JSType noObjectType = new TestJSType(this);
        private JSType numberString = new TestJSType(this) {
            {
                setMatchesNumberContext(true);
                setMatchesStringContext(true);
            }
        };
        private JSType unknownType = new TestJSType(this) { { setUnknownType(true); } };
        private JSType allValueTypes = null;
        private JSType nullOrUndefined = null;

        TestRegistry() {
            super(null);
        }

        @Override
        public JSType getNativeType(JSTypeNative typeId) {
            switch (typeId) {
                case OBJECT_TYPE: return objectType;
                case STRING_TYPE: return stringType;
                case NUMBER_TYPE: return numberType;
                case BOOLEAN_TYPE: return booleanType;
                case NULL_TYPE: return nullType;
                case VOID_TYPE: return voidType;
                case ARRAY_TYPE: return arrayType;
                case NO_OBJECT_TYPE: return noObjectType;
                case NUMBER_STRING: return numberString;
                case UNKNOWN_TYPE: return unknownType;
                default: return unknownType;
            }
        }

        @Override
        public JSType createUnionType(JSTypeNative... types) {
            // Simplified union creation
            return new TestJSType(this) {
                @Override
                public boolean isUnionType() { return true; }
                @Override
                public boolean isSubtype(JSType that) {
                    // simple union subtype: all members subtype of that
                    return false;
                }
            };
        }
    }

    // Base stub for JSType with default false/null returns
    private static class TestJSType extends JSType {
        private boolean isNoType = false;
        private boolean isUnknownType = false;
        private boolean isObject = false;
        private boolean matchesObjectContext = false;
        private boolean matchesStringContext = false;
        private boolean matchesNumberContext = false;
        private boolean isEmptyType = false;
        private boolean isSubtypeResult = false;
        private boolean isNullType = false;
        private boolean isVoidType = false;
        private boolean isNoResolvedType = false;
        private boolean canAssignToResult = false;
        private boolean canTestForShallowEqualityWithResult = false;
        private JSType autoboxesTo = null;
        private boolean isStruct = false;
        private ObjectType dereferenceResult = null;
        private JSType restrictByNotNullOrUndefinedResult = this;
        private boolean isConstructor = false;
        private boolean isEnumType = false;
        private boolean isFunctionType = false;
        private boolean isArrayType = false;
        private FunctionType toMaybeFunctionTypeResult = null;

        TestJSType(JSTypeRegistry registry) {
            super(registry);
        }

        // Setters for behavior control
        void setNoType(boolean v) { isNoType = v; }
        void setUnknownType(boolean v) { isUnknownType = v; }
        void setObject(boolean v) { isObject = v; }
        void setMatchesObjectContext(boolean v) { matchesObjectContext = v; }
        void setMatchesStringContext(boolean v) { matchesStringContext = v; }
        void setMatchesNumberContext(boolean v) { matchesNumberContext = v; }
        void setEmptyType(boolean v) { isEmptyType = v; }
        void setIsSubtype(boolean v) { isSubtypeResult = v; }
        void setNullType(boolean v) { isNullType = v; }
        void setVoidType(boolean v) { isVoidType = v; }
        void setIsNoResolvedType(boolean v) { isNoResolvedType = v; }
        void setCanAssignTo(boolean v) { canAssignToResult = v; }
        void setCanTestForShallowEqualityWith(boolean v) { canTestForShallowEqualityWithResult = v; }
        void setAutoboxesTo(JSType v) { autoboxesTo = v; }
        void setStruct(boolean v) { isStruct = v; }
        void setDereference(ObjectType v) { dereferenceResult = v; }
        void setRestrictByNotNullOrUndefined(JSType v) { restrictByNotNullOrUndefinedResult = v; }
        void setConstructor(boolean v) { isConstructor = v; }
        void setEnumType(boolean v) { isEnumType = v; }
        void setFunctionType(boolean v) { isFunctionType = v; }
        void setArrayType(boolean v) { isArrayType = v; }
        void setToMaybeFunctionType(FunctionType v) { toMaybeFunctionTypeResult = v; }

        @Override
        public boolean isNoType() { return isNoType; }
        @Override
        public boolean isUnknownType() { return isUnknownType; }
        @Override
        public boolean isObject() { return isObject; }
        @Override
        public boolean matchesObjectContext() { return matchesObjectContext; }
        @Override
        public boolean matchesStringContext() { return matchesStringContext; }
        @Override
        public boolean matchesNumberContext() { return matchesNumberContext; }
        @Override
        public boolean isEmptyType() { return isEmptyType; }
        @Override
        public boolean isSubtype(JSType that) { return isSubtypeResult; }
        @Override
        public boolean isNullType() { return isNullType; }
        @Override
        public boolean isVoidType() { return isVoidType; }
        @Override
        public boolean isNoResolvedType() { return isNoResolvedType; }
        @Override
        public boolean canAssignTo(JSType that) { return canAssignToResult; }
        @Override
        public boolean canTestForShallowEqualityWith(JSType that) { return canTestForShallowEqualityWithResult; }
        @Override
        public JSType autoboxesTo() { return autoboxesTo; }
        @Override
        public boolean isStruct() { return isStruct; }
        @Override
        public ObjectType dereference() { return dereferenceResult; }
        @Override
        public JSType restrictByNotNullOrUndefined() { return restrictByNotNullOrUndefinedResult; }
        @Override
        public boolean isConstructor() { return isConstructor; }
        @Override
        public boolean isEnumType() { return isEnumType; }
        @Override
        public boolean isFunctionType() { return isFunctionType; }
        @Override
        public boolean isArrayType() { return isArrayType; }
        @Override
        public FunctionType toMaybeFunctionType() { return toMaybeFunctionTypeResult; }

        // Abstract methods that we don't need for testing
        @Override public JSType getLeastSupertype(JSType that) { return this; }
        @Override public JSType getGreatestSubtype(JSType that) { return this; }
        @Override public TernaryValue testForEquality(JSType that) { return TernaryValue.UNKNOWN; }
        @Override public boolean matchesInt32Context() { return false; }
        @Override public boolean matchesUint32Context() { return false; }
        @Override public boolean isEquivalentTo(JSType that) { return false; }
        @Override public int hashCode() { return 0; }
        @Override public JSType collapseUnion() { return this; }
        @Override public boolean isNominalType() { return false; }
        @Override public boolean isAllType() { return false; }
        @Override public String toString() { return "testJSType"; }
    }

    private static class TestObjectType extends ObjectType {
        TestObjectType(JSTypeRegistry registry) { super(registry); }
        @Override
        public ObjectType getImplicitPrototype() { return null; }
        @Override
        public FunctionType getConstructor() { return null; }
        @Override
        public boolean isFunctionPrototypeType() { return false; }
        @Override
        public JSType getIndexType() { return null; }
        @Override
        public boolean hasOwnProperty(String name) { return false; }
        @Override
        public com.google.common.collect.ImmutableList<String> getOwnPropertyNames() { return com.google.common.collect.ImmutableList.of(); }
        @Override
        public JSType getPropertyType(String name) { return null; }
        @Override
        public boolean isPropertyTypeDeclared(String name) { return false; }
        @Override
        public boolean isPropertyTypeInferred(String name) { return false; }
        @Override
        public ObjectType restrictByNotNullOrUndefined() { return this; }
        @Override
        public String toString() { return "testObject"; }
    }

    // Create TypeValidator with custom registry and compiler
    private TypeValidator createValidator(TestRegistry registry) {
        TestCompiler compiler = new TestCompiler(registry);
        return new TypeValidator(compiler);
    }

    private Node createGetPropNode() {
        Node parent = new Node(1); // dummy
        Node child1 = new Node(2);
        Node child2 = Node.newString("prop");
        parent.addChildrenToBack(child1);
        parent.addChildrenToBack(child2);
        return parent; // node is GETPROP
    }

    @Test
    public void testExpectObject_match() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesObjectContext(true);
        assertTrue(v.expectObject(null, n, t, "msg"));
    }

    @Test
    public void testExpectObject_mismatch() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesObjectContext(false);
        assertFalse(v.expectObject(null, n, t, "msg"));
    }

    @Test
    public void testExpectActualObject_objectType() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setObject(true);
        // expectActualObject does not warn if type.isObject() true, no mismatch
        v.expectActualObject(null, n, t, "msg");
        assertTrue(v.getMismatches().iterator().hasNext() == false); // no mismatch registered
    }

    @Test
    public void testExpectActualObject_notObject() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setObject(false);
        v.expectActualObject(null, n, t, "msg");
        assertTrue(v.getMismatches().iterator().hasNext() == true); // mismatch registered
    }

    @Test
    public void testExpectAnyObject_subtype() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        // anyObjectType.isSubtype(type) -> true
        TestJSType anyObject = new TestJSType(reg);
        anyObject.setIsSubtype(true);
        reg.noObjectType = anyObject;
        // type.isEmptyType() false
        t.setEmptyType(false);
        v.expectAnyObject(null, n, t, "msg");
        // No mismatch if subtype
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectAnyObject_emptyType() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setEmptyType(true);
        v.expectAnyObject(null, n, t, "msg");
        // Even if not subtype, because isEmptyType, no mismatch
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectAnyObject_mismatch() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setEmptyType(false);
        TestJSType anyObject = new TestJSType(reg);
        anyObject.setIsSubtype(false);
        reg.noObjectType = anyObject;
        v.expectAnyObject(null, n, t, "msg");
        assertTrue(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectString_match() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesStringContext(true);
        v.expectString(null, n, t, "msg");
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectString_mismatch() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesStringContext(false);
        v.expectString(null, n, t, "msg");
        assertTrue(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectNumber_match() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesNumberContext(true);
        v.expectNumber(null, n, t, "msg");
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectNumber_mismatch() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesNumberContext(false);
        v.expectNumber(null, n, t, "msg");
        assertTrue(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectBitwiseable_numberContext() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesNumberContext(true);
        v.expectBitwiseable(null, n, t, "msg");
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectBitwiseable_subtypeAllValueTypes() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesNumberContext(false);
        t.setIsSubtype(true);
        v.expectBitwiseable(null, n, t, "msg");
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectBitwiseable_mismatch() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesNumberContext(false);
        t.setIsSubtype(false);
        v.expectBitwiseable(null, n, t, "msg");
        assertTrue(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectStringOrNumber_matchNumber() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesNumberContext(true);
        v.expectStringOrNumber(null, n, t, "msg");
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectStringOrNumber_matchString() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesStringContext(true);
        v.expectStringOrNumber(null, n, t, "msg");
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectStringOrNumber_mismatch() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setMatchesNumberContext(false);
        t.setMatchesStringContext(false);
        v.expectStringOrNumber(null, n, t, "msg");
        assertTrue(v.getMismatches().iterator().hasNext());
    }

    @Test
    public void testExpectNotNullOrUndefined_noType() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setNoType(true);
        assertTrue(v.expectNotNullOrUndefined(null, n, t, "msg", reg.stringType));
    }

    @Test
    public void testExpectNotNullOrUndefined_unknownType() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setUnknownType(true);
        assertTrue(v.expectNotNullOrUndefined(null, n, t, "msg", reg.stringType));
    }

    @Test
    public void testExpectNotNullOrUndefined_notSubtypeNullOrUndefined() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setIsSubtype(false); // not subtype of nullOrUndefined
        assertTrue(v.expectNotNullOrUndefined(null, n, t, "msg", reg.stringType));
    }

    @Test
    public void testExpectNotNullOrUndefined_subtypeButContainsForwardDeclared() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        Node n = new Node(1);
        TestJSType t = new TestJSType(reg);
        t.setIsSubtype(true); // subtype of nullOrUndefined
        t.setIsNoResolvedType(true); // forward declared
        assertTrue(v.expectNotNullOrUndefined(null, n, t, "msg", reg.stringType));
    }

    @Test
    public void testExpectNotNullOrUndefined_getPropNullInNonGlobal() {
        TestRegistry reg = new TestRegistry();
        TypeValidator v = createValidator(reg);
        NodeTraversal t = null; // not in global scope
        Node n = createGetPropNode(); // isGetProp = true
        TestJSType t1 = new TestJSType(reg);
        t1.setNullType(true);
        t1.setIsSubtype(true);
        // The special condition: n.isGetProp() && !t.inGlobalScope() && type.isNullType() -> return true, no mismatch
        assertTrue(v.expectNotNullOrUndefined(null, n, t1, "msg", reg.stringType));
        assertFalse(v.getMismatches().iterator().hasNext());
    }

    // TODO: Additional tests for all branches...
}
```
