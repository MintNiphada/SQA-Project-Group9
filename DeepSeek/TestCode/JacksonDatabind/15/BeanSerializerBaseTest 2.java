package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * Tests for {@link BeanSerializerBase} using a concrete subclass.
 */
@RunWith(MockitoJUnitRunner.class)
public class BeanSerializerBaseTest {

    // Concrete subclass for testing
    static class ConcreteBeanSerializer extends BeanSerializerBase {
        public ConcreteBeanSerializer(JavaType type, BeanSerializerBuilder builder,
                                      BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
            super(type, builder, properties, filteredProperties);
        }

        public ConcreteBeanSerializer(BeanSerializerBase src,
                                     BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
            super(src, properties, filteredProperties);
        }

        public ConcreteBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
            super(src, objectIdWriter, filterId);
        }

        public ConcreteBeanSerializer(BeanSerializerBase src, String[] toIgnore) {
            super(src, toIgnore);
        }

        public ConcreteBeanSerializer(BeanSerializerBase src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        public ConcreteBeanSerializer(BeanSerializerBase src) {
            super(src);
        }

        @Override
        public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            // Not needed for most tests, but can be overridden if needed
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new ConcreteBeanSerializer(this, objectIdWriter, _propertyFilterId);
        }

        @Override
        protected BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new ConcreteBeanSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            return this; // For testing purposes, return self
        }

        @Override
        protected BeanSerializerBase withFilterId(Object filterId) {
            return new ConcreteBeanSerializer(this, _objectIdWriter, filterId);
        }

        // Expose protected methods for testing
        public void callSerializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            serializeFields(bean, jgen, provider);
        }

        public void callSerializeFieldsFiltered(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            serializeFieldsFiltered(bean, jgen, provider);
        }

        public void call_resolve(SerializerProvider provider) throws JsonMappingException {
            resolve(provider);
        }

        public JsonSerializer<?> callCreateContextual(SerializerProvider provider, BeanProperty property)
                throws JsonMappingException {
            return createContextual(provider, property);
        }

        public void call_serializeWithObjectId(Object bean, JsonGenerator jgen, SerializerProvider provider,
                                               TypeSerializer typeSer) throws IOException {
            _serializeWithObjectId(bean, jgen, provider, typeSer);
        }

        public void call_serializeWithObjectId(Object bean, JsonGenerator jgen, SerializerProvider provider,
                                               boolean startEndObject) throws IOException {
            _serializeWithObjectId(bean, jgen, provider, startEndObject);
        }
    }

    @Mock
    private JavaType mockType;
    @Mock
    private BeanSerializerBuilder mockBuilder;
    @Mock
    private BeanPropertyWriter mockProp1;
    @Mock
    private BeanPropertyWriter mockProp2;
    @Mock
    private BeanPropertyWriter mockPropFilter1;
    @Mock
    private BeanPropertyWriter mockPropFilter2;
    @Mock
    private SerializerProvider mockProvider;
    @Mock
    private JsonGenerator mockGen;
    @Mock
    private TypeSerializer mockTypeSer;
    @Mock
    private BeanProperty mockBeanProperty;
    @Mock
    private AnnotatedMember mockMember;
    @Mock
    private AnnotationIntrospector mockIntrospector;
    @Mock
    private SerializationConfig mockConfig;
    @Mock
    private JsonSerializer<Object> mockSerializer;
    @Mock
    private PropertyFilter mockPropertyFilter;
    @Mock
    private ObjectIdWriter mockObjectIdWriter;
    @Mock
    private WritableObjectId mockWritableObjectId;
    private Object testBean;
    private ConcreteBeanSerializer serializer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        testBean = new Object();

        // Default stubs for common mocks
        when(mockType.getRawClass()).thenReturn(Object.class);
        when(mockType.isEnum()).thenReturn(false);
        when(mockBuilder.getTypeId()).thenReturn(null);
        when(mockBuilder.getAnyGetter()).thenReturn(null);
        when(mockBuilder.getFilterId()).thenReturn(null);
        when(mockBuilder.getObjectIdWriter()).thenReturn(null);
        BeanDescription mockBeanDesc = mock(BeanDescription.class);
        when(mockBuilder.getBeanDescription()).thenReturn(mockBeanDesc);
        when(mockBeanDesc.findExpectedFormat(null)).thenReturn(null);

        when(mockProp1.willSuppressNulls()).thenReturn(false);
        when(mockProp1.hasNullSerializer()).thenReturn(false);
        when(mockProp1.hasSerializer()).thenReturn(false);
        when(mockProp1.getName()).thenReturn("prop1");
        when(mockProp1.getSerializationType()).thenReturn(null);
        when(mockProp1.getGenericPropertyType()).thenReturn(Object.class);
        when(mockProp2.willSuppressNulls()).thenReturn(false);
        when(mockProp2.hasNullSerializer()).thenReturn(false);
        when(mockProp2.hasSerializer()).thenReturn(false);
        when(mockProp2.getName()).thenReturn("prop2");
        when(mockProp2.getSerializationType()).thenReturn(null);
        when(mockProp2.getGenericPropertyType()).thenReturn(Object.class);

        // Default provider stubs
        when(mockProvider.getAnnotationIntrospector()).thenReturn(mockIntrospector);
        when(mockProvider.getConfig()).thenReturn(mockConfig);
    }

    private ConcreteBeanSerializer createSerializer(BeanPropertyWriter[] props, BeanPropertyWriter[] filteredProps) {
        return new ConcreteBeanSerializer(mockType, null, props, filteredProps); // builder null for simplicity
    }

    // ---------- Constructor tests ----------

    @Test
    public void testConstructorWithBuilderNull() {
        serializer = new ConcreteBeanSerializer(mockType, null, new BeanPropertyWriter[0], null);
        assertNotNull(serializer);
        assertNull(serializer._typeId);
        assertNull(serializer._anyGetterWriter);
        assertNull(serializer._propertyFilterId);
        assertNull(serializer._objectIdWriter);
        assertNull(serializer._serializationShape);
    }

    @Test
    public void testConstructorWithBuilder() {
        when(mockBuilder.getTypeId()).thenReturn(mock(AnnotatedMember.class));
        AnyGetterWriter mockAny = mock(AnyGetterWriter.class);
        when(mockBuilder.getAnyGetter()).thenReturn(mockAny);
        when(mockBuilder.getFilterId()).thenReturn("filterId");
        ObjectIdWriter oiw = mock(ObjectIdWriter.class);
        when(mockBuilder.getObjectIdWriter()).thenReturn(oiw);
        BeanDescription desc = mock(BeanDescription.class);
        JsonFormat.Value format = mock(JsonFormat.Value.class);
        when(format.getShape()).thenReturn(JsonFormat.Shape.STRING));
        when(mockBuilder.getBeanDescription()).thenReturn(desc);
        when(desc.findExpectedFormat(null)).thenReturn(format);
        serializer = new ConcreteBeanSerializer(mockType, mockBuilder, new BeanPropertyWriter[0], null);        assertNotNull(serializer._typeId);
        assertEquals(mockAny, serializer._anyGetterWriter);
        assertEquals("filterId", serializer._propertyFilterId);
        assertEquals(oiw, serializer._objectIdWriter);
        assertEquals(JsonFormat.Shape.STRING, serializer._serializationShape);
    }

    @Test
    public void testCopyConstructorWithProps() {
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        ConcreteBeanSerializer copy = new ConcreteBeanSerializer(serializer, new BeanPropertyWriter[]{mockProp2}, new BeanPropertyWriter[0]);
        assertSame(mockType, copy._handledType);
        assertArrayEquals(new BeanPropertyWriter[]{mockProp2}, copy._props);
        assertArrayEquals(new BeanPropertyWriter[0], copy._filteredProps);
        assertSame(serializer._typeId, copy._typeId);
        assertSame(serializer._anyGetterWriter, copy._anyGetterWriter);
    }

    @Test
    public void testConstructorWithIgnorals() {
        BeanPropertyWriter[] props = {mockProp1, mockProp2};
        When(mockProp1.getName()).thenReturn("p1");
        When(mockProp2.getName()).thenReturn("p2");
        serializer = createSerializer(props, props);
        ConcreteBeanSerializer ignored = new ConcreteBeanSerializer(serializer, new String[] {"p1"});
        assertEquals(1, ignored._props.length);
        assertEquals("p2", ignored._props[0].getName());
        assertEquals(1, ignored._filteredProps.length);
        assertEquals("p2", ignored._filteredProps[0].getName());
    }

    @Test
    public void testConstructorWithObjectIdWriter() {
        serializer = createSerializer(new BeanPropertyWriter[0], null);
        ObjectIdWriter oiw = mock(ObjectIdWriter.class);
        Object filterId = new Object();
        ConcreteBeanSerializer withOID = new ConcreteBeanSerializer(serializer, oiw, filterId);
        assertEquals(oiw, withOID._objectIdWriter);
        assertEquals(filterId, withOID._propertyFilterId);
    }

    @Test
    public void testConstructorWithNameTransformer() {
        BeanPropertyWriter[] props = {mockProp1};
        when(mockProp1.rename(any(NameTransformer.class))).thenReturn(mockProp2);
        NameTransformer transformer = mock(NameTransformer.class);
        serializer = createSerializer(props, null);
        ConcreteBeanSerializer renamed = new ConcreteBeanSerializer(serializer, transformer);
        assertSame(mockProp2, renamed._props[0]);
    }

    // ---------- resolve tests ----------

    @Test
    public void testResolveAssignsNullSerializer() throws Exception {
        BeanPropertyWriter[] props = {mockProp1};
        serializer = createSerializer(props, null);
        // mockProp1: willSuppressNulls=false, hasNullSerializer=false => should assign null serializer
        JsonSerializer<Object> nullSer = mock(JsonSerializer.class);
        when(mockProvider.findNullValueSerializer(mockProp1)).thenReturn(nullSer);
        serializer.call_resolve(mockProvider);
        verify(mockProp1).assignNullSerializer(nullSer);
    }

    @Test
    public void testResolveDoesNotAssignNullSerializerWhenWillSuppressNulls() throws Exception {
        reset(mockProp1);
        when(mockProp1.willSuppressNulls()).thenReturn(true);
        when(mockProp1.hasSerializer()).thenReturn(false);
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        serializer.call_resolve(mockProvider);
        verify(mockProp1, never()).assignNullSerializer(any());
    }

    @Test
    public void testResolveDoesNotAssignNullSerializerWhenHasNullSerializer() throws Exception {
        reset(mockProp1);
        when(mockProp1.willSuppressNulls()).thenReturn(fals);
        when(mockProp1.hasNullSerializer()).thenReturn(true);
        when(mockProp1.hasSerializer()).thenReturn(false);
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        seralizer.call_resolve(mockProvider);
        verif(mockProp1, never()).assignNullSerializer(any());
    }

    @Test    public void testResolveAssignsSerializerDirectly() throws Exception {        reset(mockProp1);
        when(mockProp1.willSuppressNulls()).thenReturn(false);
        when(mockProp1.hasNullSerializer()).thenReturn(true);
        when(mockProp1.hasSerializer()).thenReturn(false); // will need serializer
        when(mockProp1.getSerializationType()).thenReturn(null);
        when(mockProp1.getGenericPropertyType()).thenReturn(String.class);
        JavaType stringType = mock(JavaType.class);
        when(mockProvider.constructType(String.class)).thenReturn(stringType);
        when(stringType.isFinal()).thenReturn(true);
        JsonSerailizer<Object> ser = mock(JsonSerializer.class);
        when(mockProvider.findValueSerializer(stringType, mockProp1)).thenReturn(ser);
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        seralizer.call_resolve(mockProvider);
        verify(mockProp1).assignSerializer(ser);
    }

    @Test    public void testResolveAssignsSerializerFromConverter() throws Exception {
        reset(mockProp1);
        when(mockProp1.willSuppressNulls()).thenReturn(false);
        when(mockProp1.hasNullSerializer()).thenReturn(true);
        when(mockProp1.hasSerializer()).thenReturn(false);
        when(mockProp1.getSerializationType()).thenReturn(null);
        // Setup converter scenario: introspector finds converter
        when(mockIntrospector.findSerializationConverter(mockMember)).thenReturn(Object.class);
        when(mockProp1.getMember()).thenReturn(mockMember);
        Converter<Object,Object> conv = mock(Converter.class);
        when(mockProvider.converterInstance(mockMember, Object.class)).thenReturn(conv);
        JavaType delegateType = mock(JavaType.class);
        when(conv.getOutputType(mockProvider.getTypeFactory())).thenReturn(delegateType);
        JsonSerializer<Object> ser = mock(JsonSerializer.class);
        when(mockProvider.findValueSerializer(delegateType, mockProp1)).thenReturn(ser);
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        serializer.call_resolve(mockProvider);
        verify(mockProp1).assignSerializer(any(StdDelegatingSerializer.class));
    }

    @Test    public void testResolveSetsNonTrivialBaseTypeForNonFinal() throws Exception {
        reset(mockProp1);
        when(mockProp1.willSuppressNulls()).thenReturn(false);
        when(mockProp1.hasNullSerializer()).thenReturn(true);
        when(mockProp1.hasSerializer()).thenReturn(false);
        when(mockProp1.getSerializationType()).thenReturn(null);
        when(mockProp1.getGenericPropertyType()).thenReturn(Object.class);
        JavaType objType = mock(JavaType.class);
        when(mockProvider.constructType(Object.class)).thenReturn(objType);
        when(objType.isFinal()).thenReturn(false);
        when(objType.isContainerType()).thenReturn(false);
        when(objType.containedTypeCount()).thenReturn(0);
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        serializer.call_resolve(mockProvider);
        verify(mockProp1).setNonTrivialBaseType(objType);
        verify(mockProp1, never()).assignSerializer(any());
    }

    @Test    public void testResolveHandlesContainerTypeWithTypeSerializer() throws Exception {
        reset(mockProp1);
        when(mockProp1.willSuppressNulls()).thenReturn(false);
        when(mockProp1.hasNullSerializer()).thenReturn(true);
        when(mockProp1.hasSerializer()).thenReturn(false);
        JavaType containerType = mock(JavaType.class);
        when(mockProp1.getSerializationType()).thenReturn(containerType);
        when(containerType.isContainerType()).thenReturn(true);
        JavaType contentType = mock(JavaType.class);
        when(containerType.getContentType()).thenReturn(contentType);
        TypeSerializer typeSer = mock(TypeSerializer.class);
        when(contentType.getTypeHandler()).thenReturn(typeSer);
        ContainerSerializer<?> containerSer = mock(ContainerSerializer.class);
        when(mockProvider.findValueSerializer(containerType, mockProp1)).thenReturn(containerSer);
        JsonSerializer<Object> wrappedSer = mock(JsonSerializer.class);
        when(containerSer.withValueTypeSerializer(typeSer)).thenReturn(wrappedSer);
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        serializer.call_resolve(mockProvider);
        verify(mockProp1).assignSerializer(wrappedSer);
    }

    @Test    public void testResolveAnyGetterWriter() throws Exception {
        AnyGetterWriter any = mock(AnyGetterWriter.class);
        when(mockBuilder.getAnyGetter()).thenReturn(any);
        when(mockBuilder.getTypeId()).thenReturn(null);
        when(mockBuilder.getFilterId()).thenReturn(null);
        when(mockBuilder.getObjectIdWriter()).thenReturn(null);
        when(mockBuilder.getBeanDescription()).thenReturn(mock(BeanDescription.class));
        JsonFormat.Value format = mock(JsonFormat.Value.class);
        when(format.getShape()).thenReturn(null);
        when(mock(BeanDescription.class).findExpectedFormat(null)).thenReturn(format);
        serializer = new ConcreteBeanSerializer(mockType, mockBuilder, new BeanPropertyWriter[]{mockProp1}, null);
        // Reset mockProvider for this test
        when(mockProp1.willSuppressNulls()).thenReturn(false);
        when(mockProp1.hasNullSerializer()).thenReturn(true);
        when(mockProp1.hasSerializer()).thenReturn(false);
        when(mockProp1.getSerializationType()).thenReturn(mock(JavaType.class));
        when(mock(JavaType.class).isContainerType()).thenReturn(false);
        when(mockProvier.findValueSerializer(any(), eq(mockProp1))).thenReturn(mockSerializer);
        serializer.call_resolve(mockProvider);
        verify(any).resolve(mockProvider);
    }

    // ---------- createContextual tests ----------

    @Test    public void testCreateContextualNoChanges() throws Exception {
        serializer = createSerializer(new BeanPropertyWriter[0], null);
        JsonSerializer<?> result = serializer.callCreateContextual(mockProvider, null);
        assertSame(serializer, result);
    }

    @Test    public void testCreateContextualEnumStringShape() throws Exception {
        when(mockType.isEnum()).thenReturn(true);
        when(mockMember.getMember()).thenReturn(mockMember);
        when(mockBeanProperty.getMember()).thenReturn(mockMember);
        // Setup format with shape STRING
        when(mockIntrospector.findFormat(any(Annotated.class))).thenReturn(
            new JsonFormat.Value().withShape(JsonFormat.Shape.STRING));
        // Constructor set _serializationShape to null
        serializer = createSerializer(new BeanPropertyWriter[0], null);
        when(mockConfig.introspectClassAnnotations(mockType)).thenReturn(mock(BeanDescription.class));
        JsonSerializer<?> enumSer = mock(JsonSerializer.class);
        // We need to stub EnumSerializer.construct, but it's static; we can't easily mock it.
        // Instead, we'll test that the branch leads to an attempt to create contextualized serializer.
        // Since we can't mock static, we'll just verify that provider.handlePrimaryContextualization is called.
        // We'll set up mockProvider to return a new serializer from handlePrimaryContextualization.
        JsonSerializer<?> retSer = mock(JsonSerializer.class);
        when(mockProvider.handlePrimaryContextualization(any(), eq(mockBeanProperty))).thenReturn(retSer);
        JsonSerializer<?> result = serializer.callCreateContextual(mockProvider, mockBeanProperty);
        assertNotSame(serializer, result);
        verify(mockProvider).handlePrimaryContextualization(any(), eq(mockBeanProperty));
    }

    @Test    public void testCreateContextualObjectIdWithPropertyGenerator() throws Exception {
        when(mockBeanProperty.getMember()).thenReturn(mockMember);
        // Setup ObjectIdInfo with PropertyGenerator
        ObjectIdInfo info = new ObjectIdInfo(
            PropertyName.construct("idProp"), null, null, ObjectIdGenerators.PropertyGenerator.class);
        when(mockIntrospector.findObjectIdInfo(mockMember)).thenReturn(info);
        // Need to mock provider methods for constructing type and objectIdGenerator
        when(mockProvider.constructType(ObjectIdGenerators.PropertyGenerator.class)).thenReturn(
            mock(JavaType.class));
        // The search for property in _props fails if no prop with that name exists
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        when(mockProp1.getName()).thenReturn("other");
        // Expect IllegalArgumentException
        try {
            serializer.callCreateContextual(mockProvider, mockBeanProperty);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test    public void testCreateContextualObjectIdWithPropertyGeneratorFound() throws Exception {
        when(mockBeanProperty.getMember()).thenReturn(mockMember);
        ObjectIdInfo info = new ObjectIdInfo(
            PropertyName.construct("idProp"), null, null, ObjectIdGenerators.PropertyGenerator.class);
        when(mockIntrospector.findObjectIdInfo(mockMember)).thenReturn(info);
        when(mockProvider.constructType(ObjectIdGenerators.PropertyGenerator.class)).thenReturn(
            mock(JavaType.class));
        // Setup property that matches
        when(mockProp2.getName()).thenReturn("idProp");
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1, mockProp2}, null);
        when(mockProp2.getType()).thenReturn(mock(JavaType.class));
        when(mockProvider.findValueSerializer(any(JavaType.class), any(BeanProperty.class))).thenReturn(mockSerializer);
        // Also stub objectIdGeneratorInstance and ObjectIdWriter.construct
        when(mockProvider.objectIdGeneratorInstance(mockMember, info)).thenReturn(mock(ObjectIdGenerator.class));
        when(mockProvider.findValueSerializer(any(JavaType.class), any(BeanProperty.class))).thenReturn(mock(JsonSerializer.class));
        // The createContextual should succeed and return a new serializer with an ObjectIdWriter
        JsonSerializer<?> result = serializer.callCreateContextual(mockProvider, mockBeanProperty);
        assertNotNull(result);
        assertNotSame(serializer, result);
        // Since we didn't set _serializationShape to ARRAY, result should be a BeanSerializerBase (the same as returned by withObjectIdWriter)
        assertTrue(result instanceof ConcreteBeanSerializer);
    }

    @Test    public void testCreateContextualWithIgnorals() throws Exception {
        when(mockBeanProperty.getMember()).thenReturn(mockMember);
        when(mockIntrospector.findPropertiesToIgnore(mockMember)).thenReturn(new String[]{"prop1"});
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1, mockProp2}, new BeanPropertyWriter[]{mockProp1, mockProp2});
        when(mockProp1.getName()).thenReturn("prop1");
        when(mockProp2.getName()).thenReturn("prop2");
        JsonSerializer<?> result = serializer.callCreateContextual(mockProvider, mockBeanProperty);
        assertNotSame(serializer, result);
        ConcreteBeanSerializer contextual = (ConcreteBeanSerializer) result;
        assertEquals(1, contextual._props.length);
        assertEquals("prop2", contextual._props[0].getName());
    }

    @Test    public void testCreateContextualWithFilterId() throws Exception {
        when(mockBeanProperty.getMember()).thenReturn(mockMember);
        when(mockIntrospector.findFilterId(mockMember)).thenReturn("newFilter");
        serializer = createSerializer(new BeanPropertyWriter[0], null);
        JsonSerializer<?> result = serializer.callCreateContextual(mockProvider, mockBeanProperty);
        assertNotSame(serializer, result);
        assertEquals("newFilter", ((ConcreteBeanSerializer) result)._propertyFilterId);
    }

    @Test    public void testCreateContextualArrayShape() throws Exception {
        when(mockBeanProperty.getMember()).thenReturn(mockMember);
        when(mockIntrospector.findFormat(any(Annotated.class))).thenReturn(
            new JsonFormat.Value().withShape(JsonFormat.Shape.ARRAY));
        // _serializationShape is set to null initially; shape from annotation overrides
        serializer = createSerializer(new BeanPropertyWriter[0], null);
        JsonSerializer<?> result = serializer.callCreateContextual(mockProvider, mockBeanProperty);
        // Since our asArraySerializer returns this, result should be same instance
        assertSame(serializer, result);
    }

    // ---------- serializeFields tests ----------

    @Test    public void testSerializeFieldsNormal() throws Exception {
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1, mockProp2}, null);
        serializer.callSerializeFields(testBean, mockGen, mockProvider);
        verify(mockProp1).serializeAsField(testBean, mockGen, mockProvider);
        verify(mockProp2).serializeAsField(testBean, mockGen, mockProvider);
    }

    @Test    public void testSerializeFieldsFilteredActiveView() throws Exception {
        BeanPropertyWriter[] filtered = {mockPropFilter1, mockPropFilter2};
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1, mockProp2}, filtered);
        when(mockProvider.getActiveView()).thenReturn(Object.class);
        serializer.callSerializeFields(testBean, mockGen, mockProvider);
        verify(mockPropFilter1).serailizeAsField(testBean, mockGen, mockProvider);
        verify(mockPropFilter2).serializeAsField(testBean, mockGen, mockProvider);
        verify(mockProp1, never()).serializeAsField(any(), any(), any());
    }

    @Test    public void testSerializeFieldsAnyGetterWriter() throws Exception {
        AnyGetterWriter any = mock(AnyGetterWriter.class);
        when(mockBuilder.getAnyGetter()).thenReturn(any);
        when(mockBuilder.getTypeId()).thenReturn(null);
        when(mockBuilder.getFilterId()).thenReturn(null);
        when(mockBuilder.getObjectIdWriter()).thenReturn(null);
        when(mockBuilder.getBeanDescription()).thenReturn(mock(BeanDescription.class);
        serializer = new ConcreteBeanSerializer(mockType, mockBuilder, new BeanPropertyWriter[0], null);
        seralizer.callSerializeFields(testBean, mockGen, mockProvider);
        verif(any).getAndSerialize(testBean, mockGen, mockProvider);
    }

    @Test    public void testSerailizeFieldsException() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        IOException ioe = new IOException("test");
        doThrow(ioe).when(mockProp1).serializeAsField( testBean, mockGen, mockProvider);
        try {
            seralizer.callSerializeFields(testBean, mockGen, mockProvider);
            fail("Excpected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("test"));
            assertEquals(testBean, e.getPath().get(0).getFrom());
        }
    }

    @Test    public void testSerializeFieldsStackOverflow() throws Exception {
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        doThrow(new StackOverflowError()).when(mockProp1).serializeAsField(testBean, mockGen, mockProvider);
        try {
            seralizer.callSerializeFields(testBean, mockGen, mockProvider);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Infinite recursion"));
        }
    }

    // ---------- serializeFieldsFiltered tests ----------

    @Test    public void testSerializeFieldsFilteredWithFilter() throws Exception {
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1, mockProp2}, null);
        // Setup provider to return a property filter
        when(mockProvider.findPropertyFilter(mockProvider, null, testBean)).thenReturn(mockPropertyFilter);
        serializer.callSerializeFieldsFiltered(testBean, mockGen, mockProvider);
        verify(mockPropertyFilter).serializeAsField(testBean, mockGen, mockProvider, mockProp1);
        verify(mockPropertyFilter).serializeAsField(testBean, mockGen, mockProvider, mockProp2);
    }

    @Test    public void testSerializeFieldsFilteredNullFilterFallsBack() throws Exception {
        serializer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        when(mockProvider.findPropertyFilter(any(), any(), any())).thenReturn(null);
        serializer.callSerializeFieldsFiltered(testBean, mockGen, mockProvider);
        verify(mockProp1).serializeAsField(testBean, mockGen, mockProvider); // because fallback
    }

    @Test    public void testSerializeFieldsFilteredAnyGetterFilter() throws Exception {
        AnyGetterWriter any = mock(AnyGetterWriter.class);
        when(mockBuilder.getAnyGetter()).thenReturn(any);
        when(mockBuilder.getTypeId()).thenReturn(null);
        when(mockBuilder.getFilterId()).thenReturn(null);
        when(mockBuilder.getObjectIdWriter()).thenReturn(null);
        when(mockBuilder.getBeanDescription()).thenReturn(mock(BeanDescription.class));
        serializer = new ConcreteBeanSerializer(mockType, mockBuilder, new BeanPropertyWriter[0], null);
        when(mockProvider.findPropertyFilter(mockProvider, null, testBean)).thenReturn(mockPropertyFilter);
        seralizer.callSerializeFieldsFiltered(testBean, mockGen, mockProvider);
        verif(any).getAndFilter(testBean, mockGen, mockProvider, mockPropertyFilter);
    }

    // ---------- _serializeWithObjectId tests ----------

    @Test    public void test_serializeWithObjectIdAlreadyId() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[0], null);
        ObjectIdWriter oiw = mock(ObectIdWriter.class);
        when(oie.generator).thenReturn(mock(ObjectIdGenerator.class);
        // Need to set _objectIdWriter
        seralizer._objectIdWriter = oiw;
        WritableObjectId oid = mockWritableObjectId;
        when(mockProvider.findObjectId(testBean, oiw.generator)).thenReturn(oid);
        when(oid.writeAsId(mockGen, mockProvider, oiw)).thenReturn(true);
        seralizer.call_serializeWithObjectId(testBean, mockGen, mockProvider, true);
        verif(oid).writeAsId(mockGen, mockProvider, oiw);
        // no startObject should be called because it returned true
        verif(mockGen, never().writeStartObject();
    }

    @Test    public void test_serializeWithObjectIdNotWrittenStartObject() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        ObjectIdWriter oiw = mock(ObjectIdWriter.class);
        when(oi.generator).thenReturn(mock(ObjectIdGenerator.class));
        seralizer._objectIdWriter = oiw;
        WritableObjectId oid = mockWritableObjectId;
        when(mockProvider.findObjectId(testBean, oiw.generator)).thenReturn(oid);
        when(oid.writeAsId(mockGen, mockProvider, oiw)).thenReturn(false);
        // generateId returns some id
        when(oid.generateId(testBean)).thenReturn(new Object());
        seralizer.call_serializeWithObjectId(testBean, mockGen, mockProvider, true);
        verif(mockGen).writeStartObect(); // startEndObject true
        verif(oid).writeAsField(mockGen, mockProvider, oiw);
        // should call serializeFields
        verif(mockProp1).serializeAsField(testBean, mockGen, mockProvider);
        verif(mockGen).writeEndObject();
    }

    @Test    public void test_serializeWithObjectIdTypeSer() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        ObjectIdWriter oiw = mock(ObjectIdWriter.class);
        when(oie.generator).thenReturn(mock(ObjectIdGenerator.class));
        seralizer._objectIdWriter = oiw;
        WritableObjectId oid = mockWritableObjectId;
        when(mockProvider.findObjectId(testBean, oiw.generator)).thenReturn(oid);
        // should call _serializeObjectId
        seralizer.call_serializeWithObjectId(testBean, mockGen, mockProvider, mockTypeSer);
        verif(oid, times(1).writeAsId(any(), any(), any()); // first call in _serializeWithObjectId before serialization
    }

    // ---------- serialzeWithType tests ----------

    @Test    public void testSerializeWithTypeObjectId() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        ObjectIdWriter oiw = mock(ObjectIdWriter.class);
        when(oie.generator).thenReturn(mock(ObjectIdGenerator.class));
        seralizer._objectIdWriter = oiw;
        seralizer.serialzeWithType(testBean, mockGen, mockProvider, mockTypeSer);
        // should delegate to _serializeWithObjectId
        verif(seralizer).call_serializeWithObjectId(testBean, mockGen, mockProvider, mockTypeSer);
    }

    @Test    public void testSerializeWithTypeNoTypeId() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        // _typeId null
        seralizer.serializeWithType(testBean, mockGen, mockProvider, mockTypeSer);
        verif(mockTypeSer).writeTypePreffixForObject(testBean, mockGen);
        verif(mockTypeSer).writeTypeSuffixForObject(testBean, mockGen);
        verif(mockProp1).serializeAsField(testBean, mockGen, mockProvider);
    }

    @Test    public void testSerializeWithTypeCustomTypeId() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        AnotatedMember typeId = mock(AnnotatedMember.class);
        when(typeId.getValue(testBean)).thenReturn("custom");
        seralizer._typeId = typeId;
        seralizer.serializeWithType(testBean, mockGen, mockProvider, mockTypeSer);
        verif(mockTypeSer).writeCustomTypePrefixForObject(testBean, mockGen, "custom");
        verif(mockTypeSer).writeCustomTypeSuffixForObject(testBean, mockGen, "custom");
        verif(mockProp1).serializeAsField(testBean, mockGen, mockProvider);
    }

    // ---------- _customTypeId tests ----------

    @Test    public void test_customTypeIdNull() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[0], null);
        AnnotatedMember typeId = mock(AnnotatedMember.class);
        when(typeId.getValue(any()).thenReturn(null);
        seralizer._typeId = typeId;
        String result = seralizer._customTypeId(testBean);
        assertEquals("", result);
    }

    @Test    public void test_customTypeIdNonString() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[0], null);
        AnotatedMember typeId = mock(AnnotatedMember.class);
        when(typeId.getValue(testBean)).thenReturn(123);
        seralizer._typeId = typeId;
        String result = seralizer._customTypeId(testBean);
        assertEquals("123", result);
    }

    // ---------- getSchema tests ----------

    @Test    public void testGetSchemaBasic() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        // Mocking ObjectNode is complex, so we'll just call and verify no exception
        JsonNode schema = seralizer.getSchema(mockProvider, null);
        assertNotNull(schema);
    }

    @Test    public void testGetSchemaWithFilter() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        seralizer._propertyFilterId = "someId";
        when(mockProvider.findPropertyFilter(mockProvider, "someId", null)).thenReturn(mockPropertyFilter);
        JsonNode schema = seralizer.getSchema(mockProvider, null);
        assertNotNull(schema);
    }

    // ---------- acceptJsonFormatVisitor tests ----------

    @Test    public void testAcceptJsonFormatVisitorNull() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        seralizer.acceptJsonFormatVisitor(null, null); // should not throw
    }

    @Test    public void testAcceptJsonFormatVisitorNoObjectVisitor() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        JsonFormatVisitorWrapper wrapper = mock(JsonFormatVisitorWrapper.class);
        when(wrapper.expectObjectFormat(null)).thenReturn(null);
        seralizer.acceptJsonFormatVisitor(wrapper, null);
        // no error expected
    }

    @Test    public void testAcceptJsonFormatVisitorWithProps() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1, mockProp2}, null);
        JsonFormatVisitorWrapper wrapper = mock(JsonFormatVisitorWrapper.class);
        JsonObjectFormatVisitor objVisitor = mock(JsonObjectFormatVisitor.class);
        when(wrapper.expectObjectFormat(null)).thenReturn(objVisitor);
        seralizer.acceptJsonFormatVisitor(wrapper, null);
        verify(mockProp1).depositSchemaProperty(objVisitor);
        verify(mockProp2).depositSchemaProperty(objVisitor);
    }

    @Test    public void testAcceptJsonFormatVisitorWithFilter() throws Exception {
        seralizer = createSerializer(new BeanPropertyWriter[]{mockProp1}, null);
        seralizer._propertyFilterId = "filterId";
        JsonFormatVisitorWrapper wrapper = mock(JsonFormatVisitorWrapper.class);
        when(wrapper.getProvider()).thenReturn(mockProvider);
        when(mockProvider.findPropertyFilter(mockProvider, "filterId", null)).thenReturn(mockPropertyFilter);
        JsonObjectFormatVisitor objVisitor = mock(JsonObjectFormatVisitor.class);
        when(wrapper.expectObjectFormat(null)).thenReturn(objVisitor);
        seralizer.acceptJsonFormatVisitor(wrapper, null);
        verif(mockPropertyFilter).depositSchemaProperty(mockProp1, objVisitor, mockProvider);
    }
}
