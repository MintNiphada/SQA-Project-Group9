package org.mockito.internal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.withSettings;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.cglib.proxy.Enhancer;
import org.mockito.cglib.proxy.MethodInterceptor;
import org.mockito.cglib.proxy.MethodProxy;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;

public class MockUtilTest {

    private MockUtil mockUtil;

    @Before
    public void setUp() {
        mockUtil = new MockUtil();
    }

    public static class SampleClass {
        private String name = "default";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testDefaultConstructor() {
        MockUtil util = new MockUtil();
        assertNotNull(util);
    }

    @Test
    public void testCustomCreationValidatorConstructor() {
        CreationValidator validator = new CreationValidator();
        MockUtil util = new MockUtil(validator);
        assertNotNull(util);
    }

    @Test
    public void testCreateMockForInterface() {
        MockSettingsImpl settings = (MockSettingsImpl) withSettings();
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertNotNull(mockUtil.getMockHandler(mock));
    }

    @Test
    public void testCreateMockForClass() {
        MockSettingsImpl settings = (MockSettingsImpl) withSettings();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertEquals("sampleClass", mockUtil.getMockName(mock).toString());
    }

    @Test
    public void testCreateMockWithExtraInterfaces() {
        MockSettingsImpl settings = (MockSettingsImpl) withSettings().extraInterfaces(Serializable.class);
        List<?> mock = mockUtil.createMock(List.class, settings);

        assertNotNull(mock);
        assertTrue(mock instanceof Serializable);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test
    public void testCreateMockWithSpiedInstance() {
        SampleClass original = new SampleClass();
        original.setName("customName");

        MockSettingsImpl settings = (MockSettingsImpl) withSettings().spiedInstance(original);
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertEquals("customName", mock.getName());
    }

    @Test
    public void testIsMockReturnsFalseForNull() {
        assertFalse(mockUtil.isMock(null));
    }

    @Test
    public void testIsMockReturnsFalseForRegularObject() {
        assertFalse(mockUtil.isMock("regularString"));
        assertFalse(mockUtil.isMock(new Object()));
        assertFalse(mockUtil.isMock(new ArrayList<Object>()));
    }

    @Test
    public void testIsMockReturnsFalseForNonMockitoCglibProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                return proxy.invokeSuper(obj, args);
            }
        });
        Object nonMockitoProxy = enhancer.create();

        assertFalse(mockUtil.isMock(nonMockitoProxy));
    }

    @Test
    public void testGetMockHandlerSuccess() {
        MockSettingsImpl settings = (MockSettingsImpl) withSettings();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        MockHandlerInterface<SampleClass> handler = mockUtil.getMockHandler(mock);
        assertNotNull(handler);
        assertSame(settings, handler.getMockSettings());
    }

    @Test
    public void testGetMockHandlerThrowsExceptionForNull() {
        try {
            mockUtil.getMockHandler(null);
            fail("Should throw NotAMockException for null argument");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is null!"));
        }
    }

    @Test
    public void testGetMockHandlerThrowsExceptionForNonMock() {
        try {
            mockUtil.getMockHandler("notAMock");
            fail("Should throw NotAMockException for non-mock argument");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is: class java.lang.String"));
        }
    }

    @Test
    public void testGetMockHandlerThrowsExceptionForNonMockitoCglibProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                return proxy.invokeSuper(obj, args);
            }
        });
        Object nonMockitoProxy = enhancer.create();

        try {
            mockUtil.getMockHandler(nonMockitoProxy);
            fail("Should throw NotAMockException for non-Mockito CGLIB proxy");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is:"));
        }
    }

    @Test
    public void testGetMockName() {
        MockSettingsImpl settings = (MockSettingsImpl) withSettings().name("customMockName");
        List<?> mock = mockUtil.createMock(List.class, settings);

        MockName mockName = mockUtil.getMockName(mock);
        assertNotNull(mockName);
        assertEquals("customMockName", mockName.toString());
    }

    @Test
    public void testResetMock() {
        MockSettingsImpl settings = (MockSettingsImpl) withSettings();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        MockHandlerInterface<SampleClass> oldHandler = mockUtil.getMockHandler(mock);
        assertNotNull(oldHandler);

        mockUtil.resetMock(mock);

        MockHandlerInterface<SampleClass> newHandler = mockUtil.getMockHandler(mock);
        assertNotNull(newHandler);
        // Resetting changes the handler instance
        assertTrue(oldHandler != newHandler);
        assertTrue(mockUtil.isMock(mock));
    }

    @Test(expected = NotAMockException.class)
    public void testResetMockThrowsExceptionForNull() {
        mockUtil.resetMock(null);
    }

    @Test(expected = NotAMockException.class)
    public void testResetMockThrowsExceptionForNonMock() {
        mockUtil.resetMock(new Object());
    }
}
