package org.apache.commons.cli;

import java.io.File;
import java.net.URL;
import java.util.Date;

import org.junit.Test;
import static org.junit.Assert.*;

public class TypeHandlerTest {

    @Test
    public void testCreateValueWithStringClass() {
        String input = "testString";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.STRING_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals(input, result);
    }

    @Test
    public void testCreateValueWithObjectClass() {
        String input = "java.lang.String";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("", result);
    }

    @Test
    public void testCreateValueWithNumberClass() {
        String input = "123";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof Number);
        assertEquals(123L, ((Number) result).longValue());
    }

    @Test
    public void testCreateValueWithDateClass() {
        String input = "2023-01-01";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.DATE_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithClassClass() {
        String input = "java.lang.String";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.CLASS_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof Class);
        assertEquals(String.class, result);
    }

    @Test
    public void testCreateValueWithFileClass() {
        String input = "test.txt";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals(input, ((File) result).getName());
    }

    @Test
    public void testCreateValueWithExistingFileClass() {
        String input = "test.txt";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals(input, ((File) result).getName());
    }

    @Test
    public void testCreateValueWithFilesClass() {
        String input = "test1.txt,test2.txt";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.FILES_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithURLClass() {
        String input = "http://example.com";
        Object result = TypeHandler.createValue(input, PatternOptionBuilder.URL_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof URL);
        assertEquals(input, result.toString());
    }

    @Test
    public void testCreateValueWithUnknownClass() {
        String input = "test";
        Object result = TypeHandler.createValue(input, Object.class);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndStringClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.STRING_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndObjectClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.OBJECT_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndNumberClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.NUMBER_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndDateClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.DATE_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndClassClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.CLASS_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndFileClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals("null", ((File) result).getName());
    }

    @Test
    public void testCreateValueWithNullStringAndExistingFileClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals("null", ((File) result).getName());
    }

    @Test
    public void testCreateValueWithNullStringAndFilesClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.FILES_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndURLClass() {
        Object result = TypeHandler.createValue(null, PatternOptionBuilder.URL_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithNullStringAndUnknownClass() {
        Object result = TypeHandler.createValue(null, Object.class);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithObjectTypeOverload() {
        String input = "java.lang.String";
        Object result = TypeHandler.createValue(input, (Object) PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("", result);
    }

    @Test
    public void testCreateObjectWithValidClass() {
        String className = "java.lang.String";
        Object result = TypeHandler.createObject(className);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("", result);
    }

    @Test
    public void testCreateObjectWithInvalidClass() {
        String className = "non.existent.Class";
        Object result = TypeHandler.createObject(className);
        assertNull(result);
    }

    @Test
    public void testCreateObjectWithClassWithoutDefaultConstructor() {
        String className = "java.lang.Integer";
        Object result = TypeHandler.createObject(className);
        assertNull(result);
    }

    @Test
    public void testCreateObjectWithNull() {
        Object result = TypeHandler.createObject(null);
        assertNull(result);
    }

    @Test
    public void testCreateNumberWithIntegerString() {
        Number result = TypeHandler.createNumber("123");
        assertNotNull(result);
        assertEquals(123L, result.longValue());
    }

    @Test
    public void testCreateNumberWithDoubleString() {
        Number result = TypeHandler.createNumber("123.45");
        assertNotNull(result);
        assertEquals(123.45, result.doubleValue(), 0.001);
    }

    @Test
    public void testCreateNumberWithInvalidString() {
        Number result = TypeHandler.createNumber("abc");
        assertNull(result);
    }

    @Test
    public void testCreateNumberWithNull() {
        Number result = TypeHandler.createNumber(null);
        assertNull(result);
    }

    @Test
    public void testCreateNumberWithEmptyString() {
        Number result = TypeHandler.createNumber("");
        assertNull(result);
    }

    @Test
    public void testCreateClassWithValidClassName() {
        Class result = TypeHandler.createClass("java.lang.String");
        assertNotNull(result);
        assertEquals(String.class, result);
    }

    @Test
    public void testCreateClassWithInvalidClassName() {
        Class result = TypeHandler.createClass("non.existent.Class");
        assertNull(result);
    }

    @Test
    public void testCreateClassWithNull() {
        Class result = TypeHandler.createClass(null);
        assertNull(result);
    }

    @Test
    public void testCreateDateWithAnyString() {
        Date result = TypeHandler.createDate("2023-01-01");
        assertNull(result);
    }

    @Test
    public void testCreateDateWithNull() {
        Date result = TypeHandler.createDate(null);
        assertNull(result);
    }

    @Test
    public void testCreateURLWithValidURL() {
        URL result = TypeHandler.createURL("http://example.com");
        assertNotNull(result);
        assertEquals("http://example.com", result.toString());
    }

    @Test
    public void testCreateURLWithInvalidURL() {
        URL result = TypeHandler.createURL("invalid-url");
        assertNull(result);
    }

    @Test
    public void testCreateURLWithNull() {
        URL result = TypeHandler.createURL(null);
        assertNull(result);
    }

    @Test
    public void testCreateFileWithValidPath() {
        File result = TypeHandler.createFile("test.txt");
        assertNotNull(result);
        assertEquals("test.txt", result.getName());
    }

    @Test
    public void testCreateFileWithNull() {
        File result = TypeHandler.createFile(null);
        assertNotNull(result);
        assertEquals("null", result.getName());
    }

    @Test
    public void testCreateFileWithEmptyString() {
        File result = TypeHandler.createFile("");
        assertNotNull(result);
        assertEquals("", result.getName());
    }

    @Test
    public void testCreateFilesWithAnyString() {
        File[] result = TypeHandler.createFiles("test1.txt,test2.txt");
        assertNull(result);
    }

    @Test
    public void testCreateFilesWithNull() {
        File[] result = TypeHandler.createFiles(null);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithEmptyStringAndStringClass() {
        Object result = TypeHandler.createValue("", PatternOptionBuilder.STRING_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("", result);
    }

    @Test
    public void testCreateValueWithEmptyStringAndNumberClass() {
        Object result = TypeHandler.createValue("", PatternOptionBuilder.NUMBER_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithEmptyStringAndURLClass() {
        Object result = TypeHandler.createValue("", PatternOptionBuilder.URL_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithEmptyStringAndFileClass() {
        Object result = TypeHandler.createValue("", PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals("", ((File) result).getName());
    }

    @Test
    public void testCreateNumberWithNegativeNumber() {
        Number result = TypeHandler.createNumber("-456");
        assertNotNull(result);
        assertEquals(-456L, result.longValue());
    }

    @Test
    public void testCreateNumberWithZero() {
        Number result = TypeHandler.createNumber("0");
        assertNotNull(result);
        assertEquals(0L, result.longValue());
    }

    @Test
    public void testCreateNumberWithLargeNumber() {
        Number result = TypeHandler.createNumber("9999999999");
        assertNotNull(result);
        assertEquals(9999999999L, result.longValue());
    }

    @Test
    public void testCreateNumberWithScientificNotation() {
        Number result = TypeHandler.createNumber("1.23e2");
        assertNotNull(result);
        assertEquals(123.0, result.doubleValue(), 0.001);
    }

    @Test
    public void testCreateURLWithFileProtocol() {
        URL result = TypeHandler.createURL("file:///tmp/test.txt");
        assertNotNull(result);
        assertEquals("file:///tmp/test.txt", result.toString());
    }

    @Test
    public void testCreateURLWithHTTPSProtocol() {
        URL result = TypeHandler.createURL("https://example.com");
        assertNotNull(result);
        assertEquals("https://example.com", result.toString());
    }

    @Test
    public void testCreateFileWithAbsolutePath() {
        File result = TypeHandler.createFile("/tmp/test.txt");
        assertNotNull(result);
        assertEquals("test.txt", result.getName());
    }

    @Test
    public void testCreateFileWithRelativePath() {
        File result = TypeHandler.createFile("../test.txt");
        assertNotNull(result);
        assertEquals("test.txt", result.getName());
    }

    @Test
    public void testCreateObjectWithPrimitiveWrapperClass() {
        String className = "java.lang.Boolean";
        Object result = TypeHandler.createObject(className);
        assertNull(result);
    }

    @Test
    public void testCreateClassWithPrimitiveClassName() {
        Class result = TypeHandler.createClass("int");
        assertNull(result);
    }

    @Test
    public void testCreateClassWithArrayClassName() {
        Class result = TypeHandler.createClass("[Ljava.lang.String;");
        assertNotNull(result);
        assertTrue(result.isArray());
    }

    @Test
    public void testCreateValueWithNumberClassAndDoubleString() {
        Object result = TypeHandler.createValue("3.14", PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof Number);
        assertEquals(3.14, ((Number) result).doubleValue(), 0.001);
    }

    @Test
    public void testCreateValueWithNumberClassAndInvalidString() {
        Object result = TypeHandler.createValue("notanumber", PatternOptionBuilder.NUMBER_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithURLClassAndInvalidString() {
        Object result = TypeHandler.createValue("not a url", PatternOptionBuilder.URL_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithClassClassAndInvalidString() {
        Object result = TypeHandler.createValue("not.a.class", PatternOptionBuilder.CLASS_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueWithObjectClassAndInvalidString() {
        Object result = TypeHandler.createValue("not.a.class", PatternOptionBuilder.OBJECT_VALUE);
        assertNull(result);
    }
}
