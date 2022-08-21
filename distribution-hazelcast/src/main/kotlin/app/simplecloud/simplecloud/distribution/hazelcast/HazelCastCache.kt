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
import app.simplecloud.simplecloud.distribution.api.EntryListener
import app.simplecloud.simplecloud.distribution.api.Predicate
import com.google.common.collect.Maps
import com.hazelcast.core.EntryEvent
import com.hazelcast.map.IMap
import java.util.*


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

    override fun put(key: K, value: V): V? {
        return this.hazelMap.put(key, value)
    }

    override fun putAll(from: Map<out K, V>) {
        this.hazelMap.putAll(from)
    }

    override fun remove(key: K): V? {
        return this.hazelMap.remove(key)
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
