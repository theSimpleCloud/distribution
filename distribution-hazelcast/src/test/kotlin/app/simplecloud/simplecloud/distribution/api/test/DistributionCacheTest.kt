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
import org.junit.jupiter.api.*

/**
 * Date: 09.04.22
 * Time: 11:04
 * @author Frederick Baier
 *
 */
@Disabled
class DistributionCacheTest {
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
    fun onlyOneServer() {
        this.server = this.factory.createServer(1630, emptyList())
        val cache = server!!.getOrCreateCache<String, String>("test")
        cache["test1"] = "test2"
        Assertions.assertEquals("test2", cache["test1"])
    }

    @Test
    fun serverAndClient_cacheIsSynchronized() {
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
        val serverCache = server!!.getOrCreateCache<String, String>("test")
        val clientCache = client!!.getOrCreateCache<String, String>("test")
        serverCache["test1"] = "test2"
        Assertions.assertEquals("test2", clientCache["test1"])
        clientCache.remove("test1")
        Assertions.assertNull(serverCache["test1"])
    }

    @Test
    fun separateCache_areIndependent() {
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
        val serverCache = server!!.getOrCreateCache<String, String>("one")
        val clientCache = client!!.getOrCreateCache<String, String>("two")
        serverCache["test1"] = "test2"
        Assertions.assertNull(clientCache["test1"])
        clientCache.remove("test1")
        Assertions.assertNotNull(serverCache["test1"])
    }

    @Test
    fun listenerRegistered_NoItemGetsAdded_WillNotFire() {
        this.server = this.factory.createServer(1630, emptyList())
        val serverCache = server!!.getOrCreateCache<String, String>("one")
        var hasFired = false
        val entryListener = object : EntryListener<String, String> {

            override fun entryAdded(entry: Pair<String, String>) {

            }

            override fun entryUpdated(entry: Pair<String, String>) {
                hasFired = true
            }

            override fun entryRemoved(entry: Pair<String, String>) {

            }
        }
        serverCache.addEntryListener(entryListener)
        Assertions.assertFalse(hasFired)
    }

    @Test
    fun listenerRegistered_ItemGetsAdded_WillFire() {
        this.server = this.factory.createServer(1630, emptyList())
        val serverCache = server!!.getOrCreateCache<String, String>("one")
        var hasFired = false
        val entryListener = object : EntryListener<String, String> {

            override fun entryAdded(entry: Pair<String, String>) {
                hasFired = true
            }

            override fun entryUpdated(entry: Pair<String, String>) {
            }

            override fun entryRemoved(entry: Pair<String, String>) {

            }
        }
        serverCache.addEntryListener(entryListener)
        serverCache.put("test", "value")
        Thread.sleep(300)
        Assertions.assertTrue(hasFired)
    }

    @Test
    fun listenerRegistered_NotExistingItemGetsRemoved_WillNotFire() {
        this.server = this.factory.createServer(1630, emptyList())
        val serverCache = server!!.getOrCreateCache<String, String>("one")
        var hasFired = false
        val entryListener = object : EntryListener<String, String> {

            override fun entryAdded(entry: Pair<String, String>) {

            }

            override fun entryUpdated(entry: Pair<String, String>) {
            }

            override fun entryRemoved(entry: Pair<String, String>) {
                hasFired = true
            }
        }
        serverCache.addEntryListener(entryListener)
        serverCache.remove("test", "value")
        Assertions.assertFalse(hasFired)
    }

    @Test
    fun listenerRegistered_ItemGetsRemoved_WillFire() {
        this.server = this.factory.createServer(1630, emptyList())
        val serverCache = server!!.getOrCreateCache<String, String>("one")
        var hasFired = false
        val entryListener = object : EntryListener<String, String> {

            override fun entryAdded(entry: Pair<String, String>) {

            }

            override fun entryUpdated(entry: Pair<String, String>) {
            }

            override fun entryRemoved(entry: Pair<String, String>) {
                hasFired = true
            }
        }
        serverCache.addEntryListener(entryListener)
        serverCache.put("test", "value")
        serverCache.remove("test")
        Thread.sleep(300)
        Assertions.assertTrue(hasFired)
    }

