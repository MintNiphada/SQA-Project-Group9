package org.mockito.internal.stubbing.defaultanswers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.internal.MockitoCore;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.MockSettings;

import java.lang.reflect.Field;
import java.util.Collections;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    @Mock
    private MockitoCore mockitoCore;

    @Mock
    private ReturnsEmptyValues delegate;

    @Mock
    private InvocationOnMock invocation;

    @Mock
    private GenericMetadataSupport returnTypeGenericMetadata;

    @Mock
    private InternalMockHandler<Object> handler;

    @Mock
    private InvocationContainerImpl container;

    @Mock
    private CreationSettings mockSettings;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        returnsDeepStubs = new ReturnsDeepStubs();
        setField(returnsDeepStubs, "mockitoCore", mockitoCore);
        setField(returnsDeepStubs, "delegate", delegate);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    public void answer_notMockable_delegatesToReturnsEmptyValues() throws Throwable {
        Class<?> rawType = String.class;
        when(returnTypeGenericMetadata.rawType()).thenReturn(rawType);
        when(mockitoCore.isTypeMockable(rawType)).thenReturn(false);
        when(delegate.returnValueFor(rawType)).thenReturn("default");

        ReturnsDeepStubs spy = spy(returnsDeepStubs);
        doReturn(returnTypeGenericMetadata).when(spy).actualParameterizedType(invocation.getMock());

        Object result = spy.answer(invocation);

        assertEquals("default", result);
        verify(delegate).returnValueFor(rawType);
        verify(spy, never()).getMock(any(), any());
    }

    @Test
    public void answer_mockable_callsGetMock() throws Throwable {
        Class<?> rawType = java.util.List.class;
        when(returnTypeGenericMetadata.rawType()).thenReturn(rawType);
        when(mockitoCore.isTypeMockable(rawType)).thenReturn(true);

        ReturnsDeepStubs spy = spy(returnsDeepStubs);
        doReturn(returnTypeGenericMetadata).when(spy).actualParameterizedType(invocation.getMock());
        doReturn("mockResult").when(spy).getMock(invocation, returnTypeGenericMetadata);

        Object result = spy.answer(invocation);

        assertEquals("mockResult", result);
        verify(spy).getMock(invocation, returnTypeGenericMetadata);
    }

    @Test
    public void getMock_matchingStubbedInvocation_returnsStubbedAnswer() throws Throwable {
        try (MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                (mock, context) -> {
                    when(mock.getMockHandler(invocation.getMock())).thenReturn(handler);
                })) {

            when(handler.getInvocationContainer()).thenReturn(container);

            StubbedInvocationMatcher stubbedMatcher = mock(StubbedInvocationMatcher.class);
            org.mockito.invocation.Invocation stubbedInvocation = mock(org.mockito.invocation.Invocation.class);
            when(stubbedMatcher.getInvocation()).thenReturn(stubbedInvocation);
            when(container.getStubbedInvocations()).thenReturn(Collections.singletonList(stubbedMatcher));

            org.mockito.invocation.Invocation currentInvocation = mock(org.mockito.invocation.Invocation.class);
            when(container.getInvocationForStubbing()).thenReturn(currentInvocation);
            when(currentInvocation.matches(stubbedInvocation)).thenReturn(true);

            when(stubbedMatcher.answer(invocation)).thenReturn("stubbedAnswer");

            ReturnsDeepStubs spy = spy(returnsDeepStubs);
            Object result = spy.getMock(invocation, returnTypeGenericMetadata);

            assertEquals("stubbedAnswer", result);
            verify(spy, never()).createNewDeepStubMock(any());
            verify(spy, never()).recordDeepStubMock(any(), any());
        }
    }

    @Test
    public void getMock_noMatchingStubbedInvocation_createsAndRecordsDeepStub() throws Throwable {
        try (MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                (mock, context) -> {
                    when(mock.getMockHandler(invocation.getMock())).thenReturn(handler);
                })) {

            when(handler.getInvocationContainer()).thenReturn(container);
            when(container.getStubbedInvocations()).thenReturn(Collections.emptyList());

            Object deepStubMock = new Object();
            ReturnsDeepStubs spy = spy(returnsDeepStubs);
            doReturn(deepStubMock).when(spy).createNewDeepStubMock(returnTypeGenericMetadata);
            doReturn(deepStubMock).when(spy).recordDeepStubMock(deepStubMock, container);

            Object result = spy.getMock(invocation, returnTypeGenericMetadata);

            assertEquals(deepStubMock, result);
            verify(spy).createNewDeepStubMock(returnTypeGenericMetadata);
            verify(spy).recordDeepStubMock(deepStubMock, container);
        }
    }

    @Test
    public void createNewDeepStubMock_usesMockitoCore() {
        Class<?> rawType = java.util.List.class;
        when(returnTypeGenericMetadata.rawType()).thenReturn(rawType);

        MockSettings mockSettings = mock(MockSettings.class);
        ReturnsDeepStubs spy = spy(returnsDeepStubs);
        doReturn(mockSettings).when(spy).withSettingsUsing(returnTypeGenericMetadata);
        when(mockitoCore.mock(rawType, mockSettings)).thenReturn("createdMock");

        Object result = spy.createNewDeepStubMock(returnTypeGenericMetadata);

        assertEquals("createdMock", result);
        verify(mockitoCore).mock(rawType, mockSettings);
    }

    @Test
    public void withSettingsUsing_withExtraInterfaces() {
        Class<?>[] extraInterfaces = new Class<?>[]{java.io.Serializable.class};
        when(returnTypeGenericMetadata.rawExtraInterfaces()).thenReturn(extraInterfaces);

        MockSettings baseSettings = mock(MockSettings.class);
        MockSettings withExtraInterfaces = mock(MockSettings.class);
        MockSettings finalSettings = mock(MockSettings.class);

        mockStatic(org.mockito.Mockito.class);
        when(Mockito.withSettings()).thenReturn(baseSettings);
        when(baseSettings.extraInterfaces(extraInterfaces)).thenReturn(withExtraInterfaces);
        when(withExtraInterfaces.defaultAnswer(any(ReturnsDeepStubs.class))).thenReturn(finalSettings);

        ReturnsDeepStubs spy = spy(returnsDeepStubs);
        MockSettings result = spy.withSettingsUsing(returnTypeGenericMetadata);

        assertSame(finalSettings, result);
        verify(withExtraInterfaces).defaultAnswer(any(ReturnsDeepStubs.class));
    }

    @Test
    public void withSettingsUsing_withoutExtraInterfaces() {
        when(returnTypeGenericMetadata.rawExtraInterfaces()).thenReturn(new Class<?>[0]);

        MockSettings baseSettings = mock(MockSettings.class);
        MockSettings finalSettings = mock(MockSettings.class);

        mockStatic(org.mockito.Mockito.class);
        when(Mockito.withSettings()).thenReturn(baseSettings);
        when(baseSettings.defaultAnswer(any(ReturnsDeepStubs.class))).thenReturn(finalSettings);

        ReturnsDeepStubs spy = spy(returnsDeepStubs);
        MockSettings result = spy.withSettingsUsing(returnTypeGenericMetadata);

        assertSame(finalSettings, result);
        verify(baseSettings, never()).extraInterfaces(any());
        verify(baseSettings).defaultAnswer(any(ReturnsDeepStubs.class));
    }

    @Test
    public void returnsDeepStubsAnswerUsing_overridesActualParameterizedType() {
        ReturnsDeepStubs answer = returnsDeepStubs.returnsDeepStubsAnswerUsing(returnTypeGenericMetadata);
        Object mock = new Object();
        GenericMetadataSupport result = answer.actualParameterizedType(mock);
        assertSame(returnTypeGenericMetadata, result);
    }

    @Test
    public void recordDeepStubMock_addsAnswerAndReturnsMock() throws Throwable {
        final Object mock = new Object();
        doAnswer(inv -> {
            Answer<Object> answer = inv.getArgument(0);
            assertEquals(mock, answer.answer(null));
            return null;
        }).when(container).addAnswer(any(Answer.class), eq(false));

        Object result = returnsDeepStubs.recordDeepStubMock(mock, container);

        assertSame(mock, result);
        verify(container).addAnswer(any(Answer.class), eq(false));
    }

    @Test
    public void actualParameterizedType_infersFromTypeToMock() {
        try (MockedConstruction<MockUtil> mockedUtil = mockConstruction(MockUtil.class,
                (mock, context) -> {
                    when(mock.getMockHandler(any())).thenReturn(handler);
                })) {

            when(handler.getMockSettings()).thenReturn(mockSettings);
            Class<?> typeToMock = java.util.List.class;
            when(mockSettings.getTypeToMock()).thenReturn(typeToMock);

            try (MockedStatic<GenericMetadataSupport> mockedStatic = mockStatic(GenericMetadataSupport.class)) {
                GenericMetadataSupport inferred = mock(GenericMetadataSupport.class);
                mockedStatic.when(() -> GenericMetadataSupport.inferFrom(typeToMock)).thenReturn(inferred);

                Object mock = new Object();
                GenericMetadataSupport result = returnsDeepStubs.actualParameterizedType(mock);

                assertSame(inferred, result);
                mockedStatic.verify(() -> GenericMetadataSupport.inferFrom(typeToMock));
            }
        }
    }
}
