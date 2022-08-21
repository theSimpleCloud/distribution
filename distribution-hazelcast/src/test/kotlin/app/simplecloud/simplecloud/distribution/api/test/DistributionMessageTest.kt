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

import app.simplecloud.simplecloud.distribution.api.*
import app.simplecloud.simplecloud.distribution.hazelcast.HazelcastDistributionFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * Date: 08.04.22
 * Time: 17:56
 * @author Frederick Baier
 *
 */
@Disabled
class DistributionMessageTest {

    private lateinit var factory: DistributionFactory

    private var server: Distribution? = null
    private var client: Distribution? = null

    @BeforeEach
    fun setUp() {
        this.factory = HazelcastDistributionFactory()
    }

    @AfterEach
    fun tearDown() {
        server?.shutdown()
        client?.shutdown()
    }

    @Test
    fun newServer_receiveSelfMessages() {
        this.server = this.factory.createServer(1630, emptyList())
        val messageManager = server!!.getMessageManager()
        var messageReceived = false
        messageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                messageReceived = true
            }

        })
        messageManager.sendMessage("test")
        Thread.sleep(500)
        assertTrue(messageReceived)
    }

    @Test
    fun serverAndClient_ClientReceiveMessage() {
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
        val clientMessageManager = client!!.getMessageManager()
        var received = 0
        clientMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        val serverMessageManager = server!!.getMessageManager()
        serverMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        serverMessageManager.sendMessage("test")
        Thread.sleep(1000)
        assertEquals(2, received)
    }

    @Test
    fun singleReceiverMessage() {
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
        val clientMessageManager = client!!.getMessageManager()
        var received = 0
        clientMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        val serverMessageManager = server!!.getMessageManager()
        serverMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        serverMessageManager.sendMessage("test", client!!.getSelfComponent())
        Thread.sleep(500)
        assertEquals(1, received)
    }

}