    @Test
    fun itemOnServerGetsAdded_WillFireOnClient() {
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
        val serverCache = server!!.getOrCreateCache<String, String>("one")
        val clientCache = client!!.getOrCreateCache<String, String>("one")
        var hasFired = false
        val entryListener = object : EntryListener<String, String> {

            override fun entryAdded(entry: Pair<String, String>) {
                hasFired = true
            }

            override fun entryUpdated(entry: Pair<String, String>) {
            }

            override fun entryRemoved(entry: Pair<String, String>) {
            }
        }
        clientCache.addEntryListener(entryListener)
        serverCache.put("test", "value")
        Thread.sleep(300)
        Assertions.assertTrue(hasFired)
    }

    @Test
    fun itemSetTwice_willFireUpdate() {
        this.server = this.factory.createServer(1630, emptyList())
        val serverCache = server!!.getOrCreateCache<String, String>("one")
        var hasFired = false
        val entryListener = object : EntryListener<String, String> {

            override fun entryAdded(entry: Pair<String, String>) {
            }

            override fun entryUpdated(entry: Pair<String, String>) {
                hasFired = true
            }

            override fun entryRemoved(entry: Pair<String, String>) {
            }
        }
        serverCache.addEntryListener(entryListener)
        serverCache.put("test", "value")
        serverCache.put("test", "value2")
        Thread.sleep(300)
        Assertions.assertTrue(hasFired)
    }

    @Test
    fun filterTest() {
        this.server = this.factory.createServer(1630, emptyList())
        val serverCache = server!!.getOrCreateCache<String, Int>("one")
        serverCache.put("a", 1)
        serverCache.put("a4", 2)
        serverCache.put("a3", 3)
        serverCache.put("b", 4)
        serverCache.put("b1", 5)
        val values = serverCache.distributedQuery { key, _ -> key.contains("a") }
        Assertions.assertEquals(hashSetOf(1, 2, 3), values.toHashSet())
    }

    @Test
    fun filterTest2() {
        this.server = this.factory.createServer(1630, emptyList())
        val serverCache = server!!.getOrCreateCache<String, Int>("one")
        serverCache.put("a", 1)
        serverCache.put("a4", 2)
        serverCache.put("a3", 3)
        serverCache.put("b", 4)
        serverCache.put("b1", 5)
        val values = serverCache.distributedQuery { key, _ -> key.contains("b") }
        Assertions.assertEquals(hashSetOf(4, 5), values.toHashSet())
    }

    @Test
    fun itemsOnServerGetsAdded_FilterOnClient() {
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
        val serverCache = server!!.getOrCreateCache<String, Int>("one")
        val clientCache = client!!.getOrCreateCache<String, Int>("one")
        serverCache.put("a", 1)
        serverCache.put("a4", 2)
        serverCache.put("a3", 3)
        serverCache.put("b", 4)
        serverCache.put("b1", 5)
        val values = clientCache.distributedQuery { key, _ -> key.contains("a") }
        Assertions.assertEquals(hashSetOf(1, 2, 3), values.toHashSet())
    }

    @Test
    fun entryProcessor_OnClient() {
        this.server = this.factory.createServer(1630, emptyList())
        this.client = this.factory.createClient(Address("127.0.0.1", 1630))
        val serverCache = server!!.getOrCreateCache<String, Int>("one")
        val clientCache = client!!.getOrCreateCache<String, Int>("one")
        serverCache.put("a", 1)
        val success = serverCache.executeOnKey("a", object : DistributionEntryProcessor<String, Int, Boolean> {
            override fun process(entry: MutableMap.MutableEntry<String?, Int?>): Boolean {
                val value = entry.value?: return false
                entry.setValue(value + 1)
                return true
            }
        })

        val values = clientCache.distributedQuery { key, _ -> key.contains("a") }
        Assertions.assertEquals(hashSetOf(1, 2, 3), values.toHashSet())
    }

}