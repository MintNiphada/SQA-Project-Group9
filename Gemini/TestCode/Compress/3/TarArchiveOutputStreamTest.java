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
import java.util.Date;

public class TarArchiveOutputStreamTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testConstructorsAndGetRecordSize() throws IOException {
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        TarArchiveOutputStream taos1 = new TarArchiveOutputStream(bos1);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, taos1.getRecordSize());
        taos1.close();

        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        TarArchiveOutputStream taos2 = new TarArchiveOutputStream(bos2, TarBuffer.DEFAULT_BLKSIZE);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, taos2.getRecordSize());
        taos2.close();

        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        TarArchiveOutputStream taos3 = new TarArchiveOutputStream(bos3, 1024, 512);
        Assert.assertEquals(512, taos3.getRecordSize());
        taos3.close();
    }

    @Test
    public void testWriteAndReadSingleEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] data = "Hello, Tar World!".getBytes("UTF-8");
        entry.setSize(data.length);
        entry.setModTime(new Date());

        taos.putArchiveEntry(entry);
        taos.write(data);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("test.txt", readEntry.getName());
        Assert.assertEquals(data.length, readEntry.getSize());

        byte[] readData = new byte[data.length];
        int bytesRead = tais.read(readData, 0, readData.length);
        Assert.assertEquals(data.length, bytesRead);
        Assert.assertArrayEquals(data, readData);

        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testWriteDirectoryEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("testDir/");
        dirEntry.setSize(0);

        taos.putArchiveEntry(dirEntry);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertTrue(readEntry.isDirectory());
        Assert.assertEquals("testDir/", readEntry.getName());
        Assert.assertEquals(0, readEntry.getSize());
        tais.close();
    }

    @Test
    public void testWriteAssembledBlocks() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        byte[] chunk1 = new byte[100];
        byte[] chunk2 = new byte[300];
        byte[] chunk3 = new byte[200];
        Arrays.fill(chunk1, (byte) 'a');
        Arrays.fill(chunk2, (byte) 'b');
        Arrays.fill(chunk3, (byte) 'c');

        int totalSize = chunk1.length + chunk2.length + chunk3.length;
        TarArchiveEntry entry = new TarArchiveEntry("assemblyTest.dat");
        entry.setSize(totalSize);

        taos.putArchiveEntry(entry);
        // Write chunk1 (100 bytes) -> goes into assemBuf
        taos.write(chunk1, 0, chunk1.length);
        // Write chunk2 (300 bytes) -> total in assemBuf becomes 400
        taos.write(chunk2, 0, chunk2.length);
        // Write chunk3 (200 bytes) -> triggers assembly overflow (400 + 200 = 600 >= 512),
        // writes 512-byte record, remainder (88 bytes) goes to assemBuf
        taos.write(chunk3, 0, chunk3.length);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(totalSize, readEntry.getSize());

        byte[] readContent = new byte[totalSize];
        int offset = 0;
        int read;
        while ((read = tais.read(readContent, offset, totalSize - offset)) > 0) {
            offset += read;
        }
        Assert.assertEquals(totalSize, offset);

        byte[] expected = new byte[totalSize];
        System.arraycopy(chunk1, 0, expected, 0, chunk1.length);
        System.arraycopy(chunk2, 0, expected, chunk1.length, chunk2.length);
        System.arraycopy(chunk3, 0, expected, chunk1.length + chunk2.length, chunk3.length);

        Assert.assertArrayEquals(expected, readContent);
        tais.close();
    }

    @Test
    public void testWriteMultipleFullBlocksAndRemainder() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        int size = 512 * 3 + 123;
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 128);
        }

        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        entry.setSize(size);
        taos.putArchiveEntry(entry);
        taos.write(data, 0, size);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(size, readEntry.getSize());

        byte[] readData = new byte[size];
        int offset = 0;
        int read;
        while ((read = tais.read(readData, offset, size - offset)) > 0) {
            offset += read;
        }
        Assert.assertEquals(size, offset);
        Assert.assertArrayEquals(data, readData);
        tais.close();
    }

    @Test(expected = IOException.class)
    public void testWriteExceedsEntrySize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("small.txt");
        entry.setSize(10);
        taos.putArchiveEntry(entry);

        byte[] excessData = new byte[20];
        taos.write(excessData, 0, excessData.length);
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryBeforeWritingAllBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("incomplete.txt");
        entry.setSize(100);
        taos.putArchiveEntry(entry);

        byte[] partialData = new byte[50];
        taos.write(partialData);
        taos.closeArchiveEntry();
    }

    @Test(expected = RuntimeException.class)
    public void testLongFileNameErrorModeDefault() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            longName.append("a");
        }

        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        entry.setSize(0);
        taos.putArchiveEntry(entry);
    }

    @Test
    public void testLongFileNameTruncateMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            longName.append("a");
        }

        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(TarConstants.NAMELEN, readEntry.getName().length());
        Assert.assertEquals(longName.substring(0, TarConstants.NAMELEN), readEntry.getName());
        tais.close();
    }

    @Test
    public void testLongFileNameGnuMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        StringBuilder longName = new StringBuilder("directory-with-very-long-path-name/");
        for (int i = 0; i < 100; i++) {
            longName.append("subfolder").append(i).append("/");
        }
        longName.append("testfile.txt");

        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        byte[] content = "GNU long name content".getBytes("UTF-8");
        entry.setSize(content.length);

        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(longName.toString(), readEntry.getName());
        Assert.assertEquals(content.length, readEntry.getSize());

        byte[] readContent = new byte[content.length];
        int read = tais.read(readContent);
        Assert.assertEquals(content.length, read);
        Assert.assertArrayEquals(content, readContent);
        tais.close();
    }

    @Test(expected = ClassCastException.class)
    public void testPutArchiveEntryInvalidType() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

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
            public Date getLastModifiedDate() {
                return new Date();
            }
        };

        try {
            taos.putArchiveEntry(nonTarEntry);
        } finally {
            taos.close();
        }
    }

    @Test
    public void testCreateArchiveEntry() throws IOException {
        File tempFile = temporaryFolder.newFile("createEntryTest.txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("sample content".getBytes("UTF-8"));
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        ArchiveEntry createdEntry = taos.createArchiveEntry(tempFile, "customName.txt");
        Assert.assertTrue(createdEntry instanceof TarArchiveEntry);
        Assert.assertEquals("customName.txt", createdEntry.getName());
        Assert.assertEquals(tempFile.length(), createdEntry.getSize());

        taos.close();
    }

    @Test
    public void testFinishAndMultipleClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("simple.txt");
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();

        taos.finish();
        int sizeAfterFinish = bos.size();
        Assert.assertTrue(sizeAfterFinish > 0);

        // Calling close should not throw exception and should be idempotent
        taos.close();
        taos.close();
    }

    @Test
    public void testFlush() throws IOException {
        final boolean[] flushed = new boolean[]{false};
        ByteArrayOutputStream bos = new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                super.flush();
                flushed[0] = true;
            }
        };

        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);
        taos.flush();
        Assert.assertTrue(flushed[0]);
        taos.close();
    }

    @Test
    public void testSingleByteWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("singleByte.txt");
        entry.setSize(1);
        taos.putArchiveEntry(entry);
        taos.write('A');
        taos.closeArchiveEntry();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals(1, readEntry.getSize());
        Assert.assertEquals('A', tais.read());
        Assert.assertEquals(-1, tais.read());
        tais.close();
    }
}
