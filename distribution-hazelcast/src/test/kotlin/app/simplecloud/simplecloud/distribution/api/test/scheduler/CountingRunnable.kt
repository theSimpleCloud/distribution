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

package app.simplecloud.simplecloud.distribution.api.test.scheduler

import app.simplecloud.simplecloud.distribution.api.Cache
import app.simplecloud.simplecloud.distribution.api.Distribution
import app.simplecloud.simplecloud.distribution.api.DistributionAware

/**
 * Date: 06.08.22
 * Time: 09:14
 * @author Frederick Baier
 *
 */
class CountingRunnable() : Runnable, java.io.Serializable, DistributionAware {

    @Transient
    private var distribution: Distribution? = null

    @Transient
    private var cache: Cache<Int, Int>? = null

    override fun run() {
        val cache = cache!!
        if (cache.isEmpty()) {
            cache[0] = 1
        }
        cache[0] = cache[0]!! + 1
        println("Scheduler tick " + distribution?.getSelfComponent()?.getDistributionId() + " " + cache[0])
    }

    override fun setDistribution(distribution: Distribution) {
        this.distribution = distribution
        val cache = distribution.getOrCreateCache<Int, Int>("test")
        this.cache = cache
    }

    fun getCount(): Int {
        try {
            return this.cache!![0] ?: 0
        } catch (e: Exception) {
            return 0
        }
    }

}