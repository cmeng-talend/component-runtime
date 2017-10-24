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
package org.talend.component.sample.other;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.stream.IntStream;

import org.talend.component.api.configuration.Option;
import org.talend.component.api.configuration.action.Proposable;
import org.talend.component.api.configuration.type.DataSet;
import org.talend.component.api.configuration.ui.OptionsOrder;
import org.talend.component.api.configuration.ui.widget.Credential;
import org.talend.component.api.input.Emitter;
import org.talend.component.api.input.Producer;
import org.talend.component.api.service.Service;
import org.talend.component.api.service.completion.DynamicValues;
import org.talend.component.api.service.completion.Values;

import lombok.Data;

@Emitter(family = "complex", name = "demo")
public class ComplexComponent implements Serializable {

    private final ComplexDataSet dataset;

    public ComplexComponent(@Option("dataset") final ComplexDataSet dataset) {
        this.dataset = dataset;
    }

    @Producer
    public String value() {
        return "";
    }

    @Data
    @DataSet("complicated")
    public static class Credentials implements Serializable {

        @Option
        private String username;

        @Option
        @Credential
        private String password;
    }

    @Data
    @DataSet("complicated")
    @OptionsOrder({ "path", "credentials" })
    public static class ComplexDataSet implements Serializable {

        @Option
        private Credentials credentials;

        @Option
        @Proposable("path")
        private String path;
    }

    @Service
    public static class PathService {

        @DynamicValues(family = "complex", value = "path")
        public Values find(@Option("value") final String value) {
            return new Values(IntStream.range(1, 11).mapToObj(i -> "/opt/sample/file_" + i + ".txt")
                                       .map(Values.Item::new).collect(toList()));
        }
    }
}