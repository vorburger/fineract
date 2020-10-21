/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.integrationtests.newstyle;

import java.io.File;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.ResponseBody;
import org.apache.fineract.client.models.GetEntityTypeEntityIdDocumentsResponse;
import org.apache.fineract.client.models.PostEntityTypeEntityIdDocumentsResponse;
import org.apache.fineract.client.util.Parts;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /documents API.
 *
 * @author Michael Vorburger.ch
 */
public class DocumentTest extends IntegrationTest {

    final File testFile = new File(getClass().getResource("/michael.vorburger-crepes.jpg").getFile());

    Long clientId = new ClientTest().getClientId();
    Long documentId;

    @Test
    @Order(1)
    void retrieveAllDocuments() throws IOException {
        assertThat(ok(fineract().documents.retreiveAllDocuments("clients", clientId))).isNotNull();
    }

    @Test
    @Order(2)
    void createDocument() throws IOException {
        String name = "Test";
        Part part = Parts.fromFile(testFile);
        String description = null;
        // TODO used var in tests when moved from fineract-client (Java 8 only) to new module
        PostEntityTypeEntityIdDocumentsResponse response = ok(
                fineract().documents.createDocument("clients", clientId, part, name, description));
        assertThat(response.getResourceId()).isNotNull();
        assertThat(response.getResourceIdentifier()).isNotEmpty();
        documentId = response.getResourceId();
    }

    @Test
    @Order(3)
    void getDocument() throws IOException {
        GetEntityTypeEntityIdDocumentsResponse doc = ok(fineract().documents.getDocument("clients", clientId, documentId));
        assertThat(doc.getName()).isEqualTo("Test");
        assertThat(doc.getFileName()).isEqualTo(testFile.getName());
        assertThat(doc.getDescription()).isNull();
        assertThat(doc.getId()).isEqualTo(documentId);
        assertThat(doc.getParentEntityType()).isEqualTo("clients");
        assertThat(doc.getParentEntityId()).isEqualTo(clientId);
        // TODO huh?! It's more than uploaded file; seems like a bug - it's including create body, not just file size
        assertThat(doc.getSize()).isEqualTo(testFile.length() + 385);
        // TODO huh?! MIME is always text/plain instead of image/jpeg... :(
        assertThat(doc.getType()).isEqualTo("text/plain");
        // TODO doc.getStorageType() shouldn't be exposed by the API?!
    }

    @Test
    @Order(4)
    void downloadFile() throws IOException {
        ResponseBody r = ok(fineract().documents.downloadFile("clients", clientId, documentId));
        assertThat(r.contentType()).isEqualTo(MediaType.get("text/plain")); // TODO wrong, bug; needs to be "image/jpeg"
                                                                            // (as above)
        assertThat(r.bytes().length).isEqualTo(testFile.length());
        // NOK: assertThat(r.contentLength()).isEqualTo(testFile.length());
    }

    @Test
    @Order(10)
    void updateDocument() throws IOException {
        String newName = "Test changed name";
        String newDescription = getClass().getName();
        ok(fineract().documents.updateDocument("clients", clientId, documentId, null, newName, newDescription));

        GetEntityTypeEntityIdDocumentsResponse doc = ok(fineract().documents.getDocument("clients", clientId, documentId));
        assertThat(doc.getName()).isEqualTo(newName);
        assertThat(doc.getDescription()).isEqualTo(newDescription);
    }

    @Test
    @Order(99)
    void deleteDocument() throws IOException {
        ok(fineract().documents.deleteDocument("clients", clientId, documentId));
    }

    @Order(9999)
    @Test // FINERACT-1036
    void createDocumentBadArgs() throws IOException {
        assertThat(fineract().documents.createDocument("clients", 123L, null, "test.pdf", null)).hasHttpStatus(400);
    }
}
