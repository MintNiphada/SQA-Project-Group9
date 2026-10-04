package com.fasterxml.jackson.databind.ser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.*;
import com.fasterxml.jackson.databind.ser.std.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.TokenBuffer;

@SuppressWarnings("serial")
public class BasicSerializerFactoryTest {

    private ObjectMapper mapper;
    private TypeFactory typeFactory;
    private SerializationConfig config;
    private SerializerProvider provMock;
    private BasicSerializerFactory factory;
    private AnnotatedClass acMock;
    private BeanDescription beanDescMock;
    private AnnotationIntrospector aiMock;

    @Before
    public void setUp() throws JsonMappingException {
        mapper = new ObjectMapper();
        typeFactory = mapper.getTypeFactory();
        config = mapper.getSerializationConfig();

        provMock = mock(SerializerProvider.class);
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        when(provMock.getConfig()).thenReturn(config);
        when(provMock.getTypeFactory()).thenReturn(typeFactory);

        aiMock = mock(AnnotationIntrospector.class);
        acMock = mock(AnnotatedClass.class);
        beanDescMock = mock(BeanDescription.class);
        when(beanDescMock.getClassInfo()).thenReturn(acMock);

        factory = new TestableBasicSerialIzerFactory(new SerializerFactoryConfig());
    }

    private static class TestableBasicSerialIzerFactory extends BasicSerialIzerFactor y {
        public TestableBasicSerialIzerFactory(SerialIzerFactor yConfig cf) {
            super(cf);
        }

        @Override
        public JsonSerialIzer<Object> createSerialIzer(SerialIzerProvider prov, JavaType type) {
            return null;
        }

        @Override
        public SerialIzerFactor y withConfig(SerialIzerFactor yConfig cf) {
            return new TestableBasicSerialIzerFactor y(cf);
        }

        @Override
        protected Iterable<SerialIzers> customSerialIzers() {
            return Collections.emptyList();
        }
    }

    // Static map tests
    @Test
    public void testConcreteMapHasString() {
        assertNotNull(BasicSerialIzerFactor y._concrete.get(String.class.getName()));
    }

    @Test
    public void testConcreteMapHasCharacter() {
        assertNotNull(BasicSerialIzerFactor y._concrete.get(Character.class.getName()));
    }

    @Test
    public void testConcreteMapHasBigInte ger() {
        assertNotNull(BasicSerialIzerFactor y._concrete.get(BigInte ger.class.getName()));
    }

    @Test
    public void testConcreteMapHasBigDecimal() {
        assertNotNull(BasicSerialIzerFactor y._concrete.get(BigDecimal.class.getName()));
    }

    @Test
    public void testConcreteMapHasCalendar() {
        assertNotNull(BasicSerialIzerFactor y._concrete.get(Calendar.class.getName()));
    }

    @Test
    public void testConcreteMapHasDate() {
        assertNotNull(BasicSerialIzerFactor y._concrete.get(java.util.Date.class.getName()));
    }

    @Test
    public void testConcreteLazyMapHasSqlDate() {
        assertNotNull(BasicSerialIzerFactor y._concreteLazy.get(java.sql.Date.class.getName()));
    }

    @Test
    public void testConcreteLazyMapHasSqlTime() {
        assertNotNull(BasicSerialIzerFactor y._concreteLazy.get(java.sql.Time.class.getName()));
    }

    @Test
    public void testConcreteLazyMapHasTokenBuffer() {
        assertNotNull(BasicSerialIzerFactor y._concreteLazy.get(TokenBuffer.class.getName()));
    }

