package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class TarArchiveInputStreamTest {

    private byte[] createTarArchive(byte[][] headers, byte[][] bodies) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < headers.length; i++) {
            bos.write(headers[i]);
            if (bodies != null && i < bodies.length && bodies[i] != null) {
                bos.write(bodies[i]);
                int remainder = bodies[i].length % 512;
                if (remainder > 0) {
                    bos.write(new byte[512 - remainder]);
                }
            }
        }
        bos.write(new byte[1024]);
        int total = bos.size();
        int pad = total % 10240;
        if (pad > 0) {
            bos.write(new byte[10240 - pad]);
        }
        return bos.toByteArray();
    }

    private byte[] createHeader(String name, long size, byte typeFlag, boolean isExtended) {
        byte[] header = new byte[512];
        TarArchiveEntry entry = new TarArchiveEntry(name, typeFlag);
        entry.setSize(size);
        entry.writeEntryHeader(header);
        if (isExtended) {
            header[482] = 1;
        }
        return header;
    }

    @Test
    public void testConstructorsAndAccessors() throws IOException {
        ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(is);
        Assert.assertEquals(512, tais1.getRecordSize());
        tais1.close();

        TarArchiveInputStream tais2 = new TarArchiveInputStream(is, "UTF-8");
        Assert.assertEquals(512, tais2.getRecordSize());

        TarArchiveInputStream tais3 = new TarArchiveInputStream(is, 1024);
        Assert.assertEquals(512, tais3.getRecordSize());

        TarArchiveInputStream tais4 = new TarArchiveInputStream(is, 1024, "UTF-8");
        Assert.assertEquals(512, tais4.getRecordSize());

        TarArchiveInputStream tais5 = new TarArchiveInputStream(is, 1024, 512);
        Assert.assertEquals(512, tais5.getRecordSize());

        TarArchiveInputStream tais6 = new TarArchiveInputStream(is, 1024, 512, "UTF-8");
        Assert.assertEquals(512, tais6.getRecordSize());
    }

    @Test
    public void testMarkSupportedAndOperations() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertFalse(tais.markSupported());
        tais.mark(100);
        tais.reset();
    }

    @Test
    public void testSettersAndGetters() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertNull(tais.getCurrentEntry());
        Assert.assertFalse(tais.isAtEOF());

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tais.setCurrentEntry(entry);
        Assert.assertSame(entry, tais.getCurrentEntry());

        tais.setAtEOF(true);
        Assert.assertTrue(tais.isAtEOF());
    }

    @Test
    public void testCanReadEntryData() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry normalEntry = new TarArchiveEntry("normal.txt");
        Assert.assertTrue(tais.canReadEntryData(normalEntry));

        TarArchiveEntry sparseEntry = new TarArchiveEntry("sparse.txt", TarConstants.LF_GNUTYPE_SPARSE);
        Assert.assertFalse(tais.canReadEntryData(sparseEntry));

        ArchiveEntry zipEntry = new ZipArchiveEntry("zip.txt");
        Assert.assertFalse(tais.canReadEntryData(zipEntry));
    }

    @Test
    public void testMatches() {
        byte[] validPosix = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, validPosix, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, validPosix, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(validPosix, 512));

        byte[] validGnuSpace = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, validGnuSpace, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, validGnuSpace, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(validGnuSpace, 512));

        byte[] validGnuZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, validGnuZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, validGnuZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(validGnuZero, 512));

        byte[] validAnt = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, validAnt, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, validAnt, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(validAnt, 512));

        byte[] invalid = new byte[512];
        Assert.assertFalse(TarArchiveInputStream.matches(invalid, 512));
        Assert.assertFalse(TarArchiveInputStream.matches(validPosix, 10));
    }

    @Test
    public void testReadEmptyArchive() throws IOException {
        byte[] empty = new byte[1024];
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(empty));
        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertNull(tais.getNextEntry());
        Assert.assertTrue(tais.isAtEOF());
        tais.close();
    }

    @Test
    public void testReadSingleEntryAndReadOperations() throws IOException {
        byte[] content = "Hello World Tar Content Test".getBytes();
        byte[] header = createHeader("test.txt", content.length, TarConstants.LF_NORMAL, false);
        byte[] tar = createTarArchive(new byte[][]{header}, new byte[][]{content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("test.txt", entry.getName());
        Assert.assertEquals(content.length, entry.getSize());
        Assert.assertEquals(content.length, tais.available());

        byte[] readBuf = new byte[content.length];
        int readBytes = tais.read(readBuf, 0, readBuf.length);
        Assert.assertEquals(content.length, readBytes);
        Assert.assertArrayEquals(content, readBuf);

        Assert.assertEquals(0, tais.available());
        Assert.assertEquals(-1, tais.read(readBuf, 0, readBuf.length));
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testSkipOperations() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes();
        byte[] header = createHeader("test_skip.txt", content.length, TarConstants.LF_NORMAL, false);
        byte[] tar = createTarArchive(new byte[][]{header}, new byte[][]{content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);

        Assert.assertEquals(0, tais.skip(-5));
        Assert.assertEquals(0, tais.skip(0));

        long skipped = tais.skip(4);
        Assert.assertEquals(4, skipped);
        Assert.assertEquals(content.length - 4, tais.available());

        byte[] buf = new byte[4];
        int read = tais.read(buf, 0, 4);
        Assert.assertEquals(4, read);
        Assert.assertEquals("4567", new String(buf));

        skipped = tais.skip(100);
        Assert.assertEquals(content.length - 8, skipped);
        Assert.assertEquals(0, tais.available());
        Assert.assertEquals(-1, tais.read(buf, 0, 1));
        tais.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testReadWithoutCurrentEntryThrows() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]));
        tais.read(new byte[10], 0, 10);
    }

    @Test(expected = IOException.class)
    public void testCorruptHeaderThrows() throws IOException {
        byte[] corrupt = new byte[1024];
        for (int i = 0; i < 512; i++) {
            corrupt[i] = (byte) (i + 1);
        }
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(corrupt));
        tais.getNextTarEntry();
    }

    @Test(expected = IOException.class)
    public void testTruncatedEntryStreamThrows() throws IOException {
        byte[] header = createHeader("trunc.txt", 100, TarConstants.LF_NORMAL, false);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(new byte[10]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        byte[] buf = new byte[50];
        tais.read(buf, 0, 50);
        tais.read(buf, 0, 50);
    }

    @Test
    public void testMultipleEntriesAndAutoSkipPadding() throws IOException {
        byte[] c1 = "File 1 Content".getBytes();
        byte[] c2 = "File 2 Longer Content".getBytes();
        byte[] h1 = createHeader("f1.txt", c1.length, TarConstants.LF_NORMAL, false);
        byte[] h2 = createHeader("f2.txt", c2.length, TarConstants.LF_NORMAL, false);
        byte[] tar = createTarArchive(new byte[][]{h1, h2}, new byte[][]{c1, c2});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry e1 = tais.getNextTarEntry();
        Assert.assertNotNull(e1);
        Assert.assertEquals("f1.txt", e1.getName());

        TarArchiveEntry e2 = tais.getNextTarEntry();
        Assert.assertNotNull(e2);
        Assert.assertEquals("f2.txt", e2.getName());

        byte[] buf = new byte[c2.length];
        int r = tais.read(buf, 0, buf.length);
        Assert.assertEquals(c2.length, r);
        Assert.assertArrayEquals(c2, buf);

        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testGNULongNameAndLinkEntries() throws IOException {
        String longName = "very/long/path/name/that/exceeds/the/normal/tar/limit/which/is/one/hundred/characters/long/test_file.txt";
        String longLink = "very/long/target/link/path/that/exceeds/the/normal/tar/limit/which/is/one/hundred/characters/long/link_target";

        byte[] nBytes = (longName + "\0").getBytes();
        byte[] hName = createHeader("././@LongLink", nBytes.length, TarConstants.LF_GNUTYPE_LONGNAME, false);

        byte[] lBytes = (longLink + "\0").getBytes();
        byte[] hLink = createHeader("././@LongLink", lBytes.length, TarConstants.LF_GNUTYPE_LONGLINK, false);

        byte[] c = "Content of long name and link file".getBytes();
        byte[] hReal = createHeader("short.txt", c.length, TarConstants.LF_SYMLINK, false);

        byte[] tar = createTarArchive(new byte[][]{hName, hLink, hReal}, new byte[][]{nBytes, lBytes, c});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(longName, entry.getName());
        Assert.assertEquals(longLink, entry.getLinkName());
        tais.close();
    }

    @Test
    public void testGNULongEntryFollowedByEofReturnsNull() throws IOException {
        byte[] nBytes = "long_name\0".getBytes();
        byte[] hName = createHeader("././@LongLink", nBytes.length, TarConstants.LF_GNUTYPE_LONGNAME, false);
        byte[] tar = createTarArchive(new byte[][]{hName}, new byte[][]{nBytes});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();

        byte[] hLink = createHeader("././@LongLink", nBytes.length, TarConstants.LF_GNUTYPE_LONGLINK, false);
        tar = createTarArchive(new byte[][]{hLink}, new byte[][]{nBytes});
        tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testPaxHeaders() throws IOException {
        String paxData = "25 path=pax/custom.txt\n"
                + "23 linkpath=pax/link\n"
                + "11 gid=123\n"
                + "15 gname=mygroup\n"
                + "11 uid=456\n"
                + "14 uname=myuser\n"
                + "13 size=15\n"
                + "20 mtime=123456789.5\n"
                + "18 SCHILY.devminor=7\n"
                + "18 SCHILY.devmajor=9\n";
        byte[] pBytes = paxData.getBytes("UTF-8");
        byte[] hPax = createHeader("PaxHeader", pBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC, false);

        byte[] c = "123456789012345".getBytes();
        byte[] hReal = createHeader("orig.txt", 10, TarConstants.LF_NORMAL, false);

        byte[] tar = createTarArchive(new byte[][]{hPax, hReal}, new byte[][]{pBytes, c});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("pax/custom.txt", entry.getName());
        Assert.assertEquals("pax/link", entry.getLinkName());
        Assert.assertEquals(123, entry.getGroupId());
        Assert.assertEquals("mygroup", entry.getGroupName());
        Assert.assertEquals(456, entry.getUserId());
        Assert.assertEquals("myuser", entry.getUserName());
        Assert.assertEquals(15, entry.getSize());
        Assert.assertEquals(123456789500L, entry.getModTime().getTime());
        Assert.assertEquals(7, entry.getDevMinor());
        Assert.assertEquals(9, entry.getDevMajor());

        byte[] buf = new byte[15];
        int r = tais.read(buf, 0, 15);
        Assert.assertEquals(15, r);
        Assert.assertArrayEquals(c, buf);
        tais.close();
    }

    @Test(expected = IOException.class)
    public void testPaxHeaderCorruptedLengthThrows() throws IOException {
        String paxData = "999 path=pax/custom.txt\n";
        ByteArrayInputStream is = new ByteArrayInputStream(paxData.getBytes("UTF-8"));
        TarArchiveInputStream tais = new TarArchiveInputStream(is);
        tais.parsePaxHeaders(is);
    }

    @Test
    public void testGNUSparseHeaderReading() throws IOException {
        byte[] hSparse1 = createHeader("sparse.txt", 1000, TarConstants.LF_GNUTYPE_SPARSE, true);
        byte[] sExt1 = new byte[512];
        sExt1[504] = 1;
        byte[] sExt2 = new byte[512];
        sExt2[504] = 0;

        byte[] tar = createTarArchive(new byte[][]{hSparse1, sExt1, sExt2}, null);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tar));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("sparse.txt", entry.getName());
        tais.close();
    }

    @Test
    public void testGNUSparseHeaderEofHandling() throws IOException {
        byte[] hSparse1 = createHeader("sparse.txt", 1000, TarConstants.LF_GNUTYPE_SPARSE, true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(hSparse1);
        bos.write(new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNull(entry);
        tais.close();
    }

    @Test
    public void testIsEOFRecord() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertTrue(tais.isEOFRecord(null));
        Assert.assertTrue(tais.isEOFRecord(new byte[512]));
        byte[] notZero = new byte[512];
        notZero[0] = 1;
        Assert.assertFalse(tais.isEOFRecord(notZero));
    }
}
