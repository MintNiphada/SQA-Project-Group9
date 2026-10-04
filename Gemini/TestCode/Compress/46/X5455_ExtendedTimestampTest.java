package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.zip.ZipException;

public class X5455_ExtendedTimestampTest {

    @Test
    public void testDefaultsAndConstants() {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        Assert.assertEquals(new ZipShort(0x5455), xf.getHeaderId());
        Assert.assertEquals(0, xf.getFlags());
        Assert.assertFalse(xf.isBit0_modifyTimePresent());
        Assert.assertFalse(xf.isBit1_accessTimePresent());
        Assert.assertFalse(xf.isBit2_createTimePresent());
        Assert.assertNull(xf.getModifyTime());
        Assert.assertNull(xf.getAccessTime());
        Assert.assertNull(xf.getCreateTime());
        Assert.assertNull(xf.getModifyJavaTime());
        Assert.assertNull(xf.getAccessJavaTime());
        Assert.assertNull(xf.getCreateJavaTime());
        Assert.assertEquals(1, xf.getLocalFileDataLength().getValue());
        Assert.assertEquals(1, xf.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testSetFlags() {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.setFlags((byte) 7);
        Assert.assertEquals((byte) 7, xf.getFlags());
        Assert.assertTrue(xf.isBit0_modifyTimePresent());
        Assert.assertTrue(xf.isBit1_accessTimePresent());
        Assert.assertTrue(xf.isBit2_createTimePresent());

        xf.setFlags((byte) 0);
        Assert.assertEquals((byte) 0, xf.getFlags());
        Assert.assertFalse(xf.isBit0_modifyTimePresent());
        Assert.assertFalse(xf.isBit1_accessTimePresent());
        Assert.assertFalse(xf.isBit2_createTimePresent());
    }

    @Test
    public void testSetAndGetModifyTime() {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        ZipLong time = new ZipLong(1000L);
        xf.setModifyTime(time);
        Assert.assertEquals(time, xf.getModifyTime());
        Assert.assertEquals(new Date(1000000L), xf.getModifyJavaTime());
        Assert.assertTrue(xf.isBit0_modifyTimePresent());
        Assert.assertEquals(X5455_ExtendedTimestamp.MODIFY_TIME_BIT, xf.getFlags() & X5455_ExtendedTimestamp.MODIFY_TIME_BIT);

        xf.setModifyTime(null);
        Assert.assertNull(xf.getModifyTime());
        Assert.assertNull(xf.getModifyJavaTime());
        Assert.assertFalse(xf.isBit0_modifyTimePresent());

        xf.setModifyJavaTime(new Date(2000000L));
        Assert.assertEquals(new ZipLong(2000L), xf.getModifyTime());
        Assert.assertEquals(new Date(2000000L), xf.getModifyJavaTime());

        xf.setModifyJavaTime(null);
        Assert.assertNull(xf.getModifyTime());
    }

    @Test
    public void testSetAndGetAccessTime() {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        ZipLong time = new ZipLong(1000L);
        xf.setAccessTime(time);
        Assert.assertEquals(time, xf.getAccessTime());
        Assert.assertEquals(new Date(1000000L), xf.getAccessJavaTime());
        Assert.assertTrue(xf.isBit1_accessTimePresent());
        Assert.assertEquals(X5455_ExtendedTimestamp.ACCESS_TIME_BIT, xf.getFlags() & X5455_ExtendedTimestamp.ACCESS_TIME_BIT);

        xf.setAccessTime(null);
        Assert.assertNull(xf.getAccessTime());
        Assert.assertNull(xf.getAccessJavaTime());
        Assert.assertFalse(xf.isBit1_accessTimePresent());

        xf.setAccessJavaTime(new Date(2000000L));
        Assert.assertEquals(new ZipLong(2000L), xf.getAccessTime());
        Assert.assertEquals(new Date(2000000L), xf.getAccessJavaTime());

        xf.setAccessJavaTime(null);
        Assert.assertNull(xf.getAccessTime());
    }

    @Test
    public void testSetAndGetCreateTime() {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        ZipLong time = new ZipLong(1000L);
        xf.setCreateTime(time);
        Assert.assertEquals(time, xf.getCreateTime());
        Assert.assertEquals(new Date(1000000L), xf.getCreateJavaTime());
        Assert.assertTrue(xf.isBit2_createTimePresent());
        Assert.assertEquals(X5455_ExtendedTimestamp.CREATE_TIME_BIT, xf.getFlags() & X5455_ExtendedTimestamp.CREATE_TIME_BIT);

        xf.setCreateTime(null);
        Assert.assertNull(xf.getCreateTime());
        Assert.assertNull(xf.getCreateJavaTime());
        Assert.assertFalse(xf.isBit2_createTimePresent());

        xf.setCreateJavaTime(new Date(2000000L));
        Assert.assertEquals(new ZipLong(2000L), xf.getCreateTime());
        Assert.assertEquals(new Date(2000000L), xf.getCreateJavaTime());

        xf.setCreateJavaTime(null);
        Assert.assertNull(xf.getCreateTime());
    }

    @Test
    public void testGetLengthsAndData() {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.setModifyJavaTime(new Date(1000000L));
        xf.setAccessJavaTime(new Date(2000000L));
        xf.setCreateJavaTime(new Date(3000000L));

        Assert.assertEquals(13, xf.getLocalFileDataLength().getValue());
        Assert.assertEquals(5, xf.getCentralDirectoryLength().getValue());

        byte[] local = xf.getLocalFileDataData();
        Assert.assertEquals(13, local.length);
        Assert.assertEquals(7, local[0]);

        byte[] central = xf.getCentralDirectoryData();
        Assert.assertEquals(5, central.length);
        Assert.assertEquals(7, central[0]);

        xf.setModifyTime(null);
        Assert.assertEquals(9, xf.getLocalFileDataLength().getValue());
        Assert.assertEquals(1, xf.getCentralDirectoryLength().getValue());
        Assert.assertEquals(6, xf.getLocalFileDataData()[0]);
        Assert.assertEquals(6, xf.getCentralDirectoryData()[0]);
    }

    @Test
    public void testParseLocalDataAllPresent() throws ZipException {
        byte[] data = new byte[]{
                7,
                (byte) 0xE8, 0x03, 0x00, 0x00,
                (byte) 0xD0, 0x07, 0x00, 0x00,
                (byte) 0xB8, 0x0B, 0x00, 0x00
        };
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromLocalFileData(data, 0, data.length);

        Assert.assertEquals((byte) 7, xf.getFlags());
        Assert.assertEquals(new ZipLong(1000L), xf.getModifyTime());
        Assert.assertEquals(new ZipLong(2000L), xf.getAccessTime());
        Assert.assertEquals(new ZipLong(3000L), xf.getCreateTime());
    }

    @Test
    public void testParseCentralDirectoryData() throws ZipException {
        byte[] data = new byte[]{
                7,
                (byte) 0xE8, 0x03, 0x00, 0x00
        };
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromCentralDirectoryData(data, 0, data.length);

        Assert.assertEquals((byte) 7, xf.getFlags());
        Assert.assertEquals(new ZipLong(1000L), xf.getModifyTime());
        Assert.assertNull(xf.getAccessTime());
        Assert.assertNull(xf.getCreateTime());
    }

    @Test
    public void testParseLocalDataPartial() throws ZipException {
        byte[] data = new byte[]{
                6,
                (byte) 0xD0, 0x07, 0x00, 0x00,
                (byte) 0xB8, 0x0B, 0x00, 0x00
        };
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromLocalFileData(data, 0, data.length);

        Assert.assertEquals((byte) 6, xf.getFlags());
        Assert.assertNull(xf.getModifyTime());
        Assert.assertEquals(new ZipLong(2000L), xf.getAccessTime());
        Assert.assertEquals(new ZipLong(3000L), xf.getCreateTime());
    }

    @Test
    public void testDateToZipLongOverflow() {
        Date tooLarge = new Date(0x100000000L * 1000L);
        try {
            new X5455_ExtendedTimestamp().setModifyJavaTime(tooLarge);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("must fit in a signed 32 bit integer"));
        }
    }

    @Test
    public void testToString() {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        String strEmpty = xf.toString();
        Assert.assertTrue(strEmpty.startsWith("0x5455 Zip Extra Field: Flags=0"));

        xf.setModifyJavaTime(new Date(1000000L));
        xf.setAccessJavaTime(new Date(2000000L));
        xf.setCreateJavaTime(new Date(3000000L));
        String strFull = xf.toString();
        Assert.assertTrue(strFull.contains("Modify:["));
        Assert.assertTrue(strFull.contains("Access:["));
        Assert.assertTrue(strFull.contains("Create:["));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.setModifyJavaTime(new Date(1000000L));
        xf.setAccessJavaTime(new Date(2000000L));
        xf.setCreateJavaTime(new Date(3000000L));

        X5455_ExtendedTimestamp cloned = (X5455_ExtendedTimestamp) xf.clone();
        Assert.assertNotSame(xf, cloned);
        Assert.assertEquals(xf, cloned);
        Assert.assertEquals(xf.getFlags(), cloned.getFlags());
        Assert.assertEquals(xf.getModifyTime(), cloned.getModifyTime());
        Assert.assertEquals(xf.getAccessTime(), cloned.getAccessTime());
        Assert.assertEquals(xf.getCreateTime(), cloned.getCreateTime());
    }

    @Test
    public void testEqualsAndHashCode() {
        X5455_ExtendedTimestamp xf1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xf2 = new X5455_ExtendedTimestamp();

        Assert.assertEquals(xf1, xf1);
        Assert.assertNotEquals(xf1, null);
        Assert.assertNotEquals(xf1, "some_string");
        Assert.assertEquals(xf1, xf2);
        Assert.assertEquals(xf1.hashCode(), xf2.hashCode());

        xf1.setFlags((byte) 0xF8);
        xf2.setFlags((byte) 0x00);
        Assert.assertEquals(xf1, xf2);
        Assert.assertEquals(xf1.hashCode(), xf2.hashCode());

        xf1.setModifyJavaTime(new Date(1000000L));
        Assert.assertNotEquals(xf1, xf2);
        Assert.assertNotEquals(xf1.hashCode(), xf2.hashCode());
        xf2.setModifyJavaTime(new Date(1000000L));
        Assert.assertEquals(xf1, xf2);

        xf1.setAccessJavaTime(new Date(2000000L));
        Assert.assertNotEquals(xf1, xf2);
        xf2.setAccessJavaTime(new Date(2000000L));
        Assert.assertEquals(xf1, xf2);

        xf1.setCreateJavaTime(new Date(3000000L));
        Assert.assertNotEquals(xf1, xf2);
        xf2.setCreateJavaTime(new Date(3000000L));
        Assert.assertEquals(xf1, xf2);
        Assert.assertEquals(xf1.hashCode(), xf2.hashCode());
    }
}
