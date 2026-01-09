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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.junit.After;
import org.junit.Test;

/**
 * Hand-written tests for SliceThread: control-flow and mock-based verification.
 * These tests target pool creation, naming conventions, and executor delegation.
 */
public class SliceThreadManualTests {

    @After
    public void resetPool() throws Exception {
        Field poolField = SliceThread.class.getDeclaredField("_pool");
        poolField.setAccessible(true);
        poolField.set(null, null);
    }

    // ========== Control-flow tests ==========

    @Test
    public void getPoolIsLazyAndSingleton() {
        ExecutorService first = SliceThread.getPool();
        ExecutorService second = SliceThread.getPool();
        assertSame(first, second);
    }

    @Test
    public void threadsCarryParentNameAndDefaultDaemonSetting() throws Exception {
        ExecutorService pool = SliceThread.getPool();
        String parentName = Thread.currentThread().getName();

        class CapturingName implements Runnable {
            volatile String capturedName;
            volatile boolean daemon;
            @Override
            public void run() {
                capturedName = Thread.currentThread().getName();
                daemon = Thread.currentThread().isDaemon();
            }
        }

        CapturingName task = new CapturingName();
        pool.submit(task).get();

        assertTrue(task.capturedName.startsWith(parentName));
        assertTrue(task.capturedName.contains("-slice-"));
        assertFalse(task.daemon);
    }

    @Test
    public void newSliceThreadUsesExplicitName() {
        Thread parent = Thread.currentThread();
        SliceThread t = new SliceThread("custom-slice", parent, () -> {});
        assertEquals("custom-slice", t.getName());
        assertEquals(parent, t.getParent());
    }

    // ========== Mock-based tests ==========

    @Test
    public void runnableDelegatesToMock() {
        Runnable runnable = mock(Runnable.class);
        SliceThread thread = new SliceThread(Thread.currentThread(), runnable);

        thread.run();

        verify(runnable).run();
        assertSame(Thread.currentThread(), thread.getParent());
    }

    @Test
    public void callableExecutesThroughPool() throws Exception {
        Callable<String> callable = () -> "ok";
        FutureTask<String> task = new FutureTask<>(callable);
        SliceThread thread = new SliceThread(Thread.currentThread(), task);

        thread.run();

        assertSame("ok", task.get());
    }
}
