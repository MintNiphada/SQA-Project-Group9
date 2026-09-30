package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TarArchiveOutputStreamTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testConstructorsAndGetRecordSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        TarArchiveOutputStream tosDefault = new TarArchiveOutputStream(baos);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tosDefault.getRecordSize());
        tosDefault.close();

        TarArchiveOutputStream tosBlock = new TarArchiveOutputStream(baos, 1024);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tosBlock.getRecordSize());
        tosBlock.close();

        TarArchiveOutputStream tosCustom = new TarArchiveOutputStream(baos, 1024, 512);
        Assert.assertEquals(512, tosCustom.getRecordSize());
        tosCustom.close();
    }

    @Test
    public void testSimpleWriteAndClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        byte[] content = "Hello World".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        byte[] result = baos.toByteArray();
        Assert.assertTrue(result.length >= TarBuffer.DEFAULT_BLKSIZE);
    }

    @Test
    public void testWriteSingleBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        byte[] content = new byte[] { 1, 2, 3, 4, 5 };
        TarArchiveEntry entry = new TarArchiveEntry("bytes.bin");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        for (byte b : content) {
            tos.write(b);
        }
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        byte[] result = baos.toByteArray();
        Assert.assertTrue(result.length > 0);
    }

    @Test
    public void testWriteDirectoryEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("mydir/", TarConstants.LF_DIR);
        dirEntry.setSize(100); // Directory size should be forced to 0 internally

        tos.putArchiveEntry(dirEntry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testChunkedWritesAssemblingRecords() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        int totalSize = 1200; // More than 2 records (512 * 2 = 1024)
        byte[] data = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            data[i] = (byte) (i % 128);
        }

        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        entry.setSize(totalSize);

        tos.putArchiveEntry(entry);

        // Write in small chunks to exercise assembly buffer branch: (assemLen + numToWrite < recordBuf.length)
        int chunkSize1 = 100;
        tos.write(data, 0, chunkSize1);

        // Write chunk that crosses record boundary: (assemLen + numToWrite >= recordBuf.length)
        int chunkSize2 = 500;
        tos.write(data, chunkSize1, chunkSize2);

        // Write remainder which is 600 bytes (longer than record size, loops in write)
        int remaining = totalSize - chunkSize1 - chunkSize2;
        tos.write(data, chunkSize1 + chunkSize2, remaining);

        tos.closeArchiveEntry();
        tos.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testWriteLargerThanSingleRecordDirectly() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        int totalSize = 1024; // Exactly two 512-byte records
        byte[] data = new byte[totalSize];
        Arrays.fill(data, (byte) 'A');

        TarArchiveEntry entry = new TarArchiveEntry("exact2records.txt");
        entry.setSize(totalSize);

        tos.putArchiveEntry(entry);
        tos.write(data, 0, totalSize);
        tos.closeArchiveEntry();
        tos.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test(expected = IOException.class)
    public void testWriteExceedingSizeThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        TarArchiveEntry entry = new TarArchiveEntry("overflow.txt");
        entry.setSize(5);

        tos.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tos.write(data, 0, data.length);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryTooEarlyThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        TarArchiveEntry entry = new TarArchiveEntry("underflow.txt");
        entry.setSize(100);

        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class)
    public void testFinishWithUnclosedEntryThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        TarArchiveEntry entry = new TarArchiveEntry("unclosed.txt");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.finish();
    }

    @Test(expected = RuntimeException.class)
    public void testLongFileNameDefaultModeError() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        String longName = "this_is_a_very_long_file_name_that_exceeds_one_hundred_characters_in_length_which_is_the_tar_constants_name_limit_for_standard_tar_headers.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
    }

    @Test
    public void testLongFileNameTruncateMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        String longName = "this_is_a_very_long_file_name_that_exceeds_one_hundred_characters_in_length_which_is_the_tar_constants_name_limit_for_standard_tar_headers.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testLongFileNameGnuMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        String longName = "this_is_a_very_long_file_name_that_exceeds_one_hundred_characters_in_length_which_is_the_tar_constants_name_limit_for_standard_tar_headers.txt";
        byte[] content = "Long name file content".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test(expected = ClassCastException.class)
    public void testPutArchiveEntryNonTarEntryThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "dummy";
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public java.util.Date getLastModifiedDate() {
                return new java.util.Date();
            }
        };

        tos.putArchiveEntry(nonTarEntry);
    }

    @Test
    public void testCloseIdempotency() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();

        tos.close();
        int sizeAfterFirstClose = baos.size();
        tos.close();
        Assert.assertEquals(sizeAfterFirstClose, baos.size());
    }

    @Test
    public void testFlush() throws IOException {
        final boolean[] flushed = new boolean[] { false };
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FilterOutputStream fos = new FilterOutputStream(baos) {
            @Override
            public void flush() throws IOException {
                flushed[0] = true;
                super.flush();
            }
        };

        TarArchiveOutputStream tos = new TarArchiveOutputStream(fos);
        tos.flush();
        Assert.assertTrue(flushed[0]);
        tos.close();
    }

    @Test
    public void testCreateArchiveEntryFromFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos);

        File tempFile = tempFolder.newFile("sample.txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("Hello sample".getBytes("UTF-8"));
        }

        ArchiveEntry entry = tos.createArchiveEntry(tempFile, "entry_name.txt");
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry instanceof TarArchiveEntry);
        Assert.assertEquals("entry_name.txt", entry.getName());
        Assert.assertEquals(tempFile.length(), entry.getSize());

        tos.close();
    }
}
