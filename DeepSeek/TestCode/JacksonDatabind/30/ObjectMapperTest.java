package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.*;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ObjectMapperTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    public static class SimpleBean {
        public int id;
        public String name;
        public SimpleBean() {}
        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(mapper);
        assertNotNull(mapper.getFactory());
        assertNotNull(mapper.getSerializationConfig());
        assertNotNull(mapper.getDeserializationConfig());
    }

    @Test
    public void testConstructorWithJsonFactory() {
        JsonFactory jf = new MappingJsonFactory();
        ObjectMapper m = new ObjectMapper(jf);
        assertSame(jf, m.getFactory());
    }

    @Test
    public void testConstructorWithAllArgs() {
        JsonFactory jf = new MappingJsonFactory();
        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        DefaultDeserializationContext dc = new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance);
        ObjectMapper m = new ObjectMapper(jf, sp, dc);
        assertSame(jf, m.getFactory());
        assertNotNull(m.getSerializerProvider());
        assertNotNull(m.getDeserializationContext());
    }

    @Test
    public void testCopy() {
        ObjectMapper copy = mapper.copy();
        assertNotSame(mapper, copy);
        assertEquals(mapper.getSerializationConfig().getClass(), copy.getSerializationConfig().getClass());
    }

    @Test(expected = IllegalStateException.class)
    public void testCopyOnSubclassWithoutOverride() {
        ObjectMapper sub = new ObjectMapper() {};
        sub.copy();
    }

    @Test
    public void testRegisterModule() {
        Module mod = mock(Module.class);
        when(mod.getModuleName()).thenReturn("test");
        when(mod.version()).thenReturn(Version.unknownVersion());
        mapper.registerModule(mod);
        verify(mod).setupModule(any(Module.SetupContext.class));
    }

    @Test
    public void testRegisterModulesArray() {
        Module mod1 = mock(Module.class);
        when(mod1.getModuleName()).thenReturn("mod1");
        when(mod1.version()).thenReturn(Version.unknownVersion());
        Module mod2 = mock(Module.class);
        when(mod2.getModuleName()).thenReturn("mod2");
        when(mod2.version()).thenReturn(Version.unknownVersion());
        mapper.registerModules(mod1, mod2);
        verify(mod1).setupModule(any(Module.SetupContext.class));
        verify(mod2).setupModule(any(Module.SetupContext.class));
    }

    @Test
    public void testRegisterModulesIterable() {
        Module mod = mock(Module.class);
        when(mod.getModuleName()).thenReturn("mod");
        when(mod.version()).thenReturn(Version.unknownVersion());
        mapper.registerModules(Collections.singletonList(mod));
        verify(mod).setupModule(any(Module.SetupContext.class));
    }

    @Test
    public void testFindModules() {
        List<Module> modules = ObjectMapper.findModules();
        assertNotNull(modules);
    }

    @Test
    public void testFindModulesWithClassLoader() {
        List<Module> modules = ObjectMapper.findModules(getClass().getClassLoader());
        assertNotNull(modules);
    }

    @Test
    public void testFindAndRegisterModules() {
        ObjectMapper m = new ObjectMapper();
        m.findAndRegisterModules();
    }

    @Test
    public void testGetSerializationConfig() {
        assertNotNull(mapper.getSerializationConfig());
    }

    @Test
    public void testGetDeserializationConfig() {
        assertNotNull(mapper.getDeserializationConfig());
    }

    @Test
    public void testGetDeserializationContext() {
        assertNotNull(mapper.getDeserializationContext());
    }

    @Test
    public void testSetSerializerFactory() {
        SerializerFactory sf = mock(SerializerFactory.class);
        mapper.setSerializerFactory(sf);
        assertSame(sf, mapper.getSerializerFactory());
    }

    @Test
    public void testGetSerializerFactory() {
        assertNotNull(mapper.getSerializerFactory());
    }

    @Test
    public void testSetSerializerProvider() {
        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        mapper.setSerializerProvider(sp);
        assertSame(sp, mapper.getSerializerProvider());
    }

    @Test
    public void testGetSerializerProvider() {
        assertNotNull(mapper.getSerializerProvider());
    }

    @Test
    public void testSetMixIns() {
        Map<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
        mixins.put(SimpleBean.class, Object.class);
        mapper.setMixIns(mixins);
        assertEquals(Object.class, mapper.findMixInClassFor(SimpleBean.class));
    }

    @Test
    public void testAddMixIn() {
        mapper.addMixIn(SimpleBean.class, Object.class);
        assertEquals(Object.class, mapper.findMixInClassFor(SimpleBean.class));
    }

    @Test
    public void testSetMixInResolver() {
        ClassIntrospector.MixInResolver resolver = mock(ClassIntrospector.MixInResolver.class);
        mapper.setMixInResolver(resolver);
    }

    @Test
    public void testFindMixInClassFor() {
        assertNull(mapper.findMixInClassFor(SimpleBean.class));
    }

    @Test
    public void testMixInCount() {
        assertEquals(0, mapper.mixInCount());
        mapper.addMixIn(SimpleBean.class, Object.class);
        assertEquals(1, mapper.mixInCount());
    }

    @Test
    public void testSetVisibilityChecker() {
        VisibilityChecker<?> vc = VisibilityChecker.Std.defaultInstance();
        mapper.setVisibility(vc);
    }

    @Test
    public void testSetVisibilityPropertyAccessor() {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Test
    public void testGetSubtypeResolver() {
        assertNotNull(mapper.getSubtypeResolver());
    }

    @Test
    public void testSetSubtypeResolver() {
        SubtypeResolver sr = new StdSubtypeResolver();
        mapper.setSubtypeResolver(sr);
        assertSame(sr, mapper.getSubtypeResolver());
    }

    @Test
    public void testSetAnnotationIntrospector() {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(ai);
    }

    @Test
    public void testSetAnnotationIntrospectors() {
        AnnotationIntrospector ser = new JacksonAnnotationIntrospector();
        AnnotationIntrospector deser = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospectors(ser, deser);
    }

    @Test
    public void testSetPropertyNamingStrategy() {
        PropertyNamingStrategy s = PropertyNamingStrategy.SNAKE_CASE;
        mapper.setPropertyNamingStrategy(s);
        assertSame(s, mapper.getPropertyNamingStrategy());
    }

    @Test
    public void testGetPropertyNamingStrategy() {
        assertNotNull(mapper.getPropertyNamingStrategy());
    }

    @Test
    public void testSetSerializationInclusion() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    public void testSetDefaultPrettyPrinter() {
        PrettyPrinter pp = new DefaultPrettyPrinter();
        mapper.setDefaultPrettyPrinter(pp);
    }

    @Test
    public void testEnableDefaultTyping() {
        mapper.enableDefaultTyping();
    }

    @Test
    public void testEnableDefaultTypingWithApplicability() {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnableDefaultTypingWithExternalProperty() {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.EXTERNAL_PROPERTY);
    }

    @Test
    public void testEnableDefaultTypingAsProperty() {
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, "type");
    }

    @Test
    public void testDisableDefaultTyping() {
        mapper.disableDefaultTyping();
    }

    @Test
    public void testSetDefaultTyping() {
        TypeResolverBuilder<?> typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.setDefaultTyping(typer);
    }

    @Test
    public void testRegisterSubtypesClasses() {
        mapper.registerSubtypes(SimpleBean.class);
    }

    @Test
    public void testRegisterSubtypesNamedTypes() {
        mapper.registerSubtypes(new NamedType(SimpleBean.class, "simple"));
    }

    @Test
    public void testGetTypeFactory() {
        assertNotNull(mapper.getTypeFactory());
    }

    @Test
    public void testSetTypeFactory() {
        TypeFactory tf = TypeFactory.defaultInstance();
        mapper.setTypeFactory(tf);
        assertSame(tf, mapper.getTypeFactory());
    }

    @Test
    public void testConstructType() {
        JavaType t = mapper.constructType(SimpleBean.class);
        assertNotNull(t);
    }

    @Test
    public void testGetNodeFactory() {
        assertNotNull(mapper.getNodeFactory());
    }

    @Test
    public void testSetNodeFactory() {
        JsonNodeFactory nf = JsonNodeFactory.instance;
        mapper.setNodeFactory(nf);
        assertSame(nf, mapper.getNodeFactory());
    }

    @Test
    public void testAddHandler() {
        DeserializationProblemHandler h = mock(DeserializationProblemHandler.class);
        mapper.addHandler(h);
    }

    @Test
    public void testClearProblemHandlers() {
        mapper.clearProblemHandlers();
    }

    @Test
    public void testSetConfigDeserialization() {
        DeserializationConfig config = mapper.getDeserializationConfig();
        mapper.setConfig(config);
    }

    @Test
    public void testSetConfigSerialization() {
        SerializationConfig config = mapper.getSerializationConfig();
        mapper.setConfig(config);
    }

    @Test
    public void testSetFilterProvider() {
        FilterProvider fp = new SimpleFilterProvider();
        mapper.setFilterProvider(fp);
    }

    @Test
    public void testSetBase64Variant() {
        mapper.setBase64Variant(Base64Variants.MIME);
    }

    @Test
    public void testSetDateFormat() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        mapper.setDateFormat(df);
        assertSame(df, mapper.getDateFormat());
    }

    @Test
    public void testGetDateFormat() {
        assertNotNull(mapper.getDateFormat());
    }

    @Test
    public void testSetHandlerInstantiator() {
        HandlerInstantiator hi = mock(HandlerInstantiator.class);
        mapper.setHandlerInstantiator(hi);
    }

    @Test
    public void testSetInjectableValues() {
        InjectableValues iv = new InjectableValues.Std();
        mapper.setInjectableValues(iv);
        assertSame(iv, mapper.getInjectableValues());
    }

    @Test
    public void testGetInjectableValues() {
        assertNull(mapper.getInjectableValues());
    }

    @Test
    public void testSetLocale() {
        mapper.setLocale(Locale.CANADA);
    }

    @Test
    public void testSetTimeZone() {
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+1"));
    }

    @Test
    public void testIsEnabledMapperFeature() {
        assertFalse(mapper.isEnabled(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
    }

    @Test
    public void testConfigureMapperFeature() {
        mapper.configure(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME, true);
        assertTrue(mapper.isEnabled(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
    }

    @Test
    public void testEnableMapperFeatures() {
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        assertTrue(mapper.isEnabled(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
    }

    @Test
    public void testDisableMapperFeatures() {
        mapper.disable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        assertFalse(mapper.isEnabled(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME));
    }

    @Test
    public void testIsEnabledSerializationFeature() {
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testConfigureSerializationFeature() {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testEnableSerializationFeature() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testEnableSerializationFeatures() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        assertTrue(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testDisableSerializationFeature() {
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testDisableSerializationFeatures() {
        mapper.disable(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testIsEnabledDeserializationFeature() {
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testConfigureDeserializationFeature() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testEnableDeserializationFeature() {
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testEnableDeserializationFeatures() {
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testDisableDeserializationFeature() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testDisableDeserializationFeatures() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testIsEnabledJsonParserFeature() {
        assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testConfigureJsonParserFeature() {
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testEnableJsonParserFeatures() {
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        assertTrue(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testDisableJsonParserFeatures() {
        mapper.disable(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testIsEnabledJsonGeneratorFeature() {
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

    @Test
    public void testConfigureJsonGeneratorFeature() {
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        assertTrue(mapper.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

    @Test
    public void testEnableJsonGeneratorFeatures() {
        mapper.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertTrue(mapper.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

    @Test
    public void testDisableJsonGeneratorFeatures() {
        mapper.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII));
    }

    @Test
    public void testIsEnabledJsonFactoryFeature() {
        assertFalse(mapper.isEnabled(JsonFactory.Feature.INTERN_FIELD_NAMES));
    }

    @Test
    public void testReadValueJsonParserClass() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("{\"id\":1,\"name\":\"test\"}");
        SimpleBean bean = mapper.readValue(jp, SimpleBean.class);
        assertEquals(1, bean.id);
        assertEquals("test", bean.name);
    }

    @Test
    public void testReadValueJsonParserTypeReference() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("{\"id\":1,\"name\":\"test\"}");
        SimpleBean bean = mapper.readValue(jp, new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueJsonParserResolvedType() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("{\"id\":1,\"name\":\"test\"}");
        JavaType type = mapper.constructType(SimpleBean.class);
        SimpleBean bean = mapper.readValue(jp, type);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueJsonParserJavaType() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("{\"id\":1,\"name\":\"test\"}");
        JavaType type = mapper.constructType(SimpleBean.class);
        SimpleBean bean = mapper.readValue(jp, type);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadTreeJsonParser() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("{\"id\":1}");
        JsonNode node = mapper.readTree(jp);
        assertNotNull(node);
        assertEquals(1, node.get("id").asInt());
    }

    @Test
    public void testReadTreeJsonParserNullToken() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("");
        assertNull(mapper.readTree(jp));
    }

    @Test
    public void testReadValuesJsonParserResolvedType() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("[{\"id\":1}]");
        jp.nextToken();
        MappingIterator<SimpleBean> it = mapper.readValues(jp, SimpleBean.class);
        assertTrue(it.hasNext());
        SimpleBean bean = it.next();
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValuesJsonParserJavaType() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("[{\"id\":1}]");
        jp.nextToken();
        MappingIterator<SimpleBean> it = mapper.readValues(jp, mapper.constructType(SimpleBean.class));
        assertTrue(it.hasNext());
        SimpleBean bean = it.next();
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValuesJsonParserClass() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("[{\"id\":1}]");
        jp.nextToken();
        MappingIterator<SimpleBean> it = mapper.readValues(jp, SimpleBean.class);
        assertTrue(it.hasNext());
        SimpleBean bean = it.next();
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValuesJsonParserTypeReference() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("[{\"id\":1}]");
        jp.nextToken();
        MappingIterator<SimpleBean> it = mapper.readValues(jp, new TypeReference<SimpleBean>() {});
        assertTrue(it.hasNext());
        SimpleBean bean = it.next();
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadTreeInputStream() throws Exception {
        InputStream in = new ByteArrayInputStream("{\"id\":1}".getBytes("UTF-8"));
        JsonNode node = mapper.readTree(in);
        assertEquals(1, node.get("id").asInt());
    }

    @Test
    public void testReadTreeReader() throws Exception {
        Reader r = new StringReader("{\"id\":1}");
        JsonNode node = mapper.readTree(r);
        assertEquals(1, node.get("id").asInt());
    }

    @Test
    public void testReadTreeString() throws Exception {
        JsonNode node = mapper.readTree("{\"id\":1}");
        assertEquals(1, node.get("id").asInt());
    }

    @Test
    public void testReadTreeBytes() throws Exception {
        JsonNode node = mapper.readTree("{\"id\":1}".getBytes("UTF-8"));
        assertEquals(1, node.get("id").asInt());
    }

    @Test
    public void testReadTreeFile() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        JsonNode node = mapper.readTree(f);
        assertEquals(1, node.get("id").asInt());
    }

    @Test
    public void testReadTreeURL() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        JsonNode node = mapper.readTree(f.toURI().toURL());
        assertEquals(1, node.get("id").asInt());
    }

    @Test
    public void testWriteValueJsonGenerator() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper.writeValue(g, new SimpleBean(1, "test"));
        g.close();
        assertEquals("{\"id\":1,\"name\":\"test\"}", sw.toString());
    }

    @Test
    public void testWriteValueFile() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(f, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testWriteValueOutputStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(out.toByteArray(), SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testWriteValueWriter() throws Exception {
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(sw.toString(), SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testWriteValueAsString() throws Exception {
        String json = mapper.writeValueAsString(new SimpleBean(1, "test"));
        assertEquals("{\"id\":1,\"name\":\"test\"}", json);
    }

    @Test
    public void testWriteValueAsBytes() throws Exception {
        byte[] bytes = mapper.writeValueAsBytes(new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(bytes, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testWriteTreeJsonGeneratorTreeNode() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        ObjectNode node = mapper.createObjectNode();
        node.put("id", 1);
        mapper.writeTree(g, (TreeNode) node);
        g.close();
        assertEquals("{\"id\":1}", sw.toString());
    }

    @Test
    public void testWriteTreeJsonGeneratorJsonNode() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        ObjectNode node = mapper.createObjectNode();
        node.put("id", 1);
        mapper.writeTree(g, node);
        g.close();
        assertEquals("{\"id\":1}", sw.toString());
    }

    @Test
    public void testCreateObjectNode() {
        ObjectNode node = mapper.createObjectNode();
        assertNotNull(node);
    }

    @Test
    public void testCreateArrayNode() {
        ArrayNode node = mapper.createArrayNode();
        assertNotNull(node);
    }

    @Test
    public void testTreeAsTokens() throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", 1);
        JsonParser jp = mapper.treeAsTokens(node);
        assertNotNull(jp);
        jp.close();
    }

    @Test
    public void testTreeToValue() throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", 1);
        node.put("name", "test");
        SimpleBean bean = mapper.treeToValue(node, SimpleBean.class);
        assertEquals(1, bean.id);
        assertEquals("test", bean.name);
    }

    @Test
    public void testTreeToValueAssignable() throws Exception {
        ObjectNode node = mapper.createObjectNode();
        JsonNode result = mapper.treeToValue(node, JsonNode.class);
        assertSame(node, result);
    }

    @Test
    public void testValueToTree() {
        SimpleBean bean = new SimpleBean(1, "test");
        JsonNode node = mapper.valueToTree(bean);
        assertEquals(1, node.get("id").asInt());
        assertEquals("test", node.get("name").asText());
    }

    @Test
    public void testValueToTreeNull() {
        assertNull(mapper.valueToTree(null));
    }

    @Test
    public void testCanSerialize() {
        assertTrue(mapper.canSerialize(SimpleBean.class));
    }

    @Test
    public void testCanSerializeWithCause() {
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(SimpleBean.class, cause));
    }

    @Test
    public void testCanDeserialize() {
        assertTrue(mapper.canDeserialize(mapper.constructType(SimpleBean.class)));
    }

    @Test
    public void testCanDeserializeWithCause() {
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canDeserialize(mapper.constructType(SimpleBean.class), cause));
    }

    @Test
    public void testReadValueFileClass() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(f, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueFileTypeReference() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(f, new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueFileJavaType() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(f, mapper.constructType(SimpleBean.class));
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueURLClass() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(f.toURI().toURL(), SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueURLTypeReference() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(f.toURI().toURL(), new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueURLJavaType() throws Exception {
        File f = tempFolder.newFile("test.json");
        mapper.writeValue(f, new SimpleBean(1, "test"));
        SimpleBean bean = mapper.readValue(f.toURI().toURL(), mapper.constructType(SimpleBean.class));
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueStringClass() throws Exception {
        SimpleBean bean = mapper.readValue("{\"id\":1,\"name\":\"test\"}", SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueStringTypeReference() throws Exception {
        SimpleBean bean = mapper.readValue("{\"id\":1,\"name\":\"test\"}", new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueStringJavaType() throws Exception {
        SimpleBean bean = mapper.readValue("{\"id\":1,\"name\":\"test\"}", mapper.constructType(SimpleBean.class));
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueReaderClass() throws Exception {
        Reader r = new StringReader("{\"id\":1,\"name\":\"test\"}");
        SimpleBean bean = mapper.readValue(r, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueReaderTypeReference() throws Exception {
        Reader r = new StringReader("{\"id\":1,\"name\":\"test\"}");
        SimpleBean bean = mapper.readValue(r, new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueReaderJavaType() throws Exception {
        Reader r = new StringReader("{\"id\":1,\"name\":\"test\"}");
        SimpleBean bean = mapper.readValue(r, mapper.constructType(SimpleBean.class));
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueInputStreamClass() throws Exception {
        InputStream in = new ByteArrayInputStream("{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8"));
        SimpleBean bean = mapper.readValue(in, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueInputStreamTypeReference() throws Exception {
        InputStream in = new ByteArrayInputStream("{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8"));
        SimpleBean bean = mapper.readValue(in, new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueInputStreamJavaType() throws Exception {
        InputStream in = new ByteArrayInputStream("{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8"));
        SimpleBean bean = mapper.readValue(in, mapper.constructType(SimpleBean.class));
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueBytesClass() throws Exception {
        byte[] bytes = "{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8");
        SimpleBean bean = mapper.readValue(bytes, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueBytesOffsetLengthClass() throws Exception {
        byte[] bytes = "{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8");
        SimpleBean bean = mapper.readValue(bytes, 0, bytes.length, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueBytesTypeReference() throws Exception {
        byte[] bytes = "{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8");
        SimpleBean bean = mapper.readValue(bytes, new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueBytesOffsetLengthTypeReference() throws Exception {
        byte[] bytes = "{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8");
        SimpleBean bean = mapper.readValue(bytes, 0, bytes.length, new TypeReference<SimpleBean>() {});
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueBytesJavaType() throws Exception {
        byte[] bytes = "{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8");
        SimpleBean bean = mapper.readValue(bytes, mapper.constructType(SimpleBean.class));
        assertEquals(1, bean.id);
    }

    @Test
    public void testReadValueBytesOffsetLengthJavaType() throws Exception {
        byte[] bytes = "{\"id\":1,\"name\":\"test\"}".getBytes("UTF-8");
        SimpleBean bean = mapper.readValue(bytes, 0, bytes.length, mapper.constructType(SimpleBean.class));
        assertEquals(1, bean.id);
    }

    @Test
    public void testWriter() {
        assertNotNull(mapper.writer());
    }

    @Test
    public void testWriterSerializationFeature() {
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testWriterSerializationFeatures() {
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void testWriterDateFormat() {
        assertNotNull(mapper.writer(new SimpleDateFormat("yyyy-MM-dd")));
    }

    @Test
    public void testWriterWithView() {
        assertNotNull(mapper.writerWithView(Object.class));
    }

    @Test
    public void testWriterForClass() {
        assertNotNull(mapper.writerFor(SimpleBean.class));
    }

    @Test
    public void testWriterForTypeReference() {
        assertNotNull(mapper.writerFor(new TypeReference<SimpleBean>() {}));
    }

    @Test
    public void testWriterForJavaType() {
        assertNotNull(mapper.writerFor(mapper.constructType(SimpleBean.class)));
    }

    @Test
    public void testWriterPrettyPrinter() {
        assertNotNull(mapper.writer(new DefaultPrettyPrinter()));
    }

    @Test
    public void testWriterWithDefaultPrettyPrinter() {
        assertNotNull(mapper.writerWithDefaultPrettyPrinter());
    }

    @Test
    public void testWriterFilterProvider() {
        assertNotNull(mapper.writer(new SimpleFilterProvider()));
    }

    @Test
    public void testWriterFormatSchema() {
        assertNotNull(mapper.writer((FormatSchema) null));
    }

    @Test
    public void testWriterBase64Variant() {
        assertNotNull(mapper.writer(Base64Variants.MIME));
    }

    @Test
    public void testWriterCharacterEscapes() {
        assertNotNull(mapper.writer(new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() { return standardAsciiEscapesForJSON(); }
            @Override
            public SerializableString getEscapeSequence(int ch) { return null; }
        }));
    }

    @Test
    public void testWriterContextAttributes() {
        assertNotNull(mapper.writer(ContextAttributes.getEmpty()));
    }

    @Test
    public void testReader() {
        assertNotNull(mapper.reader());
    }

    @Test
    public void testReaderDeserializationFeature() {
        assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testReaderDeserializationFeatures() {
        assertNotNull(mapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test
    public void testReaderForUpdating() throws Exception {
        SimpleBean bean = new SimpleBean(0, "old");
        ObjectReader reader = mapper.readerForUpdating(bean);
        SimpleBean result = reader.readValue("{\"id\":1,\"name\":\"new\"}");
        assertSame(bean, result);
        assertEquals(1, bean.id);
        assertEquals("new", bean.name);
    }

    @Test
    public void testReaderForJavaType() {
        assertNotNull(mapper.readerFor(mapper.constructType(SimpleBean.class)));
    }

    @Test
    public void testReaderForClass() {
        assertNotNull(mapper.readerFor(SimpleBean.class));
    }

    @Test
    public void testReaderForTypeReference() {
        assertNotNull(mapper.readerFor(new TypeReference<SimpleBean>() {}));
    }

    @Test
    public void testReaderJsonNodeFactory() {
        assertNotNull(mapper.reader(JsonNodeFactory.instance));
    }

    @Test
    public void testReaderFormatSchema() {
        assertNotNull(mapper.reader((FormatSchema) null));
    }

    @Test
    public void testReaderInjectableValues() {
        assertNotNull(mapper.reader(new InjectableValues.Std()));
    }

    @Test
    public void testReaderWithView() {
        assertNotNull(mapper.readerWithView(Object.class));
    }

    @Test
    public void testReaderBase64Variant() {
        assertNotNull(mapper.reader(Base64Variants.MIME));
    }

    @Test
    public void testReaderContextAttributes() {
        assertNotNull(mapper.reader(ContextAttributes.getEmpty()));
    }

    @Test
    public void testConvertValueClass() {
        SimpleBean bean = new SimpleBean(1, "test");
        Map<?,?> map = mapper.convertValue(bean, Map.class);
        assertEquals(1, map.get("id"));
        assertEquals("test", map.get("name"));
    }

    @Test
    public void testConvertValueTypeReference() {
        SimpleBean bean = new SimpleBean(1, "test");
        Map<String,Object> map = mapper.convertValue(bean, new TypeReference<Map<String,Object>>() {});
        assertEquals(1, map.get("id"));
    }

    @Test
    public void testConvertValueJavaType() {
        SimpleBean bean = new SimpleBean(1, "test");
        Map<String,Object> map = mapper.convertValue(bean, mapper.constructType(Map.class));
        assertEquals(1, map.get("id"));
    }

    @Test
    public void testConvertValueNull() {
        assertNull(mapper.convertValue(null, SimpleBean.class));
    }

    @Test
    public void testConvertValueAssignable() {
        SimpleBean bean = new SimpleBean(1, "test");
        assertSame(bean, mapper.convertValue(bean, SimpleBean.class));
    }

    @Test
    public void testGenerateJsonSchema() throws Exception {
        assertNotNull(mapper.generateJsonSchema(SimpleBean.class));
    }

    @Test
    public void testAcceptJsonFormatVisitorClass() throws Exception {
        JsonFormatVisitorWrapper visitor = mock(JsonFormatVisitorWrapper.class);
        mapper.acceptJsonFormatVisitor(SimpleBean.class, visitor);
    }

    @Test
    public void testAcceptJsonFormatVisitorJavaType() throws Exception {
        JsonFormatVisitorWrapper visitor = mock(JsonFormatVisitorWrapper.class);
        mapper.acceptJsonFormatVisitor(mapper.constructType(SimpleBean.class), visitor);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptJsonFormatVisitorNullType() throws Exception {
        mapper.acceptJsonFormatVisitor((JavaType) null, mock(JsonFormatVisitorWrapper.class));
    }

    @Test
    public void testVersion() {
        assertNotNull(mapper.version());
    }

    @Test
    public void testGetFactory() {
        assertNotNull(mapper.getFactory());
    }

    @Test
    public void testGetJsonFactory() {
        assertNotNull(mapper.getJsonFactory());
    }

    @Test
    public void testDefaultTypingResolverUseForType() {
        ObjectMapper.DefaultTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
        assertTrue(builder.useForType(TypeFactory.defaultInstance().constructType(Object.class)));
        assertFalse(builder.useForType(TypeFactory.defaultInstance().constructType(String.class)));
    }

    @Test
    public void testDefaultTypingResolverUseForTypeObjectAndNonConcrete() {
        ObjectMapper.DefaultTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        assertTrue(builder.useForType(TypeFactory.defaultInstance().constructType(Object.class)));
        assertTrue(builder.useForType(TypeFactory.defaultInstance().constructType(Number.class)));
        assertFalse(builder.useForType(TypeFactory.defaultInstance().constructType(Integer.class)));
    }

    @Test
    public void testDefaultTypingResolverUseForTypeNonConcreteAndArrays() {
        ObjectMapper.DefaultTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        assertTrue(builder.useForType(TypeFactory.defaultInstance().constructType(Object.class)));
        assertTrue(builder.useForType(TypeFactory.defaultInstance().constructType(Number.class)));
        assertFalse(builder.useForType(TypeFactory.defaultInstance().constructType(Integer.class)));
        assertTrue(builder.useForType(TypeFactory.defaultInstance().constructArrayType(Object.class)));
    }

    @Test
    public void testDefaultTypingResolverUseForTypeNonFinal() {
        ObjectMapper.DefaultTypeResolverBuilder builder = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
        assertTrue(builder.useForType(TypeFactory.defaultInstance().constructType(Object.class)));
        assertFalse(builder.useForType(TypeFactory.defaultInstance().constructType(String.class)));
        assertFalse(builder.useForType(TypeFactory.defaultInstance().constructType(Integer.class)));
    }

    @Test
    public void testWriteValueCloseable() throws Exception {
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableBean cb = new CloseableBean();
        String json = mapper.writeValueAsString(cb);
        assertTrue(cb.closed);
    }

    public static class CloseableBean implements Closeable {
        public boolean closed = false;
        public int id = 1;
        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    @Test
    public void testReadValueNullToken() throws Exception {
        String json = "null";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNull(bean);
    }

    @Test
    public void testReadValueEmptyArray() throws Exception {
        String json = "[]";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertNull(bean);
    }

    @Test(expected = JsonMappingException.class)
    public void testReadValueNoContent() throws Exception {
        mapper.readValue("", SimpleBean.class);
    }

    @Test
    public void testRootWrapping() throws Exception {
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        String json = mapper.writeValueAsString(new SimpleBean(1, "test"));
        assertTrue(json.contains("SimpleBean"));
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testRootWrappingMismatch() throws Exception {
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        String json = "{\"WrongName\":{\"id\":1,\"name\":\"test\"}}";
        try {
            mapper.readValue(json, SimpleBean.class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testDuplicateModuleRegistrationIgnored() throws Exception {
        mapper.enable(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS);
        Module mod = mock(Module.class);
        when(mod.getModuleName()).thenReturn("test");
        when(mod.version()).thenReturn(Version.unknownVersion());
        when(mod.getTypeId()).thenReturn("testId");
        mapper.registerModule(mod);
        mapper.registerModule(mod);
        verify(mod, times(1)).setupModule(any(Module.SetupContext.class));
    }

    @Test
    public void testVerifySchemaType() {
        FormatSchema schema = mock(FormatSchema.class);
        when(mapper.getFactory().canUseSchema(schema)).thenReturn(false);
        try {
            mapper.writer(schema);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testFindRootDeserializerCaching() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser1 = mapper._findRootDeserializer(mapper.createDeserializationContext(null, mapper.getDeserializationConfig()), type);
        JsonDeserializer<Object> deser2 = mapper._findRootDeserializer(mapper.createDeserializationContext(null, mapper.getDeserializationConfig()), type);
        assertSame(deser1, deser2);
    }

    @Test
    public void testInitForReadingNoContent() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("");
        try {
            mapper._initForReading(jp);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testUnwrapAndDeserializeMismatch() throws Exception {
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        JsonParser jp = mapper.getFactory().createParser("{\"SimpleBean\":{\"id\":1}}");
        jp.nextToken();
        DeserializationContext ctxt = mapper.createDeserializationContext(jp, mapper.getDeserializationConfig());
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = mapper._findRootDeserializer(ctxt, type);
        try {
            mapper._unwrapAndDeserialize(jp, ctxt, mapper.getDeserializationConfig(), type, deser);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testConfigAndWriteValueCloseable() throws Exception {
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableBean cb = new CloseableBean();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, cb);
        assertTrue(cb.closed);
    }

    @Test
    public void testWriteCloseableValue() throws Exception {
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableBean cb = new CloseableBean();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper.writeValue(g, cb);
        g.close();
        assertTrue(cb.closed);
    }

    @Test
    public void testSerializerProvider() {
        DefaultSerializerProvider sp = mapper._serializerProvider(mapper.getSerializationConfig());
        assertNotNull(sp);
    }

    @Test
    public void testCreateDeserializationContext() {
        DeserializationContext ctxt = mapper.createDeserializationContext(null, mapper.getDeserializationConfig());
        assertNotNull(ctxt);
    }

    @Test
    public void testReadMapAndClose() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("{\"id\":1}");
        Object result = mapper._readMapAndClose(jp, mapper.constructType(SimpleBean.class));
        assertTrue(result instanceof SimpleBean);
        assertEquals(1, ((SimpleBean)result).id);
    }

    @Test
    public void testReadMapAndCloseNull() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("null");
        Object result = mapper._readMapAndClose(jp, mapper.constructType(SimpleBean.class));
        assertNull(result);
    }

    @Test
    public void testReadMapAndCloseEmpty() throws Exception {
        JsonParser jp = mapper.getFactory().createParser("[]");
        Object result = mapper._readMapAndClose(jp, mapper.constructType(SimpleBean.class));
        assertNull(result);
    }

    @Test
    public void testReadValueWithRootWrapping() throws Exception {
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        JsonParser jp = mapper.getFactory().createParser("{\"SimpleBean\":{\"id\":1,\"name\":\"test\"}}");
        SimpleBean bean = mapper.readValue(jp, SimpleBean.class);
        assertEquals(1, bean.id);
    }

    @Test
    public void testConfigAndWriteValueWithView() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper._configAndWriteValue(g, new SimpleBean(1, "test"), Object.class);
        g.close();
        assertNotNull(sw.toString());
    }

    @Test
    public void testConfigAndWriteCloseable() throws Exception {
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        CloseableBean cb = new CloseableBean();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper._configAndWriteCloseable(g, cb, mapper.getSerializationConfig());
        assertTrue(cb.closed);
    }

    @Test
    public void testWriteCloseableValueWithFlush() throws Exception {
        mapper.enable(SerializationFeature.CLOSE_CLOSEABLE);
        mapper.enable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        CloseableBean cb = new CloseableBean();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        mapper._writeCloseableValue(g, cb, mapper.getSerializationConfig());
        assertTrue(cb.closed);
    }

    @Test
    public void testDeprecatedSetFilters() {
        mapper.setFilters(new SimpleFilterProvider());
    }

    @Test
    public void testDeprecatedSetMixInAnnotations() {
        Map<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
        mixins.put(SimpleBean.class, Object.class);
        mapper.setMixInAnnotations(mixins);
    }

    @Test
    public void testDeprecatedAddMixInAnnotations() {
        mapper.addMixInAnnotations(SimpleBean.class, Object.class);
    }

    @Test
    public void testDeprecatedWriterWithTypeClass() {
        assertNotNull(mapper.writerWithType(SimpleBean.class));
    }

    @Test
    public void testDeprecatedWriterWithTypeTypeReference() {
        assertNotNull(mapper.writerWithType(new TypeReference<SimpleBean>() {}));
    }

    @Test
    public void testDeprecatedWriterWithTypeJavaType() {
        assertNotNull(mapper.writerWithType(mapper.constructType(SimpleBean.class)));
    }

    @Test
    public void testDeprecatedReaderJavaType() {
        assertNotNull(mapper.reader(mapper.constructType(SimpleBean.class)));
    }

    @Test
    public void testDeprecatedReaderClass() {
        assertNotNull(mapper.reader(SimpleBean.class));
    }

    @Test
    public void testDeprecatedReaderTypeReference() {
        assertNotNull(mapper.reader(new TypeReference<SimpleBean>() {}));
    }

    @Test
    public void testDeprecatedDefaultPrettyPrinter() {
        assertNotNull(mapper._defaultPrettyPrinter());
    }

    @Test
    public void testDeprecatedGenerateJsonSchema() throws Exception {
        assertNotNull(mapper.generateJsonSchema(SimpleBean.class));
    }

    @Test
    public void testDeprecatedSetVisibilityChecker() {
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance());
    }

    @Test
    public void testDeprecatedGetJsonFactory() {
        assertNotNull(mapper.getJsonFactory());
    }
}
