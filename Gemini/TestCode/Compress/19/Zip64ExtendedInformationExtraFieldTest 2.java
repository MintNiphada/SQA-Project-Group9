package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.zip.ZipException;

public class Zip64ExtendedInformationExtraFieldTest {

    private static final ZipEightByteInteger SIZE = new ZipEightByteInteger(1000L);
    private static final ZipEightByteInteger COMPRESSED_SIZE = new ZipEightByteInteger(500L);
    private static final ZipEightByteInteger OFFSET = new ZipEightByteInteger(200L);
    private static final ZipLong DISK_START = new ZipLong(1L);

    @Test
    public void testDefaultConstructor() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        Assert.assertEquals(Zip64ExtendedInformationExtraField.HEADER_ID, field.getHeaderId());
        Assert.assertNull(field.getSize());
        Assert.assertNull(field.getCompressedSize());
        Assert.assertNull(field.getRelativeHeaderOffset());
        Assert.assertNull(field.getDiskStartNumber());
        Assert.assertEquals(new ZipShort(0), field.getLocalFileDataLength());
        Assert.assertEquals(new ZipShort(0), field.getCentralDirectoryLength());
        Assert.assertArrayEquals(new byte[0], field.getLocalFileDataData());
        Assert.assertArrayEquals(new byte[0], field.getCentralDirectoryData());
    }

    @Test
    public void testTwoArgConstructor() {
        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE);
        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertNull(field.getRelativeHeaderOffset());
        Assert.assertNull(field.getDiskStartNumber());
        Assert.assertEquals(new ZipShort(16), field.getLocalFileDataLength());
        Assert.assertEquals(new ZipShort(16), field.getCentralDirectoryLength());
    }

    @Test
    public void testFourArgConstructor() {
        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertEquals(DISK_START, field.getDiskStartNumber());
        Assert.assertEquals(new ZipShort(16), field.getLocalFileDataLength());
        Assert.assertEquals(new ZipShort(28), field.getCentralDirectoryLength());
    }

    @Test
    public void testSettersAndGetters() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(SIZE);
        field.setCompressedSize(COMPRESSED_SIZE);
        field.setRelativeHeaderOffset(OFFSET);
        field.setDiskStartNumber(DISK_START);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testGetCentralDirectoryLengthVariations() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        Assert.assertEquals(0, field.getCentralDirectoryLength().getValue());

        field.setSize(SIZE);
        Assert.assertEquals(8, field.getCentralDirectoryLength().getValue());

        field.setCompressedSize(COMPRESSED_SIZE);
        Assert.assertEquals(16, field.getCentralDirectoryLength().getValue());

        field.setRelativeHeaderOffset(OFFSET);
        Assert.assertEquals(24, field.getCentralDirectoryLength().getValue());

        field.setDiskStartNumber(DISK_START);
        Assert.assertEquals(28, field.getCentralDirectoryLength().getValue());

        // Test with only disk start
        field = new Zip64ExtendedInformationExtraField();
        field.setDiskStartNumber(DISK_START);
        Assert.assertEquals(4, field.getCentralDirectoryLength().getValue());

        // Test with only offset
        field = new Zip64ExtendedInformationExtraField();
        field.setRelativeHeaderOffset(OFFSET);
        Assert.assertEquals(8, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testGetLocalFileDataDataSuccess() {
        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE);
        byte[] data = field.getLocalFileDataData();
        Assert.assertEquals(16, data.length);

        byte[] expected = new byte[16];
        System.arraycopy(SIZE.getBytes(), 0, expected, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, expected, 8, 8);
        Assert.assertArrayEquals(expected, data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalFileDataDataMissingCompressedSize() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(SIZE);
        field.getLocalFileDataData();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalFileDataDataMissingSize() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setCompressedSize(COMPRESSED_SIZE);
        field.getLocalFileDataData();
    }

    @Test
    public void testGetCentralDirectoryDataAllFields() {
        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        byte[] data = field.getCentralDirectoryData();
        Assert.assertEquals(28, data.length);

        byte[] expected = new byte[28];
        System.arraycopy(SIZE.getBytes(), 0, expected, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, expected, 8, 8);
        System.arraycopy(OFFSET.getBytes(), 0, expected, 16, 8);
        System.arraycopy(DISK_START.getBytes(), 0, expected, 24, 4);
        Assert.assertArrayEquals(expected, data);
    }

    @Test
    public void testGetCentralDirectoryDataPartialFields() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(SIZE);
        field.setRelativeHeaderOffset(OFFSET);
        byte[] data = field.getCentralDirectoryData();
        Assert.assertEquals(16, data.length);

        byte[] expected = new byte[16];
        System.arraycopy(SIZE.getBytes(), 0, expected, 0, 8);
        System.arraycopy(OFFSET.getBytes(), 0, expected, 8, 8);
        Assert.assertArrayEquals(expected, data);

        field = new Zip64ExtendedInformationExtraField();
        field.setDiskStartNumber(DISK_START);
        data = field.getCentralDirectoryData();
        Assert.assertEquals(4, data.length);
        Assert.assertArrayEquals(DISK_START.getBytes(), data);
    }

    @Test
    public void testParseFromLocalFileDataEmpty() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(new byte[10], 0, 0);
        Assert.assertNull(field.getSize());
        Assert.assertNull(field.getCompressedSize());
    }

    @Test(expected = ZipException.class)
    public void testParseFromLocalFileDataTooShort() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(new byte[15], 0, 15);
    }

    @Test
    public void testParseFromLocalFileDataTwoSizesOnly() throws Exception {
        byte[] buffer = new byte[20];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 2, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 10, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 2, 16);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertNull(field.getRelativeHeaderOffset());
        Assert.assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataWithOffset() throws Exception {
        byte[] buffer = new byte[24];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 16, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, 24);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataWithOffsetAndDiskStart() throws Exception {
        byte[] buffer = new byte[28];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 16, 8);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, 28);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataWithDiskStartOnlyAfterSizes() throws Exception {
        // 16 bytes for sizes + 4 bytes for disk start (no offset)
        byte[] buffer = new byte[20];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 16, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, 20);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertNull(field.getRelativeHeaderOffset());
        Assert.assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataAllDataPresent() throws Exception {
        byte[] buffer = new byte[28];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 16, 8);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 28);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength24() throws Exception {
        byte[] buffer = new byte[24];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 16, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 24);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLengthMod8Equals4() throws Exception {
        // Test length 4 (only disk start)
        byte[] buffer4 = new byte[10];
        System.arraycopy(DISK_START.getBytes(), 0, buffer4, 2, 4);

        Zip64ExtendedInformationExtraField field4 = new Zip64ExtendedInformationExtraField();
        field4.parseFromCentralDirectoryData(buffer4, 2, 4);
        Assert.assertEquals(DISK_START, field4.getDiskStartNumber());
        Assert.assertNull(field4.getSize());
        Assert.assertNull(field4.getCompressedSize());
        Assert.assertNull(field4.getRelativeHeaderOffset());

        // Test length 12 (length % 8 == 4)
        byte[] buffer12 = new byte[12];
        System.arraycopy(DISK_START.getBytes(), 0, buffer12, 8, 4);

        Zip64ExtendedInformationExtraField field12 = new Zip64ExtendedInformationExtraField();
        field12.parseFromCentralDirectoryData(buffer12, 0, 12);
        Assert.assertEquals(DISK_START, field12.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataUnhandledLength() throws Exception {
        byte[] buffer = new byte[16];
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 16);

        // Neither parsed since length 16 is not >= 28, not == 24, and 16 % 8 != 4
        Assert.assertNull(field.getSize());
        Assert.assertNull(field.getCompressedSize());
        Assert.assertNull(field.getRelativeHeaderOffset());
        Assert.assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testReparseCentralDirectoryDataNullRaw() throws Exception {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        // Should not throw or do anything
        field.reparseCentralDirectoryData(true, true, true, true);
        Assert.assertNull(field.getSize());
    }

    @Test
    public void testReparseCentralDirectoryDataAllFields() throws Exception {
        byte[] buffer = new byte[28];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 16, 8);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 28);

        // Reset fields to ensure reparse sets them
        field.setSize(null);
        field.setCompressedSize(null);
        field.setRelativeHeaderOffset(null);
        field.setDiskStartNumber(null);

        field.reparseCentralDirectoryData(true, true, true, true);
        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testReparseCentralDirectoryDataCombinations() throws Exception {
        // Test size + diskStart (8 + 4 = 12 bytes)
        byte[] buffer = new byte[12];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 8, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 12);
        field.reparseCentralDirectoryData(true, false, false, true);

        Assert.assertEquals(SIZE, field.getSize());
        Assert.assertNull(field.getCompressedSize());
        Assert.assertNull(field.getRelativeHeaderOffset());
        Assert.assertEquals(DISK_START, field.getDiskStartNumber());

        // Test compressedSize + offset (8 + 8 = 16 bytes)
        buffer = new byte[16];
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 8, 8);

        field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 16);
        field.reparseCentralDirectoryData(false, true, true, false);

        Assert.assertNull(field.getSize());
        Assert.assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        Assert.assertEquals(OFFSET, field.getRelativeHeaderOffset());
        Assert.assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testReparseCentralDirectoryDataLengthMismatchThrows() {
        byte[] buffer = new byte[16];
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        try {
            field.parseFromCentralDirectoryData(buffer, 0, 16);
            field.reparseCentralDirectoryData(true, true, true, true); // expects 28
            Assert.fail("Expected ZipException on length mismatch");
        } catch (ZipException e) {
            Assert.assertTrue(e.getMessage().contains("doesn't match central directory data"));
        }
    }

    @Test
    public void testRoundTripLocalFileData() throws Exception {
        Zip64ExtendedInformationExtraField original =
            new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE);
        byte[] localData = original.getLocalFileDataData();

        Zip64ExtendedInformationExtraField parsed = new Zip64ExtendedInformationExtraField();
        parsed.parseFromLocalFileData(localData, 0, localData.length);

        Assert.assertEquals(original.getSize(), parsed.getSize());
        Assert.assertEquals(original.getCompressedSize(), parsed.getCompressedSize());
    }

    @Test
    public void testRoundTripCentralDirectoryData() throws Exception {
        Zip64ExtendedInformationExtraField original =
            new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        byte[] cdData = original.getCentralDirectoryData();

        Zip64ExtendedInformationExtraField parsed = new Zip64ExtendedInformationExtraField();
        parsed.parseFromCentralDirectoryData(cdData, 0, cdData.length);

        Assert.assertEquals(original.getSize(), parsed.getSize());
        Assert.assertEquals(original.getCompressedSize(), parsed.getCompressedSize());
        Assert.assertEquals(original.getRelativeHeaderOffset(), parsed.getRelativeHeaderOffset());
        Assert.assertEquals(original.getDiskStartNumber(), parsed.getDiskStartNumber());
    }
}
