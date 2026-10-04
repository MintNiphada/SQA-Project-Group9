package org.apache.commons.lang3;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link SystemUtils}.
 */
public class SystemUtilsTest {

    @Test
    public void testConstructor() {
        assertNotNull(new SystemUtils());
    }

    @Test
    public void testGetJavaHome() {
        File dir = SystemUtils.getJavaHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testGetJavaIoTmpDir() {
        File dir = SystemUtils.getJavaIoTmpDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testGetUserDir() {
        File dir = SystemUtils.getUserDir();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testGetUserHome() {
        File dir = SystemUtils.getUserHome();
        assertNotNull(dir);
        assertTrue(dir.exists());
    }

    @Test
    public void testIsJavaAwtHeadless() {
        boolean expected = "true".equals(System.getProperty("java.awt.headless"));
        assertEquals(expected, SystemUtils.isJavaAwtHeadless());
    }

    @Test
    public void testIsJavaVersionAtLeastFloat() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0.1f));
        assertTrue(SystemUtils.isJavaVersionAtLeast(1.1f));
        assertFalse(SystemUtils.isJavaVersionAtLeast(100.0f));
    }

    @Test
    public void testIsJavaVersionAtLeastInt() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(10));
        assertTrue(SystemUtils.isJavaVersionAtLeast(110));
        assertFalse(SystemUtils.isJavaVersionAtLeast(10000));
    }

    @Test
    public void testIsJavaVersionMatch() {
        assertFalse(SystemUtils.isJavaVersionMatch(null, "1.5"));
        assertTrue(SystemUtils.isJavaVersionMatch("1.5.0_22", "1.5"));
        assertTrue(SystemUtils.isJavaVersionMatch("1.6.0", "1.6"));
        assertFalse(SystemUtils.isJavaVersionMatch("1.4.2", "1.5"));
        assertFalse(SystemUtils.isJavaVersionMatch("1.6.0", "1.5"));
    }

    @Test
    public void testIsOSMatch() {
        assertFalse(SystemUtils.isOSMatch(null, "5.1", "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", null, "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch(null, null, "Windows", "5.1"));

        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5.1"));
        assertTrue(SystemUtils.isOSMatch("Windows 2000", "5.0", "Windows", "5.0"));
        assertTrue(SystemUtils.isOSMatch("Mac OS X", "10.6.8", "Mac", "10.6"));

        assertFalse(SystemUtils.isOSMatch("Linux", "2.6.32", "Windows", "5.1"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "6.0"));
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.1", "Linux", "5.1"));
    }

    @Test
    public void testIsOSNameMatch() {
        assertFalse(SystemUtils.isOSNameMatch(null, "Windows"));
        assertTrue(SystemUtils.isOSNameMatch("Windows XP", "Windows"));
        assertTrue(SystemUtils.isOSNameMatch("Windows 7", "Windows"));
        assertTrue(SystemUtils.isOSNameMatch("Linux", "Linux"));
        assertFalse(SystemUtils.isOSNameMatch("Linux", "Windows"));
        assertFalse(SystemUtils.isOSNameMatch("Mac OS X", "Windows"));
    }

    @Test
    public void testToJavaVersionFloat() {
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(null), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat(""), 0.00001f);
        assertEquals(0.0f, SystemUtils.toJavaVersionFloat("abc"), 0.00001f);
        assertEquals(1.0f, SystemUtils.toJavaVersionFloat("1"), 0.00001f);
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.00001f);
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("1.3.1"), 0.00001f);
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0_20"), 0.00001f);
        assertEquals(1.5f, SystemUtils.toJavaVersionFloat("1.5.0-b02"), 0.00001f);
    }

    @Test
    public void testToJavaVersionInt() {
        assertEquals(0, SystemUtils.toJavaVersionInt(null), 0.00001f);
        assertEquals(0, SystemUtils.toJavaVersionInt(""), 0.00001f);
        assertEquals(0, SystemUtils.toJavaVersionInt("abc"), 0.00001f);
        assertEquals(100, SystemUtils.toJavaVersionInt("1"), 0.00001f);
        assertEquals(120, SystemUtils.toJavaVersionInt("1.2"), 0.00001f);
        assertEquals(131, SystemUtils.toJavaVersionInt("1.3.1"), 0.00001f);
        assertEquals(160, SystemUtils.toJavaVersionInt("1.6.0_20"), 0.00001f);
        assertEquals(150, SystemUtils.toJavaVersionInt("1.5.0-b02"), 0.00001f);
    }

    @Test
    public void testToJavaVersionIntArray() {
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(null));
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(""));
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray("abc"));
        assertArrayEquals(new int[]{1}, SystemUtils.toJavaVersionIntArray("1"));
        assertArrayEquals(new int[]{1, 2}, SystemUtils.toJavaVersionIntArray("1.2"));
        assertArrayEquals(new int[]{1, 3, 1}, SystemUtils.toJavaVersionIntArray("1.3.1"));
        assertArrayEquals(new int[]{1, 5, 0, 21}, SystemUtils.toJavaVersionIntArray("1.5.0_21"));
        assertArrayEquals(new int[]{1, 6, 0, 20}, SystemUtils.toJavaVersionIntArray("1.6.0_20"));
        assertArrayEquals(new int[]{1, 7, 0, 11}, SystemUtils.toJavaVersionIntArray("Java 1.7.0_11-b21"));
    }

    @Test
    public void testEnvironmentConstantsNotNull() {
        assertNotNull(SystemUtils.FILE_SEPARATOR);
        assertNotNull(SystemUtils.JAVA_CLASS_PATH);
        assertNotNull(SystemUtils.JAVA_HOME);
        assertNotNull(SystemUtils.JAVA_IO_TMPDIR);
        assertNotNull(SystemUtils.JAVA_VENDOR);
        assertNotNull(SystemUtils.JAVA_VERSION);
        assertNotNull(SystemUtils.LINE_SEPARATOR);
        assertNotNull(SystemUtils.OS_ARCH);
        assertNotNull(SystemUtils.OS_NAME);
        assertNotNull(SystemUtils.OS_VERSION);
        assertNotNull(SystemUtils.PATH_SEPARATOR);
        assertNotNull(SystemUtils.USER_DIR);
        assertNotNull(SystemUtils.USER_HOME);
        assertNotNull(SystemUtils.USER_NAME);
        assertNotNull(SystemUtils.JAVA_VERSION_TRIMMED);
    }

    @Test
    public void testJavaVersionConstants() {
        assertTrue(SystemUtils.JAVA_VERSION_FLOAT > 0f);
        assertTrue(SystemUtils.JAVA_VERSION_INT > 0);

        int matchCount = 0;
        if (SystemUtils.IS_JAVA_1_1) matchCount++;
        if (SystemUtils.IS_JAVA_1_2) matchCount++;
        if (SystemUtils.IS_JAVA_1_3) matchCount++;
        if (SystemUtils.IS_JAVA_1_4) matchCount++;
        if (SystemUtils.IS_JAVA_1_5) matchCount++;
        if (SystemUtils.IS_JAVA_1_6) matchCount++;
        if (SystemUtils.IS_JAVA_1_7) matchCount++;

        assertTrue(matchCount <= 1);
    }

    @Test
    public void testOsConstants() {
        if (SystemUtils.IS_OS_WINDOWS) {
            assertTrue(SystemUtils.OS_NAME.startsWith("Windows"));
            assertFalse(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_LINUX) {
            assertTrue(SystemUtils.OS_NAME.startsWith("Linux") || SystemUtils.OS_NAME.startsWith("LINUX"));
            assertTrue(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_MAC_OSX) {
            assertTrue(SystemUtils.OS_NAME.startsWith("Mac OS X"));
            assertTrue(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_AIX) {
            assertTrue(SystemUtils.OS_NAME.startsWith("AIX"));
            assertTrue(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_HP_UX) {
            assertTrue(SystemUtils.OS_NAME.startsWith("HP-UX"));
            assertTrue(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_IRIX) {
            assertTrue(SystemUtils.OS_NAME.startsWith("Irix"));
            assertTrue(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_SOLARIS) {
            assertTrue(SystemUtils.OS_NAME.startsWith("Solaris"));
            assertTrue(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_SUN_OS) {
            assertTrue(SystemUtils.OS_NAME.startsWith("SunOS"));
            assertTrue(SystemUtils.IS_OS_UNIX);
        }

        if (SystemUtils.IS_OS_WINDOWS_2000) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
        if (SystemUtils.IS_OS_WINDOWS_95) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
        if (SystemUtils.IS_OS_WINDOWS_98) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
        if (SystemUtils.IS_OS_WINDOWS_ME) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
        if (SystemUtils.IS_OS_WINDOWS_NT) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
        if (SystemUtils.IS_OS_WINDOWS_XP) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
        if (SystemUtils.IS_OS_WINDOWS_VISTA) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
        if (SystemUtils.IS_OS_WINDOWS_7) {
            assertTrue(SystemUtils.IS_OS_WINDOWS);
        }
    }
}
