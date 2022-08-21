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

import app.simplecloud.simplecloud.distribution.api.Address
import app.simplecloud.simplecloud.distribution.api.Distribution
import app.simplecloud.simplecloud.distribution.api.DistributionAware
import app.simplecloud.simplecloud.distribution.api.DistributionFactory
import app.simplecloud.simplecloud.distribution.test.TestDistributionFactoryImpl
import app.simplecloud.simplecloud.distribution.test.VirtualNetwork
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Date: 03.08.22
 * Time: 21:13
 * @author Frederick Baier
 *
 */
class DistributionScheduleExecutorServiceTest {

    private lateinit var factory: DistributionFactory

    private var server: Distribution? = null
    private var client: Distribution? = null

    @BeforeEach
    fun setUp() {
        this.factory = TestDistributionFactoryImpl()
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
    }

    @AfterEach
    fun tearDown() {
        VirtualNetwork.reset()
    }

    @Test
    fun taskStartedOnServer_willBeVisibleOnClient() {
        val scheduler = this.server!!.getScheduler("test")
        val countingRunnable = CountingRunnable()
        scheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        val clientSideScheduler = this.client!!.getScheduler("test")
        Assertions.assertEquals(1, clientSideScheduler.getScheduledTasks().join().size)
    }

    @Test
    fun taskStartedOnServer_unregisterOnClient_willBeGoneInClientAndServer() {
        val scheduler = this.server!!.getScheduler("test")
        val countingRunnable = CountingRunnable()
        scheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        val clientScheduler = this.client!!.getScheduler("test")
        val task = clientScheduler.getScheduledTasks().join()[0]
        clientScheduler.cancelTask(task)
        Assertions.assertEquals(0, clientScheduler.getScheduledTasks().join().size)
        Assertions.assertEquals(0, scheduler.getScheduledTasks().join().size)
    }

    @Test
    fun taskStartedOnClient_clientShutdown_taskWillContinue() {
        val clientScheduler = this.client!!.getScheduler("test")
        val countingRunnable = CountingRunnable()
        val task = clientScheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        this.client!!.shutdown()
        Thread.sleep(1_010)
        Assertions.assertEquals(1, countingRunnable.count)
    }

    @Test
    fun afterSchedulerShutdown_TasksWillNoLongerBeExecuted() {
        val serverScheduler = this.server!!.getScheduler("test")
        val countingRunnable = CountingRunnable()
        serverScheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        serverScheduler.shutdown()
        Thread.sleep(1_010)
        Assertions.assertEquals(0, countingRunnable.count)
    }

    @Test
    fun serverShutdown_countdownWillStop() {
        val serverScheduler = this.server!!.getScheduler("test")
        val countingRunnable = CountingRunnable()
        serverScheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        this.server!!.shutdown()
        Thread.sleep(1_010)
        Assertions.assertEquals(0, countingRunnable.count)
    }

    @Test
    fun oneServerShutdown_OneStillRunning_SchedulersWillNotShutdown() {
        val server2 = this.factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        val serverScheduler = this.server!!.getScheduler("test")
        val countingRunnable = CountingRunnable()
        val task = serverScheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        this.server!!.shutdown()
        Thread.sleep(1_010)
        Assertions.assertEquals(1, countingRunnable.count)
    }

    @Test
    fun twoServerAndOneClient_BothServerShutdown_SchedulerWillShutdown() {
        val server2 = this.factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        val serverScheduler = this.server!!.getScheduler("test")
        val countingRunnable = CountingRunnable()
        val task = serverScheduler.scheduleAtFixedRate(countingRunnable, 1, 1, TimeUnit.SECONDS)
        this.server!!.shutdown()
        server2.shutdown()
        Thread.sleep(1_010)
        Assertions.assertEquals(0, countingRunnable.count)
    }

    @Test
    fun scheduleDistributionAwareRunnable_distributionWillBeSet() {
        val serverScheduler = this.server!!.getScheduler("test")
        val runnable = DistributionAwareRunnable()
        serverScheduler.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS)
        Assertions.assertNotNull(runnable.distribution)
    }

    @Test
    fun scheduleDistributionAwareRunnable_() {
        val server2 = this.factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        val serverScheduler = this.server!!.getScheduler("test")
        val runnable = DistributionAwareRunnable()
        serverScheduler.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS)
        val prevDistribution = runnable.distribution
        this.server!!.shutdown()
        Assertions.assertNotEquals(prevDistribution, runnable.distribution)
        server2.shutdown()
    }

    class DistributionAwareRunnable() : Runnable, Serializable, DistributionAware {

        @Transient
        var distribution: Distribution? = null
            private set

        override fun run() {

        }

        override fun setDistribution(distribution: Distribution) {
            this.distribution = distribution
        }

    }

}