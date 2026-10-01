package com.fasterxml.jackson.databind;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    public static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() {}

        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public String getY() { return y; }
        public void setY(String y) { this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleBean)) return false;
            SimpleBean other = (SimpleBean) o;
            return x == other.x && (y == null ? other.y == null : y.equals(other.y));
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    @JsonRootName("wrapped")
    public static class RootWrappedBean {
        public int id;
        public String name;

        public RootWrappedBean() {}
        public RootWrappedBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class CloseableBean implements Closeable {
        public String data = "test";
        public boolean closed = false;

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    public static class Views {
        public static class Public {}
        public static class Internal extends Public {}
    }

    public static class ViewBean {
        @com.fasterxml.jackson.annotation.JsonView(Views.Public.class)
        public String pub = "public";

        @com.fasterxml.jackson.annotation.JsonView(Views.Internal.class)
        public String internal = "internal";
    }

    public interface MixInInterface {
        @JsonProperty("custom_x")
        int getX();
    }

    public static abstract class AbstractShape {}
    public static class Circle extends AbstractShape {
        public int radius = 5;
    }

    // --- Constructor & Factory Tests ---

    @Test
    public void testConstructors() {
        ObjectMapper m1 = new ObjectMapper();
        Assert.assertNotNull(m1.getFactory());
        Assert.assertNotNull(m1.getJsonFactory());
        Assert.assertNotNull(m1.getSerializerProvider());
        Assert.assertNotNull(m1.getDeserializationContext());

        JsonFactory jf = new JsonFactory();
        ObjectMapper m2 = new ObjectMapper(jf);
        Assert.assertSame(jf, m2.getFactory());
        Assert.assertSame(m2, jf.getCodec());

        ObjectMapper m3 = new ObjectMapper(null, null, null);
        Assert.assertNotNull(m3.getFactory());
        Assert.assertNotNull(m3.getSerializerFactory());
    }

    @Test
    public void testCopy() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.addMixIn(SimpleBean.class, MixInInterface.class);
        ObjectMapper copy = mapper.copy();

        Assert.assertNotNull(copy);
        Assert.assertNotSame(mapper, copy);
        Assert.assertNotSame(mapper.getFactory(), copy.getFactory());
        Assert.assertEquals(mapper.mixInCount(), copy.mixInCount());
        Assert.assertFalse(copy.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidCopySubclass() {
        class CustomMapper extends ObjectMapper {}
        CustomMapper cm = new CustomMapper();
        cm.copy();
    }

    @Test
    public void testVersion() {
        Version v = mapper.version();
        Assert.assertNotNull(v);
        Assert.assertFalse(v.isUnknownVersion());
    }

    // --- Default Typing Tests ---

    @Test
    public void testDefaultTypeResolverBuilder() {
        ObjectMapper.DefaultTypeResolverBuilder b1 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        TypeFactory tf = TypeFactory.defaultInstance();
        
        Assert.assertTrue(b1.useForType(tf.constructType(Object.class)));
        Assert.assertFalse(b1.useForType(tf.constructType(String.class)));

        ObjectMapper.DefaultTypeResolverBuilder b2 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        Assert.assertTrue(b2.useForType(tf.constructType(Object.class)));
        Assert.assertTrue(b2.useForType(tf.constructType(AbstractShape.class)));
        Assert.assertTrue(b2.useForType(tf.constructType(JsonNode.class))); // TreeNode is assignable
        Assert.assertFalse(b2.useForType(tf.constructType(SimpleBean.class)));

        ObjectMapper.DefaultTypeResolverBuilder b3 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        ArrayType arrOfAbstract = tf.constructArrayType(AbstractShape.class);
        Assert.assertTrue(b3.useForType(arrOfAbstract));
        Assert.assertFalse(b3.useForType(tf.constructType(SimpleBean.class)));

        ObjectMapper.DefaultTypeResolverBuilder b4 = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        Assert.assertTrue(b4.useForType(tf.constructType(SimpleBean.class)));
        Assert.assertFalse(b4.useForType(tf.constructType(String.class)));
        Assert.assertFalse(b4.useForType(tf.constructType(JsonNode.class))); // TreeNode excluded
        ArrayType arrOfSimple = tf.constructArrayType(SimpleBean.class);
        Assert.assertTrue(b4.useForType(arrOfSimple));

        Assert.assertNotNull(b4.buildTypeDeserializer(mapper.getDeserializationConfig(), tf.constructType(SimpleBean.class), null));
        Assert.assertNull(b4.buildTypeDeserializer(mapper.getDeserializationConfig(), tf.constructType(String.class), null));
        Assert.assertNotNull(b4.buildTypeSerializer(mapper.getSerializationConfig(), tf.constructType(SimpleBean.class), null));
        Assert.assertNull(b4.buildTypeSerializer(mapper.getSerializationConfig(), tf.constructType(String.class), null));
    }

    @Test
    public void testEnableAndDisableDefaultTyping() throws Exception {
        mapper.enableDefaultTyping();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT);
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "@type");
        
        SimpleBean b = new SimpleBean(1, "test");
        String json = mapper.writeValueAsString(b);
        Assert.assertNotNull(json);

        mapper.disableDefaultTyping();
        Assert.assertNotNull(mapper.writeValueAsString(b));
    }

    // --- Module Registration Tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterModuleNullName() {
        mapper.registerModule(new Module() {
            @Override public String getModuleName() { return null; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public void setupModule(SetupContext context) {}
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterModuleNullVersion() {
        mapper.registerModule(new Module() {
            @Override public String getModuleName() { return "TestMod"; }
            @Override public Version version() { return null; }
            @Override public void setupModule(SetupContext context) {}
        });
    }

    @Test
    public void testRegisterModuleFullSetup() {
        final AtomicBoolean setupCalled = new AtomicBoolean(false);
        Module mod = new Module() {
            @Override
            public String getModuleName() { return "CustomTestModule"; }
            @Override
            public Version version() { return new Version(1, 0, 0, null, "com.test", "mod"); }
            @Override
            public void setupModule(SetupContext context) {
                setupCalled.set(true);
                Assert.assertNotNull(context.getMapperVersion());
                Assert.assertSame(mapper, context.getOwner());
                Assert.assertNotNull(context.getTypeFactory());
                Assert.assertTrue(context.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
                Assert.assertTrue(context.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
                Assert.assertTrue(context.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
                Assert.assertFalse(context.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
                Assert.assertFalse(context.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
                Assert.assertFalse(context.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));

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
                context.addAbstractTypeResolver(new SimpleAbstractTypeResolver());
                context.addValueInstantiators(new ValueInstantiators.Base());
                context.setClassIntrospector(BasicClassIntrospector.instance);
                context.insertAnnotationIntrospector(new JacksonAnnotationIntrospector());
                context.appendAnnotationIntrospector(new JacksonAnnotationIntrospector());
                context.registerSubtypes(Circle.class);
                context.registerSubtypes(new NamedType(Circle.class, "circle"));
                context.setMixInAnnotations(SimpleBean.class, MixInInterface.class);
                context.addDeserializationProblemHandler(new DeserializationProblemHandler() {});
                context.setNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            }
        };

        mapper.configure(JsonFactory.Feature.INTERN_FIELD_NAMES, false);
        mapper.registerModules(mod);
        Assert.assertTrue(setupCalled.get());
        Assert.assertEquals(1, mapper.mixInCount());
    }

    @Test
    public void testFindModules() {
        List<Module> mods = ObjectMapper.findModules();
        Assert.assertNotNull(mods);
        List<Module> modsWithCL = ObjectMapper.findModules(getClass().getClassLoader());
        Assert.assertNotNull(modsWithCL);
        mapper.findAndRegisterModules();
    }

    @Test
    public void testRegisterModulesIterable() {
        Module mod = new Module() {
            @Override public String getModuleName() { return "Mod1"; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public void setupModule(SetupContext context) {}
        };
        mapper.registerModules(Collections.singletonList(mod));
    }

    // --- Configuration Getters & Setters ---

    @Test
    public void testConfigAccessors() {
        Assert.assertNotNull(mapper.getSerializationConfig());
        Assert.assertNotNull(mapper.getDeserializationConfig());
        Assert.assertNotNull(mapper.getDeserializationContext());

        SerializerFactory sf = BeanSerializerFactory.instance;
        mapper.setSerializerFactory(sf);
        Assert.assertSame(sf, mapper.getSerializerFactory());

        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        mapper.setSerializerProvider(sp);
        Assert.assertSame(sp, mapper.getSerializerProvider());

        Map<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
        mixins.put(SimpleBean.class, MixInInterface.class);
        mapper.setMixInAnnotations(mixins);
        Assert.assertEquals(1, mapper.mixInCount());
        Assert.assertEquals(MixInInterface.class, mapper.findMixInClassFor(SimpleBean.class));

        mapper.setMixInAnnotations(null);
        Assert.assertEquals(0, mapper.mixInCount());

        mapper.addMixInAnnotations(SimpleBean.class, MixInInterface.class);
        Assert.assertEquals(MixInInterface.class, mapper.findMixInClassFor(SimpleBean.class));
    }

    @Test
    public void testVisibilityAndIntrospectors() {
        VisibilityChecker<?> vc = mapper.getVisibilityChecker();
        Assert.assertNotNull(vc);
        mapper.setVisibilityChecker(vc);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        SubtypeResolver str = new StdSubtypeResolver();
        mapper.setSubtypeResolver(str);
        Assert.assertSame(str, mapper.getSubtypeResolver());

        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        mapper.setAnnotationIntrospectors(new JacksonAnnotationIntrospector(), new JacksonAnnotationIntrospector());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void testTypeFactoryAndNodeFactory() {
        TypeFactory tf = TypeFactory.defaultInstance();
        mapper.setTypeFactory(tf);
        Assert.assertSame(tf, mapper.getTypeFactory());
        JavaType jt = mapper.constructType(SimpleBean.class);
        Assert.assertEquals(SimpleBean.class, jt.getRawClass());

        JsonNodeFactory jnf = JsonNodeFactory.instance;
        mapper.setNodeFactory(jnf);
        Assert.assertSame(jnf, mapper.getNodeFactory());

        ObjectNode objNode = mapper.createObjectNode();
        Assert.assertNotNull(objNode);
        ArrayNode arrNode = mapper.createArrayNode();
        Assert.assertNotNull(arrNode);
    }

    @Test
    public void testProblemHandlers() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        mapper.addHandler(handler);
        mapper.clearProblemHandlers();
    }

    @Test
    public void testSetConfigsDirectly() {
        DeserializationConfig dc = mapper.getDeserializationConfig();
        SerializationConfig sc = mapper.getSerializationConfig();
        mapper.setConfig(dc);
        mapper.setConfig(sc);
        Assert.assertSame(dc, mapper.getDeserializationConfig());
        Assert.assertSame(sc, mapper.getSerializationConfig());
    }

    @Test
    public void testFiltersAndBase64() {
        FilterProvider fp = new SimpleFilterProvider();
        mapper.setFilters(fp);

        Base64Variant b64 = Base64Variants.MIME;
        mapper.setBase64Variant(b64);
    }

    @Test
    public void testDateFormatLocaleTimeZoneHandlerInstantiatorInjectable() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(df);

        mapper.setHandlerInstantiator(new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, AnnotatedClass annotated, Class<?> deserClass) { return null; }
            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, AnnotatedClass annotated, Class<?> deserClass) { return null; }
            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, AnnotatedClass annotated, Class<?> serClass) { return null; }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, AnnotatedClass annotated, Class<?> builderClass) { return null; }
            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, AnnotatedClass annotated, Class<?> resolverClass) { return null; }
        });

        InjectableValues iv = new InjectableValues.Std();
        mapper.setInjectableValues(iv);

        Locale loc = Locale.GERMANY;
        mapper.setLocale(loc);

        TimeZone tz = TimeZone.getTimeZone("PST");
        mapper.setTimeZone(tz);
    }

    // --- Feature Enable / Disable / Configure Tests ---

    @Test
    public void testFeatureToggles() {
        mapper.configure(MapperFeature.AUTO_DETECT_CREATORS, false);
        Assert.assertFalse(mapper.isEnabled(MapperFeature.AUTO_DETECT_CREATORS));
        mapper.enable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS);
        Assert.assertTrue(mapper.isEnabled(MapperFeature.AUTO_DETECT_CREATORS));
        mapper.disable(MapperFeature.AUTO_DETECT_CREATORS, MapperFeature.AUTO_DETECT_FIELDS);
        Assert.assertFalse(mapper.isEnabled(MapperFeature.AUTO_DETECT_CREATORS));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        Assert.assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        Assert.assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, SerializationFeature.FAIL_ON_EMPTY_BEANS);

        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        Assert.assertTrue(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.UNWRAP_ROOT_VALUE);
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Assert.assertFalse(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.UNWRAP_ROOT_VALUE);

        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        Assert.assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        Assert.assertFalse(mapper.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
    }

    // --- Read Value Variants ---

    @Test
    public void testReadValueVariants() throws Exception {
        String json = "{\"x\":10,\"y\":\"hello\"}";
        SimpleBean expected = new SimpleBean(10, "hello");

        // String
        SimpleBean b1 = mapper.readValue(json, SimpleBean.class);
        Assert.assertEquals(expected, b1);
        SimpleBean b2 = mapper.readValue(json, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b2);
        SimpleBean b3 = mapper.readValue(json, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b3);

        // JsonParser
        JsonParser jp = mapper.getFactory().createParser(json);
        SimpleBean b4 = mapper.readValue(jp, SimpleBean.class);
        Assert.assertEquals(expected, b4);
        jp.close();

        jp = mapper.getFactory().createParser(json);
        SimpleBean b5 = mapper.readValue(jp, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b5);
        jp.close();

        jp = mapper.getFactory().createParser(json);
        SimpleBean b6 = mapper.readValue(jp, (JavaType) mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b6);
        jp.close();

        jp = mapper.getFactory().createParser(json);
        SimpleBean b6_resolved = mapper.readValue(jp, (com.fasterxml.jackson.core.type.ResolvedType) mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b6_resolved);
        jp.close();

        // byte[]
        byte[] bytes = json.getBytes("UTF-8");
        SimpleBean b7 = mapper.readValue(bytes, SimpleBean.class);
        Assert.assertEquals(expected, b7);
        SimpleBean b8 = mapper.readValue(bytes, 0, bytes.length, SimpleBean.class);
        Assert.assertEquals(expected, b8);
        SimpleBean b9 = mapper.readValue(bytes, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b9);
        SimpleBean b10 = mapper.readValue(bytes, 0, bytes.length, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b10);
        SimpleBean b11 = mapper.readValue(bytes, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b11);
        SimpleBean b12 = mapper.readValue(bytes, 0, bytes.length, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b12);

        // Reader & Stream
        SimpleBean b13 = mapper.readValue(new StringReader(json), SimpleBean.class);
        Assert.assertEquals(expected, b13);
        SimpleBean b14 = mapper.readValue(new StringReader(json), new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b14);
        SimpleBean b15 = mapper.readValue(new StringReader(json), mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b15);

        SimpleBean b16 = mapper.readValue(new ByteArrayInputStream(bytes), SimpleBean.class);
        Assert.assertEquals(expected, b16);
        SimpleBean b17 = mapper.readValue(new ByteArrayInputStream(bytes), new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b17);
        SimpleBean b18 = mapper.readValue(new ByteArrayInputStream(bytes), mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b18);

        // File
        File tmp = File.createTempFile("jackson_test", ".json");
        tmp.deleteOnExit();
        mapper.writeValue(tmp, expected);
        SimpleBean b19 = mapper.readValue(tmp, SimpleBean.class);
        Assert.assertEquals(expected, b19);
        SimpleBean b20 = mapper.readValue(tmp, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b20);
        SimpleBean b21 = mapper.readValue(tmp, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b21);

        // URL
        URL url = tmp.toURI().toURL();
        SimpleBean b22 = mapper.readValue(url, SimpleBean.class);
        Assert.assertEquals(expected, b22);
        SimpleBean b23 = mapper.readValue(url, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(expected, b23);
        SimpleBean b24 = mapper.readValue(url, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(expected, b24);
    }

    @Test
    public void testReadValueNullAndEmpty() throws Exception {
        Object res = mapper.readValue("null", SimpleBean.class);
        Assert.assertNull(res);

        try {
            mapper.readValue("", SimpleBean.class);
            Assert.fail("Expected JsonMappingException for empty input");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    // --- Read Tree Tests ---

    @Test
    public void testReadTreeVariants() throws Exception {
        String json = "{\"x\":10,\"y\":\"hello\"}";

        JsonNode n1 = mapper.readTree(json);
        Assert.assertEquals(10, n1.get("x").asInt());
        Assert.assertEquals("hello", n1.get("y").asText());

        JsonNode n2 = mapper.readTree(json.getBytes("UTF-8"));
        Assert.assertEquals(n1, n2);

        JsonNode n3 = mapper.readTree(new StringReader(json));
        Assert.assertEquals(n1, n3);

        JsonNode n4 = mapper.readTree(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Assert.assertEquals(n1, n4);

        File tmp = File.createTempFile("jackson_tree_test", ".json");
        tmp.deleteOnExit();
        mapper.writeValue(tmp, n1);
        JsonNode n5 = mapper.readTree(tmp);
        Assert.assertEquals(n1, n5);

        JsonNode n6 = mapper.readTree(tmp.toURI().toURL());
        Assert.assertEquals(n1, n6);

        JsonParser jp = mapper.getFactory().createParser(json);
        JsonNode n7 = mapper.readTree(jp);
        Assert.assertEquals(n1, n7);
        jp.close();

        // null tree
        JsonNode nullNode = mapper.readTree("null");
        Assert.assertTrue(nullNode.isNull());

        jp = mapper.getFactory().createParser("");
        JsonNode emptyNode = mapper.readTree(jp);
        Assert.assertNull(emptyNode);
        jp.close();
    }

    // --- Read Values (MappingIterator) Tests ---

    @Test
    public void testReadValues() throws Exception {
        String json = "{\"x\":1,\"y\":\"a\"}\n{\"x\":2,\"y\":\"b\"}";
        JsonParser jp = mapper.getFactory().createParser(json);

        MappingIterator<SimpleBean> it1 = mapper.readValues(jp, SimpleBean.class);
        Assert.assertTrue(it1.hasNext());
        Assert.assertEquals(1, it1.next().x);
        Assert.assertTrue(it1.hasNext());
        Assert.assertEquals(2, it1.next().x);
        Assert.assertFalse(it1.hasNext());
        jp.close();

        jp = mapper.getFactory().createParser(json);
        MappingIterator<SimpleBean> it2 = mapper.readValues(jp, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(1, it2.next().x);
        jp.close();

        jp = mapper.getFactory().createParser(json);
        MappingIterator<SimpleBean> it3 = mapper.readValues(jp, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(1, it3.next().x);
        jp.close();

        jp = mapper.getFactory().createParser(json);
        MappingIterator<SimpleBean> it4 = mapper.readValues(jp, (com.fasterxml.jackson.core.type.ResolvedType) mapper.constructType(SimpleBean.class));
        Assert.assertEquals(1, it4.next().x);
        jp.close();
    }

    // --- Write Value Variants ---

    @Test
    public void testWriteValueVariants() throws Exception {
        SimpleBean bean = new SimpleBean(42, "universe");

        String str = mapper.writeValueAsString(bean);
        Assert.assertTrue(str.contains("\"x\":42"));

        byte[] bytes = mapper.writeValueAsBytes(bean);
        Assert.assertTrue(bytes.length > 0);

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, bean);
        Assert.assertEquals(str, sw.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, bean);
        Assert.assertArrayEquals(bytes, baos.toByteArray());

        File tmp = File.createTempFile("jackson_write", ".json");
        tmp.deleteOnExit();
        mapper.writeValue(tmp, bean);
        Assert.assertTrue(tmp.length() > 0);

        // JsonGenerator writeValue
        sw = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw);
        mapper.writeValue(jg, bean);
        Assert.assertEquals(str, sw.toString());
    }

    @Test
    public void testWriteCloseableValue() throws Exception {
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableBean cb = new CloseableBean();
        Assert.assertFalse(cb.closed);
        String s = mapper.writeValueAsString(cb);
        Assert.assertNotNull(s);
        Assert.assertTrue(cb.closed);

        cb = new CloseableBean();
        StringWriter sw = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw);
        mapper.writeValue(jg, cb);
        Assert.assertTrue(cb.closed);
    }

    @Test
    public void testWriteTree() throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("field", "val");

        StringWriter sw = new StringWriter();
        JsonGenerator jg = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(jg, node);
        jg.close();
        Assert.assertEquals("{\"field\":\"val\"}", sw.toString());

        sw = new StringWriter();
        jg = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(jg, (TreeNode) node);
        jg.close();
        Assert.assertEquals("{\"field\":\"val\"}", sw.toString());
    }

    // --- Tree & Token Conversions ---

    @Test
    public void testTreeToValueAndValueToTree() throws Exception {
        SimpleBean bean = new SimpleBean(7, "seven");
        JsonNode node = mapper.valueToTree(bean);
        Assert.assertNotNull(node);
        Assert.assertEquals(7, node.get("x").asInt());

        Assert.assertNull(mapper.valueToTree(null));

        SimpleBean beanAgain = mapper.treeToValue(node, SimpleBean.class);
        Assert.assertEquals(bean, beanAgain);

        // shortcut test for same type
        JsonNode sameNode = mapper.treeToValue(node, JsonNode.class);
        Assert.assertSame(node, sameNode);

        // treeAsTokens
        JsonParser jp = mapper.treeAsTokens(node);
        SimpleBean parsed = mapper.readValue(jp, SimpleBean.class);
        Assert.assertEquals(bean, parsed);
        jp.close();
    }

    // --- Convert Value Tests ---

    @Test
    public void testConvertValue() {
        SimpleBean bean = new SimpleBean(99, "ninety-nine");

        Map<?, ?> map = mapper.convertValue(bean, Map.class);
        Assert.assertEquals(99, ((Number) map.get("x")).intValue());
        Assert.assertEquals("ninety-nine", map.get("y"));

        SimpleBean reconverted = mapper.convertValue(map, SimpleBean.class);
        Assert.assertEquals(bean, reconverted);

        SimpleBean reconvertedRef = mapper.convertValue(map, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(bean, reconvertedRef);

        SimpleBean reconvertedJt = mapper.convertValue(map, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(bean, reconvertedJt);

        Assert.assertNull(mapper.convertValue(null, SimpleBean.class));
        Assert.assertNull(mapper.convertValue(null, mapper.constructType(SimpleBean.class)));

        // direct cast optimization branch
        String s = "hello";
        String s2 = mapper.convertValue(s, String.class);
        Assert.assertSame(s, s2);
    }

    // --- Serialization and Deserialization Checking (canSerialize / canDeserialize) ---

    @Test
    public void testCanSerializeAndDeserialize() {
        Assert.assertTrue(mapper.canSerialize(SimpleBean.class));
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        Assert.assertTrue(mapper.canSerialize(SimpleBean.class, cause));
        Assert.assertNull(cause.get());

        JavaType type = mapper.constructType(SimpleBean.class);
        Assert.assertTrue(mapper.canDeserialize(type));
        cause.set(null);
        Assert.assertTrue(mapper.canDeserialize(type, cause));
        Assert.assertNull(cause.get());
    }

    // --- Root Name Wrapping & Unwrapping ---

    @Test
    public void testRootWrappingSerializationAndDeserialization() throws Exception {
        ObjectMapper rootMapper = new ObjectMapper();
        rootMapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        rootMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);

        RootWrappedBean bean = new RootWrappedBean(123, "wrappedItem");
        String json = rootMapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"wrapped\":"));

        RootWrappedBean deserialized = rootMapper.readValue(json, RootWrappedBean.class);
        Assert.assertEquals(123, deserialized.id);
        Assert.assertEquals("wrappedItem", deserialized.name);
    }

    @Test(expected = JsonMappingException.class)
    public void testRootUnwrappingMismatch() throws Exception {
        ObjectMapper rootMapper = new ObjectMapper();
        rootMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        String json = "{\"wrongRoot\":{\"id\":123,\"name\":\"test\"}}";
        rootMapper.readValue(json, RootWrappedBean.class);
    }

    // --- ObjectWriter and ObjectReader Factory Tests ---

    @Test
    public void testWriterFactories() throws Exception {
        Assert.assertNotNull(mapper.writer());
        Assert.assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT));
        Assert.assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS));
        Assert.assertNotNull(mapper.writer((DateFormat) new SimpleDateFormat("yyyy-MM-dd")));
        Assert.assertNotNull(mapper.writerWithView(Views.Public.class));
        Assert.assertNotNull(mapper.writerWithType(SimpleBean.class));
        Assert.assertNotNull(mapper.writerWithType(new TypeReference<SimpleBean>() {}));
        Assert.assertNotNull(mapper.writerWithType(mapper.constructType(SimpleBean.class)));
        Assert.assertNotNull(mapper.writer(new MinimalPrettyPrinter()));
        Assert.assertNotNull(mapper.writer((PrettyPrinter) null));
        Assert.assertNotNull(mapper.writerWithDefaultPrettyPrinter());
        Assert.assertNotNull(mapper.writer(new SimpleFilterProvider()));
        Assert.assertNotNull(mapper.writer(Base64Variants.MIME));
        Assert.assertNotNull(mapper.writer(new CharacterEscapes() {
            @Override public int[] getEscapeCodesForAscii() { return standardAsciiEscapesForJSON(); }
            @Override public SerializableString getEscapeSequence(int ch) { return null; }
        }));
        Assert.assertNotNull(mapper.writer(ContextAttributes.getEmpty()));

        // Test view serialization via writer
        ViewBean vb = new ViewBean();
        String pubOnly = mapper.writerWithView(Views.Public.class).writeValueAsString(vb);
        Assert.assertTrue(pubOnly.contains("pub"));
        Assert.assertFalse(pubOnly.contains("internal"));
    }

    @Test
    public void testReaderFactories() throws Exception {
        Assert.assertNotNull(mapper.reader());
        Assert.assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assert.assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        
        SimpleBean updateTarget = new SimpleBean(1, "orig");
        ObjectReader updatingReader = mapper.readerForUpdating(updateTarget);
        updatingReader.readValue("{\"y\":\"updated\"}");
        Assert.assertEquals("updated", updateTarget.y);
        Assert.assertEquals(1, updateTarget.x);

        Assert.assertNotNull(mapper.reader(SimpleBean.class));
        Assert.assertNotNull(mapper.reader(new TypeReference<SimpleBean>() {}));
        Assert.assertNotNull(mapper.reader(mapper.constructType(SimpleBean.class)));
        Assert.assertNotNull(mapper.reader(JsonNodeFactory.instance));
        Assert.assertNotNull(mapper.reader(new InjectableValues.Std()));
        Assert.assertNotNull(mapper.readerWithView(Views.Public.class));
        Assert.assertNotNull(mapper.reader(Base64Variants.MIME));
        Assert.assertNotNull(mapper.reader(ContextAttributes.getEmpty()));
    }

    // --- Schema Generation & Visitors ---

    @Test
    public void testSchemaGeneration() throws Exception {
        com.fasterxml.jackson.databind.jsonschema.JsonSchema schema = mapper.generateJsonSchema(SimpleBean.class);
        Assert.assertNotNull(schema);
        Assert.assertNotNull(schema.getSchemaNode());
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(SimpleBean.class, visitor);
        mapper.acceptJsonFormatVisitor(mapper.constructType(SimpleBean.class), visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptJsonFormatVisitorNullType() throws Exception {
        mapper.acceptJsonFormatVisitor((JavaType) null, new JsonFormatVisitorWrapper.Base());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySchemaTypeInvalid() {
        FormatSchema dummySchema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "UNSUPPORTED";
            }
        };
        mapper.writer(dummySchema);
    }
}
