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

import app.simplecloud.simplecloud.distribution.api.ScheduledExecutorService
import app.simplecloud.simplecloud.distribution.api.ScheduledTask
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

open class TestManualScheduledExecutorService(
    private val time: Time = Time(),
) : ScheduledExecutorService {

    private val tasks = CopyOnWriteArrayList<InternalScheduledTask>()

    override fun scheduleAtFixedRate(
        runnable: Runnable,
        initialDelay: Int,
        period: Int,
        timeUnit: TimeUnit,
    ): InternalScheduledTask {
        val testScheduledTask =
            FixedRateRepeatingTask(runnable, initialDelay, period, timeUnit, this.time.currentTime())
        this.tasks.add(testScheduledTask)
        return testScheduledTask
    }

    override fun cancelTask(scheduledTask: ScheduledTask) {
        this.tasks.remove(scheduledTask)
    }

    override fun shutdown() {

    }

    fun executeTick() {
        for (scheduledTask in this.tasks) {
            handleNextTask(scheduledTask)
        }
    }

    private fun handleNextTask(task: InternalScheduledTask) {
        if (task.getNextExecutionTimeStamp() <= this.time.currentTime()) {
            task.recalculateNextExecutionTimeStamp(this.time.currentTime())
            task.executeTask()
        }
    }

    fun skip(amount: Int, timeUnit: TimeUnit) {
        this.time.skip(amount, timeUnit)
    }

    override fun getScheduledTasks(): CompletableFuture<List<ScheduledTask>> {
        return CompletableFuture.completedFuture(this.tasks)
    }

}
