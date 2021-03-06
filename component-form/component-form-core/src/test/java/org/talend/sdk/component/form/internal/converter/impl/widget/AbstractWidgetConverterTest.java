/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.form.internal.converter.impl.widget;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.junit.jupiter.api.Test;
import org.talend.sdk.component.form.internal.converter.PropertyContext;
import org.talend.sdk.component.form.model.jsonschema.JsonSchema;
import org.talend.sdk.component.form.model.uischema.UiSchema;
import org.talend.sdk.component.server.front.model.ActionReference;
import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;

class AbstractWidgetConverterTest {

    @Test
    void emptyObjectAsParamTolerance() {
        new AbstractWidgetConverter(emptyList(), emptyList(), emptyList(), new JsonSchema(), "en") {

            @Override
            public CompletionStage<PropertyContext<?>> convert(final CompletionStage<PropertyContext<?>> context) {
                throw new UnsupportedOperationException("shouldnt be called in this test");
            }

            {
                final List<UiSchema.Parameter> noLeafParams = toParams(
                        singletonList(new SimplePropertyDefinition("me", "me", null, "OBJECT", null,
                                new PropertyValidation(), emptyMap(), null, null)),
                        new SimplePropertyDefinition("me", "me", null, "OBJECT", null, new PropertyValidation(),
                                emptyMap(), null, null),
                        new ActionReference("test", "act", "sthg",
                                singletonList(new SimplePropertyDefinition("me", "me", null, "OBJECT", null,
                                        new PropertyValidation(), singletonMap("definition::parameter::index", "1"),
                                        null, null))),
                        "me");
                assertTrue(noLeafParams.isEmpty()); // no leaf

                final List<UiSchema.Parameter> parameters = toParams(
                        asList(new SimplePropertyDefinition("me", "me", null, "OBJECT", null, new PropertyValidation(),
                                emptyMap(), null, null),
                                new SimplePropertyDefinition("me.url", "url", null, "STRING", null,
                                        new PropertyValidation(), emptyMap(), null, null)),
                        new SimplePropertyDefinition("me", "me", null, "OBJECT", null, new PropertyValidation(),
                                emptyMap(), null, null),
                        new ActionReference("test", "act", "sthg",
                                singletonList(new SimplePropertyDefinition("me", "me", null, "OBJECT", null,
                                        new PropertyValidation(), singletonMap("definition::parameter::index", "1"),
                                        null, null))),
                        "me");
                assertEquals(1, parameters.size());
                assertEquals("me.url", parameters.iterator().next().getPath());
            }
        };
    }
}
