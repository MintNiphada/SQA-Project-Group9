package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectMapperTest {

    public static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() {}

        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class CloseableBean implements Closeable {
        public int a = 1;
        public boolean closed = false;

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    public static class MixInTarget {
        public int hidden;
        public int visible;
    }

    public abstract static class MixInSource {
        @JsonProperty("v")
        public int visible;
    }

    public static class SubClassMapper extends ObjectMapper {
        public SubClassMapper() {
            super();
        }
    }

    @Test
    public void testConstructorsAndCopy() {
        ObjectMapper mapper = new ObjectMapper();
        Assert.assertNotNull(mapper.getFactory());
        Assert.assertNotNull(mapper.getJsonFactory());
        Assert.assertNotNull(mapper.getSerializationConfig());
        Assert.assertNotNull(mapper.getDeserializationConfig());
        Assert.assertNotNull(mapper.getDeserializationContext());
        Assert.assertNotNull(mapper.getSerializerProvider());
        Assert.assertNotNull(mapper.getSerializerProviderInstance());
        Assert.assertNotNull(mapper.getSerializerFactory());
        Assert.assertNotNull(mapper.getTypeFactory());
        Assert.assertNotNull(mapper.getNodeFactory());
        Assert.assertNotNull(mapper.version());

        ObjectMapper copy = mapper.copy();
        Assert.assertNotNull(copy);
        Assert.assertNotSame(mapper, copy);
        Assert.assertNotSame(mapper.getFactory(), copy.getFactory());
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidCopyInSubclass() {
        SubClassMapper sub = new SubClassMapper();
        sub.copy();
    }

    @Test
    public void testConfigurationChaining() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, true)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .disable(MapperFeature.AUTO_DETECT_FIELDS);
        Assert.assertTrue(mapper.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        Assert.assertFalse(mapper.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
                .enable(SerializationFeature.WRAP_ROOT_VALUE, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Assert.assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        Assert.assertTrue(mapper.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
        Assert.assertFalse(mapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));

        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.UNWRAP_ROOT_VALUE)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        Assert.assertTrue(mapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        Assert.assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES));

        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                .disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        Assert.assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        Assert.assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES));
        Assert.assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_SINGLE_QUOTES));

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true)
                .enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS)
                .disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        Assert.assertTrue(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        Assert.assertTrue(mapper.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        Assert.assertFalse(mapper.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
        Assert.assertTrue(mapper.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
    }

    @Test
    public void testSettingsAndOverriding() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        mapper.setDateFormat(df);
        Assert.assertNotNull(mapper.getDateFormat());

        Locale loc = Locale.GERMANY;
        mapper.setLocale(loc);
        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        mapper.setTimeZone(tz);
        mapper.setBase64Variant(Base64Variants.MIME);

        InjectableValues inject = new InjectableValues.Std();
        mapper.setInjectableValues(inject);
        Assert.assertSame(inject, mapper.getInjectableValues());

        PropertyNamingStrategy pns = PropertyNamingStrategy.SNAKE_CASE;
        mapper.setPropertyNamingStrategy(pns);
        Assert.assertSame(pns, mapper.getPropertyNamingStrategy());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.ALWAYS));

        VisibilityChecker<?> vc = mapper.getVisibilityChecker();
        mapper.setVisibility(vc);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        SubtypeResolver str = new StdSubtypeResolver();
        mapper.setSubtypeResolver(str);
        Assert.assertSame(str, mapper.getSubtypeResolver());

        mapper.setNodeFactory(JsonNodeFactory.instance);
        Assert.assertSame(JsonNodeFactory.instance, mapper.getNodeFactory());

        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        mapper.addHandler(handler);
        mapper.clearProblemHandlers();

        FilterProvider fp = new SimpleFilterProvider();
        mapper.setFilterProvider(fp);
        mapper.setFilters(fp);

        mapper.setSerializerFactory(mapper.getSerializerFactory());
        mapper.setSerializerProvider(new com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.Impl());
        mapper.setTypeFactory(TypeFactory.defaultInstance());
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        mapper.setAnnotationIntrospectors(new JacksonAnnotationIntrospector(), new JacksonAnnotationIntrospector());
        mapper.setConfig(mapper.getSerializationConfig());
        mapper.setConfig(mapper.getDeserializationConfig());
        mapper.setHandlerInstantiator((HandlerInstantiator) null);
        mapper.configOverride(SimpleBean.class);
    }

    @Test
    public void testMixIns() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(MixInTarget.class, MixInSource.class);
        Assert.assertEquals(1, mapper.mixInCount());
        Assert.assertEquals(MixInSource.class, mapper.findMixInClassFor(MixInTarget.class));

        Map<Class<?>, Class<?>> sourceMap = new HashMap<Class<?>, Class<?>>();
        sourceMap.put(SimpleBean.class, MixInSource.class);
        mapper.setMixIns(sourceMap);
        Assert.assertEquals(1, mapper.mixInCount());
        Assert.assertEquals(MixInSource.class, mapper.findMixInClassFor(SimpleBean.class));

        mapper.setMixInAnnotations(sourceMap);
        mapper.addMixInAnnotations(MixInTarget.class, MixInSource.class);
        mapper.setMixInResolver(new ClassIntrospector.MixInResolver() {
            @Override
            public Class<?> findMixInClassFor(Class<?> cls) {
                return null;
            }
            @Override
            public ClassIntrospector.MixInResolver copy() {
                return this;
            }
        });
    }

    @Test
    public void testModuleRegistration() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("TestModule", com.fasterxml.jackson.core.Version.unknownVersion());
        final AtomicBoolean setupCalled = new AtomicBoolean(false);
        module.setMixInAnnotation(MixInTarget.class, MixInSource.class);
        mapper.registerModule(new Module() {
            @Override
            public String getModuleName() { return "Custom"; }
            @Override
            public Version version() { return Version.unknownVersion(); }
            @Override
            public void setupModule(SetupContext context) {
                setupCalled.set(true);
                Assert.assertNotNull(context.getMapperVersion());
                Assert.assertNotNull(context.getOwner());
                Assert.assertNotNull(context.getTypeFactory());
                Assert.assertTrue(context.isEnabled(MapperFeature.USE_ANNOTATIONS));
                Assert.assertFalse(context.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
                Assert.assertFalse(context.isEnabled(SerializationFeature.INDENT_OUTPUT));
                Assert.assertFalse(context.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
                Assert.assertFalse(context.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
                Assert.assertFalse(context.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
                Assert.assertNotNull(context.configOverride(SimpleBean.class));
                context.addDeserializers(new Deserializers.Base());
                context.addKeyDeserializers(new KeyDeserializers() {
                    @Override
                    public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
                        return null;
                    }
                });
                context.addBeanDeserializerModifier(new BeanDeserializerModifier());
                context.addSerializers(new Serializers.Base());
                context.addKeySerializers(new Serializers.Base());
                context.addBeanSerializerModifier(new BeanSerializerModifier());
                context.addAbstractTypeResolver(new com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver());
                context.addTypeModifier(new TypeModifier() {
                    @Override
                    public JavaType modifyType(JavaType type, java.lang.reflect.Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                        return type;
                    }
                });
                context.addValueInstantiators(new ValueInstantiators.Base());
                context.setClassIntrospector(new com.fasterxml.jackson.databind.introspect.BasicClassIntrospector());
                context.insertAnnotationIntrospector(new JacksonAnnotationIntrospector());
                context.appendAnnotationIntrospector(new JacksonAnnotationIntrospector());
                context.registerSubtypes(SimpleBean.class);
                context.registerSubtypes(new NamedType(SimpleBean.class, "simple"));
                context.setMixInAnnotations(MixInTarget.class, MixInSource.class);
                context.addDeserializationProblemHandler(new DeserializationProblemHandler() {});
                context.setNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
            }
        });
        Assert.assertTrue(setupCalled.get());

        mapper.registerModules(module);
        mapper.registerModules(Collections.singletonList(module));
        List<Module> mods = ObjectMapper.findModules();
        Assert.assertNotNull(mods);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModuleWithoutName() {
        new ObjectMapper().registerModule(new Module() {
            @Override
            public String getModuleName() { return null; }
            @Override
            public Version version() { return Version.unknownVersion(); }
            @Override
            public void setupModule(SetupContext context) {}
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testModuleWithoutVersion() {
        new ObjectMapper().registerModule(new Module() {
            @Override
            public String getModuleName() { return "Test"; }
            @Override
            public Version version() { return null; }
            @Override
            public void setupModule(SetupContext context) {}
        });
    }

    @Test
    public void testDefaultTyping() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "@type");
        mapper.disableDefaultTyping();

        ObjectMapper.DefaultTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        Assert.assertTrue(builder.useForType(mapper.constructType(SimpleBean.class)));
        Assert.assertFalse(builder.useForType(mapper.constructType(TreeNode.class)));
        Assert.assertFalse(builder.useForType(mapper.constructType(String.class)));

        ObjectMapper.DefaultTypeResolverBuilder arraysBuilder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        Assert.assertTrue(arraysBuilder.useForType(mapper.constructType(CharSequence[].class)));

        ObjectMapper.DefaultTypeResolverBuilder objBuilder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        Assert.assertTrue(objBuilder.useForType(mapper.constructType(Object.class)));
        Assert.assertTrue(objBuilder.useForType(mapper.constructType(CharSequence.class)));
        Assert.assertFalse(objBuilder.useForType(mapper.constructType(SimpleBean.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultTypingInvalidInclusion() {
        new ObjectMapper().enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.EXTERNAL_PROPERTY);
    }

    @Test
    public void testSerializationAndDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean(42, "hello");

        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("42"));
        Assert.assertTrue(json.contains("hello"));

        byte[] bytes = mapper.writeValueAsBytes(bean);
        Assert.assertTrue(bytes.length > 0);

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, bean);
        Assert.assertEquals(json, sw.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, bean);
        Assert.assertEquals(json, baos.toString("UTF-8"));

        ByteArrayOutputStream dataBaos = new ByteArrayOutputStream();
        DataOutput dataOut = new DataOutputStream(dataBaos);
        mapper.writeValue(dataOut, bean);
        Assert.assertEquals(json, dataBaos.toString("UTF-8"));

        File tmpFile = File.createTempFile("jackson-test", ".json");
        tmpFile.deleteOnExit();
        mapper.writeValue(tmpFile, bean);
        Assert.assertTrue(tmpFile.length() > 0);

        SimpleBean fromString = mapper.readValue(json, SimpleBean.class);
        Assert.assertEquals(42, fromString.x);
        Assert.assertEquals("hello", fromString.y);

        SimpleBean fromTypeRef = mapper.readValue(json, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(42, fromTypeRef.x);

        SimpleBean fromJavaType = mapper.readValue(json, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromJavaType.x);

        SimpleBean fromBytes = mapper.readValue(bytes, SimpleBean.class);
        Assert.assertEquals(42, fromBytes.x);
        SimpleBean fromBytesRef = mapper.readValue(bytes, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(42, fromBytesRef.x);
        SimpleBean fromBytesJT = mapper.readValue(bytes, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromBytesJT.x);

        SimpleBean fromBytesOff = mapper.readValue(bytes, 0, bytes.length, SimpleBean.class);
        Assert.assertEquals(42, fromBytesOff.x);
        SimpleBean fromBytesOffRef = mapper.readValue(bytes, 0, bytes.length, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(42, fromBytesOffRef.x);
        SimpleBean fromBytesOffJT = mapper.readValue(bytes, 0, bytes.length, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromBytesOffJT.x);

        SimpleBean fromReader = mapper.readValue(new StringReader(json), SimpleBean.class);
        Assert.assertEquals(42, fromReader.x);
        SimpleBean fromReaderRef = mapper.readValue(new StringReader(json), new TypeReference<SimpleBean>() {});
        Assert.assertEquals(42, fromReaderRef.x);
        SimpleBean fromReaderJT = mapper.readValue(new StringReader(json), mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromReaderJT.x);

        SimpleBean fromStream = mapper.readValue(new ByteArrayInputStream(bytes), SimpleBean.class);
        Assert.assertEquals(42, fromStream.x);
        SimpleBean fromStreamRef = mapper.readValue(new ByteArrayInputStream(bytes), new TypeReference<SimpleBean>() {});
        Assert.assertEquals(42, fromStreamRef.x);
        SimpleBean fromStreamJT = mapper.readValue(new ByteArrayInputStream(bytes), mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromStreamJT.x);

        SimpleBean fromFile = mapper.readValue(tmpFile, SimpleBean.class);
        Assert.assertEquals(42, fromFile.x);
        SimpleBean fromFileRef = mapper.readValue(tmpFile, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(42, fromFileRef.x);
        SimpleBean fromFileJT = mapper.readValue(tmpFile, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromFileJT.x);

        DataInput dataIn = new DataInputStream(new ByteArrayInputStream(bytes));
        SimpleBean fromDataInput = mapper.readValue(dataIn, SimpleBean.class);
        Assert.assertEquals(42, fromDataInput.x);

        DataInput dataIn2 = new DataInputStream(new ByteArrayInputStream(bytes));
        SimpleBean fromDataInputJT = mapper.readValue(dataIn2, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromDataInputJT.x);

        URL url = tmpFile.toURI().toURL();
        SimpleBean fromUrl = mapper.readValue(url, SimpleBean.class);
        Assert.assertEquals(42, fromUrl.x);
        SimpleBean fromUrlRef = mapper.readValue(url, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(42, fromUrlRef.x);
        SimpleBean fromUrlJT = mapper.readValue(url, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(42, fromUrlJT.x);
    }

    @Test
    public void testTreeAndConversions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode objNode = mapper.createObjectNode();
        objNode.put("x", 7);
        objNode.put("y", "test");
        ArrayNode arrNode = mapper.createArrayNode();
        arrNode.add(1).add(2);

        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(g, (TreeNode) objNode);
        Assert.assertTrue(sw.toString().contains("test"));

        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = mapper.getFactory().createGenerator(sw2);
        mapper.writeTree(g2, (JsonNode) objNode);
        Assert.assertTrue(sw2.toString().contains("test"));

        JsonNode fromStr = mapper.readTree("{\"x\":7,\"y\":\"test\"}");
        Assert.assertEquals(7, fromStr.get("x").asInt());

        JsonNode fromBytes = mapper.readTree("{\"x\":7}".getBytes("UTF-8"));
        Assert.assertEquals(7, fromBytes.get("x").asInt());

        JsonNode fromStream = mapper.readTree(new ByteArrayInputStream("{\"x\":7}".getBytes("UTF-8")));
        Assert.assertEquals(7, fromStream.get("x").asInt());

        JsonNode fromReader = mapper.readTree(new StringReader("{\"x\":7}"));
        Assert.assertEquals(7, fromReader.get("x").asInt());

        File tmp = File.createTempFile("jackson-node", ".json");
        tmp.deleteOnExit();
        mapper.writeValue(tmp, objNode);
        JsonNode fromFile = mapper.readTree(tmp);
        Assert.assertEquals(7, fromFile.get("x").asInt());
        JsonNode fromUrl = mapper.readTree(tmp.toURI().toURL());
        Assert.assertEquals(7, fromUrl.get("x").asInt());

        JsonParser parser = mapper.treeAsTokens(objNode);
        JsonNode fromParser = mapper.readTree(parser);
        Assert.assertEquals(7, fromParser.get("x").asInt());

        SimpleBean bean = mapper.treeToValue(objNode, SimpleBean.class);
        Assert.assertEquals(7, bean.x);

        POJONode pojoNode = new POJONode(bean);
        SimpleBean fromPojo = mapper.treeToValue(pojoNode, SimpleBean.class);
        Assert.assertSame(bean, fromPojo);

        ObjectNode fromNodeCast = mapper.treeToValue(objNode, ObjectNode.class);
        Assert.assertSame(objNode, fromNodeCast);

        JsonNode convertedNode = mapper.valueToTree(bean);
        Assert.assertEquals(7, convertedNode.get("x").asInt());
        Assert.assertNull(mapper.valueToTree(null));

        SimpleBean convertedBean = mapper.convertValue(objNode, SimpleBean.class);
        Assert.assertEquals(7, convertedBean.x);
        SimpleBean convertedRef = mapper.convertValue(objNode, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(7, convertedRef.x);
        SimpleBean convertedJT = mapper.convertValue(objNode, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(7, convertedJT.x);

        SimpleBean sameBean = mapper.convertValue(bean, SimpleBean.class);
        Assert.assertSame(bean, sameBean);
        Assert.assertNull(mapper.convertValue(null, SimpleBean.class));
    }

    @Test
    public void testReadValuesMappingIterator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonSeq = "{\"x\":1,\"y\":\"a\"}\n{\"x\":2,\"y\":\"b\"}";

        JsonParser p1 = mapper.getFactory().createParser(jsonSeq);
        MappingIterator<SimpleBean> it1 = mapper.readValues(p1, SimpleBean.class);
        Assert.assertTrue(it1.hasNext());
        Assert.assertEquals(1, it1.next().x);
        Assert.assertEquals(2, it1.next().x);

        JsonParser p2 = mapper.getFactory().createParser(jsonSeq);
        MappingIterator<SimpleBean> it2 = mapper.readValues(p2, new TypeReference<SimpleBean>() {});
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(1, it2.next().x);

        JsonParser p3 = mapper.getFactory().createParser(jsonSeq);
        MappingIterator<SimpleBean> it3 = mapper.readValues(p3, mapper.constructType(SimpleBean.class));
        Assert.assertTrue(it3.hasNext());
        Assert.assertEquals(1, it3.next().x);
    }

    @Test
    public void testWritersAndReaders() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Assert.assertNotNull(mapper.writer());
        Assert.assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT));
        Assert.assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRAP_ROOT_VALUE));
        Assert.assertNotNull(mapper.writer((DateFormat) null));
        Assert.assertNotNull(mapper.writerWithView(Object.class));
        Assert.assertNotNull(mapper.writerFor(SimpleBean.class));
        Assert.assertNotNull(mapper.writerFor(new TypeReference<SimpleBean>() {}));
        Assert.assertNotNull(mapper.writerFor(mapper.constructType(SimpleBean.class)));
        Assert.assertNotNull(mapper.writer((PrettyPrinter) null));
        Assert.assertNotNull(mapper.writerWithDefaultPrettyPrinter());
        Assert.assertNotNull(mapper.writer(new SimpleFilterProvider()));
        Assert.assertNotNull(mapper.writer(Base64Variants.MIME));
        Assert.assertNotNull(mapper.writer((CharacterEscapes) null));
        Assert.assertNotNull(mapper.writer(ContextAttributes.getEmpty()));
        Assert.assertNotNull(mapper.writerWithType(SimpleBean.class));
        Assert.assertNotNull(mapper.writerWithType(new TypeReference<SimpleBean>() {}));
        Assert.assertNotNull(mapper.writerWithType(mapper.constructType(SimpleBean.class)));

        Assert.assertNotNull(mapper.reader());
        Assert.assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        Assert.assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertNotNull(mapper.readerFor(SimpleBean.class));
        Assert.assertNotNull(mapper.readerFor(new TypeReference<SimpleBean>() {}));
        Assert.assertNotNull(mapper.readerFor(mapper.constructType(SimpleBean.class)));
        Assert.assertNotNull(mapper.readerForUpdating(new SimpleBean()));
        Assert.assertNotNull(mapper.reader(JsonNodeFactory.instance));
        Assert.assertNotNull(mapper.reader(new InjectableValues.Std()));
        Assert.assertNotNull(mapper.readerWithView(Object.class));
        Assert.assertNotNull(mapper.reader(Base64Variants.MIME));
        Assert.assertNotNull(mapper.reader(ContextAttributes.getEmpty()));
        Assert.assertNotNull(mapper.reader(SimpleBean.class));
        Assert.assertNotNull(mapper.reader(new TypeReference<SimpleBean>() {}));
        Assert.assertNotNull(mapper.reader(mapper.constructType(SimpleBean.class)));
    }

    @Test
    public void testCanSerializeAndDeserialize() {
        ObjectMapper mapper = new ObjectMapper();
        Assert.assertTrue(mapper.canSerialize(SimpleBean.class));
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        Assert.assertTrue(mapper.canSerialize(SimpleBean.class, cause));
        Assert.assertNull(cause.get());

        JavaType type = mapper.constructType(SimpleBean.class);
        Assert.assertTrue(mapper.canDeserialize(type));
        Assert.assertTrue(mapper.canDeserialize(type, cause));
        Assert.assertNull(cause.get());
    }

    @Test
    public void testCloseableSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);

        CloseableBean bean1 = new CloseableBean();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, bean1);
        Assert.assertTrue(bean1.closed);

        CloseableBean bean2 = new CloseableBean();
        JsonGenerator g = mapper.getFactory().createGenerator(new StringWriter());
        mapper.writeValue(g, bean2);
        Assert.assertTrue(bean2.closed);
    }

    @Test
    public void testRootWrappingSerializationAndDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);

        SimpleBean bean = new SimpleBean(13, "wrapped");
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("SimpleBean"));

        SimpleBean deserialized = mapper.readValue(json, SimpleBean.class);
        Assert.assertEquals(13, deserialized.x);
        Assert.assertEquals("wrapped", deserialized.y);
    }

    @Test
    public void testSubtypesAndFormatVisitors() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SimpleBean.class);
        mapper.registerSubtypes(new NamedType(SimpleBean.class, "Simple"));

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(SimpleBean.class, visitor);
        mapper.acceptJsonFormatVisitor(mapper.constructType(SimpleBean.class), visitor);
        Assert.assertNotNull(mapper.generateJsonSchema(SimpleBean.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatVisitorNullType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.acceptJsonFormatVisitor((JavaType) null, new JsonFormatVisitorWrapper.Base());
    }

    @Test
    public void testNullAndEmptyInputs() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode nullNode = mapper.readTree("null");
        Assert.assertNotNull(nullNode);
        Assert.assertTrue(nullNode.isNull());

        JsonNode emptyNode = mapper.readTree("");
        Assert.assertSame(NullNode.instance, emptyNode);

        String nullResult = mapper.readValue("null", String.class);
        Assert.assertNull(nullResult);
    }

    @Test(expected = JsonMappingException.class)
    public void testUnwrapMismatchThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        mapper.readValue("{\"WrongRoot\":{\"x\":1}}", SimpleBean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSchemaHandling() {
        ObjectMapper mapper = new ObjectMapper();
        FormatSchema schema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "UNSUPPORTED_SCHEMA";
            }
        };
        mapper.writer(schema);
    }
}
