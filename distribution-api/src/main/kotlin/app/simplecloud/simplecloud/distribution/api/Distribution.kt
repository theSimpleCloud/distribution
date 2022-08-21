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

import java.util.concurrent.ConcurrentMap

/**
 * Date: 03.04.22
 * Time: 15:25
 * @author Frederick Baier
 *
 */
interface Distribution {

    /**
     * Returns the self component
     * [ClientComponent] if this is a client
     * [ServerComponent] if this is a server
     */
    fun getSelfComponent(): DistributionComponent

    /**
     * Returns all servers currently connected to the cluster
     */
    fun getServers(): List<ServerComponent>

    /**
     * Returns the clients currently connected to this server
     * If this is not a server the list will be empty
     */
    fun getConnectedClients(): List<ClientComponent>

    /**
     * Creates a distributed key value store
     */
    fun <K, V> getOrCreateCache(name: String): Cache<K, V>

    fun getMessageManager(): MessageManager

    fun getScheduler(name: String): ScheduledExecutorService

    fun getUserContext(): ConcurrentMap<String, Any>

    fun shutdown()

}