package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipException;

public class ZipArchiveEntryTest {

    private static final ZipShort HEADER_ID_1 = new ZipShort(1);
    private static final ZipShort HEADER_ID_2 = new ZipShort(2);

    private static class DummyExtraField implements ZipExtraField {
        private final ZipShort headerId;
        private byte[] localData = new byte[0];
        private byte[] centralData = new byte[0];

        public DummyExtraField(ZipShort headerId) {
            this.headerId = headerId;
        }

        public DummyExtraField(ZipShort headerId, byte[] localData, byte[] centralData) {
            this.headerId = headerId;
            this.localData = localData != null ? localData : new byte[0];
            this.centralData = centralData != null ? centralData : new byte[0];
        }

        @Override
        public ZipShort getHeaderId() {
            return headerId;
        }

        @Override
        public ZipShort getLocalFileDataLength() {
            return new ZipShort(localData.length);
        }

        @Override
        public ZipShort getCentralDirectoryLength() {
            return new ZipShort(centralData.length);
        }

        @Override
        public byte[] getLocalFileDataData() {
            return localData;
        }

        @Override
        public byte[] getCentralDirectoryData() {
            return centralData;
        }

        @Override
        public void parseFromLocalFileData(byte[] buffer, int offset, int length) throws ZipException {
            localData = new byte[length];
            System.arraycopy(buffer, offset, localData, 0, length);
        }

        @Override
        public void parseFromCentralDirectoryData(byte[] buffer, int offset, int length) throws ZipException {
            centralData = new byte[length];
            System.arraycopy(buffer, offset, centralData, 0, length);
        }
    }

