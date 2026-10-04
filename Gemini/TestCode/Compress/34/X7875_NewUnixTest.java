package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.zip.ZipException;

public class X7875_NewUnixTest {

    @Test
    public void testHeaderId() {
        X7875_NewUnix xf = new X7875_NewUnix();
        Assert.assertEquals(new ZipShort(0x7875), xf.getHeaderId());
    }

    @Test
    public void testDefaultValues() {
        X7875_NewUnix xf = new X7875_NewUnix();
        Assert.assertEquals(1000L, xf.getUID());
        Assert.assertEquals(1000L, xf.getGID());
        Assert.assertEquals(new ZipShort(7), xf.getLocalFileDataLength());
        Assert.assertEquals(new ZipShort(7), xf.getCentralDirectoryLength());
        Assert.assertArrayEquals(new byte[0], xf.getCentralDirectoryData());
    }

    @Test
    public void testGetAndSetUidGid() {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(0);
        xf.setGID(0);
        Assert.assertEquals(0L, xf.getUID());
        Assert.assertEquals(0L, xf.getGID());
        Assert.assertEquals(new ZipShort(5), xf.getLocalFileDataLength());

        xf.setUID(0xFFFFFFFFL);
        xf.setGID(0x12345678L);
        Assert.assertEquals(0xFFFFFFFFL, xf.getUID());
        Assert.assertEquals(0x12345678L, xf.getGID());
    }

    @Test
    public void testGetLocalFileDataData() {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(0);
        xf.setGID(0);
        byte[] expected = new byte[]{1, 1, 0, 1, 0};
        Assert.assertArrayEquals(expected, xf.getLocalFileDataData());

        xf.setUID(1000);
        xf.setGID(1000);
        byte[] expected1000 = new byte[]{1, 2, (byte) 0xE8, 0x03, 2, (byte) 0xE8, 0x03};
        Assert.assertArrayEquals(expected1000, xf.getLocalFileDataData());
    }

    @Test
    public void testParseFromLocalFileData() throws ZipException {
        X7875_NewUnix xf = new X7875_NewUnix();
        byte[] data = new byte[]{0x00, 1, 2, (byte) 0xE8, 0x03, 2, (byte) 0xE8, 0x03, 0x00};
        xf.parseFromLocalFileData(data, 1, 7);
        Assert.assertEquals(1000L, xf.getUID());
        Assert.assertEquals(1000L, xf.getGID());
    }

    @Test
    public void testParseFromCentralDirectoryData() throws ZipException {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.parseFromCentralDirectoryData(new byte[5], 0, 5);
        Assert.assertEquals(1000L, xf.getUID());
        Assert.assertEquals(1000L, xf.getGID());
    }

    @Test
    public void testToString() {
        X7875_NewUnix xf = new X7875_NewUnix();
        Assert.assertEquals("0x7875 Zip Extra Field: UID=1000 GID=1000", xf.toString());
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        X7875_NewUnix xf = new X7875_NewUnix();
        xf.setUID(12345);
        xf.setGID(67890);
        X7875_NewUnix clone = (X7875_NewUnix) xf.clone();
        Assert.assertNotSame(xf, clone);
        Assert.assertEquals(xf, clone);
        Assert.assertEquals(xf.getUID(), clone.getUID());
        Assert.assertEquals(xf.getGID(), clone.getGID());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        X7875_NewUnix xf1 = new X7875_NewUnix();
        X7875_NewUnix xf2 = new X7875_NewUnix();

        Assert.assertTrue(xf1.equals(xf1));
        Assert.assertFalse(xf1.equals(null));
        Assert.assertFalse(xf1.equals("String"));
        Assert.assertTrue(xf1.equals(xf2));
        Assert.assertEquals(xf1.hashCode(), xf2.hashCode());

        xf2.setUID(500);
        Assert.assertFalse(xf1.equals(xf2));
        Assert.assertNotEquals(xf1.hashCode(), xf2.hashCode());

        xf2.setUID(1000);
        xf2.setGID(500);
        Assert.assertFalse(xf1.equals(xf2));
        Assert.assertNotEquals(xf1.hashCode(), xf2.hashCode());

        byte[] rawVersion2 = new byte[]{2, 1, 0, 1, 0};
        X7875_NewUnix xf3 = new X7875_NewUnix();
        xf3.parseFromLocalFileData(rawVersion2, 0, 5);
        X7875_NewUnix xf4 = new X7875_NewUnix();
        xf4.setUID(0);
        xf4.setGID(0);
        Assert.assertFalse(xf3.equals(xf4));
        Assert.assertNotEquals(xf3.hashCode(), xf4.hashCode());
    }

    @Test
    public void testTrimLeadingZeroesForceMinLength() {
        Assert.assertNull(X7875_NewUnix.trimLeadingZeroesForceMinLength(null));

        byte[] empty = new byte[0];
        Assert.assertArrayEquals(new byte[]{0}, X7875_NewUnix.trimLeadingZeroesForceMinLength(empty));

        byte[] allZeroes = new byte[]{0, 0, 0};
        Assert.assertArrayEquals(new byte[]{0}, X7875_NewUnix.trimLeadingZeroesForceMinLength(allZeroes));

        byte[] leadingZeroes = new byte[]{0, 0, 1, 2};
        Assert.assertArrayEquals(new byte[]{1, 2}, X7875_NewUnix.trimLeadingZeroesForceMinLength(leadingZeroes));

        byte[] noLeadingZeroes = new byte[]{1, 2, 3};
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, X7875_NewUnix.trimLeadingZeroesForceMinLength(noLeadingZeroes));
    }
}
