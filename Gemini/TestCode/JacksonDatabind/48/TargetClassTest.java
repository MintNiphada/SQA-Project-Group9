package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.FormatFeature;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.LinkedNode;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DeserializationConfigTest {

    private enum DummyFormatFeature implements FormatFeature {
        FEAT_1(true),
        FEAT_2(false);

        private final boolean _defaultState;
        private final int _mask;

        DummyFormatFeature(boolean defaultState) {
            _defaultState = defaultState;
            _mask = (1 << ordinal());
        }

        @Override
        public boolean enabledByDefault() {
            return _defaultState;
        }

        @Override
        public int getMask() {
            return _mask;
        }

        @Override
        public boolean enabledIn(int flags) {
            return (flags & _mask) != 0;
        }
    }

    private ObjectMapper _mapper;
    private DeserializationConfig _config;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getDeserializationConfig();
    }

    @Test
    public void testConstructorsAndBasics() throws Exception {
        BaseSettings base = _config.getBaseSettings();
        SubtypeResolver str = new StdSubtypeResolver();
        SimpleMixInResolver mixins = new SimpleMixInResolver(null);
        RootNameLookup rnl = new RootNameLookup();

        DeserializationConfig cfg = new DeserializationConfig(base, str, mixins, rnl);
        assertNotNull(cfg.getNodeFactory());
        assertNull(cfg.getProblemHandlers());
        assertEquals(0, cfg._parserFeatures);
        assertEquals(0, cfg._parserFeaturesToChange);
        assertEquals(0, cfg._formatReadFeatures);
        assertEquals(0, cfg._formatReadFeaturesToChange);
        assertNotNull(cfg.getBaseSettings());

        DeserializationConfig cfgCopy = new DeserializationConfig(cfg, mixins, rnl);
        assertNotNull(cfgCopy.getNodeFactory());

        DeserializationConfig cfgMixins = new DeserializationConfig(cfg, mixins);
        assertNotNull(cfgMixins);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(_config);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        DeserializationConfig deserialized = (DeserializationConfig) ois.readObject();
        assertNotNull(deserialized);
    }

    @Test
    public void testMapperFeatureOperations() {
        DeserializationConfig cfg = _config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        assertTrue(cfg.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertSame(cfg, cfg.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));

        DeserializationConfig cfg2 = cfg.without(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        assertFalse(cfg2.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertSame(cfg2, cfg2.without(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));

        DeserializationConfig cfg3 = _config.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        assertTrue(cfg3.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertSame(cfg3, cfg3.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true));

        DeserializationConfig cfg4 = cfg3.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false);
        assertFalse(cfg4.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES));
        assertSame(cfg4, cfg4.with(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false));
    }

    @Test
    public void testDeserializationFeaturesOperations() {
        DeserializationConfig cfg = _config.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertTrue(cfg.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertSame(cfg, cfg.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        DeserializationConfig cfg2 = cfg.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        assertFalse(cfg2.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertSame(cfg2, cfg2.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        DeserializationConfig cfg3 = _config.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertTrue(cfg3.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertTrue(cfg3.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        assertSame(cfg3, cfg3.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        DeserializationConfig cfg4 = cfg3.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertFalse(cfg4.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(cfg4.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        assertSame(cfg4, cfg4.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        DeserializationConfig cfg5 = _config.withFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertTrue(cfg5.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertTrue(cfg5.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        assertSame(cfg5, cfg5.withFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        DeserializationConfig cfg6 = cfg5.withoutFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        assertFalse(cfg6.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        assertFalse(cfg6.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        assertSame(cfg6, cfg6.withoutFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        int mask = DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask() | DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES.getMask();
        assertTrue(cfg5.hasSomeOfFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask()));
        assertFalse(cfg5.hasSomeOfFeatures(0));
        assertTrue(cfg5.getDeserializationFeatures() != 0);

        DeserializationConfig allSet = cfg5.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        assertTrue(allSet.hasDeserializationFeatures(mask));
        assertFalse(cfg6.without(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES).hasDeserializationFeatures(mask));
    }

    @Test
    public void testParserFeatures() {
        DeserializationConfig cfg = _config.with(JsonParser.Feature.ALLOW_COMMENTS);
        assertSame(cfg, cfg.with(JsonParser.Feature.ALLOW_COMMENTS));

        JsonFactory factory = new JsonFactory();
        assertTrue(cfg.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, factory));
        assertFalse(_config.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, factory));

        DeserializationConfig cfg2 = cfg.without(JsonParser.Feature.ALLOW_COMMENTS);
        assertSame(cfg2, cfg2.without(JsonParser.Feature.ALLOW_COMMENTS));
        assertFalse(cfg2.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, factory));

        DeserializationConfig cfg3 = _config.withFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS);
        assertSame(cfg3, cfg3.withFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS));
        assertTrue(cfg3.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, factory));
        assertTrue(cfg3.isEnabled(JsonParser.Feature.ALLOW_YAML_COMMENTS, factory));

        DeserializationConfig cfg4 = cfg3.withoutFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS);
        assertSame(cfg4, cfg4.withoutFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS));
        assertFalse(cfg4.isEnabled(JsonParser.Feature.ALLOW_COMMENTS, factory));
        assertFalse(cfg4.isEnabled(JsonParser.Feature.ALLOW_YAML_COMMENTS, factory));
    }

    @Test
    public void testFormatFeatures() {
        DeserializationConfig cfg = _config.with(DummyFormatFeature.FEAT_1);
        assertSame(cfg, cfg.with(DummyFormatFeature.FEAT_1));

        DeserializationConfig cfg2 = cfg.without(DummyFormatFeature.FEAT_1);
        assertSame(cfg2, cfg2.without(DummyFormatFeature.FEAT_1));

        DeserializationConfig cfg3 = _config.withFeatures(DummyFormatFeature.FEAT_1, DummyFormatFeature.FEAT_2);
        assertSame(cfg3, cfg3.withFeatures(DummyFormatFeature.FEAT_1, DummyFormatFeature.FEAT_2));

        DeserializationConfig cfg4 = cfg3.withoutFeatures(DummyFormatFeature.FEAT_1, DummyFormatFeature.FEAT_2);
        assertSame(cfg4, cfg4.withoutFeatures(DummyFormatFeature.FEAT_1, DummyFormatFeature.FEAT_2));
    }

    @Test
    public void testWithBaseSettingsMutators() {
        ClassIntrospector ci = _config.getClassIntrospector();
        assertSame(_config, _config.with(ci));
        assertNotNull(_config.with(ci.forClassAnnotations(_config, _config.constructType(String.class), _config).getClassInfo()));

        JacksonAnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        assertSame(_config, _config.with((AnnotationIntrospector) _config.getAnnotationIntrospector()));
        assertNotNull(_config.with((AnnotationIntrospector) ai));

        VisibilityChecker<?> vc = _config.getDefaultVisibilityChecker();
        assertSame(_config, _config.with(vc));
        assertNotNull(_config.with(vc.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)));

        assertNotNull(_config.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC));

        TypeResolverBuilder<?> trb = new StdTypeResolverBuilder();
        assertNotNull(_config.with(trb));

        SubtypeResolver str = _config.getSubtypeResolver();
        assertSame(_config, _config.with(str));
        assertNotNull(_config.with(new StdSubtypeResolver()));

        PropertyNamingStrategy pns = PropertyNamingStrategy.SNAKE_CASE;
        assertNotNull(_config.with(pns));

        TypeFactory tf = _config.getTypeFactory();
        assertSame(_config, _config.with(tf));
        assertNotNull(_config.with(TypeFactory.defaultInstance().withModifier(null)));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        assertNotNull(_config.with(df));

        HandlerInstantiator hi = null;
        assertSame(_config, _config.with(hi));

        assertNotNull(_config.withInsertedAnnotationIntrospector(ai));
        assertNotNull(_config.withAppendedAnnotationIntrospector(ai));

        Class<?> view = String.class;
        DeserializationConfig withView = _config.withView(view);
        assertNotSame(_config, withView);
        assertSame(withView, withView.withView(view));

        Locale loc = Locale.GERMANY;
        assertNotNull(_config.with(loc));

        TimeZone tz = TimeZone.getTimeZone("GMT+2");
        assertNotNull(_config.with(tz));

        assertNotNull(_config.with(Base64Variants.MODIFIED_FOR_URL));

        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k", "v");
        DeserializationConfig withAttrs = _config.with(attrs);
        assertNotSame(_config, withAttrs);
        assertSame(withAttrs, withAttrs.with(attrs));
    }

    @Test
    public void testWithRootName() {
        assertSame(_config, _config.withRootName((PropertyName) null));

        PropertyName pn = new PropertyName("root");
        DeserializationConfig cfg1 = _config.withRootName(pn);
        assertNotSame(_config, cfg1);
        assertSame(cfg1, cfg1.withRootName(pn));

        DeserializationConfig cfg2 = cfg1.withRootName(PropertyName.construct("root"));
        assertSame(cfg1, cfg2);

        DeserializationConfig cfg3 = cfg1.withRootName((PropertyName) null);
        assertNotSame(cfg1, cfg3);
    }

    @Test
    public void testWithNodeFactory() {
        JsonNodeFactory nf = JsonNodeFactory.instance;
        assertSame(_config, _config.with(nf));

        JsonNodeFactory customNf = new JsonNodeFactory(true);
        DeserializationConfig cfg = _config.with(customNf);
        assertNotSame(_config, cfg);
        assertSame(customNf, cfg.getNodeFactory());
    }

    @Test
    public void testProblemHandlers() {
        DeserializationProblemHandler handler1 = new DeserializationProblemHandler() {};
        DeserializationProblemHandler handler2 = new DeserializationProblemHandler() {};

        assertSame(_config, _config.withNoProblemHandlers());

        DeserializationConfig cfg1 = _config.withHandler(handler1);
        assertNotSame(_config, cfg1);
        assertSame(cfg1, cfg1.withHandler(handler1));

        DeserializationConfig cfg2 = cfg1.withHandler(handler2);
        assertNotSame(cfg1, cfg2);
        LinkedNode<DeserializationProblemHandler> handlers = cfg2.getProblemHandlers();
        assertNotNull(handlers);
        assertSame(handler2, handlers.value());
        assertNotNull(handlers.next());
        assertSame(handler1, handlers.next().value());

        DeserializationConfig cfgCleared = cfg2.withNoProblemHandlers();
        assertNull(cfgCleared.getProblemHandlers());
    }

    @Test
    public void testInitializeJsonParser() throws Exception {
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(new byte[0]);

        _config.initialize(p);

        DeserializationConfig cfg = _config.with(JsonParser.Feature.ALLOW_COMMENTS)
                .with(DummyFormatFeature.FEAT_1);
        cfg.initialize(p);
    }

    @Test
    public void testAnnotationIntrospectorAndVisibilityChecker() {
        DeserializationConfig cfgWithAnno = _config.with(MapperFeature.USE_ANNOTATIONS, true);
        assertNotNull(cfgWithAnno.getAnnotationIntrospector());
        assertNotSame(NopAnnotationIntrospector.instance, cfgWithAnno.getAnnotationIntrospector());

        DeserializationConfig cfgNoAnno = _config.without(MapperFeature.USE_ANNOTATIONS);
        assertSame(NopAnnotationIntrospector.instance, cfgNoAnno.getAnnotationIntrospector());

        DeserializationConfig cfgNoDetect = _config
                .without(MapperFeature.AUTO_DETECT_SETTERS)
                .without(MapperFeature.AUTO_DETECT_CREATORS)
                .without(MapperFeature.AUTO_DETECT_FIELDS);
        VisibilityChecker<?> vc = cfgNoDetect.getDefaultVisibilityChecker();
        assertNotNull(vc);
    }

    @Test
    public void testIntrospectionMethods() {
        JavaType type = _config.constructType(String.class);
        BeanDescription bd1 = _config.introspectClassAnnotations(type);
        assertNotNull(bd1);

        BeanDescription bd2 = _config.introspectDirectClassAnnotations(type);
        assertNotNull(bd2);

        BeanDescription bd3 = _config.introspect(type);
        assertNotNull(bd3);

        BeanDescription bd4 = _config.introspectForCreation(type);
        assertNotNull(bd4);

        BeanDescription bd5 = _config.introspectForBuilder(type);
        assertNotNull(bd5);

        assertEquals(JsonInclude.Value.empty(), _config.getDefaultPropertyInclusion());
        assertEquals(JsonInclude.Value.empty(), _config.getDefaultPropertyInclusion(String.class));
        assertEquals(JsonFormat.Value.empty(), _config.getDefaultPropertyFormat(String.class));
    }

    @Test
    public void testUseRootWrapping() {
        assertFalse(_config.without(DeserializationFeature.UNWRAP_ROOT_VALUE).useRootWrapping());
        assertTrue(_config.with(DeserializationFeature.UNWRAP_ROOT_VALUE).useRootWrapping());

        DeserializationConfig namedEmpty = _config.withRootName(PropertyName.construct(""));
        assertFalse(namedEmpty.useRootWrapping());

        DeserializationConfig namedNotEmpty = _config.withRootName(PropertyName.construct("myRoot"));
        assertTrue(namedNotEmpty.useRootWrapping());
    }

    @Test
    public void testFindTypeDeserializer() throws Exception {
        JavaType stringType = _config.constructType(String.class);
        TypeDeserializer td = _config.findTypeDeserializer(stringType);
        assertNull(td);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        DeserializationConfig defaultTypingConfig = mapper.getDeserializationConfig();
        TypeDeserializer tdDefault = defaultTypingConfig.findTypeDeserializer(defaultTypingConfig.constructType(Object.class));
        assertNotNull(tdDefault);
    }
}
