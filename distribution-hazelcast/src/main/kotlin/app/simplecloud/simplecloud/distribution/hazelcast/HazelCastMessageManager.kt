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

package app.simplecloud.simplecloud.distribution.hazelcast

import app.simplecloud.simplecloud.distribution.api.DistributionComponent
import app.simplecloud.simplecloud.distribution.api.MessageListener
import app.simplecloud.simplecloud.distribution.api.MessageManager
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.topic.ITopic

class HazelCastMessageManager(
    private val selfComponent: DistributionComponent,
    private val hazelCast: HazelcastInstance
) : MessageManager {

    @Volatile
    private var messageListener: MessageListener = object : MessageListener {
        override fun messageReceived(message: Any, sender: DistributionComponent) {
        }
    }

    init {
        createAddressedMessageListener()
        createAllMessageListener()
    }

    override fun sendMessage(any: Any) {
        this.hazelCast.getTopic<HazelCastPacket>("all")
            .publish(HazelCastPacket(this.selfComponent, any))
    }

    override fun sendMessage(any: Any, receiver: DistributionComponent) {
        this.hazelCast.getTopic<HazelCastPacket>(receiver.getDistributionId().toString())
            .publish(HazelCastPacket(this.selfComponent, any))
    }

    override fun setMessageListener(messageListener: MessageListener) {
        this.messageListener = messageListener
    }

    private fun createAddressedMessageListener() {
        val topic = this.hazelCast.getTopic<HazelCastPacket>(this.selfComponent.getDistributionId().toString())
        addListenerToTopic(topic)
    }

    private fun createAllMessageListener() {
        val topic = this.hazelCast.getTopic<HazelCastPacket>("all")
        addListenerToTopic(topic)
    }

    private fun addListenerToTopic(topic: ITopic<HazelCastPacket>) {
        topic.addMessageListener {
            val packet = it.messageObject
            this.messageListener.messageReceived(packet.message, packet.sender)
        }
    }

}
