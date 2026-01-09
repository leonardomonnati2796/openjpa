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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.junit.Test;

/**
 * Mock/stub-focused tests for SliceThread interactions.
 */
public class SliceThreadMockTest {

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
