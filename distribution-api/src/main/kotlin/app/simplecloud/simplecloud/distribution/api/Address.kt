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

package app.simplecloud.simplecloud.distribution.api

/**
 * Created by IntelliJ IDEA.
 * Date: 21.03.2021
 * Time: 12:57
 * @author Frederick Baier
 */
class Address(
    val host: String,
    val port: Int
) : java.io.Serializable {

    constructor() : this("127.0.0.1", -1)

    fun asIpString(): String {
        return "${host}:${port}"
    }

    override fun toString(): String {
        return "Address(${asIpString()})"
    }

    companion object {
        fun fromIpString(string: String): Address {
            val array = string.split(":")
            if (array.size != 2) {
                throw IllegalArgumentException(
                    "Wrong Address format. Expected format: 'host:port' (e.g. 55.55.55.55:1630) but was $string"
                )
            }
            return Address(array[0], array[1].toInt())
        }
    }

}