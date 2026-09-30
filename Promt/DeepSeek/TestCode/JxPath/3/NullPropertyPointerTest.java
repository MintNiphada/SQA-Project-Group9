package org.apache.commons.jxpath.ri.model.beans;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathInvalidAccessException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.Before;
import org.junit.Test;

public class NullPropertyPointerTest {

    private NullPropertyPointer pointer;
    private NodePointer parentMock;
    private JXPathContext contextMock;

    @Before
    public void setUp() {
        parentMock = mock(NodePointer.class);
        pointer = new NullPropertyPointer(parentMock);
        contextMock = mock(JXPathContext.class);
    }

    @Test
    public void testConstructor() {
        assertEquals("*", pointer.getPropertyName());
        assertFalse(pointer.isActual());
    }

    @Test
    public void testGetName() {
        QName name = pointer.getName();
        assertEquals("*", name.getName());
    }

    @Test
    public void testSetPropertyIndex() {
        pointer.setPropertyIndex(5);
        // no exception, nothing to assert
    }

    @Test
    public void testGetLength() {
        assertEquals(0, pointer.getLength());
    }

    @Test
    public void testGetBaseValue() {
        assertNull(pointer.getBaseValue());
    }

    @Test
    public void testGetImmediateNode() {
        assertNull(pointer.getImmediateNode());
    }

    @Test
    public void testIsLeaf() {
        assertTrue(pointer.isLeaf());
    }

    @Test
    public void testGetValuePointer() {
        NodePointer valuePointer = pointer.getValuePointer();
        assertTrue(valuePointer instanceof NullPointer);
        assertEquals(pointer, valuePointer.getParent());
        assertEquals("*", valuePointer.getName().getName());
    }

    @Test
    public void testIsActualProperty() {
        assertFalse(pointer.isActualProperty());
    }

    @Test
    public void testIsActual() {
        assertFalse(pointer.isActual());
    }

    @Test
    public void testIsContainer() {
        assertTrue(pointer.isContainer());
    }

    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValueNullParent() {
        NullPropertyPointer nullParentPointer = new NullPropertyPointer(null);
        nullParentPointer.setValue("value");
    }

    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValueParentContainer() {
        when(parentMock.isContainer()).thenReturn(true);
        pointer.setValue("value");
    }

    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValueParentNotContainerNotPropertyOwner() {
        when(parentMock.isContainer()).thenReturn(false);
        pointer.setValue("value");
    }

    @Test
    public void testSetValueParentPropertyOwnerDynamicSupported() {
        PropertyOwnerPointer propertyOwnerMock = mock(PropertyOwnerPointer.class);
        PropertyPointer propertyPointerMock = mock(PropertyPointer.class);
        when(propertyOwnerMock.isContainer()).thenReturn(false);
        when(propertyOwnerMock.isDynamicPropertyDeclarationSupported()).thenReturn(true);
        when(propertyOwnerMock.getPropertyPointer()).thenReturn(propertyPointerMock);

        NullPropertyPointer pointerWithOwner = new NullPropertyPointer(propertyOwnerMock);
        pointerWithOwner.setPropertyName("testProp");
        pointerWithOwner.setValue("newValue");

        verify(propertyPointerMock).setPropertyName("testProp");
        verify(propertyPointerMock).setValue("newValue");
    }

    @Test(expected = JXPathInvalidAccessException.class)
    public void testSetValueParentPropertyOwnerDynamicNotSupported() {
        PropertyOwnerPointer propertyOwnerMock = mock(PropertyOwnerPointer.class);
        when(propertyOwnerMock.isContainer()).thenReturn(false);
        when(propertyOwnerMock.isDynamicPropertyDeclarationSupported()).thenReturn(false);

        NullPropertyPointer pointerWithOwner = new NullPropertyPointer(propertyOwnerMock);
        pointerWithOwner.setValue("value");
    }

    @Test
    public void testCreatePathAttributeTrue() {
        // override isAttribute to return true
        NullPropertyPointer attrPointer = new NullPropertyPointer(parentMock) {
            @Override
            public boolean isAttribute() {
                return true;
            }
        };
        NodePointer newParentMock = mock(NodePointer.class);
        when(parentMock.createPath(contextMock)).thenReturn(newParentMock);
        NodePointer attributeMock = mock(NodePointer.class);
        when(newParentMock.createAttribute(eq(contextMock), any(QName.class))).thenReturn(attributeMock);

        NodePointer result = attrPointer.createPath(contextMock);
        assertSame(attributeMock, result);
        verify(newParentMock).createAttribute(eq(contextMock), eq(attrPointer.getName()));
    }

