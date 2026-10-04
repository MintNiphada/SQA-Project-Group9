package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.PropertyName;
import org.junit.Assert;
import org.junit.Test;

public class ObjectIdInfoTest {

    private static class CustomResolver implements ObjectIdResolver {
        @Override
        public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {}

        @Override
        public Object resolveId(ObjectIdGenerator.IdKey id) {
            return null;
        }

        @Override
        public ObjectIdResolver newForDeserialization(Object context) {
            return this;
        }

        @Override
        public boolean canUseFor(ObjectIdResolver resolverType) {
            return false;
        }
    }

    @Test
    public void testConstructorWithPropertyNameScopeGeneratorResolver() {
        PropertyName propName = new PropertyName("id");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.IntSequenceGenerator.class;
        Class<? extends ObjectIdResolver> resolver = CustomResolver.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, resolver);

        Assert.assertSame(propName, info.getPropertyName());
        Assert.assertSame(scope, info.getScope());
        Assert.assertSame(gen, info.getGeneratorType());
        Assert.assertSame(resolver, info.getResolverType());
        Assert.assertFalse(info.getAlwaysAsId());
    }

    @Test
    public void testConstructorWithPropertyNameScopeGenerator() {
        PropertyName propName = new PropertyName("id");
        Class<?> scope = Object.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.UUIDGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen);

        Assert.assertSame(propName, info.getPropertyName());
        Assert.assertSame(scope, info.getScope());
        Assert.assertSame(gen, info.getGeneratorType());
        Assert.assertSame(SimpleObjectIdResolver.class, info.getResolverType());
        Assert.assertFalse(info.getAlwaysAsId());
    }

    @Test
    public void testConstructorWithStringScopeGenerator() {
        String propName = "testId";
        Class<?> scope = Integer.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.PropertyGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen);

        Assert.assertEquals(new PropertyName(propName), info.getPropertyName());
        Assert.assertSame(scope, info.getScope());
        Assert.assertSame(gen, info.getGeneratorType());
        Assert.assertSame(SimpleObjectIdResolver.class, info.getResolverType());
        Assert.assertFalse(info.getAlwaysAsId());
    }

    @Test
    public void testConstructorWithNullResolverDefaultsToSimpleObjectIdResolver() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(propName, Object.class, ObjectIdGenerators.IntSequenceGenerator.class, null);

        Assert.assertSame(SimpleObjectIdResolver.class, info.getResolverType());
    }

    @Test
    public void testWithAlwaysAsIdSameState() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo info = new ObjectIdInfo(propName, Object.class, ObjectIdGenerators.IntSequenceGenerator.class, CustomResolver.class);

        ObjectIdInfo same = info.withAlwaysAsId(false);
        Assert.assertSame(info, same);
    }

    @Test
    public void testWithAlwaysAsIdDifferentState() {
        PropertyName propName = new PropertyName("id");
        Class<?> scope = Object.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.IntSequenceGenerator.class;
        Class<? extends ObjectIdResolver> resolver = CustomResolver.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, resolver);
        ObjectIdInfo changed = info.withAlwaysAsId(true);

        Assert.assertNotSame(info, changed);
        Assert.assertTrue(changed.getAlwaysAsId());
        Assert.assertSame(propName, changed.getPropertyName());
        Assert.assertSame(scope, changed.getScope());
        Assert.assertSame(gen, changed.getGeneratorType());
        Assert.assertSame(resolver, changed.getResolverType());

        ObjectIdInfo changedBack = changed.withAlwaysAsId(false);
        Assert.assertNotSame(changed, changedBack);
        Assert.assertFalse(changedBack.getAlwaysAsId());
    }

    @Test
    public void testToStringWithNonNullValues() {
        PropertyName propName = new PropertyName("customId");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.IntSequenceGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, CustomResolver.class);
        String expected = "ObjectIdInfo: propName=customId, scope=java.lang.String, generatorType=" + gen.getName() + ", alwaysAsId=false";

        Assert.assertEquals(expected, info.toString());
    }

    @Test
    public void testToStringWithNullValues() {
        ObjectIdInfo info = new ObjectIdInfo((PropertyName) null, null, null, null);
        String expected = "ObjectIdInfo: propName=null, scope=null, generatorType=null, alwaysAsId=false";

        Assert.assertEquals(expected, info.toString());
    }

    @Test
    public void testProtectedConstructorsViaInheritance() {
        PropertyName propName = new PropertyName("inherited");
        Class<?> scope = Long.class;
        Class<? extends ObjectIdGenerator<?>> gen = ObjectIdGenerators.IntSequenceGenerator.class;

        ObjectIdInfo info1 = new ObjectIdInfo(propName, scope, gen, true);
        Assert.assertTrue(info1.getAlwaysAsId());
        Assert.assertSame(SimpleObjectIdResolver.class, info1.getResolverType());

        ObjectIdInfo info2 = new ObjectIdInfo(propName, scope, gen, true, CustomResolver.class);
        Assert.assertTrue(info2.getAlwaysAsId());
        Assert.assertSame(CustomResolver.class, info2.getResolverType());
    }
}
