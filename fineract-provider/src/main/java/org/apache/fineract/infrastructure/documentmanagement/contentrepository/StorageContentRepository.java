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
package org.apache.fineract.infrastructure.documentmanagement.contentrepository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Base64;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;


/**
 * A {@link ContentRepository} implementation which delegates to a {@link StorageRepository}.
 *
 * @author Michael Vorburger.ch
 */
public class StorageContentRepository implements ContentRepository {

    private final StorageRepository storageRepository;
    private final StorageType storageType;

    public StorageContentRepository(StorageRepository storageRepository, StorageType storageType) {
        this.storageRepository = storageRepository;
        this.storageType = storageType;
    }

    @Override
    public String saveFile(InputStream uploadedInputStream, DocumentCommand documentCommand) {
        final String fileName = documentCommand.getFileName();
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(documentCommand.getSize(), fileName);
        String path = "documents" + File.separator + documentCommand.getParentEntityType() + File.separator
                + documentCommand.getParentEntityId() + File.separator + ContentRepositoryUtils.generateRandomString();
        storageRepository.write(getNS(), path, uploadedInputStream);
        // TODO Test if OK that this now omits Base dir +  Tenant which previously was saved? Feature, or Bug?
        return path;
    }

    @Override
    public String saveImage(InputStream uploadedInputStream, Long resourceId, String imageName, Long fileSize) {
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(fileSize, imageName);
        String path = "images" + File.separator + "clients" + File.separator + resourceId + File.separator + imageName;
        storageRepository.write(getNS(), path, uploadedInputStream);
        // TODO test as above...
        return path;
    }

    @Override
    public String saveImage(Base64EncodedImage base64EncodedImage, Long resourceId, String imageName) {
        byte[] image = Base64.getMimeDecoder().decode(base64EncodedImage.getBase64EncodedString());
        return saveImage(new ByteArrayInputStream(image), resourceId, imageName + base64EncodedImage.getFileExtension(), (long)image.length);
    }

    @Override
    public FileData fetchFile(DocumentData documentData) {
        return new FileData(storageRepository.read(getNS(), documentData.fileLocation()), documentData.fileName(), documentData.contentType());
    }

    @Override
    public ImageData fetchImage(ImageData imageData) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteFile(String documentPath) {
        storageRepository.delete(getNS(), documentPath);
    }

    @Override
    public void deleteImage(String location) {
        storageRepository.delete(getNS(), location);
    }

    @Override
    public StorageType getStorageType() {
        return storageType;
    }

    private String getNS() {
        return ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim();
    }
}
