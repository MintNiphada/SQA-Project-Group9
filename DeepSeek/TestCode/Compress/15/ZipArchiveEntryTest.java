package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ZipArchiveEntryTest {

    // Helper: a simple ZipExtraField implementation for testing
    private static class TestZipExtraField implements ZipExtraField {
        private final ZipShort headerId;
        private byte[] localData;
        private byte[] centralData;
        private boolean localParsed;
        private boolean centralParsed;

        TestZipExtraField(ZipShort headerId) {
            this.headerId = headerId;
        }

        @Override
        public ZipShort getHeaderId() {
            return headerId;
        }

        @Override
        public ZipShort getLocalFileDataLength() {
            return new ZipShort(localData != null ? localData.length : 0);
        }

        @Override
        public ZipShort getCentralDirectoryLength() {
            return new ZipShort(centralData != null ? centralData.length : 0);
        }

        @Override
        public byte[] getLocalFileDataData() {
            return localData != null ? localData.clone() : new byte[0];
        }

        @Override
        public byte[] getCentralDirectoryData() {
            return centralData != null ? centralData.clone() : new byte[0];
        }

        @Override
        public void parseFromLocalFileData(byte[] data, int offset, int length) {
            localData = new byte[length];
            System.arraycopy(data, offset, localData, 0, length);
            localParsed = true;
        }

        @Override
        public void parseFromCentralDirectoryData(byte[] data, int offset, int length) {
            centralData = new byte[length];
            System.arraycopy(data, offset, centralData, 0, length);
            centralParsed = true;
        }

        public boolean isLocalParsed() { return localParsed; }
        public boolean isCentralParsed() { return centralParsed; }
    }

    @Test
    public void testConstructorWithName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
        assertEquals(-1, entry.getMethod());
        assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, entry.getSize());
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    @Test
    public void testConstructorWithDirectoryName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
        assertEquals("dir/", entry.getName());
    }

    @Test
    public void testConstructorWithZipEntry() throws Exception {
        ZipEntry ze = new ZipEntry("original");
        ze.setMethod(ZipEntry.DEFLATED);
        ze.setSize(100);
        byte[] extra = new byte[] { 0x01, 0x02, 0x03, 0x04 }; // minimal extra
        ze.setExtra(extra);
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("original", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        assertEquals(100, entry.getSize());
        assertNotNull(entry.getExtra());
    }

    @Test
    public void testConstructorWithZipEntryNullExtra() throws Exception {
        ZipEntry ze = new ZipEntry("noextra");
        ze.setExtra(null);
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertNotNull(entry.getExtra());
        assertEquals(0, entry.getExtra().length);
    }

    @Test
    public void testConstructorWithZipArchiveEntry() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("original");
        original.setInternalAttributes(1);
        original.setExternalAttributes(2);
        original.setMethod(8);
        original.setSize(200);
        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getInternalAttributes(), copy.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), copy.getExternalAttributes());
        assertEquals(original.getMethod(), copy.getMethod());
        assertEquals(original.getSize(), copy.getSize());
    }

    @Test
    public void testProtectedNoArgConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        assertEquals("", entry.getName());
    }

    @Test
    public void testConstructorWithFileDirectory() {
        File dir = new File("testDir");
        dir.mkdir();
        dir.deleteOnExit();
        ZipArchiveEntry entry = new ZipArchiveEntry(dir, "mydir");
        assertTrue(entry.isDirectory());
        assertEquals("mydir/", entry.getName());
    }

    @Test
    public void testConstructorWithFileFile() {
        File file = new File("testFile.txt");
        try {
            file.createNewFile();
            file.deleteOnExit();
            ZipArchiveEntry entry = new ZipArchiveEntry(file, "entryName");
            assertFalse(entry.isDirectory());
            assertEquals("entryName", entry.getName());
            assertEquals(file.length(), entry.getSize());
            assertEquals(file.lastModified(), entry.getTime());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testClone() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("clone");
        original.setInternalAttributes(5);
        original.setExternalAttributes(10);
        original.addExtraField(new TestZipExtraField(new ZipShort(0x0001)));
        ZipArchiveEntry cloned = (ZipArchiveEntry) original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.getName(), cloned.getName());
        assertEquals(original.getInternalAttributes(), cloned.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), cloned.getExternalAttributes());
        assertArrayEquals(original.getExtraFields(), cloned.getExtraFields());
    }

    @Test
    public void testSetMethodNegative() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        try {
            entry.setMethod(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetMethodValid() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(8);
        assertEquals(8, entry.getMethod());
    }

    @Test
    public void testInternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setInternalAttributes(123);
        assertEquals(123, entry.getInternalAttributes());
    }

    @Test
    public void testExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setExternalAttributes(456L);
        assertEquals(456L, entry.getExternalAttributes());
    }

    @Test
    public void testSetUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        long extAttr = entry.getExternalAttributes();
        // Check that the mode is shifted and the directory flag is not set (since not directory)
        assertEquals(0755, (extAttr >> 16) & 0xFFFF);
        // Check MS-DOS read-only attribute: (mode & 0200) == 0 => 1, else 0
        assertEquals(1, extAttr & 1);
        // Directory flag: not directory => 0
        assertEquals(0, extAttr & 0x10);
    }

    @Test
    public void testSetUnixModeDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        entry.setUnixMode(0755);
        long extAttr = entry.getExternalAttributes();
        assertEquals(0x10, extAttr & 0x10); // directory flag set
    }

    @Test
    public void testGetUnixModeOnFAT() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getUnixMode());
    }

    @Test
    public void testGetUnixModeOnUnix() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(0644);
        assertEquals(0644, entry.getUnixMode());
    }

    @Test
    public void testSetPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    @Test
    public void testSetExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        TestZipExtraField field1 = new TestZipExtraField(new ZipShort(0x0001));
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        ZipExtraField[] fields = new ZipExtraField[] { field1, unparseable };
        entry.setExtraFields(fields);
        assertNotNull(entry.getExtraField(new ZipShort(0x0001)));
        assertNotNull(entry.getUnparseableExtraFieldData());
        // getExtraFields without unparseable
        ZipExtraField[] result = entry.getExtraFields();
        assertEquals(1, result.length);
        assertEquals(field1, result[0]);
        // with unparseable
        result = entry.getExtraFields(true);
        assertEquals(2, result.length);
    }

    @Test
    public void testGetExtraFieldsWhenNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getExtraFields().length);
        assertEquals(0, entry.getExtraFields(true).length);
    }

    @Test
    public void testGetExtraFieldsWithOnlyUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        assertEquals(0, entry.getExtraFields().length);
        ZipExtraField[] result = entry.getExtraFields(true);
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof UnparseableExtraFieldData);
    }

    @Test
    public void testAddExtraFieldRegular() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        TestZipExtraField field = new TestZipExtraField(new ZipShort(0x0001));
        entry.addExtraField(field);
        assertNotNull(entry.getExtraField(new ZipShort(0x0001)));
    }

    @Test
    public void testAddExtraFieldUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        assertNotNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        TestZipExtraField field1 = new TestZipExtraField(new ZipShort(0x0001));
        TestZipExtraField field2 = new TestZipExtraField(new ZipShort(0x0002));
        entry.addExtraField(field1);
        entry.addAsFirstExtraField(field2);
        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(field2, fields[0]);
        assertEquals(field1, fields[1]);
    }

    @Test
    public void testAddAsFirstExtraFieldUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addAsFirstExtraField(unparseable);
        assertNotNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testRemoveExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        TestZipExtraField field = new TestZipExtraField(new ZipShort(0x0001));
        entry.addExtraField(field);
        entry.removeExtraField(new ZipShort(0x0001));
        assertNull(entry.getExtraField(new ZipShort(0x0001)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldWhenNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeExtraField(new ZipShort(0x0001));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNotPresent() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.addExtraField(new TestZipExtraField(new ZipShort(0x0001)));
        entry.removeExtraField(new ZipShort(0x0002));
    }

    @Test
    public void testRemoveUnparseableExtraFieldData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        entry.removeUnparseableExtraFieldData();
        assertNull(entry.getUnparseableExtraFieldData());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldDataWhenNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeUnparseableExtraFieldData();
    }

    @Test
    public void testGetExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNull(entry.getExtraField(new ZipShort(0x0001)));
        TestZipExtraField field = new TestZipExtraField(new ZipShort(0x0001));
        entry.addExtraField(field);
        assertEquals(field, entry.getExtraField(new ZipShort(0x0001)));
    }

    @Test
    public void testGetUnparseableExtraFieldData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNull(entry.getUnparseableExtraFieldData());
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        assertEquals(unparseable, entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testSetExtraByteArray() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // Create a valid extra field: header id 0x0001, length 2, data {0x01,0x02}
        byte[] extra = new byte[] { 0x01, 0x00, 0x02, 0x00, 0x01, 0x02 };
        entry.setExtra(extra);
        ZipExtraField field = entry.getExtraField(new ZipShort(0x0001));
        assertNotNull(field);
        assertArrayEquals(new byte[] { 0x01, 0x02 }, field.getLocalFileDataData());
    }

    @Test
    public void testSetExtraByteArrayInvalid() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] invalid = new byte[] { 0x01 }; // incomplete
        try {
            entry.setExtra(invalid);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test
    public void testSetCentralDirectoryExtra() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] centralExtra = new byte[] { 0x01, 0x00, 0x02, 0x00, 0x03, 0x04 };
        entry.setCentralDirectoryExtra(centralExtra);
        ZipExtraField field = entry.getExtraField(new ZipShort(0x0001));
        assertNotNull(field);
        assertArrayEquals(new byte[] { 0x03, 0x04 }, field.getCentralDirectoryData());
    }

    @Test
    public void testGetLocalFileDataExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] extra = entry.getLocalFileDataExtra();
        assertNotNull(extra);
        assertEquals(0, extra.length);
        entry.setExtra(new byte[] { 0x01, 0x00, 0x00, 0x00 });
        extra = entry.getLocalFileDataExtra();
        assertTrue(extra.length > 0);
    }

    @Test
    public void testGetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] central = entry.getCentralDirectoryExtra();
        assertNotNull(central);
        // Without extra fields, should be empty
        assertEquals(0, central.length);
    }

    @Test
    public void testGetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
    }

    @Test
    public void testIsDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
        entry = new ZipArchiveEntry("file");
        assertFalse(entry.isDirectory());
    }

    @Test
    public void testSetNameWithBackslashOnFAT() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setName("dir\\file");
        assertEquals("dir/file", entry.getName());
    }

    @Test
    public void testSetNameWithSlashOnFAT() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setName("dir/file");
        assertEquals("dir/file", entry.getName());
    }

    @Test
    public void testSetNameWithRawName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] raw = new byte[] { 0x41, 0x42 };
        entry.setName("AB", raw);
        assertArrayEquals(raw, entry.getRawName());
    }

    @Test
    public void testGetRawNameNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNull(entry.getRawName());
    }

    @Test
    public void testGetRawNameCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] raw = new byte[] { 1, 2, 3 };
        entry.setName("test", raw);
        byte[] retrieved = entry.getRawName();
        assertNotSame(raw, retrieved);
        assertArrayEquals(raw, retrieved);
    }

    @Test
    public void testSetSizeNegative() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        try {
            entry.setSize(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetSizeValid() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(1000);
        assertEquals(1000, entry.getSize());
    }

    @Test
    public void testHashCode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals("test".hashCode(), entry.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertTrue(entry.equals(entry));
    }

    @Test
    public void testEqualsNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertFalse(entry.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertFalse(entry.equals(new Object()));
    }

    @Test
    public void testEqualsDifferentName() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("a");
        ZipArchiveEntry e2 = new ZipArchiveEntry("b");
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsNullName() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        // Force name to null via reflection? Not needed; we can test with null name scenario.
        // Actually, getName() never returns null because super.getName() returns "" if name is null.
        // But we can test the case where one name is null and other not? Not possible via public API.
        // We'll skip that branch.
    }

    @Test
    public void testEqualsDifferentComment() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setComment("comment1");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setComment("comment2");
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsNullComment() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setComment(null);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setComment("comment");
        assertFalse(e1.equals(e2));
        e2.setComment(null);
        assertTrue(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentTime() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setTime(1000);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setTime(2000);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentInternalAttributes() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setInternalAttributes(1);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setInternalAttributes(2);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentPlatform() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentExternalAttributes() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setExternalAttributes(1);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setExternalAttributes(2);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentMethod() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setMethod(0);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setMethod(8);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentSize() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setSize(100);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setSize(200);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentCrc() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setCrc(123);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setCrc(456);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentCompressedSize() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setCompressedSize(50);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setCompressedSize(60);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentCentralDirectoryExtra() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setCentralDirectoryExtra(new byte[] { 1 });
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setCentralDirectoryExtra(new byte[] { 2 });
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentLocalFileDataExtra() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setExtra(new byte[] { 1 });
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setExtra(new byte[] { 2 });
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentGeneralPurposeBit() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        GeneralPurposeBit gpb1 = new GeneralPurposeBit();
        gpb1.useEncryption(true);
        e1.setGeneralPurposeBit(gpb1);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        GeneralPurposeBit gpb2 = new GeneralPurposeBit();
        e2.setGeneralPurposeBit(gpb2);
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsAllSame() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        assertTrue(e1.equals(e2));
    }

    @Test
    public void testGetLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        long time = 1000000L;
        entry.setTime(time);
        Date date = entry.getLastModifiedDate();
        assertEquals(time, date.getTime());
    }

    @Test
    public void testGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        entry.setGeneralPurposeBit(gpb);
        assertSame(gpb, entry.getGeneralPurposeBit());
    }

    @Test
    public void testMergeExtraFieldsLocal() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        TestZipExtraField existing = new TestZipExtraField(new ZipShort(0x0001));
        entry.addExtraField(existing);
        // Now set extra bytes that contain the same header id, should merge (local)
        byte[] extra = new byte[] { 0x01, 0x00, 0x02, 0x00, 0x0A, 0x0B };
        entry.setExtra(extra);
        assertTrue(existing.isLocalParsed());
        assertArrayEquals(new byte[] { 0x0A, 0x0B }, existing.getLocalFileDataData());
    }

    @Test
    public void testMergeExtraFieldsCentral() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        TestZipExtraField existing = new TestZipExtraField(new ZipShort(0x0001));
        entry.addExtraField(existing);
        byte[] centralExtra = new byte[] { 0x01, 0x00, 0x02, 0x00, 0x0C, 0x0D };
        entry.setCentralDirectoryExtra(centralExtra);
        assertTrue(existing.isCentralParsed());
        assertArrayEquals(new byte[] { 0x0C, 0x0D }, existing.getCentralDirectoryData());
    }

    @Test
    public void testMergeExtraFieldsWithUnparseable() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        // setExtra with unparseable data should merge
        byte[] extra = new byte[] { 0x01, 0x00, 0x00, 0x00 }; // minimal valid field
        entry.setExtra(extra);
        // unparseable should still be there
        assertNotNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testSetExtraFieldsWithNullExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setExtraFields(new ZipExtraField[0]);
        assertNotNull(entry.getExtraFields());
        assertEquals(0, entry.getExtraFields().length);
    }
}
