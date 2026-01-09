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
package org.apache.openjpa.lib.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Test;

/**
 * Mock/stub-focused tests for LRUMap eviction hook.
 */
public class LRUMapMockTest {

    @Test
    public void usesRemoveLruHookWhenFull() {
        LRUMap<String, Integer> map = spy(new LRUMap<>(1));
        doReturn(true).when(map).removeLRU(any(AbstractLinkedMap.LinkEntry.class));

        map.put("a", 1);
        map.put("b", 2); // should delegate to removeLRU

        verify(map, atLeastOnce()).removeLRU(any(AbstractLinkedMap.LinkEntry.class));
        assertFalse(map.containsKey("a"));
        assertEquals(Integer.valueOf(2), map.get("b"));
    }
}
