package org.mockito.internal.util;

import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.cglib.proxy.Callback;
import org.mockito.cglib.proxy.Factory;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class MockUtilTest {

    private MockUtil mockUtil = new MockUtil();

    @Test
    public void testDefaultConstructor() {
        assertNotNull(new MockUtil());
    }

    @Test
    public void testCustomConstructor() {
        MockCreationValidator validator = new MockCreationValidator();
        MockUtil util = new MockUtil(validator);
        assertNotNull(util);
    }

    @Test
    public void testCreateMockBasic() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertNotNull(mockUtil.getMockHandler(mock));
    }

    @Test
    public void testCreateMockWithExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mock instanceof Serializable);
    }

    @Test
    public void testCreateMockSerializable() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        Foo mock = mockUtil.createMock(Foo.class, settings);
        assertTrue(mock instanceof Serializable);
    }

    @Test
    public void testCreateMockSerializableAndExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Cloneable.class).serializable();
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mock instanceof Serializable);
        assertTrue(mock instanceof Cloneable);
    }

    @Test
    public void testCreateMockWithSpiedInstance() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<String> spied = new ArrayList<String>();
        spied.add("test");
        settings.spiedInstance(spied);
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(expected = NotAMockException.class)
    public void testCreateMockValidationTypeThrows() {
        MockCreationValidator validator = new MockCreationValidator() {
            @Override
            public void validateType(Class<?> classToMock) {
                throw new NotAMockException("type invalid");
            }
        };
        MockUtil util = new MockUtil(validator);
        MockSettingsImpl settings = new MockSettingsImpl();
        util.createMock(List.class, settings);
    }

    @Test(expected = NotAMockException.class)
    public void testCreateMockValidationExtraInterfacesThrows() {
        MockCreationValidator validator = new MockCreationValidator() {
            @Override
            public void validateExtraInterfaces(Class<?> classToMock, Class<?>[] extraInterfaces) {
                throw new NotAMockException("extra interfaces invalid");
            }
        };
        MockUtil util = new MockUtil(validator);
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Serializable.class);
        util.createMock(List.class, settings);
    }

    @Test(expected = NotAMockException.class)
    public void testCreateMockValidationMockedTypeThrows() {
        MockCreationValidator validator = new MockCreationValidator() {
            @Override
            public void validateMockedType(Class<?> classToMock, Object spiedInstance) {
                throw new NotAMockException("mocked type invalid");
            }
        };
        MockUtil util = new MockUtil(validator);
        MockSettingsImpl settings = new MockSettingsImpl();
        util.createMock(List.class, settings);
    }

    @Test
    public void testResetMockOnMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);
        mockUtil.resetMock(mock);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(expected = NotAMockException.class)
    public void testResetMockNull() {
        mockUtil.resetMock(null);
    }

    @Test(expected = NotAMockException.class)
    public void testResetMockNonMock() {
        mockUtil.resetMock("not a mock");
    }

    @Test(expected = NotAMockException.class)
    public void testGetMockHandlerNull() {
        mockUtil.getMockHandler(null);
    }

    @Test(expected = NotAMockException.class)
    public void testGetMockHandlerNonMock() {
        mockUtil.getMockHandler("not a mock");
    }

    @Test
    public void testGetMockHandlerWithMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mockUtil.getMockHandler(mock));
    }

    @Test
    public void testIsMockNull() {
        assertFalse(mockUtil.isMock(null));
    }

    @Test
    public void testIsMockNonMock() {
        assertFalse(mockUtil.isMock("not a mock"));
    }

    @Test
    public void testIsMockMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test
    public void testGetMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("myMock");
        List<?> mock = mockUtil.createMock(List.class, settings);
        assertNotNull(mockUtil.getMockName(mock));
        assertEquals("myMock", mockUtil.getMockName(mock).toString());
    }

    @Test
    public void testIsMockWithFactoryButNonMethodInterceptorFilter() {
        FakeFactory fake = new FakeFactory();
        fake.setCallback(0, new Callback() {});
        assertFalse(mockUtil.isMock(fake));
    }

    @Test(expected = NotAMockException.class)
    public void testGetMockHandlerWithFactoryButNonMethodInterceptorFilter() {
        FakeFactory fake = new FakeFactory();
        fake.setCallback(0, new Callback() {});
        mockUtil.getMockHandler(fake);
    }

    static class Foo {}

    static class FakeFactory implements Factory {
        private Callback[] callbacks = new Callback[1];

        @Override
        public Callback getCallback(int index) {
            return callbacks[index];
        }

        @Override
        public void setCallback(int index, Callback callback) {
            callbacks[index] = callback;
        }

        @Override
        public Callback[] getCallbacks() {
            return callbacks;
        }

        @Override
        public void setCallbacks(Callback[] callbacks) {
            this.callbacks = callbacks;
        }

        @Override
        public Object newInstance(Callback callback) {
            return null;
        }

        @Override
        public Object newInstance(Callback[] callbacks) {
            return null;
        }

        @Override
        public Object newInstance(Class[] types, Object[] args, Callback[] callbacks) {
            return null;
        }
    }
}
