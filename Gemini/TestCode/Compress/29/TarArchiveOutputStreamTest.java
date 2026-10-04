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
    public void testConstructorsAndGetters() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos1 = new TarArchiveOutputStream(bos);
        Assert.assertEquals(512, tos1.getRecordSize());
        Assert.assertEquals(0, tos1.getBytesWritten());
        Assert.assertEquals(0, tos1.getCount());
        tos1.close();

        TarArchiveOutputStream tos2 = new TarArchiveOutputStream(bos, "UTF-8");
        Assert.assertEquals(512, tos2.getRecordSize());
        tos2.close();

        TarArchiveOutputStream tos3 = new TarArchiveOutputStream(bos, 1024);
        Assert.assertEquals(512, tos3.getRecordSize());
        tos3.close();

        TarArchiveOutputStream tos4 = new TarArchiveOutputStream(bos, 1024, "UTF-8");
        Assert.assertEquals(512, tos4.getRecordSize());
        tos4.close();

        TarArchiveOutputStream tos5 = new TarArchiveOutputStream(bos, 1024, 256);
        Assert.assertEquals(256, tos5.getRecordSize());
        tos5.close();
    }

    @Test
    public void testBasicWriteAndClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 1024, 512);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] content = "Hello World".getBytes("UTF-8");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();

        tos.finish();
        Assert.assertTrue(tos.getBytesWritten() > 0);
        Assert.assertTrue(tos.getCount() > 0);
        tos.flush();
        tos.close();
    }

    @Test
    public void testWriteByteByByteAndSmallBuffers() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("chunk.bin");
        byte[] data = new byte[1500];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 128);
        }
        entry.setSize(data.length);

        tos.putArchiveEntry(entry);
        for (int i = 0; i < 600; i++) {
            tos.write(data[i]);
        }
        tos.write(data, 600, 900);
        tos.closeArchiveEntry();
        tos.close();
        Assert.assertEquals(2560, bos.toByteArray().length);
    }

    @Test
    public void testDirectoryEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("somedir/");
        tos.putArchiveEntry(dirEntry);
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.putArchiveEntry(new TarArchiveEntry("test.txt"));
    }

    @Test(expected = IOException.class)
    public void testFinishTwice() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testFinishWithUnclosedEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutOpenEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.closeArchiveEntry();
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteWithoutOpenEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.write(new byte[]{1, 2, 3});
    }

    @Test(expected = IOException.class)
    public void testWriteExceedingSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(5);
        tos.putArchiveEntry(entry);
        tos.write(new byte[10]);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryTooEarly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[5]);
        tos.closeArchiveEntry();
    }

    @Test(expected = RuntimeException.class)
    public void testLongFileNameErrorMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        tos.putArchiveEntry(entry);
    }

    @Test
    public void testLongFileNameTruncateMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test
    public void testLongFileNameGnuMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setModTime(new Date(100000));
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();

        TarArchiveEntry linkEntry = new TarArchiveEntry("short.txt", TarConstants.LF_SYMLINK);
        linkEntry.setLinkName(sb.toString());
        linkEntry.setModTime(new Date(-10000));
        tos.putArchiveEntry(linkEntry);
        tos.closeArchiveEntry();

        tos.close();
    }

    @Test
    public void testLongFileNamePosixMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();

        TarArchiveEntry linkEntry = new TarArchiveEntry("short.txt", TarConstants.LF_SYMLINK);
        linkEntry.setLinkName(sb.toString());
        tos.putArchiveEntry(linkEntry);
        tos.closeArchiveEntry();

        tos.close();
    }

    @Test
    public void testPaxHeadersForNonAscii() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);

        TarArchiveEntry entry = new TarArchiveEntry("\u00e4\u00f6\u00fc.txt");
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();

        TarArchiveEntry linkEntry = new TarArchiveEntry("link.txt", TarConstants.LF_SYMLINK);
        linkEntry.setLinkName("\u00e4\u00f6\u00fc_target.txt");
        tos.putArchiveEntry(linkEntry);
        tos.closeArchiveEntry();

        tos.close();
    }

    @Test(expected = RuntimeException.class)
    public void testBigNumberErrorModeSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("big.bin");
        entry.setSize(TarConstants.MAXSIZE + 1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class)
    public void testBigNumberErrorModeGroupId() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("big.bin");
        entry.setGroupId(TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
    }

    @Test
    public void testBigNumberStarMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);

        TarArchiveEntry entry = new TarArchiveEntry("big.bin");
        entry.setSize(0);
        entry.setGroupId(TarConstants.MAXID + 1);
        entry.setUserId(TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test
    public void testBigNumberPosixMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry entry = new TarArchiveEntry("big.bin");
        entry.setSize(0);
        entry.setGroupId(TarConstants.MAXID + 1);
        entry.setUserId(TarConstants.MAXID + 1);
        entry.setModTime(new Date((TarConstants.MAXSIZE + 100) * 1000));
        entry.setDevMajor((int) TarConstants.MAXID + 1);
        entry.setDevMinor((int) TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test(expected = RuntimeException.class)
    public void testBigNumberPosixModeFailsOnBigMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry entry = new TarArchiveEntry("big.bin");
        entry.setMode(07777777 + 1);
        tos.putArchiveEntry(entry);
    }

    @Test
    public void testWritePaxHeadersFormattingAndEscaping() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("k", "v");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("longvalue");
        }
        headers.put("longkey", sb.toString());

        StringBuilder nameSb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            nameSb.append("a/\\");
        }
        nameSb.append((char) 0);
        tos.writePaxHeaders(entry, nameSb.toString(), headers);
        tos.close();
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        File f = File.createTempFile("tartest", ".tmp");
        try {
            ArchiveEntry entry = tos.createArchiveEntry(f, "entryName.txt");
            Assert.assertNotNull(entry);
            Assert.assertEquals("entryName.txt", entry.getName());
        } finally {
            f.delete();
        }

        tos.finish();
        try {
            tos.createArchiveEntry(f, "afterFinish.txt");
            Assert.fail("Expected IOException");
        } catch (IOException e) {
        }
        tos.close();
    }
}
