package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TarArchiveOutputStreamTest {

    @Test
    public void testConstructorsAndRecordSize() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos1 = new TarArchiveOutputStream(bos);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos1.getRecordSize());
        tos1.close();

        TarArchiveOutputStream tos2 = new TarArchiveOutputStream(bos, "UTF-8");
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos2.getRecordSize());
        tos2.close();

        TarArchiveOutputStream tos3 = new TarArchiveOutputStream(bos, 1024);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos3.getRecordSize());
        tos3.close();

        TarArchiveOutputStream tos4 = new TarArchiveOutputStream(bos, 1024, "UTF-8");
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos4.getRecordSize());
        tos4.close();

        TarArchiveOutputStream tos5 = new TarArchiveOutputStream(bos, 1024, 512);
        Assert.assertEquals(512, tos5.getRecordSize());
        tos5.close();
    }

    @Test
    public void testBasicWriteAndCounts() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] content = "Hello Tar World".getBytes("UTF-8");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();

        Assert.assertTrue(tos.getBytesWritten() > 0);
        Assert.assertEquals((int) tos.getBytesWritten(), tos.getCount());

        tos.flush();
        tos.finish();
        tos.close();

        // Calling close again should be safe and idempotent
        tos.close();
    }

    @Test
    public void testWriteChunkedAssembly() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 512, 512);

        TarArchiveEntry entry = new TarArchiveEntry("chunked.bin");
        byte[] data = new byte[1200];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 127);
        }
        entry.setSize(data.length);

        tos.putArchiveEntry(entry);
        // Write in small chunks (e.g. 100 bytes each) to test assemBuf
        int offset = 0;
        while (offset < data.length) {
            int len = Math.min(100, data.length - offset);
            tos.write(data, offset, len);
            offset += len;
        }
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();
    }

    @Test
    public void testWriteMultipleRecordsAtOnce() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 1024, 512);

        TarArchiveEntry entry = new TarArchiveEntry("bigchunk.bin");
        byte[] data = new byte[1536]; // Exactly 3 records of 512
        entry.setSize(data.length);

        tos.putArchiveEntry(entry);
        tos.write(data, 0, data.length);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();
    }

    @Test
    public void testWriteExceedsEntrySizeThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("overflow.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);

        byte[] buf = new byte[11];
        try {
            tos.write(buf, 0, 11);
            Assert.fail("Expected IOException when writing more than specified entry size");
        } catch (IOException expected) {
            // Success
        }
        tos.close();
    }

    @Test
    public void testPrematureCloseArchiveEntryThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("short.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);

        try {
            tos.closeArchiveEntry();
            Assert.fail("Expected IOException when closing entry before writing all data");
        } catch (IOException expected) {
            // Success
        }
        tos.close();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutActiveEntryThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testFinishWithUnclosedEntryThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("unclosed.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);

        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testDoubleFinishThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryAfterFinishThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.putArchiveEntry(new TarArchiveEntry("file.txt"));
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryAfterFinishThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.closeArchiveEntry();
    }

    @Test
    public void testDirectoryEntryHandling() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("directory/");
        tos.putArchiveEntry(dirEntry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();
    }

    @Test
    public void testCreateArchiveEntry() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        File tmp = File.createTempFile("test_tar", ".tmp");
        try {
            ArchiveEntry entry = tos.createArchiveEntry(tmp, "customName.tmp");
            Assert.assertNotNull(entry);
            Assert.assertEquals("customName.tmp", entry.getName());
        } finally {
            tmp.delete();
            tos.close();
        }
    }

    @Test(expected = IOException.class)
    public void testCreateArchiveEntryAfterFinishThrowsException() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        File tmp = File.createTempFile("test_tar", ".tmp");
        try {
            tos.createArchiveEntry(tmp, "customName.tmp");
        } finally {
            tmp.delete();
        }
    }

    @Test
    public void testLongFileNameModes() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("a");
        }
        String longName = sb.toString();

        // 1. LONGFILE_ERROR mode (default) -> throws RuntimeException
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos1 = new TarArchiveOutputStream(bos1);
        tos1.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        TarArchiveEntry entry1 = new TarArchiveEntry(longName);
        try {
            tos1.putArchiveEntry(entry1);
            Assert.fail("Expected RuntimeException for long file name under LONGFILE_ERROR");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1.close();

        // 2. LONGFILE_TRUNCATE mode
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos2 = new TarArchiveOutputStream(bos2);
        tos2.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        TarArchiveEntry entry2 = new TarArchiveEntry(longName);
        tos2.putArchiveEntry(entry2);
        tos2.closeArchiveEntry();
        tos2.finish();
        tos2.close();

        // 3. LONGFILE_GNU mode
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos3 = new TarArchiveOutputStream(bos3);
        tos3.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        TarArchiveEntry entry3 = new TarArchiveEntry(longName);
        tos3.putArchiveEntry(entry3);
        tos3.closeArchiveEntry();
        tos3.finish();
        tos3.close();

        // 4. LONGFILE_POSIX mode
        ByteArrayOutputStream bos4 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos4 = new TarArchiveOutputStream(bos4);
        tos4.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        TarArchiveEntry entry4 = new TarArchiveEntry(longName);
        tos4.putArchiveEntry(entry4);
        tos4.closeArchiveEntry();
        tos4.finish();
        tos4.close();
    }

    @Test
    public void testBigNumberModes() throws Exception {
        long bigSize = TarConstants.MAXSIZE + 100L;

        // 1. BIGNUMBER_ERROR (default) -> throws RuntimeException
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos1 = new TarArchiveOutputStream(bos1);
        tos1.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        TarArchiveEntry entry1 = new TarArchiveEntry("bigfile.bin");
        entry1.setSize(bigSize);
        try {
            tos1.putArchiveEntry(entry1);
            Assert.fail("Expected RuntimeException for big size under BIGNUMBER_ERROR");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1.close();

        // Test other fields for failForBigNumbers
        long bigId = TarConstants.MAXID + 100L;
        TarArchiveOutputStream tos1b = new TarArchiveOutputStream(new ByteArrayOutputStream());
        TarArchiveEntry entry1b = new TarArchiveEntry("bigid.bin");
        entry1b.setGroupId(bigId);
        try {
            tos1b.putArchiveEntry(entry1b);
            Assert.fail("Expected RuntimeException for big group id");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1b.close();

        TarArchiveOutputStream tos1c = new TarArchiveOutputStream(new ByteArrayOutputStream());
        TarArchiveEntry entry1c = new TarArchiveEntry("bigtime.bin");
        entry1c.setModTime(new Date((TarConstants.MAXSIZE + 1000L) * 1000L));
        try {
            tos1c.putArchiveEntry(entry1c);
            Assert.fail("Expected RuntimeException for big mod time");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1c.close();

        TarArchiveOutputStream tos1d = new TarArchiveOutputStream(new ByteArrayOutputStream());
        TarArchiveEntry entry1d = new TarArchiveEntry("biguid.bin");
        entry1d.setUserId(bigId);
        try {
            tos1d.putArchiveEntry(entry1d);
            Assert.fail("Expected RuntimeException for big user id");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1d.close();

        TarArchiveOutputStream tos1e = new TarArchiveOutputStream(new ByteArrayOutputStream());
        TarArchiveEntry entry1e = new TarArchiveEntry("bigmode.bin");
        entry1e.setMode((int) bigId);
        try {
            tos1e.putArchiveEntry(entry1e);
            Assert.fail("Expected RuntimeException for big mode");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1e.close();

        TarArchiveOutputStream tos1f = new TarArchiveOutputStream(new ByteArrayOutputStream());
        TarArchiveEntry entry1f = new TarArchiveEntry("bigmajor.bin");
        entry1f.setDevMajor((int) bigId);
        try {
            tos1f.putArchiveEntry(entry1f);
            Assert.fail("Expected RuntimeException for big devmajor");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1f.close();

        TarArchiveOutputStream tos1g = new TarArchiveOutputStream(new ByteArrayOutputStream());
        TarArchiveEntry entry1g = new TarArchiveEntry("bigminor.bin");
        entry1g.setDevMinor((int) bigId);
        try {
            tos1g.putArchiveEntry(entry1g);
            Assert.fail("Expected RuntimeException for big devminor");
        } catch (RuntimeException expected) {
            // Success
        }
        tos1g.close();

        // 2. BIGNUMBER_STAR
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos2 = new TarArchiveOutputStream(bos2);
        tos2.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry2 = new TarArchiveEntry("bigfile.bin");
        entry2.setSize(0);
        entry2.setGroupId(bigId);
        entry2.setUserId(bigId);
        tos2.putArchiveEntry(entry2);
        tos2.closeArchiveEntry();
        tos2.finish();
        tos2.close();

        // 3. BIGNUMBER_POSIX
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos3 = new TarArchiveOutputStream(bos3);
        tos3.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry3 = new TarArchiveEntry("bigfile.bin");
        entry3.setSize(0);
        entry3.setGroupId(bigId);
        entry3.setUserId(bigId);
        entry3.setDevMajor((int) bigId);
        entry3.setDevMinor((int) bigId);
        entry3.setModTime(new Date((TarConstants.MAXSIZE + 1000L) * 1000L));
        tos3.putArchiveEntry(entry3);
        tos3.closeArchiveEntry();
        tos3.finish();
        tos3.close();
    }

    @Test
    public void testPaxHeadersForNonAscii() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);

        // Non-ascii name
        TarArchiveEntry entry1 = new TarArchiveEntry("äöü.txt");
        tos.putArchiveEntry(entry1);
        tos.closeArchiveEntry();

        // Non-ascii link name
        TarArchiveEntry entry2 = new TarArchiveEntry("symlink");
        entry2.setLinkName("äöü_target");
        entry2.setMode(TarArchiveEntry.LF_SYMLINK);
        tos.putArchiveEntry(entry2);
        tos.closeArchiveEntry();

        tos.finish();
        tos.close();
    }

    @Test
    public void testWritePaxHeadersFormattingAndLongName() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        StringBuilder longHeaderName = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            longHeaderName.append("a");
        }
        Map<String, String> headers = new HashMap<String, String>();
        // Test key/value sizing logic including multi-pass adjustment in loop
        headers.put("simple", "val");
        StringBuilder longVal = new StringBuilder();
        for (int i = 0; i < 95; i++) {
            longVal.append("x");
        }
        headers.put("longKey", longVal.toString());

        tos.writePaxHeaders(longHeaderName.toString(), headers);
        tos.finish();
        tos.close();
    }
}
