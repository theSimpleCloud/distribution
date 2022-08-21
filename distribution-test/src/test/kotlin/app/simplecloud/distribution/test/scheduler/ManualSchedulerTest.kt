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

package app.simplecloud.distribution.test.scheduler

import app.simplecloud.simplecloud.distribution.test.scheduler.TestManualScheduledExecutorService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit


/**
 * Date: 05.08.22
 * Time: 09:48
 * @author Frederick Baier
 *
 */
class ManualSchedulerTest {

    private var scheduler: TestManualScheduledExecutorService = TestManualScheduledExecutorService()


    @BeforeEach
    fun setUp() {
        this.scheduler = TestManualScheduledExecutorService()
    }

    @Test
    fun simpleTaskCallAfter0InitialDelay() {
        val runnable = CalledRunnable()
        scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertTrue(runnable.wasCalled)
    }

    @Test
    fun simpleTaskCallAfter1SecondInitialDelay() {
        val runnable = CalledRunnable()
        scheduler.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertFalse(runnable.wasCalled)
        scheduler.skip(1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertTrue(runnable.wasCalled)
    }

    @Test
    fun schedule2TasksWith0InitialDelay_bothWillBeExecutedOnNextTick() {
        val runnable1 = CalledRunnable()
        val runnable2 = CalledRunnable()
        scheduler.scheduleAtFixedRate(runnable1, 0, 1, TimeUnit.SECONDS)
        scheduler.scheduleAtFixedRate(runnable2, 0, 1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertTrue(runnable1.wasCalled)
        Assertions.assertTrue(runnable2.wasCalled)
    }

    @Test
    fun schedule2TasksWith1InitialDelay_bothWillBeExecutedAfterOneSecond() {
        val runnable1 = CalledRunnable()
        val runnable2 = CalledRunnable()
        scheduler.scheduleAtFixedRate(runnable1, 1, 1, TimeUnit.SECONDS)
        scheduler.scheduleAtFixedRate(runnable2, 1, 1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertFalse(runnable1.wasCalled)
        Assertions.assertFalse(runnable2.wasCalled)
        scheduler.skip(1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertTrue(runnable1.wasCalled)
        Assertions.assertTrue(runnable2.wasCalled)
    }

    @Test
    fun taskWith0InitialDelay_willNotBeExecutedTwice() {
        val runnable = CountingRunnable()
        scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
        scheduler.executeTick()
        scheduler.executeTick()
        Assertions.assertEquals(1, runnable.count)

    }

    @Test
    fun taskWith1Period_willBeExecutedAgainAfterOneSecond() {
        val runnable = CountingRunnable()
        scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertEquals(1, runnable.count)
        scheduler.skip(1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertEquals(2, runnable.count)
    }

    @Test
    fun taskWillNotBeCalledAfterUnregister() {
        val runnable = CountingRunnable()
        val scheduledTask = scheduler.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS)
        scheduler.executeTick()
        scheduler.cancelTask(scheduledTask)
        scheduler.skip(1, TimeUnit.SECONDS)
        scheduler.executeTick()
        Assertions.assertEquals(1, runnable.count)
    }

    @Test
    fun afterTaskScheduled_TaskWillBeRegistered() {
        this.scheduler.scheduleAtFixedRate(CountingRunnable(), 1, 1, TimeUnit.SECONDS)
        Assertions.assertEquals(1, this.scheduler.getScheduledTasks().join().size)
    }

    @Test
    fun afterTaskScheduled_TaskWillBeRegistered_2() {
        this.scheduler.scheduleAtFixedRate(CountingRunnable(), 1, 1, TimeUnit.SECONDS)
        this.scheduler.scheduleAtFixedRate(CountingRunnable(), 1, 1, TimeUnit.SECONDS)
        Assertions.assertEquals(2, this.scheduler.getScheduledTasks().join().size)
    }

    @Test
    fun registerTwoTasks_unregisterOne_OneTaskWillBeRegistered() {
        this.scheduler.scheduleAtFixedRate(CountingRunnable(), 1, 1, TimeUnit.SECONDS)
        val task = this.scheduler.scheduleAtFixedRate(CountingRunnable(), 1, 1, TimeUnit.SECONDS)
        this.scheduler.cancelTask(task)
        Assertions.assertEquals(1, this.scheduler.getScheduledTasks().join().size)
    }

}