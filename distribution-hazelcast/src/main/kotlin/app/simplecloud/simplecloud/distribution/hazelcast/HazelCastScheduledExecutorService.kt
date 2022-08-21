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

import app.simplecloud.simplecloud.distribution.api.Distribution
import app.simplecloud.simplecloud.distribution.api.DistributionAware
import app.simplecloud.simplecloud.distribution.api.ScheduledExecutorService
import app.simplecloud.simplecloud.distribution.api.ScheduledTask
import com.hazelcast.core.HazelcastInstance
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Date: 10.08.22
 * Time: 09:06
 * @author Frederick Baier
 *
 */
class HazelCastScheduledExecutorService(
    private val name: String,
    private val hazelCast: HazelcastInstance,
    private var distribution: Distribution,
) : ScheduledExecutorService {

    private val hazelCastScheduler = this.hazelCast.getScheduledExecutorService(name)

    override fun scheduleAtFixedRate(
        runnable: Runnable,
        initialDelay: Int,
        period: Int,
        timeUnit: TimeUnit,
    ): ScheduledTask {
        if (runnable is DistributionAware) {
            runnable.setDistribution(this.distribution)
        }
        val scheduledFuture =
            hazelCastScheduler.scheduleAtFixedRate<Unit>(runnable, initialDelay.toLong(), period.toLong(), timeUnit)
        return HazelCastScheduledTask(scheduledFuture)
    }

    override fun cancelTask(scheduledTask: ScheduledTask) {
        if (scheduledTask is HazelCastScheduledTask) {
            val future = scheduledTask.future
            future.cancel(false)
            future.dispose()
        }
    }

    override fun getScheduledTasks(): CompletableFuture<List<ScheduledTask>> {
        return CompletableFuture.supplyAsync {
            val allScheduledFutures = this.hazelCastScheduler.getAllScheduledFutures<Unit>()
            return@supplyAsync allScheduledFutures.values.flatten().map { HazelCastScheduledTask(it) }
        }.orTimeout(500, TimeUnit.MILLISECONDS) as CompletableFuture<List<ScheduledTask>>
    }

    override fun shutdown() {
        this.hazelCastScheduler.shutdown()
    }

}