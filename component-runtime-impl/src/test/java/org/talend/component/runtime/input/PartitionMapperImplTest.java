/**
 *  Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.talend.component.runtime.input;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;
import org.talend.component.api.input.Assessor;
import org.talend.component.api.input.Emitter;
import org.talend.component.api.input.PartitionSize;
import org.talend.component.api.input.Producer;
import org.talend.component.api.input.Split;
import org.talend.component.runtime.serialization.Serializer;

public class PartitionMapperImplTest {

    @Test
    public void split() {
        final Mapper mapper = new PartitionMapperImpl("Root", "Test", null, "Plugin", false, new SampleMapper());
        assertEquals(10, mapper.assess());

        final List<Mapper> split = mapper.split(3);
        assertEquals(3, split.stream().distinct().count());
        split.forEach(s -> {
            assertTrue(PartitionMapperImpl.class.isInstance(s));
            assertNotSame(mapper, s);
            assertInput(s);
        });
    }

    @Test
    public void create() {
        assertInput(new PartitionMapperImpl("Root", "Test", null, "Plugin", false, new SampleMapper()));
    }

    @Test
    public void serialization() throws IOException, ClassNotFoundException {
        final Mapper mapper = new PartitionMapperImpl("Root", "Test", null, "Plugin", false, new SampleMapper());
        final Mapper copy = Serializer.roundTrip(mapper);
        assertNotSame(copy, mapper);
        assertEquals("Root", copy.rootName());
        assertEquals("Test", copy.name());
        assertEquals("Plugin", copy.plugin());
    }

    private void assertInput(final Mapper mapper) {
        assertTrue(Input.class.isInstance(mapper.create()));
        assertTrue(Sample.class.isInstance(mapper.create().next())); // it was a sample in
    }

    public static class SampleMapper implements Serializable {

        @Assessor
        public long assess() {
            return 10;
        }

        @Split
        public Collection<SampleMapper> split(@PartitionSize final int partitions) {
            return IntStream.range(0, partitions).mapToObj(i -> new SampleMapper()).collect(toList());
        }

        @Emitter
        public SampleIn create() {
            return new SampleIn();
        }
    }

    public static class SampleIn implements Serializable {

        @Producer
        public Sample next() {
            return new Sample();
        }
    }

    public static class Sample {

    }
}