package org.mockito.internal.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TimerTest {

    @Test
    public void testStartThenIsCountingTrue() {
        Timer timer = new Timer(1000);
        timer.start();
        assertTrue(timer.isCounting());
    }

    @Test
    public void testIsCountingFalseAfterDuration() throws InterruptedException {
        Timer timer = new Timer(10);
        timer.start();
        Thread.sleep(20);
        assertFalse(timer.isCounting());
    }

    @Test
    public void testIsCountingWithoutStartAssertsDisabled() {
        Timer timer = new Timer(1000);
        // Without calling start(), isCounting() should return false when assertions are disabled
        assertFalse(timer.isCounting());
    }

    @Test(expected = AssertionError.class)
    public void testIsCountingWithoutStartAssertsEnabled() {
        Timer timer = new Timer(1000);
        // When assertions are enabled, this should throw AssertionError
        timer.isCounting();
        fail("Expected AssertionError when assertions are enabled");
    }

    @Test
    public void testNegativeDuration() {
        Timer timer = new Timer(-100);
        timer.start();
        assertFalse(timer.isCounting());
    }

    @Test
    public void testZeroDuration() throws InterruptedException {
        Timer timer = new Timer(0);
        timer.start();
        Thread.sleep(1);
        assertFalse(timer.isCounting());
    }

    @Test
    public void testStartResetsTimer() throws InterruptedException {
        Timer timer = new Timer(1000);
        timer.start();
        Thread.sleep(500);
        timer.start(); // reset
        assertTrue(timer.isCounting());
        Thread.sleep(600);
        assertTrue(timer.isCounting()); // 600 <= 1000
        Thread.sleep(500);
        assertFalse(timer.isCounting()); // 1100 > 1000
    }

    @Test
    public void testLargeDuration() {
        Timer timer = new Timer(Long.MAX_VALUE);
        timer.start();
        assertTrue(timer.isCounting());
    }

    @Test
    public void testMultipleStarts() throws InterruptedException {
        Timer timer = new Timer(100);
        timer.start();
        Thread.sleep(50);
        assertTrue(timer.isCounting());
        timer.start(); // reset
        Thread.sleep(60);
        assertTrue(timer.isCounting()); // 60 <= 100
        Thread.sleep(50);
        assertFalse(timer.isCounting()); // 110 > 100
    }
}
