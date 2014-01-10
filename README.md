# SAP HANA Cloud Platform Samples - Cloud Jenkins

The Cloud Jenkins sample project builds a web archive for running Jenkins in a developer account of the SAP HANA Cloud Platform.

There is no persistent file system available in SAP HANA Cloud Platform developer accounts.
The Cloud Jenkins overcomes this limitation by storing the configuration in the SAP HANA Cloud Platform document service.

## Prerequisites

1. [Apache Maven](http://maven.apache.org/) 3.0.4 or newer

2. A [Maven settings.xml](http://maven.apache.org/settings.html) which allows access to the Maven repository of the Jenkins project.
   To configure this, you can e.g. insert the following configuration into your settings.xml file:

    ```
    <profiles>
        <profile>
            <id>default-repositories</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <repositories>
                <repository>
                    <id>central</id>
                    <url>http://repo.maven.apache.org/maven2/</url>
                </repository>
                <repository>
                    <id>repo.jenkins-ci.org</id>
                    <url>http://repo.jenkins-ci.org/public/</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>central</id>
                    <url>http://repo.maven.apache.org/maven2/</url>
                </pluginRepository>
                <pluginRepository>
                    <id>repo.jenkins-ci.org</id>
                    <url>http://repo.jenkins-ci.org/public/</url>
                </pluginRepository>
            </pluginRepositories>
        </profile>
    </profiles>
    ```

3. A [SAP HANA Cloud Platform developer account](https://help.hana.ondemand.com/help/frameset.htm?65d74d39cb3a4bf8910cd36ec54d2b99.html)

## Quick start

1. Clone the project from Github:

    ```
	git clone https://github.com/sap/cloud-jenkins.git;
	cd cloud-jenkins
	```

2. Build the project

	```
    mvn clean install
    ```

3. Deploy the web archive to your SAP HANA Cloud Platform developer account:
   If you are behind a proxy you have to set the following environment variables before you execute the neo command.
	 
	    export http_proxy=http://<HTTP proxy hostname>:<HTTP proxy port>
	    export https_proxy=https://<HTTPS proxy hostname>:<HTTPS proxy port> 
	    export no_proxy="localhost"  

	  ```
	  <path to neo tool> deploy --host hanatrial.ondemand.com --account <your developer account> --application jenkins --user <your user ID> --source cloud-jenkins-webarchive/target/ROOT.war
	  ```
    The neo tool (`neo.sh` or `neo.bat`) is part of the SAP HANA Cloud SDK and located in the `tools` folder.

4. Configure the permissions for the Cloud Jenkins deployment:
	 - Go to the [SAP HANA Cloud Platform cockpit](https://account.hanatrial.ondemand.com/cockpit/)
	 - On the "Authorizations" tab, enter your user ID and click on "Show Roles"
	 - Assign your user to the "admin" role of the "jenkins" application.

5. Start the "jenkins" application on the "Applications" tab. The application status page also shows the URL of the application.
           
### Result

As a result, you get:

- A running Jenkins instance with
- A build job "install-git" which is automatically triggered on Jenkins startup and installs Git, and
- A plugin installed that provides the "Manage Jenkins Installation on Cloud" link under "Manage Jenkins"

In the "Manage Jenkins Installation on Cloud" UI, you can:

- Upload files to Jenkins
- Delete files from Jenkins
- Store the Jenkins configuration in the SAP HANA Cloud document service so that it survives a restart

### Next steps

As next steps, you may want to
- Add a new build job on your Jenkins instance.
  Don't forget to also store the configuration in the SAP HANA Cloud document service ("Manage Jenkins" > "Manage Jenkins Installation on Cloud") so that the new job is still available after a restart.
- Read the this [blog article](http://scn.sap.com/community/developer-center/cloud-platform/blog/2013/10/11/run-your-own-jenkins-on-sap-hana-cloud-platform)

## Project Overview

The project consists of the following modules:

1. *cloud-jenkins-bootstrap*: This module contains the bootstrapping logic which restores the Jenkins configuratation from the SAP HANA Cloud document service before Jenkins is started.
2. *cloud-jenkins-storage*: This module contains the logic to store the Jenkins configuration in the SAP HANA Cloud document service.
3. *cloud-jenkins-defaults*: This module contains the configuration which is used on first startup of the Cloud Jenkins.
4. *cloud-jenkins-plugin*: This module contains the Cloud Jenkins plug-in. It provides the "Manage Cloud Jenkins Configuration" user interface.
5. *cloud-jenkins-webarchive*: This module builds the Jenkins web archive that is ready to use in a SAP HANA Cloud Platform developer account.

## Copyright and license

Copyright 2013, 2014 SAP AG

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

See also the file LICENSE.
