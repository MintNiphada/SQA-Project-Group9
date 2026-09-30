package org.apache.commons.jxpath.ri.model.beans;

import java.util.HashMap;
import java.util.Locale;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dom.DOMNodePointer;
import org.junit.Assert;
import org.junit.Test;

public class NullPropertyPointerTest {

    @Test
    public void testBasicPropertiesAndDefaults() {
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);

        Assert.assertEquals("*", npp.getPropertyName());
        Assert.assertEquals(new QName("*"), npp.getName());
        Assert.assertEquals(0, npp.getLength());
        Assert.assertNull(npp.getBaseValue());
        Assert.assertNull(npp.getImmediateNode());
        Assert.assertTrue(npp.isLeaf());
        Assert.assertFalse(npp.isActualProperty());
        Assert.assertFalse(npp.isActual());
        Assert.assertTrue(npp.isContainer());
        Assert.assertEquals(0, npp.getPropertyCount());
        Assert.assertArrayEquals(new String[0], npp.getPropertyNames());

        npp.setPropertyIndex(5);
        Assert.assertEquals(PropertyPointer.UNSPECIFIED_PROPERTY, npp.getPropertyIndex());

        npp.setPropertyName("customProp");
        Assert.assertEquals("customProp", npp.getPropertyName());
        Assert.assertEquals(new QName("customProp"), npp.getName());

        NodePointer valPointer = npp.getValuePointer();
        Assert.assertTrue(valPointer instanceof NullPointer);
        Assert.assertEquals(new QName("customProp"), valPointer.getName());
    }

    @Test
    public void testIsCollection() {
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);

        Assert.assertEquals(NullPropertyPointer.WHOLE_COLLECTION, npp.getIndex());
        Assert.assertFalse(npp.isCollection());

        npp.setIndex(0);
        Assert.assertTrue(npp.isCollection());

        npp.setIndex(2);
        Assert.assertTrue(npp.isCollection());

        npp.setIndex(NullPropertyPointer.WHOLE_COLLECTION);
        Assert.assertFalse(npp.isCollection());
    }

    @Test
    public void testAsPathDefaultAndByNameAttribute() {
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), new Object(), Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);

        npp.setPropertyName("foo");
        Assert.assertEquals("/foo", npp.asPath());

        npp.setNameAttributeValue("bar");
        Assert.assertEquals("/[@name='bar']", npp.asPath());

        npp.setIndex(0);
        Assert.assertEquals("/[@name='bar'][1]", npp.asPath());

        npp.setIndex(2);
        Assert.assertEquals("/[@name='bar'][3]", npp.asPath());

        npp.setNameAttributeValue("a'b\"c'd\"e");
        Assert.assertEquals("/[@name='a&apos;b&quot;c&apos;d&quot;e'][3]", npp.asPath());
    }

    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValueNullParent() {
        NullPropertyPointer npp = new NullPropertyPointer(null);
        npp.setValue("test");
    }

    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValueContainerParent() {
        NullPointer nullParent = new NullPointer(Locale.ENGLISH, "id");
        NullPropertyPointer npp = new NullPropertyPointer(nullParent);
        npp.setValue("test");
    }

    @Test
    public void testSetValueDynamicPropertyOwnerParent() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), map, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setPropertyName("newKey");

        npp.setValue("newValue");
        Assert.assertEquals("newValue", map.get("newKey"));
    }

    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValueNonDynamicParent() {
        Object bean = new Object();
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), bean, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setPropertyName("nonExistent");
        npp.setValue("val");
    }

    @Test
    public void testCreatePathOnDynamicParent() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), map, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setPropertyName("dynamicProp");

        NodePointer createdPointer = npp.createPath(context);
        Assert.assertNotNull(createdPointer);
        Assert.assertTrue(map.containsKey("dynamicProp"));
    }

    @Test
    public void testCreatePathWithValueOnDynamicParent() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), map, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setPropertyName("dynamicPropWithValue");

        NodePointer createdPointer = npp.createPath(context, "myValue");
        Assert.assertNotNull(createdPointer);
        Assert.assertEquals("myValue", map.get("dynamicPropWithValue"));
    }

    @Test
    public void testCreatePathAttribute() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), map, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setAttribute(true);
        npp.setPropertyName("attrName");

        NodePointer created = npp.createPath(context);
        Assert.assertNotNull(created);
    }

    @Test
    public void testCreatePathAttributeWithValue() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        JXPathContext context = JXPathContext.newContext(map);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), map, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setAttribute(true);
        npp.setPropertyName("attrName");

        NodePointer created = npp.createPath(context, "attrVal");
        Assert.assertNotNull(created);
        Assert.assertEquals("attrVal", created.getValue());
    }

    @Test
    public void testCreateChild() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        HashMap<String, Object> childMap = new HashMap<String, Object>();
        map.put("childMap", childMap);

        JXPathContext context = JXPathContext.newContext(map);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), map, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setPropertyName("childMap");

        NodePointer createdChild = npp.createChild(context, new QName("subChild"), 0);
        Assert.assertNotNull(createdChild);
    }

    @Test
    public void testCreateChildWithValue() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        HashMap<String, Object> childMap = new HashMap<String, Object>();
        map.put("childMap", childMap);

        JXPathContext context = JXPathContext.newContext(map);
        NodePointer rootPointer = NodePointer.newNodePointer(new QName("root"), map, Locale.ENGLISH);
        NullPropertyPointer npp = new NullPropertyPointer(rootPointer);
        npp.setPropertyName("childMap");

        NodePointer createdChild = npp.createChild(context, new QName("subChild"), 0, "finalValue");
        Assert.assertNotNull(createdChild);
        Assert.assertEquals("finalValue", childMap.get("subChild"));
    }

    @Test
    public void testCreatePathWithNonPropertyOwnerParent() {
        NodePointer nonPropertyParent = new NodePointer(null, Locale.ENGLISH) {
            private static final long serialVersionUID = 1L;

            public boolean isLeaf() { return false; }
            public boolean isCollection() { return false; }
            public int getLength() { return 1; }
            public QName getName() { return new QName("mock"); }
            public Object getBaseValue() { return null; }
            public Object getImmediateNode() { return null; }
            public void setValue(Object value) {}
            public int compareChildNodePointers(NodePointer p1, NodePointer p2) { return 0; }

            public NodePointer createPath(JXPathContext context) {
                return this;
            }

            public NodePointer createChild(JXPathContext context, QName name, int index) {
                return new NullPointer(this, name);
            }

            public NodePointer createChild(JXPathContext context, QName name, int index, Object value) {
                NullPointer np = new NullPointer(this, name);
                return np;
            }
        };

        NullPropertyPointer npp = new NullPropertyPointer(nonPropertyParent);
        npp.setPropertyName("someProp");

        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer path = npp.createPath(context);
        Assert.assertNotNull(path);
        Assert.assertTrue(path instanceof NullPointer);

        NodePointer pathWithValue = npp.createPath(context, "dummy");
        Assert.assertNotNull(pathWithValue);
        Assert.assertTrue(pathWithValue instanceof NullPointer);
    }
}
