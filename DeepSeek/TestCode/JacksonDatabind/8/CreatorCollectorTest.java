package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.reflect.Member;
import java.util.*;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.util.ClassUtil;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CreatorCollectorTest {

    private CreatorCollector collector;
    private BeanDescription mockBeanDesc;
    private DeserializationConfig mockConfig;
    private JavaType mockType;
    private TypeBindings mockBindings;

    @Before
    public void setUp() {
        mockBeanDesc = mock(BeanDescription.class);
        mockConfig = mock(DeserializationConfig.class);
        mockType = mock(JavaType.class);
        mockBindings = mock(TypeBindings.class);

        when(mockBeanDesc.getType()).thenReturn(mockType);
        when(mockBeanDesc.bindingsForBeanType()).thenReturn(mockBindings);
        when(mockType.getRawClass()).thenReturn(Object.class);

        collector = new CreatorCollector(mockBeanDesc, true);
    }

    // Helper to create a mock AnnotatedWithParams
    private AnnotatedWithParams mockCreator(Class<?> clazz) {
        AnnotatedWithParams creator = mock(AnnotatedWithParams.class);
        when(creator.getClass()).thenReturn((Class) clazz);
        when(creator.getAnnotated()).thenReturn(mock(Member.class));
        return creator;
    }

    private AnnotatedWithParams mockCreator() {
        return mockCreator(AnnotatedConstructor.class);
    }

    private CreatorProperty mockProperty(String name) {
        CreatorProperty prop = mock(CreatorProperty.class);
        when(prop.getName()).thenReturn(name);
        return prop;
    }

    private CreatorProperty mockInjectableProperty(String name, Object id) {
        CreatorProperty prop = mock(CreatorProperty.class);
        when(prop.getName()).thenReturn(name);
        when(prop.getInjectableValueId()).thenReturn(id);
        return prop;
    }

    // --- Constructor and basic state tests ---

    @Test
    public void testConstructorWithCanFixAccessTrue() {
        CreatorCollector c = new CreatorCollector(mockBeanDesc, true);
        assertNotNull(c);
        assertFalse(c.hasDefaultCreator());
    }

    @Test
    public void testConstructorWithCanFixAccessFalse() {
        CreatorCollector c = new CreatorCollector(mockBeanDesc, false);
        assertNotNull(c);
        assertFalse(c.hasDefaultCreator());
    }

    @Test
    public void testHasDefaultCreatorInitiallyFalse() {
        assertFalse(collector.hasDefaultCreator());
    }

    @Test
    public void testHasDefaultCreatorAfterSet() {
        collector.setDefaultCreator(mockCreator());
        assertTrue(collector.hasDefaultCreator());
    }

    // --- setDefaultCreator ---

    @Test
    public void testSetDefaultCreator() {
        AnnotatedWithParams c = mockCreator();
        collector.setDefaultCreator(c);
        assertTrue(collector.hasDefaultCreator());
    }

    @Test
    public void testSetDefaultCreatorNull() {
        collector.setDefaultCreator(null);
        assertFalse(collector.hasDefaultCreator());
    }

    // --- addStringCreator ---

    @Test
    public void testAddStringCreatorExplicit() {
        AnnotatedWithParams c = mockCreator();
        collector.addStringCreator(c, true);
    }

    @Test
    public void testAddStringCreatorNonExplicit() {
        AnnotatedWithParams c = mockCreator();
        collector.addStringCreator(c, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddStringCreatorDuplicateExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        collector.addStringCreator(c1, true);
        collector.addStringCreator(c2, true);
    }

    @Test
    public void testAddStringCreatorDuplicateNonExplicitOverExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        collector.addStringCreator(c1, true);
        collector.addStringCreator(c2, false);
    }

    // --- addIntCreator ---

    @Test
    public void testAddIntCreatorExplicit() {
        AnnotatedWithParams c = mockCreator();
        collector.addIntCreator(c, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddIntCreatorDuplicateExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        collector.addIntCreator(c1, true);
        collector.addIntCreator(c2, true);
    }

    // --- addLongCreator ---

    @Test
    public void testAddLongCreatorExplicit() {
        AnnotatedWithParams c = mockCreator();
        collector.addLongCreator(c, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddLongCreatorDuplicateExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        collector.addLongCreator(c1, true);
        collector.addLongCreator(c2, true);
    }

    // --- addDoubleCreator ---

    @Test
    public void testAddDoubleCreatorExplicit() {
        AnnotatedWithParams c = mockCreator();
        collector.addDoubleCreator(c, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDoubleCreatorDuplicateExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        collector.addDoubleCreator(c1, true);
        collector.addDoubleCreator(c2, true);
    }

    // --- addBooleanCreator ---

    @Test
    public void testAddBooleanCreatorExplicit() {
        AnnotatedWithParams c = mockCreator();
        collector.addBooleanCreator(c, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddBooleanCreatorDuplicateExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        collector.addBooleanCreator(c1, true);
        collector.addBooleanCreator(c2, true);
    }

    // --- addDelegatingCreator ---

    @Test
    public void testAddDelegatingCreatorExplicit() {
        AnnotatedWithParams c = mockCreator();
        CreatorProperty[] injectables = new CreatorProperty[]{mockProperty("inject")};
        collector.addDelegatingCreator(c, true, injectables);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDelegatingCreatorDuplicateExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        collector.addDelegatingCreator(c1, true, null);
        collector.addDelegatingCreator(c2, true, null);
    }

    // --- addPropertyCreator ---

    @Test
    public void testAddPropertyCreatorSingle() {
        AnnotatedWithParams c = mockCreator();
        CreatorProperty[] props = new CreatorProperty[]{mockProperty("p1")};
        collector.addPropertyCreator(c, true, props);
    }

    @Test
    public void testAddPropertyCreatorMultipleNoDuplicates() {
        AnnotatedWithParams c = mockCreator();
        CreatorProperty[] props = new CreatorProperty[]{
                mockProperty("p1"), mockProperty("p2")
        };
        collector.addPropertyCreator(c, true, props);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPropertyCreatorDuplicateNames() {
        AnnotatedWithParams c = mockCreator();
        CreatorProperty[] props = new CreatorProperty[]{
                mockProperty("p1"), mockProperty("p1")
        };
        collector.addPropertyCreator(c, true, props);
    }

    @Test
    public void testAddPropertyCreatorWithInjectableSkipped() {
        AnnotatedWithParams c = mockCreator();
        CreatorProperty[] props = new CreatorProperty[]{
                mockInjectableProperty("", "id1"),
                mockInjectableProperty("", "id2")
        };
        collector.addPropertyCreator(c, true, props);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPropertyCreatorDuplicateExplicit() {
        AnnotatedWithParams c1 = mockCreator();
        AnnotatedWithParams c2 = mockCreator();
        CreatorProperty[] props = new CreatorProperty[]{mockProperty("p1")};
        collector.addPropertyCreator(c1, true, props);
        collector.addPropertyCreator(c2, true, props);
    }

    // --- addIncompeteParameter ---

    @Test
    public void testAddIncompleteParameter() {
        AnnotatedParameter param = mock(AnnotatedParameter.class);
        collector.addIncompeteParameter(param);
        collector.addIncompeteParameter(mock(AnnotatedParameter.class));
    }

    // --- Deprecated methods ---

    @Test
    public void testDeprecatedAddStringCreator() {
        AnnotatedWithParams c = mockCreator();
        collector.addStringCreator(c);
    }

    @Test
    public void testDeprecatedAddIntCreator() {
        AnnotatedWithParams c = mockCreator();
        collector.addIntCreator(c);
    }

    @Test
    public void testDeprecatedAddLongCreator() {
        AnnotatedWithParams c = mockCreator();
        collector.addLongCreator(c);
    }

    @Test
    public void testDeprecatedAddDoubleCreator() {
        AnnotatedWithParams c = mockCreator();
        collector.addDoubleCreator(c);
    }

    @Test
    public void testDeprecatedAddBooleanCreator() {
        AnnotatedWithParams c = mockCreator();
        collector.addBooleanCreator(c);
    }

    @Test
    public void testDeprecatedAddDelegatingCreator() {
        AnnotatedWithParams c = mockCreator();
        CreatorProperty[] injectables = new CreatorProperty[]{mockProperty("inject")};
        collector.addDelegatingCreator(c, injectables);
    }

    @Test
    public void testDeprecatedAddPropertyCreator() {
        AnnotatedWithParams c = mockCreator();
        CreatorProperty[] props = new CreatorProperty[]{mockProperty("p1")};
        collector.addPropertyCreator(c, props);
    }

    @Test
    public void testDeprecatedVerifyNonDup() {
        AnnotatedWithParams c = mockCreator();
        collector.addStringCreator(c, false);
    }

    // --- constructValueInstantiator ---

    @Test
    public void testConstructValueInstantiatorVanillaCollection() {
        when(mockType.getRawClass()).thenReturn((Class) Collection.class);
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof CreatorCollector.Vanilla);
        assertEquals(CreatorCollector.Vanilla.TYPE_COLLECTION, ((CreatorCollector.Vanilla) vi)._type);
    }

    @Test
    public void testConstructValueInstantiatorVanillaList() {
        when(mockType.getRawClass()).thenReturn((Class) List.class);
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof CreatorCollector.Vanilla);
        assertEquals(CreatorCollector.Vanilla.TYPE_COLLECTION, ((CreatorCollector.Vanilla) vi)._type);
    }

    @Test
    public void testConstructValueInstantiatorVanillaArrayList() {
        when(mockType.getRawClass()).thenReturn((Class) ArrayList.class);
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof CreatorCollector.Vanilla);
        assertEquals(CreatorCollector.Vanilla.TYPE_COLLECTION, ((CreatorCollector.Vanilla) vi)._type);
    }

    @Test
    public void testConstructValueInstantiatorVanillaMap() {
        when(mockType.getRawClass()).thenReturn((Class) Map.class);
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof CreatorCollector.Vanilla);
        assertEquals(CreatorCollector.Vanilla.TYPE_MAP, ((CreatorCollector.Vanilla) vi)._type);
    }

    @Test
    public void testConstructValueInstantiatorVanillaLinkedHashMap() {
        when(mockType.getRawClass()).thenReturn((Class) LinkedHashMap.class);
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof CreatorCollector.Vanilla);
        assertEquals(CreatorCollector.Vanilla.TYPE_MAP, ((CreatorCollector.Vanilla) vi)._type);
    }

    @Test
    public void testConstructValueInstantiatorVanillaHashMap() {
        when(mockType.getRawClass()).thenReturn((Class) HashMap.class);
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof CreatorCollector.Vanilla);
        assertEquals(CreatorCollector.Vanilla.TYPE_HASH_MAP, ((CreatorCollector.Vanilla) vi)._type);
    }

    @Test
    public void testConstructValueInstantiatorStdWithDefaultCreator() {
        when(mockType.getRawClass()).thenReturn(Object.class);
        collector.setDefaultCreator(mockCreator());
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof StdValueInstantiator);
    }

    @Test
    public void testConstructValueInstantiatorStdWithDelegatingCreator() {
        when(mockType.getRawClass()).thenReturn(Object.class);
        AnnotatedWithParams delegator = mockCreator();
        when(delegator.getGenericParameterType(0)).thenReturn(mock(java.lang.reflect.Type.class));
        when(mockBindings.resolveType(any(java.lang.reflect.Type.class))).thenReturn(mock(JavaType.class));
        collector.addDelegatingCreator(delegator, true, new CreatorProperty[]{null});
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof StdValueInstantiator);
    }

    @Test
    public void testConstructValueInstantiatorStdWithDelegatingCreatorAndInjectables() {
        when(mockType.getRawClass()).thenReturn(Object.class);
        AnnotatedWithParams delegator = mockCreator();
        when(delegator.getGenericParameterType(1)).thenReturn(mock(java.lang.reflect.Type.class));
        when(mockBindings.resolveType(any(java.lang.reflect.Type.class))).thenReturn(mock(JavaType.class));
        CreatorProperty[] injectables = new CreatorProperty[]{
                mockProperty("inject"), null
        };
        collector.addDelegatingCreator(delegator, true, injectables);
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof StdValueInstantiator);
    }

    @Test
    public void testConstructValueInstantiatorStdWithAllCreators() {
        when(mockType.getRawClass()).thenReturn(Object.class);
        collector.setDefaultCreator(mockCreator());
        collector.addStringCreator(mockCreator(), true);
        collector.addIntCreator(mockCreator(), true);
        collector.addLongCreator(mockCreator(), true);
        collector.addDoubleCreator(mockCreator(), true);
        collector.addBooleanCreator(mockCreator(), true);
        AnnotatedWithParams delegator = mockCreator();
        when(delegator.getGenericParameterType(0)).thenReturn(mock(java.lang.reflect.Type.class));
        when(mockBindings.resolveType(any(java.lang.reflect.Type.class))).thenReturn(mock(JavaType.class));
        collector.addDelegatingCreator(delegator, true, new CreatorProperty[]{null});
        collector.addPropertyCreator(mockCreator(), true, new CreatorProperty[]{mockProperty("p1")});
        collector.addIncompeteParameter(mock(AnnotatedParameter.class));
        ValueInstantiator vi = collector.constructValueInstantiator(mockConfig);
        assertNotNull(vi);
        assertTrue(vi instanceof StdValueInstantiator);
    }

    // --- verifyNonDup edge cases ---

    @Test
    public void testVerifyNonDupSubclassOverride() {
        AnnotatedWithParams c1 = mockCreator(AnnotatedConstructor.class);
        AnnotatedWithParams c2 = mockCreator(AnnotatedMethod.class);
        collector.addStringCreator(c1, true);
        collector.addStringCreator(c2, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyNonDupSameClassConflict() {
        AnnotatedWithParams c1 = mockCreator(AnnotatedConstructor.class);
        AnnotatedWithParams c2 = mockCreator(AnnotatedConstructor.class);
        collector.addStringCreator(c1, true);
        collector.addStringCreator(c2, true);
    }

    // --- Vanilla inner class tests ---

    @Test
    public void testVanillaGetValueTypeDescCollection() {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        assertEquals(ArrayList.class.getName(), v.getValueTypeDesc());
    }

    @Test
    public void testVanillaGetValueTypeDescMap() {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_MAP);
        assertEquals(LinkedHashMap.class.getName(), v.getValueTypeDesc());
    }

    @Test
    public void testVanillaGetValueTypeDescHashMap() {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_HASH_MAP);
        assertEquals(HashMap.class.getName(), v.getValueTypeDesc());
    }

    @Test
    public void testVanillaGetValueTypeDescUnknown() {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(999);
        assertEquals(Object.class.getName(), v.getValueTypeDesc());
    }

    @Test
    public void testVanillaCanInstantiate() {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        assertTrue(v.canInstantiate());
    }

    @Test
    public void testVanillaCanCreateUsingDefault() {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        assertTrue(v.canCreateUsingDefault());
    }

    @Test
    public void testVanillaCreateUsingDefaultCollection() throws IOException {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object result = v.createUsingDefault(ctxt);
        assertTrue(result instanceof ArrayList);
    }

    @Test
    public void testVanillaCreateUsingDefaultMap() throws IOException {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_MAP);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object result = v.createUsingDefault(ctxt);
        assertTrue(result instanceof LinkedHashMap);
    }

    @Test
    public void testVanillaCreateUsingDefaultHashMap() throws IOException {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_HASH_MAP);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        Object result = v.createUsingDefault(ctxt);
        assertTrue(result instanceof HashMap);
    }

    @Test(expected = IllegalStateException.class)
    public void testVanillaCreateUsingDefaultUnknown() throws IOException {
        CreatorCollector.Vanilla v = new CreatorCollector.Vanilla(999);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        v.createUsingDefault(ctxt);
    }
}
