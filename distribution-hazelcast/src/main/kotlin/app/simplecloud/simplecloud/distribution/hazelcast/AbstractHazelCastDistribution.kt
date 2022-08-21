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

import app.simplecloud.simplecloud.distribution.api.*
import app.simplecloud.simplecloud.distribution.api.impl.ServerComponentImpl
import com.hazelcast.core.HazelcastInstance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Date: 10.04.22
 * Time: 18:19
 * @author Frederick Baier
 *
 */
abstract class AbstractHazelCastDistribution : Distribution {

    private val userContext = ConcurrentHashMap<String, Any>()

    abstract fun getHazelCastInstance(): HazelcastInstance

    override fun getServers(): List<ServerComponent> {
        return getHazelCastInstance().cluster.members.map { ServerComponentImpl(it.uuid) }
    }

    override fun <K, V> getOrCreateCache(name: String): Cache<K, V> {
        return HazelCastCache(getHazelCastInstance().getMap(name))
    }

    override fun getMessageManager(): MessageManager {
        return HazelCastMessageManager(getSelfComponent(), getHazelCastInstance())
    }

    override fun getScheduler(name: String): ScheduledExecutorService {
        return HazelCastScheduledExecutorService(name, getHazelCastInstance(), this)
    }

    override fun getUserContext(): ConcurrentMap<String, Any> {
        return this.userContext
    }

}