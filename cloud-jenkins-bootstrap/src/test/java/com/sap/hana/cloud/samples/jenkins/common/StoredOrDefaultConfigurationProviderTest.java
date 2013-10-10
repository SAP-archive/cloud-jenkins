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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.sap.hana.cloud.samples.jenkins.storage.Storage;

public class StoredOrDefaultConfigurationProviderTest {

    @Test
    public void testEmptyRepositoryProvidesDefaultConfiguration() throws IOException {
        final Storage storage = setupMockStorage(null);
        final InputStream configuration = new StoredOrDefaultConfigurationProvider(storage).getConfigurationStream();
        final InputStream expectedStream = getClass().getResourceAsStream("cloud-jenkins-defaults-assembly.zip");

        assertArrayEquals(IOUtils.toByteArray(expectedStream), IOUtils.toByteArray(configuration));
    }

    @Test
    public void testGetConfigurationFromECM() throws IOException {
        final InputStream expectedInputStream = mock(InputStream.class);
        final Storage storage = setupMockStorage(expectedInputStream);
        final InputStream configuration = new StoredOrDefaultConfigurationProvider(storage).getConfigurationStream();

        assertSame(expectedInputStream, configuration);
    }

    static Storage setupMockStorage(final InputStream expectedInputStream) {
        final Storage storage = mock(Storage.class);
        when(storage.load()).thenReturn(expectedInputStream);
        return storage;
    }
}