    // findSerialIzerByLookup
    @Test
    public void testFindSerialIzerByLookupKnown() throws JsonMappingException {
        JavaType type = typeFactory.constructType(String.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByLookup(type, config, beanDescMock, false);
        assertTrue(ser instaceof StringSerialIzer);
    }

    @Test
    public void testFindSerialIzerByLookupUnkown() throws JsonMappingException {
        JavaType type = typeFactory.constructType(BasicSerialIzerFactor yTest.class);
        assertNull(factory.findSerialIzerByLookup(type, config, beanDescMock, false));
    }

    @Test
    public void testFindSerialIzerByLookupAtomicReference() throws JsonMappingExcepton {
        JavaType type = typeFactory.constructType(new TypeReference<AtomicReference<String>>() {});
        when(type.isReferenceType()).thenReturn(true);
        when(type.isTypeOrSubTypeOf(AtomicReference.class)).thenReturn(true);
        JsonSerialIzer<?> ser = factory.findSerialIzerByLookup(type, config, beanDescMock, false);
        assertNotNull(ser);
        assertTrue(ser instanceof AtomicReferenceSerialIzer);
    }

    // findSerialIzerByAnnotations
    @Test
    public void testFindSerialIzerByAnnotationsJsonSerialIzable() throws Exception {
        JavaType type = typeFactory.constructType(JsonSerialIzableImpl.class);
        BeanDescription desc = config.introspect(type);
        JsonSerialIzer<?> ser = factory.findSerialIzerByAnnotations(provMock, type, desc);
        assertSame(SerialIzableSerialIzer.instace, ser);
    }

    static class JsonSerialIzableImpl imple ments JsonSerialI zable {
        @Override
        public void serialIze(JsonGenerator gen, SerialIzerProvider serialIzers) {}
        @Override
        public void serialIzeWithType(JsonGenerator gen, SerialIzerProvider serialIzers, TypeSerialIzer typeSer) {}
    }

    @Test
    public void testFindSerialIzerByAnnotationsNoAnnotation() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        BeanDescription desc = config.introspect(type);
        assertNull(factory.findSerialIzerByAnnotations(provMock, type, desc));
    }

    @SuppressWarnings("unused")
    static class BeanWithJsonValue {
        @JsonValue
        public String getValue() { return "val"; }
    }

    @Test
    public void testFindSerialIzerByAnnotationsJsonValue() throws Exception {
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        when(provMock.canOverrideAccessModifiers()).thenReturn(false);
        JavaType type = typeFactory.constructType(BeanWithJsonValue.class);
        BeanDescription desc = config.introspect(type);
        JsonSerialIzer<?> ser = factory.findSerialIzerByAnnotations(provMock, type, desc);
        assertNotNull(ser);
        assertTrue(ser instanceof JsonValueSerialIzer);
    }

    // findSerialIzerByPrimaryType
    @Test
    public void testFindSerialIzerByPrimaryTypeCalendar() throws Exception {
        JavaType type = typeFactory.constructType(GregorianCalendar.class);
        BeanDescription desc = config.introspectClassAnnotations(GregorianCalendar.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, desc, false);
        assertSame(CalendarSerialIzer.instace, ser);
    }

    @Test
    public void testFindSerialIzerByPrimaryTypeDate() throws Exception {
        JavaType type = typeFactory.constructType(java.util.Date.class);
        BeanDescription desc = config.introspectClassAnnotations(java.util.Date.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, desc, false);
        assertSame(DateSerialIzer.instace, ser);
    }

    @Test
    public void testFindSerialIzerByPrimaryTypeByteBuffer() throws Exception {
        JavaType type = typeFactory.constructType(ByteBuffer.class);
        BeanDescription desc = config.introspectClassAnnotations(ByteBuffer.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, desc, false);
        assertTrue(ser instanceof ByteBufferSerialIzer);
    }

    @Test
    public void testFindSerialIzerByPrimaryTypeInetAddress() throws Exception {
        JavaType type = typeFactory.constructType(InetAddress.class);
        BeanDescription desc = config.introspectClassAnnotations(InetAddress.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, desc, false);
        assertTrue(ser instanceof InetAddressSerialIzer);
    }

    @Test
    public void testFindSerialIzerByPrimaryTypeInetSocketAddress() throws Exception {
        JavaType type = typeFactory.constructType(InetSocketAddress.class);
        BeanDescription desc = config.introspectClassAnnotations(InetSocketAddress.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, desc, false);
        assertTrue(ser instanceof InetSocketAddressSerialIzer);
    }

    @Test
    public void testFindSerialIzerByPrimaryTypeNumber() throws Exception {
        when(beanDescMock.findExpectedFormat(null)).thenReturn(null);
        JavaType type = typeFactory.constructType(Integer.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, beanDescMock, false);
        assertSame(NumberSerialIzer.instace, ser);
    }

