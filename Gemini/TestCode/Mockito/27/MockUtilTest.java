package org.mockito.internal.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.cglib.proxy.Enhancer;
import org.mockito.cglib.proxy.NoOp;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.MockHandlerInterface;
import org.mockito.internal.creation.MockSettingsImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import static org.junit.Assert.*;

public class MockUtilTest {

    private MockUtil mockUtil;

    public static class SampleClass {
        private String value = "initial";

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class CustomValidator extends MockCreationValidator {
        boolean validateTypeCalled = false;
        boolean validateExtraInterfacesCalled = false;
        boolean validateMockedTypeCalled = false;

        @Override
        public void validateType(Class<?> classToMock) {
            validateTypeCalled = true;
            super.validateType(classToMock);
        }

        @Override
        public void validateExtraInterfaces(Class<?> classToMock, Class<?>[] extraInterfaces) {
            validateExtraInterfacesCalled = true;
            super.validateExtraInterfaces(classToMock, extraInterfaces);
        }

        @Override
        public void validateMockedType(Class<?> classToMock, Object spiedInstance) {
            validateMockedTypeCalled = true;
            super.validateMockedType(classToMock, spiedInstance);
        }
    }

    @Before
    public void setUp() {
        mockUtil = new MockUtil();
    }

    @Test
    public void shouldConstructWithCustomValidatorAndInvokeValidations() {
        CustomValidator validator = new CustomValidator();
        MockUtil customMockUtil = new MockUtil(validator);

        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock = customMockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(validator.validateTypeCalled);
        assertTrue(validator.validateExtraInterfacesCalled);
        assertTrue(validator.validateMockedTypeCalled);
    }

    @Test
    public void shouldIdentifyMockCorrectly() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mockList = mockUtil.createMock(List.class, settings);

        assertTrue(mockUtil.isMock(mockList));
        assertFalse(mockUtil.isMock(new ArrayList<>()));
        assertFalse(mockUtil.isMock("non-mock string"));
        assertFalse(mockUtil.isMock(null));
    }

    @Test
    public void shouldReturnFalseWhenObjectIsFactoryButNotMockitoMock() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Object cglibFactory = enhancer.create();

        assertFalse(mockUtil.isMock(cglibFactory));
    }

    @Test
    public void shouldGetMockHandlerForMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        List<?> mockList = mockUtil.createMock(List.class, settings);

        MockHandlerInterface<?> handler = mockUtil.getMockHandler(mockList);
        assertNotNull(handler);
        assertEquals(settings, handler.getMockSettings());
    }

    @Test
    public void shouldThrowNotAMockExceptionWhenGetMockHandlerOnNull() {
        try {
            mockUtil.getMockHandler(null);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is null!"));
        }
    }

    @Test
    public void shouldThrowNotAMockExceptionWhenGetMockHandlerOnNonMock() {
        try {
            mockUtil.getMockHandler("not a mock");
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is: class java.lang.String"));
        }
    }

    @Test
    public void shouldThrowNotAMockExceptionWhenGetMockHandlerOnNonMockitoFactory() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Object cglibFactory = enhancer.create();

        try {
            mockUtil.getMockHandler(cglibFactory);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument should be a mock, but is:"));
        }
    }

    @Test
    public void shouldGetMockName() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.name("customMockName");

        List<?> mock = mockUtil.createMock(List.class, settings);
        MockName mockName = mockUtil.getMockName(mock);

        assertNotNull(mockName);
        assertEquals("customMockName", mockName.toString());
    }

    @Test
    public void shouldCreateMockWithDefaultNonSerializableAndNoExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertFalse(mock instanceof Serializable);
        assertFalse(mock instanceof Observer);
    }

    @Test
    public void shouldCreateMockWithExtraInterfacesOnly() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.extraInterfaces(Observer.class);

        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof Observer);
        assertFalse(mock instanceof Serializable);
    }

    @Test
    public void shouldCreateSerializableMockWithoutExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();

        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof Serializable);
        assertFalse(mock instanceof Observer);
    }

    @Test
    public void shouldCreateSerializableMockWithExtraInterfaces() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.serializable();
        settings.extraInterfaces(Observer.class);

        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertTrue(mock instanceof Serializable);
        assertTrue(mock instanceof Observer);
    }

    @Test
    public void shouldCopyStateFromSpiedInstance() {
        SampleClass spied = new SampleClass();
        spied.setValue("modifiedValue");

        MockSettingsImpl settings = new MockSettingsImpl();
        settings.spiedInstance(spied);
        settings.defaultAnswer(org.mockito.Mockito.CALLS_REAL_METHODS);

        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        assertNotNull(mock);
        assertTrue(mockUtil.isMock(mock));
        assertEquals("modifiedValue", mock.getValue());
    }

    @Test
    public void shouldResetMock() {
        MockSettingsImpl settings = new MockSettingsImpl();
        SampleClass mock = mockUtil.createMock(SampleClass.class, settings);

        MockHandlerInterface<?> handlerBefore = mockUtil.getMockHandler(mock);

        mockUtil.resetMock(mock);

        MockHandlerInterface<?> handlerAfter = mockUtil.getMockHandler(mock);
        assertNotNull(handlerAfter);
        assertNotSame(handlerBefore, handlerAfter);
        assertTrue(mockUtil.isMock(mock));
    }
}
