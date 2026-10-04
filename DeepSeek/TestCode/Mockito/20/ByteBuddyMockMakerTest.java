package org.mockito.internal.creation.bytebuddy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.creation.instance.ClassInstantiator;
import org.mockito.internal.creation.instance.InstantiationException;
import org.mockito.invocation.MockHandler;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.SerializableMode;

public class ByteBuddyMockMakerTest {

    private ByteBuddyMockMaker maker;
    private CachingMockBytecodeGenerator cachingMock;
    private ClassInstantiator instantiator;

    // helper interfaces/classes
    public interface Foo {}
    public static class FooImpl implements Foo, MockMethodInterceptor.MockAccess {
        private MockMethodInterceptor interceptor;
        @Override
        public MockMethodInterceptor getMockitoInterceptor() { return interceptor; }
        @Override
        public void setMockitoInterceptor(MockMethodInterceptor interceptor) { this.interceptor = interceptor; }
    }
    public static class MockAccessStub implements MockMethodInterceptor.MockAccess {
        private MockMethodInterceptor interceptor;
        @Override
        public MockMethodInterceptor getMockitoInterceptor() { return interceptor; }
        @Override
        public void setMockitoInterceptor(MockMethodInterceptor interceptor) { this.interceptor = interceptor; }
    }

    @Before
    public void setUp() throws Exception {
        maker = new ByteBuddyMockMaker();

        Field cachingField = ByteBuddyMockMaker.class.getDeclaredField("cachingMockBytecodeGenerator");
        cachingField.setAccessible(true);
        cachingMock = mock(CachingMockBytecodeGenerator.class);
        cachingField.set(maker, cachingMock);

        Field instantiatorField = ByteBuddyMockMaker.class.getDeclaredField("classInstantiator");
        instantiatorField.setAccessible(true);
        instantiator = mock(ClassInstantiator.class);
        instantiatorField.set(maker, instantiator);
    }

    @Test
    public void should_create_instance() {
        ByteBuddyMockMaker m = new ByteBuddyMockMaker();
        assertNotNull(m);
    }

    @Test(expected = MockitoException.class)
    public void createMock_with_serialization_across_classloaders_throws() {
        MockCreationSettings settings = mock(MockCreationSettings.class);
        when(settings.getSerializableMode()).thenReturn(SerializableMode.ACROSS_CLASSLOADERS);
        maker.createMock(settings, mock(MockHandler.class));
    }

    @Test
    public void createMock_success() {
        MockCreationSettings<Foo> settings = mock(MockCreationSettings.class);
        when(settings.getTypeToMock()).thenReturn(Foo.class);
        when(settings.getExtraInterfaces()).thenReturn(Collections.<Class<?>>emptyList());
        when(settings.getSerializableMode()).thenReturn(SerializableMode.NONE);

        InternalMockHandler handler = mock(InternalMockHandler.class);

        when(cachingMock.get(Foo.class, settings.getExtraInterfaces())).thenReturn(FooImpl.class);

        FooImpl fooInstance = new FooImpl();
        when(instantiator.instantiate(FooImpl.class)).thenReturn(fooInstance);

        Foo result = maker.createMock(settings, handler);

        assertSame(fooInstance, result);
        assertNotNull(fooInstance.getMockitoInterceptor());
        assertEquals(handler, fooInstance.getMockitoInterceptor().getMockHandler());
    }

    @Test(expected = MockitoException.class)
    public void createMock_non_internal_handler_throws() {
        MockCreationSettings settings = mock(MockCreationSettings.class);
        when(settings.getSerializableMode()).thenReturn(SerializableMode.NONE);
        when(cachingMock.get(any(), any())).thenReturn(FooImpl.class);
        when(instantiator.instantiate(any())).thenReturn(new FooImpl());

        MockHandler handler = mock(MockHandler.class);
        maker.createMock(settings, handler);
    }

    @Test
    public void createMock_classCastException_wraps() {
        MockCreationSettings<Foo> settings = mock(MockCreationSettings.class);
        when(settings.getTypeToMock()).thenReturn(Foo.class);
        when(settings.getExtraInterfaces()).thenReturn(Collections.<Class<?>>emptyList());
        when(settings.getSerializableMode()).thenReturn(SerializableMode.NONE);

        InternalMockHandler handler = mock(InternalMockHandler.class);

        when(cachingMock.get(Foo.class, Collections.<Class<?>>emptyList())).thenReturn(Object.class);
        when(instantiator.instantiate(Object.class)).thenReturn("not a Foo instance");

        try {
            maker.createMock(settings, handler);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("ClassCastException occurred while creating the mockito mock"));
        }
    }

    @Test
    public void createMock_instantiationException_wraps() {
        MockCreationSettings settings = mock(MockCreationSettings.class);
        when(settings.getSerializableMode()).thenReturn(SerializableMode.NONE);
        when(cachingMock.get(any(), any())).thenReturn(FooImpl.class);
        when(instantiator.instantiate(FooImpl.class)).thenThrow(new InstantiationException("fail"));

        try {
            maker.createMock(settings, mock(InternalMockHandler.class));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Unable to create mock instance of type"));
        }
    }

    @Test
    public void getHandler_non_mockAccess_returns_null() {
        assertNull(maker.getHandler(new Object()));
        assertNull(maker.getHandler(null));
    }

    @Test
    public void getHandler_returns_handler() {
        MockAccessStub mock = new MockAccessStub();
        MockMethodInterceptor interceptor = mock(MockMethodInterceptor.class);
        MockHandler expectedHandler = mock(MockHandler.class);
        when(interceptor.getMockHandler()).thenReturn(expectedHandler);
        mock.setMockitoInterceptor(interceptor);

        MockHandler result = maker.getHandler(mock);
        assertSame(expectedHandler, result);
    }

    @Test
    public void resetMock_sets_new_interceptor() {
        MockAccessStub mock = new MockAccessStub();
        InternalMockHandler newHandler = mock(InternalMockHandler.class);
        MockCreationSettings settings = mock(MockCreationSettings.class);

        maker.resetMock(mock, newHandler, settings);

        MockMethodInterceptor interceptor = mock.getMockitoInterceptor();
        assertNotNull(interceptor);
        assertSame(newHandler, interceptor.getMockHandler());
    }

    @Test(expected = MockitoException.class)
    public void resetMock_non_internal_handler_throws() {
        MockAccessStub mock = new MockAccessStub();
        MockHandler handler = mock(MockHandler.class);
        maker.resetMock(mock, handler, mock(MockCreationSettings.class));
    }
}
