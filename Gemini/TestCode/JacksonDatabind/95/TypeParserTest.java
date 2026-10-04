package com.fasterxml.jackson.databind.type;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.fasterxml.jackson.databind.JavaType;

public class TypeParserTest {

    private TypeFactory _typeFactory;
    private TypeParser _parser;

    @Before
    public void setUp() {
        _typeFactory = TypeFactory.defaultInstance();
        _parser = new TypeParser(_typeFactory);
    }

    @Test
    public void testWithFactorySame() {
        TypeParser same = _parser.withFactory(_typeFactory);
        Assert.assertSame(_parser, same);
    }

    @Test
    public void testWithFactoryDifferent() {
        TypeFactory newFactory = TypeFactory.defaultInstance().withModifier(null);
        TypeParser diff = _parser.withFactory(newFactory);
        Assert.assertNotSame(_parser, diff);
        Assert.assertSame(newFactory, diff._factory);
    }

    @Test
    public void testParseSimpleClass() {
        JavaType type = _parser.parse("java.lang.String");
        Assert.assertNotNull(type);
        Assert.assertEquals(String.class, type.getRawClass());
    }

    @Test
    public void testParseSimpleClassWithSpaces() {
        JavaType type = _parser.parse("  java.lang.Integer  ");
        Assert.assertNotNull(type);
        Assert.assertEquals(Integer.class, type.getRawClass());
    }

    @Test
    public void testParseGenericSingleParam() {
        JavaType type = _parser.parse("java.util.List<java.lang.String>");
        Assert.assertNotNull(type);
        Assert.assertEquals(List.class, type.getRawClass());
        Assert.assertEquals(1, type.containedTypeCount());
        Assert.assertEquals(String.class, type.containedType(0).getRawClass());
    }

    @Test
    public void testParseGenericMultipleParams() {
        JavaType type = _parser.parse("java.util.Map<java.lang.String, java.lang.Integer>");
        Assert.assertNotNull(type);
        Assert.assertEquals(java.util.Map.class, type.getRawClass());
        Assert.assertEquals(2, type.containedTypeCount());
        Assert.assertEquals(String.class, type.containedType(0).getRawClass());
        Assert.assertEquals(Integer.class, type.containedType(1).getRawClass());
    }

    @Test
    public void testParseNestedGenerics() {
        JavaType type = _parser.parse("java.util.Map<java.lang.String, java.util.List<java.lang.Long>>");
        Assert.assertNotNull(type);
        Assert.assertEquals(java.util.Map.class, type.getRawClass());
        Assert.assertEquals(2, type.containedTypeCount());
        JavaType valueType = type.containedType(1);
        Assert.assertEquals(List.class, valueType.getRawClass());
        Assert.assertEquals(Long.class, valueType.containedType(0).getRawClass());
    }

    @Test
    public void testParseEmptyString() {
        try {
            _parser.parse("");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test
    public void testParseWhitespaceOnly() {
        try {
            _parser.parse("   ");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test
    public void testParseUnexpectedTokensAfterCompleteType() {
        try {
            _parser.parse("java.lang.String extraToken");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected tokens after complete type"));
        }
    }

    @Test
    public void testParseUnclosedGeneric() {
        try {
            _parser.parse("java.util.List<java.lang.String");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test
    public void testParseMissingTypeInGenerics() {
        try {
            _parser.parse("java.util.List<");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test
    public void testParseUnexpectedTokenInGenerics() {
        try {
            _parser.parse("java.util.Map<java.lang.String; java.lang.Integer>");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("expected ',' or '>'"));
        }
    }

    @Test
    public void testParseClassNotFound() {
        try {
            _parser.parse("com.nonexistent.Class12345");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Can not locate class 'com.nonexistent.Class12345'"));
        }
    }

    @Test
    public void testFindClassRuntimeException() {
        TypeFactory customFactory = new TypeFactory(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> findClass(String className) throws ClassNotFoundException {
                throw new IllegalStateException("Custom runtime error");
            }
        };
        TypeParser customParser = new TypeParser(customFactory);
        try {
            customParser.parse("java.lang.String");
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("Custom runtime error", e.getMessage());
        }
    }

    @Test
    public void testTokenizerMethodsDirectly() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("a,b>c");
        Assert.assertEquals("a,b>c", tokenizer.getAllInput());
        Assert.assertTrue(tokenizer.hasMoreTokens());
        Assert.assertEquals("a", tokenizer.nextToken());
        Assert.assertEquals(",b>c", tokenizer.getRemainingInput());
        
        tokenizer.pushBack("a");
        Assert.assertTrue(tokenizer.hasMoreTokens());
        Assert.assertEquals("a", tokenizer.nextToken());

        Assert.assertEquals(",", tokenizer.nextToken());
        Assert.assertEquals("b", tokenizer.nextToken());
        Assert.assertEquals(">", tokenizer.nextToken());
        Assert.assertEquals("c", tokenizer.nextToken());
        Assert.assertFalse(tokenizer.hasMoreTokens());
    }

    @Test
    public void testParseTypesWithImmediateEnd() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("");
        try {
            _parser.parseTypes(tokenizer);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unexpected end-of-string"));
        }
    }

    @Test
    public void testProblemFormatting() {
        TypeParser.MyTokenizer tokenizer = new TypeParser.MyTokenizer("test.Type<other>");
        tokenizer.nextToken();
        IllegalArgumentException ex = _parser._problem(tokenizer, "Test problem");
        Assert.assertTrue(ex.getMessage().contains("test.Type<other>"));
        Assert.assertTrue(ex.getMessage().contains("Test problem"));
    }
}
