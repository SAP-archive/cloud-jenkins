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

import hudson.FilePath;
import hudson.util.DirScanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;

import com.gc.iotools.stream.is.InputStreamFromOutputStream;
import com.sap.hana.cloud.samples.jenkins.storage.StorageFactory;

public class ConfigurationFileManager {

    private static final String MARKER_FILE_NAME = ".cloud-jenkins-bootstrap-marker";

    private final File rootDirectoryAsFile;
    private final FilePath rootDirectory;

    private final ConfigurationProvider configurationProvider;

    public ConfigurationFileManager() {
        this(new StoredOrDefaultConfigurationProvider(StorageFactory.createStorage()),
                ConfigurationRootDirectory.getDirectory());
    }

    ConfigurationFileManager(final ConfigurationProvider configurationProvider, final File rootDirectory) {
        this.configurationProvider = configurationProvider;
        this.rootDirectoryAsFile = rootDirectory;
        this.rootDirectory = new FilePath(rootDirectory);
    }

    /**
     * Extracts the configuration files from the given source, if there is currently no
     * configuration at all.
     */
    public void initConfiguration() {
        if (new File(rootDirectoryAsFile, MARKER_FILE_NAME).exists()) {
            return;
        }
        restoreConfiguration();
    }

    public void restoreConfiguration() {
        try {
            new File(rootDirectoryAsFile, MARKER_FILE_NAME).createNewFile();
            try {
                final InputStream inputStream = configurationProvider.getConfigurationStream();
                try {
                    rootDirectory.unzipFrom(inputStream);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void unzipFilesFrom(final FileItem file) throws IOException {
        final InputStream inputStream = file.getInputStream();
        try {
            try {
                rootDirectory.unzipFrom(inputStream);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } finally {
            inputStream.close();
        }
    }

    public void deleteFile(final String path) throws IOException {
        try {
            new FilePath(rootDirectory, path).deleteRecursive();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void saveConfiguration(final String includes, final String excludes) {
        final InputStreamFromOutputStream<Object> input = new InputStreamFromOutputStream<Object>() {
            @Override
            protected Object produce(final OutputStream output) throws Exception {
                final DirScanner scanner = new DirScanner.Glob(includes, excludes);
                rootDirectory.zip(output, scanner);
                return null;
            }
        };
        configurationProvider.getStorage().save(input);
    }

    public InputStream downloadStoredConfiguration() {
        return configurationProvider.getStorage().load();
    }

    /**
     * Returns the base directory for all configuration files. All file paths are relative to to
     * this directory.
     */
    public File getRootDirectory() {
        return rootDirectoryAsFile;
    }

}
