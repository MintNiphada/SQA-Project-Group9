package org.apache.commons.compress.utils;

import java.lang.reflect.Constructor;
import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

public class ArchiveUtilsTest {

    private static class TestArchiveEntry implements ArchiveEntry {
        private final String name;
        private final long size;
        private final boolean isDir;

        TestArchiveEntry(String name, long size, boolean isDir) {
            this.name = name;
            this.size = size;
            this.isDir = isDir;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public boolean isDirectory() {
            return isDir;
        }

        public Date getLastModifiedDate() {
            return null;
        }
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor<ArchiveUtils> constructor = ArchiveUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ArchiveUtils instance = constructor.newInstance();
        Assert.assertNotNull(instance);
    }

    @Test
    public void testToStringFile() {
        ArchiveEntry entry = new TestArchiveEntry("main.c", 2000L, false);
        Assert.assertEquals("-    2000 main.c", ArchiveUtils.toString(entry));
    }

    @Test
    public void testToStringDirectory() {
        ArchiveEntry entry = new TestArchiveEntry("testfiles", 100L, true);
        Assert.assertEquals("d     100 testfiles", ArchiveUtils.toString(entry));
    }

    @Test
    public void testToStringLargeSize() {
        ArchiveEntry entry = new TestArchiveEntry("large.bin", 12345678L, false);
        Assert.assertEquals("- 12345678 large.bin", ArchiveUtils.toString(entry));
    }

    @Test
    public void testMatchAsciiBuffer() {
        byte[] buffer = "Hello World".getBytes();
        Assert.assertTrue(ArchiveUtils.matchAsciiBuffer("Hello World", buffer));
        Assert.assertFalse(ArchiveUtils.matchAsciiBuffer("Hello", buffer));
        Assert.assertTrue(ArchiveUtils.matchAsciiBuffer("World", buffer, 6, 5));
        Assert.assertFalse(ArchiveUtils.matchAsciiBuffer("World", buffer, 6, 4));
        Assert.assertFalse(ArchiveUtils.matchAsciiBuffer("Earth", buffer, 6, 5));
    }

    @Test
    public void testToAsciiBytes() {
        byte[] expected = new byte[]{'t', 'e', 's', 't'};
        Assert.assertArrayEquals(expected, ArchiveUtils.toAsciiBytes("test"));
    }

    @Test
    public void testToAsciiString() {
        byte[] bytes = new byte[]{'h', 'e', 'l', 'l', 'o'};
        Assert.assertEquals("hello", ArchiveUtils.toAsciiString(bytes));
        Assert.assertEquals("ell", ArchiveUtils.toAsciiString(bytes, 1, 3));
    }

    @Test
    public void testIsEqualSimple() {
        byte[] b1 = new byte[]{1, 2, 3};
        byte[] b2 = new byte[]{1, 2, 3};
        byte[] b3 = new byte[]{1, 2, 4};
        byte[] b4 = new byte[]{1, 2};

        Assert.assertTrue(ArchiveUtils.isEqual(b1, b2));
        Assert.assertFalse(ArchiveUtils.isEqual(b1, b3));
        Assert.assertFalse(ArchiveUtils.isEqual(b1, b4));
    }

    @Test
    public void testIsEqualWithOffsets() {
        byte[] b1 = new byte[]{0, 1, 2, 3, 0};
        byte[] b2 = new byte[]{9, 1, 2, 3, 9};

        Assert.assertTrue(ArchiveUtils.isEqual(b1, 1, 3, b2, 1, 3));
        Assert.assertFalse(ArchiveUtils.isEqual(b1, 1, 3, b2, 1, 4));
        Assert.assertFalse(ArchiveUtils.isEqual(b1, 0, 3, b2, 1, 3));
    }

    @Test
    public void testIsEqualTrailingNulls() {
        byte[] b1 = new byte[]{1, 2, 3, 0, 0};
        byte[] b2 = new byte[]{1, 2, 3};
        byte[] b3 = new byte[]{1, 2, 3, 0, 4};

        Assert.assertTrue(ArchiveUtils.isEqual(b1, b2, true));
        Assert.assertTrue(ArchiveUtils.isEqual(b2, b1, true));
        Assert.assertFalse(ArchiveUtils.isEqual(b1, b2, false));
        Assert.assertFalse(ArchiveUtils.isEqual(b3, b2, true));
        Assert.assertFalse(ArchiveUtils.isEqual(b2, b3, true));
    }

    @Test
    public void testIsEqualWithNullMethod() {
        byte[] b1 = new byte[]{1, 2, 0, 0};
        byte[] b2 = new byte[]{9, 1, 2, 9};

        Assert.assertTrue(ArchiveUtils.isEqualWithNull(b1, 0, 4, b2, 1, 2));
        Assert.assertTrue(ArchiveUtils.isEqualWithNull(b2, 1, 2, b1, 0, 4));
        Assert.assertFalse(ArchiveUtils.isEqualWithNull(b1, 0, 4, b2, 1, 3));
    }

    @Test
    public void testIsArrayZero() {
        byte[] allZero = new byte[]{0, 0, 0, 0};
        byte[] nonZero = new byte[]{0, 0, 1, 0};

        Assert.assertTrue(ArchiveUtils.isArrayZero(allZero, 4));
        Assert.assertTrue(ArchiveUtils.isArrayZero(allZero, 0));
        Assert.assertTrue(ArchiveUtils.isArrayZero(nonZero, 2));
        Assert.assertFalse(ArchiveUtils.isArrayZero(nonZero, 3));
    }

    @Test
    public void testSanitize() {
        Assert.assertEquals("Hello World", ArchiveUtils.sanitize("Hello World"));
        Assert.assertEquals("Hello?World", ArchiveUtils.sanitize("Hello\nWorld"));
        Assert.assertEquals("Hello?World", ArchiveUtils.sanitize("Hello\rWorld"));
        Assert.assertEquals("Hello?World", ArchiveUtils.sanitize("Hello\0World"));
        Assert.assertEquals("Test??", ArchiveUtils.sanitize("Test\u007F\uFFF0"));
        Assert.assertEquals("", ArchiveUtils.sanitize(""));
    }
}
