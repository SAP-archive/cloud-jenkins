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

import java.io.InputStream;

public interface Storage {

    /**
     * Stores the contents of the given InputStream as a document in the ECM repository. The path to
     * that document is "/configuration.zip", the document will be created if it didn't exist
     * before.
     * 
     * @param stream
     *            Supplies the contents to be stored. Must not be <code>null</code>.
     */
    public abstract void save(InputStream stream);

    /**
     * Retrieves previously stored configuration from the ECM repository as an InputStream.
     * 
     * @return An InputStream with the previously stored configuration. <code>null</code>, if no
     *         configuration has been stored before.
     */
    public abstract InputStream load();

}