    @Test
    public void testCreatePathAttributeFalseNewParentNotPropertyOwner() {
        NodePointer newParentMock = mock(NodePointer.class);
        when(parentMock.createPath(contextMock)).thenReturn(newParentMock);
        NodePointer childMock = mock(NodePointer.class);
        when(newParentMock.createChild(eq(contextMock), any(QName.class), eq(pointer.getIndex()))).thenReturn(childMock);

        NodePointer result = pointer.createPath(contextMock);
        assertSame(childMock, result);
        verify(newParentMock).createChild(eq(contextMock), eq(pointer.getName()), eq(pointer.getIndex()));
    }

    @Test
    public void testCreatePathAttributeFalseNewParentPropertyOwner() {
        PropertyOwnerPointer propertyOwnerMock = mock(PropertyOwnerPointer.class);
        when(parentMock.createPath(contextMock)).thenReturn(propertyOwnerMock);
        PropertyPointer propertyPointerMock = mock(PropertyPointer.class);
        when(propertyOwnerMock.getPropertyPointer()).thenReturn(propertyPointerMock);
        NodePointer childMock = mock(NodePointer.class);
        when(propertyPointerMock.createChild(eq(contextMock), any(QName.class), eq(pointer.getIndex()))).thenReturn(childMock);

        NodePointer result = pointer.createPath(contextMock);
        assertSame(childMock, result);
        verify(propertyOwnerMock).getPropertyPointer();
        verify(propertyPointerMock).createChild(eq(contextMock), eq(pointer.getName()), eq(pointer.getIndex()));
    }

    @Test
    public void testCreatePathWithValueAttributeTrue() {
        NullPropertyPointer attrPointer = new NullPropertyPointer(parentMock) {
            @Override
            public boolean isAttribute() {
                return true;
            }
        };
        NodePointer newParentMock = mock(NodePointer.class);
        when(parentMock.createPath(contextMock)).thenReturn(newParentMock);
        NodePointer attributeMock = mock(NodePointer.class);
        when(newParentMock.createAttribute(eq(contextMock), any(QName.class))).thenReturn(attributeMock);

        Object value = "testValue";
        NodePointer result = attrPointer.createPath(contextMock, value);
        assertSame(attributeMock, result);
        verify(attributeMock).setValue(value);
    }

    @Test
    public void testCreatePathWithValueAttributeFalseNewParentNotPropertyOwner() {
        NodePointer newParentMock = mock(NodePointer.class);
        when(parentMock.createPath(contextMock)).thenReturn(newParentMock);
        NodePointer childMock = mock(NodePointer.class);
        Object value = "testValue";
        when(newParentMock.createChild(eq(contextMock), any(QName.class), eq(pointer.getIndex()), eq(value))).thenReturn(childMock);

        NodePointer result = pointer.createPath(contextMock, value);
        assertSame(childMock, result);
        verify(newParentMock).createChild(eq(contextMock), eq(pointer.getName()), eq(pointer.getIndex()), eq(value));
    }

    @Test
    public void testCreatePathWithValueAttributeFalseNewParentPropertyOwner() {
        PropertyOwnerPointer propertyOwnerMock = mock(PropertyOwnerPointer.class);
        when(parentMock.createPath(contextMock)).thenReturn(propertyOwnerMock);
        PropertyPointer propertyPointerMock = mock(PropertyPointer.class);
        when(propertyOwnerMock.getPropertyPointer()).thenReturn(propertyPointerMock);
        NodePointer childMock = mock(NodePointer.class);
        Object value = "testValue";
        when(propertyPointerMock.createChild(eq(contextMock), any(QName.class), eq(pointer.getIndex()), eq(value))).thenReturn(childMock);

        NodePointer result = pointer.createPath(contextMock, value);
        assertSame(childMock, result);
        verify(propertyOwnerMock).getPropertyPointer();
        verify(propertyPointerMock).createChild(eq(contextMock), eq(pointer.getName()), eq(pointer.getIndex()), eq(value));
    }

