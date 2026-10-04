package com.fasterxml.jackson.databind.deser;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonInject;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.impl.CreatorCollector;
import com.fasterxml.jackson.databind.deser.std.*;
import com.fasterxml.jackson.databind.ext.OptionalHandlerFactory;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BasicDeserializerFactoryTest extends BasicDeserializerFactory {

    public BasicDeserializerFactoryTest(DeserializerFactoryConfig config) {
        super(config);
    }

    @Override
    protected DeserializerFactory withConfig(DeserializerFactoryConfig config) {
        return new BasicDeserializerFactoryTest(config);
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DeserializationConfig config() {
        return MAPPER.getDeserializationConfig();
    }

    private DeserializationContext ctxt() {
        return MAPPER.getDeserializationContext();
    }

    private BeanDescription beanDesc(Class<?> clazz) {
        return config().introspectClassAnnotations(clazz);
    }

    private JavaType type(Class<?> clazz) {
        return config().getTypeFactory().constructType(clazz);
    }

    @Test
    public void testMapAbstractTypeWithMapping() throws Exception {
        DeserializationConfig cfg = config().withAbstractTypeResolver(new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                if (type.getRawClass() == List.class) {
                    return config.constructType(ArrayList.class);
                }
                return null;
            }
        });
        BasicDeserializerFactoryTest factory = new BasicDeserializerFactoryTest(cfg.getFactoryConfig());
        JavaType result = factory.mapAbstractType(cfg, cfg.constructType(List.class));
        assertEquals(ArrayList.class, result.getRawClass());
    }

    @Test
    public void testMapAbstractTypeNoMapping() throws Exception {
        JavaType input = config().constructType(List.class);
        JavaType result = mapAbstractType(config(), input);
        assertEquals(List.class, result.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractTypeInvalidSubtype() throws Exception {
        DeserializationConfig cfg = config().withAbstractTypeResolver(new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                return config.constructType(HashSet.class);
            }
        });
        BasicDeserializerFactoryTest factory = new BasicDeserializerFactoryTest(cfg.getFactoryConfig());
        factory.mapAbstractType(cfg, cfg.constructType(List.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractTypeRecursive() throws Exception {
        DeserializationConfig cfg = config().withAbstractTypeResolver(new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                return type;
            }
        });
        BasicDeserializerFactoryTest factory = new BasicDeserializerFactoryTest(cfg.getFactoryConfig());
        factory.mapAbstractType(cfg, cfg.constructType(List.class));
    }

    @Test
    public void testFindValueInstantiatorForJsonLocation() throws Exception {
        ValueInstantiator inst = findValueInstantiator(ctxt(), beanDesc(JsonLocation.class));
        assertNotNull(inst);
        assertTrue(inst instanceof JsonLocationInstantiator);
    }

    @Test
    public void testFindValueInstantiatorDefault() throws Exception {
        ValueInstantiator inst = findValueInstantiator(ctxt(), beanDesc(SimpleBean.class));
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDefault());
    }

    @Test
    public void testFindValueInstantiatorWithAnnotation() throws Exception {
        DeserializationConfig cfg = config();
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = cfg.introspectClassAnnotations(BeanWithValueInstantiator.class);
        ValueInstantiator inst = findValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst instanceof CustomValueInstantiator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindValueInstantiatorIncompleteParameter() throws Exception {
        DeserializationConfig cfg = config();
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = cfg.introspectClassAnnotations(BeanWithIncompleteCreator.class);
        findValueInstantiator(ctxt, desc);
    }

    @Test
    public void testConstructDefaultValueInstantiatorDefaultCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(SimpleBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDefault());
    }

    @Test
    public void testConstructDefaultValueInstantiatorStringCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(StringCreatorBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromString());
    }

    @Test
    public void testConstructDefaultValueInstantiatorIntCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(IntCreatorBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromInt());
    }

    @Test
    public void testConstructDefaultValueInstantiatorLongCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(LongCreatorBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromLong());
    }

    @Test
    public void testConstructDefaultValueInstantiatorDoubleCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(DoubleCreatorBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromDouble());
    }

    @Test
    public void testConstructDefaultValueInstantiatorBooleanCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(BooleanCreatorBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromBoolean());
    }

    @Test
    public void testConstructDefaultValueInstantiatorDelegatingCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(DelegatingCreatorBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDelegate());
    }

    @Test
    public void testConstructDefaultValueInstantiatorPropertyBasedCtor() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(PropertyBasedCreatorBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateFromObjectWith());
    }

    @Test
    public void testConstructDefaultValueInstantiatorFactoryMethod() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(FactoryMethodBean.class);
        ValueInstantiator inst = _constructDefaultValueInstantiator(ctxt, desc);
        assertNotNull(inst);
        assertTrue(inst.canCreateUsingDefault() || inst.canCreateFromString());
    }

    @Test
    public void testAddDeserializerConstructorsImplicitNames() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(ImplicitNamesBean.class);
        CreatorCollector creators = new CreatorCollector(desc, ctxt.canOverrideAccessModifiers());
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        VisibilityChecker<?> vchecker = config().getDefaultVisibilityChecker();
        Map<AnnotatedWithParams, BeanPropertyDefinition[]> defs = _findCreatorsFromProperties(ctxt, desc);
        _addDeserializerConstructors(ctxt, desc, vchecker, intr, creators, defs);
        assertTrue(creators.hasPropertyBasedCreator() || creators.hasDefaultCreator());
    }

    @Test
    public void testAddDeserializerFactoryMethodsStringFactory() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(StringFactoryBean.class);
        CreatorCollector creators = new CreatorCollector(desc, ctxt.canOverrideAccessModifiers());
        AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
        VisibilityChecker<?> vchecker = config().getDefaultVisibilityChecker();
        Map<AnnotatedWithParams, BeanPropertyDefinition[]> defs = _findCreatorsFromProperties(ctxt, desc);
        _addDeserializerFactoryMethods(ctxt, desc, vchecker, intr, creators, defs);
        assertTrue(creators.hasPropertyBasedCreator() || creators.hasDelegatingCreator());
    }

    @Test
    public void testConstructCreatorPropertyBasic() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(PropertyBasedCreatorBean.class);
        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        AnnotatedParameter param = ctor.getParameter(0);
        SettableBeanProperty prop = constructCreatorProperty(ctxt, desc, new PropertyName("value"), 0, param, null);
        assertNotNull(prop);
        assertEquals("value", prop.getName());
    }

    @Test
    public void testCreateArrayDeserializerPrimitive() throws Exception {
        ArrayType type = (ArrayType) config().getTypeFactory().constructArrayType(int.class);
        JsonDeserializer<?> deser = createArrayDeserializer(ctxt(), type, beanDesc(int[].class));
        assertNotNull(deser);
    }

    @Test
    public void testCreateArrayDeserializerString() throws Exception {
        ArrayType type = (ArrayType) config().getTypeFactory().constructArrayType(String.class);
        JsonDeserializer<?> deser = createArrayDeserializer(ctxt(), type, beanDesc(String[].class));
        assertNotNull(deser);
        assertTrue(deser instanceof StringArrayDeserializer);
    }

    @Test
    public void testCreateCollectionDeserializerList() throws Exception {
        CollectionType type = (CollectionType) config().getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<?> deser = createCollectionDeserializer(ctxt(), type, beanDesc(ArrayList.class));
        assertNotNull(deser);
    }

    @Test
    public void testCreateCollectionDeserializerInterface() throws Exception {
        CollectionType type = (CollectionType) config().getTypeFactory().constructCollectionType(List.class, String.class);
        JsonDeserializer<?> deser = createCollectionDeserializer(ctxt(), type, beanDesc(List.class));
        assertNotNull(deser);
    }

    @Test
    public void testCreateCollectionDeserializerEnumSet() throws Exception {
        CollectionType type = (CollectionType) config().getTypeFactory().constructCollectionType(EnumSet.class, Day.class);
        JsonDeserializer<?> deser = createCollectionDeserializer(ctxt(), type, beanDesc(EnumSet.class));
        assertNotNull(deser);
        assertTrue(deser instanceof EnumSetDeserializer);
    }

    @Test
    public void testCreateCollectionDeserializerArrayBlockingQueue() throws Exception {
        CollectionType type = (CollectionType) config().getTypeFactory().constructCollectionType(ArrayBlockingQueue.class, String.class);
        JsonDeserializer<?> deser = createCollectionDeserializer(ctxt(), type, beanDesc(ArrayBlockingQueue.class));
        assertNotNull(deser);
        assertTrue(deser instanceof ArrayBlockingQueueDeserializer);
    }

    @Test
    public void testCreateMapDeserializerHashMap() throws Exception {
        MapType type = (MapType) config().getTypeFactory().constructMapType(HashMap.class, String.class, Integer.class);
        JsonDeserializer<?> deser = createMapDeserializer(ctxt(), type, beanDesc(HashMap.class));
        assertNotNull(deser);
    }

    @Test
    public void testCreateMapDeserializerInterface() throws Exception {
        MapType type = (MapType) config().getTypeFactory().constructMapType(Map.class, String.class, Integer.class);
        JsonDeserializer<?> deser = createMapDeserializer(ctxt(), type, beanDesc(Map.class));
        assertNotNull(deser);
    }

    @Test
    public void testCreateMapDeserializerEnumMap() throws Exception {
        MapType type = (MapType) config().getTypeFactory().constructMapType(EnumMap.class, Day.class, String.class);
        JsonDeserializer<?> deser = createMapDeserializer(ctxt(), type, beanDesc(EnumMap.class));
        assertNotNull(deser);
        assertTrue(deser instanceof EnumMapDeserializer);
    }

    @Test
    public void testCreateEnumDeserializer() throws Exception {
        JavaType type = config().getTypeFactory().constructType(Day.class);
        JsonDeserializer<?> deser = createEnumDeserializer(ctxt(), type, beanDesc(Day.class));
        assertNotNull(deser);
    }

    @Test
    public void testCreateEnumDeserializerWithJsonCreator() throws Exception {
        JavaType type = config().getTypeFactory().constructType(DayWithCreator.class);
        JsonDeserializer<?> deser = createEnumDeserializer(ctxt(), type, beanDesc(DayWithCreator.class));
        assertNotNull(deser);
    }

    @Test
    public void testFindTypeDeserializerNoAnnotation() throws Exception {
        TypeDeserializer td = findTypeDeserializer(config(), type(String.class));
        assertNull(td);
    }

    @Test
    public void testFindTypeDeserializerWithDefaultTyper() throws Exception {
        DeserializationConfig cfg = config().withDefaultTyper(new TypeResolverBuilder<TypeResolverBuilder<?>>() {
            @Override
            public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes) {
                return null;
            }
            @Override
            public TypeResolverBuilder<?> init(JsonTypeInfo.Id idType, TypeIdResolver res) { return this; }
            @Override
            public TypeResolverBuilder<?> inclusion(JsonTypeInfo.As includeAs) { return this; }
            @Override
            public TypeResolverBuilder<?> typeProperty(String typeIdPropName) { return this; }
            @Override
            public TypeResolverBuilder<?> defaultImpl(Class<?> defaultImpl) { return this; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
        });
        TypeDeserializer td = findTypeDeserializer(cfg, type(String.class));
        assertNull(td);
    }

    @Test
    public void testFindDefaultDeserializerObject() throws Exception {
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), type(Object.class), beanDesc(Object.class));
        assertNotNull(deser);
        assertTrue(deser instanceof UntypedObjectDeserializer);
    }

    @Test
    public void testFindDefaultDeserializerString() throws Exception {
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), type(String.class), beanDesc(String.class));
        assertNotNull(deser);
        assertTrue(deser instanceof StringDeserializer);
    }

    @Test
    public void testFindDefaultDeserializerCharSequence() throws Exception {
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), type(CharSequence.class), beanDesc(CharSequence.class));
        assertNotNull(deser);
        assertTrue(deser instanceof StringDeserializer);
    }

    @Test
    public void testFindDefaultDeserializerAtomicReference() throws Exception {
        JavaType refType = config().getTypeFactory().constructReferenceType(AtomicReference.class, type(String.class));
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), refType, beanDesc(AtomicReference.class));
        assertNotNull(deser);
        assertTrue(deser instanceof AtomicReferenceDeserializer);
    }

    @Test
    public void testFindDefaultDeserializerIterable() throws Exception {
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), type(Iterable.class), beanDesc(Iterable.class));
        assertNotNull(deser);
    }

    @Test
    public void testFindDefaultDeserializerMapEntry() throws Exception {
        JavaType entryType = config().getTypeFactory().constructMapType(Map.class, String.class, Integer.class).getContentType();
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), entryType, beanDesc(Map.Entry.class));
        assertNotNull(deser);
        assertTrue(deser instanceof MapEntryDeserializer);
    }

    @Test
    public void testFindDefaultDeserializerPrimitive() throws Exception {
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), type(int.class), beanDesc(int.class));
        assertNotNull(deser);
    }

    @Test
    public void testFindDefaultDeserializerTokenBuffer() throws Exception {
        JsonDeserializer<?> deser = findDefaultDeserializer(ctxt(), type(TokenBuffer.class), beanDesc(TokenBuffer.class));
        assertNotNull(deser);
        assertTrue(deser instanceof TokenBufferDeserializer);
    }

    @Test
    public void testCreateKeyDeserializerEnum() throws Exception {
        KeyDeserializer kd = createKeyDeserializer(ctxt(), type(Day.class));
        assertNotNull(kd);
    }

    @Test
    public void testModifyTypeByAnnotation() throws Exception {
        JavaType type = config().getTypeFactory().constructType(String.class);
        JavaType modified = modifyTypeByAnnotation(ctxt(), new AnnotatedParameter(null, null, null, null, 0), type);
        assertNotNull(modified);
    }

    @Test
    public void testResolveType() throws Exception {
        JavaType type = config().getTypeFactory().constructType(String.class);
        JavaType resolved = resolveType(ctxt(), beanDesc(String.class), type, null);
        assertNotNull(resolved);
    }

    @Test
    public void testConstructEnumResolver() throws Exception {
        EnumResolver resolver = constructEnumResolver(Day.class, config(), null);
        assertNotNull(resolver);
    }

    @Test
    public void testConstructEnumResolverWithJsonValue() throws Exception {
        BeanDescription desc = beanDesc(DayWithJsonValue.class);
        AnnotatedMethod jsonValueMethod = desc.findJsonValueMethod();
        EnumResolver resolver = constructEnumResolver(DayWithJsonValue.class, config(), jsonValueMethod);
        assertNotNull(resolver);
    }

    @Test
    public void testFindJsonValueFor() throws Exception {
        AnnotatedMethod method = _findJsonValueFor(config(), type(DayWithJsonValue.class));
        assertNotNull(method);
    }

    @Test
    public void testFindPropertyTypeDeserializer() throws Exception {
        TypeDeserializer td = findPropertyTypeDeserializer(config(), type(String.class), null);
        assertNull(td);
    }

    @Test
    public void testFindPropertyContentTypeDeserializer() throws Exception {
        JavaType containerType = config().getTypeFactory().constructCollectionType(List.class, String.class);
        TypeDeserializer td = findPropertyContentTypeDeserializer(config(), containerType, null);
        assertNull(td);
    }

    @Test
    public void testValueInstantiatorInstanceNull() throws Exception {
        assertNull(_valueInstantiatorInstance(config(), null, null));
    }

    @Test
    public void testValueInstantiatorInstanceDirect() throws Exception {
        ValueInstantiator vi = new CustomValueInstantiator();
        ValueInstantiator result = _valueInstantiatorInstance(config(), null, vi);
        assertSame(vi, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceInvalidType() throws Exception {
        _valueInstantiatorInstance(config(), null, "invalid");
    }

    @Test
    public void testValueInstantiatorInstanceBogusClass() throws Exception {
        assertNull(_valueInstantiatorInstance(config(), null, Void.class));
    }

    @Test
    public void testFindCreatorsFromPropertiesEmpty() throws Exception {
        Map<AnnotatedWithParams, BeanPropertyDefinition[]> result = _findCreatorsFromProperties(ctxt(), beanDesc(SimpleBean.class));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCheckIfCreatorPropertyBasedExplicitName() throws Exception {
        AnnotationIntrospector intr = ctxt().getAnnotationIntrospector();
        AnnotatedConstructor ctor = beanDesc(PropertyBasedCreatorBean.class).getConstructors().get(0);
        BeanPropertyDefinition propDef = beanDesc(PropertyBasedCreatorBean.class).findProperties().get(0);
        assertTrue(_checkIfCreatorPropertyBased(intr, ctor, propDef));
    }

    @Test
    public void testHandleSingleArgumentConstructorString() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(StringCreatorBean.class);
        CreatorCollector creators = new CreatorCollector(desc, ctxt.canOverrideAccessModifiers());
        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        boolean result = _handleSingleArgumentConstructor(ctxt, desc, config().getDefaultVisibilityChecker(),
                ctxt.getAnnotationIntrospector(), creators, ctor, false, true);
        assertTrue(result);
        assertTrue(creators.hasStringCreator());
    }

    @Test
    public void testHandleSingleArgumentFactoryString() throws Exception {
        DeserializationConfig cfg = config();
        BeanDescription desc = beanDesc(StringFactoryBean.class);
        CreatorCollector creators = new CreatorCollector(desc, true);
        AnnotatedMethod factory = desc.getFactoryMethods().get(0);
        boolean result = _handleSingleArgumentFactory(cfg, desc, cfg.getDefaultVisibilityChecker(),
                cfg.getAnnotationIntrospector(), creators, factory, false);
        assertTrue(result);
        assertTrue(creators.hasStringCreator());
    }

    @Test
    public void testCheckImplicitlyNamedConstructors() throws Exception {
        DeserializationContext ctxt = ctxt();
        BeanDescription desc = beanDesc(ImplicitNamesBean.class);
        CreatorCollector creators = new CreatorCollector(desc, ctxt.canOverrideAccessModifiers());
        List<AnnotatedConstructor> implicitCtors = new ArrayList<AnnotatedConstructor>();
        for (AnnotatedConstructor ctor : desc.getConstructors()) {
            if (ctor.getParameterCount() > 0) {
                implicitCtors.add(ctor);
            }
        }
        _checkImplicitlyNamedConstructors(ctxt, desc, config().getDefaultVisibilityChecker(),
                ctxt.getAnnotationIntrospector(), creators, implicitCtors);
        assertTrue(creators.hasPropertyBasedCreator());
    }

    @Test
    public void testFindParamName() throws Exception {
        AnnotationIntrospector intr = ctxt().getAnnotationIntrospector();
        PropertyName name = _findParamName(null, intr);
        assertNull(name);
    }

    @Test
    public void testFindImplicitParamName() throws Exception {
        AnnotationIntrospector intr = ctxt().getAnnotationIntrospector();
        PropertyName name = _findImplicitParamName(null, intr);
        assertNull(name);
    }

    @Test
    public void testMapAbstractCollectionType() throws Exception {
        CollectionType result = _mapAbstractCollectionType(type(Set.class), config());
        assertNotNull(result);
        assertEquals(HashSet.class, result.getRawClass());
    }

    @Test
    public void testMapAbstractCollectionTypeNull() throws Exception {
        CollectionType result = _mapAbstractCollectionType(type(ArrayList.class), config());
        assertNull(result);
    }

    @Test
    public void testFindCustomArrayDeserializer() throws Exception {
        ArrayType type = (ArrayType) config().getTypeFactory().constructArrayType(String.class);
        JsonDeserializer<?> deser = _findCustomArrayDeserializer(type, config(), beanDesc(String[].class), null, null);
        assertNull(deser);
    }

    @Test
    public void testFindCustomBeanDeserializer() throws Exception {
        JsonDeserializer<?> deser = _findCustomBeanDeserializer(type(String.class), config(), beanDesc(String.class));
        assertNull(deser);
    }

    @Test
    public void testFindCustomCollectionDeserializer() throws Exception {
        CollectionType type = (CollectionType) config().getTypeFactory().constructCollectionType(List.class, String.class);
        JsonDeserializer<?> deser = _findCustomCollectionDeserializer(type, config(), beanDesc(List.class), null, null);
        assertNull(deser);
    }

    @Test
    public void testFindCustomCollectionLikeDeserializer() throws Exception {
        CollectionLikeType type = (CollectionLikeType) config().getTypeFactory().constructCollectionLikeType(String.class, String.class);
        JsonDeserializer<?> deser = _findCustomCollectionLikeDeserializer(type, config(), beanDesc(String.class), null, null);
        assertNull(deser);
    }

    @Test
    public void testFindCustomEnumDeserializer() throws Exception {
        JsonDeserializer<?> deser = _findCustomEnumDeserializer(Day.class, config(), beanDesc(Day.class));
        assertNull(deser);
    }

    @Test
    public void testFindCustomMapDeserializer() throws Exception {
        MapType type = (MapType) config().getTypeFactory().constructMapType(Map.class, String.class, Integer.class);
        JsonDeserializer<?> deser = _findCustomMapDeserializer(type, config(), beanDesc(Map.class), null, null, null);
        assertNull(deser);
    }

    @Test
    public void testFindCustomMapLikeDeserializer() throws Exception {
        MapLikeType type = (MapLikeType) config().getTypeFactory().constructMapLikeType(Map.class, String.class, Integer.class);
        JsonDeserializer<?> deser = _findCustomMapLikeDeserializer(type, config(), beanDesc(Map.class), null, null, null);
        assertNull(deser);
    }

    @Test
    public void testFindCustomTreeNodeDeserializer() throws Exception {
        JsonDeserializer<?> deser = _findCustomTreeNodeDeserializer(JsonNode.class, config(), beanDesc(JsonNode.class));
        assertNull(deser);
    }

    @Test
    public void testFindDeserializerFromAnnotation() throws Exception {
        JsonDeserializer<?> deser = findDeserializerFromAnnotation(ctxt(), beanDesc(SimpleBean.class).getClassInfo());
        assertNull(deser);
    }

    @Test
    public void testFindRemappedType() throws Exception {
        JavaType result = _findRemappedType(config(), List.class);
        assertNull(result);
    }

    @Test
    public void testFindStdValueInstantiator() throws Exception {
        ValueInstantiator inst = _findStdValueInstantiator(config(), beanDesc(JsonLocation.class));
        assertNotNull(inst);
        assertTrue(inst instanceof JsonLocationInstantiator);
    }

    @Test
    public void testFindStdValueInstantiatorNull() throws Exception {
        ValueInstantiator inst = _findStdValueInstantiator(config(), beanDesc(SimpleBean.class));
        assertNull(inst);
    }

    @Test
    public void testWithAdditionalDeserializers() throws Exception {
        DeserializerFactory factory = withAdditionalDeserializers(new Deserializers.Base());
        assertNotNull(factory);
    }

    @Test
    public void testWithAdditionalKeyDeserializers() throws Exception {
        DeserializerFactory factory = withAdditionalKeyDeserializers(new KeyDeserializers.Base());
        assertNotNull(factory);
    }

    @Test
    public void testWithDeserializerModifier() throws Exception {
        DeserializerFactory factory = withDeserializerModifier(new BeanDeserializerModifier() {});
        assertNotNull(factory);
    }

    @Test
    public void testWithAbstractTypeResolver() throws Exception {
        DeserializerFactory factory = withAbstractTypeResolver(new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                return null;
            }
        });
        assertNotNull(factory);
    }

    @Test
    public void testWithValueInstantiators() throws Exception {
        DeserializerFactory factory = withValueInstantiators(new ValueInstantiators.Base() {});
        assertNotNull(factory);
    }

    @Test
    public void testGetFactoryConfig() throws Exception {
        assertNotNull(getFactoryConfig());
    }

    static class SimpleBean { }

    static class StringCreatorBean {
        private String value;
        @JsonCreator
        public StringCreatorBean(String v) { value = v; }
    }

    static class IntCreatorBean {
        @JsonCreator
        public IntCreatorBean(int v) { }
    }

    static class LongCreatorBean {
        @JsonCreator
        public LongCreatorBean(long v) { }
    }

    static class DoubleCreatorBean {
        @JsonCreator
        public DoubleCreatorBean(double v) { }
    }

    static class BooleanCreatorBean {
        @JsonCreator
        public BooleanCreatorBean(boolean v) { }
    }

    static class DelegatingCreatorBean {
        @JsonCreator
        public DelegatingCreatorBean(Object delegate) { }
    }

    static class PropertyBasedCreatorBean {
        private String a;
        @JsonCreator
        public PropertyBasedCreatorBean(@JsonProperty("a") String a) { this.a = a; }
    }

    static class FactoryMethodBean {
        private FactoryMethodBean() { }
        @JsonCreator
        public static FactoryMethodBean create() { return new FactoryMethodBean(); }
    }

    static class StringFactoryBean {
        private StringFactoryBean(String v) { }
        @JsonCreator
        public static StringFactoryBean valueOf(String v) { return new StringFactoryBean(v); }
    }

    static class ImplicitNamesBean {
        private String a;
        private int b;
        public ImplicitNamesBean(String a, int b) { this.a = a; this.b = b; }
    }

    static class BeanWithValueInstantiator {
        @JsonValueInstantiator(CustomValueInstantiator.class)
        public BeanWithValueInstantiator() { }
    }

    static class CustomValueInstantiator extends ValueInstantiator.Base {
        @Override
        public boolean canCreateUsingDefault() { return true; }
    }

    static class BeanWithIncompleteCreator {
        @JsonCreator
        public BeanWithIncompleteCreator(String a, int b) { }
    }

    enum Day { MONDAY, TUESDAY }

    enum DayWithCreator {
        MONDAY;
        @JsonCreator
        public static DayWithCreator fromString(String v) { return MONDAY; }
    }

    enum DayWithJsonValue {
        MONDAY;
        @JsonValue
        public String toValue() { return name(); }
    }

    @JsonValueInstantiator(CustomValueInstantiator.class)
    static class AnnotatedWithValueInstantiator { }
}
