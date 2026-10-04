package org.mockito.internal.creation;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.stubbing.Answer;
import org.mockito.internal.util.MockName;
import java.io.Serializable;

public class MockSettingsImplTest {

    @Test
    public void testSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertSame(settings, settings.serializable());
        assertTrue(settings.isSerializable());
        Class<?>[] interfaces = settings.getExtraInterfaces();
        assertNotNull(interfaces);
        assertEquals(1, interfaces.length);
        assertEquals(Serializable.class, interfaces[0]);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesNull() {
        new MockSettingsImpl().extraInterfaces((Class<?>[]) null);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesEmpty() {
        new MockSettingsImpl().extraInterfaces();
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesNullElement() {
        new MockSettingsImpl().extraInterfaces(Serializable.class, null);
    }

    @Test(expected = MockitoException.class)
    public void testExtraInterfacesNonInterface() {
        new MockSettingsImpl().extraInterfaces(Object.class);
    }

    @Test
    public void testExtraInterfacesValid() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Class<?>[] ifaces = new Class<?>[] { Serializable.class, Cloneable.class };
        assertSame(settings, settings.extraInterfaces(ifaces));
        assertArrayEquals(ifaces, settings.getExtraInterfaces());
    }

    @Test
    public void testGetMockNameInitiallyNull() {
        assertNull(new MockSettingsImpl().getMockName());
    }

    @Test
    public void testInitiateMockNameWithName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("myMock");
        settings.initiateMockName(String.class);
        MockName mockName = settings.getMockName();
        assertNotNull(mockName);
        assertEquals("myMock", mockName.toString());
    }

    @Test
    public void testInitiateMockNameWithoutName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.initiateMockName(Integer.class);
        MockName mockName = settings.getMockName();
        assertNotNull(mockName);
        assertEquals("Integer", mockName.toString());
    }

    @Test
    public void testName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertSame(settings, settings.name("test"));
        // name is used later in initiateMockName, verify via mockName
        settings.initiateMockName(Object.class);
        assertEquals("test", settings.getMockName().toString());
    }

    @Test
    public void testSpiedInstance() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Object spy = new Object();
        assertSame(settings, settings.spiedInstance(spy));
        assertSame(spy, settings.getSpiedInstance());
    }

    @Test
    public void testSpiedInstanceNull() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.spiedInstance(null);
        assertNull(settings.getSpiedInstance());
    }

    @Test
    public void testDefaultAnswer() {
        MockSettingsImpl settings = new MockSettingsImpl();
        Answer<Object> answer = invocation -> null;
        assertSame(settings, settings.defaultAnswer(answer));
        assertSame(answer, settings.getDefaultAnswer());
    }

    @Test
    public void testDefaultAnswerNull() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(null);
        assertNull(settings.getDefaultAnswer());
    }

    @Test
    public void testIsSerializableTrueViaSerializableMethod() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        assertTrue(settings.isSerializable());
    }

    @Test
    public void testIsSerializableTrueViaExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);
        assertTrue(settings.isSerializable());
    }

    @Test
    public void testIsSerializableFalseWhenExtraInterfacesNull() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertFalse(settings.isSerializable());
    }

    @Test
    public void testIsSerializableFalseWhenExtraInterfacesDoesNotContainSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Cloneable.class);
        assertFalse(settings.isSerializable());
    }

    @Test
    public void testIsSerializableFalseWhenExtraInterfacesEmptyAfterException() {
        // Cannot set empty directly because it throws, but we can test after a valid set then overwrite? 
        // Actually we can't set empty, but we can test that after construction it's false.
        // Already covered by testIsSerializableFalseWhenExtraInterfacesNull.
    }

    @Test
    public void testChaining() {
        MockSettingsImpl settings = new MockSettingsImpl();
        assertSame(settings, settings.name("chain"));
        assertSame(settings, settings.serializable());
        assertSame(settings, settings.spiedInstance(new Object()));
        assertSame(settings, settings.defaultAnswer(invocation -> null));
        assertSame(settings, settings.extraInterfaces(Cloneable.class));
    }

    @Test
    public void testGetExtraInterfacesAfterMultipleCalls() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);
        settings.extraInterfaces(Cloneable.class);
        Class<?>[] interfaces = settings.getExtraInterfaces();
        assertEquals(1, interfaces.length);
        assertEquals(Cloneable.class, interfaces[0]);
    }
}
