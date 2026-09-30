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
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testStringConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertEquals(-1, entry.getMethod());
        Assert.assertEquals(0, entry.getInternalAttributes());
        Assert.assertEquals(0, entry.getExternalAttributes());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        Assert.assertFalse(entry.isDirectory());
        Assert.assertEquals(0, entry.getExtraFields().length);
    }

    @Test
    public void testProtectedDefaultConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry() {};
        Assert.assertEquals("", entry.getName());
        Assert.assertEquals(-1, entry.getMethod());
    }

    @Test
    public void testJavaZipEntryConstructorWithoutExtra() throws Exception {
        ZipEntry javaEntry = new ZipEntry("foo.txt");
        javaEntry.setMethod(ZipEntry.DEFLATED);
        javaEntry.setSize(1234L);
        javaEntry.setTime(500000L);

        ZipArchiveEntry entry = new ZipArchiveEntry(javaEntry);
        Assert.assertEquals("foo.txt", entry.getName());
        Assert.assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        Assert.assertEquals(1234L, entry.getSize());
        Assert.assertEquals(500000L, entry.getTime());
        Assert.assertEquals(0, entry.getExtraFields().length);
        Assert.assertEquals(0, entry.getLocalFileDataExtra().length);
    }

    @Test
    public void testJavaZipEntryConstructorWithExtra() throws Exception {
        ZipEntry javaEntry = new ZipEntry("bar.txt");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(1));
        uef.setLocalFileDataData(new byte[] { 1, 2, 3 });
        javaEntry.setExtra(uef.getHeaderId().getBytes());

        byte[] extraData = ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] { uef });
        javaEntry.setExtra(extraData);

        ZipArchiveEntry entry = new ZipArchiveEntry(javaEntry);
        Assert.assertEquals("bar.txt", entry.getName());
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertNotNull(entry.getExtraField(new ZipShort(1)));
    }

    @Test
    public void testCopyConstructor() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("original.txt");
        original.setMethod(ZipEntry.STORED);
        original.setInternalAttributes(42);
        original.setExternalAttributes(84L);
        original.setUnixMode(0644);

        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(2));
        uef.setLocalFileDataData(new byte[] { 4, 5 });
        original.addExtraField(uef);

        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        Assert.assertEquals("original.txt", copy.getName());
        Assert.assertEquals(ZipEntry.STORED, copy.getMethod());
        Assert.assertEquals(42, copy.getInternalAttributes());
        Assert.assertEquals(original.getExternalAttributes(), copy.getExternalAttributes());
        Assert.assertEquals(1, copy.getExtraFields().length);
        Assert.assertNotNull(copy.getExtraField(new ZipShort(2)));
    }

    @Test
    public void testFileConstructorForDirectory() throws IOException {
        File dir = temporaryFolder.newFolder("testDir");
        ZipArchiveEntry entry = new ZipArchiveEntry(dir, "myDir");
        Assert.assertEquals("myDir/", entry.getName());
        Assert.assertTrue(entry.isDirectory());

        ZipArchiveEntry entryWithSlash = new ZipArchiveEntry(dir, "myDir/");
        Assert.assertEquals("myDir/", entryWithSlash.getName());
        Assert.assertTrue(entryWithSlash.isDirectory());
    }

    @Test
    public void testFileConstructorForFile() throws IOException {
        File file = temporaryFolder.newFile("sample.txt");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(new byte[] { 1, 2, 3, 4, 5 });
        fos.close();

        ZipArchiveEntry entry = new ZipArchiveEntry(file, "sample.txt");
        Assert.assertEquals("sample.txt", entry.getName());
        Assert.assertEquals(5L, entry.getSize());
        Assert.assertEquals(file.lastModified(), entry.getTime());
        Assert.assertFalse(entry.isDirectory());
    }

    @Test
    public void testClone() {
        ZipArchiveEntry entry = new ZipArchiveEntry("cloneTarget.txt");
        entry.setInternalAttributes(10);
        entry.setExternalAttributes(20L);
        entry.setMethod(ZipEntry.DEFLATED);

        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(100));
        uef.setLocalFileDataData(new byte[] { 10 });
        entry.addExtraField(uef);

        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        Assert.assertNotSame(entry, cloned);
        Assert.assertEquals(entry.getName(), cloned.getName());
        Assert.assertEquals(entry.getInternalAttributes(), cloned.getInternalAttributes());
        Assert.assertEquals(entry.getExternalAttributes(), cloned.getExternalAttributes());
        Assert.assertEquals(entry.getMethod(), cloned.getMethod());
        Assert.assertEquals(1, cloned.getExtraFields().length);
        Assert.assertNotNull(cloned.getExtraField(new ZipShort(100)));

        ZipArchiveEntry emptyExtra = new ZipArchiveEntry("noExtra.txt");
        ZipArchiveEntry emptyCloned = (ZipArchiveEntry) emptyExtra.clone();
        Assert.assertEquals(0, emptyCloned.getExtraFields().length);
    }

    @Test
    public void testCompressionMethodSupport() {
        ZipArchiveEntry entry = new ZipArchiveEntry("method.txt");
        Assert.assertFalse(entry.isSupportedCompressionMethod());

        entry.setMethod(ZipEntry.STORED);
        Assert.assertTrue(entry.isSupportedCompressionMethod());
        Assert.assertEquals(ZipEntry.STORED, entry.getMethod());

        entry.setMethod(ZipEntry.DEFLATED);
        Assert.assertTrue(entry.isSupportedCompressionMethod());
        Assert.assertEquals(ZipEntry.DEFLATED, entry.getMethod());

        entry.setMethod(12);
        Assert.assertFalse(entry.isSupportedCompressionMethod());
        Assert.assertEquals(12, entry.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMethodThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("invalidMethod.txt");
        entry.setMethod(-2);
    }

    @Test
    public void testInternalAndExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("attrs.txt");
        entry.setInternalAttributes(123);
        Assert.assertEquals(123, entry.getInternalAttributes());

        entry.setExternalAttributes(0x12345678L);
        Assert.assertEquals(0x12345678L, entry.getExternalAttributes());
    }

    @Test
    public void testUnixModeAndPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("unix.txt");
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        Assert.assertEquals(0, entry.getUnixMode());

        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());

        entry.setUnixMode(0644);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        Assert.assertEquals(0644, entry.getUnixMode());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("unixDir/");
        dirEntry.setUnixMode(0755);
        Assert.assertEquals(0755, dirEntry.getUnixMode());
        Assert.assertTrue((dirEntry.getExternalAttributes() & 0x10) != 0);

        ZipArchiveEntry readOnlyEntry = new ZipArchiveEntry("readOnly.txt");
        readOnlyEntry.setUnixMode(0444);
        Assert.assertEquals(0444, readOnlyEntry.getUnixMode());
        Assert.assertTrue((readOnlyEntry.getExternalAttributes() & 1) != 0);

        readOnlyEntry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        Assert.assertEquals(0, readOnlyEntry.getUnixMode());
    }

    @Test
    public void testSetExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extra.txt");
        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(new ZipShort(1));
        f1.setLocalFileDataData(new byte[] { 1 });
        f1.setCentralDirectoryData(new byte[] { 1, 1 });

        UnrecognizedExtraField f2 = new UnrecognizedExtraField();
        f2.setHeaderId(new ZipShort(2));
        f2.setLocalFileDataData(new byte[] { 2 });
        f2.setCentralDirectoryData(new byte[] { 2, 2 });

        entry.setExtraFields(new ZipExtraField[] { f1, f2 });
        ZipExtraField[] fields = entry.getExtraFields();
        Assert.assertEquals(2, fields.length);
        Assert.assertEquals(f1, entry.getExtraField(new ZipShort(1)));
        Assert.assertEquals(f2, entry.getExtraField(new ZipShort(2)));
        Assert.assertNull(entry.getExtraField(new ZipShort(999)));

        byte[] localExtra = entry.getLocalFileDataExtra();
        Assert.assertTrue(localExtra.length > 0);

        byte[] cdExtra = entry.getCentralDirectoryExtra();
        Assert.assertTrue(cdExtra.length > 0);
    }

    @Test
    public void testAddExtraFieldAndReplace() {
        ZipArchiveEntry entry = new ZipArchiveEntry("addExtra.txt");
        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(new ZipShort(1));
        f1.setLocalFileDataData(new byte[] { 1 });

        entry.addExtraField(f1);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertArrayEquals(new byte[] { 1 }, entry.getExtraField(new ZipShort(1)).getLocalFileDataData());

        UnrecognizedExtraField f1Replacement = new UnrecognizedExtraField();
        f1Replacement.setHeaderId(new ZipShort(1));
        f1Replacement.setLocalFileDataData(new byte[] { 99 });

        entry.addExtraField(f1Replacement);
        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertArrayEquals(new byte[] { 99 }, entry.getExtraField(new ZipShort(1)).getLocalFileDataData());
    }

    @Test
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("order.txt");

        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(new ZipShort(1));
        entry.addAsFirstExtraField(f1);

        UnrecognizedExtraField f2 = new UnrecognizedExtraField();
        f2.setHeaderId(new ZipShort(2));
        entry.addAsFirstExtraField(f2);

        ZipExtraField[] fields = entry.getExtraFields();
        Assert.assertEquals(2, fields.length);
        Assert.assertEquals(new ZipShort(2), fields[0].getHeaderId());
        Assert.assertEquals(new ZipShort(1), fields[1].getHeaderId());

        UnrecognizedExtraField f1Updated = new UnrecognizedExtraField();
        f1Updated.setHeaderId(new ZipShort(1));
        entry.addAsFirstExtraField(f1Updated);

        fields = entry.getExtraFields();
        Assert.assertEquals(2, fields.length);
        Assert.assertEquals(new ZipShort(1), fields[0].getHeaderId());
        Assert.assertEquals(new ZipShort(2), fields[1].getHeaderId());
    }

    @Test
    public void testRemoveExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("remove.txt");
        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(new ZipShort(1));
        entry.addExtraField(f1);

        entry.removeExtraField(new ZipShort(1));
        Assert.assertEquals(0, entry.getExtraFields().length);
        Assert.assertNull(entry.getExtraField(new ZipShort(1)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldWhenEmptyThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("emptyRemove.txt");
        entry.removeExtraField(new ZipShort(1));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldWhenNotFoundThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("notFoundRemove.txt");
        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(new ZipShort(1));
        entry.addExtraField(f1);
        entry.removeExtraField(new ZipShort(2));
    }

    @Test
    public void testSetExtraByteArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("bytesExtra.txt");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(10));
        uef.setLocalFileDataData(new byte[] { 1, 2, 3 });

        byte[] extra = ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] { uef });
        entry.setExtra(extra);

        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertNotNull(entry.getExtraField(new ZipShort(10)));

        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        uef2.setHeaderId(new ZipShort(10));
        uef2.setLocalFileDataData(new byte[] { 9, 8, 7 });
        byte[] extra2 = ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] { uef2 });
        entry.setExtra(extra2);

        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertArrayEquals(new byte[] { 9, 8, 7 }, entry.getExtraField(new ZipShort(10)).getLocalFileDataData());

        UnrecognizedExtraField uef3 = new UnrecognizedExtraField();
        uef3.setHeaderId(new ZipShort(20));
        uef3.setLocalFileDataData(new byte[] { 4 });
        byte[] extra3 = ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] { uef3 });
        entry.setExtra(extra3);

        Assert.assertEquals(2, entry.getExtraFields().length);
    }

    @Test(expected = RuntimeException.class)
    public void testSetExtraInvalidByteArrayThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("invalidBytes.txt");
        entry.setExtra(new byte[] { 0, 1, 2 });
    }

    @Test
    public void testSetCentralDirectoryExtraByteArray() {
        ZipArchiveEntry entry = new ZipArchiveEntry("cdExtra.txt");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(11));
        uef.setCentralDirectoryData(new byte[] { 5, 6 });

        byte[] extra = ExtraFieldUtils.mergeCentralDirectoryData(new ZipExtraField[] { uef });
        entry.setCentralDirectoryExtra(extra);

        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertNotNull(entry.getExtraField(new ZipShort(11)));

        UnrecognizedExtraField uefUpdate = new UnrecognizedExtraField();
        uefUpdate.setHeaderId(new ZipShort(11));
        uefUpdate.setCentralDirectoryData(new byte[] { 7, 8 });
        byte[] extraUpdate = ExtraFieldUtils.mergeCentralDirectoryData(new ZipExtraField[] { uefUpdate });
        entry.setCentralDirectoryExtra(extraUpdate);

        Assert.assertEquals(1, entry.getExtraFields().length);
        Assert.assertArrayEquals(new byte[] { 7, 8 }, entry.getExtraField(new ZipShort(11)).getCentralDirectoryData());
    }

    @Test(expected = RuntimeException.class)
    public void testSetCentralDirectoryExtraInvalidBytesThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("invalidCd.txt");
        entry.setCentralDirectoryExtra(new byte[] { 0, 1, 2 });
    }

    @Test
    public void testNameAndDirectoryHandling() {
        ZipArchiveEntry entry = new ZipArchiveEntry("folder/");
        Assert.assertTrue(entry.isDirectory());
        Assert.assertEquals("folder/", entry.getName());

        entry.setName("file.txt");
        Assert.assertFalse(entry.isDirectory());
        Assert.assertEquals("file.txt", entry.getName());
    }

    @Test
    public void testHashCode() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test");
        Assert.assertEquals(entry1.hashCode(), entry2.hashCode());
        Assert.assertEquals("test".hashCode(), entry1.hashCode());
    }

    @Test
    public void testEquals() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("fileA.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("fileA.txt");
        ZipArchiveEntry entry3 = new ZipArchiveEntry("fileB.txt");

        Assert.assertTrue(entry1.equals(entry1));
        Assert.assertTrue(entry1.equals(entry2));
        Assert.assertTrue(entry2.equals(entry1));
        Assert.assertFalse(entry1.equals(entry3));
        Assert.assertFalse(entry1.equals(null));
        Assert.assertFalse(entry1.equals("Not a ZipArchiveEntry"));

        ZipArchiveEntry nullName1 = new ZipArchiveEntry("dummy");
        nullName1.setName(null);
        ZipArchiveEntry nullName2 = new ZipArchiveEntry("dummy");
        nullName2.setName(null);
        ZipArchiveEntry nonNullName = new ZipArchiveEntry("dummy");

        Assert.assertTrue(nullName1.equals(nullName2));
        Assert.assertFalse(nullName1.equals(nonNullName));
        Assert.assertFalse(nonNullName.equals(nullName1));
    }

    @Test
    public void testGetLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("time.txt");
        long time = 123456789000L;
        entry.setTime(time);
        Assert.assertEquals(new Date(time), entry.getLastModifiedDate());
    }
}
