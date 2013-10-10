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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EcmStorageTest {
    private Storage storage;
    private Session session;
    private Folder root;

    @Before
    public void setUp() {
        session = Mockito.mock(Session.class);
        storage = new EcmStorage(session);
        root = Mockito.mock(Folder.class);
        when(session.getRootFolder()).thenReturn(root);
        when(session.getObjectFactory()).thenReturn(Mockito.mock(ObjectFactory.class));
        when(root.createDocument(stringMap(), any(ContentStream.class), eq(VersioningState.NONE))).thenReturn(
            Mockito.mock(Document.class));
    }

    @Test(expected = NullPointerException.class)
    public void testEcmStoreNullFails() {
        final InputStream stream = null;
        storage.save(stream);
    }

    @Test
    public void testStoreStreamCreatesNewDocument() throws Exception {
        final ByteArrayInputStream input = new ByteArrayInputStream("hello".getBytes());
        makeConfigurationNotExisting();
        storage.save(input);
        final Map<String, String> properties = new HashMap<String, String>();
        properties.put("cmis:objectTypeId", "cmis:document");
        properties.put("cmis:name", "configuration.zip");
        verify(root).createDocument(eq(properties), any(ContentStream.class), eq(VersioningState.NONE));
    }

    @Test
    public void testStoreStreamUpdatesExistingDocumentAndDoesNotTryToCreateNewOne() throws Exception {
        final ByteArrayInputStream input = new ByteArrayInputStream("hello".getBytes());
        final Document cmisObject = Mockito.mock(Document.class);
        makeConfigurationExisting(cmisObject);
        storage.save(input);
        verify(cmisObject).setContentStream(any(ContentStream.class), eq(true));
        verify(root, never()).createDocument(stringMap(), any(ContentStream.class), any(VersioningState.class));
    }

    @Test
    public void testLoadConfigurationReturnsNullWhenStoreIsEmpty() throws Exception {
        makeConfigurationNotExisting();
        assertNull(storage.load());
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        final Document cmisObject = Mockito.mock(Document.class);
        final ContentStream contentStream = mock(ContentStream.class);
        when(cmisObject.getContentStream()).thenReturn(contentStream);
        when(contentStream.getStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        makeConfigurationExisting(cmisObject);
        assertArrayEquals("test".getBytes(), IOUtils.toByteArray(storage.load()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> stringMap() {
        return anyMap();
    }

    private void makeConfigurationExisting(final Document cmisObject) {
        when(session.getObjectByPath("/configuration.zip")).thenReturn(cmisObject);
    }

    private void makeConfigurationNotExisting() {
        when(session.getObjectByPath("/configuration.zip")).thenThrow(new CmisObjectNotFoundException());
    }
}
