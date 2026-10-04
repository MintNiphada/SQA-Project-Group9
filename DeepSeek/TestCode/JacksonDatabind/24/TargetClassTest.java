package com.fasterxml.jackson.databind.cfg;

import static org.junit.Assert.*;
import org.junit.Test;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class BaseSettingsTest {

    private static final ClassIntrospector CI = new ClassIntrospector() {
        @Override
        public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) { return null; }
        @Override
        public BasicBeanDescription forDeserialization(DeserializationConfig cfg, JavaType type, MixInResolver r) { return null; }
        @Override
        public BasicBeanDescription forDeserializationWithBuilder(DeserializationConfig cfg, JavaType type, MixInResolver r) { return null; }
        @Override
        public BasicBeanDescription forCreation(DeserializationConfig cfg, JavaType type, MixInResolver r) { return null; }
        @Override
        public BasicBeanDescription forClassAnnotations(MapperConfig<?> cfg, JavaType type, MixInResolver r) { return null; }
        @Override
        public BasicBeanDescription forDirectClassAnnotations(MapperConfig<?> cfg, JavaType type, MixInResolver r) { return null; }
    };

    private static final AnnotationIntrospector AI = new AnnotationIntrospector() {
        @Override
        public Version version() { return null; }
    };

    private static final VisibilityChecker<?> VC = VisibilityChecker.Std.defaultInstance();
    private static final PropertyNamingStrategy PNS = PropertyNamingStrategy.SNAKE_CASE;
    private static final TypeFactory TF = TypeFactory.defaultInstance();
    private static final TypeResolverBuilder<?> TRB = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL);
    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
    private static final HandlerInstantiator HI = null;
    private static final Locale LOC = Locale.US;
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT+2");
    private static final Base64Variant B64 = Base64Variants.MIME;

    private BaseSettings createDefault() {
        return new BaseSettings(CI, AI, VC, PNS, TF, TRB, DF, HI, LOC, TZ, B64);
    }

    @Test
    public void testConstructorAndGetters() {
        BaseSettings bs = createDefault();
        assertSame(CI, bs.getClassIntrospector());
        assertSame(AI, bs.getAnnotationIntrospector());
        assertSame(VC, bs.getVisibilityChecker());
        assertSame(PNS, bs.getPropertyNamingStrategy());
        assertSame(TF, bs.getTypeFactory());
        assertSame(TRB, bs.getTypeResolverBuilder());
        assertSame(DF, bs.getDateFormat());
        assertSame(HI, bs.getHandlerInstantiator());
        assertSame(LOC, bs.getLocale());
        assertSame(TZ, bs.getTimeZone());
        assertSame(B64, bs.getBase64Variant());
    }

    @Test
    public void testWithClassIntrospectorSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withClassIntrospector(CI));
    }

    @Test
    public void testWithClassIntrospectorDifferent() {
        BaseSettings bs = createDefault();
        ClassIntrospector newCI = new ClassIntrospector() {
            @Override
            public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) { return null; }
            @Override
            public BasicBeanDescription forDeserialization(DeserializationConfig cfg, JavaType type, MixInResolver r) { return null; }
            @Override
            public BasicBeanDescription forDeserializationWithBuilder(DeserializationConfig cfg, JavaType type, MixInResolver r) { return null; }
            @Override
            public BasicBeanDescription forCreation(DeserializationConfig cfg, JavaType type, MixInResolver r) { return null; }
            @Override
            public BasicBeanDescription forClassAnnotations(MapperConfig<?> cfg, JavaType type, MixInResolver r) { return null; }
            @Override
            public BasicBeanDescription forDirectClassAnnotations(MapperConfig<?> cfg, JavaType type, MixInResolver r) { return null; }
        };
        BaseSettings newBs = bs.withClassIntrospector(newCI);
        assertNotSame(bs, newBs);
        assertSame(newCI, newBs.getClassIntrospector());
        assertSame(AI, newBs.getAnnotationIntrospector());
        assertSame(VC, newBs.getVisibilityChecker());
        assertSame(PNS, newBs.getPropertyNamingStrategy());
        assertSame(TF, newBs.getTypeFactory());
        assertSame(TRB, newBs.getTypeResolverBuilder());
        assertSame(DF, newBs.getDateFormat());
        assertSame(HI, newBs.getHandlerInstantiator());
        assertSame(LOC, newBs.getLocale());
        assertSame(TZ, newBs.getTimeZone());
        assertSame(B64, newBs.getBase64Variant());
    }

    @Test
    public void testWithAnnotationIntrospectorSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withAnnotationIntrospector(AI));
    }

    @Test
    public void testWithAnnotationIntrospectorDifferent() {
        BaseSettings bs = createDefault();
        AnnotationIntrospector newAI = new AnnotationIntrospector() {
            @Override
            public Version version() { return null; }
        };
        BaseSettings newBs = bs.withAnnotationIntrospector(newAI);
        assertNotSame(bs, newBs);
        assertSame(newAI, newBs.getAnnotationIntrospector());
        assertSame(CI, newBs.getClassIntrospector());
    }

    @Test
    public void testWithInsertedAnnotationIntrospector() {
        BaseSettings bs = createDefault();
        AnnotationIntrospector newAI = new AnnotationIntrospector() {
            @Override
            public Version version() { return null; }
        };
        BaseSettings newBs = bs.withInsertedAnnotationIntrospector(newAI);
        assertNotSame(bs, newBs);
        AnnotationIntrospector result = newBs.getAnnotationIntrospector();
        assertTrue(result instanceof AnnotationIntrospectorPair);
        AnnotationIntrospectorPair pair = (AnnotationIntrospectorPair) result;
        assertSame(newAI, pair.allIntrospectors().get(0));
        assertSame(AI, pair.allIntrospectors().get(1));
    }

    @Test
    public void testWithAppendedAnnotationIntrospector() {
        BaseSettings bs = createDefault();
        AnnotationIntrospector newAI = new AnnotationIntrospector() {
            @Override
            public Version version() { return null; }
        };
        BaseSettings newBs = bs.withAppendedAnnotationIntrospector(newAI);
        assertNotSame(bs, newBs);
        AnnotationIntrospector result = newBs.getAnnotationIntrospector();
        assertTrue(result instanceof AnnotationIntrospectorPair);
        AnnotationIntrospectorPair pair = (AnnotationIntrospectorPair) result;
        assertSame(AI, pair.allIntrospectors().get(0));
        assertSame(newAI, pair.allIntrospectors().get(1));
    }

    @Test
    public void testWithVisibilityCheckerSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withVisibilityChecker(VC));
    }

    @Test
    public void testWithVisibilityCheckerDifferent() {
        BaseSettings bs = createDefault();
        VisibilityChecker<?> newVC = VisibilityChecker.Std.defaultInstance().withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        BaseSettings newBs = bs.withVisibilityChecker(newVC);
        assertNotSame(bs, newBs);
        assertSame(newVC, newBs.getVisibilityChecker());
    }

    @Test
    public void testWithVisibility() {
        BaseSettings bs = createDefault();
        BaseSettings newBs = bs.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
        assertNotSame(bs, newBs);
        VisibilityChecker<?> vc = newBs.getVisibilityChecker();
        assertEquals(JsonAutoDetect.Visibility.NONE, vc.isFieldVisible(VisibilityChecker.Std.class));
    }

    @Test
    public void testWithPropertyNamingStrategySame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withPropertyNamingStrategy(PNS));
    }

    @Test
    public void testWithPropertyNamingStrategyDifferent() {
        BaseSettings bs = createDefault();
        PropertyNamingStrategy newPNS = PropertyNamingStrategy.LOWER_CASE;
        BaseSettings newBs = bs.withPropertyNamingStrategy(newPNS);
        assertNotSame(bs, newBs);
        assertSame(newPNS, newBs.getPropertyNamingStrategy());
    }

    @Test
    public void testWithTypeFactorySame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withTypeFactory(TF));
    }

    @Test
    public void testWithTypeFactoryDifferent() {
        BaseSettings bs = createDefault();
        TypeFactory newTF = TypeFactory.defaultInstance().withClassLoader(null);
        BaseSettings newBs = bs.withTypeFactory(newTF);
        assertNotSame(bs, newBs);
        assertSame(newTF, newBs.getTypeFactory());
    }

    @Test
    public void testWithTypeResolverBuilderSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withTypeResolverBuilder(TRB));
    }

    @Test
    public void testWithTypeResolverBuilderDifferent() {
        BaseSettings bs = createDefault();
        TypeResolverBuilder<?> newTRB = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.EVERYTHING);
        BaseSettings newBs = bs.withTypeResolverBuilder(newTRB);
        assertNotSame(bs, newBs);
        assertSame(newTRB, newBs.getTypeResolverBuilder());
    }

    @Test
    public void testWithDateFormatSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withDateFormat(DF));
    }

    @Test
    public void testWithDateFormatDifferent() {
        BaseSettings bs = createDefault();
        DateFormat newDF = new SimpleDateFormat("MM/dd/yyyy");
        BaseSettings newBs = bs.withDateFormat(newDF);
        assertNotSame(bs, newBs);
        assertSame(newDF, newBs.getDateFormat());
        assertEquals(newDF.getTimeZone(), newBs.getTimeZone());
    }

    @Test
    public void testWithDateFormatNull() {
        BaseSettings bs = createDefault();
        BaseSettings newBs = bs.withDateFormat(null);
        assertNotSame(bs, newBs);
        assertNull(newBs.getDateFormat());
        assertSame(TZ, newBs.getTimeZone());
    }

    @Test
    public void testWithHandlerInstantiatorSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.withHandlerInstantiator(HI));
    }

    @Test
    public void testWithHandlerInstantiatorDifferent() {
        BaseSettings bs = createDefault();
        HandlerInstantiator newHI = new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) { return null; }
            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) { return null; }
            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) { return null; }
            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) { return null; }
            @Override
            public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) { return null; }
        };
        BaseSettings newBs = bs.withHandlerInstantiator(newHI);
        assertNotSame(bs, newBs);
        assertSame(newHI, newBs.getHandlerInstantiator());
    }

    @Test
    public void testWithLocaleSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.with(LOC));
    }

    @Test
    public void testWithLocaleDifferent() {
        BaseSettings bs = createDefault();
        Locale newLoc = Locale.CANADA;
        BaseSettings newBs = bs.with(newLoc);
        assertNotSame(bs, newBs);
        assertSame(newLoc, newBs.getLocale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithTimeZoneNull() {
        BaseSettings bs = createDefault();
        bs.with((TimeZone) null);
    }

    @Test
    public void testWithTimeZoneSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.with(TZ));
    }

    @Test
    public void testWithTimeZoneDifferentStdDateFormat() {
        BaseSettings bs = createDefault();
        TimeZone newTZ = TimeZone.getTimeZone("GMT+3");
        BaseSettings newBs = bs.with(newTZ);
        assertNotSame(bs, newBs);
        assertSame(newTZ, newBs.getTimeZone());
        DateFormat df = newBs.getDateFormat();
        assertTrue(df instanceof StdDateFormat);
        assertEquals(newTZ, df.getTimeZone());
    }

    @Test
    public void testWithTimeZoneDifferentNonStdDateFormat() {
        DateFormat customDF = new SimpleDateFormat("yyyy-MM-dd");
        customDF.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        BaseSettings bs = new BaseSettings(CI, AI, VC, PNS, TF, TRB, customDF, HI, LOC, TZ, B64);
        TimeZone newTZ = TimeZone.getTimeZone("GMT+3");
        BaseSettings newBs = bs.with(newTZ);
        assertNotSame(bs, newBs);
        assertSame(newTZ, newBs.getTimeZone());
        DateFormat df = newBs.getDateFormat();
        assertNotSame(customDF, df);
        assertEquals(newTZ, df.getTimeZone());
    }

    @Test
    public void testWithBase64VariantSame() {
        BaseSettings bs = createDefault();
        assertSame(bs, bs.with(B64));
    }

    @Test
    public void testWithBase64VariantDifferent() {
        BaseSettings bs = createDefault();
        Base64Variant newB64 = Base64Variants.MODIFIED_FOR_URL;
        BaseSettings newBs = bs.with(newB64);
        assertNotSame(bs, newBs);
        assertSame(newB64, newBs.getBase64Variant());
    }
}
