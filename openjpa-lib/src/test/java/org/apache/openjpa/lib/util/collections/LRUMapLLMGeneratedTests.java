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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.openjpa.lib.util.collections.AbstractLinkedMap.LinkEntry;
import org.junit.Test;

/**
 * LLM-generated scenario tests for LRUMap.
 */
public class LRUMapLLMGeneratedTests {

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMaxSizeBelowOne() {
        new LRUMap<>(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInitialSizeGreaterThanMax() {
        new LRUMap<>(1, 2);
    }

    @Test
    public void evictsLeastRecentlyUsedEntryOnOverflow() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3);

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

        map.get("a");
        map.put("c", 3);

        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    @Test
    public void scanUntilRemovableSkipsNonRemovableEntries() {
        RecordingLRUMap<String, Integer> map = new RecordingLRUMap<>(2, true);
        map.put("a", 1);
        map.put("b", 2);

        map.put("c", 3);

        assertEquals(Arrays.asList("a", "b"), map.getSeenKeys());
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
        assertEquals(2, map.size());
    }

    @Test
    public void multipleEvictionsInSequence() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3); // evicts a
        map.put("d", 4); // evicts b

        assertFalse(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
        assertTrue(map.containsKey("d"));
        assertEquals(2, map.size());
    }

    @Test
    public void putWithExistingKeyUpdatesValueWithoutEviction() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        Integer oldValue = map.put("a", 10);

        assertEquals(Integer.valueOf(1), oldValue);
        assertEquals(Integer.valueOf(10), map.get("a"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals(2, map.size());
    }

    @Test
    public void getMultipleTimesKeepsEntryAsRecent() {
        LRUMap<String, Integer> map = new LRUMap<>(2);
        map.put("a", 1);
        map.put("b", 2);

        map.get("a");
        map.get("a");
        map.get("a");
        map.put("c", 3); // should still evict b, not a

        assertTrue(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    @Test
    public void sizeOfOneMap() {
        LRUMap<String, Integer> map = new LRUMap<>(1);
        map.put("a", 1);
        assertEquals(1, map.size());

        map.put("b", 2);

        assertFalse(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertEquals(1, map.size());
    }

    @Test
    public void largerMapCapacity() {
        LRUMap<String, Integer> map = new LRUMap<>(5);
        for (int i = 0; i < 5; i++) {
            map.put("key" + i, i);
        }
        assertEquals(5, map.size());

        map.put("key5", 5); // should evict key0

        assertFalse(map.containsKey("key0"));
        for (int i = 1; i <= 5; i++) {
            assertTrue(map.containsKey("key" + i));
        }
        assertEquals(5, map.size());
    }

    private static final class RecordingLRUMap<K, V> extends LRUMap<K, V> {
        private final List<K> seenKeys = new ArrayList<>();

        RecordingLRUMap(int maxSize, boolean scanUntilRemovable) {
            super(maxSize, maxSize, DEFAULT_LOAD_FACTOR, scanUntilRemovable);
        }

        @Override
        protected boolean removeLRU(LinkEntry<K, V> entry) {
            seenKeys.add(entry.getKey());
            return seenKeys.size() > 1;
        }

        List<K> getSeenKeys() {
            return seenKeys;
        }
    }
}
