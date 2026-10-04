package com.fasterxml.jackson.databind.ser;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.FilteredBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.Converter;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class BeanSerializerFactoryTest {

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
    private AnnotationIntrospector ai;
    @Mock
    private JsonSerializer<Object> mockSer;
    @Mock
    private Converter<Object, Object> converter;
    @Mock
    private JavaType delegateType;
    @Mock
    private TypeResolverBuilder<?> typeResBuilder;
    @Mock
    private TypeSerializer typeSer;
    @Mock
    private BeanSerializerBuilder builder;
    @Mock
    private ObjectIdInfo objectIdInfo;
    @Mock
    private AnnotatedMember accessor;
    @Mock
    private BeanPropertyDefinition propDef;
    @Mock
    private TypeBindings typeBind;
    @Mock
    private AnnotatedMethod method;
    @Mock
    private AnnotatedField field;

    private BeanSerializerFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = spy(BeanSerializerFactory.instance);
        when(prov.getConfig()).thenReturn(config);
        when(config.getAnnotationIntrospector()).thenReturn(ai);
        when(config.introspect(origType)).thenReturn(beanDesc);
        when(beanDesc.getClassInfo())).thenReturn(classInfo);

        // stub common behavior to return same type
        doReturn(origType).when(factory).modifyTypeByAnnotation(any(SerializationConfig.class), any(Annotated.class), any(JavaType.class));
        doReturn(mockSer).when(factory)._createSerializer2(any(SerializerProvider.class), any(JavaType.class), any(BeanDescription.class), anyBoolean());    }

    // ------ createSerializer tests ------

    @Test
    public void testCreateSerializerAnnotationSerFound() throws Exception {
        when(factory.findSerializerFromAnnotation(prov, classInfo)).thenReturn(mockSer);
        JsonSerializer<Object> result = factory.createSerializer(prov, origType);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializerNoAnnotationNoConversion() throws Exception {
        when(factory.findSerializerFromAnnotation(prov, classInfo)).thenReturn(null);
        JsonSerializer<Object> result = factory.createSerializer(prov, origType);
        verify(factory)._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializerModifiedTypeForcesStaticTyping() throws Exception {
        JavaType modifiedType = mock(JavaType.class);
        when(mmodifiedType.hasRawClass(origType.getRawClass())).thenReturn(false);
        when(config.introspect(mmodifiedType)).thenReturn(beandesc);
        doReturn(mmodifiedType).when(factory).modifyTypeByAnnotation(config, classInfo, origType);
        when(factory.findSerializerFromAnnotation(prov, classInfo)).thenReturn(null));

        JsonSerializer<Object> result = factory.createSerializer(prov, origType);
        verify(factory)._createSerializer2(prov, modifiedType, beanDesc, true);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializerWithConverter() throws Exception {
        when(beandesc.findSerializationConverter()).thenReturn(converter);
        when(converter.getOutputType(prov.getTypeFactory())).thenReturn(delegateType);
        when(delegateType.hasRawClass(origType.getRawClass())).thenReturn(false);
        when(config.introspect(delegateType)).thenReturn(beanDesc);
        // no annotation serializer found
        when(factory.findSerializerFromAnnotation(eq(prov), any(Notated.class))).thenReturn(null);
        doReturn(mockSer).when(factory)._createSerializer2(eq(prov), eq(delegateType), eq(beanDesc), anyBoolean());
        // mock StdDelegatingSerializer construction? Actually it's created safely.
        JsonSerializer<Object> result = factory.createSerializer(prov, origType);
        assertNotNull(result);
        // verify result is StdDelegatingSerializer (can't cast easily but check class)
        assertTrue(result instanceof StdDelegatingSerializer);
    }

    @Test
    public void testCreateSerializerConverterOutputObject() throws Exception {
        when(beanDesc.findSerializationConverter()).thenReturn(converter);
        when(converter.getOutputType(prov.getTypeFactory())).thenReturn(delegateType);
        when(delegateType.hasRawClass(origType.getRawClass()).thenReturn(false);
        when(config.introspect(delegateType)).thenReturn(beanDesc);
        when(delegateType.isJavaLangObject()).thenReturn(true);
        when(factory.findSerializerFromAnnotation(eq(prov), any(Notated.class))).thenReturn(null);
        // _createSerializer2 should NOT be called because delegate type is Object
        JsonSerializer<Object> result = factory.createSerializer(prov, origType);
        assertNotNull(result);
        assertTrue(result instanceof StdDelegatingSerializer);
        StdDelegatingSerializer std = (StddDelegatingSerializer) result;
        assertNull(std.getDelegateSerializer()); // ser was never created
    }

    // ------ _createSerializer2 tests ------

    @Test
    public void testCreateSerializer2ByAnnotations() throws Exception {
        doReturn(mockSer).when(factory).findSerializerByAnnotations(prov, origType, beanDesc);
        // call directly (no spy needed since real method will call it)
        // but we need to invoke real method, so we have to not mock _createSerializer2
        // We can call a new instance with real method? We'll use a separate spy without overriding _createSerializer2.
        BeanSerializerFactory realfactory = new BeanSerializerFactory(null) {
            // dummy to allow instantiation? protected, so not accessible.
        };
        // Not possible. Instead, we'll use factory instance with yes, we overrode _createSerializer2 earlier.
        // So we need to create a new spy with different stubs.
        BeanSerializerFactory factory2 = spy(BeanSerializerFactory.instance);
        doReturn(mockSer).when(factory2).findSerializerByAnnotations(prov, origType, beanDesc);
        JsonSerializer<?> result = factory2._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializer2ContainerType() throws Exception {
        when(origType.isContainerType()).thenReturn(true);
        // buildContainerserializer returns mockSer
        doReturn(mockSer).when(factory).buildContainerserializer(eq(prov), eq(origType), eq(beanDesc), anyBoolean()));
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializer2CustomSerializers() throws Exception {
        // non-container
        when(origType.isContainerType()).thenReturn(false);
        // setup custom serializers
        Serializers customSer = mock(Serializers.class);
        when(customSer.findSerializer(config, origType, beanDesc)).thenReturn(mockSer);
        // replace factory's customSerializers to return singleton
        doReturn(Collections.singleton(customSer)).when(factory).customSerializers();
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializer2_ByLookup() throws Exception {
        // non-container, custom serializers return null
        when(origType.isContainerType()).thenReturn(false);
        doReturn(Collections.emptyList()).when(factory).customSerializers());
        doReturn(mockSer).when(factory).findSerializerByLookup(origType, config, beanDesc, false);
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializer2_ByPrimaryType() throws Exception {
        when(origType.isContainerType()).thenReturn(false);
        doReturn(Collections.emptyList()).when(factory).customSerializers();
        doReturn(null).when(factory).findSerializerByLookup(origType, config, beanDesc, false);
        doReturn(mockSer).when(factory).findSerializerByPrimaryType(prov, origType, beanDesc, false);
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializer2_ByBeanSerializer() throws Exception {
        when(origType.isContainerType()).thenReturn(false);
        doReturn(Collections.emptyList()).when(factory).customSerializers();
        doReturn(null).when(factory).findSerializerByLookup(any(JavaType.class), any(SerializationConfig.class), any(BeanDescription.class), anyBoolean());
        doReturn(null).when(factory).findSerializerByPrimaryType(any(SerializerProvider.class), any(JavaType.class), any(BeanDescription.class), anyBoolean());
        doReturn(mockSer).when(factory).findBeanSerializer(prov, origType, beanDesc);
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializer2_ByAddonType() throws Exception {
        when(origType.isContainerType()).thenReturn(false);
        doReturn(Collections.emptyList()).when(factory).customSerializers();
        doReturn(null).when(factory).findSerializerByLookup(origType, config, beanDesc, false);
        doReturn(null).when(factory).findSerializerByPrimaryType(prov, origType, beanDesc, false);
        doReturn(null).when(factory).findBeanSerializer(prov, origType, beanDesc);
        doReturn(mockSer).when(factory).findSerializerByAddonType(config, origType, beanDesc, false);
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(mockSer, result);
    }

    @Test
    public void testCreateSerializer2_UnknownType() throws Exception {
        when(origType.isContainerType()).thenReturn(false);
        doReturn(Collections.emptyList()).when(factory).customSerializers();
        doReturn(null).when(factory).findSerializerByLookup(any(JavaType.class), any(SerializationConfig.class), any(BeanDescription.class), anyBoolean());
        doReturn(null).when(factory).findSerializerByPrimaryType(any(SerializerProvider.class), any(JavaType.class), any(BeanDescription.class), anyBoolean());
        doReturn(null).when(factory).findBeanSerializer(any(SerializerProvider.class), any(JavaType.class), any(BeanDescription.class));
        doReturn(null).when(factory).findSerializerByAddonType(any(SerializationConfig.class), any(JavaType.class), any(BeanDescription.class), anyBoolean());
        JsonSerializer<Object> unknownSer = mock(JsonSerializer.class);
        when(prov.getUnknownTypeSerializer(beanDesc.getBeanClass())).thenReturn(unknownSer);
        // modifiers empty
        when(_factoryConfig.hasSerializerModifiers()).thenReturn(false);
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        assertSame(unknownSer, result);
    }

    @Test
    public void testCreateSerializer2_ModifiersApplied() throws Exception {
        when(origType.isContainerType()).thenReturn(false);
        doReturn(Collections.emptyList()).when(factory).customSerializers();
        doReturn(mockSer).when(factory).findSerializerByLookup(origType, config, beanDesc, false);
        when(_factoryConfig.hasSerializerModifiers()).thenReturn(true);
        BeanSerializerModifier mod = mock(BeanSerializerModifier.class);
        when(_factoryConfig.serializerModifiers()).thenReturn(Collections.singletonList(mod));
        when(mod.modifySerializer(config, beanDesc, mockSer)).thenReturn(mockSer);
        JsonSerializer<?> result = factory._createSerializer2(prov, origType, beanDesc, false);
        verify(mod).modifySerializer(config, beanDesc, mockSer);
        assertSame(mockSer, result);
    }

    // ------ findBeanSerializer tests ------

    @Test
    public void testFindBeanSerializerNonPotentialType() throws Exception {
        when(origType.getRawClass()).thenReturn(String.class); // isPotentialBeanType true for String? Actually canBeABeanType(null) returns true for String, but we'll use a primitive
        when(origType.getRawClass()).thenReturn(Integer.TYPE);
        doReturn(true).when(factory).isPotentialBeanType(Integer.TYPE); // override to return false
        when(origType.isEnumType()).thenReturn(false);
        assertNull(factory.findBeanSerializer(prov, origType, beanDesc));
    }

    @Test
    public void testFindBeanSerializerPotentialTypeCallsConstruct() throws Exception {
        when(origType.getRawClass()).thenReturn(TestBean.class);
        doReturn(true).when(factory).isPotentialBeanType(TestBean.class);
        JsonSerializer<Object> ser = mock(JsonSerializer.class);
        doReturn(ser).when(factory).constructBeanSerializer(prov, beanDesc);
        assertSame(ser, factory.findBeanSerializer(prov, origType, beanDesc));
    }

    // ------ findPropertyTypeSerializer tests ------

    @Test
    public void testFindPropertyTypeSerializerDefault() throws Exception {
        when(ai.findPropertyTypeResolver(config, accessor, origType)).thenReturn(null);
        doReturn(typeSer).when(factory).createTypeSerializer(config, origType);
        assertSame(typeSer, factory.findPropertyTypeSerializer(origType, config, accessor));
    }

    @Test
    public void testFindPropertyTypeSerializerWithResolver() throws Exception {
        when(ai.findPropertyTypeResolver(config, accessor, origType)).thenReturn(typeResBuilder);
        Collection<NamedType> subtypes = new ArrayList<NamedType>();
        when(config.getSubtypeResolver()).thenReturn(mock(SubtypeResolver.class));
        when(config.getSubtypeResolver().collectAndResolveSubtypesByClass(config, accessor, origType)).thenReturn(subtypes);
        when(typeResBuilder.buildTypeSerializer(config, origType, subtypes)).thenReturn(typeSer);
        assertSame(typeSer, factory.findPropertyTypeSerializer(origType, config, accessor));
    }

    // ------ findPropertyContentTypeSerializer tests ------

    @Test
    public void testFindPropertyContentTypeSerializerDefault() throws Exception {
        when(origType.getContentType()).thenReturn(origType);
        when(ai.findPropertyContentTypeResolver(config, accessor, origType)).thenReturn(null);
        doReturn(typeSer).when(factory).createTypeSerializer(config, origType);
        assertSame(typeSer, factory.findPropertyContentTypeSerializer(origType, config, accessor));
    }

    @Test
    public void testFindPropertyContentTypeSerializerWithResolver() throws Exception {
        when(origType.getContentType()).thenReturn(origType);
        when(aai.findPropertyContentTypeResolver(config, accessor, origType)).thenReturn(typeResBuilder);
        Collection<NamedType> subtypes = new ArrayList<NamedType>();
        when(config.getSubtypeResolver()).thenReturn(mock(SubtypeResolver.class));
        when(config.getSubtypeResolver().collectAndResolveSubtypesByClass(config, accessor, origType)).thenReturn(subtypes);
        when(typeResBuilder.buildTypeSerializer(config, origType, subtypes)).thenReturn(typeSer);
        assertSame(typeSer, factory.findPropertyContentTypeSerializer(origType, config, accessor));
    }

    // ------ constructBeanSerializer tests ------
    @Test
    public void testConstructBeanSerializerObjectClass() throws Exception {
        when(beanDesc.getBeanClass()).thenReturn(Object.class);
        JsonSerializer<Object> result = factory.constructBeanSerializer(prov, beanDesc);
        verify(prov).getUnknownTypeSerializer(Object.class);
        // could assert something
    }

    @Test
    public void testConstructBeanSerializerWithProps() throws Exception {
        when(beanDesc.getBeanClass()).thenReturn(TestBean.class);
        BeanSerializerBuilder realBuilder = mock(BeanSerializerBuilder.class);
        doReturn(realBuilder).when(factory).constructBeanSerializerBuilder(beanDesc);
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        doReturn(props).when(factory).findBeanProperties(prov, beanDesc, realBuilder);
        doReturn(null).when(factory).constructObjectIdHandler(prov, beanDesc, props);
        when(ai.findAndAddVirtualProperties(config, classInfo, props)).thenReturn(Collections.emptyList());
        // mock builder
        when(realBuilder.getObjectIdWriter()).thenReturn(null);
        when(realBuilder.getProperties()).thenReturn(props);
        when(realBuilder.build()).thenReturn(mockSer);
        when(config.canOverrideAccessModifiers()).thenReturn(false);
        when(beanDesc.findAnyGetter()).thenReturn(null);
        // config.getAnnotationIntrospector() returns ai, already mocked
        JsonSerializer<Object> result = factory.constructBeanSerializer(prov, beanDesc);
        assertSame(mockSer, result);
    }

    // ------ constructObjectIdHandler tests ------

    @Test
    public void testConstructObjectIdHandlerNullObjectIdInfo() throws Exception {
        when(beanDesc.getObjectIdInfo()).thenReturn(null);
        assertNull(factory.constructObjectIdHandler(prov, beanDesc, new ArrayList<BeanPropertyWriter>()));
    }

    @Test
    public void testConstructObjectIdHandlerPropertyGenerator() throws Exception {
        when(beanDesc.getObjectIdInfo()).thenReturn(objectIdInfo);
        when(objectIdInfo.getGeneratorType()).thenReturn(ObjectIdGenerators.PropertyGenerator.class);
        PropertyName propName = new PropertyName("id");
        when(objectIdInfo.getPropertyName()).thenReturn(propName);
        when(propName.getSimpleName()).thenReturn("id");
        // prepare props list with matching property
        BeanPropertyWriter idProp = mock(BeanPropertyWriter.class);
        when(idProp.getName()).thenReturn("id");
        JavaType idType = mock(JavaType.class);
        when(idProp.getType()).thenReturn(idType);
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        props.add(mock(BeanPropertyWriter.class)); // non-matching
        props.add(idProp);
        // call
        ObjectIdWriter writer = factory.constructObjectIdHandler(prov, beanDesc, props);
        assertNotNull(writer);
        // verify idProp moved to front
        assertEquals(idProp, props.get(0));
    }

    // ------ filterBeanProperties tests ------

    @Test
    public void testFilterBeanPropertiesIgnoreSome() {
        when(ai.findPropertiesToIgnore(classInfo, true)).thenReturn(new String[]{"b"});
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        BeanPropertyWriter a = mock(BeanPropertyWriter.class);
        when(a.getName()).thenReturn("a");
        BeanPropertyWriter b = mock(BeanPropertyWriter.class);
        when(b.getName()).thenReturn("b");
        props.add(a);
        props.add(b);
        factory.filterBeanProperties(config, beanDesc, props);
        assertEquals(1, props.size());
        assertSame(a, props.get(0));
    }

    // ------ processViews tests ------

    @Test
    public void testProcessViewsDefaultInclusion() {
        when(config.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION)).thenReturn(true);
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        BeanPropertyWriter bpw = mock(BeanPropertyWriter.class);
        when(bpw.getViews()).thenReturn(null);
        props.add(bpw);
        // builder mock with getProperties returns props
        when(builder.getProperties()).thenReturn(props);
        factory.processViews(config, builder);
        verify(builder).setFilteredProperties(any(BeanPropertyWriter[].class));
    }

    // ------ removeIgnorableTypes tests ------

    @Test
    public void testRemoveIgnorableTypes() {
        List<BeanPropertyDefinition> properties = new ArrayList<BeanPropertyDefinition>();
        BeanPropertyDefinition prop = mock(BeanPropertyDefinition.class);
        when(prop.getAccessor()).thenReturn(accessor);
        when(accessor.getRawType()).thenReturn(String.class);
        properties.add(prop);
        BeanDescription classDesc = mock(BeanDescription.class);
        AnnotatedClass classAc = mock(AnnotatedClass.class);
        when(config.introspectClassAnnotations(String.class)).thenReturn(classDesc);
        when(classDesc.getClassInfo()).thenReturn(classAc);
        when(ai.isIgnorableType(classAc)).thenReturn(Boolean.TRUE);
        factory.removeIgnorableTypes(config, beanDesc, properties);
        assertTrue(properties.isEmpty());
    }

    // ------ removeSetterlessGetters tests ------
    @Test
    public void testRemoveSetterlessGetters() {
        List<BeanPropertyDefinition> properties = new ArrayList<BeanPropertyDefinition>();
        BeanPropertyDefinition prop = mock(BeanPropertyDefinition.class);
        when(prop.couldDeserialize()).thenReturn(false);
        when(prop.isExplicitlyIncluded()).thenReturn(false);
        properties.add(prop);
        factory.removeSetterlessGetters(config, beanDesc, properties);
        assertTrue(properties.isEmpty());
    }

    // ------ removeOverlappingTypeIds tests ------
    @Test
    public void testRemoveOverlappingTypeIs() {
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        BeanPropertyWriter bpw = mock(BeanPropertyWriter.class);
        TypeSerializer ts = mock(TypeSerializer.class);
        when(ts.getTypeInclusion()).thenReturn(As.EXTERNAL_PROERTY);
        when(ts.getPropertyName()).thenReturn("type");
        when(bpw.getTypeSerializer()).thenReturn(ts);
        // another writer that conflicts
        BeanPropertyWriter w2 = mock(BeanPropertyWriter.class);
        when(w2.wouldConflictWithName(any(PropertyName.class)).thenReturn(true);
        props.add(bpw);
        props.add(w2);
        factory.removeOverappingTypeIds(prov, beanDesc, builder, props);
        verify(bpw).assignTypeSerializer((TypeSerializer) null);
    }

    // ------ _constructWriter tests ------

    @Test
    public void testConstructWriter() throws Exception {
        when(propDef.getFullName()).thenReturn(new PropertyName("prop"));
        when(prov.canOverrideAccessModifiers()).thenReturn(false);
        when(accessor.getType(typeBind)).thenReturn(origType);
        when(propDef.getWrapperName()).thenReturn(null);
        when(propDef.getMetadata()).thenReturn(PropertyMetadata.STD_OPTIONAL);
        doReturn(null).when(factory).findSerializerFromAnnotation(prov, accessor);
        // stub buildWriter on pb
        PropertyBuilder pb = mock(PropertyBuilder.class);
        BeanPropertyWriter expectedWriter = mock(BeanPropertyWriter.class);
        when(pb.buildWriter(eq(prov), eq(propDef), eq(origType), any(JsonSerializer.class), any(TypeSerializer.class), any(TypeSerializer.class), eq(accessor), anyBoolean())).thenReturn(expectedWriter);
        // call _constructWriter with method
        BeanPropertyWriter result = factory._constructWriter(prov, propDef, typeBind, pb, false, method);
        assertSame(expectedWriter, result);
    }

    // ------ withConfig tests ------

    @Test(expected = IllegalStateException.class)
    public void testWithConfigSubclassThrows() {
        SerializerFactoryConfig newConfig = mock(SerializerFactoryConfig.class);
        TestSubclass sub = new TestSubclass(null);
        sub.withConfig(newConfig);
    }

    // helper subclass
    public static class TestSubclass extends BeanSerializerFactory {
        public TestSubclass(SerializerFactoryConfig config) {
            super(config);
        }
    }

    // dummy bean class for tests
    static class TestBean { }
}
