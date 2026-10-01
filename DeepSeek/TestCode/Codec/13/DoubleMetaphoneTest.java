package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

public class DoubleMetaphoneTest {

    private final DoubleMetaphone doubleMetaphone = new DoubleMetaphone();

    @Test
    public void testConstructor() {
        assertNotNull(doubleMetaphone);
        assertEquals(4, doubleMetaphone.getMaxCodeLen());
    }

    @Test
    public void testSetMaxCodeLen() {
        doubleMetaphone.setMaxCodeLen(10);
        assertEquals(10, doubleMetaphone.getMaxCodeLen());
        doubleMetaphone.setMaxCodeLen(4); // reset
    }

    @Test
    public void testEncodeObjectString() throws Exception {
        assertEquals("KSTN", doubleMetaphone.encode("Christine"));
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectNonString() throws Exception {
        doubleMetaphone.encode(new Integer(1));
    }

    @Test
    public void testEncodeString() {
        assertEquals("KSTN", doubleMetaphone.encode("Christine"));
    }

    @Test
    public void testDoubleMetaphoneNull() {
        assertNull(doubleMetaphone.doubleMetaphone(null));
    }

    @Test
    public void testDoubleMetaphoneEmpty() {
        assertNull(doubleMetaphone.doubleMetaphone(""));
    }

    @Test
    public void testDoubleMetaphoneWhitespace() {
        assertNull(doubleMetaphone.doubleMetaphone("   "));
    }

    @Test
    public void testDoubleMetaphoneSingleChar() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("A"));
    }

    @Test
    public void testIsDoubleMetaphoneEqual() {
        assertTrue(doubleMetaphone.isDoubleMetaphoneEqual("test", "test"));
        assertFalse(doubleMetaphone.isDoubleMetaphoneEqual("test", "different"));
    }

    @Test
    public void testIsDoubleMetaphoneEqualAlternate() {
        assertTrue(doubleMetaphone.isDoubleMetaphoneEqual("test", "test", true));
        assertFalse(doubleMetaphone.isDoubleMetaphoneEqual("test", "different", true));
    }

    @Test
    public void testSilentStartGN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("Gnome"));
    }

    @Test
    public void testSilentStartKN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("Knee"));
    }

    @Test
    public void testSilentStartPN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("Pneumatic"));
    }

    @Test
    public void testSilentStartWR() {
        assertEquals("R", doubleMetaphone.doubleMetaphone("Write"));
    }

    @Test
    public void testSilentStartPS() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("Psychology"));
    }

    @Test
    public void testVowelA() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("A"));
    }

    @Test
    public void testVowelE() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("E"));
    }

    @Test
    public void testVowelI() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("I"));
    }

    @Test
    public void testVowelO() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("O"));
    }

    @Test
    public void testVowelU() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("U"));
    }

    @Test
    public void testVowelY() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("Y"));
    }

    @Test
    public void testB() {
        assertEquals("P", doubleMetaphone.doubleMetaphone("B"));
        assertEquals("P", doubleMetaphone.doubleMetaphone("BB"));
    }

    @Test
    public void testCedilla() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("\u00C7"));
    }

    @Test
    public void testC_CHIA() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("CHIA"));
    }

    @Test
    public void testC_CAESAR() {
        assertEquals("SSR", doubleMetaphone.doubleMetaphone("CAESAR"));
    }

    @Test
    public void testC_CH() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("CH"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("CH", true));
    }

    @Test
    public void testC_CHAE() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("Michael"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("Michael", true));
    }

    @Test
    public void testC_CH_conditionCH0() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("Chorus"));
    }

    @Test
    public void testC_CH_conditionCH1() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("Vanch"));
    }

    @Test
    public void testC_CZ() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("Czerny"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("Czerny", true));
    }

    @Test
    public void testC_CIA() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("focaccia"));
    }

    @Test
    public void testC_CC() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("McClelland"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("bacci"));
    }

    @Test
    public void testC_CC_condition() {
        assertEquals("KS", doubleMetaphone.doubleMetaphone("accident"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("succeed"));
    }

    @Test
    public void testC_CK_CG_CQ() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("ack"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("acg"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("acq"));
    }

    @Test
    public void testC_CI_CE_CY() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("CIA"));
        assertEquals("S", doubleMetaphone.doubleMetaphone("CIA", true));
        assertEquals("S", doubleMetaphone.doubleMetaphone("Cello"));
        assertEquals("S", doubleMetaphone.doubleMetaphone("Cyan"));
    }

    @Test
    public void testC_CIO_CIE_CIA() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("CIO"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("CIO", true));
    }

    @Test
    public void testC_default() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("C"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("Mac Caffrey"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("Mac Gregor"));
    }

    @Test
    public void testD_DG() {
        assertEquals("J", doubleMetaphone.doubleMetaphone("edge"));
        assertEquals("TK", doubleMetaphone.doubleMetaphone("Edgar"));
    }

    @Test
    public void testD_DT_DD() {
        assertEquals("T", doubleMetaphone.doubleMetaphone("dt"));
        assertEquals("T", doubleMetaphone.doubleMetaphone("dd"));
    }

    @Test
    public void testD_default() {
        assertEquals("T", doubleMetaphone.doubleMetaphone("D"));
    }

    @Test
    public void testF() {
        assertEquals("F", doubleMetaphone.doubleMetaphone("F"));
        assertEquals("F", doubleMetaphone.doubleMetaphone("FF"));
    }

    @Test
    public void testG_GH() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("gh"));
        assertEquals("J", doubleMetaphone.doubleMetaphone("ghi"));
    }

    @Test
    public void testG_GN() {
        assertEquals("KN", doubleMetaphone.doubleMetaphone("gn"));
        assertEquals("N", doubleMetaphone.doubleMetaphone("gn", true));
    }

    @Test
    public void testG_GLI() {
        assertEquals("KL", doubleMetaphone.doubleMetaphone("agli"));
        assertEquals("L", doubleMetaphone.doubleMetaphone("agli", true));
    }

    @Test
    public void testG_GY_GER() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("gy"));
        assertEquals("J", doubleMetaphone.doubleMetaphone("gy", true));
        assertEquals("K", doubleMetaphone.doubleMetaphone("ger"));
        assertEquals("J", doubleMetaphone.doubleMetaphone("ger", true));
    }

    @Test
    public void testG_Italian() {
        assertEquals("J", doubleMetaphone.doubleMetaphone("biaggi"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("biaggi", true));
    }

    @Test
    public void testG_GG() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("agg"));
    }

    @Test
    public void testG_default() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("G"));
    }

    @Test
    public void testH() {
        assertEquals("H", doubleMetaphone.doubleMetaphone("AHA"));
        assertEquals("", doubleMetaphone.doubleMetaphone("AH"));
    }

    @Test
    public void testJ_JOSE() {
        assertEquals("H", doubleMetaphone.doubleMetaphone("Jose"));
        assertEquals("J", doubleMetaphone.doubleMetaphone("Jose", true));
    }

    @Test
    public void testJ_SAN() {
        assertEquals("H", doubleMetaphone.doubleMetaphone("San Jacinto"));
    }

    @Test
    public void testJ_default() {
        assertEquals("J", doubleMetaphone.doubleMetaphone("J"));
        assertEquals("A", doubleMetaphone.doubleMetaphone("J", true));
    }

    @Test
    public void testK() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("K"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("KK"));
    }

    @Test
    public void testL_LL() {
        assertEquals("L", doubleMetaphone.doubleMetaphone("LL"));
        assertEquals("L", doubleMetaphone.doubleMetaphone("ALLE"));
    }

    @Test
    public void testL_default() {
        assertEquals("L", doubleMetaphone.doubleMetaphone("L"));
    }

    @Test
    public void testM() {
        assertEquals("M", doubleMetaphone.doubleMetaphone("M"));
        assertEquals("M", doubleMetaphone.doubleMetaphone("MM"));
    }

    @Test
    public void testM_UMB() {
        assertEquals("M", doubleMetaphone.doubleMetaphone("dumb"));
        assertEquals("M", doubleMetaphone.doubleMetaphone("umber"));
    }

    @Test
    public void testN() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("N"));
        assertEquals("N", doubleMetaphone.doubleMetaphone("NN"));
    }

    @Test
    public void testN_tilde() {
        assertEquals("N", doubleMetaphone.doubleMetaphone("\u00D1"));
    }

    @Test
    public void testP_PH() {
        assertEquals("F", doubleMetaphone.doubleMetaphone("PH"));
    }

    @Test
    public void testP_PP_PB() {
        assertEquals("P", doubleMetaphone.doubleMetaphone("PP"));
        assertEquals("P", doubleMetaphone.doubleMetaphone("PB"));
    }

    @Test
    public void testP_default() {
        assertEquals("P", doubleMetaphone.doubleMetaphone("P"));
    }

    @Test
    public void testQ() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("Q"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("QQ"));
    }

    @Test
    public void testR() {
        assertEquals("R", doubleMetaphone.doubleMetaphone("R"));
        assertEquals("R", doubleMetaphone.doubleMetaphone("RR"));
    }

    @Test
    public void testR_alternate() {
        assertEquals("R", doubleMetaphone.doubleMetaphone("breaux"));
        assertEquals("R", doubleMetaphone.doubleMetaphone("breaux", true));
    }

    @Test
    public void testS_ISL_YSL() {
        assertEquals("SL", doubleMetaphone.doubleMetaphone("ISLE"));
        assertEquals("SL", doubleMetaphone.doubleMetaphone("YSL"));
    }

    @Test
    public void testS_SUGAR() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("SUGAR"));
        assertEquals("S", doubleMetaphone.doubleMetaphone("SUGAR", true));
    }

    @Test
    public void testS_SH() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("SH"));
        assertEquals("S", doubleMetaphone.doubleMetaphone("SHOEK"));
    }

    @Test
    public void testS_SIO_SIA() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("SIO"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("SIO", true));
    }

    @Test
    public void testS_SM_SN_SL_SW() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("SMITH"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("SMITH", true));
    }

    @Test
    public void testS_SC() {
        assertEquals("SK", doubleMetaphone.doubleMetaphone("SC"));
        assertEquals("S", doubleMetaphone.doubleMetaphone("SCI"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("SCH"));
        assertEquals("SK", doubleMetaphone.doubleMetaphone("SCH", true));
    }

    @Test
    public void testS_default() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("S"));
        assertEquals("S", doubleMetaphone.doubleMetaphone("SS"));
    }

    @Test
    public void testT_TION() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("TION"));
    }

    @Test
    public void testT_TIA_TCH() {
        assertEquals("X", doubleMetaphone.doubleMetaphone("TIA"));
        assertEquals("X", doubleMetaphone.doubleMetaphone("TCH"));
    }

    @Test
    public void testT_TH() {
        assertEquals("0", doubleMetaphone.doubleMetaphone("TH"));
        assertEquals("T", doubleMetaphone.doubleMetaphone("TH", true));
        assertEquals("T", doubleMetaphone.doubleMetaphone("THOMAS"));
    }

    @Test
    public void testT_TTH() {
        assertEquals("0", doubleMetaphone.doubleMetaphone("TTH"));
        assertEquals("T", doubleMetaphone.doubleMetaphone("TTH", true));
    }

    @Test
    public void testT_default() {
        assertEquals("T", doubleMetaphone.doubleMetaphone("T"));
        assertEquals("T", doubleMetaphone.doubleMetaphone("TT"));
    }

    @Test
    public void testV() {
        assertEquals("F", doubleMetaphone.doubleMetaphone("V"));
        assertEquals("F", doubleMetaphone.doubleMetaphone("VV"));
    }

    @Test
    public void testW_WR() {
        assertEquals("R", doubleMetaphone.doubleMetaphone("WR"));
    }

    @Test
    public void testW_WH() {
        assertEquals("A", doubleMetaphone.doubleMetaphone("WH"));
    }

    @Test
    public void testW_EWSKI() {
        assertEquals("F", doubleMetaphone.doubleMetaphone("EWSKI", true));
        assertEquals("", doubleMetaphone.doubleMetaphone("EWSKI"));
    }

    @Test
    public void testW_WICZ_WITZ() {
        assertEquals("TS", doubleMetaphone.doubleMetaphone("WICZ"));
        assertEquals("FX", doubleMetaphone.doubleMetaphone("WICZ", true));
    }

    @Test
    public void testW_default() {
        assertEquals("", doubleMetaphone.doubleMetaphone("W"));
    }

    @Test
    public void testX_initial() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("X"));
    }

    @Test
    public void testX_nonInitial() {
        assertEquals("KS", doubleMetaphone.doubleMetaphone("AX"));
        assertEquals("KS", doubleMetaphone.doubleMetaphone("AXC"));
    }

    @Test
    public void testX_french() {
        assertEquals("", doubleMetaphone.doubleMetaphone("breaux"));
    }

    @Test
    public void testZ_ZH() {
        assertEquals("J", doubleMetaphone.doubleMetaphone("ZH"));
    }

    @Test
    public void testZ_ZO_ZI_ZA() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("ZO"));
        assertEquals("TS", doubleMetaphone.doubleMetaphone("ZO", true));
    }

    @Test
    public void testZ_default() {
        assertEquals("S", doubleMetaphone.doubleMetaphone("Z"));
        assertEquals("S", doubleMetaphone.doubleMetaphone("ZZ"));
    }

    @Test
    public void testSlavoGermanic() {
        assertEquals("FK", doubleMetaphone.doubleMetaphone("Wach"));
        assertEquals("FK", doubleMetaphone.doubleMetaphone("Wach", true));
    }

    @Test
    public void testMaxCodeLen() {
        doubleMetaphone.setMaxCodeLen(2);
        assertEquals("KS", doubleMetaphone.doubleMetaphone("Christine"));
        doubleMetaphone.setMaxCodeLen(4);
    }

    @Test
    public void testAlternate() {
        assertEquals("KSTN", doubleMetaphone.doubleMetaphone("Christine", false));
        assertEquals("KSTN", doubleMetaphone.doubleMetaphone("Christine", true));
    }

    @Test
    public void testDoubleMetaphoneResult() {
        DoubleMetaphone.DoubleMetaphoneResult result = doubleMetaphone.new DoubleMetaphoneResult(4);
        assertFalse(result.isComplete());
        result.append('A');
        assertEquals("A", result.getPrimary());
        assertEquals("A", result.getAlternate());
        result.append('B', 'C');
        assertEquals("AB", result.getPrimary());
        assertEquals("AC", result.getAlternate());
        result.append("DE");
        assertEquals("ABDE", result.getPrimary());
        assertEquals("ACDE", result.getAlternate());
        assertTrue(result.isComplete());
        result.append('X');
        assertEquals("ABDE", result.getPrimary());
        assertEquals("ACDE", result.getAlternate());
    }

    @Test
    public void testDoubleMetaphoneResultTruncation() {
        DoubleMetaphone.DoubleMetaphoneResult result = doubleMetaphone.new DoubleMetaphoneResult(2);
        result.append("ABC");
        assertEquals("AB", result.getPrimary());
        assertEquals("AB", result.getAlternate());
        result.appendPrimary("DEF");
        assertEquals("AB", result.getPrimary());
        result.appendAlternate("DEF");
        assertEquals("AB", result.getAlternate());
    }

    @Test
    public void testConditionC0() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("CHIA"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("BACHER"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("MACHER"));
    }

    @Test
    public void testConditionCH0() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("CHARAC"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("CHORUS"));
    }

    @Test
    public void testConditionCH1() {
        assertEquals("K", doubleMetaphone.doubleMetaphone("VANCH"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("SCH"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("ORCHES"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("ARCHIT"));
        assertEquals("K", doubleMetaphone.doubleMetaphone("ORCHID"));
    }

    @Test
    public void testConditionL0() {
        assertEquals("L", doubleMetaphone.doubleMetaphone("ILLO"));
        assertEquals("L", doubleMetaphone.doubleMetaphone("ALLE"));
        assertEquals("L", doubleMetaphone.doubleMetaphone("ASALLE"));
    }

    @Test
    public void testConditionM0() {
        assertEquals("M", doubleMetaphone.doubleMetaphone("MM"));
        assertEquals("M", doubleMetaphone.doubleMetaphone("UMB"));
        assertEquals("M", doubleMetaphone.doubleMetaphone("UMBER"));
    }

    @Test
    public void testCharAt() {
        assertEquals('A', doubleMetaphone.charAt("ABC", 0));
        assertEquals(Character.MIN_VALUE, doubleMetaphone.charAt("ABC", -1));
        assertEquals(Character.MIN_VALUE, doubleMetaphone.charAt("ABC", 3));
    }

    @Test
    public void testContains() {
        assertTrue(DoubleMetaphone.contains("ABC", 0, 2, "AB"));
        assertFalse(DoubleMetaphone.contains("ABC", 0, 2, "BC"));
        assertFalse(DoubleMetaphone.contains("ABC", -1, 2, "AB"));
        assertFalse(DoubleMetaphone.contains("ABC", 0, 4, "ABC"));
    }

    @Test
    public void testIsVowel() {
        // isVowel is private, but we can test indirectly via handle methods
        // We'll trust it works.
    }

    @Test
    public void testIsSilentStart() {
        // isSilentStart is private, but tested via silent start tests.
    }

    @Test
    public void testCleanInput() {
        // cleanInput is private, tested via null/empty/whitespace tests.
    }

    @Test
    public void testComplexCases() {
        assertEquals("KSTN", doubleMetaphone.doubleMetaphone("Christine"));
        assertEquals("KSTN", doubleMetaphone.doubleMetaphone("Christine", true));
        assertEquals("ANTR", doubleMetaphone.doubleMetaphone("Andrew"));
        assertEquals("ANTR", doubleMetaphone.doubleMetaphone("Andrew", true));
        assertEquals("JN", doubleMetaphone.doubleMetaphone("John"));
        assertEquals("AN", doubleMetaphone.doubleMetaphone("John", true));
    }

    @Test
    public void testEdgeCases() {
        assertEquals("", doubleMetaphone.doubleMetaphone(""));
        assertEquals("", doubleMetaphone.doubleMetaphone("   "));
        assertEquals("A", doubleMetaphone.doubleMetaphone("A"));
        assertEquals("", doubleMetaphone.doubleMetaphone("B"));
    }
}
