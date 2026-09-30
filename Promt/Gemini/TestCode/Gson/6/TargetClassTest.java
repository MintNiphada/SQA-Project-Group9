package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;

public class JsonAdapterAnnotationTypeAdapterFactoryTest {

  private ConstructorConstructor constructorConstructor;
  private JsonAdapterAnnotationTypeAdapterFactory factory;
  private Gson gson;

  @Before
  public void setUp() {
    constructorConstructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    factory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
    gson = new Gson();
  }

  private static class UnannotatedClass {
  }

  @JsonAdapter(CustomTypeAdapter.class)
  private static class ClassWithTypeAdapter {
  }

  @JsonAdapter(CustomTypeAdapterFactory.class)
  private static class ClassWithTypeAdapterFactory {
  }

  @SuppressWarnings("rawtypes")
  @JsonAdapter((Class) String.class)
  private static class ClassWithInvalidAdapter {
  }

  private static class CustomTypeAdapter extends TypeAdapter<ClassWithTypeAdapter> {
    @Override
    public void write(JsonWriter out, ClassWithTypeAdapter value) throws IOException {
      out.value("custom_type_adapter");
    }

    @Override
    public ClassWithTypeAdapter read(JsonReader in) throws IOException {
      in.nextString();
      return new ClassWithTypeAdapter();
    }
  }

  private static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() == ClassWithTypeAdapterFactory.class) {
        return (TypeAdapter<T>) new TypeAdapter<ClassWithTypeAdapterFactory>() {
          @Override
          public void write(JsonWriter out, ClassWithTypeAdapterFactory value) throws IOException {
            out.value("factory_adapter");
          }

          @Override
          public ClassWithTypeAdapterFactory read(JsonReader in) throws IOException {
            in.nextString();
            return new ClassWithTypeAdapterFactory();
          }
        };
      }
      return null;
    }
  }

  @Test
  public void testCreateReturnsNullWhenNoAnnotation() {
    TypeToken<UnannotatedClass> typeToken = TypeToken.get(UnannotatedClass.class);
    TypeAdapter<UnannotatedClass> adapter = factory.create(gson, typeToken);
    Assert.assertNull("Adapter should be null for unannotated class", adapter);
  }

  @Test
  public void testCreateWithTypeAdapterAnnotation() {
    TypeToken<ClassWithTypeAdapter> typeToken = TypeToken.get(ClassWithTypeAdapter.class);
    TypeAdapter<ClassWithTypeAdapter> adapter = factory.create(gson, typeToken);

    Assert.assertNotNull("Adapter should not be null", adapter);
    Assert.assertEquals("\"custom_type_adapter\"", adapter.toJson(new ClassWithTypeAdapter()));
    Assert.assertEquals("null", adapter.toJson(null));
  }

  @Test
  public void testCreateWithTypeAdapterFactoryAnnotation() {
    TypeToken<ClassWithTypeAdapterFactory> typeToken = TypeToken.get(ClassWithTypeAdapterFactory.class);
    TypeAdapter<ClassWithTypeAdapterFactory> adapter = factory.create(gson, typeToken);

    Assert.assertNotNull("Adapter should not be null", adapter);
    Assert.assertEquals("\"factory_adapter\"", adapter.toJson(new ClassWithTypeAdapterFactory()));
    Assert.assertEquals("null", adapter.toJson(null));
  }

  @Test
  public void testCreateWithInvalidAdapterThrowsIllegalArgumentException() {
    TypeToken<ClassWithInvalidAdapter> typeToken = TypeToken.get(ClassWithInvalidAdapter.class);
    try {
      factory.create(gson, typeToken);
      Assert.fail("Expected IllegalArgumentException for invalid @JsonAdapter value");
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("@JsonAdapter value must be TypeAdapter or TypeAdapterFactory reference.", e.getMessage());
    }
  }

  @Test
  public void testGetTypeAdapterDirectlyWithTypeAdapter() {
    JsonAdapter annotation = ClassWithTypeAdapter.class.getAnnotation(JsonAdapter.class);
    TypeToken<ClassWithTypeAdapter> typeToken = TypeToken.get(ClassWithTypeAdapter.class);

    TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
        constructorConstructor, gson, typeToken, annotation);

    Assert.assertNotNull(adapter);
    Assert.assertEquals("null", adapter.toJson(null));
  }

  @Test
  public void testGetTypeAdapterDirectlyWithTypeAdapterFactory() {
    JsonAdapter annotation = ClassWithTypeAdapterFactory.class.getAnnotation(JsonAdapter.class);
    TypeToken<ClassWithTypeAdapterFactory> typeToken = TypeToken.get(ClassWithTypeAdapterFactory.class);

    TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
        constructorConstructor, gson, typeToken, annotation);

    Assert.assertNotNull(adapter);
    Assert.assertEquals("null", adapter.toJson(null));
  }
}
