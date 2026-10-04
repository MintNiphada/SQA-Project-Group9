package org.mockito.internal.matchers;

import org.hamcrest.StringDescription;
import org.junit.Test;
import static org.junit.Assert.*;

public class SameTest {

    @Test
    public void matchesWithSameReference() {
        Object obj = new Object();
        Same same = new Same(obj);
        assertTrue(same.matches(obj));
    }

    @Test
    public void matchesWithDifferentReference() {
        Same same = new Same(new Object());
        assertFalse(same.matches(new Object()));
    }

    @Test
    public void matchesWithNullWantedAndNullActual() {
        Same same = new Same(null);
        assertTrue(same.matches(null));
    }

    @Test
    public void matchesWithNullWantedAndNonNullActual() {
        Same same = new Same(null);
        assertFalse(same.matches(new Object()));
    }

    @Test
    public void matchesWithNonNullWantedAndNullActual() {
        Same same = new Same(new Object());
        assertFalse(same.matches(null));
    }

    @Test
    public void describeToWithString() {
        Same same = new Same("hello");
        StringDescription desc = new StringDescription();
        same.describeTo(desc);
        assertEquals("same(\"hello\")", desc.toString());
    }

    @Test
    public void describeToWithCharacter() {
        Same same = new Same('A');
        StringDescription desc = new StringDescription();
        same.describeTo(desc);
        assertEquals("same('A')", desc.toString());
    }

    @Test
    public void describeToWithOtherObject() {
        Same same = new Same(123);
        StringDescription desc = new StringDescription();
        same.describeTo(desc);
        assertEquals("same(123)", desc.toString());
    }

    @Test(expected = NullPointerException.class)
    public void describeToWithNullWantedThrowsNullPointerException() {
        Same same = new Same(null);
        same.describeTo(new StringDescription());
    }
}
