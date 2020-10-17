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

import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link StorageRepository} implementation using {@link File}.
 *
 * @author Michael Vorburger.ch
 */
public class FileStorageRepository implements StorageRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FileStorageRepository.class);

    private final String base = FileSystemContentRepository.FINERACT_BASE_DIR + File.separator;

    private File file(String namespace, String path) {
        return new File(base + namespace, path);
    }

    @Override
    public void write(String namespace, String path, InputStream is) throws ContentManagementException {
        try {
            File file = file(namespace, path);
            Files.createParentDirs(file);
            FileUtils.copyInputStreamToFile(is, file);
        } catch (final IOException e) {
            LOG.warn("write() IOException (logged because cause is not propagated in ContentManagementException)", e);
            throw new ContentManagementException(path, e.getMessage(), e);
        }
    }

    @Override
    public InputStream read(String namespace, String path) {
        try {
            return new FileInputStream(file(namespace, path));
        } catch (FileNotFoundException e) {
            LOG.warn("read() FileNotFoundException (logged because cause is not propagated in ContentManagementException)", e);
            throw new ContentManagementException(path, e.getMessage(), e);
        }
    }

    @Override
    public void delete(String namespace, String path) {
        try {
            java.nio.file.Files.delete(file(namespace, path).toPath());
        } catch (IOException e) {
            LOG.warn("delete() IOException (logged because cause is not propagated in ContentManagementException)", e);
            throw new ContentManagementException(path, e.getMessage(), e);
        }
    }
}
