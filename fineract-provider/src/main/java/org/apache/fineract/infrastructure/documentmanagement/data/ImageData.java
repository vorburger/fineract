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
package org.apache.fineract.infrastructure.documentmanagement.data;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageData {

    private static final Logger LOG = LoggerFactory.getLogger(ImageData.class);

    private final String location;
    private final StorageType storageType;
    private final String entityDisplayName;

    private File file;
    private ContentRepositoryUtils.ImageFileExtension fileExtension;
    private InputStream inputStream;

    public ImageData(final String location, final StorageType storageType, final String entityDisplayName) {
        this.location = location;
        this.storageType = storageType;
        this.entityDisplayName = entityDisplayName;
    }

    private byte[] getContent() {
        try {
            if (this.storageType.equals(StorageType.S3) && this.inputStream != null) {
                return IOUtils.toByteArray(this.inputStream);
            } else if (this.storageType.equals(StorageType.FILE_SYSTEM) && this.file != null) {
                final FileInputStream fileInputStream = new FileInputStream(this.file);
                return IOUtils.toByteArray(fileInputStream);
            }
        } catch (IOException e) {
            LOG.error("Error occured.", e);
        }
        return null;
    }

    private byte[] resizeImage(InputStream in, int maxWidth, int maxHeight) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resizeImage(in, out, maxWidth, maxHeight);
        return out.toByteArray();
    }

    private void resizeImage(InputStream in, OutputStream out, int maxWidth, int maxHeight) throws IOException {
        BufferedImage src = ImageIO.read(in);
        if (src.getWidth() <= maxWidth && src.getHeight() <= maxHeight) {
            out.write(getContent());
            return;
        }
        float widthRatio = (float) src.getWidth() / maxWidth;
        float heightRatio = (float) src.getHeight() / maxHeight;
        float scaleRatio = widthRatio > heightRatio ? widthRatio : heightRatio;

        // TODO(lindahl): Improve compressed image quality (perhaps quality
        // ratio)

        int newWidth = (int) (src.getWidth() / scaleRatio);
        int newHeight = (int) (src.getHeight() / scaleRatio);
        int colorModel = fileExtension == ContentRepositoryUtils.ImageFileExtension.JPEG ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;
        BufferedImage target = new BufferedImage(newWidth, newHeight, colorModel);
        Graphics2D g = target.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newWidth, newHeight, Color.BLACK, null);
        g.dispose();
        ImageIO.write(target, fileExtension != null ? fileExtension.getValueWithoutDot() : "jpeg", out);
    }

    public byte[] getContentOfSize(Integer maxWidth, Integer maxHeight) {
        if (maxWidth == null && maxHeight != null) {
            return getContent();
        }
        byte[] out = null;
        if (this.storageType.equals(StorageType.S3) && this.inputStream != null) {
            try {
                out = resizeImage(this.inputStream, maxWidth != null ? maxWidth : Integer.MAX_VALUE,
                        maxHeight != null ? maxHeight : Integer.MAX_VALUE);
            } catch (IOException e) {
                LOG.error("Error occured.", e);
            }
        } else if (this.storageType.equals(StorageType.FILE_SYSTEM) && this.file != null) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(this.file);
                out = resizeImage(fis, maxWidth != null ? maxWidth : Integer.MAX_VALUE, maxHeight != null ? maxHeight : Integer.MAX_VALUE);
            } catch (IOException ex) {
                LOG.error("Error occured.", ex);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        LOG.error("Error occured.", ex);
                    }
                }
            }
        }
        return out;
    }

    private void setImageContentType(String filename) {
        fileExtension = ContentRepositoryUtils.ImageFileExtension.JPEG;

        if (StringUtils.endsWith(filename.toLowerCase(), ContentRepositoryUtils.ImageFileExtension.GIF.getValue())) {
            fileExtension = ContentRepositoryUtils.ImageFileExtension.GIF;
        } else if (StringUtils.endsWith(filename, ContentRepositoryUtils.ImageFileExtension.PNG.getValue())) {
            fileExtension = ContentRepositoryUtils.ImageFileExtension.PNG;
        }
    }

    public void updateContent(final File file) {
        this.file = file;
        if (this.file != null) {
            setImageContentType(this.file.getName());
        }
    }

    public String contentType() {
        return ContentRepositoryUtils.ImageMIMEtype.fromFileExtension(this.fileExtension).getValue();
    }

    public StorageType storageType() {
        return this.storageType;
    }

    public String name() {
        return this.file.getName();
    }

    public String location() {
        return this.location;
    }

    public void updateContent(final InputStream objectContent) {
        this.inputStream = objectContent;
    }

    public String getEntityDisplayName() {
        return this.entityDisplayName;
    }

    public boolean available() {
        int available = -1; // not -1
        if (this.storageType.equals(StorageType.S3) && this.inputStream != null) {
            try {
                available = this.inputStream.available();
            } catch (IOException e) {
                LOG.error("Error occured.", e);
            }
        } else if (this.storageType.equals(StorageType.FILE_SYSTEM) && this.file != null) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(this.file);
                available = fileInputStream.available();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                LOG.error("Error occured.", e);
            } catch (IOException e) {
                LOG.error("Error occured.", e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        LOG.error("Problem occurred in available function", e);
                    }
                }
            }
        }
        return available >= 0;
    }
}
