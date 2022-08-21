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

package app.simplecloud.simplecloud.distribution.api.test

import app.simplecloud.simplecloud.distribution.api.Address
import app.simplecloud.simplecloud.distribution.api.Distribution
import app.simplecloud.simplecloud.distribution.api.DistributionFactory
import app.simplecloud.simplecloud.distribution.hazelcast.HazelcastDistributionFactory
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.net.ConnectException

/**
 * Date: 07.04.22
 * Time: 13:02
 * @author Frederick Baier
 *
 */
@Disabled
class DistributionTest {

    private lateinit var factory: DistributionFactory

    private var server: Distribution? = null
    private var server2: Distribution? = null
    private var server3: Distribution? = null
    private var client: Distribution? = null

    @BeforeEach
    fun setUp() {
        this.factory = HazelcastDistributionFactory()
    }

    @AfterEach
    fun tearDown() {
        this.server?.shutdown()
        this.server2?.shutdown()
        this.server3?.shutdown()
        this.client?.shutdown()
    }

    @Test
    fun newDistributionServer_hasOneServerAndZeroClients() {
        this.server = factory.createServer(1330, emptyList())
        assertEquals(1, this.server!!.getServers().size)
        assertEquals(0, this.server!!.getConnectedClients().size)
    }

    @Test
    fun newClientWithoutServer_WillThrowError() {
        assertThrows<ConnectException> {
            factory.createClient(Address("127.0.0.1", 1630))
        }
    }

    @Test
    fun newClientWithWrongAddressAndServer_wilThrowError() {
        this.server = factory.createServer(1630, emptyList())
        assertThrows<ConnectException> {
            factory.createClient(Address("127.0.0.1", 1631))
        }
    }

    @Test
    fun newClientWithServer_wilNotThrowError() {
        this.server = factory.createServer(1630, emptyList())
        this.client = factory.createClient(Address("127.0.0.1", 1630))
    }

    @Test
    fun newClientWithServer_HasOneClientAndOneServer() {
        this.server = factory.createServer(1630, emptyList())
        this.client = factory.createClient(Address("127.0.0.1", 1630))
        assertEquals(1, client!!.getServers().size)
        assertEquals(1, server!!.getServers().size)
        assertEquals(1, server!!.getConnectedClients().size)
    }

    @Test
    fun newServerWithNotExistingServerToConnectTo_willNotThrow() {
        this.server = factory.createServer(1630, listOf(Address("127.0.0.1", 1631)))
        assertEquals(1, server!!.getServers().size)
        assertEquals(0, server!!.getConnectedClients().size)
    }

    @Test
    fun serverConnectToServer() {
        this.server = factory.createServer(1630, emptyList())
        this.server2 = factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        assertEquals(2, server!!.getServers().size)
        assertEquals(2, server2!!.getServers().size)
    }

    @Test
    fun twoServerAndClient() {
        this.server = factory.createServer(1630, emptyList())
        this.server2 = factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        this.client = factory.createClient(Address("127.0.0.1", 1630))
        assertEquals(2, server!!.getServers().size)
        assertEquals(2, server2!!.getServers().size)
        assertEquals(2, client!!.getServers().size)
    }

    @Test
    fun serverAndClientCluster_ServerJoin_HaveNewServer() {
        this.server = factory.createServer(1630, emptyList())
        this.client = factory.createClient(Address("127.0.0.1", 1630))
        this.server2 = factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        assertEquals(2, client!!.getServers().size)
        assertEquals(2, client!!.getServers().size)
        assertEquals(2, server2!!.getServers().size)
    }

    @Test
    fun twoServers_ShutdownSecond_FirstWillContinue() {
        this.server = factory.createServer(1630, emptyList())
        this.server2 = factory.createServer(1631, listOf(Address("127.0.0.1", 1630)))
        this.server2!!.shutdown()
        Thread.sleep(1000)
        assertEquals(1, this.server!!.getServers().size)
    }

}