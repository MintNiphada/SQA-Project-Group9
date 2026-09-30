package org.apache.commons.cli;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test suite for {@link TypeHandler}.
 */
public class TypeHandlerTest {

    private static class PrivateTestClass {
        private PrivateTestClass() {
        }
    }

    @Test
    public void testConstructor() {
        TypeHandler handler = new TypeHandler();
        assertNotNull(handler);
    }

    @Test
    public void testCreateValueString() {
        Object result = TypeHandler.createValue("testString", PatternOptionBuilder.STRING_VALUE);
        assertEquals("testString", result);
        assertTrue(result instanceof String);

        Object resultViaObjectMethod = TypeHandler.createValue("testString", (Object) PatternOptionBuilder.STRING_VALUE);
        assertEquals("testString", resultViaObjectMethod);
    }

    @Test
    public void testCreateValueObject() {
        Object result = TypeHandler.createValue("java.util.ArrayList", PatternOptionBuilder.OBJECT_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof ArrayList);

        Object resultInvalid = TypeHandler.createValue("non.existing.Class", PatternOptionBuilder.OBJECT_VALUE);
        assertNull(resultInvalid);
    }

    @Test
    public void testCreateValueNumber() {
        Object resultLong = TypeHandler.createValue("123", PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull(resultLong);
        assertTrue(resultLong instanceof Number);
        assertEquals(123L, ((Number) resultLong).longValue());

        Object resultDouble = TypeHandler.createValue("123.45", PatternOptionBuilder.NUMBER_VALUE);
        assertNotNull(resultDouble);
        assertTrue(resultDouble instanceof Double);
        assertEquals(123.45, ((Number) resultDouble).doubleValue(), 0.0001);

        Object resultInvalid = TypeHandler.createValue("not-a-number", PatternOptionBuilder.NUMBER_VALUE);
        assertNull(resultInvalid);
    }

    @Test
    public void testCreateValueDate() {
        Object result = TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueClass() {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        assertNotNull(result);
        assertEquals(String.class, result);

        Object resultInvalid = TypeHandler.createValue("non.existing.Class", PatternOptionBuilder.CLASS_VALUE);
        assertNull(resultInvalid);
    }

    @Test
    public void testCreateValueFile() {
        Object result = TypeHandler.createValue("test.txt", PatternOptionBuilder.FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals("test.txt", ((File) result).getName());
    }

    @Test
    public void testCreateValueExistingFile() {
        Object result = TypeHandler.createValue("existing_file.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals("existing_file.txt", ((File) result).getName());
    }

    @Test
    public void testCreateValueFiles() {
        Object result = TypeHandler.createValue("test_files", PatternOptionBuilder.FILES_VALUE);
        assertNull(result);
    }

    @Test
    public void testCreateValueURL() {
        Object result = TypeHandler.createValue("http://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        assertNotNull(result);
        assertTrue(result instanceof URL);
        assertEquals("http://commons.apache.org", ((URL) result).toExternalForm());

        Object resultInvalid = TypeHandler.createValue("invalid-url-string", PatternOptionBuilder.URL_VALUE);
        assertNull(resultInvalid);
    }

    @Test
    public void testCreateValueUnknownClass() {
        Object result = TypeHandler.createValue("anyValue", Void.class);
        assertNull(result);

        Object resultNullClass = TypeHandler.createValue("anyValue", (Class) null);
        assertNull(resultNullClass);
    }

    @Test(expected = ClassCastException.class)
    public void testCreateValueInvalidTypeObject() {
        TypeHandler.createValue("anyValue", new Object());
    }

    @Test
    public void testCreateObjectSuccess() {
        Object obj = TypeHandler.createObject("java.lang.String");
        assertNotNull(obj);
        assertEquals("", obj);
    }

    @Test
    public void testCreateObjectClassNotFound() {
        Object obj = TypeHandler.createObject("com.invalid.NonExistingClassName");
        assertNull(obj);
    }

    @Test
    public void testCreateObjectInstantiationException() {
        Object obj = TypeHandler.createObject("java.lang.Number");
        assertNull(obj);

        Object objInterface = TypeHandler.createObject("java.util.List");
        assertNull(objInterface);
    }

    @Test
    public void testCreateObjectIllegalAccessException() {
        Object obj = TypeHandler.createObject("org.apache.commons.cli.TypeHandlerTest$PrivateTestClass");
        assertNull(obj);
    }

    @Test
    public void testCreateNumber() {
        Number intNum = TypeHandler.createNumber("42");
        assertNotNull(intNum);
        assertEquals(42L, intNum.longValue());

        Number doubleNum = TypeHandler.createNumber("3.14159");
        assertNotNull(doubleNum);
        assertEquals(3.14159, doubleNum.doubleValue(), 0.00001);

        Number invalidNum = TypeHandler.createNumber("not_a_number");
        assertNull(invalidNum);

        Number nullNum = TypeHandler.createNumber(null);
        assertNull(nullNum);
    }

    @Test
    public void testCreateClass() {
        Class<?> clazz = TypeHandler.createClass("java.lang.Integer");
        assertNotNull(clazz);
        assertEquals(Integer.class, clazz);

        Class<?> invalidClazz = TypeHandler.createClass("invalid.ClassName");
        assertNull(invalidClazz);
    }

    @Test
    public void testCreateDate() {
        assertNull(TypeHandler.createDate("2023-10-10"));
        assertNull(TypeHandler.createDate(""));
        assertNull(TypeHandler.createDate(null));
    }

    @Test
    public void testCreateURL() {
        URL url = TypeHandler.createURL("https://www.apache.org/");
        assertNotNull(url);
        assertEquals("https://www.apache.org/", url.toExternalForm());

        URL invalidUrl = TypeHandler.createURL("malformed url");
        assertNull(invalidUrl);

        URL nullUrl = TypeHandler.createURL(null);
        assertNull(nullUrl);
    }

    @Test
    public void testCreateFile() {
        File file = TypeHandler.createFile("/tmp/some_file.txt");
        assertNotNull(file);
        assertEquals(new File("/tmp/some_file.txt"), file);
    }

    @Test
    public void testCreateFiles() {
        assertNull(TypeHandler.createFiles("path1;path2"));
        assertNull(TypeHandler.createFiles(null));
    }
}