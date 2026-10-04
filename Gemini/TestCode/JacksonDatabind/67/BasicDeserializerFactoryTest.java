package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.SimpleBeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.EnumResolver;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class BasicDeserializerFactoryTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private DeserializerFactory factory;

    static class TestBasicFactory extends BasicDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public TestBasicFactory(DeserializerFactoryConfig config) {
            super(config);
        }

        @Override
        protected DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new TestBasicFactory(config);
        }

        @Override
        public JavaType _mapAbstractCollectionType(JavaType type, DeserializationConfig config) {
            return super._mapAbstractCollectionType(type, config);
        }

        @Override
        public JavaType _findRemappedType(DeserializationConfig config, Class<?> rawType) throws JsonMappingException {
            return super._findRemappedType(config, rawType);
        }

        @Override
        public EnumResolver constructEnumResolver(Class<?> enumClass, DeserializationConfig config, AnnotatedMethod jsonValueMethod) {
            return super.constructEnumResolver(enumClass, config, jsonValueMethod);
        }

        @Override
        public ValueInstantiator _valueInstantiatorInstance(DeserializationConfig config, Annotated annotated, Object instDef) throws JsonMappingException {
            return super._valueInstantiatorInstance(config, annotated, instDef);
        }

        @Override
        public PropertyName _findParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
            return super._findParamName(param, intr);
        }

        @Override
        public PropertyName _findImplicitParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
            return super._findImplicitParamName(param, intr);
        }

        @Override
        public PropertyName _findExplicitParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
            return super._findExplicitParamName(param, intr);
        }

        @Override
        public boolean _hasExplicitParamName(AnnotatedParameter param, AnnotationIntrospector intr) {
            return super._hasExplicitParamName(param, intr);
        }

        @Override
        public AnnotatedMethod _findJsonValueFor(DeserializationConfig config, JavaType enumType) {
            return super._findJsonValueFor(config, enumType);
        }

        @Override
        public JavaType modifyTypeByAnnotation(DeserializationContext ctxt, Annotated a, JavaType type) throws JsonMappingException {
            return super.modifyTypeByAnnotation(ctxt, a, type);
        }

        @Override
        public JavaType resolveType(DeserializationContext ctxt, BeanDescription beanDesc, JavaType type, AnnotatedMember member) throws JsonMappingException {
            return super.resolveType(ctxt, beanDesc, type, member);
        }
    }

    enum TestEnum {
        A, B, C;
    }

    enum CustomEnumWithCreator {
        FIRST, SECOND;

        @JsonCreator
        public static CustomEnumWithCreator fromString(String val) {
            return "FIRST".equalsIgnoreCase(val) ? FIRST : SECOND;
        }
    }

    enum BadEnumWithNonStringCreator {
        ONE, TWO;

        @JsonCreator
        public static BadEnumWithNonStringCreator fromInt(int val) {
            return ONE;
        }
    }

    enum EnumWithJsonValue {
        X, Y;

        @JsonValue
        public String toVal() {
            return name().toLowerCase();
        }
    }

    static class SingleArgCtors {
        String s;
        int i;
        long l;
        double d;
        boolean b;

        public SingleArgCtors(String s) { this.s = s; }
        public SingleArgCtors(int i) { this.i = i; }
        public SingleArgCtors(long l) { this.l = l; }
        public SingleArgCtors(double d) { this.d = d; }
        public SingleArgCtors(boolean b) { this.b = b; }
    }

    static class SingleArgFactoryMethods {
        @JsonCreator public static SingleArgFactoryMethods create(String s) { return new SingleArgFactoryMethods(); }
        @JsonCreator public static SingleArgFactoryMethods create(int i) { return new SingleArgFactoryMethods(); }
        @JsonCreator public static SingleArgFactoryMethods create(long l) { return new SingleArgFactoryMethods(); }
        @JsonCreator public static SingleArgFactoryMethods create(double d) { return new SingleArgFactoryMethods(); }
        @JsonCreator public static SingleArgFactoryMethods create(boolean b) { return new SingleArgFactoryMethods(); }
    }

    static class DummyInstantiator extends ValueInstantiator implements Serializable {
        private static final long serialVersionUID = 1L;
        @Override public String getValueTypeDesc() { return "Dummy"; }
    }

    public static class CustomInstantiatorClass extends ValueInstantiator implements Serializable {
        private static final long serialVersionUID = 1L;
        @Override public String getValueTypeDesc() { return "CustomInstantiatorClass"; }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        factory = BeanDeserializerFactory.instance;
    }

    @Test
    public void testFactoryFluentConfigMethods() {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        Assert.assertNotNull(basicFactory.getFactoryConfig());

        DeserializerFactory f1 = basicFactory.withAdditionalDeserializers(new SimpleDeserializers());
        Assert.assertTrue(f1.getFactoryConfig().hasDeserializers());

        DeserializerFactory f2 = basicFactory.withAdditionalKeyDeserializers(new SimpleKeyDeserializers());
        Assert.assertTrue(f2.getFactoryConfig().hasKeyDeserializers());

        DeserializerFactory f3 = basicFactory.withDeserializerModifier(new BeanDeserializerModifier());
        Assert.assertTrue(f3.getFactoryConfig().hasDeserializerModifiers());

        DeserializerFactory f4 = basicFactory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        Assert.assertTrue(f4.getFactoryConfig().hasAbstractTypeResolvers());

        DeserializerFactory f5 = basicFactory.withValueInstantiators(new SimpleValueInstantiators());
        Assert.assertTrue(f5.getFactoryConfig().hasValueInstantiators());
    }

    @Test
    public void testMapAbstractType() throws Exception {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        JavaType mapType = mapper.constructType(Map.class);
        JavaType mapped = basicFactory.mapAbstractType(mapper.getDeserializationConfig(), mapType);
        Assert.assertEquals(Map.class, mapped.getRawClass());

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        basicFactory = (TestBasicFactory) basicFactory.withAbstractTypeResolver(resolver);
        JavaType resolved = basicFactory.mapAbstractType(mapper.getDeserializationConfig(), mapper.constructType(CharSequence.class));
        Assert.assertEquals(String.class, resolved.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractTypeCycle() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, (Class) Integer.class);
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig().withAbstractTypeResolver(resolver));
        basicFactory.mapAbstractType(mapper.getDeserializationConfig(), mapper.constructType(CharSequence.class));
    }

    @Test
    public void testFindStdValueInstantiator() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        BeanDescription beanDesc = config.introspect(mapper.constructType(JsonLocation.class));
        ValueInstantiator vi = factory.findValueInstantiator(ctxt, beanDesc);
        Assert.assertNotNull(vi);
        Assert.assertEquals("com.fasterxml.jackson.core.JsonLocation", vi.getValueTypeDesc());
    }

    @Test
    public void testValueInstantiatorInstance() throws Exception {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, config);

        Assert.assertNull(basicFactory._valueInstantiatorInstance(config, ac, null));
        DummyInstantiator dummy = new DummyInstantiator();
        Assert.assertSame(dummy, basicFactory._valueInstantiatorInstance(config, ac, dummy));

        ValueInstantiator inst = basicFactory._valueInstantiatorInstance(config, ac, CustomInstantiatorClass.class);
        Assert.assertNotNull(inst);
        Assert.assertEquals("CustomInstantiatorClass", inst.getValueTypeDesc());

        Assert.assertNull(basicFactory._valueInstantiatorInstance(config, ac, com.fasterxml.jackson.databind.annotation.NoClass.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceInvalidNonClass() throws Exception {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        basicFactory._valueInstantiatorInstance(mapper.getDeserializationConfig(), null, "not-a-class-or-vi");
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceInvalidClass() throws Exception {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        basicFactory._valueInstantiatorInstance(mapper.getDeserializationConfig(), null, String.class);
    }

    @Test
    public void testCreateArrayDeserializer() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType primType = mapper.constructType(int[].class);
        BeanDescription beanDesc = config.introspect(primType);
        JsonDeserializer<?> deser = factory.createArrayDeserializer(ctxt, (ArrayType) primType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType strArrType = mapper.constructType(String[].class);
        deser = factory.createArrayDeserializer(ctxt, (ArrayType) strArrType, config.introspect(strArrType));
        Assert.assertNotNull(deser);

        JavaType objArrType = mapper.constructType(Object[].class);
        deser = factory.createArrayDeserializer(ctxt, (ArrayType) objArrType, config.introspect(objArrType));
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateCollectionDeserializers() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType listType = mapper.constructType(List.class);
        JsonDeserializer<?> deser = factory.createCollectionDeserializer(ctxt, (CollectionType) listType, config.introspect(listType));
        Assert.assertNotNull(deser);

        JavaType strListType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        deser = factory.createCollectionDeserializer(ctxt, (CollectionType) strListType, config.introspect(strListType));
        Assert.assertNotNull(deser);

        JavaType enumSetType = TypeFactory.defaultInstance().constructCollectionType(EnumSet.class, TestEnum.class);
        deser = factory.createCollectionDeserializer(ctxt, (CollectionType) enumSetType, config.introspect(enumSetType));
        Assert.assertNotNull(deser);

        JavaType abqType = TypeFactory.defaultInstance().constructCollectionType(ArrayBlockingQueue.class, Integer.class);
        deser = factory.createCollectionDeserializer(ctxt, (CollectionType) abqType, config.introspect(abqType));
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateCollectionLikeDeserializer() throws Exception {
        CollectionLikeType clt = TypeFactory.defaultInstance().constructCollectionLikeType(String.class, Integer.class);
        JsonDeserializer<?> deser = factory.createCollectionLikeDeserializer(ctxt, clt, mapper.getDeserializationConfig().introspect(clt));
        Assert.assertNull(deser);
    }

    @Test
    public void testCreateMapDeserializers() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType mapType = mapper.constructType(Map.class);
        JsonDeserializer<?> deser = factory.createMapDeserializer(ctxt, (MapType) mapType, config.introspect(mapType));
        Assert.assertNotNull(deser);

        JavaType enumMapType = TypeFactory.defaultInstance().constructMapType(EnumMap.class, TestEnum.class, String.class);
        deser = factory.createMapDeserializer(ctxt, (MapType) enumMapType, config.introspect(enumMapType));
        Assert.assertNotNull(deser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnumMapInvalidKey() throws Exception {
        JavaType enumMapType = TypeFactory.defaultInstance().constructMapType(EnumMap.class, String.class, String.class);
        factory.createMapDeserializer(ctxt, (MapType) enumMapType, mapper.getDeserializationConfig().introspect(enumMapType));
    }

    @Test
    public void testCreateMapLikeDeserializer() throws Exception {
        MapLikeType mlt = TypeFactory.defaultInstance().constructMapLikeType(String.class, String.class, Integer.class);
        JsonDeserializer<?> deser = factory.createMapLikeDeserializer(ctxt, mlt, mapper.getDeserializationConfig().introspect(mlt));
        Assert.assertNull(deser);
    }

    @Test
    public void testCreateEnumDeserializer() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType enumType = mapper.constructType(TestEnum.class);
        JsonDeserializer<?> deser = factory.createEnumDeserializer(ctxt, enumType, config.introspect(enumType));
        Assert.assertNotNull(deser);

        JavaType creatorEnumType = mapper.constructType(CustomEnumWithCreator.class);
        deser = factory.createEnumDeserializer(ctxt, creatorEnumType, config.introspect(creatorEnumType));
        Assert.assertNotNull(deser);

        JavaType jsonValEnumType = mapper.constructType(EnumWithJsonValue.class);
        deser = factory.createEnumDeserializer(ctxt, jsonValEnumType, config.introspect(jsonValEnumType));
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateTreeAndReferenceDeserializers() throws Exception {
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType nodeType = mapper.constructType(ObjectNode.class);
        JsonDeserializer<?> deser = factory.createTreeDeserializer(config, nodeType, config.introspect(nodeType));
        Assert.assertNotNull(deser);

        JavaType refType = TypeFactory.defaultInstance().constructReferenceType(AtomicReference.class, mapper.constructType(String.class));
        deser = factory.createReferenceDeserializer(ctxt, (ReferenceType) refType, config.introspect(refType));
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateKeyDeserializer() throws Exception {
        KeyDeserializer kd = factory.createKeyDeserializer(ctxt, mapper.constructType(String.class));
        Assert.assertNotNull(kd);

        kd = factory.createKeyDeserializer(ctxt, mapper.constructType(TestEnum.class));
        Assert.assertNotNull(kd);

        kd = factory.createKeyDeserializer(ctxt, mapper.constructType(CustomEnumWithCreator.class));
        Assert.assertNotNull(kd);

        kd = factory.createKeyDeserializer(ctxt, mapper.constructType(EnumWithJsonValue.class));
        Assert.assertNotNull(kd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateKeyDeserializerBadCreator() throws Exception {
        factory.createKeyDeserializer(ctxt, mapper.constructType(BadEnumWithNonStringCreator.class));
    }

    @Test
    public void testFindDefaultDeserializer() throws Exception {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType objType = mapper.constructType(Object.class);
        JsonDeserializer<?> deser = basicFactory.findDefaultDeserializer(ctxt, objType, config.introspect(objType));
        Assert.assertNotNull(deser);

        JavaType strType = mapper.constructType(String.class);
        deser = basicFactory.findDefaultDeserializer(ctxt, strType, config.introspect(strType));
        Assert.assertNotNull(deser);

        JavaType charSeqType = mapper.constructType(CharSequence.class);
        deser = basicFactory.findDefaultDeserializer(ctxt, charSeqType, config.introspect(charSeqType));
        Assert.assertNotNull(deser);

        JavaType iterType = mapper.constructType(Iterable.class);
        deser = basicFactory.findDefaultDeserializer(ctxt, iterType, config.introspect(iterType));
        Assert.assertNotNull(deser);

        JavaType mapEntryType = TypeFactory.defaultInstance().constructMapLikeType(Map.Entry.class, String.class, Integer.class);
        deser = basicFactory.findDefaultDeserializer(ctxt, mapEntryType, config.introspect(mapEntryType));
        Assert.assertNotNull(deser);

        JavaType intType = mapper.constructType(int.class);
        deser = basicFactory.findDefaultDeserializer(ctxt, intType, config.introspect(intType));
        Assert.assertNotNull(deser);

        JavaType dateType = mapper.constructType(Date.class);
        deser = basicFactory.findDefaultDeserializer(ctxt, dateType, config.introspect(dateType));
        Assert.assertNotNull(deser);

        JavaType tokenBufferType = mapper.constructType(TokenBuffer.class);
        deser = basicFactory.findDefaultDeserializer(ctxt, tokenBufferType, config.introspect(tokenBufferType));
        Assert.assertNotNull(deser);
    }

    @Test
    public void testMapAbstractCollectionTypeFallbacks() {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        DeserializationConfig config = mapper.getDeserializationConfig();

        JavaType collType = mapper.constructType(Collection.class);
        JavaType res = basicFactory._mapAbstractCollectionType(collType, config);
        Assert.assertEquals(ArrayList.class, res.getRawClass());

        JavaType setType = mapper.constructType(Set.class);
        res = basicFactory._mapAbstractCollectionType(setType, config);
        Assert.assertEquals(HashSet.class, res.getRawClass());

        JavaType sortedSetType = mapper.constructType(SortedSet.class);
        res = basicFactory._mapAbstractCollectionType(sortedSetType, config);
        Assert.assertEquals(TreeSet.class, res.getRawClass());

        JavaType queueType = mapper.constructType(Queue.class);
        res = basicFactory._mapAbstractCollectionType(queueType, config);
        Assert.assertEquals(LinkedList.class, res.getRawClass());

        JavaType dequeType = mapper.constructType(Deque.class);
        res = basicFactory._mapAbstractCollectionType(dequeType, config);
        Assert.assertEquals(LinkedList.class, res.getRawClass());

        JavaType navSetType = mapper.constructType(NavigableSet.class);
        res = basicFactory._mapAbstractCollectionType(navSetType, config);
        Assert.assertEquals(TreeSet.class, res.getRawClass());

        JavaType nonAbstract = mapper.constructType(ArrayList.class);
        res = basicFactory._mapAbstractCollectionType(nonAbstract, config);
        Assert.assertNull(res);
    }

    @Test
    public void testFindRemappedType() throws Exception {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        JavaType remapped = basicFactory._findRemappedType(mapper.getDeserializationConfig(), List.class);
        Assert.assertNull(remapped);

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, LinkedList.class);
        basicFactory = (TestBasicFactory) basicFactory.withAbstractTypeResolver(resolver);
        remapped = basicFactory._findRemappedType(mapper.getDeserializationConfig(), List.class);
        Assert.assertNotNull(remapped);
        Assert.assertEquals(LinkedList.class, remapped.getRawClass());
    }

    @Test
    public void testDeprecatedMethodsAndParamLookups() throws Exception {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        DeserializationConfig config = mapper.getDeserializationConfig();

        Assert.assertNull(basicFactory._findJsonValueFor(config, null));
        AnnotatedMethod am = basicFactory._findJsonValueFor(config, mapper.constructType(EnumWithJsonValue.class));
        Assert.assertNotNull(am);

        JavaType t = mapper.constructType(String.class);
        JavaType modified = basicFactory.modifyTypeByAnnotation(ctxt, null, t);
        Assert.assertEquals(t, modified);

        BeanDescription desc = config.introspect(t);
        JavaType resolved = basicFactory.resolveType(ctxt, desc, t, null);
        Assert.assertEquals(t, resolved);

        Assert.assertNull(basicFactory._findParamName(null, null));
        Assert.assertNull(basicFactory._findImplicitParamName(null, null));
        Assert.assertNull(basicFactory._findExplicitParamName(null, null));
        Assert.assertFalse(basicFactory._hasExplicitParamName(null, null));
    }

    @Test
    public void testConstructEnumResolver() {
        TestBasicFactory basicFactory = new TestBasicFactory(new DeserializerFactoryConfig());
        DeserializationConfig config = mapper.getDeserializationConfig();

        EnumResolver res1 = basicFactory.constructEnumResolver(TestEnum.class, config, null);
        Assert.assertNotNull(res1);
        Assert.assertEquals(TestEnum.class, res1.getEnumClass());

        BeanDescription desc = config.introspect(mapper.constructType(EnumWithJsonValue.class));
        EnumResolver res2 = basicFactory.constructEnumResolver(EnumWithJsonValue.class, config, desc.findJsonValueMethod());
        Assert.assertNotNull(res2);
    }
}
