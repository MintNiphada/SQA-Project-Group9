package com.google.gson.internal.bind;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JsonAdapterAnnotationTypeAdapterFactoryTest {

    @Mock private ConstructorConstructor constructorConstructor;
    @Mock private Gson gson;
    @Mock private TypeToken<?> typeToken;
    @Mock private JsonAdapter annotation;
    @Mock private TypeAdapter<Object> adapterMock;
    @Mock private TypeAdapterFactory factoryMock;
    @Mock private ObjectConstructor<Object> objectConstructor;
    @Mock private ObjectConstructor<Object> factoryObjectConstructor;

    private JsonAdapterAnnotationTypeAdapterFactory factory;

    @Before
    public void setUp() {
        factory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
    }

    @Test
    public void testCreate_NoAnnotation_ReturnsNull() {
        when(typeToken.getRawType()).thenReturn(Object.class);
        TypeAdapter<Object> result = factory.create(gson, (TypeToken<Object>) typeToken);
        assertNull(result);
    }

    @Test
    public void testCreate_WithTypeAdapterAnnotation_ReturnsAdapter() {
        @JsonAdapter(TestTypeAdapter.class)
        class AnnotatedClass {}
        Class<?> rawType = AnnotatedClass.class;
        when(typeToken.getRawType()).thenReturn((Class) rawType);

        TypeToken<TestTypeAdapter> adapterToken = TypeToken.get(TestTypeAdapter.class);
        when(constructorConstructor.get(adapterToken)).thenReturn((ObjectConstructor) objectConstructor);
        when(objectConstructor.construct()).thenReturn((TestTypeAdapter) (Object) adapterMock);
        when(adapterMock.nullSafe()).thenReturn(adapterMock);

        TypeAdapter<Object> result = factory.create(gson, (TypeToken<Object>) typeToken);
        assertSame(adapterMock, result);
        verify(adapterMock).nullSafe();
    }

    @Test
    public void testGetTypeAdapter_TypeAdapterClass() {
        when(annotation.value()).thenReturn((Class) TestTypeAdapter.class);
        TypeToken<TestTypeAdapter> adapterToken = TypeToken.get(TestTypeAdapter.class);
        when(constructorConstructor.get(adapterToken)).thenReturn((ObjectConstructor) objectConstructor);
        when(objectConstructor.construct()).thenReturn((TestTypeAdapter) (Object) adapterMock);
        when(adapterMock.nullSafe()).thenReturn(adapterMock);

        TypeAdapter<?> result = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(constructorConstructor, gson, typeToken, annotation);
        assertSame(adapterMock, result);
        verify(adapterMock).nullSafe();
    }

    @Test
    public void testGetTypeAdapter_TypeAdapterFactoryClass() {
        when(annotation.value()).thenReturn((Class) TestTypeAdapterFactory.class);
        TypeToken<TestTypeAdapterFactory> factoryToken = TypeToken.get(TestTypeAdapterFactory.class);
        when(constructorConstructor.get(factoryToken)).thenReturn((ObjectConstructor) factoryObjectConstructor);
        when(factoryObjectConstructor.construct()).thenReturn(factoryMock);
        when(factoryMock.create(gson, typeToken)).thenReturn(adapterMock);
        when(adapterMock.nullSafe()).thenReturn(adapterMock);

        TypeAdapter<?> result = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(constructorConstructor, gson, typeToken, annotation);
        assertSame(adapterMock, result);
        verify(factoryMock).create(gson, typeToken);
        verify(adapterMock).nullSafe();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTypeAdapter_InvalidClass_ThrowsException() {
        when(annotation.value()).thenReturn((Class) String.class);
        JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(constructorConstructor, gson, typeToken, annotation);
    }

    public static class TestTypeAdapter extends TypeAdapter<String> {
        @Override public void write(JsonWriter out, String value) throws IOException {}
        @Override public String read(JsonReader in) throws IOException { return null; }
    }

    public static class TestTypeAdapterFactory implements TypeAdapterFactory {
        @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return null;
        }
    }
}
