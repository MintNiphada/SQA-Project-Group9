package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public class ReturnsDeepStubsTest {

    private ReturnsDeepStubs returnsDeepStubs;

    interface Level3 {
        String getString();
        int getInt();
        boolean getBoolean();
    }

    interface Level2 {
        Level3 getLevel3();
        List<String> getList();
    }

    interface Level1 {
        Level2 getLevel2();
        final class FinalClass {}
        FinalClass getFinalClass();
        String getPrimitiveString();
        int getPrimitiveInt();
    }

    interface GenericContainer<T> {
        T getValue();
        List<T> getList();
    }

    interface ComplexGenericInterface {
        GenericContainer<Level2> getContainer();
        Map<String, Set<Level3>> getComplexMap();
    }

    @Before
    public void setUp() {
        returnsDeepStubs = new ReturnsDeepStubs();
    }

    @Test
    public void shouldReturnMockForMockableReturnType() {
        Level1 level1 = mock(Level1.class, returnsDeepStubs);
        Level2 level2 = level1.getLevel2();

        assertNotNull(level2);
        assertTrue(new org.mockito.internal.util.MockUtil().isMock(level2));
    }

    @Test
    public void shouldReturnSameMockWhenCalledMultipleTimes() {
        Level1 level1 = mock(Level1.class, returnsDeepStubs);
        Level2 firstCall = level1.getLevel2();
        Level2 secondCall = level1.getLevel2();

        assertNotNull(firstCall);
        assertSame("Subsequent calls to deep stub should return the cached mock instance", firstCall, secondCall);
    }

    @Test
    public void shouldSupportDeepChaining() {
        Level1 level1 = mock(Level1.class, returnsDeepStubs);
        
        Level3 level3 = level1.getLevel2().getLevel3();
        assertNotNull(level3);
        assertTrue(new org.mockito.internal.util.MockUtil().isMock(level3));
        
        // Deep chained invocation of primitive and non-mockable types
        assertEquals("", level1.getLevel2().getLevel3().getString());
        assertEquals(0, level1.getLevel2().getLevel3().getInt());
        assertFalse(level1.getLevel2().getLevel3().getBoolean());
    }

    @Test
    public void shouldReturnEmptyValuesForNonMockableTypes() {
        Level1 level1 = mock(Level1.class, returnsDeepStubs);

        assertEquals("", level1.getPrimitiveString());
        assertEquals(0, level1.getPrimitiveInt());
        assertNull(level1.getFinalClass());
    }

    @Test
    public void shouldHonorExplicitStubbingOverDeepStubs() {
        Level1 level1 = mock(Level1.class, returnsDeepStubs);
        Level2 customLevel2 = mock(Level2.class);

        when(level1.getLevel2()).thenReturn(customLevel2);

        assertSame(customLevel2, level1.getLevel2());
    }

    @Test
    public void shouldHonorDeepExplicitStubbing() {
        Level1 level1 = mock(Level1.class, returnsDeepStubs);

        when(level1.getLevel2().getLevel3().getString()).thenReturn("custom_value");

        assertEquals("custom_value", level1.getLevel2().getLevel3().getString());
    }

    @Test
    public void shouldHandleGenericReturnTypes() {
        ComplexGenericInterface mock = mock(ComplexGenericInterface.class, returnsDeepStubs);

        GenericContainer<Level2> container = mock.getContainer();
        assertNotNull(container);
        assertTrue(new org.mockito.internal.util.MockUtil().isMock(container));

        List<Level2> list = container.getList();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void shouldResolveGenericMetadataCorrectly() {
        Level1 mock = mock(Level1.class, returnsDeepStubs);
        GenericMetadataSupport metadata = returnsDeepStubs.actualParameterizedType(mock);

        assertNotNull(metadata);
        assertEquals(Level1.class, metadata.rawType());
    }

    @Test
    public void shouldBeSerializable() throws Exception {
        ReturnsDeepStubs original = new ReturnsDeepStubs();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsDeepStubs);

        Level1 level1 = mock(Level1.class, (ReturnsDeepStubs) deserialized);
        assertNotNull(level1.getLevel2());
    }

    @Test
    public void shouldWorkWithMockSettingsSerializable() {
        Level1 level1 = mock(Level1.class, withSettings().defaultAnswer(returnsDeepStubs).serializable());
        assertNotNull(level1.getLevel2());
        assertEquals("", level1.getPrimitiveString());
    }
}
