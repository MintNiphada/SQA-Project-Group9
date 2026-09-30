package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Date;

public class TypeHandlerTest
{
    public static class PrivateConstructorClass
    {
        private PrivateConstructorClass()
        {
        }
    }

    public static abstract class AbstractTestClass
    {
        public AbstractTestClass()
        {
        }
    }

    public static class InstantiationThrowingClass
    {
        public InstantiationThrowingClass()
        {
            throw new RuntimeException("Instantiation failed deliberately");
        }
    }

    @Test
    public void testConstructor()
    {
        TypeHandler handler = new TypeHandler();
        Assert.assertNotNull(handler);
    }

    @Test
    public void testCreateValueString() throws ParseException
    {
        Object result = TypeHandler.createValue("testString", PatternOptionBuilder.STRING_VALUE);
        Assert.assertEquals("testString", result);

        Object resultViaObject = TypeHandler.createValue("testString", (Object) PatternOptionBuilder.STRING_VALUE);
        Assert.assertEquals("testString", resultViaObject);
    }

    @Test
    public void testCreateValueObject() throws ParseException
    {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.OBJECT_VALUE);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof String);
    }

    @Test
    public void testCreateValueNumberDouble() throws ParseException
    {
        Object result = TypeHandler.createValue("123.45", PatternOptionBuilder.NUMBER_VALUE);
        Assert.assertEquals(Double.valueOf(123.45), result);
    }

    @Test
    public void testCreateValueNumberLong() throws ParseException
    {
        Object result = TypeHandler.createValue("12345", PatternOptionBuilder.NUMBER_VALUE);
        Assert.assertEquals(Long.valueOf(12345L), result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValueDate() throws ParseException
    {
        TypeHandler.createValue("2023-01-01", PatternOptionBuilder.DATE_VALUE);
    }

    @Test
    public void testCreateValueClass() throws ParseException
    {
        Object result = TypeHandler.createValue("java.lang.String", PatternOptionBuilder.CLASS_VALUE);
        Assert.assertEquals(String.class, result);
    }

    @Test
    public void testCreateValueFile() throws ParseException
    {
        Object result = TypeHandler.createValue("test.txt", PatternOptionBuilder.FILE_VALUE);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof File);
        Assert.assertEquals(new File("test.txt"), result);
    }

    @Test
    public void testCreateValueExistingFile() throws ParseException
    {
        Object result = TypeHandler.createValue("someExistingFile.txt", PatternOptionBuilder.EXISTING_FILE_VALUE);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof File);
        Assert.assertEquals(new File("someExistingFile.txt"), result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValueFiles() throws ParseException
    {
        TypeHandler.createValue("test.txt", PatternOptionBuilder.FILES_VALUE);
    }

    @Test
    public void testCreateValueURL() throws ParseException
    {
        Object result = TypeHandler.createValue("http://www.apache.org", PatternOptionBuilder.URL_VALUE);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof URL);
        Assert.assertEquals("http://www.apache.org", result.toString());
    }

    @Test
    public void testCreateValueUnknownClass() throws ParseException
    {
        Object result = TypeHandler.createValue("something", Integer.class);
        Assert.assertNull(result);

        Object resultWithNullClass = TypeHandler.createValue("something", (Class<?>) null);
        Assert.assertNull(resultWithNullClass);
    }

    @Test
    public void testCreateObjectSuccess() throws ParseException
    {
        Object result = TypeHandler.createObject("java.lang.StringBuilder");
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof StringBuilder);
    }

    @Test
    public void testCreateObjectClassNotFound()
    {
        try
        {
            TypeHandler.createObject("org.apache.commons.cli.NonExistingClass");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertEquals("Unable to find the class: org.apache.commons.cli.NonExistingClass", e.getMessage());
        }
    }

    @Test
    public void testCreateObjectPrivateConstructor()
    {
        try
        {
            TypeHandler.createObject(PrivateConstructorClass.class.getName());
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertTrue(e.getMessage().contains("IllegalAccessException")
                    || e.getMessage().contains("Unable to create an instance of: " + PrivateConstructorClass.class.getName()));
        }
    }

    @Test
    public void testCreateObjectAbstractClass()
    {
        try
        {
            TypeHandler.createObject(AbstractTestClass.class.getName());
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertTrue(e.getMessage().contains("InstantiationException")
                    || e.getMessage().contains("Unable to create an instance of: " + AbstractTestClass.class.getName()));
        }
    }

    @Test
    public void testCreateObjectExceptionInConstructor()
    {
        try
        {
            TypeHandler.createObject(InstantiationThrowingClass.class.getName());
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertTrue(e.getMessage().contains("Unable to create an instance of: " + InstantiationThrowingClass.class.getName()));
        }
    }

    @Test
    public void testCreateNumberValidLong() throws ParseException
    {
        Number result = TypeHandler.createNumber("1234567890");
        Assert.assertEquals(1234567890L, result);

        Number negative = TypeHandler.createNumber("-42");
        Assert.assertEquals(-42L, negative);

        Number zero = TypeHandler.createNumber("0");
        Assert.assertEquals(0L, zero);
    }

    @Test
    public void testCreateNumberValidDouble() throws ParseException
    {
        Number result = TypeHandler.createNumber("12345.6789");
        Assert.assertEquals(12345.6789, result);

        Number negative = TypeHandler.createNumber("-0.5");
        Assert.assertEquals(-0.5, negative);

        Number dotOnly = TypeHandler.createNumber(".5");
        Assert.assertEquals(0.5, dotOnly);
    }

    @Test
    public void testCreateNumberInvalid()
    {
        try
        {
            TypeHandler.createNumber("not_a_number");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertNotNull(e.getMessage());
        }

        try
        {
            TypeHandler.createNumber("12.34.56");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertNotNull(e.getMessage());
        }

        try
        {
            TypeHandler.createNumber("");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testCreateClassSuccess() throws ParseException
    {
        Class<?> clazz = TypeHandler.createClass("java.lang.Integer");
        Assert.assertEquals(Integer.class, clazz);

        Class<?> arrayClass = TypeHandler.createClass("[Ljava.lang.String;");
        Assert.assertEquals(String[].class, arrayClass);
    }

    @Test
    public void testCreateClassNotFound()
    {
        try
        {
            TypeHandler.createClass("org.apache.commons.cli.UnknownClassName");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertEquals("Unable to find the class: org.apache.commons.cli.UnknownClassName", e.getMessage());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateDateThrowsUnsupportedOperationException()
    {
        TypeHandler.createDate("2023-10-15");
    }

    @Test
    public void testCreateURLSuccess() throws ParseException
    {
        URL url = TypeHandler.createURL("http://commons.apache.org");
        Assert.assertEquals("http://commons.apache.org", url.toString());

        URL fileUrl = TypeHandler.createURL("file:/tmp/test.txt");
        Assert.assertEquals("file:/tmp/test.txt", fileUrl.toString());
    }

    @Test
    public void testCreateURLInvalid()
    {
        try
        {
            TypeHandler.createURL("invalid_url_protocol://test");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertEquals("Unable to parse the URL: invalid_url_protocol://test", e.getMessage());
        }

        try
        {
            TypeHandler.createURL("not a url");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertEquals("Unable to parse the URL: not a url", e.getMessage());
        }
    }

    @Test
    public void testCreateFile()
    {
        File file = TypeHandler.createFile("path/to/file.txt");
        Assert.assertNotNull(file);
        Assert.assertEquals("file.txt", file.getName());
        Assert.assertEquals(new File("path/to/file.txt"), file);

        File emptyFile = TypeHandler.createFile("");
        Assert.assertNotNull(emptyFile);
        Assert.assertEquals("", emptyFile.getPath());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFilesThrowsUnsupportedOperationException()
    {
        TypeHandler.createFiles("path/to/files");
    }
}