    @Test
    public void testCreateChild() {
        NodePointer pathResultMock = mock(NodePointer.class);
        NullPropertyPointer spyPointer = spy(pointer);
        doReturn(pathResultMock).when(spyPointer).createPath(contextMock);
        QName childName = new QName("child");
        int index = 0;
        NodePointer childMock = mock(NodePointer.class);
        when(pathResultMock.createChild(contextMock, childName, index)).thenReturn(childMock);

        NodePointer result = spyPointer.createChild(contextMock, childName, index);
        assertSame(childMock, result);
        verify(pathResultMock).createChild(contextMock, childName, index);
    }

    @Test
    public void testCreateChildWithValue() {
        NodePointer pathResultMock = mock(NodePointer.class);
        NullPropertyPointer spyPointer = spy(pointer);
        doReturn(pathResultMock).when(spyPointer).createPath(contextMock);
        QName childName = new QName("child");
        int index = 0;
        Object value = "value";
        NodePointer childMock = mock(NodePointer.class);
        when(pathResultMock.createChild(contextMock, childName, index, value)).thenReturn(childMock);

        NodePointer result = spyPointer.createChild(contextMock, childName, index, value);
        assertSame(childMock, result);
        verify(pathResultMock).createChild(contextMock, childName, index, value);
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("*", pointer.getPropertyName());
        pointer.setPropertyName("foo");
        assertEquals("foo", pointer.getPropertyName());
    }

    @Test
    public void testSetPropertyName() {
        pointer.setPropertyName("bar");
        assertEquals("bar", pointer.getPropertyName());
    }

    @Test
    public void testSetNameAttributeValue() {
        pointer.setNameAttributeValue("attr");
        assertEquals("attr", pointer.getPropertyName());
        // byNameAttribute should be true, verified via asPath
    }

    @Test
    public void testIsCollectionWholeCollection() {
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        assertFalse(pointer.isCollection());
    }

    @Test
    public void testIsCollectionNotWholeCollection() {
        pointer.setIndex(0);
        assertTrue(pointer.isCollection());
    }

    @Test
    public void testGetPropertyCount() {
        assertEquals(0, pointer.getPropertyCount());
    }

    @Test
    public void testGetPropertyNames() {
        assertArrayEquals(new String[0], pointer.getPropertyNames());
    }

    @Test
    public void testAsPathByNameAttributeFalse() {
        when(parentMock.asPath()).thenReturn("/parent");
        pointer.setPropertyName("foo");
        // byNameAttribute is false by default
        String path = pointer.asPath();
        assertTrue(path.startsWith("/parent"));
        assertTrue(path.contains("foo"));
        assertFalse(path.contains("[@name='"));
    }

    @Test
    public void testAsPathByNameAttributeTrueNoIndex() {
        when(parentMock.asPath()).thenReturn("/parent");
        pointer.setNameAttributeValue("foo");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        String path = pointer.asPath();
        assertEquals("/parent[@name='foo']", path);
    }

    @Test
    public void testAsPathByNameAttributeTrueWithIndex() {
        when(parentMock.asPath()).thenReturn("/parent");
        pointer.setNameAttributeValue("foo");
        pointer.setIndex(2); // index 2 -> [3]
        String path = pointer.asPath();
        assertEquals("/parent[@name='foo'][3]", path);
    }

    @Test
    public void testAsPathEscapeSingleQuote() {
        when(parentMock.asPath()).thenReturn("/parent");
        pointer.setNameAttributeValue("it's");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        String path = pointer.asPath();
        assertEquals("/parent[@name='it&apos;s']", path);
    }

    @Test
    public void testAsPathEscapeDoubleQuote() {
        when(parentMock.asPath()).thenReturn("/parent");
        pointer.setNameAttributeValue("say \"hello\"");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        String path = pointer.asPath();
        assertEquals("/parent[@name='say &quot;hello&quot;']", path);
    }

    @Test
    public void testAsPathEscapeBothQuotes() {
        when(parentMock.asPath()).thenReturn("/parent");
        pointer.setNameAttributeValue("a'b\"c");
        pointer.setIndex(NodePointer.WHOLE_COLLECTION);
        String path = pointer.asPath();
        assertEquals("/parent[@name='a&apos;b&quot;c']", path);
    }
}
