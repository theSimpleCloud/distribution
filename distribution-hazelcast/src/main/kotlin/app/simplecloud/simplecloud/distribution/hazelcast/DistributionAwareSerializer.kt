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

import app.simplecloud.simplecloud.distribution.api.Distribution
import app.simplecloud.simplecloud.distribution.api.DistributionAware
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Date: 14.08.22
 * Time: 08:44
 * @author Frederick Baier
 *
 */
class DistributionAwareSerializer(
    private val distribution: Distribution,
) : StreamSerializer<DistributionAware> {

    override fun getTypeId(): Int {
        return 1
    }

    override fun read(input: ObjectDataInput): DistributionAware {
        val byteArray = input.readByteArray()
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val readObject = objectInputStream.readObject() as DistributionAware
        readObject.setDistribution(this.distribution)
        return readObject
    }

    override fun write(out: ObjectDataOutput, distributionAware: DistributionAware) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(distributionAware)
        objectOutputStream.flush()
        out.writeByteArray(byteArrayOutputStream.toByteArray())
    }

}