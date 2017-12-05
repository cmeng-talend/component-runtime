/**
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.runtime.manager.reflect.parameterenricher;

import java.lang.annotation.Annotation;
import java.util.HashMap;

import org.junit.Test;
import org.talend.sdk.component.api.configuration.type.DataSet;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;

public class ConfigurationTypeParameterEnricherTest {

    @Test
    public void readConfigTypes() {
        final ConfigurationTypeParameterEnricher enricher = new ConfigurationTypeParameterEnricher();
        assertEquals(new HashMap<String, String>() {

            {
                put("tcomp::configurationtype::type", "dataset");
                put("tcomp::configurationtype::name", "test");
            }
        }, enricher.onParameterAnnotation("testParam", null, new DataSet() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return DataSet.class;
            }

            @Override
            public String value() {
                return "test";
            }
        }));
        assertEquals(emptyMap(), enricher.onParameterAnnotation("testParam", null, new Override() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Override.class;
            }
        }));
    }
}