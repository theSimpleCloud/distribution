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
import app.simplecloud.simplecloud.distribution.test.scheduler.TestThreadScheduledExecutorService
import com.google.common.collect.Maps
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Date: 08.04.22
 * Time: 18:19
 * @author Frederick Baier
 *
 */
class VirtualCluster(
    initialServer: TestServerDistributionImpl,
) {

    private val servers = CopyOnWriteArrayList<TestServerDistributionImpl>()
    private val clients = CopyOnWriteArrayList<TestClientDistributionImpl>()

    private val cacheNameToCache = Maps.newConcurrentMap<String, Cache<*, *>>()

    private val nameToScheduler = Maps.newConcurrentMap<String, ScheduledExecutorService>()

    init {
        this.servers.add(initialServer)
    }

    fun getServerComponents(): List<ServerComponent> {
        return this.servers.map { it.getSelfComponent() }
    }

    fun getClientComponents(): List<ClientComponent> {
        return this.clients.map { it.getSelfComponent() }
    }

    fun addServer(server: TestServerDistributionImpl) {
        this.servers.add(server)
        onServerJoin(server)
    }

    fun addClient(client: TestClientDistributionImpl) {
        this.clients.add(client)
        this.servers.forEach {
            client.onComponentJoin(it.getSelfComponent())
            it.onComponentJoin(client.getSelfComponent())
        }
    }

    fun removeComponent(component: AbstractTestDistribution) {
        if (component is TestClientDistributionImpl) {
            removeClient(component)
        } else {
            removeServer(component as TestServerDistributionImpl)
        }
    }

    private fun removeServer(component: TestServerDistributionImpl) {
        getAllDistributions().forEach {
            component.onComponentLeave(it.getSelfComponent())
            it.onComponentLeave(component.getSelfComponent())
        }
        this.servers.remove(component)
        if (this.servers.isEmpty()) {
            shutdownCluster()
        } else {
            updateSchedulerDistributions()
        }
    }

    private fun updateSchedulerDistributions() {
        for (scheduler in this.nameToScheduler.values) {
            if (scheduler !is TestThreadScheduledExecutorService) {
                return
            }
            scheduler.setDistribution(this.servers.first())
        }
    }

    private fun shutdownCluster() {
        this.clients.forEach { it.shutdown() }
        this.nameToScheduler.values.forEach { it.shutdown() }
    }

    private fun removeClient(component: TestClientDistributionImpl) {
        this.servers.forEach {
            component.onComponentLeave(it.getSelfComponent())
            it.onComponentLeave(component.getSelfComponent())
        }
        this.clients.remove(component)
    }

    private fun onServerJoin(joiningDistribution: AbstractTestDistribution) {
        getAllDistributions().forEach { it.onComponentJoin(joiningDistribution.getSelfComponent()) }
    }

    fun getServerPorts(): List<Int> {
        return this.servers.map { it.port }
    }

    fun sendMessage(sender: DistributionComponent, message: Any) {
        this.getAllDistributions().forEach { it.messageManager.onReceive(message, sender) }
    }

    private fun getAllDistributions(): Set<AbstractTestDistribution> {
        return this.servers.union(this.clients)
    }

    fun sendMessage(sender: DistributionComponent, message: Any, receiver: DistributionComponent) {
        val receiverDistribution = getAllDistributions().firstOrNull { it.getSelfComponent() == receiver }
        receiverDistribution?.messageManager?.onReceive(message, sender)
    }

    fun <K, V> getOrCreateCache(name: String): Cache<K, V> {
        if (this.cacheNameToCache.containsKey(name)) {
            return this.cacheNameToCache[name] as Cache<K, V>
        }
        val cache = TestCacheImpl<K, V>(name)
        this.cacheNameToCache[name] = cache
        return cache
    }

    fun getServerByPort(port: Int): TestServerDistributionImpl? {
        return this.servers.firstOrNull { it.port == port }
    }

    fun getScheduler(name: String): ScheduledExecutorService {
        if (this.nameToScheduler.containsKey(name)) {
            return this.nameToScheduler[name]!!
        }
        val scheduler = TestThreadScheduledExecutorService(this.servers.first())
        this.nameToScheduler[name] = scheduler
        return scheduler
    }


}