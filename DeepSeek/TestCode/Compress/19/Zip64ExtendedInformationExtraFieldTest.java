package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.zip.ZipException;

public class Zip64ExtendedInformationExtraFieldTest {

    private static final ZipEightByteInteger SIZE = new ZipEightByteInteger(1000L);
    private static final ZipEightByteInteger COMPRESSED_SIZE = new ZipEightByteInteger(500L);
    private static final ZipEightByteInteger OFFSET = new ZipEightByteInteger(2000L);
    private static final ZipLong DISK_START = new ZipLong(1);

    @Test
    public void testDefaultConstructor() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testTwoArgConstructor() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testFourArgConstructor() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testGetHeaderId() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(new ZipShort(0x0001), field.getHeaderId());
    }

    @Test
    public void testGetLocalFileDataLengthWhenSizeNotNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, null);
        assertEquals(2 * ZipConstants.DWORD, field.getLocalFileDataLength().getValue());
    }

    @Test
    public void testGetLocalFileDataLengthWhenSizeNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(0, field.getLocalFileDataLength().getValue());
    }

    @Test
    public void testGetCentralDirectoryLengthAllFields() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        int expected = ZipConstants.DWORD + ZipConstants.DWORD + ZipConstants.DWORD + ZipConstants.WORD;
        assertEquals(expected, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testGetCentralDirectoryLengthOnlySize() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, null, null, null);
        assertEquals(ZipConstants.DWORD, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testGetCentralDirectoryLengthOnlyCompressedSize() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(null, COMPRESSED_SIZE, null, null);
        assertEquals(ZipConstants.DWORD, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testGetCentralDirectoryLengthOnlyOffset() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(null, null, OFFSET, null);
        assertEquals(ZipConstants.DWORD, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testGetCentralDirectoryLengthOnlyDiskStart() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(null, null, null, DISK_START);
        assertEquals(ZipConstants.WORD, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testGetCentralDirectoryLengthMixed() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, null, OFFSET, null);
        assertEquals(ZipConstants.DWORD + ZipConstants.DWORD, field.getCentralDirectoryLength().getValue());
    }

    @Test
    public void testGetLocalFileDataDataBothSizesPresent() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE);
        byte[] data = field.getLocalFileDataData();
        assertNotNull(data);
        assertEquals(2 * ZipConstants.DWORD, data.length);
        // verify content by parsing back
        ZipEightByteInteger parsedSize = new ZipEightByteInteger(data, 0);
        ZipEightByteInteger parsedCompressedSize = new ZipEightByteInteger(data, ZipConstants.DWORD);
        assertEquals(SIZE, parsedSize);
        assertEquals(COMPRESSED_SIZE, parsedCompressedSize);
    }

    @Test
    public void testGetLocalFileDataDataBothNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] data = field.getLocalFileDataData();
        assertNotNull(data);
        assertEquals(0, data.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalFileDataDataSizeNullCompressedSizeNotNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(null, COMPRESSED_SIZE);
        field.getLocalFileDataData();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetLocalFileDataDataSizeNotNullCompressedSizeNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, null);
        field.getLocalFileDataData();
    }

    @Test
    public void testGetCentralDirectoryDataAllFields() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        byte[] data = field.getCentralDirectoryData();
        assertEquals(field.getCentralDirectoryLength().getValue(), data.length);
        // verify content
        ZipEightByteInteger parsedSize = new ZipEightByteInteger(data, 0);
        ZipEightByteInteger parsedCompressedSize = new ZipEightByteInteger(data, ZipConstants.DWORD);
        ZipEightByteInteger parsedOffset = new ZipEightByteInteger(data, 2 * ZipConstants.DWORD);
        ZipLong parsedDiskStart = new ZipLong(data, 3 * ZipConstants.DWORD);
        assertEquals(SIZE, parsedSize);
        assertEquals(COMPRESSED_SIZE, parsedCompressedSize);
        assertEquals(OFFSET, parsedOffset);
        assertEquals(DISK_START, parsedDiskStart);
    }

    @Test
    public void testGetCentralDirectoryDataOnlySizeAndCompressedSize() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, null, null);
        byte[] data = field.getCentralDirectoryData();
        assertEquals(2 * ZipConstants.DWORD, data.length);
        ZipEightByteInteger parsedSize = new ZipEightByteInteger(data, 0);
        ZipEightByteInteger parsedCompressedSize = new ZipEightByteInteger(data, ZipConstants.DWORD);
        assertEquals(SIZE, parsedSize);
        assertEquals(COMPRESSED_SIZE, parsedCompressedSize);
    }

    @Test
    public void testGetCentralDirectoryDataOnlyOffsetAndDiskStart() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(null, null, OFFSET, DISK_START);
        byte[] data = field.getCentralDirectoryData();
        assertEquals(ZipConstants.DWORD + ZipConstants.WORD, data.length);
        ZipEightByteInteger parsedOffset = new ZipEightByteInteger(data, 0);
        ZipLong parsedDiskStart = new ZipLong(data, ZipConstants.DWORD);
        assertEquals(OFFSET, parsedOffset);
        assertEquals(DISK_START, parsedDiskStart);
    }

    @Test
    public void testGetCentralDirectoryDataNoFields() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] data = field.getCentralDirectoryData();
        assertEquals(0, data.length);
    }

    @Test
    public void testParseFromLocalFileDataLengthZero() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(new byte[0], 0, 0);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test(expected = ZipException.class)
    public void testParseFromLocalFileDataLengthTooSmall() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(new byte[15], 0, 15);
    }

    @Test
    public void testParseFromLocalFileDataExactlyTwoDwords() throws ZipException {
        byte[] buffer = new byte[2 * ZipConstants.DWORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, buffer.length);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataWithOffset() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, buffer.length);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataWithOffsetAndDiskStart() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD + ZipConstants.WORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 3 * ZipConstants.DWORD, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, buffer.length);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromLocalFileDataWithOffsetAndDiskStartUsingOffset() throws ZipException {
        // test that offset parameter is used
        byte[] buffer = new byte[10 + 3 * ZipConstants.DWORD + ZipConstants.WORD];
        int offset = 10;
        System.arraycopy(SIZE.getBytes(), 0, buffer, offset, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, offset + ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, offset + 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, offset + 3 * ZipConstants.DWORD, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, offset, 3 * ZipConstants.DWORD + ZipConstants.WORD);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength28() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD + ZipConstants.WORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 3 * ZipConstants.DWORD, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength24() throws ZipException {
        byte[] buffer = new byte[3 * ZipConstants.DWORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength20() throws ZipException {
        // length 20: 2*DWORD + WORD, so diskStart set from end
        byte[] buffer = new byte[2 * ZipConstants.DWORD + ZipConstants.WORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength12() throws ZipException {
        // length 12: DWORD + WORD, diskStart set from end
        byte[] buffer = new byte[ZipConstants.DWORD + ZipConstants.WORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength4() throws ZipException {
        // length 4: WORD, diskStart set from end
        byte[] buffer = new byte[ZipConstants.WORD];
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 0, ZipConstants.WORD);
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLength0() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(new byte[0], 0, 0);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testParseFromCentralDirectoryDataLengthNotMatching() throws ZipException {
        // length 5: not matching any condition, should do nothing
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(new byte[5], 0, 5);
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test
    public void testReparseCentralDirectoryDataNullRawData() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.reparseCentralDirectoryData(true, true, true, true);
        // no exception, fields remain null
        assertNull(field.getSize());
    }

    @Test
    public void testReparseCentralDirectoryDataAllFlagsTrue() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        // prepare raw data via parseFromCentralDirectoryData
        byte[] buffer = new byte[3 * ZipConstants.DWORD + ZipConstants.WORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(OFFSET.getBytes(), 0, buffer, 2 * ZipConstants.DWORD, ZipConstants.DWORD);
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 3 * ZipConstants.DWORD, ZipConstants.WORD);
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        // now reparse with all true
        field.reparseCentralDirectoryData(true, true, true, true);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testReparseCentralDirectoryDataSomeFlagsTrue() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] buffer = new byte[2 * ZipConstants.DWORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        System.arraycopy(COMPRESSED_SIZE.getBytes(), 0, buffer, ZipConstants.DWORD, ZipConstants.DWORD);
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        // reparse with only size and compressedSize true
        field.reparseCentralDirectoryData(true, true, false, false);
        assertEquals(SIZE, field.getSize());
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test(expected = ZipException.class)
    public void testReparseCentralDirectoryDataLengthMismatch() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] buffer = new byte[ZipConstants.DWORD];
        System.arraycopy(SIZE.getBytes(), 0, buffer, 0, ZipConstants.DWORD);
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        // expect length DWORD but flags say we need DWORD + DWORD
        field.reparseCentralDirectoryData(true, true, false, false);
    }

    @Test
    public void testReparseCentralDirectoryDataOnlyDiskStart() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] buffer = new byte[ZipConstants.WORD];
        System.arraycopy(DISK_START.getBytes(), 0, buffer, 0, ZipConstants.WORD);
        field.parseFromCentralDirectoryData(buffer, 0, buffer.length);
        field.reparseCentralDirectoryData(false, false, false, true);
        assertEquals(DISK_START, field.getDiskStartNumber());
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
    }

    @Test
    public void testSettersAndGetters() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(SIZE);
        assertEquals(SIZE, field.getSize());
        field.setCompressedSize(COMPRESSED_SIZE);
        assertEquals(COMPRESSED_SIZE, field.getCompressedSize());
        field.setRelativeHeaderOffset(OFFSET);
        assertEquals(OFFSET, field.getRelativeHeaderOffset());
        field.setDiskStartNumber(DISK_START);
        assertEquals(DISK_START, field.getDiskStartNumber());
    }

    @Test
    public void testSetSizeNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE);
        field.setSize(null);
        assertNull(field.getSize());
    }

    @Test
    public void testSetCompressedSizeNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE);
        field.setCompressedSize(null);
        assertNull(field.getCompressedSize());
    }

    @Test
    public void testSetRelativeHeaderOffsetNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        field.setRelativeHeaderOffset(null);
        assertNull(field.getRelativeHeaderOffset());
    }

    @Test
    public void testSetDiskStartNumberNull() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField(SIZE, COMPRESSED_SIZE, OFFSET, DISK_START);
        field.setDiskStartNumber(null);
        assertNull(field.getDiskStartNumber());
    }
}
