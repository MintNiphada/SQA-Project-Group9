package org.apache.commons.codec.language;

import junit.framework.JUnit4TestAdapter;
import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DoubleMetaphoneTest {

    private DoubleMetaphone doubleMetaphone;

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DoubleMetaphoneTest.class);
    }

    @Before
    public void setUp() {
        this.doubleMetaphone = new DoubleMetaphone();
    }

    @Test
    public void testCleanInput() {
        Assert.assertNull(doubleMetaphone.doubleMetaphone(null));
        Assert.assertNull(doubleMetaphone.doubleMetaphone(""));
        Assert.assertNull(doubleMetaphone.doubleMetaphone("   "));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone(" a "));
    }

    @Test
    public void testGetSetMaxCodeLen() {
        Assert.assertEquals(4, doubleMetaphone.getMaxCodeLen());
        doubleMetaphone.setMaxCodeLen(8);
        Assert.assertEquals(8, doubleMetaphone.getMaxCodeLen());
        doubleMetaphone.setMaxCodeLen(2);
        Assert.assertEquals(2, doubleMetaphone.getMaxCodeLen());
    }

    @Test
    public void testEncodeObject() throws EncoderException {
        Assert.assertEquals("APL", doubleMetaphone.encode((Object) "Apple"));
        try {
            doubleMetaphone.encode(new Integer(123));
            Assert.fail("Expected EncoderException for non-string input");
        } catch (EncoderException e) {
            // expected
        }
    }

    @Test
    public void testEncodeString() {
        Assert.assertEquals("APL", doubleMetaphone.encode("Apple"));
        Assert.assertEquals(doubleMetaphone.doubleMetaphone("Apple"), doubleMetaphone.encode("Apple"));
    }

    @Test
    public void testIsDoubleMetaphoneEqual() {
        Assert.assertTrue(doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt"));
        Assert.assertTrue(doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt", false));
        Assert.assertFalse(doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Jones"));
        Assert.assertFalse(doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Jones", false));

        Assert.assertTrue(doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Schmidt", true));
        Assert.assertFalse(doubleMetaphone.isDoubleMetaphoneEqual("Smith", "Jones", true));
    }

    @Test
    public void testSilentStart() {
        // GN, KN, PN, WR, PS
        Assert.assertEquals("NAT", doubleMetaphone.doubleMetaphone("GNAT"));
        Assert.assertEquals("NAT", doubleMetaphone.doubleMetaphone("KNIGHT"));
        Assert.assertEquals("NMNE", doubleMetaphone.doubleMetaphone("PNEUMONIA"));
        Assert.assertEquals("RT", doubleMetaphone.doubleMetaphone("WRITE"));
        Assert.assertEquals("SM", doubleMetaphone.doubleMetaphone("PSALM"));
    }

    @Test
    public void testVowels() {
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("A"));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("E"));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("I"));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("O"));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("U"));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("Y"));
        Assert.assertEquals("APL", doubleMetaphone.doubleMetaphone("APPLE"));
        Assert.assertEquals("AKSN", doubleMetaphone.doubleMetaphone("ACTION"));
    }

    @Test
    public void testB() {
        Assert.assertEquals("P", doubleMetaphone.doubleMetaphone("B"));
        Assert.assertEquals("P", doubleMetaphone.doubleMetaphone("BB"));
        Assert.assertEquals("PR", doubleMetaphone.doubleMetaphone("BAR"));
        Assert.assertEquals("PPR", doubleMetaphone.doubleMetaphone("BABAR"));
    }

    @Test
    public void testCedilla() {
        Assert.assertEquals("SF", doubleMetaphone.doubleMetaphone("\u00C7AFE"));
    }

    @Test
    public void testC() {
        // Caesar
        Assert.assertEquals("SSR", doubleMetaphone.doubleMetaphone("CAESAR"));

        // Chia
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("CHIA"));

        // Condition C0 (Bacher / Macher)
        Assert.assertEquals("PKR", doubleMetaphone.doubleMetaphone("BACHER"));
        Assert.assertEquals("MKR", doubleMetaphone.doubleMetaphone("MACHER"));
        Assert.assertEquals("AK", doubleMetaphone.doubleMetaphone("ACH"));

        // Czerny
        Assert.assertEquals("SRN", doubleMetaphone.doubleMetaphone("CZERNY", false));
        Assert.assertEquals("XRN", doubleMetaphone.doubleMetaphone("CZERNY", true));

        // WICZ
        Assert.assertEquals("FTS", doubleMetaphone.doubleMetaphone("WICZ", false));
        Assert.assertEquals("FFX", doubleMetaphone.doubleMetaphone("WICZ", true));

        // CIA (focaccia)
        Assert.assertEquals("FKX", doubleMetaphone.doubleMetaphone("FOCACCIA"));

        // CC: accident / bacchus / bellocchio / McClelland
        Assert.assertEquals("AKST", doubleMetaphone.doubleMetaphone("ACCIDENT"));
        Assert.assertEquals("PKS", doubleMetaphone.doubleMetaphone("BACCHUS"));
        Assert.assertEquals("PLX", doubleMetaphone.doubleMetaphone("BELLOCCHIO"));
        Assert.assertEquals("MKLN", doubleMetaphone.doubleMetaphone("MCCLELLAND"));
        Assert.assertEquals("SKST", doubleMetaphone.doubleMetaphone("SUCCEED"));

        // CK, CG, CQ
        Assert.assertEquals("PK", doubleMetaphone.doubleMetaphone("PACK"));
        Assert.assertEquals("AK", doubleMetaphone.doubleMetaphone("ACQUIR"));
        Assert.assertEquals("AK", doubleMetaphone.doubleMetaphone("ACGR"));

        // CI, CE, CY - CIO, CIE, CIA
        Assert.assertEquals("SX", doubleMetaphone.doubleMetaphone("CIO"));
        Assert.assertEquals("SX", doubleMetaphone.doubleMetaphone("CIE"));
        Assert.assertEquals("SX", doubleMetaphone.doubleMetaphone("CIA"));
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("CI"));
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("CE"));
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("CY"));

        // C with spaces / other consonants
        Assert.assertEquals("MKKF", doubleMetaphone.doubleMetaphone("MAC CAFFREY"));
        Assert.assertEquals("MKKR", doubleMetaphone.doubleMetaphone("MAC GREGOR"));
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("C"));
        Assert.assertEquals("KK", doubleMetaphone.doubleMetaphone("CQ"));
        Assert.assertEquals("KK", doubleMetaphone.doubleMetaphone("CK"));
    }

    @Test
    public void testCH() {
        // Michael
        Assert.assertEquals("MKL", doubleMetaphone.doubleMetaphone("MICHAEL", false));
        Assert.assertEquals("MXL", doubleMetaphone.doubleMetaphone("MICHAEL", true));

        // Greek roots: chemistry, chorus, character, charity
        Assert.assertEquals("KMST", doubleMetaphone.doubleMetaphone("CHEMISTRY"));
        Assert.assertEquals("KRS", doubleMetaphone.doubleMetaphone("CHORUS"));
        Assert.assertEquals("KRKT", doubleMetaphone.doubleMetaphone("CHARACTER"));
        Assert.assertEquals("KRS", doubleMetaphone.doubleMetaphone("CHARIS"));
        Assert.assertEquals("XR", doubleMetaphone.doubleMetaphone("CHORE"));

        // conditionCH1
        Assert.assertEquals("FNSK", doubleMetaphone.doubleMetaphone("VON SCHMIDT"));
        Assert.assertEquals("ARKS", doubleMetaphone.doubleMetaphone("ARCHITECT"));
        Assert.assertEquals("ARKS", doubleMetaphone.doubleMetaphone("ORCHESTRA"));
        Assert.assertEquals("ARK", doubleMetaphone.doubleMetaphone("ORCHID"));
        Assert.assertEquals("KST", doubleMetaphone.doubleMetaphone("CHTH"));
        Assert.assertEquals("AKL", doubleMetaphone.doubleMetaphone("ACHL"));

        // MC vs Other
        Assert.assertEquals("MKLN", doubleMetaphone.doubleMetaphone("MCLEAN"));
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("CH"));
        Assert.assertEquals("X", doubleMetaphone.doubleMetaphone("CHIN"));
        Assert.assertEquals("XRF", doubleMetaphone.doubleMetaphone("CHAIR"));
        Assert.assertEquals("AX", doubleMetaphone.doubleMetaphone("ACHE", false));
        Assert.assertEquals("AK", doubleMetaphone.doubleMetaphone("ACHE", true));
    }

    @Test
    public void testD() {
        // DG -> EDGE, EDGAR
        Assert.assertEquals("AJ", doubleMetaphone.doubleMetaphone("EDGE"));
        Assert.assertEquals("ATK", doubleMetaphone.doubleMetaphone("EDGAR"));

        // DT, DD
        Assert.assertEquals("AT", doubleMetaphone.doubleMetaphone("ADTE"));
        Assert.assertEquals("AT", doubleMetaphone.doubleMetaphone("ADDE"));
        Assert.assertEquals("T", doubleMetaphone.doubleMetaphone("D"));
    }

    @Test
    public void testG() {
        // GH
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("GH"));
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("GHOST"));
        Assert.assertEquals("J", doubleMetaphone.doubleMetaphone("GHIS"));
        Assert.assertEquals("H", doubleMetaphone.doubleMetaphone("HUGH"));
        Assert.assertEquals("LF", doubleMetaphone.doubleMetaphone("LAUGH"));
        Assert.assertEquals("KF", doubleMetaphone.doubleMetaphone("COUGH"));
        Assert.assertEquals("RF", doubleMetaphone.doubleMetaphone("ROUGH"));
        Assert.assertEquals("TF", doubleMetaphone.doubleMetaphone("TOUGH"));
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("AUGHT"));

        // GN
        Assert.assertEquals("KN", doubleMetaphone.doubleMetaphone("AGNES", false));
        Assert.assertEquals("N", doubleMetaphone.doubleMetaphone("AGNES", true));
        Assert.assertEquals("N", doubleMetaphone.doubleMetaphone("GNEISS", false));
        Assert.assertEquals("KN", doubleMetaphone.doubleMetaphone("GNEISS", true));

        // GLI
        Assert.assertEquals("KL", doubleMetaphone.doubleMetaphone("TAGLIARO", false));
        Assert.assertEquals("L", doubleMetaphone.doubleMetaphone("TAGLIARO", true));

        // Initial G-
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("GES", false));
        Assert.assertEquals("J", doubleMetaphone.doubleMetaphone("GES", true));
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("GEL", false));
        Assert.assertEquals("J", doubleMetaphone.doubleMetaphone("GEL", true));

        // -GER-, -GY-
        Assert.assertEquals("TNKR", doubleMetaphone.doubleMetaphone("DANGER"));
        Assert.assertEquals("RNKR", doubleMetaphone.doubleMetaphone("RANGER"));
        Assert.assertEquals("MNKR", doubleMetaphone.doubleMetaphone("MANGER"));
        Assert.assertEquals("KR", doubleMetaphone.doubleMetaphone("GERMAN", false));
        Assert.assertEquals("JR", doubleMetaphone.doubleMetaphone("GERMAN", true));
        Assert.assertEquals("AJL", doubleMetaphone.doubleMetaphone("EGY"));

        // Italian AGGI, OGGI, BIAGGI, VAN/VON/SCH
        Assert.assertEquals("FNSK", doubleMetaphone.doubleMetaphone("VON GET"));
        Assert.assertEquals("AJ", doubleMetaphone.doubleMetaphone("SAGGIER"));
        Assert.assertEquals("PJ", doubleMetaphone.doubleMetaphone("BIAGGI", false));
        Assert.assertEquals("PK", doubleMetaphone.doubleMetaphone("BIAGGI", true));

        // GG
        Assert.assertEquals("AK", doubleMetaphone.doubleMetaphone("AGGA"));
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("G"));
    }

    @Test
    public void testH() {
        Assert.assertEquals("H", doubleMetaphone.doubleMetaphone("HA"));
        Assert.assertEquals("AH", doubleMetaphone.doubleMetaphone("AHA"));
        Assert.assertEquals("", doubleMetaphone.doubleMetaphone("H"));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("AH"));
    }

    @Test
    public void testJ() {
        // Spanish JOSE, SAN
        Assert.assertEquals("HS", doubleMetaphone.doubleMetaphone("JOSE"));
        Assert.assertEquals("SNHS", doubleMetaphone.doubleMetaphone("SAN JOSE"));
        Assert.assertEquals("J", doubleMetaphone.doubleMetaphone("JOSEPH", false));
        Assert.assertEquals("H", doubleMetaphone.doubleMetaphone("JOSEPH", true));

        // Initial J
        Assert.assertEquals("J", doubleMetaphone.doubleMetaphone("JOHN", false));
        Assert.assertEquals("A", doubleMetaphone.doubleMetaphone("JOHN", true));

        // Vowel + J + Vowel
        Assert.assertEquals("AJH", doubleMetaphone.doubleMetaphone("AJA", false));
        Assert.assertEquals("AH", doubleMetaphone.doubleMetaphone("AJA", true));

        // End of word J
        Assert.assertEquals("AJ", doubleMetaphone.doubleMetaphone("RAJ", false));
        Assert.assertEquals("A ", doubleMetaphone.doubleMetaphone("RAJ", true));

        // JJ
        Assert.assertEquals("AJ", doubleMetaphone.doubleMetaphone("AJJA"));

        // J followed by consonant
        Assert.assertEquals("JT", doubleMetaphone.doubleMetaphone("JTE"));
    }

    @Test
    public void testL() {
        // LL cases: conditionL0
        Assert.assertEquals("AL", doubleMetaphone.doubleMetaphone("ARMILLO"));
        Assert.assertEquals("AL", doubleMetaphone.doubleMetaphone("CABALLO"));
        Assert.assertEquals("VLL", doubleMetaphone.doubleMetaphone("VILLA"));
        Assert.assertEquals("AL", doubleMetaphone.doubleMetaphone("ALL"));
        Assert.assertEquals("L", doubleMetaphone.doubleMetaphone("L"));
    }

    @Test
    public void testM() {
        Assert.assertEquals("M", doubleMetaphone.doubleMetaphone("M"));
        Assert.assertEquals("M", doubleMetaphone.doubleMetaphone("MM"));
        Assert.assertEquals("TM", doubleMetaphone.doubleMetaphone("THUMB"));
        Assert.assertEquals("TMR", doubleMetaphone.doubleMetaphone("THUMBER"));
    }

    @Test
    public void testN() {
        Assert.assertEquals("N", doubleMetaphone.doubleMetaphone("N"));
        Assert.assertEquals("N", doubleMetaphone.doubleMetaphone("NN"));
        Assert.assertEquals("N", doubleMetaphone.doubleMetaphone("\u00D1"));
    }

    @Test
    public void testP() {
        Assert.assertEquals("P", doubleMetaphone.doubleMetaphone("P"));
        Assert.assertEquals("P", doubleMetaphone.doubleMetaphone("PP"));
        Assert.assertEquals("P", doubleMetaphone.doubleMetaphone("PB"));
        Assert.assertEquals("F", doubleMetaphone.doubleMetaphone("PH"));
    }

    @Test
    public void testQ() {
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("Q"));
        Assert.assertEquals("K", doubleMetaphone.doubleMetaphone("QQ"));
        Assert.assertEquals("KK", doubleMetaphone.doubleMetaphone("QUICK"));
    }

    @Test
    public void testR() {
        Assert.assertEquals("R", doubleMetaphone.doubleMetaphone("R"));
        Assert.assertEquals("R", doubleMetaphone.doubleMetaphone("RR"));
        // French -IER at end
        Assert.assertEquals("", doubleMetaphone.doubleMetaphone("ROGIER", false));
        Assert.assertEquals("R", doubleMetaphone.doubleMetaphone("ROGIER", true));
        Assert.assertEquals("MR", doubleMetaphone.doubleMetaphone("MIER"));
    }

    @Test
    public void testS() {
        // Island / Isle
        Assert.assertEquals("ALNT", doubleMetaphone.doubleMetaphone("ISLAND"));
        Assert.assertEquals("AL", doubleMetaphone.doubleMetaphone("ISLE"));
        Assert.assertEquals("KRL", doubleMetaphone.doubleMetaphone("CARLISLE"));
        Assert.assertEquals("KRL", doubleMetaphone.doubleMetaphone("CARLYSLE"));

        // Sugar
        Assert.assertEquals("XKR", doubleMetaphone.doubleMetaphone("SUGAR", false));
        Assert.assertEquals("SKR", doubleMetaphone.doubleMetaphone("SUGAR", true));

        // SH (Germanic vs Other)
        Assert.assertEquals("SM", doubleMetaphone.doubleMetaphone("SHEIM"));
        Assert.assertEquals("X", doubleMetaphone.doubleMetaphone("SHOOT"));

        // SIO, SIA, SIAN
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("ASIO", false));
        Assert.assertEquals("X", doubleMetaphone.doubleMetaphone("ASIO", true));
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("ASIAT", false));

        // Slavo-Germanic SIO
        Assert.assertEquals("SKW", doubleMetaphone.doubleMetaphone("SIOW"));

        // Initial S + M/N/L/W, SZ
        Assert.assertEquals("SMT", doubleMetaphone.doubleMetaphone("SMITH", false));
        Assert.assertEquals("XMT", doubleMetaphone.doubleMetaphone("SMITH", true));
        Assert.assertEquals("SNT", doubleMetaphone.doubleMetaphone("SNIDER", false));
        Assert.assertEquals("XNT", doubleMetaphone.doubleMetaphone("SNIDER", true));
        Assert.assertEquals("STR", doubleMetaphone.doubleMetaphone("SZTR", false));
        Assert.assertEquals("XTR", doubleMetaphone.doubleMetaphone("SZTR", true));

        // French AI/OI + S at end
        Assert.assertEquals("RSN", doubleMetaphone.doubleMetaphone("RESNAIS", false));
        Assert.assertEquals("RSNS", doubleMetaphone.doubleMetaphone("RESNAIS", true));

        // SC cases
        Assert.assertEquals("SKL", doubleMetaphone.doubleMetaphone("SCHOOL"));
        Assert.assertEquals("SKR", doubleMetaphone.doubleMetaphone("SCHERMERHORN", false));
        Assert.assertEquals("SKKR", doubleMetaphone.doubleMetaphone("SCHERMERHORN", true));
        Assert.assertEquals("XMR", doubleMetaphone.doubleMetaphone("SCHMIDT", false));
        Assert.assertEquals("SMR", doubleMetaphone.doubleMetaphone("SCHMIDT", true));
        Assert.assertEquals("SS", doubleMetaphone.doubleMetaphone("SCIENCE"));
        Assert.assertEquals("SK", doubleMetaphone.doubleMetaphone("SCALA"));
    }

    @Test
    public void testT() {
        // TION, TIA, TCH
        Assert.assertEquals("XN", doubleMetaphone.doubleMetaphone("TION"));
        Assert.assertEquals("X", doubleMetaphone.doubleMetaphone("TIA"));
        Assert.assertEquals("X", doubleMetaphone.doubleMetaphone("MATCH"));

        // TH / TTH: Thomas, Thames, etc.
        Assert.assertEquals("TMS", doubleMetaphone.doubleMetaphone("THOMAS"));
        Assert.assertEquals("TMS", doubleMetaphone.doubleMetaphone("THAMES"));
        Assert.assertEquals("0", doubleMetaphone.doubleMetaphone("TH", false));
        Assert.assertEquals("T", doubleMetaphone.doubleMetaphone("TH", true));

        // TT, TD
        Assert.assertEquals("T", doubleMetaphone.doubleMetaphone("TTE"));
        Assert.assertEquals("T", doubleMetaphone.doubleMetaphone("TD"));
    }

    @Test
    public void testV() {
        Assert.assertEquals("F", doubleMetaphone.doubleMetaphone("V"));
        Assert.assertEquals("F", doubleMetaphone.doubleMetaphone("VV"));
        Assert.assertEquals("FT", doubleMetaphone.doubleMetaphone("VOTE"));
    }

    @Test
    public void testW() {
        // WR
        Assert.assertEquals("RT", doubleMetaphone.doubleMetaphone("WRITE"));

        // Initial W + vowel
        Assert.assertEquals("ASRM", doubleMetaphone.doubleMetaphone("WASSERMAN", false));
        Assert.assertEquals("FSRM", doubleMetaphone.doubleMetaphone("WASSERMAN", true));
        Assert.assertEquals("AT", doubleMetaphone.doubleMetaphone("WHITE"));

        // End of word / Slavic suffixes
        Assert.assertEquals("ARN", doubleMetaphone.doubleMetaphone("ARNOW", false));
        Assert.assertEquals("ARNF", doubleMetaphone.doubleMetaphone("ARNOW", true));
        Assert.assertEquals("ASK", doubleMetaphone.doubleMetaphone("ARNEWSKI", false));
        Assert.assertEquals("ASFK", doubleMetaphone.doubleMetaphone("ARNEWSKI", true));

        // WICZ / WITZ
        Assert.assertEquals("TS", doubleMetaphone.doubleMetaphone("WICZ", false));
        Assert.assertEquals("FX", doubleMetaphone.doubleMetaphone("WICZ", true));
    }

    @Test
    public void testX() {
        // Initial X
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("X"));
        Assert.assertEquals("SBR", doubleMetaphone.doubleMetaphone("XAVIER"));

        // French endings: -iau, -eau, -au, -ou
        Assert.assertEquals("PR", doubleMetaphone.doubleMetaphone("BREAUX"));
        Assert.assertEquals("BKS", doubleMetaphone.doubleMetaphone("BOX"));
        Assert.assertEquals("BKS", doubleMetaphone.doubleMetaphone("BOXX"));
        Assert.assertEquals("BKS", doubleMetaphone.doubleMetaphone("BOXC"));
    }

    @Test
    public void testZ() {
        // ZH
        Assert.assertEquals("J", doubleMetaphone.doubleMetaphone("ZHANG"));

        // ZO, ZI, ZA or slavoGermanic
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("ZOOM", false));
        Assert.assertEquals("TS", doubleMetaphone.doubleMetaphone("ZOOM", true));
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("Z"));
        Assert.assertEquals("S", doubleMetaphone.doubleMetaphone("ZZ"));
        Assert.assertEquals("KS", doubleMetaphone.doubleMetaphone("KAZ", false));
        Assert.assertEquals("KTS", doubleMetaphone.doubleMetaphone("KAZ", true));
    }

    @Test
    public void testDoubleMetaphoneResult() {
        DoubleMetaphone.DoubleMetaphoneResult result = doubleMetaphone.new DoubleMetaphoneResult(4);
        Assert.assertEquals("", result.getPrimary());
        Assert.assertEquals("", result.getAlternate());
        Assert.assertFalse(result.isComplete());

        result.append('A');
        Assert.assertEquals("A", result.getPrimary());
        Assert.assertEquals("A", result.getAlternate());

        result.append('B', 'C');
        Assert.assertEquals("AB", result.getPrimary());
        Assert.assertEquals("AC", result.getAlternate());

        result.append("DE");
        Assert.assertEquals("ABDE", result.getPrimary());
        Assert.assertEquals("ACDE", result.getAlternate());
        Assert.assertTrue(result.isComplete());

        // Appending more should not exceed maxLength
        result.append('F');
        result.append("GH");
        result.appendPrimary('X');
        result.appendAlternate('Y');
        result.appendPrimary("ZZ");
        result.appendAlternate("WW");
        result.append("1", "2");
        Assert.assertEquals("ABDE", result.getPrimary());
        Assert.assertEquals("ACDE", result.getAlternate());

        DoubleMetaphone.DoubleMetaphoneResult smallResult = doubleMetaphone.new DoubleMetaphoneResult(2);
        smallResult.append("TOOLONG", "EXTRALONG");
        Assert.assertEquals("TO", smallResult.getPrimary());
        Assert.assertEquals("EX", smallResult.getAlternate());
        Assert.assertTrue(smallResult.isComplete());
    }

    @Test
    public void testProtectedHelpers() {
        Assert.assertEquals('A', doubleMetaphone.charAt("ABC", 0));
        Assert.assertEquals(Character.MIN_VALUE, doubleMetaphone.charAt("ABC", -1));
        Assert.assertEquals(Character.MIN_VALUE, doubleMetaphone.charAt("ABC", 3));

        Assert.assertTrue(DoubleMetaphone.contains("TESTING", 0, 4, new String[]{"TEST", "OTHER"}));
        Assert.assertFalse(DoubleMetaphone.contains("TESTING", 0, 4, new String[]{"FAIL"}));
        Assert.assertFalse(DoubleMetaphone.contains("TESTING", -1, 4, new String[]{"TEST"}));
        Assert.assertFalse(DoubleMetaphone.contains("TESTING", 5, 4, new String[]{"TEST"}));
    }
}
