package org.mockito;

import org.junit.After;
import org.junit.Test;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.verification.api.VerificationMode;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class ArticleManagerTestTest {

    @After
    public void tearDown() {
        Mockito.validateMockitoUsage();
    }

    public static class SampleClass {
        public String getFoo() {
            return "real_foo";
        }

        public void voidMethod() {
            // real void
        }

        public String echo(String input) {
            return input;
        }

        public int calculate(int a, int b) {
            return a + b;
        }
    }

    public interface SampleInterface {
        String query(String q);
        void perform();
    }

    @Test
    public void testConstructorCoverage() {
        Mockito mockito = new Mockito();
        assertNotNull(mockito);
    }

    @Test
    public void testConstants() {
        assertNotNull(Mockito.RETURNS_DEFAULTS);
        assertNotNull(Mockito.RETURNS_SMART_NULLS);
        assertNotNull(Mockito.RETURNS_MOCKS);
        assertNotNull(Mockito.CALLS_REAL_METHODS);
    }

    @Test
    public void testMockClass() {
        List<?> list = Mockito.mock(List.class);
        assertNotNull(list);
        assertNull(list.get(0));
    }

    @Test
    public void testMockClassWithName() {
        List<?> list = Mockito.mock(List.class, "myCustomMock");
        assertNotNull(list);
        assertTrue(list.toString().contains("myCustomMock"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testMockWithDeprecatedReturnValues() {
        ReturnValues returnValues = new ReturnValues() {
            public Object valueFor(org.mockito.invocation.InvocationOnMock invocation) {
                return "custom_default";
            }
        };
        SampleClass mock = Mockito.mock(SampleClass.class, returnValues);
        assertEquals("custom_default", mock.getFoo());
    }

    @Test
    public void testMockWithDefaultAnswer() {
        Answer<Object> answer = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "answer_default";
            }
        };
        SampleClass mock = Mockito.mock(SampleClass.class, answer);
        assertEquals("answer_default", mock.getFoo());
    }

    @Test
    public void testMockWithSettings() {
        MockSettings settings = Mockito.withSettings().name("settingsMock").defaultAnswer(Mockito.RETURNS_SMART_NULLS);
        List<?> mock = Mockito.mock(List.class, settings);
        assertNotNull(mock);
    }

    @Test
    public void testWithSettingsSerializable() {
        MockSettings settings = Mockito.withSettings().serializable();
        List<?> mock = Mockito.mock(List.class, settings);
        assertTrue(mock instanceof Serializable);
    }

    @Test
    public void testSpy() {
        List<String> list = new ArrayList<String>();
        List<String> spyList = Mockito.spy(list);

        spyList.add("one");
        spyList.add("two");

        assertEquals(2, spyList.size());
        assertEquals("one", spyList.get(0));
        Mockito.verify(spyList).add("one");
        Mockito.verify(spyList).add("two");
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedStub() {
        List<String> mock = Mockito.mock(List.class);
        Mockito.stub(mock.get(0)).toReturn("zero");
        assertEquals("zero", mock.get(0));

        Mockito.stub(mock.get(1)).toThrow(new IllegalArgumentException("test_ex"));
        try {
            mock.get(1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("test_ex", e.getMessage());
        }
    }

    @Test
    public void testWhenAndThenReturn() {
        List<String> mock = Mockito.mock(List.class);
        Mockito.when(mock.get(0)).thenReturn("first");
        Mockito.when(mock.get(1)).thenReturn("second", "third");

        assertEquals("first", mock.get(0));
        assertEquals("second", mock.get(1));
        assertEquals("third", mock.get(1));
        assertEquals("third", mock.get(1));
    }

    @Test
    public void testWhenAndThenThrow() {
        List<String> mock = Mockito.mock(List.class);
        Mockito.when(mock.get(0)).thenThrow(new IllegalStateException("err1"));

        try {
            mock.get(0);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("err1", e.getMessage());
        }
    }

    @Test
    public void testWhenAndThenAnswer() {
        SampleClass mock = Mockito.mock(SampleClass.class);
        Mockito.when(mock.echo(Mockito.anyString())).thenAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                return "echoed:" + args[0];
            }
        });

        assertEquals("echoed:hello", mock.echo("hello"));
    }

    @Test
    public void testWhenAndThenCallRealMethod() {
        SampleClass mock = Mockito.mock(SampleClass.class);
        Mockito.when(mock.getFoo()).thenCallRealMethod();
        assertEquals("real_foo", mock.getFoo());
    }

    @Test
    public void testVerifyDefaultAndTimes() {
        List<String> mock = Mockito.mock(List.class);
        mock.add("a");
        mock.add("b");
        mock.add("b");

        Mockito.verify(mock).add("a");
        Mockito.verify(mock, Mockito.times(1)).add("a");
        Mockito.verify(mock, Mockito.times(2)).add("b");
    }

    @Test
    public void testVerifyModes() {
        List<String> mock = Mockito.mock(List.class);
        mock.add("c");
        mock.add("c");
        mock.add("c");

        Mockito.verify(mock, Mockito.never()).add("never_added");
        Mockito.verify(mock, Mockito.atLeastOnce()).add("c");
        Mockito.verify(mock, Mockito.atLeast(2)).add("c");
        Mockito.verify(mock, Mockito.atMost(5)).add("c");
    }

    @Test
    public void testVerifyOnly() {
        List<String> mock = Mockito.mock(List.class);
        mock.add("only_call");
        Mockito.verify(mock, Mockito.only()).add("only_call");
    }

    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyOnlyFailsWhenMultipleInteractions() {
        List<String> mock = Mockito.mock(List.class);
        mock.add("call1");
        mock.add("call2");
        Mockito.verify(mock, Mockito.only()).add("call1");
    }

    @Test
    public void testReset() {
        List<String> mock = Mockito.mock(List.class);
        Mockito.when(mock.get(0)).thenReturn("val");
        mock.add("interaction");
        assertEquals("val", mock.get(0));

        Mockito.reset(mock);
        assertNull(mock.get(0));
        Mockito.verifyZeroInteractions(mock);
    }

    @Test
    public void testVerifyZeroInteractions() {
        List<?> mock1 = Mockito.mock(List.class);
        List<?> mock2 = Mockito.mock(List.class);
        Mockito.verifyZeroInteractions(mock1, mock2);
    }

    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyZeroInteractionsFailsOnInteraction() {
        List<String> mock = Mockito.mock(List.class);
        mock.clear();
        Mockito.verifyZeroInteractions(mock);
    }

    @Test
    public void testVerifyNoMoreInteractions() {
        List<String> mock = Mockito.mock(List.class);
        mock.add("one");
        Mockito.verify(mock).add("one");
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test(expected = NoInteractionsWanted.class)
    public void testVerifyNoMoreInteractionsFailsOnUnverified() {
        List<String> mock = Mockito.mock(List.class);
        mock.add("one");
        mock.add("unverified");
        Mockito.verify(mock).add("one");
        Mockito.verifyNoMoreInteractions(mock);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testStubVoid() {
        List<String> mock = Mockito.mock(List.class);
        Mockito.stubVoid(mock).toThrow(new RuntimeException("void_err")).on().clear();

        try {
            mock.clear();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("void_err", e.getMessage());
        }
    }

    @Test
    public void testDoThrow() {
        List<String> mock = Mockito.mock(List.class);
        Mockito.doThrow(new IllegalStateException("doThrow_err")).when(mock).clear();

        try {
            mock.clear();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("doThrow_err", e.getMessage());
        }
    }

    @Test
    public void testDoReturn() {
        List<String> mock = Mockito.mock(List.class);
        Mockito.doReturn("doReturn_val").when(mock).get(99);
        assertEquals("doReturn_val", mock.get(99));
    }

    @Test
    public void testDoNothing() {
        List<String> list = new LinkedList<String>();
        List<String> spy = Mockito.spy(list);

        Mockito.doNothing().when(spy).clear();
        spy.add("keep_me");
        spy.clear();
        assertEquals(1, spy.size());
    }

    @Test
    public void testDoAnswer() {
        SampleClass mock = Mockito.mock(SampleClass.class);
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "doAnswer_val";
            }
        }).when(mock).getFoo();

        assertEquals("doAnswer_val", mock.getFoo());
    }

    @Test
    public void testDoCallRealMethod() {
        SampleClass mock = Mockito.mock(SampleClass.class);
        Mockito.doCallRealMethod().when(mock).voidMethod();
        mock.voidMethod();
        Mockito.verify(mock).voidMethod();
    }

    @Test
    public void testInOrder() {
        List<String> firstMock = Mockito.mock(List.class);
        List<String> secondMock = Mockito.mock(List.class);

        firstMock.add("first");
        secondMock.add("second");

        InOrder inOrder = Mockito.inOrder(firstMock, secondMock);
        inOrder.verify(firstMock).add("first");
        inOrder.verify(secondMock).add("second");
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void testInOrderFailsOnWrongOrder() {
        List<String> firstMock = Mockito.mock(List.class);
        List<String> secondMock = Mockito.mock(List.class);

        firstMock.add("first");
        secondMock.add("second");

        InOrder inOrder = Mockito.inOrder(firstMock, secondMock);
        inOrder.verify(secondMock).add("second");
        inOrder.verify(firstMock).add("first");
    }

    @Test
    public void testDebug() {
        MockitoDebugger debugger = Mockito.debug();
        assertNotNull(debugger);
    }

    @Test
    public void testVerificationModeFactoryWrappers() {
        VerificationMode timesMode = Mockito.times(3);
        assertNotNull(timesMode);

        VerificationMode neverMode = Mockito.never();
        assertNotNull(neverMode);

        VerificationMode atLeastOnceMode = Mockito.atLeastOnce();
        assertNotNull(atLeastOnceMode);

        VerificationMode atLeastMode = Mockito.atLeast(4);
        assertNotNull(atLeastMode);

        VerificationMode atMostMode = Mockito.atMost(2);
        assertNotNull(atMostMode);

        VerificationMode onlyMode = Mockito.only();
        assertNotNull(onlyMode);
    }
}
