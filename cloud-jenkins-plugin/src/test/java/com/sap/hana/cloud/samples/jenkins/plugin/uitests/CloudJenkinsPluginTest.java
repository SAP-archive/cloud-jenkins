package com.sap.hana.cloud.samples.jenkins.plugin.uitests;

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

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Chmod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.PageFactory;

public class CloudJenkinsPluginTest {

    protected WebDriver webDriver;
    private CloudJenkinsConfigurationPage pluginConfigurationPage;
    private File configurationRoot;
    private File localStorage;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void init() throws Exception {
        /*
         * JenkinsRule starts a Jenkins in process, so we can change the root directory the plugin
         * works on via a system property.
         */
        configurationRoot = tempFolder.newFolder("configuration");
        System.setProperty("com.sap.hana.cloud.samples.jenkins.common.ConfigurationRootDirectory",
            configurationRoot.getAbsolutePath());

        localStorage = tempFolder.newFolder("localStorage");
        System.setProperty("com.sap.hana.cloud.samples.jenkins.storage.FileStorage", localStorage.getAbsolutePath());

        createJenkinsTestFiles();

        webDriver = createWebDriver();
        pluginConfigurationPage = openConfigurationPage(new URL(jenkins.getURL(), "manageInstallationOnCloud"));
    }

    @After
    public void tearDown() throws Exception {
        webDriver.quit();
    }

    @AfterClass
    public static void clearTestSettings() {
        System.clearProperty("com.sap.hana.cloud.samples.jenkins.common.ConfigurationRootDirectory");
        System.clearProperty("com.sap.hana.cloud.samples.jenkins.storage.FileStorage");
    }

    @Test
    public void testStore() throws Exception {
        final FilesToStorePage page = pluginConfigurationPage.storeConfiguration();
        assertThat(page.getListedFiles(), is(asSet(//
            ".m2/settings.xml",//
            ".jenkins/config.xml",//
            ".jenkins/jobs/job1/config.xml",//
            ".jenkins/jobs/job2/config.xml")));

        page.clickStore();
        assertTrue(new File(localStorage, "configuration.zip").isFile());
    }

    @Test
    public void testStoreWithCustomParameters() throws Exception {
        final FilesToStorePage page =
            pluginConfigurationPage.storeConfiguration("**", ".jenkins/*, .jenkins/jobs/job2/*");
        assertThat(page.getListedFiles(), is(asSet(//
            ".m2/settings.xml",//
            ".jenkins/jobs/job1/config.xml")));
    }

    @Test
    public void testDelete() {
        final FilesToDeletePage page = pluginConfigurationPage.deleteFiles(".jenkins");
        assertThat(page.getListedFiles(), is(asSet(//
            ".jenkins/",//
            ".jenkins/config.xml",//
            ".jenkins/jobs/",//
            ".jenkins/jobs/job1/",//
            ".jenkins/jobs/job1/config.xml",//
            ".jenkins/jobs/job2/",//
            ".jenkins/jobs/job2/config.xml")));

        page.clickDelete();
        final Collection<String> allConfigurationFiles = findAllFiles(configurationRoot);
        assertThat(allConfigurationFiles, not(hasItem(".jenkins/config.xml")));
        assertThat(allConfigurationFiles, hasItem(".m2/settings.xml"));
        assertThat(allConfigurationFiles.size(), is(1));
    }

    private void createJenkinsTestFiles() throws IOException {
        final File m2 = new File(configurationRoot, ".m2");
        final File jenkins = new File(configurationRoot, ".jenkins");
        final File job1 = new File(configurationRoot, ".jenkins/jobs/job1");
        final File job2 = new File(configurationRoot, ".jenkins/jobs/job2");
        m2.mkdirs();
        jenkins.mkdirs();
        job1.mkdirs();
        job2.mkdirs();

        final File m2Settings = new File(m2, "settings.xml");
        final File jenkinsConfig = new File(jenkins, "config.xml");
        final File job1Config = new File(job1, "config.xml");
        final File job2Config = new File(job2, "config.xml");
        m2Settings.createNewFile();
        jenkinsConfig.createNewFile();
        job1Config.createNewFile();
        job2Config.createNewFile();
    }

    private WebDriver createWebDriver() {
        final DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, preparePhantomJSExecutable());
        final WebDriver driver = new PhantomJSDriver(caps);
        return driver;
    }

    private String preparePhantomJSExecutable() {
        final File phantomJsExecutable =
            new File(System.getProperty("phantomjs.path", "./target/phantomjs/phantomjs.exe"));
        if (!phantomJsExecutable.getName().endsWith(".exe")) {
            final Chmod chmod = new Chmod();
            chmod.setProject(new Project());
            chmod.setFile(phantomJsExecutable);
            chmod.setPerm("ugo+rx");
            chmod.execute();
        }
        return phantomJsExecutable.getAbsolutePath();
    }

    private CloudJenkinsConfigurationPage openConfigurationPage(final URL url) {
        webDriver.navigate().to(url);
        return PageFactory.initElements(webDriver, CloudJenkinsConfigurationPage.class);
    }

    static Set<String> asSet(final String... values) {
        return new HashSet<String>(asList(values));
    }

    static Set<String> findAllFiles(final File root) {
        final Set<String> result = new HashSet<String>();
        for (final Object object : FileUtils.listFiles(root, FileFilterUtils.trueFileFilter(),
            FileFilterUtils.trueFileFilter())) {
            result.add(root.toURI().relativize(((File) object).toURI()).toString());
        }
        return result;
    }

}