    @Test
    public void testDefaultConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        Assert.assertEquals("", entry.getName());
        Assert.assertFalse(entry.isDirectory());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        Assert.assertEquals(-1, entry.getMethod());
        Assert.assertEquals(ArchiveEntry.SIZE_UNKNOWN, entry.getSize());
    }

    @Test
    public void testStringConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry("foo");
        Assert.assertEquals("foo", entry.getName());
        Assert.assertFalse(entry.isDirectory());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("foo/");
        Assert.assertEquals("foo/", dirEntry.getName());
        Assert.assertTrue(dirEntry.isDirectory());

        // Platform FAT converts backslashes if no forward slash present
        ZipArchiveEntry winEntry = new ZipArchiveEntry("foo\\bar");
        Assert.assertEquals("foo/bar", winEntry.getName());
    }

    @Test
    public void testZipEntryConstructorWithoutExtra() throws Exception {
        java.util.zip.ZipEntry javaEntry = new java.util.zip.ZipEntry("entryName");
        javaEntry.setMethod(java.util.zip.ZipEntry.DEFLATED);
        javaEntry.setSize(12345L);

        ZipArchiveEntry entry = new ZipArchiveEntry(javaEntry);
        Assert.assertEquals("entryName", entry.getName());
        Assert.assertEquals(java.util.zip.ZipEntry.DEFLATED, entry.getMethod());
        Assert.assertEquals(12345L, entry.getSize());
        Assert.assertEquals(0, entry.getExtraFields().length);
    }

    @Test
    public void testZipEntryConstructorWithExtra() throws Exception {
        java.util.zip.ZipEntry javaEntry = new java.util.zip.ZipEntry("entryName");
        // Extra field with HeaderId = 0x0001, length = 0
        byte[] extra = new byte[] { 1, 0, 0, 0 };
        javaEntry.setExtra(extra);

        ZipArchiveEntry entry = new ZipArchiveEntry(javaEntry);
        Assert.assertEquals("entryName", entry.getName());
        Assert.assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        ZipArchiveEntry src = new ZipArchiveEntry("srcEntry");
        src.setInternalAttributes(2);
        src.setExternalAttributes(42L);
        src.setMethod(java.util.zip.ZipEntry.STORED);
        src.setSize(100L);
        src.addExtraField(new DummyExtraField(HEADER_ID_1));

        ZipArchiveEntry copy = new ZipArchiveEntry(src);
        Assert.assertEquals("srcEntry", copy.getName());
        Assert.assertEquals(2, copy.getInternalAttributes());
        Assert.assertEquals(42L, copy.getExternalAttributes());
        Assert.assertEquals(java.util.zip.ZipEntry.STORED, copy.getMethod());
        Assert.assertEquals(100L, copy.getSize());
        Assert.assertNotNull(copy.getExtraField(HEADER_ID_1));
    }

    @Test
    public void testFileConstructorForRegularFile() throws Exception {
        File tempFile = File.createTempFile("compress-test", ".tmp");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(new byte[] { 1, 2, 3, 4, 5 });
        fos.close();

        ZipArchiveEntry entry = new ZipArchiveEntry(tempFile, "entryFile");
        Assert.assertEquals("entryFile", entry.getName());
        Assert.assertEquals(5L, entry.getSize());
        Assert.assertEquals(tempFile.lastModified(), entry.getTime());
        Assert.assertFalse(entry.isDirectory());

        tempFile.delete();
    }

    @Test
    public void testFileConstructorForDirectory() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test-zip-dir-" + System.currentTimeMillis());
        Assert.assertTrue(tempDir.mkdir());
        tempDir.deleteOnExit();

        ZipArchiveEntry entryWithoutSlash = new ZipArchiveEntry(tempDir, "entryDir");
        Assert.assertEquals("entryDir/", entryWithoutSlash.getName());
        Assert.assertTrue(entryWithoutSlash.isDirectory());

        ZipArchiveEntry entryWithSlash = new ZipArchiveEntry(tempDir, "entryDir/");
        Assert.assertEquals("entryDir/", entryWithSlash.getName());
        Assert.assertTrue(entryWithSlash.isDirectory());

        tempDir.delete();
    }

    @Test
    public void testClone() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setInternalAttributes(10);
        entry.setExternalAttributes(20L);
        entry.addExtraField(new DummyExtraField(HEADER_ID_1));

        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        Assert.assertEquals(entry.getName(), cloned.getName());
        Assert.assertEquals(entry.getInternalAttributes(), cloned.getInternalAttributes());
        Assert.assertEquals(entry.getExternalAttributes(), cloned.getExternalAttributes());
        Assert.assertNotNull(cloned.getExtraField(HEADER_ID_1));
        Assert.assertEquals(entry, cloned);
    }

    @Test
    public void testMethodGetterSetter() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertEquals(-1, entry.getMethod());

        entry.setMethod(java.util.zip.ZipEntry.STORED);
        Assert.assertEquals(java.util.zip.ZipEntry.STORED, entry.getMethod());

        entry.setMethod(java.util.zip.ZipEntry.DEFLATED);
        Assert.assertEquals(java.util.zip.ZipEntry.DEFLATED, entry.getMethod());

        entry.setMethod(99); // custom compression method
        Assert.assertEquals(99, entry.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMethodThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(-2);
    }

    @Test
    public void testAttributesGetSet() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setInternalAttributes(5);
        Assert.assertEquals(5, entry.getInternalAttributes());

        entry.setExternalAttributes(500L);
        Assert.assertEquals(500L, entry.getExternalAttributes());
    }

    @Test
    public void testUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertEquals(0, entry.getUnixMode());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());

        entry.setUnixMode(0755);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        Assert.assertEquals(0755, entry.getUnixMode());

        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        Assert.assertEquals(0, entry.getUnixMode());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("dir/");
        dirEntry.setUnixMode(0755);
        Assert.assertEquals(0755, dirEntry.getUnixMode());
        // Verify MS-DOS directory flag (0x10) is set in external attributes
        Assert.assertTrue((dirEntry.getExternalAttributes() & 0x10) != 0);

        // Read-only permission mode (without 0200 write bit)
        ZipArchiveEntry roEntry = new ZipArchiveEntry("ro");
        roEntry.setUnixMode(0444);
        Assert.assertEquals(0444, roEntry.getUnixMode());
        Assert.assertTrue((roEntry.getExternalAttributes() & 1) != 0);
    }

    @Test
    public void testExtraFieldsManagement() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertEquals(0, entry.getExtraFields().length);
        Assert.assertEquals(0, entry.getExtraFields(true).length);
        Assert.assertEquals(0, entry.getExtraFields(false).length);

        DummyExtraField field1 = new DummyExtraField(HEADER_ID_1, new byte[]{1}, new byte[]{1, 1});
        DummyExtraField field2 = new DummyExtraField(HEADER_ID_2, new byte[]{2}, new byte[]{2, 2});
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[]{9, 9}, 0, 2);

        entry.setExtraFields(new ZipExtraField[] { field1, unparseable });
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertEquals(2, entry.getExtraFields(true).length);
        Assert.assertEquals(unparseable, entry.getUnparseableExtraFieldData());
        Assert.assertEquals(field1, entry.getExtraField(HEADER_ID_1));
        Assert.assertNull(entry.getExtraField(HEADER_ID_2));

        // Test addExtraField replacing and adding
        entry.addExtraField(field2);
        Assert.assertEquals(2, entry.getExtraFields().length);
        Assert.assertEquals(3, entry.getExtraFields(true).length);

        // Test addAsFirstExtraField
        ZipShort headerId3 = new ZipShort(3);
        DummyExtraField field3 = new DummyExtraField(headerId3);
        entry.addAsFirstExtraField(field3);
        Assert.assertEquals(field3, entry.getExtraFields()[0]);

        // Re-adding existing field as first should move it to the front
        entry.addAsFirstExtraField(field2);
        Assert.assertEquals(field2, entry.getExtraFields()[0]);

        // Add unparseable via addExtraField and addAsFirstExtraField
        UnparseableExtraFieldData unparseable2 = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable2);
        Assert.assertSame(unparseable2, entry.getUnparseableExtraFieldData());

        UnparseableExtraFieldData unparseable3 = new UnparseableExtraFieldData();
        entry.addAsFirstExtraField(unparseable3);
        Assert.assertSame(unparseable3, entry.getUnparseableExtraFieldData());

        // Remove field
        entry.removeExtraField(HEADER_ID_1);
        Assert.assertNull(entry.getExtraField(HEADER_ID_1));

        // Remove unparseable field
        entry.removeUnparseableExtraFieldData();
        Assert.assertNull(entry.getUnparseableExtraFieldData());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeExtraField(HEADER_ID_1);
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldKeyNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.addExtraField(new DummyExtraField(HEADER_ID_1));
        entry.removeExtraField(HEADER_ID_2);
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldDataNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeUnparseableExtraFieldData();
    }

    @Test
    public void testGetExtraFieldsNullExtraFieldsWithUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);

        Assert.assertEquals(0, entry.getExtraFields(false).length);
        Assert.assertEquals(1, entry.getExtraFields(true).length);
        Assert.assertSame(unparseable, entry.getExtraFields(true)[0]);
    }

    @Test
    public void testParseExtraBytes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // HeaderId 1 (2 bytes), Length 2 (2 bytes), Data (2 bytes)
        byte[] extra = new byte[] { 1, 0, 2, 0, 10, 20 };
        entry.setExtra(extra);
        Assert.assertNotNull(entry.getExtraField(HEADER_ID_1));
        Assert.assertEquals(6, entry.getLocalFileDataExtra().length);

        // Merge extra fields via setCentralDirectoryExtra
        byte[] cdExtra = new byte[] { 1, 0, 2, 0, 30, 40, 2, 0, 1, 0, 99 };
        entry.setCentralDirectoryExtra(cdExtra);
        Assert.assertNotNull(entry.getExtraField(HEADER_ID_1));
        Assert.assertNotNull(entry.getExtraField(HEADER_ID_2));

        // Merge local data when fields already exist
        byte[] localExtra2 = new byte[] { 2, 0, 1, 0, 88 };
        entry.setExtra(localExtra2);
        Assert.assertNotNull(entry.getExtraField(HEADER_ID_2));
    }

    @Test
    public void testSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(1024L);
        Assert.assertEquals(1024L, entry.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(-10L);
    }

    @Test
    public void testRawName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertNull(entry.getRawName());

        byte[] raw = new byte[] { 't', 'e', 's', 't' };
        entry.setName("test", raw);
        Assert.assertArrayEquals(raw, entry.getRawName());
        // Verify immutability of returned array
        byte[] retrieved = entry.getRawName();
        retrieved[0] = 'x';
        Assert.assertEquals((byte) 't', entry.getRawName()[0]);
    }

    @Test
    public void testSetNameFATPlatformHandling() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        entry.setName("folder\\file.txt");
        Assert.assertEquals("folder/file.txt", entry.getName());

        // Platform UNIX keeps backslashes
        ZipArchiveEntry unixEntry = new ZipArchiveEntry();
        unixEntry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        unixEntry.setName("folder\\file.txt");
        Assert.assertEquals("folder\\file.txt", unixEntry.getName());

        // Name with forward slashes already should not replace backslashes
        ZipArchiveEntry mixedEntry = new ZipArchiveEntry();
        mixedEntry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        mixedEntry.setName("folder/sub\\file.txt");
        Assert.assertEquals("folder/sub\\file.txt", mixedEntry.getName());
    }

    @Test
    public void testGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        entry.setGeneralPurposeBit(gpb);
        Assert.assertSame(gpb, entry.getGeneralPurposeBit());
    }

    @Test
    public void testHashCode() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test");
        Assert.assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    public void testGetLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        long now = 1600000000000L;
        entry.setTime(now);
        Assert.assertEquals(new Date(now), entry.getLastModifiedDate());
    }

    @Test
    public void testEqualsContract() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test");

        Assert.assertTrue(entry1.equals(entry1));
        Assert.assertFalse(entry1.equals(null));
        Assert.assertFalse(entry1.equals("test"));
        Assert.assertTrue(entry1.equals(entry2));
        Assert.assertTrue(entry2.equals(entry1));

        // Different names
        ZipArchiveEntry diffName = new ZipArchiveEntry("other");
        Assert.assertFalse(entry1.equals(diffName));

        // Different comments
        entry1.setComment("comment");
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setComment("comment");
        Assert.assertTrue(entry1.equals(entry2));

        entry2.setComment("different comment");
        Assert.assertFalse(entry1.equals(entry2));
        entry1.setComment(null);
        entry2.setComment("comment");
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setComment(null);

        // Different times
        entry1.setTime(1000L);
        entry2.setTime(2000L);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setTime(1000L);

        // Different internal attributes
        entry1.setInternalAttributes(1);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setInternalAttributes(1);

        // Different platform
        entry1.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);

        // Different external attributes
        entry1.setExternalAttributes(50L);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setExternalAttributes(50L);

        // Different method
        entry1.setMethod(java.util.zip.ZipEntry.STORED);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setMethod(java.util.zip.ZipEntry.STORED);

        // Different size
        entry1.setSize(500L);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setSize(500L);

        // Different CRC
        entry1.setCrc(12345L);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setCrc(12345L);

        // Different compressed size
        entry1.setCompressedSize(250L);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setCompressedSize(250L);

        // Different GPB
        GeneralPurposeBit gpb1 = new GeneralPurposeBit();
        gpb1.useUTF8ForNames(true);
        entry1.setGeneralPurposeBit(gpb1);
        Assert.assertFalse(entry1.equals(entry2));
        entry2.setGeneralPurposeBit(gpb1);

        // Different Extra fields
        entry1.addExtraField(new DummyExtraField(HEADER_ID_1, new byte[]{1}, new byte[]{1}));
        Assert.assertFalse(entry1.equals(entry2));
        entry2.addExtraField(new DummyExtraField(HEADER_ID_1, new byte[]{1}, new byte[]{1}));
        Assert.assertTrue(entry1.equals(entry2));
    }
}
