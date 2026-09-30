package org.apache.commons.compress.archivers.zip;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

public class ZipFileTest {

    private final List<File> tempFiles = new ArrayList<File>();

    @Before
    public void setUp() {
        tempFiles.clear();
    }

    @After
    public void tearDown() {
        for (File f : tempFiles) {
            if (f.exists()) {
                f.delete();
            }
        }
    }

    private File createTempFile(String prefix, String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();
        tempFiles.add(file);
        return file;
    }

    @Test
    public void testReadStandardZipStoredAndDeflated() throws Exception {
        File zipFile = createTempFile("standard", ".zip");
        byte[] storedData = "Hello Stored World!".getBytes("UTF-8");
        byte[] deflatedData = "Hello Deflated World with some repetitive content 1234567890 1234567890!".getBytes("UTF-8");

        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        try {
            ZipArchiveEntry ze1 = new ZipArchiveEntry("stored.txt");
            ze1.setMethod(ZipArchiveEntry.STORED);
            ze1.setSize(storedData.length);
            ze1.setCompressedSize(storedData.length);
            CRC32 crc = new CRC32();
            crc.update(storedData);
            ze1.setCrc(crc.getValue());
            ze1.setComment("Stored entry comment");
            zos.putArchiveEntry(ze1);
            zos.write(storedData);
            zos.closeArchiveEntry();

            ZipArchiveEntry ze2 = new ZipArchiveEntry("deflated.txt");
            ze2.setMethod(ZipArchiveEntry.DEFLATED);
            ze2.setComment("Deflated entry comment");
            zos.putArchiveEntry(ze2);
            zos.write(deflatedData);
            zos.closeArchiveEntry();
        } finally {
            zos.close();
        }

        ZipFile zf = new ZipFile(zipFile);
        try {
            Assert.assertEquals(ZipEncodingHelper.UTF8, zf.getEncoding());

            ZipArchiveEntry e1 = zf.getEntry("stored.txt");
            Assert.assertNotNull(e1);
            Assert.assertEquals(ZipArchiveEntry.STORED, e1.getMethod());
            Assert.assertEquals("Stored entry comment", e1.getComment());
            Assert.assertTrue(zf.canReadEntryData(e1));

            InputStream is1 = zf.getInputStream(e1);
            Assert.assertNotNull(is1);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            int b;
            while ((b = is1.read()) != -1) {
                baos1.write(b);
            }
            is1.close();
            Assert.assertArrayEquals(storedData, baos1.toByteArray());

            ZipArchiveEntry e2 = zf.getEntry("deflated.txt");
            Assert.assertNotNull(e2);
            Assert.assertEquals(ZipArchiveEntry.DEFLATED, e2.getMethod());
            Assert.assertEquals("Deflated entry comment", e2.getComment());
            Assert.assertTrue(zf.canReadEntryData(e2));

            InputStream is2 = zf.getInputStream(e2);
            Assert.assertNotNull(is2);
            byte[] buf = new byte[16];
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            int read;
            while ((read = is2.read(buf, 0, buf.length)) != -1) {
                baos2.write(buf, 0, read);
            }
            Assert.assertEquals(0, is2.read(buf, 0, 0));
            Assert.assertEquals(-1, is2.read());
            is2.close();
            Assert.assertArrayEquals(deflatedData, baos2.toByteArray());

            // Check non-existing entry
            Assert.assertNull(zf.getEntry("non-existing.txt"));

            // Check entry enumeration
            Enumeration<ZipArchiveEntry> entries = zf.getEntries();
            List<String> names = new ArrayList<String>();
            while (entries.hasMoreElements()) {
                names.add(entries.nextElement().getName());
            }
            Assert.assertEquals(2, names.size());
            Assert.assertEquals("stored.txt", names.get(0));
            Assert.assertEquals("deflated.txt", names.get(1));

            // Check physical order enumeration
            Enumeration<ZipArchiveEntry> physicalEntries = zf.getEntriesInPhysicalOrder();
            List<String> physNames = new ArrayList<String>();
            while (physicalEntries.hasMoreElements()) {
                physNames.add(physicalEntries.nextElement().getName());
            }
            Assert.assertEquals(2, physNames.size());
            Assert.assertEquals("stored.txt", physNames.get(0));
            Assert.assertEquals("deflated.txt", physNames.get(1));
        } finally {
            zf.close();
        }
    }

    @Test
    public void testConstructors() throws Exception {
        File zipFile = createTempFile("constructors", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[]{'a', 'b', 'c'});
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf1 = new ZipFile(zipFile.getAbsolutePath());
        Assert.assertNotNull(zf1.getEntry("test.txt"));
        zf1.close();

        ZipFile zf2 = new ZipFile(zipFile.getAbsolutePath(), "UTF-8");
        Assert.assertEquals("UTF-8", zf2.getEncoding());
        zf2.close();

        ZipFile zf3 = new ZipFile(zipFile, "UTF-8");
        Assert.assertEquals("UTF-8", zf3.getEncoding());
        zf3.close();

        ZipFile zf4 = new ZipFile(zipFile, "UTF-8", false);
        Assert.assertEquals("UTF-8", zf4.getEncoding());
        zf4.close();
    }

