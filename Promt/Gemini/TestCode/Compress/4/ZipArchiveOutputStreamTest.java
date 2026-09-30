package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class ZipArchiveOutputStreamTest {

    private File tempFile;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("zip-output-test", ".zip");
    }

    @After
    public void tearDown() throws Exception {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testIsSeekable() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaosStream = new ZipArchiveOutputStream(baos);
        Assert.assertFalse(zaosStream.isSeekable());
        zaosStream.close();

        ZipArchiveOutputStream zaosFile = new ZipArchiveOutputStream(tempFile);
        Assert.assertTrue(zaosFile.isSeekable());
        zaosFile.close();
    }

    @Test
    public void testSetAndGetEncoding() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        
        Assert.assertEquals("UTF8", zaos.getEncoding());
        zaos.setEncoding("CP437");
        Assert.assertEquals("CP437", zaos.getEncoding());
        zaos.setEncoding(null);
        Assert.assertNull(zaos.getEncoding());
        
        zaos.close();
    }

    @Test
    public void testSetUseLanguageEncodingFlag() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        
        zaos.setEncoding("UTF8");
        zaos.setUseLanguageEncodingFlag(true);
        zaos.setUseLanguageEncodingFlag(false);
        
        zaos.setEncoding("US-ASCII");
        zaos.setUseLanguageEncodingFlag(true);
        
        zaos.close();
    }

    @Test
    public void testSetLevelValidAndInvalid() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        zaos.setLevel(Deflater.DEFAULT_COMPRESSION);
        zaos.setLevel(Deflater.NO_COMPRESSION);
        zaos.setLevel(Deflater.BEST_SPEED);
        zaos.setLevel(Deflater.BEST_COMPRESSION);

        try {
            zaos.setLevel(-2);
            Assert.fail("Expected IllegalArgumentException for level -2");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            zaos.setLevel(10);
            Assert.fail("Expected IllegalArgumentException for level 10");
        } catch (IllegalArgumentException e) {
            // expected
        }

        zaos.close();
    }

    @Test
    public void testSetMethod() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        zaos.setMethod(ZipArchiveOutputStream.STORED);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setSize(0);
        entry.setCrc(0);
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        Assert.assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
    }

    @Test
    public void testDeflatedStreamOutput() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("deflated.txt");
        zaos.putArchiveEntry(entry);

        byte[] data = "Hello Deflated World! This is compressed content.".getBytes("UTF-8");
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();

        byte[] zipBytes = baos.toByteArray();
        Assert.assertTrue(zipBytes.length > 0);
        Assert.assertEquals(data.length, entry.getSize());
        Assert.assertTrue(entry.getCompressedSize() > 0);
    }

    @Test
    public void testDeflatedLargeDataBlockChunking() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("large.bin");
        zaos.putArchiveEntry(entry);

        // Size > 8192 (DEFLATER_BLOCK_SIZE) to test loop and remainder branch
        byte[] largeData = new byte[20000];
        Arrays.fill(largeData, (byte) 0xAA);
        zaos.write(largeData, 0, largeData.length);
        
        // Zero length write coverage
        zaos.write(largeData, 0, 0);

        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        Assert.assertEquals(largeData.length, entry.getSize());
    }

    @Test
    public void testDeflatedWithLevelChange() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        zaos.setLevel(Deflater.BEST_COMPRESSION);
        ZipArchiveEntry entry = new ZipArchiveEntry("level9.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("Compress with 9".getBytes("UTF-8"), 0, 15);
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
    }

    @Test
    public void testStoredStreamOutputSuccess() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = "Stored data exact size".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(data);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();

        Assert.assertEquals(data.length, entry.getSize());
        Assert.assertEquals(data.length, entry.getCompressedSize());
        Assert.assertEquals(crc.getValue(), entry.getCrc());
    }

    @Test(expected = ZipException.class)
    public void testStoredStreamMissingSizeThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored_no_size.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setCrc(12345L);

        zaos.putArchiveEntry(entry);
    }

    @Test(expected = ZipException.class)
    public void testStoredStreamMissingCrcThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored_no_crc.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(100L);

        zaos.putArchiveEntry(entry);
    }

    @Test(expected = ZipException.class)
    public void testStoredStreamBadCrcThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = "Hello World".getBytes("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("stored_bad_crc.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCrc(999999L); // wrong CRC

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
    }

    @Test(expected = ZipException.class)
    public void testStoredStreamBadSizeThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = "Hello World".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(data);

        ZipArchiveEntry entry = new ZipArchiveEntry("stored_bad_size.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length + 5); // wrong size
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
    }

    @Test
    public void testRandomAccessFileOutputDeflatedAndStored() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(tempFile);
        Assert.assertTrue(zaos.isSeekable());

        // Stored entry without pre-calculating size and CRC
        ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored_raf.txt");
        storedEntry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(storedEntry);
        byte[] storedData = "Random access stored entry content".getBytes("UTF-8");
        zaos.write(storedData, 0, storedData.length);
        zaos.closeArchiveEntry();

        // Deflated entry
        ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("deflated_raf.txt");
        deflatedEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(deflatedEntry);
        byte[] deflatedData = "Random access deflated entry content".getBytes("UTF-8");
        zaos.write(deflatedData, 0, deflatedData.length);
        zaos.closeArchiveEntry();

        zaos.setComment("RAF Zip Comment");
        zaos.finish();
        zaos.close();

        Assert.assertTrue(tempFile.length() > 0);
        Assert.assertEquals(storedData.length, storedEntry.getSize());
        Assert.assertEquals(storedData.length, storedEntry.getCompressedSize());
        Assert.assertEquals(deflatedData.length, deflatedEntry.getSize());
    }

    @Test
    public void testFinishWithUnclosedEntryThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("unclosed.txt");
        zaos.putArchiveEntry(entry);

        try {
            zaos.finish();
            Assert.fail("Expected IOException when finishing with unclosed entry");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("unclosed entries"));
        } finally {
            zaos.closeArchiveEntry();
            zaos.finish();
            zaos.close();
        }
    }

    @Test
    public void testCloseArchiveEntryWhenNoCurrentEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        // Should be a no-op
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
    }

    @Test
    public void testConsecutivePutArchiveEntryAutoClosesPrevious() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry1 = new ZipArchiveEntry("file1.txt");
        zaos.putArchiveEntry(entry1);
        zaos.write("File 1 content".getBytes("UTF-8"), 0, 14);

        ZipArchiveEntry entry2 = new ZipArchiveEntry("file2.txt");
        // putArchiveEntry calls closeArchiveEntry on entry1 internally
        zaos.putArchiveEntry(entry2);
        zaos.write("File 2 content".getBytes("UTF-8"), 0, 14);
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();

        Assert.assertEquals(14, entry1.getSize());
        Assert.assertEquals(14, entry2.getSize());
    }

    @Test
    public void testEntryCommentsAndArchiveComment() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        zaos.setComment("Global archive comment");

        ZipArchiveEntry entryWithComment = new ZipArchiveEntry("commented.txt");
        entryWithComment.setComment("Entry level comment");
        zaos.putArchiveEntry(entryWithComment);
        zaos.closeArchiveEntry();

        ZipArchiveEntry entryWithoutComment = new ZipArchiveEntry("no_comment.txt");
        entryWithoutComment.setComment(null);
        zaos.putArchiveEntry(entryWithoutComment);
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
    }

    @Test
    public void testUnicodeExtraFieldPolicyAlwaysAndNeverAndNotEncodeable() throws IOException {
        // ALWAYS policy with ASCII encoding
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("US-ASCII");
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        ZipArchiveEntry entry = new ZipArchiveEntry("ascii.txt");
        entry.setComment("ascii comment");
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        // NOT_ENCODEABLE policy with non-encodable characters
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos2 = new ZipArchiveOutputStream(baos2);
        zaos2.setEncoding("US-ASCII");
        zaos2.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);
        zaos2.setFallbackToUTF8(true);

        ZipArchiveEntry nonAsciiEntry = new ZipArchiveEntry("тест.txt");
        nonAsciiEntry.setComment("коммент");
        zaos2.putArchiveEntry(nonAsciiEntry);
        zaos2.closeArchiveEntry();
        zaos2.finish();
        zaos2.close();

        // NEVER policy
        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos3 = new ZipArchiveOutputStream(baos3);
        zaos3.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER);
        zaos3.setFallbackToUTF8(false);

        ZipArchiveEntry standardEntry = new ZipArchiveEntry("standard.txt");
        zaos3.putArchiveEntry(standardEntry);
        zaos3.closeArchiveEntry();
        zaos3.finish();
        zaos3.close();
    }

    @Test
    public void testUnicodeExtraFieldPolicyToString() {
        Assert.assertEquals("always", ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS.toString());
        Assert.assertEquals("never", ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER.toString());
        Assert.assertEquals("not encodeable", ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE.toString());
    }

    @Test
    public void testCreateArchiveEntryFromFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        File sampleFile = File.createTempFile("sample", ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(sampleFile);
            fos.write("sample content".getBytes("UTF-8"));
            fos.close();

            ArchiveEntry entry = zaos.createArchiveEntry(sampleFile, "custom/name.txt");
            Assert.assertNotNull(entry);
            Assert.assertTrue(entry instanceof ZipArchiveEntry);
            Assert.assertEquals("custom/name.txt", entry.getName());
            Assert.assertEquals(sampleFile.length(), entry.getSize());
        } finally {
            sampleFile.delete();
            zaos.close();
        }
    }

    @Test
    public void testFlushAndClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("flush_test.txt");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3}, 0, 3);
        zaos.flush();
        zaos.closeArchiveEntry();
        zaos.close();
    }

    @Test
    public void testPlatformAttributesInCentralDirectory() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("platform.txt");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        entry.setInternalAttributes(1);
        entry.setExternalAttributes(0100644L << 16);

        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
    }
}
