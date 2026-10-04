package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BuilderBasedDeserializerTest {

    public interface Views {
        interface Public {}
        interface Internal extends Views.Public {}
    }

    @JsonDeserialize(builder = SimpleBean.Builder.class)
    public static class SimpleBean {
        private final int x;
        private final String y;

        SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public String getY() { return y; }

        @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
        public static class Builder {
            private int x;
            private String y;

            public Builder setX(int x) {
                this.x = x;
                return this;
            }

            public Builder setY(String y) {
                this.y = y;
                return this;
            }

            public SimpleBean create() {
                return new SimpleBean(x, y);
            }
        }
    }

    @JsonDeserialize(builder = NoBuildMethodBean.Builder.class)
    public static class NoBuildMethodBean {
        @JsonPOJOBuilder(buildMethodName = "")
        public static class Builder {
            public int x;
            public Builder withX(int x) {
                this.x = x;
                return this;
            }
        }
    }

    @JsonDeserialize(builder = FailingBuildBean.Builder.class)
    public static class FailingBuildBean {
        @JsonPOJOBuilder
        public static class Builder {
            public FailingBuildBean build() {
                throw new IllegalStateException("Build failed intentional");
            }
        }
    }

    @JsonDeserialize(builder = CreatorBean.Builder.class)
    public static class CreatorBean {
        final int id;
        final String name;
        final int extra;

        CreatorBean(int id, String name, int extra) {
            this.id = id;
            this.name = name;
            this.extra = extra;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Builder {
            private final int id;
            private final String name;
            private int extra;

            @JsonCreator
            public Builder(@JsonProperty("id") int id, @JsonProperty("name") String name) {
                this.id = id;
                this.name = name;
            }

            public Builder withExtra(int extra) {
                this.extra = extra;
                return this;
            }

            public CreatorBean build() {
                return new CreatorBean(id, name, extra);
            }
        }
    }

    @JsonDeserialize(builder = ViewBean.Builder.class)
    public static class ViewBean {
        final String publicVal;
        final String internalVal;

        ViewBean(String p, String i) {
            this.publicVal = p;
            this.internalVal = i;
        }

        public static class Builder {
            @JsonView(Views.Public.class)
            public String publicVal;

            @JsonView(Views.Internal.class)
            public String internalVal;

            public ViewBean build() {
                return new ViewBean(publicVal, internalVal);
            }
        }
    }

    @JsonDeserialize(builder = AnySetterBean.Builder.class)
    public static class AnySetterBean {
        final Map<String, Object> map;

        AnySetterBean(Map<String, Object> map) {
            this.map = map;
        }

        public static class Builder {
            private final Map<String, Object> values = new HashMap<String, Object>();

            @JsonAnySetter
            public Builder add(String key, Object value) {
                values.put(key, value);
                return this;
            }

            public AnySetterBean build() {
                return new AnySetterBean(values);
            }
        }
    }

    public static class UnwrappedLocation {
        public int x;
        public int y;
    }

    @JsonDeserialize(builder = OuterUnwrappedBean.Builder.class)
    public static class OuterUnwrappedBean {
        final String name;
        final UnwrappedLocation loc;

        OuterUnwrappedBean(String name, UnwrappedLocation loc) {
            this.name = name;
            this.loc = loc;
        }

        public static class Builder {
            private String name;
            private UnwrappedLocation loc;

            public Builder withName(String name) {
                this.name = name;
                return this;
            }

            @JsonUnwrapped
            public Builder withLoc(UnwrappedLocation loc) {
                this.loc = loc;
                return this;
            }

            public OuterUnwrappedBean build() {
                return new OuterUnwrappedBean(name, loc);
            }
        }
    }

    @JsonDeserialize(builder = ExtTypeBean.Builder.class)
    public static class ExtTypeBean {
        final Object value;

        ExtTypeBean(Object value) {
            this.value = value;
        }

        public static class Builder {
            private Object value;

            @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
            public Builder setValue(Object value) {
                this.value = value;
                return this;
            }

            public ExtTypeBean build() {
                return new ExtTypeBean(value);
            }
        }
    }

    @JsonDeserialize(builder = ExtTypeCreatorBean.Builder.class)
    public static class ExtTypeCreatorBean {
        final Object value;

        ExtTypeCreatorBean(Object value) {
            this.value = value;
        }

        public static class Builder {
            private final Object value;

            @JsonCreator
            public Builder(@JsonProperty("value")
                           @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
                           Object value) {
                this.value = value;
            }

            public ExtTypeCreatorBean build() {
                return new ExtTypeCreatorBean(value);
            }
        }
    }

    @JsonDeserialize(builder = FromScalarBean.Builder.class)
    public static class FromScalarBean {
        final String text;

        FromScalarBean(String text) {
            this.text = text;
        }

        public static class Builder {
            private String text;

            @JsonCreator
            public Builder(String text) {
                this.text = text;
            }

            public FromScalarBean build() {
                return new FromScalarBean(text);
            }
        }
    }

    @JsonDeserialize(builder = FromIntBean.Builder.class)
    public static class FromIntBean {
        final int val;

        FromIntBean(int val) {
            this.val = val;
        }

        public static class Builder {
            private int val;

            @JsonCreator
            public Builder(int val) {
                this.val = val;
            }

            public FromIntBean build() {
                return new FromIntBean(val);
            }
        }
    }

    @JsonDeserialize(builder = FromDoubleBean.Builder.class)
    public static class FromDoubleBean {
        final double val;

        FromDoubleBean(double val) {
            this.val = val;
        }

        public static class Builder {
            private double val;

            @JsonCreator
            public Builder(double val) {
                this.val = val;
            }

            public FromDoubleBean build() {
                return new FromDoubleBean(val);
            }
        }
    }

    @JsonDeserialize(builder = FromBooleanBean.Builder.class)
    public static class FromBooleanBean {
        final boolean val;

        FromBooleanBean(boolean val) {
            this.val = val;
        }

        public static class Builder {
            private boolean val;

            @JsonCreator
            public Builder(boolean val) {
                this.val = val;
            }

            public FromBooleanBean build() {
                return new FromBooleanBean(val);
            }
        }
    }

    @Test
    public void testVanillaAndStandardDeserialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"x\":42,\"y\":\"hello\"}";
        SimpleBean result = mapper.readValue(json, SimpleBean.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(42, result.getX());
        Assert.assertEquals("hello", result.getY());
    }

    @Test
    public void testNoBuildMethodReturnsBuilder() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"withX\":7}";
        Object result = mapper.readValue(json, NoBuildMethodBean.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof NoBuildMethodBean.Builder);
        Assert.assertEquals(7, ((NoBuildMethodBean.Builder) result).x);
    }

    @Test(expected = JsonMappingException.class)
    public void testFailingBuildMethodThrows() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{}", FailingBuildBean.class);
    }

    @Test
    public void testPropertyBasedCreator() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":101,\"unknownProp\":\"ignored\",\"extra\":55,\"name\":\"Jackson\"}";
        CreatorBean result = mapper.readValue(json, CreatorBean.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(101, result.id);
        Assert.assertEquals("Jackson", result.name);
        Assert.assertEquals(55, result.extra);
    }

    @Test
    public void testViews() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"publicVal\":\"pub\",\"internalVal\":\"priv\"}";

        ViewBean publicResult = mapper.readerWithView(Views.Public.class)
                .forType(ViewBean.class)
                .readValue(json);
        Assert.assertEquals("pub", publicResult.publicVal);
        Assert.assertNull(publicResult.internalVal);

        ViewBean internalResult = mapper.readerWithView(Views.Internal.class)
                .forType(ViewBean.class)
                .readValue(json);
        Assert.assertEquals("pub", internalResult.publicVal);
        Assert.assertEquals("priv", internalResult.internalVal);
    }

    @Test
    public void testAnySetter() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"key1\":\"value1\",\"key2\":123}";
        AnySetterBean result = mapper.readValue(json, AnySetterBean.class);
        Assert.assertNotNull(result);
        Assert.assertEquals("value1", result.map.get("key1"));
        Assert.assertEquals(123, result.map.get("key2"));
    }

    @Test
    public void testUnwrapped() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"PointA\",\"x\":10,\"y\":20}";
        OuterUnwrappedBean result = mapper.readValue(json, OuterUnwrappedBean.class);
        Assert.assertNotNull(result);
        Assert.assertEquals("PointA", result.name);
        Assert.assertNotNull(result.loc);
        Assert.assertEquals(10, result.loc.x);
        Assert.assertEquals(20, result.loc.y);
    }

    @Test
    public void testExternalTypeId() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SimpleBean.class);
        String json = "{\"value\":{\"x\":1,\"y\":\"test\"},\"extType\":\"BuilderBasedDeserializerTest$SimpleBean\"}";
        ExtTypeBean result = mapper.readValue(json, ExtTypeBean.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.value instanceof SimpleBean);
        Assert.assertEquals(1, ((SimpleBean) result.value).getX());
    }

    @Test(expected = JsonMappingException.class)
    public void testExternalTypeIdWithCreatorThrows() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SimpleBean.class);
        String json = "{\"value\":{\"x\":1,\"y\":\"test\"},\"extType\":\"BuilderBasedDeserializerTest$SimpleBean\"}";
        mapper.readValue(json, ExtTypeCreatorBean.class);
    }

    @Test
    public void testScalars() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        FromScalarBean strBean = mapper.readValue("\"stringValue\"", FromScalarBean.class);
        Assert.assertEquals("stringValue", strBean.text);

        FromIntBean intBean = mapper.readValue("1234", FromIntBean.class);
        Assert.assertEquals(1234, intBean.val);

        FromDoubleBean dblBean = mapper.readValue("12.34", FromDoubleBean.class);
        Assert.assertEquals(12.34, dblBean.val, 0.0001);

        FromBooleanBean boolBean = mapper.readValue("true", FromBooleanBean.class);
        Assert.assertTrue(boolBean.val);
    }

    @Test(expected = JsonMappingException.class)
    public void testUnexpectedTokenThrows() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("[1, 2, 3]", SimpleBean.class);
    }

    @Test(expected = JsonMappingException.class)
    public void testUnknownPropertyVanilla() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.readValue("{\"unknownField\": true}", SimpleBean.class);
    }
}
