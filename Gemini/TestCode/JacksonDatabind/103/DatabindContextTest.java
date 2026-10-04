package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DatabindContextTest {

    private static class DummyContext extends DatabindContext {
        private final ObjectMapper mapper = new ObjectMapper();
        private HandlerInstantiator handlerInstantiator;

        public void setHandlerInstantiator(HandlerInstantiator hi) {
            this.handlerInstantiator = hi;
        }

        @Override
        public MapperConfig<?> getConfig() {
            if (handlerInstantiator != null) {
                return mapper.getDeserializationConfig().with(handlerInstantiator);
            }
            return mapper.getDeserializationConfig();
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() {
            return getConfig().getAnnotationIntrospector();
        }

        @Override
        public boolean isEnabled(MapperFeature feature) {
            return getConfig().isEnabled(feature);
        }

        @Override
        public boolean canOverrideAccessModifiers() {
            return getConfig().canOverrideAccessModifiers();
        }

        @Override
        public Class<?> getActiveView() {
            return null;
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }

        @Override
        public TimeZone getTimeZone() {
            return TimeZone.getDefault();
        }

        @Override
        public JsonFormat.Value getDefaultPropertyFormat(Class<?> baseType) {
            return JsonFormat.Value.empty();
        }

        @Override
        public Object getAttribute(Object key) {
            return null;
        }

        @Override
        public DatabindContext setAttribute(Object key, Object value) {
            return this;
        }

        @Override
        protected JsonMappingException invalidTypeIdException(JavaType baseType, String typeId, String extraDesc) {
            return new JsonMappingException(null, "Invalid type id: " + typeId + " (" + extraDesc + ")");
        }

        @Override
        public TypeFactory getTypeFactory() {
            return mapper.getTypeFactory();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T reportBadDefinition(JavaType type, String msg) throws JsonMappingException {
            throw new JsonMappingException(null, "Bad definition: " + msg);
        }
    }

    public static class StringToNumberConverter implements Converter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.parseInt(value);
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(String.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Integer.class);
        }
    }

    private DummyContext context;

    @Before
    public void setUp() {
        context = new DummyContext();
    }

    @Test
    public void testConstructType() {
        Assert.assertNull(context.constructType((Type) null));
        JavaType type = context.constructType(String.class);
        Assert.assertNotNull(type);
        Assert.assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testConstructSpecializedType() {
        JavaType baseType = context.constructType(Number.class);
        JavaType sameType = context.constructSpecializedType(baseType, Number.class);
        Assert.assertSame(baseType, sameType);

        JavaType subType = context.constructSpecializedType(baseType, Integer.class);
        Assert.assertEquals(Integer.class, subType.getRawClass());
    }

    @Test
    public void testResolveSubTypeCanonical() throws JsonMappingException {
        JavaType baseList = context.constructType(List.class);
        JavaType resolved = context.resolveSubType(baseList, "java.util.ArrayList<java.lang.String>");
        Assert.assertNotNull(resolved);
        Assert.assertEquals(ArrayList.class, resolved.getRawClass());
        Assert.assertEquals(String.class, resolved.getContentType().getRawClass());
    }

    @Test(expected = JsonMappingException.class)
    public void testResolveSubTypeCanonicalMismatch() throws JsonMappingException {
        JavaType baseType = context.constructType(Number.class);
        context.resolveSubType(baseType, "java.util.ArrayList<java.lang.String>");
    }

    @Test
    public void testResolveSubTypeSimple() throws JsonMappingException {
        JavaType baseType = context.constructType(Number.class);
        JavaType resolved = context.resolveSubType(baseType, "java.lang.Integer");
        Assert.assertNotNull(resolved);
        Assert.assertEquals(Integer.class, resolved.getRawClass());
    }

    @Test
    public void testResolveSubTypeClassNotFound() throws JsonMappingException {
        JavaType baseType = context.constructType(Number.class);
        JavaType resolved = context.resolveSubType(baseType, "com.nonexistent.NoSuchClass");
        Assert.assertNull(resolved);
    }

    @Test(expected = JsonMappingException.class)
    public void testResolveSubTypeNotSubtype() throws JsonMappingException {
        JavaType baseType = context.constructType(Number.class);
        context.resolveSubType(baseType, "java.lang.String");
    }

    @Test(expected = JsonMappingException.class)
    public void testResolveSubTypeIllegalName() throws JsonMappingException {
        JavaType baseType = context.constructType(Number.class);
        context.resolveSubType(baseType, ";;invalid class name");
    }

    @Test
    public void testObjectIdGeneratorInstanceDefault() throws JsonMappingException {
        ObjectIdInfo info = new ObjectIdInfo(PropertyName.construct("id"), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdGenerator<?> gen = context.objectIdGeneratorInstance(null, info);
        Assert.assertNotNull(gen);
        Assert.assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, gen.getClass());
        Assert.assertEquals(Object.class, gen.getScope());
    }

    @Test
    public void testObjectIdGeneratorInstanceWithHandlerInstantiator() throws JsonMappingException {
        final ObjectIdGenerator<?> customGen = new ObjectIdGenerators.IntSequenceGenerator();
        context.setHandlerInstantiator(new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
                return null;
            }

            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
                return null;
            }

            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
                return null;
            }

            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
                return null;
            }

            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
                return null;
            }

            @Override
            public ObjectIdGenerator<?> objectIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return customGen;
            }

            @Override
            public ObjectIdResolver resolverIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return null;
            }
        });

        ObjectIdInfo info = new ObjectIdInfo(PropertyName.construct("id"), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdGenerator<?> gen = context.objectIdGeneratorInstance(null, info);
        Assert.assertNotNull(gen);
    }

    @Test
    public void testObjectIdResolverInstanceDefault() {
        ObjectIdInfo info = new ObjectIdInfo(PropertyName.construct("id"), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdResolver resolver = context.objectIdResolverInstance(null, info);
        Assert.assertNotNull(resolver);
        Assert.assertEquals(SimpleObjectIdResolver.class, resolver.getClass());
    }

    @Test
    public void testObjectIdResolverInstanceWithHandlerInstantiator() {
        final ObjectIdResolver customResolver = new SimpleObjectIdResolver();
        context.setHandlerInstantiator(new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
                return null;
            }

            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
                return null;
            }

            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
                return null;
            }

            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
                return null;
            }

            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
                return null;
            }

            @Override
            public ObjectIdGenerator<?> objectIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return null;
            }

            @Override
            public ObjectIdResolver resolverIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return customResolver;
            }
        });

        ObjectIdInfo info = new ObjectIdInfo(PropertyName.construct("id"), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);
        ObjectIdResolver resolver = context.objectIdResolverInstance(null, info);
        Assert.assertSame(customResolver, resolver);
    }

    @Test
    public void testConverterInstance() throws JsonMappingException {
        Assert.assertNull(context.converterInstance(null, null));

        StringToNumberConverter converterObj = new StringToNumberConverter();
        Assert.assertSame(converterObj, context.converterInstance(null, converterObj));

        Assert.assertNull(context.converterInstance(null, Converter.None.class));
        Assert.assertNull(context.converterInstance(null, Void.class));

        Converter<?, ?> conv = context.converterInstance(null, StringToNumberConverter.class);
        Assert.assertNotNull(conv);
        Assert.assertTrue(conv instanceof StringToNumberConverter);
    }

    @Test(expected = IllegalStateException.class)
    public void testConverterInstanceNotAClass() throws JsonMappingException {
        context.converterInstance(null, "NotAClassOrConverter");
    }

    @Test(expected = IllegalStateException.class)
    public void testConverterInstanceNotAConverterSubclass() throws JsonMappingException {
        context.converterInstance(null, String.class);
    }

    @Test
    public void testConverterInstanceWithHandlerInstantiator() throws JsonMappingException {
        final Converter<?, ?> customConv = new StringToNumberConverter();
        context.setHandlerInstantiator(new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
                return null;
            }

            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
                return null;
            }

            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
                return null;
            }

            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
                return null;
            }

            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
                return null;
            }

            @Override
            public ObjectIdGenerator<?> objectIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return null;
            }

            @Override
            public ObjectIdResolver resolverIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return null;
            }

            @Override
            public Converter<?, ?> converterInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return customConv;
            }
        });

        Converter<?, ?> conv = context.converterInstance(null, StringToNumberConverter.class);
        Assert.assertSame(customConv, conv);
    }

    @Test(expected = JsonMappingException.class)
    public void testReportBadDefinitionClass() throws JsonMappingException {
        context.reportBadDefinition(String.class, "Testing bad definition");
    }

    @Test
    public void testFormattingAndTruncatingHelpers() {
        Assert.assertEquals("simple msg", context._format("simple msg"));
        Assert.assertEquals("formatted 123 foo", context._format("formatted %d %s", 123, "foo"));

        Assert.assertEquals("", context._truncate(null));
        Assert.assertEquals("short", context._truncate("short"));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 600; i++) {
            sb.append('a');
        }
        String longStr = sb.toString();
        String truncated = context._truncate(longStr);
        Assert.assertEquals(1005, truncated.length());
        Assert.assertTrue(truncated.contains("]...["));

        Assert.assertEquals("[N/A]", context._quotedString(null));
        Assert.assertEquals("\"foo\"", context._quotedString("foo"));

        Assert.assertEquals("base", context._colonConcat("base", null));
        Assert.assertEquals("base: extra", context._colonConcat("base", "extra"));

        Assert.assertEquals("[N/A]", context._desc(null));
        Assert.assertEquals("foo", context._desc("foo"));
    }
}
