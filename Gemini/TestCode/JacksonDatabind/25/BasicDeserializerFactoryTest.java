package com.fasterxml.jackson.databind.deser;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.std.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.EnumResolver;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class BasicDeserializerFactoryTest {

    // Concrete subclass to test BasicDeserializerFactory directly
    static class TestBasicDeserializerFactory extends BasicDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public TestBasicDeserializerFactory(DeserializerFactoryConfig config) {
            super(config);
        }

        public TestBasicDeserializerFactory() {
            this(new DeserializerFactoryConfig());
        }

        @Override
        protected DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new TestBasicDeserializerFactory(config);
        }

        @Override
        public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt,
                JavaType type, BeanDescription beanDesc) throws JsonMappingException {
            return null;
        }

        @Override
        public JsonDeserializer<Object> createBuilderBasedDeserializer(DeserializationContext ctxt,
                JavaType type, BeanDescription beanDesc, Class<?> builderClass) throws JsonMappingException {
            return null;
        }
    }

    enum TestEnum {
        FOO, BAR;
    }

    enum EnumWithCreator {
        A, B;

        @JsonCreator
        public static EnumWithCreator fromString(String val) {
            if ("a".equalsIgnoreCase(val)) return A;
            if ("b".equalsIgnoreCase(val)) return B;
            return null;
        }
    }

    enum EnumWithBadCreator {
        A;

        @JsonCreator
        public static EnumWithBadCreator bad(int x, int y) {
            return A;
        }
    }

    enum EnumWithNonStringCreatorKey {
        A;

        @JsonCreator
        public static EnumWithNonStringCreatorKey badKey(int x) {
            return A;
        }
    }

    enum EnumWithValue {
        VAL1("1"), VAL2("2");

        private final String code;
        EnumWithValue(String code) { this.code = code; }

        @JsonValue
        public String getCode() { return code; }
    }

    static class CustomValueInstantiator extends ValueInstantiator {
        @Override
        public String getValueTypeDesc() {
            return "CustomValueInstantiator";
        }
    }

    static class NonStaticInner {
        public NonStaticInner(@JsonProperty("val") String val) {}
    }

    static class MultiParamCtorNoAnnotation {
        public MultiParamCtorNoAnnotation(String a, String b) {}
    }

    static class SingleArgCtors {
        public SingleArgCtors(String s) {}
        public SingleArgCtors(int i) {}
        public SingleArgCtors(long l) {}
        public SingleArgCtors(double d) {}
        public SingleArgCtors(boolean b) {}
    }

    static class SingleArgFactoryMethods {
        public static SingleArgFactoryMethods create(String s) { return null; }
        public static SingleArgFactoryMethods create(int i) { return null; }
        public static SingleArgFactoryMethods create(long l) { return null; }
        public static SingleArgFactoryMethods create(double d) { return null; }
        public static SingleArgFactoryMethods create(boolean b) { return null; }
    }

    public interface UnresolvableInterface {}

    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private TypeFactory _typeFactory;
    private TestBasicDeserializerFactory _factory;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _typeFactory = _mapper.getTypeFactory();
        _factory = new TestBasicDeserializerFactory();
    }

    @Test
    public void testFactoryConfigFluentMethods() {
        DeserializerFactoryConfig config = _factory.getFactoryConfig();
        Assert.assertNotNull(config);

        Deserializers desers = new SimpleDeserializers();
        KeyDeserializers keyDesers = new SimpleKeyDeserializers();
        BeanDeserializerModifier modifier = new BeanDeserializerModifier();
        AbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        ValueInstantiators instantiators = new SimpleValueInstantiators();

        DeserializerFactory f1 = _factory.withAdditionalDeserializers(desers);
        Assert.assertNotSame(_factory, f1);
        Assert.assertTrue(f1.getFactoryConfig().hasDeserializers());

        DeserializerFactory f2 = _factory.withAdditionalKeyDeserializers(keyDesers);
        Assert.assertNotSame(_factory, f2);
        Assert.assertTrue(f2.getFactoryConfig().hasKeyDeserializers());

        DeserializerFactory f3 = _factory.withDeserializerModifier(modifier);
        Assert.assertNotSame(_factory, f3);
        Assert.assertTrue(f3.getFactoryConfig().hasDeserializerModifiers());

        DeserializerFactory f4 = _factory.withAbstractTypeResolver(resolver);
        Assert.assertNotSame(_factory, f4);
        Assert.assertTrue(f4.getFactoryConfig().hasAbstractTypeResolvers());

        DeserializerFactory f5 = _factory.withValueInstantiators(instantiators);
        Assert.assertNotSame(_factory, f5);
        Assert.assertTrue(f5.getFactoryConfig().hasValueInstantiators());
    }

    @Test
    public void testMapAbstractType() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        JavaType listType = _typeFactory.constructType(List.class);

        // Without resolvers, returns the original type
        JavaType mapped = _factory.mapAbstractType(config, listType);
        Assert.assertEquals(listType, mapped);

        // With resolver
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        TestBasicDeserializerFactory factoryWithResolver = (TestBasicDeserializerFactory) _factory.withAbstractTypeResolver(resolver);
        
        JavaType charSeqType = _typeFactory.constructType(CharSequence.class);
        JavaType mappedCharSeq = factoryWithResolver.mapAbstractType(config, charSeqType);
        Assert.assertEquals(_typeFactory.constructType(String.class), mappedCharSeq);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractTypeInvalidCycleOrNonSubtype() throws Exception {
        DeserializationConfig config = _mapper.getDeserializationConfig();
        AbstractTypeResolver badResolver = new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                if (type.hasRawClass(List.class)) {
                    return _typeFactory.constructType(Map.class); // Map is not subtype of List
                }
                return null;
            }
            @Override
            public JavaType resolveAbstractType(DeserializationConfig config, JavaType type) {
                return null;
            }
        };

        TestBasicDeserializerFactory factory = (TestBasicDeserializerFactory) _factory.withAbstractTypeResolver(badResolver);
        factory.mapAbstractType(config, _typeFactory.constructType(List.class));
    }

    @Test
    public void testFindValueInstantiatorStdAndCustom() throws Exception {
        BeanDescription locDesc = _context.getConfig().introspect(_typeFactory.constructType(JsonLocation.class));
        ValueInstantiator locInst = _factory.findValueInstantiator(_context, locDesc);
        Assert.assertNotNull(locInst);
        Assert.assertTrue(locInst instanceof JsonLocationInstantiator);

        // Custom ValueInstantiator via config
        final ValueInstantiator customVi = new CustomValueInstantiator();
        ValueInstantiators viProvider = new ValueInstantiators.Base() {
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
                return customVi;
            }
        };
        TestBasicDeserializerFactory customFactory = (TestBasicDeserializerFactory) _factory.withValueInstantiators(viProvider);
        ValueInstantiator returnedVi = customFactory.findValueInstantiator(_context, locDesc);
        Assert.assertSame(customVi, returnedVi);
    }

    @Test(expected = JsonMappingException.class)
    public void testFindValueInstantiatorBrokenResolver() throws Exception {
        ValueInstantiators brokenProvider = new ValueInstantiators.Base() {
            @Override
            public ValueInstantiator findValueInstantiator(DeserializationConfig config, BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
                return null;
            }
        };
        TestBasicDeserializerFactory customFactory = (TestBasicDeserializerFactory) _factory.withValueInstantiators(brokenProvider);
        BeanDescription desc = _context.getConfig().introspect(_typeFactory.constructType(String.class));
        customFactory.findValueInstantiator(_context, desc);
    }

    @Test
    public void testValueInstantiatorInstance() throws Exception {
        DeserializationConfig config = _context.getConfig();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config.getAnnotationIntrospector(), null);

        Assert.assertNull(_factory._valueInstantiatorInstance(config, ac, null));
        Assert.assertNull(_factory._valueInstantiatorInstance(config, ac, Object.class)); // bogus class

        ValueInstantiator vi = new CustomValueInstantiator();
        Assert.assertSame(vi, _factory._valueInstantiatorInstance(config, ac, vi));

        ValueInstantiator viCreated = _factory._valueInstantiatorInstance(config, ac, CustomValueInstantiator.class);
        Assert.assertNotNull(viCreated);
        Assert.assertTrue(viCreated instanceof CustomValueInstantiator);
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceInvalidClass() throws Exception {
        DeserializationConfig config = _context.getConfig();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config.getAnnotationIntrospector(), null);
        _factory._valueInstantiatorInstance(config, ac, String.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceNotClassOrInstantiator() throws Exception {
        DeserializationConfig config = _context.getConfig();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config.getAnnotationIntrospector(), null);
        _factory._valueInstantiatorInstance(config, ac, "not-a-class");
    }

    @Test
    public void testCreateArrayDeserializerPrimitivesAndObjects() throws Exception {
        // Primitive arrays
        Class<?>[] primitives = new Class<?>[] {
            int[].class, long[].class, byte[].class, short[].class, float[].class,
            double[].class, boolean[].class, char[].class
        };
        for (Class<?> prim : primitives) {
            ArrayType arrayType = _typeFactory.constructArrayType(prim.getComponentType());
            BeanDescription beanDesc = _context.getConfig().introspect(arrayType);
            JsonDeserializer<?> deser = _factory.createArrayDeserializer(_context, arrayType, beanDesc);
            Assert.assertNotNull("Should create deserializer for " + prim.getName(), deser);
            Assert.assertTrue(deser instanceof PrimitiveArrayDeserializers);
        }

        // String[]
        ArrayType strArrayType = _typeFactory.constructArrayType(String.class);
        BeanDescription strBeanDesc = _context.getConfig().introspect(strArrayType);
        JsonDeserializer<?> strDeser = _factory.createArrayDeserializer(_context, strArrayType, strBeanDesc);
        Assert.assertNotNull(strDeser);
        Assert.assertTrue(strDeser instanceof StringArrayDeserializer);

        // Object[]
        ArrayType objArrayType = _typeFactory.constructArrayType(Object.class);
        BeanDescription objBeanDesc = _context.getConfig().introspect(objArrayType);
        JsonDeserializer<?> objDeser = _factory.createArrayDeserializer(_context, objArrayType, objBeanDesc);
        Assert.assertNotNull(objDeser);
        Assert.assertTrue(objDeser instanceof ObjectArrayDeserializer);
    }

    @Test
    public void testCreateArrayDeserializerWithModifiersAndCustom() throws Exception {
        ArrayType arrayType = _typeFactory.constructArrayType(Double.class);
        BeanDescription beanDesc = _context.getConfig().introspect(arrayType);

        final JsonDeserializer<?> mockDeser = new StdDeserializer<Object>(Double[].class) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        Deserializers customDesers = new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findArrayDeserializer(ArrayType type, DeserializationConfig config,
                    BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) {
                return mockDeser;
            }
        };

        TestBasicDeserializerFactory f = (TestBasicDeserializerFactory) _factory.withAdditionalDeserializers(customDesers);
        JsonDeserializer<?> deser = f.createArrayDeserializer(_context, arrayType, beanDesc);
        Assert.assertSame(mockDeser, deser);

        // BeanDeserializerModifier for array
        BeanDeserializerModifier mod = new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyArrayDeserializer(DeserializationConfig config, ArrayType valueType,
                    BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                return mockDeser;
            }
        };
        TestBasicDeserializerFactory fMod = (TestBasicDeserializerFactory) _factory.withDeserializerModifier(mod);
        Assert.assertSame(mockDeser, fMod.createArrayDeserializer(_context, arrayType, beanDesc));
    }

    @Test
    public void testCreateCollectionDeserializers() throws Exception {
        // EnumSet
        CollectionType enumSetType = _typeFactory.constructCollectionType(EnumSet.class, TestEnum.class);
        BeanDescription beanDesc = _context.getConfig().introspect(enumSetType);
        JsonDeserializer<?> enumSetDeser = _factory.createCollectionDeserializer(_context, enumSetType, beanDesc);
        Assert.assertNotNull(enumSetDeser);
        Assert.assertTrue(enumSetDeser instanceof EnumSetDeserializer);

        // String Collection (List<String>)
        CollectionType strListType = _typeFactory.constructCollectionType(List.class, String.class);
        BeanDescription strListDesc = _context.getConfig().introspect(strListType);
        JsonDeserializer<?> strListDeser = _factory.createCollectionDeserializer(_context, strListType, strListDesc);
        Assert.assertNotNull(strListDeser);
        Assert.assertTrue(strListDeser instanceof StringCollectionDeserializer);

        // Generic Collection (Set<Integer>)
        CollectionType intSetType = _typeFactory.constructCollectionType(Set.class, Integer.class);
        BeanDescription intSetDesc = _context.getConfig().introspect(intSetType);
        JsonDeserializer<?> intSetDeser = _factory.createCollectionDeserializer(_context, intSetType, intSetDesc);
        Assert.assertNotNull(intSetDeser);
        Assert.assertTrue(intSetDeser instanceof CollectionDeserializer);

        // ArrayBlockingQueue
        CollectionType abqType = _typeFactory.constructCollectionType(ArrayBlockingQueue.class, Integer.class);
        BeanDescription abqDesc = _context.getConfig().introspect(abqType);
        JsonDeserializer<?> abqDeser = _factory.createCollectionDeserializer(_context, abqType, abqDesc);
        Assert.assertNotNull(abqDeser);
        Assert.assertTrue(abqDeser instanceof ArrayBlockingQueueDeserializer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnresolvableCollectionThrowsException() throws Exception {
        CollectionType unresolvableType = _typeFactory.constructCollectionType(UnresolvableCollection.class, String.class);
        BeanDescription desc = _context.getConfig().introspect(unresolvableType);
        _factory.createCollectionDeserializer(_context, unresolvableType, desc);
    }

    public static abstract class UnresolvableCollection<T> implements Collection<T> {}

    @Test
    public void testMapAbstractCollectionTypeFallbacks() {
        DeserializationConfig config = _context.getConfig();

        Class<?>[] interfaces = new Class<?>[] {
            Collection.class, List.class, Set.class, SortedSet.class, Queue.class, Deque.class, NavigableSet.class
        };
        for (Class<?> iface : interfaces) {
            CollectionType type = _typeFactory.constructCollectionType((Class<? extends Collection>) iface, String.class);
            CollectionType mapped = _factory._mapAbstractCollectionType(type, config);
            Assert.assertNotNull("Should map fallback for " + iface.getName(), mapped);
            Assert.assertFalse(mapped.isInterface());
        }
    }

    @Test
    public void testCreateCollectionLikeDeserializer() throws Exception {
        CollectionLikeType colLikeType = _typeFactory.constructCollectionLikeType(ArrayList.class, String.class);
        BeanDescription beanDesc = _context.getConfig().introspect(colLikeType);

        final JsonDeserializer<?> customDeser = new StdDeserializer<Object>(ArrayList.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        Deserializers customDesers = new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config,
                    BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) {
                return customDeser;
            }
        };

        TestBasicDeserializerFactory f = (TestBasicDeserializerFactory) _factory.withAdditionalDeserializers(customDesers);
        JsonDeserializer<?> deser = f.createCollectionLikeDeserializer(_context, colLikeType, beanDesc);
        Assert.assertSame(customDeser, deser);
    }

    @Test
    public void testCreateMapDeserializers() throws Exception {
        // EnumMap
        MapType enumMapType = _typeFactory.constructMapType(EnumMap.class, TestEnum.class, String.class);
        BeanDescription beanDesc = _context.getConfig().introspect(enumMapType);
        JsonDeserializer<?> enumMapDeser = _factory.createMapDeserializer(_context, enumMapType, beanDesc);
        Assert.assertNotNull(enumMapDeser);
        Assert.assertTrue(enumMapDeser instanceof EnumMapDeserializer);

        // Fallback Map interfaces
        Class<?>[] mapInterfaces = new Class<?>[] {
            Map.class, ConcurrentMap.class, SortedMap.class, NavigableMap.class, ConcurrentNavigableMap.class
        };
        for (Class<?> mapIface : mapInterfaces) {
            MapType mapType = _typeFactory.constructMapType((Class<? extends Map>) mapIface, String.class, Integer.class);
            BeanDescription md = _context.getConfig().introspect(mapType);
            JsonDeserializer<?> mdDeser = _factory.createMapDeserializer(_context, mapType, md);
            Assert.assertNotNull("Should create map deser for " + mapIface.getName(), mdDeser);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumMapInvalidKeyTypeThrows() throws Exception {
        MapType badEnumMap = _typeFactory.constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription beanDesc = _context.getConfig().introspect(badEnumMap);
        _factory.createMapDeserializer(_context, badEnumMap, beanDesc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnresolvableMapThrowsException() throws Exception {
        MapType unresolvableType = _typeFactory.constructMapType(UnresolvableMap.class, String.class, String.class);
        BeanDescription desc = _context.getConfig().introspect(unresolvableType);
        _factory.createMapDeserializer(_context, unresolvableType, desc);
    }

    public static abstract class UnresolvableMap<K, V> implements Map<K, V> {}

    @Test
    public void testCreateMapLikeDeserializer() throws Exception {
        MapLikeType mapLikeType = _typeFactory.constructMapLikeType(Map.Entry.class, String.class, Integer.class);
        BeanDescription beanDesc = _context.getConfig().introspect(mapLikeType);

        final JsonDeserializer<?> customDeser = new StdDeserializer<Object>(Map.Entry.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        Deserializers customDesers = new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type, DeserializationConfig config,
                    BeanDescription beanDesc, KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) {
                return customDeser;
            }
        };

        TestBasicDeserializerFactory f = (TestBasicDeserializerFactory) _factory.withAdditionalDeserializers(customDesers);
        JsonDeserializer<?> deser = f.createMapLikeDeserializer(_context, mapLikeType, beanDesc);
        Assert.assertSame(customDeser, deser);
    }

    @Test
    public void testCreateEnumDeserializer() throws Exception {
        // Standard Enum
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        BeanDescription beanDesc = _context.getConfig().introspect(enumType);
        JsonDeserializer<?> deser = _factory.createEnumDeserializer(_context, enumType, beanDesc);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof EnumDeserializer);

        // Enum with @JsonCreator
        JavaType creatorEnumType = _typeFactory.constructType(EnumWithCreator.class);
        BeanDescription creatorBeanDesc = _context.getConfig().introspect(creatorEnumType);
        JsonDeserializer<?> creatorDeser = _factory.createEnumDeserializer(_context, creatorEnumType, creatorBeanDesc);
        Assert.assertNotNull(creatorDeser);
        Assert.assertTrue(creatorDeser instanceof EnumDeserializer);

        // Enum with @JsonValue
        JavaType valEnumType = _typeFactory.constructType(EnumWithValue.class);
        BeanDescription valBeanDesc = _context.getConfig().introspect(valEnumType);
        JsonDeserializer<?> valDeser = _factory.createEnumDeserializer(_context, valEnumType, valBeanDesc);
        Assert.assertNotNull(valDeser);
        Assert.assertTrue(valDeser instanceof EnumDeserializer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnumDeserializerBadCreator() throws Exception {
        JavaType badEnumType = _typeFactory.constructType(EnumWithBadCreator.class);
        BeanDescription badBeanDesc = _context.getConfig().introspect(badEnumType);
        _factory.createEnumDeserializer(_context, badEnumType, badBeanDesc);
    }

    @Test
    public void testCreateTreeDeserializer() throws Exception {
        DeserializationConfig config = _context.getConfig();
        JavaType nodeType = _typeFactory.constructType(JsonNode.class);
        BeanDescription beanDesc = config.introspect(nodeType);
        JsonDeserializer<?> deser = _factory.createTreeDeserializer(config, nodeType, beanDesc);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof JsonNodeDeserializer);

        JavaType objNodeType = _typeFactory.constructType(ObjectNode.class);
        BeanDescription objDesc = config.introspect(objNodeType);
        JsonDeserializer<?> objDeser = _factory.createTreeDeserializer(config, objNodeType, objDesc);
        Assert.assertNotNull(objDeser);

        JavaType arrNodeType = _typeFactory.constructType(ArrayNode.class);
        BeanDescription arrDesc = config.introspect(arrNodeType);
        JsonDeserializer<?> arrDeser = _factory.createTreeDeserializer(config, arrNodeType, arrDesc);
        Assert.assertNotNull(arrDeser);
    }

    @Test
    public void testCreateKeyDeserializer() throws Exception {
        // Enum Key Deserializer
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        KeyDeserializer enumKeyDes = _factory.createKeyDeserializer(_context, enumType);
        Assert.assertNotNull(enumKeyDes);

        // Enum with Creator Key Deserializer
        JavaType creatorEnumType = _typeFactory.constructType(EnumWithCreator.class);
        KeyDeserializer creatorKeyDes = _factory.createKeyDeserializer(_context, creatorEnumType);
        Assert.assertNotNull(creatorKeyDes);

        // Enum with @JsonValue Key Deserializer
        JavaType valEnumType = _typeFactory.constructType(EnumWithValue.class);
        KeyDeserializer valKeyDes = _factory.createKeyDeserializer(_context, valEnumType);
        Assert.assertNotNull(valKeyDes);

        // Standard String-based Key Deserializers (int, long, UUID, etc.)
        JavaType intType = _typeFactory.constructType(Integer.class);
        KeyDeserializer intKeyDes = _factory.createKeyDeserializer(_context, intType);
        Assert.assertNotNull(intKeyDes);

        JavaType uuidType = _typeFactory.constructType(UUID.class);
        KeyDeserializer uuidKeyDes = _factory.createKeyDeserializer(_context, uuidType);
        Assert.assertNotNull(uuidKeyDes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnumKeyDeserializerNonStringCreatorKeyThrows() throws Exception {
        JavaType badEnumType = _typeFactory.constructType(EnumWithNonStringCreatorKey.class);
        _factory.createKeyDeserializer(_context, badEnumType);
    }

    @Test
    public void testFindDefaultDeserializer() throws Exception {
        // Object.class
        JavaType objType = _typeFactory.constructType(Object.class);
        BeanDescription objDesc = _context.getConfig().introspect(objType);
        JsonDeserializer<?> objDeser = _factory.findDefaultDeserializer(_context, objType, objDesc);
        Assert.assertNotNull(objDeser);
        Assert.assertTrue(objDeser instanceof UntypedObjectDeserializer);

        // String & CharSequence
        JavaType strType = _typeFactory.constructType(String.class);
        BeanDescription strDesc = _context.getConfig().introspect(strType);
        JsonDeserializer<?> strDeser = _factory.findDefaultDeserializer(_context, strType, strDesc);
        Assert.assertSame(StringDeserializer.instance, strDeser);

        JavaType csType = _typeFactory.constructType(CharSequence.class);
        BeanDescription csDesc = _context.getConfig().introspect(csType);
        JsonDeserializer<?> csDeser = _factory.findDefaultDeserializer(_context, csType, csDesc);
        Assert.assertSame(StringDeserializer.instance, csDeser);

        // AtomicReference
        JavaType refType = _typeFactory.constructType(AtomicReference.class);
        BeanDescription refDesc = _context.getConfig().introspect(refType);
        JsonDeserializer<?> refDeser = _factory.findDefaultDeserializer(_context, refType, refDesc);
        Assert.assertNotNull(refDeser);
        Assert.assertTrue(refDeser instanceof AtomicReferenceDeserializer);

        // Iterable
        JavaType iterType = _typeFactory.constructType(Iterable.class);
        BeanDescription iterDesc = _context.getConfig().introspect(iterType);
        JsonDeserializer<?> iterDeser = _factory.findDefaultDeserializer(_context, iterType, iterDesc);
        Assert.assertNotNull(iterDeser);

        // Map.Entry
        JavaType entryType = _typeFactory.constructType(Map.Entry.class);
        BeanDescription entryDesc = _context.getConfig().introspect(entryType);
        JsonDeserializer<?> entryDeser = _factory.findDefaultDeserializer(_context, entryType, entryDesc);
        Assert.assertNotNull(entryDeser);
        Assert.assertTrue(entryDeser instanceof MapEntryDeserializer);

        // TokenBuffer
        JavaType tbType = _typeFactory.constructType(TokenBuffer.class);
        BeanDescription tbDesc = _context.getConfig().introspect(tbType);
        JsonDeserializer<?> tbDeser = _factory.findDefaultDeserializer(_context, tbType, tbDesc);
        Assert.assertNotNull(tbDeser);
        Assert.assertTrue(tbDeser instanceof TokenBufferDeserializer);

        // JDK Numbers & Dates
        Class<?>[] jdkTypes = new Class<?>[] {
            int.class, Integer.class, double.class, Double.class, Date.class, Calendar.class,
            UUID.class, URL.class, URI.class, Locale.class, Currency.class, Pattern.class
        };
        for (Class<?> clz : jdkTypes) {
            JavaType jt = _typeFactory.constructType(clz);
            BeanDescription bd = _context.getConfig().introspect(jt);
            JsonDeserializer<?> deser = _factory.findDefaultDeserializer(_context, jt, bd);
            Assert.assertNotNull("Default deserializer should exist for " + clz.getName(), deser);
        }
    }

    @Test
    public void testConstructEnumResolver() throws Exception {
        DeserializationConfig config = _context.getConfig();
        EnumResolver res1 = _factory.constructEnumResolver(TestEnum.class, config, null);
        Assert.assertNotNull(res1);
        Assert.assertEquals(TestEnum.FOO, res1.findEnum("FOO"));

        // With READ_ENUMS_USING_TO_STRING enabled
        DeserializationConfig configToString = config.with(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        EnumResolver res2 = _factory.constructEnumResolver(TestEnum.class, configToString, null);
        Assert.assertNotNull(res2);

        // With @JsonValue method
        BeanDescription valDesc = config.introspect(_typeFactory.constructType(EnumWithValue.class));
        AnnotatedMethod jsonValueMethod = valDesc.findJsonValueMethod();
        EnumResolver res3 = _factory.constructEnumResolver(EnumWithValue.class, config, jsonValueMethod);
        Assert.assertNotNull(res3);
        Assert.assertEquals(EnumWithValue.VAL1, res3.findEnum("1"));
    }

    @Test
    public void testFindJsonValueFor() {
        DeserializationConfig config = _context.getConfig();
        Assert.assertNull(_factory._findJsonValueFor(config, null));

        JavaType enumType = _typeFactory.constructType(EnumWithValue.class);
        AnnotatedMethod method = _factory._findJsonValueFor(config, enumType);
        Assert.assertNotNull(method);
        Assert.assertEquals("getCode", method.getName());

        JavaType stdEnumType = _typeFactory.constructType(TestEnum.class);
        Assert.assertNull(_factory._findJsonValueFor(config, stdEnumType));
    }

    @Test
    public void testParamNameLookupHelpers() {
        AnnotationIntrospector intr = _context.getAnnotationIntrospector();
        Assert.assertNull(_factory._findParamName(null, null));
        Assert.assertNull(_factory._findImplicitParamName(null, null));
        Assert.assertNull(_factory._findExplicitParamName(null, null));
        Assert.assertFalse(_factory._hasExplicitParamName(null, null));

        BeanDescription desc = _context.getConfig().introspect(_typeFactory.constructType(NonStaticInner.class));
        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        AnnotatedParameter param = ctor.getParameter(0);

        PropertyName name = _factory._findParamName(param, intr);
        Assert.assertNotNull(name);
        Assert.assertEquals("val", name.getSimpleName());
        Assert.assertTrue(_factory._hasExplicitParamName(param, intr));
    }

    @Test
    public void testFindTypeDeserializer() throws Exception {
        JavaType strType = _typeFactory.constructType(String.class);
        TypeDeserializer td = _factory.findTypeDeserializer(_context.getConfig(), strType);
        Assert.assertNull(td); // by default null for String unless default typing enabled
    }

    @Test
    public void testModifyTypeByAnnotation() throws Exception {
        BeanDescription desc = _context.getConfig().introspect(_typeFactory.constructType(SingleArgCtors.class));
        AnnotatedConstructor ctor = desc.getConstructors().get(0);
        JavaType origType = _typeFactory.constructType(String.class);
        JavaType modified = _factory.modifyTypeByAnnotation(_context, ctor, origType);
        Assert.assertEquals(origType, modified);
    }

    @Test
    public void testFindRemappedType() throws Exception {
        DeserializationConfig config = _context.getConfig();
        JavaType remapped = _factory._findRemappedType(config, List.class);
        Assert.assertNull(remapped); // No resolver registered, so null

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, ArrayList.class);
        TestBasicDeserializerFactory f = (TestBasicDeserializerFactory) _factory.withAbstractTypeResolver(resolver);
        JavaType remappedList = f._findRemappedType(config, List.class);
        Assert.assertNotNull(remappedList);
        Assert.assertEquals(ArrayList.class, remappedList.getRawClass());
    }
}
