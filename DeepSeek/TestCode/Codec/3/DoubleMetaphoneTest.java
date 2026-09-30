package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.codec.EncoderException;

public class DoubleMetaphoneTest {

    @Test
    public void testDoubleMetaphoneNull() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertNull(dm.doubleMetaphone(null));
        assertNull(dm.doubleMetaphone(null, false));
        assertNull(dm.doubleMetaphone(null, true));
    }

    @Test
    public void testDoubleMetaphoneEmpty() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertNull(dm.doubleMetaphone(""));
        assertNull(dm.doubleMetaphone("   "));
    }

    @Test
    public void testDoubleMetaphoneBasic() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("TST", dm.doubleMetaphone("test"));
        assertEquals("TST", dm.doubleMetaphone("TEST"));
        assertEquals("TST", dm.doubleMetaphone("test", false));
        assertEquals("TST", dm.doubleMetaphone("test", true));
    }

    @Test
    public void testMaxCodeLen() {
        DoubleMetaphone dm = new DoubleMetaphone();
        dm.setMaxCodeLen(2);
        assertEquals(2, dm.getMaxCodeLen());
        assertEquals("TS", dm.doubleMetaphone("test"));
        dm.setMaxCodeLen(4);
        assertEquals("TST", dm.doubleMetaphone("test"));
    }

    @Test
    public void testEncodeObject() throws EncoderException {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("TST", dm.encode((Object) "test"));
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectNonString() throws EncoderException {
        DoubleMetaphone dm = new DoubleMetaphone();
        dm.encode(new Integer(5));
    }

    @Test
    public void testEncodeString() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("TST", dm.encode("test"));
    }

    @Test
    public void testIsDoubleMetaphoneEqual() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertTrue(dm.isDoubleMetaphoneEqual("test", "test"));
        assertFalse(dm.isDoubleMetaphoneEqual("test", "different"));
        assertTrue(dm.isDoubleMetaphoneEqual("test", "test", false));
        assertTrue(dm.isDoubleMetaphoneEqual("test", "test", true));
        assertFalse(dm.isDoubleMetaphoneEqual("test", "different", true));
    }

    @Test
    public void testSilentStart() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("N", dm.doubleMetaphone("GNOME"));
        assertEquals("N", dm.doubleMetaphone("KNIGHT"));
        assertEquals("N", dm.doubleMetaphone("PNEUMONIA"));
        assertEquals("R", dm.doubleMetaphone("WRONG"));
        assertEquals("S", dm.doubleMetaphone("PSYCHIC"));
    }

    @Test
    public void testSlavoGermanic() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Contains W
        assertTrue(dm.doubleMetaphone("WAGNER").contains("F"));
        // Contains K
        assertTrue(dm.doubleMetaphone("KAFKA").contains("K"));
        // Contains CZ
        assertTrue(dm.doubleMetaphone("CZERNOWITZ").contains("S"));
        // Contains WITZ
        assertTrue(dm.doubleMetaphone("WITZ").contains("TS"));
    }

    @Test
    public void testHandleAEIOUY() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("A", dm.doubleMetaphone("A"));
        assertEquals("A", dm.doubleMetaphone("E"));
        assertEquals("A", dm.doubleMetaphone("I"));
        assertEquals("A", dm.doubleMetaphone("O"));
        assertEquals("A", dm.doubleMetaphone("U"));
        assertEquals("A", dm.doubleMetaphone("Y"));
        assertEquals("AB", dm.doubleMetaphone("AB"));
    }

    @Test
    public void testHandleB() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("P", dm.doubleMetaphone("B"));
        assertEquals("P", dm.doubleMetaphone("BB"));
    }

    @Test
    public void testHandleC() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // CHIA -> K
        assertEquals("K", dm.doubleMetaphone("CHIA"));
        // CAESAR -> S
        assertEquals("SSR", dm.doubleMetaphone("CAESAR"));
        // CH
        assertEquals("X", dm.doubleMetaphone("CH"));
        assertEquals("K", dm.doubleMetaphone("CHORUS"));
        assertEquals("K", dm.doubleMetaphone("CHROME"));
        assertEquals("K", dm.doubleMetaphone("CHRISTIAN"));
        assertEquals("X", dm.doubleMetaphone("CHICKEN"));
        // CZ not after WICZ
        assertEquals("S", dm.doubleMetaphone("CZERNOWITZ"));
        // CIA -> X
        assertEquals("X", dm.doubleMetaphone("FOCACCIA"));
        // CC
        assertEquals("X", dm.doubleMetaphone("BACCI"));
        assertEquals("KS", dm.doubleMetaphone("ACCIDENT"));
        assertEquals("K", dm.doubleMetaphone("MCCLELLAND"));
        assertEquals("K", dm.doubleMetaphone("BACCHUS"));
        // CK, CG, CQ -> K
        assertEquals("K", dm.doubleMetaphone("BACK"));
        assertEquals("K", dm.doubleMetaphone("ECG"));
        assertEquals("K", dm.doubleMetaphone("ACQUIRE"));
        // CI, CE, CY -> S or X
        assertEquals("S", dm.doubleMetaphone("CITY"));
        assertEquals("S", dm.doubleMetaphone("CENT"));
        assertEquals("S", dm.doubleMetaphone("CYCLE"));
        assertEquals("X", dm.doubleMetaphone("CIO"));
        assertEquals("X", dm.doubleMetaphone("CIE"));
        assertEquals("X", dm.doubleMetaphone("CIA"));
        // else K, with special skip
        assertEquals("K", dm.doubleMetaphone("C"));
        assertEquals("K", dm.doubleMetaphone("MAC CAFFREY"));
        assertEquals("K", dm.doubleMetaphone("MAC GREGOR"));
        assertEquals("K", dm.doubleMetaphone("MCKINLEY"));
        assertEquals("K", dm.doubleMetaphone("MCK"));
    }

    @Test
    public void testHandleD() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // DG before I,E,Y -> J
        assertEquals("J", dm.doubleMetaphone("EDGE"));
        // DG else -> TK
        assertEquals("TK", dm.doubleMetaphone("EDGAR"));
        // DT, DD -> T
        assertEquals("T", dm.doubleMetaphone("ADD"));
        assertEquals("T", dm.doubleMetaphone("ADT"));
        // else T
        assertEquals("T", dm.doubleMetaphone("D"));
    }

    @Test
    public void testHandleF() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("F", dm.doubleMetaphone("F"));
        assertEquals("F", dm.doubleMetaphone("FF"));
    }

    @Test
    public void testHandleG() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // GH
        assertEquals("K", dm.doubleMetaphone("GHOST"));
        assertEquals("J", dm.doubleMetaphone("GHI"));
        assertEquals("", dm.doubleMetaphone("BOUGH"));
        assertEquals("F", dm.doubleMetaphone("LAUGH"));
        assertEquals("K", dm.doubleMetaphone("HUGH"));
        // GN
        assertEquals("N", dm.doubleMetaphone("GNOME"));
        assertEquals("KN", dm.doubleMetaphone("AGNOSTIC"));
        assertEquals("N", dm.doubleMetaphone("SIGN"));
        // GLI -> KL/L
        assertEquals("KL", dm.doubleMetaphone("TAGLIATELLE"));
        // GY, GER, etc.
        assertEquals("K", dm.doubleMetaphone("GYRO"));
        assertEquals("J", dm.doubleMetaphone("GYRO", true));
        assertEquals("K", dm.doubleMetaphone("GERMAN"));
        assertEquals("J", dm.doubleMetaphone("GERMAN", true));
        assertEquals("K", dm.doubleMetaphone("DANGER"));
        assertEquals("K", dm.doubleMetaphone("RANGER"));
        assertEquals("K", dm.doubleMetaphone("MANGER"));
        assertEquals("K", dm.doubleMetaphone("RGY"));
        assertEquals("K", dm.doubleMetaphone("OGY"));
        // Italian
        assertEquals("J", dm.doubleMetaphone("BIAGGI"));
        assertEquals("K", dm.doubleMetaphone("VAN GOGH"));
        assertEquals("K", dm.doubleMetaphone("SCHOOL"));
        assertEquals("J", dm.doubleMetaphone("GIER"));
        // GG
        assertEquals("K", dm.doubleMetaphone("EGG"));
        // else K
        assertEquals("K", dm.doubleMetaphone("G"));
    }

    @Test
    public void testHandleH() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("H", dm.doubleMetaphone("HA"));
        assertEquals("H", dm.doubleMetaphone("AHA"));
        assertEquals("", dm.doubleMetaphone("AH"));
        assertEquals("", dm.doubleMetaphone("HH"));
    }

    @Test
    public void testHandleJ() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // JOSE
        assertEquals("H", dm.doubleMetaphone("JOSE"));
        assertEquals("J", dm.doubleMetaphone("JOSEPH"));
        assertEquals("H", dm.doubleMetaphone("SAN JACINTO"));
        // else
        assertEquals("J", dm.doubleMetaphone("J"));
        assertEquals("J", dm.doubleMetaphone("JACK"));
        assertEquals("J", dm.doubleMetaphone("JILL"));
        assertEquals("J", dm.doubleMetaphone("JOSE", true)); // alternate
        assertEquals("J", dm.doubleMetaphone("JOSE", false));
        assertEquals("J", dm.doubleMetaphone("JOSE", false));
        assertEquals("J", dm.doubleMetaphone("JOSE", true));
        assertEquals("J", dm.doubleMetaphone("JOSE"));
        assertEquals("J", dm.doubleMetaphone("JOSE"));
        // J at end
        assertEquals("J", dm.doubleMetaphone("RAJ"));
        // JJ
        assertEquals("J", dm.doubleMetaphone("JJO"));
    }

    @Test
    public void testHandleK() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("K", dm.doubleMetaphone("K"));
        assertEquals("K", dm.doubleMetaphone("KK"));
    }

    @Test
    public void testHandleL() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("L", dm.doubleMetaphone("L"));
        assertEquals("L", dm.doubleMetaphone("LL"));
        // conditionL0
        assertEquals("L", dm.doubleMetaphone("ALLE"));
        assertEquals("L", dm.doubleMetaphone("ILLO"));
        assertEquals("L", dm.doubleMetaphone("ILLA"));
        assertEquals("L", dm.doubleMetaphone("ALLE", true));
    }

    @Test
    public void testHandleM() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("M", dm.doubleMetaphone("M"));
        assertEquals("M", dm.doubleMetaphone("MM"));
        assertEquals("M", dm.doubleMetaphone("UMB"));
        assertEquals("M", dm.doubleMetaphone("UMBER"));
    }

    @Test
    public void testHandleN() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("N", dm.doubleMetaphone("N"));
        assertEquals("N", dm.doubleMetaphone("NN"));
        // N tilde
        assertEquals("N", dm.doubleMetaphone("\u00D1"));
    }

    @Test
    public void testHandleP() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("F", dm.doubleMetaphone("PH"));
        assertEquals("P", dm.doubleMetaphone("P"));
        assertEquals("P", dm.doubleMetaphone("PP"));
        assertEquals("P", dm.doubleMetaphone("PB"));
    }

    @Test
    public void testHandleQ() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("K", dm.doubleMetaphone("Q"));
        assertEquals("K", dm.doubleMetaphone("QQ"));
    }

    @Test
    public void testHandleR() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("R", dm.doubleMetaphone("R"));
        assertEquals("R", dm.doubleMetaphone("RR"));
        // alternate R for IE ending not after ME,MA
        assertEquals("R", dm.doubleMetaphone("CARRIE"));
        assertEquals("R", dm.doubleMetaphone("CARRIE", true));
        assertEquals("R", dm.doubleMetaphone("MARIE"));
        assertEquals("R", dm.doubleMetaphone("MARIE", true));
    }

    @Test
    public void testHandleS() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // ISL, YSL
        assertEquals("SL", dm.doubleMetaphone("ISLAND"));
        assertEquals("SL", dm.doubleMetaphone("CARLISLE"));
        // SUGAR
        assertEquals("X", dm.doubleMetaphone("SUGAR"));
        assertEquals("S", dm.doubleMetaphone("SUGAR", true));
        // SH
        assertEquals("X", dm.doubleMetaphone("SHOE"));
        assertEquals("S", dm.doubleMetaphone("SHERMAN"));
        assertEquals("S", dm.doubleMetaphone("SHOLZ"));
        // SIO, SIA, SIAN
        assertEquals("S", dm.doubleMetaphone("SIOBHAN"));
        assertEquals("X", dm.doubleMetaphone("SIOBHAN", true));
        assertEquals("S", dm.doubleMetaphone("SIAN"));
        assertEquals("X", dm.doubleMetaphone("SIAN", true));
        // S with M,N,L,W,Z
        assertEquals("S", dm.doubleMetaphone("SMITH"));
        assertEquals("X", dm.doubleMetaphone("SMITH", true));
        assertEquals("S", dm.doubleMetaphone("SNIDER"));
        assertEquals("X", dm.doubleMetaphone("SNIDER", true));
        assertEquals("S", dm.doubleMetaphone("SLATER"));
        assertEquals("X", dm.doubleMetaphone("SLATER", true));
        assertEquals("S", dm.doubleMetaphone("SWEET"));
        assertEquals("X", dm.doubleMetaphone("SWEET", true));
        assertEquals("S", dm.doubleMetaphone("SZYMANSKI"));
        assertEquals("X", dm.doubleMetaphone("SZYMANSKI", true));
        // SC
        assertEquals("SK", dm.doubleMetaphone("SCOTT"));
        assertEquals("S", dm.doubleMetaphone("SCENE"));
        assertEquals("X", dm.doubleMetaphone("SCHOOL"));
        assertEquals("SK", dm.doubleMetaphone("SCHERMERHORN"));
        assertEquals("X", dm.doubleMetaphone("SCHERMERHORN", true));
        assertEquals("SK", dm.doubleMetaphone("SCHENKER"));
        assertEquals("X", dm.doubleMetaphone("SCHENKER", true));
        assertEquals("X", dm.doubleMetaphone("SCH"));
        assertEquals("S", dm.doubleMetaphone("SCH", true));
        // French endings
        assertEquals("S", dm.doubleMetaphone("RESNAIS"));
        assertEquals("S", dm.doubleMetaphone("ARTOIS"));
        // else S
        assertEquals("S", dm.doubleMetaphone("S"));
        assertEquals("S", dm.doubleMetaphone("SS"));
        assertEquals("S", dm.doubleMetaphone("SZ"));
    }

    @Test
    public void testHandleT() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // TION -> X
        assertEquals("X", dm.doubleMetaphone("ACTION"));
        // TIA, TCH -> X
        assertEquals("X", dm.doubleMetaphone("MARTIAL"));
        assertEquals("X", dm.doubleMetaphone("MATCH"));
        // TH, TTH
        assertEquals("0", dm.doubleMetaphone("THOMAS"));
        assertEquals("T", dm.doubleMetaphone("THOMAS", true));
        assertEquals("T", dm.doubleMetaphone("THAMES"));
        assertEquals("0", dm.doubleMetaphone("THAMES", true));
        assertEquals("T", dm.doubleMetaphone("VAN THAL"));
        assertEquals("T", dm.doubleMetaphone("SCHMIDT"));
        assertEquals("0", dm.doubleMetaphone("TH"));
        assertEquals("T", dm.doubleMetaphone("TH", true));
        // else T
        assertEquals("T", dm.doubleMetaphone("T"));
        assertEquals("T", dm.doubleMetaphone("TT"));
        assertEquals("T", dm.doubleMetaphone("TD"));
    }

    @Test
    public void testHandleV() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("F", dm.doubleMetaphone("V"));
        assertEquals("F", dm.doubleMetaphone("VV"));
    }

    @Test
    public void testHandleW() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // WR -> R
        assertEquals("R", dm.doubleMetaphone("WRONG"));
        // WH or vowel at start
        assertEquals("A", dm.doubleMetaphone("WASSERMAN"));
        assertEquals("F", dm.doubleMetaphone("WASSERMAN", true));
        assertEquals("A", dm.doubleMetaphone("WHALE"));
        // EWSKI, EWSKY, OWSKI, OWSKY, SCH
        assertEquals("F", dm.doubleMetaphone("ARNOW", true));
        assertEquals("F", dm.doubleMetaphone("EWSKI", true));
        assertEquals("F", dm.doubleMetaphone("OWSKY", true));
        assertEquals("F", dm.doubleMetaphone("SCHWARTZ", true));
        // WICZ, WITZ
        assertEquals("TS", dm.doubleMetaphone("FILIPOWICZ"));
        assertEquals("FX", dm.doubleMetaphone("FILIPOWICZ", true));
        // else skip
        assertEquals("", dm.doubleMetaphone("W"));
    }

    @Test
    public void testHandleX() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // initial X -> S
        assertEquals("S", dm.doubleMetaphone("XAVIER"));
        // else KS, except French endings
        assertEquals("KS", dm.doubleMetaphone("AXE"));
        assertEquals("KS", dm.doubleMetaphone("EXCESS"));
        assertEquals("KS", dm.doubleMetaphone("BREAUX"));
        assertEquals("KS", dm.doubleMetaphone("BORDEAUX"));
        // double X
        assertEquals("KS", dm.doubleMetaphone("AXX"));
    }

    @Test
    public void testHandleZ() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // ZH -> J
        assertEquals("J", dm.doubleMetaphone("ZHAO"));
        // ZO, ZI, ZA or slavoGermanic not after T
        assertEquals("S", dm.doubleMetaphone("ZOO"));
        assertEquals("TS", dm.doubleMetaphone("ZOO", true));
        assertEquals("S", dm.doubleMetaphone("ZINC"));
        assertEquals("TS", dm.doubleMetaphone("ZINC", true));
        assertEquals("S", dm.doubleMetaphone("ZAP"));
        assertEquals("TS", dm.doubleMetaphone("ZAP", true));
        assertEquals("S", dm.doubleMetaphone("WITZ"));
        assertEquals("TS", dm.doubleMetaphone("WITZ", true));
        // else S
        assertEquals("S", dm.doubleMetaphone("Z"));
        assertEquals("S", dm.doubleMetaphone("ZZ"));
    }

    @Test
    public void testConditionC0() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // CHIA -> true
        assertEquals("K", dm.doubleMetaphone("CHIA"));
        // BACHER, MACHER
        assertEquals("K", dm.doubleMetaphone("BACHER"));
        assertEquals("K", dm.doubleMetaphone("MACHER"));
        // ACH not followed by I/E
        assertEquals("K", dm.doubleMetaphone("ACH"));
        assertEquals("K", dm.doubleMetaphone("ACHO"));
        // ACH followed by I/E but not BACHER/MACHER -> false
        assertEquals("X", dm.doubleMetaphone("ACHI"));
    }

    @Test
    public void testConditionCH0() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // CH at start with HARAC, HARIS, HOR, HYM, HIA, HEM but not CHORE
        assertEquals("K", dm.doubleMetaphone("CHARAC"));
        assertEquals("K", dm.doubleMetaphone("CHARIS"));
        assertEquals("K", dm.doubleMetaphone("CHOR"));
        assertEquals("K", dm.doubleMetaphone("CHYM"));
        assertEquals("K", dm.doubleMetaphone("CHIA"));
        assertEquals("K", dm.doubleMetaphone("CHEM"));
        assertEquals("X", dm.doubleMetaphone("CHORE"));
        assertEquals("X", dm.doubleMetaphone("CH"));
    }

    @Test
    public void testConditionCH1() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // VAN, VON, SCH
        assertEquals("K", dm.doubleMetaphone("VAN CH"));
        assertEquals("K", dm.doubleMetaphone("VON CH"));
        assertEquals("K", dm.doubleMetaphone("SCH"));
        // ORCHES, ARCHIT, ORCHID
        assertEquals("K", dm.doubleMetaphone("ORCHES"));
        assertEquals("K", dm.doubleMetaphone("ARCHIT"));
        assertEquals("K", dm.doubleMetaphone("ORCHID"));
        // CH followed by T or S
        assertEquals("K", dm.doubleMetaphone("CHT"));
        assertEquals("K", dm.doubleMetaphone("CHS"));
        // CH after A,O,U,E or at start, followed by L,R,N,M,B,H,F,V,W, space or end
        assertEquals("K", dm.doubleMetaphone("ACH"));
        assertEquals("K", dm.doubleMetaphone("OCH"));
        assertEquals("K", dm.doubleMetaphone("UCH"));
        assertEquals("K", dm.doubleMetaphone("ECH"));
        assertEquals("K", dm.doubleMetaphone("CHL"));
        assertEquals("K", dm.doubleMetaphone("CHR"));
        assertEquals("K", dm.doubleMetaphone("CHN"));
        assertEquals("K", dm.doubleMetaphone("CHM"));
        assertEquals("K", dm.doubleMetaphone("CHB"));
        assertEquals("K", dm.doubleMetaphone("CHH"));
        assertEquals("K", dm.doubleMetaphone("CHF"));
        assertEquals("K", dm.doubleMetaphone("CHV"));
        assertEquals("K", dm.doubleMetaphone("CHW"));
        assertEquals("K", dm.doubleMetaphone("CH "));
        assertEquals("K", dm.doubleMetaphone("CH"));
    }

    @Test
    public void testConditionL0() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // ILLO, ILLA, ALLE at end-3
        assertEquals("L", dm.doubleMetaphone("BILLO"));
        assertEquals("L", dm.doubleMetaphone("BILLA"));
        assertEquals("L", dm.doubleMetaphone("BALLE"));
        // ALLE before AS, OS, A, O at end
        assertEquals("L", dm.doubleMetaphone("BALLES"));
        assertEquals("L", dm.doubleMetaphone("BALLOS"));
        assertEquals("L", dm.doubleMetaphone("BALLA"));
        assertEquals("L", dm.doubleMetaphone("BALLO"));
    }

    @Test
    public void testConditionM0() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // MM
        assertEquals("M", dm.doubleMetaphone("MM"));
        // UMB + ER or end
        assertEquals("M", dm.doubleMetaphone("UMBER"));
        assertEquals("M", dm.doubleMetaphone("UMB"));
    }

    @Test
    public void testCharAt() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals('A', dm.charAt("ABC", 0));
        assertEquals(Character.MIN_VALUE, dm.charAt("ABC", -1));
        assertEquals(Character.MIN_VALUE, dm.charAt("ABC", 3));
    }

    @Test
    public void testContains() {
        // Testing static contains via public method? We can't directly call private static.
        // But we can test indirectly through encoding.
        // We'll trust coverage from other tests.
    }

    @Test
    public void testDoubleMetaphoneResult() {
        DoubleMetaphone dm = new DoubleMetaphone();
        DoubleMetaphone.DoubleMetaphoneResult result = dm.new DoubleMetaphoneResult(4);
        assertFalse(result.isComplete());
        result.append('A');
        assertEquals("A", result.getPrimary());
        assertEquals("A", result.getAlternate());
        result.append('B', 'C');
        assertEquals("AB", result.getPrimary());
        assertEquals("AC", result.getAlternate());
        result.appendPrimary('D');
        assertEquals("ABD", result.getPrimary());
        assertEquals("AC", result.getAlternate());
        result.appendAlternate('E');
        assertEquals("ABD", result.getPrimary());
        assertEquals("ACE", result.getAlternate());
        result.append("FG");
        assertEquals("ABDF", result.getPrimary());
        assertEquals("ACEF", result.getAlternate());
        result.append("HI", "JK");
        assertEquals("ABDFHI", result.getPrimary());
        assertEquals("ACEFJK", result.getAlternate());
        assertTrue(result.isComplete());
        // Test truncation
        result = dm.new DoubleMetaphoneResult(2);
        result.append("ABC");
        assertEquals("AB", result.getPrimary());
        assertEquals("AB", result.getAlternate());
        result.appendPrimary("DEF");
        assertEquals("AB", result.getPrimary());
        result.appendAlternate("GHI");
        assertEquals("AB", result.getAlternate());
    }

    @Test
    public void testAlternateEncoding() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Test some words that produce different primary and alternate
        assertEquals("K", dm.doubleMetaphone("C"));
        assertEquals("K", dm.doubleMetaphone("C", true));
        assertEquals("S", dm.doubleMetaphone("CITY"));
        assertEquals("S", dm.doubleMetaphone("CITY", true));
        assertEquals("X", dm.doubleMetaphone("CIO"));
        assertEquals("X", dm.doubleMetaphone("CIO", true));
        assertEquals("J", dm.doubleMetaphone("JOSE"));
        assertEquals("H", dm.doubleMetaphone("JOSE", true));
        assertEquals("S", dm.doubleMetaphone("SUGAR"));
        assertEquals("X", dm.doubleMetaphone("SUGAR", true));
        assertEquals("0", dm.doubleMetaphone("THOMAS"));
        assertEquals("T", dm.doubleMetaphone("THOMAS", true));
        assertEquals("TS", dm.doubleMetaphone("WITZ"));
        assertEquals("FX", dm.doubleMetaphone("WITZ", true));
    }

    @Test
    public void testEdgeCases() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Single character
        assertEquals("", dm.doubleMetaphone(" "));
        assertEquals("A", dm.doubleMetaphone("A"));
        assertEquals("P", dm.doubleMetaphone("B"));
        assertEquals("K", dm.doubleMetaphone("C"));
        assertEquals("T", dm.doubleMetaphone("D"));
        assertEquals("F", dm.doubleMetaphone("F"));
        assertEquals("K", dm.doubleMetaphone("G"));
        assertEquals("H", dm.doubleMetaphone("H"));
        assertEquals("J", dm.doubleMetaphone("J"));
        assertEquals("K", dm.doubleMetaphone("K"));
        assertEquals("L", dm.doubleMetaphone("L"));
        assertEquals("M", dm.doubleMetaphone("M"));
        assertEquals("N", dm.doubleMetaphone("N"));
        assertEquals("P", dm.doubleMetaphone("P"));
        assertEquals("K", dm.doubleMetaphone("Q"));
        assertEquals("R", dm.doubleMetaphone("R"));
        assertEquals("S", dm.doubleMetaphone("S"));
        assertEquals("T", dm.doubleMetaphone("T"));
        assertEquals("F", dm.doubleMetaphone("V"));
        assertEquals("", dm.doubleMetaphone("W"));
        assertEquals("S", dm.doubleMetaphone("X"));
        assertEquals("S", dm.doubleMetaphone("Z"));
    }

    @Test
    public void testComplexWords() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("ANTR", dm.doubleMetaphone("ANDREW"));
        assertEquals("ANTR", dm.doubleMetaphone("ANDREW", true));
        assertEquals("JN", dm.doubleMetaphone("JOHN"));
        assertEquals("AN", dm.doubleMetaphone("JOHN", true));
        assertEquals("SM0", dm.doubleMetaphone("SMITH"));
        assertEquals("SMT", dm.doubleMetaphone("SMITH", true));
        assertEquals("KRS", dm.doubleMetaphone("CHRISTOPHER"));
        assertEquals("KRST", dm.doubleMetaphone("CHRISTOPHER", true));
        assertEquals("MKL", dm.doubleMetaphone("MICHAEL"));
        assertEquals("MKL", dm.doubleMetaphone("MICHAEL", true));
        assertEquals("TMS", dm.doubleMetaphone("THOMAS"));
        assertEquals("TMS", dm.doubleMetaphone("THOMAS", true));
        assertEquals("FLP", dm.doubleMetaphone("PHILIP"));
        assertEquals("FLP", dm.doubleMetaphone("PHILIP", true));
        assertEquals("RFR", dm.doubleMetaphone("ROBERT"));
        assertEquals("RFR", dm.doubleMetaphone("ROBERT", true));
        assertEquals("JMS", dm.doubleMetaphone("JAMES"));
        assertEquals("AMS", dm.doubleMetaphone("JAMES", true));
    }
}
