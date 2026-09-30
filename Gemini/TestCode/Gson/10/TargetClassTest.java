package com.google.gson.internal.bind;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.ObjectConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReflectiveTypeAdapterFactoryTest {

  private ConstructorConstructor constructorConstructor;
  private FieldNamingStrategy fieldNamingPolicy;
  private Excluder excluder;
  private ReflectiveTypeAdapterFactory factory;
  private Gson gson;

  @Before
  public void setUp() {
    constructorConstructor = new ConstructorConstructor(Collections.emptyMap());
    fieldNamingPolicy = FieldNamingPolicy.IDENTITY;
    excluder = Excluder.DEFAULT;
    factory = new ReflectiveTypeAdapterFactory(constructorConstructor, fieldNamingPolicy, excluder);
    gson = new Gson();
  }

  static class SimpleClass {
    public int intValue = 42;
    public String stringValue = "test";
    public transient String transientValue = "transient";
  }

  static class ClassWithSerializedName {
    @SerializedName(value = "custom_name", alternate = {"alt1", "alt2"})
    public String value;

    public ClassWithSerializedName() {}
    public ClassWithSerializedName(String value) {
      this.value = value;
    }
  }

  static class ClassWithDuplicateField {
    @SerializedName("duplicate")
    public String val1;
    @SerializedName("duplicate")
    public String val2;
  }

  static class BaseClass {
    public String baseField = "base";
  }

  static class SubClass extends BaseClass {
    public String subField = "sub";
  }

  static class RecursiveClass {
    public String name;
    public RecursiveClass self;

    public RecursiveClass(String name) {
      this.name = name;
    }
  }

  static class CustomTypeAdapter extends TypeAdapter<String> {
    @Override
    public void write(JsonWriter out, String value) throws IOException {
      out.value("custom:" + value);
    }

    @Override
    public String read(JsonReader in) throws IOException {
      return in.nextString().replace("custom:", "");
    }
  }

  static class ClassWithJsonAdapterField {
    @JsonAdapter(CustomTypeAdapter.class)
    public String adaptedField;

    public ClassWithJsonAdapterField() {}
    public ClassWithJsonAdapterField(String adaptedField) {
      this.adaptedField = adaptedField;
    }
  }

  interface AnInterface {
  }

  @Test
  public void testExcludeField() throws Exception {
    Field intField = SimpleClass.class.getDeclaredField("intValue");
    Field transientField = SimpleClass.class.getDeclaredField("transientValue");

    Assert.assertTrue(factory.excludeField(intField, true));
    Assert.assertTrue(factory.excludeField(intField, false));
    Assert.assertFalse(factory.excludeField(transientField, true));
    Assert.assertFalse(factory.excludeField(transientField, false));
  }

  @Test
  public void testCreateNonObject() {
    TypeAdapter<Integer> adapter = factory.create(gson, TypeToken.get(int.class));
    Assert.assertNull(adapter);
  }

  @Test
  public void testCreateInterface() throws IOException {
    TypeAdapter<AnInterface> adapter = factory.create(gson, TypeToken.get(AnInterface.class));
    Assert.assertNotNull(adapter);

    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, new AnInterface() {});
    Assert.assertEquals("{}", sw.toString());
  }

  @Test
  public void testSimpleSerializationAndDeserialization() throws IOException {
    TypeAdapter<SimpleClass> adapter = factory.create(gson, TypeToken.get(SimpleClass.class));
    SimpleClass instance = new SimpleClass();
    instance.intValue = 100;
    instance.stringValue = "hello";

    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, instance);

    String json = sw.toString();
    Assert.assertTrue(json.contains("\"intValue\":100"));
    Assert.assertTrue(json.contains("\"stringValue\":\"hello\""));
    Assert.assertFalse(json.contains("transientValue"));

    SimpleClass deserialized = adapter.read(new JsonReader(new StringReader(json)));
    Assert.assertEquals(100, deserialized.intValue);
    Assert.assertEquals("hello", deserialized.stringValue);
  }

  @Test
  public void testNullSerializationAndDeserialization() throws IOException {
    TypeAdapter<SimpleClass> adapter = factory.create(gson, TypeToken.get(SimpleClass.class));

    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, null);
    Assert.assertEquals("null", sw.toString());

    SimpleClass deserialized = adapter.read(new JsonReader(new StringReader("null")));
    Assert.assertNull(deserialized);
  }

  @Test
  public void testSerializedNameAndAlternates() throws IOException {
    TypeAdapter<ClassWithSerializedName> adapter =
        factory.create(gson, TypeToken.get(ClassWithSerializedName.class));

    ClassWithSerializedName obj = new ClassWithSerializedName("testVal");
    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, obj);
    Assert.assertEquals("{\"custom_name\":\"testVal\"}", sw.toString());

    ClassWithSerializedName read1 = adapter.read(new JsonReader(new StringReader("{\"custom_name\":\"a\"}")));
    Assert.assertEquals("a", read1.value);

    ClassWithSerializedName read2 = adapter.read(new JsonReader(new StringReader("{\"alt1\":\"b\"}")));
    Assert.assertEquals("b", read2.value);

    ClassWithSerializedName read3 = adapter.read(new JsonReader(new StringReader("{\"alt2\":\"c\"}")));
    Assert.assertEquals("c", read3.value);

    ClassWithSerializedName read4 = adapter.read(new JsonReader(new StringReader("{\"unknown\":\"d\",\"alt1\":\"e\"}")));
    Assert.assertEquals("e", read4.value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicateFieldNamesThrowsException() {
    factory.create(gson, TypeToken.get(ClassWithDuplicateField.class));
  }

  @Test
  public void testInheritanceSerializationAndDeserialization() throws IOException {
    TypeAdapter<SubClass> adapter = factory.create(gson, TypeToken.get(SubClass.class));
    SubClass obj = new SubClass();
    obj.baseField = "baseVal";
    obj.subField = "subVal";

    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, obj);

    String json = sw.toString();
    Assert.assertTrue(json.contains("\"baseField\":\"baseVal\""));
    Assert.assertTrue(json.contains("\"subField\":\"subVal\""));

    SubClass deserialized = adapter.read(new JsonReader(new StringReader(json)));
    Assert.assertEquals("baseVal", deserialized.baseField);
    Assert.assertEquals("subVal", deserialized.subField);
  }

  @Test
  public void testJsonAdapterAnnotationOnField() throws IOException {
    TypeAdapter<ClassWithJsonAdapterField> adapter =
        factory.create(gson, TypeToken.get(ClassWithJsonAdapterField.class));

    ClassWithJsonAdapterField obj = new ClassWithJsonAdapterField("val");
    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, obj);

    Assert.assertEquals("{\"adaptedField\":\"custom:val\"}", sw.toString());

    ClassWithJsonAdapterField deserialized =
        adapter.read(new JsonReader(new StringReader("{\"adaptedField\":\"custom:newVal\"}")));
    Assert.assertEquals("newVal", deserialized.adaptedField);
  }

  @Test
  public void testRecursiveSelfReferenceAvoidance() throws IOException {
    TypeAdapter<RecursiveClass> adapter = factory.create(gson, TypeToken.get(RecursiveClass.class));
    RecursiveClass obj = new RecursiveClass("root");
    obj.self = obj;

    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, obj);

    Assert.assertEquals("{\"name\":\"root\"}", sw.toString());
  }

  @Test(expected = JsonSyntaxException.class)
  public void testSyntaxExceptionOnMalformedJson() throws IOException {
    TypeAdapter<SimpleClass> adapter = factory.create(gson, TypeToken.get(SimpleClass.class));
    adapter.read(new JsonReader(new StringReader("12345")));
  }

  @Test
  public void testPrimitiveDefaultNotOverriddenByNull() throws IOException {
    TypeAdapter<SimpleClass> adapter = factory.create(gson, TypeToken.get(SimpleClass.class));
    SimpleClass deserialized = adapter.read(new JsonReader(new StringReader("{\"intValue\":null}")));
    Assert.assertEquals(42, deserialized.intValue);
  }

  @Test
  public void testBoundFieldDirectAccess() throws Exception {
    ReflectiveTypeAdapterFactory.BoundField boundField =
        new ReflectiveTypeAdapterFactory.BoundField("testName", true, false) {
          @Override
          boolean writeField(Object value) {
            return serialized;
          }

          @Override
          void write(JsonWriter writer, Object value) throws IOException {
            writer.value(value.toString());
          }

          @Override
          void read(JsonReader reader, Object value) throws IOException {
            reader.skipValue();
          }
        };

    Assert.assertEquals("testName", boundField.name);
    Assert.assertTrue(boundField.serialized);
    Assert.assertFalse(boundField.deserialized);
    Assert.assertTrue(boundField.writeField("sample"));
  }

  @Test
  public void testConstructorConstructFailureInAdapter() {
    ObjectConstructor<SimpleClass> failingConstructor = new ObjectConstructor<SimpleClass>() {
      @Override
      public SimpleClass construct() {
        throw new RuntimeException("Construction failed");
      }
    };
    ReflectiveTypeAdapterFactory.Adapter<SimpleClass> adapter =
        new ReflectiveTypeAdapterFactory.Adapter<SimpleClass>(failingConstructor, new HashMap<String, ReflectiveTypeAdapterFactory.BoundField>());

    try {
      adapter.read(new JsonReader(new StringReader("{}")));
      Assert.fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      Assert.assertEquals("Construction failed", e.getMessage());
    } catch (IOException e) {
      Assert.fail("Unexpected IOException");
    }
  }
}
