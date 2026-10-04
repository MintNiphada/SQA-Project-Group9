package com.fasterxml.jackson.databind.module;

import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ClassKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class SimpleAbstractTypeResolverTest {

    private SimpleAbstractTypeResolver resolver;

    @Before
    public void setUp() {
        resolver = new SimpleAbstractTypeResolver();
    }

    @Test
    public void testAddMappingValidAbstractToConcrete() {
        resolver.addMapping(AbstractCollection.class, ArrayList.class);
        Assert.assertEquals(1, resolver._mappings.size());
        Assert.assertEquals(ArrayList.class, resolver._mappings.get(new ClassKey(AbstractCollection.class)));
    }

    @Test
    public void testAddMappingAbstractToAbstract() {
        resolver.addMapping(AbstractCollection.class, AbstractList.class);
        Assert.assertEquals(AbstractList.class, resolver._mappings.get(new ClassKey(AbstractCollection.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMappingSameClassThrows() {
        resolver.addMapping(ArrayList.class, ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMappingNotSubtypeThrows() {
        resolver.addMapping(List.class, Map.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddMappingConcreteSuperTypeThrows() {
        resolver.addMapping(ConcreteClass.class, SubConcreteClass.class);
    }

    @Test
    public void testAddMappingReturnsSelf() {
        SimpleAbstractTypeResolver returned = resolver.addMapping(Collection.class, ArrayList.class);
        Assert.assertSame(resolver, returned);
    }

    @Test
    public void testFindTypeMappingDirectHit() {
        resolver.addMapping(Collection.class, ArrayList.class);
        JavaType input = new MockJavaType(Collection.class);
        JavaType result = resolver.findTypeMapping(null, input);
        Assert.assertNotNull(result);
        Assert.assertEquals(ArrayList.class, result.getRawClass());
    }

    @Test
    public void testFindTypeMappingNoMapping() {
        JavaType input = new MockJavaType(List.class);
        JavaType result = resolver.findTypeMapping(null, input);
        Assert.assertNull(result);
    }

    @Test
    public void testFindTypeMappingIndirectInheritance() {
        resolver.addMapping(Collection.class, ArrayList.class);
        JavaType input = new MockJavaType(HashSet.class);
        JavaType result = resolver.findTypeMapping(null, input);
        Assert.assertNull(result);
    }

    @Test
    public void testResolveAbstractTypeAlwaysNull() {
        JavaType input = new MockJavaType(Collection.class);
        JavaType result = resolver.resolveAbstractType(null, input);
        Assert.assertNull(result);
    }

    @Test
    public void testMultipleMappingsOverride() {
        resolver.addMapping(Collection.class, ArrayList.class);
        resolver.addMapping(Collection.class, LinkedList.class);
        Assert.assertEquals(LinkedList.class, resolver._mappings.get(new ClassKey(Collection.class)));
    }

    @Test
    public void testAddMappingInterface() {
        resolver.addMapping(Map.class, HashMap.class);
        Assert.assertEquals(HashMap.class, resolver._mappings.get(new ClassKey(Map.class)));
    }

    @Test
    public void testAddMappingWithGenerics() {
        resolver.addMapping(Iterator.class, Scanner.class);
        Assert.assertEquals(Scanner.class, resolver._mappings.get(new ClassKey(Iterator.class)));
    }

    @Test
    public void testAddMappingNullArguments() {
        try {
            resolver.addMapping(null, ArrayList.class);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            resolver.addMapping(Collection.class, null);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testFindTypeMappingNullType() {
        try {
            JavaType result = resolver.findTypeMapping(null, null);
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSerializable() {
        Assert.assertTrue(resolver instanceof java.io.Serializable);
    }

    static abstract class AbstractCollection extends AbstractList {
    }

    static class ConcreteClass {
    }

    static class SubConcreteClass extends ConcreteClass {
    }

    static class MockJavaType extends JavaType {
        private final Class<?> raw;

        public MockJavaType(Class<?> raw) {
            super(raw, null, null, null, false);
            this.raw = raw;
        }

        @Override
        public Class<?> getRawClass() {
            return raw;
        }

        @Override
        public JavaType narrowBy(Class<?> subclass) {
            return new MockJavaType(subclass);
        }

        @Override
        public boolean isTypeOrSubTypeOf(Class<?> clz) {
            return clz.isAssignableFrom(raw);
        }

        @Override
        public boolean isConcrete() {
            return !Modifier.isAbstract(raw.getModifiers());
        }

        @Override
        public boolean isAbstract() {
            return Modifier.isAbstract(raw.getModifiers());
        }

        @Override
        public JavaType refine(Class<?> rawType, com.fasterxml.jackson.databind.type.TypeBindings bindings,
                JavaType superClass, JavaType[] superInterfaces) {
            return null;
        }

        @Override
        public JavaType withStaticTyping() {
            return null;
        }

        @Override
        public JavaType withContentType(com.fasterxml.jackson.databind.type.TypeFactory typeFactory) {
            return null;
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return null;
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return null;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return null;
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return null;
        }

        @Override
        public JavaType withContentType(JavaType contentType) {
            return null;
        }

        @Override
        public boolean hasRawClass(Class<?> clz) {
            return raw == clz;
        }

        @Override
        public boolean hasGenericTypes() {
            return false;
        }

        @Override
        public boolean isContainerType() {
            return false;
        }

        @Override
        public boolean isMapLikeType() {
            return false;
        }

        @Override
        public boolean isCollectionLikeType() {
            return false;
        }

        @Override
        public boolean hasValueHandler() {
            return false;
        }

        @Override
        public boolean hasTypeHandler() {
            return false;
        }

        @Override
        public boolean hasContentTypeHandler() {
            return false;
        }

        @Override
        public boolean hasContentValueHandler() {
            return false;
        }

        @Override
        public JavaType getKeyType() {
            return null;
        }

        @Override
        public JavaType getContentType() {
            return null;
        }

        @Override
        public JavaType getReferencedType() {
            return null;
        }

        @Override
        public int containedTypeCount() {
            return 0;
        }

        @Override
        public JavaType containedType(int index) {
            return null;
        }

        @Override
        public String containedTypeName(int index) {
            return null;
        }

        @Override
        public Class<?> getBindings() {
            return null;
        }

        @Override
        public JavaType withHandlersFrom(JavaType src) {
            return this;
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            return sb;
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            return sb;
        }

        @Override
        public String toString() {
            return raw.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            MockJavaType other = (MockJavaType) o;
            return raw == other.raw;
        }
    }
}
