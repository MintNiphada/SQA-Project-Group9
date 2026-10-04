package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonWrapped;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleValueInstantiators;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class BasicDeserializerFactoryTest {

    private ObjectMapper _mapper;
    private DeserializationContext _ctxt;
    private DeserializationConfig _config;
    private TypeFactory _tf;
    private BasicDeserializerFactory _factory;

    static class CustomFactory extends BasicDeserializerFactory {
        private static final long serialVersionUID = 1L;

        public CustomFactory(DeserializerFactoryConfig config) {
            super(config);
        }

        @Override
        protected DeserializerFactory withConfig(DeserializerFactoryConfig config) {
            return new CustomFactory(config);
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
        A, B, C;
    }

    enum EnumWithCreator {
        ITEM_A("a"), ITEM_B("b");

        private final String code;

        EnumWithCreator(String code) {
            this.code = code;
        }

        @JsonCreator
        public static EnumWithCreator fromCode(String code) {
            for (EnumWithCreator e : values()) {
                if (e.code.equalsIgnoreCase(code)) {
                    return e;
                }
            }
            return null;
        }
    }

    enum EnumWithNoArgCreator {
        VAL;

        @JsonCreator
        public static EnumWithNoArgCreator createDefault() {
            return VAL;
        }
    }

    enum EnumWithInvalidCreator {
        VAL;

        @JsonCreator
        public static EnumWithInvalidCreator create(int code) {
            return VAL;
        }
    }

    enum EnumWithJsonValue {
        X("x_val"), Y("y_val");

        private final String val;

        EnumWithJsonValue(String val) {
            this.val = val;
        }

        @JsonValue
        public String getVal() {
            return val;
        }
    }

    static class CustomValueInstantiator extends ValueInstantiator implements Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String getValueTypeDesc() {
            return "Custom";
        }

        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }

        @Override
        public Object createUsingDefault(DeserializationContext ctxt) {
            return "created";
        }
    }

    static class BeanWithSingleArgCtor {
        String _name;

        public BeanWithSingleArgCtor(String name) {
            _name = name;
        }
    }

    static class BeanWithCreatorProps {
        String _name;
        int _age;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public BeanWithCreatorProps(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            _name = name;
            _age = age;
        }
    }

    static class BeanWithDelegatingCreator {
        String _raw;

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public BeanWithDelegatingCreator(String raw) {
            _raw = raw;
        }
    }

    static class BeanWithUnwrappedCreator {
        @JsonCreator
        public BeanWithUnwrappedCreator(@JsonWrapped String name) {}
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    static class PolymorphicBase {}

    static class PolymorphicSub extends PolymorphicBase {}

    interface TestAbstractMap extends Map<String, String> {}
    interface TestAbstractCollection extends Collection<String> {}

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _ctxt = _mapper.getDeserializationContext();
        _config = _mapper.getDeserializationConfig();
        _tf = _mapper.getTypeFactory();
        _factory = new CustomFactory(new DeserializerFactoryConfig());
    }

    @Test
    public void testFactoryConfigurationFluency() {
        DeserializerFactoryConfig config = _factory.getFactoryConfig();
        Assert.assertNotNull(config);

        DeserializerFactory f2 = _factory.withAdditionalDeserializers(new SimpleDeserializers());
        Assert.assertNotSame(_factory, f2);
        Assert.assertTrue(f2.getFactoryConfig().hasDeserializers());

        DeserializerFactory f3 = _factory.withAdditionalKeyDeserializers(new SimpleKeyDeserializers());
        Assert.assertNotSame(_factory, f3);
        Assert.assertTrue(f3.getFactoryConfig().hasKeyDeserializers());

        DeserializerFactory f4 = _factory.withDeserializerModifier(new BeanDeserializerModifier());
        Assert.assertNotSame(_factory, f4);
        Assert.assertTrue(f4.getFactoryConfig().hasDeserializerModifiers());

        DeserializerFactory f5 = _factory.withAbstractTypeResolver(new SimpleAbstractTypeResolver());
        Assert.assertNotSame(_factory, f5);
        Assert.assertTrue(f5.getFactoryConfig().hasAbstractTypeResolvers());

        DeserializerFactory f6 = _factory.withValueInstantiators(new SimpleValueInstantiators());
        Assert.assertNotSame(_factory, f6);
        Assert.assertTrue(f6.getFactoryConfig().hasValueInstantiators());
    }

    @Test
    public void testMapAbstractType() throws Exception {
        JavaType mapType = _tf.constructType(Map.class);
        JavaType mapped = _factory.mapAbstractType(_config, mapType);
        Assert.assertEquals(mapType, mapped);

        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        DeserializerFactory f = _factory.withAbstractTypeResolver(resolver);

        JavaType csType = _tf.constructType(CharSequence.class);
        JavaType resolvedType = f.mapAbstractType(_config, csType);
        Assert.assertEquals(String.class, resolvedType.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractTypeInvalidResolution() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, (Class) Map.class);
        DeserializerFactory f = _factory.withAbstractTypeResolver(resolver);

        f.mapAbstractType(_config, _tf.constructType(List.class));
    }

    @Test
    public void testFindStdValueInstantiators() throws Exception {
        BeanDescription locDesc = _config.introspect(_tf.constructType(JsonLocation.class));
        ValueInstantiator inst = _factory.findValueInstantiator(_ctxt, locDesc);
        Assert.assertNotNull(inst);

        BeanDescription emptyListDesc = _config.introspect(_tf.constructType(Collections.EMPTY_LIST.getClass()));
        inst = _factory.findValueInstantiator(_ctxt, emptyListDesc);
        Assert.assertNotNull(inst);
        Assert.assertTrue(inst.canCreateUsingDefault());

        BeanDescription emptySetDesc = _config.introspect(_tf.constructType(Collections.EMPTY_SET.getClass()));
        inst = _factory.findValueInstantiator(_ctxt, emptySetDesc);
        Assert.assertNotNull(inst);

        BeanDescription emptyMapDesc = _config.introspect(_tf.constructType(Collections.EMPTY_MAP.getClass()));
        inst = _factory.findValueInstantiator(_ctxt, emptyMapDesc);
        Assert.assertNotNull(inst);
    }

    @Test
    public void testValueInstantiatorInstance() throws Exception {
        Assert.assertNull(_factory._valueInstantiatorInstance(_config, null, null));

        CustomValueInstantiator cvi = new CustomValueInstantiator();
        ValueInstantiator out = _factory._valueInstantiatorInstance(_config, null, cvi);
        Assert.assertSame(cvi, out);

        out = _factory._valueInstantiatorInstance(_config, null, CustomValueInstantiator.class);
        Assert.assertNotNull(out);
        Assert.assertEquals(CustomValueInstantiator.class, out.getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceInvalidClass() throws Exception {
        _factory._valueInstantiatorInstance(_config, null, String.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceInvalidObject() throws Exception {
        _factory._valueInstantiatorInstance(_config, null, 12345);
    }

    @Test
    public void testCreateArrayDeserializer() throws Exception {
        ArrayType primitiveArray = _tf.constructArrayType(int.class);
        BeanDescription beanDesc = _config.introspectClassAnnotations(primitiveArray);
        JsonDeserializer<?> deser = _factory.createArrayDeserializer(_ctxt, primitiveArray, beanDesc);
        Assert.assertNotNull(deser);

        ArrayType stringArray = _tf.constructArrayType(String.class);
        deser = _factory.createArrayDeserializer(_ctxt, stringArray, beanDesc);
        Assert.assertNotNull(deser);

        ArrayType objArray = _tf.constructArrayType(Object.class);
        deser = _factory.createArrayDeserializer(_ctxt, objArray, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateCollectionDeserializers() throws Exception {
        CollectionType listType = _tf.constructCollectionType(ArrayList.class, String.class);
        BeanDescription beanDesc = _config.introspect(listType);
        JsonDeserializer<?> deser = _factory.createCollectionDeserializer(_ctxt, listType, beanDesc);
        Assert.assertNotNull(deser);

        CollectionType abstractListType = _tf.constructCollectionType(List.class, String.class);
        beanDesc = _config.introspect(abstractListType);
        deser = _factory.createCollectionDeserializer(_ctxt, abstractListType, beanDesc);
        Assert.assertNotNull(deser);

        CollectionType queueType = _tf.constructCollectionType(ArrayBlockingQueue.class, Integer.class);
        beanDesc = _config.introspect(queueType);
        deser = _factory.createCollectionDeserializer(_ctxt, queueType, beanDesc);
        Assert.assertNotNull(deser);

        CollectionType enumSetType = _tf.constructCollectionType(EnumSet.class, TestEnum.class);
        beanDesc = _config.introspect(enumSetType);
        deser = _factory.createCollectionDeserializer(_ctxt, enumSetType, beanDesc);
        Assert.assertNotNull(deser);

        CollectionLikeType colLikeType = _tf.constructCollectionLikeType(String.class, Integer.class);
        beanDesc = _config.introspectClassAnnotations(colLikeType);
        deser = _factory.createCollectionLikeDeserializer(_ctxt, colLikeType, beanDesc);
        Assert.assertNull(deser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAbstractCollectionWithoutFallbackFails() throws Exception {
        CollectionType customAbstract = _tf.constructCollectionType(TestAbstractCollection.class, String.class);
        BeanDescription beanDesc = _config.introspect(customAbstract);
        _factory.createCollectionDeserializer(_ctxt, customAbstract, beanDesc);
    }

    @Test
    public void testCreateMapDeserializers() throws Exception {
        MapType mapType = _tf.constructMapType(HashMap.class, String.class, Object.class);
        BeanDescription beanDesc = _config.introspect(mapType);
        JsonDeserializer<?> deser = _factory.createMapDeserializer(_ctxt, mapType, beanDesc);
        Assert.assertNotNull(deser);

        MapType abstractMap = _tf.constructMapType(Map.class, String.class, String.class);
        beanDesc = _config.introspect(abstractMap);
        deser = _factory.createMapDeserializer(_ctxt, abstractMap, beanDesc);
        Assert.assertNotNull(deser);

        MapType enumMap = _tf.constructMapType(EnumMap.class, TestEnum.class, String.class);
        beanDesc = _config.introspect(enumMap);
        deser = _factory.createMapDeserializer(_ctxt, enumMap, beanDesc);
        Assert.assertNotNull(deser);

        MapLikeType mapLike = _tf.constructMapLikeType(String.class, String.class, Integer.class);
        beanDesc = _config.introspectClassAnnotations(mapLike);
        deser = _factory.createMapLikeDeserializer(_ctxt, mapLike, beanDesc);
        Assert.assertNull(deser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnumMapWithNonEnumKeyFails() throws Exception {
        MapType invalidEnumMap = _tf.constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription beanDesc = _config.introspect(invalidEnumMap);
        _factory.createMapDeserializer(_ctxt, invalidEnumMap, beanDesc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAbstractMapWithoutFallbackFails() throws Exception {
        MapType customAbstract = _tf.constructMapType(TestAbstractMap.class, String.class, String.class);
        BeanDescription beanDesc = _config.introspect(customAbstract);
        _factory.createMapDeserializer(_ctxt, customAbstract, beanDesc);
    }

    @Test
    public void testCreateEnumDeserializers() throws Exception {
        JavaType enumType = _tf.constructType(TestEnum.class);
        BeanDescription beanDesc = _config.introspect(enumType);
        JsonDeserializer<?> deser = _factory.createEnumDeserializer(_ctxt, enumType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType enumCreatorType = _tf.constructType(EnumWithCreator.class);
        beanDesc = _config.introspect(enumCreatorType);
        deser = _factory.createEnumDeserializer(_ctxt, enumCreatorType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType enumNoArgType = _tf.constructType(EnumWithNoArgCreator.class);
        beanDesc = _config.introspect(enumNoArgType);
        deser = _factory.createEnumDeserializer(_ctxt, enumNoArgType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType enumValueType = _tf.constructType(EnumWithJsonValue.class);
        beanDesc = _config.introspect(enumValueType);
        deser = _factory.createEnumDeserializer(_ctxt, enumValueType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateTreeAndReferenceDeserializer() throws Exception {
        JavaType nodeType = _tf.constructType(JsonNode.class);
        BeanDescription beanDesc = _config.introspect(nodeType);
        JsonDeserializer<?> deser = _factory.createTreeDeserializer(_config, nodeType, beanDesc);
        Assert.assertNotNull(deser);

        ReferenceType refType = _tf.constructReferenceType(AtomicReference.class, _tf.constructType(String.class));
        beanDesc = _config.introspect(refType);
        deser = _factory.createReferenceDeserializer(_ctxt, refType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateKeyDeserializer() throws Exception {
        JavaType stringType = _tf.constructType(String.class);
        KeyDeserializer kd = _factory.createKeyDeserializer(_ctxt, stringType);
        Assert.assertNotNull(kd);

        JavaType enumType = _tf.constructType(TestEnum.class);
        kd = _factory.createKeyDeserializer(_ctxt, enumType);
        Assert.assertNotNull(kd);

        JavaType enumCreatorType = _tf.constructType(EnumWithCreator.class);
        kd = _factory.createKeyDeserializer(_ctxt, enumCreatorType);
        Assert.assertNotNull(kd);

        JavaType enumValueType = _tf.constructType(EnumWithJsonValue.class);
        kd = _factory.createKeyDeserializer(_ctxt, enumValueType);
        Assert.assertNotNull(kd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnumKeyDeserializerInvalidMethod() throws Exception {
        JavaType invalidType = _tf.constructType(EnumWithInvalidCreator.class);
        _factory.createKeyDeserializer(_ctxt, invalidType);
    }

    @Test
    public void testFindDefaultDeserializer() throws Exception {
        JavaType objType = _tf.constructType(Object.class);
        BeanDescription beanDesc = _config.introspect(objType);
        JsonDeserializer<?> deser = _factory.findDefaultDeserializer(_ctxt, objType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType strType = _tf.constructType(String.class);
        beanDesc = _config.introspect(strType);
        deser = _factory.findDefaultDeserializer(_ctxt, strType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType csType = _tf.constructType(CharSequence.class);
        beanDesc = _config.introspect(csType);
        deser = _factory.findDefaultDeserializer(_ctxt, csType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType iterType = _tf.constructType(Iterable.class);
        beanDesc = _config.introspect(iterType);
        deser = _factory.findDefaultDeserializer(_ctxt, iterType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType mapEntryType = _tf.constructType(Map.Entry.class);
        beanDesc = _config.introspect(mapEntryType);
        deser = _factory.findDefaultDeserializer(_ctxt, mapEntryType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType tokenBufType = _tf.constructType(TokenBuffer.class);
        beanDesc = _config.introspect(tokenBufType);
        deser = _factory.findDefaultDeserializer(_ctxt, tokenBufType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType dateType = _tf.constructType(Date.class);
        beanDesc = _config.introspect(dateType);
        deser = _factory.findDefaultDeserializer(_ctxt, dateType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType intType = _tf.constructType(int.class);
        beanDesc = _config.introspect(intType);
        deser = _factory.findDefaultDeserializer(_ctxt, intType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testFindTypeDeserializer() throws Exception {
        JavaType polyType = _tf.constructType(PolymorphicBase.class);
        TypeDeserializer td = _factory.findTypeDeserializer(_config, polyType);
        Assert.assertNotNull(td);

        JavaType nonPolyType = _tf.constructType(String.class);
        td = _factory.findTypeDeserializer(_config, nonPolyType);
        Assert.assertNull(td);
    }

    @Test
    public void testValueInstantiatorConstructionBranches() throws Exception {
        BeanDescription singleArgDesc = _config.introspect(_tf.constructType(BeanWithSingleArgCtor.class));
        ValueInstantiator vi1 = _factory.findValueInstantiator(_ctxt, singleArgDesc);
        Assert.assertNotNull(vi1);
        Assert.assertTrue(vi1.canCreateFromString());

        BeanDescription propsDesc = _config.introspect(_tf.constructType(BeanWithCreatorProps.class));
        ValueInstantiator vi2 = _factory.findValueInstantiator(_ctxt, propsDesc);
        Assert.assertNotNull(vi2);
        Assert.assertTrue(vi2.canCreateFromObjectWith());

        BeanDescription delegDesc = _config.introspect(_tf.constructType(BeanWithDelegatingCreator.class));
        ValueInstantiator vi3 = _factory.findValueInstantiator(_ctxt, delegDesc);
        Assert.assertNotNull(vi3);
        Assert.assertTrue(vi3.canCreateFromString());
    }

    @Test(expected = JsonMappingException.class)
    public void testUnwrappedCreatorThrowsException() throws Exception {
        BeanDescription unwrappedDesc = _config.introspect(_tf.constructType(BeanWithUnwrappedCreator.class));
        _factory.findValueInstantiator(_ctxt, unwrappedDesc);
    }

    @Test
    public void testDeprecatedMethods() throws Exception {
        JavaType type = _tf.constructType(String.class);
        BeanDescription desc = _config.introspect(type);
        AnnotatedMember member = desc.getClassInfo();

        JavaType modified = _factory.modifyTypeByAnnotation(_ctxt, member, type);
        Assert.assertEquals(type, modified);

        JavaType resolved = _factory.resolveType(_ctxt, desc, type, member);
        Assert.assertEquals(type, resolved);

        Assert.assertNull(_factory._findJsonValueFor(_config, null));
        AnnotatedMethod jvMethod = _factory._findJsonValueFor(_config, _tf.constructType(EnumWithJsonValue.class));
        Assert.assertNotNull(jvMethod);
        Assert.assertEquals("getVal", jvMethod.getName());
    }
}
