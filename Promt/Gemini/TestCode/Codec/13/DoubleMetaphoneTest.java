package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DoubleMetaphoneTest {

    private DoubleMetaphone encoder;

    @Before
    public void setUp() {
        this.encoder = new DoubleMetaphone();
    }

    @Test
    public void testCleanInputAndNullOrEmpty() throws Exception {
        assertNull(encoder.doubleMetaphone(null));
        assertNull(encoder.doubleMetaphone(""));
        assertNull(encoder.doubleMetaphone("   "));
        assertNull(encoder.encode((String) null));
        assertNull(encoder.encode(null));
    }

    @Test
    public void testEncodeObjectValid() throws Exception {
        Object result = encoder.encode((Object) "Smith");
        assertEquals("SM0", result);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalid() throws Exception {
        encoder.encode(new Integer(1234));
    }

    @Test
    public void testMaxCodeLength() {
        assertEquals(4, encoder.getMaxCodeLen());
        encoder.setMaxCodeLen(6);
        assertEquals(6, encoder.getMaxCodeLen());
        assertEquals("SMRK", encoder.doubleMetaphone("Schmidt", false));
        encoder.setMaxCodeLen(4);
    }

    @Test
    public void testIsDoubleMetaphoneEqual() {
        assertTrue(encoder.isDoubleMetaphoneEqual("Smith", "Schmidt", true));
        assertTrue(encoder.isDoubleMetaphoneEqual("Smith", "Smyth"));
        assertFalse(encoder.isDoubleMetaphoneEqual("Smith", "Jones"));
        assertFalse(encoder.isDoubleMetaphoneEqual("Smith", "Jones", true));
    }

    @Test
    public void testSilentStarts() {
        assertEquals("N", encoder.doubleMetaphone("GNOME"));
        assertEquals("N", encoder.doubleMetaphone("KNIGHT"));
        assertEquals("N", encoder.doubleMetaphone("PNEUMONIA"));
        assertEquals("R", encoder.doubleMetaphone("WRIST"));
        assertEquals("S", encoder.doubleMetaphone("PSALM"));
    }

    @Test
    public void testVowels() {
        assertEquals("A", encoder.doubleMetaphone("ALLEN"));
        assertEquals("E", encoder.doubleMetaphone("EVAN"));
        assertEquals("I", encoder.doubleMetaphone("IAN"));
        assertEquals("O", encoder.doubleMetaphone("OWEN"));
        assertEquals("U", encoder.doubleMetaphone("URBAN"));
        assertEquals("Y", encoder.doubleMetaphone("YORK"));
    }

    @Test
    public void testBasicConsonants() {
        assertEquals("P", encoder.doubleMetaphone("B"));
        assertEquals("P", encoder.doubleMetaphone("BB"));
        assertEquals("F", encoder.doubleMetaphone("F"));
        assertEquals("F", encoder.doubleMetaphone("FF"));
        assertEquals("K", encoder.doubleMetaphone("K"));
        assertEquals("K", encoder.doubleMetaphone("KK"));
        assertEquals("N", encoder.doubleMetaphone("N"));
        assertEquals("N", encoder.doubleMetaphone("NN"));
        assertEquals("K", encoder.doubleMetaphone("Q"));
        assertEquals("K", encoder.doubleMetaphone("QQ"));
        assertEquals("F", encoder.doubleMetaphone("V"));
        assertEquals("F", encoder.doubleMetaphone("VV"));
    }

    @Test
    public void testSpecialCharacters() {
        assertEquals("S", encoder.doubleMetaphone("\u00C7")); // Ç
        assertEquals("N", encoder.doubleMetaphone("\u00D1")); // Ñ
    }

    @Test
    public void testHandleC_ConditionC0() {
        assertEquals("K", encoder.doubleMetaphone("CHIA"));
        assertEquals("PKR", encoder.doubleMetaphone("BACHER"));
        assertEquals("MKR", encoder.doubleMetaphone("MACHER"));
    }

    @Test
    public void testHandleC_Caesar() {
        assertEquals("SSR", encoder.doubleMetaphone("CAESAR"));
    }

    @Test
    public void testHandleC_CH() {
        // Michael
        assertEquals("MKL", encoder.doubleMetaphone("MICHAEL", false));
        assertEquals("MXL", encoder.doubleMetaphone("MICHAEL", true));

        // Greek CH0
        assertEquals("KRK", encoder.doubleMetaphone("CHARACTER"));
        assertEquals("KRS", encoder.doubleMetaphone("CHARIS"));
        assertEquals("KR", encoder.doubleMetaphone("CHOR"));
        assertEquals("KM", encoder.doubleMetaphone("CHYM"));
        assertEquals("K", encoder.doubleMetaphone("CHIA"));
        assertEquals("KM", encoder.doubleMetaphone("CHEM"));
        assertEquals("XR", encoder.doubleMetaphone("CHORE"));

        // CH1
        assertEquals("FNTX", encoder.doubleMetaphone("VON CH"));
        assertEquals("SKX", encoder.doubleMetaphone("SCH"));
        assertEquals("ARK", encoder.doubleMetaphone("ORCHES"));
        assertEquals("ARK", encoder.doubleMetaphone("ARCHIT"));
        assertEquals("ARK", encoder.doubleMetaphone("ORCHID"));
        assertEquals("STX", encoder.doubleMetaphone("CHTH"));
        assertEquals("K", encoder.doubleMetaphone("ACH"));
        assertEquals("K", encoder.doubleMetaphone("ACHL"));

        // Other CH
        assertEquals("MK", encoder.doubleMetaphone("MCCH"));
        assertEquals("X", encoder.doubleMetaphone("CH"));
        assertEquals("AX", encoder.doubleMetaphone("ACHE", false));
        assertEquals("AK", encoder.doubleMetaphone("ACHE", true));
    }

    @Test
    public void testHandleC_CZ_and_CIA() {
        assertEquals("S", encoder.doubleMetaphone("CZ", false));
        assertEquals("X", encoder.doubleMetaphone("CZ", true));
        assertEquals("FKX", encoder.doubleMetaphone("FOCACCIA"));
    }

    @Test
    public void testHandleC_CC() {
        assertEquals("MKLN", encoder.doubleMetaphone("MCCLELLAND"));
        assertEquals("AKS", encoder.doubleMetaphone("ACCIDENT"));
        assertEquals("AKS", encoder.doubleMetaphone("ACCEDE"));
        assertEquals("SKS", encoder.doubleMetaphone("SUCCEED"));
        assertEquals("PKS", encoder.doubleMetaphone("BACCI"));
        assertEquals("PKX", encoder.doubleMetaphone("BACCHI"));
        assertEquals("PK", encoder.doubleMetaphone("BACC"));
    }

    @Test
    public void testHandleC_CK_CG_CQ() {
        assertEquals("K", encoder.doubleMetaphone("CK"));
        assertEquals("K", encoder.doubleMetaphone("CG"));
        assertEquals("K", encoder.doubleMetaphone("CQ"));
    }

    @Test
    public void testHandleC_CI_CE_CY() {
        assertEquals("S", encoder.doubleMetaphone("CIO", false));
        assertEquals("X", encoder.doubleMetaphone("CIO", true));
        assertEquals("S", encoder.doubleMetaphone("CIE", false));
        assertEquals("X", encoder.doubleMetaphone("CIE", true));
        assertEquals("S", encoder.doubleMetaphone("CIA", false));
        assertEquals("X", encoder.doubleMetaphone("CIA", true));
        assertEquals("S", encoder.doubleMetaphone("CITY"));
        assertEquals("S", encoder.doubleMetaphone("CENT"));
        assertEquals("S", encoder.doubleMetaphone("CYRIL"));
    }

    @Test
    public void testHandleC_Other() {
        assertEquals("MKFR", encoder.doubleMetaphone("MAC CAFFREY"));
        assertEquals("MKRK", encoder.doubleMetaphone("MAC GREGOR"));
        assertEquals("KK", encoder.doubleMetaphone("C C"));
        assertEquals("K", encoder.doubleMetaphone("CQ"));
        assertEquals("K", encoder.doubleMetaphone("CAT"));
    }

    @Test
    public void testHandleD() {
        assertEquals("AJ", encoder.doubleMetaphone("EDGE"));
        assertEquals("ATK", encoder.doubleMetaphone("EDGAR"));
        assertEquals("T", encoder.doubleMetaphone("DT"));
        assertEquals("T", encoder.doubleMetaphone("DD"));
        assertEquals("T", encoder.doubleMetaphone("D"));
    }

    @Test
    public void testHandleG_GH() {
        assertEquals("K", encoder.doubleMetaphone("NGH"));
        assertEquals("J", encoder.doubleMetaphone("GHI"));
        assertEquals("K", encoder.doubleMetaphone("GH"));
        assertEquals("P", encoder.doubleMetaphone("HUGHB"));
        assertEquals("LF", encoder.doubleMetaphone("LAUGH"));
        assertEquals("MKLF", encoder.doubleMetaphone("MCLAUGHLIN"));
        assertEquals("KF", encoder.doubleMetaphone("COUGH"));
        assertEquals("KF", encoder.doubleMetaphone("GOUGH"));
        assertEquals("RF", encoder.doubleMetaphone("ROUGH"));
        assertEquals("TF", encoder.doubleMetaphone("TOUGH"));
        assertEquals("AK", encoder.doubleMetaphone("AGH"));
        assertEquals("H", encoder.doubleMetaphone("HUGH"));
    }

    @Test
    public void testHandleG_GN() {
        assertEquals("KN", encoder.doubleMetaphone("AGNES", false));
        assertEquals("N", encoder.doubleMetaphone("AGNES", true));
        assertEquals("N", encoder.doubleMetaphone("GNEIS", false));
        assertEquals("KN", encoder.doubleMetaphone("GNEIS", true));
        assertEquals("KN", encoder.doubleMetaphone("GNEY"));
        assertEquals("KN", encoder.doubleMetaphone("KGN"));
    }

    @Test
    public void testHandleG_GLI() {
        assertEquals("KL", encoder.doubleMetaphone("TAGLIARO", false));
        assertEquals("L", encoder.doubleMetaphone("TAGLIARO", true));
        assertEquals("KL", encoder.doubleMetaphone("WTAGLIARO", false));
    }

    @Test
    public void testHandleG_Initial_and_GER_GY() {
        assertEquals("K", encoder.doubleMetaphone("GY", false));
        assertEquals("J", encoder.doubleMetaphone("GY", true));
        assertEquals("K", encoder.doubleMetaphone("GES", false));
        assertEquals("J", encoder.doubleMetaphone("GES", true));
        assertEquals("K", encoder.doubleMetaphone("GEL", false));
        assertEquals("J", encoder.doubleMetaphone("GEL", true));

        assertEquals("TNJR", encoder.doubleMetaphone("DANGER"));
        assertEquals("RNJR", encoder.doubleMetaphone("RANGER"));
        assertEquals("MNJR", encoder.doubleMetaphone("MANGER"));
        assertEquals("AJR", encoder.doubleMetaphone("EGER"));
        assertEquals("ARJ", encoder.doubleMetaphone("ERGY"));
        assertEquals("AJ", encoder.doubleMetaphone("OGY"));
        assertEquals("K", encoder.doubleMetaphone("GER", false));
        assertEquals("J", encoder.doubleMetaphone("GER", true));
    }

    @Test
    public void testHandleG_Italian_and_Misc() {
        assertEquals("FNTK", encoder.doubleMetaphone("VAN GET"));
        assertEquals("SKK", encoder.doubleMetaphone("SCHGET"));
        assertEquals("KT", encoder.doubleMetaphone("GET"));
        assertEquals("J", encoder.doubleMetaphone("GIER"));
        assertEquals("J", encoder.doubleMetaphone("GIO", false));
        assertEquals("K", encoder.doubleMetaphone("GIO", true));
        assertEquals("J", encoder.doubleMetaphone("BIAGGI", false));
        assertEquals("K", encoder.doubleMetaphone("BIAGGI", true));
        assertEquals("K", encoder.doubleMetaphone("GG"));
        assertEquals("K", encoder.doubleMetaphone("G"));
    }

    @Test
    public void testHandleH() {
        assertEquals("H", encoder.doubleMetaphone("HA"));
        assertEquals("AH", encoder.doubleMetaphone("AHA"));
        assertEquals("A", encoder.doubleMetaphone("AH"));
    }

    @Test
    public void testHandleJ() {
        assertEquals("HS", encoder.doubleMetaphone("JOSE"));
        assertEquals("SN", encoder.doubleMetaphone("SAN JOSE"));
        assertEquals("H", encoder.doubleMetaphone("JOSEPH", false));
        assertEquals("J", encoder.doubleMetaphone("JOSEPH", true));

        assertEquals("J", encoder.doubleMetaphone("J", false));
        assertEquals("A", encoder.doubleMetaphone("J", true));
        assertEquals("AJ", encoder.doubleMetaphone("AJAX", false));
        assertEquals("AH", encoder.doubleMetaphone("AJAX", true));
        assertEquals("AJ", encoder.doubleMetaphone("AJ", false));
        assertEquals("A ", encoder.doubleMetaphone("AJ", true));
        assertEquals("J", encoder.doubleMetaphone("JA"));
        assertEquals("SJ", encoder.doubleMetaphone("SJA"));
        assertEquals("J", encoder.doubleMetaphone("JJ"));
    }

    @Test
    public void testHandleL() {
        assertEquals("L", encoder.doubleMetaphone("CABRILLO", false));
        assertEquals("", encoder.doubleMetaphone("CABRILLO", true));
        assertEquals("L", encoder.doubleMetaphone("VILLA", false));
        assertEquals("", encoder.doubleMetaphone("VILLA", true));
        assertEquals("AL", encoder.doubleMetaphone("ALLE"));
        assertEquals("L", encoder.doubleMetaphone("LL"));
        assertEquals("L", encoder.doubleMetaphone("L"));
    }

    @Test
    public void testHandleM() {
        assertEquals("M", encoder.doubleMetaphone("MM"));
        assertEquals("TM", encoder.doubleMetaphone("DUMB"));
        assertEquals("TMR", encoder.doubleMetaphone("DUMBER"));
        assertEquals("M", encoder.doubleMetaphone("M"));
    }

    @Test
    public void testHandleP() {
        assertEquals("F", encoder.doubleMetaphone("PH"));
        assertEquals("P", encoder.doubleMetaphone("PP"));
        assertEquals("P", encoder.doubleMetaphone("PB"));
        assertEquals("P", encoder.doubleMetaphone("P"));
    }

    @Test
    public void testHandleR() {
        assertEquals("R", encoder.doubleMetaphone("TAGLIER", false));
        assertEquals("RR", encoder.doubleMetaphone("TAGLIER", true));
        assertEquals("MR", encoder.doubleMetaphone("MAIER", true));
        assertEquals("R", encoder.doubleMetaphone("RR"));
        assertEquals("R", encoder.doubleMetaphone("R"));
    }

    @Test
    public void testHandleS() {
        assertEquals("ALNT", encoder.doubleMetaphone("ISLAND"));
        assertEquals("AL", encoder.doubleMetaphone("ISLE"));
        assertEquals("KRL", encoder.doubleMetaphone("CARLISLE"));
        assertEquals("KRL", encoder.doubleMetaphone("CARLYSLE"));
        assertEquals("XKR", encoder.doubleMetaphone("SUGAR", false));
        assertEquals("SKR", encoder.doubleMetaphone("SUGAR", true));

        assertEquals("SM", encoder.doubleMetaphone("SHEIM"));
        assertEquals("SK", encoder.doubleMetaphone("SHOEK"));
        assertEquals("SM", encoder.doubleMetaphone("SHOLM"));
        assertEquals("S", encoder.doubleMetaphone("SHOLZ"));
        assertEquals("X", encoder.doubleMetaphone("SH"));

        assertEquals("S", encoder.doubleMetaphone("SIO"));
        assertEquals("S", encoder.doubleMetaphone("WSIO", false));
        assertEquals("S", encoder.doubleMetaphone("SIAN", false));
        assertEquals("X", encoder.doubleMetaphone("SIAN", true));

        assertEquals("S", encoder.doubleMetaphone("SMITH", false));
        assertEquals("X", encoder.doubleMetaphone("SMITH", true));
        assertEquals("S", encoder.doubleMetaphone("SNIDER", false));
        assertEquals("X", encoder.doubleMetaphone("SNIDER", true));
        assertEquals("S", encoder.doubleMetaphone("SZ", false));
        assertEquals("X", encoder.doubleMetaphone("SZ", true));

        assertEquals("RSN", encoder.doubleMetaphone("RESNAIS", false));
        assertEquals("RSNS", encoder.doubleMetaphone("RESNAIS", true));
        assertEquals("ARTS", encoder.doubleMetaphone("ARTOIS", true));
        assertEquals("S", encoder.doubleMetaphone("SS"));
        assertEquals("S", encoder.doubleMetaphone("SZ"));
    }

    @Test
    public void testHandleSC() {
        assertEquals("SKL", encoder.doubleMetaphone("SCHOOL"));
        assertEquals("SKNR", encoder.doubleMetaphone("SCHOONER"));
        assertEquals("XRMR", encoder.doubleMetaphone("SCHERMERHORN", false));
        assertEquals("SKRM", encoder.doubleMetaphone("SCHERMERHORN", true));
        assertEquals("XNKR", encoder.doubleMetaphone("SCHENKER", false));
        assertEquals("SKNK", encoder.doubleMetaphone("SCHENKER", true));
        assertEquals("X", encoder.doubleMetaphone("SCHMIDT", false));
        assertEquals("S", encoder.doubleMetaphone("SCHMIDT", true));
        assertEquals("X", encoder.doubleMetaphone("SCHW"));
        assertEquals("S", encoder.doubleMetaphone("SCIENTIFIC"));
        assertEquals("SK", encoder.doubleMetaphone("SCOTT"));
    }

    @Test
    public void testHandleT() {
        assertEquals("XN", encoder.doubleMetaphone("TION"));
        assertEquals("X", encoder.doubleMetaphone("TIA"));
        assertEquals("X", encoder.doubleMetaphone("TCH"));
        assertEquals("TMS", encoder.doubleMetaphone("THOMAS"));
        assertEquals("TMS", encoder.doubleMetaphone("THAMES"));
        assertEquals("FNT", encoder.doubleMetaphone("VAN TH"));
        assertEquals("SKT", encoder.doubleMetaphone("SCHTH"));
        assertEquals("0", encoder.doubleMetaphone("TH", false));
        assertEquals("T", encoder.doubleMetaphone("TH", true));
        assertEquals("T", encoder.doubleMetaphone("TT"));
        assertEquals("T", encoder.doubleMetaphone("TD"));
        assertEquals("T", encoder.doubleMetaphone("T"));
    }

    @Test
    public void testHandleW() {
        assertEquals("R", encoder.doubleMetaphone("WR"));
        assertEquals("A", encoder.doubleMetaphone("WASSERMAN", false));
        assertEquals("F", encoder.doubleMetaphone("WASSERMAN", true));
        assertEquals("A", encoder.doubleMetaphone("WH"));
        assertEquals("ARN", encoder.doubleMetaphone("ARNOW", false));
        assertEquals("ARNF", encoder.doubleMetaphone("ARNOW", true));
        assertEquals("F", encoder.doubleMetaphone("EWSKI", true));
        assertEquals("F", encoder.doubleMetaphone("EWSKY", true));
        assertEquals("F", encoder.doubleMetaphone("OWSKI", true));
        assertEquals("F", encoder.doubleMetaphone("OWSKY", true));
        assertEquals("SKF", encoder.doubleMetaphone("SCHW", true));
        assertEquals("TS", encoder.doubleMetaphone("WICZ", false));
        assertEquals("FX", encoder.doubleMetaphone("WICZ", true));
        assertEquals("TS", encoder.doubleMetaphone("WITZ", false));
        assertEquals("FX", encoder.doubleMetaphone("WITZ", true));
        assertEquals("", encoder.doubleMetaphone("W"));
    }

    @Test
    public void testHandleX() {
        assertEquals("S", encoder.doubleMetaphone("XAVIER"));
        assertEquals("PR", encoder.doubleMetaphone("BREAUX"));
        assertEquals("FKS", encoder.doubleMetaphone("FAX"));
        assertEquals("FKS", encoder.doubleMetaphone("FAXC"));
        assertEquals("FKS", encoder.doubleMetaphone("FAXX"));
    }

    @Test
    public void testHandleZ() {
        assertEquals("J", encoder.doubleMetaphone("ZHAO"));
        assertEquals("S", encoder.doubleMetaphone("ZO", false));
        assertEquals("TS", encoder.doubleMetaphone("ZO", true));
        assertEquals("S", encoder.doubleMetaphone("WZO", false));
        assertEquals("TS", encoder.doubleMetaphone("WZO", true));
        assertEquals("S", encoder.doubleMetaphone("TZ"));
        assertEquals("S", encoder.doubleMetaphone("ZZ"));
        assertEquals("S", encoder.doubleMetaphone("Z"));
    }

    @Test
    public void testProtectedCharAtAndContains() {
        assertEquals(Character.MIN_VALUE, encoder.charAt("TEST", -1));
        assertEquals(Character.MIN_VALUE, encoder.charAt("TEST", 4));
        assertEquals('T', encoder.charAt("TEST", 0));

        assertFalse(DoubleMetaphone.contains("TEST", -1, 2, "TE"));
        assertFalse(DoubleMetaphone.contains("TEST", 3, 2, "TE"));
        assertFalse(DoubleMetaphone.contains("TEST", 0, 2, "ES", "ST"));
        assertTrue(DoubleMetaphone.contains("TEST", 0, 2, "TE", "ES"));
    }

    @Test
    public void testDoubleMetaphoneResultClass() {
        DoubleMetaphone.DoubleMetaphoneResult result = encoder.new DoubleMetaphoneResult(4);
        result.append('A');
        result.append('B', 'C');
        result.append("DE");
        assertEquals("ABDE", result.getPrimary());
        assertEquals("ACDE", result.getAlternate());
        assertTrue(result.isComplete());

        DoubleMetaphone.DoubleMetaphoneResult result2 = encoder.new DoubleMetaphoneResult(2);
        result2.append("LONGSTRING", "LONGSTRING");
        assertEquals("LO", result2.getPrimary());
        assertEquals("LO", result2.getAlternate());
    }
}
