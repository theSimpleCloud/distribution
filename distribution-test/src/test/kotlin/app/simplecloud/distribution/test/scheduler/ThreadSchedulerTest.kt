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

import app.simplecloud.simplecloud.distribution.test.TestDistributionFactoryImpl
import app.simplecloud.simplecloud.distribution.test.VirtualNetwork
import app.simplecloud.simplecloud.distribution.test.scheduler.TestThreadScheduledExecutorService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

/**
 * Date: 06.08.22
 * Time: 09:12
 * @author Frederick Baier
 *
 */
class ThreadSchedulerTest {

    private val distributionFactory = TestDistributionFactoryImpl()
    private var scheduler: TestThreadScheduledExecutorService =
        TestThreadScheduledExecutorService(this.distributionFactory.createServer(1630, emptyList()))


    @BeforeEach
    fun setUp() {
        val distribution = this.distributionFactory.createServer(1631, emptyList())
        this.scheduler = TestThreadScheduledExecutorService(distribution)
    }

    @AfterEach
    fun tearDown() {
        VirtualNetwork.reset()
    }

    @Test
    fun willExecuteAfterOneSecondInitialDelay() {
        val countingRunnable = CountingRunnable()
        val scheduledTask = scheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        Assertions.assertEquals(0, countingRunnable.count)
        Thread.sleep(1_010)
        Assertions.assertEquals(1, countingRunnable.count)
    }

    @Test
    fun willExecuteAfterOneSecondInitialDelayAndAfterOneSecondPeriod() {
        val countingRunnable = CountingRunnable()
        val scheduledTask = scheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        Assertions.assertEquals(0, countingRunnable.count)
        Thread.sleep(2_200)
        Assertions.assertEquals(2, countingRunnable.count)
    }

    @Test
    fun willNotExecuteAfterUnregister() {
        val countingRunnable = CountingRunnable()
        val scheduledTask = scheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        Thread.sleep(1_010)
        scheduler.cancelTask(scheduledTask)
        Thread.sleep(1_010)
        Assertions.assertEquals(1, countingRunnable.count)
    }

    @Test
    fun schedulerWith5SecondPeriod_willNotBlockOthers() {
        val countingRunnable = CountingRunnable()
        val countingRunnable2 = CountingRunnable()
        scheduler.scheduleAtFixedRate(countingRunnable, 1, 5, TimeUnit.SECONDS)
        Thread.sleep(1_100)
        scheduler.scheduleAtFixedRate(countingRunnable2, 1, 1, TimeUnit.SECONDS)
        Thread.sleep(2_200)
        Assertions.assertEquals(2, countingRunnable2.count)
    }

    @Test
    fun afterShutdown_thereWillBeNoFurtherExecutes() {
        val countingRunnable = CountingRunnable()
        scheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        scheduler.shutdown()
        Thread.sleep(1_010)
        Assertions.assertEquals(0, countingRunnable.count)
    }

}