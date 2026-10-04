package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.*;

public class StdSubtypeResolverTest {

    private StdSubtypeResolver resolver;
    private ObjectMapper mapper;
    private MapperConfig<?> config;

    @JsonSubTypes({
            @JsonSubTypes.Type(value = SubA.class, name = "subA"),
            @JsonSubTypes.Type(value = SubB.class)
    })
    static abstract class AbstractBase {
    }

    static class ConcreteBase {
    }

    @JsonTypeName("namedSubA")
    static class SubA extends AbstractBase {
    }

    static class SubB extends AbstractBase {
    }

    @JsonSubTypes({
            @JsonSubTypes.Type(value = SubSubA.class, name = "subSubA")
    })
    static class SubSubBase extends SubA {
    }

    static class SubSubA extends SubSubBase {
    }

    static class Unrelated {
    }

    static class Container {
        @JsonSubTypes({
                @JsonSubTypes.Type(value = SubA.class, name = "propSubA"),
                @JsonSubTypes.Type(value = SubB.class)
        })
        @JsonProperty
        public AbstractBase prop;

        @JsonProperty
        public AbstractBase plainProp;
    }

    @Before
    public void setUp() {
        resolver = new StdSubtypeResolver();
        mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
    }

    @Test
    public void testRegistration() {
        resolver.registerSubtypes(SubA.class, SubB.class);
        Assert.assertNotNull(resolver._registeredSubtypes);
        Assert.assertEquals(2, resolver._registeredSubtypes.size());

        resolver.registerSubtypes(new NamedType(Unrelated.class, "unrelated"));
        Assert.assertEquals(3, resolver._registeredSubtypes.size());
    }

