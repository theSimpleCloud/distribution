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

import java.net.BindException
import java.net.ConnectException
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Date: 08.04.22
 * Time: 17:37
 * @author Frederick Baier
 *
 */
object VirtualNetwork {


    private val virtualClusters = CopyOnWriteArrayList<VirtualCluster>()

    fun registerServer(server: TestServerDistributionImpl, otherServerPorts: List<Int>): VirtualCluster {
        checkForPortInUse(server)
        val existingCluster = otherServerPorts.map { findVirtualClusterByPort(it) }.firstOrNull()
            ?: return createNewCluster(server)
        existingCluster.addServer(server)
        return existingCluster
    }

    private fun createNewCluster(server: TestServerDistributionImpl): VirtualCluster {
        val newCluster = VirtualCluster(server)
        virtualClusters.add(newCluster)
        return newCluster
    }

    private fun checkForPortInUse(server: TestServerDistributionImpl) {
        val clusterByServerPort = findVirtualClusterByPort(server.port)
        if (clusterByServerPort != null)
            throw BindException("Address already in use!")
    }

    fun connectClient(client: TestClientDistributionImpl, port: Int): VirtualCluster {
        val cluster = findVirtualClusterByPort(port) ?: throw ConnectException("No Server is running at port $port")
        cluster.addClient(client)
        return cluster
    }

    private fun findVirtualClusterByPort(port: Int): VirtualCluster? {
        return virtualClusters.firstOrNull { it.getServerPorts().contains(port) }
    }

    fun reset() {
        virtualClusters.clear()
    }

}