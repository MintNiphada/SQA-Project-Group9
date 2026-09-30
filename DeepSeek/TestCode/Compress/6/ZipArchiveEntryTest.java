package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.junit.Test;

public class ZipArchiveEntryTest {

    @Test
    public void testConstructorString() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertEquals(-1, entry.getMethod());
    }

    @Test
    public void testConstructorZipEntry() throws Exception {
        ZipEntry ze = new ZipEntry("source.zip");
        ze.setMethod(ZipEntry.DEFLATED);
        ze.setExtra(new byte[] { 0, 0 });
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("source.zip", entry.getName());
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    @Test
    public void testConstructorZipEntryNullExtra() throws Exception {
        ZipEntry ze = new ZipEntry("noextra.zip");
        ze.setMethod(ZipEntry.STORED);
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("noextra.zip", entry.getName());
        assertEquals(ZipEntry.STORED, entry.getMethod());
    }

    @Test
    public void testConstructorZipArchiveEntry() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("original.txt");
        original.setMethod(ZipEntry.DEFLATED);
        original.setInternalAttributes(1);
        original.setExternalAttributes(2L);
        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals("original.txt", copy.getName());
        assertEquals(ZipEntry.DEFLATED, copy.getMethod());
        assertEquals(1, copy.getInternalAttributes());
        assertEquals(2L, copy.getExternalAttributes());
    }

    @Test
    public void testConstructorFileDirectory() {
        File dir = new File("testDir");
        dir.mkdir();
        try {
            ZipArchiveEntry entry = new ZipArchiveEntry(dir, "mydir");
            assertTrue(entry.getName().endsWith("/"));
            assertTrue(entry.isDirectory());
        } finally {
            dir.delete();
        }
    }

    @Test
    public void testConstructorFileDirectoryWithSlash() {
        File dir = new File("testDir2");
        dir.mkdir();
        try {
            ZipArchiveEntry entry = new ZipArchiveEntry(dir, "mydir/");
            assertEquals("mydir/", entry.getName());
        } finally {
            dir.delete();
        }
    }

    @Test
    public void testConstructorFileRegular() {
        File file = new File("testFile.tmp");
        try {
            file.createNewFile();
            ZipArchiveEntry entry = new ZipArchiveEntry(file, "file.txt");
            assertEquals("file.txt", entry.getName());
            assertEquals(file.length(), entry.getSize());
            assertEquals(file.lastModified(), entry.getTime());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testClone() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("clone.txt");
        original.setMethod(ZipEntry.DEFLATED);
        original.setInternalAttributes(5);
        original.setExternalAttributes(10L);
        ZipArchiveEntry cloned = (ZipArchiveEntry) original.clone();
        assertEquals(original.getName(), cloned.getName());
        assertEquals(original.getMethod(), cloned.getMethod());
        assertEquals(original.getInternalAttributes(), cloned.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), cloned.getExternalAttributes());
        assertNotSame(original, cloned);
    }

    @Test
    public void testIsSupportedCompressionMethodStored() {
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipEntry.STORED);
        assertTrue(entry.isSupportedCompressionMethod());
    }

    @Test
    public void testIsSupportedCompressionMethodDeflated() {
        ZipArchiveEntry entry = new ZipArchiveEntry("deflated.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        assertTrue(entry.isSupportedCompressionMethod());
    }

    @Test
    public void testIsSupportedCompressionMethodUnsupported() {
        ZipArchiveEntry entry = new ZipArchiveEntry("unsupported.txt");
        entry.setMethod(999);
        assertFalse(entry.isSupportedCompressionMethod());
    }

    @Test
    public void testGetMethodDefault() {
        ZipArchiveEntry entry = new ZipArchiveEntry("default.txt");
        assertEquals(-1, entry.getMethod());
    }

    @Test
    public void testSetMethodValid() {
        ZipArchiveEntry entry = new ZipArchiveEntry("valid.txt");
        entry.setMethod(8);
        assertEquals(8, entry.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMethodNegative() {
        ZipArchiveEntry entry = new ZipArchiveEntry("negative.txt");
        entry.setMethod(-5);
    }

    @Test
    public void testSetMethodZero() {
        ZipArchiveEntry entry = new ZipArchiveEntry("zero.txt");
        entry.setMethod(0);
        assertEquals(0, entry.getMethod());
    }

    @Test
    public void testInternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("internal.txt");
        assertEquals(0, entry.getInternalAttributes());
        entry.setInternalAttributes(123);
        assertEquals(123, entry.getInternalAttributes());
    }

    @Test
    public void testExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("external.txt");
        assertEquals(0L, entry.getExternalAttributes());
        entry.setExternalAttributes(456L);
        assertEquals(456L, entry.getExternalAttributes());
    }

    @Test
    public void testSetUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("unix.txt");
        entry.setUnixMode(0755);
        assertEquals(3, entry.getPlatform());
        assertEquals(0755, entry.getUnixMode());
    }

    @Test
    public void testSetUnixModeDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("unixdir/");
        entry.setUnixMode(0644);
        assertEquals(3, entry.getPlatform());
        assertEquals(0644, entry.getUnixMode());
    }

    @Test
    public void testGetUnixModeNonUnix() {
        ZipArchiveEntry entry = new ZipArchiveEntry("fat.txt");
        assertEquals(0, entry.getPlatform());
        assertEquals(0, entry.getUnixMode());
    }

    @Test
    public void testGetPlatformDefault() {
        ZipArchiveEntry entry = new ZipArchiveEntry("platform.txt");
        assertEquals(0, entry.getPlatform());
    }

    @Test
    public void testSetPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("setplat.txt");
        entry.setPlatform(3);
        assertEquals(3, entry.getPlatform());
    }

    @Test
    public void testSetExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extra.txt");
        ZipExtraField field = new AsiExtraField();
        entry.setExtraFields(new ZipExtraField[] { field });
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testGetExtraFieldsEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("emptyextra.txt");
        assertEquals(0, entry.getExtraFields().length);
    }

    @Test
    public void testAddExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("add.txt");
        ZipExtraField field = new AsiExtraField();
        entry.addExtraField(field);
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testAddExtraFieldReplace() {
        ZipArchiveEntry entry = new ZipArchiveEntry("replace.txt");
        AsiExtraField field1 = new AsiExtraField();
        AsiExtraField field2 = new AsiExtraField();
        entry.addExtraField(field1);
        entry.addExtraField(field2);
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("first.txt");
        ZipExtraField field1 = new AsiExtraField();
        ZipExtraField field2 = new UnrecognizedExtraField();
        entry.addExtraField(field2);
        entry.addAsFirstExtraField(field1);
        assertEquals(2, entry.getExtraFields().length);
        assertEquals(field1, entry.getExtraFields()[0]);
    }

    @Test
    public void testAddAsFirstExtraFieldReplace() {
        ZipArchiveEntry entry = new ZipArchiveEntry("firstreplace.txt");
        AsiExtraField field1 = new AsiExtraField();
        AsiExtraField field2 = new AsiExtraField();
        entry.addExtraField(field1);
        entry.addAsFirstExtraField(field2);
        assertEquals(1, entry.getExtraFields().length);
        assertEquals(field2, entry.getExtraFields()[0]);
    }

    @Test
    public void testRemoveExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("remove.txt");
        ZipExtraField field = new AsiExtraField();
        entry.addExtraField(field);
        entry.removeExtraField(field.getHeaderId());
        assertEquals(0, entry.getExtraFields().length);
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testRemoveExtraFieldNotPresent() {
        ZipArchiveEntry entry = new ZipArchiveEntry("removefail.txt");
        entry.removeExtraField(new ZipShort(1234));
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testRemoveExtraFieldNullMap() {
        ZipArchiveEntry entry = new ZipArchiveEntry("removenull.txt");
        entry.removeExtraField(new ZipShort(1));
    }

    @Test
    public void testGetExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("getextra.txt");
        ZipExtraField field = new AsiExtraField();
        entry.addExtraField(field);
        assertNotNull(entry.getExtraField(field.getHeaderId()));
    }

    @Test
    public void testGetExtraFieldNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("getnull.txt");
        assertNull(entry.getExtraField(new ZipShort(999)));
    }

    @Test
    public void testSetExtraBytes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("setbytes.txt");
        entry.setExtra(new byte[] { 0, 0 });
        assertNotNull(entry.getExtra());
    }

    @Test
    public void testSetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("central.txt");
        entry.setCentralDirectoryExtra(new byte[] { 0, 0 });
        assertNotNull(entry.getCentralDirectoryExtra());
    }

    @Test
    public void testGetLocalFileDataExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("local.txt");
        byte[] local = entry.getLocalFileDataExtra();
        assertNotNull(local);
    }

    @Test
    public void testGetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("name.txt");
        assertEquals("name.txt", entry.getName());
    }

    @Test
    public void testIsDirectoryTrue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
    }

    @Test
    public void testIsDirectoryFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        assertFalse(entry.isDirectory());
    }

    @Test
    public void testSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("oldname.txt");
        entry.setName("newname.txt");
        assertEquals("newname.txt", entry.getName());
    }

    @Test
    public void testHashCode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("hash.txt");
        assertEquals("hash.txt".hashCode(), entry.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        ZipArchiveEntry entry = new ZipArchiveEntry("eq.txt");
        assertTrue(entry.equals(entry));
    }

    @Test
    public void testEqualsNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("eqnull.txt");
        assertFalse(entry.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        ZipArchiveEntry entry = new ZipArchiveEntry("diffclass.txt");
        assertFalse(entry.equals("string"));
    }

    @Test
    public void testEqualsSameName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("same.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("same.txt");
        assertTrue(entry1.equals(entry2));
    }

    @Test
    public void testEqualsDifferentName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("one.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("two.txt");
        assertFalse(entry1.equals(entry2));
    }

    @Test
    public void testEqualsNullName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("nonnull.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry();
        entry2.setName(null);
        assertFalse(entry1.equals(entry2));
    }

    @Test
    public void testEqualsBothNullName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry();
        entry1.setName(null);
        ZipArchiveEntry entry2 = new ZipArchiveEntry();
        entry2.setName(null);
        assertTrue(entry1.equals(entry2));
    }

    @Test
    public void testGetLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("date.txt");
        Date date = entry.getLastModifiedDate();
        assertNotNull(date);
    }

    @Test
    public void testProtectedConstructor() {
        ZipArchiveEntry entry = new ZipArchiveEntry();
        assertEquals("", entry.getName());
    }

    @Test
    public void testMergeExtraFieldsLocal() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("merge.txt");
        AsiExtraField field = new AsiExtraField();
        entry.addExtraField(field);
        entry.setExtra(new byte[] { 0x75, 0x63, 0x09, 0x00, 0x03, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });
        assertNotNull(entry.getExtraField(field.getHeaderId()));
    }

    @Test
    public void testMergeExtraFieldsCentral() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("mergecentral.txt");
        AsiExtraField field = new AsiExtraField();
        entry.addExtraField(field);
        entry.setCentralDirectoryExtra(new byte[] { 0x75, 0x63, 0x04, 0x00, 0x01, 0x02, 0x03, 0x04 });
        assertNotNull(entry.getExtraField(field.getHeaderId()));
    }

    @Test
    public void testSetExtraWithNullExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("nullextra.txt");
        entry.setExtra(new byte[] { 0x75, 0x63, 0x04, 0x00, 0x01, 0x02, 0x03, 0x04 });
        assertNotNull(entry.getExtraFields());
    }

    @Test
    public void testSetUnixModeReadOnly() {
        ZipArchiveEntry entry = new ZipArchiveEntry("readonly.txt");
        entry.setUnixMode(0444);
        assertEquals(0444, entry.getUnixMode());
    }

    @Test
    public void testSetUnixModeWithDirectoryFlag() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dirflag/");
        entry.setUnixMode(0755);
        assertEquals(0755, entry.getUnixMode());
    }

    @Test
    public void testCloneWithExtraFields() {
        ZipArchiveEntry original = new ZipArchiveEntry("cloneextra.txt");
        original.addExtraField(new AsiExtraField());
        ZipArchiveEntry cloned = (ZipArchiveEntry) original.clone();
        assertEquals(original.getExtraFields().length, cloned.getExtraFields().length);
    }

    @Test
    public void testAddAsFirstExtraFieldNullCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("firstnull.txt");
        ZipExtraField field = new AsiExtraField();
        entry.addAsFirstExtraField(field);
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test
    public void testGetNameSuperFallback() {
        ZipArchiveEntry entry = new ZipArchiveEntry("supername.txt");
        entry.setName(null);
        assertEquals("supername.txt", entry.getName());
    }
}
