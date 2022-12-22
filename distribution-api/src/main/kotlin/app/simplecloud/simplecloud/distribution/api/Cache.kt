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

package app.simplecloud.simplecloud.distribution.api

import java.util.concurrent.CompletionStage

/**
 * Date: 03.04.22
 * Time: 15:43
 * @author Frederick Baier
 *
 */
interface Cache<K, V> : MutableMap<K, V> {

    fun getName(): String

    fun first(): Map.Entry<K, V>

    fun set(key: K, value: V)

    fun <R> executeOnKey(
        key: K,
        entryProcessor: DistributionEntryProcessor<K, V, R>
    ): R

    fun <R> executeOnKeys(
        keys: Set<K>,
        entryProcessor: DistributionEntryProcessor<K, V, R>
    ): Map<K, R>

    fun <R> submitToKeys(
        keys: Set<K>,
        entryProcessor: DistributionEntryProcessor<K, V, R>
    ): CompletionStage<Map<K, R>?>

    fun <R> submitToKey(
        key: K,
        entryProcessor: DistributionEntryProcessor<K, V, R>
    ): CompletionStage<R>

    fun <R> executeOnEntries(entryProcessor: DistributionEntryProcessor<K, V, R>): Map<K, R>

    fun <R> executeOnEntries(
        entryProcessor: DistributionEntryProcessor<K, V, R>,
        predicate: Predicate<K, V>
    ): Map<K, R>


    fun addEntryListener(entryListener: EntryListener<K, V>)

    fun distributedQuery(predicate: Predicate<K, V>): Collection<V>

}