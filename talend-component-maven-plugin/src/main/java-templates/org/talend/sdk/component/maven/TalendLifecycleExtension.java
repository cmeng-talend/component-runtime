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
package org.talend.sdk.component.maven;

import java.util.List;
import java.util.Optional;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.component.annotations.Component;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "talend-component")
public class TalendLifecycleExtension extends AbstractMavenLifecycleParticipant {

    @Override
    public void afterProjectsRead(final MavenSession session) {
        final Optional<Plugin> plugin = session.getCurrentProject()
                                               .getBuild()
                                               .getPlugins()
                                               .stream()
                                               .filter(p -> "${project.groupId}".equals(
                                                       p.getGroupId()) && "${project.artifactId}".equals(
                                                       p.getArtifactId()))
                                               .findFirst();
        final List<PluginExecution> executions;
        if (plugin.isPresent()) {
            executions = plugin.get().getExecutions();
        } else {
            final Plugin def = new Plugin();
            def.setGroupId("${project.groupId}");
            def.setArtifactId("${project.artifactId}");
            def.setVersion("${project.version}");
            session.getCurrentProject().getBuild().getPlugins().add(def);

            executions = def.getExecutions();
        }

        if (isExecutionMissing(executions, "validate")) {
            final PluginExecution validate = new PluginExecution();
            validate.setId("talend-validate");
            validate.addGoal("validate");
            validate.setPhase("process-classes");
            executions.add(validate);
        }
        if (isExecutionMissing(executions, "dependencies")) {
            final PluginExecution dependencies = new PluginExecution();
            dependencies.setId("talend-dependencies");
            dependencies.addGoal("dependencies");
            dependencies.setPhase("process-classes");
            executions.add(dependencies);
        }
        if (isExecutionMissing(executions, "asciidoc")) {
            final PluginExecution documentation = new PluginExecution();
            documentation.setId("talend-dependencies");
            documentation.addGoal("asciidoc");
            documentation.setPhase("process-classes");
            executions.add(documentation);
        }
    }

    private boolean isExecutionMissing(final List<PluginExecution> executions, final String goal) {
        return executions.stream().noneMatch(e -> e.getGoals() != null && e.getGoals().contains(goal));
    }
}