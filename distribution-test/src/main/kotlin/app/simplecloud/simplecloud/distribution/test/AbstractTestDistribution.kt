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

import app.simplecloud.simplecloud.distribution.api.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Date: 08.04.22
 * Time: 17:42
 * @author Frederick Baier
 *
 */
abstract class AbstractTestDistribution : Distribution {

    private val userContext = ConcurrentHashMap<String, Any>()

    abstract val messageManager: TestMessageManager

    override fun getServers(): List<ServerComponent> {
        return getVirtualCluster().getServerComponents()
    }

    override fun getMessageManager(): MessageManager {
        return this.messageManager
    }

    override fun <K, V> getOrCreateCache(name: String): Cache<K, V> {
        return getVirtualCluster().getOrCreateCache(name)
    }

    open fun onComponentJoin(component: DistributionComponent) {

    }

    open fun onComponentLeave(component: DistributionComponent) {

    }

    override fun getScheduler(name: String): ScheduledExecutorService {
        return getVirtualCluster().getScheduler(name)
    }

    override fun getUserContext(): ConcurrentMap<String, Any> {
        return this.userContext
    }

    override fun shutdown() {
        getVirtualCluster().removeComponent(this)
    }

    abstract fun getVirtualCluster(): VirtualCluster

}