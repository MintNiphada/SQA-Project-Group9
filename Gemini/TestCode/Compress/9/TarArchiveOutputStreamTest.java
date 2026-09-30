package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class TarArchiveOutputStreamTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testConstructorsAndRecordSize() throws IOException {
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos1 = new TarArchiveOutputStream(bos1);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos1.getRecordSize());
        tos1.close();

        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos2 = new TarArchiveOutputStream(bos2, 1024);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos2.getRecordSize());
        tos2.close();

        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        TarArchiveOutputStream tos3 = new TarArchiveOutputStream(bos3, 1024, 512);
        Assert.assertEquals(512, tos3.getRecordSize());
        tos3.close();
    }

    @Test
    public void testWriteAndReadBasicEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        byte[] content = "Hello, TarArchiveOutputStream World!".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("test.txt", readEntry.getName());
        Assert.assertEquals(content.length, readEntry.getSize());

        byte[] readContent = new byte[content.length];
        int readBytes = tis.read(readContent);
        Assert.assertEquals(content.length, readBytes);
        Assert.assertArrayEquals(content, readContent);
        Assert.assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test
    public void testWriteSingleBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        byte[] content = new byte[] { 1, 2, 3, 4, 5 };
        TarArchiveEntry entry = new TarArchiveEntry("bytes.bin");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        for (byte b : content) {
            tos.write(b & 0xFF);
        }
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        byte[] readContent = new byte[5];
        int read = tis.read(readContent);
        Assert.assertEquals(5, read);
        Assert.assertArrayEquals(content, readContent);
        tis.close();
    }

    @Test
    public void testWriteDirectoryEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("dir/");
        tos.putArchiveEntry(dirEntry);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertTrue(readEntry.isDirectory());
        Assert.assertEquals("dir/", readEntry.getName());
        tis.close();
    }

    @Test
    public void testWriteMultipleRecordsAndPartialBuffer() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        int size = 512 * 2 + 100; // 2 full records + 100 bytes partial
        byte[] content = new byte[size];
        for (int i = 0; i < size; i++) {
            content[i] = (byte) (i % 128);
        }

        TarArchiveEntry entry = new TarArchiveEntry("multi-records.bin");
        entry.setSize(size);

        tos.putArchiveEntry(entry);
        // Write in small chunks to test assembly buffer logic
        int offset = 0;
        int chunkSize = 70;
        while (offset < size) {
            int toWrite = Math.min(chunkSize, size - offset);
            tos.write(content, offset, toWrite);
            offset += toWrite;
        }
        tos.closeArchiveEntry();
        tos.finish();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(size, readEntry.getSize());

        byte[] readBuffer = new byte[size];
        int totalRead = 0;
        int read;
        while ((read = tis.read(readBuffer, totalRead, size - totalRead)) > 0) {
            totalRead += read;
        }
        Assert.assertEquals(size, totalRead);
        Assert.assertArrayEquals(content, readBuffer);
        tis.close();
        tos.close();
    }

    @Test
    public void testWriteLargeChunkDirect() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        int size = 512 * 3;
        byte[] content = new byte[size];
        Arrays.fill(content, (byte) 'A');

        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        entry.setSize(size);

        tos.putArchiveEntry(entry);
        // Write all 3 records at once
        tos.write(content, 0, size);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(size, readEntry.getSize());
        byte[] readData = new byte[size];
        int readBytes = tis.read(readData);
        Assert.assertEquals(size, readBytes);
        Assert.assertArrayEquals(content, readData);
        tis.close();
    }

    @Test
    public void testLongFileNameErrorModeDefault() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 105; i++) {
            sb.append("a");
        }
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        try {
            tos.putArchiveEntry(entry);
            Assert.fail("Expected RuntimeException due to long file name");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("is too long"));
        } finally {
            try {
                tos.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    public void testLongFileNameTruncateMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("b");
        }
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(longName.substring(0, TarConstants.NAMELEN), readEntry.getName());
        tis.close();
    }

    @Test
    public void testLongFileNameGnuMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("c");
        }
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        byte[] content = "Long name content".getBytes("UTF-8");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(longName, readEntry.getName());
        byte[] readContent = new byte[content.length];
        int read = tis.read(readContent);
        Assert.assertEquals(content.length, read);
        Assert.assertArrayEquals(content, readContent);
        tis.close();
    }

    @Test(expected = IOException.class)
    public void testFinishTwiceThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testFinishWithUnclosedEntryThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.finish();
    }

    @Test(expected = IOException.class)
    public void testPutArchiveEntryAfterFinishedThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tos.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutEntryThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryAfterFinishedThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryBeforeBytesWrittenThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testWriteExceedsEntrySizeThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[11]);
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        File file = tempFolder.newFile("testFile.txt");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(new byte[] { 1, 2, 3 });
        fos.close();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        ArchiveEntry entry = tos.createArchiveEntry(file, "customName.txt");
        Assert.assertNotNull(entry);
        Assert.assertEquals("customName.txt", entry.getName());
        Assert.assertEquals(3L, entry.getSize());
        tos.close();
    }

    @Test(expected = IOException.class)
    public void testCreateArchiveEntryWhenFinishedThrowsException() throws IOException {
        File file = tempFolder.newFile("testFileFinished.txt");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.createArchiveEntry(file, "customName.txt");
    }

    @Test
    public void testFlushAndCloseMultipleTimes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.flush();
        tos.close();
        tos.close(); // closing twice should not throw an exception
    }
}
