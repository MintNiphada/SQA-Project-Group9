package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockCreationValidator;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ReturnsDeepStubsTest {

    @Mock
    private InvocationOnMock invocation;

    @Mock
    private GenericMetadataSupport returnTypeGenericMetadata;

    @Mock
    private InternalMockHandler<Object> mockHandler;

    @Mock
    private InvocationContainerImpl container;

    @Mock
    private StubbedInvocationMatcher stubbedInvocationMatcher;

    @Mock
    private CreationSettings mockSettings;

    @Mock
    private Method method;

    private ReturnsDeepStubs returnsDeepStubs = new ReturnsDeepStubs();

    @Test
    public void testAnswerWhenTypeNotMockable() throws Throwable {
        try (MockedConstruction<MockCreationValidator> mockedValidator =
                     mockConstruction(MockCreationValidator.class,
                             (mock, context) -> when(mock.isTypeMockable(String.class)).thenReturn(false));
             MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class);
             MockedStatic<GenericMetadataSupport> mockedMetadata = mockStatic(GenericMetadataSupport.class)) {

            when(invocation.getMock()).thenReturn(new Object());
            mockedMetadata.when(() -> GenericMetadataSupport.inferFrom(null))
                    .thenReturn(returnTypeGenericMetadata);
            when(returnTypeGenericMetadata.resolveGenericReturnType(method)).thenReturn(returnTypeGenericMetadata);
            when(returnTypeGenericMetadata.rawType()).thenReturn(String.class);
            when(invocation.getMethod()).thenReturn(method);

            Object result = returnsDeepStubs.answer(invocation);
            assertEquals(new ReturnsEmptyValues().returnValueFor(String.class), result);
        }
    }

    @Test
    public void testAnswerWhenTypeMockableAndNoStubbedMatch() throws Throwable {
        try (MockedConstruction<MockCreationValidator> mockedValidator =
                     mockConstruction(MockCreationValidator.class,
                             (mock, context) -> when(mock.isTypeMockable(Object.class)).thenReturn(true));
             MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                     (mock, context) -> {
                         when(mock.getMockHandler(invocation.getMock())).thenReturn(mockHandler);
                         when(mockHandler.getInvocationContainer()).thenReturn(container);
                         when(mockHandler.getMockSettings()).thenReturn(mockSettings);
                         when(mockSettings.getTypeToMock()).thenReturn(Object.class);
                     });
             MockedStatic<GenericMetadataSupport> mockedMetadata = mockStatic(GenericMetadataSupport.class)) {

            when(invocation.getMock()).thenReturn(new Object());
            mockedMetadata.when(() -> GenericMetadataSupport.inferFrom(Object.class))
                    .thenReturn(returnTypeGenericMetadata);
            when(returnTypeGenericMetadata.resolveGenericReturnType(method)).thenReturn(returnTypeGenericMetadata);
            when(returnTypeGenericMetadata.rawType()).thenReturn(Object.class);
            when(invocation.getMethod()).thenReturn(method);
            when(method.getReturnType()).thenReturn(Object.class);
            when(container.getStubbedInvocations()).thenReturn(Collections.emptyList());

            Object result = returnsDeepStubs.answer(invocation);
            assertEquals(Object.class, result.getClass());
        }
    }

    @Test
    public void testAnswerWhenTypeMockableAndStubbedMatch() throws Throwable {
        try (MockedConstruction<MockCreationValidator> mockedValidator =
                     mockConstruction(MockCreationValidator.class,
                             (mock, context) -> when(mock.isTypeMockable(Object.class)).thenReturn(true));
             MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                     (mock, context) -> {
                         when(mock.getMockHandler(invocation.getMock())).thenReturn(mockHandler);
                         when(mockHandler.getInvocationContainer()).thenReturn(container);
                         when(mockHandler.getMockSettings()).thenReturn(mockSettings);
                         when(mockSettings.getTypeToMock()).thenReturn(Object.class);
                     });
             MockedStatic<GenericMetadataSupport> mockedMetadata = mockStatic(GenericMetadataSupport.class)) {

            when(invocation.getMock()).thenReturn(new Object());
            mockedMetadata.when(() -> GenericMetadataSupport.inferFrom(Object.class))
                    .thenReturn(returnTypeGenericMetadata);
            when(returnTypeGenericMetadata.resolveGenericReturnType(method)).thenReturn(returnTypeGenericMetadata);
            when(returnTypeGenericMetadata.rawType()).thenReturn(Object.class);
            when(invocation.getMethod()).thenReturn(method);
            when(method.getReturnType()).thenReturn(Object.class);

            InvocationOnMock stubbedInvocation = mock(InvocationOnMock.class);
            when(container.getStubbedInvocations()).thenReturn(Collections.singletonList(stubbedInvocationMatcher));
            when(container.getInvocationForStubbing()).thenReturn(invocation);
            when(stubbedInvocationMatcher.getInvocation()).thenReturn(stubbedInvocation);
            when(invocation.matches(stubbedInvocation)).thenReturn(true);
            Object expectedAnswer = new Object();
            when(stubbedInvocationMatcher.answer(invocation)).thenReturn(expectedAnswer);

            Object result = returnsDeepStubs.answer(invocation);
            assertSame(expectedAnswer, result);
        }
    }

    @Test
    public void testGetMockWithMatchingStubbedInvocation() throws Throwable {
        try (MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                (mock, context) -> {
                    when(mock.getMockHandler(invocation.getMock())).thenReturn(mockHandler);
                    when(mockHandler.getInvocationContainer()).thenReturn(container);
                })) {

            when(invocation.getMock()).thenReturn(new Object());
            InvocationOnMock stubbedInvocation = mock(InvocationOnMock.class);
            when(container.getStubbedInvocations()).thenReturn(Collections.singletonList(stubbedInvocationMatcher));
            when(container.getInvocationForStubbing()).thenReturn(invocation);
            when(stubbedInvocationMatcher.getInvocation()).thenReturn(stubbedInvocation);
            when(invocation.matches(stubbedInvocation)).thenReturn(true);
            Object expectedAnswer = new Object();
            when(stubbedInvocationMatcher.answer(invocation)).thenReturn(expectedAnswer);

            Object result = returnsDeepStubs.answer(invocation);
            assertSame(expectedAnswer, result);
        }
    }

    @Test
    public void testGetMockWithoutMatchingStubbedInvocation() throws Throwable {
        try (MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                (mock, context) -> {
                    when(mock.getMockHandler(invocation.getMock())).thenReturn(mockHandler);
                    when(mockHandler.getInvocationContainer()).thenReturn(container);
                })) {

            when(invocation.getMock()).thenReturn(new Object());
            when(invocation.getMethod()).thenReturn(method);
            when(method.getReturnType()).thenReturn(Object.class);
            when(container.getStubbedInvocations()).thenReturn(Collections.emptyList());

            Object result = returnsDeepStubs.answer(invocation);
            assertEquals(Object.class, result.getClass());
        }
    }

    @Test
    public void testRecordDeepStubMock() throws Throwable {
        try (MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class)) {
            when(invocation.getMethod()).thenReturn(method);
            when(method.getReturnType()).thenReturn(Object.class);

            Object result = returnsDeepStubs.answer(invocation);
            assertEquals(Object.class, result.getClass());
        }
    }

    @Test
    public void testActualParameterizedType() {
        try (MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                (mock, context) -> {
                    when(mock.getMockHandler(invocation.getMock())).thenReturn(mockHandler);
                    when(mockHandler.getMockSettings()).thenReturn(mockSettings);
                    when(mockSettings.getTypeToMock()).thenReturn(Object.class);
                });
             MockedStatic<GenericMetadataSupport> mockedMetadata = mockStatic(GenericMetadataSupport.class)) {

            when(invocation.getMock()).thenReturn(new Object());
            mockedMetadata.when(() -> GenericMetadataSupport.inferFrom(Object.class))
                    .thenReturn(returnTypeGenericMetadata);

            GenericMetadataSupport result = returnsDeepStubs.actualParameterizedType(invocation.getMock());
            assertSame(returnTypeGenericMetadata, result);
        }
    }
}
