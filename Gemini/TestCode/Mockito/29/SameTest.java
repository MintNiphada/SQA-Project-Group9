package org.mockito.internal.matchers;

import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SameTest {

    @Test
    public void testMatchesSameInstance() {
        Object obj = new Object();
        Same same = new Same(obj);
        Assert.assertTrue(same.matches(obj));
    }

    @Test
    public void testMatchesDifferentInstanceSameContent() {
        String str1 = new String("test");
        String str2 = new String("test");
        Same same = new Same(str1);
        Assert.assertFalse(same.matches(str2));
    }

    @Test
    public void testMatchesDifferentObjects() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Same same = new Same(obj1);
        Assert.assertFalse(same.matches(obj2));
    }

    @Test
    public void testMatchesNullWantedAndNullActual() {
        Same same = new Same(null);
        Assert.assertTrue(same.matches(null));
    }

    @Test
    public void testMatchesNullWantedAndNonNullActual() {
        Same same = new Same(null);
        Assert.assertFalse(same.matches(new Object()));
    }

    @Test
    public void testMatchesNonNullWantedAndNullActual() {
        Same same = new Same(new Object());
        Assert.assertFalse(same.matches(null));
    }

    @Test
    public void testDescribeToString() {
        Same same = new Same("hello");
        StringDescription description = new StringDescription();
        same.describeTo(description);
        Assert.assertEquals("same(\"hello\")", description.toString());
    }

    @Test
    public void testDescribeToCharacter() {
        Same same = new Same('a');
        StringDescription description = new StringDescription();
        same.describeTo(description);
        Assert.assertEquals("same('a')", description.toString());
    }

    @Test
    public void testDescribeToObject() {
        Object obj = new Object() {
            @Override
            public String toString() {
                return "customObject";
            }
        };
        Same same = new Same(obj);
        StringDescription description = new StringDescription();
        same.describeTo(description);
        Assert.assertEquals("same(customObject)", description.toString());
    }

    @Test
    public void testDescribeToInteger() {
        Same same = new Same(123);
        StringDescription description = new StringDescription();
        same.describeTo(description);
        Assert.assertEquals("same(123)", description.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testDescribeToNullWanted() {
        Same same = new Same(null);
        StringDescription description = new StringDescription();
        same.describeTo(description);
    }

    @Test
    public void testSerialization() throws Exception {
        Same original = new Same("serializedString");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Same deserialized = (Same) ois.readObject();
        ois.close();

        StringDescription description = new StringDescription();
        deserialized.describeTo(description);
        Assert.assertEquals("same(\"serializedString\")", description.toString());
    }
}
