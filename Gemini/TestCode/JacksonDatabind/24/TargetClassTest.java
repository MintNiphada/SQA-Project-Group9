package com.fasterxml.jackson.databind.cfg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;

public class BaseSettingsTest {

    private ClassIntrospector classIntrospector;
    private AnnotationIntrospector annotationIntrospector;
    private VisibilityChecker<?> visibilityChecker;
    private PropertyNamingStrategy propertyNamingStrategy;
    private TypeFactory typeFactory;
    private TypeResolverBuilder<?> typeResolverBuilder;
    private DateFormat dateFormat;
    private HandlerInstantiator handlerInstantiator;
    private Locale locale;
    private TimeZone timeZone;
    private Base64Variant base64Variant;

    private BaseSettings baseSettings;

    @Before
    public void setUp() {
        classIntrospector = new BasicClassIntrospector();
        annotationIntrospector = new JacksonAnnotationIntrospector();
        visibilityChecker = VisibilityChecker.Std.defaultInstance();
        propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE;
        typeFactory = TypeFactory.defaultInstance();
        typeResolverBuilder = new StdTypeResolverBuilder();
        dateFormat = new StdDateFormat();
        handlerInstantiator = null;
        locale = Locale.US;
        timeZone = TimeZone.getTimeZone("UTC");
        base64Variant = Base64Variants.MIME;

        baseSettings = new BaseSettings(
                classIntrospector,
                annotationIntrospector,
                visibilityChecker,
                propertyNamingStrategy,
                typeFactory,
                typeResolverBuilder,
                dateFormat,
                handlerInstantiator,
                locale,
                timeZone,
                base64Variant
        );
    }

    @Test
    public void testGettersAndInitialization() {
        Assert.assertSame(classIntrospector, baseSettings.getClassIntrospector());
        Assert.assertSame(annotationIntrospector, baseSettings.getAnnotationIntrospector());
        Assert.assertSame(visibilityChecker, baseSettings.getVisibilityChecker());
        Assert.assertSame(propertyNamingStrategy, baseSettings.getPropertyNamingStrategy());
        Assert.assertSame(typeFactory, baseSettings.getTypeFactory());
        Assert.assertSame(typeResolverBuilder, baseSettings.getTypeResolverBuilder());
        Assert.assertSame(dateFormat, baseSettings.getDateFormat());
        Assert.assertSame(handlerInstantiator, baseSettings.getHandlerInstantiator());
        Assert.assertSame(locale, baseSettings.getLocale());
        Assert.assertSame(timeZone, baseSettings.getTimeZone());
        Assert.assertSame(base64Variant, baseSettings.getBase64Variant());
    }

