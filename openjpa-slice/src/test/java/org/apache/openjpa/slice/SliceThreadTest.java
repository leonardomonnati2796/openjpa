package org.apache.openjpa.slice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class SliceThreadTest {

    @Test
    public void reusesSameExecutorInstance() throws Exception {
        resetPool();
        ExecutorService first = SliceThread.getPool();
        ExecutorService second = SliceThread.getPool();
        assertSame(first, second);
    }

    @Test
    public void factoryUsesParentNameAndPropagatesParent() throws Exception {
        resetPool();
        String originalName = Thread.currentThread().getName();
        Thread.currentThread().setName("SliceTestParent");
        try {
            ExecutorService pool = SliceThread.getPool();
            Future<ThreadInfo> result = pool.submit(() -> {
                Thread current = Thread.currentThread();
                String parentName = current instanceof SliceThread
                        ? ((SliceThread) current).getParent().getName()
                        : null;
                return new ThreadInfo(current.getClass(), current.getName(), parentName);
            });
            ThreadInfo info = result.get(5, TimeUnit.SECONDS);

            assertEquals(SliceThread.class, info.threadClass);
            assertEquals("SliceTestParent", info.parentName);
            assertTrue(info.threadName.startsWith("SliceTestParent-slice-"));
        } finally {
            Thread.currentThread().setName(originalName);
        }
    }

    @Test
    public void executesSubmittedRunnable() throws Exception {
        resetPool();
        ExecutorService pool = SliceThread.getPool();
        CountDownLatch latch = new CountDownLatch(1);
        pool.submit(latch::countDown);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    private void resetPool() throws Exception {
        Field field = SliceThread.class.getDeclaredField("_pool");
        field.setAccessible(true);
        ExecutorService existing = (ExecutorService) field.get(null);
        if (existing != null) {
            existing.shutdownNow();
        }
        field.set(null, null);
    }

    private static final class ThreadInfo {
        final Class<?> threadClass;
        final String threadName;
        final String parentName;

        ThreadInfo(Class<?> threadClass, String threadName, String parentName) {
            this.threadClass = threadClass;
            this.threadName = threadName;
            this.parentName = parentName;
        }
    }
}
