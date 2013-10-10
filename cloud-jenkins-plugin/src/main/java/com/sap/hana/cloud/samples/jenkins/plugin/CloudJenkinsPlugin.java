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

import hudson.Plugin;

public class CloudJenkinsPlugin extends Plugin {

    private static CloudJenkinsPlugin instance;

    private String includes =
        ".jenkins/*.xml,.jenkins/jobs/*/config.xml,.jenkins/plugins/*.jpi,.jenkins/secrets/**,.ssh/*,.m2/settings.xml";
    private String excludes = ".jenkins/plugins/cloud-jenkins.jpi";

    @Override
    public void start() throws Exception {
        load();
    }

    public CloudJenkinsPlugin() {
        instance = this;
    }

    public static CloudJenkinsPlugin getInstance() {
        return instance;
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(final String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(final String excludes) {
        this.excludes = excludes;
    }
}