    @Test
    public void testCollectAndResolveSubtypesByClassWithClass() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, AbstractBase.class);
        Collection<NamedType> types = resolver.collectAndResolveSubtypesByClass(config, ac);
        Assert.assertNotNull(types);
        Assert.assertEquals(3, types.size());

        resolver.registerSubtypes(new NamedType(SubA.class, "customA"));
        types = resolver.collectAndResolveSubtypesByClass(config, ac);
        Assert.assertEquals(3, types.size());
    }

    @Test
    public void testCollectAndResolveSubtypesByClassWithMember() throws Exception {
        Field field = Container.class.getField("prop");
        AnnotatedMember member = new AnnotatedField(null, field, null);
        JavaType baseType = mapper.constructType(AbstractBase.class);

        Collection<NamedType> types = resolver.collectAndResolveSubtypesByClass(config, member, baseType);
        Assert.assertNotNull(types);
        Assert.assertTrue(types.size() >= 3);

        Field plainField = Container.class.getField("plainProp");
        AnnotatedMember plainMember = new AnnotatedField(null, plainField, null);
        types = resolver.collectAndResolveSubtypesByClass(config, plainMember, null);
        Assert.assertNotNull(types);
    }

    @Test
    public void testCollectAndResolveSubtypesByClassWithRegisteredSubtypes() throws Exception {
        resolver.registerSubtypes(new NamedType(SubA.class, "regA"), new NamedType(Unrelated.class, "unrelated"));
        Field field = Container.class.getField("prop");
        AnnotatedMember member = new AnnotatedField(null, field, null);
        JavaType baseType = mapper.constructType(AbstractBase.class);

        Collection<NamedType> types = resolver.collectAndResolveSubtypesByClass(config, member, baseType);
        Assert.assertNotNull(types);

        boolean foundRegA = false;
        for (NamedType nt : types) {
            if (nt.getType() == SubA.class && "regA".equals(nt.getName())) {
                foundRegA = true;
            }
            Assert.assertNotEquals(Unrelated.class, nt.getType());
        }
        Assert.assertTrue(foundRegA);
    }

    @Test
    public void testCollectAndResolveSubtypesByTypeIdWithClass() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, AbstractBase.class);
        Collection<NamedType> types = resolver.collectAndResolveSubtypesByTypeId(config, ac);
        Assert.assertNotNull(types);
        Assert.assertEquals(2, types.size());

        AnnotatedClass concreteAc = AnnotatedClassResolver.resolveWithoutSuperTypes(config, ConcreteBase.class);
        Collection<NamedType> concreteTypes = resolver.collectAndResolveSubtypesByTypeId(config, concreteAc);
        Assert.assertEquals(1, concreteTypes.size());
        Assert.assertEquals(ConcreteBase.class, concreteTypes.iterator().next().getType());
    }

    @Test
    public void testCollectAndResolveSubtypesByTypeIdWithMember() throws Exception {
        Field field = Container.class.getField("prop");
        AnnotatedMember member = new AnnotatedField(null, field, null);
        JavaType baseType = mapper.constructType(AbstractBase.class);

        resolver.registerSubtypes(new NamedType(SubA.class, "regSubA"), new NamedType(Unrelated.class, "unrelated"));
        Collection<NamedType> types = resolver.collectAndResolveSubtypesByTypeId(config, member, baseType);

        Assert.assertNotNull(types);
        boolean foundRegSubA = false;
        boolean foundSubB = false;
        for (NamedType nt : types) {
            if (nt.getType() == SubA.class && "regSubA".equals(nt.getName())) {
                foundRegSubA = true;
            }
            if (nt.getType() == SubB.class) {
                foundSubB = true;
            }
            Assert.assertNotEquals(Unrelated.class, nt.getType());
        }
        Assert.assertTrue(foundRegSubA);
        Assert.assertTrue(foundSubB);
    }

    @Test
    public void testRecursiveSubtypes() {
        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, SubSubBase.class);
        Collection<NamedType> types = resolver.collectAndResolveSubtypesByClass(config, ac);
        Assert.assertNotNull(types);
        Assert.assertEquals(2, types.size());

        Collection<NamedType> typesById = resolver.collectAndResolveSubtypesByTypeId(config, ac);
        Assert.assertNotNull(typesById);
        Assert.assertEquals(2, typesById.size());
    }

    @Test
    public void testSerialization() throws Exception {
        resolver.registerSubtypes(SubA.class);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(resolver);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        StdSubtypeResolver deserialized = (StdSubtypeResolver) ois.readObject();
        ois.close();

        Assert.assertNotNull(deserialized);
        Assert.assertNotNull(deserialized._registeredSubtypes);
        Assert.assertEquals(1, deserialized._registeredSubtypes.size());
    }

    @Test
    public void testNameUpdateInCollectAndResolve() {
        HashMap<NamedType, NamedType> collected = new HashMap<NamedType, NamedType>();
        NamedType unnamed = new NamedType(SubA.class, null);
        NamedType named = new NamedType(SubA.class, "explicitName");

        AnnotatedClass ac = AnnotatedClassResolver.resolveWithoutSuperTypes(config, SubA.class);
        resolver._collectAndResolve(ac, unnamed, config, config.getAnnotationIntrospector(), collected);
        Assert.assertTrue(collected.containsKey(unnamed));

        resolver._collectAndResolve(ac, named, config, config.getAnnotationIntrospector(), collected);
        Assert.assertEquals("explicitName", collected.get(unnamed).getName());
    }

    @Test
    public void testCombineNamedAndUnnamed() {
        Set<Class<?>> handled = new HashSet<Class<?>>();
        handled.add(AbstractBase.class);
        handled.add(ConcreteBase.class);
        handled.add(SubA.class);

        Map<String, NamedType> byName = new LinkedHashMap<String, NamedType>();
        byName.put("subA", new NamedType(SubA.class, "subA"));

        Collection<NamedType> result = resolver._combineNamedAndUnnamed(AbstractBase.class, handled, byName);
        Assert.assertEquals(2, result.size());

        boolean hasSubA = false;
        boolean hasConcrete = false;
        for (NamedType nt : result) {
            if (nt.getType() == SubA.class) hasSubA = true;
            if (nt.getType() == ConcreteBase.class) hasConcrete = true;
        }
        Assert.assertTrue(hasSubA);
        Assert.assertTrue(hasConcrete);
    }
}
