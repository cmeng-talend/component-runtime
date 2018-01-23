/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.sdk.component.studio.util;

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.sdk.component.studio.GAV;

public class TaCoKitConst {

    public static final String BUNDLE_ID = GAV.ARTIFACT_ID;

    public static final ERepositoryObjectType METADATA_TACOKIT = ERepositoryObjectType.valueOf("TACOKIT"); //$NON-NLS-1$

    public static final String METADATA_TACOKIT_PATH = METADATA_TACOKIT.getFolder();

    /**
     * Prefix used in repository node type for TaCoKit components
     */
    public static final String METADATA_TACOKIT_PREFIX = "metadata.tacokit.";

    public static final String IMAGE_TACOKIT_REPOSITORY_PATH = "/icon/connection.png"; //$NON-NLS-1$

    public static final String IMAGE_TACOKIT_CONFIGURATION_PATH = "/icon/configuration_16x16.png"; //$NON-NLS-1$

    public static final String GUESS_SCHEMA_COMPONENT_NAME = "tTaCoKitGuessSchema"; //$NON-NLS-1$

    /**
     * DON'T modify the value, otherwise please also modify it in tTaCoKitGuessSchema_begin.javajet
     */
    public static final String GUESS_SCHEMA_PARAMETER_TEMP_FILE_KEY =
            "___TACOKIT_GUESS_SCHEMA_PARAMETER_TEMP_FILE_KEY___"; //$NON-NLS-1$

    /**
     * DON'T modify the value, otherwise please also modify it in tTaCoKitGuessSchema_begin.javajet
     */
    public static final String GUESS_SCHEMA_PARAMETER_ENCODING_KEY =
            "___TACOKIT_GUESS_SCHEMA_PARAMETER_ENCODING_KEY___"; //$NON-NLS-1$

    public static final String TYPE_STRING = "STRING"; //$NON-NLS-1$

}
