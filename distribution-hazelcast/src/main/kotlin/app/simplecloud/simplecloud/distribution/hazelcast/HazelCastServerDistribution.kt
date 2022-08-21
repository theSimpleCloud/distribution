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

import app.simplecloud.simplecloud.distribution.api.Address
import app.simplecloud.simplecloud.distribution.api.ClientComponent
import app.simplecloud.simplecloud.distribution.api.DistributionAware
import app.simplecloud.simplecloud.distribution.api.DistributionComponent
import app.simplecloud.simplecloud.distribution.api.impl.ClientComponentImpl
import app.simplecloud.simplecloud.distribution.api.impl.ServerComponentImpl
import com.hazelcast.config.Config
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance

class HazelCastServerDistribution(
    private val bindPort: Int,
    private val connectAddresses: List<Address>,
) : AbstractHazelCastDistribution() {

    private val hazelCast: HazelcastInstance = createHazelCastInstance()

    private val selfComponent = ServerComponentImpl(this.hazelCast.cluster.localMember.uuid)


    private fun createHazelCastInstance(): HazelcastInstance {
        val config = Config()
        config.networkConfig.port = this.bindPort
        config.networkConfig.join.awsConfig.isEnabled = false
        config.networkConfig.join.kubernetesConfig.isEnabled = false
        config.networkConfig.join.azureConfig.isEnabled = false

        config.networkConfig.join.tcpIpConfig.isEnabled = true
        for (address in connectAddresses) {
            config.networkConfig.join.tcpIpConfig.addMember(address.asIpString())
        }

        val serializerConfig = SerializerConfig()
            .setImplementation(DistributionAwareSerializer(this))
            .setTypeClass(DistributionAware::class.java)
        config.serializationConfig.addSerializerConfig(serializerConfig)
        return Hazelcast.newHazelcastInstance(config)
    }

    override fun getHazelCastInstance(): HazelcastInstance {
        return this.hazelCast
    }

    override fun getSelfComponent(): DistributionComponent {
        return this.selfComponent
    }

    override fun getConnectedClients(): List<ClientComponent> {
        return this.hazelCast.clientService.connectedClients.map { ClientComponentImpl(it.uuid) }
    }

    override fun shutdown() {
        this.hazelCast.shutdown()
    }

}
