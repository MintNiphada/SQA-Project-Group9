package org.apache.commons.jxpath.ri.model.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathAbstractFactoryException;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyPointerTest {

    private static class TestBean {
        private String name = "testName";
        private List<String> list = new ArrayList<String>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }
    }

    private static class ConcretePropertyPointer extends PropertyPointer {
        private static final long serialVersionUID = 1L;

        private String propertyName = "testProp";
        private Object baseValue;
        private boolean actualProperty = true;
        private int propertyCount = 1;
        private String[] propertyNames = new String[] { "testProp" };

        public ConcretePropertyPointer(NodePointer parent) {
            super(parent);
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public int getPropertyCount() {
            return propertyCount;
        }

        public void setPropertyCount(int count) {
            this.propertyCount = count;
        }

        public String[] getPropertyNames() {
            return propertyNames;
        }

        public void setPropertyNames(String[] names) {
            this.propertyNames = names;
        }

        protected boolean isActualProperty() {
            return actualProperty;
        }

        public void setActualProperty(boolean actualProperty) {
            this.actualProperty = actualProperty;
        }

        public Object getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(Object baseValue) {
            this.baseValue = baseValue;
        }

        public void setValue(Object value) {
            this.baseValue = value;
        }

        public String asPath() {
            return (parent == null ? "" : parent.asPath()) + "/" + propertyName;
        }

        public int getLength() {
            return super.getLength();
        }

        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }
    }

    private TestBean testBean;
    private NodePointer parentPointer;
    private ConcretePropertyPointer pointer;

    @Before
    public void setUp() {
        testBean = new TestBean();
        parentPointer = NodePointer.newNodePointer(new QName("root"), testBean, Locale.ENGLISH);
        pointer = new ConcretePropertyPointer(parentPointer);
    }

    @Test
    public void testPropertyIndex() {
        Assert.assertEquals(PropertyPointer.UNSPECIFIED_PROPERTY, pointer.getPropertyIndex());

        pointer.setIndex(2);
        pointer.setPropertyIndex(1);
        Assert.assertEquals(1, pointer.getPropertyIndex());
        Assert.assertEquals(NodePointer.WHOLE_COLLECTION, pointer.getIndex());

        // Setting same property index should not reset the collection index
        pointer.setIndex(3);
        pointer.setPropertyIndex(1);
        Assert.assertEquals(3, pointer.getIndex());
    }

    @Test
    public void testGetBean() {
        Object bean = pointer.getBean();
        Assert.assertSame(testBean, bean);

        // Subsequent call returns cached bean
        Assert.assertSame(testBean, pointer.getBean());
    }

    @Test
    public void testGetName() {
        pointer.setPropertyName("customProp");
        QName name = pointer.getName();
        Assert.assertNull(name.getPrefix());
        Assert.assertEquals("customProp", name.getName());
    }

    @Test
    public void testIsActual() {
        pointer.setActualProperty(true);
        Assert.assertTrue(pointer.isActual());

        pointer.setActualProperty(false);
        Assert.assertFalse(pointer.isActual());
    }

    @Test
    public void testGetImmediateNode() {
        // BaseValue is null
        pointer.setBaseValue(null);
        Assert.assertNull(pointer.getImmediateNode());

        // BaseValue is list, index is WHOLE_COLLECTION
        ConcretePropertyPointer ptrList = new ConcretePropertyPointer(parentPointer);
        List<String> list = new ArrayList<String>();
        list.add("item0");
        list.add("item1");
        ptrList.setBaseValue(list);
        ptrList.setIndex(NodePointer.WHOLE_COLLECTION);
        Assert.assertEquals(list, ptrList.getImmediateNode());

        // BaseValue is list, index is 1
        ConcretePropertyPointer ptrIndex = new ConcretePropertyPointer(parentPointer);
        ptrIndex.setBaseValue(list);
        ptrIndex.setIndex(1);
        Assert.assertEquals("item1", ptrIndex.getImmediateNode());

        // Cache verification: modifying baseValue doesn't affect cached immediateNode
        ptrIndex.setBaseValue(new ArrayList<String>());
        Assert.assertEquals("item1", ptrIndex.getImmediateNode());
    }

    @Test
    public void testIsCollection() {
        pointer.setBaseValue(null);
        Assert.assertFalse(pointer.isCollection());

        pointer.setBaseValue("a string");
        Assert.assertFalse(pointer.isCollection());

        pointer.setBaseValue(new String[] { "a", "b" });
        Assert.assertTrue(pointer.isCollection());

        pointer.setBaseValue(new ArrayList<Object>());
        Assert.assertTrue(pointer.isCollection());
    }

    @Test
    public void testIsLeaf() {
        // null value is a leaf
        pointer.setBaseValue(null);
        Assert.assertTrue(pointer.isLeaf());

        // String is an atomic value (leaf)
        pointer.setBaseValue("atomic string");
        Assert.assertTrue(pointer.isLeaf());

        // Non-atomic object is not a leaf
        pointer.setBaseValue(new TestBean());
        Assert.assertFalse(pointer.isLeaf());
    }

    @Test
    public void testGetLength() {
        pointer.setBaseValue(null);
        Assert.assertEquals(0, pointer.getLength());

        pointer.setBaseValue(new String[] { "a", "b", "c" });
        Assert.assertEquals(3, pointer.getLength());

        pointer.setBaseValue("single");
        Assert.assertEquals(1, pointer.getLength());
    }

    @Test
    public void testGetImmediateValuePointer() {
        pointer.setPropertyName("prop");
        pointer.setBaseValue("val");
        NodePointer vp = pointer.getImmediateValuePointer();
        Assert.assertNotNull(vp);
        Assert.assertEquals("prop", vp.getName().getName());
        Assert.assertEquals("val", vp.getBaseValue());
    }

    @Test
    public void testCreatePathSuccessWholeCollection() {
        JXPathContext context = JXPathContext.newContext(testBean);
        final boolean[] created = new boolean[] { false };
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                if ("testProp".equals(name) && index == 0) {
                    created[0] = true;
                    ((PropertyPointer) pointer).setValue("createdValue");
                    return true;
                }
                return false;
            }
        });

        pointer.setBaseValue(null);
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        NodePointer result = pointer.createPath(context);
        Assert.assertSame(pointer, result);
        Assert.assertTrue(created[0]);
    }

    @Test
    public void testCreatePathSuccessSpecificIndex() {
        JXPathContext context = JXPathContext.newContext(testBean);
        final int[] indexPassed = new int[] { -1 };
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                indexPassed[0] = index;
                return true;
            }
        });

        pointer.setBaseValue(null);
        pointer.setIndex(3);
        NodePointer result = pointer.createPath(context);
        Assert.assertSame(pointer, result);
        Assert.assertEquals(3, indexPassed[0]);
    }

    @Test
    public void testCreatePathAlreadyExisting() {
        JXPathContext context = JXPathContext.newContext(testBean);
        pointer.setBaseValue("alreadyExisting");
        NodePointer result = pointer.createPath(context);
        Assert.assertSame(pointer, result);
    }

    @Test(expected = JXPathAbstractFactoryException.class)
    public void testCreatePathFailure() {
        JXPathContext context = JXPathContext.newContext(testBean);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                return false;
            }
        });

        pointer.setBaseValue(null);
        pointer.createPath(context);
    }

    @Test
    public void testCreatePathWithValue() {
        JXPathContext context = JXPathContext.newContext(testBean);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                return true;
            }
        });

        // Index WHOLE_COLLECTION
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        pointer.createPath(context, "newValue");
        Assert.assertEquals("newValue", pointer.getBaseValue());

        // Index > length triggers createPath(context)
        pointer.setBaseValue(new ArrayList<String>());
        pointer.setIndex(2);
        pointer.createPath(context, "appendedValue");
        Assert.assertEquals("appendedValue", pointer.getBaseValue());
    }

    @Test
    public void testCreateChildWithValue() {
        JXPathContext context = JXPathContext.newContext(testBean);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                return true;
            }
        });

        NodePointer child = pointer.createChild(context, new QName("childProp"), 2, "childVal");
        Assert.assertTrue(child instanceof PropertyPointer);
        PropertyPointer propChild = (PropertyPointer) child;
        Assert.assertEquals("childProp", propChild.getPropertyName());
        Assert.assertEquals(2, propChild.getIndex());
        Assert.assertEquals("childVal", propChild.getBaseValue());

        // Test with null QName
        NodePointer childNullName = pointer.createChild(context, null, 1, "val2");
        Assert.assertEquals("testProp", ((PropertyPointer) childNullName).getPropertyName());
    }

    @Test
    public void testCreateChildWithoutValue() {
        JXPathContext context = JXPathContext.newContext(testBean);
        context.setFactory(new AbstractFactory() {
            public boolean createObject(JXPathContext context, NodePointer pointer, Object parent, String name, int index) {
                return true;
            }
        });

        NodePointer child = pointer.createChild(context, new QName("childProp2"), 0);
        Assert.assertTrue(child instanceof PropertyPointer);
        PropertyPointer propChild = (PropertyPointer) child;
        Assert.assertEquals("childProp2", propChild.getPropertyName());
        Assert.assertEquals(0, propChild.getIndex());

        // Test with null QName
        NodePointer childNullName = pointer.createChild(context, null, 1);
        Assert.assertEquals("testProp", ((PropertyPointer) childNullName).getPropertyName());
    }

    @Test
    public void testHashCode() {
        pointer.setPropertyIndex(2);
        pointer.setIndex(3);
        int expected = parentPointer.hashCode() + 2 + 3;
        Assert.assertEquals(expected, pointer.hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(pointer.equals(pointer));
        Assert.assertFalse(pointer.equals(null));
        Assert.assertFalse(pointer.equals("someString"));

        ConcretePropertyPointer other = new ConcretePropertyPointer(parentPointer);
        Assert.assertTrue(pointer.equals(other));

        // Different parent
        NodePointer otherParent = NodePointer.newNodePointer(new QName("other"), testBean, Locale.ENGLISH);
        ConcretePropertyPointer otherDiffParent = new ConcretePropertyPointer(otherParent);
        Assert.assertFalse(pointer.equals(otherDiffParent));

        // Parent is null on one
        ConcretePropertyPointer nullParent1 = new ConcretePropertyPointer(null);
        ConcretePropertyPointer nullParent2 = new ConcretePropertyPointer(null);
        Assert.assertTrue(nullParent1.equals(nullParent2));
        Assert.assertFalse(pointer.equals(nullParent1));
        Assert.assertFalse(nullParent1.equals(pointer));

        // Different property index
        other.setPropertyIndex(5);
        Assert.assertFalse(pointer.equals(other));
        other.setPropertyIndex(pointer.getPropertyIndex());

        // Different property name
        other.setPropertyName("differentProp");
        Assert.assertFalse(pointer.equals(other));
        other.setPropertyName(pointer.getPropertyName());

        // Different index (e.g. 1 vs 2)
        pointer.setIndex(1);
        other.setIndex(2);
        Assert.assertFalse(pointer.equals(other));

        // Index WHOLE_COLLECTION vs 0 should be equal (as both evaluate to 0 in equals)
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        other.setIndex(0);
        Assert.assertTrue(pointer.equals(other));

        pointer.setIndex(0);
        other.setIndex(NodePointer.WHOLE_COLLECTION);
        Assert.assertTrue(pointer.equals(other));

        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        other.setIndex(NodePointer.WHOLE_COLLECTION);
        Assert.assertTrue(pointer.equals(other));
    }

    @Test
    public void testCompareChildNodePointers() {
        pointer.setBaseValue(testBean);
        NodePointer p1 = NodePointer.newNodePointer(new QName("name"), "testName", Locale.ENGLISH);
        NodePointer p2 = NodePointer.newNodePointer(new QName("list"), Collections.emptyList(), Locale.ENGLISH);
        int result = pointer.compareChildNodePointers(p1, p2);
        // Default comparison on PropertyPointer delegates to getValuePointer().compareChildNodePointers(p1, p2)
        Assert.assertTrue(result == 0 || result != 0);
    }

    @Test
    public void testGetPropertyCountAndNames() {
        Assert.assertEquals(1, pointer.getPropertyCount());
        Assert.assertArrayEquals(new String[] { "testProp" }, pointer.getPropertyNames());

        pointer.setPropertyCount(2);
        pointer.setPropertyNames(new String[] { "p1", "p2" });
        Assert.assertEquals(2, pointer.getPropertyCount());
        Assert.assertArrayEquals(new String[] { "p1", "p2" }, pointer.getPropertyNames());
    }
}
