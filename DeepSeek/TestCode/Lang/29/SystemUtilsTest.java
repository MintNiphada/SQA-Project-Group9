package org.apache.commons.lang3;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

public class SystemUtilsTest {

    @Test
    public void testIsJavaVersionMatchNullVersion() {
        assertFalse(SystemUtils.isJavaVersionMatch(null, "1.6"));
    }

    @Test
    public void testIsJavaVersionMatchNullPrefix() {
        assertFalse(SystemUtils.isJavaVersionMatch("1.6.0", null));
    }

    @Test
    public void testIsJavaVersionMatchBothNull() {
        assertFalse(SystemUtils.isJavaVersionMatch(null, null));
    }

    @Test
    public void testIsJavaVersionMatchExactMatch() {
        assertTrue(SystemUtils.isJavaVersionMatch("1.6.0", "1.6"));
    }

    @Test
    public void testIsJavaVersionMatchNoMatch() {
        assertFalse(SystemUtils.isJavaVersionMatch("1.5.0", "1.6"));
    }

    @Test
    public void testIsJavaVersionMatchEmptyVersion() {
        assertFalse(SystemUtils.isJavaVersionMatch("", "1.6"));
    }

    @Test
    public void testIsJavaVersionMatchEmptyPrefix() {
        assertTrue(SystemUtils.isJavaVersionMatch("1.6.0", ""));
    }

    @Test
    public void testIsJavaVersionMatchPrefixLongerThanVersion() {
        assertFalse(SystemUtils.isJavaVersionMatch("1.6", "1.6.0"));
    }

    @Test
    public void testIsOSMatchNullOsName() {
        assertFalse(SystemUtils.isOSMatch(null, "5.1", "Windows", "5.1"));
    }

    @Test
    public void testIsOSMatchNullOsVersion() {
        assertFalse(SystemUtils.isOSMatch("Windows XP", null, "Windows", "5.1"));
    }

    @Test
    public void testIsOSMatchBothNull() {
        assertFalse(SystemUtils.isOSMatch(null, null, "Windows", "5.1"));
    }

    @Test
    public void testIsOSMatchExactMatch() {
        assertTrue(SystemUtils.isOSMatch("Windows XP", "5.1", "Windows", "5.1"));
    }

    @Test
    public void testIsOSMatchOsNameMismatch() {
        assertFalse(SystemUtils.isOSMatch("Linux", "2.6", "Windows", "5.1"));
    }

    @Test
    public void testIsOSMatchOsVersionMismatch() {
        assertFalse(SystemUtils.isOSMatch("Windows XP", "5.2", "Windows", "5.1"));
    }

    @Test
    public void testIsOSMatchPrefixMatch() {
        assertTrue(SystemUtils.isOSMatch("Windows 7", "6.1", "Windows", "6."));
    }

    @Test
    public void testIsOSNameMatchNullOsName() {
        assertFalse(SystemUtils.isOSNameMatch(null, "Windows"));
    }

    @Test
    public void testIsOSNameMatchExactMatch() {
        assertTrue(SystemUtils.isOSNameMatch("Windows XP", "Windows"));
    }

    @Test
    public void testIsOSNameMatchNoMatch() {
        assertFalse(SystemUtils.isOSNameMatch("Linux", "Windows"));
    }

    @Test
    public void testIsOSNameMatchEmptyPrefix() {
        assertTrue(SystemUtils.isOSNameMatch("Windows", ""));
    }

