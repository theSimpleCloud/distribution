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

package app.simplecloud.distribution.test

import app.simplecloud.simplecloud.distribution.api.Address
import app.simplecloud.simplecloud.distribution.api.DistributionFactory
import app.simplecloud.simplecloud.distribution.test.TestDistributionFactoryImpl
import app.simplecloud.simplecloud.distribution.test.VirtualNetwork
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.ConnectException

/**
 * Date: 07.04.22
 * Time: 13:02
 * @author Frederick Baier
 *
 */
class DistributionTest {

    private lateinit var factory: DistributionFactory

    @BeforeEach
    fun setUp() {
        this.factory = TestDistributionFactoryImpl()
    }

    @AfterEach
    fun tearDown() {
        VirtualNetwork.reset()
    }

    @Test
    fun newDistributionServer_hasOneServerAndZeroClients() {
        val distribution = factory.createServer(1330, emptyList())
        assertEquals(1, distribution.getServers().size)
        assertEquals(0, distribution.getConnectedClients().size)
    }

    @Test
    fun newClientWithoutServer_WillThrowError() {
        assertThrows<ConnectException> {
            factory.createClient(Address("127.0.0.1", 1630))
        }
    }

    @Test
    fun newClientWithWrongAddressAndServer_wilThrowError() {
        factory.createServer(1630, emptyList())
        assertThrows<ConnectException> {
            factory.createClient(Address("127.0.0.1", 1631))
        }
    }

    @Test
    fun newClientWithServer_wilNotThrowError() {
        factory.createServer(1630, emptyList())
        factory.createClient(Address("127.0.0.1", 1630))
    }

    @Test
    fun newClientWithServer_HasOneClientAndOneServer() {
        val factory = TestDistributionFactoryImpl()
        val server = factory.createServer(1630, emptyList())
        val clientDistribution = factory.createClient(Address("127.0.0.1", 1630))
        assertEquals(1, clientDistribution.getServers().size)
        assertEquals(1, server.getServers().size)
        assertEquals(1, server.getConnectedClients().size)
    }

    @Test
    fun newServerWithNotExistingServerToConnectTo_willNotThrow() {
        val factory = TestDistributionFactoryImpl()
        val server = factory.createServer(1630, listOf(Address("127.0.0.1", 1631)))
        assertEquals(1, server.getServers().size)
        assertEquals(0, server.getConnectedClients().size)
    }

    @Test
    fun serverConnectToServer() {
        val factory = TestDistributionFactoryImpl()
        val server1 = factory.createServer(1630, emptyList())
        val server2 = factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        assertEquals(2, server1.getServers().size)
        assertEquals(2, server2.getServers().size)
    }

    @Test
    fun twoServerAndClient() {
        val factory = TestDistributionFactoryImpl()
        val server1 = factory.createServer(1630, emptyList())
        val server2 = factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        val clientDistribution = factory.createClient(Address("127.0.0.1", 1630))
        assertEquals(2, server1.getServers().size)
        assertEquals(2, server2.getServers().size)
        assertEquals(2, clientDistribution.getServers().size)
    }

    @Test
    fun serverAndClientCluster_ServerJoin_HaveNewServer() {
        val factory = TestDistributionFactoryImpl()
        val server1 = factory.createServer(1630, emptyList())
        val clientDistribution = factory.createClient(Address("127.0.0.1", 1630))
        val server2 = factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        assertEquals(2, server1.getServers().size)
        assertEquals(2, clientDistribution.getServers().size)
        assertEquals(2, server2.getServers().size)
    }

    @Test
    fun serverLeaveCluster_serverWillBeGone() {
        val server = this.factory.createServer(1630, emptyList())
        val server2 = this.factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        server.shutdown()
        assertEquals(1, server2.getServers().size)
    }

}