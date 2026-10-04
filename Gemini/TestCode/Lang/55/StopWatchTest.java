package org.apache.commons.lang.time;

import org.junit.Assert;
import org.junit.Test;
import java.lang.reflect.Field;

public class StopWatchTest {

    @Test
    public void testStopWatchSimple() throws InterruptedException {
        StopWatch watch = new StopWatch();
        Assert.assertEquals(0, watch.getTime());
        Assert.assertEquals("0:00:00.000", watch.toString());

        watch.start();
        Thread.sleep(20);
        long time1 = watch.getTime();
        Assert.assertTrue(time1 >= 10);

        watch.stop();
        long totalTime = watch.getTime();
        Thread.sleep(20);
        Assert.assertEquals(totalTime, watch.getTime());
        Assert.assertNotNull(watch.toString());

        watch.reset();
        Assert.assertEquals(0, watch.getTime());
    }

    @Test
    public void testSplit() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(20);
        watch.split();
        long splitTime1 = watch.getSplitTime();
        String splitStr = watch.toSplitString();
        Assert.assertNotNull(splitStr);
        Thread.sleep(20);
        Assert.assertEquals(splitTime1, watch.getSplitTime());
        Assert.assertTrue(watch.getTime() > splitTime1);

        watch.unsplit();
        Thread.sleep(20);
        watch.stop();
        Assert.assertTrue(watch.getTime() > splitTime1);
    }

    @Test
    public void testSuspendResume() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(20);
        watch.suspend();
        long suspendTime = watch.getTime();
        Thread.sleep(20);
        Assert.assertEquals(suspendTime, watch.getTime());

        watch.resume();
        Thread.sleep(20);
        Assert.assertTrue(watch.getTime() > suspendTime);

        watch.stop();
        long finalTime = watch.getTime();
        Assert.assertTrue(finalTime >= suspendTime);
    }

    @Test
    public void testStopWhenSuspended() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(20);
        watch.suspend();
        long suspendTime = watch.getTime();
        watch.stop();
        Assert.assertEquals(suspendTime, watch.getTime());
    }

    @Test(expected = IllegalStateException.class)
    public void testStartAlreadyStarted() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStartWhenStopped() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStartWhenSuspended() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        watch.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStopWhenUnstarted() {
        StopWatch watch = new StopWatch();
        watch.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testStopWhenStopped() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.stop();
    }

    @Test(expected = IllegalStateException.class)
    public void testSplitWhenUnstarted() {
        StopWatch watch = new StopWatch();
        watch.split();
    }

    @Test(expected = IllegalStateException.class)
    public void testSplitWhenStopped() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.split();
    }

    @Test(expected = IllegalStateException.class)
    public void testSplitWhenSuspended() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        watch.split();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnsplitWhenNotSplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.unsplit();
    }

    @Test(expected = IllegalStateException.class)
    public void testUnsplitWhenUnstarted() {
        StopWatch watch = new StopWatch();
        watch.unsplit();
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspendWhenUnstarted() {
        StopWatch watch = new StopWatch();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspendWhenStopped() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testSuspendWhenSuspended() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class)
    public void testResumeWhenUnstarted() {
        StopWatch watch = new StopWatch();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class)
    public void testResumeWhenRunning() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class)
    public void testResumeWhenStopped() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSplitTimeWhenUnsplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.getSplitTime();
    }

    @Test(expected = IllegalStateException.class)
    public void testToSplitStringWhenUnsplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.toSplitString();
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalRunningStateInGetTime() throws Exception {
        StopWatch watch = new StopWatch();
        Field field = StopWatch.class.getDeclaredField("runningState");
        field.setAccessible(true);
        field.setInt(watch, 999);
        watch.getTime();
    }
}
