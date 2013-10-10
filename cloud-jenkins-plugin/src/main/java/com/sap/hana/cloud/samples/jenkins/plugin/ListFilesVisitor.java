package com.sap.hana.cloud.samples.jenkins.plugin;

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

import hudson.util.FileVisitor;

import java.io.File;
import java.io.IOException;

final class ListFilesVisitor extends FileVisitor {
    private final StringBuilder fileList = new StringBuilder();

    @Override
    public void visit(final File f, final String relativePath) throws IOException {
        String osIndependentPath = relativePath.replace(File.separatorChar, '/');
        if (f.isDirectory()) {
            fileList.append(osIndependentPath + "/");
        } else {
            fileList.append(osIndependentPath);
        }
        fileList.append("\n");
    }

    String filesToString() {
        return fileList.toString();
    }
}