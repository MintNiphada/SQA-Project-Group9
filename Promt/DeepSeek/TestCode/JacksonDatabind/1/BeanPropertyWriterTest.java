package com.fasterxml.jackson.databind.ser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

@SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class BeanPropertyWriterTest {

    @Mock
    private BeanPropertyDefinition propDef;
    @Mock
    private AnnotatedMember member;
    @Mock
    private Annotations contextAnnotations;
    @Mock
    private JavaType declaredType;
    @Mock
    private JsonSerializer<Object> serializer;
    @Mock
    private TypeSerializer typeSerializer;
    @Mock
    private JavaType serType;
    @Mock
    private JsonGenerator jgen;
    @Mock
    private SerializerProvider prov;
    @Mock
    private NameTransformer nameTransformer;
    @Mock
    private JsonObjectFormatVisitor objectVisitor;
    @Mock
    private JsonSerializer<Object> nullSerializer;
    @Mock
    private AnnotationIntrospector annotationIntrospector;
    @Mock
    private ObjectNode propertiesNode;
    @Mock
    private JsonNode schemaNode;

    private BeanPropertyWriter writer;
    private Object bean = new Object();
    private Object mockValue = new Object(); // representative value

    @Before
    public void setUp() throws Exception {
        // Default setup for a typical method-based property
        when(propDef.getName()).thenReturn("testProp");
        when(propDef.isRequired()).thenReturn(true);
        when(propDef.findViews()).thenReturn(null);
        when(propDef.getWrapperName()).thenReturn(PropertyName.NO_NAME);
        // member is AnnotatedMethod for tests
        AnnotatedMethod mockMethod = mock(AnnotatedMethod.class);
        Method realMethod = this.getClass().getMethod("setUp");
        when(mockMethod.getMember()).thenReturn(realMethod);
        when(mockMethod.getAnnotation(any(Class.class))).thenReturn(null);
        // General member mocks: getAnnotation, getMember
        when(member.getAnnotation(any(Class.class))).thenReturn(null);
        when(member.getMember()).thenReturn(realMethod);
        when(member instanceof AnnotatedMethod).thenReturn(true);
        when(member instanceof AnnotatedField).thenReturn(false);
        when(contextAnnotations.get(any(Class.class))).thenReturn(null);

        // Initialize writer with serializer null to trigger dynamic handling
        writer = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
    }

    // ---------- Constructor Tests ----------
    @Test
    public void testConstructorWithSerializer() {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, serializer, typeSerializer, serType, false, null);
        assertNotNull(w.getSerializer());
        assertEquals(serializer, w.getSerializer());
    }

    @Test
    public void testConstructorWithNullSerializer() {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        assertNull(w.getSerializer());
    }

    @Test
    public void testConstructorSetsSuppressNulls() {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, serializer, typeSerializer, serType, true, null);
        assertTrue(w.willSuppressNulls());
        w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, serializer, typeSerializer, serType, false, null);
        assertFalse(w.willSuppressNulls());
    }

    @Test
    public void testConstructorSetsSuppressableValue() {
        Object val = new Object();
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, val);
        assertNotNull(w._suppressableValue);
        assertEquals(val, w._suppressableValue);
    }

    @Test
    public void testConstructorWithAnnotatedField() throws Exception {
        AnnotatedField mockField = mock(AnnotatedField.class);
        Field someField = this.getClass().getDeclaredField("mockValue");
        when(mockField.getMember()).thenReturn(someField);
        when(mockField.getAnnotation(any(Class.class))).thenReturn(null);
        when(mockField instanceof AnnotatedField).thenReturn(true);
        when(mockField instanceof AnnotatedMethod).thenReturn(false);
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, mockField, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        assertNotNull(w._field);
        assertNull(w._accessorMethod);
        assertEquals(someField, w._field);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithUnknownMemberThrows() {
        AnnotatedMember unknown = mock(AnnotatedMember.class);
        when(propDef.getName()).thenReturn("x");
        new BeanPropertyWriter(propDef, unknown, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
    }

    @Test
    public void testCopyConstructor() {
        BeanPropertyWriter orig = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, serializer, typeSerializer, serType, true, null);
        BeanPropertyWriter copy = new BeanPropertyWriter(orig);
        assertEquals(orig._name, copy._name);
        assertEquals(orig._wrapperName, copy._wrapperName);
        assertEquals(orig._member, copy._member);
        assertEquals(orig._contextAnnotations, copy._contextAnnotations);
        assertEquals(orig._declaredType, copy._declaredType);
        assertEquals(orig._accessorMethod, copy._accessorMethod);
        assertEquals(orig._field, copy._field);
        assertEquals(orig._serializer, copy._serializer);
        assertNull(copy._nullSerializer); // nullSerializer not copied?
        assertTrue(copy.willSuppressNulls());
        assertEquals(orig._suppressableValue, copy._suppressableValue);
        assertArrayEquals(orig._includeInViews, copy._includeInViews);
        assertEquals(orig._typeSerializer, copy._typeSerializer);
        assertEquals(orig._nonTrivialBaseType, copy._nonTrivialBaseType);
        assertEquals(orig._isRequired, copy._isRequired);
    }

    @Test
    public void testCopyConstructorWithInternalSettings() {
        BeanPropertyWriter orig = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        orig.setInternalSetting("key", "value");
        BeanPropertyWriter copy = new BeanPropertyWriter(orig);
        assertNotNull(copy.getInternalSetting("key"));
        assertEquals("value", copy.getInternalSetting("key"));
        assertNotSame(orig._internalSettings, copy._internalSettings); // defensive copy
    }

    @Test
    public void testCopyConstructorWithName() {
        BeanPropertyWriter orig = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, serializer, typeSerializer, serType, false, null);
        SerializedString newName = new SerializedString("newName");
        BeanPropertyWriter copy = new BeanPropertyWriter(orig, newName);
        assertEquals(newName, copy._name);
        assertEquals(orig._wrapperName, copy._wrapperName);
    }

    // ---------- rename tests ----------
    @Test
    public void testRenameWithDifferentName() {
        when(nameTransformer.transform("testProp")).thenReturn("other");
        BeanPropertyWriter renamed = writer.rename(nameTransformer);
        assertNotSame(writer, renamed);
        assertEquals("other", renamed.getName());
    }

    @Test
    public void testRenameWithSameName() {
        when(nameTransformer.transform("testProp")).thenReturn("testProp");
        BeanPropertyWriter renamed = writer.rename(nameTransformer);
        assertSame(writer, renamed);
    }

    // ---------- assignSerializer / assignNullSerializer tests ----------
    @Test
    public void testAssignSerializerWhenNull() {
        writer.assignSerializer(serializer);
        assertEquals(serializer, writer.getSerializer());
    }

    @Test(expected = IllegalStateException.class)
    public void testAssignSerializerWhenAlreadySet() {
        writer.assignSerializer(serializer);
        JsonSerializer<Object> other = mock(JsonSerializer.class);
        writer.assignSerializer(other);
    }

    @Test
    public void testAssignSerializerSameInstance() {
        writer.assignSerializer(serializer);
        writer.assignSerializer(serializer); // should not throw
    }

    @Test
    public void testAssignNullSerializerWhenNull() {
        writer.assignNullSerializer(nullSerializer);
        assertTrue(writer.hasNullSerializer());
    }

    @Test(expected = IllegalStateException.class)
    public void testAssignNullSerializerWhenAlreadySet() {
        writer.assignNullSerializer(nullSerializer);
        writer.assignNullSerializer(mock(JsonSerializer.class));
    }

    @Test
    public void testAssignNullSerializerSameInstance() {
        writer.assignNullSerializer(nullSerializer);
        writer.assignNullSerializer(nullSerializer); // no error
    }

    // ---------- unwrappingWriter test ----------
    @Test
    public void testUnwrappingWriter() {
        NameTransformer unwrapper = mock(NameTransformer.class);
        BeanPropertyWriter unwrapped = writer.unwrappingWriter(unwrapper);
        assertNotNull(unwrapped);
        // Since we can't check exact class without knowing implementation, verify it's a subclass
        assertTrue(unwrapped instanceof com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter);
    }

    // ---------- setNonTrivialBaseType test ----------
    @Test
    public void testSetNonTrivialBaseType() {
        JavaType t = mock(JavaType.class);
        writer.setNonTrivialBaseType(t);
        assertEquals(t, writer._nonTrivialBaseType);
    }

    // ---------- BeanProperty implementation tests ----------
    @Test
    public void testGetName() {
        assertEquals("testProp", writer.getName());
    }

    @Test
    public void testGetType() {
        assertEquals(declaredType, writer.getType());
    }

    @Test
    public void testGetWrapperName() {
        assertEquals(PropertyName.NO_NAME, writer.getWrapperName());
    }

    @Test
    public void testIsRequired() {
        assertTrue(writer.isRequired());
    }

    @Test
    public void testGetAnnotation() {
        when(member.getAnnotation(Override.class)).thenReturn(mock(Override.class));
        assertNotNull(writer.getAnnotation(Override.class));
    }

    @Test
    public void testGetContextAnnotation() {
        when(contextAnnotations.get(SuppressWarnings.class)).thenReturn(mock(SuppressWarnings.class));
        assertNotNull(writer.getContextAnnotation(SuppressWarnings.class));
    }

    @Test
    public void testGetMember() {
        assertEquals(member, writer.getMember());
    }

    @Test
    public void testDepositSchemaPropertyWithNullVisitor() throws Exception {
        writer.depositSchemaProperty(null); // should do nothing
    }

    @Test
    public void testDepositSchemaPropertyRequired() throws Exception {
        when(propDef.isRequired()).thenReturn(true);
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, true, null);
        w.depositSchemaProperty(objectVisitor);
        verify(objectVisitor).property(w);
        verify(objectVisitor, never()).optionalProperty(any(BeanProperty.class));
    }

    @Test
    public void testDepositSchemaPropertyOptional() throws Exception {
        when(propDef.isRequired()).thenReturn(false);
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        w.depositSchemaProperty(objectVisitor);
        verify(objectVisitor).optionalProperty(w);
        verify(objectVisitor, never()).property(any(BeanProperty.class));
    }

    // ---------- Internal settings tests ----------
    @Test
    public void testGetInternalSettingWhenMapNull() {
        assertNull(writer.getInternalSetting("key"));
    }

    @Test
    public void testSetInternalSettingCreatesMapAndPuts() {
        writer.setInternalSetting("key", "value");
        assertEquals("value", writer.getInternalSetting("key"));
        assertNotNull(writer._internalSettings);
    }

    @Test
    public void testSetInternalSettingOverwrites() {
        writer.setInternalSetting("key", "old");
        Object old = writer.setInternalSetting("key", "new");
        assertEquals("old", old);
        assertEquals("new", writer.getInternalSetting("key"));
    }

    @Test
    public void testRemoveInternalSetting() {
        writer.setInternalSetting("key", "value");
        Object removed = writer.removeInternalSetting("key");
        assertEquals("value", removed);
        assertNull(writer.getInternalSetting("key"));
        assertNull(writer._internalSettings); // map cleared
    }

    @Test
    public void testRemoveInternalSettingWhenMapNull() {
        assertNull(writer.removeInternalSetting("key"));
    }

    // ---------- Accessor tests ----------
    @Test
    public void testHasSerializer() {
        assertFalse(writer.hasSerializer());
        writer.assignSerializer(serializer);
        assertTrue(writer.hasSerializer());
    }

    @Test
    public void testHasNullSerializer() {
        assertFalse(writer.hasNullSerializer());
        writer.assignNullSerializer(nullSerializer);
        assertTrue(writer.hasNullSerializer());
    }

    @Test
    public void testWillSuppressNulls() {
        assertFalse(writer.willSuppressNulls());
        when(propDef.getName()).thenReturn("p");
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, true, null);
        assertTrue(w.willSuppressNulls());
    }

    @Test
    public void testGetSerializationType() {
        assertEquals(serType, writer.getSerializationType());
    }

    @Test
    public void testGetRawSerializationType() {
        when(serType.getRawClass()).thenReturn(String.class);
        assertEquals(String.class, writer.getRawSerializationType());
    }

    @Test
    public void testGetRawSerializationTypeWhenNull() {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, null, false, null);
        assertNull(w.getRawSerializationType());
    }

    @Test
    public void testGetPropertyTypeFromMethod() throws Exception {
        // _accessorMethod is set from member.getMember() as a Method (setUp method)
        Class<?> propType = writer.getPropertyType();
        assertNotNull(propType);
        // The type will be void because setUp returns void; but it's a class.
        assertEquals(void.class, propType);
    }

    @Test
    public void testGetPropertyTypeFromField() throws Exception {
        AnnotatedField mockField = mock(AnnotatedField.class);
        Field field = this.getClass().getDeclaredField("mockValue");
        when(mockField.getMember()).thenReturn(field);
        when(mockField instanceof AnnotatedField).thenReturn(true);
        when(mockField instanceof AnnotatedMethod).thenReturn(false);
        when(mockField.getAnnotation(any(Class.class))).thenReturn(null);
        when(propDef.getName()).thenReturn("fProp");
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, mockField, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        assertEquals(Object.class, w.getPropertyType()); // mockValue is Object
    }

    @Test
    public void testGetGenericPropertyTypeMethod() {
        Type genType = writer.getGenericPropertyType();
        assertNotNull(genType);
        // setUp returns void
        assertEquals(void.class, genType);
    }

    @Test
    public void testGetGenericPropertyTypeField() throws Exception {
        AnnotatedField mockField = mock(AnnotatedField.class);
        Field field = this.getClass().getDeclaredField("mockValue");
        when(mockField.getMember()).thenReturn(field);
        when(mockField instanceof AnnotatedField).thenReturn(true);
        when(mockField instanceof AnnotatedMethod).thenReturn(false);
        when(mockField.getAnnotation(any(Class.class))).thenReturn(null);
        when(propDef.getName()).thenReturn("gfProp");
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, mockField, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        assertEquals(Object.class, w.getGenericPropertyType()); // field type
    }

    @Test
    public void testGetViews() {
        assertNull(writer.getViews()); // propDef.findViews() returned null
    }

    @Test
    public void testGetViewsWhenSet() {
        Class<?>[] views = new Class[]{Object.class};
        when(propDef.findViews()).thenReturn(views);
        when(propDef.getName()).thenReturn("v");
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        assertArrayEquals(views, w.getViews());
    }

    // deprecated isRequired
    @Test
    public void testDeprecatedIsRequired() {
        assertTrue(writer.isRequired(annotationIntrospector));
    }

    // ---------- serializeAsField tests ----------
    @Test
    public void testSerializeAsFieldNullValueWithNullSerializer() throws Exception {
        // value is null, _nullSerializer null => return
        writer.serializeAsField(bean, jgen, prov);
        verify(jgen, never()).writeFieldName(anyString());
    }

    @Test
    public void testSerializeAsFieldNullValueWithNullSerializerSet() throws Exception {
        writer.assignNullSerializer(nullSerializer);
        writer.serializeAsField(bean, jgen, prov);
        verify(jgen).writeFieldName(writer._name);
        verify(nullSerializer).serialize(null, jgen, prov);
    }

    @Test
    public void testSerializeAsFieldNonNullValueNoStaticSerializer() throws Exception {
        // arrange bean value to be non-null, and _serializer = null, so dynamic lookup
        Object value = "hello";
        writer = spy(writer); // to stub get()
        doReturn(value).when(writer).get(bean);
        // mock dynamic serializer map to return a serializer
        PropertySerializerMap map = mock(PropertySerializerMap.class);
        JsonSerializer<Object> dynSer = mock(JsonSerializer.class);
        when(map.serializerFor(String.class)).thenReturn(dynSer);
        setField(writer, "_dynamicSerializers", map);
        writer.serializeAsField(bean, jgen, prov);
        verify(jgen).writeFieldName(writer._name);
        verify(dynSer).serialize(value, jgen, prov);
        verify(typeSerializer, never()).serializeWithType(any(), eq(jgen), eq(prov), any(TypeSerializer.class));
    }

    @Test
    public void testSerializeAsFieldWithTypeSerializer() throws Exception {
        writer.assignSerializer(serializer);
        Object value = new Object();
        writer = spy(writer);
        doReturn(value).when(writer).get(bean);
        writer.serializeAsField(bean, jgen, prov);
        verify(serializer).serializeWithType(value, jgen, prov, typeSerializer);
    }

    @Test
    public void testSerializeAsFieldWithSuppressableDefaultValue() throws Exception {
        Object suppressVal = "suppressMe";
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, suppressVal);
        w.assignSerializer(serializer);
        w = spy(w);
        doReturn(suppressVal).when(w).get(bean);
        w.serializeAsField(bean, jgen, prov);
        verify(jgen, never()).writeFieldName(anyString()); // suppressed
    }

    @Test
    public void testSerializeAsFieldWithSuppressableMarkerForEmpty() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false,
                BeanPropertyWriter.MARKER_FOR_EMPTY);
        w.assignSerializer(serializer);
        when(serializer.isEmpty(any())).thenReturn(true);
        w = spy(w);
        Object value = "nonNull";
        doReturn(value).when(w).get(bean);
        w.serializeAsField(bean, jgen, prov);
        verify(jgen, never()).writeFieldName(anyString());
    }

    @Test
    public void testSerializeAsFieldWithSuppressableMarkerForEmptyButNotEmpty() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false,
                BeanPropertyWriter.MARKER_FOR_EMPTY);
        w.assignSerializer(serializer);
        when(serializer.isEmpty(any())).thenReturn(false);
        w = spy(w);
        Object value = "nonNull";
        doReturn(value).when(w).get(bean);
        w.serializeAsField(bean, jgen, prov);
        verify(jgen).writeFieldName(w._name);
        verify(serializer).serializeWithType(value, jgen, prov, typeSerializer);
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializeAsFieldSelfReferenceNoObjectId() throws Exception {
        writer.assignSerializer(serializer);
        writer = spy(writer);
        doReturn(bean).when(writer).get(bean);
        when(serializer.usesObjectId()).thenReturn(false);
        writer.serializeAsField(bean, jgen, prov);
    }

    @Test
    public void testSerializeAsFieldSelfReferenceWithObjectId() throws Exception {
        writer.assignSerializer(serializer);
        writer = spy(writer);
        doReturn(bean).when(writer).get(bean);
        when(serializer.usesObjectId()).thenReturn(true);
        try {
            writer.serializeAsField(bean, jgen, prov);
            verify(jgen).writeFieldName(writer._name);
            verify(serializer).serializeWithType(bean, jgen, prov, typeSerializer);
        } catch (JsonMappingException e) {
            fail("Should not have thrown");
        }
    }

    // ---------- serializeAsColumn tests ----------
    @Test
    public void testSerializeAsColumnNullValueNullSerializer() throws Exception {
        writer.serializeAsColumn(bean, jgen, prov);
        verify(jgen).writeNull();
    }

    @Test
    public void testSerializeAsColumnNullValueWithNullSerializer() throws Exception {
        writer.assignNullSerializer(nullSerializer);
        writer.serializeAsColumn(bean, jgen, prov);
        verify(nullSerializer).serialize(null, jgen, prov);
    }

    @Test
    public void testSerializeAsColumnSuppressDefaultValue() throws Exception {
        Object suppressVal = "suppress";
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, suppressVal);
        w.assignSerializer(serializer);
        w = spy(w);
        doReturn(suppressVal).when(w).get(bean);
        // will call serializeAsPlaceholder
        w.serializeAsColumn(bean, jgen, prov);
        // since _nullSerializer is null, it should call jgen.writeNull() within serializeAsPlaceholder
        verify(jgen).writeNull();
        verify(serializer, never()).serialize(any(), eq(jgen), eq(prov));
    }

    @Test
    public void testSerializeAsColumnSuppressMarkerEmpty() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false,
                BeanPropertyWriter.MARKER_FOR_EMPTY);
        w.assignSerializer(serializer);
        when(serializer.isEmpty(any())).thenReturn(true);
        w = spy(w);
        Object value = "notNull";
        doReturn(value).when(w).get(bean);
        w.serializeAsColumn(bean, jgen, prov);
        // should call serializeAsPlaceholder -> jgen.writeNull
        verify(jgen).writeNull();
    }

    @Test
    public void testSerializeAsColumnNonSuppress() throws Exception {
        writer.assignSerializer(serializer);
        writer = spy(writer);
        Object value = "columnValue";
        doReturn(value).when(writer).get(bean);
        writer.serializeAsColumn(bean, jgen, prov);
        verify(serializer).serializeWithType(value, jgen, prov, typeSerializer);
    }

    // ---------- serializeAsPlaceholder tests ----------
    @Test
    public void testSerializeAsPlaceholderWithoutNullSerializer() throws Exception {
        writer.serializeAsPlaceholder(bean, jgen, prov);
        verify(jgen).writeNull();
    }

    @Test
    public void testSerializeAsPlaceholderWithNullSerializer() throws Exception {
        writer.assignNullSerializer(nullSerializer);
        writer.serializeAsPlaceholder(bean, jgen, prov);
        verify(nullSerializer).serialize(null, jgen, prov);
    }

    // ---------- _findAndAddDynamic tests (indirectly via serializeAsField) ----------
    @Test
    public void testDynamicSerializerWithNonTrivialBaseType() throws Exception {
        // set _nonTrivialBaseType to trigger specialized type path
        JavaType nonTrivial = mock(JavaType.class);
        writer.setNonTrivialBaseType(nonTrivial);
        PropertySerializerMap map = mock(PropertySerializerMap.class);
        PropertySerializerMap.SerializerAndMapResult result = mock(PropertySerializerMap.SerializerAndMapResult.class);
        when(result.serializer).thenReturn(serializer);
        when(result.map).thenReturn(map);
        when(map.serializerFor(String.class)).thenReturn(null);
        when(map.findAndAddSerializer(any(JavaType.class), eq(prov), eq(writer))).thenReturn(result);
        setField(writer, "_dynamicSerializers", map);
        // simulate get returns non-null String
        writer = spy(writer);
        doReturn("hello").when(writer).get(bean);
        writer.serializeAsField(bean, jgen, prov);
        verify(prov).constructSpecializedType(nonTrivial, String.class);
        verify(map).findAndAddSerializer(any(JavaType.class), eq(prov), eq(writer));
        verify(jgen).writeFieldName(writer._name);
        verify(serializer).serialize("hello", jgen, prov);
    }

    @Test
    public void testDynamicSerializerWithoutNonTrivialBaseType() throws Exception {
        PropertySerializerMap map = mock(PropertySerializerMap.class);
        PropertySerializerMap.SerializerAndMapResult result = mock(PropertySerializerMap.SerializerAndMapResult.class);
        when(result.serializer).thenReturn(serializer);
        when(result.map).thenReturn(map);
        when(map.serializerFor(String.class)).thenReturn(null);
        when(map.findAndAddSerializer(String.class, prov, writer)).thenReturn(result);
        setField(writer, "_dynamicSerializers", map);
        writer = spy(writer);
        doReturn("world").when(writer).get(bean);
        writer.serializeAsField(bean, jgen, prov);
        verify(map).findAndAddSerializer(String.class, prov, writer);
        verify(jgen).writeFieldName(writer._name);
        verify(serializer).serialize("world", jgen, prov);
    }

    // ---------- depositSchemaProperty(ObjectNode) tests (complex) ----------
    @Test
    @SuppressWarnings("deprecation")
    public void testDepositSchemaPropertyWithStaticSerializerSchemaAware() throws Exception {
        // serializer is set, and is SchemaAware
        JsonSerializer<Object> schemaAwareSer = mock(JsonSerializer.class, withSettings().extraInterfaces(SchemaAware.class));
        when(propDef.getName()).thenReturn("schemaProp");
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, schemaAwareSer, typeSerializer, serType, false, null);
        when(prov.findValueSerializer(any(Class.class), eq(w))).thenReturn(schemaAwareSer);
        when(((SchemaAware) schemaAwareSer).getSchema(prov, serType.getRawClass(), true)).thenReturn(schemaNode);
        when(serType.getRawClass()).thenReturn(String.class);
        w.depositSchemaProperty(propertiesNode, prov);
        verify(propertiesNode).put("schemaProp", schemaNode);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDepositSchemaPropertyNoStaticSerializer() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations, declaredType, null, typeSerializer, serType, false, null);
        when(prov.findValueSerializer(serType.getRawClass(), w)).thenReturn(serializer);
        when(serType.getRawClass()).thenReturn(String.class);
        JsonNode defaultNode = mock(JsonNode.class);
        // can't mock static method easily; we will just ensure that the put is called with getName() and some node.
        // Since JsonSchema.getDefaultSchemaNode() is a static, we can use PowerMock, but we avoid. Instead, we'll just verify interaction.
        // We'll rely on the fact that serializer is not SchemaAware, so default node is used.
        // We'll just test that serializer is called and propertiesNode.put invoked.
        w.depositSchemaProperty(propertiesNode, prov);
        verify(propertiesNode).put(eq("testProp"), any(JsonNode.class));
    }

    // ---------- toString test ----------
    @Test
    public void testToString() {
        String str = writer.toString();
        assertNotNull(str);
        assertTrue(str.contains("testProp"));
    }

    // ---------- Helper to set private fields via reflection ----------
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field f = BeanPropertyWriter.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}