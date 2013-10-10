package com.sap.hana.cloud.samples.jenkins.storage;

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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileStorageTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private Storage storage;

    @Before
    public void setUp() {
        storage = new FileStorage(tempDir.getRoot());
    }

    @Test(expected = NullPointerException.class)
    public void testEcmStoreNullFails() {
        final InputStream stream = null;
        storage.save(stream);
    }

    @Test
    public void testStoreStreamCreatesNewDocument() throws Exception {
        final ByteArrayInputStream input = new ByteArrayInputStream("hello".getBytes());
        storage.save(input);
        final File configFile = new File(tempDir.getRoot(), "configuration.zip");
        assertTrue(configFile.exists());
        assertArrayEquals("hello".getBytes(), FileUtils.readFileToByteArray(configFile));
    }

    @Test
    public void testStoreStreamUpdatesExistingDocumentAndDoesNotTryToCreateNewOne() throws Exception {
        final ByteArrayInputStream input = new ByteArrayInputStream("hello".getBytes());
        makeConfigurationExisting("huch".getBytes());
        storage.save(input);
        final File configFile = new File(tempDir.getRoot(), "configuration.zip");
        assertTrue(configFile.exists());
        assertArrayEquals("hello".getBytes(), FileUtils.readFileToByteArray(configFile));
    }

    @Test
    public void testLoadConfigurationReturnsNullWhenStoreIsEmpty() throws Exception {
        assertNull(storage.load());
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        makeConfigurationExisting("test".getBytes());
        assertArrayEquals("test".getBytes(), IOUtils.toByteArray(storage.load()));
    }

    private void makeConfigurationExisting(final byte[] data) throws IOException {
        final File configFile = tempDir.newFile("configuration.zip");
        FileUtils.writeByteArrayToFile(configFile, data);
    }
}
