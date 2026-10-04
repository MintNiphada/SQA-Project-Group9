package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CollectionTypeTest {

    @Test
    public void testConstructWithBindings() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        TypeBindings bindings = TypeBindings.create(ArrayList.class, elemType);
        JavaType superClass = tf.constructType(Object.class);
        JavaType[] superInterfaces = new JavaType[0];

        CollectionType type = CollectionType.construct(ArrayList.class, bindings, superClass, superInterfaces, elemType);

        Assert.assertNotNull(type);
        Assert.assertEquals(ArrayList.class, type.getRawClass());
        Assert.assertEquals(elemType, type.getContentType());
        Assert.assertEquals(bindings, type.getBindings());
        Assert.assertEquals(superClass, type.getSuperClass());
        Assert.assertFalse(type.useStaticType());
        Assert.assertNull(type.getValueHandler());
        Assert.assertNull(type.getTypeHandler());
        Assert.assertTrue(type.isCollectionLikeType());
        Assert.assertTrue(type.isContainerType());
    }

    @Test
    public void testConstructDeprecated() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(Integer.class);

        CollectionType type = CollectionType.construct(List.class, elemType);

        Assert.assertNotNull(type);
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(elemType, type.getContentType());
        Assert.assertFalse(type.useStaticType());
    }

    @Test
    public void testProtectedConstructor() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType baseType = CollectionType.construct(ArrayList.class, elemType);
        JavaType altElemType = tf.constructType(Long.class);

        CollectionType type = new CollectionType(baseType, altElemType);

        Assert.assertEquals(ArrayList.class, type.getRawClass());
        Assert.assertEquals(altElemType, type.getContentType());
    }

    @Test
    public void testNarrow() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(List.class, elemType);

        JavaType narrowed = type._narrow(ArrayList.class);

        Assert.assertTrue(narrowed instanceof CollectionType);
        Assert.assertEquals(ArrayList.class, narrowed.getRawClass());
        Assert.assertEquals(elemType, narrowed.getContentType());
    }

    @Test
    public void testWithContentTypeSame() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType);

        JavaType same = type.withContentType(elemType);

        Assert.assertSame(type, same);
    }

    @Test
    public void testWithContentTypeDifferent() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType1 = tf.constructType(String.class);
        JavaType elemType2 = tf.constructType(Integer.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType1);

        JavaType modified = type.withContentType(elemType2);

        Assert.assertNotSame(type, modified);
        Assert.assertTrue(modified instanceof CollectionType);
        Assert.assertEquals(elemType2, modified.getContentType());
        Assert.assertEquals(ArrayList.class, modified.getRawClass());
    }

    @Test
    public void testWithTypeHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType);
        Object handler = "customTypeHandler";

        CollectionType modified = type.withTypeHandler(handler);

        Assert.assertNotSame(type, modified);
        Assert.assertEquals(handler, modified.getTypeHandler());
        Assert.assertNull(type.getTypeHandler());
    }

    @Test
    public void testWithContentTypeHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType);
        Object handler = "customContentTypeHandler";

        CollectionType modified = type.withContentTypeHandler(handler);

        Assert.assertNotSame(type, modified);
        Assert.assertEquals(handler, modified.getContentType().getTypeHandler());
        Assert.assertNull(type.getContentType().getTypeHandler());
    }

    @Test
    public void testWithValueHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType);
        Object handler = "customValueHandler";

        CollectionType modified = type.withValueHandler(handler);

        Assert.assertNotSame(type, modified);
        Assert.assertEquals(handler, modified.getValueHandler());
        Assert.assertNull(type.getValueHandler());
    }

    @Test
    public void testWithContentValueHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType);
        Object handler = "customContentValueHandler";

        CollectionType modified = type.withContentValueHandler(handler);

        Assert.assertNotSame(type, modified);
        Assert.assertEquals(handler, modified.getContentType().getValueHandler());
        Assert.assertNull(type.getContentType().getValueHandler());
    }

    @Test
    public void testWithStaticTyping() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType);

        Assert.assertFalse(type.useStaticType());

        CollectionType staticType = type.withStaticTyping();

        Assert.assertNotSame(type, staticType);
        Assert.assertTrue(staticType.useStaticType());
        Assert.assertTrue(staticType.getContentType().useStaticType());

        CollectionType sameStatic = staticType.withStaticTyping();
        Assert.assertSame(staticType, sameStatic);
    }

    @Test
    public void testRefine() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(List.class, elemType);

        TypeBindings newBindings = TypeBindings.create(LinkedList.class, elemType);
        JavaType newSuperClass = tf.constructType(Object.class);
        JavaType[] newSuperInts = new JavaType[0];

        JavaType refined = type.refine(LinkedList.class, newBindings, newSuperClass, newSuperInts);

        Assert.assertTrue(refined instanceof CollectionType);
        Assert.assertEquals(LinkedList.class, refined.getRawClass());
        Assert.assertEquals(newBindings, refined.getBindings());
        Assert.assertEquals(newSuperClass, refined.getSuperClass());
        Assert.assertEquals(elemType, refined.getContentType());
    }

    @Test
    public void testToString() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionType type = CollectionType.construct(ArrayList.class, elemType);

        String str = type.toString();

        Assert.assertEquals("[collection type; class java.util.ArrayList, contains " + elemType.toString() + "]", str);
    }
}
