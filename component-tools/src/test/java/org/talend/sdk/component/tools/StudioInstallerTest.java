/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
package org.talend.sdk.component.tools;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.talend.sdk.component.junit.base.junit5.TemporaryFolder;
import org.talend.sdk.component.junit.base.junit5.WithTemporaryFolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WithTemporaryFolder
class StudioInstallerTest {

    private File artifact;

    private File studioHome;

    private File configuration;

    @BeforeEach
    void initEnv(final TemporaryFolder temporaryFolder, final TestInfo info) throws IOException {
        final String testName =
                info.getTestMethod().orElseThrow(() -> new IllegalStateException("test name can't be null")).getName();
        this.studioHome = new File(temporaryFolder.getRoot(), testName);
        this.configuration = org.apache.ziplock.Files.mkdir(new File(this.studioHome, "configuration"));
        try (final Writer configIni = new FileWriter(new File(this.configuration, "config.ini"))) {
            // no-op
        }
        this.artifact = new File(temporaryFolder.getRoot(), testName + ".jar");
        try (final JarOutputStream out = new JarOutputStream(new FileOutputStream(artifact))) {
            out.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            out.closeEntry();
        }
    }

    @Test
    void normalInstall() throws IOException {
        final StudioInstaller installer = newStudioInstaller();
        installer.run();
        final File backup = new File(this.studioHome, "configuration/backup");
        assertTrue(backup.exists());
        assertEquals(1, requireNonNull(backup.listFiles((dir, name) -> name.startsWith("config.ini"))).length);
        assertSetup(this.studioHome);
        // 1 again cause already here so no other backup
        installer.run();
        assertSetup(this.studioHome);
        assertEquals(1, requireNonNull(backup.listFiles((dir, name) -> name.startsWith("config.ini"))).length);

    }

    @Test
    void reinstallWithLockedJar() throws IOException {
        final File m2Artifact =
                new File(configuration, ".m2/repository/gtest/atest/1.0-SNAPSHOT/atest-1.0-SNAPSHOT.jar");
        if (!m2Artifact.getParentFile().mkdirs()) {
            fail("Can't create m2 repository for the test ");
        }
        try (final Writer w = new FileWriter(m2Artifact)) {
            // done
        }

        try (final Reader lock = new FileReader(m2Artifact)) {// lock artifact then try to install
            final StudioInstaller installer = newStudioInstaller();
            installer.run();
        }
        final File backup = new File(studioHome, "configuration/backup");
        assertTrue(backup.exists());
        assertEquals(1, requireNonNull(backup.listFiles((dir, name) -> name.startsWith("config.ini"))).length);
        assertSetup(studioHome);
    }

    private StudioInstaller newStudioInstaller() {
        return new StudioInstaller("gtest:atest:1.0-SNAPSHOT", studioHome,
                singletonMap("gtest:atest:1.0-SNAPSHOT", artifact), LOGGER);
    }

    private void assertSetup(File studioHome) throws IOException {
        final File registration = new File(studioHome, "configuration/components-registration.properties");
        assertTrue(registration.exists());
        assertEquals(singleton("atest=gtest\\:atest\\:1.0-SNAPSHOT"),
                Files.readAllLines(registration.toPath()).stream().filter(l -> !l.startsWith("#")).collect(toSet()));

        final File configIni = new File(studioHome, "configuration/config.ini");
        assertEquals(
                singleton("component.java.registry="
                        + registration.getAbsolutePath().replace("\\", "\\\\").replace(":", "\\:")),
                Files.readAllLines(configIni.toPath()).stream().filter(l -> !l.startsWith("#")).collect(toSet()));

        assertTrue(new File(studioHome, "configuration/.m2/repository/gtest/atest/1.0-SNAPSHOT/atest-1.0-SNAPSHOT.jar")
                .exists());
    }

    private static final Log LOGGER = new Log() {

        @Override
        public void debug(final String s) {
            log.info(s);
        }

        @Override
        public void error(final String s) {
            log.error(s);
        }

        @Override
        public void info(final String s) {
            log.info(s);
        }
    };
}
