package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class BasicDeserializerFactoryTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private DeserializationConfig config;
    private BasicDeserializerFactory factory;

    static enum TestEnum {
        A, B, C;
    }

    static enum EnumWithCreator {
        ONE, TWO;
        @JsonCreator
        public static EnumWithCreator fromString(String val) {
            return ONE;
        }
    }

    static enum EnumWithBadCreator {
        FOO;
        @JsonCreator
        public static EnumWithBadCreator fromInt(int v) {
            return FOO;
        }
    }

    static class CustomValueInstantiator extends ValueInstantiator implements Serializable {
        private static final long serialVersionUID = 1L;
        @Override
        public String getValueTypeDesc() {
            return "CustomValueInstantiator";
        }
        @Override
        public boolean canCreateUsingDefault() {
            return true;
        }
        @Override
        public Object createUsingDefault(DeserializationContext ctxt) {
            return new ArrayList<Object>();
        }
    }

    static class SubAtomicReference<T> extends AtomicReference<T> {
        private static final long serialVersionUID = 1L;
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        config = mapper.getDeserializationConfig();
        factory = BeanDeserializerFactory.instance;
    }

    @Test
    public void testFactoryConfigFluentMethods() {
        DeserializerFactory f = factory;
        Deserializers desers = new SimpleDeserializers();
        KeyDeserializers keyDesers = new SimpleKeyDeserializers();
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {};
        AbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        ValueInstantiators instantiators = new SimpleValueInstantiators();

        f = f.withAdditionalDeserializers(desers);
        f = f.withAdditionalKeyDeserializers(keyDesers);
        f = f.withDeserializerModifier(modifier);
        f = f.withAbstractTypeResolver(resolver);
        f = f.withValueInstantiators(instantiators);

        DeserializerFactoryConfig cfg = ((BasicDeserializerFactory) f).getFactoryConfig();
        Assert.assertTrue(cfg.hasDeserializers());
        Assert.assertTrue(cfg.hasKeyDeserializers());
        Assert.assertTrue(cfg.hasDeserializerModifiers());
        Assert.assertTrue(cfg.hasAbstractTypeResolvers());
        Assert.assertTrue(cfg.hasValueInstantiators());
    }

    @Test
    public void testMapAbstractType() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CharSequence.class, String.class);
        DeserializerFactory f = factory.withAbstractTypeResolver(resolver);

        JavaType type = mapper.getTypeFactory().constructType(CharSequence.class);
        JavaType mapped = f.mapAbstractType(config, type);
        Assert.assertEquals(String.class, mapped.getRawClass());

        JavaType nonMapped = f.mapAbstractType(config, mapper.getTypeFactory().constructType(Integer.class));
        Assert.assertEquals(Integer.class, nonMapped.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapAbstractTypeInvalidResolution() throws Exception {
        AbstractTypeResolver invalidResolver = new AbstractTypeResolver() {
            @Override
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                if (type.hasRawClass(CharSequence.class)) {
                    return config.constructType(Integer.class);
                }
                return null;
            }
        };
        DeserializerFactory f = factory.withAbstractTypeResolver(invalidResolver);
        JavaType type = mapper.getTypeFactory().constructType(CharSequence.class);
        f.mapAbstractType(config, type);
    }

    @Test
    public void testFindValueInstantiatorStdTypes() throws Exception {
        JavaType locType = mapper.getTypeFactory().constructType(JsonLocation.class);
        BeanDescription locDesc = config.introspect(locType);
        ValueInstantiator locInst = factory.findValueInstantiator(ctxt, locDesc);
        Assert.assertNotNull(locInst);
        Assert.assertTrue(locInst.canCreateFromObjectWith());

        JavaType emptyListType = mapper.getTypeFactory().constructType(Collections.EMPTY_LIST.getClass());
        BeanDescription listDesc = config.introspect(emptyListType);
        ValueInstantiator listInst = factory.findValueInstantiator(ctxt, listDesc);
        Assert.assertNotNull(listInst);
        Assert.assertSame(Collections.EMPTY_LIST, listInst.createUsingDefault(ctxt));

        JavaType emptySetType = mapper.getTypeFactory().constructType(Collections.EMPTY_SET.getClass());
        BeanDescription setDesc = config.introspect(emptySetType);
        ValueInstantiator setInst = factory.findValueInstantiator(ctxt, setDesc);
        Assert.assertNotNull(setInst);
        Assert.assertSame(Collections.EMPTY_SET, setInst.createUsingDefault(ctxt));

        JavaType emptyMapType = mapper.getTypeFactory().constructType(Collections.EMPTY_MAP.getClass());
        BeanDescription mapDesc = config.introspect(emptyMapType);
        ValueInstantiator mapInst = factory.findValueInstantiator(ctxt, mapDesc);
        Assert.assertNotNull(mapInst);
        Assert.assertSame(Collections.EMPTY_MAP, mapInst.createUsingDefault(ctxt));
    }

    @Test
    public void testValueInstantiatorInstance() throws Exception {
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, mapper.getTypeFactory().constructType(String.class), config);
        ValueInstantiator inst = factory._valueInstantiatorInstance(config, ac, null);
        Assert.assertNull(inst);

        CustomValueInstantiator custom = new CustomValueInstantiator();
        inst = factory._valueInstantiatorInstance(config, ac, custom);
        Assert.assertSame(custom, inst);

        inst = factory._valueInstantiatorInstance(config, ac, CustomValueInstantiator.class);
        Assert.assertTrue(inst instanceof CustomValueInstantiator);
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceBadClass() throws Exception {
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, mapper.getTypeFactory().constructType(String.class), config);
        factory._valueInstantiatorInstance(config, ac, String.class);
    }

    @Test(expected = IllegalStateException.class)
    public void testValueInstantiatorInstanceNonClassNonInst() throws Exception {
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, mapper.getTypeFactory().constructType(String.class), config);
        factory._valueInstantiatorInstance(config, ac, "notAClassOrInstantiator");
    }

    @Test
    public void testCreateArrayDeserializer() throws Exception {
        ArrayType strArrType = mapper.getTypeFactory().constructArrayType(String.class);
        BeanDescription beanDesc = config.introspect(strArrType);
        JsonDeserializer<?> deser = factory.createArrayDeserializer(ctxt, strArrType, beanDesc);
        Assert.assertNotNull(deser);

        ArrayType intArrType = mapper.getTypeFactory().constructArrayType(int.class);
        beanDesc = config.introspect(intArrType);
        deser = factory.createArrayDeserializer(ctxt, intArrType, beanDesc);
        Assert.assertNotNull(deser);

        ArrayType objArrType = mapper.getTypeFactory().constructArrayType(Object.class);
        beanDesc = config.introspect(objArrType);
        deser = factory.createArrayDeserializer(ctxt, objArrType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateCollectionDeserializers() throws Exception {
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        BeanDescription beanDesc = config.introspect(listType);
        JsonDeserializer<?> deser = factory.createCollectionDeserializer(ctxt, listType, beanDesc);
        Assert.assertNotNull(deser);

        CollectionType absListType = mapper.getTypeFactory().constructCollectionType(List.class, Integer.class);
        beanDesc = config.introspect(absListType);
        deser = factory.createCollectionDeserializer(ctxt, absListType, beanDesc);
        Assert.assertNotNull(deser);

        CollectionType enumSetType = mapper.getTypeFactory().constructCollectionType(EnumSet.class, TestEnum.class);
        beanDesc = config.introspect(enumSetType);
        deser = factory.createCollectionDeserializer(ctxt, enumSetType, beanDesc);
        Assert.assertNotNull(deser);

        CollectionType queueType = mapper.getTypeFactory().constructCollectionType(ArrayBlockingQueue.class, String.class);
        beanDesc = config.introspect(queueType);
        deser = factory.createCollectionDeserializer(ctxt, queueType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testMapAbstractCollectionType() {
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        CollectionType res = factory._mapAbstractCollectionType(listType, config);
        Assert.assertNotNull(res);
        Assert.assertEquals(ArrayList.class, res.getRawClass());

        JavaType setType = mapper.getTypeFactory().constructCollectionType(Set.class, String.class);
        res = factory._mapAbstractCollectionType(setType, config);
        Assert.assertNotNull(res);
        Assert.assertEquals(HashSet.class, res.getRawClass());

        JavaType dequeType = mapper.getTypeFactory().constructCollectionType(Deque.class, String.class);
        res = factory._mapAbstractCollectionType(dequeType, config);
        Assert.assertNotNull(res);
        Assert.assertEquals(LinkedList.class, res.getRawClass());

        JavaType concListType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        res = factory._mapAbstractCollectionType(concListType, config);
        Assert.assertNull(res);
    }

    @Test
    public void testCreateCollectionLikeDeserializer() throws Exception {
        CollectionLikeType colLikeType = mapper.getTypeFactory().constructCollectionLikeType(ArrayList.class, String.class);
        BeanDescription beanDesc = config.introspect(colLikeType);
        JsonDeserializer<?> deser = factory.createCollectionLikeDeserializer(ctxt, colLikeType, beanDesc);
        Assert.assertNull(deser);
    }

    @Test
    public void testCreateMapDeserializer() throws Exception {
        MapType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        BeanDescription beanDesc = config.introspect(mapType);
        JsonDeserializer<?> deser = factory.createMapDeserializer(ctxt, mapType, beanDesc);
        Assert.assertNotNull(deser);

        MapType absMapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        beanDesc = config.introspect(absMapType);
        deser = factory.createMapDeserializer(ctxt, absMapType, beanDesc);
        Assert.assertNotNull(deser);

        MapType enumMapType = mapper.getTypeFactory().constructMapType(EnumMap.class, TestEnum.class, String.class);
        beanDesc = config.introspect(enumMapType);
        deser = factory.createMapDeserializer(ctxt, enumMapType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEnumMapNonEnumKeyThrows() throws Exception {
        MapType badEnumMap = mapper.getTypeFactory().constructMapType(EnumMap.class, String.class, String.class);
        BeanDescription beanDesc = config.introspect(badEnumMap);
        factory.createMapDeserializer(ctxt, badEnumMap, beanDesc);
    }

    @Test
    public void testCreateMapLikeDeserializer() throws Exception {
        MapLikeType mapLikeType = mapper.getTypeFactory().constructMapLikeType(HashMap.class, String.class, String.class);
        BeanDescription beanDesc = config.introspect(mapLikeType);
        JsonDeserializer<?> deser = factory.createMapLikeDeserializer(ctxt, mapLikeType, beanDesc);
        Assert.assertNull(deser);
    }

    @Test
    public void testCreateEnumDeserializer() throws Exception {
        JavaType enumType = mapper.getTypeFactory().constructType(TestEnum.class);
        BeanDescription beanDesc = config.introspect(enumType);
        JsonDeserializer<?> deser = factory.createEnumDeserializer(ctxt, enumType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType enumWithCreatorType = mapper.getTypeFactory().constructType(EnumWithCreator.class);
        beanDesc = config.introspect(enumWithCreatorType);
        deser = factory.createEnumDeserializer(ctxt, enumWithCreatorType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateTreeDeserializer() throws Exception {
        JavaType objNodeType = mapper.getTypeFactory().constructType(ObjectNode.class);
        BeanDescription beanDesc = config.introspect(objNodeType);
        JsonDeserializer<?> deser = factory.createTreeDeserializer(config, objNodeType, beanDesc);
        Assert.assertNotNull(deser);

        JavaType arrNodeType = mapper.getTypeFactory().constructType(ArrayNode.class);
        beanDesc = config.introspect(arrNodeType);
        deser = factory.createTreeDeserializer(config, arrNodeType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateReferenceDeserializer() throws Exception {
        ReferenceType refType = ReferenceType.upgradeFrom(
                mapper.getTypeFactory().constructType(AtomicReference.class),
                mapper.getTypeFactory().constructType(String.class));
        BeanDescription beanDesc = config.introspect(refType);
        JsonDeserializer<?> deser = factory.createReferenceDeserializer(ctxt, refType, beanDesc);
        Assert.assertNotNull(deser);

        ReferenceType subRefType = ReferenceType.upgradeFrom(
                mapper.getTypeFactory().constructType(SubAtomicReference.class),
                mapper.getTypeFactory().constructType(String.class));
        beanDesc = config.introspect(subRefType);
        deser = factory.createReferenceDeserializer(ctxt, subRefType, beanDesc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateKeyDeserializer() throws Exception {
        JavaType stringType = mapper.getTypeFactory().constructType(String.class);
        KeyDeserializer kd = factory.createKeyDeserializer(ctxt, stringType);
        Assert.assertNotNull(kd);

        JavaType enumType = mapper.getTypeFactory().constructType(TestEnum.class);
        kd = factory.createKeyDeserializer(ctxt, enumType);
        Assert.assertNotNull(kd);

        JavaType enumCreatorType = mapper.getTypeFactory().constructType(EnumWithCreator.class);
        kd = factory.createKeyDeserializer(ctxt, enumCreatorType);
        Assert.assertNotNull(kd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateKeyDeserializerBadCreator() throws Exception {
        JavaType badEnumType = mapper.getTypeFactory().constructType(EnumWithBadCreator.class);
        factory.createKeyDeserializer(ctxt, badEnumType);
    }

    @Test
    public void testFindDefaultDeserializer() throws Exception {
        JavaType objType = mapper.getTypeFactory().constructType(Object.class);
        BeanDescription desc = config.introspect(objType);
        JsonDeserializer<?> deser = factory.findDefaultDeserializer(ctxt, objType, desc);
        Assert.assertNotNull(deser);

        JavaType strType = mapper.getTypeFactory().constructType(String.class);
        desc = config.introspect(strType);
        deser = factory.findDefaultDeserializer(ctxt, strType, desc);
        Assert.assertNotNull(deser);

        JavaType charSeqType = mapper.getTypeFactory().constructType(CharSequence.class);
        desc = config.introspect(charSeqType);
        deser = factory.findDefaultDeserializer(ctxt, charSeqType, desc);
        Assert.assertNotNull(deser);

        JavaType iterType = mapper.getTypeFactory().constructType(Iterable.class);
        desc = config.introspect(iterType);
        deser = factory.findDefaultDeserializer(ctxt, iterType, desc);
        Assert.assertNotNull(deser);

        JavaType entryType = mapper.getTypeFactory().constructType(Map.Entry.class);
        desc = config.introspect(entryType);
        deser = factory.findDefaultDeserializer(ctxt, entryType, desc);
        Assert.assertNotNull(deser);

        JavaType tokenBufType = mapper.getTypeFactory().constructType(TokenBuffer.class);
        desc = config.introspect(tokenBufType);
        deser = factory.findDefaultDeserializer(ctxt, tokenBufType, desc);
        Assert.assertNotNull(deser);

        JavaType dateType = mapper.getTypeFactory().constructType(Date.class);
        desc = config.introspect(dateType);
        deser = factory.findDefaultDeserializer(ctxt, dateType, desc);
        Assert.assertNotNull(deser);

        JavaType intType = mapper.getTypeFactory().constructType(int.class);
        desc = config.introspect(intType);
        deser = factory.findDefaultDeserializer(ctxt, intType, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testConstructEnumResolver() {
        EnumResolver res = factory.constructEnumResolver(TestEnum.class, config, null);
        Assert.assertNotNull(res);
        Assert.assertEquals(TestEnum.class, res.getEnumClass());
    }

    @Test
    public void testDeprecatedMethods() throws Exception {
        JavaType type = mapper.getTypeFactory().constructType(String.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, type, config);
        BeanDescription desc = config.introspect(type);

        JavaType modified = factory.modifyTypeByAnnotation(ctxt, ac, type);
        Assert.assertEquals(type, modified);

        JavaType resolved = factory.resolveType(ctxt, desc, type, (AnnotatedMember) null);
        Assert.assertEquals(type, resolved);

        Assert.assertNull(factory._findJsonValueFor(config, null));
        Assert.assertNull(factory._findJsonValueFor(config, type));
    }
}
