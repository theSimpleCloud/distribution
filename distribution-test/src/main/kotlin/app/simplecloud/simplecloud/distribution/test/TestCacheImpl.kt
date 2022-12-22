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

package app.simplecloud.simplecloud.distribution.test

import app.simplecloud.simplecloud.distribution.api.Cache
import app.simplecloud.simplecloud.distribution.api.DistributionEntryProcessor
import app.simplecloud.simplecloud.distribution.api.EntryListener
import app.simplecloud.simplecloud.distribution.api.Predicate
import com.google.common.collect.Maps
import java.util.concurrent.CompletionStage
import java.util.concurrent.CopyOnWriteArrayList

class TestCacheImpl<K, V>(
    private val name: String
) : Cache<K, V> {

    private val map = Maps.newConcurrentMap<K, V>()

    private val entryListeners = CopyOnWriteArrayList<EntryListener<K, V>>()

    override fun getName(): String {
        return this.name
    }

    override fun first(): Map.Entry<K, V> {
        return this.map.entries.first()
    }

    override fun <R> executeOnEntries(
        entryProcessor: DistributionEntryProcessor<K, V, R>,
        predicate: Predicate<K, V>
    ): Map<K, R> {
        TODO("Not yet implemented")
    }

    override fun <R> executeOnEntries(entryProcessor: DistributionEntryProcessor<K, V, R>): Map<K, R> {
        TODO("Not yet implemented")
    }

    override fun <R> submitToKey(key: K, entryProcessor: DistributionEntryProcessor<K, V, R>): CompletionStage<R> {
        TODO("Not yet implemented")
    }

    override fun <R> submitToKeys(
        keys: Set<K>,
        entryProcessor: DistributionEntryProcessor<K, V, R>
    ): CompletionStage<Map<K, R>?> {
        TODO("Not yet implemented")
    }

    override fun <R> executeOnKeys(keys: Set<K>, entryProcessor: DistributionEntryProcessor<K, V, R>): Map<K, R> {
        TODO("Not yet implemented")
    }

    override fun <R> executeOnKey(key: K, entryProcessor: DistributionEntryProcessor<K, V, R>): R {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = this.map.size

    override fun containsKey(key: K): Boolean {
        return this.map.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        return this.map.containsValue(value)
    }

    override fun get(key: K): V? {
        return this.map.get(key)
    }

    override fun isEmpty(): Boolean {
        return this.map.isEmpty()
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = this.map.entries
    override val keys: MutableSet<K>
        get() = this.map.keys
    override val values: MutableCollection<V>
        get() = this.map.values

    override fun clear() {
        this.map.clear()
    }

    override fun set(key: K, value: V) {
        if (!this.map.containsKey(key)) {
            this.entryListeners.forEach { it.entryAdded(key to value) }
        } else {
            this.entryListeners.forEach { it.entryUpdated(key to value) }
        }
        this.map.set(key, value)
    }

    override fun put(key: K, value: V): V? {
        val result = this.map.put(key, value)
        if (result == null) {
            this.entryListeners.forEach { it.entryAdded(key to value) }
        } else {
            this.entryListeners.forEach { it.entryUpdated(key to value) }
        }
        return result
    }

    override fun putAll(from: Map<out K, V>) {
        this.map.putAll(from)
    }

    override fun remove(key: K): V? {
        val result = this.map.remove(key)
        if (result != null)
            this.entryListeners.forEach { it.entryRemoved(key to result) }
        return result
    }

    override fun addEntryListener(entryListener: EntryListener<K, V>) {
        this.entryListeners.add(entryListener)
    }

    override fun distributedQuery(predicate: Predicate<K, V>): Collection<V> {
        return this.entries.filter { predicate.apply(it.key, it.value) }.map { it.value }
    }

}
