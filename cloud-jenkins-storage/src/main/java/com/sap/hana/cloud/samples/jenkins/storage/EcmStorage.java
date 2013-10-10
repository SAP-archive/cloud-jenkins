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
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;

import com.sap.ecm.api.EcmService;
import com.sap.ecm.api.RepositoryOptions;
import com.sap.ecm.api.RepositoryOptions.Visibility;

public final class EcmStorage implements Storage {
    private static final String WORKSPACE_ZIP_NAME = "configuration.zip";
    private static final Map<String, String> CMIS_DOCUMENT_PROPERTIES = createCmisProperties();
    private final Session session;

    EcmStorage(final Session session) {
        this.session = session;
    }

    EcmStorage(final EcmService ecmService) {
        this(createSession(ecmService));
    }

    private static Session createSession(final EcmService service) {
        final String repositoryId = "com.sap.hana.cloud.samples.jenkins.storage.EcmStorage";
        /*
         * Cloud Jenkins is only intended to be used in a SAP HANA Cloud Platform developer account,
         * where there is anyway only one application running in the account. Also, the document
         * service's account isolation prevents access from other accounts, so we don't need to keep
         * the key private.
         */
        final String repositoryAccessKey = "public_within_same_account";

        try {
            return service.connect(repositoryId, repositoryAccessKey);
        } catch (final CmisObjectNotFoundException e) {
            createRepository(repositoryId, repositoryAccessKey, service);
            return service.connect(repositoryId, repositoryAccessKey);
        }
    }

    private static void createRepository(final String repositoryId, final String repositoryAccessKey,
            final EcmService ecmService) {
        final RepositoryOptions options = new RepositoryOptions();
        options.setUniqueName(repositoryId);
        options.setRepositoryKey(repositoryAccessKey);
        options.setVisibility(Visibility.PROTECTED);
        ecmService.createRepository(options);
    }

    @Override
    public void save(final InputStream stream) {
        if (stream == null) {
            throw new NullPointerException();
        }
        final ContentStream contentStream =
            session.getObjectFactory().createContentStream(WORKSPACE_ZIP_NAME, -1, "application/zip", stream);

        try {
            final Document existingDocument = (Document) session.getObjectByPath("/" + WORKSPACE_ZIP_NAME);
            existingDocument.setContentStream(contentStream, true);
        } catch (final CmisObjectNotFoundException e) {
            session.getRootFolder().createDocument(CMIS_DOCUMENT_PROPERTIES, contentStream, VersioningState.NONE);
        }
    }

    private static Map<String, String> createCmisProperties() {
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, WORKSPACE_ZIP_NAME);
        return properties;
    }

    @Override
    public InputStream load() {
        try {
            final Document existingDocument = (Document) session.getObjectByPath("/" + WORKSPACE_ZIP_NAME);
            return existingDocument.getContentStream().getStream();
        } catch (final CmisObjectNotFoundException e) {
            return null;
        }
    }
}
