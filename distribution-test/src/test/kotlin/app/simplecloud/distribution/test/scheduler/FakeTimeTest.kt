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

package app.simplecloud.distribution.test.scheduler

import app.simplecloud.simplecloud.distribution.test.scheduler.Time
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

/**
 * Date: 05.08.22
 * Time: 10:18
 * @author Frederick Baier
 *
 */
class FakeTimeTest {

    private var time = Time()

    @BeforeEach
    fun setUp() {
        this.time = Time()
    }

    @Test
    fun test() {
        Assertions.assertEquals(System.currentTimeMillis(), time.currentTime())
    }

    @Test
    fun test2() {
        time.skip(1, TimeUnit.SECONDS)
        Assertions.assertEquals(System.currentTimeMillis() + 1000, time.currentTime())
    }

    @Test
    fun tes3() {
        time.back(1, TimeUnit.SECONDS)
        Assertions.assertEquals(System.currentTimeMillis() - 1000, time.currentTime())
    }

}