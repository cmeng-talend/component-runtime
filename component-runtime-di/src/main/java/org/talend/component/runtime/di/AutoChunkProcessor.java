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
package org.talend.component.runtime.di;

import org.talend.component.runtime.base.Lifecycle;
import org.talend.component.runtime.output.InputFactory;
import org.talend.component.runtime.output.OutputFactory;
import org.talend.component.runtime.output.Processor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AutoChunkProcessor implements Lifecycle {

    private static final OutputFactory FAILING_OUTPUT_FACTORY = name -> {
        throw new IllegalArgumentException("Output from @AfterGroup is not supported here");
    };

    private final int chunkSize;

    private final Processor processor;

    private int processedItemCount = 0;

    public void onElement(final InputFactory ins, final OutputFactory outs) {
        if (processedItemCount == 0) {
            processor.beforeGroup();
        }
        try {
            processor.onNext(ins, outs);
            processedItemCount++;
        } finally {
            if (processedItemCount == chunkSize) {
                processor.afterGroup(outs);
            }
        }
    }

    @Override
    public void stop() {
        try {
            if (processedItemCount > 0) {
                processor.afterGroup(FAILING_OUTPUT_FACTORY);
            }
        } finally {
            processor.stop();
        }
    }

    @Override
    public String plugin() {
        return processor.plugin();
    }

    @Override
    public String rootName() {
        return processor.rootName();
    }

    @Override
    public String name() {
        return processor.name();
    }

    @Override
    public void start() {
        processor.start();
    }
}