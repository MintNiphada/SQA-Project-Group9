package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CaverphoneTest {

    private Caverphone caverphone;

    @Before
    public void setUp() {
        this.caverphone = new Caverphone();
    }

    @Test
    public void testNullAndEmptyInput() {
        Assert.assertEquals("1111111111", this.caverphone.caverphone(null));
        Assert.assertEquals("1111111111", this.caverphone.caverphone(""));
        Assert.assertEquals("1111111111", this.caverphone.encode((String) null));
        Assert.assertEquals("1111111111", this.caverphone.encode(""));
    }

    @Test
    public void testNonAlphaFiltering() {
        // Numbers, spaces, and punctuation should be stripped out
        Assert.assertEquals(this.caverphone.caverphone("Peter"), this.caverphone.caverphone("P1e2t3e4r!"));
        Assert.assertEquals(this.caverphone.caverphone("Stevenson"), this.caverphone.caverphone("Steven-son"));
        Assert.assertEquals("1111111111", this.caverphone.caverphone("12345!@#$%^&*()_+"));
    }

    @Test
    public void testFinalEHandling() {
        // Tests rule 2.5: txt.replaceAll("e$", "")
        Assert.assertEquals(this.caverphone.caverphone("bake"), this.caverphone.caverphone("bak"));
    }

    @Test
    public void testStartOptions() {
        // ^cough -> cou2f
        Assert.assertTrue(this.caverphone.caverphone("cough").startsWith("KF"));
        // ^rough -> rou2f
        Assert.assertTrue(this.caverphone.caverphone("rough").startsWith("RF"));
        // ^tough -> tou2f
        Assert.assertTrue(this.caverphone.caverphone("tough").startsWith("TF"));
        // ^enough -> enou2f
        Assert.assertTrue(this.caverphone.caverphone("enough").startsWith("ANF"));
        // ^trough -> trou2f
        Assert.assertTrue(this.caverphone.caverphone("trough").startsWith("TRF"));
        // ^gn -> 2n
        Assert.assertEquals(this.caverphone.caverphone("gnat"), this.caverphone.caverphone("nat"));
        // ^mb -> m2
        Assert.assertTrue(this.caverphone.caverphone("mbappe").startsWith("MP"));
    }

    @Test
    public void testReplacementsRules() {
        // cq -> 2q -> K
        Assert.assertEquals(this.caverphone.caverphone("acquire"), this.caverphone.caverphone("aquire"));
        
        // ci -> si, ce -> se, cy -> sy
        Assert.assertEquals(this.caverphone.caverphone("cider"), this.caverphone.caverphone("sider"));
        Assert.assertEquals(this.caverphone.caverphone("centre"), this.caverphone.caverphone("sentre"));
        Assert.assertEquals(this.caverphone.caverphone("cycle"), this.caverphone.caverphone("sycle"));

        // tch -> 2ch
        Assert.assertEquals(this.caverphone.caverphone("catch"), this.caverphone.caverphone("cach"));

        // c, q, x -> k
        Assert.assertTrue(this.caverphone.caverphone("cat").startsWith("KT"));
        Assert.assertTrue(this.caverphone.caverphone("quick").startsWith("KWK"));
        Assert.assertTrue(this.caverphone.caverphone("xenon").startsWith("KNN"));

        // v -> f
        Assert.assertEquals(this.caverphone.caverphone("vase"), this.caverphone.caverphone("fase"));

        // dg -> 2g
        Assert.assertEquals(this.caverphone.caverphone("badge"), this.caverphone.caverphone("bage"));

        // tio -> sio, tia -> sia
        Assert.assertEquals(this.caverphone.caverphone("nation"), this.caverphone.caverphone("nasion"));
        Assert.assertEquals(this.caverphone.caverphone("spatial"), this.caverphone.caverphone("spasial"));

        // d -> t
        Assert.assertEquals(this.caverphone.caverphone("door"), this.caverphone.caverphone("toor"));

        // ph -> fh
        Assert.assertEquals(this.caverphone.caverphone("phone"), this.caverphone.caverphone("fone"));

        // b -> p
        Assert.assertEquals(this.caverphone.caverphone("bad"), this.caverphone.caverphone("pat"));

        // sh -> s2
        Assert.assertTrue(this.caverphone.caverphone("shoe").startsWith("S"));

        // z -> s
        Assert.assertEquals(this.caverphone.caverphone("zoo"), this.caverphone.caverphone("soo"));

        // ^[aeiou] -> A, [aeiou] -> 3
        Assert.assertTrue(this.caverphone.caverphone("apple").startsWith("AP"));
        Assert.assertTrue(this.caverphone.caverphone("eagle").startsWith("AK"));

        // j -> y, ^y3 -> Y3, ^y -> A, y -> 3
        Assert.assertTrue(this.caverphone.caverphone("jam").startsWith("YM"));
        Assert.assertTrue(this.caverphone.caverphone("yellow").startsWith("YL"));
        Assert.assertTrue(this.caverphone.caverphone("ytterbium").startsWith("AT"));

        // 3gh3 -> 3kh3, gh -> 22, g -> k
        Assert.assertEquals(this.caverphone.caverphone("aghar"), this.caverphone.caverphone("akar"));
        Assert.assertTrue(this.caverphone.caverphone("ghost").startsWith("KST"));
        Assert.assertTrue(this.caverphone.caverphone("gold").startsWith("KT"));

        // Consecutive consonants reduction: s+, t+, p+, k+, f+, m+, n+
        Assert.assertEquals(this.caverphone.caverphone("kiss"), this.caverphone.caverphone("kis"));
        Assert.assertEquals(this.caverphone.caverphone("butter"), this.caverphone.caverphone("buter"));
        Assert.assertEquals(this.caverphone.caverphone("apple"), this.caverphone.caverphone("aple"));
        Assert.assertEquals(this.caverphone.caverphone("account"), this.caverphone.caverphone("akount"));
        Assert.assertEquals(this.caverphone.caverphone("offer"), this.caverphone.caverphone("ofer"));
        Assert.assertEquals(this.caverphone.caverphone("summer"), this.caverphone.caverphone("sumer"));
        Assert.assertEquals(this.caverphone.caverphone("tunnel"), this.caverphone.caverphone("tunel"));

        // w3 -> W3, wh3 -> Wh3, w$ -> 3, w -> 2
        Assert.assertTrue(this.caverphone.caverphone("water").startsWith("WT"));
        Assert.assertTrue(this.caverphone.caverphone("what").startsWith("WT"));
        Assert.assertTrue(this.caverphone.caverphone("flow").endsWith("A111111111".substring(0, 5)));

        // ^h -> A, h -> 2
        Assert.assertTrue(this.caverphone.caverphone("hello").startsWith("AL"));
        Assert.assertTrue(this.caverphone.caverphone("behind").startsWith("PNT"));

        // r3 -> R3, r$ -> 3, r -> 2
        Assert.assertTrue(this.caverphone.caverphone("rain").startsWith("RN"));
        Assert.assertTrue(this.caverphone.caverphone("car").startsWith("KA"));

        // l3 -> L3, l$ -> 3, l -> 2
        Assert.assertTrue(this.caverphone.caverphone("light").startsWith("LT"));
        Assert.assertTrue(this.caverphone.caverphone("bell").startsWith("PA"));
    }

    @Test
    public void testKnownEqualPairs() {
        Assert.assertTrue(this.caverphone.isCaverphoneEqual("Lee", "Leigh"));
        Assert.assertTrue(this.caverphone.isCaverphoneEqual("Peter", "Peiter"));
        Assert.assertTrue(this.caverphone.isCaverphoneEqual("Stevenson", "Stephenson"));
        Assert.assertTrue(this.caverphone.isCaverphoneEqual("Smith", "Smyth"));
    }

    @Test
    public void testKnownUnequalPairs() {
        Assert.assertFalse(this.caverphone.isCaverphoneEqual("Peter", "Paul"));
        Assert.assertFalse(this.caverphone.isCaverphoneEqual("Thompson", "Johnson"));
    }

    @Test
    public void testEncodeString() {
        String result = this.caverphone.encode("Stevenson");
        Assert.assertNotNull(result);
        Assert.assertEquals(10, result.length());
        Assert.assertEquals(this.caverphone.caverphone("Stevenson"), result);
    }

    @Test
    public void testEncodeObjectValid() throws EncoderException {
        Object result = this.caverphone.encode((Object) "Stevenson");
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals(this.caverphone.caverphone("Stevenson"), result);
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectInvalidNotString() throws EncoderException {
        this.caverphone.encode(new Integer(42));
    }

    @Test(expected = EncoderException.class)
    public void testEncodeObjectNull() throws EncoderException {
        this.caverphone.encode((Object) null);
    }

    @Test
    public void testCaverphoneOutputLength() {
        // Output must always be exactly 10 characters long
        Assert.assertEquals(10, this.caverphone.caverphone("A").length());
        Assert.assertEquals(10, this.caverphone.caverphone("Supercalifragilisticexpialidocious").length());
        Assert.assertEquals(10, this.caverphone.caverphone("W").length());
        Assert.assertEquals(10, this.caverphone.caverphone("").length());
    }

    @Test
    public void testEndRulesWithTrailingThree() {
        // Tests txt.replaceAll("3$", "A") and subsequent padding/truncation
        // "banana" -> b-a-n-a-n-a ends with vowel -> 3 at end -> becomes A
        String encoded = this.caverphone.caverphone("banana");
        Assert.assertEquals(10, encoded.length());
        Assert.assertEquals("PNN1111111", encoded);
    }
}
