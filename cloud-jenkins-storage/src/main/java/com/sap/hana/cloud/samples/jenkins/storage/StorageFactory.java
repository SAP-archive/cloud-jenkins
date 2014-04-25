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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.ecm.api.EcmService;

public class StorageFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(StorageFactory.class);

    public static Storage createStorage() {
        LOGGER.debug("createStorage");
        try {
            final EcmService ecmService = (EcmService) new InitialContext().lookup("java:comp/env/EcmService");
            LOGGER.debug("createStorage: using ECMStorage");
            return new EcmStorage(ecmService);
        } catch (final NamingException e) {
            LOGGER.error("createStorage: failed to lookup EcmService, using FileStorage instead", e);
            return new FileStorage();
        }
    }
}
