package com.google.javascript.rhino.jstype;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.StaticScope;
import com.google.javascript.rhino.StaticSlot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NamedTypeTest {

    @Mock
    private JSTypeRegistry registry;
    @Mock
    private ErrorReporter errorReporter;
    @Mock
    private StaticScope<JSType> enclosingScope;
    @Mock
    private StaticSlot<JSType> slot;
    @Mock
    private JSType mockType;
    @Mock
    private ObjectType mockObjectType;
    @Mock
    private FunctionType mockFunctionType;
    @Mock
    private EnumType mockEnumType;
    @Mock
    private EnumElementType mockEnumElementType;
    @Mock
    private Predicate<JSType> validator;

    private static final String REFERENCE = "test.Type";
    private static final String SOURCE_NAME = "test.js";
    private static final int LINENO = 10;
    private static final int CHARNO = 5;

    private NamedType namedType;

    @Before
    public void setUp() {
        when(registry.getNativeObjectType(JSTypeNative.UNKNOWN_TYPE)).thenReturn(mockObjectType);
        when(registry.getNativeObjectType(JSTypeNative.NO_RESOLVED_TYPE)).thenReturn(mockObjectType);
        when(registry.getNativeFunctionType(JSTypeNative.NO_OBJECT_TYPE)).thenReturn(mockFunctionType);
        when(mockFunctionType.getInstanceType()).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(mockObjectType.isNoObjectType()).thenReturn(false);

        namedType = new NamedType(registry, REFERENCE, SOURCE_NAME, LINENO, CHARNO);
    }

    @Test
    public void testConstructor() {
        assertEquals(REFERENCE, namedType.getReferenceName());
        assertTrue(namedType.hasReferenceName());
        assertTrue(namedType.isNamedType());
        assertTrue(namedType.isNominalType());
        assertEquals(REFERENCE.hashCode(), namedType.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullReference() {
        new NamedType(registry, null, SOURCE_NAME, LINENO, CHARNO);
    }

    @Test
    public void testToStringHelper() {
        assertEquals(REFERENCE, namedType.toStringHelper(false));
        assertEquals(REFERENCE, namedType.toStringHelper(true));
    }

    @Test
    public void testDefinePropertyUnresolved() {
        assertFalse(namedType.isResolved());
        boolean result = namedType.defineProperty("prop", mockType, false, null);
        assertTrue(result);
    }

    @Test
    public void testDefinePropertyResolved() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        namedType.resolveInternal(errorReporter, enclosingScope);
        assertTrue(namedType.isResolved());

        when(mockObjectType.defineProperty(anyString(), any(JSType.class), anyBoolean(), any(Node.class))).thenReturn(true);
        boolean result = namedType.defineProperty("prop", mockType, false, null);
        assertTrue(result);
    }

    @Test
    public void testResolveViaRegistrySuccess() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        assertEquals(mockObjectType, result);
        verify(registry).getType(REFERENCE);
    }

    @Test
    public void testResolveViaRegistryNullType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot(anyString())).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testResolveViaPropertiesFunctionType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockFunctionType);
        when(mockFunctionType.isAllType()).thenReturn(false);
        when(mockFunctionType.isNoType()).thenReturn(false);
        when(mockFunctionType.isFunctionType()).thenReturn(true);
        when(mockFunctionType.isConstructor()).thenReturn(true);
        when(mockFunctionType.isInterface()).thenReturn(false);
        when(mockFunctionType.toMaybeFunctionType()).thenReturn(mockFunctionType);
        when(mockFunctionType.getInstanceType()).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        assertEquals(mockObjectType, result);
    }

    @Test
    public void testResolveViaPropertiesEnumType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockEnumType);
        when(mockEnumType.isAllType()).thenReturn(false);
        when(mockEnumType.isNoType()).thenReturn(false);
        when(mockEnumType.isFunctionType()).thenReturn(false);
        when(mockEnumType.isNoObjectType()).thenReturn(false);
        when(mockEnumType.getElementsType()).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        assertEquals(mockObjectType, result);
    }

    @Test
    public void testResolveViaPropertiesNoObjectType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockObjectType);
        when(mockObjectType.isAllType()).thenReturn(false);
        when(mockObjectType.isNoType()).thenReturn(false);
        when(mockObjectType.isFunctionType()).thenReturn(false);
        when(mockObjectType.isNoObjectType()).thenReturn(true);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        assertEquals(mockObjectType, result);
    }

    @Test
    public void testResolveViaPropertiesNullValue() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockObjectType);
        when(mockObjectType.isAllType()).thenReturn(false);
        when(mockObjectType.isNoType()).thenReturn(false);
        when(mockObjectType.isFunctionType()).thenReturn(false);
        when(mockObjectType.isNoObjectType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testResolveWithImplicitPrototypeCycle() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);
        when(mockObjectType.getImplicitPrototype()).thenReturn(namedType);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Cycle detected"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testResolveWithEnumElementCycle() {
        when(registry.getType(REFERENCE)).thenReturn(mockEnumElementType);
        when(mockEnumElementType.getPrimitiveType()).thenReturn(namedType);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Cycle detected"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testResolveNotLastGeneration() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot(anyString())).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(false);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertSame(namedType, result);
    }

    @Test
    public void testLookupViaPropertiesMultipleComponents() {
        String reference = "A.B.C";
        NamedType multiNamedType = new NamedType(registry, reference, SOURCE_NAME, LINENO, CHARNO);

        when(registry.getType(reference)).thenReturn(null);
        when(enclosingScope.getSlot("A")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockObjectType);
        when(mockObjectType.isAllType()).thenReturn(false);
        when(mockObjectType.isNoType()).thenReturn(false);
        when(mockObjectType.isFunctionType()).thenReturn(false);
        when(mockObjectType.isNoObjectType()).thenReturn(false);

        ObjectType parentClass = mock(ObjectType.class);
        when(mockObjectType.cast(mockObjectType)).thenReturn(parentClass);
        when(parentClass.getPropertyType("B")).thenReturn(mockObjectType);
        when(mockObjectType.cast(mockObjectType)).thenReturn(parentClass);
        when(parentClass.getPropertyType("C")).thenReturn(mockObjectType);

        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = multiNamedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
    }

    @Test
    public void testLookupViaPropertiesEmptyComponent() {
        String reference = ".Type";
        NamedType emptyNamedType = new NamedType(registry, reference, SOURCE_NAME, LINENO, CHARNO);

        when(registry.getType(reference)).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = emptyNamedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testLookupViaPropertiesNullSlot() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testLookupViaPropertiesAllType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockObjectType);
        when(mockObjectType.isAllType()).thenReturn(true);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testLookupViaPropertiesNoType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockObjectType);
        when(mockObjectType.isAllType()).thenReturn(false);
        when(mockObjectType.isNoType()).thenReturn(true);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testLookupViaPropertiesNullSlotType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testGetReferencedType() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        namedType.resolveInternal(errorReporter, enclosingScope);

        JSType referenced = namedType.getReferencedType();
        assertNotNull(referenced);
        assertEquals(mockObjectType, referenced);
    }

    @Test
    public void testSetValidatorResolved() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);
        namedType.resolveInternal(errorReporter, enclosingScope);

        boolean result = namedType.setValidator(validator);

        assertTrue(result);
        verify(validator).apply(mockObjectType);
    }

    @Test
    public void testSetValidatorUnresolved() {
        boolean result = namedType.setValidator(validator);

        assertTrue(result);
        verify(validator, never()).apply(any(JSType.class));
    }

    @Test
    public void testResolveWithValidator() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        namedType.setValidator(validator);
        namedType.resolveInternal(errorReporter, enclosingScope);

        verify(validator).apply(mockObjectType);
    }

    @Test
    public void testResolveForwardDeclaredType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot(anyString())).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(true);
        when(registry.isForwardDeclaredType(REFERENCE)).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter, never()).warning(contains("Bad type annotation"), anyString(), anyInt(), anyInt());
    }

    @Test
    public void testPropertyContinuationsAfterResolve() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        namedType.defineProperty("prop1", mockType, false, null);
        namedType.defineProperty("prop2", mockType, true, null);

        namedType.resolveInternal(errorReporter, enclosingScope);

        verify(mockObjectType, times(2)).defineProperty(anyString(), eq(mockType), anyBoolean(), isNull());
    }

    @Test
    public void testPropertyContinuationsWithUnknownType() {
        when(registry.getType(REFERENCE)).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(true);
        when(registry.isLastGeneration()).thenReturn(true);

        namedType.defineProperty("prop", mockType, false, null);
        namedType.resolveInternal(errorReporter, enclosingScope);

        verify(mockObjectType, never()).defineProperty(anyString(), any(JSType.class), anyBoolean(), any(Node.class));
    }

    @Test
    public void testGetTypedefTypeNullType() {
        when(slot.getType()).thenReturn(null);

        JSType result = namedType.getTypedefType(errorReporter, slot, "test");

        assertNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testGetTypedefTypeValidType() {
        when(slot.getType()).thenReturn(mockType);

        JSType result = namedType.getTypedefType(errorReporter, slot, "test");

        assertEquals(mockType, result);
        verify(errorReporter, never()).warning(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    public void testResolveViaPropertiesWithMultipleComponentsAndNullParent() {
        String reference = "A.B.C";
        NamedType multiNamedType = new NamedType(registry, reference, SOURCE_NAME, LINENO, CHARNO);

        when(registry.getType(reference)).thenReturn(null);
        when(enclosingScope.getSlot("A")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockObjectType);
        when(mockObjectType.isAllType()).thenReturn(false);
        when(mockObjectType.isNoType()).thenReturn(false);
        when(mockObjectType.isFunctionType()).thenReturn(false);
        when(mockObjectType.isNoObjectType()).thenReturn(false);

        when(mockObjectType.cast(mockObjectType)).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = multiNamedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testResolveViaPropertiesWithEmptyComponentInMiddle() {
        String reference = "A..C";
        NamedType multiNamedType = new NamedType(registry, reference, SOURCE_NAME, LINENO, CHARNO);

        when(registry.getType(reference)).thenReturn(null);
        when(enclosingScope.getSlot("A")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockObjectType);
        when(mockObjectType.isAllType()).thenReturn(false);
        when(mockObjectType.isNoType()).thenReturn(false);
        when(mockObjectType.isFunctionType()).thenReturn(false);
        when(mockObjectType.isNoObjectType()).thenReturn(false);

        ObjectType parentClass = mock(ObjectType.class);
        when(mockObjectType.cast(mockObjectType)).thenReturn(parentClass);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = multiNamedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testResolveViaPropertiesFunctionTypeNotConstructorOrInterface() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockFunctionType);
        when(mockFunctionType.isAllType()).thenReturn(false);
        when(mockFunctionType.isNoType()).thenReturn(false);
        when(mockFunctionType.isFunctionType()).thenReturn(true);
        when(mockFunctionType.isConstructor()).thenReturn(false);
        when(mockFunctionType.isInterface()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        verify(errorReporter).warning(contains("Bad type annotation"), eq(SOURCE_NAME), eq(LINENO), eq(CHARNO));
    }

    @Test
    public void testResolveViaPropertiesFunctionTypeInterface() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot("test")).thenReturn(slot);
        when(slot.getType()).thenReturn(mockFunctionType);
        when(mockFunctionType.isAllType()).thenReturn(false);
        when(mockFunctionType.isNoType()).thenReturn(false);
        when(mockFunctionType.isFunctionType()).thenReturn(true);
        when(mockFunctionType.isConstructor()).thenReturn(false);
        when(mockFunctionType.isInterface()).thenReturn(true);
        when(mockFunctionType.toMaybeFunctionType()).thenReturn(mockFunctionType);
        when(mockFunctionType.getInstanceType()).thenReturn(mockObjectType);
        when(mockObjectType.isUnknownType()).thenReturn(false);
        when(registry.isLastGeneration()).thenReturn(true);

        JSType result = namedType.resolveInternal(errorReporter, enclosingScope);

        assertNotNull(result);
        assertEquals(mockObjectType, result);
    }

    @Test
    public void testResolveWithValidatorAndForwardDeclaredType() {
        when(registry.getType(REFERENCE)).thenReturn(null);
        when(enclosingScope.getSlot(anyString())).thenReturn(null);
        when(registry.isLastGeneration()).thenReturn(true);
        when(registry.isForwardDeclaredType(REFERENCE)).thenReturn(true);

        namedType.setValidator(validator);
        namedType.resolveInternal(errorReporter, enclosingScope);

        verify(validator).apply(any(JSType.class));
    }
}
