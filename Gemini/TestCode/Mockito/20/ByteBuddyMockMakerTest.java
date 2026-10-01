package org.mockito.internal.creation.bytebuddy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.InternalMockHandler;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.SerializableMode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ByteBuddyMockMakerTest {

    private ByteBuddyMockMaker mockMaker;

    public static class SampleClass {
        public String foo() {
            return "foo";
        }
    }

    public interface SampleInterface {
        void bar();
    }

    public interface ExtraInterface {
        void extra();
    }

    @Before
    public void setUp() {
        mockMaker = new ByteBuddyMockMaker();
    }

    @SuppressWarnings("unchecked")
    private <T> MockCreationSettings<T> createSettings(final Class<T> typeToMock,
                                                       final Set<Class<?>> extraInterfaces,
                                                       final SerializableMode serializableMode) {
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("getTypeToMock".equals(name)) {
                    return typeToMock;
                } else if ("getExtraInterfaces".equals(name)) {
                    return extraInterfaces != null ? extraInterfaces : Collections.emptySet();
                } else if ("getSerializableMode".equals(name)) {
                    return serializableMode != null ? serializableMode : SerializableMode.NONE;
                } else if ("isSerializable".equals(name)) {
                    return serializableMode != null && serializableMode != SerializableMode.NONE;
                } else if ("isStripAnnotations".equals(name)) {
                    return false;
                } else if ("isStubOnly".equals(name)) {
                    return false;
                } else if ("getDefaultAnswer".equals(name)) {
                    return null;
                } else if ("toString".equals(name)) {
                    return "MockCreationSettingsProxy";
                } else if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                } else if ("equals".equals(name)) {
                    return proxy == args[0];
                }
                return defaultValue(method.getReturnType());
            }
        };

        return (MockCreationSettings<T>) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{MockCreationSettings.class},
                invocationHandler
        );
    }

    @SuppressWarnings("unchecked")
    private InternalMockHandler<Object> createInternalHandler() {
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String name = method.getName();
                if ("handle".equals(name)) {
                    return null;
                } else if ("toString".equals(name)) {
                    return "InternalMockHandlerProxy";
                } else if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                } else if ("equals".equals(name)) {
                    return proxy == args[0];
                }
                return defaultValue(method.getReturnType());
            }
        };

        return (InternalMockHandler<Object>) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{InternalMockHandler.class},
                invocationHandler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0.0f;
        }
        if (returnType == double.class) {
            return 0.0d;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }

    @Test
    public void testCreateMockClassSuccess() {
        MockCreationSettings<SampleClass> settings = createSettings(SampleClass.class, null, SerializableMode.NONE);
        InternalMockHandler<Object> handler = createInternalHandler();

        SampleClass mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleClass);
        assertTrue(mock instanceof MockMethodInterceptor.MockAccess);
        assertEquals(handler, mockMaker.getHandler(mock));
    }

    @Test
    public void testCreateMockInterfaceSuccess() {
        MockCreationSettings<SampleInterface> settings = createSettings(SampleInterface.class, null, SerializableMode.NONE);
        InternalMockHandler<Object> handler = createInternalHandler();

        SampleInterface mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleInterface);
        assertTrue(mock instanceof MockMethodInterceptor.MockAccess);
        assertEquals(handler, mockMaker.getHandler(mock));
    }

    @Test
    public void testCreateMockWithExtraInterfaces() {
        Set<Class<?>> extraInterfaces = new HashSet<Class<?>>();
        extraInterfaces.add(ExtraInterface.class);

        MockCreationSettings<SampleClass> settings = createSettings(SampleClass.class, extraInterfaces, SerializableMode.NONE);
        InternalMockHandler<Object> handler = createInternalHandler();

        SampleClass mock = mockMaker.createMock(settings, handler);

        assertNotNull(mock);
        assertTrue(mock instanceof SampleClass);
        assertTrue(mock instanceof ExtraInterface);
        assertEquals(handler, mockMaker.getHandler(mock));
    }

    @Test
    public void testCreateMockAcrossClassloadersThrowsException() {
        MockCreationSettings<SampleClass> settings = createSettings(SampleClass.class, null, SerializableMode.ACROSS_CLASSLOADERS);
        InternalMockHandler<Object> handler = createInternalHandler();

        try {
            mockMaker.createMock(settings, handler);
            fail("Expected MockitoException for ACROSS_CLASSLOADERS");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Serialization across classloaders not yet supported"));
        }
    }

    @Test
    public void testCreateMockWithNonInternalHandlerThrowsException() {
        MockCreationSettings<SampleClass> settings = createSettings(SampleClass.class, null, SerializableMode.NONE);
        MockHandler nonInternalHandler = new MockHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object handle(Invocation invocation) throws Throwable {
                return null;
            }
        };

        try {
            mockMaker.createMock(settings, nonInternalHandler);
            fail("Expected MockitoException for non-InternalMockHandler");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("At the moment you cannot provide own implementations of MockHandler"));
        }
    }

    @Test
    public void testGetHandlerReturnsNullForNonMock() {
        assertNull(mockMaker.getHandler("not a mock"));
        assertNull(mockMaker.getHandler(null));
        assertNull(mockMaker.getHandler(new SampleClass()));
    }

    @Test
    public void testResetMockSuccess() {
        MockCreationSettings<SampleClass> settings = createSettings(SampleClass.class, null, SerializableMode.NONE);
        InternalMockHandler<Object> handler1 = createInternalHandler();
        InternalMockHandler<Object> handler2 = createInternalHandler();

        SampleClass mock = mockMaker.createMock(settings, handler1);
        assertEquals(handler1, mockMaker.getHandler(mock));

        mockMaker.resetMock(mock, handler2, settings);
        assertEquals(handler2, mockMaker.getHandler(mock));
    }

    @Test
    public void testResetMockWithNonInternalHandlerThrowsException() {
        MockCreationSettings<SampleClass> settings = createSettings(SampleClass.class, null, SerializableMode.NONE);
        InternalMockHandler<Object> handler = createInternalHandler();

        SampleClass mock = mockMaker.createMock(settings, handler);

        MockHandler nonInternalHandler = new MockHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public Object handle(Invocation invocation) throws Throwable {
                return null;
            }
        };

        try {
            mockMaker.resetMock(mock, nonInternalHandler, settings);
            fail("Expected MockitoException for non-InternalMockHandler in resetMock");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("At the moment you cannot provide own implementations of MockHandler"));
        }
    }

    @Test
    public void testDescribeClassReflectively() throws Exception {
        Method describeClassMethod = ByteBuddyMockMaker.class.getDeclaredMethod("describeClass", Class.class);
        describeClassMethod.setAccessible(true);

        assertEquals("null", describeClassMethod.invoke(null, (Class<?>) null));

        String classDescription = (String) describeClassMethod.invoke(null, String.class);
        assertTrue(classDescription.contains("java.lang.String"));

        Method describeObjectMethod = ByteBuddyMockMaker.class.getDeclaredMethod("describeClass", Object.class);
        describeObjectMethod.setAccessible(true);

        assertEquals("null", describeObjectMethod.invoke(null, (Object) null));

        String objectDescription = (String) describeObjectMethod.invoke(null, "test string");
        assertTrue(objectDescription.contains("java.lang.String"));
    }

    @Test
    public void testEnsureMockIsAssignableToMockedTypeReflectively() throws Exception {
        Method ensureMethod = ByteBuddyMockMaker.class.getDeclaredMethod("ensureMockIsAssignableToMockedType", MockCreationSettings.class, Object.class);
        ensureMethod.setAccessible(true);

        MockCreationSettings<SampleClass> settings = createSettings(SampleClass.class, null, SerializableMode.NONE);
        SampleClass sampleInstance = new SampleClass();

        Object result = ensureMethod.invoke(mockMaker, settings, sampleInstance);
        assertEquals(sampleInstance, result);
    }
}