    @Test
    public void testWithClassIntrospector() {
        Assert.assertSame(baseSettings, baseSettings.withClassIntrospector(classIntrospector));

        ClassIntrospector newCi = new BasicClassIntrospector();
        BaseSettings modified = baseSettings.withClassIntrospector(newCi);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newCi, modified.getClassIntrospector());
        Assert.assertSame(baseSettings.getAnnotationIntrospector(), modified.getAnnotationIntrospector());
    }

    @Test
    public void testWithAnnotationIntrospector() {
        Assert.assertSame(baseSettings, baseSettings.withAnnotationIntrospector(annotationIntrospector));

        AnnotationIntrospector newAi = new JacksonAnnotationIntrospector();
        BaseSettings modified = baseSettings.withAnnotationIntrospector(newAi);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newAi, modified.getAnnotationIntrospector());
    }

    @Test
    public void testWithInsertedAnnotationIntrospector() {
        AnnotationIntrospector extra = new JacksonAnnotationIntrospector();
        BaseSettings modified = baseSettings.withInsertedAnnotationIntrospector(extra);

        Assert.assertNotSame(baseSettings, modified);
        Assert.assertTrue(modified.getAnnotationIntrospector() instanceof AnnotationIntrospectorPair);
        AnnotationIntrospectorPair pair = (AnnotationIntrospectorPair) modified.getAnnotationIntrospector();
        Assert.assertTrue(pair.allIntrospectors().contains(extra));
        Assert.assertTrue(pair.allIntrospectors().contains(annotationIntrospector));
    }

    @Test
    public void testWithAppendedAnnotationIntrospector() {
        AnnotationIntrospector extra = new JacksonAnnotationIntrospector();
        BaseSettings modified = baseSettings.withAppendedAnnotationIntrospector(extra);

        Assert.assertNotSame(baseSettings, modified);
        Assert.assertTrue(modified.getAnnotationIntrospector() instanceof AnnotationIntrospectorPair);
        AnnotationIntrospectorPair pair = (AnnotationIntrospectorPair) modified.getAnnotationIntrospector();
        Assert.assertTrue(pair.allIntrospectors().contains(extra));
        Assert.assertTrue(pair.allIntrospectors().contains(annotationIntrospector));
    }

    @Test
    public void testWithVisibilityChecker() {
        Assert.assertSame(baseSettings, baseSettings.withVisibilityChecker(visibilityChecker));

        VisibilityChecker<?> newVc = VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY);
        BaseSettings modified = baseSettings.withVisibilityChecker(newVc);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newVc, modified.getVisibilityChecker());
    }

    @Test
    public void testWithVisibility() {
        BaseSettings modified = baseSettings.withVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertNotSame(baseSettings.getVisibilityChecker(), modified.getVisibilityChecker());
    }

    @Test
    public void testWithPropertyNamingStrategy() {
        Assert.assertSame(baseSettings, baseSettings.withPropertyNamingStrategy(propertyNamingStrategy));

        PropertyNamingStrategy newPns = PropertyNamingStrategy.UPPER_CAMEL_CASE;
        BaseSettings modified = baseSettings.withPropertyNamingStrategy(newPns);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newPns, modified.getPropertyNamingStrategy());
    }

    @Test
    public void testWithTypeFactory() {
        Assert.assertSame(baseSettings, baseSettings.withTypeFactory(typeFactory));

        TypeFactory newTf = TypeFactory.defaultInstance().withModifier(null);
        BaseSettings modified = baseSettings.withTypeFactory(newTf);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newTf, modified.getTypeFactory());
    }

    @Test
    public void testWithTypeResolverBuilder() {
        Assert.assertSame(baseSettings, baseSettings.withTypeResolverBuilder(typeResolverBuilder));

        TypeResolverBuilder<?> newTyper = new StdTypeResolverBuilder();
        BaseSettings modified = baseSettings.withTypeResolverBuilder(newTyper);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newTyper, modified.getTypeResolverBuilder());
    }

    @Test
    public void testWithDateFormat() {
        Assert.assertSame(baseSettings, baseSettings.withDateFormat(dateFormat));

        DateFormat customDf = new SimpleDateFormat("yyyy/MM/dd");
        TimeZone customTz = TimeZone.getTimeZone("GMT+2");
        customDf.setTimeZone(customTz);

        BaseSettings modified = baseSettings.withDateFormat(customDf);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(customDf, modified.getDateFormat());
        Assert.assertEquals(customTz, modified.getTimeZone());

        BaseSettings nullDfSettings = baseSettings.withDateFormat(null);
        Assert.assertNotSame(baseSettings, nullDfSettings);
        Assert.assertNull(nullDfSettings.getDateFormat());
        Assert.assertSame(timeZone, nullDfSettings.getTimeZone());
    }

    @Test
    public void testWithHandlerInstantiator() {
        Assert.assertSame(baseSettings, baseSettings.withHandlerInstantiator(handlerInstantiator));

        HandlerInstantiator hi = new HandlerInstantiator() {
            @Override
            public com.fasterxml.jackson.databind.JsonSerializer<?> serializerInstance(
                    SerializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated,
                    Class<?> serClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.JsonDeserializer<?> deserializerInstance(
                    DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated,
                    Class<?> deserClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.KeyDeserializer keyDeserializerInstance(
                    DeserializationConfig config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated,
                    Class<?> keyDeserClass) {
                return null;
            }

            @Override
            public TypeResolverBuilder<?> typeResolverBuilderInstance(
                    MapperConfig<?> config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated,
                    Class<?> builderClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver typeIdResolverInstance(
                    MapperConfig<?> config,
                    com.fasterxml.jackson.databind.introspect.Annotated annotated,
                    Class<?> resolverClass) {
                return null;
            }
        };

        BaseSettings modified = baseSettings.withHandlerInstantiator(hi);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(hi, modified.getHandlerInstantiator());
    }

    @Test
    public void testWithLocale() {
        Assert.assertSame(baseSettings, baseSettings.with(locale));

        Locale newLocale = Locale.FRANCE;
        BaseSettings modified = baseSettings.with(newLocale);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newLocale, modified.getLocale());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNullTimeZoneThrowsException() {
        baseSettings.with((TimeZone) null);
    }

    @Test
    public void testWithTimeZoneUsingStdDateFormat() {
        TimeZone newTz = TimeZone.getTimeZone("GMT+5");
        BaseSettings modified = baseSettings.with(newTz);

        Assert.assertNotSame(baseSettings, modified);
        Assert.assertEquals(newTz, modified.getTimeZone());
        Assert.assertNotNull(modified.getDateFormat());
        Assert.assertEquals(newTz, modified.getDateFormat().getTimeZone());
    }

    @Test
    public void testWithTimeZoneUsingCustomDateFormat() {
        SimpleDateFormat customDf = new SimpleDateFormat("yyyy-MM-dd");
        customDf.setTimeZone(TimeZone.getTimeZone("UTC"));

        BaseSettings withCustomDf = baseSettings.withDateFormat(customDf);
        TimeZone newTz = TimeZone.getTimeZone("PST");

        BaseSettings modified = withCustomDf.with(newTz);
        Assert.assertNotSame(withCustomDf, modified);
        Assert.assertEquals(newTz, modified.getTimeZone());
        Assert.assertNotSame(customDf, modified.getDateFormat());
        Assert.assertEquals(newTz, modified.getDateFormat().getTimeZone());
    }

    @Test
    public void testWithBase64Variant() {
        Assert.assertSame(baseSettings, baseSettings.with(base64Variant));

        Base64Variant newVariant = Base64Variants.MODIFIED_FOR_URL;
        BaseSettings modified = baseSettings.with(newVariant);
        Assert.assertNotSame(baseSettings, modified);
        Assert.assertSame(newVariant, modified.getBase64Variant());
    }

    @Test
    public void testSerialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(baseSettings);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        BaseSettings deserialized = (BaseSettings) ois.readObject();

        Assert.assertNotNull(deserialized);
        Assert.assertEquals(baseSettings.getLocale(), deserialized.getLocale());
        Assert.assertEquals(baseSettings.getTimeZone(), deserialized.getTimeZone());
        Assert.assertNotNull(deserialized.getClassIntrospector());
        Assert.assertNotNull(deserialized.getAnnotationIntrospector());
    }
}
