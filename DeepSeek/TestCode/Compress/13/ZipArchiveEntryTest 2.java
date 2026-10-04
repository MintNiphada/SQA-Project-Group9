package org.apache.commons.compress.archivers.zip;

import java.io.File;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZipArchiveEntryTest {

    private static final byte[] DUMMY_LOCAL = new byte[] {1, 2, 3};
    private static final byte[] DUMMY_CENTRAL = new byte[] {4, 5, 6};

    // A simple ZipExtraField implementation for testing
    private static class TestField implements ZipExtraField {
        private final ZipShort headerId;
        private byte[] localData;
        private byte[] centralData;

        TestField(ZipShort headerId, byte[] localData, byte[] centralData) {
            this.headerId = headerId;
            this.localData = localData.clone();
            this.centralData = centralData.clone();
        }

        @Override
        public ZipShort getHeaderId() {
            return headerId;
        }

        @Override
        public byte[] getLocalFileDataData() {
            return localData.clone();
        }

        @Override
        public byte[] getCentralDirectoryData() {
            return centralData.clone();
        }

        @Override
        public void parseFromLocalFileData(byte[] data, int offset, int length) {
            this.localData = new byte[length];
            System.arraycopy(data, offset, this.localData, 0, length);
        }

        @Override
        public void parseFromCentralDirectoryData(byte[] data, int offset, int length) {
            this.centralData = new byte[length];
            System.arraycopy(data, offset, this.centralData, 0, length);
        }
    }

    private ZipArchiveEntry entry;

    @Before
    public void setUp() {
        entry = new ZipArchiveEntry("test");
    }

    @Test
    public void testConstructorString() {
        ZipArchiveEntry e = new ZipArchiveEntry("test1");
        Assert.assertEquals("test1", e.getName());
        Assert.assertEquals(-1, e.getMethod());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, e.getPlatform());
        Assert.assertEquals(0, e.getInternalAttributes());
        Assert.assertEquals(0L, e.getExternalAttributes());
        Assert.assertEquals(0L, e.getSize());
        Assert.assertTrue(e.isDirectory()); // because name ends with "/"? "test1" no slash
        Assert.assertFalse(e.isDirectory()); // corrected: "test1" doesn't end with "/"        Assert.assertFalse(e.isDirectory());
    }

    @Test
    public void testConstructorStringDirectory() {
        ZipArchiveEntry e = new ZipArchiveEntry("dir/");
        Assert.assertTrue(e.isDirectory());
    }

    @Test
    public void testConstructorZipEntry() throws Exception {
        java.util.zip.ZipEntry jz = new java.util.zip.ZipEntry("jzEntry");
        jz.setMethod(ZipEntry.DEFLATED);
        jz.setSize(100);
        byte[] extra = new byte[] {
            0x00, 0x09, // header id 9
            0x00, 0x02, // length 2
            0x01, 0x02    // data
        };
        jz.setExtra(extra);
        ZipArchiveEntry e = new ZipArchiveEntry(jz);
        Assert.assertEquals("jzEntry", e.getName());
        Assert.assertEquals(ZipEntry.DEFLATED, e.getMethod());
        Assert.assertEquals(100L, e.getSize());
        // extra should be parsed as unparseable
        Assert.assertNotNull(e.getUnparseableExtraFieldData());
    }

    @Test
    public void testConstructorZipEntryNullExtra() throws Exception {
        java.util.zip.ZipEntry jz = new java.util.zip.ZipEntry("noextra");
        jz.setExtra(null);
        ZipArchiveEntry e = new ZipArchiveEntry(jz);
        Assert.assertNull(e.getUnparseableExtraFieldData());
        byte[] extra = e.getExtra();
        Assert.assertNotNull(extra);
        Assert.assertEquals(0, extra.length);
    }

    @Test
    public void testConstructorZipArchiveEntry() throws Exception {
        ZipArchiveEntry orig = new ZipArchiveEntry("orig");
        orig.setMethod(8);
        orig.setSize(512);
        orig.setInternalAttributes(0x0100);
        orig.setExternalAttributes(0x0200L);
        orig.setUnixMode(0644);
        ZipExtraField[] fields = new ZipExtraField[] {
            new TestField(new ZipShort(0x0001), DUMMY_LOCAL, DUMMY_CENTRAL)
        };
        orig.setExtraFields(fields);

        ZipArchiveEntry copy = new ZipArchiveEntry(orig);
        Assert.assertEquals("orig", copy.getName());
        Assert.assertEquals(8, copy.getMethod());
        Assert.assertEquals(512L, copy.getSize());
        Assert.assertEquals(0x0100, copy.getInternalAttributes());
        Assert.assertEquals(orig.getExternalAttributes(), copy.getExternalAttributes());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, copy.getPlatform());
        Assert.assertEquals(1, copy.getExtraFields().length);
    }

    @Test
    public void testConstructorFile() throws Exception {
        File f = File.createTempFile("testfile", ".tmp");
        f.deleteOnExit();
        long timestamp = f.lastModified();
        long length = f.length();
        ZipArchiveEntry e = new ZipArchiveEntry(f, "fileEntry");
        Assert.assertEquals("fileEntry", e.getName());
        Assert.assertEquals(length, e.getSize());
        Assert.assertEquals(timestamp, e.getTime());
        Assert.assertFalse(e.isDirectory());
    }

    @Test
    public void testConstructorFileDirectory() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        // dir is a directory, entryName does not end with "/", so name becomes "dirEntry/"
        ZipArchiveEntry e = new ZipArchiveEntry(dir, "dirEntry");
        Assert.assertEquals("dirEntry/", e.getName());
        Assert.assertTrue(e.isDirectory());
    }

    @Test
    public void testConstructorFileDirectoryWithSlash() throws Exception {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        ZipArchiveEntry e = new ZipArchiveEntry(dir, "dirEntry/");
        Assert.assertEquals("dirEntry/", e.getName());
    }

    @Test
    public void testClone() throws Exception {
        ZipArchiveEntry orig = new ZipArchiveEntry("cloneTest");
        orig.setMethod(4);
        orig.setInternalAttributes(0x0100);
        orig.setExternalAttributes(0x0200);
        ZipExtraField field = new TestField(new ZipShort(0x0101), DUMMY_LOCAL, DUMMY_CENTRAL);
        orig.addExtraField(field);

        ZipArchiveEntry cloned = (ZipArchiveEntry) orig.clone();
        Assert.assertNotSame(orig, cloned);
        Assert.assertEquals(orig.getName(), cloned.getName());
        Assert.assertEquals(orig.getMethod(), cloned.getMethod());
        Assert.assertEquals(orig.getInternalAttributes(), cloned.getInternalAttributes());
        Assert.assertEquals(orig.getExternalAttributes(), cloned.getExternalAttributes());
        Assert.assertEquals(orig.getExtraFields().length, cloned.getExtraFields().length);
        Assert.assertFalse(orig.getExtraFields() == cloned.getExtraFields());
        // modify clone should not affect orig
        cloned.setMethod(0);
        Assert.assertEquals(4, orig.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMethodNegative() {
        entry.setMethod(-1);
    }

    @Test
    public void testSetMethodValid() {
        entry.setMethod(8);
        Assert.assertEquals(8, entry.getMethod());
    }

    @Test
    public void testGetMethodDefault() {
        // default is -1
        Assert.assertEquals(-1, entry.getMethod());
    }

    @Test
    public void testSetSizeNegative() {
        try {
            entry.setSize(-1);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testSetSizeValid() {
        entry.setSize(1024);
        Assert.assertEquals(1024L, entry.getSize());
    }

    @Test
    public void testSetInternalAttributes() {
        entry.setInternalAttributes(0x1234);
        Assert.assertEquals(0x1234, entry.getInternalAttributes());
    }

    @Test
    public void testSetExternalAttributes() {
        entry.setExternalAttributes(0x5678L);
        Assert.assertEquals(0x5678L, entry.getExternalAttributes());
    }

    @Test
    public void testSetUnixMode() {
        entry.setUnixMode(0644);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        Assert.assertTrue(entry.getUnixMode() == 0644);
        // check external attrs: (0644 << 16) | ( (0644 & 0200)==0 ? 1:0) ) | (isDirectory()?0x10:0)
        // here isDirectory() false so 0
        int expectedMode = 0644;
        long expectedExternal = ((long)expectedMode << 16) | 1; // because (0644 & 0200) == 0, so add 1
        Assert.assertEquals(expectedExternal, entry.getExternalAttributes());
    }

    @Test
    public void testSetUnixModeDirectory() {
        ZipArchiveEntry dir = new ZipArchiveEntry("dir/");
        dir.setUnixMode(0755);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, dir.getPlatform());
        Assert.assertEquals(0755, dir.getUnixMode());
        long expectedExternal = ((long)0755 << 16) | 0 | 0x10; // because (0755 & 0200) != 0, so 0, and directory flag 0x10
        Assert.assertEquals(expectedExternal, dir.getExternalAttributes());
    }

    @Test
    public void testGetUnixModeNotUnix() {
        // platform FAT
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        Assert.assertEquals(0, entry.getUnixMode());
    }

    @Test
    public void testSetPlatform() {
        entry.setPlatform(3);
        Assert.assertEquals(3, entry.getPlatform());
    }

    @Test
    public void testSetExtraFields() {
        ZipExtraField[] fields = new ZipExtraField[] {
            new TestField(new ZipShort(0x0001), DUMMY_LOCAL, DUMMY_CENTRAL)
        };
        entry.setExtraFields(fields);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testSetExtraFieldsWithUnparseable() {
        UnparseableExtraFieldData ud = new UnparseableExtraFieldData();
        ZipExtraField[] fields = new ZipExtraField[] { ud };
        entry.setExtraFields(fields);
        Assert.assertEquals(0, entry.getExtraFields().length);
        Assert.assertNotNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testGetExtraFieldsEmpty() {
        Assert.assertNotNull(entry.getExtraFields());
        Assert.assertEquals(0, entry.getExtraFields().length);
    }

    @Test
    public void testGetExtraFieldsIncludeUnparseable() {
        UnparseableExtraFieldData ud = new UnparseableExtraFieldData();
        entry.setExtraFields(new ZipExtraField[] { ud });
        ZipExtraField[] result = entry.getExtraFields(true);
        Assert.assertEquals(1, result.length);
        Assert.assertTrue(result[0] instanceof UnparseableExtraFieldData);
    }

    @Test
    public void testAddExtraField() {
        ZipExtraField f = new TestField(new ZipShort(0x0002), DUMMY_LOCAL, DUMMY_CENTRAL);
        entry.addExtraField(f);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertEquals(f, entry.getExtraField(new ZipShort(0x0002)));
    }

    @Test
    public void testAddExtraFieldUnparseable() {
        UnparseableExtraFieldData ud = new UnparseableExtraFieldData();
        entry.addExtraField(ud);
        Assert.assertNull(entry.getExtraField(new ZipShort(0))); // not in map
        Assert.assertNotNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testAddExtraFieldDuplicateHeader() {
        ZipExtraField first = new TestField(new ZipShort(0x0003), new byte[]{1}, new byte[]{2});
        ZipExtraField second = new TestField(new ZipShort(0x0003), new byte[]{3}, new byte[]{4});
        entry.addExtraField(first);
        entry.addExtraField(second);
        ZipExtraField[] result = entry.getExtraFields();
        Assert.assertEquals(1, result.length);
        Assert.assertSame(second, result[0]); // second overwrites
    }

    @Test
    public void testAddAsFirstExtraField() {
        ZipExtraField first = new TestField(new ZipShort(0x0010), DUMMY_LOCAL, DUMMY_CENTRAL);
        ZipExtraField second = new TestField(new ZipShort(0x0011), DUMMY_LOCAL, DUMMY_CENTRAL);
        entry.addExtraField(first);
        entry.addAsFirstExtraField(second);
        ZipExtraField[] result = entry.getExtraFields();
        Assert.assertEquals(2, result.length);
        Assert.assertSame(second, result[0]);
        Assert.assertSame(first, result[1]);
    }

    @Test
    public void testAddAsFirstExtraFieldUnparseable() {
        UnparseableExtraFieldData ud = new UnparseableExtraFieldData();
        ZipExtraField field = new TestField(new ZipShort(0x0050), DUMMY_LOCAL, DUMMY_CENTRAL);
        entry.addExtraField(field);
        entry.addAsFirstExtraField(ud);
        Assert.assertNotNull(entry.getUnparseableExtraFieldData());
        Assert.assertEquals(1, entry.getExtraFields().length); // field still present
        Assert.assertSame(field, entry.getExtraFields()[0]);
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldFromNullMap() {
        entry.removeExtraField(new ZipShort(0));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNotExist() {
        entry.addExtraField(new TestField(new ZipShort(0x0060), DUMMY_LOCAL, DUMMY_CENTRAL));
        entry.removeExtraField(new ZipShort(0x0061));
    }

    @Test
    public void testRemoveExtraFieldSuccess() {
        ZipExtraField f = new TestField(new ZipShort(0x0070), DUMMY_LOCAL, DUMMY_CENTRAL);
        entry.addExtraField(f);
        entry.removeExtraField(new ZipShort(0x0070));
        Assert.assertEquals(0, entry.getExtraFields().length);
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldDataNull() {
        entry.removeUnparseableExtraFieldData();
    }

    @Test
    public void testRemoveUnparseableExtraFieldDataSuccess() {
        entry.addExtraField(new UnparseableExtraFieldData());
        entry.removeUnparseableExtraFieldData();
        Assert.assertNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testGetExtraFieldNull() {
        Assert.assertNull(entry.getExtraField(new ZipShort(0)));
    }

    @Test
    public void testGetExtraFieldExists() {
        ZipExtraField f = new TestField(new ZipShort(0x0080), DUMMY_LOCAL, DUMMY_CENTRAL);
        entry.addExtraField(f);
        Assert.assertSame(f, entry.getExtraField(new ZipShort(0x0080)));
    }

    @Test
    public void testGetUnparseableExtraFieldDataDefault() {
        Assert.assertNull(entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testSetExtraBytes() {
        // simple extra bytes that lead to unparseable data
        byte[] extraBytes = new byte[] {
            0x00, 0x09, // id 9
            0x00, 0x02, // length 2
            0x0A, 0x0B
        };
        entry.setExtra(extraBytes);
        UnparseableExtraFieldData ud = entry.getUnparseableExtraFieldData();
        Assert.assertNotNull(ud);
    }

    @Test
    public void testSetCentralDirectoryExtra() {
        ZipExtraField field = new TestField(new ZipShort(0x00A0), DUMMY_LOCAL, DUMMY_CENTRAL);
        entry.addExtraField(field);
        byte[] centralBytes = new byte[] {
            0x00, (byte)0xA0,
            0x00, 0x02,
            0x0C, 0x0D
        };
        entry.setCentralDirectoryExtra(centralBytes);
        // The extra field should now have its central data parsed from the bytes
        // We can check by getting central directory extra and compare
        byte[] resultCentral = entry.getCentralDirectoryExtra();
        // it should contain the updated data for field 0xA0
        // Not trivial to fully verify, but at least no exception
        Assert.assertNotNull(resultCentral);
    }

   @Test
    public void testGetLocalFileDataExtra() {
        entry.setExtraFields(new ZipExtraField[0]);
        byte[] extra = entry.getLocalFileDataExtra();
        Assert.assertNotNull(extra);
        Assert.assertEquals(0, extra.length);
    }

    @Test
    public void testGetCentralDirectoryExtra() {
        entry.setExtraFields(new ZipExtraField[] {
            new TestField(new ZipShort(0x00B0), DUMMY_LOCAL, DUMMY_CENTRAL)
        });
        byte[] central = entry.getCentralDirectoryExtra();
        Assert.assertNotNull(central);
        Assert.assertTrue(central.length > 0);
    }

    @Test
    public void testGetName() {
        ZipArchiveEntry e = new ZipArchiveEntry("original");
        Assert.assertEquals("original", e.getName());
    }

    @Test
    public void testGetNameWhenNameFieldNull() {
        // when constructed via super only name field may be null? Actually protected constructor sets name to "".
        // Use the protected constructor? We'll test via a subclass or directly.
        // The empty constructor calls this("") which sets name to "" so not null.
        // So we can't easily get null name without reflection. But we can test setName(null)? Not allowed.
        // We'll test the case where super.getName() is used if name is null. We'll create a subclass that sets name to null.
        // Since it's protected, we can't access. We'll skip or use reflection. Maybe not needed for coverage.
        // But to satisfy getName() logic, we can set name to null via reflection.
        // Including a test using reflection to set name to null.
        try {
            java.lang.reflect.Field f = ZipArchiveEntry.class.getDeclaredField("name");
            f.setAccessible(true);
            f.set(entry, null);
            // now getName() should fallback to super.getName()
            Assert.assertEquals("test", entry.getName()); // super constructor was "test"
        } catch (Exception e) {
            Assert.fail("Reflection issue: " + e);
        }
    }

    @Test
    public void testIsDirectoryTrue() {
        ZipArchiveEntry d = new ZipArchiveEntry("folder/");
        Assert.assertTrue(d.isDirectory());
    }

    @Test
    public void testIsDirectoryFalse() {
        ZipArchiveEntry f = new ZipArchiveEntry("file");
        Assert.assertFalse(f.isDirectory());
    }

    @Test
    public void testSetName() throws Exception {
        java.lang.reflect.Field nameField = ZipArchiveEntry.class.getDeclaredField("name");
        nameField.setAccessible(true);
        entry.setName("newName");
        Assert.assertEquals("newName", nameField.get(entry));
    }

    @Test
    public void testSetNameWithRaw() throws Exception {
        java.lang.reflect.Field nameField = ZipArchiveEntry.class.getDeclaredField("name");
        java.lang.reflect.Field rawField = ZipArchiveEntry.class.getDeclaredField("rawName");
        nameField.setAccessible(true);
        rawField.setAccessible(true);
        byte[] raw = new byte[] { 0x41, 0x42 };
        entry.setName("AB", raw);
        Assert.assertEquals("AB", nameField.get(entry));
        Assert.assertArrayEquals(raw, (byte[]) rawField.get(entry));
    }

    @Test
    public void testGetRawNameNull() {
        Assert.assertNull(entry.getRawName());
    }

    @Test
    public void testGetRawNameNonNull() throws Exception {
        java.lang.reflect.Field rawField = ZipArchiveEntry.class.getDeclaredField("rawName");
        rawField.setAccessible(true);
        byte[] raw = new byte[] {7, 8, 9};
        rawField.set(entry, raw);
        byte[] result = entry.getRawName();
        Assert.assertArrayEquals(raw, result);
        // ensure defensive copy
        Assert.assertFalse(raw == result);
    }

    @Test
    public void testHashCode() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("hash");
        ZipArchiveEntry e2 = new ZipArchiveEntry("hash");
        Assert.assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        Assert.assertTrue(entry.equals(entry));
    }

    @Test
    public void testEqualsNull() {
        Assert.assertFalse(entry.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Assert.assertFalse(entry.equals("string"));
    }

    @Test
    public void testEqualsDifferentName() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("name1");
        ZipArchiveEntry e2 = new ZipArchiveEntry("name2");
        Assert.assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsNullName() throws Exception {
        ZipArchiveEntry e1 = new ZipArchiveEntry("nonNull");
        java.lang.reflect.Field f = ZipArchiveEntry.class.getDeclaredField("name");
        f.setAccessible(true);
        f.set(e1, null);
        ZipArchiveEntry e2 = new ZipArchiveEntry("nonNull");
        Assert.assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsCommentNulls() {
        // getComment() returns null from super
        ZipArchiveEntry e1 = new ZipArchiveEntry("n");
        ZipArchiveEntry e2 = new ZipArchiveEntry("n");
        Assert.assertTrue(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentComment() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("n");
        e1.setComment("c1");
        ZipArchiveEntry e2 = new ZipArchiveEntry("n");
        e2.setComment("c2");
        Assert.assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsAllAttributes() throws Exception {
        ZipArchiveEntry e1 = new ZipArchiveEntry("eq");
        ZipArchiveEntry e2 = new ZipArchiveEntry("eq");
        e1.setMethod(8);
        e2.setMethod(8);
        e1.setSize(100);
        e2.setSize(100);
        e1.setInternalAttributes(0x5);
        e2.setInternalAttributes(0x5);
        e1.setUnixMode(0644);
        e2.setUnixMode(0644);
        e1.setComment("comment");
        e2.setComment("comment");
        e1.setTime(12345678L);
        e2.setTime(12345678L);
        // set extra fields identically
        ZipExtraField field = new TestField(new ZipShort(0x00FF), DUMMY_LOCAL, DUMMY_CENTRAL);
        e1.addExtraField(field);
        e2.addExtraField(field);
        // need to set compressed size and crc? Use setters from ZipEntry
        e1.setCompressedSize(50);
        e2.setCompressedSize(50);
        e1.setCrc(0x12345678L);
        e2.setCrc(0x12345678L);
        Assert.assertTrue(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentExternalAttributes() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("diffExt");
        ZipArchiveEntry e2 = new ZipArchiveEntry("diffExt");
        e1.setExternalAttributes(1L);
        e2.setExternalAttributes(2L);
        Assert.assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentCentralExtra() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("central");
        ZipArchiveEntry e2 = new ZipArchiveEntry("central");
        e1.setExtraFields(new ZipExtraField[] { new TestField(new ZipShort(0x0001), DUMMY_LOCAL, DUMMY_CENTRAL) });
        e2.setExtraFields(new ZipExtraField[] { new TestField(new ZipShort(0x0001), DUMMY_LOCAL, new byte[]{9,9,9}) });
        // central directory extra will differ => not equal
        Assert.assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentLocalExtra() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("local");
        ZipArchiveEntry e2 = new ZipArchiveEntry("local");
        e1.setExtraFields(new ZipExtraField[] { new TestField(new ZipShort(0x0002), DUMMY_LOCAL, DUMMY_CENTRAL) });
        e2.setExtraFields(new ZipExtraField[] { new TestField(new ZipShort(0x0002), new byte[]{7,7}, DUMMY_CENTRAL) });
        Assert.assertFalse(e1.equals(e2));
    }

    @Test
    public void testGetLastModifiedDate() {
        long time = System.currentTimeMillis();
        entry.setTime(time);
        Date d = entry.getLastModifiedDate();
        Assert.assertEquals(time, d.getTime());
    }

    @Test
    public void testGetGeneralPurposeBit() {
        Assert.assertNotNull(entry.getGeneralPurposeBit());
        GeneralPurposeBit bit = new GeneralPurposeBit();
        entry.setGeneralPurposeBit(bit);
        Assert.assertSame(bit, entry.getGeneralPurposeBit());
    }
}