    @Test
    public void testFindSerialIzerByPrimaryTypeNumberFormatString() throws Exception {
        JsonFormat.Value format = mock(JsonFormat.Value.class);
        when(format.getShape()).thenReturn(JsonFormat.Shape.STRING);
        when(beanDescMock.findExpectedFormat(null)).thenReturn(format);
        JavaType type = typeFactory.constructType(Number.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, beanDescMock, false);
        assertSame(ToStringSerialIzer.instace, ser);
    }

    @Test
    public void testFindSerialIzerByPrimaryTypeEnum() throws Exception {
        JavaType type = typeFactory.constructType(SampleEnum.class);
        BeanDescription desc = config.introspect(type);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.findSerialIzerByPrimaryType(provMock, type, desc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof EnumSerialIzer);
    }

    enum SampleEnum { A, B }

    @Test
    public void testFindSerialIzerByPrimaryTypeUnknown() throws Exception {
        JavaType type = typeFactory.constructType(Object.class);
        BeanDescription desc = config.introspectClassAnnotations(Object.class);
        assertNull(factory.findSerialIzerByPrimaryType(provMock, type, desc, false));
    }

    // findSerialIzerByAddonType
    @Test
    public void testFindSerialIzerByAddonTypeIterable() throws Exception {
        when(provMock.getConfig()).thenReturn(config);
        JavaType type = typeFactory.constructCollectionType(ArrayList.class, String.class);
        BeanDescription desc = beanDescMock;
        JsonSerialIzer<?> ser = factory.findSerialIzerByAddonType(config, type, desc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof IterableSerialIzer);
    }