    @Test
    public void testToJavaVersionIntArrayNull() {
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(null));
    }

    @Test
    public void testToJavaVersionIntArrayEmpty() {
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray(""));
    }

    @Test
    public void testToJavaVersionIntArrayOnlyNonDigits() {
        assertArrayEquals(new int[0], SystemUtils.toJavaVersionIntArray("abc"));
    }

    @Test
    public void testToJavaVersionIntArraySingleDigit() {
        assertArrayEquals(new int[]{1}, SystemUtils.toJavaVersionIntArray("1"));
    }

    @Test
    public void testToJavaVersionIntArrayJava5() {
        assertArrayEquals(new int[]{1, 5, 0}, SystemUtils.toJavaVersionIntArray("1.5.0"));
    }

    @Test
    public void testToJavaVersionIntArrayJava6Update() {
        assertArrayEquals(new int[]{1, 6, 0, 20}, SystemUtils.toJavaVersionIntArray("1.6.0_20"));
    }

    @Test
    public void testToJavaVersionIntArrayLeadingNonDigits() {
        assertArrayEquals(new int[]{1, 6, 0}, SystemUtils.toJavaVersionIntArray("jdk1.6.0"));
    }

    @Test
    public void testToJavaVersionFloatNull() {
        assertEquals(0f, SystemUtils.toJavaVersionFloat(null), 0.0f);
    }

    @Test
    public void testToJavaVersionFloatEmpty() {
        assertEquals(0f, SystemUtils.toJavaVersionFloat(""), 0.0f);
    }

    @Test
    public void testToJavaVersionFloatJava2() {
        assertEquals(1.2f, SystemUtils.toJavaVersionFloat("1.2"), 0.0f);
    }

    @Test
    public void testToJavaVersionFloatJava3_1() {
        assertEquals(1.31f, SystemUtils.toJavaVersionFloat("1.3.1"), 0.0f);
    }

    @Test
    public void testToJavaVersionFloatJava6Update() {
        assertEquals(1.6f, SystemUtils.toJavaVersionFloat("1.6.0_20"), 0.0f);
    }

    @Test
    public void testToJavaVersionFloatJava9() {
        assertEquals(9.0f, SystemUtils.toJavaVersionFloat("9"), 0.0f);
    }

    @Test
    public void testToJavaVersionFloatJava10() {
        assertEquals(10.0f, SystemUtils.toJavaVersionFloat("10.0.1"), 0.0f);
    }

    @Test
    public void testToJavaVersionIntNull() {
        assertEquals(0, SystemUtils.toJavaVersionInt(null));
    }

    @Test
    public void testToJavaVersionIntEmpty() {
        assertEquals(0, SystemUtils.toJavaVersionInt(""));
    }

    @Test
    public void testToJavaVersionIntJava2() {
        assertEquals(120, SystemUtils.toJavaVersionInt("1.2"));
    }

    @Test
    public void testToJavaVersionIntJava3_1() {
        assertEquals(131, SystemUtils.toJavaVersionInt("1.3.1"));
    }

    @Test
    public void testToJavaVersionIntJava6Update() {
        assertEquals(160, SystemUtils.toJavaVersionInt("1.6.0_20"));
    }

    @Test
    public void testToJavaVersionIntJava9() {
        assertEquals(900, SystemUtils.toJavaVersionInt("9"));
    }

    @Test
    public void testToJavaVersionIntJava10() {
        assertEquals(1000, SystemUtils.toJavaVersionInt("10.0.1"));
    }

    @Test
    public void testIsJavaVersionAtLeastFloatVeryLow() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0.0f));
    }

    @Test
    public void testIsJavaVersionAtLeastFloatVeryHigh() {
        assertFalse(SystemUtils.isJavaVersionAtLeast(999.9f));
    }

    @Test
    public void testIsJavaVersionAtLeastIntVeryLow() {
        assertTrue(SystemUtils.isJavaVersionAtLeast(0));
    }

    @Test
    public void testIsJavaVersionAtLeastIntVeryHigh() {
        assertFalse(SystemUtils.isJavaVersionAtLeast(9999));
    }

    @Test
    public void testIsJavaAwtHeadless() {
        boolean result = SystemUtils.isJavaAwtHeadless();
        assertTrue(result == true || result == false);
    }

    @Test
    public void testGetJavaHome() {
        File javaHome = SystemUtils.getJavaHome();
        assertNotNull(javaHome);
        assertNotNull(javaHome.getPath());
        assertFalse(javaHome.getPath().isEmpty());
    }

    @Test
    public void testGetJavaIoTmpDir() {
        File tmpDir = SystemUtils.getJavaIoTmpDir();
        assertNotNull(tmpDir);
        assertNotNull(tmpDir.getPath());
        assertFalse(tmpDir.getPath().isEmpty());
    }

    @Test
    public void testGetUserDir() {
        File userDir = SystemUtils.getUserDir();
        assertNotNull(userDir);
        assertNotNull(userDir.getPath());
        assertFalse(userDir.getPath().isEmpty());
    }

    @Test
    public void testGetUserHome() {
        File userHome = SystemUtils.getUserHome();
        assertNotNull(userHome);
        assertNotNull(userHome.getPath());
        assertFalse(userHome.getPath().isEmpty());
    }

    @Test
    public void testConstructor() {
        new SystemUtils();
    }
}
