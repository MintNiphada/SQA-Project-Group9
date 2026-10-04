package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class TarArchiveInputStreamTest {

    private byte[] createTarArchive(TarArchiveEntry... entries) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        for (TarArchiveEntry entry : entries) {
            tos.putArchiveEntry(entry);
            tos.closeArchiveEntry();
        }
        tos.close();
        return bos.toByteArray();
    }

    private byte[] createTarArchiveWithData(TarArchiveEntry entry, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.putArchiveEntry(entry);
        if (data != null && data.length > 0) {
            tos.write(data);
        }
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    @Test
    public void testConstructorsAndGetters() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(bais);
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais1.getRecordSize());
        tais1.close();

        TarArchiveInputStream tais2 = new TarArchiveInputStream(bais, "UTF-8");
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais2.getRecordSize());

        TarArchiveInputStream tais3 = new TarArchiveInputStream(bais, 1024);
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais3.getRecordSize());

        TarArchiveInputStream tais4 = new TarArchiveInputStream(bais, 1024, "UTF-8");
        Assert.assertEquals(TarConstants.DEFAULT_RCDSIZE, tais4.getRecordSize());

        TarArchiveInputStream tais5 = new TarArchiveInputStream(bais, 1024, 512);
        Assert.assertEquals(512, tais5.getRecordSize());

        TarArchiveInputStream tais6 = new TarArchiveInputStream(bais, 1024, 512, "UTF-8");
        Assert.assertEquals(512, tais6.getRecordSize());
        Assert.assertFalse(tais6.markSupported());
        tais6.mark(10);
        tais6.reset();
    }

    @Test
    public void testReadEmptyStream() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertNull(tais.getNextEntry());
        Assert.assertEquals(0, tais.available());
        tais.close();
    }

    @Test
    public void testSingleEntryReadAndSkip() throws IOException {
        byte[] content = "Hello World Tar Content Test".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(content.length);
        byte[] tarData = createTarArchiveWithData(entry, content);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("test.txt", readEntry.getName());
        Assert.assertSame(readEntry, tais.getCurrentEntry());

        Assert.assertEquals(content.length, tais.available());
        Assert.assertEquals(0, tais.skip(-5));
        Assert.assertEquals(0, tais.skip(0));

        long skipped = tais.skip(5);
        Assert.assertEquals(5, skipped);
        Assert.assertEquals(content.length - 5, tais.available());

        byte[] buf = new byte[content.length];
        int read = tais.read(buf, 0, buf.length);
        Assert.assertEquals(content.length - 5, read);
        Assert.assertEquals(" World Tar Content Test", new String(buf, 0, read, "UTF-8"));
        Assert.assertEquals(-1, tais.read(buf, 0, 1));

        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertTrue(tais.isAtEOF());
        tais.close();
    }

    @Test
    public void testSkipBeyondAvailable() throws IOException {
        byte[] content = "0123456789".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("skip.txt");
        entry.setSize(content.length);
        byte[] tarData = createTarArchiveWithData(entry, content);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        Assert.assertNotNull(tais.getNextTarEntry());
        long skipped = tais.skip(100);
        Assert.assertEquals(10, skipped);
        Assert.assertEquals(0, tais.available());
        Assert.assertEquals(-1, tais.read(new byte[10], 0, 10));
        tais.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testReadWithoutCurrentEntry() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]));
        tais.read(new byte[10], 0, 10);
    }

    @Test(expected = IOException.class)
    public void testReadTruncatedEntryData() throws IOException {
        byte[] content = "Partial Data".getBytes("UTF-8");
        TarArchiveEntry entry = new TarArchiveEntry("truncated.txt");
        entry.setSize(100);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.putArchiveEntry(entry);
        tos.write(content);
        byte[] tarData = bos.toByteArray();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        Assert.assertNotNull(tais.getNextTarEntry());
        byte[] buf = new byte[100];
        tais.read(buf, 0, 12);
        tais.read(buf, 12, 50);
    }

    @Test
    public void testConsecutiveEntriesAndPadding() throws IOException {
        TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        byte[] data1 = "12345".getBytes("UTF-8");
        entry1.setSize(data1.length);

        TarArchiveEntry entry2 = new TarArchiveEntry("file2.txt");
        byte[] data2 = "ABCDE67890".getBytes("UTF-8");
        entry2.setSize(data2.length);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.putArchiveEntry(entry1);
        tos.write(data1);
        tos.closeArchiveEntry();
        tos.putArchiveEntry(entry2);
        tos.write(data2);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry e1 = tais.getNextTarEntry();
        Assert.assertNotNull(e1);
        Assert.assertEquals("file1.txt", e1.getName());

        TarArchiveEntry e2 = tais.getNextTarEntry();
        Assert.assertNotNull(e2);
        Assert.assertEquals("file2.txt", e2.getName());

        byte[] buf2 = new byte[data2.length];
        int r2 = tais.read(buf2, 0, buf2.length);
        Assert.assertEquals(data2.length, r2);
        Assert.assertArrayEquals(data2, buf2);

        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testGNULongNameAndLongLink() throws IOException {
        String longName = "very/long/path/name/that/exceeds/the/normal/tar/limit/of/one/hundred/characters/which/requires/gnu/extension/longfilename.txt";
        String longLink = "very/long/link/target/path/name/that/exceeds/the/normal/tar/limit/of/one/hundred/characters/which/requires/gnu/extension/target.txt";

        TarArchiveEntry entry = new TarArchiveEntry(longName, TarConstants.LF_SYMLINK);
        entry.setLinkName(longLink);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry result = tais.getNextTarEntry();
        Assert.assertNotNull(result);
        Assert.assertEquals(longName, result.getName());
        Assert.assertEquals(longLink, result.getLinkName());
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testGNULongNameWithoutFollowingEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry longEntry = new TarArchiveEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGNAME);
        byte[] nameBytes = "truncated_name".getBytes("UTF-8");
        longEntry.setSize(nameBytes.length);
        tos.putArchiveEntry(longEntry);
        tos.write(nameBytes);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testGNULongLinkWithoutFollowingEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry longLink = new TarArchiveEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGLINK);
        byte[] linkBytes = "truncated_link".getBytes("UTF-8");
        longLink.setSize(linkBytes.length);
        tos.putArchiveEntry(longLink);
        tos.write(linkBytes);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testPaxHeaders() throws IOException {
        String paxData = "25 path=pax/custom/path\n"
                + "29 linkpath=pax/custom/link\n"
                + "11 gid=1001\n"
                + "14 gname=mygroup\n"
                + "11 uid=2002\n"
                + "13 uname=myuser\n"
                + "12 size=12345\n"
                + "18 mtime=1234567.89\n"
                + "23 SCHILY.devminor=10\n"
                + "23 SCHILY.devmajor=20\n";
        byte[] paxBytes = paxData.getBytes("UTF-8");

        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader/entry", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        paxEntry.setSize(paxBytes.length);

        TarArchiveEntry mainEntry = new TarArchiveEntry("orig.txt");
        mainEntry.setSize(0);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.putArchiveEntry(paxEntry);
        tos.write(paxBytes);
        tos.closeArchiveEntry();
        tos.putArchiveEntry(mainEntry);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry res = tais.getNextTarEntry();
        Assert.assertNotNull(res);
        Assert.assertEquals("pax/custom/path", res.getName());
        Assert.assertEquals("pax/custom/link", res.getLinkName());
        Assert.assertEquals(1001, res.getGroupId());
        Assert.assertEquals("mygroup", res.getGroupName());
        Assert.assertEquals(2002, res.getUserId());
        Assert.assertEquals("myuser", res.getUserName());
        Assert.assertEquals(12345L, res.getSize());
        Assert.assertEquals(1234567890L, res.getModTime().getTime());
        Assert.assertEquals(10, res.getDevMinor());
        Assert.assertEquals(20, res.getDevMajor());
        tais.close();
    }

    @Test(expected = IOException.class)
    public void testPaxHeaderPrematureEOF() throws IOException {
        String paxData = "50 path=incomplete";
        ByteArrayInputStream bais = new ByteArrayInputStream(paxData.getBytes("UTF-8"));
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.parsePaxHeaders(bais);
    }

    @Test
    public void testParsePaxHeadersDirectly() throws IOException {
        String paxData = "13 key1=val1\n14 key2=value\n";
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Map<String, String> map = tais.parsePaxHeaders(new ByteArrayInputStream(paxData.getBytes("UTF-8")));
        Assert.assertEquals("val1", map.get("key1"));
        Assert.assertEquals("value", map.get("key2"));
    }

    @Test
    public void testGNUSparseArchiveEntry() throws IOException {
        byte[] header = new byte[512];
        System.arraycopy("sparse.bin".getBytes("UTF-8"), 0, header, 0, 10);
        header[156] = TarConstants.LF_GNUTYPE_SPARSE;
        header[TarConstants.MAGIC_OFFSET] = 'u';
        header[TarConstants.MAGIC_OFFSET + 1] = 's';
        header[TarConstants.MAGIC_OFFSET + 2] = 't';
        header[TarConstants.MAGIC_OFFSET + 3] = 'a';
        header[TarConstants.MAGIC_OFFSET + 4] = 'r';
        header[TarConstants.MAGIC_OFFSET + 5] = ' ';
        header[TarConstants.VERSION_OFFSET] = ' ';
        header[TarConstants.VERSION_OFFSET + 1] = 0;
        header[482] = 1;

        long chk = 0;
        Arrays.fill(header, 148, 156, (byte) ' ');
        for (byte b : header) {
            chk += (b & 0xFF);
        }
        String chkStr = Long.toOctalString(chk);
        while (chkStr.length() < 6) chkStr = "0" + chkStr;
        System.arraycopy((chkStr + "\0 ").getBytes("UTF-8"), 0, header, 148, 8);

        byte[] sparseExtHeader = new byte[512];
        sparseExtHeader[504] = 0;

        byte[] emptyRecords = new byte[1024];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(sparseExtHeader);
        bos.write(emptyRecords);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertTrue(entry.isGNUSparse());
        Assert.assertFalse(tais.canReadEntryData(entry));
        tais.close();
    }

    @Test
    public void testGNUSparseArchiveEntryTruncated() throws IOException {
        byte[] header = new byte[512];
        System.arraycopy("sparse_trunc.bin".getBytes("UTF-8"), 0, header, 0, 16);
        header[156] = TarConstants.LF_GNUTYPE_SPARSE;
        header[482] = 1;
        Arrays.fill(header, 148, 156, (byte) ' ');
        long chk = 0;
        for (byte b : header) chk += (b & 0xFF);
        String chkStr = Long.toOctalString(chk);
        while (chkStr.length() < 6) chkStr = "0" + chkStr;
        System.arraycopy((chkStr + "\0 ").getBytes("UTF-8"), 0, header, 148, 8);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(header));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNull(entry);
        Assert.assertNull(tais.getCurrentEntry());
        tais.close();
    }

    @Test(expected = IOException.class)
    public void testCorruptedHeaderThrowsIOException() throws IOException {
        byte[] corrupted = new byte[512];
        Arrays.fill(corrupted, (byte) 'A');
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(corrupted));
        tais.getNextTarEntry();
    }

    @Test
    public void testCanReadEntryData() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry regular = new TarArchiveEntry("test.txt");
        Assert.assertTrue(tais.canReadEntryData(regular));
        Assert.assertFalse(tais.canReadEntryData(new ArchiveEntry() {
            public String getName() { return "other"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return null; }
        }));
        Assert.assertFalse(tais.canReadEntryData(null));
    }

    @Test
    public void testSettersAndGetters() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry entry = new TarArchiveEntry("dummy");
        tais.setCurrentEntry(entry);
        Assert.assertSame(entry, tais.getCurrentEntry());
        Assert.assertFalse(tais.isAtEOF());
        tais.setAtEOF(true);
        Assert.assertTrue(tais.isAtEOF());
    }

    @Test
    public void testTryToConsumeSecondEOFRecordWithMarkSupportedStream() throws IOException {
        byte[] twoEOFBlocks = new byte[1024];
        ByteArrayInputStream bais = new ByteArrayInputStream(twoEOFBlocks);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais);
        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertTrue(tais.isAtEOF());
        tais.close();
    }

    @Test
    public void testTryToConsumeSecondEOFRecordWithNonMarkSupportedStream() throws IOException {
        byte[] twoEOFBlocks = new byte[1024];
        InputStream nonMarkStream = new FilterInputStream(new ByteArrayInputStream(twoEOFBlocks)) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };
        TarArchiveInputStream tais = new TarArchiveInputStream(nonMarkStream);
        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertTrue(tais.isAtEOF());
        tais.close();
    }

    @Test
    public void testMatches() {
        Assert.assertFalse(TarArchiveInputStream.matches(new byte[100], 100));

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

        byte[] unknownSig = new byte[512];
        System.arraycopy("unknown".getBytes(), 0, unknownSig, TarConstants.MAGIC_OFFSET, 7);
        Assert.assertFalse(TarArchiveInputStream.matches(unknownSig, 512));
    }

    @Test
    public void testIsEOFRecord() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Assert.assertTrue(tais.isEOFRecord(null));
        Assert.assertTrue(tais.isEOFRecord(new byte[512]));
        byte[] notEof = new byte[512];
        notEof[0] = 1;
        Assert.assertFalse(tais.isEOFRecord(notEof));
    }
}
