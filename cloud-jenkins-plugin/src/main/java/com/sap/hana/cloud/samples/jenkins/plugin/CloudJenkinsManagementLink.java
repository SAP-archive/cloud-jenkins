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

import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.model.Hudson;
import hudson.util.DirScanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

import javax.servlet.ServletException;

import jenkins.model.Jenkins;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.sap.hana.cloud.samples.jenkins.common.ConfigurationFileManager;

@Extension
public class CloudJenkinsManagementLink extends ManagementLink {
    private static final String URL_NAME = "manageInstallationOnCloud";
    private static final String TITLE = "Manage Jenkins Installation on Cloud";

    private final ConfigurationFileManager configurationFileManager = new ConfigurationFileManager();
    private String deleteFilePath;

    @Override
    public String getDisplayName() {
        return TITLE;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/cloud-jenkins-plugin/images/cloud.png";
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    @Override
    public String getDescription() {
        return "Save/restore Jenkins configuration in SAP HANA Cloud document service. Upload/remove files on disk.";
    }

    public CloudJenkinsPlugin getPlugin() {
        return CloudJenkinsPlugin.getInstance();
    }

    public void doUpload(final StaplerRequest request, final StaplerResponse response) throws IOException,
            ServletException {
        String nextPage = "";
        final FileItem configArchive = request.getFileItem("jenkinsConfiguration.zip");
        try {
            configurationFileManager.unzipFilesFrom(configArchive);
        } catch (final ZipException e) {
            nextPage = "/failedUploadArchivePage";
        }
        execute(request, response, nextPage);
    }

    public void doDownload(final StaplerRequest request, final StaplerResponse response) throws IOException,
            ServletException {
        execute(request, response, "/failedDownloadConfiguration", new IORunnable() {
            @Override
            public void run() throws IOException, ServletException {
                final InputStream is = configurationFileManager.downloadStoredConfiguration();
                if (is != null) {
                    try {
                        response.addHeader("Content-Disposition", "attachment; filename=jenkinsConfiguration.zip");
                        response.serveFile(request, is, 0l, 0l, -1l, "jenkinsConfiguration.zip");
                    } finally {
                        is.close();
                    }
                }
            }
        });
    }

    public void doListFilesToDelete(final StaplerRequest request, final StaplerResponse response) throws IOException,
            ServletException {
        deleteFilePath = getRequestParameter(request, "path");
        String nextPage;
        if (StringUtils.isBlank(deleteFilePath)) {
            nextPage = "/missingDeletionPathPage";
        } else {
            nextPage = "/confirmDeletePage";
        }
        execute(request, response, nextPage);
    }

    public void doConfirmDelete(final StaplerRequest request, final StaplerResponse response) throws IOException,
            ServletException {
        execute(request, response, "", new IORunnable() {
            @Override
            public void run() throws IOException, ServletException {
                configurationFileManager.deleteFile(deleteFilePath);
            }
        });
    }

    public void doListFilesToSave(final StaplerRequest request, final StaplerResponse response) throws IOException,
            ServletException {
        execute(request, response, "/confirmSavePage", new IORunnable() {
            @Override
            public void run() throws IOException, ServletException {
                final CloudJenkinsPlugin plugin = getPlugin();
                plugin.setIncludes(getRequestParameter(request, "includes"));
                plugin.setExcludes(getRequestParameter(request, "excludes"));
                plugin.save();
            }
        });
    }

    public void doConfirmSave(final StaplerRequest request, final StaplerResponse response) throws IOException,
            ServletException {
        execute(request, response, "", new IORunnable() {
            @Override
            public void run() throws IOException, ServletException {
                final CloudJenkinsPlugin plugin = getPlugin();
                configurationFileManager.saveConfiguration(plugin.getIncludes(), plugin.getExcludes());
            }
        });
    }

    public void doRestore(final StaplerRequest request, final StaplerResponse response) throws IOException,
            ServletException {
        execute(request, response, "", new IORunnable() {
            @Override
            public void run() throws IOException, ServletException {
                configurationFileManager.restoreConfiguration();
                Hudson.getInstance().doReload();
            }
        });
    }

    public String getFilesToDeleteSummary() throws IOException {
        final File fileToDelete = new File(configurationFileManager.getRootDirectory(), deleteFilePath);
        final ListFilesVisitor visitor = new ListFilesVisitor();
        if (StringUtils.isNotBlank(deleteFilePath)) {
            new DirScanner.Full().scan(fileToDelete, visitor);
            return addParentPathtoDisplay(fileToDelete, visitor.filesToString());
        }
        return visitor.filesToString();
    }

    /*
     * The directory scanner does not append the parent path to the files scanned. So this method
     * adds the parent path to the visited files. This method will later to be deleted once the
     * "delete" functionality supports file patterns.
     */
    private String addParentPathtoDisplay(final File fileToDelete, final String scannedFiles) {
        final StringBuilder finalFilesScanned = new StringBuilder();
        if (StringUtils.isNotBlank(scannedFiles)) {
            final String[] scannedDirs = scannedFiles.split("\n");
            final String parent = fileToDelete.getParentFile().getName();

            for (String child : scannedDirs) {
                if (!configurationFileManager.getRootDirectory().getName().equals(parent)) {
                    child = parent + "/" + child;
                }
                finalFilesScanned.append(child + "\n");
            }
        }
        return finalFilesScanned.toString();
    }

    public String getFilesToSaveSummary() throws IOException {
        final CloudJenkinsPlugin plugin = getPlugin();
        final ListFilesVisitor visitor = new ListFilesVisitor();
        new DirScanner.Glob(plugin.getIncludes(), plugin.getExcludes()).scan(
            configurationFileManager.getRootDirectory(), visitor);
        return visitor.filesToString();
    }

    private static String getRequestParameter(final StaplerRequest request, final String key) throws ServletException {
        return request.getSubmittedForm().getString(key);
    }

    private void execute(final StaplerRequest request, final StaplerResponse response, final String nextPage,
            final IORunnable runnable) throws IOException, ServletException {
        Hudson.getInstance().checkPermission(Jenkins.ADMINISTER);
        runnable.run();
        if (!response.isCommitted()) {
            response.sendRedirect(request.getContextPath() + "/" + getUrlName() + nextPage);
        }
    }

    private void execute(final StaplerRequest request, final StaplerResponse response, final String nextPage)
            throws IOException, ServletException {
        Hudson.getInstance().checkPermission(Jenkins.ADMINISTER);
        response.sendRedirect(request.getContextPath() + "/" + getUrlName() + nextPage);
    }
}
