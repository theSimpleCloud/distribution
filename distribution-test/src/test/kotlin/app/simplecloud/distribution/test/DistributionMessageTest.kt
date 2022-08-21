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
import app.simplecloud.simplecloud.distribution.api.DistributionComponent
import app.simplecloud.simplecloud.distribution.api.DistributionFactory
import app.simplecloud.simplecloud.distribution.api.MessageListener
import app.simplecloud.simplecloud.distribution.test.TestDistributionFactoryImpl
import app.simplecloud.simplecloud.distribution.test.VirtualNetwork
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Date: 08.04.22
 * Time: 17:56
 * @author Frederick Baier
 *
 */
class DistributionMessageTest {

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
    fun newServer_receiveSelfMessages() {
        val server = this.factory.createServer(1630, emptyList())
        val messageManager = server.getMessageManager()
        var messageReceived = false
        messageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                messageReceived = true
            }

        })
        messageManager.sendMessage("test")

        assertTrue(messageReceived)
    }

    @Test
    fun serverAndClient_ClientReceiveMessage() {
        val server = this.factory.createServer(1630, emptyList())
        val client = this.factory.createClient(Address("127.0.0.1", 1630))
        val clientMessageManager = client.getMessageManager()
        var received = 0
        clientMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        val serverMessageManager = server.getMessageManager()
        serverMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        serverMessageManager.sendMessage("test")

        assertEquals(2, received)
    }

    @Test
    fun singleReceiverMessage() {
        val server = this.factory.createServer(1630, emptyList())
        val client = this.factory.createClient(Address("127.0.0.1", 1630))
        val clientMessageManager = client.getMessageManager()
        var received = 0
        clientMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        val serverMessageManager = server.getMessageManager()
        serverMessageManager.setMessageListener(object : MessageListener {

            override fun messageReceived(message: Any, sender: DistributionComponent) {
                received++
            }

        })
        serverMessageManager.sendMessage("test", client.getSelfComponent())

        assertEquals(1, received)
    }

}