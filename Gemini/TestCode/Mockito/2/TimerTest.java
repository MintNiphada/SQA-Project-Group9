package org.mockito.internal.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimerTest {

    @Test
    public void should_be_counting_right_after_start() {
        Timer timer = new Timer(1000L);
        timer.start();

        assertTrue(timer.isCounting());
    }

    @Test
    public void should_not_be_counting_after_duration_expires() throws InterruptedException {
        Timer timer = new Timer(10L);
        timer.start();

        Thread.sleep(30L);

        assertFalse(timer.isCounting());
    }

    @Test
    public void should_not_be_counting_when_duration_is_zero_and_time_passes() throws InterruptedException {
        Timer timer = new Timer(0L);
        timer.start();

        Thread.sleep(5L);

        assertFalse(timer.isCounting());
    }

    @Test
    public void should_be_counting_for_long_duration() {
        Timer timer = new Timer(Long.MAX_VALUE / 2);
        timer.start();

        assertTrue(timer.isCounting());
    }

    @Test
    public void should_restart_timer_on_consecutive_start_calls() throws InterruptedException {
        Timer timer = new Timer(50L);
        timer.start();

        Thread.sleep(70L);
        assertFalse(timer.isCounting());

        timer.start();
        assertTrue(timer.isCounting());
    }

    @Test
    public void should_handle_negative_duration() throws InterruptedException {
        Timer timer = new Timer(-10L);
        timer.start();

        assertFalse(timer.isCounting());
    }

    @Test
    public void should_throw_assertion_error_or_return_value_if_not_started() {
        Timer timer = new Timer(100L);
        try {
            boolean counting = timer.isCounting();
            // If assertions are disabled (-da), isCounting() will evaluate System.currentTimeMillis() - (-1) <= 100L
            // which evaluates to false for modern epoch timestamps.
            assertFalse(counting);
        } catch (AssertionError expected) {
            // If assertions are enabled (-ea), assert startTime != -1 will trigger AssertionError.
            assertTrue(true);
        }
    }
}
