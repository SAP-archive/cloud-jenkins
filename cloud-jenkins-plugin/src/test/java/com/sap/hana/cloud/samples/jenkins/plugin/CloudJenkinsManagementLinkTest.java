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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CloudJenkinsManagementLinkTest {

    private File configurationRoot;
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setup() throws Exception {
        configurationRoot = tempFolder.newFolder("configuration");
        System.setProperty("com.sap.hana.cloud.samples.jenkins.common.ConfigurationRootDirectory",
            configurationRoot.getAbsolutePath());
    }

    @Test
    public void testGetModifiedFileListToDeleteWithInputAsFolder() {
        //Showing files to delete when .jenkins/updates is given as input
        final CloudJenkinsManagementLink cloudJenkinsMgmtLink = new CloudJenkinsManagementLink();
        final String scannedFiles = "updates/\nupdates/tmp1.txt\nupdates/tmp2.txt\n";
        final String expectedResult = ".jenkins/updates/\n.jenkins/updates/tmp1.txt\n.jenkins/updates/tmp2.txt\n";

        final String actualResult = cloudJenkinsMgmtLink.getModifiedFileListToDelete(".jenkins", scannedFiles);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetModifiedFileListToDeleteWithInputAsFile() {
        //Showing files to delete when .jenkins/config.xml is given as input
        final CloudJenkinsManagementLink cloudJenkinsMgmtLink = new CloudJenkinsManagementLink();
        final String scannedFiles = "config.xml\n";
        final String expectedResult = ".jenkins/config.xml\n";

        final String actualResult = cloudJenkinsMgmtLink.getModifiedFileListToDelete(".jenkins", scannedFiles);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetModifiedFileListToDeleteWithInputAsFileInHomeDir() {
        //Showing files to delete when tmp.txt is under the home directory 
        final CloudJenkinsManagementLink cloudJenkinsMgmtLink = new CloudJenkinsManagementLink();
        final String scannedFiles = "tmp.txt\n";
        final String expectedResult = scannedFiles;

        final String actualResult =
            cloudJenkinsMgmtLink.getModifiedFileListToDelete(configurationRoot.getName(), scannedFiles);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetModifiedFileListToDeleteWithInputAsFolderInHomeDir() {
        //Showing files to delete when .jenkins is given as input
        final CloudJenkinsManagementLink cloudJenkinsMgmtLink = new CloudJenkinsManagementLink();
        final String scannedFiles =
            ".jenkins/\n.jenkins/config.xml\n.jenkins/jobs/\n.jenkins/jobs/install-git/\n.jenkins/jobs/install-git/config.xml\n";
        final String expectedResult = scannedFiles;

        final String actualResult =
            cloudJenkinsMgmtLink.getModifiedFileListToDelete(configurationRoot.getName(), scannedFiles);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetModifiedFileListToDeleteWithInputAsFolderNameSameAsHomeDir() {
        // Showing files to delete when the input Folder has same name as HomeDir 
        final CloudJenkinsManagementLink cloudJenkinsMgmtLink = new CloudJenkinsManagementLink();
        final String rootFolderName = configurationRoot.getName();
        final String scannedFiles = rootFolderName + "/\n" + rootFolderName + "/tmp.txt\n";
        final String expectedResult = scannedFiles;

        final String actualResult = cloudJenkinsMgmtLink.getModifiedFileListToDelete(rootFolderName, scannedFiles);
        assertEquals(expectedResult, actualResult);
    }

    @AfterClass
    public static void clearTestSettings() {
        System.clearProperty("com.sap.hana.cloud.samples.jenkins.common.ConfigurationRootDirectory");
    }
}