package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class ZipArchiveEntryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testDefaultConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        Assert.assertEquals("", entry.getName());
        Assert.assertEquals(ZipMethod.UNKNOWN_CODE, entry.getMethod());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        Assert.assertEquals(0, entry.getInternalAttributes());
        Assert.assertEquals(0, entry.getExternalAttributes());
        Assert.assertEquals(0, entry.getExtraFields().length);
        Assert.assertNull(entry.getRawName());
        Assert.assertFalse(entry.isDirectory());
    }

    @Test
    public void testStringConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertFalse(entry.isDirectory());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("dir/");
        Assert.assertTrue(dirEntry.isDirectory());

        ZipArchiveEntry backslashEntry = new ZipArchiveEntry("folder\\file.txt");
        Assert.assertEquals("folder/file.txt", backslashEntry.getName());
    }

    @Test
    public void testZipEntryConstructor() throws Exception {
        java.util.zip.ZipEntry base = new java.util.zip.ZipEntry("foo.txt");
        base.setComment("comment");
        base.setMethod(ZipEntry.DEFLATED);
        base.setSize(1234L);
        base.setTime(50000L);

        ZipArchiveEntry entry = new ZipArchiveEntry(base);
        Assert.assertEquals("foo.txt", entry.getName());
        Assert.assertEquals(ZipEntry.DEFLATED, entry.getMethod());
        Assert.assertEquals(1234L, entry.getSize());
        Assert.assertEquals(50000L, entry.getTime());

        UnicodePathExtraField upef = new UnicodePathExtraField("foo.txt", "foo.txt".getBytes(), 0, 8);
        base.setExtra(upef.getLocalFileDataData());
        ZipArchiveEntry entryWithExtra = new ZipArchiveEntry(base);
        Assert.assertTrue(entryWithExtra.getExtraFields().length > 0);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        ZipArchiveEntry src = new ZipArchiveEntry("foo.txt");
        src.setInternalAttributes(2);
        src.setExternalAttributes(42L);
        src.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        src.setMethod(ZipEntry.STORED);
        src.setSize(500L);
        src.setVersionMadeBy(20);
        src.setVersionRequired(10);
        src.setRawFlag(8);

        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useEncryption(true);
        src.setGeneralPurposeBit(gpb);

        ZipArchiveEntry copy = new ZipArchiveEntry(src);
        Assert.assertEquals("foo.txt", copy.getName());
        Assert.assertEquals(2, copy.getInternalAttributes());
        Assert.assertEquals(42L, copy.getExternalAttributes());
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, copy.getPlatform());
        Assert.assertEquals(ZipEntry.STORED, copy.getMethod());
        Assert.assertEquals(500L, copy.getSize());
        Assert.assertTrue(copy.getGeneralPurposeBit().usesEncryption());

        src.setGeneralPurposeBit(null);
        ZipArchiveEntry copy2 = new ZipArchiveEntry(src);
        Assert.assertNull(copy2.getGeneralPurposeBit());
    }

    @Test
    public void testFileConstructor() throws Exception {
        File file = folder.newFile("testFile.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[]{1, 2, 3, 4});
        }
        file.setLastModified(100000L);

        ZipArchiveEntry fileEntry = new ZipArchiveEntry(file, "customName.txt");
        Assert.assertEquals("customName.txt", fileEntry.getName());
        Assert.assertEquals(4L, fileEntry.getSize());
        Assert.assertEquals(file.lastModified(), fileEntry.getTime());
        Assert.assertFalse(fileEntry.isDirectory());

        File dir = folder.newFolder("testDir");
        ZipArchiveEntry dirEntry = new ZipArchiveEntry(dir, "customDir");
        Assert.assertEquals("customDir/", dirEntry.getName());
        Assert.assertTrue(dirEntry.isDirectory());
    }

    @Test
    public void testClone() {
        ZipArchiveEntry entry = new ZipArchiveEntry("clone.txt");
        entry.setInternalAttributes(10);
        entry.setExternalAttributes(20L);
        entry.setUnixMode(0755);

        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        Assert.assertEquals(entry.getName(), cloned.getName());
        Assert.assertEquals(entry.getInternalAttributes(), cloned.getInternalAttributes());
        Assert.assertEquals(entry.getExternalAttributes(), cloned.getExternalAttributes());
        Assert.assertEquals(entry.getPlatform(), cloned.getPlatform());
        Assert.assertEquals(entry.getUnixMode(), cloned.getUnixMode());
    }

    @Test
    public void testMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("m.txt");
        entry.setMethod(ZipEntry.STORED);
        Assert.assertEquals(ZipEntry.STORED, entry.getMethod());
        entry.setMethod(ZipMethod.BZIP2.getCode());
        Assert.assertEquals(ZipMethod.BZIP2.getCode(), entry.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("m.txt");
        entry.setMethod(-2);
    }

    @Test
    public void testSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("s.txt");
        entry.setSize(1024L);
        Assert.assertEquals(1024L, entry.getSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("s.txt");
        entry.setSize(-1L);
    }

    @Test
    public void testUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("unix.txt");
        entry.setUnixMode(0755);
        Assert.assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        Assert.assertEquals(0755, entry.getUnixMode());
        Assert.assertFalse(entry.isUnixSymlink());

        ZipArchiveEntry symlink = new ZipArchiveEntry("link.txt");
        symlink.setUnixMode(UnixStat.LINK_FLAG | 0777);
        Assert.assertTrue(symlink.isUnixSymlink());

        ZipArchiveEntry fatEntry = new ZipArchiveEntry("fat.txt");
        fatEntry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        fatEntry.setExternalAttributes(0755 << 16);
        Assert.assertEquals(0, fatEntry.getUnixMode());
    }

    @Test
    public void testExtraFieldsHandling() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extra.txt");
        Assert.assertEquals(0, entry.getExtraFields().length);
        Assert.assertEquals(0, entry.getExtraFields(true).length);
        Assert.assertEquals(0, entry.getExtraFields(false).length);

        AsiExtraField asi1 = new AsiExtraField();
        asi1.setMode(0755);
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[]{1, 2, 3, 4}, 0, 4);

        entry.setExtraFields(new ZipExtraField[]{asi1, unparseable});
        Assert.assertEquals(1, entry.getExtraFields(false).length);
        Assert.assertEquals(2, entry.getExtraFields(true).length);
        Assert.assertSame(unparseable, entry.getUnparseableExtraFieldData());
        Assert.assertSame(asi1, entry.getExtraField(asi1.getHeaderId()));
        Assert.assertNull(entry.getExtraField(new ZipShort(0x9999)));

        AsiExtraField asi2 = new AsiExtraField();
        asi2.setMode(0644);
        entry.addExtraField(asi2);
        Assert.assertEquals(1, entry.getExtraFields(false).length);
        Assert.assertEquals(0644, ((AsiExtraField) entry.getExtraField(asi1.getHeaderId())).getMode());

        X5455_ExtendedTimestamp timestamp = new X5455_ExtendedTimestamp();
        entry.addAsFirstExtraField(timestamp);
        Assert.assertEquals(2, entry.getExtraFields(false).length);
        Assert.assertEquals(timestamp.getHeaderId(), entry.getExtraFields(false)[0].getHeaderId());

        entry.addAsFirstExtraField(asi2);
        Assert.assertEquals(asi2.getHeaderId(), entry.getExtraFields(false)[0].getHeaderId());

        UnparseableExtraFieldData unparseable2 = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable2);
        Assert.assertSame(unparseable2, entry.getUnparseableExtraFieldData());

        entry.addAsFirstExtraField(unparseable);
        Assert.assertSame(unparseable, entry.getUnparseableExtraFieldData());

        entry.removeExtraField(asi2.getHeaderId());
        Assert.assertNull(entry.getExtraField(asi2.getHeaderId()));

        entry.removeUnparseableExtraFieldData();
        Assert.assertNull(entry.getUnparseableExtraFieldData());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.removeExtraField(new ZipShort(1));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNotFoundWhenNotEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.addExtraField(new AsiExtraField());
        entry.removeExtraField(new ZipShort(0x1234));
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldDataNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.removeUnparseableExtraFieldData();
    }

    @Test
    public void testParseExtraDataAndMerge() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] extra = new byte[]{1, 0, 1, 0, 42};
        entry.setExtra(extra);
        Assert.assertNotNull(entry.getUnparseableExtraFieldData());
        Assert.assertTrue(entry.getLocalFileDataExtra().length > 0);

        byte[] extraCentral = new byte[]{2, 0, 1, 0, 84};
        entry.setCentralDirectoryExtra(extraCentral);
        Assert.assertTrue(entry.getCentralDirectoryExtra().length > 0);

        AsiExtraField asi = new AsiExtraField();
        asi.setMode(0755);
        byte[] asiData = asi.getLocalFileDataData();
        byte[] fullExtra = new byte[4 + asiData.length];
        System.arraycopy(asi.getHeaderId().getBytes(), 0, fullExtra, 0, 2);
        System.arraycopy(new ZipShort(asiData.length).getBytes(), 0, fullExtra, 2, 2);
        System.arraycopy(asiData, 0, fullExtra, 4, asiData.length);

        ZipArchiveEntry entry2 = new ZipArchiveEntry("merge.txt");
        entry2.addExtraField(asi);
        entry2.setExtra(fullExtra);
        entry2.setCentralDirectoryExtra(fullExtra);
        Assert.assertNotNull(entry2.getExtraField(asi.getHeaderId()));
    }

    @Test
    public void testRawNameAndGuess() {
        ZipArchiveEntry entry = new ZipArchiveEntry("foo");
        Assert.assertNull(entry.getRawName());
        byte[] raw = new byte[]{102, 111, 111};
        entry.setName("foo", raw);
        Assert.assertEquals("foo", entry.getName());
        Assert.assertArrayEquals(raw, entry.getRawName());
    }

    @Test
    public void testLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("date.txt");
        long time = 1600000000000L;
        entry.setTime(time);
        Assert.assertEquals(new Date(time), entry.getLastModifiedDate());
    }

    @Test
    public void testEqualsAndHashCode() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test.txt");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test.txt");
        Assert.assertEquals(e1, e1);
        Assert.assertEquals(e1, e2);
        Assert.assertEquals(e1.hashCode(), e2.hashCode());

        Assert.assertFalse(e1.equals(null));
        Assert.assertFalse(e1.equals(new Object()));

        ZipArchiveEntry e3 = new ZipArchiveEntry("other.txt");
        Assert.assertFalse(e1.equals(e3));

        e2.setComment("comment");
        Assert.assertFalse(e1.equals(e2));
        e1.setComment("comment");
        Assert.assertEquals(e1, e2);

        e2.setTime(1000L);
        Assert.assertFalse(e1.equals(e2));
        e1.setTime(1000L);
        Assert.assertEquals(e1, e2);

        e2.setInternalAttributes(5);
        Assert.assertFalse(e1.equals(e2));
        e1.setInternalAttributes(5);
        Assert.assertEquals(e1, e2);

        e2.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        Assert.assertFalse(e1.equals(e2));
        e1.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        Assert.assertEquals(e1, e2);

        e2.setExternalAttributes(50L);
        Assert.assertFalse(e1.equals(e2));
        e1.setExternalAttributes(50L);
        Assert.assertEquals(e1, e2);

        e2.setMethod(ZipEntry.STORED);
        Assert.assertFalse(e1.equals(e2));
        e1.setMethod(ZipEntry.STORED);
        Assert.assertEquals(e1, e2);

        e2.setSize(100L);
        Assert.assertFalse(e1.equals(e2));
        e1.setSize(100L);
        Assert.assertEquals(e1, e2);

        e2.setCrc(999L);
        Assert.assertFalse(e1.equals(e2));
        e1.setCrc(999L);
        Assert.assertEquals(e1, e2);

        e2.setCompressedSize(50L);
        Assert.assertFalse(e1.equals(e2));
        e1.setCompressedSize(50L);
        Assert.assertEquals(e1, e2);

        GeneralPurposeBit gpb1 = new GeneralPurposeBit();
        gpb1.useUTF8ForNames(true);
        e2.setGeneralPurposeBit(gpb1);
        Assert.assertFalse(e1.equals(e2));
        e1.setGeneralPurposeBit(gpb1);
        Assert.assertEquals(e1, e2);
    }

    @Test
    public void testVersionAndFlagFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("version.txt");
        entry.setVersionMadeBy(45);
        Assert.assertEquals(45, entry.getVersionMadeBy());

        entry.setVersionRequired(20);
        Assert.assertEquals(20, entry.getVersionRequired());

        entry.setRawFlag(2);
        Assert.assertEquals(2, entry.getRawFlag());
    }
}