    @Test
    public void testCloseQuietly() throws Exception {
        ZipFile.closeQuietly(null);

        File zipFile = createTempFile("quietly", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        ZipArchiveEntry entry = new ZipArchiveEntry("entry.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[]{1, 2, 3});
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        ZipFile.closeQuietly(zf);
    }

    @Test
    public void testGetInputStreamForUnknownEntry() throws Exception {
        File zipFile = createTempFile("unknown_entry", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        ZipArchiveEntry entry = new ZipArchiveEntry("present.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[]{1});
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        try {
            ZipArchiveEntry foreignEntry = new ZipArchiveEntry("foreign.txt");
            InputStream is = zf.getInputStream(foreignEntry);
            Assert.assertNull(is);
        } finally {
            zf.close();
        }
    }

    @Test
    public void testUnsupportedCompressionMethod() throws Exception {
        File zipFile = createTempFile("unsupported_method", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        ZipArchiveEntry entry = new ZipArchiveEntry("custom.bin");
        entry.setMethod(ZipArchiveEntry.STORED);
        entry.setSize(4);
        entry.setCompressedSize(4);
        CRC32 crc = new CRC32();
        crc.update(new byte[]{1, 2, 3, 4});
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(new byte[]{1, 2, 3, 4});
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        try {
            ZipArchiveEntry ze = zf.getEntry("custom.bin");
            ze.setMethod(99); // Unsupported compression method
            try {
                zf.getInputStream(ze);
                Assert.fail("Expected ZipException for unsupported method");
            } catch (ZipException expected) {
                Assert.assertTrue(expected.getMessage().contains("unsupported compression method"));
            }
        } finally {
            zf.close();
        }
    }

    @Test(expected = ZipException.class)
    public void testNonZipFileThrowsException() throws Exception {
        File nonZip = createTempFile("invalid", ".zip");
        FileOutputStream fos = new FileOutputStream(nonZip);
        fos.write("This is not a zip file content at all. Just plain text to fail parsing.".getBytes("UTF-8"));
        fos.close();

        new ZipFile(nonZip);
    }

    @Test
    public void testZip64ArchiveReading() throws Exception {
        File zipFile = createTempFile("zip64", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        zos.setUseZip64(Zip64Mode.Always);

        byte[] data1 = "Zip64 First Entry Content".getBytes("UTF-8");
        byte[] data2 = "Zip64 Second Entry Content".getBytes("UTF-8");

        ZipArchiveEntry ze1 = new ZipArchiveEntry("z64_1.txt");
        ze1.setMethod(ZipArchiveEntry.DEFLATED);
        zos.putArchiveEntry(ze1);
        zos.write(data1);
        zos.closeArchiveEntry();

        ZipArchiveEntry ze2 = new ZipArchiveEntry("z64_2.txt");
        ze2.setMethod(ZipArchiveEntry.STORED);
        ze2.setSize(data2.length);
        ze2.setCompressedSize(data2.length);
        CRC32 crc = new CRC32();
        crc.update(data2);
        ze2.setCrc(crc.getValue());
        zos.putArchiveEntry(ze2);
        zos.write(data2);
        zos.closeArchiveEntry();

        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        try {
            ZipArchiveEntry e1 = zf.getEntry("z64_1.txt");
            Assert.assertNotNull(e1);
            InputStream is1 = zf.getInputStream(e1);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            byte[] buf = new byte[64];
            int r;
            while ((r = is1.read(buf)) != -1) {
                baos1.write(buf, 0, r);
            }
            is1.close();
            Assert.assertArrayEquals(data1, baos1.toByteArray());

            ZipArchiveEntry e2 = zf.getEntry("z64_2.txt");
            Assert.assertNotNull(e2);
            InputStream is2 = zf.getInputStream(e2);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            while ((r = is2.read(buf)) != -1) {
                baos2.write(buf, 0, r);
            }
            is2.close();
            Assert.assertArrayEquals(data2, baos2.toByteArray());
        } finally {
            zf.close();
        }
    }

    @Test
    public void testUnicodeExtraFieldsResolution() throws Exception {
        File zipFile = createTempFile("unicode_extra", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        zos.setEncoding("CP437");
        zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        ZipArchiveEntry entry = new ZipArchiveEntry("ü_test_ä.txt");
        entry.setComment("ü_comment_ö");
        byte[] data = "some content".getBytes("UTF-8");
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(zipFile, "CP437", true);
        try {
            ZipArchiveEntry readEntry = zf.getEntry("ü_test_ä.txt");
            Assert.assertNotNull("Unicode extra field should update the name in nameMap", readEntry);
            Assert.assertEquals("ü_comment_ö", readEntry.getComment());
        } finally {
            zf.close();
        }
    }

    @Test
    public void testEmptyCentralDirectoryWithLocalFileHeaderThrowsException() throws Exception {
        File zipFile = createTempFile("empty_cd", ".zip");
        FileOutputStream fos = new FileOutputStream(zipFile);
        // Write LFH signature
        fos.write(ZipArchiveOutputStream.LFH_SIG);
        // Write zeros to make it look like local header
        fos.write(new byte[26]);
        // Write End of Central Directory with 0 central directory size and 0 entries
        byte[] eocd = new byte[]{
            'P', 'K', 5, 6, // EOCD signature
            0, 0, // disk number
            0, 0, // disk with start of CD
            0, 0, // total entries on disk
            0, 0, // total entries
            0, 0, 0, 0, // CD size
            30, 0, 0, 0, // CD offset (pointing to byte 30 which is EOCD itself, sig won't match CFH)
            0, 0 // comment length
        };
        fos.write(eocd);
        fos.close();

        try {
            new ZipFile(zipFile);
            Assert.fail("Expected IOException due to corrupt archive with empty central directory");
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().contains("central directory is empty"));
        }
    }

    @Test
    public void testCorruptZip64EocdLocator() throws Exception {
        File zipFile = createTempFile("corrupt_zip64", ".zip");
        FileOutputStream fos = new FileOutputStream(zipFile);

        // Put a fake zip64 EOCD locator
        byte[] zip64Locator = new byte[]{
            'P', 'K', 6, 7, // zip64 locator sig
            0, 0, 0, 0,     // disk number with zip64 EOCD
            0, 0, 0, 0, 0, 0, 0, 0, // offset of zip64 EOCD (at 0, but 0 won't have zip64 EOCD sig)
            1, 0, 0, 0      // total disks
        };
        fos.write(zip64Locator);

        // Put normal EOCD
        byte[] eocd = new byte[]{
            'P', 'K', 5, 6,
            0, 0,
            0, 0,
            0, 0,
            0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0
        };
        fos.write(eocd);
        fos.close();

        try {
            new ZipFile(zipFile);
            Assert.fail("Expected ZipException for corrupt ZIP64 locator");
        } catch (ZipException expected) {
            Assert.assertTrue(expected.getMessage().contains("archive's ZIP64 end of central directory locator is corrupt"));
        }
    }

    @Test
    public void testFinalizeClosesArchive() throws Throwable {
        File zipFile = createTempFile("finalize_test", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        ZipArchiveEntry entry = new ZipArchiveEntry("sample.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[]{1, 2, 3});
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        try {
            zf.finalize();
        } finally {
            zf.close();
        }
    }

    @Test
    public void testOffsetComparator() throws Exception {
        File zipFile = createTempFile("comparator_test", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);

        ZipArchiveEntry e1 = new ZipArchiveEntry("first.txt");
        zos.putArchiveEntry(e1);
        zos.write("first".getBytes("UTF-8"));
        zos.closeArchiveEntry();

        ZipArchiveEntry e2 = new ZipArchiveEntry("second.txt");
        zos.putArchiveEntry(e2);
        zos.write("second".getBytes("UTF-8"));
        zos.closeArchiveEntry();

        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        try {
            Enumeration<ZipArchiveEntry> physicalOrder = zf.getEntriesInPhysicalOrder();
            Assert.assertTrue(physicalOrder.hasMoreElements());
            ZipArchiveEntry readE1 = physicalOrder.nextElement();
            Assert.assertEquals("first.txt", readE1.getName());
            Assert.assertTrue(physicalOrder.hasMoreElements());
            ZipArchiveEntry readE2 = physicalOrder.nextElement();
            Assert.assertEquals("second.txt", readE2.getName());
            Assert.assertFalse(physicalOrder.hasMoreElements());
        } finally {
            zf.close();
        }
    }

    @Test
    public void testBoundedInputStreamDummyByte() throws Exception {
        File zipFile = createTempFile("dummy_byte_test", ".zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        ZipArchiveEntry entry = new ZipArchiveEntry("test_dummy.txt");
        entry.setMethod(ZipArchiveEntry.DEFLATED);
        zos.putArchiveEntry(entry);
        zos.write("test with deflated stream requesting dummy byte at end".getBytes("UTF-8"));
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        try {
            ZipArchiveEntry ze = zf.getEntry("test_dummy.txt");
            InputStream is = zf.getInputStream(ze);
            byte[] buffer = new byte[1024];
            int totalRead = 0;
            int r;
            while ((r = is.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
                totalRead += r;
            }
            Assert.assertTrue(totalRead > 0);
            is.close();
        } finally {
            zf.close();
        }
    }
}
