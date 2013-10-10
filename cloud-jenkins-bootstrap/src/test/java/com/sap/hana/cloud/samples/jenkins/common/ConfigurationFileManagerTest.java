package com.sap.hana.cloud.samples.jenkins.common;

/*
 * #%L
 * SAP HANA Cloud Platform Samples - Cloud Jenkins
 * %%
 * Copyright (C) 2013 SAP AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sap.hana.cloud.samples.jenkins.storage.Storage;

public class ConfigurationFileManagerTest {

    private static final class EntryNamesVerifyingStorage implements Storage {
        private final List<String> names = new ArrayList<String>();

        @Override
        public void save(final InputStream stream) {
            final ZipInputStream zipInput = new ZipInputStream(stream);
            try {
                ZipEntry entry = zipInput.getNextEntry();
                while (entry != null) {
                    names.add(entry.getName());
                    entry = zipInput.getNextEntry();
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InputStream load() {
            return getExampleInputStream();
        }
    }

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    private File defaultFile;

    @Before
    public void setup() {
        defaultFile = new File(tempDir.getRoot(), ".jenkins/proxy.xml");
    }

    @Test
    public void testExampleConfigurationProvidesExampleFiles() throws Exception {
        final ConfigurationFileManager subject = getTestJenkinsConfiguration();
        subject.initConfiguration();
        for (final String expectedFileName : new String[] { "example.txt", "example/example.txt", "emptyExample" }) {
            assertTrue(new File(tempDir.getRoot(), expectedFileName).exists());
        }
    }

    @Test
    public void testExampleConfigurationLeavesExistingFileAlone() throws Exception {
        final File existingFile = tempDir.newFile();
        final ConfigurationFileManager subject = getTestJenkinsConfiguration();
        subject.initConfiguration();
        assertTrue(existingFile.exists());
    }

    @Test
    public void testBootstrapExtractsDefaultArchive() {
        final StoredOrDefaultConfigurationProvider configurationProvider =
            new StoredOrDefaultConfigurationProvider(StoredOrDefaultConfigurationProviderTest.setupMockStorage(null));
        final ConfigurationFileManager subject = getTestJenkinsConfiguration(configurationProvider);
        subject.initConfiguration();
        assertTrue(defaultFile.exists());
    }

    @Test
    public void testSecondBootstrapDoesNothing() {
        final StoredOrDefaultConfigurationProvider configurationProvider =
            new StoredOrDefaultConfigurationProvider(StoredOrDefaultConfigurationProviderTest.setupMockStorage(null));
        final ConfigurationFileManager subject = getTestJenkinsConfiguration(configurationProvider);
        subject.initConfiguration();

        // modify the extracted configuration
        assertTrue(defaultFile.delete());

        // running bootstrap again must not restore the file
        subject.initConfiguration();
        assertFalse(defaultFile.exists());
    }

    @Test
    public void testUnzipFileItem() throws Exception {
        final ConfigurationFileManager subject = getTestJenkinsConfiguration();

        final FileItem file = mock(FileItem.class);
        when(file.getInputStream()).thenReturn(getExampleInputStream());
        subject.unzipFilesFrom(file);
        assertTrue(new File(tempDir.getRoot(), "example.txt").exists());
    }

    @Test
    public void testDeleteFile() throws Exception {
        final ConfigurationFileManager subject = getTestJenkinsConfiguration();
        final File file = tempDir.newFile("aFile");
        subject.deleteFile(file.getName());
        assertFalse(file.exists());
    }

    @Test
    public void testDeleteDirectoryWithContents() throws Exception {
        final ConfigurationFileManager subject = getTestJenkinsConfiguration();

        final File directory = tempDir.newFolder("directory");
        new File(directory, "file").createNewFile();

        subject.deleteFile(directory.getName());
        assertFalse(directory.exists());
    }

    @Test
    public void testDownloadFromFileStorage() {
        final ConfigurationFileManager subject = getTestJenkinsConfiguration();
        assertNotNull(subject.downloadStoredConfiguration());
    }

    @Test
    public void testSaveNothing() throws Exception {
        final EntryNamesVerifyingStorage storage = new EntryNamesVerifyingStorage();
        final ConfigurationFileManager subject = getTestJenkinsConfiguration(storage);

        subject.saveConfiguration("", "");

        assertEquals(Collections.emptyList(), storage.names);
    }

    @Test
    public void testSaveOneFile() throws Exception {
        final EntryNamesVerifyingStorage storage = new EntryNamesVerifyingStorage();
        final ConfigurationFileManager subject = getTestJenkinsConfiguration(storage);

        tempDir.newFile("example.txt");
        subject.saveConfiguration("*.txt", "");

        assertEquals(Collections.singletonList("example.txt"), storage.names);
    }

    @Test
    public void testSaveExclusionPattern() throws Exception {
        final EntryNamesVerifyingStorage storage = new EntryNamesVerifyingStorage();
        final ConfigurationFileManager subject = getTestJenkinsConfiguration(storage);

        tempDir.newFile("example.txt");
        tempDir.newFile("example.exe");
        subject.saveConfiguration("*", "*.exe");

        assertEquals(Collections.singletonList("example.txt"), storage.names);
    }

    private ConfigurationFileManager getTestJenkinsConfiguration() {
        return getTestJenkinsConfiguration((ConfigurationProvider) null);
    }

    private ConfigurationFileManager getTestJenkinsConfiguration(final EntryNamesVerifyingStorage storage) {
        return getTestJenkinsConfiguration(new StoredOrDefaultConfigurationProvider(storage));
    }

    private ConfigurationFileManager getTestJenkinsConfiguration(final ConfigurationProvider configurationProvider) {
        if (configurationProvider == null) {
            return new ConfigurationFileManager(getTestConfigurationProvider(), tempDir.getRoot());
        } else {
            return new ConfigurationFileManager(configurationProvider, tempDir.getRoot());
        }
    }

    private static ConfigurationProvider getTestConfigurationProvider() {
        return new ConfigurationProvider() {

            @Override
            public InputStream getConfigurationStream() {
                return getExampleInputStream();
            }

            @Override
            public Storage getStorage() {
                return new EntryNamesVerifyingStorage();
            }
        };
    }

    private static InputStream getExampleInputStream() {
        return ConfigurationFileManagerTest.class.getResourceAsStream("/example.zip");
    }
}
