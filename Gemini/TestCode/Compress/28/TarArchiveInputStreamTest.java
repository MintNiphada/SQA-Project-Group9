package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public class TarArchiveInputStreamTest {

    private byte[] createTarArchive(byte[]... entries) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            for (byte[] entry : entries) {
                baos.write(entry);
            }
            // End of archive: 2 records of 512 zeros
            baos.write(new byte[1024]);
            // Pad to block size (10240)
            int mod = baos.size() % TarConstants.DEFAULT_BLKSIZE;
            if (mod != 0) {
                baos.write(new byte[TarConstants.DEFAULT_BLKSIZE - mod]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private byte[] createEntry(String name, byte typeFlag, byte[] content) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] header = new byte[512];

        byte[] nameBytes = name.getBytes();
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));

        // mode
        System.arraycopy("0000777\0".getBytes(), 0, header, 100, 8);
        // uid
        System.arraycopy("0000000\0".getBytes(), 0, header, 108, 8);
        // gid
        System.arraycopy("0000000\0".getBytes(), 0, header, 116, 8);
        // size
        String sizeStr = String.format("%011o", content.length);
        System.arraycopy(sizeStr.getBytes(), 0, header, 124, 11);
        header[135] = 0;
        // mtime
        System.arraycopy("00000000000\0".getBytes(), 0, header, 136, 12);
        // typeflag
        header[156] = typeFlag;

        // magic & version
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, header, 257, 6);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, header, 263, 2);

        // Checksum calculation (places 148-155 as spaces)
        Arrays.fill(header, 148, 156, (byte) ' ');
        long sum = 0;
        for (byte b : header) {
            sum += (b & 0xFF);
        }
        String chkStr = String.format("%06o\0 ", sum);
        System.arraycopy(chkStr.getBytes(), 0, header, 148, 8);

        try {
            baos.write(header);
            baos.write(content);
            // Pad content to 512
            int pad = content.length % 512;
            if (pad != 0) {
                baos.write(new byte[512 - pad]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    @Test
    public void testConstructorsAndGetters() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(is);
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais1.getRecordSize());

        TarArchiveInputStream tais2 = new TarArchiveInputStream(is, "UTF-8");
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais2.getRecordSize());

        TarArchiveInputStream tais3 = new TarArchiveInputStream(is, 1024);
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais3.getRecordSize());

        TarArchiveInputStream tais4 = new TarArchiveInputStream(is, 1024, "UTF-8");
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais4.getRecordSize());

        TarArchiveInputStream tais5 = new TarArchiveInputStream(is, 1024, 512);
        Assert.assertEquals(512, tais5.getRecordSize());

        TarArchiveInputStream tais6 = new TarArchiveInputStream(is, 1024, 512, "UTF-8");
        Assert.assertEquals(512, tais6.getRecordSize());

        tais6.close();
    }

    @Test
    public void testReadSimpleTar() throws IOException {
        byte[] content = "Hello World Tar".getBytes();
        byte[] entry = createEntry("test.txt", TarConstants.LF_NORMAL, content);
        byte[] archive = createTarArchive(entry);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry te = is.getNextTarEntry();
        Assert.assertNotNull(te);
        Assert.assertEquals("test.txt", te.getName());
        Assert.assertEquals(content.length, te.getSize());
        Assert.assertEquals(te, is.getCurrentEntry());
        Assert.assertTrue(is.canReadEntryData(te));

        Assert.assertEquals(content.length, is.available());

        byte[] readBuf = new byte[content.length];
        int bytesRead = is.read(readBuf, 0, readBuf.length);
        Assert.assertEquals(content.length, bytesRead);
        Assert.assertArrayEquals(content, readBuf);

        Assert.assertEquals(0, is.available());
        Assert.assertEquals(-1, is.read(readBuf, 0, 1));

        Assert.assertNull(is.getNextEntry());
        Assert.assertTrue(is.isAtEOF());
        is.close();
    }

    @Test
    public void testSkip() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes();
        byte[] entry = createEntry("skip.txt", TarConstants.LF_NORMAL, content);
        byte[] archive = createTarArchive(entry);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        Assert.assertNotNull(is.getNextTarEntry());

        long skipped = is.skip(5);
        Assert.assertEquals(5, skipped);
        Assert.assertEquals(content.length - 5, is.available());

        byte[] buf = new byte[5];
        int read = is.read(buf, 0, 5);
        Assert.assertEquals(5, read);
        Assert.assertEquals("56789", new String(buf));

        // Skip beyond available
        skipped = is.skip(100);
        Assert.assertEquals(content.length - 10, skipped);
        Assert.assertEquals(0, is.available());
        Assert.assertEquals(-1, is.read(buf, 0, 1));

        is.reset(); // covers reset no-op
        is.close();
    }

    @Test
    public void testSkipRecordPaddingOnMultipleEntries() throws IOException {
        byte[] content1 = new byte[100]; // will require 412 bytes padding
        byte[] content2 = new byte[200];
        byte[] entry1 = createEntry("file1.bin", TarConstants.LF_NORMAL, content1);
        byte[] entry2 = createEntry("file2.bin", TarConstants.LF_NORMAL, content2);
        byte[] archive = createTarArchive(entry1, entry2);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry te1 = is.getNextTarEntry();
        Assert.assertNotNull(te1);
        Assert.assertEquals("file1.bin", te1.getName());

        // Call getNextTarEntry without fully reading file1.bin
        TarArchiveEntry te2 = is.getNextTarEntry();
        Assert.assertNotNull(te2);
        Assert.assertEquals("file2.bin", te2.getName());

        Assert.assertNull(is.getNextTarEntry());
        is.close();
    }

    @Test
    public void testGnuLongLinkAndName() throws IOException {
        String longLinkName = "target/directory/with/very/long/path/which/exceeds/one/hundred/bytes/in/length/for/tar/link.txt";
        String longFileName = "source/directory/with/very/long/path/which/exceeds/one/hundred/bytes/in/length/for/tar/file.txt";

        byte[] linkEntry = createEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGLINK, (longLinkName + "\0").getBytes());
        byte[] nameEntry = createEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGNAME, (longFileName + "\0").getBytes());
        byte[] targetEntry = createEntry("short.txt", TarConstants.LF_SYMLINK, new byte[0]);

        byte[] archive = createTarArchive(linkEntry, nameEntry, targetEntry);
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        TarArchiveEntry te = is.getNextTarEntry();
        Assert.assertNotNull(te);
        Assert.assertEquals(longFileName, te.getName());
        Assert.assertEquals(longLinkName, te.getLinkName());

        is.close();
    }

    @Test
    public void testGnuLongNameMalformedTruncated() throws IOException {
        byte[] linkEntry = createEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGLINK, "malformed_link\0".getBytes());
        // Archive contains only the GNU_LONGLINK without target following
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(linkEntry);
        baos.write(new byte[1024]); // EOF records

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Assert.assertNull(is.getNextTarEntry());
        is.close();

        byte[] nameEntry = createEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGNAME, "malformed_name\0".getBytes());
        baos = new ByteArrayOutputStream();
        baos.write(nameEntry);
        baos.write(new byte[1024]); // EOF records

        is = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Assert.assertNull(is.getNextTarEntry());
        is.close();
    }

    @Test
    public void testPaxHeaders() throws IOException {
        String paxContent = "25 path=new/pax/path.txt\n"
                + "29 linkpath=pax/link/path\n"
                + "11 gid=1001\n"
                + "16 gname=paxgroup\n"
                + "11 uid=2002\n"
                + "15 uname=paxuser\n"
                + "13 size=15\n"
                + "28 mtime=1234567890.123456\n"
                + "20 SCHILY.devminor=5\n"
                + "20 SCHILY.devmajor=8\n";

        byte[] paxEntry = createEntry("pax_header", TarConstants.LF_PAX_EXTENDED_HEADER_LC, paxContent.getBytes(CharsetNames.UTF_8));
        byte[] regularEntry = createEntry("old_path.txt", TarConstants.LF_NORMAL, "123456789012345".getBytes());
        byte[] archive = createTarArchive(paxEntry, regularEntry);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry te = is.getNextTarEntry();
        Assert.assertNotNull(te);
        Assert.assertEquals("new/pax/path.txt", te.getName());
        Assert.assertEquals("pax/link/path", te.getLinkName());
        Assert.assertEquals(1001, te.getGroupId());
        Assert.assertEquals("paxgroup", te.getGroupName());
        Assert.assertEquals(2002, te.getUserId());
        Assert.assertEquals("paxuser", te.getUserName());
        Assert.assertEquals(15, te.getSize());
        Assert.assertEquals(1234567890123L, te.getModTime().getTime());
        Assert.assertEquals(5, te.getDevMinor());
        Assert.assertEquals(8, te.getDevMajor());

        byte[] data = new byte[15];
        int read = is.read(data, 0, 15);
        Assert.assertEquals(15, read);
        Assert.assertEquals("123456789012345", new String(data));

        is.close();
    }

    @Test(expected = IOException.class)
    public void testMalformedPaxHeaderShortRead() throws IOException {
        String paxContent = "50 path=short_content\n";
        byte[] paxEntry = createEntry("pax_header", TarConstants.LF_PAX_EXTENDED_HEADER_LC, paxContent.getBytes());
        byte[] regularEntry = createEntry("file.txt", TarConstants.LF_NORMAL, new byte[0]);
        byte[] archive = createTarArchive(paxEntry, regularEntry);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        is.getNextTarEntry();
    }

    @Test
    public void testParsePaxHeadersDirectly() throws IOException {
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        String paxData = "13 size=100\n16 key=value12\n";
        ByteArrayInputStream bis = new ByteArrayInputStream(paxData.getBytes(CharsetNames.UTF_8));
        Map<String, String> headers = is.parsePaxHeaders(bis);
        Assert.assertEquals("100", headers.get("size"));
        Assert.assertEquals("value12", headers.get("key"));
        is.close();
    }

    @Test(expected = IOException.class)
    public void testInvalidHeaderThrowsIOException() throws IOException {
        byte[] invalidHeader = new byte[512];
        Arrays.fill(invalidHeader, (byte) 'a'); // invalid octals will trigger IllegalArgumentException
        byte[] archive = createTarArchive(invalidHeader);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        is.getNextTarEntry();
    }

    @Test
    public void testReadWithoutCurrentEntry() throws IOException {
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            is.read(new byte[10], 0, 10);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
        is.close();
    }

    @Test
    public void testAvailableOverflowBoundary() throws Exception {
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Field entrySizeField = TarArchiveInputStream.class.getDeclaredField("entrySize");
        entrySizeField.setAccessible(true);
        entrySizeField.setLong(is, Long.MAX_VALUE);

        Field entryOffsetField = TarArchiveInputStream.class.getDeclaredField("entryOffset");
        entryOffsetField.setAccessible(true);
        entryOffsetField.setLong(is, 0L);

        Assert.assertEquals(Integer.MAX_VALUE, is.available());
        is.close();
    }

    @Test
    public void testMatches() {
        byte[] posixSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, posixSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, posixSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(posixSig, 512));

        byte[] gnuSpaceSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuSpaceSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, gnuSpaceSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(gnuSpaceSig, 512));

        byte[] gnuZeroSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuZeroSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, gnuZeroSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(gnuZeroSig, 512));

        byte[] antSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, antSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, antSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(antSig, 512));

        Assert.assertFalse(TarArchiveInputStream.matches(posixSig, TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN - 1));
        Assert.assertFalse(TarArchiveInputStream.matches(new byte[512], 512));
    }

    @Test
    public void testCanReadEntryData() {
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry normalEntry = new TarArchiveEntry("test.txt");
        Assert.assertTrue(is.canReadEntryData(normalEntry));

        TarArchiveEntry sparseEntry = new TarArchiveEntry("sparse.txt", TarConstants.LF_GNUTYPE_SPARSE);
        Assert.assertFalse(is.canReadEntryData(sparseEntry));

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "dummy"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public java.util.Date getLastModifiedDate() { return null; }
        };
        Assert.assertFalse(is.canReadEntryData(nonTarEntry));
    }

    @Test
    public void testGettersSettersAndEofHelpers() {
        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry entry = new TarArchiveEntry("dummy.txt");
        is.setCurrentEntry(entry);
        Assert.assertEquals(entry, is.getCurrentEntry());

        Assert.assertFalse(is.isAtEOF());
        is.setAtEOF(true);
        Assert.assertTrue(is.isAtEOF());
    }

    @Test
    public void testReadGNUSparseTruncated() throws IOException {
        byte[] entryBytes = createEntry("sparse_entry", TarConstants.LF_GNUTYPE_SPARSE, new byte[0]);
        // Set isExtended flag in sparse header (offset 482 is isExtended in TarArchiveEntry/TarHeader)
        entryBytes[482] = 1;
        // Recompute checksum
        Arrays.fill(entryBytes, 148, 156, (byte) ' ');
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            sum += (entryBytes[i] & 0xFF);
        }
        String chkStr = String.format("%06o\0 ", sum);
        System.arraycopy(chkStr.getBytes(), 0, entryBytes, 148, 8);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(entryBytes));
        TarArchiveEntry te = is.getNextTarEntry();
        // Since sparse continuation block is missing/EOF, getRecord() will return null and currEntry becomes null
        Assert.assertNull(te);
        is.close();
    }

    @Test
    public void testMarkSupportedAndSingleEOFRecord() throws IOException {
        byte[] entry = createEntry("one.txt", TarConstants.LF_NORMAL, "hello".getBytes());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(entry);
        baos.write(new byte[512]); // single EOF record instead of two
        // pad non-record
        baos.write(new byte[100]);

        // Wrap stream in a FilterInputStream that does NOT support mark
        InputStream unmarkable = new FilterInputStream(new ByteArrayInputStream(baos.toByteArray())) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };

        TarArchiveInputStream is = new TarArchiveInputStream(unmarkable);
        Assert.assertNotNull(is.getNextTarEntry());
        Assert.assertNull(is.getNextTarEntry());
        is.close();
    }

    @Test
    public void testGetLongNameDataWithTrailingZeros() throws IOException {
        byte[] longNameBytes = "long_name_sample\0\0\0".getBytes();
        byte[] linkEntry = createEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGNAME, longNameBytes);
        byte[] fileEntry = createEntry("normal.txt", TarConstants.LF_NORMAL, new byte[0]);
        byte[] archive = createTarArchive(linkEntry, fileEntry);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry te = is.getNextTarEntry();
        Assert.assertNotNull(te);
        Assert.assertEquals("long_name_sample", te.getName());
        is.close();
    }

    @Test
    public void testReadReturnsMinusOneOnEOF() throws IOException {
        byte[] entry = createEntry("test.txt", TarConstants.LF_NORMAL, new byte[10]);
        // Create an input stream that runs out of bytes earlier than expected
        byte[] truncatedEntry = new byte[520]; // 512 header + 8 bytes only
        System.arraycopy(entry, 0, truncatedEntry, 0, 520);

        TarArchiveInputStream is = new TarArchiveInputStream(new ByteArrayInputStream(truncatedEntry));
        TarArchiveEntry te = is.getNextTarEntry();
        Assert.assertNotNull(te);
        byte[] buf = new byte[10];
        int read = is.read(buf, 0, 10);
        Assert.assertEquals(8, read);
        // Next read will hit stream EOF (-1)
        int eofRead = is.read(buf, 0, 2);
        Assert.assertEquals(-1, eofRead);
        Assert.assertTrue(is.isAtEOF());
        is.close();
    }
}
