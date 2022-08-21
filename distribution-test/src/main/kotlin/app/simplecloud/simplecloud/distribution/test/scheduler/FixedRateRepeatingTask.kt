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

package app.simplecloud.simplecloud.distribution.test.scheduler

import app.simplecloud.simplecloud.distribution.api.Distribution
import app.simplecloud.simplecloud.distribution.api.DistributionAware
import java.util.concurrent.TimeUnit

/**
 * Date: 05.08.22
 * Time: 09:58
 * @author Frederick Baier
 *
 */
class FixedRateRepeatingTask(
    val runnable: Runnable,
    initialDelay: Int,
    val period: Int,
    val timeUnit: TimeUnit,
    initialCurrentTime: Long,
) : InternalScheduledTask {

    @Volatile
    private var nextExecutionTimeStamp = initialCurrentTime + this.timeUnit.toMillis(initialDelay.toLong())

    override fun executeTask() {
        runnable.run()
    }

    override fun recalculateNextExecutionTimeStamp(currentTime: Long) {
        this.nextExecutionTimeStamp = currentTime + this.timeUnit.toMillis(this.period.toLong())
    }

    override fun getNextExecutionTimeStamp(): Long {
        return this.nextExecutionTimeStamp
    }

    override fun updateDistribution(distribution: Distribution) {
        if (runnable is DistributionAware) {
            runnable.setDistribution(distribution)
        }
    }

}