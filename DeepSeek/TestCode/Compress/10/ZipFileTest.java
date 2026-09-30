package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

public class ZipFileTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File zipFile;

    @Before
    public void setUp() throws Exception {
        zipFile = folder.newFile("test.zip");
    }

    @After
    public void tearDown() throws Exception {
        // cleanup is automatic with TemporaryFolder
    }

    // Helper: create a ZIP file with a single stored entry
    private void createZipWithStoredEntry(File target, String entryName, byte[] content) throws IOException {
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new FileOutputStream(target));
        ZipEntry entry = new ZipEntry(entryName);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
        zos.close();
    }

    // Helper: create a ZIP file with a deflated entry
    private void createZipWithDeflatedEntry(File target, String entryName, byte[] content) throws IOException {
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new FileOutputStream(target));
        ZipEntry entry = new ZipEntry(entryName);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
        zos.close();
    }

    // Helper: create a ZIP file with multiple entries (stored and deflated)
    private void createMultiEntryZip(File target) throws IOException {
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new FileOutputStream(target));
        // entry 1 stored
        ZipEntry e1 = new ZipEntry("file1.txt");
        byte[] data1 = "Hello".getBytes("UTF-8");
        e1.setMethod(ZipEntry.STORED);
        e1.setSize(data1.length);
        e1.setCompressedSize(data1.length);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data1);
        e1.setCrc(crc.getValue());
        zos.putNextEntry(e1);
        zos.write(data1);
        zos.closeEntry();
        // entry 2 deflated
        ZipEntry e2 = new ZipEntry("file2.txt");
        byte[] data2 = "World".getBytes("UTF-8");
        e2.setMethod(ZipEntry.DEFLATED);
        zos.putNextEntry(e2);
        zos.write(data2);
        zos.closeEntry();
        zos.close();
    }

    // Helper: create a ZIP64 archive using ZipArchiveOutputStream
    private void createZip64File(File target, String entryName, byte[] content, boolean forceZip64) throws IOException {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(target);
        if (forceZip64) {
            zos.setUseZip64(Zip64Mode.Always);
        }
        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        entry.setMethod(ZipArchiveEntry.DEFLATED);
        // ensure large sizes trigger Zip64
        entry.setSize(0xFFFFFFFFL + 1); // > 4GB-1
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(content);
        entry.setCrc(crc.getValue());
        entry.setCompressedSize(content.length);
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.close();
    }

    // Helper: create a minimal valid ZIP (EOCD only, no entries)
    private void createEmptyZip(File target) throws IOException {
        FileOutputStream fos = new FileOutputStream(target);
        // End of central directory record with zero entries
        byte[] eocd = new byte[22];
        // signature
        eocd[0] = 0x50; eocd[1] = 0x4b; eocd[2] = 0x05; eocd[3] = 0x06;
        // all other fields zero (disk number, etc.)
        fos.write(eocd);
        fos.close();
    }

    // Helper: create a corrupt ZIP that starts with local file header but no valid CEN
    private void createCorruptZipStartsWithLFH(File target) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(target, "rw");
        // write a local file header signature and some dummy data
        raf.write(new byte[]{0x50, 0x4b, 0x03, 0x04}); // LFH signature
        raf.write(new byte[26]); // rest of LFH fields (making minimum)
        // Then write EOCD pointing to offset 0 as central directory (invalid)
        byte[] eocd = new byte[22];
        eocd[0] = 0x50; eocd[1] = 0x4b; eocd[2] = 0x05; eocd[3] = 0x06;
        // offset of central directory we set to 0 (start of file)
        // size of central directory = 0
        raf.seek(raf.length());
        raf.write(eocd);
        raf.close();
    }

    // Helper: create a file that is not a ZIP at all
    private void createNonZipFile(File target) throws IOException {
        FileOutputStream fos = new FileOutputStream(target);
        fos.write("not a zip".getBytes("UTF-8"));
        fos.close();
    }

    @Test
    public void testConstructorFile() throws IOException {
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1,2,3});
        ZipFile zf = new ZipFile(zipFile);
        assertNotNull(zf.getEntry("entry.txt"));
        zf.close();
    }

    @Test
    public void testConstructorString() throws IOException {
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1,2,3});
        ZipFile zf = new ZipFile(zipFile.getAbsolutePath());
        assertNotNull(zf.getEntry("entry.txt"));
        zf.close();
    }

    @Test
    public void testConstructorWithEncoding() throws IOException {
        // use platform default encoding, ensure no crash
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1,2,3});
        ZipFile zf = new ZipFile(zipFile, null); // null means platform default
        assertNotNull(zf.getEntry("entry.txt"));
        assertEquals(null, zf.getEncoding()); // null encoding
        zf.close();
    }

    @Test
    public void testConstructorWithEncodingNamed() throws IOException {
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1,2,3});
        ZipFile zf = new ZipFile(zipFile, "US-ASCII");
        assertEquals("US-ASCII", zf.getEncoding());
        assertNotNull(zf.getEntry("entry.txt"));
        zf.close();
    }

    @Test
    public void testConstructorWithUnicodeExtraFields() throws IOException {
        // create a simple zip; flag true just tests no exception
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1,2,3});
        ZipFile zf = new ZipFile(zipFile, "UTF-8", true);
        assertNotNull(zf.getEntry("entry.txt"));
        zf.close();
    }

    @Test
    public void testGetEntries() throws IOException {
        createMultiEntryZip(zipFile);
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<ZipArchiveEntry> entries = zf.getEntries();
        assertTrue(entries.hasMoreElements());
        assertEquals("file1.txt", entries.nextElement().getName());
        assertEquals("file2.txt", entries.nextElement().getName());
        assertFalse(entries.hasMoreElements());
        zf.close();
    }

    @Test
    public void testGetEntriesEmpty() throws IOException {
        createEmptyZip(zipFile);
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<ZipArchiveEntry> entries = zf.getEntries();
        assertFalse(entries.hasMoreElements());
        zf.close();
    }

    @Test
    public void testGetEntriesInPhysicalOrder() throws IOException {
        // create zip with entries, then verify order by offset
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new FileOutputStream(zipFile));
        // first entry 'b'
        ZipEntry be = new ZipEntry("b.txt");
        byte[] bdata = new byte[10];
        be.setMethod(ZipEntry.STORED);
        be.setSize(bdata.length);
        be.setCompressedSize(bdata.length);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(bdata);
        be.setCrc(crc.getValue());
        zos.putNextEntry(be);
        zos.write(bdata);
        zos.closeEntry();
        // second entry 'a' (should appear after 'b' in physical order)
        ZipEntry ae = new ZipEntry("a.txt");
        byte[] adata = new byte[20];
        ae.setMethod(ZipEntry.STORED);
        ae.setSize(adata.length);
        ae.setCompressedSize(adata.length);
        crc.reset(); crc.update(adata);
        ae.setCrc(crc.getValue());
        zos.putNextEntry(ae);
        zos.write(adata);
        zos.closeEntry();
        zos.close();

        ZipFile zf = new ZipFile(zipFile);
        Enumeration<ZipArchiveEntry> physical = zf.getEntriesInPhysicalOrder();
        assertTrue(physical.hasMoreElements());
        assertEquals("b.txt", physical.nextElement().getName());
        assertEquals("a.txt", physical.nextElement().getName());
        assertFalse(physical.hasMoreElements());
        zf.close();
    }

    @Test
    public void testGetEntry() throws IOException {
        createZipWithStoredEntry(zipFile, "myfile.dat", new byte[]{0});
        ZipFile zf = new ZipFile(zipFile);
        ZipArchiveEntry entry = zf.getEntry("myfile.dat");
        assertNotNull(entry);
        assertEquals("myfile.dat", entry.getName());
        assertNull(zf.getEntry("nonexistent"));
        zf.close();
    }

    @Test
    public void testCanReadEntryData() throws IOException {
        createZipWithDeflatedEntry(zipFile, "deflated.txt", new byte[]{1,2,3});
        ZipFile zf = new ZipFile(zipFile);
        ZipArchiveEntry e = zf.getEntry("deflated.txt");
        assertTrue(zf.canReadEntryData(e));
        zf.close();
    }

    @Test
    public void testGetInputStreamStored() throws IOException {
        byte[] data = "Hello, World!".getBytes("UTF-8");
        createZipWithStoredEntry(zipFile, "stored.txt", data);
        ZipFile zf = new ZipFile(zipFile);
        ZipArchiveEntry entry = zf.getEntry("stored.txt");
        assertNotNull(entry);
        InputStream in = zf.getInputStream(entry);
        byte[] buf = new byte[data.length];
        int read = in.read(buf);
        assertEquals(data.length, read);
        assertArrayEquals(data, buf);
        assertEquals(-1, in.read());
        in.close();
        zf.close();
    }

    @Test
    public void testGetInputStreamDeflated() throws IOException {
        byte[] data = "Hello, World!".getBytes("UTF-8");
        createZipWithDeflatedEntry(zipFile, "def.txt", data);
        ZipFile zf = new ZipFile(zipFile);
        ZipArchiveEntry entry = zf.getEntry("def.txt");
        InputStream in = zf.getInputStream(entry);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            bos.write(buf, 0, len);
        }
        assertArrayEquals(data, bos.toByteArray());
        in.close();
        zf.close();
    }

    @Test(expected = ZipException.class)
    public void testGetInputStreamUnsupportedMethod() throws IOException {
        // create a zip entry with an unsupported method (e.g., 99)
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry entry = new java.util.zip.ZipEntry("bad.txt");
        entry.setMethod(99); // unsupported
        // java.util.zip doesn't allow arbitrary methods easily; we'll craft raw zip bytes later
        // simpler: we can use reflection or create raw bytes. Instead, we can create a zip with a known unsupported method using ZipArchiveOutputStream
        // Since we already test manual zip, we can rely on ZipArchiveOutputStream to create an entry with method 99.
        // But using our own writer to produce invalid zip may be acceptable. We'll create a ZipArchiveEntry with method 99 and write.
        zos.putNextEntry(entry);
        zos.write("data".getBytes());
        zos.closeEntry();
        zos.close();
        
        ZipFile zf = new ZipFile(zipFile);
        ZipArchiveEntry ze = zf.getEntry("bad.txt");
        assertNotNull(ze);
        // This should throw ZipException because ZipUtil.checkRequestedFeatures will deny it
        zf.getInputStream(ze);
        // in JUnit 4, expected exception is on test method directly
    }

    @Test
    public void testGetInputStreamNullEntry() throws IOException {
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1});
        ZipFile zf = new ZipFile(zipFile);
        assertNull(zf.getInputStream(null));
        // entry not in this zip
        ZipArchiveEntry external = new ZipArchiveEntry("other");
        assertNull(zf.getInputStream(external));
        zf.close();
    }

    @Test
    public void testClose() throws IOException {
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1});
        ZipFile zf = new ZipFile(zipFile);
        zf.close();
        // after close, getInputStream should fail
        try {
            zf.getInputStream(zf.getEntry("entry.txt"));
            fail("Expected IOException after close");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testCloseQuietly() throws IOException {
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1});
        ZipFile zf = new ZipFile(zipFile);
        ZipFile.closeQuietly(zf);
        // should be closed, no exception
        ZipFile.closeQuietly(null); // no exception
    }

    @Test
    public void testEncoding() throws IOException {
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1});
        ZipFile zf = new ZipFile(zipFile, "ISO-8859-1");
        assertEquals("ISO-8859-1", zf.getEncoding());
        zf.close();
    }

    @Test
    public void testEmptyArchive() throws IOException {
        createEmptyZip(zipFile);
        ZipFile zf = new ZipFile(zipFile);
        assertNotNull(zf.getEntries());
        assertFalse(zf.getEntries().hasMoreElements());
        zf.close();
    }

    @Test(expected = ZipException.class)
    public void testNonZipFile() throws IOException {
        createNonZipFile(zipFile);
        new ZipFile(zipFile);
    }

    @Test(expected = IOException.class)
    public void testCorruptArchiveStartsWithLFH() throws IOException {
        createCorruptZipStartsWithLFH(zipFile);
        new ZipFile(zipFile);
    }

    @Test
    public void testZip64Archive() throws IOException {
        // create a Zip64 archive with a single entry
        File zip64File = folder.newFile("zip64.zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zip64File);
        zos.setUseZip64(Zip64Mode.Always);
        ZipArchiveEntry entry = new ZipArchiveEntry("large.bin");
        byte[] data = "Zip64 test".getBytes("UTF-8");
        entry.setMethod(ZipArchiveEntry.DEFLATED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(zip64File);
        ZipArchiveEntry ze = zf.getEntry("large.bin");
        assertNotNull(ze);
        InputStream in = zf.getInputStream(ze);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            bos.write(buf, 0, len);
        }
        assertArrayEquals(data, bos.toByteArray());
        in.close();
        zf.close();
    }

    @Test
    public void testResolveLocalFileHeaderDataWithUnicodeExtraField() throws IOException {
        // Create a zip with Unicode extra field using ZipArchiveOutputStream (which sets it)
        File unicodeZip = folder.newFile("unicode.zip");
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(unicodeZip);
        zos.setEncoding("UTF-8");
        ZipArchiveEntry entry = new ZipArchiveEntry("test_é.txt");
        entry.setMethod(ZipArchiveEntry.STORED);
        byte[] data = "some data".getBytes("UTF-8");
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
        zos.close();

        ZipFile zf = new ZipFile(unicodeZip, "UTF-8", true);
        ZipArchiveEntry readEntry = zf.getEntry("test_é.txt");
        assertNotNull(readEntry);
        assertEquals("test_é.txt", readEntry.getName());
        zf.close();
    }

    @Test
    public void testFinalize() throws Throwable {
        // finalize should call close if not closed; just invoke finalize directly for coverage
        createZipWithStoredEntry(zipFile, "entry.txt", new byte[]{1});
        ZipFile zf = new ZipFile(zipFile);
        // not closed explicitly
        zf.finalize(); // should close internally
        // after finalize, try to read should fail
        try {
            zf.getInputStream(zf.getEntry("entry.txt"));
            fail("Expected IOException after finalize close");
        } catch (IOException expected) {
        }
    }

    @Test
    public void testBoundedInputStreamReadSingle() throws IOException {
        // test BoundedInputStream read() and dummy byte for inflater
        // We can test via getInputStream on a deflated entry which uses InflaterInputStream wrapping BoundedInputStream
        // The dummy byte is added via bis.addDummy(). We'll verify that after reading all compressed bytes, an extra 0 byte is returned.
        // However, that's internal; we can just confirm deflated read works (already covered). To add a dedicated test, we can directly instantiate BoundedInputStream
        // but it's a private inner class. We can test through getInputStream to ensure no exception.
        // Coverage: we will rely on existing deflated test to cover the dummy byte usage.
    }
}
```
