package org.mockito.internal.creation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockName;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MockSettingsImplTest {

    private MockSettingsImpl mockSettings;

    @Before
    public void setUp() {
        mockSettings = new MockSettingsImpl();
    }

    @Test
    public void shouldHaveDefaultStateUponCreation() {
        assertNull(mockSettings.getExtraInterfaces());
        assertNull(mockSettings.getMockName());
        assertNull(mockSettings.getSpiedInstance());
        assertNull(mockSettings.getDefaultAnswer());
        assertFalse(mockSettings.isSerializable());
    }

    @Test
    public void shouldSetSerializable() {
        MockSettings result = mockSettings.serializable();
        assertSame(mockSettings, result);
        assertTrue(mockSettings.isSerializable());
        assertArrayEquals(new Class<?>[]{Serializable.class}, mockSettings.getExtraInterfaces());
    }

    @Test
    public void shouldSetExtraInterfaces() {
        MockSettings result = mockSettings.extraInterfaces(List.class, Set.class);
        assertSame(mockSettings, result);
        assertFalse(mockSettings.isSerializable());
        assertArrayEquals(new Class<?>[]{List.class, Set.class}, mockSettings.getExtraInterfaces());
    }

    @Test
    public void shouldBeSerializableWhenExtraInterfacesContainSerializable() {
        mockSettings.extraInterfaces(List.class, Serializable.class);
        assertTrue(mockSettings.isSerializable());
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenExtraInterfacesIsNull() {
        mockSettings.extraInterfaces((Class<?>[]) null);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenExtraInterfacesIsEmpty() {
        mockSettings.extraInterfaces(new Class<?>[0]);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenExtraInterfacesContainsNull() {
        mockSettings.extraInterfaces(List.class, null);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenFirstExtraInterfaceIsNull() {
        mockSettings.extraInterfaces((Class<?>) null);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenExtraInterfacesContainsNonInterfaceClass() {
        mockSettings.extraInterfaces(String.class);
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowExceptionWhenExtraInterfacesContainsClassAlongWithInterface() {
        mockSettings.extraInterfaces(List.class, Object.class);
    }

    @Test
    public void shouldSetAndGetMockName() {
        mockSettings.name("customName");
        mockSettings.initiateMockName(List.class);

        MockName mockName = mockSettings.getMockName();
        assertNotNull(mockName);
        assertTrue(mockName.toString().contains("customName"));
    }

    @Test
    public void shouldInitiateMockNameWithoutExplicitName() {
        mockSettings.initiateMockName(List.class);

        MockName mockName = mockSettings.getMockName();
        assertNotNull(mockName);
        assertTrue(mockName.toString().contains("list"));
    }

    @Test
    public void shouldSetAndGetSpiedInstance() {
        Object instance = new Object();
        MockSettings result = mockSettings.spiedInstance(instance);

        assertSame(mockSettings, result);
        assertSame(instance, mockSettings.getSpiedInstance());
    }

    @Test
    public void shouldSetAndGetDefaultAnswer() {
        Answer<Object> answer = invocation -> "default";
        MockSettings result = mockSettings.defaultAnswer(answer);

        assertSame(mockSettings, result);
        assertSame(answer, mockSettings.getDefaultAnswer());
    }

    @Test
    public void shouldSupportMethodChaining() {
        Answer<Object> answer = invocation -> null;
        Object spied = new Object();

        MockSettings result = mockSettings
                .name("chainedMock")
                .spiedInstance(spied)
                .defaultAnswer(answer)
                .serializable();

        assertSame(mockSettings, result);
        assertSame(spied, mockSettings.getSpiedInstance());
        assertSame(answer, mockSettings.getDefaultAnswer());
        assertTrue(mockSettings.isSerializable());
    }
}
