# SAP HANA Cloud Platform Samples - Cloud Jenkins

The Cloud Jenkins sample project builds a web archive for running Jenkins in a developer account of the SAP HANA Cloud Platform.

There is no persistent file system available in SAP HANA Cloud Platform developer accounts.
The Cloud Jenkins overcomes this limitation by storing the configuration in the SAP HANA Cloud Platform document service.

## Prerequisites

1. [Apache Maven](http://maven.apache.org/) 3.0.4 or newer
2. A [SAP HANA Cloud Platform developer account](https://help.hana.ondemand.com/help/frameset.htm?65d74d39cb3a4bf8910cd36ec54d2b99.html)
3. The [SAP HANA Cloud SDK 1.34.25.3](https://tools.hana.ondemand.com/sdk/neo-sdk-javaweb-1.34.25.3.zip).
   Download the SDK and extract it to a folder.
   Go to that folder in a console and enter the following command:
   ```
   mvn install:install-file -Dfile=api/neo-sdk-core-api-1.34.25.3.jar -DartifactId=neo-sdk-core-api -DgroupId=com.sap.hana.cloud -Dversion=1.34.25.3 -Dpackaging=jar
   ```
   This makes the Java API of the of the SAP HANA Cloud Platform available in the Maven build.


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
- Read the this [blog article](TODO)

## Project Overview

The project consists of the following modules:

1. *cloud-jenkins-bootstrap*: This module contains the bootstrapping logic which restores the Jenkins configuratation from the SAP HANA Cloud document service before Jenkins is started.
2. *cloud-jenkins-storage*: This module contains the logic to store the Jenkins configuration in the SAP HANA Cloud document service.
3. *cloud-jenkins-defaults*: This module contains the configuration which is used on first startup of the Cloud Jenkins.
4. *cloud-jenkins-plugin*: This module contains the Cloud Jenkins plug-in. It provides the "Manage Cloud Jenkins Configuration" user interface.
5. *cloud-jenkins-webarchive*: This module builds the Jenkins web archive that is ready to use in a SAP HANA Cloud Platform developer account.

## Copyright and license

Copyright 2013 SAP AG

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
