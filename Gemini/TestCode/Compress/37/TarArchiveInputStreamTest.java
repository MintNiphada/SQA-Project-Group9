package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class TarArchiveInputStreamTest {

    private byte[] createTarArchive(byte[][] headers, byte[][] bodies) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < headers.length; i++) {
            bos.write(headers[i]);
            if (bodies != null && i < bodies.length && bodies[i] != null && bodies[i].length > 0) {
                bos.write(bodies[i]);
                int pad = 512 - (bodies[i].length % 512);
                if (pad != 512) {
                    bos.write(new byte[pad]);
                }
            }
        }
        bos.write(new byte[1024]);
        int total = bos.size();
        int blockRem = total % (512 * 20);
        if (blockRem > 0) {
            bos.write(new byte[(512 * 20) - blockRem]);
        }
        return bos.toByteArray();
    }

    private byte[] createHeader(String name, long size, byte linkFlag) {
        TarArchiveEntry entry = new TarArchiveEntry(name, linkFlag);
        entry.setSize(size);
        entry.setModTime(1000L);
        byte[] buf = new byte[512];
        entry.writeEntryHeader(buf);
        return buf;
    }

    @Test
    public void testConstructorsAndGetters() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[1024]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(bais);
        Assert.assertEquals(512, tais1.getRecordSize());
        Assert.assertNull(tais1.getCurrentEntry());
        Assert.assertFalse(tais1.isAtEOF());
        tais1.close();

        TarArchiveInputStream tais2 = new TarArchiveInputStream(bais, "UTF-8");
        Assert.assertEquals("UTF-8", tais2.encoding);
        tais2.close();

        TarArchiveInputStream tais3 = new TarArchiveInputStream(bais, 1024);
        Assert.assertEquals(512, tais3.getRecordSize());
        tais3.close();

        TarArchiveInputStream tais4 = new TarArchiveInputStream(bais, 1024, "UTF-8");
        Assert.assertEquals(512, tais4.getRecordSize());
        tais4.close();

        TarArchiveInputStream tais5 = new TarArchiveInputStream(bais, 1024, 512);
        Assert.assertEquals(512, tais5.getRecordSize());
        tais5.close();
    }

    @Test
    public void testMarkAndReset() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[512]));
        Assert.assertFalse(tais.markSupported());
        tais.mark(100);
        tais.reset();
        tais.close();
    }

    @Test
    public void testAvailableAndReadErrors() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[512]));
        Assert.assertEquals(0, tais.available());
        Assert.assertEquals(0, tais.skip(-10));
        Assert.assertEquals(0, tais.skip(0));

        try {
            tais.read(new byte[10], 0, 10);
            Assert.fail("Expected IllegalStateException on read without current entry");
        } catch (IllegalStateException expected) {
        }
        tais.close();
    }

    @Test
    public void testReadStandardEntry() throws IOException {
        byte[] content = "Hello World, Testing TAR stream data!".getBytes("UTF-8");
        byte[] hdr = createHeader("test.txt", content.length, TarConstants.LF_NORMAL);
        byte[] tarData = createTarArchive(new byte[][]{hdr}, new byte[][]{content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
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
        Assert.assertEquals(-1, tais.read(readBuf, 0, 1));

        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertTrue(tais.isAtEOF());
        tais.close();
    }

    @Test
    public void testSkip() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes("UTF-8");
        byte[] hdr = createHeader("skip.txt", content.length, TarConstants.LF_NORMAL);
        byte[] tarData = createTarArchive(new byte[][]{hdr}, new byte[][]{content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        Assert.assertNotNull(tais.getNextEntry());
        long skipped = tais.skip(10);
        Assert.assertEquals(10, skipped);
        Assert.assertEquals(6, tais.available());
        byte[] rem = new byte[6];
        int read = tais.read(rem, 0, 6);
        Assert.assertEquals(6, read);
        Assert.assertEquals("ABCDEF", new String(rem, "UTF-8"));
        tais.close();
    }

    @Test
    public void testDirectoryEntry() throws IOException {
        byte[] hdr = createHeader("folder/", 0, TarConstants.LF_DIR);
        byte[] tarData = createTarArchive(new byte[][]{hdr}, new byte[][]{new byte[0]});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.isDirectory());
        Assert.assertEquals(0, tais.available());
        Assert.assertEquals(0, tais.skip(100));
        Assert.assertEquals(-1, tais.read(new byte[10], 0, 10));
        tais.close();
    }

    @Test
    public void testTruncatedArchive() throws IOException {
        byte[] hdr = createHeader("trunc.txt", 100, TarConstants.LF_NORMAL);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(hdr);
        bos.write("short content".getBytes("UTF-8"));

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Assert.assertNotNull(tais.getNextTarEntry());
        byte[] buf = new byte[100];
        try {
            tais.read(buf, 0, 100);
            tais.read(buf, 0, 100);
            Assert.fail("Expected IOException for truncated tar entry");
        } catch (IOException expected) {
        }
        tais.close();
    }

    @Test
    public void testInvalidHeaderThrowsIOException() {
        byte[] invalidHeader = new byte[512];
        Arrays.fill(invalidHeader, (byte) 'A');
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(invalidHeader));
        try {
            tais.getNextTarEntry();
            Assert.fail("Expected IOException on bad header");
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().contains("Error detected parsing the header"));
        }
    }

    @Test
    public void testGNULongNameAndLinkEntry() throws IOException {
        String longName = "very/long/path/name/that/exceeds/the/standard/one/hundred/characters/limit/in/tar/archives/test.txt";
        byte[] longNameBytes = longName.getBytes("UTF-8");
        byte[] gnuNameHdr = createHeader(TarConstants.GNU_LONGLINK, longNameBytes.length + 1, TarConstants.LF_GNUTYPE_LONGNAME);

        String longLink = "very/long/target/path/name/that/exceeds/the/standard/limit/target.txt";
        byte[] longLinkBytes = longLink.getBytes("UTF-8");
        byte[] gnuLinkHdr = createHeader(TarConstants.GNU_LONGLINK, longLinkBytes.length + 1, TarConstants.LF_GNUTYPE_LONGLINK);

        byte[] finalHdr = createHeader("dummy.txt", 0, TarConstants.LF_SYMLINK);

        byte[] nameBody = new byte[longNameBytes.length + 1];
        System.arraycopy(longNameBytes, 0, nameBody, 0, longNameBytes.length);

        byte[] linkBody = new byte[longLinkBytes.length + 1];
        System.arraycopy(longLinkBytes, 0, linkBody, 0, longLinkBytes.length);

        byte[] tarData = createTarArchive(
                new byte[][]{gnuNameHdr, gnuLinkHdr, finalHdr},
                new byte[][]{nameBody, linkBody, new byte[0]}
        );

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(longName, entry.getName());
        Assert.assertEquals(longLink, entry.getLinkName());
        tais.close();
    }

    @Test
    public void testGNULongNamePrematureEOF() throws IOException {
        byte[] gnuNameHdr = createHeader(TarConstants.GNU_LONGLINK, 20, TarConstants.LF_GNUTYPE_LONGNAME);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(gnuNameHdr);
        bos.write(new byte[512]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testPaxHeaders() throws IOException {
        String paxContent = "25 path=pax/custom/path\n"
                + "21 linkpath=target/path\n"
                + "11 gid=2000\n"
                + "14 gname=custom\n"
                + "11 uid=1000\n"
                + "12 uname=test\n"
                + "13 size=50\n"
                + "18 mtime=123456.789\n"
                + "21 SCHILY.devminor=10\n"
                + "21 SCHILY.devmajor=20\n"
                + "20 GNU.sparse.size=50\n"
                + "24 GNU.sparse.realsize=50\n"
                + "27 SCHILY.filetype=sparse\n"
                + "13 toremove=val\n"
                + "10 toremove\n";

        byte[] paxBytes = paxContent.getBytes("UTF-8");
        byte[] paxHdr = createHeader("PaxHeader/test", paxBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        byte[] actualHdr = createHeader("default.txt", 10, TarConstants.LF_NORMAL);
        byte[] actualBody = new byte[50];

        byte[] tarData = createTarArchive(
                new byte[][]{paxHdr, actualHdr},
                new byte[][]{paxBytes, actualBody}
        );

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("pax/custom/path", entry.getName());
        Assert.assertEquals("target/path", entry.getLinkName());
        Assert.assertEquals(2000L, entry.getGroupId());
        Assert.assertEquals("custom", entry.getGroupName());
        Assert.assertEquals(1000L, entry.getUserId());
        Assert.assertEquals("test", entry.getUserName());
        Assert.assertEquals(50L, entry.getSize());
        Assert.assertEquals(123456789L, entry.getModTime().getTime());
        Assert.assertEquals(10, entry.getDevMinor());
        Assert.assertEquals(20, entry.getDevMajor());
        tais.close();
    }

    @Test
    public void testGlobalPaxHeaders() throws IOException {
        String globalPax = "18 uname=globalUser\n16 gname=global\n";
        byte[] globalPaxBytes = globalPax.getBytes("UTF-8");
        byte[] gHdr = createHeader("GlobalHead", globalPaxBytes.length, TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER);
        byte[] actualHdr = createHeader("entry.txt", 0, TarConstants.LF_NORMAL);

        byte[] tarData = createTarArchive(
                new byte[][]{gHdr, actualHdr},
                new byte[][]{globalPaxBytes, new byte[0]}
        );

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals("globalUser", entry.getUserName());
        Assert.assertEquals("global", entry.getGroupName());
        tais.close();
    }

    @Test
    public void testPaxHeaderCorruptedLength() throws IOException {
        String corruptedPax = "50 path=short\n";
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            tais.parsePaxHeaders(new ByteArrayInputStream(corruptedPax.getBytes("UTF-8")));
            Assert.fail("Expected IOException on truncated Pax header body");
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().contains("Failed to read Paxheader"));
        }
        tais.close();
    }

    @Test
    public void testCanReadEntryData() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry normalEntry = new TarArchiveEntry("test.txt");
        Assert.assertTrue(tais.canReadEntryData(normalEntry));

        TarArchiveEntry sparseEntry = new TarArchiveEntry("sparse.txt", TarConstants.LF_GNUTYPE_SPARSE);
        Assert.assertFalse(tais.canReadEntryData(sparseEntry));

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            public String getName() { return "other"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return null; }
        };
        Assert.assertFalse(tais.canReadEntryData(nonTarEntry));
    }

    @Test
    public void testIsEOFRecordAndSetters() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertTrue(tais.isEOFRecord(null));
        Assert.assertTrue(tais.isEOFRecord(new byte[512]));

        byte[] notZero = new byte[512];
        notZero[0] = 1;
        Assert.assertFalse(tais.isEOFRecord(notZero));

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tais.setCurrentEntry(entry);
        Assert.assertEquals(entry, tais.getCurrentEntry());

        tais.setAtEOF(true);
        Assert.assertTrue(tais.isAtEOF());
        tais.close();
    }

    @Test
    public void testMatches() {
        byte[] invalid = new byte[100];
        Assert.assertFalse(TarArchiveInputStream.matches(invalid, 100));

        byte[] posix = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, posix, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, posix, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(posix, 512));

        byte[] gnuSpace = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuSpace, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, gnuSpace, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(gnuSpace, 512));

        byte[] gnuZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, gnuZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(gnuZero, 512));

        byte[] ant = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, ant, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, ant, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(ant, 512));

        byte[] wrong = new byte[512];
        System.arraycopy("WRONG!".getBytes(), 0, wrong, TarConstants.MAGIC_OFFSET, 6);
        Assert.assertFalse(TarArchiveInputStream.matches(wrong, 512));
    }

    @Test
    public void testReadRecordEofHandling() throws IOException {
        byte[] shortData = new byte[256];
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(shortData));
        Assert.assertNull(tais.readRecord());
        tais.close();
    }

    @Test
    public void testConsumeRemainderOfLastBlock() throws IOException {
        byte[] hdr = createHeader("test.txt", 10, TarConstants.LF_NORMAL);
        byte[] content = "0123456789".getBytes("UTF-8");
        byte[] tarData = createTarArchive(new byte[][]{hdr}, new byte[][]{content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData), 1024, 512);
        Assert.assertNotNull(tais.getNextTarEntry());
        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertTrue(tais.isAtEOF());
        tais.close();
    }
}
