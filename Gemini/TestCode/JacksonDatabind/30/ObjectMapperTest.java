package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.MappingIterator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ObjectMapperTest {

    private ObjectMapper mapper;

    static class SampleBean {
        public int id;
        public String name;

        public SampleBean() {}

        public SampleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonRootName("wrapped")
    static class WrappedBean {
        public int value;

        public WrappedBean() {}

        public WrappedBean(int value) {
            this.value = value;
        }
    }

    static class CloseableBean implements Closeable {
        public int count = 42;
        public boolean closed = false;

        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    abstract static class AbstractValue {
        public int val;
    }

    static class ConcreteValue extends AbstractValue {
        public ConcreteValue() { this.val = 7; }
    }

    static class SubclassWithoutCopy extends ObjectMapper {
        public SubclassWithoutCopy() { super(); }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testConstructorsAndCopy() {
        ObjectMapper m1 = new ObjectMapper();
        Assert.assertNotNull(m1.getFactory());
        Assert.assertNotNull(m1.getSerializerProvider());
        Assert.assertNotNull(m1.getDeserializationContext());

        JsonFactory jf = new JsonFactory();
        ObjectMapper m2 = new ObjectMapper(jf);
        Assert.assertSame(jf, m2.getFactory());

        ObjectMapper m3 = new ObjectMapper(null, null, null);
        Assert.assertNotNull(m3.getFactory());

        ObjectMapper copy = m1.copy();
        Assert.assertNotSame(m1, copy);
        Assert.assertNotNull(copy.getFactory());
        Assert.assertEquals(m1.version(), copy.version());
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidCopyCheck() {
        SubclassWithoutCopy custom = new SubclassWithoutCopy();
        custom.copy();
    }

    @Test
    public void testVersionAndDefaultClassIntrospector() {
        Assert.assertNotNull(mapper.version());
        Assert.assertNotNull(mapper.defaultClassIntrospector());
    }

    @Test
    public void testConfigurationAccessorsAndModifiers() {
        Assert.assertNotNull(mapper.getSerializationConfig());
        Assert.assertNotNull(mapper.getDeserializationConfig());
        Assert.assertNotNull(mapper.getDeserializationContext());
        Assert.assertNotNull(mapper.getSerializerFactory());
        Assert.assertNotNull(mapper.getSerializerProvider());

        mapper.setSerializerFactory(BeanSerializerFactory.instance);
        Assert.assertSame(BeanSerializerFactory.instance, mapper.getSerializerFactory());

        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        mapper.setSerializerProvider(sp);
        Assert.assertSame(sp, mapper.getSerializerProvider());

        mapper.setNodeFactory(JsonNodeFactory.instance);
        Assert.assertSame(JsonNodeFactory.instance, mapper.getNodeFactory());

        mapper.setTypeFactory(TypeFactory.defaultInstance());
        Assert.assertSame(TypeFactory.defaultInstance(), mapper.getTypeFactory());
        Assert.assertNotNull(mapper.constructType(String.class));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(df);
        Assert.assertEquals(df, mapper.getDateFormat());

        Locale locale = Locale.GERMANY;
        mapper.setLocale(locale);
        Assert.assertEquals(locale, mapper.getSerializationConfig().getLocale());

        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        mapper.setTimeZone(tz);
        Assert.assertEquals(tz, mapper.getSerializationConfig().getTimeZone());

        mapper.setBase64Variant(Base64Variants.MIME);
        Assert.assertEquals(Base64Variants.MIME, mapper.getSerializationConfig().getBase64Variant());

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Assert.assertEquals(JsonInclude.Include.NON_NULL, mapper.getSerializationConfig().getSerializationInclusion());

        PropertyNamingStrategy pns = PropertyNamingStrategy.SNAKE_CASE;
        mapper.setPropertyNamingStrategy(pns);
        Assert.assertSame(pns, mapper.getPropertyNamingStrategy());

        PrettyPrinter pp = new DefaultPrettyPrinter();
        mapper.setDefaultPrettyPrinter(pp);
        Assert.assertSame(pp, mapper.getSerializationConfig().getDefaultPrettyPrinter());

        mapper.setHandlerInstantiator(null);
        mapper.setInjectableValues(new InjectableValues.Std());
        Assert.assertNotNull(mapper.getInjectableValues());

        mapper.setConfig(mapper.getDeserializationConfig());
        mapper.setConfig(mapper.getSerializationConfig());
        Assert.assertNotNull(mapper.getJsonFactory());
    }

    @Test
    public void testFeatureConfigurations() {
        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        Assert.assertFalse(mapper.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
        mapper.enable(MapperFeature.AUTO_DETECT_FIELDS);
        Assert.assertTrue(mapper.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
        mapper.disable(MapperFeature.AUTO_DETECT_FIELDS);
        Assert.assertFalse(mapper.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        Assert.assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        Assert.assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        mapper.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRAP_ROOT_VALUE);
        Assert.assertTrue(mapper.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
        mapper.disable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRAP_ROOT_VALUE);
        Assert.assertFalse(mapper.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Assert.assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Assert.assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Assert.assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertTrue(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        mapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertFalse(mapper.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        Assert.assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        mapper.disable(JsonParser.Feature.ALLOW_COMMENTS);
        Assert.assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        Assert.assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));

        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        Assert.assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        mapper.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        Assert.assertTrue(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        mapper.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        Assert.assertFalse(mapper.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        Assert.assertTrue(mapper.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
    }

    @Test
    public void testMixInsAndSubtypes() {
        Assert.assertEquals(0, mapper.mixInCount());
        mapper.addMixIn(SampleBean.class, Object.class);
        Assert.assertEquals(1, mapper.mixInCount());
        Assert.assertEquals(Object.class, mapper.findMixInClassFor(SampleBean.class));

        Map<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
        mixins.put(String.class, Object.class);
        mapper.setMixIns(mixins);
        Assert.assertEquals(1, mapper.mixInCount());
        Assert.assertEquals(Object.class, mapper.findMixInClassFor(String.class));

        mapper.setMixInAnnotations(mixins);
        mapper.addMixInAnnotations(Integer.class, Number.class);
        Assert.assertEquals(Number.class, mapper.findMixInClassFor(Integer.class));

        ClassIntrospector.MixInResolver resolver = new ClassIntrospector.MixInResolver() {
            @Override
            public Class<?> findMixInClassFor(Class<?> cls) {
                return null;
            }
            @Override
            public ClassIntrospector.MixInResolver copy() {
                return this;
            }
        };
        mapper.setMixInResolver(resolver);

        StdSubtypeResolver str = new StdSubtypeResolver();
        mapper.setSubtypeResolver(str);
        Assert.assertSame(str, mapper.getSubtypeResolver());

        mapper.registerSubtypes(ConcreteValue.class);
        mapper.registerSubtypes(new NamedType(ConcreteValue.class, "concrete"));
    }

    @Test
    public void testVisibilityAndIntrospectors() {
        VisibilityChecker<?> vc = mapper.getVisibilityChecker();
        Assert.assertNotNull(vc);
        mapper.setVisibility(vc);
        mapper.setVisibilityChecker(vc);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(ai);
        mapper.setAnnotationIntrospectors(ai, ai);
    }

    @Test
    public void testDefaultTypingLogic() {
        ObjectMapper.DefaultTypeResolverBuilder builderObj = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        Assert.assertTrue(builderObj.useForType(TypeFactory.defaultInstance().constructType(Object.class)));
        Assert.assertFalse(builderObj.useForType(TypeFactory.defaultInstance().constructType(String.class)));

        ObjectMapper.DefaultTypeResolverBuilder builderNonConcrete = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        Assert.assertTrue(builderNonConcrete.useForType(TypeFactory.defaultInstance().constructType(AbstractValue.class)));
        Assert.assertFalse(builderNonConcrete.useForType(TypeFactory.defaultInstance().constructType(JsonNode.class)));

        ObjectMapper.DefaultTypeResolverBuilder builderArrays = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        Assert.assertTrue(builderArrays.useForType(TypeFactory.defaultInstance().constructType(AbstractValue[].class)));

        ObjectMapper.DefaultTypeResolverBuilder builderNonFinal = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        Assert.assertTrue(builderNonFinal.useForType(TypeFactory.defaultInstance().constructType(SampleBean.class)));
        Assert.assertFalse(builderNonFinal.useForType(TypeFactory.defaultInstance().constructType(JsonNode.class)));

        mapper.enableDefaultTyping();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@type");
        mapper.disableDefaultTyping();
        mapper.setDefaultTyping(builderNonFinal);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnableDefaultTypingExternalPropertyThrows() {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.EXTERNAL_PROPERTY);
    }

    @Test
    public void testProblemHandlersAndFilters() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        mapper.addHandler(handler);
        mapper.clearProblemHandlers();

        FilterProvider fp = new SimpleFilterProvider();
        mapper.setFilterProvider(fp);
        mapper.setFilters(fp);
    }

    @Test
    public void testModuleRegistration() {
        final AtomicBoolean setupCalled = new AtomicBoolean(false);
        Module module = new SimpleModule("TestModule", new Version(1, 0, 0, null, "grp", "art")) {
            @Override
            public void setupModule(SetupContext context) {
                setupCalled.set(true);
                Assert.assertNotNull(context.getMapperVersion());
                Assert.assertNotNull(context.getOwner());
                Assert.assertNotNull(context.getTypeFactory());
                Assert.assertTrue(context.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
                Assert.assertFalse(context.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
                Assert.assertFalse(context.isEnabled(SerializationFeature.INDENT_OUTPUT));
                Assert.assertTrue(context.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
                Assert.assertFalse(context.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
                Assert.assertTrue(context.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

                context.setMixInAnnotations(SampleBean.class, Object.class);
                context.setNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            }
        };

        mapper.registerModule(module);
        Assert.assertTrue(setupCalled.get());

        mapper.enable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        mapper.registerModules(module);
        mapper.registerModules(Arrays.asList(module));

        List<Module> discovered = ObjectMapper.findModules();
        Assert.assertNotNull(discovered);
        List<Module> discoveredCL = ObjectMapper.findModules(getClass().getClassLoader());
        Assert.assertNotNull(discoveredCL);

        mapper.findAndRegisterModules();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterModuleNullName() {
        Module invalid = new Module() {
            @Override public String getModuleName() { return null; }
            @Override public Version version() { return Version.unknownVersion(); }
            @Override public void setupModule(SetupContext context) {}
        };
        mapper.registerModule(invalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterModuleNullVersion() {
        Module invalid = new Module() {
            @Override public String getModuleName() { return "name"; }
            @Override public Version version() { return null; }
            @Override public void setupModule(SetupContext context) {}
        };
        mapper.registerModule(invalid);
    }

    @Test
    public void testTreeModelOperations() throws Exception {
        ObjectNode objNode = mapper.createObjectNode();
        objNode.put("id", 100);
        objNode.put("name", "test");
        ArrayNode arrNode = mapper.createArrayNode();
        arrNode.add(objNode);

        JsonParser parser = mapper.treeAsTokens(objNode);
        Assert.assertNotNull(parser);
        SampleBean bean = mapper.treeToValue(objNode, SampleBean.class);
        Assert.assertEquals(100, bean.id);
        Assert.assertEquals("test", bean.name);

        ObjectNode casted = mapper.treeToValue(objNode, ObjectNode.class);
        Assert.assertSame(objNode, casted);

        JsonNode treeFromVal = mapper.valueToTree(bean);
        Assert.assertEquals(100, treeFromVal.get("id").asInt());
        Assert.assertNull(mapper.valueToTree(null));

        String json = "{\"id\":100,\"name\":\"test\"}";
        Assert.assertEquals(100, mapper.readTree(json).get("id").asInt());
        Assert.assertEquals(100, mapper.readTree(json.getBytes("UTF-8")).get("id").asInt());
        Assert.assertEquals(100, mapper.readTree(new StringReader(json)).get("id").asInt());
        Assert.assertEquals(100, mapper.readTree(new ByteArrayInputStream(json.getBytes("UTF-8"))).get("id").asInt());

        File tmpFile = File.createTempFile("jackson_test", ".json");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(json.getBytes("UTF-8"));
        fos.close();

        Assert.assertEquals(100, mapper.readTree(tmpFile).get("id").asInt());
        Assert.assertEquals(100, mapper.readTree(tmpFile.toURI().toURL()).get("id").asInt());

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(gen, objNode);
        mapper.writeTree(gen, (TreeNode) arrNode);
        gen.close();
        Assert.assertTrue(sw.toString().length() > 0);
    }

    @Test
    public void testReadAndWriteValuesVariousSources() throws Exception {
        SampleBean bean = new SampleBean(1, "abc");
        String json = mapper.writeValueAsString(bean);
        byte[] bytes = mapper.writeValueAsBytes(bean);

        Assert.assertEquals(1, mapper.readValue(json, SampleBean.class).id);
        Assert.assertEquals(1, mapper.readValue(json, new TypeReference<SampleBean>() {}).id);
        Assert.assertEquals(1, ((SampleBean) mapper.readValue(json, mapper.constructType(SampleBean.class))).id);

        Assert.assertEquals(1, mapper.readValue(bytes, SampleBean.class).id);
        Assert.assertEquals(1, mapper.readValue(bytes, 0, bytes.length, SampleBean.class).id);
        Assert.assertEquals(1, mapper.readValue(bytes, new TypeReference<SampleBean>() {}).id);
        Assert.assertEquals(1, mapper.readValue(bytes, 0, bytes.length, new TypeReference<SampleBean>() {}).id);
        Assert.assertEquals(1, ((SampleBean) mapper.readValue(bytes, mapper.constructType(SampleBean.class))).id);
        Assert.assertEquals(1, ((SampleBean) mapper.readValue(bytes, 0, bytes.length, mapper.constructType(SampleBean.class))).id);

        Assert.assertEquals(1, mapper.readValue(new StringReader(json), SampleBean.class).id);
        Assert.assertEquals(1, mapper.readValue(new StringReader(json), new TypeReference<SampleBean>() {}).id);
        Assert.assertEquals(1, ((SampleBean) mapper.readValue(new StringReader(json), mapper.constructType(SampleBean.class))).id);

        Assert.assertEquals(1, mapper.readValue(new ByteArrayInputStream(bytes), SampleBean.class).id);
        Assert.assertEquals(1, mapper.readValue(new ByteArrayInputStream(bytes), new TypeReference<SampleBean>() {}).id);
        Assert.assertEquals(1, ((SampleBean) mapper.readValue(new ByteArrayInputStream(bytes), mapper.constructType(SampleBean.class))).id);

        File tmpFile = File.createTempFile("jackson_read", ".json");
        tmpFile.deleteOnExit();
        mapper.writeValue(tmpFile, bean);
        Assert.assertEquals(1, mapper.readValue(tmpFile, SampleBean.class).id);
        Assert.assertEquals(1, mapper.readValue(tmpFile, new TypeReference<SampleBean>() {}).id);
        Assert.assertEquals(1, ((SampleBean) mapper.readValue(tmpFile, mapper.constructType(SampleBean.class))).id);

        URL url = tmpFile.toURI().toURL();
        Assert.assertEquals(1, mapper.readValue(url, SampleBean.class).id);
        Assert.assertEquals(1, mapper.readValue(url, new TypeReference<SampleBean>() {}).id);
        Assert.assertEquals(1, ((SampleBean) mapper.readValue(url, mapper.constructType(SampleBean.class))).id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, bean);
        Assert.assertTrue(baos.size() > 0);

        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, bean);
        Assert.assertTrue(sw.toString().contains("abc"));
    }

    @Test
    public void testReadValuesIterator() throws Exception {
        String json = "{\"id\":1,\"name\":\"a\"}\n{\"id\":2,\"name\":\"b\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        MappingIterator<SampleBean> it = mapper.readValues(p, SampleBean.class);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(1, it.next().id);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(2, it.next().id);
        it.close();

        p = mapper.getFactory().createParser(json);
        MappingIterator<SampleBean> it2 = mapper.readValues(p, new TypeReference<SampleBean>() {});
        Assert.assertEquals(1, it2.next().id);
        it2.close();

        p = mapper.getFactory().createParser(json);
        MappingIterator<SampleBean> it3 = mapper.readValues(p, mapper.constructType(SampleBean.class));
        Assert.assertEquals(1, it3.next().id);
        it3.close();
    }

    @Test
    public void testConvertValue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 123);
        map.put("name", "converted");

        SampleBean bean = mapper.convertValue(map, SampleBean.class);
        Assert.assertEquals(123, bean.id);
        Assert.assertEquals("converted", bean.name);

        SampleBean same = mapper.convertValue(bean, SampleBean.class);
        Assert.assertSame(bean, same);

        Assert.assertNull(mapper.convertValue(null, SampleBean.class));
        Assert.assertNull(mapper.convertValue(null, new TypeReference<SampleBean>() {}));
        Assert.assertNull(mapper.convertValue(null, mapper.constructType(SampleBean.class)));

        SampleBean beanRef = mapper.convertValue(map, new TypeReference<SampleBean>() {});
        Assert.assertEquals(123, beanRef.id);

        SampleBean beanType = mapper.convertValue(map, mapper.constructType(SampleBean.class));
        Assert.assertEquals(123, beanType.id);
    }

    @Test
    public void testCanSerializeAndCanDeserialize() {
        Assert.assertTrue(mapper.canSerialize(SampleBean.class));
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        Assert.assertTrue(mapper.canSerialize(SampleBean.class, cause));
        Assert.assertNull(cause.get());

        Assert.assertTrue(mapper.canDeserialize(mapper.constructType(SampleBean.class)));
        Assert.assertTrue(mapper.canDeserialize(mapper.constructType(SampleBean.class), cause));
        Assert.assertNull(cause.get());
    }

    @Test
    public void testCloseableHandling() throws Exception {
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableBean cb = new CloseableBean();
        String json = mapper.writeValueAsString(cb);
        Assert.assertTrue(cb.closed);
        Assert.assertTrue(json.contains("42"));
    }

    @Test
    public void testRootWrapping() throws Exception {
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);

        WrappedBean wb = new WrappedBean(99);
        String json = mapper.writeValueAsString(wb);
        Assert.assertTrue(json.contains("wrapped"));

        WrappedBean read = mapper.readValue(json, WrappedBean.class);
        Assert.assertEquals(99, read.value);
    }

    @Test
    public void testWritersAndReaders() {
        ObjectWriter writer = mapper.writer();
        Assert.assertNotNull(writer);
        Assert.assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT));
        Assert.assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.FAIL_ON_EMPTY_BEANS));
        Assert.assertNotNull(mapper.writer(new SimpleDateFormat("yyyy")));
        Assert.assertNotNull(mapper.writerWithView(Object.class));
        Assert.assertNotNull(mapper.writerFor(SampleBean.class));
        Assert.assertNotNull(mapper.writerFor(new TypeReference<SampleBean>() {}));
        Assert.assertNotNull(mapper.writerFor(mapper.constructType(SampleBean.class)));
        Assert.assertNotNull(mapper.writer(new DefaultPrettyPrinter()));
        Assert.assertNotNull(mapper.writer((PrettyPrinter) null));
        Assert.assertNotNull(mapper.writerWithDefaultPrettyPrinter());
        Assert.assertNotNull(mapper.writer(new SimpleFilterProvider()));
        Assert.assertNotNull(mapper.writer(Base64Variants.MIME));
        Assert.assertNotNull(mapper.writer((CharacterEscapes) null));
        Assert.assertNotNull(mapper.writer(ContextAttributes.getEmpty()));
        Assert.assertNotNull(mapper.writerWithType(SampleBean.class));
        Assert.assertNotNull(mapper.writerWithType(new TypeReference<SampleBean>() {}));
        Assert.assertNotNull(mapper.writerWithType(mapper.constructType(SampleBean.class)));

        ObjectReader reader = mapper.reader();
        Assert.assertNotNull(reader);
        Assert.assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
        Assert.assertNotNull(mapper.readerForUpdating(new SampleBean()));
        Assert.assertNotNull(mapper.readerFor(SampleBean.class));
        Assert.assertNotNull(mapper.readerFor(new TypeReference<SampleBean>() {}));
        Assert.assertNotNull(mapper.readerFor(mapper.constructType(SampleBean.class)));
        Assert.assertNotNull(mapper.reader(JsonNodeFactory.instance));
        Assert.assertNotNull(mapper.reader(new InjectableValues.Std()));
        Assert.assertNotNull(mapper.readerWithView(Object.class));
        Assert.assertNotNull(mapper.reader(Base64Variants.MIME));
        Assert.assertNotNull(mapper.reader(ContextAttributes.getEmpty()));
        Assert.assertNotNull(mapper.reader(SampleBean.class));
        Assert.assertNotNull(mapper.reader(new TypeReference<SampleBean>() {}));
        Assert.assertNotNull(mapper.reader(mapper.constructType(SampleBean.class)));
    }

    @Test
    public void testSchemaAndVisitor() throws Exception {
        JsonSchema schema = mapper.generateJsonSchema(SampleBean.class);
        Assert.assertNotNull(schema);

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base();
        mapper.acceptJsonFormatVisitor(SampleBean.class, visitor);
        mapper.acceptJsonFormatVisitor(mapper.constructType(SampleBean.class), visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptJsonFormatVisitorNullTypeThrows() throws Exception {
        mapper.acceptJsonFormatVisitor((JavaType) null, new JsonFormatVisitorWrapper.Base());
    }

    @Test(expected = JsonMappingException.class)
    public void testReadEmptyContentThrows() throws Exception {
        mapper.readValue("", SampleBean.class);
    }

    @Test
    public void testReadNullToken() throws Exception {
        SampleBean bean = mapper.readValue("null", SampleBean.class);
        Assert.assertNull(bean);

        JsonNode nullNode = mapper.readTree("null");
        Assert.assertTrue(nullNode.isNull());
    }
}
