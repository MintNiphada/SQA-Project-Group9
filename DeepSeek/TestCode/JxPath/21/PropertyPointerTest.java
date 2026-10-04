package org.apache.commons.jxpath.ri.model.beans;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.util.ValueUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PropertyPointerTest {

    private TestNodePointer parent;
    private TestPropertyPointer pointer;
    private Object bean;

    @Before
    public void setUp() {
        bean = new Object();
        parent = new TestNodePointer(bean);
        pointer = new TestPropertyPointer(parent, "testProperty", null, true);
    }

    // Concrete NodePointer for testing
    private static class TestNodePointer extends NodePointer {
        private Object node;
        private boolean actual = true;
        private NodePointer valuePointer;

        public TestNodePointer(Object node) {
            super(null);
            this.node = node;
        }

        public void setActual(boolean actual) {
            this.actual = actual;
        }

        public void setValuePointer(NodePointer valuePointer) {
            this.valuePointer = valuePointer;
        }

        @Override
        public Object getNode() {
            return node;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public QName getName() {
            return new QName("test");
        }

        @Override
        public Object getImmediateNode() {
            return node;
        }

        @Override
        public void setValue(Object value) {
            this.node = value;
        }

        @Override
        public NodePointer createPath(JXPathContext context) {
            return this;
        }

        @Override
        public NodePointer createPath(JXPathContext context, Object value) {
            setValue(value);
            return this;
        }

        @Override
        public NodePointer createChild(JXPathContext context, QName name, int index) {
            return new TestNodePointer(null);
        }

        @Override
        public NodePointer createChild(JXPathContext context, QName name, int index, Object value) {
            return new TestNodePointer(value);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(node);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof TestNodePointer)) return false;
            return node == ((TestNodePointer) obj).node;
        }

        @Override
        public String asPath() {
            return "/test";
        }

        @Override
        public boolean isActual() {
            return actual;
        }

        @Override
        public NodePointer getValuePointer() {
            if (valuePointer != null) {
                return valuePointer;
            }
            return this;
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0; // default
        }
    }

    // Concrete PropertyPointer for testing
    private static class TestPropertyPointer extends PropertyPointer {
        private String propertyName;
        private String[] propertyNames;
        private Object baseValue;
        private boolean actualProperty;
        private AbstractFactory factory;

        public TestPropertyPointer(NodePointer parent, String propertyName, Object baseValue, boolean actualProperty) {
            super(parent);
            this.propertyName = propertyName;
            this.baseValue = baseValue;
            this.actualProperty = actualProperty;
        }

        public void setBaseValue(Object baseValue) {
            this.baseValue = baseValue;
        }

        public void setFactory(AbstractFactory factory) {
            this.factory = factory;
        }

        @Override
        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public int getPropertyCount() {
            return propertyNames == null ? 0 : propertyNames.length;
        }

        @Override
        public String[] getPropertyNames() {
            return propertyNames;
        }

        @Override
        protected boolean isActualProperty() {
            return actualProperty;
        }

        @Override
        public Object getBaseValue() {
            return baseValue;
        }

        @Override
        protected AbstractFactory getAbstractFactory(JXPathContext context) {
            if (factory != null) {
                return factory;
            }
            return super.getAbstractFactory(context);
        }
    }

    @Test
    public void testConstructor() {
        Assert.assertEquals(PropertyPointer.UNSPECIFIED_PROPERTY, pointer.getPropertyIndex());
        Assert.assertNull(pointer.getBean()); // bean is null initially
    }

    @Test
    public void testGetPropertyIndex() {
        pointer.setPropertyIndex(2);
        Assert.assertEquals(2, pointer.getPropertyIndex());
    }

    @Test
    public void testSetPropertyIndexSameValue() {
        pointer.setPropertyIndex(5);
        int oldIndex = pointer.getIndex();
        pointer.setPropertyIndex(5);
        Assert.assertEquals(5, pointer.getPropertyIndex());
        // index should remain unchanged if propertyIndex didn't change
        Assert.assertEquals(oldIndex, pointer.getIndex());
    }

    @Test
    public void testSetPropertyIndexDifferentValue() {
        pointer.setPropertyIndex(1);
        pointer.setIndex(2); // set a specific index
        pointer.setPropertyIndex(3);
        Assert.assertEquals(3, pointer.getPropertyIndex());
        Assert.assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());
    }

    @Test
    public void testSetPropertyIndexFromUnspecified() {
        pointer.setPropertyIndex(10);
        Assert.assertEquals(10, pointer.getPropertyIndex());
        Assert.assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());
    }

    @Test
    public void testGetBeanWhenNull() {
        // bean is null, should get from parent
        Object bean = pointer.getBean();
        Assert.assertSame(this.bean, bean);
    }

    @Test
    public void testGetBeanWhenAlreadySet() {
        Object customBean = "custom";
        pointer.bean = customBean; // directly set
        Assert.assertSame(customBean, pointer.getBean());
    }

    @Test
    public void testGetName() {
        QName name = pointer.getName();
        Assert.assertNull(name.getPrefix());
        Assert.assertEquals("testProperty", name.getName());
    }

    @Test
    public void testIsActualWhenNotActualProperty() {
        pointer = new TestPropertyPointer(parent, "prop", null, false);
        Assert.assertFalse(pointer.isActual());
    }

    @Test
    public void testIsActualWhenActualPropertyAndParentActual() {
        parent.setActual(true);
        pointer = new TestPropertyPointer(parent, "prop", null, true);
        Assert.assertTrue(pointer.isActual());
    }

    @Test
    public void testIsActualWhenActualPropertyButParentNotActual() {
        parent.setActual(false);
        pointer = new TestPropertyPointer(parent, "prop", null, true);
        Assert.assertFalse(pointer.isActual());
    }

    @Test
    public void testGetImmediateNodeWholeCollection() {
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        pointer.setBaseValue("baseValue");
        Object result = pointer.getImmediateNode();
        Assert.assertEquals("baseValue", result);
        // call again to test caching
        Assert.assertSame(result, pointer.getImmediateNode());
    }

    @Test
    public void testGetImmediateNodeSpecificIndex() {
        List<String> list = Arrays.asList("a", "b", "c");
        pointer.setBaseValue(list);
        pointer.setIndex(1);
        Object result = pointer.getImmediateNode();
        Assert.assertEquals("b", result);
        Assert.assertSame(result, pointer.getImmediateNode());
    }

    @Test
    public void testGetImmediateNodeNullBaseValue() {
        pointer.setBaseValue(null);
        pointer.setIndex(0);
        Assert.assertNull(pointer.getImmediateNode());
    }

    @Test
    public void testIsCollectionTrue() {
        pointer.setBaseValue(new ArrayList());
        Assert.assertTrue(pointer.isCollection());
    }

    @Test
    public void testIsCollectionFalse() {
        pointer.setBaseValue("string");
        Assert.assertFalse(pointer.isCollection());
    }

    @Test
    public void testIsCollectionNull() {
        pointer.setBaseValue(null);
        Assert.assertFalse(pointer.isCollection());
    }

    @Test
    public void testIsLeafNullNode() {
        pointer.setBaseValue(null);
        Assert.assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafAtomic() {
        pointer.setBaseValue("atomic");
        Assert.assertTrue(pointer.isLeaf());
    }

    @Test
    public void testIsLeafNonAtomic() {
        pointer.setBaseValue(new ArrayList());
        Assert.assertFalse(pointer.isLeaf());
    }

    @Test
    public void testGetLengthArray() {
        pointer.setBaseValue(new String[]{"a", "b"});
        Assert.assertEquals(2, pointer.getLength());
    }

    @Test
    public void testGetLengthCollection() {
        pointer.setBaseValue(Arrays.asList(1, 2, 3));
        Assert.assertEquals(3, pointer.getLength());
    }

    @Test
    public void testGetLengthNull() {
        pointer.setBaseValue(null);
        Assert.assertEquals(0, pointer.getLength());
    }

    @Test
    public void testGetLengthSingleObject() {
        pointer.setBaseValue("single");
        Assert.assertEquals(1, pointer.getLength());
    }

    @Test
    public void testGetImmediateValuePointer() {
        pointer.setBaseValue("value");
        NodePointer valuePtr = pointer.getImmediateValuePointer();
        Assert.assertNotNull(valuePtr);
        Assert.assertEquals("testProperty", valuePtr.getName().getName());
        Assert.assertEquals("value", valuePtr.getImmediateNode());
    }

    @Test
    public void testCreatePathWhenImmediateNodeNotNull() {
        pointer.setBaseValue("existing");
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = pointer.createPath(context);
        Assert.assertSame(pointer, result);
    }

    @Test
    public void testCreatePathWhenImmediateNodeNullAndFactorySuccess() {
        pointer.setBaseValue(null);
        pointer.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                return true;
            }
        });
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = pointer.createPath(context);
        Assert.assertSame(pointer, result);
    }

    @Test(expected = JXPathAbstractFactoryException.class)
    public void testCreatePathWhenImmediateNodeNullAndFactoryFails() {
        pointer.setBaseValue(null);
        pointer.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                return false;
            }
        });
        JXPathContext context = JXPathContext.newContext(new Object());
        pointer.createPath(context);
    }

    @Test
    public void testCreatePathWithValueExpandCollection() {
        pointer.setBaseValue(new ArrayList());
        pointer.setIndex(0); // index < length? length is 0, so index >= length -> expand
        JXPathContext context = JXPathContext.newContext(new Object());
        // We need to ensure createPath(context) is called, which will try to create object.
        // We'll set factory to succeed and then setValue.
        pointer.setFactory(new AbstractFactory() {
            @Override
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                // Simulate expansion by adding element
                ((List) parent).add("newElement");
                return true;
            }
        });
        NodePointer result = pointer.createPath(context, "testValue");
        Assert.assertSame(pointer, result);
        // After expansion, setValue should have been called with "testValue"
        // We can check that the base value list now contains "testValue" at index 0
        List list = (List) pointer.getBaseValue();
        Assert.assertEquals("testValue", list.get(0));
    }

    @Test
    public void testCreatePathWithValueNoExpand() {
        pointer.setBaseValue(Arrays.asList("old"));
        pointer.setIndex(0); // index < length
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = pointer.createPath(context, "newValue");
        Assert.assertSame(pointer, result);
        Assert.assertEquals("newValue", ((List) pointer.getBaseValue()).get(0));
    }

    @Test
    public void testCreatePathWithValueWholeCollection() {
        pointer.setBaseValue("old");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer result = pointer.createPath(context, "newValue");
        Assert.assertSame(pointer, result);
        Assert.assertEquals("newValue", pointer.getBaseValue());
    }

    @Test
    public void testCreateChildWithNameAndValue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        QName childName = new QName("child");
        NodePointer child = pointer.createChild(context, childName, 0, "childValue");
        Assert.assertNotNull(child);
        Assert.assertTrue(child instanceof TestPropertyPointer);
        TestPropertyPointer childPtr = (TestPropertyPointer) child;
        Assert.assertEquals("child", childPtr.getPropertyName());
        Assert.assertEquals(0, childPtr.getIndex());
        // The child's createPath(context, value) should have been called, setting value
        Assert.assertEquals("childValue", childPtr.getBaseValue());
    }

    @Test
    public void testCreateChildWithNullName() {
        JXPathContext context = JXPathContext.newContext(new Object());
        NodePointer child = pointer.createChild(context, null, 0, "value");
        Assert.assertNotNull(child);
        TestPropertyPointer childPtr = (TestPropertyPointer) child;
        // property name should remain unchanged (original "testProperty")
        Assert.assertEquals("testProperty", childPtr.getPropertyName());
    }

    @Test
    public void testCreateChildWithoutValue() {
        JXPathContext context = JXPathContext.newContext(new Object());
        QName childName = new QName("child");
        pointer.setBaseValue("existing");
        NodePointer child = pointer.createChild(context, childName, 0);
        Assert.assertNotNull(child);
        TestPropertyPointer childPtr = (TestPropertyPointer) child;
        Assert.assertEquals("child", childPtr.getPropertyName());
        Assert.assertEquals(0, childPtr.getIndex());
        // createPath(context) should have been called, which returns the pointer itself if node exists
        Assert.assertSame(childPtr, childPtr.createPath(context));
    }

    @Test
    public void testHashCode() {
        pointer.setPropertyIndex(1);
        pointer.setIndex(2);
        int expected = parent.hashCode() + 1 + 2;
        Assert.assertEquals(expected, pointer.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        Assert.assertTrue(pointer.equals(pointer));
    }

    @Test
    public void testEqualsDifferentClass() {
        Assert.assertFalse(pointer.equals("string"));
    }

    @Test
    public void testEqualsDifferentParent() {
        TestPropertyPointer other = new TestPropertyPointer(new TestNodePointer(new Object()), "testProperty", null, true);
        other.setPropertyIndex(pointer.getPropertyIndex());
        other.setIndex(pointer.getIndex());
        Assert.assertFalse(pointer.equals(other));
    }

    @Test
    public void testEqualsDifferentPropertyIndex() {
        TestPropertyPointer other = new TestPropertyPointer(parent, "testProperty", null, true);
        other.setPropertyIndex(99);
        Assert.assertFalse(pointer.equals(other));
    }

    @Test
    public void testEqualsDifferentPropertyName() {
        TestPropertyPointer other = new TestPropertyPointer(parent, "otherName", null, true);
        other.setPropertyIndex(pointer.getPropertyIndex());
        Assert.assertFalse(pointer.equals(other));
    }

    @Test
    public void testEqualsDifferentIndex() {
        TestPropertyPointer other = new TestPropertyPointer(parent, "testProperty", null, true);
        other.setPropertyIndex(pointer.getPropertyIndex());
        other.setIndex(5);
        pointer.setIndex(10);
        Assert.assertFalse(pointer.equals(other));
    }

    @Test
    public void testEqualsWholeCollectionTreatedAsZero() {
        TestPropertyPointer other = new TestPropertyPointer(parent, "testProperty", null, true);
        other.setPropertyIndex(pointer.getPropertyIndex());
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        other.setIndex(0);
        Assert.assertTrue(pointer.equals(other));
    }

    @Test
    public void testEqualsBothWholeCollection() {
        TestPropertyPointer other = new TestPropertyPointer(parent, "testProperty", null, true);
        other.setPropertyIndex(pointer.getPropertyIndex());
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        other.setIndex(NodePointer.WHOLE_COLLECTION);
        Assert.assertTrue(pointer.equals(other));
    }

    @Test
    public void testEqualsNullParent() {
        TestPropertyPointer p1 = new TestPropertyPointer(null, "prop", null, true);
        TestPropertyPointer p2 = new TestPropertyPointer(null, "prop", null, true);
        p1.setPropertyIndex(0);
        p2.setPropertyIndex(0);
        Assert.assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsOneNullParent() {
        TestPropertyPointer p1 = new TestPropertyPointer(null, "prop", null, true);
        TestPropertyPointer p2 = new TestPropertyPointer(parent, "prop", null, true);
        p1.setPropertyIndex(0);
        p2.setPropertyIndex(0);
        Assert.assertFalse(p1.equals(p2));
    }

    @Test
    public void testCompareChildNodePointers() {
        NodePointer child1 = new TestNodePointer("a");
        NodePointer child2 = new TestNodePointer("b");
        // Set up value pointer to return a specific comparison
        TestNodePointer valuePtr = new TestNodePointer("value") {
            @Override
            public int compareChildNodePointers(NodePointer p1, NodePointer p2) {
                return p1.getImmediateNode().toString().compareTo(p2.getImmediateNode().toString());
            }
        };
        parent.setValuePointer(valuePtr);
        int result = pointer.compareChildNodePointers(child1, child2);
        Assert.assertTrue(result < 0);
    }
}
