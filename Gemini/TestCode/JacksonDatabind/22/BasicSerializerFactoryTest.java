package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.std.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BasicSerializerFactoryTest {

    private ObjectMapper mapper;
    private SerializationConfig config;
    private DefaultSerializerProvider.Impl provider;
    private TypeFactory typeFactory;
    private TestBasicSerializerFactory factory;

    static class TestBasicSerializerFactory extends BasicSerializerFactory {
        public TestBasicSerializerFactory(SerializerFactoryConfig config) {
            super(config);
        }

        @Override
        public SerializerFactory withConfig(SerializerFactoryConfig config) {
            return new TestBasicSerializerFactory(config);
        }

        @Override
        public JsonSerializer<Object> createSerializer(SerializerProvider prov, JavaType type) {
            return null;
        }

        @Override
        protected Iterable<Serializers> customSerializers() {
            return _factoryConfig.serializers();
        }
    }

    enum TestEnum {
        A, B, C
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum EnumAsObject {
        ONE, TWO;
        public int getVal() { return 1; }
    }

    static class JsonSerializableBean implements JsonSerializable {
        @Override
        public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {}
        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {}
    }

    static class ValueBean {
        private final String text;
        public ValueBean(String text) { this.text = text; }

        @JsonValue
        public String getText() { return text; }
    }

    static class ValueBeanIntKey {
        private final int code;
        public ValueBeanIntKey(int code) { this.code = code; }

        @JsonValue
        public int getCode() { return code; }
    }

    static class UpperConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return value == null ? null : value.toUpperCase();
        }
    }

    static class ConvertedBean {
        @JsonSerialize(converter = UpperConverter.class)
        public String value = "hello";
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    static class FormattedNumber extends Number {
        private final long v;
        public FormattedNumber(long v) { this.v = v; }
        @Override public int intValue() { return (int) v; }
        @Override public long longValue() { return v; }
        @Override public float floatValue() { return (float) v; }
        @Override public double doubleValue() { return (double) v; }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class ObjectFormattedNumber extends Number {
        @JsonProperty public int num = 42;
        @Override public int intValue() { return num; }
        @Override public long longValue() { return num; }
        @Override public float floatValue() { return num; }
        @Override public double doubleValue() { return num; }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayFormattedNumber extends Number {
        @Override public int intValue() { return 0; }
        @Override public long longValue() { return 0; }
        @Override public float floatValue() { return 0; }
        @Override public double doubleValue() { return 0; }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class ObjectCollection extends ArrayList<String> {}

    @JsonInclude(content = JsonInclude.Include.NON_EMPTY)
    static class NonEmptyContentMap extends HashMap<String, String> {}

    @JsonInclude(content = JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultContentMap extends HashMap<String, String> {}

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    static class StaticTypingClass {}

    @JsonSerialize(typing = JsonSerialize.Typing.DYNAMIC)
    static class DynamicTypingClass {}

    static class CustomKeySerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeFieldName("custom:" + value);
        }
    }

    static class CustomMapLike<K, V> {}
    static class CustomCollectionLike<E> {}

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getSerializationConfig();
        provider = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        typeFactory = mapper.getTypeFactory();
        factory = new TestBasicSerializerFactory(new SerializerFactoryConfig());
    }

    @Test
    public void testFactoryConfigurationAndFluency() {
        SerializerFactoryConfig initialConfig = factory.getFactoryConfig();
        Assert.assertNotNull(initialConfig);

        Serializers.Base serializers = new Serializers.Base();
        SerializerFactory f2 = factory.withAdditionalSerializers(serializers);
        Assert.assertNotSame(factory, f2);
        Assert.assertTrue(f2.getFactoryConfig().hasSerializers());

        Serializers.Base keySerializers = new Serializers.Base();
        SerializerFactory f3 = factory.withAdditionalKeySerializers(keySerializers);
        Assert.assertNotSame(factory, f3);
        Assert.assertTrue(f3.getFactoryConfig().hasKeySerializers());

        BeanSerializerModifier modifier = new BeanSerializerModifier();
        SerializerFactory f4 = factory.withSerializerModifier(modifier);
        Assert.assertNotSame(factory, f4);
        Assert.assertTrue(f4.getFactoryConfig().hasSerializerModifiers());

        TestBasicSerializerFactory nullConfigFactory = new TestBasicSerializerFactory(null);
        Assert.assertNotNull(nullConfigFactory.getFactoryConfig());
    }

    @Test
    public void testCreateKeySerializerDefaultsAndCustom() {
        JavaType stringType = typeFactory.constructType(String.class);
        JsonSerializer<Object> keySer = factory.createKeySerializer(config, stringType, null);
        Assert.assertNotNull(keySer);
        Assert.assertTrue(keySer instanceof StdKeySerializer || keySer instanceof StdKeySerializers.StringKeySerializer);

        JsonSerializer<Object> customDefault = new CustomKeySerializer();
        JsonSerializer<Object> keySer2 = factory.createKeySerializer(config, stringType, customDefault);
        Assert.assertSame(customDefault, keySer2);

        // With module/custom key serializers
        Serializers.Base keySerializers = new Serializers.Base() {
            @Override
            public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
                if (type.getRawClass() == Integer.class) {
                    return customDefault;
                }
                return null;
            }
        };
        SerializerFactory factoryWithKeys = factory.withAdditionalKeySerializers(keySerializers);
        JsonSerializer<Object> foundKeySer = factoryWithKeys.createKeySerializer(config, typeFactory.constructType(Integer.class), null);
        Assert.assertSame(customDefault, foundKeySer);

        // With @JsonValue on key class
        JavaType valueKeyType = typeFactory.constructType(ValueBean.class);
        JsonSerializer<Object> valueKeySer = factory.createKeySerializer(config, valueKeyType, null);
        Assert.assertNotNull(valueKeySer);
        Assert.assertTrue(valueKeySer instanceof JsonValueSerializer);

        JavaType intValueKeyType = typeFactory.constructType(ValueBeanIntKey.class);
        JsonSerializer<Object> intValueKeySer = factory.createKeySerializer(config, intValueKeyType, null);
        Assert.assertNotNull(intValueKeySer);
        Assert.assertTrue(intValueKeySer instanceof JsonValueSerializer);

        // With SerializerModifier modifying key serializer
        final JsonSerializer<Object> modifiedSer = new CustomKeySerializer();
        BeanSerializerModifier mod = new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifyKeySerializer(SerializationConfig config, JavaType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return modifiedSer;
            }
        };
        SerializerFactory factoryWithModifier = factory.withSerializerModifier(mod);
        JsonSerializer<Object> modifiedKeySer = factoryWithModifier.createKeySerializer(config, stringType, null);
        Assert.assertSame(modifiedSer, modifiedKeySer);
    }

    @Test
    public void testCreateTypeSerializer() {
        JavaType listType = typeFactory.constructCollectionType(List.class, String.class);
        TypeSerializer tsNull = factory.createTypeSerializer(config, listType);
        Assert.assertNull(tsNull);

        ObjectMapper typedMapper = new ObjectMapper();
        typedMapper.enableDefaultTyping();
        SerializationConfig typedConfig = typedMapper.getSerializationConfig();
        TypeSerializer tsDefault = factory.createTypeSerializer(typedConfig, typeFactory.constructType(Object.class));
        Assert.assertNotNull(tsDefault);
    }

    @Test
    public void testFindSerializerByLookupConcreteAndLazy() {
        BeanDescription beanDesc = config.introspectClassAnnotations(String.class);

        // Concrete types
        JavaType strType = typeFactory.constructType(String.class);
        Assert.assertTrue(factory.findSerializerByLookup(strType, config, beanDesc, false) instanceof StringSerializer);

        JavaType sbType = typeFactory.constructType(StringBuffer.class);
        Assert.assertSame(ToStringSerializer.instance, factory.findSerializerByLookup(sbType, config, beanDesc, false));

        JavaType boolType = typeFactory.constructType(Boolean.class);
        Assert.assertTrue(factory.findSerializerByLookup(boolType, config, beanDesc, false) instanceof BooleanSerializer);

        JavaType bigIntType = typeFactory.constructType(BigInteger.class);
        Assert.assertTrue(factory.findSerializerByLookup(bigIntType, config, beanDesc, false) instanceof NumberSerializer);

        JavaType bigDecType = typeFactory.constructType(BigDecimal.class);
        Assert.assertTrue(factory.findSerializerByLookup(bigDecType, config, beanDesc, false) instanceof NumberSerializer);

        JavaType calType = typeFactory.constructType(Calendar.class);
        Assert.assertSame(CalendarSerializer.instance, factory.findSerializerByLookup(calType, config, beanDesc, false));

        JavaType dateType = typeFactory.constructType(Date.class);
        Assert.assertSame(DateSerializer.instance, factory.findSerializerByLookup(dateType, config, beanDesc, false));

        JavaType tsType = typeFactory.constructType(Timestamp.class);
        Assert.assertSame(DateSerializer.instance, factory.findSerializerByLookup(tsType, config, beanDesc, false));

        // Lazy types
        JavaType sqlDateType = typeFactory.constructType(java.sql.Date.class);
        Assert.assertTrue(factory.findSerializerByLookup(sqlDateType, config, beanDesc, false) instanceof SqlDateSerializer);

        JavaType sqlTimeType = typeFactory.constructType(Time.class);
        Assert.assertTrue(factory.findSerializerByLookup(sqlTimeType, config, beanDesc, false) instanceof SqlTimeSerializer);

        JavaType tbType = typeFactory.constructType(TokenBuffer.class);
        Assert.assertTrue(factory.findSerializerByLookup(tbType, config, beanDesc, false) instanceof TokenBufferSerializer);

        // AtomicReference / ReferenceType
        JavaType refType = typeFactory.constructReferenceType(AtomicReference.class, strType);
        Assert.assertTrue(factory.findSerializerByLookup(refType, config, beanDesc, false) instanceof AtomicReferenceSerializer);

        // Non-standard type returns null
        JavaType unknown = typeFactory.constructType(ValueBean.class);
        Assert.assertNull(factory.findSerializerByLookup(unknown, config, beanDesc, false));
    }

    @Test
    public void testFindSerializerByAnnotations() throws Exception {
        // JsonSerializable
        JavaType jsType = typeFactory.constructType(JsonSerializableBean.class);
        BeanDescription jsDesc = config.introspect(jsType);
        JsonSerializer<?> jsSer = factory.findSerializerByAnnotations(provider, jsType, jsDesc);
        Assert.assertSame(SerializableSerializer.instance, jsSer);

        // @JsonValue
        JavaType jvType = typeFactory.constructType(ValueBean.class);
        BeanDescription jvDesc = config.introspect(jvType);
        JsonSerializer<?> jvSer = factory.findSerializerByAnnotations(provider, jvType, jvDesc);
        Assert.assertTrue(jvSer instanceof JsonValueSerializer);

        // None
        JavaType strType = typeFactory.constructType(String.class);
        BeanDescription strDesc = config.introspect(strType);
        Assert.assertNull(factory.findSerializerByAnnotations(provider, strType, strDesc));
    }

    @Test
    public void testFindSerializerByPrimaryType() throws Exception {
        // Calendar & Date
        JavaType calType = typeFactory.constructType(GregorianCalendar.class);
        Assert.assertSame(CalendarSerializer.instance, factory.findSerializerByPrimaryType(provider, calType, config.introspect(calType), false));

        JavaType dateType = typeFactory.constructType(java.sql.Timestamp.class);
        Assert.assertSame(DateSerializer.instance, factory.findSerializerByPrimaryType(provider, dateType, config.introspect(dateType), false));

        // Map.Entry
        JavaType entryType = typeFactory.constructMapLikeType(Map.Entry.class, String.class, Integer.class);
        Assert.assertTrue(factory.findSerializerByPrimaryType(provider, entryType, config.introspect(entryType), false) instanceof MapEntrySerializer);

        // ByteBuffer, InetAddress, InetSocketAddress, TimeZone, Charset
        JavaType bbType = typeFactory.constructType(ByteBuffer.class);
        Assert.assertTrue(factory.findSerializerByPrimaryType(provider, bbType, config.introspect(bbType), false) instanceof ByteBufferSerializer);

        JavaType inetAddrType = typeFactory.constructType(InetAddress.class);
        Assert.assertTrue(factory.findSerializerByPrimaryType(provider, inetAddrType, config.introspect(inetAddrType), false) instanceof InetAddressSerializer);

        JavaType inetSockType = typeFactory.constructType(InetSocketAddress.class);
        Assert.assertTrue(factory.findSerializerByPrimaryType(provider, inetSockType, config.introspect(inetSockType), false) instanceof InetSocketAddressSerializer);

        JavaType tzType = typeFactory.constructType(TimeZone.class);
        Assert.assertTrue(factory.findSerializerByPrimaryType(provider, tzType, config.introspect(tzType), false) instanceof TimeZoneSerializer);

        JavaType csType = typeFactory.constructType(Charset.class);
        Assert.assertSame(ToStringSerializer.instance, factory.findSerializerByPrimaryType(provider, csType, config.introspect(csType), false));

        // Number with various JsonFormat shapes
        JavaType plainNum = typeFactory.constructType(Long.class);
        Assert.assertSame(NumberSerializer.instance, factory.findSerializerByPrimaryType(provider, plainNum, config.introspect(plainNum), false));

        JavaType strNum = typeFactory.constructType(FormattedNumber.class);
        Assert.assertSame(ToStringSerializer.instance, factory.findSerializerByPrimaryType(provider, strNum, config.introspect(strNum), false));

        JavaType objNum = typeFactory.constructType(ObjectFormattedNumber.class);
        Assert.assertNull(factory.findSerializerByPrimaryType(provider, objNum, config.introspect(objNum), false));

        JavaType arrNum = typeFactory.constructType(ArrayFormattedNumber.class);
        Assert.assertNull(factory.findSerializerByPrimaryType(provider, arrNum, config.introspect(arrNum), false));

        // Enum
        JavaType enumType = typeFactory.constructType(TestEnum.class);
        Assert.assertTrue(factory.findSerializerByPrimaryType(provider, enumType, config.introspect(enumType), false) instanceof EnumSerializer);

        // Return null for others
        JavaType objectType = typeFactory.constructType(Object.class);
        Assert.assertNull(factory.findSerializerByPrimaryType(provider, objectType, config.introspect(objectType), false));
    }

    @Test
    public void testFindSerializerByAddonType() throws Exception {
        JavaType iterType = typeFactory.constructParametricType(Iterator.class, String.class);
        Assert.assertTrue(factory.findSerializerByAddonType(config, iterType, config.introspect(iterType), false) instanceof IteratorSerializer);

        JavaType iterableType = typeFactory.constructParametricType(Iterable.class, String.class);
        Assert.assertTrue(factory.findSerializerByAddonType(config, iterableType, config.introspect(iterableType), false) instanceof IterableSerializer);

        JavaType csType = typeFactory.constructType(CharSequence.class);
        Assert.assertSame(ToStringSerializer.instance, factory.findSerializerByAddonType(config, csType, config.introspect(csType), false));

        JavaType otherType = typeFactory.constructType(Object.class);
        Assert.assertNull(factory.findSerializerByAddonType(config, otherType, config.introspect(otherType), false));
    }

    @Test
    public void testBuildContainerSerializerCollections() throws Exception {
        // ArrayList<String> -> IndexedStringListSerializer
        JavaType strListType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        JsonSerializer<?> ser1 = factory.buildContainerSerializer(provider, strListType, config.introspect(strListType), false);
        Assert.assertSame(IndexedStringListSerializer.instance, ser1);

        // LinkedList<String> -> StringCollectionSerializer
        JavaType strLinkedListType = typeFactory.constructCollectionType(LinkedList.class, String.class);
        JsonSerializer<?> ser2 = factory.buildContainerSerializer(provider, strLinkedListType, config.introspect(strLinkedListType), false);
        Assert.assertSame(StringCollectionSerializer.instance, ser2);

        // ArrayList<Integer> -> IndexedListSerializer
        JavaType intListType = typeFactory.constructCollectionType(ArrayList.class, Integer.class);
        JsonSerializer<?> ser3 = factory.buildContainerSerializer(provider, intListType, config.introspect(intListType), false);
        Assert.assertTrue(ser3 instanceof IndexedListSerializer);

        // HashSet<Integer> -> CollectionSerializer
        JavaType intSetType = typeFactory.constructCollectionType(HashSet.class, Integer.class);
        JsonSerializer<?> ser4 = factory.buildContainerSerializer(provider, intSetType, config.introspect(intSetType), false);
        Assert.assertTrue(ser4 instanceof CollectionSerializer);

        // EnumSet
        JavaType enumSetType = typeFactory.constructCollectionType(EnumSet.class, TestEnum.class);
        JsonSerializer<?> ser5 = factory.buildContainerSerializer(provider, enumSetType, config.introspect(enumSetType), false);
        Assert.assertTrue(ser5 instanceof EnumSetSerializer);

        // @JsonFormat(shape=OBJECT) on Collection -> null
        JavaType objCollType = typeFactory.constructCollectionType(ObjectCollection.class, String.class);
        JsonSerializer<?> ser6 = factory.buildContainerSerializer(provider, objCollType, config.introspect(objCollType), false);
        Assert.assertNull(ser6);
    }

    @Test
    public void testBuildContainerSerializerMaps() throws Exception {
        JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        JsonSerializer<?> mapSer = factory.buildContainerSerializer(provider, mapType, config.introspect(mapType), false);
        Assert.assertTrue(mapSer instanceof MapSerializer);

        JavaType nonEmptyMapType = typeFactory.constructMapType(NonEmptyContentMap.class, String.class, String.class);
        JsonSerializer<?> neMapSer = factory.buildContainerSerializer(provider, nonEmptyMapType, config.introspect(nonEmptyMapType), false);
        Assert.assertTrue(neMapSer instanceof MapSerializer);

        JavaType nonDefMapType = typeFactory.constructMapType(NonDefaultContentMap.class, String.class, String.class);
        JsonSerializer<?> ndMapSer = factory.buildContainerSerializer(provider, nonDefMapType, config.introspect(nonDefMapType), false);
        Assert.assertTrue(ndMapSer instanceof MapSerializer);
    }

    @Test
    public void testBuildContainerSerializerArrays() throws Exception {
        JavaType strArrType = typeFactory.constructArrayType(String.class);
        JsonSerializer<?> strArrSer = factory.buildContainerSerializer(provider, strArrType, config.introspect(strArrType), false);
        Assert.assertSame(StringArraySerializer.instance, strArrSer);

        JavaType intArrType = typeFactory.constructArrayType(int.class);
        JsonSerializer<?> intArrSer = factory.buildContainerSerializer(provider, intArrType, config.introspect(intArrType), false);
        Assert.assertNotNull(intArrSer);

        JavaType objArrType = typeFactory.constructArrayType(Object.class);
        JsonSerializer<?> objArrSer = factory.buildContainerSerializer(provider, objArrType, config.introspect(objArrType), false);
        Assert.assertTrue(objArrSer instanceof ObjectArraySerializer);
    }

    @Test
    public void testCustomAndModifiersOnContainerSerializers() throws Exception {
        final JsonSerializer<Object> customCollSer = new ToStringSerializer();
        final JsonSerializer<Object> customMapSer = new ToStringSerializer();
        final JsonSerializer<Object> customArraySer = new ToStringSerializer();

        Serializers.Base customSer = new Serializers.Base() {
            @Override
            public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
                return customCollSer;
            }
            @Override
            public JsonSerializer<?> findMapSerializer(SerializationConfig config, MapType type, BeanDescription beanDesc, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
                return customMapSer;
            }
            @Override
            public JsonSerializer<?> findArraySerializer(SerializationConfig config, ArrayType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
                return customArraySer;
            }
        };

        BeanSerializerModifier mod = new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifyCollectionSerializer(SerializationConfig config, CollectionType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return serializer;
            }
            @Override
            public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return serializer;
            }
            @Override
            public JsonSerializer<?> modifyArraySerializer(SerializationConfig config, ArrayType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return serializer;
            }
        };

        SerializerFactory customFactory = factory.withAdditionalSerializers(customSer).withSerializerModifier(mod);
        TestBasicSerializerFactory tbf = (TestBasicSerializerFactory) customFactory;

        JavaType colType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        Assert.assertSame(customCollSer, tbf.buildContainerSerializer(provider, colType, config.introspect(colType), false));

        JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
        Assert.assertSame(customMapSer, tbf.buildContainerSerializer(provider, mapType, config.introspect(mapType), false));

        JavaType arrType = typeFactory.constructArrayType(String.class);
        Assert.assertSame(customArraySer, tbf.buildContainerSerializer(provider, arrType, config.introspect(arrType), false));
    }

    @Test
    public void testMapLikeAndCollectionLikeCustomSerializers() throws Exception {
        final JsonSerializer<Object> mlSer = new ToStringSerializer();
        final JsonSerializer<Object> clSer = new ToStringSerializer();

        Serializers.Base customSer = new Serializers.Base() {
            @Override
            public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config, MapLikeType type, BeanDescription beanDesc, JsonSerializer<Object> keySerializer, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
                return mlSer;
            }
            @Override
            public JsonSerializer<?> findCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
                return clSer;
            }
        };

        BeanSerializerModifier mod = new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifyMapLikeSerializer(SerializationConfig config, MapLikeType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return serializer;
            }
            @Override
            public JsonSerializer<?> modifyCollectionLikeSerializer(SerializationConfig config, CollectionLikeType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return serializer;
            }
        };

        TestBasicSerializerFactory tbf = (TestBasicSerializerFactory) factory.withAdditionalSerializers(customSer).withSerializerModifier(mod);

        JavaType mlType = typeFactory.constructMapLikeType(CustomMapLike.class, String.class, Integer.class);
        Assert.assertSame(mlSer, tbf.buildContainerSerializer(provider, mlType, config.introspect(mlType), false));

        JavaType clType = typeFactory.constructCollectionLikeType(CustomCollectionLike.class, String.class);
        Assert.assertSame(clSer, tbf.buildContainerSerializer(provider, clType, config.introspect(clType), false));

        // When no custom serializer for MapLike / CollectionLike, returns null
        TestBasicSerializerFactory plainFactory = factory;
        Assert.assertNull(plainFactory.buildContainerSerializer(provider, mlType, config.introspect(mlType), false));
        Assert.assertNull(plainFactory.buildContainerSerializer(provider, clType, config.introspect(clType), false));
    }

    @Test
    public void testBuildEnumSerializer() throws Exception {
        JavaType enumType = typeFactory.constructType(TestEnum.class);
        JsonSerializer<?> enumSer = factory.buildEnumSerializer(config, enumType, config.introspect(enumType));
        Assert.assertTrue(enumSer instanceof EnumSerializer);

        // Enum formatted as Object
        JavaType objEnumType = typeFactory.constructType(EnumAsObject.class);
        BasicBeanDescription desc = (BasicBeanDescription) config.introspect(objEnumType);
        JsonSerializer<?> objEnumSer = factory.buildEnumSerializer(config, objEnumType, desc);
        Assert.assertNull(objEnumSer);

        // Modifier on EnumSerializer
        final JsonSerializer<Object> modSer = new ToStringSerializer();
        BeanSerializerModifier mod = new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifyEnumSerializer(SerializationConfig config, JavaType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return modSer;
            }
        };
        TestBasicSerializerFactory factoryWithMod = (TestBasicSerializerFactory) factory.withSerializerModifier(mod);
        Assert.assertSame(modSer, factoryWithMod.buildEnumSerializer(config, enumType, config.introspect(enumType)));
    }

    @Test
    public void testDeprecatedBuildIteratorAndIterableSerializers() throws Exception {
        JavaType iterType = typeFactory.constructParametricType(Iterator.class, String.class);
        Assert.assertNotNull(factory.buildIteratorSerializer(config, iterType, config.introspect(iterType), false));

        JavaType iterableType = typeFactory.constructParametricType(Iterable.class, String.class);
        Assert.assertNotNull(factory.buildIterableSerializer(config, iterableType, config.introspect(iterableType), false));
    }

    @Test
    public void testModifyTypeByAnnotation() {
        AnnotatedClass ac = config.introspectClassAnnotations(String.class).getClassInfo();
        JavaType strType = typeFactory.constructType(String.class);
        JavaType resType = factory.modifyTypeByAnnotation(config, ac, strType);
        Assert.assertSame(strType, resType);

        JavaType mapType = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        JavaType resMapType = BasicSerializerFactory.modifySecondaryTypesByAnnotation(config, ac, mapType);
        Assert.assertSame(mapType, resMapType);
    }

    @Test
    public void testUsesStaticTyping() {
        BeanDescription normalDesc = config.introspect(typeFactory.constructType(String.class));
        Assert.assertFalse(factory.usesStaticTyping(config, normalDesc, null));

        BeanDescription staticDesc = config.introspect(typeFactory.constructType(StaticTypingClass.class));
        Assert.assertTrue(factory.usesStaticTyping(config, staticDesc, null));

        BeanDescription dynamicDesc = config.introspect(typeFactory.constructType(DynamicTypingClass.class));
        Assert.assertFalse(factory.usesStaticTyping(config, dynamicDesc, null));

        // When TypeSerializer is present, must be false
        TypeSerializer dummyTs = new StdTypeResolverBuilder().buildTypeSerializer(config, typeFactory.constructType(Object.class), Collections.<NamedType>emptyList());
        Assert.assertFalse(factory.usesStaticTyping(config, staticDesc, dummyTs));
    }

    @Test
    public void testVerifyAsClass() {
        Assert.assertNull(factory._verifyAsClass(null, "test", Void.class));
        Assert.assertNull(factory._verifyAsClass(Void.class, "test", Void.class));
        Assert.assertNull(factory._verifyAsClass(NoClass.class, "test", Void.class));
        Assert.assertEquals(String.class, factory._verifyAsClass(String.class, "test", Void.class));

        try {
            factory._verifyAsClass("NotAClass", "testMethod", Void.class);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("AnnotationIntrospector.testMethod()"));
        }
    }

    @Test
    public void testFindFilterId() {
        BeanDescription beanDesc = config.introspect(typeFactory.constructType(String.class));
        Object filterId = factory.findFilterId(config, beanDesc);
        Assert.assertNull(filterId);
    }

    @Test
    public void testConvertingSerializer() throws Exception {
        JavaType convType = typeFactory.constructType(ConvertedBean.class);
        BeanDescription desc = config.introspect(convType);
        Annotated member = desc.findProperties().get(0).getAccessor();

        Converter<Object, Object> conv = factory.findConverter(provider, member);
        Assert.assertNotNull(conv);
        Assert.assertTrue(conv instanceof UpperConverter);

        JsonSerializer<?> ser = factory.findConvertingSerializer(provider, member, null);
        Assert.assertNotNull(ser);
        Assert.assertTrue(ser instanceof StdDelegatingSerializer);
    }

    @Test
    public void testFindKeyAndContentSerializer() throws Exception {
        BeanDescription beanDesc = config.introspect(typeFactory.constructType(String.class));
        AnnotatedClass ac = beanDesc.getClassInfo();
        Assert.assertNull(factory._findKeySerializer(provider, ac));
        Assert.assertNull(factory._findContentSerializer(provider, ac));
    }
}
