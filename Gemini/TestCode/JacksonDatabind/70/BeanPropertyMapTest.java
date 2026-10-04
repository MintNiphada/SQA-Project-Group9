package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Test;

public class BeanPropertyMapTest {

    static class DummyProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private boolean _throwInDeserialize = false;
        private Throwable _toThrow = null;

        public DummyProperty(String name) {
            super(new PropertyName(name), TypeFactory.defaultInstance().constructType(String.class), PropertyMetadata.STD_OPTIONAL, null);
        }

        public DummyProperty(String name, JsonDeserializer<Object> deser) {
            super(new PropertyName(name), TypeFactory.defaultInstance().constructType(String.class), PropertyMetadata.STD_OPTIONAL, deser);
        }

        public DummyProperty(DummyProperty src) {
            super(src);
            this._throwInDeserialize = src._throwInDeserialize;
            this._toThrow = src._toThrow;
        }

        public DummyProperty(DummyProperty src, PropertyName newName) {
            super(src, newName);
            this._throwInDeserialize = src._throwInDeserialize;
            this._toThrow = src._toThrow;
        }

        public DummyProperty(DummyProperty src, JsonDeserializer<?> deser) {
            super(src, deser);
            this._throwInDeserialize = src._throwInDeserialize;
            this._toThrow = src._toThrow;
        }

        public void setToThrow(Throwable t) {
            this._throwInDeserialize = true;
            this._toThrow = t;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new DummyProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new DummyProperty(this, deser);
        }

