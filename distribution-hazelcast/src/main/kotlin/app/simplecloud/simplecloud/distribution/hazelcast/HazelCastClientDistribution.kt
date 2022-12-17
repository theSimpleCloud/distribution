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
import app.simplecloud.simplecloud.distribution.api.DistributionComponent
import app.simplecloud.simplecloud.distribution.api.impl.ClientComponentImpl
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.client.config.ClientConnectionStrategyConfig
import com.hazelcast.core.HazelcastInstance
import java.net.ConnectException

class HazelCastClientDistribution(
    private val connectAddress: Address,
    private val classLoader: ClassLoader = HazelCastServerDistribution::class.java.classLoader,
) : AbstractHazelCastDistribution() {

    private val hazelCast: HazelcastInstance = createHazelCastInstance()

    private val selfComponent = ClientComponentImpl(this.hazelCast.localEndpoint.uuid)

    private fun createHazelCastInstance(): HazelcastInstance {
        val config = ClientConfig()
        config.classLoader = this::class.java.classLoader
        config.networkConfig.addAddress(connectAddress.asIpString())
        config.connectionStrategyConfig.reconnectMode = ClientConnectionStrategyConfig.ReconnectMode.OFF
        config.connectionStrategyConfig.connectionRetryConfig.clusterConnectTimeoutMillis = 1
        config.networkConfig.kubernetesConfig.isEnabled = false
        config.networkConfig.azureConfig.isEnabled = false
        config.networkConfig.awsConfig.isEnabled = false
        config.networkConfig.cloudConfig.isEnabled = false
        config.serializationConfig.compactSerializationConfig.isEnabled = true
        config.classLoader = this.classLoader
        val hazelcastClient = try {
            HazelcastClient.newHazelcastClient(config)
        } catch (e: IllegalStateException) {
            throw ConnectException()
        }
        return hazelcastClient
    }

    override fun getHazelCastInstance(): HazelcastInstance {
        return this.hazelCast
    }

    override fun getSelfComponent(): DistributionComponent {
        return this.selfComponent
    }

    override fun getConnectedClients(): List<ClientComponent> {
        return emptyList()
    }

    override fun shutdown() {
        this.hazelCast.shutdown()
    }

}
