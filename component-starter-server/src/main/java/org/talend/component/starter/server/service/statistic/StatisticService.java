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
package org.talend.component.starter.server.service.statistic;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.talend.component.starter.server.service.event.CreateProject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class StatisticService {

    // TODO: move to an actual backend like elasticsearch
    public void save(final CreateProject project) {
        project.getFacets().forEach(f -> log.info("[STATISTICS][facet=" + f + "][project=" + project.getName() + "]"));
    }

    @ApplicationScoped
    public static class ProjectListener {

        @Inject
        private StatisticService statistics;

        @Inject
        @ConfigProperty(name = "statistics.threads", defaultValue = "8")
        private Integer threads;

        @Inject
        @ConfigProperty(name = "statistics.retries", defaultValue = "3")
        private Integer retries;

        @Inject
        @ConfigProperty(name = "statistics.retry-sleep", defaultValue = "250")
        private Integer retrySleep;

        @Inject
        @ConfigProperty(name = "statistics.shutdown-timeout", defaultValue = "50000")
        private Integer shutdownTimeout;

        private ExecutorService executorService;

        private volatile boolean skip = false;

        @PostConstruct
        private void init() {
            executorService = Executors.newFixedThreadPool(threads);
        }

        // don't block to return ASAP to the client, not very important if it fails for the end user
        void capture(@Observes final CreateProject createProject) {
            if (skip) {
                return;
            }
            executorService.submit(() -> {
                for (int i = 0; i < retries; i++) {
                    try {
                        statistics.save(createProject);
                        return;
                    } catch (final Exception te) {
                        final Throwable e = te.getCause();
                        if (e != null) {
                            if (retries - 1 == i) { // no need to retry
                                failed(createProject);
                                throw RuntimeException.class.isInstance(e) ? RuntimeException.class.cast(e)
                                        : new IllegalStateException(e);
                            }

                            if (retrySleep > 0) {
                                try {
                                    sleep(retrySleep);
                                } catch (final InterruptedException ie) {
                                    Thread.interrupted();
                                    break;
                                }
                            }
                        }
                    }
                }
                // we shouldn't come there so warn
                failed(createProject);
            });
        }

        private void failed(final CreateProject createProject) {
            log.warn("Can't save statistics of " + createProject + " in " + retries + " retries.");
        }

        @PreDestroy
        private void tryToSaveCurrentTasks() {
            executorService.shutdown();

            skip = true;
            try {
                if (!executorService.awaitTermination(shutdownTimeout, MILLISECONDS)) {
                    log.warn("Some statistics have been missed, this is not important but reporting can not be 100% accurate");
                }
            } catch (final InterruptedException e) {
                Thread.interrupted();
                log.warn("interruption during statistics shutdown, {}", e.getMessage());
            }
        }
    }
}