        @Override
        public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            if (_throwInDeserialize) {
                if (_toThrow instanceof IOException) {
                    throw (IOException) _toThrow;
                }
                if (_toThrow instanceof RuntimeException) {
                    throw (RuntimeException) _toThrow;
                }
                if (_toThrow instanceof Error) {
                    throw (Error) _toThrow;
                }
                throw new RuntimeException(_toThrow);
            }
            if (instance instanceof Map) {
                ((Map<String, Object>) instance).put(getName(), "value");
            }
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            deserializeAndSet(p, ctxt, instance);
            return instance;
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            if (instance instanceof Map) {
                ((Map<String, Object>) instance).put(getName(), value);
            }
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            set(instance, value);
            return instance;
        }
    }

    static class DummyUnwrappingDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer transformer) {
            return new DummyUnwrappingDeserializer();
        }
    }

    @Test
    public void testConstructAndBasics() {
        List<SettableBeanProperty> props = new ArrayList<SettableBeanProperty>();
        props.add(new DummyProperty("prop1"));
        props.add(new DummyProperty("prop2"));
        props.add(null);

        BeanPropertyMap map = BeanPropertyMap.construct(props, false);
        Assert.assertEquals(3, map.size());
        Assert.assertNotNull(map.find("prop1"));
        Assert.assertNotNull(map.find("prop2"));
        Assert.assertNull(map.find("nonExisting"));
        Assert.assertEquals(2, map.getPropertiesInInsertionOrder().length);
    }

    @Test
    public void testFindSizes() {
        for (int count : new int[]{1, 5, 6, 12, 13, 25, 50}) {
            List<SettableBeanProperty> list = new ArrayList<SettableBeanProperty>();
            for (int i = 0; i < count; i++) {
                list.add(new DummyProperty("p" + i));
            }
            BeanPropertyMap map = new BeanPropertyMap(false, list);
            Assert.assertEquals(count, map.size());
            for (int i = 0; i < count; i++) {
                Assert.assertNotNull(map.find("p" + i));
            }
        }
    }

    @Test
    public void testCollisionsAndSpillover() {
        List<SettableBeanProperty> props = new ArrayList<SettableBeanProperty>();
        for (int i = 0; i < 40; i++) {
            props.add(new DummyProperty("field_" + i + "_" + (i * 17)));
        }
        BeanPropertyMap map = new BeanPropertyMap(false, props);
        for (int i = 0; i < 40; i++) {
            SettableBeanProperty prop = map.find("field_" + i + "_" + (i * 17));
            Assert.assertNotNull("Should find prop " + i, prop);
        }
        Assert.assertNull(map.find("missingField"));
    }

    @Test
    public void testCaseInsensitivity() {
        List<SettableBeanProperty> props = Arrays.<SettableBeanProperty>asList(
                new DummyProperty("myProp"),
                new DummyProperty("OTHER")
        );
        BeanPropertyMap mapSensitive = BeanPropertyMap.construct(props, false);
        Assert.assertNotNull(mapSensitive.find("myProp"));
        Assert.assertNull(mapSensitive.find("MYPROP"));
        Assert.assertSame(mapSensitive, mapSensitive.withCaseInsensitivity(false));

        BeanPropertyMap mapInsensitive = mapSensitive.withCaseInsensitivity(true);
        Assert.assertNotSame(mapSensitive, mapInsensitive);
        Assert.assertSame(mapInsensitive, mapInsensitive.withCaseInsensitivity(true));

        Assert.assertNotNull(mapInsensitive.find("myprop"));
        Assert.assertNotNull(mapInsensitive.find("MYPROP"));
        Assert.assertNotNull(mapInsensitive.find("other"));
        Assert.assertNotNull(mapInsensitive.find("OTHER"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindWithNullKey() {
        BeanPropertyMap map = new BeanPropertyMap(false, Collections.<SettableBeanProperty>emptyList());
        map.find((String) null);
    }

    @Test
    public void testAssignIndexesAndFindIndex() {
        DummyProperty p1 = new DummyProperty("a");
        DummyProperty p2 = new DummyProperty("b");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1, p2));
        map.assignIndexes();

        Assert.assertTrue(p1.getPropertyIndex() >= 0);
        Assert.assertTrue(p2.getPropertyIndex() >= 0);
        Assert.assertNotNull(map.find(p1.getPropertyIndex()));
        Assert.assertNotNull(map.find(p2.getPropertyIndex()));
        Assert.assertNull(map.find(9999));
    }

    @Test
    public void testWithProperty() {
        DummyProperty p1 = new DummyProperty("a");
        DummyProperty p2 = new DummyProperty("b");
        BeanPropertyMap map = new BeanPropertyMap(false, new ArrayList<SettableBeanProperty>(Arrays.asList(p1)));

        map = map.withProperty(p2);
        Assert.assertEquals(2, map.getPropertiesInInsertionOrder().length);
        Assert.assertNotNull(map.find("a"));
        Assert.assertNotNull(map.find("b"));

        DummyProperty p1Replacement = new DummyProperty("a");
        map = map.withProperty(p1Replacement);
        Assert.assertSame(p1Replacement, map.find("a"));

        for (int i = 0; i < 30; i++) {
            map = map.withProperty(new DummyProperty("extra_" + i));
            Assert.assertNotNull(map.find("extra_" + i));
        }
    }

    @Test
    public void testReplace() {
        DummyProperty p1 = new DummyProperty("a");
        DummyProperty p2 = new DummyProperty("b");
        DummyProperty p3 = new DummyProperty("c");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1, p2, p3));

        DummyProperty p2New = new DummyProperty("b");
        map.replace(p2New);
        Assert.assertSame(p2New, map.find("b"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testReplaceNonExisting() {
        DummyProperty p1 = new DummyProperty("a");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1));
        map.replace(new DummyProperty("nonExisting"));
    }

    @Test
    public void testRemove() {
        DummyProperty p1 = new DummyProperty("a");
        DummyProperty p2 = new DummyProperty("b");
        DummyProperty p3 = new DummyProperty("c");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1, p2, p3));

        map.remove(p2);
        Assert.assertNull(map.find("b"));
        Assert.assertNotNull(map.find("a"));
        Assert.assertNotNull(map.find("c"));

        SettableBeanProperty[] inOrder = map.getPropertiesInInsertionOrder();
        Assert.assertEquals(3, inOrder.length);
        Assert.assertNull(inOrder[1]);
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveNonExisting() {
        DummyProperty p1 = new DummyProperty("a");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1));
        map.remove(new DummyProperty("b"));
    }

    @Test
    public void testRenameAll() {
        DummyProperty p1 = new DummyProperty("first", new DummyUnwrappingDeserializer());
        DummyProperty p2 = new DummyProperty("second", null);
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1, p2));

        Assert.assertSame(map, map.renameAll(null));
        Assert.assertSame(map, map.renameAll(NameTransformer.NOP));

        NameTransformer transformer = new NameTransformer() {
            @Override
            public String transform(String name) {
                return "prefix_" + name;
            }

            @Override
            public String reverse(String transformed) {
                return transformed.substring("prefix_".length());
            }
        };

        map.remove(p2);
        BeanPropertyMap renamed = map.renameAll(transformer);
        Assert.assertNotNull(renamed.find("prefix_first"));
        Assert.assertNull(renamed.find("prefix_second"));
    }

    @Test
    public void testWithoutProperties() {
        DummyProperty p1 = new DummyProperty("a");
        DummyProperty p2 = new DummyProperty("b");
        DummyProperty p3 = new DummyProperty("c");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1, p2, p3));

        Assert.assertSame(map, map.withoutProperties(Collections.<String>emptyList()));

        map.remove(p2);
        BeanPropertyMap pruned = map.withoutProperties(Collections.singleton("c"));
        Assert.assertNotNull(pruned.find("a"));
        Assert.assertNull(pruned.find("b"));
        Assert.assertNull(pruned.find("c"));
    }

    @Test
    public void testFindDeserializeAndSetSuccess() throws IOException {
        DummyProperty p1 = new DummyProperty("prop");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1));
        Map<String, Object> target = new HashMap<String, Object>();

        Assert.assertFalse(map.findDeserializeAndSet(null, null, target, "unknown"));
        Assert.assertTrue(map.findDeserializeAndSet(null, null, target, "prop"));
        Assert.assertEquals("value", target.get("prop"));
    }

    @Test
    public void testFindDeserializeAndSetWrapExceptions() throws IOException {
        DummyProperty p1 = new DummyProperty("prop");
        p1.setToThrow(new InvocationTargetException(new RuntimeException("wrapped cause")));
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1));

        try {
            map.findDeserializeAndSet(null, null, new HashMap<String, Object>(), "prop");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("wrapped cause"));
        }
    }

    @Test(expected = OutOfMemoryError.class)
    public void testWrapAndThrowError() throws IOException {
        DummyProperty p = new DummyProperty("prop");
        p.setToThrow(new OutOfMemoryError("OOM"));
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p));
        map.findDeserializeAndSet(null, null, new HashMap<String, Object>(), "prop");
    }

    @Test
    public void testWrapAndThrowIOException() {
        DummyProperty p = new DummyProperty("prop");
        p.setToThrow(new IOException("IO error"));
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p));

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            map.findDeserializeAndSet(null, ctxt, new HashMap<String, Object>(), "prop");
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("IO error", e.getMessage());
        }
    }

    @Test
    public void testWrapAndThrowJsonProcessingException() throws IOException {
        DummyProperty p = new DummyProperty("prop");
        p.setToThrow(new JsonParseException(null, "parse error"));
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p));

        try {
            map.findDeserializeAndSet(null, null, new HashMap<String, Object>(), "prop");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("parse error"));
        }
    }

    @Test
    public void testWrapAndThrowUncheckedExceptionWhenWrapDisabled() throws IOException {
        DummyProperty p = new DummyProperty("prop");
        p.setToThrow(new IllegalStateException("state error"));
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p));

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.WRAP_EXCEPTIONS);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            map.findDeserializeAndSet(null, ctxt, new HashMap<String, Object>(), "prop");
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("state error", e.getMessage());
        }
    }

    @Test
    public void testIteratorAndToString() {
        DummyProperty p1 = new DummyProperty("prop1");
        DummyProperty p2 = new DummyProperty("prop2");
        BeanPropertyMap map = new BeanPropertyMap(false, Arrays.<SettableBeanProperty>asList(p1, p2));

        int count = 0;
        for (SettableBeanProperty prop : map) {
            Assert.assertNotNull(prop);
            count++;
        }
        Assert.assertEquals(2, count);

        String str = map.toString();
        Assert.assertTrue(str.startsWith("Properties=["));
        Assert.assertTrue(str.contains("prop1"));
        Assert.assertTrue(str.contains("prop2"));
    }
}
