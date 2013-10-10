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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class FileStorage implements Storage {

    private final File configFile;

    public FileStorage() {
        this(getLocalStorageDirectory());
    }

    FileStorage(final File localStorageDirectory) {
        configFile = new File(localStorageDirectory, "configuration.zip");
    }

    private static File getLocalStorageDirectory() {
        final String explicitValue = System.getProperty("com.sap.hana.cloud.samples.jenkins.storage.FileStorage");
        if (!StringUtils.isEmpty(explicitValue)) {
            return new File(explicitValue);
        } else {
            // use the processes working directory
            return new File(".");
        }
    }

    @Override
    public void save(final InputStream stream) {
        FileOutputStream fileOutput;
        try {
            fileOutput = new FileOutputStream(configFile);
            try {
                IOUtils.copy(stream, fileOutput);
            } finally {
                fileOutput.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream load() {
        try {
            if (configFile.exists()) {
                return new FileInputStream(configFile);
            }
            return null;
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
