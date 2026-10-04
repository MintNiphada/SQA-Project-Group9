package org.apache.commons.compress.archivers.zip;

import org.junit.Assert;
import org.junit.Test;

public class UnixStatTest {

    @Test
    public void testPermMask() {
        Assert.assertEquals(07777, UnixStat.PERM_MASK);
        Assert.assertEquals(4095, UnixStat.PERM_MASK);
    }

    @Test
    public void testLinkFlag() {
        Assert.assertEquals(0120000, UnixStat.LINK_FLAG);
        Assert.assertEquals(40960, UnixStat.LINK_FLAG);
    }

    @Test
    public void testFileFlag() {
        Assert.assertEquals(0100000, UnixStat.FILE_FLAG);
        Assert.assertEquals(32768, UnixStat.FILE_FLAG);
    }

    @Test
    public void testDirFlag() {
        Assert.assertEquals(040000, UnixStat.DIR_FLAG);
        Assert.assertEquals(16384, UnixStat.DIR_FLAG);
    }

    @Test
    public void testDefaultLinkPerm() {
        Assert.assertEquals(0777, UnixStat.DEFAULT_LINK_PERM);
        Assert.assertEquals(511, UnixStat.DEFAULT_LINK_PERM);
    }

    @Test
    public void testDefaultDirPerm() {
        Assert.assertEquals(0755, UnixStat.DEFAULT_DIR_PERM);
        Assert.assertEquals(493, UnixStat.DEFAULT_DIR_PERM);
    }

    @Test
    public void testDefaultFilePerm() {
        Assert.assertEquals(0644, UnixStat.DEFAULT_FILE_PERM);
        Assert.assertEquals(420, UnixStat.DEFAULT_FILE_PERM);
    }

    @Test
    public void testBitwiseCombinations() {
        int dirMode = UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
        Assert.assertEquals(040755, dirMode);
        Assert.assertEquals(UnixStat.DIR_FLAG, dirMode & ~UnixStat.PERM_MASK);
        Assert.assertEquals(UnixStat.DEFAULT_DIR_PERM, dirMode & UnixStat.PERM_MASK);

        int fileMode = UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
        Assert.assertEquals(0100644, fileMode);
        Assert.assertEquals(UnixStat.FILE_FLAG, fileMode & ~UnixStat.PERM_MASK);
        Assert.assertEquals(UnixStat.DEFAULT_FILE_PERM, fileMode & UnixStat.PERM_MASK);

        int linkMode = UnixStat.LINK_FLAG | UnixStat.DEFAULT_LINK_PERM;
        Assert.assertEquals(0120777, linkMode);
        Assert.assertEquals(UnixStat.LINK_FLAG, linkMode & ~UnixStat.PERM_MASK);
        Assert.assertEquals(UnixStat.DEFAULT_LINK_PERM, linkMode & UnixStat.PERM_MASK);
    }

    @Test
    public void testInterfaceImplementation() {
        UnixStat instance = new UnixStat() {};
        Assert.assertNotNull(instance);
    }
}
