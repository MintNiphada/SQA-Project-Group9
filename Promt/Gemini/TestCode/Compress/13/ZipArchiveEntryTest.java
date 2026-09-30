package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ZipArchiveEntryTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testStringConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertFalse(entry.isDirectory());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        Assert.assertEquals(-1, entry.getMethod());
        Assert.assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, entry.getSize());
        Assert.assertEquals(0, entry.getInternalAttributes());
        Assert.assertEquals(0L, entry.getExternalAttributes());
        Assert.assertNull(entry.getRawName());
        Assert.assertNotNull(entry.getGeneralPurposeBit());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("dir/");
        Assert.assertEquals("dir/", dirEntry.getName());
        Assert.assertTrue(dirEntry.isDirectory());
    }

    @Test
    public void testDefaultConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        Assert.assertEquals("", entry.getName());
        Assert.assertFalse(entry.isDirectory());
    }

    @Test
    public void testJavaZipEntryConstructorWithoutExtra() throws Exception {
        java.util.zip.ZipEntry baseEntry = new java.util.zip.ZipEntry("foo.bar");
        baseEntry.setMethod(ZipEntry.DEFLATED);
        baseEntry.setSize(1024L);
        baseEntry.setTime(1000000L);
        baseEntry.setComment("a comment");

        ZipArchiveEntry entry = new ZipArchiveEntry(baseEntry);
        Assert.assertEquals("foo.bar", entry.getName());
        Assert.assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        Assert.assertEquals(1024L, entry.getSize());
        Assert.assertEquals(1000000L, entry.getTime());
        Assert.assertEquals("a comment", entry.getComment());
        Assert.assertEquals(0, entry.getExtraFields().length);
    }

    @Test
    public void testJavaZipEntryConstructorWithExtra() throws Exception {
        java.util.zip.ZipEntry baseEntry = new java.util.zip.ZipEntry("foo.bar");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(1));
        uef.setLocalFileDataData(new byte[]{1, 2, 3});
        baseEntry.setExtra(uef.getHeaderId().getBytes());

        byte[] extraData = new byte[] {
            1, 0, // Header ID 1
            3, 0, // Length 3
            1, 2, 3 // Data
        };
        baseEntry.setExtra(extraData);

        ZipArchiveEntry entry = new ZipArchiveEntry(baseEntry);
        Assert.assertEquals("foo.bar", entry.getName());
        ZipExtraField[] fields = entry.getExtraFields();
        Assert.assertEquals(1, fields.length);
        Assert.assertEquals(new ZipShort(1), fields[0].getHeaderId());
    }

    @Test
    public void testCopyConstructor() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("test.txt");
        original.setInternalAttributes(2);
        original.setExternalAttributes(0x41FD0000L);
        original.setMethod(ZipEntry.STORED);
        original.setSize(500L);
        original.setUnixMode(0755);

        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1234));
        uef.setLocalFileDataData(new byte[]{1, 2});
        original.addExtraField(uef);

        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        Assert.assertEquals(original.getName(), copy.getName());
        Assert.assertEquals(original.getInternalAttributes(), copy.getInternalAttributes());
        Assert.assertEquals(original.getExternalAttributes(), copy.getExternalAttributes());
        Assert.assertEquals(original.getMethod(), copy.getMethod());
        Assert.assertEquals(original.getSize(), copy.getSize());
        Assert.assertEquals(original.getUnixMode(), copy.getUnixMode());
        Assert.assertEquals(1, copy.getExtraFields().length);
        Assert.assertEquals(new ZipShort(0x1234), copy.getExtraFields()[0].getHeaderId());
    }

    @Test
    public void testFileConstructorWithFile() throws IOException {
        File file = tempFolder.newFile("sample.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[]{1, 2, 3, 4, 5});
        }
        long lastMod = file.lastModified();

        ZipArchiveEntry entry = new ZipArchiveEntry(file, "sample.txt");
        Assert.assertEquals("sample.txt", entry.getName());
        Assert.assertFalse(entry.isDirectory());
        Assert.assertEquals(5L, entry.getSize());
        Assert.assertEquals(lastMod, entry.getTime());
    }

    @Test
    public void testFileConstructorWithDirectory() throws IOException {
        File dir = tempFolder.newFolder("sampleDir");
        long lastMod = dir.lastModified();

        ZipArchiveEntry entryWithoutSlash = new ZipArchiveEntry(dir, "sampleDir");
        Assert.assertEquals("sampleDir/", entryWithoutSlash.getName());
        Assert.assertTrue(entryWithoutSlash.isDirectory());
        Assert.assertEquals(lastMod, entryWithoutSlash.getTime());

        ZipArchiveEntry entryWithSlash = new ZipArchiveEntry(dir, "sampleDir/");
        Assert.assertEquals("sampleDir/", entryWithSlash.getName());
        Assert.assertTrue(entryWithSlash.isDirectory());
    }

    @Test
    public void testClone() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry.txt");
        entry.setInternalAttributes(10);
        entry.setExternalAttributes(20L);
        entry.setMethod(ZipEntry.DEFLATED);
        entry.setSize(100L);

        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1000));
        uef.setLocalFileDataData(new byte[]{1});
        entry.addExtraField(uef);

        ZipArchiveEntry clone = (ZipArchiveEntry) entry.clone();
        Assert.assertNotSame(entry, clone);
        Assert.assertEquals(entry.getName(), clone.getName());
        Assert.assertEquals(entry.getInternalAttributes(), clone.getInternalAttributes());
        Assert.assertEquals(entry.getExternalAttributes(), clone.getExternalAttributes());
        Assert.assertEquals(entry.getMethod(), clone.getMethod());
        Assert.assertEquals(entry.getSize(), clone.getSize());
        Assert.assertEquals(1, clone.getExtraFields().length);
        Assert.assertEquals(new ZipShort(0x1000), clone.getExtraFields()[0].getHeaderId());
    }

    @Test
    public void testMethodGetterSetter() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertEquals(-1, entry.getMethod());
        entry.setMethod(ZipEntry.STORED);
        Assert.assertEquals(ZipEntry.STORED, entry.getMethod());
        entry.setMethod(ZipEntry.DEFLATED);
        Assert.assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        entry.setMethod(8);
        Assert.assertEquals(8, entry.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMethodNegativeThrows() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(-2);
    }

    @Test
    public void testSizeGetterSetter() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, entry.getSize());
        entry.setSize(0L);
        Assert.assertEquals(0L, entry.getSize());
        entry.setSize(1048576L);
        Assert.assertEquals(1048576L, entry.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSizeNegativeThrows() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(-1L);
    }

    @Test
    public void testInternalAndExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setInternalAttributes(42);
        Assert.assertEquals(42, entry.getInternalAttributes());

        entry.setExternalAttributes(0x12345678L);
        Assert.assertEquals(0x12345678L, entry.getExternalAttributes());
    }

    @Test
    public void testUnixModeAndPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        Assert.assertEquals(0, entry.getUnixMode());

        entry.setUnixMode(0755);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        Assert.assertEquals(0755, entry.getUnixMode());
        Assert.assertEquals(0, entry.getExternalAttributes() & 1); // read-write (mode & 0200 != 0)

        // Read-only file (mode & 0200 == 0)
        ZipArchiveEntry roEntry = new ZipArchiveEntry("ro.txt");
        roEntry.setUnixMode(0444);
        Assert.assertEquals(0444, roEntry.getUnixMode());
        Assert.assertEquals(1, roEntry.getExternalAttributes() & 1);

        // Directory
        ZipArchiveEntry dirEntry = new ZipArchiveEntry("dir/");
        dirEntry.setUnixMode(0755);
        Assert.assertEquals(0755, dirEntry.getUnixMode());
        Assert.assertEquals(0x10, dirEntry.getExternalAttributes() & 0x10);

        // Change platform manually
        dirEntry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, dirEntry.getPlatform());
        Assert.assertEquals(0, dirEntry.getUnixMode()); // Returns 0 if not PLATFORM_UNIX
    }

    @Test
    public void testExtraFieldsOperations() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Assert.assertEquals(0, entry.getExtraFields().length);
        Assert.assertEquals(0, entry.getExtraFields(true).length);
        Assert.assertEquals(0, entry.getExtraFields(false).length);
        Assert.assertNull(entry.getExtraField(new ZipShort(1)));
        Assert.assertNull(entry.getUnparseableExtraFieldData());

        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[]{9, 8}, 0, 2);

        // getExtraFields(true) when extraFields is null but unparseable is present
        entry.addExtraField(unparseable);
        Assert.assertSame(unparseable, entry.getUnparseableExtraFieldData());
        Assert.assertEquals(0, entry.getExtraFields(false).length);
        Assert.assertEquals(1, entry.getExtraFields(true).length);
        Assert.assertSame(unparseable, entry.getExtraFields(true)[0]);

        UnrecognizedExtraField uef1 = new UnrecognizedExtraField();
        uef1.setHeaderId(new ZipShort(1));
        uef1.setLocalFileDataData(new byte[]{10});

        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        uef2.setHeaderId(new ZipShort(2));
        uef2.setLocalFileDataData(new byte[]{20});

        entry.addExtraField(uef1);
        Assert.assertEquals(1, entry.getExtraFields(false).length);
        Assert.assertEquals(2, entry.getExtraFields(true).length);
        Assert.assertSame(uef1, entry.getExtraField(new ZipShort(1)));

        entry.addExtraField(uef2);
        Assert.assertEquals(2, entry.getExtraFields(false).length);
        Assert.assertEquals(3, entry.getExtraFields(true).length);

        // Replace uef1
        UnrecognizedExtraField uef1Replace = new UnrecognizedExtraField();
        uef1Replace.setHeaderId(new ZipShort(1));
        uef1Replace.setLocalFileDataData(new byte[]{11});
        entry.addExtraField(uef1Replace);
        Assert.assertEquals(2, entry.getExtraFields(false).length);
        Assert.assertSame(uef1Replace, entry.getExtraField(new ZipShort(1)));

        // Remove field
        entry.removeExtraField(new ZipShort(1));
        Assert.assertEquals(1, entry.getExtraFields(false).length);
        Assert.assertNull(entry.getExtraField(new ZipShort(1)));

        // Remove unparseable
        entry.removeUnparseableExtraFieldData();
        Assert.assertNull(entry.getUnparseableExtraFieldData());
        Assert.assertEquals(1, entry.getExtraFields(true).length);
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeExtraField(new ZipShort(999));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNotFoundWithExistingFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(1));
        uef.setLocalFileDataData(new byte[0]);
        entry.addExtraField(uef);
        entry.removeExtraField(new ZipShort(999));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldDataWhenNone() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeUnparseableExtraFieldData();
    }

    @Test
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef1 = new UnrecognizedExtraField();
        uef1.setHeaderId(new ZipShort(1));
        uef1.setLocalFileDataData(new byte[]{1});

        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        uef2.setHeaderId(new ZipShort(2));
        uef2.setLocalFileDataData(new byte[]{2});

        // Add first to empty
        entry.addAsFirstExtraField(uef1);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertSame(uef1, entry.getExtraFields()[0]);

        // Add uef2 as first
        entry.addAsFirstExtraField(uef2);
        ZipExtraField[] fields = entry.getExtraFields();
        Assert.assertEquals(2, fields.length);
        Assert.assertSame(uef2, fields[0]);
        Assert.assertSame(uef1, fields[1]);

        // Replace existing uef1 as first
        UnrecognizedExtraField uef1Replacement = new UnrecognizedExtraField();
        uef1Replacement.setHeaderId(new ZipShort(1));
        uef1Replacement.setLocalFileDataData(new byte[]{100});
        entry.addAsFirstExtraField(uef1Replacement);

        fields = entry.getExtraFields();
        Assert.assertEquals(2, fields.length);
        Assert.assertSame(uef1Replacement, fields[0]);
        Assert.assertSame(uef2, fields[1]);

        // Add unparseable via addAsFirstExtraField
        UnparseableExtraFieldData unp = new UnparseableExtraFieldData();
        unp.parseFromLocalFileData(new byte[]{5}, 0, 1);
        entry.addAsFirstExtraField(unp);
        Assert.assertSame(unp, entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testSetExtraFieldsArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(10));
        uef.setLocalFileDataData(new byte[]{1, 2});

        UnparseableExtraFieldData unp = new UnparseableExtraFieldData();
        unp.parseFromLocalFileData(new byte[]{3, 4}, 0, 2);

        entry.setExtraFields(new ZipExtraField[]{uef, unp});
        Assert.assertEquals(1, entry.getExtraFields(false).length);
        Assert.assertEquals(2, entry.getExtraFields(true).length);
        Assert.assertSame(uef, entry.getExtraField(new ZipShort(10)));
        Assert.assertSame(unp, entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testSetExtraByteArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] extraData = new byte[] {
            1, 0, // Header ID 1
            2, 0, // Length 2
            0x11, 0x22
        };
        entry.setExtra(extraData);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertNotNull(entry.getExtraField(new ZipShort(1)));
        Assert.assertArrayEquals(extraData, entry.getLocalFileDataExtra());

        // Merge additional extra bytes
        byte[] extraData2 = new byte[] {
            2, 0, // Header ID 2
            1, 0, // Length 1
            0x33
        };
        entry.setExtra(extraData2);
        Assert.assertEquals(2, entry.getExtraFields().length);
        Assert.assertNotNull(entry.getExtraField(new ZipShort(1)));
        Assert.assertNotNull(entry.getExtraField(new ZipShort(2)));
    }

    @Test
    public void testSetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] centralExtra = new byte[] {
            5, 0, // Header ID 5
            2, 0, // Length 2
            0x44, 0x55
        };
        entry.setCentralDirectoryExtra(centralExtra);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertNotNull(entry.getExtraField(new ZipShort(5)));

        // Merge central directory extra
        byte[] centralExtra2 = new byte[] {
            5, 0, // Header ID 5 update
            2, 0, // Length 2
            0x66, 0x77
        };
        entry.setCentralDirectoryExtra(centralExtra2);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertArrayEquals(new byte[]{0x66, 0x77}, entry.getExtraField(new ZipShort(5)).getCentralDirectoryData());
    }

    @Test
    public void testMergeUnparseableExtraData() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unp1 = new UnparseableExtraFieldData();
        unp1.parseFromLocalFileData(new byte[]{1, 2}, 0, 2);
        entry.addExtraField(unp1);

        UnparseableExtraFieldData unp2 = new UnparseableExtraFieldData();
        unp2.parseFromLocalFileData(new byte[]{3, 4}, 0, 2);

        entry.setExtraFields(new ZipExtraField[]{unp2});
        Assert.assertSame(unp2, entry.getUnparseableExtraFieldData());
    }

    @Test
    public void testRawName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        Assert.assertNull(entry.getRawName());

        byte[] rawBytes = new byte[]{ 't', 'e', 's', 't', '.', 't', 'x', 't' };
        entry.setName("test.txt", rawBytes);
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertArrayEquals(rawBytes, entry.getRawName());

        // Modify returned array to verify defensive copy
        byte[] copy = entry.getRawName();
        copy[0] = 'x';
        Assert.assertEquals('t', entry.getRawName()[0]);
    }

    @Test
    public void testGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        gpb.useEncryption(true);
        entry.setGeneralPurposeBit(gpb);
        Assert.assertSame(gpb, entry.getGeneralPurposeBit());
        Assert.assertTrue(entry.getGeneralPurposeBit().usesUTF8ForNames());
        Assert.assertTrue(entry.getGeneralPurposeBit().usesEncryption());
    }

    @Test
    public void testLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        long time = 1500000000000L;
        entry.setTime(time);
        Assert.assertEquals(new Date(time), entry.getLastModifiedDate());
    }

    @Test
    public void testHashCode() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("entry1");
        ZipArchiveEntry e2 = new ZipArchiveEntry("entry1");
        ZipArchiveEntry e3 = new ZipArchiveEntry("entry2");

        Assert.assertEquals(e1.hashCode(), e2.hashCode());
        Assert.assertNotEquals(e1.hashCode(), e3.hashCode());
    }

    @Test
    public void testEqualsContract() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");

        // Reflexive
        Assert.assertTrue(e1.equals(e1));

        // Symmetric
        Assert.assertTrue(e1.equals(e2));
        Assert.assertTrue(e2.equals(e1));

        // Null check
        Assert.assertFalse(e1.equals(null));

        // Different class
        Assert.assertFalse(e1.equals("test"));

        // Different name
        ZipArchiveEntry diffName = new ZipArchiveEntry("other");
        Assert.assertFalse(e1.equals(diffName));

        // Name null handling
        ZipArchiveEntry nullName1 = new ZipArchiveEntry();
        nullName1.setName(null);
        ZipArchiveEntry nullName2 = new ZipArchiveEntry();
        nullName2.setName(null);
        Assert.assertTrue(nullName1.equals(nullName2));
        Assert.assertFalse(nullName1.equals(e1));
        Assert.assertFalse(e1.equals(nullName1));

        // Comment check
        e1.setComment("comment");
        Assert.assertFalse(e1.equals(e2));
        Assert.assertFalse(e2.equals(e1));
        e2.setComment("comment");
        Assert.assertTrue(e1.equals(e2));
        e2.setComment("other comment");
        Assert.assertFalse(e1.equals(e2));
        e2.setComment(null);
        Assert.assertFalse(e1.equals(e2));
        e1.setComment(null);

        // Time check
        e1.setTime(100L);
        e2.setTime(200L);
        Assert.assertFalse(e1.equals(e2));
        e2.setTime(100L);
        Assert.assertTrue(e1.equals(e2));

        // Internal attributes check
        e1.setInternalAttributes(1);
        Assert.assertFalse(e1.equals(e2));
        e2.setInternalAttributes(1);
        Assert.assertTrue(e1.equals(e2));

        // Platform check
        e1.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        Assert.assertFalse(e1.equals(e2));
        e2.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        Assert.assertTrue(e1.equals(e2));

        // External attributes check
        e1.setExternalAttributes(50L);
        Assert.assertFalse(e1.equals(e2));
        e2.setExternalAttributes(50L);
        Assert.assertTrue(e1.equals(e2));

        // Method check
        e1.setMethod(ZipEntry.DEFLATED);
        Assert.assertFalse(e1.equals(e2));
        e2.setMethod(ZipEntry.DEFLATED);
        Assert.assertTrue(e1.equals(e2));

        // Size check
        e1.setSize(1234L);
        Assert.assertFalse(e1.equals(e2));
        e2.setSize(1234L);
        Assert.assertTrue(e1.equals(e2));

        // CRC check
        e1.setCrc(999L);
        Assert.assertFalse(e1.equals(e2));
        e2.setCrc(999L);
        Assert.assertTrue(e1.equals(e2));

        // Compressed size check
        e1.setCompressedSize(555L);
        Assert.assertFalse(e1.equals(e2));
        e2.setCompressedSize(555L);
        Assert.assertTrue(e1.equals(e2));

        // Central directory extra check
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(1));
        uef.setCentralDirectoryData(new byte[]{1});
        uef.setLocalFileDataData(new byte[]{1});
        e1.addExtraField(uef);
        Assert.assertFalse(e1.equals(e2));
        e2.addExtraField(uef);
        Assert.assertTrue(e1.equals(e2));

        // GPB check
        GeneralPurposeBit gpb1 = new GeneralPurposeBit();
        gpb1.useUTF8ForNames(true);
        e1.setGeneralPurposeBit(gpb1);
        Assert.assertFalse(e1.equals(e2));
        GeneralPurposeBit gpb2 = new GeneralPurposeBit();
        gpb2.useUTF8ForNames(true);
        e2.setGeneralPurposeBit(gpb2);
        Assert.assertTrue(e1.equals(e2));
    }

    @Test
    public void testGetLocalFileDataExtraReturnsEmptyArrayWhenNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test") {
            @Override
            public byte[] getExtra() {
                return null;
            }
        };
        Assert.assertEquals(0, entry.getLocalFileDataExtra().length);
    }
}
