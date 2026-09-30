package org.apache.commons.cli;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class TypeHandlerTest
{
    @Test
    public void testConstructor()
    {
        TypeHandler handler = new TypeHandler();
        Assert.assertNotNull(handler);
    }

    @Test
    public void testCreateValueObjectWithObjectParam() throws ParseException
    {
        Object result = TypeHandler.createValue("testString", (Object) PatternOptionBuilder.STRING_VALUE);
        Assert.assertEquals("testString", result);

        Object numberResult = TypeHandler.createValue("123", (Object) PatternOptionBuilder.NUMBER_VALUE);
        Assert.assertEquals(123L, numberResult);
    }

    @Test
    public void testCreateValueString() throws ParseException
    {
        Object result = TypeHandler.createValue("hello", PatternOptionBuilder.STRING_VALUE);
        Assert.assertEquals("hello", result);
    }

    @Test
    public void testCreateValueObject() throws ParseException
    {
        Object result = TypeHandler.createValue("java.util.ArrayList", PatternOptionBuilder.OBJECT_VALUE);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof ArrayList);
    }

    @Test
    public void testCreateValueNumber() throws ParseException
    {
        Object intResult = TypeHandler.createValue("42", PatternOptionBuilder.NUMBER_VALUE);
        Assert.assertEquals(42L, intResult);

        Object doubleResult = TypeHandler.createValue("42.5", PatternOptionBuilder.NUMBER_VALUE);
        Assert.assertEquals(42.5, doubleResult);
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
        Object result = TypeHandler.createValue("someFile.txt", PatternOptionBuilder.FILE_VALUE);
        Assert.assertTrue(result instanceof File);
        Assert.assertEquals("someFile.txt", ((File) result).getPath());
    }

    @Test
    public void testCreateValueExistingFile() throws IOException, ParseException
    {
        File tempFile = File.createTempFile("typehandler_test", ".tmp");
        tempFile.deleteOnExit();
        try
        {
            Object result = TypeHandler.createValue(tempFile.getAbsolutePath(), PatternOptionBuilder.EXISTING_FILE_VALUE);
            Assert.assertTrue(result instanceof FileInputStream);
            ((FileInputStream) result).close();
        }
        finally
        {
            tempFile.delete();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateValueFiles() throws ParseException
    {
        TypeHandler.createValue("file1,file2", PatternOptionBuilder.FILES_VALUE);
    }

    @Test
    public void testCreateValueURL() throws ParseException
    {
        Object result = TypeHandler.createValue("http://commons.apache.org", PatternOptionBuilder.URL_VALUE);
        Assert.assertTrue(result instanceof URL);
        Assert.assertEquals("http://commons.apache.org", result.toString());
    }

    @Test
    public void testCreateValueUnknownClass() throws ParseException
    {
        Object result = TypeHandler.createValue("anyValue", Integer.class);
        Assert.assertNull(result);
    }

    @Test
    public void testCreateObjectSuccess() throws ParseException
    {
        Object result = TypeHandler.createObject("java.lang.String");
        Assert.assertNotNull(result);
        Assert.assertEquals("", result);
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
            Assert.assertTrue(e.getMessage().contains("Unable to find the class: org.apache.commons.cli.NonExistingClass"));
        }
    }

    @Test
    public void testCreateObjectInstantiationFailure()
    {
        try
        {
            TypeHandler.createObject("java.lang.Void");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertTrue(e.getMessage().contains("Unable to create an instance of: java.lang.Void"));
        }
    }

    @Test
    public void testCreateNumberSuccess() throws ParseException
    {
        Number intNum = TypeHandler.createNumber("100");
        Assert.assertTrue(intNum instanceof Long);
        Assert.assertEquals(100L, intNum.longValue());

        Number negativeInt = TypeHandler.createNumber("-50");
        Assert.assertTrue(negativeInt instanceof Long);
        Assert.assertEquals(-50L, negativeInt.longValue());

        Number floatNum = TypeHandler.createNumber("123.456");
        Assert.assertTrue(floatNum instanceof Double);
        Assert.assertEquals(123.456, floatNum.doubleValue(), 0.00001);

        Number negativeFloat = TypeHandler.createNumber("-0.789");
        Assert.assertTrue(negativeFloat instanceof Double);
        Assert.assertEquals(-0.789, negativeFloat.doubleValue(), 0.00001);
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
    }

    @Test
    public void testCreateClassSuccess() throws ParseException
    {
        Class<?> clazz = TypeHandler.createClass("java.lang.Integer");
        Assert.assertEquals(Integer.class, clazz);
    }

    @Test
    public void testCreateClassNotFound()
    {
        try
        {
            TypeHandler.createClass("com.invalid.ClassXYZ");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertTrue(e.getMessage().contains("Unable to find the class: com.invalid.ClassXYZ"));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateDate()
    {
        TypeHandler.createDate("2023-01-01");
    }

    @Test
    public void testCreateURLSuccess() throws ParseException
    {
        URL url = TypeHandler.createURL("https://www.apache.org");
        Assert.assertEquals("https://www.apache.org", url.toString());
    }

    @Test
    public void testCreateURLMalformed()
    {
        try
        {
            TypeHandler.createURL("malformed://url with spaces:invalid");
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertTrue(e.getMessage().contains("Unable to parse the URL: malformed://url with spaces:invalid"));
        }
    }

    @Test
    public void testCreateFile()
    {
        File file = TypeHandler.createFile("path/to/my/file.txt");
        Assert.assertEquals("path/to/my/file.txt", file.getPath());
    }

    @Test
    public void testOpenFileSuccess() throws IOException, ParseException
    {
        File tempFile = File.createTempFile("open_file_test", ".tmp");
        tempFile.deleteOnExit();
        try
        {
            FileInputStream fis = TypeHandler.openFile(tempFile.getAbsolutePath());
            Assert.assertNotNull(fis);
            fis.close();
        }
        finally
        {
            tempFile.delete();
        }
    }

    @Test
    public void testOpenFileNonExistent()
    {
        String path = "non_existent_file_typehandler_test_99999.xyz";
        try
        {
            TypeHandler.openFile(path);
            Assert.fail("Expected ParseException");
        }
        catch (ParseException e)
        {
            Assert.assertTrue(e.getMessage().contains("Unable to find file: " + path));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateFiles()
    {
        TypeHandler.createFiles("path1,path2");
    }
}