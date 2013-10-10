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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * Integration test name pattern is: IT*.java, *IT.java and *ITCase.java: So this gets executed in
 * the maven-failsafe-plugin.
 */
public class EcmStorageIT {

    public Map<String, String> getSessionParameters() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.REPOSITORY_ID, "A1");
        parameters.put(SessionParameter.USER, "dummyuser");
        parameters.put(SessionParameter.PASSWORD, "dummysecret");
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        parameters.put(SessionParameter.ATOMPUB_URL, "http://localhost:19080/opencmis/atom");

        return parameters;
    }

    @Test
    public void testSaveTwice() throws InterruptedException {
        final SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        final Session session = sessionFactory.createSession(getSessionParameters());
        final Storage storage = new EcmStorage(session);
        storage.save(new ByteArrayInputStream("hello".getBytes()));
        storage.save(new ByteArrayInputStream("world".getBytes()));
    }

    @Test
    public void testLoadSavedConfiguration() throws InterruptedException, IOException {
        final SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        final Session session = sessionFactory.createSession(getSessionParameters());
        final Storage storage = new EcmStorage(session);
        final byte[] inputBytes = "hello".getBytes();
        storage.save(new ByteArrayInputStream(inputBytes));
        assertArrayEquals(inputBytes, IOUtils.toByteArray(storage.load()));
    }
}
