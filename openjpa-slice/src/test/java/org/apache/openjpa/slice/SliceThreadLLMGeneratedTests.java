/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

// LLM-generated scenario tests
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
