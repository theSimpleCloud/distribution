/*
 * MIT License
 *
 * Copyright (c) 2022 Frederick Baier & Philipp Eistrach
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package app.simplecloud.simplecloud.distribution.hazelcast

import app.simplecloud.simplecloud.distribution.api.Cache
import app.simplecloud.simplecloud.distribution.api.DistributionEntryProcessor
import app.simplecloud.simplecloud.distribution.api.EntryListener
import app.simplecloud.simplecloud.distribution.api.Predicate
import com.google.common.collect.Maps
import com.hazelcast.core.EntryEvent
import com.hazelcast.map.IMap
import java.util.*
import java.util.concurrent.CompletionStage


class HazelCastCache<K, V>(
    private val hazelMap: IMap<K, V>
) : Cache<K, V> {

    private val listenerToId = Maps.newConcurrentMap<EntryListener<K, V>, UUID>()

    override fun getName(): String {
        return this.hazelMap.name
    }

    override fun first(): Map.Entry<K, V> {
        return this.hazelMap.first()
    }

    override fun <R> executeOnEntries(
        entryProcessor: DistributionEntryProcessor<K, V, R>,
        predicate: Predicate<K, V>
    ): Map<K, R> {
        return this.hazelMap.executeOnEntries(HazelcastEntryProcessor(entryProcessor)) {
            predicate.apply(
                it.key,
                it.value
            )
        }
    }

    override fun <R> executeOnEntries(entryProcessor: DistributionEntryProcessor<K, V, R>): Map<K, R> {
        return this.hazelMap.executeOnEntries(HazelcastEntryProcessor(entryProcessor))
    }

    override fun <R> submitToKey(key: K, entryProcessor: DistributionEntryProcessor<K, V, R>): CompletionStage<R> {
        return this.hazelMap.submitToKey(key, HazelcastEntryProcessor(entryProcessor))
    }

    override fun <R> submitToKeys(
        keys: Set<K>,
        entryProcessor: DistributionEntryProcessor<K, V, R>
    ): CompletionStage<Map<K, R>?> {
        return this.hazelMap.submitToKeys(keys, HazelcastEntryProcessor(entryProcessor))
    }

    override fun <R> executeOnKeys(keys: Set<K>, entryProcessor: DistributionEntryProcessor<K, V, R>): Map<K, R> {
        return this.hazelMap.executeOnKeys(keys, HazelcastEntryProcessor(entryProcessor))
    }

    override fun <R> executeOnKey(key: K, entryProcessor: DistributionEntryProcessor<K, V, R>): R {
        return this.hazelMap.executeOnKey(key, HazelcastEntryProcessor(entryProcessor))
    }

    override val size: Int
        get() = this.hazelMap.size

    override fun containsKey(key: K): Boolean {
        return this.hazelMap.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return this.hazelMap.containsValue(value)
    }

    override fun get(key: K): V? {
        return this.hazelMap.get(key)
    }

    override fun isEmpty(): Boolean {
        return this.hazelMap.isEmpty
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = this.hazelMap.entries
    override val keys: MutableSet<K>
        get() = this.hazelMap.keys
    override val values: MutableCollection<V>
        get() = this.hazelMap.values

    override fun clear() {
        this.hazelMap.clear()
    }

    override fun set(key: K, value: V) {
        try {
            this.hazelMap.lock(key)
            this.hazelMap.set(key, value)
        } finally {
            this.hazelMap.unlock(key)
        }
    }

    override fun put(key: K, value: V): V? {
        try {
            this.hazelMap.lock(key)
            return this.hazelMap.put(key, value)
        } finally {
            this.hazelMap.unlock(key)
        }
    }

    override fun putAll(from: Map<out K, V>) {
        this.hazelMap.setAll(from)
    }

    override fun remove(key: K): V? {
        try {
            this.hazelMap.lock(key)
            return this.hazelMap.remove(key)
        } finally {
            this.hazelMap.unlock(key)
        }
    }

    override fun addEntryListener(entryListener: EntryListener<K, V>) {
        val mapListener = object : HazelCastEntryListener<K, V> {
            override fun entryAdded(event: EntryEvent<K, V>) {
                entryListener.entryAdded(event.key to event.value)
            }

            override fun entryUpdated(event: EntryEvent<K, V>) {
                entryListener.entryUpdated(event.key to event.value)
            }

            override fun entryRemoved(event: EntryEvent<K, V>) {
                entryListener.entryRemoved(event.key to event.oldValue)
            }

        }
        val listenerId = this.hazelMap.addEntryListener(mapListener, true)
        this.listenerToId[entryListener] = listenerId
    }

    override fun distributedQuery(predicate: Predicate<K, V>): Collection<V> {
        return this.hazelMap.values { predicate.apply(it.key, it.value) }
    }

}