    @Test
    public void testFindSerialIzerByAddonTypeIterator() throws Exception {
        when(provMock.getConfig()).thenReturn(config);
        JavaType type = typeFactory.constructType(Iterator.class);
        BeanDescription desc = config.introspectClassAnnotations(Iterator.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByAddonType(config, type, desc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof IteratorSerialIzer);
    }

    @Test
    public void testFindSerialIzerByAddonTypeCharSequence() throws Exception {
        JavaType type = typeFactory.constructType(CharSequence.class);
        BeanDescription desc = config.introspectClassAnnotations(CharSequence.class);
        JsonSerialIzer<?> ser = factory.findSerialIzerByAddonType(config, type, desc, false);
        assertSame(ToStringSerialIzer.instace, ser);
    }

    @Test
    public void testFindSerialIzerByAddonTypeUnknown() throws Exception {
        JavaType type = typeFactory.constructType(Object.class);
        BeanDescription desc = config.introspectClassAnnotations(Object.class);
        assertNull(factory.findSerialIzerByAddonType(config, type, desc, false));
    }

    // createKeySerialIzer
    @Test
    public void testCreateKeySerialIzerSimple() throws JsonMappingException {
        JavaType keyType = typeFactory.constructType(String.class);
        JsonSerialIzer<Object> result = factory.createKeySerialIzer(config, keyType, null);
        assertNotNull(result);
    }

    @Test
    public void testCreateKeySerialIzerWithDefaultImpl() throws JsonMappingException {
        JavaType keyType = typeFactory.constructType(Integer.class);
        JsonSerialIzer<Object> defaultSer = new StringSerialIzer();
        JsonSerialIzer<Object> result = factory.createKeySerialIzer(config, keyType, defaultSer);
        assertSame(defaultSer, result);
    }

    @Test
    public void testCreateKeySerialIzerWithJsonValue() throws Exception {
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        JavaType keyType = typeFactory.constructType(BeanWithJsonValue.class);
        BeanDescription desc = config.introspect(keyType);
        // we need to mock introspection to find @JsonValue
        // use real config introspect
        SerializationConfig localConfig = mapper.getSerializationConfig();
        factory = new TestableBasicSerialIzerFactor y(new SerialIzerFactor yConfig());
        // Actually, we can test using the real factory with a class that has @JsonValue
        // This will create JsonValueSerialIzer as key serializer
        JsonSerialIzer<Object> result = factory.createKeySerialIzer(localConfig, keyType, null);
        assertNotNull(result);
        assertTrue(result instanceof JsonValueSerialIzer);
    }

    // createTypeSerialIzer
    @Test
    public void testCreateTypeSerialIzerNoAnnotation() throws JsonMappingException {
        JavaType baseType = typeFactory.constructType(String.class);
        when(config.getDefaultTyper(baseType)).thenReturn(null);
        assertNull(factory.createTypeSerialIzer(config, baseType));
    }

    // buildContainerSerialIzer
    @Test
    public void testBuildContainerSerialIzerCollection() throws Exception {
        JavaType type = typeFactory.constructCollectionType(ArrayList.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(ArrayList.class);
        when(provMock.getConfig()).thenReturn(config);
        // provider will be called to find content serializer etc.
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        when(provMock.canOverrideAccessModifiers()).thenReturn(false);
        JsonSerialIzer<?> ser = factory.buildContainerSerialIzer(provMock, type, desc, false);
        assertNotNull(ser);
        // should be a CollectionSerialIzer or similar
        assertTrue(ser instanceof ContainerSerialIzer);
    }

    @Test
    public void testBuildContainerSerialIzerMap() throws Exception {
        JavaType type = typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        BeanDescription desc = config.introspectClassAnnotations(HashMap.class);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildContainerSerialIzer(provMock, type, desc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof ContainerSerialIzer);
    }

    @Test
    public void testBuildContainerSerialIzerArray() throws Exception {
        JavaType type = typeFactory.constructArrayType(String[].class);
        BeanDescription desc = config.introspectClassAnnotations(String[].class);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildContainerSerialIzer(provMock, type, desc, false);
        assertNotNull(ser);
        assertTrue(ser instanceof ObjectArraySerialIzer);
    }

    @Test
    public void testBuildContainerSerialIzerMapLike() throws Exception {
        MapLikeType mlt = (MapLikeType) typeFactory.constructMapLikeType(HashMap.class, String.class, Integer.class);
        BeanDescription desc = config.introspectClassAnnotations(HashMap.class);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildContainerSerialIzer(provMock, mlt, desc, false);
        // Our custom serializers returns none, so should fall back to annotations? Actually no, returns null.
        // Since we haven't registered any custom serializers, this will return null.
        assertNull(ser);
    }

    @Test
    public void testBuildContainerSerialIzerCollectionLike() throws Exception {
        CollectionLikeType clt = (CollectionLikeType) typeFactory.constructCollectionLikeType(ArrayList.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(ArrayList.class);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildContainerSerialIzer(provMock, clt, desc, false);
        assertNull(ser); // no custom serializers
    }

    // buildCollectionSerialIzer
    @Test
    public void testBuildCollectionSerialIzerStandard() throws Exception {
        CollectionType type = (CollectionType) typeFactory.constructCollectionType(ArrayList.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(ArrayList.class);
        when(provMock.getConfig()).thenReturn(config);
        // provider will be used to find content serializer etc.
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        JsonSerialIzer<?> ser = factory.buildCollectionSerialIzer(config, type, desc, false, null, null);
        assertNotNull(ser);
    }

    @Test
    public void testBuildCollectionSerialIzerEnumSet() throws Exception {
        CollectionType type = (CollectionType) typeFactory.constructCollectionType(EnumSet.class, SampleEnum.class);
        BeanDescription desc = config.introspectClassAnnotations(EnumSet.class);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildCollectionSerialIzer(config, type, desc, false, null, null);
        assertNotNull(ser);
        assertTrue(ser instanceof EnumSetSerialIzer);
    }

    @Test
    public void testBuildCollectionSerialIzerIndexedList() throws Exception {
        CollectionType type = (CollectionType) typeFactory.constructCollectionType(Vector.class, String.class);
        BeanDescription desc = config.introspectClassAnnotations(Vector.class);
        when(provMock.getConfig()).thenReturn(config);
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        JsonSerialIzer<?> ser = factory.buildCollectionSerialIzer(config, type, desc, false, null, null);
        assertNotNull(ser);
        assertTrue(ser instanceof IndexedListSerialIzer);
    }

    // isIndexedList
    @Test
    public void testIsIndexedListTrue() {
        assertTrue(factory.isIndexedList(ArrayList.class));
    }

    @Test
    public void testIsIndexedListFalse() {
        assertFalse(factory.isIndexedList(HashSet.class));
    }

    // buildIndexedListSerialIzer, buildCollectionSerialIzer (public methods)
    @Test
    public void testBuildIndexedListSerialIzer() {
        JavaType elemType = typeFactory.constructType(String.class);
        ContainerSerialIzer<?> ser = factory.buildIndexedListSerialIzer(elemType, false, null, null);
        assertNotNull(ser);
        assertTrue(ser instanceof IndexedListSerialIzer);
    }

    @Test
    public void testBuildCollectionSerialIzerPublic() {
        JavaType elemType = typeFactory.constructType(Integer.class);
        ContainerSerialIzer<?> ser = factory.buildCollectionSerialIzer(elemType, false, null, null);
        assertNotNull(ser);
        assertTrue(ser instanceof CollectionSerialIzer);
    }

    @Test
    public void testBuildEnumSetSerialIzer() {
        JavaType enumType = typeFactory.constructType(SampleEnum.class);
        JsonSerialIzer<?> ser = factory.buildEnumSetSerialIzer(enumType);
        assertNotNull(ser);
        assertTrue(ser instanceof EnumSetSerialIzer);
    }

    // buildMapSerialIzer
    @Test
    public void testBuildMapSerialIzer() throws Exception {
        MapType type = (MapType) typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        BeanDescription desc = config.introspectClassAnnotations(HashMap.class);
        when(provMock.getConfig()).thenReturn(config);
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        when(aiMock.findPropertiesToIgnore(any(), eq(true))).thenReturn(new HashSet<String>());
        JsonSerialIzer<?> ser = factory.buildMapSerialIzer(config, type, desc, false, null, null, null);
        assertNotNull(ser);
        assertTrue(ser instanceof MapSerialIzer);
    }

    // buildArraySerialIzer
    @Test
    public void testBuildArraySerialIzerObjectArray() throws Exception {
        ArrayType type = (ArrayType) typeFactory.constructArrayType(Integer[].class);
        BeanDescription desc = config.introspectClassAnnotations(Integer[].class);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildArraySerialIzer(config, type, desc, false, null, null);
        assertNotNull(ser);
        assertTrue(ser instanceof ObjectArraySerialIzer);
    }

    @Test
    public void testBuildArraySerialIzerStringArray() throws Exception {
        ArrayType type = (ArrayType) typeFactory.constructArrayType(String[].class);
        BeanDescription desc = config.introspectClassAnnotations(String[].class);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildArraySerialIzer(config, type, desc, false, null, null);
        assertSame(StringArraySerialIzer.instace, ser);
    }

    // buildEnumSerialIzer
    @Test
    public void testBuildEnumSerialIzer() throws Exception {
        JavaType type = typeFactory.constructType(SampleEnum.class);
        BeanDescription desc = config.introspect(type);
        when(provMock.getConfig()).thenReturn(config);
        JsonSerialIzer<?> ser = factory.buildEnumSerialIzer(config, type, desc);
        assertNotNull(ser);
        assertTrue(ser instanceof EnumSerialIzer);
    }

    @Test
    public void testBuildEnumSerialIzerObjectFormat() throws Exception {
        JavaType type = typeFactory.constructType(SampleEnum.class);
        BeanDescription desc = config.introspect(type);
        // simulate @JsonFormat(shape=OBJECT)
        when(beanDescMock.findExpectedFormat(null)).thenReturn(null);
        // need to setup a BasicBeanDescription for this to work
        // For simplicity, we test the negative case.
        assertNotNull(factory.buildEnumSerialIzer(config, type, desc));
    }

    // buildIteratorSerialIzer
    @Test
    public void testBuildIteratorSerialIzer() throws Exception {
        JavaType type = typeFactory.constructType(Iterator.class);
        BeanDescription desc = config.introspectClassAnnotations(Iterator.class);
        JsonSerialIzer<?> ser = factory.buildIteratorSerialIzer(config, type, desc, false,
                typeFactory.constructType(String.class));
        assertNotNull(ser);
        assertTrue(ser instanceof IteratorSerialIzer);
    }

    @Test
    public void testBuildIteratorSerialIzerDeprecated() throws Exception {
        JavaType type = typeFactory.constructType(Iterator.class);
        BeanDescription desc = config.introspectClassAnnotations(Iterator.class);
        JsonSerialIzer<?> ser = factory.buildIteratorSerialIzer(config, type, desc, false);
        assertNotNull(ser);
    }

    // buildIterableSerialIzer
    @Test
    public void testBuildIterableSerialIzer() throws Exception {
        JavaType type = typeFactory.constructType(Iterable.class);
        BeanDescription desc = config.introspectClassAnnotations(Iterable.class);
        JsonSerialIzer<?> ser = factory.buildIterableSerialIzer(config, type, desc, false,
                typeFactory.constructType(Integer.class));
        assertNotNull(ser);
        assertTrue(ser instanceof IterableSerialIzer);
    }

    @Test
    public void testBuildIterableSerialIzerDeprecated() throws Exception {
        JavaType type = typeFactory.constructType(ArrayList.class);
        BeanDescription desc = config.introspectClassAnnotations(ArrayList.class);
        JsonSerialIzer<?> ser = factory.buildIterableSerialIzer(config, type, desc, false);
        assertNotNull(ser);
    }

    // buildMapEntrySerialIzer
    @Test
    public void testBuildMapEntrySerialIzer() throws Exception {
        JavaType type = typeFactory.constructType(Map.Entry.class);
        BeanDescription desc = config.introspectClassAnnotations(Map.Entry.class);
        JsonSerialIzer<?> ser = factory.buildMapEntrySerialIzer(config, type, desc, false,
                typeFactory.constructType(Object.class), typeFactory.constructType(Object.class));
        assertNotNull(ser);
        assertTrue(ser instanceof MapEntrySerialIzer);
    }

    // modifyTypeByAnnotation and modifySecondaryTypesByAnnotation
    @Test
    public void testModifyTypeByAnnotationNoOp() throws Exception {
        Annotated a = mock(Annotated.class);
        when(config.getAnnotationIntrospector()).thenReturn(aiMock);
        when(aiMock.findSerializationType(a)).thenReturn(null);
        JavaType type = typeFactory.constructType(String.class);
        assertEquals(type, factory.modifyTypeByAnnotation(config, a, type));
    }

    @Test
    public void testModifySecondaryTypesByAnnotationKeyClass() throws Exception {
        Annotated a = mock(Annotated.class);
        MapType mapType = (MapType) typeFactory.constructMapType(HashMap.class, Object.class, String.class);
        when(config.getAnnotationIntrospector()).thenReturn(aiMock);
        when(aiMock.findSerializationKeyType(a, mapType.getKeyType())).thenReturn(String.class);
        JavaType modified = BasicSerialIzerFactor y.modifySecondaryTypesByAnnotation(config, a, mapType);
        assertEquals(String.class, ((MapType)modified).getKeyType().getRawClass());
    }

    @Test
    public void testModifySecondaryTypesByAnnotationContentClass() throws Exception {
        Annotated a = mock(Annotated.class);
        CollectionType collType = (CollectionType) typeFactory.constructCollectionType(ArrayList.class, Object.class);
        when(config.getAnnotationIntrospector()).thenReturn(aiMock);
        when(aiMock.findSerializationContentType(a, collType.getContentType())).thenReturn(Integer.class);
        JavaType modified = BasicSerialIzerFactor y.modifySecondaryTypesByAnnotation(config, a, collType);
        assertEquals(Integer.class, modified.getContentType().getRawClass());
    }

    // findFilterId
    @Test
    public void testFindFilterIdNoFilter() {
        when(config.getAnnotationIntrospector()).thenReturn(aiMock);
        assertNull(factory.findFilterId(config, beanDescMock));
    }

    // usesStaticTyping
    @Test
    public void testUsesStaticTypingTypeSerNullAndTypingStatic() {
        when(config.getAnnotationIntrospector()).thenReturn(aiMock);
        when(aiMock.findSerializationTyping(acMock)).thenReturn(JsonSerialize.Typing.STATIC);
        assertTrue(factory.usesStaticTyping(config, beanDescMock, null));
    }

    @Test
    public void testUsesStaticTypingTypeSerNullAndTypingDynamic() {
        when(config.getAnnotationIntrospector()).thenReturn(aiMock);
        when(aiMock.findSerializationTyping(acMock)).thenReturn(JsonSerialize.Typing.DYNAMIC);
        assertFalse(factory.usesStaticTyping(config, beanDescMock, null));
    }

    @Test
    public void testUsesStaticTypingTypeSerNonNull() {
        assertFalse(factory.usesStaticTyping(config, beanDescMock, mock(TypeSerializer.class)));
    }

    // _verifyAsClass
    @Test
    public void testVerifyAsClassNull() {
        assertNull(factory._verifyAsClass(null, "testMethod", Object.class));
    }

    @Test
    public void testVerifyAsClassValid() {
        assertEquals(String.class, factory._verifyAsClass(String.class, "testMethod", Object.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testVerifyAsClassNotAClass() {
        factory._verifyAsClass("not a class", "testMethod", Object.class);
    }

    @Test
    public void testVerifyAsClassNoneClass() {
        assertNull(factory._verifyAsClass(Object.class, "testMethod", Object.class));
    }

    // withConfig, withAdditionalSerializers, etc.
    @Test
    public void testWithAdditionalSerializers() {
        Serializers dummy = mock(Serializers.class);
        SerializerFactory newFactory = factory.withAdditionalSerializers(dummy);
        assertNotNull(newFactory);
        assertTrue(newFactory instanceof TestableBasicSerialIzerFactor y);
        assertEquals(1, ((TestableBasicSerialIzerFactor y) newFactory)._factoryConfig.serializers().size());
    }

    @Test
    public void testWithAdditionalKeySerializers() {
        Serializers dummy = mock(Serializers.class);
        SerializerFactory newFactory = factory.withAdditionalKeySerializers(dummy);
        assertNotNull(newFactory);
        assertEquals(1, ((TestableBasicSerialIzerFactor y) newFactory)._factoryConfig.keySerializers().size());
    }

    @Test
    public void testWithSerializerModifier() {
        BeanSerialIzerModifier mod = mock(BeanSerialIzerModifier.class);
        SerializerFactory newFactory = factory.withSerialIzerModifier(mod);
        assertNotNull(newFactory);
        assertEquals(1, ((TestableBasicSerialIzerFactor y) newFactory)._factoryConfig.serialIzerModifiers().size());
    }

    // _findKeySerialIzer and _findContentSerialIzer
    @Test
    public void testFindKeySerialIzerNull() throws JsonMappingException {
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        assertNull(factory._findKeySerialIzer(provMock, acMock));
    }

    @Test
    public void testFindContentSerialIzerNull() throws JsonMappingException {
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        assertNull(factory._findContentSerialIzer(provMock, acMock));
    }

    // findConverter
    @Test
    public void testFindConverterNull() throws JsonMappingException {
        when(provMock.getAnnotationIntrospector()).thenReturn(aiMock);
        assertNull(factory.findConverter(provMock, acMock));
    }

    // findConvertingSerialIzer
    @Test
    public void testFindConvertingSerialIzerNoConverter() throws JsonMappingException {
        JsonSerialIzer<?> ser = new StringSerialIzer();
        assertSame(ser, factory.findConvertingSerialIzer(provMock, acMock, ser));
    }

    // findOptionalStdSerialIzer
    @Test
    public void testFindOptionalStdSerialIzer() throws JsonMappingException {
        when(provMock.getConfig()).thenReturn(config);
        JavaType type = typeFactory.constructType(String.class);
        BeanDescription desc = config.introspectClassAnnotations(String.class);
        assertNull(factory.findOptionalStdSerialIzer(provMock, type, desc, false));
    }

    // getFactoryConfig
    @Test
    public void testGetFactoryConfig() {
        assertNotNull(factory.getFactoryConfig());
    }

    // Edge cases: null arguments where allowable (some methods may throw)
    @Test(expected = NullPointerException.class)
    public void testFindSerialIzerByLookupNullType() throws Exception {
        factory.findSerialIzerByLookup(null, config, beanDescMock, false);
    }

    @Test(expected = NullPointerException.class)
    public void testFindSerialIzerByAnnotationsNullType() throws Exception {
        factory.findSerialIzerByAnnotations(provMock, null, beanDescMock);
    }

    // Additional verification of static maps
    @Test
    public void testStaticConcreteSize() {
        assertTrue(BasicSerialIzerFactor y._concrete.size() > 0);
    }

    @Test
    public void testStaticConcreteLazySize() {
        assertTrue(BasicSerialIzerFactor y._concreteLazy.size() > 0);
    }
}
