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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Test;

/**
 * Hand-written tests for LRUMap: control-flow and mock-based verification.
 * These tests target specific code paths and verify hook behavior.
 */
public class LRUMapManualTests {

    // ========== Control-flow tests ==========

    @Test
    public void evictsLeastRecentlyUsedEntryOnOverflow() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3); // should evict "a"

        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
        assertEquals(2, map.size());
    }

    @Test
    public void getPromotesEntryBeforeEviction() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        map.get("a"); // promotes a
        map.put("c", 3); // evicts b

        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    @Test
    public void scanUntilRemovableSkipsNonRemovableEntries() {
        RecordingLRUMap<String, Integer> map = new RecordingLRUMap<>(2, true);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3); // first candidate rejected, second accepted

        assertEquals(Arrays.asList("a", "b"), map.seenKeys);
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    // ========== Mock-based tests ==========

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

    @Test
    public void multipleEvictionsVerifyOrder() {
        EvictionTrackingMap<String, Integer> map = new EvictionTrackingMap<>(2);
        map.put("first", 1);
        map.put("second", 2);
        map.put("third", 3);  // evicts first

        assertEquals(1, map.getEvictionCount());
        assertFalse(map.containsKey("first"));

        map.put("fourth", 4);  // evicts second

        assertEquals(2, map.getEvictionCount());
        assertFalse(map.containsKey("second"));
    }

    @Test
    public void removeViaContainsKeyDoesNotAffectEviction() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        boolean exists = map.containsKey("a");

        assertTrue(exists);
        map.put("c", 3); // should still evict a, not b

        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
    }

    // ========== Inner helper classes ==========

    private static final class RecordingLRUMap<K, V> extends LRUMap<K, V> {
        final java.util.List<K> seenKeys = new java.util.ArrayList<>();

        RecordingLRUMap(int maxSize, boolean scanUntilRemovable) {
            super(maxSize, maxSize, DEFAULT_LOAD_FACTOR, scanUntilRemovable);
        }

        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry<K, V> entry) {
            seenKeys.add(entry.getKey());
            return seenKeys.size() > 1; // evict second candidate
        }
    }

    private static final class EvictionTrackingMap<K, V> extends LRUMap<K, V> {
        private int evictionCount = 0;

        EvictionTrackingMap(int maxSize) {
            super(maxSize);
        }

        @Override
        protected boolean removeLRU(AbstractLinkedMap.LinkEntry<K, V> entry) {
            evictionCount++;
            return true;
        }

        int getEvictionCount() {
            return evictionCount;
        }
    }
}
