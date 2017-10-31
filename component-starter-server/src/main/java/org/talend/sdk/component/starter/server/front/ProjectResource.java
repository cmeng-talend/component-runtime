/**
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.starter.server.front;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.talend.sdk.component.starter.server.model.FactoryConfiguration;
import org.talend.sdk.component.starter.server.model.ProjectModel;
import org.talend.sdk.component.starter.server.service.ProjectGenerator;
import org.talend.sdk.component.starter.server.service.domain.ProjectRequest;

@Path("project")
@ApplicationScoped
public class ProjectResource {

    @Inject
    private ProjectGenerator generator;

    private FactoryConfiguration configuration;

    @PostConstruct
    private void init() {
        final List<String> buildTypes = new ArrayList<>(generator.getGenerators().keySet());
        buildTypes.sort(String::compareTo);

        final Map<String, List<FactoryConfiguration.Facet>> facets = generator.getFacets().values().stream()
                .collect(toMap(e -> e.category().getHumanName(),
                        e -> new ArrayList<>(singletonList(new FactoryConfiguration.Facet(e.name(), e.description()))),
                        (u, u2) -> {
                            if (u == null) {
                                return u2;
                            }
                            u.addAll(u2);
                            return u;
                        }, TreeMap::new));
        facets.forEach((k, v) -> v.sort(Comparator.comparing(FactoryConfiguration.Facet::getName)));

        configuration = new FactoryConfiguration(buildTypes, facets);
    }

    @GET
    @Path("configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public FactoryConfiguration getConfiguration() {
        return configuration;
    }

    @POST
    @Path("zip")
    @Produces("application/zip")
    @Consumes(MediaType.APPLICATION_JSON)
    public StreamingOutput create(final ProjectModel model) {
        return out -> generator.generate(toRequest(model), out);
    }

    private ProjectRequest toRequest(final ProjectModel model) {
        String packageBase = ofNullable(model.getGroup()).orElse("com.component").replace('/', '.');
        if (packageBase.endsWith(".")) {
            packageBase = packageBase.substring(0, packageBase.length() - 1);
        }
        if (packageBase.isEmpty()) {
            packageBase = "component";
        }

        return new ProjectRequest(ofNullable(model.getBuildType()).orElse("maven").toLowerCase(Locale.ENGLISH),
                new ProjectRequest.BuildConfiguration(
                        ofNullable(model.getName()).orElse("A Talend generated Component Starter Project"),
                        ofNullable(model.getDescription()).orElse("An application generated by the Talend Component Kit Starter"),
                        "jar", packageBase, ofNullable(model.getArtifact()).orElse("application"),
                        ofNullable(model.getVersion()).orElse("0.0.1-SNAPSHOT"), "1.8"),
                ofNullable(model.getPackageBase()).orElse("com.application").replace('/', '.'),
                ofNullable(model.getFacets()).orElse(emptyList()),
                ofNullable(model.getSources())
                        .map(s -> s.stream()
                                .map(i -> new ProjectRequest.SourceConfiguration(i.getName(), i.getIcon(),
                                        toStructure(false, i.getConfigurationStructure()).getStructure(),
                                        toStructure(i.isGenericOutput(), i.getOutputStructure())))
                                .collect(toList()))
                        .orElse(emptyList()),
                ofNullable(model.getProcessors())
                        .map(s -> s.stream()
                                .map(i -> new ProjectRequest.ProcessorConfiguration(i.getName(), i.getIcon(),
                                        toStructure(false, i.getConfigurationStructure()).getStructure(),
                                        ofNullable(i.getInputStructures())
                                                .map(is -> is.stream()
                                                        .collect(toMap(ProjectModel.NamedModel::getName,
                                                                nm -> toStructure(nm.isGeneric(), nm.getStructure()))))
                                                .orElse(emptyMap()),
                                        ofNullable(i.getOutputStructures())
                                                .map(is -> is.stream()
                                                        .collect(toMap(ProjectModel.NamedModel::getName,
                                                                nm -> toStructure(nm.isGeneric(), nm.getStructure()))))
                                                .orElse(emptyMap())))
                                .collect(toList()))
                        .orElse(emptyList()),
                model.getFamily(), model.getCategory());
    }

    private ProjectRequest.StructureConfiguration toStructure(final boolean generic, final ProjectModel.Model model) {
        return new ProjectRequest.StructureConfiguration(!generic ? new ProjectRequest.DataStructure(model.getEntries() == null
                ? emptyList()
                : model.getEntries().stream()
                        .map(e -> new ProjectRequest.Entry(e.getName(), e.getType(),
                                e.getModel() != null ? toStructure(false, e.getModel()).getStructure() : null))
                        .collect(toList()))
                : null, generic);
    }
}