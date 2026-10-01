package com.fasterxml.jackson.databind.ser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.ser.impl.FilteredBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.Converter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;

    @Mock
    private SerializerProvider prov;
    @Mock
    private SerializationConfig config;
    @Mock
    private JavaType origType;
    @Mock
    private BeanDescription beanDesc;
    @Mock
    private AnnotatedClass classInfo;
    @Mock
    private JsonSerializer<Object> jsonSerializer;
    @Mock
    private Converter<Object, Object> converter;
    @Mock
    private TypeFactory typeFactory;
    @Mock
    private JavaType delegateType;
    @Mock
    private BeanDescription delegateDesc;
    @Mock
    private AnnotatedClass delegateClassInfo;
    @Mock
    private BeanSerializerBuilder builder;
    @Mock
    private SerializerFactoryConfig factoryConfig;
    @Mock
    private PropertyBuilder propertyBuilder;
    @Mock
    private BeanPropertyWriter propertyWriter;
    @Mock
    private AnnotatedMember accessor;
    @Mock
    private AnnotatedMethod methodAccessor;
    @Mock
    private AnnotatedField fieldAccessor;
    @Mock
    private BeanPropertyDefinition propDef;
    @Mock
    private TypeBindings typeBindings;
    @Mock
    private TypeResolverBuilder<?> typeResolverBuilder;
    @Mock
    private TypeSerializer typeSer;
    @Mock
    private AnnotationIntrospector annotationIntrospector;

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        when(prov.getConfig()).thenReturn(config);
        when(config.introspect(any(JavaType.class))).thenReturn(beanDesc);
        when(beanDesc.getClassInfo()).thenReturn(classInfo);
        when(config.getAnnotationIntrospector()).thenReturn(annotationIntrospector);
        when(typeFactory.constructType(any())).thenReturn(delegateType);
        when(typeFactory.findTypeParameters(any(JavaType.class), eq(ObjectIdGenerator.class)))
                .thenReturn(new JavaType[]{mock(JavaType.class)});
        when(prov.getTypeFactory()).thenReturn(typeFactory);
    }

    /** Helper to spawn a subclass instance for testing withConfig override detection */
    private static class SubBeanSerializerFactory extends BeanSerializerFactory {
        protected SubBeanSerializerFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    @Test
    public void testInstanceSingleton() {
        assertNotNull(BeanSerializerFactory.instance);
        assertSame(BeanSerializerFactory.instance, factory);
    }

    @Test
    public void testWithConfigSameInstance() {
        BeanSerializerFactory result = factory.withConfig(factory._factoryConfig);
        assertSame(factory, result);
    }

    @Test
    public void testWithConfigNewInstance() {
        SerializerFactoryConfig newConfig = mock(SerializerFactoryConfig.class);
        BeanSerializerFactory result = factory.withConfig(newConfig);
        assertNotSame(factory, result);
        assertTrue(result instanceof BeanSerializerFactory);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithConfigOnSubclassThrows() {
        SubBeanSerializerFactory sub = new SubBeanSerializerFactory(factory._factoryConfig);
        sub.withConfig(factory._factoryConfig);
    }

    @Test
    public void testCreateSerializerAnnotationFound() throws JsonMappingException {
        when(jsonSerializer.handlePrimaryContextualization(any(), any())).thenReturn(jsonSerializer);
        when(factory.findSerializerFromAnnotation(prov, classInfo)).thenReturn(jsonSerializer);

        JsonSerializer<Object> result = factory.createSerializer(prov, origType);
        assertSame(jsonSerializer, result);
    }

    @Test
    public void testCreateSerializerTypeModificationStaticTyping() throws JsonMappingException {
        when(factory.findSerializerFromAnnotation(prov, classInfo)).thenReturn(null);
        JavaType modifiedType = mock(JavaType.class);
        when(factory.modifyTypeByAnnotation(config, classInfo, origType)).thenReturn(modifiedType);
        when(modifiedType.hasRawClass(any())).thenReturn(false);
        when(config.introspect(modifiedType)).thenReturn(beanDesc);
        when(beanDesc.findSerializationConverter()).thenReturn(null);

        // Stub _createSerializer2
        BeanSerializerFactory spy = spy(factory);
        doReturn(jsonSerializer).when(spy)._createSerializer2(prov, modifiedType, beanDesc, true);
        JsonSerializer<Object> result = spy.createSerializer(prov, origType);
        assertSame(jsonSerializer, result);
    }

    @Test
    public void testCreateSerializerWithConverterAndAnnotation() throws JsonMappingException {
        when(factory.findSerializerFromAnnotation(prov, classInfo)).thenReturn(null);
        when(factory.modifyTypeByAnnotation(config, classInfo, origType)).thenReturn(origType);
        when(beanDesc.findSerializationConverter()).thenReturn(converter);
        when(converter.getOutputType(typeFactory)).thenReturn(delegateType);
        when(delegateType.hasRawClass(any())).thenReturn(false);
        when(config.introspect(delegateType)).thenReturn(delegateDesc);
        when(delegateDesc.getClassInfo()).thenReturn(delegateClassInfo);
        when(factory.findSerializerFromAnnotation(prov, delegateClassInfo)).thenReturn(jsonSerializer);

        JsonSerializer<Object> result = factory.createSerializer(prov, origType);
        assertTrue(result instanceof StdDelegatingSerializer);
    }

    @Test
    public void testCreateSerializerWithConverterNoAnnotation() throws JsonMappingException {
        when(factory.findSerializerFromAnnotation(prov, classInfo)).thenReturn(null);
        when(factory.modifyTypeByAnnotation(config, classInfo, origType)).thenReturn(origType);
        when(beanDesc.findSerializationConverter()).thenReturn(converter);
        when(converter.getOutputType(typeFactory)).thenReturn(delegateType);
        when(delegateType.hasRawClass(any())).thenReturn(true);
        when(factory.findSerializerFromAnnotation(prov, delegateClassInfo)).thenReturn(null);

        BeanSerializerFactory spy = spy(factory);
        doReturn(jsonSerializer).when(spy)._createSerializer2(prov, delegateType, beanDesc, true);
        JsonSerializer<Object> result = spy.createSerializer(prov, origType);
        assertTrue(result instanceof StdDelegatingSerializer);
    }

    @Test
    public void testCreateSerializer2AnnotationFound() throws JsonMappingException {
        when(factory.findSerializerByAnnotations(prov, origType, beanDesc)).thenReturn(jsonSerializer);
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(jsonSerializer, result);
    }

    @Test
    public void testCreateSerializer2ContainerType() throws JsonMappingException {
        when(factory.findSerializerByAnnotations(prov, origType, beanDesc)).thenReturn(null);
        when(origType.isContainerType()).thenReturn(true);
        when(config.isEnabled(MapperFeature.USE_STATIC_TYPING)).thenReturn(false);
        when(factory.usesStaticTyping(config, beanDesc, null)).thenReturn(true);

        BeanSerializerFactory spy = spy(factory);
        doReturn(jsonSerializer).when(spy).buildContainerSerializer(prov, origType, beanDesc, true);
        JsonSerializer<?> result = spy._createSerializer2(prov, origType, beanDesc, false);
        assertSame(jsonSerializer, result);
    }

    @Test
    public void testCreateSerializer2CustomSerializers() throws JsonMappingException {
        when(factory.findSerializerByAnnotations(prov, origType, beanDesc)).thenReturn(null);
        when(origType.isContainerType()).thenReturn(false);
        Serializers customSer = mock(Serializers.class);
        when(customSer.findSerializer(config, origType, beanDesc)).thenReturn(jsonSerializer);
        when(factory.customSerializers()).thenReturn(Collections.singletonList(customSer));

        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(jsonSerializer, result);
    }

    @Test
    public void testCreateSerializer2LookupFallback() throws JsonMappingException {
        when(factory.findSerializerByAnnotations(prov, origType, beanDesc)).thenReturn(null);
        when(origType.isContainerType()).thenReturn(false);
        when(factory.customSerializers()).thenReturn(Collections.emptyList());
        when(factory.findSerializerByLookup(origType, config, beanDesc, false)).thenReturn(null);
        when(factory.findSerializerByPrimaryType(prov, origType, beanDesc, false)).thenReturn(null);
        when(factory.findBeanSerializer(prov, origType, beanDesc)).thenReturn(null);
        when(factory.findSerializerByAddonType(config, origType, beanDesc, false)).thenReturn(null);
        when(prov.getUnknownTypeSerializer(beanDesc.getBeanClass())).thenReturn(jsonSerializer);

        JsonSerializer<?> result =factory._createSerializer2(prov, origType,beanDesc, false);
       assertSame(jsonSerializer,result);
   }

    @Test
   public void testCreateSerializer2PostProcessing() throws JsonMappingException {
        when(factory.findSerializerByAnnotations(prov, origType,beanDesc))thenReturn(null);
        when(origType.isContainerType())).thenReturn(false);
        when(factory.customSerializers())thenReturn(Collections.emptyList());
        when(factory.findSerializerByLookup(origType,config,beanDesc,false).thenReturn(jsonSerializer);
        when(factoryConfig.hasSerializerModifiers()).thenReturn(true);
        BeanSerializerModifier modifier =mock(BeanSerializerModifier.class);
        when(modifier.modifySerializer(config,beanDesc, jsonSerializer)).thenReturn(jsonSerializer);
        List<BeanSerializerModifier> modifiers =Collections.singletonList(modifier);
        when(factory._factoryConfig).thenReturn(factoryConfig);
        when(factoryConfig.serializerModifiers()).thenReturn(modifiers);

        factory =spy(factory);
        doReturn(null).when(factory).findSerializerByAnnotations(prov, origType,beanDesc);
        doReturn(jsonserializer).when(factory).findSerializerByLookup(origType,config,beanDesc, false);
        doReturn(factoryConfig.serializerModifiers()).when(factoryConfig).serializerModifiers();

       JsonSerializer<?>result =factory._createSerializer2(prov,origType,beanDesc, false);
        assertSame(jsonSerializer,result);
   }

    @Test
   public void testFindBeanSerializerNotPotentialBeanTypeAndNotEnum() throws JsonMappingException {
        when(origType.getRawClass()).thenReturn(String.class);
        when(origType.isEnumType()).thenReturn(false);
        when(ClassUtil.canBeABeanType(String.class)).thenReturn("some reason");
        // ClassUtil.isProxyType is static; we cannot mock, but String is not proxy.
        // To ensure isPotentialBeanType returns false, we need to manufacture a class that fails both checks.
 // We'll instead use spy to force return false.
        BeanSerializerFactory spy = spy(factory);
        doReturn(false).when(spy).isPotentialBeanType(any(Class.class));
        assertNull(spy.findBeanSerializer(prov, origType, beanDesc));
   }

    @Test
   public void testFindBeanSerializerNotPotentialBeanTypeButEnum() throws JsonMappingException {
        when(origType.getRawClass()).thenReturn(TestEnum.class);
        when(origType.isEnumType()).thenReturn(true);
        BeanSerializerFactory spy = spy(factory);
        doReturn(false).when(spy).isPotentialBeanType(any(Class.class));
        doReturn(jsonSerializer).when(spy).constructBeanSerializer(prov, beanDesc);
        JsonSerializer<Object> result = spy.findBeanSerializer(prov, origType, beanDesc);
        assertSame(jsonSerializer, result);
   }

    enum TestEnum { A }

    @Test
    public void testFindPropertyTypeSerializerWithAnnotation() throws JsonMappingException {
        when(annotationIntrospector.findPropertyTypeResolver(config, accessor, origType)).thenReturn(typeResolverBuilder);
        Collection<NamedType> subtypes = Collections.emptyList();
        when(config.getSubtypeResolver()).thenReturn(mock(com.fasterxml.jackson.databind.jsontype.SubtypeResolver.class));
        when(config.getSubtypeResolver().collectAndResolveSubtypes(accessor, config, annotationIntrospector, origType)).thenReturn(subtypes);
        when(typeResolverBuilder.buildTypeSerializer(config, origType, subtypes)).thenReturn(typeSer);

        TypeSerializer result = factory.findPropertyTypeSerializer(origType, config, accessor);
        assertSame(typeSer, result);
    }

    @Test
    public void testFindPropertyTypeSerializerDefault() throws JsonMappingException {
        when(annotationIntrospector.findPropertyTypeResolver(config, accessor, origType)).thenReturn(null);
        BeanSerializerFactory spy = spy(factory);
        doReturn(typeSer).when(spy).createTypeSerializer(config, origType);
        TypeSerializer result = spy.findPropertyTypeSerializer(origType, config, accessor);
        assertSame(typeSer, result);
    }

    @Test
    public void testFindPropertyContentTypeSerializerWithAnnotation() throws JsonMappingException {
        JavaType containerType = mock(JavaType.class);
        JavaType contentType = mock(JavaType.class);
        when(containerType.getContentType()).thenReturn(contentType);
        when(annotationIntrospector.findPropertyContentTypeResolver(config, accessor, containerType)).thenReturn(typeResolverBuilder);
        Collection<NamedType> subtypes = Collections.emptyList();
        when(config.getSubtypeResolver()).thenReturn(mock(com.fasterxml.jackson.databind.jsontype.SubtypeResolver.class));
        when(config.getSubtypeResolver().collectAndResolveSubtypes(accessor, config, annotationIntrospector, contentType)).thenReturn(subtypes);
        when(typeResolverBuilder.buildTypeSerializer(config, contentType, subtypes)).thenReturn(typeSer);

        TypeSerializer result = factory.findPropertyContentTypeSerializer(containerType, config, accessor);
        assertSame(typeSer, result);
    }

    @Test
    public void testConstructBeanSerializerObjectClass() throws JsonMappingException {
        when(beanDesc.getBeanClass()).thenReturn(Object.class);
        when(prov.getUnknownTypeSerializer(Object.class)).thenReturn(jsonSerializer);
        JsonSerializer<Object> result = factory.constructBeanSerializer(prov, beanDesc);
        assertSame(jsonSerializer, result);
    }

    @Test
    public void testConstructBeanSerializerWithProperties() throws JsonMappingException {
        when(beanDesc.getBeanClass()).thenReturn(String.class); // not Object
        BeanSerializerFactory spy = spy(factory);
        doReturn(builder).when(spy).constructBeanSerializerBuilder(beanDesc);
        when(builder.setConfig(config)).thenReturn(builder);

        List<BeanPropertyWriter> props = new ArrayList<>();
        props.add(propertyWriter);
        doReturn(props).when(spy).findBeanProperties(prov, beanDesc, builder);
        when(prov.getAnnotationIntrospector()).thenReturn(annotationIntrospector);
        doNothing().when(annotationIntrospector).findAndAddVirtualProperties(config, classInfo, props);

        when(factoryConfig.hasSerializerModifiers()).thenReturn(false);
        spy._factoryConfig = factoryConfig;

        doReturn(props).when(spy).filterBeanProperties(config, beanDesc, props);
        doReturn(null).when(spy).constructObjectIdHandler(prov, beanDesc, props); // no object id
        when(builder.setProperties(props)).thenReturn(builder);
        when(builder.setFilterId(any())).thenReturn(builder);
        when(beanDesc.findAnyGetter()).thenReturn(null);

        doNothing().when(spy).processViews(config, builder);

        when(builder.build()).thenReturn(jsonSerializer);
        assertSame(jsonSerializer, spy.constructBeanSerializer(prov, beanDesc));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructObjectIdHandlerPropertyGeneratorNotFound() throws JsonMappingException {
        ObjectIdInfo objectIdInfo = mock(ObjectIdInfo.class);
        when(objectIdInfo.getGeneratorType()).thenReturn(ObjectIdGenerators.PropertyGenerator.class);
        when(objectIdInfo.getPropertyName()).thenReturn(new PropertyName("missingProp", null));
        when(beanDesc.getObjectIdInfo()).thenReturn(objectIdInfo);

        List<BeanPropertyWriter> props = new ArrayList<>();
        props.add(propertyWriter);
        when(propertyWriter.getName()).thenReturn("otherProp");

        factory.constructObjectIdHandler(prov, beanDesc, props);
    }

    @Test
    public void testConstructObjectIdHandlerPropertyGeneratorFound() throws JsonMappingException {
        ObjectIdInfo objectIdInfo = mock(ObjectIdInfo.class);
        when(objectIdInfo.getGeneratorType()).thenReturn(ObjectIdGenerators.PropertyGenerator.class);
        when(objectIdInfo.getPropertyName()).thenReturn(new PropertyName("myProp", null));
        when(objectIdInfo.getAlwaysAsId()).thenReturn(false);
        when(beanDesc.getObjectIdInfo()).thenReturn(objectIdInfo);

        BeanPropertyWriter idProp = mock(BeanPropertyWriter.class);
        when(idProp.getName()).thenReturn("myProp");
        when(idProp.getType()).thenReturn(mock(JavaType.class));
        List<BeanPropertyWriter> props = new ArrayList<>();
        props.add(idProp);
        props.add(propertyWriter);

        ObjectIdWriter result = factory.constructObjectIdHandler(prov, beanDesc, props);
        assertNotNull(result);
        // Check that idProp was moved to first position
        assertEquals(0, props.indexOf(idProp));
        assertTrue(result.generator instanceof PropertyBasedObjectIdGenerator);
    }

    @Test
    public void testConstructObjectIdHandlerNormal() throws JsonMappingException {
        ObjectIdInfo objectIdInfo = mock(ObjectIdInfo.class);
        when(objectIdInfo.getGeneratorType()).thenReturn(ObjectIdGenerators.IntSequenceGenerator.class);
        when(objectIdInfo.getPropertyName()).thenReturn(new PropertyName("id", null));
        when(objectIdInfo.getAlwaysAsId()).thenReturn(true);
        when(beanDesc.getObjectIdInfo()).thenReturn(objectIdInfo);
        when(prov.constructType(ObjectIdGenerators.IntSequenceGenerator.class)).thenReturn(delegateType);
        when(typeFactory.findTypeParameters(delegateType, ObjectIdGenerator.class)).thenReturn(new JavaType[]{mock(JavaType.class)});
        when(prov.objectIdGeneratorInstance(classInfo, objectIdInfo)).thenReturn(mock(ObjectIdGenerator.class));

        ObjectIdWriter result = factory.constructObjectIdHandler(prov, beanDesc, Collections.emptyList());
        assertNotNull(result);
    }

    @Test
    public void testConstructFilteredBeanWriter() {
        Class<?>[] views = new Class<?>[]{Object.class};
        BeanPropertyWriter result = factory.constructFilteredBeanWriter(propertyWriter, views);
        assertTrue(result instanceof FilteredBeanPropertyWriter);
    }

    @Test
    public void testConstructPropertyBuilder() {
        PropertyBuilder pb = factory.constructPropertyBuilder(config, beanDesc);
        assertNotNull(pb);
    }

    @Test
    public void testConstructBeanSerializerBuilder() {
        BeanSerializerBuilder bld = factory.constructBeanSerializerBuilder(beanDesc);
        assertNotNull(bld);
    }

    @Test
    public void testIsPotentialBeanType() {
        assertTrue(factory.isPotentialBeanType(String.class));
        assertFalse(factory.isPotentialBeanType(int.class));
    }

    @Test
    public void testFindBeanPropertiesEmpty() throws JsonMappingException {
        when(beanDesc.findProperties()).thenReturn(Collections.emptyList());
        when(config.isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS)).thenReturn(false);

        List<BeanPropertyWriter> result = factory.findBeanProperties(prov, beanDesc, builder);
        assertNull(result);
    }

    @Test
    public void testFindBeanPropertiesWithIgnorableTypes() throws JsonMappingException {
        List<BeanPropertyDefinition> properties = new ArrayList<>();
        properties.add(propDef);
        when(propDef.getAccessor()).thenReturn(accessor);
        when(accessor.getRawType()).thenReturn(String.class);
        when(beanDesc.findProperties()).thenReturn(properties);
        when(config.isEnabled(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS)).thenReturn(false);
        when(factory.usesStaticTyping(config, beanDesc, null)).thenReturn(false);
        when(factory.constructPropertyBuilder(config, beanDesc)).thenReturn(propertyBuilder);
        when(beanDesc.bindingsForBeanType()).thenReturn(typeBindings);

        // simulate ignorable type: after removal, list empty -> return null
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                List<BeanPropertyDefinition> list = invocation.getArgument(2);
                list.clear();
                return null;
            }
        }).when(factory).removeIgnorableTypes(config, beanDesc, properties);

        BeanSerializerFactory spy = spy(factory);
        List<BeanPropertyWriter> result = spy.findBeanProperties(prov, beanDesc, builder);
        assertNull(result);
    }

    @Test
    public void testFilterBeanProperties() {
        when(annotationIntrospector.findPropertiesToIgnore(classInfo)).thenReturn(new String[]{"ignoreMe"});
        List<BeanPropertyWriter> props = new ArrayList<>();
        BeanPropertyWriter wp = mock(BeanPropertyWriter.class);
        when(wp.getName()).thenReturn("ignoreMe");
        props.add(wp);
        List<BeanPropertyWriter> result = factory.filterBeanProperties(config, beanDesc, props);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testProcessViewsIncludeByDefault() {
        when(config.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION)).thenReturn(true);
        List<BeanPropertyWriter> props = new ArrayList<>();
        BeanPropertyWriter wp = mock(BeanPropertyWriter.class);
        when(wp.getViews()).thenReturn(null); // no views
        props.add(wp);
        when(builder.getProperties()).thenReturn(props);

        factory.processViews(config, builder);
        // Since includeByDefault and no views, no filtering should be set
        verify(builder, never()).setFilteredProperties(any());
    }

    @Test
    public void testRemoveIgnorableTypes() {
        List<BeanPropertyDefinition> props = new ArrayList<>();
        when(propDef.getAccessor()).thenReturn(accessor);
        when(accessor.getRawType()).thenReturn(String.class);
        props.add(propDef);

        when(annotationIntrospector.isIgnorableType(any(AnnotatedClass.class))).thenReturn(Boolean.TRUE);
        when(config.introspectClassAnnotations(String.class)).thenReturn(beanDesc);
        when(beanDesc.getClassInfo()).thenReturn(classInfo);

        factory.removeIgnorableTypes(config, beanDesc, props);
        assertTrue(props.isEmpty());
    }

    @Test
    public void testRemoveSetterlessGetters() {
        List<BeanPropertyDefinition> props = new ArrayList<>();
        when(propDef.couldDeserialize()).thenReturn(false);
        when(propDef.isExplicitlyIncluded()).thenReturn(false);
        props.add(propDef);

        factory.removeSetterlessGetters(config, beanDesc, props);
        assertTrue(props.isEmpty());
    }

    @Test
    public void test_constructWriter() throws JsonMappingException {
        when(propDef.getFullName()).thenReturn(new PropertyName("prop"));
        when(prov.canOverrideAccessModifiers()).thenReturn(false);
        when(accessor.getType(typeBindings)).thenReturn(origType);
        when(propDef.getWrapperName()).thenReturn(PropertyName.NO_NAME);
        when(propDef.getMetadata()).thenReturn(PropertyMetadata.STD_OPTIONAL);
        when(factory.findSerializerFromAnnotation(prov, accessor)).thenReturn(null);
        when(origType.getRawClass()).thenReturn(String.class);
        when(factory.findPropertyContentTypeSerializer(origType, config, accessor)).thenReturn(null);
        when(factory.findPropertyTypeSerializer(origType, config, accessor)).thenReturn(null);
        when(propertyBuilder.buildWriter(prov, propDef, origType, null, null, null, accessor, false)).thenReturn(propertyWriter);

        BeanPropertyWriter result = factory._constructWriter(prov, propDef, typeBindings, propertyBuilder, false, accessor);
        assertSame(propertyWriter, result);
    }
}
