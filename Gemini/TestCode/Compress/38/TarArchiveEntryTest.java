package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TarArchiveEntryTest {

    @Test
    public void testConstructorsAndBasicGetters() {
        TarArchiveEntry entry = new TarArchiveEntry("test/file.txt");
        Assert.assertEquals("test/file.txt", entry.getName());
        Assert.assertEquals(TarArchiveEntry.DEFAULT_FILE_MODE, entry.getMode());
        Assert.assertFalse(entry.isDirectory());
        Assert.assertTrue(entry.isFile());
        Assert.assertNull(entry.getFile());

        TarArchiveEntry dirEntry = new TarArchiveEntry("test/dir/");
        Assert.assertEquals("test/dir/", dirEntry.getName());
        Assert.assertEquals(TarArchiveEntry.DEFAULT_DIR_MODE, dirEntry.getMode());
        Assert.assertTrue(dirEntry.isDirectory());
        Assert.assertFalse(dirEntry.isFile());
    }

    @Test
    public void testPreserveLeadingSlashes() {
        TarArchiveEntry entryNoPreserve = new TarArchiveEntry("/foo/bar", false);
        Assert.assertEquals("foo/bar", entryNoPreserve.getName());

        TarArchiveEntry entryPreserve = new TarArchiveEntry("/foo/bar", true);
        Assert.assertEquals("/foo/bar", entryPreserve.getName());
    }

    @Test
    public void testLinkFlagConstructors() {
        TarArchiveEntry longNameEntry = new TarArchiveEntry("longName", TarConstants.LF_GNUTYPE_LONGNAME);
        Assert.assertTrue(longNameEntry.isGNULongNameEntry());

        TarArchiveEntry longLinkEntry = new TarArchiveEntry("longLink", TarConstants.LF_GNUTYPE_LONGLINK, false);
        Assert.assertTrue(longLinkEntry.isGNULongLinkEntry());
    }

    @Test
    public void testFileConstructors() throws IOException {
        File tempFile = File.createTempFile("tar_test", ".tmp");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[]{1, 2, 3, 4});
        }

        TarArchiveEntry entry = new TarArchiveEntry(tempFile);
        Assert.assertEquals(tempFile, entry.getFile());
        Assert.assertEquals(4, entry.getSize());
        Assert.assertTrue(entry.isFile());
        Assert.assertFalse(entry.isDirectory());
        Assert.assertArrayEquals(new TarArchiveEntry[0], entry.getDirectoryEntries());

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "tar_test_dir_" + System.currentTimeMillis());
        Assert.assertTrue(tempDir.mkdir());
        tempDir.deleteOnExit();
        File childFile = new File(tempDir, "child.txt");
        Assert.assertTrue(childFile.createNewFile());
        childFile.deleteOnExit();

        TarArchiveEntry dirEntry = new TarArchiveEntry(tempDir);
        Assert.assertTrue(dirEntry.isDirectory());
        Assert.assertTrue(dirEntry.getName().endsWith("/"));
        TarArchiveEntry[] children = dirEntry.getDirectoryEntries();
        Assert.assertNotNull(children);
        Assert.assertEquals(1, children.length);
        Assert.assertEquals("child.txt", children[0].getFile().getName());

        childFile.delete();
        tempDir.delete();
        tempFile.delete();
    }

    @Test
    public void testSettersAndGetters() {
        TarArchiveEntry entry = new TarArchiveEntry("file.txt");
        entry.setSize(1024L);
        Assert.assertEquals(1024L, entry.getSize());

        entry.setMode(0644);
        Assert.assertEquals(0644, entry.getMode());

        entry.setUserId(1001);
        Assert.assertEquals(1001, entry.getUserId());
        Assert.assertEquals(1001L, entry.getLongUserId());

        entry.setGroupId(1002);
        Assert.assertEquals(1002, entry.getGroupId());
        Assert.assertEquals(1002L, entry.getLongGroupId());

        entry.setIds(2001, 2002);
        Assert.assertEquals(2001L, entry.getLongUserId());
        Assert.assertEquals(2002L, entry.getLongGroupId());

        entry.setUserName("testUser");
        Assert.assertEquals("testUser", entry.getUserName());

        entry.setGroupName("testGroup");
        Assert.assertEquals("testGroup", entry.getGroupName());

        entry.setNames("user2", "group2");
        Assert.assertEquals("user2", entry.getUserName());
        Assert.assertEquals("group2", entry.getGroupName());

        entry.setLinkName("linked_file");
        Assert.assertEquals("linked_file", entry.getLinkName());

        entry.setDevMajor(5);
        Assert.assertEquals(5, entry.getDevMajor());

        entry.setDevMinor(10);
        Assert.assertEquals(10, entry.getDevMinor());

        Date date = new Date(1600000000000L);
        entry.setModTime(date);
        Assert.assertEquals(date.getTime() / 1000 * 1000, entry.getModTime().getTime());
        Assert.assertEquals(entry.getModTime(), entry.getLastModifiedDate());

        entry.setModTime(1500000000000L);
        Assert.assertEquals(1500000000000L, entry.getModTime().getTime());

        entry.setName("/new/path.txt");
        Assert.assertEquals("new/path.txt", entry.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSize() {
        new TarArchiveEntry("test").setSize(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDevMajor() {
        new TarArchiveEntry("test").setDevMajor(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidDevMinor() {
        new TarArchiveEntry("test").setDevMinor(-1);
    }

    @Test
    public void testEqualityAndDescendant() {
        TarArchiveEntry entry1 = new TarArchiveEntry("foo/bar");
        TarArchiveEntry entry2 = new TarArchiveEntry("foo/bar");
        TarArchiveEntry entry3 = new TarArchiveEntry("foo/bar/baz");

        Assert.assertTrue(entry1.equals(entry2));
        Assert.assertTrue(entry1.equals((Object) entry2));
        Assert.assertFalse(entry1.equals(entry3));
        Assert.assertFalse(entry1.equals(null));
        Assert.assertFalse(entry1.equals("foo/bar"));
        Assert.assertEquals(entry1.hashCode(), entry2.hashCode());

        Assert.assertTrue(entry1.isDescendent(entry3));
        Assert.assertFalse(entry3.isDescendent(entry1));
    }

    @Test
    public void testFlagsAndTypePredicates() {
        TarArchiveEntry sym = new TarArchiveEntry("sym", TarConstants.LF_SYMLINK);
        Assert.assertTrue(sym.isSymbolicLink());

        TarArchiveEntry link = new TarArchiveEntry("link", TarConstants.LF_LINK);
        Assert.assertTrue(link.isLink());

        TarArchiveEntry chr = new TarArchiveEntry("chr", TarConstants.LF_CHR);
        Assert.assertTrue(chr.isCharacterDevice());

        TarArchiveEntry blk = new TarArchiveEntry("blk", TarConstants.LF_BLK);
        Assert.assertTrue(blk.isBlockDevice());

        TarArchiveEntry fifo = new TarArchiveEntry("fifo", TarConstants.LF_FIFO);
        Assert.assertTrue(fifo.isFIFO());

        TarArchiveEntry paxLc = new TarArchiveEntry("pax", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        Assert.assertTrue(paxLc.isPaxHeader());

        TarArchiveEntry paxUc = new TarArchiveEntry("pax", TarConstants.LF_PAX_EXTENDED_HEADER_UC);
        Assert.assertTrue(paxUc.isPaxHeader());

        TarArchiveEntry paxGlobal = new TarArchiveEntry("pax", TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER);
        Assert.assertTrue(paxGlobal.isGlobalPaxHeader());

        TarArchiveEntry sparse = new TarArchiveEntry("sparse", TarConstants.LF_GNUTYPE_SPARSE);
        Assert.assertTrue(sparse.isOldGNUSparse());
        Assert.assertTrue(sparse.isGNUSparse());
        Assert.assertTrue(sparse.isSparse());
    }

    @Test
    public void testWriteAndParseEntryHeader() {
        TarArchiveEntry entry = new TarArchiveEntry("test_entry.txt");
        entry.setSize(2048);
        entry.setMode(0644);
        entry.setUserId(500);
        entry.setGroupId(500);
        entry.setModTime(1000000000000L);
        entry.setUserName("tester");
        entry.setGroupName("testing");

        byte[] buf = new byte[512];
        entry.writeEntryHeader(buf);

        TarArchiveEntry parsed = new TarArchiveEntry(buf);
        Assert.assertEquals("test_entry.txt", parsed.getName());
        Assert.assertEquals(2048, parsed.getSize());
        Assert.assertEquals(0644, parsed.getMode());
        Assert.assertEquals(500, parsed.getLongUserId());
        Assert.assertEquals(500, parsed.getLongGroupId());
        Assert.assertEquals("tester", parsed.getUserName());
        Assert.assertEquals("testing", parsed.getGroupName());
        Assert.assertTrue(parsed.isCheckSumOK());
        Assert.assertFalse(parsed.isExtended());
        Assert.assertEquals(0L, parsed.getRealSize());
    }

    @Test
    public void testWriteEntryHeaderStarMode() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("star_test.txt");
        entry.setSize(0777777777777L + 100L);
        byte[] buf = new byte[512];
        entry.writeEntryHeader(buf, ZipEncodingHelper.getZipEncoding("UTF-8"), true);

        TarArchiveEntry parsed = new TarArchiveEntry(buf, ZipEncodingHelper.getZipEncoding("UTF-8"));
        Assert.assertEquals("star_test.txt", parsed.getName());
        Assert.assertEquals(entry.getSize(), parsed.getSize());
    }

    @Test
    public void testWriteEntryHeaderNonStarModeOverflow() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("overflow.txt");
        entry.setSize(0777777777777L + 100L);
        byte[] buf = new byte[512];
        entry.writeEntryHeader(buf, ZipEncodingHelper.getZipEncoding("UTF-8"), false);

        TarArchiveEntry parsed = new TarArchiveEntry(buf);
        Assert.assertEquals(0L, parsed.getSize());
    }

    @Test
    public void testFillSparseDataMethods() {
        TarArchiveEntry entry = new TarArchiveEntry("sparse.bin");

        Map<String, String> gnu0x = new HashMap<>();
        gnu0x.put("GNU.sparse.size", "12345");
        gnu0x.put("GNU.sparse.name", "sparse0x.bin");
        entry.fillGNUSparse0xData(gnu0x);
        Assert.assertTrue(entry.isPaxGNUSparse());
        Assert.assertTrue(entry.isGNUSparse());
        Assert.assertTrue(entry.isSparse());
        Assert.assertEquals(12345, entry.getRealSize());
        Assert.assertEquals("sparse0x.bin", entry.getName());

        Map<String, String> gnu1x = new HashMap<>();
        gnu1x.put("GNU.sparse.realsize", "54321");
        gnu1x.put("GNU.sparse.name", "sparse1x.bin");
        entry.fillGNUSparse1xData(gnu1x);
        Assert.assertTrue(entry.isPaxGNUSparse());
        Assert.assertEquals(54321, entry.getRealSize());
        Assert.assertEquals("sparse1x.bin", entry.getName());

        Map<String, String> star = new HashMap<>();
        star.put("SCHILY.realsize", "99999");
        entry.fillStarSparseData(star);
        Assert.assertTrue(entry.isStarSparse());
        Assert.assertTrue(entry.isSparse());
        Assert.assertEquals(99999L, entry.getRealSize());
    }

    @Test
    public void testParseTarHeaderWithPrefix() throws IOException {
        byte[] buf = new byte[512];
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.writeEntryHeader(buf);

        byte[] prefixBytes = "prefix/path".getBytes("US-ASCII");
        System.arraycopy(prefixBytes, 0, buf, TarConstants.PREFIXLEN_OFFSET, prefixBytes.length);

        long chk = TarUtils.computeCheckSum(buf);
        TarUtils.formatCheckSumOctalBytes(chk, buf, 148, TarConstants.CHKSUMLEN);

        TarArchiveEntry parsed = new TarArchiveEntry(buf);
        Assert.assertEquals("prefix/path/test.txt", parsed.getName());
    }

    @Test
    public void testParseOldGNUHeader() {
        byte[] buf = new byte[512];
        TarArchiveEntry entry = new TarArchiveEntry("oldgnu.txt", TarConstants.LF_GNUTYPE_SPARSE);
        entry.writeEntryHeader(buf);

        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, buf, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        buf[TarConstants.ISEXTENDED_OFFSET_GNU] = 1;
        TarUtils.formatOctalBytes(123456L, buf, TarConstants.REALSIZE_OFFSET_GNU, TarConstants.REALSIZELEN_GNU);

        long chk = TarUtils.computeCheckSum(buf);
        TarUtils.formatCheckSumOctalBytes(chk, buf, 148, TarConstants.CHKSUMLEN);

        TarArchiveEntry parsed = new TarArchiveEntry(buf);
        Assert.assertTrue(parsed.isOldGNUSparse());
        Assert.assertTrue(parsed.isExtended());
        Assert.assertEquals(123456L, parsed.getRealSize());
    }

    @Test
    public void testParseXStarHeader() {
        byte[] buf = new byte[512];
        TarArchiveEntry entry = new TarArchiveEntry("xstar.txt");
        entry.writeEntryHeader(buf);

        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, buf, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.MAGIC_XSTAR.getBytes(), 0, buf, TarConstants.XSTAR_MAGIC_OFFSET, TarConstants.XSTAR_MAGIC_LEN);
        byte[] prefixBytes = "xstar_prefix".getBytes();
        System.arraycopy(prefixBytes, 0, buf, TarConstants.PREFIXLEN_OFFSET, prefixBytes.length);

        long chk = TarUtils.computeCheckSum(buf);
        TarUtils.formatCheckSumOctalBytes(chk, buf, 148, TarConstants.CHKSUMLEN);

        TarArchiveEntry parsed = new TarArchiveEntry(buf);
        Assert.assertEquals("xstar_prefix/xstar.txt", parsed.getName());
    }
}
