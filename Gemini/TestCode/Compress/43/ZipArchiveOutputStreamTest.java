package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class ZipArchiveOutputStreamTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testDeflatedEntryToOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        Assert.assertFalse(zaos.isSeekable());
        Assert.assertEquals("UTF-8", zaos.getEncoding());

        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("Hello World!".getBytes("UTF-8"));
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        Assert.assertTrue(baos.toByteArray().length > 0);
    }

    @Test
    public void testStoredEntryToOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data = "Stored data test".getBytes("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        Assert.assertTrue(baos.toByteArray().length > 0);
    }

    @Test(expected = ZipException.class)
    public void testStoredWithoutSizeThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
    }

    @Test(expected = ZipException.class)
    public void testStoredWithoutCrcThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(10);
        zaos.putArchiveEntry(entry);
    }

    @Test(expected = ZipException.class)
    public void testStoredCrcMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        byte[] data = "Hello".getBytes("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCrc(12345L);

        zaos.putArchiveEntry(entry);
        zaos.write(data);
        zaos.closeArchiveEntry();
    }

    @Test(expected = ZipException.class)
    public void testStoredSizeMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        byte[] data = "Hello".getBytes("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(100);
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());

        zaos.putArchiveEntry(entry);
        zaos.write(data);
        zaos.closeArchiveEntry();
    }

    @Test
    public void testFileChannelConstructorAndStored() throws IOException {
        File file = tempFolder.newFile("test.zip");
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(file);
        Assert.assertTrue(zaos.isSeekable());

        ZipArchiveEntry entry = new ZipArchiveEntry("seekableStored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.write("Seekable test data".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();

        Assert.assertTrue(file.length() > 0);
    }

    @Test
    public void testSeekableInMemoryByteChannel() throws IOException {
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(channel);
        Assert.assertTrue(zaos.isSeekable());

        ZipArchiveEntry entry = new ZipArchiveEntry("file1.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("in-memory-channel-data".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();

        Assert.assertTrue(channel.size() > 0);
    }

    @Test
    public void testZip64AlwaysMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseZip64(Zip64Mode.Always);

        ZipArchiveEntry entry = new ZipArchiveEntry("zip64.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("zip64 test".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testZip64AlwaysSeekableMode() throws IOException {
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(channel);
        zaos.setUseZip64(Zip64Mode.Always);

        ZipArchiveEntry entry = new ZipArchiveEntry("zip64_seekable.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("zip64 seekable test".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
        Assert.assertTrue(channel.size() > 0);
    }

    @Test(expected = Zip64RequiredException.class)
    public void testZip64NeverModeWithLargeEntryThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setUseZip64(Zip64Mode.Never);

        ZipArchiveEntry entry = new ZipArchiveEntry("large.txt");
        entry.setSize(ZipConstants.ZIP64_MAGIC + 1L);
        zaos.putArchiveEntry(entry);
    }

    @Test
    public void testUnicodeExtraFieldsPolicy() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
        zaos.setEncoding("US-ASCII");

        ZipArchiveEntry entry = new ZipArchiveEntry("unicode_test.txt");
        entry.setComment("Unicode Comment");
        zaos.putArchiveEntry(entry);
        zaos.write("test".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();

        Assert.assertEquals("always", ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS.toString());
        Assert.assertEquals("never", ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER.toString());
        Assert.assertEquals("not encodeable", ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE.toString());
    }

    @Test
    public void testFallbackToUTF8() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("US-ASCII");
        zaos.setFallbackToUTF8(true);
        zaos.setUseLanguageEncodingFlag(true);

        ZipArchiveEntry entry = new ZipArchiveEntry("test_non_ascii_\u00E4\u00F6\u00FC.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("fallback test".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
    }

    @Test
    public void testAlignmentExtraField() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("aligned.bin");
        entry.setAlignment(16);
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3, 4});
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
    }

    @Test
    public void testAddRawArchiveEntry() throws IOException {
        byte[] content = "Raw payload".getBytes("UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("raw.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());

        zaos.addRawArchiveEntry(entry, new ByteArrayInputStream(content));
        zaos.finish();
        zaos.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testAddRawArchiveEntryWithZip64() throws IOException {
        byte[] content = "Raw Zip64 payload".getBytes("UTF-8");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry = new ZipArchiveEntry("raw64.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        entry.addExtraField(new Zip64ExtendedInformationExtraField());

        zaos.addRawArchiveEntry(entry, new ByteArrayInputStream(content));
        zaos.finish();
        zaos.close();

        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testCanWriteEntryData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry validEntry = new ZipArchiveEntry("valid.txt");
        Assert.assertTrue(zaos.canWriteEntryData(validEntry));

        ZipArchiveEntry unshrinking = new ZipArchiveEntry("unshrink.txt");
        unshrinking.setMethod(ZipMethod.UNSHRINKING.getCode());
        Assert.assertFalse(zaos.canWriteEntryData(unshrinking));

        ZipArchiveEntry imploding = new ZipArchiveEntry("implode.txt");
        imploding.setMethod(ZipMethod.IMPLODING.getCode());
        Assert.assertFalse(zaos.canWriteEntryData(imploding));

        ArchiveEntry nonZipEntry = new ArchiveEntry() {
            public String getName() { return "other"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
        };
        Assert.assertFalse(zaos.canWriteEntryData(nonZipEntry));
    }

    @Test
    public void testSetLevelAndMethod() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(Deflater.BEST_SPEED);
        zaos.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.setComment("Archive comment test");

        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[]{1, 2, 3});
        zaos.closeArchiveEntry();

        zaos.setLevel(Deflater.BEST_COMPRESSION);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("file2.txt");
        zaos.putArchiveEntry(entry2);
        zaos.write(new byte[]{4, 5, 6});
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidLevelThrows() {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        zaos.setLevel(15);
    }

    @Test(expected = IOException.class)
    public void testFinishTwiceThrows() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        zaos.finish();
        zaos.finish();
    }

    @Test(expected = IOException.class)
    public void testFinishWithOpenEntryThrows() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        zaos.putArchiveEntry(new ZipArchiveEntry("test.txt"));
        zaos.finish();
    }

    @Test(expected = IOException.class)
    public void testCloseArchiveEntryWithoutOpenEntryThrows() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        zaos.closeArchiveEntry();
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteWithoutOpenEntryThrows() throws IOException {
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        zaos.write(new byte[]{1});
    }

    @Test
    public void testAutoCloseOpenEntryOnNewPut() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        zaos.putArchiveEntry(new ZipArchiveEntry("entry1.txt"));
        zaos.write("data1".getBytes("UTF-8"));

        zaos.putArchiveEntry(new ZipArchiveEntry("entry2.txt"));
        zaos.write("data2".getBytes("UTF-8"));
        zaos.closeArchiveEntry();

        zaos.finish();
        zaos.close();
    }

    @Test
    public void testCreateArchiveEntryFromFile() throws IOException {
        File file = tempFolder.newFile("sample.txt");
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        ArchiveEntry entry = zaos.createArchiveEntry(file, "sample.txt");
        Assert.assertNotNull(entry);
        Assert.assertEquals("sample.txt", entry.getName());
        zaos.close();
    }

    @Test(expected = IOException.class)
    public void testCreateArchiveEntryAfterCloseThrows() throws IOException {
        File file = tempFolder.newFile("sample.txt");
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new ByteArrayOutputStream());
        zaos.close();
        zaos.createArchiveEntry(file, "sample.txt");
    }

    @Test
    public void testEmptyEntryCreation() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("empty.txt");
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
        Assert.assertTrue(baos.size() > 0);
    }

    @Test
    public void testFlushAndDeflate() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("flush.txt");
        zaos.putArchiveEntry(entry);
        zaos.write("test".getBytes("UTF-8"));
        zaos.deflate();
        zaos.flush();
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
    }
}
